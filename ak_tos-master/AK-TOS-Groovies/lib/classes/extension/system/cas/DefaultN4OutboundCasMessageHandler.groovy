/*
 * Copyright (c) 2012 Navis LLC. All Rights Reserved.
 *
 */



package extension.system.cas
import com.navis.argo.ArgoBizMetafield
import com.navis.argo.ArgoField
import com.navis.argo.ContextHelper
import com.navis.argo.business.api.ArgoUtils
import com.navis.argo.business.api.IServiceEventFieldChange
import com.navis.argo.business.api.Serviceable
import com.navis.argo.business.atoms.CarrierVisitPhaseEnum
import com.navis.argo.business.atoms.EventEnum
import com.navis.argo.business.atoms.LocTypeEnum
import com.navis.argo.business.model.CarrierVisit
import com.navis.argo.business.model.VisitDetails
import com.navis.argo.business.xps.model.PointOfWork
import com.navis.argo.util.EntityXmlStreamWithSimpleHeader
import com.navis.external.services.AbstractGeneralNoticeCodeExtension
import com.navis.framework.business.Roastery
import com.navis.framework.metafields.MetafieldIdFactory
import com.navis.framework.persistence.HibernateApi
import com.navis.framework.portal.query.DomainQuery
import com.navis.framework.util.ValueObject
import com.navis.inventory.business.api.UnitField
import com.navis.inventory.business.moves.IWorkFinder
import com.navis.inventory.business.moves.WorkQueue
import com.navis.inventory.business.units.Unit
import com.navis.inventory.business.units.UnitFacilityVisit
import com.navis.rail.business.entity.TrainVisitDetails
import com.navis.services.business.event.Event
import com.navis.services.business.event.GroovyEvent
import com.navis.vessel.business.schedule.VesselVisitDetails
/**
 * This groovy is used to send messages triggered by an N4 event to QRCAS system
 * Supported Messages:
 * 1. CarrierVisitUpdate, when the status changes to 'Arrived' or 'Completed'
 * 2. Onboard unit Update  (yet to be implemented)
 * 3. Crane work list Update  (yet to be implemented)
 * 3. Ready to transfer  (yet to be implemented)
 *
 * @author <a href="mailto:arvinder.brar@navis.com">Arvinder Brar</a>, 9/8/12
 */
class DefaultN4OutboundCasMessageHandler extends AbstractGeneralNoticeCodeExtension {

  public final String VISIT_ID = "visitId"
  public final String VISIT_TYPE = "visitType"
  public final String CARRIER_VISIT_UPDATE = "carrierVisitUpdate"
  public final String CRANE_WORK_LIST_UPDATE = "CRANE_WORK_LIST_UPDATE"
  public final String RAIL_CRANE_WORK_LIST_UPDATE = "RAIL_CRANE_WORK_LIST_UPDATE"
  public final String REQUEST_TYPE = "requestType"
  public final String PHASE_VV = "PHASE_VV"
  public final String PHASE_RV = "PHASE_RV"
  public final String UNIT_ON_BOARD_UPDATE = "UNIT_ON_BOARD_UPDATE"
  public final String VISIT_TYPE_VESSEL = "VESSEL"
  public final String VISIT_TYPE_TRAIN = "TRAIN"
  public final String SNX = "snx"
  public final String FIELD_CHANGES = "fieldChanges";
  public final String CRANE_ID = "craneId";
  //The name of the cas helper groovy
  public final String CAS_HELPER = "CasHelper"
  public final String INTSERV_GROUP = "intservGroup"
  def _casHelperCodeExtInstance = null;

  /**
   * Main entry point method
   * @param inEvent groovy event
   */
  public void execute(GroovyEvent inEvent) {
    Event event = inEvent.getEvent();
    Serviceable serviceable = inEvent.getEntity();
    log(event.toString() + " Serviceable : " + serviceable.getLogEntityId() + ": " + serviceable.getHumanReadableKey())
    Map<String, String> additionalInfoMap = new HashMap<String, String>();
    _casHelperCodeExtInstance = getLibrary(CAS_HELPER);
    String xmlPayload = null;
    if (PHASE_VV.equals(event.getEvntEventType().getId()) || PHASE_RV.equals(event.getEvntEventType().getId())) {
      Serializable[] primaryKeys = new Serializable[1];
      primaryKeys[0] = serviceable.getPrimaryKey();
      String fieldChangesAsString = event.getEvntFieldChangesString();
      fieldChangesAsString = fieldChangesAsString.replaceAll("->", "-");
      IWorkFinder workFinder = Roastery.getBean(IWorkFinder.BEAN_ID) as IWorkFinder;
      final String cvId = ((VisitDetails) serviceable).getCvdCv().getCvId()
      final List<String> craneIds = workFinder.findPOWNamesForCarrierVisit(cvId)
      //Add field changes and cranes to the additional info
      addAdditionalInfoForCarrierVisitUpdate(serviceable, additionalInfoMap, fieldChangesAsString, craneIds)
      xmlPayload = createSnxBasedXmlPayload(additionalInfoMap,serviceable.getEntityName(),primaryKeys);
      //Record event
      if (serviceable  instanceof VesselVisitDetails) {
        recordEvent(serviceable, EventEnum.CAS_VESSEL_VISIT_UPDATE, null, null);
      } else  if (serviceable instanceof TrainVisitDetails){
        recordEvent(serviceable, EventEnum.CAS_TRAIN_VISIT_UPDATE, null, null);
      }

    }else if (UNIT_ON_BOARD_UPDATE.equals(event.getEvntEventType().getId())){
      Unit unit = serviceable as Unit
      UnitFacilityVisit unitFacilityVisit = unit.getUnitActiveUfvNowActive()
      Serializable[] primaryKeys = new Serializable[1];
      primaryKeys[0] = unitFacilityVisit.getPrimaryKey();

      //Check if the visit has changed
     ValueObject visitChangeVAO = null;
      List<ValueObject> fieldChangesVAOs = event.getEventFieldChanges();
      for (ValueObject fieldChangeVAO : fieldChangesVAOs) {
         if (UnitField.UNIT_DECLARED_IB_CV.equals(fieldChangeVAO.getFieldValue(ArgoBizMetafield.EVENT_FIELD_CHANGE_METAFIELD_ID))){
           visitChangeVAO = fieldChangeVAO
         }
      }
      DomainQuery dq = _casHelperCodeExtInstance.createUnitScalarQuery(primaryKeys);

      if (visitChangeVAO != null){
        LocTypeEnum locTypeEnum = unit.getUnitDeclaredIbCv().getLocType()
        String previousVisitId = (String)visitChangeVAO.getFieldValue(ArgoBizMetafield.EVENT_FIELD_CHANGE_PREV_VALUE)
        String newVisitId = (String)visitChangeVAO.getFieldValue(ArgoBizMetafield.EVENT_FIELD_CHANGE_NEW_VALUE)
        CarrierVisit previousCarrierVisit = CarrierVisit.findCarrierVisit(ContextHelper.getThreadFacility(), locTypeEnum, previousVisitId)
        CarrierVisit newCarrierVisit = CarrierVisit.findCarrierVisit(ContextHelper.getThreadFacility(), locTypeEnum, newVisitId)
        String unitXml = null;
        //Check both carrier visits
        if (newCarrierVisit != null && CarrierVisitPhaseEnum.WORKING == newCarrierVisit.getCvVisitPhase() && newCarrierVisit.getCvSendOnBoardUnitUpdates()){
          additionalInfoMap.put(VISIT_TYPE, _casHelperCodeExtInstance.translateValue(newCarrierVisit.getLocType()))
          additionalInfoMap.put(VISIT_ID, newVisitId)
          additionalInfoMap.put(REQUEST_TYPE, UNIT_ON_BOARD_UPDATE)
          additionalInfoMap.put("action", "add")
          unitXml = _casHelperCodeExtInstance.createUnitXml(dq)
          xmlPayload = _casHelperCodeExtInstance.getXmlPayloadContent(additionalInfoMap, unitXml)
          String webServiceResponse = ArgoUtils.invokeExternalBasicService( _casHelperCodeExtInstance.CAS_OUTBOUND, xmlPayload);
          log("Response from CAS Server for 'add' action: " + webServiceResponse);
        }
        if (previousCarrierVisit != null && CarrierVisitPhaseEnum.WORKING == previousCarrierVisit.getCvVisitPhase() && previousCarrierVisit.getCvSendOnBoardUnitUpdates()){
          additionalInfoMap.put(VISIT_TYPE, _casHelperCodeExtInstance.translateValue(previousCarrierVisit.getLocType()))
          additionalInfoMap.put(VISIT_ID, previousVisitId)
          additionalInfoMap.put(REQUEST_TYPE, UNIT_ON_BOARD_UPDATE)
          additionalInfoMap.put("action", "remove")
          if (unitXml == null) { //create unit xml only if it has not been created yet
            unitXml = _casHelperCodeExtInstance.createUnitXml(dq)
          }
          xmlPayload = _casHelperCodeExtInstance.getXmlPayloadContent(additionalInfoMap, unitXml)
          String webServiceResponse = ArgoUtils.invokeExternalBasicService( _casHelperCodeExtInstance.CAS_OUTBOUND, xmlPayload);
          log("Response from CAS Server for 'remove' action: " + webServiceResponse);
        }
        return;
      }else{
        additionalInfoMap.put(VISIT_TYPE, _casHelperCodeExtInstance.translateValue(unit.getUnitDeclaredIbCv().getLocType()))
        additionalInfoMap.put(VISIT_ID, unit.getUnitDeclaredIbCv().getCvId())
        additionalInfoMap.put(REQUEST_TYPE, UNIT_ON_BOARD_UPDATE)
        additionalInfoMap.put("action", "update")
        xmlPayload = _casHelperCodeExtInstance.getXmlPayloadContent(additionalInfoMap, _casHelperCodeExtInstance.createUnitXml(dq))
      }
    } else if (CRANE_WORK_LIST_UPDATE.equals(event.getEvntEventType().getId()) ||
            RAIL_CRANE_WORK_LIST_UPDATE.equals(event.getEvntEventType().getId())){
      String craneId = ""
      if (CRANE_WORK_LIST_UPDATE.equals(event.getEvntEventType().getId())) {
        //Related entity is a work queue
        WorkQueue workQueue = HibernateApi.getInstance().load(WorkQueue.class, event.getEvntRelatedEntityGkey()) as WorkQueue
        PointOfWork pointOfWork = workQueue.getWqPowViaWorkShift();
        if (pointOfWork != null) {
          craneId = workQueue.getWqPowViaWorkShift().getPointofworkName()
        }else{//Current POW is null, get the prior POW from field changes
          Iterator fcIt = event.getFieldChanges().iterator();
          while (fcIt.hasNext()) {
            IServiceEventFieldChange fc = (IServiceEventFieldChange) fcIt.next();
            if (ArgoField.POINTOFWORK_NAME.equals(fc.getMetafieldId())){
              craneId = fc.getPrevVal();
              break
            }
          }
        }
      } else { //Rail work list update
        craneId = "ALL"
      }
      VisitDetails visitDetails = serviceable as VisitDetails
      additionalInfoMap.put(VISIT_TYPE, _casHelperCodeExtInstance.translateValue(visitDetails.getCvdCv().getCvCarrierMode()))
      additionalInfoMap.put(VISIT_ID, visitDetails.getCvdCv().getCvId())
      additionalInfoMap.put("craneId", craneId)
      //Add request type to the message
      additionalInfoMap.put(REQUEST_TYPE, CRANE_WORK_LIST_UPDATE);
      xmlPayload = _casHelperCodeExtInstance.getXmlPayloadContent(additionalInfoMap, _casHelperCodeExtInstance.createCraneWorkListXmlContent(craneId, visitDetails, true))
    }
    log("Xml payload to be sent to  CAS Server : " + xmlPayload);
    //Check if an integration service with group name "CAS-Outbound" is present; if present send an outbound message to all the integration services
    // with that group name, if not, send message to an integration service by that name
    String webServiceResponse = null;
    if (Roastery.getMetafieldDictionary().isFieldDefined(MetafieldIdFactory.valueOf(INTSERV_GROUP))) {
        List<String> webResponseList = ArgoUtils.invokeExternalBasicServiceForGroup(_casHelperCodeExtInstance.CAS_OUTBOUND, xmlPayload);
        if (webResponseList.isEmpty()){ //check by integration service name
          webServiceResponse = ArgoUtils.invokeExternalBasicService(_casHelperCodeExtInstance.CAS_OUTBOUND, xmlPayload);
        }else{
          StringBuilder sb = new StringBuilder();
          for (String webResponse : webResponseList) {
            sb.append(webResponse).append("\n");
          }
          webServiceResponse = sb.toString();
        }
    } else {
        webServiceResponse = ArgoUtils.invokeExternalBasicService(_casHelperCodeExtInstance.CAS_OUTBOUND, xmlPayload);
    }
    log("Response from CAS Server : " + webServiceResponse);
  }
  /**
   * Adds additional information which is sent as part of xml payload
   * @param serviceable serviceable entity
   * @param additionalInfoMap map to which information is added
   * @param inFieldChangesAsString field changes
   * @param inCraneIds list of cranes which wil be working on the carrier visit
   */
  protected void addAdditionalInfoForCarrierVisitUpdate(Serviceable serviceable, HashMap<String, String> additionalInfoMap,
                                                        String inFieldChangesAsString, List<String> inCraneIds) {
    String craneIdsStr = ""
    if (inCraneIds != null && !inCraneIds.isEmpty()) {
      craneIdsStr =  inCraneIds.join(",")
    }
    additionalInfoMap.put(CRANE_ID, craneIdsStr);
    additionalInfoMap.put(FIELD_CHANGES, inFieldChangesAsString);
    //Add request type to the message
    additionalInfoMap.put(REQUEST_TYPE, CARRIER_VISIT_UPDATE);
    if (serviceable instanceof VesselVisitDetails) {
      additionalInfoMap.put(VISIT_TYPE, VISIT_TYPE_VESSEL);
    } else if (serviceable instanceof TrainVisitDetails) {
      additionalInfoMap.put(VISIT_TYPE, VISIT_TYPE_TRAIN);
    }
  }

  private String createSnxBasedXmlPayload(Map<String, String> inAdditionalInfo,String inEntityName, Serializable[] inPrimaryKeys){
    InputStream entityXmlStream = new EntityXmlStreamWithSimpleHeader(inEntityName, inPrimaryKeys, SNX, null);
    String snxContent = entityXmlStream.getText();
    return _casHelperCodeExtInstance.getXmlPayloadContent(inAdditionalInfo, unwrapSnxElement(snxContent));
  }

  private String unwrapSnxElement(String inSnxString){
    String returnString = inSnxString.replaceAll("<" + SNX + ">", "")
    return returnString.replaceAll("</" + SNX + ">", "")
  }
}
