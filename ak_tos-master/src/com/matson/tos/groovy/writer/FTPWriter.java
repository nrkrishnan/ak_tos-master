package com.matson.tos.groovy.writer;

import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;


import com.matson.cas.service.ftp.FtpProxyDeleterBiz;
import com.matson.cas.service.ftp.FtpProxySenderBiz;
import com.matson.cas.service.ftp.FtpProxyListBiz;
import com.matson.cas.service.ftp.FtpProxyGetterBiz;
import com.matson.tos.util.CalendarUtil;
import com.matson.tos.util.EmailSender;
import com.matson.tos.processor.IFileProcessor;
import com.matson.tos.util.TosRefDataUtil;


/**
 *  * 3/25/2013  Removing SingletonService, if it is going to run clustered it needs to be replaced.
 *
 */
public class FTPWriter implements ObjectWriter, IFileProcessor {
	public void activate() {
		// TODO Auto-generated method stub
		
	}

	public void deactivate() {
		// TODO Auto-generated method stub
		
	}

	private static Logger logger = Logger.getLogger(ParadoxWriter.class);

	private Map _data;
	private static FTPWriter _instance = null;
	
	// N4 and CMIS Data Destination Folders
	
	private int ftpToN4Dest = 0;
	private int ftpToCmisDest = 0;
	
	private String MAIL_HOST = TosRefDataUtil.getValue("MAIL_HOST");
	private String emailAddr = TosRefDataUtil.getValue("SUPPORT_EMAIL");
	// A1
	private static final String FTP = "FTP";
	private static final String WRITE = "WRITE";

	private static int timeout = Integer.parseInt(TosRefDataUtil.getValue( "FTP_TIMEOUT"));
	public static synchronized FTPWriter getInstance() {
		if ( _instance == null) {
			_instance = new FTPWriter();
		}
		return _instance;
	}
	private void init() {
		try {
			String value = TosRefDataUtil.getValue("FTP_ID_N4");
			logger.debug("FTP_ID_N4="+value);
			ftpToN4Dest =   Integer.parseInt(TosRefDataUtil.getValue("FTP_ID_N4"));
			ftpToCmisDest = Integer.parseInt(TosRefDataUtil.getValue("FTP_ID_CMIS"));
		} catch (Exception e) {
			logger.error("Error in process msg type data init", e);
		}
	}
	// Constructor for GroovyMessageProcessor
	private FTPWriter(Map data) {
		init();
		_data = data;
	}

	// Constructor for TosJobScheduler
	private FTPWriter() {
		init();
	}

	public void setData( Map data) {
		_data = data;
	}
	// TosJobScheduler - TimeBound FTPing
	public void processFiles() {
		processFTPData(FTP, "");
	}

	// GroovyMsgProcessor - FTP Append record
	public boolean write() {
		String msgType = null;
		boolean ret = false;
		try {
			msgType = (String) _data.get(ObjectWriterFactory.MSG_TYPE);

			MessageConfig instance = MessageConfig.getInstance();
			String fileName = instance.getFileName(msgType);
			Map<String, FieldData> fields = instance.getFields(msgType);
			if(fields == null){
				throw new Exception("Mapping XML Null:Check Event Entry in MessageList.XMl");
			}
			String txtStr = createFixedLenTxtStr(fileName, fields);
			logger.debug("Formatted String Rec: " + txtStr);

			ret = processFTPData(WRITE, txtStr);

		} catch (Exception ex) {
			logger.error("Error in process msg type" + msgType, ex);
		}
		return ret;

	}

	/*
	 * Based on Function call method either Appends data to FTP file or Uploads
	 * FTP File
	 */
	private synchronized boolean processFTPData(String processFunction,	String msgRec) {
		String ftpFileName = null;
		String filedata = null;
		String dtFormat = "yyMMddHHmmssSSS";
		String contents = null;
		String date = CalendarUtil.dateFormat(dtFormat);
		FtpProxySenderBiz sender = new FtpProxySenderBiz();
		sender.setTimeout(timeout);
		FtpProxyDeleterBiz del = new FtpProxyDeleterBiz();
		del.setTimeout(timeout);
		FtpProxyGetterBiz getter = new FtpProxyGetterBiz();
		getter.setTimeout(timeout);
		FtpProxyListBiz list = new FtpProxyListBiz();
		list.setTimeout(timeout);
		boolean fileFound = false;
		
		
		try {
			logger.debug( "Start processFTPData() .............");
			String[] files = list.getFileNames(ftpToN4Dest,null,null);
			// TosJobScheduler - Files for Upload
			if (files != null && processFunction.equals(FTP)) {
				logger.debug("files.length:" + files.length);
				for (int i = 0; i < files.length; i++) {
					logger.debug( "File name = " + files[i]);

					ftpFileName = files[i];
					logger.debug("File Length " + files.length + " FTP File: " + ftpFileName);
					contents = getter.getFileText(ftpToN4Dest, ftpFileName);
					//sender.sendFile(ftpToCmisDest, ftpFileName, contents);
					sender.sendFile(ftpToCmisDest, ftpFileName, contents.getBytes(), true, null);
					del.removeFile(ftpToN4Dest, ftpFileName);
					
				}
				logger.debug( "End processFTPData() .............");
				return true;
			}

			// Groovy Processor Write - File to be appended
			if (files != null && processFunction.equals(WRITE)) {
				logger.debug("process Upload writer:");
				for (int i = 0; i < files.length; i++) {

						ftpFileName = files[i];
						logger.debug("ftpFileName: " + ftpFileName);
						// Check for Failed Files & Uploads
						if ( fileFound || ftpFileName.startsWith("FAIL")) {
							contents = getter.getFileText(ftpToN4Dest, ftpFileName);
							//sender.sendFile(ftpToCmisDest, ftpFileName,	contents);
							sender.sendFile(ftpToCmisDest, ftpFileName, contents.getBytes(), true, null);
							
							del.removeFile(ftpToN4Dest, ftpFileName);
						}
						// Checks for MSG files and Appends
						else if (ftpFileName.startsWith("MSG")) {
							fileFound = true;
							logger.debug("MSG File: " + ftpFileName);
							int recThreshold = Integer.parseInt(TosRefDataUtil.getValue("MAX_MESSAGE_TO_SEND"));
							contents = getter.getFileText(ftpToN4Dest, ftpFileName);
							String[] recNum = contents.split("\n");
							filedata = contents.concat("\n" + msgRec);
							// Appends last Record and FTP's File
							if (recNum.length >= recThreshold - 1) {
								logger.debug("RecNum == Limit-1 " + recNum.length + "FileName : " + ftpFileName);
								//sender.sendFile(ftpToCmisDest, ftpFileName,	filedata);
								sender.sendFile(ftpToCmisDest, ftpFileName, filedata.getBytes(), true, null);
								
								del.removeFile(ftpToN4Dest, ftpFileName);
							}
							// Appends Records if length is below max limit
							else {
								logger.debug("RecNum < Limit " + recNum.length + "FileName : " + ftpFileName);
								//sender.sendFile(ftpToN4Dest, ftpFileName, filedata);
								sender.sendFile(ftpToN4Dest, ftpFileName, filedata.getBytes(), true, null);
							}
						}
						//return true;
				}// for Ends
				if (!fileFound) {
					ftpFileName = "MSG" + date + ".TXT";
					logger.debug("New File Created " + ftpFileName);
					//sender.sendFile(ftpToN4Dest, ftpFileName, msgRec);
					sender.sendFile(ftpToN4Dest, ftpFileName, msgRec.getBytes(), true, null);
					
					logger.debug( "End processFTPData() .............");
					return true;
				}
			}// else if ends

			if (files == null && processFunction.equals(WRITE)) {
				ftpFileName = "MSG" + date + ".TXT";
				logger.debug("Files=NULL : New File Created " + ftpFileName);
				//sender.sendFile(ftpToN4Dest, ftpFileName, msgRec);
				sender.sendFile(ftpToN4Dest, ftpFileName, msgRec.getBytes(), true, null);
				
				logger.debug( "End processFTPData() .............");
				return true;
			}
			logger.debug("No condition matched.");
			logger.debug( "End processFTPData() .............");
			return true;
		}// try ends
		catch (Exception e) {
			logger.error("Error in FTP sending", e);
			try {
				// Error file Renamed in FTP Folder and Emailed to Support Group
				//sender.sendFile(ftpToN4Dest, "FAIL_" + ftpFileName, filedata);
				sender.sendFile(ftpToN4Dest, "FAIL_" + ftpFileName, filedata.getBytes(),true,null);

				EmailSender.mailAttachment(emailAddr, emailAddr, MAIL_HOST,	ftpFileName, filedata,
								"Error while writting/Uploading Data File \n" + e,
								"Error In writting/Uploading N4 transaction data on the server");
			} catch (Exception ex) {
				logger.error("Renamed Error FTP File and Sent out Mail", e);
			}
		}
		logger.debug( "End processFTPData() .............");
		return false;
	}

	/*
	 * Method Creates a Fixed Length Format Text String
	 */
	private String createFixedLenTxtStr(String fileName,
			Map<String, FieldData> fields)throws Exception {

		StringBuffer bufValue = new StringBuffer();
		String formattedRec = null;
		try {
			Iterator it = fields.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry entryField = (Map.Entry) it.next();
				FieldData fieldData = (FieldData) entryField.getValue();
				String fieldName = fieldData.getFieldName();
				int seqNumber = fieldData.getSequenceNum();
				int fieldSize = fieldData.getFieldSize();

				String fieldValue = _data.get(fieldName) != null ? (String) _data.get(fieldName) : "";
				String fixStrValue = fixLengthString(fieldValue, fieldSize);
				bufValue.append(fixStrValue);
				//logger.debug("SEQ NUM: " + seqNumber + "  FILE NAME : " + fieldName);
			}
			if (bufValue.length() > 1)
				formattedRec = bufValue.substring(0, bufValue.length() - 1);

		} catch (Exception e) {
			logger.error("Error in creating FixLength String : " + e);
			throw new Exception(e);
		}
		return formattedRec;
	}

	// Method Appends
	private static String fixLengthString(String fillChar, int count)
	{
		char[] chars = null;
		chars = new char[count];
		char[] strChars = fillChar.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if (i < strChars.length) {
				chars[i] = strChars[i];
			} else {
				chars[i] = ' ';
			}
		}
	 return new String(chars);
	}
}
