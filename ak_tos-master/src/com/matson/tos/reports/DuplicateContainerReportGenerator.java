package com.matson.tos.reports;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import com.matson.cas.refdata.mapping.TosRdsDataFinalMt;
import com.matson.tos.dao.NewReportVesselDao;
import com.matson.tos.dao.TosLookup;

/* This class gets the records from DB for the Duplicate Container Report and stores it in the list. 
 * 
 * 	Srno	Date			AuthorName				Change Description
 * 	A1		10/11/2012		Meena Kumari			Initial creation 
 */

public class DuplicateContainerReportGenerator implements JRDataSource{

	ArrayList<TosRdsDataFinalMt> jrDuplicateCtrCellsDetail = new ArrayList<TosRdsDataFinalMt>();

	int index = -1;
	private static TosLookup tosLookUp=null;
	public DuplicateContainerReportGenerator(String Vesvoy) {
		try{
			tosLookUp = new TosLookup();
		//Query needs to be changed...
		List<?> dupCtrList = NewReportVesselDao.getDuplicateContainerList(Vesvoy);
		if ((dupCtrList == null) || (dupCtrList.size() <= 0)) { // no data 
			return;
		}
		for (int i = 0; i < dupCtrList.size(); i++) {
			TosRdsDataFinalMt jrTosRdsFinalData = new TosRdsDataFinalMt();	
			boolean checkDuplicate = false;
			String containerNumber = ((TosRdsDataFinalMt)dupCtrList.get(i)).getContainerNumber() + ((TosRdsDataFinalMt)dupCtrList.get(i)).getCheckDigit();
			checkDuplicate = tosLookUp.checkDuplicateContainer(containerNumber);
			if(checkDuplicate){
				String cn = ((TosRdsDataFinalMt)dupCtrList.get(i)).getContainerNumber();
				String cd = ((TosRdsDataFinalMt)dupCtrList.get(i)).getCheckDigit();
				cd = cd==null?"X":cd;
				cn = cn+cd;
				jrTosRdsFinalData.setContainerNumber(cn);	
				jrTosRdsFinalData.setLoc(((TosRdsDataFinalMt)dupCtrList.get(i)).getLoc());
				jrTosRdsFinalData.setTemp(((TosRdsDataFinalMt)dupCtrList.get(i)).getTemp());
				jrTosRdsFinalData.setCell(((TosRdsDataFinalMt)dupCtrList.get(i)).getCell());
				jrTosRdsFinalData.setVesvoy(((TosRdsDataFinalMt)dupCtrList.get(i)).getVesvoy());
				jrTosRdsFinalData.setDs(((TosRdsDataFinalMt)dupCtrList.get(i)).getDs());
				jrTosRdsFinalData.setDsc(((TosRdsDataFinalMt)dupCtrList.get(i)).getDsc());
				jrTosRdsFinalData.setConsignee(((TosRdsDataFinalMt)dupCtrList.get(i)).getConsignee());
				jrDuplicateCtrCellsDetail.add(jrTosRdsFinalData);
			}
		}
		}catch(SQLException e){
			e.printStackTrace();
		}catch(Exception ex){
			System.out.println("Unable to create TosLookup.<br /> " + ex.toString());
		}
	}  		

	public Object getFieldValue(JRField jrField) throws JRException {
		
		TosRdsDataFinalMt dupCtrCells = (TosRdsDataFinalMt) jrDuplicateCtrCellsDetail.get(index);
		if (dupCtrCells == null) {
			return null;
		}
		if (jrField.getName().equals("ContainerNumber")) {
			return dupCtrCells.getContainerNumber();
		}
		if (jrField.getName().equals("Loc")) {
			return dupCtrCells.getLoc();
		}
		if (jrField.getName().equals("Temp")) {
			return dupCtrCells.getTemp();
		}
		if (jrField.getName().equals("Vesvoy")) {
			return dupCtrCells.getVesvoy();
		}
		if (jrField.getName().equals("Cell")) {
			return dupCtrCells.getCell();
		}
		if (jrField.getName().equals("Ds")) {
			return dupCtrCells.getDs();
		}
		if (jrField.getName().equals("Dsc")) {
			return dupCtrCells.getDsc();
		}
		if (jrField.getName().equals("Consignee")) {
			return dupCtrCells.getConsignee();
		}
		return null;
		
	}

	public boolean next() throws JRException {
		
		index++;
		if (jrDuplicateCtrCellsDetail != null && index < jrDuplicateCtrCellsDetail.size()) {
			return true;
		}
		return false;
	}

}

