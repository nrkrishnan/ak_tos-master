/**
 * A1 - 01/29/2009 SKB  Added Cancel Hazards Injection and hazard email.
 * A2 - 07/16/2009 SKB  Switched to unit-hazards instead of TUnit.
 * A3 - 06/21/2011 GR   Check To Process HAZ Insert file to post SNX to TOS
 * A4 - 07/20/2011 GR   Added Loaded unit check for HAz Spreadsheet insert 
 * @author xw8
 *
 */
package com.matson.tos.processor;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.jms.JMSException;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.log4j.Logger;

import com.matson.cas.service.ftp.FtpBizException;
import com.matson.cas.service.ftp.FtpProxyDeleterBiz;
import com.matson.cas.service.ftp.FtpProxyGetterBiz;
import com.matson.cas.service.ftp.FtpProxyListBiz;
import com.matson.tos.dao.TosLookup;
import com.matson.tos.exception.TosException;
import com.matson.tos.jatb.DcmConvert;
import com.matson.tos.jaxb.snx.Snx;
import com.matson.tos.jaxb.snx.TCarrier;
import com.matson.tos.jaxb.snx.TDirection;
import com.matson.tos.jaxb.snx.THazard;
import com.matson.tos.jaxb.snx.THazards;
import com.matson.tos.jaxb.snx.TRouting;
import com.matson.tos.jaxb.snx.TUnit;
import com.matson.tos.jaxb.snx.TUnitHazards;
import com.matson.tos.jaxb.snx.TUnitIdentity;
import com.matson.tos.jaxb.snx.TRouting.Carrier;
import com.matson.tos.messageHandler.AbstractMessageHandler;
import com.matson.tos.messageHandler.DcmCSVMessageHandler;
import com.matson.tos.messageHandler.DcmLateMessageHandler;
import com.matson.tos.messageHandler.SnxMessageHandler;
import com.matson.tos.util.CheckDigit;
import com.matson.tos.util.GroovyXmlUtil;
import com.matson.tos.util.JMSSender;
import com.matson.tos.util.StrUtil;
import com.matson.tos.util.TosRefDataUtil;
import com.matson.tos.util.ExportXmlData;
import com.matson.tos.util.EmailSender;
import com.matson.tos.vo.CommodityVO;
import com.matson.tos.constants.TransitState;


public class DcmLateFileProcessor extends AbstractFileProcessor {

	private static Logger logger = Logger.getLogger(DcmLateFileProcessor.class);
	private DcmLateMessageHandler msgHandler = null;
	private SnxMessageHandler snxMsgHandler = null;
	private JMSSender jmsSender;
	
	private StringWriter out = new StringWriter();
	private String FILE_NAME = "Dcm_Late.xml";
	
	private String _fileName = null;
	
	Snx snxObj = new Snx(); 
	//List<TUnit> unitList = snxObj.getUnit();
	//TUnit aUnit = null;
	List<TUnitHazards> unitList = snxObj.getUnitHazards();
	TUnitHazards aUnit = null;
	
	private String unitId = null;
	private String vessel = "UNK";
	private String voyage = "";
	private DcmCSVMessageHandler csv;
	
	private String MAIL_HOST = TosRefDataUtil.getValue( "MAIL_HOST");
	private String supportMail = TosRefDataUtil.getValue("SUPPORT_EMAIL");
	private String lateDcmEmailToAddress = TosRefDataUtil.getValue("EMAIL_LATE_DCM");
	private StringBuffer unitBuf = new StringBuffer();
	private String type;
	private CheckDigit check = new CheckDigit();
	private String port = "HON";
	
	public DcmLateFileProcessor(String type) {
		try {
			this.type = type;
			msgHandler = new DcmLateMessageHandler( "com.matson.tos.jaxb.snx",
					"com.matson.tos.jatb", "/xml/dcmConvert.xml", AbstractMessageHandler.TEXT_TO_XML);
			
		    snxMsgHandler = new SnxMessageHandler("com.matson.tos.jaxb.snx", 
				    "com.matson.tos.jatb", "",AbstractMessageHandler.TEXT_TO_XML);
			
			
		} catch (TosException tex) {
			//tex.printStackTrace();
			logger.error("Error in creating the object: ", tex);
		}
	}
	
/*   public void processFiles() {
		// TODO Auto-generated method stub
		String ftpFileName = null;
		int ftpProxyId = Integer.parseInt( TosRefDataUtil.getValue( "DCM_IN_FTP_ID"));
		try {
			FtpProxyListBiz list = new FtpProxyListBiz();
		    FTPFile[] files = list.getFileNames(ftpProxyId,null,null);
		    logger.debug(  "Total "+ files.length + " number of files/dir are found on the ftp dir.");
			FtpProxyGetterBiz getter = new FtpProxyGetterBiz();
			//FTPFile aFile = null;
			
			for ( int i=0; i<files.length; i++) {
				boolean processed = false;
				//aFile = files[i];
				if ( aFile.isFile()) {
					ftpFileName = files[i];
					logger.debug( "Processing file: " + ftpFileName);
					String contents = getter.getFileText(ftpProxyId, ftpFileName);
					if ( ftpFileName.toUpperCase().endsWith("LT.TXT")) {
						processFile( contents);
						processed = true;
					} else {
						logger.debug( "The file name: " + ftpFileName + 
								" is not DCM late file and will be ignored.");
					}
					if ( processed) {
						// remove processed file
						logger.debug( "Deleting file: " + ftpFileName);
						FtpProxyDeleterBiz del = new FtpProxyDeleterBiz();
						del.removeFile(ftpProxyId, ftpFileName);
					}
				}
			}
			
		} catch ( FtpBizException ftpEx) {
			logger.error( "FTP error found: ", ftpEx);
			//ftpEx.printStackTrace();
		} 
	}
*/
	
   public void emailFile( String contents) {
	    csv =  new DcmCSVMessageHandler();
		String[] lines = contents.split("\n");
		logger.debug( "There are " + lines.length + " lines in the file.");
		
		processHeader(lines[1]);
		
		for (int i = 2; i < lines.length; i++) {
			//logger.debug("Loop count:"+i);
			processLine(lines[i], i+1);
		}
		
		String vesvoy = vessel+voyage;
    
    // If not a late or final, do not email at all - per Steve from Keiko
    if (DcmConverterFileProcessor.DCM_VER_FINAL.equalsIgnoreCase(type) || DcmConverterFileProcessor.DCM_VER_LATE.equalsIgnoreCase(type)) {
  		EmailSender.mailAttachment(lateDcmEmailToAddress, supportMail, MAIL_HOST, vesvoy + "_" + type + ".csv", csv.toString(), "", vesvoy + " HAZ DCM " + type + " File Processed in N4");
    }
   }
   
   protected void processFile( String contents) 
	{
	 try{
		jmsSender = new JMSSender( JMSSender.BATCH_QUEUE, "HON");
		csv =  new DcmCSVMessageHandler();
		String[] lines = contents.split("\n");
		logger.debug( "There are " + lines.length + " lines in the file.");
		
		processHeader(lines[1]);
		
		
		for (int i = 2; i < lines.length; i++) {
			//logger.debug("Loop count:"+i);
			processLine(lines[i], i+1);
		}
		if(aUnit != null){
		   TosLookup lookup = null;
		   try {
				lookup = new TosLookup();
				CommodityVO vo = lookup.getMostActiveCommodity(aUnit.getUnitIdentity().getId(), port);
				if(vo == null || "SAT".equals(vo.getCommodity()) || vo.getTstate() == null || !vo.getTstate().isActiveUnit()) {
					logger.debug("Skip late dcm "+aUnit.getUnitIdentity().getId());
				} else {
					sendOneUnit();
				}
				
				//A4 - Haz Insert from Spreadsheet to post Tstate Loaded units as the Above Condition Skips it 
				if(_fileName != null && _fileName.endsWith("INS.TXT") && vo.getTstate().isLoadedUnit()){
					sendOneUnit();
				}
		   } catch (Exception e) {
			   logger.error("Error in late dcm "+aUnit.getUnitIdentity().getId(), e);
		   } finally {
			   lookup.close();
		   }
		}
        // TEST XML
		if (xmlTestFlag != null) {
			out.close();
			ExportXmlData.copyXmlToFile(FILE_NAME,out.toString(),xmlTestFlag);
		}
		String vesvoy = vessel+voyage;
		EmailSender.mailAttachment(lateDcmEmailToAddress, supportMail, MAIL_HOST, vesvoy+".csv", csv.toString(), "", vesvoy+" HAZ DCM "+type+" File Processed in N4");
		//A4
		sendCancelInsertHazards(_fileName);
		
	 }
	 catch(Exception e){
		 //e.printStackTrace();
		 logger.error("Error in processFile : ", e);
	 }
	}

	protected void processLine( String lineText, int lineNum) {
	  try {
			
			msgHandler.setTextStr( lineText);
			DcmConvert dcmConvt = (DcmConvert)msgHandler.getTextObj();
			csv.addDcmLine(dcmConvt, vessel, voyage);
			String newId = check.getCheckDigitUnit(StrUtil.trimQuotes( dcmConvt.getCtrNo()));
			if(unitId == null || !unitId.equals(newId)) 
			{
			   //aUnit.setSnxUpdateNote("LATE DCM");
			   if(aUnit != null){
				   TosLookup lookup = null;
				   try {
						lookup = new TosLookup();
						CommodityVO vo = lookup.getMostActiveCommodity(aUnit.getUnitIdentity().getId(), port);
						if(vo == null || "SAT".equals(vo.getCommodity()) || vo.getTstate() == null || !vo.getTstate().isActiveUnit()) {
							logger.debug("Skip late dcm "+aUnit.getUnitIdentity().getId());
						} else {
							sendOneUnit();
						}
				   } catch (Exception e) {
					   logger.error("Error in late dcm "+aUnit.getUnitIdentity().getId(), e);
				   } finally {
					   lookup.close();
				   }
				   unitList.clear();
			   }	
			   unitId = newId;
			   
		       unitBuf.append(unitId).append(","); 
			   
			   aUnit = new TUnitHazards();
			   unitList.add( aUnit);
			   TUnitIdentity id = new TUnitIdentity();
			   TCarrier vv = new TCarrier();
			   if(_fileName != null && _fileName.endsWith("INS.TXT")){ //A3
			     vv.setDirection(TDirection.OB);
			   }else{ 
				 vv.setDirection(TDirection.IB);
			   }
			   vv.setId(vessel+voyage);
			   vv.setMode("VESSEL");
			   vv.setQualifier("DECLARED");
			 //  vv.setQualifier("ACTUAL");
			   id.setId( StrUtil.trimQuotes( unitId ));
			   id.setCarrier(vv);
			   aUnit.setUnitIdentity(id);
			   logger.debug("Initialized Unit : "+unitId);
			}
			
			THazards hazs = aUnit.getHazards();
			if ( hazs == null) {
				hazs = new THazards();
				aUnit.setHazards( hazs);
			}
			List<THazard> hazList = hazs.getHazard();
			THazard aHaz = (THazard)msgHandler.getXmlObj();
			hazList.add( aHaz);

		} catch ( TosException jex) {
			logger.debug( "The error line num = " + lineNum);
			logger.debug( "The error line = " + lineText);
			logger.error( "Exception: ", jex);
		} catch ( Exception ex) {
			logger.debug( "The error line num = " + lineNum);
			logger.debug( "The error line = " + lineText);
			logger.error( "Exception: ", ex);
		}
	}

	private void sendOneUnit() {
	  try {
		  if(jmsSender == null) return; // Don't send for final.
		  
		 snxMsgHandler.setXmlObj(snxObj);
		 String xmlStr = snxMsgHandler.getXmlStr();
		 logger.debug( "XML message: " + xmlStr);
		 jmsSender.send(xmlStr);
		 //TEST XML
		 if ( xmlTestFlag != null) {
			out.write( xmlStr);
			out.write( "\r\n");
		  }
		} catch (TosException tex) {
			logger.error("Tos exception found: ", tex);
			//tex.printStackTrace();
		} catch (JMSException jex) {
			logger.error("JMS exception found: ", jex);
			//jex.printStackTrace();
		}
		catch (Exception ex) {
			logger.error("Exception found: ", ex);
			//ex.printStackTrace();
		}
	} 
	
	private void processHeader(String line) {
		StringTokenizer tokens = new StringTokenizer(line,",");
		if(tokens.countTokens() < 3 ) return;
		tokens.nextToken();
		vessel = tokens.nextToken().trim();
		vessel = vessel.replaceAll("\"", "");
		voyage = tokens.nextToken().trim();
		voyage = voyage.replaceAll("\"", "");
		
	}
	
	private void sendCancelInsertHazards(String fileName) {
		Map<String, String> data = new HashMap<String, String>();
		data.put("vesvoy", vessel+voyage);
		data.put("units", unitBuf.toString());
		data.put("fileName", fileName);
		logger.debug("Units = "+unitBuf.toString());
		//a) Late DCm files Send units to cancel HAz of those units
		//b) HAz Insert sends units to trigger customer event RECONCILE_HAZ
		String xmlStr = null;
		xmlStr = GroovyXmlUtil.getInjectionXmlStr( "GvyInjRemoveHazards", "database", data);
		try {
			logger.debug("RemoveHaz Xml ="+xmlStr);
			jmsSender.send(xmlStr);
		} catch (Exception ex) {
			logger.error("JMS exception found: ", ex);
		}
	}
	
	public void setFileName(String name){
		this._fileName = name;
	}
	
/*	public static void main( String[] args) 
	{
		try
		{
			File file = new File("C:/TEST_DIR_ALL/TestDCM/testsubfolder/HAZMHI.TXT");
			if(file != null)
			{
			  System.out.println("File :: "+file);
			}
			System.out.println("FILE NAME :"+file.getName());
			BufferedReader br = new BufferedReader(new FileReader(file));
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = br.readLine()) != null) 
			{
				sb.append(line + "\n");
			}
				br.close();
				String fileStr = sb.toString();
				DcmLateFileProcessor dcmLate = new DcmLateFileProcessor();
				System.out.println("Before processFile(fileStr)");
				dcmLate.processFile(fileStr);
				System.out.println("After processFile(fileStr)");
			
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
	}
*/
	
}
