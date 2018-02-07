package com.matson.tos.processor;

/* This is a common business class which deals with all the common transformation
 * logic.
 *
 * 	Srno	Date			AuthorName				Change Description
 * 	A1		09/26/2012		Raghu Pattangi			Initial creation
 *  A2		09/30/2012		Karthik Rajendran		Added XML fields and file content reader logics
 *  A3		10/11/2012		Karthik Rajendran		Added: SRSTATUS, CRSTATUS, Combine ShipmentNo logics
 *  A4		10/12/2012		Karthik Rajendran		Added: Hard coded variable loadPortTrade, Modified crstatus logic for primary vessel
 *  														cargo notes transformation logic added.
 *  A5      11/22/2012      Meena Kumari            Added args to cargoNotesTransformation().
 *  A6		11/30/2012		Karthik Rajendran		Added: Supplemental validations
 *  A7		12/03/2012		Karthik Rajendran		Added: Supplemental holds validations and commodity validation
 *  A8		12/18/2012		Karthik Rajendran		Added:  1. CalculateLastFreeDayDueDate()
 *  														2. If DPORT = �MIX� then set Consignee=�MIX�
 *  A9		12/19/2012		Karthik Rajendran		Removed: If DPORT = �MIX� then set Consignee=�MIX�
 *  A10		12/21/2012		Karthik Rajendran		Added: notifyNewVessSuccess(), notifyNewVessFailure()
 *  A11		12/26/2012		Karthik Rajendran		Removed:notifyNewVessSuccess(), notifyNewVessFailure()
 *  												Added: Custom NewVesselLogger added to catch errors and email alert
 *  A12		12/27/2012		Karthik Rajendran		Changed: Adding oversize information to notes only if it is not blank.
 *  A13		01/03/2013		Karthik Rajendran		Removed: Reefer check from calculateLastFreeDayDueDate.
 *  A14		01/04/2013		Karthik Rajendran		Added: updateConsignee- If dport=MIX and consigneName is not blank then set Consignee=MIX
 *  A15		01/09/2013		Karthik Rajendran		Removed: Eliminating supplemental containers by checking misc2 field from TOS
 *  												Removed: Eliminating supplemental containers holds by checking holds from TOS
 *  A16		01/10/2013		Karthik Rajendran		Changed: updateConsignee - If dport=MIX and multiple shippment and different shipper
 *  																				then set consignee = MIX
 *  A17		01/19/2013		Karthik Rajendran		Added: Getting RDD value from multiple shipment records,so it goes to bl-nbr
 *  A18		01/23/2013		Karthik Rajendran		Added: validateContainers - Common validation function to be used before RDS Final transformations
 *  A19 	01/24/2013		Karthik Rajendran		Added: extractBargeNLTDescripancies();
 *  A20		02/05/2013		Karthik Rajendran		Added: LS Error in extractBargeNLTDescripancies();
 *  A21		02/21/2013		Karthik Rajendran		Added: Barge - Set DSC=S, COMMENTS=EB SIT if COMMODITY=SIT
 *  A21		02/21/2013		Karthik Rajendran		Added: TEMPERATURE UNIT to NLT Discrepancies
 *  A22		03/08/2013		Karthik Rajendran		Added: Set stow restriction code = 1 for GEAR container.
 *  A23		03/13/2013		Karthik Rajendran		Added: fixIncorrectDischargePort()
 *  A24		03/14/2013		Karthik Rajendran		Added: Set DIR=IN for Gear containers
 *  A25		04/15/2013		Karthik Rajendran		Added: checkIDStrapFlatR()
 *  A26		04/16/2013		Karthik Rajendran		Fix:crstatusHoldsCheck1() - Holdon6 - typecode<>(GR,GB)
 *  A27		04/17/2013		Karthik Rajendran		Changed: getAvailableDateByVVD - Get available date from TOS lookup, if not then from FSS
 *  												Changed: SupplementalValidations - Added ADD/CNC comments based on add/remove holds.
 *  												Changed: assignTrucker() - Assign trucker from NotifyParty if not then from TOS lookup.
 *  A28		04/24/2013		Karthik Rajendran		Added: getMinRddRecord() - to select minimum RDD date record from the multiple RDS records.
 *  												Changed: cargoNotesTransformation() -  Add OHZ haz description if DCM haz desc is not added to cargo notes
 *  A29		04/25/2013		Karthik Rajendran		Changed: cargoNotesTransformation() - Add OVR information to cargo notes after adding haz info
 *  A30		04/30/2013		Karthik Rajendran		Changed: supplementalValidations() - fixes for supp holds,notes
 *  A31		08/28/2013		Karthik Rajendran		Added: supplementalValidations() - misc2 not having S, oDsc<>nDsc, oDsc='S' then Add SIT_DEL to notes
 *  A32		09/02/2013		Karthik Rajendran		Changed: assignTrucker() - ConsigneeName check should be only for assigning trucker from TOS, it should not be for NP as trucker.
 *  A33		10/08/2013		Karthik Rajendran		Changed: Retaining notes from TOS for SIT removal
 *  A34		10/21/2013		Karthik Rajendran		Changed: For supp units always retain notes from TOS and add it with supp update notes.
 *  												Changed: addSupplementProblems() - Show group only if unit departed on truck
 *  A35		10/22/2013		Karthik Rajendran		Changed: addSupplementProblems() - In supp problems report, show old data from TOS instead of showing it from TDP
 *  A36		10/23/2013		Karthik Rajendran		Changed: Removed cargo notes validation, Update cargo notes only if prev cargo notes was empty or if there is a consginee change,
 *  														 Removed TOS holds lookup call as we can use holds retrieved in prev step of getting unit details from TOS.  
 *  A37		01/10/2014		Karthik Rajendran		Changed: For Barge newves - Use TosLookup.getSITByBooking() to check if it is SIT by booking, then set COmmodity
 *  A38		01/13/2014		Karthik Rajendran		Added:   CleanUp() function is called to clean TosLookup after barge transform, Add bkg nbr to email
 *  A39		01/21/2014		Karthik Rajendran		Added: COMBINE_AUTOS Logical implementations
 *  														New Methods : convCon2Cy(), combineAutos(), getMinRddDate(), autoSpray()
 *  													ModifiedMethods : combineShipmentNoCreditStatus(), getMinRddRecord(), updateConsignee(), updateConsigneeSupp()
 *  																	: some of the logics are being handled in combineAutos(), hence commented in these methods
 *  A40		02/05/2014		Karthik Rajendran		Added: RM holds should be applied to APL containers irrespective of LAX load port.
 *  A41		03/26/2014		Karthik Rajendran		Added: Barge newves/NLT discrepancies report should show If FULL container came without a booking number.
 *  A42		05/13/2014		Karthik Rajendran		Changed: Logic reverted : EP000206752 
 *  A43		05/16/2014		Karthik Rajendran		Removed: code cleanup by removing A42
 *  A44		07/03/2014		Karthik Rajendran		Added: BargeProcess: Create Bkgs in N4 by generating a BOB from stowplan message, if the bkgs are not available in N4
 */

import com.matson.cas.erd.service.data.CityVO;
import com.matson.cas.erd.service.data.VesselVO;
import com.matson.cas.refdata.mapping.*;
import com.matson.cas.service.ftp.FtpBizException;
import com.matson.cas.service.ftp.FtpProxyDeleterBiz;
import com.matson.cas.service.ftp.FtpProxySenderBiz;
import com.matson.gems.api.GEMSInterface;
import com.matson.gems.api.equipment.EquipmentDetails;
import com.matson.gems.api.equipmentattributes.GEMSEquipment;
import com.matson.gems.api.results.GEMSResults;
import com.matson.tos.cas.TosCommonVesselClient;
import com.matson.tos.dao.NewReportVesselDao;
import com.matson.tos.dao.NewVesselDao;
import com.matson.tos.dao.TosLookup;
import com.matson.tos.exception.NLTErrors;
import com.matson.tos.exception.NewVesselLogger;
import com.matson.tos.jatb.Abob;
import com.matson.tos.util.*;
import com.matson.tos.vo.CalendarVO;
import com.matson.tos.vo.SupplementProblems;
import com.matson.tos.vo.VesselVisitVO;
import com.matson.tos.vo.XmlFields;
import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class CommonBusinessProcessor {

    private static final String MULTISTOP_SIT = "MULTISTOP SIT";
    private static Logger logger = Logger
            .getLogger(CommonBusinessProcessor.class);

    static final String FLD_NAME = "name";
    static final String FLD_LENGTH = "length";
    static final String FLD_NODE = "Field";
    // static final String loadPortTrade = "G";
    public static HashMap portCodeTradeMap;
    public static HashMap outboundVesselMap;
    public static HashMap outboundNIVesselMap;
    public static HashMap outboundExportVesselMap;
    public static HashMap arrivalDateMap;
    public static ArrayList<CalendarVO> tosCalendarList;
    public static HashMap vesselDetailsMap;
    private static TosLookup tosLookUp = null;
    // public static HashMap<String, String> truckerMap = null;
    public static final String PRIMARY = "primary";
    public static final String SUPPLEMENT = "supplement";
    public static ArrayList destPodList = null;
    private static NewVesselLogger nvLogger = NewVesselLogger.getInstance();
    public static ArrayList<SupplementProblems> supProblemsList = null;
    private static final String DISCREP_DS = "DS Change";
    private static final String DISCREP_DPORT = "Dport Discrepancy";
    private static final String DISCREP_CNEE = "Consignee Discrepancy";

    // Reads all fields defined in the xml files(BRDS, OCR,OCH,OHZ,OHL,OVR,DCM)
    // Returns data objects(fields name and length) to be used in parsing
    @SuppressWarnings({"unchecked"})
    public static List<XmlFields> getFields(String fileName) {
        List<XmlFields> fields = new ArrayList<XmlFields>();
        try {
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            InputStream in = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream(fileName);// new
            // FileInputStream(fileName);
            XMLEventReader eventReader = inputFactory.createXMLEventReader(in);
            XmlFields flds = null;
            logger.debug("Reading XML fields from: " + fileName);
            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();
                if (event.isStartElement()) {
                    StartElement startElement = event.asStartElement();
                    if (startElement.getName().getLocalPart().equals(FLD_NODE)) {
                        flds = new XmlFields();
                        Iterator<Attribute> attributes = startElement
                                .getAttributes();
                        while (attributes.hasNext()) {
                            Attribute attribute = attributes.next();
                            if (attribute.getName().toString().equals(FLD_NAME)) {
                                flds.setFieldName(attribute.getValue());
                            } else if (attribute.getName().toString()
                                    .equals(FLD_LENGTH)) {
                                flds.setFieldLength(attribute.getValue());
                            }
                        }
                        fields.add(flds);
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("Error in reading xml files: ", ex);
        }
        return fields;
    }

    // Reads file contents and returns content in a string
    public static String readFileContents(String fileName) {
        StringBuilder sb = new StringBuilder();
        BufferedReader br;
        try {
            File file = new File(fileName);
            br = new BufferedReader(new FileReader(file));
            String line = null;
            logger.debug("Reading content from: " + fileName);
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            br.close();
        } catch (Exception ex) {
            logger.error("Error in reading file contents: ", ex);
        }
        return sb.toString();
    }

    public static ArrayList<TosRdsDataMt> convCon2Cy(ArrayList<TosRdsDataMt> rdsList, ArrayList<TosStowPlanCntrMt> ocrDataList) {//A39
        logger.debug("********* convCon2Cy: START *********");
        for (TosRdsDataMt rds : rdsList) {
            String ctrno = rds.getCtrno();
            if (ctrno == null)
                continue;
            String typeCode = "";
            for (TosStowPlanCntrMt stow : ocrDataList) {
                if (stow.getContainerNumber() == null)
                    continue;
                if (ctrno.equalsIgnoreCase(stow.getContainerNumber())) {
                    typeCode = stow.getTypeCode();
                    break;
                }
            }
            typeCode = typeCode == null ? "" : typeCode;
            String cgnotes = rds.getCmdyDesc();
            cgnotes = cgnotes == null ? "" : cgnotes;
            String ds = rds.getLoadDischServ();
            ds = ds == null ? "" : ds;
            if (ds.equals("CON"))
                cgnotes = "LINKED CON " + cgnotes;
            else if (ds.equals("RO"))
                cgnotes = "LINKED RO " + cgnotes;
            cgnotes = cgnotes.length() >= 255 ? cgnotes.substring(0, 255) : cgnotes;
            rds.setCmdyDesc(cgnotes);
            String commodity = rds.getCmdySrvptDesc();
            commodity = commodity == null ? "" : commodity;
            if (ds.equals("CON") || ds.equals("RO")) {
                if (typeCode.length() > 0
                        && typeCode.substring(0, 1).equals("V")) {
                    ds = "CY";
                    commodity = "AUTOCY";
                } else if (ds.equals("RO")) {
                    ds = "AU";
                    commodity = "AUTO";
                } else {
                    if (typeCode.length() > 0
                            && typeCode.substring(0, 1).equals("F")) {
                        ds = "CON";
                        commodity = "AUTOCON";
                    } else {
                        ds = "CY";
                        commodity = "AUTOCY";
                    }
                }
                logger.debug("CTRNO:" + rds.getCtrno() + "\t\tDS:" + ds + "\t\tCMDY:" + commodity + "\t\tCGNOTES:" + cgnotes);
            }
            rds.setLoadDischServ(ds);
            rds.setCmdySrvptDesc(commodity);
        }
        logger.debug("********* convCon2Cy: END *********");
        return rdsList;
    }

    public static ArrayList<TosRdsDataMt> combineAutos(ArrayList<TosRdsDataMt> rdsList, ArrayList<TosRdsDataMt> rawDataClone) {//A39
        logger.debug("********* COMBINEAUTOS: BEGIN *********");
        logger.debug("Finding Automobile containers...");
        ArrayList<TosRdsDataMt> rdsListAuto = new ArrayList<TosRdsDataMt>();
        Iterator<TosRdsDataMt> rdsItr = rdsList.iterator();
        while (rdsItr.hasNext()) {
            TosRdsDataMt rds = rdsItr.next();
            if (rds.getLoadDischServ() != null && rds.getLoadDischServ().equalsIgnoreCase("AU")) {
                rdsListAuto.add(rds);
                //rdsItr.remove();
            }
        }
        if (rdsListAuto.size() <= 0) {
            logger.debug("Exiting COMBINEAUTOS : No autos to process on this vessel");
            return rdsList;
        } else {
            logger.debug(rdsListAuto.size() + " AUTO container(s) found");
        }
        logger.debug("Finding Automobile containers also received with DS=CY ctrs ...");
        ArrayList<TosRdsDataMt> rdsListTemp = new ArrayList<TosRdsDataMt>();
        for (int i = 0; i < rdsListAuto.size(); i++) {
            TosRdsDataMt aRds = rdsListAuto.get(i);
            String aCtrno = aRds.getCtrno();
            logger.debug("aCtrno : " + aCtrno);
            if (aCtrno != null) {
                rdsItr = rdsList.iterator();
                while (rdsItr.hasNext()) {
                    TosRdsDataMt rds = rdsItr.next();
                    String rdsCtrno = rds.getCtrno();
                    if (rdsCtrno != null && rdsCtrno.equalsIgnoreCase(aCtrno)) {
                        rdsListTemp.add(rds);
                        logger.debug("Mixed DS record found for " + rdsCtrno);
                        rdsItr.remove();
                    }
                }
            }
        }
        if (rdsListTemp.size() > 0)
            rdsListAuto = rdsListTemp; //rdsListAuto.addAll(rdsListTemp); // Assign this temp rds list back to AUTO Ctrs list, since it has all AUTO ctrs now
        logger.debug("PRiNT");
        for (TosRdsDataMt rds1 : rdsListAuto)
            logger.debug(rds1.getCtrno() + "\t\t" + rds1.getShipno() + "\t\t" + rds1.getLoadPort() + "\t\t" + rds1.getDischargePort() + "\t\t" + rds1.getDestinationPort() + "\t\t" + rds1.getLoadDischServ() + "\t\t" + rds1.getShipperName() + "\t\t" + rds1.getConsigneeName() + "\t\t" + rds1.getRdd() + "\t\t" + rds1.getConsigneeArol() + "\t\t" + rds1.getConsigneeOrgnId());

        // Taking a copy of auto ctrs list for any future use
        ArrayList<TosRdsDataMt> rdsListAutoSave = new ArrayList<TosRdsDataMt>();
        for (TosRdsDataMt rds : rdsListAuto) {
            rdsListAutoSave.add(rds.clone());
        }
        // Find out any RDD has POV, if it has then set Cnee as POV-Cnee, rdd as null
        logger.debug("Finding any POV containers...");
        ArrayList<TosRdsDataMt> rdsListAutoPovs = new ArrayList<TosRdsDataMt>();
        for (TosRdsDataMt aRds : rdsListAuto) {
            if (aRds.getRdd() != null && aRds.getRdd().length() == 10) {
            } else if (aRds.getRdd() != null && aRds.getRdd().equalsIgnoreCase("POV")) {
                aRds.setRdd(null);
                String cneeName = aRds.getConsigneeName();
                cneeName = cneeName == null ? "" : cneeName;
                aRds.setConsigneeName("POV-" + cneeName);
                aRds.setConsigneeQualifier(null);
                rdsListAutoPovs.add(aRds);
                logger.debug("POV Ctr :" + aRds.getCtrno());
            }
        }
        // Set Cnee to POV to all ctrs seq
        logger.debug("Setting Cnee to POV-Cnee to all ctrs seq if POV found in other seq");
        if (rdsListAutoPovs.size() > 0) {
            for (TosRdsDataMt povRds : rdsListAutoPovs) {
                String ctrno = povRds.getCtrno();
                if (ctrno != null) {
                    for (TosRdsDataMt aRds : rdsListAuto) {
                        if (aRds.getCtrno() != null && aRds.getCtrno().equalsIgnoreCase(ctrno) && aRds.getConsigneeName() != null && !aRds.getConsigneeName().startsWith("POV-")) {
                            aRds.setConsigneeName("POV-" + aRds.getConsigneeName());
                            aRds.setConsigneeQualifier(null);
                        }
                    }
                }
            }
        }
        // Set Min RDD to all ctrs
        logger.debug("Finding minimum RDD value in all ctrs");
        for (TosRdsDataMt aRds : rdsListAuto) {
            if (aRds.getCtrno() != null) {
                ArrayList<TosRdsDataMt> ctrList = new ArrayList<TosRdsDataMt>();
                for (TosRdsDataMt tempRds : rdsListAuto) {
                    if (tempRds.getCtrno() != null && tempRds.getCtrno().equalsIgnoreCase(aRds.getCtrno()))
                        ctrList.add(tempRds);
                }
                if (ctrList.size() > 0 && aRds.getRdd() != null && !aRds.getRdd().equals("")) {
                    aRds.setRdd(getMinRddDate(ctrList));
                }
            }
        }
        logger.debug("PRiNT");
        for (TosRdsDataMt rds1 : rdsListAuto)
            logger.debug(rds1.getCtrno() + "\t\t" + rds1.getShipno() + "\t\t" + rds1.getLoadPort() + "\t\t" + rds1.getDischargePort() + "\t\t" + rds1.getDestinationPort() + "\t\t" + rds1.getLoadDischServ() + "\t\t" + rds1.getShipperName() + "\t\t" + rds1.getConsigneeName() + "\t\t" + rds1.getRdd() + "\t\t" + rds1.getConsigneeArol() + "\t\t" + rds1.getConsigneeOrgnId());
        //
        Collections.sort(rdsListAuto, new Comparator<TosRdsDataMt>() {
            public int compare(TosRdsDataMt rds1, TosRdsDataMt rds2) {
                return rds1.getCtrno().compareTo(rds2.getCtrno());
            }
        });
        logger.debug("PRiNT AFTER SORT");
        for (TosRdsDataMt rds1 : rdsListAuto)
            logger.debug(rds1.getCtrno() + "\t\t" + rds1.getShipno() + "\t\t" + rds1.getLoadPort() + "\t\t" + rds1.getDischargePort() + "\t\t" + rds1.getDestinationPort() + "\t\t" + rds1.getLoadDischServ() + "\t\t" + rds1.getShipperName() + "\t\t" + rds1.getConsigneeName() + "\t\t" + rds1.getRdd() + "\t\t" + rds1.getConsigneeArol() + "\t\t" + rds1.getConsigneeOrgnId());
        //
        //
        String mixFlag = "N";
        String oldCtr = "";
        String oldPort = "";
        String holdPort = "";
        String mixedDs = "";
        String saveCnee = "";
        String autocnee = "";
        ArrayList<TosRdsDataMt> newRdsListAuto = new ArrayList<TosRdsDataMt>();
        for (TosRdsDataMt rds : rdsListAuto) {
            String ctrno = rds.getCtrno();
            logger.debug("ctrno : " + ctrno);
            if (ctrno != null && !oldCtr.equalsIgnoreCase(ctrno)) {
                ArrayList<TosRdsDataMt> tempAutosList = new ArrayList<TosRdsDataMt>();
                for (TosRdsDataMt tempRds : rdsListAuto) {
                    if (tempRds.getCtrno() != null && tempRds.getCtrno().equalsIgnoreCase(ctrno)) {
                        tempAutosList.add(tempRds);
                    }
                }
                if (tempAutosList.size() > 1) { // multiple rds records
                    mixFlag = "N";
                    oldPort = "";
                    holdPort = "";
                    mixedDs = "N";
                    saveCnee = "";
                    autocnee = "";
                    rds = getMinRddRecord(tempAutosList); // Get min RDD record to consider
                    //tempAutosList.remove(rds); // Delete that considered record from temp list
                    oldPort = rds.getDestinationPort();
                    holdPort = rds.getDestinationPort();
                    if (autoSpray(rds)) {
                        autocnee = "AUTO-SPRAY";
                    } else {
                        autocnee = "AUTOMOBILE";
                    }
                    for (TosRdsDataMt tempRds1 : tempAutosList) {
                        if (!holdPort.contains(tempRds1.getDestinationPort()))
                            holdPort = holdPort + (holdPort.length() > 0 ? "-" : "") + tempRds1.getDestinationPort();
                        if (!tempRds1.getDestinationPort().equals(oldPort))
                            mixFlag = "Y";
                        oldPort = tempRds1.getDestinationPort();
                        if (tempRds1.getLoadDischServ() != null && !tempRds1.getLoadDischServ().equalsIgnoreCase("AU")) {
                            mixedDs = "Y";
                            saveCnee = tempRds1.getConsigneeName();
                        }
                        boolean sprayCar = autoSpray(tempRds1);
                        if (sprayCar && autocnee.equals("AUTOMOBILE"))
                            autocnee = "AUTO-SPRAY";
                    }
                    if (mixFlag.equals("Y")) {
                        rds.setDestinationPort("MIX");
                        rds.setTransit("M");
                    } else {
                        rds.setTransit(null);
                    }
                    //
                    if (rds.getConsigneeName() != null && rds.getConsigneeName().startsWith("POV-"))
                        rds.setConsigneeName("POV-" + autocnee);
                    else
                        rds.setConsigneeName(autocnee);
                    //
                    if (rds.getDestinationPort() != null && rds.getDestinationPort().equalsIgnoreCase("MIX") && mixedDs.equals("N")) {
                        if (rds.getConsigneeName() != null && rds.getConsigneeName().contains("AUTOMOBILE")) {
                            if (rds.getConsigneeName().startsWith("POV-"))
                                rds.setConsigneeName("POV-" + holdPort);
                            else
                                rds.setConsigneeName(holdPort);
                        }
                    } else if (rds.getDestinationPort() != null && !rds.getDestinationPort().equalsIgnoreCase("MIX") && mixedDs.equals("Y")) {
                        rds.setLoadDischServ("AU");
                        if (rds.getConsigneeName() != null && rds.getConsigneeName().contains("AUTOMOBILE")) {
                            if (rds.getConsigneeName().startsWith("POV-"))
                                rds.setConsigneeName("POV-" + saveCnee + " & AUTOMOBILE");
                            else
                                rds.setConsigneeName(saveCnee + " & AUTOMOBILE");
                        }
                    } else if (rds.getDestinationPort() != null && rds.getDestinationPort().equalsIgnoreCase("MIX") && mixedDs.equals("Y")) {
                        rds.setLoadDischServ("AU");
                        if (rds.getConsigneeName() != null && rds.getConsigneeName().contains("AUTOMOBILE")) {
                            if (rds.getConsigneeName().startsWith("POV-"))
                                rds.setConsigneeName("POV-" + holdPort + " " + saveCnee + " & AUTOMOBILE");
                            else
                                rds.setConsigneeName(holdPort + " " + saveCnee + " & AUTOMOBILE");
                        }
                    }
                    if (rds.getCmdyAg() != null && rds.getCmdyAg().equalsIgnoreCase("Y"))
                        rds.setCmdyAg(null);
                } else { // single rds record
                    holdPort = rds.getDestinationPort();
                    mixFlag = "N";
                    mixedDs = "N";
                    oldPort = rds.getDestinationPort();
                    oldCtr = ctrno;
                    rds.setCmdyAg(null);
                    if (!rds.getLoadDischServ().equalsIgnoreCase("AU")) {
                        mixedDs = "Y";
                        saveCnee = rds.getConsigneeName();
                    }
                    if (autoSpray(rds)) {
                        autocnee = "AUTO-SPRAY";
                    } else {
                        autocnee = "AUTOMOBILE";
                    }
                    if (rds.getConsigneeName() != null && rds.getConsigneeName().startsWith("POV-"))
                        rds.setConsigneeName("POV-" + autocnee);
                    else
                        rds.setConsigneeName(autocnee);
                }
                if (rds.getConsigneeName() != null) {
                    rds.setDamageStatus(rds.getConsigneeName().length() >= 35 ? rds.getConsigneeName().substring(0, 35) : rds.getConsigneeName());
                    rds.setConsigneeName(rds.getConsigneeName().length() >= 32 ? rds.getConsigneeName().substring(0, 32) : rds.getConsigneeName());
                }
                // Check if the cntr is originally a RoRo discharge service then concatenate shipment numbers
                logger.info("rawDataClone.size :- " + rawDataClone.size());
                //Iterator<TosRdsDataMt> rdsItrc = rawDataClone.iterator();
                String shipNo = "";
                //String tempStr = "";
                for (int i = 0; i < rawDataClone.size(); i++) {
                    TosRdsDataMt rdsFields = rawDataClone.get(i);
                    //tempStr = tempStr + rdsFields.getCtrno() +"-"+ rdsFields.getLoadDischServ()+",";
                    if (ctrno.equalsIgnoreCase(rdsFields.getCtrno()) && rdsFields.getLoadDischServ() != null && "RO".equalsIgnoreCase(rdsFields.getLoadDischServ().trim())) {
                        shipNo = shipNo + rdsFields.getShipno() + "-";
                    }
                }
                if (shipNo.length() > 0 && shipNo.endsWith("-")) {
                    shipNo = shipNo.substring(0, shipNo.length() - 1);
                    rds.setShipno(shipNo);
                }
                //logger.info("rawData : "+ tempStr);
                logger.info("SHIPNO : " + shipNo);
                newRdsListAuto.add(rds);
                //
                oldCtr = ctrno;
            }
        }
        logger.debug("PRiNT");
        logger.debug("AFTER PROCESSING AUTOS :");
        for (TosRdsDataMt rds1 : newRdsListAuto)
            logger.debug(rds1.getCtrno() + "\t\t" + rds1.getShipno() + "\t\t" + rds1.getLoadPort() + "\t\t" + rds1.getDischargePort() + "\t\t" + rds1.getDestinationPort() + "\t\t" + rds1.getLoadDischServ() + "\t\t" + rds1.getShipperName() + "\t\t" + rds1.getConsigneeName() + "\t\t" + rds1.getRdd() + "\t\t" + rds1.getConsigneeArol() + "\t\t" + rds1.getConsigneeOrgnId());
        // After processing all AUTOS, add the list back to rds data list
        rdsList.addAll(newRdsListAuto);
        //
        logger.debug("********* COMBINE_AUTOS: END *********");
        return rdsList;
    }

    public static String getMinRddDate(ArrayList<TosRdsDataMt> rdsList) {//A39
        String minRdd = null;
        ArrayList<Date> rddDates = new ArrayList<Date>();
        for (TosRdsDataMt temp : rdsList) {
            if (temp.getRdd() != null && temp.getRdd().length() == 10) {
                Date tempDate = CalendarUtil.convertStrgToDateFormat2(temp.getRdd());
                if (tempDate != null)
                    rddDates.add(tempDate);
            }
        }
        if (rddDates.size() > 0) {
            Date minDate = Collections.min(rddDates);
            minRdd = CalendarUtil.convertDateToString2(minDate);
        }
        return minRdd;
    }

    public static boolean autoSpray(TosRdsDataMt rds) {//A39
        if (rds != null && rds.getShipperName() != null && rds.getShipperName().contains("FORD MOTOR COMPANY")) {
            if (rds.getConsigneeName() != null && (rds.getConsigneeName().contains("BUDGET RENT") || rds.getConsigneeName().contains("HERTZ RENT") || rds.getConsigneeName().contains("AVIS RENT") || rds.getConsigneeName().contains("DOLLAR RENT")))
                return true;
        }
        return false;
    }

    public static ArrayList<TosRdsDataMt> combineShipmentNoCreditStatus(ArrayList<TosRdsDataMt> rdsList) {
        logger.info("******* Combining multiple records RDS begin *******");
        ArrayList<TosRdsDataMt> output = new ArrayList<TosRdsDataMt>();
        ArrayList<TosRdsDataMt> tempList = new ArrayList<TosRdsDataMt>();
        for (TosRdsDataMt rds : rdsList) {
            tempList.add(rds.clone());
        }
        String oldCtrno = "";
        for (int i = 0; i < rdsList.size(); i++) {
            TosRdsDataMt rdsd = rdsList.get(i);
            String ctrno = rdsd.getCtrno();
            logger.info("oldCtrno:" + oldCtrno + ", ctrno:" + ctrno);
            if (!oldCtrno.equalsIgnoreCase(ctrno)) {
                StringBuilder sb = new StringBuilder();
                StringBuilder sbCStat = new StringBuilder();
                //ArrayList<TosRdsDataMt> tempAutosList = new ArrayList<TosRdsDataMt>(); //A39
                for (int j = 0; j < tempList.size(); j++) {
                    TosRdsDataMt temp = tempList.get(j);
                    if (ctrno.equalsIgnoreCase(temp.getCtrno())) {
                        // Concatenate all shippment numbers
                        sb.append(temp.getShipno() + "-");
                        // if there are duplicate values, then we should only
                        // take one value.
                        // For example, if there are 4 records with V, H, V, H
                        // then we should only store VH but not VVHH
                        // Modified by Karthik
                        if (temp.getCreditStatus() != null) {
                            String cstatus = temp.getCreditStatus();
                            if (sbCStat.indexOf(cstatus) == -1) {
                                sbCStat.append(cstatus);
                            }
                        }
                        //tempAutosList.add(temp); //A39
                    }
                }
                rdsd.setShipno(sb.toString().substring(0, sb.length() - 1));
                if (sbCStat.length() > 0) {
                    logger.info("Credit status : " + ctrno + " "
                            + sbCStat.toString());
                    rdsd.setCreditStatus(sbCStat.toString());
                }
                /*if (rdsd.getLoadDischServ() != null && rdsd.getLoadDischServ().equals("AU") && tempAutosList.size() > 1) //A39
                {
					output.add(getMinRddRecord(rdsd, tempAutosList));
				}
				else
				{*/
                output.add(rdsd);
                //}
                oldCtrno = ctrno;
            }
        }
        logger.info("******* Combining multiple records RDS end *******");
        return output;
    }

    public static TosRdsDataMt getMinRddRecord(ArrayList<TosRdsDataMt> rdsList) {
        //logger.debug("getMinRddRecord for : " + oldRds.getCtrno());//A39
        //String shipNo = oldRds.getShipno();
        //String creditStatus = oldRds.getCreditStatus();
        TosRdsDataMt newRds = null;
        HashMap<String, TosRdsDataMt> nonNullRdds = new HashMap<String, TosRdsDataMt>();
        ArrayList<Date> rddDates = new ArrayList<Date>();
        // Get non-null rdd
        for (int i = 0; i < rdsList.size(); i++) {
            TosRdsDataMt temp = rdsList.get(i);
            logger.debug("CTRNO:" + temp.getCtrno() + "\t\t�RDD:" + temp.getRdd() + "\t\t�BL:" + temp.getShipno() + "\t\t�CNEE:" + temp.getConsigneeName() + "\t\t�SHIPER:" + temp.getShipperName());
            if (temp.getRdd() != null && temp.getRdd().length() == 10) {
                nonNullRdds.put(temp.getRdd(), temp);
                rddDates.add(CalendarUtil.convertStrgToDateFormat2(temp.getRdd()));
            }
        }
        if (rddDates.size() > 0) {
            Date minDate = Collections.min(rddDates);
            newRds = nonNullRdds.get(CalendarUtil.convertDateToString2(minDate));
        }
        if (newRds == null)
            newRds = rdsList.get(0); //A39
        logger.debug("CHOSEN --> CTRNO:" + newRds.getCtrno() + "\t\t�RDD:" + newRds.getRdd() + "\t\t�BL:" + newRds.getShipno() + "\t\t�CNEE:" + newRds.getConsigneeName() + "\t\t�SHIPER:"
                + newRds.getShipperName());
        return newRds;
    }

    // Method added to Fix for Unique Constraint violation
    public static ArrayList<TosRdsDataMt> eliminateDuplicatesInRDS(
            ArrayList<TosRdsDataMt> rdsList) {
        logger.info("********* eliminateDuplicatesInRDS begin *********");
        List<TosRdsDataMt> tempList = rdsList;
        List<TosRdsDataMt> outputList = new ArrayList<TosRdsDataMt>();

        for (int i = 0; i < rdsList.size(); i++) {
            TosRdsDataMt rds = rdsList.get(i);
            String cntrno = rds.getCtrno();
            int found = 0;

            for (int j = 0; j < tempList.size(); j++) {
                TosRdsDataMt rdstemp = tempList.get(j);
                String cntrnotemp = rdstemp.getCtrno();

                if (cntrno.equals(cntrnotemp)) {
                    found++;
                }
            }
            if (found == 1) {
                outputList.add(rdsList.get(i));
                logger.info("Result ctrno : " + rdsList.get(i).getCtrno());
            }

        }
        logger.info("********* eliminateDuplicatesInRDS end *********");
        return (ArrayList<TosRdsDataMt>) outputList;
    }

    // Method added to Fix for Unique Constraint violation
    public static ArrayList<TosRdsDataFinalMt> eliminateDuplicatesInRDSFinal(
            ArrayList<TosRdsDataFinalMt> rdsFinalList) {

        List<TosRdsDataFinalMt> tempList = rdsFinalList;
        List<TosRdsDataFinalMt> outputList = new ArrayList<TosRdsDataFinalMt>();

        for (int i = 0; i < rdsFinalList.size(); i++) {
            TosRdsDataFinalMt rdsFinal = rdsFinalList.get(i);
            String cntrno = rdsFinal.getContainerNumber();
            int found = 0;

            for (int j = 0; j < tempList.size(); j++) {
                TosRdsDataFinalMt rdsFinaltemp = tempList.get(j);
                String cntrnotemp = rdsFinaltemp.getContainerNumber();

                if (cntrno.equals(cntrnotemp)) {
                    found++;
                }
            }
            if (found == 1) {
                outputList.add(rdsFinalList.get(i));
            }

        }
        return (ArrayList<TosRdsDataFinalMt>) outputList;

    }

    public static String srstatusTransformation(TosRdsDataMt rdsData) {
        StringBuilder output = new StringBuilder();
        String cmdyAg = rdsData.getCmdyAg();
        String notify = rdsData.getNotify();
        String inbond = rdsData.getInbound();
        String creditStatus = rdsData.getCreditStatus();
        String dataSource = rdsData.getDatasource();
        String trade = rdsData.getTrade();

        if (notify != null && notify.length() > 0)
            output = CommonBusinessProcessor.appendStatusString(output, "ON");
        if (inbond != null && inbond.trim().length() > 0) {
            if (trade != null && trade.equalsIgnoreCase("M"))
                output = CommonBusinessProcessor.appendStatusString(output,
                        "INB");
        }
        if (creditStatus != null && creditStatus.contains("C"))
            output = CommonBusinessProcessor.appendStatusString(output, "CC");
        if (creditStatus != null
                && (creditStatus.contains("H") || (creditStatus
                .equalsIgnoreCase("") && dataSource != null && dataSource
                .equalsIgnoreCase("E")))) {
            if (trade != null
                    && (!trade.equalsIgnoreCase("G") && !trade
                    .equalsIgnoreCase("H")))
                output = CommonBusinessProcessor.appendStatusString(output,
                        "HP");
        }
		/*if (dataSource != null && dataSource.equalsIgnoreCase("E"))
		{
			if (trade != null
					&& (!trade.equalsIgnoreCase("G") && !trade
							.equalsIgnoreCase("H")))
				output = CommonBusinessProcessor.appendStatusString(output,
						"DOC");
		}*/
        if (output.length() < 1)
            output = CommonBusinessProcessor.appendStatusString(output, "OK");
        logger.info(rdsData.getCtrno() + " SRSTATUS TRANS: "
                + rdsData.getCtrno() + " " + output.toString());
        return output.toString();
    }

    public static ArrayList<TosRdsDataFinalMt> cargoNotesTransformation(ArrayList<TosRdsDataFinalMt> rdsDataFinal) {
        logger.info("*** cargoNotesTransformation begin ********");
        for (int i = 0; i < rdsDataFinal.size(); i++) {
            TosRdsDataFinalMt rdsFd = rdsDataFinal.get(i);
            String ctrno = rdsFd.getContainerNumber();
            String vv = rdsFd.getVesvoy();
            String dsc = rdsFd.getDsc();
            dsc = dsc == null ? "" : dsc;
            String cargoNotes = rdsFd.getCargoNotes();
            cargoNotes = cargoNotes == null ? "" : cargoNotes.trim();
            String dport = rdsFd.getDport();
            dport = dport == null ? "" : dport;

            if (!dsc.equals("")) {
                if (dsc.equals("S"))

                    cargoNotes = cargoNotes.length() > 0 ? "SIT-" + dport + " "
                            + cargoNotes : "SIT-" + dport;

            }

            ArrayList<TosStowPlanCntrMt> stowData = NewVesselDao.getOCRDataForCtrno(ctrno, vv);
            //
            // Adding oversize information to notes
            StringBuilder out = new StringBuilder();
            if (stowData != null && stowData.size() > 0) {
                for (int j = 0; j < stowData.size(); j++) {
                    BigDecimal ovrHieght = stowData.get(j)
                            .getOversizeHeightInches();
                    if (ovrHieght != null && ovrHieght.doubleValue() > 0)
                        out.append(" " + ovrHieght + "\" OH");
                    BigDecimal ovrLeft = stowData.get(j)
                            .getOversizeLeftInches();
                    if (ovrLeft != null && ovrLeft.doubleValue() > 0)
                        out.append(" " + ovrLeft + "\" OWL");
                    BigDecimal ovrRight = stowData.get(j)
                            .getOversizeRightInches();
                    if (ovrRight != null && ovrRight.doubleValue() > 0)
                        out.append(" " + ovrRight + "\" OWR");
                    BigDecimal ovrFront = stowData.get(j)
                            .getOversizeFrontInches();
                    if (ovrFront != null && ovrFront.doubleValue() > 0)
                        out.append(" " + ovrFront + "\" OLF");
                    BigDecimal ovrRear = stowData.get(j)
                            .getOversizeRearInches();
                    if (ovrRear != null && ovrRear.doubleValue() > 0)
                        out.append(" " + ovrRear + "\" OLB");
                }
            }
            if (out.length() > 0)
                cargoNotes = cargoNotes.trim() + " " + out.toString().trim();
            //
            // Adding hazard description to notes
            ArrayList<TosDcmMt> dcmData = NewVesselDao.getDcmDataForCntr(ctrno, vv);
            String hazDesc = "";
            if (dcmData != null) {
                for (int d = 0; d < dcmData.size(); d++) {
                    TosDcmMt dcm = dcmData.get(d);
                    String desc = dcm.getHazDesc1();
                    desc = desc == null ? "" : desc;
                    hazDesc = hazDesc + " " + desc;
                }
            }
            if (hazDesc.length() > 0) {
                logger.info("Assigned hazDesc from DCM : " + hazDesc);
                cargoNotes = cargoNotes.trim() + " " + hazDesc.trim();
            } else {
                if (stowData != null && stowData.size() > 0) {
                    TosStowPlanCntrMt stowCntrMt = stowData.get(0);
                    if (stowCntrMt.getDir() != null && !stowCntrMt.getDir().equals("MTY")) {
                        Set<TosStowPlanHazMt> stowPlanHazMt = stowCntrMt.getTosStowPlanHazMts();
                        if (stowPlanHazMt != null) {
                            Iterator<TosStowPlanHazMt> itrHaz = stowPlanHazMt.iterator();
                            while (itrHaz.hasNext()) {
                                TosStowPlanHazMt hz = itrHaz.next();
                                if (hz != null && hz.getDescription() != null)
                                    hazDesc = hazDesc + hz.getDescription() + " ";
                            }
                        }
                    }
                }
                cargoNotes = cargoNotes.trim() + " " + hazDesc.trim();
                logger.info("Assigned hazDesc from OHZ : " + hazDesc);
            }
            //
            //
            rdsFd.setCargoNotes(cargoNotes.length() > 255 ? cargoNotes.substring(0, 255).trim() : cargoNotes.trim());
            logger.info("CARGO NOTES FOR " + ctrno + " �--> " + rdsFd.getCargoNotes());
            rdsDataFinal.set(i, rdsFd);
        }
        logger.info("******** cargoNotesTransformation end ********");
        return rdsDataFinal;
    }

    public static ArrayList<TosRdsDataFinalMt> cargoNotesTransformationSup(ArrayList<TosRdsDataFinalMt> rdsDataFinal, Date triggerDate) {
        logger.info("******** cargoNotesTransformationSup start ********");
        for (int i = 0; i < rdsDataFinal.size(); i++) {
            TosRdsDataFinalMt rdsFd = rdsDataFinal.get(i);
            String cargoNotes = rdsFd.getCargoNotes();
            cargoNotes = cargoNotes == null ? "" : cargoNotes.trim();
            String dsc = rdsFd.getDsc();
            dsc = dsc == null ? "" : dsc;
            String dport = rdsFd.getDport();
            dport = dport == null ? "" : dport;

            if (!dsc.equals("")) {
                if (dsc.equals("S")) {
                    cargoNotes = cargoNotes.length() > 0 ? "SIT-" + dport + " " + cargoNotes : "SIT-" + dport;
                    rdsFd.setCommodity(MULTISTOP_SIT);
                }
            }
            String supDate = calcSupplementalDate(triggerDate);
            if (cargoNotes.equals("")) {
                cargoNotes = "S - " + supDate;
            } else {
                cargoNotes = cargoNotes + " S-" + supDate;
            }
            rdsFd.setCargoNotes(cargoNotes.length() > 255 ? cargoNotes.substring(0, 255).trim() : cargoNotes.trim());
            logger.info("CTRNO=" + rdsFd.getContainerNumber() + ",COMM=" + rdsFd.getCommodity() + ",CNOTES=" + rdsFd.getCargoNotes());
            rdsDataFinal.set(i, rdsFd);
        }
        logger.info("******** cargoNotesTransformationSup end ********");
        return rdsDataFinal;
    }


    public static ArrayList<TosRdsDataFinalMt> updatePlanDispForHorizonLines(
            ArrayList<TosRdsDataFinalMt> rdsDataFinal) {
        for (int i = 0; i < rdsDataFinal.size(); i++) {
            TosRdsDataFinalMt rdsFd = rdsDataFinal.get(i);
            String shipper = rdsFd.getShipper();
            shipper = shipper == null ? "" : shipper;
            String shipperName = rdsFd.getShipperName();
            shipperName = shipperName == null ? "" : shipperName;
            if (shipper.contains("HORIZON LINES")
                    || shipperName.contains("HORIZON LINES")) {
                rdsFd.setLocationRowDeck("CSX");
                rdsFd.setOwner("CSXU");
                rdsFd.setPlanDisp("A");
            }

            rdsDataFinal.set(i, rdsFd);
        }
        return rdsDataFinal;
    }

    public static ArrayList<TosRdsDataFinalMt> trashContainersTransformation(
            ArrayList<TosRdsDataFinalMt> rdsDataFinal) {
        for (int i = 0; i < rdsDataFinal.size(); i++) {
            TosRdsDataFinalMt rdsFd = rdsDataFinal.get(i);
            String typeCode = rdsFd.getTypeCode();
            typeCode = typeCode == null ? "" : typeCode;
            String bookingNumber = rdsFd.getBookingNumber();
            bookingNumber = bookingNumber == null ? "" : bookingNumber;
            if (typeCode.endsWith("GB") && bookingNumber.equals("")) {
                rdsFd.setDir("IN");
                rdsFd.setConsignee("TRASH CONTAINERS");
                rdsFd.setConsigneeName("TRASH CONTAINERS");
                rdsFd.setStowRestrictionCode("1");
                if ("".equalsIgnoreCase(rdsFd.getCommodity())
                        || rdsFd.getCommodity() == null) {
                    rdsFd.setCommodity("TRASH");
                }
            }

            rdsDataFinal.set(i, rdsFd);
        }
        return rdsDataFinal;
    }

    public static ArrayList<TosRdsDataFinalMt> gearContainersTransformation(
            ArrayList<TosRdsDataFinalMt> rdsDataFinal) {
        for (int i = 0; i < rdsDataFinal.size(); i++) {
            TosRdsDataFinalMt rdsFd = rdsDataFinal.get(i);
            String typeCode = rdsFd.getTypeCode();
            typeCode = typeCode == null ? "" : typeCode;
            String bookingNumber = rdsFd.getBookingNumber();
            bookingNumber = bookingNumber == null ? "" : bookingNumber;
            if (typeCode.endsWith("GR") && bookingNumber.equals("")) {
                if (rdsFd.getCommodity() != null
                        && rdsFd.getCommodity().equals("GEAR")) {
                    rdsFd.setDir("IN");
                    rdsFd.setConsignee("GEAR CONTAINER");
                    rdsFd.setConsigneeName("GEAR CONTAINER");
                    rdsFd.setStowRestrictionCode("1");
                } else {
                    rdsFd.setConsignee("GEAR CONTAINERS");
                    rdsFd.setConsigneeName("GEAR CONTAINERS");
                }
            }
            rdsDataFinal.set(i, rdsFd);
        }
        return rdsDataFinal;
    }

    /*	public static ArrayList<TosRdsDataFinalMt> fixIncorrectDischargePort(
			ArrayList<TosRdsDataFinalMt> rdsDataFinal, String vesvoy)
	{
		logger.info("FIXING INCORRECT DISCH PORT****************");
		VesselVO vvo = CommonBusinessProcessor.getVesselDetails(vesvoy
				.substring(0, 3));
		String vesselOperator = "MAT";
		if (vvo != null)
		{
			vesselOperator = vvo.getVessOpr();
		}
		else
		{
			logger.info("ERROR: FIX INCORRECT DISCHARGE PORT");
			return rdsDataFinal;
		}
		vesselOperator = vesselOperator == null ? "MAT" : vesselOperator;
		if (!vesselOperator.equals("CSX"))
		{
			for (int i = 0; i < rdsDataFinal.size(); i++)
			{
				TosRdsDataFinalMt rdsFd = rdsDataFinal.get(i);
				String hazocflag = rdsFd.getHazardousOpenCloseFlag();
				String discPort = rdsFd.getDischargePort();
				String dport = rdsFd.getDport();
				String loadPort = rdsFd.getLoadPort();
				if (hazocflag != null && !hazocflag.equals("G")
						&& !hazocflag.equals("F") && !hazocflag.equals("M"))
				{
					if (!discPort.equals("HON"))
					{
						rdsFd.setRetPort(discPort);
						rdsFd.setDischargePort("HON");
						logger.info("FIX-1:\tCN:"
								+ rdsFd.getContainerNumber() + "\tOLDDISC:"
								+ rdsFd.getRetPort() + "\tNEWDISC:"
								+ rdsFd.getDischargePort());
					}
				}
				String apiTrade = CommonBusinessProcessor
						.getTradeforPort(dport);
				if (apiTrade == null)
				{
					apiTrade = "H";
					logger.info("SET APITRADE TO 'H'");
				}
				if (apiTrade.equals("H") && hazocflag.equals("G")
						&& hazocflag.equals("F") && !discPort.equals("HON"))
				{
					rdsFd.setRetPort(discPort);
					rdsFd.setDischargePort("HON");
					logger.info("FIX-2:\tCN:"
							+ rdsFd.getContainerNumber() + "\tOLDDISC:"
							+ rdsFd.getRetPort() + "\tNEWDISC:"
							+ rdsFd.getDischargePort());
				}
				if (!apiTrade.equals("F") && !apiTrade.equals("G")
						&& hazocflag.equals("F") && discPort.equals("HON")
						&& loadPort.equals(dport))
				{
					rdsFd.setRetPort(discPort);
					rdsFd.setDischargePort(dport);
					logger.info("FIX-3:\tCN:"
							+ rdsFd.getContainerNumber() + "\tOLDDISC:"
							+ rdsFd.getRetPort() + "\tNEWDISC:"
							+ rdsFd.getDischargePort());
				}
				if ((apiTrade.equals("G") || apiTrade.equals("M"))
						&& (hazocflag.equals("G") || hazocflag.equals("M"))
						&& !discPort.equals("GUM") && loadPort == null)
				{
					rdsFd.setRetPort(discPort);
					rdsFd.setDischargePort("GUM");
					logger.info("FIX-4:\tCN:"
							+ rdsFd.getContainerNumber() + "\tOLDDISC:"
							+ rdsFd.getRetPort() + "\tNEWDISC:"
							+ rdsFd.getDischargePort());
				}
				//
				rdsDataFinal.set(i, rdsFd);
			}
		}
		return rdsDataFinal;
	}
*/
    public static ArrayList<TosRdsDataFinalMt> dischargePortTransformation(
            ArrayList<TosRdsDataFinalMt> rdsDataFinal) {
        for (int i = 0; i < rdsDataFinal.size(); i++) {
            TosRdsDataFinalMt rdsFd = rdsDataFinal.get(i);
            String dischargePort = rdsFd.getDischargePort();
            if (dischargePort == null) {
                logger.info("Invokde GEMS only when discharge port is null "
                        + dischargePort);
                EquipmentDetails equip = CommonBusinessProcessor
                        .getEquipmentDetailsFromGEMS(rdsFd.getContainerNumber());
                if (equip != null && equip.getDischargePortCode() != null) {
                    rdsFd.setDischargePort(equip.getDischargePortCode());
                }
            }
            rdsDataFinal.set(i, rdsFd);
        }
        return rdsDataFinal;
    }

    public static ArrayList<TosRdsDataFinalMt> validateContainers(
            ArrayList<TosRdsDataFinalMt> rdsDataFinal) {
        for (int i = 0; i < rdsDataFinal.size(); i++) {
            Iterator<TosRdsDataFinalMt> itr = rdsDataFinal.iterator();
            while (itr.hasNext()) {
                TosRdsDataFinalMt rdsFd = itr.next();
                boolean isValid = true;
                if (rdsFd.getLoadPort() == null
                        || rdsFd.getLoadPort().equals("")) {
                    nvLogger.addError(rdsFd.getVesvoy(),
                            rdsFd.getContainerNumber(), " Load port is missing");
                    isValid = false;
                }
				/*
				 * if(rdsFd.getDischargePort()==null) {
				 * nvLogger.addError(rdsFd.getVesvoy()+rdsFd.getLeg(),
				 * rdsFd.getContainerNumber(), "Discharge port is missing");
				 * isValid = false; }
				 */
                if (rdsFd.getDport() == null || rdsFd.getDport().equals("")) {
                    nvLogger.addError(rdsFd.getVesvoy(),
                            rdsFd.getContainerNumber(),
                            " Destination port is missing");
                    isValid = false;
                }
                if (rdsFd.getTypeCode() == null
                        || rdsFd.getTypeCode().equals("")) {
                    nvLogger.addError(rdsFd.getVesvoy(),
                            rdsFd.getContainerNumber(), " Type code is missing");
                    isValid = false;
                }
                if (!isValid)
                    itr.remove();
            }
        }
        return rdsDataFinal;
    }

    //D031688 start

    public static ArrayList<TosRdsDataFinalMt> commodityTransformation2(
            ArrayList<TosRdsDataFinalMt> rdsDataFinal) {
        logger.info("inside commodityTransformation2 ");
        for (int i = 0; i < rdsDataFinal.size(); i++) {
            TosRdsDataFinalMt rdsFd = rdsDataFinal.get(i);

            logger.info("Container number" + rdsFd.getContainerNumber());

            String ds = rdsFd.getDs();
            ds = ds == null ? "" : ds;
            logger.info("ds " + ds);

            String multiStop = rdsFd.getMultiStop();
            multiStop = multiStop == null ? "" : multiStop;
            logger.info("multiStop " + multiStop);

            String dsc = rdsFd.getDsc();
            dsc = dsc == null ? "" : dsc;
            logger.info("dsc " + dsc);

            if ("S".equalsIgnoreCase(dsc) && "LCL".equalsIgnoreCase(ds) && ("Y".equalsIgnoreCase(multiStop) || "N".equalsIgnoreCase(multiStop))) {

                rdsFd.setCommodity("MULCFSSI");
            } else if ("S".equalsIgnoreCase(dsc) && "LCL".equalsIgnoreCase(ds)) {

                rdsFd.setCommodity(MULTISTOP_SIT);
            } else if ("LCL".equalsIgnoreCase(ds) && ("Y".equalsIgnoreCase(multiStop) || "N".equalsIgnoreCase(multiStop))) {

                rdsFd.setCommodity("MULTICFS");
            } else if ("S".equalsIgnoreCase(dsc) && ("Y".equalsIgnoreCase(multiStop) || "N".equalsIgnoreCase(multiStop))) {

                rdsFd.setCommodity(MULTISTOP_SIT);
            } else if ("S".equalsIgnoreCase(dsc)) {

                rdsFd.setCommodity(MULTISTOP_SIT);
            } else if ("LCL".equalsIgnoreCase(ds)) {

                rdsFd.setCommodity("CFS");
            } else if ("Y".equalsIgnoreCase(multiStop) || "N".equalsIgnoreCase(multiStop)) {

                rdsFd.setCommodity("MULTISTOP");

            }
            logger.info("commodity " + rdsFd.getCommodity());
            rdsDataFinal.set(i, rdsFd);

        }
        return rdsDataFinal;
    }

    //D031688 end

    public static ArrayList<TosRdsDataFinalMt> commodityTransformation(
            ArrayList<TosRdsDataFinalMt> rdsDataFinal) {
        for (int i = 0; i < rdsDataFinal.size(); i++) {
            TosRdsDataFinalMt rdsFd = rdsDataFinal.get(i);
            String dir = rdsFd.getDir();
            dir = dir == null ? "" : dir;
            String ds = rdsFd.getDs();
            ds = ds == null ? "" : ds;
            String typeCode = rdsFd.getTypeCode();
            typeCode = typeCode == null ? "" : typeCode;
            String shipper = rdsFd.getShipper();
            shipper = shipper == null ? "" : shipper;
            if (dir.equals("IN") && ds.equals("AUT")) {
                rdsFd.setCommodity("AUTO");
                rdsDataFinal.set(i, rdsFd);
            }
            if (dir.equals("IN")
                    && ds.equals("CY")
                    && typeCode.length() > 0
                    && typeCode.startsWith("D40")
                    && (shipper.contains("AUTO LOGISTICS SOLU") || shipper
                    .contains("GENERAL MOTORS"))) {
                rdsFd.setCommodity("ALSAUT");
                rdsDataFinal.set(i, rdsFd);
            }
        }
        return rdsDataFinal;
    }
//

    public static ArrayList<TosRdsDataFinalMt> dscTransformation(
            ArrayList<TosRdsDataFinalMt> rdsDataFinal) {
        for (int i = 0; i < rdsDataFinal.size(); i++) {
            TosRdsDataFinalMt rdsFd = rdsDataFinal.get(i);
            String cgnotes = rdsFd.getCargoNotes();
            cgnotes = cgnotes == null ? "" : cgnotes;
            if (cgnotes.contains("SIT") || cgnotes.contains("STOP IN TRANSIT")) {
                rdsFd.setDsc("S");
            }
            String dsc = rdsFd.getDsc();
            dsc = dsc == null ? "" : dsc;
            String hazOpenCloseF = rdsFd.getHazardousOpenCloseFlag();
            hazOpenCloseF = hazOpenCloseF == null ? "" : hazOpenCloseF;
            String shipper = rdsFd.getShipper();
            shipper = shipper == null ? "" : shipper;
            if (dsc.equals("S") && hazOpenCloseF.equals("H")) {
                rdsFd.setStowFlag("C");
                rdsFd.setCommodity(MULTISTOP_SIT);
            }
            if (dsc.equals("S") && !hazOpenCloseF.equals("H")
                    && shipper.contains("US GOVT POSTMASTER")) {
                rdsFd.setStowFlag("C");
                rdsFd.setCommodity(MULTISTOP_SIT);
            }
            if (dsc.equals("T"))
                rdsFd.setDsc("");
            //
            rdsDataFinal.set(i, rdsFd);
        }
        return rdsDataFinal;
    }

    public static ArrayList<TosRdsDataFinalMt> mdcChk(ArrayList<TosRdsDataFinalMt> rdsDataFinal, ArrayList<TosRdsDataMt> rdsData) {
        logger.info("*********MDCCHK BEGIN*********");
        for (int i = 0; i < rdsDataFinal.size(); i++) {
            TosRdsDataFinalMt rdsFd = rdsDataFinal.get(i);
            String ctrno = rdsFd.getContainerNumber();
            ArrayList<TosRdsDataMt> rdsList = new ArrayList<TosRdsDataMt>();
            for (int r = 0; r < rdsData.size(); r++) {
                TosRdsDataMt rds = rdsData.get(r);
                if (rds.getCtrno().equalsIgnoreCase(ctrno)) {
                    rdsList.add(rds);
                }
            }
            if (rdsList.size() > 1) // If size is >1 then it is multiple
            // shipment container
            {
                String oldPort = rdsFd.getDport();
                boolean cyFlag = false;
                boolean cfsFlag = false;
                boolean conFlag = false;
                boolean mixFlag = false;
                boolean honPort = false;
                if (oldPort.equals("ANK"))
                    honPort = true;
                String dsc = rdsFd.getDsc();
                dsc = dsc == null ? "" : dsc;
                for (int m = 0; m < rdsList.size(); m++) {
                    TosRdsDataMt mRds = rdsList.get(m);
                    String ds = mRds.getLoadDischServ();
                    ds = ds == null ? "" : ds;
                    if (ds.equals("CY"))
                        cyFlag = true;
                    else if (ds.equals("CFS"))
                        cfsFlag = true;
                    else if (ds.equals("CON"))
                        conFlag = true;
                    if (!mRds.getDestinationPort().equals(oldPort))
                        mixFlag = true;
                    oldPort = mRds.getDestinationPort();
                    if (mRds.getDestinationPort().equals("ANK"))
                        honPort = true;
                    if (m == rdsList.size() - 1) {
                        if (cfsFlag && cyFlag && honPort)
                            rdsFd.setDsc("P");
                        else if (cfsFlag && conFlag && honPort)
                            rdsFd.setDsc("P");
                        else if (cyFlag && conFlag && honPort)
                            rdsFd.setDsc("P");
                        else if (dsc.equals("") && !mixFlag)
                            rdsFd.setDsc("A");
                        else if (mixFlag && dsc.equals(""))
                            rdsFd.setDsc("M");
                    }
                }
                logger.info("Multiple shipment container:" + ctrno + "\tDSC Assigned:" + rdsFd.getDsc());
            }
            //
            rdsDataFinal.set(i, rdsFd);
        }
        logger.info("*********MDCCHK END*********");
        return rdsDataFinal;
    }

    public static ArrayList<TosRdsDataFinalMt> dsTransformationCON2CY(
            ArrayList<TosRdsDataFinalMt> rdsDataFinal) {
        for (int i = 0; i < rdsDataFinal.size(); i++) {
            TosRdsDataFinalMt rdsFd = rdsDataFinal.get(i);
            String cgnotes = rdsFd.getCargoNotes();
            cgnotes = cgnotes == null ? "" : cgnotes;
            String ds = rdsFd.getDs();
            ds = ds == null ? "" : ds;
            String commodity = rdsFd.getCommodity();
            commodity = commodity == null ? "" : commodity;
            String typeCode = rdsFd.getTypeCode();
            typeCode = typeCode == null ? "" : typeCode;
            //
            if (ds.equals("CON"))
                cgnotes = "LINKED CON " + cgnotes;
            else if (ds.equals("RO"))
                cgnotes = "LINKED RO " + cgnotes;
            cgnotes = cgnotes.length() >= 255 ? cgnotes.substring(0, 255)
                    : cgnotes;
            rdsFd.setCargoNotes(cgnotes);
            if (ds.equals("CON") || ds.equals("RO")) {
                if (typeCode.length() > 0
                        && typeCode.substring(0, 1).equals("V")) {
                    ds = "CY";
                    commodity = "AUTOCY";
                } else if (ds.equals("RO")) {
                    ds = "AUT";
                    commodity = "AUTO";
                } else {
                    if (typeCode.length() > 0
                            && typeCode.substring(0, 1).equals("F")) {
                        ds = "CON";
                        commodity = "AUTOCON";
                    } else {
                        ds = "CY";
                        commodity = "AUTOCY";
                    }
                }

            }
            rdsFd.setDs(ds);
            rdsFd.setCommodity(commodity);
            //
            rdsDataFinal.set(i, rdsFd);
        }
        return rdsDataFinal;
    }

    public static ArrayList<TosRdsDataFinalMt> MixPortAndPartlotContainerTransformation(
            ArrayList<TosRdsDataFinalMt> rdsDataFinal) {
        for (int i = 0; i < rdsDataFinal.size(); i++) {
            TosRdsDataFinalMt rdsFd = rdsDataFinal.get(i);
            String dsc = rdsFd.getDsc();
            dsc = dsc == null ? "" : dsc;
            String dport = rdsFd.getDport();
            dport = dport == null ? "" : dport;
            if (!dport.equals("MIX") && (dsc.equals("M") || dsc.equals("G"))) {
                rdsFd.setDport("MIX");
            }
            if (!dport.equals("MIX") && dsc.equals("P")) {
                rdsFd.setDport("MIX");
                rdsFd.setDir("IN");
                rdsFd.setConsignee("PARTLOT MIX");
            }
            rdsDataFinal.set(i, rdsFd);
        }
        return rdsDataFinal;
    }

    public static ArrayList<TosRdsDataFinalMt> fixMtys(
            ArrayList<TosRdsDataFinalMt> rdsDataFinal) {
        for (int i = 0; i < rdsDataFinal.size(); i++) {
            TosRdsDataFinalMt rdsFd = rdsDataFinal.get(i);
            String dir = rdsFd.getDir();
            if (dir != null && dir.equalsIgnoreCase("MTY")) {
                rdsFd.setCargoNotes(null);
                rdsFd.setConsignee(null);
                rdsFd.setHsf2(null);
                rdsFd.setHsf6(null);
                rdsFd.setSrstatus("OK");
            }
            rdsDataFinal.set(i, rdsFd);
        }
        return rdsDataFinal;
    }

    public static ArrayList<TosRdsDataFinalMt> setCUStoGtrade(
            ArrayList<TosRdsDataFinalMt> rdsDataFinal) {
        for (int i = 0; i < rdsDataFinal.size(); i++) {
            TosRdsDataFinalMt rdsFd = rdsDataFinal.get(i);
            String trade = rdsFd.getTrade();
            if (trade != null && trade.equals("G")) {
                rdsFd.setHsf5("Y");
            }
            rdsDataFinal.set(i, rdsFd);
        }
        return rdsDataFinal;
    }

    public static ArrayList<TosRdsDataFinalMt> removeGFHold(
            ArrayList<TosRdsDataFinalMt> rdsDataFinal) {
        for (int i = 0; i < rdsDataFinal.size(); i++) {
            TosRdsDataFinalMt rdsFd = rdsDataFinal.get(i);
            String hsf5 = rdsFd.getHsf5();
            String dPort = rdsFd.getDport();
            if (dPort == null)
                dPort = "";
            if (hsf5 != null && hsf5.equals("Y")) {
                String trade = getTradeforPort(dPort);
                if (trade != null
                        && (trade.equalsIgnoreCase("F") || trade
                        .equalsIgnoreCase("G"))) {
                    rdsFd.setHsf5(null);
                    String srstatus = srstatusHoldCheck(rdsFd);
                    rdsFd.setSrstatus(srstatus);
                }
            }
            rdsDataFinal.set(i, rdsFd);
        }
        return rdsDataFinal;
    }

    public static ArrayList<TosRdsDataFinalMt> processAutos(
            ArrayList<TosRdsDataFinalMt> rdsDataFinal) {
        for (int i = 0; i < rdsDataFinal.size(); i++) {
            TosRdsDataFinalMt rdsFd = rdsDataFinal.get(i);
            String ds = rdsFd.getDs();
            if (ds != null && ds.equals("AUT")) {
                rdsFd.setHsf2("V");
                rdsFd.setHsf6(null);
                String srstatus = srstatusHoldCheck(rdsFd);
                rdsFd.setSrstatus(srstatus);
                logger.info("PROCESS AUTOS - "
                        + rdsFd.getContainerNumber() + " " + rdsFd.getDs()
                        + " " + rdsFd.getCommodity() + " " + rdsFd.getHsf2()
                        + " " + rdsFd.getHsf6());
            }
            rdsDataFinal.set(i, rdsFd);
        }
        return rdsDataFinal;
    }

    public static ArrayList<TosRdsDataFinalMt> fixHonContainers(
            ArrayList<TosRdsDataFinalMt> rdsDataFinal,
            ArrayList<TosRdsDataFinalMt> honCtrDataEx) {
        for (int h = 0; h < honCtrDataEx.size(); h++) {
            TosRdsDataFinalMt rdsFHd = honCtrDataEx.get(h);
            String hctrno = rdsFHd.getContainerNumber();
            if (hctrno != null) {
                for (int i = 0; i < rdsDataFinal.size(); i++) {
                    TosRdsDataFinalMt rdsFd = rdsDataFinal.get(i);
                    String ctrno = rdsFd.getContainerNumber();
                    String trade = rdsFd.getTrade();
                    if (ctrno != null) {
                        if (ctrno.equals(hctrno)) {
                            rdsFd.setHsf6("E");
                            if (trade != null
                                    && (trade.equals("G") || trade.equals("F"))) {
                                rdsFd.setHsf5("Y");
                            }
                            rdsDataFinal.set(i, rdsFd);
                        }
                    }
                }
            }
        }
        return rdsDataFinal;
    }

    public static ArrayList<TosRdsDataFinalMt> emptyLiveStockTransformation(
            ArrayList<TosRdsDataFinalMt> rdsDataFinal) {
        for (int i = 0; i < rdsDataFinal.size(); i++) {
            TosRdsDataFinalMt rdsFd = rdsDataFinal.get(i);
            String cargoNotes = rdsFd.getCargoNotes();
            if (cargoNotes != null) {
                if (cargoNotes.contains("EMPTY LIVESTOCK")
                        || cargoNotes.contains("SURFACE EMPTY")) {
                    rdsFd.setErf("E");
                    rdsFd.setOrientation("E");
                }
            }
            rdsDataFinal.set(i, rdsFd);
        }
        return rdsDataFinal;
    }

    public static ArrayList<TosRdsDataFinalMt> updateConsignee(
            ArrayList<TosRdsDataFinalMt> rdsDataFinal,
            ArrayList<TosRdsDataMt> rdsData) {
        logger.info("**** updateConsignee ****");
        for (int i = 0; i < rdsDataFinal.size(); i++) {
            TosRdsDataFinalMt rdsFd = rdsDataFinal.get(i);
            String cargoNotes = rdsFd.getCargoNotes();
            String ds = rdsFd.getDs();
            ds = ds == null ? "" : ds;
            String dir = rdsFd.getDir();
            dir = dir == null ? "" : dir;
            String typeCode = rdsFd.getTypeCode();
            typeCode = typeCode == null ? "" : typeCode;
            String consignee = rdsFd.getConsignee();
            consignee = consignee == null ? "" : consignee;
            String consigneeName = rdsFd.getConsigneeName();
            consigneeName = consigneeName == null ? "" : consigneeName;
            String dport = rdsFd.getDport();
            dport = dport == null ? "" : dport;
            //String mixedDS = ""; //A39
            logger.info("CNTR:" + rdsFd.getContainerNumber() + "\tCNEE:"
                    + consignee + "\tCNOTES:" + cargoNotes);
            if ("IN".equals(dir) && "CY".equals(ds) && !typeCode.endsWith("GB")
                    && !typeCode.endsWith("GR")) {
                if (consignee.equals("")) {
                    rdsFd.setConsignee("REQUIRES CS ACTION");
                    logger.info("REQCSACTION "
                            + rdsFd.getContainerNumber());
                    rdsFd.setTruck("");
                } else if (consignee.equals("UNKNOWN")
                        || consignee.equals("WA")
                        || consignee.equals("WILL ADVISE")) {
                    rdsFd.setConsignee("REQUIRES CS ACTION - " + consignee);
                    logger.info("REQCSACTION- "
                            + rdsFd.getContainerNumber());
                    rdsFd.setTruck("");
                }
            }

			/*if (ds != null && "AUT".equals(ds)) //A39
			{
				logger.info("AUTO rdsFd.getConsignee()---" + rdsFd.getConsignee());
				if (rdsFd.getShipper() != null
						&& rdsFd.getShipper().contains("FORD MOTOR COMPANY"))
				{
					if (rdsFd.getConsignee() != null
							&& (!(rdsFd.getConsignee().contains("BUDGET RENT")
									|| rdsFd.getConsignee().contains(
											"HERTZ RENT")
									|| rdsFd.getConsignee().contains(
											"AVIS RENT") || rdsFd
									.getConsignee().contains("DOLLAR RENT"))) && "AUTOMOBILE".equalsIgnoreCase(rdsFd.getConsignee()))
					{
						rdsFd.setConsignee("AUTO-SPRAY");
					}
					else
					{
						rdsFd.setConsignee("AUTOMOBILE");
					}
				}
				else
				{
					rdsFd.setConsignee("AUTOMOBILE");
				}
			}*/
            if (cargoNotes != null) {
                if (cargoNotes.contains("REQUIRES CS ACTION")) {
                    rdsFd.setConsignee("REQUIRES CS ACTION");
                    rdsFd.setTruck("");
                } else if (cargoNotes.contains("UNAPPROVED VARIANCE")) {
                    rdsFd.setConsignee("UNAPPROVED VARIANCE");
                    cargoNotes = cargoNotes.replaceAll("UNAPPROVED VARIANCE",
                            "").trim();
                    rdsFd.setCargoNotes(cargoNotes);
                    rdsFd.setTruck("");
                }
            }
			/*if (dport.equalsIgnoreCase("MIX")) //A39
			{
				String containerNumber = rdsFd.getContainerNumber();
				ArrayList<TosRdsDataMt> cntrRdsData = new ArrayList<TosRdsDataMt>();
				ArrayList<String> shipperNameList = new ArrayList<String>();
				ArrayList<String> dischargeServiceList = new ArrayList<String>();
				for (int r = 0; r < rdsData.size(); r++)
				{
					TosRdsDataMt tempData = rdsData.get(r);
					if (tempData.getCtrno() != null
							&& tempData.getCtrno().equals(containerNumber))
					{
						cntrRdsData.add(tempData);
						if (tempData.getShipperName() != null
								&& tempData.getShipperName().length() > 0)
						{
							shipperNameList.add(tempData.getShipperName());
						}
						if (tempData.getLoadDischServ() != null
								&& tempData.getLoadDischServ().length() > 0)
						{
							dischargeServiceList.add(tempData
									.getLoadDischServ());
						}

					}
				}
				if (cntrRdsData.size() > 1 && shipperNameList.size() > 1)
				{
					boolean isSameShipper = false;
					String shipper = "";
					for (int s = 0; s < shipperNameList.size(); s++)
					{
						String tempShipper = shipperNameList.get(s);
						if (shipper.equals(""))
							shipper = tempShipper;
						else
						{
							if (shipper.equals(tempShipper))
								isSameShipper = true;
							else
							{
								isSameShipper = false;
								break;
							}
						}
					}
					if (!isSameShipper)
					{
						rdsFd.setConsignee("MIX");
					}
				}

				if (cntrRdsData.size() > 1 && dischargeServiceList.size() > 1)
				{
					mixedDS = "N";
					String localds = "";
					for (int x = 0; x < dischargeServiceList.size(); x++)
					{
						String tempDs = dischargeServiceList.get(x);
						if (localds.equals(""))
							localds = tempDs;
						else
						{
							if (localds.equals(tempDs))
								mixedDS = "N";
							else
							{
								mixedDS = "Y";
								break;
							}
						}
					}
				}

				// Raghu Pattangi
				if ("AUTOMOBILE".equalsIgnoreCase(rdsFd.getConsignee())
						&& "N".equals(mixedDS))
				{
					rdsFd.setConsignee("MIX");
				}
			}*/
            logger.info("CNEE-" + rdsFd.getConsignee());
            rdsDataFinal.set(i, rdsFd);
        }
        return rdsDataFinal;
    }

    public static ArrayList<TosRdsDataFinalMt> updateBlankTrades(
            ArrayList<TosRdsDataFinalMt> rdsDataFinal) {
        for (int i = 0; i < rdsDataFinal.size(); i++) {
            TosRdsDataFinalMt rdsFd = rdsDataFinal.get(i);
            String dPort = rdsFd.getDport();
            String loadPort = rdsFd.getLoadPort();
            String trade = "";
            String loadTrade = "";
            trade = rdsFd.getHazardousOpenCloseFlag();
            if (trade == null || trade.equals("")) {
                trade = CommonBusinessProcessor.getTradeforPort(dPort);
                if (trade == null) {
                    trade = "H";
                }
                loadTrade = CommonBusinessProcessor.getTradeforPort(loadPort);
                if ("G".equals(loadTrade) || "F".equals(loadTrade)) {
                    trade = loadTrade;
                }
                logger.info("BLANK TRADE Updated to " + trade + " for "
                        + rdsFd.getContainerNumber());
                rdsFd.setTrade(trade);
                rdsFd.setHazardousOpenCloseFlag(trade);
            }

            rdsDataFinal.set(i, rdsFd);
        }
        return rdsDataFinal;
    }

    public static ArrayList<TosRdsDataFinalMt> assignTrucker(ArrayList<TosRdsDataFinalMt> rdsDataFinal, ArrayList<TosRdsDataMt> rdsData) {
        logger.info("******* assignTrucker begins *******");
        try {
            if (tosLookUp == null) {
                tosLookUp = new TosLookup();
            }
        } catch (Exception e) {
            nvLogger.addError("", "",
                    "Unable to create TosLookup.<br /> " + e.toString());
        }
        for (int i = 0; i < rdsDataFinal.size(); i++) {
            TosRdsDataFinalMt rdsFd = rdsDataFinal.get(i);
            String dPort = rdsFd.getDport();
            dPort = dPort == null ? "" : dPort;
            String consigneeName = rdsFd.getConsigneeName();
            consigneeName = consigneeName == null ? "" : consigneeName;
            String consignee = rdsFd.getConsignee();
            consignee = consignee == null ? "" : consignee;
            String consigneeQual = rdsFd.getConsigneeQualifier();
            consigneeQual = consigneeQual == null ? "" : consigneeQual;
            String trucker = "";
            // Check NotifyParty from the raw rds for the container.
            String notifyParty = "";
            for (int r = 0; r < rdsData.size(); r++) {
                TosRdsDataMt rds = rdsData.get(r);
                if (rds.getCtrno().equals(rdsFd.getContainerNumber())) {
                    notifyParty = rds.getNotifyParty();
                    if (notifyParty != null && !notifyParty.equals(""))
                        break;
                }
            }
            //if (!consigneeName.equals("")) // A32
            //{
            try {
                logger.info("Container:" + rdsFd.getContainerNumber() + "\tConsigneeName:" + consigneeName);
                //if ("HON".equalsIgnoreCase(rdsFd.getDport()) || isValidNeighborIslandPort(rdsFd.getDport())) //Notifyparty issue
                if ("ANK".equalsIgnoreCase(rdsFd.getDport())) {
                    //if (("HON".equalsIgnoreCase(rdsFd.getDport()) || "S".equalsIgnoreCase(rdsFd.getDsc())) && notifyParty != null && !notifyParty.equals(""))
                    if (("ANK".equalsIgnoreCase(rdsFd.getDport()) || "S".equalsIgnoreCase(rdsFd.getDsc())) && notifyParty != null && !notifyParty.equals("")) {
                        logger.info("Unit " + rdsFd.getContainerNumber() + " DSC is: " + rdsFd.getDsc());
                        if (tosLookUp.isValidTrucker(notifyParty)) {
                            trucker = notifyParty;
                            logger.info("Assigning trucker from NotifyParty:" + trucker);
                        }
                    } else if (!consigneeName.equals("")) // A32
                    {
                        List truckerList = tosLookUp.getTrucker(consignee);
                        if (truckerList != null) {
                            logger.info("truckerList size "
                                    + truckerList.size());
                            if (truckerList.size() == 1) {
                                ArrayList truckerCon = (ArrayList) truckerList
                                        .get(0);
                                String trukcerName = (String) truckerCon.get(0);
                                logger.info("trukcerName  " + trukcerName);
                                String[] truckerArr = trukcerName.split("-");
                                logger.info("truckerArr :" + truckerArr[0]
                                        + " " + truckerArr[1]);
                                String truck = truckerArr[0];
                                if ("O/P".equalsIgnoreCase(truck))
                                    trucker = truck;
                                else {
                                    //if ("HON"
                                    if ("ANK"
                                            .equalsIgnoreCase(rdsFd.getDport())
                                            || "S".equals(rdsFd.getDsc()))
                                        trucker = truck;
                                }
                                logger.info("trucker " + trucker);
                            } else {
                                ArrayList truckerCon = null;
                                String trukcerName = null;
                                boolean isOp = false;
                                boolean isTruckerSet = false;
                                String[] truckerArr = null;
                                for (int l = 0; l < truckerList.size(); l++) {
                                    truckerCon = (ArrayList) truckerList.get(l);
                                    trukcerName = (String) truckerCon.get(0);
                                    if (trukcerName.contains("O/P")) {
                                        isOp = true;
                                        if (isTruckerSet) {
                                            break;
                                        }
                                    } else {
                                        if (!isTruckerSet) {
                                            truckerArr = trukcerName.split("-");
                                            logger.info("truckerArr :"
                                                    + truckerArr[0] + " "
                                                    + truckerArr[1]);
                                            isTruckerSet = true;
                                        }
                                    }
                                }
                                logger.info("trukcerName else " + trukcerName
                                        + " " + isOp + " " + isTruckerSet);

                                if (isOp && truckerArr != null)
                                    trucker = truckerArr[0];
                                else if ("ANK".equalsIgnoreCase(rdsFd
                                        .getDport()))
                                    trucker = truckerArr[0];
                                logger.info("trucker " + trucker);

                            }
                        }
                        logger.info("Assigning trucker from Tos:" + trucker);
                    }
                }
                if (trucker != null)
                    trucker = trucker.length() > 4 ? trucker.substring(0, 4) : trucker;
                rdsFd.setTruck(trucker);
            } catch (Exception e) {
                logger.error("Unable to get trucker for "
                        + rdsFd.getConsigneeName());
            }
            //}
            rdsDataFinal.set(i, rdsFd);
        }
        logger.info("******* assignTrucker end *******");
        return rdsDataFinal;
    }

    public static String srstatusHoldCheck(TosRdsDataFinalMt rdsDataFinal) {
        StringBuilder status = new StringBuilder();
        String hsf2 = rdsDataFinal.getHsf2();
        String hsf3 = rdsDataFinal.getHsf3();
        String hsf4 = rdsDataFinal.getHsf4();
        String hsf5 = rdsDataFinal.getHsf5();
        String hsf6 = rdsDataFinal.getHsf6();
        String trade = rdsDataFinal.getTrade();
        String ds = rdsDataFinal.getDs();
        // hsf2
        if (hsf2 != null && hsf2.contains("H"))
            status = CommonBusinessProcessor.appendStatusString(status, "HP");
        else if (hsf2 != null && hsf2.contains("C"))
            status = CommonBusinessProcessor.appendStatusString(status, "CC");
        // hsf3
        if (hsf3 != null && !hsf3.equals(""))
            status = CommonBusinessProcessor.appendStatusString(status, "ON");

        // hsf5
        if (hsf5 != null && !hsf5.equals("")) {
            if (trade != null && trade.equals("M"))
                status = CommonBusinessProcessor.appendStatusString(status,
                        "INB");
        }
        // hsf6
        if (hsf6 != null && !hsf6.equals("")) {
            if (hsf6.equals("E")) {
                if ((ds != null && !ds.equals("AUT"))) {
                    //	status = CommonBusinessProcessor.appendStatusString(status,
                    //			"DOC");
                }
            }
        }
        logger.info("SRSTATUS HOLDS CHECK : " + status);
        if (status.length() > 0)
            return status.toString().trim();
        return "OK";
    }

    public static ArrayList<TosRdsDataFinalMt> crstatusHoldsCheck1(ArrayList<TosRdsDataFinalMt> rdsDataFinal) {
        logger.info("******* crstatusHoldsCheck1 begin *********");
        for (int i = 0; i < rdsDataFinal.size(); i++) {
            StringBuilder status = new StringBuilder();
            TosRdsDataFinalMt rdsFd = rdsDataFinal.get(i);
            String dsc = rdsFd.getDsc();
            String ds = rdsFd.getDs();
            String srstatus = rdsFd.getSrstatus();
            boolean holdflag = false, cfsflag = false;
            String holdon2 = "", holdon3 = "", holdon4 = "", holdon5 = "", holdon6 = "";
            String hsf2 = rdsFd.getHsf2();
            String hsf3 = rdsFd.getHsf3();
            String hsf4 = rdsFd.getHsf4();
            String hsf5 = rdsFd.getHsf5();
            String hsf6 = rdsFd.getHsf6();
            String typeCode = rdsFd.getTypeCode();
            String trade = rdsFd.getTrade();
            String dport = rdsFd.getDport();
            if (dsc != null && dsc.equals("P")) {
                if (srstatus != null && !srstatus.equals("OK"))
                    status = CommonBusinessProcessor.appendStatusString(status,
                            srstatus);
            }
            if (ds != null && ds.equals("CFS"))
                cfsflag = true;
            logger.info(rdsFd.getContainerNumber() + " --> CFSFLAG=" + cfsflag + " HSF2=" + hsf2 + " HSF3=" + hsf3 + " HSF4=" + hsf4 + " HSF5=" + hsf5 + " HSF6=" + hsf6 + " TYPECODE="
                    + typeCode);
            if (cfsflag) {
                if (hsf5 != null && hsf5.equals("Y")) {
                    holdon5 = "Y";
                    holdflag = true;
                }
            } else {
                if (hsf2 != null && hsf2.contains("H")) {
                    holdon2 = "H";
                    holdflag = true;
                } else if (hsf2 != null && hsf2.contains("C")) {
                    holdon2 = "C";
                    holdflag = true;
                }
                if (hsf3 != null && hsf3.equals("Y")) {
                    holdon3 = "Y";
                    holdflag = true;
                }
                if (hsf4 != null && (hsf4.equals("Y") || hsf4.equals("I") || hsf4.equals("A"))) {
                    if (typeCode != null && (!typeCode.substring(0, 1).equals("R") || typeCode.equalsIgnoreCase("RoRo")))
                        holdon4 = "Y";
                    holdflag = true;
                }
                // added by raghu
                if (hsf5 != null && hsf5.equals("Y")) {
                    holdon5 = "Y";
                    holdflag = true;
                }
                if (hsf6 != null && hsf6.equals("E")) {
                    if (typeCode != null && (!typeCode.endsWith("GR") && !typeCode.endsWith("GB")))
                        holdon6 = "Y";
                    holdflag = true;
                }
            }
            if (holdflag) {
                if (!holdon2.equals("") && holdon2.equals("H"))
                    status = CommonBusinessProcessor.appendStatusString(status,
                            "HP");
                else if (!holdon2.equals("") && holdon2.equals("C"))
                    status = CommonBusinessProcessor.appendStatusString(status,
                            "CC");
                else if (!holdon2.equals("") && !holdon2.equals("C")
                        && !holdon2.equals("H"))
                    status = CommonBusinessProcessor.appendStatusString(status,
                            "HP CC");
                if (!holdon3.equals("") && holdon3.equals("Y"))
                    status = CommonBusinessProcessor.appendStatusString(status,
                            "ON");

                if (!holdon5.equals("") && holdon5.equals("Y")) {
                    if (dport != null
                            && (dport.equals("MAJ") || dport.equals("KWJ")
                            || dport.equals("EBY") || dport
                            .equals("JIS")))
                        status = CommonBusinessProcessor.appendStatusString(
                                status, "INB");
                }
			/*	if (!holdon6.equals("") && holdon6.equals("Y"))
				{
					if (trade == null
							|| (trade != null && !trade.equalsIgnoreCase("G") && !trade
									.equalsIgnoreCase("F")))
						status = CommonBusinessProcessor.appendStatusString(status, "DOC");
				}*/
            }
            String crstatus = status.toString().trim();
            rdsFd.setCrstatus(crstatus);
            rdsDataFinal.set(i, rdsFd);
            logger.info(rdsFd.getContainerNumber() + " CRSTATUS 1: " + crstatus);
        }
        logger.info("******* crstatusHoldsCheck1 end *********");
        return rdsDataFinal;
    }


    public static ArrayList<TosRdsDataFinalMt> crstatusHoldsCheck3(
            ArrayList<TosRdsDataFinalMt> rdsDataFinal) {
        for (int i = 0; i < rdsDataFinal.size(); i++) {
            TosRdsDataFinalMt rdsFd = rdsDataFinal.get(i);
            String owner = rdsFd.getOwner();
            owner = owner == null ? "" : owner;
            String inbond = rdsFd.getHsf5();
            inbond = inbond == null ? "" : inbond;

            logger.info(rdsFd.getContainerNumber() + " CRSTATUS 3: "
                    + rdsFd.getCrstatus());
            rdsDataFinal.set(i, rdsFd);
        }
        return rdsDataFinal;
    }

    public static ArrayList<TosRdsDataFinalMt> crstatusHoldsCheck4(
            ArrayList<TosRdsDataFinalMt> rdsDataFinal) {
        for (int i = 0; i < rdsDataFinal.size(); i++) {
            TosRdsDataFinalMt rdsFd = rdsDataFinal.get(i);
            String crstat = rdsFd.getCrstatus();
            crstat = crstat == null ? "" : crstat;
            boolean dateFlag = false;
            try {
                Date triggerDate = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
                String cYear = new SimpleDateFormat("yyyy").format(triggerDate);
                Date date1 = sdf.parse("10/01/" + cYear);
                Date date2 = sdf.parse("12/25/" + cYear);

                if ((triggerDate.compareTo(date1) >= 0)
                        && (triggerDate.compareTo(date2) <= 0))
                    dateFlag = true;
            } catch (ParseException ex) {
                ex.printStackTrace();
            }

            boolean cNotesFlag = false;
            String cnotes = rdsFd.getCargoNotes();
            if (cnotes != null) {
                if (getPropertyValueAsBoolean("XT_HOLD_ENABLED")) {
                    if ((cnotes.contains("CHRISTMAS BOXES")
                            || cnotes.contains("CHRISTMAS TREE")
                            || cnotes.contains("WREATH")
                            || cnotes.contains("EVERGREEN") || (cnotes.contains("TREE") && !cnotes.contains("SILK TREE")))) {
                        cNotesFlag = true;
                    }
                    if (cNotesFlag && dateFlag) {
                        crstat = crstat + " XT";
                        rdsFd.setCrstatus(crstat);
                        rdsFd.setCommodity("XMASTREE");
                        rdsFd.setLocationRunStackSectn("NO");
                        rdsFd.setStowFlag("C");
                        logger.info("XT HOLD SET FOR "
                                + rdsFd.getContainerNumber());
                    }
                }
                if (cnotes.contains("EMPTY LIVESTOCK")) {
                    String stowFlag = rdsFd.getStowFlag();
                    stowFlag = stowFlag == null ? "" : stowFlag;
                    if (stowFlag.equals("Y"))
                        rdsFd.setStowFlag("M");
                    else
                        rdsFd.setStowFlag("C");
                }
            }
            logger.info(rdsFd.getContainerNumber() + " CRSTATUS 4: "
                    + rdsFd.getCrstatus());
            rdsDataFinal.set(i, rdsFd);
        }
        return rdsDataFinal;
    }

    public static ArrayList<TosRdsDataFinalMt> crstatusHoldsCheck5(
            ArrayList<TosRdsDataFinalMt> rdsDataFinal) {
        for (int i = 0; i < rdsDataFinal.size(); i++) {
            TosRdsDataFinalMt rdsFd = rdsDataFinal.get(i);
            String crstat = rdsFd.getCrstatus();
            crstat = crstat == null ? "" : crstat;
            String ds = rdsFd.getDs();
            ds = ds == null ? "" : ds;
            String consignee = rdsFd.getConsignee();
            consignee = consignee == null ? "" : consignee;
            String loadPort = rdsFd.getLoadPort();
            loadPort = loadPort == null ? "" : loadPort;
            if (ds.equals("CY") && consignee.equals("")) {
                String loadPortTrade = getTradeforPort(loadPort);
                loadPortTrade = loadPortTrade == null ? "" : loadPortTrade;
                if (!loadPortTrade.equals("G") && !loadPortTrade.equals("F")) {
                    if (!crstat.contains("DOC")) {
                        //	crstat = crstat.trim() + " DOC";
                        //	rdsFd.setCrstatus(crstat);
                    }
                }
            }
            logger.info(rdsFd.getContainerNumber() + " CRSTATUS 5: "
                    + rdsFd.getCrstatus());
            rdsDataFinal.set(i, rdsFd);
        }
        return rdsDataFinal;
    }


    public static ArrayList<TosRdsDataFinalMt> multiStopDocHold(
            ArrayList<TosRdsDataFinalMt> rdsDataFinal) {
        for (int i = 0; i < rdsDataFinal.size(); i++) {
            TosRdsDataFinalMt rdsFd = rdsDataFinal.get(i);
            String crstat = rdsFd.getCrstatus();
            crstat = crstat == null ? "" : crstat;
            String ds = rdsFd.getMultiStop();
            ds = ds == null ? "" : ds;
            rdsFd.getBookingNumber();

            if ("N".equalsIgnoreCase(ds)) {

                if (!crstat.contains("DOC")) {
                    crstat = crstat.trim() + " DOC";
                    rdsFd.setCrstatus(crstat);
                }

            }
            logger.info(rdsFd.getContainerNumber() + " multiStopDocHold : "
                    + rdsFd.getCrstatus());
            rdsDataFinal.set(i, rdsFd);
        }
        return rdsDataFinal;
    }

    public static ArrayList<TosRdsDataFinalMt> multiStopDocHoldSupplemental(
            ArrayList<TosRdsDataFinalMt> rdsDataFinal) {

        logger.info("inside multiStopDocHoldSupplemental ");
        for (int i = 0; i < rdsDataFinal.size(); i++) {
            TosRdsDataFinalMt rdsFd = rdsDataFinal.get(i);
            String crstat = rdsFd.getCrstatus();
            crstat = crstat == null ? "" : crstat;
            String ds = rdsFd.getMultiStop();
            ds = ds == null ? "" : ds;
            rdsFd.getBookingNumber();

            logger.info("Value of ds " + ds);
            if ("N".equalsIgnoreCase(ds)) {
                logger.info("Adding DOC hold ");

                if (!crstat.contains("DOC")) {
                    crstat = crstat.trim() + "DOC";
                    rdsFd.setCrstatus(crstat);

                }

            }

            if ("Y".equalsIgnoreCase(ds) || "".equalsIgnoreCase(ds)) {

                if (crstat.contains("DOC")) {
                    logger.info("Removing DOC hold ");
                    crstat = crstat.replace("DOC", "");

                    rdsFd.setCrstatus(crstat);

                }

                if ("".equalsIgnoreCase(ds)) {

                    logger.info("inside commodity check");

                    if (rdsFd.getCommodity() != null) {

                        logger.info("Commodity is " + rdsFd.getCommodity());
                    }

                }

            }
            logger.info(rdsFd.getContainerNumber() + " multiStopDocHoldSupplemental : "
                    + rdsFd.getCrstatus());
            rdsDataFinal.set(i, rdsFd);
        }
        return rdsDataFinal;
    }

    public static ArrayList<TosRdsDataFinalMt> checkIDStrapFlatR(ArrayList<TosRdsDataFinalMt> rdsDataFinal) {
        logger.info("checkIDStrapFlatR begin *****");
        ArrayList<TosRdsDataFinalMt> toBeUpdated = new ArrayList<TosRdsDataFinalMt>();
        ArrayList<TosRdsDataFinalMt> tempList = new ArrayList<TosRdsDataFinalMt>();
        tempList.addAll(rdsDataFinal);
        for (int i = 0; i < rdsDataFinal.size(); i++) {
            TosRdsDataFinalMt rdsFd = rdsDataFinal.get(i);
            if (rdsFd.getTypeCode() != null && rdsFd.getTypeCode().startsWith("F40") && rdsFd.getDir() != null && rdsFd.getDir().equals("MTY")) {
                for (int t = 0; t < tempList.size(); t++) {
                    TosRdsDataFinalMt temp = tempList.get(t);
                    if (!temp.getContainerNumber().equals(rdsFd.getContainerNumber()) && rdsFd.getCell() != null && temp.getCell() != null && rdsFd.getCell().equals(temp.getCell())) {
                        if (temp.getTypeCode() != null && (temp.getTypeCode().substring(1, 3).equals("20") || temp.getTypeCode().substring(1, 3).equals("24"))) {
                            rdsFd.setDir("IN");
                            rdsFd.setOrientation("E");
                            rdsFd.setErf("E");
                            rdsFd.setDport("ANK");
                            rdsFd.setBookingNumber("0000000");
                            rdsFd.setConsignee("MATSON CY");
                            rdsFd.setCargoNotes("Strapped to " + temp.getContainerNumber());
                            rdsFd.setStowFlag("C");
                            rdsFd.setGateSeqNo(temp.getContainerNumber());
                            temp.setStowFlag("C");
                            String crstat = "RD " + (temp.getCrstatus() == null ? "" : temp.getCrstatus());
                            temp.setCrstatus(crstat.trim());
                            temp.setComments("STRAPPED!" + (temp.getComments() == null ? "" : temp.getComments()));
                            toBeUpdated.add(rdsFd);
                            toBeUpdated.add(temp);
                        }
                    }
                }
            }
        }
        if (toBeUpdated.size() > 0) {
            logger.info("No of IDStrapFlatR : " + toBeUpdated.size());
            for (int u = 0; u < toBeUpdated.size(); u++) {
                TosRdsDataFinalMt upd = toBeUpdated.get(u);
                for (int r = 0; r < rdsDataFinal.size(); r++) {
                    TosRdsDataFinalMt rdsFd = rdsDataFinal.get(r);
                    if (upd.getContainerNumber().equals(rdsFd.getContainerNumber())) {
                        rdsDataFinal.set(r, upd);
                        logger.info(rdsFd.getContainerNumber() + " is being updated: crstatus:" + upd.getCrstatus());
                    }
                }
            }
        }
        logger.info("checkIDStrapFlatR end *****");
        return rdsDataFinal;
    }

    public static ArrayList<TosRdsDataFinalMt> crstatusHoldsCheck6(
            ArrayList<TosRdsDataFinalMt> rdsDataFinal,
            ArrayList<TosStowPlanCntrMt> ocrDataList) {
        for (int i = 0; i < rdsDataFinal.size(); i++) {
            TosRdsDataFinalMt rdsFd = rdsDataFinal.get(i);
            String ctrno = rdsFd.getContainerNumber();
            String crstatus = rdsFd.getCrstatus();
            crstatus = crstatus == null ? "" : crstatus.trim();
            for (int c = 0; c < ocrDataList.size(); c++) {
                TosStowPlanCntrMt stowCntrMt = ocrDataList.get(c);
                String ctrNbr = stowCntrMt.getContainerNumber();
                if (ctrNbr != null && ctrNbr.equalsIgnoreCase(ctrno)) {
                    Set<TosStowPlanHoldMt> stowHoldMt = stowCntrMt
                            .getTosStowPlanHoldMts();
                    if (stowHoldMt != null && stowHoldMt.size() > 0) {
                        Iterator<TosStowPlanHoldMt> itrHold = stowHoldMt
                                .iterator();
                        logger.info(ctrno + " has hold in HOLDMT table");
                        String holds = "";
                        while (itrHold.hasNext()) {
                            TosStowPlanHoldMt holdData = itrHold.next();
                            holds = holds + " " + holdData.getId().getCode();
                        }
                        holds = holds == null ? "" : holds;

                        if (holds.contains("HP")) {
                            if (!crstatus.contains("HP"))
                                crstatus = crstatus + " HP";
                        }
                        if (holds.contains("CC")) {
                            if (!crstatus.contains("CC"))
                                crstatus = crstatus + " CC";
                        }
                        if (holds.contains("ON")) {
                            if (!crstatus.contains("ON"))
                                crstatus = crstatus + " ON";
                        }

                        rdsFd.setCrstatus(crstatus);
                        rdsDataFinal.set(i, rdsFd);
                        System.out
                                .println(rdsFd.getContainerNumber()
                                        + " HOLDSMT CRSTATUS 6: "
                                        + rdsFd.getCrstatus());
                    }
                }
            }
        }
        return rdsDataFinal;
    }

    /*
	 * This method is used to get the trade from cas referrence service.
	 */
    public static String getTradeforPort(String portCode) {
        logger.info("CommonBusinessProcessor.getTradeforPort begin");
        String portTrade = null;
        try {
            logger.info("portCodeTradeMap " + portCodeTradeMap);
            if (portCodeTradeMap != null && portCodeTradeMap.size() > 0) {
                portTrade = (String) portCodeTradeMap.get(portCode);
            } else {
                portCodeTradeMap = new HashMap();
            }
            logger.info("portTrade here is :" + portTrade);
            if (portTrade == null) {
                logger.info("Before calling cityclient: " + portCode);
                // logger.info("URL BEFORE- "+TosRefDataUtil.getValue("CAS_REF_WS_URI")+" - "+TosRefDataUtil.getValue("CAS_REF_WS_USER_NAME")+" - "+TosRefDataUtil.getValue("CAS_REF_WS_PASSWORD"));
                com.matson.cas.erd.client.CityClient tosCityClient = new com.matson.cas.erd.client.CityClient();
                // logger.info("URL AFTER- "+TosRefDataUtil.getValue("CAS_REF_WS_URI")+" - "+TosRefDataUtil.getValue("CAS_REF_WS_USER_NAME")+" - "+TosRefDataUtil.getValue("CAS_REF_WS_PASSWORD"));
                tosCityClient.setUrl(TosRefDataUtil.getValue("CAS_REF_WS_URI"));
                tosCityClient.setUsername(TosRefDataUtil
                        .getValue("CAS_REF_WS_USER_NAME"));
                tosCityClient.setPassword(TosRefDataUtil
                        .getValue("CAS_REF_WS_PASSWORD"));

                CityVO cityVO = tosCityClient.getCity(portCode);
                if (cityVO.isGuamTrade()) {
                    portTrade = "G";
                } else if (cityVO.isHawaiiTrade()) {
                    portTrade = "H";
                } else if (cityVO.isFarEastTrade()) {
                    portTrade = "F";
                } else if (cityVO.isMidPacificTrade()) {
                    portTrade = "M";
                } else if (cityVO.isAlaskaTrade()) {
                    portTrade = "A"; // TODO: Need to check the value.
                } else if (cityVO.isMainlandTrade()) {
                    portTrade = "H"; // TODO: Need to check the value.
                }
                portCodeTradeMap.put(portCode, portTrade);
            }
        } catch (Exception e) {
            logger.error("Exception in getTradeforPort Method");
            e.printStackTrace();
        }
        logger.info("CommonBusinessProcessor.getTradeforPort end");
        return portTrade;
    }

    public static VesselVO getVesselDetails(String vessel) {
        logger.info("CommonBusinessProcessor.getVesselDetails begin " + vessel);
        VesselVO vesselVo = null;
        try {
            if (vesselDetailsMap != null && vesselDetailsMap.size() > 0) {
                vesselVo = (VesselVO) vesselDetailsMap.get(vessel);
            } else {
                vesselDetailsMap = new HashMap();
            }
            if (vesselVo == null) {
                TosCommonVesselClient tosVesselClinet = new TosCommonVesselClient();
                vesselVo = tosVesselClinet.getVessel(vessel);
                vesselDetailsMap.put(vessel, vesselVo);
            }
        } catch (Exception e) {
            logger.info("Exception in getVesselDetails Method \n" + e);
            e.printStackTrace();
        }
        logger.info("CommonBusinessProcessor.getVesselDetails end");
        return vesselVo;
    }

    public static Date getArrivalDateByVVD(String vvd, String portCode) {
        Date arrivalDate = null;
        try {
            logger.info("getArrivalDateByVVD begin :" + vvd + "-"
                    + portCode);
            if (arrivalDateMap != null && arrivalDateMap.size() > 0) {
                arrivalDate = (Date) arrivalDateMap.get(vvd + portCode);
            } else {
                arrivalDateMap = new HashMap();
            }
            // logger.info("arrivalDateMap :" + arrivalDateMap
            // + " arrivalDate :" + arrivalDate);
            if (arrivalDate == null
                    && !arrivalDateMap.containsKey(vvd + portCode)) {
                // logger.info("Calling FFS");
                logger.info("Get arrival date for " + vvd + " " + portCode);
                VesselScheduleLookup vesLoopUp = new VesselScheduleLookup();
                arrivalDate = vesLoopUp.getArrivalDateByVVD(vvd, portCode);
                logger.info(vvd + " Arrival date - " + arrivalDate);
                arrivalDateMap.put(vvd + portCode, arrivalDate);
            }
        } catch (Exception e) {
            logger.error("Error in retreiving arrival date for " + vvd + " "
                    + portCode);
            logger.error(e);
        }
        return arrivalDate;
		/*
		 * logger.info("Testing next guam vessel 1");
		 * VesselScheduleLookup vesLoopUp = new VesselScheduleLookup();
		 * //HashMap vesMap = vesLoopUp.getNextOutBoundVesvoy("GUM",
		 * Calendar.getInstance(), "MAT"); String outVesvoy =
		 * getNextOutboundVesselForPort("SHA",new Date()); //Date newDate =
		 * vesLoopUp.getArrivalDateByVVD("MKI131", "HON");
		 * //logger.info("vesMap in CommonBusinessProcessor :"+vesMap);
		 * logger.info("vesMap1 in CommonBusinessProcessor :"+outVesvoy);
		 */
    }

    /**
     * This method gets arrival date from FSS scedule and then adds 2 days to
     * that arrival date to calculate the available date. 04/17/2013 - Karthik -
     * Changed to get Available date from TosLookup, If not then get it form FSS
     *
     * @param vvd
     * @param portCode
     * @return
     */
    public static String getAvailableDateByVVD(String vvd, String portCode) {
        logger.info("******* getAvailableDateByVVD begins *******");
        //
        String availDateStr = null;
        //
        // Check vessel availability in Tos
        try {
            if (tosLookUp == null) {
                tosLookUp = new TosLookup();
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error:TosLookup for getAvailableDateByVVD \n " + e);
        }
        availDateStr = tosLookUp.getBeginReceive(vvd, "ANK");
        if (availDateStr != null && availDateStr.length() > 1) // Return if the
        // date is from
        // TOS.
        {
            logger.info("******* getAvailableDateByVVD end *******");
            return availDateStr;
        }
        // Continue here to get date from FSS since TOS didn't return any date.
        Date arrDate = null;
        Date availableDate = null;
        VesselVO vesselVo = null;
        String vesselOperator = null;
        int noOfDays = 0;
        Calendar calendarInstance = Calendar.getInstance();
        try {
            VesselScheduleLookup lookUp = new VesselScheduleLookup();
            arrDate = lookUp.getArrivalDateByVVD(vvd, portCode);
            logger.info("arrDate from FSS :" + arrDate);
            if (arrDate != null) {
                if (vvd != null) {
                    vesselVo = getVesselDetails(vvd.substring(0, 3));
                }
                if (vesselVo != null) {
                    vesselOperator = vesselVo.getVessOpr();
                    logger.info("vessel operator in getAvailableDateByVVD "
                            + vesselOperator);
                }
                if ("MAT".equals(vesselOperator)) {
                    noOfDays = Integer
                            .parseInt(TDPConstants.NO_OF_DAYS_AVAILDATE_MATSON);
                } else {
                    System.out
                            .println("TDPConstants.NO_OF_DAYS_AVAILDATE_NON_MATSON  "
                                    + TDPConstants.NO_OF_DAYS_AVAILDATE_NON_MATSON);
                    noOfDays = Integer
                            .parseInt(TDPConstants.NO_OF_DAYS_AVAILDATE_NON_MATSON);
                }
                logger.info("noOfDays in getAvailableDateByVVD " + noOfDays);
                calendarInstance.setTime(arrDate);
                calendarInstance.add(Calendar.DATE, +noOfDays);
                availableDate = calendarInstance.getTime();
                if (availableDate != null) {
                    availDateStr = CalendarUtil
                            .convertDateToString(availableDate);
                }
            }
            logger.info("availDateStr-" + availDateStr);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        logger.info("******* getAvailableDateByVVD end *******");
        return availDateStr;
    }

    /**
     * This methos takes equipment number (container number or chassis number)
     * and gets the list of events that fall between the start date and end date
     * for the equipment passed. Then loop through the resulst set and get the
     * discharge port of latest record from the result set. eventEndDate -
     * current date eventStartDate - 2 weeks back date from current date.
     *
     * @param equipmentNumber
     */
    public static EquipmentDetails getEquipmentDetailsFromGEMS(
            String equipmentNumber) {
        logger.info("getEquipmentDetailsFromGEMS begin :" + equipmentNumber);
        EquipmentDetails equipmentDtls = new EquipmentDetails();
        EquipmentDetails localequipmentDtls = null;
        List equipmentDtlsList = new ArrayList();
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
                "/gems-app-context.xml");
        logger.info("loaded 1");
        GEMSInterface gemsInterface = (GEMSInterface) ctx
                .getBean("gemsInterface");
        logger.info("loaded 2");
        Date startDate = null;
        Date endDate = null;
        try {
            equipmentDtls.setEquipmentId(equipmentNumber);
            endDate = new Date();
            startDate = new Date();
            Calendar calendarInstance = Calendar.getInstance();
            calendarInstance.setTime(startDate);
            calendarInstance.add(Calendar.DATE, -10);
            startDate = calendarInstance.getTime();
            logger.info("Start date :" + startDate + " End date :"
                    + endDate);
            equipmentDtls.setStartEventDate(startDate);
            equipmentDtls.setEndEventDate(new Date());
            equipmentDtlsList.add(equipmentDtls);
            GEMSResults result = gemsInterface
                    .gemsContainerDetails(equipmentDtlsList);
            if (result != null && result.getResults().size() > 0) {
                for (int k = 0; k < result.getResults().size(); k++) {
                    localequipmentDtls = (EquipmentDetails) result.getResults()
                            .get(k);
                    if (localequipmentDtls.getDischargePortCode() != null) {
                        break;
                    } else {
                        continue;
                    }
                }
            }
            logger.info("result size is :" + result.getResults().size());
        } catch (Exception ex) {
            logger.error("getEquipmentDetailsFromGEMS exception :" + ex);
            ex.printStackTrace();
        }
        logger.info("getEquipmentDetailsFromGEMS end");
        return localequipmentDtls;
    }

    /**
     * This methd is used to archive the files into FTP location based on the
     * proxy id passed. The file that is passed here should have the timestamp
     * appended. * @param proxyId
     *
     * @param archFile
     * @throws FtpBizException
     */
    public static void archivetoFTP(String contents, int proxyId,
                                    String archFile, String subDirectory) throws FtpBizException {
        logger.info("CommonBusinessProcessor.archivetoFTP begin");

        if (proxyId > 0 && archFile != null) {
            logger.info("File name inside FTP Utils>>>>" + archFile);
            FtpProxySenderBiz sender = new FtpProxySenderBiz();
            if (subDirectory == null) {
                sender.sendFile(proxyId, archFile, contents);
            } else {
                sender.sendFile(proxyId, archFile, contents, subDirectory);
            }
        } else {
            logger.info("Either proxy Id or file is not available");
        }
        logger.info("CommonBusinessProcessor.archivetoFTP end");
    }

    public static void archiveNVtoFTP(File archFile, int proxyId,
                                      boolean skipFlag) throws FtpBizException {
        logger.info("CommonBusinessProcessor.archivetoFTP begin");

        if (proxyId > 0 && archFile != null) {
            logger.info("File name inside FTP Utils>>>>" + archFile);

            FtpProxySenderBiz sender = new FtpProxySenderBiz();
            sender.sendFile(proxyId, archFile, skipFlag);
            System.out
                    .println("successfully moved output file to FTP location");
            logger.info("successfully moved output file to FTP location");
        } else {
            logger.info("Either proxy Id or file is not available");
        }
        logger.info("CommonBusinessProcessor.archivetoFTP end");
    }

    public static void archiveNVtoFTPSub(File archFile, int proxyId,
                                         boolean skipFlag, String subDirectory) throws FtpBizException {
        logger.info("CommonBusinessProcessor.archiveNVtoFTPSub begin");

        if (proxyId > 0 && archFile != null) {
            logger.info("File name inside FTP Utils>>>>" + archFile);

            FtpProxySenderBiz sender = new FtpProxySenderBiz();
            sender.sendFile(proxyId, archFile, skipFlag, subDirectory);
            System.out
                    .println("successfully moved output file to FTP location");
            logger.info("successfully moved output file to FTP location");
        } else {
            logger.info("Either proxy Id or file is not available");
        }
        logger.info("CommonBusinessProcessor.archiveNVtoFTPSub end");
    }

    /**
     * This method will remove the files from FTP location based on the proxy id
     * passed.
     *
     * @param proxyID
     * @param fileName
     * @throws Exception
     */
    public static void deleteFtpFiles(int proxyID, String fileName,
                                      String subDir) throws Exception {
        logger.info("CommonBusinessProcessor.deleteFtpFiles begin");
        if (fileName != null) {
            FtpProxyDeleterBiz ftpProxyDeleterBiz = new FtpProxyDeleterBiz();
            logger.info("FTP Deleting file:-" + fileName + " from proxyID:-"
                    + proxyID);
            if (subDir == null) {
                ftpProxyDeleterBiz.removeFile(proxyID, fileName);
            } else {
                ftpProxyDeleterBiz.removeFile(proxyID, fileName, subDir);
            }
        }
        logger.info("CommonBusinessProcessor.deleteFtpFiles end");
    }

    public static boolean isValidNeighborIslandPort(String port) {
        if (port != null
                && (port.equals("HIL") || port.equals("KAH")
                || port.equals("LNI") || port.equals("MOL")
                || port.equals("NAW") || port.equals("KHI")))
            return true;

        return false;
    }

    public static void updateHazfByDcmCheck(String vesvoy) {
        NewVesselDao.updateHazfByDcmCheck(vesvoy);
    }

    public static boolean isDryWall(String cargoNotes, String consignee) {
        if (consignee.contains("KILLEBREW")) {
            if (cargoNotes.contains("PLASTERBOARD")
                    || cargoNotes.contains("PLASTER BOARD")
                    || cargoNotes.contains("GYPSUM")
                    || cargoNotes.contains("WALL BOARD")
                    || cargoNotes.contains("WALLBOARD"))
                return true;
        }
        return false;
    }

    public static String calcLastFreeDays(String availDate, int nod) {
        try {
            if (availDate != null) {
                Date availableDate = new SimpleDateFormat("MM/dd/yyyy")
                        .parse(availDate);
                Date lastFreeDate = CalendarUtil.addCalendarDays(availableDate,
                        nod - 1);
                if (lastFreeDate != null)
                    return CalendarUtil.convertDateToString(lastFreeDate);
            }
        } catch (ParseException e) {
            logger.error(e);
        }
        return "";
    }

    public static String calcDueDate(String availDate, int nod, String type) {
        if (tosCalendarList == null) {
            try {
                if (tosLookUp == null) {
                    tosLookUp = new TosLookup();
                }
                tosCalendarList = tosLookUp.getCalendar();
                if (tosCalendarList != null) {
                    tosCalendarList.remove(0);
                    tosCalendarList.remove(1);
                }
            } catch (Exception e) {
                logger.error("Error in retreiving Tos calendar.");
                logger.error(e);
            }
        }
        try {
            if (availDate != null) {
                Date availableDate = new SimpleDateFormat("MM/dd/yyyy")
                        .parse(availDate);
                Date dueDate = null;
                if (type.equals("CALENDAR"))
                    dueDate = CalendarUtil.addCalendarDays(availableDate,
                            nod - 1);
                else if (type.equals("BIZ"))
                    dueDate = CalendarUtil.addBusinessDays(availableDate, nod,
                            tosCalendarList);
                if (dueDate != null)
                    return CalendarUtil.convertDateToString(dueDate);
            }
        } catch (ParseException e) {
            logger.error(e);
        }

        return "";
    }

    public static ArrayList<TosRdsDataFinalMt> calculateLastFreeDayDueDate(
            ArrayList<TosRdsDataFinalMt> rdsDataFinal) {
        HashMap<String, String> vesselAvailableDateMap = new HashMap<String, String>();
        for (int i = 0; i < rdsDataFinal.size(); i++) {
            TosRdsDataFinalMt rdsFd = rdsDataFinal.get(i);
            String vesvoy = rdsFd.getVesvoy();
            String leg = rdsFd.getLeg();
            String vvd = vesvoy + leg;
            String vesselAvailableDate = "";
            String tempVesselAvailableDate = "";
            String cargoNotes = rdsFd.getCargoNotes();
            cargoNotes = cargoNotes == null ? "" : cargoNotes;
            String consignee = rdsFd.getConsignee();
            consignee = consignee == null ? "" : consignee;
            String temperature = rdsFd.getTemp();
            temperature = temperature == null ? "" : temperature;
            String lastFreeDay = "";
            String dueDate = "";

            if (!vesselAvailableDateMap.containsKey(vvd)) {
                tempVesselAvailableDate = CommonBusinessProcessor
                        .getAvailableDateByVVD(vvd, "ANK");
                logger.info(vvd
                        + " - IS NOT IN THE MAP - ADDING DATE NOW "
                        + tempVesselAvailableDate);
                if (tempVesselAvailableDate != null)
                    vesselAvailableDateMap.put(vvd, tempVesselAvailableDate);
            }
            vesselAvailableDate = vesselAvailableDateMap.get(vvd);
            if (vesselAvailableDate != null && !vesselAvailableDate.equals("")) {
                // Set available date in ARRDATE field
                rdsFd.setArrdate(CalendarUtil
                        .convertStrgToDateFormat(vesselAvailableDate));
                //
                String due6Days = CommonBusinessProcessor.calcDueDate(
                        vesselAvailableDate, 6, "BIZ");
                String due7Days = CommonBusinessProcessor.calcDueDate(
                        vesselAvailableDate, 7, "BIZ");
                String due10Days = CommonBusinessProcessor.calcDueDate(
                        vesselAvailableDate, 10, "BIZ");
                String due8CalendarDays = CommonBusinessProcessor.calcDueDate(
                        vesselAvailableDate, 8, "CALENDAR");
                String due10CalendarDays = CommonBusinessProcessor.calcDueDate(
                        vesselAvailableDate, 10, "CALENDAR");
                String srv = rdsFd.getSrv();
                srv = srv == null ? "" : srv;
                String dport = rdsFd.getDport();
                dport = dport == null ? "" : dport;
                String ds = rdsFd.getDs();
                ds = ds == null ? "" : ds;
                String milTcn = rdsFd.getMilTcn();
                milTcn = milTcn == null ? "" : milTcn;
                String typeCode = rdsFd.getTypeCode();
                typeCode = typeCode == null ? "" : typeCode;
                String commodity = rdsFd.getCommodity();
                commodity = commodity == null ? "" : commodity;
                if (srv.equals("MAT")) {
                    if (dport.equals("ANK")
                            && (ds.equals("CY") || ds.equals("CON"))) {
                        // Detention Due Date
                        if (!milTcn.equals("")) {
                            if (typeCode.startsWith("R"))
                                dueDate = due8CalendarDays;
                            else
                                dueDate = due10CalendarDays;
                        } else if (commodity.equals("XMAS40")
                                || commodity.equals("XMASTREE"))
                            dueDate = due7Days;
                        else if (typeCode.startsWith("R"))
                            dueDate = due6Days;
                        else
                            dueDate = due10Days;
                        // Last Free Day
                        if (!milTcn.equals("")) {
                        } else if (typeCode.startsWith("R")) {
                        } else {
                            if (CommonBusinessProcessor.isDryWall(cargoNotes,
                                    consignee)) {
                                lastFreeDay = CommonBusinessProcessor
                                        .calcLastFreeDays(vesselAvailableDate,
                                                15);
                            } else {
                                lastFreeDay = CommonBusinessProcessor
                                        .calcLastFreeDays(vesselAvailableDate,
                                                10);
                            }
                        }
                    }
                }
                if (dueDate != null && dueDate.length() > 0) {
                    rdsFd.setMisc3(dueDate);
                }
                if (lastFreeDay != null && !lastFreeDay.equals("")) {
                    rdsFd.setLocationCategory(CalendarUtil
                            .convertDateStringToString(lastFreeDay, true));
                }
                //
                logger.info(rdsFd.getContainerNumber() + " DATES-->"
                        + vesselAvailableDate + "-->" + lastFreeDay + "-->"
                        + dueDate);
                rdsDataFinal.set(i, rdsFd);
            }
        }
        vesselAvailableDateMap = null;
        return rdsDataFinal;
    }

    /**
     * This method gets the next outbound vessel for a transhipment container
     * based on the discharge port and arrival date. Gets the all the vessels
     * from vessel schedule between the arrival date 30 days after that and pass
     * through HON. This method skips the vessels if the arrival date is after
     * the vessel departure date and eliminates the barges as new vessel
     * considers only long haul vessels.
     *
     * @param port
     * @param arrivalDate
     * @return
     */
    public static String getNextOutboundVesselForPort(String port,
                                                      Date arrivalDate) {
        logger.info("getNextOutboundVesselForPort begin :" + port + "-"
                + arrivalDate);
        String outboundVesvoy = null;
        String startDate = null;
        String startDate1 = null;
        String endDate = null;
        Date arrEndDate = null;
        Date arrStartDate = null;
        HashMap vesselMap = new HashMap();
        try {
            startDate = CalendarUtil.convertDateToString(arrivalDate);
            logger.info("getNextOutboundVesselForPort begin :" + port
                    + "-" + startDate);
            if (outboundVesselMap != null && outboundVesselMap.size() > 0) {
                outboundVesvoy = (String) outboundVesselMap.get(port
                        + startDate);
            } else {
                outboundVesselMap = new HashMap();
            }
            logger.info("outboundVesselMap  :" + outboundVesselMap);
            if (outboundVesvoy == null || "".equals(outboundVesvoy)) {
                if (arrivalDate != null) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(arrivalDate);
                    cal.add(Calendar.DATE, -3);
                    arrStartDate = cal.getTime();
                    startDate1 = CalendarUtil.convertDateToString(arrStartDate);
                    cal.add(Calendar.DATE, 30);
                    arrEndDate = cal.getTime();
                    // converting dates to strig to pass to FSS getShedule
                    // method.
                    endDate = CalendarUtil.convertDateToString(arrEndDate);
                    logger.info("startDate1 " + startDate1
                            + " endDate : " + endDate);
                }
                if (startDate1 != null && endDate != null) {
                    VesselScheduleLookup vesselLookUp = new VesselScheduleLookup();
                    vesselMap = vesselLookUp.getSheduleForOutBoundVes(port,
                            startDate1, endDate, arrivalDate);

                }
                logger.info("vesselMap in getNextOutboundVesselForPort "
                        + vesselMap);
                outboundVesvoy = (String) vesselMap.get(port);
                outboundVesselMap.put(port + startDate, outboundVesvoy);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        logger.info("getNextOutboundVesselForPort end");
        return outboundVesvoy;
    }

    /**
     * This method gets the next outbound vessel for a transhipment container
     * based on the discharge port and arrival date. Gets the all the vessels
     * from vessel schedule between the arrival date 30 days after that and pass
     * through HON. This method skips the vessels if the arrival date is after
     * the vessel departure date and eliminates the barges as new vessel
     * considers only long haul vessels.
     *
     * @param port
     * @param vesselCode
     * @return
     */
    public static String getNextOutboundVesselForPortForBarge(String port,
                                                              String vesselCode) {
        logger.info("getNextOutboundVesselForPort begin :" + port + "-"
                + vesselCode);
        String outboundVesvoy = null;
        String startDate = null;
        Date arrStartDate = null;
        HashMap vesselMap = new HashMap();
        try {

            logger.info("getNextOutboundVesselForPort begin :" + port
                    + "-" + startDate);
            if (outboundVesselMap != null && outboundVesselMap.size() > 0) {
                outboundVesvoy = (String) outboundVesselMap.get(port
                        + startDate);
            } else {
                outboundVesselMap = new HashMap();
            }
            logger.info("outboundVesselMap  :" + outboundVesselMap);
            logger.info("outboundVesvoy:" + outboundVesvoy);
            if (outboundVesvoy == null || "".equals(outboundVesvoy)) {
                // String str_date="14-December-12";
                // DateFormat formatter ;
                // Date arrStartDate ;
                // formatter = new SimpleDateFormat("dd-MMM-yy");
                // arrStartDate = (Date)formatter.parse(str_date);
                Calendar cal = Calendar.getInstance();
                // cal.add(Calendar.DATE,-3);
                arrStartDate = cal.getTime();

                logger.info("arrStartDate:" + arrStartDate
                        + " and vesselCode:" + vesselCode + " port value:"
                        + port);
                VesselScheduleLookup vesselLookUp = new VesselScheduleLookup();
                vesselMap = vesselLookUp.getNextOutboundVoyageForBarge(
                        vesselCode, port, arrStartDate);

                logger.info("vesselMap in getNextOutboundVesselForPort "
                        + vesselMap);
                outboundVesvoy = (String) vesselMap.get(port);
                outboundVesselMap.put(port + startDate, outboundVesvoy);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        logger.info("getNextOutboundVesselForPort end");
        return outboundVesvoy;
    }


    public static ArrayList<TosRdsDataFinalMt> stowRestrictionCodeTransformation(String vesvoy,
                                                                                 ArrayList<TosRdsDataFinalMt> rdsDataFinal) {
        String vesselOpr = "";
        VesselVO vvo = getVesselDetails(vesvoy.substring(0, 3));
        vesselOpr = vvo.getVessOpr();
        for (int i = 0; i < rdsDataFinal.size(); i++) {
            TosRdsDataFinalMt rdsFd = rdsDataFinal.get(i);
            String ds = rdsFd.getDs();
            String commodity = rdsFd.getCommodity();
            String typeCode = rdsFd.getTypeCode();
            //String vesvoy = rdsFd.getVesvoy();
            if (typeCode != null && !typeCode.trim().equals("")) {
                typeCode = typeCode.substring(0, 3);
            }

            if (ds != null && commodity != null && typeCode != null
                    && vesselOpr != null && !vesselOpr.trim().equals("")
                    && !vesselOpr.equals("MAT")) {
                if (ds.equals("CY") && commodity.equals("ALSAUT")) {
                    rdsFd.setStowRestrictionCode("V");
                } else if (ds.equals("CY") && !commodity.equals("ALSAUT")) {
                    if (ds.equals("CY") && typeCode.equals("D45")) {
                        rdsFd.setStowRestrictionCode("5");
                    } else if (ds.equals("CY") && typeCode.equals("D40")) {
                        rdsFd.setStowRestrictionCode("4");
                    } else if (ds.equals("CY") && typeCode.equals("D20")) {
                        rdsFd.setStowRestrictionCode("2");
                    } else if (ds.equals("CY") && typeCode.equals("F40")) {
                        rdsFd.setStowRestrictionCode("6");
                    } else if (ds.equals("CY") && typeCode.equals("D24")) {
                        rdsFd.setStowRestrictionCode("7");
                    }
                } else if (ds.equals("AUT") && typeCode.equals("F40")) {
                    rdsFd.setStowRestrictionCode("F");
                } else if (ds.equals("AUT")
                        && (typeCode.equals("O40") || typeCode.equals("D40"))) {
                    rdsFd.setStowRestrictionCode("O");
                }
            }

            rdsDataFinal.set(i, rdsFd);
        }
        return rdsDataFinal;
    }

    public static BigDecimal fixTareWeight(BigDecimal tareWeight,
                                           String typeCode) {
        if (typeCode == null || typeCode.equals("")) {
            return tareWeight;
        }
        if (tareWeight == null || tareWeight.equals(new BigDecimal(4200))) {
            if (typeCode.length() > 6) {
                String t1 = typeCode.substring(0, 3);
                String t2 = typeCode.substring(0, 6);
                if ("D20".equals(t1)) {
                    tareWeight = new BigDecimal(5000);
                } else if ("D24 86".equals(t2)) {
                    tareWeight = new BigDecimal(5900);
                } else if ("D24 96".equals(t2)) {
                    tareWeight = new BigDecimal(6800);
                } else if ("R24".equals(t1)) {
                    tareWeight = new BigDecimal(7000);
                } else if ("F24".equals(t1)) {
                    tareWeight = new BigDecimal(7200);
                } else if ("D40 86".equals(t2)) {
                    tareWeight = new BigDecimal(8500);
                } else if ("R40 86".equals(t2)) {
                    tareWeight = new BigDecimal(9600);
                } else if ("D40 96".equals(t2)) {
                    tareWeight = new BigDecimal(9600);
                } else if ("D45 96".equals(t2)) {
                    tareWeight = new BigDecimal(10100);
                } else if ("R40 96".equals(t2)) {
                    tareWeight = new BigDecimal(10400);
                } else if ("F40".equals(t1)) {
                    tareWeight = new BigDecimal(11300);
                }
            }
        }

        return tareWeight;
    }

    public static String fixTypeCode(String typeCode, String containerNumber,
                                     String consigneeName) {
        logger.info("typeCode in fixTypeCode " + typeCode + " - "
                + containerNumber);
        if (typeCode.length() > 7) {
            if (containerNumber.startsWith("GEAR")
                    || (consigneeName != null && consigneeName
                    .equals("GEAR CONTAINERS"))) {
                if (!typeCode.endsWith("GR")) {
                    typeCode = typeCode.substring(0, 6) + "GR";
                }
            }
            if (typeCode.endsWith("XX")) {
                typeCode = typeCode.substring(0, 6) + "ST";
            }
        }
        if (typeCode == null || "".equals(typeCode)) {

            GEMSEquipment gemsEquipment = getEquipmentAttributesFromGems(containerNumber);
            if (gemsEquipment != null) {
                typeCode = gemsEquipment.getCode();
                logger.info("Type code from GEMS" + typeCode);
            } else {
                logger.debug("getEquipmentAttributesFromGems returns null while accessing code for "
                        + containerNumber);
            }
        }
        return typeCode;
    }

    public static String calcSupplementalDate(Date today) {
        DateFormat df = new SimpleDateFormat("MM/dd/yy");
        String suppDate = df.format(today);

        if (suppDate != null)
            return suppDate;
        else
            return "";
    }

    public static StringBuilder appendStatusString(StringBuilder sb,
                                                   String status) {
        if (sb.indexOf(status) == -1) {
            sb.append(status + " ");
        }

        return sb;
    }

    // Set all values into RDSFinal table from Stow Plan records for Barge
    // process
    public static ArrayList<TosRdsDataFinalMt> transformBargeDataIntoRDSFinal(
            ArrayList<TosStowPlanCntrMt> stowPlanCntrList) {
        logger.info("******** transformBargeDataIntoRDSFinal begin ********");
        ArrayList<TosRdsDataFinalMt> rdsDataFinal = new ArrayList<TosRdsDataFinalMt>();
        HashMap<String, Abob> ctrsBkgNbrs = new HashMap<String, Abob>();
        String bargeVVD = null;
        for (int i = 0; i < stowPlanCntrList.size(); i++) {
            TosStowPlanCntrMt stowPlan = stowPlanCntrList.get(i);
            TosRdsDataFinalMt rdsFd = new TosRdsDataFinalMt();
            rdsFd.setActualVessel(stowPlan.getActualVessel());
            rdsFd.setActualVoyage(stowPlan.getActualVoyage());
            rdsFd.setAction(stowPlan.getAction());//A44 //use action field to hold longhaul direction
            rdsFd.setAei(stowPlan.getAei());
            rdsFd.setAtime(stowPlan.getAtime());
            rdsFd.setBookingNumber(stowPlan.getBookingNumber());
            rdsFd.setCargoNotes(stowPlan.getCargoNotes());
            rdsFd.setCell(stowPlan.getCell());
            rdsFd.setChassisNumber(stowPlan.getChassisNumber());
            rdsFd.setCheckDigit(stowPlan.getCheckDigit());
            rdsFd.setCneeCode(stowPlan.getCneeCode());
            rdsFd.setComments(stowPlan.getComments());
            rdsFd.setCommodity(stowPlan.getCommodity());
            rdsFd.setConsignee(stowPlan.getConsignee());
            rdsFd.setContainerNumber(stowPlan.getContainerNumber());
            rdsFd.setCreateUser(stowPlan.getCreateUser());
            rdsFd.setCrstatus(stowPlan.getCrstatus());
            rdsFd.setCreateDate(stowPlan.getCreateDate());
            rdsFd.setCweight(stowPlan.getCweight());
            rdsFd.setDamageCode(stowPlan.getDamageCode());
            rdsFd.setDir(stowPlan.getDir());
            rdsFd.setDischargePort(stowPlan.getDischargePort());
            rdsFd.setDoer(stowPlan.getDoer());
            rdsFd.setDport(stowPlan.getDport());
            rdsFd.setDs(stowPlan.getDs());
            rdsFd.setDsc(stowPlan.getDsc());
            rdsFd.setDss(stowPlan.getDss());
            rdsFd.setErf(stowPlan.getErf());
            rdsFd.setGateSeqNo(stowPlan.getGateSeqNo());
            rdsFd.setHazardousOpenCloseFlag(stowPlan
                    .getHazardousOpenCloseFlag());
            rdsFd.setHazf(stowPlan.getHazf());
            rdsFd.setHgt(stowPlan.getHgt());
            rdsFd.setHsf7(stowPlan.getHsf7());
            rdsFd.setLastAction(stowPlan.getLastAction());
            rdsFd.setLastDoer(stowPlan.getLastDoer());
            rdsFd.setLastUpdateUser(stowPlan.getLastUpdateUser());
            rdsFd.setLastUpdateDate(stowPlan.getLastUpdateDate());
            rdsFd.setLeg(stowPlan.getLeg());
            rdsFd.setLoadPort(stowPlan.getLoadPort());
            rdsFd.setLoc(stowPlan.getLoc());
            rdsFd.setLocationCategory(stowPlan.getLocationCategory());
            rdsFd.setLocationRowDeck(stowPlan.getLocationRowDeck());
            rdsFd.setLocationRunStackSectn(stowPlan.getLocationRunStackSectn());
            rdsFd.setLocationStallConfig(stowPlan.getLocationStallConfig());
            rdsFd.setLocationStatus(stowPlan.getLocationStatus());
            rdsFd.setLocationTierStall(stowPlan.getLocationTierStall());
            rdsFd.setMisc1(stowPlan.getMisc1());
            rdsFd.setMisc2(stowPlan.getMisc2());
            rdsFd.setMisc3(stowPlan.getMisc3());
            rdsFd.setOdf(stowPlan.getOdf());
            rdsFd.setOrientation(stowPlan.getOrientation());
            rdsFd.setOwner(stowPlan.getOwner());
            rdsFd.setPlanDisp(stowPlan.getPlanDisp());
            rdsFd.setPmd(stowPlan.getPmd());
            rdsFd.setRetPort(stowPlan.getRetPort());
            rdsFd.setSealNumber(stowPlan.getSealNumber());
            rdsFd.setSectionCode(stowPlan.getSectionCode());
            rdsFd.setShipper(stowPlan.getShipper());
            rdsFd.setShipperPool(stowPlan.getShipperPool());
            rdsFd.setSrv(stowPlan.getSrv());
            rdsFd.setStowFlag(stowPlan.getStowFlag());
            rdsFd.setStowRestrictionCode(stowPlan.getStowRestrictionCode());
            rdsFd.setStrength(stowPlan.getStrength());
            rdsFd.setTemp(stowPlan.getTemp());
            rdsFd.setTempMeasurementUnit(stowPlan.getTempMeasurementUnit());
            rdsFd.setTruck(stowPlan.getTruck());
            rdsFd.setTypeCode(stowPlan.getTypeCode());
            rdsFd.setTareWeight(stowPlan.getTareWeight());
            rdsFd.setVesvoy(stowPlan.getVesvoy());
            if (bargeVVD == null)
                bargeVVD = stowPlan.getVesvoy() + stowPlan.getLeg();
            // TRANSFORMATIONS
            //String SIT_PATTERN = ".*[S].*[I].*[T]";
            String commodity = stowPlan.getCommodity();
            commodity = commodity == null ? "" : new String(commodity);
            commodity = commodity.toUpperCase();
            if (commodity.contains("SIT") || commodity.contains("S I T") || commodity.contains("S.I.T")) {
                rdsFd.setDsc("S");
                rdsFd.setComments("EB SIT");
                rdsFd.setCommodity(MULTISTOP_SIT);
            } else if (stowPlan.getBookingNumber() != null && stowPlan.getBookingNumber().length() > 0) { // A37 starts
                try {
                    if (tosLookUp == null)
                        tosLookUp = new TosLookup();
                    HashMap<String, String> resultMap = tosLookUp.getSITByBooking(stowPlan.getBookingNumber());
                    if (resultMap != null) {
                        String cmdy = resultMap.get("COMMODITY");
                        if (cmdy != null && (cmdy.equalsIgnoreCase("SIT") || cmdy.contains("SIT"))) {
                            rdsFd.setDsc("S");
                            rdsFd.setComments("EB SIT");
                            rdsFd.setCommodity(MULTISTOP_SIT);
                            logger.info("SIT Container : " + stowPlan.getContainerNumber());
                        }
                    }
                } catch (Exception e) {
                    logger.error("Unable to get Commodity from TOS for SIT BKG Check: " + stowPlan.getContainerNumber() + "  " + stowPlan.getBookingNumber() + "\n", e);
                    nvLogger.addError(stowPlan.getVesvoy(), stowPlan.getContainerNumber(), "Unable to get Commodity from TOS for SIT BKG Check: " + stowPlan.getBookingNumber() + "<br/>" + e.getMessage());//A38
                }
            }// A37 ends

            rdsDataFinal.add(rdsFd);
            //A44 - Starts
            if (stowPlan.getBookingNumber() != null && stowPlan.getBookingNumber().length() > 0 && stowPlan.getActualVessel() != null && stowPlan.getActualVoyage() != null) {
                if (ctrsBkgNbrs.containsKey(stowPlan.getBookingNumber())) {
                    continue;
                }
                try {
                    Abob bob = new Abob();
                    bob.setVessel(stowPlan.getActualVessel());
                    bob.setVoyageNbr(stowPlan.getActualVoyage());
                    bob.setDirSeq(stowPlan.getAction());
                    bob.setDischargePort(stowPlan.getDischargePort());
                    bob.setLoadPort(stowPlan.getLoadPort());
                    bob.setBlDestPort(stowPlan.getDport());
                    bob.setBookingNbr(stowPlan.getBookingNumber());
                    if (tosLookUp == null)
                        tosLookUp = new TosLookup();
                    if (stowPlan.getShipper() != null) {
                        bob.setShipperName(stowPlan.getShipper());
                        bob.setShipperId(tosLookUp.getShipper(null, stowPlan.getShipper()));
                    }
                    if (stowPlan.getConsignee() != null) {
                        bob.setConsigneeName(stowPlan.getConsignee());
                        bob.setConsigneeId(tosLookUp.getConsignee(null, stowPlan.getConsignee()));
                    }
                    logger.info("HAZF : " + stowPlan.getHazf());
                    logger.info("HAZSET :" + stowPlan.getTosStowPlanHazMts());
                    if (stowPlan.getHazf() != null && stowPlan.getHazf().trim().length() > 0) {
                        bob.setHazardousInd(stowPlan.getHazf());
                        Set<TosStowPlanHazMt> hazSet = stowPlan.getTosStowPlanHazMts();
                        if (hazSet != null && !hazSet.isEmpty()) {
                            Iterator<TosStowPlanHazMt> itrHaz = hazSet.iterator();
                            while (itrHaz.hasNext()) {
                                TosStowPlanHazMt haz = itrHaz.next();
                                bob.setBfrtUnNa(haz.getUnNumber().toString());
                                bob.setBfrtHzdClazz(haz.getHazClass());
                                logger.info("HAZ RECORD : " + haz.getUnNumber() + "," + haz.getHazClass());
                                break;
                            }
                        }
                    }
                    bob.setCommodity(stowPlan.getCommodity());
                    bob.setPrimaryCarrier(stowPlan.getLocationRowDeck());
                    bob.setLoadType(stowPlan.getErf());
                    bob.setSit(rdsFd.getDsc());
                    bob.setOog(stowPlan.getOdf());
                    bob.setTypeCode(convertTypeCodeToTypeISO(stowPlan.getTypeCode(), stowPlan.getHgt()));
                    ctrsBkgNbrs.put(stowPlan.getBookingNumber(), bob);
                } catch (Exception e) {
                    logger.error("Error while retreiving shipper/consignee id from tos", e);
                }
            }
            //A44 - Ends
        }
        //A44 - Starts
        logger.info("STOWPLAN BKGS : " + ctrsBkgNbrs.keySet());
        if (ctrsBkgNbrs.keySet().size() > 0) {
            try {
                if (tosLookUp == null)
                    tosLookUp = new TosLookup();
                logger.info("******** CHECKING BKG NUMBERS IN TOS *********");
                ArrayList<String> n4BkgNbrs = tosLookUp.searchBkgNbrs(new ArrayList<String>(ctrsBkgNbrs.keySet()));
                logger.info("BKGS FOUND IN N4 : " + n4BkgNbrs);
                if (n4BkgNbrs != null) {
                    for (String bkg : n4BkgNbrs) {
                        ctrsBkgNbrs.remove(bkg);
                    }
                }
                if (ctrsBkgNbrs != null && ctrsBkgNbrs.keySet().size() > 0) {
                    logger.info("BKGS TO BE CREATED : " + ctrsBkgNbrs.keySet());
                    String from = TosRefDataUtil.getValue("EMAIL_NEWVES_FROM");
                    String to = TosRefDataUtil.getValue("MAIL_BARGE_BKGS_TOS");
                    String message = "TOS Team,<br /> The following bookings were not found in TOS, hence they are being generated for " + bargeVVD + " Barge Newves processing.<br />" + ctrsBkgNbrs.keySet();
                    String subject = bargeVVD + " Booking(s) Creation Alert";
                    EmailSender.sendMail(from, to, subject, message);
                    AcetsMessageProcessor amp = new AcetsMessageProcessor();
                    logger.info("*********** CREATE & POST ABOB : BEGIN");
                    for (String bkg : ctrsBkgNbrs.keySet()) {
                        Abob bob = ctrsBkgNbrs.get(bkg);
                        String abobMessage = AbobMessageUtil.constructMessage(bob);
                        logger.info(abobMessage);
                        amp.processMsg(abobMessage, "MQ");
                    }
                    logger.info("*********** CREATE & POST ABOB : END");
                }
            } catch (Exception e) {
                logger.error("Error while trying to create a new bookings", e);
            }
        }
        //A44 - Ends
        cleanUp();
        logger.info("******** transformBargeDataIntoRDSFinal end ********");
        return rdsDataFinal;
    }

    public static String convertTypeCodeToTypeISO(String tCode, String hgt) {
        if (tCode == null || hgt == null)
            return " 0000 ";
        hgt = hgt.substring(0, 4);
        int hgtinch;
        String temp1 = hgt.substring(0, 2);
        String temp2 = hgt.substring(2, 4);
        hgtinch = Integer.parseInt(temp1) * 12 + Integer.parseInt(temp2);
        if (tCode.substring(0, 1).equals("A")) {
            if (hgtinch <= 138)
                return tCode.substring(0, 1) + leftPad(tCode.substring(1, 3), "0", 4) + "L";
            else if (hgtinch >= 152)
                return tCode.substring(0, 1) + leftPad(tCode.substring(1, 3), "0", 4) + "H";
            else
                return tCode.substring(0, 1) + leftPad(tCode.substring(1, 3), "0", 4);
        } else if (tCode.substring(0, 1).equals("F")) {
            if (hgt.equals("1300"))
                return tCode.substring(0, 1) + leftPad(tCode.substring(1, 3), "0", 4) + "M";
            else if (hgtinch <= 96)
                return tCode.substring(0, 1) + leftPad(tCode.substring(1, 3), "0", 4) + "L";
            else if (hgtinch > 102)
                return tCode.substring(0, 1) + leftPad(tCode.substring(1, 3), "0", 4) + "H";
            else
                return tCode.substring(0, 1) + leftPad(tCode.substring(1, 3), "0", 4);
        } else if (tCode.substring(0, 1).equals("R")) {
            if (hgtinch <= 96)
                return tCode.substring(0, 1) + leftPad(tCode.substring(1, 3), "0", 4) + "L";
            else if (hgtinch > 102)
                return tCode.substring(0, 1) + leftPad(tCode.substring(1, 3), "0", 4) + "H";
            else
                return tCode.substring(0, 1) + leftPad(tCode.substring(1, 3), "0", 4);
        } else {
            if (hgtinch <= 96)
                return tCode.substring(0, 1) + leftPad(tCode.substring(1, 3), "0", 4) + "L";
            else if (hgtinch > 102)
                return tCode.substring(0, 1) + leftPad(tCode.substring(1, 3), "0", 4) + "H";
            else
                return tCode.substring(0, 1) + leftPad(tCode.substring(1, 3), "0", 4);
        }
    }

    public static List[] splitList(final List pList, final int pSize) {
        if (pList == null || pList.size() == 0 || pSize == 0)
            return new List[]{};
        if (pSize < 0)
            return new List[]{pList};

        // Calculate the number of batches
        int numBatches = (pList.size() / pSize) + 1;

        // Create a new array of Lists to hold the return value
        List[] batches = new List[numBatches];

        for (int index = 0; index < numBatches; index++) {
            int count = index + 1;
            int fromIndex = Math.max(((count - 1) * pSize), 0);
            int toIndex = Math.min((count * pSize), pList.size());
            batches[index] = pList.subList(fromIndex, toIndex);
        }

        return batches;
    }

    public static ArrayList<TosRdsDataFinalMt> supplementalValidations(ArrayList<TosRdsDataFinalMt> oldSupDataList, ArrayList<TosRdsDataFinalMt> newSupDataList) {
        logger.info("Supplemental validations - Begin");
        ArrayList<TosRdsDataFinalMt> updatedRdsFinal = new ArrayList<TosRdsDataFinalMt>();
        ArrayList<TosRdsDataFinalMt> newContainers = new ArrayList<TosRdsDataFinalMt>();
        supProblemsList = new ArrayList<SupplementProblems>();
        for (int n = 0; n < newSupDataList.size(); n++) {
            TosRdsDataFinalMt newRdsFd = newSupDataList.get(n);
            String nContainerNumber = newRdsFd.getContainerNumber();
            boolean containerFound = false;
            boolean isDifferent = false;
            // Get unit details from TOS
            String tState = null;
            String carrMode = null;
            String eventDate = null;
            String group = null;
            String dport = null;
            String consignee = null;
            String blNbr = null;
            String holds = null;
            String freightKind = null;
            String shipper = null;
            String consigneeId = null;
            String pol = null;
            String pod1 = null;
            String type = null;
            String sealNbr1 = null;
            String category = null;
            String unitNotes = null;
            String strengthCode = null;
            String shipperId = null;
            //
            try {
                if (tosLookUp == null)
                    tosLookUp = new TosLookup();
                HashMap<String, String> resultMap = tosLookUp.getUnitDetails(nContainerNumber);
                if (resultMap != null) {
                    tState = resultMap.get("TRANSIT_STATE");
                    tState = tState == null ? "" : tState;
                    carrMode = resultMap.get("CARRIER_MODE");
                    carrMode = carrMode == null ? "" : carrMode;
                    eventDate = resultMap.get("TIME_MOVE");
                    group = resultMap.get("GRP");
                    group = group == null ? "" : group;
                    dport = resultMap.get("DESTINATION");
                    consignee = resultMap.get("CONSIGNEE");
                    consignee = consignee == null ? "" : consignee;
                    blNbr = resultMap.get("BL_NBR");
                    blNbr = blNbr == null ? "" : blNbr;
					/*blNbr = blNbr.length() >= 7 ? blNbr.substring(0, 7) : blNbr;*/
                    blNbr = blNbr.replace("null", "");
                    holds = resultMap.get("HOLDS");
                    holds = holds == null ? "" : holds;
                    freightKind = resultMap.get("FREIGHT_KIND");
                    freightKind = freightKind == null ? "" : freightKind;
                    shipper = resultMap.get("SHIPPER");
                    consigneeId = resultMap.get("CONSIGNEE_ID");
                    pol = resultMap.get("POL");
                    pod1 = resultMap.get("POD1");
                    type = resultMap.get("EQUIP_TYPE");
                    sealNbr1 = resultMap.get("SEAL_NBR1");
                    unitNotes = resultMap.get("REMARK");
                    unitNotes = unitNotes == null ? "" : unitNotes;
                    category = resultMap.get("CATEGORY");
                    strengthCode = resultMap.get("STRENGTH_CODE");
                    shipperId = resultMap.get("SHIPPER_ID");
                } else {
                    logger.error("Unable to get TosLookup Unit Details from TOS, hence skipping this unit ---->" + nContainerNumber + "<---- from supplemental validations");
                    continue;
                }
            } catch (Exception e) {
                logger.error("Unable to get TosLookup Unit Details from TOS, hence skipping this unit ---->" + nContainerNumber + "<---- from supplemental validations", e);
                continue;
            }
			/*if(category!=null && category.equalsIgnoreCase("THRGH")) {
				logger.info("Skipping ROB/THRGH unit ---->"+ nContainerNumber + "<---- from supplemental processing.");
				continue;
			}*/
            if (freightKind != null && freightKind.equalsIgnoreCase("MTY")) {
                logger.info("Skipping MTY unit ---->" + nContainerNumber + "<---- from supplemental processing.");
                continue;
            }
            //
            for (int o = 0; o < oldSupDataList.size(); o++) {
                TosRdsDataFinalMt oldRdsFd = oldSupDataList.get(o);
                String oContainerNumber = oldRdsFd.getContainerNumber();
                if (nContainerNumber.equals(oContainerNumber)) {
                    containerFound = true;
                    String writeNotes = "";
                    // New supplemental data
                    String nDport = newRdsFd.getDport();
                    nDport = nDport == null ? "" : nDport;
                    String nDs = newRdsFd.getDs();
                    nDs = nDs == null ? "" : nDs;
                    String nDsc = newRdsFd.getDsc();
                    nDsc = nDsc == null ? "" : nDsc;
                    String nCrstatus = newRdsFd.getCrstatus();
                    nCrstatus = nCrstatus == null ? "" : nCrstatus;
                    String nConsignee = newRdsFd.getConsignee();
                    nConsignee = nConsignee == null ? "" : nConsignee;
                    String nShipper = newRdsFd.getShipperName();
                    nShipper = nShipper == null ? "" : nShipper;
                    String nBookingNbr = newRdsFd.getBookingNumber();
                    String tempNBookingNbr = nBookingNbr;
                    tempNBookingNbr = tempNBookingNbr == null ? ""
                            : tempNBookingNbr;
				/*	tempNBookingNbr = tempNBookingNbr.length() >= 7 ? tempNBookingNbr
							.substring(0, 7) : tempNBookingNbr;*/
                    tempNBookingNbr = tempNBookingNbr.replace("null", "");
                    String nPlanDisp = newRdsFd.getPlanDisp();
                    nPlanDisp = nPlanDisp == null ? "" : nPlanDisp;
                    String nVesvoy = newRdsFd.getVesvoy();
                    nVesvoy = nVesvoy == null ? "" : nVesvoy;
                    String nDir = newRdsFd.getDir();
                    nDir = nDir == null ? "" : nDir;
                    String nCargoNotes = newRdsFd.getCargoNotes();
                    nCargoNotes = nCargoNotes == null ? "" : nCargoNotes;
                    String nLocStatus = newRdsFd.getLocationStatus();
                    nLocStatus = nLocStatus == null ? "" : nLocStatus;

                    // Old supplemental data
                    String oDport = oldRdsFd.getDport();
                    oDport = oDport == null ? "" : oDport;
                    String oDs = oldRdsFd.getDs();
                    oDs = oDs == null ? "" : oDs;
                    String oDsc = oldRdsFd.getDsc();
                    oDsc = oDsc == null ? "" : oDsc;
                    String oCrstatus = oldRdsFd.getCrstatus();
                    oCrstatus = oCrstatus == null ? "" : oCrstatus;
                    String oConsignee = oldRdsFd.getConsignee();
                    oConsignee = oConsignee == null ? "" : oConsignee;
                    String oShipper = oldRdsFd.getShipperName();
                    oShipper = oShipper == null ? "" : oShipper;
                    String oBookingNbr = oldRdsFd.getBookingNumber();
                    String tempOBookingNbr = oBookingNbr;
                    tempOBookingNbr = tempOBookingNbr == null ? ""
                            : tempOBookingNbr;
							/*tempOBookingNbr = tempOBookingNbr.length() >= 7 ? tempOBookingNbr
									.substring(0, 7) : tempOBookingNbr;*/
                    tempOBookingNbr = tempOBookingNbr.replace("null", "");
                    String oPlanDisp = oldRdsFd.getPlanDisp();
                    oPlanDisp = oPlanDisp == null ? "" : oPlanDisp;
                    String oVesvoy = oldRdsFd.getVesvoy();
                    oVesvoy = oVesvoy == null ? "" : oVesvoy;
                    String oDir = oldRdsFd.getDir();
                    oDir = oDir == null ? "" : oDir;
                    String oCargoNotes = oldRdsFd.getCargoNotes();
                    oCargoNotes = oCargoNotes == null ? "" : oCargoNotes;
                    String oCommodity = oldRdsFd.getCommodity();
                    oCommodity = oCommodity == null ? "" : oCommodity;
                    String oLocationRowDeck = oldRdsFd.getLocationRowDeck();
                    oLocationRowDeck = oLocationRowDeck == null ? ""
                            : oLocationRowDeck;
                    String oMisc2 = "";
                    String chkDgt = oldRdsFd.getCheckDigit();
                    String sitInfo = "";
                    chkDgt = chkDgt == null ? "X" : chkDgt;
                    try {
                        if (tosLookUp == null) {
                            tosLookUp = new TosLookup();
                        }
                        oMisc2 = tosLookUp.getEditFlag(oContainerNumber + chkDgt);
                        logger.info("Misc2 from TOS for "
                                + oContainerNumber + " is : " + oMisc2);
                        oMisc2 = oMisc2 == null ? " " : oMisc2;

										/*sitInfo=tosLookUp.getSITInfo(oContainerNumber+chkDgt);
						logger.info("sitInfo from TOS for "
								+ oContainerNumber + " is : " + sitInfo);
						sitInfo = sitInfo == null ? " " : sitInfo;*/
                    } catch (Exception e) {
                        logger.error("Unable to get Misc2 field from TOS for "
                                + oContainerNumber + "\n" + e);
                        e.printStackTrace();
                    }
                    // Validations
                    // if(!oMisc2.contains("S")&&!oMisc2.contains("X")&&!oMisc2.contains("K")&&!oMisc2.contains("P")&&!oMisc2.contains("D"))
                    // {
                    if (oMisc2.contains("M") || oMisc2.contains("B") || oMisc2.contains("O") || oMisc2.contains("A") || oMisc2.contains("V") || oMisc2.contains("L") || oMisc2.contains("U") || oMisc2.contains("S")) {
                        isDifferent = true;
                    }
                    logger.info("oContainerNumber is: " + oContainerNumber + "nVesvoy: " + nVesvoy + " oVesvoy: " + oVesvoy + " nDsc :" + nDsc + " nDir :" + nDir);
                    if (nVesvoy.equals(oVesvoy) && !nDsc.equals("T")
                            && !nDsc.equals("P") && nDir.equals("IN")) {
                        logger.info("Supp OLD - " + oContainerNumber
                                + "^" + oDport + "^" + oConsignee + "-"
                                + oShipper + "^" + oDir + "^" + oDs + "^"
                                + oDsc + "^" + oBookingNbr + "^" + oCrstatus
                                + "^" + oCargoNotes);
                        logger.info("Supp New - " + nContainerNumber
                                + "^" + nDport + "^" + nConsignee + "-"
                                + nShipper + "^" + nDir + "^" + nDs + "^"
                                + nDsc + "^" + nBookingNbr + "^" + nCrstatus
                                + "^" + nCargoNotes);
                        logger.info("Supp TOS - " + nContainerNumber
                                + "^" + dport + "^" + consignee + "-"
                                + shipper + "^" + nDir + "^" + nDs + "^"
                                + nDsc + "^" + blNbr + "^" + holds
                                + "^");
                        if (nDport.equalsIgnoreCase(dport) && nConsignee.equalsIgnoreCase(consignee) && nShipper.equalsIgnoreCase(shipper)
												/*&& nConsignee.equalsIgnoreCase(oConsignee) */ && nDs.equalsIgnoreCase(oDs) && nDsc.equalsIgnoreCase(oDsc) &&
                                nCrstatus.equalsIgnoreCase(holds) && tempNBookingNbr.equalsIgnoreCase(blNbr) && nPlanDisp.equalsIgnoreCase(oPlanDisp)
												/*&& nCargoNotes.indexOf(oCargoNotes) == -1 */ && ("".equals(oMisc2) || oMisc2 == null)) { // A36 Removed cargo notes check
                            logger.info("Cotainer skippedd from supplemental processing :" + oContainerNumber);
                            continue;

                        } else {
                            logger.info("Cotainer considered for supplemental processing :" + oContainerNumber);

                            if (!nDs.equalsIgnoreCase(oDs)) {
                                if (oDs.equals("")) {
                                    isDifferent = true;
                                    oldRdsFd.setDs(nDs);
                                    logger.info("Diff: Ds");
                                    writeNotes = writeNotes + " " + oDs + " to " + nDs;
                                } else if (oMisc2.contains("D")) {
                                    continue;
                                } else if (oMisc2.contains("H")) {
                                    continue;
                                } else if (oDport.equals(nDport) && isValidNeighborIslandPort(oDport) && isValidNeighborIslandPort(nDport)) {
                                    isDifferent = true;
                                    oldRdsFd.setDs(nDs);
                                    logger.info("Diff: Ds");
                                    writeNotes = writeNotes + " " + oDs + " to " + nDs;
                                } else if (oDs.equals("CON")) {
                                    isDifferent = true;
                                    oldRdsFd.setDs(nDs);
                                    logger.info("Diff: Ds");
                                    writeNotes = writeNotes + " " + oDs + " to " + nDs;
                                } else {
                                    //addSupplementProblems(DISCREP_DS, oldRdsFd, newRdsFd, tState, eventDate,carrMode, group, dport, consignee, blNbr, holds); //A35
                                    logger.info("Adding to sup problems : " + oContainerNumber + ", " + DISCREP_DS);
                                    continue; // DS Discrepancy, then skip ctr
                                }
                            }
                            if (!nDport.equalsIgnoreCase(dport) && !oMisc2.contains("P")) {
                                if (nDs.equals("CY") || (nDs.equals("AUT") && oDs.equals("AUT"))) {
                                    if (tState != null && !tState.equalsIgnoreCase("null") && !tState.equalsIgnoreCase("S40_YARD") && !tState.equalsIgnoreCase("S20_INBOUND")) {
                                        //addSupplementProblems(DISCREP_DPORT, oldRdsFd, newRdsFd, tState, eventDate,carrMode, group, dport, consignee, blNbr, holds); //A35
                                        logger.info("Adding to sup problems : " + oContainerNumber + ", " + tState + ", " + carrMode + ", " + DISCREP_DPORT);
                                        continue; // DPORT Discrepancy, then skip ctr
                                    }
                                }
                                isDifferent = true;
                                oldRdsFd.setDport(dport);
                                if (tState != null && (tState.equalsIgnoreCase("S40_YARD") || tState.equalsIgnoreCase("S20_INBOUND")) && oDs.equals("AUT")) {
                                    oldRdsFd.setDport("MIX");
                                    writeNotes = writeNotes + " " + oDport + " to MIX";
                                } else {
                                    oldRdsFd.setDport(nDport);
                                    writeNotes = writeNotes + " " + oDport + " to " + nDport;
                                }
                                logger.info("Diff: DPORT");
                                if (dport.equals("ANK")
                                        && oDs.equals("CY")) {
                                    oldRdsFd.setMisc3(null);
                                    oldRdsFd.setLocationCategory(null);
                                } else if (CommonBusinessProcessor
                                        .isValidNeighborIslandPort(oDport)
                                        && oDs.equals("CY")
                                        && nLocStatus.equals("7")) {
                                    oldRdsFd.setMisc3(null);
                                    oldRdsFd.setLocationCategory(null);
                                } else if ("MIX".equalsIgnoreCase(dport)) {
                                    oldRdsFd.setDport(dport);
                                    writeNotes = "";
                                }
                                //when DPORT is changed, we should update the trade from supp
                                oldRdsFd.setTrade(newRdsFd.getTrade());
                                oldRdsFd.setHazardousOpenCloseFlag(newRdsFd.getHazardousOpenCloseFlag());
                            } else if (oMisc2.contains("P")) {
                                oldRdsFd.setDport(dport);
                                if (CommonBusinessProcessor
                                        .isValidNeighborIslandPort(dport))

                                {
                                    oldRdsFd.setMisc3(null);
                                    oldRdsFd.setLocationCategory(null);
                                }
                            }
                            if ((!nConsignee.equalsIgnoreCase(consignee) && !oMisc2
                                    .contains("C"))
                                    && (oDs.equals("CY") || (oDs.equals("AUT") && nDs
                                    .equals("AUT")))) {
                                if (tState != null && !tState.equalsIgnoreCase("null") && tState.equalsIgnoreCase("S70_DEPARTED")
                                        && carrMode != null && !carrMode.equals("null") && carrMode.equalsIgnoreCase("TRUCK")) {
                                    //addSupplementProblems(DISCREP_CNEE, oldRdsFd, newRdsFd, tState, eventDate,carrMode, group, dport, consignee, blNbr, holds); //A35
                                    logger.info("Adding to sup problems : " + oContainerNumber + ", " + tState + ", " + carrMode + ", " + DISCREP_CNEE);
                                    continue; // Consignee Discrepancy, then skip ctr
                                }
                                isDifferent = true;
                                oldRdsFd.setConsignee(nConsignee);
                                writeNotes = writeNotes + " RECON X " + oConsignee;
                                logger.info("Diff: Consignee");
                                oldRdsFd.setCargoNotes(nCargoNotes); // A36 - Update cargo notes only if there is a consignee change
                            } else if (oMisc2.contains("C")) {
                                oldRdsFd.setConsignee(consignee);
                                oldRdsFd.setCneeCode(consigneeId);
                            } else if ("CON".equals(oDs) && !nConsignee.equalsIgnoreCase(oConsignee) && !oMisc2.contains("C")) {
                                if (tState != null && !tState.equalsIgnoreCase("null") && tState.equalsIgnoreCase("S70_DEPARTED")
                                        && carrMode != null && !carrMode.equals("null") && carrMode.equalsIgnoreCase("TRUCK")) {
                                    //addSupplementProblems(DISCREP_CNEE, oldRdsFd, newRdsFd, tState, eventDate,carrMode, group, dport, consignee, blNbr, holds); //A35
                                    logger.info("Adding to sup problems : " + oContainerNumber + ", " + tState + ", " + carrMode + ", " + DISCREP_CNEE);
                                    continue; // Consignee Discrepancy, then skip ctr
                                }
                            }
                            if (!nShipper.equalsIgnoreCase(shipper)
                                    || !oShipper.equalsIgnoreCase(shipper)) {
                                isDifferent = true;
                                logger.info("Diff: Shipper");
                            }
                            if (!nDsc.equalsIgnoreCase(oDsc) && !oMisc2
                                    .contains("S")) {
                                isDifferent = true;
                                oldRdsFd.setDsc(nDsc);
                                logger.info("Diff: Dsc");
                                if (oDsc.equals("S") && !nDsc.equals("S")) {
													/*try { // A33 - Starts
														logger.info("Get Unit Remark from TOS");
														if(tosLookUp==null)
															tosLookUp = new TosLookup();
														String remark = tosLookUp.getUnitRemark(oContainerNumber);
														if(remark!=null) {
															remark = "~1" + remark;
															remark = remark.length()> 65?remark.substring(0, 65):remark;
															oldRdsFd.setCargoNotes(remark);
															nCargoNotes = remark;
														}
													} catch(Exception e) {*/
                                    //logger.error("Error in get unit remark from TOS for SIT removal \n ", e);
                                    //nCargoNotes = "~1" + nCargoNotes;
                                    //oldRdsFd.setCargoNotes(nCargoNotes);
                                    oCargoNotes = "~1" + oCargoNotes;
                                    oldRdsFd.setCargoNotes(oCargoNotes); // A36 - For SIT removal, add ~1 to the old cargo notes
                                    ///} // A33 - Ends
                                }
                            } else if (oMisc2
                                    .contains("S")) {
                                HashMap sitMap = null;
                                String drayStatusSIT = " ";
                                String cmdyId = "";
                                try {
                                    if (tosLookUp == null)
                                        tosLookUp = new TosLookup();
                                    sitMap = tosLookUp.getSITDtls(oContainerNumber + chkDgt);
                                    if (sitMap != null) {
                                        cmdyId = (String) sitMap.get("COMMODITY");
                                        cmdyId = cmdyId == null ? "" : cmdyId;
                                        drayStatusSIT = (String) sitMap.get("DRAYSTATUS");
                                        drayStatusSIT = drayStatusSIT == null ? " " : drayStatusSIT;
                                        logger.info("drayStatusSIT: " + drayStatusSIT + " cmdyId " + cmdyId);
                                    }
                                } catch (Exception ex) {
                                    logger.error("Error in tos look up " + ex);
                                }
                                isDifferent = true;
                                if ("OFFSITE".equals(drayStatusSIT) && ("SIT".equals(cmdyId) || MULTISTOP_SIT.equals(cmdyId))) {
                                    oldRdsFd.setDsc("S");
                                    oldRdsFd.setCommodity(MULTISTOP_SIT);
                                } else if (" ".equalsIgnoreCase(drayStatusSIT)) {
                                    oldRdsFd.setDsc(" ");
                                    oldRdsFd.setCommodity(" ");
                                }
                            }
                            if (!nCrstatus.equalsIgnoreCase(oCrstatus)) {
                                isDifferent = true;
                                logger.info("Diff: Crstatus");
                            }
                            if (!tempNBookingNbr.equalsIgnoreCase(blNbr)) {
                                isDifferent = true;
                                oldRdsFd.setBookingNumber(tempNBookingNbr);
                                logger.info("Diff: Booking nbr");
                            } else {
                                oldRdsFd.setBookingNumber(blNbr);
                            }
                            if (!nPlanDisp.equalsIgnoreCase(oPlanDisp)) {
                                isDifferent = true;
                                oldRdsFd.setPlanDisp(nPlanDisp);
                                logger.info("Diff: Plan disp");
                            }
											/*if (nCargoNotes.indexOf(oCargoNotes) == -1)
											{
												isDifferent = true;

												if (oCargoNotes !=null && nCargoNotes!=null
														&& oCargoNotes.startsWith("WEST OAHU") && !nCargoNotes.startsWith("WEST OAHU")) {
													nCargoNotes = "~2"+nCargoNotes;
												}
												oldRdsFd.setCargoNotes(nCargoNotes);
												logger.info("Diff: cargo notes");
											}*/
                            // A36 -
                            if (oCargoNotes != null && nCargoNotes != null
                                    && oCargoNotes.startsWith("WEST OAHU") && !nCargoNotes.startsWith("WEST OAHU")) {
                                oCargoNotes = "~2" + oCargoNotes;
                                oldRdsFd.setCargoNotes(oCargoNotes); // For WO removal, add ~2 to the old cargo notes
                            }
                        }
                        if (isDifferent) {
                            oldRdsFd.setLastUpdateDate(newRdsFd
                                    .getLastUpdateDate());
                            oldRdsFd.setTriggerDate(newRdsFd.getTriggerDate());
                            //oldRdsFd.setBookingNumber(blNbr);
                            oldRdsFd.setCweight(newRdsFd.getCweight());
                            oldRdsFd.setCneeCode(newRdsFd.getCneeCode());
                            oldRdsFd.setCneePo(newRdsFd.getCneePo());
                            logger.info(oldRdsFd.getShipperArol()
                                    + "<->" + oldRdsFd.getShipperOrgnId()
                                    + "<->" + oldRdsFd.getShipperQualifier());
                            logger.info(newRdsFd.getShipperArol()
                                    + "<->" + newRdsFd.getShipperOrgnId()
                                    + "<->" + newRdsFd.getShipperQualifier());
                            oldRdsFd.setShipperArol(newRdsFd.getShipperArol());
                            if (shipperId != null)
                                oldRdsFd.setShipperOrgnId(shipperId);

                            oldRdsFd.setShipperQualifier(newRdsFd
                                    .getShipperQualifier());
                            if (shipper != null)
                                oldRdsFd.setShipper(shipper);
                            if (pol != null)
                                oldRdsFd.setLoadPort(pol);
                            if (sealNbr1 != null)
                                oldRdsFd.setSealNumber(sealNbr1);
                            if (oCargoNotes == null || oCargoNotes.equals("")) { // A36 - Update cargo notes only if prev notes is empty
                                oldRdsFd.setCargoNotes(nCargoNotes);
                            }
                            if (pod1 != null)
                                oldRdsFd.setDischargePort(pod1);
                            if (strengthCode != null)
                                oldRdsFd.setStrength(strengthCode);
                            if (type != null)
                                oldRdsFd.setTypeCode(type);
                            //
                            String crFlag = "";
                            String onFlag = "";
                            String agFlag = "";
                            String xtFlag = "";
                            String bndFlag = "";
                            String tosHolds = holds; // A36 - Use the holds already retreived from TOS, do not make another call.
                            tosHolds = tosHolds == null ? "" : tosHolds;
											/*
											// get N4 holds for the unit.
											try
											{
												if(tosLookUp==null)
													tosLookUp = new TosLookup();
												tosHolds = tosLookUp.getHolds(oContainerNumber);
												tosHolds = tosHolds==null?"":tosHolds;
												logger.info("Holds lookup : "+oContainerNumber+"-> "+tosHolds);
											}
											catch(Exception e)
											{
												logger.info("Holds lookup ERROR : "+oContainerNumber+" ");
											}
											// end of N4 holds
											*/
                            if (!nCrstatus.equals("") && nCrstatus.length() > 0) {
                                if (nCrstatus.contains("HP"))
                                    crFlag = "H";
                                else if (nCrstatus.contains("CC"))
                                    crFlag = "C";
                                else
                                    crFlag = "V";
                                if (nCrstatus.contains("ON"))
                                    onFlag = "Y";
                                //if (nCrstatus.contains("XT"))
                                //	xtFlag = "Y";

                                if (nCrstatus.contains("BND")
                                        || nCrstatus.contains("INB")
                                        || nCrstatus.contains("PER")) {
                                    bndFlag = "Y";
                                }
                            }
                            logger.info("CURRENT HOLDS  : CRFLAG=" + crFlag + "\tONFLAG=" + onFlag + "\tAGFLAG=" + agFlag + "\tBNDFLAG=" + bndFlag);

                            ArrayList<String> exCrstatus = null;
                            if (tosHolds != null && !tosHolds.equals("") && !tosHolds.equals("null") && tosHolds.length() > 0)
                                exCrstatus = new ArrayList<String>(Arrays.asList(tosHolds.split(",")));
                            else
                                exCrstatus = new ArrayList<String>();
                            logger.info("exCrstatus - " + exCrstatus);
                            if (tosHolds != null && tosHolds.contains("DOC")) {
                                exCrstatus.remove("DOC");
                                writeNotes = writeNotes + " " + "CNC DOC";
                                logger.info("DOC Removed on " + oContainerNumber);
                            }
                            // HP Holds
                            if (crFlag.equals("H") && !tosHolds.contains("HP") && !oMisc2.contains("M")) {
                                exCrstatus.add("HP");
                                writeNotes = writeNotes + " " + "ADD HP";
                                logger.info("HP Added on " + oContainerNumber);
                            } else if (!crFlag.equals("H") && tosHolds.contains("HP") && !oMisc2.contains("M")) {
                                exCrstatus.remove("HP");
                                writeNotes = writeNotes + " " + "CNC HP";
                                logger.info("HP Removed on " + oContainerNumber);
                            }

                            // CC Holds
                            if (crFlag.equals("C") && !tosHolds.contains("CC") && !oMisc2.contains("M")) {
                                exCrstatus.add("CC");
                                writeNotes = writeNotes + " " + "ADD CC";
                                logger.info("CC Added on " + oContainerNumber);
                            } else if (!crFlag.equals("C") && tosHolds.contains("CC") && !oMisc2.contains("M")) {
                                exCrstatus.remove("CC");
                                writeNotes = writeNotes + " " + "CNC CC";
                                logger.info("CC Removed on " + oContainerNumber);
                            }

                            // ON Holds
                            if (onFlag.equals("Y") && !tosHolds.contains("ON") && !oMisc2.contains("O")) {
                                exCrstatus.add("ON");
                                writeNotes = writeNotes + " " + "ADD ON";
                                logger.info("ON Added on " + oContainerNumber);
                            } else if (!onFlag.equals("Y") && tosHolds.contains("ON") && !oMisc2.contains("O")) {
                                exCrstatus.remove("ON");
                                writeNotes = writeNotes + " " + "CNC ON";
                                logger.info("ON Removed on " + oContainerNumber);
                            }

                            //XT Holds

											/*if (xtFlag.equals("Y") && !exCrstatus.contains("XT") && !oMisc2.contains("A"))
											{
												exCrstatus.add("XT");
												writeNotes = writeNotes + " " + "ADD XT";
												logger.info("XT Added on " + oContainerNumber);
											}
											else if (!xtFlag.equals("Y") && exCrstatus.contains("XT") && !oMisc2.contains("A"))
											{
												exCrstatus.remove("XT");
												writeNotes = writeNotes + " " + "CNC XT";
												logger.info("XT Removed on " + oContainerNumber);
											}*/

                            // CUS,RM,INB Holds
                            if (!oLocationRowDeck.equals("") && !oLocationRowDeck.equals("MIDPAC")) {
                                if (!exCrstatus.contains("PER")) {
                                    if (bndFlag.equals("") && !oMisc2.contains("B") && !oMisc2.contains("V")) {
                                        if (exCrstatus.contains("BND")) {
                                            exCrstatus.remove("BND");
                                            writeNotes = writeNotes + " " + "CNC BND";
                                            logger.info("BND Removed on " + oContainerNumber);
                                        }
                                        if (exCrstatus.contains("INB")) {
                                            exCrstatus.remove("INB");
                                            writeNotes = writeNotes + " " + "CNC INB";
                                            logger.info("INB Removed on " + oContainerNumber);
                                        }

                                    }
                                }
                            }
                            logger.info("SUPP COMMENTS: " + writeNotes.trim());
                            // A34
                            logger.info("Adding TOS unit notes + supplemental change notes");
                            String cargoNotes = oldRdsFd.getCargoNotes();
                            cargoNotes = cargoNotes == null ? "" : cargoNotes;
                            if (cargoNotes.contains("~1"))
                                cargoNotes = "~1" + unitNotes + " " + writeNotes.trim();
                            else if (cargoNotes.contains("~2"))
                                cargoNotes = "~2" + unitNotes + " " + writeNotes.trim();
                            else if (nCargoNotes.contains("WEST OAHU") && !unitNotes.contains("WEST OAHU"))
                                cargoNotes = "WEST OAHU " + unitNotes + " " + writeNotes.trim();
                            else if (nCargoNotes.contains("SIT-"))
                                cargoNotes = "SIT-" + nDport + " " + unitNotes + " " + writeNotes.trim();
                            else
                                cargoNotes = unitNotes + " " + writeNotes.trim();
                            cargoNotes = cargoNotes.length() > 255 ? cargoNotes.substring(0, 255).trim() : cargoNotes.trim();
                            oldRdsFd.setCargoNotes(cargoNotes);
                            logger.info("SUPP CARGO NOTES: " + cargoNotes);
                            //
                            if (group != null && "YB".equalsIgnoreCase(group)) { // Retaining YB group and commodity from TOS
                                oldRdsFd.setPlanDisp("7");
                                //oldRdsFd.setCommodity("YB");
                            }
                            //
                            String status = "";
                            if (exCrstatus != null) {
                                for (int e = 0; e < exCrstatus.size(); e++) {
                                    status = status + exCrstatus.get(e)
                                            + " ";
                                }
                            }
                            oldRdsFd.setCrstatus(status.trim());

                            if (!nDs.equals("AUT")) {
                                if (oDs.equals("AUT") && nDs.equals("CY")) {
                                    oldRdsFd.setCommodity("AUTOCY");
                                } else if (oCommodity.equals("AUTO")) {
                                    oldRdsFd.setCommodity("");
                                } else {
                                    oldRdsFd.setCommodity(" ");
                                }
                            } else {
                                oldRdsFd.setCommodity("AUTO");
                            }
                            //
                            oldRdsFd.setLastUpdateUser(newRdsFd.getLastUpdateUser());
                            oldRdsFd.setLastUpdateDate(newRdsFd.getLastUpdateDate());

                            if (oMisc2.contains("S")) {
                                logger.info("DrayStatus has changed in TOS - Get DrayStatus & Group from TOS");
                                // get N4 DRAYSTATUS / XFER-WO / XFER-SI info from TOS
                                try {
                                    if (tosLookUp == null)
                                        tosLookUp = new TosLookup();
                                    HashMap<String, String> resultMap = tosLookUp.getWOSIGrpInfo(oContainerNumber);
                                    if (resultMap != null && resultMap.keySet().size() > 0) {
                                        String drayStatus = resultMap.get("DRAY_STATUS");
                                        drayStatus = drayStatus == null ? "" : drayStatus;
                                        String grp = resultMap.get("GRP");
                                        grp = grp == null ? "" : grp;
                                        if (drayStatus.equals("OFFSITE"))
                                            oldRdsFd.setPlanDisp("W");
                                        else if (drayStatus.equals("RETURN"))
                                            oldRdsFd.setPlanDisp("9");
                                        else if (drayStatus.equals("DRAYIN"))
                                            oldRdsFd.setDsc("C");
                                        else if (drayStatus.equals("TRANSFER"))
                                            oldRdsFd.setPlanDisp("7");
                                        if (grp.equals("XFER-WO")) {
                                            oldRdsFd.setPlanDisp("W");
                                            if (!oldRdsFd.getCargoNotes().contains("WEST OAHU-")) {
                                                oldRdsFd.setCargoNotes("WEST OAHU-" + oldRdsFd.getCargoNotes());
                                            }
                                        } else if (grp.equals("XFER-SI")) {
                                            oldRdsFd.setPlanDisp("W");
                                            oldRdsFd.setCommodity("53" + oldRdsFd.getCommodity());
                                        }
                                        logger.info("oldRdsFd.getCargoNotes() - " + oldRdsFd.getCargoNotes());
                                        logger.info("oldRdsFd.getPlanDisp() - " + oldRdsFd.getPlanDisp());
                                        logger.info("oldRdsFd.getCommodity() - " + oldRdsFd.getCommodity());
                                    } else {
                                        logger.info("DrayStatus & Group are 'null' in TOS for " + oContainerNumber + ", hence sending the same thru supp");
                                        oldRdsFd.setPlanDisp("");
														/*// Since we are suppressing the WO update thru supp, the cargo notes should not contain the WO notes
														String tempStr = oldRdsFd.getCargoNotes();
														if(tempStr!=null){
															tempStr = tempStr.replace("WEST OAHU-", "");
															tempStr = tempStr.replace("WEST OAHU", "");
															oldRdsFd.setCargoNotes(tempStr);
														}*/
														/*//A34
														 * logger.info("Get Unit Remark from TOS");
														if(tosLookUp==null)
															tosLookUp = new TosLookup();
														String remark = tosLookUp.getUnitRemark(oContainerNumber);
														if(remark!=null) {
															remark = remark.length()> 65?remark.substring(0, 65):remark;
															oldRdsFd.setCargoNotes(remark);
														}*/
                                    }
                                } catch (Exception e) {
                                    logger.error("N4 XFER-WO / XFER-SI lookup ERROR : " + oContainerNumber + "\n" + e);
                                }
                                // end of N4 DRAYSTATUS / XFER-WO / XFER-SI info from TOS
                            }
                            updatedRdsFinal.add(oldRdsFd);
                        }
                    }
                    // }
                    // else
                    // {
                    // logger.info("Misc2 Field was already updated in TOS for"
                    // + oContainerNumber + " with " + oMisc2);
                    // }
                }
            }
            if (!containerFound) {
                logger.info("New Supplemental container : "
                        + newRdsFd.getContainerNumber());
                newContainers.add(newRdsFd);
            }
        }
        if (newContainers != null && newContainers.size() > 0) {
            logger.info("NewContainers in supplemental - "
                    + newContainers.size());
            // For new containers calculate Last free day and due dates
            logger.info("DATES FOR SUPP NEW CONTAINERS");
            newContainers = CommonBusinessProcessor
                    .calculateLastFreeDayDueDate(newContainers);
            updatedRdsFinal.addAll(newContainers);
        }

        logger.info("UpdatedRdsFinal supplemental - "
                + updatedRdsFinal.size());
        logger.info("Supplemental validations - End");
        //
        return updatedRdsFinal;
    }

    public static ArrayList<TosRdsDataFinalMt> supplementalValidationsNew(ArrayList<TosRdsDataFinalMt> newSupDataList) {
        ArrayList<TosRdsDataFinalMt> outputSupDataList = new ArrayList<TosRdsDataFinalMt>();
        supProblemsList = new ArrayList<SupplementProblems>();
        for (int n = 0; n < newSupDataList.size(); n++) {
            TosRdsDataFinalMt supRdsFd = newSupDataList.get(n);
            String supContainerNumber = supRdsFd.getContainerNumber();
            String supVesvoy = supRdsFd.getVesvoy();
            String chkDgt = supRdsFd.getCheckDigit();
            chkDgt = chkDgt == null ? "" : chkDgt;
            try {
                if (chkDgt == null || "".equalsIgnoreCase(chkDgt)) {
                    GEMSEquipment gemsEquipment = getEquipmentAttributesFromGems(supContainerNumber);
                    if (gemsEquipment != null) {
                        chkDgt = gemsEquipment.getCheckDigit();
                        logger.info("Check Digit from GEMS :" + chkDgt);
                    } else {
                        chkDgt = "X";
                        logger.info("GEMSEquipment is null -> Assigning check digit as X for " + supContainerNumber);
                    }
                }
            } catch (Exception excep) {
                logger.error("Unable to get check digit from GEMS API for ---->" + supContainerNumber + "hence assiging X");
                chkDgt = "X";
            }

            logger.info("Validation starts for - " + supContainerNumber + "," + supVesvoy);
            boolean isDifferent = false;
            // Get unit details from TOS
            HashMap<String, String> unitDetails = null;
            String freightKind = null;
            String category = null;
            String tosIBDeclaredVV = null;
            String tState = null;
            String carrMode = null;
            String eventDate = null;
            String group = null;
            String obActual = null;
            //
            try {
                if (tosLookUp == null)
                    tosLookUp = new TosLookup();
                unitDetails = tosLookUp.getUnitDetails(supContainerNumber + chkDgt);
                if (unitDetails != null) {
                    freightKind = unitDetails.get("FREIGHT_KIND");
                    category = unitDetails.get("CATEGORY");
                    tosIBDeclaredVV = unitDetails.get("IB_DECLRD");
                    tState = unitDetails.get("TRANSIT_STATE");
                    tState = tState == null ? "" : tState;
                    carrMode = unitDetails.get("CARRIER_MODE");
                    carrMode = carrMode == null ? "" : carrMode;
                    eventDate = unitDetails.get("TIME_MOVE");
                    group = unitDetails.get("GRP");
                    group = group == null ? "" : group;
                    obActual = unitDetails.get("OB_ACTUAL");
                    logger.error("ObActual value: " + obActual);
                    obActual = obActual == null ? "" : obActual;
                    logger.error("ObActual value: " + obActual);
                } else {
                    logger.error("Unable to get TosLookup Unit Details from TOS, hence skipping this unit ---->" + supContainerNumber + "<---- from supplemental validations");
                    continue;
                }
            } catch (Exception e) {
                logger.error("Unable to get TosLookup Unit Details from TOS, hence skipping this unit ---->" + supContainerNumber + "<---- from supplemental validations", e);
                continue;
            }
			/*if(category!=null && category.equalsIgnoreCase("THRGH")) {
				logger.info("Skipping ROB/THRGH unit ---->"+ supContainerNumber + "<---- from supplemental processing.");
				continue;
			}*/

            if (category != null && category.equalsIgnoreCase("EXPRT")) {
                logger.info("Skipping EXPORT unit ---->" + supContainerNumber + "<---- from supplemental processing.");
                continue;
            }

            if (freightKind != null && freightKind.equalsIgnoreCase("MTY")) {
                logger.info("Skipping MTY unit ---->" + supContainerNumber + "<---- from supplemental processing.");
                continue;
            }
            if (tosIBDeclaredVV != null && !tosIBDeclaredVV.equalsIgnoreCase(supVesvoy) && supVesvoy != null) {
                logger.info("Skipping VV_Not_Matched unit ---->" + supContainerNumber + "<---- from supplemental processing.nVesvoy=" + supVesvoy);
                continue;
            }
            //
            logger.info("~~~Constructing RDSFinalTable object from TOS unit details data");
            TosRdsDataFinalMt tosRdsFd = constructRdsDataFinalFromTOS(unitDetails);
            logger.info("~~~constructed object :" + tosRdsFd);
            if (tosRdsFd != null) {
                // Validations
                // Supplemental Data
                String supDs = supRdsFd.getDs();
                supDs = supDs == null ? "" : supDs;
                String supDsc = supRdsFd.getDsc();
                supDsc = supDsc == null ? "" : supDsc;
                String supDir = supRdsFd.getDir();
                supDir = supDir == null ? "" : supDir;
                String supDport = supRdsFd.getDport();
                supDport = supDport == null ? "" : supDport;
                String supDischargePort = supRdsFd.getDischargePort();
                supDischargePort = supDischargePort == null ? "" : supDischargePort;
                String supConsignee = supRdsFd.getConsignee();
                supConsignee = supConsignee == null ? "" : supConsignee;
                String supShipper = supRdsFd.getShipper();
                supShipper = supShipper == null ? "" : supShipper;
                String supCrstatus = supRdsFd.getCrstatus();
                supCrstatus = supCrstatus == null ? "" : supCrstatus;
                String supBkgNbr = supRdsFd.getBookingNumber();
                supBkgNbr = supBkgNbr == null ? "" : supBkgNbr;
                String supPlanDisp = supRdsFd.getPlanDisp();
                supPlanDisp = supPlanDisp == null ? "" : supPlanDisp;
                String supLocStatus = supRdsFd.getLocationStatus();
                supLocStatus = supLocStatus == null ? "" : supLocStatus;
                String supCargoNotes = supRdsFd.getCargoNotes();
                supCargoNotes = supCargoNotes == null ? "" : supCargoNotes;
                String supCommodity = supRdsFd.getCommodity();
                supCommodity = supCommodity == null ? "" : supCommodity;
                String supTypeCode = supRdsFd.getTypeCode();
                supTypeCode = supTypeCode == null ? "" : supTypeCode;
                // TOS Data
                String tosDport = tosRdsFd.getDport();
                tosDport = tosDport == null ? "" : tosDport;
                String tosDischargePort = tosRdsFd.getDischargePort();
                tosDischargePort = tosDischargePort == null ? "" : tosDischargePort;
                String tosDs = tosRdsFd.getDs();
                tosDs = tosDs == null ? "" : tosDs;
                String tosDsc = tosRdsFd.getDsc();
                tosDsc = tosDsc == null ? "" : tosDsc;
                String tosDir = tosRdsFd.getDir();
                tosDir = tosDir == null ? "" : tosDir;
                String tosConsignee = tosRdsFd.getConsignee();
                tosConsignee = tosConsignee == null ? "" : tosConsignee;
                String tosShipper = tosRdsFd.getShipper();
                tosShipper = tosShipper == null ? "" : tosShipper;
                String tosCrstatus = tosRdsFd.getCrstatus();
                tosCrstatus = tosCrstatus == null ? "" : tosCrstatus;
                String tosBkgNbr = tosRdsFd.getBookingNumber();
                tosBkgNbr = tosBkgNbr == null ? "" : tosBkgNbr;
                String tosPlanDisp = tosRdsFd.getPlanDisp();
                tosPlanDisp = tosPlanDisp == null ? "" : tosPlanDisp;
                String tosConsigneeId = tosRdsFd.getConsigneeOrgnId();
                tosConsigneeId = tosConsigneeId == null ? "" : tosConsigneeId;
                String tosCargoNotes = tosRdsFd.getCargoNotes();
                tosCargoNotes = tosCargoNotes == null ? "" : tosCargoNotes;
                String tosLocationRowDeck = tosRdsFd.getLocationRowDeck();
                tosLocationRowDeck = tosLocationRowDeck == null ? "" : tosLocationRowDeck;
                String tosCommodity = tosRdsFd.getCommodity();
                tosCommodity = tosCommodity == null ? "" : tosCommodity;
                String oMisc2 = tosRdsFd.getMisc2();
                oMisc2 = oMisc2 == null ? " " : oMisc2;
                String writeNotes = "";
                if (oMisc2.contains("M") || oMisc2.contains("B") || oMisc2.contains("O") || oMisc2.contains("A") || oMisc2.contains("V") || oMisc2.contains("L") || oMisc2.contains("U") || oMisc2.contains("S") || oMisc2.contains("N") || oMisc2.contains("T") || oMisc2.contains("G")) {
                    isDifferent = true;
                }
                logger.info("supVesvoy   and tosIBDeclaredVV " + supVesvoy + "    " + tosIBDeclaredVV);
                if (supVesvoy.equals(tosIBDeclaredVV) && !supDsc.equals("T") && !supDsc.equals("P") && supDir.equals("IN")) {
                    logger.info("SUP DATA--->" + "DIR=" + supDir + ",DS=" + supDs + ",DSC=" + supDsc + ",DPORT=" + supDport + ",DISCHPORT=" + supDischargePort + ",SHIPPER=" + supShipper + ",CONSIGNEE=" + supConsignee + ",BKG=" + supBkgNbr + ",HOLDS=" + supCrstatus + ",PDISP=" + supPlanDisp + ",CMDY=" + supCommodity);
                    logger.info("TOS DATA--->" + "DIR=" + tosDir + ",DS=" + tosDs + ",DSC=" + tosDsc + ",DPORT=" + tosDport + ",DISCHPORT=" + tosDischargePort + ",SHIPPER=" + tosShipper + ",CONSIGNEE=" + tosConsignee + ",BKG=" + tosBkgNbr + ",HOLDS=" + tosCrstatus + ",PDISP=" + tosPlanDisp + ",CMDY=" + tosCommodity);
                    if (supDport.equalsIgnoreCase(tosDport) && supConsignee.equalsIgnoreCase(tosConsignee) && supShipper.equalsIgnoreCase(tosShipper)
                            && supDs.equalsIgnoreCase(tosDs) && supDsc.equalsIgnoreCase(tosDsc)
                            && supCrstatus.equalsIgnoreCase(tosCrstatus) && supBkgNbr.equalsIgnoreCase(tosBkgNbr) && supPlanDisp.equalsIgnoreCase(tosPlanDisp)
                            && ("".equals(oMisc2) || oMisc2 == null)) {
                        logger.info("Container skippedd from supplemental processing :" + supContainerNumber);
                        continue;
                    } else {
                        logger.info("Container considered for supplemental processing :" + supContainerNumber);
                        // DS VALIDATION
                        if (!supDs.equalsIgnoreCase(tosDs)) {
                            if (tosDs.equals("")) {
                                isDifferent = true;
                                tosRdsFd.setDs(supDs);
                                logger.info("Diff: Ds");
                                writeNotes = writeNotes + " " + tosDs + " to " + supDs;
                            } else if (oMisc2.contains("D")) {
                                continue;
                            } else if (oMisc2.contains("H")) {
                                continue;
                            } else if (tosDport.equals(supDport)) {
                                isDifferent = true;
                                tosRdsFd.setDs(supDs);
                                logger.info("Diff: Ds");
                                writeNotes = writeNotes + " " + tosDs + " to " + supDs;
                            } else if (tosDs.equals("CON")) {
                                isDifferent = true;
                                tosRdsFd.setDs(supDs);
                                logger.info("Diff: Ds");
                                writeNotes = writeNotes + " " + tosDs + " to " + supDs;
                            } else {
                                addSupplementProblems(DISCREP_DS, tosRdsFd, supRdsFd, tState, eventDate, carrMode, group, tosDport, tosConsignee, tosBkgNbr, tosCrstatus, obActual);
                                logger.info("Adding to sup problems : " + supContainerNumber + ", " + DISCREP_DS);
                                continue; // DS Discrepancy, then skip ctr
                            }
                        }
                        // DPORT VALIDATION
                        if (!supDport.equalsIgnoreCase(tosDport) && !oMisc2.contains("P") && !oMisc2.contains("T")) {
                            if (supDs.equals("CY") || (supDs.equals("AUT") && tosDs.equals("AUT"))) {
                                if (tState != null && !tState.equalsIgnoreCase("null") && !tState.equalsIgnoreCase("S40_YARD") && !tState.equalsIgnoreCase("S20_INBOUND")) {
                                    addSupplementProblems(DISCREP_DPORT, tosRdsFd, supRdsFd, tState, eventDate, carrMode, group, tosDport, tosConsignee, tosBkgNbr, tosCrstatus, obActual);
                                    logger.info("Adding to sup problems : " + supContainerNumber + ", " + tState + ", " + carrMode + ", " + DISCREP_DPORT);
                                    continue; // DPORT Discrepancy, then skip ctr
                                }
                            }
                            isDifferent = true;
                            tosRdsFd.setDport(tosDport);
                            if (tState != null && (tState.equalsIgnoreCase("S40_YARD") || tState.equalsIgnoreCase("S20_INBOUND")) && tosDs.equals("AUT")) {
                                tosRdsFd.setDport("MIX");
                                writeNotes = writeNotes + " " + tosDport + " to MIX";
                            } else {
                                tosRdsFd.setDport(supDport);
                                writeNotes = writeNotes + " " + tosDport + " to " + supDport;
                            }
                            tosRdsFd.setDischargePort(supDischargePort);
                            logger.info("Diff: DPORT");
                            if (tosDport.equals("ANK") && tosDs.equals("CY")) {
                                tosRdsFd.setMisc3(null);
                                tosRdsFd.setLocationCategory(null);
                            } else if ("MIX".equalsIgnoreCase(tosDport)) {
                                tosRdsFd.setDport(tosDport);
                                writeNotes = "";
                            }
                            //when DPORT is changed, we should update the trade from supp
                            tosRdsFd.setTrade(supRdsFd.getTrade());
                            tosRdsFd.setHazardousOpenCloseFlag(supRdsFd.getHazardousOpenCloseFlag());
                        } else if (oMisc2.contains("P") || oMisc2.contains("T")) {
                            tosRdsFd.setDport(tosDport);
                            if (CommonBusinessProcessor.isValidNeighborIslandPort(tosDport)) {
                                tosRdsFd.setMisc3(null);
                                tosRdsFd.setLocationCategory(null);
                            }
                        }
                        // CONSIGNEE VALIDATION
                        if ((!"".equalsIgnoreCase(supConsignee) && !supConsignee.equalsIgnoreCase(tosConsignee) && !oMisc2.contains("C") && !oMisc2.contains("N")) && (tosDs.equals("CY") || (tosDs.equals("AUT") && supDs.equals("AUT")))) {
                            if (tState != null && !tState.equalsIgnoreCase("null") && tState.equalsIgnoreCase("S70_DEPARTED") && carrMode != null && !carrMode.equals("null") && carrMode.equalsIgnoreCase("TRUCK")) {
                                addSupplementProblems(DISCREP_CNEE, tosRdsFd, supRdsFd, tState, eventDate, carrMode, group, tosDport, tosConsignee, tosBkgNbr, tosCrstatus, obActual);
                                logger.info("Adding to sup problems : " + supContainerNumber + ", " + tState + ", " + carrMode + ", " + DISCREP_CNEE);
                                continue; // Consignee Discrepancy, then skip ctr
                            }
                            isDifferent = true;
                            tosRdsFd.setConsignee(supConsignee);
                            tosRdsFd.setConsigneeName(supRdsFd.getConsigneeName());
                            tosRdsFd.setCneeCode(supRdsFd.getCneeCode());
                            tosRdsFd.setConsigneeOrgnId(supRdsFd.getConsigneeOrgnId());
                            tosRdsFd.setConsigneeArol(supRdsFd.getConsigneeArol());
                            tosRdsFd.setConsigneeQualifier(supRdsFd.getConsigneeQualifier());
                            writeNotes = writeNotes + " RECON X " + tosConsignee;
                            logger.info("Diff: Consignee");
                            tosRdsFd.setCargoNotes(supCargoNotes); // A36 - Update cargo notes only if there is a consignee change
                        } else if (oMisc2.contains("C") || oMisc2.contains("N")) {
                            tosRdsFd.setConsignee(tosConsignee);
                            tosRdsFd.setCneeCode(tosConsigneeId);
                        } else if ("CON".equals(tosDs) && !supConsignee.equalsIgnoreCase(tosConsignee) && !oMisc2.contains("C") && !oMisc2.contains("N")) {
                            if (tState != null && !tState.equalsIgnoreCase("null") && tState.equalsIgnoreCase("S70_DEPARTED")
                                    && carrMode != null && !carrMode.equals("null") && carrMode.equalsIgnoreCase("TRUCK")) {
                                addSupplementProblems(DISCREP_CNEE, tosRdsFd, supRdsFd, tState, eventDate, carrMode, group, tosDport, tosConsignee, tosBkgNbr, tosCrstatus, obActual);
                                logger.info("Adding to sup problems : " + supContainerNumber + ", " + tState + ", " + carrMode + ", " + DISCREP_CNEE);
                                continue; // Consignee Discrepancy, then skip ctr
                            } else {
                                isDifferent = true;
                                tosRdsFd.setConsignee(supConsignee);
                                tosRdsFd.setConsigneeName(supRdsFd.getConsigneeName());
                                tosRdsFd.setCneeCode(supRdsFd.getCneeCode());
                                tosRdsFd.setConsigneeOrgnId(supRdsFd.getConsigneeOrgnId());
                                tosRdsFd.setConsigneeArol(supRdsFd.getConsigneeArol());
                                tosRdsFd.setConsigneeQualifier(supRdsFd.getConsigneeQualifier());
                                writeNotes = writeNotes + " RECON X " + tosConsignee;
                                logger.info("Diff: Consignee");
                                tosRdsFd.setCargoNotes(supCargoNotes);
                            }
                        }
                        // SHIPPER VALIDATION
                        if (!"".equalsIgnoreCase(supShipper) && !supShipper.equalsIgnoreCase(tosShipper)) {
                            isDifferent = true;
                            logger.info("Diff: Shipper");
                            tosRdsFd.setShipper(supShipper);
                            tosRdsFd.setShipperName(supRdsFd.getShipperName());
                            tosRdsFd.setShipperOrgnId(supRdsFd.getShipperOrgnId());
                            tosRdsFd.setShipperArol(supRdsFd.getShipperArol());
                            tosRdsFd.setShipperQualifier(supRdsFd.getShipperQualifier());
                        }
                        // DSC VALIDATION
                        if (!supDsc.equalsIgnoreCase(tosDsc) && !oMisc2.contains("S")) {
                            isDifferent = true;
                            tosRdsFd.setDsc(supDsc);
                            logger.info("Diff: Dsc");
                            if (tosDsc.equals("S") && !supDsc.equals("S")) { // SIT Removal
                                tosCargoNotes = "~1" + tosCargoNotes;
                                tosRdsFd.setCargoNotes(tosCargoNotes);
                            }
                        } else if (oMisc2.contains("S")) {
                            HashMap sitMap = null;
                            String drayStatusSIT = " ";
                            String cmdyId = "";
                            try {
                                if (tosLookUp == null)
                                    tosLookUp = new TosLookup();
                                sitMap = tosLookUp.getSITDtls(supContainerNumber);
                                if (sitMap != null) {
                                    cmdyId = (String) sitMap.get("COMMODITY");
                                    cmdyId = cmdyId == null ? "" : cmdyId;
                                    drayStatusSIT = (String) sitMap.get("DRAYSTATUS");
                                    drayStatusSIT = drayStatusSIT == null ? " " : drayStatusSIT;
                                    logger.info("drayStatusSIT: " + drayStatusSIT + " cmdyId " + cmdyId);
                                }
                            } catch (Exception ex) {
                                logger.error("Error in tos look up " + ex);
                            }
                            isDifferent = true;
                            if ("OFFSITE".equals(drayStatusSIT) && ("SIT".equals(cmdyId) || MULTISTOP_SIT.equals(cmdyId))) {
                                tosRdsFd.setDsc("S");
                                tosRdsFd.setCommodity(MULTISTOP_SIT);
                            } else if (" ".equalsIgnoreCase(drayStatusSIT)) {
                                tosRdsFd.setDsc(" ");
                                tosRdsFd.setCommodity(" ");
                            } else if ("SAT".equals(cmdyId)) {
                                logger.info("unit is SAT in TOS");
                                tosRdsFd.setCommodity("SAT");
                            }
                        }
                        // CRSTATUS VALIDATION
                        if (!supCrstatus.equalsIgnoreCase(tosCrstatus)) {
                            isDifferent = true;
                            logger.info("Diff: Crstatus");
                        }
                        // BOOKING NUMBER VALIDATION
                        if (!supBkgNbr.equalsIgnoreCase(tosBkgNbr) && !oMisc2.contains("G")) {
                            isDifferent = true;
                            tosRdsFd.setBookingNumber(supBkgNbr);
                            logger.info("Diff: Booking nbr");
                        } else {
                            tosRdsFd.setBookingNumber(tosBkgNbr);
                        }
                        // PLANDISP VALIDATION
                        if (!supPlanDisp.equalsIgnoreCase(tosPlanDisp)) {
                            isDifferent = true;
                            tosRdsFd.setPlanDisp(supPlanDisp);
                            logger.info("Diff: Plan disp");
                        }
                        //
                        if (tosCargoNotes != null && supCargoNotes != null && tosCargoNotes.startsWith("WEST OAHU") && !supCargoNotes.startsWith("WEST OAHU")) {
                            tosCargoNotes = "~2" + tosCargoNotes;
                            tosRdsFd.setCargoNotes(tosCargoNotes); // For WO removal, add ~2 to the cargo notes
                            isDifferent = true;
                        }
                        //
                        if (isDifferent) {
                            tosRdsFd.setLastUpdateUser(supRdsFd.getLastUpdateUser());
                            tosRdsFd.setLastUpdateDate(supRdsFd.getLastUpdateDate());
                            if (tosCargoNotes == null || tosCargoNotes.equals("")) { // A36 - Update cargo notes only if prev notes is empty
                                tosRdsFd.setCargoNotes(supCargoNotes);
                            }
                            String crFlag = "";
                            String onFlag = "";
                            String agFlag = "";
                            String xtFlag = "";
                            String bndFlag = "";
                            String tosHolds = tosCrstatus; // A36 - Use the holds already retreived from TOS, do not make another call.
                            tosHolds = tosHolds == null ? "" : tosHolds;
                            if (!supCrstatus.equals("") && supCrstatus.length() > 0) {
                                if (supCrstatus.contains("HP"))
                                    crFlag = "H";
                                else if (supCrstatus.contains("CC"))
                                    crFlag = "C";
                                else
                                    crFlag = "V";
                                if (supCrstatus.contains("ON"))
                                    onFlag = "Y";
                                if (getPropertyValueAsBoolean("XT_HOLD_ENABLED")) {
                                    if (supCrstatus.contains("XT"))
                                        xtFlag = "Y";
                                }
                                if (supCrstatus.contains("BND")
                                        || supCrstatus.contains("INB")
                                        || supCrstatus.contains("PER")) {
                                    bndFlag = "Y";
                                }
                            }
                            logger.info("CURRENT HOLDS  : CRFLAG=" + crFlag + "\tONFLAG=" + onFlag + "\tAGFLAG=" + agFlag + "\tBNDFLAG=" + bndFlag);
                            ArrayList<String> exCrstatus = null;
                            if (tosHolds != null && !tosHolds.equals("") && !tosHolds.equals("null") && tosHolds.length() > 0)
                                exCrstatus = new ArrayList<String>(Arrays.asList(tosHolds.split(",")));
                            else
                                exCrstatus = new ArrayList<String>();
                            logger.info("exCrstatus - " + exCrstatus);
                            if (tosHolds != null && tosHolds.contains("DOC")) {
                                exCrstatus.remove("DOC");
                                writeNotes = writeNotes + " " + "CNC DOC";
                                logger.info("DOC Removed on " + supContainerNumber);
                            }
                            // HP Holds
                            if (crFlag.equals("H") && !tosHolds.contains("HP") && !oMisc2.contains("M")) {
                                exCrstatus.add("HP");
                                writeNotes = writeNotes + " " + "ADD HP";
                                logger.info("HP Added on " + supContainerNumber);
                            } else if (!crFlag.equals("H") && tosHolds.contains("HP") && !oMisc2.contains("M")) {
                                exCrstatus.remove("HP");
                                writeNotes = writeNotes + " " + "CNC HP";
                                logger.info("HP Removed on " + supContainerNumber);
                            }

                            // CC Holds
                            if (crFlag.equals("C") && !tosHolds.contains("CC") && !oMisc2.contains("M")) {
                                exCrstatus.add("CC");
                                writeNotes = writeNotes + " " + "ADD CC";
                                logger.info("CC Added on " + supContainerNumber);
                            } else if (!crFlag.equals("C") && tosHolds.contains("CC") && !oMisc2.contains("M")) {
                                exCrstatus.remove("CC");
                                writeNotes = writeNotes + " " + "CNC CC";
                                logger.info("CC Removed on " + supContainerNumber);
                            }

                            // ON Holds
                            if (onFlag.equals("Y") && !tosHolds.contains("ON") && !oMisc2.contains("O")) {
                                exCrstatus.add("ON");
                                writeNotes = writeNotes + " " + "ADD ON";
                                logger.info("ON Added on " + supContainerNumber);
                            } else if (!onFlag.equals("Y") && tosHolds.contains("ON") && !oMisc2.contains("O")) {
                                exCrstatus.remove("ON");
                                writeNotes = writeNotes + " " + "CNC ON";
                                logger.info("ON Removed on " + supContainerNumber);
                            }

                            //XT Holds
                            if (getPropertyValueAsBoolean("XT_HOLD_ENABLED")) {
                                if (xtFlag.equals("Y") && !exCrstatus.contains("XT") && !oMisc2.contains("A")) {
                                    exCrstatus.add("XT");
                                    writeNotes = writeNotes + " " + "ADD XT";
                                    logger.info("XT Added on " + supContainerNumber);
                                } else if (!xtFlag.equals("Y") && exCrstatus.contains("XT") && !oMisc2.contains("A")) {
                                    exCrstatus.remove("XT");
                                    writeNotes = writeNotes + " " + "CNC XT";
                                    logger.info("XT Removed on " + supContainerNumber);
                                }
                            }

                            // CUS,RM,INB Holds
                            if (!tosLocationRowDeck.equals("") && !tosLocationRowDeck.equals("MIDPAC")) {
                                if (!exCrstatus.contains("PER")) {
                                    if (bndFlag.equals("") && !oMisc2.contains("B") && !oMisc2.contains("V")) {
                                        if (exCrstatus.contains("BND")) {
                                            exCrstatus.remove("BND");
                                            writeNotes = writeNotes + " " + "CNC BND";
                                            logger.info("BND Removed on " + supContainerNumber);
                                        }
                                        if (exCrstatus.contains("INB")) {
                                            exCrstatus.remove("INB");
                                            writeNotes = writeNotes + " " + "CNC INB";
                                            logger.info("INB Removed on " + supContainerNumber);
                                        }

                                    }
                                }
                            }
                            logger.info("SUPP COMMENTS: " + writeNotes.trim());
                            logger.info("Adding TOS unit notes + supplemental change notes");
                            String cargoNotes = tosRdsFd.getCargoNotes();
                            cargoNotes = cargoNotes == null ? "" : cargoNotes;
							/*if(cargoNotes.contains("~1"))
								cargoNotes = "~1" + tosCargoNotes + " "+ writeNotes.trim();
							else if(cargoNotes.contains("~2"))
								cargoNotes = "~2" + tosCargoNotes + " " +writeNotes.trim();
							else */
                            if (supCargoNotes.contains("WEST OAHU") && !tosCargoNotes.contains("WEST OAHU"))
                                cargoNotes = "WEST OAHU " + tosCargoNotes + " " + writeNotes.trim();
                            else if (supCargoNotes.contains("SIT-"))
                                cargoNotes = "SIT-" + supDport + " " + tosCargoNotes + " " + writeNotes.trim();
                            else
                                cargoNotes = tosCargoNotes + " " + writeNotes.trim();
                            cargoNotes = cargoNotes.length() > 255 ? cargoNotes.substring(0, 255).trim() : cargoNotes.trim();
                            tosRdsFd.setCargoNotes(cargoNotes);
                            logger.info("SUPP CARGO NOTES: " + cargoNotes);
                            //
							/*if(group!=null && "YB".equalsIgnoreCase(group)) { // Retaining YB group and commodity from TOS
								tosRdsFd.setPlanDisp("7");
								tosRdsFd.setCommodity("YB");
							}*/
                            //
                            String status = "";
                            if (exCrstatus != null) {
                                for (int e = 0; e < exCrstatus.size(); e++) {
                                    status = status + exCrstatus.get(e)
                                            + " ";
                                }
                            }
                            tosRdsFd.setCrstatus(status.trim());

                            // Sup commodity changes when CY to AUTO, CY to AUTOCON, AUTO to CY, and AUTO to AUTOCON
                            // Deviation from CMIS is CON to CY
                            if (!supDs.equalsIgnoreCase("AUT")) {
                                if ((tosDs.equalsIgnoreCase("AUT") || tosDs.equalsIgnoreCase("CON")) && supDs.equalsIgnoreCase("CY")) {
                                    tosRdsFd.setCommodity("AUTOCY");
                                } else if (supDs.equalsIgnoreCase("CON")) {
                                    if (supTypeCode.startsWith("V")) {
                                        tosRdsFd.setDs("CY");
                                        tosRdsFd.setCommodity("AUTOCY");
                                    } else if (supTypeCode.startsWith("F")) {
                                        tosRdsFd.setCommodity("AUTOCON");
                                    } else {
                                        tosRdsFd.setDs("CY");
                                        tosRdsFd.setCommodity("AUTOCY");
                                    }
                                } else if (supCommodity.equalsIgnoreCase("AUTO")) {
                                    tosRdsFd.setCommodity("");
                                }
                            } else {
                                tosRdsFd.setCommodity("AUTO");
                            }
                            logger.debug("CMDY PRINT 1 for " + supContainerNumber);
                            logger.debug(" - ORGN CMDY = " + supCommodity);
                            logger.debug(" - VALIDATED CMDY = " + tosRdsFd.getCommodity());
                            //
                            //
                            if (oMisc2.contains("S")) {
                                logger.info("DrayStatus has changed in TOS - Get DrayStatus & Group from TOS");
                                // get N4 DRAYSTATUS / XFER-WO / XFER-SI info from TOS
                                try {
                                    if (tosLookUp == null)
                                        tosLookUp = new TosLookup();
                                    HashMap<String, String> resultMap = tosLookUp.getWOSIGrpInfo(supContainerNumber);
                                    if (resultMap != null && resultMap.keySet().size() > 0) {
                                        String drayStatus = resultMap.get("DRAY_STATUS");
                                        drayStatus = drayStatus == null ? "" : drayStatus;
                                        String grp = resultMap.get("GRP");
                                        grp = grp == null ? "" : grp;
                                        if (drayStatus.equals("OFFSITE"))
                                            tosRdsFd.setPlanDisp("W");
                                        else if (drayStatus.equals("RETURN"))
                                            tosRdsFd.setPlanDisp("9");
                                        else if (drayStatus.equals("DRAYIN"))
                                            tosRdsFd.setDsc("C");
                                        else if (drayStatus.equals("TRANSFER"))
                                            tosRdsFd.setPlanDisp("7");
                                        if (grp.equals("XFER-WO")) {
                                            tosRdsFd.setPlanDisp("W");
                                            if (!tosRdsFd.getCargoNotes().contains("WEST OAHU-")) {
                                                tosRdsFd.setCargoNotes("WEST OAHU-" + tosRdsFd.getCargoNotes());
                                            }
                                        } else if (grp.equals("XFER-SI")) {
                                            tosRdsFd.setPlanDisp("W");
                                            tosRdsFd.setCommodity("53" + tosRdsFd.getCommodity());
                                        }
                                        logger.info("tosRdsFd.getCargoNotes() - " + tosRdsFd.getCargoNotes());
                                        logger.info("tosRdsFd.getPlanDisp() - " + tosRdsFd.getPlanDisp());
                                        logger.info("tosRdsFd.getCommodity() - " + tosRdsFd.getCommodity());
                                    } else {
                                        logger.info("DrayStatus & Group are 'null' in TOS for " + supContainerNumber + ", hence sending the same thru supp");
                                        tosRdsFd.setPlanDisp("");
                                    }
                                } catch (Exception e) {
                                    logger.error("N4 XFER-WO / XFER-SI lookup ERROR : " + supContainerNumber + "\n" + e);
                                }
                                // end of N4 DRAYSTATUS / XFER-WO / XFER-SI info from TOS
                            }
                            if (group != null && "YB".equalsIgnoreCase(group)) { // Retaining YB group and commodity from TOS
                                tosRdsFd.setPlanDisp("7");
                                //tosRdsFd.setCommodity("YB");
                            }
                            //
                            tosRdsFd.setSrv(supRdsFd.getSrv());
                            tosRdsFd.setOwner(supRdsFd.getOwner());
                            tosRdsFd.setTareWeight(supRdsFd.getTareWeight());//Got from TOS to sup object and to tos object
                            tosRdsFd.setHgt(supRdsFd.getHgt());//Got from TOS to sup object and to tos object
                            tosRdsFd.setTypeCode(supRdsFd.getTypeCode()); // This type code is constructed from TOS type code henec passing is back with the update.

                            //Issues with DOC hold, multistop- start

                            String multiStop = supRdsFd.getMultiStop();
                            logger.info("Supplemetal multistop Value" + multiStop);
                            multiStop = multiStop == null ? "" : multiStop;

                            if ("".equalsIgnoreCase(supRdsFd.getMultiStop())) {
                                if (tosRdsFd.getCommodity() != null && tosRdsFd.getCommodity().equalsIgnoreCase("MULTISTOP")) {
                                    tosRdsFd.setCommodity(" ");
                                } else {
                                    tosRdsFd.setCommodity(supCommodity);
                                }

                                logger.info("Supplemetal multistop Value is empty then" + supCommodity);

                            }
                            logger.info("Supplemetal multistop Value get commodity" + tosRdsFd.getCommodity());
                            tosRdsFd.setMultiStop(supRdsFd.getMultiStop());

                            logger.info("Setting multistop value" + tosRdsFd.getMultiStop());
                            logger.info("Commodity set to " + tosRdsFd.getCommodity());
                            //Issues with DOC hold, multistop- end
                            // Add the updated TOS RDSFD to the list of updated container records.
                            outputSupDataList.add(tosRdsFd);
                            //
                        }
                    }
                }
            }
        }

        return outputSupDataList;
    }

    public static TosRdsDataFinalMt constructRdsDataFinalFromTOS(HashMap<String, String> unitDetails) {
        TosRdsDataFinalMt tosRdsFd = null;
        if (unitDetails != null) {
            tosRdsFd = new TosRdsDataFinalMt();
            //
            String freightKind = unitDetails.get("FREIGHT_KIND");
            freightKind = freightKind == null ? "" : freightKind;
            String category = unitDetails.get("CATEGORY");
            category = category == null ? "" : category;
            String unitId = unitDetails.get("UNIT_ID");
            String group = unitDetails.get("GRP");
            group = group == null ? "" : group;
            String drayStatus = unitDetails.get("DRAY_STATUS");
            drayStatus = drayStatus == null ? "" : drayStatus;
            String tState = unitDetails.get("TRANSIT_STATE");
            tState = tState == null ? "" : tState;
            String locType = unitDetails.get("LAST_POS_LOCTYPE");
            locType = locType == null ? "" : locType;
            String vesClassType = unitDetails.get("VES_CLASS_TYPE");
            vesClassType = vesClassType == null ? "" : vesClassType;
            String requiresPower = unitDetails.get("REQUIRES_POWER");
            requiresPower = requiresPower == null ? "" : requiresPower;
            //
            String vesvoy = unitDetails.get("IB_DECLRD");
            vesvoy = vesvoy == null ? "" : vesvoy;
            tosRdsFd.setVesvoy(vesvoy.length() > 6 ? vesvoy.substring(0, 6) : vesvoy);
            tosRdsFd.setContainerNumber(unitId.substring(0, unitId.length() - 1));
            tosRdsFd.setCheckDigit(unitId.substring(unitId.length() - 1));
            String bkgNbr = unitDetails.get("BL_NBR");
            bkgNbr = bkgNbr == null ? "" : bkgNbr;
            tosRdsFd.setBookingNumber(bkgNbr);
            tosRdsFd.setCneeCode(unitDetails.get("CONSIGNEE_ID"));
            tosRdsFd.setConsigneeOrgnId(unitDetails.get("CONSIGNEE_ID"));
            tosRdsFd.setConsignee(unitDetails.get("CONSIGNEE"));
            tosRdsFd.setConsigneeName(unitDetails.get("CONSIGNEE"));
            tosRdsFd.setShipperOrgnId(unitDetails.get("SHIPPER_ID"));
            tosRdsFd.setShipper(unitDetails.get("SHIPPER"));
            tosRdsFd.setShipperName(unitDetails.get("SHIPPER"));
            tosRdsFd.setSealNumber(unitDetails.get("SEAL_NBR1"));
            String commodity = unitDetails.get("COMM_ID");
            commodity = commodity == null ? "" : commodity;
            tosRdsFd.setCommodity(commodity);
            tosRdsFd.setDischargePort(unitDetails.get("POD1"));
            tosRdsFd.setLoadPort(unitDetails.get("POL"));
            tosRdsFd.setDport(unitDetails.get("DESTINATION"));
            tosRdsFd.setTypeCode(unitDetails.get("EQUIP_TYPE"));
            tosRdsFd.setCrstatus(unitDetails.get("HOLDS"));
            tosRdsFd.setCargoNotes(unitDetails.get("REMARK"));
            tosRdsFd.setTruck(unitDetails.get("TRUCK"));
            tosRdsFd.setMisc2(unitDetails.get("MISC2"));
            tosRdsFd.setStrength(unitDetails.get("STRENGTH_CODE"));
            tosRdsFd.setLocationRowDeck(unitDetails.get("LINEOP"));
            if (requiresPower.equals("1")) {
                String tempString = unitDetails.get("TEMP_REQD_C");
                try {
                    if (tempString != null && !tempString.isEmpty()) {
                        Double temp = Double.parseDouble(tempString) * 1.8 + 32;
                        tosRdsFd.setTemp(String.valueOf(Math.round(temp)));
                        tosRdsFd.setTempMeasurementUnit("F");
                    }
                } catch (Exception e) {
                    logger.error("Error occured while updating the temp/temp.setting : " + tempString);
                }
            }
            String slot = unitDetails.get("LAST_POS_SLOT");
            slot = slot == null ? "" : slot;
            tosRdsFd.setCell(slot);
            tosRdsFd.setCsrId(unitDetails.get("CSRID"));
            tosRdsFd.setCneePo(unitDetails.get("CNEEPO"));
            tosRdsFd.setMilTcn(unitDetails.get("MILTCN"));
            String cWeight = unitDetails.get("CWEIGHT_KG");
            if (cWeight != null && cWeight.length() > 0) {
                Double cWtdouble = new Double(cWeight);
                String cWtstring = CommonBusinessProcessor.convertKGToPounds(cWtdouble);
                tosRdsFd.setCweight(new BigDecimal(cWtstring));
            }
            String availDateStr = unitDetails.get("AVAIL_DATE");
            if (availDateStr != null && availDateStr.length() > 0) {
                tosRdsFd.setArrdate(CalendarUtil.convertStrgToDateFormat(availDateStr));
            }
            String dueDateStr = unitDetails.get("DUE_DATE");
            if (dueDateStr != null && dueDateStr.length() > 0) {
                tosRdsFd.setMisc3(dueDateStr);
            }
            String lastFreeDayStr = unitDetails.get("LAST_FREE_DAY");
            if (lastFreeDayStr != null && lastFreeDayStr.length() > 0) {
                tosRdsFd.setLocationCategory(lastFreeDayStr);
            }
            //
            // Construct DS
            if (commodity.equalsIgnoreCase("AUTOCON"))
                tosRdsFd.setDs("CON");
            else if (commodity.equalsIgnoreCase("AUTO"))
                tosRdsFd.setDs("AUT");
            else if ((freightKind.equalsIgnoreCase("FCL") || commodity.equalsIgnoreCase("AUTOCY")) && !category.equalsIgnoreCase("STRGE"))
                tosRdsFd.setDs("CY");
            else
                tosRdsFd.setDs(null);
            // Construct DSC
            if (group.equalsIgnoreCase("TS"))
                tosRdsFd.setDsc("C");
            else if ((group.equalsIgnoreCase("XFER-P2") || group.equalsIgnoreCase("XFER-SI") || group.equalsIgnoreCase("XFER-WO")) && freightKind.equalsIgnoreCase("MTY"))
                tosRdsFd.setDsc(null);
            else if ((group.equalsIgnoreCase("XFER-P2") || group.equalsIgnoreCase("XFER-SI") || group.equalsIgnoreCase("XFER-WO")) && !freightKind.equalsIgnoreCase("MTY"))
                tosRdsFd.setDsc("C");
            else if (drayStatus.equalsIgnoreCase("OFFSITE") && (commodity.equalsIgnoreCase("SIT") || commodity.equalsIgnoreCase(MULTISTOP_SIT)))
                tosRdsFd.setDsc("S");
            else if (drayStatus.equalsIgnoreCase("OFFSITE") || drayStatus.equalsIgnoreCase("DRAYIN"))
                tosRdsFd.setDsc("C");
            else if (commodity.equalsIgnoreCase("SIT") || commodity.equalsIgnoreCase(MULTISTOP_SIT))
                tosRdsFd.setDsc("S");
            else
                tosRdsFd.setDsc(null);
            // Construct PLANDISP
            if (group.equalsIgnoreCase("TS"))
                tosRdsFd.setPlanDisp("T");
            else if (group.equalsIgnoreCase("XFER-P2") || group.equalsIgnoreCase("XFER-WO"))
                tosRdsFd.setPlanDisp(group.equalsIgnoreCase("XFER-P2") ? "3" : "W");
            else if (group.equalsIgnoreCase("XFER-SI"))
                tosRdsFd.setPlanDisp(slot.startsWith("WOA") ? "W" : "3");
            else if (group.equalsIgnoreCase("COMSVC"))
                tosRdsFd.setPlanDisp("3");
            else if ((group.equalsIgnoreCase("OTR") || group.equalsIgnoreCase("passpass")) && drayStatus.equalsIgnoreCase("forward to loading"))
                tosRdsFd.setPlanDisp("8");
            else if (group.equalsIgnoreCase("YB"))
                tosRdsFd.setPlanDisp("7");
            else if (group.equalsIgnoreCase("1WAY"))
                tosRdsFd.setPlanDisp("9");
            else if ((group.equalsIgnoreCase("SHOW") || group.equalsIgnoreCase("passpass")) && drayStatus.equalsIgnoreCase("Return to Shipper"))
                tosRdsFd.setPlanDisp("A");
            else if (group.equalsIgnoreCase("MDA"))
                tosRdsFd.setPlanDisp("B");
            // Construct ORIENTATION
            if (freightKind.equalsIgnoreCase("MTY"))
                tosRdsFd.setOrientation("E");
            else
                tosRdsFd.setOrientation("F");
            // Construct DIR
            if (category.equalsIgnoreCase("EXPRT") && (bkgNbr.length() > 0 || !freightKind.equalsIgnoreCase("MTY")))
                tosRdsFd.setDir("OUT");
            else if (category.equalsIgnoreCase("IMPRT") && tState.equalsIgnoreCase("S60_LOADED") && freightKind.equalsIgnoreCase("MTY") && bkgNbr.length() == 0)
                tosRdsFd.setDir("DIR");
            else if (category.equalsIgnoreCase("IMPRT"))
                tosRdsFd.setDir("IN");
            else if (category.equalsIgnoreCase("TRSHP") && tState.equalsIgnoreCase("S20_INBOUND"))
                tosRdsFd.setDir("IN");
            else if (category.equalsIgnoreCase("TRSHP") && !tState.equalsIgnoreCase("S20_INBOUND"))
                tosRdsFd.setDir("OUT");
            else if (category.equalsIgnoreCase("THRGH") && !freightKind.equalsIgnoreCase("MTY"))
                tosRdsFd.setDir("OUT");
            // Construct LOCATION STATUS
            if (locType.equalsIgnoreCase("TRUCK"))
                tosRdsFd.setLocationStatus("3");
            else if (locType.equalsIgnoreCase("YARD"))
                tosRdsFd.setLocationStatus("1");
            else if (locType.equalsIgnoreCase("VESSEL")) {
                if (vesClassType.equalsIgnoreCase("BARGE"))
                    tosRdsFd.setLocationStatus("7");
                else if (vesClassType.equalsIgnoreCase("CELL")) {
                    if (category.equalsIgnoreCase("THRGH"))
                        tosRdsFd.setLocationStatus("2");
                    else if (tState.equalsIgnoreCase("S60_LOADED") || tState.equalsIgnoreCase("S70_DEPARTED"))
                        tosRdsFd.setLocationStatus("2");
                    else if (tState.equalsIgnoreCase("S20_INBOUND"))
                        tosRdsFd.setLocationStatus("4");
                }
            }
            // Construct TRADE/HAZARDOUS_OPEN_CLOSE_FLAG
            String loadPortTrade = null;
            String destPortTrade = null;
            String trade = null;
            if (tosRdsFd.getDport() != null)
                destPortTrade = getTradeforPort(tosRdsFd.getDport());
            if (destPortTrade == null)
                trade = "A";
            else
                trade = destPortTrade;
            if (tosRdsFd.getLoadPort() != null)
                loadPortTrade = getTradeforPort(tosRdsFd.getLoadPort());
            loadPortTrade = loadPortTrade == null ? "" : loadPortTrade;
            if ("M".equals(loadPortTrade) || "F".equals(loadPortTrade) || "G".equals(loadPortTrade)) {
                trade = loadPortTrade;
            }
            tosRdsFd.setTrade(trade);
            tosRdsFd.setHazardousOpenCloseFlag(trade);

            //based on defect D31975 commenting the code
            // Construct STOW RESTRICTION CODE
            //String specialStow = unitDetails.get("STOW_RESTRICTION_CODE");
            //specialStow = specialStow==null?"":specialStow;
            //if(specialStow.length()==1)
            //	tosRdsFd.setStowRestrictionCode(specialStow);
            //else if(specialStow.equalsIgnoreCase("INSP"))
            //	tosRdsFd.setStowRestrictionCode("3");
            //else if(specialStow.equalsIgnoreCase("SHOP"))
            //	tosRdsFd.setStowRestrictionCode("W");
            //else if(specialStow.equalsIgnoreCase("CL"))
            //	tosRdsFd.setStowRestrictionCode("C");
            //
        }
        return tosRdsFd;
    }

    public static GEMSEquipment getEquipmentAttributesFromGems(
            String equipmentNumber) {
        logger.info("getEquipmentAttributesFromGems begin :" + equipmentNumber);
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
                "/gems-app-context.xml");
        GEMSInterface gemsInterface = (GEMSInterface) ctx
                .getBean("gemsInterface");
        String[] equipmentList = new String[1];
        GEMSEquipment gemsEquipment = null;
        try {
            equipmentList[0] = equipmentNumber;
            GEMSResults result = gemsInterface
                    .getEquipmentAttributes(equipmentList);
            if (result != null && result.getResults().size() > 0) {
                logger.info("result size is :" + result.getResults().size());
                for (int k = 0; k < result.getResults().size(); k++) {
                    gemsEquipment = (GEMSEquipment) result.getResults().get(k);
                    if (gemsEquipment == null) {
                        logger.info("No equipment record from GEMS");
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("getEquipmentAttributesFromGems exception :" + ex);
            ex.printStackTrace();
        }
        logger.info("getEquipmentAttributesFromGems end");
        return gemsEquipment;
    }

    public static String convertKGToPounds(double kg) {
        kg = kg * 2.20462262185;
        String newPound = new DecimalFormat("##.########").format(kg)
                .toString();
        return newPound;
    }

    public static String constructHeightMm(String nomHeight) {
        String heightMm = "000000";
        if (!nomHeight.equalsIgnoreCase("NA")) {
            nomHeight = nomHeight.replaceAll("NOM", "");
            if (nomHeight.length() > 0) {
                String feet = nomHeight.substring(0, nomHeight.length() - 1);
                String inches = nomHeight.substring(nomHeight.length() - 1,
                        nomHeight.length());
                if (feet.length() == 1)
                    feet = "0" + feet;
                inches = "0" + inches + "00";
                heightMm = feet + inches;
            }
        }
        return heightMm;
    }

    public static String constructTypeCode(String type, String heightMm,
                                           String material) {
        String typeCode = "";
        typeCode = type.substring(0, 3);
        typeCode = typeCode + " ";
        String feet = heightMm.substring(0, 2);
        if (feet.startsWith("0"))
            feet = "" + Integer.parseInt(feet);
        else
            feet = feet.substring(0, 1);
        String inches = heightMm.substring(2, 4);
        inches = "" + Integer.parseInt(inches);
        typeCode = typeCode + feet + inches;
        if (material.equals("STEEL") || material.equals("UNKNOWN"))
            typeCode = typeCode + "ST";
        else if (material.equals("ALUMINUM"))
            typeCode = typeCode + "AL";
        return typeCode;
    }

    public static void cleanUp() {
        logger.info("CommonBusiness cleanUp start");
        if (tosLookUp != null) {
            tosLookUp.close();
            tosLookUp = null;
        }
        logger.info("CommonBusiness cleanUp end");
    }

    public static void updateShedulerProp(HashMap shedulerMap) {
        logger.info("CommonBusinessProcessor.updateShedulerProp begin");
        try {

            if (shedulerMap != null) {
                logger.info("CommonBusinessProcessor shedulerMap : "
                        + shedulerMap);
                NewVesselDao.updateTosAppParameter("IS_STOWPLAN_JOB_ON",
                        shedulerMap.get("IS_STOWPLAN_JOB_ON").toString());
                NewVesselDao.updateTosAppParameter("IS_DCM_JOB_ON", shedulerMap
                        .get("IS_DCM_JOB_ON").toString());
                NewVesselDao.updateTosAppParameter("IS_RDS_JOB_ON", shedulerMap
                        .get("IS_RDS_JOB_ON").toString());
            }
        } catch (Exception ex) {
            logger.error("CommonBusinessProcessor.updateShedulerProp exception: "
                    + ex);
        }
        logger.info("CommonBusinessProcessor.updateShedulerProp end");
    }

    public static String getNextOutboundVesvoyfromNItoHon(String vesselCode,
                                                          String loadPort, Date inputDate) {
        logger.info("getNextOutboundVesvoyfromNItoHon begin :" + vesselCode
                + "-" + inputDate);
        String outputVesvoy = null;
        HashMap vesselMap = null;
        String inputDateStr = null;
        try {
            inputDateStr = CalendarUtil.convertDateToStringMMDDYYY(inputDate);
            logger.info("inputDateStr :" + inputDateStr);
            // need to delete
			/*
			 * Calendar cal = Calendar.getInstance();
			 * logger.info("Cal is "+cal); cal.setTime(new Date());
			 * //cal.add(Calendar.DATE,+5); cal.add(Calendar.DATE,-31); Date
			 * availableDate = cal.getTime(); logger.info(
			 * "availableDate in getNextOutboundVesvoyfromNItoHon "
			 * +availableDate);
			 */

            if (outboundNIVesselMap != null && outboundNIVesselMap.size() > 0) {
                outputVesvoy = (String) outboundNIVesselMap.get(vesselCode
                        + loadPort + inputDateStr);
            } else {
                outboundNIVesselMap = new HashMap();
            }
            logger.info("outboundNIVesselMap  :" + outboundNIVesselMap);
            if (outputVesvoy == null || "".equals(outputVesvoy)) {
                if (inputDate != null) {
                    VesselScheduleLookup vesselLookUp = new VesselScheduleLookup();
                    vesselMap = vesselLookUp.getNextOutboundVoyageForBarge(
                            vesselCode, loadPort, inputDate);
                }
                if (vesselMap != null)
                    outputVesvoy = (String) vesselMap.get(loadPort);

                outboundNIVesselMap.put(vesselCode + loadPort + inputDateStr,
                        outputVesvoy);
                System.out
                        .println("getNextOutboundVesvoyfromNItoHon.outputVesvoy :"
                                + outputVesvoy);
            }
        } catch (Exception ex) {
            logger.error("getNextOutboundVesvoyfromNItoHon:exception is " + ex);
        }
        logger.info("getNextOutboundVesvoyfromNItoHon end");
        return outputVesvoy;
    }

    public static String getOutboundVesvoyForExportCntrs(String dischargePort,
                                                         String obVesvoy1) {
        logger.info("getOutboundVesvoyForExportCntrs begin");
        String outVesvoyForExport = null;
        try {
            if (outboundExportVesselMap != null
                    && outboundExportVesselMap.size() > 0) {
                outVesvoyForExport = (String) outboundNIVesselMap.get(obVesvoy1
                        + dischargePort);
            } else {
                outboundExportVesselMap = new HashMap();
            }
            if (outVesvoyForExport == null || "".equals(outVesvoyForExport)) {
                Date availableDate1 = getArrivalDateByVVD(obVesvoy1, "ANK");
                System.out
                        .println("getOutboundVesvoyForExportCntrs availableDate1 "
                                + availableDate1);
                outVesvoyForExport = getNextOutboundVesselForPort(
                        dischargePort, availableDate1);
            }
        } catch (Exception ex) {
            logger.error("Error:getOutboundVesvoyForExportCntr :" + ex);
        }
        logger.info("getOutboundVesvoyForExportCntrs end");
        return outVesvoyForExport;
    }


    public static ArrayList<NLTErrors> extractBargeNLTDescripancies(
            List<?> containersList, String Vesvoy) {
        ArrayList<NLTErrors> errorsList = new ArrayList<NLTErrors>();
        VesselVisitVO deptVesvoy = null;
        if (containersList == null)
            return errorsList;
        List mtyCtrList = NewReportVesselDao.getInvalidCellList(Vesvoy);
        for (int i = 0; i < containersList.size(); i++) {
            boolean isErrorContainer = false;
            ArrayList<String> errors = new ArrayList<String>();
            TosStowPlanCntrMt tosStowPlan = (TosStowPlanCntrMt) containersList
                    .get(i);
            logger.info("tosStowPlan***"
                    + tosStowPlan.getContainerNumber() + " "
                    + tosStowPlan.getCommodity() + " " + tosStowPlan.getDport()
                    + " " + tosStowPlan.getDischargePort() + " "
                    + tosStowPlan.getDs());
            String vesvoy = "";
            if (tosStowPlan.getActualVessel() != null
                    && tosStowPlan.getActualVoyage() != null)
                vesvoy = tosStowPlan.getActualVessel()
                        + tosStowPlan.getActualVoyage();
            vesvoy = vesvoy == null ? "" : vesvoy;

            if (vesvoy != null && !"".equals(vesvoy)) {
                logger.info(" vesvoy for vesvoy error " + vesvoy);
                try {
                    if (tosLookUp == null) {
                        tosLookUp = new TosLookup();
                    }
                    deptVesvoy = tosLookUp.chkForDepartedOutboundVesvoy(vesvoy);

                    if (deptVesvoy != null) {
                        isErrorContainer = true;
                        errors.add("CarrierOB departed");
                    }
                } catch (Exception e) {
                    logger.error("exception is " + e);
                }

            }
            String dir = tosStowPlan.getDir();
            dir = dir == null ? "" : dir;
            String emptyFull = tosStowPlan.getErf(); // For A41
            emptyFull = emptyFull == null ? "" : emptyFull;
            String dport = tosStowPlan.getDport();
            dport = dport == null ? "" : dport;
            String dischPort = tosStowPlan.getDischargePort();
            dischPort = dischPort == null ? "" : dischPort;
            String loadPort = tosStowPlan.getLoadPort();
            loadPort = loadPort == null ? "" : loadPort;
            String ds = tosStowPlan.getDs();
            ds = ds == null ? "" : ds;
            String comments = tosStowPlan.getComments();
            comments = comments == null ? "" : comments;
            String commodity = tosStowPlan.getCommodity();
            commodity = commodity == null ? "" : commodity;
            String cweight = tosStowPlan.getCweight() == null ? ""
                    : tosStowPlan.getCweight().toString();
            String temp = tosStowPlan.getTemp();
            temp = temp == null ? "" : temp;
            String hazf = tosStowPlan.getHazf();
            hazf = hazf == null ? "" : hazf;
            String srv = tosStowPlan.getSrv();
            srv = srv == null ? "" : srv;
            String locationStatus = tosStowPlan.getLocationStatus();
            locationStatus = locationStatus == null ? "" : locationStatus;
            String tempUnit = tosStowPlan.getTempMeasurementUnit();
            tempUnit = tempUnit == null ? "" : tempUnit;
            String strengthCode = tosStowPlan.getStrength();
            strengthCode = strengthCode == null ? "" : strengthCode;

            // TEMP error
            if (!temp.equals("") && !temp.equals("AMB")) {
                if (!isNumber(temp)) {
                    isErrorContainer = true;
                    errors.add("Invalid temperature.");
                } else {
                    int t = Integer.parseInt(temp);
                    if (t > 80 || t < -50) {
                        isErrorContainer = true;
                        errors.add("Invalid temperature.");
                    }
                }
                // TEMPERATURE UNIT
                if (tempUnit.equals("")) {
                    isErrorContainer = true;
                    errors.add("Temperature unit missing. Verify the temperature.");
                }
            }

            // Invalid Cell
            for (ListIterator<?> iter = mtyCtrList.listIterator(); iter
                    .hasNext(); ) {
                Object[] row = (Object[]) iter.next();
                String ctrno = (String) row[0];
                // logger.info("ctrno:" + ctrno);
                if (tosStowPlan.getContainerNumber().equalsIgnoreCase(ctrno)) {
                    isErrorContainer = true;
                    errors.add("Invalid Cell Location.");
                }

            }
            // A41 - start
            // Validate a FULL container without booking number
            if ("F".equalsIgnoreCase(emptyFull) && (tosStowPlan.getBookingNumber() == null || tosStowPlan.getBookingNumber().equals(""))) {
                isErrorContainer = true;
                errors.add("FULL container without booking number");
            }
            // A41 - end

            if (isErrorContainer) {
                NLTErrors error = new NLTErrors();
                String cd = tosStowPlan.getCheckDigit();
                cd = cd == null ? "X" : cd;
                error.setContainerNumber(tosStowPlan.getContainerNumber() + cd);
                error.setVesvoy(vesvoy);
                error.setTypeCode(tosStowPlan.getTypeCode());
                error.setCommodity(tosStowPlan.getCommodity());
                error.setComments(tosStowPlan.getComments());
                error.setTemp(tosStowPlan.getTemp());
                error.setCell(tosStowPlan.getCell());
                error.setErrors(errors.toString());
                errorsList.add(error);
                logger.info("NTLERR: "
                        + tosStowPlan.getContainerNumber() + ","
                        + tosStowPlan.getCommodity() + "," + vesvoy + ","
                        + errors.toString());
            }
        }
        return errorsList;
    }

    public static boolean vaidateDischargePort(String dischPort) {

        try {
            if (destPodList == null) {
                destPodList = NewReportVesselDao.getDestPodList();
            }
            if (destPodList != null) {
                for (int i = 0; i < destPodList.size(); i++) {
                    TosDestPodData localDestPod = (TosDestPodData) destPodList
                            .get(i);
                    if (dischPort.equals(localDestPod.getPod1())) {
                        return true;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private static boolean isNumber(String num) {
        try {
            Integer.parseInt(num);
        } catch (NumberFormatException ex) {
            return false;
        }
        return true;
    }

    public static void updateCopyFlagAndSendSN4Noti(String vvd) {
        logger.info("updateCopyFlagAndSendSN4Noti Begin");
        try {
            NewVesselDao.updateTosAppParameter("IS_PRIMARY", "true");
            nvLogger.sendNewVessSuccess(vvd, "L", "true");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        logger.info("updateCopyFlagAndSendSN4Noti End");
    }

    public static void genScriptAndSend(ArrayList<TosRdsDataMt> genScript, String vvd) {
        if (vvd == null || genScript == null)
            return;
        logger.info("****** genScriptAndSend begin ******");
        String outFilePath = System.getProperty("java.io.tmpdir");
        if (outFilePath != null && !outFilePath.endsWith("/")) {
            outFilePath = outFilePath + "/";
        }
        String outFileName = outFilePath + vvd + "_EASTBOUND_" + ((new SimpleDateFormat("MM-dd-yyyy")).format(new Date()).toString()) + ".SQL.TXT";
        String insertInto = "Insert into TOS_RDS_DATA_MT (VES,VOY,LEG,CTRNO,SHIPNO,DATASOURCE,LOAD_PORT,DISCHARGE_PORT,DESTINATION_PORT,TEMP,LOAD_DISCH_SERV,HAZF,FREE_STORAGE,ODF,INBOUND,NOTIFY,CREDIT_STATUS,TRANSIT,SHIPPER_AROL,SHIPPER_NAME,SHIPPER_QUALIFIER,CONSIGNEE_AROL,CONSIGNEE_NAME,CONSIGNEE_C_O,CONSIGNEE_QUALIFIER,CONSIGNEE_ADDR,CONSIGNEE_SUITE,CONSIGNEE_CITY,CONSIGNEE_STATE,CONSIGNEE_COUNTRY,CONSIGNEE_ZIP_CODE,CONSIGNEE_DEPARTMENT,CONSIGNEE_TITLE,CONSIGNEE_LAST_NAME,CONSIGNEE_FIRST_NAME,CMDY_DESC,CMDY_SRVPT_DESC,CMDY_AG,CMDY_HHG,CELL,GROSS_WT,MAX_WT,SEAL_NUMBER,OWNER_LESSOR,DAMAGE_STATUS,EMPTY_FULL,ECOS_RETPORT,CHECK_DIGIT,SPEC_MSG1,SPEC_MSG2,SPEC_MSG3,CNSG_AREA_CODE,CNSG_PHONE,PO_NUMBER,PRI_CARRIER,SECOND_VESVOY,TRADE,LOAD_TYPE,MIL_CNEE,MIL_TCN,RDD,CSR_ID,NOTIFY_PARTY,SHIPPER_ORGN_ID,CONSIGNEE_ORGN_ID,DELIVERY_DEPOT,CREATE_USER,CREATE_DATE,LAST_UPDATE_USER,LAST_UPDATE_DATE) values ";
        StringBuilder out = new StringBuilder();
        out.append("set define off;\n");
        String tempStr = "";
        for (int g = 0; g < genScript.size(); g++) {
            TosRdsDataMt temp = genScript.get(g);
            tempStr = "";
            tempStr = insertInto + "(";
            tempStr = tempStr + "'" + temp.getVes() + "',";
            tempStr = tempStr + "'" + temp.getVoy() + "',";
            tempStr = tempStr + "'" + temp.getLeg() + "',";
            tempStr = tempStr + "'" + temp.getCtrno() + "',";
            tempStr = tempStr + "'" + temp.getShipno() + "',";
            tempStr = tempStr + "'" + temp.getDatasource() + "',";
            tempStr = tempStr + "'" + temp.getLoadPort() + "',";
            tempStr = tempStr + "'" + temp.getDischargePort() + "',";
            tempStr = tempStr + "'" + temp.getDestinationPort() + "',";
            tempStr = tempStr + "'" + temp.getTemp() + "',";
            tempStr = tempStr + "'" + temp.getLoadDischServ() + "',";
            tempStr = tempStr + "'" + temp.getHazf() + "',";
            tempStr = tempStr + "'" + temp.getFreeStorage() + "',";
            tempStr = tempStr + "'" + temp.getOdf() + "',";
            tempStr = tempStr + "'" + temp.getInbound() + "',";
            tempStr = tempStr + "'" + temp.getNotify() + "',";
            tempStr = tempStr + "'" + temp.getCreditStatus() + "',";
            tempStr = tempStr + "'" + temp.getTransit() + "',";
            tempStr = tempStr + "'" + temp.getShipperArol() + "',";
            if (temp.getShipperName() != null)
                tempStr = tempStr + "q'[" + temp.getShipperName() + "]',";
            else
                tempStr = tempStr + "'" + "null" + "',";
            if (temp.getShipperQualifier() != null)
                tempStr = tempStr + "q'[" + temp.getShipperQualifier() + "]',";
            else
                tempStr = tempStr + "'" + "null" + "',";
            tempStr = tempStr + "'" + temp.getConsigneeArol() + "',";
            if (temp.getConsigneeName() != null)
                tempStr = tempStr + "q'[" + temp.getConsigneeName() + "]',";
            else
                tempStr = tempStr + "'" + "null" + "',";
            if (temp.getConsigneeCO() != null)
                tempStr = tempStr + "q'[" + temp.getConsigneeCO() + "]',";
            else
                tempStr = tempStr + "'" + "null" + "',";
            if (temp.getConsigneeQualifier() != null)
                tempStr = tempStr + "q'[" + temp.getConsigneeQualifier() + "]',";
            else
                tempStr = tempStr + "'" + "null" + "',";
            if (temp.getConsigneeAddr() != null)
                tempStr = tempStr + "q'[" + temp.getConsigneeAddr() + "]',";
            else
                tempStr = tempStr + "'" + "null" + "',";
            tempStr = tempStr + "'" + temp.getConsigneeSuite() + "',";
            tempStr = tempStr + "'" + temp.getConsigneeCity() + "',";
            tempStr = tempStr + "'" + temp.getConsigneeState() + "',";
            tempStr = tempStr + "'" + temp.getConsigneeCountry() + "',";
            tempStr = tempStr + "'" + temp.getConsigneeZipCode() + "',";
            tempStr = tempStr + "'" + temp.getConsigneeDepartment() + "',";
            tempStr = tempStr + "'" + temp.getConsigneeTitle() + "',";
            tempStr = tempStr + "'" + temp.getConsigneeLastName() + "',";
            tempStr = tempStr + "'" + temp.getConsigneeFirstName() + "',";
            if (temp.getCmdyDesc() != null)
                tempStr = tempStr + "q'[" + temp.getCmdyDesc() + "]',";
            else
                tempStr = tempStr + "'" + "null" + "',";
            if (temp.getCmdySrvptDesc() != null)
                tempStr = tempStr + "q'[" + temp.getCmdySrvptDesc() + "]',";
            else
                tempStr = tempStr + "'" + "null" + "',";
            tempStr = tempStr + "'" + temp.getCmdyAg() + "',";
            tempStr = tempStr + "'" + temp.getCmdyHhg() + "',";
            tempStr = tempStr + "'" + temp.getCell() + "',";
            tempStr = tempStr + "'" + temp.getGrossWt() + "',";
            tempStr = tempStr + "'" + temp.getMaxWt() + "',";
            tempStr = tempStr + "'" + temp.getSealNumber() + "',";
            tempStr = tempStr + "'" + temp.getOwnerLessor() + "',";
            tempStr = tempStr + "'" + temp.getDamageStatus() + "',";
            tempStr = tempStr + "'" + temp.getEmptyFull() + "',";
            tempStr = tempStr + "'" + temp.getEcosRetport() + "',";
            tempStr = tempStr + "'" + temp.getCheckDigit() + "',";
            tempStr = tempStr + "'" + temp.getSpecMsg1() + "',";
            tempStr = tempStr + "'" + temp.getSpecMsg2() + "',";
            tempStr = tempStr + "'" + temp.getSpecMsg3() + "',";
            tempStr = tempStr + "'" + temp.getCnsgAreaCode() + "',";
            tempStr = tempStr + "'" + temp.getCnsgPhone() + "',";
            tempStr = tempStr + "'" + temp.getPoNumber() + "',";
            tempStr = tempStr + "'" + temp.getPriCarrier() + "',";
            tempStr = tempStr + "'" + temp.getSecondVesvoy() + "',";
            tempStr = tempStr + "'" + temp.getTrade() + "',";
            tempStr = tempStr + "'" + temp.getLoadType() + "',";
            tempStr = tempStr + "'" + temp.getMilCnee() + "',";
            tempStr = tempStr + "'" + temp.getMilTcn() + "',";
            tempStr = tempStr + "'" + temp.getRdd() + "',";
            tempStr = tempStr + "'" + temp.getCsrId() + "',";
            tempStr = tempStr + "'" + temp.getNotifyParty() + "',";
            tempStr = tempStr + "'" + temp.getShipperOrgnId() + "',";
            tempStr = tempStr + "'" + temp.getConsigneeOrgnId() + "',";
            tempStr = tempStr + "'" + temp.getDeliveryDepot() + "',";
            tempStr = tempStr + "'" + temp.getCreateUser() + "',";
            if (temp.getCreateDate() != null)
                tempStr = tempStr + "to_date('" + temp.getCreateDate() + "', 'YYYY-MM-DD'),";
            else
                tempStr = tempStr + "'" + temp.getCreateDate() + "',";
            tempStr = tempStr + "'" + temp.getLastUpdateUser() + "',";
            if (temp.getLastUpdateDate() != null)
                tempStr = tempStr + "to_date('" + temp.getLastUpdateDate() + "', 'YYYY-MM-DD')";
            else
                tempStr = tempStr + "'" + temp.getLastUpdateDate() + "'";
            tempStr = tempStr + ");";
            tempStr = tempStr.replaceAll("'null'", "null");
            logger.info(tempStr);
            out.append(tempStr + "\n");
        }
        try {
            if (out.toString().length() > 0) {
                logger.info("Generating eastbound insert script file..." + outFileName);
                FileWriter fileWriter = new FileWriter(new File(outFileName));
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                bufferedWriter.write(out.toString());
                bufferedWriter.close();
                String host = TosRefDataUtil.getValue("MAIL_HOST");
                String from = TosRefDataUtil.getValue("EMAIL_NEWVES_FROM");
                String to = TosRefDataUtil.getValue("SUPPORT_EMAIL");
                String message = "Please find the attached insert script for " + vvd;
                String subject = "Eastbound Insert Script For " + vvd;
                logger.info("Attaching eastbound insert script file..." + outFileName);
                EmailSender.mailTextAttachment(to, from, host, outFileName, message, subject);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("ERROR: genScriptAndSend \n" + e);
        }
        logger.info("****** genScriptAndSend end ******");
    }

    public static ArrayList<TosRdsDataFinalMt> updateConsigneeSupp(
            ArrayList<TosRdsDataFinalMt> rdsDataFinal,
            ArrayList<TosRdsDataMt> rdsData) {
        logger.info("**** updateConsigneeSupp ****");
        for (int i = 0; i < rdsDataFinal.size(); i++) {
            TosRdsDataFinalMt rdsFd = rdsDataFinal.get(i);
            String cargoNotes = rdsFd.getCargoNotes();
            String ds = rdsFd.getDs();
            ds = ds == null ? "" : ds;
            String dir = rdsFd.getDir();
            dir = dir == null ? "" : dir;
            String typeCode = rdsFd.getTypeCode();
            typeCode = typeCode == null ? "" : typeCode;
            String consignee = rdsFd.getConsignee();
            consignee = consignee == null ? "" : consignee;
            String consigneeName = rdsFd.getConsigneeName();
            consigneeName = consigneeName == null ? "" : consigneeName;
            String dport = rdsFd.getDport();
            dport = dport == null ? "" : dport;
            //String mixedDS = ""; //A39
            logger.info("SUPP CNTR:" + rdsFd.getContainerNumber() + "\tCNEE:"
                    + consignee + "\tCNOTES:" + cargoNotes);


            if ("IN".equals(dir) && "CY".equals(ds) && !typeCode.endsWith("GB")
                    && !typeCode.endsWith("GR")) {
                // Raghu - Commeneted this code. If cosignee does not come in supplemental, then consignee in TOS should be retained.
                // The below logic makes sense in case of primary where if the consignee is not downloaded from FACTS, then the unit should mark with "REQUIRES CS ACTION".
                // For supplemental this is not requiredd.
				/*if (consignee.equals(""))
				{
					rdsFd.setConsignee("REQUIRES CS ACTION");
					logger.info("REQCSACTION "
							+ rdsFd.getContainerNumber());
				}*/
                if (consignee.equals("UNKNOWN")
                        || consignee.equals("WA")
                        || consignee.equals("WILL ADVISE")
                        || consignee.startsWith("X ")) {
                    rdsFd.setConsignee("REQUIRES CS ACTION - " + consignee);
                    logger.info("REQCSACTION- "
                            + rdsFd.getContainerNumber());
                }
            }

			/*if (ds != null && "AUT".equals(ds)) //A39
			{
				logger.info("SUPP AUTO rdsFd.getConsignee()---" + rdsFd.getConsignee());
				if (rdsFd.getShipper() != null
						&& rdsFd.getShipper().contains("FORD MOTOR COMPANY"))
				{
					if (rdsFd.getConsignee() != null
							&& (!(rdsFd.getConsignee().contains("BUDGET RENT")
									|| rdsFd.getConsignee().contains(
											"HERTZ RENT")
									|| rdsFd.getConsignee().contains(
											"AVIS RENT") || rdsFd
									.getConsignee().contains("DOLLAR RENT"))) && "AUTOMOBILE".equalsIgnoreCase(rdsFd.getConsignee()))
					{
						rdsFd.setConsignee("AUTO-SPRAY");
					}
					else
					{
						rdsFd.setConsignee("AUTOMOBILE");
					}
				}
				else {

					rdsFd.setConsignee("AUTOMOBILE");
				}

			}*/
			/*if (cargoNotes != null)
			{
				if (cargoNotes.contains("REQUIRES CS ACTION"))
				{
					rdsFd.setConsignee("REQUIRES CS ACTION");
				}
				else if (cargoNotes.contains("UNAPPROVED VARIANCE"))
				{
					rdsFd.setConsignee("UNAPPROVED VARIANCE");
					cargoNotes = cargoNotes.replaceAll("UNAPPROVED VARIANCE",
							"").trim();
					rdsFd.setCargoNotes(cargoNotes);
				}
			}*/
			/*if (dport.equalsIgnoreCase("MIX")) //A39
			{
				String containerNumber = rdsFd.getContainerNumber();
				ArrayList<TosRdsDataMt> cntrRdsData = new ArrayList<TosRdsDataMt>();
				ArrayList<String> shipperNameList = new ArrayList<String>();
				ArrayList<String> dischargeServiceList = new ArrayList<String>();
				for (int r = 0; r < rdsData.size(); r++)
				{
					TosRdsDataMt tempData = rdsData.get(r);
					if (tempData.getCtrno() != null
							&& tempData.getCtrno().equals(containerNumber))
					{
						cntrRdsData.add(tempData);
						if (tempData.getShipperName() != null
								&& tempData.getShipperName().length() > 0)
						{
							shipperNameList.add(tempData.getShipperName());
						}
						if (tempData.getLoadDischServ() != null
								&& tempData.getLoadDischServ().length() > 0)
						{
							dischargeServiceList.add(tempData
									.getLoadDischServ());
						}

					}
				}
				if (cntrRdsData.size() > 1 && shipperNameList.size() > 1)
				{
					boolean isSameShipper = false;
					String shipper = "";
					for (int s = 0; s < shipperNameList.size(); s++)
					{
						String tempShipper = shipperNameList.get(s);
						if (shipper.equals(""))
							shipper = tempShipper;
						else
						{
							if (shipper.equals(tempShipper))
								isSameShipper = true;
							else
							{
								isSameShipper = false;
								break;
							}
						}
					}
					if (!isSameShipper)
					{
						rdsFd.setConsignee("MIX");
					}
				}

				if (cntrRdsData.size() > 1 && dischargeServiceList.size() > 1)
				{
					mixedDS = "N";
					String localds = "";
					for (int x = 0; x < dischargeServiceList.size(); x++)
					{
						String tempDs = dischargeServiceList.get(x);
						if (localds.equals(""))
							localds = tempDs;
						else
						{
							if (localds.equals(tempDs))
								mixedDS = "N";
							else
							{
								mixedDS = "Y";
								break;
							}
						}
					}
				}
				logger.info("SUPP mixedDS :"+mixedDS);
				// Raghu Pattangi
				if ("AUTOMOBILE".equalsIgnoreCase(rdsFd.getConsignee())
						&& "N".equals(mixedDS))
				{
					rdsFd.setConsignee("MIX");
				}
			}*/
            logger.info("SUPP CNEE-" + rdsFd.getConsignee());
            rdsDataFinal.set(i, rdsFd);
        }
        return rdsDataFinal;
    }

    public static void addSupplementProblems(String discrepancy, TosRdsDataFinalMt oldRdsFd, TosRdsDataFinalMt newRdsFd, String tState, String eventDate, String carrMode,
                                             String group, String destination, String consignee, String blNbr, String holds, String obActual) { //A35
        if (supProblemsList != null) {
            SupplementProblems pblm = new SupplementProblems();
            pblm.setDiscrepancy(discrepancy);
            pblm.setContainerNumber(oldRdsFd.getContainerNumber());
            pblm.setVesvoy(oldRdsFd.getVesvoy());
            if (eventDate != null && !"null".equals(eventDate)) {
                pblm.setEventDate(eventDate);
            }
            logger.info("Container Number, tState, carrMode, discrepancy->" + oldRdsFd.getContainerNumber() + " - " + tState + " - " + carrMode + " - " + discrepancy);
            if (tState != null && !"null".equals(tState)) {
                group = group == null ? "" : group;
                group = group.equalsIgnoreCase("null") ? "" : group;
                if ("S70_DEPARTED".equals(tState) && "TRUCK".equalsIgnoreCase(carrMode)) {
                    pblm.setLocationStatus("Departed");
                    pblm.setEvent("Truck   " + group); //A34
                } else if ("S99_RETIRED".equals(tState)) {
                    pblm.setLocationStatus("Retired");
                    pblm.setEvent("Truck   " + group); //A34
                } else if ("S20_INBOUND".equals(tState)) {
                    pblm.setLocationStatus("Inbound");
                    pblm.setEvent("Vessel   " + group);
                } else if ("S40_YARD".equals(tState)) {
                    pblm.setLocationStatus("Yard");
                    pblm.setEvent("      " + group);
                } else if ("S70_DEPARTED".equals(tState) && "VESSEL".equalsIgnoreCase(carrMode)) {
                    pblm.setLocationStatus("Departed");
                    pblm.setEvent("Vessel   " + group);
                    pblm.setVesvoy(obActual);
                    logger.info("ObActual Value updated" + obActual);
                }
            }
            pblm.setOldDS(oldRdsFd.getDs());
            // A35 starts
			/*pblm.setOldDport(oldRdsFd.getDport());
			pblm.setOldCrstatus(oldRdsFd.getCrstatus());
			pblm.setOldConsignee(oldRdsFd.getConsignee());
			pblm.setOldBookingNbr(oldRdsFd.getBookingNumber());*/
            if (destination == null || destination.equals("null"))
                pblm.setOldDport(null);
            else
                pblm.setOldDport(destination);
            if (consignee == null || consignee.equals("null"))
                pblm.setOldConsignee(null);
            else
                pblm.setOldConsignee(consignee);
            if (blNbr == null || blNbr.equals("null"))
                pblm.setOldBookingNbr(null);
            else
                pblm.setOldBookingNbr(blNbr);
            if (holds == null || holds.equals("null"))
                pblm.setOldCrstatus(null);
            else
                pblm.setOldCrstatus(holds);
            //A35 ends
            pblm.setSupDS(newRdsFd.getDs());
            pblm.setSupDport(newRdsFd.getDport());
            pblm.setSupCrstatus(newRdsFd.getCrstatus());
            pblm.setSupConsignee(newRdsFd.getConsignee());
            pblm.setSupBookingNbr(newRdsFd.getBookingNumber());
            supProblemsList.add(pblm);
        }
    }

    public static boolean isOneOfGCSPorts(String port) {
        if (port != null && (port.equals("GUM") || port.equals("TMGU")
                || port.equals("NGB") || port.equals("SHA")
                || port.equals("TSI") || port.equals("XMN") || port.equals("YTN")))
            return true;

        return false;
    }

    public static boolean isValidVesvoy(String vesvoy) {
        boolean valid = false;
        VesselVO vesselVO = CommonBusinessProcessor.getVesselDetails(vesvoy.substring(0, 3));
        if (vesselVO.getVessCode() != null) {
            valid = true;
        }
        return valid;
    }

    public static String determineEastBoundVvd(String currentVvd) {
        String prevVvd = null;
        if (currentVvd != null && currentVvd.length() > 6) {
            String vesCode = currentVvd.substring(0, 3);
            String voyCode = currentVvd.substring(3, 6);
            try {
                int voyage = Integer.parseInt(voyCode);
                if (voyage > 0)
                    voyage = voyage - 1;
                prevVvd = vesCode + leftPad("" + voyage, "0", 3) + "E";
            } catch (Exception e) {
                logger.error("Error in determineEastBoundVvd :- ", e);
            }
        }
        return prevVvd;
    }

    public static String leftPad(String inputStr, String padChars, Integer length) {
        String result = inputStr;
        while (result.length() < length)
            result = padChars + result;
        return result;
    }

    public static String getVesselService(String vvd) {
        String vesselService = null;
        // gettting vessel service
        try {
            if (tosLookUp == null)
                tosLookUp = new TosLookup();
            vesselService = tosLookUp.getVesselService(vvd.substring(0, 6));
        } catch (Exception ex1) {
            logger.error("Unable to get vessel service for " + vvd, ex1);
        }
        logger.info("vesselService is : " + vesselService);
        // getting service end
        cleanUp();
        return vesselService;
    }
    public static Boolean getPropertyValueAsBoolean(String s) {
        String returnValue = TosRefDataUtil.getValue(s);
        logger.info("HOLD key : " + s  + "value as "+ returnValue);
        if (returnValue != null && !returnValue.isEmpty()) {
            try {
                Boolean returnBoolean = Boolean.parseBoolean(returnValue);
                return returnBoolean;
            } catch (Exception ex) {
                logger.error("Not able to parse the value " + returnValue + " to boolean, using Boolean.parseBoolean()", ex);
            }
        } else {
            logger.error("configure value for reference " + s);
            return Boolean.FALSE;
        }
        return Boolean.FALSE;
    }
}
