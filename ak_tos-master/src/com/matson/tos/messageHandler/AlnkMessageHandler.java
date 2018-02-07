package com.matson.tos.messageHandler;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.matson.tos.exception.TosException;
import com.matson.tos.jatb.Alnk;
import com.matson.tos.jatb.Aupd;
import com.matson.tos.util.GroovyXmlUtil;
import com.matson.tos.util.StrUtil;

public class AlnkMessageHandler extends AbstractMessageHandler {
	private static Logger logger = Logger.getLogger(AlnkMessageHandler.class);
	public static final String CLASS_LOC = "database";
	public static final String CLASS_NAME = "GvyInjAlnk";

	public AlnkMessageHandler(String xmlObjPackageName,
			String textObjPackageName, String fmtFile, int convertDir)
			throws TosException {
		super(xmlObjPackageName, textObjPackageName, fmtFile, convertDir);
		// TODO Auto-generated constructor stub
	}

	protected String createXmlStr() throws TosException {
		Map<String, String> data = new HashMap<String, String>();
		Alnk textObj = (Alnk)createTextObj();
		populateData(data, textObj);
		
		return GroovyXmlUtil.getInjectionXmlStr( CLASS_NAME, CLASS_LOC, data);
	}
	
	private void populateData( Map<String, String> data, Alnk lnkObj) {		
		data.put( "equipment-id", lnkObj.getLnkEquipmentNumber().trim() + lnkObj.getLnkEquipmentCheckDigit().trim());
		data.put( "bookingNum", lnkObj.getLnkShipmentNumber().trim());
		data.put( "userId", lnkObj.getLnkUserId().trim());
		//data.put( "shipperName", lnkObj.getLnkShipperName().trim());
		data.put( "orderNegotiablInd", lnkObj.getLnkOrderNegotiableInd().trim());
		data.put( "loadService", lnkObj.getLnkLoadService().trim());
		data.put( "discService", lnkObj.getLnkLoadDischargeService().trim());
		data.put( "inboundFlg", lnkObj.getLnkInboundFlag().trim());
		data.put( "cneeContact", lnkObj.getLnkConsigneeContact().trim());
		data.put( "cneeAreaCode", lnkObj.getLnkConsigneeAreaCode().trim());
		data.put( "cneePhoneCountryCode", lnkObj.getLnkConsigneePhoneCntryCode().trim());
		data.put( "cneePhoneExchange", lnkObj.getLnkConsigneePhoneExchange().trim());
		data.put( "cneePhoneExtension", lnkObj.getLnkConsigneePhoneExtension().trim());
		data.put( "cneePhoneStation", lnkObj.getLnkConsigneePhoneStation().trim());
		data.put( "destCityCode", lnkObj.getLnkFreightDestCityCode().trim());
		data.put( "primCarrier", lnkObj.getLnkPrimCarrier().trim());
		data.put( "trade", lnkObj.getLnkTrade().trim());
		data.put( "creditProfileStatus", lnkObj.getLnkCreditProfileStatus().trim());
		data.put( "shipFromAddrRoleId", lnkObj.getLnkShipFromAddrRoleId().trim());
		data.put( "shipFromAddrRoleQual", lnkObj.getLnkShipFromAddrRoleQaul().trim());
		data.put( "shipFromUseOrgName", lnkObj.getLnkShipFromUserOrgName().trim());
		data.put( "shipToAddrRoleId", lnkObj.getLnkShipToAddrRoleId().trim());
		data.put( "shipToAddrRoleQual", lnkObj.getLnkShipToAddrRoleQaul().trim());
		data.put( "referenceNum", lnkObj.getLnkReferenceNumber().trim());
		data.put( "shipToUseOrgName", lnkObj.getLnkShipToUserOrgName().trim());
		data.put( "shipToOwnOrgName", lnkObj.getLnkShipToOwnOrgName().trim());
		data.put( "shipToCoOrgNameQual", lnkObj.getLnkShipToCoOrgNameQual().trim());
		data.put( "shipToOrgAddrLine1", lnkObj.getLnkAddrLine1().trim());
		data.put( "shipToOrgSuite", lnkObj.getLnkSuite().trim());
		data.put( "shipToOrgCity", lnkObj.getLnkCity().trim());
		data.put( "shipToOrgState", lnkObj.getLnkState().trim());
		data.put( "shipToOrgCountry", lnkObj.getLnkCountry().trim());
		data.put( "shipToOrgZip", lnkObj.getLnkZipCode().trim());
		data.put( "shipmentRefNum", lnkObj.getLnkShipmentRefNumber().trim());
		String vesvoy = "";
		String direction = lnkObj.getLnkDirection().trim();
		if ( direction.equals("N") || direction.equals("S") ||
				direction.equals("E") || direction.equals("W")) {
			vesvoy = lnkObj.getLnkVessel().trim() + lnkObj.getLnkVoyage().trim();
		} else {
			vesvoy = lnkObj.getLnkVessel().trim() + lnkObj.getLnkVoyage().trim() + direction;
		}
		data.put( "vesvoy", vesvoy);
		data.put( "dischargePort", lnkObj.getLnkDischargePort().trim());
		data.put( "destPort", lnkObj.getLnkDestPort().trim());
		data.put( "loadPort", lnkObj.getLnkLoadPort().trim());
		
		String shipperName = lnkObj.getLnkShipperOrgn50().trim();
		if ( lnkObj.getLnkShipQual40().trim().length() > 0) {
			shipperName = shipperName + " " + lnkObj.getLnkShipQual40().trim();
			if ( shipperName.length() > 80)
				shipperName = shipperName.substring( 0, 80);
		}
		data.put( "shipperId", StrUtil.padLeadingZero( lnkObj.getLnkShipperArol().trim(), 10));
		data.put( "shipperName", shipperName);
		
		String ConsigneeName = lnkObj.getLnkConsignOrgn50().trim();
		if ( lnkObj.getLnkConsignQual40().trim().length() > 0) {
			ConsigneeName = ConsigneeName + " " + lnkObj.getLnkConsignQual40().trim();
			if ( ConsigneeName.length() > 80)
				ConsigneeName = ConsigneeName.substring( 0, 80);
		}
		data.put( "consigneeId", StrUtil.padLeadingZero( lnkObj.getLnkConsigneeArol().trim(), 10));
		data.put( "consigneeName", ConsigneeName);
	}

	@Override
	protected Object textObjToXmlObj(Object textObj) throws TosException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Object xmlObjToTextObj(Object xmlObj) throws TosException {
		// TODO Auto-generated method stub
		return null;
	}

}
