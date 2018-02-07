package com.matson.tos.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.matson.cas.refdata.RefDataLookup;
import com.matson.cas.refdata.mapping.TosStifCliOwner;

/**
 * TODO, Make this a table
 * @author Steven Bauer
 *
 */
public class StifClientOwnerMap {
	//private static HashMap<String,String> ownerMap = new HashMap();
	//private static HashMap<String,String> operatorMap = new HashMap();
	
	private static HashMap<String,TosStifCliOwner> ownerMap = new HashMap();
	
	
	static {
		try {
		List owners = RefDataLookup.getCache("com.matson.cas.refdata.mapping.TosStifCliOwner");
		Iterator iter = owners.iterator();
		while(iter.hasNext()) {
			TosStifCliOwner record = (TosStifCliOwner)iter.next();
			addRecord(record);
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		/*
		addRecord("MAT","MATU","MAT");
		
		addRecord("HLC","HLCU","HLC");
		addRecord("HLL","HLCU","HLC");
		
		addRecord("HSD","HSDU","HSD");
		addRecord("COL","HSDU","HSD");
		
		addRecord("MAE","MAEU","MAE");
		addRecord("PON","MAEU","MAE");
		addRecord("BSU","MAEU","MAE");
		addRecord("BSP","MAEU","MAE");
		addRecord("MSK","MAEU","MAE");
		addRecord("MSL","MAEU","MAE");
		//addRecord("ANL","MAEU","MAE");
		
		addRecord("FHS","FHSU","FHS");
		addRecord("FAN","FHSU","FHS");
		addRecord("FES","FHSU","FHS");
		
		addRecord("ANA","ANLC","ANL");
		addRecord("ANL","ANLC","ANL");
		addRecord("CMD","ANLC","ANL");
		
		*/
	}
	
	private static void addRecord(TosStifCliOwner record) {
		ownerMap.put(record.getService(), record);
	}
	
	private static void addRecord(String key,String owner, String operator) {
		TosStifCliOwner record = new TosStifCliOwner();
		record.setService(key);
		record.setOwner(owner);
		record.setOperator(operator);
		
		ownerMap.put(key, record);
		//operatorMap.put(key, operator);
	}
	
	public static String getOperator(String service) {
		TosStifCliOwner result = ownerMap.get(service);
		if(result == null) return service;
		return result.getOperator();
	}
	
	public static String getOwner(String service) {
		TosStifCliOwner result = ownerMap.get(service);
		if(result == null) return service;
		return result.getOwner();
	}
}
