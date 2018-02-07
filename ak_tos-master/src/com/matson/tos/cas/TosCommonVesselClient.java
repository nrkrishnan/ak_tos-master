/**
 *  @Author  Raghu Pattangi
 *  @Date    10/15/2012
 *  This class is to invoke CAS CityClient to get the trade of the load port OR to get the vessel details etc. 
 */
package com.matson.tos.cas;

import com.matson.cas.erd.client.VesselClient;
import com.matson.tos.util.TosRefDataUtil;

public class TosCommonVesselClient  extends VesselClient{
	
	public static String CAS_REF_WS_URI = TosRefDataUtil.getValue("CAS_REF_WS_URI");
	//testing check in
	//public static String CAS_REF_WS_URI = CAS_REF_WS_URI = "http://dev.erd.cas.matson.com/casreferenceservice";
	
	public static String CAS_REF_WS_USER_NAME = TosRefDataUtil.getValue("CAS_REF_WS_USER_NAME");
	public static String CAS_REF_WS_PASSWORD = TosRefDataUtil.getValue("CAS_REF_WS_PASSWORD");
	
 	public TosCommonVesselClient(){
		System.out.println("URL "+CAS_REF_WS_URI+" "+CAS_REF_WS_USER_NAME+" "+CAS_REF_WS_PASSWORD);
		setUrl(CAS_REF_WS_URI);
		setUsername(CAS_REF_WS_USER_NAME);
		setPassword(CAS_REF_WS_PASSWORD);
	}

}
