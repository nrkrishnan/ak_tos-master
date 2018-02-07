package com.matson.tos.upload;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.matson.cas.refdata.mapping.TosNoFaxConsigneeMt;
import com.matson.tos.dao.NoFaxConsigneeDao;
import com.matson.tos.exception.NewVesselLogger;
import com.matson.tos.processor.CommonBusinessProcessor;

public class NoFaxAddConsignee extends HttpServlet{

	/*
	 * 	Srno   	Date			AuthorName			Change Description
	 * 	A1     	10/14/2014		Raghu Iyer			Initial creation
	 */
	private static final long serialVersionUID = 7989305258921981144L;
	private static Logger logger = Logger.getLogger(NoFaxAddConsignee.class);
	//
	protected void service(HttpServletRequest request, HttpServletResponse response)
												throws ServletException, IOException {
		try {
			if(request.getParameter("Save")!=null) {
				logger.info("******** Adding new NoFax Consignee - begin********");
					TosNoFaxConsigneeMt conFd = new TosNoFaxConsigneeMt();
					String consignee = request.getParameter("addConsignee");
					consignee = consignee.toUpperCase();					
					conFd.setConsigneeName(consignee);
					String phoneNumber = request.getParameter("addPhone");
					conFd.setPhone(phoneNumber);
					String type = request.getParameter("addType");
					type = type==null?"":type.toUpperCase();
					conFd.setType(type);
					String speed = request.getParameter("addSpeed");
					conFd.setSpeed(speed);
										
					boolean isAdded = NoFaxConsigneeDao.addToNoFaxConsignee(conFd);
					if(isAdded) {
						request.setAttribute("message", "<font font size='2' color='Green' face='Courier New'>" + consignee + " has been added successfully.</font>");
						request.getSession().setAttribute("consigneeAdded", "consigneeAdded");
					} else {
						request.setAttribute("message", "<font font size='2' color='Red' face='Courier New'> DATA VIOLATION : " + consignee + " has been already available.</font>");
						request.getSession().setAttribute("consigneeAdded", "consigneeNotAdded");
					}
					logger.info("******** adding new consignee - end********");
				} 
		}catch(Exception ex) {
			logger.error(ex.getMessage());
			request.setAttribute("message", "<font font size='2' color='Red' face='Courier New'>Add Container failed due to -->" + ex.toString() + "</font>");
		}
		request.getRequestDispatcher("NoFaxAddConsignee.jsp").forward(request, response);
	}
}
