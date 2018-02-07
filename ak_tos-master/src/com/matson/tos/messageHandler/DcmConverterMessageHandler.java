package com.matson.tos.messageHandler;

import org.apache.log4j.Logger;

import com.matson.tos.exception.TosException;
import com.matson.tos.jatb.DcmConvert;
import com.matson.tos.util.DcmFormatter;
import com.matson.tos.util.UnitConversion;

public class DcmConverterMessageHandler extends AbstractMessageHandler {
	private static Logger logger = Logger.getLogger(DcmConverterMessageHandler.class);

	
	public DcmConverterMessageHandler(String textObjPackageName, String fmtFile, int convertDir)
			throws TosException {
		super(textObjPackageName, fmtFile, convertDir);
		// TODO Auto-generated constructor stub
	}

	protected String textObjToTextObj(Object textObj) throws TosException 
	{
		StringBuffer strBuff = new StringBuffer();
		String temp = null;

		DcmConvert txtConvt = (DcmConvert)textObj;
		temp = DcmFormatter.addQuotes(txtConvt.getVesvoy());
		strBuff.append(temp);
		temp = DcmFormatter.addQuotes(txtConvt.getCtrNo());
		strBuff.append(temp);
		temp = DcmFormatter.addQuotes(txtConvt.getCnSeq());
		strBuff.append(temp);
		temp = DcmFormatter.addQuotes(txtConvt.getBlankData1());
		strBuff.append(temp);
		temp = DcmFormatter.addQuotes(txtConvt.getCheckDigit());
		strBuff.append(temp);
		temp = DcmFormatter.addQuotes(txtConvt.getHazClass());
		strBuff.append(temp);
		temp = DcmFormatter.addQuotes(txtConvt.getHazClassDesc());
		strBuff.append(temp);
		temp = DcmFormatter.addQuotes(txtConvt.getHazSubClass());
		strBuff.append(temp);
		temp = DcmFormatter.addQuotes(txtConvt.getConsignee());
		strBuff.append(temp);
		temp = DcmFormatter.addQuotes(txtConvt.getPack());
		strBuff.append(temp);
		temp = DcmFormatter.addQuotes(txtConvt.getBlankData2());
		strBuff.append(temp);
		temp = DcmFormatter.addQuotes(txtConvt.getHazDesc1());
		strBuff.append(temp);
		temp = DcmFormatter.addQuotes(txtConvt.getEmergencyContact());
		strBuff.append(temp);
		String grossWeightUnit =DcmFormatter.formatWeightToLB(txtConvt.getGrossWeightUnit());
		temp = DcmFormatter.addQuotes(grossWeightUnit);
		strBuff.append(temp);
		temp = DcmFormatter.addQuotes(txtConvt.getCellLocation());
		strBuff.append(temp);
		temp = DcmFormatter.addQuotes(txtConvt.getDestPort());
		strBuff.append(temp);
		temp = DcmFormatter.addQuotes(txtConvt.getYardLocation());
		strBuff.append(temp);
		temp = DcmFormatter.addQuotes(txtConvt.getCarsFlag());
		strBuff.append(temp);
		temp = DcmFormatter.addQuotes(txtConvt.getShipment());
		strBuff.append(temp);
		temp = DcmFormatter.addQuotes(txtConvt.getHazCode());
		strBuff.append(temp);
		temp = DcmFormatter.addQuotes(txtConvt.getHazCodeType());
		strBuff.append(temp);
		temp = DcmFormatter.addQuotes(txtConvt.getHazRegQualifier());
		strBuff.append(temp);
		temp = DcmFormatter.addQuotes(txtConvt.getShippingName());
		strBuff.append(temp);
		temp = DcmFormatter.addQuotes(txtConvt.getTechName());
		strBuff.append(temp);
		String grossWeight = DcmFormatter.weightFromKgToLB(txtConvt.getGrossWeight());
		temp = DcmFormatter.addQuotes(grossWeight);
		strBuff.append(temp);
		String grossUnit = DcmFormatter.formatWeightToLB(txtConvt.getGrossUnit());
		temp = DcmFormatter.addQuotes(grossUnit);
		strBuff.append(temp);
		temp = DcmFormatter.addQuotes(txtConvt.getFlashPoint());
		strBuff.append(temp);
		temp = DcmFormatter.addQuotes(txtConvt.getPackagingGrpMarks());
		strBuff.append(temp);
		temp = DcmFormatter.addQuotes(txtConvt.getRemarks());
		strBuff.append(temp);
		temp = DcmFormatter.addQuotes(txtConvt.getEmergencyResponse());
		strBuff.append(temp);
		temp = DcmFormatter.addQuotes(txtConvt.getLoadPort());
		strBuff.append(temp);
		temp = DcmFormatter.addQuotes(txtConvt.getConsigneeName()!= null ? txtConvt.getConsigneeName(): "");
		strBuff.append(temp);
		return strBuff.toString();

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
