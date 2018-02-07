package com.matson.tos.exception;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.matson.tos.util.EmailSender;
import com.matson.tos.util.TosRefDataUtil;

public class NewVesselLogger {
	private static Logger logger = Logger.getLogger(NewVesselLogger.class);
	public static NewVesselLogger nvLogger;
	public static ArrayList<NewVesselError> nvErrors;
	public static ArrayList<NewVesselError> nvSuppErrors;
	public static ArrayList<NewVesselFtpError> nvFtpErrors;
	public static ArrayList<NewVesselFileError> nvFileErrors;
	public static ArrayList<NewVesselFileError> nvDcmDataErrors;
	public static ArrayList<NewVesselError> nvChErrors;
	//
	public NewVesselLogger(){
		//
	}
	public static NewVesselLogger getInstance(){
		if(nvLogger==null)
			nvLogger = new NewVesselLogger();
		return nvLogger;
	}
	public void clearLogger(){
		logger.info("NewVesselLogger.clearLogger()");
		nvErrors = null;
		nvSuppErrors = null;
		nvFileErrors = null;
		nvFtpErrors = null;
		nvDcmDataErrors = null;
		nvChErrors = null;
		nvLogger = null;
	}
	public void addError(String vvd, String containerNumber, String errMessage){
		logger.info("NewVesselLogger.addError()-"+vvd+"\t"+containerNumber+"\t"+errMessage);
		if(nvErrors==null)
			nvErrors = new ArrayList<NewVesselError>();
		NewVesselError nvErr = new NewVesselError();
		nvErr.setContainerNumber(containerNumber);
		nvErr.setErrorMessage(errMessage);
		nvErr.setVvd(vvd);
		nvErrors.add(nvErr);
		logger.info("nvErrors.addError()-"+ nvErrors);
	}
	
	
	public void addSuppError(String vvd, String containerNumber, String errMessage){
		logger.info("NewVesselLogger.addSuppError()-"+vvd+"\t"+containerNumber+"\t"+errMessage);
		logger.info("NewVesselLogger.addSuppError()-"+ nvSuppErrors);
		if(nvSuppErrors==null)
			nvSuppErrors = new ArrayList<NewVesselError>();
		NewVesselError nvErr = new NewVesselError();
		nvErr.setContainerNumber(containerNumber);
		nvErr.setErrorMessage(errMessage);
		nvErr.setVvd(vvd);
		nvSuppErrors.add(nvErr);
		logger.info("NewVesselLogger.addSuppError()-"+ nvSuppErrors);
	}
	public void addFtpError(String ftpId, String message)
	{
		logger.info("NewVesselLogger.addFtpError()-"+ftpId+"\t"+message);
		if(nvFtpErrors==null)
			nvFtpErrors = new ArrayList<NewVesselFtpError>();
		NewVesselFtpError nvFtpErr = new NewVesselFtpError();
		nvFtpErr.setFtpId(ftpId);
		nvFtpErr.setMessage(message);
		nvFtpErrors.add(nvFtpErr);
	}
	public void addFileError(String fileName, String message)
	{
		logger.info("NewVesselLogger.addFileError()-"+fileName+"\t"+message);
		if(nvFileErrors==null)
			nvFileErrors = new ArrayList<NewVesselFileError>();
		NewVesselFileError nvFileErr = new NewVesselFileError();
		nvFileErr.setFileName(fileName);
		nvFileErr.setMessage(message);
		nvFileErrors.add(nvFileErr);
	}
	public void addDcmDataError(String fileName, String message)
	{
		logger.info("NewVesselLogger.addDcmDataError()-"+fileName+"\t"+message);
		if(nvDcmDataErrors==null)
			nvDcmDataErrors = new ArrayList<NewVesselFileError>();
		NewVesselFileError nvDcmErr = new NewVesselFileError();
		nvDcmErr.setFileName(fileName);
		nvDcmErr.setMessage(message);
		nvDcmDataErrors.add(nvDcmErr);
	}
	public void addChExecError(String vvd, String message)
	{
		logger.info("NewVesselLogger.addChExecError()-"+vvd+"\t"+message);
		if(nvChErrors==null)
			nvChErrors = new ArrayList<NewVesselError>();
		NewVesselError nvChErr = new NewVesselError();
		nvChErr.setVvd(vvd);
		nvChErr.setErrorMessage(message);
		nvChErrors.add(nvChErr);
	}
	public void sendNewVessStowPlanSuccess(String vvd, int totalNoOfContainers)
	{
		logger.info("NewVesselLogger.sendNewVessStowPlanSuccess()-"+vvd+"\t"+totalNoOfContainers);
		String from = TosRefDataUtil.getValue("EMAIL_NEWVES_FROM");
		String to = TosRefDataUtil.getValue("EMAIL_NEWVES_TO_O");
		String message = "";
		String subject = "";
		subject = "Vessel " + vvd + " _O Received from GEMS";
		message = vvd +" Ready for Pre-Import "+ "<br /><br />";
		message = message + "Total Number of Containers: " + totalNoOfContainers + "<br /><br />";
		message = message + "Make sure we have RDS Download and Hazard files. " + "<br /><br /><br /><br />";
		
		EmailSender.sendMail(from, to, subject, message);
	}
	public void sendAvailDateNotification(String vvd, String availDate, String source)
	{
		logger.info("NewVesselLogger.sendAvailDateNotification()-"+vvd+"\t"+availDate+"\t"+source);
		String from = TosRefDataUtil.getValue("EMAIL_NEWVES_FROM");
		String to = TosRefDataUtil.getValue("EMAIL_NV_TO_CMC");
		if ("".equalsIgnoreCase(availDate))
			availDate = "Blank";
		String message = "";
		String subject = "";
		if ("stowplan".equalsIgnoreCase(source) && !"Blank".equalsIgnoreCase(availDate)) {
			subject = "Available date for vessel " + vvd + " is "+availDate ;
			message = "Available date for vessel " + vvd + "is "+availDate + ". Please review and update if required. <br /><br />";
		} else if ("stowplan".equalsIgnoreCase(source) && "Blank".equalsIgnoreCase(availDate)) {
			subject = "Newves ERRORS : No available date for " + vvd;
			message = "Stowplan is processed. Please go to Vessel Visits tab and update the Begin Delivery date for "+ vvd +". <br /><br />";
		}
		else {
			subject = "Newves ERRORS : No available date for " + vvd+". Cannot process RDS Download for "+vvd+".";
			message = message + "RDS process is stopped." + "<br /><br />";
			message = message + "Please go to Vessel Visits tab and update the Begin Delivery date for "+ vvd +". <br /><br />";
		}
		EmailSender.sendMail(from, to, subject, message);
	}
	public void sendGumNewVessStowPlanSuccess(String vvd, int totalNoOfContainers)
	{
		logger.info("NewVesselLogger.sendGumNewVessStowPlanSuccess()-"+vvd+"\t"+totalNoOfContainers);
		String from = TosRefDataUtil.getValue("GUAM_MAIL_NV_FROM");
		String to = TosRefDataUtil.getValue("GUAM_MAIL_NV_TO");
		String message = "";
		String subject = "";
		subject = "Vessel " + vvd + " _O Received from GEMS";
		message = vvd +" Ready for Pre-Import "+ "<br /><br />";
		message = message + "Total Number of Containers: " + totalNoOfContainers + "<br /><br />";
		message = message + "Make sure we have RDS Download and Hazard files. " + "<br /><br /><br /><br />";
		
		EmailSender.sendMail(from, to, subject, message);
	}
	public void sendGumRdsProcessSuccess(String vvd, String truckerUrl)
	{
		logger.info("NewVesselLogger.sendGumRdsProcessSuccess()-"+vvd+"\t"+truckerUrl);
		String from = TosRefDataUtil.getValue("GUAM_MAIL_NV_FROM");
		String to = TosRefDataUtil.getValue("GUAM_MAIL_NV_TO");
		String message = "";
		String subject = "";
		subject = "GUM RDS process is completed for " + vvd;
		message = "RDS process is completed for " + vvd + ". Please proceed with trucker coding.";
		if(truckerUrl!=null && truckerUrl.length()>0)
		{
			message = message + "<br /><a href=\"" + truckerUrl + "\">" + truckerUrl + "</a>"; 
		}
		EmailSender.sendMail(from, to, subject, message);
	}
	public void sendNewVessSuccess(String vvd, String vesType, String copyFlag)
	{
		logger.info("NewVesselLogger.sendNewVessSuccess()-"+vvd+"\t"+vesType+"\t"+copyFlag);
		String from = TosRefDataUtil.getValue("EMAIL_NEWVES_FROM");
		String to = TosRefDataUtil.getValue("EMAIL_NEWVES_TO");
		String message = "";
		String subject = "";
		copyFlag = copyFlag==null?"":copyFlag;
		if(copyFlag.equalsIgnoreCase("true"))
		{
			if (vesType.equalsIgnoreCase("L")) {
				to = TosRefDataUtil.getValue("EMAIL_NV_TO_SN4_L1");
				subject = "Import Newves File for "+vvd+" sent to SN4";
				message = "The containers on the vessel/barge will be available in SN4.<br /><br /><br />";
			}else if (vesType.equalsIgnoreCase("B")) {
				to = TosRefDataUtil.getValue("EMAIL_NV_TO_SN4_B1");
				subject = "Import Newves File for "+vvd+" sent to SN4";
				message = "The containers on the vessel/barge will be available in SN4.<br /><br /><br />";
			}
			else if (vesType.equalsIgnoreCase("A")) {
				to = TosRefDataUtil.getValue("EMAIL_NV_TO_SN4_L2");
				subject = "Import Newves File for "+vvd+" is available in SN4";
				message = "The containers on the vessel/barge are available in SN4.<br /><br /><br />";
			}
		}
		else 
		{
			if(vesType.equalsIgnoreCase("L")) {
				to = TosRefDataUtil.getValue("EMAIL_NV_TO_SN4_L3");
				subject = "New Matson vessel " + vvd + " - Processed successfully.";
				message = "RDS is processed for " + vvd + " and Preview download report is generated. " + "<br /><br /><br />";
			} else if(vesType.equalsIgnoreCase("B")) {
				to = TosRefDataUtil.getValue("EMAIL_NV_TO_SN4_B2");
				subject = "New Matson vessel - Barge - " + vvd + " - Processed successfully.";
				message = "New Matson vessel - Barge - " + vvd + " - Processed successfully. " + new Date() + "<br /><br /><br />";
			}
		}
		EmailSender.sendMail(from, to, subject, message);
	}
	public void sendGumNewVessSuccess(String vvd, String vesType, String copyFlag)
	{
		logger.info("NewVesselLogger.sendGumNewVessSuccess()-"+vvd+"\t"+vesType+"\t"+copyFlag);
		String from = TosRefDataUtil.getValue("GUAM_MAIL_NV_FROM");
		String to = TosRefDataUtil.getValue("GUAM_MAIL_NV_TO");
		String message = "";
		String subject = "";
		copyFlag = copyFlag==null?"":copyFlag;
		if(copyFlag.equalsIgnoreCase("true"))
		{
			subject = "Import Newves File for "+vvd+" sent to SN4";
			message = "The containers on the vessel/barge will be available in SN4.<br /><br /><br />";
		}
		else 
		{
			if(vesType.equalsIgnoreCase("L")) {
				subject = "New Matson vessel " + vvd + " - Processed successfully.";
				message = "New Matson vessel " + vvd + " - Processed successfully. " + new Date() + "<br /><br /><br />";
			} 
		}
		EmailSender.sendMail(from, to, subject, message);
	}
	public void sendSupplementalSuccess(String ctrsSize, String file, String copyFlag)
	{
		logger.info("NewVesselLogger.sendSupplementalSuccess()-"+ctrsSize+"\t"+file+"\t"+copyFlag);
		String from = TosRefDataUtil.getValue("EMAIL_NEWVES_FROM");
		String to = TosRefDataUtil.getValue("EMAIL_NEWVES_TO_SUPP");
		String message = "";
		String subject = "";
		copyFlag = copyFlag==null?"":copyFlag;
		file = file==null?"":file;
		if(copyFlag.equalsIgnoreCase("true"))
		{
			if(file.length()>0)
				subject = "Daily Supplemental Updates for " + file + " sent to SN4";
			else
				subject = "Daily Supplemental Updates sent to SN4";
			message = "Supplemental udpates for Units will be available in SN4. <br /><br /><br />";
		}
		else 
		{
			subject = "Supplemental containers - Processed successfully.";
			message = ctrsSize + " Supplemental container(s) processed. " + new Date();
			message = message.trim()+ "<br /><br /><br />";
		}		
		EmailSender.sendMail(from, to, subject, message);
	}
	public void sendNewVessErrors()
	{
		logger.info("NewVesselLogger.sendNewVessErrors()");
		String from = TosRefDataUtil.getValue("EMAIL_NEWVES_FROM");
		String to = TosRefDataUtil.getValue("EMAIL_NEWVES_ERR_TO");
		String message = "";
		String subject = "";
		if(nvFtpErrors!=null && nvFtpErrors.size() >= 1)
		{
			message = "";
			subject = "NewVessel FTP Errors";
			for(int i=0; i<nvFtpErrors.size(); i++)
			{
				NewVesselFtpError nvFtpErr = nvFtpErrors.get(i);
				message = message + nvFtpErr.getMessage() + "<br />";
			}
			EmailSender.sendMail(from, to, subject, message);
		}
		if(nvFileErrors!=null && nvFileErrors.size() >= 1)
		{
			message = "";
			subject = "NewVessel File Errors";
			for(int i=0; i<nvFileErrors.size(); i++)
			{
				NewVesselFileError nvFileErr = nvFileErrors.get(i);
				subject = subject + " " + nvFileErr.getFileName();
				message = message + nvFileErr.getMessage() + "<br />";
			}
			if(message.contains("No OND"))
				to = TosRefDataUtil.getValue("HON_STOWP_NO_OND");
			EmailSender.sendMail(from, to, subject, message);
		}
		if(nvErrors!=null && nvErrors.size() >= 1)
		{
			message = "";
			String subjVesvoy = "";
			subject = "NewVessel Errors: ";
			String loadport = null;
			String dcmvvd="";
			for(int i=0; i<nvErrors.size(); i++)
			{
				NewVesselError nvErr = nvErrors.get(i);
				String vvd = nvErr.getVvd();
				if(vvd!=null&&!vvd.equals(""))
				{
					if(!subjVesvoy.contains(vvd))
					{
						subjVesvoy = subjVesvoy + vvd + ",";
					}
					if(!nvErr.getErrorMessage().contains("-No HAZ/DCM")) {
						message = message + " " + vvd;
					}
					if (nvErr.getErrorMessage().contains("-No HAZ/DCM")) {
						loadport = nvErr.getErrorMessage().replace("-No HAZ/DCM", "");
						System.out.println("Inside errors "+loadport);
						dcmvvd = vvd;
					}
				}
				String cntrno = nvErr.getContainerNumber();
				if(cntrno!=null&&!cntrno.equals(""))
				{
					message = message + " " + cntrno;
				}
				message = message + " " +  nvErr.getErrorMessage() + "<br />";
			}
			if(subjVesvoy!=null && subjVesvoy.length()>0)
				subjVesvoy = subjVesvoy.substring(0, subjVesvoy.length()-1);
			subject = subject + subjVesvoy;
			if (loadport!=null) {
				subject = "Newves ERRORS : Upload DCM for "+dcmvvd+" at "+loadport;
				message = loadport+" planners please upload DCM for "+dcmvvd+" Or contact TOS support.<br /><br /><br />";
				if ("KAH".equalsIgnoreCase(loadport))
					to = TosRefDataUtil.getValue("EMAIL_DCM_ERR_TOKAH");
				else if ("NAW".equalsIgnoreCase(loadport))
					to = TosRefDataUtil.getValue("EMAIL_DCM_ERR_TONAW");
				else if ("HIL".equalsIgnoreCase(loadport) || "KHI".equalsIgnoreCase(loadport))
					to = TosRefDataUtil.getValue("EMAIL_DCM_ERR_TOHIL");
				else 
					to = TosRefDataUtil.getValue("EMAIL_NEWVES_ERR_TO");
				
			}
			EmailSender.sendMail(from, to, subject, message);
		}
		
		logger.info("NewVesselLogger.Supplemental Errors");
		if(nvSuppErrors!=null && nvSuppErrors.size() >= 1)
		{
			message = "";
			String subjVesvoy = "";
			subject = "Supplemental Errors: ";
			for(int i=0; i<nvSuppErrors.size(); i++)
			{
				NewVesselError nvErr = nvSuppErrors.get(i);
				String vvd = nvErr.getVvd();
				if(vvd!=null && !vvd.equals(""))
				{
					if(!subjVesvoy.contains(vvd))
					{
						subjVesvoy = subjVesvoy + vvd + ",";
					}
					if(!nvErr.getErrorMessage().contains("-No HAZ/DCM")) {
						message = message + " " + vvd;
					}
				}
				String cntrno = nvErr.getContainerNumber();
				if(cntrno!=null&&!cntrno.equals(""))
				{
					message = message + " " + cntrno;
				}
				message = message + " " +  nvErr.getErrorMessage() + "<br />";
			}
			if(subjVesvoy!=null && subjVesvoy.length()>0)
				subjVesvoy = subjVesvoy.substring(0, subjVesvoy.length()-1);
			subject = subject + subjVesvoy;
			EmailSender.sendMail(from, to, subject, message);
		}
		clearLogger();
	}
	public void sendGumNewVessErrors()
	{
		logger.info("NewVesselLogger.sendGumNewVessErrors()");
		String from = TosRefDataUtil.getValue("GUAM_MAIL_NV_FROM");
		String to = TosRefDataUtil.getValue("GUAM_MAIL_NV_ERROR");
		String message = "";
		String subject = "";
		if(nvFtpErrors!=null && nvFtpErrors.size() >= 1)
		{
			subject = "GUM NewVessel FTP Errors";
			for(int i=0; i<nvFtpErrors.size(); i++)
			{
				NewVesselFtpError nvFtpErr = nvFtpErrors.get(i);
				message = message + nvFtpErr.getMessage() + "<br />";
			}
			EmailSender.sendMail(from, to, subject, message);
		}
		if(nvFileErrors!=null && nvFileErrors.size() >= 1)
		{
			subject = "GUM NewVessel File Errors";
			for(int i=0; i<nvFileErrors.size(); i++)
			{
				NewVesselFileError nvFileErr = nvFileErrors.get(i);
				subject = subject + " " + nvFileErr.getFileName();
				message = message + nvFileErr.getMessage() + "<br />";
			}
			if(message.contains("No OND"))
				to = TosRefDataUtil.getValue("GUM_STOWP_NO_OND");
			EmailSender.sendMail(from, to, subject, message);
		}
		if(nvErrors!=null && nvErrors.size() >= 1)
		{
			String subjVesvoy = "";
			subject = "GUM  NewVessel Errors: ";
			String loadport = null;
			String dcmvvd="";
			for(int i=0; i<nvErrors.size(); i++)
			{
				NewVesselError nvErr = nvErrors.get(i);
				String vvd = nvErr.getVvd();
				if(vvd!=null&&!vvd.equals(""))
				{
					if(!subjVesvoy.contains(vvd))
					{
						subjVesvoy = subjVesvoy + vvd + ",";
					}
					if(!nvErr.getErrorMessage().contains("-No HAZ/DCM")) {
						message = message + " " + vvd;
					}
					if (nvErr.getErrorMessage().contains("-No HAZ/DCM")) {
						loadport = nvErr.getErrorMessage().replace("-No HAZ/DCM", "");
						System.out.println("Inside errors "+loadport);
						dcmvvd = vvd;
					}
				}
				String cntrno = nvErr.getContainerNumber();
				if(cntrno!=null&&!cntrno.equals(""))
				{
					message = message + " " + cntrno;
				}
				message = message + " " +  nvErr.getErrorMessage() + "<br />";
			}
			if(subjVesvoy!=null && subjVesvoy.length()>0)
				subjVesvoy = subjVesvoy.substring(0, subjVesvoy.length()-1);
			subject = subject + subjVesvoy;
			if (loadport!=null) {
				subject = "GUM Newves ERRORS : Upload DCM for "+dcmvvd+" at "+loadport;
				message = loadport+" planners please upload DCM for "+dcmvvd+" Or contact TOS support.<br /><br /><br />";
				if ("HON".equalsIgnoreCase(loadport))
					to = TosRefDataUtil.getValue("GUAM_ML_DCM_ERR_HON");
				else if ("LAX".equalsIgnoreCase(loadport))
					to = TosRefDataUtil.getValue("GUAM_ML_DCM_ERR_LAX");
				else 
					to = TosRefDataUtil.getValue("GUAM_MAIL_NV_ERROR");
				
			}
			EmailSender.sendMail(from, to, subject, message);
		}
		clearLogger();
	}
	public void sendDcmDataErrors() {
		if(nvDcmDataErrors!=null && nvDcmDataErrors.size()>=1) {
			String from = TosRefDataUtil.getValue("EMAIL_NEWVES_FROM");
			String to = TosRefDataUtil.getValue("EMAIL_NEWVES_ERR_TO");
			String message = "";
			String subject = "";
			for(int i=0; i<nvDcmDataErrors.size(); i++) {
				NewVesselFileError nvDcmErr = nvDcmDataErrors.get(i);
				String loadPort = null;
				String fileName = nvDcmErr.getFileName();
				fileName = fileName==null?"":fileName;
				ArrayList<String> tempList = new ArrayList<String>(Arrays.asList(fileName.split(",")));
				fileName = tempList.get(0);
				loadPort = tempList.get(1);
				if(loadPort!=null && !loadPort.equals("") && !loadPort.equals("null")) {
					if ("KAH".equalsIgnoreCase(loadPort))
						to = TosRefDataUtil.getValue("EMAIL_DCM_ERR_TOKAH");
					else if ("NAW".equalsIgnoreCase(loadPort))
						to = TosRefDataUtil.getValue("EMAIL_DCM_ERR_TONAW");
					else if ("HIL".equalsIgnoreCase(loadPort) || "KHI".equalsIgnoreCase(loadPort))
						to = TosRefDataUtil.getValue("EMAIL_DCM_ERR_TOHIL");
					message = "<pre>"+loadPort+" planners, please correct the errors \n\n</pre>";
				}
				subject = "Newves DCM Data Errors : "+fileName;
				message = message + nvDcmErr.getMessage();
				EmailSender.sendMail(from, to, subject, message);
			}
		}
	}
	public void sendGumDcmDataErrors() {
		if(nvDcmDataErrors!=null && nvDcmDataErrors.size()>=1) {
			String from = TosRefDataUtil.getValue("GUAM_MAIL_NV_FROM");
			String to = TosRefDataUtil.getValue("GUAM_MAIL_NV_ERROR");
			String message = "";
			String subject = "";
			for(int i=0; i<nvDcmDataErrors.size(); i++) {
				NewVesselFileError nvDcmErr = nvDcmDataErrors.get(i);
				String loadPort = null;
				String fileName = nvDcmErr.getFileName();
				fileName = fileName==null?"":fileName;
				ArrayList<String> tempList = new ArrayList<String>(Arrays.asList(fileName.split(",")));
				fileName = tempList.get(0);
				loadPort = tempList.get(1);
				if(loadPort!=null && !loadPort.equals("") && !loadPort.equals("null")) {
					if ("HON".equalsIgnoreCase(loadPort))
						to = TosRefDataUtil.getValue("GUAM_ML_DCM_ERR_HON");
					else if ("LAX".equalsIgnoreCase(loadPort))
						to = TosRefDataUtil.getValue("GUAM_ML_DCM_ERR_LAX");
					message = "<pre>"+loadPort+" planners, please correct the errors \n\n</pre>";
				}
				subject = "Newves DCM Data Errors : "+fileName;
				message = message + nvDcmErr.getMessage();
				EmailSender.sendMail(from, to, subject, message);
			}
		}
	}
	public void sendCHErrors() {
		logger.info("sendCHErrors()");
		if(nvChErrors!=null && nvChErrors.size()>=1) {
			String from = TosRefDataUtil.getValue("EMAIL_NEWVES_FROM");
			String to = TosRefDataUtil.getValue("EMAIL_NV_CH_ERR_TO");
			String message = "";
			String subject = "";
			for(int i=0; i<nvChErrors.size(); i++) {
				NewVesselError nvChErr = nvChErrors.get(i);				
				subject = "Newvessel Errors : Cash hold file is not received for "+nvChErr.getVvd();
				logger.info(subject);
				message = "Cash hold file is mandatory for " + nvChErr.getVvd() + ". " + nvChErr.getErrorMessage();
				EmailSender.sendMail(from, to, subject, message);
			}
		}
	}
}
