/**
 * 
 */
package com.matson.tos.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.matson.tos.exception.TosException;


/**
 * @author JZF
 * A1 2/26/2009 SKB added escapeXml for the value.
 */
public class GroovyXmlUtil {
	private static Logger logger = Logger.getLogger(GroovyXmlUtil.class);
	
	public static final String VALUE_CLASS_LOCATION = "database";
	
	public static String getInjectionXmlStr( String className, Map<String, String> data){
		return getInjectionXmlStr( className, VALUE_CLASS_LOCATION, data);
	}
	
	public static String getInjectionXmlStr( String className, String classLoc, Map<String, String> data) {
		if ( data == null)
			return null;
		
		StringBuffer ret = new StringBuffer();
		
		ret.append( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		ret.append( "<argo:snx xmlns:argo=\"http://www.navis.com/argo\" ");
		ret.append( "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
		ret.append( "xsi:schemaLocation=\"http://www.navis.com/argo snx.xsd\">");
		ret.append( "<groovy class-location=\"" + classLoc
				+ "\" class-name=\"" + className + "\">");
		ret.append( "<parameters>");
		
		for (Map.Entry<String, String> item : data.entrySet() ) {
			// A1, escape values.
			String value = org.apache.commons.lang.StringEscapeUtils.escapeXml(item.getValue());
			ret.append( "<parameter id=\"");
			ret.append( item.getKey() + "\" ");
			ret.append( "value=\"" + value + "\" />");
		}
		ret.append( "</parameters>" + "</groovy>");
		ret.append( "</argo:snx>");
		return ret.toString();
	}
	
	public static Map getMsgMap(String groovyMsg) throws TosException
	{
		logger.info(" Begin getMsgMap :"+groovyMsg);
		Map<String, String> map = new HashMap<String, String>();
		try {
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
	        InputStream is = new ByteArrayInputStream ( groovyMsg.getBytes());
	        Document doc = docBuilder.parse ( is);
	        
	        NodeList listOfElem = doc.getElementsByTagName( "GroovyMsg");
	        Node aNode = listOfElem.item( 0);
	        NamedNodeMap attributes = aNode.getAttributes();
	        for ( int i=0; i < attributes.getLength(); i++) {
	        	Attr anAttr = (Attr)attributes.item( i);
	        	logger.debug(" Key and Value in getMsgMap :"+anAttr.getNodeName()+"---"+anAttr.getNodeValue());
	        	map.put( anAttr.getNodeName(), anAttr.getNodeValue());
	        }
	    }catch(Exception e) {
			logger.error( "Error in parsing GroovyMsg: " + groovyMsg,e);
			throw new TosException( "Error inparsing GroovyMsg: " + groovyMsg);
		}
		logger.info("End getMsgMap :"+map.toString());
		return map; 
	}

	public static Map getResponseMap(String groovyMsg) throws TosException
	{
		Map<String, String> map = new HashMap<String, String>();
		try {
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
	        InputStream is = new ByteArrayInputStream ( groovyMsg.getBytes());
	        Document doc = docBuilder.parse ( is);
	        
	        NodeList listOfElem = doc.getElementsByTagName( "parameter");
	        for ( int i=0; i < listOfElem.getLength(); i++) {
	        	Node aNode = listOfElem.item(i);
	        	NamedNodeMap attr = aNode.getAttributes();
	        	if(attr.getNamedItem("id") != null && attr.getNamedItem("value") != null) {
	        	   map.put( attr.getNamedItem("id").getNodeValue(), attr.getNamedItem("value").getNodeValue());
	        	}
	        }
	    }catch(Exception e) {
			logger.error( "Error in parsing Groovy Response: " + groovyMsg,e);
			throw new TosException( "Error inparsing Groovy Response: " + groovyMsg);
		}
		return map; 
	}
}
