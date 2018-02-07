package com.matson.tos.reports;
import java.util.ArrayList;
import java.util.List;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import com.matson.cas.refdata.mapping.TosRdsDataFinalMt;
import com.matson.cas.refdata.mapping.TosRdsDataMt;
import com.matson.tos.dao.NewReportVesselDao;

/* This class gets the records from the supplemental  list for the Blank Consignee CS Report and stores it in the list. 
 * 
 * 	Srno	Date			AuthorName				Change Description
 * 	A1		11/29/2012		Meena Kumari			Initial creation 
 */

public class SupBlankConsigneeCSReportGenerator implements JRDataSource{

	ArrayList<TosRdsDataFinalMt> jrBlankConsigneeDetail = new ArrayList<TosRdsDataFinalMt>();

	int index = -1;

	public SupBlankConsigneeCSReportGenerator( ArrayList<TosRdsDataFinalMt> rdsDataFinalListUpdate ) {

		//"FROM TosRdsDataFinalMt WHERE vesvoy = '" + Vesvoy + "' 
		//AND (consignee LIKE 'REQUIRES CS ACTION%'  OR Consignee LIKE 'UNAPPROVED VARIANCE%') 
		//AND (typeCode NOT LIKE '%GR' AND typeCode NOT LIKE '%GB' AND typeCode NOT LIKE '%GE')")
		if(rdsDataFinalListUpdate != null){
		List<TosRdsDataFinalMt>  lBlankConsigneeList = new ArrayList<TosRdsDataFinalMt>(); 
		
		for (int i = 0; i < rdsDataFinalListUpdate.size(); i++){
			TosRdsDataFinalMt rdsF = rdsDataFinalListUpdate.get(i);
			String consignee = rdsF.getConsignee();
			String typeCode = rdsF.getTypeCode();
			if((consignee.equals("REQUIRES CS ACTION") || consignee.equals("UNAPPROVED VARIANCE") )&& ((!typeCode.contains("GR") && !typeCode.contains("GB")) && !typeCode.contains("GE"))){
				lBlankConsigneeList.add(rdsF);
			}
		 }
							
		if ((lBlankConsigneeList == null) || (lBlankConsigneeList.size() <= 0)) { // no data 
			return;
		}

		for (int i = 0; i < lBlankConsigneeList.size(); i++) {
			TosRdsDataFinalMt jrTosRdsFinalData = new TosRdsDataFinalMt();	
			jrTosRdsFinalData.setLastUpdateDate(((TosRdsDataFinalMt)lBlankConsigneeList.get(i)).getLastUpdateDate());
			String cn = ((TosRdsDataFinalMt)lBlankConsigneeList.get(i)).getContainerNumber();
			String cd = ((TosRdsDataFinalMt)lBlankConsigneeList.get(i)).getCheckDigit();
			cd = cd==null?"X":cd;
			cn = cn+cd;
			jrTosRdsFinalData.setContainerNumber(cn);
			jrTosRdsFinalData.setDs(((TosRdsDataFinalMt)lBlankConsigneeList.get(i)).getDs());
			jrTosRdsFinalData.setDport(((TosRdsDataFinalMt)lBlankConsigneeList.get(i)).getDport());
			jrTosRdsFinalData.setCrstatus(((TosRdsDataFinalMt)lBlankConsigneeList.get(i)).getCrstatus());
			jrTosRdsFinalData.setConsignee(((TosRdsDataFinalMt)lBlankConsigneeList.get(i)).getConsignee());
		    String bookingNumber = ((TosRdsDataFinalMt)lBlankConsigneeList.get(i)).getBookingNumber();
		    if(bookingNumber != null){
		    	if (bookingNumber.equals("null"))
		    		bookingNumber = "";
		    }
			jrTosRdsFinalData.setBookingNumber(bookingNumber);
			jrBlankConsigneeDetail.add(jrTosRdsFinalData);					
		} 	
		}
	}
	
	public Object getFieldValue(JRField jrField) throws JRException {

		TosRdsDataFinalMt blankConsignee = (TosRdsDataFinalMt) jrBlankConsigneeDetail .get(index);
		if (blankConsignee == null) {
			return null;
		}
		if (jrField.getName().equals("Sup Date")) {
			return blankConsignee.getLastUpdateDate();
		}	
		if (jrField.getName().equals("ContainerNumber")) {
			return blankConsignee.getContainerNumber();
		}
		if (jrField.getName().equals("DS")) {
			return blankConsignee.getDs();
		}	
		if (jrField.getName().equals("DPort")) {
			return blankConsignee.getDport();
		}	
		if (jrField.getName().equals("Crstatus")) {
			return blankConsignee.getCrstatus();
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

