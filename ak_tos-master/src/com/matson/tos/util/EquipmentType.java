package com.matson.tos.util;

import com.matson.tos.jaxb.snx.TPhysical;

public class EquipmentType {

	private String type;
	private String grade;
	
	public EquipmentType(String cmisType) {
		if(cmisType == null || cmisType.length() == 0) return;
		
		if(cmisType.length() > 4) {
			grade = cmisType.substring(4).trim();
			type  = cmisType.substring(0,4).trim();
		} else {
			type  = cmisType.trim();
		}
	}

	public String getType() {
		return type;
	}

	public String getGrade() {
		return grade;
	}
	
	
}
