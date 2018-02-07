/*
 * Copyright (c) 2013 Navis LLC. All Rights Reserved.
 *
 */

package extension.system.cas

import com.navis.argo.ArgoPropertyKeys
import com.navis.argo.ArgoRefField
import com.navis.argo.ContextHelper
import com.navis.argo.business.api.ArgoUtils
import com.navis.argo.business.api.IEventType
import com.navis.argo.business.api.ServicesManager
import com.navis.argo.business.atoms.DataSourceEnum
import com.navis.argo.business.atoms.EquipClassEnum
import com.navis.argo.business.atoms.EquipNominalLengthEnum
import com.navis.argo.business.atoms.EventEnum
import com.navis.argo.business.atoms.RestowTypeEnum
import com.navis.argo.business.atoms.TankRailTypeEnum
import com.navis.argo.business.atoms.UnitCategoryEnum
import com.navis.argo.business.atoms.WiMoveKindEnum
import com.navis.argo.business.model.CarrierVisit
import com.navis.argo.business.model.LocPosition
import com.navis.argo.business.model.VisitDetails
import com.navis.argo.business.reference.Container
import com.navis.argo.business.reference.EqComponent
import com.navis.argo.business.reference.EquipDamageType
import com.navis.argo.business.reference.Equipment
import com.navis.argo.business.snx.IPropertyResolver
import com.navis.external.argo.AbstractGroovyWSCodeExtension
import com.navis.framework.business.Roastery
import com.navis.framework.util.BizViolation
import com.navis.inventory.InventoryField
import com.navis.inventory.InventoryPropertyKeys
import com.navis.inventory.business.api.HcTbdUnitManager
import com.navis.inventory.business.api.RectifyParms
import com.navis.inventory.business.api.UnitManager
import com.navis.inventory.business.atoms.EqDamageSeverityEnum
import com.navis.inventory.business.atoms.UfvTransitStateEnum
import com.navis.inventory.business.atoms.UnitVisitStateEnum
import com.navis.inventory.business.imdg.ObservedPlacard
import com.navis.inventory.business.moves.WorkInstruction
import com.navis.inventory.business.units.Unit
import com.navis.inventory.business.units.UnitEquipment
import com.navis.inventory.business.units.UnitFacilityVisit
import com.navis.xpscache.constants.BentoMessageAttribute
import com.navis.xpscache.constants.BentoMessageAttributeGetNextLoadPosition
import groovy.xml.MarkupBuilder
import org.apache.commons.lang.StringUtils

/**
 * This groovy is used to receive an inbound message from a CAS system.
 * This specific version is for N4 version 2.5 and does not support the unitPositionUpdateMessage request type, only the unitCaptureMessage
 *
 * @author <a href="mailto:peter.kaplan@navis.com">Peter Kaplan</a>, 12/12/12
 */

class Default25InboundCasMessageHandler extends AbstractGroovyWSCodeExtension {

  protected final String REQUEST_TYPE_PARAM = "requestType"
  protected final String VISIT_ID_PARAM = "visitId"
  protected final String VISIT_TYPE_PARAM = "visitType"
  protected final String UNITS_XML_PARAM = "unitXml"
  protected final String VISIT_TYPE_VESSEL = "VESSEL"
  protected final String VISIT_TYPE_TRAIN = "TRAIN"
  protected final String UNIT_CAPTURE_MESSAGE = "unitCaptureMessage";
  protected final String UNIT_CAPTURE_IMAGE_TYPE = "Image";
  protected final String UNIT_CAPTURE_IDENTIFY_TYPE = "Identify";
  protected final String UNIT_CAPTURE_CREATE_TYPE = "Create";
  protected final String UNIT_CAPTURE_UPDATE_TYPE = "Update";
  protected final String UNIT_ID = "unitId"
  public final String CRANE_ID_PARAM = "craneId"
  protected final String ACTION_PARAM = "action"
  //2.5 specific fields
  protected final String IS_TBD_LOAD = "isTbdLoad"
  protected final String IS_LOAD = "isLoad"
  protected final String IS_DISC = "isDisc"
  protected final String POSITION_PARAM = "position"
  protected final String TIER = "tier"


  // parameter fields
  protected String _requestType
  protected String _visitId
  protected String _visitType
  protected String _craneId
  protected String _unitsXml
  protected CasInUnit25[] _casInUnits
  protected int _unitCount
  protected String _action
  protected enum Status {
    OK, WARNING, ERROR
  }
  protected String _tier

  //The name of the cas helper groovy
  public final String CAS_HELPER = "CasHelper"
  def _casHelper = null
  //CasMessageHelper library name
  public final String CAS_MESSAGE_HELPER = "CasMessageHelper"
  //CasHelper library class
  def _casMessageHelper = null;
  //error code and message which are populated for non-unit specific errors, unit specific arrors are handled at unit level
  private String _errorCode = null;
  private String _errorMessage = null;


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

    _action = _parameterMap.get(ACTION_PARAM)
    loadParameters(unitsNode);
    //Validate the required parameters for the message
    if (!validateParameters()) {
      return getXmlErrorContent();
    }
    // Identify the message type and call appropriate handler
    if (UNIT_CAPTURE_MESSAGE.equals(_requestType)) {
      // Validate Capture type
      boolean isValidAction = UNIT_CAPTURE_IMAGE_TYPE.equals(_action) || UNIT_CAPTURE_IDENTIFY_TYPE.equals(_action) ||
              UNIT_CAPTURE_CREATE_TYPE.equals(_action) || UNIT_CAPTURE_UPDATE_TYPE.equals(_action);
      if (!isValidAction) {
        _errorCode = _casMessageHelper.INVALID_CAPTURE_TYPE_CODE
        _errorMessage = _casMessageHelper.INVALID_CAPTURE_TYPE_MESSAGE
        registerAndLogError()
        return getXmlErrorContent();
      }
      processUnitCapture();
    } else {
      _errorCode = _casMessageHelper.INVALID_25_MESSAGE_REQUEST_TYPE_CODE
      _errorMessage = _casMessageHelper.INVALID_25_MESSAGE_REQUEST_TYPE_MESSAGE
      registerAndLogError()
      return getXmlErrorContent();
    }
    // Create the response message
    final String responseXml = createResponseXml(additionalInfoMap)
    log("\nRequest: " + getParametersAsString() + "\nResponse : " + responseXml)
    return responseXml;
  }

  /**
   * Loads the parameters to fields, so that they are available to all methods
   */
  protected void loadParameters(Node inUnitsNode) {
    _casInUnits = new CasInUnit25[_unitCount]
    _requestType = _parameterMap.get(REQUEST_TYPE_PARAM);
    _visitId = _parameterMap.get(VISIT_ID_PARAM)
    _visitType = _parameterMap.get(VISIT_TYPE_PARAM)
    _craneId = _parameterMap.get(CRANE_ID_PARAM)
    _tier = _parameterMap.get(TIER)
    try {
      if (StringUtils.isBlank(_tier) || Integer.parseInt(_tier) < 0) {
        _tier = 1
      }
    } catch (NumberFormatException e) {
      _tier = 1
    }
    def units = inUnitsNode.'unit'
    int i = 0
    units.each { Node unitNode ->
      _casInUnits[i] = new CasInUnit25(unitNode)
      initializeCasUnit(_casInUnits[i]);
      i++;
    }
  }

  /**
   * A hook for subclasses to add additional parameters/attributes to the attribute map of CasInUnit25. This method is called immediately after the
   * CasInUnit25 is constructed
   * @param inCasInUnit newly created CasInUnit25
   */
  protected void initializeCasUnit(CasInUnit25 inCasInUnit) {
    Node unitNode = inCasInUnit.getUnitNode()
    if (unitNode != null) {
      String positionStr = unitNode."@position"
      inCasInUnit.setAttribute(POSITION_PARAM, positionStr)
    }
  }

  /**
   * Validate the required parameters for the message, for the unit's required parameters message is added to CasInUnit25
   * @return true if all the required parameters for message as a whole are valid, for unit attributes it sets hasError of
   * unit to true and adds a return message for that unit, so that the other valid units are processed.
   */
  protected boolean validateParameters() {
    boolean isValid = true;

    if (StringUtils.isBlank(_requestType)) {
      _errorCode = _casMessageHelper.MISSING_REQUEST_TYPE_CODE
      _errorMessage = _casMessageHelper.MISSING_REQUEST_TYPE_MESSAGE
      registerAndLogError()
      return false;
    }
    if (StringUtils.isBlank(_visitId)) {
      _errorCode = _casMessageHelper.MISSING_VISIT_ID_CODE
      _errorMessage = _casMessageHelper.MISSING_VISIT_ID_MESSAGE
      registerAndLogError()
      return false;
    }
    if (StringUtils.isBlank(_visitType)) {
      _errorCode = _casMessageHelper.MISSING_VISIT_TYPE_CODE
      _errorMessage = _casMessageHelper.MISSING_VISIT_TYPE_MESSAGE
      registerAndLogError()
      return false;
    } else if (!(_visitType.equals(VISIT_TYPE_VESSEL) || _visitType.equals(VISIT_TYPE_TRAIN))) {
      _errorCode = _casMessageHelper.INVALID_VISIT_TYPE_CODE
      _errorMessage = _casMessageHelper.INVALID_VISIT_TYPE_MESSAGE
      registerAndLogError()
      return false;
    }
    //Validate the carrier visit
    CarrierVisit carrierVisit = null
    if (_visitType.equals(VISIT_TYPE_VESSEL)){
      carrierVisit = CarrierVisit.findVesselVisit(ContextHelper.getThreadFacility(), _visitId)
    }else if (_visitType.equals(VISIT_TYPE_TRAIN)){
      carrierVisit = CarrierVisit.findTrainVisit(ContextHelper.getThreadComplex(), ContextHelper.getThreadFacility(), _visitId)
    }
    if (carrierVisit == null ){
      _errorCode = _casMessageHelper.NO_CARRIER_VISIT_FOUND_CODE
      _errorMessage = _casMessageHelper.NO_CARRIER_VISIT_FOUND_MESSAGE + "[visitId=" +  _visitId + "]"
      registerAndLogError()
      return false;
    }
    if (StringUtils.isBlank(_craneId)) {
      _errorCode = _casMessageHelper.MISSING_CRANE_ID_CODE
      _errorMessage = _casMessageHelper.MISSING_CRANE_ID_MESSAGE
      registerAndLogError()
      return false;
    } else {
      try {
        _casHelper.validateCraneId(_craneId);
      } catch (Exception ex) {
        _errorCode = _casMessageHelper.INVALID_CRANE_ID_CODE
        _errorMessage = _casMessageHelper.INVALID_CRANE_ID_MESSAGE + "[craneId=" +  _craneId + "]"
        registerAndLogError()
        return false;
      }
    }

    for (CasInUnit25 casInUnit : _casInUnits) {
      if (StringUtils.isBlank(casInUnit.getCasUnitReference())) {
        log(_casMessageHelper.MISSING_CAS_UNIT_REFERENCE_CODE + ":" + _casMessageHelper.MISSING_CAS_UNIT_REFERENCE_MESSAGE);
        casInUnit.setReturnStatus(Status.ERROR);
        casInUnit.setReturnCode(_casMessageHelper.MISSING_CAS_UNIT_REFERENCE_CODE)
        casInUnit.setReturnMessage(_casMessageHelper.MISSING_CAS_UNIT_REFERENCE_MESSAGE)
      }
    }
    return isValid;
  }

  /**
   * Processes the unit capture message after preliminary validations have been done
   */
  protected void processUnitCapture() {
    boolean tbdLoad = false
    boolean load = false
    boolean discharge = false
    boolean hasError = false;
    // Validate each unit and determine the type of operation to perform
    for (CasInUnit25 casInUnit : _casInUnits) {
      if (!casInUnit.hasError()) {
        validateUnitForCapture(casInUnit);
      }
      if (!casInUnit.hasError()) {
        handleUnitCaptureAndUpdate(casInUnit)
      }
      if (casInUnit.getAttribute(IS_TBD_LOAD)) {
        tbdLoad = true
      } else if (casInUnit.getAttribute(IS_LOAD)) {
        load = true
      } else if (casInUnit.getAttribute(IS_DISC)) {
        discharge = true
      }
      if (casInUnit.hasError()) {
        hasError = true;
      }
    }
    if (hasError) {
      return;
    }
    try {
      if (tbdLoad) {
        loadUnitsForTbd()
      } else if (load) {
        loadUnits()
      } else if (discharge) {
        dischargeUnits()
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      log(ex.getLocalizedMessage());
      // If exception, set error status for all units
      for (CasInUnit25 casInUnit : _casInUnits) {
        casInUnit.setReturnStatus(Status.ERROR)
        casInUnit.setReturnCode(_casMessageHelper.BIZ_ERROR_CODE)
        casInUnit.setReturnMessage(ex.getLocalizedMessage())
      }
    }
  }

  /**
   * Validate a unit
   * @param inCasInUnit
   */
  protected void validateUnitForCapture(CasInUnit25 inCasInUnit) {
    if (!UNIT_CAPTURE_IMAGE_TYPE.equals(_action) && StringUtils.isBlank(inCasInUnit.getUnitId())) {
      log(_casMessageHelper.MISSING_UNIT_ID_CODE + ":" + _casMessageHelper.MISSING_UNIT_ID_MESSAGE);
      inCasInUnit.setReturnStatus(Status.ERROR);
      inCasInUnit.setReturnCode(_casMessageHelper.MISSING_UNIT_ID_CODE)
      inCasInUnit.setReturnMessage(_casMessageHelper.MISSING_UNIT_ID_MESSAGE)
      return
    }
    //Validate unit
    try {
      if (UNIT_CAPTURE_IMAGE_TYPE.equals(_action) || UNIT_CAPTURE_CREATE_TYPE.equals(_action)) {
        return;//nothing to validate
      }
      validateUnitActive(inCasInUnit)
      UnitFacilityVisit unitFacilityVisit = inCasInUnit.getUnitFacilityVisit()
      //Validate that the in case of "Identify" action, unit is valid for load/discharge
      if (UNIT_CAPTURE_IDENTIFY_TYPE.equals(_action)) {
        List<WorkInstruction> currentWIList = unitFacilityVisit.getCurrentWiList();
        if (currentWIList == null || currentWIList.isEmpty()) {
          try {
            LocPosition currPos = unitFacilityVisit.getUfvUnit().findCurrentPosition();
            if (currPos.isVesselPosition()) {
              inCasInUnit.setAttribute(IS_DISC, true)
            } else {
              validateTbdLoadStatus(inCasInUnit)
              inCasInUnit.setAttribute(IS_TBD_LOAD, true)
            }
          } catch (BizViolation tbdBv) {
            log(_casMessageHelper.BIZ_ERROR_CODE + ":" + tbdBv.getLocalizedMessage());
            inCasInUnit.setReturnStatus(Status.ERROR);
            inCasInUnit.setReturnCode(_casMessageHelper.BIZ_ERROR_CODE)
            inCasInUnit.setReturnMessage(tbdBv.getLocalizedMessage())
            return;
          }

          if (!inCasInUnit.getAttribute(IS_TBD_LOAD)) {
            inCasInUnit.setReturnStatus(Status.WARNING)

            if (inCasInUnit.getAttribute(IS_DISC)) {
              log(_casMessageHelper.NOT_IN_DSCH_PLAN_CODE + ":" + _casMessageHelper.NOT_IN_DSCH_PLAN_MESSAGE);
              inCasInUnit.setReturnCode(_casMessageHelper.NOT_IN_DSCH_PLAN_CODE)
              inCasInUnit.setReturnMessage(_casMessageHelper.NOT_IN_DSCH_PLAN_MESSAGE)
              inCasInUnit.removeAttribute(IS_DISC);
            } else {
              log(_casMessageHelper.NOT_VALID_FOR_TBD_LOAD_CODE + ":" + _casMessageHelper.NOT_VALID_FOR_TBD_LOAD_MESSAGE);
              inCasInUnit.setReturnCode(_casMessageHelper.NOT_VALID_FOR_TBD_LOAD_CODE)
              inCasInUnit.setReturnMessage(_casMessageHelper.NOT_VALID_FOR_TBD_LOAD_MESSAGE)
            }
          }
        } else { // if WI is not empty
          WorkInstruction targetWI = null;
          if (RestowTypeEnum.RESTOW.equals(unitFacilityVisit.getUfvRestowType()) ||
                  UnitCategoryEnum.TRANSSHIP.equals(unitFacilityVisit.getUfvUnit().getUnitCategory())) {
            for (WorkInstruction currentWi: currentWIList) {
              if (currentWi.getWiMoveKind().equals(WiMoveKindEnum.VeslDisch)) {
                targetWI = currentWi;
                break;
              }
            }
          }
          if  (targetWI == null) {
            targetWI = currentWIList.get(0);
          }
          LocPosition currentPos = unitFacilityVisit.getUfvUnit().findCurrentPosition();
          if (targetWI != null) {
            //2013.03.14 azharad ARGO-45543 Allow Shift On Board move from this Groovy
            if (currentPos.isVesselPosition() && targetWI.getWiToPosition().isVesselPosition()
                    && currentPos.getPosLocId().equals(targetWI.getWiToPosition().getPosLocId())) {
              inCasInUnit.setAttribute(IS_LOAD, true)
            } else if (targetWI.getWiToPosition().isVesselPosition() || targetWI.getWiToPosition().isRailPosition()) {
              log("Unit is planned for load to [" + targetWI.getWiToPosition() + "]")
              CarrierVisit obCv = unitFacilityVisit.getUfvActualObCv();
              if (!obCv.getCvId().equals(_visitId)) {
                final String errorMsg = _casMessageHelper.CARRIER_MISMATCH_LOAD_MESSAGE + "[ActualVisitId="+ _visitId + ",PlannedVisitId=" + obCv.getCarrierVehicleId() + "]"
                log(_casMessageHelper.CARRIER_MISMATCH_LOAD_CODE + ":" + errorMsg)
                inCasInUnit.setReturnStatus(Status.ERROR);
                inCasInUnit.setReturnCode(_casMessageHelper.CARRIER_MISMATCH_LOAD_CODE)
                inCasInUnit.setReturnMessage(errorMsg)
              }
              inCasInUnit.setAttribute(IS_LOAD, true)
            } else if (currentPos.isCarrierPosition() && targetWI.getWiToPosition().isYardPosition()) {
              log("Unit is planned for discharge to [" + targetWI.getWiToPosition() + "]")
              CarrierVisit ibCv = unitFacilityVisit.getUfvActualIbCv();
              if (!ibCv.getCvId().equals(_visitId)) {
                final String errorMsg =_casMessageHelper.CARRIER_MISMATCH_DSCH_MESSAGE + "[ActualVisitId="+ _visitId + ",PlannedVisitId=" + ibCv.getCarrierVehicleId() + "]"
                log(_casMessageHelper.CARRIER_MISMATCH_DSCH_CODE + ":" + errorMsg)
                inCasInUnit.setReturnStatus(Status.ERROR);
                inCasInUnit.setReturnCode(_casMessageHelper.CARRIER_MISMATCH_DSCH_CODE)
                inCasInUnit.setReturnMessage(errorMsg)
              }
              inCasInUnit.setAttribute(IS_DISC, true)
            } else {
              log(_casMessageHelper.NOT_IN_LOAD_OR_DSCH_PLAN_ERR_CODE + ":" + _casMessageHelper.NOT_IN_LOAD_OR_DSCH_PLAN_ERR_MESSAGE);
              inCasInUnit.setReturnStatus(Status.ERROR);
              inCasInUnit.setReturnCode(_casMessageHelper.NOT_IN_LOAD_OR_DSCH_PLAN_ERR_CODE)
              inCasInUnit.setReturnMessage(_casMessageHelper.NOT_IN_LOAD_OR_DSCH_PLAN_ERR_MESSAGE)
            }
          }
        }
      } else if (UNIT_CAPTURE_UPDATE_TYPE.equals(_action)) {
        //Verify that unit reference in ufv and unit reference of the incoming message match
        verifyUnitReferenceAndTransactionReference(inCasInUnit, unitFacilityVisit)
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      log(ex.getLocalizedMessage());
      inCasInUnit.setReturnStatus(Status.ERROR);
      inCasInUnit.setReturnMessage(ex.getLocalizedMessage());
    }
  }

  /**
   * Validate if unit is active and populate the unit id and unitFacilityVisit fields of inCasInUnit.
   *
   * @param inCasInUnit CasInUnit
   * @throws BizViolation trows BizViolation if the validation fails
   */
  protected void validateUnitActive(CasInUnit25 inCasInUnit) throws BizViolation {
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
   * Validate that the unit can act as a TBD Load
   * @param inCasInUnit
   * @throws BizViolation
   */
  protected void validateTbdLoadStatus(CasInUnit25 inCasInUnit) throws BizViolation {
    if (inCasInUnit.getUnitFacilityVisit().getFinalPlannedPosition().equals(null)) {
      HcTbdUnitManager hcTbdMngr = (HcTbdUnitManager) Roastery.getBean(HcTbdUnitManager.BEAN_ID);
      CarrierVisit cv = CarrierVisit.findVesselVisit(ContextHelper.getThreadFacility(), _visitId);
      if (cv == null) {
        throw BizViolation.create(InventoryPropertyKeys.ERROR_UNIT_IDENTIFICATION_VESSEL_VISIT_DOES_NOT_EXIST, null, _visitId);
      }
      hcTbdMngr.validateTbdUnitForLoad(inCasInUnit.getUnitFacilityVisit(), cv);
    }
  }

  protected void verifyUnitReferenceAndTransactionReference(CasInUnit25 inCasInUnit, UnitFacilityVisit inUnitFacilityVisit) {
    if (!inCasInUnit.getCasUnitReference().equals(inUnitFacilityVisit.getUfvCasUnitReference())) {
      final String unitErrorCode = _casMessageHelper.UNIT_REFERENCE_NOT_SAME_CODE
      final String unitErrorMsg = _casMessageHelper.UNIT_REFERENCE_NOT_SAME_MESSAGE + "[Incoming=" + inCasInUnit.getCasUnitReference() +
              ",Existing=" + inUnitFacilityVisit.getUfvCasUnitReference() + "]"
      log(unitErrorCode + ":" + unitErrorMsg);
      inCasInUnit.setReturnStatus(Status.ERROR);
      inCasInUnit.setReturnCode(unitErrorCode)
      inCasInUnit.setReturnMessage(unitErrorMsg)
    }
    if (inCasInUnit.getCasTransactionReference() != null &&
            inUnitFacilityVisit.getUfvCasTransactionReference() != null &&
            !inCasInUnit.getCasTransactionReference().equals(inUnitFacilityVisit.getUfvCasTransactionReference())) {
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
   * Handles the unit capture message and updates the unit
   * @param inCasInUnit
   */
  protected void handleUnitCaptureAndUpdate(CasInUnit25 inCasInUnit) {
    try {
      if (UNIT_CAPTURE_CREATE_TYPE.equals(_action)) {
        createUnit(inCasInUnit)
      }
      if (UNIT_CAPTURE_IMAGE_TYPE.equals(_action)) {
        handleImageCaptureMessage(inCasInUnit)
      } else {
        // Record cas unit capture event
        String nodeText = "Action: " + _action + "," + inCasInUnit.getUnitNode().toString();
        if (nodeText.length() > 255) {
          nodeText = nodeText.substring(0, 254)
        }
        recordEvent(inCasInUnit.getUnit(), EventEnum.CAS_UNIT_CAPTURE, null, nodeText)
        updateUnitAttributes(inCasInUnit);
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      log(ex.getLocalizedMessage());
      inCasInUnit.setReturnStatus(Status.ERROR);
      inCasInUnit.setReturnMessage(ex.getLocalizedMessage())
    }
  }

  /**
   * Creates a unit corresponding to
   * @param inCasInUnit : CAS unit
   */
  protected void createUnit(CasInUnit25 inCasInUnit) throws BizViolation {
    UnitManager unitManager = (UnitManager) Roastery.getBean(UnitManager.BEAN_ID);
    CarrierVisit carrierVisit = CarrierVisit.findVesselVisit(ContextHelper.getThreadFacility(), _visitId);

    Container container = null;
    String isoCode = inCasInUnit.getUnitNode().attributes().get("iso-code")
    if (StringUtils.isBlank(isoCode)) {
      log(_casMessageHelper.MISSING_ISO_CODE_FOR_CREATE_WARNING_CODE + ":" + _casMessageHelper.MISSING_ISO_CODE_FOR_CREATE_WARNING_MESSAGE);
      inCasInUnit.setReturnStatus(Status.WARNING);
      inCasInUnit.setReturnCode(_casMessageHelper.MISSING_ISO_CODE_FOR_CREATE_WARNING_CODE)
      inCasInUnit.setReturnMessage(_casMessageHelper.MISSING_ISO_CODE_FOR_CREATE_WARNING_MESSAGE)
      container = Container.findOrCreateContainer(inCasInUnit.getUnitId(), DataSourceEnum.CRANE_AUTOMATION_SYSTEM)
    } else {
      container = Container.findOrCreateContainer(inCasInUnit.getUnitId(), isoCode, DataSourceEnum.CRANE_AUTOMATION_SYSTEM)
    }
    UnitFacilityVisit ufv = unitManager.findOrCreateStowplanUnit(container, carrierVisit, carrierVisit.getCarrierOperator(),
            ContextHelper.getThreadFacility())
    RectifyParms parms = new RectifyParms()
    parms.setUfvTransitState(UfvTransitStateEnum.S20_INBOUND)
    parms.setUnitVisitState(UnitVisitStateEnum.ACTIVE)
    ufv.rectify(parms);
    final Unit unit = ufv.getUfvUnit()
    unit.updateCategory(UnitCategoryEnum.IMPORT)
    //Set the newly created unit and ufv on CasInUnit
    inCasInUnit.setUnit(unit)
    inCasInUnit.setUnitFacilityVisit(ufv)
  }

  /**
   * Handle image capture message
   * @param inCasInUnit : CAS unit
   */
  protected void handleImageCaptureMessage(CasInUnit25 inCasInUnit) {
    inCasInUnit.setReturnStatus(Status.OK)
    inCasInUnit.setReturnCode(_casMessageHelper.UNIT_CAPTURE_HANDLED_SUCCESSFULLY_CODE)
    inCasInUnit.setReturnMessage(_casMessageHelper.UNIT_CAPTURE_HANDLED_SUCCESSFULLY_MESSAGE + "[Action=" + _action + "]")
  }

  /**
   * Update the unit with attributes from unit xml
   * @param inCasInUnit
   * @throws BizViolation
   */
  protected void updateUnitAttributes(CasInUnit25 inCasInUnit) throws BizViolation {
    Node unitNode = inCasInUnit.getUnitNode()
    Unit unit = inCasInUnit.getUnit()
    UnitFacilityVisit ufv = inCasInUnit.getUnitFacilityVisit()
    Equipment primaryEquipment = unit.getPrimaryEq();
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
    // Gross Weight
    if (unitNode.attributes().containsKey("gross-weight")) {
      unit.updateGoodsAndCtrWtKg((Double) getAttributeValue("gross-weight", unitNode.@"gross-weight", Double.class));
    }
    // Yard Measured weight
    if (unitNode.attributes().containsKey("yard-measured-weight")) {
      unit.updateGoodsAndCtrWtKgAdvised((Double) getAttributeValue("yard-measured-weight", unitNode.@"yard-measured-weight", Double.class));
    }
    // ISO code
    if (!UNIT_CAPTURE_CREATE_TYPE.equals(_action)) { //iso-code is handled as part of create
      if (unitNode.attributes().containsKey("iso-code")) {
        primaryEquipment.upgradeEqType(unitNode.@"iso-code", DataSourceEnum.CRANE_AUTOMATION_SYSTEM)
      }
    }
    // Height mm
    if (unitNode.attributes().containsKey("height-mm")) {
      primaryEquipment.upgradeEqHeight((Long) getAttributeValue("height-mm", unitNode.@"height-mm", Long.class), DataSourceEnum.CRANE_AUTOMATION_SYSTEM)
    }
    // Length mm
    if (unitNode.attributes().containsKey("length-mm")) {
      primaryEquipment.setFieldValue(ArgoRefField.EQ_LENGTH_MM, (Long) getAttributeValue("length-mm", unitNode.@"length-mm", Long.class))
    }
    // Width mm
    if (unitNode.attributes().containsKey("width-mm")) {
      primaryEquipment.setFieldValue(ArgoRefField.EQ_WIDTH_MM, (Long) getAttributeValue("width-mm", unitNode.@"width-mm", Long.class))
    }
    // Tank rail type
    if (unitNode.attributes().containsKey("tank-rail-type")) {
      String tankRails = unitNode.@"tank-rail-type"
      TankRailTypeEnum tankRailTypeEnum = TankRailTypeEnum.getEnum(tankRails)
      if (tankRailTypeEnum == null) {
        throw invalidValueViolation("tank-rail-type", tankRails)
      }
      primaryEquipment.setFieldValue(ArgoRefField.EQ_TANK_RAILS, tankRailTypeEnum)
    }
    // OOG
    Node oogNode = unitNode.'oog'[0]
    if (oogNode != null) {
      unit.updateOog(extractOog("back-cm", oogNode.@"back-cm"), extractOog("front-cm", oogNode.@"front-cm"), extractOog("left-cm", oogNode.@"left-cm"),
              extractOog("right-cm", oogNode.@"right-cm"), extractOog("top-cm", oogNode.@"top-cm"))
    }
    // Seals
    Node sealsNode = unitNode.'seals'[0]
    if (sealsNode != null) {
      unit.updateSeals(sealsNode.@"seal-1", sealsNode.@"seal-2", sealsNode.@"seal-3", sealsNode.@"seal-4")
    }
    // Damages
    Node damagesNode = unitNode.'damages'[0]
    if (damagesNode != null) {
      UnitEquipment unitEquipment = unit.getCurrentlyAttachedUe(primaryEquipment)
      if (unitEquipment != null) {
        // Clear all existing damages - they will be completely replaced by this update
        unitEquipment.attachDamages(null);
        EquipClassEnum equipClass = unitEquipment.getUeEquipment().getEqClass();
        NodeList damageNodes = damagesNode.'damage'
        damageNodes.each { Node damageNode ->
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
    // Flags
    Node flagsNode = unitNode.'flags'[0]
    if (flagsNode != null) {
      ServicesManager servicesManager = (ServicesManager) Roastery.getBean(ServicesManager.BEAN_ID);
      NodeList holdsNodeList = flagsNode.'hold'
      holdsNodeList.each { Node holdNode ->
        String holdId = holdNode.@'id'
        servicesManager.applyHold(holdId, unit, null, null, "cas");
      }
      NodeList permissionsNode = flagsNode.'permission'
      permissionsNode.each { Node permissionNode ->
        String permissionId = permissionNode.@'id'
        servicesManager.applyPermission(permissionId, unit, null, null, "cas");
      }
    }
    // Observed placards
    Node observedPlacardsNode = unitNode."observed-placards"[0]
    if (observedPlacardsNode != null) {
      NodeList obsPlacardNodeList = observedPlacardsNode."observed-placard"
      obsPlacardNodeList.each { Node obsPlacardNode ->
        String placardText = obsPlacardNode.@'placard'
        ObservedPlacard observedPlacard = ObservedPlacard.createObservedPlacard(unit, placardText)
        observedPlacard.setFieldValue(InventoryField.OBSPLACARD_REMARK, obsPlacardNode.@'remarks')
      }
    }
    //Update Bundled Equipment
    if (bundledEquipmentNode != null) {
      NodeList equipmentNodeList = bundledEquipmentNode."equipment"
      equipmentNodeList.each { Node equipmentNode ->
        Equipment equipment = Equipment.findEquipment(equipmentNode.@'id')
        if (equipment != null) {
          // Update the attributes
          // ISO code
          if (equipmentNode.attributes().containsKey("iso-code")) {
            equipment.upgradeEqType(equipmentNode.@"iso-code", DataSourceEnum.CRANE_AUTOMATION_SYSTEM)
          }
          // Height mm
          if (equipmentNode.attributes().containsKey("height-mm")) {
            equipment.upgradeEqHeight((Long) getAttributeValue("height-mm", equipmentNode.@"height-mm", Long.class), DataSourceEnum.CRANE_AUTOMATION_SYSTEM)
          }
          // Length mm
          if (equipmentNode.attributes().containsKey("length-mm")) {
            equipment.setFieldValue(ArgoRefField.EQ_LENGTH_MM, (Long) getAttributeValue("length-mm", equipmentNode.@"length-mm", Long.class))
          }
          // Width mm
          if (equipmentNode.attributes().containsKey("width-mm")) {
            equipment.setFieldValue(ArgoRefField.EQ_WIDTH_MM, (Long) getAttributeValue("width-mm", equipmentNode.@"width-mm", Long.class))
          }
          // Attach it to the unit
          unit.attachPayload(equipment)
        }

      }
    }
    //Set the unit reference and transaction reference
    if (UNIT_CAPTURE_IDENTIFY_TYPE.equals(_action) || UNIT_CAPTURE_CREATE_TYPE.equals(_action)) {
      ufv.setFieldValue(InventoryField.UFV_CAS_UNIT_REFERENCE, inCasInUnit.getCasUnitReference())
      if (!StringUtils.isBlank(inCasInUnit.getCasTransactionReference())) {
        ufv.setFieldValue(InventoryField.UFV_CAS_TRANSACTION_REFERENCE, inCasInUnit.getCasTransactionReference())
      }
    }
  }

  /**
   * Utility to extract OOG long value
   * @param inField
   * @param inValue
   * @return Long
   */
  private Long extractOog(String inField, String inValue) {
    return getAttributeValue(inField, inValue, Long.class) as Long
  }

  /**
   * Translates an attribute value to correct class.
   * @param inName name of the attribute
   * @param inName value of the attribute
   * @param inValueClass The java Class of the value within the Entity to which it belongs
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

  /**
   * Load TBD units through XPS
   */
  protected void loadUnitsForTbd() {
    UnitManager unitManager = (UnitManager) Roastery.getBean(UnitManager.BEAN_ID);
    List<UnitFacilityVisit> ufvList = new ArrayList<UnitFacilityVisit>(_unitCount);
    String[] unitIds = new String[_unitCount]
    String[] locIds = new String[_unitCount]
    Boolean requestPos = false;
    Map<String, Object> resultFromXPS = null;
    UnitFacilityVisit ufv = null;
    CarrierVisit cv = CarrierVisit.findVesselVisit(ContextHelper.getThreadFacility(), _visitId);
    if (cv != null) {
      VisitDetails visitDetails = cv.getCvCvd();
      // Determine if message contains position(s)
      for (int i = 0; i < _casInUnits.length; i++) {
        CasInUnit25 casInUnit = _casInUnits[i];
        ufv = unitManager.findActiveUfvForUnitDigits(casInUnit.getUnitId());
        ufvList.add(ufv);
        unitIds[i] = casInUnit.getUnitId();
        String unitPos = casInUnit.getAttribute(POSITION_PARAM)
        if (unitPos == null || unitPos.isEmpty()) {
          requestPos = true;
        }
      }
      // Request positions if necessary
      if (requestPos) {
        String mode = "single";
        if (_casInUnits.length == 4) {
          mode = "quad";
        } else if (_casInUnits.length == 2) {
          ufv = unitManager.findActiveUfvForUnitDigits(unitIds[0]);
          Unit unit = ufv.getUfvUnit();
          EquipNominalLengthEnum unitEqNomLen = unit.getPrimaryEq().getEqEquipType().getEqtypNominalLength();
          if (unitEqNomLen == EquipNominalLengthEnum.NOM20) {
            mode = "twin";
          } else {
            mode = "tandem";
          }
        }
        resultFromXPS = unitManager.requestXPSForLoadPosition(unitIds, _craneId, _visitId, mode);
        int result = (Integer) resultFromXPS.get(BentoMessageAttribute.RESULT);
        // Return errors in position request
        if (result < 0) {
          String errorMsg = (String) resultFromXPS.get(BentoMessageAttribute.ERROR_MESSAGE);
          for (CasInUnit25 casInUnit : _casInUnits) {
            casInUnit.setReturnMessage(errorMsg)
            casInUnit.setReturnStatus(Status.ERROR)
          }
          registerError(errorMsg)
          return;
        }
        for (int i = 0; i < _casInUnits.length; i++) {
          String posString = BentoMessageAttributeGetNextLoadPosition.LOAD_POSITION_PREFIX;
          posString += Integer.toString(i + 1);
          locIds[i] = resultFromXPS.get(posString);
          if (locIds[i] == null || StringUtils.isEmpty(locIds[i])) {
            String errStr = _casMessageHelper.EMPTY_POSITION_FOR_TBD_UNIT_MESSAGE + "[id=" + unitIds[i] + "]"
            _casInUnits[i].setReturnCode(_casMessageHelper.EMPTY_POSITION_FOR_TBD_UNIT_CODE)
            _casInUnits[i].setReturnMessage(errStr)
            _casInUnits[i].setReturnStatus(Status.ERROR)
            registerError(errStr)
            return;
          }
          _casInUnits[i].setAttribute(POSITION_PARAM, locIds[i])
        }
      } else { // All units submitted with position
        for (int i = 0; i < _casInUnits.length; i++) {
          CasInUnit25 casInUnit = _casInUnits[i];
          String unitPos = casInUnit.getAttribute(POSITION_PARAM);
          locIds[i] = unitPos;
        }
      }
      unitManager.loadUnitThruXPS(ufvList, visitDetails, _craneId, unitIds, locIds);
      for (CasInUnit25 casInUnit : _casInUnits) {
        StringBuilder buffer = new StringBuilder("Unit ")
        buffer.append(casInUnit.getUnitId())
        buffer.append(" loaded to location ")
        buffer.append(casInUnit.getAttribute(POSITION_PARAM))
        casInUnit.setReturnStatus(Status.OK)
        casInUnit.setReturnCode(_casMessageHelper.UNIT_SUCCESSFULLY_PROCESSED_CODE)
        casInUnit.setReturnMessage(buffer.toString())
      }
    }
  }

  /**
   * Load units through XPS
   */
  protected void loadUnits() {
    UnitManager unitManager = (UnitManager) Roastery.getBean(UnitManager.BEAN_ID);
    String[] slots = new String[_casInUnits.length]
    List<UnitFacilityVisit> ufvList = new ArrayList<UnitFacilityVisit>(_casInUnits.length)
    CarrierVisit cv = CarrierVisit.findVesselVisit(ContextHelper.getThreadFacility(), _visitId)
    String[] ctrIds = new String[_casInUnits.length]
    for (int i = 0; i < _casInUnits.length; i++) {
      CasInUnit25 casInUnit = _casInUnits[i]
      ufvList.add(casInUnit._unitFacilityVisit)
      ctrIds[i] = casInUnit._unitId
      String stowPos = casInUnit.getAttribute(POSITION_PARAM)
      if (StringUtils.isEmpty(stowPos)) {
        LocPosition finalStowPos = casInUnit._unitFacilityVisit.getFinalPlannedPosition()
        stowPos = finalStowPos != null ? finalStowPos.getPosSlot() : "";
      }
      slots[i] = stowPos
      UnitFacilityVisit ufv = casInUnit._unitFacilityVisit
      Unit unit = ufv.getUfvUnit()
      IEventType eventType = EventEnum.UNIT_LOAD;
      if (!LocPosition.isApron(stowPos)) {
          ServicesManager srvcMgr = (ServicesManager) Roastery.getBean(ServicesManager.BEAN_ID);
          BizViolation bv = srvcMgr.verifyEventAllowed(eventType, unit);
          if (bv != null) {
              log(_casMessageHelper.BIZ_ERROR_CODE + ":" + bv.getLocalizedMessage());
              casInUnit.setReturnStatus(Status.ERROR);
              casInUnit.setReturnCode(_casMessageHelper.BIZ_ERROR_CODE)
              casInUnit.setReturnMessage(bv.getLocalizedMessage())
              return;
          }
      }
    }
    unitManager.loadUnitThruXPS(ufvList, cv.getCvCvd(), _craneId, ctrIds, slots)
    for (int i = 0; i < _casInUnits.length; i++) {
      CasInUnit25 casInUnit = _casInUnits[i]
      StringBuilder buffer = new StringBuilder("Unit ")
      buffer.append(casInUnit.getUnitId())
      buffer.append(" loaded to location ")
      buffer.append(slots[i])
      casInUnit.setReturnStatus(Status.OK)
      casInUnit.setReturnCode(_casMessageHelper.UNIT_SUCCESSFULLY_PROCESSED_CODE)
      casInUnit.setReturnMessage(buffer.toString())
    }
  }

  /**
   * Discharge units through XPS
   */
  protected void dischargeUnits() {
    UnitManager unitManager = (UnitManager) Roastery.getBean(UnitManager.BEAN_ID);
    String[] ctrIds = new String[_casInUnits.length]
    String[] laneIds = new String[_casInUnits.length];
    Boolean[] replanCtrs = new Boolean[_casInUnits.length]
    CarrierVisit cv = CarrierVisit.findVesselVisit(ContextHelper.getThreadFacility(), _visitId)
    for (int i = 0; i < _casInUnits.length; i++) {
      CasInUnit25 casInUnit = _casInUnits[i]
      ctrIds[i] = casInUnit._unitId
      laneIds[i] = casInUnit._laneId
      String stowPos = casInUnit.getAttribute(POSITION_PARAM)
      if (StringUtils.isEmpty(stowPos)) {
        LocPosition finalStowPos = casInUnit._unitFacilityVisit.getFinalPlannedPosition()
        stowPos = finalStowPos != null ? finalStowPos.getPosSlot() : "";
      }
      if (StringUtils.isEmpty(laneIds[i])) {
        laneIds[i] = stowPos == null ? "" : stowPos
      }
    }
    unitManager.dischargeUnitThruXPS(_craneId, cv.getCvCvd(), ctrIds, replanCtrs, laneIds, _tier, null);
    for (int i = 0; i < _casInUnits.length; i++) {
      CasInUnit25 casInUnit = _casInUnits[i]
      StringBuilder buffer = new StringBuilder("Unit ")
      buffer.append(casInUnit.getUnitId())
      buffer.append(" discharged to location ")
      buffer.append(laneIds[i])
      casInUnit.setReturnStatus(Status.OK)
      casInUnit.setReturnCode(_casMessageHelper.UNIT_SUCCESSFULLY_PROCESSED_CODE)
      casInUnit.setReturnMessage(buffer.toString())
    }
  }

  /**
   * Create response message to CAS request
   * @param inAdditionalInfo
   * @return String
   */
  protected String createResponseXml(Map<String, String> inAdditionalInfo) {
    def writer = new StringWriter();
    def xml = new MarkupBuilder(writer);
    xml.payload() {
      parameters() {
        if (_parameterMap != null && !_parameterMap.isEmpty()) {
          _parameterMap.keySet().each {
            parameter(id: it, value: _parameterMap.get(it));
          }
        }
      }
      if (inAdditionalInfo != null && !inAdditionalInfo.isEmpty()) {
        "additional-info"() {
          inAdditionalInfo.keySet().each {
            field(id: it, value: inAdditionalInfo.get(it));
          }
        }
      }
      "units-response"() {
        for (CasInUnit25 casInUnit : _casInUnits) {
          "unit-response"("cas-unit-reference": casInUnit.getCasUnitReference(), "cas-transaction-reference": casInUnit.getCasTransactionReference(),
                  id: casInUnit.getUnitId(), status: casInUnit.getStatusAsString()) {
            message(code:casInUnit.getReturnCode(), text: casInUnit.getReturnMessage())
          }
        }
      }
    }
    String out = writer.toString();
    return out;
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
   * This class encapsulates the unit information coming from the CAS
   * It also holds any error information added during validation and unit facility visit information if validation is successful
   */
  protected class CasInUnit25 {

    CasInUnit25(Node inUnitNode) {
      _unitNode = inUnitNode
      _casUnitReference = _unitNode."@cas-unit-reference"
      _casTransactionReference = _unitNode."@cas-transaction-reference"
      _unitId = _unitNode."@id"
      _returnStatus = Status.WARNING
      _returnCode = _casMessageHelper.UNIT_NOT_PROCESSED_CODE
      _returnMessage = _casMessageHelper.UNIT_NOT_PROCESSED_MESSAGE
      _laneId = _unitNode."@lane"
    }

    private String _laneId;
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

    void setAttribute(String inString, Object inObject) {
      _attributeMap.put(inString, inObject)
    }

    Object getAttribute(String inString) {
      if (_attributeMap.get(inString) != null) {
        return _attributeMap.get(inString);
      } else {
        return null;
      }
    }

    Object removeAttribute(String inString) {
      return _attributeMap.remove(inString)
    }

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

    boolean isOK() {
      return Status.OK == _returnStatus
    }

    public String getStatusAsString() {
      switch (_returnStatus) {
        case Status.OK: return "OK"
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