package com.matson.tos.processor;

import org.apache.log4j.Logger;

import com.matson.cas.refdata.mapping.TosConsigneeTruckerMz;
import com.matson.cas.refdata.mapping.TosDestPodData;
import com.matson.cas.refdata.mapping.TosGumDcmMt;
import com.matson.cas.refdata.mapping.TosGumRdsDataFinalMt;
import com.matson.cas.refdata.mapping.TosGumRdsDataMt;
import com.matson.cas.refdata.mapping.TosGumStPlanCntrMt;
import com.matson.cas.refdata.mapping.TosGumStPlanHazMt;
import com.matson.cas.refdata.mapping.TosGumStPlanHoldMt;
import com.matson.cas.refdata.mapping.TosRdsDataFinalMt;
import com.matson.cas.refdata.mapping.TosRefData;
import com.matson.cas.erd.service.data.VesselVO;
import com.matson.tos.dao.NewVesselDaoGum;
import com.matson.tos.exception.NewVesselLogger;
import com.matson.tos.util.CalendarUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/*
 *
 * #		Date			Author						Description
 * ---		-----------		------------------			---------------------
 * 1		03/20/2013		Karthik Rajendran			Class file created from HON Newves CommonBusinessProcessor
 * 2		03/23/2013		Karthik Rajendran			Changed: consignee-AUTOMOBILE removed, rdd removed, update cnotes with stowplan haz description
 * 														Added: Remove SIT,TIT flags.
 * 3		03/27/2013		Karthik Rajendran			Added: assignTrucker() - Trucker code assignment only for GUM containers.
 * 4		03/29/2013		Karthik Rajendran			Changed: AvailableDate should be retrieved for GUM port.
 * 5		04/02/2013		Karthik Rajendran			Changed: combineShipmentNoCreditStatus check for dischargeport
 * 6		04/03/2013		Karthik Rajendran			Added: Set cargonotes=VEHICLES if its empty and commodity has AUTO
 * 7		04/04/2013		Karthik Rajendran			Changed: assignTrucker() - To have update new GUM consignees back into TosConsigneeTruckerMz table
 * 																					after automatic trucker update operation.
 * 8		04/08/2013		Karthik Rajendran			Added fix for: Duplicate insert of new consignee everytime the process runs.
 * 9		04/15/2013		Karthik Rajendran			Change: enabled Unapproved variances logic in updateConsignee()
 *
 *
 */


public class GumCommonBusinessProcessor extends CommonBusinessProcessor {

	private static Logger logger = Logger.getLogger(CommonBusinessProcessor.class);
	private static NewVesselLogger nvLogger = NewVesselLogger.getInstance();

	public static ArrayList<TosGumRdsDataMt> combineShipmentNoCreditStatus(ArrayList<TosGumRdsDataMt> rdsList)
	{
		ArrayList<TosGumRdsDataMt> output = new ArrayList<TosGumRdsDataMt>();
		ArrayList<TosGumRdsDataMt> tempList = rdsList;
		String oldCtrno = "";
		for (int i=0; i<rdsList.size(); i++)
		{
			TosGumRdsDataMt rdsd = rdsList.get(i);
			String ctrno = rdsd.getCtrno();
			String oldDiscPort = rdsd.getDischargePort();
			String oldDport = rdsd.getDestinationPort();
			if(!oldCtrno.equalsIgnoreCase(ctrno))
			{
				StringBuilder sb = new StringBuilder();
				StringBuilder sbCStat = new StringBuilder();
				//String rdd = "";
				for (int j=0; j<tempList.size(); j++)
				{
					TosGumRdsDataMt temp = tempList.get(j);
					if(ctrno.equalsIgnoreCase(temp.getCtrno()))
					{
						// Concatenate all shippment numbers
						sb.append(temp.getShipno() + "-");
						//if there are duplicate values, then we should only take one value.
						//For example, if there are 4 records with V, H, V, H then we should only store VH but not VVHH
						// Modified by Karthik
						if(temp.getCreditStatus()!=null)
						{
							String cstatus = temp.getCreditStatus();
							if(sbCStat.indexOf(cstatus) == -1)
							{
								sbCStat.append(cstatus);
							}
						}
						/*// Check for RDD availability
						if(temp.getRdd()!=null && temp.getRdd().length()>0)
						{
							if(rdd.equals(""))
								rdd = temp.getRdd();
						}*/
						if(!temp.getDischargePort().equals(oldDiscPort)&&oldDiscPort.equals("HON"))
						{
							rdsd = temp;
						}
					}
				}
				rdsd.setShipno(sb.toString().substring(0, sb.length()-1));
				/*if(rdd.length()>0)
					rdsd.setRdd(rdd);
				System.out.println("RDD - "+rdd +" , "+ctrno);*/
				if(sbCStat.length()>0)
				{
					logger.info("Credit status : " + ctrno + " " + sbCStat.toString());
					rdsd.setCreditStatus(sbCStat.toString());
				}
				output.add(rdsd);
				oldCtrno = ctrno;
			}
		}
		return output;
	}

	// Method added to Fix for Unique Constraint violation
	public static ArrayList<TosGumRdsDataMt> eliminateDuplicatesInRDS(ArrayList<TosGumRdsDataMt> rdsList){

		List<TosGumRdsDataMt>  tempList =  rdsList;
		List<TosGumRdsDataMt>  outputList = new ArrayList<TosGumRdsDataMt>();

		for (int i = 0; i < rdsList.size(); i++){
			TosGumRdsDataMt rds =  rdsList.get(i);
			String cntrno = rds.getCtrno();
			int found = 0;

			for (int j = 0; j < tempList.size(); j++){
				TosGumRdsDataMt rdstemp =  tempList.get(j);
				String cntrnotemp = rdstemp.getCtrno();

				if(cntrno.equals(cntrnotemp)){
					found++;
				}
			}
			if(found == 1){
				outputList.add(rdsList.get(i));
			}

		}
		return (ArrayList<TosGumRdsDataMt>) outputList;

	}
	// Method added to Fix for Unique Constraint violation
	public static ArrayList<TosGumRdsDataFinalMt> eliminateDuplicatesInRDSFinal(ArrayList<TosGumRdsDataFinalMt> rdsFinalList){

		List<TosGumRdsDataFinalMt>  tempList =  rdsFinalList;
		List<TosGumRdsDataFinalMt>  outputList = new ArrayList<TosGumRdsDataFinalMt>();

		for (int i = 0; i < rdsFinalList.size(); i++){
			TosGumRdsDataFinalMt rdsFinal =  rdsFinalList.get(i);
			String cntrno = rdsFinal.getContainerNumber();
			int found = 0;

			for (int j = 0; j < tempList.size(); j++){
				TosGumRdsDataFinalMt rdsFinaltemp =  tempList.get(j);
				String cntrnotemp = rdsFinaltemp.getContainerNumber();

				if(cntrno.equals(cntrnotemp)){
					found++;
				}
			}
			if(found == 1){
				outputList.add(rdsFinalList.get(i));
			}

		}
		return (ArrayList<TosGumRdsDataFinalMt>) outputList;

	}
	public static ArrayList<TosGumRdsDataFinalMt> geartrashContainersTransformation(ArrayList<TosGumRdsDataFinalMt> rdsDataFinal)
	{
		for(int i=0; i<rdsDataFinal.size(); i++)
		{
			TosGumRdsDataFinalMt rdsFd = rdsDataFinal.get(i);
			String typeCode = rdsFd.getTypeCode();
			typeCode = typeCode==null?"":typeCode;
			String bookingNumber = rdsFd.getBookingNumber();
			bookingNumber = bookingNumber==null?"":bookingNumber;
			String consignee = rdsFd.getConsignee();
			consignee = consignee==null?"":consignee;
			String srstatus = rdsFd.getSrstatus();
			srstatus = srstatus==null?"":srstatus;
			if(typeCode.endsWith("GR") || typeCode.endsWith("GB") )
			{
				if(typeCode.endsWith("GR"))
				{
					rdsFd.setConsignee(consignee+"GEAR CONTAINERS");
				}
				else
				{
					rdsFd.setConsignee(consignee+"TRASH CONTAINERS");
				}
				if(srstatus.contains("DOC"))
					rdsFd.setSrstatus(srstatus.replaceAll("DOC", ""));
				else if(srstatus.contains("DOC "))
					rdsFd.setSrstatus(srstatus.replaceAll("DOC ", ""));
			}

			rdsDataFinal.set(i, rdsFd);
		}
		return rdsDataFinal;
	}
	public static ArrayList<TosGumRdsDataFinalMt> dsGumTransformations(ArrayList<TosGumRdsDataFinalMt> rdsDataFinal)
	{
		for(int i=0; i<rdsDataFinal.size(); i++)
		{
			TosGumRdsDataFinalMt rdsFd = rdsDataFinal.get(i);
			String ds = rdsFd.getDs();
			ds = ds==null?"":ds;
			String dir = rdsFd.getDir();
			dir = dir==null?"":dir;
			String shipper = rdsFd.getShipper();
			shipper = shipper==null?"":shipper;
			if(dir.equals("MTY"))
			{
				rdsFd.setDs("MTY");
			}
			if(shipper.equals("US GOVT MSC"))
			{
				rdsFd.setConsignee("US GOVT MSC");
				if(ds.equals("AUT"))
					rdsFd.setDs("ACY");
			}

			rdsDataFinal.set(i, rdsFd);
		}
		return rdsDataFinal;
	}
	public static String srstatusTransformation(TosGumRdsDataMt rdsData)
	{
		StringBuilder output = new StringBuilder();
		String cmdyAg = rdsData.getCmdyAg();
		String notify = rdsData.getNotify();
		String inbond = rdsData.getInbound();
		String creditStatus = rdsData.getCreditStatus();
		String dataSource = rdsData.getDatasource();
		String trade = rdsData.getTrade();
		if(cmdyAg != null && (cmdyAg.equalsIgnoreCase("Y") || cmdyAg.equalsIgnoreCase("I")))
			output = CommonBusinessProcessor.appendStatusString(output, "AG");
		if(notify != null && notify.length() > 0)
			output = CommonBusinessProcessor.appendStatusString(output, "ON");
		if(inbond != null && inbond.trim().length()>0)
		{
			if(trade != null && trade.equalsIgnoreCase("M"))
				output = CommonBusinessProcessor.appendStatusString(output, "INB");
			else
				output = CommonBusinessProcessor.appendStatusString(output, "CUS");
		}
		if(creditStatus != null && creditStatus.contains("C"))
			output = CommonBusinessProcessor.appendStatusString(output, "CC");
		if(creditStatus != null && (creditStatus.contains("H") || (creditStatus.equalsIgnoreCase("") &&
				dataSource!=null && dataSource.equalsIgnoreCase("E"))))
		{
			if(trade != null && (!trade.equalsIgnoreCase("G") && !trade.equalsIgnoreCase("H")))
				output = CommonBusinessProcessor.appendStatusString(output, "HP");
		}
		if(dataSource != null && dataSource.equalsIgnoreCase("E"))
		{
			if(trade != null && (!trade.equalsIgnoreCase("G") && !trade.equalsIgnoreCase("H")))
				output = CommonBusinessProcessor.appendStatusString(output, "DOC");
		}
		if(output.length()<1)
			output = CommonBusinessProcessor.appendStatusString(output, "OK");
		logger.info(rdsData.getCtrno()+" SRSTATUS TRANS: " + rdsData.getCtrno() + " " + output.toString());
		return output.toString();
	}
	public static ArrayList<TosGumRdsDataFinalMt> cargoNotesTransformation(ArrayList<TosGumRdsDataFinalMt> rdsDataFinal, ArrayList<TosGumStPlanCntrMt> ocrDataList)
	{
		logger.info("****cargoNotesTransformation begin****");
		for(int i=0; i<rdsDataFinal.size(); i++)
		{
			StringBuilder out = new StringBuilder();
			TosGumRdsDataFinalMt rdsFd = rdsDataFinal.get(i);
			String ctrno = rdsFd.getContainerNumber();
			String vv = rdsFd.getVesvoy();
			String dsc = rdsFd.getDsc();
			dsc = dsc==null?"":dsc;
			String cargoNotes = rdsFd.getCargoNotes();
			cargoNotes = cargoNotes==null?"":cargoNotes.trim();
			String dport = rdsFd.getDport();
			dport = dport==null?"":dport;

			//Adding oversize information to notes
			ArrayList<TosGumStPlanCntrMt> stowData = NewVesselDaoGum.getGumOCRDataForCtrno(ctrno, vv);
			if(stowData != null && stowData.size() > 0)
			{
				for(int j=0; j<stowData.size(); j++)
				{
					BigDecimal ovrHieght = stowData.get(j).getOversizeHeightInches();
					if(ovrHieght!=null && ovrHieght.doubleValue() > 0)
						out.append(" "+ovrHieght + "\" OH");
					BigDecimal ovrLeft = stowData.get(j).getOversizeLeftInches();
					if(ovrLeft!=null && ovrLeft.doubleValue() > 0)
						out.append(" "+ovrLeft + "\" OWL");
					BigDecimal ovrRight = stowData.get(j).getOversizeRightInches();
					if(ovrRight!=null && ovrRight.doubleValue() > 0)
						out.append(" "+ovrRight + "\" OWR");
					BigDecimal ovrFront = stowData.get(j).getOversizeFrontInches();
					if(ovrFront!=null && ovrFront.doubleValue() > 0)
						out.append(" "+ovrFront + "\" OLF");
					BigDecimal ovrRear = stowData.get(j).getOversizeRearInches();
					if(ovrRear!=null && ovrRear.doubleValue() > 0)
						out.append(" "+ovrRear + "\" OLB");
				}
			}
			if(out.length()>0 && cargoNotes.length()>0)
				cargoNotes = cargoNotes.trim() + " " + out.toString().trim();

			//Adding hazard description to notes only if DS<>CFS, DS<>AUT
			if(rdsFd.getDs()!=null && !rdsFd.getDs().equals("CFS") && !rdsFd.getDs().equals("AUT"))
			{
				String hazDesc = "";
				ArrayList<TosGumStPlanCntrMt> stowPlanlist = NewVesselDaoGum.getGumOCRDataForCtrno(ctrno, vv);
				if(stowPlanlist!=null && stowPlanlist.size()>0)
				{
					TosGumStPlanCntrMt stowCntrMt = stowPlanlist.get(0);
					String ctrNbr = stowCntrMt.getContainerNumber();
					if(ctrNbr != null && ctrNbr.equalsIgnoreCase(ctrno))
					{
						Set<TosGumStPlanHazMt> stowPlanHazMt = stowCntrMt.getTosGumStPlanHazMts();
						if(stowPlanHazMt!=null)
						{
							Iterator<TosGumStPlanHazMt> itrHaz = stowPlanHazMt.iterator();
							while(itrHaz.hasNext())
							{
								TosGumStPlanHazMt hz = itrHaz.next();
								if(hz!=null)
									hazDesc = hazDesc + hz.getDescription() + " ";
							}
						}
					}
				}
				if(hazDesc.length()>0)
					cargoNotes = cargoNotes.trim() + " " + hazDesc.trim();
			}
			if(cargoNotes.equals("")) { // If cargo notes is still empty then check for commodity AUTOS.
				TosGumStPlanCntrMt ocrD = null;
				for(int o=0; o<ocrDataList.size(); o++)
				{
					if(rdsFd.getContainerNumber().equals(ocrDataList.get(o).getContainerNumber()))
					{
						ocrD = ocrDataList.get(o);
						break;
					}
				}
				if(ocrD!=null)
				{
					if(ocrD.getCommodity()!=null && ocrD.getCommodity().toUpperCase().contains("AUTOS"))
						cargoNotes = "VEHICLES";
					else if(ocrD.getCommodity()!=null && ocrD.getCommodity().toUpperCase().contains("AUTO"))
						cargoNotes = "VEHICLE";
				}
				else
					logger.error("CargoNotesTransformation-->OCR data not found for "+ rdsFd.getContainerNumber());
			}
			rdsFd.setCargoNotes(cargoNotes.length()>65?cargoNotes.substring(0, 65).trim():cargoNotes.trim());
			logger.info(rdsFd.getContainerNumber()+"\t"+rdsFd.getCargoNotes());
			rdsDataFinal.set(i, rdsFd);
		}
		logger.info("****cargoNotesTransformation end****");
		return rdsDataFinal;
	}
	public static ArrayList<TosGumRdsDataFinalMt> commodityTransformation(ArrayList<TosGumRdsDataFinalMt> rdsDataFinal)
	{
		for(int i=0; i<rdsDataFinal.size(); i++)
		{
			TosGumRdsDataFinalMt rdsFd = rdsDataFinal.get(i);
			String ds = rdsFd.getDs();
			ds = ds==null?"":ds;
			String dsc = rdsFd.getDsc();
			dsc = dsc==null?"":dsc;
			String dport = rdsFd.getDport();
			dport = dport==null?"":dport;
			if(!dport.equals("GUM"))
			{
				if(ds.equals("AUT"))
					rdsFd.setCommodity("AUTO");
				else if(ds.equals("CFS"))
					rdsFd.setCommodity("CFS");
			}
			if(dsc.equals("S") || dsc.equals("T")) // Remove SIT,TIT flags
				rdsFd.setDsc(null);
			rdsDataFinal.set(i, rdsFd);
		}
		return rdsDataFinal;
	}
	public static ArrayList<TosGumRdsDataFinalMt> dscTransformation(ArrayList<TosGumRdsDataFinalMt> rdsDataFinal)
	{
		for(int i=0; i<rdsDataFinal.size(); i++)
		{
			TosGumRdsDataFinalMt rdsFd = rdsDataFinal.get(i);
			String cgnotes = rdsFd.getCargoNotes();
			cgnotes = cgnotes==null?"":cgnotes;
			/*if(cgnotes.contains("SIT") || cgnotes.contains("STOP IN TRANSIT"))
			{
				rdsFd.setDsc("S");
			}*/
			String dsc = rdsFd.getDsc();
			dsc = dsc==null?"":dsc;
			String hazOpenCloseF = rdsFd.getHazardousOpenCloseFlag();
			hazOpenCloseF = hazOpenCloseF==null?"":hazOpenCloseF;
			String shipper = rdsFd.getShipper();
			shipper = shipper==null?"":shipper;
			/*if(dsc.equals("S") && hazOpenCloseF.equals("H"))
			{
				rdsFd.setStowFlag("C");
				rdsFd.setCommodity("SIT");
			}
			if(dsc.equals("S") && !hazOpenCloseF.equals("H") && shipper.contains("US GOVT POSTMASTER"))
			{
				rdsFd.setStowFlag("C");
				rdsFd.setCommodity("SIT");
			}*/
			/*if(dsc.equals("T"))
				rdsFd.setDsc("");*/
			//DSC check for multiple shipment
			String ctrno = rdsFd.getContainerNumber();
			String ds = rdsFd.getDs();
			ds = ds==null?"":ds;
			String dport = rdsFd.getDport();
			dport = dport==null?"":dport;
			String vv = rdsFd.getVesvoy();
			ArrayList<TosGumRdsDataMt> rdsDataList = NewVesselDaoGum.getGumRdsDataForCtrno(ctrno, vv);
			if(rdsDataList!=null && rdsDataList.size()>1)
			{
				if((ds.equals("CFS") || ds.equals("CY")) && dport.equals("HON"))
					rdsFd.setDsc("P");
				else if((ds.equals("CFS") || ds.equals("CON")) && dport.equals("HON"))
					rdsFd.setDsc("P");
				else if((ds.equals("CY") || ds.equals("CON")) && dport.equals("HON"))
					rdsFd.setDsc("P");
				if(rdsFd.getDsc()==null || rdsFd.getDsc().equals(""))
				{
					TosGumRdsDataMt data1 = rdsDataList.get(0);
					TosGumRdsDataMt data2 = rdsDataList.get(1);
					String dport1 = data1.getDestinationPort();
					dport1 = dport1==null?"":dport1;
					String dport2 = data2.getDestinationPort();
					dport2 = dport2==null?"":dport2;
					if(dport1.equals(dport2))
						rdsFd.setDsc("A");
					else
						rdsFd.setDsc("C");
				}
			}
			rdsDataFinal.set(i, rdsFd);
		}
		return rdsDataFinal;
	}
	public static ArrayList<TosGumRdsDataFinalMt> fixMtys(ArrayList<TosGumRdsDataFinalMt> rdsDataFinal)
	{
		for(int i=0; i<rdsDataFinal.size(); i++)
		{
			TosGumRdsDataFinalMt rdsFd = rdsDataFinal.get(i);
			String dir = rdsFd.getDir();
			if(dir!=null && dir.equalsIgnoreCase("MTY"))
			{
				rdsFd.setCargoNotes(null);
				rdsFd.setConsignee(null);
				rdsFd.setHsf2(null);
				rdsFd.setHsf6(null);
				rdsFd.setSrstatus("OK");
			}
			rdsDataFinal.set(i, rdsFd);
		}
		return rdsDataFinal;
	}
	public static ArrayList<TosGumRdsDataFinalMt> processAutos(ArrayList<TosGumRdsDataFinalMt> rdsDataFinal)
	{
		for(int i=0; i<rdsDataFinal.size(); i++)
		{
			TosGumRdsDataFinalMt rdsFd = rdsDataFinal.get(i);
			String ds = rdsFd.getDs();
			if(ds!=null && ds.equals("AUT"))
			{
				rdsFd.setHsf2("V");
				rdsFd.setHsf6(null);
				String srstatus = srstatusHoldCheck(rdsFd);
				rdsFd.setSrstatus(srstatus);
				logger.info("PROCESS AUTOS - " + rdsFd.getContainerNumber() + " "+rdsFd.getDs()+" "+rdsFd.getCommodity()+" "+rdsFd.getHsf2()+" "+rdsFd.getHsf6());
			}
			rdsDataFinal.set(i, rdsFd);
		}
		return rdsDataFinal;
	}
	public static ArrayList<TosGumRdsDataFinalMt> updateConsignee(ArrayList<TosGumRdsDataFinalMt> rdsDataFinal, ArrayList<TosGumRdsDataMt> rdsData)
	{
		logger.info("**** updateConsignee ****");
		for(int i=0; i<rdsDataFinal.size(); i++)
		{
			TosGumRdsDataFinalMt rdsFd = rdsDataFinal.get(i);
			String cargoNotes = rdsFd.getCargoNotes();
			String ds = rdsFd.getDs();
			ds = ds==null?"":ds;
			String dir = rdsFd.getDir();
			dir = dir==null?"":dir;
			String typeCode = rdsFd.getTypeCode();
			typeCode = typeCode==null?"":typeCode;
			String consignee = rdsFd.getConsignee();
			consignee = consignee==null?"":consignee;
			String consigneeName = rdsFd.getConsigneeName();
			consigneeName = consigneeName==null?"":consigneeName;
			String dport = rdsFd.getDport();
			dport = dport==null?"":dport;
			String mixedDS = "";
			logger.info("CNTR:"+rdsFd.getContainerNumber()+"\tCNEE:"+consignee+"\tCNOTES:"+cargoNotes);
		/*	if("IN".equals(dir) && "CY".equals(ds) && !typeCode.endsWith("GB") && !typeCode.endsWith("GR"))
			{
				if(consignee.equals(""))
				{
					rdsFd.setConsignee("REQUIRES CS ACTION"); logger.info("REQCSACTION "+rdsFd.getContainerNumber());
				}
				else if(consignee.equals("UNKNOWN") || consignee.equals("WA") || consignee.equals("WILL ADVISE") || consignee.startsWith("X "))
				{
					rdsFd.setConsignee("REQUIRES CS ACTION - " + consignee); logger.info("REQCSACTION- "+rdsFd.getContainerNumber());
				}
			}*/

			/*if (ds!=null && "AUT".equals(ds)) {
				if (rdsFd.getShipper() !=null && rdsFd.getShipper().contains("FORD MOTOR COMPANY")) {
					if (rdsFd.getConsignee() !=null && (! (rdsFd.getConsignee().contains("BUDGET RENT") || rdsFd.getConsignee().contains("HERTZ RENT")
							|| rdsFd.getConsignee().contains("AVIS RENT") || rdsFd.getConsignee().contains("DOLLAR RENT")))){
						rdsFd.setConsignee("AUTO-SPRAY");
					}else {
						rdsFd.setConsignee("AUTOMOBILE");
					}
				} else {
					rdsFd.setConsignee("AUTOMOBILE");
				}
			}*/
			if(cargoNotes!=null)
			{
				/*if(cargoNotes.contains("REQUIRES CS ACTION"))
				{
					rdsFd.setConsignee("REQUIRES CS ACTION");
				}
				else*/ if(cargoNotes.contains("UNAPPROVED VARIANCE"))
				{
					rdsFd.setConsignee("UNAPPROVED VARIANCE");
					cargoNotes = cargoNotes.replaceAll("UNAPPROVED VARIANCE", "").trim();
					rdsFd.setCargoNotes(cargoNotes);
				}
			}
			if(dport.equalsIgnoreCase("MIX"))
			{
				String containerNumber = rdsFd.getContainerNumber();
				ArrayList<TosGumRdsDataMt> cntrRdsData = new ArrayList<TosGumRdsDataMt>();
				ArrayList<String> shipperNameList = new ArrayList<String>();
				ArrayList<String> dischargeServiceList = new ArrayList<String>();
				for(int r=0; r<rdsData.size(); r++)
				{
					TosGumRdsDataMt tempData = rdsData.get(r);
					if(tempData.getCtrno()!=null && tempData.getCtrno().equals(containerNumber))
					{
						cntrRdsData.add(tempData);
						if(tempData.getShipperName()!=null && tempData.getShipperName().length()>0)
						{
							shipperNameList.add(tempData.getShipperName());
						}
						if (tempData.getLoadDischServ() != null && tempData.getLoadDischServ().length() > 0) {
							dischargeServiceList.add(tempData.getLoadDischServ());
						}

					}
				}
				if(cntrRdsData.size()>1 && shipperNameList.size()>1)
				{
					boolean isSameShipper = false;
					String shipper = "";
					for(int s=0; s<shipperNameList.size(); s++)
					{
						String tempShipper = shipperNameList.get(s);
						if(shipper.equals(""))
							shipper = tempShipper;
						else
						{
							if(shipper.equals(tempShipper))
								isSameShipper = true;
							else
							{
								isSameShipper = false;
								break;
							}
						}
					}
					if(!isSameShipper)
					{
						rdsFd.setConsignee("MIX");
					}
				}

				if(cntrRdsData.size()>1 && dischargeServiceList.size()>1)
				{
					mixedDS = "N";
					String localds = "";
					for(int x=0; x<dischargeServiceList.size(); x++)
					{
						String tempDs = dischargeServiceList.get(x);
						if(localds.equals(""))
							localds = tempDs;
						else
						{
							if(localds.equals(tempDs))
								mixedDS = "N";
							else
							{
								mixedDS = "Y";
								break;
							}
						}
					}
				}

				//Raghu Pattangi
				if ("AUTOMOBILE".equalsIgnoreCase(rdsFd.getConsignee()) && "N".equals(mixedDS)) {
					rdsFd.setConsignee("MIX");
				}
			}
			logger.info("CNEE-"+rdsFd.getConsignee());
			rdsDataFinal.set(i, rdsFd);
		}
		return rdsDataFinal;
	}
	public static ArrayList<TosGumRdsDataFinalMt> assignTrucker(ArrayList<TosGumRdsDataFinalMt> rdsDataFinal)
	{
		logger.info("Getting Trucker codes from TosConsigneeTruckerMz");
		ArrayList<TosConsigneeTruckerMz> truckers = NewVesselDaoGum.getTruckers();
		if(truckers==null || truckers.size()==0)
		{
			logger.error("No truckers assigned - truckers="+truckers);
			return rdsDataFinal;
		}
		logger.info("**** Assigning Trucker code only for GUM containers ****");
		ArrayList<String> newConsigneeList = new ArrayList<String>();
		for(int i=0; i<rdsDataFinal.size(); i++)
		{
			TosGumRdsDataFinalMt rdsFd = rdsDataFinal.get(i);
			if(rdsFd.getDport().equals("GUM"))
			{
				String trucker = "";
				boolean truckConsigneeFound = false;
				if(rdsFd.getConsigneeName()!=null && !rdsFd.getConsigneeName().equals(""))
				{
					for(int t=0; t<truckers.size(); t++)
					{
						if(rdsFd.getConsigneeName().toUpperCase().contains(truckers.get(t).getConsigneeName().toUpperCase()) ||
								truckers.get(t).getConsigneeName().toUpperCase().contains(rdsFd.getConsigneeName().toUpperCase()))
						{
							trucker = truckers.get(t).getTruckerCode();
							truckConsigneeFound = true;
							break;
						}
					}
					if(!truckConsigneeFound)
					{
						if(!newConsigneeList.contains(rdsFd.getConsigneeName().toUpperCase())) {
							newConsigneeList.add(rdsFd.getConsigneeName().toUpperCase());
							logger.info("New Consignee to add --> "+rdsFd.getConsigneeName().toUpperCase());
						}
					}
					else
					{
						logger.info(rdsFd.getContainerNumber()+"\t"+rdsFd.getConsigneeName()+"\t"+trucker);
						rdsFd.setTruck(trucker);
					}
				}
			}
			rdsDataFinal.set(i, rdsFd);
		}
		if(newConsigneeList.size()>0)
		{
			logger.info("New GUM consignees found and being added to consigneeTruckerMz");
			ArrayList<TosConsigneeTruckerMz> newTruckers = new ArrayList<TosConsigneeTruckerMz>();
			for(int n=0; n<newConsigneeList.size(); n++)
			{
				TosConsigneeTruckerMz tkr = new TosConsigneeTruckerMz();
				tkr.setConsigneeName(newConsigneeList.get(n).length()>35?newConsigneeList.get(n).substring(0, 35):newConsigneeList.get(n));
				tkr.setLocationCode("GUM");
				tkr.setCreateUser("TDP");
				tkr.setCreateDate(new Date());
				newTruckers.add(tkr);
			}
			NewVesselDaoGum.insertNewConsigneeTruckers(newTruckers);
		}
		return rdsDataFinal;
	}
	public static String srstatusHoldCheck(TosGumRdsDataFinalMt rdsDataFinal)
	{
		StringBuilder status = new StringBuilder();
		String hsf2 = rdsDataFinal.getHsf2();
		String hsf3 = rdsDataFinal.getHsf3();
		String hsf4 = rdsDataFinal.getHsf4();
		String hsf5 = rdsDataFinal.getHsf5();
		String hsf6 = rdsDataFinal.getHsf6();
		String trade = rdsDataFinal.getTrade();
		String ds = rdsDataFinal.getDs();
		//hsf2
		if(hsf2!=null && hsf2.contains("H"))
			status = CommonBusinessProcessor.appendStatusString(status, "HP");
		else if(hsf2!=null && hsf2.contains("C"))
			status = CommonBusinessProcessor.appendStatusString(status, "CC");
		//hsf3
		if(hsf3!=null && !hsf3.equals(""))
			status = CommonBusinessProcessor.appendStatusString(status, "ON");
		//hsf4
		if(hsf4!=null && !hsf4.equals(""))
			status = CommonBusinessProcessor.appendStatusString(status, "AG");
		//hsf5
		if(hsf5!=null && !hsf5.equals(""))
		{
			if(trade!=null && trade.equals("M"))
				status = CommonBusinessProcessor.appendStatusString(status, "INB");
			else
				status = CommonBusinessProcessor.appendStatusString(status, "CUS");
		}
		//hsf6
		if(hsf6!=null && !hsf6.equals(""))
		{
			if(hsf6.equals("E"))
			{
				if((ds!=null && !ds.equals("AUT")))
				{
					status = CommonBusinessProcessor.appendStatusString(status, "DOC");
				}
			}
		}
		logger.info("SRSTATUS HOLDS CHECK : "+status);
		if(status.length()>0)
			return status.toString().trim();
		return "OK";
	}
	public static ArrayList<TosGumRdsDataFinalMt> crstatusHoldsCheck1(ArrayList<TosGumRdsDataFinalMt> rdsDataFinal)
	{
		logger.info("crstatusHoldsCheck1 method begin");
		for(int i=0; i<rdsDataFinal.size(); i++)
		{
			StringBuilder status = new StringBuilder();
			TosGumRdsDataFinalMt rdsFd = rdsDataFinal.get(i);
			String dsc = rdsFd.getDsc();
			String ds = rdsFd.getDs();
			String srstatus = rdsFd.getSrstatus();
			boolean holdflag = false, cfsflag = false;
			String holdon2 = "",holdon3 = "",holdon4 = "",holdon5 = "",holdon6 = "";
			String hsf2 = rdsFd.getHsf2();
			String hsf3 = rdsFd.getHsf3();
			String hsf4 = rdsFd.getHsf4();
			String hsf5 = rdsFd.getHsf5();
			String hsf6 = rdsFd.getHsf6();
			String typeCode = rdsFd.getTypeCode();
			String trade = rdsFd.getTrade();
			String dport = rdsFd.getDport();
			if(dsc!=null && dsc.equals("P"))
			{
				if(srstatus!=null && !srstatus.equals("OK"))
					status = CommonBusinessProcessor.appendStatusString(status, srstatus);
			}
			if(ds!=null && ds.equals("CFS"))
				cfsflag = true;
			if(hsf2!=null && hsf2.contains("H"))
			{
				holdon2 = "H"; 
				holdflag = true;
			}
			else if(hsf2!=null && hsf2.contains("C"))
			{
				holdon2 = "C"; 
				holdflag = true;
			}
			if(hsf3!=null && hsf3.equals("Y"))
			{
				holdon3 = "Y"; 
				holdflag = true;
			}
			if(hsf4!=null && (hsf4.equals("Y") || hsf4.equals("I") || hsf4.equals("A")))
			{
				if(typeCode!=null && (!typeCode.substring(0,1).equals("R") || typeCode.equalsIgnoreCase("RoRo")))
					holdon4 = "Y"; 
					holdflag = true;
			}
			// added by raghu
			if(hsf5!=null && hsf5.equals("Y"))
			{
				holdon5 = "Y"; 
				holdflag = true;
			}
			if(hsf6!=null && hsf6.equals("E"))
			{
				//if(typeCode!=null && (!typeCode.endsWith("GR") || !typeCode.endsWith("GB")))
					holdon6 = "Y"; 
					holdflag = true;
			}
			if(holdflag && cfsflag==false)
			{
				if(!holdon2.equals("") && holdon2.equals("H"))
					status = CommonBusinessProcessor.appendStatusString(status, "HP");
				else if(!holdon2.equals("") && holdon2.equals("C"))
					status = CommonBusinessProcessor.appendStatusString(status, "CC");
				else if(!holdon2.equals("") && !holdon2.equals("C") && !holdon2.equals("H"))
					status = CommonBusinessProcessor.appendStatusString(status, "HP CC");
				// from here, the code is corrected to be in synch with Hon new vessel process. This is a deviation from CMIS where 
				// CMIS Gum new vessel process does not apply ON, AG, CUS, INB holds. With this change, TDP GUM new vessel process
				// applies these holds.
				if(!holdon3.equals("") && holdon3.equals("Y"))
					status = CommonBusinessProcessor.appendStatusString(status, "ON");
				if(!holdon4.equals("") && holdon4.equals("Y"))
					status = CommonBusinessProcessor.appendStatusString(status, "AG");
				if(!holdon5.equals("") && holdon5.equals("Y"))
				{
					if(dport!=null && (dport.equals("MAJ")||dport.equals("KWJ")||dport.equals("EBY")||dport.equals("JIS")))
						status = CommonBusinessProcessor.appendStatusString(status, "INB");
					else
						//added by raghu
						if (trade!=null && (!trade.equals("G") && !trade.equals("F"))){
							status = CommonBusinessProcessor.appendStatusString(status, "CUS");
						}
				}
				if(!holdon6.equals("") && holdon6.equals("Y"))
				{
					//if(trade==null || (trade!=null && !trade.equalsIgnoreCase("G") && !trade.equalsIgnoreCase("F")))
						status = CommonBusinessProcessor.appendStatusString(status, "DOC");
				}
			}
			String crstatus = status.toString().trim();
			rdsFd.setCrstatus(crstatus);
			rdsDataFinal.set(i, rdsFd);
			logger.info(rdsFd.getContainerNumber()+" CRSTATUS 1: " + crstatus);
		}
		logger.info("crstatusHoldsCheck1 method end");
		return rdsDataFinal;
	}
	public static ArrayList<TosGumRdsDataFinalMt> crstatusHoldsCheck2(ArrayList<TosGumRdsDataFinalMt> rdsDataFinal)
	{
		for(int i=0; i<rdsDataFinal.size(); i++)
		{
			TosGumRdsDataFinalMt rdsFd = rdsDataFinal.get(i);
			String locationRowDeck = rdsFd.getLocationRowDeck();
			String bookingNbr = rdsFd.getBookingNumber();
			String ds = rdsFd.getDs();
			String loadPort = rdsFd.getLoadPort();
			loadPort = loadPort==null?"":loadPort;
			String loadPortTrade = getTradeforPort(loadPort);
			loadPortTrade = loadPortTrade==null?"":loadPortTrade;
			if(locationRowDeck!=null && locationRowDeck.equals("APL") && (loadPortTrade.equals("F") || loadPortTrade.equals("G")))
			{
				rdsFd.setCrstatus("CUS CAR");
				rdsFd.setCommodity("CUS");
			}
			if(locationRowDeck!=null && locationRowDeck.equals("APL") && (loadPortTrade.equals("F") || loadPortTrade.equals("G"))
					&& bookingNbr!=null && bookingNbr.equals("") && ds!=null && ds.equals("CY"))
			{
				rdsFd.setCrstatus("DOC CUS");
				rdsFd.setCommodity("CUS");
			}
			if(loadPortTrade!=null && (loadPortTrade.equals("F") || loadPortTrade.equals("G") || loadPortTrade.equals("M")))
			{
				String crs = rdsFd.getCrstatus();
				if(crs==null)
					crs = "";
				if(crs.length()>0)
				{
					if(!crs.contains("CUS"))
					{
						crs = "CUS "+crs;
						rdsFd.setCrstatus(crs);
						rdsFd.setCommodity("CUS");
					}
				}
				else
				{
					crs = "CUS";
					rdsFd.setCrstatus(crs);
					rdsFd.setCommodity("CUS");
				}
			}
			//apply RM hold to all CUS containers...
			String crstat = rdsFd.getCrstatus();
			String stowFlag = rdsFd.getStowFlag();
			stowFlag = stowFlag==null?"":stowFlag;
			if(crstat!=null && crstat.contains("CUS"))
			{
				if(!crstat.contains("RM"))
				{
					crstat = crstat + " RM";
					rdsFd.setCrstatus(crstat);
					if(stowFlag.equals("Y"))
						rdsFd.setStowFlag("M");
					else
						rdsFd.setStowFlag("C");
				}
			}
			logger.info(rdsFd.getContainerNumber()+" CRSTATUS 2: " + rdsFd.getCrstatus());
			rdsDataFinal.set(i, rdsFd);
		}
		return rdsDataFinal;
	}
	public static ArrayList<TosGumRdsDataFinalMt> crstatusHoldsCheck4(ArrayList<TosGumRdsDataFinalMt> rdsDataFinal)
	{
		for(int i=0; i<rdsDataFinal.size(); i++)
		{
			TosGumRdsDataFinalMt rdsFd = rdsDataFinal.get(i);
			String crstat = rdsFd.getCrstatus();
			crstat = crstat == null?"":crstat;
			boolean dateFlag = false;
			try {
				Date triggerDate = new Date();
				SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
				String cYear = new SimpleDateFormat("yyyy").format(triggerDate);
				Date date1 = sdf.parse("11/01/"+cYear);
				Date date2 = sdf.parse("12/25/"+cYear);

				if((triggerDate.compareTo(date1)>=0) && (triggerDate.compareTo(date2)<=0))
					dateFlag = true;
			}
			catch(ParseException ex)
			{
				ex.printStackTrace();
			}
			boolean cNotesFlag = false;
			String cnotes = rdsFd.getCargoNotes();
			if(cnotes != null)
			{
				cnotes = cnotes.toLowerCase();
				if((cnotes.contains("christmas") || cnotes.contains("chrismtas") || cnotes.contains("wreath")
						|| cnotes.contains("tree") || (cnotes.contains("evergreen"))))
				{
					cNotesFlag = true;
				}
				if(cNotesFlag && dateFlag)
				{
					crstat = crstat +" XT";
					rdsFd.setCrstatus(crstat);
					rdsFd.setCommodity("XMASTREE");
					logger.info("XT HOLD SET FOR "+rdsFd.getContainerNumber());
				}
			}
			logger.info(rdsFd.getContainerNumber()+" CRSTATUS 4: " + rdsFd.getCrstatus());
			rdsDataFinal.set(i, rdsFd);
		}
		return rdsDataFinal;
	}

	public static ArrayList<TosGumRdsDataFinalMt> crstatusHoldsCheck5(ArrayList<TosGumRdsDataFinalMt> rdsDataFinal)
	{
		for(int i=0; i<rdsDataFinal.size(); i++)
		{
			TosGumRdsDataFinalMt rdsFd = rdsDataFinal.get(i);
			String crstat = rdsFd.getCrstatus();
			crstat = crstat == null?"":crstat;
			String ds = rdsFd.getDs();
			ds = ds==null?"":ds;
			String consignee = rdsFd.getConsignee();
			consignee = consignee==null?"":consignee;
			if(ds.equals("CY") && consignee.equals(""))
			{
				if(!crstat.contains("DOC"))
				{
					crstat = crstat.trim() + " DOC";
					rdsFd.setCrstatus(crstat);
				}
			}
			logger.info(rdsFd.getContainerNumber()+" CRSTATUS 5: " + rdsFd.getCrstatus());
			rdsDataFinal.set(i, rdsFd);
		}
		return rdsDataFinal;
	}

	public static ArrayList<TosGumRdsDataFinalMt> crstatusHoldsCheck6(ArrayList<TosGumRdsDataFinalMt> rdsDataFinal, ArrayList<TosGumStPlanCntrMt> ocrDataList)
	{
		for(int i=0; i<rdsDataFinal.size(); i++)
		{
			TosGumRdsDataFinalMt rdsFd = rdsDataFinal.get(i);
			String ctrno = rdsFd.getContainerNumber();
			String crstatus = rdsFd.getCrstatus();
			crstatus = crstatus==null?"":crstatus.trim();
			for(int c=0; c<ocrDataList.size(); c++)
			{
				TosGumStPlanCntrMt stowCntrMt = ocrDataList.get(c);
				String ctrNbr = stowCntrMt.getContainerNumber();
				if(ctrNbr != null && ctrNbr.equalsIgnoreCase(ctrno))
				{
					Set<TosGumStPlanHoldMt> stowHoldMt = stowCntrMt.getTosGumStPlanHoldMts();
					if(stowHoldMt!=null && stowHoldMt.size()>0)
					{
						Iterator<TosGumStPlanHoldMt> itrHold = stowHoldMt.iterator();
						logger.info(ctrno + " has hold in HOLDMT table");
						String holds = "";
						while(itrHold.hasNext())
						{
							TosGumStPlanHoldMt holdData = itrHold.next();
							holds = holds + " " + holdData.getId().getCode();
						}
						holds = holds==null?"":holds;
						if(holds.contains("CUS"))
						{
							if(!crstatus.contains("CUS"))
								crstatus = crstatus + " CUS";
							if(!crstatus.contains("RM"))
								crstatus = crstatus.trim() + " RM";
						}
						if(holds.contains("HP"))
						{
							if(!crstatus.contains("HP"))
								crstatus = crstatus + " HP";
						}
						if(holds.contains("CC"))
						{
							if(!crstatus.contains("CC"))
								crstatus = crstatus + " CC";
						}
						if(holds.contains("ON"))
						{
							if(!crstatus.contains("ON"))
								crstatus = crstatus + " ON";
						}
						if(holds.contains("AG"))
						{
							if(!crstatus.contains("AG"))
								crstatus = crstatus + " AG";
						}
						rdsFd.setCrstatus(crstatus);
						rdsDataFinal.set(i, rdsFd);
						logger.info(rdsFd.getContainerNumber()+" HOLDSMT CRSTATUS 6: " + rdsFd.getCrstatus());
					}
				}
			}
		}
		return rdsDataFinal;
	}
	public static ArrayList<TosGumRdsDataFinalMt> validateContainers(ArrayList<TosGumRdsDataFinalMt> rdsDataFinal)
	{
		for(int i=0; i<rdsDataFinal.size(); i++)
		{
			Iterator<TosGumRdsDataFinalMt> itr = rdsDataFinal.iterator();
			while(itr.hasNext())
			{
				TosGumRdsDataFinalMt rdsFd = itr.next();
				boolean isValid = true;
				if(rdsFd.getLoadPort()==null || rdsFd.getLoadPort().equals(""))
				{
					nvLogger.addError(rdsFd.getVesvoy(), rdsFd.getContainerNumber(), " Load port is missing");
					isValid = false;
				}
				/*if(rdsFd.getDischargePort()==null)
				{
					nvLogger.addError(rdsFd.getVesvoy()+rdsFd.getLeg(), rdsFd.getContainerNumber(), "Discharge port is missing");
					isValid = false;
				}*/
				if(rdsFd.getDport()==null || rdsFd.getDport().equals(""))
				{
					nvLogger.addError(rdsFd.getVesvoy(), rdsFd.getContainerNumber(), " Destination port is missing");
					isValid = false;
				}
				if(rdsFd.getTypeCode()==null || rdsFd.getTypeCode().equals(""))
				{
					nvLogger.addError(rdsFd.getVesvoy(), rdsFd.getContainerNumber(), " Type code is missing");
					isValid = false;
				}
				if(!isValid)
					itr.remove();
			}
		}
		return rdsDataFinal;
	}
	public static ArrayList<TosGumRdsDataFinalMt> calculateLastFreeDayDueDate(ArrayList<TosGumRdsDataFinalMt> rdsDataFinal)
	{
		HashMap<String, String> vesselAvailableDateMap = new HashMap<String, String>();
		for(int i=0; i<rdsDataFinal.size(); i++)
		{
			TosGumRdsDataFinalMt rdsFd = rdsDataFinal.get(i);
			String vesvoy = rdsFd.getVesvoy();
			String leg = rdsFd.getLeg();
			String vvd = vesvoy+leg;
			String vesselAvailableDate = "";
			String tempVesselAvailableDate = "";
			String cargoNotes = rdsFd.getCargoNotes();
			cargoNotes = cargoNotes==null?"":cargoNotes;
			String consignee = rdsFd.getConsignee();
			consignee = consignee==null?"":consignee;
			String temperature = rdsFd.getTemp();
			temperature = temperature==null?"":temperature;
			String lastFreeDay = "";
			String dueDate = "";

			if(!vesselAvailableDateMap.containsKey(vvd))
			{
				tempVesselAvailableDate = CommonBusinessProcessor.getAvailableDateByVVD(vvd, "GUM");
				//System.out.println(vvd + " - IS NOT IN THE MAP - ADDING DATE NOW " + tempVesselAvailableDate);
				if(tempVesselAvailableDate!=null)
					vesselAvailableDateMap.put(vvd, tempVesselAvailableDate);
			}
			vesselAvailableDate = vesselAvailableDateMap.get(vvd);
			if(vesselAvailableDate!=null && !vesselAvailableDate.equals(""))
			{
				// Set available date in ARRDATE field
				rdsFd.setArrdate(CalendarUtil.convertStrgToDateFormat(vesselAvailableDate));
				//
				String due6Days = CommonBusinessProcessor.calcDueDate(vesselAvailableDate, 6, "BIZ");
				String due7Days = CommonBusinessProcessor.calcDueDate(vesselAvailableDate, 7, "BIZ");
				String due10Days = CommonBusinessProcessor.calcDueDate(vesselAvailableDate, 10, "BIZ");
				String due8CalendarDays = CommonBusinessProcessor.calcDueDate(vesselAvailableDate, 8, "CALENDAR");
				String due10CalendarDays = CommonBusinessProcessor.calcDueDate(vesselAvailableDate, 10, "CALENDAR");
				String srv = rdsFd.getSrv();
				srv = srv==null?"":srv;
				String dport = rdsFd.getDport();
				dport = dport==null?"":dport;
				String ds = rdsFd.getDs();
				ds = ds==null?"":ds;
				String milTcn = rdsFd.getMilTcn();
				milTcn = milTcn==null?"":milTcn;
				String typeCode = rdsFd.getTypeCode();
				typeCode = typeCode==null?"":typeCode;
				String commodity = rdsFd.getCommodity();
				commodity = commodity==null?"":commodity;
				if(srv.equals("MAT"))
				{
					if(dport.equals("HON") && (ds.equals("CY") || ds.equals("CON")))
					{
						// Detention Due Date
						if(!milTcn.equals(""))
						{
							if(typeCode.startsWith("R"))
								dueDate = due8CalendarDays;
							else
								dueDate = due10CalendarDays;
						}
						else if(commodity.equals("XMAS40") || commodity.equals("XMASTREE"))
							dueDate = due7Days;
						else if(typeCode.startsWith("R"))
							dueDate = due6Days;
						else
							dueDate = due10Days;
						// Last Free Day
						if(!milTcn.equals(""))
						{}
						else if(typeCode.startsWith("R"))
						{}
						else
						{
							if(CommonBusinessProcessor.isDryWall(cargoNotes, consignee)) {
								lastFreeDay = CommonBusinessProcessor.calcLastFreeDays(vesselAvailableDate, 15);
							}
							else {
								lastFreeDay = CommonBusinessProcessor.calcLastFreeDays(vesselAvailableDate, 10);
							}
						}
					}
				}
				if(dueDate!=null && dueDate.length()>0)
				{
					rdsFd.setMisc3(dueDate);
				}
				if(lastFreeDay!=null && !lastFreeDay.equals(""))
				{
					rdsFd.setLocationCategory(CalendarUtil.convertDateStringToString(lastFreeDay, true));
				}
				//
				//System.out.println(rdsFd.getContainerNumber()+" DATES-->"+vesselAvailableDate+"-->"+lastFreeDay+"-->"+dueDate);
				rdsDataFinal.set(i, rdsFd);
			}
		}
		vesselAvailableDateMap = null;
		return rdsDataFinal;
	}
	public static ArrayList<TosGumRdsDataFinalMt> stowRestrictionCodeTransformation(ArrayList<TosGumRdsDataFinalMt> rdsDataFinal)
	{
		for(int i=0; i<rdsDataFinal.size(); i++)
		{
			TosGumRdsDataFinalMt rdsFd = rdsDataFinal.get(i);
			String ds = rdsFd.getDs();
			String commodity = rdsFd.getCommodity();
			String typeCode  = rdsFd.getTypeCode();
			String vesvoy = rdsFd.getVesvoy();
			if(typeCode != null && !typeCode.trim().equals("")){
				typeCode = typeCode.substring(0,3);
			}
			String vesselOpr = "";
			VesselVO vvo = getVesselDetails(vesvoy.substring(0, 3));
			vesselOpr = vvo.getVessOpr();

			if(ds != null && commodity != null && typeCode != null &&  vesselOpr != null &&
					!vesselOpr.trim().equals("") &&   !vesselOpr.equals("MAT")){
				if(ds.equals("CY") &&  commodity.equals("ALSAUT")){
					rdsFd.setStowRestrictionCode("V");
				}
				else if(ds.equals("CY") &&  !commodity.equals("ALSAUT")){
					if(ds.equals("CY") && typeCode.equals("D45")){
						rdsFd.setStowRestrictionCode("5");
					}
					else if(ds.equals("CY") && typeCode.equals("D40")){
						rdsFd.setStowRestrictionCode("4");
					}
					else if(ds.equals("CY") && typeCode.equals("D20")){
						rdsFd.setStowRestrictionCode("2");
					}
					else if(ds.equals("CY") && typeCode.equals("F40")){
						rdsFd.setStowRestrictionCode("6");
					}
					else if(ds.equals("CY") && typeCode.equals("D24")){
						rdsFd.setStowRestrictionCode("7");
					}
				}
				else if(ds.equals("AUT") && typeCode.equals("F40")){
					rdsFd.setStowRestrictionCode("F");
				}
				else if(ds.equals("AUT") && (typeCode.equals("O40") || typeCode.equals("D40"))){
					rdsFd.setStowRestrictionCode("O");
				}
			}

			rdsDataFinal.set(i, rdsFd);
		}
		return rdsDataFinal;
	}


}
