package com.matson.tos.upload;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.matson.tos.processor.QueryProcessor;


public class QueryEnabler extends HttpServlet {
	public void service(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		try {
			boolean commit = false;
			String queryText = request.getParameter("queryText");
			
			if(request.getParameter("commit") != null && request.getParameter("commit").trim().length() > 0){
				commit = true;
			}
			if(queryText != null && queryText.length() > 0){
				QueryProcessor.executeUpdate(queryText, commit);
			}else{
				throw new Exception("no text added");
			}
			
		} catch (Exception e) {
			System.out.println(e);			
			
		}
	}
}
