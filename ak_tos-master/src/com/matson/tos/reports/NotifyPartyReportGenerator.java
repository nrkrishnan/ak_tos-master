package com.matson.tos.reports;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import com.matson.cas.refdata.mapping.TosRdsDataFinalMt;
import com.matson.tos.dao.NewReportVesselDao;
import com.matson.tos.dao.TosLookup;

/* This class gets the records from DB for the Destination Ports Discrepancies Report and stores it in the list. 
 * 
 * 	Srno	Date			AuthorName				Change Description
 * 	A1		03/11/2014		Azhar Naafiya						Initial creation 
 * 	
 */
public class NotifyPartyReportGenerator implements JRDataSource{
	ArrayList<TosRdsDataFinalMt> jrnotifyDetail = new ArrayList<TosRdsDataFinalMt>();
	int index = -1;
	
	public NotifyPartyReportGenerator(String Vesvoy) {
		List<?> notifyList = NewReportVesselDao.getNotifyPartyReportGeneratorList(Vesvoy);	
		
		if ((notifyList == null ) || (notifyList.size() <= 0)) { // no data 
			return;
		}
		for (int i = 0; i < notifyList.size(); i++) {
			TosRdsDataFinalMt jrTosRdsFinalData = new TosRdsDataFinalMt();
			String cn = ((TosRdsDataFinalMt)notifyList.get(i)).getContainerNumber();
			String cd = ((TosRdsDataFinalMt)notifyList.get(i)).getCheckDigit();
			cd = cd==null?"X":cd;
			cn = cn+cd;
			jrTosRdsFinalData.setContainerNumber(cn);
			jrTosRdsFinalData.setConsignee(((TosRdsDataFinalMt) notifyList.get(i)).getConsignee());
			jrTosRdsFinalData.setShipper(((TosRdsDataFinalMt) notifyList.get(i)).getShipper());
			String cargoNotes = ((TosRdsDataFinalMt)notifyList.get(i)).getCargoNotes();
			if(cargoNotes != null){
			int indexnp = cargoNotes.indexOf("N/P");
			String notParty = cargoNotes.substring(indexnp+4,indexnp+8).replace(",", "");
			jrTosRdsFinalData.setTruck(notParty);
			}
			jrTosRdsFinalData.setDport(((TosRdsDataFinalMt) notifyList.get(i)).getDport());
			jrnotifyDetail.add(jrTosRdsFinalData );
		}
	}
		

	public Object getFieldValue(JRField jrField) throws JRException {
		
		TosRdsDataFinalMt notifyReport = (TosRdsDataFinalMt) jrnotifyDetail.get(index);
		if (notifyReport == null) {
			return null;
		}
		if (jrField.getName().equals("ContainerNumber")) {
			return notifyReport.getContainerNumber();
		}
		if (jrField.getName().equals("Consignee")) {
			return notifyReport.getConsignee();
		}
		if (jrField.getName().equals("Shipper")) {
			return notifyReport.getShipper();
		}	
		if (jrField.getName().equals("Truck")) {
			return notifyReport.getTruck();
		}	
		if (jrField.getName().equals("Dport")) {
			return notifyReport.getDport();
		}	
			
		return null;
		
	}
	public boolean next() throws JRException {
		
		index++;
		if (jrnotifyDetail != null && index < jrnotifyDetail.size()) {
			return true;
		}
		return false;
	}
}