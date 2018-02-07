/*
*********************************************************************************************
*Srno   Date			AuthorName			Change Description
A1		11/4			Steven Bauer		Send Error every 5 minutes.
*********************************************************************************************
*/
package com.matson.tos.util;

import java.util.Date;

import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.timer.Timer;

import org.apache.log4j.Logger;


public class TosJobMonitor implements NotificationListener {
	private static Logger logger = Logger.getLogger(TosJobMonitor.class);
	private Timer timer;
	private Integer monitorId;
	private TimedJob job;
	private boolean cleanup = false;
	private boolean error = false;
	private int threshold = 300000;
	
	private static String TIMER_MONITOR = "TIMER_MONITOR";
	private static String supportMail = TosRefDataUtil.getValue("SUPPORT_EMAIL");


	public TosJobMonitor(TimedJob job) {
		this.job = job;
		
		timer = new Timer();
		timer.addNotificationListener(this, null, "some handback object");
		
		monitorId = timer.addNotification("MonitorProcess",  "Monitor Process",
				this, new Date(), 
				Timer.ONE_MINUTE * 5);
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
		String message;
		Date lastDate  = job.getLastExecutionDate();
		
		
		Date now = new Date();
		long time = now.getTime() - lastDate.getTime();
		long min = Math.round((double)time/60000.0);
		
		message = "TosJobScheduler last ran "+min+" minutes ago.";
		
		if(error) {
			if(time < threshold) {
				error = false;
				logger.debug(message);
				EmailSender.sendMail(supportMail, supportMail, "Error Resolved, "+message, message);
				
			}
		} 
		
		if(time > threshold) {
			error = true;
			EmailSender.sendMail(supportMail, supportMail, "Error Detected, "+message, message);
			logger.debug(message);
		}
		
	}
	
	public synchronized void cleanUp()
	{
	    logger.debug( "TosJobMonitor cleanUp method called.");
	    if(cleanup) {
	    	logger.debug( "TosJobMonitor already stopped.");
	    	return;
	    }
	    cleanup = true;
	    try
	    {          
	       timer.stop();
	       timer.removeNotification( monitorId);
	       timer.removeAllNotifications();
	       logger.debug( "TosJobMonitor stopped.");
	    }
	    catch (Exception e)
	    {
	       e.printStackTrace();
	       logger.error( "Error found when TosMonitor cleanup: ", e);
	    }
	}
	protected void finalize() throws Throwable
	  {
		logger.debug( "TosJobMonitor finalize called.");
		cleanUp();
		super.finalize();
	  }

}
