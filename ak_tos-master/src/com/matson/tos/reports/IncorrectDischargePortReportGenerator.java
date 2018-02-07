package com.matson.tos.reports;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import weblogic.auddi.util.Logger;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import com.matson.cas.refdata.mapping.TosRdsDataFinalMt;
import com.matson.cas.refdata.mapping.TosStowPlanCntrMt;
import com.matson.tos.dao.NewReportVesselDao;
import com.matson.tos.dao.NewVesselDao;

/* This class gets the records from DB for the Incorrect Discharge Port Report and stores it in the list. 
 * 
 * 	Srno	Date			AuthorName				Change Description
 * 	A1		10/8/2012		Meena Kumari			Initial creation 
 *  A2		10/18/2012		Meena Kumari			Added more queries 
 *  A3		02/13/2013		Karthik Rajendran		ReConstructed the report: By comparing disch port bw stoplan and RDS final table
 *  A4		04/01/2013		Karthik Rajendran		Added: MIS containers(hazOpenCloseFlag=M) should not be included.
 *  A5		04/12/2013		Karthik Rajendran		Added: Sorting on Container number
 */

public class IncorrectDischargePortReportGenerator implements JRDataSource{

	ArrayList<TosRdsDataFinalMt> jrIncorrectDiscPortDetail = new ArrayList<TosRdsDataFinalMt>();
	HashMap<Long, String> hmOriginalDischargePort = new HashMap<Long, String>(); 

	int index = -1;

	public IncorrectDischargePortReportGenerator(String Vesvoy) {
		ArrayList<TosRdsDataFinalMt> rdsFinalDataList = NewReportVesselDao.getRdsDataFinalForVesvoy(Vesvoy);
		ArrayList<TosStowPlanCntrMt> stowPlanList = NewReportVesselDao.getStowPlanForVesvoy(Vesvoy);
		if(rdsFinalDataList!=null && stowPlanList!=null)
		{
			for(int i=0; i<rdsFinalDataList.size(); i++)
			{
				TosRdsDataFinalMt rdsDF = rdsFinalDataList.get(i);
				String rdsCtrno = rdsDF.getContainerNumber();
				String rdsDischPort = rdsDF.getDischargePort();
				String rdsCd = rdsDF.getCheckDigit();
				rdsCd = rdsCd==null?"X":rdsCd;
				String rdsDs = rdsDF.getDs();
				String rdsBooking = rdsDF.getBookingNumber();
				String rdsTrade = rdsDF.getTrade();
				String rdsDport = rdsDF.getDport();
				String rdsLoadPort = rdsDF.getLoadPort();
				String stowCtrno = null;
				String stowDischPort = null;
				String hazOpenCloseFlag = rdsDF.getHazardousOpenCloseFlag();
				String grpCode = "";
				/*if (rdsDF.getTypeCode()!=null)
						grpCode = rdsDF.getTypeCode().substring(6,8);*/
				//if ("GB".equalsIgnoreCase(grpCode) || "GR".equalsIgnoreCase(grpCode) || "GE".equalsIgnoreCase(grpCode))
				if (rdsDF.getTypeCode()!=null && (rdsDF.getTypeCode().endsWith("GB") || rdsDF.getTypeCode().endsWith("GR") || rdsDF.getTypeCode().endsWith("GE")))
					continue;
				if(hazOpenCloseFlag!=null && hazOpenCloseFlag.equals("M"))
					continue;
				boolean cntrFound = false;
				for(int s=0; s<stowPlanList.size(); s++)
				{
					TosStowPlanCntrMt stowP = stowPlanList.get(s);
					stowCtrno = stowP.getContainerNumber();
					stowDischPort = stowP.getDischargePort();
					if(rdsCtrno.equalsIgnoreCase(stowCtrno))
					{
						cntrFound = true;
						break;
					}
				}
				if(cntrFound)
				{
					if(stowDischPort!=null && !stowDischPort.equals(rdsDischPort))
					{
						TosRdsDataFinalMt newRdsD = new TosRdsDataFinalMt();
						String temp = rdsCtrno+rdsCd;
						temp = temp.replace("MATU", "");
						newRdsD.setContainerNumber(temp);
						newRdsD.setDs(rdsDs);
						newRdsD.setDport(rdsDport);
						newRdsD.setLoadPort(rdsLoadPort);
						newRdsD.setTrade(rdsTrade);
						newRdsD.setBookingNumber(rdsBooking);
						newRdsD.setDischargePort(rdsDischPort);
						newRdsD.setRetPort(stowDischPort); // For report display: Setting the stowplan disch port to display it as original disch port
						jrIncorrectDiscPortDetail.add(newRdsD);
					}
				}
			}
		}
		Collections.sort(jrIncorrectDiscPortDetail, new Comparator<TosRdsDataFinalMt>(){
		     public int compare(TosRdsDataFinalMt rds1, TosRdsDataFinalMt rds2){
		    	return rds1.getContainerNumber().compareTo(rds2.getContainerNumber());
		     }
		});
	}

	public Object getFieldValue(JRField jrField) throws JRException {

		TosRdsDataFinalMt incorrectDiscPort = (TosRdsDataFinalMt) jrIncorrectDiscPortDetail.get(index);
		if (incorrectDiscPort == null) {
			return null;
		}
		if (jrField.getName().equals("ContainerNumber")) {
			return incorrectDiscPort.getContainerNumber();
		}
		if (jrField.getName().equals("Ds")) {
			return incorrectDiscPort.getDs();
		}
		if (jrField.getName().equals("BookingNumber")) {
			return incorrectDiscPort.getBookingNumber();
		}
		if (jrField.getName().equals("Trade")) {
			return incorrectDiscPort.getTrade();
		}
		if (jrField.getName().equals("DPort")) {
			return incorrectDiscPort.getDport();
		}	
		if (jrField.getName().equals("DischargePort1")) {
			return incorrectDiscPort.getRetPort();
		}
		if (jrField.getName().equals("DischargePort2")) {
			return incorrectDiscPort.getDischargePort();
		}
		if (jrField.getName().equals("LoadPort")) {
			return incorrectDiscPort.getLoadPort();
		}

		return null;

	}

	public boolean next() throws JRException {

		index++;
		if (jrIncorrectDiscPortDetail != null && index < jrIncorrectDiscPortDetail.size()) {
			return true;
		}
		return false;
	}
}





