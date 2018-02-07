/*
 * Copyright (c) 2015 Navis LLC. All Rights Reserved.
 *
 */
package test.extension.groovy

import com.navis.argo.ContextHelper
import com.navis.argo.business.atoms.DataSourceEnum
import com.navis.argo.business.atoms.EdiMessageClassEnum
import com.navis.argo.business.model.CarrierVisit
import com.navis.argo.business.portal.EdiExtractDao
import com.navis.argo.business.reference.LineOperator
import com.navis.argo.business.snx.ISnxImporter
import com.navis.argo.business.snx.SnxUtil
import com.navis.argo.util.FileUtil
import com.navis.argo.util.XmlUtil
import com.navis.edi.EdiEntity;

/**
 * Created by rajansh on 08-04-2015.
 */
import com.navis.edi.EdiField
import com.navis.edi.business.api.EdiExtractManager
import com.navis.edi.business.api.EdiFinder
import com.navis.edi.business.api.EdiPostManager
import com.navis.edi.business.atoms.EdiProcessEnum
import com.navis.edi.business.edimodel.EdiInboundDao
import com.navis.edi.business.edimodel.EdiInboundManagerPea
import com.navis.edi.business.edimodel.EdiUtil
import com.navis.edi.business.entity.EdiBatch
import com.navis.edi.business.entity.EdiEvent
import com.navis.edi.business.entity.EdiInterchange
import com.navis.edi.business.entity.EdiMailbox
import com.navis.edi.business.entity.EdiSegment
import com.navis.edi.business.entity.EdiSession
import com.navis.edi.business.entity.EdiTradingPartner
import com.navis.edi.business.portal.communication.FtpAdaptor
import com.navis.edi.business.portal.communication.Inbox
import com.navis.edi.test.EdiTestData
import com.navis.framework.business.Roastery
import com.navis.framework.persistence.HibernateApi
import com.navis.framework.persistence.hibernate.CarinaPersistenceCallback
import com.navis.framework.persistence.hibernate.PersistenceTemplate
import com.navis.framework.portal.Ordering
import com.navis.framework.portal.QueryUtils
import com.navis.framework.portal.UserContext
import com.navis.framework.portal.query.DomainQuery
import com.navis.framework.portal.query.PredicateFactory
import com.navis.framework.util.CarinaUtils
import com.navis.framework.util.TransactionParms
import com.navis.framework.util.message.MessageCollector
import com.navis.framework.util.message.MessageCollectorFactory
import com.navis.framework.util.message.MessageLevel;
import com.navis.framework.zk.util.JSONBuilder
import com.navis.rail.business.RailConsistExtractorPea
import net.sf.hibernate.exception.ExceptionUtils;
import org.apache.log4j.Logger
import org.jdom.Document
import org.jdom.Element
import org.jdom.input.SAXBuilder
import org.jdom.input.SAXHandler
import org.apache.commons.lang.StringUtils


import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.SAXParser
import javax.xml.parsers.SAXParserFactory
import java.text.SimpleDateFormat;

/**
 * Created by rajansh on 07-04-2015.
 */
public class TestCommandSnxEdi {

  //Logger for TestCommand
  public Logger LOGGER = Logger.getLogger(test.extension.groovy.TestCommandSnxEdi.class);
  protected TestCommandHelper _testCommandHelper = new TestCommandHelper();
  /** holds the result returned by each method */
  def String returnString = null;
  /** json builder, frames the output in json format */
  def builder = new groovy.json.JsonBuilder();


  /**
     * Imports given snx file and creates the given entity<br>
     *
     * @param inParameters The map containing the method name to call along with the parameters<br><br>
     * command=LoadSnxFile<br>
     * filePath=Path of the xml file to be imported
     * @return JSON , <code>Posted Snx Successfully</code><br>
     *                <code>Snx loading failed:</code>
     * @see test
     * Table invoked in SPARCS : inv_unit<br>
     * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
     * &lt;parameters&gt;<br>
     * &lt;parameter id="command" value="PostSnx" /&gt;<br>
     * &lt;parameter id="filePath" value="C:\CreateVesselVisit.xml" /&gt;<br>
     * &lt;/parameters&gt;<br>
     * &lt;/groovy&gt;<br>
     *
     */
    public String PostSnx(Map inParameters) { //ARGO-72978
        assert inParameters.size() == 2 , "Must supply 2 parameters:" +
                "<parameter id=\"command\" value=\"PostSnx\" />" +
                "<parameter id=\"filePath\" value=\"Path of the snx file to be loaded...\" />";

        String inFilePath = _testCommandHelper.checkParameter("filePath", inParameters);
        try {
            //using sax parser to get the document object from xml file
            SAXBuilder builder = new SAXBuilder();
            Document document=builder.build(new File(inFilePath));
            importSnx(document,ContextHelper.getThreadUserContext());
            returnString = "Posted Snx Successfully";
        } catch (Exception ex) {
            returnString = "Snx loading failed:" + ex;
        }
      builder {
        actual_result returnString;
      }
      return builder;
    }

    /**
     * Loads given edi file (*.edi/*.xml) and creates the given entity<br>
     * <a href="http://jira.navis.com/browse/ARGO-73979"> ARGO-73979 </a>
     *
     * @param inParameters The map containing the method name to call along with the parameters<br><br>
     * command=LoadEdi<br>
     * filePath=Path of the xml file to be imported<br>
     * ediSession=session name on which the file is to be loaded
     * @return JSON , <code>Loaded Edi Successfully</code><br>
     *                <code>Edi loading failed:</code>
     * @see test
     * Table invoked in SPARCS : inv_unit<br>
     * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
     * &lt;parameters&gt;<br>
     * &lt;parameter id="command" value="LoadEdi" /&gt;<br>
     * &lt;parameter id="filePath" value="C:\CreateVesselVisit.xml" /&gt;<br>
     * &lt;parameter id="ediSession" value="APL_BAPLIE_IN" /&gt;<br>
     * &lt;/parameters&gt;<br>
     * &lt;/groovy&gt;<br>
     *
     */
    public String LoadEdi(Map inParameters) { //ARGO-73979
        assert inParameters.size() >= 2 , '''Must supply 2 parameters:
                <parameter id=command value="LoadEdi"/>
                <parameter id=filePath value="Path of the edi file to be loaded"/>
                <parameter id=ediSession value="EDI session on which the file is to be loaded"/>'''

        String inFilePath = _testCommandHelper.checkParameter("filePath", inParameters);
        String inEdiSession = _testCommandHelper.checkParameter("ediSession", inParameters);
        try {
            EdiSession session = EdiSession.findEdiSession(inEdiSession);
            //verify if session is found before proceeding
            if(session != null) {
                UserContext userContext = ContextHelper.getThreadUserContext();
                MessageCollector messageCollector =
                        getIbMngr().loadClassifyMapAndPost(null,userContext,FileUtil.getFile(inFilePath).getAbsolutePath(),null,session.getEdisessGkey() ,Boolean.TRUE,Boolean.FALSE);
                if (messageCollector.hasError()) {
                    returnString = 'Edi loading failed : ' + messageCollector.toLoggableString(ContextHelper.getThreadUserContext());
                } else {
                    DomainQuery dq = QueryUtils.createDomainQuery(EdiEntity.EDI_BATCH)
                            .addDqPredicate(PredicateFactory.eq(EdiField.EDIBATCH_SESSION, session.getEdisessGkey()))
                            .addDqOrdering(Ordering.desc(EdiField.EDIBATCH_CREATED));
                    List batches = HibernateApi.getInstance().findEntitiesByDomainQuery(dq);
                    EdiBatch batch = (EdiBatch) batches.get(0);
                    if (batch != null && (batch.getEdibatchStatus().name.equalsIgnoreCase('COMPLETE') || batch.getEdibatchStatus().name.equalsIgnoreCase('WARNING'))) {
                        returnString = "Loaded Edi Successfully : " + batch.getEdibatchStatus().name;
                    } else
                        returnString = 'Edi loading failed : ' + batch.getEdibatchStatus().name;
                }
            } else returnString = "Edi loading failed, given session : '" + inEdiSession + "' not found";
        } catch (Exception ex) {
            returnString = "Edi loading failed:" + ex;
        }
      builder {
        actual_result returnString;
      }
      return builder;
    }

    /**
     * Extracts edi files other than baplie files for the given session<br>
     * <a href="http://jira.navis.com/browse/ARGO-73981"> ARGO-73981 </a>
     *
     * @param inParameters The map containing the method name to call along with the parameters<br><br>
     * command=ExtractEdi<br>
     * ediSession=edi session name
     * @return JSON , <code>Edi extraction successful</code><br>
     *                <code>Edi extraction failed, given session : </code>
     *                <code>Edi extraction failed:</code>
     * @see test
     * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
     * &lt;parameters&gt;<br>
     * &lt;parameter id="command" value="ExtractEdi" /&gt;<br>
     * &lt;parameter id="ediSession" value="APL_BAPLIE_IN" /&gt;<br>
     * &lt;parameter id="lastRunTime" value="timestamp" /&gt;<br>
     * &lt;/parameters&gt;<br>
     * &lt;/groovy&gt;<br>
     *
     */
    public String ExtractEdi(Map inParameters) {
        assert inParameters.size() >= 3 , '''Must supply 3 parameters:
                <parameter id=command value="ExtractEdi"/>
                <parameter id=ediSession value="EDI session on which the file is to be loaded"/>
                <parameter id=lastRunTime value="current"/>'''

        Set<EdiSegment> ediSegments;
        String segment;
        String inEdiSession = _testCommandHelper.checkParameter("ediSession", inParameters);
        String inLastRunTime = _testCommandHelper.checkParameter("lastRunTime", inParameters);
        try {
            Date lastRunTime;
            //if input contains the keyword 'current' calculate current time from helper method
            if(inLastRunTime.contains('current')) {
                lastRunTime = _testCommandHelper.calculateTime(inLastRunTime);
            } else { //if direct time stamp is given as string
                SimpleDateFormat formatter = new SimpleDateFormat("dd-mm-yyyy HH:mm");
                lastRunTime = formatter.parse(inLastRunTime);

            }
            EdiInboundDao ediInboundDao = new EdiInboundDao();
            EdiSession session = EdiSession.findEdiSession(inEdiSession);

            //verify if session is found before proceeding
            if(session != null) {
                EdiExtractManager extractMgr = (EdiExtractManager) Roastery.getBean(EdiExtractManager.BEAN_ID);
                session.setFieldValue(EdiField.EDISESS_LAST_RUN_TIMESTAMP,lastRunTime);
                final EdiExtractDao ediExtractDao = new EdiExtractDao();
                ediExtractDao.setSessionGkey(session.getPrimaryKey());
                MessageCollector mc = extractMgr.extractEdiSession(ediExtractDao);

                //output the extracted segments
                DomainQuery dq = QueryUtils.createDomainQuery(EdiEntity.EDI_BATCH)
                        .addDqPredicate(PredicateFactory.eq(EdiField.EDIBATCH_SESSION, session.getEdisessGkey()))
                        .addDqOrdering(Ordering.desc(EdiField.EDIBATCH_CREATED));
                List batches = HibernateApi.getInstance().findEntitiesByDomainQuery(dq);
                EdiBatch batch = (EdiBatch) batches.get(0);
                ediSegments = batch.getEdiSegmentSet();
                ediSegments.each {
                    segment = segment + "," +it.edisegSegment
                }
                segment = segment.replace("null,","")

                if(mc.hasError()) {
                    returnString = 'Edi extraction failed : ' + mc.toLoggableString(ContextHelper.getThreadUserContext());
                } else if (mc.containsMessageLevel(MessageLevel.WARNING) || mc.containsMessageLevel(MessageLevel.INFO)) {
                    returnString = 'Edi extraction successful : ' + mc.toLoggableString(ContextHelper.getThreadUserContext());
                }
                else returnString = 'Edi extraction successful';
            } else returnString = "Edi extraction failed, given session : '" + inEdiSession + "' not found";
        } catch (Exception ex) {
            returnString = "Edi extraction failed:" + ex;
        }
      builder {
        actual_result returnString;
		if (!segment.isEmpty()) {	
			data('extractedSegment': segment)
		}
      }
      return builder;
    }

   /**
     * Extracts edi files other than baplie files for the given session<br>
     * <a href="http://jira.navis.com/browse/ARGO-73981"> ARGO-73981 </a>
     *
     * @param inParameters The map containing the method name to call along with the parameters<br><br>
     * command=ExtractBaplie<br>
     * ediSession=edi session name
     * @return JSON , <code>Baplie extract successful</code><br>
     *                <code>Baplie extract failed : </code>
     *                <code>Vessel visit not found :</code>
     *                <code>Edi extraction failed, given session : '" + inEdiSession + "' not found</code>
     *                <code>Edi extraction failed</code>
     * @see test
     * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
     * &lt;parameters&gt;<br>
     * &lt;parameter id="command" value="ExtractBaplie" /&gt;<br>
     * &lt;parameter id="ediSession" value="APL_BAPLIE_IN" /&gt;<br>
     * &lt;parameter id="vesselVisitId" value="APLCHO001" /&gt;<br>
     * &lt;/parameters&gt;<br>
     * &lt;/groovy&gt;<br>
     **/
    public String ExtractBaplie(Map inParameters) {
        assert inParameters.size() >= 2 , '''Must supply 2 parameters:
                <parameter id=command value="ExtractBaplie"/>
                <parameter id=vesselVisitId value="vessel visit id from which the outbound edi should be extracted"/>
                <parameter id=ediSession value="EDI session on which the file is to be loaded"/>'''

        Set<EdiSegment> ediSegments;
        String segment;
        String inEdiSession = _testCommandHelper.checkParameter("ediSession", inParameters);
        String inVesselVisitId = _testCommandHelper.checkParameter('vesselVisitId', inParameters);
        try {
            EdiSession session = EdiSession.findEdiSession(inEdiSession);
            //verify if session is found before proceeding
            if(session != null) {
                CarrierVisit vesselVisit = CarrierVisit.findVesselVisit(ContextHelper.getThreadFacility(),inVesselVisitId);
                if(vesselVisit != null) {
                    EdiExtractManager extractMgr = (EdiExtractManager) Roastery.getBean(EdiExtractManager.BEAN_ID);
                    MessageCollector mc = extractMgr.extractEdiSession(null, session.edisessGkey, vesselVisit.getCvGkey(), Boolean.TRUE);

                    //output the extracted segments
                    DomainQuery dq = QueryUtils.createDomainQuery(EdiEntity.EDI_BATCH)
                            .addDqPredicate(PredicateFactory.eq(EdiField.EDIBATCH_SESSION, session.getEdisessGkey()))
                            .addDqOrdering(Ordering.desc(EdiField.EDIBATCH_CREATED));
                    List batches = HibernateApi.getInstance().findEntitiesByDomainQuery(dq);
                    EdiBatch batch = (EdiBatch) batches.get(0);
                    ediSegments = batch.getEdiSegmentSet();
                    ediSegments.each {
                        segment = segment + "," +it.edisegSegment
                    }
                    segment = segment.replace("null,","")

                    if(mc.hasError()) {
                        returnString = 'Baplie extract failed : ' + mc.toLoggableString(ContextHelper.getThreadUserContext())
                    } else {
                        returnString = 'Baplie extract successful'
                    }
                } else returnString = 'Vessel visit not found : ' + inVesselVisitId
            } else returnString = "Edi extraction failed, given session : '" + inEdiSession + "' not found";
        } catch (Exception ex) {
            returnString = "Edi extraction failed:" + ex;
        }
		builder {
			actual_result returnString;
			if (!segment.isEmpty()) {	
				data('extractedSegment': segment)
			}
		}
		return builder;
    }

   /**
     * Extracts edi files other than baplie files for the given session<br>
     * <a href="http://jira.navis.com/browse/ARGO-73981"> ARGO-73981 </a>
     *
     * @param inParameters The map containing the method name to call along with the parameters<br><br>
     * command=ExtractRailConsist<br>
     * ediSession=edi session name<br>
     * trainVisitId=name of the train visit
     * @return JSON , <code>Baplie extract successful</code><br>
     *                <code>Baplie extract failed : </code>
     *                <code>Train visit not found :</code>
     *                <code>Edi extraction failed, given session : '" + inEdiSession + "' not found</code>
     *                <code>Edi extraction failed</code>
     * @see test
     * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
     * &lt;parameters&gt;<br>
     * &lt;parameter id="command" value="ExtractRailConsist" /&gt;<br>
     * &lt;parameter id="ediSession" value="APL_BAPLIE_IN" /&gt;<br>
     * &lt;parameter id="trainVisitId" value="3CBE1" /&gt;<br>
     * &lt;/parameters&gt;<br>
     * &lt;/groovy&gt;<br>
     **/
    public String ExtractRailConsist(Map inParameters) {
        assert inParameters.size() >= 2 , '''Must supply 2 parameters:
                <parameter id=command value="ExtractRailConsist"/>
                <parameter id=ediSession value="EDI session on which the file is to be loaded"/>'''

  		Set<EdiSegment> ediSegments;
        String segment;
        String inEdiSession = _testCommandHelper.checkParameter("ediSession", inParameters);
        String inTrainVisitId = _testCommandHelper.checkParameter('trainVisitId', inParameters);
        try {
            EdiSession session = EdiSession.findEdiSession(inEdiSession);
            //verify if session is found before proceeding
            if(session != null) {
                CarrierVisit trainVisit = CarrierVisit.findTrainVisit(ContextHelper.getThreadComplex(),ContextHelper.getThreadFacility(),inTrainVisitId)
                if(trainVisit != null) {
                    EdiExtractManager extractMgr = (EdiExtractManager) Roastery.getBean(EdiExtractManager.BEAN_ID);
                    MessageCollector mc = extractMgr.extractEdiSession(null, session.edisessGkey, trainVisit.getCvGkey(), Boolean.TRUE);

                    //output the extracted segments
                    DomainQuery dq = QueryUtils.createDomainQuery(EdiEntity.EDI_BATCH)
                            .addDqPredicate(PredicateFactory.eq(EdiField.EDIBATCH_SESSION, session.getEdisessGkey()))
                            .addDqOrdering(Ordering.desc(EdiField.EDIBATCH_CREATED));
                    List batches = HibernateApi.getInstance().findEntitiesByDomainQuery(dq);
                    EdiBatch batch = (EdiBatch) batches.get(0);
                    ediSegments = batch.getEdiSegmentSet();
                    ediSegments.each {
                        segment = segment + "," +it.edisegSegment
                    }
                    segment = segment.replace("null,","")

                    if(mc.hasError()) {
                        returnString = 'Rail consist extract failed : ' + mc.toLoggableString(ContextHelper.getThreadUserContext())
                    } else {
                        returnString = 'Rail consist extract successful'
                    }
                } else returnString = 'Train visit not found : ' + inTrainVisitId
            } else returnString = "Edi extraction failed, given session : '" + inEdiSession + "' not found";
        } catch (Exception ex) {
            returnString = "Edi extraction failed:" + ex;
        }
		builder {
			actual_result returnString;
			if (!segment.isEmpty()) {	
				data('extractedSegment': segment)
			}	
		}
		return builder;
    }

    private static EdiInboundManagerPea getIbMngr() {
        return (EdiInboundManagerPea) Roastery.getBean(EdiInboundManagerPea.BEAN_ID);
    }

    private static EdiFinder getFndr() {
        return (EdiFinder) Roastery.getBean(EdiFinder.BEAN_ID);
    }

    /**
     * Imports Snx file and creates the given entity in the xml file
     * (Replica of method importSnx in BaseArgoTestCase)
     * @param inSnx
     * @param inUc
     */
    public void importSnx(Document inSnx, UserContext inUc) {
        try {
            PersistenceTemplate pt = new PersistenceTemplate(inUc);
            Element rootElement = inSnx.getRootElement()
            final String userId = rootElement.getAttributeValue("user-id");
            final String sequenceNumber = rootElement.getAttributeValue("sequence-number");
            final String eventTime = rootElement.getAttributeValue("event-time");

            List<Element> elementList = rootElement.getChildren();
            for (final Element inElement : elementList) {
                // Finds the Importer for this element type
                String elementName = inElement.getName();
                final ISnxImporter importer;
                try {
                    importer = SnxUtil.getSnxImporterForElement(elementName);
                    importer.setScopeParameters();
                    ContextHelper.setThreadDataSource(DataSourceEnum.SNX);

                    if (userId != null) {
                        ContextHelper.setThreadExternalUser(userId);
                    }
                    if (sequenceNumber != null) {
                        ContextHelper.setThreadExternalSequenceNumber(sequenceNumber);
                    }
                    if (eventTime != null) {
                        ContextHelper.setThreadExternalEventTime(XmlUtil.toDate(eventTime, ContextHelper.getThreadUserTimezone()));
                    }
                    importer.parseElement(inElement);
                } catch (Exception ex) {
                    returnString = "Could not find Importer" + ex;
                    return;
                }
            }
        } catch (Exception ex) {
            returnString = "SNX Import failed" + ex;
        }
    }

    public String execute(Map inParameters) {
        assert inParameters.size() > 0, '''Must supply at least 1 parameter:
                                       <parameter id="command" value="<API name>" />''';
        String methodName = _testCommandHelper.checkParameter('command', inParameters);

        this."$methodName"(inParameters);
    }
}

