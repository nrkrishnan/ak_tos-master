package com.matson.tos.reports;
import java.util.ArrayList;
import java.util.List;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import com.matson.cas.refdata.mapping.TosRdsDataFinalMt;
import com.matson.cas.refdata.mapping.TosRdsDataMt;
import com.matson.tos.dao.NewReportVesselDao;

/* This class gets the records from the supplemental  list for the Consignee Discrepancies Report and stores it in the list. 
 * 
 * 	Srno	Date			AuthorName				Change Description
 * 	A1		11/29/2012		Meena Kumari			Initial creation 
 */

public class SupConsigneeDiscrepReportGenerator implements JRDataSource{

	ArrayList<TosRdsDataFinalMt> jrConsigneeDiscrepDetail = new ArrayList<TosRdsDataFinalMt>();

	int index = -1;

	public SupConsigneeDiscrepReportGenerator( ArrayList<TosRdsDataFinalMt> rdsDataFinalListTDP, ArrayList<TosRdsDataFinalMt> rdsDataFinalListUpdate ) {
		
		List<TosRdsDataFinalMt>  lSupConsigneeDiscrepList = new ArrayList<TosRdsDataFinalMt>(); 
		if(rdsDataFinalListTDP != null && rdsDataFinalListUpdate != null){
		for (int i = 0; i < rdsDataFinalListTDP.size(); i++){
			TosRdsDataFinalMt rdsFinal =  rdsDataFinalListTDP.get(i);
			String ctrno = rdsFinal.getContainerNumber();
			String consignee = rdsFinal.getConsignee();
			int found = 0;

			for (int j = 0; j < rdsDataFinalListUpdate.size(); j++){
				TosRdsDataFinalMt rdsFinaltemp =  rdsDataFinalListUpdate.get(j);
				String ctrnotemp = rdsFinaltemp.getContainerNumber();
				String consigneetemp = rdsFinaltemp.getConsignee();
			
				if(ctrno.equals(ctrnotemp) && !consignee.equals(consigneetemp)){
					found++;
					lSupConsigneeDiscrepList.add(rdsFinaltemp);					
				} 
				if(found == 1) break;
			}
			if(found == 1){
				lSupConsigneeDiscrepList.add(rdsFinal);
			}
       
		}
							
		if ((lSupConsigneeDiscrepList == null) || (lSupConsigneeDiscrepList.size() <= 0)) { // no data 
			return;
		}

		for (int i = 0; i < lSupConsigneeDiscrepList.size(); i++) {
			TosRdsDataFinalMt jrTosRdsFinalData = new TosRdsDataFinalMt();
			String cn = ((TosRdsDataFinalMt)lSupConsigneeDiscrepList.get(i)).getContainerNumber();
			String cd = ((TosRdsDataFinalMt)lSupConsigneeDiscrepList.get(i)).getCheckDigit();
			cd = cd==null?"X":cd;
			cn = cn+cd;
			jrTosRdsFinalData.setContainerNumber(cn);	
			jrTosRdsFinalData.setDs(((TosRdsDataFinalMt)lSupConsigneeDiscrepList.get(i)).getDs());
			jrTosRdsFinalData.setDport(((TosRdsDataFinalMt)lSupConsigneeDiscrepList.get(i)).getDport());
			jrTosRdsFinalData.setCrstatus(((TosRdsDataFinalMt)lSupConsigneeDiscrepList.get(i)).getCrstatus());
			jrTosRdsFinalData.setConsignee(((TosRdsDataFinalMt)lSupConsigneeDiscrepList.get(i)).getConsignee());
		    String bookingNumber = ((TosRdsDataFinalMt)lSupConsigneeDiscrepList.get(i)).getBookingNumber();
		    if(bookingNumber != null){
		    	if (bookingNumber.equals("null"))
		    		bookingNumber = "";
		    }
			jrTosRdsFinalData.setBookingNumber(bookingNumber);
			jrConsigneeDiscrepDetail.add(jrTosRdsFinalData);					
		} 	
		}
		}
	
	
	public Object getFieldValue(JRField jrField) throws JRException {

		TosRdsDataFinalMt consigneeDiscrep = (TosRdsDataFinalMt) jrConsigneeDiscrepDetail .get(index);
		if (consigneeDiscrep == null) {
			return null;
		}
		if (jrField.getName().equals("Type")) {
			if(index%2 == 0)
			         return "SUP";
			else
				return "TDP";
		}	
		if (jrField.getName().equals("ContainerNumber")) {
			return consigneeDiscrep.getContainerNumber();
		}
		if (jrField.getName().equals("DS")) {
			return consigneeDiscrep.getDs();
		}	
		if (jrField.getName().equals("Dport")) {
			return consigneeDiscrep.getDport();
		}	
		/*if (jrField.getName().equals("Crstatus")) {
			return consigneeDiscrep.getCrstatus();
		}	*/
		if (jrField.getName().equals("Consignee")) {
			return consigneeDiscrep.getConsignee();
		}	
		if (jrField.getName().equals("BookingNumber")) {
			return consigneeDiscrep.getBookingNumber();
		}	
		return null;

	}

	public boolean next() throws JRException {

		index++;
		if (jrConsigneeDiscrepDetail  != null && index < jrConsigneeDiscrepDetail .size()) {
			return true;
		}
		return false;
	}

}

