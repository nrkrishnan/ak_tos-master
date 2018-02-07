/*
**************************************************************************************
*Srno   Date			AuthorName		Change Description
* A1    04/28/08        Glenn Raposo	1)Commented out “unique-key” attribute from the unit element. 
										2)Change the creation of the OB Carrier sub-elements 
										3)change to "owner" and "operator" attribute from the Equipment element
* A2	05/06/08  		Glenn Raposo    1)Reefer Temp attributes max and min removed
* A3    20/08/08        Glenn Raposo    Added MIX in port check condition for CATEGORY   
* A4    10/09/08		Steven Bauer	Modify POD to use discharge port if destination is blank.
* 									    Modify dischard port to handle client port codes.	
* A5    17/09/08    	Steven Bauer	Modified STIF for client owner/line operator	
* A6	20/10/08		Steven Bauer	Modified STIF with default remarks.		
* A7    28/10/08        Glenn Raposo    Commented out the T-State(INBOUND) 		
* A8    06/11/08		Steven Bauer	Change from Temp Required to Temp Recorded.	  
* A9	25/11/08		Steven Bauer	Addied CLI group code
* A10   09/12/08		Steven Bauer	Change to Category to set empties like Fulls.
* A11   09/12/08		Steven Bauer	Changed dest logic to use discharge port were available.
* A12	12/17/08		Steven Bauer	Changed Group coding to use discharge port and not dest.
* A13   12/23/08		Steven Bauer	Added eqFlex01 passed on group code
* A14   01/21/09		Steven Bauer	Using POD from the POD table and not the discharge in the file.
* 										Discharge in the file will be HON for NI containers.
* A15	02/03/09		Steven Bauer	Changed priority for DHX.
* A16	02/19/09		Steven Bauer	Parse equipment type into Type and Grade
* A17	03/06/09		Steven Bauer	Added workaround for bug, 61630.  Get a can not
*                                    	create UFV error if the uniqueId is supplied.
* A18	04/05/09		Steven Bauer	Added check digit lookup.
* A19	04/16/09		Steven Bauer	Setting NI Port OB carrier to BARGE
* A20   05/29/09		Steven Bauer	410 - Add Message to STIF for BL Nbr field “DO NOT EDIT – WAIT FOR NEWVES”
* A21   06/23/09		Steven Bauer	Only apply hazard for CLI group coding.
**************************************************************************************
*/
package com.matson.tos.messageHandler;

import java.util.List;
import java.math.BigInteger;
import java.math.BigDecimal;
import java.lang.Integer;

import org.apache.log4j.Logger;

import com.matson.cas.refdata.RefDataLookup;
import com.matson.cas.refdata.mapping.TosDestPodData;
import com.matson.text.bind.JATBContext;
import com.matson.text.bind.JATBException;
import com.matson.tos.exception.TosException;
import com.matson.tos.jatb.Stif;
import com.matson.tos.jaxb.snx.*;
import com.matson.tos.util.CheckDigit;
import com.matson.tos.util.EmailSender;
import com.matson.tos.util.EquipmentType;
import com.matson.tos.util.StrUtil;
import com.matson.tos.util.TerminalInfo;
import com.matson.tos.util.TosRefDataUtil;
import com.matson.tos.constants.*;
import com.matson.tos.util.EnvironmentProperty;
import com.matson.tos.util.StifClientOwnerMap;

public class StifMessageHandler extends AbstractMessageHandler {
	private static Logger logger = Logger.getLogger( StifMessageHandler.class); 
	private String emailAddr = TosRefDataUtil.getValue( "SUPPORT_EMAIL");
	private String _carrierId;
	private String _groupCoding;
	private String _titleLine;
	private static CheckDigit check = new CheckDigit();
	
	public StifMessageHandler( String s1, String s2, String s3, int dir) throws TosException {
		super( s1, s2, s3, dir);
	}
	@Override
	protected Object textObjToXmlObj(Object textObj)
			throws TosException {
		Stif stifObj = null;
		try {
			stifObj = (Stif)textObj;
			Snx snxObj = new Snx();
			
			List<TUnit> unitList = snxObj.getUnit();
			TUnit aUnit = new TUnit();
			unitList.add( aUnit);
			//1.5.M SNX Change Starts  
			TUnitContents uContents = new TUnitContents();
			aUnit.setContents(uContents);
			TUnitFlexFields unitFlex = new TUnitFlexFields();
			aUnit.setUnitFlex(unitFlex);
			
			// A6: Added Default remark to id Stif load
			TUnitHandling uHandling = new TUnitHandling();
			uHandling.setRemark("Stowplan Data");
			aUnit.setHandling(uHandling);
			aUnit.setSnxUpdateNote("Stowplan Data");
			
			// on UNIT level
			String id = StrUtil.trimQuotes( stifObj.getEquipNumber());
			id = check.getCheckDigitUnit(id);
			aUnit.setId( id);
			//A17
			//aUnit.setUniqueKey(stifObj.getEquipNumber());
			
			//A4 change Modify stif for client for HON to make it work
			//logger.debug( "Looking up dest:" + stifObj.getDestination());
			TosDestPodData refData = (TosDestPodData)RefDataLookup.queryById("com.matson.cas.refdata.mapping.TosDestPodData", stifObj.getDestination());
			if ( refData == null) {
				logger.error( "Can not find POD for DEST:" + stifObj.getDestination()+" looking up by Discharge Port");
				refData = (TosDestPodData)RefDataLookup.queryById("com.matson.cas.refdata.mapping.TosDestPodData", stifObj.getDischPort());
			}
			
			if(refData != null && refData.getDescription().equals(PortCode.PORT_HON_NAME)) {
				stifObj.setDestination(PortCode.PORT_HON);
				stifObj.setDischPort(PortCode.PORT_HON);
			}
			
			
			//A1 Change
			//aUnit.setUniqueKey( stifObj.getEquipNumber());
			//if ( stifObj.getSpecialStow1() != null && stifObj.getSpecialStow1().trim().length() > 0)
			//	aUnit.setSpecialStow( stifObj.getSpecialStow1());
			if ( stifObj.getKgWeight() > 0)
				uContents.setWeightKg( new BigDecimal( stifObj.getKgWeight()));
			if ( stifObj.getFullStatus().trim().length() > 0) {
				if ( stifObj.getFullStatus().trim().equalsIgnoreCase( "F"))
					aUnit.setFreightKind( "FCL");
				else if ( stifObj.getFullStatus().trim().equalsIgnoreCase( "E"))
					aUnit.setFreightKind( "MTY");
				else
					logger.error( "Unknown freight kind: " + stifObj.getFullStatus().trim());
			}
			// A20
			uContents.setBlNbr("DO NOT EDIT - WAIT FOR NEWVES");
			//if ( stifObj.getBooking() != null && stifObj.getBooking().trim().length() > 0) {
			//	uContents.setBlNbr( stifObj.getBooking());
			//}
			if ( stifObj.getUserField3() != null && stifObj.getUserField3().trim().length() > 0)
				//aUnit.setShipperName( stifObj.getUserField3());
				unitFlex.setUnitFlex6( stifObj.getUserField3());
			if ( stifObj.getUserField4() != null && stifObj.getUserField4().trim().length() > 0)
				unitFlex.setUnitFlex8( stifObj.getUserField4());
			//1.5.M changes End
			
			//if ( stifObj.getRemarks() != null && stifObj.getRemarks().trim().length() > 0)
			//	aUnit.setRemark( stifObj.getRemarks());
			// A1 Change
			if ( stifObj.getOwner() != null && stifObj.getOwner().trim().length() > 0){
				String lineOper = StifClientOwnerMap.getOperator(stifObj.getOwner());
				aUnit.setLine( lineOper);	
			}else{
				aUnit.setLine( "MAT"); // always MAT
			}
			//A7- aUnit.setTransitState( "INBOUND"); // default for all
			// A10, was the CATEGORY depends on the destination port and full status
			// Now same as full.
			String dest = stifObj.getDestination().trim();
			if (dest == null || dest.equals("")) {
				dest = stifObj.getDischPort().trim();
			}
			
			// A14, using pod table not disch field.
			String disch;
			if(refData != null) {
				disch = refData.getPod1();
			} else {
				disch = dest;
			}
			//String disch = stifObj.getDischPort().trim();;
			//Added vessel code constants 
			// Should I set it to dish port.
			
		//	logger.debug("Code type="+_groupCoding);
				if ( disch.equalsIgnoreCase(PortCode.PORT_HIL ) || disch.equalsIgnoreCase(PortCode.PORT_HON) ||
						disch.equalsIgnoreCase(PortCode.PORT_KHI) || disch.equalsIgnoreCase( PortCode.PORT_NAW) ||
						disch.equalsIgnoreCase(PortCode.PORT_KAH) || disch.equalsIgnoreCase( PortCode.PORT_MOL) ||
						disch.equalsIgnoreCase( PortCode.PORT_LNI) || disch.equalsIgnoreCase("MIX") ||
						disch.equalsIgnoreCase(PortCode.PORT_OPT)) { //A3
					aUnit.setCategory( TCategory.fromValue( "IMPORT"));
				} else if ( disch.equalsIgnoreCase( PortCode.PORT_SEA) || disch.equalsIgnoreCase( PortCode.PORT_OAK) ||
						disch.equalsIgnoreCase( PortCode.PORT_LAX) || disch.equalsIgnoreCase( PortCode.PORT_PDX)) {
					if("SUN".equalsIgnoreCase(_groupCoding ) ) {
						aUnit.setCategory( TCategory.fromValue( "TRANSSHIP"));
					} else {
						aUnit.setCategory( TCategory.fromValue( "THROUGH"));
					}
				} else {
					if("SUN".equalsIgnoreCase(_groupCoding ) ) {
						aUnit.setCategory( TCategory.fromValue( "THROUGH"));
					} else {
						aUnit.setCategory( TCategory.fromValue( "TRANSSHIP"));
					}
				}
			/*
			if ( stifObj.getFullStatus().trim().equalsIgnoreCase( "F")) {

			} else if ( stifObj.getFullStatus().trim().equalsIgnoreCase( "E")) {
				aUnit.setCategory( TCategory.fromValue( "STORAGE"));
			} else {
				logger.error( "Unknown freight kind: " + stifObj.getFullStatus().trim());
			}
			*/
			// on Equipment level
			List<TUnitEquipment> equipList = aUnit.getEquipment();
			TUnitEquipment aEquip = new TUnitEquipment();
			equipList.add( aEquip);
			
			// Parse Type into type and grade
			EquipmentType eqType = new EquipmentType(stifObj.getEquipTypeISO());		
			if ( eqType.getGrade() != null) {
				TPhysical physical = new TPhysical();
				physical.setGrade(eqType.getGrade());
				aEquip.setPhysical(physical);
			}
			if( eqType.getType() != null ) {
				aEquip.setType(eqType.getType());
			}
			
			//aEquip.setType( stifObj.getEquipTypeISO());
			
			Integer mmHeight;
			if ( stifObj.getCmHeight() != null && stifObj.getCmHeight() != 0) {
				mmHeight = new Integer( stifObj.getCmHeight().intValue() * 10);
				aEquip.setHeightMm( new BigInteger( mmHeight.toString()));
			}
			//A1 Change
			//aEquip.setOwner( stifObj.getOwner());
			if ( stifObj.getUserField1() != null && stifObj.getUserField1().trim().length() > 0)
				aEquip.setStrengthCode( stifObj.getUserField1());
			String eqid = StrUtil.trimQuotes( stifObj.getEquipNumber());
			eqid = check.getCheckDigitUnit(eqid);
			aEquip.setEqid(eqid );
			aEquip.setRole( "PRIMARY"); // default for all
			aEquip.setClazz( TEquipmentClass.fromValue( "CTR")); // always containers
			if ( stifObj.getOwner() != null && stifObj.getOwner().trim().length() > 0){
				String lineOper = StifClientOwnerMap.getOperator(stifObj.getOwner());
				String eqOwner =  StifClientOwnerMap.getOwner(stifObj.getOwner());
				aEquip.setOperator( lineOper);
				aEquip.setOwner(eqOwner);
			}else{
				aEquip.setOperator( "MAT"); // always MAT
				aEquip.setOwner( "MATU"); // always MAT
			}
			// A13
			if("CLI".equalsIgnoreCase(_groupCoding ) ) {
				aEquip.setEqFlex01("CLIENT");
			} else {
				aEquip.setEqFlex01("MAT");
			}
			
			//A1 Change Ends
			//aEquip.setDamages(value)
			if ( stifObj.getDamage() != null && ! stifObj.getDamage().equalsIgnoreCase( "N")) {
				TDamages dams = new TDamages();
				aEquip.setDamages( dams);
				List<TDamage> damList = dams.getDamage();
				TDamage aDam = new TDamage();
				aDam.setType( "UNKNOWN");
				aDam.setComponent( "UNKNOWN");
				aDam.setSeverity( "MINOR");
				damList.add( aDam);
			}
			// on Position level
			TPosition pos = new TPosition();
			aUnit.setPosition( pos);
			pos.setSlot( stifObj.getStowLocation());
			pos.setLocation( _carrierId);
			pos.setLocType( "VESSEL");
			
			// on Routing level
			TRouting routing = new TRouting();
			aUnit.setRouting( routing);
			routing.setPol( stifObj.getLoadPort());
			
			// POD depends on the TosDestPodData.java
			
				
			if(refData == null) {
				logger.error( "Can not find POD for Discharge Port:" + stifObj.getDestination());
				try {
					EmailSender.sendMail(emailAddr, emailAddr, "SN4 Error Message ", "Could not find POD for unit "+stifObj.getEquipNumber()+"\nDischarge port was "+stifObj.getDestination());
				} catch (Exception e) {
					logger.error("Could not email error",e);
				}
			} else {
		
				logger.debug( "The dest = " + stifObj.getDestination() + 
						"; Pod1 = " + refData.getPod1() + "; Pod2 = " + refData.getPod2());
				routing.setPod1( refData.getPod1());
				if ( refData.getPod2() != null)
					routing.setPod2( refData.getPod2());
			}
			// A11 Change to calculated dest.
			routing.setDestination( dest );
			routing.setOpl( stifObj.getLoadPort());
			boolean isReefer = "Y".equalsIgnoreCase(stifObj.getLiveReefer());
			// Changed from dest to .
			String group = getGroup(stifObj.getEquipNumber(),stifObj.getUserField3(),disch, stifObj.getCommodity(), stifObj.getEquipTypeISO(), isReefer);
			
			//String group = getGroup(stifObj.getEquipNumber(),stifObj.getUserField3(),dest, stifObj.getCommodity(), stifObj.getEquipTypeISO(), isReefer);
			if(group!=null) {
				logger.debug("Group Code="+group);
				routing.setGroup(group);
			}
				
			
			// on carrier level
			List<TRouting.Carrier> carrierList = routing.getCarrier();
			TRouting.Carrier aCarrier = new TRouting.Carrier();
			carrierList.add( aCarrier);
			aCarrier.setDirection( TDirection.IB);
			aCarrier.setId( _carrierId);
			aCarrier.setMode( "VESSEL");
			aCarrier.setQualifier( "DECLARED");
			aCarrier = new TRouting.Carrier();
			carrierList.add( aCarrier);
			aCarrier.setDirection( TDirection.IB);
			aCarrier.setId( _carrierId);
			aCarrier.setMode( "VESSEL");
			aCarrier.setQualifier( "ACTUAL");
			aCarrier = new TRouting.Carrier();
			carrierList.add( aCarrier);
			aCarrier.setDirection( TDirection.OB);
			aCarrier.setQualifier( "DECLARED");
			//A1 Change Starts
			// This should consider Category,
			// Give through the same carrier for inbound and outbound.
			//A14 changed from getDischPort to disch variable.
			if ( disch.equalsIgnoreCase( PortCode.PORT_HON)) {
				if(aUnit.getFreightKind() != null && aUnit.getFreightKind().equals("MTY")){
					aCarrier.setMode( "UNKNOWN");
				}else{
				aCarrier.setId( "GEN_TRUCK");
				aCarrier.setMode( "TRUCK");
				}
			// A19
			} else if(TerminalInfo.isHawaiiPort(disch)) {
				aCarrier.setId( "GEN_TRUCK");
				aCarrier.setMode( "TRUCK");
			} else {
				aCarrier.setId( "GEN_VESSEL");
				aCarrier.setMode( "VESSEL");
			}
			aCarrier = new TRouting.Carrier();
			carrierList.add( aCarrier);
			aCarrier.setDirection( TDirection.OB);
			aCarrier.setQualifier( "ACTUAL");
			if ( disch.equalsIgnoreCase( PortCode.PORT_HON)) {
				if(aUnit.getFreightKind() != null && aUnit.getFreightKind().equals("MTY")){
					aCarrier.setMode( "UNKNOWN");
				}else{
				aCarrier.setId( "GEN_TRUCK");
				aCarrier.setMode( "TRUCK");
				}
			// A19
			} else if(TerminalInfo.isHawaiiPort(disch)) {
				aCarrier.setId( "BARGE");
				aCarrier.setMode( "VESSEL");
			} else {
				aCarrier.setId( "GEN_VESSEL");
				aCarrier.setMode( "VESSEL");
			}
			//A1 Change Ends
			// on Hazard level
			
			// A21, only for CLI vessels.
			if(_groupCoding.equals("CLI")) {
				if ( stifObj.getImoCode() != null && stifObj.getImoCode().length() >0) {
					THazards hazards = new THazards();
					aUnit.setHazards( hazards);
					List<THazard> hazList = hazards.getHazard();
					THazard aHaz = new THazard();
					hazList.add( aHaz);
				
					aHaz.setImdg( stifObj.getImoCode());
				}
			}
			//A2 change on Reefer level
			if (stifObj.getReeferTemp() != null && stifObj.getReeferTemp().intValue() !=0)
			{   //TReeferRequirements reefer = new TReeferRequirements();
			    //aUnit.setReefer( reefer);
				//reefer.setTempReqdC( new BigDecimal( stifObj.getReeferTemp()).setScale(0, BigDecimal.ROUND_HALF_UP).divide(new BigDecimal(10)));
			
			
			}
			
			// on OOG level
			if ( (stifObj.getOvrHeight() != null && stifObj.getOvrHeight().intValue() != 0) ||
					( stifObj.getOvrWideRight() != null && stifObj.getOvrWideRight().intValue() != 0) ||
					( stifObj.getOvrWideLeft() != null && stifObj.getOvrWideLeft().intValue() != 0) ||
					( stifObj.getOvrLongFront() != null && stifObj.getOvrLongFront().intValue() != 0 ) ||
					( stifObj.getOvrLongBack() != null && stifObj.getOvrLongBack().intValue() != 0)) {
				TOog oog = new TOog();
				aUnit.setOog( oog);
				oog.setTopCm( new Long( stifObj.getOvrHeight()));
				oog.setRightCm( new Long( stifObj.getOvrWideRight()));
				oog.setLeftCm( new Long( stifObj.getOvrWideLeft()));
				oog.setFrontCm( new Long( stifObj.getOvrLongFront()));
				oog.setBackCm( new Long( stifObj.getOvrLongBack()));
			}
			return snxObj;
		} catch ( Exception ex) {
			throw new TosException( "Error: " + ex.toString());
		}
	}

	@Override
	protected Object xmlObjToTextObj(Object xmlObj)
			throws TosException {
		// TODO Auto-generated method stub
		throw new TosException( "This method has not been implemented.");
	}
	// Override the createTextObj for STIF file so that we can sort the 
	// fields by column title
	@Override 
	protected Object createTextObj() throws TosException {
		if ( _textStr == null)
			throw new TosException( "The text value string is null.");
		try {
			if ( jatbU == null) {
				JATBContext jc = JATBContext.newInstance( _textObjPackage, _textFmtFile );
				jatbU = jc.createUnmarshaller();
				jatbU.sortColumnByTitle( _titleLine);
			}
			_textObj = jatbU.unmarshal( _textStr); 
			return _textObj;
		} catch ( JATBException exJabx) {
			exJabx.printStackTrace();
			throw new TosException( "Error in creating text object for " + _textStr);
		} catch ( Exception ex) {
			throw new TosException( "Error: " + ex);
		}
	}

	public void setCarrierId( String id){
		_carrierId = id;
	}
	
	public void setGroupCoding( String id){
		_groupCoding = id;
	}
	public void setTitleLine( String aLine) {
		_titleLine = aLine;
	}
	
	/**
	 * Get the correct Group code
	 * List of codes supported
	 * DX,HZ,K,H,KH,GC,IC,20R,40R,20,24,40,45
	 * 
	 * @param shipper
	 * @param dest
	 * @param commodity
	 * @param type
	 * @param isReefer
	 * @return
	 */
	public String getGroup(String id,String shipper, String dest, String commodity, String type, boolean isReefer) {
		if(_groupCoding.equals("CLI")) {
			return null;
		}
		
		if(dest != null) {
			if(_groupCoding.equalsIgnoreCase("SUN") && 
					(dest.equalsIgnoreCase("GUM") || dest.equalsIgnoreCase("NGB") || dest.equalsIgnoreCase("SHA"))) {
				return "GC";	
			}
		}
		
		if(shipper != null ) {
			if(shipper.toUpperCase().contains("HORIZON")) {
				return "HZ";
			}
		}
		
		if(id.toLowerCase().startsWith("usau")) {
			return "US";
		}
		
		if(dest != null) {
			if(_groupCoding.equalsIgnoreCase("TUE")) {
				if(dest.equalsIgnoreCase("KAH")) {
					return "K";
				} else if(dest.equalsIgnoreCase("HIL")) {
					return "H";
				} else if(dest.equalsIgnoreCase("KHI")) {
					return "KH";
				}
			} else if(_groupCoding.equalsIgnoreCase("SAT")) {
				if(dest.equalsIgnoreCase("HIL")) {
					return "H";
				} else if(dest.equalsIgnoreCase("KHI")) {
					return "KH";
				}
			} else if(_groupCoding.equalsIgnoreCase("SUN")) {
				if(dest.equalsIgnoreCase("KAH")) {
					return "K";
				} else if(dest.equalsIgnoreCase("HIL")) {
					return "H";
				} else if(dest.equalsIgnoreCase("KHI")) {
					return "KH";
				} 
				
			}
		}
		
		if(shipper != null ) {
			if(shipper.toUpperCase().contains("DHX")) {
				return "DX";
			} 
		}
		
		if(commodity != null) {
			commodity = commodity.toUpperCase();
			if(commodity.contains("CREAM")  || commodity.contains("MILK") ) {
				return "IC";
			}
		}
		
		if(type != null  && type.length() >= 3) {
			type = type.substring(1,3);

			if(isReefer) {
				if(type.equals("20") || type.equals("24")) return "2R";	
				if(type.equals("40") || type.equals("45")) return "4R";	
			} else {
				if(type.equals("20") ) return "20";
				if(type.equals("24") ) return "24";
				if(type.equals("40") ) return "40";
				if(type.equals("45") ) return "45";
			} 
		}
		
		return null;
	
	}
}
