package com.matson.tos.util;

import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBException;

import com.matson.cas.refdata.mapping.TosErrorMsg;
import com.matson.tos.jaxb.snx.Snx;
import com.matson.tos.jaxb.snx.error.SnxError;
import com.matson.tos.processor.MdbSn4MessageProcessor;

public class TosErrorMsgRecorder {
	private static final String groovyMsg = "Groovy Injection Error Message";
	private static final String snxMsg = "SN4 Error Message";
	private static Logger logger = Logger.getLogger(TosErrorMsgRecorder.class);
	
	/*
	 * Method Inserts messages entry in DB from QUEUE (N4/ACETS)
	 */  
	// todo code it
	public static void insertSnxErrorMessage(String msg) {
		if(msg == null) return;
		TosErrorMsg errorMsg = new TosErrorMsg();
		ByteArrayInputStream stream = new ByteArrayInputStream(msg.getBytes());
		try {
			SnxError error = SnxUnmarshaller.unmarshallError(stream);
			
			
			errorMsg.setErrType(snxMsg);
			errorMsg.setErrDateTime(new Date());
			errorMsg.setErrorStatus("New");
			errorMsg.setMsgText(error.getPayload());
			StringBuffer buf = new StringBuffer();
			if(error.getReason() != null) {
				List<Object> l = error.getReason().getContent();
				if(l != null) {
					Iterator iter = l.iterator();
					while(iter.hasNext()) {
						buf.append(iter.next());
					}
				}
				errorMsg.setErrorDesc(buf.toString());
			}
			
			/*
			stream = new ByteArrayInputStream(error.getPayload().getBytes());
			Snx snx = SnxUnmarshaller.unmarshall(stream);
			if(snx.getUnit() != null && snx.getUnit().size() > 0) {
				errorMsg.setContainerNumber(snx.getUnit().get(0).getId());
			}
		
			if(snx.getVesselVisit() != null && snx.getVesselVisit().size() > 0) {
				errorMsg.setVesvoy(snx.getVesselVisit().get(0).getId());
			}
			*/
			errorMsg.setContainerNumber(SnxUnmarshaller.getContainerNumber(error.getPayload()));
			errorMsg.setVesvoy(SnxUnmarshaller.getVesvoy(error.getPayload()));
			errorMsg.setBkgNumber(SnxUnmarshaller.getBookingNumber(error.getPayload()));
			
		} catch (Exception e) {
		}
		
		errorMsg.insertMessage();
	}

	public static void insertGroovyErrorMessage(String msg) {
		if(msg == null) return;
		TosErrorMsg errorMsg = new TosErrorMsg();
		ByteArrayInputStream stream = new ByteArrayInputStream(msg.getBytes());
		try {
			SnxError error = SnxUnmarshaller.unmarshallError(stream);
			
			
			errorMsg.setErrType(groovyMsg);
			errorMsg.setErrDateTime(new Date());
			errorMsg.setErrorStatus("New");
			errorMsg.setMsgText(error.getPayload());
			StringBuffer buf = new StringBuffer();
			if(error.getReason() != null) {
				List<Object> l = error.getReason().getContent();
				if(l != null) {
					Iterator iter = l.iterator();
					while(iter.hasNext()) {
						buf.append(iter.next());
					}
				}
				errorMsg.setErrorDesc(buf.toString());
			}
			
			stream = null;
			
			Map map = GroovyXmlUtil.getResponseMap(error.getPayload());
			errorMsg.setContainerNumber((String)map.get("equipment-id"));
			errorMsg.setVesvoy((String)map.get("vesvoy"));
			errorMsg.setBkgNumber((String)map.get("bookingNum"));
		} catch (Exception e) {
		}
		

		errorMsg.insertMessage();
	}

}
