package com.matson.tos.util;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.tempuri.ManifestWSRequest_xsd.HawaiiHarbors;

import com.matson.tos.exception.ConsigneeNotFoundException;
import com.matson.tos.exception.ContainerNumberNotFoundException;
import com.matson.tos.exception.ContainerSizeNotFoundException;
import com.matson.tos.exception.ContainerWeightNotFoundException;
import com.matson.tos.exception.PortNotFoundException;
import com.matson.tos.exception.PortNotSupportedException;
import com.matson.tos.exception.ShipperNotFoundException;
import com.matson.tos.exception.ReeferTempNotFoundException;
import com.matson.tos.jaxb.snx.TDirection;
import com.matson.tos.jaxb.snx.TFlags;
import com.matson.tos.jaxb.snx.TRouting;
import com.matson.tos.jaxb.snx.TUnit;
import com.matson.tos.jaxb.snx.TUnitEquipment;
import com.matson.tos.jaxb.snx.TUnitHandling;

public class ManifestHelper {
	private static Logger logger = Logger.getLogger(ManifestHelper.class);
	public interface MANIFEST_TYPES {
		String MANIFEST_TYPE_CONTAINER = "C";
		String MANIFEST_TYPE_NON_CONTAINER = "N";		
	}
	
	public static final String AG_NAMESPACE = "http://tempuri.org/Invicta/ManifestSvc";	
	public static final String AG_HEADER_TAG = "ValidationSoapHeader";
	public static final String AG_HEADER_TAG_ELEMENT_USER = "user";
	public static final String AG_HEADER_TAG_ELEMENT_PASSWD = "password";
	public static final String EMAIL_ADDR = TosRefDataUtil.getValue( "SUPPORT_EMAIL");
	public String AG_PASSWD = TosRefDataUtil.getValue( "AG_PASSWD");
	public String AG_USER = TosRefDataUtil.getValue( "AG_USER");
	public String AG_URL = TosRefDataUtil.getValue( "AG_URL");
	
	// A101 - private static final String AG_NOTICE = TosRefDataUtil.getValue( "AG_VES_SUBMITTED_EMAIL");
	private static final String SUBJECT = "MANIFEST ERROR : SEV 3: " + System.getProperty("weblogic.name");
	
/*	private static final String AG_USER_DEV = "MP555";
	private static final String AG_USER_PROD = "MP555";
	private static final String AG_PASSWD_DEV = "12345";
	private static final String AG_PASSWD_PROD = "12345";
	public static final String DEV_URL = "http://64.129.8.55:20100/manifestsvc/manifestsvc.asmx";
	//only port is different in DEV & prod, this is what was provided by Louis in decision research
	public static final String PROD_URL = "http://64.129.8.55:20200/manifestsvc/manifestsvc.asmx";
*/	
	
	public static final String TRANSPORTER = "Matson Navigation Company";
	
	public static final String AG_HOLD = "AG";
	
	public interface TRANSACTION_TYPES {
		public static final String ADD = "A";// only add allowed for now
		public static final String UPDATE = "U";
		public static final String DELETE = "D";	
	}

	public interface CONTAINER_TYPES {
		short STANDARD =1 , REEFER =2, ROLLON =3, 
		ROLLOFF =4, PALLET = 5, BULKTAINER = 6, FLATRACK = 7, AUTORACK = 8, PLATFORM = 9, TANK = 10, GASBOTTLE = 11, 
		SWAPBODY = 12, VEHICLE = 13;
	}	
	
	public interface TEMPRATURE_SPECIFICATION {
		String CELCIUS = "C", FAHRENHEIT = "F";
	}

	public interface DISCHARGE_SERVICE {
		String CY="CY", CFS="CFS", AUT="AUT", RS= "R/S", CON="CON";
	}
	
	public interface TYPE_OF_CONSIGNEE {
		String SINGLE = "S", MULTI="M"; //Single Consignee or multiple consignee
	}
	
	public interface WEIGHT_SPECIFICATION {
		String POUND = "lb", KG = "kg", TON = "T", METRICTON = "MT";
	}
	
	public static Map<String, String> ports = new HashMap<String, String>();
	static {
//		ports.put("", "BEH");// BENTON HARBOR ROSS FIELD, MI USA
//		ports.put("", "BHB");// BAR HARBOR, ME USA
//		ports.put("", "DUT");// DUTCH HARBOR, AK USA
//		ports.put("", "FRD");// FRIDAY HARBOR, WA USA
//		ports.put("", "HBE");// BRISBANE HARBOR, BRISBANE AUSTRALIA
//		ports.put("", "HFX");// HALIFAX HARBOR, NOVA SCOTIA CANADA
//		ports.put("", "JMM");// MALMOE HARBOR HELIPORT SWEDEN
//		ports.put("", "KPH");// PAULOFF HARBOR, AK USA
//		ports.put("", "LHB");// LOST HARBOR SEA PORT, AK USA
//		ports.put("", "LVH");// LONGVIEW HARBOR, WA USA
//		ports.put("", "NZA");// AUCKLAND HARBOR NEW ZEALAND
//		ports.put("", "OBK");// NORTHBROOK SKY HARBOR, IL USA
//		ports.put("", "ODW");// OAK HARBOR, WA USA
//		ports.put("", "OLH");// OLD HARBOR, AK USA
//		ports.put("", "PTR");// PLEASANT HARBOR, AK USA
//		ports.put("", "QIS");// HOUSTON HARBOR, TEXAS USA
//		ports.put("", "QLA");// LOS ANGELES HARBOR, CA USA
		ports.put("OAK", "QOK");// OAKLAND HARBOR, CA USA
		ports.put("PDX", "QPH");// PORTLAND HARBOR, OREGON USA
//		ports.put("", "QPT");// PORTLAND HARBOR, WA USA
//		ports.put("", "QSD");// SAN DIEGO HARBOR, CA USA
		ports.put("SEA", "QSE");// SEATTLE HARBOR, WA USA
//		ports.put("", "QVH");// VANCOUVER HARBOR, VANCOUVER CANADA
//		ports.put("", "QXN");// XINGANG HARBOR CHINA
//		ports.put("", "QYH");// YOKOSUKA HARBOR JAPAN
//		ports.put("", "RCE");// ROCHE HARBOR, WA USA
//		ports.put("", "TWH");// CATALINA IS. TWO HARBORS, CA USA
//		ports.put("", "VHK");// VICTORIA HARBOR HONG KONG
//		ports.put("", "YBW");// BEDWELL HARBOR CANADA
//		ports.put("", "YGE");// GORGE HARBOR CANADA
//		ports.put("", "YGG");// GANGES HARBOR CANADA
//		ports.put("", "YMF");// MONTAGUE HARBOR CANADA
		ports.put("NAW", "NAW");// Nawiliwili Harbor, future use
		ports.put("LAX", "QLB");// LONG BEACH HARBOR, CA USA
		
		ports.put("KHI", "KWE");// Kawaihae Harbor, future use
		ports.put("KAH", "MUA");// Kahului Harbor, future use
		ports.put("HIL", "HIL");// Hilo Harbor, future use
		ports.put("GUM", "GUM");// Guam, future use
		ports.put("HON", "PIO");// Honolulu Harbor
		
		//A101
		ports.put("MOL", "MOL");
		ports.put("LNI", "LNI");
		ports.put("XMN", "XMN");
		ports.put("SHA", "SHA");
		ports.put("NGB", "NGB");
	}
	

	public static String[] extractVesselName(TUnit unit) {
		if (unit == null || unit.getRouting() == null
				|| unit.getRouting().getCarrier() == null) {
			logger.error("ERROR unit or routing or carrier information is null, this cannot be null");
			return null;
		}
		String result[] = new String[2];
		List<TRouting.Carrier> carriers = unit.getRouting().getCarrier();
		// According to Steven Bauer the carrier id will have the vessel code in
		// the 1st 3 characters
		// All the carriers will have the same vessel code as this is for a new
		// vessel feed
		// hence we can use the first carrier we got to get the vessel code &
		// voyage code
		TRouting.Carrier carrier = carriers.get(0);
		if (carrier == null || carrier.getId() == null) {
			logger.error("ERROR carrier information is null, this cannot be null");
			return null;
		}
		result[0] = carrier.getId().substring(0, 3);
		result[1] = carrier.getId().substring(3);
		//logger.debug("The vessel code recieved is: " + result[0]);
		//logger.debug("The voyage code recieved is: " + result[1]);
		return result;
	}

	public static String[] extractPorts(TUnit unit) throws PortNotFoundException, PortNotSupportedException {
		String result[] = new String[4];
		if (unit == null || unit.getRouting() == null
				|| unit.getRouting().getCarrier() == null) {
			logger.error("ERROR unit or routing or carrier information is null, this cannot be null");
			return null;
		}
		TRouting routing = unit.getRouting();
		result[0] = new String(routing.getOpl());
		result[1] = new String(routing.getDestination());
		//original port codes without transformation
		result[2] = new String(routing.getOpl());
		result[3] = new String(routing.getDestination());		
		//convert our port names to dept of agri port names
		if (ports.get(result[0]) == null) {
			throw new PortNotFoundException("Port: " + result[0] + " not found in the port mapper collection");
		}
		result[0] = ports.get(result[0]);
		result[1] = ports.get(result[1]);
		//TODO: Temporary Code added needed to be removed later
		//if there is no port default it to HONO
		if (null == result[1] || "".equalsIgnoreCase(result[1])) {
			logger.error("The Discharge port was determined to be null for the vessel is: " + result[0]);
			result [1] = ports.get("HON");
		}
		return result;
	}
	
	public static String extractDischargePort(TUnit unit){
		String result = "";
		if (unit.getRouting()!=null) {
			TRouting rout = unit.getRouting();
			//A101
			String pod = rout.getPod1();
			result = ports.get(pod);
			if(result == null){
				result = "HON";
			}else{
				result = pod;
			}
		}
		return result;
	}
	
	
	public static HawaiiHarbors getInspectionPort(TUnit unit){

		String dischargePort = extractDischargePort(unit);
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
	}

	public static String extractPortOfLoading(TUnit unit)throws PortNotFoundException{
		String result = "";
		if (unit.getRouting()!=null) {
			TRouting rout = unit.getRouting();
			String pol = rout.getPol();
			//If POL is Null get OPL
			if(pol == null || pol.length() == 0){
				pol = rout.getOpl();
			}
			result = ports.get(pol);
			//Throw Exception if Not In Mapping List
			if(result == null ||"".equalsIgnoreCase(result)){
				throw new PortNotFoundException("Port: " + pol+ " not found in the port mapper collection");
			}
		}
		return result;
	}
	
	public static String extractOriginalPortOfLoad(TUnit unit)throws PortNotFoundException{
		String result = "";
		if (unit.getRouting()!=null) {
			TRouting rout = unit.getRouting();
			String opl = rout.getOpl();
			if(opl != null && !(opl.equals("LAX") || opl.equals("OAK") || opl.equals("SEA"))){
				throw new PortNotFoundException("Port of Origin " +opl+ " not a Valid Itinerary to Fetch Vessel Details");       				
			}
			result = opl;
			//Throw Exception if Not In Mapping List
			if(result == null ||"".equalsIgnoreCase(result)){
				throw new PortNotFoundException("Original Port: " + opl+ " not found in the port mapper collection");
			}
		}
		return result;
	}
	
	public static String extractDestinationPort(TUnit unit) {
		String result = "";
		if (unit.getRouting()!=null) {
			TRouting rout = unit.getRouting();
			//A101
			String dest = rout.getDestination();
			dest = (dest == null || dest.length() == 0) ? "HON" : dest; 
			result = ports.get(dest);
			result = result == null ? dest : result;
		}
		return result;
	}
	
	public static String extractContainerNumber(TUnit unit) throws ContainerNumberNotFoundException {
		String result = null;
		if (unit.getEquipment().get(0) == null)
			return null;
		TUnitEquipment equipment = unit.getEquipment().get(0);
		result = equipment.getEqid();		
		if (result == null) throw new ContainerNumberNotFoundException("Container Number not found for equipment: " + equipment);
		return result;
	}
	
	public static String extractContainerType(TUnit unit){
		String result = "";
		if (unit.getEquipment().get(0) == null)
			return null;
		TUnitEquipment equipment = unit.getEquipment().get(0);
		result = equipment.getType();
		result = result.substring(0,1);
		if ("D".equalsIgnoreCase(result))
			result = "" + CONTAINER_TYPES.STANDARD;
		else if ("R".equalsIgnoreCase(result))
			result = "" + CONTAINER_TYPES.REEFER;
		else
			result = "" + CONTAINER_TYPES.STANDARD;//default type if it is not Dry or reefer
		return result;
	}
	
	public static int extractContainerSizeFeet(TUnit unit) throws ContainerSizeNotFoundException {
		String result = null;
		if (unit.getEquipment().get(0) == null)
			return -1;
		TUnitEquipment equipment = unit.getEquipment().get(0);
		result = equipment.getType();
		if (result == null) throw new ContainerSizeNotFoundException("Container Size not found it is mandatory for equipment: " + equipment);
		result = result.substring(1,3);
		int resulti=-1;
		try {
			resulti = Integer.parseInt(result);
		} catch (Exception e) {
			logger.error("Container Size could not be parsed!");
			throw new ContainerSizeNotFoundException("Container Size not found it is mandatory for equipment: " + equipment);
		}
		
		return resulti;		
	}
	
	public static String extractContainerCommodity(TUnit unit) {
		String result = null;
		if (unit.getHandling() == null)
			return "";
		TUnitHandling handling = unit.getHandling();
		result = handling.getRemark();
		return result;
	}
	
	public static BigDecimal extractContainerWeight(TUnit unit)  {
		BigDecimal result = null;
		if (unit.getEquipment().get(0) == null)
			return null;
		TUnitEquipment equipment = unit.getEquipment().get(0);
		result = equipment.getTareKg();		
		return result;		
	}
	
	public static BigDecimal extractContentWeight(TUnit unit) throws ContainerWeightNotFoundException {
		BigDecimal result = null;
		if (unit.getContents() == null)
			return null;
		result = unit.getContents().getWeightKg();
		if (result == null) throw new ContainerWeightNotFoundException("Container weight is mandatory not found equipment : " + unit.getEquipment().get(0));
		return result;		
	}	
	
	public static String extractShipper(TUnit unit) throws ShipperNotFoundException {
		String result = "";
		result = unit.getContents().getShipperName();
		if (result == null){
			logger.debug("Shipper Null in equipment");
		}
		return result;		
	}	
	
	public static String extractShipperId(TUnit unit) {
		String result = "";
		result = unit.getContents().getShipperId();
		return result;		
	}		
	
	public static String extractConsignee(TUnit unit) throws ConsigneeNotFoundException {
		String result = "";
		result = unit.getContents().getConsigneeName();
		if (result == null){
			//throw new ConsigneeNotFoundException("Consignee not found in equipment: " + unit.getEquipment().get(0));
			logger.debug("Consignee Null in equipment ");
		}
		return result;		
	}
	
	public static String extractFreightKind(TUnit unit) throws ConsigneeNotFoundException {
		String result = "";
		result = unit.getFreightKind();
		if (result == null){
		  logger.debug("FreightKind Null in Equipment");
		}
		return result;		
	}
	
	public static String extractContainerTypeCode(TUnit unit){
		String result = "";
		if (unit.getEquipment().get(0) == null)
			return null;
		TUnitEquipment equipment = unit.getEquipment().get(0);
		result = equipment.getType();
		if (result == null){
			logger.debug("Cntr not found in Equipment");
		}
		return result;
	}
	
	public static String extractBooking(TUnit unit){
		String result = "";
		if (unit.getContents() == null)
			return null;
		  result = unit.getContents().getBlNbr();
		  
		if (result == null){
			logger.debug("Booking not found in Equipment");
		}
		return result;
	}
	
	public static String extractHazmat(TUnit unit) {
		if (unit.getHazards()!=null && unit.getHazards().getHazard()!=null)
			return "1"; //hazardous
		else
			return "0"; //not hazardous			
	}
	
	public static String extractSealNumber(TUnit unit) {
		String result = "";
		if (unit.getSeals()!= null && unit.getSeals().getSeal1()!=null)
			result = unit.getSeals().getSeal1();
		return result;
	}
	
	public static Integer extractAGHold(TUnit unit) {
		Integer result = Integer.valueOf(0);
		if (unit.getFlags()!=null) {
			TFlags flags = unit.getFlags();
			List holds = flags.getHold();
			if (holds!=null) {
				Iterator iter = holds.iterator();
				while(iter.hasNext()) {
					TFlags.Hold hold = (TFlags.Hold)iter.next(); 
					String shold = hold.getId(); 
					if (AG_HOLD.equalsIgnoreCase(shold)) {
						result = Integer.valueOf(1);
						break;
					}
				}
			}
		}
		return result;
	}
	
	public static BigDecimal extractTemperature(TUnit unit)throws ReeferTempNotFoundException {
		BigDecimal result = null;
		if (unit.getReefer() != null) {
			if (unit.getReefer().getTempReqdC()!=null) {
				result = unit.getReefer().getTempReqdC();
			}
		}
		String CntrTypeCode = extractContainerTypeCode(unit);
		if(result == null && CntrTypeCode!= null && CntrTypeCode.startsWith("R")){
			throw new ReeferTempNotFoundException("Temperature Null in Equipment");
		}
		return result;
	}
	
	/**
	 * 
	 * @return returns the Web service URL based on the environment we are in 
	 */
	public String getWSURL() {
		return AG_URL;
	}
	
	public String getUserName() {
			return AG_USER;
	}
	
	public String getPassword() {
		return AG_PASSWD;
	}
	
	public static String extractLoadingPort(TUnit unit) {
		String result = "";
		if (unit.getRouting() != null) {
			TRouting rout = unit.getRouting();
			if (ports.containsKey(rout.getOpl()))
				result = ports.get(rout.getOpl());
			else
				logger.error("Originating port of the unit could not be found in the port hasmap for port: " + rout.getOpl());			
		}
		return result;
	}
	
	public static String extractDirection(TUnit unit) {
		String[] vesvoy;
		String result = null;
		vesvoy = extractVesselName(unit);
		String lastChar = vesvoy[1].substring(vesvoy[1].length()-1);
		if ("A".equalsIgnoreCase(lastChar) ||
			"B".equalsIgnoreCase(lastChar) ||
			"C".equalsIgnoreCase(lastChar) ||
			"D".equalsIgnoreCase(lastChar) ||
			"N".equalsIgnoreCase(lastChar) ||
			"S".equalsIgnoreCase(lastChar) ||
			"E".equalsIgnoreCase(lastChar) ||
			"W".equalsIgnoreCase(lastChar)) {
			
			result = lastChar;			
		}
		if (result == null) {
			TRouting routing = unit.getRouting();
			List<TRouting.Carrier> carriers = routing.getCarrier();
			TRouting.Carrier carrier = carriers.get(0);
			TDirection direction = carrier.getDirection();
			if ("IB".equalsIgnoreCase(direction.value()))
				result = "W";
			else 
				result = "E";
		}
		return result;
	}
	public static void sendEmail(String msg) {
		try {
			EmailSender.sendMail( EMAIL_ADDR, EMAIL_ADDR, SUBJECT, msg );
		} catch (Exception ex) {
			logger.error( "Error sending email.", ex);
		}
	}
	
	public static void sendEmail(String subject,String msg) {
		try {
			EmailSender.sendMail( EMAIL_ADDR, EMAIL_ADDR, subject, msg );
		} catch (Exception ex) {
			logger.error( "Error sending email.", ex);
		}
	}
	
	public static void sendEmail(String from,String to,String subject,String msg) {
		try {
			EmailSender.sendMail( from, to, subject, msg );
		} catch (Exception ex) {
			logger.error( "Error sending email.", ex);
		}
	}
}
