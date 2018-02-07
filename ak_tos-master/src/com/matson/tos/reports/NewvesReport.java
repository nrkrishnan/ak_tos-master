package com.matson.tos.reports;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import com.hp.gagawa.java.elements.B;
import com.hp.gagawa.java.elements.Br;
import com.hp.gagawa.java.elements.Div;
import com.hp.gagawa.java.elements.Table;
import com.hp.gagawa.java.elements.Td;
import com.hp.gagawa.java.elements.Text;
import com.hp.gagawa.java.elements.Tr;
import com.matson.cas.refdata.mapping.TosRdsDataFinalMt;
import com.matson.cas.refdata.mapping.TosStowPlanCntrMt;
import com.matson.cas.erd.service.data.VesselVO;
import com.matson.tos.dao.NewReportVesselDao;
import com.matson.tos.dao.NewVesselDao;
import com.matson.tos.dao.TosLookup;
import com.matson.tos.processor.CommonBusinessProcessor;
import com.matson.tos.processor.NewvesProcessorHelper;
import com.matson.tos.reports.barge.*;
import com.matson.tos.util.CalendarUtil;
import com.matson.tos.util.EmailSender;
import com.matson.tos.util.TosRefDataUtil;
import com.matson.tos.vo.SupplementProblems;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRPdfExporterParameter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;

/* This class generates the needed report and sent it through email.
 *
 * 	Srno	Date			AuthorName				Change Description
 * 	A1		10/8/2012		Meena Kumari			Initial creation
 *  A2		10/14/2012		Meena Kumari		    Merged the files using code reuse
 *  A3      10/29/2012      Meena Kumari            Changed to absolute path and removed excel and html format reports
 *  A5		02/13/2013		Karthik Rajendran		DownloadDiscrepanciesReports - Combined 5 reports.
 *  												Export reports in XLS format and attaching the same in email
 *  A6		02/18/2013		Karthik Rajendran		All reports should be under one sheet in excel reports - set one page per sheet to false
 *  A7		02/19/2013		Karthik Rajendran		Download discrepancies results should be writen into email body, not the excel
 *  A8		02/21/2013		Karthik Rajendran		Added: New Barge report - InboundTypecodeSummary and export as html content to email
 *  												InboundTypecodeSummary - HTML formatting added.
 *  A9		03/04/2013		Karthik Rajendran		InboundTypecodeSummary - HTML formatting Fix.
 *  												Added: CyLines Report
 *  A10		03/05/2013		Karthik Rajendran		Added: BargeStowageSummaryReport parameters for totals.
 *  A11		03/07/2013		Karthik Rajendran		Added: InboundTypecodeSummary - HTML formatting spaces and alignment Fix.
 *  A12		04/11/2013		Karthik Rajendran		Added: AG Containers Not Flagged Report
 *  A13		04/12/2013		Karthik Rajendran		Added: Damage code excel report - total count passed as parameter.
 *  A14		10/23/2013		Karthik Rajendran		Changed: Supp problems report email key change from MAIL_SUPP_NEWVES to MAIL_SUPP_PBLMS
 *  A15		02/05/2013		Karthik Rajendran		Changed: In DownloadDiscrepancies report - Placed "Containers found in STIF but not in Stowplan" report section
 *  															right below the "Require CS action/Unapproved variances" report section
 *  A16		07/02/2014		Karthik Rajendran		Added: RORO report
 *  A17     03/18/2015		Raghu Pattangi			Add new section in preview discrepanices / discrepancies to show full containers with dray status return to shipper.
 *  												
 */

public class NewvesReport {
	private static Logger logger = Logger.getLogger(NewvesReport.class);
	private static String MAIL_HOST = TosRefDataUtil.getValue("MAIL_HOST");

	final static String BLANK_CONSIGNEE_CS_REPORT = "BlankConsigneeCSReport";
	final static String INCORRECT_DISCHARGE_PORT_REPORT= "IncorrectDischargePortReport";
	final static String MIX_PORT_CONTAINER_REPORT = "MixPortContainerReport";
	final static String MTY_CONTAINER_REPORT = "MTYContainerReport";
	final static String ROB_CONTAINERS_ON_VESSEL_REPORT = "RobContainersOnVesselReport";
	final static String MULTI_CONTAINER_CELLS_REPORT = "MultiContainerCellsReport";
	final static String DUPLICATE_CONTAINER_REPORT =  "DuplicateContainerReport";
	final static String REEFER_FOR_CONTAINERS_REPORT = "ReeferForFandMContainersReport";
	final static String SF_TAG_REPORT = "SFTagReport";
	final static String DAMAGE_CODE_REPORT = "DamageCodeReport";
	final static String PRODUCE_REPORT = "ProduceReport";
	final static String PARADISE_BEVERAGES_CONTAINERS_REPORT = "ParadiseBeveragesContainersReport";
	final static String TAG_CONSIGNEE_CALL_SHEET_REPORT = "TagConsigneeCallSheetReport";
	final static String DPORT_CHANGES_REPORT = "DPortChangesReport";
	final static String CUSTOMS_REPORT = "CustomsReport";
	final static String AG_CONTAINER_REPORT = "AGContainerInspectionsReport";
	final static String AG_CTRS_NOT_FLAGGED_REPORT = "AGCtrsNotFlaggedReport";
	final static String MIS_REEFER_REPORT = "MISReeferReport";

	final static String SUP_BLANK_CONSIGNEE_CS_REPORT= "SupBlankConsigneeCSReport";
	final static String SUP_CONSIGNEE_DISCREP_REPORT = "SupConsigneeDiscrepReport";
	final static String SUP_DS_CHANGE_REPORT = "SupDSChangeReport";
	final static String SUP_DPORT_DISCREP_REPORT = "SupDPortDiscrepReport";

	final static String SUPPLEMENAL_REPORT = "SupplementalProblems";	
	final static String MTY_CONTAINER_SEGREGATION_REPORT = "MTYContainerSegregationReport";
	final static String EB_SIT_REPORT = "EBSITReport";
	final static String HONO_AUTO_CNTRS_REPORT = "HonoAutoCntrOnVVReport";
	final static String CNTR_DISCH_BY_CELL_REPORT = "CntrDischByCellReport";
	
	final static String REEFER_CONTAINERS_BARGE_REPORT = "ReeferContainersBargeReport";
	final static String MODIFIED_FLATRACKS_REPORT = "ModifiedFlatracksReport";
	final static String CY_HON_CONTAINER_REPORT = "CyHonContainerReport";
	final static String MATU_CONTAINER_GUAM_FE_REPORT = "MatuContainerForGuamFeReport";
	final static String DAMAGE_CONTAINER_BARGE_REPORT = "DamageContainerBargeReport";
	final static String NO_DAMAGE_CONTAINER_BARGE_REPORT = "NoDamageContainerBargeReport";
	final static String CLIENT_CONTAINER_BARGE_REPORT = "ClientContainerBargeReport"; 
	final static String DAMAGE_BARGE_REPORT = "DamageBargeReport";
	final static String NLT_DISCREPANCIES_REPORT = "NLTDiscrepanciesReport";
	final static String MULTI_CELL_CONTAINER_BARGE_REPORT = "MultiCellContainerBargeReport"; 
	final static String NLT_DISCREPANCIES_BARGE_ERROR_REPORT = "NLTDiscrepanciesBargeErrorReport";
	final static boolean exportToPdf = true;
	final static boolean exportToXls = true;
	final static String DOWNLOAD_DISCREPANCIES_REPORTS = "DownloadDiscrepanciesReports";
	final static String INBOUND_TYPECODE_SUMMARY = "InboundTypecodeSummary";
	final static String CY_DISC_SUM_REPORT = "CyDiscSumReport";
	final static String BARGE_STOWAGE_SUMAMRY_REPORT = "BargeStowageSummaryReport";
	final static String CONSIGNEE_SHIPPER_NOTES = "ConsigneeShipperNotesReport";
	final static String AUTOMATIC_TRUCKER_ASSIGNMENT = "AutomaticTruckerAssignmentReport";
	final static String NOTIFY_PARTY = "NotifyPartyReport";
	final static String HOLDS = "HoldsReport";
	final static String INVALID_NOTIFY_PARTY = "InvalidNotifyParty";
	// final static String CNTRS_STOWED_RORO_REPORT = "CntrsStowedToRoRoPositionsReport"; //A16

	static int tcTotal = 0;

	public static void createReport(String reportName, String vesvoy, 
			ArrayList<SupplementProblems> problems,
			String prefix){

		JasperReport jasperReport = null;
		JasperReport jasperReportXL = null;

		JasperPrint jasperPrint = null;
		JasperPrint jasperPrintXL = null;

		List<JasperPrint> jprintlist = new ArrayList<JasperPrint>();
		List<JasperPrint> jprintlistXL = new ArrayList<JasperPrint>();

		HashMap<String, Object> jasperParameter = new HashMap<String, Object>();
		HashMap<String, Object> jasperParameterXL = new HashMap<String, Object>();
		int mtyTcTotal = 0;
		int outTcTotal = 0;
		int inTcTotal = 0;
		tcTotal = 0;
		TosLookup lookup = null;
		//Properties properties = new Properties();
		List<String> reportFiles = new ArrayList<String>();
		try {
			if(reportName != null && reportName.trim() !="") {
				if(vesvoy != null && vesvoy.trim() != "")
					vesvoy = vesvoy.trim();

				String outputFilePath = System.getProperty("java.io.tmpdir");
				if (outputFilePath!=null && !outputFilePath.endsWith("/")) {
					outputFilePath = outputFilePath+"/";
				}
				String vvd = "";
				String subject = "";
				String outputFileName = "";				
				String from = TosRefDataUtil.getValue("EMAIL_NEWVES_FROM");
				String to = "";	
				String reportFileName = reportName +".jrxml";
				String reportFileNameXL = reportName + "XLS.jrxml";
				logger.info("reportFileName **** "+reportFileName);
				logger.info("reportFileNameXL **** "+reportFileNameXL);
				logger.info("CreateReport() " +reportFileName);

				if(MULTI_CELL_CONTAINER_BARGE_REPORT.equalsIgnoreCase(reportName)){
					reportFileName = "MultiContainerCellsReport" +".jrxml";
					reportFileNameXL = "MultiContainerCellsReport" +"XLS.jrxml";
					logger.info("reportFileNameXL ::::: "+reportFileNameXL);
				}

				Calendar currCal = Calendar.getInstance();
				Date currDate = (Date) currCal.getTime();   
				Boolean isDayLightSaving = currCal.getTimeZone().inDaylightTime(currDate);
				int offset = isDayLightSaving?-3:-2;
				currCal.add(Calendar.HOUR, offset);

				String date = CalendarUtil.convertDateToString(currCal.getTime());

				DateFormat df = new SimpleDateFormat("hh:mm");
				String time = df.format(currCal.getTime());

				jasperParameter.put("Vesvoy", vesvoy);
				jasperParameter.put("FormattedDate", date);
				jasperParameter.put("FormattedTime", time);
				jasperParameterXL.put("Vesvoy", vesvoy);
				jasperParameterXL.put("FormattedDate", date);
				jasperParameterXL.put("FormattedTime", time);
				jasperParameterXL.put(JRParameter.IS_IGNORE_PAGINATION, Boolean.TRUE);
				String ddContent = "";

				if(MTY_CONTAINER_SEGREGATION_REPORT.equalsIgnoreCase(reportName) || REEFER_CONTAINERS_BARGE_REPORT.equalsIgnoreCase(reportName) ||
						MODIFIED_FLATRACKS_REPORT.equalsIgnoreCase(reportName) ||  CY_HON_CONTAINER_REPORT.equalsIgnoreCase(reportName) ||
						MATU_CONTAINER_GUAM_FE_REPORT.equalsIgnoreCase(reportName) || DAMAGE_BARGE_REPORT.equalsIgnoreCase(reportName) ||
						CLIENT_CONTAINER_BARGE_REPORT.equalsIgnoreCase(reportName) || NLT_DISCREPANCIES_REPORT.equalsIgnoreCase(reportName)
						|| MULTI_CELL_CONTAINER_BARGE_REPORT.equalsIgnoreCase(reportName) || NLT_DISCREPANCIES_BARGE_ERROR_REPORT.equalsIgnoreCase(reportName)
						|| EB_SIT_REPORT.equalsIgnoreCase(reportName) || HONO_AUTO_CNTRS_REPORT.equalsIgnoreCase(reportName) || CNTR_DISCH_BY_CELL_REPORT.equalsIgnoreCase(reportName)
						|| BARGE_STOWAGE_SUMAMRY_REPORT.equalsIgnoreCase(reportName)){
					logger.info("vesvoy with Leg value:"+vesvoy);
					logger.info("reportName is "+reportName);
					vvd = vesvoy.substring(0, 6).toString();
					logger.info("vvd :"+vvd);

				}

				if(!(DAMAGE_BARGE_REPORT.equalsIgnoreCase(reportName)) && !reportName.equals(DOWNLOAD_DISCREPANCIES_REPORTS)
						&& !reportName.equals(INBOUND_TYPECODE_SUMMARY)){
					logger.info("Compile reportFileName :"+reportFileName+"   "+reportFileNameXL);
					jasperReport = JasperCompileManager.compileReport(Thread.currentThread().getContextClassLoader().getResourceAsStream(reportFileName));
					jasperReportXL = JasperCompileManager.compileReport(Thread.currentThread().getContextClassLoader().getResourceAsStream(reportFileNameXL));
				}

				if (reportName != null && reportName.equals(DOWNLOAD_DISCREPANCIES_REPORTS)) {					
					jprintlist = new ArrayList<JasperPrint>();
					// BLANK_CONSIGNEE_CS_REPORT - BEGIN
					BlankConsigneeCSReportGenerator ds = new BlankConsigneeCSReportGenerator(vesvoy);
					reportName = BLANK_CONSIGNEE_CS_REPORT;
					/*// PDF
					reportFileName = reportName +".jrxml";					
					jasperReport = JasperCompileManager.compileReport(Thread.currentThread().getContextClassLoader().getResourceAsStream(reportFileName));
					jasperPrint = JasperFillManager.fillReport(jasperReport,jasperParameter, ds);					
					jprintlist.add(jasperPrint);*/
					// HTML
					Div div = new Div();
					div.appendChild( new B().appendText("Requires CS Action/Unapproved Variances"));					
					ddContent = ddContent + div.write()+ new Br().write();
					if(ds.jrBlankConsigneeDetail!=null && ds.jrBlankConsigneeDetail.size()>0)
					{
						Table table = new Table();
						table.setStyle("font-family:Courier New;font-size:12px;");
						table.setCellspacing("10");
						Tr tr = new Tr();
						table.appendChild(tr);
						Td td = new Td();
						String headings[] = {"ContainerNumber", "DPort", "Consignee", "BookingNumber"};
						for(int h=0; h<headings.length; h++) 
						{
							td = new Td();
							td.appendChild(new B().appendText(headings[h]));
							tr.appendChild(td);
						}
						for(int i=0; i<ds.jrBlankConsigneeDetail.size(); i++)
						{
							TosRdsDataFinalMt rds = ds.jrBlankConsigneeDetail.get(i);
							tr = new Tr(); 
							table.appendChild(tr);
							td = new Td();
							td.appendChild(new Text(rds.getContainerNumber()));
							tr.appendChild(td);
							td = new Td();
							td.appendChild(new Text(rds.getDport()));
							tr.appendChild(td);
							td = new Td();
							td.appendChild(new Text(rds.getConsignee()==null?" ":rds.getConsignee()));
							tr.appendChild(td);
							td = new Td();
							td.appendChild(new Text(rds.getBookingNumber()==null?" ":rds.getBookingNumber()));
							tr.appendChild(td);							
						}
						ddContent = ddContent + table.write()+ new Br().write();
					}
					else
					{
						ddContent = ddContent + "-No Unknown Consignees Found"+ new Br().write()+ new Br().write();
					}					
					// BLANK_CONSIGNEE_CS_REPORT - END

					// INCORRECT_DISCHARGE_PORT_REPORT - BEGIN
					IncorrectDischargePortReportGenerator ds1 = new IncorrectDischargePortReportGenerator(vesvoy);
					reportName = INCORRECT_DISCHARGE_PORT_REPORT;
					/*// PDF
					reportFileName = reportName + ".jrxml";					
					jasperReport = JasperCompileManager.compileReport(Thread.currentThread().getContextClassLoader().getResourceAsStream(reportFileName));
					jasperPrint = JasperFillManager.fillReport(jasperReport,jasperParameter, ds1);			
					jprintlist.add(jasperPrint);*/
					// HTML
					div = new Div();
					div.appendChild( new B().appendText("Incorrect Discharge Ports - Confirm discharge ports"));					
					ddContent = ddContent + div.write()+ new Br().write();
					if(ds1.jrIncorrectDiscPortDetail!=null && ds1.jrIncorrectDiscPortDetail.size()>0)
					{
						Table table = new Table();
						table.setStyle("font-family:Courier New;font-size:12px;");
						table.setCellspacing("10");
						Tr tr = new Tr();
						table.appendChild(tr);
						Td td = new Td();
						String headings[] = {"ContainerNumber", "DS", "BookingNumber", "Trade", "LoadPort", "DPort", "Orig.DiscPort", "Corrected.DiscPort"};
						for(int h=0; h<headings.length; h++) 
						{
							td = new Td();
							td.appendChild(new B().appendText(headings[h]));
							tr.appendChild(td);
						}
						for(int i=0; i<ds1.jrIncorrectDiscPortDetail.size(); i++)
						{
							TosRdsDataFinalMt rds = ds1.jrIncorrectDiscPortDetail.get(i);
							tr = new Tr(); 
							table.appendChild(tr);
							td = new Td();
							td.appendChild(new Text(rds.getContainerNumber()));
							tr.appendChild(td);
							td = new Td();
							td.appendChild(new Text(rds.getDs()==null?" ":rds.getDs()));
							tr.appendChild(td);
							td = new Td();
							td.appendChild(new Text(rds.getBookingNumber()==null?" ":rds.getBookingNumber()));
							tr.appendChild(td);
							td = new Td();
							td.appendChild(new Text(rds.getTrade()==null?" ":rds.getTrade()));
							tr.appendChild(td);			
							td = new Td();
							td.appendChild(new Text(rds.getLoadPort()));
							tr.appendChild(td);
							td = new Td();
							td.appendChild(new Text(rds.getDport()));
							tr.appendChild(td);
							td = new Td();
							td.appendChild(new Text(rds.getRetPort()));
							tr.appendChild(td);
							td = new Td();
							td.appendChild(new Text(rds.getDischargePort()));
							tr.appendChild(td);
						}
						ddContent = ddContent + table.write()+ new Br().write();
					}
					else
					{
						ddContent = ddContent + "-No incorrect discharge ports found"+ new Br().write()+ new Br().write();
					}					
					// INCORRECT_DISCHARGE_PORT_REPORT - END
					// MIX_PORT_CONTAINER_REPORT - BEGIN
					MixPortContainerReportGenerator ds2 = new MixPortContainerReportGenerator(vesvoy);
					reportName = MIX_PORT_CONTAINER_REPORT;
					/*// PDF
					reportFileName = reportName +".jrxml";					
					jasperReport = JasperCompileManager.compileReport(Thread.currentThread().getContextClassLoader().getResourceAsStream(reportFileName));
					jasperPrint = JasperFillManager.fillReport(jasperReport,jasperParameter, ds2);			
					jprintlist.add(jasperPrint);*/
					// HTML
					div = new Div();
					div.appendChild( new B().appendText("MIX Port Containers (Autos excluded)"));					
					ddContent = ddContent + div.write()+ new Br().write();
					if(ds2.jrMixPortCtrDetail!=null && ds2.jrMixPortCtrDetail.size()>0)
					{
						Table table = new Table();
						table.setStyle("font-family:Courier New;font-size:12px;");
						table.setCellspacing("10");
						Tr tr = new Tr();
						table.appendChild(tr);
						Td td = new Td();
						String headings[] = {"ContainerNumber", "DPort", "Dir", "Consignee"};
						for(int h=0; h<headings.length; h++) 
						{
							td = new Td();
							td.appendChild(new B().appendText(headings[h]));
							tr.appendChild(td);
						}
						for(int i=0; i<ds2.jrMixPortCtrDetail.size(); i++)
						{
							TosRdsDataFinalMt rds = ds2.jrMixPortCtrDetail.get(i);
							tr = new Tr(); 
							table.appendChild(tr);
							td = new Td();
							td.appendChild(new Text(rds.getContainerNumber()));
							tr.appendChild(td);
							td = new Td();
							td.appendChild(new Text(rds.getDport()));
							tr.appendChild(td);
							td = new Td();
							td.appendChild(new Text(rds.getDir()));
							tr.appendChild(td);
							td = new Td();
							td.appendChild(new Text(rds.getConsignee()==null?" ":rds.getConsignee()));
							tr.appendChild(td);							
						}
						ddContent = ddContent + table.write()+ new Br().write();
					}
					else
					{
						ddContent = ddContent + "-No MIX Port Found"+ new Br().write()+ new Br().write();
					}	

					// MIX_PORT_CONTAINER_REPORT - END
					// MTY_CONTAINER_REPORT - BEGIN
					MTYContainerReportGenerator ds3 = new MTYContainerReportGenerator(vesvoy);
					reportName = MTY_CONTAINER_REPORT;
					/*// PDF
					reportFileName = reportName +".jrxml";					
					jasperReport = JasperCompileManager.compileReport(Thread.currentThread().getContextClassLoader().getResourceAsStream(reportFileName));
					jasperPrint = JasperFillManager.fillReport(jasperReport,jasperParameter, ds3);			
					jprintlist.add(jasperPrint);*/
					// HTML
					div = new Div();
					div.appendChild( new B().appendText("MTY Containers - Please confirm"));					
					ddContent = ddContent + div.write()+ new Br().write();
					if(ds3.jrMtyCtrDetail!=null && ds3.jrMtyCtrDetail.size()>0)
					{
						Table table = new Table();
						table.setStyle("font-family:Courier New;font-size:12px;");
						table.setCellspacing("10");
						Tr tr = new Tr();
						table.appendChild(tr);
						Td td = new Td();
						td.appendChild(new B().appendText("ContainerNumber"));
						tr.appendChild(td);
						for(int i=0; i<ds3.jrMtyCtrDetail.size(); i++)
						{
							TosRdsDataFinalMt rds = ds3.jrMtyCtrDetail.get(i);
							tr = new Tr(); 
							table.appendChild(tr);
							td = new Td();
							td.appendChild(new Text(rds.getContainerNumber()));
							tr.appendChild(td);		
						}
						ddContent = ddContent + table.write()+ new Br().write();
					}
					else
					{
						ddContent = ddContent + "-No MTY Containers Found"+ new Br().write()+ new Br().write();
					}	
					// MTY_CONTAINER_REPORT - END
					// ROB_CONTAINERS_ON_VESSEL_REPORT - BEGIN
					RobContainersOnVesselReportGenerator ds4 = new RobContainersOnVesselReportGenerator(vesvoy);
					reportName = ROB_CONTAINERS_ON_VESSEL_REPORT;
					/*// PDF
					reportFileName = reportName +".jrxml";					
					jasperReport = JasperCompileManager.compileReport(Thread.currentThread().getContextClassLoader().getResourceAsStream(reportFileName));
					jasperPrint = JasperFillManager.fillReport(jasperReport,jasperParameter, ds4);			
					jprintlist.add(jasperPrint);*/
					// HTML
					div = new Div();
					div.appendChild( new B().appendText("ROB Containers on Vessel"));					
					ddContent = ddContent + div.write()+ new Br().write();
					if(ds4.jrRobContainersDetail!=null && ds4.jrRobContainersDetail.size()>0)
					{
						Table table = new Table();
						table.setStyle("font-family:Courier New;font-size:12px;");
						table.setCellspacing("10");
						Tr tr = new Tr();
						table.appendChild(tr);
						Td td = new Td();
						String headings[] = {"ContainerNumber", "Dir", "DPort", "Consignee"};
						for(int h=0; h<headings.length; h++) 
						{
							td = new Td();
							td.appendChild(new B().appendText(headings[h]));
							tr.appendChild(td);
						}
						for(int i=0; i<ds4.jrRobContainersDetail.size(); i++)
						{
							TosRdsDataFinalMt rds = ds4.jrRobContainersDetail.get(i);
							tr = new Tr(); 
							table.appendChild(tr);
							td = new Td();
							td.appendChild(new Text(rds.getContainerNumber()));
							tr.appendChild(td);
							td = new Td();
							td.appendChild(new Text(rds.getDir()));
							tr.appendChild(td);	
							td = new Td();
							td.appendChild(new Text(rds.getDport()));
							tr.appendChild(td);
							td = new Td();
							td.appendChild(new Text(rds.getConsignee()==null?" ":rds.getConsignee()));
							tr.appendChild(td);						
						}
						ddContent = ddContent + table.write()+ new Br().write();
					}
					else
					{
						ddContent = ddContent + "-No ROB Containers found matching Trash Table containers"+ new Br().write()+ new Br().write();
					}	
					//
					// Reefer Temperature scale missing containers 
					div = new Div();
					div.appendChild( new B().appendText("Containers missing temperature unit : verify temperature"));					
					ddContent = ddContent + div.write()+ new Br().write();
					ReeferForFandMContainersReportGenerator reefer = new ReeferForFandMContainersReportGenerator(vesvoy);
					ArrayList<String> tempUnitMissingCntrs = new ArrayList<String>();
					if(reefer.jrReeferDetail!=null && reefer.jrReeferDetail.size()>0)
					{
						for (int i=0; i<reefer.jrReeferDetail.size(); i++)
						{
							TosRdsDataFinalMt rds = reefer.jrReeferDetail.get(i);
							if(rds.getTempMeasurementUnit()==null||rds.getTempMeasurementUnit().equals(""))
								tempUnitMissingCntrs.add(rds.getContainerNumber());
						}						
					}
					if(tempUnitMissingCntrs.size()>0)
					{
						Table table = new Table();
						table.setStyle("font-family:Courier New;font-size:12px;");
						table.setCellspacing("10");
						Tr tr = new Tr();
						table.appendChild(tr);
						Td td = new Td();
						td.appendChild(new B().appendText("ContainerNumber"));
						tr.appendChild(td);
						for(int i=0; i<tempUnitMissingCntrs.size(); i++)
						{
							tr = new Tr(); 
							table.appendChild(tr);
							td = new Td();
							td.appendChild(new Text(tempUnitMissingCntrs.get(i)));
							tr.appendChild(td);
						}
						ddContent = ddContent + table.write()+ new Br().write();
					}
					else
					{
						ddContent = ddContent + "-No containers found"+ new Br().write()+ new Br().write();
					}
					
					
					// A17 - Return to shipper report 
					div = new Div();
					div.appendChild( new B().appendText("Full containers with dray status as Return to Shipper"));					
					ddContent = ddContent + div.write()+ new Br().write();
					ReturnToShipperGenerator returnToShip = new ReturnToShipperGenerator(vesvoy);
					ArrayList<String> tempreturnToShipCntrs = new ArrayList<String>();
					if(returnToShip.jrFullRtnShipperDetail!=null && returnToShip.jrFullRtnShipperDetail.size()>0)
					{
						for (int i=0; i<returnToShip.jrFullRtnShipperDetail.size(); i++)
						{
							TosRdsDataFinalMt rds = returnToShip.jrFullRtnShipperDetail.get(i);
								tempreturnToShipCntrs.add(rds.getContainerNumber());
						}						
					}
					if(tempreturnToShipCntrs.size()>0)
					{
						Table table = new Table();
						table.setStyle("font-family:Courier New;font-size:12px;");
						table.setCellspacing("10");
						Tr tr = new Tr();
						table.appendChild(tr);
						Td td = new Td();
						td.appendChild(new B().appendText("ContainerNumber"));
						tr.appendChild(td);
						for(int i=0; i<tempreturnToShipCntrs.size(); i++)
						{
							tr = new Tr(); 
							table.appendChild(tr);
							td = new Td();
							td.appendChild(new Text(tempreturnToShipCntrs.get(i)));
							tr.appendChild(td);
						}
						ddContent = ddContent + table.write()+ new Br().write();
					}
					else
					{
						ddContent = ddContent + "-No containers found"+ new Br().write()+ new Br().write();
					}

					//
					reportName = DOWNLOAD_DISCREPANCIES_REPORTS;
					if(prefix != null && prefix.trim() != "") {
						logger.info("This is preview download report");
						subject=prefix+"Download Discrepancies Report"+" for "+vesvoy;
						to = TosRefDataUtil.getValue("MAIL_DWLD_DISCRE_PRE");
					}
					else {
						logger.info("This is download report");
						subject="Download Discrepancies Report"+" for "+vesvoy;
						to = TosRefDataUtil.getValue("MAIL_DOWNLOAD_DISCRE");
					}
					outputFileName = subject.replace(' ', '_');
					
				}
				else if (reportName != null && reportName.equals( MULTI_CONTAINER_CELLS_REPORT )) {
					MultiContainerCellsReportGenerator ds5 = new MultiContainerCellsReportGenerator(vesvoy);
					jasperPrint = JasperFillManager.fillReport(jasperReport,jasperParameter, ds5);
					jasperPrintXL = JasperFillManager.fillReport(jasperReportXL,jasperParameterXL, new MultiContainerCellsReportGenerator(vesvoy));
					if(vesvoy != null && vesvoy.trim() != "")
						subject="Multiple Containers Per Cell Report"+" for "+vesvoy;
					else
						subject="Multiple Containers Per Cell Report";				

					outputFileName = subject.replace(' ', '_');
					to = TosRefDataUtil.getValue("MAIL_MULTI_NEWVES");	
				}
				else if (reportName != null && reportName.equals("VVDDownloadReport")) {
					//DownloadVesselReportGenerator downloadVesselReportGenerator = new DownloadVesselReportGenerator(vesvoy);
					//jasperPrint = JasperFillManager.fillReport(jasperReport,jasperParameter, downloadVesselReportGenerator);
					jasperPrintXL = JasperFillManager.fillReport(jasperReportXL,jasperParameterXL, new DownloadVesselReportGenerator(vesvoy));
					if(vesvoy != null && vesvoy.trim() != "")
						subject="GUM Vessel Data for "+vesvoy;
					else
						subject="GUM Vessel Data";				

					outputFileName = subject.replace(' ', '_');
					to = TosRefDataUtil.getValue("GUM_VESSEL_DATA");	 
				}
				else if (reportName != null && reportName.equals(DUPLICATE_CONTAINER_REPORT)) {
					/*DuplicateContainerReportGenerator ds6 = new DuplicateContainerReportGenerator(vesvoy);
					jasperPrint = JasperFillManager.fillReport(jasperReport,jasperParameter, ds6);
					jasperPrintXL = JasperFillManager.fillReport(jasperReportXL,jasperParameter, ds6);
					if(vesvoy != null && vesvoy.trim() != "")
						subject="Duplicate Containers Report"+" for "+vesvoy;
					else
						subject="Duplicate Containers Report";				

					outputFileName = subject.replace(' ', '_');
					to = TosRefDataUtil.getValue("MAIL_DUP_NEWVES");	*/
				}
				else if (reportName != null && reportName.equals(REEFER_FOR_CONTAINERS_REPORT)) {
					ReeferForFandMContainersReportGenerator ds7 = new ReeferForFandMContainersReportGenerator(vesvoy);
					jasperPrint = JasperFillManager.fillReport(jasperReport,jasperParameter, ds7);
					jasperPrintXL = JasperFillManager.fillReport(jasperReportXL,jasperParameterXL, new ReeferForFandMContainersReportGenerator(vesvoy));
					if(vesvoy != null && vesvoy.trim() != "")
						subject="Reefer For F and M Containers Report"+" for "+vesvoy;
					else
						subject="Reefer For F and M Containers Report";				

					outputFileName = subject.replace(' ', '_');
					to = TosRefDataUtil.getValue("MAIL_REEFR_NEWVES");	
				}
				else if (reportName != null && reportName.equals(SF_TAG_REPORT)) {
					/*jasperPrint = JasperFillManager.fillReport(jasperReport,jasperParameter, new SFTagReportGenerator(vesvoy));
					if(vesvoy != null && vesvoy.trim() != "")
						subject="SF Tag Report"+" for "+vesvoy;
					else
						subject="SF Tag Report";				

					outputFileName = subject.replace(' ', '_');
					to = TosRefDataUtil.getValue("MAIL_SF_NEWVES");	*/
				}
				else if (reportName != null && reportName.equals( DAMAGE_CODE_REPORT)) {
					String totalNumberOfContainers =  getTotalNumberOfContainers(vesvoy);
					jasperParameter.put("TotalNumberOfContainers", totalNumberOfContainers);
					jasperParameterXL.put("TotalNumberOfContainers", totalNumberOfContainers);
					DamageCodeReportGenerator ds8 = new DamageCodeReportGenerator(vesvoy);
					jasperParameter.put("TotalDamagedCtrs", ds8.totalDamagedCtrs+"");
					jasperParameterXL.put("TotalDamagedCtrs", ds8.totalDamagedCtrs+"");
					jasperPrint = JasperFillManager.fillReport(jasperReport,jasperParameter, ds8);
					jasperPrintXL = JasperFillManager.fillReport(jasperReportXL,jasperParameterXL, new DamageCodeReportGenerator(vesvoy));
					if(vesvoy != null && vesvoy.trim() != "")
						subject="Damage Code Report"+" for "+vesvoy;
					else
						subject="Damage Code Report";				

					outputFileName = subject.replace(' ', '_');
					to = TosRefDataUtil.getValue("MAIL_DAMAGE_NEWVES");	
				}
				else if (reportName != null && reportName.equals(PRODUCE_REPORT)) {
					ProduceReportGenerator ds9 = new ProduceReportGenerator(vesvoy);
					jasperPrint = JasperFillManager.fillReport(jasperReport,jasperParameter, ds9);
					jasperPrintXL = JasperFillManager.fillReport(jasperReportXL,jasperParameterXL, new ProduceReportGenerator(vesvoy));
					if(vesvoy != null && vesvoy.trim() != "")
						subject="Produce Report"+" for "+vesvoy;
					else
						subject="Produce Report";				

					outputFileName = subject.replace(' ', '_');
					to = TosRefDataUtil.getValue("MAIL_PRODUCE_NEWVES");	
				}
				else if (reportName != null && reportName.equals(PARADISE_BEVERAGES_CONTAINERS_REPORT )) {
					ParadiseBeveragesContainersReportGenerator ds10 = new ParadiseBeveragesContainersReportGenerator(vesvoy);
					jasperPrint = JasperFillManager.fillReport(jasperReport,jasperParameter, ds10);
					jasperPrintXL = JasperFillManager.fillReport(jasperReportXL,jasperParameterXL, new ParadiseBeveragesContainersReportGenerator(vesvoy));
					if(vesvoy != null && vesvoy.trim() != "")
						subject="Paradise Beverages Containers Report"+" for "+vesvoy;
					else
						subject="Paradise Beverages Containers Report";				

					outputFileName = subject.replace(' ', '_');
					to = TosRefDataUtil.getValue("MAIL_PARBEV_NEWVES");	
				}
				else if (reportName != null && reportName.equals(TAG_CONSIGNEE_CALL_SHEET_REPORT)) {
					TagConsigneeCallSheetReportGenerator ds11 = new TagConsigneeCallSheetReportGenerator(vesvoy);
					jasperPrint = JasperFillManager.fillReport(jasperReport,jasperParameter, ds11);
					jasperPrintXL = JasperFillManager.fillReport(jasperReportXL,jasperParameterXL, new TagConsigneeCallSheetReportGenerator(vesvoy));
					if(vesvoy != null && vesvoy.trim() != "")
						subject="Tag Consignee Call Sheet Report"+" for "+vesvoy;
					else
						subject="Tag Consignee Call Sheet Report";				

					outputFileName = subject.replace(' ', '_');
					to = TosRefDataUtil.getValue("MAIL_TAGCON_NEWVES");	
				}
				else if (reportName != null && reportName.equals(DPORT_CHANGES_REPORT)) {
					DPortChangesReportGenerator ds12 =  new DPortChangesReportGenerator(vesvoy);
					jasperPrint = JasperFillManager.fillReport(jasperReport,jasperParameter, ds12);
					jasperPrintXL = JasperFillManager.fillReport(jasperReportXL,jasperParameterXL, new DPortChangesReportGenerator(vesvoy));
					if(vesvoy != null && vesvoy.trim() != "")
						subject="Dport Changes After LTV Report"+" for "+vesvoy;
					else
						subject="Dport Changes After LTV Report";				

					outputFileName = subject.replace(' ', '_');
					to = TosRefDataUtil.getValue("MAIL_DPORT_NEWVES");	
				}
				else if (reportName != null && reportName.equals(CUSTOMS_REPORT)) {
					CustomsReportGenerator ds13 = new CustomsReportGenerator(vesvoy);
					jasperPrint = JasperFillManager.fillReport(jasperReport,jasperParameter, ds13);
					jasperPrintXL = JasperFillManager.fillReport(jasperReportXL,jasperParameterXL, new CustomsReportGenerator(vesvoy));
					if(vesvoy != null && vesvoy.trim() != "")
						subject="Customs Report"+" for "+vesvoy;
					else
						subject="Customs Report";				

					outputFileName = subject.replace(' ', '_');
					to = TosRefDataUtil.getValue("MAIL_CUST_NEWVES");	
				}
				else if (reportName != null && reportName.equals(AG_CONTAINER_REPORT)) {
					AGContainerInspectionsReportGenerator ds14 = new AGContainerInspectionsReportGenerator(vesvoy);
					jasperPrint = JasperFillManager.fillReport(jasperReport,jasperParameter, ds14);
					jasperPrintXL = JasperFillManager.fillReport(jasperReportXL,jasperParameterXL, new AGContainerInspectionsReportGenerator(vesvoy));
					if(vesvoy != null && vesvoy.trim() != "")
						subject="AGContainer Inspections Report"+" for "+vesvoy;
					else
						subject="AGContainer Inspections Report";				

					outputFileName = subject.replace(' ', '_');
					to = TosRefDataUtil.getValue("MAIL_AG_NEWVES");	
				}
				else if (reportName != null && reportName.equals(AG_CTRS_NOT_FLAGGED_REPORT)) {
					jasperPrint = JasperFillManager.fillReport(jasperReport,jasperParameter, new AGCtrsNotFlaggedReportGenerator(vesvoy));
					jasperPrintXL = JasperFillManager.fillReport(jasperReportXL,jasperParameterXL, new AGCtrsNotFlaggedReportGenerator(vesvoy));
					if(vesvoy != null && vesvoy.trim() != "")
						subject="AG Ctrs Not Flagged AG Report"+" for "+vesvoy;
					else
						subject="AG Ctrs Not Flagged AG Report";				

					outputFileName = subject.replace(' ', '_');
					to = TosRefDataUtil.getValue("MAIL_AG_NEWVES");	
				}
				// This report is commeneted becuase this is developed in N4 in newVesComleted service event flow.
			/*	else if (reportName != null && reportName.equals(CNTRS_STOWED_RORO_REPORT)) { // A16
					jasperPrint = JasperFillManager.fillReport(jasperReport,jasperParameter, new CntrsStowedToRoRoPositions(vesvoy));
					jasperPrintXL = JasperFillManager.fillReport(jasperReportXL,jasperParameterXL, new CntrsStowedToRoRoPositions(vesvoy));
					if(vesvoy != null && vesvoy.trim() != "")
						subject="Ctrs Stowed to RORO positions on "+vesvoy;
					else
						subject="Ctrs Stowed to RORO positions";				

					outputFileName = subject.replace(' ', '_');
					to = TosRefDataUtil.getValue("MAIL_RORO_NEWVES");	
				}*/ // A16
				else if (reportName != null && reportName.equals(MIS_REEFER_REPORT)) {
					/*jasperPrint = JasperFillManager.fillReport(jasperReport,jasperParameter, new MISReeferReportGenerator(vesvoy));
					if(vesvoy != null && vesvoy.trim() != "")
						subject="MIS Reefer Reportt"+" for "+vesvoy;
					else
						subject="MIS Reefer Report";				

					outputFileName = subject.replace(' ', '_');
					to = TosRefDataUtil.getValue("MAIL_MISREF_NEWVES");	*/
				}
				else if (reportName != null && reportName.equals(CONSIGNEE_SHIPPER_NOTES)) {
					logger.info("before calling consigneeshippernotes/trucker/N/P reports");
					logger.info("Generating Consignee_Shipper_Notes report");
					jasperPrint = JasperFillManager.fillReport(jasperReport,jasperParameter, new ConsigneeShipperNotesGenerator(vesvoy));
					jasperPrintXL = JasperFillManager.fillReport(jasperReportXL,jasperParameterXL, new ConsigneeShipperNotesGenerator(vesvoy));
					exportPdf(reportName, jasperPrint, jprintlist, outputFilePath + "Consignee_Shipper_Notes_"+vesvoy+".pdf");
					exportXls(reportName, jasperPrintXL, jprintlistXL, outputFilePath + "Consignee_Shipper_Notes_"+vesvoy+".xls");
					reportFiles.add(outputFilePath + "Consignee_Shipper_Notes_"+vesvoy+".pdf");
					reportFiles.add(outputFilePath + "Consignee_Shipper_Notes_"+vesvoy+".xls");
					logger.info("Generating Notify_Party_Report report");
					reportName = NOTIFY_PARTY;
					reportFileName = reportName +".jrxml";
					reportFileNameXL = reportName + "XLS.jrxml";
					jasperReport = JasperCompileManager.compileReport(Thread.currentThread().getContextClassLoader().getResourceAsStream(reportFileName));
					jasperReportXL = JasperCompileManager.compileReport(Thread.currentThread().getContextClassLoader().getResourceAsStream(reportFileNameXL));
					jasperPrint = JasperFillManager.fillReport(jasperReport,jasperParameter, new NotifyPartyReportGenerator(vesvoy));
					jasperPrintXL = JasperFillManager.fillReport(jasperReportXL,jasperParameterXL, new NotifyPartyReportGenerator(vesvoy));
					exportPdf(reportName, jasperPrint, jprintlist, outputFilePath + "Notify_Party_Report_"+vesvoy+".pdf");
					exportXls(reportName, jasperPrintXL, jprintlistXL, outputFilePath + "Notify_Party_Report_"+vesvoy+".xls");
					reportFiles.add(outputFilePath + "Notify_Party_Report_"+vesvoy+".pdf");
					reportFiles.add(outputFilePath + "Notify_Party_Report_"+vesvoy+".xls");
					logger.info("Generating Automatic_Trucker_Assignment_Report report");
					reportName = AUTOMATIC_TRUCKER_ASSIGNMENT;
					reportFileName = reportName +".jrxml";
					reportFileNameXL = reportName + "XLS.jrxml";
					jasperReport = JasperCompileManager.compileReport(Thread.currentThread().getContextClassLoader().getResourceAsStream(reportFileName));
					jasperReportXL = JasperCompileManager.compileReport(Thread.currentThread().getContextClassLoader().getResourceAsStream(reportFileNameXL));
					jasperPrint = JasperFillManager.fillReport(jasperReport,jasperParameter, new AutomaticTruckerAssignmentReportGenerator(vesvoy));
					jasperPrintXL = JasperFillManager.fillReport(jasperReportXL,jasperParameterXL, new AutomaticTruckerAssignmentReportGenerator(vesvoy));
					exportPdf(reportName, jasperPrint, jprintlist, outputFilePath + "Automatic_Trucker_Assignment_Report_"+vesvoy+".pdf");
					exportXls(reportName, jasperPrintXL, jprintlistXL, outputFilePath + "Automatic_Trucker_Assignment_Report_"+vesvoy+".xls");
					reportFiles.add(outputFilePath + "Automatic_Trucker_Assignment_Report_"+vesvoy+".pdf");
					reportFiles.add(outputFilePath + "Automatic_Trucker_Assignment_Report_"+vesvoy+".xls");
					//
					reportName = CONSIGNEE_SHIPPER_NOTES;
					logger.info("reportFiles :-"+reportFiles);
					if(vesvoy != null && vesvoy.trim() != "")
						subject="Consignee Shipper Notes, Automatic & N/P Trucker reports for "+vesvoy;
					else
						subject="Consignee Shipper Notes, Automatic & N/P Trucker reports";
					to = TosRefDataUtil.getValue("EMAIL_NV_TO_CMC");	
				}
				else if (reportName!=null && reportName.equals(HOLDS)) {
					logger.info("before calling Holds Report");
					jasperPrint = JasperFillManager.fillReport(jasperReport,jasperParameter, new HoldsReportGenerator(vesvoy));
					jasperPrintXL = JasperFillManager.fillReport(jasperReportXL,jasperParameterXL, new HoldsReportGenerator(vesvoy));
					if(vesvoy != null && vesvoy.trim() != "")
						subject="Holds Report for "+vesvoy;
					else
						subject="Holds Report";				

					outputFileName = subject.replace(' ', '_');
					to = TosRefDataUtil.getValue("MAIL_ALL_HOLDS");
				}
				else if (reportName!=null && reportName.equals(INVALID_NOTIFY_PARTY)) {
					logger.info("before calling Invalid notify party Report");
					jasperPrint = JasperFillManager.fillReport(jasperReport,jasperParameter, new InvalidNotifyPartyReportGenerator(vesvoy));
					jasperPrintXL = JasperFillManager.fillReport(jasperReportXL,jasperParameterXL, new InvalidNotifyPartyReportGenerator(vesvoy));
					if(vesvoy != null && vesvoy.trim() != "")
						subject="Invalid notify report for "+vesvoy;
					else
						subject="Invalid notify Report";				

					outputFileName = subject.replace(' ', '_');
					to = TosRefDataUtil.getValue("MAIL_INVALID_NP");
				}
				else if (reportName != null && reportName.equals(SUPPLEMENAL_REPORT)) {
					/*jprintlist = new ArrayList<JasperPrint>();
					jprintlistXL = new ArrayList<JasperPrint>();

					SupBlankConsigneeCSReportGenerator ds15 = new SupBlankConsigneeCSReportGenerator(rdsDataFinalListUpdate);
					reportName = SUP_BLANK_CONSIGNEE_CS_REPORT;
					reportFileName = reportName +".jrxml";					
					jasperReport = JasperCompileManager.compileReport(Thread.currentThread().getContextClassLoader().getResourceAsStream(reportFileName));
					jasperPrint = JasperFillManager.fillReport(jasperReport,jasperParameter, ds15);					
					jprintlist.add(jasperPrint);
					reportFileNameXL = reportName +"XLS.jrxml";					
					jasperReportXL = JasperCompileManager.compileReport(Thread.currentThread().getContextClassLoader().getResourceAsStream(reportFileNameXL));
					jasperPrintXL = JasperFillManager.fillReport(jasperReportXL,jasperParameterXL, new SupBlankConsigneeCSReportGenerator(rdsDataFinalListUpdate));					
					jprintlistXL.add(jasperPrintXL);

					SupConsigneeDiscrepReportGenerator ds16 = new SupConsigneeDiscrepReportGenerator(rdsDataFinalListTDP, rdsDataFinalListUpdate);
					reportName = SUP_CONSIGNEE_DISCREP_REPORT;
					reportFileName = reportName +".jrxml";					
					jasperReport = JasperCompileManager.compileReport(Thread.currentThread().getContextClassLoader().getResourceAsStream(reportFileName));
					jasperPrint = JasperFillManager.fillReport(jasperReport,jasperParameter, ds16);
					jprintlist.add(jasperPrint);
					reportFileNameXL = reportName +"XLS.jrxml";					
					jasperReportXL = JasperCompileManager.compileReport(Thread.currentThread().getContextClassLoader().getResourceAsStream(reportFileNameXL));
					jasperPrintXL = JasperFillManager.fillReport(jasperReportXL,jasperParameterXL, new SupConsigneeDiscrepReportGenerator(rdsDataFinalListTDP, rdsDataFinalListUpdate));
					jprintlistXL.add(jasperPrintXL);

					SupDSChangeReportGenerator ds17 = new SupDSChangeReportGenerator(rdsDataFinalListTDP, rdsDataFinalListUpdate);
					reportName = SUP_DS_CHANGE_REPORT;
					reportFileName = reportName +".jrxml";					
					jasperReport = JasperCompileManager.compileReport(Thread.currentThread().getContextClassLoader().getResourceAsStream(reportFileName));
					jasperPrint = JasperFillManager.fillReport(jasperReport,jasperParameter, ds17);
					jprintlist.add(jasperPrint);
					reportFileNameXL = reportName +"XLS.jrxml";					
					jasperReportXL = JasperCompileManager.compileReport(Thread.currentThread().getContextClassLoader().getResourceAsStream(reportFileNameXL));
					jasperPrintXL = JasperFillManager.fillReport(jasperReportXL,jasperParameterXL, new SupDSChangeReportGenerator(rdsDataFinalListTDP, rdsDataFinalListUpdate));
					jprintlistXL.add(jasperPrintXL);

					SupDPortDiscrepReportGenerator ds18 = new SupDPortDiscrepReportGenerator(rdsDataFinalListTDP, rdsDataFinalListUpdate);
					reportName = SUP_DPORT_DISCREP_REPORT;
					reportFileName = reportName +".jrxml";					
					jasperReport = JasperCompileManager.compileReport(Thread.currentThread().getContextClassLoader().getResourceAsStream(reportFileName));
					jasperPrint= JasperFillManager.fillReport(jasperReport,jasperParameter, ds18);
					jprintlist.add(jasperPrint);
					reportFileNameXL = reportName +"XLS.jrxml";					
					jasperReportXL = JasperCompileManager.compileReport(Thread.currentThread().getContextClassLoader().getResourceAsStream(reportFileNameXL));
					jasperPrintXL = JasperFillManager.fillReport(jasperReportXL,jasperParameterXL, new SupDPortDiscrepReportGenerator(rdsDataFinalListTDP, rdsDataFinalListUpdate));
					jprintlistXL.add(jasperPrintXL);

					reportName  = SUPPLEMENAL_REPORT;
					if(vesvoy != null && vesvoy.trim() != "")
						subject="Supplemental Reports"+" for "+vesvoy;
					else
						subject="Supplemental Reports";			*/
					jasperPrint = JasperFillManager.fillReport(jasperReport,jasperParameter, new SupplementProblemsReportGenerator(problems));
					jasperPrintXL = JasperFillManager.fillReport(jasperReportXL,jasperParameterXL, new SupplementProblemsReportGenerator(problems));
					subject="TOS AUTOMATIC-SUPPLEMENTAL UPDATE PROBLEMS";
					outputFileName = subject.replace(' ', '_');
					to = TosRefDataUtil.getValue("MAIL_SUPP_PBLMS");// A14

				}else if (reportName != null && reportName.equals(MTY_CONTAINER_SEGREGATION_REPORT)) {
					MTYContainerSegregationReportGenerator ds19 = new MTYContainerSegregationReportGenerator(vesvoy);
					jasperPrint = JasperFillManager.fillReport(jasperReport,jasperParameter, ds19);
					jasperPrintXL = JasperFillManager.fillReport(jasperReportXL,jasperParameterXL, new MTYContainerSegregationReportGenerator(vesvoy));
					if(vesvoy != null && vesvoy.trim() != "")
						subject="MTY Container Segregation Report"+" for "+vesvoy;
					else
						subject="MTY Container Segregation Report";				

					outputFileName = subject.replace(' ', '_');
					to = TosRefDataUtil.getValue("MAIL_MTY_SEG_BARGE");	
				}else if (reportName != null && reportName.equals(EB_SIT_REPORT)) {
					EBSITReportGenerator ebsit = new EBSITReportGenerator(vesvoy);
					jasperPrint = JasperFillManager.fillReport(jasperReport,jasperParameter, ebsit);
					jasperPrintXL = JasperFillManager.fillReport(jasperReportXL,jasperParameterXL, new EBSITReportGenerator(vesvoy));
					if(vesvoy != null && vesvoy.trim() != "")
						subject="EB SIT Containers"+" on "+vesvoy;
					else
						subject="EB SIT Containers Report";				

					outputFileName = subject.replace(' ', '_');
					to = TosRefDataUtil.getValue("MAIL_EB_SIT_BARGE");
				}else if (reportName != null && reportName.equals(HONO_AUTO_CNTRS_REPORT)) {
					HonoAutoCntrsReportGenerator honoAutoCntr = new HonoAutoCntrsReportGenerator(vesvoy);
					jasperPrint = JasperFillManager.fillReport(jasperReport,jasperParameter, honoAutoCntr);
					jasperPrintXL = JasperFillManager.fillReport(jasperReportXL,jasperParameterXL, new HonoAutoCntrsReportGenerator(vesvoy));
					if(vesvoy != null && vesvoy.trim() != "")
						subject="HONO Auto Containers "+" on "+vesvoy;
					else
						subject="HONO Auto Containers Report";				

					outputFileName = subject.replace(' ', '_');
					to = TosRefDataUtil.getValue("MAIL_HONAUTCTR_BARGE");
				}else if (reportName != null && reportName.equals(CNTR_DISCH_BY_CELL_REPORT)) {
					ContainerDischargeByCellReportGenerator aleRpt = new ContainerDischargeByCellReportGenerator(vesvoy);
					jasperPrint = JasperFillManager.fillReport(jasperReport,jasperParameter, aleRpt);
					jasperPrintXL = JasperFillManager.fillReport(jasperReportXL,jasperParameterXL, new ContainerDischargeByCellReportGenerator(vesvoy));
					if(vesvoy != null && vesvoy.trim() != "")
						subject="Container Discharge report by Cell "+" from "+vesvoy;
					else
						subject="Container Discharge report by Cell ";				

					outputFileName = subject.replace(' ', '_');
					to = TosRefDataUtil.getValue("MAIL_CTRDISCEL_BARGE");
					//CNTR_DISCH_BY_CELL_REPORT
				}
				else if (reportName != null && reportName.equals(REEFER_CONTAINERS_BARGE_REPORT)) {
					ReeferContainerReportGenerator ds20 = new ReeferContainerReportGenerator(vesvoy);
					jasperPrint = JasperFillManager.fillReport(jasperReport,jasperParameter, ds20);
					jasperPrintXL = JasperFillManager.fillReport(jasperReportXL,jasperParameterXL, new ReeferContainerReportGenerator(vesvoy));
					if(vesvoy != null && vesvoy.trim() != "")
						subject="Reefer Container Report"+" for "+vesvoy;
					else
						subject="Reefer Container Report";				

					outputFileName = subject.replace(' ', '_');
					to = TosRefDataUtil.getValue("MAIL_REEFER_BARGE");	
				}
				else if (reportName != null && reportName.equals(MODIFIED_FLATRACKS_REPORT)) {
					ModifiedFlatracksReportGenerator ds21 = new ModifiedFlatracksReportGenerator(vesvoy);
					jasperPrint = JasperFillManager.fillReport(jasperReport,jasperParameter, ds21);
					jasperPrintXL = JasperFillManager.fillReport(jasperReportXL,jasperParameterXL, new ModifiedFlatracksReportGenerator(vesvoy));
					if(vesvoy != null && vesvoy.trim() != "")
						subject="Modified Flatrack(s) Report"+" for "+vesvoy;
					else
						subject="Modified Flatrack(s) Report";				

					outputFileName = subject.replace(' ', '_');
					to = TosRefDataUtil.getValue("MAIL_MODFLAT_BARGE");	
				}
				else if (reportName != null && reportName.equals(CY_HON_CONTAINER_REPORT)) {
					CYHONContainerReportGenerator ds22 = new CYHONContainerReportGenerator(vesvoy);
					jasperPrint = JasperFillManager.fillReport(jasperReport,jasperParameter, ds22);
					jasperPrintXL = JasperFillManager.fillReport(jasperReportXL,jasperParameterXL, new CYHONContainerReportGenerator(vesvoy));
					if(vesvoy != null && vesvoy.trim() != "")
						subject="CY Hon Container Report"+" for "+vesvoy;
					else
						subject="CY Hon Container Report";				

					outputFileName = subject.replace(' ', '_');
					to = TosRefDataUtil.getValue("MAIL_CYHON_BARGE");	
				}
				else if (reportName != null && reportName.equals(MATU_CONTAINER_GUAM_FE_REPORT)) {
					MatuContainerForGuamFeReportGenerator ds23 = new MatuContainerForGuamFeReportGenerator(vesvoy);
					jasperPrint = JasperFillManager.fillReport(jasperReport,jasperParameter, ds23);
					jasperPrintXL = JasperFillManager.fillReport(jasperReportXL,jasperParameterXL, new MatuContainerForGuamFeReportGenerator(vesvoy));
					if(vesvoy != null && vesvoy.trim() != "")
						subject="MATU Ctrs Report"+" from "+vesvoy +" for Guam or FE";
					else
						subject="MATU Ctrs Report for Guam orFE Report";				

					outputFileName = subject.replace(' ', '_');
					to = TosRefDataUtil.getValue("MAIL_MATU_BARGE");	
				}else if (reportName != null && reportName.equals(CLIENT_CONTAINER_BARGE_REPORT)) {
					ClientContainersReportGenerator ds24 = new ClientContainersReportGenerator(vesvoy);
					jasperPrint = JasperFillManager.fillReport(jasperReport,jasperParameter, ds24);
					jasperPrintXL = JasperFillManager.fillReport(jasperReportXL,jasperParameterXL, new ClientContainersReportGenerator(vesvoy));
					if(vesvoy != null && vesvoy.trim() != "")
						subject="Client Containers Report"+" for "+vesvoy;
					else
						subject="Client Containers Report";				

					outputFileName = subject.replace(' ', '_');
					to = TosRefDataUtil.getValue("MAIL_CLIENT_BARGE");
				}else if (reportName != null && reportName.equals(NLT_DISCREPANCIES_REPORT)) {
					List resultList = (ArrayList)NewReportVesselDao.getNumberODRecords(vesvoy);
					JRMapCollectionDataSource jrDataSource = new JRMapCollectionDataSource(resultList);
					jasperPrint = JasperFillManager.fillReport(jasperReport,jasperParameter, jrDataSource);
					jasperPrintXL = JasperFillManager.fillReport(jasperReportXL,jasperParameterXL, new JRMapCollectionDataSource(resultList));
					if(vesvoy != null && vesvoy.trim() != "")
						subject="Barge Newves Totals Report"+" for "+vesvoy;
					else
						subject="Barge Newves Totals Report";				

					outputFileName = subject.replace(' ', '_');
					String loadPort = NewReportVesselDao.getLoadPort(vesvoy);
					logger.info("Loadport in NLT Barge totals report :"+loadPort);
					loadPort = loadPort==null?"":loadPort;
					if ("KAH".equalsIgnoreCase(loadPort))
						to = TosRefDataUtil.getValue("MAIL_NI_BGE_STOW_KAH");
					else if ("NAW".equalsIgnoreCase(loadPort))
						to = TosRefDataUtil.getValue("MAIL_NI_BGE_STOW_NAW");
					else if ("KHI".equalsIgnoreCase(loadPort) || "HIL".equalsIgnoreCase(loadPort))
						to = TosRefDataUtil.getValue("MAIL_NI_BGE_STOW_HIL");
					else 
						to = TosRefDataUtil.getValue("MAIL_NLT_BARGE");	
				}else if (reportName != null && reportName.equals(MULTI_CELL_CONTAINER_BARGE_REPORT)) {
					List<TosStowPlanCntrMt> resultList = (ArrayList)NewReportVesselDao.getMultiCellContainerBarge(vesvoy);
					JRMapCollectionDataSource jrDataSource = new JRMapCollectionDataSource(resultList);
					jasperPrint = JasperFillManager.fillReport(jasperReport,jasperParameter, jrDataSource);
					jasperPrintXL = JasperFillManager.fillReport(jasperReportXL,jasperParameterXL, new JRMapCollectionDataSource(resultList));
					if(vesvoy != null && vesvoy.trim() != "")
						subject="Multi Cell Container Report"+" for "+vesvoy;
					else
						subject="Multi Cell Container Report";				

					outputFileName = subject.replace(' ', '_');
					to = TosRefDataUtil.getValue("MAIL_NLT_BARGE");
				}else if (reportName != null && reportName.equals(NLT_DISCREPANCIES_BARGE_ERROR_REPORT)) {
					NLTDiscrepanciesReportGenerator ds25 = new NLTDiscrepanciesReportGenerator(vesvoy);
					jasperPrint = JasperFillManager.fillReport(jasperReport,jasperParameter, ds25);
					jasperPrintXL = JasperFillManager.fillReport(jasperReportXL,jasperParameterXL, new NLTDiscrepanciesReportGenerator(vesvoy));
					if(vesvoy != null && vesvoy.trim() != "")
						subject="Barge Newves Discrepancies Report"+" for "+vesvoy;
					else
						subject="Barge Newves Discrepancies Report";				

					outputFileName = subject.replace(' ', '_');
					to = TosRefDataUtil.getValue("MAIL_NLT_BARGE");
				}else if (reportName != null && reportName.equals(CY_DISC_SUM_REPORT)) {
					String vesselName = "";
					VesselVO vvo = CommonBusinessProcessor.getVesselDetails(vesvoy.substring(0, 3));
					if(vvo!=null)
					{
						vesselName = vvo.getVessName() + " - " +  vesvoy.substring(3, 7);
						logger.info("Barge Name: " + vesselName);
					}
					if(vesselName==null||vesselName.equals(""))
					{
						logger.error("Barge - CY Lines report - Error retrieving vessel name");
						return;
					}
					String loadPort = NewReportVesselDao.getLoadPort(vesvoy);
					if(loadPort==null||loadPort.equals(""))
					{
						logger.error("Barge - CY Lines report report - Error retrieving load port");
						return;
					}
					jasperParameter.put("VesselName", vesselName);
					jasperParameter.put("LoadPort", loadPort);
					jasperParameterXL.put("VesselName", vesselName);
					jasperParameterXL.put("LoadPort", loadPort);
					CyDiscSumGenerator cyDiscDs = new CyDiscSumGenerator(vesvoy);
					jasperParameter.put("TotalMoves", ""+cyDiscDs.totalMoves);
					jasperParameterXL.put("TotalMoves", ""+cyDiscDs.totalMoves);
					jasperPrint = JasperFillManager.fillReport(jasperReport,jasperParameter, cyDiscDs);
					jasperPrintXL = JasperFillManager.fillReport(jasperReportXL,jasperParameterXL, new CyDiscSumGenerator(vesvoy));
					if(vesvoy != null && vesvoy.trim() != "")
						subject="CY Lines Report"+" for "+vesvoy;
					else
						subject="CY Lines Report";				

					outputFileName = subject.replace(' ', '_');
					to = TosRefDataUtil.getValue("MAIL_CY_LINES_BARGE");
				} else if (reportName != null && reportName.equals(BARGE_STOWAGE_SUMAMRY_REPORT)) {
					BargeStowageSummaryReportGenerator bStow = new BargeStowageSummaryReportGenerator(vesvoy);
					jasperParameter.put("VesselName", vesvoy);
					jasperParameter.put("DepPort", bStow.depPort);
					jasperParameter.put("DisPort", bStow.disPort);
					jasperParameter.put("totCtrsLocal",""+bStow.totCtrsLocal);
					jasperParameter.put("totCtrs",""+bStow.totCtrs);
					jasperParameter.put("f20Dry",""+bStow.f20Dry);
					jasperParameter.put("f24Dry",""+bStow.f24Dry);
					jasperParameter.put("f24Reefer",""+bStow.f24Reefer);
					jasperParameter.put("f40Dry",""+bStow.f40Dry);
					jasperParameter.put("f40Reefer",""+bStow.f40Reefer);
					jasperParameter.put("f45Van",""+bStow.f45Van);
					jasperParameter.put("aut24Dry",""+bStow.aut24Dry);
					jasperParameter.put("aut24AF",""+bStow.aut24AF);
					jasperParameter.put("aut40Dry",""+bStow.aut40Dry);
					jasperParameter.put("aut40AF",""+bStow.aut40AF);
					jasperParameter.put("aut45Dry",""+bStow.aut45Dry);
					jasperParameter.put("aut40ALS",""+bStow.aut40ALS);
					jasperParameter.put("totFreight",""+bStow.totFreight);
					jasperParameter.put("totMty",""+bStow.totMty);
					jasperParameter.put("totAut",""+bStow.totAut);
					jasperParameter.put("totBare",""+bStow.totBare);
					jasperParameter.put("totBareLocal",""+bStow.totBareLocal);
					jasperPrint = JasperFillManager.fillReport(jasperReport,jasperParameter, new JREmptyDataSource());
					jasperPrintXL = JasperFillManager.fillReport(jasperReportXL,jasperParameter, new JREmptyDataSource());
					if(vesvoy != null && vesvoy.trim() != "")
						subject="NEIGHBOR ISLAND BARGE STOWAGE SUMMARY"+" on "+vesvoy;
					else
						subject="NEIGHBOR ISLAND BARGE STOWAGE SUMMARY";				

					outputFileName = subject.replace(' ', '_');
					String loadPort = NewReportVesselDao.getLoadPort(vesvoy);
					logger.info("Loadport in barge stowage summary report :"+loadPort);
					loadPort = loadPort==null?"":loadPort;
					if ("KAH".equalsIgnoreCase(loadPort))
						to = TosRefDataUtil.getValue("MAIL_NI_BGE_STOW_KAH");
					else if ("NAW".equalsIgnoreCase(loadPort))
						to = TosRefDataUtil.getValue("MAIL_NI_BGE_STOW_NAW");
					else if ("KHI".equalsIgnoreCase(loadPort) || "HIL".equalsIgnoreCase(loadPort))
						to = TosRefDataUtil.getValue("MAIL_NI_BGE_STOW_HIL");
					else 
						to = TosRefDataUtil.getValue("MAIL_NI_BARGE_STOW");	
				}
				else if (reportName != null && reportName.equals(DAMAGE_BARGE_REPORT)) {
					jprintlist = new ArrayList<JasperPrint>();
					jprintlistXL = new ArrayList<JasperPrint>();

					DamageContainerReportGenerator ds26 = new DamageContainerReportGenerator(vesvoy);
					reportName = DAMAGE_CONTAINER_BARGE_REPORT;
					reportFileName = reportName +".jrxml";					
					jasperReport = JasperCompileManager.compileReport(Thread.currentThread().getContextClassLoader().getResourceAsStream(reportFileName));
					jasperPrint = JasperFillManager.fillReport(jasperReport,jasperParameter, ds26);					
					jprintlist.add(jasperPrint);
					reportFileNameXL = reportName +"XLS.jrxml";					
					jasperReportXL = JasperCompileManager.compileReport(Thread.currentThread().getContextClassLoader().getResourceAsStream(reportFileNameXL));
					jasperPrintXL = JasperFillManager.fillReport(jasperReportXL,jasperParameterXL, new DamageContainerReportGenerator(vesvoy));					
					jprintlistXL.add(jasperPrintXL);

					NoDamageContainerReportGenerator ds27 = new NoDamageContainerReportGenerator(vesvoy);
					reportName = NO_DAMAGE_CONTAINER_BARGE_REPORT;
					reportFileName = reportName +".jrxml";					
					jasperReport = JasperCompileManager.compileReport(Thread.currentThread().getContextClassLoader().getResourceAsStream(reportFileName));
					jasperPrint = JasperFillManager.fillReport(jasperReport,jasperParameter, ds27);		
					jprintlist.add(jasperPrint);
					reportFileNameXL = reportName +"XLS.jrxml";					
					jasperReportXL = JasperCompileManager.compileReport(Thread.currentThread().getContextClassLoader().getResourceAsStream(reportFileNameXL));
					jasperPrintXL = JasperFillManager.fillReport(jasperReportXL,jasperParameterXL, new NoDamageContainerReportGenerator(vesvoy));		
					jprintlistXL.add(jasperPrintXL);

					reportName  = DAMAGE_BARGE_REPORT;
					if(vesvoy != null && vesvoy.trim() != "")
						subject="Damage Container Reports"+" for "+vesvoy;
					else
						subject="Damage Container Reports";				

					outputFileName = subject.replace(' ', '_');
					to = TosRefDataUtil.getValue("MAIL_DAMAGE_BARGE");	
				}
				else if(reportName!=null && reportName.equals(INBOUND_TYPECODE_SUMMARY))
				{
					String loadPort = NewReportVesselDao.getLoadPort(vesvoy);
					if(loadPort==null||loadPort.equals(""))
					{
						logger.error("Barge - Inbound Typecode Summary report - Error retrieving load port");
						return;
					}
					ddContent = "";
					List tcList = NewReportVesselDao.getInboundTypeCodeSummary(vesvoy);
					if(tcList==null)
						return;
					Table head = new Table();
					Tr htr = new Tr();
					Td htd = new Td();
					htr.appendChild(htd);
					htd.appendText(date);
					htd = new Td();
					htr.appendChild(htd);
					htd.appendText("Barge Name: "+vesvoy);
					head.appendChild(htr);
					htr = new Tr();
					htd = new Td();
					htr.appendChild(htd);
					htd.appendText(time);
					htd = new Td();
					htr.appendChild(htd);
					htd.appendText("Ex: "+loadPort);
					htd.setAlign("center");
					head.appendChild(htr);
					htr = new Tr();
					htd = new Td();
					htr.appendChild(htd);
					htd.appendText(" ");
					htd = new Td();
					htr.appendChild(htd);
					htd.appendText("Inbound Typecode Summary");
					head.appendChild(htr);
					head.setStyle("font-family:Courier New;font-size:12px;font-weight:bold");
					head.setCellspacing("10");
					ddContent = ddContent + head.write()+ new Br().write();
					Table table = new Table();
					table = writeTable1(tcList, table);
					table.setStyle("font-family:Courier New;font-size:12px;");
					Tr tr = new Tr(); 
					table.appendChild(tr);
					Td td = new Td();
					td.appendChild(new B().appendText(" Total "+loadPort +" : " + tcTotal));
					tr.appendChild(td);
					td.setColspan("2");
					td.setAlign("right");
					td.setStyle("padding-top:5px;border-top:1px dashed #000000;");
					ddContent = ddContent + table.write()+ new Br().write();
					ddContent = ddContent + new Br().write();
					if(vesvoy != null && vesvoy.trim() != "")
						subject="Inbound Typecode Summary"+" for "+vesvoy;
					else
						subject="Inbound Typecode Summary";				

					outputFileName = subject.replace(' ', '_');
					to = TosRefDataUtil.getValue("MAIL_INBTYP_CD_BARGE");	

				}
				ddContent = ddContent.replaceAll("!", "");
				if(exportToPdf) 
				{
					if(!reportName.equals(DOWNLOAD_DISCREPANCIES_REPORTS)&&!reportName.equals(INBOUND_TYPECODE_SUMMARY) && !reportName.equals("VVDDownloadReport")
							&& !reportName.equals(CONSIGNEE_SHIPPER_NOTES)) 
					{
						exportPdf(reportName, jasperPrint, jprintlist, outputFilePath + outputFileName + ".pdf");
					}
				}
				if(exportToXls)
				{
					if(!reportName.equals(DOWNLOAD_DISCREPANCIES_REPORTS)&&!reportName.equals(INBOUND_TYPECODE_SUMMARY)&& !reportName.equals(CONSIGNEE_SHIPPER_NOTES)) 
					{
						exportXls(reportName, jasperPrintXL, jprintlistXL, outputFilePath + outputFileName + ".xls");
					}
				}
				if(!reportName.equals(DOWNLOAD_DISCREPANCIES_REPORTS)&&!reportName.equals(INBOUND_TYPECODE_SUMMARY) && !reportName.equals("VVDDownloadReport")&& !reportName.equals(CONSIGNEE_SHIPPER_NOTES)) 
				{
					EmailSender.mailAttachment(to, from, MAIL_HOST, outputFileName + ".pdf",  outputFilePath+outputFileName + ".pdf", 
							outputFileName + ".xls",  outputFilePath+outputFileName + ".xls",
							"Please find the attached report(s)", subject,1);
					logger.info("Attaching:::"+outputFileName + ".pdf");
					logger.info("Attaching:::"+outputFileName + ".xls");
				}
				else if(reportName.equals(DOWNLOAD_DISCREPANCIES_REPORTS)) 
				{
					EmailSender.sendMail(from, to, subject, ddContent);
					logger.info("Attaching:::"+DOWNLOAD_DISCREPANCIES_REPORTS);
				}
				else if(reportName.equals("VVDDownloadReport")) 
				{
					EmailSender.mailAttachmentForGumVesselData(to, from, MAIL_HOST,  
							outputFileName + ".xls",  outputFilePath+outputFileName + ".xls",
							"Please find the attached report ", subject,1);
					logger.info("Attaching:::"+outputFileName + ".xls");
				}
				
				else if(reportName.equals(INBOUND_TYPECODE_SUMMARY)) 
				{
					EmailSender.sendMail(from, to, subject, ddContent);
					logger.info("Attaching:::"+INBOUND_TYPECODE_SUMMARY);
				}
				else if(reportName.equals(CONSIGNEE_SHIPPER_NOTES))
				{
					EmailSender.mailMultiAttachment(to, from, MAIL_HOST, reportFiles, "Please find the attached reports", subject);
				}
			} else{
				logger.error("Report Name and/or Vesvoy Number is not proper");
			}
		}catch (JRException e) {
			logger.error("Report Create Exception : " +reportName +" For "+vesvoy, e);
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("File Not Found Exception For the Report : "+ reportName + " For "+ vesvoy, e);
			e.printStackTrace();
		}finally {
			if (lookup!=null){
				logger.info("Closing TosLookUp in NewvesReport class");
				lookup.close();
				lookup=null;
			}
		}
	}
	private static void exportPdf(String reportName, JasperPrint jasperPrint, List<JasperPrint> jprintlist, String file) {
		try {
			JRPdfExporter exp = new JRPdfExporter();		           
			if(DAMAGE_BARGE_REPORT.equalsIgnoreCase(reportName)){
				exp.setParameter(JRPdfExporterParameter.JASPER_PRINT_LIST, jprintlist);
			}else{
				exp.setParameter(JRPdfExporterParameter.JASPER_PRINT, jasperPrint);	
			}
			exp.setParameter(JRPdfExporterParameter.OUTPUT_FILE, new File(file));
			exp.exportReport();
		} catch(Exception ex) {
			logger.error("Error while export PDF :- ", ex);
		}
	}
	private static void exportXls(String reportName, JasperPrint jasperPrintXL, List<JasperPrint> jprintlistXL, String file) {
		try {
			JRXlsExporter exporterXLS = new JRXlsExporter();
			if(DAMAGE_BARGE_REPORT.equalsIgnoreCase(reportName)){
				exporterXLS.setParameter(JRXlsExporterParameter.JASPER_PRINT_LIST, jprintlistXL);
			}else{
				exporterXLS.setParameter(JRXlsExporterParameter.JASPER_PRINT, jasperPrintXL);	
			}
			exporterXLS.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.FALSE);
			exporterXLS.setParameter(JRXlsExporterParameter.SHEET_NAMES, new String[]{"Sheet1","Sheet2","Sheet3","Sheet4","Sheet5"});
			exporterXLS.setParameter(JRXlsExporterParameter.IS_DETECT_CELL_TYPE, Boolean.TRUE);
			exporterXLS.setParameter(JRXlsExporterParameter.IGNORE_PAGE_MARGINS, Boolean.TRUE);
			exporterXLS.setParameter(JRXlsExporterParameter.IS_WHITE_PAGE_BACKGROUND, Boolean.FALSE);
			exporterXLS.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, Boolean.TRUE);
			exporterXLS.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_COLUMNS, Boolean.FALSE);
			exporterXLS.setParameter(JRXlsExporterParameter.IS_IGNORE_GRAPHICS, Boolean.TRUE);
			exporterXLS.setParameter(JRXlsExporterParameter.OUTPUT_FILE, new File(file));
			exporterXLS.exportReport();
		} catch(Exception ex) {
			logger.error("Error while export XLS :- ", ex);
		}
	}
	public static String getTotalNumberOfContainers(String vesvoy){

		List<?> totalCtrList = NewReportVesselDao.getTotalNoOfContainersList(vesvoy);

		if ( (totalCtrList == null ) || (totalCtrList.size() <= 0)) { // no data
			return "";
		}else{
			return  (totalCtrList.get(0).toString());
		}
	}

	private static Table writeTable1(List list, Table tbl)
	{
		Tr tr;
		Td td;
		// List
		ArrayList<String> vvList = new ArrayList<String> ();
		for(ListIterator<?> itr = list.listIterator(); itr.hasNext(); ) 
		{ 
			Object[] obj = (Object[]) itr.next(); 
			String vv = obj[1].toString() ;
			/*if ("E".equals(vv)) {
				//OutVV = "MTY";
			} else if ("K".equals(vv)) {
				OutVV = "CHAS";
			}*/
			if(!vvList.contains(vv))
				vvList.add(vv);
		}
		for(int i=0; i<vvList.size(); i++)
		{
			int total = 0;
			String rec = "";
			tr = new Tr();
			td = new Td();
			tr.appendChild(td);
			if ("E".equals(vvList.get(i))) {
				td.appendText("MTY");
			}else if ("K".equalsIgnoreCase(vvList.get(i)))
			{
				td.appendText("CHAS");
			}else {
				td.appendText(vvList.get(i));
			}
			td.setStyle("font-weight:bold");
			tbl.appendChild(tr);
			for(ListIterator<?> itr1 = list.listIterator(); itr1.hasNext(); ) 
			{
				Object[] obj1 = (Object[]) itr1.next(); 
				String tempVv = obj1[1].toString() ;
				if(vvList.get(i).equals(tempVv))
				{
					String tempRec = obj1[0].toString();
					//System.out.println("tempRec :"+tempRec);
					String typecode = obj1[2].toString() ;
					//System.out.println("obj1[3]  :"+obj1[3]);
					BigDecimal tcount = new BigDecimal(obj1[3].toString() )  ;
					if(!rec.equals(tempRec))
					{   String outString = null;
						if ("D".equalsIgnoreCase(tempRec)) { 
							outString = "DAMAGE";
						}
						else if ("M".equalsIgnoreCase(tempRec)) { 
							outString = "MAT";
						}
						else if ("A".equalsIgnoreCase(tempRec)) {
							outString = "APL";
						}
						else if ("X".equalsIgnoreCase(tempRec)) {
							outString = "OUT LOAD";
						}
						else if ("Z".equalsIgnoreCase(tempRec)) {
							outString = "IN LOAD";
						}
						else if ("Y".equalsIgnoreCase(tempRec)) {
							outString = "OUT AUTO";
						}
						else if ("U".equalsIgnoreCase(tempRec)) {
							outString = "IN AUTO";
						}
						else if ("H".equalsIgnoreCase(tempRec)) {
							outString = "Bare CHAS";
						}

//						String temprec1 = populatetemprec(tempRec);
						tr = new Tr();
						td = new Td();
						tr.appendChild(td);
						if (outString!=null)
							td.appendText("&nbsp;"+outString);
						else
							td.appendText("&nbsp;"+tempRec);
						td.setColspan("2");
						td.setStyle("font-weight:bold");
						tbl.appendChild(tr);
					}
					tr = new Tr();
					tbl.appendChild(tr);
					td = new Td();
					tr.appendChild(td);
					td.setColspan("2");
					Table table = new Table();
					td.appendChild(table);
					//table.setCellpadding("10");
					tr = new Tr();
					table.appendChild(tr);
					table.setStyle("font-family:Courier New;font-size:12px;margin-left:50px;margin-top:-5;");
					td = new Td();
					tr.appendChild(td);
					td.appendText(typecode);
					td = new Td();
					td.appendChild(new Text(tcount+""));
					td.setWidth("75");
					td.setAlign("right");
					tr.appendChild(td);
					total = total + tcount.intValue();
					rec = tempRec;
				}
			}
			tcTotal = tcTotal + total;
			//tbl.appendChild(tr);
			tr = new Tr();
			td = new Td();
			tr.appendChild(td);
			td.appendChild(new B().appendText("Total:" + total));
			td.setAlign("right");
			td.setColspan("2");
			tbl.appendChild(tr);
		}
		//
		return tbl;
	}
	
	public static String populatetemprec(String temprec) {
		String outString = "";
		if ("D".equalsIgnoreCase(temprec)) 
			outString = "DAMAGE";
		else if ("M".equalsIgnoreCase(temprec)) 
			outString = "MAT";
		else if ("A".equalsIgnoreCase(temprec))
			outString = "APL";
		else if ("OF".equalsIgnoreCase(temprec))
			outString = "OUT LOAD";
		else if ("IF".equalsIgnoreCase(temprec))
			outString = "IN LOAD";
		else if ("OA".equalsIgnoreCase(temprec))
			outString = "OUT AUTO";
		else if ("IA".equalsIgnoreCase(temprec))
			outString = "IN AUTO";
		else if ("CH".equalsIgnoreCase(temprec))
			outString = "Bare Chassis";
		else if ("E".equalsIgnoreCase(temprec))
			outString = "MTY";
		else 
			outString=temprec; 
		
		return outString;
	}
}
