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
import java.util.List;

import com.matson.cas.erd.service.data.VesselVO;
import com.matson.cas.service.ftp.FtpProxyGetterBiz;
import com.matson.cas.service.ftp.FtpProxyListBiz;
import com.matson.tos.processor.CommonBusinessProcessor;
import com.matson.tos.processor.ConsigneeTruckerProcessor;
import com.matson.tos.processor.GumNewvesProcessorHelper;
import com.matson.tos.processor.NewMatsonVesselJob;
import com.matson.tos.reports.NewvesReport;
import com.matson.tos.util.TosRefDataUtil;
import com.matson.cas.refdata.mapping.TosGumRdsDataFinalMt;
import com.matson.gems.api.carrier.GEMSCarrier;


public class AssignCodeForConsignee extends HttpServlet 
{
	/**
	 * This is the Servlet class which will be invoked for the Assigning Codes Screen
	 */
	private static final long serialVersionUID = 1991245004798075935L;
	private static Logger logger = Logger.getLogger(AssignCodeForConsignee.class);
	
	/**
	 * This method is used to retrieve the Consignee Information
	 * @param request
	 * @param response
	 * @throws IOException
	 * @throws ServletException
	 */
	public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException 
	{
		String page="assignCodeForConsignee.jsp";
		try
		{
			String vvd = null;
			String inputBookingNumber = null;
			String inputContainerNumber = null;
			String inputConsigneeName = null;
			response.setContentType("text/html");
			request.getSession().removeAttribute("vvdUpdated");
			request.getSession().removeAttribute("vvd");
			request.getSession().removeAttribute("assignCodeErrorMsg");
			request.getSession().removeAttribute("isValidVVD");
			vvd = (String)request.getParameter("vvd");
			if (vvd!=null && !vvd.equals(""))
				vvd = vvd.toUpperCase();
			inputBookingNumber = (String)request.getParameter("inputBookingNumber");
			inputContainerNumber = (String)request.getParameter("inputContainerNumber");
			inputConsigneeName = (String)request.getParameter("inputConsigneeName");
			String isValidVVD = null; 
			
			if(vvd!=null && !vvd.equals(""))
			{
				request.getSession().setAttribute("vvd", vvd);
				logger.info("vvd in Servlet:::"+vvd);
			}
			
			logger.info("inputBookingNumber in Servlet:::"+inputBookingNumber);
			logger.info("inputContainerNumber in Servlet:::"+inputContainerNumber);
			if(inputBookingNumber!=null && !inputBookingNumber.equals(""))
			{
				request.getSession().setAttribute("inputBookingNumber", inputBookingNumber);
				logger.info("inputBookingNumber in Servlet:::"+inputBookingNumber);
			}
			
			if(inputContainerNumber!=null && !inputContainerNumber.equals(""))
			{
				inputContainerNumber = inputContainerNumber.toUpperCase();
				request.getSession().setAttribute("inputContainerNumber", inputContainerNumber);
				logger.info("inputContainerNumber in Servlet:::"+inputContainerNumber);
			}
			if(inputConsigneeName!=null && !inputConsigneeName.equals(""))
			{
				inputConsigneeName = inputConsigneeName.toUpperCase();
				request.getSession().setAttribute("inputConsigneeName", inputConsigneeName);
				logger.info("inputConsigneeName in Servlet:::"+inputConsigneeName);
			}
			
			// Code Change to validate VVD(Issue No 2) - Start
			if(vvd!=null && !vvd.equals(""))
			{
				VesselVO vesselVO = CommonBusinessProcessor.getVesselDetails(vvd.substring(0, 3));
				if(vesselVO.getVessCode()!=null)
				{
					logger.info("vesselVO is not null:getVessCode():"+vesselVO.getVessCode());
					//logger.info("vesselVO.getVessOpr() ::"+vesselVO.getVessOpr());
					//logger.info("vesselVO.getVessCarrierCode() ::"+vesselVO.getVessCarrierCode());
					isValidVVD = "Valid";
					request.getSession().setAttribute("isValidVVD", isValidVVD);
				}
				else
				{
					isValidVVD = "Invalid";
					request.getSession().setAttribute("isValidVVD", isValidVVD);
					logger.info("vesselVO is null::");
					request.getSession().setAttribute("vvdInfoList",null);
				}
				logger.info("isValidVVD:::"+isValidVVD);
			}
			// Code Change to validate VVD(Issue No 2) - End
			//isValidVVD = "Valid";
			
			if(request.getParameter("Search") != null && vvd!=null && !vvd.equals("") && isValidVVD!=null && isValidVVD.equalsIgnoreCase("Valid"))
			{
				logger.info("action name in Search:"+request.getParameter("Search"));
				String buttonAction = (String)request.getParameter("Search");
				ConsigneeTruckerProcessor consigneeTruckerProcessor = new ConsigneeTruckerProcessor();
				ArrayList<TosGumRdsDataFinalMt> vvdInfoList = consigneeTruckerProcessor.getVVDInformation(vvd,inputContainerNumber,inputBookingNumber,buttonAction,inputConsigneeName);
				logger.info("vvdInfoList Size:"+vvdInfoList.size());
				if (vvdInfoList != null) {
					request.setAttribute("vvdInfoList",vvdInfoList);
					request.getSession().setAttribute("vvdInfoList",vvdInfoList);
				}
			}
			else if(request.getParameter("Save") != null &&  isValidVVD!=null && isValidVVD.equalsIgnoreCase("Valid"))
			{
				logger.info("action name in Save:"+request.getParameter("Save"));
				String buttonAction = (String)request.getParameter("Save");
				updateVVDInformation(request, response,vvd,buttonAction);
				request.getSession().removeAttribute("vvd");
			}
			else if(request.getParameter("SaveAndProcessNewVes") != null && isValidVVD!=null && isValidVVD.equalsIgnoreCase("Valid"))
			{
				logger.info("action name in SaveAndProcess:"+request.getParameter("SaveAndProcessNewVes"));
				String buttonAction = (String)request.getParameter("SaveAndProcessNewVes");
				updateVVDInformation(request, response,vvd,buttonAction);
				request.getSession().removeAttribute("vvd");
			}
			else if(request.getParameter("DownloadVessel") != null && vvd!=null && !vvd.equals("") && isValidVVD!=null && isValidVVD.equalsIgnoreCase("Valid"))
			{
				logger.info("action name in DownloadVessel:"+request.getParameter("DownloadVessel"));
				String vvdUpdated = "";
				ConsigneeTruckerProcessor consigneeTruckerProcessor = new ConsigneeTruckerProcessor();
				String buttonAction = (String)request.getParameter("DownloadVessel");
				ArrayList<TosGumRdsDataFinalMt> vvdInfoList = consigneeTruckerProcessor.getVVDInformation(vvd,inputContainerNumber,inputBookingNumber,buttonAction,inputConsigneeName);
				logger.info("vvdInfoList Size:"+vvdInfoList.size());
				if (vvdInfoList != null) {
					request.setAttribute("vvdInfoList",vvdInfoList);
					request.getSession().setAttribute("vvdInfoList",vvdInfoList);
				}
				try
				{
					if(vvdInfoList != null && vvdInfoList.size()>0)
					{
						NewvesReport.createReport("VVDDownloadReport",vvd, null,null);
						vvdUpdated = "ReportGenerated";
						request.getSession().setAttribute("vvdUpdated", vvdUpdated);
					}
				}
				catch(Exception e)
				{
					logger.error("Error in creating reports for " + vvd);
					vvdUpdated = "ReportNotGenerated";
					request.getSession().setAttribute("vvdUpdated", vvdUpdated);
				}
			}
			else
			{
				logger.info("In Else Block");
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
	
	private  void updateVVDInformation(HttpServletRequest request, HttpServletResponse response,String vesvoy,String buttonAction) throws IOException, ServletException 
	{
		try
		{
			String vvdUpdated = "";
			boolean isInValidTrucker = false;
			logger.info("into updateVVDInformation:::");
			ArrayList<TosGumRdsDataFinalMt> vvdInfoList = (ArrayList) request.getSession().getAttribute("vvdInfoList");
			if(vvdInfoList==null || vvdInfoList.isEmpty())
			{
				vvdInfoList = new ArrayList<TosGumRdsDataFinalMt>();
				request.setAttribute("vvdInfoList",vvdInfoList);
				request.getSession().setAttribute("vvdInfoList",vvdInfoList);
				return;
			}
			ArrayList<TosGumRdsDataFinalMt> updatedList = new ArrayList<TosGumRdsDataFinalMt>();
			String[] truckerCode = request.getParameterValues("trucker");
			String[] bookingNumber = request.getParameterValues("bookingNumber");
			String[] consigneeName = request.getParameterValues("consigneeName");
			String[] shipperName = request.getParameterValues("shipperName");
			String[] remarks = request.getParameterValues("remarks");
			String[] dischargePort = request.getParameterValues("dischargePort");
			String[] dport = request.getParameterValues("dport");
			String[] sealNumber = request.getParameterValues("sealNumber");
			ArrayList<String> oldTruckerList = new ArrayList<String>();
			//logger.info("buttonAction:::"+buttonAction);
			String assignCodeErrorMsg = "";
			int i = 0;
			int j = 0;
			int k = 0;
			int l = 0;
			int m = 0;
			int n = 0;
			int d = 0;
			int p = 0;
			TosGumRdsDataFinalMt tosGumRdsDataFinalMt = null;
			Iterator it  = vvdInfoList.iterator();
			while(it.hasNext())
			{
				tosGumRdsDataFinalMt = new TosGumRdsDataFinalMt();
				tosGumRdsDataFinalMt = (TosGumRdsDataFinalMt)it.next();
				if(!tosGumRdsDataFinalMt.getTruck().equalsIgnoreCase(truckerCode[i]))
				{
					logger.info("Old  Code ::::"+tosGumRdsDataFinalMt.getTruck());
					logger.info("New Code ::::"+truckerCode[i]);
					oldTruckerList.add(truckerCode[i]);
				}
				tosGumRdsDataFinalMt.setTruck(truckerCode[i].toUpperCase());
				tosGumRdsDataFinalMt.setBookingNumber(bookingNumber[j].toUpperCase());
				// Code Fix to Update Consignee only if the user changes its value - Start
				if(!tosGumRdsDataFinalMt.getConsignee().equalsIgnoreCase(consigneeName[k]))
				{
					logger.info("Container ::"+tosGumRdsDataFinalMt.getContainerNumber()+ " Old Consignee Value is " +tosGumRdsDataFinalMt.getConsigneeName()+ "New Consignee Value :"+consigneeName[k]);
					tosGumRdsDataFinalMt.setConsignee(consigneeName[k].toUpperCase());
					tosGumRdsDataFinalMt.setConsigneeName(consigneeName[k].toUpperCase());
				}
				//tosGumRdsDataFinalMt.setConsigneeName(consigneeName[k]);
				// Code Fix to Update Consignee only if the user changes its value - End
				
				// Code Fix to Update Shipper only if the user changes its value - Start
				if(!tosGumRdsDataFinalMt.getShipper().equalsIgnoreCase(shipperName[l]))
				{
					logger.info("Container ::"+tosGumRdsDataFinalMt.getContainerNumber()+ " Old Shipper Value is " +tosGumRdsDataFinalMt.getShipper()+ "New Shipper Value :"+shipperName[l]);
					tosGumRdsDataFinalMt.setShipper(shipperName[l].toUpperCase());
					tosGumRdsDataFinalMt.setShipperName(shipperName[l].toUpperCase());
				}
				//tosGumRdsDataFinalMt.setShipper(shipperName[l]);
				// Code Fix to Update Shipper only if the user changes its value - End
				tosGumRdsDataFinalMt.setCargoNotes(remarks[m].toUpperCase());
				tosGumRdsDataFinalMt.setDischargePort(dischargePort[d].toUpperCase());
				tosGumRdsDataFinalMt.setDport(dport[n].toUpperCase());
				tosGumRdsDataFinalMt.setSealNumber(sealNumber[p].toUpperCase());
				i++;
				j++;
				k++;
				l++;
				m++;
				n++;
				d++;
				p++;
				updatedList.add(tosGumRdsDataFinalMt);
			}
			//logger.info("oldTruckerList Size :::"+oldTruckerList.size());
			//logger.info("truckerCode length :::"+truckerCode.length);
			for(int x=0;x<oldTruckerList.size();x++)
			{
				//logger.info("X Value:::"+x);
				String newTruckerCode = (String) oldTruckerList.get(x);
				logger.info("newTruckerCode:in for loop:::"+newTruckerCode);
				ConsigneeTruckerProcessor consigneeTruckerProcessor = new ConsigneeTruckerProcessor();
				GEMSCarrier gemsCarrier = new GEMSCarrier();
				gemsCarrier = consigneeTruckerProcessor.validateCarrierCodeWithGems(newTruckerCode);
				if(gemsCarrier==null)
				{
					//vvdUpdated = "InvalidTrucker";
					isInValidTrucker = true;
					//request.getSession().setAttribute("vvdUpdated", vvdUpdated);
					//TosGumRdsDataFinalMt tosGumRdsDataFinalMt1 = new TosGumRdsDataFinalMt();
					///tosGumRdsDataFinalMt1 = (TosGumRdsDataFinalMt)updatedList.get(x);
					//assignCodeErrorMsg = "Trucker Code " +truckerCode[x]+ " for the Container " +tosGumRdsDataFinalMt1.getContainerNumber()+ " is not valid";
					assignCodeErrorMsg = "Trucker Code " +newTruckerCode+ " is not valid";
					request.getSession().setAttribute("assignCodeErrorMsg", assignCodeErrorMsg);
					break;
				}
			}
			// Validate Trucker Code with GEMS - Start
			/*for(int x=0;x<truckerCode.length;x++)
			{
				if(truckerCode[x]!=null && !truckerCode[x].equals(""))
				{
					ConsigneeTruckerProcessor consigneeTruckerProcessor = new ConsigneeTruckerProcessor();
					GEMSCarrier gemsCarrier = new GEMSCarrier();
					gemsCarrier = consigneeTruckerProcessor.validateCarrierCodeWithGems(truckerCode[x]);
					if(gemsCarrier==null)
					{
						//vvdUpdated = "InvalidTrucker";
						isInValidTrucker = true;
						//request.getSession().setAttribute("vvdUpdated", vvdUpdated);
						TosGumRdsDataFinalMt tosGumRdsDataFinalMt1 = new TosGumRdsDataFinalMt();
						tosGumRdsDataFinalMt1 = (TosGumRdsDataFinalMt)updatedList.get(x);
						assignCodeErrorMsg = "Trucker Code " +truckerCode[x]+ " for the Container " +tosGumRdsDataFinalMt1.getContainerNumber()+ " is not valid";
						request.getSession().setAttribute("assignCodeErrorMsg", assignCodeErrorMsg);
						break;
					}
				}
			}*/
			// Validate Trucker Code with GEMS - End
			
			if(updatedList!=null && !updatedList.isEmpty() && !isInValidTrucker)
			{
				ConsigneeTruckerProcessor consigneeTruckerProcessor = new ConsigneeTruckerProcessor();
				vvdUpdated = consigneeTruckerProcessor.updateVVDInformation(updatedList);
				logger.info("vvdUpdated in updateVVDInformation:::" +vvdUpdated);
				request.setAttribute("recordsUpdated",updatedList);
				request.getSession().setAttribute("vvdUpdated", vvdUpdated);
			}
			if(vesvoy!=null && buttonAction!=null && buttonAction.equalsIgnoreCase("SaveAndProcessNewVes") && !isInValidTrucker)
			{
				GumNewvesProcessorHelper gumNewvesProcessorHelper = new GumNewvesProcessorHelper();
				logger.info("buttonAction:::" +buttonAction);
				logger.info("vesvoy:::" +vesvoy);
				logger.info("Calling GumNewvesProcessorHelper:::");
				vesvoy = vesvoy.length()==6?(vesvoy+"W"):vesvoy;
				gumNewvesProcessorHelper.startNewVessProc(vesvoy);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
}
