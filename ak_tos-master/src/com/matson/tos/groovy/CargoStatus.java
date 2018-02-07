package com.matson.tos.groovy;

import java.util.Enumeration;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.log4j.Logger;

import com.matson.cas.refdata.RefDataLookup;
import com.matson.cas.refdata.mapping.TosCargoStatusFormula;
import com.matson.tos.exception.TosException;
import com.matson.tos.groovy.formatter.EmailCargoStatus;


public class CargoStatus {
	private static Logger logger = Logger.getLogger(CargoStatus.class);
	
	public static String EMAIL_KEY_PREFIX = "emailAddress_";
	
	private static String refClassName = "com.matson.cas.refdata.mapping.TosCargoStatusFormula";

	private Map _data;
	
	
	public CargoStatus( Map data) {
		_data = data;
		// following code is special for HOLD/RELEASE process
		String eventId = (String)_data.get( "EVENT_ID");
		String currHold = (String)_data.get( EmailCargoStatus.tagUnitHoldsAndPermissions);
		String[] holds = currHold.split( ",");
		int idx=-1;
		logger.debug( "Current holds = " + currHold);
		String type = eventId.substring(0, eventId.indexOf( "_"));
		logger.debug( "Type = " + type);
		if ( eventId.equalsIgnoreCase( type + "_HOLD")) {
			idx = index( holds, type);
			if ( idx >= 0){
				insertPrevValue( holds, idx);
			}
		} else if ( eventId.equalsIgnoreCase( type + "_RELEASE")) {
			_data.put( "updt_" + EmailCargoStatus.tagUnitHoldsAndPermissions, getPrevValue( currHold, type));
		}
	}
	private int index( String[] holds, String aHold) {
		if ( holds.length == 0)
			return -1;
		for ( int i = 0; i < holds.length; i++){
			if ( aHold.equalsIgnoreCase( holds[i]))
				return i;
		}
		return -1;
	}
	private void insertPrevValue( String[] holds, int idx) {
		String prevValue ="";
		for ( int i=0; i<holds.length; i++) {
			if ( idx != i)
				prevValue += holds[i] + ",";
		}
		if ( prevValue.endsWith( ","))
			prevValue = prevValue.substring(0, prevValue.length()-1);
		_data.put( "updt_" + EmailCargoStatus.tagUnitHoldsAndPermissions, prevValue);
		logger.debug( "Prev holds = " + prevValue);
	}
	private String getPrevValue( String currHold, String prevHold) {
		if ( currHold.length() > 0)
			return currHold + "," + prevHold;
		else
			return prevHold;
	}
	public boolean sendEmail(){
		List result = RefDataLookup.getCache( refClassName);
		if ( result == null) {
			logger.error( "Could not find any formula from database.");
			return false;
		}
		logger.debug( "Found " + result.size() + " formula.");
		//Check for cargo status No Email group users
		if(!noEmailGroup()){
	    	logger.debug("Do Not Send CS Report to User/Group");
	    	return false;
	    }
		
		TosCargoStatusFormula aFormulaClass = null;
		String aFormula = null;
		int emailIndex = 0;
		boolean ret = false;
		ListIterator iter = result.listIterator();
		String emailKey = null;
		while ( iter.hasNext()) {
			aFormulaClass = (TosCargoStatusFormula)iter.next();
			aFormula = aFormulaClass.getFormula();
			
			if ( evaluate( aFormula)) {
				emailKey = EMAIL_KEY_PREFIX + emailIndex++;
				logger.debug( " email key = " + emailKey);
				logger.debug( "email = " + aFormulaClass.getEmails());
				_data.put( emailKey, aFormulaClass.getEmails());
				ret = true;
			}
		}
		return ret;
	}
	
	public Map getEmailData() {
		return _data;
	}
	
	private boolean evaluate( String formula) {
		logger.debug( "Formula = " + formula);
		boolean ret = false;
		try {
			org.nfunk.jep.JEP myParser = new org.nfunk.jep.JEP();
			myParser.setAllowUndeclared( true);
			org.nfunk.jep.Node aNode = myParser.parse( formula);
			Enumeration st = myParser.getSymbolTable().keys();
			String aVar = null;
			String aValue = null;
			while ( st.hasMoreElements()) {
				aVar = (String)st.nextElement();
				aValue = (String)_data.get( aVar);
				logger.debug( "Symbol = " + aVar + "  value = " + aValue);
				if ( aValue != null) {
					if ( aValue.equalsIgnoreCase( "null"))
						return ret;
					else
						myParser.addVariable( aVar, aValue);
				} else 
					return ret;
			}
			
			Object res = myParser.evaluate( aNode); 
			ret = res.toString().equalsIgnoreCase( "1.0");
			logger.debug( "Return from evaluate() = " + ret);
			return ret;
		} catch ( Exception ex) {
			logger.error( "Error: ", ex);
		}
		return ret;
	}
	
	//Check for cargo status No Email group users
	public boolean noEmailGroup()
	{ 
	  boolean reportFlag = false; 	
	  try
	  {
	    String reportStatus = (String)_data.get("userRole");
	    String pod = (String)_data.get("POD");
	    pod = pod != null ? pod : "";
	    if(reportStatus.equals("No Email") && (pod.equals("HIL")||pod.equals("KAH") || pod.equals("KHI") || pod.equals("NAW"))){
	        reportFlag=true;           
	    }else if(reportStatus.equals("Email")){
	    	reportFlag= true;
	    }
	  }catch(Exception e){
		  logger.error( "Error: ", e); 
	  }
	  return reportFlag;
	}
}
