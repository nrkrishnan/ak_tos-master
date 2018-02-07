/*
*********************************************************************************************
*Srno   Date			AuthorName			Change Description
* A1    10/07/08        Glenn Raposo		CMIS Fixed length Format Changes  
* *     3/25/2013       Steven Bauer        Removing SingletonService, if it is going to run clustered it needs to be replaced.
*********************************************************************************************
*/
package com.matson.tos.groovy.writer;

import java.util.Map;
import org.apache.log4j.Logger;



import com.matson.tos.groovy.CargoStatus;
import com.matson.tos.groovy.formatter.EmailCargoStatus;
import com.matson.tos.groovy.formatter.EmailInGateTempFormatter;



/**
 * @author JZF
 * 
 */
public class ObjectWriterFactory {
	private static Logger logger = Logger.getLogger(ObjectWriterFactory.class);
	
	public static final String MSG_TYPE = "msgType";
	public static final String UNIT_CLASS = "unitClass";
	public static final String EMAIL_GATE_TEMP = "InGateReqTemp";
	public static final String IN_GATE_MSG = "IGT";
	
	public static synchronized ObjectWriter getWriter( Map data) {
		String msgType = (String)data.get(MSG_TYPE);
		String unitClass = (String)data.get(UNIT_CLASS);
		
		if ( msgType.equalsIgnoreCase( EMAIL_GATE_TEMP)) 
			return new EmailWriter( new EmailInGateTempFormatter( data));
//		else if (msgType.equalsIgnoreCase( IN_GATE_MSG))
//			return new ParadoxWriter( data);
		else if (IN_GATE_MSG.equalsIgnoreCase(msgType)) {
			FTPWriter writer = FTPWriter.getInstance();
			writer.setData( data);
			return writer;
		} else if ( msgType.equalsIgnoreCase( "TEST_FORMULA") || 
				msgType.equalsIgnoreCase( "CARGO_STATUS")) {
			logger.debug( "In Cargo Status.");
			CargoStatus cargoSt = new CargoStatus( data);
			boolean sendEmail = cargoSt.sendEmail();
			logger.debug( "sendEmail = " + sendEmail);
			if ( sendEmail) {
				return new EmailWriter( new EmailCargoStatus( cargoSt.getEmailData()));
			}
			logger.debug( "End of Cargo Status.");
			
			return null;
		 	
		} else if (unitClass.equals("CONTAINER") || unitClass.equals("CHASSIS") || 
				unitClass.equals("ACCESSORY") || unitClass.equals("VESSELVISIT") || unitClass.equals("TRUCKVISIT") ){ 
		   FTPWriterCmis writerCmis = FTPWriterCmis.getInstance();
		   writerCmis.setData( data);
		   logger.debug("After calling the FTPWriterCmis Class");
		   return writerCmis;
		}
		
		return null;
	}
}
