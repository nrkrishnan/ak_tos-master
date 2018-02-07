/**
 *  @Author  Raghu Pattangi
 *  @Date    10/15/2012
 *  This class is to invoke CAS CityClient to get the trade of the load port OR to get the vessel details etc. 
 */
package com.matson.tos.cas;



import org.apache.log4j.Logger;

import com.matson.cas.erd.client.CityClient;
import com.matson.tos.util.TosRefDataUtil;

public class TosCommonCityClient  extends CityClient {
	private static Logger logger = Logger.getLogger(TosCommonCityClient.class);
	public static String CAS_REF_WS_URI = TosRefDataUtil.getValue("CAS_REF_WS_URI"); //"http://10.3.4.179:8203/casreferenceservice"; 
	// Work around for testing
	//public static String CAS_REF_WS_URI = CAS_REF_WS_URI = "http://dev.erd.cas.matson.com/casreferenceservice";
	
	public static String CAS_REF_WS_USER_NAME = TosRefDataUtil.getValue("CAS_REF_WS_USER_NAME"); //"SB"; 
	public static String CAS_REF_WS_PASSWORD = TosRefDataUtil.getValue("CAS_REF_WS_PASSWORD"); //"matson123"; 
	
 	public TosCommonCityClient(){
 		logger.info("URL "+CAS_REF_WS_URI+" "+CAS_REF_WS_USER_NAME+" "+CAS_REF_WS_PASSWORD);
		setUrl(CAS_REF_WS_URI);
		setUsername(CAS_REF_WS_USER_NAME);
		setPassword(CAS_REF_WS_PASSWORD);
	}

}
