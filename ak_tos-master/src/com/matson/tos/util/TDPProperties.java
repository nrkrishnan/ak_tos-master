package com.matson.tos.util;

import java.util.Properties;

import org.apache.log4j.Logger;

import com.matson.tos.processor.NewVesFileProcessor;

public class TDPProperties {
	private static TDPProperties tdpProperties;
	private static Logger logger = Logger.getLogger(TDPProperties.class);
	private Properties properties;
	private TDPProperties() { 
		properties = new Properties();
		try {
			logger.info("Load Properties file from class path.");							
			properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("tdp.properties"));
		} catch (Exception ex) {
			logger.error("Exception occurs:", ex);
		}
	}
	
	public static final TDPProperties getInstance() {
		if (tdpProperties==null) {
			synchronized(TDPProperties.class) {
				tdpProperties = new TDPProperties();
			}
		}
		return tdpProperties;
	}
	
	public void setProperty(String key, String value) {
		properties.setProperty(key, value);
	}
	
	public String getProperty(String key) {
		System.out.println("Get value from properties "+key);
		return properties.getProperty(key);
	}

}
