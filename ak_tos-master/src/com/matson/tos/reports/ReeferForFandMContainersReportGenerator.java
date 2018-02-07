package com.matson.tos.reports;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import com.matson.cas.refdata.mapping.TosRdsDataFinalMt;
import com.matson.tos.dao.NewReportVesselDao;
import com.matson.tos.dao.TosLookup;
import com.matson.tos.processor.NewvesProcessorHelper;

/* This class gets the records from DB for the Reefer For F and M Containers Report and stores it in the list. 
 * 
 * 	Srno	Date			AuthorName				Change Description
 * 	A1		10/11/2012		Meena Kumari			Initial creation 
 *  A2		09/05/2013		Karthik Rajendran		Added: Restow column and retrieving data from TOS lookup
 */

public class ReeferForFandMContainersReportGenerator implements JRDataSource{
	private static Logger logger = Logger.getLogger(ReeferForFandMContainersReportGenerator.class);
	ArrayList<TosRdsDataFinalMt> jrReeferDetail = new ArrayList<TosRdsDataFinalMt>();
	TosLookup lookup = null;
	int index = -1;

	public ReeferForFandMContainersReportGenerator(String Vesvoy) {

		List<?> reeferList = NewReportVesselDao.getReeferForFandMContainersReportGeneratorList(Vesvoy);	
		ArrayList<String> restowContainers = null;
		
		if ((reeferList == null ) || reeferList.size() <= 0) { // no data 
			return;
		} else {
			try {
				if (lookup == null)
					lookup = new TosLookup();
				restowContainers = lookup.getRestowMarkedContainersForVesVoy(Vesvoy);
			} catch(Exception e) {
				logger.error("Error: while retreiving restowed containers from TOS ", e);
			} finally {
				if (lookup!=null) {
					lookup.close();
					lookup = null;
				}
			}
		}
		for (int i = 0; i <  reeferList.size(); i++) {
			TosRdsDataFinalMt jrTosRdsFinalData = new TosRdsDataFinalMt();		

			String cn = ((TosRdsDataFinalMt)reeferList.get(i)).getContainerNumber();
			String cd = ((TosRdsDataFinalMt)reeferList.get(i)).getCheckDigit();
			String dportCntr = ((TosRdsDataFinalMt)reeferList.get(i)).getDport();
			cd = cd==null?"X":cd;
			cn = cn+cd;
			jrTosRdsFinalData.setContainerNumber(cn);
			String tempMeasurementUnit = ((TosRdsDataFinalMt) reeferList.get(i)).getTempMeasurementUnit();
			tempMeasurementUnit = tempMeasurementUnit==null?"":tempMeasurementUnit;
			jrTosRdsFinalData.setTemp(((TosRdsDataFinalMt) reeferList.get(i)).getTemp()+tempMeasurementUnit);
			jrTosRdsFinalData.setTempMeasurementUnit(((TosRdsDataFinalMt) reeferList.get(i)).getTempMeasurementUnit());
			jrTosRdsFinalData.setSrv(((TosRdsDataFinalMt) reeferList.get(i)).getSrv());
			jrTosRdsFinalData.setConsignee(((TosRdsDataFinalMt)reeferList.get(i)).getConsignee());
			jrTosRdsFinalData.setCargoNotes(((TosRdsDataFinalMt)reeferList.get(i)).getCargoNotes());

			jrTosRdsFinalData.setDir(((TosRdsDataFinalMt)reeferList.get(i)).getDir());
			jrTosRdsFinalData.setLocationStatus(((TosRdsDataFinalMt) reeferList.get(i)).getLocationStatus());
			jrTosRdsFinalData.setLoc(((TosRdsDataFinalMt) reeferList.get(i)).getLoc());
			jrTosRdsFinalData.setDport(((TosRdsDataFinalMt) reeferList.get(i)).getDport());
			jrTosRdsFinalData.setDischargePort(((TosRdsDataFinalMt) reeferList.get(i)).getDischargePort());
			jrTosRdsFinalData.setVesvoy(((TosRdsDataFinalMt) reeferList.get(i)).getVesvoy());

			jrTosRdsFinalData.setDs(((TosRdsDataFinalMt) reeferList.get(i)).getDs());
			jrTosRdsFinalData.setShipper(((TosRdsDataFinalMt) reeferList.get(i)).getShipper());
			jrTosRdsFinalData.setTruck(((TosRdsDataFinalMt) reeferList.get(i)).getTruck());
			jrTosRdsFinalData.setTrade(((TosRdsDataFinalMt) reeferList.get(i)).getTrade());
			jrTosRdsFinalData.setTypeCode(((TosRdsDataFinalMt) reeferList.get(i)).getTypeCode());

			jrTosRdsFinalData.setOwner(((TosRdsDataFinalMt) reeferList.get(i)).getOwner());
			jrTosRdsFinalData.setAction(((TosRdsDataFinalMt) reeferList.get(i)).getAction());
			jrTosRdsFinalData.setRetPort(((TosRdsDataFinalMt) reeferList.get(i)).getRetPort());
			jrTosRdsFinalData.setAdate(((TosRdsDataFinalMt) reeferList.get(i)).getAdate());

			jrTosRdsFinalData.setAtime(((TosRdsDataFinalMt) reeferList.get(i)).getAtime());
			// Fixing BARGE column
			if (dportCntr != null
					&& (! (dportCntr.equals("HON") || dportCntr.equals("HIL") || dportCntr.equals("KAH")
							|| dportCntr.equals("LNI") || dportCntr.equals("MOL")
							|| dportCntr.equals("NAW") || dportCntr.equals("KHI")))) {
				HashMap<String, String> tosCarrMap = null;
				
				try {
					logger.info("dportCntr in reefer report "+cn+"==="+dportCntr);
					if (lookup==null)
						lookup = new TosLookup();
					tosCarrMap = lookup.getUnitCarrierDetails(cn);
					String tosObActual = "";
					if (tosCarrMap !=null) { 
						tosObActual = tosCarrMap.get("OB_ACTUAL");
						tosObActual = tosObActual==null ? "" : tosObActual;
						logger.info("tosObActual in reefer report "+tosObActual);
						jrTosRdsFinalData.setMisc1(tosObActual);
					}
				} catch(Exception e) {
					logger.error("Error: while retreiving unit details(OB_ACTUAL) from TOS ", e);
				} finally {
					if (lookup!=null) {
						lookup.close();
						lookup = null;
					}
				}
			}
			
			//End
			boolean isRestow = false;
			jrTosRdsFinalData.setStowRestrictionCode("");
			if(restowContainers!=null && restowContainers.size()>0) {
				for(int r=0; r<restowContainers.size(); r++) {
					if(restowContainers.get(r).contains(((TosRdsDataFinalMt)reeferList.get(i)).getContainerNumber())) {
						jrTosRdsFinalData.setStowRestrictionCode("Restow");
						isRestow = true;
						break;
					}
				}
			}
			if(jrTosRdsFinalData.getDischargePort()!=null && jrTosRdsFinalData.getDischargePort().equalsIgnoreCase("HON")) {
				jrReeferDetail.add(jrTosRdsFinalData);
			}
			else if(jrTosRdsFinalData.getDischargePort()!=null && !jrTosRdsFinalData.getDischargePort().equalsIgnoreCase("HON") && isRestow) {
				jrReeferDetail.add(jrTosRdsFinalData);
			}
		}  		

	}

	public Object getFieldValue(JRField jrField) throws JRException {
		
		TosRdsDataFinalMt reeferCtr = (TosRdsDataFinalMt) jrReeferDetail.get(index);
		if (reeferCtr == null) {
			return null;
		}
		if (jrField.getName().equals("ContainerNumber")) {
			return reeferCtr.getContainerNumber();
		}
		if (jrField.getName().equals("Temp")) {
			//return "20";
			return reeferCtr.getTemp();
		}	
		if (jrField.getName().equals("Comments")) {
			if(reeferCtr.getTempMeasurementUnit()==null || reeferCtr.getTempMeasurementUnit().equals("")){
				return "Temperature scale was not downloaded, verify the temperature";
			}
			else
				return "";
		}
		if (jrField.getName().equals("Srv")) {
			return reeferCtr.getSrv();
		}	
		if (jrField.getName().equals("Consignee")) {
			return reeferCtr.getConsignee();
		}
		if (jrField.getName().equals("CargoNotes")) {
			return reeferCtr.getCargoNotes();
		}	
		if (jrField.getName().equals("Dir")) {
			return reeferCtr.getDir();
		}	
		if (jrField.getName().equals("LS")) {
			return reeferCtr.getLocationStatus();
		}	
		if (jrField.getName().equals("Loc")) {
			return reeferCtr.getCell();
		}	
		if (jrField.getName().equals("Dport")) {
			return reeferCtr.getDport();
		}	
		if (jrField.getName().equals("Vesvoy")) {
			return reeferCtr.getVesvoy();
		}	
		if (jrField.getName().equals("Ds")) {
			return reeferCtr.getDs();
		}	
		if (jrField.getName().equals("Shipper")) {
			return reeferCtr.getShipper();
		}	
		if (jrField.getName().equals("Truck")) {
			return reeferCtr.getTruck();
		}	
		if (jrField.getName().equals("Trade")) {
			return reeferCtr.getTrade();
		}
		if (jrField.getName().equals("TypeCode")) {
			return reeferCtr.getTypeCode();
		}	
		if (jrField.getName().equals("Owner")) {
			return reeferCtr.getOwner();
		}	
		if (jrField.getName().equals("Action")) {
			return reeferCtr.getAction();
		}
		if (jrField.getName().equals("RetPort")) {
			return reeferCtr.getRetPort();
		}	
		if (jrField.getName().equals("Adate")) {
			//return "";
			return reeferCtr.getAdate().toString();
		}	
		if (jrField.getName().equals("Barge")) {
			return reeferCtr.getMisc1();
			//return reeferCtr.getBarge();//NO BARGE FIELD IN TABLE
		}	
		if (jrField.getName().equals("Atime")) {
			//return "";
			return reeferCtr.getAtime();
		}	
		//INYARD is  not set....
		if (jrField.getName().equals("RestowType")) {
			return reeferCtr.getStowRestrictionCode();
		}
		return null;
		
	}

	public boolean next() throws JRException {
		
		index++;
		if (jrReeferDetail != null && index < jrReeferDetail.size()) {
			return true;
		}
		return false;
	}

}

