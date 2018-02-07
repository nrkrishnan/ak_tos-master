package com.matson.tos.reports;
import java.util.ArrayList;
import java.util.List;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import com.matson.cas.refdata.mapping.TosRdsDataFinalMt;
import com.matson.cas.refdata.mapping.TosRdsDataMt;
import com.matson.tos.dao.NewReportVesselDao;

/* This class gets the records from the supplemental  list for the DPort discrepansies Report and stores it in the list. 
 * 
 * 	Srno	Date			AuthorName				Change Description
 * 	A1		11/29/2012		Meena Kumari			Initial creation 
 */

public class SupDPortDiscrepReportGenerator implements JRDataSource{

	ArrayList<TosRdsDataFinalMt> jrDPortChangeDetail = new ArrayList<TosRdsDataFinalMt>();

	int index = -1;

	public SupDPortDiscrepReportGenerator( ArrayList<TosRdsDataFinalMt> rdsDataFinalListTDP, ArrayList<TosRdsDataFinalMt> rdsDataFinalListUpdate ) {
		if(rdsDataFinalListTDP != null && rdsDataFinalListUpdate != null){
		List<TosRdsDataFinalMt>  lSupDPortChangeList = new ArrayList<TosRdsDataFinalMt>(); 
		
		for (int i = 0; i < rdsDataFinalListTDP.size(); i++){
			TosRdsDataFinalMt rdsFinal =  rdsDataFinalListTDP.get(i);
			String ctrno = rdsFinal.getContainerNumber();
			String dport= rdsFinal.getDport();
			int found = 0;

			for (int j = 0; j < rdsDataFinalListUpdate.size(); j++){
				TosRdsDataFinalMt rdsFinaltemp =  rdsDataFinalListUpdate.get(j);
				String ctrnotemp = rdsFinaltemp.getContainerNumber();
				String dporttemp = rdsFinaltemp.getDport();
			
				if(ctrno.equals(ctrnotemp) && !dport.equals(dporttemp)){
					found++;
					lSupDPortChangeList.add(rdsFinaltemp);
					break;
				}              
			}
			if(found == 1){
				lSupDPortChangeList.add(rdsFinal);
			}
       
		}
							
		if ((lSupDPortChangeList == null) || (lSupDPortChangeList.size() <= 0)) { // no data 
			return;
		}

		for (int i = 0; i < lSupDPortChangeList.size(); i++) {
			TosRdsDataFinalMt jrTosRdsFinalData = new TosRdsDataFinalMt();
			String cn = ((TosRdsDataFinalMt)lSupDPortChangeList.get(i)).getContainerNumber();
			String cd = ((TosRdsDataFinalMt)lSupDPortChangeList.get(i)).getCheckDigit();
			cd = cd==null?"X":cd;
			cn = cn+cd;
			jrTosRdsFinalData.setContainerNumber(cn);
			jrTosRdsFinalData.setDs(((TosRdsDataFinalMt)lSupDPortChangeList.get(i)).getDs());
			jrTosRdsFinalData.setDport(((TosRdsDataFinalMt)lSupDPortChangeList.get(i)).getDport());
			jrTosRdsFinalData.setCrstatus(((TosRdsDataFinalMt)lSupDPortChangeList.get(i)).getCrstatus());
			jrTosRdsFinalData.setConsignee(((TosRdsDataFinalMt)lSupDPortChangeList.get(i)).getConsignee());
		    String bookingNumber = ((TosRdsDataFinalMt)lSupDPortChangeList.get(i)).getBookingNumber();
		    if(bookingNumber != null){
		    	if (bookingNumber.equals("null"))
		    		bookingNumber = "";
		    }
			jrTosRdsFinalData.setBookingNumber(bookingNumber);
			jrDPortChangeDetail.add(jrTosRdsFinalData);					
		} 
		}
		}
	
	
	public Object getFieldValue(JRField jrField) throws JRException {

		TosRdsDataFinalMt dportChange = (TosRdsDataFinalMt) jrDPortChangeDetail .get(index);
		if (dportChange == null) {
			return null;
		}
		if (jrField.getName().equals("Type")) {
			if(index%2 == 0)
			         return "SUP";
			else
				return "TDP";
		}	
		if (jrField.getName().equals("ContainerNumber")) {
			return dportChange.getContainerNumber();
		}
		if (jrField.getName().equals("DS")) {
			return dportChange.getDs();
		}	
		if (jrField.getName().equals("Dport")) {
			return dportChange.getDport();
		}	
		if (jrField.getName().equals("Crstatus")) {
			return dportChange.getCrstatus();
		}	
		if (jrField.getName().equals("Consignee")) {
			return dportChange.getConsignee();
		}	
		if (jrField.getName().equals("BookingNumber")) {
			return dportChange.getBookingNumber();
		}	
		return null;

	}

	public boolean next() throws JRException {

		index++;
		if (jrDPortChangeDetail  != null && index < jrDPortChangeDetail .size()) {
			return true;
		}
		return false;
	}

}

