/**
 * 
 */
package com.matson.tos.messageHandler;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import com.matson.tos.exception.TosException;
import com.matson.tos.jatb.Abob;
import com.matson.tos.jatb.Gstuff;
import com.matson.tos.jaxb.snx.Snx;
import com.matson.tos.jaxb.snx.THazard;
import com.matson.tos.jaxb.snx.THazards;
import com.matson.tos.jaxb.snx.TUnit;

/**
 * @author JZF
 *
 */
public class GstuffMessageHandler extends AbstractMessageHandler {

	/**
	 * @param xmlObjPackageName
	 * @param textObjPackageName
	 * @param fmtFile
	 * @param convertDir
	 * @throws TosException
	 */
	public GstuffMessageHandler(String xmlObjPackageName,
			String textObjPackageName, String fmtFile, int convertDir)
			throws TosException {
		super(xmlObjPackageName, textObjPackageName, fmtFile, convertDir);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see com.matson.tos.messageHandler.AbstractMessageHandler#textObjToXmlObj(java.lang.Object)
	 */
	@Override
	protected Object textObjToXmlObj(Object textObj) throws TosException {
		// TODO Auto-generated method stub
		Gstuff stuffObj = (Gstuff)textObj;
		Snx snxObj = new Snx();
		// hazards
		List<TUnit> unitList = snxObj.getUnit();
		TUnit aUnit = new TUnit();
		unitList.add( aUnit);
		
		aUnit.setId( stuffObj.getId());
		aUnit.setTransitState( "YARD");
		
		THazards hazs = new THazards();
		aUnit.setHazards( hazs);
		List<THazard> hazList = hazs.getHazard();
		THazard aHaz = new THazard();
		hazList.add( aHaz);
		// these should get from DB
		aHaz.setImdg( "9");
		//aHaz.setQuantity( new BigInteger( "3"));
		aHaz.setPackageType( "AUTO");
		//aHaz.setUn( new Short( "3166"));
		aHaz.setUn( "3166");
		aHaz.setEmergencyTelephone( "1(800)424-9300");
		aHaz.setProperName( "VEHICLE, FLAMMABLE LIQUID POWERED");
		aHaz.setEmsResponseGuideNbr( "128");
		
		return snxObj;
	}

	/* (non-Javadoc)
	 * @see com.matson.tos.messageHandler.AbstractMessageHandler#xmlObjToTextObj(java.lang.Object)
	 */
	@Override
	protected Object xmlObjToTextObj(Object xmlObj) throws TosException {
		// TODO Auto-generated method stub
		return null;
	}

}
