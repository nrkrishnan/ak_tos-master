package com.matson.tos.reports.gum;

/**
 * S.NO			Date			Author						Description
 * -----		-----			-------						------------
 * 1			04/12/2013		Karthik Rajendran			Class created to generate Guam Download Discrepancies.
 * 2			04/15/2013		Karthik Rajendran			Added: getUnApprovedVariances(),getGumDischargeMTYContainers()
 * 																	getFactsCmisDifferences(),getUnknownConsignees(), getBlankCyFactsRecords
 * 3			04/16/2013		Karthik Rajendran			Changed: Blank DS header text.
 *
 *
 */


import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.matson.cas.refdata.mapping.TosGumRdsDataFinalMt;
import com.matson.cas.refdata.mapping.TosGumRdsDataMt;
import com.matson.cas.refdata.mapping.TosGumStPlanCntrMt;
import com.matson.tos.dao.GumVesselReportDao;
import com.matson.tos.processor.GumCommonBusinessProcessor;

public class GumDownloadDiscrepanciesGenerator {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//GumDownloadDiscrepanciesGenerator gen = new GumDownloadDiscrepanciesGenerator("MLI090");
		//logger.info(gen.getDiscrepancies());
	}
	private static Logger logger = Logger.getLogger(GumDownloadDiscrepanciesGenerator.class);
	private StringBuilder output = null;
	private final static String newLine = "\n";
	private final static String CMIS_FACTS_DIFF = "CMIS/FACTS Differences";
	private final static String NO_CMIS_FACTS_DIFF = " - No CMIS/FACTS Differences Found.";
	private final static String FACTS_CMIS_DIFF = "The following containers were in the FACTS download but not in the CMIS file:";
	private final static String GUM_MTY_DISCH_CTRS = "Gum Discharge MTY Containers - Maybe OK but please confirm";
	private final static String NO_GUM_MTY_DISCH_CTRS = " - No Gum Discharge MTY Containers Found";
	private final static String UNAPPROVED_VARIANCES = "Containers with Unapproved Variances/Invalid Assignments";
	private final static String NO_UNAPPROVED_VARIANCES = " - No Unapproved Variances Found";
	private final static String BLANK_CY_FACTS_RECORDS = "No FACTS info - Per CMIS not AUT or CFS";
	private final static String NO_BLANK_CY_FACTS_RECORDS = " - No Blank CY FACTS Records Found";
	private final static String UNKNOWN_CONSIGNEES = "Unknown Consignees";
	private final static String NO_UNKNOWN_CONSIGNEES = " - No Unknown Consignees Found";

	public GumDownloadDiscrepanciesGenerator()
	{ }
	public GumDownloadDiscrepanciesGenerator(String vesvoy)
	{
		output = new StringBuilder();
		output.append(newLine);
		logger.info("Retrieving CMIS Facts Differences...");
		output.append(getCmisFactsDifferences(vesvoy));
		logger.info("Retrieving Facts CMIS Differences...");
		output.append(getFactsCmisDifferences(vesvoy));
		output.append(newLine+newLine);
		logger.info("Retrieving Blank CY CMIS Differences...");
		output.append(getBlankCyFactsRecords(vesvoy));
		output.append(newLine+newLine);
		logger.info("Retrieving UnApproved Variances ...");
		output.append(getUnApprovedVariances(vesvoy));
		output.append(newLine+newLine);
		logger.info("Retrieving Unknown consignees ...");
		output.append(getUnknownConsignees(vesvoy));
		output.append(newLine+newLine);
		logger.info("Retrieving Gum Discharge MTY Containers...");
		output.append(getGumDischargeMTYContainers(vesvoy));
		output.append(newLine+newLine);
	}
	private String getCmisFactsDifferences(String vesvoy)
	{
		StringBuilder outStr = new StringBuilder();
		outStr.append(printHeader(CMIS_FACTS_DIFF));
		ArrayList<TosGumStPlanCntrMt> stowList = null;
		stowList = GumVesselReportDao.getGumCmisFactsDifferences(vesvoy);
		if(stowList!=null && stowList.size()>0)
		{
			String[] colHeads = {"CTRNO", "BOOKING #", "PORT", "CONSIGNEE", "CELL", "TYPECODE", "WEIGHT"};
			int[] colLengths = {12, 17, 5, 19, 7, 8, 6};
			outStr.append(printColHeader(colHeads, colLengths));
			List<String> temp = new ArrayList<String>();
			for(int i=0; i<stowList.size(); i++)
			{
				TosGumStPlanCntrMt st = stowList.get(i);
				temp.add(st.getContainerNumber().replace("MATU", ""));
				temp.add(st.getBookingNumber());
				temp.add(st.getDport());
				temp.add(st.getConsignee());
				temp.add(st.getCell());
				temp.add(st.getTypeCode());
				temp.add(""+st.getCweight());
			}
			outStr.append(printDetails(temp, colHeads.length, colLengths));
		}
		else
		{
			outStr.append(printHeader(NO_CMIS_FACTS_DIFF));
		}
		//
		return outStr.toString();
	}
	private String getFactsCmisDifferences(String vesvoy)
	{
		StringBuilder outStr = new StringBuilder();
		ArrayList<TosGumRdsDataMt> rList = null;
		rList = GumVesselReportDao.getGumFactsCmisDifferences(vesvoy);
		if(rList!=null && rList.size()>0)
		{
			outStr.append(printHeader(FACTS_CMIS_DIFF));
			String[] colHeads = {"CTRNO", "BOOKING #", "PORT", "CONSIGNEE", "CELL", "TYPECODE", "WEIGHT"};
			int[] colLengths = {12, 17, 5, 19, 7, 8, 6};
			outStr.append(printColHeader(colHeads, colLengths));
			List<String> temp = new ArrayList<String>();
			for(int i=0; i<rList.size(); i++)
			{
				TosGumRdsDataMt st = rList.get(i);
				temp.add(st.getCtrno().replace("MATU", ""));
				temp.add(st.getShipno());
				temp.add(st.getDestinationPort());
				temp.add(st.getConsigneeName());
				temp.add(st.getCell());
				temp.add(" ");
				temp.add(""+st.getGrossWt());
			}
			outStr.append(printDetails(temp, colHeads.length, colLengths));
		}
		//
		return outStr.toString();
	}
	private String getGumDischargeMTYContainers(String vesvoy)
	{
		StringBuilder outStr = new StringBuilder();
		outStr.append(printHeader(GUM_MTY_DISCH_CTRS));
		ArrayList<TosGumRdsDataFinalMt> mtyList = GumVesselReportDao.getGumDischargeMTYContainers(vesvoy);
		if(mtyList!=null && mtyList.size()>0)
		{
			String[] colHeads = {"CTRNO"};
			int[] colLengths = {12};
			outStr.append(printColHeader(colHeads, colLengths));
			List<String> temp = new ArrayList<String>();
			for(int i=0; i<mtyList.size(); i++)
			{
				TosGumRdsDataFinalMt st = mtyList.get(i);
				temp.add(st.getContainerNumber().replace("MATU", ""));
			}
			outStr.append(printDetails(temp, colHeads.length, colLengths));
		}
		else
		{
			outStr.append(printHeader(NO_GUM_MTY_DISCH_CTRS));
		}
		//
		return outStr.toString();
	}
	private String getUnApprovedVariances(String vesvoy)
	{
		StringBuilder outStr = new StringBuilder();
		outStr.append(printHeader(UNAPPROVED_VARIANCES));
		ArrayList<TosGumRdsDataFinalMt> uList = null;
		uList = GumVesselReportDao.getUnApprovedVariances(vesvoy);
		if(uList!=null && uList.size()>0)
		{
			String[] colHeads = {"CTRNO", "DIR", "DPORT"};
			int[] colLengths = {12, 5, 5};
			outStr.append(printColHeader(colHeads, colLengths));
			List<String> temp = new ArrayList<String>();
			for(int i=0; i<uList.size(); i++)
			{
				TosGumRdsDataFinalMt st = uList.get(i);
				temp.add(st.getContainerNumber().replace("MATU", ""));
				temp.add(st.getDir());
				temp.add(st.getDport());
			}
			outStr.append(printDetails(temp, colHeads.length, colLengths));
		}
		else
		{
			outStr.append(printHeader(NO_UNAPPROVED_VARIANCES));
		}
		//
		return outStr.toString();
	}
	private String getBlankCyFactsRecords(String vesvoy)
	{
		StringBuilder outStr = new StringBuilder();
		outStr.append(printHeader(BLANK_CY_FACTS_RECORDS));
		ArrayList<TosGumRdsDataFinalMt> cyList = GumVesselReportDao.getBlankCyFactsRecords(vesvoy);
		if(cyList!=null && cyList.size()>0)
		{
			String[] colHeads = {"CTRNO"};
			int[] colLengths = {12};
			outStr.append(printColHeader(colHeads, colLengths));
			List<String> temp = new ArrayList<String>();
			for(int i=0; i<cyList.size(); i++)
			{
				TosGumRdsDataFinalMt st = cyList.get(i);
				temp.add(st.getContainerNumber().replace("MATU", ""));
			}
			outStr.append(printDetails(temp, colHeads.length, colLengths));
		}
		else
		{
			outStr.append(printHeader(NO_BLANK_CY_FACTS_RECORDS));
		}
		//
		return outStr.toString();
	}
	private String getUnknownConsignees(String vesvoy)
	{
		StringBuilder outStr = new StringBuilder();
		outStr.append(printHeader(UNKNOWN_CONSIGNEES));
		ArrayList<TosGumRdsDataFinalMt> cList = GumVesselReportDao.getUnknownConsignees(vesvoy);
		if(cList!=null && cList.size()>0)
		{
			String[] colHeads = {"CTRNO", "CONSIGNEE", "BOOKING NUMBER"};
			int[] colLengths = {12, 35, 17};
			outStr.append(printColHeader(colHeads, colLengths));
			List<String> temp = new ArrayList<String>();
			for(int i=0; i<cList.size(); i++)
			{
				TosGumRdsDataFinalMt st = cList.get(i);
				temp.add(st.getContainerNumber().replace("MATU", ""));
				temp.add(st.getConsignee());
				temp.add(st.getBookingNumber());
			}
			outStr.append(printDetails(temp, colHeads.length, colLengths));
		}
		else
		{
			outStr.append(printHeader(NO_UNKNOWN_CONSIGNEES));
		}
		//
		return outStr.toString();
	}

	//***************************************************************
	private String printColHeader(String[] colHeads, int[] colLengths)
	{
		String outStr = "";
		for(int i=0; i<colHeads.length; i++)
		{
			outStr = outStr + StringUtils.rightPad(colHeads[i], colLengths[i]) + " ";
		}
		outStr = outStr + newLine;
		for(int i=0; i<colHeads.length; i++)
		{
			outStr = outStr + StringUtils.rightPad("", colLengths[i], "-") + " ";
		}
		outStr = outStr + newLine;
		//
		return outStr;
	}
	private String printHeader(String text)
	{
		return text + newLine + newLine;
	}
	private String printDetails(List<String> list, int splitLength, int[] colLengths)
	{
		StringBuilder outStr = new StringBuilder();
		//
		List[] spList = (List[]) GumCommonBusinessProcessor.splitList(list, splitLength);
		for(int l=0; l<spList.length; l++)
		{
			List<String> cList = (List<String>)spList[l];
			for(int k=0; k<cList.size(); k++)
			{
				String str = cList.get(k);
				str = str==null?"":str;
				str = str.length()>colLengths[k]?str.substring(0, colLengths[k]):str;
				outStr.append(StringUtils.rightPad(str, colLengths[k])+" ");
			}
			outStr.append(newLine);
		}
		//
		return outStr.toString();
	}
	public String getDiscrepancies()
	{
		return output.toString().replaceAll("null", "");
	}
}
