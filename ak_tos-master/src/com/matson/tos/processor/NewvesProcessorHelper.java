package com.matson.tos.processor;

import com.matson.cas.erd.service.data.VesselVO;
import com.matson.cas.refdata.RefDataLookup;
import com.matson.cas.refdata.mapping.*;
import com.matson.cas.service.ftp.FtpBizException;
import com.matson.cas.service.ftp.FtpProxyListBiz;
import com.matson.tos.dao.NewVesselDao;
import com.matson.tos.dao.TosLookup;
import com.matson.tos.exception.NewVesselLogger;
import com.matson.tos.jatb.NewVes;
import com.matson.tos.jaxb.snx.Snx;
import com.matson.tos.jaxb.snx.TUnit;
import com.matson.tos.reports.NewvesReport;
import com.matson.tos.util.CalendarUtil;
import com.matson.tos.util.TosRefDataUtil;
import com.matson.tos.vo.SupplementProblems;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;



/* This is a helper class which will have all the object population rules for each
 * of the input files received..
 *
 * 	Srno	Date			AuthorName				Change Description
 * 	A1		09/26/2012		Raghu Pattangi			Initial creation
 * 	A2		10/05/2012		Karthik Rajendran		Sample NV text file generation from RDS only
 * 	A3		10/08/2012		Karthik Rajendran		Creating HAZARD record on the output from DCM data
 *  A4		10/10/2012		Karthik Rajendran		Changed all methods parameter from RDS table to RDS final table.
 *  A5		10/12/2012		Karthik Rajendran		Hold record for primary vessel creation, oversize record creation
 *  												Carrier record creation, 19 out of 24 issues solved.
 *  A6      11/16/2012      Meena Kumari            Changed the startSupplementalVesselProc() to call newly added getAllRequiredDataForVesVoySupplement().
 *  A7		11/30/2012		Karthik Rajendran		Changed: Supplemental output process
 *  A8		12/13/2012		Karthik Rajendran		Cahnged: TransitState shout be set for ROB containers
 *  														Set drayStatus = "DRAYIN" for CSX vessels
 *  														Supplemental output file name change
 *  A9		12/27/2012		Karthik Rajendran		Changed: CreateHazardData - FlashPoint calculation.
 *  												Added: Copying output files to respective FTP locations upon user selection from TosAppParameter.
 *  A10		12/28/2012		Karthik Rajendran		Changed: Setting hazardous flag by checking both DCM and HZ from stowplan data
 *  A11		01/03/2013		Karthik Rajendran		Changed: createCarrierData()
 *  A12		01/10/2013		Karthik Rajendran		Changed: "reefer" element - temperature conversion and setting temp unit
 *  A13		01/19/2013		Karthik Rajendran		Changed: Container number prefix correction
 *  A14		01/22/2013		Karthik Rajendran		Changed: For Barge output:
 *  															Position-slot
 *																Routing-POD1
 *																Routing-Destination
 *																Equipment � eq-flex-01
 *																Routing-POL
 *	A15		01/27/2013		Karthik Rajendran		Changed: Logic to populate hazf flag and calling create hazard records in output
 *	A16		02/04/2013		Karthik Rajendran		Changed: POD-2 is not set for Barge
 *	A17		02/05/2013		Karthik Rajendran		Changed: Copying output from VES_FILES_FTP_ID to NV_VES_FILES_FTP_ID
 *	A18		02/13/2013		Karthik Rajendran		Added: Invoking DownloadDiscrepanciesReports.(Which is a combined report)
 *	A19		02/13/2013		Karthik Rajendran		Changed: createHandlingData() - For Barge Remarks is updated with stow plan Haz information,
 *																		not the DCM information
 *	A20		02/21/2013		Karthik Rajendran		Added: In case of missing strength code, adding default code "A"
 *															Barge - InboundTypecodeSummary report calling.
 *	A21		03/04/2013		Karthik Rajendran		Added: Calling CyLines Report or CyDiscSum report
 *	A22		03/08/2013		Karthik Rajendran		Added: TosLookup added to get IB carrier id,location for Barge.
 *	A23		03/09/2013		Karthik Rajendran		Added: Designated-trucker set only for HON or NIS Port containers
 *	A24		03/29/2013		Karthik Rajendran		Added: null check for vesselAvailableDate
 *	A25		04/11/2013		Karthik Rajendran		Added: Do not copy long haul NV.txt file to other ftp locations except the RDS archive location.
 *													Added: AG Ctrs not flagged AG report
 *	A26		04/15/2013		Karthik Rajendran		Changed: Haz remarks check for MARINE ,POLLUTANT, LTD.  QTY. and LTD, QTY.
 *	A27		04/17/2013		Karthik Rajendran		Changed: Do not generate "hazard" record from OHZ data for non-csx vessels.
 *													Added: Display comments in the "remarks" for supplemental proc holds updates.
 *	A28		04/23/2013		Karthik Rajendran		Changed: Success email notification for primary & supplemental.
 *	A29		05/09/2013		Karthik Rajendran		Added: Setting unitFlex7(NIS_TRUCKERS) for NIS containers and cargoNotes contains N/P
 *	A30		08/28/2013		Karthik Rajendran		Added: Checking discharge port instead dport against GCS ports
 *  A31		10/15/2013		Karthik Rajendran		Added: Get trucking company from TOS for the unit during supplemental trucker population.
 *  A32		10/24/2013		Karthik Rajendran		Added: Get OB actual carrier id & mode from TOS for supplemental units.
 *  A33		10/28/2013		Karthik Rajendran		Added: Set TRANSIT STATE FROM TOS for supplemental unit.
 *  A34		10/28/2013		Karthik Rajendran		Added: If outbound BargeCarrierID is null then process should not continue, generate email alert.
 *  A35		10/29/2013		Karthik Rajendran		Changed: A32 implementation has been changed, Set IB Actual/Declared, OB Actual/Declared from TOS 
 *  A36		01/22/2014		Karthik Rajendran		Changed: Consignee substring increased from 32 to 35 to match with CMIS(-Testing vessel MNA284 - MIX 833D US ARMY TRANSPORTATION BAT)
 *  A37		06/05/2014		Karthik Rajendran		Fix : EP000205387	SUPP DAY 2: IMPORT to THROUGH  (on a GCS vessel)
 *  												(Update category & OB carrier details during Import to Through supplemental update)
 *  A38		07/02/2014		Karthik Rajendran		Added: Invoke RORO report
 */


public class NewvesProcessorHelper {

    private static final String MULTISTOP_SIT = "MULTISTOP SIT";
    private static final String MULTISTOP_CFS_SIT = "MULTISTOP CFS SIT";
    private static Logger logger = Logger.getLogger(NewvesProcessorHelper.class);
    public ArrayList<TosRdsDataFinalMt> rdsDataFinal;
    public ArrayList<TosRdsDataFinalMt> robRdsDataFinal;
    public ArrayList<TosDcmMt> dcmData;
    public ArrayList<TosStowPlanCntrMt> stowCntrData;
    public ArrayList<TosStowPlanChassisMt> stowChassisData;
    public ArrayList<TosStowPlanChassisMt> bareChassisData;
    private ArrayList<String> outputText;
    private String outFilePath = System.getProperty("java.io.tmpdir");
    private static String outFileName = "";
    public static final String carrierId = "GEN_TRUCK";
    public static final String CNTR = "cntr";
    public static final String ROB = "rob";
    public static final String BARE = "bare";
    public String currentVesvoyType = "";
    public static final String PRIMARY = "primary";
    public static final String SUPPLEMENT = "supplement";
    public String obVesvoy = "";
    public String vesselAvailableDate = "";
    public Date vesselArrivalDate = null;
    public String vesselOpr = "";
    public Date triggerDate = null;
    public String processType = "";
    private NewVesselLogger nvLogger = NewVesselLogger.getInstance();
    ;
    public static String IS_PRIMARY = "IS_PRIMARY";
    public static String IS_SUPPLEMENTAL = "IS_SUPPLEMENTAL";
    public static String IS_BARGE = "IS_BARGE";
    public String COPY_PRIMARY = "false";
    public String COPY_SUPPLEMENT = "false";
    public String COPY_BARGE = "false";
    //public static String VES_FILES_FTP_ID = "VES_FILES_FTP_ID";
    public static String NV_VES_FILES_FTP_ID = "NV_VES_FILES_FTP_ID";
    public static String SUP_FILES_FTP_ID = "SUP_FILES_FTP_ID";
    private static TosLookup tosLookUp = null;
    private static String bargeCarrierId = null;
    private static String ANK = "ANK";
    private static String KDK = "KDK";
    private static String DUT = "DUT";
    private String supCntrCategory = null; //A37

    public NewvesProcessorHelper() {
        // TODO Auto-generated constructor stub

    }

    public void startNewVessProc(ArrayList<String> primaryVesselVoyageList, Date triggerDate) {
        logger.info("#### Start generating NV.TXT ####");
        this.triggerDate = triggerDate;
        startPrimaryVesselProc(primaryVesselVoyageList);
    }

    public void startPrimaryVesselProc(ArrayList<String> primaryVesselVoyageList) {
        for (int i = 0; i < primaryVesselVoyageList.size(); i++) {
            String vvd = primaryVesselVoyageList.get(i);
            if (vvd != null) {
                logger.info("Begin: Generating NV.TXT for " + vvd);
                String vesvoy = vvd.substring(0, 6);
                getAllRequiredDataForVesVoy(vesvoy);
                obVesvoy = vesvoy;
                outputText = new ArrayList<String>();
                if (outFilePath != null && !outFilePath.endsWith("/")) {
                    outFilePath = outFilePath + "/";
                }
                outFileName = outFilePath + vesvoy + "NV.TXT";
                currentVesvoyType = PRIMARY;
                processType = "LH";
                boolean outnew = createNewVesselTextFile(vvd, outFileName, "");
                if (outnew) {
                    //generateNewVesReports(vesvoy);
                    NewvesReport.createReport("DownloadDiscrepanciesReports", vesvoy, null, "PREVIEW - ");
                    // Consignee - notes report
                    logger.info("calling consineeshippernotes report");
                    NewvesReport.createReport("ConsigneeShipperNotesReport", vesvoy, null, null);
                    nvLogger.sendNewVessSuccess(vvd, "L", "false");
                }
                logger.info("End: Generating NV.TXT for " + vvd);
            }
        }
        if (tosLookUp != null) {
            tosLookUp.close();
            tosLookUp = null;
        }
    }

    public void startSupplementalProc(ArrayList<TosRdsDataFinalMt> updatedSupDataList, ArrayList<SupplementProblems> problems) {
        logger.info("Creating Supplemental Output test - Begin");
        Date today = new Date();
        int fil = 1;
        String supCount = "";
        supCount = "0" + fil;
        String mmdd = new SimpleDateFormat("MMdd").format(today);
        outFileName = mmdd + supCount + "SP.TXT";
        String newFileName = outFileName;
        try {
            int ftpProxyId = Integer.parseInt(TosRefDataUtil
                    .getValue("RDS_ARCH_FTP_ID"));
            int timeout = Integer.parseInt(TosRefDataUtil
                    .getValue("FTP_TIMEOUT"));
            logger.debug("FTP timeout retrieved is: " + timeout);
            FtpProxyListBiz list = new FtpProxyListBiz();
            list.setTimeout(timeout);
            String[] supFiles = list.getFileNames(ftpProxyId, null, null);
            ArrayList<String> supFilesList = new ArrayList<String>(Arrays.asList(supFiles));
            boolean fileExists = true;
            while (fileExists) {
                if (supFilesList.contains(newFileName)) {
                    fil++;
                    supCount = "" + fil;
                    if (supCount.length() == 1)
                        supCount = "0" + supCount;
                    newFileName = mmdd + supCount + "SP.TXT";
                    fileExists = true;
                } else {
                    fileExists = false;
                }
            }
            outFileName = newFileName;
        } catch (FtpBizException ftpEx) {
            logger.error("FTP error found: ", ftpEx);
        } catch (Exception e) {
            logger.error("Exception: ", e);
            e.printStackTrace();
        }
        if (outFilePath != null && !outFilePath.endsWith("/")) {
            outFilePath = outFilePath + "/";
        }
        outFileName = outFilePath + outFileName;
        outputText = new ArrayList<String>();
        currentVesvoyType = SUPPLEMENT;
        processType = "LH";
        rdsDataFinal = updatedSupDataList;
        robRdsDataFinal = new ArrayList<TosRdsDataFinalMt>();
        stowCntrData = new ArrayList<TosStowPlanCntrMt>();
        stowChassisData = new ArrayList<TosStowPlanChassisMt>();
        bareChassisData = new ArrayList<TosStowPlanChassisMt>();
        dcmData = new ArrayList<TosDcmMt>();
        boolean outSupp = createNewVesselTextFile("", outFileName, "");
        if (outSupp) {
            writeToUnitTextFile(outputText, outFileName);
            logger.info("Creating Supplemental Output - End");
            generateSupplementalReports(problems);
            nvLogger.sendSupplementalSuccess("" + rdsDataFinal.size(), newFileName, COPY_SUPPLEMENT);
        }
        if (tosLookUp != null) {
            tosLookUp.close();
            tosLookUp = null;
        }
    }

    public void startBargeProc(String vesvoy, String leg, Date trigD) {
        //String vesvoy = vvd.substring(0, 6);
        this.processType = "Barge";
        triggerDate = trigD;
        stowCntrData = NewVesselDao.getOCRDataForVesvoy(vesvoy, leg, trigD);
        this.rdsDataFinal = CommonBusinessProcessor.transformBargeDataIntoRDSFinal(stowCntrData);
        stowChassisData = NewVesselDao.getOCHDataForVesvoy(vesvoy, triggerDate);
        bareChassisData = NewVesselDao.getBareOCHDataForVesvoy(vesvoy, triggerDate);
        dcmData = NewVesselDao.getDcmDataForVesvoy(vesvoy + leg);
        outputText = new ArrayList<String>();
        if (outFilePath != null && !outFilePath.endsWith("/")) {
            outFilePath = outFilePath + "/";
        }
        outFileName = outFilePath + vesvoy.substring(0, 1) + vesvoy.substring(3, 6) + leg + "_NI.TXT";
        currentVesvoyType = PRIMARY;
        VesselVO vvo = CommonBusinessProcessor.getVesselDetails(vesvoy.substring(0, 3));
        vesselOpr = vvo.getVessOpr();
        boolean out = createNewVesselTextFile(vesvoy, outFileName, leg);
        if (out) { //A34 If bargeCarrierId is null then do not generate output TXT and Report as well
            writeToUnitTextFile(outputText, outFileName);
            //logger.info("Barge processed: " + vesvoy);
            generateBargeReports(vesvoy + leg);
            nvLogger.sendNewVessSuccess(vesvoy + leg, "B", COPY_BARGE);
        }
        if (tosLookUp != null) {
            tosLookUp.close();
            tosLookUp = null;
            bargeCarrierId = null;
        }
    }

    public void generateNewVesReports(String vesvoy) {
        try {
            logger.info("Generate New Vessel Report....");
            NewvesReport.createReport("MultiContainerCellsReport", vesvoy, null, null);
            //NewvesReport.createReport("DuplicateContainerReport",vesvoy, null,null);
            NewvesReport.createReport("ReeferForFandMContainersReport", vesvoy, null, null);
            //NewvesReport.createReport("SFTagReport",vesvoy,null,null);
            NewvesReport.createReport("DamageCodeReport", vesvoy, null, null);
            NewvesReport.createReport("ProduceReport", vesvoy, null, null);
            NewvesReport.createReport("ParadiseBeveragesContainersReport", vesvoy, null, null);
            NewvesReport.createReport("TagConsigneeCallSheetReport", vesvoy, null, null);
            NewvesReport.createReport("DPortChangesReport", vesvoy, null, null);
            NewvesReport.createReport("CustomsReport", vesvoy, null, null);
            NewvesReport.createReport("AGContainerInspectionsReport", vesvoy, null, null);
            NewvesReport.createReport("AGCtrsNotFlaggedReport", vesvoy, null, null);
            NewvesReport.createReport("HoldsReport", vesvoy, null, null);
            NewvesReport.createReport("InvalidNotifyParty", vesvoy, null, null);
            //NewvesReport.createReport("MISReeferReport",vesvoy,null,null);
            //downloadDiscrepanciesReports(vesvoy, "");
            NewvesReport.createReport("DownloadDiscrepanciesReports", vesvoy, null, "");
            //NewvesReport.createReport("CntrsStowedToRoRoPositionsReport",vesvoy, null,""); //A38
        } catch (Exception e) {
            logger.error("Error in creating reports for " + vesvoy);
        }
    }

    private void generateBargeReports(String vvd) {
        try {
            logger.info("Generate New Vessel Report....");
            NewvesReport.createReport("InboundTypecodeSummary", vvd, null, null);
            NewvesReport.createReport("MTYContainerSegregationReport", vvd, null, null);
            NewvesReport.createReport("EBSITReport", vvd, null, null);
            NewvesReport.createReport("HonoAutoCntrOnVVReport", vvd, null, null);
            NewvesReport.createReport("ReeferContainersBargeReport", vvd, null, null);
            if ("ALE".equalsIgnoreCase(vvd.substring(0, 3))) {
                NewvesReport.createReport("CntrDischByCellReport", vvd, null, null);
            }
            NewvesReport.createReport("CyDiscSumReport", vvd, null, null);
            NewvesReport.createReport("BargeStowageSummaryReport", vvd, null, null);
            NewvesReport.createReport("ModifiedFlatracksReport", vvd, null, null);
            NewvesReport.createReport("CyHonContainerReport", vvd, null, null);
            NewvesReport.createReport("MatuContainerForGuamFeReport", vvd, null, null);
            NewvesReport.createReport("DamageBargeReport", vvd, null, null);
            NewvesReport.createReport("ClientContainerBargeReport", vvd, null, null);
            NewvesReport.createReport("NLTDiscrepanciesReport", vvd, null, null);
            NewvesReport.createReport("MultiCellContainerBargeReport", vvd, null, null);
            NewvesReport.createReport("NLTDiscrepanciesBargeErrorReport", vvd, null, null);
        } catch (Exception e) {
            logger.error("Error in creating reports for " + vvd);
        }
    }

    public void generateSupplementalReports(ArrayList<SupplementProblems> problems) {
        try {
            logger.info("Generate Supplemental Problems Report....");

            NewvesReport.createReport("SupplementalProblems", null, problems, null);

        } catch (Exception e) {
            logger.error("Error in creating reports for supplemental containers");
        }
    }

    private void getAllRequiredDataForVesVoy(String vesvoy) {
        try {
            /*if("Barge".equalsIgnoreCase(processType)){
                stowCntrData = NewVesselDao.getOCRDataForVesvoy(vesvoy);
				//rdsDataFinal = CommonBusinessProcessor.transformBargeDataIntoRDSFinal(stowCntrData);
				stowChassisData = NewVesselDao.getOCHDataForVesvoy(vesvoy);
				robRdsDataFinal = new ArrayList<TosRdsDataFinalMt>();
				bareChassisData = NewVesselDao.getBareOCHDataForVesvoy(vesvoy);
				dcmData = new ArrayList<TosDcmMt>();

			}else{*/
            rdsDataFinal = NewVesselDao.getRdsDataFinalForVesvoy(vesvoy, triggerDate);
            robRdsDataFinal = NewVesselDao.getRobRdsDataFinalForVesvoy(vesvoy, triggerDate);
            stowCntrData = NewVesselDao.getOCRDataForVesvoy(vesvoy, null, null);
            stowChassisData = NewVesselDao.getOCHDataForVesvoy(vesvoy, null);
            bareChassisData = NewVesselDao.getBareOCHDataForVesvoy(vesvoy, null);
            dcmData = NewVesselDao.getDcmDataForVesvoy(vesvoy);

            //}
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error : Unable to get RDS/ROB/STOW CNTR/CHASSIS/DCM for " + vesvoy);
        }
    }

    private boolean createNewVesselTextFile(String vesvoy, String fileName, String leg) {
        try {
            logger.debug("entered createNewVesselTextFile - vesvoy value:" + vesvoy);
            if (!"Barge".equalsIgnoreCase(processType)) {
                if (currentVesvoyType.equals(PRIMARY)) {
                    try {
                        createVesselVisitData(vesvoy);
                    } catch (Exception e) {
                        nvLogger.addError(vesvoy, "", "Vessel available date is missing.");
                        return false;//If vessel available date is null then no output should be produced for the vesvoy.
                    }
                }
            }
            for (int i = 1; i <= 3; i++) {
                if (i == 1) {
                    logger.debug("In createNewVesselTextFile: i value:" + i);
                    createUnitRecordData(CNTR, rdsDataFinal, null, stowCntrData, stowChassisData, null, dcmData, vesvoy + leg);
                } else if (i == 2) {
                    logger.debug("In createNewVesselTextFile: i value:" + i);
                    if (!"Barge".equalsIgnoreCase(processType))
                        createUnitRecordData(ROB, null, robRdsDataFinal, stowCntrData, stowChassisData, null, dcmData, vesvoy + leg);
                } else if (i == 3) {
                    logger.debug("In createNewVesselTextFile: i value:" + i);
                    createUnitRecordData(BARE, null, null, null, null, bareChassisData, null, vesvoy + leg);
                }
            }
            createEofData();
            // If bargeCarrierId is null then mark it as error
            if ("barge".equalsIgnoreCase(processType) && (bargeCarrierId == null || bargeCarrierId.equals(""))) { //A34
                nvLogger.addError(vesvoy, "", "Cannot find OB Actual Carrier Id for " + vesvoy + ", process cannot be continued.");
                return false;
            }
            if (currentVesvoyType.equals(PRIMARY) && !"barge".equalsIgnoreCase(processType))
                writeToUnitTextFile(outputText, fileName);
        } catch (Exception e) {
            logger.error("ERROR in processing", e);
            nvLogger.addError(vesvoy, "", "ERROR in processing<br/><br/>" + e.getMessage());
            return false;
        }
        return true;
    }

    private void createVesselVisitData(String vesvoy) {
        NewVes data = new NewVes();
        data.setRecType("vesselVisit");
        data.setUnitId(vesvoy.substring(0, 6));
        Date today = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        String trigDate = sdf.format(today);
        data.setUfvFlex2(trigDate);
        sdf = new SimpleDateFormat("HH:mm:ss");
        String trigTime = sdf.format(today);
        data.setUfvFlex3(trigTime);
        try {
            vesselAvailableDate = CommonBusinessProcessor.getAvailableDateByVVD(vesvoy, "ANK");
        } catch (Exception e) {
            logger.error("Problem in retreiving available date for" + vesvoy);
            nvLogger.addError(vesvoy, "", "Problem in retreiving available date for" + vesvoy);
        }
        if (vesselAvailableDate == null) {
            logger.error("vesselAvailableDate is null");
            return;
        }
        if (vesselAvailableDate.length() > 0)
            vesselAvailableDate = vesselAvailableDate.trim();//To catch error if available date is null
        /*if(vesselAvailableDate==null || vesselAvailableDate.equals("")) // For testing purpose, setting this date as todays date
        {
			vesselAvailableDate = CalendarUtil.convertDateToString(triggerDate);
		}*/

        data.setUfvFlexDate2(vesselAvailableDate);
        outputText.add(createTextRecord(data));
        try {
            VesselVO vvo = CommonBusinessProcessor.getVesselDetails(vesvoy.substring(0, 3));
            vesselOpr = vvo.getVessOpr();
        } catch (Exception e) {
            logger.error("Problem in retreiving vessel operator for " + vesvoy.substring(0, 3));
        }
        if (vesselOpr == null || vesselOpr.equals("")) // For testing purpose, setting this vessel operator to MAT
            vesselOpr = "MAT";
        //Get arrival date
        vesselArrivalDate = CommonBusinessProcessor.getArrivalDateByVVD(vesvoy, "ANK");
    }

    private String getCategory(String type, String dir, String dischPort, String dport, String hazOpenCloseFlg) {
        String category = "";


        if (type.equals(ROB)) {
            category = "THROUGH";
        } else if (dir.equalsIgnoreCase("MTY")) {
            if (dischPort.equalsIgnoreCase("ANK") || dischPort.equalsIgnoreCase("OPT")) {
                category = "IMPORT";
            } else {
                category = "THROUGH";
            }
        } else if (dir.equalsIgnoreCase("IN") && dport.equalsIgnoreCase("ANK")) {
            category = "IMPORT";
        } else if (dir.equalsIgnoreCase("IN")) {
            category = "IMPORT";
        } else if (dir.equalsIgnoreCase("OUT")) {
            category = "EXPORT";
        }
        return category;
    }

    private void createUnitRecordData(String type,
                                      ArrayList<TosRdsDataFinalMt> rdsFinalData, ArrayList<TosRdsDataFinalMt> robRdsData,
                                      ArrayList<TosStowPlanCntrMt> stowCtrData,
                                      ArrayList<TosStowPlanChassisMt> stowChsData, ArrayList<TosStowPlanChassisMt> bareChsData,
                                      ArrayList<TosDcmMt> dcmData,
                                      String vvd) // This vvd is used only for Barge - Bare chassis record processing
    {
        logger.debug("entered createUnitRecordData - type value:" + type);

        // Getting vessel service.
        String vesselServiceCarr = null;
        TosRdsDataFinalMt tempRdsDataFinalMtForvesSer = new TosRdsDataFinalMt();
        if (rdsFinalData != null && rdsFinalData.size() > 0)
            tempRdsDataFinalMtForvesSer = (TosRdsDataFinalMt) rdsFinalData.get(0);
        logger.info("Looking up vessel service for " + tempRdsDataFinalMtForvesSer.getVesvoy());
        try {
            if (tosLookUp == null)
                tosLookUp = new TosLookup();
            vesselServiceCarr = tosLookUp.getVesselService(tempRdsDataFinalMtForvesSer.getVesvoy());
        } catch (Exception ex1) {
            logger.error("Failed to create TosLookup in createUnitRecordData \n" + ex1);
        } finally {
            if (tosLookUp != null) {
                tosLookUp.close();
                tosLookUp = null;
            }
        }
        logger.info("vesselServiceCarr is : " + vesselServiceCarr);
        vesselServiceCarr = vesselServiceCarr == null ? "" : vesselServiceCarr;
        // end vessel service

        if (type.equals(CNTR) || type.equals(ROB)) {
            if (type.equals(ROB))
                rdsFinalData = robRdsData;
            //logger.info("**** UNIT RECORD ****");
            for (int i = 0; i < rdsFinalData.size(); i++) {
                TosRdsDataFinalMt rdsFd = rdsFinalData.get(i);
                NewVes data = new NewVes();
                data.setRecType("unit");
                String ctrno = rdsFd.getContainerNumber();
                String chkdigit = rdsFd.getCheckDigit();
                String owner = rdsFd.getOwner();
                owner = owner == null ? "MATU" : owner;
                if (isNumber(ctrno))
                    ctrno = owner + ctrno;
                chkdigit = chkdigit != null ? chkdigit : "X";
                data.setUnitId(ctrno + chkdigit);
                String dischPort = rdsFd.getDischargePort();
                dischPort = dischPort == null ? "" : dischPort;
                String category = "";
                String dir = rdsFd.getDir();
                dir = dir == null ? "" : dir;
                String ds = rdsFd.getDs();
                ds = ds == null ? "" : ds;
                String dport = rdsFd.getDport();
                dport = dport == null ? "" : dport;
                String orientation = rdsFd.getOrientation();
                orientation = orientation == null ? "" : orientation;
                String locationRowDeck = rdsFd.getLocationRowDeck();
                locationRowDeck = locationRowDeck == null ? "" : locationRowDeck;
                String hazOpenCloseFlg = rdsFd.getHazardousOpenCloseFlag();
                hazOpenCloseFlg = hazOpenCloseFlg == null ? "" : hazOpenCloseFlg;
                boolean transhipToImport = false;
                supCntrCategory = null;//A37
                logger.info("Type:" + type + "\t CN:" + rdsFd.getContainerNumber() + "\tDIR:" + dir + "\tDISCPORT:" + dischPort + "\tDPORT:" + dport + "\tHAZOCFLAG:" + hazOpenCloseFlg +
                        "\tDS:" + ds + "\tORIENTATION:" + orientation + "\tCONSIGNE:" + rdsFd.getConsignee() + "\tSHIPPER:" + rdsFd.getShipper());
                /*category = getCategory(type, dir, dischPort, dport, hazOpenCloseFlg);
				if("barge".equalsIgnoreCase(processType)){
					//logger.info("barge dir value:"+dir +" and ds value:"+ds + "and dport value"+dport + " for container#:"+rdsFd.getContainerNumber());
					if(!"MTY".equalsIgnoreCase(dir)){
						if("HON".equalsIgnoreCase(dport))
							dir = "IN";
						else if("HIL".equalsIgnoreCase(dport))
							dir = "IN";
						else if("KHI".equalsIgnoreCase(dport))
							dir = "IN";
						else if("NAW".equalsIgnoreCase(dport))
							dir = "IN";
						else if("KAH".equalsIgnoreCase(dport))
							dir = "IN";
						else if("KWJ".equalsIgnoreCase(dport))
							dir = "IN";
						else if("EBY".equalsIgnoreCase(dport))
							dir = "IN";
						else if("MAJ".equalsIgnoreCase(dport))
							dir = "IN";
						else if("JIS".equalsIgnoreCase(dport))
							dir = "IN";

					}
					if(dir.equalsIgnoreCase("IN") ){
						category = "IMPORT";
						//logger.info("category import set"+rdsFd.getContainerNumber());
					}

				}*/
                if (!currentVesvoyType.equals(SUPPLEMENT)) {
                    //Alaska changes
                    if ("ANK".equalsIgnoreCase(dischPort)) {
                        category = "IMPORT";
                    } else {
                        category = "THROUGH";
                    }
                }

                String vesselService = null;
                boolean isOneOfGCSPort = CommonBusinessProcessor.isOneOfGCSPorts(dischPort);
                if (currentVesvoyType.equals(SUPPLEMENT) && isOneOfGCSPort) {

                    logger.info("Looking up vessel service for " + rdsFd.getVesvoy());
                    try {
                        if (tosLookUp == null)
                            tosLookUp = new TosLookup();
                        vesselService = tosLookUp.getVesselService(rdsFd.getVesvoy());
                    } catch (Exception ex1) {
                        logger.error("Failed to create TosLookup in createUnitRecordData \n" + ex1);
                    }
                    logger.info("vesselService is : " + vesselService);
                    vesselService = vesselService == null ? "" : vesselService;
                    if (vesselService.equals("GCS")) {
                        category = "THROUGH";
                        logger.info("Assigning Category as THOUGH, since its GCS vessel");
                    }
                }
                logger.info("CATEGORY ASSIGNED:" + category);
                data.setCategory(category);
                if (type.equals(CNTR)) {
                    data.setTransitState("INBOUND");
                }
                // A33 - Starts
                if (currentVesvoyType.equals(SUPPLEMENT)) {
                    String tState = null;
                    String tosCategory = null;
                    try {
                        if (tosLookUp == null)
                            tosLookUp = new TosLookup();
                        HashMap<String, String> resultMap = tosLookUp.getUnitDetails(rdsFd.getContainerNumber() + chkdigit);
                        if (resultMap != null) {
                            tState = resultMap.get("TRANSIT_STATE");
                            tosCategory = resultMap.get("CATEGORY");
                        }
                    } catch (Exception e) {
                        tState = null;
                        logger.error("Unable to get tstate from TOS for " + rdsFd.getContainerNumber());
                    }
                    if (tState != null) {
                        if (tState.equalsIgnoreCase("S20_INBOUND"))
                            tState = "INBOUND";
                        else if (tState.equalsIgnoreCase("S40_YARD"))
                            tState = "YARD";
                        else if (tState.equalsIgnoreCase("S10_ADVISED"))
                            tState = "ADVISED";
                        else if (tState.equalsIgnoreCase("S70_DEPARTED"))
                            tState = "DEPARTED";
                        else if (tState.equalsIgnoreCase("S99_RETIRED"))
                            tState = "RETIRED";
                        else if (tState.equalsIgnoreCase("S50_ECOUT"))
                            tState = "ECOUT";
                        else if (tState.equalsIgnoreCase("S30_ECIN"))
                            tState = "ECIN";
                        else if (tState.equalsIgnoreCase("S60_LOADED"))
                            tState = "LOADED";
                        data.setTransitState(tState);
                        if (vesselService != null && vesselService.equalsIgnoreCase("GCS")) {//A37
                            if (tState.equalsIgnoreCase("INBOUND") && tosCategory.equalsIgnoreCase("IMPRT") && category.equalsIgnoreCase("THROUGH")) {
                                supCntrCategory = "THROUGH";
                            } else if (tState.equalsIgnoreCase("YARD") && tosCategory.equalsIgnoreCase("IMPRT") && category.equalsIgnoreCase("THROUGH")) {
                                supCntrCategory = "TRANSSHIP";
                            } else
                                supCntrCategory = category;
                            data.setCategory(supCntrCategory);
                            logger.info("supCntrCategory=" + supCntrCategory + ", tState=" + tState);
                        }//A37
                    } else {
                        logger.error("Discarding tstate from TOS for " + rdsFd.getContainerNumber() + ", since tstate is null");
                    }
                    logger.info("createUnitRecordData: tosCategory :" + tosCategory + " supp category :" + category);
                    if ("IMPORT".equalsIgnoreCase(category) && "TRSHP".equalsIgnoreCase(tosCategory)) {
                        transhipToImport = true;
                    }
                }
                // A33 - Ends.
                String freightKind = "";
                if (ds.equalsIgnoreCase("CON"))
                    freightKind = "FCL";
                else if (orientation.equalsIgnoreCase("F"))
                    freightKind = "FCL";
                else if (orientation.equalsIgnoreCase("E"))
                    freightKind = "MTY";
                else if (dir.equalsIgnoreCase("IN"))
                    freightKind = "FCL";
                else if (dir.equalsIgnoreCase("OUT"))
                    freightKind = "FCL";
                else if (dir.equalsIgnoreCase("MTY"))
                    freightKind = "MTY";
                else
                    freightKind = "MTY";
                data.setFreightKind(freightKind);
                if (locationRowDeck.equalsIgnoreCase(""))
                    data.setLine("MAT");
                else
                    data.setLine(locationRowDeck);
                String hazf = "";//rdsFd.getHazf();
                vesselOpr = vesselOpr == null ? "" : vesselOpr;
                //logger.info("HAZF -> Table:"+hazf +", "+vesselOpr+", "+ ctrno);
                //if(hazf==null||hazf.equals(""))
                //{
                // Check the DCM if there is any haz data for the container
                if (type != null && (type.equals(CNTR) || type.equals(ROB)) && !currentVesvoyType.equals(SUPPLEMENT)) {
                    for (int d = 0; d < dcmData.size(); d++) {
                        TosDcmMt dcmD = dcmData.get(d);
                        String dCtrno = dcmD.getContainerNumber();
                        if (dCtrno != null && dCtrno.equals(ctrno)) {
                            hazf = "Y";
                            logger.info("HAZF-> Y From DCM check : " + ctrno);
                            break;
                        }
                    }
                    if (!hazf.equals("Y")) {
                        // If the container dont have DCM then look into stowplan haz table
                        for (int s = 0; s < stowCntrData.size(); s++) {
                            TosStowPlanCntrMt stowCntrMt = stowCntrData.get(s);
                            String ctrNbr = stowCntrMt.getContainerNumber();
                            if (ctrNbr != null && ctrNbr.equalsIgnoreCase(ctrno)) {
                                if (stowCntrMt.getHazf() == null || !stowCntrMt.getHazf().equals("Y"))
                                    break;
                                Set<TosStowPlanHazMt> stowPlanHazMt = stowCntrMt.getTosStowPlanHazMts();
                                if (stowPlanHazMt != null) {
                                    Iterator<TosStowPlanHazMt> itrHaz = stowPlanHazMt.iterator();
                                    while (itrHaz.hasNext()) {
                                        hazf = "Y";
                                        logger.info("HAZF-> Y From OHZ check : " + ctrno);
                                        break;
                                    }
                                }
                            }
                            if (hazf.equals("Y"))
                                break;
                        }
                    }
                    if (hazf.equals("Y"))
                        data.setHazardFlag(hazf);
                }
                outputText.add(createTextRecord(data));
                //
                createHandlingData(rdsFd, null, type);
                createEtcData(rdsFd, null, type);
                createSealsData(rdsFd);
                createContentsData(rdsFd, null, type);
                createUnitFlexData(rdsFd);
                createUfvFlexData(rdsFd);
                createEquipmentData(rdsFd, null, type);
                if (!currentVesvoyType.equals(SUPPLEMENT)) {
                    if (hazf.equals("Y")) {
                        boolean isHazCreatedFromDcm = false;
                        for (int d = 0; d < dcmData.size(); d++) {
                            TosDcmMt dcmD = dcmData.get(d);
                            String dCtrno = dcmD.getContainerNumber();
                            if (dCtrno != null && dCtrno.equals(ctrno)) {
                                isHazCreatedFromDcm = true;
                                createHazardData(dcmD);
                            }
                        }
                        if (!isHazCreatedFromDcm && vesselOpr.equals("CSX")) {
                            for (int c = 0; c < stowCtrData.size(); c++) {
                                TosStowPlanCntrMt stowCntrMt = stowCntrData.get(c);
                                String ctrNbr = stowCntrMt.getContainerNumber();
                                if (ctrNbr != null && ctrNbr.equalsIgnoreCase(ctrno)) {
                                    if (stowCntrMt.getHazf() == null || !stowCntrMt.getHazf().equals("Y"))
                                        break;
                                    Set<TosStowPlanHazMt> hazDataSet = stowCntrMt.getTosStowPlanHazMts();
                                    Iterator<TosStowPlanHazMt> itrHaz = hazDataSet.iterator();
                                    while (itrHaz.hasNext()) {
                                        TosStowPlanHazMt hazData = itrHaz.next();
                                        createHazardDataForCsx(hazData);
                                    }
                                }
                            }
                        }
                    }
                    createOogData(rdsFd);
                    createPositionData(rdsFd, null, type);
                    createReeferData(rdsFd);
                }
                createRoutingData(rdsFd, null, type, transhipToImport);
                createCarrierData(rdsFd, null, type, vesselServiceCarr);
                if ("Barge".equalsIgnoreCase(processType)) {
                    createHoldDataForBarge(rdsFd);
                    createBookingDate(rdsFd);
                }

            }
        } else if (type.equals(BARE)) {
            //logger.info("entered bare process"+bareChassisData.size());
            for (int b = 0; b < bareChassisData.size(); b++) {
                TosStowPlanChassisMt bareChd = bareChassisData.get(b);
                NewVes data = new NewVes();
                data.setRecType("unit");
                data.setUnitId(bareChd.getChassisNumber() + bareChd.getChassisCd());
                data.setCategory("IMPORT");
                data.setTransitState("INBOUND");
                data.setFreightKind("MTY");
                String chsHolds = bareChd.getChassisHolds();
                if (chsHolds != null && chsHolds.length() > 0)
                    data.setLine(chsHolds);
                else
                    data.setLine("MAT");
                outputText.add(createTextRecord(data));
                if ("barge".equalsIgnoreCase(processType)) {
                    TosRdsDataFinalMt inData = new TosRdsDataFinalMt();
					/*if(stowCntrData!=null && stowCntrData.size() > 0) {
						inData.setVesvoy(stowCntrData.get(0).getVesvoy());
						inData.setLeg(stowCntrData.get(0).getLeg());
					}*/
                    if (vvd != null && vvd.length() > 0) {
                        inData.setVesvoy(vvd.substring(0, 6));
                        inData.setLeg(vvd.substring(6, 7));
                    }
                    createHandlingData(inData, bareChd, type);
                    createContentsData(inData, bareChd, type);
                    createEquipmentData(inData, bareChd, type);
                    createPositionData(inData, bareChd, type);
                    createRoutingData(inData, bareChd, type, false);
                    createCarrierData(inData, bareChd, type, vesselServiceCarr);
                } else {
                    createHandlingData(null, bareChd, type);
                    createContentsData(null, bareChd, type);
                    createEquipmentData(null, bareChd, type);
                    createPositionData(null, bareChd, type);
                    createRoutingData(null, bareChd, type, false);
                    createCarrierData(null, bareChd, type, vesselServiceCarr);
                }
            }
        }
    }

    private void createHandlingData(TosRdsDataFinalMt inData, TosStowPlanChassisMt bareChd, String type) {
        NewVes data = new NewVes();
        data.setRecType("handling");
        if (type.equals(ROB) || type.equals(CNTR)) {
            String cargoNotes = inData.getCargoNotes();
            cargoNotes = cargoNotes == null ? "" : cargoNotes;
            String crstatus = inData.getCrstatus();
            crstatus = crstatus == null ? "" : crstatus;
            String comments = inData.getComments();
            comments = comments == null ? "" : comments;
            String ctrno = inData.getContainerNumber();
            ctrno = ctrno == null ? "" : ctrno;
            String dest = inData.getDport();
            dest = dest == null ? "" : dest;
            if ("O/P".equalsIgnoreCase(inData.getTruck()) && CommonBusinessProcessor.isValidNeighborIslandPort(dest) && !currentVesvoyType.equals(SUPPLEMENT)) {
                data.setRemark(cargoNotes.trim() + " ZZZZ");
            } else
                data.setRemark(cargoNotes.trim());// + " " + comments.trim());
            //if(currentVesvoyType.equals(SUPPLEMENT))
            //	data.setRemark(cargoNotes.trim() + " " + comments.trim());
            if ("barge".equalsIgnoreCase(processType)) {
                for (int c = 0; c < stowCntrData.size(); c++) {
                    TosStowPlanCntrMt stowCntrMt = stowCntrData.get(c);
                    String ctrNbr = stowCntrMt.getContainerNumber();
                    if (ctrNbr != null && ctrNbr.equalsIgnoreCase(ctrno)) {
                        Set<TosStowPlanHazMt> hazDataSet = stowCntrMt.getTosStowPlanHazMts();
                        Iterator<TosStowPlanHazMt> itrHaz = hazDataSet.iterator();
                        if (itrHaz.hasNext()) {
                            TosStowPlanHazMt hazData = itrHaz.next();
                            String unNbr = "";
                            String hazDesc = hazData.getDescription();
                            hazDesc = hazDesc == null ? "" : hazDesc;
                            String hazClass = hazData.getHazClass();
                            hazClass = hazClass == null ? "" : hazClass;
                            if (hazData.getUnNumber() != null) {
                                if (hazData.getUnNumber().doubleValue() > 0) {
                                    unNbr = "UN" + hazData.getUnNumber();
                                }
                            }
                            if (!"MTY".equalsIgnoreCase(inData.getDir())) {
                                if (comments.equals(""))
                                    comments = hazDesc + ", " + hazClass + ", " + unNbr;
                                else
                                    comments = comments + ", " + hazDesc + ", " + hazClass + ", " + unNbr;
                            }
                        }
                    }
                }
                data.setRemark(comments);
            }

            String strc = inData.getStowRestrictionCode();
			/*data.setSpecialStow(strc);

			if(strc!= null && strc.equals("C"))
				data.setSpecialStow("CL");
			else if(strc != null && strc.equals("3"))
				data.setSpecialStow("INSP");
			if(crstatus != null)
			{
				if(crstatus.contains("TI") || crstatus.contains("CG"))
					data.setSpecialStow("INSP");
			}*/

            data.setSpecialStow(null);
            //
            String lastFreeDay = "";
            lastFreeDay = inData.getLocationCategory();
            if (lastFreeDay != null && lastFreeDay.length() > 0) {
                lastFreeDay = CalendarUtil.convertDateStringToString(lastFreeDay, false);
                data.setLastFreeDay(lastFreeDay);
            }
        } else if (type.equals(BARE)) {
            data.setRemark(bareChd.getComments() + " ");
        }
        if (data.getSpecialStow() == null && data.getRemark() == null && data.getLastFreeDay() == null)
            return;
        outputText.add(createTextRecord(data));
    }

    private void createEtcData(TosRdsDataFinalMt inData, TosStowPlanChassisMt bareChd, String type) {
        NewVes data = new NewVes();
        data.setRecType("etc");
        if (type.equals(ROB) || type.equals(CNTR)) {
            String drayStatus = null;
            String planDisp = inData.getPlanDisp();
            planDisp = planDisp == null ? "" : planDisp;
            String dsc = inData.getDsc();
            dsc = dsc == null ? "" : dsc;
            String commodity = inData.getCommodity();
            commodity = commodity == null ? "" : commodity;
            String locationStatus = inData.getLocationStatus();
            locationStatus = locationStatus == null ? "" : locationStatus;
            String dir = inData.getDir();
            dir = dir == null ? "" : dir;
            if ("S".equals(dsc))
                drayStatus = "OFFSITE";
            else if (planDisp.equals("W"))
                drayStatus = "OFFSITE";
            else if (planDisp.equals("3")) {
                if (commodity.length() > 2) {
                    if (commodity.substring(0, 2).equals("P2") || commodity.substring(0, 2).equals("53"))
                        drayStatus = "OFFSITE";
                }
            } else if (dsc.equals("C") && !locationStatus.equals("6"))
                drayStatus = "DRAYIN";
            else if (planDisp.equals("7"))
                drayStatus = "TRANSFER";
            else if (planDisp.equals("9") || planDisp.equals("A") || planDisp.equals("B"))
                drayStatus = "RETURN";
            if (vesselOpr.equals("CSX"))
                drayStatus = "DRAYIN";
            data.setDrayStatus(drayStatus);
            String temp = inData.getTemp();
            logger.debug(inData.getContainerNumber() + " temp value " + temp);

            if (temp != null && temp.length() > 0 && !temp.equalsIgnoreCase("AMB")) {
                data.setRequiresPower("Y");
            }

            if (temp != null && (temp.equalsIgnoreCase("KFF") || inData.getTypeCode().startsWith("I"))) {

                data.setRequiresPower("N");
            }
        }
        if (data.getDrayStatus() == null && data.getRequiresPower() == null)
            return;
        outputText.add(createTextRecord(data));
    }

    private void createSealsData(TosRdsDataFinalMt inData) {
        NewVes data = new NewVes();
        data.setRecType("seals");
        data.setSeal1(inData.getSealNumber());
        if (data.getSeal1() == null)
            return;
        outputText.add(createTextRecord(data));
    }

    private void createContentsData(TosRdsDataFinalMt inData, TosStowPlanChassisMt bareChd, String type) {
        NewVes data = new NewVes();
        data.setRecType("contents");
        if (type.equals(ROB) || type.equals(CNTR)) {
            String consigneeName = inData.getConsignee();

            if (consigneeName != null) {//A36
                consigneeName = consigneeName.length() >= 35 ? consigneeName
                        .substring(0, 35) : consigneeName;
            }

            if ("barge".equalsIgnoreCase(processType)) {
                data.setBlNumber(inData.getBookingNumber());
            } else {
                if (currentVesvoyType.equals(SUPPLEMENT)) {
                    data.setBlNumber(inData.getBookingNumber());
                } else if (currentVesvoyType.equals(PRIMARY)) {
                    data.setBlNumber("DO NOT EDIT-NEWVES:" + inData.getBookingNumber());
                }
            }

            BigDecimal cwg = inData.getCweight();
            if (cwg != null) {
                double ncwg = cwg.doubleValue();
                data.setWeightKg(convertPoundsToKG(ncwg));
            }

            String commodity = inData.getCommodity();
            commodity = commodity == null ? "" : commodity;
            if ("barge".equalsIgnoreCase(processType)) {
                if (commodity.length() == 0) {
                    if ("MTY".equalsIgnoreCase(inData.getDir()))
                        commodity = "MTY";
                }
                if ("AUT".equalsIgnoreCase(inData.getDs()))
                    commodity = "AUTO";
                if ("CFS".equalsIgnoreCase(inData.getDs()))
                    commodity = "CFS";

            }

            // Alaska changes start
            //if("Y".equalsIgnoreCase(inData.getMultiStop()))
            //	commodity = "MULTISTOP";
            // Alaska changes end

            String outComId = " ";
            String outComName = " ";

            logger.info("Container " + inData.getContainerNumber() + "Commodity " + commodity);

            if (commodity.length() > 0) {

                if (commodity.equals("COBUS") || commodity.equals("COBIZ") || commodity.equals("CO BUS") || commodity.equals("CO BIZ")) {
                    outComId = "COBUS";
                    outComName = "COMPANY BUSINESS";
                } else if (commodity.equals("ASTRAY")) {
                    outComId = "ASTRAY";
                    outComName = "ASTRAY";
                } else if (commodity.equals("SHRED")) {
                    outComId = "SHRED";
                    outComName = "SHRED";
                } else if (commodity.equals("MTY") || commodity.equals("EMPTY")) {
                    outComId = "MTY";
                    outComName = "EMPTY";
                } else if (commodity.equals("XMAS40")) {
                    outComId = "XMAS40";
                    outComName = "XMAS40";
                } else if (commodity.equals("XMASTREE")) {
                    outComId = "XMASTREE";
                    outComName = "XMASTREE";
                } else if (commodity.equals("T-60") || commodity.equals("T60")) {
                    outComId = "T-60";
                    outComName = "TARIFF 60";
                } else if (commodity.equals("LOAD")) {
                    outComId = "LOAD";
                    outComName = "LOAD";
                } else if (commodity.equals("HHGDS")) {
                    outComId = "HHGDS";
                    outComName = "HOUSEHOLD GOODS";
                } else if (commodity.equals("PAPAYA")) {
                    outComId = "PAPAYA";
                    outComName = "PAPAYA";
                } else if (commodity.equals("PLANTS")) {
                    outComId = "PLANTS";
                    outComName = "PLANTS";
                } else if (commodity.equals("CANNED P")) {
                    outComId = "CANNED PINEAPPLE";
                    outComName = "CANNED PINEAPPLE";
                } else if (commodity.equals("FRESH PI")) {
                    outComId = "FRESH PINEAPPLE";
                    outComName = "FRESH PINEAPPLE";
                } else if (commodity.equals("MP")) {
                    outComId = "MP";
                    outComName = "MAUI PINE";
                } else if (commodity.equals("DM")) {
                    outComId = "DM";
                    outComName = "DEL MONTE";
                } else if (commodity.equals("DOLE")) {
                    outComId = "DOLE";
                    outComName = "DOLE";
                } else if (commodity.equals("P.O.")) {
                    outComId = "P.O.";
                    outComName = "POST OFFICE";
                } else if (commodity.equals("PAPER")) {
                    outComId = "PAPER";
                    outComName = "PAPER";
                } else if (commodity.equals("AUTO")) {
                    outComId = "AUTO";
                    outComName = "AUTO";
                } else if (commodity.equals("ANHEUSER")) {
                    outComId = "ANHEUSER";
                    outComName = "ANHEUSER BUSCH";
                } else if (commodity.equals("MTY TANK")) {
                    outComId = "MTY TANK";
                    outComName = "MTY TANK";
                } else if (commodity.equals("DHX")) {
                    outComId = "DHX";
                    outComName = "DHX";
                } else if (commodity.equals("GOLDNSTA")) {
                    outComId = "GOLDEN STATE";
                    outComName = "GOLDEN STATE";
                } else if (commodity.equals("HAZARD")) {
                    outComId = "HAZARD";
                    outComName = "HAZARDOUS";
                } else if (commodity.equals("MLK CASE")) {
                    outComId = "MTY MILK CASES";
                    outComName = "MTY MILK CASES";
                } else if (commodity.equals("MTY PLTS")) {
                    outComId = "MTY PTLS";
                    outComName = "MTY PALLETS";
                } else if (commodity.equals("SCRAP")) {
                    outComId = "SCRAP";
                    outComName = "SCRAP";
                } else if (commodity.equals("TIRES")) {
                    outComId = "TIRES";
                    outComName = "TIRES";
                } else if (commodity.equals("SAFEWAY")) {
                    outComId = "SAFEWAY";
                    outComName = "SAFEWAY";
                } else if (commodity.equals("SIT") || commodity.equals(MULTISTOP_SIT))   // the value can come in Newves as SIT, but set as MULTISTOP in data
                {
                    outComId = MULTISTOP_SIT;
                    outComName = MULTISTOP_SIT;
                } else if (commodity.equals("AUTOCY")) {
                    outComId = "AUTOCY";
                    outComName = "AUTO CY";
                } else if (commodity.equals("AUTOCON")) {
                    outComId = "AUTOCON";
                    outComName = "AUTO CON";
                } else if (commodity.equals("ALSAUT")) {
                    outComId = "ALS AUT";
                    outComName = "ALS AUT";
                } else if (commodity.equals("ALS ?")) {
                    outComId = "ALS ?";
                    outComName = "ALS AUTO?";
                } else if (commodity.equals("MTYAUT")) {
                    outComId = "MTYAUT";
                    outComName = "MTY AUTO";
                } else if (commodity.equals("YB")) {
                    outComId = "YB";
                    outComName = "YOUNG BROTHERS";
                } else if (commodity.equals("TRASH")) {
                    outComId = "TRASH";
                    outComName = "TRASH";
                } else if (commodity.equals("GYM")) {
                    outComId = "GYM";
                    outComName = "GYM";
                } else if (commodity.equals("GEAR")) {
                    outComId = "GEAR";
                    outComName = "GEAR";
                } else if (commodity.equals("BALLAST")) {
                    outComId = "BALLAST";
                    outComName = "BALLAST";
                } else if (commodity.equals("MULTISTOP")) {
                    outComId = "MULTISTOP";
                    outComName = "MULTISTOP";
                }
                //D031688
                else if (commodity.equals("CFS")) {
                    outComId = "CFS";
                    outComName = "CFS";
                } else if (commodity.equals("MULTICFS")) {
                    outComId = "MULTISTOP CFS";
                    outComName = "MULTISTOP CFS";
                } else if (commodity.equals("MULTISIT")) {
                    outComId = MULTISTOP_SIT;
                    outComName = MULTISTOP_SIT;
                } else if (commodity.equals("CFS SIT")) {
                    outComId = MULTISTOP_CFS_SIT;
                    outComName = MULTISTOP_CFS_SIT;
                } else if (commodity.equals("MULCFSSI")) {
                    outComId = MULTISTOP_CFS_SIT;
                    outComName = MULTISTOP_CFS_SIT;
                } else if ("Y".equalsIgnoreCase(inData.getMultiStop()) || "N".equalsIgnoreCase(inData.getMultiStop())) {
                    outComId = "MULTISTOP";
                    outComName = "MULTISTOP";
                }
                //D031688

            }

            data.setCommodityId(outComId);
            data.setCommodityName(outComName);

            // Modified by karthik
            String consigneeId = "";
            //String tpCodeFtur = "";
            //if (inData.getTypeCode() != null && inData.getTypeCode().length()>=8)
            //	tpCodeFtur = inData.getTypeCode().substring(6, 8);
            if ("AUTOMOBILE".equals(consigneeName)) {
                consigneeId = "AUTOID";
            } else if (inData.getDir() != null && !inData.getDir().equals("MTY")) {
                if (!(inData.getTypeCode() != null && inData.getTypeCode().endsWith("GR") && inData.getBookingNumber() == null)) {
                    consigneeId = inData.getCneeCode();
                }
            }

            data.setConsigneeName(consigneeName);

            if (consigneeName != null && consigneeName.contains("REQUIRES CS ACTION")) {
                consigneeId = "REQCSACTON";
                data.setConsigneeId(consigneeId);
            }
			/*if(inData.getConsigneeArol()!=null && "0000000000".equals(inData.getConsigneeArol()) && inData.getBookingNumber()!=null&& !inData.getBookingNumber().equals("null")  && !isNumber(inData.getBookingNumber()))
			{
				data.setConsigneeId("REQCSACTON");
				data.setConsigneeName("REQUIRES CS ACTION");
			}*/
            if (consigneeName != null && consigneeName.contains("UNAPPROVED VARIANCE")) {
                consigneeId = "UNAPPROVAR";
                data.setConsigneeId(consigneeId);
            }
            if (type.equals(CNTR)) {
                data.setConsigneeId(consigneeId);
            }

            //Alaska changes - Consignee Address
            data.setConsigneeAddr(inData.getConsigneeAddr());
            data.setConsigneeSuite(inData.getConsigneeSuite());
            data.setConsigneeCity(inData.getConsigneeCity());
            data.setConsigneeState(inData.getConsigneeState());
            data.setConsigneeCountry(inData.getConsigneeCountry());
            data.setConsigneeZipCode(inData.getConsigneeZipCode());

            String shipperName = inData.getShipper();
            data.setShipperName(shipperName);
            if ("barge".equalsIgnoreCase(processType)) {
                if ("AUT".equalsIgnoreCase(inData.getDs())) {
                    data.setShipperName("AUTO");
                } else if ("CFS".equalsIgnoreCase(inData.getDs())) {
                    data.setShipperName("CFS");
                }

            }
            String shipperId = "";
            if (inData.getDir() != null && !inData.getDir().equals("MTY")) {
                if (shipperName == null) {
                    shipperId = "";
                } else if (inData.getShipperArol() != null && !inData.getShipperArol().equals("") && !inData.getShipperArol().equals("null")) {
                    shipperId = inData.getShipperArol();
                } else {
                    shipperId = inData.getShipperOrgnId();
                }
            }


            if (currentVesvoyType.equals(SUPPLEMENT) && inData.getShipperOrgnId() != null && !"".equalsIgnoreCase(inData.getShipperOrgnId()) && !shipperId.equalsIgnoreCase(inData.getShipperOrgnId())) {
                logger.info("shipper id is overwrote from TOS for " + inData.getContainerNumber());
                shipperId = inData.getShipperOrgnId();
            }

            if (type.equals(CNTR)) {
                data.setShipperId(shipperId);
            }


            //
            if (currentVesvoyType.equals(PRIMARY)) { // For Barge or Longhaul primary
                // Check if consignee name is set, if not dont set consignee id too
                if (data.getConsigneeName() == null || data.getConsigneeName().length() <= 0 || data.getConsigneeName().contains("TRASH")) {
                    data.setConsigneeId(null);
                }
            } else if (currentVesvoyType.equals(SUPPLEMENT)) { // For supplemental
                if (data.getConsigneeName() != null && data.getConsigneeName().contains("TRASH")) {
                    data.setConsigneeId(null);
                }
            }
        } else if (type.equals(BARE)) {
            BigDecimal tare = bareChd.getChassisTare();
            if (tare.doubleValue() > 0)
                data.setWeightKg(convertPoundsToKG(tare.doubleValue()));
        }
        if (data.getBlNumber() == null && data.getWeightKg() == null && data.getConsigneeId() == null &&
                data.getConsigneeName() == null && data.getShipperId() == null && data.getShipperName() == null)
            return;
        outputText.add(createTextRecord(data));
    }

    private void createUnitFlexData(TosRdsDataFinalMt inData) {
        NewVes data = new NewVes();
        data.setRecType("unit-flex");
        data.setUnitFlex1(inData.getCneePo());
        data.setUnitFlex3(inData.getCsrId());
        String unitFlex12 = inData.getCneePo();
        String dport = inData.getDport();
        dport = dport == null ? "" : dport;
        String cargoNotes = inData.getCargoNotes();
        cargoNotes = cargoNotes == null ? "" : cargoNotes;
        if (unitFlex12 != null) {
            if (unitFlex12.length() >= 24) {
                if (unitFlex12.substring(0, 6).length() == 6 && unitFlex12.substring(7, 24).length() >= 17) {
                    //data.setUnitFlex12(unitFlex12.substring(7, 24)); //
                }
            }
        }
        if (!SUPPLEMENT.equalsIgnoreCase(currentVesvoyType)
                && CommonBusinessProcessor.isValidNeighborIslandPort(dport)
                && cargoNotes.contains("N/P")) {
            try {
                if (tosLookUp == null) {
                    tosLookUp = new TosLookup();
                }
            } catch (Exception e) {
                nvLogger.addError("", "",
                        "Unable to create TosLookup.<br /> " + e.toString());
            }
            logger.info("Testing checkout");
            int indexnp = cargoNotes.indexOf("N/P");
            logger.info(inData.getContainerNumber() + " - NIS_TRUCKER: " + inData.getCargoNotes().substring(indexnp + 4, indexnp + 8));
            String notParty = inData.getCargoNotes().substring(indexnp + 4, indexnp + 8).replace(",", "");
            if (tosLookUp.isValidTrucker(notParty) && !"S".equalsIgnoreCase(inData.getDsc())) {
                data.setUnitFlex7(notParty);
            }
            if (tosLookUp != null) {
                tosLookUp.close();
                tosLookUp = null;
            }
        }

        //Alaska chnage
        //priority stow
        data.setUnitFlex8(inData.getPriorityStow());

        if (data.getUnitFlex1() == null && data.getUnitFlex12() == null && data.getUnitFlex3() == null && data.getUnitFlex8() == null)
            return;
        outputText.add(createTextRecord(data));
    }

    private void createUfvFlexData(TosRdsDataFinalMt inData) {
        NewVes data = new NewVes();
        data.setRecType("ufv-flex");
        String pmd = inData.getPmd();
        if (pmd != null) {
            if (pmd.equalsIgnoreCase("TX") || pmd.equalsIgnoreCase("MG"))
                data.setUfvFlex2(pmd);
            Date today = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("MM");
            String month = sdf.format(today);
            sdf = new SimpleDateFormat("yyyy");
            String year = sdf.format(today);
            data.setUfvFlexDate1(month + pmd + year);
        }
        String vessAvailableDate = "";
        String dueDate = "";
        Date tempDate = inData.getArrdate();
        if (tempDate != null)
            vessAvailableDate = CalendarUtil.convertDateToString(tempDate);
        dueDate = inData.getMisc3();
        if (dueDate != null && dueDate.length() > 0) {
            data.setUfvFlexDate2(vessAvailableDate);
            data.setUfvFlexDate3(dueDate);
        }


        //setting KFF to ufvFlexString07
        if (inData.getTemp2() != null && ("KFF".equalsIgnoreCase(inData.getTemp2()) || "AMB".equalsIgnoreCase(inData.getTemp2()))) {
            data.setUfvFlex7(inData.getTemp2());
        }

        if (inData.getTemp() != null) {
            //logger.debug("Temp1 value is KFF or AMB  "+inData.getTemp());
            data.setUfvFlex7(inData.getTemp());
        }

        if (data.getUfvFlex2() == null && data.getUfvFlexDate1() == null &&
                data.getUfvFlexDate2() == null && data.getUfvFlexDate3() == null && data.getUfvFlex7() == null)
            return;
        outputText.add(createTextRecord(data));
    }

    private void createHoldDataForBarge(TosRdsDataFinalMt inData) {
        String ctrno = inData.getContainerNumber();
        for (int i = 0; i < stowCntrData.size(); i++) {
            TosStowPlanCntrMt stowCntrMt = stowCntrData.get(i);
            String ctrNbr = stowCntrMt.getContainerNumber();
            if (ctrNbr != null && ctrNbr.equalsIgnoreCase(ctrno)) {
                Set<TosStowPlanHoldMt> stowHoldMt = stowCntrMt.getTosStowPlanHoldMts();
                Iterator<TosStowPlanHoldMt> itrHold = stowHoldMt.iterator();
                while (itrHold.hasNext()) {
                    TosStowPlanHoldMt holdData = itrHold.next();
                    NewVes data = new NewVes();
                    data.setRecType("hold");
                    data.setHoldId(holdData.getId().getCode());
                    outputText.add(createTextRecord(data));
                }

            }
        }

    }

    private void createEquipmentData(TosRdsDataFinalMt inData, TosStowPlanChassisMt bareChd, String type) {
        if (type.equals(ROB) || type.equals(CNTR)) {
            createEquipmentContainer(inData);
            if (inData.getChassisNumber() != null && inData.getChassisNumber().length() > 0)
                createEquipmentChassis(inData);
        } else if (type.equals(BARE)) {
            createEquipmentBare(bareChd);
            if (bareChd.getMgNumber() != null && bareChd.getMgNumber().length() > 0)
                createEquipmentAcc(bareChd);
        }
    }

    private void createEquipmentContainer(TosRdsDataFinalMt inData) {
        NewVes data = new NewVes();
        data.setRecType("equipment");
        String checkDigit = inData.getCheckDigit();
        checkDigit = checkDigit == null ? "X" : checkDigit;
        if (StringUtils.isAlpha(inData.getContainerNumber().substring(0, 1))) { // added by Meena
            data.setEqid(inData.getContainerNumber() + checkDigit);
        } else {
            data.setEqid(inData.getOwner() + inData.getContainerNumber() + checkDigit);
        }
        String tCode = inData.getTypeCode();
        if (tCode != null) {
            if (tCode.substring(0, 1).equalsIgnoreCase("C"))
                data.setClazz("CHS");
            else
                data.setClazz("CTR");
        }
        String hgt = inData.getHgt();
        data.setType(inData.getTypeCode());
        if (hgt != null && hgt.length() > 4) {
            hgt = hgt.substring(0, 4);
            if (!isNumber(hgt.substring(0, 2)) || !isNumber(hgt.substring(2, 4))) {
                if (tCode != null && tCode.equals("UNKNOWN") && data.getClazz().equals("CTR"))
                    data.setType("UNKN");
                else if (tCode != null && tCode.equals("UNKNOWN") && data.getClazz().equals("CHS"))
                    data.setType("UNK");
                else
                    data.setType(tCode.length() > 3 ? tCode.substring(0, 3) : tCode);
            } else {
                int hgtinch;
                String temp1 = hgt.substring(0, 2);
                String temp2 = hgt.substring(2, 4);
                hgtinch = Integer.parseInt(temp1) * 12 + Integer.parseInt(temp2);
                if (tCode != null) {
                    if (tCode.equals("UNKNOWN")) {
                        if (data.getClazz().equals("CHS"))
                            data.setType("UNK");
                        else
                            data.setType("UNKN");
                    } else if (tCode.substring(0, 1).equals("A")) {
                        if (hgtinch <= 138)
                            data.setType(tCode.substring(0, 3) + "L");
                        else if (hgtinch >= 152)
                            data.setType(tCode.substring(0, 3) + "H");
                        else
                            data.setType(tCode.substring(0, 3));
                    } else if (tCode.substring(0, 1).equals("F")) {
                        if (hgt.equals("1300"))
                            data.setType(tCode.substring(0, 3) + "M");
                        else if (hgtinch <= 96)
                            data.setType(tCode.substring(0, 3) + "L");
                        else if (hgtinch > 102)
                            data.setType(tCode.substring(0, 3) + "H");
                        else
                            data.setType(tCode.substring(0, 3));
                    } else if (tCode.substring(0, 1).equals("R")) {
                        if (hgtinch <= 96)
                            data.setType(tCode.substring(0, 3) + "L");
                        else if (hgtinch > 102)
                            data.setType(tCode.substring(0, 3) + "H");
                        else
                            data.setType(tCode.substring(0, 3));
                    } else {
                        if (hgtinch <= 96)
                            data.setType(tCode.substring(0, 3) + "L");
                        else if (hgtinch > 102)
                            data.setType(tCode.substring(0, 3) + "H");
                        else
                            data.setType(tCode.substring(0, 3));
                    }
                    String subGrp = "";
                    String temp = tCode.substring(6, 8);
                    //logger.info("before validate the equipment grade subGrp " +  subGrp +"temp "+temp);
                    subGrp = validateEquipmentGrade(temp);
                    logger.info("After validate the equipment grade subGrp " + subGrp + "temp " + temp);
					/*if(temp.equals("4V"))
						subGrp = "4V";
					else if(temp.equals("CL"))
						subGrp = "CL";
					else if(temp.equals("DV"))
						subGrp = "DV";
					else if(temp.equals("FC"))
						subGrp = "FC";
					else if(temp.equals("GB"))
						subGrp = "GB";
					else if(temp.equals("GR"))
						subGrp = "GR";
					else if(temp.equals("GY"))
						subGrp = "GY";
					else if(temp.equals("H3"))
						subGrp = "H3";
					else if(temp.equals("H4"))
						subGrp = "H4";
					else if(temp.equals("HG"))
						subGrp = "HG";
					else if(temp.equals("CA"))
						subGrp = "CA";
					else if(temp.equals("VO"))
						subGrp = "VO";*/

                    if (currentVesvoyType.equals(PRIMARY)) {
                        if (!subGrp.equals("")) {
                            if (data.getType().length() == 3)
                                data.setType(data.getType() + " " + subGrp);
                            else
                                data.setType(data.getType() + subGrp);
                        }
                    }
                }
            }
        }
        data.setRole("PRIMARY");
        BigDecimal twt = inData.getTareWeight();
        if (twt != null) {
            double ntwt = twt.doubleValue();
            data.setTareKg(convertPoundsToKG(ntwt));
        }

        String hgt1 = inData.getHgt();
        if (hgt1 != null && hgt1.length() >= 6) {
            double feet = Double.parseDouble(hgt1.substring(0, 2));
            double inches = Double.parseDouble(hgt1.substring(2, 4) + "." + hgt1.substring(4, 6));
            double totalinches = (feet * 12) + inches;
            totalinches = totalinches * 25.4;
            data.setHeightMm("" + new DecimalFormat("##.#").format(totalinches));
        }
        if (hgt1 != null && !hgt1.endsWith("00")) {
            data.setHeightMm(hgt1);
        }
        String strength = inData.getStrength();
        if (strength != null && !strength.equalsIgnoreCase("")) {
            if (strength.length() >= 2 && strength.startsWith("I")) {
                strength = strength.substring(1);
            }
            if (strength.length() > 2) {
                data.setStrengthCode(strength.substring(1, strength.length()));
            } else {
                data.setStrengthCode(strength);
            }
        } else {
            if ("barge".equalsIgnoreCase(processType))
                data.setStrengthCode("A");
        }
        String locationRowDeck = inData.getLocationRowDeck();
        locationRowDeck = locationRowDeck == null ? "" : locationRowDeck;
        if (locationRowDeck.equals(""))
            data.setOperator("MAT");
        else
            data.setOperator(locationRowDeck);
        data.setOwner(inData.getOwner());
        String srv = inData.getSrv();
        if (srv != null && !srv.equalsIgnoreCase("MAT") && !srv.equalsIgnoreCase("CSX"))
            data.setEqFlex01("CLIENT");
        else if ("barge".equalsIgnoreCase(processType))
            data.setEqFlex01("");
        else
            data.setEqFlex01("MAT");
        if (tCode != null && tCode.length() > 6) {
            if (tCode.substring(6, tCode.length()).equals("ST"))
                data.setMaterial("STEEL");
            else if (tCode.substring(6, tCode.length()).equals("AL"))
                data.setMaterial("ALUMINUM");
        }
        outputText.add(createTextRecord(data));
        String crstatus = inData.getCrstatus();
        if (crstatus != null && crstatus.length() > 0) {
            List<String> holdsList = (List<String>) Arrays.asList(crstatus.split(" "));
            Collections.sort(holdsList);
            for (int i = 0; i < holdsList.size(); i++) {
                NewVes data1 = new NewVes();
                data1.setRecType("hold");
                String stat = holdsList.get(i);
                if (stat.trim().length() > 0 && !stat.trim().equals("OK")) {
                    data1.setHoldId(stat.trim());
                    outputText.add(createTextRecord(data1));
                }
            }
        }

        // Damage record
        NewVes data2 = new NewVes();
        data2.setRecType("damage");
        String dcode = inData.getDamageCode();
        if (dcode != null) {
            if (!dcode.equalsIgnoreCase("Z")) {
                data2.setDamageType("UNKNOWN");
                data2.setComponent("UNKNOWN");
                if (dcode.equalsIgnoreCase("L") || dcode.equalsIgnoreCase("M"))
                    data2.setSeverity("MINOR");
                else if (dcode.equalsIgnoreCase("H"))
                    data2.setSeverity("MAJOR");
                else
                    data2.setSeverity("NONE");
            }
        }
        outputText.add(createTextRecord(data2));
    }

    private void createEquipmentChassis(TosRdsDataFinalMt inData) {
        String chassisNbr = inData.getChassisNumber();
        for (int i = 0; i < stowChassisData.size(); i++) {
            TosStowPlanChassisMt stowChsD = stowChassisData.get(i);
            String chsNbr = stowChsD.getChassisNumber();
            if (chsNbr != null && chsNbr.equalsIgnoreCase(chassisNbr)) {
                NewVes data = new NewVes();
                data.setRecType("equipment");
                data.setEqid(stowChsD.getChassisNumber() + stowChsD.getChassisCd());
                data.setClazz("CHS");
                String typeCode = stowChsD.getTypeCode();
                if (typeCode != null && typeCode.length() > 0)
                    data.setType(typeCode.substring(0, 3));
                else
                    data.setType("CXX");
                data.setRole("CARRIAGE");
                BigDecimal tare = stowChsD.getChassisTare();
                if (tare.doubleValue() > 0)
                    data.setTareKg(convertPoundsToKG(tare.doubleValue()));
                data.setOwner(stowChsD.getOwner());
                outputText.add(createTextRecord(data));
                if (stowChsD.getMgNumber() != null && stowChsD.getMgNumber().length() > 0) {
                    data = new NewVes();
                    data.setRecType("equipment");
                    data.setEqid(stowChsD.getMgNumber());
                    data.setClazz("ACC");
                    data.setType("MG01");
                    data.setRole("ACCESSORY_ON_CHS");
                    BigDecimal tareC = stowChsD.getMgTare();
                    if (tare.doubleValue() > 0)
                        data.setTareKg(convertPoundsToKG(tareC.doubleValue()));
                    outputText.add(createTextRecord(data));
                }
                createChassisHoldDamageData(stowChsD, "CHS");
            }
        }
    }

    private void createChassisHoldDamageData(TosStowPlanChassisMt stowChsD, String type) {
        if (stowChsD.getChassisAlert() != null && stowChsD.getChassisAlert().length() > 0) {
            NewVes data = new NewVes();
            data.setRecType("hold");
            data.setHoldId("RDCH");
            outputText.add(createTextRecord(data));
        }
        NewVes data2 = new NewVes();
        data2.setRecType("damage");
        String dcode = stowChsD.getDamageCode();
        if (dcode != null && dcode.trim().length() > 0) {
            if (type.equals(BARE)) {
                if (!dcode.equalsIgnoreCase("Z")) {
                    data2.setDamageType("UNKNOWN");
                    data2.setComponent("UNKNOWN");
                    if (dcode.equalsIgnoreCase("L") || dcode.equalsIgnoreCase("M"))
                        data2.setSeverity("MINOR");
                    else if (dcode.equalsIgnoreCase("H"))
                        data2.setSeverity("MAJOR");
                    else
                        data2.setSeverity("NONE");
                }
            } else if (type.equals("CHS")) {
                data2.setDamageType("UNKNOWN");
                data2.setComponent("UNKNOWN");
                if (dcode.equalsIgnoreCase("L") || dcode.equalsIgnoreCase("M"))
                    data2.setSeverity("MINOR");
                else if (dcode.equalsIgnoreCase("H"))
                    data2.setSeverity("MAJOR");
                else
                    data2.setSeverity("NONE");
            }
        }
        outputText.add(createTextRecord(data2));
    }

    private void createEquipmentBare(TosStowPlanChassisMt bareChd) {
        NewVes data = new NewVes();
        data.setRecType("equipment");
        data.setEqid(bareChd.getChassisNumber() + bareChd.getChassisCd());
        data.setClazz("CHS");
        String typeCode = bareChd.getTypeCode();
        if (typeCode != null && typeCode.length() > 0)
            data.setType(typeCode.substring(0, 3));
        else
            data.setType("CXX");
        data.setRole("PRIMARY");
        BigDecimal tare = bareChd.getChassisTare();
        if (tare.doubleValue() > 0)
            data.setTareKg(convertPoundsToKG(tare.doubleValue()));
        data.setOwner(bareChd.getOwner());
        data.setOperator("MAT");
        String srv = bareChd.getSrv();
        if (srv != null && !srv.equalsIgnoreCase("MAT") && !srv.equalsIgnoreCase("CSX"))
            data.setEqFlex01("CLIENT");
        else
            data.setEqFlex01("MAT");
        if ("barge".equalsIgnoreCase(processType))
            data.setEqFlex01("");
        outputText.add(createTextRecord(data));
        createChassisHoldDamageData(bareChd, BARE);
    }

    private void createEquipmentAcc(TosStowPlanChassisMt bareChd) {
        NewVes data = new NewVes();
        data.setRecType("equipment");
        data.setEqid(bareChd.getMgNumber());
        data.setClazz("ACC");
        data.setType("MG01");
        data.setRole("ACCESSORY_ON_CHS");
        BigDecimal tare = bareChd.getMgTare();
        if (tare.doubleValue() > 0)
            data.setTareKg(convertPoundsToKG(tare.doubleValue()));
        outputText.add(createTextRecord(data));
    }

    private void createHazardDataForCsx(TosStowPlanHazMt hazData) {
        NewVes data = new NewVes();
        data.setRecType("hazard");
        data.setImdg(hazData.getHazClass());
        data.setUn("" + hazData.getUnNumber());
        data.setTechName(hazData.getDescription());
        data.setProperName(hazData.getDescription());
        outputText.add(createTextRecord(data));
    }

    private void createHazardData(TosDcmMt inData) {
        NewVes data = new NewVes();
        data.setRecType("hazard");
        //psethuraman : set imdg class 'X' if blank
        if (inData != null && inData.getHazClass() != null && !inData.getHazClass().isEmpty()) {
            data.setImdg(inData.getHazClass());
        } else {
            data.setImdg("X");
        }
        data.setUn(inData.getHazardCode());
        String hazCodeType = inData.getHazardCodeType();
        hazCodeType = hazCodeType == null ? "" : hazCodeType;
        if (hazCodeType.equals(""))
            data.setNbrType("UN");
        else
            data.setNbrType(hazCodeType);
        String remarks = inData.getRemarks();
        remarks = remarks == null ? "" : remarks;
        //logger.info("remarks:"+remarks +" for container#:"+inData.getContainerNumber());
        if (remarks.length() > 0) {
            //logger.info(inData.getContainerNumber()+ " CREATE HAZARD DATA - REMARKS -->" + remarks);
            remarks = remarks.toUpperCase();
            if (remarks.contains("LIMITED QUANTITY") || (remarks.contains("LTD") && remarks.contains("QTY")))                                                                // Commented : should not check LTD alone
            {
                data.setLtdQtyFlag("Y");
                String tempStr = remarks.replaceAll("LIMITED QUANTITY", "").trim();
                tempStr = tempStr.replaceAll("LTD\\.", "").trim();
                tempStr = tempStr.replaceAll("LTD", "").trim();
                tempStr = tempStr.replaceAll("QTY\\.", "").trim();
                tempStr = tempStr.replaceAll("QTY", "").trim();
                tempStr = tempStr.replaceAll(",", "").trim();
                //tempStr = tempStr.replaceAll("\\.", "").trim();
                remarks = tempStr.trim();
            }
            if (remarks.indexOf("MARINE") != -1 && remarks.indexOf("POLLUTANT") != -1) {
                data.setMarinePollutants("Y");
                remarks = remarks.replaceAll("MARINE", "");
                remarks = remarks.replaceAll("POLLUTANT", "");
            }
        }
        data.setRemark(remarks.trim().toUpperCase());

        String packNum = inData.getPackNum();
        packNum = packNum == null ? "" : packNum;
        String shname = inData.getShippingName();
        shname = shname == null ? "" : shname;
        if (packNum.length() > 0) {
            ArrayList<String> fields = new ArrayList<String>(Arrays.asList(packNum.split(" ")));
            if (fields.size() > 1) {
                String qty = packNum.substring(0, packNum.indexOf(" "));
                String type = packNum.substring(packNum.indexOf(" ") + 1);
                if (qty != null && qty.length() > 0) {
                    data.setQuantity(qty);
                    data.setPackageType(type.length() >= 7 ? type.substring(0, 7) : type);
                } else {
                    data.setQuantity("1");
                    data.setPackageType(type.length() >= 7 ? type.substring(0, 7) : type);
                }
            } else {
                fields = new ArrayList<String>(Arrays.asList(packNum.split("X")));
                if (fields.size() > 1) {
                    String qty = packNum.substring(0, packNum.indexOf("X"));
                    String type = packNum.substring(packNum.indexOf("X") + 1);
                    if (qty != null && qty.length() > 0) {
                        data.setQuantity(qty);
                        data.setPackageType(type.length() >= 7 ? type.substring(0, 7) : type);
                    } else {
                        data.setQuantity("1");
                        data.setPackageType(type.length() >= 7 ? type.substring(0, 7) : type);
                    }
                } else {
                    Scanner scr = new Scanner(packNum);
                    if (scr.hasNextInt()) {
                        int qty = scr.nextInt();
                        data.setQuantity("" + qty);
                        if (shname.contains("VEHICLE")) {
                            if (qty > 1)
                                data.setPackageType("AUTOS");
                            else
                                data.setPackageType("AUTO");
                        } else {
                            if (qty > 1)
                                data.setPackageType("UNITS");
                            else
                                data.setPackageType("UNIT");
                        }
                    } else {
                        data.setQuantity("1");
                        data.setPackageType(packNum.length() >= 7 ? packNum.substring(0, 7) : packNum);
                    }
                }
            }
        } else {
            data.setQuantity("1");
            if (shname.contains("VEHICLE"))
                data.setPackageType("AUTO");
            else
                data.setPackageType("UNIT");
        }
        if (data.getPackageType() != null)
            data.setPackageType(data.getPackageType().toUpperCase());
        String tempFlp = inData.getFlashPoint();
        tempFlp = tempFlp == null ? "" : tempFlp;
        String tempFlpU = "C";
        while (true) {
            if (tempFlp.startsWith("<") || tempFlp.startsWith("+")) {
                tempFlp = tempFlp.substring(1, tempFlp.length());
            }
            if (!tempFlp.equals("")) {
                if (tempFlp.endsWith(" ")) {
                    tempFlp = tempFlp.substring(0, tempFlp.length() - 1);
                } else if (tempFlp.endsWith("C") || tempFlp.endsWith("F") || (!isNumber(tempFlp.substring(tempFlp.length() - 1, tempFlp.length())))) {
                    tempFlpU = tempFlp.substring(tempFlp.length() - 1, tempFlp.length());
                    tempFlp = tempFlp.substring(0, tempFlp.length() - 1);
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        try {
            Double temperature = Double.parseDouble(tempFlp);
            String tempAfterConversion = tempFlpU != null && "F".equalsIgnoreCase(tempFlpU) ? convertFtoC(temperature) : temperature.toString();
            data.setFlashPoint(tempAfterConversion + "");
        } catch (Exception e) {
            logger.error("Temperature is not numeric" + tempFlp + " for container:" + data.getEqid());
            data.setFlashPoint("");
        }


        data.setProperName(inData.getShippingName());
        data.setTechName(inData.getTechnicalName());
        if (inData.getPackagingMarks() != null && !inData.getPackagingMarks().isEmpty()) {
            String packagingGrpMarks = inData.getPackagingMarks().trim();
            if (packagingGrpMarks.equalsIgnoreCase("1"))
                packagingGrpMarks = "I";
            if (packagingGrpMarks.equalsIgnoreCase("2"))
                packagingGrpMarks = "II";
            if (packagingGrpMarks.equalsIgnoreCase("3"))
                packagingGrpMarks = "III";
            if (packagingGrpMarks.trim().equalsIgnoreCase("I") ||
                    packagingGrpMarks.trim().equalsIgnoreCase("II") ||
                    packagingGrpMarks.trim().equalsIgnoreCase("III")) {
                packagingGrpMarks = packagingGrpMarks.trim();
                data.setPackingGroup(packagingGrpMarks);
            }
        }
        String gw = inData.getGrossWeight();
        gw = gw == null ? "" : gw;
        String gwu = inData.getGrossWgUnit();
        gwu = gwu == null ? "" : gwu;
        if (gw.length() > 0) {
            if (gwu.equalsIgnoreCase("KG"))
                data.setHazardWeightKg(gw);
            else {
                String grossWt = gw.replace(".", "");
                if (isNumber(grossWt)) {
                    double weight = Double.parseDouble(gw);
                    //String newW = new DecimalFormat("##.########").format(weight).toString();
                    data.setHazardWeightKg(convertPoundsToKG(weight));
                } else {
                    data.setHazardWeightKg(gw);
                }
            }
        } else
            data.setHazardWeightKg("0");
        String phone = inData.getPhone();
        //if(phone != null && phone.length()>=10) //commented by Meena
        //	phone = phone.substring(0, 3) + " " +  phone.substring(3, 6) + " " +  phone.substring(6, 10);//commented by Meena
        data.setEmergencyTelephone(phone);
        String subClass = inData.getSubClass();
        subClass = subClass == null ? "" : subClass;
        ArrayList<String> tempList = new ArrayList<String>(Arrays.asList(subClass.split(",")));
        if (tempList != null) {
            if (tempList.size() > 1) {
                data.setSecondaryImo1(tempList.get(0));
                data.setSecondaryImo2(tempList.get(1));
            } else {
                data.setSecondaryImo1(subClass);
            }
        }
        outputText.add(createTextRecord(data));
    }

    private void createOogData(TosRdsDataFinalMt inData) {
        String oog = inData.getOdf();
        if (oog != null && oog.equalsIgnoreCase("Y")) {
            NewVes data = new NewVes();
            data.setRecType("oog");
            String ctrno = inData.getContainerNumber();
            for (int j = 0; j < stowCntrData.size(); j++) {
                TosStowPlanCntrMt stowCntrD = stowCntrData.get(j);
                String cctrno = stowCntrD.getContainerNumber();
                if (cctrno != null && (cctrno.equalsIgnoreCase(ctrno) || cctrno.equalsIgnoreCase(ctrno.substring(4, ctrno.length())))) {
                    BigDecimal ovrHieght = stowCntrD.getOversizeHeightInches();
                    if (ovrHieght.doubleValue() > 0)
                        data.setTopCm(convertInchesToCm(ovrHieght.doubleValue()));
                    else
                        data.setTopCm("0");
                    BigDecimal ovrLeft = stowCntrD.getOversizeLeftInches();
                    if (ovrLeft.doubleValue() > 0)
                        data.setLeftCm(convertInchesToCm(ovrLeft.doubleValue()));
                    else
                        data.setLeftCm("0");
                    BigDecimal ovrRight = stowCntrD.getOversizeRightInches();
                    if (ovrRight.doubleValue() > 0)
                        data.setRightCm(convertInchesToCm(ovrRight.doubleValue()));
                    else
                        data.setRightCm("0");
                    BigDecimal ovrFront = stowCntrD.getOversizeFrontInches();
                    if (ovrFront.doubleValue() > 0)
                        data.setFrontCm(convertInchesToCm(ovrFront.doubleValue()));
                    else
                        data.setFrontCm("0");
                    BigDecimal ovrRear = stowCntrD.getOversizeRearInches();
                    if (ovrRear.doubleValue() > 0)
                        data.setBackCm(convertInchesToCm(ovrRear.doubleValue()));
                    else
                        data.setBackCm("0");
                }
            }
            outputText.add(createTextRecord(data));
        }
    }

    private void createPositionData(TosRdsDataFinalMt inData, TosStowPlanChassisMt bareChd, String type) {
        NewVes data = new NewVes();
        data.setRecType("position");
        data.setLocType("VESSEL");
        if (type.equals(ROB) || type.equals(CNTR)) {
            String srv = inData.getSrv();
            if (srv != null && srv.equalsIgnoreCase("MAT") && srv.equalsIgnoreCase("CSX")) {
                data.setLocation(inData.getVesvoy() != null ? inData.getVesvoy() : "VESSEL");
            } else {
                if (inData.getMisc1() != null)
                    data.setLocation(inData.getVesvoy());//Need to set outbound vesvoy
                else if (inData.getVesvoy() != null)
                    data.setLocation(inData.getVesvoy());//Need to set outbound vesvoy
                else
                    data.setLocation("VESSEL");
            }
            data.setSlot(inData.getCell());
            if ("barge".equalsIgnoreCase(processType)) {
                // logger.info("Inputs for barge "+inData.getVesvoy().substring(0,
                // 3)+"-"+inData.getLoadPort());
                // String vvd =
                // CommonBusinessProcessor.getNextOutboundVesvoyfromNItoHon(inData.getVesvoy().substring(0,
                // 3), inData.getLoadPort(), new Date());
                // inData.getActualVessel() + inData.getActualVoyage() +
                // inData.getLeg();
                try {
                    if (tosLookUp == null)
                        tosLookUp = new TosLookup();
                    bargeCarrierId = tosLookUp.getNextObVesvoyToHon(inData.getVesvoy() + inData.getLeg());
                } catch (Exception e) {
                    nvLogger.addError(inData.getVesvoy() + inData.getLeg(), "", "Unable to create TosLookUp.\n" + e);
                }
                String vvd = bargeCarrierId;
                if (srv != null && srv.equalsIgnoreCase("MAT") && srv.equalsIgnoreCase("CSX")) {
                    data.setLocation(vvd != null ? vvd : "VESSEL");
                } else {
                    if (inData.getMisc1() != null)
                        data.setLocation(vvd);// Need to set outbound vesvoy
                    else if (inData.getVesvoy() != null)
                        data.setLocation(vvd);// Need to set outbound vesvoy
                    else
                        data.setLocation("VESSEL");
                }
            }
        } else if (type.equals(BARE)) {
            String srv = bareChd.getSrv();
            if ("barge".equalsIgnoreCase(processType)) {
                try {
                    if (tosLookUp == null)
                        tosLookUp = new TosLookup();
                    bargeCarrierId = tosLookUp.getNextObVesvoyToHon(inData.getVesvoy() + inData.getLeg());
                } catch (Exception e) {
                    nvLogger.addError(inData.getVesvoy() + inData.getLeg(), "", "Unable to create TosLookUp.\n" + e);
                }
                String vvd = bargeCarrierId;
                if (srv != null && srv.equalsIgnoreCase("MAT") && srv.equalsIgnoreCase("CSX")) {
                    data.setLocation(vvd != null ? vvd : "VESSEL");
                } else {
                    if (vvd != null)
                        data.setLocation(vvd);//Need to set outbound vesvoy
                    else
                        data.setLocation("VESSEL");
                }
                data.setSlot(bareChd.getLoc());
            } else {
                if (bareChd.getVesvoy() != null) {
                    data.setLocation(bareChd.getVesvoy());
                    data.setSlot(bareChd.getLoc());
                } else
                    data.setLocation("VESSEL");
            }
        }
        outputText.add(createTextRecord(data));

    }

    private void createReeferData(TosRdsDataFinalMt inData) {
        NewVes data = new NewVes();
        data.setRecType("reefer");

        String temp = inData.getTemp();
        if (temp != null && temp.length() > 0 && !("KFF".equalsIgnoreCase(temp) || "AMB".equalsIgnoreCase(temp))) {

            String tempUnit = inData.getTempMeasurementUnit();
            if (tempUnit != null && "C".equalsIgnoreCase(tempUnit)) {
            } else {
                logger.info("temp" + temp);
                double tm = Double.parseDouble(temp);
                temp = convertFtoC(tm);
                tempUnit = "F";
            }
            if (temp != null && !temp.equals("")) {
                data.setTempReqdC(temp);

                //Alaska change
                //Setting temp1 to TempMinC
                data.setTempMinC(temp);

                data.setTempDisplayUnit(tempUnit);
                outputText.add(createTextRecord(data));
            }
        }

        //Alaska change start
        String temp2 = inData.getTemp2();
        if (temp2 != null && temp2.length() > 0 && !("KFF".equalsIgnoreCase(temp2) || "AMB".equalsIgnoreCase(temp2))) {

            String tempUnit = inData.getTempMeasurementUnit();
            if (tempUnit != null && "C".equalsIgnoreCase(tempUnit)) {
            } else {
                double tm = Double.parseDouble(temp2);
                temp2 = convertFtoC(tm);
                tempUnit = "F";
            }
            if (temp2 != null && !temp2.equals("")) {
                data.setTempMaxC(temp2);
                data.setTempDisplayUnit(tempUnit);
                outputText.add(createTextRecord(data));
            }
        }
        //Alaska change - end

    }

    private void createRoutingData(TosRdsDataFinalMt inData, TosStowPlanChassisMt bareChd, String type, boolean tranToImp) {
        NewVes data = new NewVes();
        data.setRecType("routing");
        TosDestPodData refData = null;
        if (type.equals(ROB) || type.equals(CNTR)) {
            String shiperPool = inData.getShipperPool();
            if (shiperPool != null && shiperPool.length() > 0)
                data.setOpl(shiperPool);
            else
                data.setOpl(inData.getLoadPort());
            logger.info("discharge port:" + inData.getDport() + " for container#:" + inData.getContainerNumber());
            String pod1 = "";
            String pod2 = "";
            try {
                //Set Pod-1 value from TosDestPodData by sending Dport as input
                refData = (TosDestPodData) RefDataLookup.queryById("com.matson.cas.refdata.mapping.TosDestPodData", inData.getDport());
                logger.info("refdata from TosDestPodData" + refData);
                if (refData != null) {
                    pod2 = refData.getPod2();
                    logger.info("POD-1 and POD-2 from refdata :" + refData.getPod1() + " - " + refData.getPod2());
                }
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Error while reading TosDestPodData ");
            }
            if (!"barge".equalsIgnoreCase(processType)) {
                if (refData != null && !"OPT".equals(inData.getDischargePort())) {
                    pod1 = refData.getPod1();
                    logger.info("POD1 FROM TABLE :" + pod1);
                } else {


                    pod1 = inData.getDischargePort();
                    logger.info("POD1 FROM DISCPORT :" + pod1);
                }
                if ("OPT".equalsIgnoreCase(inData.getDport())) {
                    pod1 = inData.getDport();
                }

                if (("S").equalsIgnoreCase(inData.getDsc())) {

                    logger.debug("inside SIT condition");
                    pod1 = "ANK";
                }

            } else {
                if (!"OPT".equals(inData.getDischargePort())
                        && !CommonBusinessProcessor.vaidateDischargePort(inData
                        .getDischargePort())) {
                    pod1 = refData.getPod1();
                } else {
                    pod1 = inData.getDischargePort();
                }
            }

            logger.debug("setting pod to" + pod1);
            data.setPod1(pod1);
            data.setPol(inData.getLoadPort());
            data.setPod2(pod2);
            if (SUPPLEMENT.equalsIgnoreCase(currentVesvoyType)) {
                try {
                    if (tosLookUp == null) {
                        tosLookUp = new TosLookup();
                    }
                    String isdeparted = tosLookUp.isLatestUnitDeparted(inData.getContainerNumber());
                    if ("true".equalsIgnoreCase(isdeparted)) {
                        String loadport = tosLookUp.getPOLForDepartedUnit(inData.getContainerNumber());
                        data.setPol(loadport);
                    }

                } catch (Exception ex) {
                    logger.info("Issue in getting TOS lookup");
                } finally {
                    if (tosLookUp != null) {
                        tosLookUp.close();
                        tosLookUp = null;
                    }
                }
            }


            //if(!"barge".equalsIgnoreCase(processType))
            data.setDestination(inData.getDport());
            String truck = inData.getTruck();
            truck = truck == null ? "" : truck;
            String dir = inData.getDir();
            dir = dir == null ? "" : dir;
            String dport = inData.getDport();
            dport = dport == null ? "" : dport;
            String planDisp = inData.getPlanDisp();
            planDisp = planDisp == null ? "" : planDisp;
            String commodity = inData.getCommodity();
            commodity = commodity == null ? "" : commodity;
            String misc1 = inData.getMisc1();
            misc1 = misc1 == null ? "" : misc1;
            String locationStatus = inData.getLocationStatus();
            locationStatus = locationStatus == null ? "" : locationStatus;
            String locationRowDeck = inData.getLocationRowDeck();
            locationRowDeck = locationRowDeck == null ? "" : locationRowDeck;
            String cargoNotes = inData.getCargoNotes();
            cargoNotes = cargoNotes == null ? "" : cargoNotes;
            String crstatus = inData.getCrstatus();
            crstatus = crstatus == null ? "" : crstatus;
            String owner = inData.getOwner();
            owner = owner == null ? "" : owner;
			/*if(("HON".equalsIgnoreCase(dport) || CommonBusinessProcessor.isValidNeighborIslandPort(dport) || "S".equals(inData.getDsc()))&& !"O/P".equalsIgnoreCase(truck) && "CY".equalsIgnoreCase(inData.getDs()))
			{
				if(truck.equals("YBUU"))
					data.setDesignatedTrucker(misc1);
				else if(planDisp.equals("W"))
				{
					if(commodity.startsWith("WO"))
					{
						String temp = commodity.length()>5?commodity.substring(2, 5):"";
						//logger.info("temp in NewVesProc calss:"+temp+" - "+temp.length());
						//logger.info("truck in NewVesProc calss:"+truck+" - "+truck.length());
						if (temp.length()<= 0 && truck.length() > 0) {
							//logger.info("Inside If truck");
							data.setDesignatedTrucker(truck);
						} else {
							//logger.info("Inside else temp");
							data.setDesignatedTrucker(temp);
						}
					} else {
						data.setDesignatedTrucker(truck);
					}
				}
				else
					data.setDesignatedTrucker(truck);
				//logger.info("Assigned Designated-Truck:"+data.getDesignatedTrucker()+"\t"+inData.getContainerNumber()+"\t"+dport);
			}
			*/
            if (("ANK".equalsIgnoreCase(dport) || "S".equals(inData.getDsc())) && "CY".equalsIgnoreCase(inData.getDs())) {
                data.setDesignatedTrucker(truck);

            }

			/*if(CommonBusinessProcessor.isValidNeighborIslandPort(dport) && cargoNotes.contains("N/P"))
				data.setDesignatedTrucker(null);*/
            //A31 - starts
            if (SUPPLEMENT.equalsIgnoreCase(currentVesvoyType)) {
                try {
                    if (tosLookUp == null)
                        tosLookUp = new TosLookup();
                    String tosTruckCo = tosLookUp.getUnitTruckingCo(inData.getContainerNumber());
                    data.setDesignatedTrucker(tosTruckCo);
                } catch (Exception e) {
                    logger.error("Error retrieving Truck from TOS for " + inData.getContainerNumber() + "\n" + e.getMessage());
                }
            }
            //A31 - ends
            String group = "";
            if (planDisp.equals("7"))
                group = "YB";
            else if (locationStatus.equals("7") && truck.equals("YBUU"))
                group = "YB";
            else if (commodity.equals("YB"))
                group = "YB";
            else if (planDisp.equals("3")) {
                if (commodity.length() > 1) {
                    if (commodity.substring(0, 2).equals("P2"))
                        group = "XFER-P2";
                    else if (commodity.substring(0, 2).equals("53"))
                        group = "XFER-SI";
                }
            } else if (planDisp.equals("W")) {
                if (cargoNotes.contains("WEST OAHU"))
                    group = "XFER-WO";
                if (commodity != null && !"".equalsIgnoreCase(commodity) && commodity.length() > 1 && commodity.substring(0, 2).equals("WO"))
                    group = "XFER-WO";
                else if (commodity != null && !"".equalsIgnoreCase(commodity) && commodity.length() > 1 && commodity.substring(0, 2).equals("53"))
                    group = "XFER-SI";
            } else if (locationRowDeck.equals("CSX") && owner.equals("CSXU"))
                group = "PASSPASS";
            else if (crstatus.contains("TS"))
                group = "TS";
            logger.info("INside routing element for PER " + inData.getCrstatus() + " " + inData.getContainerNumber());
            if (inData.getCrstatus() != null && inData.getCrstatus().contains("PER")) {
                try {
                    if (tosLookUp == null) {
                        tosLookUp = new TosLookup();
                    }
                    HashMap<String, String> resultMap = tosLookUp.getWOSIGrpInfo(inData.getContainerNumber());
                    if (resultMap != null && resultMap.keySet().size() > 0) {
                        group = resultMap.get("GRP");
                        logger.info("group inside routing for PER " + group);
                    }
                } catch (Exception ex) {
                    logger.info("Exception in getting tos look up" + ex.toString());
                } finally {
                    if (tosLookUp != null) {
                        tosLookUp.close();
                        tosLookUp = null;
                    }
                }
            }
            data.setGroup(group);
        } else if (type.equals(BARE)) {
            data.setPol("HON");
            data.setOpl("HON");
            data.setPod1(bareChd.getDport());
            data.setDestination(bareChd.getDport());
        }
        outputText.add(createTextRecord(data));
    }

    private void createCarrierData(TosRdsDataFinalMt inData, TosStowPlanChassisMt bareChd, String type, String vesService) {
        if (type.equals(ROB) || type.equals(CNTR)) {
            //logger.info("createCarrierData-Begin --> "+inData.getContainerNumber());
            String locationStatus = inData.getLocationStatus();
            locationStatus = locationStatus == null ? "" : locationStatus;
            String dport = inData.getDport();
            dport = dport == null ? "" : dport;
            String dischPort = inData.getDischargePort();
            dischPort = dischPort == null ? "" : dischPort;
            String vesvoy = inData.getVesvoy();
            vesvoy = vesvoy == null ? "" : vesvoy;
            String hazOpenCloseFlg = inData.getHazardousOpenCloseFlag();
            hazOpenCloseFlg = inData.getTrade();
            hazOpenCloseFlg = hazOpenCloseFlg == null ? "" : hazOpenCloseFlg;
            String dir = inData.getDir();
            dir = dir == null ? "" : dir;
            String misc1 = inData.getMisc1();
            misc1 = misc1 == null ? "" : misc1;
            String srv = inData.getSrv();
            String vesSrv = srv;
            String obVesvoy1 = "";
            String outVesvoyforExport = "";
            String loadportTrade = null;
            String category = getCategory(type, dir, dischPort, dport, hazOpenCloseFlg);
            category = category == null ? "" : category;
            //logger.info("vesselAvailableDate->"+vesselAvailableDate);
            //logger.info("vesselArrivalDate->"+vesselArrivalDate);
            Date availDate = null;
            if (vesselAvailableDate != null) {
                availDate = CalendarUtil.convertStrgToDateFormat(vesselAvailableDate);
            }
            if (SUPPLEMENT.equalsIgnoreCase(currentVesvoyType)) {
                String tempStr = CommonBusinessProcessor.getAvailableDateByVVD(vesvoy, "ANK");
                logger.info("Available date during supplement for " + vesvoy + " : " + tempStr);
                if (tempStr != null) {
                    availDate = CalendarUtil.convertStrgToDateFormat(tempStr);
                }
            }

            // Changed for HLI - RP

			/*if (dport != null && "ANC".equalsIgnoreCase(dport)) {
				obVesvoy1 = "GEN_TRUCK";
				misc1 = "GEN_TRUCK";
			} else {
				obVesvoy1 = inData.getVesvoy();
				misc1 = inData.getVesvoy();
			}*/


            logger.info("obVesvoy1 + misc1 --> " + obVesvoy1 + misc1);

            // A35 - starts
            HashMap<String, String> tosCarrMap = null;
            String tosCategory = null;
            String tosTstate = null;
            String tosObActual = null;
            String tosObActualMode = null;
            String tosObDeclrd = null;
            String tosObDeclrdMode = null;
            String tosIbActual = null;
            String tosIbActualMode = null;
            String tosIbDeclrd = null;
            String tosIbDeclrdMode = null;
            if (currentVesvoyType.equals(SUPPLEMENT)) {
                try {
                    if (tosLookUp == null)
                        tosLookUp = new TosLookup();
                    String chkDgt = inData.getCheckDigit();
                    if (chkDgt == null)
                        chkDgt = "X";
                    tosCarrMap = tosLookUp.getUnitCarrierDetails(inData.getContainerNumber() + chkDgt);
                    if (tosCarrMap != null) {
                        tosCategory = tosCarrMap.get("CATEGORY");
                        tosCategory = tosCategory == null ? "" : tosCategory;
                        if (tosCategory.equalsIgnoreCase("IMPRT"))
                            tosCategory = "IMPORT";
                        else if (tosCategory.equalsIgnoreCase("EXPRT"))
                            tosCategory = "EXPORT";
                        else if (tosCategory.equalsIgnoreCase("THRGH"))
                            tosCategory = "THROUGH";
                        else if (tosCategory.equalsIgnoreCase("STRGE"))
                            tosCategory = "STORAGE";
                        else if (tosCategory.equalsIgnoreCase("TRSHP"))
                            tosCategory = "TRANSSHIP";
                        tosTstate = tosCarrMap.get("TRANSIT_STATE");
                        tosTstate = tosTstate == null ? "" : tosTstate;
                        tosObActual = tosCarrMap.get("OB_ACTUAL");
                        tosObActual = tosObActual == null ? "" : tosObActual;
                        tosObActualMode = tosCarrMap.get("OB_ACTUAL_MODE");
                        tosObActualMode = tosObActualMode == null ? "" : tosObActualMode;
                        tosObDeclrd = tosCarrMap.get("OB_DECLRD");
                        tosObDeclrd = tosObDeclrd == null ? "" : tosObDeclrd;
                        tosObDeclrdMode = tosCarrMap.get("OB_DECLRD_MODE");
                        tosObDeclrdMode = tosObDeclrdMode == null ? "" : tosObDeclrdMode;
                        tosIbActual = tosCarrMap.get("IB_ACTUAL");
                        tosIbActual = tosIbActual == null ? "" : tosIbActual;
                        tosIbActualMode = tosCarrMap.get("IB_ACTUAL_MODE");
                        tosIbActualMode = tosIbActualMode == null ? "" : tosIbActualMode;
                        tosIbDeclrd = tosCarrMap.get("IB_DECLRD");
                        tosIbDeclrd = tosIbDeclrd == null ? "" : tosIbDeclrd;
                        tosIbDeclrdMode = tosCarrMap.get("IB_DECLRD_MODE");
                        tosIbDeclrdMode = tosIbDeclrdMode == null ? "" : tosIbDeclrdMode;
                    } else {
                        logger.error("Unable to get carrier details for " + inData.getContainerNumber());
                    }
                } catch (Exception e) {
                    logger.error("Error Retreiving carrier details for " + inData.getContainerNumber());
                } finally {
                    if (tosLookUp != null) {
                        tosLookUp.close();
                        tosLookUp = null;
                    }
                }
            }
            logger.info("category <<" + tosCategory + ">> TRANSIT_STATE <<" + tosTstate + ">> DISCH_PORT <<" + dischPort
                    + ">>Ib Actual <<" + tosIbActual + ">> IB Declared <<" + tosObActual + ">> IB Actual Mode <<" + tosIbActualMode
                    + ">>Ob Actual <<" + tosObActual + ">> OB Declared <<" + tosObActual + ">> OB Actual Mode <<" + tosObActualMode);
            // A35 - ends
            String obNextVesvoy = "";
            if (currentVesvoyType.equals(SUPPLEMENT)) {
                obNextVesvoy = CommonBusinessProcessor.getNextOutboundVesselForPort("GUM", new Date());
                logger.info("Next OB Vesvoy from current date : " + obNextVesvoy);
            }
            logger.info("Process now : " + currentVesvoyType);
            for (int i = 1; i <= 2; i++) {
                for (int j = 1; j <= 2; j++) {
                    logger.info("Tos Category : " + tosCategory);

                    NewVes data1 = new NewVes();
                    data1.setRecType("carrier");
                    if (i == 1)
                        data1.setDirection("IB");
                    else if (i == 2)
                        data1.setDirection("OB");
                    if (i == 1) {
                        data1.setMode("VESSEL");
                        if (vesvoy.equals(""))
                            data1.setCarrierId1("GEN_VESSEL");
                        else {
                            data1.setCarrierId1(vesvoy);
                        }
                    } else {
                        if (tosCategory == null || tosCategory.isEmpty()) {
                            if ("ANK".equalsIgnoreCase(dischPort)) {
                                data1.setMode("TRUCK");
                                data1.setCarrierId1("GEN_TRUCK");
                            } else {
                                data1.setMode("VESSEL");
                                data1.setCarrierId1(vesvoy);
                            }
                        } else {
                            if ("IMPORT".equalsIgnoreCase(tosCategory)) {
                                data1.setMode("TRUCK");
                                data1.setCarrierId1("GEN_TRUCK");
                            } else {
                                data1.setMode("VESSEL");
                                data1.setCarrierId1(vesvoy);
                            }
                        }
                    }
                    if (j == 1)
                        data1.setQualifier("DECLARED");
                    else
                        data1.setQualifier("ACTUAL");
                    // A35 - A32 - ends
                    outputText.add(createTextRecord(data1));
                }
            }
        } else if (type.equals(BARE)) {
            String obVesvoy1 = "";
            String vesvoy = bareChd.getVesvoy();
            vesvoy = vesvoy == null ? "" : vesvoy;
            for (int o = 0; o < 2; o++) {
                NewVes data1 = new NewVes();
                data1.setRecType("carrier");
                data1.setDirection("IB");
                data1.setMode("VESSEL");
                if ("barge".equalsIgnoreCase(processType)) {
                    //logger.info("Load Port:"+bareChd.getLoadPort());
                    try {
                        if (tosLookUp == null)
                            tosLookUp = new TosLookup();
                        bargeCarrierId = tosLookUp.getNextObVesvoyToHon(inData.getVesvoy() + inData.getLeg());
                    } catch (Exception e) {
                        nvLogger.addError(inData.getVesvoy() + inData.getLeg(), "", "Unable to create TosLookUp.\n" + e);
                    }
                    obVesvoy1 = bargeCarrierId;
                }
                if (obVesvoy1 != null && !obVesvoy1.equals(""))
                    data1.setCarrierId1(obVesvoy1);
                else
                    data1.setCarrierId1(bareChd.getVesvoy());
                if (o == 0)
                    data1.setQualifier("DECLARED");
                else
                    data1.setQualifier("ACTUAL");
                outputText.add(createTextRecord(data1));
            }
            for (int p = 0; p < 2; p++) {
                NewVes data1 = new NewVes();
                data1.setRecType("carrier");
                data1.setDirection("OB");
                data1.setMode("TRUCK");
                data1.setCarrierId1("GEN_TRUCK");
                if (p == 0)
                    data1.setQualifier("DECLARED");
                else
                    data1.setQualifier("ACTUAL");
                outputText.add(createTextRecord(data1));
            }
        }
    }

    private void createBookingDate(TosRdsDataFinalMt inData) {

        if (inData.getBookingNumber() != null && inData.getBookingNumber().length() > 0) {
            NewVes data = new NewVes();
            data.setRecType("booking");
            data.setDepartureOrderNbr(inData.getBookingNumber());
            outputText.add(createTextRecord(data));
        }


    }

    private void createEofData() {
        NewVes data = new NewVes();
        data.setRecType(">>END OF FILE<<");
        outputText.add(createTextRecord(data));
    }

    private String createTextRecord(NewVes rec) {
        String temp = rec.toString();
        temp = temp.replace("null", "");
        return temp;
    }

    private String wrapStringWithQuotes(String inStr) {
        if (inStr != null && inStr.length() > 0) {
            inStr = "\"" + inStr + "\"";
            return inStr;
        }
        return "\"\"";
    }

    private String convertInchesToCm(double inches) {
        StringBuilder out = new StringBuilder();
        String temp = new DecimalFormat("##.##").format(inches * 2.54).toString();
        out.append(temp);
        return out.toString();
    }

    private String convertPoundsToKG(double pound) {
        pound = pound * 0.45359237;
        String newKG = new DecimalFormat("##.########").format(pound).toString();
        return newKG;
    }

    private String convertFtoC(double f) {
        f = ((f - 32) * 5) / 9;
        String newT = new DecimalFormat("##.##########").format(f).toString();
        return newT;
    }

    private String convertJulianDate5ToStdDate(String input) {
        String output = "";
        try {
            DateFormat fmt1 = new SimpleDateFormat("yyyyD");
            Date date = fmt1.parse(input);
            DateFormat fmt2 = new SimpleDateFormat("MM/dd/yyyy");
            output = fmt2.format(date);
        } catch (ParseException ex) {
            ex.printStackTrace();
            logger.error("Unable to convert julian date : " + input);
        }
        return output;
    }

    private boolean isNumber(String num) {
        try {
            Integer.parseInt(num);
        } catch (NumberFormatException ex) {
            return false;
        }
        return true;
    }

    private void writeToUnitTextFile(ArrayList<String> out, String fileName) {
        if (out.size() > 0) {
            try {
                logger.info("Writing containers text file " + fileName);
                FileWriter fileWriter = new FileWriter(new File(fileName));
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                for (int i = 0; i < out.size(); i++) {
                    bufferedWriter.write("\"" + (i + 1) + "\"," + out.get(i));
					/*String temp = out.get(i);
					ArrayList<String> outFields = new ArrayList<String> (Arrays.asList(temp.split(",")));
					outFields.remove(outFields.size()-1);
					for(int j=0; j<outFields.size(); j++)
					{
						String wrappedStr = wrapStringWithQuotes(outFields.get(j));
						if(j==outFields.size()-1)
							bufferedWriter.write( wrappedStr);
						else
							bufferedWriter.write( wrappedStr + ",");
					}*/
                    bufferedWriter.write(System.getProperty("line.separator"));
                }
                bufferedWriter.close();

                //writing to FTP proxy location.
                CommonBusinessProcessor.archiveNVtoFTP(new File(fileName), Integer.parseInt(TosRefDataUtil
                        .getValue("RDS_ARCH_FTP_ID")), false);
                // copying final output to exisintg new vessel location for new vessel processor to pick up.
                try {
                    if (processType.equalsIgnoreCase("Barge"))
                        COPY_BARGE = TosRefDataUtil.getValue(IS_BARGE);
                    else {
                        //if(currentVesvoyType.equalsIgnoreCase(PRIMARY))
                        //COPY_PRIMARY = TosRefDataUtil.getValue(IS_PRIMARY);
                        if (currentVesvoyType.equalsIgnoreCase(SUPPLEMENT))
                            COPY_SUPPLEMENT = TosRefDataUtil.getValue(IS_SUPPLEMENTAL);
                    }
                } catch (Exception e) {
                    logger.error("Unable to get TosAppParameters: " + IS_BARGE + "/" + IS_PRIMARY + "/" + IS_SUPPLEMENTAL);
                    return;
                }
                //if((COPY_PRIMARY.equalsIgnoreCase("true") || COPY_BARGE.equalsIgnoreCase("true")) && currentVesvoyType.equals(PRIMARY))
                // Copy only Barge and Supplmental outputs to FTP locations based on their flags
                if (COPY_BARGE.equalsIgnoreCase("true") && currentVesvoyType.equals(PRIMARY))
                    CommonBusinessProcessor.archiveNVtoFTP(new File(fileName), Integer.parseInt(TosRefDataUtil.getValue(NV_VES_FILES_FTP_ID)), true);
                if (COPY_SUPPLEMENT.equalsIgnoreCase("true") && currentVesvoyType.equals(SUPPLEMENT))
                    CommonBusinessProcessor.archiveNVtoFTP(new File(fileName), Integer.parseInt(TosRefDataUtil.getValue(SUP_FILES_FTP_ID)), true);
            } catch (Exception e) {
                logger.error("Error in writing the file " + outFilePath + outFileName);
            }
        } else {
            logger.error("No output to write into unit text file");
        }
    }

    protected boolean writeToUnitTextSupplementFiles(List<String> out, String fileName) {
        boolean processed = false;
        if (outFilePath != null && !outFilePath.endsWith("/")) {
            outFilePath = outFilePath + "/";
        }
        outFileName = outFilePath + outFileName;
        if (out.size() > 0) {
            try {
                logger.info("Writing supplement containers text file for a vessel : " + fileName);
                FileWriter fileWriter = new FileWriter(new File(fileName));
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                for (int i = 0; i < out.size(); i++) {
                    bufferedWriter.write(out.get(i));

                    bufferedWriter.write(System.getProperty("line.separator"));
                }
                bufferedWriter.close();
                //writing to FTP proxy location.
                logger.debug("Archiving supplementary text file for a vessel : " + fileName);
                CommonBusinessProcessor.archiveNVtoFTP(new File(fileName), Integer.parseInt(TosRefDataUtil
                        .getValue("RDS_ARCH_FTP_ID")), false);

                logger.debug("Copying supplementary text file for a vessel to SUPP DIR : " + COPY_SUPPLEMENT);
                CommonBusinessProcessor.archiveNVtoFTP(new File(fileName), Integer.parseInt(TosRefDataUtil.getValue(SUP_FILES_FTP_ID)), true);
                processed = true;
            } catch (Exception e) {
                processed = false;
                logger.error("Error in writing the supp file " + outFilePath + outFileName);
            }
        } else {
            logger.error("No output to write into unit text file");
        }
        return processed;
    }

	/*protected Map<String, List> populateSupplementVesselRecordsMap(String[] supFiles) {
		String eof = ">>END OF FILE<<";
		Map<String, List> vesselMap = new HashMap<String, List>();
		logger.debug( "Start seggregating Supp file: ");
		int timeout = Integer.parseInt(TosRefDataUtil.getValue( "FTP_TIMEOUT"));
		int supFtpProxyId = Integer.parseInt( TosRefDataUtil.getValue( "SUP_FILES_FTP_ID"));
		FtpProxyGetterBiz getter = new FtpProxyGetterBiz();
		getter.setTimeout(timeout);
		String ftpFileName = null;
		for ( int i=0; supFiles!=null && i<supFiles.length; i++) {
			ftpFileName = supFiles[i];
			logger.debug( "Splitting Supp file: " + ftpFileName);
			String contents = null;
			try {
				contents = getter.getFileText(supFtpProxyId, ftpFileName);
			} catch (FtpBizException e) {
				logger.error("ERROR WHILE GETTING SUPP FILES FOR SPLITTING" +e);
			}

			if ( ftpFileName.toUpperCase().endsWith("SP.TXT")
					&& (contents != null && contents.contains(eof))) {
				String[] lines = contents.split("\n");
				logger.debug( "There are " + lines.length + " lines in the file.");
				boolean newUnit = false;
				boolean foundActual = false;
				String carrierId = null;
				List <String>aUnitRecords = new ArrayList<String>();
				for (int j = 0; j < lines.length; j++) {
					String singleLine = lines[j];
					String tokens[] =  singleLine.split(",");
					if (tokens.length > 0) {
						if ("\"unit\"".equalsIgnoreCase(tokens[1])) {
							if (carrierId != null) {
								if (vesselMap.get(carrierId) != null) {
									vesselMap.get(carrierId).addAll(aUnitRecords);
								} else {
									vesselMap.put(carrierId, aUnitRecords);
								}
							}
							aUnitRecords = null;
							aUnitRecords = new ArrayList<String>();
							aUnitRecords.add(singleLine);
							foundActual = false;
						} else if (singleLine.contains(eof)) {
							if (carrierId != null) {
								if (vesselMap.get(carrierId) != null) {
									vesselMap.get(carrierId).addAll(aUnitRecords);
								} else {
									vesselMap.put(carrierId, aUnitRecords);
								}
							}
							aUnitRecords = null;
							aUnitRecords = new ArrayList<String>();
							aUnitRecords.add(singleLine);
							vesselMap.put(eof, aUnitRecords);
						} else {
							aUnitRecords.add(singleLine);
							if ("\"carrier\"".equalsIgnoreCase(tokens[1]) && tokens.length > 128) {
								if ("\"IB\"".equalsIgnoreCase(tokens[124]) && "\"VESSEL\"".equalsIgnoreCase(tokens[128])) {
									if ("\"ACTUAL\"".equalsIgnoreCase(tokens[129])) {
										carrierId = tokens[127].replace("\"","");
										foundActual = true;
										logger.info("Found Actual I/B visit : "+carrierId);
									} else if ("\"DECLARED\"".equalsIgnoreCase(tokens[129]) && !foundActual) {
										carrierId = tokens[127].replace("\"","");
										foundActual = false;
									}
								}
							}
						}
					}
				}
			}
		}
		return vesselMap;
	}*/

    protected Map<String, List<TUnit>> populateSupplementUnitsForPort(Snx snxObj) {
        Map<String, List<TUnit>> unitMap = new HashMap<String, List<TUnit>>();
        List<TUnit> ANKList = new ArrayList<TUnit>();
        List<TUnit> KDKList = new ArrayList<TUnit>();
        List<TUnit> DUTList = new ArrayList<TUnit>();
        if (snxObj != null && snxObj.getUnit() != null && snxObj.getUnit().size() > 0) {
            for (TUnit unit : snxObj.getUnit()) {
                logger.debug("Splitting xnxObj for POD : " + unit.getId());
                if (unit.getRouting() != null) {
                    if (ANK.equalsIgnoreCase(unit.getRouting().getPod1())) {
                        ANKList.add(unit);
                    } else if (KDK.equalsIgnoreCase(unit.getRouting().getPod1())) {
                        KDKList.add(unit);
                    } else if (DUT.equalsIgnoreCase(unit.getRouting().getPod1())) {
                        DUTList.add(unit);
                    }
                }
            }
        }
        unitMap.put(ANK, ANKList);
        unitMap.put(KDK, KDKList);
        unitMap.put(DUT, DUTList);
        return unitMap;
    }

    //Create files from seggregate vessel map records
    //Creating files from Map of ArrayList for each vessel
    protected void createSupplementFilesForVessel(Map<String, List> vesselMap) {
        String eof = ">>END OF FILE<<";
        Set<String> vesselKeys = vesselMap.keySet();
        for (String vesselId : vesselKeys) {
            if (eof.equalsIgnoreCase(vesselId)) {
                logger.info("Split process reached EOF " + eof);
                continue;
            }

            List<String> vesselRecords = vesselMap.get(vesselId);
            vesselRecords.addAll(vesselMap.get(eof));

            Date today = new Date();
            int fil = 1;
            String supCount = "";
            supCount = "0" + fil;
            String mmdd = new SimpleDateFormat("MMdd").format(today);
            String newFileName = vesselId + mmdd + supCount + "SP.TXT";
            int ftpArchProxyId = Integer.parseInt(TosRefDataUtil
                    .getValue("RDS_ARCH_FTP_ID"));

            try {
                FtpProxyListBiz list = new FtpProxyListBiz();
                int timeout = Integer.parseInt(TosRefDataUtil.getValue("FTP_TIMEOUT"));
                list.setTimeout(timeout);

                String[] supArchFiles = list.getFileNames(ftpArchProxyId, null, null);
                ArrayList<String> supFilesList = new ArrayList<String>(Arrays.asList(supArchFiles));
                boolean fileExists = true;
                while (fileExists) {
                    if (supFilesList.contains(newFileName)) {
                        fil++;
                        supCount = "" + fil;
                        if (supCount.length() == 1)
                            supCount = "0" + supCount;
                        newFileName = vesselId + mmdd + supCount + "SP.TXT";
                        fileExists = true;
                    } else {
                        fileExists = false;
                    }
                }

                logger.debug("Before writing vessel records for " + vesselId);
                boolean processed = writeToUnitTextSupplementFiles(vesselRecords, newFileName);
                logger.debug("after writing vessel records for " + vesselId + " WRITING : " + processed);
            } catch (Exception e) {
                logger.error("ERROR WHILE GETTING SUPP FILES WHEN WRITING SPLIT FILES" + e);
            }
        }
    }

    // TOSAK-3 Test commit for Jira-GITHUB Integration - code to add Equipment Grades configurable
    private String validateEquipmentGrade(String inEquipGrade) {
        java.lang.String allEquipmentGrades = null;

        try {
            allEquipmentGrades = TosRefDataUtil.getValue("EQUIPMENT_GRADES");
        } catch (Exception ex) {
            logger.error("ERROR WHILE VALIDATING EQUIPMENT_GRADES in the method validateEquipmentGrade" + ex);
        }
        logger.info("Picking the equipment grades from  db" + allEquipmentGrades + "inEquipGrade" + inEquipGrade);
        try {
            if (allEquipmentGrades != null && inEquipGrade != null && !inEquipGrade.isEmpty()) {
                java.lang.String[] equipmentGrades = allEquipmentGrades.split("\\,");

                for (java.lang.String s : equipmentGrades) {

                    logger.info("the equipment grade " + s);
                    if (s != null && !(s.isEmpty())) {

                        if (s.trim().equalsIgnoreCase(inEquipGrade.trim())) {

                            return s;
                        }
                    }
                }

            }
        } catch (Exception e) {
            logger.error("ERROR WHILE PARSING EQUIPMENT_GRADES in the method validateEquipmentGrade" + e);
        }
        logger.info("Could not find the equipment grade " + inEquipGrade + " not  matching grade from DB" + allEquipmentGrades);
        return "";
    }


}
