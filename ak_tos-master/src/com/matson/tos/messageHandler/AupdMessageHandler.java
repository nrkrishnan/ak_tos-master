/*
* Srno   Date		AuthorName Desc
* A1     10/13/10   GR	       Major feature added to up2,up3  
*/
package com.matson.tos.messageHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import com.matson.tos.exception.TosException;
import com.matson.tos.jatb.Aupd;
import com.matson.tos.util.CheckDigit;
import com.matson.tos.util.EquipmentType;
import com.matson.tos.util.GroovyXmlUtil;

/**
 * A1  SKB Change, substring for inches was wrong
 * A2  SKB Change, 5/27/09 Added checkdigit lookup
 * A3  GR  Change  10/28/09 Added majorFeature to AUP1 
 */
	public class AupdMessageHandler extends AbstractMessageHandler {
		private static Logger logger = Logger.getLogger(AtrxMessageHandler.class);
		public static final String CLASS_LOC = "database";
		public static final String CLASS_NAME = "GvyInjAupd";
		private CheckDigit check = new CheckDigit();
		
		public AupdMessageHandler(String xmlObjPackageName,
				String textObjPackageName, String fmtFile, int convertDir)
				throws TosException {
			super(xmlObjPackageName, textObjPackageName, fmtFile, convertDir);
			// TODO Auto-generated constructor stub
		}
		
		protected String createXmlStr() throws TosException {
			Map<String, String> data = new HashMap<String, String>();
			Aupd textObj = (Aupd)createTextObj();
			populateData(data, textObj);
			
			return GroovyXmlUtil.getInjectionXmlStr( CLASS_NAME, CLASS_LOC, data);
		}
		
		private void populateData( Map<String, String> data, Aupd updObj) {
			String txnCode = updObj.getTxnCode().trim();
			String majorFeature = updObj.getMajorFeature() != null ? updObj.getMajorFeature().trim() : "";
			if ( txnCode.equalsIgnoreCase( "AUP1"))  {// container
				String typeCode = acetsType2N4Type( updObj.getTypeCode().trim());
				EquipmentType type = new EquipmentType(typeCode);
				
				if(type.getType() != null) {
					data.put( "typeCode", type.getType());
				}
				if(type.getGrade() != null) {
					data.put( "grade", type.getGrade());
				}//A3
			}
			else if ( txnCode.equalsIgnoreCase( "AUP2")) // chassis
				if ( updObj.getTypeCode().trim().length() > 3)
					data.put( "typeCode", updObj.getTypeCode().trim().substring(0, 3));
				else 
					data.put( "typeCode", updObj.getTypeCode().trim());
			else if ( txnCode.equalsIgnoreCase( "AUP3")) // accesssories
				data.put( "typeCode", "MG01"); // hard code. No other types found.

			if(majorFeature != null){ data.put( "majorFeature", majorFeature); }
			
			String equipNumber = updObj.getEquipNumber();
			if(equipNumber != null) equipNumber = equipNumber.trim();
			String checkDigit = updObj.getCheckDigit();
			if(checkDigit != null) checkDigit = checkDigit.trim();
			String primaryCarrier = updObj.getPrimaryCarrier();
			if(primaryCarrier != null) primaryCarrier = primaryCarrier.trim();
			String primaryController = updObj.getPrimaryController();
			if(primaryController != null) primaryController = primaryController.trim();
			
			String id = equipNumber + checkDigit;
			id = check.getCheckDigitUnit(id);
			data.put( "equipment-id", id);
			data.put( "primCarrier", primaryCarrier);
			data.put( "primController", primaryController);
			data.put( "acetsMsgType", txnCode);
		}


		@Override
		protected Object textObjToXmlObj(Object textObj) throws TosException {
			// TODO Auto-generated method stub
			/*Aupd aupdObj = (Aupd)textObj;
			Snx snxObj = new Snx();
			List<TTruckingCompany> truckCompList = snxObj.getTruckingCompany();
			TTruckingCompany truckComp = new TTruckingCompany();
			truckCompList.add( truckComp);
			
			// on line agreement level
			TTruckingCompany.LineAgreements lineAgree;
			lineAgree = truckComp.getLineAgreements();
			if ( lineAgree == null) {
				lineAgree = new TTruckingCompany.LineAgreements();
				truckComp.setLineAgreements( lineAgree);
			}
			
			lineAgree.setUpdateMode( TUpdateMode.MERGE);
			TLineAgreement aLine = new TLineAgreement();
			List<TLineAgreement> lineList = lineAgree.getLineAgreement();
			lineList.add( aLine);
			aLine.setShippingLine( "MAT");
			aLine.setExpirationDate( CalendarUtil.getXmlCalendar( "12/31/2050", "MM/dd/yyyy"));
			if ( aupdObj.getTxnCode().trim().equalsIgnoreCase( "ATRU") ||
					aupdObj.getTxnCode().trim().equalsIgnoreCase( "ATRA"))
				aLine.setTruckLineStatus( TTruckStatus.OK);
			else if ( aupdObj.getTxnCode().trim().equalsIgnoreCase( "ATRD"))
				aLine.setTruckLineStatus( TTruckStatus.RCVONLY);
			else
				logger.debug( "Wrong txn code: " + aupdObj.getTxnCode().trim());
			
			return snxObj; */
			return null;
		}

		@Override
		protected Object xmlObjToTextObj(Object xmlObj) throws TosException {
			// TODO Auto-generated method stub
			return null;
		}
		
		private static String acetsType2N4Type( String acetsType) {
			String ret = "";
			
			if ( acetsType.trim().length() < 8)
				if ( acetsType.trim().length() < 3)
					return ret;
				else 
					return acetsType.substring(0, 3);
			
			int height;
			String heightStr = acetsType.substring( 4, 6);
			try {
				int h1 = Integer.parseInt( heightStr.substring(0, 1));
				// Fixed substring
				int h2 = Integer.parseInt( heightStr.substring(1, 2));
				height = h1 * 12 + h2;
			} catch ( Exception ex) {
				ret = acetsType.substring(0, 3);
				return ret;
			}
			
			String firstChar = acetsType.substring(0, 1);
			if ( firstChar.equalsIgnoreCase( "A")) {
				if ( height  <= 138)
					ret = acetsType.substring(0, 3) + "L";
				else if ( height  >= 152)
					ret = acetsType.substring(0, 3) + "H";
				else 
					ret = acetsType.substring(0, 3);
			} else if ( firstChar.equalsIgnoreCase( "F")) {
				if ( heightStr.equalsIgnoreCase( "13")) 
					ret = acetsType.substring(0, 3) + "M";
				else if ( height <= 96)
					ret = acetsType.substring(0, 3) + "L";
				else if ( height < 102)
					ret = acetsType.substring(0, 3) + "H";
				else
					ret = acetsType.substring(0, 3);
			} else if ( firstChar.equalsIgnoreCase( "R")) {
				if ( height <= 96)
					ret = acetsType.substring(0, 3) + "L";
				else if ( height > 102)
					ret = acetsType.substring(0, 3) + "H";
				else
					ret = acetsType.substring(0, 3);
			} else {
				if ( height <= 96)
					ret = acetsType.substring(0, 3) + "L";
				else if ( height > 102)
					ret = acetsType.substring(0, 3) + "H";
				else 
					ret = acetsType.substring(0, 3);
			} 
			
			String last2Chars = acetsType.substring(6, 8);
			if ( last2Chars.equalsIgnoreCase( "H3") || last2Chars.equalsIgnoreCase( "H4") ||
				last2Chars.equalsIgnoreCase( "GB") || last2Chars.equalsIgnoreCase( "GR") ||
				last2Chars.equalsIgnoreCase( "GY") || last2Chars.equalsIgnoreCase( "CL") ||
				last2Chars.equalsIgnoreCase( "FC") || last2Chars.equalsIgnoreCase( "HG")) {
			
				if ( ret.length() == 3)
					ret = ret + " " + last2Chars;
				else 
					ret = ret + last2Chars;
			}
			return ret;
		}
		
/*		public static void main(String args[]){
			try{
				String msg = "AUP1¦04/08/09¦11:35:57¦NEQR2¦027539151¦HLXU339595  ¦MTY ¦MAT ¦04/08/09¦D20 86XX¦                                                  ¦0¦SHOW¦HLC¦HLCU¦ST";
				AupdMessageHandler	msgHandler = new AupdMessageHandler( "com.matson.tos.jaxb.snx",
						"com.matson.tos.jatb", "xml/aupd.xml", AbstractMessageHandler.TEXT_TO_XML);
				
				 msgHandler.setTextStr(msg);
				String str = msgHandler.createXmlStr();
				System.out.println(str);
				
				
			}catch(Exception e){
				e.printStackTrace();
			}
		}
*/
	}

