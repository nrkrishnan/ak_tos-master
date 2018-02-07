package com.matson.tos.util;

import org.apache.log4j.Logger;
import org.jboss.system.ServiceMBeanSupport;

import com.matson.tos.messageHandler.AbstractMessageHandler;
import com.matson.tos.messageHandler.VesScheduleMessageHandler;


public class TosJbossPostConstructService extends ServiceMBeanSupport  implements TosJbossPostConstructServiceMBean,  org.jboss.system.ServiceMBean
	{
	private static Logger logger = Logger.getLogger(TosJbossPostConstructService.class);
	TosJobScheduler scheduler;
	TosJobMonitor monitor;
	
	
	
	
	@Override
	protected void startService() throws Exception {
		// TODO Auto-generated method stub
		super.startService();
		 logger.info( "TosAppListener:postStart Event");
		  
   	   //Start the Scheduler
   	  	scheduler = new TosJobScheduler();
   	  	scheduler.start();
   	  
   	  	monitor = new TosJobMonitor(scheduler);
   	  	monitor.start();
   	
	}

	@Override
	protected void stopService() throws Exception {
		// TODO Auto-generated method stub
		super.stopService();
		logger.info( "TosAppListener:preStop Event");
		    //Stop the Scheduler
		 scheduler.cleanUp();
		 monitor.cleanUp();
	}

   
	// The print message operation
	public  String printScheduler() {
		   if(scheduler != null) {
			   return "Last Execution="+scheduler.getLastExecutionDate().toString();
		   }
		   return "No last execution time!";
	   }
}
