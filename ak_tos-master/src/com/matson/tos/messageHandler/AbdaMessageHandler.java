package com.matson.tos.messageHandler;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.matson.tos.exception.TosException;
import com.matson.tos.jatb.Abda;
import com.matson.tos.jatb.Abdb;
import com.matson.tos.jaxb.snx.Snx;
import com.matson.tos.jaxb.snx.TBkg;
import com.matson.tos.jaxb.snx.TCarrier;
import com.matson.tos.jaxb.snx.TDirection;
import com.matson.tos.jaxb.snx.TReeferRequirements;
import com.matson.tos.jaxb.snx.TRouting;
import com.matson.tos.jaxb.snx.TUnit;
import com.matson.tos.jaxb.snx.TUnitAssignToBooking;
import com.matson.tos.jaxb.snx.TUnitContents;
import com.matson.tos.jaxb.snx.TUnitIdentity;
import com.matson.tos.jaxb.snx.TUnitSeals;
import com.matson.tos.util.GroovyXmlUtil;
import com.matson.tos.util.StrUtil;
import com.matson.tos.util.UnitConversion;


/**
 * Change History
 * 04/27/2009	SKB		Changed mapping for dest from discharge to dest.
 *
 */
public class AbdaMessageHandler extends AbstractMessageHandler {
	public static final String CLASS_LOC = "database";
	public static final String CLASS_NAME = "GvyInjAbda";

	public AbdaMessageHandler(String xmlObjPackageName,
			String textObjPackageName, String fmtFile, int convertDir)
			throws TosException {
		super(xmlObjPackageName, textObjPackageName, fmtFile, convertDir);
		// TODO Auto-generated constructor stub
	}

	protected String createXmlStr() throws TosException {
		Map<String, String> data = new HashMap<String, String>();
		Abda textObj = (Abda)createTextObj();
		populateData(data, textObj);
		
		return GroovyXmlUtil.getInjectionXmlStr( CLASS_NAME, CLASS_LOC, data);
	}
	
	private void populateData( Map<String, String> data, Abda bdaObj) {
		data.put("equipment-id", bdaObj.getEquipmentNum().trim() + bdaObj.getCheckDigit().trim());
		data.put( "recorder", bdaObj.getUserId().trim());
		String dirSeq = bdaObj.getDirection().trim().toUpperCase();
		String vesVisit = "";
		if ( dirSeq.equals("N") || dirSeq.equals("S") ||
				dirSeq.equals("E") || dirSeq.equals("W")) {
			vesVisit = bdaObj.getVessel().trim() + bdaObj.getVoyage().trim();
		} else {
			vesVisit = bdaObj.getVessel().trim() + bdaObj.getVoyage().trim() + dirSeq;
		}
		data.put( "vesvoy", vesVisit);
		data.put( "bookingNum", bdaObj.getBookingNum().trim());
		data.put("dischargePort", bdaObj.getDischargePort().trim());
		//data.put( "destPort", bdaObj.getDischargePort().trim());
		data.put( "destPort", bdaObj.getDestPort().trim());
		data.put( "loadPort", bdaObj.getLoadPort().trim());
		
		String shipperName = bdaObj.getShipperOrgn50().trim();
		if ( bdaObj.getShipperQual40().trim().length() > 0) {
			shipperName = shipperName + " " + bdaObj.getShipperQual40().trim();
			if ( shipperName.length() > 80)
				shipperName = shipperName.substring( 0, 80);
		}
		data.put( "shipperId", StrUtil.padLeadingZero( bdaObj.getShipperArol().trim(), 10));
		data.put( "shipperName", shipperName);
		String ConsigneeName = bdaObj.getConsigneeOrgn50().trim();
		if ( bdaObj.getConsigneeQual40() != null &&  bdaObj.getConsigneeQual40().trim().length() > 0) {
			ConsigneeName = ConsigneeName + " " + bdaObj.getConsigneeQual40().trim();
			if ( ConsigneeName.length() > 80)
				ConsigneeName = ConsigneeName.substring( 0, 80);
		}
		data.put( "consigneeId", StrUtil.padLeadingZero( bdaObj.getConsigneeArol().trim(), 10));
		data.put( "consigneeName", ConsigneeName);
		if ( bdaObj.getTempScale().trim().equalsIgnoreCase( "F")
				&& bdaObj.getTemp().trim().length() >0) {
			String temp = null;
			try {
				temp = new BigDecimal( UnitConversion.fahrenheitToCelsius( bdaObj.getTemp().trim())).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
			} catch ( Exception ex) {
				temp = "";
			}
			data.put( "temp", temp);
		} else { 
			data.put( "temp", bdaObj.getTemp().trim());
		}
		data.put( "sealNum", bdaObj.getCurrentSeal().trim());
		data.put( "commodity", bdaObj.getCommodity().trim());
	}

	@Override
	protected Object textObjToXmlObj(Object textObj) throws TosException {
		/*
		Abda bdaObj = (Abda)textObj;
		Snx snxObj = new Snx();
		
		snxObj.setUserId( bdaObj.getUserId().trim());
		List<TUnit> unitList = snxObj.getUnit();
		
		TUnit aUnit = new TUnit();
		unitList.add( aUnit);
		aUnit.setId( bdaObj.getEquipmentNum().trim() + bdaObj.getCheckDigit().trim());
		
		// Unit routing
		TRouting aRouting = new TRouting();
		aUnit.setRouting( aRouting);
		List<TRouting.Carrier> carrList = aRouting.getCarrier();
		
		TRouting.Carrier aCarr = new TRouting.Carrier();
		carrList.add( aCarr);
		aCarr.setDirection( TDirection.OB);
		aCarr.setQualifier( "DECLARED");
		aCarr.setMode( "VESSEL");
		String dirSeq = bdaObj.getDirection().trim().toUpperCase();
		String vesVisit = "";
		if ( dirSeq.equals("N") || dirSeq.equals("S") ||
				dirSeq.equals("E") || dirSeq.equals("W")) {
			vesVisit = bdaObj.getVessel().trim() + bdaObj.getVoyage().trim();
			aCarr.setId( vesVisit);
		} else {
			vesVisit = bdaObj.getVessel().trim() + bdaObj.getVoyage().trim() + dirSeq;
			aCarr.setId( vesVisit);
		}
		
		aRouting.setPod1( bdaObj.getDischargePort().trim());
		aRouting.setDestination( bdaObj.getDestPort().trim());
		aRouting.setPol( bdaObj.getLoadPort().trim());
		
		// booking
		TBkg aBooking = new TBkg();
		aUnit.setBooking( aBooking);

		aBooking.setId( bdaObj.getBookingNum().trim());
		if ( bdaObj.getShipperName().trim().length() == 0)
			aBooking.setLine( "MAT");
		else
			aBooking.setLine( bdaObj.getShipperName().trim());
		
		aBooking.setVesslVisit( vesVisit);
		
		//temperature
		if ( bdaObj.getTemp().trim().length() > 0) {
			TReeferRequirements temp = new TReeferRequirements();
			aUnit.setReefer( temp);
			if ( bdaObj.getTempScale().trim().equalsIgnoreCase( "F")){
				double tempC = UnitConversion.fahrenheitToCelsius( bdaObj.getTemp().trim());
				temp.setTempReqdC( new BigDecimal( tempC).setScale(1, BigDecimal.ROUND_HALF_UP));
				
			} else {
				temp.setTempReqdC( new BigDecimal( bdaObj.getTemp().trim()));
			}
		}
		
		//seal
		if ( bdaObj.getCurrentSeal().trim().length() > 0) {
			TUnitSeals seals = new TUnitSeals();
			aUnit.setSeals( seals);
			seals.setSeal1( bdaObj.getCurrentSeal().trim());
		}
		//shipper & consignee
		if ( bdaObj.getShipperName().trim().length() > 0 || 
				bdaObj.getConsignee().trim().length() > 0) {
			TUnitContents contents = new TUnitContents();
			aUnit.setContents( contents);
			contents.setShipperName( bdaObj.getShipperName().trim());
			contents.setConsigneeName( bdaObj.getConsignee().trim());
		}
		return snxObj; */
		return null;
	}

	@Override
	protected Object xmlObjToTextObj(Object xmlObj) throws TosException {
		// TODO Auto-generated method stub
		return null;
	}

}
