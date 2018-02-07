package com.matson.tos.reports;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import antlr.StringUtils;

import com.matson.cas.refdata.mapping.TosRdsDataFinalMt;
import com.matson.tos.dao.NewReportVesselDao;
import com.matson.tos.dao.TosLookup;

/* This class gets the records from DB for the Destination Ports Discrepancies Report and stores it in the list. 
 * 
 * 	Srno	Date			AuthorName				Change Description
 * 	A1		05/13/2014		Raghu Pattangi						Initial creation 
 * 	
 */
public class InvalidNotifyPartyReportGenerator implements JRDataSource{
	private static Logger logger = Logger
			.getLogger(InvalidNotifyPartyReportGenerator.class);
	ArrayList<TosRdsDataFinalMt> jrnotifyDetail = new ArrayList<TosRdsDataFinalMt>();
	int index = -1;
	TosLookup lookup = null;
	
	public InvalidNotifyPartyReportGenerator(String Vesvoy) {
		logger.info("InvalidNotifyPartyReportGenerator  begin "+Vesvoy);
		List<?> notifyList = NewReportVesselDao.getNotifyPartyForInvalidNPReport(Vesvoy);	
		String notPartyStr ="";
		String notParty ="";
		ArrayList<String> notifypartyList = null;
		ArrayList<String> invalidNotifyPartyList = new ArrayList<String>();
		ArrayList<String> validNotifyPartyList = null;
		if ((notifyList == null ) || (notifyList.size() <= 0)) { // no data 
			return;
		}else {
			notifypartyList = new ArrayList<String>();
		}
		for (int i = 0; i < notifyList.size(); i++) {
			TosRdsDataFinalMt notifyRDSData = (TosRdsDataFinalMt)notifyList.get(i);
			notParty = notifyRDSData.getNotifyParty();
			//logger.info("notParty  :"+notParty);
			if ("".equalsIgnoreCase(notPartyStr)) {
				notPartyStr = "'"+notParty+"'";
				notifypartyList.add(notParty);
			}else {
				if (!notifypartyList.contains(notParty)) {
					notPartyStr = notPartyStr+",'"+notParty+"'";
					notifypartyList.add(notParty);
				}
			}
		}
		logger.info("Notify party list "+notPartyStr);
	
		if ("".equalsIgnoreCase(notPartyStr))
				return;

		try {
			if (lookup == null)
				lookup = new TosLookup();
			validNotifyPartyList = lookup.getValidTruckerList(notifypartyList);
		} catch(Exception e) {
			logger.error("Error: while retreiving restowed containers from TOS ", e);
		} finally {
			if (lookup!=null) {
				lookup.close();
				lookup = null;
			}
		}
		//can be deleted
		if (validNotifyPartyList.size() <=0) {
			logger.info("VALID NOTIFY PARTY LIST IS EMPTY");
		}
/*		
		if (validNotifyPartyList !=null && validNotifyPartyList.size() >= 1) {
			for (int k =0;k<validNotifyPartyList.size();k++) {
				logger.info("Valid trucker "+(String)validNotifyPartyList.get(k));
			}
		}*/
		
		for (int l=0;l<notifypartyList.size();l++) {
			if (!validNotifyPartyList.contains((String)notifypartyList.get(l))) {
				invalidNotifyPartyList.add((String)notifypartyList.get(l));
			}
		}
		logger.info("invalidNotifyPartyList  size is :"+invalidNotifyPartyList.size());
		if (invalidNotifyPartyList !=null && invalidNotifyPartyList.size() >= 1) {
			logger.info("Printing the report ");
			List<?> inValidNPList = NewReportVesselDao.getInvalidNotifyPartyReportGeneratorList(Vesvoy, invalidNotifyPartyList);
			if (inValidNPList==null || inValidNPList.size() <= 0) 
				return;
			for (int i = 0; i < inValidNPList.size(); i++) {
				TosRdsDataFinalMt jrTosRdsFinalData = new TosRdsDataFinalMt();
				String cn = ((TosRdsDataFinalMt)inValidNPList.get(i)).getContainerNumber();
				String cd = ((TosRdsDataFinalMt)inValidNPList.get(i)).getCheckDigit();
				cd = cd==null?"X":cd;
				cn = cn+cd;
				jrTosRdsFinalData.setContainerNumber(cn);
				jrTosRdsFinalData.setConsignee(((TosRdsDataFinalMt) inValidNPList.get(i)).getConsignee());
				jrTosRdsFinalData.setShipper(((TosRdsDataFinalMt) inValidNPList.get(i)).getShipper());
				String cargoNotes = ((TosRdsDataFinalMt)inValidNPList.get(i)).getCargoNotes();
				jrTosRdsFinalData.setTruck(((TosRdsDataFinalMt) inValidNPList.get(i)).getNotifyParty());
				jrTosRdsFinalData.setDport(((TosRdsDataFinalMt) inValidNPList.get(i)).getDport());
				jrnotifyDetail.add(jrTosRdsFinalData );
			}
		}else {
			return;
		}
	}
		

	public Object getFieldValue(JRField jrField) throws JRException {
		
		TosRdsDataFinalMt notifyReport = (TosRdsDataFinalMt) jrnotifyDetail.get(index);
		if (notifyReport == null) {
			return null;
		}
		if (jrField.getName().equals("ContainerNumber")) {
			return notifyReport.getContainerNumber();
		}
		if (jrField.getName().equals("Consignee")) {
			return notifyReport.getConsignee();
		}
		if (jrField.getName().equals("Shipper")) {
			return notifyReport.getShipper();
		}	
		if (jrField.getName().equals("Truck")) {
			return notifyReport.getTruck();
		}	
		if (jrField.getName().equals("Dport")) {
			return notifyReport.getDport();
		}	
			
		return null;
		
	}
	public boolean next() throws JRException {
		
		index++;
		if (jrnotifyDetail != null && index < jrnotifyDetail.size()) {
			return true;
		}
		return false;
	}
}