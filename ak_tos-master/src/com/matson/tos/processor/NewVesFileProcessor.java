/*
*********************************************************************************************
* Srno   Date			AuthorName			Change Description
* A1     02/25/08       Glenn Raposo		Changes made to Copy XML string to Temp Test File
* A2     09/12/08       Glenn Raposo        Added Process completion & Error Email Notification   
* A3	 10/01/08       Steven Bauer	    Update Shipper and Consignee names. 
* A4     10/09/08       Glenn Raposo        Supplemental File Processing & Mailing  
* A5	 11/06/08		Steven Bauer		Change from required temp to recorded temp
* A6	 11/10/08		Steven Bauer		Added code to hande New Vess in Prod and Supp in DataLoad.
* A7     11/18/08       Glenn Raposo        Commented out reefer recording history change    
* A8	 12/04/08	    Steven Bauer		Added vessel visit 
* A9     12/09/08       Glenn Raposo        Added Event Notes
* A10    12/09/08       Glenn Raposo        Change to set the first unit EventNotes and tState  
* A11	 12/18/08		Steven Bauer		Added Tstate Lookup 
* A12    02/12/09		Steven Bauer		Added uniqueKey to Supplementals.
* A13    02/13/09       Glenn Raposo        Added Unit NLT Gvy injection xml Creation 
* A14    02/25/09		Steven Bauer		Added publish of the units to the NewVess Topic
* A15	 03/13/09		Steven Bauer		Added DCM CSV for Phoenix.
* A16	 03/19/09		Steven Bauer	    Fixed memory leak in topic message.
* A17    03/28/09		Steven Bauer		Fixed double blank.
* A18	 04/01/09		Steven Bauer		Supress all but containers on Supplimentals
* A19    04/03/09       Steven Bauer		Supress OB Carrier on Sublimentals  
* A20	 04/07/09		Steven Bauer		Suppress booking for Hi to Hi shipping.
* A21    04/08/09	    Steven Bauer		Send correct digit message in the note.
* A22    04/14/09		Steven Bauer		Remove NLT code.16
* A23    04/16/09		Steven Bauer		Added count to notice email
* A24    05/05/09       Glenn Raposo        Added holds to Snx notes to validate and release holds in N4
* A25	 05/08/09		Steven Bauer		Added Inbound to subject error, issue 159
* A26    05/13/09       Glenn Raposo        Change code to Set CarrierId  
* A27    05/22/09		Steven Bauer		Added missing hazard
* A28	 06/08/09		Steven Bauer		Handled null pointer exception in addDcm and only create records 
* 										    in prod mode.
* A29	 06/10/09		Steven Bauer		Don't pass I/B carrier on supplimental
* A30	 06/19/09		Steven Bauer		Fix SNX to not produce mutliple updates on exception.
* A31    07/23/09       Glenn Raposo        Update the Email Notification Sub Message
* A32    07/27/09       Glenn Raposo        Differentiate NV and Barge mail Distribution list 
* A33	 08/06/09		Steven Bauer		Fixed VesselVisit update, was never running.
* A34	 08/25/09		Steven Bauer		Send Invoice to Hon only.
* A35    10/07/09       Glenn Raposo        Haz UN/NA Change to Inbound Dcm
* A36    10/20/09       Glenn Raposo        Added Notes on Newves Completion & Check to snxtopicObj
* A37    10/22/09       Glenn Raposo        Move Mailing to Mail Confirmation
* A38    01/06/10       Glenn Raposo        Add Equi Dmg code to message, EOF check, Set RDS DateTime 24hrs Standard.
* A39    01/20/10       Glenn Raposo        Post AG only Newves 
* A40	 12/30/10		Steven Bauer		Added delay between postings.
* A41    09/02/11       Glenn Raposo        Added Facility to the last unit messages
* A42    09/02/11       Glenn Raposo        Add Code to handel Gum newves File
* A43    10/05/11       Glenn Raposo        SnxMailBox changes a) post to ftp site and backup Filein logs
* A44    11/28/11       Glenn Raposo        Added Facility to carrier Attribute 
* A45    12/16/11       Glenn Raposo        Adding Vessel-visit to SnxmailBox file
* A46	 08/19/13		Karthik Rajendran	Setting rdsDtTime for supplemental from current date & time
* A47	 08/19/13		Karthik Rajendran	Removing the A46 change, bcz its not needed
* A48	 08/19/13		Karthik Rajendran	Removing rdsDtTime = null from Supplemental update notes
* A49	 09/16/13		Karthik Rajendran	Archive Guam newves snx xml to "gumnewvesxml" FTP location
* A50    09/24/13		Karthik Rajendran	For Barge newves set validate="false" in <equipment> tag
*********************************************************************************************
*/

package com.matson.tos.processor;

import com.matson.cas.erd.service.data.VesselVO;
import com.matson.cas.refdata.mapping.TosHoldPermData;
import com.matson.cas.service.ftp.FtpProxyDeleterBiz;
import com.matson.cas.service.ftp.FtpProxyGetterBiz;
import com.matson.cas.service.ftp.FtpProxyListBiz;
import com.matson.cas.service.ftp.FtpProxySenderBiz;
import com.matson.tos.constants.TransitState;
import com.matson.tos.dao.NewVesselDao;
import com.matson.tos.dao.TosLookup;
import com.matson.tos.exception.TosException;
import com.matson.tos.jatb.DcmConvert;
import com.matson.tos.jatb.NewVes;
import com.matson.tos.jaxb.snx.*;
import com.matson.tos.messageHandler.AbstractMessageHandler;
import com.matson.tos.messageHandler.DcmCSVMessageHandler;
import com.matson.tos.messageHandler.NewVesMessageHandler;
import com.matson.tos.messageHandler.SnxMessageHandler;
import com.matson.tos.util.*;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.jms.JMSException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.util.JAXBSource;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class NewVesFileProcessor extends AbstractFileProcessor {

    private static Logger logger = Logger.getLogger(NewVesFileProcessor.class);
    private NewVesMessageHandler msgHandler = null;
    private DcmCSVMessageHandler dcmHandler = null;
    private int containerCnt = 1;

    private Snx snxObj = new Snx();
    private Snx snxShipper = new Snx();
    private Snx snxConsignee = new Snx();

    private Snx snxVesselVisit = new Snx();
    private List<TUnit> units = snxObj.getUnit();
    private List<TShipperConsignee> shippers = snxShipper.getShipperConsignee();
    private List<TShipperConsignee> consignees = snxConsignee.getShipperConsignee();

    private TUnit aUnit = null;
    private TUnit mnsUnit = null;
    private TUnit mnsUnit1 = null;

    private Snx snxTopicObj = null;

    private TUnit currUnit = null;
    private String carrierId = null;
    private String rdsTriggerDtTime = null;
    private String localRdsTriggerDtTime = null;

    private SnxMessageHandler snxMsgHandler = null;
    private JMSSender sender;
    private JMSTopicSender topicSender;
    // Hard coded for now, could move to ref data.
    private static final String topic = "jms.topic.tdp.newVesselHon";

    //A1 Starts
    private StringWriter out = new StringWriter();
    private String FILE_NAME = "NewVes.xml";
    private String PROCESS_NEWVES = "NV";
    private String process = "";
    private String Completed = " Completed ";
    private String eof = ">>END OF FILE<<";
    //A2
    private String MAIL_HOST = TosRefDataUtil.getValue("MAIL_HOST");
    private String supportMail = TosRefDataUtil.getValue("SUPPORT_EMAIL");
    private String lateDcm = TosRefDataUtil.getValue("EMAIL_LATE_DCM");
    private String environment = EnvironmentProperty.getEnvType();
    private boolean prodMode = false;
    private String vesselService = null;

    private Snx consignee = null;
    private String port = "ANK";
    private static String ANK = "ANK";
    private static String KDK = "KDK";
    private static String DUT = "DUT";

    private static final int injectionPause = 120000;
    private CheckDigit check = new CheckDigit();
    private int unitCnt = 0;
    private String facility = null;
    //A24
    private StringBuffer holdOnUnit = new StringBuffer();
    private int postingDelay = 1;
    private String copyPrimary = null;
    //A43
    FtpProxySenderBiz ftpSender = null;
    int SNX_FTP_ID = 0;
    boolean postToSnxMailBox;
    boolean postNewvesToQueue;
    public static String storeDir = "/home/logs/applogs/TOS/snxmailbox";
    public static String gumSnxDir = "/home/logs/applogs/TOS/gumnewvesxml";

    public NewVesFileProcessor() {
        port = "ANK";
        init();
    }


    public NewVesFileProcessor(String port) {
        this.port = port;
        init();
    }

    public void init() {
        try {
            msgHandler = new NewVesMessageHandler(
                    "com.matson.tos.jaxb.snx", "com.matson.tos.jatb",
                    "/xml/newVes.xml", AbstractMessageHandler.TEXT_TO_XML);
            snxMsgHandler = new SnxMessageHandler(
                    "com.matson.tos.jaxb.snx", "com.matson.tos.jatb", "",
                    AbstractMessageHandler.TEXT_TO_XML);
            sender = new JMSSender(JMSSender.BATCH_QUEUE, port);
            topicSender = new JMSTopicSender(topic);

            dcmHandler = new DcmCSVMessageHandler();
        } catch (TosException tex) {
            tex.printStackTrace();
            logger.error("Error in creating the object: ", tex);
        }
    }

    public void processFiles() {
        String ftpFileName = null;

        try {
            int ftpProxyId = Integer.parseInt(TosRefDataUtil.getValue("VES_FILES_FTP_ID"));
            int supFtpProxyId = Integer.parseInt(TosRefDataUtil.getValue("SUP_FILES_FTP_ID"));

            int timeout = Integer.parseInt(TosRefDataUtil.getValue("FTP_TIMEOUT"));
            logger.debug("FTP timeout retrieved is: " + timeout);
            //A43 - Started
            SNX_FTP_ID = Integer.parseInt(TosRefDataUtil.getValue("NV_SNX_MAILBOX"));
            postToSnxMailBox = Boolean.parseBoolean(TosRefDataUtil.getValue("SNX_MAILBOX_FTP"));
            postNewvesToQueue = Boolean.parseBoolean(TosRefDataUtil.getValue("POST_NEWVES_TO_QUE"));

            ftpSender = new FtpProxySenderBiz();
            ftpSender.setTimeout(timeout);
            ftpSender.setCreateSubDirectory(true);
            //A43 - Stopped

            try {
                postingDelay = Integer.parseInt(TosRefDataUtil.getValue("NEW_VESS_PAUSE"));
            } catch (Exception e) {
            }
            logger.debug("NEWVES PROXY ID : " + ftpProxyId + "   SUPP PROXY ID" + supFtpProxyId);

            FtpProxyListBiz list = new FtpProxyListBiz();
            list.setTimeout(timeout);
            //FTPFile[] files = list.getFileList(ftpProxyId);
            String[] files = list.getFileNames(ftpProxyId, null, null);
            logger.debug("Total number of NewVes files " + (files != null ? files.length : "0"));
            String[] supFiles = list.getFileNames(supFtpProxyId, null, null);
            logger.debug("Total number of supp files " + (supFiles != null ? supFiles.length : "0"));

            FtpProxyGetterBiz getter = new FtpProxyGetterBiz();
            getter.setTimeout(timeout);


            // For the new tos new vess changes as N4 will send to MNS and DAS
            HashMap parmsMap = NewVesselDao.getTosCopyParameters();
            if (parmsMap != null) {
                copyPrimary = (String) parmsMap.get("IS_PRIMARY");
                logger.info("new ves copy parameters copyPrimary :" + copyPrimary);
            }
            //End

            //Process NewVes File
            for (int i = 0; files != null && i < files.length; i++) {
                ftpFileName = files[i];
                boolean processed = false;
                logger.debug("Processing New Ves file: " + ftpFileName);
                String contents = getter.getFileText(ftpProxyId, ftpFileName);
                String vesselCode = ftpFileName.substring(0, 3);
                VesselVO vvo = CommonBusinessProcessor.getVesselDetails(vesselCode);
                vesselService = vvo.getVessOpr();
                logger.info("vesselService in NewvesFileProcessor :" + vesselService);
                //logger.debug( "After Getting Contents for file: " + ftpFileName);
                //A38 - End of File Check
                if ((ftpFileName.toUpperCase().endsWith("NV.TXT") || ftpFileName.toUpperCase().endsWith("NI.TXT"))
                        && (contents != null && contents.contains(eof))) {
                    //A10,A13
                    if (ftpFileName.toUpperCase().endsWith("NI.TXT")) {

                    }
                    process = ftpFileName.toUpperCase().endsWith("NV.TXT") ? "NewVes" : "NIS Load Transaction";
                    facility = port;
                    prodMode = true;
                    msgHandler.setNewVes(true);
                    processFile(contents);
                    processed = true;
                    logger.debug("The file : " + ftpFileName + " processed successfully ::: processed flag - " + processed);

                } else {
                    logger.debug("The file name: " + ftpFileName +
                            " is not New Vessel file OR Skipped File as Copying is not Complete and will be ignored.");
                }
                if (processed) {
                    // remove file that has been processed
                    logger.debug("Deleting New Ves file: " + ftpFileName);
                    FtpProxyDeleterBiz del = new FtpProxyDeleterBiz();
                    del.setTimeout(timeout);
                    del.removeFile(ftpProxyId, ftpFileName);
                }
            }

            // START Splitting supplement files into multiple files based on vessel - AK changes
            // SNX for these vessel files should be posted to corresponding facility SNX mailbox
            // Skipping this splitting logic, instead implement the seggregation based on unit's active phase
//			Map<String, List> vesselMap = new NewvesProcessorHelper().populateSupplementVesselRecordsMap(supFiles);

            // Deleting old supp files
            /*for ( int i=0; supFiles!=null && i<supFiles.length; i++) {
				ftpFileName = supFiles[i];

				// remove file that has been processed
				logger.debug( "Deleting Supp file: " + ftpFileName);
				FtpProxyDeleterBiz del = new FtpProxyDeleterBiz();
				del.setTimeout(timeout);
				del.removeFile(supFtpProxyId, ftpFileName);

			}*/
            //Create files from seggregate vessel map records
            //Creating files from Map of ArrayList for each vessel
            // psethuraman, ignoring the split logic
//			new NewvesProcessorHelper().createSupplementFilesForVessel(vesselMap);

//			supFiles = list.getFileNames(supFtpProxyId, null, null);
//			logger.debug(  "Total number of supp files after split "+ (supFiles != null ? supFiles.length : "0"));
            //END splitting changes

            //Process Supplement File - A4
            for (int i = 0; supFiles != null && i < supFiles.length; i++) {
                ftpFileName = supFiles[i];
                boolean processed = false;

                logger.debug("Processing Supp file: " + ftpFileName);
                String contents = getter.getFileText(supFtpProxyId, ftpFileName);
                logger.debug("After Getting Contents for SUPP file: " + ftpFileName);
                if (ftpFileName.toUpperCase().endsWith("SP.TXT")
                        && (contents != null && contents.contains(eof))) {
                    process = "Supplemental Data";
                    facility = port;
                    prodMode = false;
                    msgHandler.setNewVes(false);
                    processFile(contents);
                    processed = true;
                } else {
                    logger.debug("The file name: " + ftpFileName +
                            " is not a Supp file OR Skipped File as Copying is not Complete and will be ignored.");
                }
                if (processed) {
                    // remove file that has been processed
                    logger.debug("Deleting Supp file: " + ftpFileName);
                    FtpProxyDeleterBiz del = new FtpProxyDeleterBiz();
                    del.setTimeout(timeout);
                    del.removeFile(supFtpProxyId, ftpFileName);
                }
            }

            //A42 - GUAM NEWVES PROCESS
            files = list.getFileNames(ftpProxyId, null, null);
            for (int i = 0; files != null && i < files.length; i++) {
                ftpFileName = files[i];
                boolean processed = false;

                logger.debug("Processing Gum file: " + ftpFileName);
                String contents = getter.getFileText(ftpProxyId, ftpFileName);
                logger.debug("After Getting Contents for GUAM file: " + ftpFileName);
                if (ftpFileName.toUpperCase().endsWith(".GUM") || ftpFileName.toUpperCase().endsWith(".SPN")
                        || ftpFileName.toUpperCase().endsWith(".OTH") && (contents != null && contents.contains(eof))) {
                    process = "GuamNewVes";
                    facility = "GUM";
                    prodMode = true;
                    msgHandler.setNewVes(true);
                    processFile(contents);
                    processed = true;

                } else {
                    logger.debug("The Guam file name: " + ftpFileName +
                            " is not a Supp file OR Skipped File as Copying is not Complete and will be ignored.");
                }
                if (processed) {
                    // remove file that has been processed
                    logger.debug("Deleting Guam file: " + ftpFileName);
                    FtpProxyDeleterBiz del = new FtpProxyDeleterBiz();
                    del.setTimeout(timeout);
                    del.removeFile(ftpProxyId, ftpFileName);
                }
            }

        } catch (Exception ftpEx) {
            logger.error("FTP error found: ", ftpEx);
            ftpEx.printStackTrace();
        } finally {
            copyPrimary = null;
        }
    }

    protected void processFile(String contents) {
        unitCnt = 0;
        snxTopicObj = new Snx();

        ProcessLoggerUtil processLogger = new ProcessLoggerUtil();
        try {
            boolean processFlag = false;
            String[] lines = contents.split("\n");
            logger.debug("There are " + lines.length + " lines in the file.");

            for (int i = 0; i < lines.length; i++) {
                processLine(lines[i], i + 1);
                //Records NV Process start A31
                if ("false".equalsIgnoreCase(copyPrimary)) {
                    if (carrierId != null && process.startsWith("NewVes") && !processFlag) {//A4
                        processLogger.recordProcessDetails(i, carrierId, PROCESS_NEWVES, "In-Process");
                        processFlag = true;
                    }
                }
            }
            // the last ves visit has not been sent out.
            if (aUnit != null) {

                if (prodMode) { //A36
                    aUnit.setSnxUpdateNote(process + Completed + currUnit.getSnxUpdateNote()); //A9, A21
                    aUnit.setTransitState(null);
                } else {
                    aUnit.setSnxUpdateNote(process + Completed); //A9
                    aUnit.setUniqueKey(aUnit.getId()); // A12
                    //A11
                    TosLookup lookup = null;
                    try {
                        lookup = new TosLookup();
                        TransitState state = lookup.getMostActiveTstate(aUnit.getId(), port);
                        if (state != null) {
                            logger.debug("Setting tstate=" + state.name);
                            aUnit.setTransitState(state.name);
                        } else {
                            logger.debug("No tstate found!");
                        }
                    } catch (Exception e) {
                        logger.error("Could not lookup TState", e);
                    } finally {

                        if (lookup != null) lookup.close();
                    }
                }
                // A23,A37
                unitCnt++;
                // A15
                mnsUnit = new TUnit();
                mnsUnit1 = new TUnit();
                mnsUnit = deepCopyJAXB(aUnit);
                mnsUnit1 = deepCopyJAXB(aUnit);
                logger.debug("before-aUnit--------------" + aUnit.getSnxUpdateNote());
                logger.debug("before-mnsUnit------------" + mnsUnit.getSnxUpdateNote());
                // A47
			/*// A46
			if(process.startsWith("Supplemental")) {
				Date today = new Date();
				SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
				String trigDate = sdf.format(today);
				localRdsTriggerDtTime = convertHSTTimeZone(trigDate,"HST");
				rdsTriggerDtTime = convertHSTTimeZone(trigDate,"PST");
				logger.debug("Setting rdsTriggerDtTime for Supplemental ---:"+rdsTriggerDtTime);
				logger.debug("Setting localRdsTriggerDtTime for Supplemental ---:"+localRdsTriggerDtTime);
			}*/
                // A48
                if (process.startsWith("Supplemental")) {
                    aUnit.setSnxUpdateNote(aUnit.getSnxUpdateNote() + unitSnxHolds() + " unitCnt=" + unitCnt + " facility=" + facility);
                    mnsUnit.setSnxUpdateNote(mnsUnit.getSnxUpdateNote() + unitSnxHolds() + " unitCnt=" + unitCnt + " facility=" + facility);
                    mnsUnit1.setSnxUpdateNote(mnsUnit1.getSnxUpdateNote() + unitSnxHolds() + " unitCnt=" + unitCnt + " facility=" + facility);// A48 end
                } else {
                    aUnit.setSnxUpdateNote(aUnit.getSnxUpdateNote() + unitSnxHolds() + " unitCnt=" + unitCnt + " facility=" + facility + " rdsDtTime=" + rdsTriggerDtTime); //A41
                    mnsUnit.setSnxUpdateNote(mnsUnit.getSnxUpdateNote() + unitSnxHolds() + " unitCnt=" + unitCnt + " facility=" + facility + " rdsDtTime=" + localRdsTriggerDtTime);
                    mnsUnit1.setSnxUpdateNote(mnsUnit1.getSnxUpdateNote() + unitSnxHolds() + " unitCnt=" + unitCnt + " facility=" + facility + " rdsDtTime=" + rdsTriggerDtTime);
                }
                logger.debug("after-aUnit getSnxUpdateNote--------------" + aUnit.getSnxUpdateNote());
                logger.debug("after-mnsUnit getSnxUpdateNote------------" + mnsUnit.getSnxUpdateNote());
                logger.debug("after-mnsUnit1 getSnxUpdateNote------------" + mnsUnit1.getSnxUpdateNote());
                clearBooking(aUnit);
                sendOneUnit(snxObj);
                //logger.debug("aUnit.setSnxUpdateNote:"+aUnit.getSnxUpdateNote());
                //logger.debug("mnsUnit.setSnxUpdateNote:"+mnsUnit.getSnxUpdateNote());

                if ("true".equalsIgnoreCase(copyPrimary)) {
                    logger.info("before creating repcord in process logger with trigger dat time :" + carrierId + " - " + rdsTriggerDtTime);
                    if (carrierId != null && process.startsWith("NewVes") && !processFlag) {//A4
                        processLogger.recordProcessDetails(1, carrierId, CalendarUtil.convertDateStringToString2(rdsTriggerDtTime, true), "In-Process");
                        processFlag = true;
                    }
                }

                addDcm(aUnit);
            }
            if (shippers.size() > 0) {
                sendOneUnit(snxShipper);
            }
            if (consignees.size() > 0) {
                sendOneUnit(snxConsignee);
            }
            //A1
            if (xmlTestFlag != null) {
                out.close();
                ExportXmlData.copyXmlToFile(FILE_NAME, out.toString(), xmlTestFlag);
            }


            // Publish Units to Topic
            if (prodMode) {
                // Only publish if going to HON, A34, A36, A39
                if (snxTopicObj.getUnit() != null && snxTopicObj.getUnit().size() > 0 && "NewVes".equals(process)) { //A42
                    //ADDED NEWVES FOR DAS and now POSTING TO QUEUE FOR MNS PROJECT
                    if ("false".equalsIgnoreCase(copyPrimary)) {
                        logger.info("*** SENDING TO MNS AND DETENTION***");
                        sendTopic(snxTopicObj);  //Das consumes
                        sendToMNS(snxTopicObj, mnsUnit);  //MNS consumes
                    }
                    //Raghu end
                    //Send Message to SnxMailBox
                } else if (snxTopicObj.getUnit() != null && snxTopicObj.getUnit().size() > 0 && "GuamNewVes".equals(process)) {
                    sendToMNS(snxTopicObj, mnsUnit);   //POST GUAM NEWVES TO QUEUE FOR MNS PROJECT
                }
            }

            // Moved here from down A43
            if (postToSnxMailBox) {// && !"GuamNewVes".equals(process)){ // A49
                if (mnsUnit1 != null) {
                    List<TUnit> localTUnits = snxTopicObj.getUnit();
                    if (localTUnits != null && localTUnits.size() > 0) {
                        for (int i = 0; i < localTUnits.size(); i++) {
                            TUnit localTUnit = localTUnits.get(i);
                            if (localTUnit != null && localTUnit.getSnxUpdateNote() != null && localTUnit.getSnxUpdateNote().contains("rdsDtTime")) {
                                localTUnit.setSnxUpdateNote(mnsUnit1.getSnxUpdateNote());
                                logger.debug("localTUnit.SnxUpdateNote---" + localTUnit.getSnxUpdateNote());
                            }
                        }
                    }
                }
                sendftpNew(snxTopicObj);  //A43
            }


            units.clear();
            shippers.clear();
            consignees.clear();

            snxTopicObj = null;
            aUnit = null;
            currUnit = null;
            holdOnUnit = new StringBuffer();

            if (prodMode) {

                EmailSender.mailAttachment(lateDcm, supportMail, MAIL_HOST, carrierId + ".csv", dcmHandler.toString(), "", carrierId + " Inbound HAZ DCM for New Vess File Processed in N4");
            }

        } catch (IOException e) {
            logger.error("Error in processFile : ", e);
        } catch (Exception ex) {
            logger.error("Error in processFile : ", ex);
        } finally {
            try {
                if (carrierId != null && process.startsWith("NewVes")) {
                    if ("false".equalsIgnoreCase(copyPrimary)) {
                        processLogger.recordProcessDetails(0, carrierId, PROCESS_NEWVES, "Processed/Emailed");
                    }
                }
            } catch (Exception e) {
                logger.error("Error in processFile : ", e);
            }
        }
    }


    protected void processLine(String msg, int lineNum) {
        try {
            if (msg.trim().length() < 3) {
                logger.debug("Empty line with length " + msg.trim().length());
                return;
            }
            msgHandler.setTextStr(msg);
            //logger.debug( "Processing line: " + msg);
            NewVes vesObj = (NewVes) msgHandler.getTextObj();
            String recType = StrUtil.trimQuotes(vesObj.getRecType());

            // A33, moved up VesselVisit update so it does not need a unit.
            if (recType.equalsIgnoreCase("vesselVisit")) {
                TVesselVisit vv = (TVesselVisit) msgHandler.getXmlObj();
                snxVesselVisit.getVesselVisit().clear();
                snxVesselVisit.getVesselVisit().add(vv);
				/*
				* alaska requirement
				* */
                if (vv.getVisitPhase() != null && "DEPARTED".equalsIgnoreCase(vv.getVisitPhase())) {
                    /**
                     * for departed vessel don't post it into N4, as Alaska manually want to depart vessels
                     *
                     * */
                    logger.info("The vessel visit " + vv.getId() + " is at phase " + vv.getVisitPhase() + ", so the vessel is not posted into N4");
                } else {
                    sendOneUnit(snxVesselVisit);
                    snxTopicObj.getVesselVisit().add(vv); // A45
                }
                //A38
                if (process.startsWith("NewVes") || process.startsWith("GuamNewVes")) { //A42
                    String rdsDate = StrUtil.trimQuotes(vesObj.getUfvFlex2()).trim();
                    String rdsTime = StrUtil.trimQuotes(vesObj.getUfvFlex3()).trim();
                    localRdsTriggerDtTime = convertHSTTimeZone(rdsDate + " " + rdsTime, "AST");
                    if (process.startsWith("NewVes") && "true".equalsIgnoreCase(copyPrimary)) {
                        rdsTriggerDtTime = convertHSTTimeZone(rdsDate + " " + rdsTime, "PST");
                    } else {
                        rdsTriggerDtTime = convertTimeZone(rdsDate + " " + rdsTime, "AST", "PST");
                    }
                    logger.debug("localRdsTriggerDtTime--- :" + localRdsTriggerDtTime);
                    logger.debug("rdsTriggerDtTime--- :" + rdsTriggerDtTime);
                }
                return;
            }

            if (recType.equalsIgnoreCase("unit")) {
                aUnit = (TUnit) msgHandler.getXmlObj();
                aUnit.setId(check.getCheckDigitUnit(aUnit.getId()));
                if (currUnit != null) {// send the last one before we create another
                    if (prodMode) {
                        //A10
                        currUnit.setTransitState(null);
                        currUnit.setSnxUpdateNote(process + currUnit.getSnxUpdateNote()); //A9
                    } else {
                        currUnit.setSnxUpdateNote(process); //A9
                        currUnit.setUniqueKey(currUnit.getId()); // A12
                        //A11
                        TosLookup lookup = null;
                        try {
                            lookup = new TosLookup();
                            TransitState state = lookup.getMostActiveTstate(currUnit.getId(), port);
                            if (state != null) {
                                logger.debug("Setting tstate=" + state.name);
                                currUnit.setTransitState(state.name);
                            } else {
                                logger.debug("No tstate found!");
                            }
                        } catch (Exception e) {
                            logger.error("Could not lookup TState", e);
                        } finally {

                            if (lookup != null) lookup.close();
                        }
                    }
                    try {
                        currUnit.setSnxUpdateNote(currUnit.getSnxUpdateNote() + unitSnxHolds());     //A24
                        //logger.debug("currUnit.setSnxUpdateNote::"+currUnit.getSnxUpdateNote());
                        clearBooking(currUnit);
                        sendOneUnit(snxObj);
                        unitCnt++;
                        addDcm(currUnit);

                        if (shippers.size() > 0) {
                            sendOneUnit(snxShipper);
                        }
                        if (consignees.size() > 0) {
                            sendOneUnit(snxConsignee);
                        }
                    } finally {
                        units.clear();
                        shippers.clear();
                        consignees.clear();
                        holdOnUnit = new StringBuffer(); //A24
                    }
                }
                currUnit = aUnit;
                units.add(aUnit);
                snxTopicObj.getUnit().add(aUnit);

                return;
            }
            if (aUnit == null) {
                logger.error("The aUnit object is null when adding a " + recType + ".");
                return;
            }
            if (recType.equalsIgnoreCase("equipment")) {
                TUnitEquipment anEquip = (TUnitEquipment) msgHandler.getXmlObj();
                List<TUnitEquipment> equipList = aUnit.getEquipment();
                if ("NIS Load Transaction".equals(process)) { //A50
                    anEquip.setValidate("false");
                }
                //A18 , Suppress non-containers.
                if ("Supplemental Data".equals(process) && !TEquipmentClass.CTR.equals(anEquip.getClazz())) {
                    logger.warn("Skipping equipment " + anEquip.getEqid() + " on unit " + aUnit.getId());
                } else {
                    logger.debug("Check id from " + anEquip.getEqid());
                    anEquip.setEqid(check.getCheckDigitUnit(anEquip.getEqid()));
                    aUnit.setSnxUpdateNote(aUnit.getSnxUpdateNote() + check.renumString);
                    //logger.debug("Snx note="+aUnit.getSnxUpdateNote());
                    equipList.add(anEquip);
                }

            } else if (recType.equalsIgnoreCase("hold")) {
                TFlags flags = (TFlags) msgHandler.getXmlObj();
                //A24
                holdOnUnit.append(((TFlags.Hold) flags.getHold().get(0)).getId() + ",");
                //logger.debug("holdOnUnit ::"+holdOnUnit);

                String applyTo = getHoldApplyTo(flags);
                if (applyTo == null) {
                    return;
                }
                if (applyTo.equalsIgnoreCase("unit")) {
                    TFlags unitFlags = aUnit.getFlags();
                    if (unitFlags == null) {
                        //logger.debug( "flags is null.");
                        aUnit.setFlags(flags);
                    } else {
                        unitFlags.getHold().addAll(flags.getHold());
                        unitFlags.getPermission().addAll(flags.getPermission());
                    }
                } else if (applyTo.equalsIgnoreCase("equipment")) {
                    List<TUnitEquipment> equipList = aUnit.getEquipment();
                    if (equipList.isEmpty()) {
                        logger.error("The equipment object is null when adding a hold.");
                        return;
                    }
                    // get the last equipment
                    TUnitEquipment anEquip = equipList.get(equipList.size() - 1);
                    TFlags eqFlags = anEquip.getFlags();
                    if (eqFlags == null) {
                        anEquip.setFlags(flags);
                    } else {
                        eqFlags.getHold().addAll(flags.getHold());
                        eqFlags.getPermission().addAll(flags.getPermission());
                    }
                } else {
                    logger.error(" Wrong applyTo: " + applyTo);
                }
            } else if (recType.equalsIgnoreCase("damage")) {
                List<TUnitEquipment> equipList = aUnit.getEquipment();
                if (equipList.isEmpty()) {
                    logger.error("The equipment object is null when adding a damage.");
                    return;
                }
                // get the last equipment
                TUnitEquipment anEquip = equipList.get(equipList.size() - 1);
                TDamage aDamage = (TDamage) msgHandler.getXmlObj();
                TDamages damList = anEquip.getDamages(); //A38
                if (damList == null) {
                    damList = new TDamages();
                    anEquip.setDamages(damList);
                }//A38
                if (aDamage.getSeverity() != null) {
                    damList.getDamage().add(aDamage);
                }

            } else if (recType.equalsIgnoreCase("hazard")) {
                THazards hazs = aUnit.getHazards();
                if (hazs == null) {
                    hazs = new THazards();
                    aUnit.setHazards(hazs);
                }
                List<THazard> hazList = hazs.getHazard();
                THazard aHaz = (THazard) msgHandler.getXmlObj();
                hazList.add(aHaz);
                // A27, remove generic hazard.
                hazList.remove(NewVesMessageHandler.genericHaz);
            } else if (recType.equalsIgnoreCase("oog")) {
                aUnit.setOog((TOog) msgHandler.getXmlObj());
            } else if (recType.equalsIgnoreCase("position")) {
                aUnit.setPosition((TPosition) msgHandler.getXmlObj());
                carrierId = aUnit.getPosition() != null ? aUnit.getPosition().getLocation() : "";   //A26
            } else if (recType.equalsIgnoreCase("reefer")) {
                aUnit.setReefer((TReeferRequirements) msgHandler.getXmlObj());
			/*	//A7 Starts
				TReeferRequirements reefer= (TReeferRequirements)msgHandler.getXmlObj();
				aUnit.setReefer(reefer);
				if(reefer.getTempReqdC() != null) {
					TReeferRecordingHistory reeferRecord = new TReeferRecordingHistory();
					reeferRecord.setTmpSetPt(reefer.getTempReqdC());
					XMLGregorianCalendar cal = DatatypeFactory.newInstance().newXMLGregorianCalendar();
					//A7- reeferRecord.setTimeOfRecording(cal);
					TReeferRecordingHistories reeferList = new TReeferRecordingHistories();

					aUnit.setReeferRecordingHistories(reeferList);
					reeferList.getReeferRecordingHistory().add(reeferRecord);

					reefer.setTempReqdC(null);
				}
			*/    //A7 Ends

            } else if (recType.equalsIgnoreCase("routing")) {
                TRouting aRouting = (TRouting) msgHandler.getXmlObj();
                aUnit.setRouting(aRouting);
            } else if (recType.equalsIgnoreCase("carrier")) {
                TRouting.Carrier aCarrier = (TRouting.Carrier) msgHandler.getXmlObj();
                //A44
                if ("NewVes".equals(process)) {
                    aCarrier.setFacility("ANK");
                } else if ("GuamNewVes".equals(process)) {
                    aCarrier.setFacility("GUM");
                }
                TRouting aRouting = aUnit.getRouting();
                if (aRouting == null) {
                    logger.error("Routing object is null when adding a carrier.");
                    return;
                }
				/*if("Supplemental Data".equals(process)){
					//&& aCarrier.getDirection().equals(TDirection.OB) ) {
					logger.info("Skipping OB carrier for Supplemental "+aUnit.getId() );
				} else {*/

                // Commented above code on Aug 12, 2013, by Karthik Rajendran.
                // Purpose: Carrier elements to be populated for supplemental data too, bcz units routing service gets overwritten with null
                aRouting.getCarrier().add(aCarrier);
                //}

            }//1.5.N Change Starts
            else if (recType.equalsIgnoreCase("handling")) {
                aUnit.setHandling((TUnitHandling) msgHandler.getXmlObj());
            } else if (recType.equalsIgnoreCase("contents")) {
                aUnit.setContents((TUnitContents) msgHandler.getXmlObj());

                if (aUnit.getContents().getShipperId() != null && aUnit.getContents().getShipperName() != null) {
                    TShipperConsignee shipper = new TShipperConsignee();
                    shipper.setId(aUnit.getContents().getShipperId());
                    shipper.setName(aUnit.getContents().getShipperName());
                    shippers.clear(); //A30
                    shippers.add(shipper);
                    snxTopicObj.getShipperConsignee().add(shipper); //A43
                }

                if (aUnit.getContents().getConsigneeId() != null && aUnit.getContents().getConsigneeName() != null) {
                    TShipperConsignee consignee = new TShipperConsignee();
                    consignee.setId(aUnit.getContents().getConsigneeId());
                    consignee.setName(aUnit.getContents().getConsigneeName());

                    //consignee address
                    TContactInfo contactInfo = new TContactInfo();
                    contactInfo.setAddressLine1(StrUtil.trimQuotes(vesObj.getConsigneeAddr()));
                    contactInfo.setAddressLine2(StrUtil.trimQuotes(vesObj.getConsigneeSuite()));
                    contactInfo.setCity(StrUtil.trimQuotes(vesObj.getConsigneeCity()));
                    contactInfo.setState(StrUtil.trimQuotes(vesObj.getConsigneeState()));
                    contactInfo.setCountry(StrUtil.trimQuotes(vesObj.getConsigneeCountry()));
                    contactInfo.setPostalCode(StrUtil.trimQuotes(vesObj.getConsigneeZipCode()));

                    consignee.setContactInfo(contactInfo);
                    consignees.clear(); // A30
                    consignees.add(consignee);
                    snxTopicObj.getShipperConsignee().add(consignee); //A43
                }

            } else if (recType.equalsIgnoreCase("seals")) {
                aUnit.setSeals((TUnitSeals) msgHandler.getXmlObj());
            } else if (recType.equalsIgnoreCase("etc")) {
                aUnit.setUnitEtc((TUnitEtc) msgHandler.getXmlObj());
            } else if (recType.equalsIgnoreCase("ufv-flex")) {
                aUnit.setUfvFlex((TUfvFlexFields) msgHandler.getXmlObj());
            } else if (recType.equalsIgnoreCase("unit-flex")) {
                aUnit.setUnitFlex((TUnitFlexFields) msgHandler.getXmlObj());
            } else if (recType.equalsIgnoreCase("booking")) {
                aUnit.setBooking((TBkg) msgHandler.getXmlObj());
            } else if (recType.equalsIgnoreCase("timestamps")) {
                aUnit.setTimestamps((TUfvTimestamps) msgHandler.getXmlObj());
            }//1.5.N Change Ends
            //
            else {
                logger.debug("Wrong message type: " + recType);
            }
        } catch (TosException tex) {
            logger.error("The error line num = " + lineNum);
            logger.error("The error line = " + msg);
            logger.error("Error in text message converting: ", tex);//A2
            EmailSender.mailAttachment(supportMail, supportMail, MAIL_HOST, "NewVesErrorRecord.TXT", msg, "EXCEPTION \n" + tex, " NewVes Process Error Record");
        } catch (Exception ex) {
            logger.error("The error line num = " + lineNum);
            logger.error("The error line = " + msg);
            logger.error("Error in text message converting: ", ex);//A2
            EmailSender.mailAttachment(supportMail, supportMail, MAIL_HOST, "NewVesErrorRecord.TXT", msg, "EXCEPTION \n" + ex, " NewVes Process Error Record");
        }
    }

    /*
     * A43 - Commented out entry for snxMailBox
     */
    private void sendOneUnit(Snx snxObj) {
        // we should have the snx object now.
        try {
            if (!postNewvesToQueue) {
                logger.debug("Comming SendOneUnit Into The Return");
                return;
            }//A500

            //A42 - DONT POST GUM SNX TO N4
            if ("GuamNewVes".equals(process)) {
                logger.debug("Guam Unit Skipped Posting");
                return;
            }

            snxMsgHandler.setXmlObj(snxObj);
            String xmlStr = snxMsgHandler.getXmlStr();
            logger.debug("XML message(" + postingDelay + "): " + xmlStr);
            Thread.sleep(postingDelay);
            sender.send(xmlStr);

            //A1
            if (xmlTestFlag != null) {
                out.write(xmlStr);
                out.write("\r\n");
            }
        } catch (TosException tex) {
            logger.error("Tos exception found: ", tex);
            tex.printStackTrace();
        } catch (JMSException jex) {
            logger.error("JMS exception found: ", jex);
            jex.printStackTrace();

        } catch (Exception ex) {
            logger.error("Exception found: ", ex);
            ex.printStackTrace();
        }
    }

    /*
     * MNS HON and GUM NewVes SNX Posting Added 09-16-11 (A42)
     * Change Approach after DAS testing Didnt Work
     */
    private void sendToMNS(Snx snxObj, TUnit mnsUnit) {
        logger.info("NewVesFileProcessor.sendToMNS started");
        try {
            String n4MnsNivSaf = TosRefDataUtil.getValue("N4_MNS_NVI_SAF");
            String mnsNviFlgStr = TosRefDataUtil.getValue("N4_MNS_NVI_FLAG");
            boolean mnsNviFlg = Boolean.parseBoolean(mnsNviFlgStr);

            if (mnsUnit != null) {
                List<TUnit> localUnits = snxObj.getUnit();
                if (localUnits != null && localUnits.size() > 0) {
                    for (int i = 0; i < localUnits.size(); i++) {
                        TUnit localUnit = localUnits.get(i);
                        if (localUnit != null && localUnit.getSnxUpdateNote() != null && localUnit.getSnxUpdateNote().contains("rdsDtTime")) {
                            localUnit.setSnxUpdateNote(mnsUnit.getSnxUpdateNote());
                            logger.debug("MNSlocalUnit.SnxUpdateNote---" + localUnit.getSnxUpdateNote());
                        }
                    }
                }
            }

            snxMsgHandler.setXmlObj(snxObj);
            String xmlStr = snxMsgHandler.getXmlStr();
            //logger.debug("MNSxmlStr---"+xmlStr);
            if (mnsNviFlg) {
                JMSSender sender = new JMSSender(n4MnsNivSaf, false);
                sender.send(xmlStr);
            }

        } catch (TosException tex) {
            logger.error("Tos exception found: ", tex);
            tex.printStackTrace();
        } catch (JMSException jex) {
            logger.error("JMS exception found: ", jex);
            jex.printStackTrace();

        } catch (Exception ex) {
            logger.error("Exception found: ", ex);
            ex.printStackTrace();
        }
        logger.info("NewVesFileProcessor.sendToMNS stopped");
    }

    private void sendTopic(Snx snxObj) {
        // turned off topic for now.
        // turned it on again.
        //if(true) return;
        // we should have the snx object now.
        logger.info("NewVesFileProcessor.sendTopic started");
        try {
            snxMsgHandler.setXmlObj(snxObj);
            String xmlStr = snxMsgHandler.getXmlStr();
            //logger.debug( "XML message sent to topic: " + xmlStr);
            topicSender.send(xmlStr);
        } catch (TosException tex) {
            logger.error("Tos exception found: ", tex);
            tex.printStackTrace();
        } catch (JMSException jex) {
            logger.error("JMS exception found: ", jex);
            jex.printStackTrace();
        } catch (Exception ex) {
            logger.error("Exception found: ", ex);
            ex.printStackTrace();
        }
        logger.info("NewVesFileProcessor.sendTopic stopped");
    }

    private String getHoldApplyTo(TFlags flags) {
        if (flags == null)
            return null;

        if (flags != null) {
            if (flags.getHold().size() == 0) {
                logger.debug("No hold ID found in flags");
                return null;
            }
            String holdId = ((TFlags.Hold) flags.getHold().get(0)).getId();
            TosHoldPermData holdObj = TosRefDataUtil.getHoldPermObj(holdId);
            if (holdObj == null) {
                logger.debug("Can not find appyTo for hold: " + holdId);
                return null;
            }
            return holdObj.getApplyTo();

        } else {
            logger.error("The flags obj is null");
            return null;
        }
    }

    /**
     * Add DCM only used in prod mode.
     */
    private void addDcm(TUnit unit) {
        if (prodMode && unit.getHazards() != null && unit.getHazards().getHazard().size() > 0) {
            Iterator<THazard> iter = unit.getHazards().getHazard().iterator();
            while (iter.hasNext()) {
                THazard hazard = iter.next();
                double weight = 0;
                try {
                    weight = hazard.getWeightKg().doubleValue();
                } catch (Exception e) {

                }
                String vesvoy = null;
                if (unit.getPosition() != null) {
                    vesvoy = unit.getPosition().getLocation();
                }


				/*
				String vessel = null;
				String voyage = null;
				if(vesvoy != null && vesvoy.length() > 3 ) {
					vessel = vesvoy.substring(0,3);
					voyage = vesvoy.substring(3);
				} else {
					vessel = vesvoy;
				}*/
                DcmConvert convert = new DcmConvert();
                if (unit.getRouting() != null) convert.setDestPort(unit.getRouting().getDestination());
                convert.setCnSeq("" + containerCnt);
                convert.setCtrNo(unit.getId());
                convert.setHazClass(hazard.getImdg());
                if (unit.getContents() != null) convert.setConsigneeName(unit.getContents().getShipperName());
                convert.setPack(hazard.getQuantity() + " " + hazard.getPackageType());
                convert.setHazDesc1(getDesc(hazard));
                convert.setEmergencyContact(hazard.getEmergencyTelephone());
                convert.setGrossWeight("" + Math.round(weight * 2.20462262));
                if (unit.getPosition() != null) convert.setCellLocation(unit.getPosition().getSlot());
                convert.setCarsFlag(getCarFlag(unit, hazard));
                convert.setHazCode("" + hazard.getUn());
                //convert.setHazCodeType("UN");//A35
                convert.setHazCodeType(hazard.getHazNbrType() != null ? hazard.getHazNbrType().name() : "UN");
                convert.setVesvoy(vesvoy);

                dcmHandler.addDcmLine(convert, vesvoy, "");
            }
            containerCnt++;


        }
    }

    private String getCarFlag(TUnit unit, THazard hazard) {
        if (unit == null || unit.getContents() == null || unit.getContents().getCommodityName() == null) return "F";
        if (unit.getContents().getCommodityName().toLowerCase().contains("auto")) return "T";
        if (hazard.getPackageType() != null && hazard.getPackageType().toLowerCase().contains("auto")) return "T";
        return "F";
    }

    private String getDesc(THazard hazard) {
        boolean hasImo1 = hazard.getSecondaryImo1() != null && hazard.getSecondaryImo1().length() > 0;
        boolean hasImo2 = hazard.getSecondaryImo2() != null && hazard.getSecondaryImo2().length() > 0;

        String desc = hazard.getProperName();
        desc += hazard.getTechName() == null ? "" : ", (" + hazard.getTechName() + ")";
        desc += hazard.getImdg() == null ? "" : ", " + hazard.getImdg().trim();
        desc += (hasImo1 || hasImo2) ? ", (" : "";
        desc += hazard.getSecondaryImo1() != null ? hazard.getSecondaryImo1() : "";
        desc += " ";
        desc += hazard.getSecondaryImo2() != null ? hazard.getSecondaryImo2() : "";
        desc += (hasImo1 || hasImo2) ? ")" : "";
        desc += " " + hazard.getUn();
        desc += hazard.getPackingGroup() != null ? " " + hazard.getPackingGroup() : "";
        desc += hazard.getMarinePollutants() != null ? ", MARINE POLLUTANTS" : "";
        desc += "T".equals(hazard.getLtdQtyFlag()) ? ", Ltd Qty" : "";

        return desc;

    }

    /**
     * Clear the Booking for Hawaii to Hawaii shipping.
     *
     * @param unit
     */
    private void clearBooking(TUnit unit) {
        if (unit.getRouting() == null) return;

        String opl = unit.getRouting().getOpl();
        String pod = unit.getRouting().getPod1();

        if (TerminalInfo.isHawaiiPort(opl) && TerminalInfo.isHawaiiPort(pod)) {
            unit.setBooking(null);
        }
    }

    private String unitSnxHolds(){
        String snxHolds = "";
        try{
            //logger.debug("Holds unit Buff :"+holdOnUnit);
            if(holdOnUnit.length() > 0 && holdOnUnit.length()!=1){
                String holdsMsg = holdOnUnit.substring(0,holdOnUnit.length()-1);
                snxHolds = " Holds:"+"("+holdsMsg+")";
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return snxHolds;
    }
    /**
    private String unitSnxHolds() {
        String snxHolds = "";
        //String snxHoldId = getSnxHoldId();
        try {
            //logger.debug("Holds unit Buff :"+holdOnUnit);
            if (holdOnUnit.length() > 0 && holdOnUnit.length() != 1) {

                String holdsMsg = holdOnUnit.substring(0, holdOnUnit.length() - 1);
                if (snxHoldId != null) {
                    snxHolds = " Holds:" + "(" + snxHoldId + "," + holdsMsg + ")";
                } else {
                    snxHolds = " Holds:" + "(" + holdsMsg + ")";
                }
            } else {
                if (holdOnUnit.length() == 0 && snxHoldId != null) {
                    snxHolds = " Holds:" + "(" + snxHoldId + ")";
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return snxHolds;
    }**/
    /**
    private String getSnxHoldId() {

        if (!process.startsWith("NewVes")) {
            logger.info("process is not a new vess hence skipping the hold info");
            return null;
        }

        TosHoldPermData holdObj = TosRefDataUtil.getHoldPermObj(NewVesMessageHandler.snxHoldAcetsCode);

        if (holdObj == null) {
            logger.debug("Can not apply for hold: " + NewVesMessageHandler.snxHoldAcetsCode);
            return null;
        }
        return holdObj.getSn4Id() != null ? holdObj.getSn4Id() : null;
    }
     **/

    public static String convertTimeZone(String dateStr, String fromTimeZone, String toTimeZone) {
        String convertDateStr = null;
        try {
            DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            formatter.setTimeZone(TimeZone.getTimeZone(fromTimeZone));
            Date date = formatter.parse(dateStr);
            logger.debug("HST FMT Date:" + formatter.format(date));
            formatter.setTimeZone(TimeZone.getTimeZone(toTimeZone));
            logger.debug("PST FMT Date:" + formatter.format(date));
            convertDateStr = formatter.format(date);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return convertDateStr;

    }


    public static String convertHSTTimeZone(String dateStr, String fromTimeZone) {
        String localConvertDateStr = null;
        try {
            DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            formatter.setTimeZone(TimeZone.getTimeZone(fromTimeZone));
            Date date = formatter.parse(dateStr);
            localConvertDateStr = formatter.format(date);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return localConvertDateStr;
    }

    //A43
    private void sendftp(Snx snxObj) {
        // turned off ftp for now.
        // turn it on 2.1
        int ftpId = 0;
        String fileName = null;
        logger.info("process in sendFtp " + process);
        try {
            snxMsgHandler.setXmlObj(snxObj);
            String xmlStr = snxMsgHandler.getXmlStr();
            logger.debug("-- NEWVES SNXMAIL BOX XML POSTED -- ");
            ftpId = SNX_FTP_ID;
            String snxSubDir = findPortFTPToPostSNX(snxObj);
            logger.info("SNX files placed in DIR : " + snxSubDir);
            if ("NewVes".equals(process)) {
                fileName = carrierId + "NV_";
            } else if ("NIS Load Transaction".equals(process)) {
                fileName = carrierId + "NLT_";
            } else if ("Supplemental Data".equals(process)) {
                String date = CalendarUtil.dateFormat("MMddyyyy");
                fileName = date + "SUP_";
            } else if ("GuamNewVes".equals(process)) { // A49
                fileName = carrierId + "GUM_";
            }
            FileWriterUtil.writeFile(xmlStr, storeDir, fileName, ".xml");
            if (StringUtils.isNotBlank(snxSubDir)) {
                ftpSender.sendFile(ftpId, fileName + ".xml", xmlStr.getBytes(), true, snxSubDir);
            }

        } catch (TosException tex) {
            logger.error("Tos exception found: ", tex);
            tex.printStackTrace();
        } catch (Exception ex) {
            logger.error("Exception found: ", ex);
            ex.printStackTrace();
        }
    }

    private void sendftpNew(Snx snxObj) {
        logger.info("process in sendFtp " + process);
        try {
            splitSnxShipperAndUnits(snxObj);
        } catch (Exception ex) {
            logger.error("Exception found: ", ex);
            ex.printStackTrace();
        }
    }

    private void sendftpForPort(Snx snxObj, String snxSubDir) {
        // turned off ftp for now.
        // turn it on 2.1
        int ftpId = 0;
        String fileName = null;
        logger.info("process in sendFtp " + process);
        try {
            snxMsgHandler.setXmlObj(snxObj);
            String xmlStr = snxMsgHandler.getXmlStr();
            logger.debug("-- NEWVES SNXMAIL BOX XML POSTED -- ");
            ftpId = SNX_FTP_ID;
            logger.info("SNX files placed in DIR : " + snxSubDir);
            String date = CalendarUtil.dateFormat("MMddyyyyhhmmssSSS");
            if ("NewVes".equals(process)) {
                fileName = carrierId + "NV_" + date;
            } else if ("Supplemental Data".equals(process)) {
                fileName = snxSubDir + "_" + "SUP_" + date;
            }
            FileWriterUtil.writeFile(xmlStr, storeDir, fileName, ".xml");
            if (StringUtils.isNotBlank(snxSubDir)) {
                ftpSender.sendFile(ftpId, fileName + ".xml", xmlStr.getBytes(), true, snxSubDir);
            }

        } catch (TosException tex) {
            logger.error("Tos exception found: ", tex);
            tex.printStackTrace();
        } catch (Exception ex) {
            logger.error("Exception found: ", ex);
            ex.printStackTrace();
        }
    }

    /**
     * This method will split the SNX file into multiple, based on the Vessel's current facility
     * and then POD of the unit onboard.
     *
     * @param snxObj
     */
    private void splitSnxShipperAndUnits(Snx snxObj) {
        if (snxObj != null && snxObj.getUnit() != null && snxObj.getUnit().size() > 0) {
            logger.info("Finding current port for the vessel ");
            if (process.startsWith("NewVes")) {
                sendftpForPort(snxObj, ANK);
            } else {
                Snx shipCon = new Snx();
                for (TShipperConsignee shipperConsignee : snxObj.getShipperConsignee()) {
                    logger.info("Get Shipper Consignee : " + shipperConsignee.getName());
                    shipCon.getShipperConsignee().add(shipperConsignee);
                }
                sendftpForPort(shipCon, ANK);
                seggregateUnitForPorts(snxObj);
            }
			/*if (ANK.equalsIgnoreCase(portToPost))	{
				logger.info("This vessel is in ANK, so posting in ANK only");
				sendftpForPort(snxObj, ANK);
			} else if (KDK.equalsIgnoreCase(portToPost)) {
				logger.info("This vessel is in KDK, so posting in ANK, KDK");
				Snx ankSnx = new Snx();
				Snx kdkSnx = new Snx();
				logger.info("Total units in SNX >>> "+snxObj.getUnit().size());
				logger.info("Total Shipper Consignees in SNX >>> "+snxObj.getShipperConsignee().size());
				for (TUnit unit : snxObj.getUnit()) {
					logger.info("get Unit : "+unit.getId());
					logger.info("Get POD : "+unit.getRouting().getPod1());
					if (ANK.equalsIgnoreCase(unit.getRouting().getPod1())) {
						ankSnx.getUnit().add(unit);
					} else {
						kdkSnx.getUnit().add(unit);
					}
				}
				sendftpForPort(ankSnx, ANK);
				sendftpForPort(kdkSnx, KDK);
			} else if (DUT.equalsIgnoreCase(portToPost)) {
				logger.info("This vessel is in DUT, so posting in ANK, KDK and DUT");
				Snx ankSnx = new Snx();
				Snx kdkSnx = new Snx();
				Snx dutSnx = new Snx();
				logger.info("Total units in SNX >>> "+snxObj.getUnit().size());
				logger.info("Total Shipper Consignees in SNX >>> "+snxObj.getShipperConsignee().size());
				for (TUnit unit : snxObj.getUnit()) {
					logger.info("get Unit : "+unit.getId());
					logger.info("Get POD : "+unit.getRouting().getPod1());
					if (ANK.equalsIgnoreCase(unit.getRouting().getPod1())) {
						ankSnx.getUnit().add(unit);
					} else if (KDK.equalsIgnoreCase(unit.getRouting().getPod1())){
						kdkSnx.getUnit().add(unit);
					} else {
						dutSnx.getUnit().add(unit);
					}
				}
				sendftpForPort(ankSnx, ANK);
				sendftpForPort(kdkSnx, KDK);
				sendftpForPort(dutSnx, DUT);
			}*/
        }
    }

    private void seggregateUnitForPorts(Snx snxObj) {
        Snx ankSnx = new Snx();
        Snx kdkSnx = new Snx();
        Snx dutSnx = new Snx();
        try {
            String carrierIdForPort = "";
            String portToPostUnit = "";
            TosLookup lookup = null;
            logger.info("Total Units to be posted to all facility  >>> " + snxObj.getUnit().size());
            for (TUnit unit : snxObj.getUnit()) {
                try {
                    TRouting routing = unit.getRouting();
                    if (routing != null) {
                        List<TRouting.Carrier> carriers = routing.getCarrier();
                        logger.info("Finding Vessel Visit to find Active Unit facility for : " + unit.getId());
                        for (TRouting.Carrier carrier : carriers) {
                            if (TDirection.IB.value().equalsIgnoreCase(carrier.getDirection().value())) {
                                if ("ACTUAL".equalsIgnoreCase(carrier.getQualifier())) {
                                    carrierIdForPort = carrier.getId();
                                    break;
                                } else {
                                    carrierIdForPort = carrier.getId();
                                }
                            }
                        }
                        logger.info("Found Carrier id to post unit : " + carrierIdForPort);
                    }
                    lookup = null;
                    lookup = new TosLookup();
                    portToPostUnit = lookup.getUnitActiveFacility(unit.getId(), carrierIdForPort);
                    logger.info(unit.getId() + " will be posted to : " + portToPostUnit);
                    if (portToPostUnit != null && !portToPostUnit.isEmpty()) {
                        if (ANK.equalsIgnoreCase(portToPostUnit)) {
                            ankSnx.getUnit().add(unit);
                        } else if (KDK.equalsIgnoreCase(portToPostUnit)) {
                            kdkSnx.getUnit().add(unit);
                        } else if (DUT.equalsIgnoreCase(portToPostUnit)) {
                            dutSnx.getUnit().add(unit);
                        }
                    }
                } catch (Exception e) {
                    logger.error("Could not lookup Current Vessel Facility : ", e);
                } finally {
                    if (lookup != null) lookup.close();
                }
            }
            logger.info("Total Units posted to ANK >>> " + ankSnx.getUnit().size());
            logger.info("Total Units posted to KDK >>> " + kdkSnx.getUnit().size());
            logger.info("Total Units posted to DUT >>> " + dutSnx.getUnit().size());
            if (ankSnx.getUnit().size() > 0) {
                sendftpForPort(ankSnx, ANK);
            }
            if (kdkSnx.getUnit().size() > 0) {
                sendftpForPort(kdkSnx, KDK);
            }

            if (dutSnx.getUnit().size() > 0) {
                sendftpForPort(dutSnx, DUT);
            }
        } catch (Exception e) {
            logger.error("seggregateUnitForPorts : Could not seggregate  Facility : ", e);
        }
    }


    private String findPortFTPToPostSNX(Snx snxObj) {
        String portToPostSNX = "";
        String carrierIdForPort = null;
        logger.debug("Finding Port to Post UNit : " + snxObj.getUnit());
        if (snxObj.getUnit() != null) {
            TUnit unit = (TUnit) snxObj.getUnit().get(0);
            TRouting routing = unit.getRouting();
            if (routing != null) {

                List<TRouting.Carrier> carriers = routing.getCarrier();
                logger.info("Finding Port to Post Carriers : " + carriers);
                for (TRouting.Carrier carrier : carriers) {
                    if (TDirection.IB.value().equalsIgnoreCase(carrier.getDirection().value())
                            && "ACTUAL".equalsIgnoreCase(carrier.getQualifier())) {
                        carrierIdForPort = carrier.getId();
                        break;
                    } else {
                        carrierIdForPort = carrier.getId();
                    }

                }
                logger.info("Finding Port to Post Carrier id : " + carrierIdForPort);
            }
        }
        if (carrierIdForPort != null) {
            this.carrierId = carrierIdForPort;
            TosLookup lookup = null;
            logger.info("Finding Port to Post Carrier id : " + carrierIdForPort);
            try {
                lookup = new TosLookup();
                if (process.startsWith("NewVes")) {
                    portToPostSNX = ANK;
                } else {
                    portToPostSNX = lookup.getVesselCurrentFacility(carrierIdForPort);
                }
                logger.info("Finding Port to Post portToPostSNX : " + portToPostSNX);
            } catch (Exception e) {
                logger.error("Could not lookup Current Vessel Facility : ", e);
            } finally {
                if (lookup != null) lookup.close();
            }
        }
        return portToPostSNX;
    }

    /*	public static void main(String args[]){
            try{
                String date = convertTimeZone("12/21/2009 10:37:00", "HST", "PST");
                System.out.println("date=:"+date);
                DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                Date date1 = formatter.parse("12/21/2009 10:37:00");
                System.out.println("date1=:"+date1);

                DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
                Date dt1 = formatter.parse("12/30/2009");
                Date dt2 = formatter.parse("01/05/2010");
                long days = dt2.getTime() - dt1.getTime();
                System.out.println(" days="+(int)days/1000/60/60/24);


                Date dtOne = new Date(new SimpleDateFormat("MM/dd/yyyy").format(dt1));
                Date dtTwo = new Date(new SimpleDateFormat("MM/dd/yyyy").format(dt2));
                long daysFinal = dtTwo.getTime() - dtOne.getTime();
                System.out.println(" days="+(int)daysFinal/1000/60/60/24);


            //Date dt	= new Date (new Date().getTime() - (1000 * 60 * 60 *24));
            //System.out.println("dt : "+dt);



            }catch(Exception e){
                e.printStackTrace();
            }
        }	*/
    public static <TUnit> TUnit deepCopyJAXB(TUnit object, Class<TUnit> clazz) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
            JAXBElement<TUnit> contentObject = new JAXBElement<TUnit>(new QName(clazz.getSimpleName()), clazz, object);
            JAXBSource source = new JAXBSource(jaxbContext, contentObject);
            return jaxbContext.createUnmarshaller().unmarshal(source, clazz).getValue();
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    public static <TUnit> TUnit deepCopyJAXB(TUnit object) {
        if (object == null) throw new RuntimeException("Can't guess at class");
        return deepCopyJAXB(object, (Class<TUnit>) object.getClass());
    }

}
