/*
 * Work left 
 * Add Scheduler Task 
 * Map Cmis Area in CAS
 * 101 - Things to DO
 * Added Rec 
 */

/*
 * Srno   Date			AuthorName		Change Description
 * A1     01/11/2011     GR             Post messages Gems SAF agent   
 * A2     01/19/11       GR 			N4 to Gems posting Flag
 * A3     08/26/11   	 GR 			Handle NIZ Action for MNS and Gems posting 
*/
package com.matson.tos.processor;
import com.matson.tos.exception.TosException;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.log4j.Logger;

import com.matson.cas.service.ftp.FtpBizException;
import com.matson.cas.service.ftp.FtpProxyDeleterBiz;
import com.matson.cas.service.ftp.FtpProxyGetterBiz;
import com.matson.cas.service.ftp.FtpProxyListBiz;
import com.matson.tos.jatb.NewVes;
import com.matson.tos.messageHandler.AbstractMessageHandler;
import com.matson.tos.messageHandler.CmisToGemsMessageHandler;
import com.matson.tos.util.JMSSender;
import com.matson.tos.util.JMSTopicSender;
import com.matson.tos.util.StrUtil;
import com.matson.tos.util.TosRefDataUtil;
import java.io.*;
import java.util.Map;
import java.util.HashMap;
import com.matson.tos.util.CalendarUtil;

import com.matson.tos.jatb.CmisToGems;

public class CmisToGemsFileProcessor extends AbstractFileProcessor {
	
	private static Logger logger = Logger.getLogger(CmisToGemsFileProcessor.class);
	private static int timeout = Integer.parseInt(TosRefDataUtil.getValue( "FTP_TIMEOUT"));
	private int guamFtpProxyId = Integer.parseInt( TosRefDataUtil.getValue( "GVPQ_FILES_FTP_ID"));
	private int honFtpProxyId = Integer.parseInt( TosRefDataUtil.getValue( "HON_FILES_FTP_ID"));
	
	String guamFtpFileName = null;
	String honFtpFileName = null;
	private	CmisToGemsMessageHandler msgHandler = null;
	private JMSTopicSender topicSender;
	private static String topic = TosRefDataUtil.getValue("TOPIC_N4");
	private String n4GemsSaf = TosRefDataUtil.getValue("N4_GEMS_SAF"); //A1
	private String gemsPostingFlg = TosRefDataUtil.getValue("N4_GEMS_FLAG");
	private static String eof = ">>END OF FILE<<";
	private static JMSSender gemsSaf = null; //A1
	private boolean n4GemsFlag = false;
    //A3 - Change for MNS	
	private String n4MnsSaf = null; //A1
	private String mnsPostingFlg = null;
	private static JMSSender mnsSaf = null; 
	private boolean n4mnsFlag = false;
	private static String dateFormat = "yyyy-MM-dd'T'HH:mm:ss";
	
	public CmisToGemsFileProcessor(){
	 try{	
		 n4MnsSaf = TosRefDataUtil.getValue("N4_MNS_NIZ_SAF"); //A1
		 mnsPostingFlg = TosRefDataUtil.getValue("N4_MNS_NIZ_FLAG");
		 
		 msgHandler = new CmisToGemsMessageHandler("com.matson.tos.jaxb.snx", "com.matson.tos.jatb",
			"/xml/cmisToGems.xml", AbstractMessageHandler.TEXT_TO_XML);

		 topicSender = new JMSTopicSender(topic);
	  }catch(Exception e){
		 e.printStackTrace();
	  }
	}
	
	
	public void processFiles() {
	  try
	  {
		gemsSaf = new JMSSender(n4GemsSaf, false);
		n4GemsFlag = Boolean.parseBoolean(gemsPostingFlg);
		mnsSaf = new JMSSender(n4MnsSaf, false);
		n4mnsFlag = Boolean.parseBoolean(mnsPostingFlg);
		
		FtpProxyListBiz list = new FtpProxyListBiz();
		list.setTimeout(timeout);
		String[] guamFiles = list.getFileNames(guamFtpProxyId,null,null);
		String[] honFiles = list.getFileNames(honFtpProxyId,null,null);
		
		FtpProxyGetterBiz getter = new FtpProxyGetterBiz();
		getter.setTimeout(timeout);
		FTPFile aFile = null;
		//GUAM FILES
		for ( int i=0; guamFiles !=null && i<guamFiles.length; i++) {
		   boolean processed = false;
		   guamFtpFileName = guamFiles[i];
		   logger.debug( "Processing file: " + guamFtpFileName);
		   String contents = getter.getFileText(guamFtpProxyId, guamFtpFileName);
		   //101- Change the File End With Check
		   if ( guamFtpFileName.toUpperCase().endsWith("TXT") &&
				   (contents != null && contents.contains(eof))) {
			   processFile( contents);
			   processed = true;
			} else {
			   logger.debug( "The file name: " + guamFtpFileName +" is not GVPQ file OR not Completed and will be ignored.");
			}
			
		   if (processed) {  
			 // remove file that has been processed
			 logger.debug( "Deleting file: " + guamFtpFileName);
			 FtpProxyDeleterBiz del = new FtpProxyDeleterBiz();
			 del.setTimeout(timeout);
			 del.removeFile(guamFtpProxyId, guamFtpFileName);
		   }
		}
		
		//HON FILES
		for ( int i=0; honFiles != null && i<honFiles.length; i++) {
			   boolean processed = false;
			   honFtpFileName = honFiles[i];
			   logger.debug( "Processing file: " + honFtpFileName);
			   String contents = getter.getFileText(honFtpProxyId, honFtpFileName);
			   //101- Change the File End With Check
			   if ( honFtpFileName.toUpperCase().endsWith("TXT") &&
					   (contents != null && contents.contains(eof))) {
				   processFile( contents);
				   processed = true;
				} else {
				   logger.debug( "The file name: " + honFtpFileName +" is not HON file OR not Completed and will be ignored.");
				}
				
			   if (processed) {  
				 // remove file that has been processed
				 logger.debug( "Deleting file: " + honFtpFileName);
				 FtpProxyDeleterBiz del = new FtpProxyDeleterBiz();
				 del.setTimeout(timeout);
				 del.removeFile(honFtpProxyId, honFtpFileName);
			   }
			}
		
			
		} catch ( FtpBizException ftpEx) {
			logger.error( "FTP error found: ", ftpEx);
			ftpEx.printStackTrace();
			//101 - Add Email Code 
		} catch ( Exception tex) {
			logger.error( "FTP error found: ", tex);
			tex.printStackTrace();
		} 
	}
	
	public void processFile(String contents){
		try{
		String[] lines = contents.split("\n");
		logger.debug( "There are " + lines.length + " lines in the file.");
		for (int i = 0; i < lines.length; i++) {
		   processLine(lines[i], i+1);
		}
		
		}catch(Exception e){
			logger.error("processFile :"+e);
			//101 - Entire File Failed to Process
		}
	
		
	}
	
	protected void processLine(String msg, int lineNum) {
	  try{
		  msgHandler.setTextStr(msg);
		  CmisToGems cmisToGems = (CmisToGems) msgHandler.getTextObj();
		  String recType = StrUtil.trimQuotes(cmisToGems.getRectype());
		  String temp = cmisToGems.getRectype() != null ? cmisToGems.getRectype().substring(1) : null ;
		  if("NIZ".equals(temp)){
			  populateNIZ(cmisToGems); 
		  }else if(recType != null && recType.trim().length() > 1){ 
		    String xml = msgHandler.createXmlStr();
		    logger.debug("Cmis xml:"+xml);
		    topicSender.send(xml);
		    if(n4GemsFlag){ gemsSaf.send(xml); }	//A1,A2
		  }
	  }catch(Exception e){
		logger.error("processLine :"+e);
		e.printStackTrace();
		//Add Email Code - Processing GVPQ REC Failed
	  }
	}
	
	/* 
	 * Method To post NIZ (newves barge coding comepltion notice to Gems and MNS)
	 * A3 Change  
	 */
	
    private void populateNIZ(CmisToGems cmisToGems) {
       //Gems - <GroovyMsg msgType='NIZ_COMPLETE'  action='NIZ'   aDate='08/26/2011' aTime='08:43:58' doer='tos' vesvoy='ALE134B'  dPort='HIL'/>
       //MNS - <new-vessel source-system="TOS" vessel-voyage="ALE019" port-code="KHI" status="COMPLETE"/>
      //1. Create Gems Data Mapping 
      try
      {
    	Map<String, String> data = new HashMap<String, String>(); 
    	String dataElement1 = StrUtil.trimQuotes(cmisToGems.getDataElement1());
		String port = StrUtil.trimQuotes(cmisToGems.getUser());
		String date = CalendarUtil.dateFormat(dateFormat);
		
		data.put( "msgType", "NIZ_COMPLETE");
		data.put( "action", "NIZ");
		data.put( "aDate", StrUtil.trimQuotes(cmisToGems.getDate()));
		data.put( "aTime", StrUtil.trimQuotes(cmisToGems.getTime()));
		
		data.put( "doer", "TOS");
		String vesvoy = dataElement1 != null ? dataElement1.trim() : null ;
		if(vesvoy == null){ return; }
		data.put( "vesvoy", vesvoy);
		data.put( "dPort", port);
		String gemsMsg = msgHandler.createGvyMsg(data);
		
		//2. Create MNS Data mapping
		String mnsMsg = "<new-vessel source-system='TOS' vvd='"+vesvoy+"' port-code='"+port+"' date-time='"+date+"' status='COMPLETE'/>";
        
		//3. Post Message to Gems and Msg
		logger.debug("GemsNIZMsg="+gemsMsg);
		logger.debug("MnsNIZMsg="+mnsMsg);
		
		if(n4GemsFlag){ gemsSaf.send(gemsMsg); }
		if(n4mnsFlag){ mnsSaf.send(mnsMsg); }
		
      }catch(Exception e){
    	logger.error("processLine :"+e);
        e.printStackTrace();
      }

	}
	
/*	
	public static void main(String args[]){
		GvpqFileProcessor proc = new GvpqFileProcessor();
		//proc.processFiles();
		try {
			BufferedReader reader = new BufferedReader( new InputStreamReader( new FileInputStream("C:/Documents and Settings/Gxr/Desktop/GVPQ/GVPQ.TXT")));
			String aLine;
			String content = "";
			while (( aLine = reader.readLine()) != null) {
				//content = content + aLine + "\n";
				System.out.println("Read line :"+content+aLine);
				proc.processLine(content+aLine, 0);
			}
			//proc.processFile( content);
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
 
}
