/*
 *****************************************************************
 * Srno   Date		Doer Change Description
 * A1     09/22/10   GR   convertTimeZone return Calendar table
 * A2     10/24/10   GR   Julian Date Conversion Method
 * A3	  12/19/12	 Karthik	Added: addBusinessDays()
 * A4	  04/24/13	 Karthik    Added: convertDateToString2(), convertStrgToDateFormat2()
 ******************************************************************
 */
package com.matson.tos.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.apache.log4j.Logger;

import com.adventnet.aclparser.ParseException;
import com.matson.tos.vo.CalendarVO;

public class CalendarUtil {
	private static Logger logger = Logger.getLogger(CalendarUtil.class);

	public static XMLGregorianCalendar getXmlCalendar( Date date) {
		if ( date == null)
			return null;
		XMLGregorianCalendar xmlCal = null;
		Calendar cal = null;
		try {
			cal = Calendar.getInstance();
			cal.setTimeInMillis( date.getTime());
			DatatypeFactory factory = DatatypeFactory.newInstance();
			//xmlCal = factory.newXMLGregorianCalendar( cal);
			xmlCal = factory.newXMLGregorianCalendar( cal.get(Calendar.YEAR),
					cal.get(Calendar.MONTH) + 1,cal.get(Calendar.DAY_OF_MONTH),
					cal.get(Calendar.HOUR_OF_DAY),cal.get(Calendar.MINUTE),
					cal.get(Calendar.SECOND),0, DatatypeConstants.FIELD_UNDEFINED );
		} catch ( DatatypeConfigurationException dex) {
			logger.error( "Exception in converting timestamp to XML calendar: ", dex);
			logger.debug( "Input date: " + date);
		} catch ( Exception ex) {
			logger.error( "Exception in converting timestamp to XML calendar: ", ex);
			logger.debug( "Input date: " + date);
		}
		return xmlCal;

	}

	public static XMLGregorianCalendar getXmlCalendar( String date, String pattern) {
		if ( date == null)
			return null;
		if ( date.trim().length() < 3)
			return null;
		XMLGregorianCalendar xmlCal = null;
		Calendar cal = null;
		try {
			SimpleDateFormat aDateFormat = new SimpleDateFormat( pattern);
			//logger.debug( "Date string = " + date);
			Date aDate = aDateFormat.parse( date);
			cal = Calendar.getInstance();
			cal.setTime( aDate);
			DatatypeFactory factory = DatatypeFactory.newInstance();
			//xmlCal = factory.newXMLGregorianCalendar( cal);
			xmlCal = factory.newXMLGregorianCalendar( cal.get(Calendar.YEAR),
					cal.get(Calendar.MONTH) + 1,cal.get(Calendar.DAY_OF_MONTH),
					cal.get(Calendar.HOUR_OF_DAY),cal.get(Calendar.MINUTE),
					cal.get(Calendar.SECOND),0, DatatypeConstants.FIELD_UNDEFINED );
		} catch ( DatatypeConfigurationException dex) {
			logger.error( "Exception in converting timestamp to XML calendar: ", dex);
			logger.debug( "Input date: " + date);
		} catch ( Exception ex) {
			logger.error( "Exception in converting timestamp to XML calendar: ", ex);
			logger.debug( "Input date: " + date);
		}
		return xmlCal;

	}
	//Method returns date format 
	public static String dateFormat(String dtFormat)
	{
		DateFormat dateFormat = new SimpleDateFormat(dtFormat);
		Date date = new Date();
		String dtformat = dateFormat.format(date);
		return dtformat;
	}

	//Method returns a TimeZone Formatted Date String
	public static String dateFormat(Date date, String dtFormat, TimeZone timezone)
	{
		DateFormat dateFormat = new SimpleDateFormat(dtFormat);
		dateFormat.setTimeZone(timezone);
		String dtformat = dateFormat.format(date);
		return dtformat;
	}


	public static String convertTimeZone(String dateStr, String dateFormat, String fromTimeZone,String toTimeZone){
		String convertDateStr = null;
		try{
			DateFormat formatter = new SimpleDateFormat(dateFormat);   
			formatter.setTimeZone(TimeZone.getTimeZone(fromTimeZone));
			Date date = formatter.parse(dateStr);
			formatter.setTimeZone(TimeZone.getTimeZone(toTimeZone));   
			convertDateStr = formatter.format(date);
		}catch(Exception e){
			e.printStackTrace();
		}
		return convertDateStr;
	}

	public static String convertDateToJulian(String date)
	{
		String year  = date.substring(0,4);
		String month = date.substring(5,7);
		String day   = date.substring(8,10);
		GregorianCalendar newGregCal = new GregorianCalendar(Integer.parseInt(year),Integer.parseInt(month) - 1,Integer.parseInt(day));
		long lngTime = newGregCal.getTimeInMillis(); 
		Date today = new Date(lngTime);
		SimpleDateFormat julianDate = new java.text.SimpleDateFormat("yyDDD");
		String dayOfYear = julianDate.format(today);
		return dayOfYear;
	}

	public static String ConvertJulianToDate(String julianDt, String dateFormat)
	{
		String convertDate = null;
		try
		{
			SimpleDateFormat julianDate = new java.text.SimpleDateFormat("yyDDD");
			Date date = julianDate.parse(julianDt);
			SimpleDateFormat fmtDate = new java.text.SimpleDateFormat(dateFormat);
			convertDate = fmtDate.format(date);
		}catch(Exception e){
			e.printStackTrace();
		}
		return convertDate;
	}

	public static final String convertDateToString(Date inputDate) {

		if (inputDate == null)
			return null;

		DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
		String parsedDate = null;
		try{
			parsedDate = df.format(inputDate);
		}catch(Exception pexp){
		}
		return parsedDate;
	}
	public static final String convertDateToString2(Date inputDate) {

		if (inputDate == null)
			return null;

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		String parsedDate = null;
		try{
			parsedDate = df.format(inputDate);
		}catch(Exception pexp){
			pexp.printStackTrace();
			logger.error(pexp);
		}
		return parsedDate;
	}
	public static String convertDateStringToString(String inDate, boolean noSlash)
	{
		if(inDate==null || inDate.equals(""))
			return "";
		DateFormat df1 = new SimpleDateFormat("MM/dd/yyyy");
		DateFormat df2 = new SimpleDateFormat("MMddyyyy");
		String outDate = "";
		Date tempDate = null;
		try{
			if(noSlash) {
				tempDate = df1.parse(inDate);
				outDate = df2.format(tempDate);
			} else {
				tempDate = df2.parse(inDate);
				outDate = df1.format(tempDate);
			}
		}catch(Exception pexp){
		}
		return outDate;
	}
	public static String convertDateStringToString2(String inDate, boolean noSlash)
	{
		if(inDate==null || inDate.equals(""))
			return "";
		DateFormat df1 = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		DateFormat df2 = new SimpleDateFormat("MMddyyHHmm");
		String outDate = "";
		Date tempDate = null;
		try{
			if(noSlash) {
				tempDate = df1.parse(inDate);
				outDate = df2.format(tempDate);
			} else {
				tempDate = df2.parse(inDate);
				outDate = df1.format(tempDate);
			}
		}catch(Exception pexp){
		}
		return outDate;
	}
	public static Date addCalendarDays(Date inDate, int numberOfDays){
		if(inDate == null)
			return null;
		Calendar baseDateCal = Calendar.getInstance();
		baseDateCal.setTime(inDate);
		baseDateCal.add(Calendar.DATE, +numberOfDays);	    
		return baseDateCal.getTime();
	}

	/*
	 * Converts TimeZone Date and TimeMilliseconds  -- Comment for NOW 101
	 */
	/*public static Calendar convertTimeZone(Date date, TimeZone timeZone){

		 Calendar mbCal = new GregorianCalendar(timeZone);
		 mbCal.setTime(date);


		 Calendar cal = Calendar.getInstance();
		 cal.set(Calendar.YEAR, mbCal.get(Calendar.YEAR));
		 cal.set(Calendar.MONTH, mbCal.get(Calendar.MONTH));
		 cal.set(Calendar.DAY_OF_MONTH, mbCal.get(Calendar.DAY_OF_MONTH));
         cal.set(Calendar.HOUR_OF_DAY, mbCal.get(Calendar.HOUR_OF_DAY));
		 cal.set(Calendar.MINUTE, mbCal.get(Calendar.MINUTE));
		 cal.set(Calendar.SECOND, mbCal.get(Calendar.SECOND));
		 cal.set(Calendar.MILLISECOND, mbCal.get(Calendar.MILLISECOND));

		 return cal;
	}*/

	public static final Date convertStrgToDateFormat(String inputDateString) {
		if(inputDateString == null )
			return null;
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
		Date parsedDate = null;
		try{
			parsedDate = dateFormat.parse(inputDateString);
		}catch(Exception exception) {
		}
		return parsedDate;
	}
	public static final Date convertStrgToDateFormat2(String inputDateString) {
		if(inputDateString == null )
			return null;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date parsedDate = null;
		try{
			parsedDate = dateFormat.parse(inputDateString);
		}catch(Exception exception) {
			exception.printStackTrace();
			logger.error(exception);
		}
		return parsedDate;
	}

	public static void main(String args[]){
		try
		{  
			SimpleDateFormat julianDate = new java.text.SimpleDateFormat("yyDDD");
			Date date = julianDate.parse("10295");
			SimpleDateFormat fmtDate = new java.text.SimpleDateFormat("MM/dd/yyyy");
			String convertDate = fmtDate.format(date);
			System.out.println("convertDate="+convertDate);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public static Date addBusinessDays(Date dateFrom, int noOfDays, ArrayList<CalendarVO> tosCalendar) 
	{
		if(tosCalendar == null)
			return null;
		System.out.println("addBusinessDays --> ADATE:"+dateFrom+" NOD:"+noOfDays);
		int dayCounter = 1;
		Date curHoliday = null;
		int cur=0;
		int numHol = tosCalendar.size();
		Calendar cal = Calendar.getInstance();
		cal.setTime(dateFrom);
		try {
			for (cur=0;cur<numHol;cur++) {
				CalendarVO calendarVo = tosCalendar.get(cur);
				String holidayStr = new SimpleDateFormat("MM/dd/yyyy").format(calendarVo.getHlidayDate());
				//System.out.println("holidayStr-->"+holidayStr);
				Date holiday = new SimpleDateFormat("MM/dd/yyyy").parse(holidayStr);
				if (holiday.before(dateFrom)) {
					continue;
				} else {
					curHoliday = holiday;
					System.out.println("curHoliday -->"+curHoliday);
					break;
				}
			}
			while (dayCounter < noOfDays) {
				cal.add(Calendar.DATE,1);
				int weekday = cal.get(Calendar.DAY_OF_WEEK);
				if (weekday == 1 || weekday == 7) {
					// do nothing 
				} 
				else if (curHoliday!= null && cal.getTime().compareTo(curHoliday) == 0) {
					cur = cur + 1;
					if(cur >= numHol) {
						curHoliday = null;
					}
					else {
						CalendarVO calendarVo = tosCalendar.get(cur);
						curHoliday = calendarVo.getHlidayDate();
					}
				} 
				else {
					dayCounter = dayCounter+1;
				}
			}

		}catch (Exception ex) {
			ex.printStackTrace();
		}
		return cal.getTime();
	}
	public static final String convertDateToStringMMDDYYY(Date inputDate) {

		if (inputDate == null)
			return null;

		DateFormat df = new SimpleDateFormat("MMddyyyy");
		String parsedDate = null;
		try{
			parsedDate = df.format(inputDate);
		}catch(Exception pexp){
		}
		return parsedDate;
	}

}
