package com.matson.tos.reports;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.hibernate.Query;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import com.matson.cas.refdata.mapping.TosRdsDataFinalMt;
import com.matson.tos.dao.NewReportVesselDao;

/* This class gets the records from DB for the MTY Container Report and stores it in the list. 
 * 
 * 	Srno	Date			AuthorName				Change Description
 * 	A1		10/8/2012		Meena Kumari			Initial creation 
 * 	A2		04/12/2013		Karthik Rajendran		Added: Sorting on Container number
 * 
 */

public class MTYContainerReportGenerator implements JRDataSource{

	ArrayList<TosRdsDataFinalMt> jrMtyCtrDetail = new ArrayList<TosRdsDataFinalMt>();

	int index = -1;

	public MTYContainerReportGenerator(String Vesvoy) {

		List<?> mtyCtrList = NewReportVesselDao.getMTYContainerReportGeneratorList(Vesvoy);
		
		if ((mtyCtrList == null ) || (mtyCtrList.size() <= 0)) { // no data 
			return;
		}
		for (int i = 0; i < mtyCtrList.size(); i++) {
			TosRdsDataFinalMt jrTosRdsFinalData = new TosRdsDataFinalMt();			
			String cn = ((TosRdsDataFinalMt)mtyCtrList.get(i)).getContainerNumber();
			String cd = ((TosRdsDataFinalMt)mtyCtrList.get(i)).getCheckDigit();
			cd = cd==null?"X":cd;
			cn = cn+cd;
			jrTosRdsFinalData.setContainerNumber(cn.replace("MATU", ""));				
			jrMtyCtrDetail.add(jrTosRdsFinalData );					
		}  		
		Collections.sort(jrMtyCtrDetail, new Comparator<TosRdsDataFinalMt>(){
		     public int compare(TosRdsDataFinalMt rds1, TosRdsDataFinalMt rds2){
		    	return rds1.getContainerNumber().compareTo(rds2.getContainerNumber());
		     }
		});

	}

	public Object getFieldValue(JRField jrField) throws JRException {
		
		TosRdsDataFinalMt mtyCtr = (TosRdsDataFinalMt) jrMtyCtrDetail.get(index);
		if (mtyCtr == null) {
			return null;
		}
		if (jrField.getName().equals("ContainerNumber")) {
			return mtyCtr.getContainerNumber();
		}
		return null;
		
	}

	public boolean next() throws JRException {
		
		index++;
		if (jrMtyCtrDetail != null && index < jrMtyCtrDetail.size()) {
			return true;
		}
		return false;
	}

}

