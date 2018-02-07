package com.matson.tos.reports.gum;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRPdfExporterParameter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;

import org.apache.log4j.Logger;

import com.matson.tos.util.CalendarUtil;
import com.matson.tos.util.EmailSender;
import com.matson.tos.util.TosRefDataUtil;


/* This class generates the needed report and sent it through email.
 *
 * 	Srno	Date			AuthorName				Change Description
 * 	1		04/01/2013		Karthik Rajendran		Initial creation
 * 	2		04/02/2013		Karthik Rajendran		Added: Sending TAG a manifest file
 * 													Changed: Email from/to keys
 * 	3		04/03/2013		Karthik Rajendran		Reefer report integrated.
 * 	4		04/04/2013		Karthik Rajendran		Reefer report name changed.
 * 	5		04/08/2013		Karthik Rajendran		Added: Manifest file check before sending email.
 * 	6		04/12/2013		Karthik Rajendran		Added: Download discrepancies report
 *  7		05/01/2013		Raghu Iyer				Added: Rider report
 *  8		05/08/2013		Karthik Rajendran		Added: Prefix to Download discrepancies report
 * 
 */
public class GumNewvesReport {
	private static Logger logger = Logger.getLogger(GumNewvesReport.class);
	private static String MAIL_HOST = TosRefDataUtil.getValue("MAIL_HOST");
	final static boolean exportToPdf = true;
	final static boolean exportToXls = true;
	//
	public final static String INBOUND_BY_DESTINATION_DISCHARGE_SERVICE = "InboundByDestinationAndDs";
	public final static String RIDER_REPORT = "RiderRpt";
	public final static String SEND_TAG_MANIFEST = "SendTagManifest";
	public final static String REEFER = "GuamReeferReport";
	public final static String DOWNLOAD_DISCREPANCIES = "DD";
	//
	public static void createReport(String reportName, String vvd, String prefix)
	{
		JasperReport jasperReport = null;
		JasperReport jasperReportXL = null;
		JasperPrint jasperPrint = null;
		JasperPrint jasperPrintXL = null;
		List<JasperPrint> jprintlist = new ArrayList<JasperPrint>();
		List<JasperPrint> jprintlistXL = new ArrayList<JasperPrint>();
		HashMap<String, Object> jasperParameter = new HashMap<String, Object>();
		HashMap<String, Object> jasperParameterXL = new HashMap<String, Object>();
		GumTagManifestGenerator mft = null;
		try{
			if(reportName != null && reportName.trim() !="") 
			{
				String outputFilePath = System.getProperty("java.io.tmpdir");
				if (outputFilePath !=null && !outputFilePath.endsWith("/")) {
					outputFilePath = outputFilePath + "/";
				}
				String vesvoy = vvd.substring(0, 6);
				String subject = "";
				String message = "";
				String outputFileName = "";		
				String outputZipFileName = "";
				String from = TosRefDataUtil.getValue("GUAM_MAIL_NV_FROM");
				String to = "";	
				String reportFileName = reportName +".jrxml";
				String reportFileNameXL = reportName + "XLS.jrxml";
				System.out.println("reportFileName **** "+reportFileName);
				System.out.println("reportFileNameXL **** "+reportFileNameXL);
				System.out.println("CreateReport() " +reportFileName);
				Calendar currCal = Calendar.getInstance();
				Date currDate = (Date) currCal.getTime();   
				Boolean isDayLightSaving = currCal.getTimeZone().inDaylightTime(currDate);
				int offset = isDayLightSaving?-3:-2;
				currCal.add(Calendar.HOUR, offset);
				String date = CalendarUtil.convertDateToString(currCal.getTime());
				DateFormat df = new SimpleDateFormat("hh:mm");
				String time = df.format(currCal.getTime());
				jasperParameter.put("Vesvoy", vvd);
				jasperParameter.put("FormattedDate", date);
				jasperParameter.put("FormattedTime", time);
				jasperParameterXL.put("Vesvoy", vvd);
				jasperParameterXL.put("FormattedDate", date);
				jasperParameterXL.put("FormattedTime", time);
				jasperParameterXL.put(JRParameter.IS_IGNORE_PAGINATION, Boolean.TRUE);
				//
				if(reportName.equals(INBOUND_BY_DESTINATION_DISCHARGE_SERVICE))
				{
					jasperReport = JasperCompileManager.compileReport(Thread.currentThread().getContextClassLoader().getResourceAsStream(reportFileName));
					jasperReportXL = JasperCompileManager.compileReport(Thread.currentThread().getContextClassLoader().getResourceAsStream(reportFileNameXL));
					jasperPrint = JasperFillManager.fillReport(jasperReport,jasperParameter, new GumInboundDportDsGenerator(vesvoy));
					jasperPrintXL = JasperFillManager.fillReport(jasperReportXL,jasperParameterXL, new GumInboundDportDsGenerator(vesvoy));
					subject="Inbound By Destination And Discharge Service Report for "+vesvoy;
					outputFileName = subject.replace(' ', '_');
					to = TosRefDataUtil.getValue("GUAM_MAIL_INBOUND");
				}
				else if(reportName.equals(SEND_TAG_MANIFEST))
				{
					mft = new GumTagManifestGenerator();
					mft.generateManifestFile(vesvoy);
					subject = vesvoy + " Manifest File";
					//outputFileName = vesvoy + "_G.TXT";
					outputZipFileName = outputFilePath + vesvoy + "_G.ZIP";
					to = TosRefDataUtil.getValue("GUAM_MAIL_MANIFEST");
					message = "Please find the attached manifest file.";
				}
				else if(reportName.equals(REEFER))
				{
					jasperReport = JasperCompileManager.compileReport(Thread.currentThread().getContextClassLoader().getResourceAsStream(reportFileName));
					jasperReportXL = JasperCompileManager.compileReport(Thread.currentThread().getContextClassLoader().getResourceAsStream(reportFileNameXL));
					jasperPrint = JasperFillManager.fillReport(jasperReport,jasperParameter, new GumReeferReportGenerator(vesvoy));
					jasperPrintXL = JasperFillManager.fillReport(jasperReportXL,jasperParameterXL, new GumReeferReportGenerator(vesvoy));
					subject = "Reefer report for " + vesvoy;
					outputFileName = subject.replace(' ', '_');
					to = TosRefDataUtil.getValue("GUAM_MAIL_REEFER");
				}
				else if(reportName.equals(DOWNLOAD_DISCREPANCIES))
				{
					subject = vesvoy + " Download Discrepancies";
					message = message + "<pre>";
					message = message + (vesvoy + " Download Discrepancies").toUpperCase();
					message = message + "<br /><br />";
					GumDownloadDiscrepanciesGenerator gen = new GumDownloadDiscrepanciesGenerator(vesvoy);
					message = message + gen.getDiscrepancies();
					message = message + "</pre>";
					to = TosRefDataUtil.getValue("GUAM_MAIL_DISCREP");
					if(prefix!=null && prefix.length()>0)
					{
						subject = prefix + subject;
						to = TosRefDataUtil.getValue("GUAM_MAIL_DSCRE_PRE");
					}
				}
				else if(reportName.equals(RIDER_REPORT))
				{
					jasperReport = JasperCompileManager.compileReport(Thread.currentThread().getContextClassLoader().getResourceAsStream(reportFileName));
					jasperReportXL = JasperCompileManager.compileReport(Thread.currentThread().getContextClassLoader().getResourceAsStream(reportFileNameXL));
					jasperPrint = JasperFillManager.fillReport(jasperReport,jasperParameter, new GumRiderReportGenerator(vesvoy));
					jasperPrintXL = JasperFillManager.fillReport(jasperReportXL,jasperParameterXL, new GumRiderReportGenerator(vesvoy));
					subject = "Rider report for " + vesvoy;
					outputFileName = subject.replace(' ', '_');
					to = TosRefDataUtil.getValue("GUAM_MAIL_RIDER");
				}
				//
				// Generate pdf report
				if(exportToPdf) 
				{
					if(!reportName.equals(SEND_TAG_MANIFEST)&& !reportName.equals(DOWNLOAD_DISCREPANCIES))
					{
						JRPdfExporter exp = new JRPdfExporter();		           
						//if(reportName.equals(SUPPLEMENAL_REPORT) || (DAMAGE_BARGE_REPORT.equalsIgnoreCase(reportName))){
						//	exp.setParameter(JRPdfExporterParameter.JASPER_PRINT_LIST, jprintlist);
						//}else{
						exp.setParameter(JRPdfExporterParameter.JASPER_PRINT, jasperPrint);	
						//}
						exp.setParameter(JRPdfExporterParameter.OUTPUT_FILE, new File(outputFilePath + outputFileName + ".pdf"));
						exp.exportReport();
					}
				}
				// Generate xls report
				if(exportToXls)
				{
					if(!reportName.equals(SEND_TAG_MANIFEST)&& !reportName.equals(DOWNLOAD_DISCREPANCIES))
					{
						JRXlsExporter exporterXLS = new JRXlsExporter();
						//if(reportName.equals(SUPPLEMENAL_REPORT) || DAMAGE_BARGE_REPORT.equalsIgnoreCase(reportName)){
						//	exporterXLS.setParameter(JRXlsExporterParameter.JASPER_PRINT_LIST, jprintlistXL);
						//}else{
						exporterXLS.setParameter(JRXlsExporterParameter.JASPER_PRINT, jasperPrintXL);	
						//}
						exporterXLS.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.FALSE);
						exporterXLS.setParameter(JRXlsExporterParameter.SHEET_NAMES, new String[]{"Sheet1","Sheet2","Sheet3","Sheet4","Sheet5"});
						exporterXLS.setParameter(JRXlsExporterParameter.IS_DETECT_CELL_TYPE, Boolean.TRUE);
						exporterXLS.setParameter(JRXlsExporterParameter.IGNORE_PAGE_MARGINS, Boolean.TRUE);
						exporterXLS.setParameter(JRXlsExporterParameter.IS_WHITE_PAGE_BACKGROUND, Boolean.FALSE);
						exporterXLS.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, Boolean.TRUE);
						exporterXLS.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_COLUMNS, Boolean.FALSE);
						exporterXLS.setParameter(JRXlsExporterParameter.IS_IGNORE_GRAPHICS, Boolean.TRUE);
						exporterXLS.setParameter(JRXlsExporterParameter.OUTPUT_FILE, new File(outputFilePath + outputFileName + ".xls"));
						exporterXLS.exportReport();
					}
				}
				// Sending email.
				if(reportName.equals(SEND_TAG_MANIFEST))
				{
					if(mft!=null && mft.fileCreated) {
						System.out.println("Sending manifest file...");
						EmailSender.mailAttachment(to, from, MAIL_HOST, outputZipFileName, message, subject);
					}
				}
				else if (reportName.equals(DOWNLOAD_DISCREPANCIES))
				{
					System.out.println("Sending Download Discrepancies...");
					EmailSender.sendMail(from, to, subject, message);					
				}
				else
				{
					EmailSender.mailAttachment(to, from, MAIL_HOST, outputFileName + ".pdf",  outputFilePath+outputFileName + ".pdf", 
							outputFileName + ".xls",  outputFilePath+outputFileName + ".xls",
							"Please find the attached report(s)", subject,1);
					System.out.println("Attaching:::"+outputFileName + ".pdf");
					System.out.println("Attaching:::"+outputFileName + ".xls");
				}
			}
			else
			{
				logger.error("Report name error - "+reportName);
			}
		}
		catch (JRException e) {
			logger.error("Report Create Exception : " +reportName +" For "+vvd);
			e.printStackTrace();
		}
		catch(Exception e){
			logger.error(e);
			e.printStackTrace();
		}
	}

}
