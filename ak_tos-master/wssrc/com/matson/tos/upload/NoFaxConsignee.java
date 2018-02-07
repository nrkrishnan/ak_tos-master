package com.matson.tos.upload;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.List;


import com.matson.cas.service.ftp.FtpProxyGetterBiz;
import com.matson.cas.service.ftp.FtpProxyListBiz;
import com.matson.tos.processor.CommonBusinessProcessor;
import com.matson.tos.processor.NoFaxConsigneeProcessor;
import com.matson.tos.processor.NewMatsonVesselJob;
import com.matson.tos.reports.NoFaxConsigneeReport;
import com.matson.tos.util.TosRefDataUtil;
import com.matson.cas.refdata.mapping.TosNoFaxConsigneeMt;


public class NoFaxConsignee extends HttpServlet 
{
	/**
	 * This is the Servlet class which will be invoked for the No Fax Consignee Screen
	 */
	private static final long serialVersionUID = 1991245004798075931L;
	private static Logger logger = Logger.getLogger(NoFaxConsignee.class);
	
	public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException 
	{
		String page="noFaxConsignee.jsp";
		try
		{
			String inputPhoneNumber = null;
			String inputConsigneeName = null;
			response.setContentType("text/html");
			inputPhoneNumber = (String)request.getParameter("inputPhoneNumber");
			inputConsigneeName = (String)request.getParameter("inputConsigneeName");

			logger.info("inputPhoneNumber in Servlet:::"+inputPhoneNumber);
			logger.info("inputConsigneeName in Servlet:::"+inputConsigneeName);
			if(inputPhoneNumber!=null && !inputPhoneNumber.equals(""))
			{
				request.getSession().setAttribute("inputPhoneNumber", inputPhoneNumber);
				logger.info("inputPhoneNumber in Servlet:::"+inputPhoneNumber);
			}
			
			if(inputConsigneeName!=null && !inputConsigneeName.equals(""))
			{
				inputConsigneeName = inputConsigneeName.toUpperCase();
				request.getSession().setAttribute("inputConsigneeName", inputConsigneeName);
				logger.info("inputConsigneeName in Servlet:::"+inputConsigneeName);
			}
			
			if(request.getParameter("Search") != null)
			{
				logger.info("action name in Search:"+request.getParameter("Search"));
				String buttonAction = (String)request.getParameter("Search");
				NoFaxConsigneeProcessor noFaxConsigneeProcessor = new NoFaxConsigneeProcessor();
				ArrayList<TosNoFaxConsigneeMt> consigneeList = noFaxConsigneeProcessor.getConsigneeInformation(inputPhoneNumber,buttonAction,inputConsigneeName);
				logger.info("consigneeList Size:"+consigneeList.size());
				if (consigneeList != null) {
					request.setAttribute("consigneeList",consigneeList);
					request.getSession().setAttribute("consigneeList",consigneeList);
					request.getSession().setAttribute("consigneeUpdated", "");
				}
			}
			else if(request.getParameter("Save") != null)
			{
				logger.info("action name in Save:"+request.getParameter("Save"));
				String buttonAction = (String)request.getParameter("Save");
				updateConsigneeInfo(request, response,buttonAction);
				request.getSession().removeAttribute("inputConsigneeName");
			}

			else if(request.getParameter("Save & Email Report") != null)
			{
				logger.info("action name in Save & Email Report:"+request.getParameter("Save & Email Report") + ":"+inputPhoneNumber +":"+ inputConsigneeName);
				String phone = inputPhoneNumber;
				String consignee = inputConsigneeName;
				String consigneeUpdated = "";
				NoFaxConsigneeProcessor noFaxConsigneeProcessor = new NoFaxConsigneeProcessor();
				String buttonAction = (String)request.getParameter("Save & Email Report");
				updateConsigneeInfo(request, response,buttonAction);
				ArrayList<TosNoFaxConsigneeMt> consigneeList = noFaxConsigneeProcessor.getConsigneeInformation(phone,buttonAction,consignee);
				logger.info("consigneeList Size:"+consigneeList.size());
				if (consigneeList != null) {
					request.setAttribute("consigneeList",consigneeList);
					request.getSession().setAttribute("consigneeList",consigneeList);
				}
				try
				{
					if(consigneeList != null && consigneeList.size()>0)
					{
						
						NoFaxConsigneeReport.createReport("NoFaxConsigneeReport",consignee,phone);
						consigneeUpdated = "ReportGenerated";
						request.getSession().setAttribute("consigneeUpdated", consigneeUpdated);
					}else{
						consigneeUpdated = "ReportNotGenerated";
						request.getSession().setAttribute("consigneeUpdated", consigneeUpdated);
					}
				}
				catch(Exception e)
				{
					logger.error("Error in creating reports for " + consignee);
				}
			}
			else if(request.getParameter("Delete") != null)
			{
				logger.info("action name in Delete:"+request.getParameter("Delete") + "::" + inputConsigneeName + "::" + inputConsigneeName);
				String buttonAction = (String)request.getParameter("Delete");
				deleteConsigneeInfo(request, response,buttonAction);
				request.getSession().removeAttribute("inputConsigneeName");
				
				logger.info("action name in Search after Delete for ::: " + inputConsigneeName + "::" + inputConsigneeName);
				
				buttonAction = "Search";
				NoFaxConsigneeProcessor noFaxConsigneeProcessor = new NoFaxConsigneeProcessor();
				ArrayList<TosNoFaxConsigneeMt> consigneeList = noFaxConsigneeProcessor.getConsigneeInformation(inputPhoneNumber,buttonAction,inputConsigneeName);
				logger.info("consigneeList Size:"+consigneeList.size());
				if (consigneeList != null) {
					request.setAttribute("consigneeList",consigneeList);
					request.getSession().setAttribute("consigneeList",consigneeList);
				}
			}			
			else
			{
				logger.info("In Else Block");
				String buttonAction = "Search";
				NoFaxConsigneeProcessor noFaxConsigneeProcessor = new NoFaxConsigneeProcessor();
				ArrayList<TosNoFaxConsigneeMt> consigneeList = noFaxConsigneeProcessor.getConsigneeInformation(inputPhoneNumber,buttonAction,inputConsigneeName);
				logger.info("consigneeList Size:"+consigneeList.size());
				if (consigneeList != null) {
					request.setAttribute("consigneeList",consigneeList);
					request.getSession().setAttribute("consigneeList",consigneeList);
					request.getSession().setAttribute("consigneeUpdated", "");
				}				
			}
			RequestDispatcher dispatcher = request.getRequestDispatcher(page);
			if(dispatcher!=null)
			{
				dispatcher.forward(request, response);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	private  void updateConsigneeInfo(HttpServletRequest request, HttpServletResponse response,String buttonAction) throws IOException, ServletException 
	{
		try
		{
			String consigneeUpdated = "";
			logger.info("into updateConsigneeInfo:::");
			ArrayList<TosNoFaxConsigneeMt> consigneeList = (ArrayList) request.getSession().getAttribute("consigneeList");
			logger.info(" consigneeList size " +consigneeList.size());
			if(consigneeList==null || consigneeList.isEmpty())
			{
				consigneeList = new ArrayList<TosNoFaxConsigneeMt>();
				request.setAttribute("consigneeList",consigneeList);
				request.getSession().setAttribute("consigneeList",consigneeList);
				return;
			}
			ArrayList<TosNoFaxConsigneeMt> updatedList = new ArrayList<TosNoFaxConsigneeMt>();
			String[] consigneeName = request.getParameterValues("consigneeName");
			String[] phoneNumber = request.getParameterValues("phoneNumber");
			String[] type = request.getParameterValues("type");
			String[] speed = request.getParameterValues("speed");
			ArrayList<String> oldconsuigneeList = new ArrayList<String>();
			//logger.info("buttonAction:::"+buttonAction);
			String updateCodeErrorMsg = "";
			int i = 0;
			int j = 0;
			int k = 0;
			int l = 0;
			TosNoFaxConsigneeMt tosNoFaxConsignee = null;
			Iterator it  = consigneeList.iterator();
			while(it.hasNext())
			{
				tosNoFaxConsignee = new TosNoFaxConsigneeMt();
				tosNoFaxConsignee = (TosNoFaxConsigneeMt)it.next();
				if(!tosNoFaxConsignee.getConsigneeName().equalsIgnoreCase(consigneeName[i]))
				{
					logger.info(" Old Consignee Value is " +tosNoFaxConsignee.getConsigneeName()+ "New Consignee Value :"+consigneeName[i]);
					tosNoFaxConsignee.setConsigneeName(consigneeName[i].toUpperCase());
				}				
				
				tosNoFaxConsignee.setPhone(phoneNumber[j]);
				tosNoFaxConsignee.setType(type[k].toUpperCase());
				tosNoFaxConsignee.setSpeed(speed[l]);
	
				i++;
				j++;
				k++;
				l++;
				updatedList.add(tosNoFaxConsignee);
			}
			logger.info(" updatedList : "+updatedList.size());			
			if(updatedList!=null && !updatedList.isEmpty())
			{
				NoFaxConsigneeProcessor noFaxConsigneeProcessor = new NoFaxConsigneeProcessor();
				consigneeUpdated = noFaxConsigneeProcessor.updateConsigneeInformation(updatedList);
				logger.info("consigneeUpdated in updateConsigneeInformation:::" +consigneeUpdated);
				request.setAttribute("recordsUpdated",updatedList);
				request.getSession().setAttribute("consigneeUpdated", consigneeUpdated);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	private  void deleteConsigneeInfo(HttpServletRequest request, HttpServletResponse response,String buttonAction) throws IOException, ServletException 
	{
		try
		{
			String consigneeUpdated = "";
			logger.info("into deleteConsigneeInfo:::");
			
			String[] delete = request.getParameterValues("Delete");
			List list =  Arrays.asList(delete); 
			logger.info("into deleteConsigneeInfo:::" + list.size());

			String select[] = request.getParameterValues("Delete"); 
			if (select != null && select.length != 0) {
				
				logger.info("You have selected: " + select.length);
				for (int i = 0; i < select.length; i++) {
					logger.info(" delete ID " +select[i]);	
					if (!select[i].equalsIgnoreCase("Delete")){
							NoFaxConsigneeProcessor noFaxConsigneeProcessor = new NoFaxConsigneeProcessor();
							consigneeUpdated = noFaxConsigneeProcessor.deleteConsigneeInformation(Integer.parseInt(select[i]));
							logger.info("consigneeUpdated in deleteConsigneeInformation:::" +consigneeUpdated);
							request.getSession().setAttribute("consigneeUpdated", consigneeUpdated);
						}
							
					}  				
				
				}
			
			request.setAttribute("Delete", list); 
	
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}	
		
}
