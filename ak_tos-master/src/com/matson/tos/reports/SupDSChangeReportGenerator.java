package com.matson.tos.reports;
import java.util.ArrayList;
import java.util.List;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import com.matson.cas.refdata.mapping.TosRdsDataFinalMt;
import com.matson.cas.refdata.mapping.TosRdsDataMt;
import com.matson.tos.dao.NewReportVesselDao;

/* This class gets the records from the supplemental  list for the DS Change Report and stores it in the list. 
 * 
 * 	Srno	Date			AuthorName				Change Description
 * 	A1		11/29/2012		Meena Kumari			Initial creation 
 */

public class SupDSChangeReportGenerator implements JRDataSource{

	ArrayList<TosRdsDataFinalMt> jrDSChangeDetail = new ArrayList<TosRdsDataFinalMt>();

	int index = -1;

	public SupDSChangeReportGenerator( ArrayList<TosRdsDataFinalMt> rdsDataFinalListTDP, ArrayList<TosRdsDataFinalMt> rdsDataFinalListUpdate ) {
		if(rdsDataFinalListTDP != null && rdsDataFinalListUpdate != null){
		List<TosRdsDataFinalMt>  lSupDSChangeList = new ArrayList<TosRdsDataFinalMt>(); 
		
		for (int i = 0; i < rdsDataFinalListTDP.size(); i++){
			TosRdsDataFinalMt rdsFinal =  rdsDataFinalListTDP.get(i);
			String ctrno = rdsFinal.getContainerNumber();
			String ds = rdsFinal.getDs();
			int found = 0;

			for (int j = 0; j < rdsDataFinalListUpdate.size(); j++){
				TosRdsDataFinalMt rdsFinaltemp =  rdsDataFinalListUpdate.get(j);
				String ctrnotemp = rdsFinaltemp.getContainerNumber();
				String dstemp = rdsFinaltemp.getDs();
			
				if(ctrno.equals(ctrnotemp) && !ds.equals(dstemp)){
					found++;
					lSupDSChangeList.add(rdsFinaltemp);
					break;
				}              
			}
			if(found == 1){
				lSupDSChangeList.add(rdsFinal);
			}
       
		}
							
		if ((lSupDSChangeList == null) || (lSupDSChangeList.size() <= 0)) { // no data 
			return;
		}

		for (int i = 0; i < lSupDSChangeList.size(); i++) {
			TosRdsDataFinalMt jrTosRdsFinalData = new TosRdsDataFinalMt();
			String cn = ((TosRdsDataFinalMt)lSupDSChangeList.get(i)).getContainerNumber();
			String cd = ((TosRdsDataFinalMt)lSupDSChangeList.get(i)).getCheckDigit();
			cd = cd==null?"X":cd;
			cn = cn+cd;
			jrTosRdsFinalData.setContainerNumber(cn);
			jrTosRdsFinalData.setDs(((TosRdsDataFinalMt)lSupDSChangeList.get(i)).getDs());
			jrTosRdsFinalData.setDport(((TosRdsDataFinalMt)lSupDSChangeList.get(i)).getDport());
			jrTosRdsFinalData.setCrstatus(((TosRdsDataFinalMt)lSupDSChangeList.get(i)).getCrstatus());
			jrTosRdsFinalData.setConsignee(((TosRdsDataFinalMt)lSupDSChangeList.get(i)).getConsignee());
		    String bookingNumber = ((TosRdsDataFinalMt)lSupDSChangeList.get(i)).getBookingNumber();
		    if(bookingNumber != null){
		    	if (bookingNumber.equals("null"))
		    		bookingNumber = "";
		    }
			jrTosRdsFinalData.setBookingNumber(bookingNumber);
			jrDSChangeDetail.add(jrTosRdsFinalData);					
		} 
		}
		}
	
	
	public Object getFieldValue(JRField jrField) throws JRException {

		TosRdsDataFinalMt dsChange = (TosRdsDataFinalMt) jrDSChangeDetail .get(index);
		if (dsChange == null) {
			return null;
		}
		if (jrField.getName().equals("Type")) {
			if(index%2 == 0)
			         return "SUP";
			else
				return "TDP";
		}	
		if (jrField.getName().equals("ContainerNumber")) {
			return dsChange.getContainerNumber();
		}
		if (jrField.getName().equals("DS")) {
			return dsChange.getDs();
		}	
		if (jrField.getName().equals("Dport")) {
			return dsChange.getDport();
		}	
		if (jrField.getName().equals("Crstatus")) {
			return dsChange.getCrstatus();
		}	
		if (jrField.getName().equals("Consignee")) {
			return dsChange.getConsignee();
		}	
		if (jrField.getName().equals("BookingNumber")) {
			return dsChange.getBookingNumber();
		}	
		return null;

	}

	public boolean next() throws JRException {

		index++;
		if (jrDSChangeDetail  != null && index < jrDSChangeDetail .size()) {
			return true;
		}
		return false;
	}

}

