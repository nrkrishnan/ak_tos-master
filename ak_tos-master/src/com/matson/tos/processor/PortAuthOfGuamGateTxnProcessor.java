/*
 * Purpose : To Post Port Authority of guam transaction Directly To Gems.
 * As right now its being manually entered by a user in Acets/Gems
 * 
 * Approach : PAOG is going to FTP a CSV Flat File to matson
 * TDP is going to Poll the FTP area and Post the Transaction to the Gems SAF and N4EventTOPIC     
 */
package com.matson.tos.processor;
import java.util.Date;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.log4j.Logger;

import com.matson.cas.service.ftp.FtpBizException;
import com.matson.cas.service.ftp.FtpProxyDeleterBiz;
import com.matson.cas.service.ftp.FtpProxyGetterBiz;
import com.matson.cas.service.ftp.FtpProxyListBiz;
import com.matson.tos.exception.TosException;
import com.matson.tos.jatb.GuamGateTxn;
import com.matson.tos.messageHandler.AbstractMessageHandler;
import com.matson.tos.messageHandler.GuamGateTxnMessageHandler;
import com.matson.tos.util.EmailSender;
import com.matson.tos.util.FileWriterUtil;
import com.matson.tos.util.JMSSender;
import com.matson.tos.util.JMSTopicSender;
import com.matson.tos.util.StrUtil;
import com.matson.tos.util.TosRefDataUtil;
import com.matson.tos.util.CalendarUtil;

public class PortAuthOfGuamGateTxnProcessor extends AbstractFileProcessor {
	
	private static Logger logger = Logger.getLogger(CmisToGemsFileProcessor.class);
	private static int timeout = Integer.parseInt(TosRefDataUtil.getValue( "FTP_TIMEOUT"));
	private int guamPortFtpProxyId = Integer.parseInt( TosRefDataUtil.getValue( "GUM_PORT_FTP_ID"));
	private String gumSupport = TosRefDataUtil.getValue("GUAM_SUPPORT_EMAIL");	
	private String supportMail = TosRefDataUtil.getValue("SUPPORT_EMAIL");
	private String MAIL_HOST = TosRefDataUtil.getValue( "MAIL_HOST");
	
	String guamGateTxnFtpFileName = null;
	private	GuamGateTxnMessageHandler guamTxnmsgHandler = null;
	private JMSTopicSender topicSender;
	private static String topic = TosRefDataUtil.getValue("TOPIC_N4");
	private String n4GemsSaf = TosRefDataUtil.getValue("N4_GEMS_SAF"); 
	private String guamGemsPstFlg = TosRefDataUtil.getValue("GUAM_GEMS_FLAG"); 
	public static String storeDir = "/home/logs/applogs/TOS/guamGateTxn";
	private static String eof = ">>END OF FILE<<";
	private static JMSSender gemsSaf = null; //A1
	private static String pat1 = "MMddyyyy HH:mm";

	
	public PortAuthOfGuamGateTxnProcessor(){
	 try{	
		 guamTxnmsgHandler = new GuamGateTxnMessageHandler("com.matson.tos.jaxb.snx", "com.matson.tos.jatb",
					"/xml/guamGateTxn.xml", AbstractMessageHandler.TEXT_TO_XML);

		 topicSender = new JMSTopicSender(topic);
	  }catch(Exception e){
		 e.printStackTrace();
	  }
	}
	
	public void processFiles() {
	  try
	  {
		gemsSaf = new JMSSender(n4GemsSaf,false);
		FtpProxyListBiz list = new FtpProxyListBiz();
		list.setTimeout(timeout);
		String[] guamPortTxnFiles = list.getFileNames(guamPortFtpProxyId,null,null);
		String dateStr = CalendarUtil.dateFormat(pat1);
		String convtDate = CalendarUtil.convertTimeZone(dateStr, pat1, "PST","HST");
		FtpProxyGetterBiz getter = new FtpProxyGetterBiz();
		getter.setTimeout(timeout);
		
		//GUAM PORT OF AUTHORITY GATE TXN FILES
		for ( int i=0; guamPortTxnFiles !=null && i<guamPortTxnFiles.length; i++) {
		   boolean processed = false;
		   guamGateTxnFtpFileName = guamPortTxnFiles[i];
		   String contents = getter.getFileText(guamPortFtpProxyId, guamGateTxnFtpFileName);
		   logger.debug("FileName="+guamGateTxnFtpFileName.toUpperCase()+"  contents="+contents.length()+"  eof="+eof);
		   //101- Change the File End With Check
		   if(guamGateTxnFtpFileName.toUpperCase().endsWith("CSV") &&
					(contents != null && contents.contains(eof))){ //A3
			    processFile(contents);
				FileWriterUtil.writeFile(contents, storeDir,"GumTxn",".csv");
				//Send Email to Gum
				EmailSender.mailAttachment(gumSupport, supportMail, MAIL_HOST, "GuamGate"+convtDate+"_HST.csv", contents, "Port of Guam Gate Transaction Data File Attached", "Port Of Guam Transaction Data File");
				processed = true;
				
			}else {
			   logger.debug( "The file name PAOG: " + guamGateTxnFtpFileName +" is not Completed and will be ignored.");
			}
			
		   if (processed) {  
			 // remove file that has been processed
			 logger.debug( "Deleting file PAOG: " + guamGateTxnFtpFileName);
			 FtpProxyDeleterBiz del = new FtpProxyDeleterBiz();
			 del.setTimeout(timeout);
			 del.removeFile(guamPortFtpProxyId, guamGateTxnFtpFileName);
		   }
		}
			
		} catch ( FtpBizException ftpEx) {
			logger.error( "FTP error found: ", ftpEx);
			ftpEx.printStackTrace();
			//101 - Add Email Code 
		} catch (Exception tex) {
			logger.error( "FTP error found: ", tex);
			tex.printStackTrace();
		} 
	}
	
	
	
	public void processFile(String contents)
	{
	  try{
		String[] lines = contents.split("\n");
		logger.debug( "There are " + lines.length + " lines in the file.");
		for (int i = 0; i < lines.length; i++) {
		   if(i == 0){
		 	continue; //Skip Header
		   }
		   processLine(lines[i], i+1);
		 }
		}catch(Exception e){
			logger.error("PAOG processFile Method ",e);
			//101 - Entire File Failed to Process
		}
	}
	
	protected void processLine(String msg, int lineNum) {
		try{
  		     guamTxnmsgHandler.setTextStr(msg);
			 GuamGateTxn guamgateTxn = (GuamGateTxn) guamTxnmsgHandler.getTextObj();
			 String recType = StrUtil.trimQuotes(guamgateTxn.getAction());
			 //Mail out INgate Full Transaction
			 if(recType != null && recType.trim().length() > 1){ 
			    String xml = guamTxnmsgHandler.createXmlStr();
			    if(xml != null){
			      logger.debug("POG xml:"+xml);
				  topicSender.send(xml);
				  if(Boolean.parseBoolean(guamGemsPstFlg)){ gemsSaf.send(xml); }
 			      //Pass HZU if Ingate has Haz flag
				  if(xml.contains("hazF='Y'") && xml.contains("action='IGT'")){
				   String temp = xml.replace("action='IGT'", "action='HZU'");
				   String hzuXml = temp.replace("lastAction='IGT'", "lastAction='HZU'");
				   topicSender.send(hzuXml);
				   logger.debug("POG xml:"+hzuXml);
				   if(Boolean.parseBoolean(guamGemsPstFlg)){ gemsSaf.send(hzuXml); }
				  }
			    }
			  }
			}catch(Exception e){
				logger.error(e);
				e.printStackTrace();
			}
	}
	
	
	/*
	 * A3 -- Objective : Read Guam Gate messages and post it to Gems. 
	 * 1. Lookup for FTP area
	 * 2. XML mapping file
	 * 3. Processor method  
	 */
	/*public void processGuamGateFile(String contents){ //A3
		try{
		String[] lines = contents.split("\n");
		logger.debug( "There are " + lines.length + " lines in the file.");
		 for (int i = 0; i < lines.length; i++) {
			if(i == 0){
				continue; //Skip Header
			}
		   guamTxnmsgHandler.setTextStr(lines[i]);
		   GuamGateTxn guamgateTxn = (GuamGateTxn) guamTxnmsgHandler.getTextObj();
			  String recType = StrUtil.trimQuotes(guamgateTxn.getAction());
			  //Mail out INgate Full Transaction
			  if(recType != null && recType.trim().length() > 1){ 
			    String xml = guamTxnmsgHandler.createXmlStr();
			    logger.debug("POG xml:"+xml);
			    topicSender.send(xml);
			    if(Boolean.parseBoolean(guamGemsPstFlg)){ gemsSaf.send(xml); }	
			  }
 		 }
		}catch(Exception e){
			logger.error(e);
			e.printStackTrace();
		}
	}*/
}
