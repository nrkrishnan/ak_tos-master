/*
*********************************************************************************************
* Srno   Date			AuthorName			Change Description
* A1     05/28/09       Glenn Raposo		Added Holds Notes to lookup in N4.  
*********************************************************************************************
*/
package com.matson.tos.messageHandler;

import java.util.List;

import org.apache.log4j.Logger;

import com.matson.cas.refdata.RefDataException;
import com.matson.cas.refdata.RefDataLookup;
import com.matson.cas.refdata.mapping.TosHoldPermData;
import com.matson.cas.refdata.mapping.TosRefData;
import com.matson.tos.exception.TosException;
import com.matson.tos.jatb.Ahlx;
import com.matson.tos.jaxb.snx.*;

public class AhlxMessageHandler extends AbstractMessageHandler {
	private static Logger logger = Logger.getLogger(AhlxMessageHandler.class);

	public AhlxMessageHandler(String xmlObjPackageName,
			String textObjPackageName, String fmtFile, int convertDir)
			throws TosException {
		super(xmlObjPackageName, textObjPackageName, fmtFile, convertDir);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Object textObjToXmlObj(Object textObj) throws TosException {
		Ahlx ahlxObj = (Ahlx)textObj;
		Snx snxObj = new Snx();
		List<THpu> hpu = snxObj.getHpu();
		THpu aHpu = new THpu();
		hpu.add( aHpu);
		THpu.Entities ent = new THpu.Entities();
		aHpu.setEntities( ent);
		TosHoldPermData refData = getHoldPermObj( ahlxObj.getHoldCode().trim());
		if ( refData != null)
			logger.debug( "The applyTo is: " + refData.getApplyTo());
		else
			logger.error( "Can not get ref data for hold: " + ahlxObj.getHoldCode().trim());
		
		if ( refData != null && refData.getApplyTo().equalsIgnoreCase( "equipment")) {
			if ( ahlxObj.getEquipClass().equalsIgnoreCase( "C")) {
				// set container ID
				THpu.Entities.Containers cntnr = new THpu.Entities.Containers();
				ent.setContainers( cntnr);
				List<TCtr> ctrList = cntnr.getCtr();
				TCtr aCtr = new TCtr();
				ctrList.add( aCtr);
				aCtr.setId( ahlxObj.getEqNbr().trim() + ahlxObj.getCheckDigit().trim());
			} else if ( ahlxObj.getEquipClass().equalsIgnoreCase( "H")) {
				THpu.Entities.Chassis chs = new THpu.Entities.Chassis();
				ent.setChassis( chs);
				List<TChs> chsList = chs.getChs();
				TChs aChs = new TChs();
				chsList.add( aChs);
				aChs.setId( ahlxObj.getEqNbr().trim() + ahlxObj.getCheckDigit().trim());
			} else {
				logger.error( "Wrong equipment Class from ACETS: " + ahlxObj.getEquipClass());
				return null;
			}
		} else {
			// set unit
			THpu.Entities.Units unit = new THpu.Entities.Units();
			ent.setUnits( unit);
			List<TUnitIdentity> unitList = unit.getUnit();
			TUnitIdentity unitIden = new TUnitIdentity();
			unitList.add( unitIden);
			unitIden.setId( ahlxObj.getEqNbr().trim() + ahlxObj.getCheckDigit().trim());
		}
		// set hold/release flag
		THpu.Flags flagObj = new THpu.Flags();
		aHpu.setFlags( flagObj);
		List<THpu.Flags.Flag> flags = flagObj.getFlag();
		THpu.Flags.Flag aFlag = new THpu.Flags.Flag();
		flags.add( aFlag);
		if ( refData != null)
			aFlag.setHoldPermId( refData.getSn4Id());
		aFlag.setReferenceId( ahlxObj.getUserId().trim());
		aFlag.setNote("Acets HLP/HLR"); //A1
		if ( ahlxObj.getTxnCode().equalsIgnoreCase( "AHLR"))
			aFlag.setAction( "RELEASE_HOLD");
		else
			aFlag.setAction( "ADD_HOLD");
		return snxObj;
	}

	@Override
	protected Object xmlObjToTextObj(Object xmlObj) throws TosException {
		// TODO Auto-generated method stub
		return null;
	}

	private TosHoldPermData getHoldPermObj( String acetsCode) {
		TosHoldPermData obj = null;
		try {
			obj = (TosHoldPermData)RefDataLookup.queryById("com.matson.cas.refdata.mapping.TosHoldPermData", acetsCode);
		} catch ( Exception ex) {
			logger.error( "Error in get obj for ACETS code:" + acetsCode, ex);
		}
		return obj;
	}
}
