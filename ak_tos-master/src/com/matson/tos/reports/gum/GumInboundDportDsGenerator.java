package com.matson.tos.reports.gum;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import com.matson.tos.dao.GumVesselReportDao;

/* This class gets the records from DB for the Mix Port Container Report and stores it in the list. 
 * 
 * 	Srno	Date			AuthorName				Change Description
 *  1		03/29/2013		Karthik Rajendran		Class created - retrieving all inbounds for Guam
 *  2		04/03/2013		Karthik Rajendran		Removed: Date parameter
 */

public class GumInboundDportDsGenerator implements JRDataSource{

	List<?> gumInboundList = null;

	int index = -1;

	public GumInboundDportDsGenerator(String Vesvoy) {

		gumInboundList = GumVesselReportDao.getGuamInboundByDportDs(Vesvoy);

		if ((gumInboundList == null) || (gumInboundList.size() <= 0)) { // no data 
			return;
		} 	
		
	}
	public Object getFieldValue(JRField jrField) throws JRException {

		Object[] obj =  (Object[]) gumInboundList.get(index);
		if (obj == null) {
			return null;
		}
		if (jrField.getName().equals("Dport")) {
			return obj[0].toString();
		}
		if (jrField.getName().equals("Ds")) {
			return obj[1]==null?"MTY":obj[1].toString();
		}
		if (jrField.getName().equals("Dscount")) {
			return obj[2].toString();
		}
		return null;

	}

	public boolean next() throws JRException {

		index++;
		if (gumInboundList != null && index < gumInboundList.size()) {
			return true;
		}
		return false;
	}
}

