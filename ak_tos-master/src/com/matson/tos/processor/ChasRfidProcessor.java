package com.matson.tos.processor;
/*
 *   A1  12/06/2011, Create Gvy xml for Kulana Processing.
 */
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.matson.tos.exception.TosException;
import com.matson.tos.util.GroovyXmlUtil;
import com.matson.tos.util.JMSSender;


public class ChasRfidProcessor {
	private static Logger logger = Logger.getLogger(ChasRfidProcessor.class);
	private static JMSSender sender = null; 
	
	public static final String CLASS_LOC = "database";
	public static final String CLASS_NAME = "NowToN4ChasMessageProcessor";
	public static final String REFRESH_CLASS_NAME = "NowYardRowPositionRefresh";
	public static final String KULANA_CLASS_NAME = "KulanaProcessor";
	public static DocumentBuilder db = null;
	
	public ChasRfidProcessor(){
	}
	
	/*
	 * Initialize Class variable 
	 */
	public void init(){
	 try{
	   if(db == null){
	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    db = dbf.newDocumentBuilder();
	   }
	   if(sender == null){
		sender = new JMSSender( JMSSender.REAL_TIME_QUEUE, "HON");
	   }
	}catch(Exception e){
	 e.printStackTrace();
	 logger.error("ChasRifd Init"+e);
    }
   }
	
	public void process(String xml){
		try{
			logger.debug("ChasRfid Now Msg="+xml);			
			init();
			
			String gvyInjStr = createGvyInjXml(xml);
			
			sender.send(gvyInjStr);
			logger.debug("ChasRfid GvyInj Msg="+gvyInjStr);
			
		}catch(Exception e){
			e.printStackTrace();
			logger.error("Chas Rfid Process Method"+e);
		}

	}
	
	public void rowRefreshProcess(String row){
		try{
			init();
			
			Map<String, String> data = new HashMap<String, String>();
			data.put( "position", row);
			data.put( "recorder", "now");
			
			String gvyInjStr = GroovyXmlUtil.getInjectionXmlStr( REFRESH_CLASS_NAME, CLASS_LOC, data);
			
			sender.send(gvyInjStr);
			logger.debug("Row Refresh GvyInj Msg="+gvyInjStr);
			
		}catch(Exception e){
			e.printStackTrace();
			logger.error("Row Refresh Process Method"+e);
		}
	}
	
	
	public void kulanaProcess(String eqId, String notes){
		try{
			init();
			
			Map<String, String> data = new HashMap<String, String>();
			data.put( "unitNbr", eqId);
			data.put( "notes", notes);
			
			String gvyInjStr = GroovyXmlUtil.getInjectionXmlStr( KULANA_CLASS_NAME, CLASS_LOC, data);
			
			sender.send(gvyInjStr);
			logger.debug("Kulana GvyInj Msg="+gvyInjStr);
			
		}catch(Exception e){
			e.printStackTrace();
			logger.error("Row Refresh Process Method"+e);
		}
	}
   

	
	protected String createGvyInjXml(String xml) throws TosException {
		Map<String, String> data = new HashMap<String, String>();
		snxConversion(data,xml);
		//Going Ahead Add Refresh Conversion Here
		
		System.out.println("data="+data);
		return GroovyXmlUtil.getInjectionXmlStr( CLASS_NAME, CLASS_LOC, data);
	}
	
	
	/*
	 * Method Reads an xml String and maps value to Java Object  
	 */
	private void snxConversion(Map<String, String> data, String xml) {
	 try{
		InputStream input = new ByteArrayInputStream(xml.getBytes("UTF-8")); 
		Document doc = db.parse(input);
		NamedNodeMap attrNodes = null;
		Node aNode = null;
		NodeList nodes = null; NodeList equiNodes = null; NodeList posNodes = null; 
		
		//Groovy Inj Attributes
		 String equipPrimId = null;
		 String equipSecId = null;
		 String position = null;
		 String note = null;
		 String location = null;
		
		//Stores table name and MessageType in object key value pair 
		nodes = doc.getElementsByTagName("unit");
		attrNodes = nodes.item(0).getAttributes();
		note = attrNodes.getNamedItem( "snx-update-note").getNodeValue();
		
		equiNodes = doc.getElementsByTagName("equipment");
		if(equiNodes.getLength() == 1){
		  attrNodes = equiNodes.item(0).getAttributes();
		  equipPrimId = attrNodes.getNamedItem("eqid").getNodeValue();	
		}else if(equiNodes.getLength() == 2){
			attrNodes = equiNodes.item(0).getAttributes();
		    equipPrimId = attrNodes.getNamedItem("eqid").getNodeValue();
		    
		    attrNodes = equiNodes.item(1).getAttributes();
		    equipSecId = attrNodes.getNamedItem("eqid").getNodeValue();
		}
		
		posNodes = doc.getElementsByTagName("position");
		if(posNodes.getLength() == 1){
			attrNodes = posNodes.item(0).getAttributes();
			position = attrNodes.getNamedItem("slot").getNodeValue();
			location = attrNodes.getNamedItem("location").getNodeValue();
		}
		
		data.put( "equipPrimary", equipPrimId);
		if(equipSecId != null){
		  data.put( "equipSecondary", equipSecId);
		}
		data.put( "position", position);
		data.put( "note", note);
		data.put( "location", location);
		data.put( "recorder", "now");
		
		
		//System.out.println("equipPrimId="+equipPrimId+"  equipSecId="+equipSecId+"  position="+position+"  note="+note+"  location="+location);
        //logger.debug("equipPrimId="+equipPrimId+"  equipSecId="+equipSecId+"  position="+position+"  note="+note+"  location="+location);

		}catch(Exception e){
			e.printStackTrace();
			logger.error("Errror in Data conversion:"+e);
		}finally{
			data = null;
		}
		//System.out.println("data-1="+data);
	}

	
/*	public void process (String xml)
	{
	  logger.debug("ChasRfidProc Now Msg Recieved :"+xml);
	  try
	  {
		  msgHandler.setTextObj(xml);
		 Object o = msgHandler.getXmlObj();
		 Snx snxObj = (Snx) o;
			
		 if (snxObj.getUnit() == null || snxObj.getUnit().size() <= 0) {
		 	logger.error("Unit Object from now is NULL  : "+ xml);
		 	return;
		 }
		 	
		 String xmlStr = createXmlStr(snxObj);

		 logger.debug("ChasRfidProc GroovyInj Now Message:"+xmlStr);  
		 sender.send(xmlStr);
		 
	 }catch(Exception e){
	 	 e.printStackTrace();
		 logger.error("ChasRfidProcessor Error ", e);
	 }finally{
		
	 }
    }*/	
	
/*	public void process (String xml)
	{
	  TosLookup lookup = null;
	  logger.debug("ChasRfidProc Now Message:"+xml);
	  try
	  {
		 msgHandler.setTextObj(xml);
		 Object o = msgHandler.getXmlObj();
		 Snx snxObj = (Snx) o;
			
		 if (snxObj.getUnit() == null || snxObj.getUnit().size() <= 0) {
		 	logger.error("Unit Object from now is NULL  : "+ xml);
		 	return;
		 }
		 	
		 List<TUnit> unitList = snxObj.getUnit();
		 TUnit aUnit = (TUnit)unitList.get(0);
		 logger.debug("unitId ="+aUnit.getId());
		 List<TUnitEquipment> equipList = unitList.get(0).getEquipment();
		 TUnitEquipment equipPrim = (TUnitEquipment)equipList.get(0);
		 String equipPrimId = equipPrim.getEqid();
		 
		 String position = aUnit.getPosition().getSlot();
		 String notes = aUnit.getSnxUpdateNote();
		 notes = notes != null ? notes : "";
			
		 String cntrEqType = null;
		 String chassEqType = null;
		 String attachedCntr = null;
		 lookup = new TosLookup();
		 //1. Handle Cntr and Chs Msg
		 if(equipList != null && equipList.size() == 2){
		 	//get Equip type Code
		    TUnitEquipment equipSec = (TUnitEquipment)equipList.get(1); 
			logger.debug("Equip1="+equipPrimId+"Equip2="+equipSec.getEqid()); 
		 	cntrEqType = lookup.getEquipmentType(equipPrimId);
		 	chassEqType = lookup.getEquipmentType(equipSec.getEqid());
				
			//set Fields for unit Mount 
		 	equipPrim.setType(cntrEqType);
		 	equipSec.setType(chassEqType);
			aUnit.setSnxUpdateNote("NowMount"+","+notes);
			logger.debug("cntrEqType="+cntrEqType+"  chassEqType="+chassEqType);
			
	     }else if (equipList != null && equipList.size() == 1){
			 //2.1 Lookup Attached unit 
	    	 chassEqType = lookup.getEquipmentType(equipPrimId);
	    	 attachedCntr = lookup.getAttachedUnit(equipPrimId);
				
 			 if(attachedCntr == null){
				//2.2 - Set bare chassis values
				equipPrim.setType(chassEqType);
				aUnit.setSnxUpdateNote("NowPositionUpdate"+",chasPosition:"+position+","+notes);
			}else if (attachedCntr != null ){
				//2.3 Set Equip attributes for attached unit 
				TUnitEquipment equipAttached = new TUnitEquipment();
				cntrEqType = lookup.getEquipmentType(attachedCntr);
				equipAttached.setEqid(attachedCntr);
				equipAttached.setClazz(TEquipmentClass.CTR);
				equipAttached.setRole("PRIMARY");
				equipAttached.setType(cntrEqType);
				equipList.add(equipAttached);
				
				
				equipPrim.setType(chassEqType);
				equipPrim.setRole("CARRIAGE");
				aUnit.setId(attachedCntr);
				aUnit.setUniqueKey(attachedCntr);

				if(notes.contains("nowborn")){
					aUnit.setSnxUpdateNote("NowMount"+","+notes);   
				}else{
					aUnit.setSnxUpdateNote("NowDismount"+",chasPosition:"+position+","+notes);	
				}
			}
		 }else{
			 logger.debug("Bad Now Message");
		 }
		 
		 sendUnit(snxObj);
		 
	 }catch(Exception e){
	 	 e.printStackTrace();
		 logger.error("ChasRfidProcessor Error ", e);
	 }finally{
		//closed connection.
		if(lookup != null) lookup.close(); 
	 }
    }
*/

/*	public void sendUnit(Snx snxObj){
		try{
		 //Marshal Obj to xml and Post message
		 snxMsgHandler.setXmlObj(snxObj);
		 String xmlStr = snxMsgHandler.getXmlStr();
		 logger.debug("ChasRfidProc updated Now Message:"+xmlStr);
		 sender.send(xmlStr);
		}catch(Exception e){
			logger.error("SendUnit Error :"+e);
		}
	}
*/
	
	public static void main(String[] args){
		
		String xml = "<?xml version='1.0' encoding='UTF-8'?>"+
		"<argo:snx xmlns:argo='http://www.navis.com/argo' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.navis.com/argo snx.xsd'>"+
		"<unit id='MATU4553946' transit-state='YARD' snx-update-note='Test' unique-key='MATU4553946' >"+
		"  <equipment eqid='MATU4553946' class='CTR' role='PRIMARY' />"+
		"  <equipment eqid='MATZ9022294' class='CHS' role='CARRIAGE'/>"+
		"  <position slot='A1216' location='SI' loc-type='YARD' />"+
		"</unit>"+
		"</argo:snx>";
		
		xml = "<?xml version='1.0' encoding='UTF-8'?>"+
		"<argo:snx xmlns:argo='http://www.navis.com/argo' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.navis.com/argo snx.xsd'>"+
		"<unit id='MATZ9022294' transit-state='YARD' snx-update-note='Test' unique-key='MATZ9022294' >"+
		"  <equipment eqid='MATZ9022294' class='CHS' role='PRIMARY'/>"+
		"  <position slot='A1216' location='SI' loc-type='YARD' />"+
		"</unit>"+
		"</argo:snx>";
		
		ChasRfidProcessor chasRfidProc = new ChasRfidProcessor ();
		//chasRfidProc.process(xml);
		chasRfidProc.kulanaProcess("701", "ASSIGN SHOP");
		
	}
}
