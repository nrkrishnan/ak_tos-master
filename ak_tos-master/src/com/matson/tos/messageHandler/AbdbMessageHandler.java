package com.matson.tos.messageHandler;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.matson.tos.exception.TosException;
import com.matson.tos.jatb.Abda;
import com.matson.tos.jatb.Abdb;
import com.matson.tos.jatb.Acar;
import com.matson.tos.jaxb.snx.Snx;
import com.matson.tos.jaxb.snx.TBkg;
import com.matson.tos.jaxb.snx.TBooking;
import com.matson.tos.jaxb.snx.TCarrier;
import com.matson.tos.jaxb.snx.TDirection;
import com.matson.tos.jaxb.snx.TReeferRequirements;
import com.matson.tos.jaxb.snx.TRouting;
import com.matson.tos.jaxb.snx.TUnit;
import com.matson.tos.jaxb.snx.TUnitContents;
import com.matson.tos.jaxb.snx.TUnitSeals;
import com.matson.tos.util.GroovyXmlUtil;
import com.matson.tos.util.StrUtil;
import com.matson.tos.util.UnitConversion;

public class AbdbMessageHandler extends AbstractMessageHandler {
	
	private static Logger logger = Logger.getLogger(AbdbMessageHandler.class);
	public static final String CLASS_LOC = "database";
	public static final String CLASS_NAME = "GvyInjAbdb";

	public AbdbMessageHandler(String xmlObjPackageName,
			String textObjPackageName, String fmtFile, int convertDir)
			throws TosException {
		super(xmlObjPackageName, textObjPackageName, fmtFile, convertDir);
		// TODO Auto-generated constructor stub
	}
	
	protected String createXmlStr() throws TosException {
		Map<String, String> data = new HashMap<String, String>();
		Abdb textObj = (Abdb)createTextObj();
		populateData(data, textObj);
		
		return GroovyXmlUtil.getInjectionXmlStr( CLASS_NAME, CLASS_LOC, data);
	}
	
	private void populateData( Map<String, String> data, Abdb bdbObj) {
		data.put("equipment-id", bdbObj.getEquipmentNum().trim() + bdbObj.getCheckDigit().trim());
		data.put( "recorder", bdbObj.getUserId().trim());
		String dirSeq = bdbObj.getDirection().trim().toUpperCase();
		String vesVisit = "";
		if ( dirSeq.equals("N") || dirSeq.equals("S") ||
				dirSeq.equals("E") || dirSeq.equals("W")) {
			vesVisit = bdbObj.getVessel().trim() + bdbObj.getVoyage().trim();
		} else {
			vesVisit = bdbObj.getVessel().trim() + bdbObj.getVoyage().trim() + dirSeq;
		}
		data.put( "vesvoy", vesVisit);
		data.put( "bookingNum", bdbObj.getBookingNum().trim());
		data.put("dischargePort", bdbObj.getDischargePort().trim());
		data.put( "destPort", bdbObj.getDestPort().trim());
		data.put( "loadPort", bdbObj.getLoadPort().trim());
		String shipperName = bdbObj.getShipperOrgn50().trim();
		if ( bdbObj.getShipQual40() != null && bdbObj.getShipQual40().trim().length() > 0) {
			shipperName = shipperName + " " + bdbObj.getShipQual40().trim();
			if ( shipperName.length() > 80)
				shipperName = shipperName.substring( 0, 80);
		}
		data.put( "shipperId", StrUtil.padLeadingZero( bdbObj.getShipperArol().trim(), 10));
		data.put( "shipperName", shipperName);
		String ConsigneeName = bdbObj.getConsignOrgn50().trim();
		if ( bdbObj.getConsigneeQual40() != null && bdbObj.getConsigneeQual40().trim().length() > 0) {
			ConsigneeName = ConsigneeName + " " + bdbObj.getConsigneeQual40().trim();
			if ( ConsigneeName.length() > 80)
				ConsigneeName = ConsigneeName.substring( 0, 80);
		}
		data.put( "consigneeId", StrUtil.padLeadingZero( bdbObj.getConsigneeArol().trim(), 10));
		data.put( "consigneeName", ConsigneeName);
		data.put( "primCarrier", bdbObj.getPrimCarrier().trim());
		data.put( "trade", bdbObj.getTrade().trim());
		data.put( "sit", bdbObj.getSit().trim());
		data.put( "tit", bdbObj.getTit().trim());
		data.put( "inBound", bdbObj.getInBound().trim());
		data.put( "ag", bdbObj.getAg().trim());
		if (bdbObj.getUnitFlexString02() != null)
			data.put( "unitFlexString02", bdbObj.getUnitFlexString02().trim());
		
		
		/* Alaska Changes - Start */		

		String temp1 = bdbObj.getTemp1();
		String temp2 = bdbObj.getTemp2();
		String tempUnit = bdbObj.getTempUnit();

		if(temp1!=null){		
			logger.debug("Temp1 is not null value: " + temp1);
			if("KFF".equalsIgnoreCase(temp1) || "AMB".equalsIgnoreCase(temp1)){										
				data.put( "temp1", temp1);
			}else{
				if(tempUnit!=null && tempUnit.length() > 0) {
					
					if ( ( "C".equalsIgnoreCase(tempUnit))) {
						try {
				
							data.put( "temp1", new BigDecimal( temp1).toString()+tempUnit);
							
						} catch (Exception e) {
							logger.error("Could not convert temp1 "+temp1+" for booking.");
						}
					} else if (  "F".equalsIgnoreCase(tempUnit)) {
						if ( temp1.trim().length() != 0) {
							try {
								double tempDbl = new Float( temp1).doubleValue();
								tempDbl = ( tempDbl -32)/9*5;
								
								data.put( "temp1", new BigDecimal( tempDbl).setScale( 3, RoundingMode.HALF_UP).toString()+tempUnit);												
								
							} catch (Exception e) {
								logger.error("Could not convert temp1 "+temp1+" for booking.");
							}	
						}
					}
				}
			}
		}
		
	if(temp2!=null){
		logger.debug("Temp2 is not null value: " + temp2);
		if("KFF".equals(temp2) || "AMB".equalsIgnoreCase(temp2)){							
			data.put( "temp2", temp2);
		}else{
			if(tempUnit!=null && tempUnit.length() > 0) {
				
				if ( ( "C".equalsIgnoreCase(tempUnit))) {
					try {
			
						data.put( "temp2", new BigDecimal( temp2).toString()+tempUnit);
						
					} catch (Exception e) {
						logger.error("Could not convert temp2 "+temp2+" for booking.");
					}
				} else if ( ( "F".equalsIgnoreCase(tempUnit))) {
					if ( temp2.trim().length() != 0) {
						try {
							double tempDbl = new Float( temp2).doubleValue();
							tempDbl = ( tempDbl -32)/9*5;							
							data.put( "temp2", new BigDecimal( tempDbl).setScale( 3, RoundingMode.HALF_UP).toString()+tempUnit);												
							
						} catch (Exception e) {
							logger.error("Could not convert temp2 "+temp2+" for booking.");
						}	
					}
				}
			}
		}
	}
		
		/* Alaska Changes - end*/
		
		
	}

	@Override
	protected Object textObjToXmlObj(Object textObj) throws TosException {
		/*Abdb bdbObj = (Abdb)textObj;
		Snx snxObj = new Snx();
		
		snxObj.setUserId( bdbObj.getUserId().trim());
		List<TUnit> unitList = snxObj.getUnit();
		
		TUnit aUnit = new TUnit();
		unitList.add( aUnit);
		aUnit.setId( bdbObj.getEquipmentNum().trim() + bdbObj.getCheckDigit().trim());
		if ( bdbObj.getPrimCarrier().trim().length() == 0)
			aUnit.setLine( "MAT");
		else
			aUnit.setLine( bdbObj.getPrimCarrier().trim().toUpperCase());
		// Unit routing
		TRouting aRouting = new TRouting();
		aUnit.setRouting( aRouting);
		List<TRouting.Carrier> carrList = aRouting.getCarrier();
		
		TRouting.Carrier aCarr = new TRouting.Carrier();
		carrList.add( aCarr);
		aCarr.setDirection( TDirection.IB);
		aCarr.setQualifier( "DECLARED");
		aCarr.setMode( "VESSEL");
		String dirSeq = bdbObj.getDirection().trim().toUpperCase();
		String vesVisit = "";
		if ( dirSeq.equals("N") || dirSeq.equals("S") ||
				dirSeq.equals("E") || dirSeq.equals("W")) {
			vesVisit = bdbObj.getVessel().trim() + bdbObj.getVoyage().trim();
			aCarr.setId( vesVisit);
		} else {
			vesVisit = bdbObj.getVessel().trim() + bdbObj.getVoyage().trim() + dirSeq;
			aCarr.setId( vesVisit);
		}
		
		aRouting.setPod1( bdbObj.getDischargePort().trim());
		aRouting.setDestination( bdbObj.getDestPort().trim());
		aRouting.setPol( bdbObj.getLoadPort().trim());
		aRouting.setCarrierService( bdbObj.getTrade().trim());
		
		// booking
		/*
		List<TBooking> bookingList = snxObj.getBooking();
		TBooking aBooking = new TBooking();
		bookingList.add( aBooking);
		if ( bdbObj.getPrimCarrier().trim().length() == 0)
			aBooking.setLine( "MAT");
		else
			aBooking.setLine( bdbObj.getPrimCarrier().trim());

		aBooking.setNbr( bdbObj.getBookingNum().trim());
		//A1 SNX change 1.6.2
		aBooking.setPreventTypeSubst("false");
		if ( bdbObj.getSit().trim().equalsIgnoreCase("Y"))
			aBooking.setDrayOff( "OFFSITE");

		aBooking.setOrigin( bdbObj.getLoadPort().trim());
		aBooking.setPod1( bdbObj.getDischargePort().trim());
		aBooking.setPol( bdbObj.getLoadPort().trim());
		//A1 SNX change 1.6.2
		aBooking.setPreventTypeSubst("FALSE");
		*/
/*		
		TBkg aBooking = new TBkg();
		aUnit.setBooking( aBooking);

		aBooking.setId( bdbObj.getBookingNum().trim());
		if ( bdbObj.getPrimCarrier().trim().length() == 0)
			aBooking.setLine( "MAT");
		else
			aBooking.setLine( bdbObj.getPrimCarrier().trim());
		
		aBooking.setVesslVisit( vesVisit);
*/		
	
		//shipper & consignee
		/*TUnitContents contents = new TUnitContents();
		aUnit.setContents( contents);
		contents.setBlNbr( bdbObj.getBookingNum().trim());
		contents.setShipperId( StrUtil.padLeadingZero( bdbObj.getShipperArol().trim(), 10));
		String shipperName = bdbObj.getShipperOrgn50().trim();
		if ( bdbObj.getShipQual40().trim().length() > 0) {
			shipperName = shipperName + " " + bdbObj.getShipQual40().trim();
			if ( shipperName.length() > 80)
				shipperName = shipperName.substring( 0, 80);
		}
		contents.setShipperName( shipperName);
	
		contents.setConsigneeId( StrUtil.padLeadingZero( bdbObj.getConsigneeArol().trim(), 10));
		String ConsigneeName = bdbObj.getConsignOrgn50().trim();
		if ( bdbObj.getConsigneeQual40().trim().length() > 0) {
			ConsigneeName = ConsigneeName + " " + bdbObj.getConsigneeQual40().trim();
			if ( ConsigneeName.length() > 80)
				ConsigneeName = ConsigneeName.substring( 0, 80);
		}
		contents.setConsigneeName( ConsigneeName);		
		*/
		return null;

	}

	@Override
	protected Object xmlObjToTextObj(Object xmlObj) throws TosException {
		// TODO Auto-generated method stub
		return null;
	}

}
