package com.matson.tos.reports;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import com.matson.cas.refdata.mapping.TosRdsDataFinalMt;
import com.matson.tos.dao.NewReportVesselDao;

/* This class gets the records from DB for the Damage Code Report and stores it in the list. 
 * 
 * 	Srno	Date			AuthorName				Change Description
 * 	A1		10/11/2012		Meena Kumari			Initial creation 
 */

public class DamageCodeReportGenerator implements JRDataSource{

	ArrayList<TosRdsDataFinalMt> jrsfTagDetail = new ArrayList<TosRdsDataFinalMt>();
	int totalDamagedCtrs = 0;

	int index = -1;

	public DamageCodeReportGenerator(String Vesvoy) {

		List<?> sfTagList = NewReportVesselDao.getDamageReportList(Vesvoy);
		if ((sfTagList == null) || ( sfTagList.size() <= 0)) { // no data 
			return;
		}
		/*for (int i = 0; i <  sfTagList.size(); i++) {
			TosRdsDataFinalMt jrTosRdsFinalData = new TosRdsDataFinalMt();
			String typeCode = ((TosRdsDataFinalMt) sfTagList.get(i)).getTypeCode();
			if(typeCode != null &&  typeCode.trim()!= "") {
				
				typeCode = typeCode.substring(0,4);
			}
			jrTosRdsFinalData.setTypeCode(typeCode);
			jrTosRdsFinalData.setDamageCode(((TosRdsDataFinalMt) sfTagList.get(i)).getDamageCode());
			jrTosRdsFinalData.setDs(((TosRdsDataFinalMt) sfTagList.get(i)).getDs());
			jrTosRdsFinalData.setTareWeight(((TosRdsDataFinalMt) sfTagList.get(i)).getTareWeight());						
			jrsfTagDetail.add(jrTosRdsFinalData );					
		}  		*/
		for (ListIterator<?> iter = sfTagList.listIterator(); iter.hasNext();) {
			Object[] row = (Object[]) iter.next();
			TosRdsDataFinalMt temp = new TosRdsDataFinalMt();
			temp.setTypeCode((String) row[0]);
			temp.setDamageCode((String) row[1]);
			temp.setDs((String) row[2]);
			temp.setTemp(row[3].toString()); // Temp is for storing typecodes count
			BigDecimal tcount = (BigDecimal)row[3];
			if(tcount!=null)
				totalDamagedCtrs = totalDamagedCtrs + tcount.intValue();
			jrsfTagDetail.add(temp);
		}

	}

	public Object getFieldValue(JRField jrField) throws JRException {
		
		TosRdsDataFinalMt  sfTag = (TosRdsDataFinalMt) jrsfTagDetail.get(index);
		if (sfTag == null) {
			return null;
		}
		if (jrField.getName().equals("TypeCode")) {
			return sfTag.getTypeCode();
		}
		if (jrField.getName().equals("DamageCode")) {
			return sfTag.getDamageCode();
		}
		if (jrField.getName().equals("DS")) {
			return sfTag.getDs();
		}
		if (jrField.getName().equals("Quantity")) {
			return sfTag.getTemp();
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

