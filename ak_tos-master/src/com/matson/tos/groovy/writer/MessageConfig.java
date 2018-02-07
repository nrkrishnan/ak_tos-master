/**
 * 
 */
package com.matson.tos.groovy.writer;

import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.matson.tos.exception.TosException;

/**
 * @author JZF
 *
 */
public class MessageConfig {
	private static Logger logger = Logger.getLogger(MessageConfig.class);
	
	private static MessageConfig _instance = null;
	private static Map<String, Map> _msgFields = new HashMap<String, Map>();
	private static Map<String, String> _msgFileNames = new HashMap<String, String>();
	// this is a singleton. no public constructor
	private MessageConfig() {
		
	}
	
	public synchronized static MessageConfig getInstance() {
		if ( _instance == null) {
			_instance = new MessageConfig();
			_instance.init();
		}
		return _instance;
	}
	
	public Map getFields( String msgType) {
		return _msgFields.get( msgType);
	}
	
	public String getFileName( String msgType) {
		return (String)_msgFileNames.get( msgType);
	}
	
	private void init() {
		String msgType;
		try{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			//Reads list of MessageType and Mapping data file value. 
			InputStream input = getFileStream( "/xml/MessageList.xml");
			Document doc = db.parse(input);
			Node aNode = null;
			NodeList nodes = doc.getElementsByTagName("Message");
			logger.debug("Nodes Length : "+nodes.getLength());
			for(int i=0; i<nodes.getLength();i++)
			{
				if (!nodes.item( i).hasAttributes()){
					logger.debug("No Attributes Present");
					continue;
				}
				NamedNodeMap attrNodes = nodes.item( i).getAttributes();
				aNode = attrNodes.getNamedItem("type");
				if ( aNode == null)
	            	throw new Exception( "Need to define type tag in the file.");
				msgType = aNode.getNodeValue();
				
            	aNode = attrNodes.getNamedItem("mappingFile");
            	logger.debug("MessageType : "+msgType+ " Mapping File : "+aNode.getNodeValue());
            	//Read the Data Mapping File 
            	_msgFields.put( msgType, readMappingFile(msgType,aNode.getNodeValue()));
	            
			}
		}catch(Exception e) {
			logger.debug( "Error in parsing mapping files: ", e);
		}
	}
	
	/*
	 * Method read the Data Mapping.xml and obtains the data Field and MessageType mapping values.   
	 */
	private Map<String, FieldData> readMappingFile(String msgType,String mappingFile)
	{
		Map<String, FieldData> fields = new LinkedHashMap<String, FieldData>();
		FieldData fieldData = null;
		String fieldSize = "0";
		String dataType = null;
		String name = null;
		String seq  = "0";
		try
		{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			//Read the Mapping file for specific Message
			InputStream input = getFileStream( "/xml/"+mappingFile);
			Document doc = db.parse(input);
			NamedNodeMap attrNodes = null;
			Node aNode = null;
			NodeList nodes = null;
			
			//Stores table name and MessageType in object key value pair 
			nodes = doc.getElementsByTagName("File");
			attrNodes = nodes.item(0).getAttributes();
			aNode = attrNodes.getNamedItem("name");
			String fileName = aNode.getNodeValue();
			_msgFileNames.put(msgType,fileName);
		
			//1. Stores the Field mapping data in a Java Object
			//2. Stores the TagName and Object instance as key value pair   
			nodes = doc.getElementsByTagName("Field");
		
			for(int i=0;i<nodes.getLength();i++)
			{
				fieldData = new FieldData();
				attrNodes = nodes.item(i).getAttributes();
				seq = attrNodes.getNamedItem( "sequence").getNodeValue();
				fieldData.setSequenceNum(Integer.parseInt(seq));
				name =  attrNodes.getNamedItem( "name").getNodeValue();
				fieldData.setFieldName(attrNodes.getNamedItem( "name").getNodeValue());
				dataType = attrNodes.getNamedItem( "dataType").getNodeValue();
				fieldData.setDataType(dataType);
				fieldSize = attrNodes.getNamedItem( "size").getNodeValue();
				fieldData.setFieldSize(Integer.parseInt(fieldSize));
				fields.put( name, fieldData);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return fields;
	}

	/*
	 * Open the Formated File as  Stream
	 */
	private InputStream getFileStream( String name) throws TosException {
		ClassLoader cl = this.getClass().getClassLoader();
		
		InputStream fileStream = cl.getResourceAsStream( name);
		if ( fileStream == null) {
			//logger.debug( "Can not find the format file: " + name);
			throw new TosException( "Can not find format file: " + name);
		}
		//logger.debug( "Found the format file:" + name);
		return fileStream;
	}

}
