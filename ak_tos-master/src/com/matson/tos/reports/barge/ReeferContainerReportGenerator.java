package com.matson.tos.reports.barge;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import com.matson.cas.refdata.mapping.TosRdsDataFinalMt;
import com.matson.cas.refdata.mapping.TosStowPlanCntrMt;
import com.matson.tos.dao.NewReportVesselDao;

/* This class gets the records from DB for the MTY Container Report and stores it in the list. 
 * 
 * 	Srno	Date			AuthorName				Change Description
 * 	A1		10/8/2012		Meena Kumari			Initial creation 
 */

public class ReeferContainerReportGenerator implements JRDataSource{

	ArrayList<TosStowPlanCntrMt> jrMtyCtrDetail = new ArrayList<TosStowPlanCntrMt>();

	int index = -1;

	public ReeferContainerReportGenerator(String Vesvoy) {

		List<?> mtyCtrList = NewReportVesselDao.getReeferContainerReportGeneratorList(Vesvoy);
		
		if ((mtyCtrList == null ) || (mtyCtrList.size() <= 0)) { // no data 
			return;
		}
		for (int i = 0; i < mtyCtrList.size(); i++) {
			TosStowPlanCntrMt jrTosRdsFinalData = new TosStowPlanCntrMt();			
			String cn = ((TosStowPlanCntrMt)mtyCtrList.get(i)).getContainerNumber();
			String cd = ((TosStowPlanCntrMt)mtyCtrList.get(i)).getCheckDigit();
			cd = cd==null?"X":cd;
			cn = cn+cd;
			jrTosRdsFinalData.setContainerNumber(cn);
			jrTosRdsFinalData.setCell(((TosStowPlanCntrMt)mtyCtrList.get(i)).getCell());
			String tempMeasurementUnit = ((TosStowPlanCntrMt)mtyCtrList.get(i)).getTempMeasurementUnit();
			tempMeasurementUnit = tempMeasurementUnit==null?"":tempMeasurementUnit;
			jrTosRdsFinalData.setTemp(((TosStowPlanCntrMt)mtyCtrList.get(i)).getTemp()+tempMeasurementUnit);
			jrTosRdsFinalData.setTempMeasurementUnit(((TosStowPlanCntrMt) mtyCtrList.get(i)).getTempMeasurementUnit());
			jrTosRdsFinalData.setDport(((TosStowPlanCntrMt)mtyCtrList.get(i)).getDport());
			jrTosRdsFinalData.setDischargePort(((TosStowPlanCntrMt)mtyCtrList.get(i)).getDischargePort());
			jrTosRdsFinalData.setCweight(((TosStowPlanCntrMt)mtyCtrList.get(i)).getCweight());
			jrTosRdsFinalData.setTypeCode(((TosStowPlanCntrMt)mtyCtrList.get(i)).getTypeCode());
			System.out.println("commodity value:"+((TosStowPlanCntrMt)mtyCtrList.get(i)).getCommodity());
			jrTosRdsFinalData.setCommodity(((TosStowPlanCntrMt)mtyCtrList.get(i)).getCommodity());
			String commodity = ((TosStowPlanCntrMt)mtyCtrList.get(i)).getCommodity();
			if(commodity!=null && commodity.contains("PLANTS"))
				jrTosRdsFinalData.setHazardousOpenCloseFlag("X");
			else
				jrTosRdsFinalData.setHazardousOpenCloseFlag("");
						
			jrMtyCtrDetail.add(jrTosRdsFinalData );					
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
		if (jrField.getName().equals("Temp")) {
			return mtyCtr.getTemp();
		}	
		if (jrField.getName().equals("Comments")) {
			if(mtyCtr.getTempMeasurementUnit()==null || mtyCtr.getTempMeasurementUnit().equals("")){
				return "Temperature scale was not downloaded, verify the temperature";
			}
			else
				return "";
		}
		if (jrField.getName().equals("Dport")) {
			return mtyCtr.getDport();
		}
		if (jrField.getName().equals("DischargePort")) {
			return mtyCtr.getDischargePort();
		}
		if (jrField.getName().equals("Weight")) {
			return mtyCtr.getCweight();
		}
		if (jrField.getName().equals("TypeCode")) {
			return mtyCtr.getTypeCode();
		}
		if (jrField.getName().equals("Commodity")) {
			return mtyCtr.getCommodity();
		}
		if (jrField.getName().equals("Critical")) {
			return mtyCtr.getHazardousOpenCloseFlag();
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

