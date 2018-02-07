package com.matson.tos.reports.barge;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import com.matson.cas.refdata.mapping.TosStowPlanChassisMt;
import com.matson.cas.refdata.mapping.TosStowPlanCntrMt;
import com.matson.tos.dao.NewReportVesselDao;


public class BargeStowageSummaryReportGenerator implements JRDataSource {
	public int totCtrsLocal = 0;
	public int totCtrs = 0;
	public int f20Dry = 0;
	public int f24Dry = 0;
	public int f24Reefer = 0;
	public int f40Dry = 0;
	public int f40Reefer = 0;
	public int f45Van = 0;
	public int aut24Dry = 0;
	public int aut24AF = 0;
	public int aut40Dry = 0;
	public int aut40AF = 0;
	public int aut45Dry = 0;
	public int aut40ALS = 0;
	public int totFreight = 0;
	public int totMty = 0;
	public int totAut = 0;
	public int totBare = 0;
	public int totBareLocal = 0;
	int index = -1;
	public String depPort = "";
	public String disPort = "";
	ArrayList<TosStowPlanCntrMt> jrMtyCtrDetail = new ArrayList<TosStowPlanCntrMt>();
	//
	
	public BargeStowageSummaryReportGenerator(String vvd)
	{
		depPort = NewReportVesselDao.getLoadPort(vvd);
		disPort = "HON";
		ArrayList<TosStowPlanCntrMt> cntrList = null;
		ArrayList<TosStowPlanChassisMt> chsList = null;
		cntrList = NewReportVesselDao.getBargeStowageSummary(vvd);		

		if(cntrList!=null) {
			System.out.println("cntrList size before : "+cntrList.size());
			totCtrs = cntrList.size();
			totCtrsLocal = cntrList.size();
		
			for(Iterator<TosStowPlanCntrMt> itr = cntrList.iterator(); itr.hasNext();)
			{
				TosStowPlanCntrMt cntrMt = itr.next();
				TosStowPlanCntrMt resultCntr = new TosStowPlanCntrMt();
				int length = Integer.parseInt(cntrMt.getTypeCode().substring(1, 3));
				System.out.println("TEST :"+cntrMt.getContainerNumber()+" "+cntrMt.getDs()+" "+cntrMt.getTypeCode()+" " +length+" "+cntrMt.getShipper()+" "+cntrMt.getDir() );
				String ds = cntrMt.getDs();
				ds = ds==null?"":ds;
				if ("CY".equalsIgnoreCase(ds) && "D40".equalsIgnoreCase(cntrMt.getTypeCode().substring(0, 3)) &&
						(cntrMt.getShipper().contains("GENERAL MOTORS") || cntrMt.getShipper().contains("AUTO LOGISTICS SOLU") )) {
					resultCntr.setMisc3("3");
					resultCntr.setShipper("Auto Carrying Units");
					resultCntr.setComments("40' ALS");
				}
				else if (!"AUT".equalsIgnoreCase(ds) && !"MTY".equalsIgnoreCase(cntrMt.getDir())) {
					resultCntr.setMisc3("1");
					resultCntr.setShipper("Freight Loads");
					
					if ("R".equalsIgnoreCase(cntrMt.getTypeCode().substring(0, 1))) {
						if (length < 25)
							resultCntr.setComments("24' Reefers");
						else 
							resultCntr.setComments("40' Reefers");
					} else if (length < 21){
						resultCntr.setComments("20' Drys/Tanks");
					} else if (length < 25 ){
						resultCntr.setComments("24' Drys");
					} else if (length < 41) {
						resultCntr.setComments("40' Drys");
					} else if (length > 40) {
						resultCntr.setComments("45' Vans/Drys");
					}
				} else if ("AUT".equalsIgnoreCase(ds)) {
					resultCntr.setMisc3("3");
					resultCntr.setShipper("Auto Carrying Units");
					if ("A".equalsIgnoreCase(cntrMt.getTypeCode().substring(0, 1))) {
						if (length < 25) {
							resultCntr.setComments("24' A/F");
						} else {
							resultCntr.setComments("40' A/F");
						}
					} else {
						if (length < 25){
							resultCntr.setComments("24' Drys");
						} else if (length < 41) {
							resultCntr.setComments("40' Drys");
						} else if (length > 40) {
							resultCntr.setComments("45' Dry");
						}
					}
				} else {
					resultCntr.setMisc3("2");
					resultCntr.setShipper("MTY");
					resultCntr.setComments("MTY");
				}
					
				System.out.println(" Barge report :"+resultCntr.getShipper()+" - "+resultCntr.getComments()+" - "+cntrMt.getContainerNumber()+" - "+cntrMt.getTypeCode());
				jrMtyCtrDetail.add(resultCntr);
			}
			chsList = NewReportVesselDao.getBareChassisDataForCyLines(vvd);
			if(chsList!=null) {
				totBare = chsList.size();
				totBareLocal = chsList.size();
				// Add all the bare chassis records to the container list if any
				/*for(int b=0; b<chsList.size(); b++)
				{
					TosStowPlanChassisMt chsMt = chsList.get(b);
					TosStowPlanCntrMt cntrMt = new TosStowPlanCntrMt();
					cntrMt.setContainerNumber(chsMt.getChassisNumber());
					cntrMt.setShipper("Bare Chas");
					cntrMt.setMisc1(chsMt.getVesvoy());
					cntrMt.setLoadPort(chsMt.getDport());
					cntrList.add(b, cntrMt);
					jrMtyCtrDetail.add(cntrMt);
				}
				System.out.println("cntrList size after : "+cntrList.size());
				System.out.println("Bare chassis records added : " + chsList.size());*/
				
			}
			// Get all required counts
			for(int i=0; i<jrMtyCtrDetail.size(); i++)
			{
				TosStowPlanCntrMt stowCt = jrMtyCtrDetail.get(i);
				String shipper = stowCt.getShipper();
				String comments = stowCt.getComments();
				if(shipper.equals("Freight Loads"))
				{
					totFreight = totFreight + 1;
					if (comments.equals("20' Drys/Tanks"))
						f20Dry = f20Dry + 1;
					else if (comments.equals("24' Drys"))
						f24Dry = f24Dry + 1;
					else if (comments.equals("24' Reefers"))
						f24Reefer = f24Reefer + 1;
					else if (comments.equals("40' Drys"))
						f40Dry = f40Dry + 1;
					else if (comments.equals("40' Reefers"))
						f40Reefer = f40Reefer + 1;
					else if (comments.equals("45' Vans/Drys"))
						f45Van = f45Van + 1;
				}
				else if(shipper.equals("MTY"))
				{
					totMty = totMty + 1;					
				}
				else if(shipper.equals("Auto Carrying Units"))
				{
					totAut = totAut + 1;
					if (comments.equals("24' Dry"))
						aut24Dry = aut24Dry + 1;
					else if (comments.equals("24' A/F"))
						aut24AF = aut24AF + 1;
					else if (comments.equals("40' Drys"))
						aut40Dry = aut40Dry + 1;
					else if (comments.equals("40' A/F"))
						aut40AF = aut40AF + 1;
					else if (comments.equals("40' ALS"))
						aut40ALS = aut40ALS + 1;
					else if (comments.equals("45' Dry"))
						aut45Dry = aut45Dry + 1;
				}
			}
		}
	}

	public Object getFieldValue(JRField jrField) throws JRException {

		TosStowPlanCntrMt mtyCtr = (TosStowPlanCntrMt) jrMtyCtrDetail.get(index);
		if (mtyCtr == null) {
			return null;
		}
		if (jrField.getName().equals("shipper")) {
			return mtyCtr.getShipper();
		}
		if (jrField.getName().equals("comments")) {
			return mtyCtr.getComments();
		}	

		return null;
		
	}

	public boolean next() throws JRException {
		
		index++;
		if (jrMtyCtrDetail != null && index < jrMtyCtrDetail.size()) {
			return true;
		}
		return false;
	}

}
