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
import com.matson.cas.refdata.mapping.TosConsgineeTrucker;
import com.matson.tos.processor.ConsigneeTruckerProcessor;
import com.matson.tos.processor.CommonBusinessProcessor;
import com.matson.tos.processor.NewMatsonVesselJob;
import com.matson.tos.util.TosRefDataUtil;
import com.matson.gems.api.carrier.GEMSCarrier;

public class ConsigneeMaster extends HttpServlet 
{
	/**
	 * This is ths Servlet class for the Consignee Trucker Screen which will be used to retrieve and update
	 * trucker information for the Consignee
	 */
	private static final long serialVersionUID = 1991245004798075935L;
	private static Logger logger = Logger.getLogger(ConsigneeMaster.class);
	
	/**
	 * This method is used to retrieve the Consignee Information
	 * @param request
	 * @param response
	 * @throws IOException
	 * @throws ServletException
	 */
	public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException 
	{
		String page="consigneeMaster.jsp";
		try
		{
			String consigneeName = null;
			response.setContentType("text/html");
			request.getSession().removeAttribute("consigneeName");
			request.getSession().removeAttribute("isUpdated");
			request.getSession().removeAttribute("truckerCodeErrorMsg");
			consigneeName = (String)request.getParameter("consigneeName");
			if(consigneeName!=null && !consigneeName.equals(""))
			{
				logger.info("consigneeName in Servlet:::"+consigneeName);
				request.getSession().setAttribute("consigneeName", consigneeName);
			}
			
			if(request.getParameter("Search") != null && consigneeName!=null && !consigneeName.equals(""))
			{
				//logger.info("action name in Search:"+request.getParameter("Search"));
				ConsigneeTruckerProcessor consigneeTruckerProcessor = new ConsigneeTruckerProcessor();
				TosConsgineeTrucker x = new TosConsgineeTrucker();
				ArrayList<TosConsgineeTrucker> consigneeInfoList = consigneeTruckerProcessor.getConsigneeInfo(consigneeName);
				
				if (consigneeInfoList != null) {
					request.setAttribute("resultList",consigneeInfoList);
					request.getSession().setAttribute("resultList",consigneeInfoList);
				}
			}
			else if(request.getParameter("Save") != null)
			{
				//logger.info("action name in Save:"+request.getParameter("Save"));
				updateConsigneeInformation(request, response);
				request.getSession().removeAttribute("consigneeName");
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
	
	/**
	 * This method is used to update the Trucker Code Information into the database
	 * @param request
	 * @param response
	 * @throws IOException
	 * @throws ServletException
	 */
	private  void updateConsigneeInformation(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException 
	{
		try
		{
			String isUpdated = "";
			String truckerCodeErrorMsg = "";
			boolean isInValidTrucker = false;
			logger.info("into updateConsigneeInformation:::");
			ArrayList<TosConsgineeTrucker> resultList = (ArrayList) request.getSession().getAttribute("resultList");
			TosConsgineeTrucker tosConsgineeTrucker = null;
			ArrayList<TosConsgineeTrucker> updatedList = new ArrayList<TosConsgineeTrucker>();
			String[] truckerCode = request.getParameterValues("truckerCode");
			int i = 0;
			Iterator it  = resultList.iterator();
			while(it.hasNext())
			{
				tosConsgineeTrucker = new TosConsgineeTrucker();
				tosConsgineeTrucker = (TosConsgineeTrucker)it.next();
				//logger.info("truckerCode:inside while loop::"+truckerCode[i]);
				tosConsgineeTrucker.setTruckerCode(truckerCode[i]);
				i++;
				updatedList.add(tosConsgineeTrucker);
			}
			// Validate Trucker Code with GEMS - Start
			for(int x=0;x<truckerCode.length;x++)
			{
				if(truckerCode[x]!=null && !truckerCode[x].equals(""))
				{
					ConsigneeTruckerProcessor consigneeTruckerProcessor = new ConsigneeTruckerProcessor();
					GEMSCarrier gemsCarrier = new GEMSCarrier();
					gemsCarrier = consigneeTruckerProcessor.validateCarrierCodeWithGems(truckerCode[x]);
					if(gemsCarrier==null)
					{
						truckerCodeErrorMsg = "Trucker Code "+truckerCode[x]+ " is not valid";
						//isUpdated = "InvalidTrucker";
						isInValidTrucker = true;
						//request.getSession().setAttribute("isUpdated", isUpdated);
						request.getSession().setAttribute("truckerCodeErrorMsg", truckerCodeErrorMsg);
						break;
					}
				}
			}
			// Validate Trucker Code with GEMS - End
			
			if(updatedList!=null && !updatedList.isEmpty() && isInValidTrucker==false)
			{
				ConsigneeTruckerProcessor consigneeTruckerProcessor = new ConsigneeTruckerProcessor();
				isUpdated =consigneeTruckerProcessor.updateTruckerInfo(updatedList);
				logger.info("isUpdated:in updateConsigneeInformation:::" +isUpdated);
				request.setAttribute("recordsUpdated",resultList);
				request.getSession().setAttribute("isUpdated", isUpdated);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
}
