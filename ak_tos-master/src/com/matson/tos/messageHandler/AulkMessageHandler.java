package com.matson.tos.messageHandler;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.matson.tos.exception.TosException;
import com.matson.tos.jatb.Aulk;
import com.matson.tos.util.GroovyXmlUtil;

public class AulkMessageHandler extends AbstractMessageHandler {
	private static Logger logger = Logger.getLogger(AulkMessageHandler.class);
	public static final String CLASS_LOC = "database";
	public static final String CLASS_NAME = "GvyInjAulk";

	public AulkMessageHandler(String xmlObjPackageName,
			String textObjPackageName, String fmtFile, int convertDir)
			throws TosException {
		super(xmlObjPackageName, textObjPackageName, fmtFile, convertDir);
		// TODO Auto-generated constructor stub
	}
	protected String createXmlStr() throws TosException {
		Map<String, String> data = new HashMap<String, String>();
		Aulk textObj = (Aulk)createTextObj();
		populateData(data, textObj);
		
		return GroovyXmlUtil.getInjectionXmlStr( CLASS_NAME, CLASS_LOC, data);
	}
	
	private void populateData( Map<String, String> data, Aulk ulkObj) {
		data.put("equipment-id", ulkObj.getUlkEquipmentNum().trim() + ulkObj.getCheckDigit().trim());
		data.put( "recorder", ulkObj.getUlkUserId());
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
