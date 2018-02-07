/*
*********************************************************************************************
* Srno   Date			AuthorName			Change Description
* A1     02/25/08       Glenn Raposo		Changes made to Copy XML string to Temp Test File
* A2     05/06/09		Steven Bauer		Changes to close open visits, clean up schedule and not
* 											Depart the vessel until N4 has.
* A3     05/13/09		Steven Bauer		Move the new units at the end to prevent conflicts.
* A4	 06/02/09		Steven Bauer	    Only update units that don't match the existing unit.
* A4	 06/23/09		Steven Bauer		Fixed Update where activeVO == WORKING and visitVO == Departed,
* 											Was updating to departed, now leave it as Working.
* A5     07/01/09		Steven Bauer		Moved advancing the Phase to Groovy
* A6	 07/15/09		Steven Bauer		Fixed YB Close
* A7	 07/29/09		Steven Bauer		Reduce memory footprint
* A8	 08/06/2009		Steven Bauer	    Added Horizon auto advance.
* A9	 09/22/2009		Steven Bauer		Testing FSS->N4 direct
* A10	 10/15/2009		Steven Bauer		Set start time on depart to make adjust if needed.
* A11    02/01/10       Glenn Raposo		Added MOL and LNI for YB Service & Itinerary
* A12    04/06/10       Glenn Raposo	    Removed the portStatusCode check in getPhase
*                                           Added code to write output messages to a file
*                                           Move working to 24Hrs Prior
* A13    11/18/10       Glenn Raposo	    Set Service & Itinerary for PAH Vessel
* A14    10/30/11       Glenn Raposo	    TOS2.1 Issue : Added classification Attribute
* A15	 09/22/14		Karthik Rajendran	EP000169724 - TOS Departing YB Barges based on pacific time instead of hawaiian
*********************************************************************************************
*/
package com.matson.tos.processor;

import com.matson.cas.service.ftp.FtpBizException;
import com.matson.cas.service.ftp.FtpProxyDeleterBiz;
import com.matson.cas.service.ftp.FtpProxyGetterBiz;
import com.matson.cas.service.ftp.FtpProxyListBiz;
import com.matson.sched.Schedule;
import com.matson.sched.Voyage;
import com.matson.sched.view.PortCallView;
import com.matson.sched.view.PortCallViewBean;
import com.matson.tos.dao.TosLookup;
import com.matson.tos.exception.TosException;
import com.matson.tos.jatb.VesSchedule;
import com.matson.tos.jaxb.snx.Snx;
import com.matson.tos.jaxb.snx.TVesselVisit;
import com.matson.tos.messageHandler.AbstractMessageHandler;
import com.matson.tos.messageHandler.SnxMessageHandler;
import com.matson.tos.messageHandler.VesScheduleMessageHandler;
import com.matson.tos.util.*;
import com.matson.tos.vo.VesselVisitVO;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.log4j.Logger;

import java.lang.String;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.*;


public class VesScheduleFileProcessor extends AbstractFileProcessor {
	public static final String VISIT_ADVANCE_SKIP = "VISIT_ADVANCE_SKIP";
	public static final String VESSEL_ADVANCE_SKIP = "VESSEL_ADVANCE_SKIP";

	private static Logger logger = Logger.getLogger(VesScheduleFileProcessor.class);

	private VesScheduleMessageHandler msgHandler = null;

	//private Snx snxObj = new Snx();

	//private List<TVesselVisit> vesselVisitList = snxObj.getVesselVisit();

	private List<VesselVisitVO> vesselVisitList = new ArrayList<VesselVisitVO>();
	private List<VesselVisitVO> newVesselVisitList = new ArrayList<VesselVisitVO>();

	private VesselVisitVO vesVisit = null;

	//private VesselVisitVO currVesVisit = null;

	private SnxMessageHandler snxMsgHandler = null;
	private JMSSender sender;
	//private String currSeqNum = null;

	//A1 Starts
	private StringWriter out = new StringWriter();
	private static String FILE_NAME = "VesVisit.xml";
	//A1 Ends

	//private String destQueueId = "";

	private List<VesselVisitVO> currentVisits;

	private StringBuffer portRotation = new StringBuffer();
	private String currentVessel = "";

	private static long offset;

	private String[] vesselLines;

	private String prevService;

	private StringBuffer bufout;
//	private Map<Snx,String> multiVesVisitMap = new HashMap<Snx,String>(); //A12
private String visitsNotToAdvance = TosRefDataUtil.getValue(VISIT_ADVANCE_SKIP);
	private String vesselNotToAdvance = TosRefDataUtil.getValue(VESSEL_ADVANCE_SKIP);

	public VesScheduleFileProcessor() {
		try {
			//TosRefData ftpRef = (TosRefData)RefDataLookup.queryById( "TosRefData", "VesScheduleFtpId");
			//this.setFtpProxyId( Integer.parseInt( ftpRef.getValue()));
			msgHandler = new VesScheduleMessageHandler(
					"com.matson.tos.jaxb.snx", "com.matson.tos.jatb",
					"/xml/vesSchedule.xml", AbstractMessageHandler.TEXT_TO_XML);
			snxMsgHandler = new SnxMessageHandler(
					"com.matson.tos.jaxb.snx", "com.matson.tos.jatb", "",
					AbstractMessageHandler.TEXT_TO_XML);
			//sender = new JMSSender(JMSSender.BATCH_QUEUE);
		} catch (TosException tex) {
			tex.printStackTrace();
			logger.error("Error in creating the object: ", tex);
		//} catch (RefDataException rex ) {
		//	rex.printStackTrace();
		//	logger.error("Error in getting CAS Ref Data: " + rex);
		}
	}

	public void processFiles() {
		// TODO Auto-generated method stub
		String ftpFileName = null;
		int ftpProxyId = Integer.parseInt( TosRefDataUtil.getValue( "FSS_FILES_FTP_ID"));

		try {
			FtpProxyListBiz list = new FtpProxyListBiz();
			int timeout = Integer.parseInt(TosRefDataUtil.getValue( "FTP_TIMEOUT"));
			logger.debug("FTP timeout retrieved is: " + timeout);
			list.setTimeout(timeout);
		    String[] files = list.getFileNames(ftpProxyId,null,null);
		    //logger.debug(  "Total "+ files.length + " number of files/dir are found on the ftp dir.");
			FtpProxyGetterBiz getter = new FtpProxyGetterBiz();
			getter.setTimeout(timeout);
			FTPFile aFile = null;

			for ( int i=0; i<files.length; i++) {
				boolean processed = false;
					ftpFileName = files[i];
					logger.debug( "Processing file: " + ftpFileName);
					String contents = getter.getFileText(ftpProxyId, ftpFileName);
					if ( ftpFileName.equalsIgnoreCase( "VESVISIT.TXT")) {
						processFile( contents);
						processed = true;
					} else {
						logger.debug( "The file name: " + ftpFileName +
								" is not Vessel Schedule file and will be ignored.");
					}
					if ( processed) {
						// remove file that has been processed
						logger.debug( "Deleting file: " + ftpFileName);
						FtpProxyDeleterBiz del = new FtpProxyDeleterBiz();
						del.removeFile(ftpProxyId, ftpFileName);
					}

			}

		} catch ( FtpBizException ftpEx) {
			logger.error( "FTP error found: ", ftpEx);
			ftpEx.printStackTrace();
		}
	}

	protected void processFile( String contents) {
		try
		{
			currentVisits = null;
			TosLookup lookup = null;
			try {
				lookup = new TosLookup();
				currentVisits = lookup.getActiveVV();
				logger.debug("Found "+currentVisits.size()+" current visits");
			} finally {
				if(lookup != null) lookup.close();
			}

			String[] lines = contents.split("\n");
			logger.debug( "There are " + lines.length + " lines in the file.");
			for (int i = 0; i < lines.length; i++) {
				processLine(lines[i], i+1);
			}

			logger.debug(currentVisits.size()+" remaining/remain visits");
			addCanceledVisits();
			// A3 Add the new vessel visits at the end.
			vesselVisitList.addAll(newVesselVisitList);
			newVesselVisitList.clear();

			logger.debug(vesselVisitList.size()+" visits to update");
			// the last ves visit has not been sent out.
			if ( vesVisit != null) {
				sendAllVesVisit();
				vesselVisitList.clear();
			}
			// if an XML file needs to be created
			if ( xmlTestFlag != null) {
				//A1 change starts
				out.close();
				ExportXmlData.copyXmlToFile( FILE_NAME, out.toString(),xmlTestFlag);
				//A1 change ends
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			logger.error("Error in processFile : ", e);
		} finally {
			currentVisits = null;
		}
	}

	private void addCanceledVisits() {
		visitsNotToAdvance = TosRefDataUtil.getValue(VISIT_ADVANCE_SKIP);
		vesselNotToAdvance = TosRefDataUtil.getValue(VESSEL_ADVANCE_SKIP);
		Iterator<VesselVisitVO> iter = currentVisits.iterator();
		while(iter.hasNext()) {
			VesselVisitVO vo = iter.next();

			if (vo.isActive() && isValidVisitForAdvancing(vo.getId())) {  //DONOT sail vessel (NSS) should not be cancelled
				TVesselVisit visit= new TVesselVisit();
				visit.setId(vo.getId());
				visit.setVisitPhase(VesselVisitVO.CANCELED);
				visit.setInVoyNbr("C"+vo.getId());
				vo.setXml(visit);
				vesselVisitList.add(0,vo);  // Add to the top of the list,
				// Should keep them from interfering with the real schedule.
				logger.debug("Vessel Visit Cancel "+vo.getId()+" "+vo.getPort()+" "+vo.getPhase());
			} else {
				logger.debug("Vessel Visit Skip cancel : "+vo.getId()+" "+vo.getPort()+" "+vo.getPhase());
			}
		}
	}


	/**
	 * <p>The methods determines if the visit is valid to be advanced. The visits represented by list <i>visitsNotToAdvance</i> are not to be advanced<p>
	 *
	 * @param inVisitId Vessel+VisitId
	 * @return True if visit could be advanced, False otherwise
	 */
	private Boolean isValidVisitForAdvancing(String inVisitId) {
		if (visitsNotToAdvance == null || visitsNotToAdvance.length() == 0) {
			logger.error("List of Vessel Visits, that should not be closed or cancelled is not defined, look for " +
					"reference data with key " + VISIT_ADVANCE_SKIP);
			if (inVisitId.startsWith("NSS") || inVisitId.contains("LCL") || inVisitId.contains("DUM"))
				return Boolean.FALSE;
		} // Above Block is not required, but it's an fallback option
        if (vesselNotToAdvance != null && !vesselNotToAdvance.isEmpty() &&vesselNotToAdvance.length() > 0) {
            List<String> vesselsList = Arrays.asList(vesselNotToAdvance.split("\\s*,\\s*"));
            //if all visits will be listed (case insensitive)
            Boolean isVesselInList = Boolean.FALSE;
            String inVesselId = null;
			if (inVisitId != null && !inVisitId.isEmpty() && inVisitId.trim().length() == 6) {
				inVesselId = inVisitId.trim().substring(0, 3);
			}
            if (inVesselId != null) {
                for (String vesselId : vesselsList) {
					if (!vesselId.isEmpty() && vesselId.trim().length() == 3 && vesselId.trim().equalsIgnoreCase(inVesselId)) { // need to use startsWith if not all visits listed
						logger.info(inVisitId + " is not eligible for advancing it's visit phase, the vessel (" + inVesselId + ") is marked to be " +
                                "skipped by reference data with key " + VESSEL_ADVANCE_SKIP);
                        return Boolean.FALSE;
                    }
                }
            }

        }
        if (visitsNotToAdvance != null && !visitsNotToAdvance.isEmpty() && visitsNotToAdvance.length() > 0) {
			List<String> vesselVisitsList = Arrays.asList(visitsNotToAdvance.split("\\s*,\\s*"));
			//if all visits will be listed (case insensitive)
			Boolean isVisitInList = Boolean.FALSE;
			for (String vesselVisitId : vesselVisitsList) {
				if (!vesselVisitId.isEmpty() && vesselVisitId.trim().length() == 6 && vesselVisitId.trim().equalsIgnoreCase(inVisitId)) { // need to use startsWith if not all visits listed
					logger.info(inVisitId + " is not eligible for advancing it's visit phase, it is marked to be " +
							"skipped by reference data with key " + VISIT_ADVANCE_SKIP);
					return Boolean.FALSE;
				}
			}
		}
		return Boolean.TRUE;
	}

	private void appendPhase(VesselVisitVO vesVisit, String phase) {
		String note = vesVisit.getXml().getNotes();
		if(note == null) note = "";
		note += " phase_vv='"+VesselVisitVO.DBPhase(phase)+"'";
		vesVisit.getXml().setNotes(note);
	}

	@Override
	protected void processLine(String msg, int lineNum) {
		bufout = new StringBuffer();
		try {
			if ( msg.trim().length() < 3) {
				logger.debug( "Wrong line with length " + msg.trim().length() + " on line:" + lineNum);
				return;
			}
			msgHandler.setTextStr(msg);
			//logger.debug( "Processing line: " + msg);
			VesSchedule vesObj = (VesSchedule) msgHandler.getTextObj();
			String recType = StrUtil.trimQuotes( vesObj.getRecType());

			if (recType.equalsIgnoreCase("vesselVisit")) {
				vesVisit = new VesselVisitVO();
				vesVisit.setXml((TVesselVisit) msgHandler.getXmlObj());

				vesVisit.setPort(StrUtil.trimQuotes( vesObj.getDestQueueId().trim()));
				vesVisit.setSeqNo(StrUtil.trimQuotes( vesObj.getSeqNum()));
				vesVisit.setId(vesVisit.getXml().getId());
				vesVisit.setPhase(vesVisit.getXml().getVisitPhase());

				boolean liveVisit = true;
				// Check if it is an existing phase
				if(currentVisits.contains(vesVisit)) {

					VesselVisitVO activeVO = currentVisits.remove(currentVisits.indexOf(vesVisit));
					// YB needs to be moved to closed after ETD, this forces the matter.
					boolean ybDepart = false;
					if(vesVisit.getXml().getETD() != null && vesVisit.getXml().getVesselId() != null && vesVisit.getXml().getVesselId().toUpperCase().startsWith("YB")) {
						vesVisit.getXml().setATA(vesVisit.getXml().getETA());
						vesVisit.getXml().setTimeStartWork(vesVisit.getXml().getETA());
						GregorianCalendar cal = new GregorianCalendar();
						XMLGregorianCalendar etd
						= vesVisit.getXml().getETD();
						if(etd.toGregorianCalendar().before(cal)) {
							ybDepart = true;
							vesVisit.getXml().setATD(vesVisit.getXml().getETD());
							vesVisit.getXml().setTimeStartWork(vesVisit.getXml().getETD());
							vesVisit.getXml().setTimeEndWork(vesVisit.getXml().getETD());
						}
					}

					boolean horizon = false;
					String service = vesVisit.getXml().getServiceId();
					if(service != null && service.startsWith("HRZ")) {
						horizon = true;
					}


					if(!ybDepart && ( activeVO.isArchived() || activeVO.isNotChanged(vesVisit.getXml()))) {
						logger.debug("Vessel Visit Not changed "+vesVisit.getId()+" "+vesVisit.getPort()+" "+vesVisit.getPhase()+" "+activeVO.getPhase());
						return;
					} else {
						logger.debug("Vessel Visit Import "+vesVisit.getId()+" "+vesVisit.getPort()+" "+vesVisit.getPhase()+" "+activeVO.getPhase());

					}



					if(activeVO.isCanceled() ) {
						// do nothing, This will revive the unit
					} else {
						// A5, Use the current phase.
						vesVisit.getXml().setVisitPhase(activeVO.getPhase());
						if (activeVO.isArchived() ) {
							vesVisit.setPhase(activeVO.getPhase());
						} else if(activeVO.isDeparted()) {
							XMLGregorianCalendar atd = vesVisit.getXml().getATD();
							if(atd == null && vesVisit.getXml().getVesselId() != null && vesVisit.getXml().getVesselId().toUpperCase().startsWith("YB")) {
								atd = vesVisit.getXml().getETD();
							}

							if(atd == null) {
								vesVisit.setPhase(VesselVisitVO.DEPARTED);
							} else {
								GregorianCalendar cal = new GregorianCalendar();
								int delay = TosRefDataUtil.getClosedTime(vesVisit.getId());
								cal.add(Calendar.DAY_OF_MONTH, delay);
								// Leave the vessel departed for a preset # of days.
								logger.debug(atd.toGregorianCalendar().getTime() +" "+cal.getTime()+" delay="+delay);
								vesVisit.setPhase(VesselVisitVO.DEPARTED);
								// If the depart date is more than delay days ago close it.
								if(atd.toGregorianCalendar().before(cal)) {
									appendPhase(vesVisit, VesselVisitVO.CLOSED);
								}
							}
						} else if(activeVO.isComplete()) {
							if(ybDepart) {
								appendPhase(vesVisit, VesselVisitVO.DEPARTED);
							}
							if(vesVisit.getPhaseId() > activeVO.getPhaseId() && vesVisit.isManagedPort()) {
								appendPhase(vesVisit, VesselVisitVO.DEPARTED);
							}
							if(vesVisit.getPhaseId() > activeVO.getPhaseId() && horizon) {
								appendPhase(vesVisit, VesselVisitVO.DEPARTED);
							}
							vesVisit.setPhase(activeVO.getPhase());
						} else if(activeVO.isWorking()) {
							if(ybDepart) {
								appendPhase(vesVisit, VesselVisitVO.DEPARTED);
							}
							if(vesVisit.getPhaseId() > activeVO.getPhaseId() && vesVisit.isManagedPort()) {
								appendPhase(vesVisit, VesselVisitVO.DEPARTED);
							}
							if(vesVisit.getPhaseId() > activeVO.getPhaseId() && horizon) {
								appendPhase(vesVisit, VesselVisitVO.DEPARTED);
							}
							vesVisit.setPhase(activeVO.getPhase());
						} else {
							if(activeVO.getPhaseId() < vesVisit.getPhaseId()) {
								appendPhase(vesVisit, activeVO.nextPhase());
							}
							vesVisit.setPhase(activeVO.getPhase());
						}
					}
					vesselVisitList.add(vesVisit);
				} else {
					logger.debug("Vessel Visit Import New "+vesVisit.getId()+" "+vesVisit.getPort()+" "+vesVisit.getPhase());
					newVesselVisitList.add(vesVisit);
				}

			} else if (recType.equalsIgnoreCase("line")) {
				if (vesVisit != null) {
					TVesselVisit.Lines.Line aLine = (TVesselVisit.Lines.Line) msgHandler
							.getXmlObj();
					TVesselVisit.Lines lines = vesVisit.getXml().getLines();
					List<TVesselVisit.Lines.Line> lineList;
					if (lines == null) {
						lines = new TVesselVisit.Lines();
						vesVisit.getXml().setLines(lines);
					}
					lineList = lines.getLine();
					lineList.add(aLine);
				} else {
					logger.error("The VesVisit object is null when adding a line.");
				}

			} else if (recType.equalsIgnoreCase("berthing")) {
				if (vesVisit != null) {
					TVesselVisit.Berths berths = vesVisit.getXml().getBerths();
					if (berths == null) {
						berths = new TVesselVisit.Berths();
						vesVisit.getXml().setBerths(berths);
					}
					List<TVesselVisit.Berths.Berthing> berthList = berths
							.getBerthing();

					TVesselVisit.Berths.Berthing aBerth = (TVesselVisit.Berths.Berthing) msgHandler
							.getXmlObj();

					berthList.add(aBerth);
				} else {
					logger.error("The VesVisit object is null when adding a berthing.");
				}
			} else if (recType.equalsIgnoreCase("estMoveCount")) {
				if (vesVisit != null) {
					TVesselVisit.EstMoveCount aMoveCount = (TVesselVisit.EstMoveCount) msgHandler
							.getXmlObj();
					vesVisit.getXml().setEstMoveCount(aMoveCount);
				} else {
					logger.error("The VesVisit object is null when adding a estMoveCount.");
				}
			} else {
				logger.debug("Wrong message type: " + recType);
			}
		} catch (TosException tex) {
			logger.error("Error in text message converting: ", tex);
			logger.debug("Error in processing line: " + lineNum + ". Line=[" + msg + "]");
		} catch ( Exception ex) {
			logger.error("Error in text message converting: ", ex);
			logger.debug("Error in processing line: " + lineNum + ". Line=[" + msg + "]");
		}
	}

	private void sendAllVesVisit() {
		// we should have the snx object now.
		logger.debug("Vessel schedule Count = "+vesselVisitList.size());
		Iterator<VesselVisitVO> iter = vesselVisitList.iterator();
		while(iter.hasNext()) {
			VesselVisitVO visit = iter.next();
			/**
			 * Alaska Don't publish Departed from FSS, it'll be done manually at ports.
			 */
			if ((visit.getPhase() != null && ("DEPARTED".equalsIgnoreCase(visit.getPhase())||("60DEPARTED").equalsIgnoreCase(visit.getPhase())))
			||(visit.getPhaseId()==60) || (visit.getXml().getNotes()!=null && visit.getXml().getNotes().contains("60DEPARTED"))){
				logger.info("Vessel " + visit.getId() + " with visit sequence number " + visit.getSeqNo() + "is not posted to N4 as it's in phase" + visit.getPhase() +"notes is"+visit.getXml().getNotes());
				logger.info("VO Object"+visit);
			} else {
				Snx snxObj = new Snx();
				snxObj.getVesselVisit().add(visit.getXml());
				try {
					snxMsgHandler.setXmlObj(snxObj);
					String xmlStr = snxMsgHandler.getXmlStr();
					logger.debug("XML message:(" + visit.getPort() + ")" + xmlStr);
					if (visit.getPort() == null || visit.getPort().length() < 1) {
						logger.error("No destination queue found.");
					}
					sender = new JMSSender(JMSSender.BATCH_QUEUE, visit.getPort().toUpperCase());
					sender.send(xmlStr);
					bufout.append(xmlStr + "\r\n");
					if (xmlTestFlag != null) {
						//A1 change starts
						out.write("Line: " + visit.getSeqNo() + " Queue: " + visit.getPort() + " XML: " + xmlStr);
						out.write("\r\n");
						//A1 change ends
						// Jobs can not be scheduled to close togeather, slow the process down to prevent this.

					}
					Thread.sleep(1000);
				} catch (TosException tex) {
					logger.error("Tos exception found: ", tex);
					//} catch (JMSException jex) {
					//	logger.error("JMS exception found: ", jex);
				} catch (Exception ex) {
					logger.error("Exception found: ", ex);
				}
			}
		}//While ends A12
	}

	/*
	public static void main( String[] args) {
		VesScheduleFileProcessor proc = new VesScheduleFileProcessor();
    	proc.processFiles();
	} */
/*	public static void main( String[] args)
	{
		try
		{
			java.io.File file = new java.io.File("C:/TestXMLtoFlatFile/VESVISIT.TXT");
			if(file != null)
			{
			  System.out.println("File :: "+file);
			}
			java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(file));
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = br.readLine()) != null)
			{
				sb.append(line + "\n");
			}
				br.close();
				String fileStr = sb.toString();
				VesScheduleFileProcessor newVes = new VesScheduleFileProcessor();
				System.out.println("Before processFile(fileStr)");
				newVes.processFile(fileStr);
				System.out.println("After processFile(fileStr)");

		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
*/

	/**
	 * Replaces ProcessFile, instead of reading from a Text file it uses a FSS schedule object to
	 * create the SNX to feed to N4.
	 */
	public void processSchedule( Schedule sched) {
		logger.info("*************** BEGIN : VesScheduleFileProcessor.processSchedule() ***************");
        vesselVisitList.clear();
		 bufout = new StringBuffer();
		try {
			String lineStr = TosRefDataUtil.getValue("VESSEL_LINES");
			if(lineStr != null) vesselLines = lineStr.split(" *, *");
			else {
				vesselLines = new String[1];
				vesselLines[0] = "MAT";
			}
			PortCallView view = new PortCallView(sched);
			List portCallList = view.getPortCalls();
			if(portCallList == null) {
				logger.error("Could not read schedule");
				return;
			}

			TosLookup lookup = null;
			try {
				lookup = new TosLookup();
				currentVisits = lookup.getActiveVV();
				logger.debug("Found "+currentVisits.size()+" current visits");
			} finally {
				if(lookup != null) lookup.close();
			}

			Iterator portCallIter = portCallList.iterator();
			while(portCallIter.hasNext()) {
				PortCallViewBean portCall = (PortCallViewBean)portCallIter.next();

				// Skip Z Trade records
				if(portCall.getDeparture() != null && "Z".equals(portCall.getDeparture().getTrade().getCode())) {
					continue;
				}

				if("HNC".equals(portCall.getPort().getCode()))  portCall.getPort().setCode("HON");

				if(!JMSSender.isMappedPort(portCall.getPort().getCode())) continue;

				TVesselVisit xml = processPortCall(portCall);
				//logger.info("PortCall "+xml);

				if(xml == null) continue;

				//System.out.println("Trade="+portCall.getDeparture().getTrade().getCode());
				Snx snxObj = new Snx();
				snxObj.getVesselVisit().add(xml);
				snxMsgHandler.setXmlObj(snxObj);
				String xmlStr = snxMsgHandler.getXmlStr();
				logger.debug( "RAW XML message: " + xmlStr);
				vesVisit = new VesselVisitVO();
				vesVisit.setXml(xml);

				vesVisit.setPort(portCall.getPort().getCode());

				vesVisit.setSeqNo(xml.getInCallNumber().toString());
				vesVisit.setId(xml.getId());



				boolean liveVisit = true;
				boolean YB = false;  boolean HRZ = false;
				String phase= null;  VesselVisitVO activeVO = null;
				// Check if it is an existing phase
				if(currentVisits.contains(vesVisit)) {
					 activeVO = currentVisits.remove(currentVisits.indexOf(vesVisit));
					 //logger.debug("Found :Current Visit in N4="+activeVO.getId());
					YB = vesVisit.getId().toUpperCase().startsWith("YB") ? true : false;
					logger.debug("is YB : " + YB + ", Found :Current Visit in N4="+activeVO.getId());
					if(YB) {
						if(xml.getATA() == null) xml.setATA(xml.getETA());
						if(xml.getATD() == null) xml.setATD(xml.getETD());
					}

					HRZ = xml.getServiceId() != null && xml.getServiceId().startsWith("HRZ") ? true : false;

					phase = getPhase(portCall,activeVO, YB, HRZ);

					//System.out.println("ID="+vesVisit.getId()+" Phase="+phase+" active phase="+activeVO.getPhase()+" Service="+xml.getServiceId()+" Port="+activeVO.getPort());
					if(phase.equals(activeVO.getPhase())) {
						if(activeVO.isNotChanged(xml, false)) {
							//System.out.println("Not changed");
							continue;
						} else {
							//logger.debug("Value Change "+vesVisit.getId()+" ETA="+vesVisit.getEta()+" ETD="+vesVisit.getEta()+" ATA="+vesVisit.getAta());
							logger.debug("ID="+vesVisit.getId()+" Phase="+phase+" active phase="+activeVO.getPhase()+" Service="+xml.getServiceId()+" nextPort="+xml.getNextFacility());

						}
					} else {
						logger.debug("APPEND="+phase);
						logger.debug("ID="+vesVisit.getId()+" Phase="+phase+" active phase="+activeVO.getPhase()+" Service="+xml.getServiceId()+" nextPort="+xml.getNextFacility());

						if(!"CANCELED".equals(activeVO.getPhase())) appendPhase(vesVisit,phase);
						if(phase.equals("ARRIVED") && xml.getATA() == null){
							xml.setATA(xml.getETA()); //Set ETA as ATA if portStatusCall != 'A' but Phase moved to Arrived to Avoid an Snx exception
						}
						if(phase.equals("WORKING")) xml.setTimeStartWork(xml.getATA());
						if(phase.equals("DEPARTED")) {
							xml.setTimeStartWork(xml.getATA());
							xml.setTimeEndWork(xml.getATD());
						}
					}
					if("CANCELED".equals(activeVO.getPhase())) {
						vesVisit.getXml().setVisitPhase(phase);
					} else {
						vesVisit.getXml().setVisitPhase(activeVO.getPhase());
					}
					if(YB && phase.equals("DEPARTED")) { //A15
						vesVisit.getXml().setVisitPhase(phase);
					}
					vesselVisitList.add(vesVisit);

				} else {
					vesVisit.setPhase("CREATED");
					vesVisit.getXml().setVisitPhase("CREATED");

					//A12 Has No Active Vessel Visit
					phase = "CREATED";
					//logger.debug("Set Phase :Created");
					//To Handle new Created units
					activeVO = new VesselVisitVO();
					activeVO.setPhase(null);

					// Seperate list of new visit, to give the canceled visits time to be canceled.
					newVesselVisitList.add(vesVisit);
				}

				//A12 - IF Phase Change Then Check if we need to Jump Phases (Append Multiple Phases to VV Notes and Handle it in Groovy code)
				boolean isActive = "CREATED".equals(phase)  || "INBOUND".equals(phase) || "ARRIVED".equals(phase);
				if(!phase.equals(activeVO.getPhase()) && isActive && !(YB && HRZ)){
					logger.debug("Jump Phase="+vesVisit.getId()+" ActiveVoPhase="+(activeVO !=null ? activeVO.getPhase(): "activeVO Not Initialized")+" Port="+vesVisit.getPort()+" IsActive="+isActive);
					setMultiplePhases(portCall,vesVisit,phase,activeVO);
				 }//outer If Ends

			}//While Ends
			// todo, add canceled visits.

			logger.debug(currentVisits.size()+" remaining/remain visits");
			addCanceledVisits();
			// A3 Add the new vessel visits at the end.
			vesselVisitList.addAll(newVesselVisitList);
			newVesselVisitList.clear();

			if ( vesVisit != null) {
				xmlTestFlag = null;
				sendAllVesVisit();
				vesselVisitList.clear();
				FileWriterUtil.writeFile(bufout.toString(), VesselScheduleLookup.storeDir,"VV_Output",".xml");
				bufout = null;
			}

			if(lookup != null) lookup.close();
		} catch ( Exception ex) {
			logger.error( "Error in processing schedule: ", ex);
			ex.printStackTrace();
		}finally{
			bufout = null;
		}
		logger.info("*************** END : VesScheduleFileProcessor.processSchedule() ***************");
	}

	/*
	 *A12 If we need to list iterative Phases in the same publish then
	 * Create Multiple vesselvisitVO Objects and String it together to create one Snx message
	 */
	private void setMultiplePhases(PortCallViewBean portCall, VesselVisitVO curVesVisit, String firstComputedPhase, VesselVisitVO activeVo) {

		logger.debug("CurrentVesVisit "+curVesVisit.getId()+" Port"+curVesVisit.getPort()+" activeVesVisitVo"+activeVo.getPhase());

		for(int i=0; i<4 ;i++){
			//1.Set currentVisit For First loop then newVesVisit for the Consecutive loops
			if(i == 0){
				activeVo.setPhase(firstComputedPhase);
			}
			String phase = getPhase(portCall,activeVo, false, false);

			logger.debug("counter="+i+" Break Incase Equal="+activeVo.getPhase()+" phase="+phase);
			//If Phase didn't change Break out of the loop
			if(activeVo.getPhase().equals(phase) || "COMPLETE".equals(phase) || "DEPARTED".equals(phase)){ break; }

			if(!activeVo.getPhase().equals(phase)) {
				//Append Computed Phase to VesVisit Notes
				if(!"CANCELED".equals(activeVo.getPhase())){
					String note = curVesVisit.getXml().getNotes();
					if(note == null || note.trim().length() == 0){
						appendPhase(curVesVisit,phase);
					}else{
					  String temp = note.substring(0,note.length()-1);
					  note =temp+","+VesselVisitVO.DBPhase(phase)+"'";
					  curVesVisit.getXml().setNotes(note);
					}
				}

				if(phase.equals("ARRIVED") && curVesVisit.getXml().getATA() == null ){
					curVesVisit.getXml().setATA(curVesVisit.getXml().getETA());
					//logger.debug("newVesVisit Arrived="+curVesVisit.getId()+" "+activeVo.getAta()+" "+activeVo.getPhase());
				}else if(phase.equals("WORKING")){
					if(curVesVisit.getXml().getATA() == null){
						curVesVisit.getXml().setATA(curVesVisit.getXml().getETA());
					}if(curVesVisit.getXml().getTimeStartWork() == null){
					    curVesVisit.getXml().setTimeStartWork(curVesVisit.getXml().getATA());
					}
					//logger.debug("newVesVisit Working="+curVesVisit.getId()+" "+activeVo.getAta()+" "+activeVo.getPhase());
				}
				//Set VO object to Current Phase for Next loop Iteration
				activeVo.setPhase(phase);
				//logger.debug("newVesVisit "+curVesVisit.getId()+" "+curVesVisit.getPort()+" "+activeVo.getPhase());
			} else {
				logger.debug("setMultiplePhases Vessel Visit Skip "+curVesVisit.getId()+" "+curVesVisit.getPort()+" "+activeVo.getPhase());
			}
		}
	}



	public TVesselVisit processPortCall(PortCallViewBean portCall) {
		TosLookup lookup = null;
		try {
			lookup = new TosLookup();

			TVesselVisit visit = new TVesselVisit();

			//System.out.println( portCall.getArrival().getStatus().getCode() + " "+portCall.getDeparture().getStatus().getCode() );

			if("A".equals(portCall.getArrival().getStatus().getCode())) {
				visit.setATA( CalendarUtil.getXmlCalendar(portCall.getArrival().getTime()));
			} else {
				visit.setETA( CalendarUtil.getXmlCalendar(portCall.getArrival().getTime()));
			}

			if("A".equals(portCall.getDeparture().getStatus().getCode())) {
				visit.setATD( CalendarUtil.getXmlCalendar(portCall.getDeparture().getTime()));
			} else {
				visit.setETD( CalendarUtil.getXmlCalendar(portCall.getDeparture().getTime()));
			}

			boolean isBarge = false;
			if(portCall.getArrival().getVoyages() != null && portCall.getArrival().getVoyages().size() > 0) {
				isBarge = "Barge".equals(portCall.getArrival().getVessel().getType().getDescription()) ||  "YBBarge".equals(portCall.getArrival().getVessel().getType().getDescription()) ? true : false;

				List voyages = portCall.getArrival().getVoyages();
				Voyage voyage = (Voyage)voyages.get(0);
				// todo, barge versus long haul
				// Find the right voyage, not just the first.
				if(isBarge) {
					//visit.setInVoyNbr(voyage.getVoyageID().numToString()+portCall.getArrival().getTrade().getCode());
					visit.setClassification("BARGE"); //A14
					visit.setFacility(portCall.getPort().getCode());
				}
				visit.setInVoyNbr(voyage.getVoyageID().numToString());
			}

			visit.setVesselId(portCall.getDeparture().getVessel().getCode());

			// Read from n4.
			visit.setOperatorId(getOperator(portCall.getDeparture().getVessel().getCode(), lookup));
			//System.out.println("Oper="+visit.getOperatorId());
			// todo, is this mapping ok?
			if("YBU".equals(visit.getOperatorId())) visit.setOperatorId("MAT");
			if("HZN".equals(visit.getOperatorId())) visit.setOperatorId("HRZ");

			//System.out.println("Barge="+portCall.getDeparture().getVessel().getType().getDescription());

			//System.out.println("Barge="+portCall.getPort().get);
			if(portCall.getDeparture().getVoyages() != null && portCall.getDeparture().getVoyages().size() > 0) {
				isBarge = "Barge".equals(portCall.getDeparture().getVessel().getType().getDescription()) ||  "YBBarge".equals(portCall.getDeparture().getVessel().getType().getDescription()) ? true : false;
				//System.out.println("isBarge="+isBarge);
				List voyages = portCall.getDeparture().getVoyages();
				Voyage voyage = (Voyage)voyages.get(0);
				if (isBarge) {
					visit.setFacility(portCall.getPort().getCode());
				}

				visit.setOutVoyNbr(voyage.getVoyageID().numToString());

				visit.setId(visit.getVesselId()+visit.getOutVoyNbr());
			} else {
				logger.error("No outbound voyage number, skipping");
				return null;
			}

			visit.setInCallNumber(BigInteger.ONE);
			visit.setOutCallNumber(BigInteger.ONE);
			visit.setIsCommonCarrier("Y");
			visit.setIsDrayOff("N");
			visit.setIsNoClientAccess("Y");

			// todo, setNotes, not used by notes

			String next = lookupPortRotation(portCall);
			setService(portCall, visit);
			// Use the last service if the new service can't be guessed.
			if(visit.getServiceId() == null) visit.setServiceId(prevService);
			else prevService = visit.getServiceId();

			if(next != null && JMSSender.isMappedPort(next) ) visit.setNextFacility(next);

			TVesselVisit.Lines lines = new TVesselVisit.Lines();
			visit.setLines(lines);

			// for MAE issue - begin
			VesselVisitVO temVesVisitVo = new VesselVisitVO();
			temVesVisitVo.setXml(visit);
			temVesVisitVo.setPort(portCall.getPort().getCode());
			temVesVisitVo.setId(visit.getId());
			logger.info("visit.getOperatorId() in processPortCall "+visit.getOperatorId()+" ---- "+temVesVisitVo.getPort()+"----"+temVesVisitVo.getId());
			if (!"MAT".equalsIgnoreCase(visit.getOperatorId()) &&  currentVisits.contains(temVesVisitVo)) {
				VesselVisitVO locVO = currentVisits.get(currentVisits.indexOf(temVesVisitVo));

				logger.info("getting lines for VVD "+locVO.getId());

				Map<String, String>  vvdLinesMap = lookup.getVVDLines(locVO.getId(),locVO.getPort());
				String ibOb = null;
				String lineIBOB[] = null;

				for(int i=0;vesselLines != null && i<vesselLines.length;i++) {
					TVesselVisit.Lines.Line aLine = new TVesselVisit.Lines.Line();
					logger.info("vesselLines[i]   "+vesselLines[i]+" IB :"+locVO.getIbVyg()+" OB: "+locVO.getObVyg());
					aLine.setId(vesselLines[i]);
					if (vvdLinesMap!=null && vvdLinesMap.size()>0) {
						ibOb = (String) vvdLinesMap.get(vesselLines[i]);
						if (ibOb!=null){
							lineIBOB = ibOb.split("-");
						}
					}

					if(lineIBOB!=null && lineIBOB.length>0){

					aLine.setInVoyNbr(lineIBOB[0]);
					aLine.setOutVoyNbr(lineIBOB[1]);
					}

					else{
						aLine.setInVoyNbr(visit.getInVoyNbr());
						aLine.setOutVoyNbr(visit.getOutVoyNbr());
					}
					lines.getLine().add(aLine);
				}
				logger.info("This is not a Matson vessel and not a new vessel visit hence skipping the update of line operators");
			} else {
				for(int i=0;vesselLines != null && i<vesselLines.length;i++) {
					TVesselVisit.Lines.Line aLine = new TVesselVisit.Lines.Line();
					logger.info("vesselLines[i]   "+vesselLines[i]);
					aLine.setId(vesselLines[i]);
					aLine.setInVoyNbr(visit.getInVoyNbr());
					aLine.setOutVoyNbr(visit.getOutVoyNbr());
					lines.getLine().add(aLine);
				}
			}
			// for MAE issue - end
			// todo, Berth
			TVesselVisit.Berths berths = new TVesselVisit.Berths();

			String berthStr = null;
			if(portCall.getBerth() != null) {
				berthStr = portCall.getBerth().getCode();
			}
			if(berthStr == null ) {
				if("HON".equals(portCall.getPort().getCode()) || "HNC".equals(portCall.getPort().getCode())  ) {
					TVesselVisit.Berths.Berthing berthing = new TVesselVisit.Berths.Berthing();
					berthing.setQuayId("52");
					berthing.setShipSideTo("STARBOARD");
					berths.getBerthing().add(berthing);
					visit.setBerths(berths);
				}
			} else {
				TVesselVisit.Berths.Berthing berthing = new TVesselVisit.Berths.Berthing();
				berthing.setQuayId(berthStr);
				if(berthStr.startsWith("29")) berthing.setShipSideTo("PORTSIDE");
				else berthing.setShipSideTo("STARBOARD");
				berths.getBerthing().add(berthing);
				visit.setBerths(berths);
			}



			return visit;
		} catch (Exception e) {
			logger.error("Unexpected exception in processPortCall",e);
			return null;
		} finally {
			if(lookup != null) lookup.close();
		}
	}

	/*
	 * Taken from CMIS code
	 *
case trade = "C" : serviceId.a    = "CLI"
                   itineraryId.a  = "CLI-STD"
case trade = "U" : serviceId.a    = "GCS"
                   itineraryId.a  = "GCS-STD"
case trade = "B" :
      case search("HIL", portRotation) <> 0 :
           serviceId.a = "BI"
           itineraryId.a = "BI-STD"
      case search("KHI", portRotation) <> 0 :
           serviceId.a = "BI"
           itineraryId.a = "BI-STD"
      case search("KAH", portRotation) <> 0 :
           serviceId.a = "KAH"
           itineraryId.a = "KAH-STD"
      case search("NAW", portRotation) <> 0 :
           serviceId.a   = "NAW"
           itineraryId.a = "NAW-STD"
case trade = "H" :
   switch
      case search("SEA", portRotation) <> 0 :      ; going to SEA in port rotation
           serviceId.a   = "PNW
           itineraryId.a = "PNW-STD"
      case search("HILLAX", portRotation) <> 0:    ;new
           serviceId.a   = "LHH"
           itineraryId.a = "LHH-STD"
      case search("KAHOAK", portRotation) <> 0:    ;new
           serviceId.a   = "OHK"
           itineraryId.a = "OHK-STD"
      case search("LAXOAK", portRotation) <> 0:
          serviceId.a   = "OLH"      ;was LOH
           itineraryId.a = "OLH-STD"
      case search("OAKLAX", portRotation) <> 0:
           serviceId.a   = "OLH"      ;was LOH
           itineraryId.a = "OLH-STD"
      case search("LAX", portRotation) <> 0:
           serviceId.a   = "LAX"
           itineraryId.a = "LAX-STD"
      case search("OAK", portRotation) <> 0:
           serviceId.a   = "OAK"
           itineraryId.a = "OAK-STD"
case trade = "S" :
   switch
      case search("GUM", portRotation) <> 0 :   ; going to GUM in port rotation
           serviceId.a   = "HRZ-GUM"
           itineraryId.a = "HRZ-GUM-STD"
      case search("LBC", portRotation) <> 0:    ; going to LBC in port rotation
           serviceId.a   = "HRZ-LBC"
           itineraryId.a = "HRZ-LBC-STD"
      case search("TCC", portRotation) <> 0:    ; going to TCC in port rotation
           serviceId.a   = "HRZ-PNW"
           itineraryId.a = "HRZ-PNW-STD"
	 */
	public void setService(PortCallViewBean portCall, TVesselVisit visit) {
		String trade = portCall.getDeparture().getTrade().getCode();
		if(trade == null) return;
		if("MAT".equalsIgnoreCase(visit.getOperatorId())){
			//if("A".equalsIgnoreCase(trade)){
				computeServiceAndItinerary(portRotation, visit);
			//}
		}

		/*if(trade.equals("A")) {
			if(portRotation.toString().contains("ANK") && portRotation.toString().contains("KDK")
					&& portRotation.toString().contains("DUT")) {
				visit.setServiceId("AKS");
				visit.setItineraryId("ALASKA-STD");
			} else if(portRotation.toString().contains("ANK") && portRotation.toString().contains("KDK")) {
				visit.setServiceId("ANK-KDK-TAC");
				visit.setItineraryId("ANK-KDK-TAC-STD");
			} else if (portRotation.toString().contains("KQA")) {
				visit.setServiceId("DUT-KQA-DUT");
				visit.setItineraryId("DUT-KQA-DUT-STD");
			}
		}*/
		logger.info("visit.getOperatorId() in setService "+visit.getOperatorId());
		/*if ("MAE".equalsIgnoreCase(visit.getOperatorId())){
			 visit.setServiceId("MAERSK");
	         visit.setItineraryId("MAERSK-STD");
		}*/
		if (visit.getOperatorId() != null && !"MAT".equalsIgnoreCase(visit.getOperatorId()) && isConfiguredService(visit.getOperatorId())) {
			Map<java.lang.String, java.lang.String[]> vesselServicesMap = getVesselServicesMap();
			if (!vesselServicesMap.isEmpty() && visit.getOperatorId() != null) {
				visit.setServiceId(getConfiguredServiceId(visit.getOperatorId(), vesselServicesMap));
				visit.setItineraryId(getConfiguredItineraryId(visit.getOperatorId(), vesselServicesMap));
			}
		}

	}

	private Map<java.lang.String, java.lang.String[]> getVesselServicesMap() {
		java.lang.String operatorKey = TosRefDataUtil.getValue("N4_CONFIG_VES_SRV");
		logger.info("Picking the keys for determining the services and itinerary " + operatorKey);
		if (operatorKey != null) {
			java.lang.String[] operatorKeys = operatorKey.split("\\,");
			Map<String, String[]> returnMap = new HashMap<String, String[]>();
			for (java.lang.String s : operatorKeys) {
				java.lang.String vesselServiceRef = TosRefDataUtil.getValue(s);
				logger.debug("the returned values for key " + s + " is " + vesselServiceRef);
				if (vesselServiceRef != null) {
					String[] references = vesselServiceRef.split("\\,");
					if (references.length >= 3) {
						returnMap.put(references[0], Arrays.copyOfRange(references, 1, references.length));
					}
				}
			}
			if (!returnMap.isEmpty()) {
				for (String key : returnMap.keySet()) {
					logger.debug(key + "	" + Arrays.toString(returnMap.get(key)));
				}
				return returnMap;
			}
		}
		return Collections.EMPTY_MAP;
	}

	private java.lang.String getConfiguredItineraryId(String operatorId, Map<String, String[]> vesselServicesMap) {
		String[] strings = vesselServicesMap.get(operatorId);
		logger.info("Itinerary id is "+strings[1]);
		return strings[1];
	}

	private java.lang.String getConfiguredServiceId(String operatorId, Map<String, String[]> vesselServicesMap) {
		String[] strings = vesselServicesMap.get(operatorId);
		logger.info("Service id is "+strings[0]);
		return strings[0];
	}

	private boolean isConfiguredService(java.lang.String operatorId) {
		if (operatorId != null) {
		java.lang.String vesselService = TosRefDataUtil.getValue("N4_CONFIG_VES_LST");
		logger.info("Checking for operator " + operatorId + " availability against property N4_CONFIG_VES_LST" + vesselService);
			if (vesselService != null) {
				java.lang.String[] vesselServices = vesselService.split("\\,");
				for (java.lang.String s : vesselServices) {
					if (operatorId.equalsIgnoreCase(s)) {
						logger.info("The vessel operator " + operatorId + "is available in the list");
						return Boolean.TRUE;
					}
				}
			}
		}
		logger.info("The vessel operator " + operatorId + "is not available in the list");
		return Boolean.FALSE;
	}

	private void computeServiceAndItinerary(StringBuffer portRotation, TVesselVisit visit) {
		String allServicesKeys = TosRefDataUtil.getValue("ALL_SRV_KEYS");
		Boolean isServiceSet = Boolean.FALSE;
		if(allServicesKeys!=null){
			String[] services = allServicesKeys.split("\\,");
			logger.debug("Services to match up with \t"+Arrays.toString(services));
			for(String s:services){
				logger.debug("Matching up with service "+s);
				String serviceItineraryPorts = TosRefDataUtil.getValue(s);
				if (serviceItineraryPorts != null) {
				logger.debug("The refValue from Database is "+serviceItineraryPorts);
				String serviceId = getServiceId(serviceItineraryPorts);
				logger.debug("Service ID "+serviceId);
				String itineraryId = getItineraryId(serviceItineraryPorts);
				logger.debug("Itinerary ID "+itineraryId);
				String[] ports = getPortRotations(serviceItineraryPorts);
				logger.debug("Port in Itinerary "+Arrays.toString(ports));
				if (doesPortRotationHaveAllPorts(portRotation, ports) && !isServiceSet) {
					visit.setItineraryId(itineraryId);
					visit.setServiceId(serviceId);
					logger.info("For Vessel Visit "+visit.getId()+" the Service Id is "+serviceId+" Itinerary Id is "+itineraryId);
					isServiceSet = Boolean.TRUE;
				}
				}
			}
		}
		if (!isServiceSet) {
			logger.error("For Vessel visit " + visit.getId() + " unable to set Service and Itinerary");
			logger.error("There is no matching service configured for port rotation " + portRotation.toString());
			String emailAddr = TosRefDataUtil.getValue("SUPPORT_EMAIL");
			EmailSender.sendMail(emailAddr, emailAddr, "Vessel Schedule Processing Error for VesselVisit " + visit.getId(),
					"For Vessel visit " + visit.getId() + " unable to set Service and Itinerary" +
							"\nThere is no matching service configured for port rotation " + portRotation.toString());
		}

	}

	private String[] getPortRotations(String serviceItineraryPorts) {
		String[] strings = serviceItineraryPorts.split("\\,");
		return Arrays.copyOfRange(strings,2,strings.length);
	}

	private String getItineraryId(String serviceItineraryPorts) {
		String[] strings = serviceItineraryPorts.split("\\,");
		return strings[1];
	}

	private String getServiceId(String serviceItineraryPorts) {
		String[] strings = serviceItineraryPorts.split("\\,");
		return strings[0];
	}

	private boolean doesPortRotationHaveAllPorts(StringBuffer portRotation, String[] s) {
		for(String s1: s){
			if(!portRotation.toString().contains(s1)){
				return false;
			}
		}
		return true;
	}

	/** Phase logic
	 *          if arrTypeAct  = "A" then
            if isBlank(strVal(depDateAct)) then
               [visit-Phase]   =  "WORKING"
            else
               if depDateAct <= today() - 4 then   ; set to closed 4 days after actual departure
                  [visit-Phase]   =  "CLOSED"      ; date 4/21/09 km
               else
                  [visit-Phase]   =  "DEPARTED"
               endif
            endif
         else
            if isBlank(strVal(arrDate)) then
               [visit-Phase]   =  "CREATED"
            endif
            if trade = "B" then
               createdDays.n = 21
            else
               createdDays.n = 47
            endif
            if (arrDate - today()) > createdDays.n then   ; was 21 7/14/09 km
               [visit-Phase]   =  "CREATED"
            else
               [visit-Phase]   =  inboundOrWorking()
            endif
         endif

         proc inBoundOrWorking()
      private phase.a

   if arrDate <= today() then
      return "WORKING"
   endif

   ;...get today's minutes left to midnight
   tmin = numVal(substr(time(),1,2))*60 + numVal(substr(time(),4,2))
   minLeftToday = 1440 - tmin

   ;...get ETA for arrival day in minutes.
   etaDate  =  arrDate
   tm       =  arrTime
   etaTime  =  numVal(substr(tm,1,2))*60 + numVal(substr(tm,4,2))

   wkg   =  minleftToday + etaTime

   ;...add full minutes (1440) in a day for any days in between
   wholeDays = (etaDate - today()) - 1
   if wholeDays >= 1 then
      wkg   =  wkg + (wholeDays * 1440)
   endif

   if trade =  "B" then
      if wkg <= fssWORKINGBarge then
         phase.a   = "ARRIVED"   ;"WORKING"
      else
         phase.a   = "INBOUND"
      endif
   else
      if wkg <= fssWORKINGLong then
         phase.a   = "ARRIVED"   ;"WORKING"
      else
         phase.a   = "INBOUND"
      endif
   endif

   return phase.a
	 */

	/* New Alogrithm
	 * If it does not exist:
    status = Created

If Canceled
    status = Created

If Created
    if(eta == null) or eta > now + inbound_time // indoun_time 21 days for barges, 47 otherwise
    	status = Created
    status = inbound
If Status = Inbound
    if(eta <= today - 1)
    	Status = ARRIVED
    else
        Status = Inbound

If Status = Arrived
    if(ata <= today)
    	Status = WORKING
    else
    	Status = Arrived

If Status = Working
   if(!YB or HZ) status = WORKING
   if(ATD <= Today)
        Status = Departed
   else Status = WORKING

If Status = Complete
   Status = Complete

If Status = Departed
    if(ATD <= today - offset)
        Status = CLOSED
    else
       Status = DEPARTED

If Status = CLOSED
   Status = CLOSED

If it is not in the schedule
   If Status = CLOSED, Departed ot Complete
   	No Update
   Else
   	Status = canceled
	 */
	private String getPhase(PortCallViewBean portCall, VesselVisitVO activeVO, boolean YB, boolean HRZ) {
		String currentPhase = activeVO.getPhase();
		boolean isBarge =  "Barge".equals(portCall.getDeparture().getVessel().getType().getDescription()) ? true : false;

		if(currentPhase == null || currentPhase.equals("CANCELED")) return "CREATED";
		if(activeVO.isArchived()) return currentPhase;

		Date arrival = portCall.getArrival().getTime();
		if(arrival == null) return currentPhase;
		if(currentPhase.equals("CREATED")) {
			//System.out.println("Days ID="+activeVO.getId());
			int offset;
			if(isBarge) offset = 21;
			else offset = 47;
			if(before(arrival,offset, YB)) return "INBOUND"; //A15
			return "CREATED";
		}
		if(currentPhase.equals("INBOUND")) {//A12
			if(before(arrival,1, YB)) return "ARRIVED";//A15
			return "INBOUND";
		}
		if(currentPhase.equals("ARRIVED")) {
			if(before(arrival,1, YB)) { //A12//A15
				return "WORKING";
			}
			return "ARRIVED";
		}
		Date departure = portCall.getDeparture().getTime();
		if(departure == null) return currentPhase;

		if(currentPhase.equals("WORKING")) {
			//System.out.println("YB= "+YB+" HRZ="+HRZ+" managed="+activeVO.isManagedPort()+" type="+("A".equals(portCall.getDeparture().getStatus().getCode()))+" '"+portCall.getDeparture().getStatus()+"' date="+departure);
			// Is YB, or Is an actual call and HRZ or any line in a managed port.
			if(YB) {
				if(before(departure,0, YB)) {//A15
					return "DEPARTED";
				}
			} else if(HRZ  && "A".equals(portCall.getDeparture().getStatus().getCode()) ) {
				if(before(departure,0, YB)) {//A15
					return "DEPARTED";
				}
			} else if (activeVO.isManagedPort() && "A".equals(portCall.getDeparture().getStatus().getCode())) {
				if(before(departure,0, YB)) {//A15
					return "DEPARTED";
				}
			}

			return "WORKING";

		}

		if(currentPhase.equals("COMPLETE")) {
			return "COMPLETE";
		}

		if(currentPhase.equals("DEPARTED")) {
			int delay = TosRefDataUtil.getClosedTime(activeVO.getId());
			if(before(departure, delay, YB)) return "CLOSED";//A15
			return "DEPARTED";
		}

		if(currentPhase.equals("CLOSED")) {
			return "CLOSED";
		}


		return currentPhase;

	}



	private String lookupPortRotation(PortCallViewBean portCall) {
		String newVessel = portCall.getDeparture().getVessel().getCode();
		String port = portCall.getPort().getCode();
		String trade = portCall.getDeparture().getTrade().getCode();
		String chkport;
		String locCode = null;

		boolean isFirst = !newVessel.equals(currentVessel);
		// Clear out previous service as out service of last resort.
		if(isFirst) prevService = null;
		currentVessel = newVessel;

		if("S".equals(trade)) {
			chkport = "HNC";
		} else {
			chkport = "HON";
		}

		String prevPortRotation = portRotation.toString();
		if("HON".equals(port) || port.equals(chkport) || isFirst) {
			portRotation.setLength(0);
			PortCallViewBean nextPort = portCall;
			if(!port.equals("HON") && !port.equals(chkport) ) portRotation.append(port);
			while( (nextPort = nextPort.getNext()) != null) {
				String nextPortString = nextPort.getPort().getCode();
				if(nextPortString.equals("HON") || nextPortString.equals(chkport) ) break;
				if(!newVessel.equals(nextPort.getDeparture().getVessel().getCode())) break;
				portRotation.append(nextPortString);
			}
		}
		if(portRotation.length() == 0) portRotation.append(prevPortRotation);

		//System.out.println("Vessel="+newVessel+" Port location="+portRotation+" port="+port+" Trade="+trade+" In="+((Voyage)portCall.getArrival().getVoyages().get(0)).getVoyageID()+" Out="+((Voyage)portCall.getDeparture().getVoyages().get(0)).getVoyageID()+" type="+portCall.getDeparture().getStatus());

		PortCallViewBean nextPort = portCall.getNext();

		if(nextPort != null && nextPort.getDeparture() != null && newVessel.equals(nextPort.getDeparture().getVessel().getCode())) return nextPort.getPort().getCode();
		return null;


	}


	public boolean before(Date date, int days, boolean YB) {
		if(YB) { //A15
			Boolean isDayLightSaving = false;
			int offset = 0;
			// Get HST date
			Calendar pstCal = Calendar.getInstance();
			Date pstDate = pstCal.getTime();
			pstDate.setDate(pstDate.getDate() + days);
			Date hstDate = null;
			isDayLightSaving = pstCal.getTimeZone().inDaylightTime(pstDate);
			offset = isDayLightSaving?-3:-2;
			pstCal.add(Calendar.HOUR, offset);
			hstDate = pstCal.getTime();
			logger.debug("HST DATE : "+hstDate);
			// Convert departure date into HST for comparison
			logger.debug("DEP DATE : "+date);
			/*Calendar depCal = Calendar.getInstance();
			depCal.setTime(date);
			isDayLightSaving = depCal.getTimeZone().inDaylightTime(date);
			offset = isDayLightSaving?-3:-2;
			depCal.add(Calendar.HOUR, offset);
			date = depCal.getTime();
			logger.debug("CONV DEP DATE : "+date);*/
			if(date.before(hstDate) || date.equals(hstDate)){
				return true;
			}
		}
		else {
			Date now = new Date();
			long nowLong = now.getTime() + (long)days*(long)(24*3600*1000);
			long thenLong = date.getTime();
			if(thenLong <= nowLong) return true;
		}
		return false;
	}

	public String getOperator(String vessel, TosLookup lookup) {
		try {
			//if(lookup == null) lookup = new TosLookup();
			return lookup.getVesselOperator(vessel);
		} catch (Exception e) {
			logger.error("Could not get operator", e);
			return null;
		}
	}


}
