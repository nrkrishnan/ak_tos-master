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

public class NoDamageContainerReportGenerator implements JRDataSource{

	ArrayList<TosStowPlanCntrMt> jrsfNoDamageTagDetail = new ArrayList<TosStowPlanCntrMt>();

	int index = -1;

	public NoDamageContainerReportGenerator(String Vesvoy) {

		List<?> sfNoDamageTagList = NewReportVesselDao.getNoDamageReportList(Vesvoy);
		if ((sfNoDamageTagList == null) || ( sfNoDamageTagList.size() <= 0)) { // no data 
			return;
		}
		System.out.println("No Damage data size:"+sfNoDamageTagList.size());
		for (int i = 0; i <  sfNoDamageTagList.size(); i++) {
			TosStowPlanCntrMt jrNoDamageTosStowPlanData = new TosStowPlanCntrMt();
		
			String cn = ((TosStowPlanCntrMt)sfNoDamageTagList.get(i)).getContainerNumber();
			String cd = ((TosStowPlanCntrMt)sfNoDamageTagList.get(i)).getCheckDigit();
			cd = cd==null?"X":cd;
			cn = cn+cd;
			jrNoDamageTosStowPlanData.setContainerNumber(cn);
			jrNoDamageTosStowPlanData.setDir(((TosStowPlanCntrMt) sfNoDamageTagList.get(i)).getDir());
			jrNoDamageTosStowPlanData.setDport(((TosStowPlanCntrMt)sfNoDamageTagList.get(i)).getDport());
			jrNoDamageTosStowPlanData.setTypeCode(((TosStowPlanCntrMt) sfNoDamageTagList.get(i)).getTypeCode());
			jrNoDamageTosStowPlanData.setDamageCode(((TosStowPlanCntrMt) sfNoDamageTagList.get(i)).getDamageCode());
			jrNoDamageTosStowPlanData.setCargoNotes(((TosStowPlanCntrMt) sfNoDamageTagList.get(i)).getCargoNotes());						
			jrsfNoDamageTagDetail.add(jrNoDamageTosStowPlanData );					
		}  

	}

	public Object getFieldValue(JRField jrField) throws JRException {
		
		
		TosStowPlanCntrMt  sfNoDamageTag = (TosStowPlanCntrMt) jrsfNoDamageTagDetail.get(index);
		if (sfNoDamageTag == null) {
			return null;
		}
		if (jrField.getName().equals("NDContainerNumber")) {
			return sfNoDamageTag.getContainerNumber();
		}
		if (jrField.getName().equals("NDDport")) {
			return sfNoDamageTag.getDport();
		}
		if (jrField.getName().equals("NDTypeCode")) {
			return sfNoDamageTag.getTypeCode();
		}
		if (jrField.getName().equals("NDDamageCode")) {
			return sfNoDamageTag.getDamageCode();
		}
		if (jrField.getName().equals("NDDir")) {
			return sfNoDamageTag.getDir();
		}
		if (jrField.getName().equals("NDCargoNotes")) {
			return sfNoDamageTag.getCargoNotes();
		}	
		return null;
		
	}

	public boolean next() throws JRException {
		
		index++;
		if (jrsfNoDamageTagDetail != null && index < jrsfNoDamageTagDetail.size()) {
			return true;
		}
		return false;
	}

}

