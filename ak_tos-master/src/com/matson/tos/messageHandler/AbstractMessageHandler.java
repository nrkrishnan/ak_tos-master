/*
**************************************************************************************
*Srno   Date			AuthorName			Change Description
* A1    19/12/07        Glenn Raposo		Changes made to incorporate DCMConverter  
**************************************************************************************
*/
package com.matson.tos.messageHandler;

import javax.xml.bind.*;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import java.io.StringReader;
import java.io.StringWriter;

import com.matson.text.bind.JATBContext;
import com.matson.text.bind.JATBException;
import com.matson.tos.exception.TosException;
import static javax.xml.bind.JAXBContext.newInstance;

public abstract class AbstractMessageHandler implements IMessageHandler {
	private static Logger logger = Logger.getLogger(AbstractMessageHandler.class);
	protected static final String pat = "MM/dd/yyyy HH:mm:ss";

	public static final int TEXT_TO_XML = 2;
	public static final int XML_TO_TEXT = 1;
	//A1 Changes Starts 
	public static final int TEXT_TO_TEXT = 3;
	//private int _direction;
	//A1 Changes Ends
	protected int _direction;
	protected String _textFmtFile;
	protected Object _textObj;
	protected String _textObjPackage;
	protected String _textStr;
	protected Object _xmlObj;
	protected String _xmlObjPackage;
	protected String _xmlStr;
	// for performance we hold these refs after instantiated
	protected com.matson.text.bind.Marshaller jatbM = null;
	protected com.matson.text.bind.Unmarshaller jatbU = null;
	protected javax.xml.bind.Marshaller jaxbM = null;
	protected javax.xml.bind.Unmarshaller jaxbU = null;
	
	/**
	 * Creates a message handler.
	 * @param xmlObjPackageName - the XML object package name like com.matson.tos.xml
	 * @param textObjPackageName - the text object package name like com.matson.tos.text
	 * @param fmtFile - the file that describes the text file format in XML
	 * @param convertDir - the conversion direction either XML to text or text to XML. it's defined in the this class
	 * @throws TosException - if the conversion direction is not defined
	 */
	public AbstractMessageHandler( String xmlObjPackageName, String textObjPackageName, String fmtFile, int convertDir) throws TosException {
		_direction = convertDir;
		_textFmtFile = fmtFile;
		_textObjPackage = textObjPackageName;
		_xmlObjPackage = xmlObjPackageName;
		if ( convertDir != XML_TO_TEXT && convertDir != TEXT_TO_XML) {
			throw new TosException( "Conversion direction: " + _direction + " is not supported.");
		}
	}
	//A1 Changes Starts 
	public AbstractMessageHandler(String textObjPackageName, String fmtFile, int convertDir) throws TosException {
		_direction = convertDir;
		_textFmtFile = fmtFile;
		_textObjPackage = textObjPackageName;
		if ( convertDir != TEXT_TO_TEXT) {
			throw new TosException( "Conversion direction: " + _direction + " is not supported.");
		}
	}
	//A1 Changes Ends 
	protected Object createTextObj() throws TosException {
		if ( _textStr == null)
			throw new TosException( "The text value string is null.");
		try {
			if ( jatbU == null) {
				JATBContext jc = JATBContext.newInstance( _textObjPackage, _textFmtFile );
				jatbU = jc.createUnmarshaller();
			}
			_textObj = jatbU.unmarshal( _textStr); 
			return _textObj;
		} catch ( JATBException exJabx) {
			logger.error("Error in creating text object for " + _textStr, exJabx);
			throw new TosException( "Error in creating text object for " + _textStr);
		} catch ( Exception ex) {
			throw new TosException( "Error: " + ex);
		}
	}
	protected String createTextStr() throws TosException {
		String xmlTemp = new String();
		try {
			if ( jatbM == null) {
				JATBContext jc = JATBContext.newInstance( _xmlObjPackage, _textFmtFile );
				jatbM = jc.createMarshaller();
			}
			jatbM.marshal( _xmlObj, xmlTemp); 
			return xmlTemp.toString();
			
		} catch ( JATBException exJabx) {
			logger.error("Error in creating text string for " + _xmlStr, exJabx);
			throw new TosException("Error in creating text string for " + _xmlStr);
		} catch ( Exception ex){
			throw new TosException("Error: " + ex);
		}
	}
	
	protected Object createXmlObj() throws TosException {
		if ( _xmlStr == null)
			throw new TosException( "The xml data string is null.");
		try {
			if ( jaxbU == null) {
				JAXBContext jc = newInstance( _xmlObjPackage );
				jaxbU = jc.createUnmarshaller();
			}
			_xmlObj = jaxbU.unmarshal( new StreamSource( new StringReader( _xmlStr ))); 
			return _xmlObj;
		} catch ( javax.xml.bind.JAXBException exJabx) {
			logger.error("Error in creating xml obj for: " + _xmlStr, exJabx);
			throw new TosException( "Error in creating xml object for " + _xmlStr);
		} catch ( Exception ex) {
			throw new TosException( "Error: " + ex);
		}
	}
	
	protected String createXmlStr() throws TosException {
		StringWriter xmlTemp = new StringWriter();
		try {
			if ( jaxbM == null) {
				JAXBContext jc = JAXBContext.newInstance( _xmlObjPackage );
				jaxbM = jc.createMarshaller();
			}
			jaxbM.marshal( _xmlObj, xmlTemp); 
			return xmlTemp.toString();
			
		} catch ( JAXBException exJabx) {
			exJabx.printStackTrace();
			logger.error("Error in creating xml string for " + _xmlStr, exJabx);
			throw new TosException("Error in creating xml string for " + _xmlStr);
		} catch ( Exception ex){
			ex.printStackTrace();
			logger.error("UnKnown Error in creating xml string for " + _xmlStr, ex);
			throw new TosException("Error: " + ex);
		}
	}
	
	public int getDirection(){
		return _direction;
	}
	public Object getTextObj(){
		if ( _textObj == null) {
			try {
				return this.createTextObj();
			} catch ( Exception ex) {
				logger.error( "Can not create text obj for " + _xmlStr);
				ex.printStackTrace();
			}
		}
		return _textObj;
	}

	public String getTextStr() throws TosException {
		if ( _direction == AbstractMessageHandler.XML_TO_TEXT) {
			_textObj = xmlObjToTextObj( _xmlObj);
			_textStr = createTextStr();
		}
		return _textStr;
	}
	public Object getXmlObj()throws TosException{
		if ( _xmlObj == null) {
			try {
				if ( _textObj != null)
					_xmlObj = textObjToXmlObj( _textObj);
				else if ( _xmlStr != null)
					_xmlObj = createXmlObj();
			} catch ( Exception ex) {
				logger.error( "Can not create xml obj for " + _xmlStr);
				logger.error( ex);
				throw new TosException(ex.toString());
			}
		}
		return _xmlObj;
	}
	public String getXmlStr() throws TosException {
		if ( _direction == AbstractMessageHandler.TEXT_TO_XML) {
			if ( _xmlObj == null) // convert only when no xml obj is available
				_xmlObj = textObjToXmlObj( _textObj);
			_xmlStr = createXmlStr();
			_xmlObj = null; // reset xml obj to null
		}
		return _xmlStr;
	}
	public void setDirection( int dir){
		_direction = dir;
	}
	
	public void setTextObj( Object obj) {
		_textObj = obj;
	}
	public void setTextStr( String text) throws TosException {
		_textStr = text;
		createTextObj();
		_xmlObj = null;
	}
	public void setXmlObj( Object xmlObj) {
		_xmlObj = xmlObj;
	}
	public void setXmlStr( String xml) throws TosException {
		_xmlStr = xml;
		createXmlObj();
		_textObj = null;
	}
	protected abstract Object textObjToXmlObj( Object textObj) throws TosException;
	protected abstract Object xmlObjToTextObj( Object xmlObj) throws TosException;
} 
