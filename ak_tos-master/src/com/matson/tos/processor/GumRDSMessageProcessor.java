package com.matson.tos.processor;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.matson.cas.refdata.RefDataLookup;
import com.matson.cas.refdata.mapping.TosDestPodData;
import com.matson.cas.refdata.mapping.TosGumRdsDataFinalMt;
import com.matson.cas.refdata.mapping.TosGumRdsDataMt;
import com.matson.cas.refdata.mapping.TosGumStPlanCntrMt;
import com.matson.cas.erd.service.data.VesselVO;
import com.matson.cas.service.ftp.FtpBizException;
import com.matson.cas.service.ftp.FtpProxyGetterBiz;
import com.matson.cas.service.ftp.FtpProxyListBiz;
import com.matson.tos.dao.NewVesselDaoGum;
import com.matson.tos.dao.TosLookup;
import com.matson.tos.exception.NewVesselLogger;
import com.matson.tos.reports.gum.GumNewvesReport;
import com.matson.tos.util.TosRefDataUtil;
import com.matson.tos.vo.RDSFields;
import com.matson.tos.vo.XmlFields;

/*
 *
 * #		Date			Author						Description
 * ---		-----------		------------------			---------------------
 * 1		03/20/2013		Karthik Rajendran			Class file created from HON Newves RDSMessageProcessor
 * 2		03/23/2013		Karthik Rajendran			Changed:Booking #, cneecode fix. LocationStatus removed, trade removed.
 * 3		03/25/2013		Karthik Rajendran			Commented: setting holds from OHL check.
 * 														Added: Set orientation from RDS, not from stow plan.
 * 														Get owner from RDS, Get load port from stowplan.
 * 4		03/26/2013		Karthik Rajendran			Booking number fix, consignee substring upto 35 characters, cnotes concatenation fix.
 * 5		03/27/2013		Karthik Rajendran			Added: assignTrucker() invocation.
 * 6		03/28/2013		Karthik Rajendran			Removed: vessel service call-not required here for Guam
 * 7		03/29/2013		Karthik Rajendran			Commented: GumNewvesProcessorHelper().startNewVessProc() invocation.
 * 														Added: Sending email success notification once the RDS is processed.
 * 8		04/02/2013		Karthik Rajendran			Added: Null check on VesselVO usage
 * 9		04/03/2013		Karthik Rajendran			Added: BL-NBR Fix: HOME DEPOT Containers should end with 000 and for others get it from RDS/stowpla as it is.
 * 														Added: Set commodity for stowplan containers.
 * 10		04/04/2013		Karthik Rajendran			Added: Persist non-primary vvd containers into RDS initial table without discarding.
 * 11		04/16/2013		Karthik Rajendran			Added: skip empty line breaks
 * 12		09/16/2013		Karthik Rajendran			Added: Correcting disch ports using DEST_POD_LOOKUP table
 * 13		10/29/2013		Karthik Rajendran			Changed: Set Create_user,last_update_user to "gumnewves" 
 *
 *
 */


public class GumRDSMessageProcessor extends AbstractFileProcessor {

	private static Logger logger = Logger.getLogger(GumRDSMessageProcessor.class);
	private ArrayList<String> contentLines;
	private List<XmlFields> xmlFields;
	private ArrayList<RDSFields> rdsData;
	public ArrayList<String> vesselVoyageList;
	public ArrayList<TosGumRdsDataFinalMt> additionalCtrFromHon;
	public ArrayList<TosGumStPlanCntrMt> ocrDataList;
	public static final String PRIMARY = "primary";
	public static final String SUPPLEMENT = "supplement";
	private static TosLookup lookUp=null;
	private static String userPrefVVD = null;
	public Date triggerDate = null;
	private NewVesselLogger nvLogger = NewVesselLogger.getInstance();;
	String ftpFileName = null;
	int ftpProxyId = -1;
	int ftpProxyArchId = -1;
	boolean isFileProcessed = false;
	public static HashMap<String, String>shipperRefMap = null;
	private String errorLines		= null;

	public GumRDSMessageProcessor(String userEntrdVVD) {
		try {
			lookUp = new TosLookup();
			shipperRefMap = new HashMap<String, String>();
			userPrefVVD = userEntrdVVD;
			logger.info("User preferred VVD: " + userPrefVVD);
			processFiles();
			nvLogger.sendGumNewVessErrors();
			userPrefVVD = null;
		} catch (Exception e) {
			logger.error("Failed to create TosLookup in RDSMessageProcessor");
		}finally {
			logger.info("Cleaning up of static variables start");
			CommonBusinessProcessor.outboundExportVesselMap = null;
			CommonBusinessProcessor.outboundNIVesselMap = null;
			CommonBusinessProcessor.outboundVesselMap = null;
			CommonBusinessProcessor.portCodeTradeMap = null;
			CommonBusinessProcessor.arrivalDateMap = null;
			shipperRefMap = null;
			logger.info("Cleaning up of static variables end");
		}
	}

	public void processFiles() {
		try {
			ftpProxyId = Integer.parseInt(TosRefDataUtil.getValue("RDS_IN_FTP_ID"));
			ftpProxyArchId = Integer.parseInt(TosRefDataUtil.getValue("RDS_ARCH_FTP_ID"));
			int timeout = Integer.parseInt(TosRefDataUtil.getValue("FTP_TIMEOUT"));
			logger.debug("FTP timeout retrieved is: " + timeout);
			FtpProxyListBiz list = new FtpProxyListBiz();
			list.setTimeout(timeout);
			String[] rdsFiles = list.getFileNames(ftpProxyId, "gumrds", null);
			if(rdsFiles==null||rdsFiles.length<=0)
				return;
			List<String> rdsFilesList = (List<String>) Arrays.asList(rdsFiles);
			Collections.sort(rdsFilesList, Collections.reverseOrder());
			FtpProxyGetterBiz getter = new FtpProxyGetterBiz();
			getter.setTimeout(timeout);
			//
			xmlFields = CommonBusinessProcessor.getFields("/newvesXml/RDS.xml");
			triggerDate = new Date();
			//
			for(int i=0; rdsFilesList!=null&&i<rdsFilesList.size(); i++)
			{
				ftpFileName = rdsFilesList.get(i);
				logger.debug("Processing file: " + ftpFileName);
				if (-1 == ftpFileName.indexOf(".")) {
					continue;
				}
				String contents = getter.getFileText(ftpProxyId, ftpFileName, "gumrds");
				String startWFTime  = (new SimpleDateFormat("MM-dd-yyyy_HH-mm-ss")).format(new Date()).toString();
				CommonBusinessProcessor.archivetoFTP(contents, ftpProxyArchId, ftpFileName+"_"+startWFTime,"gumrds");
				vesselVoyageList = new ArrayList<String>();
				rdsData = new ArrayList<RDSFields>();
				//
				isFileProcessed = false;
				if(ftpFileName.contains("BRDSIN.000"))
					processFile(contents);
				else
					logger.info(ftpFileName + " is not an RDS named file.");
				//
				if(isFileProcessed)
				{
					CommonBusinessProcessor.deleteFtpFiles(ftpProxyId, ftpFileName,"gumrds");
				}
			}
		} catch (FtpBizException ftpEx) {
			ftpEx.printStackTrace();
			logger.error("FTP error found: ", ftpEx);
			nvLogger.addFtpError(""+ftpProxyId, "FTP ERROR: Unable to get into FTP");
		} catch (Exception e) {
			logger.error("Exception: ", e);
			e.printStackTrace();
		} finally {
			cleanUp();
			CommonBusinessProcessor.cleanUp();
		}

	}

	public void startRDSProcessTransformations(String vvd, ArrayList<TosGumRdsDataMt> primaryCntrList, ArrayList<TosGumRdsDataMt> supplementalCntrList)
	{
			boolean isProcessed = startPrimaryVesselProc(vvd, primaryCntrList);
			if(isProcessed) {
				GumNewvesReport.createReport(GumNewvesReport.DOWNLOAD_DISCREPANCIES, vvd, "PREVIEW - ");
				//new GumNewvesProcessorHelper().startNewVessProc(vvd);
				nvLogger.sendGumRdsProcessSuccess(vvd, "");
			}
		isFileProcessed = true;
	}

	public boolean startPrimaryVesselProc(String vvd, ArrayList<TosGumRdsDataMt> rdsData)
	{
			if(vvd != null)
			{
				String vesvoy = vvd.substring(0, 6);
				boolean isTransformed = transformRdsDataToRdsFinal(vesvoy, PRIMARY, rdsData);
				if(!isTransformed)
					return false;
				logger.info("Primary RDS data has been transformed to RDS Final table.");
				ArrayList<TosGumRdsDataFinalMt> rdsDataFinal = NewVesselDaoGum.getGumRdsDataFinalForVesvoy(vesvoy);
				rdsDataFinal = GumCommonBusinessProcessor.dscTransformation(rdsDataFinal);
				rdsDataFinal = GumCommonBusinessProcessor.cargoNotesTransformation(rdsDataFinal, this.ocrDataList);
				rdsDataFinal = GumCommonBusinessProcessor.fixMtys(rdsDataFinal);
				rdsDataFinal = GumCommonBusinessProcessor.processAutos(rdsDataFinal);
				rdsDataFinal = GumCommonBusinessProcessor.geartrashContainersTransformation(rdsDataFinal);
				rdsDataFinal = GumCommonBusinessProcessor.crstatusHoldsCheck1(rdsDataFinal);
				rdsDataFinal = GumCommonBusinessProcessor.crstatusHoldsCheck2(rdsDataFinal);
				rdsDataFinal = GumCommonBusinessProcessor.crstatusHoldsCheck4(rdsDataFinal);
				rdsDataFinal = GumCommonBusinessProcessor.crstatusHoldsCheck5(rdsDataFinal);
				//rdsDataFinal = GumCommonBusinessProcessor.crstatusHoldsCheck6(rdsDataFinal, this.ocrDataList);
				rdsDataFinal = GumCommonBusinessProcessor.commodityTransformation(rdsDataFinal);
				rdsDataFinal = GumCommonBusinessProcessor.updateConsignee(rdsDataFinal, rdsData);
				rdsDataFinal = GumCommonBusinessProcessor.calculateLastFreeDayDueDate(rdsDataFinal);
				rdsDataFinal = GumCommonBusinessProcessor.dsGumTransformations(rdsDataFinal);
				rdsDataFinal = GumCommonBusinessProcessor.assignTrucker(rdsDataFinal);

				//
				NewVesselDaoGum.updateRdsDataFinal(rdsDataFinal);
				this.additionalCtrFromHon = null;
			}
		return true;
	}
	//

	//
	public void processFile(String contents)
	{
		errorLines = "";
		contentLines = new ArrayList<String> (Arrays.asList(contents.split("\n")));
		if(contentLines.size()>=1)
		{
			logger.debug("Processing raw RDS data...");
			for (int i=0; i<contentLines.size(); i++)
			{
				try{
					String line = contentLines.get(i);
					line = line.replaceAll("\\x00", " ");
					if (!line.startsWith("") && !line.startsWith(" "))
						processLine(line, 0);
					else if (line.startsWith(" "))
					{
						errorLines = errorLines + line + "\n\n";
					}
				} catch(Exception e) {
					e.printStackTrace();
					logger.error("Error: Unable to process the RDS file. Please verify the file errors.");
					nvLogger.addFileError(ftpFileName, "Unable to process the RDS file. <br/><br/>"+ e.getMessage());
					return;
				}
			}
			if(errorLines.length() > 0) {
				String message = "VVD not found in the records, unable to parse the below lines <br/><br/>";
				String errorMessage = message + "<pre>" + errorLines + "</pre><br/><br/>";
				nvLogger.addFileError(ftpFileName, errorMessage);
			}

			logger.info("Incoming RDS Data size: "+ rdsData.size());
			//
			ArrayList<String> primaryVesselVoyageList = new ArrayList<String>();
			ArrayList<String> nonPrimaryVesselVoyageList = new ArrayList<String>();
			// Identifying primary and supplemental vvd.
			// If user enters a vvd, it should be added to the primary vvd list to be processed.
			// Rest of the vvd are added to supplemental vvd list
			if(userPrefVVD!=null)
			{
				if(vesselVoyageList.contains(userPrefVVD))
				{
					String vesselOpr = "";
					try {
						VesselVO vvo = CommonBusinessProcessor.getVesselDetails(userPrefVVD.substring(0, 3));
						if(vvo!=null)
							vesselOpr = vvo.getVessOpr();
						else
						{
							logger.error("Unable to get vessel details - VesselVO-->"+vvo+"\n");
						}
					}
					catch(Exception e)
					{
						logger.error("Problem in retreiving vessel operator for "+userPrefVVD.substring(0, 3));
					}
					vesselOpr = vesselOpr==null?"":vesselOpr;

					logger.info("vesselOpr:"+vesselOpr);
					boolean isStowPlanAvailable = NewVesselDaoGum.isStowPlanAvailForVesvoy(userPrefVVD.substring(0, 6));
					boolean isDcmDataAvailable = NewVesselDaoGum.isDCMAvailForVesselWithVVD(userPrefVVD.substring(0, 6));
					if((isStowPlanAvailable && isDcmDataAvailable)||(isStowPlanAvailable && vesselOpr.equalsIgnoreCase("CSX")))
					{
						primaryVesselVoyageList.add(userPrefVVD);
						vesselVoyageList.remove(userPrefVVD);
						nonPrimaryVesselVoyageList.addAll(vesselVoyageList);
					}
					else
					{
						//nvLogger.addError(userPrefVVD, null, userPrefVVD+" dont have stowplan/DCM.");
						if (!isStowPlanAvailable)
						{
							nvLogger.addError(userPrefVVD, null, " does not have stowplan.");
							isFileProcessed = false;
							return;
						}
						if (!isDcmDataAvailable)
						{
							nvLogger.addError(userPrefVVD, null, " does not have DCM.");
							isFileProcessed = false;
							return;
						}
					}

				}
				else
				{
					nvLogger.addError(userPrefVVD, null, userPrefVVD+" is not available in the RDS.");
					isFileProcessed = false;
					return;
				}
			}
			// If user has not entered vvd and running the process or process triggered by scheduler
			// List of vvd should be identified by checking RDS final table to mark it as primary or supplemental.
			else
			{
				for(int i=0; i<vesselVoyageList.size(); i++)
				{
					String vvd = vesselVoyageList.get(i);
					String vv = vvd.substring(0, 6);
					boolean isPrimary = NewVesselDaoGum.isPrimaryVesvoy(vv);
					if(isPrimary)
					{
						String vesselOpr = "";
						boolean isStowPlanAvailable = NewVesselDaoGum.isStowPlanAvailForVesvoy(vv.substring(0, 6));
						boolean isDcmDataAvailable = NewVesselDaoGum.isDCMAvailForVesselWithVVD(vv.substring(0, 6));
						try {
							VesselVO vvo = CommonBusinessProcessor.getVesselDetails(vv.substring(0, 3));
							if(vvo!=null)
								vesselOpr = vvo.getVessOpr();
							else
							{
								logger.error("Unable to get vessel details - VesselVO-->"+vvo+"\n");
							}
						}
						catch(Exception e)
						{
							e.printStackTrace();
							logger.error("Problem in retreiving vessel operator for "+vv.substring(0, 3)+"\n"+e);
						}
						vesselOpr = vesselOpr==null?"":vesselOpr;
						/*if((isStowPlanAvailable && isDcmDataAvailable)||(isStowPlanAvailable && vesselOpr.equalsIgnoreCase("CSX")))
						{
							primaryVesselVoyageList.add(vvd);
							logger.info(vvd + " added to primary");
						} else if (isStowPlanAvailable && !isDcmDataAvailable && !vesselOpr.equalsIgnoreCase("CSX")) {
							
						}
						else
						{
							nonPrimaryVesselVoyageList.add(vvd);
							logger.info(vvd + " added to supplemental-inner");
						}*/
						if (!vesselOpr.equalsIgnoreCase("CSX")) {
							if (isStowPlanAvailable || isDcmDataAvailable) {
									if (!isStowPlanAvailable)
									{
										nvLogger.addError(vvd, null, " does not have stowplan.");
										isFileProcessed = false;
										continue;
									}
									if (!isDcmDataAvailable)
									{
										nvLogger.addError(vvd, null, " does not have DCM.");
										isFileProcessed = false;
										continue;
									}
									primaryVesselVoyageList.add(vvd);
									logger.info(vvd + " added to primary");
							}else {
									nonPrimaryVesselVoyageList.add(vvd);
									logger.info(vvd + " added to supplemental-outer");
							}
						}else if (vesselOpr.equalsIgnoreCase("CSX") && isStowPlanAvailable)
						{
							primaryVesselVoyageList.add(vvd);
							logger.info(vvd + " added to primary");
						}
					}
					else
					{
						nonPrimaryVesselVoyageList.add(vvd);
						logger.info(vvd + " added to supplemental-outer");
					}
				}
			}

			//
			logger.info("Primary vv : "+primaryVesselVoyageList.size());
			logger.info("Non-Primary vv : "+nonPrimaryVesselVoyageList.size());
			if(primaryVesselVoyageList.size()>0) {
				logger.info("Building containers list for primary vvd "+primaryVesselVoyageList.get(0));
				ArrayList<TosGumRdsDataMt> primaryCntrList = buildCntrList(primaryVesselVoyageList.get(0), rdsData);
				logger.info("Primary containers : "+primaryCntrList.size());
				NewVesselDaoGum.insertRDSData(primaryCntrList);
				startRDSProcessTransformations(primaryVesselVoyageList.get(0), primaryCntrList, null);
			}
			if(nonPrimaryVesselVoyageList.size()>0)
			{
				ArrayList<TosGumRdsDataMt> nonPrimaryCntrList = new ArrayList<TosGumRdsDataMt>();
				for(int n=0; n<nonPrimaryVesselVoyageList.size(); n++)
				{
					String vvd = nonPrimaryVesselVoyageList.get(n);
					logger.info("Building containers list for non-primary vvd "+vvd);
					ArrayList<TosGumRdsDataMt> tempList = buildCntrList(vvd, rdsData);
					if(tempList!=null && tempList.size()>0)
					{
						nonPrimaryCntrList.addAll(tempList);
						logger.info("Containers List size for "+vvd+" "+tempList.size());
					}
				}
				logger.info("Total non-primary vvd containers : "+nonPrimaryCntrList.size());
				NewVesselDaoGum.insertRDSData(nonPrimaryCntrList);
				logger.info("Non-primary containers are stored successfully.");
				isFileProcessed = true;
			}
		}
		else
		{
			nvLogger.addFileError(ftpFileName, "Error: No data. Check file for errors.");
			return;
		}
	}
	//


	private ArrayList<TosGumRdsDataMt> buildCntrList(String vvd, ArrayList<RDSFields> rdsData)
	{
		ArrayList<TosGumRdsDataMt> cntrList = new ArrayList<TosGumRdsDataMt>();
		if(vvd!=null)
		{
			Iterator<RDSFields> itrRds = rdsData.iterator();
			for(; itrRds.hasNext() ; )
			{
				RDSFields rdsField = itrRds.next();
				String rdsvvd = rdsField.getVes()+rdsField.getVoy()+rdsField.getLeg();
				if(vvd.equalsIgnoreCase(rdsvvd))
				{
					TosGumRdsDataMt rdsD = populateTosRdsDataMt(rdsField, vvd);
					cntrList.add(rdsD);
					itrRds.remove();
				}
			}
		}
		//
		return cntrList;
	}

	public void processLine(String line,int lineNumber)
	{
		if(line==null || line.length()<=0)
			return;
		int beginIndex = 0, endIndex = 0;
		RDSFields brdf = new RDSFields();
		for(int i=0; i<xmlFields.size(); i++)
		{
			XmlFields fld = (XmlFields)xmlFields.get(i);
			endIndex = new Integer(fld.getFieldLength());
			String temp_str = line.substring(beginIndex, beginIndex + endIndex).trim();
			switch(EnumRDS.valueOf(fld.getFieldName()))
			{
			case ves:
				brdf.setVes(temp_str); break;
			case voy:
				brdf.setVoy(temp_str); break;
			case leg:
				brdf.setLeg(temp_str); break;
			case containerNumber:
				brdf.setContainerNumber(temp_str); break;
			case shipmentNo:
				brdf.setShipmentNo(temp_str); break;
			case dataSource:
				brdf.setDataSource(temp_str); break;
			case routeToPort:
				brdf.setRouteToPort(temp_str); break;
			case loadPort:
				brdf.setLoadPort(temp_str); break;
			case dischPort:
				brdf.setDischPort(temp_str); break;
			case placeDelCode:
				brdf.setPlaceDelCode(temp_str); break;
			case temp:
				brdf.setTemp(temp_str); break;
			case loadService:
				brdf.setLoadService(temp_str); break;
			case dischService:
				brdf.setDischService(temp_str); break;
			case hazf:
				brdf.setHazf(temp_str); break;
			case filler1:
				brdf.setFiller1(temp_str); break;
			case cntrOversize:
				brdf.setCntrOversize(temp_str); break;
			case primaryCarrier:
				brdf.setPrimaryCarrier(temp_str); break;
			case initialVes:
				brdf.setInitialVes(temp_str); break;
			case initialVoy:
				brdf.setInitialVoy(temp_str); break;
			case initialLeg:
				brdf.setInitialLeg(temp_str); break;
			case inbond:
				brdf.setInbond(temp_str); break;
			case notify:
				brdf.setNotify(temp_str); break;
			case creditStatus:
				brdf.setCreditStatus(temp_str); break;
			case transit:
				brdf.setTransit(temp_str); break;
			case trade:
				brdf.setTrade(temp_str); break;
			case loadType:
				brdf.setLoadType(temp_str); break;
			case shipperArolIden:
				brdf.setShipperArolIden(temp_str); break;
			case shipperOrgName:
				brdf.setShipperOrgName(temp_str); break;
			case shipperOrgNameQual:
				brdf.setShipperOrgNameQual(temp_str); break;
			case consigneeArolIden:
				brdf.setConsigneeArolIden(temp_str); break;
			case consigneeOrgName:
				brdf.setConsigneeOrgName(temp_str); break;
			case consigneeCo:
				brdf.setConsigneeCo(temp_str); break;
			case consigneeOrgNameQual:
				brdf.setConsigneeOrgNameQual(temp_str); break;
			case consigneeAddrLine1:
				brdf.setConsigneeAddrLine1(temp_str); break;
			case consigneeSuite:
				brdf.setConsigneeSuite(temp_str); break;
			case consigneeCity:
				brdf.setConsigneeCity(temp_str); break;
			case consigneeState:
				brdf.setConsigneeState(temp_str); break;
			case consigneeCountry:
				brdf.setConsigneeCountry(temp_str); break;
			case consigneeZipCode:
				brdf.setConsigneeZipCode(temp_str); break;
			case consigneeDepartment:
				brdf.setConsigneeDepartment(temp_str); break;
			case consigneeTitle:
				brdf.setConsigneeTitle(temp_str); break;
			case consigneeLastName:
				brdf.setConsigneeLastName(temp_str); break;
			case consigneeFirstName:
				brdf.setConsigneeFirstName(temp_str); break;
			case cmdyDesc:
				brdf.setCmdyDesc(temp_str); break;
			case cmdySrvrptDesc:
				brdf.setCmdySrvrptDesc(temp_str); break;
			case cmdyAg:
				brdf.setCmdyAg(temp_str); break;
			case cmdyHhg:
				brdf.setCmdyHhg(temp_str); break;
			case cell:
				brdf.setCell(temp_str); break;
			case grossWt:
				brdf.setGrossWt(temp_str); break;
			case highWtFlag:
				brdf.setHighWtFlag(temp_str); break;
			case sealNo:
				brdf.setSealNo(temp_str); break;
			case ownerLessor:
				brdf.setOwnerLessor(temp_str); break;
			case damageStatus:
				brdf.setDamageStatus(temp_str); break;
			case emptyFull:
				brdf.setEmptyFull(temp_str); break;
			case ecosRetPort:
				brdf.setEcosRetPort(temp_str); break;
			case checkDigit:
				brdf.setCheckDigit(temp_str); break;
			case specMsg1:
				brdf.setSpecMsg1(temp_str); break;
			case specMsg2:
				brdf.setSpecMsg2(temp_str); break;
			case specMsg3:
				brdf.setSpecMsg3(temp_str); break;
			case cnsgAreaCode:
				brdf.setCnsgAreaCode(temp_str); break;
			case cnsgPhone1:
				brdf.setCnsgPhone1(temp_str); break;
			case cnsgPhone2:
				brdf.setCnsgPhone2(temp_str); break;
			case poNumber:
				brdf.setPoNumber(temp_str); break;
			case filler2:
				brdf.setFiller2(temp_str); break;
			case shmtDestCityCode:
				brdf.setShmtDestCityCode(temp_str); break;
			case vesselType:
				brdf.setVesselType(temp_str); break;
			case consigneeFaxNotifyOrgnName:
				brdf.setConsigneeFaxNotifyOrgnName(temp_str); break;
			case consigneeFaxNotifyAttnParty:
				brdf.setConsigneeFaxNotifyAttnParty(temp_str); break;
			case consigneeFaxNotifyCtryCode:
				brdf.setConsigneeFaxNotifyCtryCode(temp_str); break;
			case consigneeFaxNotifyAreaCode:
				brdf.setConsigneeFaxNotifyAreaCode(temp_str); break;
			case consigneeFaxNotifyExch:
				brdf.setConsigneeFaxNotifyExch(temp_str); break;
			case consigneeFaxNotifyStation:
				brdf.setConsigneeFaxNotifyStation(temp_str); break;
			case consigneeFaxNotifyExt:
				brdf.setConsigneeFaxNotifyExt(temp_str); break;
			case consignee2NDFaxCtryCode:
				brdf.setConsignee2NDFaxCtryCode(temp_str); break;
			case consignee2NDFaxAreaCode:
				brdf.setConsignee2NDFaxAreaCode(temp_str); break;
			case consignee2NDFaxExch:
				brdf.setConsignee2NDFaxExch(temp_str); break;
			case consignee2ndFaxStation:
				brdf.setConsignee2ndFaxStation(temp_str); break;
			case consignee2NDFaxExt:
				brdf.setConsignee2NDFaxExt(temp_str); break;
			case truckerFaxNotifyOrgnName:
				brdf.setTruckerFaxNotifyOrgnName(temp_str); break;
			case truckerFaxNotifyAttnParty:
				brdf.setTruckerFaxNotifyAttnParty(temp_str); break;
			case truckerFaxNotifyCtryCode:
				brdf.setTruckerFaxNotifyCtryCode(temp_str); break;
			case truckerFaxNotifyAreaCode:
				brdf.setTruckerFaxNotifyAreaCode(temp_str); break;
			case truckerFaxNotifyExch:
				brdf.setTruckerFaxNotifyExch(temp_str); break;
			case truckerFaxNotifyStation:
				brdf.setTruckerFaxNotifyStation(temp_str); break;
			case truckerFaxNotifyExt:
				brdf.setTruckerFaxNotifyExt(temp_str); break;
			case trucker2NDFaxCtryCode:
				brdf.setTrucker2NDFaxCtryCode(temp_str); break;
			case trucker2NDFaxAreaCode:
				brdf.setTrucker2NDFaxAreaCode(temp_str); break;
			case trucker2NDFaxExch:
				brdf.setTrucker2NDFaxExch(temp_str); break;
			case trucker2ndFaxStation:
				brdf.setTrucker2ndFaxStation(temp_str); break;
			case trucker2NDFaxExt:
				brdf.setTrucker2NDFaxExt(temp_str); break;
			case headQuartersFaxNotifyOrgnName:
				brdf.setHeadQuartersFaxNotifyOrgnName(temp_str); break;
			case headQuartersFaxNotifyAttnParty:
				brdf.setHeadQuartersFaxNotifyAttnParty(temp_str); break;
			case headQuartersFaxNotifyCtryCode:
				brdf.setHeadQuartersFaxNotifyCtryCode(temp_str); break;
			case headQuartersFaxNotifyAreaCode:
				brdf.setHeadQuartersFaxNotifyAreaCode(temp_str); break;
			case headQuartersFaxNotifyExch:
				brdf.setHeadQuartersFaxNotifyExch(temp_str); break;
			case headQuartersFaxNotifyStation:
				brdf.setHeadQuartersFaxNotifyStation(temp_str); break;
			case headQuartersFaxNotifyExt:
				brdf.setHeadQuartersFaxNotifyExt(temp_str); break;
			case headQuarters2NDFaxCtryCode:
				brdf.setHeadQuarters2NDFaxCtryCode(temp_str); break;
			case headQuarters2NDFaxAreaCode:
				brdf.setHeadQuarters2NDFaxAreaCode(temp_str); break;
			case headQuarters2NDFaxExch:
				brdf.setHeadQuarters2NDFaxExch(temp_str); break;
			case headQuarters2ndFaxStation:
				brdf.setHeadQuarters2ndFaxStation(temp_str); break;
			case headQuarters2NDFaxExt:
				brdf.setHeadQuarters2NDFaxExt(temp_str); break;
			case notifyFaxNotifyOrgnName:
				brdf.setNotifyFaxNotifyOrgnName(temp_str); break;
			case notifyFaxNotifyAttnParty:
				brdf.setNotifyFaxNotifyAttnParty(temp_str); break;
			case notifyFaxNotifyCtryCode:
				brdf.setNotifyFaxNotifyCtryCode(temp_str); break;
			case notifyFaxNotifyAreaCode:
				brdf.setNotifyFaxNotifyAreaCode(temp_str); break;
			case notifyFaxNotifyExch:
				brdf.setNotifyFaxNotifyExch(temp_str); break;
			case notifyFaxNotifyStation:
				brdf.setNotifyFaxNotifyStation(temp_str); break;
			case notifyFaxNotifyExt:
				brdf.setNotifyFaxNotifyExt(temp_str); break;
			case notify2NDFaxCtryCode:
				brdf.setNotify2NDFaxCtryCode(temp_str); break;
			case notify2NDFaxAreaCode:
				brdf.setNotify2NDFaxAreaCode(temp_str); break;
			case notify2NDFaxExch:
				brdf.setNotify2NDFaxExch(temp_str); break;
			case notify2ndFaxStation:
				brdf.setNotify2ndFaxStation(temp_str); break;
			case notify2NDFaxExt:
				brdf.setNotify2NDFaxExt(temp_str); break;
			case carrierCode:
				brdf.setCarrierCode(temp_str); break;
			case shipperRef1Text:
				brdf.setShipperRef1Text(temp_str); break;
			case milContNbr:
				brdf.setMilContNbr(temp_str); break;
			case milConsignee:
				brdf.setMilConsignee(temp_str); break;
			case milConsigner:
				brdf.setMilConsigner(temp_str); break;
			case govtBlNbr:
				brdf.setGovtBlNbr(temp_str); break;
			case milPortCall:
				brdf.setMilPortCall(temp_str); break;
			case milTcn:
				brdf.setMilTcn(temp_str); break;
			case milVNbr:
				brdf.setMilVNbr(temp_str); break;
			case milWeight:
				brdf.setMilWeight(temp_str); break;
			case milCubeFt:
				brdf.setMilCubeFt(temp_str); break;
			case milPackNbr:
				brdf.setMilPackNbr(temp_str); break;
			case detentoinFreeDays:
				brdf.setDetentoinFreeDays(temp_str); break;
			case storageFreeDays:
				brdf.setStorageFreeDays(temp_str); break;
			case rdd:
				brdf.setRdd(temp_str); break;
			case bookingCsrId:
				brdf.setBookingCsrId(temp_str); break;
			case npArcrCarrCode:
				brdf.setNpArcrCarrCode(temp_str); break;
			case npOrgnName:
				brdf.setNpOrgnName(temp_str); break;
			case dPort:
				brdf.setdPort(temp_str); break;
			case csrShipperOrgnId:
				brdf.setCsrShipperOrgnId(temp_str); break;
			case csrConsigneeOrgnId:
				brdf.setCsrConsigneeOrgnId(temp_str); break;
			case csrSpecialSvcArray1:
				brdf.setCsrSpecialSvcArray1(temp_str); break;
			case csrSpecialSvcArray2:
				brdf.setCsrSpecialSvcArray2(temp_str); break;
			case csrSpecialSvcArray3:
				brdf.setCsrSpecialSvcArray3(temp_str); break;
			case csrSpecialSvcArray4:
				brdf.setCsrSpecialSvcArray4(temp_str); break;
			case csrSpecialSvcArray5:
				brdf.setCsrSpecialSvcArray5(temp_str); break;
			case csrClxRoute:
				brdf.setCsrClxRoute(temp_str); break;
			case csrClxHonCall:
				brdf.setCsrClxHonCall(temp_str); break;
			case csrClxGumCall:
				brdf.setCsrClxGumCall(temp_str); break;
			case filler3:
				brdf.setFiller3(temp_str); break;
			case delimeter:
				brdf.setDelimeter(temp_str); break;
			}
			beginIndex = beginIndex + endIndex;
		}
		rdsData.add(brdf);
		String cVesVoy = "";
		if(brdf.getVes()!= null && brdf.getVoy()!=null)
		{
			cVesVoy = brdf.getVes()+brdf.getVoy()+brdf.getLeg();
			if(!vesselVoyageList.contains(cVesVoy))
			{
				vesselVoyageList.add(cVesVoy);
			}
		}

	}

	private TosGumRdsDataMt populateTosRdsDataMt(RDSFields brdf, String tobeRDSVesvoy)
	{
		TosGumRdsDataMt rds = new TosGumRdsDataMt();
		rds.setVes(tobeRDSVesvoy.substring(0,3));
		rds.setVoy(tobeRDSVesvoy.substring(3,6));
		rds.setLeg(tobeRDSVesvoy.substring(6,7));
		rds.setCtrno(brdf.getContainerNumber());
		if(shipperRefMap.get(brdf.getContainerNumber())==null)
			shipperRefMap.put(brdf.getContainerNumber(), brdf.getShipperRef1Text());
		//logger.info(brdf.getContainerNumber() + "\t" + brdf.getShipperRef1Text());
		rds.setShipno(brdf.getShipmentNo());
		rds.setDatasource(brdf.getDataSource());
		rds.setLoadPort(brdf.getLoadPort());
		rds.setDischargePort(brdf.getDischPort());
		rds.setDestinationPort(brdf.getdPort());
		rds.setTemp(brdf.getTemp());
		rds.setLoadDischServ(brdf.getDischService());
		rds.setHazf(brdf.getHazf());
		rds.setInbound(brdf.getInbond());
		rds.setNotify(brdf.getNotify());
		rds.setCreditStatus(brdf.getCreditStatus());
		rds.setTransit(brdf.getTransit());
		rds.setShipperArol(brdf.getShipperArolIden());
		if (brdf.getShipperOrgName() != null)
			rds.setShipperName(brdf.getShipperOrgName().length() > 35 ? brdf
					.getShipperOrgName().substring(0, 35) : brdf
					.getShipperOrgName());
		if (brdf.getShipperOrgNameQual() != null)
			rds.setShipperQualifier(brdf.getShipperOrgNameQual().length() > 32 ? brdf
					.getShipperOrgNameQual().substring(0, 32) : brdf
					.getShipperOrgNameQual());
		rds.setConsigneeArol(brdf.getConsigneeArolIden());
		if (brdf.getConsigneeOrgName() != null)
			rds.setConsigneeName(brdf.getConsigneeOrgName().length() > 35 ? brdf
					.getConsigneeOrgName().substring(0, 35) : brdf
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
		rds.setSecondVesvoy(brdf.getInitialVes()+brdf.getInitialVoy());
		rds.setCreateUser("gumnewves");
		rds.setCreateDate(triggerDate);
		rds.setLastUpdateUser("gumnewves");
		rds.setLastUpdateDate(triggerDate);
		return rds;
	}

	private TosGumRdsDataFinalMt populateTosRdsDataFinalMt(TosGumRdsDataMt rdsData, TosGumStPlanCntrMt ctrData, String type)
	{
		TosGumRdsDataFinalMt rdsF = new TosGumRdsDataFinalMt();
		// From RDS Data
		rdsF.setContainerNumber(rdsData.getCtrno());
		rdsF.setVesvoy(rdsData.getVes() + rdsData.getVoy());
		rdsF.setMisc1(rdsData.getSecondVesvoy());
		String sealNumber = ctrData.getSealNumber();
		if(sealNumber==null)
			sealNumber = rdsData.getSealNumber();
		rdsF.setSealNumber(sealNumber);
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
			bkgNbr = shipNoList.get(0);
		}
		if (multipleShipment && cmdyhhg.equals("Y") && transit.equals(""))
		{
			rdsF.setDsc("A");
		}
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
		String shipperRef = shipperRefMap.get(rdsData.getCtrno());
		shipperRef = shipperRef==null?"":shipperRef+" ";
		if (consigneeCo != null && !consigneeCo.equals(""))
		{
			cgn = shipperRef + "C/O " + consigneeCo + "-" + cgn.trim();
		}
		else
		{
			cgn = shipperRef + cgn.trim();
		}
		String commodity = null;
		if(rdsData.getCmdyAg()!=null && rdsData.getCmdyAg().equals("C"))
			commodity = "PRODUCE";
		else
			commodity = rdsData.getCmdyDesc();
		commodity = commodity == null ? "" : commodity;
		cgn = cgn.trim();
		if(!cgn.endsWith("-"))
			cgn = cgn + " ";
		cgn = cgn +  commodity.trim();
		if(multipleShipment)
		{
			if(!cgn.contains("MNFST INFO"))
				cgn = shipNo.trim() + " " + cgn.trim();
			else
				cgn = cgn.trim() + " " + shipNo.trim();
		}
		cgn = cgn.length() >= 65 ? cgn.substring(0, 65) : cgn;
		rdsF.setCargoNotes(cgn.trim());
		//logger.info("CNTRNO: " + rdsF.getContainerNumber()
		//		+ "\tCGNOTES: " + rdsF.getCargoNotes());
		// rdsF.setCell(rdsData.getCell());
		// rdsF.setCweight(rdsData.getGrossWt());
		/*if (rdsData.getConsigneeQualifier() != null
				&& !rdsData.getConsigneeQualifier().trim().equals(""))
		{
			rdsF.setCneeCode(rdsData.getConsigneeArol());
		} else
			rdsF.setCneeCode(rdsData.getConsigneeOrgnId());

		if (rdsData.getMilTcn() != null && rdsData.getMilCnee() != null
				&& !rdsData.getMilTcn().trim().equals("")
				&& !rdsData.getMilCnee().trim().equals(""))
		{
			rdsF.setCneePo(rdsData.getMilCnee() + " " + rdsData.getMilTcn());
		} else if (rdsData.getMilTcn() != null
				&& !rdsData.getMilTcn().trim().equals(""))
		{
			rdsF.setCneePo("       " + rdsData.getMilTcn());
		} else if (rdsData.getMilCnee() != null
				&& !rdsData.getMilCnee().trim().equals(""))
		{
			rdsF.setCneePo(rdsData.getMilCnee());
		} else
		{
			rdsF.setCneePo(rdsData.getPoNumber());
		}*/
		rdsF.setCneePo(rdsData.getPoNumber());

		rdsF.setDischargePort(rdsData.getDischargePort());
		logger.info("Disch Port from RDS : " + rdsData.getDischargePort());
		try {
			TosDestPodData refData = null;
			refData = (TosDestPodData)RefDataLookup.queryById("com.matson.cas.refdata.mapping.TosDestPodData",rdsData.getDestinationPort());
			if (refData !=null && refData.getPod1() !=null) {
				rdsF.setDischargePort(refData.getPod1());
				logger.info("Disch Port from DEST_POD_DATA : " + refData.getPod1());
			} 
		}catch(Exception ex) {
			logger.info("Exception getting POD " + ex);
		}
		commodity = commodity.length() >= 8 ? commodity.substring(0, 8)
				: commodity;
		rdsF.setCommodity(commodity);
		//Fix for Gear container commodity
		if(ctrData.getCommodity()!=null&&ctrData.getCommodity().equals("GEAR")&&(rdsData.getCmdyDesc()==null||rdsData.getCmdyDesc().equals("")))
			rdsF.setCommodity(ctrData.getCommodity());
		rdsF.setLoadPort(ctrData.getLoadPort());
		if (rdsData.getCheckDigit() != null && rdsData.getCheckDigit().trim().length()>0)
			rdsF.setCheckDigit(rdsData.getCheckDigit());
		else
			rdsF.setCheckDigit(ctrData.getCheckDigit());
		rdsF.setCnsgPhone(rdsData.getCnsgPhone());
		rdsF.setCsrId(rdsData.getCsrId());
		rdsF.setTrade(rdsData.getTrade());
		rdsF.setConsigneeOrgnId(rdsData.getConsigneeOrgnId());
		rdsF.setConsigneeArol(rdsData.getConsigneeArol());
		// Consignee Qualifier
		String consigneeQual = rdsData.getConsigneeQualifier();
		consigneeQual = consigneeQual == null ? "" : consigneeQual;
		consigneeQual = consigneeQual.length()>32?consigneeQual.substring(0, 32):consigneeQual;
		rdsF.setConsigneeQualifier(consigneeQual);
		// Consignee
		String consignee = rdsData.getConsigneeName();
		consignee = consignee==null?"":consignee;
		consignee = consignee + " " + consigneeQual;
		consignee = consignee.length()>35?consignee.substring(0, 35):consignee;
		rdsF.setConsignee(consignee);
		// ConsigneeName
		String consigneeName = rdsData.getConsigneeName();
		consigneeName = consigneeName == null ? "" : consigneeName;
		consigneeName = consigneeName.length()>35?consigneeName.substring(0, 35):consigneeName;
		rdsF.setConsigneeName(consigneeName);
		
		rdsF.setCneeCode(rdsData.getConsigneeArol());
		//rdsF.setShipperArol(rdsData.getShipperArol());
		//rdsF.setShipperOrgnId(rdsData.getShipperOrgnId());
		String shipperName = rdsData.getShipperName();
		shipperName = shipperName == null ? "" : shipperName;
		shipperName = shipperName.length()>35?shipperName.substring(0, 35):shipperName;
		rdsF.setShipperName(shipperName);
		rdsF.setShipper(shipperName);
		//String shipperQual = rdsData.getShipperQualifier();
		//shipperQual = shipperQual == null ? "" : shipperQual;
		//rdsF.setShipperQualifier(shipperQual);
		///
		if(bkgNbr!=null&&bkgNbr.length()>0)
			rdsF.setBookingNumber(bkgNbr);
		else
			rdsF.setBookingNumber(ctrData.getBookingNumber());
		if(consignee.toUpperCase().contains("HOME DEPOT"))
		{
			if(rdsF.getBookingNumber()!=null&&rdsF.getBookingNumber().length()>=7)
			{
				rdsF.setBookingNumber(rdsF.getBookingNumber().substring(0, 7)+"000");
			}
		}
		String creditStatus = rdsData.getCreditStatus();
		creditStatus = creditStatus==null?"":creditStatus;
		if(creditStatus.equals(""))
		{
			if(rdsData.getDatasource()!=null && rdsData.getDatasource().equals("E"))
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
		String srstatus = GumCommonBusinessProcessor.srstatusTransformation(rdsData);
		srstatus = srstatus.length()>10?srstatus.substring(0, 10):srstatus;
		rdsF.setSrstatus(srstatus);
		rdsF.setMilTcn(rdsData.getMilTcn());
		rdsF.setDeliveryDepot(rdsData.getDeliveryDepot());
		// From Stow Data - OCR
		rdsF.setSrv("MAT");// ctrData.getSrv());
		rdsF.setTruck(ctrData.getTruck());
		rdsF.setPmd(ctrData.getPmd());
		rdsF.setOrientation(rdsData.getEmptyFull());
		String emptyFull = rdsData.getEmptyFull();
		emptyFull = emptyFull == null ? "" : emptyFull;
		if (emptyFull.equals("F"))
			rdsF.setDir("IN");
		else if (emptyFull.equals("E"))
			rdsF.setDir("MTY");
		//logger.info("Dir value for container#:"+rdsData.getCtrno() +" is: "+ctrData.getDir());
		if ("OUT".equalsIgnoreCase(ctrData.getDir())) {
			rdsF.setDir("IN");
		}
		rdsF.setComments(ctrData.getComments());
		String ds = rdsData.getLoadDischServ();
		ds = ds==null?"":ds;
		if(ds.equals("AU"))
			ds = "AUT";
		else if(ds.equals("PJT") || ds.equals("P") || ds.equals("IM") || ds.equals("IMS") || ds.equals("IMR"))
			ds = "CY";
		rdsF.setDs(ds);
		// rdsF.setDsc(ctrData.getDsc());
		if(rdsF.getDs()==null || rdsF.getDs().equals(""))
		{
			rdsF.setDs(ctrData.getDs());
		}
		rdsF.setDss(ctrData.getDss());
		rdsF.setTypeCode(ctrData.getTypeCode());
		/*if(ctrData.getOwner()!=null)
            rdsF.setOwner(ctrData.getOwner());
        else if (rdsData.getOwnerLessor()!=null) */
            rdsF.setOwner(rdsData.getOwnerLessor());

		/*//Booking number
		String tempDs = rdsF.getDs();
		tempDs = tempDs==null?"":tempDs;
		String tempDsc = rdsF.getDsc();
		tempDsc = tempDsc==null?"":tempDsc;
		String rdsbkgNbr = bkgNbr;
		rdsbkgNbr = rdsbkgNbr==null?"":rdsbkgNbr;
		String stowbkgNbr = ctrData.getBookingNumber();
		stowbkgNbr = stowbkgNbr==null?"":stowbkgNbr;
		if(!tempDsc.equals("P")&&!tempDsc.equals("M")&&(tempDs.equals("CY")||tempDs.equals("CON")))
		{
			rdsF.setBookingNumber(rdsbkgNbr);
		}
		else
		{
			rdsF.setBookingNumber(stowbkgNbr);
		}*/
		//
		rdsF.setPlanDisp(ctrData.getPlanDisp());
		rdsF.setLocationCategory(ctrData.getLocationCategory());
		rdsF.setLocationRowDeck(ctrData.getLocationRowDeck());
		rdsF.setLocationRunStackSectn(ctrData.getLocationRunStackSectn());
		rdsF.setLocationTierStall(ctrData.getLocationTierStall());
		rdsF.setLocationStallConfig(ctrData.getLocationStallConfig());
		//rdsF.setOrientation(ctrData.getOrientation());
		rdsF.setStowRestrictionCode(ctrData.getStowRestrictionCode());
		String trade = rdsData.getTrade();
		/*if(ctrData.getHazardousOpenCloseFlag()!=null)
		{
			trade = ctrData.getHazardousOpenCloseFlag();
		}
		else
		{
			trade = CommonBusinessProcessor.getTradeforPort(ctrData.getDport());
		}*/
		rdsF.setHazardousOpenCloseFlag(trade);
		rdsF.setTrade(trade);
		BigDecimal tareWeight = ctrData.getTareWeight();
		tareWeight = CommonBusinessProcessor.fixTareWeight(tareWeight, ctrData.getTypeCode());
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
		/*if (ctrData.getDport() != null)
		    rdsF.setDport(ctrData.getDport());
		else
		    rdsF.setDport(rdsData.getDestinationPort());*/
		//Raghu
		if (rdsData.getDestinationPort() != null) {
			rdsF.setDport(rdsData.getDestinationPort());
		} else {
			rdsF.setDport(ctrData.getDport());
		}
		if (rdsF.getDport() != null && rdsF.getDport().equals("KAU"))
			rdsF.setDport("MOL");
		else if (rdsF.getDport() != null && rdsF.getDport().equals("LAN"))
			rdsF.setDport("LNI");
		/*if (rdsData.getDischargePort() != null)
			rdsF.setDischargePort(rdsData.getDischargePort());
		else
		if (!"GCS".equalsIgnoreCase(service))
			rdsF.setDischargePort("HON");
		else
			rdsF.setDischargePort(ctrData.getDischargePort());*/
		String temperature = "";
		temperature = ctrData.getTemp();
		temperature = temperature==null?"":temperature;
		if(temperature.equals("") || temperature.equalsIgnoreCase("AMB"))
		{
			temperature = rdsData.getTemp();
			temperature = temperature==null?"":temperature;
		}
		rdsF.setTemp(temperature);
		rdsF.setTempMeasurementUnit(ctrData.getTempMeasurementUnit());
		rdsF.setDamageCode(ctrData.getDamageCode());
		if(ctrData.getCweight()!=null)
			rdsF.setCweight(ctrData.getCweight());
		else
			rdsF.setCweight(rdsData.getGrossWt());
		String cell = ctrData.getCell();
		if(cell!=null && cell.length()>0)
			rdsF.setCell(cell);
		else
			rdsF.setCell(rdsData.getCell());
		rdsF.setChassisNumber(ctrData.getChassisNumber());
		if(rdsF.getDs()!=null && !rdsF.getDs().equals("CFS") && !rdsF.getDs().equals("AUT"))
			rdsF.setHazf(ctrData.getHazf());
		//
		rdsF.setCreateUser("gumnewves");
		rdsF.setCreateDate(triggerDate);
		rdsF.setLastUpdateUser("gumnewves");
		rdsF.setLastUpdateDate(triggerDate);
		rdsF.setTriggerDate(triggerDate);
		return rdsF;
	}

	private TosGumRdsDataFinalMt populateTosRdsDataFinalMtExtra(TosGumStPlanCntrMt ctrData, String type)
	{
		TosGumRdsDataFinalMt rdsFext = new TosGumRdsDataFinalMt();
		//
		rdsFext.setContainerNumber(ctrData.getContainerNumber());
		rdsFext.setCheckDigit(ctrData.getCheckDigit());
		String shipper = ctrData.getShipper();
		if(shipper!=null)
		{
			shipper = shipper.length()>35?shipper.substring(0, 35):shipper;
		}
		rdsFext.setShipper(shipper);
		rdsFext.setShipperName(shipper);
		String consignee = ctrData.getConsignee();
		if(consignee!=null)
		{
			consignee = consignee.length()>35?consignee.substring(0, 35):consignee;
		}
		rdsFext.setConsignee(consignee);
		rdsFext.setConsigneeName(consignee);
		/*if (ctrData.getShipper() != null)
		{
			rdsFext.setShipper(ctrData.getShipper().length() >= 32 ? ctrData
					.getShipper().substring(0, 32) : ctrData.getShipper());
			rdsFext.setShipperName(ctrData.getShipper().length() >= 32 ? ctrData
					.getShipper().substring(0, 32) : ctrData.getShipper());
			try {
				String shipperId = lookUp.getShipper(null, ctrData.getShipper());
				if(shipperId!=null)
				{
					shipperId = shipperId.length()>10?shipperId.substring(0, 10):shipperId;
				}
				rdsFext.setShipperOrgnId(shipperId);
				logger.info("Shipper Id from TOS : " + shipperId + " for " + ctrData.getShipper());
			} catch(Exception e)
			{
				logger.error("Unable to get shipper id from TOS for " + ctrData.getShipper());
			}
		}
		if (ctrData.getConsignee() != null)
		{
			rdsFext.setConsignee(ctrData.getConsignee().length() >= 32 ? ctrData
					.getConsignee().substring(0, 32) : ctrData.getConsignee());
			rdsFext.setConsigneeName(ctrData.getConsignee().length() >= 32 ? ctrData
					.getConsignee().substring(0, 32) : ctrData.getConsignee());
			try {
				String consigneeId = lookUp.getConsignee(null, ctrData.getConsignee());
				if(consigneeId!=null)
				{
					consigneeId = consigneeId.length()>10?consigneeId.substring(0, 10):consigneeId;
				}
				rdsFext.setConsigneeOrgnId(consigneeId);
				rdsFext.setCneeCode(consigneeId);
				logger.info("Consginee Id from TOS : " + consigneeId + " for " + ctrData.getConsignee());
			} catch(Exception e)
			{
				logger.error("Unable to get consignee id from TOS for " + ctrData.getConsignee());
			}
		}*/
		rdsFext.setVesvoy(ctrData.getVesvoy());
		rdsFext.setSealNumber(ctrData.getSealNumber());
		rdsFext.setCell(ctrData.getCell());
		rdsFext.setCweight(ctrData.getCweight());
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
		// rdsF.setDsc(ctrData.getDsc());
		rdsFext.setDss(ctrData.getDss());
		rdsFext.setTypeCode(ctrData.getTypeCode());
		rdsFext.setOwner(ctrData.getOwner());
		rdsFext.setBookingNumber(ctrData.getBookingNumber());
		if(consignee!=null && consignee.toUpperCase().contains("HOME DEPOT"))
		{
			if(rdsFext.getBookingNumber()!=null&&rdsFext.getBookingNumber().length()>=7)
			{
				rdsFext.setBookingNumber(rdsFext.getBookingNumber().substring(0, 7)+"000");
			}
		}
		rdsFext.setCommodity(ctrData.getCommodity());
		rdsFext.setPlanDisp(ctrData.getPlanDisp());
		rdsFext.setLocationCategory(ctrData.getLocationCategory());
		rdsFext.setLocationRowDeck(ctrData.getLocationRowDeck());
		rdsFext.setLocationRunStackSectn(ctrData.getLocationRunStackSectn());
		rdsFext.setLocationTierStall(ctrData.getLocationTierStall());
		rdsFext.setLocationStallConfig(ctrData.getLocationStallConfig());
		rdsFext.setOrientation(ctrData.getOrientation());
		rdsFext.setStowRestrictionCode(ctrData.getStowRestrictionCode());

		//String trade = ctrData.getHazardousOpenCloseFlag();
		/*
		String dPort = ctrData.getDport();
		String loadPort = ctrData.getLoadPort();
		String loadTrade = "";
		if(trade==null || trade.equals(""))
		{
			trade = CommonBusinessProcessor.getTradeforPort(dPort);
			if (trade == null) {
				trade = "H";
			}
			loadTrade = CommonBusinessProcessor.getTradeforPort(loadPort);
			if ("G".equals(loadTrade) || "F".equals(loadTrade)) {
				trade = loadTrade;
			}
		}
		rdsFext.setHazardousOpenCloseFlag(trade);
		rdsFext.setTrade(trade);
		rdsFext.setHsf6("E");
		if(trade!=null)
			if(trade.equalsIgnoreCase("G") || trade.equalsIgnoreCase("F"))
				rdsFext.setHsf5("Y");
		logger.info("Extra Container:"+rdsFext.getContainerNumber()+" Trade:"+trade+" Hsf6:"+rdsFext.getHsf6()+" Hsf5:"+rdsFext.getHsf5());*/
		BigDecimal tareWeight = ctrData.getTareWeight();
		tareWeight = CommonBusinessProcessor.fixTareWeight(tareWeight, ctrData.getTypeCode());
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
		rdsFext.setDischargePort(ctrData.getDischargePort());
		logger.info("Disch Port from Stowplan : " + ctrData.getDischargePort());
		try {
			TosDestPodData refData = null;
			refData = (TosDestPodData)RefDataLookup.queryById("com.matson.cas.refdata.mapping.TosDestPodData",ctrData.getDport());
			if (refData !=null && refData.getPod1() !=null) {
				rdsFext.setDischargePort(refData.getPod1());
				logger.info("Disch Port from DEST_POD_DATA : " + refData.getPod1());
			} 
		}catch(Exception ex) {
			logger.info("Exception getting POD " + ex);
		}
		rdsFext.setLoadPort(ctrData.getLoadPort());
		rdsFext.setTemp(ctrData.getTemp());
		rdsFext.setTempMeasurementUnit(ctrData.getTempMeasurementUnit());
		rdsFext.setDamageCode(ctrData.getDamageCode());
		rdsFext.setChassisNumber(ctrData.getChassisNumber());
		if(rdsFext.getDs()!=null && !rdsFext.getDs().equals("CFS") && !rdsFext.getDs().equals("AUT"))
			rdsFext.setHazf(ctrData.getHazf());
		//
		rdsFext.setCreateUser("gumnewves");
		rdsFext.setCreateDate(triggerDate);
		rdsFext.setLastUpdateUser("gumnewves");
		rdsFext.setLastUpdateDate(triggerDate);
		rdsFext.setTriggerDate(triggerDate);
		return rdsFext;
	}

	private boolean transformRdsDataToRdsFinal(String vesvoy, String type, ArrayList<TosGumRdsDataMt> rdsData)
	{
		logger.debug("Transforming RDS data into RDS Final table");
		ArrayList<TosGumRdsDataFinalMt> rdsDataFinalList;
		ArrayList<TosGumRdsDataFinalMt> rdsDataFinalListExt;
		rdsDataFinalList = new ArrayList<TosGumRdsDataFinalMt>();
		rdsDataFinalListExt = new ArrayList<TosGumRdsDataFinalMt>();
		if (type.equals(PRIMARY))
		{
			//ArrayList<TosGumRdsDataMt> rdsData = NewVesselDao.getRdsDataForVesvoy(vesvoy, triggerDate);
			logger.debug("RDS Data size before combine - " + rdsData.size());
			rdsData = GumCommonBusinessProcessor.combineShipmentNoCreditStatus(rdsData);
			rdsData =  GumCommonBusinessProcessor.eliminateDuplicatesInRDS(rdsData);
			logger.debug("RDS Data size after combine - " + rdsData.size());
			this.ocrDataList = NewVesselDaoGum.getGumOCRDataForVesvoy(vesvoy, null);
			if(this.ocrDataList==null || this.ocrDataList.size()==0)
			{
				logger.error("Error: No stowplan data found for the vesvoy "+vesvoy);
				nvLogger.addError(vesvoy, "", "Error: Unable to get stowplan data for "+vesvoy);
				return false;
			}
			for (int i = 0; i < rdsData.size(); i++)
			{
				TosGumRdsDataMt rdsD = rdsData.get(i);
				String ctrno = rdsD.getCtrno();
				TosGumStPlanCntrMt ocrD = new TosGumStPlanCntrMt();
				for(int o=0; o<this.ocrDataList.size(); o++)
				{
					TosGumStPlanCntrMt tempOcrD = this.ocrDataList.get(o);
					String ocrCtrno = tempOcrD.getContainerNumber();
					if(ctrno.equals(ocrCtrno))
					{
						ocrD = tempOcrD;
					}
				}
				TosGumRdsDataFinalMt rdsFinal = populateTosRdsDataFinalMt(rdsD, ocrD, type);
				rdsDataFinalList.add(rdsFinal);
			}
			logger.info("RDS Before validation : "+rdsDataFinalList.size());
			rdsDataFinalList = GumCommonBusinessProcessor.validateContainers(rdsDataFinalList);
			logger.info("RDS After validation : "+rdsDataFinalList.size());
			logger.info("RDS Final - From DOWNLOAD : " + rdsDataFinalList.size());
			NewVesselDaoGum.insertRdsDataFinal(rdsDataFinalList);
			//
			this.additionalCtrFromHon = new ArrayList<TosGumRdsDataFinalMt>();
			for (int o = 0; o < this.ocrDataList.size(); o++)
			{
				TosGumStPlanCntrMt ocrData = this.ocrDataList.get(o);
				String ocrCtrno = ocrData.getContainerNumber();
				boolean foundInRds = false;
				for (int r = 0; r < rdsData.size(); r++)
				{
					TosGumRdsDataMt rdsD = rdsData.get(r);
					String ctrno = rdsD.getCtrno();
					if (ocrCtrno.equals(ctrno))
					{
						foundInRds = true;
						break;
					}
				}
				if(!foundInRds)
				{
					logger.info("Picking up data from stowplan for "+ocrData.getContainerNumber());
					TosGumRdsDataFinalMt rdsFinal1 = populateTosRdsDataFinalMtExtra(ocrData, type);
					this.additionalCtrFromHon.add(rdsFinal1);
					if(rdsFinal1!=null)
						rdsDataFinalListExt.add(rdsFinal1);
				}
			}
			logger.info("RDS Before validation : "+rdsDataFinalListExt.size());
			rdsDataFinalListExt = GumCommonBusinessProcessor.validateContainers(rdsDataFinalListExt);
			logger.info("RDS After validation : "+rdsDataFinalListExt.size());
			logger.info("RDS Final - From .HON : " + this.additionalCtrFromHon.size());
			NewVesselDaoGum.insertRdsDataFinal(rdsDataFinalListExt);
		}
		return true;
	}


	public enum EnumRDS
	{
		ves,voy,leg,containerNumber,shipmentNo,dataSource,routeToPort,loadPort,dischPort,placeDelCode,temp,
		loadService,dischService,hazf,filler1,cntrOversize,primaryCarrier,initialVes,initialVoy,initialLeg,
		inbond,notify,creditStatus,transit,trade,loadType,shipperArolIden,shipperOrgName,shipperOrgNameQual,
		consigneeArolIden,consigneeOrgName,consigneeCo,consigneeOrgNameQual,consigneeAddrLine1,consigneeSuite,
		consigneeCity,consigneeState,consigneeCountry,consigneeZipCode,consigneeDepartment,consigneeTitle,
		consigneeLastName,consigneeFirstName,cmdyDesc,cmdySrvrptDesc,cmdyAg,cmdyHhg,cell,grossWt,highWtFlag,
		sealNo,ownerLessor,damageStatus,emptyFull,ecosRetPort,checkDigit,specMsg1,specMsg2,specMsg3,cnsgAreaCode,
		cnsgPhone1,cnsgPhone2,poNumber,filler2,shmtDestCityCode,vesselType,consigneeFaxNotifyOrgnName,
		consigneeFaxNotifyAttnParty,consigneeFaxNotifyCtryCode,consigneeFaxNotifyAreaCode,consigneeFaxNotifyExch,
		consigneeFaxNotifyStation,consigneeFaxNotifyExt,consignee2NDFaxCtryCode,consignee2NDFaxAreaCode,
		consignee2NDFaxExch,consignee2ndFaxStation,consignee2NDFaxExt,truckerFaxNotifyOrgnName,truckerFaxNotifyAttnParty,
		truckerFaxNotifyCtryCode,truckerFaxNotifyAreaCode,truckerFaxNotifyExch,truckerFaxNotifyStation,truckerFaxNotifyExt,
		trucker2NDFaxCtryCode,trucker2NDFaxAreaCode,trucker2NDFaxExch,trucker2ndFaxStation,trucker2NDFaxExt,
		headQuartersFaxNotifyOrgnName,headQuartersFaxNotifyAttnParty,headQuartersFaxNotifyCtryCode,
		headQuartersFaxNotifyAreaCode,headQuartersFaxNotifyExch,headQuartersFaxNotifyStation,headQuartersFaxNotifyExt,
		headQuarters2NDFaxCtryCode,headQuarters2NDFaxAreaCode,headQuarters2NDFaxExch,headQuarters2ndFaxStation,headQuarters2NDFaxExt,notifyFaxNotifyOrgnName,
		notifyFaxNotifyAttnParty,notifyFaxNotifyCtryCode,notifyFaxNotifyAreaCode,notifyFaxNotifyExch,notifyFaxNotifyStation,
		notifyFaxNotifyExt,notify2NDFaxCtryCode,notify2NDFaxAreaCode,notify2NDFaxExch,notify2ndFaxStation,notify2NDFaxExt,
		carrierCode,shipperRef1Text,milContNbr,milConsignee,milConsigner,govtBlNbr,milPortCall,milTcn,milVNbr,
		milWeight,milCubeFt,milPackNbr,detentoinFreeDays,storageFreeDays,rdd,bookingCsrId,npArcrCarrCode,npOrgnName,
		dPort,csrShipperOrgnId,csrConsigneeOrgnId,csrSpecialSvcArray1,csrSpecialSvcArray2,csrSpecialSvcArray3,
		csrSpecialSvcArray4,csrSpecialSvcArray5,csrClxRoute,csrClxHonCall,csrClxGumCall,filler3,delimeter
	}

	public static void cleanUp() {
		logger.info("RDS cleanUp start");
		if (lookUp != null ) {
			lookUp.close();
			lookUp = null;
		}
		logger.info("RDS cleanUp end");
	}
}
