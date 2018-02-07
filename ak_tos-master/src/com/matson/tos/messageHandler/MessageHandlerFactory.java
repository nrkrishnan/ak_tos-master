package com.matson.tos.messageHandler;

import org.apache.log4j.Logger;

import com.matson.tos.exception.TosException;
import com.matson.tos.messageHandler.IMessageHandler;

public class MessageHandlerFactory {
	private static Logger logger = Logger.getLogger(MessageHandlerFactory.class);
	private static MessageHandlerFactory _instance = null;
	
	private MessageHandlerFactory() {
		
	}
	public static synchronized MessageHandlerFactory  getInstance() {
		if ( _instance == null) {
			_instance = new MessageHandlerFactory();
		}
		return _instance;
	}
	
	public IMessageHandler createHandler( String msgType, String msgBody) {
		logger.debug("Creating handler for message type: " + msgType);
		logger.debug("Message body: [" + msgBody + "]");
		IMessageHandler handler = null;
		
		try {
		if ( msgType.equalsIgnoreCase( "M102")) {
			handler = new SnxMessageHandler("","", "",9);
			//handler.setText("");
			handler.setDirection( AbstractMessageHandler.TEXT_TO_XML);
		} else {
			
			return null;
		}
		} catch ( TosException tex) {
			
		}
		return handler;
	}
}
