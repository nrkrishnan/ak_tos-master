package com.matson.tos.processor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

import com.matson.cas.service.ftp.FtpProxyDeleterBiz;
import com.matson.cas.service.ftp.FtpProxyGetterBiz;
import com.matson.cas.service.ftp.FtpProxySenderBiz;
import com.matson.tos.util.TosRefDataUtil;
import com.oreilly.servlet.MultipartRequest;


public class FTPUploadProcessor  
{   
	private static Logger logger = Logger.getLogger(FTPUploadProcessor.class);
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.sss");
	
	
	private  static final HashMap postMethod(HttpServletRequest request)throws Exception{	
		System.out.println("postMethod Start");
		FileItem fileItem = null;
		HashMap parameterMap = new HashMap();
		try{
			ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
			List<FileItem> fileItems = upload.parseRequest(request);

			for (FileItem item : fileItems) {
	            if (item.isFormField()) {
	                String fieldname = item.getFieldName();
	                String fieldvalue = item.getString();
	                System.out.println("inside loop: "+fieldname+" "+fieldvalue);
	                parameterMap.put(fieldname, fieldvalue);
	            } else {
	                // Process form file field (input type="file").
	                parameterMap.put("fileitem", item);
	            }
	        }
		}catch(Exception e){	
			logger.error("Not Thrown", e);
		}
		System.out.println("postMethod End");
		return parameterMap;
	}
	
	public static final boolean uploadFile(HttpServletRequest request){
		FileItem fileItem = null;
		System.out.println("Start");
		InputStream inputStream = null;
		String fileType = null;
		FileOutputStream fileOutputstream = null;
		String statusMessage = "";
		String fileName="";
		boolean status = false;
		String fileToDelete=null;
		String buttonValue = null;
		int proxyId = 0;
		int uploadProxyId = 0;
		try{
			HashMap parmMap = postMethod(request);
			fileType = (String)parmMap.get("filetype");
			fileToDelete = (String)parmMap.get("filename");
			buttonValue = (String)parmMap.get("buttonclicked");
			System.out.println("fileType is :"+fileType+"- "+fileToDelete+" - "+buttonValue);
			fileItem = (FileItem)parmMap.get("fileitem");
			if (fileType !=null){
		    	if ("stowplan".equals(fileType)) {
		    		proxyId = Integer.parseInt(TosRefDataUtil
							.getValue("STOWPLAN_IN_FTP_ID"));
		    		uploadProxyId = Integer.parseInt(TosRefDataUtil
							.getValue("STOWPLAN_ARCH_FTP_ID"));
		    	} else if ("dcm".equals(fileType)) {
		    		proxyId = Integer.parseInt(TosRefDataUtil
							.getValue("DCM_INS_FTP_ID"));
		    		uploadProxyId = Integer.parseInt(TosRefDataUtil
							.getValue("DCM_ARCH_FTP_ID"));
		    	}else if ("rds".equals(fileType)) {
		    		proxyId = Integer.parseInt(TosRefDataUtil
							.getValue("RDS_IN_FTP_ID"));
		    		uploadProxyId = Integer.parseInt(TosRefDataUtil
							.getValue("RDS_ARCH_FTP_ID"));
		    	}
	    }
		if(fileItem == null){
//				throw new GemsBusinessException("fileItem is null");
				System.out.println("File item is null");
			}else if (fileItem != null && "Upload File".equalsIgnoreCase(buttonValue) ){
				System.out.println("fileItem is not null");
				inputStream = fileItem.getInputStream();
				if(inputStream == null){
					System.out.println("inputStream is null");
				}else{
					fileName = fileItem.getName();
					if(fileName == null){
						System.out.println("fileName is Null");
					}else{
						System.out.println("positve flow");
						int position = fileName.lastIndexOf("\\");
						if(position> 0){
							fileName = fileName.substring(position + 1);
						}
						System.out.println("fileName: "+fileName);
						fileOutputstream = new FileOutputStream(fileName);
						long size = 0L;
					    int c;
					    while ((c = inputStream.read()) != -1) {
					    	fileOutputstream.write(c);
					    	size++;
					    }
				    
					    if (proxyId != 0) {
					    	//writing to FTP proxy location.
					    	if (fileType!=null) {
					    		CommonBusinessProcessor.archiveNVtoFTP(new File(fileName),proxyId,false);
					    	}
					    }
					}
					statusMessage = fileType+" file "+fileName+" upload is Succesful";
					status=true;
				}
			} else if (fileToDelete != null && "Delete File".equalsIgnoreCase(buttonValue)){
				System.out.println("Deleting the file");
				// Delete file from FTP location and archive it.
				String contents = null;
				int timeout = Integer.parseInt(TosRefDataUtil.getValue( "FTP_TIMEOUT"));
				logger.debug("FTP timeout retrieved is: " + timeout);
				FtpProxyGetterBiz getter = new FtpProxyGetterBiz();
				FtpProxySenderBiz sender = new FtpProxySenderBiz();				
				FtpProxyDeleterBiz del = new FtpProxyDeleterBiz();
				getter.setTimeout(timeout);
				sender.setTimeout(timeout);
				del.setTimeout(timeout);
				if (fileType!=null) {
					contents = getter.getFileText(proxyId,fileToDelete );
					sender.sendFile(uploadProxyId, fileToDelete, contents);
					del.removeFile(proxyId, fileToDelete );
				}
				statusMessage = fileToDelete+" is deleted";
				System.out.println("File delete");
			}
		}catch(Exception e){
			statusMessage = fileType+" file "+fileName+" pload Failed";
			System.out.println("The exception is "+e);
			logger.error("The exception is : " + e);
			e.printStackTrace();
			status=false;
		}finally{
			try{
				if(inputStream != null){
					inputStream.close();
				}
			}catch(Exception e){}
			try{
				if(fileOutputstream != null){
					fileOutputstream.close();
				}
			}catch(Exception e){}
		}
		logger.info("End");
		request.setAttribute("statusMessage", statusMessage);
		return status;
	
	}

}




