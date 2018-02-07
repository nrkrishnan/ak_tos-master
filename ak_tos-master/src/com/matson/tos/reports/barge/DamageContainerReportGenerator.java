package com.matson.tos.reports.barge;
import java.util.ArrayList;
import java.util.List;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import com.matson.cas.refdata.mapping.TosStowPlanCntrMt;
import com.matson.tos.dao.NewReportVesselDao;

/* This class gets the records from DB for the Damage Code Report and stores it in the list. 
 * 
 * 	Srno	Date			AuthorName				Change Description
 * 	A1		10/11/2012		Meena Kumari			Initial creation 
 */

public class DamageContainerReportGenerator implements JRDataSource{

	ArrayList<TosStowPlanCntrMt> jrsfTagDetail = new ArrayList<TosStowPlanCntrMt>();
	
	int index = -1;

	public DamageContainerReportGenerator(String Vesvoy) {

		List<?> sfTagList = NewReportVesselDao.getDamageBargeReportList(Vesvoy);
		if ((sfTagList == null) || ( sfTagList.size() <= 0)) { // no data 
			return;
		}                                     
		System.out.println("Damage data size:"+sfTagList.size());
		for (int i = 0; i <  sfTagList.size(); i++) {
			TosStowPlanCntrMt jrTosStowPlanData = new TosStowPlanCntrMt();
			
			String cn = ((TosStowPlanCntrMt)sfTagList.get(i)).getContainerNumber();
			String cd = ((TosStowPlanCntrMt)sfTagList.get(i)).getCheckDigit();
			cd = cd==null?"X":cd;
			cn = cn+cd;
			jrTosStowPlanData.setContainerNumber(cn);
			jrTosStowPlanData.setDir(((TosStowPlanCntrMt) sfTagList.get(i)).getDir());
			jrTosStowPlanData.setDport(((TosStowPlanCntrMt)sfTagList.get(i)).getDport());
			jrTosStowPlanData.setTypeCode(((TosStowPlanCntrMt) sfTagList.get(i)).getTypeCode());
			jrTosStowPlanData.setDamageCode(((TosStowPlanCntrMt) sfTagList.get(i)).getDamageCode());
			jrTosStowPlanData.setCargoNotes(((TosStowPlanCntrMt) sfTagList.get(i)).getCargoNotes());						
			jrsfTagDetail.add(jrTosStowPlanData );					
		}  
		
	}

	public Object getFieldValue(JRField jrField) throws JRException {
		
		TosStowPlanCntrMt  sfTag = (TosStowPlanCntrMt) jrsfTagDetail.get(index);
		if (sfTag == null) {
			return null;
		}
		if (jrField.getName().equals("ContainerNumber")) {
			return sfTag.getContainerNumber();
		}
		if (jrField.getName().equals("Dport")) {
			return sfTag.getDport();
		}
		if (jrField.getName().equals("TypeCode")) {
			return sfTag.getTypeCode();
		}
		if (jrField.getName().equals("DamageCode")) {
			return sfTag.getDamageCode();
		}
		if (jrField.getName().equals("Dir")) {
			return sfTag.getDir();
		}
		if (jrField.getName().equals("CargoNotes")) {
			return sfTag.getCargoNotes();
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

