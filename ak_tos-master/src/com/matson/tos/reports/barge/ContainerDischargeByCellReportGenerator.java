package com.matson.tos.reports.barge;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import com.matson.cas.refdata.mapping.TosStowPlanCntrMt;
import com.matson.tos.dao.NewReportVesselDao;

/* This class gets the records from DB for EB SIT containers.
 * 
 * 	Srno	Date			AuthorName				Change Description
 * 	A1		10/8/2012		Meena Kumari			Initial creation 
 */

public class ContainerDischargeByCellReportGenerator implements JRDataSource{

	ArrayList<TosStowPlanCntrMt> jrMtyCtrDetail = new ArrayList<TosStowPlanCntrMt>();

	int index = -1;

	public ContainerDischargeByCellReportGenerator(String Vesvoy) {

		List<?> mtyCtrList = NewReportVesselDao.getAleReport(Vesvoy);
		
		if ((mtyCtrList == null ) || (mtyCtrList.size() <= 0)) { // no data 
			return;
		}
		for (int i = 0; i < mtyCtrList.size(); i++) {
			TosStowPlanCntrMt jrTosStowPlanData = new TosStowPlanCntrMt();			
			jrTosStowPlanData.setContainerNumber(((TosStowPlanCntrMt)mtyCtrList.get(i)).getContainerNumber());
			jrTosStowPlanData.setCell(((TosStowPlanCntrMt)mtyCtrList.get(i)).getCell());
			jrTosStowPlanData.setCommodity(((TosStowPlanCntrMt)mtyCtrList.get(i)).getCommodity());
			jrTosStowPlanData.setMisc1(((TosStowPlanCntrMt)mtyCtrList.get(i)).getMisc1());
			jrTosStowPlanData.setCweight(((TosStowPlanCntrMt)mtyCtrList.get(i)).getCweight());
			jrTosStowPlanData.setChassisNumber(((TosStowPlanCntrMt)mtyCtrList.get(i)).getChassisNumber());
		
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
		if (jrField.getName().equals("Cell")) {
			return mtyCtr.getCell();
		}	
		if (jrField.getName().equals("commodity")) {
			return mtyCtr.getCommodity();
		}	
		if (jrField.getName().equals("vesvoy")) {
			return mtyCtr.getMisc1();
		}	
		if (jrField.getName().equals("cweight")) {
			return mtyCtr.getCweight();
		}
		if (jrField.getName().equals("chassisNumber")) {
			return mtyCtr.getChassisNumber();
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

