/*
 *Srno   Date			AuthorName			Change Description
 *A1     03/28/2010     GR                  Map Event Action and moved topic publish after error check.  
 *A2     07/26/10       GR                  Compute Action and Leg value Before sending Message to Topic.
 *A3     10/25/10       GR                  Rules to post messages to the Topic.
 *A4     01/11/10       GR   				Post msg xml to Gems SAF (N4 -> Gems Communication)
 *A5     01/19/11       GR 					N4 to Gems posting Flag
 *A6     02/23/11       GR 					Create Only Cmis FlatFile for ATRA,ATRU,ATRD [Added Filter]
 *A7     02/23/11       GR 					Remove % Set Null if value is Percentage
 */
package com.matson.tos.processor;

import java.util.Map;

import org.apache.log4j.Logger;

import com.matson.cas.refdata.mapping.TosProcessLoggerDAO;
import com.matson.tos.dao.TosLookup;
import com.matson.tos.exception.TosException;
import com.matson.tos.groovy.writer.MessageConfigCmis;
import com.matson.tos.groovy.writer.ObjectWriter;
import com.matson.tos.groovy.writer.ObjectWriterFactory;
import com.matson.tos.util.EmailSender;
import com.matson.tos.util.GroovyXmlUtil;
import com.matson.tos.util.JMSTopicSender;
import com.matson.tos.util.JMSSender;
import com.matson.tos.util.TosRefDataUtil;
import com.matson.tos.util.StrUtil;
import java.util.Iterator;
import com.matson.tos.groovy.writer.FieldDataCmis;;

/**
 * @author JZF
 *
 */
public class GroovyMessageProcessor {
	private static Logger logger = Logger.getLogger(GroovyMessageProcessor.class);
		
	public static String GVY_RET_TAG = "gvyReturn";
	private String _gvyMessage;
	private String emailAddr = TosRefDataUtil.getValue( "SUPPORT_EMAIL");
	// Made this static
	private static String topic = TosRefDataUtil.getValue("TOPIC_N4");
	private String n4GemsSaf = TosRefDataUtil.getValue("N4_GEMS_SAF"); //A4
	private String gemsPostingFlg = TosRefDataUtil.getValue("N4_GEMS_FLAG");
    private static JMSTopicSender topicSender = new JMSTopicSender(topic);
    private static JMSSender gemsSaf = null;
	
	public GroovyMessageProcessor( String gvyMessage) {
		_gvyMessage = gvyMessage;
	}
	
	public boolean process() {
		TosLookup lookUp = null;
		try {
			gemsSaf = new JMSSender(n4GemsSaf, false);
			boolean n4GemsFlag = Boolean.parseBoolean(gemsPostingFlg);
			String bookingNbr = null;
			Map data = getMap();
			//check if the message is an error msg
			if ( isErrorMsg( data)) {
				return true;
			}
			
		    //A2 - Set Action & Leg  
			data = setActionLegValue(data);
			//A3,A5 : Rules to post messages to the Topic and Gems SAF if Flag is True .
			bookingNbr = (String)data.get("bookingNumber");
			boolean sendTo = true; 
			if (bookingNbr!=null && bookingNbr.contains("DO NOT EDIT")) {
				sendTo = false;
			}else {
				sendTo = true;
			}
			logger.info("bookingNumber before sending to gems :"+bookingNbr+" sendTo :"+sendTo);
			if(isPostingRuleValid(data) && sendTo){
			    publishTopic();  
			    if(n4GemsFlag)
			    { 
			    	// Code Fix to restrict message to be posted to GEMS when Message type is CMIS_DATA_REFRESH and action is NVI - Start
			    	String msgType = (String)data.get( ObjectWriterFactory.MSG_TYPE);
			    	String action = (String)data.get("action");
			    	logger.info("msgType:::"+msgType+"and action ::::"+action);
			    	if(msgType!=null && msgType.equalsIgnoreCase("CMIS_DATA_REFRESH") && action!=null && action.equalsIgnoreCase("NVI"))
			    	{
			    		logger.info("Message will not be posted to GEMS Queue when Message Type is CMIS_DATA_REFRESH and Action is NVI");
			    	}
			    	else
			    	{
			    		logger.info("Calling postToGemsSaf()::::");
			    		postToGemsSaf();
			    	}
			    	// Code Fix to restrict message to be posted to GEMS when Message type is CMIS_DATA_REFRESH and action is NVI - End
			    }
			    logger.debug( "Published Msg: " + _gvyMessage);
			}else{
				logger.debug("Msg not posted to Topic="+_gvyMessage);
			}
			
			// Begin Stopping cargo status messages if the client vessel is not completed.
			String msgType = (String)data.get("msgType");
			logger.info("Message type before condition check is :"+msgType);
			if (msgType!=null && "CARGO_STATUS".equalsIgnoreCase(msgType)) {
				String vesvoy = (String)data.get("Vesvoy");
				logger.info("vesvoy inside condition check :"+vesvoy);
				if (vesvoy !=null) {
					try
					{
						lookUp = new TosLookup();
						logger.info("getting lookup!!!");
					}
					catch (Exception ex1)
					{
						logger.error("Failed to create TosLookup in GroovyMessageProcessor \n"+ ex1);
						return false;
					}
					String vesselService = lookUp.getVesselService(vesvoy);
					logger.info("vesselService before condition check is :"+vesselService);
					if (vesselService!=null && "CLI".equalsIgnoreCase(vesselService)) {
						boolean loggerFlag = new TosProcessLoggerDAO().verifyProcessExecution(vesvoy, "NV");
						logger.info("loggerFlag inside check is "+loggerFlag);
						if (!loggerFlag) {
							logger.info("Stopping CARGO_STATUS message since cliet vessel is not completed");
							return false;
						}
					}
					
				}
			}
			// End Stopping cargo status messages if the client vessel is not completed.
			// if not an error msg, process it
			ObjectWriter writer = ObjectWriterFactory.getWriter( data);
			if ( writer == null) {
				logger.debug( "Unknown/unprocess Groovy msgType: " + (String)data.get( ObjectWriterFactory.MSG_TYPE));
				return false;
			}
			// 
			boolean writerSucessFlag = writer.write();
			if (writerSucessFlag) {
				logger.debug( "Writting successful.");
				return true;
			} 
			else
			{	
				logger.debug( "Writting failed.");
				return false;
			}
		} catch ( TosException tex) {
			logger.error( "Error in processing Groovy message.", tex);
			String value = _gvyMessage.replace("'", "");
			value = org.apache.commons.lang.StringEscapeUtils.escapeXml(value);
			EmailSender.sendMail(emailAddr, emailAddr, "Error in processing/Parsing Groovy message", "Error message :"+value);
		}finally {
			if (lookUp != null)
			{
				logger.info( "Closing TosLookUp in GroovyMessageProcessor");
				lookUp.close();
				lookUp = null;
			}
		}
		return false;
	}
	
	private Map getMap() throws TosException {
		return GroovyXmlUtil.getMsgMap( _gvyMessage);
	}
	
	private boolean isErrorMsg( Map data) {
		String msgType = (String)data.get( ObjectWriterFactory.MSG_TYPE);
		if ( msgType.equalsIgnoreCase( "GVY_INJ_RET")) {
			String retStr = (String)data.get( GVY_RET_TAG);
			logger.debug( "Groovy return msg: " + retStr);
			if ( retStr.startsWith("ERR_") || retStr.indexOf( "ERR_GVY_") > 0) {
				EmailSender.sendMail( emailAddr, emailAddr, "SN4 Error Message", retStr );
				logger.debug( "Send error msg to " + emailAddr);
				return true;
			}
			logger.debug( "Ignore message type: " + msgType);
			return true;
		}
		return false;
	}
	
	public void publishTopic() {
		if(topic == null ) return;
		try {
			topicSender.send(_gvyMessage);	
		} catch (Exception e) {
			logger.error("Could not send to topic "+topic,e);
		}
		
	}
	
	//A4 - (N4 ->Gems Communication) 
	public void postToGemsSaf() {
		if(n4GemsSaf == null ) return;
		try {
			gemsSaf.send(_gvyMessage);	
		} catch (Exception e) {
			logger.error("Could not send Gems SAF Agent"+n4GemsSaf,e);
		}
		
	}
	
	/* ---- Moved Method out of FTPWriterCmis class -----
	 * Method Maps the CMIS leg Value by fetching the Vessel Direction 
	 * Based on the Load port and Destination Port 
	 */
	public String getVoyageDirectionCode(String fieldValue, MessageConfigCmis messageConfig,Map _data)
	{
	  String directionCode = "null";  //A7
	  try
	  {
	    if(fieldValue.indexOf("_") == -1){
	    	String warningMsg = "Unit:"+_data.get("ctrNo")+"   leg value not correct for Evnt:"+_data.get("msgType"); 
	    	EmailSender.sendMail(emailAddr, emailAddr,warningMsg,warningMsg);
	    	return "null";
	    }
		String loadPort = fieldValue.substring(0,fieldValue.indexOf("_"));
		String dischargePort = fieldValue.substring(fieldValue.indexOf("_")+1);
		logger.debug("getVoyageDirectionCode.loadport :"+loadPort);
		logger.debug("getVoyageDirectionCode.dischargePort :"+dischargePort);
		//Setting dport='OPT' condition for INGATE MTY Blank Leg Value 
		if(dischargePort.equals("OPT")){
			return directionCode="null";
		}else if(loadPort.equals("null") || dischargePort.equals("null")){
			return directionCode="null";
		} 
		
		Map<String, FieldDataCmis> legMapping = messageConfig.getLegMapping(loadPort);
		
		//If Load Port is not mapped pass Percentage 
		if(legMapping == null){
			return directionCode;
		} 
		Iterator legIter = legMapping.entrySet().iterator();
		while (legIter.hasNext())
		{
		  Map.Entry legFieldCode = (Map.Entry) legIter.next();
		  
		  String toPort = (String)legFieldCode.getKey();
		  String legCode = (String)legFieldCode.getValue();
      	  logger.debug("getVoyageDirectionCode.toPort :"+toPort+" legCode :"+legCode);	 
		  if(dischargePort.equalsIgnoreCase(toPort)){
			  directionCode = legCode;
			  break;
		  }
		 }//While Ends
	  }catch(Exception e){
			logger.error("Leg value not correct : " + e);
	  }
	  logger.debug("directionCode-----"+directionCode);
	  return directionCode;
	}

	
	public Map setActionLegValue(Map data){
		//A1 - Fetch Cmis Action
		String unitClass = (String)data.get("unitClass");
		if (unitClass != null && !(unitClass.equals("CONTAINER") || unitClass.equals("CHASSIS") || unitClass.equals("ACCESSORY"))){
			logger.debug("Dont Set action Leg Not Cntr/Chas/Acry");
			return data;
		}
		MessageConfigCmis instance = MessageConfigCmis.getInstance();
		String n4msgType = (String)data.get(ObjectWriterFactory.MSG_TYPE);
		String cmisRelatedAction = instance.getCmisActionName(n4msgType);
		String lastAction = (String)data.get("lastAction");
		String action = (String)data.get("action");
		String leg = (String)data.get("leg");
		leg = leg == null ? "null" : leg;
		logger.debug("setActionLegValue.cmisRelatedAction :"+cmisRelatedAction+" ,lastAction :"+lastAction+" ,action :"+action);
		logger.debug("setActionLegValue.legFromData------- :"+leg);
		
		//Replace 
		if(lastAction != null && lastAction.equals("null")){
			_gvyMessage = _gvyMessage.replace("lastAction='null'","lastAction='"+cmisRelatedAction+"'");
			data.put("lastAction", cmisRelatedAction);
			//logger.debug("lastAction Computed --------------------- :"+cmisRelatedAction);
		}if(action != null && !action.equals("%") && action.equals("null")){
		    _gvyMessage = _gvyMessage.replace("action='null'","action='"+cmisRelatedAction+"'") ;
		    data.put("action", cmisRelatedAction);
		    //logger.debug("action Computed --------------------- :"+cmisRelatedAction);
		}if(!(leg.equals("null") || leg.trim().length() == 1)){
			String legDir = getVoyageDirectionCode(leg,instance,data);
			  logger.debug("setActionLegValue.LegComputed --------------------- :"+legDir);
			 _gvyMessage = _gvyMessage.replace("leg='"+leg+"'","leg='"+legDir+"'") ;
			data.put("leg", legDir);
		}
		return data;
	}
	
	/*
	 * Rules to post messages to the Topic
	 */
	public boolean isPostingRuleValid(Map data){
		boolean isValid = true;
		
		//1. Msg Type=NULL then dont post
		//2. Dont post Cargo status Msg
		String msgType = (String)data.get("msgType");
		if(msgType == null ){isValid = false; }
		else if(msgType != null && msgType.equalsIgnoreCase( "CARGO_STATUS")){
			isValid = false;
		}else if(msgType != null && msgType.equals("TRUCKER")){ //A6
		    isValid = false;
		}
		return isValid; 
	}
/*	public static void main(String args[]){
		try{
			String msg ="<GroovyMsg msgType='UNIT_IN_GATE'  unitClass='CONTAINER' ctrNo='451674' checkDigit='7' chassisNumber='null' chassisCd='null' category='STRGE' accessory='null' mgWeight='null' tareWeight='10000' typeCode='D45 96ST' hgt='090600' strength='V' owner='MATU' damageCode='null' srv='MAT' temp='null' tempMeasurementUnit='null' equipTypeCode='D45H'  locationRow='MAT' cWeight='10000' seal='null' stowRestCode='null' stowFlag='null' odf='null'  bookingNumber='null' consignee='null' shipper='null' cneeCode='null' hazF='null' hazImdg='null' hazUnNum='null' flex01='null' locationCategory='null' arrDate='null' consigneePo='null' restow='NONE' shipperId='null'  commodity='MTY' dir='MTY' dsc='null' planDisp='null' ds='%' orientation='E'  shipperPool='null' dischargePort='OPT' dPort='OPT' loadPort='null' retPort='null' overLongBack='0' overLongFront='0' overWideLeft='0' overWideRight='0' overHeight='0'  loc='null' cell='null' locationTier='null' locationStatus='1' locationStallConfig='null'  vesvoy='null' truck='PACT' misc1='null' actualVessel='null' actualVoyage='null' leg='null'  hsf7='null' pmd='null' locationRun='%' misc2='null' misc3='null'  action='null' aDate='03/31/2010' aTime='15:08:44' doer='3graposo' sectionCode='Z' lastAction='null' lastADate='03/31/2010' lastATime='15:08:44' lastDoer='3graposo' hazOpenCloseFlag='null' aei='null' dss='%' erf='null'  comments='null' crStatus='null' cargoNotes='null'  safetyExpiry='%' lastInspection='%' lastAnnual='%' chassisAlert='%' mgp='null' chassisHold='MAT' chassisNotes='%' chassTareWeight='null'  gateTruckCmpyCd='PACT' gateTruckCmpyName='PACIFIC TRANSFER LLC' gateTruckId='TGY56' batNumber='345' turnTime='null' gateSeqNo='10090150843' laneGateId='SI GATE' deptTruckCode='null' deptDport='HON' deptVesVoy='MLE040' deptOutGatedDt='03/31/2010' deptConsignee='ROSS STORES INC #315' deptCneeCode='0038211410' deptBkgNum='7133647' deptMisc3='1006110075' deptCargoNotes='N/P HWAI, ROSS STORES DEPARTMENT STORE' />";
			GroovyMessageProcessor gvymsg = new GroovyMessageProcessor(msg);
			gvymsg.process();
			
		}catch(Exception e){
			
		}
	}*/
}
