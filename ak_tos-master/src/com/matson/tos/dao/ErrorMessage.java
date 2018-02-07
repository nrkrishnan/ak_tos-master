package com.matson.tos.dao;

import org.apache.log4j.Logger;

import com.matson.cas.refdata.mapping.TosErrorMsg;
import com.matson.tos.processor.MdbSn4MessageProcessor;
import com.matson.tos.util.EmailSender;
import com.matson.tos.util.JMSSender;
import com.matson.tos.util.TosRefDataUtil;

public class ErrorMessage {
	private static Logger logger = Logger.getLogger(ErrorMessage.class);
	
	public static TosErrorMsg getError(String id) {
		System.out.println("Error msg="+id);
		TosErrorMsg msg = new TosErrorMsg();
		return (TosErrorMsg)msg.queryById(id);
	}
	
	public static void resubmit(String msg, String port) {
		logger.info("Resubmitting("+port+"):"+msg);
		try {
			JMSSender sender = new JMSSender(JMSSender.REAL_TIME_QUEUE, port);
			sender.send(msg);
		} catch (Exception e) {
			logger.error("Error in resubmitting("+port+"):"+msg, e);
			String emailAddr = TosRefDataUtil.getValue( "SUPPORT_EMAIL");
			String mailhost = TosRefDataUtil.getValue( "MAIL_HOST");
			EmailSender.mailAttachment(emailAddr,emailAddr,mailhost,"ResubmitError.txt",msg,e.getMessage(),"JMS resubmit error");
		}
	}
	
}
