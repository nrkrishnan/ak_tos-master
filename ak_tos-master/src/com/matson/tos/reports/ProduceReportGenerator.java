package com.matson.tos.reports;
import java.util.ArrayList;
import java.util.List;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import com.matson.cas.refdata.mapping.TosRdsDataFinalMt;
import com.matson.tos.dao.NewReportVesselDao;

/* This class gets the records from DB for the Produce Report and stores it in the list. 
 * 
 * 	Srno	Date			AuthorName				Change Description
 * 	A1		10/12/2012		Meena Kumari			Initial creation 
 */

public class ProduceReportGenerator implements JRDataSource{

	ArrayList<TosRdsDataFinalMt> jrProduceDetail = new ArrayList<TosRdsDataFinalMt>();

	int index = -1;

	public ProduceReportGenerator(String Vesvoy) {

		List<?> produceCtrList = NewReportVesselDao.getProduceCtrList(Vesvoy);
		
		//List<?> sitproduceCtrList = NewReportVesselDao.getSitpPoduceCtrList(Vesvoy);
				
		/*if ( ((produceCtrList == null ) || produceCtrList.size() <= 0) && 
				((sitproduceCtrList == null ) || (sitproduceCtrList.size() <=0)) ) { // no data 
			return;
		}*/
		if ( produceCtrList != null && produceCtrList.size() > 0){		
		for (int i = 0; i <  produceCtrList.size(); i++) {
			TosRdsDataFinalMt jrTosRdsFinalData = new TosRdsDataFinalMt();
			String cn = ((TosRdsDataFinalMt)produceCtrList.get(i)).getContainerNumber();
			String cd = ((TosRdsDataFinalMt)produceCtrList.get(i)).getCheckDigit();
			cd = cd==null?"X":cd;
			cn = cn+cd;
			jrTosRdsFinalData.setContainerNumber(cn);
			jrTosRdsFinalData.setCrstatus(((TosRdsDataFinalMt) produceCtrList.get(i)).getCrstatus());
			jrTosRdsFinalData.setCargoNotes(((TosRdsDataFinalMt) produceCtrList.get(i)).getCargoNotes());
			jrTosRdsFinalData.setTemp(((TosRdsDataFinalMt) produceCtrList.get(i)).getTemp());	
			jrTosRdsFinalData.setCell(((TosRdsDataFinalMt) produceCtrList.get(i)).getCell());
			jrTosRdsFinalData.setConsignee(((TosRdsDataFinalMt) produceCtrList.get(i)).getConsignee());
			jrProduceDetail.add(jrTosRdsFinalData );					
		} 
		}
		/*if ( sitproduceCtrList != null && sitproduceCtrList.size() > 0){
		for (int i = 0; i <  sitproduceCtrList.size(); i++) {
			TosRdsDataFinalMt jrTosRdsFinalData = new TosRdsDataFinalMt();
			jrTosRdsFinalData.setContainerNumber(((TosRdsDataFinalMt) sitproduceCtrList.get(i)).getContainerNumber());
			jrTosRdsFinalData.setCrstatus(((TosRdsDataFinalMt) sitproduceCtrList.get(i)).getCrstatus());
			jrTosRdsFinalData.setCargoNotes(((TosRdsDataFinalMt) sitproduceCtrList.get(i)).getCargoNotes());
			jrTosRdsFinalData.setTemp(((TosRdsDataFinalMt) sitproduceCtrList.get(i)).getTemp());
			jrTosRdsFinalData.setCell(((TosRdsDataFinalMt) sitproduceCtrList.get(i)).getCell());
			jrTosRdsFinalData.setConsignee(((TosRdsDataFinalMt) produceCtrList.get(i)).getConsignee());
			jrProduceDetail.add(jrTosRdsFinalData );					
		}  	
		}*/
	}

	public Object getFieldValue(JRField jrField) throws JRException {
		
		TosRdsDataFinalMt  sfTag = (TosRdsDataFinalMt) jrProduceDetail.get(index);
		if (sfTag == null) {
			return null;
		}
		if (jrField.getName().equals("ContainerNumber")) {
			return sfTag.getContainerNumber();
		}
		if (jrField.getName().equals("Crstatus")) {
			return sfTag.getCrstatus();
		}
		if (jrField.getName().equals("CargoNotes")) {
			return sfTag.getCargoNotes();
		}
		if (jrField.getName().equals("Temp")) {
			return sfTag.getTemp();
		}
		if (jrField.getName().equals("Cell")) {
			return sfTag.getCell();
		}
		if (jrField.getName().equals("Consignee")) {
			return sfTag.getConsignee();
		}
		return null;
		
	}

	public boolean next() throws JRException {
		
		index++;
		if (jrProduceDetail != null && index < jrProduceDetail.size()) {
			return true;
		}
		return false;
	}

}

