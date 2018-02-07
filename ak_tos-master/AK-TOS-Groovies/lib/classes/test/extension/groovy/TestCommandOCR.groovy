package test.extension.groovy

import com.navis.argo.ArgoEntity
import com.navis.argo.ArgoField
import com.navis.argo.ContextHelper
import com.navis.argo.business.api.VesselVisitFinder
import com.navis.argo.business.atoms.CheKindEnum
import com.navis.argo.business.atoms.WaMovePurposeEnum
import com.navis.argo.business.xps.model.Che
import com.navis.argo.business.xps.model.WorkAssignment
import com.navis.argo.test.ArgoTestUtil
import com.navis.argo.webservice.types.v1_0.ScopeCoordinateIdsWsType
import com.navis.framework.business.Roastery
import com.navis.framework.persistence.DatabaseHelper
import com.navis.framework.persistence.HibernateApi
import com.navis.framework.portal.QueryUtils
import com.navis.framework.portal.UserContext
import com.navis.framework.portal.query.DomainQuery
import com.navis.framework.portal.query.PredicateFactory
import com.navis.framework.util.BizFailure
import com.navis.inventory.business.api.SearchResults
import com.navis.inventory.business.api.UnitFinder
import com.navis.inventory.business.units.Unit
import com.navis.inventory.business.units.UnitFacilityVisit
import org.apache.commons.lang.StringEscapeUtils
import org.apache.commons.lang.StringUtils
import org.apache.log4j.Logger

/**
 * Created with IntelliJ IDEA.
 * User: rajansh
 * Date: 29/10/13
 * Time: 3:15 PM
 * To change this template use File | Settings | File Templates.
 */
class TestCommandOCR {

  private HibernateApi _hibernateApi;
  /** holds the result returned by each method */
  def String returnString = null;
  /** json builder, frames the output in json format */
  def builder = new groovy.json.JsonBuilder();
  private DatabaseHelper _teamsDbHelper;
  /** handler to invoke the fields in helper class */
  test.extension.groovy.TestCommandHelper _testCommandHelper = new test.extension.groovy.TestCommandHelper();

  //Logger for TestCommand
  public Logger LOGGER = Logger.getLogger(TestCommandOCR.class);

  /**
   * Generic API call to fecth data from N4 for different OCR Requests
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=OCRRequest<br>
   * requestType=containerListQuery/carrierGeometryRequest<br>
   * @return <code>Executed Query</code> if executed successfully<br>
   *         <code>Query not executed</code> failed to execute query
   * @Example
   * &lt;parameter id="command" value="OCRRequest" &gt;<br>
   * &lt;parameter id="requestType" value="containerListRequest/carrierGeometryRequest" &gt;<br>
   * &lt;parameter id="visitType" value="VESSEL" &gt;<br>
   * &lt;parameter id="visitId" value="ACT11954"&gt;<br>
   * &lt;parameter id="onBoard" value="Y/N"&gt;<br>
   * &lt;parameter id="craneId" value="C1,C2"&gt;<br>
   * &lt;parameter id="sendOnBoardUnitUpdates" value="Y/N"&gt;<br>
   * &lt;parameter id="sendCraneWorkListUpdates" value="Y/N"&gt;<br>
   * &lt;parameter id="operator" value="DPW" &gt;<br>
   * &lt;parameter id="complex" value="DPWR"&gt;<br>
   * &lt;parameter id="facility" value="RWG" &gt;<br>
   * &lt;parameter id="yard" value="RWG"&gt;<br>
   */
  public String OCRRequest(Map inParameters) {
    assert inParameters.size() >= 5, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply atleast first four parameters:
                                        <parameter id="command" value="OCRRequest" />
                                        <parameter id="className" value="className" />
                                        <parameter id="operator" value="Name of the operator" />
                                        <parameter id="complex" value="complex name" />
                                        <parameter id="facility" value="Name of the facility" />
                                        <parameter id="yard" value="Name of the yard" />
                                        <parameter id="requestType" value="containerListQuery/carrierGeometryRequest" />
                                        <parameter id="visitType" value="VESSEL" />
                                        <parameter id="visitId" value="ACT11954"/>
                                        <parameter id="onBoard" value="Y/N"/>
                                        <parameter id="craneId" value="C1,C2"/>
                                        <parameter id="sendOnBoardUnitUpdates" value="Y/N"/>
                                        <parameter id="sendCraneWorkListUpdates" value="Y/N"/>'''

    String inRequestType = inParameters.get('requestType');
    String inOperator = _testCommandHelper.checkParameter('operator', inParameters);
    String inComplex = _testCommandHelper.checkParameter('complex', inParameters);
    String inFacility = _testCommandHelper.checkParameter('facility', inParameters);
    String inYard = _testCommandHelper.checkParameter('yard', inParameters);
    String inVisitType = _testCommandHelper.checkParameter('visitType', inParameters);
    String inVisitId = ''
    if (inParameters.get('visitId')) {
      inVisitId = inParameters.get('visitId')
    }
    String inOnBoard = inParameters.get('onBoard');
    String inCraneId = inParameters.get('craneId');
    String inSendOnBoardUnitUpdates = inParameters.get('sendOnBoardUnitUpdates');
    String inSendCraneWorkListUpdates = inParameters.get('sendCraneWorkListUpdates');
    String inClassName = inParameters.get('className')
    try {
      if (inRequestType.equalsIgnoreCase("carrierGeometryRequest")) {
        //carrierGeometryRequest
        String groovyXml = "<groovy class-location=\"code-extension\" class-name=\"" + inClassName + "\">\n" +
                "   <parameters>\n" +
                "       <parameter id=\"requestType\" value=\"" + inRequestType + "\"/>\n" +
                "       <parameter id=\"visitType\" value=\"" + inVisitType + "\" />\n" +
                "       <parameter id=\"visitId\" value=\"" + inVisitId + "\" />\n" +
                "   </parameters>\n" +
                "</groovy>";
        String response = invokeGenericWebService(groovyXml, inOperator, inComplex, inFacility, inYard, 'OCRRequest');
        returnString = 'OCR Response received:' + response;
        if (response == null) {
          returnString = 'OCR Request failed'
        }
      } else if (inRequestType.equalsIgnoreCase("containerListRequest")) {      //containerListRequest
        String groovyXml = "<groovy class-location=\"code-extension\" class-name=\"" + inClassName + "\">\n" +
                "   <parameters>\n" +
                "       <parameter id=\"requestType\" value=\"" + inRequestType + "\"/>\n" +
                "       <parameter id=\"visitType\" value=\"" + inVisitType + "\" />\n" +
                "       <parameter id=\"visitId\" value=\"" + inVisitId + "\"/>\n" +
                "       <parameter id=\"onBoard\" value=\"" + inOnBoard + "\"/>\n" +
                "       <parameter id=\"sendOnBoardUnitUpdates\" value=\"" + inSendOnBoardUnitUpdates + "\"/>\n" +
                "       <parameter id=\"craneId\" value=\"" + inCraneId + "\"/>\n" +
                "       <parameter id=\"sendCraneWorkListUpdates\" value=\"" + inSendCraneWorkListUpdates + "\"/>\n" +
                "   </parameters>\n" +
                "</groovy>";
        String response = invokeGenericWebService(groovyXml, inOperator, inComplex, inFacility, inYard, 'OCRRequest');
        returnString = 'OCR Response received:' + response;
        if (response == null) {
          returnString = 'OCR Request failed'
        }
      } else {
        String groovyXml = "<groovy class-location=\"code-extension\" class-name=\"" + inClassName + "\">\n" +
                "   <parameters>\n" +
                "       <parameter id=\"requestType\" value=\"" + inRequestType + "\"/>\n" +
                "       <parameter id=\"visitType\" value=\"" + inVisitType + "\" />\n" +
                "       <parameter id=\"visitId\" value=\"" + inVisitId + "\"/>\n" +
                "       <parameter id=\"onBoard\" value=\"" + inOnBoard + "\"/>\n" +
                "   </parameters>\n" +
                "</groovy>";
        String response = invokeGenericWebService(groovyXml, inOperator, inComplex, inFacility, inYard, 'OCRRequest');
        returnString = 'OCR Response received:' + response;
        if (response == null) {
          returnString = 'OCR Request failed'
        }
      }
    } catch (Exception ex) {
      returnString = 'Query not executed' + ex;
    }
    builder {
      actual_result returnString;
    }
    LOGGER.debug('OCRRequest:' + returnString)
    return builder;
  }

  /**
   * Sends the xml file through Webservices to N4
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=OCRUnitPositionUpdate<br>
   * OCRFile=XML file
   * @return JSON ,
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="OCRUnitPositionUpdate"/&gt;<br>
   * &lt;parameter id="OCRFile" value="file.xml"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String OCRUnitPositionUpdate(Map inParameters) {
    assert inParameters.size() >= 7, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 7 parameters:
                                        <parameter id="command" value="OCRUnitPositionUpdate" />
                                        <parameter id="requestType" value="<unitPositionUpdateMessage>" />
                                        <parameter id="visitType" value="<Visit Type VESSEL or TRAIN>" />
                                        <parameter id="visitId" value="<Visit Id>" />
                                        <parameter id="craneId" value="<Crane Id>" />
                                        <parameter name='action' value= <'Lift' or 'Set'>
                                        <parameter id="unitXml" value="<unitXml>" />
                                        <parameter id="operator" value="Name of the operator" />
                                        <parameter id="complex" value="complex name" />
                                        <parameter id="facility" value="Name of the facility" />
                                        <parameter id="yard" value="Name of the yard" />'''
    CasInUnit[] _casInUnits;
    String requestType = _testCommandHelper.checkParameter('requestType', inParameters);
    String visitType = _testCommandHelper.checkParameter('visitType', inParameters);
    String visitId = _testCommandHelper.checkParameter('visitId', inParameters);
    String craneId = _testCommandHelper.checkParameter('craneId', inParameters);
    String action = _testCommandHelper.checkParameter('action', inParameters);
    String unitXml = _testCommandHelper.checkParameter('unitXml', inParameters);
    String inOperator = _testCommandHelper.checkParameter('operator', inParameters);
    String inComplex = _testCommandHelper.checkParameter('complex', inParameters);
    String inFacility = _testCommandHelper.checkParameter('facility', inParameters);
    String inYard = _testCommandHelper.checkParameter('yard', inParameters);
    final String MISSING_UNIT_XML_ERROR = "No Unit Xml was sent as part of message"
    final String INVALID_UNIT_XML_ERROR = "The Unit Xml sent as part of the message is invalid"
    UserContext userContext = ContextHelper.getThreadUserContext();
    ScopeCoordinateIdsWsType scopeCoordinateIdsWsType = ArgoTestUtil.getScopeCoordinatesWSType(inOperator, inComplex, inFacility, inYard);
    String resultXml = null;
    String responseMessages = '';
    try {
      if (StringUtils.isBlank(unitXml)) {
        returnString = MISSING_UNIT_XML_ERROR;
      }
      Node unitsNode = null;
      try {

        unitsNode = new XmlParser().parseText(StringEscapeUtils.unescapeXml(unitXml));
      } catch (Exception ex) {
        returnString = INVALID_UNIT_XML_ERROR;
      }
      def units = unitsNode.'unit'
      int _unitCount = units.size()
      if (!"units".equals(unitsNode.name()) || _unitCount == 0) {
        returnString = INVALID_UNIT_XML_ERROR;
      }
      int i = 0
      _casInUnits = new CasInUnit[_unitCount];
      units.each { Node unitNode ->
        _casInUnits[i] = new CasInUnit(unitNode)
        initializeCasUnit(_casInUnits[i]);
        String unitId = _casInUnits[i]._unitId;
        String casTranRef = _casInUnits[i]._casTransactionReference;
        String casUnitRef = _casInUnits[i]._casUnitReference;
        SearchResults results = getUnitFinder().findUfvByDigits(unitId, false, false);
        if (null == results || results.foundCount == 0) {
          throw BizFailure.create("No Unit found with given id: " + unitId);
        }
        i++;

        String xmlString = getUnitPositionUpdateXmlPayload(visitId, casUnitRef, casTranRef, craneId, action, unitId, visitType);
        getSession()
        resultXml = invokeGenericWebService(xmlString, inOperator, inComplex, inFacility, inYard, 'OCRUnitPositionUpdate');
        int unitRespStart = 0;
        int unitRespEnd = 0;
        if (resultXml.indexOf("unit-response") != -1) {
          unitRespStart = resultXml.indexOf("unit-response") - 1;
        }
        if (resultXml.lastIndexOf("unit-response") != -1) {
          unitRespEnd = resultXml.lastIndexOf("unit-response") + 14;
        }
        if (unitRespStart > 0 && unitRespEnd > 0) {
          resultXml = resultXml.substring(unitRespStart, unitRespEnd);
        }
        Node unitRespNode = new XmlParser().parseText(StringEscapeUtils.unescapeXml(resultXml));
        if (!"unit-response".equals(unitRespNode.name())) {
          returnString = INVALID_UNIT_XML_ERROR;
        }
        String unit_id = unitRespNode."@id"

        NodeList messageNodeList = (NodeList) unitRespNode.get("message");
        Node messageNode = (Node) messageNodeList.get(0);
        String message = messageNode."@text"
        String unit_ws_response = 'Cas response for the unit ' + unit_id + ': ' + message;
        responseMessages = responseMessages + unit_ws_response;

        returnString = responseMessages;
      }
      returnString = 'Unit Position Update successful';
    } catch (Exception inEx) {
      returnString = 'Unit Position Update failed for the given request ' + inEx;
    }
    builder {
      actual_result returnString;
    }
    LOGGER.debug('OCRUnitPositionUpdate:' + returnString)
    return builder;
  }

  /**
   * Returns Unit identified
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=OCRUnitIdentify<br>
   * OCRFile=XML file
   * @return JSON ,
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="OCRUnitIdentify"/&gt;<br>
   * &lt;parameter id="OCRFile" value="file.xml"/&gt;<br>
   * &lt;parameter id="visitType" value="<Vessel or train>"/&gt;<br>
   * &lt;parameter id="visitId" value="<visit id>"/&gt;<br>
   * &lt;parameter id="craneId" value="<crane Id>" &gt;<br>
   * &lt;parameter id="action" value="Image , Identify, Create"/&gt;<br>
   * &lt;parameter id="unitXml" value="xml " &gt;<br>
   * &lt;parameter id="operator" value="DPW" &gt;<br>
   * &lt;parameter id="complex" value="DPWR"&gt;<br>
   * &lt;parameter id="facility" value="RWG" &gt;<br>
   * &lt;parameter id="yard" value="RWG"&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String OCRUnitIdentify(Map inParameters) {
    assert inParameters.size() >= 7, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 7 parameters:
                                        <parameter id="command" value="OCRUnitIdentify" />
                                        <parameter id="requestType" value="<UnitCaptureMessage>" />
                                        <parameter id="visitType" value="<Visit Type VESSEL or TRAIN>" />
                                        <parameter id="visitId" value="<Visit Id>" />
                                        <parameter id="craneId" value="<Crane Id>" />
                                        <parameter id="action" value="<Action - Create Update Image or Identify>" />
                                        <parameter id="unitXml" value="<unitXml>" />
                                        <parameter id="operator" value="Name of the operator" />
                                        <parameter id="complex" value="complex name" />
                                        <parameter id="facility" value="Name of the facility" />
                                        <parameter id="yard" value="Name of the yard" />
                                        <parameter id="formXML" value="True (Make it as true If OCR xml needs to be formed using the given params)" />
                                        <parameter id="isoCode" value="to indicate it is 20' or 40'" />
                                        <parameter id="length" value="length of the container" />
                                        <parameter id="height" value="height of the container" />
                                        <parameter id="width" value="width of the container" />
                                        <parameter id="tankRailType" value="tank rail type" />
                                        <parameter id="currentPosition" value="current position of the container" />
                                        <parameter id="tankRailType" value="tank rail type" />
                                        <parameter id="currentPosition" value="current position of the container" />'''
    CasInUnit[] _casInUnits;
    String command = _testCommandHelper.checkParameter('command', inParameters);
    //2013-08-02 oviyak ARGO-50241 Fixed to display proper error messages for crane ID etc by making these fields optional
    String requestType = inParameters.get('requestType', inParameters);
    String visitType = inParameters.get('visitType', inParameters);
    //2013-07-16 oviyak 2.6.H ARGO-49070 Modified Visit Id as optional field
    String visitId = inParameters.get('visitId');
    String craneId = inParameters.get('craneId', inParameters);
    String action = inParameters.get('action', inParameters);
    String unitXml = inParameters.get('unitXml')
    String inOperator = _testCommandHelper.checkParameter('operator', inParameters);
    String inComplex = _testCommandHelper.checkParameter('complex', inParameters);
    String inFacility = _testCommandHelper.checkParameter('facility', inParameters);
    String inYard = _testCommandHelper.checkParameter('yard', inParameters);
    String inFormXML = '', xmlString = ''
    final String INVALID_UNIT_XML_ERROR = "The Unit Xml sent as part of the message is invalid"
    if (inParameters.containsKey('formXML')) {
      inFormXML = inParameters.get('formXML')
    };
    //set ocr identity true as per new changes done by Arvinder
    Che che = ((Che) HibernateApi.getInstance().getUniqueEntityByDomainQuery(QueryUtils.createDomainQuery(ArgoEntity.CHE)
            .addDqPredicate(PredicateFactory.eq(ArgoField.CHE_SHORT_NAME, craneId))));
    if(che != null) {
    if (CheKindEnum.QC == che.getCheKindEnum()){
      che.setCheIsOcrDataBeingAccepted(true);
      LOGGER.debug('OCRUnitIdentify - setCheIsOcrDataBeingAccepted is set to true for QC :' + craneId )
    }
    }
    String resultXml = null;
    String responseMessages = '', messageCode = '', finalMsgCode = '';
    //If formXML is requested for TEAMS, then form it with the given values
    try {
      if (!inFormXML.isEmpty()) {
        String unitXMLStr = formXMLForTEAMS(inParameters) //doing the XML forming process separately
        getSession()
        if (unitXMLStr.contains('TEAMS')) {
          returnString = unitXMLStr
        } else {
          resultXml = invokeGenericWebService(unitXMLStr, inOperator, inComplex, inFacility, inYard, 'OCRUnitIdentify')
        };
      } else {
        final String MISSING_UNIT_XML_ERROR = "No Unit Xml was sent as part of message"

        UserContext userContext = ContextHelper.getThreadUserContext();
        //ScopeCoordinateIdsWsType scopeCoordinateIdsWsType = getScopeCoordinateIdsWsForCurrentUser();
        com.navis.argo.webservice.types.v1_0.ScopeCoordinateIdsWsType scopeCoordinateIdsWsType = ArgoTestUtil.getScopeCoordinatesWSType(inOperator, inComplex, inFacility, inYard);
        if (StringUtils.isBlank(unitXml)) {
          returnString = MISSING_UNIT_XML_ERROR;
        }
        Node unitsNode = null;
        try {
          unitsNode = new XmlParser().parseText(StringEscapeUtils.unescapeXml(unitXml));
        } catch (Exception ex) {
          returnString = INVALID_UNIT_XML_ERROR;
        }
        def units = unitsNode.'unit'
        int _unitCount = units.size()
        if (!"units".equals(unitsNode.name()) || _unitCount == 0) {
          returnString = INVALID_UNIT_XML_ERROR;
        }
        int i = 0
        _casInUnits = new CasInUnit[_unitCount];
        units.each { Node unitNode ->
          _casInUnits[i] = new CasInUnit(unitNode)
          initializeCasUnit(_casInUnits[i]);
          String unitId = _casInUnits[i]._unitId;
          String casTranRef = _casInUnits[i]._casTransactionReference;
          String casUnitRef = _casInUnits[i]._casUnitReference;
          if ("Identify".equalsIgnoreCase(action) || "Update".equalsIgnoreCase(action)) {
            SearchResults results = getUnitFinder().findUfvByDigits(unitId, false, false);
            if (null == results || results.foundCount == 0) {
              throw BizFailure.create("No Unit found with given id: " + unitId);
            }
          }
          i++;
          if (xmlString.isEmpty()) {
            //if formXML was not forced, then give the default XML to OCR capture
            if (action.equalsIgnoreCase('Create'))
            //2013-08-02 oviyak ARGO-50244 To validate the request type specified by the user
            {
              xmlString = getUnitCaptureXmlPayload(visitId, casUnitRef, casTranRef, craneId, requestType, action, unitId, visitType, unitXml);
              i = _unitCount; // as units are created by passing the unitXML the next run is not required. "i" is used for next iteration
            } else {
              xmlString = getUnitCaptureXmlPayload(visitId, casUnitRef, casTranRef, craneId, requestType, action, unitId, visitType, unitXml)
            };
          }
          getSession()
          resultXml = invokeGenericWebService(xmlString, inOperator, inComplex, inFacility, inYard, 'OCRUnitIdentify');
          println 'resultxml:' + resultXml
          //2013-07-19 oviyak 2.6.H ARGO-48590 & ARGO-49778 Fixed problem when multiple containers are posted using OCRUnitIdentify API
          xmlString = '';
        }
      }

      int unitRespStart = 0;
      int unitRespEnd = 0;
      String finalResult = "";
      if (resultXml != null) {
        if (resultXml.contains('error')) {
          responseMessages = resultXml
        } else {
          int count = resultXml.count("unit-response");
          int startIndex = 0;
          for (int index = 0; index < count / 2; index++) {
            if (resultXml.indexOf("unit-response") != -1) {
              unitRespStart = resultXml.indexOf("unit-response", startIndex);
            }
            if (resultXml.indexOf("unit-response", unitRespStart + 1) != -1) {
              unitRespEnd = resultXml.indexOf("unit-response", unitRespStart + 1)
            }
            finalResult = resultXml.substring(unitRespStart - 1, unitRespEnd + 14);
            startIndex = unitRespEnd + 1;

            Node unitRespNode = new XmlParser().parseText(StringEscapeUtils.unescapeXml(finalResult));
            if (!"unit-response".equals(unitRespNode.name())) {
              returnString = INVALID_UNIT_XML_ERROR;
            }
            String unit_id = unitRespNode."@id"

            NodeList messageNodeList = (NodeList) unitRespNode.get("message");
            Node messageNode = (Node) messageNodeList.get(0);
            String message = messageNode."@text"
            messageCode = messageNode."@code"
            String unit_ws_response = 'Cas response for the unit ' + unit_id + ': ' + message;
            if (count > 1 && index >= 1) {
              responseMessages = responseMessages + '; ';
            }
            //2013-08-02 oviyak ARGO-48590 To capture mesage code for multiple containers
            if (finalMsgCode.isEmpty()) {
              finalMsgCode = messageCode;
            } else {
              finalMsgCode = finalMsgCode + '; ' + messageCode;
            }
            responseMessages = responseMessages + unit_ws_response;
          }
        }
        returnString = responseMessages;
      } else {
        if (returnString.isEmpty()) {
          returnString = 'Response is null'
        }
      }
    } catch (Exception inEx) {
      returnString = 'Unit capture failed for the given request ' + inEx;
    }
    builder {
      actual_result returnString;
      if (!messageCode.isEmpty()) {
        data('messageCode': finalMsgCode)
      }
    }
    LOGGER.debug('OCRUnitIdentify:' + returnString)
    return builder;
  }


  public static class LloydsIdProvider extends com.navis.argo.business.model.ArgoSequenceProvider {
    /**
     * returns next lloyds id that is unqiue for the current thread complex.
     */
    public Long getNextLloydsId() {
      return super.getNextSeqValue(_extractLloydsIdSeq, (Long) ContextHelper.getThreadComplexKey());
    }

    private String _extractLloydsIdSeq = "lLOYDS_SEQUENCE";
  }

  public static class VoyageIdProvider extends com.navis.argo.business.model.ArgoSequenceProvider {
    /**
     * returns next voyage id that is unqiue for the current thread complex.
     */
    public Long getNextVoyageId() {
      return super.getNextSeqValue(_extractVoyageIdSeq, (Long) ContextHelper.getThreadComplexKey());
    }

    private String _extractVoyageIdSeq = "VOYAGE_SEQUENCE";
  }

  public static VesselVisitFinder getVvFinder() {
    return (VesselVisitFinder) Roastery.getBean(VesselVisitFinder.BEAN_ID);
  }

  public static UnitFinder getUnitFinder() {
    return (UnitFinder) Roastery.getBean(UnitFinder.BEAN_ID);
  }

  public ScopeCoordinateIdsWsType getScopeCoordinateIdsWsForCurrentUser() {
    String oprId = null;
    String cpxId = null;
    String fcyId = null;
    String yrdId = null;
    if (ContextHelper.getThreadOperator() != null) {
      oprId = ContextHelper.getThreadOperator().getId()
    };
    if (ContextHelper.getThreadComplex() != null) {
      cpxId = ContextHelper.getThreadComplex().getId()
    };
    if (ContextHelper.getThreadFacility() != null) {
      fcyId = ContextHelper.getThreadFacility().getId()
    };
    if (ContextHelper.getThreadYard() != null) {
      yrdId = ContextHelper.getThreadYard().getId()
    };
    ScopeCoordinateIdsWsType scopeCoordinatesWSType = ArgoTestUtil.getScopeCoordinatesWSType(oprId, cpxId, fcyId, yrdId);
    return scopeCoordinatesWSType;
  }

  /**
   * This class encapsulates the unit information coming from the CAS, it also holds any error information added during validation and unit facility
   * visit information id validation is successful
   */
  public class CasInUnit {

    CasInUnit(Node inUnitNode) {
      _unitNode = inUnitNode
      _casUnitReference = _unitNode."@cas-unit-reference"
      _casTransactionReference = _unitNode."@cas-transaction-reference"
      _unitId = _unitNode."@id"
      _returnStatus = Status.SUCCESS
      _returnMessage = "Success: Unit successfully processed"
    }

    private String _casUnitReference
    private String _casTransactionReference
    private String _unitId
    private UnitFacilityVisit _unitFacilityVisit
    private Unit _unit
    //Message which will be returned in the response for this unit
    private String _returnMessage
    private Status _returnStatus
    private Node _unitNode
    //A generic fields for any additional attributes which may be used by subclasses
    private Map<String, Object> _attributeMap = new HashMap<String, Object>();

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

    public String getStatusAsString() {
      switch (_returnStatus) {
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
  }

  /**
   * A hook for subclasses to add additional parameters/attributes to the attribute map of CasInUnit. This method is called immediately after the
   * CasInUnit is constructed
   * @param inCasInUnit newly created CasInUnit
   */
  public void initializeCasUnit(CasInUnit inCasInUnit) {

  }

  public enum Status {
    SUCCESS, ERROR, WARNING
  }

  private String invokeGenericWebService(String inXmlString, String inOperator, String inComplex, String inFacility, String inYard, String scenario) {
    LOGGER.debug('Executing scenario : ' + scenario)
    if (scenario.equalsIgnoreCase('GateOperations')) {
      //closing the session explicitly for Gate Operations as we get this below message
      //'Session or transaction parameters are already bound from a previous request. Caller must close all previous sessions and transactions'.
      _hibernateApi = HibernateApi.getInstance().closeSession(true);
    } else if (scenario.contains('OCR')) {
      //OCR doesnt require session creation explicitly, adding this just in case if session was closed by some other caller.This will create
      //session only when session is not available
      HibernateApi.getInstance().createSessionIfNecessary()
    }
    UserContext userContext = ContextHelper.getThreadUserContext();

    com.navis.argo.webservice.IArgoWebService ws = new com.navis.argo.webservice.ArgoWebServicesFacade(userContext);
    com.navis.argo.webservice.types.v1_0.ScopeCoordinateIdsWsType scopeCoordinatesWSType = ArgoTestUtil.getScopeCoordinatesWSType(inOperator, inComplex, inFacility, inYard);
    com.navis.argo.webservice.types.v1_0.GenericInvokeResponseWsType invokeResponseWsType = ws.genericInvoke(scopeCoordinatesWSType, inXmlString);
    com.navis.argo.webservice.types.v1_0.ResponseType commonResponse = invokeResponseWsType.getCommonResponse();
    com.navis.argo.webservice.types.v1_0.MessageCollectorType messageCollectorType = commonResponse.getMessageCollector();
    final com.navis.argo.webservice.types.v1_0.MessageType[] messages = messageCollectorType.getMessages();
    boolean isError = false;
    String errorText = "Error(s):";
    String result = "";
    if (messages != null) {
      for (int i = 0; i < messages.length; i++) {
        com.navis.argo.webservice.types.v1_0.MessageType message = messages[i];
        String msgSeverity = message.getSeverityLevel();
        String msgText = message.getMessage();
        if ("SEVERE".equalsIgnoreCase(msgSeverity)) {
          isError = true;
          errorText = errorText + msgText;
        } else if ("INFO".equalsIgnoreCase(msgSeverity)) {
          if (msgText.startsWith("Result")) {
            result = result + msgText
          }
        }
      }
      if (isError) {
        return errorText;
      }
    }
    com.navis.argo.webservice.types.v1_0.QueryResultType[] queryResults = commonResponse.getQueryResults();

    com.navis.argo.webservice.types.v1_0.QueryResultType queryResult = queryResults[0];
    if (scenario.contains('OCRUnitIdentify')) {
      return result;
    }
    return queryResult.getResult();
  }

  /**
   * Creates the truck visit for the given truck Id or returns the truck visit if already exists.
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=GateOperations<br>
   * @return JSON ,
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="GateOperations"/&gt;<br>
   * &lt;parameter id="gateRequest" value="<XML for the gate operation request"/&gt;<br>
   * &lt;parameter id="operator" value="DPW" &gt;<br>
   * &lt;parameter id="complex" value="DPWR"&gt;<br>
   * &lt;parameter id="facility" value="RWG" &gt;<br>
   * &lt;parameter id="yard" value="RWG"&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String GateOperations(Map inParameters) {
    assert inParameters.size() >= 2, '''Must supply 2 parameters:
                                          <parameter id="command" value="GateOperations" />
                                          <parameter id="gateRequest" value="Request XML " />
                                          <parameter id="operator" value="Name of the operator" />
                                          <parameter id="complex" value="complex name" />
                                          <parameter id="facility" value="Name of the facility" />
                                          <parameter id="yard" value="Name of the yard" />'''
    String inGateRequest = _testCommandHelper.checkParameter("gateRequest", inParameters);
    String dataKey = "";
    String dataValue = "";
    String result = "";
    String tranGkey = "0";
    String nextStageOut = "";
    String statusOut = "";
    String transactionAppointment = "";
    String truckVisitAppointment = "";
    String inOperator = _testCommandHelper.checkParameter('operator', inParameters);
    String inComplex = _testCommandHelper.checkParameter('complex', inParameters);
    String inFacility = _testCommandHelper.checkParameter('facility', inParameters);
    String inYard = _testCommandHelper.checkParameter('yard', inParameters);

    if (inGateRequest.contains("&lt;")) {
      inGateRequest = inGateRequest.replaceAll("&lt;", "<")
    }
    if (inGateRequest.contains("&gt;")) {
      inGateRequest = inGateRequest.replaceAll("&gt;", ">")
    }
    if (inGateRequest.contains("&quot;")) {
      inGateRequest = inGateRequest.replaceAll("&quot;", "\"")
    }

    String response = invokeGenericWebService(inGateRequest, inOperator, inComplex, inFacility, inYard, 'GateOperations');
    if (response != null && !response.contains('Error(s)')) {
      def rootNode = new XmlSlurper().parseText(response)
      if (response.contains("create-truck-visit-response")) {  //create-truck-visit gateRequest
        def nextStage = rootNode."create-truck-visit-response"[0]."truck-visit"[0].@"next-stage-id"
        nextStageOut = nextStage.text()
        def status = rootNode."create-truck-visit-response"[0]."truck-visit"[0].@"status"
        statusOut = status.text()
        //Returned the truck visit key in the data
        def tvKey = rootNode."create-truck-visit-response"[0]."truck-visit"[0].@"tv-key".text()
        dataKey = "TvKey";dataValue = tvKey;
        result = "Create-truck-visit-response -- Next-Stage-Id : " + nextStageOut + "; Status :" + statusOut;
      } else if (response.contains("submit-multiple-transactions-response")) {  //submit-multiple-transactions gateRequest
        def tranKey = rootNode."submit-multiple-transactions-response"[0]."truck-transactions"[0]."truck-transaction"[0].@"tran-key"
        tranGkey = tranKey.text()
        def nextStage = rootNode."submit-multiple-transactions-response"[0]."truck-transactions"[0]."truck-transaction"[0].@"next-stage-id"
        nextStageOut = nextStage.text()
        def status = rootNode."submit-multiple-transactions-response"[0]."truck-transactions"[0]."truck-transaction"[0].@"status"
        statusOut = status.text()
        dataKey = "tranGkey";dataValue = tranGkey;
        result = "Submit-multiple-transactions-response -- Next-Stage-Id : " + nextStageOut + "; Status :" + statusOut;
      } else if (response.contains("stage-done-response")) { //stage-done gateRequest
        def nextStage = rootNode."stage-done-response"[0]."truck-visit"[0].@"next-stage-id"
        nextStageOut = nextStage.text()
        def status = rootNode."stage-done-response"[0]."truck-visit"[0].@"status"
        statusOut = status.text()
        result = "Stage-done-response -- Next-Stage-Id : " + nextStageOut + "; Status :" + statusOut;
      } else if (response.contains("notify-arrival-response")) { //notify-arrival gateRequest
        def nextStage = rootNode."notify-arrival-response"[0]."truck-visit"[0].@"next-stage-id"
        nextStageOut = nextStage.text()
        def status = rootNode."notify-arrival-response"[0]."truck-visit"[0].@"status"
        statusOut = status.text()
        result = "Notify-arrival-response -- Next-Stage-Id : " + nextStageOut + "; Status :" + statusOut;
      } else if (response.contains("process-truck-response")) { //process-truck gateRequest
        def status = rootNode."process-truck-response"[0]."truck-visit"[0].@"status"
        statusOut = status.text()
        result = "Process-truck-response -- Status :" + statusOut;
      } else if(response.contains("create-appointment-response")){ //appointment gateRequest
        transactionAppointment = rootNode."create-appointment-response"[0]."appointment-nbr"[0].text()
        dataKey = "TransactionAppointment";dataValue = transactionAppointment;
        result = "Transaction appointment created";
      } else if(response.contains("create-truck-visit-appointment-response")){ //truck visit appointment gateRequest
        truckVisitAppointment = rootNode."create-truck-visit-appointment-response"[0]."appointment-nbr"[0].text()
        dataKey = "TruckVisitAppointment";dataValue = truckVisitAppointment ;
        result = "Truck visit appointment created";
      } else if(response.contains("submit-transaction-response")){ //submit transaction gateRequest
        tranGkey = rootNode."submit-transaction-response"[0]."truck-transactions"[0]."truck-transaction"[0].@"tran-key".text()
        def nextStage = rootNode."submit-transaction-response"[0]."truck-transactions"[0]."truck-transaction"[0].@"next-stage-id"
        nextStageOut = nextStage.text()
        def status = rootNode."submit-transaction-response"[0]."truck-transactions"[0]."truck-transaction"[0].@"status"
        statusOut = status.text()
        dataKey = "tranGkey";dataValue = tranGkey;
        result = "Submit-transaction-response -- Next-Stage-Id : " + nextStageOut + "; Status :" + statusOut;
      }
      if (!result.isEmpty()) {
        returnString = 'Response received:' + result;

      } else {
        returnString = 'Response received:' + response;
      }
    } else {
      returnString = 'Response received:' + response ;
    }

    builder {
      actual_result returnString;
      //ARGO-81394 data values not retrieved properly, changed ',' to ':' while forming data key and value pair in output
      data("$dataKey":dataValue);
    }
    LOGGER.debug('GateOperations:' + returnString)
    return builder;
  }

  /**
   * Gets the response of the http request and return it as a string
   * @param httpRequest
   */
  private String handleHTTPRequests(String ip, int port, String userName, String pwd, String filterName, String operatorId, String complexId, String facilityId, String yardId) {
    String responseString = "";
    try {
      String httpRequest = "http://" + ip + ":" + port + "/apex/api/query?filtername=" + filterName + "&operatorId=" + operatorId + "&complexId=" + complexId + "&facilityId=" + facilityId + "&yardId=" + yardId
      //def addr = "http://localhost:8280/apex/api/query?filtername=IN_AGW_YARD&operatorId=DPW&complexId=DPWA&facilityId=AGW&yardId=AGW";
      def authString = userName + ":" + pwd   //form the authString using username,password
      String authStringEnc = authString.getBytes().encodeBase64().toString();  //encrypt the password before sending it
      URLConnection conn = httpRequest.toURL().openConnection()
      conn.setRequestProperty("Authorization", "Basic ${authStringEnc}")
      InputStream is = conn.getInputStream();    // get the stream output from http response
      InputStreamReader isr;
      StringBuffer sb
      if (is != null) {
        isr = new InputStreamReader(is)
      } else {
        responseString = 'Response not obtained, request may be invalid'
        return responseString
      }
      int numCharsRead;
      char[] charArray = new char[1024];
      if (isr != null) {
        sb = new StringBuffer();   //read the response and display
        while ((numCharsRead = isr.read(charArray)) > 0) {
          sb.append(charArray, 0, numCharsRead);
        }
      } else {
        responseString = 'Response not obtained, request may be invalid'
        return responseString
      }
      responseString = sb.toString();
    } catch (Exception ex) {
      responseString = 'Failed to get the response:' + ex
      return responseString
    }
    return responseString
  }

  //Returns the request xml to be sent for OCRUnitIdentify
  private String getUnitCaptureXmlPayload(String inVisitId, String inCasUnitReferenceId, String inCasTranReferenceId,
                                          String inCraneId, String inRequestType, String inAction,
                                          String inUnitId, String inVisitType, String unitXML) {
    if (unitXML.isEmpty()) {
      unitXML = "&lt;units&gt;&lt;unit cas-unit-reference=&apos;" + inCasUnitReferenceId +
              "&apos; cas-transaction-reference=&apos;" + inCasTranReferenceId +
              "&apos; id=&apos;" + inUnitId + "&apos; /&gt;&lt;/units&gt;"
    } else {
      if (unitXML.contains("<")) {
        unitXML = unitXML.replaceAll("<", "&lt;")
      }
      if (unitXML.contains(">")) {
        unitXML = unitXML.replaceAll(">", "&gt;")
      }
      if (unitXML.contains("\"")) {
        unitXML = unitXML.replaceAll("\"", "&quot;")
      }
      if (unitXML.contains("'")) {
        unitXML = unitXML.replaceAll("'", "&apos;")
      }
    }
    if (inRequestType.equalsIgnoreCase('UnitCaptureMessage')) {
      inRequestType = 'unitCaptureMessage'
    }
    return "<groovy class-location='code-extension' class-name='DefaultN4InboundCasMessageHandler'> \n" +
            "  <parameters> \n" +
            "  <parameter id='requestType' value='" + inRequestType + "'/> \n" +
            "  <parameter id='visitType' value='" + inVisitType + "'/> \n" +
            "  <parameter id='visitId' value='" + inVisitId + "'/> \n" +
            "  <parameter id='craneId' value='" + inCraneId + "'/> \n" +
            "  <parameter id='action' value='" + inAction + "'/> \n" +
            "  <parameter id='unitXml' value='" + unitXML + "'/> \n" +
            "  </parameters> \n" +
            "  </groovy>";
  }
  //Returns the request xml to be sent for OCRUnitPositionUpdate
  private String getUnitPositionUpdateXmlPayload(String inVisitId, String inCasUnitReferenceId, String inCasTranReferenceId,
                                                 String inCraneId, String inAction,
                                                 String inUnitId, String inVisitType) {
    return "<groovy class-location='code-extension' class-name='DefaultN4InboundCasMessageHandler'> \n" +
            "  <parameters> \n" +
            "  <parameter id='requestType' value=\"unitPositionUpdateMessage\"/> \n" +
            "  <parameter id='visitType' value='" + inVisitType + "'/> \n" +
            "  <parameter id='visitId' value='" + inVisitId + "'/> \n" +
            "  <parameter id='craneId' value='" + inCraneId + "'/> \n" +
            "  <parameter id='action' value='" + inAction + "'/> \n" +
            "  <parameter id='unitXml' value='&lt;units&gt;&lt;unit cas-unit-reference=&apos;" + inCasUnitReferenceId + "&apos; " +
            "  cas-transaction-reference=&apos;" + inCasTranReferenceId + "&apos; " +
            "   id=&apos;" + inUnitId + "&apos; " +
            "   &gt; \n" +
            "&lt;current-position loc-type=&apos;" + inVisitType + "&apos; location=&apos;A11234&apos; slot=&apos;11234&apos; /&gt; \n " +
            "&lt;/unit&gt;/> \n" +
            "&lt;/units&gt;'/> \n" +
            "  </parameters> \n" +
            "  </groovy>";
  }

  private void getSession() {
    _hibernateApi = HibernateApi.getInstance()
  }

  private String formXMLForTEAMS(HashMap inParams) {
    String inUnitId = '', inCurrentPosition = '', inQCId = ''
    String inIsoCode = '2200', inLength = '12193', inHeight = '2592', inWidth = '2439', inTankRailType = 'BOTH'
    inQCId = inParams.get('craneId')
    if (inParams.containsKey('unitId')) {
      inUnitId = inParams.get('unitId')
    };
    if (inParams.containsKey('isoCode')) {
      inIsoCode = inParams.get('isoCode')
    };
    if (inParams.containsKey('length')) {
      inLength = inParams.get('length')
    };
    if (inParams.containsKey('height')) {
      inHeight = inParams.get('height')
    };
    if (inParams.containsKey('width')) {
      inWidth = inParams.get('width')
    };
    if (inParams.containsKey('currentPosition')) {
      inCurrentPosition = inParams.get('currentPosition')
    };
    if (inParams.containsKey('tankRailType')) {
      inTankRailType = inParams.get('tankRailType')
    };
    String inVisitType = inParams.get('visitType')
    String inVisitId = inParams.get('visitId')
    String inAction = inParams.get('action')
    String reference = '', result = '', ocrXML = ''
    try {
      //Check current position not empty to get the relevant reference for QC_Commands to do OCR
      if (!inCurrentPosition.isEmpty()) {
        String queryreference = "select REFERENCE_ID from qc_commands where qc_id = '" + inQCId + "' and CONTAINER_CURR_LOCATION='" + inCurrentPosition + "'"
        List qr = _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).queryForList(
                String.format(queryreference)
        );
        if (qr.size() > 0) {
          reference = qr.get(0).toString().substring(qr.get(0).toString().indexOf(':') + 1, qr.get(0).toString().lastIndexOf(']'))
        } else { //if current position is not spreader
          /**
           * Check whether its a load move, because TEAMS recently changed the behavior of completing Load Jobs according to different customer reqmts
           * Load move gets completed even before OCRUnitIdentify happens, so handling it here.
           * Form xml and do OCR even after load move gets completed
           */
          def isLoadMove = getMovePurpose(inQCId)
          if (isLoadMove) {
            /** select REFERENCE_ID from qc_commands where QC_ID = 'QC101' and
            PRESUMED_CONTAINER_ID = 'SHAU0908074' */
            String loadQueryreference = "select REFERENCE_ID from qc_commands where qc_id = '" + inQCId + "' and PRESUMED_CONTAINER_ID='" + inUnitId +  "'"
            List loadQr = _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).queryForList(
                    String.format(loadQueryreference)
            );
            if (loadQr.size() > 0) {
              reference = loadQr.get(0).toString().substring(loadQr.get(0).toString().indexOf(':') + 1, loadQr.get(0).toString().lastIndexOf(']'))
            }
          } else //its a discharge move
          {
            result = 'Reference Id not fetched from TEAMS'
          }
        }
      } else {
        result = 'Warning: Current Position parameter must be supplied to frame XML for TEAMS'
      }
      if (!reference.isEmpty()) {
        ocrXML = '&lt;units&gt;&#10;  &lt;unit cas-unit-reference=&apos;' + reference + '&apos; cas-transaction-reference=&apos;' + reference + '&apos; id=&apos;' + inUnitId +
                '&apos; gross-weight=&apos;12000&apos; iso-code=&apos;' + inIsoCode + '&apos; tank-rail-type=&apos;' + inTankRailType + '&apos; length-mm=&apos;' + inLength +
                '&apos; height-mm=&apos;' + inHeight + '&apos; width-mm=&apos;' + inWidth + '&apos;&gt;&#10;&lt;/unit&gt;&#10;&lt;/units&gt;'
      } else {
        result = 'Warning: Transaction reference  is Empty, not forming XML for TEAMS'
      }
    } catch (Exception ex) {
      result = 'Framing XML for TEAMS failed : ' + ex;
    }
    if (result.isEmpty()) // if there are no warnings, send ocr xml formed here
    {
      return "<groovy class-location='code-extension' class-name='DefaultN4InboundCasMessageHandler'> \n" +
              "  <parameters> \n" +
              "  <parameter id='requestType' value=\"unitCaptureMessage\"/> \n" +
              "  <parameter id='visitType' value='" + inVisitType + "'/> \n" +
              "  <parameter id='visitId' value='" + inVisitId + "'/> \n" +
              "  <parameter id='craneId' value='" + inQCId + "'/> \n" +
              "  <parameter id='action' value='" + inAction + "'/> \n" +
              "  <parameter id='unitXml' value='" + ocrXML + "'/> \n" +
              "  </parameters> \n" +
              "  </groovy>"
    } else {
      return result
    }
  }

   private boolean getMovePurpose(String qcId) {
    Che che = ((Che) HibernateApi.getInstance().getUniqueEntityByDomainQuery(QueryUtils.createDomainQuery(ArgoEntity.CHE)
            .addDqPredicate(PredicateFactory.eq(ArgoField.CHE_SHORT_NAME, qcId))));
    def workAssignment = null;
    DomainQuery dq = QueryUtils.createDomainQuery(ArgoEntity.WORK_ASSIGNMENT)
    dq.addDqPredicate(PredicateFactory.eq(ArgoField.WORKASSIGNMENT_CHE, che.cheGkey))
    dq.addDqPredicate(PredicateFactory.eq(ArgoField.WORKASSIGNMENT_MOVE_PURPOSE_ENUM, WaMovePurposeEnum.QC_LOAD))
    workAssignment = HibernateApi.getInstance().findEntitiesByDomainQuery(dq);
    if (workAssignment != null) {
      return true
    }
    return false; // if its not a load move
  }
}
