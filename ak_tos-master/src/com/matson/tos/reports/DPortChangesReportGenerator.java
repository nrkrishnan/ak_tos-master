package com.matson.tos.reports;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import com.matson.cas.refdata.mapping.TosRdsDataFinalMt;
import com.matson.cas.refdata.mapping.TosStowPlanCntrMt;
import com.matson.tos.dao.NewReportVesselDao;

/* This class gets the records from DB for the Destination Ports Discrepancies Report and stores it in the list. 
 * 
 * 	Srno	Date			AuthorName				Change Description
 * 	A1		10/16/2012		Meena Kumari			Initial creation 
 * 	A2		04/08/2013		Karthik Rajendran		Added: Sorting on Cell
 */

public class DPortChangesReportGenerator implements JRDataSource {

	ArrayList<TosRdsDataFinalMt> jrDportChangesDetail = new ArrayList<TosRdsDataFinalMt>();

	int index = -1;

	public DPortChangesReportGenerator(String Vesvoy) {

		List results = NewReportVesselDao.getDPortChangesList(Vesvoy);
		if ((results == null) || (results.size() <= 0)) { // no data
			return;
		}
		System.out.println("DPortChangesReportGenerator ****** result size:"+results.size());
		for (ListIterator<?> iter = results.listIterator(); iter.hasNext();) {
			Object[] row = (Object[]) iter.next();
			TosRdsDataFinalMt temp = new TosRdsDataFinalMt();
			System.out.println((String) row[0]+"\t"+(String) row[1]+"\t"+(String) row[2]+"\t"+(String) row[3]+"\t"+(String) row[4]);
			temp.setContainerNumber((String) row[0]);
			temp.setDischargePort((String) row[1]);// Dport N 
			temp.setDport((String) row[2]); // Dport O 
			temp.setConsignee((String) row[3]);
			temp.setCell((String) row[4]);
			jrDportChangesDetail.add(temp);
		}
		Collections.sort(jrDportChangesDetail, new Comparator<TosRdsDataFinalMt>(){
		     public int compare(TosRdsDataFinalMt rds1, TosRdsDataFinalMt rds2){
		    	return rds1.getCell().compareTo(rds2.getCell());
		     }
		});
	}

	public Object getFieldValue(JRField jrField) throws JRException {

		TosRdsDataFinalMt dportChanges = (TosRdsDataFinalMt) jrDportChangesDetail
				.get(index);
		if (dportChanges == null) {
			return null;
		}
		if (jrField.getName().equals("ContainerNumber")) {
			return dportChanges.getContainerNumber();
		}
		if (jrField.getName().equals("DPortO")) {
			return dportChanges.getDport();
		}
		if (jrField.getName().equals("DPortN")) {
			return dportChanges.getDischargePort();// //This field is used to
													// store DPort N
		}
		if (jrField.getName().equals("Consignee")) {
			return dportChanges.getConsignee();
		}
		if (jrField.getName().equals("Cell")) {
			return dportChanges.getCell();
		}
		return null;

	}

	public boolean next() throws JRException {

		index++;
		if (jrDportChangesDetail != null && index < jrDportChangesDetail.size()) {
			return true;
		}
		return false;
	}
}
