package com.matson.tos.reports;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.hibernate.Query;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import com.matson.cas.refdata.mapping.TosRdsDataFinalMt;
import com.matson.tos.dao.NewReportVesselDao;

/* This class gets the records from DB for the MTY Container Report and stores it in the list. 
 * 
 * 	Srno	Date			AuthorName				Change Description
 * 	A1		03/18/2015		Raghu Pattangi			Initial creation 
 */

public class ReturnToShipperGenerator implements JRDataSource{

	ArrayList<TosRdsDataFinalMt> jrFullRtnShipperDetail = new ArrayList<TosRdsDataFinalMt>();

	int index = -1;

	public ReturnToShipperGenerator(String Vesvoy) {

		List<?> fullRetShipperLst = NewReportVesselDao.getReturnToShipperrReportGeneratorList(Vesvoy);
		
		if ((fullRetShipperLst == null ) || (fullRetShipperLst.size() <= 0)) { // no data 
			return;
		}
		for (int i = 0; i < fullRetShipperLst.size(); i++) {
			TosRdsDataFinalMt jrTosRdsFinalData = new TosRdsDataFinalMt();			
			String cn = ((TosRdsDataFinalMt)fullRetShipperLst.get(i)).getContainerNumber();
			String cd = ((TosRdsDataFinalMt)fullRetShipperLst.get(i)).getCheckDigit();
			cd = cd==null?"X":cd;
			cn = cn+cd;
			jrTosRdsFinalData.setContainerNumber(cn.replace("MATU", ""));				
			jrFullRtnShipperDetail.add(jrTosRdsFinalData );					
		}  		
		Collections.sort(jrFullRtnShipperDetail, new Comparator<TosRdsDataFinalMt>(){
		     public int compare(TosRdsDataFinalMt rds1, TosRdsDataFinalMt rds2){
		    	return rds1.getContainerNumber().compareTo(rds2.getContainerNumber());
		     }
		});

	}

	public Object getFieldValue(JRField jrField) throws JRException {
		
		TosRdsDataFinalMt mtyCtr = (TosRdsDataFinalMt) jrFullRtnShipperDetail.get(index);
		if (mtyCtr == null) {
			return null;
		}
		if (jrField.getName().equals("ContainerNumber")) {
			return mtyCtr.getContainerNumber();
		}
		return null;
		
	}

	public boolean next() throws JRException {
		
		index++;
		if (jrFullRtnShipperDetail != null && index < jrFullRtnShipperDetail.size()) {
			return true;
		}
		return false;
	}

}

