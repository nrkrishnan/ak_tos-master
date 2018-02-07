package com.matson.tos.util;

import org.apache.log4j.Logger;

import com.matson.tos.dao.TosLookup;
import com.matson.tos.exception.TosException;
import com.matson.tos.processor.NewVesFileProcessor;

/**
 * Change:
 * A1  SKB	05/01/09	Added length check for X digits.
 * A2  SKB  06/11/09	Fixed bug in renumber code.
 * 						Was  renumbering if id shorter than new id
 * A3  SKB  	11/10/09	  Renumber only 1 digit away.
 * @author xw8
 *
 */
public class CheckDigit {
	private String MAIL_HOST = TosRefDataUtil.getValue( "MAIL_HOST");
	private String supportMail = TosRefDataUtil.getValue("SUPPORT_EMAIL");
	private static Logger logger = Logger.getLogger(CheckDigit.class);
	
	public String renumString = "";
	
	public String getCheckDigitUnit(String id) {
		renumString = "";
		TosLookup dao = null;
		try {
			dao = new com.matson.tos.dao.TosLookup();
			
		} catch (Exception e) {
			logger.error("SQL Error for check digit lookup "+id,e);
			EmailSender.sendMail(supportMail, supportMail, "SQL Error for check digit lookup "+id, e.getMessage());
		}
		
		try {
			String newId = dao.getCheckDigit(id);
			//logger.debug("New = "+newId+" old="+id);
			if(!newId.equals(id)) {
				logger.debug("Tos Warning for check digit lookup "+id+" system has "+newId);
			//	EmailSender.sendMail(supportMail, supportMail, "Tos Warning for check digit lookup "+id+" system has "+newId, "Tos Warning for check digit lookup "+id+" system has "+newId);
				if(newId.length() > id.length()+1) {
					logger.warn("Invalid new id for "+id+ " id found = "+newId);
					newId = id;
				}
				else if( id.toUpperCase().endsWith("X") &&  id.length() == newId.length()) {
				    logger.debug("Skipping renum for "+id+ " id found = "+newId);
				} else if(id.length() < newId.length()) {
					logger.debug("Skipping renum for "+id+ " id found = "+newId);
				} else {
					renumString += " Renum["+newId+"|"+id+"]";
				}
			}
			return newId;
		} catch (TosException e) {
			logger.error("Tos Error for check digit lookup "+id,e);
			EmailSender.sendMail(supportMail, supportMail, "Tos Error for check digit lookup "+id, e.getMessage());
		} finally {
			if(dao != null)  dao.close();
		}
		
		return id;
		
		
		
	}
}
