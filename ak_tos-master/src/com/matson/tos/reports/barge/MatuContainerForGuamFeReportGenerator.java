package com.matson.tos.reports.barge;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import com.matson.cas.refdata.mapping.TosStowPlanCntrMt;
import com.matson.tos.dao.NewReportVesselDao;

/* This class gets the records from DB for the MTY Container Report and stores it in the list. 
 * 
 * 	Srno	Date			AuthorName				Change Description
 * 	A1		10/8/2012		Meena Kumari			Initial creation 
 */

public class MatuContainerForGuamFeReportGenerator implements JRDataSource{

	ArrayList<TosStowPlanCntrMt> jrMtyCtrDetail = new ArrayList<TosStowPlanCntrMt>();

	int index = -1;

	public MatuContainerForGuamFeReportGenerator(String Vesvoy) {

		List<?> mtyCtrList = NewReportVesselDao.getMatuContainersReportGeneratorList(Vesvoy);
		
		if ((mtyCtrList == null ) || (mtyCtrList.size() <= 0)) { // no data 
			return;
		}
		for (int i = 0; i < mtyCtrList.size(); i++) {
			TosStowPlanCntrMt jrTosStowPlanData = new TosStowPlanCntrMt();			
			String cn = ((TosStowPlanCntrMt)mtyCtrList.get(i)).getContainerNumber();
			String cd = ((TosStowPlanCntrMt)mtyCtrList.get(i)).getCheckDigit();
			cd = cd==null?"X":cd;
			cn = cn+cd;
			jrTosStowPlanData.setContainerNumber(cn);
			jrTosStowPlanData.setDischargePort(((TosStowPlanCntrMt)mtyCtrList.get(i)).getDischargePort());
			jrTosStowPlanData.setCell(((TosStowPlanCntrMt)mtyCtrList.get(i)).getCell());
			jrTosStowPlanData.setShipper(((TosStowPlanCntrMt)mtyCtrList.get(i)).getShipper());
			jrTosStowPlanData.setCommodity(((TosStowPlanCntrMt)mtyCtrList.get(i)).getCommodity());	
			jrMtyCtrDetail.add(jrTosStowPlanData );					
		}  		

	}

	public Object getFieldValue(JRField jrField) throws JRException {
		
		TosStowPlanCntrMt mtyCtr = (TosStowPlanCntrMt) jrMtyCtrDetail.get(index);
		if (mtyCtr == null) {
			return null;
		}
		if (jrField.getName().equals("ContainerNumber")) {
			return mtyCtr.getContainerNumber();
		}
		if (jrField.getName().equals("DischargePort")) {
			return mtyCtr.getDischargePort();
		}
		if (jrField.getName().equals("Cell")) {
			return mtyCtr.getCell();
		}
		if (jrField.getName().equals("Shipper")) {
			return mtyCtr.getShipper();
		}
		if (jrField.getName().equals("Commodity")) {
			return mtyCtr.getCommodity();
		}
		return null;
		
	}

	public boolean next() throws JRException {
		
		index++;
		if (jrMtyCtrDetail != null && index < jrMtyCtrDetail.size()) {
			return true;
		}
		return false;
	}

}

