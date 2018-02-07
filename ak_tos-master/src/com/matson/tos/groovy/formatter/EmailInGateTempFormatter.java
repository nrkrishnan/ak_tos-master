/**
 * 
 */
package com.matson.tos.groovy.formatter;

import java.util.Map;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.matson.tos.dao.CargoStatusDao;
import com.matson.tos.util.EmailSender;
import com.matson.tos.util.UnitConversion;
import com.matson.tos.util.TosRefDataUtil;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
/**
 * @author JZF
 * 
 */
public class EmailInGateTempFormatter implements EmailFormatter {
	private static Logger logger = Logger.getLogger(EmailInGateTempFormatter.class);

	private Map _data;
	private String emailAddr = TosRefDataUtil.getValue( "EMAIL_GATE_TEMP");
	private String fromEmailAddr = TosRefDataUtil.getValue( "SUPPORT_EMAIL");
	
	public EmailInGateTempFormatter(Map data) {
		_data = data;
	}

	public String getContent() {
		String emailContent = null;
		try {
			String port = (String) _data.get("port");
			String unit = (String) _data.get("unit");
			String bookingNum = (String) _data.get("bkgNum");
			// Converts Required temperature from Celsius to Fahrenheit
			double gateTemp = UnitConversion.celsiusToFahrenheit((String) _data.get("gateReqTemp"));
			double bkgTemp = UnitConversion.celsiusToFahrenheit((String) _data.get("bkgReqTemp"));

			emailContent = emailDisplayFormatter(port,unit,bookingNum,gateTemp,bkgTemp);
		} catch (Exception ex) {
			logger.debug("Error in creating email." + ex);
		}
		return emailContent;
	}

	public String getFromEmailAddr() {
		// TODO Auto-generated method stub
		return fromEmailAddr;
		
	}

	public String getSubject() {
		// TODO Auto-generated method stub
		return "Gate Booked Temp. Discrepancy";
	}

	public String getToEmailAddr() {
		// TODO Auto-generated method stub
		return emailAddr;
	}
	/*
	 * Method formats the Email content for display
	 */
	public String emailDisplayFormatter(String strPort,String strUnit,String strBookingNum,double strGateTemp,double strBkgTemp )
	{
		DateFormat dateFormat = new SimpleDateFormat("yy-MMM-dd HHmm");
        Date date = new Date();
        String currDate = dateFormat.format(date); 
        
		String message = "<HTML><BODY><table>"+
			"<table border='0'>"+
				"<tr><td align='left'>Date<b>:</b> "+currDate+"</td></tr>"+
				"<tr><table width='100%' border='0' cellspacing='15'>" +
				"<td align='center'>MATSON TERMINALS - CONTAINER YARD</td></table></tr>"+
			"</table>"+
			"<table border='0'>"+
				"<tr><td align='left'>From<b>:</b> Gate</td></tr>"+
				"<tr><td align='left'>Subject<b>:</b> Gate Booked Temperature Discrepancy</td></tr>"+
			"</table>"+
			"<table border='0'>"+
				"<tr><table width='100%' border='0' cellspacing='15'><td></td></table></tr>"+ 
				"<tr align='left'>"+
				"<td>Container<b>:</b> "+strUnit+"&nbsp;&nbsp;&nbsp;</td>"+
				"<td>Booking<b> # :</b> "+strBookingNum+"&nbsp;&nbsp;&nbsp;</td>"+
				"<td>Gate Temp<b>:</b> "+strGateTemp+"&nbsp;&nbsp;&nbsp;</td>"+
				"<td>Book Temp<b>:</b> "+strBkgTemp+"&nbsp;&nbsp;&nbsp;</td>"+
				"<td>Port<b>:</b> "+strPort+"&nbsp;&nbsp;&nbsp;</td>"+
				"</tr>"+
			"</table>"+
		"</table></BODY></HTML>";
		
		return message;
	}
	
	/**
	 * Log the msg to the database
	 * Currently does nothing.
	 */
	public void logMsg(String contents) {
		
		
	}
}
