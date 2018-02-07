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

/* This class gets the records from DB for the Mix Port Container Report and stores it in the list. 
 * 
 * 	Srno	Date			AuthorName				Change Description
 * 	A1		10/8/2012		Meena Kumari			Initial creation 
 *  A2		04/12/2013		Karthik Rajendran		Added: Sorting on Container number
 *  
 */

public class MixPortContainerReportGenerator implements JRDataSource{

	ArrayList<TosRdsDataFinalMt> jrMixPortCtrDetail = new ArrayList<TosRdsDataFinalMt>();

	int index = -1;

	public MixPortContainerReportGenerator(String Vesvoy) {
		
		List<?> mixPortCtrList =  NewReportVesselDao.getMixPortContainerReportGeneratorList(Vesvoy);
	
	
		
		if ((mixPortCtrList == null) || (mixPortCtrList.size() <= 0)) { // no data 
			return;
		}
		for (int i = 0; i < mixPortCtrList.size(); i++) {
			TosRdsDataFinalMt jrTosRdsFinalData = new TosRdsDataFinalMt();			
			String cn = ((TosRdsDataFinalMt)mixPortCtrList.get(i)).getContainerNumber();
			String cd = ((TosRdsDataFinalMt)mixPortCtrList.get(i)).getCheckDigit();
			cd = cd==null?"X":cd;
			cn = cn+cd;
			jrTosRdsFinalData.setContainerNumber(cn.replace("MATU", ""));
			jrTosRdsFinalData.setDir(((TosRdsDataFinalMt)mixPortCtrList.get(i)).getDir());
			jrTosRdsFinalData.setDport(((TosRdsDataFinalMt)mixPortCtrList.get(i)).getDport());
			jrTosRdsFinalData.setConsignee(((TosRdsDataFinalMt)mixPortCtrList.get(i)).getConsignee());
			jrMixPortCtrDetail.add(jrTosRdsFinalData);					
		}
 		
		Collections.sort(jrMixPortCtrDetail, new Comparator<TosRdsDataFinalMt>(){
		     public int compare(TosRdsDataFinalMt rds1, TosRdsDataFinalMt rds2){
		    	return rds1.getContainerNumber().compareTo(rds2.getContainerNumber());
		     }
		});
	}

	public Object getFieldValue(JRField jrField) throws JRException {
		
		TosRdsDataFinalMt mixPortCtr = (TosRdsDataFinalMt) jrMixPortCtrDetail.get(index);
		if (mixPortCtr == null) {
			return null;
		}
		if (jrField.getName().equals("ContainerNumber")) {
			return mixPortCtr.getContainerNumber();
		}
		if (jrField.getName().equals("Dir")) {
			return mixPortCtr.getDir();
		}	
		if (jrField.getName().equals("DPort")) {
			return mixPortCtr.getDport();
		}	
		if (jrField.getName().equals("Consignee")) {
			return mixPortCtr.getConsignee();
		}	
		return null;
		
	}

	public boolean next() throws JRException {
		
		index++;
		if (jrMixPortCtrDetail != null && index < jrMixPortCtrDetail.size()) {
			return true;
		}
		return false;
	}
}

