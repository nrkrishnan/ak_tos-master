package com.matson.tos.reports;
import java.util.ArrayList;
import java.util.List;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import com.matson.cas.refdata.mapping.TosRdsDataFinalMt;
import com.matson.tos.dao.NewReportVesselDao;

/* This class gets the records from DB for the SF Tag  Report and stores it in the list. 
 * 
 * 	Srno	Date			AuthorName				Change Description
 * 	A1		10/11/2012		Meena Kumari			Initial creation 
 */

public class SFTagReportGenerator implements JRDataSource{

	ArrayList<TosRdsDataFinalMt> jrsfTagDetail = new ArrayList<TosRdsDataFinalMt>();

	int index = -1;

	public SFTagReportGenerator(String Vesvoy) {

		List<?> sfTagList = NewReportVesselDao.getSFTagReportGeneratorList(Vesvoy);
		//No duplication is possible		
		if ( (sfTagList == null) || sfTagList.size() <= 0) { // no data 
			return;
		}
		for (int i = 0; i <  sfTagList.size(); i++) {
			TosRdsDataFinalMt jrTosRdsFinalData = new TosRdsDataFinalMt();			
			String cn = ((TosRdsDataFinalMt)sfTagList.get(i)).getContainerNumber();
			String cd = ((TosRdsDataFinalMt)sfTagList.get(i)).getCheckDigit();
			cd = cd==null?"X":cd;
			cn = cn+cd;
			jrTosRdsFinalData.setContainerNumber(cn);
			jrTosRdsFinalData.setDport(((TosRdsDataFinalMt) sfTagList.get(i)).getDport());
			jrTosRdsFinalData.setCrstatus(((TosRdsDataFinalMt) sfTagList.get(i)).getCrstatus());
			jrTosRdsFinalData.setTemp(((TosRdsDataFinalMt) sfTagList.get(i)).getTemp());
			jrTosRdsFinalData.setCargoNotes(((TosRdsDataFinalMt) sfTagList.get(i)).getCargoNotes());
			jrTosRdsFinalData.setCweight(((TosRdsDataFinalMt) sfTagList.get(i)).getCweight());
			jrTosRdsFinalData.setTypeCode(((TosRdsDataFinalMt) sfTagList.get(i)).getTypeCode());
			jrTosRdsFinalData.setShipper(((TosRdsDataFinalMt) sfTagList.get(i)).getShipper());
			jrTosRdsFinalData.setBookingNumber(((TosRdsDataFinalMt) sfTagList.get(i)).getBookingNumber());
			jrsfTagDetail.add(jrTosRdsFinalData );					
		}  		

	}

	public Object getFieldValue(JRField jrField) throws JRException {
		
		TosRdsDataFinalMt  sfTag = (TosRdsDataFinalMt) jrsfTagDetail.get(index);
		if (sfTag == null) {
			return null;
		}
		if (jrField.getName().equals("ContainerNumber")) {
			return sfTag.getContainerNumber();
		}
		if (jrField.getName().equals("Dport")) {
			return sfTag.getDport();
		}
		if (jrField.getName().equals("Crstatus")) {
			return sfTag.getCrstatus();
		}
		if (jrField.getName().equals("Temp")) {
			return sfTag.getTemp();
		}
		if (jrField.getName().equals("CargoNotes")) {
			return sfTag.getCargoNotes();
		}
		if (jrField.getName().equals("Weight")) {
			return sfTag.getCweight();
		}
		if (jrField.getName().equals("Type")) {
			return sfTag.getTypeCode();
		}
		if (jrField.getName().equals("Shipper")) {
			return sfTag.getShipper();
		}
		if (jrField.getName().equals("Booking")) {
			return sfTag.getBookingNumber();
		}
		return null;
		
	}

	public boolean next() throws JRException {
		
		index++;
		if (jrsfTagDetail != null && index < jrsfTagDetail.size()) {
			return true;
		}
		return false;
	}

}

