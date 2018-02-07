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


/* This class gets the records from DB for the Mix Port Container Report and stores it in the list. 
 * 
 * 	Srno	Date			AuthorName				Change Description
 * 	A1		10/17/2012		Meena Kumari			Initial creation 
 * 	A2		04/12/2013		Karthik Rajendran		Added: Sorting Full, Empties, Auto containers.
 */

public class CustomsReportGenerator implements JRDataSource {

	ArrayList<TosRdsDataFinalMt> jrCustomsDetail = new ArrayList<TosRdsDataFinalMt>();

	int index = -1;

	public CustomsReportGenerator(String Vesvoy) {

		List<?> customsList = NewReportVesselDao.getCustomsList(Vesvoy);

		if ((customsList == null) || (customsList.size() <= 0)) { // no data
			return;
		}
		for (int i = 0; i < customsList.size(); i++) {
			TosRdsDataFinalMt jrTosRdsFinalData = new TosRdsDataFinalMt();
			String cn = ((TosRdsDataFinalMt) customsList.get(i))
					.getContainerNumber();
			String cd = ((TosRdsDataFinalMt) customsList.get(i))
					.getCheckDigit();
			cd = cd == null ? "X" : cd;
			cn = cn + cd;
			jrTosRdsFinalData.setContainerNumber(cn);
			jrTosRdsFinalData.setSrv(((TosRdsDataFinalMt) customsList.get(i))
					.getSrv());
			jrTosRdsFinalData.setLoc(((TosRdsDataFinalMt) customsList.get(i))
					.getLoc());
			jrTosRdsFinalData.setCrstatus(((TosRdsDataFinalMt) customsList
					.get(i)).getCrstatus());
			jrTosRdsFinalData.setDport(((TosRdsDataFinalMt) customsList.get(i))
					.getDport());
			jrTosRdsFinalData.setConsignee(((TosRdsDataFinalMt) customsList
					.get(i)).getConsignee());
			jrTosRdsFinalData
					.setVesvoy(((TosRdsDataFinalMt) customsList.get(i))
							.getVesvoy());
			jrTosRdsFinalData.setTypeCode(((TosRdsDataFinalMt) customsList
					.get(i)).getTypeCode());
			jrTosRdsFinalData.setCargoNotes(((TosRdsDataFinalMt) customsList
					.get(i)).getCargoNotes());
			jrTosRdsFinalData.setSealNumber(((TosRdsDataFinalMt) customsList
					.get(i)).getSealNumber());
			String bkg = ((TosRdsDataFinalMt) customsList.get(i))
					.getBookingNumber();
			bkg = bkg == null ? "" : bkg;
			bkg = bkg.replace("null", "");
			jrTosRdsFinalData.setBookingNumber(bkg);
			jrTosRdsFinalData.setMisc1(((TosRdsDataFinalMt) customsList.get(i))
					.getMisc1());
			String ds = ((TosRdsDataFinalMt) customsList.get(i)).getDs();
			ds = ds == null ? "MTY" : ds;
			jrTosRdsFinalData.setDs(ds);
			if (ds.equals("AUT"))
				jrTosRdsFinalData.setAei("4");
			else if (ds.equals("CY"))
				jrTosRdsFinalData.setAei("1");
			else if (ds.equals("MTY"))
				jrTosRdsFinalData.setAei("3");
			else
				jrTosRdsFinalData.setAei("2");
			jrCustomsDetail.add(jrTosRdsFinalData);
		}
		Collections.sort(jrCustomsDetail, new Comparator<TosRdsDataFinalMt>() {
			public int compare(TosRdsDataFinalMt rds1, TosRdsDataFinalMt rds2) {
				return rds1.getAei().compareTo(rds2.getAei());
			}
		});
	}

	public Object getFieldValue(JRField jrField) throws JRException {

		TosRdsDataFinalMt mixPortCtr = (TosRdsDataFinalMt) jrCustomsDetail
				.get(index);
		if (mixPortCtr == null) {
			return null;
		}
		if (jrField.getName().equals("ContainerNumber")) {
			return mixPortCtr.getContainerNumber();
		}
		if (jrField.getName().equals("Srv")) {
			return mixPortCtr.getSrv();
		}
		if (jrField.getName().equals("Loc")) {
			return mixPortCtr.getLoc();
		}
		if (jrField.getName().equals("Crstatus")) {
			return mixPortCtr.getCrstatus();
		}
		if (jrField.getName().equals("Dport")) {
			return mixPortCtr.getDport();
		}
		if (jrField.getName().equals("Consignee")) {
			return mixPortCtr.getConsignee();
		}
		if (jrField.getName().equals("Vesvoy")) {
			return mixPortCtr.getVesvoy();
		}
		if (jrField.getName().equals("TypeCode")) {
			return mixPortCtr.getTypeCode();
		}
		if (jrField.getName().equals("CargoNotes")) {
			return mixPortCtr.getCargoNotes();
		}
		if (jrField.getName().equals("SealNumber")) {
			return mixPortCtr.getSealNumber();
		}
		if (jrField.getName().equals("BookingNumber")) {
			return mixPortCtr.getBookingNumber();
		}
		if (jrField.getName().equals("Misc1")) {
			return mixPortCtr.getMisc1();
		}
		if (jrField.getName().equals("Ds")) {
			return mixPortCtr.getDs();
		}
		return null;

	}

	public boolean next() throws JRException {

		index++;
		if (jrCustomsDetail != null && index < jrCustomsDetail.size()) {
			return true;
		}
		return false;
	}
}
