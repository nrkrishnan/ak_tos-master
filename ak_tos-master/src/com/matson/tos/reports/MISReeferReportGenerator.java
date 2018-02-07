package com.matson.tos.reports;
import java.util.ArrayList;
import java.util.List;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;


import com.matson.cas.refdata.mapping.TosRdsDataFinalMt;
import com.matson.tos.dao.NewReportVesselDao;

/* This class gets the records from DB for the MIS Reefer Report and stores it in the list. 
 * 
 * 	Srno	Date			AuthorName				Change Description
 * 	A1		10/18/2012		Meena Kumari			Initial creation 
 */

public class MISReeferReportGenerator implements JRDataSource{

	ArrayList<TosRdsDataFinalMt> jrMisReeferDetail = new ArrayList<TosRdsDataFinalMt>();

	int index = -1;

	public MISReeferReportGenerator(String Vesvoy) {
		
		
    
		List<?> misReeferList = NewReportVesselDao.getMISReeferReportGeneratorList(Vesvoy);
	
	
		
		if ((misReeferList == null ) || (misReeferList.size() <= 0)) { // no data 
			return;
		}
		for (int i = 0; i < misReeferList.size(); i++) {
			TosRdsDataFinalMt jrTosRdsFinalData = new TosRdsDataFinalMt();			
			String cn = ((TosRdsDataFinalMt)misReeferList.get(i)).getContainerNumber();
			String cd = ((TosRdsDataFinalMt)misReeferList.get(i)).getCheckDigit();
			cd = cd==null?"X":cd;
			cn = cn+cd;
			jrTosRdsFinalData.setContainerNumber(cn);
			jrTosRdsFinalData.setDir(((TosRdsDataFinalMt)misReeferList.get(i)).getDir());
			jrTosRdsFinalData.setDport(((TosRdsDataFinalMt)misReeferList.get(i)).getDport());
			jrTosRdsFinalData.setConsignee(((TosRdsDataFinalMt)misReeferList.get(i)).getConsignee());
			jrMisReeferDetail.add(jrTosRdsFinalData);					
		}  	
	}

	public Object getFieldValue(JRField jrField) throws JRException {
		
		TosRdsDataFinalMt misReefer = (TosRdsDataFinalMt) jrMisReeferDetail.get(index);
		if (misReefer == null) {
			return null;
		}
		if (jrField.getName().equals("ContainerNumber")) {
			return misReefer.getContainerNumber();
		}
		if (jrField.getName().equals("Dir")) {
			return misReefer.getDir();
		}	
		if (jrField.getName().equals("DPort")) {
			return misReefer.getDport();
		}	
		if (jrField.getName().equals("Consignee")) {
			return misReefer.getConsignee();
		}	
		return null;
		
	}

	public boolean next() throws JRException {
		
		index++;
		if (jrMisReeferDetail != null && index < jrMisReeferDetail.size()) {
			return true;
		}
		return false;
	}
}

