/*
 * A1 - Adding Trucker and vessel lookup 
 * Issues Addressed 
 * 1) Ingate Chassis as %
 * 2) Map Me as FE
 * 3) Post File to Guam users
 * 4) Set Destination as Discharge port
 * 5) Map PacificIsland Movers
 * 6) Making OverDimensionFields as % in the static xml Attributes
 * 7) Adding TI trans mapping 
 * 8) Adding Seal,Weight,HazFlag,Operator
 * 9) Mask only POD=OAK to LAX
 * 10)Pass Dummy Haz values for Ingate
 * 11) OGP for outgate Empty 
 * 12) Masked SEA=HON and Add Trans code TM
 * 13) Guam Skip non-mat ingate and outgate transactions 
 */
package com.matson.tos.messageHandler;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.matson.cas.refdata.RefDataLookup;
import com.matson.cas.refdata.mapping.TosDestPodData;
import com.matson.cas.refdata.mapping.TosTruckerLookup;
import com.matson.cas.refdata.mapping.TosVesselLookup;
import com.matson.tos.exception.TosException;
import com.matson.tos.groovy.writer.MessageConfigCmis;
import com.matson.tos.jatb.GuamGateTxn;
import com.matson.tos.processor.GroovyMessageProcessor;
import com.matson.tos.util.CalendarUtil;
import com.matson.tos.util.EmailSender;
import com.matson.tos.util.TosRefDataUtil;


public class GuamGateTxnMessageHandler extends AbstractMessageHandler {
	
	private static Logger logger = Logger.getLogger(GuamGateTxnMessageHandler.class);
	private static String dateFormat = "MM/dd/yyyy";
	private static String timeFormat = "HH:mm:ss";
	private String supportMail = TosRefDataUtil.getValue("SUPPORT_EMAIL");
	private String gumSupport = TosRefDataUtil.getValue("GUAM_SUPPORT_EMAIL");	
	
	public GuamGateTxnMessageHandler(String xmlObjPackageName,
			String textObjPackageName, String fmtFile, int convertDir)
			throws TosException {
		super(xmlObjPackageName, textObjPackageName, fmtFile, convertDir);
		// TODO Auto-generated constructor stub
	}
	
	public String createXmlStr() throws TosException {
		String xmlOutMsg = null;
		Map<String, String> data = new LinkedHashMap<String, String>();
		GuamGateTxn textObj = (GuamGateTxn)createTextObj();
		String transCode = textObj.getTransCode() != null ? textObj.getTransCode().trim() : null;
		String recType = textObj.getAction() != null ? textObj.getAction().trim() : null ;
		String operator = textObj.getOperator() != null ? textObj.getOperator().trim() : "";
		String cntrNbr = textObj.getContainerNbr() != null ?  textObj.getContainerNbr().trim() : "";
		
		//For Guam Ingate and outgate only process MAT containers 
		if("OG".equals(transCode) && "TM".equals(recType) && !"MAT".equals(operator)){ //A13
			logger.debug("Guam Skip non-mat :"+cntrNbr+" trans="+recType+"  operator="+operator);
			return null;
		}
		//Set action generic Fields
		populateActionFields(data, textObj);
		//Set action generic Fields
		//1. have a generic Ingate Method 
		//2. based on action populate cretain fields 
		if("CYO".equals(transCode)){
			//Method to set Ingate variables 
		   populateGateTxnData(data, textObj);
		   if(data != null){
		     xmlOutMsg = createGroovyMsg(data, recType);
		   }
    	   return xmlOutMsg;
		   
		}else if("OG".equals(transCode)){
			//Method to set Outgate variables
			populateGateTxnData(data, textObj);
			if(data != null){
 		      xmlOutMsg = createGroovyMsg(data, recType);
			}
	    	return xmlOutMsg;
	    	
		}else {
			logger.debug("message type unknown :"+recType);
			//Email unknown message type
			//populateBdcData(data, textObj);
		}
		  return xmlOutMsg;
	}
	
	public String  createGroovyMsg(Map<String, String> data, String recType){
		String xmlOutMsg = null;
		try{
		//Create Message String
		StringBuffer strBuff = new StringBuffer();
	    strBuff.append("<GroovyMsg");
	    
		Set<String> set = data.keySet();
		for(String aKey : set){
  		String fldValue = aKey+"=\'"+(data.get(aKey) != null && data.get(aKey).trim().length() > 0 ? data.get(aKey).trim() : "null")+"\' ";
	    strBuff.append(" "+fldValue);
		}
		strBuff.append("/>");
		xmlOutMsg = strBuff.toString();
		//logger.debug("createGroovyMsg xmlOutMsg:"+xmlOutMsg);
		xmlOutMsg = addOnFields(xmlOutMsg,recType);
		}catch(Exception e){
			logger.error(e);
		}
		return xmlOutMsg;
	}
	
	/*
	 * Only read these Fields for BDC-FREE messages all other fields set to percentage(%) Kel's Email 10/09
	 * Data Element1
	 * Container 1-12
       Service   13-17
       Consignee 50-84
       Data Element2
       Booking number 181
       Data Element3 
       Storage date 25-32  --Convert Julain date 
       Available date  120-130(MISC3) -- Convert Julian Date  
       Detention date  120-130(MISC3)
	 */
	private void populateGateTxnData( Map<String, String> data, GuamGateTxn guamGateTxn) {
		String cntrNbr = null;
		String action = null;
    	String vesselCode = null;
		String leg = null;
	    String truckCode = null;
    	
		//HARDCODED VALUES
		data.put( "unitClass", "CONTAINER");
		data.put( "facility", "GUM");
	
	  try{
		String txn = guamGateTxn.getTransCode() != null ? guamGateTxn.getTransCode().trim() : "";
		action = guamGateTxn.getAction() != null ? guamGateTxn.getAction().trim() : "";
		cntrNbr = guamGateTxn.getContainerNbr() != null ?  guamGateTxn.getContainerNbr().trim() : "";
		cntrNbr = cntrNbr.replaceAll("MATU", "");
		String checkDigit = guamGateTxn.getCheckdigit() != null ? guamGateTxn.getCheckdigit().trim() : "X";
		
		data.put( "ctrNo", cntrNbr);
		data.put("checkDigit", checkDigit); //HARDCODED VALUE
		
		//PORT and leg mapping
		String podPort = guamGateTxn.getPod()!= null ? guamGateTxn.getPod().trim() : null;
  	    podPort = podPort!= null && podPort.trim().length() > 2  ? podPort.substring(2) : "null";
  	  
  	    String destPort = guamGateTxn.getDest() != null ? guamGateTxn.getDest().trim() : null;
	    destPort = destPort!= null && destPort.trim().length() > 2 ? destPort.substring(2) : "null";
	    
	    //Mask 1)OAK to LAX  2)SEA to HON
	    if("OAK".equals(podPort)){
		  podPort = "LAX";
		 }else if("SEA".equals(podPort)){ //A12
			 podPort = "HON"; 
		 }

	    TosDestPodData podRefData = !("MM".equals(action) || "MI".equals(action)) ? mapPort(destPort, podPort) : null; //A11
	    if(podRefData == null && !("MM".equals(action) || "MI".equals(action))){
	    	// Added on Aug 12, 2013 by Karthik Rajendran
	    	// Adding Container, Dest, POD port information to the email body.
	    	String msg = "Gate Transaction Port Information not Mapped in TDP. <br />";
	    	msg = msg + "  Container = " + cntrNbr + " <br />";
	    	msg = msg + "  Dest port = " + destPort + " <br />";
	    	msg = msg + "  POD port = " + podPort + " <br />";
	    	EmailSender.sendMail(supportMail, gumSupport, "PAOG Gate Txn Leg Incorrect for cntr="+cntrNbr, msg );
	    }
         
  	    //Compute leg value 
  	    MessageConfigCmis instance = MessageConfigCmis.getInstance();
		GroovyMessageProcessor  gvyMessageProc = new GroovyMessageProcessor(null);
		podPort = podRefData != null ? podRefData.getPod1() : null;
		logger.debug("populateGateTxnData:podPort :"+podPort);
		if("GUM".equals(podPort)){ //Change Added to Get Gum Leg Right and Map PNI,MNL Port
			leg = "W";  
			podPort = podRefData != null ? podRefData.getPod2() : podPort ;  
			destPort = "OAK".equals(destPort) || "SEA".equals(destPort)  ?  destPort : (podRefData != null ? podRefData.getPod2() : destPort);
			logger.debug("Inside If podPort "+podPort+" destPort "+destPort+" leg "+leg);
		}else{
		   //Set Destination as Discharge
		  podPort = podRefData != null ? podRefData.getPod1() : podPort ;  
		  destPort =  "OAK".equals(destPort) || "SEA".equals(destPort)  ?  destPort : (podRefData != null ? podRefData.getPod1() : destPort);
		  leg = gvyMessageProc.getVoyageDirectionCode("GUM_"+podPort,instance,null);  
		  logger.debug("Inside else podPort "+podPort+" destPort "+destPort+" leg :"+leg);
		}
		
		//Code to Map Vessel name to Vessel Code
		String tempVes = guamGateTxn.getVessel() != null ? guamGateTxn.getVessel().trim() : "";
    	TosVesselLookup tosVesselLookup = new TosVesselLookup();
  	    if(tempVes != null && tempVes.length() > 1){
  		  vesselCode = tosVesselLookup.getVesselCode("%"+tempVes+"%");
  	    }

		//21. Code to Map Trucker name to Trucker Code a) Map the Whole word  b)Else Map on the First Word 
		//A101 - a) ADD XML escape character
  	    TosTruckerLookup tosTruckerLookup = new TosTruckerLookup();
		String truckCom = guamGateTxn.getTruckingCmpy() != null ? guamGateTxn.getTruckingCmpy().trim() : "";
		if(truckCom.length() > 1){
			truckCode = tosTruckerLookup.getTruckerCode(truckCom);
		}
	    if(truckCode == null){
	    	EmailSender.sendMail(supportMail, gumSupport, "Guam Tnx no Matching Trucker Code for cntr="+cntrNbr, "Guam Tnx no Matching Trucker Code for cntr="+cntrNbr);
	    }
		//B. If didnt find a whole word match then match the first word
		/*String tempArr[] = truckCom.split(" ");
		String tempTrk = tempArr[0] != null ? (tempArr[0].length() > 1 ? tempArr[0] : tempArr[1])  : null ;
		if(truckCode == null && truckCom.contains("PACIFIC") &&  truckCom.contains("ISLAND")){
			tempTrk = "PACIFICISLAND MOVERS";
		}
	    if(truckCode == null && tempTrk != null){
		  truckCode = tosTruckerLookup.getTruckerCode("%"+tempTrk+"%");
	    }if(truckCode == null){  truckCode =  "null"; } */   
	    
	    String bkg = guamGateTxn.getBooking() != null ? getNumeric(guamGateTxn.getBooking().trim()) : "null";
  	    String seal = guamGateTxn.getSeal() != null ? guamGateTxn.getSeal().trim() : "null" ;
  	    String weight = guamGateTxn.getWeight() != null ? guamGateTxn.getWeight().trim() : "%" ;
  	    String hazFlag = "Y".equals(guamGateTxn.getHazFlag() != null ? guamGateTxn.getHazFlag().trim() : "N") ? "Y" : "N" ;
  	    String hazImdg = "null"; String hazUnNum = "null"; String hazDesc = "null"; String hazRegs = "null"; 
  	    if("Y".equals(hazFlag)){
			hazImdg="HAZ";
			hazUnNum="0000";
			hazDesc="UNKNOWN";
			hazRegs="DOT";
		}
  	    logger.debug("populateGateTxnData:leg :"+leg+",vesselCode :"+vesselCode+",truckCode :"+truckCode);
		//22. Put RefData Code here to Get Get Vessel Code 
	    if("MM".equals(action) || "MI".equals(action))
	    {		    
	      data.put( "commodity", "null");
	      data.put( "dir", "MTY");
	      data.put( "orientation", "E");
	      data.put( "dischargePort", "null");
	      data.put( "dPort", "null");
	      data.put( "loadPort", "null");
	      data.put( "locationTier", "null");
	      data.put( "truck", truckCode);
	      data.put( "misc1", "null");
	      data.put( "actualVessel", "null");
	      data.put( "actualVoyage", "null");
	      data.put( "leg", "null");
	      data.put( "ds", "%");
	      data.put( "seal", "null");
	      data.put( "cWeight", weight); 
	      data.put( "hazF", hazFlag);
	      data.put( "hazImdg", hazImdg);
	      data.put( "hazUnNum", hazUnNum);
	      data.put( "hazDesc", hazDesc);
	      data.put( "hazRegs", hazRegs);
	     
	      if("CYO".equals(txn)){
	    	  data.put( "msgType", "POG_IGT_MTY");
		      data.put( "bookingNumber", "null");
		      data.put( "locationStatus", "1");
	      }else if("OG".equals(txn)){
	    	  data.put( "msgType", "POG_OGT_MTY");
			  data.put( "bookingNumber", "MTYOUT");
		      data.put( "locationStatus", "3");
	      }
	      
	    }else if ("FE".equals(action) || "ME".equals(action)){ //A14
	    	  
	    	  String actualVessel = vesselCode;
	    	  String actualVoyage = guamGateTxn.getVoyage() != null ? guamGateTxn.getVoyage().trim() : null;
	    	  if(leg == null || leg == "%" ||actualVessel == null || actualVessel.length() == 0 
	    			  || actualVoyage == null || actualVoyage.length() == 0  || "null".equals(leg)){
	    		  //EmailSender.sendMail(supportMail, gumSupport, cntrNbr+" Guam IGT Error", "Record Contains BAD Vessel & Leg Information" );
	    		  actualVessel = "null";  actualVessel = "null";  leg = "null";
	    	  }
	    	  
		      data.put( "msgType", "POG_IGT_FCL");
			  data.put( "bookingNumber", bkg);
			  data.put( "commodity", "null");
			  data.put( "dir", "IN");
			  data.put( "orientation", "F");
			  data.put( "ds", "CY");
			  data.put( "dischargePort", podPort);
			  data.put( "dPort", destPort);
			  data.put( "loadPort", "GUM");
			  data.put( "locationTier", "null");
			  data.put( "locationStatus", "1");
			  data.put( "truck", truckCode);
			  data.put( "misc1", "null");
			  data.put( "actualVessel", actualVessel); //A101 - Put Method here to get CODE
			  data.put( "actualVoyage", actualVoyage);
			  data.put( "leg", leg);  
			  data.put( "seal", seal); //A5
			  data.put( "cWeight", weight);
			  data.put( "hazF", hazFlag);
		      data.put( "hazImdg", hazImdg);
		      data.put( "hazUnNum", hazUnNum);
		      data.put( "hazDesc", hazDesc);
		      data.put( "hazRegs", hazRegs);
	    	
	    }else if ("FI".equals(action) || "TI".equals(action) || "TM".equals(action)){
	    	
	      data.put( "msgType", "POG_OUTGATE_FCL");
		  data.put( "bookingNumber", bkg);
		  data.put( "commodity", "%");
		  data.put( "dir", "%");
		  data.put( "orientation", "F");
		  data.put( "dischargePort", podPort);
		  data.put( "dPort", destPort);
		  data.put( "loadPort", "%");
		  data.put( "locationTier", "null");
		  data.put( "locationStatus", "3");
		  data.put( "truck", truckCode);
		  data.put( "misc1", "%");
		  data.put( "actualVessel", "%"); //Put Method here to get CODE
		  data.put( "actualVoyage", "%");
		  data.put( "leg", "%");
		  data.put( "ds", "%");
		  data.put( "seal", "%"); //A5
		  data.put( "cWeight", weight);
		  data.put( "hazF", hazFlag);
	      data.put( "hazImdg", hazImdg);
	      data.put( "hazUnNum", hazUnNum);
	      data.put( "hazDesc", hazDesc);
	      data.put( "hazRegs", hazRegs);
	    	
	    }else{
	    	EmailSender.sendMail(supportMail, gumSupport, "PAOG Gate Txn Type unknown"+action+" For cntr="+cntrNbr, "Gate Transaction not processed" );
	    	data = null;
	    }
	  }catch(Exception e){
		  EmailSender.sendMail(supportMail, gumSupport, cntrNbr+" Guam Transaction Error On Action="+action, "Record Contains BAD Information" );
		  e.printStackTrace();		  
	  }
	  logger.debug("dataMapInTest :"+data);
	}
	
	public void populateActionFields(Map<String, String> data, GuamGateTxn guamGateTxn)throws TosException{
		
		String txn = guamGateTxn.getTransCode() != null ? guamGateTxn.getTransCode().trim() : "";
		String tempRemarks = guamGateTxn.getRemarks();
		tempRemarks = tempRemarks != null ? getNumeric(tempRemarks.trim()) : "";
		String chassis = tempRemarks.length() == 6 ? "MATZ"+tempRemarks : "null";
		//A14 -- Only Ingates
		if("CYO".equals(txn) && ("null".equals(chassis) || tempRemarks.length() < 6)){
			chassis = "%";
		}
		String msgAction = guamGateTxn.getAction() != null ? guamGateTxn.getAction().trim() : "";
		
		String date = null;
		String action = null;
		
		if("CYO".equals(txn)){
		   date = guamGateTxn.getArrivalDate();
		   action = "IGT";
		}else if("OG".equals(txn)){
		    if("MI".equals(msgAction)){
		    	 action = "OGS";
		    }else if("FI".equals(msgAction) || "TI".equals(msgAction) || "TM".equals(msgAction)){ //A12
		    	action = "OGC";
		    }else if ("MM".equals(msgAction)){
		    	action = "OGP";
		    }else{
		    	action = "OGS";
		    }
		   date = guamGateTxn.getDeptDate();
		}else{
		    throw new TosException("No Tran Code");
		}
		data.put( "action", action);
		try{	
   		  String tempDate = date.trim().substring(0, 10);
		  DateFormat fromFormatter = new SimpleDateFormat("yyyy-MM-dd");   
		  DateFormat dateFormatter = new SimpleDateFormat(dateFormat);
		  Date dateTmp = fromFormatter.parse(tempDate);
		  String convtDateStr = dateFormatter.format(dateTmp);

		  data.put( "aDate", convtDateStr);  
		  data.put( "aTime", date.trim().substring(11, 19));
		  data.put( "lastADate", convtDateStr);
		  data.put( "lastATime", date.trim().substring(11, 19));
		}catch(Exception e){
			e.printStackTrace();
			String tempDt = CalendarUtil.dateFormat(dateFormat); 
			String tempTime = CalendarUtil.dateFormat(timeFormat); //A110
			data.put( "aDate", tempDt );  
			data.put( "aTime", tempTime);
		    data.put( "lastADate", tempDt);
			data.put( "lastATime", tempTime);
		}
		
		data.put( "doer", "GumTxn");
		data.put( "lastAction", action);

		data.put( "lastDoer", "GumTxn");
		data.put( "chassisNumber",chassis);
		if("%".equals(chassis)){ //A14
			data.put( "chassisCd","%");
		}else if(chassis.startsWith("MATZ")){
			data.put( "chassisCd","X");
		}else{
			data.put( "chassisCd","null");
		}
        
	}

	public String addOnFields(String xml, String recType){
		String xmlmsg = xml.substring(0,xml.length()-2);
		
		String addOnFields = "category='null' accessory='null' mgWeight='%' tareWeight='%' typeCode='%' hgt='%' "+
		"strength='%' owner='MATU' damageCode='null' srv='MAT' temp='null' tempMeasurementUnit='null' equipTypeCode='%' "+
		"hazOpenCloseFlag='%'  locationRow='%' stowRestCode='null' stowFlag='null' odf='null' "+
		"consignee='null' shipper='null' cneeCode='null' "+
		"locationCategory='null' arrDate='null' consigneePo='null' restow='NONE' shipperId='null' "+
		"dsc='null' planDisp='null' shipperPool='null' retPort='null' overLongBack='%' overLongFront='%' overWideLeft='%' "+
		"overWideRight='%' overHeight='%'  loc='null' cell='null' locationStallConfig='null'  vesvoy='null' hsf7='null' pmd='null' "+
		"locationRun='%' misc2='null' misc3='null' sectionCode='Z' aei='null' dss='%' erf='null' availDt='null' dtnDueDt='null' "+
		"lastFreeStgDt='null' oldVesvoy='%' lineTime='%' tractorNbr='%' vNumber='null' chassAei='%' mgAei='%' chasdamageCode='%' "+
		"comments='null' crStatus='null' cargoNotes='null'  safetyExpiry='%' lastInspection='%' lastAnnual='%' chassisAlert='%' mgp='null' "+
		"chassisHold='MAT' chassisNotes='null' chassTareWeight='%'  gateTruckCmpyCd='null' gateTruckCmpyName='null' gateTruckId='null' "+
		"batNumber='null' turnTime='null' gateSeqNo='null' laneGateId='null'";
		xmlmsg = xmlmsg+" "+addOnFields+" />";
		return xmlmsg;
	}
	
	
	public String getNumeric(String str){
		try{
			StringBuffer buff = new StringBuffer();
			char[] remarkTemp = str.toCharArray();
			for(int i=0; remarkTemp.length > i; i++){
				String temp = ""+remarkTemp[i]; 
			 if(temp.matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+")){
				 buff.append(temp);
  			  }
			}
			return buff.toString();
		}catch(Exception e){
			e.printStackTrace();
		}
		return "null";
	}
	
	public TosDestPodData mapPort(String destPort, String podPort)throws Exception {
	  //A11 - Lookup POD for REFDATA MAPPING 
  	  TosDestPodData refData = (TosDestPodData)RefDataLookup.queryById("com.matson.cas.refdata.mapping.TosDestPodData", podPort);
			if ( refData == null) {
				refData = (TosDestPodData)RefDataLookup.queryById("com.matson.cas.refdata.mapping.TosDestPodData", destPort);
			}
	  return refData;
	}
	
      
	//171843.TXT File ask KEl
	public static void readDataElement(GuamGateTxn guamGateTxn){
		/*String dataElement = null;
  	    dataElement = "201105050001,IN/MM,MATU4536574,9500,20110505,080253,,,,KWIK SPACE                    ,,MAT ,CHZ 945428";
		dataElement = "700743     YGUM    MS/SP         HONGUMSPN FMAT"; //VPU
		dataElement = "MHQU005970 YGUM    GUAM          HONGUMGUM FAPL"; //VPU
		dataElement = "296229     YGUM    R/H           LAXNGBNGB FMAT"; //VPU
		GuamGateTxn guamGateTxnObj = new GuamGateTxn();
		guamGateTxnObj.setCtrNo(dataElement.substring(0, 11));
		guamGateTxnObj.setPos(dataElement1.substring(11, 12));
		guamGateTxnObj.setLocation(dataElement1.substring(12, 19));
		guamGateTxnObj.setBlock(dataElement1.substring(19, 24));
		guamGateTxnObj.setRow(dataElement1.substring(24, 27));
		guamGateTxnObj.setColumn(dataElement1.substring(27, 31));
		guamGateTxnObj.setTier(dataElement1.substring(31, 33));
		guamGateTxnObj.setLoadPort(dataElement1.substring(33, 36));
		guamGateTxnObj.setDischPort(dataElement1.substring(36, 39));
		guamGateTxnObj.setDestport(dataElement1.substring(39, 43));
		guamGateTxnObj.setFreightKind(dataElement1.substring(43, 44));
		guamGateTxnObj.setOwner(dataElement1.substring(44, 47)); */
		
/*		System.out.println("guamGateTxnObj.getCtrNo="+guamGateTxnObj.getCtrNo());                  
		System.out.println("guamGateTxnObj.getPos="+guamGateTxnObj.getPos());                      
		System.out.println("guamGateTxnObj.getLocationStatus="+guamGateTxnObj.getLocationStatus());
		System.out.println("guamGateTxnObj.getBlock="+guamGateTxnObj.getVesvoy());                
		System.out.println("guamGateTxnObj.getRow="+guamGateTxnObj.getRow());                      
		System.out.println("guamGateTxnObj.getColumn="+guamGateTxnObj.getColumn());                
		System.out.println("guamGateTxnObj.getTier="+guamGateTxnObj.getTier());                    
		System.out.println("guamGateTxnObj.getLoadPort="+guamGateTxnObj.getLoadPort());            
		System.out.println("guamGateTxnObj.getDischPort="+guamGateTxnObj.getDischPort());          
		System.out.println("guamGateTxnObj.getDestport="+guamGateTxnObj.getDestport());           
		System.out.println("guamGateTxnObj.getFreightKind="+guamGateTxnObj.getFreightKind());        
		System.out.println("guamGateTxnObj.getOwner="+guamGateTxnObj.getOwner());
*/              
	}
   
	protected Object textObjToXmlObj(Object textObj) throws TosException {
		return null;
	}
	
	protected Object xmlObjToTextObj(Object xmlObj) throws TosException {
		// TODO Auto-generated method stub
		return null;
	}

	
	public static void main(String args[]){
		try{
            String temp ="Test this out hazF='Y'  action='IGT'";
            if(temp.contains("hazF='Y'") && temp.contains("action='IGT'")){
            	System.out.println("-----Found match----");
            	temp = temp.replace("action='IGT'", "action='HZU'");
            	System.out.println("temp -----"+temp);
            }else{
            	System.out.println("-----Didnt Find match----");
            }
            
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
		

	}
	
    
}
