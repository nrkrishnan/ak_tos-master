package com.matson.tos.util;

import org.apache.log4j.Logger;

//import weblogic.application.ApplicationLifecycleListener;
//import weblogic.application.ApplicationLifecycleEvent;

public class TosAppListener 
//extends ApplicationLifecycleListener 
{
	private static Logger logger = Logger.getLogger(TosAppListener.class);
	TosJobScheduler scheduler;
	TosJobMonitor monitor;
	public void postStart( 
			//ApplicationLifecycleEvent evt)
			Object evt)
	  {   
     	  logger.info("TosAppListener:postStart Event");
     	   //Start the Scheduler
     	  scheduler = new TosJobScheduler();
     	  scheduler.start();
     	  
     	  monitor = new TosJobMonitor(scheduler);
     	  monitor.start();
     	  
	  }
	public void preStop(
			//ApplicationLifecycleEvent evt)
			Object evt)
	  {
	     logger.info( "TosAppListener:preStop Event");
	    //Stop the Scheduler
	    scheduler.cleanUp();
	    monitor.cleanUp();
	  }
}
