/*
*********************************************************************************************
* A1     04/07/10       Glenn Raposo		Compare Dates(ETA,ETD,ATA,ATD) in method isNotChanged
* A2     09/21/10       Glenn Raposo		TimeZone parameter 
*********************************************************************************************
*/
package com.matson.tos.vo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.log4j.Logger;

import com.matson.tos.jaxb.snx.TVesselVisit;
import com.matson.tos.util.CalendarUtil;
import java.util.GregorianCalendar; 
import java.text.SimpleDateFormat;


/**
 * VV properties that require an update:
 * Phase
 * Next Port
 * Service
 * @author xw8
 *
 * Change History
 * A1  SKB	06/03/2009	Added compare.
 */
public class VesselVisitVO {
	private String id;
	private String port;
	private String phase;
	private String nextPort;
	private Date ata;
	private Date atd;
	private Date eta;
	private Date etd;
	private String ibVyg;
	private String obVyg;
	private String carrierService;
	
	private String seqNo;
	private TVesselVisit xml;
	
	public static final String DEPARTED = "DEPARTED";
	public static final String COMPLETE = "COMPLETE";
	public static final String CANCELED = "CANCELED";
	public static final String CREATED = "CREATED";
	public static final String INBOUND = "INBOUND";
	public static final String ARRIVED = "ARRIVED";
	public static final String WORKING = "WORKING";
	public static final String CLOSED = "CLOSED";
	public static final String ARCHIVED = "ARCHIVED";
	
	private static String dateFormat = "yyyy-MM-dd HH:mm:ss.SS";
	
	private static HashMap<String, Integer> order;
	private static ArrayList<String> phaseList;
	private static Logger logger = Logger.getLogger(VesselVisitVO.class);
	
	static {
		order = new HashMap<String, Integer>();
		order.put(CREATED, new Integer(10));
		order.put(INBOUND, new Integer(20));
		order.put(ARRIVED, new Integer(30));
		order.put(WORKING, new Integer(40));
		order.put(COMPLETE, new Integer(50));
		order.put(DEPARTED, new Integer(60));
		order.put(CLOSED, new Integer(70));
		order.put(CANCELED, new Integer(80));
		order.put(ARCHIVED, new Integer(90));
		
		phaseList = new ArrayList<String>();
		phaseList.add(CREATED);
		phaseList.add(INBOUND);
		phaseList.add(ARRIVED);
		phaseList.add(WORKING);
		phaseList.add(COMPLETE);
		phaseList.add(DEPARTED);
		phaseList.add(DEPARTED);
		
	}
	
	public static String DBPhase(String phase) {
		Integer value = order.get(phase);
		return value+phase;
	}
	
	public VesselVisitVO(String id, String port) {
		this.id = id;
		this.port = port;
	}
	
	public VesselVisitVO() {
		
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getPort() {
		return port;
	}
	public void setPort(String port) {
		this.port = port;
	}
	
	public String getPhase() {
		return phase;
	}
	
	public int getPhaseId() {
		Integer value = order.get(phase);
		if(value != null ) return value.intValue();
		return  0;
	}
	
	public void setDBPhase(String phase) {
		if(phase == null || phase.length() < 3) this.phase =  null;
		this.phase = phase.substring(2);
	}
	
	public void setPhase(String phase) {
		this.phase = phase;
	}
	
	@Override
	public boolean equals(Object o) {
		if( o instanceof VesselVisitVO) {
			VesselVisitVO vo = (VesselVisitVO)o;
			if(getId() != null && getPort() != null &&
					getId().equals(vo.getId()) && getPort().equals(vo.getPort())) return true;
			
		} 
		return false;
	}
	
	/**
	 * Check if the stored VO object matches an XML SNX object.
	 * Matches on Phase
	 * @param newVV
	 * @return
	 */
	public boolean isNotChanged(TVesselVisit newVV) {
		return isNotChanged(newVV,true);
	}
	
	/**
	 * Check if the stored VO object matches an XML SNX object.
	 * @param newVV
	 * @param phase If True match on phase
	 * @return
	 */
	public boolean isNotChanged(TVesselVisit newVV, boolean phase  ) {
		//logger.debug("Is not Changed ID="+getId()+" "+newVV.getId()+", phase="+getPhase()+" "+newVV.getVisitPhase()+", nPort="+getNextPort()+" "+newVV.getNextFacility());
		//logger.debug("Is not Changed Ib="+getIbVyg()+" "+newVV.getInVoyNbr()+", Ob="+getObVyg()+" "+newVV.getOutVoyNbr()+", cs="+getCarrierService()+" "+newVV.getServiceId());
		//logger.debug("Is not Changed thisETA="+this.getEta()+" newETA="+newVV.getETA()+" thisETD="+this.getEtd()+" newETD="+newVV.getETD()+"thisATA="+this.getAta()+" newATA="+newVV.getATA()+" thisATD="+this.getAtd()+"  newATD="+newVV.getATD());
       
		if(isEqual(getId(),newVV.getId() ) && isEqual(getNextPort(), newVV.getNextFacility()) && 
				isEqual(getIbVyg(), newVV.getInVoyNbr()) && isEqual(getObVyg(), newVV.getOutVoyNbr()) && isEqual(getCarrierService(), newVV.getServiceId()) 
		) {
			boolean YB = getId() != null && getId().toUpperCase().startsWith("YB") ? true : false;
			boolean HRZ = getCarrierService() != null && getCarrierService().startsWith("HRZ") ? true : false;
			//logger.debug("isNotChanged YB="+YB+" HRZ="+HRZ);
			
			if(phase) {
				if(isEqual(getPhase(), newVV.getVisitPhase()) ) return true;
				else return false;
			}
			else if( !(YB && HRZ) && (("CREATED".equals(getPhase()) || "INBOUND".equals(getPhase())) && !datesEqual(this.getEta(),newVV.getETA())) 
					|| (("CREATED".equals(getPhase()) || "INBOUND".equals(getPhase())) &&  !datesEqual(this.getEtd(),newVV.getETD()))
					|| (("ARRIVED".equals(getPhase()) || "WORKING".equals(getPhase())) && !datesEqual(this.getAta(),newVV.getATA()))
					|| (("COMPLETE".equals(getPhase()) || "DEPARTED".equals(getPhase())) && !datesEqual(this.getAtd(),newVV.getATD()))
			){//A1
				logger.debug("vesId="+getId()+" phase="+getPhase()+" Is not Changed thisETA="+this.getEta()+" newETA="+newVV.getETA()+" thisETD="+this.getEtd()+" newETD="+newVV.getETD()+" thisATA="+this.getAta()+" newATA="+newVV.getATA()+" thisATD="+this.getAtd()+"  newATD="+newVV.getATD());
				return false;
			}
			else {
				return true;
			}
			/*
			logger.debug("Check dates");
			if(datesEqual(this.getAta(),newVV.getATA()) &&  datesEqual(this.getEta(),newVV.getETA())
			    && datesEqual(this.getAtd(),newVV.getATD()) && datesEqual(this.getEtd(),newVV.getETD()) 		
			)  return true;*/
		}
		
		return false;
	}
	
	public boolean isEqual(Object o1, Object o2) {
		if(o1 == null && o2 == null) return true;
		if(o1 == null || o2 == null) return false;
		return o1.equals(o2);
	}
	
	public boolean datesEqual(Date d, XMLGregorianCalendar c) {
		//logger.debug("Date compare this time="+d+"  Gregorian="+c);
		if(d == null && c == null) return true;
		if(d == null && c != null) return false;
		if(d != null && c == null) return false;
		
		//Test Code to Be Removed
		/*Calendar calDateTemp = Calendar.getInstance();
		calDateTemp.setTime(d);
		calDateTemp.add(Calendar.HOUR, -3);  //A2
		logger.debug("Input parameter Date="+d+" calDateTemp Milliseconds="+calDateTemp.getTimeInMillis()+"\n\r"); */
		//Test Code to Be Removed		

		//TimeZone inZone = TimeZone.getTimeZone("America/Hawaii");
		//Calendar calDate = CalendarUtil.convertTimeZone(d, inZone); //A2
		//logger.debug("CalDate Time="+calDate+"  calDate TimeMilli="+calDate.getTimeInMillis()+"\n\r");
		
		//101 - Starts
		SimpleDateFormat datefmt = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        StringBuilder dbDate = new StringBuilder(datefmt.format(d));
        
        String convtDate = CalendarUtil.convertTimeZone(dbDate.toString(),"MM/dd/yyyy HH:mm:ss","PST","HST");
        //logger.debug("dbDate =="+dbDate+"\n\r"+"  convtDate =="+convtDate);
        XMLGregorianCalendar finalDate = CalendarUtil.getXmlCalendar(convtDate, "MM/dd/yyyy HH:mm:ss");
        //logger.debug("  Gregorian Final Date="+finalDate+ "  Gregorian finalDate="+finalDate.toGregorianCalendar().getTimeInMillis()+"\n\r");
        
        //101 - Ends
		//logger.debug("  Gregorian="+c+ "  Gregorian TimeMilli="+c.toGregorianCalendar().getTimeInMillis()+"\n\r");
		//long t1 = calDate.getTimeInMillis();
		long t1 = finalDate.toGregorianCalendar().getTimeInMillis();
		long t2 = c.toGregorianCalendar().getTimeInMillis();
		//logger.debug("Date compare this time="+d+"  Gregorian="+c+" after conversion time="+t1+" Gregorian="+t2+" "+ (t1 == t2));
		
		if(t1 == t2) return true;
		return false;
	}
	
	@Override
	public int hashCode() {
		if (getId() == null) return 0;
		return getId().hashCode() ;
	}
	
	/**
	 * Not yet Completed
	 * @return
	 */
	public boolean isActive() {
		if(CREATED.equals(getPhase())  || INBOUND.equals(getPhase()) || ARRIVED.equals(getPhase()) || WORKING.equals(getPhase())) return true;
		return false;
 	}
	
	public boolean isComplete() {
		if(COMPLETE.equals(getPhase())) return true;
		return false;
	}
	
	public boolean isWorking() {
		if(WORKING.equals(getPhase())) return true;
		return false;
	}
	
	/**
	 * Live plus Completed
	 * @return
	 */
	public boolean isLive() {
		if(isComplete() || isActive()) return true;
		return false;
	}
	
	public boolean isCanceled() {
		if(CANCELED.equals(getPhase())) return true;
		return false;
	}
	
	public boolean isDeparted() {
		if(DEPARTED.equals(getPhase())) return true;
		return false;
	}
	
	public boolean isArchived() {
		if(ARCHIVED.equals(getPhase()) || CLOSED.equals(getPhase()) ) return true;
		return false;
	}	
	
	public boolean isClosed() {
		if(DEPARTED.equals(getPhase())  || CLOSED.equals(getPhase()) || CANCELED.equals(getPhase()) || ARCHIVED.equals(getPhase())) return true;
		return false;
	}

	public TVesselVisit getXml() {
		return xml;
	}

	public void setXml(TVesselVisit xml) {
		this.xml = xml;
	}

	public String getSeqNo() {
		return seqNo;
	}

	public void setSeqNo(String seqNo) {
		this.seqNo = seqNo;
	}

	public String getNextPort() {
		return nextPort;
	}

	public void setNextPort(String nextPort) {
		this.nextPort = nextPort;
	}

	public Date getAta() {
		return ata;
	}

	public void setAta(Date ata) {
		this.ata = ata;
	}

	public Date getAtd() {
		return atd;
	}

	public void setAtd(Date atd) {
		this.atd = atd;
	}

	public Date getEta() {
		return eta;
	}

	public void setEta(Date eta) {
		this.eta = eta;
	}

	public Date getEtd() {
		return etd;
	}

	public void setEtd(Date etd) {
		this.etd = etd;
	}

	public String getIbVyg() {
		return ibVyg;
	}

	public void setIbVyg(String ibVyg) {
		this.ibVyg = ibVyg;
	}

	public String getObVyg() {
		return obVyg;
	}

	public void setObVyg(String obVyg) {
		this.obVyg = obVyg;
	}

	public String getCarrierService() {
		return carrierService;
	}

	public void setCarrierService(String carrierService) {
		this.carrierService = carrierService;
	}
	
	public String nextPhase() {
		int index = phaseList.indexOf(phase)+1;
		if(index != 0 && index < phaseList.size()) {
			return phaseList.get(index);
		}
		return phase;
	}
	
	/**
	 * Port that are managed from the schedule and not by the users
	 * @return
	 */
	public boolean isManagedPort() {
		if("HON".equalsIgnoreCase(this.getPort())) return false;
		return true;
	}
	
/*	public static void main(String[] args) {
	    Calendar localTime = Calendar.getInstance();
	    localTime.set(Calendar.HOUR, 17);
	    localTime.set(Calendar.MINUTE, 15);
	    localTime.set(Calendar.SECOND, 20);

	    int hour = localTime.get(Calendar.HOUR);
	    int minute = localTime.get(Calendar.MINUTE);
	    int second = localTime.get(Calendar.SECOND);

	    System.out.printf("Local time  : %02d:%02d:%02d\n", hour, minute, second);
	    System.out.printf("Local time  : "+localTime.getTimeInMillis()+"\n");

	    Calendar germanyTime = new GregorianCalendar(TimeZone.getTimeZone("Germany"));
	    germanyTime.setTimeInMillis(localTime.getTimeInMillis());
	    hour = germanyTime.get(Calendar.HOUR);
	    minute = germanyTime.get(Calendar.MINUTE);
	    second = germanyTime.get(Calendar.SECOND);
	    int milliSeconds = germanyTime.get(Calendar.MILLISECOND);
	    
	    System.out.printf("Germany time: %02d:%02d:%02d\n", hour, minute, second);
	    System.out.printf("Germany timeMilliSeconds : "+germanyTime.getTimeInMillis()+"\n");
	    
	   //Date germanyDate = germanyTime.getTime();
	   // Calendar germanyTimeConverted =  Calendar.getInstance();

	  //  System.out.printf("Germany time Date : "+germanyTimeConverted.getTime()+"\n");
	    
	    germanyTime.add(Calendar.HOUR, -3);
	    System.out.printf("Germany getTimeInMillis: "+germanyTime.getTimeInMillis()+"\n");
	    
	  }
*/
	
/*	public static void main(String args[]){
		final Date currentTime = new Date();
		final SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, yyyy hh:mm:ss a z");

		// Give it to me in US-Pacific time. 
		sdf.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
		System.out.println("LA time: " + sdf.format(currentTime));

		// Give it to me in GMT-0 time.sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		System.out.println("GMT time: " + sdf.format(currentTime));

		// Or maybe Zagreb local time. 
		sdf.setTimeZone(TimeZone.getTimeZone("Europe/Zagreb"));
		System.out.println("Zagreb time: " + sdf.format(currentTime));

		// Even 10 hours and 10 minutes ahead of GMT
		sdf.setTimeZone(TimeZone.getTimeZone("GMT+0010"));
		System.out.println("10/10 ahead time: " + sdf.format(currentTime));	
	} */

/*	public static void main(String args[]){
		Date date = new Date();
		long lngDateTime = date.getTime();
		 System.out.println("Hon Time :"+date);
		 System.out.println("Hon TimeMilli :"+lngDateTime);
		
		 Calendar mbCal = new GregorianCalendar(TimeZone.getTimeZone("Germany"));
		 mbCal.setTimeInMillis(new Date().getTime());
		 
		 Calendar cal = Calendar.getInstance();
		 cal.set(Calendar.YEAR, mbCal.get(Calendar.YEAR));
		 cal.set(Calendar.MONTH, mbCal.get(Calendar.MONTH));
		 cal.set(Calendar.DAY_OF_MONTH, mbCal.get(Calendar.DAY_OF_MONTH));
         cal.set(Calendar.HOUR_OF_DAY, mbCal.get(Calendar.HOUR_OF_DAY));
		 cal.set(Calendar.MINUTE, mbCal.get(Calendar.MINUTE));
		 cal.set(Calendar.SECOND, mbCal.get(Calendar.SECOND));
		 cal.set(Calendar.MILLISECOND, mbCal.get(Calendar.MILLISECOND));
	
		 System.out.println("Germany Time :"+cal.getTime());
		 System.out.println("Germany TimeMilli :"+cal.getTimeInMillis());
	}*/
	
}
