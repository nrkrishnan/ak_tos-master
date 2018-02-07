package com.matson.tos.processor;

import com.matson.cas.erd.service.data.VesselVO;
import com.matson.cas.refdata.RefDataLookup;
import com.matson.cas.refdata.mapping.*;
import com.matson.tos.dao.NewVesselDaoGum;
import com.matson.tos.exception.NewVesselLogger;
import com.matson.tos.jatb.NewVes;
import com.matson.tos.reports.NewvesReport;
import com.matson.tos.reports.gum.GumNewvesReport;
import com.matson.tos.util.CalendarUtil;
import com.matson.tos.util.TosRefDataUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;



/*
 * 
 * #		Date			Author						Description
 * ---		-----------		------------------			---------------------
 * 1		03/20/2013		Karthik Rajendran			Class file created from HON Newves NewvesProcessorHelper
 * 2		03/23/2013		Karthik Rajendran			Added:blnbr-DO NOT EDIT removed, category fix,carrier id/mode fix.
 * 														removed unit-flex-3- csrid.
 * 3		03/25/2013		Karthik Rajendran			Commented: getNextOutboundVesselForPort()
 * 4		03/26/2013		Karthik Rajendran			Commented: robRdsDataFinal usage, create hazard record elements.
 * 5		03/27/2013		Karthik Rajendran			Trucker assignment condition removed.
 * 														Changed: startNewVessProc() - to have VVD as param instead of vvd list
 * 6		03/29/2013		Karthik Rajendran			Added: null check for vesselAvailableDate
 * 7		04/01/2013		Karthik Rajendran			Added: Reports flow integration - Inbound report
 * 8		04/02/2013		Karthik Rajendran			Added: Invoke creating manifest file
 * 9		04/03/2013		Karthik Rajendran			Removed: Date parameters
 * 														Added: Reefer report integrated
 * 10		01/29/2014		Karthik Rajendran			Changed: Archiving NV.GUM file into "gumrds" subdirectory
 *
 */




public class GumNewvesProcessorHelper {

	private static final String MULTISTOP_SIT = "MULTISTOP SIT";
	private static Logger logger = Logger.getLogger(GumNewvesProcessorHelper.class);
	public ArrayList<TosGumRdsDataFinalMt> rdsDataFinal;
	//public ArrayList<TosGumRdsDataFinalMt> robRdsDataFinal;
	public ArrayList<TosGumDcmMt> dcmData;
	public ArrayList<TosGumStPlanCntrMt> stowCntrData;
	public ArrayList<TosGumStPlanChasMt> stowChassisData;
	public ArrayList<TosGumStPlanChasMt> bareChassisData;
	private ArrayList<String> outputText;
	private String outFilePath = System.getProperty("java.io.tmpdir");;
	private static String outFileName = "";
	public static final String carrierId = "GEN_TRUCK";
	public static final String CNTR = "cntr";
	public static final String ROB = "rob";
	public static final String BARE = "bare";
	public String currentVesvoyType = "";
	public static final String PRIMARY = "primary";
	public static final String SUPPLEMENT = "supplement";
	public String obVesvoy = "";
	public String vesselAvailableDate = "";
	public Date vesselArrivalDate = null;
	public String vesselOpr = "";
	//public Date triggerDate = null;
	public String processType = "";
	private NewVesselLogger nvLogger = NewVesselLogger.getInstance();;
	public static String IS_COPY_GUM = "IS_COPY_GUM";
	public String COPY_PRIMARY = "false";
	public static String NV_VES_FILES_FTP_ID = "NV_VES_FILES_FTP_ID";
	public static String SUP_FILES_FTP_ID = "SUP_FILES_FTP_ID";

	public GumNewvesProcessorHelper() {
		// TODO Auto-generated constructor stub
	}

	public void startNewVessProc(String vvd)
	{
		startPrimaryVesselProc(vvd);
	}
	public void startPrimaryVesselProc(String vvd)
	{
		if(vvd != null)
		{   vvd  = vvd.toUpperCase();
			String vesvoy = vvd.substring(0, 6);
			getAllRequiredDataForVesVoy(vesvoy);
			obVesvoy = vesvoy;
			outputText = new ArrayList<String>();
			if (outFilePath !=null && !outFilePath.endsWith("/")) {
				outFilePath = outFilePath + "/";
			}
			outFileName = outFilePath + vesvoy + "NV.GUM";
			currentVesvoyType = PRIMARY;
			createNewVesselTextFile(vvd, outFileName);
			generateNewVesReports(vvd);
			nvLogger.sendGumNewVessSuccess(vvd, "L", "false");
		}
		else
		{
			nvLogger.addError("", "", "VVD Error.");
		}
	}
	public void generateNewVesReports(String vvd)
	{
		logger.info("Generating Guam New Vessel reports....");
		NewvesReport.createReport("VVDDownloadReport",vvd, null,null);
		GumNewvesReport.createReport(GumNewvesReport.INBOUND_BY_DESTINATION_DISCHARGE_SERVICE, vvd, "");
		GumNewvesReport.createReport(GumNewvesReport.SEND_TAG_MANIFEST, vvd, "");
		GumNewvesReport.createReport(GumNewvesReport.REEFER, vvd, "");
		GumNewvesReport.createReport(GumNewvesReport.DOWNLOAD_DISCREPANCIES, vvd, "");
		GumNewvesReport.createReport(GumNewvesReport.RIDER_REPORT, vvd, "");
	}
	private void getAllRequiredDataForVesVoy(String vesvoy)
	{
		try{
			rdsDataFinal = NewVesselDaoGum.getGumRdsDataFinalForVesvoy(vesvoy);
			//robRdsDataFinal = new ArrayList<TosGumRdsDataFinalMt>();
			stowCntrData = NewVesselDaoGum.getGumOCRDataForVesvoy(vesvoy, null);
			stowChassisData = NewVesselDaoGum.getGumOCHDataForVesvoy(vesvoy);
			bareChassisData = NewVesselDaoGum.getGumBareOCHDataForVesvoy(vesvoy);
			dcmData = NewVesselDaoGum.getGumDcmDataForVesvoy(vesvoy);

			//}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			logger.error("Error : Unable to get RDS/ROB/STOW CNTR/CHASSIS/DCM for " + vesvoy);
		}
	}
	private void createNewVesselTextFile(String vesvoy, String fileName)
	{
		logger.debug("entered createNewVesselTextFile - vesvoy value:"+vesvoy);
		if(currentVesvoyType.equals(PRIMARY)) {
			createVesselVisitData(vesvoy);
			if(vesselAvailableDate==null || vesselAvailableDate.equals(""))
			{
				nvLogger.addError(vesvoy, "", "Vessel available date is missing.");
				return;//If vessel available date is null then no output should be produced for the vesvoy.
			}
		}
		for(int i=1; i<=3; i++)
		{
			if(i==1)
			{
				logger.debug("In createNewVesselTextFile: i value:"+i);
				createUnitRecordData(CNTR, rdsDataFinal, null, stowCntrData, stowChassisData, null, dcmData);
			}
			else if(i==2)
			{
				logger.debug("In createNewVesselTextFile: i value:"+i);
				//createUnitRecordData(ROB, null, robRdsDataFinal, stowCntrData, stowChassisData, null, dcmData);
			}
			else if(i==3)
			{
				logger.debug("In createNewVesselTextFile: i value:"+i);
				createUnitRecordData(BARE, null, null, null, null, bareChassisData, null);
			}
		}
		createEofData();
		if(currentVesvoyType.equals(PRIMARY))
			writeToUnitTextFile(outputText, fileName);
	}
	private void createVesselVisitData(String vesvoy)
	{
		NewVes data = new NewVes();
		data.setRecType("vesselVisit");
		data.setUnitId(vesvoy.substring(0, 6));
		Date today = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
		String trigDate = sdf.format(today);
		data.setUfvFlex2(trigDate);
		sdf = new SimpleDateFormat("HH:mm:ss");
		String trigTime = sdf.format(today);
		data.setUfvFlex3(trigTime);
		try {
			vesselAvailableDate = CommonBusinessProcessor.getAvailableDateByVVD(vesvoy, "GUM");
		} 
		catch(Exception e)
		{
			logger.error("Problem in retreiving available date for" + vesvoy);
			nvLogger.addError(vesvoy, "", "Problem in retreiving available date for" + vesvoy);
		}
		if(vesselAvailableDate==null)
		{
			logger.error("vesselAvailableDate is null");
			return;
		}
		if(vesselAvailableDate.length()>0)
			vesselAvailableDate = vesselAvailableDate.trim();//To catch error if available date is null
		/*if(vesselAvailableDate==null || vesselAvailableDate.equals("")) // For testing purpose, setting this date as todays date
		{
			vesselAvailableDate = CalendarUtil.convertDateToString(triggerDate);
		}*/

		data.setUfvFlexDate2(vesselAvailableDate);
		outputText.add(createTextRecord(data));
		try {
			VesselVO vvo = CommonBusinessProcessor.getVesselDetails(vesvoy.substring(0, 3));
			vesselOpr = vvo.getVessOpr();
		} 
		catch(Exception e)
		{
			logger.error("Problem in retreiving vessel operator for "+vesvoy.substring(0, 3));
		}
		if(vesselOpr==null || vesselOpr.equals("")) // For testing purpose, setting this vessel operator to MAT
			vesselOpr = "MAT";
		//Get arrival date
		vesselArrivalDate = CommonBusinessProcessor.getArrivalDateByVVD(vesvoy, "GUM");
	}
	private void createUnitRecordData(String type, 
			ArrayList<TosGumRdsDataFinalMt> rdsFinalData, ArrayList<TosGumRdsDataFinalMt> robRdsData, 
			ArrayList<TosGumStPlanCntrMt> stowCtrData, 
			ArrayList<TosGumStPlanChasMt> stowChsData, ArrayList<TosGumStPlanChasMt> bareChsData, 
			ArrayList<TosGumDcmMt> dcmData)
	{
		logger.debug("entered createUnitRecordData - type value:"+type);

		if(type.equals(CNTR) || type.equals(ROB))
		{
			if(type.equals(ROB))
				rdsFinalData = robRdsData;
			//logger.info("**** UNIT RECORD ****");
			for(int i=0; i<rdsFinalData.size(); i++)
			{
				TosGumRdsDataFinalMt rdsFd = rdsFinalData.get(i);
				NewVes data = new NewVes();
				data.setRecType("unit");
				String ctrno = rdsFd.getContainerNumber();
				String chkdigit = rdsFd.getCheckDigit();
				String owner = rdsFd.getOwner();
				owner = owner==null?"MATU":owner;
				if(isNumber(ctrno))
					ctrno = owner+ctrno;
				chkdigit = chkdigit != null ? chkdigit : "X";
				data.setUnitId(ctrno+chkdigit);
				String dischPort = rdsFd.getDischargePort();
				dischPort = dischPort==null?"":dischPort;
				String category = "";
				String dir = rdsFd.getDir();
				dir = dir==null?"":dir;
				String ds = rdsFd.getDs();
				ds = ds==null?"":ds;
				String dport = rdsFd.getDport();
				dport = dport==null?"":dport;
				String orientation = rdsFd.getOrientation();
				orientation = orientation==null?"":orientation;
				String locationRowDeck = rdsFd.getLocationRowDeck();
				locationRowDeck = locationRowDeck==null?"":locationRowDeck;
				String hazOpenCloseFlg = rdsFd.getHazardousOpenCloseFlag();
				hazOpenCloseFlg = hazOpenCloseFlg==null?"":hazOpenCloseFlg;
				logger.info("Type:"+type+"\t CN:"+rdsFd.getContainerNumber()+"\tDIR:"+dir+"\tDISCPORT:"+dischPort+"\tDPORT:"+dport+"\tHAZOCFLAG:"+hazOpenCloseFlg+
						"\tDS:"+ds+"\tORIENTATION:"+orientation+"\tCONSIGNE:"+rdsFd.getConsignee()+"\tSHIPPER:"+rdsFd.getShipper());
				if(type.equals(ROB))
				{
					category = "THROUGH";
				}
				else if(dir.equalsIgnoreCase("MTY"))
				{
					if(dischPort.equals("GUM")||dischPort.equals("EBY")||dischPort.equals("KWJ")||dischPort.equals("MAJ")||dischPort.equals("OPT"))
						category = "IMPORT";
					else
						category = "THROUGH";
				}
				else if(dir.equalsIgnoreCase("IN") && dport.equalsIgnoreCase("HON"))
				{
					category = "IMPORT";
				}
				else if(dir.equalsIgnoreCase("IN") && CommonBusinessProcessor.isValidNeighborIslandPort(dport))
				{
					category = "IMPORT";
				}
				else if(hazOpenCloseFlg.equalsIgnoreCase("M"))
				{
					category = "IMPORT";
				}
				else if(hazOpenCloseFlg.equalsIgnoreCase("G"))
				{
					category = "IMPORT";
				}
				else if(dir.equalsIgnoreCase("IN"))
				{
					if(dischPort.equals("NGB")||dischPort.equals("SHA")||dischPort.equals("XMN"))
						category = "THROUGH";
				}
				else if(dir.equalsIgnoreCase("OUT"))
				{
					category = "EXPORT";
				}
				//logger.info("CATEGORY ASSIGNED:"+category);
				data.setCategory(category);
				if(type.equals(CNTR)){
					data.setTransitState("INBOUND");
				}
				String freightKind = "";
				if(ds.equalsIgnoreCase("CON"))
					freightKind = "FCL";
				else if(orientation.equalsIgnoreCase("F"))
					freightKind = "FCL";
				else if(orientation.equalsIgnoreCase("E"))
					freightKind = "MTY";
				else if(dir.equalsIgnoreCase("IN"))
					freightKind = "FCL";
				else if(dir.equalsIgnoreCase("OUT"))
					freightKind = "FCL";
				else if(dir.equalsIgnoreCase("MTY"))
					freightKind = "MTY";
				else
					freightKind = "MTY";		
				data.setFreightKind(freightKind);
				if(locationRowDeck.equalsIgnoreCase(""))
					data.setLine("MAT");
				else
					data.setLine(locationRowDeck);
				String hazf = "";//rdsFd.getHazf();
				vesselOpr = vesselOpr==null?"":vesselOpr;
				//logger.info("HAZF -> Table:"+hazf +", "+vesselOpr+", "+ ctrno);
				//if(hazf==null||hazf.equals(""))
				//{
				// Check the DCM if there is any haz data for the container
				if(type.equals(CNTR) && !currentVesvoyType.equals(SUPPLEMENT))
				{
					/*for(int d=0;d<dcmData.size(); d++)
					{
						TosGumDcmMt dcmD = dcmData.get(d);
						String dCtrno = dcmD.getContainerNumber();
						if(dCtrno != null && dCtrno.equals(ctrno))
						{
							hazf = "Y";
							logger.info("HAZF-> Y From DCM check : "+ctrno);
							break;
						}
					}
					if(!hazf.equals("Y"))
					{
						// If the container dont have DCM then look into stowplan haz table
						for(int s=0; s<stowCntrData.size(); s++)
						{
							TosGumStPlanCntrMt stowCntrMt = stowCntrData.get(s);
							String ctrNbr = stowCntrMt.getContainerNumber();
							if(ctrNbr != null && ctrNbr.equalsIgnoreCase(ctrno))
							{
								if(stowCntrMt.getHazf()==null || !stowCntrMt.getHazf().equals("Y"))
									break;
								Set<TosGumStPlanHazMt> stowPlanHazMt = stowCntrMt.getTosGumStPlanHazMts();
								if(stowPlanHazMt!=null)
								{
									Iterator<TosGumStPlanHazMt> itrHaz = stowPlanHazMt.iterator();
									while(itrHaz.hasNext())
									{
										hazf = "Y";
										logger.info("HAZF-> Y From OHZ check : "+ctrno);
										break;
									}
								}
							}
							if(hazf.equals("Y"))
								break;
						}
					}
					if(hazf.equals("Y"))
						data.setHazardFlag(hazf);*/
				logger.info(ctrno + " - HAZF:"+rdsFd.getHazf());
				data.setHazardFlag(rdsFd.getHazf());
				}
				outputText.add(createTextRecord(data));
				//
				createHandlingData(rdsFd, null, type);
				createEtcData(rdsFd, null, type);
				createSealsData(rdsFd);
				createContentsData(rdsFd, null, type);
				createUnitFlexData(rdsFd);
				createUfvFlexData(rdsFd);
				createEquipmentData(rdsFd, null, type);
				if(!currentVesvoyType.equals(SUPPLEMENT))
				{
					/*if(hazf.equals("Y"))
					{
						boolean isHazCreatedFromDcm = false;
						for(int d=0;d<dcmData.size(); d++)
						{
							TosGumDcmMt dcmD = dcmData.get(d);
							String dCtrno = dcmD.getContainerNumber();
							if(dCtrno != null && dCtrno.equals(ctrno))
							{
								isHazCreatedFromDcm = true;
								createHazardData(dcmD);
							}
						}
						if(!isHazCreatedFromDcm)
						{
							for(int c=0; c<stowCtrData.size(); c++)
							{
								TosGumStPlanCntrMt stowCntrMt = stowCntrData.get(c);
								String ctrNbr = stowCntrMt.getContainerNumber();
								if(ctrNbr != null && ctrNbr.equalsIgnoreCase(ctrno))
								{
									if(stowCntrMt.getHazf()==null || !stowCntrMt.getHazf().equals("Y"))
										break;
									Set<TosGumStPlanHazMt> hazDataSet = stowCntrMt.getTosGumStPlanHazMts();
									Iterator<TosGumStPlanHazMt> itrHaz = hazDataSet.iterator();
									while(itrHaz.hasNext())
									{
										TosGumStPlanHazMt hazData = itrHaz.next();
										createHazardDataForCsx(hazData);
									}
								}
							}
						}
					}*/
					createOogData(rdsFd);
					createPositionData(rdsFd, null, type);
					createReeferData(rdsFd);
				}
				createRoutingData(rdsFd, null, type);
				createCarrierData(rdsFd, null, type);
			}
		}
		else if(type.equals(BARE))
		{
			//logger.info("entered bare process"+bareChassisData.size());
			for(int b=0; b<bareChassisData.size(); b++)
			{
				TosGumStPlanChasMt bareChd = bareChassisData.get(b);
				NewVes data = new NewVes();
				data.setRecType("unit");
				data.setUnitId(bareChd.getChassisNumber() + bareChd.getChassisCd());
				data.setCategory("IMPORT");
				data.setTransitState("INBOUND");
				data.setFreightKind("MTY");
				String chsHolds = bareChd.getChassisHolds();
				if(chsHolds!=null && chsHolds.length()>0)
					data.setLine(chsHolds);
				else
					data.setLine("MAT");		
				outputText.add(createTextRecord(data));

				createHandlingData(null, bareChd, type);
				createContentsData(null, bareChd, type);
				createEquipmentData(null, bareChd, type);
				createPositionData(null, bareChd, type);
				createRoutingData(null, bareChd, type);
				createCarrierData(null, bareChd, type);
			}
		}
	}
	private void createHandlingData(TosGumRdsDataFinalMt inData, TosGumStPlanChasMt bareChd, String type)
	{
		NewVes data = new NewVes();
		data.setRecType("handling");
		if(type.equals(ROB) || type.equals(CNTR))
		{
			String cargoNotes = inData.getCargoNotes();
			cargoNotes = cargoNotes==null?"":cargoNotes;
			String crstatus = inData.getCrstatus();
			crstatus = crstatus==null?"":crstatus;
			String comments = inData.getComments();
			comments = comments==null?"":comments;
			String ctrno = inData.getContainerNumber();
			ctrno = ctrno==null?"":ctrno;
			if("ZZZZ".equalsIgnoreCase(inData.getTruck()) && !"CSX".equalsIgnoreCase(vesselOpr)){
				data.setRemark(cargoNotes.trim()+" ZZZZ");
			}else
				data.setRemark(cargoNotes.trim());// + " " + comments.trim());
			String strc = inData.getStowRestrictionCode(); 
			data.setSpecialStow(strc);

			if(strc!= null && strc.equals("C"))
				data.setSpecialStow("CL");
			else if(strc != null && strc.equals("3"))
				data.setSpecialStow("INSP");
			if(crstatus != null)
			{
				if(crstatus.contains("TI") || crstatus.contains("AG") || crstatus.contains("CG"))
					data.setSpecialStow("INSP");
			}
			//
			String lastFreeDay = "";
			lastFreeDay = inData.getLocationCategory();
			if(lastFreeDay!=null && lastFreeDay.length()>0)
			{
				lastFreeDay = CalendarUtil.convertDateStringToString(lastFreeDay, false);
				data.setLastFreeDay(lastFreeDay);
			}
		}
		else if(type.equals(BARE))
		{
			data.setRemark(bareChd.getComments());
		}
		if(data.getSpecialStow()==null && data.getRemark() == null && data.getLastFreeDay()==null)
			return;
		outputText.add(createTextRecord(data));
	}
	private void createEtcData(TosGumRdsDataFinalMt inData, TosGumStPlanChasMt bareChd, String type)
	{
		NewVes data = new NewVes();
		data.setRecType("etc");
		if(type.equals(ROB) || type.equals(CNTR))
		{
			String drayStatus = null;
			String planDisp = inData.getPlanDisp();
			planDisp = planDisp==null?"":planDisp;
			String dsc = inData.getDsc();
			dsc = dsc==null?"":dsc;
			String commodity = inData.getCommodity();
			commodity = commodity==null?"":commodity;
			String locationStatus = inData.getLocationStatus();
			locationStatus = locationStatus==null?"":locationStatus;
			String dir = inData.getDir();
			dir = dir==null?"":dir;
			if("S".equals(dsc))
				drayStatus = "OFFSITE";
			else if(planDisp.equals("W"))
				drayStatus = "OFFSITE";
			else if(planDisp.equals("3"))
			{
				if(commodity.length()>2)
				{
					if(commodity.substring(0, 2).equals("P2") || commodity.substring(0, 2).equals("53"))
						drayStatus = "OFFSITE";
				}
			}
			else if(dsc.equals("C") && !locationStatus.equals("6"))
				drayStatus = "DRAYIN";
			else if(planDisp.equals("7"))
				drayStatus = "TRANSFER";
			else if(planDisp.equals("9") || planDisp.equals("A") || planDisp.equals("B"))
				drayStatus = "RETURN";
			if(vesselOpr.equals("CSX"))
				drayStatus = "DRAYIN";
			data.setDrayStatus(drayStatus);
			String temp = inData.getTemp();
			if(temp != null && temp.length()>0 && !temp.equalsIgnoreCase("AMB"))
			{
				data.setRequiresPower("Y");
			}
		}
		if(data.getDrayStatus()==null && data.getRequiresPower()==null)
			return;
		outputText.add(createTextRecord(data));
	}
	private void createSealsData(TosGumRdsDataFinalMt inData)
	{
		NewVes data = new NewVes();
		data.setRecType("seals");
		data.setSeal1(inData.getSealNumber());
		if(data.getSeal1()==null)
			return;
		outputText.add(createTextRecord(data));
	}
	private void createContentsData(TosGumRdsDataFinalMt inData, TosGumStPlanChasMt bareChd, String type)
	{
		NewVes data = new NewVes();
		data.setRecType("contents");
		if(type.equals(ROB) || type.equals(CNTR))
		{
			String consigneeName = inData.getConsignee();
			consigneeName = consigneeName==null?"":consigneeName.trim();
			/*if (consigneeName != null) {
				consigneeName = consigneeName.length() >= 32 ? consigneeName
						.substring(0, 32) : consigneeName;
			}*/
			//if (currentVesvoyType.equals(SUPPLEMENT)) {
				data.setBlNumber(inData.getBookingNumber());
			/*}else if (currentVesvoyType.equals(PRIMARY)) {
				if (!"CSX".equalsIgnoreCase(vesselOpr)) {
					data.setBlNumber("DO NOT EDIT-NEWVES:"+inData.getBookingNumber());
				}else {
					data.setBlNumber(inData.getBookingNumber());
				}
			}*/
			BigDecimal cwg = inData.getCweight();
			if(cwg != null)
			{
				double ncwg = cwg.doubleValue();
				data.setWeightKg(convertPoundsToKG(ncwg));
			}

			String commodity = inData.getCommodity();
			commodity = commodity==null?"":commodity;
			String outComId = "";
			String outComName = "";
			if(inData.getCrstatus()!=null && inData.getCrstatus().contains("CUS"))
			{
				outComId = "CUS";
				outComName = "CUSTOMS";
			}
			else if(commodity.length()>0)
			{
				if(commodity.equals("COBUS") || commodity.equals("COBIZ") || commodity.equals("CO BUS") || commodity.equals("CO BIZ"))
				{
					outComId = "COBUS";
					outComName = "COMPANY BUSINESS";
				}
				else if(commodity.equals("ASTRAY"))
				{
					outComId = "ASTRAY";
					outComName = "ASTRAY";
				}
				else if(commodity.equals("SHRED"))
				{
					outComId = "SHRED";
					outComName = "SHRED";
				}
				else if(commodity.equals("MTY") || commodity.equals("EMPTY"))
				{
					outComId = "MTY";
					outComName = "EMPTY";
				}
				else if(commodity.equals("XMAS40"))
				{
					outComId = "XMAS40";
					outComName = "XMAS40";
				}
				else if(commodity.equals("XMASTREE"))
				{
					outComId = "XMASTREE";
					outComName = "XMASTREE";
				}
				else if(commodity.equals("T-60") || commodity.equals("T60"))
				{
					outComId = "T-60";
					outComName = "TARIFF 60";
				}
				else if(commodity.equals("LOAD"))
				{
					outComId = "LOAD";
					outComName = "LOAD";
				}
				else if(commodity.equals("HHGDS"))
				{
					outComId = "HHGDS";
					outComName = "HOUSEHOLD GOODS";
				}
				else if(commodity.equals("PAPAYA"))
				{
					outComId = "PAPAYA";
					outComName = "PAPAYA";
				}
				else if(commodity.equals("PLANTS"))
				{
					outComId = "PLANTS";
					outComName = "PLANTS";
				}
				else if(commodity.equals("CANNED P"))
				{
					outComId = "CANNED PINEAPPLE";
					outComName = "CANNED PINEAPPLE";
				}
				else if(commodity.equals("FRESH PI"))
				{
					outComId = "FRESH PINEAPPLE";
					outComName = "FRESH PINEAPPLE";
				}
				else if(commodity.equals("MP"))
				{
					outComId = "MP";
					outComName = "MAUI PINE";
				}
				else if(commodity.equals("DM"))
				{
					outComId = "DM";
					outComName = "DEL MONTE";
				}
				else if(commodity.equals("DOLE"))
				{
					outComId = "DOLE";
					outComName = "DOLE";
				}
				else if(commodity.equals("P.O."))
				{
					outComId = "P.O.";
					outComName = "POST OFFICE";
				}
				else if(commodity.equals("PAPER"))
				{
					outComId = "PAPER";
					outComName = "PAPER";
				}
				else if(commodity.equals("AUTO"))
				{
					outComId = "AUTO";
					outComName = "AUTO";
				}
				else if(commodity.equals("ANHEUSER"))
				{
					outComId = "ANHEUSER";
					outComName = "ANHEUSER BUSCH";
				}
				else if(commodity.equals("MTY TANK"))
				{
					outComId = "MTY TANK";
					outComName = "MTY TANK";
				}
				else if(commodity.equals("DHX"))
				{
					outComId = "DHX";
					outComName = "DHX";
				}
				else if(commodity.equals("GOLDNSTA"))
				{
					outComId = "GOLDEN STATE";
					outComName = "GOLDEN STATE";
				}
				else if(commodity.equals("HAZARD"))
				{
					outComId = "HAZARD";
					outComName = "HAZARDOUS";
				}
				else if(commodity.equals("MLK CASE"))
				{
					outComId = "MTY MILK CASES";
					outComName = "MTY MILK CASES";
				}
				else if(commodity.equals("MTY PLTS"))
				{
					outComId = "MTY PTLS";
					outComName = "MTY PALLETS";
				}
				else if(commodity.equals("SCRAP"))
				{
					outComId = "SCRAP";
					outComName = "SCRAP";
				}
				else if(commodity.equals("TIRES"))
				{
					outComId = "TIRES";
					outComName = "TIRES";
				}
				else if(commodity.equals("SAFEWAY"))
				{
					outComId = "SAFEWAY";
					outComName = "SAFEWAY";
				}
				else if(commodity.equals("SIT") || commodity.equals(MULTISTOP_SIT))
				{
					outComId = MULTISTOP_SIT;
					outComName = MULTISTOP_SIT;
				}
				else if(commodity.equals("AUTOCY"))
				{
					outComId = "AUTOCY";
					outComName = "AUTO CY";
				}
				else if(commodity.equals("AUTOCON"))
				{
					outComId = "AUTOCON";
					outComName = "AUTO CON";
				}
				else if(commodity.equals("ALSAUT"))
				{
					outComId = "ALS AUT";
					outComName = "ALS AUT";
				}
				else if(commodity.equals("ALS ?"))
				{
					outComId = "ALS ?";
					outComName = "ALS AUTO?";
				}
				else if(commodity.equals("MTYAUT"))
				{
					outComId = "MTYAUT";
					outComName = "MTY AUTO";
				}
				else if(commodity.equals("YB"))
				{
					outComId = "YB";
					outComName = "YOUNG BROTHERS";
				}
				else if(commodity.equals("TRASH"))
				{
					outComId = "TRASH";
					outComName = "TRASH";
				}
				else if(commodity.equals("GYM"))
				{
					outComId = "GYM";
					outComName = "GYM";
				}
				else if(commodity.equals("GEAR"))
				{
					outComId = "GEAR";
					outComName = "GEAR";
				}
				else if(commodity.equals("BALLAST"))
				{
					outComId = "BALLAST";
					outComName = "BALLAST";
				}
			}

			data.setCommodityId(outComId);
			data.setCommodityName(outComName);

			// Modified by karthik
			String consigneeId = "";
			String tpCodeFtur = "";
			if (inData.getTypeCode() != null) 
				tpCodeFtur = inData.getTypeCode().substring(6, 8);
			if ("AUTOMOBILE".equals(consigneeName)) {
				consigneeId = "AUTOID";
			}
			else if(inData.getDir() != null && !inData.getDir().equals("MTY") ){
				if (! ("GR".equalsIgnoreCase(tpCodeFtur) && inData.getBookingNumber() == null)) {
						consigneeId = inData.getCneeCode();
				}
			}

			data.setConsigneeName(consigneeName);

			if (consigneeName !=null && consigneeName.contains("REQUIRES CS ACTION" )) {
				consigneeId = "REQCSACTON";
				data.setConsigneeId(consigneeId);
			}
			/*if(inData.getConsigneeArol()!=null && "0000000000".equals(inData.getConsigneeArol()) && inData.getBookingNumber()!=null&& !inData.getBookingNumber().equals("null")  && !isNumber(inData.getBookingNumber()))
			{
				data.setConsigneeId("REQCSACTON");
				data.setConsigneeName("REQUIRES CS ACTION");
			}*/
			if (consigneeName !=null && consigneeName.contains("UNAPPROVED VARIANCE" )) {
				consigneeId = "UNAPPROVAR";
				data.setConsigneeId(consigneeId);
			}
			if(type.equals(CNTR))
			{
				data.setConsigneeId(consigneeId);
			}

			String shipperName = inData.getShipper();
			data.setShipperName(shipperName);
			String shipperId = "";
			if(inData.getDir() != null && !inData.getDir().equals("MTY")) {
				if(shipperName== null) {
					shipperId = "";
				} else if (inData.getShipperQualifier() != null && !inData.getShipperQualifier().equals("") && !inData.getShipperQualifier().equals("null")) {
					shipperId = inData.getShipperArol();
				} else {
					shipperId = inData.getShipperOrgnId();
				}
			}
			if(type.equals(CNTR))
			{
				data.setShipperId(shipperId);
			}
			//
		}
		else if(type.equals(BARE))
		{
			BigDecimal tare = bareChd.getChassisTare();
			if(tare.doubleValue() > 0)
				data.setWeightKg(convertPoundsToKG(tare.doubleValue()));
		}
		if(data.getBlNumber()==null && data.getWeightKg()==null && data.getConsigneeId()==null &&
				data.getConsigneeName()==null && data.getShipperId()==null && data.getShipperName()==null)
			return;
		outputText.add(createTextRecord(data));
	}
	private void createUnitFlexData(TosGumRdsDataFinalMt inData)
	{
		NewVes data = new NewVes();
		data.setRecType("unit-flex");
		data.setUnitFlex1(inData.getCneePo());
		String unitFlex12 = inData.getCneePo();
		if(unitFlex12 != null)
		{
			if(unitFlex12.length() >= 24)
			{
				if(unitFlex12.substring(0, 6).length() == 6 && unitFlex12.substring(7, 24).length()>= 17)
				{
					//data.setUnitFlex12(unitFlex12.substring(7, 24));//
				}
			}
		}
		if(data.getUnitFlex1()== null && data.getUnitFlex12()== null && data.getUnitFlex3()== null)
			return;
		outputText.add(createTextRecord(data));
	}
	private void createUfvFlexData(TosGumRdsDataFinalMt inData)
	{		
		NewVes data = new NewVes();
		data.setRecType("ufv-flex");
		String pmd = inData.getPmd();
		if(pmd != null)
		{
			if(pmd.equalsIgnoreCase("TX") || pmd.equalsIgnoreCase("MG"))
				data.setUfvFlex2(pmd);
			Date today = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("MM");
			String month = sdf.format(today);
			sdf = new SimpleDateFormat("yyyy");
			String year = sdf.format(today);
			data.setUfvFlexDate1(month + pmd + year);
		}
		String vessAvailableDate = "";
		String dueDate = "";
		Date tempDate = inData.getArrdate();
		if(tempDate!=null)
			vessAvailableDate = CalendarUtil.convertDateToString(tempDate);
		dueDate = inData.getMisc3();
		if(dueDate!=null && dueDate.length()>0)
		{
			data.setUfvFlexDate2(vessAvailableDate);
			data.setUfvFlexDate3(dueDate);
		}

		if(data.getUfvFlex2() == null && data.getUfvFlexDate1() == null && 
				data.getUfvFlexDate2() == null && data.getUfvFlexDate3()==null )
			return;
		outputText.add(createTextRecord(data));
	}
	private void createHoldDataForBarge(TosGumRdsDataFinalMt inData){
		String ctrno = inData.getContainerNumber();
		for(int i=0; i<stowCntrData.size(); i++)
		{
			TosGumStPlanCntrMt stowCntrMt = stowCntrData.get(i);
			String ctrNbr = stowCntrMt.getContainerNumber();
			if(ctrNbr != null && ctrNbr.equalsIgnoreCase(ctrno))
			{
				Set<TosGumStPlanHoldMt> stowHoldMt = stowCntrMt.getTosGumStPlanHoldMts();
				Iterator<TosGumStPlanHoldMt> itrHold = stowHoldMt.iterator();
				while(itrHold.hasNext())
				{
					TosGumStPlanHoldMt holdData = itrHold.next();
					NewVes data = new NewVes();
					data.setRecType("hold");
					data.setHoldId(holdData.getId().getCode());
					outputText.add(createTextRecord(data));
				}

			}
		}

	}
	private void createEquipmentData(TosGumRdsDataFinalMt inData, TosGumStPlanChasMt bareChd, String type)
	{
		if(type.equals(ROB) || type.equals(CNTR))
		{
			createEquipmentContainer(inData);
			if(inData.getChassisNumber() != null && inData.getChassisNumber().length() > 0)
				createEquipmentChassis(inData);
		}
		else if(type.equals(BARE))
		{
			createEquipmentBare(bareChd);
			if(bareChd.getMgNumber()!= null && bareChd.getMgNumber().length()>0)
				createEquipmentAcc(bareChd);
		}
	}
	private void createEquipmentContainer(TosGumRdsDataFinalMt inData)
	{
		NewVes data = new NewVes();
		data.setRecType("equipment");
		if(StringUtils.isAlpha(inData.getContainerNumber().substring(0,1))){ // added by Meena
			data.setEqid(inData.getContainerNumber()+inData.getCheckDigit());			
		}else{
			data.setEqid(inData.getOwner()+ inData.getContainerNumber()+inData.getCheckDigit());
		}
		String tCode = inData.getTypeCode();
		if(tCode != null)
		{
			if(tCode.substring(0, 1).equalsIgnoreCase("C"))
				data.setClazz("CHS");
			else
				data.setClazz("CTR");
		}
		String hgt = inData.getHgt();
		data.setType(inData.getTypeCode());
		if(hgt!=null && hgt.length()>4)
		{
			hgt = hgt.substring(0, 4);
			if(!isNumber(hgt.substring(0, 2)) || !isNumber(hgt.substring(2, 4)))
			{
				if(tCode!=null && tCode.equals("UNKNOWN") && data.getClazz().equals("CTR"))
					data.setType("UNKN");
				else if(tCode!=null && tCode.equals("UNKNOWN") && data.getClazz().equals("CHS"))
					data.setType("UNK");
				else
					data.setType(tCode.length()>3?tCode.substring(0, 3):tCode);
			}
			else
			{
				int hgtinch;
				String temp1 = hgt.substring(0, 2);
				String temp2 = hgt.substring(2, 4);
				hgtinch = Integer.parseInt(temp1) * 12 + Integer.parseInt(temp2);
				if(tCode != null)
				{
					if(tCode.equals("UNKNOWN"))
					{
						if(data.getClazz().equals("CHS"))
							data.setType("UNK");
						else
							data.setType("UNKN");
					}
					else if(tCode.substring(0,1).equals("A"))
					{
						if(hgtinch <= 138)
							data.setType(tCode.substring(0, 3) + "L");
						else if(hgtinch >= 152)
							data.setType(tCode.substring(0, 3) + "H");
						else
							data.setType(tCode.substring(0, 3));
					}
					else if(tCode.substring(0,1).equals("F"))
					{
						if(hgt.equals("1300"))
							data.setType(tCode.substring(0, 3) + "M");
						else if(hgtinch <= 96)
							data.setType(tCode.substring(0, 3) + "L");
						else if(hgtinch > 102)
							data.setType(tCode.substring(0, 3) + "H");
						else
							data.setType(tCode.substring(0, 3));
					}
					else if(tCode.substring(0,1).equals("R"))
					{
						if(hgtinch <= 96)
							data.setType(tCode.substring(0, 3) + "L");
						else if(hgtinch > 102)
							data.setType(tCode.substring(0, 3) + "H");
						else
							data.setType(tCode.substring(0, 3));
					}
					else
					{
						if(hgtinch <= 96)
							data.setType(tCode.substring(0, 3) + "L");
						else if(hgtinch > 102)
							data.setType(tCode.substring(0, 3) + "H");
						else
							data.setType(tCode.substring(0, 3));
					}
					String subGrp = "";
					String temp = tCode.substring(6, 8);
					if(temp.equals("4V"))
						subGrp = "4V";
					else if(temp.equals("CL"))
						subGrp = "CL";
					else if(temp.equals("DV"))
						subGrp = "DV";
					else if(temp.equals("FC"))
						subGrp = "FC";
					else if(temp.equals("GB"))
						subGrp = "GB";
					else if(temp.equals("GR"))
						subGrp = "GR";
					else if(temp.equals("GY"))
						subGrp = "GY";
					else if(temp.equals("H3"))
						subGrp = "H3";
					else if(temp.equals("H4"))
						subGrp = "H4";
					else if(temp.equals("HG"))
						subGrp = "HG";
					else if(temp.equals("CA"))
						subGrp = "CA";
					else if(temp.equals("VO"))
						subGrp = "VO";
					if(!subGrp.equals(""))
					{
						if(data.getType().length() == 3)
							data.setType(data.getType() + " " + subGrp);
						else
							data.setType(data.getType()+ subGrp);
					}
				}
			}
		}
		data.setRole("PRIMARY");
		BigDecimal twt = inData.getTareWeight();
		if(twt != null)
		{
			double ntwt = twt.doubleValue();
			data.setTareKg(convertPoundsToKG(ntwt));
		}

		String hgt1 = inData.getHgt();
		if(hgt1!=null && hgt1.length()>=6)
		{
			double feet = Double.parseDouble(hgt1.substring(0, 2));
			double inches = Double.parseDouble(hgt1.substring(2, 4)+"."+hgt1.substring(4, 6));
			double totalinches = (feet * 12) + inches;
			totalinches = totalinches * 25.4;
			data.setHeightMm(""+ new DecimalFormat("##.#").format(totalinches));
		}
		if(hgt1!=null && !hgt1.endsWith("00"))
		{
			data.setHeightMm(hgt1);
		}
		String strength = inData.getStrength();
		if(strength != null && !strength.equalsIgnoreCase("")) {
			data.setStrengthCode(strength.substring(1, strength.length()));
		}
		else {
		    data.setStrengthCode("A");
		}
		String locationRowDeck = inData.getLocationRowDeck();
		locationRowDeck = locationRowDeck==null?"":locationRowDeck;
		if(locationRowDeck.equals(""))
			data.setOperator("MAT");
		else
			data.setOperator(locationRowDeck);
		data.setOwner(inData.getOwner());
		String srv = inData.getSrv();
		if(srv != null && !srv.equalsIgnoreCase("MAT") && !srv.equalsIgnoreCase("CSX"))
			data.setEqFlex01("CLIENT");
		else
			data.setEqFlex01("MAT");
		if(tCode!=null && tCode.length()>6)
		{
			if(tCode.substring(6, tCode.length()).equals("ST"))
				data.setMaterial("STEEL");
			else if(tCode.substring(6, tCode.length()).equals("AL"))
				data.setMaterial("ALUMINUM");			
		}
		outputText.add(createTextRecord(data));
		String crstatus = inData.getCrstatus();
		if(crstatus != null && crstatus.length() > 0)
		{
			List<String> holdsList = (List<String>) Arrays.asList(crstatus.split(" "));
			Collections.sort(holdsList);
			for(int i=0; i<holdsList.size(); i++)
			{
				NewVes data1 = new NewVes();
				data1.setRecType("hold");
				String stat = holdsList.get(i);
				if(stat.trim().length()>0 && !stat.trim().equals("OK")) {
					data1.setHoldId(stat.trim());
					outputText.add(createTextRecord(data1));
				}
			}
		}

		// Damage record
		NewVes data2 = new NewVes();
		data2.setRecType("damage");
		String dcode = inData.getDamageCode();
		if(dcode != null)
		{
			if(!dcode.equalsIgnoreCase("Z"))
			{				
				data2.setDamageType("UNKNOWN");
				data2.setComponent("UNKNOWN");
				if(dcode.equalsIgnoreCase("L") || dcode.equalsIgnoreCase("M"))
					data2.setSeverity("MINOR");
				else if(dcode.equalsIgnoreCase("H"))
					data2.setSeverity("MAJOR");
				else
					data2.setSeverity("NONE");
			}
		}
		outputText.add(createTextRecord(data2));
	}
	private void createEquipmentChassis(TosGumRdsDataFinalMt inData)
	{
		String chassisNbr = inData.getChassisNumber();
		for(int i=0; i<stowChassisData.size(); i++)
		{
			TosGumStPlanChasMt stowChsD = stowChassisData.get(i);
			String chsNbr = stowChsD.getChassisNumber();
			if(chsNbr != null && chsNbr.equalsIgnoreCase(chassisNbr))
			{
				NewVes data = new NewVes();
				data.setRecType("equipment");
				data.setEqid(stowChsD.getChassisNumber() + stowChsD.getChassisCd());
				data.setClazz("CHS");
				String typeCode = stowChsD.getTypeCode();
				if(typeCode != null && typeCode.length() > 0)
					data.setType(typeCode.substring(0,3));
				else
					data.setType("CXX");
				data.setRole("CARRIAGE");
				BigDecimal tare = stowChsD.getChassisTare();
				if(tare.doubleValue() > 0)
					data.setTareKg(convertPoundsToKG(tare.doubleValue()));
				data.setOwner(stowChsD.getOwner());
				outputText.add(createTextRecord(data));
				if(stowChsD.getMgNumber() != null && stowChsD.getMgNumber().length()>0)
				{
					data = new NewVes();
					data.setRecType("equipment");
					data.setEqid(stowChsD.getMgNumber());
					data.setClazz("ACC");
					data.setType("MG01");
					data.setRole("ACCESSORY_ON_CHS");
					BigDecimal tareC = stowChsD.getMgTare();
					if(tare.doubleValue() > 0)
						data.setTareKg(convertPoundsToKG(tareC.doubleValue()));
					outputText.add(createTextRecord(data));
				}
				createChassisHoldDamageData(stowChsD, "CHS");
			}
		}
	}
	private void createChassisHoldDamageData(TosGumStPlanChasMt stowChsD, String type)
	{
		if(stowChsD.getChassisAlert()!=null && stowChsD.getChassisAlert().length() > 0)
		{
			NewVes data = new NewVes();
			data.setRecType("hold");
			data.setHoldId("RDCH");
			outputText.add(createTextRecord(data));
		}
		NewVes data2 = new NewVes();
		data2.setRecType("damage");
		String dcode = stowChsD.getDamageCode();
		if(dcode != null && dcode.trim().length() > 0)
		{
			if(type.equals(BARE))
			{
				if(!dcode.equalsIgnoreCase("Z"))
				{				
					data2.setDamageType("UNKNOWN");
					data2.setComponent("UNKNOWN");
					if(dcode.equalsIgnoreCase("L") || dcode.equalsIgnoreCase("M"))
						data2.setSeverity("MINOR");
					else if(dcode.equalsIgnoreCase("H"))
						data2.setSeverity("MAJOR");
					else
						data2.setSeverity("NONE");
				}
			}
			else if(type.equals("CHS"))
			{
				data2.setDamageType("UNKNOWN");
				data2.setComponent("UNKNOWN");
				if(dcode.equalsIgnoreCase("L") || dcode.equalsIgnoreCase("M"))
					data2.setSeverity("MINOR");
				else if(dcode.equalsIgnoreCase("H"))
					data2.setSeverity("MAJOR");
				else
					data2.setSeverity("NONE");
			}
		}
		outputText.add(createTextRecord(data2));
	}
	private void createEquipmentBare(TosGumStPlanChasMt bareChd)
	{
		NewVes data = new NewVes();
		data.setRecType("equipment");
		data.setEqid(bareChd.getChassisNumber() + bareChd.getChassisCd());
		data.setClazz("CHS");
		String typeCode = bareChd.getTypeCode();
		if(typeCode != null && typeCode.length() > 0)
			data.setType(typeCode.substring(0,3));
		else
			data.setType("CXX");
		data.setRole("PRIMARY");
		BigDecimal tare = bareChd.getChassisTare();
		if(tare.doubleValue() > 0)
			data.setTareKg(convertPoundsToKG(tare.doubleValue()));
		data.setOwner(bareChd.getOwner());
		data.setOperator("MAT");
		String srv = bareChd.getSrv();
		if(srv != null && !srv.equalsIgnoreCase("MAT") && !srv.equalsIgnoreCase("CSX"))
			data.setEqFlex01("CLIENT");
		else
			data.setEqFlex01("MAT");
		outputText.add(createTextRecord(data));
		createChassisHoldDamageData(bareChd, BARE);
	}
	private void createEquipmentAcc(TosGumStPlanChasMt bareChd)
	{
		NewVes data = new NewVes();
		data.setRecType("equipment");
		data.setEqid(bareChd.getMgNumber());
		data.setClazz("ACC");
		data.setType("MG01");
		data.setRole("ACCESSORY_ON_CHS");
		BigDecimal tare = bareChd.getMgTare();
		if(tare.doubleValue() > 0)
			data.setTareKg(convertPoundsToKG(tare.doubleValue()));
		outputText.add(createTextRecord(data));
	}
	private void createHazardDataForCsx(TosGumStPlanHazMt hazData)
	{
		NewVes data = new NewVes();
		data.setRecType("hazard");
		data.setImdg(hazData.getHazClass());
		data.setUn(""+hazData.getUnNumber());
		data.setTechName(hazData.getDescription());
		data.setProperName(hazData.getDescription());
		outputText.add(createTextRecord(data));
	}
	private void createHazardData(TosGumDcmMt inData)
	{
		NewVes data = new NewVes();
		data.setRecType("hazard");
		data.setImdg(inData.getHazClass());
		data.setUn(inData.getHazardCode());
		String hazCodeType = inData.getHazardCodeType();
		hazCodeType = hazCodeType==null?"":hazCodeType;
		if(hazCodeType.equals(""))
			data.setNbrType("UN");
		else
			data.setNbrType(hazCodeType);
		String remarks = inData.getRemarks();
		remarks = remarks==null?"":remarks;
		//logger.info("remarks:"+remarks +" for container#:"+inData.getContainerNumber());
		if(remarks.length()>0) {
			//logger.info(inData.getContainerNumber()+ " CREATE HAZARD DATA - REMARKS -->" + remarks);
			if(remarks.contains("LIMITED QUANTITY") || remarks.contains("LTD QTY") || remarks.contains("LTD  QTY")|| remarks.contains("LTD QTY.") || remarks.contains("LTD.QTY.") || remarks.contains("LTD. QTY")
					|| remarks.contains("LTD. QTY.") || remarks.contains("LTDQTY") || remarks.contains("ltd qty") || remarks.contains("ltd.qty.") || remarks.toUpperCase().contains("LTD"))// added by Meena
			{
				data.setLtdQtyFlag("Y");
				String tempStr = remarks.replaceAll("LIMITED QUANTITY", "");
				tempStr = tempStr.replaceAll("LTD QTY.", "");
				tempStr = tempStr.replaceAll("LTD. QTY.", "");
				tempStr = tempStr.replaceAll("LTD QTY", "");
				tempStr = tempStr.replaceAll("LTD  QTY", "");
				tempStr = tempStr.replaceAll("LTDQTY", ""); // added by Meena
				tempStr = tempStr.replaceAll("ltd qty", "");// added by Meena
				tempStr = tempStr.replaceAll("LTD.QTY.", ""); // added by Keerthi
				tempStr = tempStr.replaceAll("ltd.qty.", "");// added by Keerthi
				tempStr = tempStr.replaceAll("LTD. QTY", "");//added by Keerthi
				tempStr = tempStr.replaceAll(",", "");
				tempStr = tempStr.replaceAll("\\.", "");
				remarks = tempStr;
			}
			if(remarks.indexOf("MARINE POLLUTANT")!=-1){
				data.setMarinePollutants("Y");
				remarks = remarks.replaceAll("MARINE POLLUTANT", "");
			}
		}
		data.setRemark(remarks.toUpperCase());

		String packNum = inData.getPackNum();
		packNum = packNum==null?"":packNum;
		String shname = inData.getShippingName();
		shname = shname==null?"":shname;
		if(packNum.length()>0)
		{
			ArrayList<String> fields = new ArrayList<String> (Arrays.asList(packNum.split(" ")));
			if(fields.size()>1)
			{
				String qty = packNum.substring(0, packNum.indexOf(" "));
				String type = packNum.substring(packNum.indexOf(" ")+1);
				if(qty != null && qty.length() > 0)
				{
					data.setQuantity(qty);
					data.setPackageType(type.length()>=7 ? type.substring(0, 7):type);
				}
				else
				{
					data.setQuantity("1");
					data.setPackageType(type.length()>=7 ? type.substring(0, 7):type);
				}
			}
			else
			{
				fields = new ArrayList<String> (Arrays.asList(packNum.split("X")));
				if(fields.size()>1)
				{
					String qty = packNum.substring(0, packNum.indexOf(" "));
					String type = packNum.substring(packNum.indexOf(" ")+1);
					if(qty != null && qty.length() > 0)
					{
						data.setQuantity(qty);
						data.setPackageType(type.length()>=7 ? type.substring(0, 7):type);
					}
					else
					{
						data.setQuantity("1");
						data.setPackageType(type.length()>=7 ? type.substring(0, 7):type);
					}
				}
				else
				{
					Scanner scr = new Scanner(packNum);
					if(scr.hasNextInt())
					{
						int qty = scr.nextInt();
						data.setQuantity(""+qty);
						if(shname.contains("VEHICLE"))
						{
							if(qty > 1)
								data.setPackageType("AUTOS");
							else
								data.setPackageType("AUTO");
						}
						else
						{
							if(qty > 1)
								data.setPackageType("UNITS");
							else
								data.setPackageType("UNIT");
						}
					}
					else
					{
						data.setQuantity("1");
						data.setPackageType(packNum.length()>=7 ? packNum.substring(0, 7):packNum);
					}
				}
			}
		}
		else
		{
			data.setQuantity("1");
			if(shname.contains("VEHICLE"))
				data.setPackageType("AUTO");
			else
				data.setPackageType("UNIT");
		}
		String tempFlp = inData.getFlashPoint();
		tempFlp = tempFlp==null?"":tempFlp;
		String tempFlpU = "C";
		while(true)
		{
			if(tempFlp.startsWith("<") || tempFlp.startsWith("+"))
			{
				tempFlp = tempFlp.substring(1, tempFlp.length());
			}
			if(!tempFlp.equals(""))
			{
				if(tempFlp.endsWith(" "))
				{
					tempFlp = tempFlp.substring(0, tempFlp.length()-1);
				}
				else if(tempFlp.endsWith("C") || tempFlp.endsWith("F"))
				{
					tempFlpU = tempFlp.substring(tempFlp.length()-1, tempFlp.length());
					tempFlp = tempFlp.substring(0, tempFlp.length()-1);
				}
				else
				{
					break;
				}
			}
			else
			{
				break;
			}
			if(tempFlpU.equals("F"))
			{
				double flpF = Double.parseDouble(tempFlp);
				data.setFlashPoint(convertFtoC(flpF));
			}
			else
			{
				data.setFlashPoint(tempFlp);
			}
		}

		data.setProperName(inData.getShippingName());
		data.setTechName(inData.getTechnicalName());
		data.setPackingGroup(inData.getPackagingMarks());
		String gw = inData.getGrossWeight();
		gw = gw==null?"":gw;
		String gwu = inData.getGrossWgUnit();
		gwu = gwu==null?"":gwu;
		if(gw.length()>0)
		{
			if(gwu.equalsIgnoreCase("KG"))
				data.setHazardWeightKg(gw);
			else
			{
				double weight = Double.parseDouble(gw);
				//String newW = new DecimalFormat("##.########").format(weight).toString();
				data.setHazardWeightKg(convertPoundsToKG(weight));
			}
		}
		else
			data.setHazardWeightKg("0");
		String phone = inData.getPhone();
		//if(phone != null && phone.length()>=10) //commented by Meena
		//	phone = phone.substring(0, 3) + " " +  phone.substring(3, 6) + " " +  phone.substring(6, 10);//commented by Meena
		data.setEmergencyTelephone(phone);
		String subClass = inData.getSubClass();
		subClass = subClass==null?"":subClass;
		ArrayList<String> tempList = new ArrayList<String>(Arrays.asList(subClass.split(",")));
		if(tempList!=null) {
			if(tempList.size()>1)
			{
				data.setSecondaryImo1(tempList.get(0));
				data.setSecondaryImo2(tempList.get(1));
			}
			else
			{
				data.setSecondaryImo1(subClass);
			}
		}
		outputText.add(createTextRecord(data));
	}

	private void createOogData(TosGumRdsDataFinalMt inData)
	{
		String oog = inData.getOdf();
		if(oog != null && oog.equalsIgnoreCase("Y"))
		{
			NewVes data = new NewVes();
			data.setRecType("oog");
			String ctrno = inData.getContainerNumber();
			for(int j=0; j<stowCntrData.size(); j++)
			{
				TosGumStPlanCntrMt stowCntrD = stowCntrData.get(j);
				String cctrno = stowCntrD.getContainerNumber();
				if(cctrno != null && (cctrno.equalsIgnoreCase(ctrno) || cctrno.equalsIgnoreCase(ctrno.substring(4, ctrno.length()))))
				{
					BigDecimal ovrHieght = stowCntrD.getOversizeHeightInches();
					if(ovrHieght.doubleValue() > 0)
						data.setTopCm(convertInchesToCm(ovrHieght.doubleValue()));
					else
						data.setTopCm("0");
					BigDecimal ovrLeft = stowCntrD.getOversizeLeftInches();
					if(ovrLeft.doubleValue() > 0)
						data.setLeftCm(convertInchesToCm(ovrLeft.doubleValue()));
					else
						data.setLeftCm("0");
					BigDecimal ovrRight = stowCntrD.getOversizeRightInches();
					if(ovrRight.doubleValue() > 0)
						data.setRightCm(convertInchesToCm(ovrRight.doubleValue()));
					else
						data.setRightCm("0");
					BigDecimal ovrFront = stowCntrD.getOversizeFrontInches();
					if(ovrFront.doubleValue() > 0)
						data.setFrontCm(convertInchesToCm(ovrFront.doubleValue()));
					else
						data.setFrontCm("0");
					BigDecimal ovrRear = stowCntrD.getOversizeRearInches();
					if(ovrRear.doubleValue() > 0)
						data.setBackCm(convertInchesToCm(ovrRear.doubleValue()));
					else
						data.setBackCm("0");
				}
			}
			outputText.add(createTextRecord(data));
		}		
	}
	private void createPositionData(TosGumRdsDataFinalMt inData, TosGumStPlanChasMt bareChd, String type)
	{
		NewVes data = new NewVes();
		data.setRecType("position");
		data.setLocType("VESSEL");
		if(type.equals(ROB) || type.equals(CNTR))
		{
			String srv = inData.getSrv();
			if(srv != null && srv.equalsIgnoreCase("MAT") && srv.equalsIgnoreCase("CSX"))
			{
				data.setLocation(inData.getVesvoy()!=null?inData.getVesvoy():"VESSEL");
			}
			else
			{
				if(inData.getMisc1()!=null)
					data.setLocation(inData.getVesvoy());//Need to set outbound vesvoy
				else if(inData.getVesvoy()!=null)
					data.setLocation(inData.getVesvoy());//Need to set outbound vesvoy
				else
					data.setLocation("VESSEL");
			}
			data.setSlot(inData.getCell());
		}
		else if(type.equals(BARE))
		{
			if(bareChd.getVesvoy()!=null)
			{
				data.setLocation(bareChd.getVesvoy());
				data.setSlot(bareChd.getLoc());
			}
			else
				data.setLocation("VESSEL");

		}
		outputText.add(createTextRecord(data));

	}
	private void createReeferData(TosGumRdsDataFinalMt inData)
	{
		String temp = inData.getTemp();
		if(temp != null && temp.length()>0 && !temp.equalsIgnoreCase("AMB"))
		{
			NewVes data = new NewVes();
			data.setRecType("reefer");
			String tempUnit = inData.getTempMeasurementUnit();
			if(tempUnit != null && tempUnit.equalsIgnoreCase("C"))
			{	
			}
			else
			{
				double tm = Double.parseDouble(temp);
				temp = convertFtoC(tm);
				tempUnit = "F";
			}
			if(temp!=null && !temp.equals(""))
			{
				data.setTempReqdC(temp);
				data.setTempDisplayUnit(tempUnit);
				outputText.add(createTextRecord(data));
			}
		}
	}
	private void createRoutingData(TosGumRdsDataFinalMt inData, TosGumStPlanChasMt bareChd, String type)
	{
		NewVes data = new NewVes();
		data.setRecType("routing");
		TosDestPodData refData = null;
		if(type.equals(ROB) || type.equals(CNTR))
		{
			String shiperPool = inData.getShipperPool();
			if(shiperPool != null && shiperPool.length()>0)
				data.setOpl(shiperPool);
			else
				data.setOpl(inData.getLoadPort());
			//logger.info("discharge port:"+inData.getDport() +" for container#:"+inData.getContainerNumber());
			String pod1 = "";
			String pod2 = "";
			try{
				//Set Pod-1 value from TosDestPodData by sending Dport as input
				refData = (TosDestPodData)RefDataLookup.queryById("com.matson.cas.refdata.mapping.TosDestPodData", inData.getDport());
				//logger.info("refdata from TosDestPodData"+refData);
				if ( refData != null) {
					pod2 = refData.getPod2();
					logger.info("POD-1 and POD-2 from refdata :"+refData.getPod1()+" - "+refData.getPod2());
				}
			} catch (Exception e)
			{
				e.printStackTrace();
				logger.error("Error while reading TosDestPodData ");
			}
			if (refData != null && !"OPT".equals(inData.getDischargePort())) {
				pod1 = refData.getPod1();
				logger.info("POD1 FROM TABLE :" + pod1);
			} else {
				pod1 = inData.getDischargePort();
				logger.info("POD1 FROM DISCPORT :" + pod1);
			}
			data.setPod1(pod1);
			data.setPol(inData.getLoadPort());
			data.setPod2(pod2);
			data.setDestination(inData.getDport());
			String truck = inData.getTruck();
			truck = truck==null?"":truck;
			String dir = inData.getDir();
			dir = dir==null?"":dir;
			String dport = inData.getDport();
			dport = dport==null?"":dport;
			String planDisp = inData.getPlanDisp();
			planDisp = planDisp==null?"":planDisp;
			String commodity = inData.getCommodity();
			commodity = commodity==null?"":commodity;
			String misc1 = inData.getMisc1();
			misc1 = misc1==null?"":misc1;
			String locationStatus = inData.getLocationStatus();
			locationStatus = locationStatus==null?"":locationStatus;
			String locationRowDeck = inData.getLocationRowDeck();
			locationRowDeck = locationRowDeck==null?"":locationRowDeck;
			String cargoNotes = inData.getCargoNotes();
			cargoNotes = cargoNotes==null?"":cargoNotes;
			String crstatus = inData.getCrstatus();
			crstatus = crstatus==null?"":crstatus;
			String owner = inData.getOwner();
			owner = owner==null?"":owner;
			//if(("GUM".equalsIgnoreCase(dport) || "S".equals(inData.getDsc()))&& !"ZZZZ".equalsIgnoreCase(truck))
			//{
				if(truck.equals("YBUU"))
					data.setDesignatedTrucker(misc1);
				else if(planDisp.equals("W"))
				{
					if(commodity.startsWith("WO"))
					{
						String temp = commodity.length()>5?commodity.substring(2, 5):"";
						//logger.info("temp in NewVesProc calss:"+temp+" - "+temp.length());
						//logger.info("truck in NewVesProc calss:"+truck+" - "+truck.length());
						if (temp.length()<= 0 && truck.length() > 0) {
							//logger.info("Inside If truck");
							data.setDesignatedTrucker(truck);
						} else {
							//logger.info("Inside else temp");
							data.setDesignatedTrucker(temp);
						}
					} else {
						data.setDesignatedTrucker(truck);
					}
				}
				else
					data.setDesignatedTrucker(truck);
				//logger.info("Assigned Designated-Truck:"+data.getDesignatedTrucker()+"\t"+inData.getContainerNumber()+"\t"+dport);
			//}
			String group = "";
			if(planDisp.equals("7"))
				group = "YB";
			else if (locationStatus.equals("7") && truck.equals("YBUU"))
				group = "YB";
			else if (commodity.equals("YB"))
				group = "YB";
			else if (planDisp.equals("3"))
			{
				if(commodity.length()>1)
				{
					if(commodity.substring(0, 2).equals("P2"))
						group = "XFER-P2";
					else if(commodity.substring(0, 2).equals("53"))
						group = "XFER-SI";
				}
			}
			else if (planDisp.equals("W"))
			{
				if(cargoNotes.contains("WEST OAHU"))
					group = "XFER-WO";
				if(commodity.substring(0, 2).equals("WO"))
					group = "XFER-WO";
				else if(commodity.substring(0, 2).equals("53"))
					group = "XFER-SI";
			}
			else if(locationRowDeck.equals("CSX") && owner.equals("CSXU"))
				group = "PASSPASS";
			else if (crstatus.contains("TS"))
				group = "TS";
			data.setGroup(group);
		}
		else if(type.equals(BARE))
		{
			data.setPol("HON");
			data.setOpl("HON");
			data.setPod1(bareChd.getDport());
			data.setDestination(bareChd.getDport());
		}
		outputText.add(createTextRecord(data));
	}
	private void createCarrierData(TosGumRdsDataFinalMt inData, TosGumStPlanChasMt bareChd, String type)
	{
		if(type.equals(ROB) || type.equals(CNTR))
		{
			//logger.info("createCarrierData-Begin --> "+inData.getContainerNumber());
			String locationStatus = inData.getLocationStatus();
			locationStatus = locationStatus==null?"":locationStatus;
			String dport = inData.getDport();
			dport = dport==null?"":dport;
			String dischPort = inData.getDischargePort();
			dischPort = dischPort==null?"":dischPort;
			String vesvoy = inData.getVesvoy();
			vesvoy = vesvoy==null?"":vesvoy;
			String hazOpenCloseFlg = inData.getHazardousOpenCloseFlag();
			hazOpenCloseFlg = inData.getTrade();
			hazOpenCloseFlg = hazOpenCloseFlg==null?"":hazOpenCloseFlg;
			String dir = inData.getDir();
			dir = dir==null?"":dir;
			String misc1 = inData.getMisc1();
			misc1 = misc1==null?"":misc1;
			String srv = inData.getSrv();
			String vesSrv = srv;
			String obVesvoy1 = vesvoy;
			String outVesvoyforExport = "";
			String loadportTrade = null;
			//logger.info("vesselAvailableDate->"+vesselAvailableDate);
			//logger.info("vesselArrivalDate->"+vesselArrivalDate);
			/*Date availDate = null;
			if (vesselAvailableDate!=null) {
				availDate = CalendarUtil.convertStrgToDateFormat(vesselAvailableDate);
			}
			if(srv != null && !srv.equalsIgnoreCase("MAT") && !srv.equalsIgnoreCase("CSX"))
				vesSrv = "CLI";
			if(availDate!=null && inData.getHazardousOpenCloseFlag() != null 
					&& (inData.getHazardousOpenCloseFlag().equalsIgnoreCase("M") || inData.getHazardousOpenCloseFlag().equalsIgnoreCase("G") ||
							inData.getHazardousOpenCloseFlag().equalsIgnoreCase("F")) )
			{ 
				loadportTrade = CommonBusinessProcessor.getTradeforPort(inData.getLoadPort());
				if ("F".equals(loadportTrade) || "G".equals(loadportTrade)) {
					if(!(inData.getDport().equals("HIL") || inData.getDport().equals("KAH") || 
							inData.getDport().equals("LNI") || inData.getDport().equals("MOL") || inData.getDport().equals("NAW")
							|| inData.getDport().equals("KHI"))){
						obVesvoy1 = CommonBusinessProcessor.getNextOutboundVesselForPort(inData.getDport(), availDate);
					}
				} else {
					obVesvoy1 = CommonBusinessProcessor.getNextOutboundVesselForPort("GUM", availDate);
				}
				if(obVesvoy1!=null)
				{
					misc1 = obVesvoy1;
				}
				else
					obVesvoy1 = "";
			}
			//logger.info("obVesvoy1 -> "+obVesvoy1);
			//logger.info("misc1 -> "+misc1);*/
			for(int i=1; i<=2; i++)
			{
				for(int j=1; j<=2; j++)
				{
					NewVes data1 = new NewVes();
					data1.setRecType("carrier");
					if(i==1)
						data1.setDirection("IB");
					else if(i==2)
						data1.setDirection("OB");
					if(i==1)
					{
						if(locationStatus.equals("4"))
						{
							data1.setMode("VESSEL");
							if(vesvoy.equals(""))
								data1.setCarrierId1("GEN_VESSEL");
							else
							{
								if(vesSrv.equals("CLI"))
									data1.setCarrierId1(obVesvoy);
								else
									data1.setCarrierId1(vesvoy);
							}
						}
						else if(locationStatus.equals("7"))
						{
							//logger.info("locationStatus for barge "+locationStatus);
							data1.setMode("VESSEL");
							if(currentVesvoyType.equals(SUPPLEMENT))
							{
								if(vesvoy.equals(""))
									data1.setCarrierId1("GEN_VESSEL");
								else
									data1.setCarrierId1(vesvoy);
							}
							else
								data1.setCarrierId1(obVesvoy1);
						}
						else
						{
							data1.setMode("VESSEL");
							if(vesvoy.equals(""))
								data1.setCarrierId1("GEN_VESSEL");
							else
								data1.setCarrierId1(vesvoy);
						}
					}
					else
					{
						if(locationStatus.equals("") || locationStatus.equals("3") || locationStatus.equals("4"))
						{
							if(vesSrv.equals("CLI"))
							{
								if(j==1)
								{
									if(CommonBusinessProcessor.isValidNeighborIslandPort(dport) || dport.equals("HON"))
									{
										data1.setMode("TRUCK");
										data1.setCarrierId1("GEN_TRUCK");
									}
									else
									{
										data1.setMode("VESSEL");
										data1.setCarrierId1("GEN_VESSEL");
									}
								}
								else if(j==2)
								{
									if(CommonBusinessProcessor.isValidNeighborIslandPort(dport))
									{
										data1.setMode("VESSEL");
										data1.setCarrierId1("GEN_VESSEL");
									}
									else if(dport.equals("HON"))
									{
										data1.setMode("TRUCK");
										data1.setCarrierId1("GEN_TRUCK");
									}
									else
									{
										data1.setMode("VESSEL");
										data1.setCarrierId1("GEN_VESSEL");
									}
								}
							}
							else if(CommonBusinessProcessor.isValidNeighborIslandPort(dport))
							{
								if(j==1)
								{
									data1.setMode("TRUCK");
									data1.setCarrierId1("GEN_TRUCK");
								}
								else
								{
									data1.setMode("VESSEL");
									if(misc1.equals(""))
										data1.setCarrierId1("BARGE");
									else
										data1.setCarrierId1(misc1);
								}
							}
							else if(dport.equals("HON"))
							{
								if(dir.equals("MTY"))
								{
									data1.setMode("UNKNOWN");
									data1.setCarrierId1("");
								}
								else
								{
									data1.setMode("TRUCK");
									data1.setCarrierId1("GEN_TRUCK");
								}
							}
							else if(hazOpenCloseFlg.equals("M") || hazOpenCloseFlg.equals("G") || hazOpenCloseFlg.equals("F"))
							{
								if(dischPort.equals("GUM"))
								{
									data1.setMode("TRUCK");
									data1.setCarrierId1("GEN_TRUCK");
								}
								else
								{
									data1.setMode("VESSEL");
									data1.setCarrierId1("GEN_VESSEL");
								}
							}
							else
							{
								data1.setMode("TRUCK");
								data1.setCarrierId1("GEN_TRUCK");
							}
						}
						else if(locationStatus.equals("2"))
						{
							data1.setMode("VESSEL");
							if(vesvoy.equals(""))
								data1.setCarrierId1("GEN_VESSEL");
							else
								data1.setCarrierId1(vesvoy);
						}
						else if(locationStatus.equals("7"))
						{
							if(currentVesvoyType.equals(SUPPLEMENT))
							{
								data1.setMode("VESSEL");
								if(misc1.equals(""))
									data1.setCarrierId1("GEN_VESSEL");
								else
									data1.setCarrierId1(misc1);
							}
							else
							{
								if(dport.equals("HON"))
								{
									if(dir.equals("MTY"))
									{
										data1.setMode("UNKNOWN");
										data1.setCarrierId1("");
									}
									else
									{
										data1.setMode("TRUCK");
										data1.setCarrierId1("GEN_TRUCK");
									}
								}
								else
								{
									//logger.info("outVesvoyforExport before populatin "+outVesvoyforExport+" - "+dir+" - "+inData.getContainerNumber());
									if(outVesvoyforExport==null || "".equalsIgnoreCase(outVesvoyforExport))
									{
										if(dir.equals("MTY"))
										{
											data1.setMode("UNKNOWN");
											data1.setCarrierId1("");
										}
										else
										{	data1.setMode("VESSEL");
										data1.setCarrierId1("GEN_VESSEL");
										}
									}
									else
									{
										data1.setMode("VESSEL");
										data1.setCarrierId1(outVesvoyforExport);
									}
								}
							}
						}
						else
						{
							if(dir.equals("MTY"))
							{
								data1.setMode("UNKNOWN");
								data1.setCarrierId1("");
							}
							else
							{
								if(CommonBusinessProcessor.isValidNeighborIslandPort(dport))
								{
									if(j==1)
									{
										data1.setMode("TRUCK");
										data1.setCarrierId1("GEN_TRUCK");
									}
									else
									{
										data1.setMode("VESSEL");
										data1.setCarrierId1("BARGE");
									}
								}
								else
								{
									data1.setMode("TRUCK");
									data1.setCarrierId1("GEN_TRUCK");
								}
							}
						}
					}
					if(j==1)
						data1.setQualifier("DECLARED");
					else
						data1.setQualifier("ACTUAL");
					outputText.add(createTextRecord(data1));
				}
			}
		}
		else if(type.equals(BARE))
		{
			String obVesvoy1 = "";
			String vesvoy = bareChd.getVesvoy();
			vesvoy = vesvoy==null?"":vesvoy;
			for(int o=0; o<2; o++)
			{
				NewVes data1 = new NewVes();
				data1.setRecType("carrier");
				data1.setDirection("IB");
				data1.setMode("VESSEL");
				if(obVesvoy1!=null && !obVesvoy1.equals(""))
					data1.setCarrierId1(obVesvoy1);
				else
					data1.setCarrierId1(bareChd.getVesvoy());
				if(o==0)
					data1.setQualifier("DECLARED");
				else
					data1.setQualifier("ACTUAL");
				outputText.add(createTextRecord(data1));
			}
			for(int p=0;p<2; p++)
			{
				NewVes data1 = new NewVes();
				data1.setRecType("carrier");
				data1.setDirection("OB");
				data1.setMode("TRUCK");
				data1.setCarrierId1("GEN_TRUCK");
				if(p==0)
					data1.setQualifier("DECLARED");
				else
					data1.setQualifier("ACTUAL");
				outputText.add(createTextRecord(data1));
			}
		}
	}

	private void createEofData()
	{
		NewVes data = new NewVes();
		data.setRecType(">>END OF FILE<<");
		outputText.add(createTextRecord(data));
	}
	private String createTextRecord(NewVes rec)
	{
		String temp = rec.toString();
		temp = temp.replace("null", "");
		return temp;
	}

	private String convertInchesToCm(double inches)
	{
		StringBuilder out = new StringBuilder();
		String temp = new DecimalFormat("##.##").format(inches*2.54).toString();
		out.append(temp);
		return out.toString(); 
	}
	private String convertPoundsToKG(double pound)
	{
		pound = pound *  0.45359237;			
		String newKG = new DecimalFormat("##.########").format(pound).toString();
		return newKG;
	}

	private String convertFtoC(double f)
	{
		f = ((f-32) * 5)/9;			
		String newT = new DecimalFormat("##.##########").format(f).toString();
		return newT;
	}
	private boolean isNumber(String num)
	{
		try {
			Integer.parseInt(num);
		}
		catch(NumberFormatException ex)
		{
			return false;
		}
		return true;
	}
	private void writeToUnitTextFile(ArrayList<String> out, String fileName)
	{
		if(out.size()>0)
		{
			try
			{
				logger.info("Writing containers text file "+ fileName);
				FileWriter fileWriter = new FileWriter(new File(fileName));
				BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
				for (int i=0; i<out.size(); i++)
				{
					bufferedWriter.write("\"" + (i+1) + "\"," + out.get(i));
					bufferedWriter.write(System.getProperty( "line.separator" ));
				}
				bufferedWriter.close();

				//writing to FTP proxy location.
				CommonBusinessProcessor.archiveNVtoFTPSub(new File(fileName),Integer.parseInt(TosRefDataUtil.getValue("RDS_ARCH_FTP_ID")),true, "gumrds");
				// copying final output to exisintg new vessel location for new vessel processor to pick up.
				try {
					if(currentVesvoyType.equalsIgnoreCase(PRIMARY))
						COPY_PRIMARY = TosRefDataUtil.getValue(IS_COPY_GUM);
				}
				catch(Exception e)
				{
					e.printStackTrace();
					return;
				}
				if(COPY_PRIMARY.equalsIgnoreCase("true")  && currentVesvoyType.equals(PRIMARY))
					CommonBusinessProcessor.archiveNVtoFTP(new File(fileName),Integer.parseInt(TosRefDataUtil.getValue("NV_VES_FILES_FTP_ID")),true);
			}
			catch (Exception e)
			{
				logger.error("Error in writing the file " + outFilePath+outFileName);
			}
		}
		else
		{
			logger.error("No output to write into unit text file");
		}
	}
}
