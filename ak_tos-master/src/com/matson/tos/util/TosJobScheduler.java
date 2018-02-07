/*
*********************************************************************************************
*Srno   Date			AuthorName			Change Description
* A1    20/12/07        Glenn Raposo		Changes made to incorporate DcmConverterFileProcessor
* A2    05/12/08		Glenn Raposo		Changes made to incorporate FTPWriter
* A3    11/07/08        Glenn Raposo		Changes made to incorporate FTPWriterCmis
* A4    01/08/09		Steven Bauer		Added Cancel Gate Job
* A5    03/02/09		Steven Bauer		Changed MQ to allow sub minute running time.
* A6	08/20/09		Steven Bauer		Reduce the number of objects being created.
* A7    01/06/2009      Glenn Raposo		Adding Task For CVS Report.
* A8    03/03/2010      Glenn Raposo		Removed vesselvisit Task added FSS->N4
* A9    04/19/2010      Glenn Raposo		Added back Cmis Schedule lookup item A8
* A10   10/08/2010      Glenn Raposo	    Remove FSS Cmis Dir Pooling
* A11   11/14/2011      Glenn Raposo	    Moved CVS hourly & CANCEL_GATE_TRAN to N4-2.1 Groovy Jobs
*********************************************************************************************
*
**********************************************************************************************
*Release Notes
* 1.6.17.4		Filter error log
* 				Add barge setting to stif.
* 1.6.17.4-1	Added Direction check to CAR and ARD messages, copied from BDB.
* 1.6.17.4-2    Fix for duplicate bookings.
* 1.6.14.4-3    Add default F40 to bookings if not provided by acets.
* 1.6.17.6-2    Prelim/Final on DCM Email
*               ABOB MTY/FCL Fix.
* 1.6.17.6-3    Remove \t from STIF and New Vess Unit Remarks.
*               Cargo status header change
*               BDA remapping for dest/disch.
*               Time zone fix for det.
* 1.6.17.8      Null pointer fix on NewVess
* 1.6.17.8-1	Check digit fix if New vess is longer than Stif.
* 1.6.17.8-2	Enabled the manifest.
* 				Added fix for finindg equipment more percisely.
* 1.6.17.8-2    Added Code to append Holds on the SnxNotes for Newves and Supplemental
* 1.6.17.8-3    159-Add Inbound and Outbound to the subject area or DCM
* 1.6.17.10     Added Dept unit Fields to the CNTR mapping xml,
                Carrier Id Correction, IGT BKG Temp Descp mail Id correction.
                Added hazardFlag
                Added checkdigit lookup to upu.
			    Added Acets Hold Notes.
			    410 - Add Message to STIF for BL Nbr field �DO NOT EDIT � WAIT FOR NEWVES�.
* 1.6.17.12      Fixed problem with lost database connection when ending scheduled classes.
* 				Fixed RefData memory leak.
* 			  	Check if the vessel schedule needs to be updated to reduce process time.
* 1.6.17.13		Added Error database log
* 				Don't pass I/B carrier on supplimental
* 				Fixed bug in renumber code.
* 				Special implementation of BOB_HOLD that will handle no xmlObj created withouyt an exception.
* 1.6.18		Added error message jsp.
* 			    Fixed bug in New Ves.
* 1.6.18.1	    Throw out all ULK error message.
* 				STIF, Only apply hazard for CLI group coding.
* 				Fixed Update where activeVO == WORKING and visitVO == Departed,
* 				Was updating to departed, now leave it as Working.
* 				Filter IMDG for multiple Xs on BOB messages
* 1.6.18.2		Vessev visit change,
* 				Pass the Phase_VV events on Schedule update.
* 1.6.18.3		Email fix for certain exceptions.
* 1.6.18.4		Added different # days for long hauls and barges.
*			    Using Hazard update for late dcm instead of unit update.
*               Updated Newves Email Notification list for long hauls and barges
*               Auto advance HRZ service vessels in vessel schedule.
* 1.6.18.6	    1. Reduce the number of objects being created to help reduce garbage collection.
* 				2. Change N4 to CMIS to use a temporary directory on the App server rather than a temporary
* 				ftp site.  This will remove the dependence on the iplanet server and improve performance.
* 				3. EP000099343:  Ignore certain error messages if this unit exists but it has already departed.
* 1.6.18.7  	Improved speed of MQ Readers.
* 1.6.19		More performance improvement.
*               Acets Error Mailing Update #102578
*				Update Hibernate version
* 1.6.18.8		Removed Fix for time offset in newvess as 1.6.19 is not going in for a while.
* 1.6.19.1		FSS->N4
* 1.6.19.2		Enabling MDB.
* 1.6.19.3		Disable MDB.
* 1.6.20		Improved check digit to handle only single digit check digits (if more than 1 digit it will ignore it).
*               Fixed connection leak.
* 1.6.20.1		Added SNX Processor.
* 				Added timeout for Cas Ftp.
* 2.0           Adding Task For CVS Report
*               Changes for Ag Manifest Report.
* 2.1           Newves EOF, Damage, RDS Date Change
*               FSS - YB Itinerary prefix change
*               AG - NON Cntr Change
* 2.1.1         DCM Multiple File Issue - Reset Global variable
                CVS HOURLY - One the Hour Fix
* 2.1.2         FSS->N4 Enabling
* 2.1.3         Mapped Cmis Action Before Posting message to Topic
                RDSDataTime Index Limit
*               ChassRfid Processor
*               Added NULL check to Data Fields before Inserting into DB
*               FSS->N4 Changes (1.write in and out File 2.Remove portStatusCode
*                                3. Generated snx for Date change 4. Jump Phases)
*               Ag manifest to Read Data for RefData
* 2.1.5         ITN BKG Change
*               Check for Newves Notification Email & Vessel visit change to Process cmis File
* 2.1.6         BKG Reefer Temp WA, Bkg WaitTime lookup variable
                Leg and Action value to be computed in the GroovyProcessorClass
* 2.1.6.1		updated localhost to tosJmsServer in weblogic-ejb.xml
                RDS AM/PM comversion on MdbSn4
*
* 2.1.7         Handling GEMS JMS Queue
*               Tos Major feature added to UP2 & UP3
*				Add Gvpq and NIT message (CmisOutMessageProcessor)
                Removed FSS (Cmis Area polling)
				FSS Timezone conversion Fix
*               BDC/FREE Transaction for NIT
*               Added Topic Posting Rule Method in GroovyProcessorClass
* 2.1.7.1       XT hold to AG DEPT
*               manifest Error mail Handling
*				Handel last NULL Rec in Cmis File
*               PAH vessel set Service

* 2.1.8.0		Added a delay to the new vess,Stif proc
* 				Handled Bkg Line Operator Change
* 2.1.8.2		Gems msg Posting to Topic and Queue now
* 				Added JMS sender Method to post to queue
*               PadZero to VPU and VPS dates
*               N4_GEMS Flag Switch
* 2.1.8.6       Setup JMS Distributed Queue
                change to weblogic-ejb-jar.xml and JmsSender Class
				Stif pause variable added
* 2.1.8.8       Set leg value to null
				trucker updated to Cmis
* 2.1.8.9       Added Xml Escape Char for Trucker Msg
* 2.1.10         Set Gems MDB ThreadPool size to 1 (TRU Multiple issue)
                Added leg value for NIS ports
				Stopped Logging Xml into Archive table
* 3.0.0         Port Authority of Guam Gate Transaction
                Ref Date Trucker and Vessel table to Support Gum Change
                Stop Archieving Xml in Table
				Booking Equipment for Open-Top
				Manifest Skip Routing Error
				Hazardous Insert Function For Spreadsheet
* 3.0.2         Port of Guam Pod Fix and Booking Fix
                Reading Haz Insert spreadsheet from new FTP area
                Limiting 35Char to the LateDcm Emergency Nbr in snx
                Set Booking equipSize V28 to V40 on BOB transaction
*3.0.4          PAOG Guam Issues Addressed 1) Ingate Chassis as %
                2) Map Me as FE  3) Post File to Guam users
                4) Set Destination as Discharge port 5) Map PacificIsland Movers
*3.0.5          PAOG Guam Issues Addressed 1) Ingate Chassis as %
*3.0.7          PAOG Guam Issues Addressed
*3.0.8          PAOG Guam Issues Addressed
*3.0.9          MNS Post Barge & LH NIS Newves Alert
 			    MNS Post Gum Newves to Newves Topic
*3.0.10         MNS HON and GUM Newves Approached change to Pass to QUEUE
*3.1.0          TOS2.1 a) new xsd b) snx mail box [newves,stif proc]  c)fixed bob-hpu
*3.1.1          TOS2.1 a) FSS Change b)IB Facility in Snx c)Move Groovy Jobs d)Casftp update
*               d) Suppress Posting Trucker LifeCycle
*3.1.2          TOS2.1 a) Added Kulana Assign Method to posistion webservice
*               b)Wait on LNK and ULK posting into N4
*
************************************************************************************************
*/
package com.matson.tos.util;

import java.util.Date;
import java.util.HashMap;

import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.timer.Timer;

import com.matson.tos.processor.*;
import org.apache.log4j.Logger;


import com.matson.tos.dao.NewVesselDao;
import com.matson.tos.groovy.writer.FTPWriterCmis;

public class TosJobScheduler implements NotificationListener, TimedJob{
	private static final String version ="3.1.44_HEAD";
	private static Logger logger = Logger.getLogger(TosJobScheduler.class);
	private Timer timer;
	private Integer stifFileProcessId;

	private Integer newVesFileProcessId;
	private Integer dcmConverterFileProcessId;
	private Integer mqMsgProcessId;
	private Integer cancelGateTranId;
	private Integer cvsReportTranId; // A7
	private Integer cmisToGemsTranId;
	private Integer paogToGemsTranId; // A11
	private boolean cleanup = false;

	private Integer ftpWriterCmisId;
	private Date lastExecutionDate = new Date();

	private static String TIMER_NEW_VES = "TIMER_NEW_VES";
	private static String TIMER_STIF = "TIMER_STIF";
	private static String TIMER_DCM = "TIMER_DCM";
	private static String TIMER_MQ_PROC_INTERVAL = "TIMER_MQ_PROC_INTERV";
	private static String TIMER_CMIS_FTP = "TIMER_CMIS_FTP";
	private static String TIMER_CMISTOGEMS = "TIMER_CMISTOGEMS";
	private static String TIMER_PAOGTOGEMS = "TIMER_PAOGTOGEMS"; //A11
	private static String TIMER_NV_STOW_PLAN = "TIMER_NV_STOW_PLAN";
	private static String TIMER_NV_DCM = "TIMER_NV_DCM";
	private static String TIMER_NV_RDS = "TIMER_NV_RDS";
	private static String TIMER_NV_GUMST_PLAN = "TIMER_NV_GUMST_PLAN";
	private static String TIMER_NV_GUMDCM = "TIMER_NV_GUMDCM";
	private static String TIMER_NV_GUMRDS = "TIMER_NV_GUMRDS";
	
	private Integer stowPlanMessagePocessId;
	private Integer dcmMessageProcessId;
	private Integer rdsMessageProcessId;
	private Integer gumStowPlanMessagePocessId;
	private Integer gumDcmMessageProcessId;
	private Integer gumRdsMessageProcessId;
	
	private static AcetsMQMessageProcessor mqProc = new AcetsMQMessageProcessor();
	private static CancelGateProcessor cancelProc = new CancelGateProcessor();
	private static CvsReportProcessor cvsReportProc = new CvsReportProcessor();

	//Nascent
	
	private Integer nascentJobId;
	private static String TIMER_NASCENT = "TIMER_NASCENT";

	private Integer vgxVerifyJobId;
	private static String TIMER_AVGX = "TIMER_AVGX";

	private Integer vgxCleanUpJobId;
	private static String TIMER_AVGX_CLEANUP = "TIMER_AVGX_CLEANUP";

	static {
		EnvironmentProperty.configure();
	}

	public Date getLastExecutionDate() {
		return lastExecutionDate;
	}


	public void setLastExecutionDate(Date lastExecutionDate) {
		this.lastExecutionDate = lastExecutionDate;
	}


	public TosJobScheduler() {

		timer = new Timer();
		timer.addNotificationListener(this, null, "some handback object");
/*		stifFileProcessId = timer.addNotification("StifFileProcess",  "Stif file process",
				this, new Date(),
				Timer.ONE_MINUTE * getTimeInt( TIMER_STIF));
*/
		
		nascentJobId = timer.addNotification("NascentProcess",
				"Nascent Process", this, new Date(),
				 Timer.ONE_SECOND * getTimeInt(TIMER_NASCENT));
		
		newVesFileProcessId = timer.addNotification("NewVesFileProcess",
				"New Vessel File Process", this, new Date(),
				 Timer.ONE_MINUTE * getTimeInt(TIMER_NEW_VES));

		vgxVerifyJobId = timer.addNotification("VGXProcess",
				"VGX Process", this, new Date(),
				Timer.ONE_SECOND * getTimeInt(TIMER_AVGX));

		vgxCleanUpJobId = timer.addNotification("VGXCleanupProcess",
				"VGX Cleanup Process", this, new Date(),
				Timer.ONE_MINUTE * getTimeInt(TIMER_AVGX_CLEANUP));

/*		dcmConverterFileProcessId = timer.addNotification("DcmConverterFileProcessor",
				"Dcm Converter File Process", this, new Date(),
				 Timer.ONE_MINUTE * getTimeInt(TIMER_DCM));
		mqMsgProcessId = timer.addNotification("AcetsMQMessageProcessor",
				"ACETS MQ Message Process", this, new Date(),
				 (long)(Timer.ONE_MINUTE * getTimeDouble(TIMER_MQ_PROC_INTERVAL)));

		ftpWriterCmisId = timer.addNotification("FTPWriterCmis",
				"FTP CMIS File Process", this, new Date(),
				Timer.ONE_SECOND * getTimeInt(TIMER_CMIS_FTP));

		cmisToGemsTranId = timer.addNotification("CmisToGemsFileProcessor","CmisToGems File Process", this, new Date(),
				 Timer.ONE_MINUTE * getTimeInt(TIMER_CMISTOGEMS));

		paogToGemsTranId = timer.addNotification("PortAuthOfGuamGateTxnProcessor","PaogToGems File Process", this, new Date(),
				 Timer.ONE_MINUTE * getTimeInt(TIMER_PAOGTOGEMS)); //A11
*/		
		stowPlanMessagePocessId = timer.addNotification("StowPlanMessageProcessor",
				"Stow Plan File Process for NV", this, new Date(),
				 Timer.ONE_MINUTE * getTimeInt(TIMER_NV_STOW_PLAN));
		
		dcmMessageProcessId = timer.addNotification("DcmMessageProcessor",
				"DCM File Process for NV", this, new Date(),
				 Timer.ONE_MINUTE * getTimeInt(TIMER_NV_DCM));
		
		rdsMessageProcessId = timer.addNotification("RdsMessageProcessor",
				"RDS File Process for NV", this, new Date(),
				 Timer.ONE_MINUTE * getTimeInt(TIMER_NV_RDS));
		
/*		gumStowPlanMessagePocessId = timer.addNotification("GumStowPlanMessageProcessor",
				"Stow plan File Process for GUM NV", this, new Date(),
				 Timer.ONE_MINUTE * getTimeInt(TIMER_NV_GUMST_PLAN));
		
		gumDcmMessageProcessId = timer.addNotification("GumDCMMessageProcessor",
				"DCM File Process for GUM NV", this, new Date(),
				 Timer.ONE_MINUTE * getTimeInt(TIMER_NV_GUMDCM));
		
		gumRdsMessageProcessId = timer.addNotification("GumRDSMessageProcessor",
				"RDS File Process for GUM NV", this, new Date(),
				 Timer.ONE_MINUTE * getTimeInt(TIMER_NV_GUMRDS));*/

		logger.info( "Scheduler "+version+" was initialized.");
	}


	public void start() {
		if ( timer != null)
			timer.start();
	}
	private int getTimeInt( String timeKey) {
		try {
			return Integer.parseInt( TosRefDataUtil.getValue( timeKey));
		} catch (Exception e) {
			logger.error("No time in database for timer "+timeKey,e);
			return new Integer(10);
		}
	}

	private Double getTimeDouble( String timeKey) {
		try {
			return Double.parseDouble( TosRefDataUtil.getValue( timeKey));
		} catch (Exception e) {
			logger.error("No time in database for timer "+timeKey,e);
			return new Double(10);
		}
	}


	public void handleNotification(Notification notif, Object handback)
	{
	   lastExecutionDate = new Date();
	   String type = notif.getType();
	   IFileProcessor proc = null;
	   String stowPlanOn = null;
	   String dcmOn = null;
	   String rdsOn = null;
	   String gumStowPlanOn = null;
	   String gumDcmOn = null;
	   String gumRdsOn = null;
	   String runStifProcessIn = null;
	   String runNewVesFileProcessIn = null;
	   String runDcmConvProcIn = null;
	   String runFtpWriterCmisIn = null;
	   String runAcetsMqProcIn = null;
	   String runCmisGemsProcIn = null;
	   String runGumGateTxnProcIn = null;
	   String runStowPlanIn = null;
	   String runDcmIn = null;
	   String runRdsIn = null;
	   String runGumStowPlanIn = null;
	   String runGumDcmIn = null;
	   String runGumRdsIn = null;
	   String runNascent = null;
	   String runVgx = null;
		String runVgxCleanup = null;

	   HashMap parmsMap = NewVesselDao.getTosShedularParameters();
	   if (parmsMap!=null) {
		   stowPlanOn = (String)parmsMap.get("IS_STOWPLAN_JOB_ON");
		   dcmOn = (String)parmsMap.get("IS_DCM_JOB_ON");
		   rdsOn = (String)parmsMap.get("IS_RDS_JOB_ON");
		   gumStowPlanOn = (String)parmsMap.get("IS_GUM_STPLAN_JOB_ON");
		   gumDcmOn = (String)parmsMap.get("IS_GUM_DCM_JOB_ON");
		   gumRdsOn = (String)parmsMap.get("IS_GUM_RDS_JOB_ON");
		   runStifProcessIn = (String)parmsMap.get("RUN_STIF_PROC_IN");
		   runNewVesFileProcessIn = (String)parmsMap.get("RUN_NEWVES_PROC_IN");
		   runDcmConvProcIn = (String)parmsMap.get("RUN_DCM_CON_PROC_IN");
		   runFtpWriterCmisIn = (String)parmsMap.get("RUN_FTP_CMIS_IN");
		   runAcetsMqProcIn = (String)parmsMap.get("RUN_ACETSMQ_PROC_IN");
		   runCmisGemsProcIn = (String)parmsMap.get("RUN_CMISGEMS_PROC_IN");
		   runGumGateTxnProcIn = (String)parmsMap.get("RUN_GUMGATE_PROC_IN");
		   runStowPlanIn = (String)parmsMap.get("RUN_STOW_PLAN_IN");
		   runDcmIn = (String)parmsMap.get("RUN_DCM_IN");
		   runRdsIn = (String)parmsMap.get("RUN_RDS_IN");
		   runGumStowPlanIn = (String)parmsMap.get("RUN_GUMSTOW_IN");
		   runGumDcmIn = (String)parmsMap.get("RUN_GUMDCM_IN");
		   runGumRdsIn= (String)parmsMap.get("RUN_GUMRDS_IN");
		   runNascent= (String)parmsMap.get("RUN_NASCENT_PROC");
		   runVgx= (String)parmsMap.get("RUN_VGX_PROCESS");
		   runVgxCleanup = (String)parmsMap.get("RUN_VGX_CLEANUP");
		   logger.debug("new ves Sheduler parameters stowPlanOn-dcmOn-rdsOn "+stowPlanOn+"-"+dcmOn+"-"+rdsOn);
	   }
	   

	    if ( type.equals("StifFileProcess") ) {
	 	   if (isJBoss() && "jboss".equalsIgnoreCase(runStifProcessIn)) {
	 		  logger.debug( "StifFileProcess got a notice to excute in "+EnvironmentProperty.getEnvType());
	 		  proc = new StifFileProcessor();
	 	   }else if (!isJBoss() && "weblogic".equalsIgnoreCase(runStifProcessIn)){
	 		  logger.debug( "StifFileProcess got a notice to excute in "+EnvironmentProperty.getEnvType());
	 		  proc = new StifFileProcessor(); 
	 	   }
	       // invoke the component or ejb that does the order processing and submission
	    }

	    else if ( type.equals("NewVesFileProcess") ) {
	        // invoke the component or ejb that does the inventory processing and submission
	    	if (isJBoss() && "jboss".equalsIgnoreCase(runNewVesFileProcessIn)) {
		    	logger.debug( "New Vessel file processor got a notice to excute in "+EnvironmentProperty.getEnvType());
	    		proc = new NewVesFileProcessor();
	    	}else if (!isJBoss() && "weblogic".equalsIgnoreCase(runNewVesFileProcessIn)){
		    	logger.debug( "New Vessel file processor got a notice to excute in "+EnvironmentProperty.getEnvType());
	    		proc = new NewVesFileProcessor();
		 	  }
	    	
	    }else if ( type.equals("NascentProcess") ) {	        
	    	if (isJBoss() && "jboss".equalsIgnoreCase(runNascent)) {
		    	logger.debug( "Nascent processor got a notice to excute in "+EnvironmentProperty.getEnvType());
	    		proc = new NascentProcessor();
	    	}else if (!isJBoss() && "weblogic".equalsIgnoreCase(runNascent)){
		    	logger.debug( "Nascent processor got a notice to excute in "+EnvironmentProperty.getEnvType());
	    		proc = new NascentProcessor();
		 	  }
	    }
	    else if ( type.equals("DcmConverterFileProcessor") ) {
	        // invoke the component or ejb that does the inventory processing and submission
	    	if (isJBoss() && "jboss".equalsIgnoreCase(runDcmConvProcIn)) {
		    	logger.debug( "DCM Converter file processor got a notice to excute in "+EnvironmentProperty.getEnvType());
	    		proc = new DcmConverterFileProcessor();
	    	}else if (!isJBoss() && "weblogic".equalsIgnoreCase(runDcmConvProcIn)){
		    	logger.debug( "DCM Converter file processor got a notice to excute in "+EnvironmentProperty.getEnvType());
	    		proc = new DcmConverterFileProcessor();
	    	}
	    }
		else if ( type.equals("VGXProcess") ) {
			if (isJBoss() && "jboss".equalsIgnoreCase(runVgx)) {
				logger.debug( "VGX processor got a notice to excute in "+EnvironmentProperty.getEnvType());
				proc = new AvgxMessageProcessor();
			}else if (!isJBoss() && "weblogic".equalsIgnoreCase(runVgx)){
				logger.debug( "VGX processor got a notice to excute in "+EnvironmentProperty.getEnvType());
				proc = new AvgxMessageProcessor();
			}
		}


		else if ( type.equals("VGXCleanupProcess") ) {
		if (isJBoss() && "jboss".equalsIgnoreCase(runVgxCleanup)) {
			logger.debug( "VGX processor got a notice to excute in "+EnvironmentProperty.getEnvType());
			proc = new AvgxMessageCleanupProcessor();
		}else if (!isJBoss() && "weblogic".equalsIgnoreCase(runVgxCleanup)){
			logger.debug( "VGX processor got a notice to excute in "+EnvironmentProperty.getEnvType());
			proc = new AvgxMessageCleanupProcessor();
		}
	}
        //Replaced with CMIS Format
	    else if ( type.equals("FTPWriterCmis") ) {
	        // invoke the component or ejb that does the inventory processing and submission
	    	if (isJBoss() && "jboss".equalsIgnoreCase(runFtpWriterCmisIn)) {
	    		logger.debug( "FTPWriterCmis processor in "+EnvironmentProperty.getEnvType());
	    		FTPWriterCmis writerCmis = (FTPWriterCmis)FTPWriterCmis.getInstance();
	    		proc = writerCmis;
	    	}else if (!isJBoss() && "weblogic".equalsIgnoreCase(runFtpWriterCmisIn)){
	    		logger.debug( "FTPWriterCmis processor in "+EnvironmentProperty.getEnvType());
	    		FTPWriterCmis writerCmis = (FTPWriterCmis)FTPWriterCmis.getInstance();
	    		proc = writerCmis;	
	    	}
	    } else if ( type.equals("AcetsMQMessageProcessor") ) {
	        // invoke ACETS MQ processor
	    	if (isJBoss() && "jboss".equalsIgnoreCase(runAcetsMqProcIn)) {
	    		logger.debug( "AcetsMQMessageProcessor processor in "+EnvironmentProperty.getEnvType());
	    		mqProc.processMsg();
	    	}else if (!isJBoss() && "weblogic".equalsIgnoreCase(runAcetsMqProcIn)){
	    		logger.debug( "AcetsMQMessageProcessor processor in "+EnvironmentProperty.getEnvType());
	    		mqProc.processMsg();
	    	}
	    }else if ( type.equals("CmisToGemsFileProcessor") ) { //A7
	    	if (isJBoss() && "jboss".equalsIgnoreCase(runCmisGemsProcIn)) {
	    		logger.debug( "CmisToGems got a notice to excute in "+EnvironmentProperty.getEnvType());
	    		proc = new CmisToGemsFileProcessor();
	    	}else if (!isJBoss() && "weblogic".equalsIgnoreCase(runCmisGemsProcIn)){
	    		logger.debug( "CmisToGems got a notice to excute in "+EnvironmentProperty.getEnvType());
	    		proc = new CmisToGemsFileProcessor();
	    	}
	    }else if ( type.equals("PortAuthOfGuamGateTxnProcessor") ) { //A7
	    	if (isJBoss() && "jboss".equalsIgnoreCase(runGumGateTxnProcIn)) {
	    		logger.debug( "PaogToGems got a notice to excute.");
	    		proc = new PortAuthOfGuamGateTxnProcessor();
	    	}else if (!isJBoss() && "weblogic".equalsIgnoreCase(runGumGateTxnProcIn)){
	    		logger.debug( "PaogToGems got a notice to excute.");
	    		proc = new PortAuthOfGuamGateTxnProcessor();
	    	}
	    }else if ( type.equals("StowPlanMessageProcessor") && "Y".equalsIgnoreCase(stowPlanOn)) {
	    	if (isJBoss() && "jboss".equalsIgnoreCase(runStowPlanIn)) {
		    	logger.debug( "StowPlanMessageProcessor got a notice to excute in "+EnvironmentProperty.getEnvType());
		    	proc = new StowPlanMessageProcessor();
	    	}else if (!isJBoss() && "weblogic".equalsIgnoreCase(runStowPlanIn)){
	    		logger.debug( "StowPlanMessageProcessor got a notice to excute in "+EnvironmentProperty.getEnvType());
		    	proc = new StowPlanMessageProcessor();
	    	}
	    } else if ( type.equals("DcmMessageProcessor") && "Y".equalsIgnoreCase(dcmOn)) {
	    	if (isJBoss() && "jboss".equalsIgnoreCase(runDcmIn)) {
		    	logger.debug( "DcmMessageProcessor got a notice to excute "+EnvironmentProperty.getEnvType());
		    	proc = new DCMMessageProcessor();
	    	}else if (!isJBoss() && "weblogic".equalsIgnoreCase(runDcmIn)){
	    		logger.debug( "DcmMessageProcessor got a notice to excute "+EnvironmentProperty.getEnvType());
		    	proc = new DCMMessageProcessor();
	    	}
	    } else if ( type.equals("RdsMessageProcessor") && "Y".equalsIgnoreCase(rdsOn)) {
	    	if (isJBoss() && "jboss".equalsIgnoreCase(runRdsIn)) {
		    	logger.debug( "RdsMessageProcessor got a notice to excute "+EnvironmentProperty.getEnvType());
		    	proc = new RDSMessageProcessor(null);
	    	}else if (!isJBoss() && "weblogic".equalsIgnoreCase(runRdsIn)){
	    		logger.debug( "RdsMessageProcessor got a notice to excute "+EnvironmentProperty.getEnvType());
		    	proc = new RDSMessageProcessor(null);
	    	}
	    }else if ( type.equals("GumStowPlanMessageProcessor") && "Y".equalsIgnoreCase(gumStowPlanOn)) {
	    	if (isJBoss() && "jboss".equalsIgnoreCase(runGumStowPlanIn)) {
	    		logger.debug( "GumStowPlanMessageProcessor got a notice to excute "+EnvironmentProperty.getEnvType());
	    		proc = new GumStowPlanMessageProcessor();
	    	}else if (!isJBoss() && "weblogic".equalsIgnoreCase(runGumStowPlanIn)){
	    		logger.debug( "GumStowPlanMessageProcessor got a notice to excute "+EnvironmentProperty.getEnvType());
	    		proc = new GumStowPlanMessageProcessor();
	    	}
	    }else if ( type.equals("GumDCMMessageProcessor") && "Y".equalsIgnoreCase(gumDcmOn)) {
	    	if (isJBoss() && "jboss".equalsIgnoreCase(runGumDcmIn)) {
	    		logger.debug( "GumDCMMessageProcessor got a notice to excute "+EnvironmentProperty.getEnvType());
	    		proc = new GumDCMMessageProcessor();
	    	}else if (!isJBoss() && "weblogic".equalsIgnoreCase(runGumDcmIn)){
	    		logger.debug( "GumDCMMessageProcessor got a notice to excute "+EnvironmentProperty.getEnvType());
	    		proc = new GumDCMMessageProcessor();
	    	}
	    } else if ( type.equals("GumRDSMessageProcessor") && "Y".equalsIgnoreCase(gumRdsOn)) {
	    	if (isJBoss() && "jboss".equalsIgnoreCase(runGumRdsIn)) {
	    		logger.debug( "GumRDSMessageProcessor got a notice to excute "+EnvironmentProperty.getEnvType());
	    		proc = new GumRDSMessageProcessor(null);
	    	}else if (!isJBoss() && "weblogic".equalsIgnoreCase(runGumRdsIn)){
	    		logger.debug( "GumRDSMessageProcessor got a notice to excute "+EnvironmentProperty.getEnvType());
	    		proc = new GumRDSMessageProcessor(null);
	    	}
	    }  else {
	    	logger.error( "Wrong notify type: " + type);
	    }
	    if ( proc != null)
	    	proc.processFiles();

	    //logger.debug("Job Execution Complete for "+type);
	 }

	public synchronized void cleanUp()
	{
	    logger.debug( "TosJobScheduler cleanUp method called.");
	    if(cleanup) {
	    	logger.debug( "TosJobScheduler Schedule already stopped.");
	    	return;
	    }
	    cleanup = true;
	    try
	    {
	       timer.stop();
	       timer.removeNotification( stifFileProcessId);
	       timer.removeNotification( newVesFileProcessId);
	       timer.removeNotification( dcmConverterFileProcessId);
	       timer.removeNotification( mqMsgProcessId);
	       timer.removeNotification( ftpWriterCmisId);
		   timer.removeNotification(cmisToGemsTranId);
		   timer.removeNotification(paogToGemsTranId);//A11
		   timer.removeNotification(stowPlanMessagePocessId);
		   timer.removeNotification(dcmMessageProcessId);
		   timer.removeNotification(rdsMessageProcessId);
		   timer.removeNotification(gumStowPlanMessagePocessId);
		   timer.removeNotification(gumDcmMessageProcessId);
		   timer.removeNotification(gumRdsMessageProcessId);
		   timer.removeNotification(nascentJobId);
		   timer.removeNotification(vgxVerifyJobId);
			timer.removeNotification(vgxCleanUpJobId);

	       timer.removeAllNotifications();
	       logger.debug( "TosJobScheduler Scheduler stopped.");
	    }
	    catch (Exception e)
	    {
	       e.printStackTrace();
	       logger.error( "Error found when cleanup: ", e);
	    }
	}
	protected void finalize() throws Throwable
	  {
		logger.debug( "TosJobScheduler finalize called.");
		cleanUp();
		super.finalize();
	  }
	
	public static Boolean isJBoss() {
		String envType = EnvironmentProperty.getEnvType();
		if (envType != null)
			if (envType.indexOf("SVC") != -1)
				return Boolean.TRUE;
			else
				return Boolean.FALSE;
		return Boolean.FALSE;	
	}
}
