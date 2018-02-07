/**
 * 
 */
package com.matson.tos.groovy.writer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import com.matson.tos.exception.TosException;


/**
 * @author JZF
 *
 */
public class ParadoxWriter implements ObjectWriter {
	private static Logger logger = Logger.getLogger(ParadoxWriter.class);
	
	private Map _data;
	
	public ParadoxWriter( Map data) {
		_data = data;
	}
	/* (non-Javadoc)
	 * @see com.matson.tos.groovy.writer.ObjectWriter#write()
	 */
	public boolean write() {
		// TODO Auto-generated method stub
		String msgType = null;
		boolean ret = false;
		try {
			msgType = (String)_data.get( ObjectWriterFactory.MSG_TYPE);
			MessageConfig instance = MessageConfig.getInstance();
			String tableName = instance.getFileName( msgType);
			Map<String, FieldData> fields = instance.getFields( msgType);
			
			String sqlStr = createSQL( tableName, fields);
			
			Connection dbConnection = getConnection( tableName);
			
			ret = executeSQL( dbConnection, sqlStr);
		} catch ( Exception ex) {
			logger.error( "Error in process msg type" + msgType, ex);
		}
		return ret;
	}

	private String createSQL( String tableName, Map<String, FieldData> fields) {
		
		StringBuffer bufValues = null;
		StringBuffer bufFieldNames = null;
		String fieldName;
		String tagName;
		String dataType;
		
		for (Iterator iterField = fields.entrySet().iterator(); iterField.hasNext();)
		{ 
		    Map.Entry entryField = (Map.Entry)iterField.next();
		    
	    	FieldData aField = (FieldData)entryField.getValue();
	    	fieldName = aField.getFieldName();
	    	bufFieldNames.append( fieldName + ",");
	    	tagName = aField.getMsgTag();
	    	dataType = aField.getDataType();
	    	
	    	if ( _data.get( tagName) == null) {
	    		bufValues.append( "null,");
	    	} else {
		    	if(dataType.equalsIgnoreCase("int") || dataType.equalsIgnoreCase("Integer")){
		    		bufValues.append( _data.get( tagName)+",");   
		    	}else if(dataType.equalsIgnoreCase("double")){
		    		bufValues.append( _data.get( tagName)+",");
		    	}else{
		    		bufValues.append( "\'" + _data.get( tagName) + "\',");
		    	}  
	    	}
		}
		String fieldNames = "";
		String values = "";
		if ( bufFieldNames.length() > 1)
			fieldNames = bufFieldNames.substring( 0, bufFieldNames.length() -1); // remove the last ","
		if ( bufValues.length() > 1)
			values = bufValues.substring( 0, bufValues.length() -1);
		String sqlStr = "INSERT INTO " + tableName + " (" + fieldNames + ") VALUES (" + values + ")";
		
		return sqlStr;
	}
	
	private Connection getConnection( String tableName) {
		String URL_PARADOX_FILE = "jdbc:paradox://domain.com:3099/c:/data";
		String url = URL_PARADOX_FILE + "/" + tableName;
		Connection conn = null;
		try { 
			Class.forName("com.hxtt.sql.paradox.ParadoxDriver");
	        conn = DriverManager.getConnection(url, null, null);
	        
			if(conn != null)
				logger.debug("Connected");
			else
				logger.debug(" NOT Connected");
			
	    } catch (Exception ex) {
	    	logger.debug( "Error in creating connection on URL: " + url, ex);
	    } 
	    return conn;
	}
	
	private boolean executeSQL( Connection conn, String sqlStr) throws TosException {
		Statement stmt = null;
		try {
	        stmt = conn.createStatement();
	        int ret = stmt.executeUpdate( sqlStr);
	        conn.close();
	        conn = null;
	        if ( ret == 1) 
	        	return true;
	        else 
	        	return false;
		} catch ( Exception ex) {
			logger.error( "Error in executing SQL: " + sqlStr, ex);
		} finally{
	    	try{
      	        if( stmt != null) stmt.close();
	    		if( conn!= null) conn.close();
	    	}catch(Exception e){
	    		logger.debug( "Error in closing.", e);
	    	}
	    }
		return true;
	}
}
