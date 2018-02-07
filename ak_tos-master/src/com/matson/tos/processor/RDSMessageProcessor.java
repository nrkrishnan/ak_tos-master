package com.matson.tos.processor;

import com.matson.cas.erd.service.data.VesselVO;
import com.matson.cas.refdata.RefDataLookup;
import com.matson.cas.refdata.mapping.*;
import com.matson.cas.service.ftp.FtpBizException;
import com.matson.cas.service.ftp.FtpProxyGetterBiz;
import com.matson.cas.service.ftp.FtpProxyListBiz;
import com.matson.gems.api.equipmentattributes.GEMSEquipment;
import com.matson.tos.dao.NewVesselDao;
import com.matson.tos.dao.TosLookup;
import com.matson.tos.exception.NewVesselLogger;
import com.matson.tos.util.CalendarUtil;
import com.matson.tos.util.TosRefDataUtil;
import com.matson.tos.vo.RDSFields;
import com.matson.tos.vo.XmlFields;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/* This class processes the RDS download file and persists the RDS data into TOS database
 * , creates new vessel text file.
 *
 * 	Srno	Date			AuthorName				Change Description
 * 	A1		09/26/2012		Raghu Pattangi			Initial creation
 *  A2		09/30/2012		Karthik Rajendran		Added RDS Data parsing logics
 *  A3		10/03/2012		Karthik Rajendran		Changed processLine()-to ensure parsing all fields according to the latest RDS layout
 *  A4 		10/11/2012		Karthik Rajendran		Linked NewVesProcessorHelper, Identifying primary/supplemental vessel process
 *  A5		10/18/2012		Karthik Rajendran		FTP code integration for RDS files
 *  A6      11/16/2012      Meena Kumari            Supplemental Logic implementation and changed primary/supplemental identify process
 *  A7      11/22/2012      Meena Kumari            Added: args to cargoNotesTransformation().
 *  A8		11/30/2012		Karthik Rajendran		Added: Supplemental process code changes
 *  A9		12/05/2012		Karthik Rajendran		Added: Primay,Supplemental spliting logic, process code changes.
 *  A10		12/26/2012		Karthik Rajendran		Added: User entered VVD should be primary. Custom NewVesselLogger to catch errors.
 *  A11		01/07/2013		Karthik Rajendran		Added: 	Identify primary/supplemental check using vvd and not with vessel,
 *  														Persisting eastbound containers in RDS table from BRDSINCH.000 file.
 *  														Looking up for eastbound RDS for the containers coming from .HON file only.
 *  														buildPrimaryCntrList()
 *  A12		01/10/2013		Karthik Rajendran		Changed: identifying primary and supplemental vvd when user enters vvd and process triggered by scheduler.
 *  A13		01/10/2013		Karthik Rajendran		Changed: Set temperature from stowplan, if not set it from RDS.
 *  A14		01/19/2013		Karthik Rajendran		Added: Non Guam/China load port update load port from stowplan
 *  												Changed: Picking up eastbound data or current RDS data logic, Eastbound containers update logic
 *  A15		01/27/2013		Karthik Rajendran		Removed: setting hazf flag from stowplan or RDS
 *  												Changed: Pick cweight from stowplan, if not then from RDS
 *  A16		01/29/2013		Karthik Rajendran		Added: Retrieving file names from FTP in descending order.
 *  A17		02/12/2013		Karthik Rajendran		Changed: Shipper/consignee id from TOS should be limited to 10 characters
 *  A18		02/21/2013		Karthik Rajendran		Added: Setting default strength code A if input is null
 *  A19		03/13/2013		Karthik Rajendran		Changed: Pick dischargeport from stowplan, not from RDS
 *  A20		03/14/2013		Karthik Rajendran		Changed: Commented stow plan containers trade from API, use the trade from stow plan
 *  A21		03/27/2013		Karthik Rajendran		Commented : fixIncorrectDischargePort()
 *  A22		04/11/2013		Karthik Rajendran		Added: Skip empty lines while parsing.
 *  												Added: Persisting other VVD eastbound data from RDS.
 *  A23		04/12/2013		Karthik Rajendran		Added: Logic to identify supplemental/primary/pre-trigger vvd against TosProcessLogger table.
 *  												Added: Delete eastbound data once it has been chosen.
 *  A24		04/15/2013		Karthik Rajendran		Added: invoke CommonBusinessProcessor.checkIDStrapFlatR()
 *  A25		04/17/2013		Karthik Rajendran		Added: rdsData parameter is passed to assignTrucker().
 *  A26		04/18/2013		Karthik Rajendran		Fix: Available date check for primary vvd.
 *  A27		08/28/2013		Karthik Rajendran		Added: dischPort correction logic added to supplemental process.
 *  A28		10/04/2013		Karthik Rajendran		Added: New RDS Proc change - Get primary vvd from App Param table and execute.
 *  A29		01/21/2014		Karthik Rajendran		Added: Calling required combine_autos logical implementations methods for consignee, commodity.
 *  A30		01/31/2014		Karthik Rajendran		Added: If LoadType from BRDS is "E" then set EmptyFull="E"
 *  A31		02/03/2014		Karthik Rajendran		Added: Use combineAutos() for supp proc and assign calculated cnee to cnee field
 *
 */

public class RDSMessageProcessor extends AbstractFileProcessor
{

	private static final String MULTISTOP_SIT = "MULTISTOP SIT";
	private static Logger				logger			= Logger.getLogger(RDSMessageProcessor.class);
	private String						contents;
	private ArrayList<String>			contentLines;
	private List<XmlFields>				xmlFields;
	private ArrayList<RDSFields>		rdsData;
	public ArrayList<String>			vesselVoyageList;
	public ArrayList<TosRdsDataFinalMt>	additionalCtrFromHon;
	public ArrayList<TosStowPlanCntrMt>	ocrDataList;
	public static final String			PRIMARY			= "primary";
	public static final String			SUPPLEMENT		= "supplement";
	private static TosLookup			lookUp			= null;
	//private static String				userPrefVVD		= null;
	public Date							triggerDate		= null;
	private NewVesselLogger				nvLogger		= NewVesselLogger.getInstance();				;
	String								ftpFileName		= null;
	int									ftpProxyId		= -1;
	int									ftpProxyArchId	= -1;
	boolean								isFileProcessed	= false;
	private String 						errorLines		= null;

	public RDSMessageProcessor(String userEntrdVVD)
	{
		try
		{
			lookUp = new TosLookup();
			//userPrefVVD = userEntrdVVD;
			//logger.info("User preferred VVD: " + userPrefVVD);
			processFiles();
			nvLogger.sendCHErrors();
			nvLogger.sendNewVessErrors();
			//userPrefVVD = null;
		}
		catch (Exception e)
		{
			logger.error("Failed to create TosLookup in RDSMessageProcessor");
		}
		finally
		{
			logger.info("Cleaning up of static variables start");
			CommonBusinessProcessor.outboundExportVesselMap = null;
			CommonBusinessProcessor.outboundNIVesselMap = null;
			CommonBusinessProcessor.outboundVesselMap = null;
			CommonBusinessProcessor.portCodeTradeMap = null;
			CommonBusinessProcessor.arrivalDateMap = null;
			CommonBusinessProcessor.supProblemsList = null;
			logger.info("Cleaning up of static variables end");
		}
	}

	public void processFiles()
	{
		try
		{
			ftpProxyId = Integer.parseInt(TosRefDataUtil.getValue("RDS_IN_FTP_ID"));
			ftpProxyArchId = Integer.parseInt(TosRefDataUtil.getValue("RDS_ARCH_FTP_ID"));
			int timeout = Integer.parseInt(TosRefDataUtil.getValue("FTP_TIMEOUT"));
			logger.debug("FTP timeout retrieved is: " + timeout);
			
			FtpProxyListBiz list = new FtpProxyListBiz();
			list.setTimeout(timeout);
			String[] rdsFiles = list.getFileNames(ftpProxyId, null, null);
			// String[] gumRdsFiles = list.getFileNames(ftpProxyId, null, null);
			if (rdsFiles == null || rdsFiles.length <= 0)
				return;
			List<String> rdsFilesList = (List<String>) Arrays.asList(rdsFiles);
			Collections.sort(rdsFilesList, Collections.reverseOrder());
			/*
			 * if(rdsFiles==null || rdsFiles.length <= 0) {
			 * nvLogger.addFileError("",
			 * "No download file found in the FTP location."); throw new
			 * Exception("No download file found in the FTP location."); }
			 */
			FtpProxyGetterBiz getter = new FtpProxyGetterBiz();
			getter.setTimeout(timeout);
			//
			xmlFields = CommonBusinessProcessor.getFields("/newvesXml/RDS.xml");
			triggerDate = new Date();
			
			logger.debug("ftpProxyId : " + ftpProxyId + "  and ftpProxyArchId : "+ ftpProxyArchId);
			logger.debug("Number of RDS Files is : " + rdsFilesList.size());
			for (int i = 0; rdsFilesList != null && i < rdsFilesList.size(); i++)
			{
				ftpFileName = rdsFilesList.get(i);
				logger.debug("Processing file: " + ftpFileName);
				if (-1 == ftpFileName.indexOf("."))
				{
					continue;
				}
				String contents = "";
				String startWFTime = (new SimpleDateFormat("MM-dd-yyyy_HH-mm-ss")).format(new Date()).toString();
				vesselVoyageList = new ArrayList<String>();
				rdsData = new ArrayList<RDSFields>();
				//
				isFileProcessed = false;
				if (ftpFileName.equalsIgnoreCase("BRDSINAK.000")) {
					contents = getter.getFileText(ftpProxyId, ftpFileName);
					CommonBusinessProcessor.archivetoFTP(contents, ftpProxyArchId, ftpFileName + "_" + startWFTime, null);
					processFile(contents);
					if (isFileProcessed) {
						//Enable IS_RDS_JOB_ON Permanently 
						//NewVesselDao.clearScheduleNewves();
					}
				}
				//
				if (isFileProcessed)
				{
					CommonBusinessProcessor.deleteFtpFiles(ftpProxyId, ftpFileName, null);
				}
			}
			
			
		}
		catch (FtpBizException ftpEx)
		{
			logger.error("FTP error found: ", ftpEx);
			nvLogger.addFtpError("" + ftpProxyId, "FTP ERROR: Unable to get into FTP");
		}
		catch (Exception e)
		{
			logger.error("Exception: ", e);
			e.printStackTrace();
		}
		finally
		{
			cleanUp();
			CommonBusinessProcessor.cleanUp();
		}

	}

	public void startRDSProcessTransformations(ArrayList<String> primaryVesselVoyageList, ArrayList<TosRdsDataMt> primaryCntrList, ArrayList<TosRdsDataMt> supplementalCntrList)
	{
		logger.info(" Primary and supplemental VVD size before :"+primaryVesselVoyageList.size()+" - "+supplementalCntrList.size());
		if (primaryVesselVoyageList.size() > 0)
		{
			boolean isProcessed = startPrimaryVesselProc(primaryVesselVoyageList, primaryCntrList);
			if (isProcessed)
				new NewvesProcessorHelper().startNewVessProc(primaryVesselVoyageList, triggerDate);
			else {
				isFileProcessed = false; // If primary data didn't get processed then stop processing the file.
				return;
			}
		}
		if (supplementalCntrList.size() > 0)
		{
			startSupplementalProcNew(supplementalCntrList);
		}
		logger.info(" Primary and supplemental VVD size After :"+primaryVesselVoyageList.size()+" - "+supplementalCntrList.size());
		if (primaryVesselVoyageList.size() ==0 && supplementalCntrList.size() == 0) {
			isFileProcessed = false;
		}else {
			isFileProcessed = true;
		}
		
	}

	public boolean startPrimaryVesselProc(ArrayList<String> primaryVesselVoyageList, ArrayList<TosRdsDataMt> rdsData)//ArrayList<TosRdsDataMt> rawRrdsData)
	{
		logger.info("BEGIN:startPrimaryVesselProc");
		for (int i = 0; i < primaryVesselVoyageList.size(); i++)
		{
			String vesvoy = primaryVesselVoyageList.get(i);
			if (vesvoy != null)
			{
				vesvoy = vesvoy.substring(0, 6);
				/*logger.info("Processing starts for ---> "+vesvoy);
				ArrayList<TosRdsDataMt> rdsData = new ArrayList<TosRdsDataMt>();
				for(int r=0; r<rawRrdsData.size(); r++) {
					TosRdsDataMt tempRds = rawRrdsData.get(r);
					String tempVesvoy = tempRds.getVes() + tempRds.getVoy();
					if(tempVesvoy.equalsIgnoreCase(vesvoy))
						rdsData.add(tempRds);
				}
				logger.info("Total RDS data : "+rawRrdsData.size());
				logger.info("RDS data for "+vesvoy+" : "+rdsData.size());*/
				boolean isTransformed = transformRdsDataToRdsFinal(vesvoy, PRIMARY, rdsData);
				if (!isTransformed)
					return false;
				logger.info("Primary RDS data has been transformed to RDS Final table.");
				ArrayList<TosRdsDataFinalMt> rdsDataFinal = NewVesselDao.getRdsDataFinalForVesvoy(vesvoy, triggerDate);

				rdsDataFinal = CommonBusinessProcessor.dscTransformation(rdsDataFinal);
				rdsDataFinal = CommonBusinessProcessor.mdcChk(rdsDataFinal, rdsData);
				rdsDataFinal = CommonBusinessProcessor.dischargePortTransformation(rdsDataFinal);
				if(rdsDataFinal==null) {// If rob transformation didn't succeed then stop processing
					logger.error("Newves processing stopped here, because ROB Transformation didn't succeed.");
					return false;
				}
				rdsDataFinal = CommonBusinessProcessor.commodityTransformation(rdsDataFinal);
				//rdsDataFinal = CommonBusinessProcessor.dsTransformationCON2CY(rdsDataFinal); // A29
				rdsDataFinal = CommonBusinessProcessor.cargoNotesTransformation(rdsDataFinal);
				rdsDataFinal = CommonBusinessProcessor.fixMtys(rdsDataFinal);
				//rdsDataFinal = CommonBusinessProcessor.setCUStoGtrade(rdsDataFinal);
				rdsDataFinal = CommonBusinessProcessor.removeGFHold(rdsDataFinal);
				rdsDataFinal = CommonBusinessProcessor.processAutos(rdsDataFinal);
				rdsDataFinal = CommonBusinessProcessor.fixHonContainers(rdsDataFinal, this.additionalCtrFromHon);
				rdsDataFinal = CommonBusinessProcessor.emptyLiveStockTransformation(rdsDataFinal);
				rdsDataFinal = CommonBusinessProcessor.crstatusHoldsCheck1(rdsDataFinal);
				//rdsDataFinal = CommonBusinessProcessor.crstatusHoldsCheck2(rdsDataFinal);
				rdsDataFinal = CommonBusinessProcessor.multiStopDocHold(rdsDataFinal);
				rdsDataFinal = CommonBusinessProcessor.crstatusHoldsCheck3(rdsDataFinal);
				rdsDataFinal = CommonBusinessProcessor.crstatusHoldsCheck4(rdsDataFinal);
				rdsDataFinal = CommonBusinessProcessor.crstatusHoldsCheck5(rdsDataFinal);
				rdsDataFinal = CommonBusinessProcessor.checkIDStrapFlatR(rdsDataFinal);
				rdsDataFinal = CommonBusinessProcessor.crstatusHoldsCheck6(rdsDataFinal, this.ocrDataList);
				rdsDataFinal = CommonBusinessProcessor.updatePlanDispForHorizonLines(rdsDataFinal);
				rdsDataFinal = CommonBusinessProcessor.trashContainersTransformation(rdsDataFinal);
				rdsDataFinal = CommonBusinessProcessor.gearContainersTransformation(rdsDataFinal);
				rdsDataFinal = CommonBusinessProcessor.stowRestrictionCodeTransformation(vesvoy, rdsDataFinal);
				//not needed for ALASKA
				//rdsDataFinal = CommonBusinessProcessor.assignTrucker(rdsDataFinal, rdsData);
				rdsDataFinal = CommonBusinessProcessor.updateConsignee(rdsDataFinal, rdsData);
				rdsDataFinal = CommonBusinessProcessor.updateBlankTrades(rdsDataFinal);
				rdsDataFinal = CommonBusinessProcessor.calculateLastFreeDayDueDate(rdsDataFinal);
				//D031688 start
				rdsDataFinal = CommonBusinessProcessor.commodityTransformation2(rdsDataFinal);
				//D031688 end
				// rdsDataFinal =
				// CommonBusinessProcessor.fixIncorrectDischargePort(rdsDataFinal,
				// vesvoy);
				//
				NewVesselDao.updateRdsDataFinal(rdsDataFinal);
				this.additionalCtrFromHon = null;
				//
				NewVesselDao.updateALSByDcmCheck(vesvoy, triggerDate);
			}
		}
		logger.info("END:startPrimaryVesselProc");
		return true;
	}
	//
	public void startSupplementalProc(ArrayList<TosRdsDataMt> supplementalCntrList)
	{
		logger.info("Supplemental process - Begin");
		ArrayList<TosRdsDataMt> rawRdsData = supplementalCntrList;
		supplementalCntrList = CommonBusinessProcessor.combineShipmentNoCreditStatus(supplementalCntrList);
		supplementalCntrList = CommonBusinessProcessor.eliminateDuplicatesInRDS(supplementalCntrList);
		//
		List<String> cntrList = new ArrayList<String>();
		ArrayList<TosRdsDataFinalMt> oldSupDataList = new ArrayList<TosRdsDataFinalMt>();
		ArrayList<TosRdsDataFinalMt> newSupDataList = new ArrayList<TosRdsDataFinalMt>();
		ArrayList<TosRdsDataFinalMt> newContainersInSupDataList = new ArrayList<TosRdsDataFinalMt>();
		HashMap<String, String> serviceMap = new HashMap<String, String>();
		for (int c = 0; c < supplementalCntrList.size(); c++)
		{
			TosRdsDataMt rdsD = supplementalCntrList.get(c);
			cntrList.add(rdsD.getCtrno());
			String vesvoy = rdsD.getVes() + rdsD.getVoy();
			String vesselService = null;
			if(serviceMap.containsKey(vesvoy)) {
				vesselService = serviceMap.get(vesvoy);
			} else {
				// gettting vessel service
				try
				{
					if (lookUp == null)
						lookUp = new TosLookup();
				}
				catch (Exception ex1)
				{
					logger.error("Failed to create TosLookup in transformRdsDataToRdsFinal");
				}
				vesselService = lookUp.getVesselService(vesvoy);
				logger.info("vesselService is : " + vesselService);
				// getting service end
				serviceMap.put(vesvoy, vesselService);
			}
			vesselService = vesselService==null?"":vesselService;
			newSupDataList.add(populateTosRdsDataFinalMtSupplemental(rdsD, vesselService));
		}
		List[] spList = CommonBusinessProcessor.splitList(cntrList, 100);
		ArrayList<TosRdsDataFinalMt> tempList = null;
		for (int l = 0; l < spList.length; l++)
		{
			List<String> cList = (List<String>) spList[l];
			tempList = NewVesselDao.getRdsDataFinalForContainers(cList);
			if (tempList != null)
			{
				oldSupDataList.addAll(tempList);
			}
		}
		logger.info("newSupDataList " + newSupDataList.size());
		logger.info("oldSupDataList " + oldSupDataList.size());
		// Get type code from TOS for new containers from supplemental data
		// If the type code returned is not valid then remove the container from
		// the list
		GEMSEquipment gemsEqp = new GEMSEquipment();
		Iterator<TosRdsDataFinalMt> itr = newSupDataList.iterator();
		for (; itr.hasNext();)
		{
			TosRdsDataFinalMt newRdsFd = itr.next();
			String nCtrno = newRdsFd.getContainerNumber();
			String nChkDigit = newRdsFd.getCheckDigit();
			boolean containerFound = false;
			for (int o = 0; o < oldSupDataList.size(); o++)
			{
				TosRdsDataFinalMt oldRdsFd = oldSupDataList.get(o);
				String oContainerNumber = oldRdsFd.getContainerNumber();
				if (nCtrno.equals(oContainerNumber))
				{
					containerFound = true;
					logger.info("Setting typecode : " + nCtrno + " " + oldRdsFd.getTypeCode());
					newRdsFd.setTypeCode(oldRdsFd.getTypeCode());
				}
			}
			if (!containerFound)
			{
				String typeCode = "";
				try
				{
					if (lookUp == null)
						lookUp = new TosLookup();
					HashMap<String, String> typeCodeMap = lookUp.getEquipmentType(nCtrno , nChkDigit);
					if (typeCodeMap != null && typeCodeMap.size() > 0)
					{
						String type = typeCodeMap.get("equipmentType");
						type = type == null ? "" : type;
						// String heightMm = typeCodeMap.get("equipmentHgtMm");
						// heightMm = heightMm==null?"":heightMm;
						String strength = typeCodeMap.get("equipmentStrength");
						// strength = strength==null?"IA":strength;
						String tareKg = typeCodeMap.get("equipmentTareKg");
						tareKg = tareKg == null ? "" : tareKg;
						String material = typeCodeMap.get("equipmentMaterial");
						material = material == null ? "" : material;
						String nomHeight = typeCodeMap.get("equipmentNomHeight");
						nomHeight = nomHeight == null ? "" : nomHeight;
						String heightMm = CommonBusinessProcessor.constructHeightMm(nomHeight);
						if (type.length() >= 3 && heightMm.length() >= 4 && material.length() > 0)
						{
							typeCode = CommonBusinessProcessor.constructTypeCode(type, heightMm, material);
							// logger.info("Constructed Type code for "+nCtrno+" :"+typeCode);
							newRdsFd.setTypeCode(typeCode);
							double tareWeightDbl = Double.parseDouble(tareKg);
							String tareWeightStr = CommonBusinessProcessor.convertKGToPounds(tareWeightDbl);
							newRdsFd.setTareWeight(new BigDecimal(tareWeightStr));
							newRdsFd.setStrength(strength);
							newRdsFd.setHgt(heightMm);
						}
					}
				}
				catch (Exception e)
				{
					logger.error("TypeCode Lookup error:", e);
				}
				if ((typeCode.equals("")) || (!typeCode.equals("") && typeCode.length() != 8))
				{
					/*
					 * gemsEqp =
					 * CommonBusinessProcessor.getEquipmentAttributesFromGems
					 * (nCtrno); if(gemsEqp!=null) { typeCode =
					 * gemsEqp.getType(); } else {
					 * logger.info("GEMS Object error : null"); }
					 * if(typeCode!=null && !typeCode.equals("")) {
					 * logger.info("Type code from GEMS for "+nCtrno + " "+
					 * typeCode); logger.info("getCode : "+gemsEqp.getCode());
					 * logger.info("getId : "+gemsEqp.getId());
					 * newRdsFd.setTypeCode(typeCode); } else {
					 * logger.info("No Type code from GEMS for "+nCtrno);
					 * itr.remove(); }
					 */
					itr.remove();
					nvLogger.addError(newRdsFd.getVesvoy() + newRdsFd.getLeg(), newRdsFd.getContainerNumber(), " TypeCode is missing/invalid");
				}
			}
		}
		// Applying Transformations
		if (newSupDataList != null)
		{
			// newSupDataList =
			// CommonBusinessProcessor.cargoNotesTransformation(newSupDataList);
			newSupDataList = CommonBusinessProcessor.dsTransformationCON2CY(newSupDataList);
			newSupDataList = CommonBusinessProcessor.crstatusHoldsCheck1(newSupDataList);
			newSupDataList = CommonBusinessProcessor.crstatusHoldsCheck4(newSupDataList);
			newSupDataList = CommonBusinessProcessor.updateConsigneeSupp(newSupDataList, rawRdsData);
			newSupDataList = CommonBusinessProcessor.updateBlankTrades(newSupDataList);
		}
		//
		ArrayList<TosRdsDataFinalMt> updatedSupDataList = CommonBusinessProcessor.supplementalValidations(oldSupDataList, newSupDataList);
		updatedSupDataList = CommonBusinessProcessor.cargoNotesTransformationSup(updatedSupDataList, triggerDate);
		//
		if (updatedSupDataList != null && updatedSupDataList.size() > 0)
		{
			NewVesselDao.updateSupRdsDataFinal(updatedSupDataList);
			new NewvesProcessorHelper().startSupplementalProc(updatedSupDataList, CommonBusinessProcessor.supProblemsList);
		}
		else
		{
			new NewvesProcessorHelper().generateSupplementalReports(CommonBusinessProcessor.supProblemsList);
			logger.info("Supplemental Data found in RDS, but nothing gets processed. Please check for the supplemental problems.");
			nvLogger.addFileError(ftpFileName, "Supplemental Data found in RDS, but nothing gets processed. Please check for the supplemental problems.");
		}
		//
		logger.info("Supplemental process - End");
	}
	//
	public void startSupplementalProcNew(ArrayList<TosRdsDataMt> supplementalCntrList)
	{
		logger.info("~~~~Supplemental process*NEW - Begin");
		ArrayList<TosRdsDataMt> rawRdsDataCopy = new ArrayList<TosRdsDataMt>();
		for (TosRdsDataMt rds : supplementalCntrList)
		{
			rawRdsDataCopy.add(rds.clone());
		}
		supplementalCntrList = CommonBusinessProcessor.combineAutos(supplementalCntrList, rawRdsDataCopy); // A31
		supplementalCntrList = CommonBusinessProcessor.combineShipmentNoCreditStatus(supplementalCntrList);
		supplementalCntrList = CommonBusinessProcessor.eliminateDuplicatesInRDS(supplementalCntrList);
		logger.info("~~~~Constructing RDSDataFinal Object for supplemental containers");
		ArrayList<TosRdsDataFinalMt> newSupDataList = new ArrayList<TosRdsDataFinalMt>();
		HashMap<String, String> serviceMap = new HashMap<String, String>();
		for (int c = 0; c < supplementalCntrList.size(); c++)
		{
			TosRdsDataMt rdsD = supplementalCntrList.get(c);
			String vesvoy = rdsD.getVes() + rdsD.getVoy();
			String vesselService = null;
			// getting vessel service
			if(serviceMap.containsKey(vesvoy)) {
				vesselService = serviceMap.get(vesvoy);
			} else {
				try
				{
					if (lookUp == null)
						lookUp = new TosLookup();
					vesselService = lookUp.getVesselService(vesvoy);
					logger.info(vesvoy+" vesselService is : " + vesselService);
					serviceMap.put(vesvoy, vesselService);
				}
				catch (Exception ex1)
				{
					logger.error("Failed to create TosLookup in startSupplementalProcNew for vesselService", ex1);
					nvLogger.addSuppError(vesvoy, rdsD.getCtrno(), "Failed to create TosLookup in startSupplementalProcNew for vesselService<br/>"+ex1.getMessage()); 
				}
			}
			vesselService = vesselService==null?"":vesselService;
			
			//Checking for data source and skipping the record from supplemental if data source is any thing other than "L".
			String hsf6Local = rdsD.getDatasource();
			if (hsf6Local != null)
				hsf6Local = hsf6Local.equalsIgnoreCase("L") ? "" : hsf6Local;
			if (!"".equalsIgnoreCase(hsf6Local) && !"7".equalsIgnoreCase(hsf6Local)) {
				continue;
			}
			// skipping logic ends here
			
			newSupDataList.add(populateTosRdsDataFinalMtSupplemental(rdsD, vesselService));
		}
	/*	logger.info("~~~~Removing containers from supp list where HSF6<> blank && HSF6<>7");
		Iterator<TosRdsDataFinalMt> itrSupp = newSupDataList.iterator();
		for(;itrSupp.hasNext();) {
			TosRdsDataFinalMt newRdsFd = itrSupp.next();
			String hsf6 = newRdsFd.getHsf6();
			hsf6 = hsf6==null?"":hsf6;
			if(!"".equals(hsf6) && !"7".equals(hsf6)) {
				logger.info("Removed - CTRNO="+newRdsFd.getContainerNumber()+", HSF6="+newRdsFd.getHsf6());
				itrSupp.remove();
			}
		}*/
		// Setting type code from TOS
		logger.info("~~~~Setting type code from TOS for supplemental containers");
		Iterator<TosRdsDataFinalMt> itr = newSupDataList.iterator();
		for (; itr.hasNext();)
		{
			TosRdsDataFinalMt newRdsFd = itr.next();
			String nCtrno = newRdsFd.getContainerNumber();
			String nChkDigit = newRdsFd.getCheckDigit();

			String typeCode = "";
			try
			{
				if (lookUp == null)
					lookUp = new TosLookup();
				HashMap<String, String> typeCodeMap = lookUp.getEquipmentType(nCtrno,nChkDigit);
				if (typeCodeMap != null && typeCodeMap.size() > 0)
				{
					String type = typeCodeMap.get("equipmentType");
					type = type == null ? "" : type;
					// String heightMm = typeCodeMap.get("equipmentHgtMm");
					// heightMm = heightMm==null?"":heightMm;
					String strength = typeCodeMap.get("equipmentStrength");
					// strength = strength==null?"IA":strength;
					String tareKg = typeCodeMap.get("equipmentTareKg");
					tareKg = tareKg == null ? "" : tareKg;
					String material = typeCodeMap.get("equipmentMaterial");
					material = material == null ? "" : material;
					String nomHeight = typeCodeMap.get("equipmentNomHeight");
					nomHeight = nomHeight == null ? "" : nomHeight;
					String heightMm = CommonBusinessProcessor.constructHeightMm(nomHeight);
					if (type.length() >= 3 && heightMm.length() >= 4 && material.length() > 0)
					{
						typeCode = CommonBusinessProcessor.constructTypeCode(type, heightMm, material);
						// logger.info("Constructed Type code for "+nCtrno+" :"+typeCode);
						newRdsFd.setTypeCode(typeCode);
						logger.info("Setting TypeCode for "+nCtrno+" : "+typeCode);
						double tareWeightDbl = Double.parseDouble(tareKg);
						String tareWeightStr = CommonBusinessProcessor.convertKGToPounds(tareWeightDbl);
						newRdsFd.setTareWeight(new BigDecimal(tareWeightStr));
						newRdsFd.setHgt(heightMm);
					}
				}
			}
			catch (Exception e)
			{
				logger.error("TypeCode Lookup error:", e);
				nvLogger.addSuppError(newRdsFd.getVesvoy() + newRdsFd.getLeg(), newRdsFd.getContainerNumber(),"TypeCode Lookup error:<br/>"+e.getMessage());
			}
			if ((typeCode.equals("")) || (!typeCode.equals("") && typeCode.length() != 8))
			{
				itr.remove();
				nvLogger.addSuppError(newRdsFd.getVesvoy() + newRdsFd.getLeg(), newRdsFd.getContainerNumber(), " TypeCode is missing/invalid");
			}
		}
		logger.info("~~~~Applying  Supplemental Transformations");
		if (newSupDataList != null)
		{
			
			newSupDataList = CommonBusinessProcessor.dsTransformationCON2CY(newSupDataList);
			newSupDataList = CommonBusinessProcessor.crstatusHoldsCheck1(newSupDataList);
			newSupDataList = CommonBusinessProcessor.crstatusHoldsCheck4(newSupDataList);
			newSupDataList = CommonBusinessProcessor.updateConsigneeSupp(newSupDataList, rawRdsDataCopy);
			newSupDataList = CommonBusinessProcessor.updateBlankTrades(newSupDataList);
		}
		
		
		//
		logger.info("~~~~~~Supplemental Validations-Begin");
		ArrayList<TosRdsDataFinalMt> outputSupDataList = CommonBusinessProcessor.supplementalValidationsNew(newSupDataList);
		logger.info("~~~~~~Supplemental Validations-End");
		//
		
		outputSupDataList = CommonBusinessProcessor.multiStopDocHoldSupplemental(outputSupDataList);
		

		if (outputSupDataList != null && outputSupDataList.size() > 0)
		{
			logger.info("~~~~Supplemental containers to TXT : "+outputSupDataList.size());
			outputSupDataList = CommonBusinessProcessor.cargoNotesTransformationSup(outputSupDataList, triggerDate);
			new NewvesProcessorHelper().startSupplementalProc(outputSupDataList, CommonBusinessProcessor.supProblemsList);
			
		}
		else
		{
			new NewvesProcessorHelper().generateSupplementalReports(CommonBusinessProcessor.supProblemsList);
			logger.info("Supplemental Data found in RDS, but nothing gets processed. Please check for the supplemental problems.");
			nvLogger.addFileError(ftpFileName, "Supplemental Data found in RDS, but nothing gets processed. Please check for the supplemental problems.");
		}
		//
		//
		logger.info("~~~~Supplemental process*NEW - End");
	}
	//
	public void processFile(String contents)
	{
		errorLines = "";
		contentLines = new ArrayList<String>(Arrays.asList(contents.split("\n")));
		//logger.debug(" processFile method " + contentLines.size());
		if (contentLines.size() >= 1)
		{
			logger.debug("Processing raw RDS data...");
			for (int i = 0; i < contentLines.size(); i++)
			{
				try
				{
					String line = contentLines.get(i);
					//logger.debug("line " + i + "  "+line);
					line = line.replaceAll("\\x00", " ");
					if (!line.startsWith("") && !line.startsWith(" "))
						processLine(line, 0);
					else if (line.startsWith(" "))
					{
						errorLines = errorLines + line + "\n\n";
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
					logger.error("Error: Unable to process the RDS file. Please verify the file errors.");
					//logger.error("Error processing the line " + i);
					nvLogger.addFileError(ftpFileName, "Unable to process the RDS file. <br/><br/>"+ e.getMessage());
					return;
				}
			}
			if(errorLines.length() > 0) {
				String message = "VVD not found in the records, unable to parse the below lines <br/><br/>";
				String errorMessage = message + "<pre>" + errorLines + "</pre><br/><br/>";
				nvLogger.addFileError(ftpFileName, errorMessage);
			}
			logger.info("Incoming RDS Data size: " + rdsData.size());
			//
			ArrayList<String> primaryVesselVoyageList = new ArrayList<String>();
			ArrayList<String> supplementalVesselVoyageList = new ArrayList<String>();
			ArrayList<String> eastBoundVoyageList = new ArrayList<String>();
			// Separate any eastbound vvd from vvd list
			Iterator<String> itrVvList = vesselVoyageList.iterator();
			for (; itrVvList.hasNext();)
			{
				String vvd = itrVvList.next();
				if (vvd.endsWith("E"))
				{
					eastBoundVoyageList.add(vvd);
					itrVvList.remove();
				}
			}
			// A28
			String userPrefVvds = NewVesselDao.getUserPrefVvdFromAppParam();
			if(userPrefVvds!=null && userPrefVvds.length()>0) {
				ArrayList<String> userPrefVvdList = new ArrayList<String>(Arrays.asList(userPrefVvds.split(",")));
				for(int u=0; u<userPrefVvdList.size(); u++) {
					String userPrefVvd = userPrefVvdList.get(u);
					if (vesselVoyageList.contains(userPrefVvd)) {
						String vesselService = CommonBusinessProcessor.getVesselService(userPrefVvd);						
						if(vesselService==null || vesselService.equals("")) {							
							nvLogger.addError(userPrefVvd, null, "Process stopped, because unable to get vessel service for "+userPrefVvd);
							return;
						}
						if("GCS".equalsIgnoreCase(vesselService)) {
							String ebVvd = CommonBusinessProcessor.determineEastBoundVvd(userPrefVvd);
							if(ebVvd==null) {
								nvLogger.addError(userPrefVvd, "", "Process stopped, because unable to determine eastbound vvd for this "+userPrefVvd+" GCS vessel");
								return;
							}
							if(!NewVesselDao.verifyCHProcessExecution(ebVvd)) {
								nvLogger.addChExecError(userPrefVvd, "Please process BRDSINCH.000 file before scheduling newves.");
								return;
							}
						}
						String vesselOpr = "";
						// If available date for primary vessel is blank, then stop
						// the process.
						try
						{
							if (lookUp == null)
								lookUp = new TosLookup();
						}
						catch (Exception ex1)
						{
							logger.error("Failed to create TosLookup in transformRdsDataToRdsFinal");
						}
						String availableDate = lookUp.getBeginReceive(userPrefVvd,"ANK");
						if ("".equalsIgnoreCase(availableDate) || availableDate == null)
						{
							nvLogger.sendAvailDateNotification(userPrefVvd, availableDate, "rds");
							logger.info("availableDate is null for "+userPrefVvd);
							continue;
						}
						// end available date validation.
						try
						{
							VesselVO vvo = CommonBusinessProcessor.getVesselDetails(userPrefVvd.substring(0, 3));
							vesselOpr = vvo.getVessOpr();
						}
						catch (Exception e)
						{
							logger.error("Problem in retreiving vessel operator for " + userPrefVvd.substring(0, 3));
						}
						vesselOpr = vesselOpr == null ? "" : vesselOpr;
						logger.info("vesselOpr:" + vesselOpr);
						boolean isStowPlanAvailable = NewVesselDao.isStowPlanAvailForVesvoy(userPrefVvd.substring(0, 6));
						boolean isDcmDataAvailable = NewVesselDao.isDCMAvailForVesselWithVVD(userPrefVvd.substring(0, 6));
						if ((isStowPlanAvailable && isDcmDataAvailable) || (isStowPlanAvailable && vesselOpr.equalsIgnoreCase("CSX")))
						{
							primaryVesselVoyageList.add(userPrefVvd);
							vesselVoyageList.remove(userPrefVvd);
						}
						else
						{
							//nvLogger.addError(userPrefVvd, null, " dont have stowplan/DCM.");
							if (!isStowPlanAvailable)
							{
								nvLogger.addError(userPrefVvd, null, " dont have stowplan.");
							}
							if (!isDcmDataAvailable)
							{
								nvLogger.addError(userPrefVvd, null, " dont have DCM.");
							}
							vesselVoyageList.remove(userPrefVvd);
						}
					} 
					else {
						nvLogger.addError(userPrefVvd, null, " is not available in the RDS.");
					}
				}
			} 
			// All user prefered vvd are validated and added to primary list, 
			// now the vvd list will only have supplemental, perhaps pre-triggered, primary in case 
			if(vesselVoyageList!=null && vesselVoyageList.size()>0) {
				Iterator<String> itrVvd = vesselVoyageList.iterator();
				for (; itrVvd.hasNext() ; )
				{
					String vvd = itrVvd.next();
					String vv = vvd.substring(0, 6);
					boolean isSupplement = false;// NewVesselDao.isPrimaryVesvoy(vv);
					try
					{
						TosProcessLoggerDAO tDao = new TosProcessLoggerDAO();
						isSupplement = tDao.verifyProcessExecution(vv, TosProcessLogger.PROCESS_NEWVES);
					}
					catch (Exception e)
					{
						e.printStackTrace();
						logger.error("Error while checking Tos_Process_Logger_Mt \n" + e);
					}
					if (isSupplement)
					{
						supplementalVesselVoyageList.add(vvd);
						logger.info(vvd + " added to supplemental list");
						itrVvd.remove();
					}
					else
					{
						String vesselOpr = "";

						boolean isStowPlanAvailable = NewVesselDao.isStowPlanAvailForVesvoy(vv.substring(0, 6));
						boolean isDcmDataAvailable = NewVesselDao.isDCMAvailForVesselWithVVD(vv.substring(0, 6));
						try
						{
							VesselVO vvo = CommonBusinessProcessor.getVesselDetails(vv.substring(0, 3));
							vesselOpr = vvo.getVessOpr();
						}
						catch (Exception e)
						{
							e.printStackTrace();
							logger.error("Problem in retreiving vessel operator for " + vv.substring(0, 3));
						}
						vesselOpr = vesselOpr == null ? "" : vesselOpr;
						if (!vesselOpr.equalsIgnoreCase("CSX") && (isStowPlanAvailable || isDcmDataAvailable))
						{
							if (!isStowPlanAvailable)
							{
								nvLogger.addError(vv, null, " dont have stowplan.");
								continue;
							}
							if (!isDcmDataAvailable)
							{
								nvLogger.addError(vv, null, " dont have DCM.");
								continue;
							}
							primaryVesselVoyageList.add(vvd);
							logger.info(vvd + " added to primary");
						}
						else if (vesselOpr.equalsIgnoreCase("CSX") && isStowPlanAvailable)
						{
							primaryVesselVoyageList.add(vvd);
							logger.info(vvd + " added to primary");
						}
						else
						{
							logger.info(vvd + " Ignored bcz its pre-triggered.");
							nvLogger.addError(vvd, null, " Ignored because its pre-triggered.");
						}
					}
				}
			}
			// Available date validation to be done once again in case primary vvd is added in 
			// the above supp/pretrigger identification logics.
			logger.info("Available date vaildation for primary vvds again start");
			Iterator<String> itrPrimary = primaryVesselVoyageList.iterator();
			for (; itrPrimary.hasNext() ;)
			{
				String vv = itrPrimary.next();
				// Do one more ch file check for GCS vessel, if it was not found in userprefvvd
				String vesselService = CommonBusinessProcessor.getVesselService(vv);						
				if(vesselService==null || vesselService.equals("")) {							
					nvLogger.addError(vv, null, "Process skiped, because unable to get vessel service for "+vv);
					itrPrimary.remove();
				}
				if("GCS".equalsIgnoreCase(vesselService)) {
					String ebVvd = CommonBusinessProcessor.determineEastBoundVvd(vv);
					if(ebVvd==null) {
						nvLogger.addError(vv, "", "Process skiped, because unable to determine eastbound vvd for this "+vv+" GCS vessel");
						itrPrimary.remove();
					}
					if(!NewVesselDao.verifyCHProcessExecution(ebVvd)) {
						nvLogger.addChExecError(vv, "Please process BRDSINCH.000 file before scheduling newves.");
						itrPrimary.remove();
					}
				}
				// If available date for primary vessel is blank, then stop the
				// process.
				try
				{
					if (lookUp == null)
						lookUp = new TosLookup();
				}
				catch (Exception ex1)
				{
					logger.error("Failed to create TosLookup in transformRdsDataToRdsFinal");
				}
				String availableDate = lookUp.getBeginReceive(vv,"ANK");
				if ("".equalsIgnoreCase(availableDate) || availableDate == null)
				{
					nvLogger.sendAvailDateNotification(vv, availableDate, "rds");
					itrPrimary.remove();
					logger.info("availableDate is null for "+vv);
				}
				// end available date validation.
			}
			logger.info("Available date vaildation for primary vvds again end");
			//
			logger.info("Primary vv : " + primaryVesselVoyageList.size());
			logger.info(primaryVesselVoyageList);
			logger.info("Eastbound vv : " + eastBoundVoyageList.size());
			logger.info(eastBoundVoyageList);
			logger.info("Supplemental vv : " + supplementalVesselVoyageList.size());
			logger.info(supplementalVesselVoyageList);
			ArrayList<TosRdsDataMt> primaryCntrList = buildPrimaryCntrList(primaryVesselVoyageList, eastBoundVoyageList, rdsData);
			logger.info("Primary containers : " + primaryCntrList.size());
			ArrayList<TosRdsDataMt> supplementalCntrList = buildSupplementalCntrList(supplementalVesselVoyageList, rdsData);
			logger.info("Supplemental containers : " + supplementalCntrList.size());
			NewVesselDao.insertRDSData(primaryCntrList);
			NewVesselDao.insertRDSData(supplementalCntrList);
			startRDSProcessTransformations(primaryVesselVoyageList, primaryCntrList, supplementalCntrList);
			// Once we have processed all primary & supplemental data, lets see
			// if we still have any eastbound data.
			if (eastBoundVoyageList != null && eastBoundVoyageList.size() > 0 && rdsData != null && rdsData.size() > 0)
			{
				logger.info("Eastbound vv after primary&supp : " + eastBoundVoyageList.size());
				logger.info(eastBoundVoyageList);
				logger.info("RDS data after primary&supp : " + rdsData.size());
				saveEastBoundRDSData(rdsData);
			}
		}
		else
		{
			nvLogger.addFileError(ftpFileName, "Error: No data. Check file for errors.");
			return;
		}
	}
	//
	private void processEastBoundFile(String contents)
	{
		logger.info("EastBound data is being processed.");
		contentLines = new ArrayList<String>(Arrays.asList(contents.split("\n")));
		if (contentLines.size() >= 1)
		{
			for (int i = 0; i < contentLines.size(); i++)
			{
				try
				{
					if (!contentLines.get(i).startsWith("") && !contentLines.get(i).startsWith(" "))
						processLine(contentLines.get(i).toString(), 0);
					else
					{
						if (contentLines.get(i).startsWith(" "))
							nvLogger.addFileError(ftpFileName, "Unable to parse the text. Line " + (i + 1) + "\\n\\n" + contentLines.get(i));
					}
				}
				catch (Exception e)
				{
					logger.error("Error: Unable to process the EastBound RDS file. Please verify the file errors.");
					nvLogger.addFileError(ftpFileName, "Unable to parse the text. Line " + (i + 1) + "\\n\\n" + contentLines.get(i));
					return;
				}
			}

			logger.info(rdsData.size());
			//
			try {
				saveEastBoundRDSData(rdsData);
				isFileProcessed = true;
			} catch(Exception e) {
				isFileProcessed = false;
				return;
			}
			for(String vvd: vesselVoyageList) {
				try {
					TosRdsChLoggerMt loggerRecord = new TosRdsChLoggerMt();
					loggerRecord.setVesvoy(vvd);
					loggerRecord.setCreated(new Date());
					loggerRecord.setStatus("Processed");
					NewVesselDao.insertCHProcessExecution(loggerRecord);
				} catch(Exception e) {
					nvLogger.addError(vvd, "", "Unable to insert a logger record for cash hold file execution :- "+e.getMessage());
				}
			}
		}
		else
		{
			nvLogger.addFileError(ftpFileName, "Error: No data. Check file for errors.");
			return;
		}
	}

	private void saveEastBoundRDSData(ArrayList<RDSFields> rdsData)
	{
		logger.info("Saving eastbound data...");
		/*logger.info("Checking other than E containers in case and removing...");
		logger.info("rdsData before-->"+rdsData.size());
		Iterator<RDSFields> itr = rdsData.iterator();
		for(;itr.hasNext();) {
			RDSFields rds = itr.next();
			if(!rds.getLeg().equalsIgnoreCase("E")) {
				logger.info("Removed - "+rds.getContainerNumber()+ ","+rds.getVes() + rds.getVoy() + rds.getLeg());
				itr.remove();
			}
		}
		logger.info("rdsData after-->"+rdsData.size());*/
		ArrayList<TosRdsDataMt> rdsDataList = new ArrayList<TosRdsDataMt>();
		for (int i = 0; i < rdsData.size(); i++)
		{
			RDSFields rds1 = (RDSFields) rdsData.get(i);
			String vvd = rds1.getVes() + rds1.getVoy() + rds1.getLeg();
			if (!vvd.equals("") && vvd.length() == 7)
			{
				TosRdsDataMt rdsD = populateTosRdsDataMt(rds1, vvd);
				rdsDataList.add(rdsD);
			}
		}
		try
		{
			NewVesselDao.insertEbRDSData(rdsDataList);
		}
		catch (Exception e)
		{
			nvLogger.addFileError(ftpFileName, "Unable to persist EastBound RDS data into database. Check the database/file for errors.");
		}
	}
	private ArrayList<TosRdsDataMt> buildSupplementalCntrList(ArrayList<String> supplementalVesselVoyageList, ArrayList<RDSFields> rdsData)
	{
		ArrayList<TosRdsDataMt> supplementalCntrList = new ArrayList<TosRdsDataMt>();
		for (int s = 0; s < supplementalVesselVoyageList.size(); s++)
		{
			String vvd = supplementalVesselVoyageList.get(s);
			if (vvd != null)
			{
				Iterator<RDSFields> itrRds = rdsData.iterator();
				for (; itrRds.hasNext();)
				{
					RDSFields rdsField = itrRds.next();
					String rdsvvd = rdsField.getVes() + rdsField.getVoy() + rdsField.getLeg();
					if (vvd.equalsIgnoreCase(rdsvvd))
					{
						TosRdsDataMt rdsD = populateTosRdsDataMt(rdsField, vvd);
						supplementalCntrList.add(rdsD);
						itrRds.remove();
					}
				}
			}
		}
		//
		return supplementalCntrList;
	}

	private ArrayList<TosRdsDataMt> buildPrimaryCntrList(ArrayList<String> primaryVesselVoyageList, ArrayList<String> eastBoundVoyageList, ArrayList<RDSFields> rdsData)
	{
		ArrayList<TosRdsDataMt> primaryCntrList = new ArrayList<TosRdsDataMt>();
		for (int p = 0; p < primaryVesselVoyageList.size(); p++)
		{
			String vvd = primaryVesselVoyageList.get(p);
			String vessel = "";
			if (vvd != null)
			{
				vessel = vvd.substring(0, 3);
				Iterator<RDSFields> itrRds = rdsData.iterator();
				for (; itrRds.hasNext();)
				{
					RDSFields rdsField = itrRds.next();
					String rdsvvd = rdsField.getVes() + rdsField.getVoy() + rdsField.getLeg();
					if (vvd.equalsIgnoreCase(rdsvvd))
					{
						TosRdsDataMt rdsD = populateTosRdsDataMt(rdsField, vvd);
						primaryCntrList.add(rdsD);
						itrRds.remove();
					}
				}
				// Check if the same vvd vessel has east bound data
				Iterator<String> ebItr = eastBoundVoyageList.iterator();
				for (; ebItr.hasNext();)
				{
					String evvd = ebItr.next();
					if (evvd.startsWith(vessel))
					{
						Iterator<RDSFields> itrRds1 = rdsData.iterator();
						for (; itrRds1.hasNext();)
						{
							RDSFields rdsField = itrRds1.next();
							String rdsvvd = rdsField.getVes() + rdsField.getVoy() + rdsField.getLeg();
							if (evvd.equalsIgnoreCase(rdsvvd))
							{
								TosRdsDataMt rdsD = populateTosRdsDataMt(rdsField, vvd);
								primaryCntrList.add(rdsD);
								itrRds1.remove();
							}
						}
						ebItr.remove();
					}
				}
			}
		}
		//
		return primaryCntrList;
	}

	public void processLine(String line, int lineNumber)
	{
		if (line == null || line.length() <= 0)
			return;
		int beginIndex = 0, endIndex = 0;
		RDSFields brdf = new RDSFields();
		for (int i = 0; i < xmlFields.size(); i++)
		{
			XmlFields fld = (XmlFields) xmlFields.get(i);
			endIndex = new Integer(fld.getFieldLength());
			String temp_str = line.substring(beginIndex, beginIndex + endIndex).trim();
			switch (EnumRDS.valueOf(fld.getFieldName()))
			{
				case ves :
					brdf.setVes(temp_str);
					break;
				case voy :
					brdf.setVoy(temp_str);
					break;
				case leg :
					brdf.setLeg(temp_str);
					break;
				case containerNumber :
					brdf.setContainerNumber(temp_str);
					break;
				case shipmentNo :
					brdf.setShipmentNo(temp_str);
					break;
				case dataSource :
					brdf.setDataSource(temp_str);
					break;
				case routeToPort :
					brdf.setRouteToPort(temp_str);
					break;
				case loadPort :
					brdf.setLoadPort(temp_str);
					break;
				case dischPort :
					brdf.setDischPort(temp_str);
					break;
				case placeDelCode :
					brdf.setPlaceDelCode(temp_str);
					break;
				case temp :
					logger.info("1 temp.."+temp_str);
					 if(temp_str!=null && !temp_str.isEmpty()){

						 temp_str=temp_str.toUpperCase();
						 logger.info("2 temp_str.."+temp_str);
					 }
					brdf.setTemp(temp_str);
					break;
				case loadService :
					brdf.setLoadService(temp_str);
					break;
				case dischService :
					brdf.setDischService(temp_str);
					break;
				case hazf :
					brdf.setHazf(temp_str);
					break;
				case filler1 :
					brdf.setFiller1(temp_str);
					break;
				case cntrOversize :
					brdf.setCntrOversize(temp_str);
					break;
				case primaryCarrier :
					brdf.setPrimaryCarrier(temp_str);
					break;
				case initialVes :
					brdf.setInitialVes(temp_str);
					break;
				case initialVoy :
					brdf.setInitialVoy(temp_str);
					break;
				case initialLeg :
					brdf.setInitialLeg(temp_str);
					break;
				case inbond :
					brdf.setInbond(temp_str);
					break;
				case notify :
					brdf.setNotify(temp_str);
					break;
				case creditStatus :
					brdf.setCreditStatus(temp_str);
					break;
				case transit :
					brdf.setTransit(temp_str);
					break;
				case trade :
					brdf.setTrade(temp_str);
					break;
				case loadType :
					brdf.setLoadType(temp_str);
					break;
				case shipperArolIden :
					brdf.setShipperArolIden(temp_str);
					break;
				case shipperOrgName :
					brdf.setShipperOrgName(temp_str);
					break;
				case shipperOrgNameQual :
					brdf.setShipperOrgNameQual(temp_str);
					break;
				case consigneeArolIden :
					brdf.setConsigneeArolIden(temp_str);
					break;
				case consigneeOrgName :
					brdf.setConsigneeOrgName(temp_str);
					break;
				case consigneeCo :
					brdf.setConsigneeCo(temp_str);
					break;
				case consigneeOrgNameQual :
					brdf.setConsigneeOrgNameQual(temp_str);
					break;
				case consigneeAddrLine1 :
					brdf.setConsigneeAddrLine1(temp_str);
					break;
				case consigneeSuite :
					brdf.setConsigneeSuite(temp_str);
					break;
				case consigneeCity :
					brdf.setConsigneeCity(temp_str);
					break;
				case consigneeState :
					brdf.setConsigneeState(temp_str);
					break;
				case consigneeCountry :
					brdf.setConsigneeCountry(temp_str);
					break;
				case consigneeZipCode :
					brdf.setConsigneeZipCode(temp_str);
					break;
				case consigneeDepartment :
					brdf.setConsigneeDepartment(temp_str);
					break;
				case consigneeTitle :
					brdf.setConsigneeTitle(temp_str);
					break;
				case consigneeLastName :
					brdf.setConsigneeLastName(temp_str);
					break;
				case consigneeFirstName :
					brdf.setConsigneeFirstName(temp_str);
					break;
				case cmdyDesc :
					brdf.setCmdyDesc(temp_str);
					break;
				case cmdySrvrptDesc :
					brdf.setCmdySrvrptDesc(temp_str);
					break;
				case cmdyAg :
					brdf.setCmdyAg(temp_str);
					break;
				case cmdyHhg :
					brdf.setCmdyHhg(temp_str);
					break;
				case cell :
					brdf.setCell(temp_str);
					break;
				case grossWt :
					brdf.setGrossWt(temp_str);
					break;
				case highWtFlag :
					brdf.setHighWtFlag(temp_str);
					break;
				case sealNo :
					brdf.setSealNo(temp_str);
					break;
				case ownerLessor :
					brdf.setOwnerLessor(temp_str);
					break;
				case damageStatus :
					brdf.setDamageStatus(temp_str);
					break;
				case emptyFull :
					brdf.setEmptyFull(temp_str);
					break;
				case ecosRetPort :
					brdf.setEcosRetPort(temp_str);
					break;
				case checkDigit :
					brdf.setCheckDigit(temp_str);
					break;
				case specMsg1 :
					brdf.setSpecMsg1(temp_str);
					break;
				case specMsg2 :
					brdf.setSpecMsg2(temp_str);
					break;
				case specMsg3 :
					brdf.setSpecMsg3(temp_str);
					break;
				case cnsgAreaCode :
					brdf.setCnsgAreaCode(temp_str);
					break;
				case cnsgPhone1 :
					brdf.setCnsgPhone1(temp_str);
					break;
				case cnsgPhone2 :
					brdf.setCnsgPhone2(temp_str);
					break;
				case poNumber :
					brdf.setPoNumber(temp_str);
					break;
				case filler2 :
					brdf.setFiller2(temp_str);
					break;
				case shmtDestCityCode :
					brdf.setShmtDestCityCode(temp_str);
					break;
				case vesselType :
					brdf.setVesselType(temp_str);
					break;
				case consigneeFaxNotifyOrgnName :
					brdf.setConsigneeFaxNotifyOrgnName(temp_str);
					break;
				case consigneeFaxNotifyAttnParty :
					brdf.setConsigneeFaxNotifyAttnParty(temp_str);
					break;
				case consigneeFaxNotifyCtryCode :
					brdf.setConsigneeFaxNotifyCtryCode(temp_str);
					break;
				case consigneeFaxNotifyAreaCode :
					brdf.setConsigneeFaxNotifyAreaCode(temp_str);
					break;
				case consigneeFaxNotifyExch :
					brdf.setConsigneeFaxNotifyExch(temp_str);
					break;
				case consigneeFaxNotifyStation :
					brdf.setConsigneeFaxNotifyStation(temp_str);
					break;
				case consigneeFaxNotifyExt :
					brdf.setConsigneeFaxNotifyExt(temp_str);
					break;
				case consignee2NDFaxCtryCode :
					brdf.setConsignee2NDFaxCtryCode(temp_str);
					break;
				case consignee2NDFaxAreaCode :
					brdf.setConsignee2NDFaxAreaCode(temp_str);
					break;
				case consignee2NDFaxExch :
					brdf.setConsignee2NDFaxExch(temp_str);
					break;
				case consignee2ndFaxStation :
					brdf.setConsignee2ndFaxStation(temp_str);
					break;
				case consignee2NDFaxExt :
					brdf.setConsignee2NDFaxExt(temp_str);
					break;
				case truckerFaxNotifyOrgnName :
					brdf.setTruckerFaxNotifyOrgnName(temp_str);
					break;
				case truckerFaxNotifyAttnParty :
					brdf.setTruckerFaxNotifyAttnParty(temp_str);
					break;
				case truckerFaxNotifyCtryCode :
					brdf.setTruckerFaxNotifyCtryCode(temp_str);
					break;
				case truckerFaxNotifyAreaCode :
					brdf.setTruckerFaxNotifyAreaCode(temp_str);
					break;
				case truckerFaxNotifyExch :
					brdf.setTruckerFaxNotifyExch(temp_str);
					break;
				case truckerFaxNotifyStation :
					brdf.setTruckerFaxNotifyStation(temp_str);
					break;
				case truckerFaxNotifyExt :
					brdf.setTruckerFaxNotifyExt(temp_str);
					break;
				case trucker2NDFaxCtryCode :
					brdf.setTrucker2NDFaxCtryCode(temp_str);
					break;
				case trucker2NDFaxAreaCode :
					brdf.setTrucker2NDFaxAreaCode(temp_str);
					break;
				case trucker2NDFaxExch :
					brdf.setTrucker2NDFaxExch(temp_str);
					break;
				case trucker2ndFaxStation :
					brdf.setTrucker2ndFaxStation(temp_str);
					break;
				case trucker2NDFaxExt :
					brdf.setTrucker2NDFaxExt(temp_str);
					break;
				case headQuartersFaxNotifyOrgnName :
					brdf.setHeadQuartersFaxNotifyOrgnName(temp_str);
					break;
				case headQuartersFaxNotifyAttnParty :
					brdf.setHeadQuartersFaxNotifyAttnParty(temp_str);
					break;
				case headQuartersFaxNotifyCtryCode :
					brdf.setHeadQuartersFaxNotifyCtryCode(temp_str);
					break;
				case headQuartersFaxNotifyAreaCode :
					brdf.setHeadQuartersFaxNotifyAreaCode(temp_str);
					break;
				case headQuartersFaxNotifyExch :
					brdf.setHeadQuartersFaxNotifyExch(temp_str);
					break;
				case headQuartersFaxNotifyStation :
					brdf.setHeadQuartersFaxNotifyStation(temp_str);
					break;
				case headQuartersFaxNotifyExt :
					brdf.setHeadQuartersFaxNotifyExt(temp_str);
					break;
				case headQuarters2NDFaxCtryCode :
					brdf.setHeadQuarters2NDFaxCtryCode(temp_str);
					break;
				case headQuarters2NDFaxAreaCode :
					brdf.setHeadQuarters2NDFaxAreaCode(temp_str);
					break;
				case headQuarters2NDFaxExch :
					brdf.setHeadQuarters2NDFaxExch(temp_str);
					break;
				case headQuarters2ndFaxStation :
					brdf.setHeadQuarters2ndFaxStation(temp_str);
					break;
				case headQuarters2NDFaxExt :
					brdf.setHeadQuarters2NDFaxExt(temp_str);
					break;
				case notifyFaxNotifyOrgnName :
					brdf.setNotifyFaxNotifyOrgnName(temp_str);
					break;
				case notifyFaxNotifyAttnParty :
					brdf.setNotifyFaxNotifyAttnParty(temp_str);
					break;
				case notifyFaxNotifyCtryCode :
					brdf.setNotifyFaxNotifyCtryCode(temp_str);
					break;
				case notifyFaxNotifyAreaCode :
					brdf.setNotifyFaxNotifyAreaCode(temp_str);
					break;
				case notifyFaxNotifyExch :
					brdf.setNotifyFaxNotifyExch(temp_str);
					break;
				case notifyFaxNotifyStation :
					brdf.setNotifyFaxNotifyStation(temp_str);
					break;
				case notifyFaxNotifyExt :
					brdf.setNotifyFaxNotifyExt(temp_str);
					break;
				case notify2NDFaxCtryCode :
					brdf.setNotify2NDFaxCtryCode(temp_str);
					break;
				case notify2NDFaxAreaCode :
					brdf.setNotify2NDFaxAreaCode(temp_str);
					break;
				case notify2NDFaxExch :
					brdf.setNotify2NDFaxExch(temp_str);
					break;
				case notify2ndFaxStation :
					brdf.setNotify2ndFaxStation(temp_str);
					break;
				case notify2NDFaxExt :
					brdf.setNotify2NDFaxExt(temp_str);
					break;
				case carrierCode :
					brdf.setCarrierCode(temp_str);
					break;
				case shipperRef1Text :
					brdf.setShipperRef1Text(temp_str);
					break;
				case milContNbr :
					brdf.setMilContNbr(temp_str);
					break;
				case milConsignee :
					brdf.setMilConsignee(temp_str);
					break;
				case milConsigner :
					brdf.setMilConsigner(temp_str);
					break;
				case govtBlNbr :
					brdf.setGovtBlNbr(temp_str);
					break;
				case milPortCall :
					brdf.setMilPortCall(temp_str);
					break;
				case milTcn :
					brdf.setMilTcn(temp_str);
					break;
				case milVNbr :
					brdf.setMilVNbr(temp_str);
					break;
				case milWeight :
					brdf.setMilWeight(temp_str);
					break;
				case milCubeFt :
					brdf.setMilCubeFt(temp_str);
					break;
				case milPackNbr :
					brdf.setMilPackNbr(temp_str);
					break;
				case detentoinFreeDays :
					brdf.setDetentoinFreeDays(temp_str);
					break;
				case storageFreeDays :
					brdf.setStorageFreeDays(temp_str);
					break;
				case rdd :
					brdf.setRdd(temp_str);
					break;
				case bookingCsrId :
					brdf.setBookingCsrId(temp_str);
					break;
				case npArcrCarrCode :
					brdf.setNpArcrCarrCode(temp_str);
					break;
				case npOrgnName :
					brdf.setNpOrgnName(temp_str);
					break;
				case dPort :
					brdf.setdPort(temp_str);
					break;
				case csrShipperOrgnId :
					brdf.setCsrShipperOrgnId(temp_str);
					break;
				case csrConsigneeOrgnId :
					brdf.setCsrConsigneeOrgnId(temp_str);
					break;
				case csrSpecialSvcArray1 :
					brdf.setCsrSpecialSvcArray1(temp_str);
					break;
				case csrSpecialSvcArray2 :
					brdf.setCsrSpecialSvcArray2(temp_str);
					break;
				case csrSpecialSvcArray3 :
					brdf.setCsrSpecialSvcArray3(temp_str);
					break;
				case csrSpecialSvcArray4 :
					brdf.setCsrSpecialSvcArray4(temp_str);
					break;
				case csrSpecialSvcArray5 :
					brdf.setCsrSpecialSvcArray5(temp_str);
					break;
				case csrClxRoute :
					brdf.setCsrClxRoute(temp_str);
					break;
				case csrClxHonCall :
					brdf.setCsrClxHonCall(temp_str);
					break;
				case csrClxGumCall :
					brdf.setCsrClxGumCall(temp_str);
					break;
				case filler3 :
					brdf.setFiller3(temp_str);
					break;
				case delimeter :
					brdf.setDelimeter(temp_str);
					break;			
					
				// Alasksa						
				case temp2 :
					brdf.setTemp2(temp_str);
					break;
				case multiStop :
					brdf.setMultiStop(temp_str);
					break;

			}
			beginIndex = beginIndex + endIndex;
		}
		rdsData.add(brdf);
		//logger.debug( " processLine ------  temp1 " + brdf.getTemp() + "   temp2  " + brdf.getTemp2()  + "  multistop :  " + brdf.getMultiStop());
		String cVesVoy = "";
		if (brdf.getVes() != null && brdf.getVoy() != null)
		{
			cVesVoy = brdf.getVes() + brdf.getVoy() + brdf.getLeg();
			if (!vesselVoyageList.contains(cVesVoy))
			{
				vesselVoyageList.add(cVesVoy);
			}
		}

	}

	private TosRdsDataMt populateTosRdsDataMt(RDSFields brdf, String tobeRDSVesvoy)
	{
		
		logger.info("inside populateTosRdsDataMt ");
		TosDestPodData refData = null;
		TosRdsDataMt rds = new TosRdsDataMt();
		String pod1 = "ANK";
		// rds.setVes(brdf.getVes());
		// rds.setVoy(brdf.getVoy());
		// rds.setLeg(brdf.getLeg());
		rds.setVes(tobeRDSVesvoy.substring(0, 3));
		rds.setVoy(tobeRDSVesvoy.substring(3, 6));
		rds.setLeg(tobeRDSVesvoy.substring(6, 7));
		rds.setCtrno(brdf.getContainerNumber());
		rds.setShipno(brdf.getShipmentNo());
		rds.setDatasource(brdf.getDataSource());
		rds.setLoadPort(brdf.getLoadPort());
		rds.setDestinationPort(brdf.getPlaceDelCode());
		
		/*if("DUT".equalsIgnoreCase(brdf.getdPort()))
			rds.setDischargePort("DUT");
		else if("KDK".equalsIgnoreCase(brdf.getdPort()))
			rds.setDischargePort("KDK");
		else
			rds.setDischargePort("ANK");*/
		//
		try{
			//Set Pod-1 value from TosDestPodData by sending Dport as input
			refData = (TosDestPodData)RefDataLookup.queryById("com.matson.cas.refdata.mapping.TosDestPodData", brdf.getdPort());
			logger.info("refdata from TosDestPodData"+refData);
			if ( refData != null) {
				pod1 = refData.getPod1();
				logger.info("POD-1 "+refData.getPod1());
			}
			logger.info("brdf.getTransit()"+brdf.getTransit());
			if ("S".equalsIgnoreCase(brdf.getTransit())) {
				logger.info("set the discharge port to ANK if discharge service is S");
				rds.setDischargePort("ANK");
			} else {
				logger.info("Discharge Service is not SIT POD1 is: " + pod1);
				rds.setDischargePort(pod1);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Error while reading TosDestPodData ");
		}
		
		//
		
		rds.setTemp(brdf.getTemp());
		rds.setLoadDischServ(brdf.getDischService());
		rds.setHazf(brdf.getHazf());
		rds.setInbound(brdf.getInbond());
		rds.setNotify(brdf.getNotify());
		rds.setCreditStatus(brdf.getCreditStatus());
		rds.setTransit(brdf.getTransit());
		rds.setShipperArol(brdf.getShipperArolIden());
		if (brdf.getShipperOrgName() != null)
			rds.setShipperName(brdf.getShipperOrgName().length() > 32 ? brdf
					.getShipperOrgName().substring(0, 32) : brdf
					.getShipperOrgName());
		if (brdf.getShipperOrgNameQual() != null)
			rds.setShipperQualifier(brdf.getShipperOrgNameQual().length() > 32 ? brdf
					.getShipperOrgNameQual().substring(0, 32) : brdf
					.getShipperOrgNameQual());
		rds.setConsigneeArol(brdf.getConsigneeArolIden());
		if (brdf.getConsigneeOrgName() != null)
			rds.setConsigneeName(brdf.getConsigneeOrgName().length() > 32 ? brdf
					.getConsigneeOrgName().substring(0, 32) : brdf
					.getConsigneeOrgName());
		if (brdf.getConsigneeCo() != null)
			rds.setConsigneeCO(brdf.getConsigneeCo().length() > 32 ? brdf
					.getConsigneeCo().substring(0, 32) : brdf.getConsigneeCo());
		if (brdf.getConsigneeOrgNameQual() != null)
			rds.setConsigneeQualifier(brdf.getConsigneeOrgNameQual().length() > 32 ? brdf
					.getConsigneeOrgNameQual().substring(0, 32) : brdf
					.getConsigneeOrgNameQual());
		if (brdf.getConsigneeAddrLine1() != null)
			rds.setConsigneeAddr(brdf.getConsigneeAddrLine1().length() > 29 ? brdf
					.getConsigneeAddrLine1().substring(0, 29) : brdf
					.getConsigneeAddrLine1());
		rds.setConsigneeSuite(brdf.getConsigneeSuite());
		rds.setConsigneeCity(brdf.getConsigneeCity());
		rds.setConsigneeState(brdf.getConsigneeState());
		rds.setConsigneeCountry(brdf.getConsigneeCountry());
		rds.setConsigneeZipCode(brdf.getConsigneeZipCode());
		rds.setConsigneeDepartment(brdf.getConsigneeDepartment());
		rds.setConsigneeTitle(brdf.getConsigneeTitle());
		if (brdf.getConsigneeLastName() != null)
			rds.setConsigneeLastName(brdf.getConsigneeLastName().length() > 32 ? brdf
					.getConsigneeLastName().substring(0, 32).trim()
					: brdf.getConsigneeLastName().trim());
		if (brdf.getConsigneeFirstName() != null)
			rds.setConsigneeFirstName(brdf.getConsigneeFirstName().length() > 32 ? brdf
					.getConsigneeFirstName().substring(0, 32).trim()
					: brdf.getConsigneeFirstName().trim());
		if (brdf.getCmdyDesc() != null)
			rds.setCmdyDesc(brdf.getCmdyDesc().length() > 30 ? brdf
					.getCmdyDesc().substring(0, 30).trim() : brdf.getCmdyDesc()
					.trim());
		if (brdf.getCmdySrvrptDesc() != null)
			rds.setCmdySrvptDesc(brdf.getCmdySrvrptDesc().length() > 12 ? brdf
					.getCmdySrvrptDesc().substring(0, 12).trim() : brdf
					.getCmdySrvrptDesc().trim());
		rds.setCmdyAg(brdf.getCmdyAg());
		rds.setCmdyHhg(brdf.getCmdyHhg());
		rds.setCell(brdf.getCell());
		logger.info("brdf.getGrossWt()"+brdf.getGrossWt()+"end");
		rds.setGrossWt(new BigDecimal(brdf.getGrossWt()));
		rds.setSealNumber(brdf.getSealNo());
		rds.setOwnerLessor(brdf.getOwnerLessor());
		rds.setDamageStatus(brdf.getDamageStatus());
		rds.setEmptyFull(brdf.getEmptyFull());
		rds.setEcosRetport(brdf.getEcosRetPort());
		rds.setCheckDigit(brdf.getCheckDigit());
		rds.setSpecMsg1(brdf.getSpecMsg1());
		rds.setSpecMsg2(brdf.getSpecMsg2());
		rds.setSpecMsg3(brdf.getSpecMsg3());
		rds.setCnsgAreaCode(brdf.getCnsgAreaCode());
		rds.setCnsgPhone(brdf.getCnsgPhone1() + brdf.getCnsgPhone2());
		rds.setPoNumber(brdf.getPoNumber());
		rds.setPriCarrier(brdf.getPrimaryCarrier());
		rds.setTrade(brdf.getTrade());
		rds.setLoadType(brdf.getLoadType());
		rds.setMilCnee(brdf.getMilConsignee());
		if (brdf.getMilTcn() != null)
			rds.setMilTcn(brdf.getMilTcn().length() > 20 ? brdf.getMilTcn()
					.substring(0, 20) : brdf.getMilTcn());
		if (brdf.getRdd() != null)
			rds.setRdd(brdf.getRdd().length() > 10 ? brdf.getRdd().substring(0,
					10) : brdf.getRdd());
		rds.setCsrId(brdf.getBookingCsrId());
		rds.setNotifyParty(brdf.getNpArcrCarrCode());
		if (brdf.getCsrShipperOrgnId() != null)
			rds.setShipperOrgnId(brdf.getCsrShipperOrgnId().length() > 10 ? brdf
					.getCsrShipperOrgnId().substring(0, 10) : brdf
					.getCsrShipperOrgnId());
		if (brdf.getCsrConsigneeOrgnId() != null)
			rds.setConsigneeOrgnId(brdf.getCsrConsigneeOrgnId().length() > 10 ? brdf
					.getCsrConsigneeOrgnId().substring(0, 10) : brdf
					.getCsrConsigneeOrgnId());
		rds.setDeliveryDepot(brdf.getCsrSpecialSvcArray1());
		// rds.setConsigneeOrgnId(brdf.getConsigneeArolIden());
		rds.setSecondVesvoy(brdf.getInitialVes() + brdf.getInitialVoy());
		rds.setCreateUser("newves");
		rds.setCreateDate(triggerDate);
		rds.setLastUpdateUser("newves");
		rds.setLastUpdateDate(triggerDate);
		
		//Alaska changes
		rds.setTemp2(brdf.getTemp2());
		rds.setMultiStop(brdf.getMultiStop());

		//logger.debug( " populateTosRdsDataMt ------  temp1 " + rds.getTemp() + "   temp2  " + rds.getTemp2()  + "  multistop :  " + rds.getMultiStop() );		
		return rds;
	}

	private TosRdsDataFinalMt populateTosRdsDataFinalMt(TosRdsDataMt rdsData, TosStowPlanCntrMt ctrData, String type, String service, ArrayList<TosRdsDataMt> rawDataClone)
	{
		TosRdsDataFinalMt rdsF = new TosRdsDataFinalMt();
		// From RDS Data
		rdsF.setContainerNumber(rdsData.getCtrno());
		// rdsF.setDport(rdsData.getDestinationPort());
		rdsF.setConsignee(rdsData.getConsigneeName());
		// rdsF.setShipper(rdsData.getShipperName());
		rdsF.setVesvoy(rdsData.getVes() + rdsData.getVoy());

		rdsF.setMisc1(rdsData.getSecondVesvoy());
		String ds = rdsData.getLoadDischServ();
		ds = ds == null ? "" : ds;
		if (ds.equals("AU")) {
			ds = "AUT";
			rdsF.setCommodity("AUTO");
		}
		else if (ds.equals("TY") || ds.equals("PJT") || ds.equals("P") || ds.equals("IM") || ds.equals("IMS") || ds.equals("IMR"))
			ds = "CY";
		rdsF.setDs(ds);
		// rdsF.setDsc(ctrData.getDsc());
		if (rdsF.getDs() == null || rdsF.getDs().equals(""))
		{
			rdsF.setDs(ctrData.getDs());
		}
		rdsF.setDss(ctrData.getDss());

		String sealNumber = ctrData.getSealNumber();
		if (sealNumber == null)
			sealNumber = rdsData.getSealNumber();
		rdsF.setSealNumber(sealNumber);
		String transit = rdsData.getTransit();
		transit = transit == null ? "" : transit;
		logger.info("transit  :" + transit + " container number :" + rdsData.getCtrno());
		String cmdyhhg = rdsData.getCmdyHhg();
		cmdyhhg = cmdyhhg == null ? "" : cmdyhhg;
		String cgn = "";
		String shipNo = rdsData.getShipno();
		shipNo = shipNo == null ? "" : shipNo;
		boolean multipleShipment = false;
		String bkgNbr = shipNo;
		String dscrds = "";
		ArrayList<String> shipNoList = new ArrayList<String>(
				Arrays.asList(shipNo.split("-")));
		if (shipNoList.size() > 1)
		{
			multipleShipment = true;
			bkgNbr = shipNoList.get(0);
		}
		if (multipleShipment && cmdyhhg.equals("Y") && transit.equals(""))
		{
			rdsF.setDsc("A");
			dscrds = "A";
		}
		else
		{
			rdsF.setDsc(transit);
			dscrds = transit;
		}
		// rdsF.setBookingNumber(bkgNbr);
		String cmdySrvDesc = rdsData.getCmdySrvptDesc();
		String specMsg1 = rdsData.getSpecMsg1();
		String specMsg2 = rdsData.getSpecMsg2();
		String specMsg3 = rdsData.getSpecMsg3();
		if (specMsg1 != null)
			cgn = cgn + " " + specMsg1;
		if (specMsg2 != null)
			cgn = cgn + " " + specMsg2;
		if (specMsg3 != null)
			cgn = cgn + " " + specMsg3;
		String consigneeCo = rdsData.getConsigneeCO();
		if (consigneeCo != null && !consigneeCo.equals(""))
		{
			cgn = "C/O " + consigneeCo + "-" + cgn.trim();
		}
		String np = rdsData.getNotifyParty();
		np = np == null ? "" : np;
		if (!np.equals(""))
		{
			cgn = " N/P " + np + ", " + cgn.trim();
			rdsF.setNotifyParty(np);
			
		}
		String commodity = rdsData.getCmdyDesc();
		commodity = commodity == null ? "" : commodity;
		cgn = cgn.trim();
		if (!cgn.endsWith("-"))
			cgn = cgn + " ";
		cgn = cgn + commodity.trim();
		logger.info("testdscrds----" + dscrds + " " + rdsData.getCtrno());
		/*
		 * if(multipleShipment && ("M".equalsIgnoreCase(dscrds) ||
		 * "A".equalsIgnoreCase(dscrds) || (!"P".equalsIgnoreCase(dscrds) &&
		 * "CFS".equalsIgnoreCase(rdsData.getLoadDischServ()))))
		 */
		// Check if the cntr is originally a RoRo discharge service
		boolean isRoRo = false;
		Iterator<TosRdsDataMt> rdsItr = rawDataClone.iterator();
		while(rdsItr.hasNext()) {
			TosRdsDataMt rdsFields = rdsItr.next();
			if(rdsFields.getCtrno()!=null && rdsFields.getCtrno().trim().equalsIgnoreCase(rdsData.getCtrno()) 
					&& rdsFields.getVes()!=null && rdsFields.getVes().trim().equalsIgnoreCase(rdsData.getVes())
					&& rdsFields.getVoy()!=null && rdsFields.getVoy().trim().equalsIgnoreCase(rdsData.getVoy())) {
				logger.info("RORO CHECK - CTRNo:"+rdsFields.getCtrno()+",DS:"+rdsFields.getLoadDischServ()+",BKG:"+rdsFields.getShipno());
				if(rdsFields.getLoadDischServ()!=null && rdsFields.getLoadDischServ().trim().equalsIgnoreCase("RO")) {
					isRoRo = true; break;
				}
			}
		}
		logger.info("isRoRo = "+isRoRo);
		if (multipleShipment && rdsF.getDs() != null && (rdsF.getDs().equals("CY") || rdsF.getDs().equals("CON") || isRoRo))
		{
			if (!cgn.contains("MNFST INFO"))
				cgn = shipNo.trim() + " " + cgn.trim();
			else
				cgn = cgn.trim() + " " + shipNo.trim();
		}
		cgn = cgn.length() >= 255 ? cgn.substring(0, 255) : cgn;
		rdsF.setCargoNotes(cgn.trim());
		// logger.info("CNTRNO: " + rdsF.getContainerNumber()
		// + " CGNOTES: " + rdsF.getCargoNotes());
		// rdsF.setCell(rdsData.getCell());
		// rdsF.setCweight(rdsData.getGrossWt());
		logger.info("CTRNO:"+rdsData.getCtrno()+"\t CneeName:"+rdsData.getConsigneeName()+"\t CneeQual:"+rdsData.getConsigneeQualifier()+"\t CneeArolId:"+rdsData.getConsigneeArol()+"\t CneeOrgnId:"+rdsData.getConsigneeOrgnId());
		if (rdsData.getConsigneeQualifier() != null
				&& !rdsData.getConsigneeQualifier().trim().equals(""))
		{
			rdsF.setCneeCode(rdsData.getConsigneeArol());
		}
		else
			rdsF.setCneeCode(rdsData.getConsigneeOrgnId());
		logger.info("CNEE CODE : "+rdsF.getCneeCode());
		if (rdsData.getMilTcn() != null && rdsData.getMilCnee() != null
				&& !rdsData.getMilTcn().trim().equals("")
				&& !rdsData.getMilCnee().trim().equals(""))
		{
			rdsF.setCneePo(rdsData.getMilCnee() + " " + rdsData.getMilTcn());
		}
		else if (rdsData.getMilTcn() != null
				&& !rdsData.getMilTcn().trim().equals(""))
		{
			rdsF.setCneePo("       " + rdsData.getMilTcn());
		}
		else if (rdsData.getMilCnee() != null
				&& !rdsData.getMilCnee().trim().equals(""))
		{
			rdsF.setCneePo(rdsData.getMilCnee());
		}
		else
		{
			rdsF.setCneePo(rdsData.getPoNumber());
		}
		// rdsF.setDischargePort(rdsData.getDischargePort());
		commodity = commodity.length() >= 8 ? commodity.substring(0, 8)
				: commodity;
		rdsF.setCommodity(commodity);
		if(cmdySrvDesc!=null && (cmdySrvDesc.equals("AUTO")||cmdySrvDesc.equals("AUTOCY")||cmdySrvDesc.equals("AUTOCON"))) //A29
			rdsF.setCommodity(cmdySrvDesc); // IF AUTO/AUTOCY/AUTOCON are set from convCon2Cy(), then set commodity to the same
		// Fix for Gear container commodity
		if (ctrData.getCommodity() != null && ctrData.getCommodity().equals("GEAR") && (rdsData.getCmdyDesc() == null || rdsData.getCmdyDesc().equals("")))
			rdsF.setCommodity(ctrData.getCommodity());
		String loadPort = rdsData.getLoadPort();
		// Non Guam/China load port update load port from stowplan
		if (loadPort != null && (loadPort.equals("LAX") || loadPort.equals("OAK") || loadPort.equals("SEA") || loadPort.equals("PDX")))
		{
			if (ctrData.getLoadPort() != null)
			{
				loadPort = ctrData.getLoadPort();
			}
		}
		rdsF.setLoadPort(loadPort);
		if (rdsData.getCheckDigit() != null && rdsData.getCheckDigit().trim().length() > 0)
			rdsF.setCheckDigit(rdsData.getCheckDigit());
		else
			rdsF.setCheckDigit(ctrData.getCheckDigit());
		rdsF.setCnsgPhone(rdsData.getCnsgPhone());
		rdsF.setCsrId(rdsData.getCsrId());
		rdsF.setTrade(rdsData.getTrade());
		rdsF.setConsigneeOrgnId(rdsData.getConsigneeOrgnId());
		rdsF.setConsigneeArol(rdsData.getConsigneeArol());
		String consigneeName = rdsData.getConsigneeName();
		consigneeName = consigneeName == null ? "" : consigneeName;
		rdsF.setConsigneeName(consigneeName);
		String consigneeQual = rdsData.getConsigneeQualifier();
		consigneeQual = consigneeQual == null ? "" : consigneeQual;
		rdsF.setConsigneeQualifier(consigneeQual);
		if (rdsData.getConsigneeArol() != null && !rdsData.getConsigneeArol().isEmpty()) {
			rdsF.setCneeCode(rdsData.getConsigneeArol());
		} else {
			rdsF.setCneeCode(rdsData.getConsigneeOrgnId());
		}
		if (consigneeQual != null && !consigneeQual.equals(""))
		{
			String tempStr = consigneeName.length() >= 26 ? consigneeName
					.substring(0, 26) : consigneeName;
			tempStr = tempStr + " " + consigneeQual;

			tempStr = tempStr.length() >= 32 ? tempStr.substring(0, 32)
					: tempStr;
			rdsF.setConsignee(tempStr);

		}
		else
		{
			consigneeName = consigneeName.length() >= 32 ? consigneeName
					.substring(0, 32) : consigneeName;
			rdsF.setConsignee(consigneeName);
		}
		// A29
		// Damage Status field is not used anywhere in RDS processing, so it is being set with manipulated consignee from Combine_autos
		// This is purely for temporary storage of cnee and blank it out once it is retrieved.
		if("AU".equalsIgnoreCase(rdsData.getLoadDischServ()) && rdsData.getDamageStatus()!=null && rdsData.getDamageStatus().trim().length()>0) {
			logger.info("ASSIGNED CNEE : "+rdsData.getDamageStatus());
			rdsF.setConsignee(rdsData.getDamageStatus());
			rdsData.setDamageStatus("");
		}
		rdsF.setShipperArol(rdsData.getShipperArol());
		rdsF.setShipperOrgnId(rdsData.getShipperOrgnId());
		String shipperName = rdsData.getShipperName();
		shipperName = shipperName == null ? "" : shipperName;
		rdsF.setShipperName(shipperName);
		String shipperQual = rdsData.getShipperQualifier();
		shipperQual = shipperQual == null ? "" : shipperQual;
		rdsF.setShipperQualifier(shipperQual);
		if (shipperQual != null && !shipperQual.equals(""))
		{
			String tempStrShipper = shipperName.length() >= 29 ? shipperName
					.substring(0, 29) : shipperName;

			tempStrShipper = tempStrShipper + " " + shipperQual;
			tempStrShipper = tempStrShipper.length() >= 35 ? tempStrShipper.substring(0,
					35) : tempStrShipper;
			// logger.info("tempStrShipper>>>"+tempStrShipper+" for "+rdsData.getCtrno());
			rdsF.setShipper(tempStrShipper);
		}
		else
		{
			// logger.info("shipperName>>>"+shipperName+" for "+rdsData.getCtrno());
			shipperName = shipperName.length() >= 35 ? shipperName.substring(0,
					35) : shipperName;
			rdsF.setShipper(shipperName);
		}
		String creditStatus = rdsData.getCreditStatus();
		creditStatus = creditStatus == null ? "" : creditStatus;
		if (creditStatus.equals(""))
		{
			if (rdsData.getDatasource() != null && rdsData.getDatasource().equals("E"))
			{
				rdsF.setHsf2("H");
			}
		}
		else
		{
			rdsF.setHsf2(creditStatus);
		}
		rdsF.setHsf3(rdsData.getNotify());
		if (rdsData.getCmdyAg() != null && rdsData.getCmdyAg().equals("N"))
			rdsF.setHsf4("");
		else
			rdsF.setHsf4(rdsData.getCmdyAg());
		rdsF.setHsf5(rdsData.getInbound());
		String hsf6 = rdsData.getDatasource();
		if (hsf6 != null)
			hsf6 = hsf6.equalsIgnoreCase("L") ? "" : hsf6;
		rdsF.setHsf6(hsf6);
		String hsf7 = rdsData.getFreeStorage();
		if (hsf7 != null)
			hsf7 = hsf7.equalsIgnoreCase("Y") ? "S" : "";
		rdsF.setHsf7(hsf7);
		String srstatus = CommonBusinessProcessor.srstatusTransformation(rdsData);
		srstatus = srstatus.length() > 10 ? srstatus.substring(0, 10) : srstatus;
		rdsF.setSrstatus(srstatus);
		rdsF.setMilTcn(rdsData.getMilTcn());
		// rdsF.setHazf(ctrData.getHazf());
		rdsF.setDeliveryDepot(rdsData.getDeliveryDepot());
		// From Stow Data - OCR
		rdsF.setSrv("MAT");// ctrData.getSrv());
		rdsF.setTruck(ctrData.getTruck());
		rdsF.setPmd(ctrData.getPmd());
		if(rdsData.getLoadType()!=null && rdsData.getLoadType().equalsIgnoreCase("E"))// A30
			rdsData.setEmptyFull("E"); // A30
		String emptyFull = rdsData.getEmptyFull();
		emptyFull = emptyFull == null ? "" : emptyFull;
		if (emptyFull.equals("F"))
			rdsF.setDir("IN");
		else if (emptyFull.equals("E"))
			rdsF.setDir("MTY");
		// logger.info("Dir value for container#:"+rdsData.getCtrno()
		// +" is: "+ctrData.getDir());
		if ("OUT".equalsIgnoreCase(ctrData.getDir()))
		{
			rdsF.setDir("IN");
		}
		rdsF.setComments(ctrData.getComments());

		String typeCode = ctrData.getTypeCode();
		typeCode = typeCode == null ? "" : typeCode;
		typeCode = CommonBusinessProcessor.fixTypeCode(typeCode, rdsData.getCtrno(), rdsData.getConsigneeName());
		rdsF.setTypeCode(typeCode);
		if (ctrData.getOwner() != null)
			rdsF.setOwner(ctrData.getOwner());
		else if (rdsData.getOwnerLessor() != null)
			rdsF.setOwner(rdsData.getOwnerLessor());

		// Booking number
		String tempDs = rdsF.getDs();
		String tempDsc = rdsF.getDsc();
		String tempComments = rdsF.getComments();
		if (tempDs != null && tempDsc != null)
		{
			if ((tempDs.equals("CY") || tempDs.equals("CON")) && !tempDsc.equals("M") && !tempDsc.equals("P"))
			{
			}
			//else if (tempDs.equals("AUT") && tempComments != null && tempComments.contains("ORIGIN GAM"))
			else if (tempDs.equals("AUT"))
			{
			}
			else if (tempDs.equals("DS")|| tempDs.equals("LCL") || tempDs.equals("PPC") ) //Null booking number issue
			{
				
			}			
			else if (isRoRo) { }
			else
				bkgNbr = null;
		}

		String rdd = rdsData.getRdd();
		if (rdd != null && rdd.length() > 0)
		{
			try
			{
				Date temp = new SimpleDateFormat("yyyy-MM-dd").parse(rdd);
				rdd = CalendarUtil.convertDateToString(temp);
			}
			catch (Exception e)
			{
				logger.error("RDD date parsing error : input : " + rdd);
			}
			bkgNbr = rdd;
		}
		rdsF.setBookingNumber(bkgNbr);
		logger.info("CTRNO:"+rdsF.getContainerNumber()+"  BKGNBR:"+bkgNbr+"  DS:"+tempDs+"  DSC:"+tempDsc+"   isRoRo:"+isRoRo);
		//
		rdsF.setPlanDisp(ctrData.getPlanDisp());
		// rdsF.setLocationStatus(ctrData.getLocationStatus());
		rdsF.setLocationStatus("4");
		rdsF.setLocationCategory(ctrData.getLocationCategory());
		
		//D031817 start
		//rdsF.setLocationRowDeck(ctrData.getLocationRowDeck());
		if (rdsData.getPriCarrier() != null && !"".equalsIgnoreCase(rdsData.getPriCarrier()))
			rdsF.setLocationRowDeck(rdsData.getPriCarrier());
		else 
			rdsF.setLocationRowDeck(ctrData.getLocationRowDeck());
		//D031817 end
		
		rdsF.setLocationRunStackSectn(ctrData.getLocationRunStackSectn());
		rdsF.setLocationTierStall(ctrData.getLocationTierStall());
		rdsF.setLocationStallConfig(ctrData.getLocationStallConfig());
		rdsF.setOrientation(ctrData.getOrientation());
		if (emptyFull.equals("E")) { //A30
			rdsF.setOrientation("E");
		}
		rdsF.setStowRestrictionCode(ctrData.getStowRestrictionCode());
		String trade = rdsData.getTrade();
		/*
		 * if(ctrData.getHazardousOpenCloseFlag()!=null) { trade =
		 * ctrData.getHazardousOpenCloseFlag(); } else { trade =
		 * CommonBusinessProcessor.getTradeforPort(ctrData.getDport()); }
		 */
		rdsF.setHazardousOpenCloseFlag(trade);
		rdsF.setTrade(trade);
		BigDecimal tareWeight = ctrData.getTareWeight();
		tareWeight = CommonBusinessProcessor.fixTareWeight(tareWeight, typeCode);
		rdsF.setTareWeight(tareWeight);
		rdsF.setActualVessel(ctrData.getVesvoy() != null ? ctrData.getVesvoy()
				.substring(0, 3) : null);
		rdsF.setActualVoyage(ctrData.getVesvoy() != null ? ctrData.getVesvoy()
				.substring(3, 6) : null);
		rdsF.setLeg(ctrData.getLeg());
		rdsF.setHgt(ctrData.getHgt());
		rdsF.setStrength(ctrData.getStrength());
		rdsF.setStowFlag(ctrData.getStowFlag());
		rdsF.setAei(ctrData.getAei());
		rdsF.setOdf(ctrData.getOdf());
		// logger.info(ctrData.getDport());
		/*
		 * if (ctrData.getDport() != null) rdsF.setDport(ctrData.getDport());
		 * else rdsF.setDport(rdsData.getDestinationPort());
		 */
		// Raghu
		if (rdsData.getDestinationPort() != null)
		{
			if ("E".equalsIgnoreCase(rdsData.getDatasource()))
			{
				rdsF.setDport(ctrData.getDport());
			}
			else
			{
				rdsF.setDport(rdsData.getDestinationPort());
			}
		}
		else
		{
			rdsF.setDport(ctrData.getDport());
		}
		
		logger.debug(" container number 1 : "+rdsF.getContainerNumber()+" discharge port : "+rdsData.getDischargePort());
		rdsF.setDischargePort(rdsData.getDischargePort());

		String temperature = "";
		temperature = ctrData.getTemp();
		temperature = temperature == null ? "" : temperature;
		//logger.debug("populateTosRdsDataFinalMt : temp 1 from stow plan :: "+temperature);
		if (temperature.equals("") || temperature.equalsIgnoreCase("AMB"))
		{
			temperature = rdsData.getTemp();
			temperature = temperature == null ? "" : temperature;
		}
		//logger.debug("populateTosRdsDataFinalMt : temp 1 Final :: "+temperature);
		rdsF.setTemp(temperature);
		rdsF.setTempMeasurementUnit(ctrData.getTempMeasurementUnit());
		rdsF.setDamageCode(ctrData.getDamageCode());
		if (ctrData.getCweight() != null)
			rdsF.setCweight(ctrData.getCweight());
		else
			rdsF.setCweight(rdsData.getGrossWt());
		String cell = ctrData.getCell();
		if (cell != null && cell.length() > 0)
			rdsF.setCell(cell);
		else
			rdsF.setCell(rdsData.getCell());
		rdsF.setChassisNumber(ctrData.getChassisNumber());
		//
		rdsF.setCreateUser("newves");
		rdsF.setCreateDate(triggerDate);
		rdsF.setLastUpdateUser("newves");
		rdsF.setLastUpdateDate(triggerDate);
		rdsF.setTriggerDate(triggerDate);
		rdsF.setAction("NVI");
		rdsF.setAdate(triggerDate);
		DateFormat dateFormat = new SimpleDateFormat("HH:mm");
		Calendar cal = Calendar.getInstance();
		rdsF.setAtime(dateFormat.format(cal.getTime()));
		//
		if (rdsF.getHsf6() != null && rdsF.getHsf6().equals("E") && ctrData.getDir() != null && ctrData.getDir().equals("MTY"))
		{
			rdsF.setDir("MTY");
			rdsF.setDs(null);
			rdsF.setDport(ctrData.getDport());
		}
		if (rdsF.getHsf6() != null && rdsF.getHsf6().equals("A") && rdsF.getDs() == null)
		{
			rdsF.setDs("AUT");
			rdsF.setDport(ctrData.getDport());
			rdsF.setTrade(null);
			rdsF.setHazardousOpenCloseFlag(null);
		}
		//
		
		
		//Alaska changes
		
		temperature = ctrData.getTemperature2();
		temperature = temperature == null ? "" : temperature;
		if (temperature.equals(""))
		{
			temperature = rdsData.getTemp2();
			temperature = temperature == null ? "" : temperature;
		}
		
		rdsF.setTemp2(temperature);
		rdsF.setMultiStop(rdsData.getMultiStop());
		rdsF.setPriorityStow(ctrData.getPriorityStow());
		
		//Alaska changes - Consignee Address
		rdsF.setConsigneeAddr(rdsData.getConsigneeAddr());
		rdsF.setConsigneeSuite(rdsData.getConsigneeSuite());
		rdsF.setConsigneeCity(rdsData.getConsigneeCity());
		rdsF.setConsigneeState(rdsData.getConsigneeState());
		rdsF.setConsigneeCountry(rdsData.getConsigneeCountry());
		rdsF.setConsigneeZipCode(rdsData.getConsigneeZipCode());
				
		//logger.debug( " populateTosRdsDataFinalMt ------  temp1 " + rdsF.getTemp() + "   temp2  " + rdsF.getTemp2()  + "  multistop :  " + rdsF.getMultiStop());
		
		return rdsF;
	}

	private TosRdsDataFinalMt populateTosRdsDataFinalMtSupplemental(TosRdsDataMt rdsData, String service)
	{
		TosRdsDataFinalMt rdsF = new TosRdsDataFinalMt();
		rdsF.setContainerNumber(rdsData.getCtrno());
		rdsF.setVesvoy(rdsData.getVes() + rdsData.getVoy());
		rdsF.setLeg(rdsData.getLeg());
		rdsF.setMisc1(rdsData.getSecondVesvoy());
		rdsF.setSealNumber(rdsData.getSealNumber());
		String transit = rdsData.getTransit();
		transit = transit == null ? "" : transit;
		String cmdyhhg = rdsData.getCmdyHhg();
		cmdyhhg = cmdyhhg == null ? "" : cmdyhhg;
		String cgn = "";
		String shipNo = rdsData.getShipno();
		shipNo = shipNo == null ? "" : shipNo;
		boolean multipleShipment = false;
		String bkgNbr = shipNo;
		ArrayList<String> shipNoList = new ArrayList<String>(
				Arrays.asList(shipNo.split("-")));
		if (shipNoList.size() > 1)
		{
			multipleShipment = true;
			// cgn = cgn + shipNo;
			bkgNbr = shipNoList.get(0);
		}
		if (multipleShipment && cmdyhhg.equals("Y") && transit.equals(""))
			rdsF.setDsc("A");
		else
			rdsF.setDsc(transit);
		String specMsg1 = rdsData.getSpecMsg1();
		String specMsg2 = rdsData.getSpecMsg2();
		String specMsg3 = rdsData.getSpecMsg3();
		if (specMsg1 != null)
			cgn = cgn + " " + specMsg1;
		if (specMsg2 != null)
			cgn = cgn + " " + specMsg2;
		if (specMsg3 != null)
			cgn = cgn + " " + specMsg3;
		String consigneeCo = rdsData.getConsigneeCO();
		if (consigneeCo != null && !consigneeCo.equals(""))
		{
			cgn = "C/O " + consigneeCo + "-" + cgn.trim();
		}
		String np = rdsData.getNotifyParty();
		np = np == null ? "" : np;
		if (!np.equals(""))
		{
			cgn = cgn.trim() + " N/P " + np + ", ";
		}
		String commodity = rdsData.getCmdyDesc();
		commodity = commodity == null ? "" : commodity;
		cgn = cgn.trim() + " " + commodity.trim();
		cgn = cgn.length() >= 255 ? cgn.substring(0, 255) : cgn;
		rdsF.setCargoNotes(cgn.trim());
		rdsF.setCell(rdsData.getCell());
		rdsF.setCweight(rdsData.getGrossWt());
		if (rdsData.getConsigneeQualifier() != null
				&& !rdsData.getConsigneeQualifier().trim().equals(""))
		{
			rdsF.setCneeCode(rdsData.getConsigneeArol());
		}
		else
			rdsF.setCneeCode(rdsData.getConsigneeOrgnId());

		if (rdsData.getMilTcn() != null && rdsData.getMilCnee() != null
				&& !rdsData.getMilTcn().trim().equals("")
				&& !rdsData.getMilCnee().trim().equals(""))
		{
			rdsF.setCneePo(rdsData.getMilCnee() + " " + rdsData.getMilTcn());
		}
		else if (rdsData.getMilTcn() != null
				&& !rdsData.getMilTcn().trim().equals(""))
		{
			rdsF.setCneePo("       " + rdsData.getMilTcn());
		}
		else if (rdsData.getMilCnee() != null
				&& !rdsData.getMilCnee().trim().equals(""))
		{
			rdsF.setCneePo(rdsData.getMilCnee());
		}
		else
		{
			rdsF.setCneePo(rdsData.getPoNumber());
		}
		commodity = commodity.length() >= 8 ? commodity.substring(0, 8)
				: commodity;
		rdsF.setCommodity(commodity);
		
		//Alaska Change
		if("SIT".equalsIgnoreCase(commodity) || MULTISTOP_SIT.equalsIgnoreCase(commodity)){
			rdsF.setDischargePort(rdsData.getDischargePort());
		}
		logger.debug("Container number 2 "+rdsF.getContainerNumber()+" discharge port"+rdsData.getDischargePort());
		if("S".equalsIgnoreCase(rdsF.getDsc())){
			rdsF.setDischargePort(rdsData.getDischargePort());
		}
		logger.debug("Container number 3 "+rdsF.getContainerNumber()+" discharge port"+rdsData.getDischargePort());
		
		rdsF.setLoadPort(rdsData.getLoadPort());
		rdsF.setCheckDigit(rdsData.getCheckDigit());
		rdsF.setCnsgPhone(rdsData.getCnsgPhone());
		rdsF.setCsrId(rdsData.getCsrId());
		rdsF.setTrade(rdsData.getTrade());
		rdsF.setConsigneeOrgnId(rdsData.getConsigneeOrgnId());
		rdsF.setConsigneeArol(rdsData.getConsigneeArol());
		String consigneeName = rdsData.getConsigneeName();
		consigneeName = consigneeName == null ? "" : consigneeName;
		rdsF.setConsigneeName(consigneeName);
		String consigneeQual = rdsData.getConsigneeQualifier();
		consigneeQual = consigneeQual == null ? "" : consigneeQual;
		rdsF.setConsigneeQualifier(consigneeQual);
		if (consigneeQual != null && !consigneeQual.equals(""))
		{
			String tempStr = consigneeName.length() >= 26 ? consigneeName
					.substring(0, 26) : consigneeName;
			tempStr = tempStr + " " + consigneeQual;

			tempStr = tempStr.length() >= 32 ? tempStr.substring(0, 32)
					: tempStr;
			rdsF.setConsignee(tempStr);
			rdsF.setCneeCode(rdsData.getConsigneeArol());
		}
		else
		{
			consigneeName = consigneeName.length() >= 32 ? consigneeName
					.substring(0, 32) : consigneeName;
			rdsF.setConsignee(consigneeName);
			rdsF.setCneeCode(rdsData.getConsigneeOrgnId());
		}
		if("AU".equalsIgnoreCase(rdsData.getLoadDischServ()) && rdsData.getDamageStatus()!=null && rdsData.getDamageStatus().trim().length()>0) { // A31
			logger.info("ASSIGNED CNEE : "+rdsData.getDamageStatus());
			rdsF.setConsignee(rdsData.getDamageStatus());
			rdsData.setDamageStatus("");
		}
		rdsF.setShipperArol(rdsData.getShipperArol());
		rdsF.setShipperOrgnId(rdsData.getShipperOrgnId());
		String shipperName = rdsData.getShipperName();
		shipperName = shipperName == null ? "" : shipperName;
		rdsF.setShipperName(shipperName);
		String shipperQual = rdsData.getShipperQualifier();
		shipperQual = shipperQual == null ? "" : shipperQual;
		rdsF.setShipperQualifier(shipperQual);
		if (shipperQual != null && !shipperQual.equals(""))
		{
			String tempStrShipper = shipperName.length() >= 29 ? shipperName
					.substring(0, 29) : shipperName;

			tempStrShipper = tempStrShipper + " " + shipperQual;
			tempStrShipper = tempStrShipper.length() >= 35 ? tempStrShipper.substring(0,
					35) : tempStrShipper;
			// logger.info("tempStrShipper>>>"+tempStrShipper+" for "+rdsData.getCtrno());
			rdsF.setShipper(tempStrShipper);
		}
		else
		{
			// logger.info("shipperName>>>"+shipperName+" for "+rdsData.getCtrno());
			shipperName = shipperName.length() >= 35 ? shipperName.substring(0,
					35) : shipperName;
			rdsF.setShipper(shipperName);
		}
		String creditStatus = rdsData.getCreditStatus();
		creditStatus = creditStatus == null ? "" : creditStatus;
		if (creditStatus.equals(""))
		{
			if (rdsData.getDatasource() != null && rdsData.getDatasource().equals("E"))
			{
				rdsF.setHsf2("H");
			}
		}
		else
		{
			rdsF.setHsf2(creditStatus);
		}
		rdsF.setHsf3(rdsData.getNotify());
		logger.info("CMDYAG : " + rdsData.getCmdyAg() + " - " + rdsF.getContainerNumber());
		if (rdsData.getCmdyAg() != null && rdsData.getCmdyAg().equals("N"))
			rdsF.setHsf4("");
		else
			rdsF.setHsf4(rdsData.getCmdyAg());
		logger.info("HSF4 : " + rdsF.getHsf4());
		rdsF.setHsf5(rdsData.getInbound());
		String hsf6 = rdsData.getDatasource();
		if (hsf6 != null)
			hsf6 = hsf6.equalsIgnoreCase("L") ? "" : hsf6;
		rdsF.setHsf6(hsf6);
		String hsf7 = rdsData.getFreeStorage();
		if (hsf7 != null)
			hsf7 = hsf7.equalsIgnoreCase("Y") ? "S" : "";
		rdsF.setHsf7(hsf7);
		String srstatus = CommonBusinessProcessor
				.srstatusTransformation(rdsData);
		rdsF.setSrstatus(srstatus);
		rdsF.setMilTcn(rdsData.getMilTcn());
		rdsF.setHazf(rdsData.getHazf());
		rdsF.setDeliveryDepot(rdsData.getDeliveryDepot());
		rdsF.setSrv("MAT");
		if(rdsData.getLoadType()!=null && rdsData.getLoadType().equalsIgnoreCase("E"))// A30
			rdsData.setEmptyFull("E"); // A30
		String emptyFull = rdsData.getEmptyFull();
		emptyFull = emptyFull == null ? "" : emptyFull;
		if (emptyFull.equals("F"))
			rdsF.setDir("IN");
		else if (emptyFull.equals("E"))
			rdsF.setDir("MTY");
		String ds = rdsData.getLoadDischServ();
		ds = ds == null ? "" : ds;
		if (ds.equals("AU"))
			ds = "AUT";
		else if (ds.equals("TY") || ds.equals("PJT") || ds.equals("P") || ds.equals("IM") || ds.equals("IMS") || ds.equals("IMR"))
			ds = "CY";
		rdsF.setDs(ds);
		rdsF.setOwner(rdsData.getOwnerLessor());
		// Booking number
		String tempDs = rdsF.getDs();
		String tempDsc = rdsF.getDsc();
		String tempComments = rdsF.getComments();
		if (tempDs != null && tempDsc != null)
		{
			if ((tempDs.equals("CY") || tempDs.equals("CON")) && !tempDsc.equals("M") && !tempDsc.equals("P"))
			{
			}
			else if (tempDs.equals("AUT"))
			{
				rdsF.setCommodity("AUTO");
			}
			else if (tempDs.equals("DS") || tempDs.equals("LCL") || tempDs.equals("PPC") )
			{
			}
			else{
				logger.info("**** Booking Number set to null, since DS is **** "+tempDs);
				bkgNbr = null;
			}

		}

		String rdd = rdsData.getRdd();
		if (rdd != null && rdd.length() > 0)
		{
			try
			{
				Date temp = new SimpleDateFormat("yyyy-MM-dd").parse(rdd);
				rdd = CalendarUtil.convertDateToString(temp);
			}
			catch (Exception e)
			{
				logger.error("RDD date parsing error : input : " + rdd);
			}
			bkgNbr = rdd;
		}
		rdsF.setBookingNumber(bkgNbr);
		rdsF.setLocationStatus("4");
		rdsF.setHazardousOpenCloseFlag(rdsData.getTrade());
		rdsF.setTrade(rdsData.getTrade());
		rdsF.setDport(rdsData.getDestinationPort());
		
		logger.debug("Container number 4 "+rdsF.getContainerNumber()+" discharge port"+rdsData.getDischargePort());
		rdsF.setDischargePort(rdsData.getDischargePort());
		rdsF.setTemp(rdsData.getTemp());
		
		rdsF.setCreateUser("supplemental");
		rdsF.setCreateDate(triggerDate);
		rdsF.setLastUpdateUser("supplemental");
		rdsF.setLastUpdateDate(triggerDate);
		rdsF.setTriggerDate(triggerDate);
		

		//Alaska changes
		rdsF.setTemp2(rdsData.getTemp2());
		rdsF.setMultiStop(rdsData.getMultiStop());
		
		//Alaska changes - Consignee Address
		rdsF.setConsigneeAddr(rdsData.getConsigneeAddr());
		rdsF.setConsigneeSuite(rdsData.getConsigneeSuite());
		rdsF.setConsigneeCity(rdsData.getConsigneeCity());
		rdsF.setConsigneeState(rdsData.getConsigneeState());
		rdsF.setConsigneeCountry(rdsData.getConsigneeCountry());
		rdsF.setConsigneeZipCode(rdsData.getConsigneeZipCode());

		return rdsF;
	}

	private TosRdsDataFinalMt populateTosRdsDataFinalMtExtra(TosStowPlanCntrMt ctrData, String type, String service)
	{
		TosRdsDataFinalMt rdsFext = new TosRdsDataFinalMt();
		//
		rdsFext.setContainerNumber(ctrData.getContainerNumber());
		rdsFext.setCheckDigit(ctrData.getCheckDigit());
		/*
		 * if (ctrData.getShipper() != null) {
		 * rdsFext.setShipper(ctrData.getShipper().length() >= 32 ? ctrData
		 * .getShipper().substring(0, 32) : ctrData.getShipper());
		 * rdsFext.setShipperName(ctrData.getShipper().length() >= 32 ? ctrData
		 * .getShipper().substring(0, 32) : ctrData.getShipper()); try { String
		 * shipperId = lookUp.getShipper(null, ctrData.getShipper());
		 * if(shipperId!=null) { shipperId =
		 * shipperId.length()>10?shipperId.substring(0, 10):shipperId; }
		 * rdsFext.setShipperOrgnId(shipperId);
		 * logger.info("Shipper Id from TOS : " + shipperId + " for " +
		 * ctrData.getShipper()); } catch(Exception e) {
		 * logger.error("Unable to get shipper id from TOS for " +
		 * ctrData.getShipper()); } } if (ctrData.getConsignee() != null) {
		 * rdsFext.setConsignee(ctrData.getConsignee().length() >= 32 ? ctrData
		 * .getConsignee().substring(0, 32) : ctrData.getConsignee());
		 * rdsFext.setConsigneeName(ctrData.getConsignee().length() >= 32 ?
		 * ctrData .getConsignee().substring(0, 32) : ctrData.getConsignee());
		 * try { String consigneeId = lookUp.getConsignee(null,
		 * ctrData.getConsignee()); if(consigneeId!=null) { consigneeId =
		 * consigneeId.length()>10?consigneeId.substring(0, 10):consigneeId; }
		 * rdsFext.setConsigneeOrgnId(consigneeId);
		 * rdsFext.setCneeCode(consigneeId);
		 * logger.info("Consginee Id from TOS : " + consigneeId + " for " +
		 * ctrData.getConsignee()); } catch(Exception e) {
		 * logger.error("Unable to get consignee id from TOS for " +
		 * ctrData.getConsignee()); } }
		 */
		rdsFext.setVesvoy(ctrData.getVesvoy());
		rdsFext.setSealNumber(ctrData.getSealNumber());
		rdsFext.setCell(ctrData.getCell());
		rdsFext.setCweight(ctrData.getCweight());
		// rdsFext.setHazf(ctrData.getHazf());
		//
		rdsFext.setSrv(ctrData.getSrv());
		rdsFext.setTruck(ctrData.getTruck());
		rdsFext.setPmd(ctrData.getPmd());
		rdsFext.setComments(ctrData.getComments());
		if (ctrData.getDir() != null && ctrData.getDir().equals("OUT"))
			rdsFext.setDir("IN");
		else
			rdsFext.setDir(ctrData.getDir());
		rdsFext.setDs(ctrData.getDs());
		// A29
		// Combine_auto logic is being applied on containers coming from RDS. 
		// Since we dont have one for containers coming from Stowplan, we set consignee as AUTOMOBILE if DS=AUT
		if(ctrData.getDs()!=null && ctrData.getDs().equalsIgnoreCase("AUT")) {
			rdsFext.setConsignee("AUTOMOBILE");
		}
		// rdsF.setDsc(ctrData.getDsc());
		rdsFext.setDss(ctrData.getDss());
		String typeCode = ctrData.getTypeCode();
		typeCode = typeCode == null ? "" : typeCode;
		typeCode = CommonBusinessProcessor.fixTypeCode(typeCode, ctrData.getContainerNumber(), ctrData.getConsignee());
		rdsFext.setTypeCode(typeCode);
		rdsFext.setOwner(ctrData.getOwner());
		String bookingNbr = ctrData.getBookingNumber();
		if (bookingNbr != null)
		{
			if (bookingNbr.length() == 7)
				bookingNbr = bookingNbr + "000";
		}
		// rdsFext.setBookingNumber(bookingNbr);
		rdsFext.setPlanDisp(ctrData.getPlanDisp());
		rdsFext.setLocationStatus("4");
		rdsFext.setLocationCategory(ctrData.getLocationCategory());
		rdsFext.setLocationRowDeck(ctrData.getLocationRowDeck());
		rdsFext.setLocationRunStackSectn(ctrData.getLocationRunStackSectn());
		rdsFext.setLocationTierStall(ctrData.getLocationTierStall());
		rdsFext.setLocationStallConfig(ctrData.getLocationStallConfig());
		rdsFext.setOrientation(ctrData.getOrientation());
		rdsFext.setStowRestrictionCode(ctrData.getStowRestrictionCode());

		String trade = ctrData.getHazardousOpenCloseFlag();
		// Karthik Rajendran - Commented on 03/14/2013 - Use the trade from the
		// file, not from API
		/*
		 * String dPort = ctrData.getDport(); String loadPort =
		 * ctrData.getLoadPort(); String loadTrade = ""; if(trade==null ||
		 * trade.equals("")) { trade =
		 * CommonBusinessProcessor.getTradeforPort(dPort); if (trade == null) {
		 * trade = "H"; } loadTrade =
		 * CommonBusinessProcessor.getTradeforPort(loadPort); if
		 * ("G".equals(loadTrade) || "F".equals(loadTrade)) { trade = loadTrade;
		 * } }
		 */
		rdsFext.setHazardousOpenCloseFlag(trade);
		rdsFext.setTrade(trade);
		rdsFext.setHsf6("E");
		if (trade != null)
			if (trade.equalsIgnoreCase("G") || trade.equalsIgnoreCase("F"))
				rdsFext.setHsf5("Y");
		// logger.info("Extra Container:"+rdsFext.getContainerNumber()+" Trade:"+trade+" Hsf6:"+rdsFext.getHsf6()+" Hsf5:"+rdsFext.getHsf5());
		BigDecimal tareWeight = ctrData.getTareWeight();
		tareWeight = CommonBusinessProcessor.fixTareWeight(tareWeight, typeCode);
		rdsFext.setTareWeight(tareWeight);
		rdsFext.setActualVessel(ctrData.getVesvoy() != null ? ctrData.getVesvoy()
				.substring(0, 3) : null);
		rdsFext.setActualVoyage(ctrData.getVesvoy() != null ? ctrData.getVesvoy()
				.substring(3, 6) : null);
		rdsFext.setLeg(ctrData.getLeg());
		rdsFext.setHgt(ctrData.getHgt());
		rdsFext.setStrength(ctrData.getStrength());
		rdsFext.setStowFlag(ctrData.getStowFlag());
		rdsFext.setAei(ctrData.getAei());
		rdsFext.setOdf(ctrData.getOdf());
		// logger.info(ctrData.getDport());
		rdsFext.setDport(ctrData.getDport());
		
		//Alaska change
		if(rdsFext.getDischargePort()==null || rdsFext.getDischargePort()=="" )
			rdsFext.setDischargePort(ctrData.getDischargePort());
		
		logger.info("VesselService:" + service + ", Discharge Port :" + rdsFext.getDischargePort());
		
		rdsFext.setLoadPort(ctrData.getLoadPort());
		rdsFext.setTemp(ctrData.getTemp());
		rdsFext.setTempMeasurementUnit(ctrData.getTempMeasurementUnit());
		rdsFext.setDamageCode(ctrData.getDamageCode());
		rdsFext.setChassisNumber(ctrData.getChassisNumber());
		//
		rdsFext.setCreateUser("newves");
		rdsFext.setCreateDate(triggerDate);
		rdsFext.setLastUpdateUser("newves");
		rdsFext.setLastUpdateDate(triggerDate);
		rdsFext.setTriggerDate(triggerDate);
		rdsFext.setAction("NVI");
		rdsFext.setAdate(triggerDate);
		DateFormat dateFormat = new SimpleDateFormat("HH:mm");
		Calendar cal = Calendar.getInstance();
		rdsFext.setAtime(dateFormat.format(cal.getTime()));
		return rdsFext;
	}

	private boolean transformRdsDataToRdsFinal(String vesvoy, String type, ArrayList<TosRdsDataMt> rdsData)
	{
		logger.debug("Transforming RDS data into RDS Final table");
		ArrayList<TosRdsDataFinalMt> rdsDataFinalList;
		ArrayList<TosRdsDataFinalMt> rdsDataFinalListExt;
		rdsDataFinalList = new ArrayList<TosRdsDataFinalMt>();
		rdsDataFinalListExt = new ArrayList<TosRdsDataFinalMt>();
		String vesselService = null;
		// gettting vessel service
		try
		{
			if (lookUp == null)
				lookUp = new TosLookup();
		}
		catch (Exception ex1)
		{
			logger.error("Failed to create TosLookup in transformRdsDataToRdsFinal");
		}
		vesselService = lookUp.getVesselService(vesvoy);
		logger.info("vesselService is : " + vesselService);
		// getting service end

		if (type.equals(PRIMARY))
		{
			// ArrayList<TosRdsDataMt> rdsData =
			// NewVesselDao.getRdsDataForVesvoy(vesvoy, triggerDate);
			this.ocrDataList = NewVesselDao.getOCRDataForVesvoy(vesvoy, null, null);
			if (this.ocrDataList == null || this.ocrDataList.size() == 0)
			{
				logger.error("Error: No stowplan data found for the vesvoy " + vesvoy);
				nvLogger.addError(vesvoy, "", "Error: Unable to get stowplan data for " + vesvoy);
				return false;
			}
			ArrayList<TosRdsDataMt> rdsDataClone = new ArrayList<TosRdsDataMt>();
			for (TosRdsDataMt rds : rdsData)
			{
				rdsDataClone.add(rds.clone());
			}
			rdsData = CommonBusinessProcessor.convCon2Cy(rdsData, this.ocrDataList);
			logger.debug("RDS Data size before combine - " + rdsData.size());
			//A29
			rdsData = CommonBusinessProcessor.combineAutos(rdsData, rdsDataClone); // Combining AUTO records if any
			rdsData = CommonBusinessProcessor.combineShipmentNoCreditStatus(rdsData); // Combining CY/other records if any
			rdsData = CommonBusinessProcessor.eliminateDuplicatesInRDS(rdsData);
			logger.debug("RDS Data size after combine - " + rdsData.size());
			for (int i = 0; i < rdsData.size(); i++)
			{
				TosRdsDataMt rdsD = rdsData.get(i);
				String ctrno = rdsD.getCtrno();
				TosStowPlanCntrMt ocrD = new TosStowPlanCntrMt();
				for (int o = 0; o < this.ocrDataList.size(); o++)
				{
					TosStowPlanCntrMt tempOcrD = this.ocrDataList.get(o);
					String ocrCtrno = tempOcrD.getContainerNumber();
					if (ctrno.equals(ocrCtrno))
					{
						ocrD = tempOcrD;
					}
				}
				TosRdsDataFinalMt rdsFinal = populateTosRdsDataFinalMt(rdsD, ocrD, type, vesselService, rdsDataClone);
				rdsDataFinalList.add(rdsFinal);
			}
			logger.info("RDS Before validation : " + rdsDataFinalList.size());
			rdsDataFinalList = CommonBusinessProcessor.validateContainers(rdsDataFinalList);
			logger.info("RDS After validation : " + rdsDataFinalList.size());
			logger.info("RDS Final - From DOWNLOAD : " + rdsDataFinalList.size());
			NewVesselDao.insertRdsDataFinal(rdsDataFinalList);
			// Find RDS data for .HON file containers/these might have the
			// eastbound data.
			ArrayList<TosRdsDataMt> eastBoundData = NewVesselDao.getEastBoundContainers();
			eastBoundData = eastBoundData == null ? new ArrayList<TosRdsDataMt>() : eastBoundData;
			ArrayList<TosRdsDataMt> rawEastBoundData = new ArrayList<TosRdsDataMt>();
			// rawEastBoundData.addAll(eastBoundData);
			for (TosRdsDataMt rds : eastBoundData)
			{
				rawEastBoundData.add(rds.clone());
			}
			logger.info("rawEastBoundData size - " + rawEastBoundData.size());
			eastBoundData = CommonBusinessProcessor.convCon2Cy(eastBoundData, this.ocrDataList);
			logger.info("Eastbound RDS Data size before combine - " + eastBoundData.size());
			eastBoundData = CommonBusinessProcessor.combineAutos(eastBoundData, rawEastBoundData); //A29
			eastBoundData = CommonBusinessProcessor.combineShipmentNoCreditStatus(eastBoundData);
			eastBoundData = CommonBusinessProcessor.eliminateDuplicatesInRDS(eastBoundData);
			logger.info("Eastbound RDS Data size after combine - " + eastBoundData.size());
			logger.info("rawEastBoundData size after cobmine - " + rawEastBoundData.size());
			// boolean eastBoundDataGotUpdated = false;
			ArrayList<String> ebCntrsToBeUpdated = new ArrayList<String>();
			ArrayList<String> ebCntrsToBeDeleted = new ArrayList<String>();
			ArrayList<TosRdsDataMt> genScript = new ArrayList<TosRdsDataMt>();
			this.additionalCtrFromHon = new ArrayList<TosRdsDataFinalMt>();
			for (int o = 0; o < this.ocrDataList.size(); o++)
			{
				TosStowPlanCntrMt ocrData = this.ocrDataList.get(o);
				String ocrCtrno = ocrData.getContainerNumber();
				logger.info("OCR Ctrno : "+ocrCtrno);
				boolean foundInRds = false;
				TosRdsDataMt foundRdsData = null;
				// int foundRdsIndex = -1;
				TosRdsDataMt foundEbRdsData = null;
				// int foundEbRdsIndex = -1;
				for (int r = 0; r < rdsData.size(); r++)
				{
					TosRdsDataMt rdsD = rdsData.get(r);
					String ctrno = rdsD.getCtrno();
					if (ocrCtrno.equals(ctrno))
					{
						foundInRds = true;
						foundRdsData = rdsD;
						// foundRdsIndex = r;
						break;
					}
				}
				logger.info("foundInRds : "+foundInRds);
				if (foundInRds) // Container has RDS data from download file,
								// Still need to check whether we should pick
								// incoming
				// RDS data or from east bound data.
				{
					int ebScore = 0;
					String iShipNo, iLoadPort, iDischPort = "";
					String ebShipNo, ebLoadPort, ebDischPort = "";
					for (int e = 0; e < eastBoundData.size(); e++)
					{
						TosRdsDataMt eastRdsData = eastBoundData.get(e);
						String ctrno = eastRdsData.getCtrno();
						if (ocrCtrno.equals(ctrno))
						{
							foundEbRdsData = eastRdsData;
							// foundEbRdsIndex = e;
							break;
						}
					}
					if (foundRdsData != null && foundEbRdsData != null) // Container
																		// has
																		// both
																		// incoming
																		// RDS
																		// record
																		// and
																		// east
																		// bound
																		// record
					{
						iShipNo = foundRdsData.getShipno();
						iShipNo = iShipNo == null ? "" : iShipNo;
						iLoadPort = foundRdsData.getLoadPort();
						iLoadPort = iLoadPort == null ? "" : iLoadPort;
						iDischPort = foundRdsData.getDischargePort();
						iDischPort = iDischPort == null ? "" : iDischPort;
						ebShipNo = foundEbRdsData.getShipno();
						ebShipNo = ebShipNo == null ? "" : ebShipNo;
						ebLoadPort = foundEbRdsData.getLoadPort();
						ebLoadPort = ebLoadPort == null ? "" : ebLoadPort;
						ebDischPort = foundEbRdsData.getDischargePort();
						ebDischPort = ebDischPort == null ? "" : ebDischPort;
						if (ebShipNo.equals("") || iShipNo.equals(""))
						{
							if (ebLoadPort.equalsIgnoreCase(iLoadPort) && ebDischPort.equalsIgnoreCase(iDischPort))
								ebScore = 1;
						}
						else
						{
							if (ebShipNo.equalsIgnoreCase(iShipNo))
								ebScore = 1;
						}
						if (ebScore == 1) // If east bound data scores then we
											// should transform container with
											// east bound data.
						// Otherwise, delete eb data.
						{
							logger.info("Picking up eb data over load RDS for " + foundEbRdsData.getCtrno());
							ebCntrsToBeUpdated.add(foundEbRdsData.getCtrno());
							foundEbRdsData.setLeg("W");
							foundEbRdsData.setVes(vesvoy.substring(0, 3));
							foundEbRdsData.setVoy(vesvoy.substring(3, 6));
							TosRdsDataFinalMt rdsFinal1 = populateTosRdsDataFinalMt(foundEbRdsData, ocrData, type, vesselService, rdsDataClone);
							if (rdsFinal1 != null)
								rdsDataFinalListExt.add(rdsFinal1);
						}
						else
						{
							ebCntrsToBeDeleted.add(foundEbRdsData.getCtrno());
						}
					}
				}
				else if (!foundInRds) // .HON File container don't have RDS, So
										// pickup the data from east bound
				{
					TosRdsDataFinalMt rdsFinal1 = null;
					boolean foundInEastBoundRds = false;
					//System.out.print("EB Ctrno : ");
					for (int e = 0; e < eastBoundData.size(); e++)
					{
						TosRdsDataMt eastRdsData = eastBoundData.get(e);
						String ctrno = eastRdsData.getCtrno();
						System.out.print(ctrno);
						if (ocrCtrno.equals(ctrno)) // Container has east bound
													// data, update its
													// direction and vesvoy
						{
							foundInEastBoundRds = true;
							logger.info("Picking up eb data for " + eastRdsData.getCtrno());
							ebCntrsToBeUpdated.add(eastRdsData.getCtrno());
							eastRdsData.setLeg("W");
							eastRdsData.setVes(vesvoy.substring(0, 3));
							eastRdsData.setVoy(vesvoy.substring(3, 6));
							rdsFinal1 = populateTosRdsDataFinalMt(eastRdsData, ocrData, type, vesselService, rdsDataClone);
							break;
						}
					}
					logger.info("");
					logger.info("foundInEastBoundRds : " + foundInEastBoundRds);
					if (!foundInEastBoundRds) // If container don't have east
												// bound data too then populate
												// it only with stow plan
					{
						logger.info("Picking up data from stowplan for " + ocrData.getContainerNumber());
						rdsFinal1 = populateTosRdsDataFinalMtExtra(ocrData, type, vesselService);
						this.additionalCtrFromHon.add(rdsFinal1);
					}
					if (rdsFinal1 != null)
						rdsDataFinalListExt.add(rdsFinal1);
				}
			}
			if (ebCntrsToBeUpdated.size() > 0)
			{
				logger.info("Deleting the non-chosen Westbound Rds containers...");
				logger.info(ebCntrsToBeUpdated);
				NewVesselDao.deleteRdsContainers(ebCntrsToBeUpdated, "W");
				logger.info("Updating chosen eastbound data back to RDS table with W and primary vv");
				for (int u = 0; u < ebCntrsToBeUpdated.size(); u++)
				{
					String cntrno = ebCntrsToBeUpdated.get(u);
					for (int r = 0; r < rawEastBoundData.size(); r++)
					{
						TosRdsDataMt temp = rawEastBoundData.get(r);
						if (temp.getCtrno().equals(cntrno))
						{
							genScript.add(temp.clone());
							temp.setLeg("W");
							temp.setVes(vesvoy.substring(0, 3));
							temp.setVoy(vesvoy.substring(3, 6));
							/*
							 * ArrayList<String> shipNoList = new
							 * ArrayList<String>(
							 * Arrays.asList(temp.getShipno().split("-")));
							 * if(shipNoList!=null && shipNoList.size()>1)
							 * temp.setShipno(shipNoList.get(0));
							 */
							rawEastBoundData.set(r, temp);
							logger.info("EB Update - " + cntrno + ", " + temp.getShipno());
						}
					}
				}
				NewVesselDao.updateRdsData(rawEastBoundData);
			}
			if (ebCntrsToBeDeleted.size() > 0)
			{
				for (int e = 0; e < ebCntrsToBeDeleted.size(); e++)
				{
					String cntrno = ebCntrsToBeDeleted.get(e);
					for (int r = 0; r < rawEastBoundData.size(); r++)
					{
						TosRdsDataMt temp = rawEastBoundData.get(r);
						if (temp.getCtrno().equals(cntrno))
						{
							genScript.add(temp.clone());
						}
					}
				}
				logger.info("Deleting the non-chosen EB Rds containers...");
				logger.info(ebCntrsToBeDeleted);
				NewVesselDao.deleteRdsContainers(ebCntrsToBeDeleted, "E");
			}
			if (genScript.size() > 0)
			{
				CommonBusinessProcessor.genScriptAndSend(genScript, vesvoy+"W");
			}
			logger.info("RDS Before validation : " + rdsDataFinalListExt.size());
			rdsDataFinalListExt = CommonBusinessProcessor.validateContainers(rdsDataFinalListExt);
			logger.info("RDS After validation : " + rdsDataFinalListExt.size());
			logger.info("RDS Final - From .HON : " + rdsDataFinalListExt.size());
			logger.info("RDS Final - From .HON with No eastbound data : " + this.additionalCtrFromHon.size());
			NewVesselDao.insertRdsDataFinal(rdsDataFinalListExt);
		}
		return true;
	}

	public enum EnumRDS
	{
		ves, voy, leg, containerNumber, shipmentNo, dataSource, routeToPort, loadPort, dischPort, placeDelCode, temp,
		loadService, dischService, hazf, filler1, cntrOversize, primaryCarrier, initialVes, initialVoy, initialLeg,
		inbond, notify, creditStatus, transit, trade, loadType, shipperArolIden, shipperOrgName, shipperOrgNameQual,
		consigneeArolIden, consigneeOrgName, consigneeCo, consigneeOrgNameQual, consigneeAddrLine1, consigneeSuite,
		consigneeCity, consigneeState, consigneeCountry, consigneeZipCode, consigneeDepartment, consigneeTitle,
		consigneeLastName, consigneeFirstName, cmdyDesc, cmdySrvrptDesc, cmdyAg, cmdyHhg, cell, grossWt, highWtFlag,
		sealNo, ownerLessor, damageStatus, emptyFull, ecosRetPort, checkDigit, specMsg1, specMsg2, specMsg3, cnsgAreaCode,
		cnsgPhone1, cnsgPhone2, poNumber, filler2, shmtDestCityCode, vesselType, consigneeFaxNotifyOrgnName,
		consigneeFaxNotifyAttnParty, consigneeFaxNotifyCtryCode, consigneeFaxNotifyAreaCode, consigneeFaxNotifyExch,
		consigneeFaxNotifyStation, consigneeFaxNotifyExt, consignee2NDFaxCtryCode, consignee2NDFaxAreaCode,
		consignee2NDFaxExch, consignee2ndFaxStation, consignee2NDFaxExt, truckerFaxNotifyOrgnName, truckerFaxNotifyAttnParty,
		truckerFaxNotifyCtryCode, truckerFaxNotifyAreaCode, truckerFaxNotifyExch, truckerFaxNotifyStation, truckerFaxNotifyExt,
		trucker2NDFaxCtryCode, trucker2NDFaxAreaCode, trucker2NDFaxExch, trucker2ndFaxStation, trucker2NDFaxExt,
		headQuartersFaxNotifyOrgnName, headQuartersFaxNotifyAttnParty, headQuartersFaxNotifyCtryCode,
		headQuartersFaxNotifyAreaCode, headQuartersFaxNotifyExch, headQuartersFaxNotifyStation, headQuartersFaxNotifyExt,
		headQuarters2NDFaxCtryCode, headQuarters2NDFaxAreaCode, headQuarters2NDFaxExch, headQuarters2ndFaxStation, headQuarters2NDFaxExt, notifyFaxNotifyOrgnName,
		notifyFaxNotifyAttnParty, notifyFaxNotifyCtryCode, notifyFaxNotifyAreaCode, notifyFaxNotifyExch, notifyFaxNotifyStation,
		notifyFaxNotifyExt, notify2NDFaxCtryCode, notify2NDFaxAreaCode, notify2NDFaxExch, notify2ndFaxStation, notify2NDFaxExt,
		carrierCode, shipperRef1Text, milContNbr, milConsignee, milConsigner, govtBlNbr, milPortCall, milTcn, milVNbr,
		milWeight, milCubeFt, milPackNbr, detentoinFreeDays, storageFreeDays, rdd, bookingCsrId, npArcrCarrCode, npOrgnName,
		dPort, csrShipperOrgnId, csrConsigneeOrgnId, csrSpecialSvcArray1, csrSpecialSvcArray2, csrSpecialSvcArray3,
		csrSpecialSvcArray4, csrSpecialSvcArray5, csrClxRoute, csrClxHonCall, csrClxGumCall, filler3, delimeter, 
		
		//Alaska Changes
		temp2, multiStop
	}

	public static void cleanUp()
	{
		logger.info("RDS cleanUp start");
		if (lookUp != null)
		{
			lookUp.close();
			lookUp = null;
		}
		logger.info("RDS cleanUp end");
	}
}
