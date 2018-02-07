/*
*********************************************************************************************
*Srno   Date			AuthorName			Change Description
		8/25/09			Steven Bauer		Created
		10/21/09		Steven Bauer		disabled
		10/29/09		Steven Bauer		enabled
		02/25/10        Glenn Raposo        enabled
*********************************************************************************************
*/
package com.matson.tos.processor;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;


import org.apache.log4j.Logger;

import com.matson.sched.Schedule;
import com.matson.tos.util.EmailSender;
import com.matson.tos.util.EnvironmentProperty;
import com.matson.tos.util.JMSSender;
import com.matson.tos.util.TosRefDataUtil;
import com.matson.tos.util.VesselScheduleLookup;


public class MdbFSSMessageProcessor implements MessageListener,
		MessageDrivenBean {
	private static Logger logger = Logger.getLogger(MdbFSSMessageProcessor.class);
	private String emailAddr = TosRefDataUtil.getValue( "SUPPORT_EMAIL");
	private String mailhost = TosRefDataUtil.getValue( "MAIL_HOST");
	private MessageDrivenContext _context;
	// Global Object to synzhronize on.
	private static final Object lock = new Object();
	
	private static final String groovyMsg = "FSS Error Message";
	private static final String snxMsg = "SN4 Error Message";
	private static VesScheduleFileProcessor processor = new VesScheduleFileProcessor();

	static {
		EnvironmentProperty.configure();
	}
	
	public MdbFSSMessageProcessor() {
		// TODO Auto-generated constructor stub
	}

	public void onMessage(Message msgText) {
		synchronized (lock) {
			System.out.println("FSS Publish");
			
			VesselScheduleLookup lookup = new VesselScheduleLookup();
			try {
				Schedule sched = lookup.getSchedule();
				processor.processSchedule(sched);
			} catch (Exception e) {
				// todo Better message
				sendEmail("Error in FSS Message Processor", e.getMessage());
			} 
			
		}
	}
	public void ejbCreate ()  {
	    //logger.debug("ejbCreate called");
	  }

	public void ejbRemove() throws EJBException {
		// TODO Auto-generated method stub
	}

	public void setMessageDrivenContext(MessageDrivenContext ctx)
			throws EJBException {
		_context = ctx;
	}
		
	private void sendEmail( String subject, String msg) {
		try {
			EmailSender.sendMail( emailAddr, emailAddr, subject, msg );
		} catch (Exception ex) {
			logger.error( "Error sending email.", ex);
		}
	}	 
}
