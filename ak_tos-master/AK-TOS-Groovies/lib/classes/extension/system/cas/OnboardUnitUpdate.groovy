/*
 * Copyright (c) 2013 Navis LLC. All Rights Reserved.
 *
 */

package extension.system.cas
import com.navis.argo.ArgoRefEntity
import com.navis.argo.ArgoRefField
import com.navis.argo.ContextHelper
import com.navis.argo.business.api.ServicesManager
import com.navis.argo.business.atoms.CarrierVisitPhaseEnum
import com.navis.argo.business.atoms.LocTypeEnum
import com.navis.argo.business.model.CarrierVisit
import com.navis.argo.business.reference.Container
import com.navis.argo.business.reference.Equipment
import com.navis.external.framework.AbstractExtensionCallback
import com.navis.external.framework.entity.EEntityView
import com.navis.external.framework.util.EFieldChange
import com.navis.external.framework.util.EFieldChanges
import com.navis.external.framework.util.EFieldChangesView
import com.navis.framework.business.Roastery
import com.navis.framework.metafields.MetafieldId
import com.navis.framework.persistence.HibernateApi
import com.navis.framework.portal.FieldChange
import com.navis.framework.portal.FieldChanges
import com.navis.inventory.InventoryEntity
import com.navis.inventory.InventoryField
import com.navis.inventory.business.api.UnitField
import com.navis.inventory.business.api.UnitFinder
import com.navis.inventory.business.units.Unit
import com.navis.inventory.business.units.UnitEquipment
import com.navis.services.business.rules.EventType
import org.apache.log4j.Level
/**
 * This groovy is is used by CAS(Crane Automation System) and should be called from the entity interceptor of Unit, UnitFacilityVisit
 * and Container interceptor.  It generates a "UNIT_ON_BOARD_UPDATE" event in case there are changes of interest to CAS.
 * To handle this event, a notice needs to be created which executes a general notice code extension. The default code extension provided
 * as part of the project for this purpose is DefaultN4OutboundCasMessageHandler, which sends a message to CAS system.
  * It assumes a custom notifiable "UNIT_ON_BOARD_UPDATE" event type with Unit as a serviceable has been created.
 * @author <a href="mailto:arvinder.brar@navis.com">Arvinder Brar</a>, 1/10/13
 */
class OnboardUnitUpdate extends AbstractExtensionCallback{
  //User definable
  private final String EVENT_ID = "UNIT_ON_BOARD_UPDATE";

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
       log(Level.ERROR, "Exception while unmarshalling parameters" + inException.getLocalizedMessage())
    }

    if ("onUpdate".equals(triggerTypeParam)) {
      onUpdate(entityViewParam, originalFieldChangesParam, moreFieldChangesParam);
    }
  }
  public void onUpdate(EEntityView inEntity, EFieldChangesView inOriginalFieldChanges, EFieldChanges inMoreFieldChanges) {
    FieldChanges eventFieldChanges = new FieldChanges();
    Unit unit = null;
    if (inEntity.getEntityName().equals(InventoryEntity.UNIT_FACILITY_VISIT)) {
      registerEventFieldChangeIfPresent(InventoryField.UFV_LAST_KNOWN_POSITION, inOriginalFieldChanges, eventFieldChanges);
    }else if (inEntity.getEntityName().equals(InventoryEntity.UNIT)) {
      registerEventFieldChangeIfPresent(UnitField.UNIT_DECLARED_IB_CV, inOriginalFieldChanges, eventFieldChanges);
      registerEventFieldChangeIfPresent(UnitField.UNIT_GOODS_AND_CTR_WT_KG, inOriginalFieldChanges, eventFieldChanges);
    }else if (inEntity.getEntityName().equals(ArgoRefEntity.CONTAINER)) {
      registerEventFieldChangeIfPresent(ArgoRefField.EQ_EQUIP_TYPE, inOriginalFieldChanges, eventFieldChanges);
      registerEventFieldChangeIfPresent(ArgoRefField.EQ_LENGTH_MM, inOriginalFieldChanges, eventFieldChanges);
      registerEventFieldChangeIfPresent(ArgoRefField.EQ_HEIGHT_MM, inOriginalFieldChanges, eventFieldChanges);
    }
    if (eventFieldChanges.getFieldChangeCount() > 0){
      //Get the unit
      if (inEntity.getEntityName().equals(InventoryEntity.UNIT_FACILITY_VISIT)) {
         unit = (Unit) inEntity.getField(UnitField.UFV_UNIT);
      }else if (inEntity.getEntityName().equals(InventoryEntity.UNIT)) {
        Serializable unitGkey = inEntity.getField(UnitField.UNIT_GKEY) as Serializable;
        unit = HibernateApi.getInstance().load(Unit.class, unitGkey) as Unit
      }else if (inEntity.getEntityName().equals(ArgoRefEntity.CONTAINER)){
        Serializable eqGkey = inEntity.getField(ArgoRefField.EQ_GKEY) as Serializable;
        Equipment equipment = HibernateApi.getInstance().load(Container.class, eqGkey) as Equipment
        UnitFinder uf = (UnitFinder) Roastery.getBean(UnitFinder.BEAN_ID);
        UnitEquipment ue = uf.findActiveUeUsingEqInAnyRole(ContextHelper.getThreadOperator(), ContextHelper.getThreadComplex(), equipment);
        if (ue != null) {
          unit = ue.getUeUnit();
        }
      }

      try {
        if (unit == null){
          return;
        }
        //Position should be vessel or rail car
        if (LocTypeEnum.VESSEL == unit.getLocType() || LocTypeEnum.RAILCAR == unit.getLocType()){
          boolean createEvent =  false;
          //Current carrier visit phase should be working and the on board unit update flag should be on
          CarrierVisit inboundCV = unit.getUnitDeclaredIbCv();
          if (CarrierVisitPhaseEnum.WORKING == inboundCV.getCvVisitPhase() && inboundCV.getCvSendOnBoardUnitUpdates()){
            createEvent = true;
          }
          if (!createEvent){ //If carrier visit has changed, check if the new carrier visit satisfies the above requirements
            if (inOriginalFieldChanges.hasFieldChange(UnitField.UNIT_DECLARED_IB_CV)) {
              EFieldChange fieldChange = inOriginalFieldChanges.findFieldChange(UnitField.UNIT_DECLARED_IB_CV);
              //Only need to check the current changed visit, the previous visit is handled above, not that the value has not been updated yet
              // so the existing value is still the previous visit
              CarrierVisit newCarrierVisit = (CarrierVisit)fieldChange.getNewValue();
              if (CarrierVisitPhaseEnum.WORKING == newCarrierVisit.getCvVisitPhase() && newCarrierVisit.getCvSendOnBoardUnitUpdates()){
                createEvent = true;
              }
            }
          }
          if (createEvent) {
            ServicesManager sm = (ServicesManager) Roastery.getBean(ServicesManager.BEAN_ID);
            EventType eventType = EventType.findEventType(EVENT_ID);
            if (eventType == null) {
              log("Unknown EventType: " + EVENT_ID);
            } else {
              sm.recordEvent(eventType, EVENT_ID, null, null, unit, eventFieldChanges);
            }
          }
        }
      } catch (Exception inBizViolation) {
        log(Level.ERROR, "Failed to post event UNIT_ON_BOARD_UPDATE " + inBizViolation.getLocalizedMessage());
      }
    }
  }

  protected void registerEventFieldChangeIfPresent(MetafieldId inMetafieldId, EFieldChangesView inOriginalFieldChanges, FieldChanges eventFieldChanges) {
    if (inOriginalFieldChanges.hasFieldChange(inMetafieldId)) {
      EFieldChange fieldChange = inOriginalFieldChanges.findFieldChange(inMetafieldId);
      FieldChange eventFieldChange = new FieldChange(fieldChange.getMetafieldId(), fieldChange.getPriorValue(), fieldChange.getNewValue())
      eventFieldChanges.setFieldChange(eventFieldChange);
    }
  }

}
