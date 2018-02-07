package com.matson.tos.messageHandler;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;

import com.matson.tos.util.CheckDigit;
import com.matson.tos.util.GroovyXmlUtil;
import com.matson.tos.util.JMSSender;
import com.matson.tos.util.StrUtil;

/**
 * Send depart for previous voyage or delete for duplicate voyages.
 * @author Glenn
 * 
 * Change History:
 * A1  SKB  4/13/2009  Lookup unit ID from renaming logic.
 *
 */
public class GvyNltMessageHandler {
	private static Logger logger = Logger.getLogger(GvyNltMessageHandler.class);
	public static final String CLASS_LOC = "database";
	public static final String CLASS_NAME = "GvyInjNLT";
	//public static LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
	public static Map<String, String> map = null; 
	private CheckDigit check;
	
	
	public  GvyNltMessageHandler() {
		this(new CheckDigit());
	}
	
	public  GvyNltMessageHandler(CheckDigit check ) {
		this.check = check;
		
	}
	//Method To read file and Store Unit Number as Key Value pair
	public void processNlt(String contents)
	{
		String gvyNLTXml = null;	
		StringBuffer strBuff = new StringBuffer();
      try
      {
		int unitCount = 0;
		String[] lines = contents.split("\n");
		logger.debug( "There are " + lines.length + " lines in the file.");
		map = new HashMap<String, String>();	
		for (int i = 0; i < lines.length; i++) 
		{
		  try
		  {	
		   if(lines[i].contains("\"unit\"")) 
		   {
			  String unitValue[] = lines[i].split(",");
			  if(map.size() > 0){
			     sendMessage(map);
			     unitCount++;
			     map.clear();
			  }
			  String id = StrUtil.trimQuotes(unitValue[2]);
			  //A1, use the checkdigit in that database and not the new ves file.
			  //Renum process will fix it after.
			  id = check.getCheckDigitUnit(id);
			  logger.debug("NLT id ="+id);
			  map.put("unitId",id );
		   }else if(lines[i].contains("\"carrier\"")){
			   String[] carrier = lines[i].split(",");
               if(StrUtil.trimQuotes(carrier[123]).equals("IB") && StrUtil.trimQuotes(carrier[128]).equals("ACTUAL")){
            	   map.put("ibCarrier",StrUtil.trimQuotes(carrier[126]));
               }else if(StrUtil.trimQuotes(carrier[123]).equals("OB") && StrUtil.trimQuotes(carrier[128]).equals("DECLARED")){
            	   map.put("obCarrier",StrUtil.trimQuotes(carrier[126]));
               }
		   }
		  }catch(Exception e){
			  e.printStackTrace();
			  //Catch Individual Line Exception and Keep Reading the file
			  logger.error("NlT MessageHandler Exception Line ::"+lines[i]);
		  }
		}//For Ends
		
		//Sends Last Unit
		if(map.size() > 0){
		     sendMessage(map);
		     unitCount++;
		     map.clear();
		}
		logger.debug("NLT DEPARTED PROCESS UNIT COUNT ::"+unitCount);
      }catch(Exception e){
    	  e.printStackTrace();
      }
	}
	
	public void sendMessage(Map map)
	{
  	  try{
  		String msgXml = GroovyXmlUtil.getInjectionXmlStr( CLASS_NAME, CLASS_LOC, map);   
        if(msgXml != null)
        {
		  logger.debug( "XML:" + msgXml);
		  JMSSender sender = new JMSSender( JMSSender.REAL_TIME_QUEUE, "HON");
		  sender.send( msgXml);
	   } else {
		logger.error( "Can not create message handler.");
	  }
  	  }catch(Exception e){
  		  e.printStackTrace();
  	  }
	}


/*	public static void main(String args[])
	{
		try
		{
			java.io.File file = new java.io.File("C:/Documents and Settings/Gxr/Desktop/TEST_NLT_DEL/A915A_NI.TXT");
			if(file != null)
			{
			  System.out.println("File :: "+file);
			}
			java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(file));
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = br.readLine()) != null) 
			{
				sb.append(line + "\n");
			}
				br.close();
				String content = sb.toString();
		  System.out.println("Before processFile(fileStr)");
		  GvyNltMessageHandler ntlmsg = new GvyNltMessageHandler();
		  ntlmsg.processNlt(content); 
		  System.out.println("After processFile(fileStr)");
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}   	
	}
*/


}
