package com.matson.tos.upload;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.matson.tos.dao.NascentStoreProcDao;

/**
 * Servlet implementation class ClickButtonServlet
 */
public class NascentStoreProc extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public NascentStoreProc() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		String laneId=request.getParameter("laneId");
		NascentStoreProcDao nascentStoreProcDao= new NascentStoreProcDao();
		nascentStoreProcDao.callStoreProc(Integer.parseInt(laneId));
		
		PrintWriter pw= response.getWriter();
		
		pw.print("Request Submitted");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
