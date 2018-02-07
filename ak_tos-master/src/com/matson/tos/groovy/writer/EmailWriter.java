/**
 * 
 */
package com.matson.tos.groovy.writer;

import java.util.Map;

import org.apache.log4j.Logger;

import com.matson.tos.groovy.formatter.EmailFormatter;
import com.matson.tos.util.EmailSender;

/**
 * @author JZF
 *
 */
public class EmailWriter implements ObjectWriter {
	private static Logger logger = Logger.getLogger(EmailWriter.class);
	protected EmailFormatter _formatter;
	
	public EmailWriter( EmailFormatter formatter){
		_formatter = formatter;
	}
	/* (non-Javadoc)
	 * @see com.matson.tos.groovy.writer.ObjectWriter#write()
	 */
	public boolean write() {
		// TODO Auto-generated method stub
		if ( _formatter == null) {
			logger.debug( "No email formatter found.");
			return false;
		}
		String emailMsg = _formatter.getContent();
		logger.debug("Send Email !!!");
		EmailSender.sendMail(_formatter.getFromEmailAddr(), _formatter.getToEmailAddr(),
				_formatter.getSubject(), emailMsg);
		logger.debug("Log to database !!!");
		_formatter.logMsg(emailMsg);
		return true;
	}
	
}
