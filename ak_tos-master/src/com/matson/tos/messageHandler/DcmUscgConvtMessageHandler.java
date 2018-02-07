package com.matson.tos.messageHandler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;

import org.apache.log4j.Logger;

import com.matson.tos.exception.TosException;
import com.matson.tos.jatb.DcmConvert;
import com.matson.tos.util.DcmFormatter;
import com.matson.tos.util.UnitConversion;
import com.matson.tos.util.StrUtil;

public class DcmUscgConvtMessageHandler extends AbstractMessageHandler {
	
	private static Logger logger = Logger.getLogger(DcmUscgConvtMessageHandler.class);

	
	public DcmUscgConvtMessageHandler(String textObjPackageName, String fmtFile, int convertDir)
			throws TosException {
		super(textObjPackageName, fmtFile, convertDir);
		// TODO Auto-generated constructor stub
	}

	protected String textObjToTextObj(Object textObj) throws TosException 
	{
		StringBuffer strBuff = new StringBuffer(DcmFormatter.displayLineDataMapping);
		
		DcmConvert txtConvt = (DcmConvert)textObj;
		String hazDesc = DcmFormatter.formatHazDesc(txtConvt.getHazDesc1());
		strBuff.insert(0,txtConvt.getPack());
		strBuff.insert(10,txtConvt.getCellLocation());
		String lbsWeight = DcmFormatter.formatWeightToLB(txtConvt.getGrossWeightUnit());
		strBuff.insert(18,lbsWeight);
		strBuff.insert(29,txtConvt.getCtrNo());
		strBuff.insert(42,txtConvt.getDestPort());
		strBuff.insert(48,txtConvt.getShipment());
		strBuff.insert(59,txtConvt.getConsignee());
		String aline = StrUtil.removeTrailingSpaces(strBuff.toString());
		
		return hazDesc+aline;
	}
	
	public String getTextStr() throws TosException {
		if ( _direction == AbstractMessageHandler.TEXT_TO_TEXT) {
			_textStr = textObjToTextObj(_textObj);
		}
		return _textStr;
	}
	public void setTextStr( String text) throws TosException {
		_textStr = DcmFormatter.modifyData(text);
		createTextObj();
		_xmlObj = null;
	}
	
	@Override
	protected Object xmlObjToTextObj(Object xmlObj) throws TosException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	protected Object textObjToXmlObj(Object textObj) throws TosException{
		return null;
	}

}
