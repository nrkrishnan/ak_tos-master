package com.matson.tos.messageHandler;

import com.matson.tos.jatb.DcmConvert;
import com.matson.tos.util.DcmFormatter;

public class DcmCSVMessageHandler {

	private StringBuffer buf = new StringBuffer();
	
	public DcmCSVMessageHandler() {
		append("Dport,Cnseq,Ctrno,HAZ_CLASS,SHIPPER/CNEE,#PACK,HAZ_DESC1,PHONE,GROSSWGT,CELL,CARF,Hazard Code,Hazard Code Type,Vesvoy\n");
		//append("Dport,Cnseq,Ctrno,HAZ_CLASS,SHIPPER/CNEE,#PACK,HAZ_DESC1,PHONE,GROSSWGT,CELL,Hazard Code,Hazard Code Type,Vesvoy\n");

	}
	
	public String toString() {
		return buf.toString();
	}
	
	public void append(String s ) {
		s = DcmFormatter.replaceQuotes(s);
		buf.append(s);
	}
	
	public void addDcmLine(DcmConvert convert, String vessel, String voyage) {	
		append("\"");
		append(convert.getDestPort());
		append("\",\"");
		append(convert.getCnSeq());
		append("\",\"");
		append(convert.getCtrNo());
		append("\",\"");
		append(convert.getHazClass());
		append("\",\"");
		append(convert.getConsigneeName());
		append("\",\"");
		append(convert.getPack());
		append("\",\"");
		append(convert.getHazDesc1());
		append("\",\"");
		if(convert.getEmergencyContact() != null && convert.getEmergencyContact().length() > 0) {
			append(convert.getEmergencyContact());
		}
		append("\",\"");
		append(convert.getGrossWeight());
		append("\",\"");
		append(convert.getCellLocation());
		append("\",\"");
		append(convert.getCarsFlag());  
		append("\",\"");
		append(convert.getHazCode());
		append("\",\"");
		append(convert.getHazCodeType());
		append("\",\"");
		append(vessel);
		append(voyage);
		append("\"\n");
	}
}
