/*
 *  Srno    Date       Author          Change Description 
 *  A1      06/10/08   Glenn Raposo    SNX Update 1.6.2 Object changed from Boolean to String
 *  A2      10/06/11   Glenn Raposo    SNX Update 2.1 Commented out Bkg.setVesvoy    
 */
package com.matson.tos.messageHandler;

import java.util.List;

import org.apache.log4j.Logger;

import com.matson.tos.exception.TosException;
import com.matson.tos.jatb.Abob;
import com.matson.tos.jaxb.snx.*;
import com.matson.tos.jaxb.snx.THpu.Entities;
import com.matson.tos.jaxb.snx.THpu.Entities.ExportBookings;
import com.matson.tos.jaxb.snx.THpu.Flags.Flag;

public class AbobHpuMessageHandler extends AbstractMessageHandler {
	private static Logger logger = Logger.getLogger(AbobHpuMessageHandler.class);
		
	public AbobHpuMessageHandler(String xmlObjPackageName,
			String textObjPackageName, String fmtFile, int convertDir)
			throws TosException {
		super(xmlObjPackageName, textObjPackageName, fmtFile, convertDir);
		
	}

	@Override
	protected Object textObjToXmlObj(Object textObj) throws TosException {
		Abob bobObj = (Abob)textObj;
		Snx snxObj = new Snx();
		
		snxObj.setUserId( "ACETS_BOB");
		// booking Check
		//logger.debug("ITN="+bobObj.getItn());
		String itn =  bobObj.getItn();
		if(itn != null) itn = bobObj.getItn().trim().toUpperCase();
		String bookingNum = bobObj.getBookingNbr();
		if(bookingNum != null) bookingNum = bookingNum.trim();
		
		List<THpu> hpuList = snxObj.getHpu();
		THpu.Flags flags = null;
		THpu hpu = null;
		List<Flag> flagList = null;
		Flag aFlag = null;
		
		if ( itn.endsWith( "YES")) {
			hpu = new THpu();
			hpuList.add( hpu);
			flags = new THpu.Flags();
			hpu.setFlags( flags);
			flagList = flags.getFlag();
			aFlag = new Flag();
			flagList.add( aFlag);
			aFlag.setAction( "RELEASE_HOLD");
			aFlag.setHoldPermId( "ITN BKG");
			
		} else if ( itn.endsWith( "NO")) {
			hpu = new THpu();
			hpuList.add( hpu);
			flags = new THpu.Flags();
			hpu.setFlags( flags);
			flagList = flags.getFlag();
			aFlag = new Flag();
			flagList.add( aFlag);
			aFlag.setAction( "ADD_HOLD");
			aFlag.setHoldPermId( "ITN BKG");
		} 
		
		if ( hpu != null) {
			Entities entList = new Entities();
			hpu.setEntities( entList);
			ExportBookings bookings = new ExportBookings();
			entList.setExportBookings( bookings);
			List<TBkg> bookingList = bookings.getBkg();
			TBkg aBkg = new TBkg();
			bookingList.add( aBkg);
			aBkg.setId( bookingNum);
			if (bobObj.getPrimaryCarrier() != null &&  bobObj.getPrimaryCarrier().trim().length() == 0)
				aBkg.setLine( "MAT");
			else
				aBkg.setLine( bobObj.getPrimaryCarrier().trim());
			
			String dirSeq = bobObj.getDirSeq();
			if(dirSeq != null) dirSeq = dirSeq.trim().toUpperCase();
			else dirSeq = "W";
			String vesvoy;
			if ( dirSeq.equals("N") || dirSeq.equals("S") ||
					dirSeq.equals("E") || dirSeq.equals("W"))
				vesvoy = bobObj.getVessel().trim() + bobObj.getVoyageNbr().trim();
			else 
				vesvoy = bobObj.getVessel().trim() + bobObj.getVoyageNbr().trim() +	dirSeq;
			    //A2 -- aBkg.setVesslVisit( vesvoy);
		} else {
			logger.debug( "ABOB: no HPU found.");
			return null;
		}
		return snxObj;
	}

	@Override
	protected Object xmlObjToTextObj(Object xmlObj) throws TosException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Special implementation that will handle no xmlObj created withouyt an exception.
	 */
	@Override
	public String getXmlStr() throws TosException {
		if ( _direction == AbstractMessageHandler.TEXT_TO_XML) {
			if ( _xmlObj == null) // convert only when no xml obj is available
				_xmlObj = textObjToXmlObj( _textObj);
			if(_xmlObj != null) _xmlStr = createXmlStr();
			else _xmlStr = null;
			_xmlObj = null; // reset xml obj to null
		}
		return _xmlStr;
	}
}
