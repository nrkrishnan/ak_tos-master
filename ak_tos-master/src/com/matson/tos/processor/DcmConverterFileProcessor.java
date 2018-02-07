package com.matson.tos.processor;

import java.io.StringWriter;
import java.util.ArrayList;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.log4j.Logger;

import com.matson.tos.exception.TosException;
import com.matson.tos.messageHandler.AbstractMessageHandler;
import com.matson.tos.messageHandler.DcmConverterMessageHandler;
import com.matson.tos.messageHandler.DcmUscgConvtMessageHandler;
import com.matson.tos.util.DcmFormatter;
import com.matson.tos.util.TosRefDataUtil;
import com.matson.cas.service.ftp.FtpBizException;
import com.matson.cas.service.ftp.FtpProxyDeleterBiz;
import com.matson.cas.service.ftp.FtpProxyGetterBiz;
import com.matson.cas.service.ftp.FtpProxyListBiz;
import com.matson.cas.service.ftp.FtpProxySenderBiz;
import com.matson.tos.util.EmailSender;
import com.matson.tos.constants.VesselCode;
import com.matson.tos.constants.PortCode;
import com.matson.tos.constants.PortName;

/**
 * A1 - 5/8/2009 Added from and to port. A2 - 06/21/2011 Read Haz insert File A3
 * - 07/15/11 Change to Pickup Haz Insert files
 * 
 * @author xw8
 * 
 */
public class DcmConverterFileProcessor extends AbstractFileProcessor {

	private static Logger logger = Logger
			.getLogger(DcmConverterFileProcessor.class);
	private DcmConverterMessageHandler msgHandler = null;
	private DcmUscgConvtMessageHandler uscgMsghandler = null;
	private String MAIL_HOST = TosRefDataUtil.getValue("MAIL_HOST");
	private String emailAddr = TosRefDataUtil.getValue("SUPPORT_EMAIL");
	private String errorEmailAddr = TosRefDataUtil
			.getValue("DCM_SUPPORT_EMAIL");
	private String EMAIL_DCM_GM = TosRefDataUtil.getValue("EMAIL_DCM_GM");

	private StringWriter out;
	private StringWriter outUscg;
	// DCM File Versions
	public static final String DCM_VER_FINAL = "FINAL";
	public static final String DCM_VER_PRELIMINARY = "PRELIMINARY";
	public static final String DCM_VER_LATE = "Late";
	public static final String HAZARD_FILE = "HAZARD.TXT";
	public static final String HAZ_FAILED = "HAZ_FAILED.csv";
	// Vessel Details
	private String vesselCode = null;
	private String voyageNumber = null;
	private String dcmVersion = null;
	private String vesselBoundFor = null;
	private String vesselPort = null;
	private String vesselName = null;
	private String vesselFrom = null;

	public DcmConverterFileProcessor() {
		try {
			msgHandler = new DcmConverterMessageHandler("com.matson.tos.jatb",
					"/xml/dcmConvert.xml", AbstractMessageHandler.TEXT_TO_TEXT);

			uscgMsghandler = new DcmUscgConvtMessageHandler(
					"com.matson.tos.jatb", "/xml/dcmConvert.xml",
					AbstractMessageHandler.TEXT_TO_TEXT);

		} catch (TosException tex) {
			logger.error("Error in creating the object: ", tex);
		}
	}

	public void processFiles() {
		// TODO Auto-generated method stub
		String ftpFileName = null;

		try {
			int ftpProxyId = Integer.parseInt(TosRefDataUtil
					.getValue("DCM_IN_FTP_ID"));
			int ftpHazProxyId = Integer.parseInt(TosRefDataUtil
					.getValue("HAZ_INS_FTP_ID"));
			int timeout = Integer.parseInt(TosRefDataUtil
					.getValue("FTP_TIMEOUT"));
			logger.debug("FTP timeout retrieved is: " + timeout);
			FtpProxyListBiz list = new FtpProxyListBiz();
			list.setTimeout(timeout);
			String[] files = list.getFileNames(ftpProxyId, null, null);
			String[] hazFiles = list.getFileNames(ftpHazProxyId, null, null);
			logger.debug("Total Dcm " + (files != null ? files.length : "0")
					+ "  Total Haz Insert "
					+ (hazFiles != null ? hazFiles.length : "0"));
			FtpProxyGetterBiz getter = new FtpProxyGetterBiz();
			getter.setTimeout(timeout);
			// FTPFile aFile = null;

			// A1 Change to Import late DCM file
			for (int i = 0; files != null && i < files.length; i++) {
				boolean processed = false;
				ftpFileName = files[i];
				logger.debug("Processing file: " + ftpFileName);
				String contents = getter.getFileText(ftpProxyId, ftpFileName);
				// Processing For DCM Late File
				if (ftpFileName.toUpperCase().endsWith("LT.TXT")) {// A2
					DcmLateFileProcessor dcmLateFileProcessor = new DcmLateFileProcessor(
							DCM_VER_LATE);
					dcmLateFileProcessor.setFileName(ftpFileName.toUpperCase()); // A2
					dcmLateFileProcessor.processFile(contents);
					processed = true;
					logger.debug("Processed DCM Late file Sucessfully : "
							+ ftpFileName);
				} else if (!ftpFileName.toUpperCase().endsWith("LT.TXT")) {// A2
					processFile(contents);
					processed = true;
					logger.debug("Processed DCM Report file Sucessfully : "
							+ ftpFileName);
					String version = dcmVersion == null ? "Final" : dcmVersion;
					DcmLateFileProcessor dcmLateFileProcessor = new DcmLateFileProcessor(
							dcmVersion);
					dcmLateFileProcessor.emailFile(contents);
				} else {
					logger.debug("The file name: "
							+ ftpFileName
							+ " is not DCM Report Conversion file and will be ignored.");
				}
				if (processed) {
					// remove file that has been processed
					logger.debug("Deleting file: " + ftpFileName);
					FtpProxyDeleterBiz del = new FtpProxyDeleterBiz();
					del.setTimeout(timeout);
					del.removeFile(ftpProxyId, ftpFileName);
				}

			}// A5 Change to Include Haz Insert from a spreadsheet
			for (int i = 0; hazFiles != null && i < hazFiles.length; i++) {
				boolean processed = false;
				ftpFileName = hazFiles[i];
				logger.debug("Processing Haz Insert file: " + ftpFileName);
				String contents = getter
						.getFileText(ftpHazProxyId, ftpFileName);
				// Processing For DCM Late File
				if (ftpFileName.toUpperCase().endsWith("INS.TXT")) {// A2
					DcmLateFileProcessor dcmLateFileProcessor = new DcmLateFileProcessor(
							DCM_VER_LATE);
					dcmLateFileProcessor.setFileName(ftpFileName.toUpperCase()); // A2
					dcmLateFileProcessor.processFile(contents);
					processed = true;
					logger.debug("Processed Haz Insert Sucessfully : "
							+ ftpFileName);
				} else {
					logger.debug("The file name: "
							+ ftpFileName
							+ " is not Haz DCM Report Conversion file and will be ignored.");
				}
				if (processed) {
					// remove file that has been processed
					logger.debug("Deleting file: " + ftpFileName);
					FtpProxyDeleterBiz del = new FtpProxyDeleterBiz();
					del.setTimeout(timeout);
					del.removeFile(ftpHazProxyId, ftpFileName);
				}

			}

		} catch (FtpBizException ftpEx) {
			logger.error("FTP error found: ", ftpEx);
		} catch (Exception e) {
			logger.error("Exception: ", e);
			e.printStackTrace();
		}
	}

	protected void processFile(String contents) {
		logger.debug("Class: DcmConverterFileProcessor - Method: processFile");
		try {
			String[] lines = contents.split("\n");
			logger.debug("There are " + lines.length + " lines in the file.");
			if (lines[1] != null) {
				// Fetches Vessel Information
				vesselInformation(lines[1]);
			}
			// Writes data to a StringBuffer
			out = new StringWriter();
			// Processes File Header
			processFileHeader(lines);

			for (int i = 2; i < lines.length; i++) {
				processLine(lines[i], i + 1); // Processing for Haz viewer
			}
			if (DCM_VER_FINAL.equalsIgnoreCase(dcmVersion)) {
				outUscg.close();
			}
			out.close();
			// Conditional Mailing and FTPing Process
			processMailDistribution(out.toString());
			if (DCM_VER_FINAL.equalsIgnoreCase(dcmVersion) && vesselBoundFor!=null && vesselBoundFor.equalsIgnoreCase("Guam")) {
				sendGuamDcmToFtp(out.toString());
			}
		} catch (Exception ex) {
			logger.error("Error in text message converting: ", ex);
			// EMail to Support Group if DCM Fails
			EmailSender.mailAttachment(errorEmailAddr, emailAddr, MAIL_HOST,
					HAZ_FAILED, contents,
					"Please check the DCM hazard file data.\nException Detail\n"
							+ ex, "Dcm Hazard file did not Process");
		} finally {
			vesselCode = null;
			voyageNumber = null;
			dcmVersion = null;
			vesselBoundFor = null;
			vesselPort = null;
			vesselName = null;
			vesselFrom = null;
		}
	}

	protected void processLine(String msg, int lineNum) throws TosException {
		try {
			if (msg.trim().length() < 3) {
				logger.debug("Empty line with length " + msg.trim().length());
				return;
			}
			if (DCM_VER_FINAL.equalsIgnoreCase(dcmVersion)) {
				// USCG record Processing
				uscgMsghandler.setTextStr(msg);
				String strOut = uscgMsghandler.getTextStr();
				outUscg.write(strOut);
				outUscg.write("\r\n\n");
			}
			// Data record Processing
			msgHandler.setTextStr(msg);
			String strOut = msgHandler.getTextStr();
			strOut = DcmFormatter.removeLastComma(strOut);
			out.write(strOut);
			out.write("\r\n");
		} catch (TosException ex) {
			logger.error("Error Line Num = " + lineNum + " Line Data = " + msg);
			logger.error("Error in text message converting: ", ex);
			throw new TosException("Error in text converting " + ex
					+ " The error line num = " + lineNum);
		}
	}

	/*
	 * Method formats the header
	 */
	private void processFileHeader(String[] linesData) {
		String formatedHeader = null;
		String version = TosRefDataUtil.getValue("DCM_VERSION");
		logger.debug("TOS_APP_PARAMETER - DCM_VERSION : "+version);
		version = version==null?"2.0":version;
		// Reads and Writes the Header rows
		for (int rowNum = 0; rowNum < 2; rowNum++) {
			if(rowNum == 0)
				formatedHeader = DcmFormatter.formatHeaderTextWithVersion(linesData[rowNum], version);
			else
				formatedHeader = DcmFormatter.formatHeaderText(linesData[rowNum]);
			// Appending extra Quotes to header
			if (rowNum == 0) {
				formatedHeader = DcmFormatter.addHeaderQuotes(formatedHeader);
			}
			logger.debug("ProcessFile formatedHeader :: " + formatedHeader);
			out.write(formatedHeader);
			out.write("\r\n");
		}// Appends a "No Container" record entry for an empty DCM file
		if (linesData.length == 2) {
			String noContainer = DcmFormatter.noContainersFormat(vesselCode,
					voyageNumber);
			out.write(noContainer);
			out.write("\r\n");
		}
		if (DCM_VER_FINAL.equalsIgnoreCase(dcmVersion)) {
			// USCG Record Processing
			outUscg = new StringWriter();
			String header = DcmFormatter.formatUscgHeader(linesData[1]);
			outUscg.write(header);
			outUscg.write("\r\n");
		}
	}

	/*
	 * Method vesselInformation retrieves vessel details
	 */
	private void vesselInformation(String headerInfo) throws TosException {
		String[] vesDetails = headerInfo.split(",");
		vesselName = vesDetails[0];
		vesselCode = verifyVesselCode(vesDetails[1]);
		voyageNumber = vesDetails[2];
		vesselBoundFor = vesDetails[7];
		vesselFrom = vesDetails[6];
		dcmVersion = vesDetails[10];
		// Gets Port code
		if (vesselBoundFor != null)
			boundPortCode(vesselBoundFor);

		logger.debug("vesselCode : " + vesselCode + "\n voyageNumber : "
				+ voyageNumber + "\n vesselBoundFor : " + vesselBoundFor
				+ "\n dcmVersion : " + dcmVersion);
	}

	/*
	 * Method boundPortCode gets Port Code for Barge Vessel
	 */
	private void boundPortCode(String boundPort) {
		if (PortName.PORT_HIL.equalsIgnoreCase(boundPort)) {
			vesselPort = PortCode.PORT_HIL;
		} else if (PortName.PORT_KAH.equalsIgnoreCase(boundPort)) {
			vesselPort = PortCode.PORT_KAH;
		} else if (PortName.PORT_KHI.equalsIgnoreCase(boundPort)) {
			vesselPort = PortCode.PORT_KHI;
		} else if (PortName.PORT_NAW.equalsIgnoreCase(boundPort)) {
			vesselPort = PortCode.PORT_NAW;
		} else if (PortName.PORT_LNI.equalsIgnoreCase(boundPort)) {
			vesselPort = PortCode.PORT_LNI;
		} else if (PortName.PORT_MOL.equalsIgnoreCase(boundPort)) {
			vesselPort = PortCode.PORT_MOL;
		}
		logger.debug("Class: DcmConverterFileProcessor - Method: boundPortCode : "
				+ boundPort);
	}

	/*
	 * Method Verify's Vessel Code
	 */
	public String verifyVesselCode(String vesCode) throws TosException {
		String strVesselCode = null;

		if (VesselCode.VES_KAU.equalsIgnoreCase(vesCode))
			strVesselCode = VesselCode.VES_KAU;
		else if (VesselCode.VES_LHE.equalsIgnoreCase(vesCode))
			strVesselCode = VesselCode.VES_LHE;
		else if (VesselCode.VES_LUR.equalsIgnoreCase(vesCode))
			strVesselCode = VesselCode.VES_LUR;
		else if (VesselCode.VES_MAT.equalsIgnoreCase(vesCode))
			strVesselCode = VesselCode.VES_MAT;
		else if (VesselCode.VES_MHI.equalsIgnoreCase(vesCode))
			strVesselCode = VesselCode.VES_MHI;
		else if (VesselCode.VES_MKA.equalsIgnoreCase(vesCode))
			strVesselCode = VesselCode.VES_MKA;
		else if (VesselCode.VES_MKI.equalsIgnoreCase(vesCode))
			strVesselCode = VesselCode.VES_MKI;
		else if (VesselCode.VES_MLE.equalsIgnoreCase(vesCode))
			strVesselCode = VesselCode.VES_MLE;
		else if (VesselCode.VES_MLI.equalsIgnoreCase(vesCode))
			strVesselCode = VesselCode.VES_MLI;
		else if (VesselCode.VES_MNA.equalsIgnoreCase(vesCode))
			strVesselCode = VesselCode.VES_MNA;
		else if (VesselCode.VES_MWI.equalsIgnoreCase(vesCode))
			strVesselCode = VesselCode.VES_MWI;
		else if (VesselCode.VES_RJP.equalsIgnoreCase(vesCode))
			strVesselCode = VesselCode.VES_RJP;
		else if (VesselCode.VES_ALE.equalsIgnoreCase(vesCode))
			strVesselCode = VesselCode.VES_ALE;
		else if (VesselCode.VES_LOA.equalsIgnoreCase(vesCode))
			strVesselCode = VesselCode.VES_LOA;
		else if (VesselCode.VES_HAL.equalsIgnoreCase(vesCode))
			strVesselCode = VesselCode.VES_HAL;
		else if (VesselCode.VES_MAU.equalsIgnoreCase(vesCode))
			strVesselCode = VesselCode.VES_MAU;
		else
			throw new TosException("Vessel code Incorrect");

		return strVesselCode;
	}

	/*
	 * Method MailAttachementToFTP Stores the Attachment file to the FTP server
	 */
	private void mailAttachementToFTP(String fileContent) throws TosException {
		int FTP_DCM_REP = Integer.parseInt(TosRefDataUtil
				.getValue("FTP_DCM_REP"));
		int DCM_OUT_FTP_ID = 0;

		try {
			FtpProxySenderBiz sender = new FtpProxySenderBiz();
			logger.debug("Before setting the connection mode to passive");
			sender.setPassiveConnectionMode(true);
			logger.debug("After setting the connection mode to passive");
			sender.setCreateSubDirectory(true);
			int timeout = Integer.parseInt(TosRefDataUtil
					.getValue("FTP_TIMEOUT"));
			logger.debug("FTP timeout retrieved is: " + timeout);
			sender.setTimeout(timeout);
			String fileName = "HAZ" + vesselCode + ".TXT";

			// Checks DCM version and Vessel Type
			if (DCM_VER_FINAL.equals(dcmVersion) && vesselPort != null) {
				// Store to FTP DCM Repository
				// A5 - sender.sendFile(FTP_DCM_REP, fileName, fileContent);
				sender.sendFile(FTP_DCM_REP, fileName, fileContent.getBytes(),
						true, null);
				logger.debug("Method:mailAttachementToFTP - FINAL TO DCM(BARGE) : "
						+ fileName);
			} else if (DCM_VER_FINAL.equals(dcmVersion) && vesselPort == null) {
				// Store to FTP DCM Repository & Vessel_Access
				DCM_OUT_FTP_ID = Integer.parseInt(TosRefDataUtil
						.getValue("DCM_OUT_FTP_ID"));
				// A5 - sender.sendFile(FTP_DCM_REP, fileName, fileContent);
				sender.sendFile(FTP_DCM_REP, fileName, fileContent.getBytes(),
						true, null);
				// sendFile(<FTP ID>, <FileName>, <FileData>, <SubDirectory>)
				// A5 - sender.sendFile(DCM_OUT_FTP_ID, fileName, fileContent,
				// vesselCode);
				sender.sendFile(DCM_OUT_FTP_ID, fileName,
						fileContent.getBytes(), true, vesselCode);
				logger.debug("Method:mailAttachementToFTP - FINAL TO DCM & VESSEL ACCESS (LONG HAUL): "
						+ fileName);
			} else if (DCM_VER_PRELIMINARY.equals(dcmVersion)
					&& vesselPort == null) {
				// Store to FTP Vessel_Access
				DCM_OUT_FTP_ID = Integer.parseInt(TosRefDataUtil
						.getValue("DCM_OUT_FTP_ID"));
				// A5 - sender.sendFile(DCM_OUT_FTP_ID, fileName, fileContent,
				// vesselCode);
				sender.sendFile(DCM_OUT_FTP_ID, fileName,
						fileContent.getBytes(), true, vesselCode);
				logger.debug("Method:mailAttachementToFTP - PRELIMINARY FILE TO VESSEL ACCESS (LONG HAUL): "
						+ fileName);
			} else {
				logger.debug("PRELIMINARY BARGE FILE DO NOT FTP : " + fileName);
			}
		} catch (FtpBizException ex) {
			logger.error("Error in Sending FTP file: ", ex);
			throw new TosException("Exception in sending file to FTP Server");
		}

	}

	/**
	 * Get email list for Vessel and Next Port
	 * 
	 * @return
	 */
	private ArrayList<String> emailInformationForVessel() {
		ArrayList<String> vesselData = new ArrayList<String>();
		StringBuffer strBuffer = new StringBuffer();
		String subject = null;
		String mailBody = null;
		String fileName = null;
		// Email Subject
		if ((vesselPort != null)) {// BARGE Type
			subject = vesselCode + voyageNumber + " DCM " + dcmVersion
					+ " File for " + vesselPort;
			// Mail Body Text for BARGE Mailing Type
			mailBody = "Attached is the "
					+ dcmVersion
					+ " "
					+ vesselCode
					+ voyageNumber
					+ " DCM Text File.\n"
					+ "Save the file to C:\\HAZARD TEXT.\nUse HAZVIEW Program to Import file and print the DCM.";
			fileName = HAZARD_FILE;
			String EMAIL_VES_BARGE = TosRefDataUtil.getValue("EMAIL_DCM_"
					+ vesselPort);
			String EMAIL_DCM_BMA = TosRefDataUtil.getValue("EMAIL_DCM_BMA");
			// Append Barge Mailing list + General mailing list + Barge Mail
			// Additions
			strBuffer.append(EMAIL_VES_BARGE + ";");
			strBuffer.append(EMAIL_DCM_GM + ";");
			strBuffer.append(EMAIL_DCM_BMA);

		} else {// Long Haul Type
			subject = "DCM " + dcmVersion + " Text File for " + vesselCode
					+ voyageNumber;
			mailBody = "Attached is the "
					+ vesselCode
					+ voyageNumber
					+ " DCM Text File.\n"
					+ "Save the file to C:\\HAZARD TEXT.\nUse HAZVIEW Program to Import file and print the DCM.";
			fileName = "HAZ" + vesselCode + ".TXT";
			String EMAIL_VES_LH = TosRefDataUtil.getValue("EMAIL_DCM_"
					+ vesselCode);
			String EMAIL_DCM_LHMA = TosRefDataUtil.getValue("EMAIL_DCM_LHMA");
			// Append LongHaul Mailing list + General mailing list + LongHaul
			// Mail Additions
			strBuffer.append(EMAIL_VES_LH + ";");
			strBuffer.append(EMAIL_DCM_GM + ";");
			strBuffer.append(EMAIL_DCM_LHMA);
		}
		logger.debug("Class: DcmConverterFileProcessor - Method: vesselEmailInformation : "
				+ subject);

		vesselData.add(subject);
		vesselData.add(mailBody);
		vesselData.add(fileName);
		vesselData.add(strBuffer.toString());

		return vesselData;
	}

	/*
	 * Method provides NCB Email Information
	 */
	private ArrayList<String> emailInformationForNcb() {
		ArrayList<String> ncbData = new ArrayList<String>();
		String subject = "N.C.B.- " + vesselCode + voyageNumber + " DCM "
				+ dcmVersion + " File, Departing " + vesselFrom + " to "
				+ vesselBoundFor;
		String mailBody = "Attached is the "
				+ dcmVersion
				+ " "
				+ vesselCode
				+ voyageNumber
				+ " DCM Text File.\n"
				+ "Save the file to C:\\HAZARD TEXT.\nUse HAZVIEW Program to Import file and print the DCM.";
		String filename = HAZARD_FILE;
		StringBuffer strBuffer = new StringBuffer();
		String EMAIL_DCM_NCB = TosRefDataUtil.getValue("EMAIL_DCM_NCB");
		// Append NCB Mailing list + General mailing list
		strBuffer.append(EMAIL_DCM_NCB + ";");
		strBuffer.append(EMAIL_DCM_GM);

		ncbData.add(subject);
		ncbData.add(mailBody);
		ncbData.add(filename);
		ncbData.add(strBuffer.toString());
		return ncbData;
	}

	/*
	 * Method provides USCG Email Information
	 */
	private ArrayList<String> emailInformationForUscg() {
		String vesName = vesselName != null && vesselName.trim().length() > 0 ? vesselName
				: vesselCode;
		String subject = vesName + " " + voyageNumber + " DCM";
		String mailBody = "Attached is the DCM file for " + vesName + " "
				+ voyageNumber;
		String filename = vesselCode + voyageNumber + ".TXT";
		String EMAIL_USCG_GRP = TosRefDataUtil.getValue("EMAIL_USCG_GRP");
		ArrayList<String> uscgData = new ArrayList<String>();
		uscgData.add(subject);
		uscgData.add(mailBody);
		uscgData.add(filename);
		uscgData.add(EMAIL_USCG_GRP);
		return uscgData;
	}

	/*
	 * Method mailDistributionProcess processes the mailing and FTP
	 * Functionality based on Mailing Type
	 */
	private void processMailDistribution(String emailFileContent)
			throws Exception {
		// Vessel Information
		ArrayList<String> vesselInfo = emailInformationForVessel();
		// NCB Information
		ArrayList<String> ncbInfo = emailInformationForNcb();
		// USCG Information
		ArrayList<String> uscgInfo = emailInformationForUscg();

		logger.debug("emailFileName: " + vesselInfo.get(2)
				+ "\n emailSubContVes: " + vesselInfo.get(0)
				+ "\n emailBodyContent: " + vesselInfo.get(1)
				+ "\n emailToVesAddr: " + vesselInfo.get(3)
				+ "\n emailSubContNCB: " + ncbInfo.get(0)
				+ "\n emailToNCBAddr: " + ncbInfo.get(1));

		// Store the file on the remote site
		try {
			mailAttachementToFTP(emailFileContent);
		} catch (Exception e) {
			logger.error("FTP Send failed but eating the Exception so the email can go out..."
					+ e);
		}
		// Mail to NCB Type
		EmailSender.mailAttachment(ncbInfo.get(3), emailAddr, MAIL_HOST,
				ncbInfo.get(2), emailFileContent, ncbInfo.get(1),
				ncbInfo.get(0));
		// Mail To Vessel Type (LongHaul/Barge)
		EmailSender.mailAttachment(vesselInfo.get(3), emailAddr, MAIL_HOST,
				vesselInfo.get(2), emailFileContent, vesselInfo.get(1),
				vesselInfo.get(0));
		// Mail USCG File
		if (DCM_VER_FINAL.equalsIgnoreCase(dcmVersion)) {
			EmailSender.mailAttachment(uscgInfo.get(3), emailAddr, MAIL_HOST,
					uscgInfo.get(2), outUscg.toString(), uscgInfo.get(1),
					uscgInfo.get(0));
		}
	}
	private void sendGuamDcmToFtp(String fileContent) {
		String fileName = "HAZ" + vesselCode + ".TXT";
		try {
			int ftpProxyId = Integer.parseInt(TosRefDataUtil.getValue("DCM_INS_FTP_ID"));
			int timeout = Integer.parseInt(TosRefDataUtil.getValue("FTP_TIMEOUT"));
			FtpProxySenderBiz sender = new FtpProxySenderBiz();
			logger.debug("Before setting the connection mode to passive");
			sender.setPassiveConnectionMode(true);
			logger.debug("After setting the connection mode to passive");
			sender.setCreateSubDirectory(true);
			sender.setTimeout(timeout);
			sender.sendFile(ftpProxyId, fileName, fileContent.getBytes(), true, "gumdcm");
		}
		catch(Exception e) {
			logger.error("Unable to send Guam DCM to FTP : "+e.getMessage());
			EmailSender.sendMail(errorEmailAddr, emailAddr, "Error uploading DCM for "+vesselCode+voyageNumber, "Exception details:<br/>"+e.getMessage());
			return;
		}
		try {
			String emailTo = TosRefDataUtil.getValue("GUAM_ML_DCM_UPLOAD");
			String subject = "DCM for " + vesselCode + voyageNumber + " is successfully sent";
			String msgText = "Attached is the dcm file uploaded to Guam DCM FTP location.";
			EmailSender.mailAttachment(emailTo, emailAddr, MAIL_HOST, fileName, fileContent, msgText, subject);
		}
		catch(Exception e) {
			logger.error("Unable to send email alert ", e);
		}
	}
	/*
	 * Main Method
	 */
	/*
	 * public static void main( String[] args) { try { File file = new
	 * File("C:/TestDCM/HAL813.csv");
	 * 
	 * if(file != null) { System.out.println("File :: "+file); } BufferedReader
	 * br = new BufferedReader(new FileReader(file)); StringBuilder sb = new
	 * StringBuilder(); String line = null;
	 * 
	 * while ((line = br.readLine()) != null ) { sb.append(line + "\n"); }
	 * 
	 * br.close();
	 * 
	 * String fileStr = sb.toString(); DcmConverterFileProcessor dcmConverter =
	 * new DcmConverterFileProcessor();
	 * System.out.println("Before processFile(fileStr)");
	 * dcmConverter.processFile(fileStr);
	 * System.out.println("After processFile(fileStr)");
	 * 
	 * } catch(Exception ex) { ex.printStackTrace(); } }
	 */
}
