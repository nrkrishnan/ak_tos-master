package com.matson.tos.upload;

import java.io.*;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

import com.matson.tos.processor.ManifestRepostProcessor;


public class AGManifest extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4620919870719697564L;
	// Temp location to store uploaded xml file, file will be deleted once its been read and posted.
	private final String UPLOAD_DIRECTORY = "/home/logs/applogs/TOS";
	private static Logger logger = Logger.getLogger(AGManifest.class);
	//
	@SuppressWarnings("unchecked")
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
		if(ServletFileUpload.isMultipartContent(request)){
			try {
				List<FileItem> multiparts = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request); 
				FileItem item = multiparts.get(0);
				File file = null;
				File destFile = null;
				logger.info("item.isFormField()-"+item.isFormField());
				if(!item.isFormField()){
					file = new File(item.getName());
					logger.info("item.getName()-"+item.getName());
					destFile = new File(UPLOAD_DIRECTORY + File.separator + file.getName());
					logger.info("destFile - "+destFile.getName()+"-"+destFile.getAbsolutePath());
					item.write(destFile);
				}
				logger.info("Reading xml file");
				FileInputStream fist = new FileInputStream(destFile);
				InputStreamReader istr = new InputStreamReader(fist);
				BufferedReader reader = new BufferedReader(istr);
				StringBuffer text = new StringBuffer();
				String line = "";
				while ((line = reader.readLine()) != null) {
					text.append(line);
				}
				reader.close();
				logger.info("XML Content retrieved");
				//
				if(destFile!=null && destFile.exists()) {
					destFile.delete();
					logger.info("XML File deleted.");
				} 
				//
				ManifestRepostProcessor poster = new ManifestRepostProcessor();	
				poster.processMsg(text.toString());
				logger.info("XML Posted to AG Manifest");
				//
				//File uploaded successfully
				request.setAttribute("message", "<font font size='2' color='Green' face='Georgia, Arial'>File Posted Successfully. Please check for email notifications.</font>");
			} catch (Exception ex) {
				ex.printStackTrace();
				logger.error(ex.getMessage());
				request.setAttribute("message", "<font font size='2' color='Red' face='Courier New'>File Upload Failed due to " + ex.toString() + "</font>");
			} finally {				
			}
			request.getRequestDispatcher("AGManifestUpload.jsp").forward(request, response);
		}
	}
	
}
