/*
 *  Srno  Doer   Date          Desc
 *  A1    GR     02/22/2010    Changed Method to Count the next hour from previous hour
 */
package com.matson.tos.processor;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import com.matson.tos.util.GroovyXmlUtil;
import com.matson.tos.util.JMSSender;
import java.util.Calendar;

public class CvsReportProcessor {
	private static Logger logger = Logger.getLogger(CvsReportProcessor.class);
	//public static Date startDatetime;  
	
	public CvsReportProcessor(){
	}
	
	public void process() {
		try {
			String xml = getXml();
			JMSSender sender = new JMSSender( JMSSender.REAL_TIME_QUEUE, "HON");
			sender.send( xml);
		} catch (Exception e) {
			logger.error("Could not Trigger CVS Hourly Report", e);
		}
	}
	
	public String getXml() {
		Map<String, String> data = new HashMap<String, String>();
		return GroovyXmlUtil.getInjectionXmlStr( "GvyReportCVSHourlyStatus", "database", data);
	}
	
    public static Date getStartTime(){
    	Calendar calendar = Calendar.getInstance();
		Date currentDate = calendar.getTime();
    	int min= calendar.get(Calendar.MINUTE);
    	int sec = calendar.get(Calendar.SECOND);
    	int addMin = 59 - min;
    	int addSec = 60 - sec;
    	calendar.add(Calendar.MINUTE, addMin);
    	calendar.add(Calendar.SECOND, addSec);
    	Date manipulatedDate = calendar.getTime();
    	//logger.debug("CurrMin="+min+" CurrSec="+sec+" CurrDate="+currentDate+" ManipulatedDt="+manipulatedDate);
    	return manipulatedDate;
	}
	
	public static void main(String[] args) {
		 Date dt = getStartTime();
		//CvsReportProcessor proc = new CvsReportProcessor();
		//System.out.println(proc.getXml());
		 System.out.println("dt="+dt);
	}
}