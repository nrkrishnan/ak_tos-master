package com.matson.tos.util;



public class StrUtil {
	public static String trimQuotes( String value) {
		// Did not handle the single string case.
		if (value == null) return "";
		if (value.length() >= 2 && value.charAt(0) == '"')
			value = value.substring(0, value.length()-1).substring(1).trim();
		if ( value.length() >= 1 && value.charAt( value.length()-1) == '"')
			value = value.substring(0, value.length()-1);
		return value;
	}
	
	public static String padLeadingZero( String aId, int len){
		while ( aId.length() < len)
			aId = "0" + aId;
		return aId;
	}
	
	/*
	 * Method Removes the trailing Spaces   
	 */
	public static String removeTrailingSpaces(String strLine)
	{
		String formattedLine = strLine.replaceAll("\\s+$", "");
		return formattedLine;
	}
	/*
	 * Method returns character occurrence in String
	 */
	public static int countCharOccurrence(String line, char pattern)
	{
		int occurs = 0;
		if(line!= null && line.trim().length() > 0)
		{
			 for(int k = 0; k < line.length(); k++){
	  	      char next = line.charAt(k);
	  	      if(next == pattern){
	  	        occurs++;
	  	      }
	  	    }
		}
		return occurs;
	}
	
	 public static String getFieldValue(String msgText, String field)
	  {
	     String fieldValue = "";
	      try
	     {
	       int fieldIndx = msgText.indexOf(field);
	       if(fieldIndx == -1){ return null; }

	       int equalsIndx = msgText.indexOf("=",fieldIndx);
	       int nextspace = msgText.indexOf("\'", equalsIndx+2);
	       fieldValue  = msgText.substring(equalsIndx+2, nextspace);
	       //println("equalsIndx:"+equalsIndx+"  nextspace:"+nextspace+" oldValue:"+fieldValue);
	      }catch(Exception e){
	        e.printStackTrace();
	     }
	      return fieldValue;
	   }

}
