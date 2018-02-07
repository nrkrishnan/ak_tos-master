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
import com.matson.tos.reports.gum.GumRiderReportGenerator;
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
 * 	A1		10/7/2014		Raghu Iyer			Initial creation
  */

public class NoFaxConsigneeReport {
	private static Logger logger = Logger.getLogger(NoFaxConsigneeReport.class);
	private static String MAIL_HOST = TosRefDataUtil.getValue("MAIL_HOST");

	final static boolean exportToPdf = true;
	final static boolean exportToXls = true;
	
	static int tcTotal = 0;

	public static void createReport(String reportName, String consignee, String phone){

		JasperReport jasperReport = null;
		JasperReport jasperReportXL = null;

		JasperPrint jasperPrint = null;
		JasperPrint jasperPrintXL = null;

		List<JasperPrint> jprintlist = new ArrayList<JasperPrint>();
		List<JasperPrint> jprintlistXL = new ArrayList<JasperPrint>();

		HashMap<String, Object> jasperParameter = new HashMap<String, Object>();
		HashMap<String, Object> jasperParameterXL = new HashMap<String, Object>();
		TosLookup lookup = null;
		//Properties properties = new Properties();
		List<String> reportFiles = new ArrayList<String>();
		try {
			String outputFilePath = System.getProperty("java.io.tmpdir");
			if (outputFilePath!=null && !outputFilePath.endsWith("/")) {
					outputFilePath = outputFilePath+"/";
			}
			String subject = "";
			String outputFileName = "";				
			String from = TosRefDataUtil.getValue("EMAIL_NEWVES_FROM");
			String to = "";	
			String reportFileName = reportName +".jrxml";
			String reportFileNameXL = reportName + "XLS.jrxml";
			logger.info("reportFileName **** "+reportFileName);
			logger.info("reportFileNameXL **** "+reportFileNameXL);
			logger.info("CreateReport() " +reportFileName);
			logger.info("outputFilePath " +outputFilePath);
			
			jasperReport = JasperCompileManager.compileReport(Thread.currentThread().getContextClassLoader().getResourceAsStream(reportFileName));
			jasperReportXL = JasperCompileManager.compileReport(Thread.currentThread().getContextClassLoader().getResourceAsStream(reportFileNameXL));

			
			if (reportName != null && reportName.equals("NoFaxConsigneeReport")) {

				jasperPrintXL = JasperFillManager.fillReport(jasperReportXL,jasperParameterXL, new DownloadNoFaxConReportGenerator(consignee,phone));
				jasperPrint = JasperFillManager.fillReport(jasperReport,jasperParameter, new DownloadNoFaxConReportGenerator(consignee,phone));
				
				subject="Phone List";				

					outputFileName = subject.replace(' ', '_');
					to = TosRefDataUtil.getValue("NOFAX_CONSIGNEE_DATA");	
			}	
			
			if(exportToXls)
			{
				logger.info("Calling NoFaxConsigneeReport.exportXls ");
				exportXls(reportName, jasperPrintXL, jprintlistXL, outputFilePath + outputFileName + ".xls");
			}
			
			if(exportToPdf) 
			{
				logger.info("Calling NoFaxConsigneeReport.exportToPdf ");
				exportPdf(reportName, jasperPrint, jprintlist, outputFilePath + outputFileName + ".pdf");
				
			}
			
			if(reportName.equals("NoFaxConsigneeReport")) 
			{
				logger.info("Preparing for Attaching:::"+outputFileName + ".xls");
	
				EmailSender.mailAttachment(to, from, MAIL_HOST, outputFileName + ".pdf",  outputFilePath+outputFileName + ".pdf", 
						outputFileName + ".xls",  outputFilePath+outputFileName + ".xls",
						"Please find the attached report(s)", subject,1);
				logger.info("Attaching:::"+outputFileName + ".pdf");
				logger.info("Attaching:::"+outputFileName + ".xls");			
			}				

		}catch (JRException e) {
			logger.error("Report Create Exception : " +reportName, e);
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("File Not Found Exception For the Report : "+ reportName, e);
			e.printStackTrace();
		}finally {
			if (lookup!=null){
				logger.info("Closing TosLookUp in NewvesReport class");
				lookup.close();
				lookup=null;
			}
		}
	}
	
	

	private static void exportXls(String reportName, JasperPrint jasperPrintXL, List<JasperPrint> jprintlistXL, String file) {
		try {
			logger.info("Inside NoFaxConsigneeReport.exportXls ");
			JRXlsExporter exporterXLS = new JRXlsExporter();
			exporterXLS.setParameter(JRXlsExporterParameter.JASPER_PRINT, jasperPrintXL);
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
	
	
	private static void exportPdf(String reportName, JasperPrint jasperPrint, List<JasperPrint> jprintlist, String file) {
		try {
			logger.info("Inside NoFaxConsigneeReport.exportPdf ");
			JRPdfExporter exp = new JRPdfExporter();		           
			exp.setParameter(JRPdfExporterParameter.JASPER_PRINT, jasperPrint);	
			exp.setParameter(JRPdfExporterParameter.OUTPUT_FILE, new File(file));
			exp.exportReport();
		} catch(Exception ex) {
			logger.error("Error while export PDF :- ", ex);
		}
	}
	
}
