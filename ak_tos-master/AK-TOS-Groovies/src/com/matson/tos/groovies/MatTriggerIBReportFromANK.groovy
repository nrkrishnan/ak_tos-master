import com.navis.apex.business.model.GroovyInjectionBase
import com.navis.argo.business.api.GroovyApi
import com.navis.argo.business.atoms.CarrierVisitPhaseEnum
import com.navis.argo.business.atoms.FreightKindEnum
import com.navis.argo.business.atoms.LocTypeEnum
import com.navis.argo.business.model.CarrierVisit
import com.navis.argo.business.model.Facility
import com.navis.argo.business.model.GeneralReference
import com.navis.framework.persistence.HibernateApi
import com.navis.framework.portal.Ordering
import com.navis.framework.portal.QueryUtils
import com.navis.framework.portal.query.DomainQuery
import com.navis.framework.portal.query.PredicateFactory
import com.navis.inventory.InventoryEntity
import com.navis.inventory.business.api.UnitField
import com.navis.inventory.business.units.Unit
import com.navis.inventory.business.units.UnitFacilityVisit
import com.navis.services.business.event.GroovyEvent
import com.navis.vessel.business.schedule.VesselVisitDetails
import net.sf.jasperreports.engine.JRDataSource
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource
import org.apache.log4j.Logger

/*

*/

public class MatTriggerIBReportFromANK extends GroovyApi {


    public static final String KDK = "KDK"
    public static final String DUT = "DUT"
    public static final String REPORT_DESIGN_NAME = "IB REPORT"
    public String KDK_TO_EMAIL = EMAIL_FROM;
    public String DUT_TO_EMAIL = EMAIL_FROM;
    public static final String EMAIL_FROM = "1aktosdevteam@matson.com"
    public static final String FAILURE_EMAIL_TO = "1aktosdevteam@matson.com"
    public static final String ANK = "ANK"
    public static final String TAC = "TAC"

    public void generateReportOnNewVesComplete(GroovyEvent event, Object api) {
        LOGGER.warn("Start execution of MatTriggerIBReportFromANK")
        Unit unit = event.getEntity();
        LOGGER.warn("Unit is " + unit.getUnitId());
        Facility ibFacility = unit.getInboundCv().getCvFacility();
        CarrierVisit obCarrierVisit = unit.getInboundCv();// this is ANK, get the IB Carrier visit and pass around
        LOGGER.warn("Inbound Facility " + ibFacility.getFcyId());
        Facility obFacility = unit.getInboundCv().getCvNextFacility();
        if (obFacility == null) {
            LOGGER.warn("There is no next facility for the carrier " + obCarrierVisit.getCvId() + " at " + ibFacility.getFcyId());
        } else {
            LOGGER.warn("Outbound Facility " + obFacility.getFcyId());
        }
        Thread.sleep(120000L);
        // wait 2 minutes to trigger the report, as it needs the BL's populated ( from DONOT EDIT .... to proper BL/Booking Nbr
        GenerateReport(obCarrierVisit, null);
    }

    public void generateReportOnPhaseVV(GroovyEvent event, Object api) {
        LOGGER.warn("Start execution of MatTriggerIBReportFromANK")
        VesselVisitDetails vesselVisitDetails = event.getEntity();
        LOGGER.warn("CarrierVisit is " + vesselVisitDetails.getCvdCv().getCvId());
        Facility ibFacility = vesselVisitDetails.getInboundCv().getCvFacility();
        CarrierVisit obCarrierVisit = vesselVisitDetails.getOutboundCv();
        LOGGER.warn("Inbound Facility " + ibFacility.getFcyId());
        Facility obFacility = vesselVisitDetails.getInboundCv().getCvNextFacility();
        if (obFacility == null) {
            LOGGER.warn("There is no next facility for the carrier " + obCarrierVisit.getCvId() + " at " + ibFacility.getFcyId());
        } else {
            LOGGER.warn("Outbound Facility " + obFacility.getFcyId());
        }
        GenerateReport(obCarrierVisit, null);
    }

    public void generateReportForKDKOnSrvEvent(GroovyEvent event, Object api) {
        LOGGER.warn("Start execution of MatTriggerIBReportFromANK")
        VesselVisitDetails vesselVisitDetails = event.getEntity();
        LOGGER.warn("CarrierVisit is " + vesselVisitDetails.getCvdCv().getCvId());
        Facility ibFacility = vesselVisitDetails.getInboundCv().getCvFacility();
        CarrierVisit obCarrierVisit = vesselVisitDetails.getOutboundCv();
        LOGGER.warn("Inbound Facility " + ibFacility.getFcyId());
        Facility obFacility = vesselVisitDetails.getInboundCv().getCvNextFacility();
        if (obFacility == null) {
            LOGGER.warn("There is no next facility for the carrier " + obCarrierVisit.getCvId() + " at " + ibFacility.getFcyId());
        } else {
            LOGGER.warn("Outbound Facility " + obFacility.getFcyId());
        }
        GenerateReport(obCarrierVisit, KDK);
    }

    public void generateReportForDUTOnSrvEvent(GroovyEvent event, Object api) {
        LOGGER.warn("Start execution of MatTriggerIBReportFromANK")
        VesselVisitDetails vesselVisitDetails = event.getEntity();
        LOGGER.warn("CarrierVisit is " + vesselVisitDetails.getCvdCv().getCvId());
        Facility ibFacility = vesselVisitDetails.getInboundCv().getCvFacility();
        CarrierVisit obCarrierVisit = vesselVisitDetails.getOutboundCv();
        LOGGER.warn("Inbound Facility " + ibFacility.getFcyId());
        Facility obFacility = vesselVisitDetails.getInboundCv().getCvNextFacility();
        if (obFacility == null) {
            LOGGER.warn("There is no next facility for the carrier " + obCarrierVisit.getCvId() + " at " + ibFacility.getFcyId());
        } else {
            LOGGER.warn("Outbound Facility " + obFacility.getFcyId());
        }
        GenerateReport(obCarrierVisit, DUT);
    }

    private void GenerateReport(CarrierVisit obCarrierVisit, String inFacility) {
        Boolean runOnlyForSelectedFacility = Boolean.FALSE;
        GeneralReference genRef = GeneralReference.findUniqueEntryById("MATSON", "EMAIL", "REPORT", "KDK");
        KDK_TO_EMAIL = genRef.getRefValue1();
        LOGGER.warn("KDK MAIL  \t" + KDK_TO_EMAIL);
        genRef = GeneralReference.findUniqueEntryById("MATSON", "EMAIL", "REPORT", "DUT");
        DUT_TO_EMAIL = genRef.getRefValue1();
        LOGGER.warn("DUT MAIL \t" + DUT_TO_EMAIL);
        if (!(obCarrierVisit.getCvOperator().getBzuId().equalsIgnoreCase("MAT") || obCarrierVisit.getCvOperator().getBzuId().equalsIgnoreCase("MATU"))) {
            LOGGER.warn(" No Requirement to send the Report as operator is " + obCarrierVisit.getCvOperator().getBzuId());
            return;
        }
        if (obCarrierVisit.getCarrierVesselClassType().toString().contains("BARGE")) {
            LOGGER.warn(" No Requirement to send the Report as operator is " + obCarrierVisit.getCvOperator().getBzuId() + " and it's by barge " + obCarrierVisit.getCarrierVesselClassType().toString());
            return;
        }
        if (inFacility != null && !inFacility.isEmpty() && (KDK.equals(inFacility) || DUT.equals(inFacility)))
            runOnlyForSelectedFacility = Boolean.TRUE;
        GroovyInjectionBase inj = new GroovyInjectionBase();
        String carrierVisitID = obCarrierVisit.getCvId(); // use this to determine, if there is a CV to DUT
        String cvLlyods = obCarrierVisit.getCvCvd().getCarrierDocumentationNbr();
        LOGGER.warn(" CV " + carrierVisitID);
        Boolean processDutCv = Boolean.FALSE;
        Facility currentFacility = obCarrierVisit.getCvFacility();
        String currentFacilityId = currentFacility.getFcyId();

        if (inFacility != null && !inFacility.isEmpty() && inFacility.equalsIgnoreCase(currentFacilityId)) {
            sendFailureMail(" " + currentFacilityId + " Vessel visit " + carrierVisitID + " (" + currentFacilityId + ") - Failure to Run IB Report",
                    " " + currentFacilityId + " Vessel visit " + carrierVisitID + " (" + currentFacilityId + ") - Failure to Run IB Report,\n " +
                            "Cannot run this report on the vessel visit at " + currentFacilityId + " for units to faility " + currentFacilityId + " Try running it on vessel visit's  previous facility ",
                    FAILURE_EMAIL_TO);
            return;
        }

        Boolean processForKDK = Boolean.FALSE;
        Boolean processForDUT = Boolean.FALSE;
        Boolean is_DUT_VV = Boolean.FALSE;
        Boolean is_KDK_VV = Boolean.FALSE;


        if (ANK.equalsIgnoreCase(currentFacilityId)) {
            processForKDK = Boolean.TRUE;
            processForDUT = Boolean.TRUE;

        } else if (KDK.equalsIgnoreCase(currentFacilityId)) {
            processForDUT = Boolean.TRUE;
        }
        if (processForDUT) {
            CarrierVisit dutCv = CarrierVisit.findCarrierVisit(Facility.findFacility(DUT), LocTypeEnum.VESSEL, carrierVisitID);
            if (dutCv != null && !CarrierVisitPhaseEnum.CANCELED.equals(dutCv.getCvVisitPhase())) {
                is_DUT_VV = Boolean.TRUE;
                LOGGER.warn("Is an Vessel visit with ID " + carrierVisitID + " for facility DUT " + is_DUT_VV);
            } else
                LOGGER.warn("There is No Vessel visit with ID " + carrierVisitID + " for facility DUT or it is cancelled");
        }
        if (processForKDK) {
            CarrierVisit kdkCv = CarrierVisit.findCarrierVisit(Facility.findFacility(KDK), LocTypeEnum.VESSEL, carrierVisitID);
            if (kdkCv != null && !CarrierVisitPhaseEnum.CANCELED.equals(kdkCv.getCvVisitPhase())) {
                is_KDK_VV = Boolean.TRUE;
                LOGGER.warn("Is an Vessel visit with ID " + carrierVisitID + " for facility KDK " + is_KDK_VV);
            } else
                LOGGER.warn("There is No Vessel visit with ID " + carrierVisitID + " for facility KDK or it is cancelled");
        }

        if (processForDUT || processForKDK) {
            HashMap reportParameters = new HashMap();
            reportParameters.put("InboundCarrierId", carrierVisitID);
            reportParameters.put("InboundCarrierDocumentationNbr", cvLlyods);

            //report datasource

            List<Map> kdkUnitList = Collections.EMPTY_LIST;
            if (is_KDK_VV)
                kdkUnitList = findAndPopulateUnitsByFcy(obCarrierVisit, KDK);

            List<Map> dutUnitList = Collections.EMPTY_LIST;
            if (is_DUT_VV)
                dutUnitList = findAndPopulateUnitsByFcy(obCarrierVisit, DUT);

            String emailToKDK = EMAIL_FROM + ";" + KDK_TO_EMAIL;
            String emailToDUT = EMAIL_FROM + ";" + DUT_TO_EMAIL;
            LOGGER.warn("emailToKDK\t" + emailToKDK);
            LOGGER.warn("emailToDUT\t" + emailToDUT);

            JRDataSource reportDataSource = new JRMapCollectionDataSource(kdkUnitList);
            def reportRun = inj.getGroovyClassInstance("ReportRunner");


            String reportDesignName = REPORT_DESIGN_NAME;
            LOGGER.warn("Report Design Name " + reportDesignName);
            LOGGER.warn(" Calling report to run ");
            /**
             * Based on selection to run the report, for required facility (triggered by service event)
             */

            if (runOnlyForSelectedFacility) {
                if (inFacility != null && !inFacility.isEmpty() && KDK.equalsIgnoreCase(inFacility)) {
                    if (!kdkUnitList.isEmpty()) {
                        reportParameters.put("Port", KDK);
                        reportParameters.put("Vessel", carrierVisitID);
                        reportRun.emailExcelReport(reportDataSource, reportParameters, reportDesignName, emailToKDK, "IB Units on " + carrierVisitID + "(" + currentFacilityId + ") to KDK", "Attached report for KDK" + carrierVisitID);
                        LOGGER.warn("Report Send for KDK");
                    } else {
                        sendFailureMail(" " + KDK + " " + carrierVisitID + " No Data found ", "No Units found for the visit " + carrierVisitID + " to KDK", FAILURE_EMAIL_TO);
                    }
                } else if (inFacility != null && !inFacility.isEmpty() && DUT.equalsIgnoreCase(inFacility) && is_DUT_VV) {
                    if (!dutUnitList.isEmpty()) {
                        reportParameters.put("Port", DUT);
                        reportParameters.put("Vessel", carrierVisitID);
                        reportDataSource = new JRMapCollectionDataSource(dutUnitList);
                        reportRun.emailExcelReport(reportDataSource, reportParameters, reportDesignName, emailToDUT, "IB Units on " + carrierVisitID + "(" + currentFacilityId + ") to DUT", "Attached report for DUT" + carrierVisitID);
                        LOGGER.warn("Report Send for DUT");
                    } else {
                        sendFailureMail(" " + DUT + " " + carrierVisitID + " No Data found ", "No Units found for the visit " + carrierVisitID + " to DUT", FAILURE_EMAIL_TO);
                    }

                }
            }
            /**
             * This is for the trigger from Phase VV and NewVesCompleted
             */
            else if (!runOnlyForSelectedFacility) {
                if (is_KDK_VV) {
                    LOGGER.warn(" Calling report to run for DUT");
                    reportParameters.put("Port", KDK);
                    reportParameters.put("Vessel", carrierVisitID);
                    reportDataSource = new JRMapCollectionDataSource(kdkUnitList);
                    reportRun.emailExcelReport(reportDataSource, reportParameters, reportDesignName, emailToKDK, "IB Units on " + carrierVisitID + "(" + currentFacilityId + ") to KDK", "Attached report for KDK" + carrierVisitID);
                    LOGGER.warn("Report Send for KDK");
                }
                    LOGGER.warn(" Calling report to run for DUT");
                    if (is_DUT_VV) {
                    reportParameters.put("Port", DUT);
                    reportParameters.put("Vessel", carrierVisitID);
                    reportDataSource = new JRMapCollectionDataSource(dutUnitList);
                    reportRun.emailExcelReport(reportDataSource, reportParameters, reportDesignName, emailToDUT, "IB Units on " + carrierVisitID + "(" + currentFacilityId + ") to DUT", "Attached report for DUT" + carrierVisitID);
                    LOGGER.warn("Report Send for DUT");
                }
            }
        }

        if (!processForDUT && !processForKDK) {
            String errorMessage = "The vessel visit " + carrierVisitID + " is not in facility ANK or KDK it is for facility " + currentFacilityId + " this will not trigger IB Report. \n" +
                    "\nThis functionality is meant for usage only at Facility ANK or KDK and is not usable elsewhere \n";
            sendFailureMail("Failed to Generate IB Report for CarrierVisit " + carrierVisitID, errorMessage, FAILURE_EMAIL_TO);
        }
    }

    List<Map> findAndPopulateUnitsByFcy(CarrierVisit inOutboundCarrierVisit, String inFcy) {
        LOGGER.warn(" Executing Query to Pick Units");
        Ordering[] orderings = new Ordering[2];
        orderings[0] = Ordering.asc(UnitField.UFV_ARRIVE_POS_SLOT);
        orderings[1] = Ordering.asc(UnitField.UFV_POD);

        DomainQuery domainQuery = QueryUtils.createDomainQuery(InventoryEntity.UNIT_FACILITY_VISIT)
                .addDqPredicate(PredicateFactory.eq(UnitField.UFV_ACTUAL_OB_CV, inOutboundCarrierVisit.getCvGkey()))
                .addDqPredicate(PredicateFactory.ne(UnitField.UFV_POD_ID, "ANK"))
                .addDqOrderings(orderings);
        List<UnitFacilityVisit> unitList = (List<UnitFacilityVisit>) HibernateApi.getInstance().findEntitiesByDomainQuery(domainQuery);
        LOGGER.warn(" Units are of size " + unitList.size());
        List<Unit> reportUnits = new ArrayList<>();


        for (UnitFacilityVisit unitFacilityVisit : unitList) {
            if (KDK.equalsIgnoreCase(inFcy) && unitFacilityVisit.getUfvUnit().getUnitRouting().getRtgPOD1() != null &&
                    (unitFacilityVisit.getUfvUnit().getUnitRouting().getRtgPOD1().getPointId().equalsIgnoreCase(KDK) ||
                            unitFacilityVisit.getUfvUnit().getUnitRouting().getRtgPOD1().getPointId().equalsIgnoreCase(DUT) ||
                            unitFacilityVisit.getUfvUnit().getUnitRouting().getRtgPOD1().getPointId().equalsIgnoreCase(TAC))) {

                reportUnits.add(unitFacilityVisit);

            }
            if (DUT.equalsIgnoreCase(inFcy) && unitFacilityVisit.getUfvUnit().getUnitRouting().getRtgPOD1() != null &&
                    (unitFacilityVisit.getUfvUnit().getUnitRouting().getRtgPOD1().getPointId().equalsIgnoreCase(DUT) ||
                            unitFacilityVisit.getUfvUnit().getUnitRouting().getRtgPOD1().getPointId().equalsIgnoreCase(TAC))) {

                reportUnits.add(unitFacilityVisit);

            }
        }
        LOGGER.warn(" Number of units for report " + reportUnits.size());

        List<Map> list = new ArrayList<>();



        for (UnitFacilityVisit facilityVisit : reportUnits) {
            Map map = new HashMap();
            map.put("EquipmentCD", facilityVisit.getUfvUnit().getUnitId());
            map.put("EquipmentIso", facilityVisit.getUfvUnit().getUnitPrimaryUe().getUeEquipment().getEqEquipType().getEqtypId());
            map.put("POL", facilityVisit.getUfvUnit().getUnitRouting().getRtgPOL()!=null?facilityVisit.getUfvUnit().getUnitRouting().getRtgPOL().getPointId():"--");
            map.put("POD", facilityVisit.getUfvUnit().getUnitRouting().getRtgPOD1()!=null?facilityVisit.getUfvUnit().getUnitRouting().getRtgPOD1().getPointId():"--");
            map.put("Dest", facilityVisit.getUfvUnit().getUnitGoods()!=null?facilityVisit.getUfvUnit().getUnitGoods().getGdsDestination():"--");
            map.put("Weight", facilityVisit.getUfvUnit().getUnitGoodsAndCtrWtKg().toString());
            map.put("Shipper", facilityVisit.getUfvUnit().getUnitGoods()!=null?facilityVisit.getUfvUnit().getUnitGoods().getShipperAsString():"--");
            map.put("Consignee", facilityVisit.getUfvUnit().getUnitGoods()!=null?facilityVisit.getUfvUnit().getUnitGoods().getConsigneeAsString():"--");
            map.put("Haz", facilityVisit.getUfvUnit().getUnitGoods()!=null?facilityVisit.getUfvUnit().getUnitGoods().getGdsImdgTypes():"--");
            map.put("StowPosition", facilityVisit.getUfvUnit().getUnitArrivePosition()!=null?facilityVisit.getUfvUnit().getUnitArrivePosition().getPosSlot():"--");
            map.put("FreightKind", facilityVisit.getUfvUnit().getUnitFreightKind().equals(FreightKindEnum.FCL) ? "F" : "E");
            map.put("IsReefer", facilityVisit.getUfvUnit().isReefer() ? "Reefer" : "");
            map.put("TempSetting", facilityVisit.getUfvFlexString07());
            map.put("Operator", facilityVisit.getUfvUnit().getUnitLineOperator().getBzuId());
            String bookingNumber = facilityVisit.getUfvUnit().getUnitGoods() != null && facilityVisit.getUfvUnit().getUnitGoods().getGdsBlNbr() != null ? facilityVisit.getUfvUnit().getUnitGoods().getGdsBlNbr() :
                    (facilityVisit.getUfvUnit().getUnitPrimaryUe().getUeDepartureOrderItem() != null ? facilityVisit.getUfvUnit().getUnitPrimaryUe().getUeDepartureOrderItem().getEqboiOrder().getEqboNbr() : "--");
            map.put("BookingNumber", bookingNumber);
            list.add(map);
        }
        LOGGER.warn("List Size " + list.size());
        return list;

    }

    public void sendFailureMail(String inSubject, String inErrorMessage, String inEmailTo) {
        GeneralReference genRef = GeneralReference.findUniqueEntryById("ENV", "ENVIRONMENT");
        String environment = genRef.getRefValue1();
        String emailFrom = EMAIL_FROM;
        String emailTo = inEmailTo;
        String emailSubject = environment + inSubject;
        String emailBody = inErrorMessage;
        sendEmail(emailTo, emailFrom, emailSubject, emailBody);
    }

    private static final Logger LOGGER = Logger.getLogger(MatTriggerIBReportFromANK.class);
}