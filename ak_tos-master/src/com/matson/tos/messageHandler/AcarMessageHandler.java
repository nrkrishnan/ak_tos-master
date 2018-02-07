/**
 * 
 */
package com.matson.tos.messageHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.matson.tos.dao.TosLookup;
import com.matson.tos.jaxb.snx.Snx;
import com.matson.tos.util.EmailSender;
import com.matson.tos.util.JMSSender;
import com.matson.tos.util.TosRefDataUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.matson.tos.exception.TosException;
import com.matson.tos.jatb.Acar;
import com.matson.tos.util.GroovyXmlUtil;

/**
 * @author JZF
 * 
 * A1   12/19/2008	SKB		Change weight to KG.
 * A2   04/13/2009  SKB		Added leg to VesVoy.
 *
 */
public class AcarMessageHandler extends AbstractMessageHandler {
	private static Logger logger = Logger.getLogger(AcarMessageHandler.class);

	public static final String CLASS_LOC = "database";
	public static final String CLASS_NAME_STUFF = "StuffUnit";
	public static final String CLASS_NAME_UNSTUFF = "StripUnit";

	String _msgString = "";
	public AcarMessageHandler(String xmlObjPackageName,
			String textObjPackageName, String fmtFile, int convertDir)
			throws TosException {
		super(xmlObjPackageName, textObjPackageName, fmtFile, convertDir);
		// TODO Auto-generated constructor stub
	}
	
	//overwrite parent method to create XML string without schema file
	protected String createXmlStr() throws TosException {
		Map<String, String> data = new HashMap<String, String>();
		_msgString = getTextStr();
		Acar textObj = (Acar)createTextObj();
		populateData(data, textObj);
		if ( textObj.getEventType().trim().equalsIgnoreCase( "ULD"))
			return GroovyXmlUtil.getInjectionXmlStr( CLASS_NAME_UNSTUFF, CLASS_LOC, data);
		else {
			return GroovyXmlUtil.getInjectionXmlStr( CLASS_NAME_STUFF, CLASS_LOC, data);
		}
	}
	
	/**
	 * A1, Convert lbs to kg.
	 * @param lbs
	 * @return
	 */
	private String convertLbstoKg(String lbs) {
		try {
			double value = Double.parseDouble(lbs);
			double kg = value/2.2046;
			return Double.toString(kg);
		} catch (Exception e) {
			return lbs;
		}
	}
	
	private void populateData( Map<String, String> data, Acar carObj) {
		data.put("equipment-id", carObj.getEquipmentNum().trim() + carObj.getCheckDigit().trim());
		data.put( "recorder", carObj.getUserId().trim());
		if ( !carObj.getEventType().trim().equalsIgnoreCase( "ULD")) { // for LDA, LDC, and LDR
			data.put("freight-kind", "FCL");
			data.put( "unit-gross-weight", convertLbstoKg(carObj.getGrossWeight().trim()));
			data.put("temperature", carObj.getTemperature());
			data.put("tempUnit", carObj.getTempScale());

			data.put( "routing-carrier-mode", "VESSEL");
			// Added Voyage for Barge.
			String dirSeq = carObj.getVesselLeg();
			if(dirSeq != null)
				dirSeq = dirSeq.trim().toUpperCase();
			else dirSeq = "N";
			String vesvoy;
			if ( dirSeq.equals("N") || dirSeq.equals("S"))
				vesvoy = carObj.getVesselCode().trim() + carObj.getVoyageNum().trim();
			else 
				vesvoy = carObj.getVesselCode().trim() + carObj.getVoyageNum().trim() +	dirSeq;
			
			data.put( "routing-carrier-id", vesvoy);
			
			data.put( "routing-destination", carObj.getDestPort().trim());
			data.put( "routing-pod-1", carObj.getDischargePort().trim());
			data.put( "routing-pol", carObj.getLoadPort().trim());
			//data.put( "position-slot", ""); // no location sent from ACETS
			data.put( "seal-1", carObj.getSealNum().trim()); // no seal number sent from ACETS
			data.put( "remark", carObj.getCommodity().trim());
			if ( carObj.getEventType().trim().equalsIgnoreCase( "LDA") ||
					carObj.getEventType().trim().equalsIgnoreCase( "LDR")) {
				if ( carObj.getConsigneeName().trim().length() != 0 ){
					data.put( "consignee-id", "AUTO"); // not sent from ACETS
					data.put( "consignee-name", carObj.getConsigneeName().trim());
				}
				data.put( "shipper-id", "AUTO"); // not sent from ACETS
				data.put( "shipper-name", "AUTOMOBILE"); //carObj.getShipperName().trim());
				data.put( "commodity-id", "AUTO");
				data.put( "commodity-name", "AUTO");
			} else { // for LDC
				data.put( "commodity-id", "CFS");
				data.put( "commodity-name", "CFS");
				data.put("consignee-name", "LCL");
				data.put("shipper-name", "LCL");
			}
			logger.info("Commodity ID set based GEMS event type : "+data.get("commodity-id"));
			//data.put( "oog-oversize-flag", carObj.getOversizeFlag().trim());
			if ( carObj.getOversizeFlag().trim().equalsIgnoreCase( "Y")) {
				if ( carObj.getOversizeHeight().trim().length() != 0)
					data.put( "oog-height", carObj.getOversizeHeight().trim());
				else 
					data.put( "oog-height", "0");
				if ( carObj.getOversizeLeft().trim().length() != 0)
					data.put( "oog-left", carObj.getOversizeLeft().trim());
				else
					data.put( "oog-left", "0");
				if ( carObj.getOversizeRight().trim().length() != 0)
					data.put( "oog-right", carObj.getOversizeRight().trim());
				else
					data.put( "oog-right", "0");
				if ( carObj.getOversizeFront().trim().length() != 0)
					data.put( "oog-front", carObj.getOversizeFront().trim());
				else 
					data.put( "oog-front", "0");
				if ( carObj.getOversizeRear().trim().length() != 0)
					data.put( "oog-back", carObj.getOversizeRear().trim());
				else 
					data.put( "oog-back", "0");
			}
			data.put("is-haz", carObj.getHazFlag());
		}
	}

	/*private void populateAndSendUnitHazard(String equipmentId) {
		try {

				AbdxJSONMessageHandler jsonMessageHandler = new AbdxJSONMessageHandler();
				logger.info("AbdxJSONMessageHandler instance created" + jsonMessageHandler);
				for (Snx snx : jsonMessageHandler.getSNXMessage(null, equipmentId)) {
					String fromSnxObject = jsonMessageHandler.getXMLStringFromSnxObject(snx);
					logger.info(fromSnxObject);
					JMSSender sender = new JMSSender(JMSSender.REAL_TIME_QUEUE, "ANK");
					sender.send(fromSnxObject);
				}

		} catch (Exception e) {
			logger.error("Errored out while finding hazard details from HAZMAT System for : "
					+equipmentId);
			e.printStackTrace();
			String mailText = "Car Message		"+_msgString;
			mailText+="\nException is\n"+e.getMessage();
			mailText+="\nStackTarce\n"+e.getStackTrace();
			EmailSender.sendMail("1aktosdevteam@matson.com", "1aktosdevteam@matson.com",
					"Exception Processing HAZ for ACAR Message: " + e.getMessage(), mailText);
		}
	}*/
	/* (non-Javadoc)
	 * @see com.matson.tos.messageHandler.AbstractMessageHandler#textObjToXmlObj(java.lang.Object)
	 */
	@Override
	protected Object textObjToXmlObj(Object textObj) throws TosException {
		// TODO Auto-generated method stub
		
		return null;
	}

	/* (non-Javadoc)
	 * @see com.matson.tos.messageHandler.AbstractMessageHandler#xmlObjToTextObj(java.lang.Object)
	 */
	@Override
	protected Object xmlObjToTextObj(Object xmlObj) throws TosException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	public static void main( String args[]) {
		try {
			AcarMessageHandler handler = new AcarMessageHandler("","","",1);
			//System.out.println(handler.convertLbstoKg("41200"));
			String ret = handler.createXmlStr();
			System.out.println( ret);
			
			
		} catch ( Exception ex) {
			
		}
	} */
}
