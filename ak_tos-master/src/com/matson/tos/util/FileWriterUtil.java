package com.matson.tos.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.Logger;

public class FileWriterUtil {
	
	private static Logger logger = Logger.getLogger(FileWriterUtil.class);
	
	/*
	 * Method Writes File to Specified Server Directory path
	 */
	public static void writeFile(String data,String dirPath,String fileName, String fileExt){
		String dtFormat = "MMddyyyyHHmm";
		String dateFmt = CalendarUtil.dateFormat(dtFormat);
		String date = dateFmt.substring(0,12);
		String name = fileName+date+fileExt;
		BufferedWriter bw = null;
	      try {
	        bw = new BufferedWriter(new FileWriter(dirPath+"/"+name)); 
	        bw.write(data);
	        bw.flush(); 
	      } catch(Exception e){
	    	  logger.error("Error in Writting File", e);
	      }
	      finally { // always close the file
	    	  if (bw != null) try {
	    		  bw.close();
	    	  } catch (IOException ioe2) {
	    		  // just ignore it
	    	  }
	      }
	}

}
