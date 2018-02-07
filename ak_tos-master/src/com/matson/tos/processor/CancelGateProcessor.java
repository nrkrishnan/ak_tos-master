package com.matson.tos.processor;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.matson.tos.jatb.Aard;
import com.matson.tos.util.GroovyXmlUtil;
import com.matson.tos.util.JMSSender;

public class CancelGateProcessor {
	private static Logger logger = Logger.getLogger(CancelGateProcessor.class);
	
	public void process() {
		try {
			String xml = getXml();
			//logger.debug( "XML:" + xml);
		
			JMSSender sender = new JMSSender( JMSSender.REAL_TIME_QUEUE, "HON");
			sender.send( xml);
		} catch (Exception e) {
			logger.error("Could not Cancel Gate Processor", e);
		}
		
	}
	
	public String getXml() {
		Map<String, String> data = new HashMap<String, String>();
		return GroovyXmlUtil.getInjectionXmlStr( "GvyCancelTrouble", "database", data);
	}
	
	public static void main(String[] args) {
		CancelGateProcessor p = new CancelGateProcessor();
		System.out.println(p.getXml());
	}
}
