package com.matson.tos.processor;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import com.matson.cas.refdata.mapping.TosGumStPlanChasMt;
import com.matson.cas.refdata.mapping.TosGumStPlanCntrMt;
import com.matson.cas.refdata.mapping.TosGumStPlanHazMt;
import com.matson.cas.refdata.mapping.TosGumStPlanHoldMt;
import com.matson.cas.refdata.mapping.TosGumStPlanHoldMtId;
import com.matson.cas.erd.service.data.VesselVO;
import com.matson.cas.service.ftp.FtpBizException;
import com.matson.cas.service.ftp.FtpProxyGetterBiz;
import com.matson.cas.service.ftp.FtpProxyListBiz;
import com.matson.tos.dao.NewVesselDaoGum;
import com.matson.tos.exception.NewVesselLogger;
import com.matson.tos.util.TosRefDataUtil;
import com.matson.tos.vo.OCHFields;
import com.matson.tos.vo.OCRFields;
import com.matson.tos.vo.OHLFields;
import com.matson.tos.vo.OHZFields;
import com.matson.tos.vo.ONDFields;
import com.matson.tos.vo.OVRFields;
import com.matson.tos.vo.XmlFields;


/*
 * 
 * #		Date			Author						Description
 * ---		-----------		------------------			---------------------
 * 1		03/20/2013		Karthik Rajendran			Class file created from HON Newves StowPlanMessageProcessor
 * 2		04/03/2013		Karthik Rajendran			Changed: sendNewVessStowPlanSuccess to sendGumNewVessStowPlanSuccess
 * 3		04/16/2013		Karthik Rajendran			Added: skip empty line breaks, Strip off any comma from Haz_class
 * A4		10/15/2013		Karthik Rajendran			Added: Ignore other vvd container records
 * A5		10/29/2013		Karthik Rajendran			Changed: Set Create_user,last_update_user to "gumnewves" 
 * 
 *
 */


public class GumStowPlanMessageProcessor extends AbstractFileProcessor {

	private static Logger logger = Logger.getLogger(GumStowPlanMessageProcessor.class);
	private ArrayList<String> contentLines;
	private List<XmlFields> ochFields;
	private List<XmlFields> ocrFields;
	private List<XmlFields> ohzFields;
	private List<XmlFields> ohlFields;
	private List<XmlFields> ovrFields;
	private List<XmlFields> ondFields;
	private ArrayList<OCHFields> ochData; 
	private ArrayList<OCRFields> ocrData;
	private ArrayList<OHZFields> ohzData;
	private ArrayList<OHLFields> ohlData;
	private ArrayList<OVRFields> ovrData;
	public String ondFileVesvoy = "";
	public String ondFileLeg = "";
	private ArrayList<TosGumStPlanCntrMt> stowPlanDataList;
	private ArrayList<TosGumStPlanChasMt> stowBareChassisList;
	private ArrayList<TosGumStPlanHazMt> stowHazDataList;
	private static String processType = null;
	private ArrayList<TosGumStPlanHoldMt> stowPlanHoldList;	
	public static final String	   PRIMARY     = "primary";
	public static final String	   SUPPLEMENT  = "supplement";
	public Date			  triggerDate = null;
	private NewVesselLogger nvLogger = NewVesselLogger.getInstance();;
	String ftpFileName = null;
	int ftpProxyId = -1;
	int ftpProxyArchId = -1;
	boolean isFileProcessed = false;

	public void processFiles() 
	{
		try {
			ftpProxyId = Integer.parseInt(TosRefDataUtil.getValue("STOWPLAN_IN_FTP_ID"));
			ftpProxyArchId = Integer.parseInt(TosRefDataUtil.getValue("STOWPLAN_ARCH_FTP_ID"));
			int timeout = Integer.parseInt(TosRefDataUtil.getValue("FTP_TIMEOUT"));
			logger.debug("FTP timeout retrieved is: " + timeout);
			FtpProxyListBiz list = new FtpProxyListBiz();
			list.setTimeout(timeout);
			// Modified the below code to pass the third parameter as *.GUM so that it lists only  files ending with .GUM instead of reading all the files
			String[] stowFiles = list.getFileNames(ftpProxyId, null, "*.GUM");
			if(stowFiles!=null && stowFiles.length > 0)
			{
				FtpProxyGetterBiz getter = new FtpProxyGetterBiz();
				getter.setTimeout(timeout);
				setupStowPlanFieldsData();
				for(int i=0; i<stowFiles.length; i++)
				{
					ftpFileName = stowFiles[i];
					logger.debug("Processing file: " + ftpFileName);
					String contents = getter.getFileText(ftpProxyId, ftpFileName);
					String startWFTime  = (new SimpleDateFormat("MM-dd-yyyy_HH-mm-ss")).format(new Date()).toString();
					if(ftpFileName.endsWith(".GUM"))
					{
						//CommonBusinessProcessor.archivetoFTP(contents, ftpProxyArchId, ftpFileName+"_"+startWFTime,null);
						//
						resetStowPlanData();
						triggerDate = new Date();
						//
						isFileProcessed = false;
						processFile(contents);
						//
						if(isFileProcessed)
						{
							if (!"".equalsIgnoreCase(ondFileVesvoy)) {
								CommonBusinessProcessor.archivetoFTP(contents, ftpProxyArchId, ondFileVesvoy+ondFileLeg+".GUM"+"_"+startWFTime,null);
							}else {
								CommonBusinessProcessor.archivetoFTP(contents, ftpProxyArchId, ftpFileName+"_"+startWFTime,null);
							}
							CommonBusinessProcessor.deleteFtpFiles(ftpProxyId, ftpFileName,null);
						}
					}
					else
					{
						nvLogger.addFileError(ftpFileName, ftpFileName+" is not a .GUM stowplan file");
						//CommonBusinessProcessor.archivetoFTP(contents, ftpProxyArchId, ftpFileName+"_"+startWFTime,null);
						// Code Fix to prevent .HON files being deleted - Start
						logger.info("Commenting code which calls deleteFTP files when not GUM");
						//CommonBusinessProcessor.deleteFtpFiles(ftpProxyId, ftpFileName,null);
						// Code Fix to prevent .HON files being deleted - End
					}
				}
			}
		} catch (FtpBizException ftpEx) {
			logger.error("FTP error found: ", ftpEx);
			nvLogger.addFtpError(""+ftpProxyId, "FTP ERROR: Unable to get into FTP");
		} catch (Exception e) {
			logger.error("Exception: ", e);
			e.printStackTrace();
		} finally {
			
		}
	}
	public GumStowPlanMessageProcessor()
	{
		processFiles();
		nvLogger.sendNewVessErrors();
	}
	public static void main(String[] args)
	{

	}
	private void setupStowPlanFieldsData()
	{
		ocrFields = CommonBusinessProcessor.getFields("/newvesXml/OCR.xml");
		ochFields = CommonBusinessProcessor.getFields("/newvesXml/OCH.xml");
		ohzFields = CommonBusinessProcessor.getFields("/newvesXml/OHZ.xml");
		ohlFields = CommonBusinessProcessor.getFields("/newvesXml/OHL.xml");
		ovrFields = CommonBusinessProcessor.getFields("/newvesXml/OVR.xml");
		ondFields = CommonBusinessProcessor.getFields("/newvesXml/OND.xml");
	}
	private void resetStowPlanData()
	{
		ochData = new ArrayList<OCHFields>();
		ocrData = new ArrayList<OCRFields>();
		ohzData = new ArrayList<OHZFields>();
		ohlData = new ArrayList<OHLFields>();
		ovrData = new ArrayList<OVRFields>();
		stowPlanDataList = new ArrayList<TosGumStPlanCntrMt>();
		stowBareChassisList = new ArrayList<TosGumStPlanChasMt>();
		stowHazDataList = new ArrayList<TosGumStPlanHazMt>();
		stowPlanHoldList = new ArrayList<TosGumStPlanHoldMt>();
	}
	public void processFile(String contents)
	{		
		try{
			contentLines = new ArrayList<String> (Arrays.asList(contents.split("\n")));
			if(contentLines.size()>0)
			{

				for (int i=0; i<contentLines.size(); i++) {
					processLine(contentLines.get(i).toString(),i+1); 
				}
				logger.info("ondFileVesvoy: "+ondFileVesvoy);
				if("".equalsIgnoreCase(ondFileVesvoy)){
					nvLogger.addFileError(ftpFileName, "Error: No OND record vesvoy found. Please check the file for errors.");
					logger.info("Error: No OND data found.");
					CommonBusinessProcessor.deleteFtpFiles(ftpProxyId, ftpFileName,null);
				}
			}
			else {
				logger.error("Error: No data.");
				nvLogger.addFileError(ftpFileName, "Error: No data. Check file for errors.");
				return;
			}
		} catch (Exception e) {
			logger.error("Exception: ", e);
			e.printStackTrace();
		}
	}

	public void processLine(String line, int lineNum)
	{
		if(line==null || line.length()<=0)
			return;
		try {
			if (line.startsWith("OCR"))
			{
				processOCRData(line);
			}
			else if (line.startsWith("OCH"))
			{
				processOCHData(line);
			}
			else if (line.startsWith("OHZ"))
			{
				processOHZData(line);
			}
			else if (line.startsWith("OHL"))
			{
				processOHLData(line);
			}
			else if (line.startsWith("OVR"))
			{
				processOVRData(line);
			}
			else if(line.startsWith("OND"))
			{
				processONDData(line);
				sendStowPlanData();
				resetStowPlanData();
			}
		}
		catch(Exception e){
			e.printStackTrace();
			nvLogger.addFileError(ftpFileName, "Error while parsing/persisting input line  "+lineNum+"<br/>"+ line + "<br/>" + e);
		}
	}
	private void sendStowPlanData()
	{
		HashMap stowPlanMap = new HashMap();
		stowPlanMap.put("OCR", ocrData);
		stowPlanMap.put("OCH", ochData);
		stowPlanMap.put("OHL", ohlData);
		stowPlanMap.put("OHZ", ohzData);
		stowPlanMap.put("OVR", ovrData);
		logger.info("OCR - "+ ocrData.size());
		logger.info("OCH - "+ ochData.size());
		logger.info("OHZ - "+ ohzData.size());
		logger.info("OHL - "+ ohlData.size());
		logger.info("OVR - "+ ovrData.size());
		String vesvoy = ondFileVesvoy;	
		logger.info("getting vessel details:"+vesvoy);

		VesselVO vvo = CommonBusinessProcessor.getVesselDetails(vesvoy.substring(0, 3));
		if(vvo.getVessType() != null && vvo.getVessType().equalsIgnoreCase("L"))
		{
			processType = "newves";
			try{
				NewVesselDaoGum.deleteStowPlanData(vesvoy);
			}catch(Exception e)
			{
				e.printStackTrace();
				nvLogger.addError(vesvoy, "", "Unable to delete stow plan data <br /><br />"+e);
				return;
			}
			try {
				saveStowPlanDetail(stowPlanMap);
				isFileProcessed = true;
				nvLogger.sendGumNewVessStowPlanSuccess(vesvoy, stowPlanDataList.size());
			}
			catch(Exception e)
			{
				e.printStackTrace();
				nvLogger.addFileError(ftpFileName, "Error in "+ondFileVesvoy+"Unable to persist the data into database. Check the database/file for errors.");
			}
		}
	}

	private void processOCRData(String line)
	{
		int beginIndex = 0, endIndex = 0;
		OCRFields ocrf = new OCRFields();
		for(int i=0; i<ocrFields.size(); i++)
		{
			XmlFields fld = (XmlFields)ocrFields.get(i);
			endIndex = new Integer(fld.getFieldLength());
			String temp_str = "";
			if(line.length() >= beginIndex) 
			{
				if(line.substring(beginIndex, line.length()).length()>=endIndex)
					temp_str = line.substring(beginIndex, beginIndex + endIndex).trim();
				else
					temp_str = line.substring(beginIndex, line.length());
			}
			switch(EnumOCRFields.valueOf(fld.getFieldName()))
			{
			case tranCode:
				ocrf.setTranCode(temp_str);break;				 
			case date:
				ocrf.setDate(temp_str); break;
			case time:
				ocrf.setTime(temp_str);break; 
			case user:
				ocrf.setUser(temp_str);break; 
			case pdlgId:
				ocrf.setPdlgId(temp_str); break;
			case ctrNo:
				ocrf.setCtrNo(temp_str); break;
			case dir:
				ocrf.setDir(temp_str); break;
			case srv:
				ocrf.setSrv(temp_str); break;
			case vesvoy:
				ocrf.setVesvoy(temp_str); break;
			case hazf:
				ocrf.setHazf(temp_str); break;
			case odf:
				ocrf.setOdf(temp_str); break;
			case temp:
				ocrf.setTemp(temp_str); break;
			case seal:
				ocrf.setSeal(temp_str); break;
			case cell:
				ocrf.setCell(temp_str); break;
			case typeCode:
				ocrf.setTypeCode(temp_str); break;
			case cWeight:
				ocrf.setcWeight(temp_str); break;
			case discharge:
				ocrf.setDischarge(temp_str); break;
			case dPort:
				ocrf.setdPort(temp_str); break;
			case commodity:
				ocrf.setCommodity(temp_str); break;
			case owner:
				ocrf.setOwner(temp_str); break;
			case tare:
				ocrf.setTare(temp_str); break;
			case tempUnit:
				ocrf.setTempUnit(temp_str); break;
			case bookNo:
				ocrf.setBookNo(temp_str); break;
			case load:
				ocrf.setLoad(temp_str); break;
			case hgt:
				ocrf.setHgt(temp_str); break;
			case strength:
				ocrf.setStrength(temp_str); break;
			case consignee:
				ocrf.setConsignee(temp_str);break;
			case shipper:
				ocrf.setShipper(temp_str); break;
			case cd:
				ocrf.setCd(temp_str); break;
			case damage:
				ocrf.setDamage(temp_str); break;
			case chassis:
				ocrf.setChassis(temp_str); break;
			case pDisp:
				ocrf.setpDisp(temp_str); break;
			case retPort:
				ocrf.setRetPort(temp_str); break;
			case leg:
				ocrf.setLeg(temp_str); break;
			case ds:
				ocrf.setDs(temp_str); break;
			case aei:
				ocrf.setAei(temp_str); break;
			case longhaul:
				ocrf.setLonghaul(temp_str);break; 
			case longhaulDir:
				ocrf.setLonghaulDir(temp_str); break;
			case comment:
				ocrf.setComment(temp_str); break;
			case emptyFull:
				ocrf.setEmptyFull(temp_str); break;
			case primaryCarrier:
				ocrf.setPrimaryCarrier(temp_str); break;
			case trade:
				ocrf.setTrade(temp_str); break;
			case ovzHeight:
				ocrf.setOvzHeight(temp_str); break;
			case ovzLeft:
				ocrf.setOvzLeft(temp_str); break;
			case ovzRight:
				ocrf.setOvzRight(temp_str); break;
			case ovzFront:
				ocrf.setOvzFront(temp_str); break;
			case ovzRear:
				ocrf.setOvzRear(temp_str); break;
			case cmisVent:
				ocrf.setCmisVent(temp_str); break;
			case multipleHazardous:
				ocrf.setMultipleHazardous(temp_str); break;
			case imoClass:
				ocrf.setImoClass(temp_str); break;
			case primaryCntrllr:
				ocrf.setPrimaryCntrllr(temp_str); break;
			}
			beginIndex = beginIndex + endIndex;
		}
		ocrData.add(ocrf);
	}
	private TosGumStPlanCntrMt populateTosStowPlanCntrMt(OCRFields ocrf)
	{
		TosGumStPlanCntrMt ocr = new TosGumStPlanCntrMt();
		String ctrno = ocrf.getCtrNo();
		ctrno = ctrno==null?"":ctrno;
		//String owner = ocrf.getOwner();
		//owner = owner==null?"":owner;
		if(ctrno.length()>0)
		{
			if(!StringUtils.isAlpha(ctrno.substring(0, 1)))
				ctrno = "MATU" + ctrno;
		}
		ocr.setContainerNumber(ctrno);
		ocr.setSrv(ocrf.getSrv());
		ocr.setDport(ocrf.getdPort());
		ocr.setDischargePort(ocrf.getDischarge());
		ocr.setDir(ocrf.getDir());
		ocr.setLeg(ocrf.getLeg());
		ocr.setVesvoy(ocrf.getVesvoy()); // A4
		ocr.setLeg(ondFileLeg);
		ocr.setHazf(ocrf.getHazf());
		ocr.setOdf(ocrf.getOdf());
		String temp = ocrf.getTemp();
		temp = temp==null?"":temp;
		temp = temp.length()>3?temp.substring(0, 3):temp;
		ocr.setTemp(temp);
		ocr.setSealNumber(ocrf.getSeal());
		ocr.setCell(ocrf.getCell());
		ocr.setTypeCode(ocrf.getTypeCode());
		String cweight = ocrf.getcWeight();
		cweight = cweight==null?"":cweight;
		if(cweight.length()>0)
			ocr.setCweight(new BigDecimal(cweight));


		String commodity = ocrf.getCommodity();
		commodity =	commodity.length() >= 8 ? commodity.substring(0, 8)
				: commodity;
		ocr.setCommodity(commodity);
		ocr.setOwner(ocrf.getOwner());
		String tare = ocrf.getTare();
		tare = tare==null?"":tare;
		if(tare.length()>0)
			ocr.setTareWeight(new BigDecimal(tare));
		ocr.setTempMeasurementUnit(ocrf.getTempUnit());
		ocr.setBookingNumber(ocrf.getBookNo());
		ocr.setLoadPort(ocrf.getLoad());
		ocr.setHgt(ocrf.getHgt());
		ocr.setStrength(ocrf.getStrength()==null?"IA":ocrf.getStrength());
		ocr.setConsignee(ocrf.getConsignee());
		ocr.setShipper(ocrf.getShipper());
		ocr.setCheckDigit(ocrf.getCd());
		ocr.setDamageCode(ocrf.getDamage());
		String chsNbr = ocrf.getChassis();
		if(chsNbr != null && chsNbr.equalsIgnoreCase("~"))
			chsNbr = null;
		ocr.setChassisNumber(chsNbr);
		ocr.setPlanDisp(ocrf.getpDisp());
		ocr.setRetPort(ocrf.getRetPort());
		ocr.setDs(ocrf.getDs());
		ocr.setAei(ocrf.getAei());
		ocr.setComments(ocrf.getComment());
		ocr.setErf(ocrf.getEmptyFull());

		String height = ocrf.getOvzHeight();
		height = height==null?"":height;
		if(height.length()>0)
			ocr.setOversizeHeightInches(new BigDecimal(height));
		else
			ocr.setOversizeHeightInches(null);

		String left = ocrf.getOvzLeft();
		left = left==null?"":left;
		if(left.length()>0)
			ocr.setOversizeLeftInches(new BigDecimal(left));
		else
			ocr.setOversizeLeftInches(null);

		String right = ocrf.getOvzRight();
		right = right==null?"":right;
		if(right.length()>0)
			ocr.setOversizeRightInches(new BigDecimal(right));
		else
			ocr.setOversizeRightInches(null);

		String front = ocrf.getOvzFront();
		front = front==null?"":front;
		if(front.length()>0)
			ocr.setOversizeFrontInches(new BigDecimal(front));
		else
			ocr.setOversizeFrontInches(null);

		String rear = ocrf.getOvzRear();
		rear = rear==null?"":rear;
		if(rear.length()>0)
			ocr.setOversizeRearInches(new BigDecimal(rear));
		else
			ocr.setOversizeRearInches(null);	

		ocr.setCreateUser("gumnewves");
		ocr.setCreateDate(triggerDate);
		ocr.setLastUpdateUser("gumnewves");
		ocr.setLastUpdateDate(triggerDate);
		String dischPort = ocrf.getDischarge();
		dischPort = dischPort==null?"":dischPort;
		if(dischPort.equalsIgnoreCase("HON"))
			ocr.setLocationStatus("4");
		else
			ocr.setLocationStatus("2");
		ocr.setLocationRowDeck(ocrf.getPrimaryCarrier());
		ocr.setOrientation(ocrf.getEmptyFull());
		ocr.setHazardousOpenCloseFlag(ocrf.getTrade());
		ocr.setActualVessel(ocrf.getVesvoy().substring(0, 3));
		ocr.setActualVoyage(ocrf.getVesvoy().substring(3, 6));
		return ocr;
	}
	private void processOCHData(String line)
	{
		int beginIndex = 0, endIndex = 0;
		OCHFields ochf = new OCHFields();
		for(int i=0; i<ochFields.size(); i++)
		{
			XmlFields fld = (XmlFields)ochFields.get(i);
			endIndex = new Integer(fld.getFieldLength());
			String temp_str = line.substring(beginIndex, beginIndex + endIndex).trim();
			switch(EnumOCHFields.valueOf(fld.getFieldName()))
			{
			case tranCode:
				ochf.setTranCode(temp_str);break;
			case date:
				ochf.setDate(temp_str);break;
			case time:
				ochf.setTime(temp_str);break;
			case user:
				ochf.setUser(temp_str);break;
			case pdlgId:
				ochf.setPdlgId(temp_str);break;
			case chas:
				ochf.setChas(temp_str);break;
			case cd:
				ochf.setCd(temp_str);break;
			case cell:
				ochf.setCell(temp_str);break;
			case tare:
				ochf.setTare(temp_str);break;
			case vesvoy:
				ochf.setVesvoy(temp_str);break;
			case leg:
				ochf.setLeg(temp_str);break;
			case load:
				ochf.setLoad(temp_str);break;
			case srv:
				ochf.setSrv(temp_str);break;
			case dmg:
				ochf.setDmg(temp_str);break;
			case typeCode:
				ochf.setTypeCode(temp_str);break;
			case owner:
				ochf.setOwner(temp_str);break;
			case ctrNo:
				ochf.setCtrNo(temp_str);break;
			case mgNum:
				ochf.setMgNum(temp_str);break;
			case mgTare:
				ochf.setMgTare(temp_str);break;
			case mgCd:
				ochf.setMgCd(temp_str);break;
			case primaryCarrier:
				ochf.setPrimaryCarrier(temp_str);break;
			case trade:
				ochf.setTrade(temp_str);break;
			}
			beginIndex = beginIndex + endIndex;
		}
		ochData.add(ochf);
	}	
	private TosGumStPlanChasMt populateTosStowPlanChassisMt(OCHFields ochf)
	{
		TosGumStPlanChasMt och = new TosGumStPlanChasMt();
		och.setContainerNumber(ochf.getCtrNo());
		och.setChassisNumber(ochf.getChas());
		och.setChassisCd(ochf.getCd());
		och.setChassisTare(new BigDecimal(ochf.getTare()));
		och.setChassisHolds(ochf.getPrimaryCarrier());
		och.setDamageCode(ochf.getDmg());
		och.setLoc(ochf.getCell());
		String srv = ochf.getSrv();
		if(srv == null || srv.equalsIgnoreCase(""))
			srv = "MAT";
		och.setSrv(srv);
		och.setTypeCode(ochf.getTypeCode());
		String owner = ochf.getOwner();
		String chsNbr = ochf.getChas();
		if(owner == null || owner.equalsIgnoreCase(""))
		{
			if(chsNbr != null && chsNbr.substring(0, 4).equalsIgnoreCase("MATZ"))
			{
				owner = "MATU";
			}
		}
		och.setOwner(owner);
		och.setLocationStatus("4");
		och.setMgNumber(ochf.getMgNum());
		och.setMgTare(ochf.getMgTare().length() >0 ? new BigDecimal(ochf.getMgTare()) : null);
		och.setVesvoy(ondFileVesvoy);
		//och.setLoadPort(ochf.getLoad());
		och.setDport(ochf.getLoad()); // This is actually the destination not the load port
		och.setCreateUser("gumnewves");
		och.setCreateDate(triggerDate);
		och.setLastUpdateUser("gumnewves");
		och.setLastUpdateDate(triggerDate);
		return och;
	}
	private void processOHZData(String line)
	{
		int beginIndex = 0, endIndex = 0;
		OHZFields ohzf = new OHZFields();
		for(int i=0; i<ohzFields.size(); i++)
		{
			XmlFields fld = (XmlFields)ohzFields.get(i);
			endIndex = new Integer(fld.getFieldLength());
			String temp_str = line.substring(beginIndex, beginIndex + endIndex).trim();
			switch(EnumOHZFields.valueOf(fld.getFieldName()))
			{
			case tranCode:
				ohzf.setTranCode(temp_str);break;
			case date:
				ohzf.setDate(temp_str);break;
			case time:
				ohzf.setTime(temp_str);break;
			case user:
				ohzf.setUser(temp_str);break;
			case pdlgId:
				ohzf.setPdlgId(temp_str);break;
			case ctrNo:
				ohzf.setCtrNo(temp_str);break;
			case unNum:
				ohzf.setUnNum(temp_str);break;
			case regs:
				ohzf.setRegs(temp_str);break;
			case hazClass:
				ohzf.setHazClass(temp_str);break;
			case desc:
				ohzf.setDesc(temp_str);break;
			case vesvoy:
				ohzf.setVesvoy(temp_str);break;
			case leg:
				ohzf.setLeg(temp_str);break;
			}
			beginIndex = beginIndex + endIndex;
		}
		ohzData.add(ohzf);
	}	
	private TosGumStPlanHazMt populateTosStowPlanHazMt(OHZFields ohzf)
	{
		TosGumStPlanHazMt ohz = new TosGumStPlanHazMt();
		ohz.setContainerNumber(ohzf.getCtrNo());
		ohz.setUnNumber(new BigDecimal(ohzf.getUnNum()));
		String hazClass = ohzf.getHazClass();
		if(hazClass!=null)
		{
			hazClass = hazClass.replaceAll(",", "").trim();
		}
		ohz.setHazClass(hazClass);
		ohz.setRegs(ohzf.getRegs());
		ohz.setDescription(ohzf.getDesc());
		ohz.setCreateUser("gumnewves");
		ohz.setCreateDate(triggerDate);
		ohz.setLastUpdateUser("gumnewves");
		ohz.setLastUpdateDate(triggerDate);
		return ohz;
	}
	
	private void processOHLData(String line)
	{
		int beginIndex = 0, endIndex = 0;
		OHLFields ohlf = new OHLFields();
		for(int i=0; i<ohlFields.size(); i++)
		{
			XmlFields fld = (XmlFields)ohlFields.get(i);
			endIndex = new Integer(fld.getFieldLength());
			String temp_str = line.substring(beginIndex, beginIndex + endIndex).trim();
			switch(EnumOHLFields.valueOf(fld.getFieldName()))
			{
			case tranCode:
				ohlf.setTranCode(temp_str); break;
			case date:
				ohlf.setDate(temp_str); break;
			case time:
				ohlf.setTime(temp_str); break;
			case user1:
				ohlf.setUser1(temp_str); break;
			case pdlgId:
				ohlf.setPdlgId(temp_str); break;
			case bill:
				ohlf.setBill(temp_str); break;
			case eqNo:
				ohlf.setEqNo(temp_str); break;
			case vesvoy:
				ohlf.setVesvoy(temp_str); break;
			case srv:
				ohlf.setSrv(temp_str); break;
			case code:
				ohlf.setCode(temp_str); break;
			case pby:
				ohlf.setPby(temp_str); break;
			case hType:
				ohlf.sethType(temp_str); break;
			case iGate:
				ohlf.setiGate(temp_str); break;
			case oGate:
				ohlf.setoGate(temp_str); break;
			case rfs:
				ohlf.setRfs(temp_str); break;
			case desc:
				ohlf.setDesc(temp_str); break;
			case aDateFormat:
				ohlf.setaDateFormat(temp_str); break;
			case aTimeFormat:
				ohlf.setaTimeFormat(temp_str); break;
			case cDateFormat:
				ohlf.setcDateFormat(temp_str); break;
			case cTimeFormat:
				ohlf.setcTimeFormat(temp_str); break;
			case aFlag:
				ohlf.setaFlag(temp_str); break;
			case user2:
				ohlf.setUser2(temp_str); break;
			case leg:
				ohlf.setLeg(temp_str); break;
			}
			beginIndex = beginIndex + endIndex;
		}
		ohlData.add(ohlf);
	}
	private TosGumStPlanHoldMt populateTosStowPlanHoldMt(OHLFields ohlf)
	{
		TosGumStPlanHoldMt ohl = new TosGumStPlanHoldMt();
		ohl.setContainerNumber(ohlf.getEqNo());
		ohl.setPlacedBy(ohlf.getPby());
		ohl.setHoldType(ohlf.gethType());
		ohl.setIngateAction(ohlf.getiGate());
		ohl.setOutgateAction(ohlf.getoGate());
		ohl.setRfsAction(ohlf.getRfs());
		ohl.setDescription(ohlf.getDesc());
		String aDate  = ohlf.getaDateFormat();
		aDate = aDate==null?"":aDate;
		try{
			//create SimpleDateFormat object with source string date format
			SimpleDateFormat sdfSource = new SimpleDateFormat("MM/dd/yy");

			//parse the string into Date object
			//Date date = sdfSource.parse(aDate);
			ohl.setActiveDate(sdfSource.parse(aDate));	
			String cDate = ohlf.getcDateFormat();
			cDate = cDate==null?"":cDate;
			ohl.setActiveTime(ohlf.getaTimeFormat());
			ohl.setCancelDate(sdfSource.parse(cDate));
			ohl.setCancelTime(ohlf.getcTimeFormat());
		}
		catch(ParseException e)
		{
			e.printStackTrace();
			logger.error("ActiveDate Parsing error");
		}
		ohl.setUsr(ohlf.getUser2());	
		ohl.setCreateUser("gumnewves");
		ohl.setCreateDate(triggerDate);
		ohl.setLastUpdateUser("gumnewves");
		ohl.setLastUpdateDate(triggerDate);
		return ohl;

	}
	private TosGumStPlanHoldMtId populateTosStowPlanHoldMtId(OHLFields ohlf)
	{
		TosGumStPlanHoldMtId ohlid = new TosGumStPlanHoldMtId();
		ohlid.setCode(ohlf.getCode());
		return ohlid;
	}
	private void processOVRData(String line)
	{
		int beginIndex = 0, endIndex = 0;
		OVRFields ovrf = new OVRFields();
		for(int i=0; i<ovrFields.size(); i++)
		{
			XmlFields fld = (XmlFields)ovrFields.get(i);
			endIndex = new Integer(fld.getFieldLength());
			String temp_str = line.substring(beginIndex, beginIndex + endIndex).trim();
			switch(EnumOVRFields.valueOf(fld.getFieldName()))
			{
			case tranCode:
				ovrf.setTranCode(temp_str); break;
			case date:
				ovrf.setDate(temp_str); break;
			case time:
				ovrf.setTime(temp_str); break;
			case user:
				ovrf.setUser(temp_str); break;
			case pdlgId:
				ovrf.setPdlgId(temp_str); break;
			case ctrNo:
				ovrf.setCtrNo(temp_str); break;
			case height:
				ovrf.setHeight(temp_str); break;
			case left:
				ovrf.setLeft(temp_str); break;
			case right:
				ovrf.setRight(temp_str); break;
			case front:
				ovrf.setFront(temp_str); break;
			case rear:
				ovrf.setRear(temp_str); break;
			case vesvoy:
				ovrf.setVesvoy(temp_str); break;
			case leg:
				ovrf.setLeg(temp_str); break;
			}
			beginIndex = beginIndex + endIndex;
		}
		ovrData.add(ovrf);
	}
	private void processONDData(String line)
	{
		int beginIndex = 0, endIndex = 0;
		ONDFields ondf = new ONDFields();
		for(int i=0; i<ondFields.size(); i++)
		{
			XmlFields fld = (XmlFields)ondFields.get(i);
			endIndex = new Integer(fld.getFieldLength());
			String temp_str = line.substring(beginIndex, beginIndex + endIndex).trim();
			switch(EnumONDFields.valueOf(fld.getFieldName()))
			{
			case tranCode:
				ondf.setTranCode(temp_str); break;
			case date:
				ondf.setDate(temp_str); break;
			case time:
				ondf.setTime(temp_str); break;
			case user:
				ondf.setUser(temp_str); break;
			case pdlgId:
				ondf.setPdlgId(temp_str); break;
			case vesvoy:
				ondf.setVesvoy(temp_str); break;
			case leg:
				ondf.setLeg(temp_str); break;
			}
			beginIndex = beginIndex + endIndex;
		}
		this.ondFileVesvoy = ondf.getVesvoy();
		ondFileLeg = ondf.getLeg();
	}
	private void saveStowPlanDetail(HashMap stowPlanMap) {
		logger.info("Saving stowplan data ...");
		ArrayList ocrDataList = (ArrayList)stowPlanMap.get("OCR");
		ArrayList ochDataList = (ArrayList)stowPlanMap.get("OCH");
		ArrayList ohlDataList = (ArrayList)stowPlanMap.get("OHL");
		ArrayList ohzDataList = (ArrayList)stowPlanMap.get("OHZ");
		ArrayList ovrDataList = (ArrayList)stowPlanMap.get("OVR");

		for (int i = 0; i<ocrDataList.size(); i++) {
			OCRFields ocr1 = (OCRFields)ocrDataList.get(i);
			int temp = 0;
			if(!(ondFileVesvoy).equalsIgnoreCase(ocr1.getVesvoy())) // A4
			{
				temp++;
				logger.info("Ignoring container **** "+ocr1.getCtrNo()+"\t"+ocr1.getVesvoy()+"\t"+ocr1.getLeg());
			}
			if(temp == 0){
				TosGumStPlanCntrMt stowPlan = populateTosStowPlanCntrMt(ocr1);
				String ctrno = stowPlan.getContainerNumber();
				//System.out.println("Stowplan cntrno: "+ctrno);
				String owner = stowPlan.getOwner();
				TosGumStPlanHoldMt stowPlanHold;
				TosGumStPlanHoldMtId stowPlanHoldId;
				TosGumStPlanChasMt stowPlanChassis = null;
				TosGumStPlanHazMt stowPlanHaz;
				Set<TosGumStPlanHoldMt> setStowPlanHold = new HashSet<TosGumStPlanHoldMt>(0);
				Set<TosGumStPlanChasMt> setStowPlanChassis = new HashSet<TosGumStPlanChasMt>(0);
				Set<TosGumStPlanHazMt> setStowPlanHaz = new HashSet<TosGumStPlanHazMt>(0);
				for (int v=0; v<ovrDataList.size(); v++)
				{
					OVRFields ovr1 = (OVRFields) ovrDataList.get(v);
					String ovrctrno = ovr1.getCtrNo();
					if(ovrctrno!=null && ovrctrno.length()>0)
					{
						if(!StringUtils.isAlpha(ovrctrno.substring(0, 1)))
							ovrctrno = "MATU" + ovrctrno;
					}
					if(ovrctrno!=null && ctrno.equalsIgnoreCase(ovrctrno))
					{
						stowPlan.setOversizeHeightInches(ovr1.getHeight().length()>0?new BigDecimal(ovr1.getHeight()):null);
						stowPlan.setOversizeFrontInches(ovr1.getFront().length()>0?new BigDecimal(ovr1.getFront()):null);
						stowPlan.setOversizeLeftInches(ovr1.getLeft().length()>0?new BigDecimal(ovr1.getLeft()):null);
						stowPlan.setOversizeRearInches(ovr1.getRear().length()>0?new BigDecimal(ovr1.getRear()):null);
						stowPlan.setOversizeRightInches(ovr1.getRight().length()>0?new BigDecimal(ovr1.getRight()):null);
						if(stowPlan.getOversizeFrontInches()!=null || stowPlan.getOversizeHeightInches()!=null
								|| stowPlan.getOversizeLeftInches()!=null || stowPlan.getOversizeRearInches() != null
								|| stowPlan.getOversizeRightInches()!=null)
						{
							stowPlan.setOdf("Y");
						}
					}
				}
				for (int j=0; j<ohlDataList.size(); j++)
				{
					OHLFields ohl1 = (OHLFields) ohlDataList.get(j);
					String hlCtrno = ohl1.getEqNo();
					if(hlCtrno!=null && hlCtrno.length()>0)
					{
						if(!StringUtils.isAlpha(hlCtrno.substring(0, 1)))
							hlCtrno = "MATU" + hlCtrno;
					}
					if(hlCtrno!=null && ctrno.equalsIgnoreCase(hlCtrno))
					{
						ohl1.setEqNo(hlCtrno);
						stowPlanHold = populateTosStowPlanHoldMt(ohl1);
						stowPlanHoldId = populateTosStowPlanHoldMtId(ohl1);
						stowPlanHoldId.setTosGumStPlanCntrMt(stowPlan);
						stowPlanHold.setId(stowPlanHoldId);
						setStowPlanHold.add(stowPlanHold);
					}
				}
				stowPlan.setTosGumStPlanHoldMts(setStowPlanHold);

				for(int k=0; k<ochDataList.size(); k++)
				{
					OCHFields och1 = (OCHFields) ochDataList.get(k);
					String chCtrno = och1.getCtrNo();
					if(chCtrno!=null && chCtrno.length()>0)
					{
						if(!StringUtils.isAlpha(chCtrno.substring(0, 1)))
							chCtrno = "MATU" + chCtrno;
					}
					if(chCtrno!=null && ctrno.equalsIgnoreCase(chCtrno))
					{
						och1.setCtrNo(chCtrno);
						stowPlanChassis = populateTosStowPlanChassisMt(och1);
						stowPlanChassis.setTosGumStPlanCntrMt(stowPlan);
						setStowPlanChassis.add(stowPlanChassis);
					}
				}
				stowPlan.setTosGumStPlanChasMts(setStowPlanChassis);
				for(int l=0; l<ohzDataList.size(); l++)
				{
					OHZFields ohz1 = (OHZFields) ohzDataList.get(l);
					String hzCtrno = ohz1.getCtrNo();
					if(hzCtrno!=null && hzCtrno.length()>0)
					{
						if(!StringUtils.isAlpha(hzCtrno.substring(0, 1)))
							hzCtrno = "MATU" + hzCtrno;
					}
					if(hzCtrno!=null && ctrno.equalsIgnoreCase(hzCtrno))
					{
						//System.out.println("Haz found for "+ohz1.getCtrNo());
						ohz1.setCtrNo(hzCtrno);
						stowPlanHaz = populateTosStowPlanHazMt(ohz1);
						stowPlanHaz.setTosGumStPlanCntrMt(stowPlan);
						setStowPlanHaz.add(stowPlanHaz);
						//stowPlan.setHazf("Y");
					}
				}
				stowPlan.setTosGumStPlanHazMts(setStowPlanHaz);
				stowPlanDataList.add(stowPlan);
			}
		}
		for(int b=0; b<ochDataList.size(); b++)
		{
			OCHFields och1 = (OCHFields) ochDataList.get(b);
			String ctrno = och1.getCtrNo();
			int temp = 0;
			/*if("barge".equalsIgnoreCase(processType)){
				if(!(ondFileVesvoy+ondFileLeg).equalsIgnoreCase(och1.getVesvoy()+och1.getLeg()))
				{
					temp++;
					System.out.println("Ignoring bare chassis **** "+och1.getChas()+"\t"+och1.getVesvoy()+"\t"+och1.getLeg());
				}
			}*/
			if (temp == 0) {
				if(ctrno == null || ctrno.equalsIgnoreCase(""))
				{
					TosGumStPlanChasMt bareCh = populateTosStowPlanChassisMt(och1);
					stowBareChassisList.add(bareCh);
				}
			}
		}
		NewVesselDaoGum.insertGumOCRData(stowPlanDataList);
		NewVesselDaoGum.insertGumOCHData(stowBareChassisList);
	}

	public enum EnumOCRFields
	{
		tranCode,date,time,user,pdlgId,ctrNo,dir,srv,vesvoy,hazf,odf,temp,seal,cell,typeCode,cWeight,discharge,
		dPort,commodity,owner,tare,tempUnit,bookNo,load,hgt,strength,consignee,shipper,cd,damage,chassis,pDisp,
		retPort,leg,ds,aei,longhaul,longhaulDir,comment,emptyFull,primaryCarrier,trade,ovzHeight,ovzLeft,ovzRight,
		ovzFront,ovzRear,cmisVent,multipleHazardous,imoClass,primaryCntrllr
	}
	public enum EnumOCHFields
	{
		tranCode,date,time,user,pdlgId,chas,cd,cell,tare,vesvoy,leg,load,srv,dmg,typeCode,owner,ctrNo,mgNum,
		mgTare,mgCd,primaryCarrier,trade
	}
	public enum EnumOHZFields
	{
		tranCode,date,time,user,pdlgId,ctrNo,unNum,regs,hazClass,desc,vesvoy,leg
	}
	public enum EnumOHLFields
	{
		tranCode,date,time,user1,pdlgId,bill,eqNo,vesvoy,srv,code,pby,hType,iGate,oGate,rfs,desc,
		aDateFormat,aTimeFormat,cDateFormat,cTimeFormat,aFlag,user2,leg
	}
	public enum EnumOVRFields
	{
		tranCode,date,time,user,pdlgId,ctrNo,height,left,right,front,rear,vesvoy,leg
	}

	public enum EnumONDFields
	{
		tranCode,date,time,user,pdlgId,vesvoy,leg
	}
}
