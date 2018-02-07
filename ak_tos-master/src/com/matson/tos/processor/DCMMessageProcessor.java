package com.matson.tos.processor;

import com.matson.cas.erd.service.data.VesselVO;
import com.matson.cas.refdata.mapping.TosDcmMt;
import com.matson.cas.service.ftp.FtpBizException;
import com.matson.cas.service.ftp.FtpProxyGetterBiz;
import com.matson.cas.service.ftp.FtpProxyListBiz;
import com.matson.tos.dao.NewVesselDao;
import com.matson.tos.exception.NewVesselLogger;
import com.matson.tos.reports.USCGDCMGenerator;
import com.matson.tos.util.TosRefDataUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

/* This class processes the DCM file and persists the DCM data into TOS database.
 *
 * 	Srno	Date			AuthorName				Change Description
 * 	A1		09/26/2012		Raghu Pattangi			Initial creation
 *  A2		09/30/2012		Karthik Rajendran		Added DCM Data parsing logics
 *  A3		10/08/2012		Karthik Rajendran		processLine() changes - fields sizes should match DB field sizes,
 *  												changed consignee mapping, emergency telephone number mapping
 *  A4		10/18/2012		Karthik Rajendran		FTP code integration for DCM files.
 *  A5		01/19/2013		Karthik Rajendran		Added: DCM Data existence checking to avoid unique constraint exceptions
 *  A6		02/18/2013		Karthik Rajendran		Added: Container number prefix check and adding MATU if not present
 *  A7		03/20/2013		Karthik Rajendran		Changed: emergencyContact phone number should not get manipulated, get first 14 chars.
 *  A8		04/05/2013		Karthik Rajendran		Added: Sending DCM text to USCG after persisting DCM data.
 *  A9		04/08/2013		Karthik Rajendran		Added: Get consignee if shipperCnee is not available.
 *  A10		04/11/2013		Karthik Rajendran		Added: Skip empty lines while parsing.
 *  A11		10/03/2013		Karthik Rajendran		Added: validate DCM record before saving but while parsing
 *
 */


public class DCMMessageProcessor extends AbstractFileProcessor {

	private static Logger logger = Logger.getLogger(DCMMessageProcessor.class);
	//private String contents;
	private ArrayList<String> contentLines;
	public ArrayList<TosDcmMt> dcmDataList;
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
	public HashMap cntrMap = new HashMap();

	public DCMMessageProcessor()
	{
		processFiles();
		nvLogger.sendDcmDataErrors();
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
			String[] dcmFiles = list.getFileNames(ftpProxyId, null, null);
			/*if(dcmFiles==null || dcmFiles.length <= 0)
			{
				nvLogger.addFileError("", "No DCM file found in the FTP location.");
				throw new Exception("No DCM file found in the FTP location.");
			}*/

			logger.debug("ftpProxyId : " + ftpProxyId + "  and ftpProxyArchId : "+ ftpProxyArchId);
			logger.debug(  "Total number of DCM files "+ (dcmFiles != null ? dcmFiles.length : "0"));


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
					logger.debug("Processing file: " + ftpFileName);
					String contents = getter.getFileText(ftpProxyId, ftpFileName);
					String startWFTime  = (new SimpleDateFormat("MM-dd-yyyy_HH-mm-ss")).format(new Date()).toString();
					dcmDataList = new ArrayList<TosDcmMt>();
					isFileProcessed = false;
					processFile(contents);
					if(isFileProcessed) {
						CommonBusinessProcessor.archivetoFTP(contents, ftpProxyArchId, ftpFileName+"_"+startWFTime,null);
						CommonBusinessProcessor.deleteFtpFiles(ftpProxyId, ftpFileName,null);
					}
				}
			}
		} catch (FtpBizException ftpEx) {
			logger.error("FTP error found: ", ftpEx);
			nvLogger.addFtpError(""+ftpProxyId, "FTP ERROR: Unable to get into FTP");
		} catch (Exception e) {
			logger.error("Exception: ", e);
			e.printStackTrace();
		}
	}
	public static void main(String[] args)
	{
		//DCMMessageProcessor dcmp = new DCMMessageProcessor();
		//dcmp.setupDcmFieldsData();
		//dcmp.processFile(dcmFile);
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
		vesvoy = vesvoy.toUpperCase();
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
		hazDesc1 = hazDesc1.length() >= 300 ? hazDesc1.substring(0, 300):hazDesc1;
		emergencyContact = trimQuotes(fields.get(12));
		emergencyContact = emergencyContact.length() >= 35 ? emergencyContact.substring(0, 35):emergencyContact;
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
		shippingName = shippingName.length() >= 300 ? shippingName.substring(0, 300) : shippingName;
		techName = trimQuotes(fields.get(23));
		techName = techName.length() >= 300 ? techName.substring(0, 300) : techName;
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
		TosDcmMt dcm = new TosDcmMt();
		dcm.setVesvoy(vesvoy);
		dcm.setContainerNumber(ctrNo);
		logger.info("ctrNo in process line "+ctrNo);
		if (cntrMap.containsKey(ctrNo)) {
			cnseq = (Integer) cntrMap.get(ctrNo) + 1;
			cntrMap.put(ctrNo, cnseq);
		} else {
			cnseq = 1;
			cntrMap.put(ctrNo, cnseq);
		}
		dcm.setCnseq(new BigDecimal(cnseq));
		logger.info("CTRNO=" + ctrNo + ", CNSEQ=" + cnseq);
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
				dcm.setPackagingMarks(packagingGrpMarks);
			}
			//when the values are  other than 1,2,3 & I,II,III , skip setting an value/ null be auto inserted
		}
		dcm.setRemarks(remarks);
		dcm.setCreateUser("newves");
		dcm.setCreateDate(new Date());
		dcm.setLastUpdateUser("newves");
		dcm.setLastUpdateDate(new Date());
		//NewVesselDao.insertDCMData(dcm);

		if(validateRecord(dcm))
			dcmDataList.add(dcm);
		else {
			dcmDataList.add(dcm);
			errorLines = errorLines + line + "\n Fields with wrong/missing data --> " + errorFields + "\n\n";
		}
		errorFields = "";
	}
	private boolean validateRecord(TosDcmMt dcm) {
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
		String grosWt = "0";
		if(dcm.getGrossWeight()!=null && dcm.getGrossWeight().length()>0)
		{
			grosWt = new String(dcm.getGrossWeight());
			grosWt = grosWt.replace(".", "");
			if (!StringUtils.isNumeric(grosWt)) {
				errorFields = errorFields + "GrossWeight="+dcm.getGrossWeight()+", ";
				valid = false;
			}
		}
		if(dcm.getGrossWeight()!=null && dcm.getGrossWeight().length()>0 && (dcm.getGrossWgUnit()==null||StringUtils.isNumeric(dcm.getGrossWgUnit())))
		{
			errorFields = errorFields + "GrossWgUnit="+dcm.getGrossWgUnit()+", ";
			valid = false;
		}
		return valid;
	}
	private boolean saveDcmData(ArrayList<TosDcmMt> dcmDataList)
	{
		try{
			NewVesselDao.deleteDCMData(vesvoy);
		}catch(Exception e)
		{
			nvLogger.addError(vesvoy, "", "Unable to delete DCM data <br /><br />"+e);
			return false;
		}
		try {
			NewVesselDao.insertDCMData(dcmDataList);
		}catch(Exception e)
		{
			nvLogger.addFileError(ftpFileName, "Unable to persist the data into database. Check the database/file for errors.");
			return false;
		}
		// Send DCM to USCG
		VesselVO vvo = CommonBusinessProcessor.getVesselDetails(vesvoy.substring(0, 3));
		if (vvo!=null && vvo.getVessType() != null && "L".equalsIgnoreCase(vvo.getVessType())) {
			USCGDCMGenerator uscg = new USCGDCMGenerator(vesvoy, "INBOUND", false, USCGDCMGenerator.NV_HON);
			uscg.sendDcmToUSCG();
		}
		//
		return true;
	}
	public String trimQuotes( String value) {
		// Did not handle the single string case.
		if (value == null) return "";
		if (value.length() >= 1 && value.charAt(0) == '"')
			value = value.substring(0, value.length()).substring(1);
		if ( value.length() >= 1 && value.charAt( value.length()-1) == '"')
			value = value.substring(0, value.length()-1);
		return value.trim();
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
