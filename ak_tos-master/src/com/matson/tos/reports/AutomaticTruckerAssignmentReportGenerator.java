package com.matson.tos.reports;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.log4j.Logger;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import com.matson.cas.refdata.mapping.TosRdsDataFinalMt;
import com.matson.tos.dao.NewReportVesselDao;
import com.matson.tos.dao.TosLookup;

/* This class gets the records from DB for the Destination Ports Discrepancies Report and stores it in the list. 
 * 
 * 	Srno	Date			AuthorName				Change Description
 * 	A1		03/11/2014		Azhar Naafiya						Initial creation 
 * 	
 */
public class AutomaticTruckerAssignmentReportGenerator implements JRDataSource {
	private static Logger logger = Logger
			.getLogger(AutomaticTruckerAssignmentReportGenerator.class);
	ArrayList<TosRdsDataFinalMt> jrautoTruckAssignDetail = new ArrayList<TosRdsDataFinalMt>();
	private static TosLookup lookUp = null;
	int index = -1;

	public AutomaticTruckerAssignmentReportGenerator(String Vesvoy) {
		List<?> autoTruckAssignList = NewReportVesselDao.getAutomaticTruckerAssignmentReportGeneratorList(Vesvoy);

		if ((autoTruckAssignList == null) || (autoTruckAssignList.size() <= 0)) { // no	// data
			return;
		}
		HashMap<String, String> cneeAgentMap = new HashMap<String, String>();
		for (int i = 0; i < autoTruckAssignList.size(); i++) {
			TosRdsDataFinalMt jrTosRdsFinalData = new TosRdsDataFinalMt();
			jrTosRdsFinalData.setConsignee(((TosRdsDataFinalMt) autoTruckAssignList.get(i)).getConsignee());
			jrTosRdsFinalData.setTruck(((TosRdsDataFinalMt) autoTruckAssignList.get(i)).getTruck());
			jrTosRdsFinalData.setCargoNotes(((TosRdsDataFinalMt) autoTruckAssignList.get(i)).getCargoNotes());
			jrTosRdsFinalData.setDport(((TosRdsDataFinalMt) autoTruckAssignList.get(i)).getDport());
			jrTosRdsFinalData.setDsc(((TosRdsDataFinalMt) autoTruckAssignList.get(i)).getDsc());
			if(jrTosRdsFinalData.getCargoNotes()!=null && jrTosRdsFinalData.getCargoNotes().contains("N/P")) {
				if(cneeAgentMap.containsKey(jrTosRdsFinalData.getConsignee()))
					continue;
				String agent = null;
				try {
					if(lookUp == null)
						lookUp = new TosLookup();
					List truckerList = lookUp.getTrucker(jrTosRdsFinalData.getConsignee());
					if (truckerList != null)
					{
						logger.info("truckerList size "
								+ truckerList.size());
						if (truckerList.size() == 1)
						{
							ArrayList truckerCon = (ArrayList) truckerList
									.get(0);
							String trukcerName = (String) truckerCon.get(0);
							logger.info("trukcerName  " + trukcerName);
							String[] truckerArr = trukcerName.split("-");
							logger.info("truckerArr :" + truckerArr[0]
									+ " " + truckerArr[1]);
							String truck = truckerArr[0];
							if ("O/P".equalsIgnoreCase(truck))
								agent = truck;
							else
							{
								if ("HON"
										.equalsIgnoreCase(jrTosRdsFinalData.getDport())
										|| "S".equals(jrTosRdsFinalData.getDsc()))
									agent = truck;
							}
							logger.info("trucker " + agent);
						}
						else
						{
							ArrayList truckerCon = null;
							String trukcerName = null;
							boolean isOp = false;
							boolean isTruckerSet = false;
							String[] truckerArr = null;
							for (int l = 0; l < truckerList.size(); l++)
							{
								truckerCon = (ArrayList) truckerList.get(l);
								trukcerName = (String) truckerCon.get(0);
								if (trukcerName.contains("O/P"))
								{
									isOp = true;
									if (isTruckerSet)
									{
										break;
									}
								}
								else
								{
									if (!isTruckerSet)
									{
										truckerArr = trukcerName.split("-");
										logger.info("truckerArr :"
												+ truckerArr[0] + " "
												+ truckerArr[1]);
										isTruckerSet = true;
									}
								}
							}
							logger.info("trukcerName else " + trukcerName
									+ " " + isOp + " " + isTruckerSet);

							if (isOp && truckerArr != null)
								agent = truckerArr[0];
							logger.info("trucker " + agent);

						}
					}
					logger.info("Assigning trucker from Tos:" + agent);
					if(agent!=null) {
						jrTosRdsFinalData.setTruck(agent);
						jrautoTruckAssignDetail.add(jrTosRdsFinalData);
						cneeAgentMap.put(jrTosRdsFinalData.getConsignee(), agent);
					}
				}
				catch(Exception e) {
					logger.error("Error in retreiving agent from TOS for "+jrTosRdsFinalData.getConsignee(), e);
				}
				finally {
					if(lookUp!=null) {
						lookUp.close();lookUp=null;
					}
				}
			}
			else {
				if(cneeAgentMap.containsKey(jrTosRdsFinalData.getConsignee()))
					continue;
				jrautoTruckAssignDetail.add(jrTosRdsFinalData);
				cneeAgentMap.put(jrTosRdsFinalData.getConsignee(), jrTosRdsFinalData.getTruck());
			}
		}
	}

	public Object getFieldValue(JRField jrField) throws JRException {

		TosRdsDataFinalMt autoTruckAssignReport = (TosRdsDataFinalMt) jrautoTruckAssignDetail
				.get(index);
		if (autoTruckAssignReport == null) {
			return null;
		}
		if (jrField.getName().equals("Consignee")) {
			return autoTruckAssignReport.getConsignee();
		}

		if (jrField.getName().equals("Truck")) {
			return autoTruckAssignReport.getTruck();
		}

		return null;

	}

	public boolean next() throws JRException {

		index++;
		if (jrautoTruckAssignDetail != null
				&& index < jrautoTruckAssignDetail.size()) {
			return true;
		}
		return false;
	}
}
