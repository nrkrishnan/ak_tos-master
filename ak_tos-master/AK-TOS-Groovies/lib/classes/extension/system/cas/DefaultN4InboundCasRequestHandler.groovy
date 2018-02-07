/*
 * Copyright (c) 2013 Navis LLC. All Rights Reserved.
 *
 */



package extension.system.cas
import com.navis.argo.ArgoField
import com.navis.argo.business.api.Serviceable
import com.navis.argo.business.atoms.EventEnum
import com.navis.argo.business.atoms.LocTypeEnum
import com.navis.argo.business.model.CarrierVisit
import com.navis.argo.business.model.VisitDetails
import com.navis.argo.util.EntityXmlStreamWithSimpleHeader
import com.navis.external.argo.AbstractGroovyWSCodeExtension
import com.navis.framework.metafields.MetafieldId
import com.navis.framework.metafields.MetafieldIdFactory
import com.navis.framework.persistence.HibernateApi
import com.navis.framework.portal.QueryUtils
import com.navis.framework.portal.query.DomainQuery
import com.navis.framework.portal.query.PredicateFactory
import com.navis.framework.portal.query.PredicateIntf
import com.navis.framework.util.message.MessageCollector
import com.navis.inventory.business.api.UnitField
import com.navis.inventory.util.TransitStateQueryUtil
import com.navis.rail.RailEntity
import com.navis.rail.business.entity.TrainVisitDetails
import com.navis.spatial.business.model.AbstractBin
import com.navis.vessel.VesselEntity
import com.navis.vessel.business.schedule.VesselVisitDetails
import groovy.xml.MarkupBuilder
import org.apache.commons.lang.StringUtils
/**
 * This class contains reference implementation to handle requests coming from the QRCAS system.
 * Supported Request types handled by this class:
 * 1. Carrier Geometry request, (Vessel or Train)
 * 2. Container List request
 *
 * @author <a href="mailto:arvinder.brar@navis.com">Arvinder Brar</a>,2012-09-06
 */
class DefaultN4InboundCasRequestHandler extends AbstractGroovyWSCodeExtension {

  private final String REQUEST_TYPE_PARAM = "requestType"
  private final String VISIT_ID_PARAM = "visitId"
  private final String VISIT_TYPE_PARAM = "visitType"
  private final String ON_BOARD_PARAM = "onBoard"
  private final String CRANE_ID_PARAM = "craneId"
  private final String VISIT_TYPE_VESSEL = "VESSEL"
  private final String VISIT_TYPE_TRAIN = "TRAIN"
  private final String SHIP_BIN_MODEL_ENTITY_NAME = "ShipBinModel"
  private final String RAIL_BIN_MODEL_ENTITY_NAME = "RailBinModel"
  private final String SHIP_GEOMETRY = "ship-geometry"
  private final String CARRIER_GEOMETRY_REQUEST = "carrierGeometryRequest";
  private final String CONTAINER_LIST_REQUEST = "containerListRequest";
  private final String SEND_ON_BOARD_UNIT_UPDATES = "sendOnBoardUnitUpdates";
  private final String SEND_CRANE_WORK_LIST_UPDATES = "sendCraneWorkListUpdates";
  private MessageCollector _messageCollector;
  //CasHelper library name
  public final String CAS_HELPER = "CasHelper"
  //CasHelper library code extension instance
  def _casHelper = null;
  //CasMessageHelper library name
  public final String CAS_MESSAGE_HELPER = "CasMessageHelper"
  //CasMessageHelper library code extension instance
  def _casMessageHelper = null;

  /**
   * Main entry point method of the code extension.
   * @param inParameters Map<String, Object> parameters sent as part of groovy web service
   * @return the string response to the groovy webservice call
   */

  public String execute(Map inParameters) {
    _messageCollector = getMessageCollector();
    String returnXml = null;
    //Log the request content
    log("\nRequest: " + getParametersAsString())
    //validate request type parameter
    String requestType = inParameters.get(REQUEST_TYPE_PARAM);
    Map<String, String> additionalInfoMap = new HashMap<String, String>();
    initCasMessageHelper()
    if (StringUtils.isBlank(requestType)){
      registerError(_casMessageHelper.MISSING_REQUEST_TYPE_CODE + ":" + _casMessageHelper.MISSING_REQUEST_TYPE_MESSAGE);
      return getXmlErrorContent(_casMessageHelper.MISSING_REQUEST_TYPE_CODE, _casMessageHelper.MISSING_REQUEST_TYPE_MESSAGE);
    }
    //Validate 'visitType' and 'visitId' parameters
    String visitId = inParameters.get(VISIT_ID_PARAM);
    String visitType = inParameters.get(VISIT_TYPE_PARAM);
    if (StringUtils.isBlank(visitId)){
      registerError(_casMessageHelper.MISSING_VISIT_ID_CODE + ":" + _casMessageHelper.MISSING_VISIT_ID_MESSAGE);
      return getXmlErrorContent(_casMessageHelper.MISSING_VISIT_ID_CODE, _casMessageHelper.MISSING_VISIT_ID_MESSAGE);
    }
    if (StringUtils.isBlank(visitType)){
      registerError(_casMessageHelper.MISSING_VISIT_TYPE_CODE + ":" + _casMessageHelper.MISSING_VISIT_TYPE_MESSAGE);
      return getXmlErrorContent(_casMessageHelper.MISSING_VISIT_TYPE_CODE, _casMessageHelper.MISSING_VISIT_TYPE_MESSAGE);;
    }
    MetafieldId carrierVisitDetailsCarrierVisitId = MetafieldIdFactory.getCompoundMetafieldId(ArgoField.CVD_CV, ArgoField.CV_ID);
    String carrierVisitEntityName = null;
    if (VISIT_TYPE_VESSEL.equals(visitType)) {
      carrierVisitEntityName = VesselEntity.VESSEL_VISIT_DETAILS;
    } else if (VISIT_TYPE_TRAIN.equals(visitType)){
      carrierVisitEntityName = RailEntity.TRAIN_VISIT_DETAILS;
    } else{
      registerError(_casMessageHelper.INVALID_VISIT_TYPE_CODE + ":" + _casMessageHelper.INVALID_VISIT_TYPE_MESSAGE);
      return getXmlErrorContent(_casMessageHelper.INVALID_VISIT_TYPE_CODE, _casMessageHelper.INVALID_VISIT_TYPE_MESSAGE);;
    }
    DomainQuery dq = QueryUtils.createDomainQuery(carrierVisitEntityName)
            .addDqPredicate(PredicateFactory.eq(carrierVisitDetailsCarrierVisitId, visitId));
    VisitDetails carrierVisitDetails = (VisitDetails)HibernateApi.getInstance().getUniqueEntityByDomainQuery(dq);
    if (carrierVisitDetails == null){
      registerError(_casMessageHelper.NO_CARRIER_VISIT_FOUND_CODE + ":" + _casMessageHelper.NO_CARRIER_VISIT_FOUND_MESSAGE + "[visitId=" +  visitId + "]");
      return getXmlErrorContent(_casMessageHelper.NO_CARRIER_VISIT_FOUND_CODE, _casMessageHelper.NO_CARRIER_VISIT_FOUND_MESSAGE + "[visitId=" +  visitId + "]");
    }
    boolean isVisitTypeVessel = VISIT_TYPE_VESSEL.equals(visitType);
    CarrierVisit cv = carrierVisitDetails.getCvdCv();
    Serviceable cvd = isVisitTypeVessel ? VesselVisitDetails.resolveVvdFromCv(cv) : TrainVisitDetails.resolveTvdFromCv(cv);
    if (CARRIER_GEOMETRY_REQUEST.equals(requestType)){
      AbstractBin binModel = (AbstractBin)carrierVisitDetails.getCarrierBinModel();
      if (binModel == null){
        registerError(_casMessageHelper.CARRIER_GEOMETRY_NOT_FOUND_CODE + ":" + _casMessageHelper.CARRIER_GEOMETRY_NOT_FOUND_MESSAGE + "[visitId=" +  visitId + "]");
        return getXmlErrorContent(_casMessageHelper.CARRIER_GEOMETRY_NOT_FOUND_CODE, _casMessageHelper.CARRIER_GEOMETRY_NOT_FOUND_MESSAGE + "[visitId=" +  visitId + "]");
      }
      Serializable[] primaryKeys = new Serializable[1];
      primaryKeys[0] = binModel.getAbnGkey();
      String binModelEntityName = (isVisitTypeVessel)? SHIP_BIN_MODEL_ENTITY_NAME : RAIL_BIN_MODEL_ENTITY_NAME;
      InputStream entityXmlStream = new EntityXmlStreamWithSimpleHeader(binModelEntityName, primaryKeys, SHIP_GEOMETRY, null);
      String shipGeometryContent = "\n" + entityXmlStream.getText();
      //An example of adding additional information
     /* additionalInfoMap.put("aField", "aFieldValue");*/
      returnXml = getXmlContent(additionalInfoMap, shipGeometryContent);
      if (!_messageCollector.hasError()){
        if (isVisitTypeVessel) {
          recordEvent(cvd, EventEnum.CAS_VV_REQUEST_GEOMETRY, null, null);
        } else {
          recordEvent(cvd, EventEnum.CAS_RV_REQUEST_GEOMETRY, null, null);
        }
      }
    }else if (CONTAINER_LIST_REQUEST.equals(requestType)){
      //Get the CasHelper instance
       _casHelper = getLibrary(CAS_HELPER);

      //Check the 'onBoard' and craneId parameters
      Boolean sendOnBoard = "Y".equals(inParameters.get(ON_BOARD_PARAM));
      String craneIds = inParameters.get(CRANE_ID_PARAM);

      Boolean sendOnBoardUnitUpdates = "Y".equals(inParameters.get(SEND_ON_BOARD_UNIT_UPDATES));
      Boolean sendCraneWorkListUpdates = "Y".equals(inParameters.get(SEND_CRANE_WORK_LIST_UPDATES));
      cv.setFieldValue(ArgoField.CV_SEND_ON_BOARD_UNIT_UPDATES, sendOnBoardUnitUpdates);
      cv.setFieldValue(ArgoField.CV_SEND_CRANE_WORK_LIST_UPDATES, sendCraneWorkListUpdates);
      Map<String, String> warningMap = new HashMap<String, String>();
      if (!sendOnBoard && StringUtils.isBlank(craneIds)){
        registerWarning(_casMessageHelper.CONTAINER_LIST_REQUEST_MISSING_PARAM_CODE + ":" + _casMessageHelper.CONTAINER_LIST_REQUEST_MISSING_PARAM_MESSAGE);
        warningMap.put(_casMessageHelper.CONTAINER_LIST_REQUEST_MISSING_PARAM_CODE, _casMessageHelper.CONTAINER_LIST_REQUEST_MISSING_PARAM_MESSAGE)
      }
      returnXml = createContainerListXmlContent(additionalInfoMap, sendOnBoard, craneIds, carrierVisitDetails, warningMap);
      if (!_messageCollector.hasError()){
        if (isVisitTypeVessel) {
          recordEvent(cvd, EventEnum.CAS_VV_REQUEST_CONTAINER_LIST, null, null);
        } else {
          recordEvent(cvd, EventEnum.CAS_RV_REQUEST_CONTAINER_LIST, null, null);
        }
      }
    }else{
      registerError(_casMessageHelper.INVALID_REQUEST_TYPE_CODE + ":" + _casMessageHelper.INVALID_REQUEST_TYPE_MESSAGE);
      return getXmlErrorContent(_casMessageHelper.INVALID_REQUEST_TYPE_CODE, _casMessageHelper.INVALID_REQUEST_TYPE_MESSAGE);
    }
    if (!_messageCollector.hasError()){
      log("\nRequest: " + getParametersAsString() + "\nResponse : Successfully processed response" )
      return returnXml;
    }else{
      log("\nRequest: " + getParametersAsString() + "\nResponse : Error while processing request" )
      return getXmlErrorContent(_casMessageHelper.BIZ_ERROR_CODE, _casMessageHelper.BIZ_ERROR_MESSAGE);
    }
  }
/**
 * Creates container list xml content
 * @param inAdditionalInfo additional info map sent as part of xml payload
 * @param inSendOnBoard true, if on board units sent to be the part of xml
 * @param inCraneIds crane ids for which the work lists need to be included as part of xml
 * @param inCarrierVisitDetails carrier visit details
 * @param inWarningMap warnings if any
 * @return xml content
 */
  private String createContainerListXmlContent(Map<String, String> inAdditionalInfo, Boolean inSendOnBoard,
                                               String inCraneIds, VisitDetails inCarrierVisitDetails, Map<String, String> inWarningMap){

    CarrierVisit carrierVisit = inCarrierVisitDetails.getCvdCv();
    List<PredicateIntf> unitOnBoardPredicateList = new ArrayList<PredicateIntf>();
    if (LocTypeEnum.VESSEL.equals(carrierVisit.getCvCarrierMode())) {
      unitOnBoardPredicateList.add(PredicateFactory.eq(UnitField.UFV_POS_LOC_TYPE, LocTypeEnum.VESSEL));
      unitOnBoardPredicateList.add(PredicateFactory.eq(UnitField.UFV_POS_LOC_GKEY, carrierVisit.getCvGkey()));
    } else if (LocTypeEnum.TRAIN.equals(carrierVisit.getCvCarrierMode())){
      unitOnBoardPredicateList.add(PredicateFactory.disjunction()
           .add(PredicateFactory.conjunction()
              /*  defines a unit on its inbound train */
              .add(TransitStateQueryUtil.ADVISE_OR_INBOUND_UFV_PREDICATE)
              .add(PredicateFactory.eq(UnitField.UFV_ACTUAL_IB_CV, carrierVisit.getCvGkey())))
           .add(PredicateFactory.conjunction()
              /*  defines a unit on its outbound train */
              .add(TransitStateQueryUtil.LOADED_OR_DEPARTED_UFV_PREDICATE)
              .add(PredicateFactory.eq(UnitField.UFV_ACTUAL_OB_CV, carrierVisit.getCvGkey()))));
    }

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
      if (inWarningMap != null && !inWarningMap.isEmpty()) {
        warnings(){
          inWarningMap.keySet().each {
            warning(code:it, message:inWarningMap.get(it));
          }
        }
      }
      if (inSendOnBoard) {
        getMkp().yieldUnescaped("\n" + _casHelper.createOnBoardUnitsXml(unitOnBoardPredicateList) + "\n");
      }
      if (!StringUtils.isBlank(inCraneIds)){
        boolean allCranes = "ALL".equalsIgnoreCase(inCraneIds);
        String craneIdsForQuery = allCranes ? null : inCraneIds;
        getMkp().yieldUnescaped("\n" + _casHelper.createCraneWorkListXmlContent(craneIdsForQuery, inCarrierVisitDetails, false) + "\n");

      }
    }
    String out = writer.toString();
    return out;
  }

/**
 * Creates payload xml to be returned in response to the request from CAS
 * @param inAdditionalInfo
 * @param inXml
 * @return
 */
  private String getXmlContent(Map<String, String> inAdditionalInfo, String inXml){
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
      getMkp().yieldUnescaped(inXml);
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
  /**
   * Creates xml to be returned in case of errors
   * @param inErrorCode error code
   * @param inErrorMessage error message
   * @return
   */
  private String getXmlErrorContent(String inErrorCode, String inErrorMessage)
  {
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
      error(code:inErrorCode, message:inErrorMessage) {
      }
    }
    String out = writer.toString();
    return out;
  }

}
