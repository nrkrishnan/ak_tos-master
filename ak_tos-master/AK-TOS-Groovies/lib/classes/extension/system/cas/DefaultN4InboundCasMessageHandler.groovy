/*
 * Copyright (c) 2013 Navis LLC. All Rights Reserved.
 *
 */

package extension.system.cas

import com.navis.argo.ArgoField
import com.navis.argo.ArgoPropertyKeys
import com.navis.argo.ArgoRefField
import com.navis.argo.ContextHelper
import com.navis.argo.business.api.ArgoUtils
import com.navis.argo.business.api.IEventType
import com.navis.argo.business.api.ServicesManager
import com.navis.argo.business.atoms.*
import com.navis.argo.business.model.CarrierVisit
import com.navis.argo.business.reference.*
import com.navis.argo.business.snx.IPropertyResolver
import com.navis.argo.business.xps.model.Che
import com.navis.control.EciBizMetafield
import com.navis.control.eci.api.EciEsbConstants
import com.navis.control.eci.util.EciEsbHelper
import com.navis.external.argo.AbstractGroovyWSCodeExtension
import com.navis.framework.business.Roastery
import com.navis.framework.portal.query.DomainQuery
import com.navis.framework.util.BizViolation
import com.navis.inventory.InventoryField
import com.navis.inventory.InventoryPropertyKeys
import com.navis.inventory.business.api.HcTbdUnitManager
import com.navis.inventory.business.api.RectifyParms
import com.navis.inventory.business.api.UnitManager
import com.navis.inventory.business.atoms.*
import com.navis.inventory.business.imdg.ObservedPlacard
import com.navis.inventory.business.moves.WorkInstruction
import com.navis.inventory.business.units.Unit
import com.navis.inventory.business.units.UnitEquipment
import com.navis.inventory.business.units.UnitFacilityVisit
import com.navis.services.business.rules.EventType
import groovy.xml.MarkupBuilder
import org.apache.commons.lang.StringUtils
import org.apache.log4j.Level

/**
 * This code extension is handler for inbound messages of all types from CAS system.
 * It handles the following messages:
 * 1. Unit Capture
 * 2. Unit Position Update
 *
 * @author <a href="mailto:arvinder.brar@navis.com">Arvinder Brar</a>, 9/13/12
 */
class DefaultN4InboundCasMessageHandler extends AbstractGroovyWSCodeExtension{

  protected final String REQUEST_TYPE_PARAM = "requestType"
  protected final String VISIT_ID_PARAM = "visitId"
  protected final String VISIT_TYPE_PARAM = "visitType"
  protected final String VISIT_TYPE_VESSEL = "VESSEL"
  protected final String VISIT_TYPE_TRAIN = "TRAIN"
  protected final String SEND_READY_TO_TRANSFER_MESSAGE_PARAM = "sendReadyToTransferMessage"
  protected final String UNITS_XML_PARAM = "unitXml"
  protected final String UNIT_CAPTURE_MESSAGE = "unitCaptureMessage";
  protected final String ACTION_PARAM = "action"
  protected final String UNIT_CAPTURE_IMAGE_TYPE = "Image";
  protected final String UNIT_CAPTURE_IDENTIFY_TYPE = "Identify";
  protected final String UNIT_CAPTURE_CREATE_TYPE = "Create";
  protected final String UNIT_CAPTURE_UPDATE_TYPE = "Update";
  public final String UNIT_ID = "unitId"
  public final String CRANE_ID_PARAM = "craneId"
  //specific fields 3.0 and later releases
  protected final String UNIT_POSITION_UPDATE_LIFT = "Lift";
  protected final String UNIT_POSITION_UPDATE_SET = "Set";
  protected final String UNIT_POSITION_UPDATE = "unitPositionUpdateMessage";
  protected final String READY_TO_TRANSFER_MESSAGE = "readyToTransferMessage";
  public final String READY_TO_TRANSFER = "readyToTransfer"

  // parameter fields
  protected String _requestType
  protected String _visitId
  protected String _visitType
  protected String _craneId
  protected String _unitsXml
  protected CasInUnit[] _casInUnits
  protected int _unitCount
  protected String _action
  protected boolean _sendReadyToTransferMessage = true

  protected enum Status {SUCCESS, ERROR, WARNING}
  //error code and message which are populated for non-unit specific errors, unit specific arrors are handled at unit level
  private String _errorCode = null;
  private String _errorMessage = null;

  protected CarrierVisit _carrierVisit
  //CasHelper library name
  public final String CAS_HELPER = "CasHelper"
  //CasHelper library class
  def _casHelper = null;

  //CasMessageHelper library name
  public final String CAS_MESSAGE_HELPER = "CasMessageHelper"
  //CasHelper library class

  def _casMessageHelper = null

  //Datasource for equipment updates for this code extension
  private final DataSourceEnum _dataSourceEnum = DataSourceEnum.USER_LCL;


  /**
   * Main entry point method.
   * @param inParameters parameters sent as part of groovy web service
   * @return the string response to the groovy webservice call
   */
  public String execute(Map inParameters) {
    //Log the request content
    log("\nRequest: " + getParametersAsString())
    Map<String, String> additionalInfoMap = new HashMap<String, String>();
    //Get the CasMessageHelper library
    initCasMessageHelper();
    //Validate that the unit xml is present and valid
    _unitsXml = _parameterMap.get(UNITS_XML_PARAM)
    if (StringUtils.isBlank(_unitsXml)){
      _errorCode = _casMessageHelper.MISSING_UNIT_XML_CODE
      _errorMessage = _casMessageHelper.MISSING_UNIT_XML_MESSAGE
      registerAndLogError()
      return getXmlErrorContent();
    }
    Node unitsNode = null;
    // Parse the XML message
    try {
      unitsNode = new XmlParser().parseText(_unitsXml);
    } catch (Exception ex) {
      _errorCode = _casMessageHelper.INVALID_UNIT_XML_CODE
      _errorMessage = _casMessageHelper.INVALID_UNIT_XML_MESSAGE
      registerAndLogError()
      ex.printStackTrace();
      return getXmlErrorContent();
    }
    def units = unitsNode.'unit'
    _unitCount = units.size()
    if (!"units".equals(unitsNode.name()) || _unitCount == 0){
      _errorCode = _casMessageHelper.INVALID_UNIT_XML_CODE
      _errorMessage = _casMessageHelper.INVALID_UNIT_XML_MESSAGE
      registerAndLogError()
      return getXmlErrorContent();
    }
    // Get CasHelper library
    _casHelper = getLibrary(CAS_HELPER)

    loadParameters(unitsNode);
    //Validate the required parameters for the message
    if (!validateParameters()){
      return getXmlErrorContent();
    }

    if (UNIT_CAPTURE_MESSAGE.equals(_requestType)){
      // Validate Capture type
      boolean isValidAction = UNIT_CAPTURE_IMAGE_TYPE.equals(_action) || UNIT_CAPTURE_IDENTIFY_TYPE.equals(_action) ||
              UNIT_CAPTURE_CREATE_TYPE.equals(_action) ||UNIT_CAPTURE_UPDATE_TYPE.equals(_action);
      if (!isValidAction){
        _errorCode = _casMessageHelper.INVALID_CAPTURE_TYPE_CODE
        _errorMessage = _casMessageHelper.INVALID_CAPTURE_TYPE_MESSAGE
        registerAndLogError()
        return getXmlErrorContent();
      }
      processUnitCapture();
      if (UNIT_CAPTURE_IMAGE_TYPE.equals(_action) || UNIT_CAPTURE_IDENTIFY_TYPE.equals(_action)) {
        if (_sendReadyToTransferMessage) {
          sendReadyToTransferMessage()
        } else {
          log("Skipping sending ready to transfer message as the request has explicitly prohibited it")
        }
      }
    } else if (UNIT_POSITION_UPDATE.equals(_requestType)){
      // Validate position type
      boolean isValidAction = UNIT_POSITION_UPDATE_LIFT.equals(_action) || UNIT_POSITION_UPDATE_SET.equals(_action) ;
      if (!isValidAction){
        _errorCode = _casMessageHelper.INVALID_UNIT_POSITION_TYPE_CODE
        _errorMessage = _casMessageHelper.INVALID_UNIT_POSITION_TYPE_MESSAGE
        registerAndLogError()
        return getXmlErrorContent();
      }
      processUnitPositionUpdate();
    } else {
      _errorCode = _casMessageHelper.INVALID_MESSAGE_REQUEST_TYPE_CODE
      _errorMessage = _casMessageHelper.INVALID_MESSAGE_REQUEST_TYPE_MESSAGE
      registerAndLogError()
      return getXmlErrorContent();
    }
    //An example of adding additional information
    /* additionalInfoMap.put("aField", "aFieldValue");*/

    final String responseXml = createResponseXml(additionalInfoMap)
    log("\nRequest: " + getParametersAsString() + "\nResponse : " + responseXml)
    return responseXml;
  }

  /**
   * Loads the parameters to fields, so that they are available to all methods
   */
  protected void loadParameters(Node inUnitsNode){
    _casInUnits = new CasInUnit[_unitCount]
    _requestType = _parameterMap.get(REQUEST_TYPE_PARAM);
    _visitId = _parameterMap.get(VISIT_ID_PARAM)
    _visitType = _parameterMap.get(VISIT_TYPE_PARAM)
    _craneId = _parameterMap.get(CRANE_ID_PARAM)
    _action = _parameterMap.get(ACTION_PARAM)
    String sendReadyToTransferMessage = _parameterMap.get(SEND_READY_TO_TRANSFER_MESSAGE_PARAM)
    if (!StringUtils.isBlank(sendReadyToTransferMessage) && "N".equalsIgnoreCase(sendReadyToTransferMessage)){
      _sendReadyToTransferMessage = false
    }
    def units = inUnitsNode.'unit'
    int i = 0
    units.each{Node unitNode ->
      _casInUnits[i] = new CasInUnit(unitNode)
      initializeCasUnit(_casInUnits[i]);
      i++;
    }
  }
  /**
   * Processes the unit capture message after preliminary validations have been done
   */
  protected void processUnitCapture(){
    for (CasInUnit casInUnit : _casInUnits){
      if (!casInUnit.hasError()) {
        validateUnitForCapture(casInUnit);
      }
      if (!casInUnit.hasError()) {
        handleUnitCaptureAndUpdate(casInUnit)
      }
      if (!casInUnit.hasError() && !(Status.WARNING == casInUnit.getReturnStatus())){
        casInUnit.setReturnStatus(Status.SUCCESS)
        casInUnit.setReturnCode(_casMessageHelper.UNIT_CAPTURE_HANDLED_SUCCESSFULLY_CODE)
        casInUnit.setReturnMessage(_casMessageHelper.UNIT_CAPTURE_HANDLED_SUCCESSFULLY_MESSAGE + "[Action=" + _action + "]")
      }
    }
  }
  /**
   * Processes the unit position update message after preliminary validations have been done
   */

  protected void processUnitPositionUpdate(){
    for (CasInUnit casInUnit : _casInUnits){
      if (!casInUnit.hasError()) {
        validateUnitForPositionUpdate(casInUnit);
      }
      if (!casInUnit.hasError()) {
        handleUnitPositionUpdate(casInUnit)
      }
      if (!casInUnit.hasError() && !(Status.WARNING == casInUnit.getReturnStatus())){
        casInUnit.setReturnStatus(Status.SUCCESS)
        casInUnit.setReturnCode(_casMessageHelper.UNIT_POSITION_UPDATED_SUCCESSFULLY_CODE)
        casInUnit.setReturnMessage(_casMessageHelper.UNIT_POSITION_UPDATED_SUCCESSFULLY_MESSAGE + "[Action=" + _action + "]")
      }
    }
  }
  protected void validateTbdLoadStatus(CasInUnit inCasInUnit) {
    if (inCasInUnit.getUnitFacilityVisit().getFinalPlannedPosition().equals(null)) {
      HcTbdUnitManager hcTbdMngr = (HcTbdUnitManager) Roastery.getBean(HcTbdUnitManager.BEAN_ID);
      hcTbdMngr.validateTbdUnitForLoad(inCasInUnit.getUnitFacilityVisit(), _carrierVisit);
    }
  }
  /**
   * Validate the required parameters for the message, for the unit's required parameters message is added to CasInUnit
   * @return true if all the required parameters for message as a whole are valid, for unit attributes it sets hasError of
   * unit to true and adds a return message for that unit, so that the other valid units are processed.
   */
  protected boolean validateParameters(){
    boolean isValid = true;
    //validate request type parameter
    if (StringUtils.isBlank(_requestType)){
      _errorCode = _casMessageHelper.MISSING_REQUEST_TYPE_CODE
      _errorMessage = _casMessageHelper.MISSING_REQUEST_TYPE_MESSAGE
      registerAndLogError()
      return false;
    }
    //Validate 'visitType' and 'visitId' parameters
    if (StringUtils.isBlank(_visitId)){
      _errorCode = _casMessageHelper.MISSING_VISIT_ID_CODE
      _errorMessage = _casMessageHelper.MISSING_VISIT_ID_MESSAGE
      registerAndLogError()
      return false;
    }
    if (StringUtils.isBlank(_visitType)){
      _errorCode = _casMessageHelper.MISSING_VISIT_TYPE_CODE
      _errorMessage = _casMessageHelper.MISSING_VISIT_TYPE_MESSAGE
      registerAndLogError()
      return false;
    }else if (!(_visitType.equals(VISIT_TYPE_VESSEL) || _visitType.equals(VISIT_TYPE_TRAIN))){
      _errorCode = _casMessageHelper.INVALID_VISIT_TYPE_CODE
      _errorMessage = _casMessageHelper.INVALID_VISIT_TYPE_MESSAGE
      registerAndLogError()
      return false;
    }
    //Validate the carrier visit
    if (_visitType.equals(VISIT_TYPE_VESSEL)){
      _carrierVisit = CarrierVisit.findVesselVisit(ContextHelper.getThreadFacility(), _visitId)
    }else if (_visitType.equals(VISIT_TYPE_TRAIN)){
      _carrierVisit = CarrierVisit.findTrainVisit(ContextHelper.getThreadComplex(), ContextHelper.getThreadFacility(), _visitId)
    }
    if (_carrierVisit == null ){
      _errorCode = _casMessageHelper.NO_CARRIER_VISIT_FOUND_CODE
      _errorMessage = _casMessageHelper.NO_CARRIER_VISIT_FOUND_MESSAGE + "[visitId=" +  _visitId + "]"
      registerAndLogError()
      return false;
    }
    if (StringUtils.isBlank(_craneId)){
      _errorCode = _casMessageHelper.MISSING_CRANE_ID_CODE
      _errorMessage = _casMessageHelper.MISSING_CRANE_ID_MESSAGE
      registerAndLogError()
      return false;
    }else{
      try {
        Che quayCrane = _casHelper.validateCraneId(_craneId);
        if (UNIT_CAPTURE_MESSAGE.equals(_requestType)){
          boolean isValidCaptureAction = UNIT_CAPTURE_IMAGE_TYPE.equals(_action) || UNIT_CAPTURE_IDENTIFY_TYPE.equals(_action) ||
                  UNIT_CAPTURE_CREATE_TYPE.equals(_action) ||UNIT_CAPTURE_UPDATE_TYPE.equals(_action);
          if (isValidCaptureAction){
            //Check if OCR data is being accepted; null value equated to true value, that is, OCR data ia being accepted
            boolean dataAccepted = quayCrane.getCheIsOcrDataBeingAccepted() == null? true : quayCrane.getCheIsOcrDataBeingAccepted();
            if (!dataAccepted) {
              _errorCode = _casMessageHelper.CRANE_OCR_DATA_NOT_BEING_ACCEPTED;
              _errorMessage = _casMessageHelper.CRANE_OCR_DATA_NOT_BEING_ACCEPTED_MESSAGE + "[craneId=" +  _craneId + "]"
              registerAndLogError()
              return false;
            }
          }
        }
      } catch (Exception ex) {
        _errorCode = _casMessageHelper.INVALID_CRANE_ID_CODE
        _errorMessage = _casMessageHelper.INVALID_CRANE_ID_MESSAGE + "[craneId=" +  _craneId + "]"
        registerAndLogError()
        return false;
      }
    }

    //Validate the required attributes of unit nodes
    for (CasInUnit casInUnit : _casInUnits){
      //Validate 'casReferenceId'
      if (StringUtils.isBlank(casInUnit.getCasUnitReference())){
        log(_casMessageHelper.MISSING_CAS_UNIT_REFERENCE_CODE + ":" + _casMessageHelper.MISSING_CAS_UNIT_REFERENCE_MESSAGE);
        casInUnit.setReturnStatus(Status.ERROR);
        casInUnit.setReturnCode(_casMessageHelper.MISSING_CAS_UNIT_REFERENCE_CODE)
        casInUnit.setReturnMessage(_casMessageHelper.MISSING_CAS_UNIT_REFERENCE_MESSAGE)
      }
    }
    return isValid;
  }
  /**
   * Validates the attributes of CasInUnit for the unit capture message
   * @param inCasInUnit the CasInUnit to be validated
   */
  protected void validateUnitForCapture(CasInUnit inCasInUnit){
    //Validate unit id
    if (!UNIT_CAPTURE_IMAGE_TYPE.equals(_action) && StringUtils.isBlank(inCasInUnit.getUnitId())){
      log(_casMessageHelper.MISSING_UNIT_ID_CODE + ":" + _casMessageHelper.MISSING_UNIT_ID_MESSAGE);
      inCasInUnit.setReturnStatus(Status.ERROR);
      inCasInUnit.setReturnCode(_casMessageHelper.MISSING_UNIT_ID_CODE)
      inCasInUnit.setReturnMessage(_casMessageHelper.MISSING_UNIT_ID_MESSAGE)
      return
    }
    //Validate unit
    try {
      if(UNIT_CAPTURE_IMAGE_TYPE.equals(_action) || UNIT_CAPTURE_CREATE_TYPE.equals(_action)){
         return;//nothing to validate
      }

      try {
        validateUnitActive(inCasInUnit)
      } catch (BizViolation inBv) {
          if (UNIT_CAPTURE_IDENTIFY_TYPE.equals(_action)) {
              final Map<Object, Object> serviceParams = new HashMap<Object, Object>();
              serviceParams.put(EciBizMetafield.CAS_SERVICE_TYPE, EciEsbConstants.ECISERVICE_TYPE_QCAS_CONTAINER_MISIDENTIFIED);
              enhanceServiceParamsMapWithUnitAttributes(serviceParams, inCasInUnit);
              EciEsbHelper.enqueueEciServiceRequest(EciEsbConstants.ECISERVICE_QUEUENAME_QCAS, EciEsbConstants.ECISERVICE_ENDPOINT_REF_QCASFLOW,
                      inCasInUnit.getCasUnitReference(), serviceParams);
          }
          throw inBv;
      }
      UnitFacilityVisit unitFacilityVisit = inCasInUnit.getUnitFacilityVisit()
      //Validate that the in case of "Identify" action, unit is valid for load/discharge
      if (UNIT_CAPTURE_IDENTIFY_TYPE.equals(_action)){
        boolean isLoad = false;
        List<WorkInstruction> currentWIList = unitFacilityVisit.getCurrentWiList();
        if (currentWIList == null || currentWIList.isEmpty()){
          log(_casMessageHelper.NOT_IN_LOAD_OR_DSCH_PLAN_CODE + ":" + _casMessageHelper.NOT_IN_LOAD_OR_DSCH_PLAN_MESSAGE);
          inCasInUnit.setReturnStatus(Status.WARNING);
          inCasInUnit.setReturnCode(_casMessageHelper.NOT_IN_LOAD_OR_DSCH_PLAN_CODE)
          inCasInUnit.setReturnMessage(_casMessageHelper.NOT_IN_LOAD_OR_DSCH_PLAN_MESSAGE)
          _casHelper.recordExceptionServiceEvent(_craneId, inCasInUnit.getUnitFacilityVisit(), ContextHelper.getThreadYard(),
                  EventType.resolveIEventType(EventEnum.CHE_ERROR_UNIT_NOT_IN_LOAD_DISCH_LIST),
                  _casMessageHelper.NOT_IN_LOAD_OR_DSCH_PLAN_MESSAGE);
          return
        }
        WorkInstruction targetWI = null;
        if (RestowTypeEnum.RESTOW.equals(unitFacilityVisit.getUfvRestowType()) ||
                UnitCategoryEnum.TRANSSHIP.equals(unitFacilityVisit.getUfvUnit().getUnitCategory())) {
          for (WorkInstruction currentWi : currentWIList) {
            if (currentWi.getWiMoveKind().equals(WiMoveKindEnum.VeslDisch)) {
              targetWI = currentWi;
              break;
            }
          }
        }
        if (targetWI == null) {
          targetWI = currentWIList.get(0);
        }

        if (targetWI.getWiToPosition().isVesselPosition() || targetWI.getWiToPosition().isRailPosition()) {
          isLoad = true;
        }
        if (isLoad){
          CarrierVisit obCv  = unitFacilityVisit.getUfvActualObCv();
          if (!obCv.getCvId().equals(_visitId)){
            log(_casMessageHelper.NOT_IN_LOAD_PLAN_CODE + ":" + _casMessageHelper.NOT_IN_LOAD_PLAN_MESSAGE);
            inCasInUnit.setReturnStatus(Status.WARNING);
            inCasInUnit.setReturnCode(_casMessageHelper.NOT_IN_LOAD_PLAN_CODE)
            inCasInUnit.setReturnMessage(_casMessageHelper.NOT_IN_LOAD_PLAN_MESSAGE)
          }
        }else{
          CarrierVisit ibCv  = unitFacilityVisit.getUfvActualIbCv();
          if (!ibCv.getCvId().equals(_visitId)){
            log(_casMessageHelper.NOT_IN_DSCH_PLAN_CODE + ":" + _casMessageHelper.NOT_IN_DSCH_PLAN_MESSAGE);
            inCasInUnit.setReturnStatus(Status.WARNING);
            inCasInUnit.setReturnCode(_casMessageHelper.NOT_IN_DSCH_PLAN_CODE)
            inCasInUnit.setReturnMessage(_casMessageHelper.NOT_IN_DSCH_PLAN_MESSAGE)
          }
        }
      }else if (UNIT_CAPTURE_UPDATE_TYPE.equals(_action)){
         //Verify that unit reference in ufv and unit reference of the incoming message match
        verifyUnitReferenceAndTransactionReference(inCasInUnit, unitFacilityVisit)
      }
    } catch (Exception ex) {
      log(_casMessageHelper.BIZ_ERROR_CODE + ":" + ex.getLocalizedMessage());
      inCasInUnit.setReturnStatus(Status.ERROR);
      inCasInUnit.setReturnCode(_casMessageHelper.BIZ_ERROR_CODE)
      inCasInUnit.setReturnMessage(ex.getLocalizedMessage())
    }
  }
  /**
   * Validates the attributes of CasInUnit for the unit position update message
   * @param inCasInUnit the CasInUnit to be validated
   */

  protected void validateUnitForPositionUpdate(CasInUnit inCasInUnit){
    try{
      if (inCasInUnit.getUnitId() != null) {
        validateUnitActive(inCasInUnit)
        UnitFacilityVisit unitFacilityVisit = inCasInUnit.getUnitFacilityVisit()
        //Verify that unit reference in ufv and unit reference of the incoming message match
        verifyUnitReferenceAndTransactionReference(inCasInUnit, unitFacilityVisit)
      }
    } catch (Exception ex) {
      log(_casMessageHelper.BIZ_ERROR_CODE + ":" + ex.getLocalizedMessage());
      inCasInUnit.setReturnStatus(Status.ERROR);
      inCasInUnit.setReturnCode(_casMessageHelper.BIZ_ERROR_CODE)
      inCasInUnit.setReturnMessage(ex.getLocalizedMessage())
    }
  }
  /**
   * Verifies the unit reference and the unit transaction reference of CasInUnit matches the UFV's attributes
   * @param inCasInUnit CAS unit
   * @param inUnitFacilityVisit corresponding unit facility visit
   */
  protected void verifyUnitReferenceAndTransactionReference(CasInUnit inCasInUnit, UnitFacilityVisit inUnitFacilityVisit){
    if (!inCasInUnit.getCasUnitReference().equals(inUnitFacilityVisit.getUfvCasUnitReference())){
      final String unitErrorCode = _casMessageHelper.UNIT_REFERENCE_NOT_SAME_CODE
      final String unitErrorMsg = _casMessageHelper.UNIT_REFERENCE_NOT_SAME_MESSAGE + "[Incoming=" + inCasInUnit.getCasUnitReference() +
              ",Existing=" + inUnitFacilityVisit.getUfvCasUnitReference() + "]"
      log(unitErrorCode + ":" + unitErrorMsg);
      inCasInUnit.setReturnStatus(Status.ERROR);
      inCasInUnit.setReturnCode(unitErrorCode)
      inCasInUnit.setReturnMessage(unitErrorMsg)
      return
    }
    if (inCasInUnit.getCasTransactionReference() != null &&
            inUnitFacilityVisit.getUfvCasTransactionReference() != null &&
            !inCasInUnit.getCasTransactionReference().equals(inUnitFacilityVisit.getUfvCasTransactionReference())){
      final String unitErrorCode = _casMessageHelper.TRANSACTION_REFERENCE_NOT_SAME_CODE
      final String unitErrorMsg = _casMessageHelper.TRANSACTION_REFERENCE_NOT_SAME_MESSAGE + "[Incoming=" + inCasInUnit.getCasTransactionReference() +
              ",Existing=" + inUnitFacilityVisit.getUfvCasTransactionReference() + "]"
      log(unitErrorCode + ":" + unitErrorMsg);
      inCasInUnit.setReturnStatus(Status.ERROR);
      inCasInUnit.setReturnCode(unitErrorCode)
      inCasInUnit.setReturnMessage(unitErrorMsg)
    }
  }
  /**
   * Validate if unit is active and populates the unit id and unitFacilityVisit fields of inCasInUnit.
   *
   * @param inCasInUnit CasInUnit
   * @throws BizViolation trows BizViolation if the validation fails
   */
  protected void validateUnitActive(CasInUnit inCasInUnit) throws BizViolation{
    BizViolation bv = null;
    UnitManager unitManager = (UnitManager) Roastery.getBean(UnitManager.BEAN_ID);
    UnitFacilityVisit unitFacilityVisit = unitManager.findActiveUfvForUnitDigits(inCasInUnit.getUnitId())
    inCasInUnit.setUnitFacilityVisit(unitFacilityVisit)
    Unit unit = unitFacilityVisit.getUfvUnit()
    inCasInUnit.setUnit(unit)
    ServicesManager servicesManager = (ServicesManager) Roastery.getBean(ServicesManager.BEAN_ID);
    IEventType eventType = servicesManager.getEventType(EventEnum.UNIT_DERAMP.getKey());

    if (!unit.isActive()) {
      bv = BizViolation.create(InventoryPropertyKeys.UNITS__NOT_ACTIVE, bv, inCasInUnit.getUnitId(), eventType.getId());
    }

    if (!unitFacilityVisit.equals(unit.getUnitActiveUfvNowActive())) {
      bv = BizViolation.create(InventoryPropertyKeys.UNITS_NOT_ACTIVE_IN_FACILITY, bv,
              inCasInUnit.getUnitId(), unitFacilityVisit.getUfvFacility().getFcyId());
    }

    if (bv != null) {
      throw bv
    }
  }
  /**
   * Handles the unit capture message and update the unit
   */
  protected void handleUnitCaptureAndUpdate(CasInUnit inCasInUnit){
      try {
        if(UNIT_CAPTURE_CREATE_TYPE.equals(_action)){
          createUnit(inCasInUnit)
        }
        if(!UNIT_CAPTURE_IMAGE_TYPE.equals(_action)){
          // Record cas unit capture event
          recordCasUnitEvent(inCasInUnit, EventEnum.CAS_UNIT_CAPTURE)
          //Update the unit
          updateUnitAttributes(inCasInUnit);
        }
        //Handle the capture message
        handleUnitCapture(inCasInUnit);
      } catch (Exception ex) {
        log(_casMessageHelper.BIZ_ERROR_CODE + ":" + ex.getLocalizedMessage());
        inCasInUnit.setReturnStatus(Status.ERROR);
        inCasInUnit.setReturnCode(_casMessageHelper.BIZ_ERROR_CODE)
        inCasInUnit.setReturnMessage(ex.getLocalizedMessage())
      }
  }
  /**
   * Records the Cas Unit event against the unit
   * @param inCasInUnit CAS Unit
   * @param inEventType event type
   */
  protected void recordCasUnitEvent(CasInUnit inCasInUnit, IEventType inEventType) {
    String nodeText = "Action: " + _action + "," + inCasInUnit.getUnitNode().toString();
    if (nodeText.length() > 255) {
      nodeText = nodeText.substring(0, 254)
    }
    recordEvent(inCasInUnit.getUnit(),inEventType, null, nodeText)
  }
  /**
   * Sends the ready to transfer message
   */
  protected void sendReadyToTransferMessage() {
    String payloadXml = null
    Map<String, String> additionalInfoMap = new HashMap<String, String>();
    additionalInfoMap.put(REQUEST_TYPE_PARAM, READY_TO_TRANSFER_MESSAGE)
    additionalInfoMap.put(VISIT_TYPE_PARAM, _visitType)
    additionalInfoMap.put(VISIT_ID_PARAM, _visitId)
    additionalInfoMap.put(CRANE_ID_PARAM, _craneId)
    additionalInfoMap.put(READY_TO_TRANSFER, _casHelper.Y)
    if (UNIT_CAPTURE_IMAGE_TYPE.equals(_action)){
      payloadXml = _casHelper.getXmlPayloadContent(additionalInfoMap, _unitsXml)
    }else if (UNIT_CAPTURE_IDENTIFY_TYPE.equals(_action)){
        //Create xml
      List<Serializable> primaryKeysList = new ArrayList<Serializable>();
      for (CasInUnit casInUnit : _casInUnits){
        if (Status.ERROR !=  casInUnit.getReturnStatus() && casInUnit.getUnitFacilityVisit() != null) {
          primaryKeysList.add(casInUnit.getUnitFacilityVisit().getPrimaryKey())
        }
      }
      if (primaryKeysList.isEmpty()){
        log("No unit is ready for transfer for this capture message")
        return
      }
      Serializable[] primaryKeys = primaryKeysList.toArray(new Serializable[primaryKeysList.size()])
      DomainQuery dq = _casHelper.createUnitScalarQuery(primaryKeys);
      String unitsXmlFromN4 = _casHelper.createUnitXml(dq, true, null)
      payloadXml = _casHelper.getXmlPayloadContent(additionalInfoMap, unitsXmlFromN4)
      //Record ready to transfer event against the units
      for (CasInUnit casInUnit : _casInUnits){
        if (Status.ERROR != casInUnit.getReturnStatus()) {
          recordCasUnitEvent(casInUnit, EventEnum.CAS_UNIT_READY_TO_TRANSFER)
        }
      }
    }else{
      log(Level.ERROR, "Send ready to transfer sent for message of a wrong action : " + _action)
    }
    if (payloadXml != null){
      log("Message from N4 to CAS service for 'readyToTransfer' message for action '" + _action +"': " + payloadXml);
      String webServiceResponse = ArgoUtils.invokeExternalBasicService(_casHelper.CAS_OUTBOUND, payloadXml);
      log("Response from CAS service for 'readyToTransfer' message sent by N4 for action '" + _action +"': " + webServiceResponse);
    }
  }
  /**
   * Handles the unit position update. It records an event and pusts the message on the queue
   * @param inCasInUnit
   */
  protected void handleUnitPositionUpdate(CasInUnit inCasInUnit){
     if (inCasInUnit.getUnit() != null){
       //Record a unit position update event
       recordCasUnitEvent(inCasInUnit, EventEnum.CAS_UNIT_POSITION_UPDATE)
     }
     // handle the unit position update
    //Get the position
    String locType = null;
    String location = null;
    String slot = null;
    Node unitNode = inCasInUnit.getUnitNode()
    Node currentPositionNode = unitNode."current-position"[0]
    if (currentPositionNode != null){
      locType = currentPositionNode.attribute("loc-type")
      if (StringUtils.isBlank(locType)){
        log(_casMessageHelper.MISSING_LOC_TYPE_CODE + ":" + _casMessageHelper.MISSING_LOC_TYPE_MESSAGE);
        inCasInUnit.setReturnStatus(Status.ERROR);
        inCasInUnit.setReturnCode(_casMessageHelper.MISSING_LOC_TYPE_CODE)
        inCasInUnit.setReturnMessage(_casMessageHelper.MISSING_LOC_TYPE_MESSAGE)
        return
      }
      location = currentPositionNode.attribute("location")
      if (StringUtils.isBlank(location)){
        log(_casMessageHelper.MISSING_LOCATION_CODE + ":" + _casMessageHelper.MISSING_LOCATION_MESSAGE);
        inCasInUnit.setReturnStatus(Status.ERROR);
        inCasInUnit.setReturnCode(_casMessageHelper.MISSING_LOCATION_CODE)
        inCasInUnit.setReturnMessage(_casMessageHelper.MISSING_LOCATION_MESSAGE)
        return
      }
      slot = currentPositionNode.attribute("slot")
      if (StringUtils.isBlank(slot)){
        log(_casMessageHelper.MISSING_SLOT_CODE + ":" + _casMessageHelper.MISSING_SLOT_MESSAGE);
        inCasInUnit.setReturnStatus(Status.ERROR);
        inCasInUnit.setReturnCode(_casMessageHelper.MISSING_SLOT_CODE)
        inCasInUnit.setReturnMessage(_casMessageHelper.MISSING_SLOT_MESSAGE)
        return
      }
    }else{
      log(_casMessageHelper.MISSING_CURRENT_POSITION_NODE_CODE + ":" + _casMessageHelper.MISSING_CURRENT_POSITION_NODE_MESSAGE);
      inCasInUnit.setReturnStatus(Status.ERROR);
      inCasInUnit.setReturnCode(_casMessageHelper.MISSING_CURRENT_POSITION_NODE_CODE)
      inCasInUnit.setReturnMessage(_casMessageHelper.MISSING_CURRENT_POSITION_NODE_MESSAGE)
      return
    }

    //Put the information on the ECI service request queue
    final Map<Object, Object> serviceParams = new HashMap<Object, Object>();
    serviceParams.put(EciBizMetafield.CAS_SERVICE_TYPE, EciEsbConstants.ECISERVICE_TYPE_QCAS_CONTAINER_POSITION_UPDATE);
    serviceParams.put(EciBizMetafield.ECI_UNIT_REFERENCE, inCasInUnit.getCasUnitReference());
    serviceParams.put(EciBizMetafield.ECI_UNIT_ID, inCasInUnit.getUnitId());
    serviceParams.put(ArgoField.CHE_SHORT_NAME, _craneId);
    serviceParams.put(EciBizMetafield.ECI_USER_CONTEXT, getUserContext());
    // todo add constants for position attributes below at the appropriate place
    serviceParams.put("locType", locType);
    serviceParams.put("location", location);
    serviceParams.put("slot", slot);

    /* todo uncomment later when the implementation of handling functionality is in place
    EciEsbHelper.enqueueEciServiceRequest(EciEsbConstants.ECISERVICE_QUEUENAME_QCAS, EciEsbConstants.ECISERVICE_ENDPOINT_REF_QCASFLOW,
            serviceParams);*/
  }
  /**
   * Handles the unit capture message
   *
   */
  protected void handleUnitCapture(CasInUnit inCasInUnit){

    if (UNIT_CAPTURE_IDENTIFY_TYPE.equals(_action) || UNIT_CAPTURE_CREATE_TYPE.equals(_action) || UNIT_CAPTURE_IMAGE_TYPE.equals(_action)) {
      //Put the information on the ECI service request queue
      final Map<Object, Object> serviceParams = new HashMap<Object, Object>();
      if (UNIT_CAPTURE_IMAGE_TYPE.equals(_action)) {
        serviceParams.put(EciBizMetafield.CAS_SERVICE_TYPE, EciEsbConstants.ECISERVICE_TYPE_QCAS_CONTAINER_IMAGE_CAPTURED);
      } else { //Identify or Create
        serviceParams.put(EciBizMetafield.CAS_SERVICE_TYPE, EciEsbConstants.ECISERVICE_TYPE_QCAS_CONTAINER_IDENTIFIED);
      }
      Double weight = (Double) getAttributeValue("gross-weight", inCasInUnit.getUnitNode().@"gross-weight", Double.class);
      if (weight != null) {
        serviceParams.put(EciBizMetafield.ECI_MEASURED_WEIGHT_KG, weight);
      }
      enhanceServiceParamsMapWithUnitAttributes(serviceParams, inCasInUnit)

      EciEsbHelper.enqueueEciServiceRequest(EciEsbConstants.ECISERVICE_QUEUENAME_QCAS, EciEsbConstants.ECISERVICE_ENDPOINT_REF_QCASFLOW,
              inCasInUnit.getCasUnitReference(), serviceParams);
      if (UNIT_CAPTURE_IMAGE_TYPE.equals(_action)){
        handleImageCaptureMessage(inCasInUnit)
      }
    }
  }

  private void enhanceServiceParamsMapWithUnitAttributes(HashMap<Object, Object> serviceParams, CasInUnit inCasInUnit) {
    serviceParams.put(EciBizMetafield.ECI_TRANSACTION_ID, inCasInUnit.getCasTransactionReference());
    serviceParams.put(EciBizMetafield.ECI_UNIT_REFERENCE, inCasInUnit.getCasUnitReference());
    serviceParams.put(EciBizMetafield.ECI_UNIT_ID, inCasInUnit.getUnitId());
    serviceParams.put(ArgoField.CHE_SHORT_NAME, _craneId);
    serviceParams.put(EciBizMetafield.ECI_USER_CONTEXT, getUserContext());
    serviceParams.put(EciBizMetafield.ECI_UNIT_CAPTURE_ACTION, _action);
  }
  /**
   * Handles the unit capture message with action 'Image'
   *
   */

  protected void handleImageCaptureMessage(CasInUnit inCasInUnit){
    inCasInUnit.setReturnStatus(Status.SUCCESS)
    inCasInUnit.setReturnCode(_casMessageHelper.UNIT_CAPTURE_HANDLED_SUCCESSFULLY_CODE)
    inCasInUnit.setReturnMessage(_casMessageHelper.UNIT_CAPTURE_HANDLED_SUCCESSFULLY_MESSAGE + "[Action=" + _action + "]")
  }
  /**
   * Creates a unit corresponding to the
   * @param inCasInUnit  CAS unit
   */
  protected void createUnit(CasInUnit inCasInUnit)throws BizViolation{
    UnitManager unitManager = (UnitManager) Roastery.getBean(UnitManager.BEAN_ID);

    Container container = null;
    String isoCode = inCasInUnit.getUnitNode().attributes().get("iso-code")
    if (StringUtils.isBlank(isoCode)){
      log(_casMessageHelper.MISSING_ISO_CODE_FOR_CREATE_WARNING_CODE + ":" + _casMessageHelper.MISSING_ISO_CODE_FOR_CREATE_WARNING_MESSAGE);
      inCasInUnit.setReturnStatus(Status.WARNING);
      inCasInUnit.setReturnCode(_casMessageHelper.MISSING_ISO_CODE_FOR_CREATE_WARNING_CODE)
      inCasInUnit.setReturnMessage(_casMessageHelper.MISSING_ISO_CODE_FOR_CREATE_WARNING_MESSAGE)
      container = Container.findOrCreateContainer(inCasInUnit.getUnitId(), _dataSourceEnum)
    }else{
      container = Container.findOrCreateContainer(inCasInUnit.getUnitId(), isoCode, _dataSourceEnum)
    }
    LineOperator unknownOpr = LineOperator.findOrCreateLineOperator(ScopedBizUnit.UNKNOWN_BIZ_UNIT);
    UnitFacilityVisit ufv = unitManager.findOrCreateStowplanUnit(container, _carrierVisit, unknownOpr,
            ContextHelper.getThreadFacility())
    RectifyParms parms = new RectifyParms()
    parms.setUfvTransitState(UfvTransitStateEnum.S20_INBOUND)
    parms.setUnitVisitState(UnitVisitStateEnum.ACTIVE)
    parms.setObCv(CarrierVisit.getGenericCarrierVisit(ContextHelper.getThreadComplex()));
    ufv.rectify(parms);
    final Unit unit = ufv.getUfvUnit()
    unit.updateCategory(UnitCategoryEnum.IMPORT)
    unit.updateFreightKind(FreightKindEnum.MTY)
    //Set the newly created unit and ufv on CasInUnit
    inCasInUnit.setUnit(unit)
    inCasInUnit.setUnitFacilityVisit(ufv)
  }

  /**
   * Update the unit with attributes from unit xml
   */
  protected void updateUnitAttributes(CasInUnit inCasInUnit) throws BizViolation{
    Node unitNode = inCasInUnit.getUnitNode()
    Unit unit = inCasInUnit.getUnit()
    UnitFacilityVisit ufv = inCasInUnit.getUnitFacilityVisit()
    Equipment primaryEquipment = unit.getPrimaryEq();
    UnitEquipment unitEquipment = unit.getUnitPrimaryUe()

    //Validate bundled Equipment
    Node bundledEquipmentNode = unitNode."bundled-equipment"[0]
    if (bundledEquipmentNode != null){
      NodeList equipmentNodeList = bundledEquipmentNode."equipment"
      equipmentNodeList.each{Node equipmentNode ->
        Equipment equipment = Equipment.findEquipment(equipmentNode.@'id')
        if (equipment == null){
          final String errorCode = _casMessageHelper.BUNDLED_EQUIPMENT_NOT_FOUND_CODE
          final String errorMsg = _casMessageHelper.BUNDLED_EQUIPMENT_NOT_FOUND_MESSAGE + "[id=" + equipmentNode.@'id' + "]"
          log(errorCode + ":" + errorMsg);
          inCasInUnit.setReturnStatus(Status.ERROR);
          inCasInUnit.setReturnCode(errorCode)
          inCasInUnit.setReturnMessage(errorMsg)
          return
        }
      }
    }
    //Gross Weight
    if(unitNode.attributes().containsKey("gross-weight")){
      unit.updateGoodsAndCtrWtKg((Double)getAttributeValue("gross-weight", unitNode.@"gross-weight", Double.class));
      //unit.setFieldValue(UnitField.UNIT_GOODS_AND_CTR_WT_KG, (Double)getAttributeValue("gross-weight", unitNode.@"gross-weight", Double.class))
    }
    //Yard Measured weight
    if(unitNode.attributes().containsKey("yard-measured-weight")){
      unit.updateGoodsAndCtrWtKgAdvised((Double)getAttributeValue("yard-measured-weight", unitNode.@"yard-measured-weight", Double.class));
    }
    //ISO code
    if (!UNIT_CAPTURE_CREATE_TYPE.equals(_action)){ //iso-code is handled as part of create
      if(unitNode.attributes().containsKey("iso-code")){
        primaryEquipment.upgradeEqType(unitNode.@"iso-code", _dataSourceEnum)
      }
    }
    //Height mm
    if(unitNode.attributes().containsKey("height-mm")){
      primaryEquipment.upgradeEqHeight((Long)getAttributeValue("height-mm", unitNode.@"height-mm", Long.class), _dataSourceEnum)
    }
    //Length mm
    if(unitNode.attributes().containsKey("length-mm")){
      primaryEquipment.setFieldValue(ArgoRefField.EQ_LENGTH_MM, (Long)getAttributeValue("length-mm", unitNode.@"length-mm", Long.class))
    }
    //Width mm
    if(unitNode.attributes().containsKey("width-mm")){
      primaryEquipment.setFieldValue(ArgoRefField.EQ_WIDTH_MM, (Long)getAttributeValue("width-mm", unitNode.@"width-mm", Long.class))
    }
    //Tank rail type
    if(unitNode.attributes().containsKey("tank-rail-type")){
      String tankRails = unitNode.@"tank-rail-type"
      TankRailTypeEnum tankRailTypeEnum = TankRailTypeEnum.getEnum(tankRails)
      if (tankRailTypeEnum == null){
         throw invalidValueViolation("tank-rail-type", tankRails)
      }
      primaryEquipment.setFieldValue(ArgoRefField.EQ_TANK_RAILS, tankRailTypeEnum)
    }
    //Door direction
    if(unitNode.attributes().containsKey("door-direction")){
      String doorDirection = unitNode.@"door-direction"
      DoorDirectionEnum doorDirectionEnum = DoorDirectionEnum.getEnum(doorDirection)
      if (doorDirectionEnum == null){
        throw invalidValueViolation("door-direction", doorDirection)
      }
      ufv.updateCurrentDoorDir(doorDirectionEnum)
    }
    //Update the is-sealed, is-bundle and is-placarded attributes; update it before seals, bundle and observer placards
    //Is Sealed
    if (unitNode.attributes().containsKey("is-sealed")) {
      unit.setFieldValue(InventoryField.UNIT_IS_CTR_SEALED, (Boolean) getAttributeValue("is-sealed", unitNode.@"is-sealed", Boolean.class))
    }
    //Is Bundled
    if (unitNode.attributes().containsKey("is-bundle")) {
      unit.setFieldValue(InventoryField.UNIT_IS_BUNDLE, (Boolean) getAttributeValue("is-bundle", unitNode.@"is-bundle", Boolean.class))
    }
    //Is Placarded
    if (unitNode.attributes().containsKey("is-placarded") && unitEquipment != null) {
      boolean isPlacarded = (Boolean) getAttributeValue("is-placarded", unitNode.@"is-placarded", Boolean.class)
      unitEquipment.setFieldValue(InventoryField.UE_PLACARDED, isPlacarded ? PlacardedEnum.YES: PlacardedEnum.NO)
    }
    //OOG
    Node oogNode = unitNode.'oog'[0]
    if (oogNode != null){
      unit.updateOog(extractOog("back-cm", oogNode.@"back-cm"), extractOog("front-cm", oogNode.@"front-cm"), extractOog("left-cm", oogNode.@"left-cm"),
              extractOog("right-cm", oogNode.@"right-cm"), extractOog("top-cm", oogNode.@"top-cm"))
    }
    //Seals
    Node sealsNode = unitNode.'seals'[0]
    if (sealsNode != null) {
      unit.updateSeals(sealsNode.@"seal-1", sealsNode.@"seal-2", sealsNode.@"seal-3", sealsNode.@"seal-4")
    }

    //Damages
    Node damagesNode = unitNode.'damages'[0]
    if (damagesNode != null){
      if (unitEquipment != null) {
        // Clear all existing damages - they will be completely replaced by this update
        unitEquipment.attachDamages(null);
        EquipClassEnum equipClass = unitEquipment.getUeEquipment().getEqClass();
        NodeList damageNodes = damagesNode.'damage'
        damageNodes.each{Node damageNode ->
          String component = damageNode.@'component'
          String severity = damageNode.@'severity'
          String type = damageNode.@'type'
          Date reportedDate = (Date) getAttributeValue("reported-date", damageNode.@"reported-date", Date.class)
          Date repairedDate = (Date) getAttributeValue("repaired-date", damageNode.@"repaired-date", Date.class)

          EquipDamageType eqdmgtyp = EquipDamageType.findOrCreateEquipDamageType(type, type, equipClass)
          EqComponent eqcmp = EqComponent.findOrCreateEqComponent(component, component, equipClass)
          EqDamageSeverityEnum eqdmgSeverity = EqDamageSeverityEnum.getEnum(severity)

          unitEquipment.addDamageItem(eqdmgtyp, eqcmp, eqdmgSeverity, reportedDate, repairedDate)
        }
      }
    }
    //Flags
    Node flagsNode = unitNode.'flags'[0]
    if (flagsNode != null){
      ServicesManager servicesManager = (ServicesManager) Roastery.getBean(ServicesManager.BEAN_ID);
      NodeList holdsNodeList = flagsNode.'hold'
      holdsNodeList.each{Node holdNode ->
        String holdId = holdNode.@'id'
        servicesManager.applyHold(holdId, unit, null, null, "cas");
      }
      NodeList permissionsNode = flagsNode.'permission'
      permissionsNode.each{Node permissionNode ->
        String permissionId = permissionNode.@'id'
        servicesManager.applyPermission(permissionId, unit, null, null, "cas");
      }
    }

    //Observed placards
    Node observedPlacardsNode = unitNode."observed-placards"[0]
    if (observedPlacardsNode != null){
      NodeList obsPlacardNodeList = observedPlacardsNode."observed-placard"
      obsPlacardNodeList.each{Node obsPlacardNode ->
        String placardText = obsPlacardNode.@'placard'
        ObservedPlacard observedPlacard = ObservedPlacard.createObservedPlacard(unit, placardText)
        observedPlacard.setFieldValue(InventoryField.OBSPLACARD_REMARK, obsPlacardNode.@'remarks')
      }
    }
    //Update Bundled Equipment
    if (bundledEquipmentNode != null){
      NodeList equipmentNodeList = bundledEquipmentNode."equipment"
      equipmentNodeList.each{Node equipmentNode ->
        Equipment equipment = Equipment.findEquipment(equipmentNode.@'id')
        if (equipment != null){
          //Update the attributes
          //ISO code
          if(equipmentNode.attributes().containsKey("iso-code")){
            equipment.upgradeEqType(equipmentNode.@"iso-code", _dataSourceEnum)
          }
          //Height mm
          if(equipmentNode.attributes().containsKey("height-mm")){
            equipment.upgradeEqHeight((Long)getAttributeValue("height-mm", equipmentNode.@"height-mm", Long.class), _dataSourceEnum)
          }
          //Length mm
          if(equipmentNode.attributes().containsKey("length-mm")){
            equipment.setFieldValue(ArgoRefField.EQ_LENGTH_MM, (Long)getAttributeValue("length-mm", equipmentNode.@"length-mm", Long.class))
          }
          //Width mm
          if(equipmentNode.attributes().containsKey("width-mm")){
            equipment.setFieldValue(ArgoRefField.EQ_WIDTH_MM, (Long)getAttributeValue("width-mm", equipmentNode.@"width-mm", Long.class))
          }
          //Attach it to the unit
          unit.attachPayload(equipment)
        }
      }
    }
    //Set the unit reference and transaction reference
    if (UNIT_CAPTURE_IDENTIFY_TYPE.equals(_action) || UNIT_CAPTURE_CREATE_TYPE.equals(_action)){
        ufv.setFieldValue(InventoryField.UFV_CAS_UNIT_REFERENCE, inCasInUnit.getCasUnitReference())
        if (!StringUtils.isBlank(inCasInUnit.getCasTransactionReference())){
          ufv.setFieldValue(InventoryField.UFV_CAS_TRANSACTION_REFERENCE, inCasInUnit.getCasTransactionReference())
        }
    }

  }
  private Long extractOog(String inField, String inValue){
    return getAttributeValue(inField, inValue, Long.class) as Long
  }
  /**
   * A hook for subclasses to add additional parameters/attributes to the attribute map of CasInUnit. This method is called immediately after the
   * CasInUnit is constructed
   * @param inCasInUnit newly created CasInUnit
   */
  protected void initializeCasUnit(CasInUnit inCasInUnit){

  }
  protected String createResponseXml(Map<String, String> inAdditionalInfo){
    def writer = new StringWriter();
    def xml = new MarkupBuilder(writer);
    xml.payload() {
      parameters(){
        if (_parameterMap != null && !_parameterMap.isEmpty()) {
          _parameterMap.keySet().each {
            parameter(id:it, value:_parameterMap.get(it));
          }
        }
      }
      if (inAdditionalInfo != null && !inAdditionalInfo.isEmpty()) {
        "additional-info"(){
          inAdditionalInfo.keySet().each {
            field(id:it, value:inAdditionalInfo.get(it));
          }
        }
      }
      "units-response"() {
        for (CasInUnit casInUnit : _casInUnits){
          "unit-response"("cas-unit-reference": casInUnit.getCasUnitReference(), "cas-transaction-reference": casInUnit.getCasTransactionReference(),
                  id: casInUnit.getUnitId(), status: casInUnit.getStatusAsString()){
            message(code:casInUnit.getReturnCode(), text: casInUnit.getReturnMessage())
          }
        }
      }
    }
    String out = writer.toString();
    return out;
  }

  /**
   * Translates an attribute value to correct class.
   *
   * @param inName name of the attribute
   * @param inName value of the attribute
   * @param inValueClass    The java Class of the value within the Entity to which it belongs
   * @return translated value of the attribute
   * @throws BizViolation if attribute is present and can not be parsed
   */
  protected Object getAttributeValue(String inName, String inValue, Class inValueClass) throws BizViolation {
    IPropertyResolver resolver = ArgoUtils.getPropertyResolver(inValueClass);
    return resolver.resolve(inName, inValue);
  }
  /**
   * Returns exception for invalid value
   * @param inFieldId id of the field
   * @param inValue field value
   * @return biz violation
   */
  public BizViolation invalidValueViolation(String inFieldId, Object inValue) {
    return BizViolation.create(ArgoPropertyKeys.VALIDATION_INVALID_VALUE_FOR_FIELD, null, inFieldId, inValue);
  }
  private void initCasMessageHelper() {
    //Get the CasMessageHelper instance
    if (_casMessageHelper == null) {
      _casMessageHelper = getLibrary(CAS_MESSAGE_HELPER);
    }
  }

  protected void registerAndLogError() {
    registerError(_errorCode + ":" + _errorMessage);
    log(_errorCode + ":" + _errorMessage);
  }

  /**
   * Creates xml to be returned in case of errors
   * @param inErrorCode error code
   * @param inErrorMessage error message
   * @return
   */
  private String getXmlErrorContent(){
    def writer = new StringWriter();
    def xml = new MarkupBuilder(writer);
    xml.payload() {
      parameters(){
        if (_parameterMap != null && !_parameterMap.isEmpty()) {
          _parameterMap.keySet().each {
            parameter(id:it, value:_parameterMap.get(it));
          }
        }
      }
      error(code:_errorCode, message:_errorMessage) {
      }
    }
    String out = writer.toString();
    log("\nRequest: " + getParametersAsString() + "\nResponse : " + out)
    return out;
  }

  /**
   * This class encapsulates the unit information coming from the CAS, it also holds any error information added during validation and unit facility
   * visit information id validation is successful
   */
  protected class CasInUnit{

    CasInUnit(Node inUnitNode) {
      _unitNode = inUnitNode
      _casUnitReference = _unitNode."@cas-unit-reference"
      _casTransactionReference = _unitNode."@cas-transaction-reference"
      _unitId = _unitNode."@id"
      _returnStatus = Status.SUCCESS
      _returnCode = _casMessageHelper.UNIT_SUCCESSFULLY_PROCESSED_CODE
      _returnMessage = _casMessageHelper.UNIT_SUCCESSFULLY_PROCESSED_MESSAGE
    }

    private String _casUnitReference
    private String _casTransactionReference
    private String _unitId
    private UnitFacilityVisit _unitFacilityVisit
    private Unit _unit
    //Message which will be returned in the response for this unit
    private String _returnMessage
    private String _returnCode
    private Status _returnStatus
    private Node _unitNode
    //A generic fields for any additional attributes which may be used by subclasses
    protected Map<String, Object> _attributeMap = new HashMap<String, Object>();
    Unit getUnit() {
      return _unit
    }

    void setUnit(Unit inUnit) {
      _unit = inUnit
    }

    Node getUnitNode() {
      return _unitNode
    }

    String getCasUnitReference() {
      return _casUnitReference
    }

    String getCasTransactionReference() {
      return _casTransactionReference
    }

    String getUnitId() {
      return _unitId
    }
    UnitFacilityVisit getUnitFacilityVisit() {
      return _unitFacilityVisit
    }

    void setUnitFacilityVisit(UnitFacilityVisit inUnitFacilityVisit) {
      _unitFacilityVisit = inUnitFacilityVisit
    }

    boolean hasError() {
      return Status.ERROR == _returnStatus
    }

    boolean hasWarning() {
      return Status.WARNING == _returnStatus
    }

    boolean isSuccess() {
      return Status.SUCCESS == _returnStatus
    }
    public String getStatusAsString(){
      switch (_returnStatus){
        case Status.SUCCESS: return "SUCCESS"
        case Status.ERROR: return "ERROR"
        case Status.WARNING: return "WARNING"
      }
      return "";
    }
    String getReturnMessage() {
      return _returnMessage
    }

    void setReturnMessage(String inReturnMessage) {
      _returnMessage = inReturnMessage
    }
    Status getReturnStatus() {
      return _returnStatus
    }

    void setReturnStatus(Status inStatus) {
      _returnStatus = inStatus
    }

    String getReturnCode() {
      return _returnCode
    }

    void setReturnCode(String inReturnCode) {
      _returnCode = inReturnCode
    }
  }
}
