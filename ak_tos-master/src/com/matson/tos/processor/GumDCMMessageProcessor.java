package com.matson.tos.processor;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.matson.cas.refdata.mapping.TosDcmMt;
import com.matson.cas.refdata.mapping.TosGumDcmMt;
import com.matson.cas.service.ftp.FtpBizException;
import com.matson.cas.service.ftp.FtpProxyGetterBiz;
import com.matson.cas.service.ftp.FtpProxyListBiz;
import com.matson.tos.dao.NewVesselDaoGum;
import com.matson.tos.exception.NewVesselLogger;
import com.matson.tos.reports.USCGDCMGenerator;
import com.matson.tos.util.TosRefDataUtil;

/*
 * 
 * #		Date			Author						Description
 * ---		-----------		------------------			---------------------
 * 1		03/20/2013		Karthik Rajendran			Class file created from HON Newves DCMMessageProcessor
 * 2		04/16/2013		Karthik Rajendran			Added:USCGDCMGenerator to send DCM file to USCG
 * 3		10/07/2013		Karthik Rajendran			Added: validate DCM record before saving but while parsing
 * 4		10/29/2013		Karthik Rajendran			Changed: Set Create_user,last_update_user to "gumnewves" 
 * 
 *
 */


public class GumDCMMessageProcessor extends AbstractFileProcessor {
	
	private static Logger logger = Logger.getLogger(GumDCMMessageProcessor.class);
	//private String contents;
	private ArrayList<String> contentLines;
	public ArrayList<TosGumDcmMt> dcmDataList;
	public String vesvoy;
	public static String processType = null;
	private NewVesselLogger nvLogger = NewVesselLogger.getInstance();;
	String ftpFileName = null;
	int ftpProxyId = -1;
	int ftpProxyArchId = -1;
	boolean isFileProcessed = false;
	public String prevCtrno = null;
	public int cnseq = -1;
	private String errorFields = null;
	private String errorLines = null;
	private String loadPort = null;
	
	public GumDCMMessageProcessor()
	{
		processFiles();
		nvLogger.sendGumDcmDataErrors();
		nvLogger.sendNewVessErrors();
	}
	public void processFiles() {
		try {
			ftpProxyId = Integer.parseInt(TosRefDataUtil.getValue("DCM_INS_FTP_ID"));
			ftpProxyArchId = Integer.parseInt(TosRefDataUtil.getValue("DCM_ARCH_FTP_ID"));
			int timeout = Integer.parseInt(TosRefDataUtil.getValue("FTP_TIMEOUT"));
			logger.debug("FTP timeout retrieved is: " + timeout);
			FtpProxyListBiz list = new FtpProxyListBiz();
			list.setTimeout(timeout);
			String[] dcmFiles = list.getFileNames(ftpProxyId, "gumdcm", null);
			if(dcmFiles!=null && dcmFiles.length > 0)
			{
				FtpProxyGetterBiz getter = new FtpProxyGetterBiz();
				getter.setTimeout(timeout);
				for(int i=0; i<dcmFiles.length; i++)
				{
					ftpFileName = dcmFiles[i];
					if (-1 == ftpFileName.indexOf(".")) {
						continue;
					}
					logger.info("Processing file: " + ftpFileName);
					String contents = getter.getFileText(ftpProxyId, ftpFileName, "gumdcm");
					String startWFTime  = (new SimpleDateFormat("MM-dd-yyyy_HH-mm-ss")).format(new Date()).toString();
					dcmDataList = new ArrayList<TosGumDcmMt>();
					isFileProcessed = false;
					processFile(contents);
					if(isFileProcessed) {
						CommonBusinessProcessor.archivetoFTP(contents, ftpProxyArchId, ftpFileName+"_"+startWFTime,"gumdcm");
						CommonBusinessProcessor.deleteFtpFiles(ftpProxyId, ftpFileName,"gumdcm");
					}
				}
			}
		} catch (FtpBizException ftpEx) {
			ftpEx.printStackTrace();
			logger.error("FTP error found: ", ftpEx);
			nvLogger.addFtpError(""+ftpProxyId, "FTP ERROR: Unable to get into FTP");
		} catch (Exception e) {
			logger.error("Exception: ", e);
			e.printStackTrace();
		}
	}
	public static void main(String[] args)
	{

	}
	
	public void processFile(String contents)
	{
		contentLines = new ArrayList<String> (Arrays.asList(contents.split("\n")));
		if(contentLines.size()>0)
		{
			prevCtrno = "";
			cnseq = 1;
			errorFields = "";
			errorLines = "";
			for (int i=2; i<contentLines.size(); i++) {
				try {
					processLine(contentLines.get(i).toString(),0);
				}catch(Exception e) {
					//nvLogger.addFileError(ftpFileName, "Unable to parse the text. Line "+i+"\n\n"+ contentLines.get(i).toString());
				}
			}
			if(errorLines.length()>0) {
				errorLines = "\n\n" + errorLines + "\n\n";
				logger.info("Errors found for --> " + ftpFileName+","+loadPort);
				nvLogger.addDcmDataError(ftpFileName+","+loadPort, "<pre>DCM Parsing errors on the following line(s): please fix and process them again." + errorLines + "</pre>");
				// Clear vars
				errorFields = "";
				errorLines = "";
			}
			isFileProcessed = saveDcmData(dcmDataList);
			prevCtrno = null;
			cnseq = -1;
		}else{ 
			logger.error("Error: No data.");
			nvLogger.addFileError(ftpFileName, "Error: No data. Check file for errors.");
		}
	}
	public void processLine(String line,int lineNum)
	{
		if(line==null || line.length()<=0)
			return;
		String vesvoy,ctrNo,cnSeq,blankData1,checkDigit,hazClass,hazSubClass,shipperCnee,pack,blankData2;
		String hazDesc1,emergencyContact,grossWeightUnit,cellLocation,destPort,carsFlag,shipment,hazCode;
		String hazCodeType,hazRegQualifier,shippingName,techName,grossWeight,grossUnit,flashPoint,packagingGrpMarks;
		String remarks,consignee;
		ArrayList<String> fields = new ArrayList<String> (Arrays.asList(line.split("\",\"")));
		vesvoy = trimQuotes(fields.get(0));
		vesvoy = vesvoy.length() >= 7 ? vesvoy.substring(0, 7):vesvoy;
		this.vesvoy = vesvoy;
		ctrNo = trimQuotes(fields.get(1));
		if(ctrNo.length()>0)
		{
		    if(!StringUtils.isAlpha(ctrNo.substring(0, 1)))
			ctrNo = "MATU" + ctrNo;
		}
		ctrNo = ctrNo.length() >= 12 ? ctrNo.substring(0, 12):ctrNo;
		//cnSeq = trimQuotes(fields.get(2));
		blankData1 = trimQuotes(fields.get(3));
		blankData1 = blankData1.length() >= 1 ? blankData1.substring(0, 1):blankData1;
		checkDigit = trimQuotes(fields.get(4));
		checkDigit = checkDigit.length() >= 1 ? checkDigit.substring(0, 1):checkDigit;
		hazClass = trimQuotes(fields.get(5));
		hazClass = hazClass.length() >= 4 ? hazClass.substring(0, 4):hazClass;
		hazSubClass = trimQuotes(fields.get(7));
		hazSubClass = hazSubClass.length() >= 20 ? hazSubClass.substring(0, 20):hazSubClass;
		shipperCnee = trimQuotes(fields.get(8));
		shipperCnee = shipperCnee.length() >= 30 ? shipperCnee.substring(0, 30):shipperCnee;
		pack = trimQuotes(fields.get(9));
		if (pack!=null && pack.length()>10) {
			pack = pack.substring(0,10);//changed by Meena
		}
		blankData2 = trimQuotes(fields.get(10));
		blankData2 = blankData2.length() >= 3 ? blankData2.substring(0, 3):blankData2;
		hazDesc1 = trimQuotes(fields.get(11));
		hazDesc1 = hazDesc1.length() >= 255 ? hazDesc1.substring(0, 255):hazDesc1;
		emergencyContact = trimQuotes(fields.get(12));
		emergencyContact = emergencyContact.length() >= 14 ? emergencyContact.substring(0, 14):emergencyContact;
		grossWeightUnit = trimQuotes(fields.get(13));
		grossWeightUnit = grossWeightUnit.length() >= 10 ? grossWeightUnit.substring(0, 10):grossWeightUnit;
		cellLocation = trimQuotes(fields.get(14));
		cellLocation = cellLocation.length() >= 6 ? cellLocation.substring(0, 6):cellLocation;
		destPort = trimQuotes(fields.get(15));
		destPort = destPort.length() >= 4 ? destPort.substring(0, 4):destPort;
		carsFlag = trimQuotes(fields.get(17));
		carsFlag = carsFlag.length() >= 1 ? carsFlag.substring(0, 1):carsFlag;
		shipment = trimQuotes(fields.get(18));
		shipment = shipment.length() >= 10 ? shipment.substring(0, 10):shipment;
		hazCode = trimQuotes(fields.get(19));
		hazCode = hazCode.length() >= 4 ? hazCode.substring(0, 4):hazCode;
		hazCodeType = trimQuotes(fields.get(20));
		hazCodeType = hazCodeType.length() >= 2 ? hazCodeType.substring(0, 2):hazCodeType;
		hazRegQualifier = trimQuotes(fields.get(21));
		hazRegQualifier = hazRegQualifier.length() >= 1 ? hazRegQualifier.substring(0, 1):hazRegQualifier;
		shippingName = trimQuotes(fields.get(22));
		shippingName = shippingName.length() >= 140 ? shippingName.substring(0, 140):shippingName;
		techName = trimQuotes(fields.get(23));
		techName = techName.length() >= 140 ? techName.substring(0, 140):techName;
		grossWeight = trimQuotes(fields.get(24));
		grossWeight = grossWeight.length() >= 6 ? grossWeight.substring(0, 6):grossWeight;
		grossUnit = trimQuotes(fields.get(25));
		grossUnit = grossUnit.length() >= 2 ? grossUnit.substring(0, 2):grossUnit;
		flashPoint = trimQuotes(fields.get(26));
		flashPoint = flashPoint.length() >= 15 ? flashPoint.substring(0, 15):flashPoint;
		packagingGrpMarks = trimQuotes(fields.get(27));
		packagingGrpMarks = packagingGrpMarks.length() >= 30 ? packagingGrpMarks.substring(0, 30):packagingGrpMarks;
		remarks = trimQuotes(fields.get(28));
		remarks = remarks.length() >= 60 ? remarks.substring(0, 60):remarks;
		loadPort = trimQuotes(fields.get(30));
		consignee = trimQuotes(fields.get(31));
		consignee = consignee.length() >= 30 ? consignee.substring(0, 30):consignee;
		//
		TosGumDcmMt dcm = new TosGumDcmMt();
		dcm.setVesvoy(vesvoy);
		dcm.setContainerNumber(ctrNo);
		if(prevCtrno.equals(ctrNo)) {
			cnseq += 1;
		} else {
			prevCtrno = ctrNo;
			cnseq = 1;
		}
		dcm.setCnseq(new BigDecimal(cnseq));
		//logger.info("CTRNO="+ctrNo+", CNSEQ="+cnseq);
		dcm.setV(blankData1);
		dcm.setCheckDigit(checkDigit);
		dcm.setHazClass(hazClass);
		dcm.setSubClass(hazSubClass);
		if(consignee!=null && consignee.length()>0)
			dcm.setShipperCnee(consignee);
		else
			dcm.setShipperCnee(shipperCnee);
		dcm.setPackNum(pack);
		dcm.setDs(blankData2);
		dcm.setHazDesc1(hazDesc1);
		dcm.setPhone(emergencyContact);
		dcm.setGrosswgt(grossWeightUnit);
		dcm.setCell(cellLocation);
		dcm.setDport(destPort);
		dcm.setCarf(carsFlag);
		dcm.setShipmentNumber(shipment);
		dcm.setHazardCode(hazCode);
		dcm.setHazardCodeType(hazCodeType);
		dcm.setHazardRegQualifier(hazRegQualifier);
		dcm.setShippingName(shippingName);
		dcm.setTechnicalName(techName);
		dcm.setGrossWeight(grossWeight);
		dcm.setGrossWgUnit(grossUnit);
		dcm.setFlashPoint(flashPoint);
		dcm.setPackagingMarks(packagingGrpMarks);
		dcm.setRemarks(remarks);
		dcm.setCreateUser("gumnewves");
		dcm.setCreateDate(new Date());
		dcm.setLastUpdateUser("gumnewves");
		dcm.setLastUpdateDate(new Date());
		if(validateRecord(dcm))
			dcmDataList.add(dcm);
		else
			errorLines = errorLines + line + "\n Fields with wrong/missing data --> " + errorFields + "\n\n";
		errorFields = "";
	}
	private boolean validateRecord(TosGumDcmMt dcm) {
		boolean valid = true;
		if(dcm.getVesvoy()==null || dcm.getVesvoy().length()<6) {
			errorFields = errorFields + "Vesvoy="+dcm.getVesvoy()+", ";
			valid = false;
		}
		if(dcm.getContainerNumber()==null || dcm.getContainerNumber().length()<6) {
			errorFields = errorFields + "ContainerNumber="+dcm.getContainerNumber()+", ";
			valid = false;
		}
		/*if(dcm.getGrosswgt()!=null && dcm.getGrosswgt().length()>0 && !dcm.getGrosswgt().endsWith("LB") && !dcm.getGrosswgt().endsWith("KG") && 
				!dcm.getGrosswgt().endsWith("LBS")) {
			errorFields = errorFields + "Grosswgt="+dcm.getGrosswgt()+", ";
			valid = false;
		}*/
		if(dcm.getGrossWeight()!=null && dcm.getGrossWeight().length()>0 && !StringUtils.isNumeric(dcm.getGrossWeight()))
		{
			errorFields = errorFields + "GrossWeight="+dcm.getGrossWeight()+", ";
			valid = false;
		}
		if(dcm.getGrossWeight()!=null && dcm.getGrossWeight().length()>0 && (dcm.getGrossWgUnit()==null||StringUtils.isNumeric(dcm.getGrossWgUnit())))
		{
			errorFields = errorFields + "GrossWgUnit="+dcm.getGrossWgUnit()+", ";
			valid = false;
		}
		
		return valid;
	}
	private boolean saveDcmData(ArrayList<TosGumDcmMt> dcmDataList)
	{
		try{
			NewVesselDaoGum.deleteDCMData(vesvoy);
		}catch(Exception e)
		{
			nvLogger.addError(vesvoy, "", "Unable to delete DCM data <br /><br />"+e);
			return false;
		}
		logger.info("Saving DCM data....");
		try {
			NewVesselDaoGum.insertDCMData(dcmDataList);
		}catch(Exception e)
		{
			nvLogger.addFileError(ftpFileName, "Unable to persist the data into database. Check the database/file for errors.");
		}
		// Send DCM to USCG
		USCGDCMGenerator uscg = new USCGDCMGenerator(vesvoy, "INBOUND", false, USCGDCMGenerator.NV_GUM);
		uscg.sendDcmToUSCG();
		//
		return true;
	}
	public String trimQuotes( String value) {
		// Did not handle the single string case.
		if (value == null) return "";
		if (value.length() >= 1 && value.charAt(0) == '"')
			value = value.substring(0, value.length()).substring(1).trim();
		if ( value.length() >= 1 && value.charAt( value.length()-1) == '"')
			value = value.substring(0, value.length()-1);
		return value;
	}
	public String getDigits(String inStr)
	{
		String digits = inStr.replaceAll("[^0-9]", "");
		return digits;
	}
	private boolean validatePhoneNum(String phone) {
		ArrayList<String> patterns = new ArrayList<String>();
		patterns.add("\\d{9,10}\\s*\\w*\\D*");
		patterns.add("\\d{10,11}\\s*\\D*");
		patterns.add("\\d{3}(-|\\(|\\)|\\s*)\\d{3}(-|\\(|\\)|\\s*)\\d{4}\\s*\\D*");
		patterns.add("\\d{3}(-|\\(|\\)|\\s*)\\d{7}\\s*\\D*");
		patterns.add("\\d{1}(-|\\(|\\)|\\s*)\\d{3}(-|\\(|\\)|\\s*)\\d{7}\\s*\\D*");
		patterns.add("\\d{1}(-|\\(|\\)|\\s*)\\d{3}(-|\\(|\\)|\\s*)\\d{3,4}(-|\\(|\\)|\\s*)\\d{3,4}\\s*\\D*");
		for(String pattern: patterns) {
			if(phone.matches(pattern))
				return true;
		}
		return false;
	}
}
