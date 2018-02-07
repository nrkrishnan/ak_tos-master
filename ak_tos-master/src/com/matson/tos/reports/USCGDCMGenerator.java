package com.matson.tos.reports;

/**
 * 
 * S.No			Date				Author						Description
 * ----			----				------						-----------
 * 1			04/05/2013			Karthik Rajendran			USCG DCM File generator class created.
 * 2			04/08/2013			Karthik Rajendran			Changed: packNum field length to 7
 * 3			04/09/2013			Karthik Rajendran			Changed: shipperCnee field length to 25,
 * 																Removed: "MATU" prefix from container numbers
 * 4			04/16/2013			Karthik Rajendran			Added: TosGumDcmMt to generate DCM file for Guam too
 * 
 * 
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;
import com.matson.cas.refdata.mapping.TosDcmMt;
import com.matson.cas.refdata.mapping.TosGumDcmMt;
import com.matson.cas.erd.service.data.VesselVO;
import com.matson.tos.dao.GumVesselReportDao;
import com.matson.tos.dao.NewReportVesselDao;
import com.matson.tos.processor.CommonBusinessProcessor;
import com.matson.tos.util.EmailSender;
import com.matson.tos.util.TosRefDataUtil;

public class USCGDCMGenerator {
	private static Logger logger = Logger.getLogger(USCGDCMGenerator.class);	
	private StringBuilder outputText = null;
	private String outFilePath = System.getProperty("java.io.tmpdir");
	private String outputFileName = outFilePath;
	private String newLine = System.getProperty("line.separator");
	private String subject = "";
	private String message = "";
	private ArrayList<TosDcmMt> dcmDataList = null;
	private ArrayList<TosGumDcmMt> gumdcmDataList = null;
	public static final String NV_HON = "hon";
	public static final String NV_GUM = "gum";
	private String nvType = NV_HON;

	public USCGDCMGenerator() { }

	public USCGDCMGenerator(String vesvoy, String type, boolean autos, String nvType)
	{	
		if (outputFileName!=null) {
			outputFileName = outputFileName+"/";
		}
		
		if(vesvoy!=null && vesvoy.length()>0)
		{
			this.nvType = nvType;
			if(nvType.equals(NV_HON)) {
				// Get DCM Data list for hon
				dcmDataList = NewReportVesselDao.getDcmDataForUSCG(vesvoy, autos);
				if(dcmDataList==null)
					return;
			} else if(nvType.equals(NV_GUM)) {
				// Get DCM Data list for gum
				gumdcmDataList = GumVesselReportDao.getDcmDataForUSCG(vesvoy, autos);
				if(gumdcmDataList==null)
					return;
			}
			if(vesvoy.startsWith("HAL"))
				outputFileName = outputFileName + "H" + vesvoy.replace("HAL", "") + "DCM.txt";
			else if(vesvoy.startsWith("LOA"))
				outputFileName = outputFileName + "L" + vesvoy.replace("LOA", "") + "DCM.txt";
			else if(vesvoy.startsWith("ALE"))
				outputFileName = outputFileName + "A" + vesvoy.replace("ALE", "") + "DCM.txt";
			else if(vesvoy.startsWith("ISL"))
				outputFileName = outputFileName + "I" + vesvoy.replace("ISL", "") + "DCM.txt";
			else
				outputFileName = outputFileName + vesvoy + ".txt";
			
			outputText = new StringBuilder();
			outputText.append(getHeader(vesvoy, type));
			if(nvType.equals(NV_GUM)) {
				dcmDataList = new ArrayList<TosDcmMt>();
				for(int g=0; g<gumdcmDataList.size(); g++)
				{
					TosGumDcmMt temp = gumdcmDataList.get(g);
					TosDcmMt dcm = new TosDcmMt();
					dcm.setHazDesc1(temp.getHazDesc1());
					dcm.setPackNum(temp.getPackNum());
					dcm.setCell(temp.getCell());
					dcm.setGrosswgt(temp.getGrosswgt());
					dcm.setContainerNumber(temp.getContainerNumber());
					dcm.setDport(temp.getDport());
					dcm.setShipperCnee(temp.getShipperCnee());
					dcmDataList.add(dcm);
				}
			}
			outputText.append(getDetails(dcmDataList));
		}
	}
	private String getHeader(String vesvoy, String type)
	{
		String output = "";
		if(type.toUpperCase().equals("INBOUND"))
		{
			output = "         DCM DATA FOR USCG INBOUND TO HONOLULU BY DESCRIPTION";
			subject = "USCG-Matson Inbound DCM for " + vesvoy;
		}
		else
		{
			output = "         DCM DATA FOR USCG OUTBOUND FROM HONOLULU BY DESCRIPTION";
			subject = "USCG-Matson Outbound DCM for " + vesvoy;
		}
		if(nvType.equals(NV_GUM)) {
			output = output.replace("HONOLULU", "GUAM");
		}
		message = "Attached is the DCM file for " + vesvoy;
		output = output + "      Page 1" + newLine;
		String vesselName = "";
		String officialNumber = "";
		try {
			VesselVO vvo = CommonBusinessProcessor.getVesselDetails(vesvoy.substring(0, 3));
			if(vvo!=null)
			{
				vesselName = "                         " + vvo.getVessName() + "  Voyage: " +  vesvoy.substring(3, 6);
				logger.info("Vessel Name: " + vesselName);
				officialNumber = vvo.getVessOffclNumber();
				logger.info("Vessel Official number: "+officialNumber);
			}
			if(vesselName==null||vesselName.equals(""))
			{
				logger.error("Send DCM to USCG  - Error retrieving vessel name");
				vesselName = "                         " + vesvoy.substring(0, 3) + "   Voyage: " +  vesvoy.substring(3, 6);
			}
		} catch(Exception e) {
			logger.error("Send DCM to USCG  - Error retrieving vessel name");
			vesselName = "                         " + vesvoy.substring(0, 3) + "   Voyage: " +  vesvoy.substring(3, 6);
		}
		output = output + vesselName + newLine + newLine;
		officialNumber = officialNumber==null?"":officialNumber;
		output = output + "                        Official Number: " + officialNumber + newLine;
		output = output + "============================================================================" + newLine;
		output = output + " Description" + newLine;
		output = output + " Pkg No. Stowage Weight  Container    Dport Shipment#  Shipper" + newLine;
		output = output + " ------- ------- ------- ------------ ----- ---------- ---------------------" + newLine;
		//
		return output;
	}
	private String getDetails(ArrayList<TosDcmMt> dcmDataList)
	{
		String output = "";
		for(int d=0; d<dcmDataList.size(); d++)
		{
			TosDcmMt dcm = dcmDataList.get(d);
			String tempStr = "";
			if(dcm.getHazDesc1()!=null)
			{
				String out[] = splitIntoLine(dcm.getHazDesc1(), 75);
				for(int i=0; i<out.length; i++)
				{
					tempStr = tempStr + " " + out[i].trim() + newLine;
				}
			}
			else
			{
				tempStr =  " " + newLine;				
			}
			String pack = dcm.getPackNum();
			if(pack!=null)
			{
				pack = pack.length()>7?pack.substring(0, 7):pack;
			}
			String shipperCnee = dcm.getShipperCnee();
			shipperCnee = shipperCnee==null?"N/A":shipperCnee;
			shipperCnee = shipperCnee.length()>25?shipperCnee.substring(0, 25):shipperCnee;
			String eqNo = dcm.getContainerNumber();
			eqNo = eqNo.replace("MATU", "");
			//
			tempStr = tempStr + " " + getField(pack, 8);
			tempStr = tempStr + getField(dcm.getCell(), 8);
			tempStr = tempStr + getField(dcm.getGrosswgt(), 8);
			tempStr = tempStr + getField(eqNo, 13);
			tempStr = tempStr + getField(dcm.getDport(), 17);
			tempStr = tempStr + shipperCnee.trim();
			tempStr = tempStr.replaceAll("null", "");
			tempStr = tempStr + newLine;
			output = output + tempStr + newLine;
		}
		//
		return output;
	}
	private String getField(String data, int length)
	{
		data = data==null?" ":data;
		return String.format("%1$-" + length + "s", data);
	}
	private String[] splitIntoLine(String input, int maxCharInLine){

	    StringTokenizer tok = new StringTokenizer(input, " ");
	    StringBuilder output = new StringBuilder(input.length());
	    int lineLen = 0;
	    while (tok.hasMoreTokens()) {
	        String word = tok.nextToken();

	        while(word.length() > maxCharInLine){
	            output.append(word.substring(0, maxCharInLine-lineLen) + "\n");
	            word = word.substring(maxCharInLine-lineLen);
	            lineLen = 0;
	        }

	        if (lineLen + word.length() > maxCharInLine) {
	            output.append("\n");
	            lineLen = 0;
	        }
	        output.append(word + " ");

	        lineLen += word.length() + 1;
	    }
	    return output.toString().split("\n");
	}
	public void sendDcmToUSCG()
	{
		if(outputText!=null && dcmDataList!=null && dcmDataList.size()>0)
		{
			try{
				logger.info("Creating "+outputFileName+" file....");
				FileWriter fileWriter = new FileWriter(new File(outputFileName));
				BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
				bufferedWriter.write(outputText.toString());
				bufferedWriter.close();
				fileWriter.close();
				// Send the DCM file to email
				String host = TosRefDataUtil.getValue("MAIL_HOST");
				String from = TosRefDataUtil.getValue("EMAIL_DCM_USCG_FROM");
				String to = TosRefDataUtil.getValue("EMAIL_DCM_USCG_TO");
				if(nvType.equals(NV_GUM)) {
					from = TosRefDataUtil.getValue("GUAM_DCM_USCG_FROM");
					to = TosRefDataUtil.getValue("GUAM_DCM_USCG_TO");
				}
				logger.info("Sending DCM file to USCG");
				EmailSender.mailTextAttachment(to, from, host, outputFileName, message, subject);
			}
			catch(Exception e) {
				e.printStackTrace();
				logger.error(e);
			}			
		}
		else
		{
			logger.error("Send DCM TO USCG - No DCM Found.");
		}
	}
}
