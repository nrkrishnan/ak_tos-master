/*
*********************************************************************************************
* A1     02/25/08       Glenn Raposo		Changes made to Copy XML string to Temp Test File 
* A2     08/11/08       Glenn Raposo        File name is the Vesvoy code
* A3     10/09/08       Glenn Raposo        Added ProcessLoggerUtil Call   
* A4     01/04/11       Glenn Raposo        Adding Posting Delay to N4 Snx message   
* A5     02/17/11       Glenn Raposo        Added STIF pauses 
*********************************************************************************************
*/
package com.matson.tos.processor;

import java.io.StringWriter;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.log4j.Logger;

import com.matson.cas.refdata.mapping.TosProcessLoggerDAO;
import com.matson.cas.service.ftp.FtpBizException;
import com.matson.cas.service.ftp.FtpProxyDeleterBiz;
import com.matson.cas.service.ftp.FtpProxyGetterBiz;
import com.matson.cas.service.ftp.FtpProxyListBiz;
import com.matson.cas.service.ftp.FtpProxySenderBiz;
import com.matson.tos.exception.TosException;
import com.matson.tos.jatb.Stif;
import com.matson.tos.jaxb.snx.Snx;
import com.matson.tos.jaxb.snx.TUnit;
import com.matson.tos.messageHandler.AbstractMessageHandler;
import com.matson.tos.messageHandler.IMessageHandler;
import com.matson.tos.messageHandler.SnxMessageHandler;
import com.matson.tos.messageHandler.StifMessageHandler;
import com.matson.tos.util.EmailSender;
import com.matson.tos.util.FileWriterUtil;
import com.matson.tos.util.JMSSender;
import com.matson.tos.util.ProcessLoggerUtil;
import com.matson.tos.util.TosRefDataUtil;

public class StifFileProcessor extends AbstractFileProcessor {
	private static Logger logger = Logger.getLogger(StifFileProcessor.class);
	
	private IMessageHandler msgHandler;
	private JMSSender jmsSender;
	//A1 Starts
	//private StringWriter out = new StringWriter(); //A6
	private String FILE_NAME = "StifFile.xml";
	private String carrierId = null;
	private String group = null;
	private String PROCESS_STIF = "STIF";
	private static int timeout = Integer.parseInt(TosRefDataUtil.getValue( "FTP_TIMEOUT"));
	private String MAIL_HOST = TosRefDataUtil.getValue( "MAIL_HOST");
	private String supportMail = TosRefDataUtil.getValue("SUPPORT_EMAIL");

	//A1 Ends
	//A4
	private int postingDelay = 1;
		
	String ftpFileName = null;
	private Snx snxFtpObj = null;  //A6 -SNXMAIL BOX
	public static String storeDir = "/home/logs/applogs/TOS/snxmailbox";
	private TUnit aUnit = null;
	int SNX_FTP_ID = 0;
	boolean postToSnxMailBox;
	boolean postNewvesToQueue;
	FtpProxySenderBiz ftpSender = null;
	
	public StifFileProcessor() {
		try {
			msgHandler = new StifMessageHandler( "com.matson.tos.jaxb.snx",
				"com.matson.tos.jatb", "/xml/stif.xml", AbstractMessageHandler.TEXT_TO_XML);
			 
			jmsSender = new JMSSender( JMSSender.BATCH_QUEUE, "HON");

		} catch ( TosException tex){
			tex.printStackTrace();
			logger.error( "Error in creating the object: ", tex);
		}
	}
	public void processFiles() {
		// TODO Auto-generated method stub
		
		int ftpProxyId = Integer.parseInt( TosRefDataUtil.getValue( "STIF_FILES_FTP_ID"));
		try {
			//A4
			try {
			   postingDelay = Integer.parseInt(TosRefDataUtil.getValue( "STIF_PAUSE"));
			} catch (Exception e) {}
			
			FtpProxyListBiz list = new FtpProxyListBiz();
			list.setTimeout(timeout);
		    String[] files = list.getFileNames(ftpProxyId,null,null);
		    logger.debug(  "Total "+ files.length + " number of files/dir are found on the ftp dir.");
			FtpProxyGetterBiz getter = new FtpProxyGetterBiz();
			getter.setTimeout(timeout);
			FTPFile aFile = null;
			
			for ( int i=0; i<files.length; i++) {
				boolean processed = false;
				//aFile = files[i];
					ftpFileName = files[i];
					logger.debug( "Processing file: " + ftpFileName);
					String contents = getter.getFileText(ftpProxyId, ftpFileName);
					if ( ftpFileName.toUpperCase().endsWith("SF.TXT")) {
						processFile( contents);
						processed = true;

					} else {
						logger.debug( "The file name: " + ftpFileName + 
								" is not STIF file and will be ignored.");
					}
					if (processed) {  
						// remove file that has been processed
						logger.debug( "Deleting file: " + ftpFileName);
						FtpProxyDeleterBiz del = new FtpProxyDeleterBiz();
						del.removeFile(ftpProxyId, ftpFileName);
					}
			}
			
		} catch ( FtpBizException ftpEx) {
			logger.error( "FTP error found: ", ftpEx);
			ftpEx.printStackTrace();
		} 
	}

	protected void processFile( String fileContents) {
		String aLine;
		int lineNum =0;
		boolean nevVesFlag = false;
		snxFtpObj = new Snx();
		ProcessLoggerUtil processLogUtil = new ProcessLoggerUtil();
		try {
			//A43 - Started
			SNX_FTP_ID = Integer.parseInt(TosRefDataUtil.getValue("NV_SNX_MAILBOX"));
			postToSnxMailBox = Boolean.parseBoolean(TosRefDataUtil.getValue("SNX_MAILBOX_FTP"));
			postNewvesToQueue = Boolean.parseBoolean(TosRefDataUtil.getValue("POST_NEWVES_TO_QUE"));
			int timeout = Integer.parseInt(TosRefDataUtil.getValue( "FTP_TIMEOUT"));
			ftpSender = new FtpProxySenderBiz();
			ftpSender.setCreateSubDirectory(true);
			logger.debug("FTP timeout retrieved is: " + timeout);
			ftpSender.setTimeout(timeout);
			// read from a string
			//BufferedReader in = new BufferedReader(new FileReader( fileName));
			// asume all stif files have more than 5 lines.
			String lines[] = fileContents.split( "\n");
			if ( lines.length < 5)
				lines = fileContents.split( "\r");
			if ( lines.length < 5)
				lines = fileContents.split( "\r\n");
			
			logger.debug( "There are " + lines.length + " lines in the file.");
			if ( lines.length < 5) {
				logger.error( "STIF file must have at least 6 lines in the file.");
				logger.debug( "File content(first 2000 chars): " + fileContents.substring(0, 2000));
				return;
			}
			aLine = lines[lineNum];
			lineNum++;
			aLine = lines[lineNum];
			lineNum++;
			// the second line contains some routing info
			getCarrierId();
			if(carrierId != null){
			  //Verify Execution method
			  TosProcessLoggerDAO processLogger = new TosProcessLoggerDAO();
			  nevVesFlag = processLogger.verifyProcessExecution(carrierId, "NV");
			  if(nevVesFlag){
				  String errorSub = "NewVes Data has been executed for this vesvoy thus the StowPlan Data cannot be executed";
				  EmailSender.mailAttachment(supportMail,supportMail,MAIL_HOST, "STIF_RERUN_ERROR.TXT", fileContents,errorSub,"Stow Plan Data ReRun Error" );
				  throw new Exception(errorSub); 
			  }
			   processLogUtil.recordProcessDetails(1,carrierId,PROCESS_STIF,"In-Process");
			}

			((StifMessageHandler)msgHandler).setCarrierId( carrierId);
			((StifMessageHandler)msgHandler).setGroupCoding( group);
			// find the start line for the data
			//TODO: delete the 30 records limit
			while ( ! aLine.startsWith( "* VANS") && lineNum <30) {
				aLine = lines[lineNum];
				lineNum++;
			}
			logger.debug( "Satrt process from line: " + lineNum);
			// this is a title line. STIF has special requirement regarding the column sequence.
			// The sequence may change from file to file but all column title will be a subset of
			// definitions in abob.xml
			aLine = lines[lineNum-2];
			((StifMessageHandler)msgHandler).setTitleLine( aLine);
			
			// get the first line of data
			aLine = lines[lineNum];
			lineNum++;
			while ( ! aLine.startsWith( "*")) {
				if ( isDataLine( aLine)) 
					processLine( aLine, lineNum);
				aLine = lines[lineNum];
				lineNum++;
			}
			//Updated the Stif Process in Logger table
			if(carrierId != null){	
			   processLogUtil.recordProcessDetails(0,carrierId,PROCESS_STIF,"Processed");
			}
			
			/*if ( xmlTestFlag != null) {
				//A1 change starts 
				out.close();
				ExportXmlData.copyXmlToFile(FILE_NAME,out.toString(),xmlTestFlag);
				//A1 change ends
			}*/ //Stop WRitting copy of file
			//A6 - SNXMAIL BOX
			if(postToSnxMailBox){
			  sendftp(snxFtpObj);
			}
			
		} catch ( Exception ex) {
			ex.printStackTrace();
			logger.debug( "The error line num = " + lineNum);
			logger.error( "Exception: ", ex);
		}
		finally{
			snxFtpObj = null;
		}
	}
		
	protected void processLine( String lineText, int lineNum) {
		String xml;
		try {
			msgHandler.setTextStr(lineText);
			//xml = msgHandler.getXmlStr();
			//msgHandler.setXmlStr(xml);
			Snx aSnxUnit = (Snx)msgHandler.getXmlObj();
			
			//Added Check to Stop Posting to Gems Queue
			if(postNewvesToQueue){
			  xml = msgHandler.getXmlStr();
			  logger.debug( "XML message("+postingDelay+"): " + xml); //A4
			  Thread.sleep(postingDelay);
			  jmsSender.send( xml);
		    }
			//A6- SnxMailBox Change
			snxFtpObj.getUnit().add(aSnxUnit.getUnit().get(0));
			
			
			
			/*if ( xmlTestFlag != null ) {
				//A1 change starts
				out.write( xml);
				out.write( "\r\n");
				//A1 change ends
			}*/ //A6 Stop writing copy of file 
		} catch ( TosException jex) {
			logger.debug( "The error line num = " + lineNum);
			logger.debug( "The error line = " + lineText);
			logger.error( "Exception: ", jex);
		} catch ( Exception ex) {
			logger.debug( "The error line num = " + lineNum);
			logger.debug( "The error line = " + lineText);
			logger.error( "Exception: ", ex);
		}
	}
	
	private boolean isDataLine( String aLine) {
		boolean ret = false;
		String[] tokens = aLine.split("\t");
		if ( tokens.length > 3) {
			if ( tokens[1].equalsIgnoreCase("RS") ||
					tokens[1].equalsIgnoreCase("AUTO") ||
					tokens[1].equalsIgnoreCase("RORO"))
				ret = false;
			else 
				ret = true;
		} 
		return ret;
	}
	//A2 - Change
	private String[] getCarrierId() {
		String[] data = null;
		if ( ftpFileName != null && ftpFileName.indexOf("_") != -1){
		data = ftpFileName.split("_");
			carrierId = data[0];
			group = data[1];
			                      
		}
        else {
			logger.error( "Naming Format Error : File name is not in the expected format");
		}
		logger.debug( "The carrier ID = " + carrierId);
		return data;
	}
	
	//A43
	private void sendftp(Snx snxObj) {
		// turned off ftp for now.
		// turn it on 2.1
        int ftpId = 0; 
        String fileName = null;
        
		try {
			msgHandler.setXmlObj(snxObj);
			String xmlStr = msgHandler.getXmlStr();
			if(xmlStr == null){
				logger.debug( "-- STIF XML IS NULL -- ");
				return;
			}
			ftpId = SNX_FTP_ID; 
			fileName = carrierId+"_STIF_";
			FileWriterUtil.writeFile(xmlStr, storeDir,fileName,".xml");
			ftpSender.sendFile(ftpId, fileName+".xml", xmlStr.getBytes(), true, null);
			
		} catch (TosException tex) {
			logger.error("Tos exception found: ", tex);
			tex.printStackTrace();
		}catch (Exception ex) {
			logger.error("Exception found: ", ex);
			ex.printStackTrace();
		}
	}

	
	/*
	public static void main( String[] args) {
		StifFileProcessor proc = new StifFileProcessor();
		//proc.processFiles();
		try {
			BufferedReader reader = new BufferedReader( new InputStreamReader( new FileInputStream("c:/temp/stif_with_diff_columns.txt")));
			String aLine;
			String content = "";
			while (( aLine = reader.readLine()) != null) {
				content = content + aLine + "\n";
			}
			proc.processFile( content);
			//Writer out= new BufferedWriter(new OutputStreamWriter( new FileOutputStream("c:\\temp\\stifData.xml")));
			//logger.debug( proc.sbuf.toString());
			//out.write( proc.sbuf.toString());
			//out.close();
		} catch ( FileNotFoundException fex) {
			fex.printStackTrace();
		} catch ( IOException ioex) {
			ioex.printStackTrace();
		}
	} 
	*/
	
/*	public static void main( String[] args) 
	{
		try
		{
			File file = new File("C:/TEST_DIR_ALL/Dataload/STIF_TEST/part1/MHI147_SF.TXT");
			if(file != null)
			{
			  System.out.println("File :: "+file);
			}
			BufferedReader br = new BufferedReader(new FileReader(file));
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = br.readLine()) != null) 
			{
				sb.append(line + "\n");
			}
				br.close();
				String fileStr = sb.toString();
				StifFileProcessor stifProcess = new StifFileProcessor();
				System.out.println("Before processFile(fileStr)");
				stifProcess.processFile(fileStr);
				System.out.println("After processFile(fileStr)");
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
*/
}
