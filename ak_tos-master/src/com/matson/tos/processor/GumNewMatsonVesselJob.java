package com.matson.tos.processor;

import org.apache.log4j.Logger;

import com.matson.tos.dao.NewVesselDao;


public class GumNewMatsonVesselJob {
	private static Logger logger = Logger.getLogger(GumNewMatsonVesselJob.class);
	private static String processType = null;
	private static String vvd = null;
	public static String NEWVES = "gumnewves";
	public static String COPY_PRIMARY = "false";
	public static String IS_COPY_GUM = "IS_COPY_GUM";

	public static String getProcessType() {
		return processType;
	}
	public static void setProcessType(String processType) {
		GumNewMatsonVesselJob.processType = processType;
	}
	public static String getVvd() {
		return vvd;
	}
	public static void setVvd(String vvd) {
		GumNewMatsonVesselJob.vvd = vvd;
	}
	public GumNewMatsonVesselJob() {
		//
	}
	public static void executeNewVesProc()
	{
		logger.info("GumNewMatsonVesselJob.executeNewVesProc - Begin");
		try {
			String type = GumNewMatsonVesselJob.getProcessType();
			if(type.equals(NEWVES)){
				logger.info(NEWVES);
				//NewVesselDao.updateTosAppParameter(IS_COPY_GUM, COPY_PRIMARY);
				logger.info("****STOWPLAN****");
				GumStowPlanMessageProcessor smp = new GumStowPlanMessageProcessor();
				logger.info("****DCM****");
				GumDCMMessageProcessor dmp = new GumDCMMessageProcessor();
				logger.info("****RDS****");
				String vvd = GumNewMatsonVesselJob.getVvd();
				GumRDSMessageProcessor rmp = new GumRDSMessageProcessor(vvd);
				logger.info(NEWVES+" process end.");
			} 
		} catch(Exception ex) {
			ex.printStackTrace();
			logger.info("Exception in GumNewMatsonVesselJob :"+ex);
		}finally {
			/*logger.info("Cleaning up of static variables start");
			CommonBusinessProcessor.outboundExportVesselMap = null;
			CommonBusinessProcessor.outboundNIVesselMap = null;
			CommonBusinessProcessor.outboundVesselMap = null;
			CommonBusinessProcessor.portCodeTradeMap = null;
			CommonBusinessProcessor.arrivalDateMap = null;
			logger.info("Cleaning up of static variables end");*/
		}
		logger.info("GumNewMatsonVesselJob.executeNewVesProc - End");
	}
}
