package com.matson.tos.reports.gum;
import java.util.ArrayList;
import java.util.List;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import com.matson.cas.refdata.mapping.TosGumRdsDataFinalMt;
import com.matson.tos.dao.GumVesselReportDao;

/* This class gets the records from DB for the Rider report. 
 * 
 * 	Srno	Date			AuthorName				Change Description
 * 	1		05/01/2013		Raghu Iyer				Class created and columns mapped
 * 	
 */

public class GumRiderReportGenerator implements JRDataSource{

	ArrayList<TosGumRdsDataFinalMt> jrRiderDetail = new ArrayList<TosGumRdsDataFinalMt>();

	int index = -1;

	public GumRiderReportGenerator(String Vesvoy) {

		List riderList = GumVesselReportDao.getRiderReportData(Vesvoy);	
		if(riderList!=null)
		{
			for(int i=0; i<riderList.size(); i++)
			{
				TosGumRdsDataFinalMt jrTosRdsFinalData = new TosGumRdsDataFinalMt();
				Object[] obj =  (Object[]) riderList.get(i);
				

				jrTosRdsFinalData.setVesvoy(obj[0]==null?"":obj[0].toString());
				jrTosRdsFinalData.setBookingNumber(obj[1]==null?"":obj[1].toString());
				jrTosRdsFinalData.setContainerNumber(obj[2]==null?"":obj[2].toString());
				jrTosRdsFinalData.setSealNumber(obj[3]==null?"":obj[3].toString());
				jrTosRdsFinalData.setTypeCode(obj[4]==null?"":obj[4].toString());
				jrRiderDetail.add(jrTosRdsFinalData );
			}
		}
				
			/*if ((riderList == null ) || riderList.size() <= 0) { // no data 
			return;
		}
		for (int i = 0; i <  riderList.size(); i++) {
			TosGumRdsDataFinalMt jrTosRdsFinalData = new TosGumRdsDataFinalMt();		
			

			jrTosRdsFinalData.setVesvoy(((TosGumRdsDataFinalMt) riderList.get(i)).getVesvoy());
			jrTosRdsFinalData.setBookingNumber(((TosGumRdsDataFinalMt) riderList.get(i)).getBookingNumber());
			String cn = ((TosGumRdsDataFinalMt)riderList.get(i)).getContainerNumber();
			String cd = ((TosGumRdsDataFinalMt)riderList.get(i)).getCheckDigit();
			cd = cd==null?"X":cd;
			cn = cn+cd;
			jrTosRdsFinalData.setContainerNumber(cn);
			jrTosRdsFinalData.setSealNumber(((TosGumRdsDataFinalMt) riderList.get(i)).getSealNumber());		
			jrTosRdsFinalData.setTypeCode(((TosGumRdsDataFinalMt) riderList.get(i)).getTypeCode());

			jrRiderDetail.add(jrTosRdsFinalData );					
		}  */		

	}

	public Object getFieldValue(JRField jrField) throws JRException {
		
		TosGumRdsDataFinalMt riderCtr = (TosGumRdsDataFinalMt) jrRiderDetail.get(index);
		if (riderCtr == null) {
			return null;
		}

		if (jrField.getName().equals("vesvoy")) {
			return riderCtr.getVesvoy();
		}	
		if (jrField.getName().equals("bookingNumber")) {
			return riderCtr.getBookingNumber();
		}
		if (jrField.getName().equals("ContainerNumber")) {
			return riderCtr.getContainerNumber();
		}
		if (jrField.getName().equals("Seal")) {
			return riderCtr.getSealNumber();
		}	
		if (jrField.getName().equals("EquipmentType")) {
			return riderCtr.getTypeCode();
		}		
			
		return null;
		
	}

	public boolean next() throws JRException {
		
		index++;
		if (jrRiderDetail != null && index < jrRiderDetail.size()) {
			return true;
		}
		return false;
	}

}

