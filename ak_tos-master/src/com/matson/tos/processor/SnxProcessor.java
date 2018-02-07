/*
*********************************************************************************************
*Srno   Date			AuthorName			Change Description
* A1    03/26/2010      Glenn Raposo		Update process method
*********************************************************************************************
*/
package com.matson.tos.processor;

import org.apache.log4j.Logger;

public class SnxProcessor {
	private static Logger logger = Logger.getLogger(SnxProcessor.class);

	public void process(String xml) {

		ChasRfidProcessor chasRfidProcessor = new ChasRfidProcessor();
		chasRfidProcessor.process(xml);
		
		logger.debug("After SnxProcessor Processor");
	}
}
