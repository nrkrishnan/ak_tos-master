package com.matson.tos.upload;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.matson.cas.refdata.mapping.TosProcessLogger;
import com.matson.cas.service.ftp.FtpProxyGetterBiz;
import com.matson.cas.service.ftp.FtpProxyListBiz;
import com.matson.tos.dao.NewVesselDao;
import com.matson.tos.processor.CommonBusinessProcessor;
import com.matson.tos.processor.NewMatsonVesselJob;
import com.matson.tos.util.TosRefDataUtil;

public class HonNewVessel extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1991245004798075935L;

	public void service(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		boolean nvcopyFound = false;
		Enumeration enumeration = request.getParameterNames();
		while (enumeration.hasMoreElements()) {
			String parameterName = (String) enumeration.nextElement();
			System.out.println("Parameter:::"+parameterName);
			if(parameterName.equals("nvcopy"))
			{
				nvcopyFound = true;
				break;
			}
		}
		if(nvcopyFound)
		{
			try {
				String copy = request.getParameter("nvcopy");
				if(copy!=null && copy.length()>0)
				{
					System.out.println("NVCOPY ::: STARTED");
					String vvd = request.getParameter("vvd");
					if(vvd!=null)
						vvd = vvd.toUpperCase();
					String fileToCopy = vvd.substring(0, 6)+"NV.TXT";
					int rdsArchFtpId = Integer.parseInt(TosRefDataUtil.getValue("RDS_ARCH_FTP_ID"));
					int nvFtpId = Integer.parseInt(TosRefDataUtil.getValue("NV_VES_FILES_FTP_ID"));
					int timeout = Integer.parseInt(TosRefDataUtil.getValue("FTP_TIMEOUT"));
					FtpProxyListBiz list = new FtpProxyListBiz();
					list.setTimeout(timeout);
					String[] rdsArchFiles = list.getFileNames(rdsArchFtpId, null, null);
					if(rdsArchFiles!=null)
					{
						boolean isFileFound = false;
						for(int i=0; i<rdsArchFiles.length; i++)
						{
							if(fileToCopy.equalsIgnoreCase(rdsArchFiles[i]))
							{
								isFileFound = true;
								break;
							}
						}
						if(isFileFound)
						{
							String vesvoy = vvd.substring(0, 6);
							TosProcessLogger loggerRecord = NewVesselDao.getLoggerRecordForVesvoy(vesvoy);
							if(loggerRecord!=null && loggerRecord.getStatus()!=null) {
								if("In-Process".equalsIgnoreCase(loggerRecord.getStatus())) {
									PrintWriter out = response.getWriter();
									out.println("<html> \n" 
											+ "<head> \n" 
											+ "<script type=\"text/javascript\">var count = 5;function countdown() {count = count-1;if (count<0) {window.location=\"NVFileUpload.jsp\";}else {document.getElementById(\"countd\").innerHTML=count;window.setTimeout(\"countdown()\", 1000);}}</script> \n"
											+ "</head> \n" 
											+ "<body> \n"
											+ "<b>"+vesvoy+" is currently In-Process.</b> Redirecting in <span id=\"countd\" style=\"color:green;\">5</span>"
											+ " \n" + "<script>window.onload = countdown();</script></body> \n" 
											+ "</html>");
									return;
								}
								if(loggerRecord.getStatus().contains("Processed")) {
									PrintWriter out = response.getWriter();
									out.println("<html> \n" 
											+ "<head> \n"
											+ "<script type=\"text/javascript\">var count = 5;function countdown() {count = count-1;if (count<0) {window.location=\"NVFileUpload.jsp\";}else {document.getElementById(\"countd\").innerHTML=count;window.setTimeout(\"countdown()\", 1000);}}</script> \n"
											+ "</head> \n" 
											+ "<body> \n"
											+ "<b>"+vesvoy+" has been processed already.</b> Redirecting in <span id=\"countd\" style=\"color:green;\">5</span>"
											+ " \n" + "<script>window.onload = countdown();</script></body> \n" 
											+ "</html>");
									return;
								}
							}
							FtpProxyGetterBiz getter = new FtpProxyGetterBiz();
							getter.setTimeout(timeout);
							String contents = getter.getFileText(rdsArchFtpId, fileToCopy);
							String fileName = System.getProperty("java.io.tmpdir")+"/" + fileToCopy;
							FileWriter fileWriter = new FileWriter(new File(fileName));
							BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
							bufferedWriter.write(contents);
							bufferedWriter.close();
							CommonBusinessProcessor.archiveNVtoFTP(new File(fileName),nvFtpId,true);
							CommonBusinessProcessor.updateCopyFlagAndSendSN4Noti(vvd);
							//
							PrintWriter out = response.getWriter();
							out.println("<html> \n" 
									+ "<head> \n" 
									+ "</head> \n" 
									+ "<body> \n"
									+ "File <b>"+fileToCopy+"</b> copied successfully for processing, please visit <b>N4</b>"
									+ " \n" + "</body> \n" 
									+ "</html>");
						}
						else
						{
							PrintWriter out = response.getWriter();
							out.println("<html> \n" 
									+ "<head> \n" 
									+ "</head> \n" 
									+ "<body> \n"
									+ "File not found : "+fileToCopy
									+ " \n" + "</body> \n" 
									+ "</html>");
						}
					}
					else
					{
						PrintWriter out = response.getWriter();
						out.println("<html> \n" 
								+ "<head> \n" 
								+ "</head> \n" 
								+ "<body> \n"
								+ "No NV files found."
								+ " \n" + "</body> \n" 
								+ "</html>");
					}
				}
			} catch (Exception e) {
				System.out.println(e);			
				PrintWriter out = response.getWriter();
				out.println("<html> \n"
						+ "<head> \n"
						+ "</head> \n"
						+ "<body> \n"
						+ "Problem copying NV file for processing. <br />"
						+ e.toString()
						+ "\n" + "</body> \n" + "</html>");
			}
			System.out.println("NVCOPY:::END");
		}
		else
		{
			try {
				String newvesProcess = request.getParameter("newves");
				String bargeProcess = request.getParameter("barge");
				if(newvesProcess!=null && newvesProcess.length()>0)
				{
					NewMatsonVesselJob.setProcessType(NewMatsonVesselJob.NEWVES);
					String vvd = request.getParameter("vvd");
					if(vvd!=null && vvd.length()==7)
						NewMatsonVesselJob.setVvd(vvd.toUpperCase());
					else
						NewMatsonVesselJob.setVvd(null);
					if (request.getParameter("copyPrimary") != null && request.getParameter("copyPrimary").trim().length() > 0) 
						NewMatsonVesselJob.COPY_PRIMARY = "true";
					else
						NewMatsonVesselJob.COPY_PRIMARY = "false";
					if (request.getParameter("copySupplement") != null && request.getParameter("copySupplement").trim().length() > 0) 
						NewMatsonVesselJob.COPY_SUPPLEMENT = "true";
					else
						NewMatsonVesselJob.COPY_SUPPLEMENT = "false";
				}
				else if(bargeProcess!=null && bargeProcess.length()>0)
				{
					NewMatsonVesselJob.setProcessType(NewMatsonVesselJob.BARGE);
					if (request.getParameter("copyBarge") != null && request.getParameter("copyBarge").trim().length() > 0) 
						NewMatsonVesselJob.COPY_BARGE = "true";
					else
						NewMatsonVesselJob.COPY_BARGE = "false";
				}
				//
				NewMatsonVesselJob.executeNewVesProc();
				//
				PrintWriter out = response.getWriter();
				out.println("<html> \n" 
						+ "<head> \n" 
						+ "</head> \n" 
						+ "<body> \n"
						+ "Processing complete, please visit notifications."
						+ " \n" + "</body> \n" 
						+ "</html>");
			} catch (Exception e) {
				System.out.println(e);			
				PrintWriter out = response.getWriter();
				out.println("<html> \n"
						+ "<head> \n"
						+ "</head> \n"
						+ "<body> \n"
						+ "Problem executing the vessel(s), please visit notifications."
						+ "\n" + "</body> \n" + "</html>");
			}
		}
	}
}
