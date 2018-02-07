package com.matson.tos.reports.barge;

import java.util.ArrayList;
import java.util.List;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import com.matson.tos.dao.NewReportVesselDao;
import com.matson.tos.exception.NLTErrors;

/* This class gets the records from DB for the MTY Container Report and stores it in the list. 
 * 
 * 	Srno	Date			AuthorName				Change Description
 * 	A1		10/8/2012		Meena Kumari			Initial creation 
 */

public class NLTDiscrepanciesReportGenerator implements JRDataSource {

	int index = -1;
	ArrayList<NLTErrors> errCtrList = new ArrayList<NLTErrors>();

	public NLTDiscrepanciesReportGenerator(String Vesvoy) {
		errCtrList = NewReportVesselDao.getNLTDiscrepanciesBargeReportGeneratorList(Vesvoy);
		if ((errCtrList == null) || (errCtrList.size() <= 0)) { // no data
			return;
		}
	}

	public Object getFieldValue(JRField jrField) throws JRException {
		NLTErrors errCtr = errCtrList.get(index);
		if (errCtr == null) {
			return null;
		}
		if (jrField.getName().equals("ContainerNumber")) {
			return errCtr.getContainerNumber();
		}
		if (jrField.getName().equals("Cell")) {
			return errCtr.getCell();
		}
		if (jrField.getName().equals("VesselVoyage")) {
			return errCtr.getVesvoy();
		}
		if (jrField.getName().equals("TypeCode")) {
			return errCtr.getTypeCode();
		}
		if (jrField.getName().equals("Commodity")) {
			return errCtr.getCommodity();
		}
		if (jrField.getName().equals("Comments")) {
			return errCtr.getComments();
		}
		if (jrField.getName().equals("Temp")) {
			return errCtr.getTemp();
		}
		if (jrField.getName().equals("Errors")) {
			return errCtr.getErrors();
		}

		return null;

	}

	public boolean next() throws JRException {

		index++;
		if (errCtrList != null && index < errCtrList.size()) {
			return true;
		}
		return false;
	}

}
