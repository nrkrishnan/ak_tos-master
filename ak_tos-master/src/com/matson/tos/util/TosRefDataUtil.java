package com.matson.tos.util;

import org.apache.log4j.Logger;

import com.matson.cas.refdata.RefDataException;
import com.matson.cas.refdata.RefDataLookup; 
import com.matson.cas.refdata.mapping.TosHoldPermData;
import com.matson.cas.refdata.mapping.TosVesVisitCliOperator;
import com.matson.cas.refdata.mapping.TosRefData;

/**
 * 7/15/09  Added RefData
 * @author skb
 *
 */
public class TosRefDataUtil {
	private static Logger logger = Logger.getLogger(TosRefDataUtil.class);
	
	public static int getClosedTime(String id) {
		String key;
		if(id.length() > 6) {
			key = "CLOSE_LONG_HAUL";
		} else {
			key = "CLOSE_BARGE";
		}
		String value = getValue(key);
		if(key == null) return -4;
		int result = -4;
		try {
			result = Integer.parseInt(value);
			result = -result;
			return result;
		} catch (Exception e) {
			return -4;
		}
	}
		
	public static void clearCache() {
		RefDataLookup.clearCache("com.matson.cas.refdata.mapping.TosRefData");
	}
	
	public static String getValue( String key) {
		String ret = null;
		try {
			TosRefData data = (TosRefData)RefDataLookup.queryById("com.matson.cas.refdata.mapping.TosRefData", key);
			//	ret = data.getValue();
			    
			ret = data != null ? data.getValue() : ret ;
			    
		} catch ( RefDataException rex) {
			logger.error( "TosAppRefData Error: key=" + key, rex);
		} 
	/*	catch (NullPointerException ex)
		{
			logger.error( "TosAppRefData Error: key=" + key+ "does not exist or value is Null/Blank", ex);
		}
	*/
		return ret;
	}
	
	public static TosHoldPermData getHoldPermObj( String acetsCode) {
		TosHoldPermData obj = null;
		try {
			obj = (TosHoldPermData)RefDataLookup.queryById("com.matson.cas.refdata.mapping.TosHoldPermData", acetsCode);
		} catch ( Exception ex) {
			logger.debug( "Error in get obj for code:" + acetsCode, ex);
		}
		return obj;
	}
	
	public static String getCliOperatorObj( String operator) {
		TosVesVisitCliOperator obj = null;
		String operatorDesc = null;
		try {
			obj = (TosVesVisitCliOperator)RefDataLookup.queryById("com.matson.cas.refdata.mapping.TosVesVisitCliOperator", operator);
			operatorDesc = obj != null ? obj.getOperatorDesc() : null ; 
		} catch ( Exception ex) {
			logger.debug( "Error in get obj for code:" + operator, ex);
		}
		return operatorDesc;
	}
}
