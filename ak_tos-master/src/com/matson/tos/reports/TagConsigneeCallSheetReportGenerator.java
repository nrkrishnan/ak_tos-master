package com.matson.tos.reports;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import com.matson.cas.refdata.mapping.TosRdsDataFinalMt;
import com.matson.tos.dao.NewReportVesselDao;

import org.apache.log4j.Logger;
import com.matson.tos.dao.TosLookup;


/* This class gets the records from DB for the Tag Consignee Call Sheet Report and stores it in the list. 
 * 
 * 	Srno	Date			AuthorName				Change Description
 * 	A1		10/11/2012		Meena Kumari			Initial creation 
 *  A2		08/14/2014		Raghu Iyer				Added logic to add highcube to type code
 */

public class TagConsigneeCallSheetReportGenerator implements JRDataSource{

	ArrayList<TosRdsDataFinalMt> jrTagDetail = new ArrayList<TosRdsDataFinalMt>();
	private static Logger	logger	= Logger.getLogger(TagConsigneeCallSheetReportGenerator.class);
	private static TosLookup lookUp	= null;

	int index = -1;

	public TagConsigneeCallSheetReportGenerator(String Vesvoy) {

		List<?> tagList = NewReportVesselDao.getTagConsigneeCallSheetReportGeneratorList(Vesvoy);

		if ( tagList.size() <= 0) { // no data 
			return;
		}
		for (int i = 0; i <  tagList.size(); i++) {
			TosRdsDataFinalMt jrTosRdsFinalData = new TosRdsDataFinalMt();			
			String cn = ((TosRdsDataFinalMt)tagList.get(i)).getContainerNumber();
			String cntr = cn;
			String cd = ((TosRdsDataFinalMt)tagList.get(i)).getCheckDigit();
			cd = cd==null?"X":cd;
			cn = cn+cd;
			jrTosRdsFinalData.setContainerNumber(cn);
			jrTosRdsFinalData.setDport(((TosRdsDataFinalMt) tagList.get(i)).getDport());
			jrTosRdsFinalData.setCrstatus(((TosRdsDataFinalMt) tagList.get(i)).getCrstatus());
			jrTosRdsFinalData.setTemp(((TosRdsDataFinalMt) tagList.get(i)).getTemp());
			jrTosRdsFinalData.setCargoNotes(((TosRdsDataFinalMt) tagList.get(i)).getCargoNotes());
			jrTosRdsFinalData.setCweight(((TosRdsDataFinalMt) tagList.get(i)).getCweight());
			//String typeCode = ((TosRdsDataFinalMt) tagList.get(i)).getTypeCode();
			
			String typeCode = "";
			try
			{
				if (lookUp == null)
					lookUp = new TosLookup();
				HashMap<String, String> typeCodeMap = lookUp.getEquipmentType(cntr , cd);
				if (typeCodeMap != null && typeCodeMap.size() > 0)
				{
					typeCode = typeCodeMap.get("equipmentType");
				}
			}
			catch (Exception e)
			{
				logger.error("TypeCode Lookup error:", e);
			} finally {
				if (lookUp!=null) {
					lookUp.close();
					lookUp = null;
				}
			}

			jrTosRdsFinalData.setTypeCode(typeCode);			
			jrTosRdsFinalData.setShipper(((TosRdsDataFinalMt) tagList.get(i)).getShipper());
			jrTosRdsFinalData.setBookingNumber(((TosRdsDataFinalMt) tagList.get(i)).getBookingNumber());
			jrTosRdsFinalData.setConsignee(((TosRdsDataFinalMt) tagList.get(i)).getConsignee());
			jrTagDetail.add(jrTosRdsFinalData );					
		}  		

	}

	public Object getFieldValue(JRField jrField) throws JRException {
		
		TosRdsDataFinalMt  tagConsignee = (TosRdsDataFinalMt) jrTagDetail.get(index);
		if (tagConsignee == null) {
			return null;
		}
		if (jrField.getName().equals("ContainerNumber")) {
			return tagConsignee.getContainerNumber();
		}
		if (jrField.getName().equals("Dport")) {
			return tagConsignee.getDport();
		}
		if (jrField.getName().equals("Crstatus")) {
			return tagConsignee.getCrstatus();
		}
		if (jrField.getName().equals("Temp")) {
			return tagConsignee.getTemp();
		}
		if (jrField.getName().equals("CargoNotes")) {
			return tagConsignee.getCargoNotes();
		}
		if (jrField.getName().equals("Weight")) {
			return tagConsignee.getCweight();
		}
		if (jrField.getName().equals("Type")) {
			return tagConsignee.getTypeCode();
		}
		if (jrField.getName().equals("Shipper")) {
			return tagConsignee.getShipper();
		}
		if (jrField.getName().equals("Booking")) {
			return tagConsignee.getBookingNumber();
		}
		if (jrField.getName().equals("Consignee")) {
			return tagConsignee.getConsignee();
		}
		return null;
		
	}

	public boolean next() throws JRException {
		
		index++;
		if (jrTagDetail != null && index < jrTagDetail.size()) {
			return true;
		}
		return false;
	}

}

