package com.matson.tos.processor;

import org.apache.log4j.Logger;

import com.matson.tos.util.CalendarUtil;
import com.matson.tos.util.TosRefDataUtil;
import com.matson.cas.service.ftp.FtpProxyDeleterBiz;
import com.matson.cas.service.ftp.FtpProxyGetterBiz;
import com.matson.cas.service.ftp.FtpProxyListBiz;
import com.matson.cas.service.ftp.FtpProxySenderBiz;
import com.matson.tos.exception.TosException;


public class UploadFileProcessor  
{   
	private static Logger logger = Logger.getLogger(UploadFileProcessor.class);
	private static String STIF = "STIF";
	private static String DCM = "DCM";
	private static String LATEDCM = "Late DCM";
	private static String STIF_EXT = ".txt";
	private static String LATEDCM_EXT = "lt.txt";
	private static String DCM_EXT = ".csv";
	private static String BARGEDCM = "BARGEDCM";
	private static String BARGEDCM_EXT = ".txt";
	
	
	//Processes the Stif and DCM file  
	public static boolean processUpload(String fileName, String fileType, String group, byte[] data)throws TosException
	{
		// Set up logging for cas,not the right place for it but solves my urgent problem
		//com.matson.cas.common.util.LogProertyConfigure.configure();
		
		int ftpProxyId = 0;
		FtpProxySenderBiz sender = new FtpProxySenderBiz();
		int timeout = Integer.parseInt(TosRefDataUtil.getValue( "FTP_TIMEOUT"));
		logger.debug("FTP timeout retrieved is: " + timeout);
		sender.setTimeout(timeout);
		String fileNameFmt = null;
		String dtFormat = "yyMMddHHmmss";
		//logger.debug("File type="+fileType);
		
		try {
			//STIF Upload
			if(LATEDCM.equalsIgnoreCase(fileType)) {
				ftpProxyId = Integer.parseInt(TosRefDataUtil.getValue("DCM_IN_FTP_ID"));
				fileNameFmt = fileName+CalendarUtil.dateFormat(dtFormat)+"_"+LATEDCM_EXT;
				logger.debug("Formatted Late DCM File name :"+fileNameFmt+" proxy Id="+ftpProxyId);
				sender.sendFile(ftpProxyId, fileNameFmt, data);
				return true;
			} else if(STIF.equals(fileType))	
			{
				 ftpProxyId = Integer.parseInt( TosRefDataUtil.getValue( "STIF_FILES_FTP_ID"));
				 fileNameFmt = fileName+"_"+group+"_"+CalendarUtil.dateFormat(dtFormat)+"_SF"+STIF_EXT;
				 logger.debug("Formatted STIF File name :"+fileNameFmt);
				 sender.sendFile(ftpProxyId, fileNameFmt, data);
				 return true;
			}//DCM Upload
			else if(DCM.equals(fileType))
			{   
				ftpProxyId = Integer.parseInt(TosRefDataUtil.getValue("DCM_IN_FTP_ID"));
				fileNameFmt = fileName+CalendarUtil.dateFormat(dtFormat)+DCM_EXT;
				logger.debug("Formatted DCM File name :"+fileNameFmt+" proxy Id="+ftpProxyId);
				sender.sendFile(ftpProxyId, fileNameFmt, data);
				return true;
			 }
			else if(BARGEDCM.equals(fileType))
			{   
				ftpProxyId = Integer.parseInt(TosRefDataUtil.getValue("DCM_INS_FTP_ID"));
				fileNameFmt = "HAZ"+fileName+BARGEDCM_EXT;
				logger.debug("Formatted BARGE DCM File name :"+fileNameFmt+" proxy Id="+ftpProxyId);
				sender.sendFile(ftpProxyId, fileNameFmt, data);
				return true;
			 }
		}
		catch(Exception e)
		{	
			logger.error("Error In Processing Upload File", e);
			throw new TosException("Error in Upload Process"+e);
		}
		logger.debug("Unknown File Filename: "+fileName);
		return false;
	}
	
	//Processes the Stif and DCM file reading it from a given directory.
	public static boolean processUpload(String fileName, String fileType)throws TosException
	{
		int ftpProxyId = 0;
		int timeout = Integer.parseInt(TosRefDataUtil.getValue( "FTP_TIMEOUT"));
		logger.debug("FTP timeout retrieved is: " + timeout);
		FtpProxyGetterBiz getter = new FtpProxyGetterBiz();
		FtpProxySenderBiz sender = new FtpProxySenderBiz();				
		FtpProxyDeleterBiz del = new FtpProxyDeleterBiz();
		getter.setTimeout(timeout);
		sender.setTimeout(timeout);
		del.setTimeout(timeout);
		String fileNameFmt = null;
		String contents = null;
		String lstFilename = null;
		String enteredFileName = null;
		String dtFormat = "yyMMddHHmmss";
		int uploadDirProxyId = Integer.parseInt( TosRefDataUtil.getValue( "FTP_ID_UPLOAD"));
		FtpProxyListBiz list = new FtpProxyListBiz();
		list.setTimeout(timeout);
		try {
			String[] files = list.getFileNames(uploadDirProxyId,null,null);
			//STIF Upload
			if((files != null && files.length > 0) && STIF.equals(fileType))
			{
			  for(int i=0;i<files.length;i++)
			  {
				  lstFilename = files[i];
				  enteredFileName = fileName+STIF_EXT;
				  logger.debug("Stif EnteredFileName :"+enteredFileName+" lstFilename :"+lstFilename);
				  if(lstFilename.equalsIgnoreCase(enteredFileName))
				  {
					 ftpProxyId = Integer.parseInt( TosRefDataUtil.getValue( "VES_FILES_FTP_ID"));
					 contents = getter.getFileText(uploadDirProxyId, lstFilename);
					 fileNameFmt = fileName+"_"+CalendarUtil.dateFormat(dtFormat)+"_SF"+STIF_EXT;
					 logger.debug("Formatted File name :"+fileNameFmt);
					 sender.sendFile(ftpProxyId, fileNameFmt, contents);
					 del.removeFile(uploadDirProxyId, lstFilename);
					 return true;
				  }
			    
			  }
			}//DCM Upload
			else if((files != null && files.length > 0)&& DCM.equals(fileType))
			{   
			  for(int i=0;i<files.length;i++)
			  {
				
				  lstFilename = files[i];
				  enteredFileName = fileName+DCM_EXT;
				  logger.debug("Dcm EnteredFileName :"+enteredFileName+" lstFilename :"+lstFilename);
  				  if(lstFilename.equalsIgnoreCase(enteredFileName))
				  {
					ftpProxyId = Integer.parseInt(TosRefDataUtil.getValue("DCM_IN_FTP_ID"));
					contents = getter.getFileText(uploadDirProxyId,lstFilename );
					fileNameFmt = fileName+CalendarUtil.dateFormat(dtFormat)+DCM_EXT;
					logger.debug("Formatted File name :"+fileNameFmt);
					sender.sendFile(ftpProxyId, fileNameFmt, contents);
					del.removeFile(uploadDirProxyId, lstFilename );
					return true;
				   }
				}
			  
			 }
		}
		catch(Exception e)
		{	
			logger.error("Error In Processing Upload File", e);
			throw new TosException("Error in Upload Process"+e);
		}
		logger.debug("Unknown File Filename: "+fileName+" Specified File not in Upload Folder");
		return false;
	}

}




