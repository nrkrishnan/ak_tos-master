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
 * 	A1		03/10/2014		Azhar Naafiya						Initial creation 
 * 	
 */

public class HoldsReportGenerator implements JRDataSource{
	private static Logger logger = Logger.getLogger(HoldsReportGenerator.class);
	ArrayList<TosRdsDataFinalMt> jrholdDetail = new ArrayList<TosRdsDataFinalMt>();
	private static TosLookup lookUp=null;
	int index = -1;
	
	public HoldsReportGenerator(String Vesvoy) {
		List<?> holdList = NewReportVesselDao.getHoldsReportGeneratorList(Vesvoy);	
		
		if ((holdList == null ) || (holdList.size() <= 0)) { // no data 
			return;
		}
		for (int i = 0; i < holdList.size(); i++) {
			TosRdsDataFinalMt jrTosRdsFinalData = new TosRdsDataFinalMt();
			String cn = ((TosRdsDataFinalMt)holdList.get(i)).getContainerNumber();
			String cd = ((TosRdsDataFinalMt)holdList.get(i)).getCheckDigit();
			cd = cd==null?"X":cd;
			cn = cn+cd;
			jrTosRdsFinalData.setContainerNumber(cn);
			jrTosRdsFinalData.setBookingNumber(((TosRdsDataFinalMt)holdList.get(i)).getBookingNumber());
			jrTosRdsFinalData.setCrstatus(((TosRdsDataFinalMt)holdList.get(i)).getCrstatus().trim());
			jrTosRdsFinalData.setDport(((TosRdsDataFinalMt) holdList.get(i)).getDport());
			jrTosRdsFinalData.setLoadPort(((TosRdsDataFinalMt) holdList.get(i)).getLoadPort());
			jrTosRdsFinalData.setDischargePort(((TosRdsDataFinalMt) holdList.get(i)).getDischargePort());
			jrholdDetail.add(jrTosRdsFinalData );
		}
		
	}
public Object getFieldValue(JRField jrField) throws JRException {
		
		TosRdsDataFinalMt holdReport = (TosRdsDataFinalMt) jrholdDetail.get(index);
		if (holdReport == null) {
			return null;
		}
		if (jrField.getName().equals("ContainerNumber")) {
			return holdReport.getContainerNumber();
		}
		if (jrField.getName().equals("BookingNumber")) {
			return holdReport.getBookingNumber();
		}
		if (jrField.getName().equals("Holds")) {
			return holdReport.getCrstatus();
		}	
		if (jrField.getName().equals("LoadPort")) {
			return holdReport.getLoadPort();
		}	
		if (jrField.getName().equals("Dport")) {
			return holdReport.getDport();
		}	
		if (jrField.getName().equals("DischargePort")) {
			return holdReport.getDischargePort();
		}	
			
		return null;
		
	}
	public boolean next() throws JRException {
	
		index++;
		if (jrholdDetail != null && index < jrholdDetail.size()) {
			return true;
		}
		return false;
	}
}
