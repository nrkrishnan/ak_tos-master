/*
************************************************************************************
* Srno   Date	    AuthorName  Change Description
* A1     11/02/09   KM          handling exceptions on bill of lading, destination
*                               port, commodity description as not required fields
* A2     01/20/09   GR          Changed QA URL PORT after weblogic 10 Upgrade.  
*                               Change Method extractDestinationPort
                                Removed Throws Exception for Traiff Desc & VinNumber
*************************************************************************************
*/
package com.matson.tos.util;

import java.math.BigDecimal;

import org.apache.log4j.Logger;
import org.tempuri.ManifestWSRequest_xsd.HawaiiHarbors;

import com.matson.vinsight.webservice.cars.ws.to.VINSightHDOACommodity;

/**
 * @author AZA
 *
 */
public class VINSightHelper {
	private static Logger logger = Logger.getLogger(VINSightHelper.class);
	
/*	private static final String PROD_URL = "http://10.201.1.4:9001/VINSightHDOAWebService/VINSightHDOAWebService";
	private static final String PRE_PROD_URL = "http://10.201.2.6:9001/VINSightHDOAWebService/VINSightHDOAWebService";
	//private static final String QA_URL = "http://10.201.0.6:9001/VINSightHDOAWebService/VINSightHDOAWebService";
	//A2 
	private static final String QA_URL = "http://10.201.0.6:8102/VINSightHDOAWebService/VINSightHDOAWebService";
	private static final String DEV_URL = "http://10.8.7.145:9001/VINSightHDOAWebService/VINSightHDOAWebService";
*/
	public String AG_VIN_URL = TosRefDataUtil.getValue( "AG_VIN_URL");
	//public String AG_VIN_URL = "http://10.8.7.145:9001/VINSightHDOAWebService/VINSightHDOAWebService";
	//public String AG_VIN_URL = "http://10.8.7.144:8080/service/VINSightHDOAWebService?wsdl";
	
	public static String extractOriginPort(VINSightHDOACommodity unit) throws Exception {
		String result = "";
		result = ManifestHelper.ports.get(unit.getOriginPort());
		if (result == null) {
			logger.error("origin port could not be found in the port map for commodity:  " + unit);
			throw new Exception ("origin port could not be found in the port map for commodity:  " + unit);
		}		
		if ("".equalsIgnoreCase(result) || null == result)
			throw new Exception("Origin Port cannot be null for commodity: " + unit);
		return result;
	}
	
	public static HawaiiHarbors extractDestinationPort(VINSightHDOACommodity unit) throws Exception {
		//String result = "";
		String dischargePort = unit.getDestinationPort();
		if(null == dischargePort || "".equals(dischargePort)||   dischargePort.equals("HON") 
				|| dischargePort.equals("LNI") ||dischargePort.equals("MOL") || dischargePort.equals("NAW")){
			    return HawaiiHarbors.PIO;
		    }else if(dischargePort.equals("KHI")){
		    	return HawaiiHarbors.KWE;
		    }else if(dischargePort.equals("KAH")){
		    	return HawaiiHarbors.MUA;
		    }else if(dischargePort.equals("HIL")){
		    	return HawaiiHarbors.HIL;
		    }else if(dischargePort.equals("GUM")){
		    	return HawaiiHarbors.GUM;
		    }
			return HawaiiHarbors.PIO;
			
		/*if (result == null) {
			logger.error("destination port could not be found in the port map for commodity:  " + unit);
			//A1- throw new Exception ("destination port could not be found in the port map for commodity:  " + unit);
		}
		if ("".equalsIgnoreCase(result) || null == result)
			//A1- throw new Exception("extractDestinationPort cannot be null for commodity: " + unit);
			logger.error("extractDestinationPort cannot be null for commodity: " + unit);
		return result; */
	}
	
	public static String extractMake(VINSightHDOACommodity unit) throws Exception {
		String result = "";
		result = unit.getMake();
		return result;
	}
	
	public static String extractVINNumber(VINSightHDOACommodity unit) {
		String result = "";
		result = unit.getVinNumber();
		if ("".equalsIgnoreCase(result) || null == result)
			//throw new Exception("extractVINNumber cannot be null for commodity: " + unit);
			logger.debug("extractVINNumber cannot be null for commodity: " + unit);
		return result;
	}
	
	public static String extractNonContainerNo(VINSightHDOACommodity unit) throws Exception {
		String result = "";
		result = String.valueOf(unit.getVinsightId());
		if ("".equalsIgnoreCase(result) || null == result)
			throw new Exception("extractNonContainerNo cannot be null for commodity: " + unit);
		return result;
	}
	
	public static String extractShipper(VINSightHDOACommodity unit) throws Exception {
		String result = "";
		result = unit.getShipper();
		if ("".equalsIgnoreCase(result) || null == result)
			throw new Exception("extractShipper cannot be null for commodity: " + unit);
		return result;
	}
	
	public static String extractConsignee(VINSightHDOACommodity unit) throws Exception {
		String result = "";
		result = unit.getConsignee();
		if ("".equalsIgnoreCase(result) || null == result)
			throw new Exception("Origin Port cannot be null for commodity: " + unit);
		return result;
	}
	
	public static Integer extractHazmat(VINSightHDOACommodity unit) {
		Integer result = Integer.valueOf(0);
		return result;
	}
	
	public static String extractCommodity(VINSightHDOACommodity unit) {
		String result = "";
		result = unit.getCommodityShortDescription();
		if ("".equalsIgnoreCase(result) || null == result)
			//throw new Exception("extractCommodity cannot be null for commodity: " + unit);
			logger.debug("extractCommodity cannot be null for commodity: " + unit);
		return result;
	}
	
	public static BigDecimal extractNetWeight(VINSightHDOACommodity unit) {
		BigDecimal result = BigDecimal.ZERO;
		result = BigDecimal.valueOf(unit.getNetWeight());
		return result;
	}
	
	public static String extractBillOfLadingNo(VINSightHDOACommodity unit) throws Exception {
		String result = "";
		result = unit.getBookingNumber();
		if ("".equalsIgnoreCase(result) || null == result)
			throw new Exception("extractBillOfLadingNo cannot be null for commodity: " + unit);
		return result;
	}
	
	public  String getWSURL() {
	/*	if (EnvironmentProperty.isDev()){
			return DEV_URL;
		}
		else if (EnvironmentProperty.isQA()){
			return QA_URL;
		}
		else if (EnvironmentProperty.isPreProd()){
			return PRE_PROD_URL;
		}
		else if (EnvironmentProperty.isProd()){
			return PROD_URL;
		}
		else{
			return DEV_URL;//connect to dev if env is anything else
		}
	*/
		return AG_VIN_URL;
	}
}
