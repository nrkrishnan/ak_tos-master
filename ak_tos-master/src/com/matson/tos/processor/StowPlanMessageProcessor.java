package com.matson.tos.processor;

import java.math.BigDecimal;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import com.matson.cas.refdata.RefDataLookup;
import com.matson.cas.refdata.mapping.TosDestPodData;
import com.matson.cas.refdata.mapping.TosRdsDataFinalMt;
import com.matson.cas.refdata.mapping.TosStowPlanChassisMt;
import com.matson.cas.refdata.mapping.TosStowPlanCntrMt;
import com.matson.cas.refdata.mapping.TosStowPlanHazMt;
import com.matson.cas.refdata.mapping.TosStowPlanHazMtId;
import com.matson.cas.refdata.mapping.TosStowPlanHoldMt;
import com.matson.cas.refdata.mapping.TosStowPlanHoldMtId;
import com.matson.cas.erd.service.data.VesselVO;
import com.matson.cas.service.ftp.FtpBizException;
import com.matson.cas.service.ftp.FtpProxyGetterBiz;
import com.matson.cas.service.ftp.FtpProxyListBiz;
import com.matson.tos.dao.NewVesselDao;
import com.matson.tos.dao.TosLookup;
import com.matson.tos.exception.NewVesselLogger;
import com.matson.tos.util.TosRefDataUtil;
import com.matson.tos.vo.OCHFields;
import com.matson.tos.vo.OCRFields;
import com.matson.tos.vo.OHLFields;
import com.matson.tos.vo.OHZFields;
import com.matson.tos.vo.ONDFields;
import com.matson.tos.vo.OVRFields;
import com.matson.tos.vo.XmlFields;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;


/*
 * This class processes the .HON file and persists Container, chassis, holds, hazardous, oversize
 * information received in the .HON file.
 * 
 * 	Srno	Date			AuthorName				Change Description
 * 	A1		09/26/2012		Raghu Pattangi			Initial creation
 *  A2		09/30/2012		Karthik Rajendran		Added Stowplan(.HON) Data parsing logics
 *  A3		10/01/2012		Raghu Pattangi		
 *  A4		10/02/2012		Karthik Rajendran		Added: populate ohz, och, methods, Modified: saveStowPlanDetails method.
 *  A5		10/08/2012		Karthik Rajendran		Added populating/mapping logics for LocationStatus,LocationRowDeck,Orientation,
 *  												Hazardous open/close, Actual vessel,voyage, Strength.
 *  A6		10/12/2012		Karthik Rajendran		Added: Bare chassis data persistence.
 *  A7		10/18/2012		Karthik Rajendran		FTP code integration for Stowplan files
 *  A8		12/26/2012		Karthik Rajendran		Added: Custom NewVesselLogger to catcg errors.
 *  A9		01/03/2013		Karthik Rajendran		Added: Able to handle multiple vesvoy(multiple OND record processing) in a single file.
 *  A10		01/04/2013		Karthik Rajendran		Added: .HON file extension check, only process .HON extension file
 *  A11		01/07/2013		Karthik Rajendran		Added: Non .Hon files will archived and deleted.
 *  A12		01/09/2013		Karthik Rajendran		Added: Checking barge stowplan has hazardous containers or not.
 *  														If it has haz containers then checking for DCM data availablilty.
 *  															If DCM data available then process the stowplan data and create output.
 *  															If no DCM data then send error alert.
 *  														If it has no haz containers then process the stowplan data and create output.
 *  A13		01/19/2013		Karthik Rajendran		Added: Stow plan Data existence checking to avoid unique constraint exceptions
 *  												Changed: container number prefix correction 
 *  A14		01/22/2013		Karthik Rajendran		Added: While Chassis record population, need to set DPORT from load 
 *  A15		01/27/2013		Karthik Rajendran		Added: Setting hazf flag by checking OHZ record availability.
 *  A16		01/29/2013		Karthik Rajendran		Added: Adding prefix to container number from all records(ocr,och,ohl,ohz,ovr)
 *  A17		02/21/2013		Karthik Rajendran		Added: Setting default strength code A if input is null
 *  A18		03/07/2013		Karthik Rajendran		Added: Fix for ignoring other vvd containers other than OND vvd for Barge
 *  A19		04/11/2013		Karthik Rajendran		Added: Skip empty lines while parsing.
 *  A20		04/16/2013		Karthik Rajendran		Added: Strip off any comma from Haz_class
 *  A21		07/03/2014		Karthik Rajendran		Added: Barge Process : use action field to hold longhaul direction
 */


public class StowPlanMessageProcessor extends AbstractFileProcessor {

	private static Logger logger = Logger.getLogger(StowPlanMessageProcessor.class);
	private ArrayList<String> contentLines;
	private List<XmlFields> ochFields;
	private List<XmlFields> ocrFields;
	private List<XmlFields> ohzFields;
	private List<XmlFields> ohlFields;
	private List<XmlFields> ovrFields;
	private List<XmlFields> ondFields;
	private ArrayList<OCHFields> ochData; 
	private ArrayList<OCRFields> ocrData;
	private ArrayList<OHZFields> ohzData;
	private ArrayList<OHLFields> ohlData;
	private ArrayList<OVRFields> ovrData;
	public String ondFileVesvoy = "";
	public String ondFileLeg = "";
	private ArrayList<TosStowPlanCntrMt> stowPlanDataList;
	private ArrayList<TosStowPlanChassisMt> stowBareChassisList;
	private ArrayList<TosStowPlanHazMt> stowHazDataList;
	private static String processType = null;
	private ArrayList<TosStowPlanHoldMt> stowPlanHoldList;	
	public static final String	   PRIMARY     = "primary";
	public static final String	   SUPPLEMENT  = "supplement";
	public Date			  triggerDate = null;
	private ArrayList<TosRdsDataFinalMt> rdsDataFinal;
	private NewVesselLogger nvLogger = NewVesselLogger.getInstance();;
	String ftpFileName = null;
	int ftpProxyId = -1;
	int ftpProxyArchId = -1;
	boolean isFileProcessed = false;
	private static TosLookup lookUp=null;
	

	public void processFiles() 
	{
		try {
			ftpProxyId = Integer.parseInt(TosRefDataUtil.getValue("STOWPLAN_IN_FTP_ID"));
			ftpProxyArchId = Integer.parseInt(TosRefDataUtil.getValue("STOWPLAN_ARCH_FTP_ID"));
			int timeout = Integer.parseInt(TosRefDataUtil.getValue("FTP_TIMEOUT"));
			logger.debug("FTP timeout retrieved is: " + timeout);
			FtpProxyListBiz list = new FtpProxyListBiz();
			list.setTimeout(timeout);
			// Modifed the below code to pass the third parameter as *.ANK so that it lists only  files ending with .ANK instead of reading all the files
			String[] stowFiles =  list.getFileNames(ftpProxyId, null, "*.ANK");//new String[]{"KAU734W.ANK"};//list.getFileNames(ftpProxyId, null, "*.ANK");
			logger.debug("Stowplan FIle names : "+stowFiles);
			//logger.info("Stowplan FIle names : "+stowFiles);
/*			if(stowFiles==null || stowFiles.length <= 0)
			{
				//nvLogger.addFileError("", "No stowplan file found in the FTP location.");
				throw new Exception("No stowplan file found in the FTP location.");
			}
			else*/
			if(stowFiles!=null && stowFiles.length > 0)
			{
				FtpProxyGetterBiz getter = new FtpProxyGetterBiz();
				getter.setTimeout(timeout);
				setupStowPlanFieldsData();
				for(int i=0; i<stowFiles.length; i++)
				{
					ftpFileName = stowFiles[i];
					logger.debug("Processing file: " + ftpFileName);
					//String stowplanFile = "/home/aasija/stowplan/KAU734W.ANK";
					//URL url = new URL("file:///home/aasija/stowplan/KAU734W.ANK");
					//String content = readFile(stowplanFile);//new Scanner(url.openStream()).useDelimiter("\\Z").next();
					
					String contents = getter.getFileText(ftpProxyId, ftpFileName);
					logger.debug("STOWPLAN FILE CONTENT :: "+contents);
					String startWFTime  = (new SimpleDateFormat("MM-dd-yyyy_HH-mm-ss")).format(new Date()).toString();
					if(ftpFileName.endsWith(".ANK"))
					{
						//CommonBusinessProcessor.archivetoFTP(contents, ftpProxyArchId, ftpFileName+"_"+startWFTime,null);
						//
						resetStowPlanData();
						triggerDate = new Date();
						//
						isFileProcessed = false;
						processFile(contents);
						//
						if(isFileProcessed)
						{
							logger.info("ondFileVesvoy  testing  :"+ondFileVesvoy);
							if (!"".equalsIgnoreCase(ondFileVesvoy)) {
								CommonBusinessProcessor.archivetoFTP(contents, ftpProxyArchId, ondFileVesvoy+ondFileLeg+".ANK"+"_"+startWFTime,null);
							}else {
								CommonBusinessProcessor.archivetoFTP(contents, ftpProxyArchId, ftpFileName+"_"+startWFTime,null);
							}
							CommonBusinessProcessor.deleteFtpFiles(ftpProxyId, ftpFileName,null);
						}
					}
					else
					{
						nvLogger.addFileError(ftpFileName, ftpFileName+" is not a .ANK stowplan file");
						//CommonBusinessProcessor.archivetoFTP(contents, ftpProxyArchId, ftpFileName+"_"+startWFTime,null);
						// Code Fix to prevent .GUM files being deleted - Start
						logger.info("Commenting code which calls deleteFTP files when not GUM");
						//CommonBusinessProcessor.deleteFtpFiles(ftpProxyId, ftpFileName,null);
						// Code Fix to prevent .GUM files being deleted - End
					}
				}
			}
		} catch (FtpBizException ftpEx) {
			logger.error("FTP error found: ", ftpEx);
			nvLogger.addFtpError(""+ftpProxyId, "FTP ERROR: Unable to get into FTP");
		} catch (Exception e) {
			logger.error("Exception: ", e);
			e.printStackTrace();
		} finally {
			if ("barge".equalsIgnoreCase(processType) || "newves".equalsIgnoreCase(processType)) {
				logger.info(" stowplan message prcoessor Cleaning up of static variables start");
				CommonBusinessProcessor.outboundExportVesselMap = null;
				CommonBusinessProcessor.outboundNIVesselMap = null;
				CommonBusinessProcessor.outboundVesselMap = null;
				CommonBusinessProcessor.portCodeTradeMap = null;
				CommonBusinessProcessor.arrivalDateMap = null;
				if (lookUp!=null){
					lookUp.close();
					lookUp = null;
				}
				logger.info("stowplan message prcoessor Cleaning up of static variables end");
			}
			
		}
	}
	public StowPlanMessageProcessor()
	{
		//setupStowPlanFieldsData();
		processFiles();
		nvLogger.sendNewVessErrors();
	}
	public static void main(String[] args)
	{
		//StowPlanMessageProcessor spmp = new StowPlanMessageProcessor();
		//spmp.setupStowPlanFieldsData();
		//spmp.processFile(honFile);
	}
	private void setupStowPlanFieldsData()
	{
		ocrFields = CommonBusinessProcessor.getFields("/newvesXml/OCR.xml");
		ochFields = CommonBusinessProcessor.getFields("/newvesXml/OCH.xml");
		ohzFields = CommonBusinessProcessor.getFields("/newvesXml/OHZ.xml");
		ohlFields = CommonBusinessProcessor.getFields("/newvesXml/OHL.xml");
		ovrFields = CommonBusinessProcessor.getFields("/newvesXml/OVR.xml");
		ondFields = CommonBusinessProcessor.getFields("/newvesXml/OND.xml");
	}
	private void resetStowPlanData()
	{
		ochData = new ArrayList<OCHFields>();
		ocrData = new ArrayList<OCRFields>();
		ohzData = new ArrayList<OHZFields>();
		ohlData = new ArrayList<OHLFields>();
		ovrData = new ArrayList<OVRFields>();
		stowPlanDataList = new ArrayList<TosStowPlanCntrMt>();
		stowBareChassisList = new ArrayList<TosStowPlanChassisMt>();
		stowHazDataList = new ArrayList<TosStowPlanHazMt>();
		stowPlanHoldList = new ArrayList<TosStowPlanHoldMt>();
	}
	public void processFile(String contents)
	{		
		try{
			contentLines = new ArrayList<String> (Arrays.asList(contents.split("\n")));
			if(contentLines.size()>0)
			{

				for (int i=0; i<contentLines.size(); i++) {
					processLine(contentLines.get(i).toString(),i+1); 
				}
				logger.info("ondFileVesvoy:"+ondFileVesvoy);
				if("".equalsIgnoreCase(ondFileVesvoy)){
					nvLogger.addFileError(ftpFileName, "Error: No OND record vesvoy found. Please check the file for errors.");
					logger.info("Error: No OND data found.");
					CommonBusinessProcessor.deleteFtpFiles(ftpProxyId, ftpFileName,null);
				}
			}
			else {
				logger.error("Error: No data.");
				nvLogger.addFileError(ftpFileName, "Error: No data. Check file for errors.");
				return;
			}
		} catch (Exception e) {
			logger.error("Exception: ", e);
			e.printStackTrace();
		}
	}

	public void processLine(String line, int lineNum)
	{
		if(line==null || line.length()<=0)
			return;
		try {
			if (line.startsWith("OCR"))
			{
				processOCRData(line);
			}
			else if (line.startsWith("OCH"))
			{
				processOCHData(line);
			}
			else if (line.startsWith("OHZ"))
			{
				processOHZData(line);
			}
			else if (line.startsWith("OHL"))
			{
				processOHLData(line);
			}
			else if (line.startsWith("OVR"))
			{
				processOVRData(line);
			}
			else if(line.startsWith("OND"))
			{
				processONDData(line);
				sendStowPlanData();
				resetStowPlanData();
			}
		}
		catch(Exception e){
			nvLogger.addFileError(ftpFileName, "Unable to parse the text. Line "+lineNum+"\\n\\n"+ line);
			logger.debug("Exception : "+ e.getMessage());
		}
	}
	private void sendStowPlanData()
	{
		HashMap stowPlanMap = new HashMap();
		String loadport="";
		stowPlanMap.put("OCR", ocrData);
		stowPlanMap.put("OCH", ochData);
		stowPlanMap.put("OHL", ohlData);
		stowPlanMap.put("OHZ", ohzData);
		stowPlanMap.put("OVR", ovrData);
		logger.info("OCR - "+ ocrData.size());
		logger.info("OCH - "+ ochData.size());
		logger.info("OHZ - "+ ohzData.size());
		logger.info("OHL - "+ ohlData.size());
		logger.info("OVR - "+ ovrData.size());
		String vesvoy = ondFileVesvoy;	
		logger.info("getting vessel details:"+vesvoy);

		String ondvvd = ondFileVesvoy+ondFileLeg;
		logger.info("ondvvd: "+ondvvd);
		for(int i=0; i<ocrData.size(); i++)
		{
			OCRFields ocrf = ocrData.get(i);
			logger.info("ocrf.getVesvoy()+ocrf.getLeg()"+ocrf.getVesvoy()+ocrf.getLeg());
			if (ondvvd.equalsIgnoreCase(ocrf.getVesvoy()+ocrf.getLeg())){
				loadport = ocrf.getLoad();
				logger.info("in sendStowPlanData loadport "+loadport);
				break;
			}
			logger.info("in sendStowPlanData loadport "+loadport);
		}

		VesselVO vvo = CommonBusinessProcessor.getVesselDetails(vesvoy.substring(0, 3));
		if(vvo.getVessType() != null && vvo.getVessType().equalsIgnoreCase("B"))
		{
			processType = "barge";
			boolean isStowHasHazContainers = checkHazardousContainersInStowPlanData(ocrData);
			logger.info(" isStowHasHazContainers : "+isStowHasHazContainers+ " - "+vesvoy+" - "+ondFileLeg);
			boolean isDcmAvailable = NewVesselDao.isDCMAvailForVesselWithVVD(vesvoy+ondFileLeg);
			logger.info("isDcmAvailable :"+isDcmAvailable);
			if(isStowHasHazContainers)
			{
				if(!isDcmAvailable)
				{   
					logger.info(" No HAZ/DCM error for barge "+vesvoy+" at "+loadport);
					nvLogger.addError(vesvoy+ondFileLeg, "", loadport+"-No HAZ/DCM");
					isFileProcessed = false;
					return;
				}
			}
			try{
				NewVesselDao.deleteStowPlanData(vesvoy);
			}catch(Exception e)
			{
				nvLogger.addError(vesvoy, "", "Unable to delete stow plan data <br /><br />"+e);
				return;
			}
			saveStowPlanDetail(stowPlanMap);
			new NewvesProcessorHelper().startBargeProc(vesvoy, ondFileLeg, triggerDate);
			isFileProcessed = true;
		}
		else if(vvo.getVessType() != null && vvo.getVessType().equalsIgnoreCase("L"))
		{
			processType = "newves";
			try{
				NewVesselDao.deleteStowPlanData(vesvoy);
			}catch(Exception e)
			{
				nvLogger.addError(vesvoy, "", "Unable to delete stow plan data <br /><br />"+e);
				return;
			}
			try {
				saveStowPlanDetail(stowPlanMap);
				isFileProcessed = true;
				nvLogger.sendNewVessStowPlanSuccess(vesvoy, stowPlanDataList.size());
				String availableDateStr = null;
				if(lookUp==null)
					lookUp = new TosLookup();
				availableDateStr =lookUp.getBeginReceive(vesvoy,"ANK");
				nvLogger.sendAvailDateNotification(vesvoy, availableDateStr,"stowplan");
				
			}
			catch(Exception e)
			{
				nvLogger.addFileError(ftpFileName, "Error in "+ondFileVesvoy+"Unable to persist the data into database. Check the database/file for errors.");
			}
		}
	}

	private boolean checkHazardousContainersInStowPlanData(ArrayList<OCRFields> ocrDataList)
	{
		if(ocrDataList!=null)
		{
			for(int i=0; i<ocrDataList.size(); i++)
			{
				OCRFields ocrf = ocrDataList.get(i);
				String vvd = ocrf.getVesvoy()+ocrf.getLeg();
				if(ocrf.getHazf()!=null && ocrf.getHazf().equals("Y")&&vvd.equalsIgnoreCase(ondFileVesvoy+ondFileLeg))
				{	
					logger.info("Container number having HAZ record is "+ocrf.getCtrNo());
					return true;
				}
			}
		}
		return false;
	}

	private void processOCRData(String line)
	{
		int beginIndex = 0, endIndex = 0;
		OCRFields ocrf = new OCRFields();
		for(int i=0; i<ocrFields.size(); i++)
		{
			XmlFields fld = (XmlFields)ocrFields.get(i);
			endIndex = new Integer(fld.getFieldLength());
			String temp_str = "";
			if(line.length() >= beginIndex) 
			{
				if(line.substring(beginIndex, line.length()).length()>=endIndex)
					temp_str = line.substring(beginIndex, beginIndex + endIndex).trim();
				else
					temp_str = line.substring(beginIndex, line.length());
			}
			switch(EnumOCRFields.valueOf(fld.getFieldName()))
			{
			case tranCode:
				ocrf.setTranCode(temp_str);break;				 
			case date:
				ocrf.setDate(temp_str); break;
			case time:
				ocrf.setTime(temp_str);break; 
			case user:
				ocrf.setUser(temp_str);break; 
			case pdlgId:
				ocrf.setPdlgId(temp_str); break;
			case ctrNo:
				ocrf.setCtrNo(temp_str); break;
			case dir:
				ocrf.setDir(temp_str); break;
			case srv:
				ocrf.setSrv(temp_str); break;
			case vesvoy:
				ocrf.setVesvoy(temp_str); break;
			case hazf:
				ocrf.setHazf(temp_str); break;
			case odf:
				ocrf.setOdf(temp_str); break;
			case temp:
				ocrf.setTemp(temp_str); break;
			case seal:
				ocrf.setSeal(temp_str); break;
			case cell:
				ocrf.setCell(temp_str); break;
			case typeCode:
				ocrf.setTypeCode(temp_str); break;
			case cWeight:
				ocrf.setcWeight(temp_str); break;
			case discharge:
				ocrf.setDischarge(temp_str); break;
			case dPort:
				ocrf.setdPort(temp_str); break;
			case commodity:
				ocrf.setCommodity(temp_str); break;
			case owner:
				ocrf.setOwner(temp_str); break;
			case tare:
				ocrf.setTare(temp_str); break;
			case tempUnit:
				ocrf.setTempUnit(temp_str); break;
			case bookNo:
				ocrf.setBookNo(temp_str); break;
			case load:
				ocrf.setLoad(temp_str); break;
			case hgt:
				ocrf.setHgt(temp_str); break;
			case strength:
				ocrf.setStrength(temp_str); break;
			case consignee:
				ocrf.setConsignee(temp_str);break;
			case shipper:
				ocrf.setShipper(temp_str); break;
			case cd:
				ocrf.setCd(temp_str); break;
			case damage:
				ocrf.setDamage(temp_str); break;
			case chassis:
				ocrf.setChassis(temp_str); break;
			case pDisp:
				ocrf.setpDisp(temp_str); break;
			case retPort:
				ocrf.setRetPort(temp_str); break;
			case leg:
				ocrf.setLeg(temp_str); break;
			case ds:
				ocrf.setDs(temp_str); break;
			case aei:
				ocrf.setAei(temp_str); break;
			case longhaul:
				ocrf.setLonghaul(temp_str);break; 
			case longhaulDir:
				ocrf.setLonghaulDir(temp_str); break;
			case comment:
				ocrf.setComment(temp_str); break;
			case emptyFull:
				ocrf.setEmptyFull(temp_str); break;
			case primaryCarrier:
				ocrf.setPrimaryCarrier(temp_str); break;
			case trade:
				ocrf.setTrade(temp_str); break;
			case ovzHeight:
				ocrf.setOvzHeight(temp_str); break;
			case ovzLeft:
				ocrf.setOvzLeft(temp_str); break;
			case ovzRight:
				ocrf.setOvzRight(temp_str); break;
			case ovzFront:
				ocrf.setOvzFront(temp_str); break;
			case ovzRear:
				ocrf.setOvzRear(temp_str); break;
			case cmisVent:
				ocrf.setCmisVent(temp_str); break;
			case multipleHazardous:
				ocrf.setMultipleHazardous(temp_str); break;
			case imoClass:
				ocrf.setImoClass(temp_str); break;
			case primaryCntrllr:
				ocrf.setPrimaryCntrllr(temp_str); break;
			case temp2:
				ocrf.setTemperature2(temp_str); break;
			case psi:
				ocrf.setPriorityStow(temp_str); break;
			}
			beginIndex = beginIndex + endIndex;
		}
		ocrData.add(ocrf);
		//sendOCRData(ocrf);
	}
	private TosStowPlanCntrMt populateTosStowPlanCntrMt(OCRFields ocrf)
	{
		TosStowPlanCntrMt ocr = new TosStowPlanCntrMt();
		String ctrno = ocrf.getCtrNo();
		ctrno = ctrno==null?"":ctrno;
		//String owner = ocrf.getOwner();
		//owner = owner==null?"":owner;
		if(ctrno.length()>0)
		{
			if(!StringUtils.isAlpha(ctrno.substring(0, 1)))
				ctrno = "MATU" + ctrno;
		}
		ocr.setContainerNumber(ctrno);
		ocr.setSrv(ocrf.getSrv());
		ocr.setDport(ocrf.getdPort());
		ocr.setDischargePort(ocrf.getDischarge());
		ocr.setDir(ocrf.getDir());
		ocr.setLeg(ocrf.getLeg());
		

		ocr.setVesvoy(ondFileVesvoy);
		ocr.setLeg(ondFileLeg);

		ocr.setHazf(ocrf.getHazf());
		ocr.setOdf(ocrf.getOdf());
		String temp = ocrf.getTemp();
		temp = temp==null?"":temp;
		temp = temp.length()>3?temp.substring(0, 3):temp;
		ocr.setTemp(temp);
		ocr.setSealNumber(ocrf.getSeal());
		ocr.setCell(ocrf.getCell());
		ocr.setTypeCode(ocrf.getTypeCode());
		String cweight = ocrf.getcWeight();
		cweight = cweight==null?"":cweight;
		if(cweight.length()>0)
			ocr.setCweight(new BigDecimal(cweight));


		String commodity = ocrf.getCommodity();
		commodity =	commodity.length() >= 8 ? commodity.substring(0, 8)
				: commodity;
		ocr.setCommodity(commodity);
		ocr.setOwner(ocrf.getOwner());
		String tare = ocrf.getTare();
		tare = tare==null?"":tare;
		if(tare.length()>0)
			ocr.setTareWeight(new BigDecimal(tare));
		ocr.setTempMeasurementUnit(ocrf.getTempUnit());
		ocr.setBookingNumber(ocrf.getBookNo());
		ocr.setLoadPort(ocrf.getLoad());
		ocr.setHgt(ocrf.getHgt());
		ocr.setStrength(ocrf.getStrength()==null?"IA":ocrf.getStrength());
		ocr.setConsignee(ocrf.getConsignee());
		ocr.setShipper(ocrf.getShipper());
		ocr.setCheckDigit(ocrf.getCd());
		ocr.setDamageCode(ocrf.getDamage());
		String chsNbr = ocrf.getChassis();
		if(chsNbr != null && chsNbr.equalsIgnoreCase("~"))
			chsNbr = null;
		ocr.setChassisNumber(chsNbr);
		ocr.setPlanDisp(ocrf.getpDisp());
		ocr.setRetPort(ocrf.getRetPort());
		ocr.setDs(ocrf.getDs());
		ocr.setAei(ocrf.getAei());
		ocr.setComments(ocrf.getComment());
		ocr.setErf(ocrf.getEmptyFull());

		String height = ocrf.getOvzHeight();
		height = height==null?"":height;
		if(height.length()>0)
			ocr.setOversizeHeightInches(new BigDecimal(height));
		else
			ocr.setOversizeHeightInches(null);

		String left = ocrf.getOvzLeft();
		left = left==null?"":left;
		if(left.length()>0)
			ocr.setOversizeLeftInches(new BigDecimal(left));
		else
			ocr.setOversizeLeftInches(null);

		String right = ocrf.getOvzRight();
		right = right==null?"":right;
		if(right.length()>0)
			ocr.setOversizeRightInches(new BigDecimal(right));
		else
			ocr.setOversizeRightInches(null);

		String front = ocrf.getOvzFront();
		front = front==null?"":front;
		if(front.length()>0)
			ocr.setOversizeFrontInches(new BigDecimal(front));
		else
			ocr.setOversizeFrontInches(null);

		String rear = ocrf.getOvzRear();
		rear = rear==null?"":rear;
		if(rear.length()>0)
			ocr.setOversizeRearInches(new BigDecimal(rear));
		else
			ocr.setOversizeRearInches(null);	

		ocr.setCreateUser("newves");
		ocr.setCreateDate(triggerDate);
		ocr.setLastUpdateUser("newves");
		ocr.setLastUpdateDate(triggerDate);
		String dischPort = ocrf.getDischarge();
		dischPort = dischPort==null?"":dischPort;
		if(dischPort.equalsIgnoreCase("ANK"))
			ocr.setLocationStatus("4");
		else
			ocr.setLocationStatus("2");
		
		ocr.setLocationRowDeck(ocrf.getPrimaryCarrier());
		ocr.setOrientation(ocrf.getEmptyFull());
		ocr.setHazardousOpenCloseFlag(ocrf.getTrade());
		if("barge".equalsIgnoreCase(processType)){
			if (ocrf.getLonghaul() !=null && ocrf.getLonghaul().length() >=6 ) {
				if (!ondFileVesvoy.equals(ocrf.getLonghaul())){
					ocr.setActualVessel(ocrf.getLonghaul().substring(0, 3));
					ocr.setActualVoyage(ocrf.getLonghaul().substring(3, 6));ocr.setAction(ocrf.getLonghaulDir()); //A21
				}
			}
		}else {
			ocr.setActualVessel(ocrf.getVesvoy().substring(0, 3));
			ocr.setActualVoyage(ocrf.getVesvoy().substring(3, 6));
		}
		ocr.setTemperature2(ocrf.getTemperature2());
		ocr.setPriorityStow(ocrf.getPriorityStow());
		return ocr;
		//NewVesselDao.insertOCRData(ocr);
	}
	private void processOCHData(String line)
	{
		int beginIndex = 0, endIndex = 0;
		OCHFields ochf = new OCHFields();
		for(int i=0; i<ochFields.size(); i++)
		{
			XmlFields fld = (XmlFields)ochFields.get(i);
			endIndex = new Integer(fld.getFieldLength());
			String temp_str = line.substring(beginIndex, beginIndex + endIndex).trim();
			switch(EnumOCHFields.valueOf(fld.getFieldName()))
			{
			case tranCode:
				ochf.setTranCode(temp_str);break;
			case date:
				ochf.setDate(temp_str);break;
			case time:
				ochf.setTime(temp_str);break;
			case user:
				ochf.setUser(temp_str);break;
			case pdlgId:
				ochf.setPdlgId(temp_str);break;
			case chas:
				ochf.setChas(temp_str);break;
			case cd:
				ochf.setCd(temp_str);break;
			case cell:
				ochf.setCell(temp_str);break;
			case tare:
				ochf.setTare(temp_str);break;
			case vesvoy:
				ochf.setVesvoy(temp_str);break;
			case leg:
				ochf.setLeg(temp_str);break;
			case load:
				ochf.setLoad(temp_str);break;
			case srv:
				ochf.setSrv(temp_str);break;
			case dmg:
				ochf.setDmg(temp_str);break;
			case typeCode:
				ochf.setTypeCode(temp_str);break;
			case owner:
				ochf.setOwner(temp_str);break;
			case ctrNo:
				ochf.setCtrNo(temp_str);break;
			case mgNum:
				ochf.setMgNum(temp_str);break;
			case mgTare:
				ochf.setMgTare(temp_str);break;
			case mgCd:
				ochf.setMgCd(temp_str);break;
			case primaryCarrier:
				ochf.setPrimaryCarrier(temp_str);break;
			case trade:
				ochf.setTrade(temp_str);break;
			}
			beginIndex = beginIndex + endIndex;
		}
		ochData.add(ochf);
		//sendOCHData(ochf);
	}	
	private TosStowPlanChassisMt populateTosStowPlanChassisMt(OCHFields ochf)
	{
		TosStowPlanChassisMt och = new TosStowPlanChassisMt();
		och.setContainerNumber(ochf.getCtrNo());
		och.setChassisNumber(ochf.getChas());
		och.setChassisCd(ochf.getCd());
		och.setChassisTare(new BigDecimal(ochf.getTare()));
		och.setChassisHolds(ochf.getPrimaryCarrier());
		och.setDamageCode(ochf.getDmg());
		och.setLoc(ochf.getCell());
		String srv = ochf.getSrv();
		if(srv == null || srv.equalsIgnoreCase(""))
			srv = "MAT";
		och.setSrv(srv);
		och.setTypeCode(ochf.getTypeCode());
		String owner = ochf.getOwner();
		String chsNbr = ochf.getChas();
		if(owner == null || owner.equalsIgnoreCase(""))
		{
			if(chsNbr != null && chsNbr.substring(0, 4).equalsIgnoreCase("MATZ"))
			{
				owner = "MATU";
			}
		}
		och.setOwner(owner);
		och.setLocationStatus("4");
		och.setMgNumber(ochf.getMgNum());
		och.setMgTare(ochf.getMgTare().length() >0 ? new BigDecimal(ochf.getMgTare()) : null);
		och.setVesvoy(ondFileVesvoy);
		//och.setLoadPort(ochf.getLoad());
		och.setDport(ochf.getLoad()); // This is actually the destination not the load port
		och.setCreateUser("newves");
		och.setCreateDate(triggerDate);
		och.setLastUpdateUser("newves");
		och.setLastUpdateDate(triggerDate);
		return och;
	}
	private void processOHZData(String line)
	{
		int beginIndex = 0, endIndex = 0;
		OHZFields ohzf = new OHZFields();
		for(int i=0; i<ohzFields.size(); i++)
		{
			XmlFields fld = (XmlFields)ohzFields.get(i);
			endIndex = new Integer(fld.getFieldLength());
			String temp_str = line.substring(beginIndex, beginIndex + endIndex).trim();
			switch(EnumOHZFields.valueOf(fld.getFieldName()))
			{
			case tranCode:
				ohzf.setTranCode(temp_str);break;
			case date:
				ohzf.setDate(temp_str);break;
			case time:
				ohzf.setTime(temp_str);break;
			case user:
				ohzf.setUser(temp_str);break;
			case pdlgId:
				ohzf.setPdlgId(temp_str);break;
			case ctrNo:
				ohzf.setCtrNo(temp_str);break;
			case unNum:
				ohzf.setUnNum(temp_str);break;
			case regs:
				ohzf.setRegs(temp_str);break;
			case hazClass:
				ohzf.setHazClass(temp_str);break;
			case desc:
				ohzf.setDesc(temp_str);break;
			case vesvoy:
				ohzf.setVesvoy(temp_str);break;
			case leg:
				ohzf.setLeg(temp_str);break;
			}
			beginIndex = beginIndex + endIndex;
		}
		ohzData.add(ohzf);
		//sendOHZData(ohzf);
	}	
	private TosStowPlanHazMt populateTosStowPlanHazMt(OHZFields ohzf)
	{
		TosStowPlanHazMt ohz = new TosStowPlanHazMt();
		ohz.setContainerNumber(ohzf.getCtrNo());
		ohz.setUnNumber(new BigDecimal(ohzf.getUnNum()));
		String hazClass = ohzf.getHazClass();
		if(hazClass!=null)
		{
			hazClass = hazClass.replaceAll(",", "").trim();
		}
		ohz.setHazClass(hazClass);
		ohz.setRegs(ohzf.getRegs());
		ohz.setDescription(ohzf.getDesc());
		ohz.setCreateUser("newves");
		ohz.setCreateDate(triggerDate);
		ohz.setLastUpdateUser("newves");
		ohz.setLastUpdateDate(triggerDate);
		return ohz;
	}
	private TosStowPlanHazMtId populateTosStowPlanHazMtId(OHZFields ohzf)
	{
		TosStowPlanHazMtId ohzid = new TosStowPlanHazMtId();
		ohzid.setHazClass(ohzf.getHazClass());
		ohzid.setUnNumber(new BigDecimal(ohzf.getUnNum()));
		return ohzid;
	}
	private void processOHLData(String line)
	{
		int beginIndex = 0, endIndex = 0;
		OHLFields ohlf = new OHLFields();
		for(int i=0; i<ohlFields.size(); i++)
		{
			XmlFields fld = (XmlFields)ohlFields.get(i);
			endIndex = new Integer(fld.getFieldLength());
			String temp_str = line.substring(beginIndex, beginIndex + endIndex).trim();
			switch(EnumOHLFields.valueOf(fld.getFieldName()))
			{
			case tranCode:
				ohlf.setTranCode(temp_str); break;
			case date:
				ohlf.setDate(temp_str); break;
			case time:
				ohlf.setTime(temp_str); break;
			case user1:
				ohlf.setUser1(temp_str); break;
			case pdlgId:
				ohlf.setPdlgId(temp_str); break;
			case bill:
				ohlf.setBill(temp_str); break;
			case eqNo:
				ohlf.setEqNo(temp_str); break;
			case vesvoy:
				ohlf.setVesvoy(temp_str); break;
			case srv:
				ohlf.setSrv(temp_str); break;
			case code:
				ohlf.setCode(temp_str); break;
			case pby:
				ohlf.setPby(temp_str); break;
			case hType:
				ohlf.sethType(temp_str); break;
			case iGate:
				ohlf.setiGate(temp_str); break;
			case oGate:
				ohlf.setoGate(temp_str); break;
			case rfs:
				ohlf.setRfs(temp_str); break;
			case desc:
				ohlf.setDesc(temp_str); break;
			case aDateFormat:
				ohlf.setaDateFormat(temp_str); break;
			case aTimeFormat:
				ohlf.setaTimeFormat(temp_str); break;
			case cDateFormat:
				ohlf.setcDateFormat(temp_str); break;
			case cTimeFormat:
				ohlf.setcTimeFormat(temp_str); break;
			case aFlag:
				ohlf.setaFlag(temp_str); break;
			case user2:
				ohlf.setUser2(temp_str); break;
			case leg:
				ohlf.setLeg(temp_str); break;
			}
			beginIndex = beginIndex + endIndex;
		}
		ohlData.add(ohlf);
		//sendOHLData(ohlf);
	}
	private TosStowPlanHoldMt populateTosStowPlanHoldMt(OHLFields ohlf)
	{
		TosStowPlanHoldMt ohl = new TosStowPlanHoldMt();
		ohl.setContainerNumber(ohlf.getEqNo());
		ohl.setPlacedBy(ohlf.getPby());
		ohl.setHoldType(ohlf.gethType());
		ohl.setIngateAction(ohlf.getiGate());
		ohl.setOutgateAction(ohlf.getoGate());
		ohl.setRfsAction(ohlf.getRfs());
		ohl.setDescription(ohlf.getDesc());
		String aDate  = ohlf.getaDateFormat();
		aDate = aDate==null?"":aDate;
		try{
			//create SimpleDateFormat object with source string date format
			SimpleDateFormat sdfSource = new SimpleDateFormat("MM/dd/yy");

			//parse the string into Date object
			//Date date = sdfSource.parse(aDate);
			ohl.setActiveDate(sdfSource.parse(aDate));	
			String cDate = ohlf.getcDateFormat();
			cDate = cDate==null?"":cDate;
			ohl.setActiveTime(ohlf.getaTimeFormat());
			ohl.setCancelDate(sdfSource.parse(cDate));
			ohl.setCancelTime(ohlf.getcTimeFormat());
		}
		catch(ParseException e)
		{
			e.printStackTrace();
			logger.error("ActiveDate Parsing error");
		}
		ohl.setUsr(ohlf.getUser2());	
		ohl.setCreateUser("newves");
		ohl.setCreateDate(triggerDate);
		ohl.setLastUpdateUser("newves");
		ohl.setLastUpdateDate(triggerDate);
		return ohl;

	}
	private TosStowPlanHoldMtId populateTosStowPlanHoldMtId(OHLFields ohlf)
	{
		TosStowPlanHoldMtId ohlid = new TosStowPlanHoldMtId();
		ohlid.setCode(ohlf.getCode());
		return ohlid;
	}
	private void processOVRData(String line)
	{
		int beginIndex = 0, endIndex = 0;
		OVRFields ovrf = new OVRFields();
		for(int i=0; i<ovrFields.size(); i++)
		{
			XmlFields fld = (XmlFields)ovrFields.get(i);
			endIndex = new Integer(fld.getFieldLength());
			String temp_str = line.substring(beginIndex, beginIndex + endIndex).trim();
			switch(EnumOVRFields.valueOf(fld.getFieldName()))
			{
			case tranCode:
				ovrf.setTranCode(temp_str); break;
			case date:
				ovrf.setDate(temp_str); break;
			case time:
				ovrf.setTime(temp_str); break;
			case user:
				ovrf.setUser(temp_str); break;
			case pdlgId:
				ovrf.setPdlgId(temp_str); break;
			case ctrNo:
				ovrf.setCtrNo(temp_str); break;
			case height:
				ovrf.setHeight(temp_str); break;
			case left:
				ovrf.setLeft(temp_str); break;
			case right:
				ovrf.setRight(temp_str); break;
			case front:
				ovrf.setFront(temp_str); break;
			case rear:
				ovrf.setRear(temp_str); break;
			case vesvoy:
				ovrf.setVesvoy(temp_str); break;
			case leg:
				ovrf.setLeg(temp_str); break;
			}
			beginIndex = beginIndex + endIndex;
		}
		ovrData.add(ovrf);
	}
	private void processONDData(String line)
	{
		int beginIndex = 0, endIndex = 0;
		ONDFields ondf = new ONDFields();
		for(int i=0; i<ondFields.size(); i++)
		{
			XmlFields fld = (XmlFields)ondFields.get(i);
			endIndex = new Integer(fld.getFieldLength());
			String temp_str = line.substring(beginIndex, beginIndex + endIndex).trim();
			switch(EnumONDFields.valueOf(fld.getFieldName()))
			{
			case tranCode:
				ondf.setTranCode(temp_str); break;
			case date:
				ondf.setDate(temp_str); break;
			case time:
				ondf.setTime(temp_str); break;
			case user:
				ondf.setUser(temp_str); break;
			case pdlgId:
				ondf.setPdlgId(temp_str); break;
			case vesvoy:
				ondf.setVesvoy(temp_str); break;
			case leg:
				ondf.setLeg(temp_str); break;
			}
			beginIndex = beginIndex + endIndex;
		}
		//ovrData.add(ovrf);
		this.ondFileVesvoy = ondf.getVesvoy();
		ondFileLeg = ondf.getLeg();
	}
	private void saveStowPlanDetail(HashMap stowPlanMap) {
		ArrayList ocrDataList = (ArrayList)stowPlanMap.get("OCR");
		ArrayList ochDataList = (ArrayList)stowPlanMap.get("OCH");
		ArrayList ohlDataList = (ArrayList)stowPlanMap.get("OHL");
		ArrayList ohzDataList = (ArrayList)stowPlanMap.get("OHZ");
		ArrayList ovrDataList = (ArrayList)stowPlanMap.get("OVR");

		for (int i = 0; i<ocrDataList.size(); i++) {
			OCRFields ocr1 = (OCRFields)ocrDataList.get(i);
			int temp = 0;
			if("barge".equalsIgnoreCase(processType)){
				if(!(ondFileVesvoy+ondFileLeg).equalsIgnoreCase(ocr1.getVesvoy()+ocr1.getLeg()))
				{
					temp++;
					logger.info("Ignoring container **** "+ocr1.getCtrNo()+"\t"+ocr1.getVesvoy()+"\t"+ocr1.getLeg());
				}
			}
			if(temp == 0){
				TosStowPlanCntrMt stowPlan = populateTosStowPlanCntrMt(ocr1);
				String ctrno = stowPlan.getContainerNumber();
				//System.out.println("Stowplan cntrno: "+ctrno);
				//String owner = stowPlan.getOwner();
				TosStowPlanHoldMt stowPlanHold;
				TosStowPlanHoldMtId stowPlanHoldId;
				TosStowPlanChassisMt stowPlanChassis = null;
				TosStowPlanHazMt stowPlanHaz;
				Set<TosStowPlanHoldMt> setStowPlanHold = new HashSet<TosStowPlanHoldMt>(0);
				Set<TosStowPlanChassisMt> setStowPlanChassis = new HashSet<TosStowPlanChassisMt>(0);
				Set<TosStowPlanHazMt> setStowPlanHaz = new HashSet<TosStowPlanHazMt>(0);
				for (int v=0; v<ovrDataList.size(); v++)
				{
					OVRFields ovr1 = (OVRFields) ovrDataList.get(v);
					String ovrctrno = ovr1.getCtrNo();
					if(ovrctrno!=null && ovrctrno.length()>0)
					{
						if(!StringUtils.isAlpha(ovrctrno.substring(0, 1)))
							ovrctrno = "MATU" + ovrctrno;
					}
					if(ovrctrno!=null && ctrno.equalsIgnoreCase(ovrctrno))
					{
						stowPlan.setOversizeHeightInches(ovr1.getHeight().length()>0?new BigDecimal(ovr1.getHeight()):null);
						stowPlan.setOversizeFrontInches(ovr1.getFront().length()>0?new BigDecimal(ovr1.getFront()):null);
						stowPlan.setOversizeLeftInches(ovr1.getLeft().length()>0?new BigDecimal(ovr1.getLeft()):null);
						stowPlan.setOversizeRearInches(ovr1.getRear().length()>0?new BigDecimal(ovr1.getRear()):null);
						stowPlan.setOversizeRightInches(ovr1.getRight().length()>0?new BigDecimal(ovr1.getRight()):null);
						if(stowPlan.getOversizeFrontInches()!=null || stowPlan.getOversizeHeightInches()!=null
								|| stowPlan.getOversizeLeftInches()!=null || stowPlan.getOversizeRearInches() != null
								|| stowPlan.getOversizeRightInches()!=null)
						{
							stowPlan.setOdf("Y");
						}
					}
				}
				for (int j=0; j<ohlDataList.size(); j++)
				{
					OHLFields ohl1 = (OHLFields) ohlDataList.get(j);
					String hlCtrno = ohl1.getEqNo();
					if(hlCtrno!=null && hlCtrno.length()>0)
					{
						if(!StringUtils.isAlpha(hlCtrno.substring(0, 1)))
							hlCtrno = "MATU" + hlCtrno;
					}
					if(hlCtrno!=null && ctrno.equalsIgnoreCase(hlCtrno))
					{
						ohl1.setEqNo(hlCtrno);
						stowPlanHold = populateTosStowPlanHoldMt(ohl1);
						stowPlanHoldId = populateTosStowPlanHoldMtId(ohl1);
						stowPlanHoldId.setTosStowPlanCntrMt(stowPlan);
						stowPlanHold.setId(stowPlanHoldId);
						setStowPlanHold.add(stowPlanHold);
					}
				}
				stowPlan.setTosStowPlanHoldMts(setStowPlanHold);

				for(int k=0; k<ochDataList.size(); k++)
				{
					OCHFields och1 = (OCHFields) ochDataList.get(k);
					String chCtrno = och1.getCtrNo();
					if(chCtrno!=null && chCtrno.length()>0)
					{
						if(!StringUtils.isAlpha(chCtrno.substring(0, 1)))
							chCtrno = "MATU" + chCtrno;
					}
					if(chCtrno!=null && ctrno.equalsIgnoreCase(chCtrno))
					{
						och1.setCtrNo(chCtrno);
						stowPlanChassis = populateTosStowPlanChassisMt(och1);
						stowPlanChassis.setTosStowPlanCntrMt(stowPlan);
						setStowPlanChassis.add(stowPlanChassis);
					}
				}
				stowPlan.setTosStowPlanChassisMts(setStowPlanChassis);
				for(int l=0; l<ohzDataList.size(); l++)
				{
					OHZFields ohz1 = (OHZFields) ohzDataList.get(l);
					String hzCtrno = ohz1.getCtrNo();
					if(hzCtrno!=null && hzCtrno.length()>0)
					{
						if(!StringUtils.isAlpha(hzCtrno.substring(0, 1)))
							hzCtrno = "MATU" + hzCtrno;
					}
					if(hzCtrno!=null && ctrno.equalsIgnoreCase(hzCtrno))
					{
						//System.out.println("Haz found for "+ohz1.getCtrNo());
						ohz1.setCtrNo(hzCtrno);
						stowPlanHaz = populateTosStowPlanHazMt(ohz1);
						//stowPlanHazId = populateTosStowPlanHazMtId(ohz1);
						stowPlanHaz.setTosStowPlanCntrMt(stowPlan);
						setStowPlanHaz.add(stowPlanHaz);
						stowPlan.setHazf("Y");
					}
				}
				stowPlan.setTosStowPlanHazMts(setStowPlanHaz);
				//NewVesselDao.insertOCRData(stowPlan);
				stowPlanDataList.add(stowPlan);
			}
		}
		for(int b=0; b<ochDataList.size(); b++)
		{
			OCHFields och1 = (OCHFields) ochDataList.get(b);
			String ctrno = och1.getCtrNo();
			int temp = 0;
			if("barge".equalsIgnoreCase(processType)){
				if(!(ondFileVesvoy+ondFileLeg).equalsIgnoreCase(och1.getVesvoy()+och1.getLeg()))
				{
					temp++;
					logger.info("Ignoring bare chassis **** "+och1.getChas()+"\t"+och1.getVesvoy()+"\t"+och1.getLeg());
				}
			}
			if (temp == 0) {
				if(ctrno == null || ctrno.equalsIgnoreCase(""))
				{
					TosStowPlanChassisMt bareCh = populateTosStowPlanChassisMt(och1);
					//NewVesselDao.insertOCHData(bareCh);
					stowBareChassisList.add(bareCh);
				}
			}
		}
		/*for(int l=0; l<ohzDataList.size(); l++)
		{
			OHZFields ohz1 = (OHZFields) ohzDataList.get(l);
			TosStowPlanHazMt stowPlanHaz = populateTosStowPlanHazMt(ohz1);
			TosStowPlanHazMtId stowPlanHazId = populateTosStowPlanHazMtId(ohz1);
			//stowPlanHazId.setTosStowPlanCntrMt(stowPlan);
			stowPlanHaz.setId(stowPlanHazId);
			stowHazDataList.add(stowPlanHaz);
		}*/
		NewVesselDao.insertOCRData(stowPlanDataList);
		NewVesselDao.insertOCHData(stowBareChassisList);
		//NewVesselDao.insertOHLData(stowPlanHoldList);
		//NewVesselDao.insertOHZData(stowHazDataList);
	}

	public enum EnumOCRFields
	{
		tranCode,date,time,user,pdlgId,ctrNo,dir,srv,vesvoy,hazf,odf,temp,seal,cell,typeCode,cWeight,discharge,
		dPort,commodity,owner,tare,tempUnit,bookNo,load,hgt,strength,consignee,shipper,cd,damage,chassis,pDisp,
		retPort,leg,ds,aei,longhaul,longhaulDir,comment,emptyFull,primaryCarrier,trade,ovzHeight,ovzLeft,ovzRight,
		ovzFront,ovzRear,cmisVent,multipleHazardous,imoClass,primaryCntrllr,temp2,temp2uom,psi
	}
	public enum EnumOCHFields
	{
		tranCode,date,time,user,pdlgId,chas,cd,cell,tare,vesvoy,leg,load,srv,dmg,typeCode,owner,ctrNo,mgNum,
		mgTare,mgCd,primaryCarrier,trade
	}
	public enum EnumOHZFields
	{
		tranCode,date,time,user,pdlgId,ctrNo,unNum,regs,hazClass,desc,vesvoy,leg
	}
	public enum EnumOHLFields
	{
		tranCode,date,time,user1,pdlgId,bill,eqNo,vesvoy,srv,code,pby,hType,iGate,oGate,rfs,desc,
		aDateFormat,aTimeFormat,cDateFormat,cTimeFormat,aFlag,user2,leg
	}
	public enum EnumOVRFields
	{
		tranCode,date,time,user,pdlgId,ctrNo,height,left,right,front,rear,vesvoy,leg
	}

	public enum EnumONDFields
	{
		tranCode,date,time,user,pdlgId,vesvoy,leg
	}
	
	private static String readFile(String file ) throws IOException {
	    BufferedReader reader = new BufferedReader( new FileReader (file));
	    String         line = null;
	    StringBuilder  stringBuilder = new StringBuilder();
	    String         ls = System.getProperty("line.separator");

	    while( ( line = reader.readLine() ) != null ) {
	        stringBuilder.append( line );
	     stringBuilder.append( ls );
	    }
	    reader.close();
	    return stringBuilder.toString();
	}
}
