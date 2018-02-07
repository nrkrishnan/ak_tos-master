package com.matson.tos.reports;
import java.util.ArrayList;
import java.util.List;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import com.matson.cas.refdata.mapping.TosRdsDataFinalMt;
import com.matson.tos.dao.NewReportVesselDao;

/* This class gets the records from DB for the Paradise Beverages Containers Report and stores it in the list. 
 * 
 * 	Srno	Date			AuthorName				Change Description
 * 	A1		10/15/2012		Meena Kumari			Initial creation 
 */

public class ParadiseBeveragesContainersReportGenerator implements JRDataSource{

	ArrayList<TosRdsDataFinalMt> jrparadiseBeveragesCtrsDetail = new ArrayList<TosRdsDataFinalMt>();

	int index = -1;

	public ParadiseBeveragesContainersReportGenerator(String Vesvoy) {

		List<?> paradiseBeveragesCtrsList = NewReportVesselDao.getParadiseBeveragesCtrsList(Vesvoy);				
				
		if ( (paradiseBeveragesCtrsList == null ) || (paradiseBeveragesCtrsList.size() <= 0)) { // no data 
			return;
		}
		for (int i = 0; i <  paradiseBeveragesCtrsList.size(); i++) {
			TosRdsDataFinalMt jrTosRdsFinalData = new TosRdsDataFinalMt();			
			String cn = ((TosRdsDataFinalMt)paradiseBeveragesCtrsList.get(i)).getContainerNumber();
			String cd = ((TosRdsDataFinalMt)paradiseBeveragesCtrsList.get(i)).getCheckDigit();
			cd = cd==null?"X":cd;
			cn = cn+cd;
			jrTosRdsFinalData.setContainerNumber(cn);
			jrTosRdsFinalData.setDport(((TosRdsDataFinalMt) paradiseBeveragesCtrsList.get(i)).getDport());
			jrTosRdsFinalData.setLoadPort(((TosRdsDataFinalMt) paradiseBeveragesCtrsList.get(i)).getLoadPort());
			jrTosRdsFinalData.setShipper(((TosRdsDataFinalMt) paradiseBeveragesCtrsList.get(i)).getShipper());
			jrTosRdsFinalData.setBookingNumber(((TosRdsDataFinalMt)paradiseBeveragesCtrsList.get(i)).getBookingNumber());
			jrparadiseBeveragesCtrsDetail.add(jrTosRdsFinalData );					
		}  		

	}

	public Object getFieldValue(JRField jrField) throws JRException {
		
		TosRdsDataFinalMt  paradiseBeverages = (TosRdsDataFinalMt) jrparadiseBeveragesCtrsDetail.get(index);
		if (paradiseBeverages == null) {
			return null;
		}
		if (jrField.getName().equals("ContainerNumber")) {
			return paradiseBeverages.getContainerNumber();
		}
		if (jrField.getName().equals("Dport")) {
			return paradiseBeverages.getDport();
		}	
		if (jrField.getName().equals("LoadPort")) {
			return paradiseBeverages.getLoadPort();
		}
		if (jrField.getName().equals("Shipper")) {
			return paradiseBeverages.getShipper();
		}
		if (jrField.getName().equals("Booking")) {
			return paradiseBeverages.getBookingNumber();
		}
		return null;
		
	}

	public boolean next() throws JRException {
		
		index++;
		if (jrparadiseBeveragesCtrsDetail != null && index < jrparadiseBeveragesCtrsDetail.size()) {
			return true;
		}
		return false;
	}

}

