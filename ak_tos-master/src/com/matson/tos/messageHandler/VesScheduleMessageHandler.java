/*
*********************************************************************************************
* Srno   Date			AuthorName			Change Description
* A1     03/16/09       Glenn Raposo		Mapped CLI Line Operator (Need to Create a Mapping table) 
* A2     02/01/10       Glenn Raposo		Added ItineraryId for YB 
*********************************************************************************************
*/
package com.matson.tos.messageHandler;

import java.math.BigInteger;
import java.util.Map;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.matson.tos.exception.TosException;
import com.matson.tos.jatb.VesSchedule;
import com.matson.tos.jaxb.snx.TVesselVisit;
import com.matson.tos.util.CalendarUtil;
import com.matson.tos.util.StrUtil;
import com.matson.tos.util.TosRefDataUtil;


public class VesScheduleMessageHandler extends AbstractMessageHandler {
	private static Logger logger = Logger.getLogger(VesScheduleMessageHandler.class);
	//private static String pat = "MM/dd/yyy HH:mm:ss";
	private static String pat1 = "MM/dd/yyyy";
	
	public VesScheduleMessageHandler(String xmlObjPackageName,
			String textObjPackageName, String fmtFile, int convertDir)
			throws TosException {
		super(xmlObjPackageName, textObjPackageName, fmtFile, convertDir);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Object textObjToXmlObj(Object textObj) throws TosException {
		Object retObj = null;
		String temp;
		VesSchedule vesObj = (VesSchedule)textObj;
		String recType = StrUtil.trimQuotes(vesObj.getRecType());
		if ( recType.equalsIgnoreCase("vesselVisit")) {
			TVesselVisit aRec = new TVesselVisit();
			temp = StrUtil.trimQuotes( vesObj.getATA()).trim();
			if ( temp.length() > 10)
				aRec.setATA( CalendarUtil.getXmlCalendar( temp, pat));
			else if ( temp.length() > 4)
				aRec.setATA( CalendarUtil.getXmlCalendar( temp, pat1));
			
			temp = StrUtil.trimQuotes( vesObj.getATD()).trim();
			if ( temp.length() > 10)
				aRec.setATD( CalendarUtil.getXmlCalendar( temp, pat));
			else if ( temp.length() > 4)
				aRec.setATD( CalendarUtil.getXmlCalendar( temp, pat1));
			temp = StrUtil.trimQuotes( vesObj.getETA()).trim();
			if ( temp.length() > 10)
				aRec.setETA( CalendarUtil.getXmlCalendar( temp, pat));
			else if ( temp.length() > 4)
				aRec.setETA( CalendarUtil.getXmlCalendar( temp, pat1));
			temp = StrUtil.trimQuotes( vesObj.getETD()).trim();
			if ( temp.length() > 10)
				aRec.setETD( CalendarUtil.getXmlCalendar( temp, pat));
			else if ( temp.length() > 4)
				aRec.setETD( CalendarUtil.getXmlCalendar( temp, pat1));

			aRec.setInVoyNbr( StrUtil.trimQuotes( vesObj.getInVoyNbr()));
			aRec.setId( StrUtil.trimQuotes( vesObj.getId()));
			aRec.setInCallNumber( new BigInteger( StrUtil.trimQuotes( vesObj.getInCallNumber())));
			if ( StrUtil.trimQuotes( vesObj.getInboundFirstFreeDay()).trim().length() != 0) {
				aRec.setInboundFirstFreeDay( CalendarUtil.getXmlCalendar( StrUtil.trimQuotes( vesObj.getInboundFirstFreeDay()), pat).toString());
			}
			if ( new Boolean( StrUtil.trimQuotes( vesObj.getIsCommonCarrier())).booleanValue())
				aRec.setIsCommonCarrier( "Y");
			else
				aRec.setIsCommonCarrier( "N");
			if ( new Boolean( StrUtil.trimQuotes( vesObj.getIsDrayOff())).booleanValue())
				aRec.setIsDrayOff( "Y");
			else
				aRec.setIsDrayOff( "N");
			if ( new Boolean( StrUtil.trimQuotes( vesObj.getIsNoClientAccess())).booleanValue())
				aRec.setIsNoClientAccess( "Y");
			else
				aRec.setIsNoClientAccess( "N");
			aRec.setNotes( StrUtil.trimQuotes( vesObj.getNotes()));
			aRec.setOutVoyNbr( StrUtil.trimQuotes( vesObj.getOutVoyNbr()));
			aRec.setOperatorId( getLineOperator(StrUtil.trimQuotes(vesObj.getOperatorId())));
			aRec.setOutCallNumber(new BigInteger( new Integer( StrUtil.trimQuotes( vesObj.getOutCallNumber())).toString()));
			temp = StrUtil.trimQuotes( vesObj.getPublishedEta()).trim();
			if ( temp.length() > 10)
				aRec.setPublishedEta( CalendarUtil.getXmlCalendar( temp, pat));
			else if ( temp.length() > 4)
				aRec.setPublishedEta( CalendarUtil.getXmlCalendar( temp, pat1));

			temp = StrUtil.trimQuotes( vesObj.getPublishedEtd()).trim();
			if ( temp.length() > 10)
				aRec.setPublishedEtd( CalendarUtil.getXmlCalendar( temp, pat));
			else if ( temp.length() > 4)
				aRec.setPublishedEtd( CalendarUtil.getXmlCalendar( temp, pat1));
			
			
			
			aRec.setServiceId( StrUtil.trimQuotes( vesObj.getServiceId()));
			aRec.setItineraryId( StrUtil.trimQuotes( vesObj.getItineraryId()));
			temp = StrUtil.trimQuotes( vesObj.getTimeBeginReceive()).trim();
			if ( temp.length() > 10)
				aRec.setTimeBeginReceive( CalendarUtil.getXmlCalendar( temp, pat));
			else if ( temp.length() > 4)
				aRec.setTimeBeginReceive( CalendarUtil.getXmlCalendar( temp, pat1));
			temp = StrUtil.trimQuotes( vesObj.getTimeCargoCutoff()).trim();
			if ( temp.length() > 10)
				aRec.setTimeCargoCutoff( CalendarUtil.getXmlCalendar( temp, pat));
			else if ( temp.length() > 4)
				aRec.setTimeCargoCutoff( CalendarUtil.getXmlCalendar( temp, pat1));
			temp = StrUtil.trimQuotes( vesObj.getTimeDischargeComplete()).trim();
			if ( temp.length() > 10)
				aRec.setTimeDischargeComplete( CalendarUtil.getXmlCalendar( temp, pat));
			else if ( temp.length() > 4)
				aRec.setTimeDischargeComplete( CalendarUtil.getXmlCalendar( temp, pat1));
			temp = StrUtil.trimQuotes( vesObj.getTimeEndWork()).trim();
			if ( temp.length() > 10)
				aRec.setTimeEndWork( CalendarUtil.getXmlCalendar( temp, pat));
			else if ( temp.length() > 4)
				aRec.setTimeEndWork( CalendarUtil.getXmlCalendar( temp, pat1));
			else if( aRec.getATD() != null) {
				aRec.setTimeEndWork(aRec.getATD());
			}
			temp = StrUtil.trimQuotes( vesObj.getTimeFirstAvailability()).trim();
			if ( temp.length() > 10)
				aRec.setTimeFirstAvailability( CalendarUtil.getXmlCalendar( temp, pat));
			else if ( temp.length() > 4)
				aRec.setTimeFirstAvailability( CalendarUtil.getXmlCalendar( temp, pat1));
			temp = StrUtil.trimQuotes( vesObj.getTimeHazCutoff()).trim();
			if ( temp.length() > 10)
				aRec.setTimeHazCutoff( CalendarUtil.getXmlCalendar( temp, pat));
			else if ( temp.length() > 4)
				aRec.setTimeHazCutoff( CalendarUtil.getXmlCalendar( temp, pat1));
			
			temp = StrUtil.trimQuotes( vesObj.getTimeLaborOffBoard()).trim();
			if ( temp.length() > 10)
				aRec.setTimeLaborOffBoard( CalendarUtil.getXmlCalendar( temp, pat));
			else if ( temp.length() > 4)
				aRec.setTimeLaborOffBoard( CalendarUtil.getXmlCalendar( temp, pat1));
			temp = StrUtil.trimQuotes( vesObj.getTimeLaborOnBoard()).trim();
			if ( temp.length() > 10)
				aRec.setTimeLaborOnBoard( CalendarUtil.getXmlCalendar( temp, pat));
			else if ( temp.length() > 4)
				aRec.setTimeLaborOnBoard( CalendarUtil.getXmlCalendar( temp, pat1));
			temp = StrUtil.trimQuotes( vesObj.getTimePilotOffBoard()).trim();
			if ( temp.length() > 10)
				aRec.setTimePilotOffBoard( CalendarUtil.getXmlCalendar( temp, pat));
			else if ( temp.length() > 4)
				aRec.setTimePilotOffBoard( CalendarUtil.getXmlCalendar( temp, pat1));
			temp = StrUtil.trimQuotes( vesObj.getTimePilotOnBoard()).trim();
			if ( temp.length() > 10)
				aRec.setTimePilotOnBoard( CalendarUtil.getXmlCalendar( temp, pat));
			else if ( temp.length() > 4)
				aRec.setTimePilotOnBoard( CalendarUtil.getXmlCalendar( temp, pat1));
			temp = StrUtil.trimQuotes( vesObj.getTimeReeferCutoff()).trim();
			if ( temp.length() > 10)
				aRec.setTimeReeferCutoff( CalendarUtil.getXmlCalendar( temp, pat));
			else if ( temp.length() > 4)
				aRec.setTimeReeferCutoff( CalendarUtil.getXmlCalendar( temp, pat1));
			temp = StrUtil.trimQuotes( vesObj.getTimeStartWork()).trim();
			if ( temp.length() > 10)
				aRec.setTimeStartWork( CalendarUtil.getXmlCalendar( temp, pat));
			else if ( temp.length() > 4)
				aRec.setTimeStartWork( CalendarUtil.getXmlCalendar( temp, pat1));
			else if( aRec.getATA() != null) {
				aRec.setTimeStartWork(aRec.getATA());
			}
				

			aRec.setVesselId( StrUtil.trimQuotes( vesObj.getVesselId()));
			// Offset the Service ID for YB.
			if(aRec.getVesselId() != null && aRec.getVesselId().toUpperCase().startsWith("YB") ) {
				aRec.setServiceId("YB_"+aRec.getServiceId());
				aRec.setItineraryId("YB-"+aRec.getItineraryId()); //A2
			}
			
			if ( vesObj.getVisitPhase() != null && StrUtil.trimQuotes( vesObj.getVisitPhase()).trim().length() != 0)
			    aRec.setVisitPhase(   StrUtil.trimQuotes( vesObj.getVisitPhase()));
			if ( StrUtil.trimQuotes( vesObj.getNextFacility()).trim().length() != 0)
				aRec.setNextFacility( StrUtil.trimQuotes( vesObj.getNextFacility()));
			
			retObj = aRec;
		} else if ( recType.equalsIgnoreCase("line")) {
			TVesselVisit.Lines.Line aLine = new TVesselVisit.Lines.Line();
			aLine.setId( StrUtil.trimQuotes( vesObj.getLineId()));
			aLine.setInVoyNbr( StrUtil.trimQuotes( vesObj.getLineInVoyNbr()));
			aLine.setOutVoyNbr( StrUtil.trimQuotes( vesObj.getLineOutVoyNbr()));
			temp = StrUtil.trimQuotes( vesObj.getLineTimeCargoCutoff()).trim();
			if ( temp.length() > 10)
				aLine.setTimeCargoCutoff( CalendarUtil.getXmlCalendar( temp, pat));
			else if ( temp.length() > 4)
				aLine.setTimeCargoCutoff( CalendarUtil.getXmlCalendar( temp, pat1));
			temp = StrUtil.trimQuotes( vesObj.getLineTimeEmptyPickup()).trim();
			if ( temp.length() > 10)
				aLine.setTimeEmptyPickup( CalendarUtil.getXmlCalendar( temp, pat));
			else if ( temp.length() > 4)
				aLine.setTimeEmptyPickup( CalendarUtil.getXmlCalendar( temp, pat1));
			
			retObj = aLine;
		} else if ( recType.equalsIgnoreCase("berthing")) {
			TVesselVisit.Berths.Berthing aBerth = new TVesselVisit.Berths.Berthing();
			aBerth.setQuayId( StrUtil.trimQuotes( vesObj.getQuayId()));
			aBerth.setShipSideTo( StrUtil.trimQuotes( vesObj.getShipSideTo()));
			temp = StrUtil.trimQuotes( vesObj.getTimeShift()).trim();
			if ( temp.length() > 10)
				aBerth.setTimeShift( CalendarUtil.getXmlCalendar( temp, pat));
			else if ( temp.length() > 4)
				aBerth.setTimeShift( CalendarUtil.getXmlCalendar( temp, pat1));

			retObj = aBerth;
		} else if ( recType.equalsIgnoreCase("estMoveCount")) {
			TVesselVisit.EstMoveCount aMoveCount = new TVesselVisit.EstMoveCount();
			aMoveCount.setBbkDischarge( new BigInteger( StrUtil.trimQuotes( vesObj.getBbkDischarge())));
			aMoveCount.setBbkLoad( new BigInteger( StrUtil.trimQuotes( vesObj.getBbkLoad())));
			aMoveCount.setDischarge( new BigInteger( StrUtil.trimQuotes( vesObj.getDischarge())));
			aMoveCount.setLoad( new BigInteger( StrUtil.trimQuotes( vesObj.getLoad())));
			aMoveCount.setRestow( new BigInteger( StrUtil.trimQuotes( vesObj.getRestow())));
			aMoveCount.setShift( new BigInteger( StrUtil.trimQuotes( vesObj.getShift())));
			
			retObj = aMoveCount;
		} else {
			logger.error( "Wrong message type: " + recType);
		}
		
	
		return retObj;
	}

	@Override
	protected Object xmlObjToTextObj(Object xmlObj) throws TosException {
		// TODO Auto-generated method stub
		return null;
	}

/*	  //Method Maps line Operator for Client Vessel Visit
	  public String getLineOperator(Object operator) 
	  {
		String lineOperator = "";
		try
	    {  
	      Map map = new HashMap();      
	      map.put("CSX","HRZ");
	      map.put("CLL","HAMSUD");
	      map.put("HSD","HAMSUD");
	      map.put("MAE","MAERSK");
	      map.put("POL", "POLY");

	     lineOperator = (String)(map.get(operator) != null ? map.get(operator) : operator);
	      if(lineOperator != null && !lineOperator.equals("MAT")){
	         logger.debug("operator :"+operator+"  MappedOperator ::"+lineOperator);
	      }
	    }catch(Exception e){
	        e.printStackTrace();
	    }
	    return lineOperator; 
	  }  */
	  
	 //A1- Method Maps line Operator for Client Vessel Visit
	  public String getLineOperator(String operator) 
	  {
		String  operatorDesc = null;  
		try
		{ 
		 if(operator != null && operator.equals("MAT")){
			 return operator;
		 }
		 else if(operator != null && operator.length() > 0){
	  	   operatorDesc = TosRefDataUtil.getCliOperatorObj(operator);
	  	   logger.debug("operatorDesc ::"+operatorDesc);
         }
		}catch(Exception e){
		   e.printStackTrace();	
		}
		return operatorDesc;
	  }
}
