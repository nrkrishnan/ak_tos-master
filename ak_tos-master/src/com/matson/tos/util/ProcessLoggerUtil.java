package com.matson.tos.util;

import java.util.Date;

import org.apache.log4j.Logger;

import com.matson.cas.refdata.mapping.TosProcessLogger;
import com.matson.cas.refdata.mapping.TosProcessLoggerDAO;
import com.matson.tos.processor.StifFileProcessor;

public class ProcessLoggerUtil {

	private static Logger logger = Logger.getLogger(ProcessLoggerUtil.class);
	
	public void recordProcessDetails(int lineNum, String carrierId, String processType, String processStatus)throws Exception
	{
		logger.debug("recordProcessDetails");
		TosProcessLoggerDAO tosProcessDao = new TosProcessLoggerDAO();
		try
		{ 
		  if(lineNum == 0)
		  {  //Updates the Record entry on Process Completion
			 tosProcessDao.updateProcessDetails(carrierId,processType,processStatus);
			 logger.debug("############### RECORD UPDATED ##################");
		  }
		  else{
			 //Inserts the Record entry on Process Start
			 TosProcessLogger tosProcess = new TosProcessLogger(); 
			 tosProcess.setVesvoy(carrierId);
			 tosProcess.setProcessType(processType); 
			 tosProcess.setStartTime(new Date());
			 tosProcess.setStatus(processStatus);
			 tosProcessDao.insertProcessDetails(tosProcess);
			 logger.debug("############### RECORD INSERTED ##################");
			 
			 //TEST Select Call
			 boolean nevVesFlag = tosProcessDao.verifyProcessExecution(carrierId, processType);
		     logger.debug("PROCESS EXECUTION FLAG :::"+nevVesFlag);
		  }
		}
		catch (Exception ex) {
			logger.error("Exception found: ", ex);
			//ex.printStackTrace();
			throw ex;
		}
	}
}
