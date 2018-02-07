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
public class MessageConfigCmis {
	private static Logger logger = Logger.getLogger(MessageConfigCmis.class);
	
	private static MessageConfigCmis _instance = null;
	private static Map<String, Map> _msgFields = new HashMap<String, Map>();
	private static Map<String, String> _msgActionNames = new HashMap<String, String>();
	private static Map<String, Map> _legFieldsMapping = new HashMap<String, Map>();

	// this is a singleton. no public constructor
	private MessageConfigCmis() {
		
	}
	
	public synchronized static MessageConfigCmis getInstance() {
		if ( _instance == null) {
			_instance = new MessageConfigCmis();
			_instance.init();
		}
		return _instance;
	}
	
	public Map getFields( String msgType) {
		return _msgFields.get( msgType);
	}
	
	public String getCmisActionName( String msgType) {
		return (String)_msgActionNames.get( msgType);
	}
	
	public Map getLegMapping( String msgType) {
		return _legFieldsMapping.get( msgType);
	}
	
	private void init() {
		String msgType;
		try{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			//Reads list of MessageType and Mapping data file value. 
			InputStream input = getFileStream( "/xml/MsgMapping.xml");
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
			logger.error( "Error in parsing mapping files: ", e);
		}
	}
	
	/*
	 * Method read the Data Mapping.xml and obtains the data Field and MessageType mapping values.   
	 */
	private Map<String, FieldDataCmis> readMappingFile(String msgType,String mappingFile)
	{
		Map<String, FieldDataCmis> fields = new LinkedHashMap<String, FieldDataCmis>();
		FieldDataCmis fieldData = null;
		String fieldSize = "0";
		String name = null;
		String n4Action = null;
		String cmisAction = null;
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

			//1. Stores the Field mapping data in a Java Object
			//2. Stores the TagName and Object instance as key value pair   
			nodes = doc.getElementsByTagName("Field");
			
			if(fileName.equals("CmisAction")){
			   for(int i=0;i<nodes.getLength();i++){
				  attrNodes = nodes.item(i).getAttributes();
				  n4Action =  attrNodes.getNamedItem( "n4Action").getNodeValue();
				  cmisAction = attrNodes.getNamedItem( "cmisAction").getNodeValue();
				  //logger.debug("n4Action : "+n4Action+" cmisAction : "+cmisAction);
				 _msgActionNames.put(n4Action,cmisAction);
			  }
			}
			else if(fileName.equals("CmisFields") || fileName.equals("CmisChassisFields") 
					|| fileName.equals("CmisVesVisitFields") || fileName.equals("CmisAcryFields") || fileName.equals("CmisTruckVisitFields")){
			   for(int i=0;i<nodes.getLength();i++){
				  fieldData = new FieldDataCmis();
				  attrNodes = nodes.item(i).getAttributes();
				  seq = attrNodes.getNamedItem( "sequence").getNodeValue();
				  fieldData.setSequenceNum(Integer.parseInt(seq));
				  name =  attrNodes.getNamedItem( "name").getNodeValue();
				  fieldData.setFieldName(name);
				  fieldSize = attrNodes.getNamedItem( "size").getNodeValue();
				  fieldData.setFieldSize(Integer.parseInt(fieldSize));
				  fields.put( name, fieldData);
			  }
			}
			else if(fileName.equals("LegField")){
				boolean readLeg = readLegMappingFile(doc);
			}//Else If Ends

		}
		catch(Exception e){
			logger.error( "Error in Reading Mapping File: ", e);
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
			logger.debug( "Can not find the format file: " + name);
			throw new TosException( "Can not find format file: " + name);
		}
		logger.debug( "Found the format file:" + name);
		return fileStream;
	}
	private boolean readLegMappingFile(Document doc)
	{
		NodeList nodes = null;
		NamedNodeMap attrNodes = null;
		//Variables used by child nodes  
		String fromPort = null;
		String toPort = null;
		String legValue = null;
		NodeList childNodes = null;
		NamedNodeMap childAttrNodes = null;
		
		nodes = doc.getElementsByTagName("FromPort");
		for(int i=0;i<nodes.getLength();i++)
		{
		  attrNodes = nodes.item(i).getAttributes();
		  fromPort = attrNodes.getNamedItem("name").getNodeValue();
		  childNodes = nodes.item(i).getChildNodes();
		  Map<String, String> toPortMap = new HashMap<String,String>();
		  for(int j=0;j<childNodes.getLength();j++){
			if (childNodes.item(j).getNodeType() == Node.ELEMENT_NODE){
			  childAttrNodes = childNodes.item(j).getAttributes();
			  toPort = childAttrNodes.getNamedItem("name").getNodeValue();
			  legValue = childAttrNodes.getNamedItem("legValue").getNodeValue();
			  toPortMap.put(toPort, legValue);
			 }
		  }
		  _legFieldsMapping.put(fromPort,toPortMap);
	     }
	  return true;
	}
}
