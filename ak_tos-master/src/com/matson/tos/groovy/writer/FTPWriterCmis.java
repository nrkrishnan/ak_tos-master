package com.matson.tos.groovy.writer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import java.io.IOException;
import com.matson.cas.service.ftp.FtpProxySenderBiz;
import com.matson.tos.util.CalendarUtil;
import com.matson.tos.util.EmailSender;
import com.matson.tos.processor.IFileProcessor;
import com.matson.tos.util.TosRefDataUtil;

/**
 * Changed to local reader instead of FTP.
 * 3/25/2013  Removing SingletonService, if it is going to run clustered it needs to be replaced.
 * @author skb
 *
 */
public class FTPWriterCmis implements ObjectWriter, IFileProcessor {
	public void activate() {
		// TODO Auto-generated method stub
	}

	public void deactivate() {
		// TODO Auto-generated method stub
	}

	private static Logger logger = Logger.getLogger(ParadoxWriter.class);

	@SuppressWarnings("unchecked")
	private Map _data;
	
	private static FTPWriterCmis _instance = null;
	
	// N4 and CMIS Data Destination Folders
	private int ftpToCmisDest = 0;
	private String MAIL_HOST = TosRefDataUtil.getValue("MAIL_HOST");
	private String emailAddr = TosRefDataUtil.getValue("SUPPORT_EMAIL");


	private static final String FTP = "FTP";
	private static final String WRITE = "WRITE";
	private String cmisRelatedAction = null;
    //Mapping File Entries
	private static final String CMIS_UNIT_FIELDS = "CmisFields";
	private static final String CMIS_VESVISIT_FIELDS = "CmisVesVisitFields";
	private static final String CMIS_CHASSIS_FIELDS = "CmisChassisFields";
	private static final String CMIS_ACRY_FIELDS = "CmisAcryFields";
	private static final String CMIS_TRUCKVISIT_FIELDS = "CmisTruckVisitFields";
	private static int timeout = Integer.parseInt(TosRefDataUtil.getValue( "FTP_TIMEOUT"));
	
	private String unitClass = "";
	private boolean firstCheck = true;
	
	private java.util.Vector<String> messageQueue = new java.util.Vector<String>();
	private FTPWriterThread sender;
	private String logDir = "/home/logs/applogs/cas/";
	private String storeDir = "/home/logs/applogs/TOS/ftp";
	private String dateFormat = "yyyyMMdd";
	private String yearFmt = "";
	private boolean running = true;
	
	protected void finalize() throws Throwable {
		running = false;
		try {
			sender.interrupt();
		} catch (Exception e) {}
		sender = null;		
	}
	
	private void init() {
		try {
			String value = TosRefDataUtil.getValue("FTP_ID_N4");
			logger.debug("FTP_ID_N4="+value);
			ftpToCmisDest = Integer.parseInt(TosRefDataUtil.getValue("FTP_ID_CMIS"));
			sender = new FTPWriterThread();
			sender.setDaemon(true);
			sender.start();
		} catch (Exception e) {
			logger.error("Error in process msg type data init", e);
		}
	}
	
	public static synchronized FTPWriterCmis getInstance() {
		if ( _instance == null) {
			_instance = new FTPWriterCmis();
		}
		return _instance;
	}
	// Constructor for GroovyMessageProcessor
	private FTPWriterCmis(Map data) {
		init();
		_data = data;
	}

	// Constructor for TosJobScheduler
	private FTPWriterCmis() {
		init();
	}

	public void setData( Map data) {
		_data = data;
	}
	// TosJobScheduler - TimeBound FTPing
	public void processFiles() {
		processFTPData(FTP); 
	}

	// GroovyMsgProcessor - FTP Append record
	public boolean write() {
		String msgType = null;
		boolean ret = false;
		String unitNbr = null;
		String vesVisit = null;
		String n4msgType = null;
		String dateTime = null;
		
		try {
			Map<String, FieldDataCmis> fields = null;
			//reset value for message read
			firstCheck = true;

			MessageConfigCmis instance = MessageConfigCmis.getInstance();
			//Retrieves N4 to Cmis Action
			n4msgType = (String)_data.get(ObjectWriterFactory.MSG_TYPE);
			dateTime = (String)_data.get("aDate")+" "+(String)_data.get("aTime"); 
			cmisRelatedAction = instance.getCmisActionName(n4msgType);
			//Retrieves Unit Class Mapping/Output File Format
			unitClass = (String)_data.get(ObjectWriterFactory.UNIT_CLASS);
			if(unitClass.equals("CONTAINER")){
				fields = instance.getFields(CMIS_UNIT_FIELDS);	
				unitNbr = (String)_data.get("ctrNo");
			}else if(unitClass.equals("CHASSIS") ){
				fields = instance.getFields(CMIS_CHASSIS_FIELDS);
				unitNbr = (String)_data.get("chassisNumber");
			}else if(unitClass.equals("ACCESSORY")){
				fields = instance.getFields(CMIS_ACRY_FIELDS);
				unitNbr = (String)_data.get("accessory");
			}else if(unitClass.equals("VESSELVISIT")){
				fields = instance.getFields(CMIS_VESVISIT_FIELDS);
				vesVisit = (String)_data.get("visit");
			}else if(unitClass.equals("TRUCKVISIT")){
				fields = instance.getFields(CMIS_TRUCKVISIT_FIELDS);
				unitNbr = (String)_data.get("ctrNo");
			} 
			if(fields == null){
				throw new Exception("Mapping XML Null:Check Event Entry in xml Folder");
			}
			String txtStr = createFixedLenTxtStr(fields, instance);
			logger.debug("Formatted String Rec: " + txtStr);
			//System.out.println("Formatted String Rec: " + txtStr);
			ret = appendFTPData(txtStr); 

		} catch (Exception ex) {
			logger.error("Error in process msg type" + msgType, ex);
			String errorMsg = "Exception in writing out file for Cmis "+ (unitClass.equals("VESSELVISIT") ? "VesVisit:": "Cntr:"+unitNbr+"  Event:"+n4msgType+"   Time:"+dateTime);
			EmailSender.sendMail(emailAddr, emailAddr, errorMsg, errorMsg);
		}
		return ret;

	}

	  private boolean appendFTPData(String msgRec) { 
		  messageQueue.add(msgRec); 
		  sender.signal();
		  return true;
	  }
	
	  private void copyFile(File f1){
		   
		    try{
		      File f2 = new File(logDir+yearFmt+"_"+f1.getName());
		      InputStream in = new FileInputStream(f1);
		      
		      //For Append the file.
//		      OutputStream out = new FileOutputStream(f2,true);

		      //For Overwrite the file.
		      OutputStream out = new FileOutputStream(f2);

		      byte[] buf = new byte[1024];
		      int len;
		      while ((len = in.read(buf)) > 0){
		        out.write(buf, 0, len);
		      }
		      in.close();
		      out.close();
		    }
		    catch(FileNotFoundException ex){
		    	logger.error("Error in FTPWriter Copy File Method", ex);
		    }
		    catch(IOException e){
		    	logger.error("Error in FTPWriter Copy File Method", e);
		    }
		  }

	  private void copyFile(String file, byte[] data)throws Exception  { 
		  try {
			  file = logDir+yearFmt+"_"+file;
			  FileOutputStream stream = new FileOutputStream(file);
			  stream.write(data);
			  stream.flush();
			  stream.close();
		  } catch (Exception e) {
			  logger.error("Error in FTPWriter Copy File Method", e);
			  throw e;
		  }
		  
	  }
	  
	  private void append(String data, File f) throws IOException{
	      BufferedWriter bw = null;
	      try {
	         bw = new BufferedWriter(new FileWriter(f, true)); 
	         bw.write(data);
	         bw.flush(); 
	      } finally {                       // always close the file
	    	  if (bw != null) try {
	    		  bw.close();
	    	  } catch (IOException ioe2) {
	    		  // just ignore it
	    	  }
	      } // end try/catch/finally

	   } // end append()

	  private int countRecords(File f) throws IOException {
		  LineNumberReader ln = null;
		  try {
			  FileReader fr = new FileReader(f);
			  ln = new LineNumberReader(fr);
			  while(ln.readLine() != null);
			  logger.debug("FTP Record count="+ln.getLineNumber());
			  return   ln.getLineNumber();
		  } finally {
			  try {
				  if(ln != null) ln.close();
			  } catch (Exception e) {}
		  }
	  }

	 
	/*
	 * Based on Function call method either Appends data to FTP file or Uploads
	 * FTP File
	 */
	  private synchronized boolean processFTPData(String processFunction) {
		String ftpFileName = null;
		String dtFormat = "HHmmssSSS";
		
		String dateFmt = CalendarUtil.dateFormat(dtFormat);
		yearFmt = CalendarUtil.dateFormat(dateFormat);
		String date = dateFmt.substring(0,8);
		FtpProxySenderBiz sender = new FtpProxySenderBiz();
		sender.setTimeout(timeout);
		boolean fileFound = false;
		int recThreshold = Integer.parseInt(TosRefDataUtil.getValue("MAX_MESSAGE_TO_SEND"));
		StringBuffer msgRec = new StringBuffer();
		
		try {
			// Todo, make this file based.
			File f = new File(storeDir);
			File[] files = f.listFiles(new TxtFilter());
			// TosJobScheduler - Files for Upload
			if(processFunction.equals(FTP)) {
				if (files != null ) {
					logger.debug("files.length:" + files.length);
					for (int i = 0; i < files.length; i++) {
						ftpFileName = files[i].getName();
						logger.debug("File Length " + files.length + " FTP File: " + ftpFileName);
						sender.sendFile(ftpToCmisDest, files[i],true,null);
						copyFile(files[i]);
						files[i].delete();
					} 
				}
				return true;
			}

			
			
			int size = 0;
			if(processFunction.equals(WRITE)) {
				size = messageQueue.size();
				for(int cnt=0;cnt<size;cnt++) {
					if(cnt != 0) msgRec.append("\n");
					msgRec.append(messageQueue.remove(0));
				}
				logger.info("FTPWriter ("+size+")");
				logger.debug(msgRec.toString());
				if(size == 0) return true;
			}
			
			// Groovy Processor Write - File to be appended
			if (files != null && processFunction.equals(WRITE)) {
				//logger.debug("process Upload writer:");
				for (int i = 0; i < files.length; i++) {
						ftpFileName = files[i].getName();
						logger.debug("ftpFileName: " + ftpFileName);
						// Check for Failed Files & Uploads
						if ( fileFound ) {
							sender.sendFile(ftpToCmisDest, files[i],true,null);
							copyFile(files[i]);
							files[i].delete();
						}
						// Checks for Active MSG files and Appends
						else {
							fileFound = true;
							//logger.debug("MSG File: " + ftpFileName);
							append("\n"+msgRec.toString(),files[i]);
							int recNum = countRecords(files[i]);
							// Appends last Record and FTP's File
							if (recNum >= recThreshold) {
								//logger.debug("RecNum == Limit-1 " + recNum.length + "FileName : " + ftpFileName);
								//sender.sendFile(ftpToCmisDest, ftpFileName,	filedata);
								sender.sendFile(ftpToCmisDest, files[i],true,null);
								copyFile(files[i]);
								files[i].delete();
							}
						}
						//return true;
				}// for Ends
				if (!fileFound) {
					ftpFileName = date + ".TXT";
					logger.debug("New File Created " + ftpFileName);
					File file = new File(storeDir+"/"+ftpFileName);
					append(msgRec.toString(),file);
					logger.debug( "End processFTPData() .............");
					return true;
				}
			}// else if ends

			if (files == null && processFunction.equals(WRITE)) {
				ftpFileName = date + ".TXT";
				logger.debug("Files=NULL : New File Created " + ftpFileName);
				File file = new File(storeDir+"/"+ftpFileName);
				append(msgRec.toString(),file);
				
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
				EmailSender.mailAttachment(emailAddr, emailAddr, MAIL_HOST,	ftpFileName, msgRec.toString(),
								"Error while writting/Uploading Data File \n" + e,
								"Error In writting/Uploading N4 transaction data on the server");
			} catch (Exception ex) {
				logger.error("Renamed Error FTP File and Sent out Mail", e);
				EmailSender.sendMail(emailAddr, emailAddr, "Error in Renaming FTP CMIS File", "Error in Renaming FTP CMIS File");
			}
		}
		logger.debug( "End processFTPData() .............");
		return false;
	}
	
	/*
	 *  
	 * 1] Maps the CMIS Action Field from the N4-Cmis xml mapping file
	 * 2] Maps the CMIS Leg Field mapping
	 * 3] Creates a Fixed Length Format Text String
	 * 4] Formats fixed length file for Dataset import      
	 */
	private String createFixedLenTxtStr(Map<String, FieldDataCmis> fields,MessageConfigCmis messageConfigInst )throws Exception {

		StringBuffer bufValue = new StringBuffer();
		String formattedRec = null;
		String fixStrValue = null;
		
		try {
			Iterator it = fields.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry entryField = (Map.Entry) it.next();
				FieldDataCmis fieldData = (FieldDataCmis) entryField.getValue();
				String fieldName = fieldData.getFieldName();
				int seqNumber = fieldData.getSequenceNum();
				int fieldSize = fieldData.getFieldSize();
				String fieldValue = _data.get(fieldName) != null ? (String) _data.get(fieldName) : "";
				
				//N4 to Cmis Action Mapping -- Removed Leg value 101
			 /*	if(fieldName.equalsIgnoreCase("lastAction") ||fieldName.equalsIgnoreCase("action")){
					//logger.debug("cmisRelatedAction ::: "+cmisRelatedAction);
					//Change For CMIS Event Mapping
					if(fieldValue.equals("null"))
					{
					  fieldValue = cmisRelatedAction;
					}
				}//Mapping Leg Field  
				if(!unitClass.equalsIgnoreCase("VESSELVISIT") && fieldName.equalsIgnoreCase("leg")){
					if(!(fieldValue.equals("null") || fieldValue.trim().length() == 1)){
						fieldValue = getVoyageDirectionCode(fieldValue, messageConfigInst);
						logger.debug("Leg Direction Code :"+fieldValue);
					}
					logger.debug("Leg Barge Direction Code :"+fieldValue);
				}*/
				fixStrValue = fixLengthString(fieldValue, fieldSize);
				bufValue.append(fixStrValue); 
				//Assigning Cmis DataSet formatted value 
				//-- Remove bufValue = cmisDataSetFormatting(fieldName,fieldSize,bufValue);
			} 
			//Formatting the last Cmis DataSet index
	          if (bufValue.length() > 1){
			  String fmtRec = bufValue.substring(0, bufValue.length());
			  formattedRec = cmisDataSetFormatting(fmtRec);
			  //logger.debug("After Formatting ::"+formattedRec);
			}
		} catch (Exception e) {
			logger.error("Error in creating FixLength String : " + e);
			throw e;
		}

		return formattedRec;
	}

	/*
	 * Method returns the Field Value padding the max field length with Spaces(' ')
	 */
	private static String fixLengthString(String fillChar, int count)throws Exception
	{
        String fieldValue = fillChar == null || fillChar.equals("null") ? "" : fillChar;
		char[] chars = null;
		chars = new char[count];
		char[] strChars = fieldValue.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if (i < strChars.length) {
				chars[i] = strChars[i];
			} else {
				chars[i] = ' ';
			}
		}
	 return new String(chars);
	}
	
	/* ----101  Moving Method to GroovyMessageProcessorClass --------
	 * Method Maps the CMIS leg Value by fetching the Vessel Direction 
	 * Based on the Load port and Destination Port 
	 */
/*	private String getVoyageDirectionCode(String fieldValue, MessageConfigCmis messageConfig)throws Exception
	{
	  String directionCode = "%";
	  try
	  {
	    if(fieldValue.indexOf("_") == -1){
	    	String warningMsg = "Unit:"+_data.get("ctrNo")+"   leg value not correct for Evnt:"+_data.get("msgType"); 
	    	EmailSender.sendMail(emailAddr, emailAddr,warningMsg,warningMsg);
	    	return "%";
	    }
		String loadPort = fieldValue.substring(0,fieldValue.indexOf("_"));
		String dischargePort = fieldValue.substring(fieldValue.indexOf("_")+1);
		logger.debug("loadPort_FTP : "+loadPort+"  dischargePort_FTP : "+dischargePort);
		//Setting dport='OPT' condition for INGATE MTY Blank Leg Value 
		if(dischargePort.equals("OPT")){
			return directionCode="";
		}else if(loadPort.equals("null") || dischargePort.equals("null")){
			return directionCode="";
		} 
		
		Map<String, FieldDataCmis> legMapping = messageConfig.getLegMapping(loadPort);
		
		//If Load Port is not mapped pass Percentage 
		if(legMapping == null){
			return directionCode;
		} 
		Iterator legIter = legMapping.entrySet().iterator();
		while (legIter.hasNext())
		{
		  Map.Entry legFieldCode = (Map.Entry) legIter.next();
		  
		  String toPort = (String)legFieldCode.getKey();
		  String legCode = (String)legFieldCode.getValue();
      	  	 
		  if(dischargePort.equalsIgnoreCase(toPort)){
			  directionCode = legCode;
			  logger.debug("toPort_FTP : "+toPort+" legCode_FTP :"+legCode);
			  break;
		  }
		 }//While Ends
	  }catch(Exception e){
			logger.error("Leg value not correct : " + e);
	        throw e;
	  }
	  return directionCode;
	}
*/
	
	 /*
  	 * Method Formats the Fixed Length CMIS Feed File
  	 * By Replacing start and end blank(' ') data indexes with Special Character '~'
  	 * This Change has been made to import the CMIS FEED file into the New Data Set format in CMIS.
  	 */
      private static String cmisDataSetFormatting(String outputFile)throws Exception
      {
        int count = -55;
        boolean nextChar = false;
        StringBuilder finalCharSet = new StringBuilder();
     	char[] chars = new char[outputFile.length()];
     	char[] strChars = outputFile.toCharArray();
     	 for (int i = 0; i < strChars.length; i++) 
     	 {
     		if(nextChar)
     		{
     		  count = 0;
     		  nextChar = false;
     		  if(strChars[i] == ' '){
     		     chars[i] = '~';
     		  }else{
     			 chars[i] = strChars[i];
     		  }
     		}
     		else if(strChars[i] == ' ' && i == 55)
     		{
     		   chars[i] = '~';
     	    }
     		else if( count == 254)
     		{
     		  count = 0;
    		  nextChar = true;

     		  if(strChars[i] == ' '){
      		      chars[i] = '~';
     		  }else{
     				chars[i] = strChars[i];
     		  }
     	    }else{
     		   chars[i] = strChars[i];
     		}

     		count++; 
     	 }
     	 
     	 //If Count is Zero we don't have to append padding Spaces
     	 if(count == 1){
     		return finalCharSet.append(chars).toString();
     	 }
     	 
     	 int appendChar = 255-count;
     	 //logger.debug("appendchar :::"+appendChar);
     	 
     	 char[] appChar = new char[appendChar];
     	 for(int j = 0; j<appChar.length; j++){
   		   if(j == appChar.length-1){
   			 appChar[j]='~';
   		   }else{
   			appChar[j]=' ';		
   		   }
     	 }
     	 //logger.debug("appChar :: "+appChar.length);
     	 finalCharSet.append(chars);
     	 finalCharSet.append(appChar);
     	  
     	 return finalCharSet.toString();

      }
      
      private class FTPWriterThread extends Thread{
    	  private boolean signal = false;
    	  public void run() {
    		  while(running) {
    			  if(signal == false) {
    				  synchronized(this) {
    					  try {
    						  this.wait();
    					  } catch (Throwable e) {}
    				  }
    			  }
    			  signal = false;
    			  //processFTPData(WRITE);
    		  }
    	  }
    	  
    	  public void signal() {
    		  if(signal == false) {
    			  signal = true;
    			  synchronized(sender) {
    				  sender.notifyAll();
    			  }
    		  }
    	  }
      }

      class TxtFilter implements java.io.FilenameFilter{
  		public boolean accept(File dir, String name) {
  			if(name.endsWith(".TXT")) return true;
  			return false;
  		}
  	}
}
