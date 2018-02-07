/*
 * Copyright (c) 2013 Navis LLC. All Rights Reserved.
 *
 */



package extension.system.cas

import com.navis.argo.ArgoField
import com.navis.argo.business.api.ArgoUtils
import com.navis.argo.business.api.ServicesManager
import com.navis.argo.business.atoms.CarrierVisitPhaseEnum
import com.navis.argo.business.atoms.LocTypeEnum
import com.navis.argo.business.atoms.WiMoveStageEnum
import com.navis.argo.business.model.CarrierVisit
import com.navis.argo.business.xps.model.PointOfWork
import com.navis.argo.business.xps.model.WorkShift
import com.navis.external.framework.AbstractExtensionCallback
import com.navis.external.framework.entity.EEntityView
import com.navis.external.framework.util.EFieldChange
import com.navis.external.framework.util.EFieldChanges
import com.navis.external.framework.util.EFieldChangesView
import com.navis.framework.business.Roastery
import com.navis.framework.metafields.MetafieldId
import com.navis.framework.persistence.Entity
import com.navis.framework.persistence.HibernateApi
import com.navis.framework.portal.FieldChange
import com.navis.framework.portal.FieldChanges
import com.navis.inventory.MovesEntity
import com.navis.inventory.MovesField
import com.navis.inventory.business.moves.IWorkFinder
import com.navis.inventory.business.moves.WorkQueue
import com.navis.rail.business.entity.TrainVisitDetails
import com.navis.services.business.rules.EventType
import com.navis.vessel.business.schedule.VesselVisitDetails
import org.apache.log4j.Level
/**
 * This groovy is is used by CAS(Crane Automation System) and should be called from the entity interceptor of WorkInstruction and WorkQueue
 * entity interceptor.  It generates a "CRANE_WORK_LIST_UPDATE" or "RAIL_CRANE_WORK_LIST_UPDATE" event in case there are changes of interest to CAS.
 * To handle these events, a notice for each event needs to be created which executes a general notice code extension.
 * The default code extension provided as part of the project for this purpose is DefaultN4OutboundCasMessageHandler, which sends a
 * message to CAS system.
 * It assumes that follwing two custom notifiable have been created:
 * 1. "CRANE_WORK_LIST_UPDATE" event type with VesselVisit as a serviceable
 * 2. "RAIL_CRANE_WORK_LIST_UPDATE" event type with TrainVisit as a serviceable
 * @author <a href="mailto:arvinder.brar@navis.com">Arvinder Brar</a>, 1/21/13
 */
class CraneWorkListUpdate extends AbstractExtensionCallback{
  //User definable
  private final String EVENT_ID = "CRANE_WORK_LIST_UPDATE";
  private final String RAIL_EVENT_ID = "RAIL_CRANE_WORK_LIST_UPDATE";
  public void execute(Map inMapParam){
    String triggerTypeParam = null;
    EEntityView entityViewParam = null;
    EFieldChangesView originalFieldChangesParam = null;
    EFieldChanges moreFieldChangesParam = null;
    try {
      triggerTypeParam = (String) inMapParam.get("inTriggerType");
      entityViewParam = (EEntityView) inMapParam.get("inEntity");
      originalFieldChangesParam = (EFieldChangesView) inMapParam.get("inOriginalFieldChanges");
      moreFieldChangesParam = (EFieldChanges) inMapParam.get("inMoreFieldChanges");
    } catch (Exception inException) {
       log(Level.ERROR, "Exception while un-marshaling parameters" + inException.getLocalizedMessage())
    }

    if ("onUpdate".equals(triggerTypeParam)) {
      onUpdate(entityViewParam, originalFieldChangesParam, moreFieldChangesParam);
    }else if ("onCreate".equals(triggerTypeParam)) {
      onCreate(entityViewParam, originalFieldChangesParam, moreFieldChangesParam);
    }else if ("onDelete".equals(triggerTypeParam)) {
      onDelete(entityViewParam)
    }
  }
  protected void onDelete(EEntityView inEntity) {
    if (inEntity.getEntityName().equals(MovesEntity.WORK_INSTRUCTION) && inEntity.getField(MovesField.WI_MOVE_STAGE)!= WiMoveStageEnum.COMPLETE){
      WorkQueue workQueue = inEntity.getField(MovesField.WI_WORK_QUEUE) as WorkQueue
      if (!isValidUpdateCandidate(workQueue)){
        return
      }
      if (workQueue != null) {
        final FieldChanges eventFieldChanges = new FieldChanges()
        eventFieldChanges.setFieldChange(MovesField.WI_WORK_QUEUE, null, workQueue)
        recordCraneWorkListEventIfNeeded(workQueue, eventFieldChanges, true);
      }
    }
  }

  protected void onCreate(EEntityView inEntity, EFieldChangesView inOriginalFieldChanges, EFieldChanges inMoreFieldChanges) {
    if (inEntity.getEntityName().equals(MovesEntity.WORK_INSTRUCTION) && inEntity.getField(MovesField.WI_MOVE_STAGE)!= WiMoveStageEnum.COMPLETE){
      WorkQueue workQueue = inEntity.getField(MovesField.WI_WORK_QUEUE) as WorkQueue
      if (!isValidUpdateCandidate(workQueue)){
        return
      }
      //Field changes to be stashed in event
      if (workQueue != null) {
        final FieldChanges eventFieldChanges = new FieldChanges()
        eventFieldChanges.setFieldChange(MovesField.WI_WORK_QUEUE, null, workQueue)
        recordCraneWorkListEventIfNeeded(workQueue, eventFieldChanges, true);
      }
    }
  }

  protected boolean wqComplete(EFieldChangesView inOriginalFieldChanges, EEntityView inEntity) {
    if (!inOriginalFieldChanges.hasFieldChange(MovesField.WI_MOVE_STAGE)) {
      return false;
    }
    EFieldChange change = inOriginalFieldChanges.findFieldChange(MovesField.WI_MOVE_STAGE);
    if (!WiMoveStageEnum.COMPLETE.equals(change.getPriorValue()) && WiMoveStageEnum.COMPLETE.equals(change.getNewValue())){
      WorkQueue workQueue = inEntity.getField(MovesField.WI_WORK_QUEUE) as WorkQueue
      if (workQueue != null && workQueue.getLastSequenceNbr().equals(inEntity.getField(MovesField.WI_SEQUENCE))){
        return true;
      }
    }
    return false;
  }

  protected void onUpdate(EEntityView inEntity, EFieldChangesView inOriginalFieldChanges, EFieldChanges inMoreFieldChanges) {
    try {
      //Field changes to be stashed in event
      final FieldChanges eventFieldChanges = new FieldChanges()
      if (inEntity.getEntityName().equals(MovesEntity.WORK_INSTRUCTION) &&
              (inEntity.getField(MovesField.WI_MOVE_STAGE)!= WiMoveStageEnum.COMPLETE || wqComplete(inOriginalFieldChanges, inEntity))){
        WorkQueue parentQueue = inEntity.getField(MovesField.WI_WORK_QUEUE) as WorkQueue
        if (!isValidUpdateCandidate(parentQueue, inOriginalFieldChanges)){
          return
        }
        if (inOriginalFieldChanges.hasFieldChange(MovesField.WI_WORK_QUEUE)) {
          FieldChange fieldChange = registerEventFieldChange(MovesField.WI_WORK_QUEUE, inOriginalFieldChanges, eventFieldChanges)
          WorkQueue priorWorkQueue = fieldChange.getPriorValue() as WorkQueue
          recordCraneWorkListEventIfNeeded(priorWorkQueue, eventFieldChanges, true);
          //Get carrier visit of new WQ and record the event
          WorkQueue newWorkQueue = fieldChange.getNewValue() as WorkQueue
          recordCraneWorkListEventIfNeeded(newWorkQueue, eventFieldChanges, true);
        }else if (inOriginalFieldChanges.hasFieldChange(MovesField.WI_SEQUENCE)){
          registerEventFieldChange(MovesField.WI_SEQUENCE, inOriginalFieldChanges, eventFieldChanges)
          WorkQueue workQueue = inEntity.getField(MovesField.WI_WORK_QUEUE) as WorkQueue
          recordCraneWorkListEventIfNeeded(workQueue, eventFieldChanges, true);
        }else if (inOriginalFieldChanges.hasFieldChange(MovesField.WI_MOVE_STAGE)){
          final EFieldChange change = inOriginalFieldChanges.findFieldChange(MovesField.WI_MOVE_STAGE)
          WiMoveStageEnum newMoveStage = change.getNewValue() as WiMoveStageEnum
          Long moveSequence = inEntity.getField(MovesField.WI_SEQUENCE) as Long
          WorkQueue workQueue = inEntity.getField(MovesField.WI_WORK_QUEUE) as WorkQueue
          if (WiMoveStageEnum.COMPLETE == newMoveStage && workQueue != null && moveSequence != null && moveSequence == workQueue.getLastSequenceNbr()) {
            registerEventFieldChange(MovesField.WI_MOVE_STAGE, inOriginalFieldChanges, eventFieldChanges)
            recordCraneWorkListEventIfNeeded(workQueue, eventFieldChanges, false);//it is considered as a WQ change as the WQ has been completed
          }
        }
      }else if (inEntity.getEntityName().equals(MovesEntity.WORK_QUEUE)){
        if (inOriginalFieldChanges.hasFieldChange(MovesField.WQ_FIRST_RELATED_SHIFT)) {
          final EFieldChange eFieldChange = inOriginalFieldChanges.findFieldChange(MovesField.WQ_FIRST_RELATED_SHIFT);
          WorkShift priorWorkShift = eFieldChange.getPriorValue() as WorkShift
          WorkShift newWorkShift = eFieldChange.getNewValue() as WorkShift
          String priorPowName = null
          String newPowName = null
          if (priorWorkShift != null && priorWorkShift.getWorkshiftOwnerPow() != null){
            priorPowName = priorWorkShift.getWorkshiftOwnerPow().getPointofworkName()
          }
          if (newWorkShift != null && newWorkShift.getWorkshiftOwnerPow() != null){
            newPowName = newWorkShift.getWorkshiftOwnerPow().getPointofworkName()
          }
          FieldChange fieldChange = registerEventFieldChange(ArgoField.POINTOFWORK_NAME, priorPowName, newPowName, eventFieldChanges)
          recordCraneWorkListEventForQueueIfNeeded(inEntity, eventFieldChanges)

        }else if (inOriginalFieldChanges.hasFieldChange(MovesField.WQ_IS_BLUE)){
          registerEventFieldChange(MovesField.WQ_IS_BLUE, inOriginalFieldChanges, eventFieldChanges)
          recordCraneWorkListEventForQueueIfNeeded(inEntity, eventFieldChanges)
        }else if (inOriginalFieldChanges.hasFieldChange(MovesField.WQ_DOUBLE_CYCLE_TO_SEQUENCE)){
          registerEventFieldChange(MovesField.WQ_DOUBLE_CYCLE_TO_SEQUENCE, inOriginalFieldChanges, eventFieldChanges)
          recordCraneWorkListEventForQueueIfNeeded(inEntity, eventFieldChanges)
        }else if (inOriginalFieldChanges.hasFieldChange(MovesField.WQ_DOUBLE_CYCLE_FROM_SEQUENCE)){
          registerEventFieldChange(MovesField.WQ_DOUBLE_CYCLE_FROM_SEQUENCE, inOriginalFieldChanges, eventFieldChanges)
          recordCraneWorkListEventForQueueIfNeeded(inEntity, eventFieldChanges)
        }
      }
    } catch (Exception inBizViolation) {
      log(Level.ERROR, "Failed to post event CRANE_WORK_LIST_UPDATE " + inBizViolation.getLocalizedMessage());
    }
  }

  protected void recordCraneWorkListEventForQueueIfNeeded(EEntityView inEntity, FieldChanges eventFieldChanges) {
    Serializable wqGkey = inEntity.getField(MovesField.WQ_GKEY) as Serializable;
    WorkQueue workQueue = HibernateApi.getInstance().get(WorkQueue.class, wqGkey) as WorkQueue
    recordCraneWorkListEventIfNeeded(workQueue, eventFieldChanges, false)
  }

  protected void recordCraneWorkListEventIfNeeded(WorkQueue inWorkQueue, FieldChanges inEventFieldChanges, boolean inWiHasChanged){
    if (inWorkQueue != null) {
      CarrierVisit carrierVisit = inWorkQueue.getOwnerFromLocation()
      if (carrierVisit == null){
        log("Carrier visit is null for the work queue")
        return
      }else if(LocTypeEnum.VESSEL != carrierVisit.getCvCarrierMode() && LocTypeEnum.TRAIN != carrierVisit.getCvCarrierMode()) {
        log("Carrier mode is neither vessel not train. It is '" + carrierVisit.getCvCarrierMode() + "'")
      }
      if (shouldCreateCraneWorklistUpdateEvent(carrierVisit, inWorkQueue, inWiHasChanged)){
        recordCraneWorkListEvent(carrierVisit, inWorkQueue, inEventFieldChanges)
      }
    }else{
      log("The WorkQueue is null")
    }
  }

  protected FieldChange registerEventFieldChange(MetafieldId inMetafieldId, EFieldChangesView inOriginalFieldChanges, FieldChanges inEventFieldChanges){
      EFieldChange fieldChange = inOriginalFieldChanges.findFieldChange(inMetafieldId);
      FieldChange eventFieldChange = new FieldChange(fieldChange.getMetafieldId(), fieldChange.getPriorValue(), fieldChange.getNewValue())
    inEventFieldChanges.setFieldChange(eventFieldChange);
     return eventFieldChange
   }

  protected FieldChange registerEventFieldChange(MetafieldId inMetafieldId, Object inPriorValue, Object inNewValue, FieldChanges inEventFieldChanges){
    FieldChange eventFieldChange = new FieldChange(inMetafieldId, inPriorValue, inNewValue)
    inEventFieldChanges.setFieldChange(eventFieldChange);
    return eventFieldChange
  }

  protected void recordCraneWorkListEvent(CarrierVisit inCarrierVisit, Entity inRelatedEntity, FieldChanges inEventFieldChanges){
    ServicesManager sm = (ServicesManager) Roastery.getBean(ServicesManager.BEAN_ID);
    VesselVisitDetails vvd = VesselVisitDetails.resolveVvdFromCv(inCarrierVisit);
    if (vvd != null) {
      EventType eventType = EventType.findEventType(EVENT_ID);
      if (eventType != null) {
        sm.recordEvent(eventType, EVENT_ID, null, null, vvd, inEventFieldChanges,
                ArgoUtils.timeNow(), inRelatedEntity);
      } else {
        log("Unknown EventType: The event type needs to be created " + EVENT_ID);
      }
    } else {
      TrainVisitDetails tvd = TrainVisitDetails.resolveTvdFromCv(inCarrierVisit);
      if (tvd != null) {
        EventType railEventType = EventType.findEventType(RAIL_EVENT_ID);
        if (railEventType != null) {
          sm.recordEvent(railEventType, RAIL_EVENT_ID, null, null, tvd, inEventFieldChanges,
                  ArgoUtils.timeNow(), inRelatedEntity);
        } else {
          log("Unknown EventType: The event type needs to be created " + RAIL_EVENT_ID);
        }
      }
    }
  }

  protected boolean shouldCreateCraneWorklistUpdateEvent(CarrierVisit inCarrierVisit, WorkQueue inWorkQueue, boolean inIsWiChange){
    if (CarrierVisitPhaseEnum.WORKING == inCarrierVisit.getCvVisitPhase() && inCarrierVisit.getCvSendCraneWorkListUpdates()){
      if (LocTypeEnum.VESSEL == inCarrierVisit.getCvCarrierMode()) {//Restrict the changes for vessel queues
        if (inIsWiChange && !isCurrentOrNextWqForCrane(inWorkQueue)){
          return false
        }
      }
      return true
    }
    log("Carrier visit '" + inCarrierVisit.getCvId() +  "' is not in 'Working' phase or the SendCraneWorkListUpdates flag for this carrier visit is not true ")
    return false
  }

  protected boolean isCurrentOrNextWqForCrane(WorkQueue inWorkQueue){
    if (inWorkQueue == null){
      log("The WorkQueue for the WorkInstruction is null")
      return false
    }
    PointOfWork pointOfWork = inWorkQueue.getWqPowViaWorkShift();
    if (pointOfWork != null) {
      String craneId = inWorkQueue.getWqPowViaWorkShift().getPointofworkName()
      IWorkFinder workFinder = Roastery.getBean(IWorkFinder.BEAN_ID) as IWorkFinder
      final WorkQueue currentWorkQueue = workFinder.findCurrentWorkQueueForPOW(craneId)
      if (inWorkQueue.equals(currentWorkQueue) || inWorkQueue.equals(workFinder.findNextActiveWorkQueueForPOW(currentWorkQueue))){
        return true
      }
      log("The WorkQueue for the WorkInstruction is not the current or the next work queue for the crane assigned to it")
    }else{
      log("The POW for the work queue is null")
    }
    return false
  }
  /**
   * Validates if the WQ is not null and it is a valid vessel visit /train visit. It does the preliminary check, final decision whether
   * to send update or not is made later
   * @param inWorkQueue work queue
   * @return true if further processing is needed
   */
  protected boolean isValidUpdateCandidate(WorkQueue inWorkQueue){
    boolean shouldSendUpdate = false
    if (inWorkQueue != null) {
      CarrierVisit carrierVisit = inWorkQueue.getOwnerFromLocation()
      if (carrierVisit == null){
        log("Carrier visit is null for the work queue")
      }else {
        if (LocTypeEnum.VESSEL.equals(carrierVisit.getCvCarrierMode()) || LocTypeEnum.TRAIN.equals(carrierVisit.getCvCarrierMode())) {
          shouldSendUpdate = true
        }
      }
    }else{
      log("The WorkQueue is null")
    }
    return shouldSendUpdate
  }
  /**
   * Validates if the WQ is not null and it is a valid vessel visit /train visit. It does the preliminary check, final decision whether
   * to send update or not is made later. The wi with updated WQ can come in as two updates; in first case the new value is null,
   * in second case the prior value is null.

   * @param inWorkQueue work queue
   * @param inOriginalFieldChanges original field changes
   * @return true if further processing is needed
   */
  protected boolean isValidUpdateCandidate(WorkQueue inWorkQueue, EFieldChangesView inOriginalFieldChanges){
    boolean shouldSendUpdate = false
    if (inWorkQueue != null) {
      CarrierVisit carrierVisit = inWorkQueue.getOwnerFromLocation()
      if (carrierVisit == null){
        log("Carrier visit is null for the work queue")
      }else {
        if (LocTypeEnum.VESSEL.equals(carrierVisit.getCvCarrierMode()) || LocTypeEnum.TRAIN.equals(carrierVisit.getCvCarrierMode())) {
          shouldSendUpdate = true
        }
      }
    }else{//check if the prior WQ meets the criteria
      log("The WorkQueue is null")
      if (inOriginalFieldChanges.hasFieldChange(MovesField.WI_WORK_QUEUE)) {
        EFieldChange fieldChange = inOriginalFieldChanges.findFieldChange(MovesField.WI_WORK_QUEUE);
        WorkQueue priorWorkQueue = fieldChange.getPriorValue() as WorkQueue
        if (priorWorkQueue != null) {
          CarrierVisit carrierVisit = priorWorkQueue.getOwnerFromLocation()
          if (carrierVisit == null){
            log("Carrier visit is null for the prior work queue")
          }else {
            if (LocTypeEnum.VESSEL.equals(carrierVisit.getCvCarrierMode()) || LocTypeEnum.TRAIN.equals(carrierVisit.getCvCarrierMode())) {
              shouldSendUpdate = true
            }
          }
        }else{
          log("The prior workQueue is null")
        }
      }
    }
    return shouldSendUpdate
  }
}