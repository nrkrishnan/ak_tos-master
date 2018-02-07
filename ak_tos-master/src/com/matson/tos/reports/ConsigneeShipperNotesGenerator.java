package com.matson.tos.reports;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import org.apache.log4j.Logger;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import com.matson.cas.refdata.mapping.TosRdsDataFinalMt;
import com.matson.cas.refdata.mapping.TosStowPlanCntrMt;
import com.matson.tos.dao.NewReportVesselDao;
import com.matson.tos.dao.TosLookup;

/* This class gets the records from DB for the Destination Ports Discrepancies Report and stores it in the list. 
 * 
 * 	Srno	Date			AuthorName				Change Description
 * 	A1		10/16/2012		Meena Kumari			Initial creation 
 * 	A2		04/08/2013		Karthik Rajendran		Added: Sorting on Cell
 */

public class ConsigneeShipperNotesGenerator implements JRDataSource {
	private static Logger logger = Logger.getLogger(ConsigneeShipperNotesGenerator.class);
	ArrayList<TosRdsDataFinalMt> jrConsShipNotesDetail = new ArrayList<TosRdsDataFinalMt>();
	private static TosLookup lookUp=null;
	int index = -1;

	public ConsigneeShipperNotesGenerator(String Vesvoy) {
		System.out.println("ConsigneeShipperNotesGenerator begin");
		try {
			lookUp = new TosLookup();
		}catch(Exception ex){
			logger.error("Lookup failed: "+ex);
		}
		List results = NewReportVesselDao.getDistinctConsignees(Vesvoy);
		if ((results == null) || (results.size() <= 0)) { // no data
			return;
		}
		System.out.println("ConsigneeShipperNotesGenerator ****** result size:"+results.size());
	
		for (int i=0;i<results.size();i++) {
			//Object[] row = (Object[]) iter.next();
			String Consignee = (String)results.get(i);
			//System.out.println("Consignee name :"+Consignee);
			TosRdsDataFinalMt temp = new TosRdsDataFinalMt();
			//System.out.println("temp consignee name " +Consignee);
			String notes = null;
			notes = lookUp.getConsigneeNotes(Consignee);
			System.out.println("notes in generator: "+notes);
			if (notes!=null && !"null".equalsIgnoreCase(notes) && !"".equalsIgnoreCase(notes)) {
				temp.setConsignee(Consignee);
				temp.setCargoNotes(notes);
				jrConsShipNotesDetail.add(temp);
			}
		}
		System.out.println("ConsigneeShipperNotesGenerator end");
		if (lookUp != null)
		{
			logger.info("Begin TosLook clean up in ConsigneeShipperNotesGenerator");
			lookUp.close();
			lookUp = null;
			logger.info("End TosLook clean up in ConsigneeShipperNotesGenerator");
		}
	}

	public Object getFieldValue(JRField jrField) throws JRException {

		TosRdsDataFinalMt consigneeShipper = (TosRdsDataFinalMt) jrConsShipNotesDetail
				.get(index);
		if (consigneeShipper == null) {
			return null;
		}
		if (jrField.getName().equals("Consignee")) {
			return consigneeShipper.getConsignee();
		}
		if (jrField.getName().equals("notes")) {
			return consigneeShipper.getCargoNotes();
		}
		return null;

	}

	public boolean next() throws JRException {

		index++;
		if (jrConsShipNotesDetail != null && index < jrConsShipNotesDetail.size()) {
			return true;
		}
		return false;
	}
}
