/**
 * 
 */
package com.matson.tos.processor;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.log4j.Logger;

import com.matson.cas.service.ftp.FtpBizException;
import com.matson.cas.service.ftp.FtpProxyDeleterBiz;
import com.matson.cas.service.ftp.FtpProxyGetterBiz;
import com.matson.cas.service.ftp.FtpProxyListBiz;
import com.matson.tos.util.TosRefDataUtil;
import com.matson.tos.exception.TosException;

/**
 * @author JZF
 *
 */
public abstract class AbstractFileProcessor implements IFileProcessor {
	private static Logger logger = Logger.getLogger(AbstractFileProcessor.class);
	private int ftpProxyId;
	protected static int STIF_PROC = 1;
	protected static int NEW_VES_PROC = 2;
	protected static int VES_SCHEDULE_PROC = 3;
	protected static int DCM_PROC = 4;
	protected String xmlTestFlag =  TosRefDataUtil.getValue( "XML_TEST_FTP_ID");
   
	private int currProc;

	public AbstractFileProcessor() {
	}
	
	/* (non-Javadoc)
	 * @see com.matson.tos.processor.IFileProcessor#processFiles()
	 */
	public void processFiles() {
		// TODO Auto-generated method stub
		String ftpFileName = null;
		try {
			FtpProxyListBiz list = new FtpProxyListBiz();
			int timeout = Integer.parseInt(TosRefDataUtil.getValue( "FTP_TIMEOUT"));
			logger.debug("FTP timeout retrieved is: " + timeout);
			list.setTimeout(timeout);
		    String[] files = list.getFileNames(ftpProxyId, null, null);
		    logger.debug(  "Total "+ files.length + " number of files/dir are found on the ftp dir.");
			FtpProxyGetterBiz getter = new FtpProxyGetterBiz();
			getter.setTimeout(timeout);
			boolean processed = false;
			
			for ( int i=0; i<files.length; i++) {
				
					ftpFileName = files[i];
					logger.debug( "Processing file: " + ftpFileName);
					String contents = getter.getFileText(ftpProxyId, ftpFileName);
					if ( ftpFileName.startsWith( "STIF") && currProc == STIF_PROC) {
						processFile( contents);
						processed = true;
					} else if ( ftpFileName.startsWith( "NewVes") && currProc == NEW_VES_PROC) { 
						processFile( contents);
						processed = true;
					} else if ( ftpFileName.startsWith( "VesSchedule") && currProc == VES_SCHEDULE_PROC) {
						processFile( contents);
						processed = true;
					} else {
						logger.debug( "The file name: " + ftpFileName + 
								" is not expected and will be ignored.");
					}
					if ( processed) {
						// remove file that has been processed
						logger.debug( "Deleting file: " + ftpFileName);
						FtpProxyDeleterBiz del = new FtpProxyDeleterBiz();
						del.setTimeout(timeout);
						del.removeFile(ftpProxyId, ftpFileName);
					}
				
			}
			
		} catch ( FtpBizException ftpEx) {
			logger.error( "FTP error found: ", ftpEx);
			ftpEx.printStackTrace();
		} 
	}
	
	protected void processFile( String fileContent) {
		try{
			String[] lines = fileContent.split("\n");
			logger.debug( "There are " + lines.length + " lines in the file.");
			for (int i = 0; i < lines.length; i++) {
				processLine(lines[i], i+1);
			}
		}catch(TosException e){
			logger.error( "Error in processFile " + e);
		}
		
	}
	protected abstract void processLine( String aLine, int lineNum)throws TosException;

}
