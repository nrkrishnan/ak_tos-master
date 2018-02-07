/*
 *  Srno    Date       Author          Change Description 
 *  A1      06/10/08   Glenn Raposo    SNX Update 1.6.2 Object changed from Boolean to String   
 *  A2		02/23/09   Steven Bauer	   Prevent null pointer exceptions. 
 *  A3		03/18/09   Steven Bauer	   Prevent null pointer exceptions.
 *  A4		03/19/09   Steven Bauer	   Change hazard to trigger on either UN num or hazard class.
 *  A5		03/20/09   Steven Bauer	   Added oog fields.
 *  A6	    03/25/09   Steven Bauer	   Trimmed ClientRefNo to 16 characters.
 *  A7		04/15/09   Steven Bauer	   Added Default F40 if no lines are passed.
 *  A8		04/21/09   Steven Bauer	   Added Mapping for HIL/KHI when Acets does not map correctly
 *  A9      04/23/09   Steven Bauer    Changed default from MTY to FCL
 *  A10     04/28/09   Steven Bauer	   Filter IMDG.
 *  A11		06/24/09   Steven Bauer	   Filter IMDG for multiple Xs
 *  A12     10/07/09   Glenn Raposo    Haz item UN/NA Change  
 *  A13     07/20/10   Glenn Raposo    Adding WA Temp as 100C
 *  A14     01/03/10   Glenn Raposo    Added Code to handle BOB lineOperator updates to BookingNbr 
 *  A15     05/12/10   Glenn Raposo    TT#12467 - Added Open-Top Container as ISO group.
 *  A16     07/19/11   Glenn Raposo	   Set V28 to V40
 *  A17     10/06/11   Glenn Raposo    SNX Update 2.1 Commented out Bkg.setVesvoy    
 *  A18		06/20/13   KRAJENDRAN		Set equipment type in <item> tag
 *  A19		09/13/13   KRAJENDRAN		Removed: Setting equipmentType in <item> tag
 *  A20	    09/13/13   KRAJENDRAN		Set ISO group as TN for all T* equip types
 *  A21	    01/06/13   RIYER 		   Set Item Commodity id and name to SIT and STOP IN TRANSIT for SIT bookings.
 *  TOSAK-165   05/19/2017  kramachandran	For Empty booking, make the commodity as empty, rather "--"
 */
package com.matson.tos.messageHandler;

import com.matson.tos.dao.TosLookup;
import com.matson.tos.exception.TosException;
import com.matson.tos.jatb.Abob;
import com.matson.tos.jaxb.snx.*;
import com.matson.tos.util.*;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AbobMessageHandler extends AbstractMessageHandler {
	private static final String MULTISTOP_SIT = "MULTISTOP SIT";
	private static Logger logger = Logger.getLogger(AbobMessageHandler.class);
	private static String emailAddr = TosRefDataUtil.getValue( "SUPPORT_EMAIL");
	private boolean linesIncluded = true;


	//A14
	public static final String CLASS_LOC = "database";
	public static final String CLASS_NAME = "GvyBookingLineOpProc";

	public static final String ANK = "ANK";
	public static final String KDK = "KDK";
	public static final String DUT = "DUT";
	public static final String KQA = "KQA";
	public static final String BARGE = "BARGE";
	public static final String NONE = "NONE";
	public static final String CLASSIFICATION = "CLASSIFICATION";
	public static final String CURRENT_FACILITY = "CURRENT_FACILITY";
	public static final String NEXT_FACILITY = "NEXT_FACILITY";



	public AbobMessageHandler(String xmlObjPackageName,
			String textObjPackageName, String fmtFile, int convertDir)
			throws TosException {
		super(xmlObjPackageName, textObjPackageName, fmtFile, convertDir);

	}


	public boolean isLinesIncluded() {
		return linesIncluded;
	}


	public void setLinesIncluded(boolean linesIncluded) {
		this.linesIncluded = linesIncluded;
	}


	@Override
	protected Object textObjToXmlObj(Object textObj) throws TosException {
		Abob bobObj = (Abob)textObj;
		Snx snxObj = new Snx();
		String lineOperator = null;
		String bookingNbr = null;

		snxObj.setUserId( "ACETS_BOB");
		// booking
		List<TBooking> bookingList = snxObj.getBooking();
		TBooking aBooking = new TBooking();
		bookingList.add( aBooking);
		bookingNbr = bobObj.getBookingNbr().trim();
		if ( bobObj.getPrimaryCarrier().trim().length() == 0){
			lineOperator = "MAT";
		}else{
			lineOperator = bobObj.getPrimaryCarrier().trim();
		}
		aBooking.setLine(lineOperator);
		aBooking.setNbr(bookingNbr);
		String carrierId = null;
		String dirSeq = bobObj.getDirSeq().trim().toUpperCase();
		if ( dirSeq.equals("N") || dirSeq.equals("S") || dirSeq.equals("E") || dirSeq.equals("W"))
			carrierId = bobObj.getVessel().trim() + bobObj.getVoyageNbr().trim();
		else {
			carrierId = bobObj.getVessel().trim() + bobObj.getVoyageNbr().trim()+dirSeq;
		}

	      TosLookup lookup = null; //A14
		  try{
			lookup = new TosLookup();
			//Check if BKG exist in N4 with Different LineOperator
		    boolean hasBkg = lookup.hasDiffBkgLineOperator(lineOperator,carrierId, bookingNbr);
		    if(hasBkg){
			 //Handle LineOperator update by posting Groovy Injection
			 postBobGvyInjXml(bookingNbr,lineOperator,carrierId);
		    }
		   }catch(Exception e){
			e.printStackTrace();
		   }finally {
			 if(lookup != null) lookup.close();
		   }//A14 Ends

		//aBooking.setNotes( bobObj.getCommodity().trim() + ": " + bobObj.getComments().trim());
		//kramachandran setNotes --> setStuffingLocation for ALASKA
		String stuffingLocation = bobObj.getCommodity().trim() + ": " + bobObj.getComments().trim();
		if (!stuffingLocation.isEmpty() && stuffingLocation.length() > 40) {
			stuffingLocation = stuffingLocation.substring(0, 39);
		} else if (!stuffingLocation.isEmpty() && stuffingLocation.length() <= 40) {
			//do nothing, the length is safe to pass to N4
		}
		aBooking.setStuffingLocation(stuffingLocation);
		aBooking.setClientRefNo( bobObj.getShipmentRefNbr().trim());
		if(aBooking.getClientRefNo() != null && aBooking.getClientRefNo().length() > 16) {
			aBooking.setClientRefNo(aBooking.getClientRefNo().substring(0, 16));
		}
		aBooking.setConsigneeId( StrUtil.padLeadingZero( bobObj.getConsigneeArol().trim(), 10));
		String consigneeName = bobObj.getConsignOrgn50().trim();
		if ( bobObj.getConsigneeQual40().trim().length() > 0) {
			consigneeName = consigneeName + " " + bobObj.getConsigneeQual40().trim();
			if ( consigneeName.length() > 80)
				consigneeName = consigneeName.substring( 0, 80);
		}
		aBooking.setConsigneeName( consigneeName);
		String dest = bobObj.getBlDestPort().trim();
		aBooking.setDestination(dest );
		if ( bobObj.getSit().trim().equalsIgnoreCase("Y"))
			aBooking.setDrayOff( "OFFSITE");
		if ( bobObj.getLoadType().trim().equalsIgnoreCase("E"))
			aBooking.setEqStatus( "MTY");
		else
			aBooking.setEqStatus( "FCL");
		aBooking.setOrigin( bobObj.getLoadPort().trim());
		// A8 if POD == HON && DEST == HIL or KHI use dest as POD.
		String disch = bobObj.getDischargePort().trim();
		if( "HON".equals(disch) && ( "HIL".equals(dest) || "KHI".equals(dest) ) ) {
			disch = dest;
		}
		aBooking.setPod1(disch );
		aBooking.setPol( bobObj.getLoadPort().trim());
		aBooking.setPreventTypeSubst("false");
		aBooking.setShipperId( StrUtil.padLeadingZero( bobObj.getShipperArol().trim(), 10));
		String shipperName = bobObj.getShipperOrgn50().trim();
		if ( bobObj.getShipQual40().trim().length() > 0) {
			shipperName = shipperName + " " + bobObj.getShipQual40().trim();
			if ( shipperName.length() > 80)
				shipperName = shipperName.substring( 0, 80);
		}
		aBooking.setShipperName( shipperName);
		// Carrier in Booking
		TCarrier aCarr = new TCarrier();
		aBooking.setCarrier( aCarr);
		aCarr.setDirection( TDirection.OB);
		aCarr.setQualifier( "DECLARED");
		aCarr.setMode( "VESSEL");
		//Specific Changes for Alaska Barges at DUT
				String carrier = bobObj.getVessel().trim();
				String voyageNbr = bobObj.getVoyageNbr().trim();
				String nextFacility = bobObj.getLoadPort().trim();
				String alaskaCarrierId = "";
				try {
					alaskaCarrierId = setCarrierId(carrierId, bobObj.getLoadPort().trim(), carrier, voyageNbr, nextFacility);
				} catch (Exception e) {
					String errorMessage = "Exception is\n" + e.toString();
					errorMessage += "\nXML Build \n" + aBooking.toString();
					errorMessage += "\nTCarrier\n" + aCarr.toString();
					EmailSender.sendMail(emailAddr, emailAddr, "ABOB Message - Unable to process the carrier details -" + bobObj.getBookingNbr(), errorMessage);
				}
				aCarr.setId(alaskaCarrierId);//A11
				//D031721
		String pol=bobObj.getLoadPort().trim();

		logger.info("pol"+pol);

		if(!(pol.equalsIgnoreCase("ANK")||pol.equalsIgnoreCase("KDK")||pol.equalsIgnoreCase("DUT") || pol.equalsIgnoreCase("KQA") || pol.equalsIgnoreCase("AKU")))
		{
			logger.info("inside send mail");
			emailAddr = emailAddr == null ? TosRefDataUtil.getValue( "SUPPORT_EMAIL") : emailAddr;
			EmailSender.sendMail(emailAddr,emailAddr,"ABOB Message - Unable to process Facility- "+pol+" unknown","Booking with invalid Port of Loading - "+bobObj.getBookingNbr());
		}
		aCarr.setFacility(pol);



		if (KQA.equalsIgnoreCase(pol) ) {
			//set inCarrierId as carrierId

			logger.debug("inside setting KQA facility ");

			aCarr.setFacility(DUT);
		}

		//D031721

		boolean oog = false;
		if(bobObj.getOog() != null && bobObj.getOog().equalsIgnoreCase("Y")) {
			oog = true;
		}
//		logger.debug("bob="+bobObj.getBookingNbr()+" oog="+bobObj.getOog()+" oogBoolean="+oog);

		if(linesIncluded) {
		//Items in Booking
			TBooking.Items items = new TBooking.Items();
			aBooking.setItems( items);
			List<TBooking.Items.Item> itemList = items.getItem();
			// for Planning 1
			TBooking.Items.Item anItem = null; //A13
			if("WA".equals(bobObj.getFerqTemp1().trim())){
				bobObj.setFerqTempUnit1("F");
				bobObj.setFerqTemp1("100");
			}
			//bobObj.getCommodity().trim()
			logger.debug( "commodity ID and Flag for booking :: " + bobObj.getSitTit().trim()+"::"+bobObj.getSit().trim());
			anItem = populateItem( bobObj.getFerqPlanning1(),
					bobObj.getFerqTempUnit1().trim(),
					bobObj.getFerqTemp1().trim(), bobObj.getReeferTemp1().trim(), bobObj.getFerqVentSet1().trim(), oog,bobObj.getSit().trim());
			if ( anItem != null){
				itemList.add(anItem);
				if ((anItem != null) && (anItem.getRemarks() != null)) aBooking.setNotes(anItem.getRemarks());/* Alaska Changes, The Item setRemarks is mapped to Booking setNotes */
				if ((anItem != null) && (anItem.getRemarks() == null)) aBooking.setNotes("");
			}
			// for Planning 2
			if("WA".equals(bobObj.getFerqTemp2().trim())){ //A13
				bobObj.setFerqTempUnit2("F");
				bobObj.setFerqTemp2("100");
			}
			anItem = populateItem( bobObj.getFerqPlanning2(),
					bobObj.getFerqTempUnit2().trim(),
					bobObj.getFerqTemp2().trim(), bobObj.getReeferTemp2().trim(), bobObj.getFerqVentSet2().trim(), oog,bobObj.getSit().trim());
			if (anItem != null) {
				itemList.add(anItem);
				if ((anItem != null) && (anItem.getRemarks() != null)) aBooking.setNotes(anItem.getRemarks());/* Alaska Changes, The Item setRemarks is mapped to Booking setNotes */
			}
			//for Planning 3
			if("WA".equals(bobObj.getFerqTemp3().trim())){ //A13
				bobObj.setFerqTempUnit3("F");
				bobObj.setFerqTemp3("100");
			}
			anItem = populateItem( bobObj.getFerqPlanning3(),
					bobObj.getFerqTempUnit3().trim(),
					bobObj.getFerqTemp3().trim(), bobObj.getReeferTemp3().trim(), bobObj.getFerqVentSet3().trim(), oog,bobObj.getSit().trim());
			if ( anItem != null){
				itemList.add(anItem);
				if ((anItem != null) && (anItem.getRemarks() != null)) aBooking.setNotes(anItem.getRemarks());/* Alaska Changes, The Item setRemarks is mapped to Booking setNotes */
			}
			//for Planning 4
			if("WA".equals(bobObj.getFerqTemp4().trim())){ //A13
				bobObj.setFerqTempUnit4("F");
				bobObj.setFerqTemp4("100");
			}
			anItem = populateItem( bobObj.getFerqPlanning4(),
					bobObj.getFerqTempUnit4().trim(),
					bobObj.getFerqTemp4().trim(),bobObj.getReeferTemp4().trim(), bobObj.getFerqVentSet4().trim(), oog,bobObj.getSit().trim());
			if ( anItem != null){
				itemList.add(anItem);
				if ((anItem != null) && (anItem.getRemarks() != null)) aBooking.setNotes(anItem.getRemarks());/* Alaska Changes, The Item setRemarks is mapped to Booking setNotes */
			}
			//for Planning 5
			if("WA".equals(bobObj.getFerqTemp5().trim())){ //A13
				bobObj.setFerqTempUnit5("F");
				bobObj.setFerqTemp5("100");
			}
			anItem = populateItem( bobObj.getFerqPlanning5(),
					bobObj.getFerqTempUnit5().trim(),
					bobObj.getFerqTemp5().trim(), bobObj.getReeferTemp5().trim(), bobObj.getFerqVentSet5().trim(), oog,bobObj.getSit().trim());
			if ( anItem != null)
				itemList.add( anItem);
			if ((anItem != null) && (anItem.getRemarks() != null)) aBooking.setNotes(anItem.getRemarks());/* Alaska Changes, The Item setRemarks is mapped to Booking setNotes */
				/*the conditionn is always false, as we're adding an elemnt in the step above, we should remove the block below*/
				if(itemList.size() == 0) {
				anItem = populateItem("F0040","","","","",false,bobObj.getSit().trim());
				if ( anItem != null) {
					itemList.add(anItem);
					if ((anItem != null) && (anItem.getRemarks() != null)) aBooking.setNotes(anItem.getRemarks()); /* Alaska Changes, The Item setRemarks is mapped to Booking setNotes */
				}
			}
		}

		//Hazard
		//String hazInd = bobObj.getHazardousInd();
		Boolean isHazardRequiredForBob = getTosRefDataAsBoolean("ABOB_HAZARD_SEG_REQ");
		if (bobObj.getBfrtHzdClazz().trim().length() != 0 && isHazardRequiredForBob) {
			THazards hazs = new THazards();
			aBooking.setHazards( hazs);
			List<THazard> hazList = hazs.getHazard();
			THazard aHaz = new THazard();
			hazList.add( aHaz);
			String imdg = bobObj.getBfrtHzdClazz().trim();
			// A10
			if(imdg.indexOf(" ") != -1) {
				imdg = imdg.substring(0, imdg.indexOf(" "));
			}
			// A11
			if(imdg.matches("^X+$") || imdg.matches("^x+$")) {
				imdg = "X";
			}


			aHaz.setImdg(imdg);
			if ( bobObj.getBfrtUnNa().trim().length() > 0) {
				try {
					//aHaz.setUn( new Short( bobObj.getBfrtUnNa().trim()));
					aHaz.setUn(bobObj.getBfrtUnNa().trim());
				} catch (Exception e) {
					logger.warn("Could not convert un number "+bobObj.getBfrtUnNa()+" for booking.");
				}
			}
			//A12 Starts
			String nbrType = "UN";
			try{
			 if(bobObj.getCommodity().trim().length() > 0){
				String [] cmdyArr = bobObj.getCommodity().trim().split(" ");
		         for(int i = 0; i <cmdyArr.length ; i++){
		        	 if (cmdyArr[i].equals("NA") || cmdyArr[i].equals("NA#")){
		        		 nbrType = "NA";
		        	 }
		         }
		      }
			}catch(Exception e){
				logger.warn("Could not get Nbr Type "+bobObj.getCommodity()+" for HazItem.");
			}
			aHaz.setHazNbrType(THazardNumberType.fromValue(nbrType));
			//A12 Ends

			if ( bobObj.getBertFlashPt().trim().length() > 0) {
				try {
					aHaz.setFlashPoint( new BigDecimal(bobObj.getBertFlashPt().trim()));
				} catch (Exception e) {
					logger.warn("Could not convert flash point "+bobObj.getBertFlashPt()+" for booking.");
				}
			}
			String packagingGroup = getPackingGroup(bobObj);
			if(packagingGroup!=null && !packagingGroup.isEmpty() && packagingGroup != "0")
				aHaz.setPackingGroup( packagingGroup);
			aHaz.setEmergencyTelephone( bobObj.getBfrt24HrEmergency().trim());
		}
		//Alaska changes
		String priorityStow = bobObj.getPriorityStow();
		priorityStow = priorityStow  != null ? priorityStow.trim() : "";
		aBooking.setStowBlock(priorityStow);
		String vgmRequired = bobObj.getVgmRequired();
		vgmRequired = vgmRequired != null ? vgmRequired.trim() : "";
		if ("Y".equalsIgnoreCase(vgmRequired)|| "YES".equalsIgnoreCase(vgmRequired)) {
			aBooking.setFullReturnLocation("YES");
		} else {
			aBooking.setFullReturnLocation("NO");
		}
		return snxObj;
	}

	private Boolean getTosRefDataAsBoolean(String s) {
		String returnValue = TosRefDataUtil.getValue(s);
		if (returnValue != null && !returnValue.isEmpty()) {
			try {
				Boolean returnBoolean = Boolean.parseBoolean(returnValue);
				return returnBoolean;
			} catch (Exception ex) {
				logger.error("Not able to parse the value " + returnValue + " to boolean, using Boolean.parseBoolean()", ex);
			}
		} else {
			logger.error("configure value for reference " + s);
			return Boolean.FALSE;
		}
		return Boolean.FALSE;
	}

	@Override
	protected Object xmlObjToTextObj(Object xmlObj) throws TosException {
		// TODO Auto-generated method stub
		return null;
	}

	private String getFerqFunc( String ferqPlanning) {
		if ( ferqPlanning.length() > 0)
			return ferqPlanning.substring(0, 1);
		else
			return null;
	}
	private String getFerqLength( String ferqPlanning) {
		if ( ferqPlanning.length() > 4)
			return ferqPlanning.substring(1, 5);
		else
			return null;
	}
	private String getFerqHeight( String ferqPlanning) {
		if ( ferqPlanning.length() > 5)
			return ferqPlanning.substring(5);
		else
			return null;
	}

	private TBooking.Items.Item populateItem( String function, String tempUnit,
			String temp, String temp2, String acetsVent, boolean oog, String commodity) {
		logger.info("BOB item function : "+function);
		logger.info("input params are  \'function\'=" + function +
				" \'tempUnit\'=" + tempUnit +
				"\'temp\'=" + temp +
				"\'temp2\'=" + temp2 +
				"\'acetsVent\'=" + acetsVent +
				"\'oog\'=" + oog +
				"\'commodity\'=" + commodity);
		String ferqFunction = getFerqFunc( function);
		String ferqHeight = getFerqHeight( function);
		// A2
		if(ferqHeight != null) ferqHeight = ferqHeight.toUpperCase();
		String ferqLength = getFerqLength( function);

		if (ferqFunction == null || ferqFunction.trim().length() < 1)
			return null;
		// only need last two chars for the eq-size
		TBooking.Items.Item anItem = new TBooking.Items.Item();
		//A8 - Force Set size28 to size40
		if(ferqLength != null && ferqLength.equals("0028")){
			anItem.setEqSize("40");
		}else if ( ferqLength != null){
			anItem.setEqSize( ferqLength.substring( ferqLength.length()-2));
		}

		anItem.setQty( new BigInteger( "999"));
		if(oog) {
			TEqoiOog oogValue = new TEqoiOog();
			//oogValue.setIsOog(true);
			oogValue.setIsOog("true"); //A17
			anItem.setOog(oogValue);
		}
		//A19 - Dont set Equipment type
		//A18 - Set Equipment type/ISO
		String eqType = ferqFunction;
		if(ferqLength != null)
			eqType = eqType + ferqLength.substring( ferqLength.length()-2);
		if(ferqHeight != null)
			eqType = eqType + ferqHeight;
		if(eqType.trim().matches("[a-zA-Z]{1}\\d{2}[a-zA-Z]{0,1}"))
			anItem.setEquipmentType(eqType.trim());
		else
			logger.error("Equipment Type/ISO code not valid : "+eqType);
		//A18-End
		logger.info("BOB item eqType : "+eqType);
		//A19
		if ( ferqFunction.equals("F")) {
			if ( ferqHeight != null && ferqHeight.equals("L"))
				anItem.setEqHeight( "43");
			else if ( ferqHeight != null && ferqHeight.equals("H"))
				anItem.setEqHeight( "96");
			else if ( ferqHeight != null && ferqHeight.equals("M"))
				anItem.setEqHeight( "130");
			else
				anItem.setEqHeight("86");
			anItem.setEqIsoGroup( TIsoGroup.PL);
		} else {
			if ( ferqHeight != null && ferqHeight.equals("L"))
				anItem.setEqHeight( "43");
			else if ( ferqHeight != null && ferqHeight.equals("H"))
				anItem.setEqHeight( "96");
			else
				anItem.setEqHeight("86");

			if ( ferqFunction != null && ferqFunction.equals("D"))
				anItem.setEqIsoGroup( TIsoGroup.GP);
			else if ( ferqFunction != null && ferqFunction.equals("R"))
				anItem.setEqIsoGroup( TIsoGroup.RE);
			else if ( ferqFunction != null && ferqFunction.equals("T"))
				anItem.setEqIsoGroup( TIsoGroup.TN); //A20
			else if ( ferqFunction != null && ferqFunction.equals("O")) //A15
				anItem.setEqIsoGroup( TIsoGroup.UT);
			else if ( ferqFunction != null && ferqFunction.equals("I")) //A15
				anItem.setEqIsoGroup( TIsoGroup.HI);
			else
				anItem.setEqIsoGroup( TIsoGroup.GP);
		}
		if( tempUnit.trim().length() > 0) {
			TBaseReeferRequirements  reefer = new TBaseReeferRequirements();
			anItem.setReefer( reefer);
			if ( tempUnit.equalsIgnoreCase( "C")) {
				try {
					reefer.setTempReqdC( new BigDecimal( temp.trim()));
				} catch (Exception e) {
					logger.warn("Could not convert temp "+temp+" for booking.");
				}
			} else if ( tempUnit.equalsIgnoreCase( "F")) {
				if ( temp.trim().length() != 0) {
					try {
						double tempDbl = new Float( temp.trim()).doubleValue();
						tempDbl = ( tempDbl -32)/9*5;
						reefer.setTempReqdC( new BigDecimal( tempDbl).setScale( 3, RoundingMode.HALF_UP));
					} catch (Exception e) {
						logger.warn("Could not convert temp "+temp+" for booking.");
					}
				}
			}
			if ( acetsVent.trim().length() > 0) {
				String percent = acetsVent.substring( 4, acetsVent.length()).trim();
				String vent = acetsVent.substring( 0, 4).trim();
				logger.debug( "vent=" + vent + " percent=" + percent);
				reefer.setVentRequiredUnit( "PERCENTAGE");
				if ( vent.equalsIgnoreCase( "CLOS")) {
						reefer.setVentRequiredValue( new BigDecimal( 0.0));
				} else if ( percent.length() > 0){
					try {
						reefer.setVentRequiredValue( new BigDecimal( percent));
					} catch (Exception e) {
						logger.warn("Could not convert temp "+temp+" for booking.");
					}
				}
			}
		}

		/*if((temp == null || temp.isEmpty() || temp.equalsIgnoreCase("KFF") || temp.equalsIgnoreCase("AMB"))
				&& (temp2 == null || temp2.isEmpty() || temp2.equalsIgnoreCase("KFF") || temp2.equalsIgnoreCase("AMB"))) {
			TBaseReeferRequirements reefer = new TBaseReeferRequirements();
			anItem.setReefer(reefer);
			reefer.setTempReqdC(new BigDecimal("-999"));
		}*/

		/* Alaska Changes - Start */
			logger.info("tempUnit\t"+tempUnit);
		if (tempUnit != null) {

				String temp1Final = null;
				String temp2Final = null;

			if (tempUnit != null && !tempUnit.isEmpty() && "F".equalsIgnoreCase(tempUnit)) {
					try {

						logger.debug(" temp1 "+temp+" F  temp2 "+temp2+" F");
						temp1Final = temp.trim().toString();
						temp2Final = temp2.trim().toString();

					} catch (Exception e) {
						logger.error("Could not convert the temp1 "+temp+" or temp2 "+temp2+" for booking. The temperature is provided in \'Fahrenheit\'");
					}
			} else if (tempUnit != null && !tempUnit.isEmpty() && "C".equalsIgnoreCase(tempUnit)) {

						try {

							logger.debug(" trying to convert temp1 "+temp+"C  temp2 "+temp2+"C to \'Fahrenheit\'");
							if ( temp.trim().length() != 0 && !("KFF".equalsIgnoreCase(temp) || "AMB".equalsIgnoreCase(temp))) {
								double tempDbl = new Float( temp.trim()).doubleValue();
								tempDbl = ( tempDbl +32)/5*9;
								temp1Final = new BigDecimal( tempDbl).setScale( 3, RoundingMode.HALF_UP).toString();
							}else
								temp1Final = temp.trim();

							if ( temp2.trim().length() != 0 && !("KFF".equalsIgnoreCase(temp) || "AMB".equalsIgnoreCase(temp))) {
								double tempDb2 = new Float( temp2.trim()).doubleValue();
								tempDb2 = ( tempDb2 +32)/5*9;

								temp2Final = new BigDecimal( tempDb2).setScale( 3, RoundingMode.HALF_UP).toString();
							}else
								temp2Final = temp2.trim();


						} catch (Exception e) {
							logger.error("Could not convert the temp1 "+temp+" or temp2 "+temp2+" for booking. The temperature is provided in \'Celcius\'");
						}

				} else {
					try {

						logger.debug("this piece of code should not be reached, if input is proper, It's an safety net\n \'temp1\'= " + temp + "   \'temp2\'= " + temp2 + " ");
						temp1Final = temp != null && !temp.isEmpty() ? temp.trim().toString() : "";
						temp2Final = temp2 != null && !temp2.isEmpty() ? temp2.trim().toString() : "";

					} catch (Exception e) {
						logger.error("Could not convert the temp1 "+temp+" or temp2 "+temp2+" for booking.");
					}
				}
				if ((temp != null && !temp.isEmpty() && "KFF".equalsIgnoreCase(temp.trim())) || (temp2!=null && !temp2.isEmpty() && "KFF".equalsIgnoreCase(temp2.trim())))
					anItem.setRemarks("KFF");
				else if ((temp != null && !temp.isEmpty() && "AMB".equalsIgnoreCase(temp.trim())) || (temp2!=null && !temp2.isEmpty() && "AMB".equalsIgnoreCase(temp2.trim())))
					anItem.setRemarks("AMB");
				else if (temp != null && !temp.isEmpty() && temp2 != null && !temp2.isEmpty())
					anItem.setRemarks("DUAL/" + temp1Final + "/" + temp2Final);
				else if (temp != null && !temp.isEmpty() && (temp2 == null || temp2.isEmpty()) )
					anItem.setRemarks(temp1Final);
				else if (temp2 != null && !temp2.isEmpty() && (temp == null || temp.isEmpty())) /*only if temp2 is available, set it with an slash at front*/
					anItem.setRemarks("/" + temp2Final);
				else if ((temp == null || temp.isEmpty()) && (temp2 == null || temp2.isEmpty()))
					anItem.setRemarks("");
			}

		/* Alaska Changes - end*/


		/*Start : A21 Setting SIT for SIT bookings */
		/* All SIT Bookigs for Alaska is MULTISTOP SIT*/

		if("Y".equals(commodity)){
			anItem.setCommodityId(MULTISTOP_SIT);
			anItem.setCommodityName(MULTISTOP_SIT);
		}/*else{
			anItem.setCommodityId("--");
			anItem.setCommodityName("");
		}*/
		/*End : A21 */
		return anItem;
	}

	private String getPackingGroup( Abob obj) {
		String ret = null;
		String temp = obj.getBfrtPkgGrp().trim();
		if ( temp.length() == 0)
			return ret;
		if ( temp.length() == 1)
			return "I";

		if ( temp.equalsIgnoreCase( "2"))
			return "II";
		else if ( temp.equalsIgnoreCase( "3"))
			return "III";
		else if ( temp.length() == 2)
			return "II";
		else if ( temp.length() == 3)
			return "III";
		else
			return ret;
	}

	/*
	 * Added method to Create Bob Groovy Injection Message //A14
	 */
	protected void postBobGvyInjXml(String bookingNum,String lineOperator,String carrierId){
		try{
		Map<String, String> data = new HashMap<String, String>();

		data.put( "bookingNum", bookingNum);
		data.put( "lineOperator", lineOperator);
		data.put( "obCarrierId", carrierId);

		String bobStr = GroovyXmlUtil.getInjectionXmlStr( CLASS_NAME, CLASS_LOC, data);

		JMSSender sender = new JMSSender( JMSSender.REAL_TIME_QUEUE, "HON");
		sender.send(bobStr);
		logger.debug("Posted LineOP Updt BobMsg ="+bobStr);
		}catch(Exception e){
			e.printStackTrace();
			EmailSender.sendMail(emailAddr, emailAddr, "BOB LineOperator Update Message Handling Error", "BOB LineOperator Update Message Handling Error");
		}
	}

	//changes for ILB next voyage issue

	private String setCarrierId(String inCarrierId, String inPortofLoading, String carrier, String voyageNbr, String nextFacility) throws Exception {

		logger.info("inside setCarrierId==== "+ inCarrierId +" "+inPortofLoading+" "+carrier+" "+voyageNbr+" "+nextFacility);

		if (inPortofLoading.equalsIgnoreCase(ANK) ||
				inPortofLoading.equalsIgnoreCase(KDK) ||
				inPortofLoading.equalsIgnoreCase(DUT)) {
			//set inCarrierId as carrierId
			return inCarrierId;
		} else {
			TosLookup lookup = null;
			try {
				lookup = new TosLookup();
				//Get the BARGE Visit
				List<Map> hasBkg = lookup.getBargeVisitsById(inCarrierId);
				boolean isNewVisit = Boolean.FALSE;
				if (!hasBkg.isEmpty()) {
					////SELECT vst.ID VST_ID, fcy.ID CURRENT_FACILITY,  vst.PHASE,  nxt_fcy.ID NEXT_FACILITY,  dtl.IB_VYG,  dtl.ob_vyg,  svc.ID SVC_ID,  dtl.CLASSIFICATION
					for (Map map : hasBkg) {
						if (BARGE.equalsIgnoreCase((String) map.get(CLASSIFICATION)) &&
								inPortofLoading.equalsIgnoreCase((String) map.get(CURRENT_FACILITY)) &&
								DUT.equalsIgnoreCase((String) map.get("NEXT_FACILITY"))) {
							isNewVisit = Boolean.TRUE;
						} else if ("".equalsIgnoreCase((String) map.get(CLASSIFICATION))) {

						}
					}
				}
				if (isNewVisit) {
					String errorMessage = "";
					String newVisitId = lookup.getBargeVisitsByBargeAndNextFacility(carrier, nextFacility, voyageNbr);
					logger.info("Testing newVisitId===="+newVisitId);
					if (NONE.equalsIgnoreCase(newVisitId)) {
						errorMessage = "No Barge Visit found for barge " + carrier + "Next facility" + nextFacility + "IB Vyg" + voyageNbr;
						throw new TosException(errorMessage);
					} else if ("NO_UNIQUE".equalsIgnoreCase(newVisitId)) {
						errorMessage = "No Unique Barge Visit found for barge " + carrier + "Next facility" + nextFacility + "IB Vyg" + voyageNbr;
						throw new TosException(errorMessage);
					} else if ("ERROR".equalsIgnoreCase(newVisitId)) {
						errorMessage = "Error when searching for Barge Visit for barge " + carrier + "Next facility" + nextFacility + "IB Vyg" + voyageNbr;
						throw new TosException(errorMessage);
					} else
						return newVisitId;

				}
			} finally {
				if (lookup != null) lookup.close();
			}
			return inCarrierId;

		}
	}

	//end

}
