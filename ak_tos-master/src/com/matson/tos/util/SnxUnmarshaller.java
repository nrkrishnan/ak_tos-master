package com.matson.tos.util;

import java.io.InputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.matson.tos.jaxb.snx.*;
import com.matson.tos.jaxb.snx.error.SnxError;


public class SnxUnmarshaller {

	public static Snx unmarshall(InputStream stream) throws JAXBException, ParserConfigurationException, SAXException, IOException {
	  /*
		javax.xml.parsers.SAXParser sp = javax.xml.parsers.SAXParserFactory.newInstance().newSAXParser();
		Converter converter = new Converter();
	    converter.setParent(sp.getXMLReader());

	    JAXBContext context = JAXBContext.newInstance( "com.matson.tos.jaxb.snx" ); 
	    javax.xml.bind.UnmarshallerHandler uh = context.createUnmarshaller().getUnmarshallerHandler();
	    converter.setContentHandler(uh);
	    converter.parse(new org.xml.sax.InputSource(stream));
	    Snx snx = (Snx) uh.getResult();
*/
	    
		JAXBContext jc = JAXBContext.newInstance( "com.matson.tos.jaxb.snx" ); 
		Unmarshaller u = jc.createUnmarshaller(); 
		Snx snx = (Snx)u.unmarshal(stream);
		return snx;

	}
	
	public static SnxError unmarshallError(InputStream stream) throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance( "com.matson.tos.jaxb.snx.error" ); 
		Unmarshaller u = jc.createUnmarshaller(); 
		SnxError snx = (SnxError)u.unmarshal(stream);
		return snx;

	}
	
	public static String getContainerNumber(String msg) {
		Pattern p = Pattern.compile("<unit.*?id=\"(.*?)\"");
		//Pattern p = Pattern.compile("unit");
		Matcher m = p.matcher(msg);
		if(m.find()) {
			return m.group(1);
		} 
		return null;
		
		
	}
	
	public static String getVesvoy(String msg) {
		Pattern p = Pattern.compile("<carrier.*?id=\"(.*?)\"");
		Matcher m = p.matcher(msg);
		if(m.find()) {
			return m.group(1);
		} 
		return null;
	}
	
	public static String getBookingNumber(String msg) {
		Pattern p = Pattern.compile("<booking.*?nbr=\"(.*?)\"");
		Matcher m = p.matcher(msg);
		if(m.find()) {
			return m.group(1);
		} 
		return null;
	}
	
}
