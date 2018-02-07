/*
 * 07/20/2011  GR  Added isLoaded Method for hazInsert   
 */
		
package com.matson.tos.constants;

import java.util.HashMap;
import java.util.Iterator;

public class TransitState {
	public final String name;
	public final String id;
		
	private static HashMap<String,TransitState> stateMap = new HashMap<String,TransitState>();
	
	private TransitState(String id,String name) {
		this.id = id;
		this.name = name;
	}
	
	static {
		stateMap.put("S10_ADVISED", new TransitState("S10_ADVISED","ADVISED"));
		stateMap.put("S20_INBOUND", new TransitState("S20_INBOUND","INBOUND"));
		stateMap.put("S30_ECIN", new TransitState("S30_ECIN","ECIN"));
		stateMap.put("S40_YARD", new TransitState("S40_YARD","YARD"));
		stateMap.put("S50_ECOUT", new TransitState("S50_ECOUT","ECOUT"));
		stateMap.put("S60_LOADED", new TransitState("S60_LOADED","LOADED"));
		stateMap.put("S70_DEPARTED", new TransitState("S70_DEPARTED","DEPARTED"));
		stateMap.put("S99_RETIRED", new TransitState("S99_RETIRED","RETIRED"));
	}
	
	public static TransitState getTransitState(String id) {
		return stateMap.get(id);
	}
	
	public static TransitState findTransitStateByName(String name) {
		Iterator<TransitState> iter = stateMap.values().iterator();
		while(iter.hasNext()) {
			TransitState state = iter.next();
			if(state.name.equals(name)) return state;
		}
		return null;
	}
	
	public boolean isActiveUnit() {
		if(this == stateMap.get("S20_INBOUND") ) return true;
		if(this == stateMap.get("S30_ECIN") ) 	 return true;
		if(this == stateMap.get("S40_YARD") ) 	 return true;
		if(this == stateMap.get("S50_ECOUT") ) 	 return true;
		return false;
	}
	
	public boolean isLoadedUnit() {
		if(this == stateMap.get("S60_LOADED") ) return true;
		
		return false;
	}
}
