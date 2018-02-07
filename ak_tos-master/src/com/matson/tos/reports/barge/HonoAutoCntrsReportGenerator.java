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

public class HonoAutoCntrsReportGenerator implements JRDataSource{

	ArrayList<TosStowPlanCntrMt> jrMtyCtrDetail = new ArrayList<TosStowPlanCntrMt>();

	int index = -1;

	public HonoAutoCntrsReportGenerator(String Vesvoy) {

		List<?> mtyCtrList = NewReportVesselDao.getHonAutoCntrsOnVV(Vesvoy);
		
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
			jrTosStowPlanData.setDs(((TosStowPlanCntrMt)mtyCtrList.get(i)).getDs());
			jrTosStowPlanData.setDir(((TosStowPlanCntrMt)mtyCtrList.get(i)).getDir());
			jrTosStowPlanData.setDport(((TosStowPlanCntrMt)mtyCtrList.get(i)).getDport());
			jrTosStowPlanData.setDischargePort(((TosStowPlanCntrMt)mtyCtrList.get(i)).getDischargePort());
			jrTosStowPlanData.setCell(((TosStowPlanCntrMt)mtyCtrList.get(i)).getCell());
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
		if (jrField.getName().equals("Ds")) {
			return mtyCtr.getDs();
		}	
		if (jrField.getName().equals("Dir")) {
			return mtyCtr.getDir();
		}	
		if (jrField.getName().equals("Dport")) {
			return mtyCtr.getDport();
		}	
		if (jrField.getName().equals("DischargePort")) {
			return mtyCtr.getDischargePort();
		}	
		if (jrField.getName().equals("Cell")) {
			return mtyCtr.getCell();
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

