package com.matson.tos.reports;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import com.matson.cas.refdata.mapping.TosRdsDataFinalMt;
import com.matson.tos.dao.NewReportVesselDao;

/* This class gets the records from DB for the Rob Containers On Vessel Report and stores it in the list. 
 * 
 * 	Srno	Date			AuthorName				Change Description
 * 	A1		10/8/2012		Meena Kumari			Initial creation 
 * 	A2		04/12/2013		Karthik Rajendran		Added: Sorting on Container number
 * 
 */

public class RobContainersOnVesselReportGenerator implements JRDataSource{

	ArrayList<TosRdsDataFinalMt> jrRobContainersDetail = new ArrayList<TosRdsDataFinalMt>();

	int index = -1;

	public RobContainersOnVesselReportGenerator(String Vesvoy) {

		List<?> robContainersList = NewReportVesselDao.getRobContainersOnVesselReportGeneratorList(Vesvoy);
				
		if ((robContainersList == null) || robContainersList.size() <= 0) { // no data 
			return;
		}
		for (int i = 0; i < robContainersList.size(); i++) {
			TosRdsDataFinalMt jrTosRdsFinalData = new TosRdsDataFinalMt();			
			String cn = ((TosRdsDataFinalMt)robContainersList.get(i)).getContainerNumber();
			String cd = ((TosRdsDataFinalMt)robContainersList.get(i)).getCheckDigit();
			cd = cd==null?"X":cd;
			cn = cn+cd;
			jrTosRdsFinalData.setContainerNumber(cn.replace("MATU", ""));
			jrTosRdsFinalData.setDir(((TosRdsDataFinalMt)robContainersList.get(i)).getDir());
			jrTosRdsFinalData.setDport(((TosRdsDataFinalMt)robContainersList.get(i)).getDport());
			jrTosRdsFinalData.setConsignee(((TosRdsDataFinalMt)robContainersList.get(i)).getConsignee());
			jrRobContainersDetail.add(jrTosRdsFinalData);					
		}  		
		Collections.sort(jrRobContainersDetail, new Comparator<TosRdsDataFinalMt>(){
		     public int compare(TosRdsDataFinalMt rds1, TosRdsDataFinalMt rds2){
		    	return rds1.getContainerNumber().compareTo(rds2.getContainerNumber());
		     }
		});
	}


	public Object getFieldValue(JRField jrField) throws JRException {
		TosRdsDataFinalMt robContainer = (TosRdsDataFinalMt) jrRobContainersDetail.get(index);
		if (robContainer  == null) {
			return null;
		}
		if (jrField.getName().equals("ContainerNumber")) {
			return robContainer.getContainerNumber();
		}
		if (jrField.getName().equals("Dir")) {
			return robContainer.getDir();
		}	
		if (jrField.getName().equals("DPort")) {
			return robContainer.getDport();
		}	
		if (jrField.getName().equals("Consignee")) {
			return robContainer.getConsignee();
		}	
		return null;
	}

	public boolean next() throws JRException {

		index++;
		if (jrRobContainersDetail != null && index < jrRobContainersDetail.size()) {
			return true;
		}
		return false;
	}

}

