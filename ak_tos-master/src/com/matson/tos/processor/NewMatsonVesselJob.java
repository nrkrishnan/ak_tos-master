package com.matson.tos.processor;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;

import com.matson.tos.dao.NewVesselDao;

/*
 * This is a job which will be scheduled to looks for .HON. DCM and RDS 
 * files at frequent intervals and reads the files from FTP location and
 * invokes corresponding file processor.
 * 
 * 	Srno	Date			AuthorName				Change Description
 * 	A1		09/26/2012		Raghu Pattangi			Initial creation
 * 	A2		10/15/2012		Karthik Rajendran		Linked: Invoke Stowplan, DCM, RDS message processors
 *  A3		10/18/2012		Karthik Rajendran		Changed: Invoke processfiles() from Stowplan,DCM,RDS message processor classes
 */

public class NewMatsonVesselJob {
	private static Logger logger = Logger.getLogger(NewMatsonVesselJob.class);
	private static String processType = null;
	private static String vvd = null;
	public static String NEWVES = "newves";
	public static String BARGE = "barge";
	public static String IS_PRIMARY = "IS_PRIMARY";
	public static String IS_SUPPLEMENTAL = "IS_SUPPLEMENTAL";
	public static String IS_BARGE = "IS_BARGE";
	public static String COPY_PRIMARY = "false";
	public static String COPY_SUPPLEMENT = "false";
	public static String COPY_BARGE = "false";

	public static String getProcessType() {
		return processType;
	}
	public static void setProcessType(String processType) {
		NewMatsonVesselJob.processType = processType;
	}
	public static String getVvd() {
		return vvd;
	}
	public static void setVvd(String vvd) {
		NewMatsonVesselJob.vvd = vvd;
	}
	public NewMatsonVesselJob() {
		//
	}
	public static void main(String[] args)
	{
		//new NewMatsonVesselJob().executeNewVesProc(); 
	}
	@SuppressWarnings("unused")
	public static void executeNewVesProc()
	{
		try {
			String type = NewMatsonVesselJob.getProcessType();
			if(type.equals(NEWVES)){
				logger.info(NEWVES);
				NewVesselDao.updateTosAppParameter(IS_PRIMARY, COPY_PRIMARY);
				NewVesselDao.updateTosAppParameter(IS_SUPPLEMENTAL, COPY_SUPPLEMENT);
				StowPlanMessageProcessor smp = new StowPlanMessageProcessor();
				DCMMessageProcessor dmp = new DCMMessageProcessor();
				String vvd = NewMatsonVesselJob.getVvd();
				RDSMessageProcessor rmp = new RDSMessageProcessor(vvd);
				logger.info(NEWVES+" process end.");
			} else if(type.equals(BARGE)){
				logger.info(BARGE);
				NewVesselDao.updateTosAppParameter(IS_BARGE, COPY_BARGE);
				DCMMessageProcessor dmp = new DCMMessageProcessor();
				StowPlanMessageProcessor smp = new StowPlanMessageProcessor();
				logger.info(BARGE+" process end.");
			}
		} catch(Exception ex) {
			logger.info("Exception in NewMatsonVesselJob :"+ex);
		}finally {
			logger.info("Cleaning up of static variables start");
			CommonBusinessProcessor.outboundExportVesselMap = null;
			CommonBusinessProcessor.outboundNIVesselMap = null;
			CommonBusinessProcessor.outboundVesselMap = null;
			CommonBusinessProcessor.portCodeTradeMap = null;
			CommonBusinessProcessor.arrivalDateMap = null;
			logger.info("Cleaning up of static variables end");
		}
	}
}
