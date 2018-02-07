package com.matson.tos.util;

import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.matson.cas.common.util.LogProertyConfigure;

/**
 * Created on Feb 1, 2007
 * 
 * File: EnvironmentProperty.java
 * 
 * <PRE>
 * 	Change History
 *  Ver	Name	Date	 Comment
 *  1.0 SKB		Feb 1, 2007  Created
 * </PRE>
 */
public class EnvironmentProperty {
	private static boolean loaded = false;
	private static final String LOG = "toslog.properties";
	private static String envType = "dev";

	static {
		configure();
	}

	/**
	 * @return Returns the loaded.
	 */
	public static boolean isLoaded() {
		return loaded;
	}

	public static synchronized void configure() {
		if(!loaded) {
			try {
				envType = System.getProperty("weblogic.Name");
	            URL url = LogProertyConfigure.class.getClassLoader().getResource(LOG);
	            PropertyConfigurator.configure(url);
	            Logger.getLogger(LogProertyConfigure.class).info("LOG4J loaded from "+ url.getPath());
	            loaded = true;
	            //MemoryMonitor m = new MemoryMonitor();
	            //m.start();
	        } catch (Exception e) {
	        	System.err.println("Error configuring logging from " + LOG);
	        	e.printStackTrace();
	        }
			
		}
	}

	/**
	 * @return Returns the envType.
	 */
	public static String getEnvType() {
		return envType.toUpperCase();
	}

	public static Boolean isDev() {
		if (envType != null)
			if (envType.indexOf("DEV") != -1)
				return Boolean.TRUE;
			else
				return Boolean.FALSE;
		return Boolean.FALSE;
	}

	public static Boolean isProd() {
		if (envType != null)
			if (envType.indexOf("PROD") != -1)
				return Boolean.TRUE;
			else
				return Boolean.FALSE;
		return Boolean.FALSE;
	}
	
	public static Boolean isPreProd() {
		if (envType != null)
			if (envType.indexOf("PRE") != -1)
				return Boolean.TRUE;
			else
				return Boolean.FALSE;
		return Boolean.FALSE;
	}
	
	public static Boolean isQA() {
		if (envType != null)
			if (envType.indexOf("QA") != -1)
				return Boolean.TRUE;
			else
				return Boolean.FALSE;
		return Boolean.FALSE;
	}	
}

