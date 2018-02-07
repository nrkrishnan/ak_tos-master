package com.matson.tos.reports;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.matson.tos.vo.SupplementProblems;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

public class SupplementProblemsReportGenerator implements JRDataSource{
	
	ArrayList<SupplementProblems> problemsList = null;

	int index = -1;
	
	public SupplementProblemsReportGenerator(ArrayList<SupplementProblems> problems) {
		if(problems==null || problems.size()<1)
			return;
		problemsList = problems;
		Collections.sort(problemsList, new Comparator<SupplementProblems>(){
		     public int compare(SupplementProblems plm1, SupplementProblems plm2){
		    	return plm1.getDiscrepancy().compareTo(plm2.getDiscrepancy());
		     }
		});
	}
	
	public Object getFieldValue(JRField jrField) throws JRException {
		SupplementProblems problem = problemsList.get(index);
		if(problem==null) {
			return null;
		}
		if (jrField.getName().equals("Discrepancy")) {
			return problem.getDiscrepancy();
		}
		if (jrField.getName().equals("ContainerNumber")) {
			return problem.getContainerNumber();
		}
		if (jrField.getName().equals("VesVoy")) {
			return problem.getVesvoy();
		}
		if (jrField.getName().equals("LocationStatus")) {
			return problem.getLocationStatus();
		}
		if (jrField.getName().equals("Event")) {
			return problem.getEvent();
		}
		if (jrField.getName().equals("EventDate")) {
			return problem.getEventDate();
		}
		if (jrField.getName().equals("SupDS")) {
			return problem.getSupDS();
		}
		if (jrField.getName().equals("SupDport")) {
			return problem.getSupDport();
		}
		if (jrField.getName().equals("SupCrstatus")) {
			return problem.getSupCrstatus();
		}
		if (jrField.getName().equals("SupConsignee")) {
			return problem.getSupConsignee();
		}
		if (jrField.getName().equals("SupBookingNumber")) {
			return problem.getSupBookingNbr();
		}
		if (jrField.getName().equals("OldDS")) {
			return problem.getOldDS();
		}
		if (jrField.getName().equals("OldDport")) {
			return problem.getOldDport();
		}
		if (jrField.getName().equals("OldCrstatus")) {
			return problem.getOldCrstatus();
		}
		if (jrField.getName().equals("OldConsignee")) {
			return problem.getOldConsignee();
		}
		if (jrField.getName().equals("OldBookingNumber")) {
			return problem.getOldBookingNbr();
		}
		return null;
	}
	
	public boolean next() throws JRException {
		index++;
		if (problemsList != null && index < problemsList.size()) {
			return true;
		}
		return false;
	}
}
