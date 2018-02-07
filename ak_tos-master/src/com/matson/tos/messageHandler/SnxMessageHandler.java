package com.matson.tos.messageHandler;

import org.apache.log4j.Logger;

import com.matson.tos.exception.TosException;

public class SnxMessageHandler extends AbstractMessageHandler{
	private static Logger logger = Logger.getLogger(SnxMessageHandler.class);
	
	public SnxMessageHandler(String s1, String s2, String s3, int dir) throws TosException {
		super(  s1,  s2,  s3, dir);
	}
	
	protected Object textObjToXmlObj( Object o1) throws TosException{
		logger.debug("Convert text obj to xml obj format.");
		
		return null;
	}
	protected Object xmlObjToTextObj( Object o1) throws TosException {
		logger.debug("Convert xml to text format.");
		return null;
	}
	
}
