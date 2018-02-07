package com.matson.tos.reports;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import com.matson.cas.refdata.mapping.TosRdsDataFinalMt;
import com.matson.tos.dao.NewReportVesselDao;


/* This class gets the records from DB for the AG Container Inspections Report and stores it in the list. 
 * 
 * 	Srno	Date			AuthorName				Change Description
 * 	A1		10/18/2012		Meena Kumari			Initial creation 
 *  A2		03/28/2013		Karthik Rajendran		Removed:DIR field. Added: Cell and Commodity fields.
 *  A3		03/29/2013		Karthik Rajendran		Added: DPORT Sorting
 *  ---------------------
 *  A1		04/11/2013		Karthik Rajendran		Class created from AGContainerInspectionReportGenerator class
 */

public class AGCtrsNotFlaggedReportGenerator implements JRDataSource{

	ArrayList<TosRdsDataFinalMt> jrAgCtrInspecDetail = new ArrayList<TosRdsDataFinalMt>();

	int index = -1;

	public AGCtrsNotFlaggedReportGenerator(String Vesvoy) {


	//	List<?> agCtrInspList1 = NewReportVesselDao.getAGContainerInspectionsList1(Vesvoy);
		List<?> agCtrInspList2 = NewReportVesselDao.getAGContainerInspectionsList2(Vesvoy);

	/*	if ((agCtrInspList1 == null) || ( agCtrInspList1.size() >= 0)) {
			for (int i = 0; i < agCtrInspList1.size(); i++) {
				TosRdsDataFinalMt jrTosRdsFinalData = new TosRdsDataFinalMt();
				String cn = ((TosRdsDataFinalMt)agCtrInspList1.get(i)).getContainerNumber();
				String cd = ((TosRdsDataFinalMt)agCtrInspList1.get(i)).getCheckDigit();
				cd = cd==null?"X":cd;
				cn = cn+cd;
				jrTosRdsFinalData.setContainerNumber(cn);
				//jrTosRdsFinalData.setDir(((TosRdsDataFinalMt)agCtrInspList1.get(i)).getDir());
				jrTosRdsFinalData.setCommodity(((TosRdsDataFinalMt)agCtrInspList1.get(i)).getCommodity());
				jrTosRdsFinalData.setCell(((TosRdsDataFinalMt)agCtrInspList1.get(i)).getCell());
				jrTosRdsFinalData.setDport(((TosRdsDataFinalMt)agCtrInspList1.get(i)).getDport());
				jrTosRdsFinalData.setConsignee(((TosRdsDataFinalMt)agCtrInspList1.get(i)).getConsignee());
				jrAgCtrInspecDetail.add(jrTosRdsFinalData);					
			}  	
		}*/
		if ((agCtrInspList2 == null) || ( agCtrInspList2.size() >= 0)) {
			for (int i = 0; i < agCtrInspList2.size(); i++) {
				TosRdsDataFinalMt jrTosRdsFinalData = new TosRdsDataFinalMt();			
				String cn = ((TosRdsDataFinalMt)agCtrInspList2.get(i)).getContainerNumber();
				String cd = ((TosRdsDataFinalMt)agCtrInspList2.get(i)).getContainerNumber();
				cd = cd==null?"X":cd;
				cn = cn+cd;
				jrTosRdsFinalData.setContainerNumber(cn);
				//jrTosRdsFinalData.setDir(((TosRdsDataFinalMt)agCtrInspList2.get(i)).getDir());
				jrTosRdsFinalData.setCommodity(((TosRdsDataFinalMt)agCtrInspList2.get(i)).getCommodity());
				jrTosRdsFinalData.setCell(((TosRdsDataFinalMt)agCtrInspList2.get(i)).getCell());
				jrTosRdsFinalData.setDport(((TosRdsDataFinalMt)agCtrInspList2.get(i)).getDport());
				jrTosRdsFinalData.setConsignee(((TosRdsDataFinalMt)agCtrInspList2.get(i)).getConsignee());
				jrAgCtrInspecDetail.add(jrTosRdsFinalData);					
			} 
		} 	
		
		Collections.sort(jrAgCtrInspecDetail, new Comparator<TosRdsDataFinalMt>(){
		     public int compare(TosRdsDataFinalMt rds1, TosRdsDataFinalMt rds2){
		    	return rds1.getDport().compareTo(rds2.getDport());
		     }
		});
	}

	public Object getFieldValue(JRField jrField) throws JRException {

		TosRdsDataFinalMt mixPortCtr = (TosRdsDataFinalMt) jrAgCtrInspecDetail.get(index);
		if (mixPortCtr == null) {
			return null;
		}
		if (jrField.getName().equals("ContainerNumber")) {
			return mixPortCtr.getContainerNumber();
		}
		if (jrField.getName().equals("Commodity")) {
			return mixPortCtr.getCommodity();
		}	
		if (jrField.getName().equals("Cell")) {
			return mixPortCtr.getCell();
		}
		if (jrField.getName().equals("DPort")) {
			return mixPortCtr.getDport();
		}	
		if (jrField.getName().equals("Consignee")) {
			return mixPortCtr.getConsignee();
		}	
		return null;

	}

	public boolean next() throws JRException {

		index++;
		if (jrAgCtrInspecDetail != null && index < jrAgCtrInspecDetail.size()) {
			return true;
		}
		return false;
	}
}

