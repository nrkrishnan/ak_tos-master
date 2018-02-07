package com.matson.tos.util;

import com.matson.tos.exception.TosException;
import java.math.BigDecimal;

public class UnitConversion {

	/*
	 * Method converts from Kg to lbs for USCG file weight column display
	 */
	public static String weightFromKgToLB(String kgWeight)
    {
		String lbsWeight = null;
		if(kgWeight == null || kgWeight.trim().length()==0){
			return "";
		}
	    double convtWeight = Double.parseDouble(kgWeight)* 2.20462262;
	    double result = new BigDecimal(""+convtWeight).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	    lbsWeight = String.valueOf(result);
    	return lbsWeight;
    }
	/*
	 * Method converts from LBS to KG for USCG file weight column display
	 */
	public static String weightFromLBToKg(String kgWeight)
    {
		try {
		String lbsWeight = null;
		if(kgWeight == null || kgWeight.trim().length()==0){
			return "";
		}
	    double convtWeight = Double.parseDouble(kgWeight)* 0.45359237;
	    double result = new BigDecimal(""+convtWeight).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	    lbsWeight = String.valueOf(result);
		
    	return lbsWeight;
		} catch (Exception e) {
			return "0";
		}
    }
	
	public static double celsiusToFahrenheit(String dbCelsius) throws TosException
	{
		double celsius = Double.parseDouble(dbCelsius);
		double fahr = (celsius * 9/5) + 32;
		double result = new BigDecimal(""+fahr).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
		return result;
	}
	
	public static double fahrenheitToCelsius(String dbFahrenheit) throws TosException
	{
		double fahrenheit = Double.parseDouble(dbFahrenheit);
		double fahr = (fahrenheit - 32) * 5/9;
		double result = new BigDecimal(""+fahr).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
		return result;
	}

}
