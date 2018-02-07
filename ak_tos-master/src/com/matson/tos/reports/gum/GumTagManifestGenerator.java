package com.matson.tos.reports.gum;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;

import com.matson.cas.refdata.mapping.TosGumRdsDataFinalMt;
import com.matson.cas.refdata.mapping.TosGumRdsDataMt;
import com.matson.tos.dao.GumVesselReportDao;

/*
 *
 * #		Date			Author						Description
 * ---		-----------		------------------			---------------------
 * 1		03/20/2013		Karthik Rajendran			Manifest file generator class created
 * 2		04/03/2013		Karthik Rajendran			Removed: Date parameter
 * 3		04/08/2013		Karthik Rajendran			Changed: Field mappings from custom list, removed getImpRdsListForTAGManifest(),
 * 																setting file creation flag based on the result return. 
 *
 *
 */

public class GumTagManifestGenerator {

	private static Logger logger = Logger.getLogger(GumTagManifestGenerator.class);
	public boolean fileCreated = false;
	public GumTagManifestGenerator()
	{

	}
	public void generateManifestFile(String vesvoy)
	{
		String outFilePath = System.getProperty("java.io.tmpdir");
		if (outFilePath !=null && !outFilePath.endsWith("/")) {
			outFilePath = outFilePath + "/";
		}
		String outputFileName = outFilePath + vesvoy + "_G.TXT";
		String outputZipFileName = outFilePath + vesvoy + "_G.ZIP";
		StringBuilder output = new StringBuilder();
		logger.info("Retrieving data......");
		List cntrsList = GumVesselReportDao.getListForTAGManifest(vesvoy);
		//ArrayList<TosGumRdsDataMt> impList = GumVesselReportDao.getImpRdsListForTAGManifest(vesvoy);
		if(cntrsList!=null)
		{
			for(int i=0; i<cntrsList.size(); i++)
			{
				Object[] obj =  (Object[]) cntrsList.get(i);
				String tempStr = "";
				String dryReefer = "";
				if(obj[8]!=null && obj[8].toString().startsWith("R"))
					dryReefer = "R";
				else
					dryReefer = "D";
				tempStr = tempStr + "\"" + (obj[0]==null?"":obj[0].toString()) + "\","; //CNEE_CODE
				tempStr = tempStr + "\"" + (obj[1]==null?"":obj[1].toString()) + "\","; //CONSIGNEE
				tempStr = tempStr + "\"" + (obj[2].toString()+"-"+(obj[3]==null?"X":obj[3].toString())) + "\","; //CONTAINER_NUMBER - CHECK_DIGIT
				tempStr = tempStr + "\"" + (obj[4]==null?"":obj[4].toString()) + "\","; //DPORT
				tempStr = tempStr + "\"" + (obj[5]==null?"":obj[5].toString()) + "\","; //VESVOY
				tempStr = tempStr + "\"" + (obj[6]==null?"":obj[6].toString()) + "\","; //CARGO_NOTES
				tempStr = tempStr + "\"" + (obj[7]==null?"":obj[7].toString()) + "\","; //BOOKING_NUMBER
				tempStr = tempStr + "\"" + "NVI" + "\",";
				tempStr = tempStr + "\"" + new SimpleDateFormat("MM/dd/yyyy").format(new Date()) + "\",";
				tempStr = tempStr + "\"" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "\",";
				tempStr = tempStr + "\"" + "" + "\",";
				tempStr = tempStr + "\"" + "" + "\",";
				tempStr = tempStr + "\"" + "" + "\",";
				tempStr = tempStr + "\"" + dryReefer + "\",";
				tempStr = tempStr + "\"" + (obj[8]==null?"":obj[8].toString().substring(1, 3)) + "\","; //TYPE_CODE
				tempStr = tempStr + "\"" + (obj[9]==null?"":obj[9].toString()) + "\","; //OWNER
				tempStr = tempStr + "\"" + (obj[10]==null?"":obj[10].toString()) + "\","; //CONSIGNEE_ADDR
				tempStr = tempStr + "\"" + (obj[11]==null?"":obj[11].toString()) + "\","; //CONSIGNEE_CITY
				tempStr = tempStr + "\"" + (obj[12]==null?"":obj[12].toString()) + "\","; //CONSIGNEE_STATE
				tempStr = tempStr + "\"" + (obj[13]==null?"":obj[13].toString()) + "\","; //CONSIGNEE_ZIP_CODE
				tempStr = tempStr.substring(0, tempStr.length()-1);
				tempStr = tempStr.replaceAll("null", "");
				output.append(tempStr + System.getProperty("line.separator"));				
			}
			try{
				logger.info("Creating "+outputFileName+" file....");
				FileWriter fileWriter = new FileWriter(new File(outputFileName));
				BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
				bufferedWriter.write(output.toString());
				bufferedWriter.close();
				fileWriter.close();
				logger.info("Creating "+outputZipFileName+" file....");
				FileInputStream inStream = new FileInputStream(outputFileName);
	            ZipOutputStream outStream = new ZipOutputStream(new FileOutputStream(outputZipFileName));
	            outStream.putNextEntry(new ZipEntry(vesvoy + "_G.TXT"));
	            byte[] buffer = new byte[1024];
	            int bytesRead;
	            while ((bytesRead = inStream.read(buffer)) > 0) {
	                outStream.write(buffer, 0, bytesRead);
	            }
	            outStream.closeEntry();
	            outStream.close();
	            inStream.close();
	            fileCreated = true;
			}catch(Exception e) {
				e.printStackTrace();
				logger.error(e);
				fileCreated = false;
			}
		}
		else
		{
			logger.error("generateManifestFile() - No Manifest found.");
		}
		
	}

}
