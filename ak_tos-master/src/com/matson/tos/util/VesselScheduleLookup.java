/*
*********************************************************************************************
* Srno   Date		AuthorName		Change Description
* A1    04/06/10    Glenn Raposo	Added code to write output messages to a file	
*********************************************************************************************
*/
package com.matson.tos.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import com.matson.sched.ArrivalEvent;
import com.matson.sched.DepartureEvent;
import com.matson.sched.NQ86DepartureEvent;
import com.matson.sched.PortEvent;
import com.matson.sched.Schedule;
import com.matson.sched.Voyage;
import com.matson.sched.view.PortCallView;
import com.matson.sched.view.PortCallViewBean;
/*
import com.matson.sched.ws.ScheduleWS;
import com.matson.sched.ws.ScheduleWSPort;
import com.matson.sched.ws.ScheduleWS_Impl;
*/

import com.matson.tos.jaxb.snx.TVesselVisit;
import com.matson.tos.vo.VesselScheduleByPort;
import com.matson.www.fssWS.ScheduleWS.ScheduleWSLocator;
import com.matson.www.fssWS.ScheduleWS.ScheduleWSPort;



public class VesselScheduleLookup {
	private String startDateOffsetString = TosRefDataUtil.getValue( "FSS_START_OFFSET"); 
	private String endDateOffsetString = TosRefDataUtil.getValue( "FSS_END_OFFSET");
	private String serviceWsdl =  TosRefDataUtil.getValue( "FSS_WSDL");

	
	private int startDateOffset = 0;
	private int endDateOffset = 0;
	public static String storeDir = "/home/logs/applogs/TOS/fss";
	private SimpleDateFormat formater = new SimpleDateFormat("MM/dd/yyyy");
	private static Logger logger = Logger.getLogger(VesselScheduleLookup.class);
	
	public Schedule getSchedule() throws Exception{
		
		if(startDateOffset == 0) {
			try {
				startDateOffset = -Integer.parseInt(startDateOffsetString);
			} catch (Exception e) {
				startDateOffset = 0;
				throw e;
			}
		}
		
		if(endDateOffset == 0) {
			try {
				endDateOffset = Integer.parseInt(endDateOffsetString);
			} catch (Exception e) {
				endDateOffset = 0;
				throw e;
			}
		}
		
		GregorianCalendar start = new GregorianCalendar();
		GregorianCalendar end = new GregorianCalendar();
		start.add(GregorianCalendar.DATE, startDateOffset);
		end.add(GregorianCalendar.DATE, endDateOffset);
		
		ScheduleWSLocator locator = new ScheduleWSLocator();
		locator.setScheduleWSPortEndpointAddress(serviceWsdl);
		ScheduleWSPort port = locator.getScheduleWSPort();
		//ScheduleWS_Impl service = new ScheduleWS_Impl(serviceWsdl);
		//ScheduleWSPort port = service.getScheduleWSPort();
		logger.debug("Start Date="+formater.format(start.getTime())+" end date="+formater.format(end.getTime()));
		String result = port.getSchedule(formater.format(start.getTime()), formater.format(end.getTime()), true);
		FileWriterUtil.writeFile(result, storeDir,"VV_Input",".xml");
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        InputStream is = new ByteArrayInputStream ( result.getBytes());
        Document doc = docBuilder.parse ( is);
        Schedule sched = new Schedule();
        sched.loadFromXml(doc.getDocumentElement());
        return sched;
	} 
	

	
	/**
	 * @param args
	 */
	
	public static void main(String[] args) throws Exception{
		int days = 42;
		Date now = new Date();
		System.out.println(now.getTime() + (long)days*(long)(24*3600*1000));
		System.out.println(now.getTime() );
		logger.error("Start");
		long time = System.currentTimeMillis();
		VesselScheduleLookup lookup = new VesselScheduleLookup();
		/*
		ScheduleWS_Impl service = new ScheduleWS_Impl("http://qa.matson.com:8001/fssWS/ScheduleWS?WSDL");
		ScheduleWSPort port = service.getScheduleWSPort();
		String result = port.getSchedule("9/09/2009", "12/22/2009", true);
		//logger.error("Result="+result);
		System.out.println("time="+(System.currentTimeMillis()-time));
		//System.exit(-1);
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        InputStream is = new ByteArrayInputStream ( result.getBytes());
        Document doc = docBuilder.parse ( is);
        Schedule sched = new Schedule();
        sched.loadFromXml(doc.getDocumentElement());
        System.out.println(sched.getDescription());
        */
	/*A101	VesselScheduleLookup lookup = new VesselScheduleLookup();
		Schedule sched = lookup.getSchedule();
        System.out.println("time="+(System.currentTimeMillis()-time));
        VesScheduleFileProcessor proc = new VesScheduleFileProcessor ();
        proc.processSchedule(sched); */
        /*
        PortCallView view = new PortCallView(sched);
        Iterator portCallIterator = view.getPortCalls().iterator();
        
         Snx snx = new Snx();
        SnxMessageHandler snxMsgHandler =  new SnxMessageHandler(
			"com.matson.tos.jaxb.snx", "com.matson.tos.jatb", "",
				AbstractMessageHandler.TEXT_TO_XML);
        if(portCallIterator.hasNext()) {
        	PortCallViewBean portCall = (PortCallViewBean)portCallIterator.next();
        	System.out.println(portCall.getDeparture().getBerth() );
        	PortCallViewBean p = portCall;
        //	while(true) {
        //		p = p.getNext();
        //		if(p == null) break;
        //		System.out.println(p.getPort().getCode()+" "+p.getArrival().getVessel().getCode() + ((Voyage)p.getArrival().getVoyages().get(0)).getVoyageID());
        //	}
        	
        	TVesselVisit visit = proc.processPortCall(portCall);
        	snx.getVesselVisit().add(visit);
        	snxMsgHandler.setXmlObj(snx);
			String xmlStr = snxMsgHandler.getXmlStr();
        	System.out.println(xmlStr);
//        	System.out.println(((Voyage)portCall.getArrival().getVoyages().get(0)).getVoyageID().numToString());
        	//System.out.println(portCall.getArrival().get);
        	
        	
        	
        }
        
        */
		
	}

	public Date getArrivalDateByVVDAndPort(String inputVessel, String inputVoyage, String inputDir, String port) {
		Date arrivalDate = null;
		
		try {
			VesselScheduleLookup lookup = new VesselScheduleLookup();
			Schedule schedule = lookup.getSchedule();
			Schedule filterScheduleByPort = filterScheduleByPort(port,"A",schedule);
			PortCallView view = new PortCallView(filterScheduleByPort);
			List portCalllist =  view.getPortCalls();
			Iterator portCallListItr = portCalllist.iterator();
			List eventlist = new ArrayList();
			while(portCallListItr.hasNext()){
				PortCallViewBean portCall = (PortCallViewBean) portCallListItr.next();
				eventlist.add(portCall.getPortEvents());
			}
			List<ArrivalEvent> arrivalEventList = new ArrayList<ArrivalEvent>();
			if(eventlist != null && eventlist.size()>0){
				for(int j=0;j<eventlist.size();j++){
					List events  = (List) eventlist.get(j);	
					logger.info("PortEvents "+j+" size=="+events.size());
					if(events!=null && events.size()>0){
						if(events.size()<2){
							Object arrivalOrDepartureObj =events.get(0);
							Class className = arrivalOrDepartureObj.getClass();
							if(className.getName().equals("com.matson.sched.ArrivalEvent")){
								arrivalEventList.add((j-1),(ArrivalEvent) arrivalOrDepartureObj);
							}
						}else{
							arrivalEventList.add(j,(ArrivalEvent) events.get(0));//as per new output xml
						}
					}
				}
			}
			
			for(int m=0;m<arrivalEventList.size();m++) {
				ArrivalEvent arrivalEvent = (ArrivalEvent)arrivalEventList.get(m);
				if(arrivalEvent != null){
					if (inputVessel.equalsIgnoreCase(arrivalEvent.getVessel().getCode())) {
						List arrivalVoyageList = arrivalEvent.getVoyages();
						Iterator arrvVoyageIterator = arrivalVoyageList.iterator();
						while(arrvVoyageIterator.hasNext()) {
							Voyage voyage = (Voyage) arrvVoyageIterator.next();
							if(voyage != null){
								if (inputVoyage.equals(voyage.getVoyageID().getNumber()) && inputDir.equals(voyage.getVoyageID().getDirection())){
									logger.info("arrival port : "+arrivalEvent.getPort().getCode());
									logger.info("arrival time : "+arrivalEvent.getTime());
									arrivalDate = arrivalEvent.getTime();
									}
								}
							}
						}
					}
				}
			}catch(Exception ex){
			ex.printStackTrace();
		}
		logger.info("arrival date is "+arrivalDate);
		return arrivalDate;
	}
	
	public static Schedule filterScheduleByPort(String portCode, String arrDep, Schedule sched) {
		Schedule filteredSched = new Schedule ();
		Iterator portEventIterator = sched.getPortEvents().iterator();
		PortEvent pe = null;
		PortEvent dEvent = null;
		PortEvent aEvent = null;
		String eventPort = null;
		if (portCode==null) {
			return sched;
		} else {
			portCode = portCode.toUpperCase();		  
		}
		if (arrDep != null){
			arrDep=arrDep.toUpperCase();
		}	
		while(portEventIterator.hasNext()) {
			pe = (PortEvent)portEventIterator.next();
			if ("D".equals(pe.getCode())){
				dEvent = (DepartureEvent)pe;
				eventPort = dEvent.getPort().getCode();
				if((!"A".equals(arrDep))&&portCode.equals(eventPort)){		  					
					filteredSched.getPortEvents().add(dEvent.makeCopy());
	 
				}			  
			} else if ("A".equals(pe.getCode())){
				aEvent = (ArrivalEvent)pe;
				eventPort = aEvent.getPort().getCode();
				if((!"D".equals(arrDep))&&portCode.equals(eventPort)){		  					
					filteredSched.getPortEvents().add(aEvent.makeCopy());
				}	
			}
		}
		filteredSched.createOriginalSchedule();
		return filteredSched;
	}
	
	public Date getArrivalDateByVVD(String vvd,String portCode) {
		System.out.println("getArrivalDateByVVD begin -:"+vvd+" - "+portCode);
		Date arrDate = null;
		String vessel=null;
		String voyage = null;
		String direction = null;
		try {
			if (vvd !=null){
				vessel=vvd.substring(0,3);
				voyage= vvd.substring(3,6);
				direction = vvd.substring(6);
			}
			ScheduleWSLocator scheduleWS = new ScheduleWSLocator();
			scheduleWS.setScheduleWSPortEndpointAddress(serviceWsdl);
			//ScheduleWS scheduleWS = new ScheduleWS_Impl(serviceWsdl);
			//ScheduleWS_Impl service = new ScheduleWS_Impl(serviceWsdl);
			ScheduleWSPort port = null;
			port = scheduleWS.getScheduleWSPort();
			//ScheduleWSPort port = service.getScheduleWSPort();
			String result = port.getScheduleByVvdAndPort(vessel, 
					Integer.parseInt(voyage),direction,portCode,"A");
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			InputStream is = null;
			if(result != null) {
				is = new ByteArrayInputStream(result.getBytes());
			}
			Document doc = docBuilder.parse (is);
			Schedule sched = new Schedule();
			sched.loadFromXml(doc.getDocumentElement());  
			PortCallView view = new PortCallView(sched);
			List portCalllist =  view.getPortCalls();
			if(portCalllist == null || portCalllist.size()<=0) {
				return null; 	
			}
			Iterator portCallListItr = portCalllist.iterator();
			List eventlist = new ArrayList();
			while(portCallListItr.hasNext()){
				PortCallViewBean portCall = (PortCallViewBean) portCallListItr.next();
				eventlist.add(portCall.getPortEvents());
			}
			List<ArrivalEvent> arrivalEventList = new ArrayList<ArrivalEvent>();
			List<DepartureEvent> departureEventList = new ArrayList<DepartureEvent>();
			System.out.println("eventlist size=="+eventlist.size());
			if(eventlist != null && eventlist.size()>0){
				for(int j=0;j<eventlist.size();j++){
					List events  = (List) eventlist.get(j);	
					if(events!=null && events.size()>0){
						if(events.size()<2){
							Object arrivalOrDepartureObj =events.get(0);
							Class className = arrivalOrDepartureObj.getClass();
							if(className.getName().equals("com.matson.sched.ArrivalEvent")){
								arrivalEventList.add((j),(ArrivalEvent) arrivalOrDepartureObj);
							}else{
								departureEventList.add(j,(DepartureEvent) arrivalOrDepartureObj);
							}
						}else{
							arrivalEventList.add(j,(ArrivalEvent) events.get(0));//as per new output xml
							departureEventList.add(j,(DepartureEvent) events.get(1));
						}
					}
				}
			}
			ArrivalEvent arrivalEvent1 = (ArrivalEvent)arrivalEventList.get(0);
			for(int m=0;m<arrivalEventList.size();m++) {
				ArrivalEvent arrivalEvent = (ArrivalEvent)arrivalEventList.get(m);
				if(arrivalEvent != null){
					arrDate = arrivalEvent.getTime();
				}
			}
		} catch (Exception ex){
			ex.printStackTrace();
		}
		System.out.println("getArrivalDateByVVD end");
		return arrDate;
		
	}
	
	public List searchVoyagesByPort(String portCode,String vesselOperator,Calendar startArrivalDate,
			boolean operation, boolean booking, boolean proforma,boolean webBooking){
		System.out.println("searchVoyagesByPort Start");
		StringBuffer logPortTypes = new StringBuffer("Port Types:");
    	List arrivalDepartureList = new ArrayList();
		try {
			String result = null;
			if(portCode != null && startArrivalDate != null){
				ScheduleWSLocator scheduleWS = new ScheduleWSLocator();
				scheduleWS.setScheduleWSPortEndpointAddress(serviceWsdl);
				//ScheduleWS scheduleWS = new ScheduleWS_Impl(serviceWsdl);
				//ScheduleWS_Impl service = new ScheduleWS_Impl(serviceWsdl);
				ScheduleWSPort port = null;
				port = scheduleWS.getScheduleWSPort();
				result = port.searchVoyagesByPort(portCode, vesselOperator, startArrivalDate, false, false, false, false);
			}
			//call this method to parse the output xml and get the arrival and departure events
			//pass portcode to call filterScheduleByPort method is its null it will not call //pass portcode to call filterScheduleByPort method
			if(result != null){
				DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
				InputStream is = new ByteArrayInputStream ( result.getBytes());
				Document doc = docBuilder.parse(is);
				Schedule sched = new Schedule();
				sched.loadFromXml(doc.getDocumentElement());     
				PortCallView view = new PortCallView(sched);          
				Iterator portCallIterator = view.getPortCalls().iterator();
				while(portCallIterator.hasNext()) {
				  PortCallViewBean portCall = (PortCallViewBean)portCallIterator.next();
				  NQ86DepartureEvent departure = (NQ86DepartureEvent)portCall.getDeparture();
				  ArrivalEvent arrival =portCall.getArrival();			  
				  if (departure != null && arrival != null){
				    Voyage voy = (Voyage)portCall.getDeparture().getVoyages().get(0); 
				    VesselScheduleByPort vesselScheduleByPortDTO = new VesselScheduleByPort();
					if(voy!=null){
						 	vesselScheduleByPortDTO.setDirectionSequence(voy.getVoyageID().getDirection());
						 	if(departure.getVessel()!=null && departure.getVessel().getOperator()!=null)
						    	vesselScheduleByPortDTO.setServiceCode(departure.getVessel().getOperator());
						 	if(departure.getVessel()!=null && departure.getVessel().getCode()!=null)
						    	vesselScheduleByPortDTO.setVesselCode(departure.getVessel().getCode());			    
						  	if(voy.getVoyageID()!=null && voy.getVoyageID().getNumber()!=0)
						    	vesselScheduleByPortDTO.setVoyageNumber(String.valueOf(voy.getVoyageID().getNumber()));
						    if(voy.getVoyageID()!=null && voy.getVoyageID().getDirection()!=null)
						    	vesselScheduleByPortDTO.setDirectionSequence(voy.getVoyageID().getDirection());			  
						    if(arrival.getTime()!=null){
						    	vesselScheduleByPortDTO.setArrivalDate(arrival.getTime());
						    	vesselScheduleByPortDTO.setArrivalTime(arrival.getTime().toString());
						    	vesselScheduleByPortDTO.setArrivalDay((arrival.getTime().toString()).substring(0,3));
						    }
						    if(arrival.getStatus()!=null)
						    	vesselScheduleByPortDTO.setArrivalST(arrival.getStatus().getCode());			    
						    if(departure.getTime()!=null){
						    	vesselScheduleByPortDTO.setDepartDate(departure.getTime());
						    	vesselScheduleByPortDTO.setDepartTime(departure.getTime().toString());
						    	vesselScheduleByPortDTO.setDepartDay((departure.getTime().toString()).substring(0,3));
						    }
						    vesselScheduleByPortDTO.setNextPort(departure.getNq86NextPortCode());
						    if(departure.getStatus()!=null)
						    	vesselScheduleByPortDTO.setDepartST(departure.getStatus().getCode());
						    StringBuffer portTypes = new StringBuffer();
							if(voy.isOperational())
								portTypes.append("O ");
							else
								portTypes.append("  ");
							if(voy.isBookable())
								portTypes.append("B ");
							else
								portTypes.append("  ");
							if(voy.isProForma())
								portTypes.append("P ");
							else
								portTypes.append("  ");
							if(voy.isWebBookable())
								portTypes.append("W");
							else
								portTypes.append(" ");
							logPortTypes.append("\n\t" + vesselScheduleByPortDTO.getNextPort()+ " " + vesselScheduleByPortDTO.getVesselCode() 
									+ vesselScheduleByPortDTO.getVoyageNumber()+ vesselScheduleByPortDTO.getDirectionSequence() + " " + portTypes.toString());						
							vesselScheduleByPortDTO.setPortTypes(portTypes.toString());
					 }
					 if(vesselScheduleByPortDTO.getArrivalDate() != null && vesselScheduleByPortDTO.getDepartDate()!=null)
						 arrivalDepartureList.add(vesselScheduleByPortDTO);
				  }
			  }
			}   
		}catch (Exception e) {
			System.out.println("searchVoyagesByPort Exception occured: " + e);
		}
		System.out.println("searchVoyagesByPort End");
		return arrivalDepartureList;
	}
	
	public HashMap getNextOutBoundVesvoy(String dischPort, Calendar startArrivalDate,String service){
		System.out.println("VesselScheduleLookup.getNextVessel begin");
		List vesselList = new ArrayList();
		HashMap localMap = new HashMap();
		
		try{
			List vesselSheduleList = searchVoyagesByPort("HON", service, startArrivalDate, false, false, false, false);
			for (int i=0;i<vesselSheduleList.size();i++){
				VesselScheduleByPort vesselShedPort = (VesselScheduleByPort)vesselSheduleList.get(i);
				if (vesselShedPort.getNextPort().equals(dischPort)){
					localMap.put(vesselShedPort.getNextPort()+vesselShedPort.getVesselCode(), vesselShedPort.getVesselCode()+vesselShedPort.getVoyageNumber()+"-"+vesselShedPort.getDepartDate());
				}
			}
		} catch(Exception ex){
			System.out.println("VesselScheduleLookup.getNextVessel exception");
			ex.printStackTrace();
		}
		System.out.println("VesselScheduleLookup.getNextVessel end");
		return localMap;
		
	}
	
	public HashMap getSheduleForOutBoundVes(String nextPortCode,String startDate, String endDate,Date arrivalDate){
		logger.info("getSheduleForOutBoundVes start "+startDate+" "+endDate+" "+nextPortCode+" "+arrivalDate);
		HashMap veselMap = new HashMap();
		TVesselVisit visit = new TVesselVisit();
		String homePort = null;
		String vessel = null;
		String skipVessel = "DEFAULT";
		try {
			homePort = "HON";
			ScheduleWSLocator service = new ScheduleWSLocator();
			service.setScheduleWSPortEndpointAddress(serviceWsdl);
			//ScheduleWS_Impl service = new ScheduleWS_Impl(serviceWsdl);
			ScheduleWSPort port = service.getScheduleWSPort();
			String result = port.getSchedule(startDate, endDate, true);
			//System.out.println("result :"+result);
			FileWriterUtil.writeFile(result, storeDir,"VV_Input",".xml");
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
	        InputStream is = new ByteArrayInputStream ( result.getBytes());
	        Document doc = docBuilder.parse ( is);
	        Schedule sched = new Schedule();
	        sched.loadFromXml(doc.getDocumentElement());
	        PortCallView view = new PortCallView(sched);
			List portCalllist =  view.getPortCalls();
			long dateDiff = 0;
			
			Calendar cal = new GregorianCalendar(1, 1, 1);
			Date dateToPick1 = cal.getTime();
			cal = new GregorianCalendar(9999, 12, 31);
			Date dateToPick2 =  cal.getTime();
			
			//Date arrDate = CalendarUtil.convertStrgToDateFormat(startDate);
			System.out.println("portCalllist size=="+portCalllist.size()); 
			if(portCalllist == null || portCalllist.size()<=0) {
				return null; 	
			}
			Iterator portCallListItr = portCalllist.iterator();
			List eventlist = new ArrayList();
			while(portCallListItr.hasNext()){
				PortCallViewBean portCall = (PortCallViewBean) portCallListItr.next();
				//System.out.println("portCall.getPort().getCode() :"+portCall.getPort().getCode()+" - "+portCall.getDeparture().getTime()+ " -"+portCall.getDeparture().getVessel().getCode()+ " - skip :"+skipVessel);
				if(homePort.equals(portCall.getPort().getCode()) && arrivalDate.before(portCall.getDeparture().getTime()) && !portCall.getDeparture().getVessel().getCode().equals(skipVessel))
				{  
					if ("HON".equalsIgnoreCase(homePort)){
						dateToPick1 = portCall.getDeparture().getTime();
					}
					logger.info("dateToPick1 :"+dateToPick1);
					logger.info("Inside portCall.getPort().getCode() :"+portCall.getPort().getCode()+" - "+portCall.getDeparture().getTime());
				   if ("MAT".equals(portCall.getDeparture().getVessel().getOperator())){
						if (!"Barge".equals(portCall.getDeparture().getVessel().getType().getDescription()) || !"YBBarge".equals(portCall.getDeparture().getVessel().getType().getDescription())) {
							PortCallViewBean nextPortCall = portCall.getNext();
							if (nextPortCode.equals(nextPortCall.getPort().getCode())) {
								List voyages = nextPortCall.getArrival().getVoyages();
								Voyage voyage = (Voyage)voyages.get(0);
								if ( nextPortCall.getArrival().getVessel().getCode().equals(portCall.getDeparture().getVessel().getCode())) {
									logger.info("Date1 :"+dateToPick1+" Date2 :"+dateToPick2);
									if (dateToPick1.before(dateToPick2) || dateToPick1.equals(dateToPick2)) {
										veselMap.put(nextPortCall.getPort().getCode(), nextPortCall.getArrival().getVessel().getCode()+voyage.getVoyageID().numToString());
										System.out.println("in map "+veselMap.toString());
										dateToPick2.setTime(dateToPick1.getTime());
									}
									logger.info("dateToPick2 :"+dateToPick2);
									homePort = "HON"; 								
								    continue;
								}else {
									homePort = "HON";
									continue;
								}
							} else {
								homePort = nextPortCall.getPort().getCode();
								continue;
						}
						}
					}
				}else {
					if ("SEA".equals(nextPortCode) && "LAX".equals(nextPortCode) && "OAK".equals(nextPortCode)) {
						skipVessel = portCall.getDeparture().getVessel().getCode();
					}
					homePort = "HON";
				}
			}
	
		}catch(Exception ex) {
			ex.printStackTrace();
		}
		logger.info("getSheduleForOutBoundVes end ");
		 return veselMap;
	}
	
	public HashMap getNextOutboundVoyageForBarge(String vessel, String homePort, Date inputDate) {

		logger.info("getNextOutboundVoyageForBarge start "+vessel+" "+inputDate);
		System.out.println("getNextOutboundVoyageForBarge start "+vessel+" "+inputDate);
		HashMap veselMap = new HashMap();
		TVesselVisit visit = new TVesselVisit();
		String skipVessel = "DEFAULT";
		try {
			Calendar myCal = new GregorianCalendar();
			myCal.setTime(inputDate);
			ScheduleWSLocator service = new ScheduleWSLocator();
			service.setScheduleWSPortEndpointAddress(serviceWsdl);
			//ScheduleWS_Impl service = new ScheduleWS_Impl(serviceWsdl);
			ScheduleWSPort port = service.getScheduleWSPort();
			String result = port.searchVoyagesByVessel(vessel, myCal, true, true, true, true);
			//System.out.println("result :"+result);
			FileWriterUtil.writeFile(result, storeDir,"VV_Input",".xml");
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
	        InputStream is = new ByteArrayInputStream ( result.getBytes());
	        Document doc = docBuilder.parse ( is);
	        Schedule sched = new Schedule();
	        sched.loadFromXml(doc.getDocumentElement());
	        PortCallView view = new PortCallView(sched);
			List portCalllist =  view.getPortCalls();
			//Date arrDate = CalendarUtil.convertStrgToDateFormat(startDate);
			System.out.println("portCalllist size=="+portCalllist.size()); 
			if(portCalllist == null || portCalllist.size()<=0) {
				return null; 	
			}
			Iterator portCallListItr = portCalllist.iterator();
			List eventlist = new ArrayList();
			while(portCallListItr.hasNext()){
				PortCallViewBean portCall = (PortCallViewBean) portCallListItr.next();
				//System.out.println("portCall.getArrival().getTime() "+portCall.getArrival().getTime()+" - "+portCall.getPort().getCode());
				if(portCall != null && portCall.getArrival() != null && 
						inputDate.before(portCall.getArrival().getTime()) && "HON".equalsIgnoreCase(portCall.getPort().getCode()))
				{   
					System.out.println("Inside 1st if condition");
				   if ("MAT".equals(portCall.getDeparture().getVessel().getOperator())){
						 	//PortCallViewBean nextPortCall = portCall.getNext();
							//if ("HON".equals(portCall.getPort().getCode())) {
					   		System.out.println("Inside 2nd if condition");
							List voyages = portCall.getArrival().getVoyages();
							Voyage voyage = (Voyage)voyages.get(0);
							System.out.println("voyage :"+voyage.getVoyageID().numToString()+voyage.getVoyageID().getDirection());
							List voyages1 = portCall.getDeparture().getVoyages();
							Voyage voyage1 = (Voyage)voyages1.get(0);
							System.out.println("voyage1 :"+voyage1.getVoyageID().numToString()+voyage1.getVoyageID().getDirection());
							veselMap.put(homePort, portCall.getDeparture().getVessel().getCode()+voyage1.getVoyageID().numToString()+voyage1.getVoyageID().getDirection());
							break;
						//	}
				   }
				}
			}
	
		}catch(Exception ex) {
			ex.printStackTrace();
		}
		System.out.println("getNextOutboundVoyageForBarge end "+veselMap.toString());
		 return veselMap;
	
		
	}
	
	

}
