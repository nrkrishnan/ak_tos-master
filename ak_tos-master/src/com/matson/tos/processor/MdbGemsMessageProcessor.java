/*
*********************************************************************************************
*Srno   Date		Doer  Change Description
* A1    08/16/2010  GR    Class created to accept gems messages from "gems.tos.n4.formatted" queue 
* A2    02/22/11    GR    Post Trucker Messages from Gems -> Cmis
* A3    04/06/11    GR    Escape xml character in Trucker Message
*********************************************************************************************
*/
package com.matson.tos.processor;

import com.matson.tos.util.CalendarUtil;
import com.matson.tos.util.EnvironmentProperty;
import com.matson.tos.util.JMSSender;
import com.matson.tos.util.TosRefDataUtil;
import org.apache.log4j.Logger;

import javax.ejb.EJBException;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.jms.*;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class MdbGemsMessageProcessor implements MessageListener,
		MessageDrivenBean {
	public static final String ATRU = "ATRU";
	public static final String ALASKA_TRUCKING_COMPANIES = "ALASKA_TRUCKING_COM";
	private static Logger logger = Logger.getLogger(MdbGemsMessageProcessor.class);
	private static String dateFormat = "MM/dd/yyyy HH:mm:ss";
	private static String outqueue = TosRefDataUtil.getValue("N4_OUT_QUEUE");
	private MessageDrivenContext _context;
	// Removed this variable - Global Object to synzhronize on.
	private static final Object lock = new Object();
	
	static {
		EnvironmentProperty.configure();
	}
	
	public MdbGemsMessageProcessor() {
		// TODO Auto-generated constructor stub
	}

	public void onMessage(Message msgText) {

           String text = null;  
	    try 
	    {
	      if(msgText instanceof ObjectMessage) {
	    	  text = (String) ((ObjectMessage)msgText).getObject();
	    	  logger.debug("Received Object message : " + text);
	      } else if(msgText instanceof TextMessage) {
	    	  text = ((TextMessage)msgText).getText();
	    	  logger.debug("Received Gems message : " + text);
	      } else if (msgText instanceof BytesMessage) {
	    	  text = convertByteArrayToMsgString((BytesMessage) msgText);
	    	  logger.debug("Received Byte message : " + text);
	      }
	      else{
	    	  logger.debug("Received message Type : " + msgText.getJMSType());
	      }

	      //Process Message
	      AcetsMessageProcessor proc = new AcetsMessageProcessor();
	      if(text != null){
	        proc.processMsg(text,"JMS");
	      }
	      
	      if(text != null && (text.startsWith("ATRA") || text.startsWith("ATRU") ||text.startsWith("ATRD"))){
	    	  //Post TRUCKER Messages from GEMS -> CMIS
			  //ART A,U&D are not required by Alaska, Stopping their Processing
			  logger.debug("ATR A,U & D Message are not processed for Alaska, the following message is not processed\n" + text);
			  //procCmisTruckMessage(text);
		  }
	   
	      
	      
	    } catch(JMSException ex) {
	    	logger.error( "Exception: ", ex);
	    }
	}
	public void ejbCreate () {
	    //logger.debug("ejbCreate called");
	  }

	public void ejbRemove() throws EJBException {
		// TODO Auto-generated method stub
	}

	public void setMessageDrivenContext(MessageDrivenContext ctx)
			throws EJBException {
		_context = ctx;
	}
	
	 protected String convertByteArrayToMsgString(BytesMessage message) throws JMSException 
	 {
	     byte[] bytes = new byte[(int) message.getBodyLength()];
	     message.readBytes(bytes);
	     String msgStr = new String(bytes);  
	     return msgStr;
	  }
	 
	 public void procCmisTruckMessage(String msg){
		 String cmisTruckMsg = null;
		 try{
			 if (msg == null) return;
			 String truckMsg[] = mysplit(msg);
			 for (int i=0;i<11;i++)
				 logger.debug("Parsed Msg"  + truckMsg[i]);			 
			 if(truckMsg.length == 0){
				 return ; 
			 }
			 logger.debug("Inside procCmisTruckMessage Msg: " + msg + ", parsed truckMsg: " + truckMsg.length);
			 String action = truckMsg[0] != null ? truckMsg[0].trim() : null; 
			 action = " action='"+action+"'";
			 String truck= truckMsg[5] != null ? truckMsg[5].trim() : null;
			 truck = " truck='"+org.apache.commons.lang.StringEscapeUtils.escapeXml(truck)+"'";  //A7
			 String truckerName= truckMsg[6] != null ? truckMsg[6].trim() : null;
			 truckerName = " truckerName='"+org.apache.commons.lang.StringEscapeUtils.escapeXml(truckerName)+"'";
			 
			 String strDate = CalendarUtil.dateFormat(new Date(),dateFormat,TimeZone.getTimeZone("HST"));
			 
			 String aDate= strDate.substring(0,10);
			 aDate = " aDate='"+aDate+"'";
			 String aTime= strDate.substring(11);
			 aTime = " aTime='"+aTime+"'";
			 String srv = truckMsg[7].trim();
			 srv = " srv='"+srv+"'";
			 //1. Create Cmis Message	 
			 cmisTruckMsg = "<GroovyMsg msgType='TRUCKER' unitClass='TRUCKVISIT'"+action+aDate+aTime+truck+truckerName+srv+" />";
			 
			 //2. Post Trucker cmis Message to N4Outque 
			 //The Sn4 message processor reads/creates/Ftp's the FlatFile
			 //System.out.println("XML :"+cmisTruckMsg);
			 JMSSender sender = new JMSSender(outqueue, true);
			 /**
			  * Allow the Processor to construct the message normally, but post it conditionally
			  */
			 Boolean postToJMSQueue = Boolean.TRUE;
			 if (ATRU.equals(truckMsg[0].toUpperCase())) {
				 postToJMSQueue = Boolean.FALSE;
				 try {
					 String tempCompanies = TosRefDataUtil.getValue(ALASKA_TRUCKING_COMPANIES).toUpperCase();
					 if (tempCompanies != null) {
						 List<String> companies = Arrays.asList(tempCompanies.split(",", -1));
						 String snxCompanyId = truckMsg[5] != null ? truckMsg[5].trim().toUpperCase() : null;
						 if (snxCompanyId != null && companies.contains(snxCompanyId)) {
							 // post to JMS Queue
							 postToJMSQueue = Boolean.TRUE;
						 }
					 }
				 } catch (Exception e) {
					 logger.error("Error in processing message	" + msg, e);
				 }

			 }
			 if (postToJMSQueue) {
				 sender.send(cmisTruckMsg);
			 }
			 
		 }catch(Exception e){
			 logger.error( "Exception: ", e);
		 }
	 }
	 /**
		 * This is not a generic split function
		 * i wrote it as the java split is not working for some reason unknown
		 * @param msg
		 * @return
		 */
		private static String [] mysplit(String msg) {
			int totPipes = 1;
			final int PIPE_ASCII_VAL = 166;//this is the ASCII for  ¦ symbol we recieve from GEMS
						
			logger.debug("Inside myplit for : " + msg);
			char charMsg[] = msg.toCharArray();
			int intMsg[] = new int[charMsg.length];
			logger.debug("length of charMsg is : " + charMsg.length);
			for (int i=0, j=intMsg.length;i<j;i++) {
				intMsg [i] = charMsg[i];			
			}			
			for (int i=0, j=charMsg.length,k=0;i<j;i++) {
				if (PIPE_ASCII_VAL == intMsg[i])
					totPipes++;
			}
			
			String truckMsg[] = new String[totPipes];
			logger.debug("number of pipes: " + totPipes);
			 for (int i=0, j=charMsg.length,k=0;i<j;i++) {
				 System.out.println("char at:" + i + "is : " +charMsg[i]);
				 if (i==0) truckMsg[0] = "";
				 if (PIPE_ASCII_VAL == intMsg[i]) {
					 logger.debug("parsed so far: " + truckMsg[k]);
					 k++;
					 truckMsg[k] = "";
				 } else if (PIPE_ASCII_VAL != charMsg[i])
					 truckMsg[k]+=charMsg[i];
			 }
		return truckMsg;
		} 
	 /*
	 public static void main(String args[]){
		 try{
			 MdbGemsMessageProcessor proc = new MdbGemsMessageProcessor();
			 String msg ="ATRU¦05/22/12¦14:59:58¦NEQOL¦000000000¦DHXI¦DHX, INC. (DEPENDABLE HAWAIIAN EXP)¦MAT  ¦ ¦RICE TIM                        ¦310-537-2001";
			 proc.procCmisTruckMessage(msg);
		 }catch(Exception e){
			 e.printStackTrace();
		 }
	 }
	 */
}
