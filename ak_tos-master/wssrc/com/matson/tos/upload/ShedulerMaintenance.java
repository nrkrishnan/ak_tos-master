package com.matson.tos.upload;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import javax.servlet.http.HttpServletResponse;

import com.matson.tos.processor.CommonBusinessProcessor;

public class ShedulerMaintenance  extends HttpServlet {
	public void service(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		
		HashMap shedulerMap = new HashMap();
		String stowPlan = null;
		String dcm = null;
		String rds = null;
		
		try {
			CommonBusinessProcessor commonProcessor = new CommonBusinessProcessor();
			stowPlan = request.getParameter("stowplanRadio");
			dcm = request.getParameter("dcmRadio");
			rds = request.getParameter("rdsRadio");
			
			System.out.println("StowPlan value "+stowPlan);
			System.out.println("dcm value "+dcm);
			System.out.println("rds value "+rds);
			
			if ("stowplanOn".equalsIgnoreCase(stowPlan)) {
				shedulerMap.put("IS_STOWPLAN_JOB_ON","Y");
			}else if ("stowplanOff".equalsIgnoreCase(stowPlan)){
				shedulerMap.put("IS_STOWPLAN_JOB_ON","N");
			}
			
			if ("dcmOn".equalsIgnoreCase(dcm)) {
				shedulerMap.put("IS_DCM_JOB_ON","Y");
			}else  if ("dcmOff".equalsIgnoreCase(dcm)) {
				shedulerMap.put("IS_DCM_JOB_ON","N");
			}
			
			if ("rdsOn".equalsIgnoreCase(rds)) {
				shedulerMap.put("IS_RDS_JOB_ON","Y");
			}else if ("rdsOff".equalsIgnoreCase(rds)){
				shedulerMap.put("IS_RDS_JOB_ON","N");
			}
			
			commonProcessor.updateShedulerProp(shedulerMap);
			
			System.out.println("shedulerMap :"+shedulerMap.toString());
			
			PrintWriter out = response.getWriter();
			out.println("<html> \n" 
					+ "<head> \n" 
					+ "</head> \n" 
					+ "<body> \n"
					+ "Scheduler parameters are updated successfully!"
					+ " \n" + "</body> \n" 
					+ "</html>");
		}catch (Exception e){
			System.out.println(e);			
			PrintWriter out = response.getWriter();
			out.println("<html> \n"
					+ "<head> \n"
					+ "</head> \n"
					+ "<body> \n"
					+ "Problem updating scheduler parameters, please check application logs"
					+ "\n" + "</body> \n" + "</html>");
		}
	}
}
