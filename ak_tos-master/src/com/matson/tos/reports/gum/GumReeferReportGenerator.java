package com.matson.tos.reports.gum;
import java.util.ArrayList;
import java.util.List;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import com.matson.cas.refdata.mapping.TosGumRdsDataFinalMt;
import com.matson.tos.dao.GumVesselReportDao;

/* This class gets the records from DB for the Reefer For F and M Containers Report and stores it in the list. 
 * 
 * 	Srno	Date			AuthorName				Change Description
 * 	1		04/03/2013		Karthik Rajendran		Class created
 * 	2		04/04/2013		Karthik Rajendran		Columns mapping changed
 */

public class GumReeferReportGenerator implements JRDataSource{

	ArrayList<TosGumRdsDataFinalMt> jrReeferDetail = new ArrayList<TosGumRdsDataFinalMt>();

	int index = -1;

	public GumReeferReportGenerator(String Vesvoy) {

		List<?> reeferList = GumVesselReportDao.getReeferContainers(Vesvoy);			
		
		if ((reeferList == null ) || reeferList.size() <= 0) { // no data 
			return;
		}
		for (int i = 0; i <  reeferList.size(); i++) {
			TosGumRdsDataFinalMt jrTosRdsFinalData = new TosGumRdsDataFinalMt();		
			
			String cn = ((TosGumRdsDataFinalMt)reeferList.get(i)).getContainerNumber();
			String cd = ((TosGumRdsDataFinalMt)reeferList.get(i)).getCheckDigit();
			cd = cd==null?"X":cd;
			cn = cn+cd;
			jrTosRdsFinalData.setContainerNumber(cn);
			String tempMeasurementUnit = ((TosGumRdsDataFinalMt) reeferList.get(i)).getTempMeasurementUnit();
			tempMeasurementUnit = tempMeasurementUnit==null?"":tempMeasurementUnit;
			jrTosRdsFinalData.setTemp(((TosGumRdsDataFinalMt) reeferList.get(i)).getTemp()+tempMeasurementUnit);
			jrTosRdsFinalData.setTempMeasurementUnit(((TosGumRdsDataFinalMt) reeferList.get(i)).getTempMeasurementUnit());
			jrTosRdsFinalData.setConsignee(((TosGumRdsDataFinalMt)reeferList.get(i)).getConsignee());
			jrTosRdsFinalData.setCargoNotes(((TosGumRdsDataFinalMt)reeferList.get(i)).getCargoNotes());
			jrTosRdsFinalData.setDport(((TosGumRdsDataFinalMt) reeferList.get(i)).getDport());
			jrTosRdsFinalData.setVesvoy(((TosGumRdsDataFinalMt) reeferList.get(i)).getVesvoy());
			jrTosRdsFinalData.setTypeCode(((TosGumRdsDataFinalMt) reeferList.get(i)).getTypeCode());
			jrTosRdsFinalData.setCell(((TosGumRdsDataFinalMt) reeferList.get(i)).getCell());
			jrTosRdsFinalData.setSealNumber(((TosGumRdsDataFinalMt) reeferList.get(i)).getSealNumber());
			jrTosRdsFinalData.setBookingNumber(((TosGumRdsDataFinalMt) reeferList.get(i)).getBookingNumber());
			jrReeferDetail.add(jrTosRdsFinalData );					
		}  		

	}

	public Object getFieldValue(JRField jrField) throws JRException {
		
		TosGumRdsDataFinalMt reeferCtr = (TosGumRdsDataFinalMt) jrReeferDetail.get(index);
		if (reeferCtr == null) {
			return null;
		}
		if (jrField.getName().equals("ContainerNumber")) {
			return reeferCtr.getContainerNumber();
		}
		if (jrField.getName().equals("Temp")) {
			return reeferCtr.getTemp();
		}	
		if (jrField.getName().equals("Comments")) {
			if(reeferCtr.getTempMeasurementUnit()==null || reeferCtr.getTempMeasurementUnit().equals("")){
				return "Temperature scale was not downloaded, verify the temperature";
			}
			else
				return "";
		}	
		if (jrField.getName().equals("Consignee")) {
			return reeferCtr.getConsignee();
		}
		if (jrField.getName().equals("CargoNotes")) {
			return reeferCtr.getCargoNotes();
		}	
		if (jrField.getName().equals("Dport")) {
			return reeferCtr.getDport();
		}	
		if (jrField.getName().equals("TypeCode")) {
			return reeferCtr.getTypeCode();
		}	
		if (jrField.getName().equals("Cell")) {
			return reeferCtr.getCell();
		}	
		if (jrField.getName().equals("Seal")) {
			return reeferCtr.getSealNumber();
		}	
		if (jrField.getName().equals("ShipNo")) {
			return reeferCtr.getBookingNumber();
		}	
		return null;
		
	}

	public boolean next() throws JRException {
		
		index++;
		if (jrReeferDetail != null && index < jrReeferDetail.size()) {
			return true;
		}
		return false;
	}

}

