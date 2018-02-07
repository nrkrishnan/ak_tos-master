/*
*********************************************************************************************
*Srno   Date			AuthorName			Change Description
* A1    08/01/2008      Glenn Raposo		Changes made to incorporate Archive Mesaaage Queues
 											data into a DB table.
* A2    10/20/2008      Glenn Raposo        Change For onMessage() class cast Exception
* A3	06/22/2009		Steven Bauer		Throw out all ULK error message.
* A4	08/20/2009		Steven Bauer 		Filtered out hold messages.
* A5    10/20/2009      Glenn Raposo        Process Acets queued messages after NV process
* A6    10/29/2009		Glenn Raposo        Snx Error Mail to Specific Users
* A7    03/26/2010      Glenn Raposo        Adding End index rdsDate in notes
* A8    04/26/2010      Glenn Raposo        Added check for StringOutOfBound Exception
* A9    07/27/2010      Glenn Raposo        Commented out Received Message Logging
* A10   08/10/2010      Glenn Raposo        Changed TimeConversion to 0-24HRS
* A11   04/29/2010      Glenn Raposo        Stop Archiving xml messages
* A12   06/02/2010      Glenn Raposo        Removed References TO TOS Archiveing table
* A13   05/07/2014      Raghu Pattangi		Processing BDB/LNK/ULK messages before trucker coding and also after trucker coding.
*********************************************************************************************
*/
package com.matson.tos.processor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.matson.cas.refdata.mapping.TosProcessLogger;
import com.matson.cas.refdata.mapping.TosProcessLoggerDAO;
import com.matson.cas.erd.service.data.VesselVO;
import com.matson.tos.dao.AcetsNewVesMsgDao;
import com.matson.tos.dao.NewVesselDao;
import com.matson.tos.exception.NewVesselLogger;
import com.matson.tos.exception.TosException;
import com.matson.tos.messageHandler.AbstractMessageHandler;
import com.matson.tos.messageHandler.GstuffMessageHandler;
import com.matson.tos.messageHandler.IMessageHandler;
import com.matson.tos.util.EmailSender;
import com.matson.tos.util.EnvironmentProperty;
import com.matson.tos.util.JMSSender;
import com.matson.tos.util.TosErrorMsgRecorder;
import com.matson.tos.util.TosRefDataUtil;


public class MdbSn4MessageProcessor implements MessageListener,
		MessageDrivenBean {
	private static Logger logger = Logger.getLogger(MdbSn4MessageProcessor.class);
	private String emailAddr = TosRefDataUtil.getValue( "SUPPORT_EMAIL");
	private String mailhost = TosRefDataUtil.getValue( "MAIL_HOST");

	private MessageDrivenContext _context;
	// Global Object to synzhronize on.
	private static final Object lock = new Object();

	private static final String groovyMsg = "Groovy Injection Error Message";
	private static final String snxMsg = "SN4 Error Message";
	public String vesselOperator = "";
	private static String copyPrimary = null;
	//private static HashMap<String, String> vesvoyTriggerDateMap = null;
	private NewVesselLogger nvLogger = NewVesselLogger.getInstance();

	static {
		EnvironmentProperty.configure();
	}

	public MdbSn4MessageProcessor() {
		// TODO Auto-generated constructor stub
	}

	public void onMessage(Message msgText) {
		synchronized (lock) {

		 //A2 - Starts
		String text = null;
	    try
	    {
	      logger.info("MdbSn4MessageProcessor inside onMessage");
	      if(msgText instanceof ObjectMessage) {
	    	  text = (String) ((ObjectMessage)msgText).getObject();
	    	  logger.info("Received Object message : " + text);
	      } else if(msgText instanceof TextMessage) {
	    	  text = ((TextMessage)msgText).getText();
	    	  logger.info("Received Text message "+text);
	    	  //logger.debug("Received Text message : " + text);
	      } else if (msgText instanceof BytesMessage) {
	    	  text = convertByteArrayToMsgString((BytesMessage) msgText);
	    	  logger.info("Received Byte message : " + text);
	      }
	      else{
	    	  logger.info("Received message Type : " + msgText.getJMSType());
	      }

		   HashMap parmsMap = NewVesselDao.getTosCopyParameters();
		   if (parmsMap!=null) {
			   copyPrimary = (String)parmsMap.get("IS_PRIMARY");
			   logger.info("new ves copy parameters copyPrimary-"+copyPrimary);
		   }
	      //Process Message
	      if(text != null){
	      processMsg( text);
	      }

	    } catch(JMSException ex) {
	    	logger.error( "Exception: ", ex);
	    	ex.printStackTrace();
	    }finally{
	    	copyPrimary = null;
	    	vesselOperator = null;
	    }
	    //A2 -Ends
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

	private void processMsg( String msgText) {
		IMessageHandler msgHandler = null;
		String xml;
		ArrayList msgFieldList = null;
		String vesvoy = null;
		// check the message types and create a handler
		try {

			if ( msgText.startsWith( "<argo:snx-error")) { // handle error messages
				//logger.error( "Error message from SN4 : " + msgText);
				//Check for Error Messages From Acets
			    CharSequence acetsMsg = "ERR_GVY";
			    boolean acetsError = msgText.contains(acetsMsg);
			    if(acetsError){
			    	// Filter out messages that have nor been sent to N4 yet.
			    	if(isVesvoySubmited(msgText) || isUlk(msgText) ) {
			    		logger.info(groovyMsg+" ignored "+msgText);
			    	} else {
			    		sendEmail(groovyMsg, msgText);
			    	    TosErrorMsgRecorder.insertGroovyErrorMessage( msgText);
			    	}
			    }else{
			    	if(isBobError(msgText) || isHpuError(msgText) || isShipperError(msgText) ) {
			    		logger.info(snxMsg+" ignored "+msgText);
			    	} else {
			    		//sendEmail(snxMsg, msgText);
			    		sendSnxErrorMail(msgText); //A6
			    		TosErrorMsgRecorder.insertSnxErrorMessage( msgText);
			    	    isNewVesComplete(msgText); // A5
			    	}
			    }
			} else if ( msgText.startsWith( "<sn4")) {
				logger.debug( "SN4 message: " + msgText);
				//A1 - Setting Message Status
			}//-TestCode Message to log and send email from Cmis Feed Code
			else if ( msgText.startsWith( "<CmisFeedErrorMsg")) {
				//logger.error( "CMIS Feed Error Message: " + msgText);
				String errorMsg = "Please Check the Cmis Feed Error File for Error Details";
				String errorSub = "CMIS Feed Error Message";
				EmailSender.mailAttachment(emailAddr,emailAddr,mailhost,"CmisFeedError.txt",msgText,errorMsg,errorSub);
			} //TEST CODE Ends
			else if ( msgText.startsWith( "<GroovyMsg")) {
				//logger.debug( "Groovy message: " + msgText);
				GroovyMessageProcessor gvyProc = new GroovyMessageProcessor( msgText);
				boolean processStatus = gvyProc.process();
				 //A1 - Setting Message Status
				//msgText = preProcessGroovyMsg( msgText);
				//msgHandler = getGroovyMsgHandler( msgText);
			}else if (msgText.startsWith( "<GroovyNotice")){
				    String vesselVoyage = getFieldValue(msgText,"vesvoy");
				    logger.info("Vessel Voyage Inside groovy notice :"+vesselVoyage);
				    VesselVO vvo = CommonBusinessProcessor.getVesselDetails(vesselVoyage.substring(0, 3));
				    vesselOperator = vvo.getVessOpr();
				    logger.info("vesselOperator inside groovy notice :"+vesselOperator);
				    String msg1 = "NewVes Completed";
				    String msg2 = "NIS Load Transaction Completed";
				    String msg3 = "Supplemental Data Completed";
			       //Check XML string
				    if(msgText.contains(msg1))
				    {
				    	if ("false".equalsIgnoreCase(copyPrimary)) {
				    		logger.info("GroovyNotice for new vessel from legacy process OR CSX vessels");
				    		isNewVesComplete(msgText);
				    	} else if ("true".equalsIgnoreCase(copyPrimary)){
				    		//A13 start
				    		isNewVesComplete(msgText);
				    		//A13 end
				    		nvLogger.sendNewVessSuccess(vesselVoyage, "A", copyPrimary);
				    	}
				    	
				    }else if(msgText.contains(msg2)){
				    	logger.info("GroovyNotice for NIS Load Transaction process ");
				    	isNewVesComplete(msgText);
				    }
				    else if(msgText.contains(msg3)){
				    	logger.info("GroovyNotice for supplemental process ");
				    	isNewVesComplete(msgText);
				    }
 			} else if (msgText.contains("NEWVESSEL CODING COMPLETED")) {
 				  // update process logger with NV message
 				  isNewVesComplete(msgText);
 				  NewvesProcessorHelper newVesProcHelper = new NewvesProcessorHelper();
 				  msgFieldList = new ArrayList(Arrays.asList(msgText.split("-")));
 				  if (msgFieldList != null && msgFieldList.size() > 2) {
 					 vesvoy = (String)msgFieldList.get(1);
 				  }
 				  logger.info("VESVOY before calling reports :"+vesvoy);
 				  if (vesvoy != null) {
 					 newVesProcHelper.generateNewVesReports(vesvoy);
 				  }
 				  
 			}
			else {
				logger.debug( "Unknown SN4 message received:" + msgText);
				//A1 - Setting Error Desc
			}
			// convert the text message to xml message and send to JMS
			if ( msgHandler != null) {
				msgHandler.setTextStr( msgText);
				xml = msgHandler.getXmlStr();
				logger.debug( "XML:" + xml);
				JMSSender sender = new JMSSender( JMSSender.REAL_TIME_QUEUE, "HON");
				sender.send( xml);
			}
		} catch ( TosException tex) {
			logger.error( "Error:", tex);
		} catch ( JMSException jex) {
			logger.error( "Error:", jex);
		} catch ( Exception ex) {
			logger.error( "Error:", ex);
		}
		finally{
			try{
				//TosArchiveMsgDAO archMsgDao = new TosArchiveMsgDAO(); //A11
				//archMsgDao.insertArchiveMessage(archMsg);
				copyPrimary = null;
			}catch(Exception e){
				logger.error( "Error in Inserting Archieve Msg :", e);
			}
		}
	}
	// this method converts xml msg into csv text message so that we can utilize
	// the generic message handler
	// <GroovyMsg MsgType='GV_STUFF' type='F24H' TranState='UfvTransitStateEnum[S40_YARD]' id='MATU6485178' />
	private String preProcessGroovyMsg( String xmlMsg){
		StringBuffer ret = new StringBuffer();
		try {
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
	        InputStream is = new ByteArrayInputStream ( xmlMsg.getBytes());
	        Document doc = docBuilder.parse ( is);

	        NodeList listOfElem = doc.getElementsByTagName( "GroovyMsg");
	        Node aNode = listOfElem.item( 0);
	        NamedNodeMap attributes = aNode.getAttributes();
	        //aNode = attributes.getNamedItem( "MsgType");
	        String msgType = attributes.getNamedItem( "MsgType").getNodeValue();
	        ret.append( msgType).append("¦");


	        if ( msgType.equals( "GV_STUFF")) {
	        	ret.append( getStuffMsg( attributes));
	        } else if ( msgType.equals( "")) {

	        } else {
	        	logger.debug( "Unkown Groovy message type: " + msgType);
	        }


		} catch ( SAXParseException parseEx) {
			logger.error( "Error: ", parseEx);
		} catch ( SAXException saxEx) {
			logger.error( "Error: ", saxEx);
		} catch ( Exception ex) {
			logger.error( "Error: ", ex);
		}
		logger.debug( "TextMsg=" + ret.toString());
		return ret.toString();
	}
	private AbstractMessageHandler getGroovyMsgHandler( String csvTextMsg) {
		AbstractMessageHandler handler = null;
		try {
		if ( csvTextMsg.startsWith( "GV_STUFF"))
			handler = new GstuffMessageHandler( "com.matson.tos.jaxb.snx",
					"com.matson.tos.jatb", "/xml/gstuff.xml", AbstractMessageHandler.TEXT_TO_XML);

		} catch ( Exception ex) {
			logger.error( "Error in GV_STUFF:", ex);
		}
		return handler;
	}

	private String getStuffMsg( NamedNodeMap attributes) {
		StringBuffer msgBody = new StringBuffer();
		msgBody.append( attributes.getNamedItem("id").getNodeValue()).append( "¦");
		msgBody.append( attributes.getNamedItem("Qty").getNodeValue());
		logger.debug( "msgBody=" + msgBody.toString());
		return msgBody.toString();
	}

	private void sendEmail( String subject, String msg) {
		try {
			EmailSender.sendMail( emailAddr, emailAddr, subject, msg );
		} catch (Exception ex) {
			logger.error( "Error sending email.", ex);
		}
	}

	 protected String convertByteArrayToMsgString(BytesMessage message) throws JMSException
	 {
	     byte[] bytes = new byte[(int) message.getBodyLength()];
	     message.readBytes(bytes);
	     String msgStr = new String(bytes);
	     return msgStr;
	  }

	 private boolean isVesvoySubmited(String msgText) {
		 String msg = "parameter id=\"vesvoy\" value=\"";
		 int index = msgText.indexOf(msg);
		 if(index == -1 ) return false;
		 index += msg.length();
		 int endIndex = msgText.indexOf("\"",index);
		 String vesvoy =  msgText.substring(index, endIndex);
		 TosProcessLoggerDAO processLogger = new TosProcessLoggerDAO();
		 return !processLogger.verifyAnyExecution(vesvoy);
	 }

	 private boolean isUlk(String msgText) {
		 String msg = "class-name=\"GvyInjAulk\"";
		 int index = msgText.indexOf(msg);
		 if(index == -1 ) return false;
		 return true;
	 }

	 private boolean isBobError(String msgText) {
		 String msg = "The Order Item can not be deleted because it is referenced by the following gate transaction";
		 int index = msgText.indexOf(msg);
		 //logger.debug("bob error "+index);
		 if(index == -1 ) return false;
		 return true;
	 }

	 private boolean isHpuError(String msgText) {
		 String msg1 = "action=\"ADD HOLD\"";
		 String msg2 = "action=\"RELEASE HOLD\"";
		 int index1 = msgText.indexOf(msg1);
		 int index2 = msgText.indexOf(msg1);
		 //logger.debug("bob error "+index);
		 if(index1 == -1 && index2 == -1) return false;
		 return true;
	 }

	 private boolean isShipperError(String msgText) {
		 String msg1 = "shipper-consignee";
		 String msg2 = "ORA-00001: unique constraint";
		 int index1 = msgText.indexOf(msg1);
		 int index2 = msgText.indexOf(msg1);
		 //logger.debug("bob error "+index);
		 if(index1 == -1 || index2 == -1) return false;
		 return true;
	 }
	 //

	 private void isNewVesComplete(String msgText){
		 logger.debug("isNewVesComplete Called :"+copyPrimary);
		 ArrayList msgList = null;
		 ArrayList newvesFldLst = null;
		 String vesvoy = null;
		 String process = null;
		 String rdsDateStr = null;
		 String msg1 = "NewVes Completed";
		 String msg2 = "NIS Load Transaction Completed";
		 String msg3 = "Supplemental Data Completed";
		 String msg4 = "NEWVESSEL CODING COMPLETED";
		 try{
		 //Sends Newves Completion Mail
		 if (msgText.contains(msg2) || msgText.contains(msg3)) {
			 logger.info("Calling sendnewVesnotification for NIS Load Transaction or Supplemental Data");
			 newvesFldLst = sendNewVesNotification(msgText);
			 if (newvesFldLst != null) {
				  vesvoy = (String)newvesFldLst.get(0);
				  process = (String)newvesFldLst.get(1);
				  rdsDateStr = (String)newvesFldLst.get(2);
			 }
		 }
		 else if (msgText.contains(msg1) && "false".equalsIgnoreCase(copyPrimary)) {
			 logger.info("Calling sendnewVesnotification for FALSE or CSX");
			 newvesFldLst = sendNewVesNotification(msgText);
			 if (newvesFldLst != null) {
				  vesvoy = (String)newvesFldLst.get(0);
				  process = (String)newvesFldLst.get(1);
				  rdsDateStr = (String)newvesFldLst.get(2);
			 }
		 } else if ("true".equalsIgnoreCase(copyPrimary) && (msgText.contains(msg1) || msgText.contains(msg4) )) {
			 logger.info("This is new code in isNewVesComplete but not for CSX");
			 // This needs to be fetched from the message sent from N4 after trucker coding complete.
			 // Message format is "NEWVESSEL CODING COMPLETED-MAU796-NewVes"
			 // step 1 - extract vesvoy from the message.
			 // step 2 - read process logger table and get the rdsTriggerDate.
			 // step 3 - update this table with process type = "NV".
			 // step 4 - populate vesvoy, process, rdsDateStr variables.
			 /*if(vesvoyTriggerDateMap==null)
				 vesvoyTriggerDateMap = new HashMap<String, String>();*/
			 if (msgText.contains(msg1)) {
				 newvesFldLst = getVesvoyAndRdsDtFromMsg(msgText) ;
				 if (newvesFldLst != null) {
					  vesvoy = (String)newvesFldLst.get(0);
					  process = (String)newvesFldLst.get(1);
					  rdsDateStr = (String)newvesFldLst.get(2);
					  logger.info("rdsDateStr for msg1 "+rdsDateStr);
				 }
			 }
			 else if (msgText.contains(msg4) ) {
				 newvesFldLst = new ArrayList(Arrays.asList(msgText.split("-")));
			 }
			 if (newvesFldLst != null && newvesFldLst.size() > 2 &&  msgText.contains(msg4)) {
				 vesvoy = (String)newvesFldLst.get(1);
				 process = (String)newvesFldLst.get(2);
				 //rdsDateStr = vesvoyTriggerDateMap.get(vesvoy);
				 logger.info("isNewVesComplete new code vesvoy :"+vesvoy+" process :"+process+" rdsDateStr :"+rdsDateStr);
				 if(rdsDateStr==null){
					 TosProcessLogger loggerRecord = NewVesselDao.getProcessLoggerRecord(vesvoy);
					 if(loggerRecord!=null){
						 rdsDateStr = loggerRecord.getProcessType();
						 //vesvoyTriggerDateMap.put(vesvoy, rdsDateStr);
						 // A13 start
						 if (msgText.contains(msg4)) {
							 loggerRecord.setProcessType(TosProcessLogger.PROCESS_NEWVES);
						 	 loggerRecord.setStatus("Processed/Emailed");
						 	 NewVesselDao.updateProcessLoggerRecord(loggerRecord);
						 }
					 }
				 }
			 }
		 }

		 if(newvesFldLst == null || newvesFldLst.size() < 3){
			 logger.debug("Not a NewVesFile Execution So dont process Queue Msg");
			 return;
		 }


		 Date rdsTrigDtTime = null;
		 if(rdsDateStr != null){
			 DateFormat dateFormat =null;
			if ("true".equalsIgnoreCase(copyPrimary)) {
				if (msgText.contains(msg4))
					dateFormat = new SimpleDateFormat("MMddyyHHmm");
				else
					dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
			}else {
				logger.info("rdsDate for both copy flag false and CSX");
				dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss"); //A10
			}
		  rdsTrigDtTime = dateFormat.parse(rdsDateStr);
		  logger.debug("rdsTrigDtTime comapre Action : "+rdsTrigDtTime);
		 }

	     logger.debug("isNewVesComplete vesvoy :"+vesvoy);
	     String mailSub="Acets Queued Messages Processed for "+process+"=";
	     msgList = vesvoy != null && rdsTrigDtTime != null && vesvoy.length() > 5 ? AcetsNewVesMsgDao.sendNewVesAcetsMsg(vesvoy,rdsTrigDtTime) : null;
	     //Read HashMap and Send mail
	     if(msgList == null || msgList.size() == 0){
	     	sendEmail(mailSub+vesvoy+" count=0","No Acets Message to Process for "+vesvoy+" after Newves trigger at :"+rdsDateStr);
	    	return;
	     }
	     logger.debug("isNewVesComplete map :"+msgList.size());
	     Iterator it = msgList.iterator();
 	     StringBuilder msgStr = new StringBuilder();
 	     msgStr.append("<html><body><tr><td>Acets Message Posted into N4 after Newves trigger at :"+rdsDateStr+"</td></tr></br></br>");
 	     msgStr.append("<table border='0' width='42%' id='table1'>");
		 msgStr.append("<tr><td width='136'><u>Container Nbr</u></td><td><u>Message</u></td></tr>");
	     while(it.hasNext()){
	      String key = (String)it.next();
	      String []val = key.split(" ");
	      msgStr.append("<tr><td width='136'>"+val[0]+"</td><td>"+val[1]+"</td></tr>");

	     }//3. send Confirmation mail
	     msgStr.append("</table></body></html>");
         sendEmail(mailSub+vesvoy+" count="+msgList.size(),msgStr.toString());

         logger.debug("isNewVesComplete Ends");
	     }catch(Exception e){
	    	 e.printStackTrace();
	     }
	 }

	  public ArrayList sendNewVesNotification(String msgText)
	 {
	   logger.info("sendNewVesNotification started");
	   ArrayList arrList = null;
	   String unitCnt = null;    String vesvoy=null;
	   String rdsDateTime = null;
	   String snxNotes = null; 	 String process = null;
	   String msg1 = "NewVes Completed";
	   String msg2 = "NIS Load Transaction Completed";
	   String msg3 = "Supplemental Data Completed";
       //Check XML string
	   if(msgText.contains(msg1)){ process = "NewVes"; }
	   else if(msgText.contains(msg2)){ process =	"NIS Load Transaction"; }
	   else if(msgText.contains(msg3)){ process =	"Supplemental"; }
	   else{
		 logger.debug("Message is NOT a NewVes Completion message");
		 return null;
	   }

	     try{
	     //NewVes mailing List
	     String newVesMailList = TosRefDataUtil.getValue("EMAIL_NEW_VES");
		 String bargeMailList = TosRefDataUtil.getValue("EMAIL_BARGE");
		 String supplementalMailList = TosRefDataUtil.getValue("EMAIL_SUPP_NEWVES");

	     //Check XML Error or Response String
		 if(msgText.startsWith("<argo:snx-error")){
		   vesvoy = getFieldValue(msgText,"carrier id");
		   snxNotes = getFieldValue(msgText,"snx-update-note");
		 }else{
		   vesvoy = getFieldValue(msgText,"vesvoy");
		   snxNotes = getFieldValue(msgText,"snx-update-note");
		 }

		 int fieldIndx = snxNotes.indexOf("unitCnt");
		 if(fieldIndx != -1){
		  int equalsIndx = snxNotes.indexOf("=",fieldIndx); //A7
		  int spaceIndx = snxNotes.indexOf(" ",equalsIndx);
		  unitCnt = snxNotes.substring(equalsIndx+1,spaceIndx);
		 }
		 fieldIndx = snxNotes.indexOf("rdsDtTime");
		 if(fieldIndx != -1 && process.equals("NewVes")){
		   int equalsIndx = snxNotes.indexOf("=",fieldIndx);
		   rdsDateTime = snxNotes.substring(equalsIndx+1,equalsIndx+20);
		 }

		 logger.debug("Vesvoy :"+vesvoy+"  snxNotes :"+snxNotes+" UnitCnt:"+unitCnt+" rdsDateTime:"+rdsDateTime);
		 //Format Completion Email Text
		 String newVesMsg = process+" Process Completed for "+vesvoy+" in TOS.";
		 String supMsg = process+" Process Completed in TOS.";
		 String emailMsg = process.startsWith("N") ? newVesMsg : supMsg ;
		 emailMsg += " "+unitCnt+" units processed.";
		 logger.info("***emailMsg***"+emailMsg);
		 //NV & Barge Distribution Mail List Update
		 if(process.equals("NIS Load Transaction")&& vesvoy != null){
			EmailSender.sendMail(emailAddr,bargeMailList,emailMsg, emailMsg);
			logger.debug(process+" Email Notification Sent out to :"+emailAddr);
		 }else if (process.equals("NewVes")&& vesvoy != null && ("false".equalsIgnoreCase(copyPrimary))){
			 logger.info("***sending newvessel notification for copy false and CSX***");
		     EmailSender.sendMail(emailAddr,newVesMailList,emailMsg, emailMsg);
		     arrList = new ArrayList();
			 arrList.add(vesvoy); arrList.add(process); arrList.add(rdsDateTime);
		 } else if (process.equals("Supplemental")){
		     EmailSender.sendMail(emailAddr,supplementalMailList,emailMsg, emailMsg);
		    logger.debug(process+" Email Notification Sent out to :"+emailAddr);
		    return null;
		 }
		 if ("true".equalsIgnoreCase(copyPrimary) && msgText.contains("NEWVESSEL CODING COMPLETED")) {
			 arrList.add(vesvoy); arrList.add(process); arrList.add(rdsDateTime);
		 }
		 logger.debug(process+" Send mail for vesvoy"+vesvoy+" to "+emailAddr);
	   }catch(Exception e){
		   e.printStackTrace();
	   }
		return arrList;
	 }

	 //A6
	 public void sendSnxErrorMail(String msg){
		 try{
			String errorEmailGrp = null;
			String snxNotes = getFieldValue(msg,"snx-update-note");
			snxNotes = snxNotes != null ? snxNotes : "";

			if(snxNotes.startsWith("NewVes")){
				errorEmailGrp = TosRefDataUtil.getValue("EMAIL_NEWVES_ERROR");
			}else if(snxNotes.startsWith("NIS Load")){
				errorEmailGrp = TosRefDataUtil.getValue("EMAIL_BARGE_ERROR");
			}else if(snxNotes.startsWith("Supplemental")){
				errorEmailGrp = TosRefDataUtil.getValue("EMAIL_SUP_ERROR");
			}else if(snxNotes.startsWith("Stowplan")){
				errorEmailGrp = TosRefDataUtil.getValue("EMAIL_STIF_ERROR");
			}else if(msg.indexOf("vessel-visit next-facility") != -1){
				errorEmailGrp = TosRefDataUtil.getValue("EMAIL_VESVISIT_ERROR");
			}

			if(errorEmailGrp == null){
				sendEmail(groovyMsg, msg);
				return;
			}
			EmailSender.sendMail(emailAddr,errorEmailGrp,snxMsg,msg);

		 }catch(Exception e){
			 logger.error("Exception While Posting Snx Error Mail :"+e);
		 }
	 }


	 public static String getFieldValue(String msgText, String field)
	  {
	     String fieldValue = "";
	      try
	     {
	       int fieldIndx = msgText.indexOf(field);
	       if(fieldIndx == -1){ return null; }

	       int equalsIndx = msgText.indexOf("=",fieldIndx);
	       int nextspace = msgText.indexOf("\"", equalsIndx+2);
	       fieldValue  = msgText.substring(equalsIndx+2, nextspace);
	       //println("equalsIndx:"+equalsIndx+"  nextspace:"+nextspace+" oldValue:"+fieldValue);
	      }catch(Exception e){
	        e.printStackTrace();
	     }
	      return fieldValue;
	   }
	 	 
	  public ArrayList getVesvoyAndRdsDtFromMsg(String msgText)
		 {
		   logger.info("sendNewVesNotification started");
		   ArrayList arrList = new ArrayList();
		   String unitCnt = null;    String vesvoy=null;
		   String rdsDateTime = null;
		   String snxNotes = null; 	 String process = null;
		   String msg1 = "NewVes Completed";
	       //Check XML string
		   if(msgText.contains(msg1)){ process = "NewVes"; }
		   else{
			 logger.debug("Message is NOT a NewVes Completion message");
			 return null;
		   }
		     try{
		     //Check XML Error or Response String
			 if(!msgText.startsWith("<argo:snx-error")){
			   vesvoy = getFieldValue(msgText,"vesvoy");
			   snxNotes = getFieldValue(msgText,"snx-update-note");
			 }

			 int fieldIndx = snxNotes.indexOf("rdsDtTime");
			 if(fieldIndx != -1 && process.equals("NewVes")){
			   int equalsIndx = snxNotes.indexOf("=",fieldIndx);
			   rdsDateTime = snxNotes.substring(equalsIndx+1,equalsIndx+20);
			 }

			 logger.debug("Vesvoy :"+vesvoy+"  snxNotes :"+snxNotes+" rdsDateTime:"+rdsDateTime);
			 if ("true".equalsIgnoreCase(copyPrimary)) {
				 arrList.add(vesvoy); arrList.add(process); arrList.add(rdsDateTime);
			 }
			 logger.debug(process+" Send mail for vesvoy"+vesvoy+" to "+emailAddr);
		   }catch(Exception e){
			   e.printStackTrace();
		   }
			return arrList;
		 }

}
