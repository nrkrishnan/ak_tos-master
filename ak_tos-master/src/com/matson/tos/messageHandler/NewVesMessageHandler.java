/*
*********************************************************************************************
* Srno   Date			AuthorName			Change Description
* A1     09/12/08       Glenn Raposo		Rounded OOG values & Change to Throws Exception
* A2     09/30/08  		Steven Bauer		Change Hazard Weight to the correct column
* A3     10/28/2008		Steven Bauer		Correct for shipper/consignee ids of one time shippers.
* A4     11/06/2008		Steven Bauer		Removed tstate to switch to production mode.
* 											Added back for supp handling, now blanked out in
* 											NewVessFileProcessor.
* A5	11/14/2008		Steven Bauer		Added Equipment flex fields
* A6    02/18/2009		Steven Bauer		Handle null shipper or consignee name.
* A7    02/19/2009		Steven Bauer		Equipment Grade
* A8    02/26/2009      Steven Bauer		Check for null on eqFlexFields.
* A9	04/05/2009		Steven Bauer		Added check digit lookup.
* A10   04/09/2009      Steven Bauer		Blank out select fields on New Ves
* A11   04/23/2009      Steven Bauer		Removed \t from remarks.
* A12   04/29/2009		Steven Bauer		Added offset for last free days.
* A13	09/08/2009		Steven Bauer		Removed offset for last free days.
* A14	09/17/2009		Steven Bauer		Added it back
* A15   10/01/2009      Glenn Raposo        Set Haz Number Type [UN/NA]
* A15	09/28/2009		Steven Bauer		Removed it again.
* A16   10/20/2009      Glenn Raposo        Added Haz notes
* A17   10/05/2011      Glenn Raposo        Removed Group code Blank with 2.1 Up
*********************************************************************************************
*/
package com.matson.tos.messageHandler;

import com.matson.cas.refdata.mapping.TosHoldPermData;
import com.matson.tos.dao.TosLookup;
import com.matson.tos.exception.TosException;
import com.matson.tos.jatb.NewVes;
import com.matson.tos.jaxb.snx.*;
import com.matson.tos.util.CalendarUtil;
import com.matson.tos.util.EquipmentType;
import com.matson.tos.util.StrUtil;
import com.matson.tos.util.TosRefDataUtil;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

public class NewVesMessageHandler extends AbstractMessageHandler {
	private static Logger logger = Logger.getLogger(NewVesMessageHandler.class);
	private static String pat1 = "MM/dd/yyyy";
	private boolean isNewVes = true;
	public static THazard genericHaz = new THazard();
	public static String snxHoldAcetsCode = "NEWVESS INCOMPLETE";

	static {
		genericHaz.setImdg("X");
		genericHaz.setProperName("Missing Hazard");
	}

	public NewVesMessageHandler(String xmlObjPackageName,
			String textObjPackageName, String fmtFile, int convertDir)
			throws TosException {
		super(xmlObjPackageName, textObjPackageName, fmtFile, convertDir);
		// TODO Auto-generated constructor stub
	}


	public boolean isNewVes() {
		return isNewVes;
	}


	public void setNewVes(boolean isNewVes) {
		this.isNewVes = isNewVes;
	}


	@Override
	protected Object textObjToXmlObj(Object textObj) throws TosException {
		//logger.debug("Is newVess "+isNewVes);
		Object retObj = null;
		TUnit aUnit = null;
		String temp;
		try {
			NewVes vesObj = (NewVes)textObj;
			String recType = StrUtil.trimQuotes( vesObj.getRecType());
			if ( recType.equalsIgnoreCase("unit")) {
				aUnit = new TUnit();
				aUnit.setSnxUpdateNote("");
				String id = StrUtil.trimQuotes( vesObj.getUnitId());
				aUnit.setId(id);

				//Commented out for Testing
				//aUnit.setUniqueKey( StrUtil.trimQuotes( vesObj.getUniqueKey()));
				if ( StrUtil.trimQuotes( vesObj.getCategory()).length() > 0)
					aUnit.setCategory( TCategory.fromValue( StrUtil.trimQuotes( vesObj.getCategory())));
				//A4, put back for supp handling
				if ( StrUtil.trimQuotes( vesObj.getTransitState()).length() > 0)
					aUnit.setTransitState( StrUtil.trimQuotes( vesObj.getTransitState()));
				if ( StrUtil.trimQuotes( vesObj.getFreightKind()).length() > 0)
					aUnit.setFreightKind( StrUtil.trimQuotes( vesObj.getFreightKind()));
				if ( StrUtil.trimQuotes( vesObj.getLine()).length() > 0)
					aUnit.setLine( StrUtil.trimQuotes( vesObj.getLine()));
				String haz = StrUtil.trimQuotes( vesObj.getHazardFlag());
				if (haz.equalsIgnoreCase("Y")) {
					THazards hazs =  new THazards();
					aUnit.setHazards( hazs);
					List<THazard> hazList = hazs.getHazard();
					hazList.add(genericHaz);
				}
				/*if(isNewVes){

				    addHoldToUnits(aUnit);
					logger.info("start aUnit --> : " + aUnit.getFlags() +"unit:"+aUnit.getId());
                }*/

				retObj = aUnit;
			} else if ( recType.equalsIgnoreCase("equipment")) {
				TUnitEquipment anEqpmt = new TUnitEquipment();
				if ( StrUtil.trimQuotes( vesObj.getEqid()).length() > 0) {
					String eqid = StrUtil.trimQuotes( vesObj.getEqid());
					anEqpmt.setEqid(eqid);
				}
				if ( StrUtil.trimQuotes( vesObj.getClazz()).length() > 0)
					anEqpmt.setClazz( TEquipmentClass.fromValue (StrUtil.trimQuotes( vesObj.getClazz())));
				if ( StrUtil.trimQuotes( vesObj.getTareKg()).trim().length() >0)
					anEqpmt.setTareKg( new BigDecimal( StrUtil.trimQuotes( vesObj.getTareKg())).setScale(2, BigDecimal.ROUND_HALF_UP));
				if ( StrUtil.trimQuotes( vesObj.getRole()).length() > 0)
					anEqpmt.setRole( StrUtil.trimQuotes( vesObj.getRole()));

				temp = StrUtil.trimQuotes( vesObj.getType());
				EquipmentType type = new EquipmentType(temp);
				if ( type.getGrade() != null) {
					TPhysical physical = new TPhysical();
					physical.setGrade(type.getGrade());
					anEqpmt.setPhysical(physical);
				}
				if( type.getType() != null ) {
					anEqpmt.setType(type.getType());
				}

				temp = StrUtil.trimQuotes( vesObj.getHeightMm()).trim();
				if ( temp.length() > 0) {
					if ( temp.indexOf(".") > 0)
						anEqpmt.setHeightMm( BigInteger.valueOf( Integer.parseInt( temp.substring(0, temp.indexOf(".")))));
					else
						anEqpmt.setHeightMm( BigInteger.valueOf( Integer.parseInt( temp)));
				}
				if ( StrUtil.trimQuotes( vesObj.getStrengthCode()).length() > 0)
					anEqpmt.setStrengthCode( StrUtil.trimQuotes( vesObj.getStrengthCode()));
				if ( StrUtil.trimQuotes( vesObj.getOwner()).length() > 0)
					anEqpmt.setOwner( StrUtil.trimQuotes( vesObj.getOwner()));
				if ( StrUtil.trimQuotes( vesObj.getOperator()).length() > 0)
					anEqpmt.setOperator( StrUtil.trimQuotes( vesObj.getOperator()));
				temp = StrUtil.trimQuotes( vesObj.getMaterial());
				if ( temp != null && temp.trim().equalsIgnoreCase( "ALUMINUM"))
					anEqpmt.setMaterial( TMaterial.ALUMINUM);
				else if (temp != null && temp.trim().equalsIgnoreCase( "STEEL"))
					anEqpmt.setMaterial( TMaterial.STEEL);
				else
					anEqpmt.setMaterial( TMaterial.UNKNOWN);

				// A5 Equipment flex fields
				if (vesObj.getEqFlex01() != null &&  StrUtil.trimQuotes( vesObj.getEqFlex01()).length() > 0) {
					//logger.debug("EqFlex01="+vesObj.getEqFlex01());
					anEqpmt.setEqFlex01(StrUtil.trimQuotes( vesObj.getEqFlex01()));
				}
				if (vesObj.getEqFlex02() != null &&  StrUtil.trimQuotes( vesObj.getEqFlex02()).length() > 0) {
					anEqpmt.setEqFlex02(StrUtil.trimQuotes( vesObj.getEqFlex02()));
				}
				if (vesObj.getEqFlex03() != null &&  StrUtil.trimQuotes( vesObj.getEqFlex03()).length() > 0) {
					anEqpmt.setEqFlex03(StrUtil.trimQuotes( vesObj.getEqFlex03()));
				}

				retObj = anEqpmt;
			} else if (recType.equalsIgnoreCase( "hold")) {
				TFlags flags = new TFlags();
				List<TFlags.Hold> holdList = flags.getHold();
				List<TFlags.Permission> permList = flags.getPermission();

				temp = StrUtil.trimQuotes( vesObj.getHoldId());
				if ( temp != null && temp.trim().length() != 0) {
					TFlags.Hold aHold = new TFlags.Hold();
					aHold.setId( temp);
					holdList.add( aHold);
				}
				temp = StrUtil.trimQuotes( vesObj.getPermissionId());
				if ( temp != null && temp.trim().length() != 0) {
					TFlags.Permission aPerm = new TFlags.Permission();
					aPerm.setId( temp);
					permList.add( aPerm);
				}
				retObj = flags;
			} else if (recType.equalsIgnoreCase( "damage")) {
				TDamage aDam = new TDamage();
				if ( StrUtil.trimQuotes( vesObj.getComponent()).length() > 0)
					aDam.setComponent( StrUtil.trimQuotes( vesObj.getComponent()));
				if ( StrUtil.trimQuotes( vesObj.getSeverity()).length() > 0)
					aDam.setSeverity( StrUtil.trimQuotes( vesObj.getSeverity()));
				if ( StrUtil.trimQuotes( vesObj.getDamageType()).length() > 0)
					aDam.setType( StrUtil.trimQuotes( vesObj.getDamageType()));
				retObj = aDam;
			} else if (recType.equalsIgnoreCase( "hazard")) {
				THazard aHaz = new THazard();
				if ( StrUtil.trimQuotes( vesObj.getImdg()).length() > 0)
					aHaz.setImdg( StrUtil.trimQuotes( vesObj.getImdg()));
				if ( StrUtil.trimQuotes( vesObj.getUn()).trim().length() > 0) {
					//aHaz.setUn( new Short( StrUtil.trimQuotes( vesObj.getUn())));
					   aHaz.setUn( StrUtil.trimQuotes( vesObj.getUn()));
				}//A15,A16
				if ( StrUtil.trimQuotes( vesObj.getRemark()).length() > 0) {
					String remark = StrUtil.trimQuotes( vesObj.getRemark());
				    remark.replaceAll("\t", "");
					aHaz.setNotes(remark);
				}
				if ( StrUtil.trimQuotes( vesObj.getNbrType()).trim().length() > 0) {
					aHaz.setHazNbrType(THazardNumberType.fromValue((StrUtil.trimQuotes(vesObj.getNbrType()))));
				}
				if ( StrUtil.trimQuotes( vesObj.getLtdQtyFlag()).length() > 0)
					aHaz.setLtdQtyFlag( StrUtil.trimQuotes( vesObj.getLtdQtyFlag()));
				if ( StrUtil.trimQuotes( vesObj.getPackageType()).length() > 0)
					aHaz.setPackageType( StrUtil.trimQuotes( vesObj.getPackageType()));
				if ( StrUtil.trimQuotes( vesObj.getInhalationZone()).length() > 0)
					aHaz.setInhalationZone( StrUtil.trimQuotes( vesObj.getInhalationZone()));
				if ( StrUtil.trimQuotes( vesObj.getExplosiveClass()).length() > 0)
					aHaz.setExplosiveClass( StrUtil.trimQuotes( vesObj.getExplosiveClass()));
				if ( StrUtil.trimQuotes( vesObj.getPageNumber()).length() > 0)
					aHaz.setPageNumber( StrUtil.trimQuotes( vesObj.getPageNumber()));
				if ( StrUtil.trimQuotes( vesObj.getFlashPoint()).trim().length() > 0)
					aHaz.setFlashPoint( new BigDecimal( StrUtil.trimQuotes( vesObj.getFlashPoint())).setScale(2, BigDecimal.ROUND_HALF_UP));
				if ( StrUtil.trimQuotes( vesObj.getTechName()).length() > 0)
					aHaz.setTechName( StrUtil.trimQuotes( vesObj.getTechName()));
				if ( StrUtil.trimQuotes( vesObj.getProperName()).length() > 0)
					aHaz.setProperName( StrUtil.trimQuotes( vesObj.getProperName()));
				if ( StrUtil.trimQuotes( vesObj.getEmsNbr()).length() > 0)
					aHaz.setEmsNbr( StrUtil.trimQuotes( vesObj.getEmsNbr()));
				if ( StrUtil.trimQuotes( vesObj.getMfag()).length() > 0)
					aHaz.setMfag( StrUtil.trimQuotes( vesObj.getMfag()));
				if (StrUtil.trimQuotes(vesObj.getPackingGroup()).length() > 0) {
					String packagingGrpMarks = vesObj.getPackingGroup();
					if (packagingGrpMarks != null && !packagingGrpMarks.isEmpty()) {
						if (packagingGrpMarks.equalsIgnoreCase("1"))
							packagingGrpMarks = "I";
						if (packagingGrpMarks.equalsIgnoreCase("2"))
							packagingGrpMarks = "II";
						if (packagingGrpMarks.equalsIgnoreCase("3"))
							packagingGrpMarks = "III";
						if (packagingGrpMarks.trim().equalsIgnoreCase("I") ||
								packagingGrpMarks.trim().equalsIgnoreCase("II") ||
								packagingGrpMarks.trim().equalsIgnoreCase("III")) {
							packagingGrpMarks = packagingGrpMarks.trim();
							aHaz.setPackingGroup(packagingGrpMarks);
						}
					}
				}
				if ( StrUtil.trimQuotes( vesObj.getHazIdUpper()).length() > 0)
					aHaz.setHazIdUpper( StrUtil.trimQuotes( vesObj.getHazIdUpper()));
				if ( StrUtil.trimQuotes( vesObj.getSubstanceLower()).length() > 0)
					aHaz.setSubstanceLower( StrUtil.trimQuotes( vesObj.getSubstanceLower()));
				// A2 Change to hazardWeight, was weight
				if ( StrUtil.trimQuotes( vesObj.getHazardWeightKg()).trim().length() > 0) {
					try {
						//logger.debug("HazWeight="+vesObj.getHazardWeightKg());
						aHaz.setWeightKg( new BigDecimal( StrUtil.trimQuotes( vesObj.getHazardWeightKg())).setScale(2, BigDecimal.ROUND_HALF_UP));

					} catch (Exception e ) {
						logger.error("Could not set weight",e);
					}
				}
			    if ( StrUtil.trimQuotes( vesObj.getPlannerRef()).length() > 0)
					aHaz.setPlannerRef( StrUtil.trimQuotes( vesObj.getPlannerRef()));
				temp = StrUtil.trimQuotes( vesObj.getQuantity()).trim();
				if ( temp.length() > 0) {
					if ( temp.indexOf(".") > 0)
						aHaz.setQuantity( BigInteger.valueOf( Integer.parseInt( temp.substring(0, temp.indexOf(".")))));
					else
						aHaz.setQuantity( BigInteger.valueOf( Integer.parseInt( temp)));
				}
				if ( StrUtil.trimQuotes( vesObj.getMoveMethod()).length() > 0)
					aHaz.setMoveMethod( StrUtil.trimQuotes( vesObj.getMoveMethod()));
				if ( StrUtil.trimQuotes( vesObj.getSecondaryImo1()).length() > 0)
					aHaz.setSecondaryImo1( StrUtil.trimQuotes( vesObj.getSecondaryImo1()));
				if ( StrUtil.trimQuotes( vesObj.getSecondaryImo2()).length() > 0)
					aHaz.setSecondaryImo2( StrUtil.trimQuotes( vesObj.getSecondaryImo2()));
				if ( StrUtil.trimQuotes( vesObj.getDeckRestrictions()).length() > 0)
					aHaz.setDeckRestrictions( StrUtil.trimQuotes( vesObj.getDeckRestrictions()));
				if ( StrUtil.trimQuotes( vesObj.getMarinePollutants()).length() > 0)
					aHaz.setMarinePollutants( StrUtil.trimQuotes( vesObj.getMarinePollutants()));
				if ( StrUtil.trimQuotes( vesObj.getDcLgRef()).length() > 0)
					aHaz.setDcLgRef( StrUtil.trimQuotes( vesObj.getDcLgRef()));
				if ( StrUtil.trimQuotes( vesObj.getEmergencyTelephone()).length() > 0)
					aHaz.setEmergencyTelephone( StrUtil.trimQuotes( vesObj.getEmergencyTelephone()));
				retObj = aHaz;
			} else if (recType.equalsIgnoreCase( "oog")) {
				TOog anOog = new TOog(); //A1
				if ( StrUtil.trimQuotes( vesObj.getBackCm()).trim().length() > 0){
					double backCm = Double.parseDouble(StrUtil.trimQuotes( vesObj.getBackCm()));
					anOog.setBackCm(Math.round(backCm));
				}
				if ( StrUtil.trimQuotes( vesObj.getFrontCm()).trim().length() > 0){
					double frontCm = Double.parseDouble( StrUtil.trimQuotes( vesObj.getFrontCm()));
					anOog.setFrontCm(Math.round(frontCm));
				}
				if ( StrUtil.trimQuotes( vesObj.getLeftCm()).trim().length() > 0){
					double leftCm= Double.parseDouble( StrUtil.trimQuotes( vesObj.getLeftCm()));
					anOog.setLeftCm(Math.round(leftCm));
				}
				if ( StrUtil.trimQuotes( vesObj.getRightCm()).trim().length() > 0){
					double rightCm = Double.parseDouble( StrUtil.trimQuotes( vesObj.getRightCm()));
					anOog.setRightCm(Math.round(rightCm));
				}
				if ( StrUtil.trimQuotes( vesObj.getTopCm()).trim().length() > 0){
					double topCm = Double.parseDouble( StrUtil.trimQuotes( vesObj.getTopCm()));
					anOog.setTopCm(Math.round(topCm));
				}
				retObj = anOog;
			} else if (recType.equalsIgnoreCase( "position")) {
				TPosition pos = new TPosition();
				if ( StrUtil.trimQuotes( vesObj.getCarrierId()).length() > 0)
					pos.setCarrierId( StrUtil.trimQuotes( vesObj.getCarrierId()));
				if ( StrUtil.trimQuotes( vesObj.getLocType()).length() > 0)
					pos.setLocType( StrUtil.trimQuotes( vesObj.getLocType()));
				if ( StrUtil.trimQuotes( vesObj.getLocation()).length() > 0)
					pos.setLocation( StrUtil.trimQuotes( vesObj.getLocation()));
				if ( StrUtil.trimQuotes( vesObj.getOrientation()).length() > 0)
					pos.setOrientation( StrUtil.trimQuotes( vesObj.getOrientation()));
				if ( StrUtil.trimQuotes( vesObj.getSlot()).length() > 0)
					pos.setSlot( StrUtil.trimQuotes( vesObj.getSlot()));

				retObj = pos;
			} else if (recType.equalsIgnoreCase( "reefer")) {
				TReeferRequirements ref = new TReeferRequirements();
				if ( StrUtil.trimQuotes( vesObj.getTempReqdC()).trim().length() > 0)
					ref.setTempReqdC( new BigDecimal( StrUtil.trimQuotes( vesObj.getTempReqdC())).setScale(2, BigDecimal.ROUND_HALF_UP));
				if ( StrUtil.trimQuotes( vesObj.getO2Pct()).trim().length() > 0)
					ref.setO2Pct( new BigDecimal( StrUtil.trimQuotes( vesObj.getO2Pct())).setScale(2, BigDecimal.ROUND_HALF_UP));
				if ( StrUtil.trimQuotes( vesObj.getCo2Pct()).trim().length() > 0)
					ref.setCo2Pct( new BigDecimal( StrUtil.trimQuotes( vesObj.getCo2Pct())).setScale(2, BigDecimal.ROUND_HALF_UP));
				if ( StrUtil.trimQuotes( vesObj.getHumidityPct()).trim().length() > 0)
					ref.setHumidityPct( new BigDecimal( StrUtil.trimQuotes( vesObj.getHumidityPct())).setScale(2, BigDecimal.ROUND_HALF_UP));
				if ( StrUtil.trimQuotes( vesObj.getVentRequiredValue()).trim().length() > 0)
					ref.setVentRequiredValue( new BigDecimal( StrUtil.trimQuotes( vesObj.getVentRequiredValue())).setScale(2, BigDecimal.ROUND_HALF_UP));
				if ( StrUtil.trimQuotes( vesObj.getVentRequiredUnit()).length() > 0)
					ref.setVentRequiredUnit( StrUtil.trimQuotes( vesObj.getVentRequiredUnit()));
				if ( StrUtil.trimQuotes( vesObj.getTempMinC()).trim().length() > 0)
					ref.setTempMinC( new BigDecimal( StrUtil.trimQuotes( vesObj.getTempMinC())).setScale(2, BigDecimal.ROUND_HALF_UP));
				if ( StrUtil.trimQuotes( vesObj.getTempMaxC()).trim().length() > 0)
					ref.setTempMaxC( new BigDecimal( StrUtil.trimQuotes( vesObj.getTempMaxC())).setScale(2, BigDecimal.ROUND_HALF_UP));
				if ( StrUtil.trimQuotes( vesObj.getTempDisplayUnit()).length() > 0)
					ref.setTempDisplayUnit( StrUtil.trimQuotes( vesObj.getTempDisplayUnit()));
				temp = StrUtil.trimQuotes( vesObj.getTimeLatestOnPower()).trim();
				if ( temp.length() > 10)
					ref.setTimeLatestOnPower( CalendarUtil.getXmlCalendar( temp, pat));
				else if ( temp.length() > 4)
					ref.setTimeLatestOnPower( CalendarUtil.getXmlCalendar( temp, pat1));
				temp = StrUtil.trimQuotes( vesObj.getTimeMonitor1()).trim();
				if ( temp.length() > 10)
					ref.setTimeMonitor1( CalendarUtil.getXmlCalendar( temp, pat));
				else if ( temp.length() > 4)
					ref.setTimeMonitor1( CalendarUtil.getXmlCalendar( temp, pat1));

				temp = StrUtil.trimQuotes( vesObj.getTimeMonitor2()).trim();
				if ( temp.length() > 10)
					ref.setTimeMonitor2( CalendarUtil.getXmlCalendar( temp, pat));
				else if ( temp.length() > 4)
					ref.setTimeMonitor2( CalendarUtil.getXmlCalendar( temp, pat1));
				temp = StrUtil.trimQuotes( vesObj.getTimeMonitor3()).trim();
				if ( temp.length() > 10)
					ref.setTimeMonitor3( CalendarUtil.getXmlCalendar( temp, pat));
				else if ( temp.length() > 4)
					ref.setTimeMonitor3( CalendarUtil.getXmlCalendar( temp, pat1));
				temp = StrUtil.trimQuotes( vesObj.getTimeMonitor4()).trim();
				if ( temp.length() > 10)
					ref.setTimeMonitor4( CalendarUtil.getXmlCalendar( temp, pat));
				else if ( temp.length() > 4)
					ref.setTimeMonitor4( CalendarUtil.getXmlCalendar( temp, pat1));
				retObj = ref;

			} else if (recType.equalsIgnoreCase( "routing")) {
				TRouting routing = new TRouting();
				if ( StrUtil.trimQuotes( vesObj.getGroup()).length() > 0) {
					routing.setGroup( StrUtil.trimQuotes( vesObj.getGroup()));
				}
				if ( StrUtil.trimQuotes( vesObj.getOpl()).length() > 0) {
					routing.setOpl( StrUtil.trimQuotes( vesObj.getOpl()));
				}
				if ( StrUtil.trimQuotes( vesObj.getPod1()).length() > 0) {
					routing.setPod1( StrUtil.trimQuotes( vesObj.getPod1()));
				}
				if ( StrUtil.trimQuotes( vesObj.getPod2()).length() > 0) {
					routing.setPod2( StrUtil.trimQuotes( vesObj.getPod2()));
				}
				if ( StrUtil.trimQuotes( vesObj.getPol()).length() > 0) {
					routing.setPol( StrUtil.trimQuotes( vesObj.getPol()));
				}
				if ( StrUtil.trimQuotes( vesObj.getOrigin()).length() > 0) {
					routing.setOrigin( StrUtil.trimQuotes( vesObj.getOrigin()));
				}
				if ( StrUtil.trimQuotes( vesObj.getDestination()).length() > 0) {
					routing.setDestination( StrUtil.trimQuotes( vesObj.getDestination()));
				}
				if ( StrUtil.trimQuotes( vesObj.getDesignatedTrucker()).length() > 0) {
					routing.setDesignatedTrucker( StrUtil.trimQuotes( vesObj.getDesignatedTrucker()));
				}
				if ( StrUtil.trimQuotes( vesObj.getCarrierService()).length() > 0) {
					routing.setCarrierService( StrUtil.trimQuotes( vesObj.getCarrierService()));
				}
				retObj = routing;
			} else if (recType.equalsIgnoreCase( "carrier")) {
				TRouting.Carrier aCarr = new TRouting.Carrier();
				if ( StrUtil.trimQuotes( vesObj.getDirection()).equalsIgnoreCase("IB"))
					aCarr.setDirection( TDirection.IB);
				else
					aCarr.setDirection( TDirection.OB);
				if ( StrUtil.trimQuotes( vesObj.getCarrierId1()).length() > 0)
					aCarr.setId( StrUtil.trimQuotes( vesObj.getCarrierId1()));
				if ( StrUtil.trimQuotes( vesObj.getMode()).length() > 0)
					aCarr.setMode( StrUtil.trimQuotes( vesObj.getMode()));
				if ( StrUtil.trimQuotes( vesObj.getQualifier()).length() > 0)
					aCarr.setQualifier( StrUtil.trimQuotes( vesObj.getQualifier()));
				retObj = aCarr;
			}// 1.5.M SNX Change Starts
			else if (recType.equalsIgnoreCase( "handling")){
				TUnitHandling uHandling = new TUnitHandling();
				if ( StrUtil.trimQuotes( vesObj.getRemark()).length() > 0) {
					String remark = StrUtil.trimQuotes( vesObj.getRemark());
				    remark.replaceAll("\t", "");
					uHandling.setRemark(remark);
				}
				if ( StrUtil.trimQuotes( vesObj.getDeckRequirement()).length() > 0)
					uHandling.setDeckRequirement( StrUtil.trimQuotes( vesObj.getDeckRequirement()));
				if ( StrUtil.trimQuotes( vesObj.getSpecialStow()).length() > 0)
					uHandling.setSpecialStow( StrUtil.trimQuotes( vesObj.getSpecialStow()));
				temp = StrUtil.trimQuotes( vesObj.getLastFreeDay()).trim();
				if ( temp.length() > 10)
					uHandling.setLastFreeDay( CalendarUtil.getXmlCalendar( temp, pat));
				else if ( temp.length() > 4)
					uHandling.setLastFreeDay( CalendarUtil.getXmlCalendar( temp, pat1));
				// A12 Add three hour offset to last free day.
				// A13, removed it.
				// A14
				// A15
				//if(temp.length() > 4) {
				//	long offset = TimeZone.getTimeZone("America/Los_Angeles").getOffset(System.currentTimeMillis()) - TimeZone.getTimeZone("HST").getOffset(System.currentTimeMillis());
				//	uHandling.getLastFreeDay().add(DatatypeFactory.newInstance().newDuration(offset) );
				//}
				retObj = uHandling;
			}else if(recType.equalsIgnoreCase( "contents")){
				TUnitContents uContents = new TUnitContents();
				//logger.debug("Get Contents");
				if ( StrUtil.trimQuotes( vesObj.getWeightKg()).trim().length() > 0) {
					uContents.setWeightKg( new BigDecimal( StrUtil.trimQuotes( vesObj.getWeightKg())).setScale(2, BigDecimal.ROUND_UP));
				}
				if ( StrUtil.trimQuotes( vesObj.getBlNumber()).length() > 0) {
					uContents.setBlNbr( StrUtil.trimQuotes( vesObj.getBlNumber()));
				} else if(isNewVes) {
					uContents.setBlNbr("");
				}
				if ( StrUtil.trimQuotes( vesObj.getCommodityId()).length() > 0) {
					uContents.setCommodityId( StrUtil.trimQuotes( vesObj.getCommodityId().trim()));
				}
				if ( StrUtil.trimQuotes( vesObj.getCommodityName()).length() > 0) {
					uContents.setCommodityName( StrUtil.trimQuotes( vesObj.getCommodityName().trim()));
				}
				if ( StrUtil.trimQuotes( vesObj.getConsigneeId()).length() > 0) {
					uContents.setConsigneeId( StrUtil.trimQuotes( vesObj.getConsigneeId()));
				}
				if ( StrUtil.trimQuotes( vesObj.getConsigneeName()).length() > 0) {
					uContents.setConsigneeName( StrUtil.trimQuotes( vesObj.getConsigneeName()));
				}
				if ( StrUtil.trimQuotes( vesObj.getShipperId()).length() > 0) {
					uContents.setShipperId( StrUtil.trimQuotes( vesObj.getShipperId()));
				}
				if ( StrUtil.trimQuotes( vesObj.getShipperName()).length() > 0) {
					uContents.setShipperName( StrUtil.trimQuotes( vesObj.getShipperName()));
				}

				TosLookup lookup = null;
				try {
					lookup = new TosLookup();
				// set id if only name is given for consignee and shipper
				if(uContents.getShipperId() == null && uContents.getShipperName() != null) {

					String shipperIdTmp = lookup.getShipper(uContents.getBlNbr(), uContents.getShipperName());
					logger.debug("Setting ShipperId "+shipperIdTmp );
					if (shipperIdTmp != null) {
						uContents.setShipperId(shipperIdTmp);
					} else {
						int idCode = lookup.getShipperId(uContents.getShipperName());
						shipperIdTmp = "C"+idCode;
						try {
							while(lookup.checkId(shipperIdTmp)) {
								idCode = lookup.incShipperId(idCode);
								shipperIdTmp = "C"+idCode;
							}
							uContents.setShipperId(shipperIdTmp);
						} catch (Exception e) {
							logger.error("Error while setting ShipperId "+shipperIdTmp, e);
						}
					}
					// Create new Shipper Id
				}
				if(uContents.getConsigneeId() == null && uContents.getConsigneeName() != null) {
					String consigneeIdTmp = lookup.getConsignee(uContents.getBlNbr(), uContents.getConsigneeName());
					logger.debug("Setting consigneeId "+consigneeIdTmp );
					if (consigneeIdTmp != null) {
						uContents.setConsigneeId(consigneeIdTmp);
					}else {
						int idCode = lookup.getShipperId(uContents.getConsigneeName());
						consigneeIdTmp = "C"+idCode;
						try {
							while(lookup.checkId(consigneeIdTmp)) {
								idCode = lookup.incShipperId(idCode);
								consigneeIdTmp = "C"+idCode;
							}
							uContents.setConsigneeId(consigneeIdTmp);
						} catch (Exception e) {
							logger.error("Error while setting ConsigneeId "+consigneeIdTmp, e);
						}
					}
				}

				// Correct one time shippers
				if(uContents.getConsigneeName() != null) {
					if(uContents.getConsigneeName().equals(uContents.getShipperName()) ) {
						uContents.setShipperId(uContents.getConsigneeId());
					}
				}

				} catch (Throwable e) {

					logger.error("Could not lookup Shipper",e);
				} finally {
					if(lookup != null) lookup.close();
				}
				//logger.debug("Finished");
				retObj = uContents;
			}else if(recType.equalsIgnoreCase( "seals")){
				TUnitSeals uSeals = new TUnitSeals();
				if ( StrUtil.trimQuotes( vesObj.getSeal1()).length() > 0)
					uSeals.setSeal1( StrUtil.trimQuotes( vesObj.getSeal1()));
				if ( StrUtil.trimQuotes( vesObj.getSeal2()).length() > 0)
					uSeals.setSeal2( StrUtil.trimQuotes( vesObj.getSeal2()));
				if ( StrUtil.trimQuotes( vesObj.getSeal3()).length() > 0)
					uSeals.setSeal3( StrUtil.trimQuotes( vesObj.getSeal3()));
				if ( StrUtil.trimQuotes( vesObj.getSeal4()).length() > 0)
					uSeals.setSeal4( StrUtil.trimQuotes( vesObj.getSeal4()));
				retObj = uSeals;
			}else if (recType.equalsIgnoreCase( "etc")){
				TUnitEtc uEtc = new TUnitEtc();
				if ( StrUtil.trimQuotes( vesObj.getDrayStatus()).length() > 0)
					uEtc.setDrayStatus( StrUtil.trimQuotes( vesObj.getDrayStatus()));
				if ( StrUtil.trimQuotes(vesObj.getRequiresPower()).length() > 0)
					uEtc.setRequiresPower( StrUtil.trimQuotes(vesObj.getRequiresPower()));
				retObj = uEtc;
			}else if (recType.equalsIgnoreCase( "ufv-flex")){
				TUfvFlexFields ufvFlex =  new TUfvFlexFields();
				if ( StrUtil.trimQuotes( vesObj.getUfvFlex1()).length() > 0)
					ufvFlex.setUfvFlex1( StrUtil.trimQuotes( vesObj.getUfvFlex1()));
				if ( StrUtil.trimQuotes( vesObj.getUfvFlex2()).length() > 0)
					ufvFlex.setUfvFlex5( StrUtil.trimQuotes( vesObj.getUfvFlex2()));// changed setUfvFlex2 to setUfvFlex5
				if ( StrUtil.trimQuotes( vesObj.getUfvFlex3()).length() > 0)
					ufvFlex.setUfvFlex3( StrUtil.trimQuotes( vesObj.getUfvFlex3()));
				if ( StrUtil.trimQuotes( vesObj.getUfvFlex4()).length() > 0)
					ufvFlex.setUfvFlex4( StrUtil.trimQuotes( vesObj.getUfvFlex4()));

				//Alaska changes - start
				//KFF or AMB
				if ( StrUtil.trimQuotes( vesObj.getUfvFlex7()).length() > 0)
					ufvFlex.setUfvFlex7( StrUtil.trimQuotes( vesObj.getUfvFlex7()));

				//Alaska changes - end

				temp = StrUtil.trimQuotes( vesObj.getUfvFlexDate1()).trim();
				if ( temp.length() > 10)
					ufvFlex.setUfvFlexDate1( CalendarUtil.getXmlCalendar( temp, pat));
				else if ( temp.length() > 4)
					ufvFlex.setUfvFlexDate1( CalendarUtil.getXmlCalendar( temp, pat1));
				temp = StrUtil.trimQuotes( vesObj.getUfvFlexDate2()).trim();
				if ( temp.length() > 10)
					ufvFlex.setUfvFlexDate2( CalendarUtil.getXmlCalendar( temp, pat));
				else if ( temp.length() > 4)
					ufvFlex.setUfvFlexDate2( CalendarUtil.getXmlCalendar( temp, pat1));
				temp = StrUtil.trimQuotes( vesObj.getUfvFlexDate3()).trim();
				if ( temp.length() > 10)
					ufvFlex.setUfvFlexDate3( CalendarUtil.getXmlCalendar( temp, pat));
				else if ( temp.length() > 4)
					ufvFlex.setUfvFlexDate3( CalendarUtil.getXmlCalendar( temp, pat1));
				temp = StrUtil.trimQuotes( vesObj.getUfvFlexDate4()).trim();
				if ( temp.length() > 10)
					ufvFlex.setUfvFlexDate4( CalendarUtil.getXmlCalendar( temp, pat));
				else if ( temp.length() > 4)
					ufvFlex.setUfvFlexDate4( CalendarUtil.getXmlCalendar( temp, pat1));
				temp = StrUtil.trimQuotes( vesObj.getUfvFlexDate5()).trim();
				if ( temp.length() > 10)
					ufvFlex.setUfvFlexDate5( CalendarUtil.getXmlCalendar( temp, pat));
				else if ( temp.length() > 4)
					ufvFlex.setUfvFlexDate5( CalendarUtil.getXmlCalendar( temp, pat1));
				temp = StrUtil.trimQuotes( vesObj.getUfvFlexDate6()).trim();
				if ( temp.length() > 10)
					ufvFlex.setUfvFlexDate6( CalendarUtil.getXmlCalendar( temp, pat));
				else if ( temp.length() > 4)
					ufvFlex.setUfvFlexDate6( CalendarUtil.getXmlCalendar( temp, pat1));
				temp = StrUtil.trimQuotes( vesObj.getUfvFlexDate7()).trim();
				if ( temp.length() > 10)
					ufvFlex.setUfvFlexDate7( CalendarUtil.getXmlCalendar( temp, pat));
				else if ( temp.length() > 4)
					ufvFlex.setUfvFlexDate7( CalendarUtil.getXmlCalendar( temp, pat1));
				temp = StrUtil.trimQuotes( vesObj.getUfvFlexDate8()).trim();
				if ( temp.length() > 10)
					ufvFlex.setUfvFlexDate8( CalendarUtil.getXmlCalendar( temp, pat));
				else if ( temp.length() > 4)
					ufvFlex.setUfvFlexDate8( CalendarUtil.getXmlCalendar( temp, pat1));
				retObj = ufvFlex;
			}else if (recType.equalsIgnoreCase( "unit-flex")){
				TUnitFlexFields unitFlex = new TUnitFlexFields();
				if ( StrUtil.trimQuotes( vesObj.getUnitFlex1()).length() > 0)
					unitFlex.setUnitFlex1( StrUtil.trimQuotes( vesObj.getUnitFlex1()));
				if ( StrUtil.trimQuotes( vesObj.getUnitFlex2()).length() > 0)
					unitFlex.setUnitFlex2( StrUtil.trimQuotes( vesObj.getUnitFlex2()));
				if ( StrUtil.trimQuotes( vesObj.getUnitFlex3()).length() > 0)
					unitFlex.setUnitFlex3( StrUtil.trimQuotes( vesObj.getUnitFlex3()));
				if ( StrUtil.trimQuotes( vesObj.getUnitFlex4()).length() > 0)
					unitFlex.setUnitFlex4( StrUtil.trimQuotes( vesObj.getUnitFlex4()));
				if ( StrUtil.trimQuotes( vesObj.getUnitFlex5()).length() > 0)
					unitFlex.setUnitFlex5( StrUtil.trimQuotes( vesObj.getUnitFlex5()));
				if ( StrUtil.trimQuotes( vesObj.getUnitFlex6()).length() > 0)
					unitFlex.setUnitFlex6( StrUtil.trimQuotes( vesObj.getUnitFlex6()));
				if ( StrUtil.trimQuotes( vesObj.getUnitFlex7()).length() > 0)
					unitFlex.setUnitFlex7( StrUtil.trimQuotes( vesObj.getUnitFlex7()));
				if ( StrUtil.trimQuotes( vesObj.getUnitFlex8()).length() > 0)
					unitFlex.setUnitFlex8( StrUtil.trimQuotes( vesObj.getUnitFlex8()));
				if ( StrUtil.trimQuotes( vesObj.getUnitFlex9()).length() > 0)
					unitFlex.setUnitFlex9( StrUtil.trimQuotes( vesObj.getUnitFlex9()));
				if ( StrUtil.trimQuotes( vesObj.getUnitFlex10()).length() > 0)
					unitFlex.setUnitFlex10( StrUtil.trimQuotes( vesObj.getUnitFlex10()));
				//logger.debug("FlexField12="+vesObj.getUnitFlex12()+",");
				if ( StrUtil.trimQuotes( vesObj.getUnitFlex12()).length() > 0)
					unitFlex.setUnitFlex12( StrUtil.trimQuotes( vesObj.getUnitFlex12()));

				retObj = unitFlex;
			}else if (recType.equalsIgnoreCase( "booking")){
				TBkg aBooking =  new TBkg();
				if ( StrUtil.trimQuotes( vesObj.getDepartureOrderNbr()).length() > 0) {
					aBooking.setId( StrUtil.trimQuotes( vesObj.getDepartureOrderNbr()));
				} else if(isNewVes) {
					aBooking.setId("");
				}
				retObj = aBooking;
			}else if(recType.equalsIgnoreCase( "timestamps")){
				TUfvTimestamps uTimestamps = new TUfvTimestamps();
				temp = StrUtil.trimQuotes( vesObj.getTimeIn()).trim();
				if ( temp.length() > 10)
					uTimestamps.setTimeIn( CalendarUtil.getXmlCalendar( temp, pat));
				else if ( temp.length() > 4)
					uTimestamps.setTimeIn( CalendarUtil.getXmlCalendar( temp, pat1));
				temp = StrUtil.trimQuotes( vesObj.getTimeOut()).trim();
				if ( temp.length() > 10)
					uTimestamps.setTimeOut( CalendarUtil.getXmlCalendar( temp, pat));
				else if ( temp.length() > 4)
					uTimestamps.setTimeOut( CalendarUtil.getXmlCalendar( temp, pat1));
				temp = StrUtil.trimQuotes( vesObj.getTimeLoad()).trim();
				if ( temp.length() > 10)
					uTimestamps.setTimeLoad( CalendarUtil.getXmlCalendar( temp, pat));
				else if ( temp.length() > 4)
					uTimestamps.setTimeLoad( CalendarUtil.getXmlCalendar( temp, pat1));
				retObj = uTimestamps;
			}//1.5.M Changes End
			else if(recType.equalsIgnoreCase( "vesselVisit")) {
				TVesselVisit vVisit = new TVesselVisit();
				temp = StrUtil.trimQuotes( vesObj.getUnitId()).trim();
				vVisit.setId(temp);
				temp = StrUtil.trimQuotes( vesObj.getUfvFlexDate2()).trim();
				if ( temp.length() > 10)
					vVisit.setTimeFirstAvailability( CalendarUtil.getXmlCalendar( temp, pat));
				else if ( temp.length() > 4)
					vVisit.setTimeFirstAvailability( CalendarUtil.getXmlCalendar( temp, pat1));
				retObj = vVisit;
			}
			else {
				logger.debug( " Wrong rec type in Unit: " + recType);
			}
		} catch ( Exception ex) {
			logger.error( "Exception: ", ex);
			ex.printStackTrace(); //A1
			throw new TosException(ex.toString());
		}
		return retObj;
	}

/**
	private TUnit  addHoldToUnits(TUnit unit) {
		try {

			logger.info("start addHoldToUnits : " + unit.getId() +"snx hold Acets code -->"+snxHoldAcetsCode);

			TFlags flags = new TFlags();
			List<TFlags.Hold> holdList = flags.getHold();
			TosHoldPermData holdObj = TosRefDataUtil.getHoldPermObj(snxHoldAcetsCode);
			if ( holdObj == null) {
				logger.debug( "Can not apply hold: Acets code " + snxHoldAcetsCode);
				return null;
			}
			TFlags.Hold aHold = new TFlags.Hold();
            if(holdObj.getSn4Id()==null){
                logger.debug( "Can not  apply  hold for Sn4Id: " + holdObj.getSn4Id());
                return  null;
            }
			aHold.setId(holdObj.getSn4Id());
			holdList.add( aHold);

			if(unit.getFlags()!=null) {
				logger.info("check if  hold exists : " + unit.getFlags().getHold().contains(holdObj.getSn4Id())
						+ "unit:"+unit.getId() +"holdObj.getSn4Id()"+holdObj.getSn4Id() +"list size :"+ unit.getFlags().getHold().size());
			}

			if (unit.getFlags()==null || (unit.getFlags()!=null && !unit.getFlags().getHold().contains(holdObj.getSn4Id()))) {

				logger.info("inside  addHoldToUnits : " + unit.getId() +"unit.getSnxUpdateNote()"+unit.getSnxUpdateNote());
				unit.setFlags(flags);
				unit.setRemarks(unit.getRemarks()==null?holdObj.getSn4Id(): unit.getRemarks() + holdObj.getSn4Id());
			}

			logger.info("end addHoldToUnits : ");

		} catch (Exception e) {
			logger.error("Could not add HoldToUnits", e);
		}
		return unit;
	}
**/
	@Override
	protected Object xmlObjToTextObj(Object xmlObj) throws TosException {
		// TODO Auto-generated method stub
		return null;
	}

}
