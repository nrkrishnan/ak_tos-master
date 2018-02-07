/*
 *  Srno    Date       Author  Change Description 
 *  A1      06/10/08   GR      Update Logic as per Harrys write up   
 *  VPS/VPU - Pending Work 
 *  a) Leg Computation
 *  b) priCarrier 
 *  A2     04/06/11    GR      Adding Escape character on consignee value       
 *  A3     06/16/11    GR      Post NIS BDC to N4(commented code)    
 *  A4     08/26/11    GR      Method Created to groovy xml  
 */

/**
 * If  the record type is VPS (Vessel Planning Position)
	Program reads from the outque according to the VPS Position View and writes fields according to the ECOS View with changes as follows:

	Update Ehome port with cfg file portName
	Ectr = outque VPSctr
	Edir = blank
	Esrv = “MAT”
	ElocStat= blank
	EemptyFull = outque VPSfullEmpy
	EcheckDigit = blank
	EtypeCode = blank
	Evessell = 1st 3 bytes of outque VPSlocation
	Evoyage = bytes 4 through 6 of outque VPSlocation
	
	IF VPSloadPort from the outque is found in the ‘from’ column of the PortLeg table 
	THEN Eleg = 1st byte of discPort from Portleg table.  
	IF that value is “X”
	THEN Eleg = “W”

	EbookingNumber = blank
	EdischargePort = VPSdischarge port from the outque.
	
	IF container is empty or has a VPSdestPort in the outque 
	THEN Edport = VPS destPort from outque
	ELSE Edport = VPSdiscPort from the outque.

	Etruck = blank       
	EdamageCode   = blank 
	EchassisNumber  = blank 
	EchasCheckDigit  = blank
	Eseal  = blank 
	Ecell   =   concatenation of outque values of VPS row| VPS column| VPS tier
    	EcWeight  = blank 
	Etemp  = blank 
    EtempMU  = blank 
    Eshipper  = blank 
    EretPort  = blank 
    Eodf  = blank 
    Ehazf  = blank 
    Eowner  = blank 
    EchasDamage  = blank 
    EyardLoc  = blank 
    EloadPort    = VPSloadPort from the outque 
    EMGnumber  = blank 
    EMGcheckDigit  = blank 
    Ecommodity  = blank 
    Ecomments  = blank 
    Ehgt   = blank 
    Estrength  = blank 
    EtareWeight  = blank 
    Eaei  = blank 
    Eds  = blank 
    Econsignee  = blank 
    EcargoNotes  = blank 
    EpriCar = VPSpriCar from the outque
    Etrade  = blank
    EchasType  = blank 
    EplanDisp  = blank



  If record type is VPU (Position View)
  Program reads from the outque according to the VPS Position View and writes fields according to the ECOS View with changes as follows:
			 
  EhomePort = portName from the cfg file
  Ectrno = VPSctrno from the outque
  Edir	 = blank
  Esrv = “MAT”           
  ElocStatus 	 = blank 
  EemptyFull = VPSfullEmpty from the outque     
  EcheckDigit	 = blank 
  EtypeCode	 = blank 
  Evessel 	 = blank 
  Evoyage	 = blank 
  Eleg  = calculated as with VPS message above         
  EbookingNumber	 = blank 
  EdischargePort  = VPSdiscPort from the outque
  Edport  = VPSdestPort from the outque        
  Etruck	 = blank 
  EdamageCode	 = blank 
  EchassisNumber	 = blank 
  EchasCheckDigit	 = blank
  Eseal 	 = blank 
  Ecell	 = blank 
  EcWeight	 = blank 
  Etemp	 = blank 
  EtempMU	 = blank 
  Eshipper	 = blank 
  EretPort 	 = blank 
  Eodf	 = blank 
  Ehazf 	 = blank 
  Eowner	 = blank 
  EchasDamage 	 = blank 
  EyardLoc  = concatenation of outque values of VPSblock|VPSrow|VPScolumn| VPS tier
  EloadPort = VPSloadPort from the outque 
  EMGnumber = blank 
  EMGcheckDigit = blank 
  Ecommodity = blank 
  Ecomments = blank 
  Ehgt = blank 
  Estrength = blank 
  EtareWeight = blank 
  Eaei  = blank 
  Eds = blank 
  Econsignee = blank 
  EcargoNotes = blank 
  ChasType = blank
  EpriCar = VPSpriCar from the outque
  Etrade  = blank
  EchasType  = blank  
  EplanDisp  = blank
  
 */
package com.matson.tos.messageHandler;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.matson.tos.exception.TosException;
//import com.matson.tos.jatb.Gvpq;
import com.matson.tos.jatb.CmisToGems;
import com.matson.tos.util.CalendarUtil;
import com.matson.tos.util.EmailSender;
import com.matson.tos.util.GroovyXmlUtil;
import com.matson.tos.util.JMSSender;
import com.matson.tos.util.StrUtil;
import com.matson.tos.util.CheckDigit;
import com.matson.tos.util.TosRefDataUtil;

import java.util.Set;

public class CmisToGemsMessageHandler extends AbstractMessageHandler{
	
	private static Logger logger = Logger.getLogger(CmisToGemsMessageHandler.class);
	private static String dateFormat = "MM/dd/yyyy";
	private CheckDigit check = new CheckDigit();
	public static final String CLASS_LOC = "database";
	public static final String CLASS_NAME = "GvyInjBdcNis";
	private JMSSender sender;
	private String port = "HON";
	private String supportMail = TosRefDataUtil.getValue("SUPPORT_EMAIL");
	
	public CmisToGemsMessageHandler(String xmlObjPackageName,
			String textObjPackageName, String fmtFile, int convertDir)
			throws TosException {
		super(xmlObjPackageName, textObjPackageName, fmtFile, convertDir);
		
		sender = new JMSSender(JMSSender.BATCH_QUEUE, port);
		// TODO Auto-generated constructor stub
	}
	
	public String createXmlStr() throws TosException {
		Map<String, String> data = new LinkedHashMap<String, String>();
		CmisToGems textObj = (CmisToGems)createTextObj();
		String recType = textObj.getRectype() != null ? textObj.getRectype().substring(1) : null ;
		//Set action generic Fields
		populateActionFields(data, textObj);
		//Set action generic Fields
		if("VPU".equals(recType) || "VPS".equals(recType)){		
		   populateGvpqData(data, textObj);
		}else if("NIT".equals(recType)){
		   populateNitData(data, textObj);	
		}else if ("BDC".equals(recType)){
			populateBdcData(data, textObj);
			//Post Detention dates to N4
			 //A2 - populateBdcforN4(data);	
		}
		
		//A501
		String xmlOutMsg = createGvyMsg(data);
		if("BDC".equals(recType)){
			xmlOutMsg = addOnFields(xmlOutMsg,recType);
		}
		
		return xmlOutMsg;
	}
	
	public String createGvyMsg(Map<String, String> data){
		//Create Message String
		StringBuffer strBuff = new StringBuffer();
	    strBuff.append("<GroovyMsg");
	    
		Set<String> set = data.keySet();
		for(String aKey : set){
  		String fldValue = aKey+"=\'"+(data.get(aKey) != null && data.get(aKey).trim().length() > 0 ? data.get(aKey).trim() : "null")+"\' ";
  		//System.out.println("aKey="+aKey+" Value="+data.get(aKey));
	    strBuff.append(" "+fldValue);
		}
		strBuff.append("/>");
		
		String xmlOutMsg = strBuff.toString();
		return xmlOutMsg;
	}
	
	//Pass NIS Detention to N4 and Trigger an Event
	private void populateBdcforN4( Map<String, String> data){
		String cntrNo = null;
		try{
		   HashMap gvyInjMap = new HashMap();
		   cntrNo = check.getCheckDigitUnit(data.get("ctrNo")+"X");
		   logger.debug("populateBdcforN4 : cntrNo="+cntrNo);
		    gvyInjMap.put("ctrNo",cntrNo);
		    gvyInjMap.put("availDt",data.get("availDt"));
		    gvyInjMap.put("dtnDueDt",data.get("dtnDueDt"));
		    gvyInjMap.put("lastFreeStgDt",data.get("lastFreeStgDt"));
		
		 String gvyInjMsg = GroovyXmlUtil.getInjectionXmlStr( CLASS_NAME, CLASS_LOC, gvyInjMap);
		 logger.debug("populateBdcforN4="+gvyInjMsg);
		 sender.send(gvyInjMsg);
		}catch(Exception e){
			e.printStackTrace();
			EmailSender.sendMail(supportMail, supportMail, "Error populating Bdc for N4", e.getMessage());
		}
	}
	
	/*
	 * Msg Layout = action,aDate,Time,doer,ctrNo,locationStatus,vesvoy,(loc/cell),loadPort,dPort,dischargePort,orientation,owner 
	*/
	private void populateGvpqData( Map<String, String> data, CmisToGems cmisToGems) {

		String action = cmisToGems.getRectype();
		action = action.substring(1);
		
		String dataElement1 = cmisToGems.getDataElement1();
		data.put( "facility", "GUM");
		data.put( "ctrNo", dataElement1.substring(0, 11));
		
		//Add Leg value - Compute Leg Value
		data.put( "srv", "MAT");
		
		String position = dataElement1.substring(11, 12);
		if("Y".equals(position)){
			data.put( "locationStatus", "1");
		}else if("V".equals(position)){
			data.put( "locationStatus", "2");	
		}
		
		//Value is GUM
		String locationStatus = dataElement1.substring(12, 19);
		String block = dataElement1.substring(19, 24);
		
		String location = dataElement1.substring(24, 27)+dataElement1.substring(27, 31)+dataElement1.substring(31, 33);
		location = location != null ? location.replace(" ","") : location;
		
		
		
		//If Vessel Then Cell If yard then Loc
		if("VPU".equals(action)){
		  data.put( "loc", block); 
		  data.put( "vesvoy", "%"); //Would be Null
		}else if ("VPS".equals(action)){
		  data.put( "cell", location);
		  data.put( "vesvoy", locationStatus);
		}
		
		data.put( "loadPort", dataElement1.substring(33, 36));
		data.put( "dischargePort" , dataElement1.substring(36, 39));
		data.put( "dPort", dataElement1.substring(39, 43));
		
		data.put( "orientation" , dataElement1.substring(43, 44));
		data.put( "owner", dataElement1.substring(44, 47));
	}
	
	
	/*
	 * Msg Layout = action,aDate,Time,doer,ctrNo,locationStatus,vesvoy,(loc/cell),loadPort,dPort,dischargePort,orientation,owner 
	*/
	private void populateNitData( Map<String, String> data, CmisToGems cmisToGems) {
		
		String dataElement1 = cmisToGems.getDataElement1();
		data.put( "facility", "HON");
		data.put( "ctrNo", dataElement1.substring(0, 12));
		
		String vesvoy = dataElement1.substring(12, 18);
		vesvoy = vesvoy.trim().length() > 0 ? vesvoy : "%"; 
		
  	    data.put( "vesvoy", vesvoy);
		data.put( "truck", dataElement1.substring(18, 22));

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
	private void populateBdcData( Map<String, String> data, CmisToGems cmisToGems) {

		String dataElement1 = cmisToGems.getDataElement1();
		//HARDCODED VALUES
		data.put( "msgType", "CMIS_BDC");
		data.put( "unitClass", "CONTAINER");
		data.put( "facility", "HON");
				
		data.put( "ctrNo", dataElement1.substring(0, 12));
		data.put("checkDigit", "X"); //HARDCODED VALUE
		
		data.put( "srv", dataElement1.substring(12, 17));
		data.put( "consignee", org.apache.commons.lang.StringEscapeUtils.escapeXml(dataElement1.substring(49, 84)) ); //A7
		
		String dataElement2 = cmisToGems.getDataElement2();
		data.put( "bookingNumber", dataElement2.substring(180).trim());
		
		String dataElement3 = cmisToGems.getDataElement3();
		String lastFreeDay = dataElement3.substring(24,31);
		lastFreeDay = lastFreeDay != null && lastFreeDay.trim().length() > 0 ? 
				     CalendarUtil.ConvertJulianToDate(lastFreeDay,dateFormat) : "null"; 
		data.put( "lastFreeStgDt",lastFreeDay );		      

		String misc3 = dataElement3.substring(119,129);
		String availDate = misc3.trim().length() > 5 ? misc3.substring(0,5) : null;
		String availDt = availDate != null ? CalendarUtil.ConvertJulianToDate(availDate,dateFormat) : "null";
		String dueDate = misc3.trim().length() > 9 ? misc3.substring(5,10) : null;
		String dueDt = dueDate != null ? CalendarUtil.ConvertJulianToDate(dueDate,dateFormat) : "null";
		data.put( "availDt",availDt);
		data.put( "dtnDueDt",dueDt);
		data.put( "lastAction", "FREE");
		
	}
	
	public void populateActionFields(Map<String, String> data, CmisToGems cmisToGems){
		String action = cmisToGems.getRectype();
		action = action.substring(1);
		String date = padZero(cmisToGems.getDate());
		
		data.put( "action", action);
		data.put( "aDate", StrUtil.trimQuotes(date));
		data.put( "aTime", StrUtil.trimQuotes(cmisToGems.getTime()));
		data.put( "doer", StrUtil.trimQuotes(cmisToGems.getUser()));
		data.put( "lastAction", action);
		data.put( "lastADate", StrUtil.trimQuotes(date));
		data.put( "lastATime", StrUtil.trimQuotes(cmisToGems.getTime()));
		data.put( "lastDoer", StrUtil.trimQuotes(cmisToGems.getUser()));
	}
	
	
	public String addOnFields(String xml, String recType){
		String xmlmsg = xml.substring(0,xml.length()-2);
		
		String addOnFields = "chassisNumber='%' chassisCd='%' category='%' accessory='%' mgWeight='%' tareWeight='%' typeCode='%' hgt='%' " +
		"strength='%' owner='%' damageCode='%' temp='%' tempMeasurementUnit='%' equipTypeCode='%' hazOpenCloseFlag='%'  locationRow='%' " +
		"cWeight='%' seal='%' stowRestCode='%' stowFlag='%' odf='%'  shipper='%' cneeCode='%' hazF='%' hazImdg='%' hazUnNum='%' " +
		"locationCategory='%' arrDate='%' consigneePo='%' restow='%' shipperId='%' hazDesc='%' hazRegs='%'  commodity='%' dir='%' dsc='%' " +
		"planDisp='%' ds='%' orientation='%'  shipperPool='%' dischargePort='%' dPort='%' loadPort='%' retPort='%' overLongBack='%' overLongFront='%' " +
		"overWideLeft='%' overWideRight='%' overHeight='%'  loc='%' cell='%' locationTier='%' locationStatus='%' locationStallConfig='%'  vesvoy='%' " +
		"truck='%' misc1='%' actualVessel='%' actualVoyage='%' leg='%'  hsf7='%' pmd='%' locationRun='%' misc2='%' misc3='%' sectionCode='%' aei='%' " +
		"dss='%' erf='%' oldVesvoy='%' lineTime='%' tractorNbr='%' vNumber='%' chassAei='%' mgAei='%' chasdamageCode='%'  comments='%' crStatus='%' " +
		"cargoNotes='%'  safetyExpiry='%' lastInspection='%' lastAnnual='%' chassisAlert='%' mgp='%' chassisHold='%' chassisNotes='%' " +
		"chassTareWeight='%'  gateTruckCmpyCd='%' gateTruckCmpyName='%' gateTruckId='%' batNumber='%' turnTime='%' gateSeqNo='%' laneGateId='%'";
		xmlmsg = xmlmsg+" "+addOnFields+" />";
		return xmlmsg;
	}
	
    private String padZero(String date){
		
		if(1 == date.indexOf("/")){
			date = "0"+date; 
		}
		if(1 == date.substring(3).indexOf("/")){
			date = date.substring(0,3)+"0"+date.substring(3);
		}
		return date; 
	}
    
	//171843.TXT File ask KEl
	public static void readDataElement(CmisToGems cmisToGemsObj){
		/* String dataElement = null;
  	    dataElement = "GESU937635 VILN096 =    08 02  86MAJGUMGUM FMEL"; //VPS
		dataElement = "700743     YGUM    MS/SP         HONGUMSPN FMAT"; //VPU
		dataElement = "MHQU005970 YGUM    GUAM          HONGUMGUM FAPL"; //VPU
		dataElement = "296229     YGUM    R/H           LAXNGBNGB FMAT"; //VPU
		Gvpq cmisToGemsObj = new Gvpq();*/
	    /*String dataElement1 = cmisToGemsObj.getDataElement1();
		cmisToGemsObj.setCtrNo(dataElement.substring(0, 11));
		cmisToGemsObj.setPos(dataElement1.substring(11, 12));
		cmisToGemsObj.setLocation(dataElement1.substring(12, 19));
		cmisToGemsObj.setBlock(dataElement1.substring(19, 24));
		cmisToGemsObj.setRow(dataElement1.substring(24, 27));
		cmisToGemsObj.setColumn(dataElement1.substring(27, 31));
		cmisToGemsObj.setTier(dataElement1.substring(31, 33));
		cmisToGemsObj.setLoadPort(dataElement1.substring(33, 36));
		cmisToGemsObj.setDischPort(dataElement1.substring(36, 39));
		cmisToGemsObj.setDestport(dataElement1.substring(39, 43));
		cmisToGemsObj.setFreightKind(dataElement1.substring(43, 44));
		cmisToGemsObj.setOwner(dataElement1.substring(44, 47)); */
		
/*		System.out.println("cmisToGemsObj.getCtrNo="+cmisToGemsObj.getCtrNo());                  
		System.out.println("cmisToGemsObj.getPos="+cmisToGemsObj.getPos());                      
		System.out.println("cmisToGemsObj.getLocationStatus="+cmisToGemsObj.getLocationStatus());
		System.out.println("cmisToGemsObj.getBlock="+cmisToGemsObj.getVesvoy());                
		System.out.println("cmisToGemsObj.getRow="+cmisToGemsObj.getRow());                      
		System.out.println("cmisToGemsObj.getColumn="+cmisToGemsObj.getColumn());                
		System.out.println("cmisToGemsObj.getTier="+cmisToGemsObj.getTier());                    
		System.out.println("cmisToGemsObj.getLoadPort="+cmisToGemsObj.getLoadPort());            
		System.out.println("cmisToGemsObj.getDischPort="+cmisToGemsObj.getDischPort());          
		System.out.println("cmisToGemsObj.getDestport="+cmisToGemsObj.getDestport());           
		System.out.println("cmisToGemsObj.getFreightKind="+cmisToGemsObj.getFreightKind());        
		System.out.println("cmisToGemsObj.getOwner="+cmisToGemsObj.getOwner());
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
	/*	String dataElement1 = "251153      MAT                                  ALOHA FREIGHT FORWARDERS INC                                                                                                                         ";
		String dataElement2 = "                                                                                                                              BDC  10/19/1011:16:11KKM  BDC  10/19/1011:16:11KKM    6936594          ";
		String dataElement3 = "                        234567890                                                                                      0923809253BDC                              ";
		//HARDCODED VALUES
		System.out.println( "ctrNo="+dataElement1.substring(0, 12));
		
		System.out.println( "srv="+dataElement1.substring(12, 17));
		System.out.println( "consignee="+dataElement1.substring(49, 84));
		
		
		System.out.println("bookingNumber="+dataElement2.substring(180).trim());
		
		String lastFreeDay = dataElement3.substring(25,32);
		System.out.println("lastFreeDay-1="+lastFreeDay);
		lastFreeDay = lastFreeDay != null && lastFreeDay.trim().length() > 0 ? 
				     CalendarUtil.ConvertJulianToDate(lastFreeDay,dateFormat) : "null";
		System.out.println("lastFreeDay-2="+lastFreeDay); 
				      

		String misc3 = dataElement3.substring(119,129);
		System.out.println("misc3="+misc3);
		String availDate = misc3.trim().length() > 5 ? misc3.substring(0,5) : null;
		String availDt = availDate != null ? CalendarUtil.ConvertJulianToDate(availDate,dateFormat) : "null";
		String dueDate = misc3.trim().length() > 9 ? misc3.substring(5,10) : null;
		String dueDt = dueDate != null ? CalendarUtil.ConvertJulianToDate(dueDate,dateFormat) : "null";
		System.out.println("availDt="+availDt);
		System.out.println("dtnDueDt="+dueDt);
	*/
		String xml = "<Groovy msgtype='TEST' unitClass='Container' />";
		//System.out.println("PRINTLN = "+xml.substring(0,xml.length()-2));
		try{
		String str_date = "10/11/2011";
		java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("MM/dd/yyyy");
        java.util.Date date = (java.util.Date)formatter.parse(str_date);
        System.out.println("DATE===="+date);
		}catch(Exception e){
			e.printStackTrace();
		}
		//month = date.substring(0,2);
		

	}

}
