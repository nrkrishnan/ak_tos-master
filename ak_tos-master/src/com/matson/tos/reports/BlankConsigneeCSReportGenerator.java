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

/* This class gets the records from DB for the Blank Consignee CS Report and stores it in the list. 
 * 
 * 	Srno	Date			AuthorName				Change Description
 * 	A1		10/8/2012		Meena Kumari			Initial creation 
 * 	A2		04/12/2013		Karthik Rajendran		Added: Sorting on Container number
 */

public class BlankConsigneeCSReportGenerator implements JRDataSource{

	ArrayList<TosRdsDataFinalMt> jrBlankConsigneeDetail = new ArrayList<TosRdsDataFinalMt>();

	int index = -1;

	public BlankConsigneeCSReportGenerator(String Vesvoy) {

		//doProcess(Vesvoy); //commented as this piece of code is moved outside from this file.

		List<?> lBlankConsigneeList = NewReportVesselDao.getBlankConsigneeCSReportGeneratorList(Vesvoy);
				
		if ((lBlankConsigneeList == null) || (lBlankConsigneeList.size() <= 0)) { // no data 
			return;
		}

		for (int i = 0; i < lBlankConsigneeList.size(); i++) {
			TosRdsDataFinalMt jrTosRdsFinalData = new TosRdsDataFinalMt();			
			String cn = ((TosRdsDataFinalMt)lBlankConsigneeList.get(i)).getContainerNumber();
			String cd = ((TosRdsDataFinalMt)lBlankConsigneeList.get(i)).getCheckDigit();
			cd = cd==null?"X":cd;
			cn = cn+cd;
			jrTosRdsFinalData.setContainerNumber(cn.replace("MATU", ""));		
			jrTosRdsFinalData.setDport(((TosRdsDataFinalMt)lBlankConsigneeList.get(i)).getDport());
			jrTosRdsFinalData.setConsignee(((TosRdsDataFinalMt)lBlankConsigneeList.get(i)).getConsignee());
			String bookingNumber = ((TosRdsDataFinalMt)lBlankConsigneeList.get(i)).getBookingNumber();
			bookingNumber = bookingNumber==null?"":bookingNumber;
			bookingNumber = bookingNumber.replace("null", "");
		    jrTosRdsFinalData.setBookingNumber(bookingNumber);
			jrBlankConsigneeDetail.add(jrTosRdsFinalData);					
		}
		Collections.sort(jrBlankConsigneeDetail, new Comparator<TosRdsDataFinalMt>(){
		     public int compare(TosRdsDataFinalMt rds1, TosRdsDataFinalMt rds2){
		    	return rds1.getContainerNumber().compareTo(rds2.getContainerNumber());
		     }
		});
	}
	
	/*public void doProcess(String Vesvoy){		
	
	List<?> blankConsigneeList = NewReportVesselDao.getBlankConsigneeList(Vesvoy);

	if ((blankConsigneeList == null) || (blankConsigneeList.size() <= 0)) { // no data 
		return;
	}
	for (int i = 0; i < blankConsigneeList.size(); i++) {
		TosRdsDataFinalMt lTosRdsFinalData = (TosRdsDataFinalMt) blankConsigneeList.get(i);
		if (lTosRdsFinalData.getConsignee() != null  && !lTosRdsFinalData.getConsignee().trim().equals("")){
			lTosRdsFinalData.setConsignee("REQUIRES CS ACTION - " + lTosRdsFinalData.getConsignee());			
		}else{
			lTosRdsFinalData.setConsignee("REQUIRES CS ACTION");
		}

		NewReportVesselDao.saveOrUpdateTosRdsDatFinalMt(lTosRdsFinalData);		
	    }
	}  */

	public Object getFieldValue(JRField jrField) throws JRException {

		TosRdsDataFinalMt blankConsignee = (TosRdsDataFinalMt) jrBlankConsigneeDetail .get(index);
		if (blankConsignee == null) {
			return null;
		}
		if (jrField.getName().equals("ContainerNumber")) {
			return blankConsignee.getContainerNumber();
		}	
		if (jrField.getName().equals("DPort")) {
			return blankConsignee.getDport();
		}	
		if (jrField.getName().equals("Consignee")) {
			return blankConsignee.getConsignee();
		}	
		if (jrField.getName().equals("BookingNumber")) {
			return blankConsignee.getBookingNumber();
		}	
		return null;

	}

	public boolean next() throws JRException {

		index++;
		if (jrBlankConsigneeDetail  != null && index < jrBlankConsigneeDetail .size()) {
			return true;
		}
		return false;
	}

}

