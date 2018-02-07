package com.matson.tos.util;

import org.apache.log4j.Logger;

import com.matson.cas.refdata.RefDataLookup;
import com.matson.cas.refdata.mapping.TosDestPodData;
import com.matson.tos.processor.DcmConverterFileProcessor;
import java.util.Calendar;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.apache.commons.net.ftp.FTPClient;
/*
 * The DcmFormatter class Reads,Manipulates and Formats data 
 */
public class DcmFormatter {
	

	private static Logger logger = Logger.getLogger(DcmFormatter.class);
	public static String displayLineDataMapping  = "                                                            ";
	/*
	 * Method addQuotes formats the string data by adding quotes
	 */
	public static String addQuotes( String value) 
	{
		value = "\"" +value+ "\"," ;
		return value;
	}
	/*
	 * Method removeLastComma formats the String Data by removing the last comma  
	 */
	public static String removeLastComma(String strData)
	{   
		
		strData = replaceQuotes(strData);
		int length = strData.length();
        String formattedLineValue = strData.substring(0,length-1);
        return formattedLineValue;
	}
	/*
	 * Method addHeaderQuotes formats the String data by appending the File version and 9 blank fields
	 * to align Header one in the Hazviewver
	 */
	public static String addHeaderQuotes(String strHead)
	{
		//Appends the version to header field
		StringBuffer version = new StringBuffer(strHead);
		int ver = version.lastIndexOf("\"");
		//Appends fields with blank quotes
		String appendQuotes = version+",\"" +"\",\"" + "\",\""+
	  	"\",\"" + "\",\""+"\",\"" +  "\",\"" + "\",\"" + "\",\"" + 
	  	"\"";
		return appendQuotes;
	}
	
	/*
	 * Method formatHeaderText reads the Header text and formats the data
	 */
	public static String formatHeaderText(String strHeaderData)
	{
		String deptPort = null;
		String boundPort = null;
        StringBuffer strBuff = new StringBuffer();
        String[] temp = strHeaderData.split(",");
        //logger.debug("Length : "+temp.length);
        //Substitute vessel code if vessel name is null 
        if(temp[0]== null || temp[0].length() == 0){
        	temp[0] = temp[1];
        }
        //Converts readLine from CSV to Required TextFormat
        for(int i=0; i< temp.length; i++){
        	String formatField = addQuotes(temp[i]);
        	strBuff.append(formatField);
        }
        
        String formatedHeader = strBuff.toString();
        //Removes the last comma in the string
        formatedHeader = removeLastComma(formatedHeader);
        
       	return formatedHeader;
	}
	/*
	 * Method formatHeaderTextWithVersion reads the Header text and formats the data and sets the version number
	 */
	public static String formatHeaderTextWithVersion(String strHeaderData, String version)
	{
        StringBuffer strBuff = new StringBuffer();
        String[] temp = strHeaderData.split(",");
        //logger.debug("Length : "+temp.length);
        //Substitute vessel code if vessel name is null 
        if(temp[0]== null || temp[0].length() == 0){
        	temp[0] = temp[1];
        }
        //Converts readLine from CSV to Required TextFormat
        for(int i=0; i< temp.length; i++){
        	if(i==1)
        		temp[i] = version;
        	String formatField = addQuotes(temp[i]);
        	strBuff.append(formatField);
        }
        
        String formatedHeader = strBuff.toString();
        //Removes the last comma in the string
        formatedHeader = removeLastComma(formatedHeader);
        
       	return formatedHeader;
	}
	

	/*
	 * Method modifyData alters the String Data to escape the delimiter for the Description Fields
	 */
	public static String modifyData(String value)
	{
       StringBuffer strBuffer = new StringBuffer(value);
	   try
        {
		   char pattern = '"';
		   int occurs = getQuotesOccurence(value,pattern);
        	for(int i=0;i<occurs;i++)
        	{
	        	int startIndex = strBuffer.indexOf("\"");
	        	int endIndex = strBuffer.indexOf("\"", startIndex+1);
	        	startIndex++;
	        	String strSubstring = strBuffer.substring(startIndex, endIndex);
	        	strSubstring = strSubstring.replaceAll(",", "!");
	        	startIndex--;
	        	strBuffer.replace(startIndex, endIndex+1, strSubstring);
        	}
        }
        catch(Exception ex){
        	logger.error( "Class: DcmFormatter - Method:modifyData() :: "+strBuffer.toString(),ex);
        }
        return strBuffer.toString();
	}
	/*
	 * Method replaceQuotes replaces the Quotes in the formated Description fields
	 */
	public static String replaceQuotes(String strRecordData)
	{
		if(strRecordData == null) return null;
		String replacedQuotes = strRecordData.replaceAll("!", ",");
	    return replacedQuotes;
	}
	
	/*
	 * Method gets the occurrence of quotes to format data in the Description Fields 
	 */
	public static int getQuotesOccurence(String value, char pattern)throws Exception 
	{
	    int occurs = 0;
		 if(value != null){
    	    for(int i = 0; i < value.length(); i++){
    	      char next = value.charAt(i);
    	      if(next == pattern){
    	        occurs++;
    	      }
    	    }
	    }
 	    occurs = occurs/2; 
 	    //logger.debug( "Class: DcmFormatter - Method:getQuotesOccurence(): " + occurs);
 	    return occurs; 
	}
	/*
	 * Method Formats a No Container Record entry in empty DCM file
	 */
	public static String noContainersFormat(String strVesCode,String strVesVoyage)
	{
		StringBuffer strBuff = new StringBuffer();
		strBuff.append(addQuotes(strVesCode+strVesVoyage));
		strBuff.append(addQuotes("NO HAZ CNTRS"));
		strBuff.append(addQuotes("0"));
		for(int i=0;i<28;i++){
			if(i==3){
				strBuff.append("\" \",");
			}
			strBuff.append("\"\",");
		}
		String noContFormat = DcmFormatter.removeLastComma(strBuff.toString()); 
		return noContFormat;
	}
	
	/*
	 * Formats the Hazardous Description for the USCG file format 
	 */
	public static String formatHazDesc(String hazDescData)
	{
		int index = hazDescData.indexOf("\"");
		String hazDesc = hazDescData.substring(index+1, hazDescData.length());
		StringBuffer strBuff = new StringBuffer();

		if(hazDesc.length() > 75){
			strBuff.append(hazDesc);
			int i = strBuff.indexOf(" ", 65);
			strBuff.insert(i+1,"\n");
			if(hazDesc.length() > 140)			{
				int j = strBuff.indexOf(" ", 135);
				strBuff.insert(j+1,"\n");
			}
		}else{
		 strBuff.append(hazDesc);
		}
		strBuff.append("\n");
		String hazdesc = replaceQuotes(strBuff.toString());
		return hazdesc;
	}
	
	/*
	 * Formats the header for the USCG file format 
	 */
	public static String formatUscgHeader(String headerData)
	{
		String[] temp = headerData.split(",");
        logger.debug("Length : "+temp.length);
        //Substitute vessel code if vessel name is null 
        if(temp[0]== null || temp[0].trim().length() == 0){
        	temp[0] = temp[1];
        }
        if(temp[6] != null)
        	temp[6] = temp[6].toUpperCase();

        String header = "\t  DCM DATA FOR OUTBOUND VESSEL FROM "+temp[6]+" BY DESCRIPTION \n"+
	       "\t\t\t  "+temp[0]+"  Voyage: "+temp[2]+"\n\n\t\t\tOfficial Number: "+temp[4]+"\n"+
	       "============================================================================\n"+
	       "Description\nPkg No.   Stowage Weight     Container    Dport Shipment#  Shipper\n"+
	       "--------- ------- ---------- ------------ ----- ---------- ---------------------";
		
		return header;
	}
	/*
	 * Formats the weight unit for display   
	 */
	public static String formatWeightToLB(String kgWeight)
	{
		String lbsValue = null;
		String weightValue = null;
/*    	if(kgWeight == null || kgWeight.trim().length() == 0){
    		return "";
    	}
    	else{
			int i = kgWeight.indexOf("K");
			if(i==0){
			 return "LB";
			}
			else if(i != -1){
			 String kgVal= kgWeight.substring(0, i);
			}
    	}
*/
		if(kgWeight.indexOf("K") == 0)
		{
			return "LB";	
		}
		if(kgWeight.length() > 0)
		{
         lbsValue = weightFromKgToLB(kgWeight);

      	if((lbsValue.length() >=7 ))
    		weightValue = lbsValue.concat(" L");
    	else if((lbsValue.length() >= 4))
    		weightValue =lbsValue.concat(" LB");
    	else if((lbsValue.length() <= 3))
    		weightValue =lbsValue.concat(" LBS");
		}
		else{
		  weightValue = "";
		}
		return weightValue;
	}
	
	/*
	 * Format weightFromKgToLB
	 */ 
	public static String weightFromKgToLB(String kgWeight)
    {
		String lbsWeight = null;
		if(kgWeight == null || kgWeight.trim().length()==0){
			return "";
		}
	    double convtWeight = Double.parseDouble(kgWeight)* 2.20462262;
	    //System.out.println("convtWeight ::"+convtWeight);
	    long result = Math.round(convtWeight);
	    lbsWeight = String.valueOf(result);
    	return lbsWeight;
    }

}
