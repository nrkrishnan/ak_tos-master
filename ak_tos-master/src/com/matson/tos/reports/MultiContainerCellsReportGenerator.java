package com.matson.tos.reports;
import java.util.ArrayList;
import java.util.List;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import com.matson.cas.refdata.mapping.TosRdsDataFinalMt;
import com.matson.tos.dao.NewReportVesselDao;

/* This class gets the records from DB for the Multi Container Cells Report and stores it in the list. 
 * 
 * 	Srno	Date			AuthorName				Change Description
 * 	A1		10/11/2012		Meena Kumari			Initial creation 
 */

public class MultiContainerCellsReportGenerator implements JRDataSource{

	ArrayList<TosRdsDataFinalMt> jrMultiCtrCellsDetail = new ArrayList<TosRdsDataFinalMt>();

	int index = -1;

	public MultiContainerCellsReportGenerator(String Vesvoy) {

		List<?> multiCtrCellsList = NewReportVesselDao.getMultiContainerCellsReportGeneratorList(Vesvoy);
	
		if ((multiCtrCellsList == null ) || (multiCtrCellsList.size() <= 0)) { // no data 
			return;
		}
		for (int i = 0; i < multiCtrCellsList.size(); i++) {
			TosRdsDataFinalMt jrTosRdsFinalData = new TosRdsDataFinalMt();
			String cn = ((TosRdsDataFinalMt)multiCtrCellsList.get(i)).getContainerNumber();
			String cd = ((TosRdsDataFinalMt)multiCtrCellsList.get(i)).getCheckDigit();
			cd = cd==null?"X":cd;
			cn = cn+cd;
			jrTosRdsFinalData.setContainerNumber(cn);
			jrTosRdsFinalData.setDir(((TosRdsDataFinalMt)multiCtrCellsList.get(i)).getDir());
			jrTosRdsFinalData.setDport(((TosRdsDataFinalMt)multiCtrCellsList.get(i)).getDport());
			jrTosRdsFinalData.setConsignee(((TosRdsDataFinalMt)multiCtrCellsList.get(i)).getConsignee());
			jrTosRdsFinalData.setCell(((TosRdsDataFinalMt)multiCtrCellsList.get(i)).getCell());
			jrTosRdsFinalData.setTypeCode(((TosRdsDataFinalMt)multiCtrCellsList.get(i)).getTypeCode());
			jrMultiCtrCellsDetail.add(jrTosRdsFinalData );					
		}  		

	}

	public Object getFieldValue(JRField jrField) throws JRException {
		
		TosRdsDataFinalMt multiCtrCell = (TosRdsDataFinalMt) jrMultiCtrCellsDetail.get(index);
		if (multiCtrCell == null) {
			return null;
		}
		if (jrField.getName().equals("ContainerNumber")) {
			return multiCtrCell.getContainerNumber();
		}
		if (jrField.getName().equals("Dir")) {
			return multiCtrCell.getDir();
		}	
		if (jrField.getName().equals("DPort")) {
			return multiCtrCell.getDport();
		}	
		if (jrField.getName().equals("Consignee")) {
			return multiCtrCell.getConsignee();
		}	
		if (jrField.getName().equals("Cell")) {
			return multiCtrCell.getCell();
		}	
		if (jrField.getName().equals("TypeCode")) {
			return multiCtrCell.getTypeCode();
		}	
		return null;
		
	}

	public boolean next() throws JRException {
		
		index++;
		if (jrMultiCtrCellsDetail != null && index < jrMultiCtrCellsDetail.size()) {
			return true;
		}
		return false;
	}

}

