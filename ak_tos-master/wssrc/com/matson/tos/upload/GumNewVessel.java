package com.matson.tos.upload;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.matson.tos.processor.GumNewMatsonVesselJob;

public class GumNewVessel extends HttpServlet {


	/**
	 * 
	 */
	private static final long serialVersionUID = -992893750480981279L;

	public void service(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		Enumeration enumeration = request.getParameterNames();
		while (enumeration.hasMoreElements()) {
			String parameterName = (String) enumeration.nextElement();
			System.out.println("Parameter:::"+parameterName);
		}
		try {
			String newvesProcess = request.getParameter("newves");
			//String bargeProcess = request.getParameter("barge");
			if(newvesProcess!=null && newvesProcess.length()>0)
			{
				GumNewMatsonVesselJob.setProcessType(GumNewMatsonVesselJob.NEWVES);
				String vvd = request.getParameter("vvd");
				if(vvd!=null && vvd.length()==7)
					GumNewMatsonVesselJob.setVvd(vvd.toUpperCase());
				else
					GumNewMatsonVesselJob.setVvd(null);
				/*if (request.getParameter("copyPrimary") != null && request.getParameter("copyPrimary").trim().length() > 0) 
					GumNewMatsonVesselJob.COPY_PRIMARY = "true";
				else
					GumNewMatsonVesselJob.COPY_PRIMARY = "false";*/
				//
				GumNewMatsonVesselJob.executeNewVesProc();
				//
				PrintWriter out = response.getWriter();
				out.println("<html> \n" 
						+ "<head> \n" 
						+ "</head> \n" 
						+ "<body> \n"
						+ "Processing complete, please visit notifications."
						+ " \n" + "</body> \n" 
						+ "</html>");
			}
			else {
				System.out.println("ERROR: GumNewVessel parameter.");
			}
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

