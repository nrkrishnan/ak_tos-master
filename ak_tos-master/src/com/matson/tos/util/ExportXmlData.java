package com.matson.tos.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import org.apache.log4j.Logger;

import com.matson.cas.service.ftp.FtpProxySenderBiz;

public class ExportXmlData {
	private static Logger logger = Logger.getLogger(ExportXmlData.class);
	
	/*
	 * Method Copies the XMl Data to a file and exports it to a FTP folder  
	 */
	public static void copyXmlToFile(String fileName,String strXml,String strXmlFtpId)
	{
		try
		{
				int xmlTestFtpID = Integer.parseInt(strXmlFtpId);
				int timeout = Integer.parseInt(TosRefDataUtil.getValue( "FTP_TIMEOUT"));
				logger.debug("FTP timeout retrieved is: " + timeout);				
				logger.debug("Xml Test Ftp Id :"+xmlTestFtpID);	
				File fileToFtpAddr = new File(fileName);
				BufferedWriter bufWriter = new BufferedWriter(new FileWriter(fileToFtpAddr));
				bufWriter.write( strXml);
				bufWriter.close();
				
				FtpProxySenderBiz ftpSender = new FtpProxySenderBiz();
				ftpSender.setTimeout(timeout);
				ftpSender.sendFile(xmlTestFtpID, fileToFtpAddr);
		}
		catch(Exception e)
		{
			logger.error("Error in Sending File to FTP Address", e);
		}
	}

}
