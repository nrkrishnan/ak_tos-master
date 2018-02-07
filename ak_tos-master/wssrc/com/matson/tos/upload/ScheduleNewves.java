package com.matson.tos.upload;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.matson.tos.dao.NewVesselDao;
import com.matson.tos.processor.CommonBusinessProcessor;

public class ScheduleNewves extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -426282285888897850L;
	private static Logger logger = Logger.getLogger(ScheduleNewves.class);

	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			String vesvoy = request.getParameter("vv");
			if(vesvoy!=null && vesvoy.length()>0) {
				vesvoy = vesvoy.toUpperCase();
				boolean validVV = CommonBusinessProcessor.isValidVesvoy(vesvoy);
				if(validVV) {
					String userPrefVvds = NewVesselDao.getUserPrefVvdFromAppParam();
					userPrefVvds = userPrefVvds==null?"":userPrefVvds;
					if(userPrefVvds.length()>0 && userPrefVvds.contains(vesvoy)) {
						logger.info(vesvoy + " was already scheduled.");
						request.setAttribute("message", "<font font size='2' color='Red' face='Courier New'>" + vesvoy + " was already scheduled.</font>");
					} else {
						logger.info(vesvoy + " scheduling now.");
						vesvoy = vesvoy + "W";
						if(userPrefVvds.length()>0)
							userPrefVvds = userPrefVvds + "," + vesvoy ;
						else
							userPrefVvds = vesvoy;
						NewVesselDao.scheduleNewves(userPrefVvds);
						request.setAttribute("message", "<font font size='2' color='Green' face='Courier New'>" + vesvoy + " has been scheduled successfully.</font>");
					}
				} else {
					logger.info(vesvoy + " is invalid.");
					request.setAttribute("message", "<font font size='2' color='Red' face='Courier New'>" + vesvoy + " is not a valid vesvoy.</font>");
				}
			}
		} catch(Exception ex) {
			logger.error(ex.getMessage());
			request.setAttribute("message", "<font font size='2' color='Red' face='Courier New'>Schedule failed due to -->" + ex.toString() + "</font>");
		}
		request.getRequestDispatcher("ScheduleNewves.jsp").forward(request, response);
	}
}
