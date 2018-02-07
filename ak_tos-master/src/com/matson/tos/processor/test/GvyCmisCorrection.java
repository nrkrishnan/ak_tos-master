package com.matson.tos.processor.test;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;
import com.matson.tos.util.GroovyXmlUtil;
import com.matson.tos.util.JMSSender;
import com.matson.tos.util.StrUtil;
import java.io.*;

public class GvyCmisCorrection {
	private static Logger logger = Logger.getLogger(GvyCmisCorrection.class);
	public static final String CLASS_LOC = "database";
	public static final String CLASS_NAME = "GvyCmisEventCorrection";
	//public static LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
	public static Map<String, String> map = null; 
	
	//Method To read file and Store Unit Number as Key Value pair
	public void processNlt(String contents,String writeFileToPath)
	{
		String gvyNLTXml = null;
		StringBuffer strBuff = new StringBuffer();
      try
      {
       
		int unitCount = 0;
		File file = null;
		BufferedWriter output = null;
		int fileCount = 0;
		int countAllRec = 0;
		String[] lines = contents.split("\n");
		System.out.println( "There are " + lines.length + " lines in the file.");
		for (int i = 0; i < lines.length; i++) 
		{
			  String unitValue[] = lines[i].split(",");
			  map = new HashMap<String, String>();		  
			  map.put("eventGkey",StrUtil.trimQuotes(unitValue[0]));
			  gvyNLTXml = getInjectionXmlStrCmis( CLASS_NAME, CLASS_LOC, map);
			  unitCount++;
			  strBuff.append(gvyNLTXml);
			  strBuff.append("\r\n");
			 // if(unitCount == 200 || i == lines.length-1){
				  String header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><argo:snx xmlns:argo=\"http://www.navis.com/argo\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.navis.com/argo snx.xsd\">\n";
			 // fileCount++;	  
			 // unitCount=0;	  
	 	      file = new File(writeFileToPath);
			  output = new BufferedWriter(new FileWriter(file));
			  output.write(header);
			  output.write(strBuff.toString());
			  output.write("</argo:snx>\n");
			  output.close();
			  //strBuff = new StringBuffer();
			//  }

			  //XML Posting
			  countAllRec++;  
		}   
		  System.out.println("Created Msg for UNIT COUNT ::"+countAllRec);


      }catch(Exception e){
    	  e.printStackTrace();
      }
	}
	
	
	public static String getInjectionXmlStrCmis( String className, String classLoc, Map<String, String> data) {
		if ( data == null)
			return null;
		
		StringBuffer ret = new StringBuffer();
		
		ret.append( "<groovy class-location=\"" + classLoc
				+ "\" class-name=\"" + className + "\">");
		ret.append( "<parameters>");
		
		for (Map.Entry<String, String> item : data.entrySet() ) {
			// A1, escape values.
			String value = org.apache.commons.lang.StringEscapeUtils.escapeXml(item.getValue());
			ret.append( "<parameter id=\"");
			ret.append( item.getKey() + "\" ");
			ret.append( "value=\"" + value + "\" />");
		}
		ret.append( "</parameters>" + "</groovy>");
		return ret.toString();
	}
	
	public static void main(String args[])
	{
		try
		{
			String pickupFileFromPath = "C:/Documents and Settings/Gxr/Desktop/PROD_ISSUES/INGATE_LEG_BLANK/IGT_24.txt";
			String writeFileToPath = "C:/Documents and Settings/Gxr/Desktop/PROD_ISSUES/INGATE_LEG_BLANK/IGT_24.xml";
			java.io.File file = new java.io.File(pickupFileFromPath);
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
		  GvyCmisCorrection ntlmsg = new GvyCmisCorrection();
		  ntlmsg.processNlt(content,writeFileToPath); 
		  System.out.println("After processFile(fileStr)");
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}   	
	}


}
