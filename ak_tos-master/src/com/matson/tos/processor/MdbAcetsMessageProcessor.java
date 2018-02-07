package com.matson.tos.processor;

import com.matson.cas.refdata.mapping.TosProcessLoggerDAO;
import com.matson.tos.dao.AcetsNewVesMsgDao;
import com.matson.tos.dao.TosLookup;
import com.matson.tos.exception.TosException;
import com.matson.tos.jaxb.snx.Snx;
import com.matson.tos.messageHandler.*;
import com.matson.tos.util.EmailSender;
import com.matson.tos.util.EnvironmentProperty;
import com.matson.tos.util.JMSSender;
import com.matson.tos.util.TosRefDataUtil;
import org.apache.log4j.Logger;

import javax.ejb.EJBException;
import javax.ejb.MessageDrivenContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

/**
 * 
 * @author JZF
 * Change History
 * A1 SKB 03/02/2009 Added exception handling for non-numeric values.
 *                   Added email if there is an unhandled exeption.  
 * A2 SKB 04/09/2009 Double send Booking to work around line update issue.
 * A3 SKB 04/15/2009 Reorder and slow down booking double send.
 * A4 SKB 10/14/2009 Stored some messageHandler to improve message.
 * This class ia a message EJB and handles all messages in the queue defined in the
 * ejb-jar.xml file. Currently all messages are sent from ACETS. 
 * A5 SKB 11/12/2009 Closed lookup connection.
 * A6 GR  11/19/2009 Added Category Check for Newves Acets Msg Queue.
 * A7 GR  07/16/2010 Added Booking Wait time lookup to re-submit 
 * A8 GR  12/06/2011 Add Thread wait if msg LNK or ULK (to order/sequence msg posting into N4) 
 */

public class MdbAcetsMessageProcessor {
	private static final java.lang.String PIPE_SPLIT = "�";
	private static Logger logger = Logger.getLogger(MdbAcetsMessageProcessor.class);
	private MessageDrivenContext _context;
	private static IMessageHandler atrxMessageHandler;
	private static IMessageHandler aardMessageHandler;
	private static IMessageHandler ahlxMessageHandler;
	private static IMessageHandler alnkMessageHandler;
	private static IMessageHandler aulkMessageHandler;
	String supportMail = TosRefDataUtil.getValue("SUPPORT_EMAIL");
	String acetsBkgWait = TosRefDataUtil.getValue("ACETS_BKG_WAIT");
	String lnkUlkWait = TosRefDataUtil.getValue("LNK_ULK_WAIT");
	long acetsBkgwaitTime = Long.parseLong(acetsBkgWait); 
	long lnkUlkWaitTime = Long.parseLong(lnkUlkWait);
	
	static {
		EnvironmentProperty.configure();
	}
	
	public MdbAcetsMessageProcessor() {
		// TODO Auto-generated constructor stub
		
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

	public void onMessage(Message msg) {
		// TODO Auto-generated method stub
		TextMessage tm = (TextMessage) msg;
	    try {
	    	String text = tm.getText();
	    	logger.debug("Received new message : " + text);
		    processMsg( text);
	    } catch(JMSException ex) {
	    	logger.error( "Exception: ", ex);
	    	ex.printStackTrace();
	    }
	}
	

	public void processMsg( String msgText) {
		IMessageHandler msgHandler = null;
		String xml;
		// check the message types and create a handler
		try {
			if ( msgText.startsWith( "ATRA") || msgText.startsWith( "ATRU") ||
					msgText.startsWith( "ATRD")) { 
				// process TRA, TRU, and TRD from ACETS
				if(atrxMessageHandler == null) {
					atrxMessageHandler =    new AtrxMessageHandler( "com.matson.tos.jaxb.snx",
						"com.matson.tos.jatb", "/xml/atrx.xml", AbstractMessageHandler.TEXT_TO_XML);
				}
				msgHandler = atrxMessageHandler;
			} else if ( msgText.startsWith( "AHLP") || msgText.startsWith( "AHLR")) {
				// process HLP and HLR messages
				if(ahlxMessageHandler == null) {
					ahlxMessageHandler = new AhlxMessageHandler( "com.matson.tos.jaxb.snx",
						"com.matson.tos.jatb", "/xml/ahlx.xml", AbstractMessageHandler.TEXT_TO_XML);
				}
				msgHandler = ahlxMessageHandler;
			} else if ( msgText.startsWith( "AARD")) {
				// process ARD messages
				if(aardMessageHandler == null) {
					aardMessageHandler = new AardMessageHandler( "com.matson.tos.jaxb.snx",
						"com.matson.tos.jatb", "/xml/aard.xml", AbstractMessageHandler.TEXT_TO_XML);
				}
				msgHandler = aardMessageHandler;
			} else if ( msgText.startsWith( "ALNK")) {
//				// process LNK messages
				if(alnkMessageHandler == null) {
					alnkMessageHandler = new AlnkMessageHandler( "com.matson.tos.jaxb.snx",
						"com.matson.tos.jatb", "/xml/alnk.xml", AbstractMessageHandler.TEXT_TO_XML);
				} 
				msgHandler = alnkMessageHandler;
			} else if ( msgText.startsWith( "AULK")) {
//				// process ULK messages
				if(aulkMessageHandler == null) {
					aulkMessageHandler = new AulkMessageHandler( "com.matson.tos.jaxb.snx",
						"com.matson.tos.jatb", "/xml/aulk.xml", AbstractMessageHandler.TEXT_TO_XML);
				} 
				msgHandler = aulkMessageHandler;
			} else if ( msgText.startsWith( "ABOB")) {
//				// process BOB messages. First send out booking update message
				msgHandler = new AbobMessageHandler( "com.matson.tos.jaxb.snx",
						"com.matson.tos.jatb", "/xml/abob.xml", AbstractMessageHandler.TEXT_TO_XML);
				JMSSender sender = new JMSSender( JMSSender.REAL_TIME_QUEUE, "HON");
				if ( msgHandler != null) {
					msgHandler.setTextStr( msgText);				
					xml = msgHandler.getXmlStr();
					logger.debug( "XML:" + xml);
					sender.send( xml);
					
					
				}
				// Second send out HOLD/RELEASE message if needed.
				
				IMessageHandler msgHandlerHold = new AbobHpuMessageHandler( "com.matson.tos.jaxb.snx",
						"com.matson.tos.jatb", "/xml/abob.xml", AbstractMessageHandler.TEXT_TO_XML);
				// it's possible that HPU is null and throws exception.
				try {
					msgHandlerHold.setTextStr( msgText);	
					xml = msgHandlerHold.getXmlStr();
					if(xml != null) {
						logger.debug( "XML:" + xml);
						sender.send( xml);
					}
					msgHandlerHold = null;
				} catch (TosException ex) {
					logger.debug("Not a BOB hold in BOB message");
					msgHandlerHold = null;
				} catch ( Exception ex) {
					//logger.debug( "Reset msgHandler to null to avoid exception thrown out.");
					logger.error("Could not process BOB HPU", ex);
					msgHandlerHold = null;
				}
				
				// Slow it down, A3
				Thread.sleep(acetsBkgwaitTime);
				if ( msgHandler != null) {
					// Resend in case of line errors.
					((AbobMessageHandler)msgHandler).setLinesIncluded(false);
					xml = msgHandler.getXmlStr();
					logger.debug( "XML:" + xml);
					sender.send( xml);
				}
				msgHandler = null;
			} else if ( msgText.startsWith( "ACAR")) {
//				// process CAR messages
				msgHandler = new AcarMessageHandler( "com.matson.tos.jaxb.snx",
						"com.matson.tos.jatb", "/xml/acar.xml", AbstractMessageHandler.TEXT_TO_XML);
			} else if ( msgText.startsWith( "ABDA")) {
//				// process BDA messages
				msgHandler = new AbdaMessageHandler( "com.matson.tos.jaxb.snx",
						"com.matson.tos.jatb", "/xml/abda.xml", AbstractMessageHandler.TEXT_TO_XML);
			} else if ( msgText.startsWith( "ABDB")) {
//				// process BDB messages
				msgHandler = new AbdbMessageHandler( "com.matson.tos.jaxb.snx",
						"com.matson.tos.jatb", "/xml/abdb.xml", AbstractMessageHandler.TEXT_TO_XML);
			} else if ( msgText.startsWith( "AUP1") || msgText.startsWith( "AUP2") || msgText.startsWith( "AUP3")) {
//				// process UPD messages
				msgHandler = new AupdMessageHandler( "com.matson.tos.jaxb.snx",
						"com.matson.tos.jatb", "/xml/aupd.xml", AbstractMessageHandler.TEXT_TO_XML);
			} else if (msgText.startsWith("ABDX")) {
				//todo, kramachandran get bdx.xml & ranem as abdx.xml in /src/xml/?
				/**
				 * There is no need for XML message handler here, we'll need an text (csv) file->xml-> POJO conversion
				 * it's one of case, we will use delimiter & regex to parse the message
				 */
				logger.info("ABDX Message processor started");
				if (msgText != null) {
					String[] strings = msgText.split(PIPE_SPLIT);
					for (String s : strings) {
						logger.info(s);
					}
					if (strings != null && strings.length >= 6) {
						String _containerNumber, _billNumber;
						_containerNumber = strings[5];
						_billNumber = strings[6];
						try {
							if (_containerNumber != null && _containerNumber.length() > 0) {
								_containerNumber = _containerNumber.trim();
							} else {
								//throw new TosException("_containerNumber cannot be null for ABDX Message");
							}
							if (_billNumber != null && _billNumber.length() > 0) {
								_billNumber = _billNumber.trim();
							} else {
								throw new TosException("_billNumber cannot be null for ABDX Message");
							}
							postHazardToTOS(_billNumber, null);
						} catch (TosException tex) {
							logger.error("Error:", tex);
							tex.printStackTrace();
							EmailSender.sendMail(supportMail, supportMail, "Exception Processing Acets Message " + tex.getMessage(), msgText);
						} catch (Exception ex) {
							logger.error("Error:", ex);
							ex.printStackTrace();
							EmailSender.sendMail(supportMail, supportMail, "Exception Processing Acets Message " + ex.getMessage(), msgText);
						}
					}
				}
			} else {
				logger.debug( "Unknown message received:" + msgText);
			}
			// convert the text message to xml message and send to JMS
			if (msgHandler != null && !msgText.startsWith("ABDX")) {
				msgHandler.setTextStr(msgText);
				xml = msgHandler.getXmlStr();
				/*logger.debug( "XML:" + xml);
				JMSSender sender = new JMSSender( JMSSender.REAL_TIME_QUEUE, "HON");
				sender.send( xml);*/
                boolean messageinQueue = false;   
				if(msgText.startsWith( "ABDA") || msgText.startsWith( "ABDB") || msgText.startsWith( "ALNK")){
					messageinQueue = isMessageQueued(msgText,xml);
				}//Msg check IF 
				if(!messageinQueue){
				  logger.debug( "XML:" + xml);
				  JMSSender sender = new JMSSender( JMSSender.REAL_TIME_QUEUE, "HON");
				  sender.send( xml);
				}
				//A8 - Add wait if msg LNK or ULK (to give some time / to sequence transactions)
				if(msgText.startsWith( "ALNK") || msgText.startsWith( "AULK")){
					Thread.sleep(lnkUlkWaitTime);
				}

			} else {
				logger.error( "Can not create message handler.");
			}
			//
		} catch ( TosException tex) {
			logger.error( "Error:", tex);
			tex.printStackTrace();
			EmailSender.sendMail(supportMail, supportMail, "Exception Processing Acets Message "+tex.getMessage(), msgText);
		} catch ( Exception ex) {
			logger.error( "Error:", ex);
			ex.printStackTrace();
			EmailSender.sendMail(supportMail, supportMail, "Exception Processing Acets Message " + ex.getMessage(), msgText);
		}
	}

	private void postHazardToTOS(String inBillNumber, String inContainerNumber) throws Exception {
		logger.info("inContainerNumber	" + inContainerNumber + "inBillNumber" + inBillNumber);
		AbdxJSONMessageHandler jsonMessageHandler = new AbdxJSONMessageHandler();
		logger.info("AbdxJSONMessageHandler instance created" + jsonMessageHandler);
		for (Snx snx : jsonMessageHandler.getSNXMessage(inBillNumber, inContainerNumber)) {
			String fromSnxObject = jsonMessageHandler.getXMLStringFromSnxObject(snx);
			//todo, kramachandran publish in required queue, what we have in above string is xml
			logger.info(fromSnxObject);
			JMSSender sender = new JMSSender(JMSSender.REAL_TIME_QUEUE, "ANK");
			sender.send(fromSnxObject);
		}
	}

	public boolean isMessageQueued(String msgText, String xml)
	 {
	   boolean newvesProcessed = false;  boolean isBarge = false;
       String category = null;   
         
	   try
	   {
		 //1.Dont queue Barges Messages
		String vesvoy = getFieldValue(xml, "vesvoy");	
		if(vesvoy != null && (vesvoy.endsWith("A") || vesvoy.endsWith("B") 
				|| vesvoy.endsWith("C") || vesvoy.endsWith("D"))){
		    logger.debug("Dont Queue Barge Message:"+vesvoy);
		 	isBarge = true;
		}else{	
		//2. Get Category for Most Active unit / Not active unit return null.	
		  TosLookup lookup = new TosLookup();
		  String unitNbr = getUnitNbr(xml);
		   try{		
		    category = unitNbr != null && unitNbr.length() > 0 ?
		          lookup.getActiveUnitCategory(unitNbr, "HON") : null;
		     logger.debug("unitNbr :"+unitNbr+"category :"+category);
		     
		   }catch(Exception ex){
			  logger.error("Error in getting active unit :"+ex);
		   } finally {
			   //A5, closed connection.
				if(lookup != null) lookup.close();
		   }
		}
			
		//3. A6 If Not Barge and Category is Null then unit is departed and if category=Imprt then check for Newves
		if(!isBarge && (category == null || "IMPRT".equals(category) || "TRSHP".equals(category) 
				|| "THRGH".equals(category) )&& !isNewVesExecute(vesvoy)){
		    String msgType = getMessageType(msgText); 
		    logger.debug( "isNewVesExecute: false"+" MsgType :"+msgType);
			  	newvesProcessed = AcetsNewVesMsgDao.insertMsg(msgType,xml);
			  	logger.debug("Msg held for Newves:-1"+xml );
		}
      }catch(Exception exp){
		 logger.error("Error in Method isMessageQueued :"+exp);
	  }
		 return newvesProcessed;
	 }
	
	
	 public boolean isNewVesExecute(String vesvoy){
		 String msgType = null;
		 boolean isExecuted = false; 
		 try{
	 		logger.debug("Vesvoy ::"+vesvoy);
	 		//Check if Vessel is executed
	 		if(vesvoy != null && vesvoy.length() > 5){
		 	  TosProcessLoggerDAO processLogger = new TosProcessLoggerDAO();
		 	  isExecuted = processLogger.verifyProcessExecution(vesvoy, "NV");
		    }
		 }catch(Exception e){
            e.printStackTrace();			 
		 }
		 return isExecuted;
	 }
	 
	 public String getUnitNbr(String xml){
		 String unitNbr = ""; 
		 try{
	 		unitNbr = getFieldValue(xml, "equipment-id");
		 }catch(Exception e){
            e.printStackTrace();			 
		 }
		 return unitNbr;
	 }
	
	 public String getMessageType(String msgText){
		 String msgType = null;
		 if(msgText.startsWith( "ABDA")) msgType = "BDA"; 
 		 else if(msgText.startsWith( "ABDB")) msgType = "BDB"; 
 		 else if(msgText.startsWith( "ALNK")) msgType = "LNK";
		 return msgType;
	 }
	 
	 public static String getFieldValue(String msgText, String field)
	  {
	     String fieldValue = "";
	      try
	     {
	       int fieldIndx = msgText.indexOf(field);
	       int equalsIndx = msgText.indexOf("=",fieldIndx);
	       int nextspace = msgText.indexOf("\"", equalsIndx+2);
	       fieldValue  = msgText.substring(equalsIndx+2, nextspace);
	       //println("equalsIndx:"+equalsIndx+"  nextspace:"+nextspace+" oldValue:"+fieldValue);
	      }catch(Exception e){
	        e.printStackTrace();
	     }
	      return fieldValue;
	   }

}
