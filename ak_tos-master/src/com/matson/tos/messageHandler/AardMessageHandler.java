package com.matson.tos.messageHandler;

import java.util.HashMap;
import java.util.Map;

import com.matson.tos.exception.TosException;
import com.matson.tos.jatb.Aard;
import com.matson.tos.util.GroovyXmlUtil;

/**
 * 
 * @author xw8
 * Change History
 * A2   04/13/2009  SKB		Added leg to VesVoy.
 */
public class AardMessageHandler extends AbstractMessageHandler {
	public static final String CLASS_LOC = "database";
	public static final String CLASS_NAME = "GvyInjAard";

	public AardMessageHandler(String xmlObjPackageName,
			String textObjPackageName, String fmtFile, int convertDir)
			throws TosException {
		super(xmlObjPackageName, textObjPackageName, fmtFile, convertDir);
		// TODO Auto-generated constructor stub
	}
	protected String createXmlStr() throws TosException {
		Map<String, String> data = new HashMap<String, String>();
		Aard textObj = (Aard)createTextObj();
		populateData(data, textObj);
		
		return GroovyXmlUtil.getInjectionXmlStr( CLASS_NAME, CLASS_LOC, data);
	}
	
	private void populateData( Map<String, String> data, Aard ardObj) {
		data.put("equipment-id", ardObj.getArdFeqtNumber().trim() + ardObj.getArdFeqtCheckDigit().trim());
		// Added Voyage for Barge.
		String dirSeq = ardObj.getArdEqlgRecDirection();
		if(dirSeq != null) dirSeq = dirSeq.trim().toUpperCase();
		else dirSeq = "W";
		String vesvoy;
		if ( dirSeq.equals("N") || dirSeq.equals("S") ||
				dirSeq.equals("E") || dirSeq.equals("W"))
			vesvoy = ardObj.getArdEqlgRecVessel().trim() + ardObj.getArdEqlgRecVoyage().trim();
		else 
			vesvoy = ardObj.getArdEqlgRecVessel().trim() + ardObj.getArdEqlgRecVoyage().trim() +	dirSeq;
		
		data.put( "vesvoy", vesvoy);
		data.put( "autoRecDate", ardObj.getArdAmisAutoReceiptdate().trim());
	}

	@Override
	protected Object textObjToXmlObj(Object textObj) throws TosException {
		// TODO Auto-generated method stub
		
		return null;
	}

	@Override
	protected Object xmlObjToTextObj(Object xmlObj) throws TosException {
		// TODO Auto-generated method stub
		return null;
	}

}
