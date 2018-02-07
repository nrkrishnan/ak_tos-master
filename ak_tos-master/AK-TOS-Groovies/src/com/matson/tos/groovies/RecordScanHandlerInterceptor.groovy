import com.navis.argo.ArgoRefField
import com.navis.argo.ContextHelper
import com.navis.argo.business.api.GroovyApi
import com.navis.argo.business.atoms.EquipClassEnum
import com.navis.argo.business.atoms.FreightKindEnum
import com.navis.argo.business.atoms.UnitCategoryEnum
import com.navis.argo.business.model.CarrierVisit
import com.navis.argo.business.reference.Accessory
import com.navis.argo.business.reference.Chassis
import com.navis.argo.business.reference.Container
import com.navis.argo.business.reference.Equipment
import com.navis.external.road.EGateApiHandlerInterceptor
import com.navis.framework.business.Roastery
import com.navis.framework.business.atoms.LifeCycleStateEnum
import com.navis.framework.persistence.HibernateApi
import com.navis.framework.persistence.hibernate.CarinaPersistenceCallback
import com.navis.framework.persistence.hibernate.PersistenceTemplatePropagationRequired
import com.navis.framework.portal.FieldChanges
import com.navis.framework.portal.QueryUtils
import com.navis.framework.portal.UserContext
import com.navis.framework.portal.query.DomainQuery
import com.navis.framework.portal.query.PredicateFactory
import com.navis.framework.util.BizFailure
import com.navis.framework.util.BizViolation
import com.navis.framework.util.message.MessageCollector
import com.navis.inventory.business.api.UnitManager
import com.navis.inventory.business.atoms.UfvTransitStateEnum
import com.navis.inventory.business.imdg.Placard
import com.navis.inventory.business.units.Unit
import com.navis.inventory.business.units.UnitFacilityVisit
import com.navis.orders.business.api.OrdersFinder
import com.navis.orders.business.eqorders.EquipmentOrder
import com.navis.road.RoadField
import com.navis.road.business.api.IGateApiHandler
import com.navis.road.business.api.RoadManager
import com.navis.road.business.atoms.GateClientTypeEnum
import com.navis.road.business.atoms.TranStatusEnum
import com.navis.road.business.atoms.TranSubTypeEnum
import com.navis.road.business.atoms.TruckVisitStatusEnum
import com.navis.road.business.atoms.TruckVisitStatusGroupEnum
import com.navis.road.business.model.RoadInspection
import com.navis.road.business.model.RoadManagerPea
import com.navis.road.business.model.TransactionPlacard
import com.navis.road.business.model.Truck
import com.navis.road.business.model.TruckDriver
import com.navis.road.business.model.TruckTransaction
import com.navis.road.business.model.TruckVisitDetails
import com.navis.road.business.model.TruckingCompany
import com.navis.road.business.workflow.TransactionAndVisitHolder
import com.navis.road.portal.GateApiConstants
import org.apache.commons.lang.StringUtils
import org.apache.log4j.Logger
import org.jdom.Element
import org.jdom.output.XMLOutputter


/**
 * Created by psethuraman on 5/11/2017.
 */
class RecordScanHandlerInterceptor implements EGateApiHandlerInterceptor, GateApiConstants {
    private static final Logger LOGGER = Logger.getLogger(RecordScanHandlerInterceptor.class.getName());
    IGateApiHandler _builtInApiHandler;
    static String INSTAGE = "ingateauto";
    static String OUTSTAGE = "gateauto";

    public void execute(UserContext inUserContext, MessageCollector inMessageCollector,
                        Element inEAction, Element inOutEResult, List<Element> inOutAdditionalResponses,
                        Long inWslogGkey) throws BizViolation {

        XMLOutputter outputter=new XMLOutputter();
        String inXmlString = outputter.outputString(inEAction);
        LOGGER.warn("Inbound Message is :"+inXmlString);

        LOGGER.warn("RecordScan WS API Intercepted TV Visit: "+inEAction.getName());

        /**************************************
         * PARSING RECROD SCAN XML ATTRIBUTES
         **************************************/
        Element eTruck = inEAction.getChild(TRUCK);
        Element eTruckVisit = inEAction.getChild(TRUCK_VISIT);
        String truckCommodity = eTruckVisit != null ? eTruckVisit.getAttributeValue(TV_FLEX_STRING01, eTruck.getNamespace()) : "";
        LOGGER.warn("WS API Intercepted Truck Commodity: "+truckCommodity);

        String consoleId = inEAction.getChild(EXTERNAL_CONSOLE_ID).getText();
        LOGGER.warn("Console ID : "+consoleId);
        String tranEqoNbr = eTruckVisit != null ? eTruckVisit.getAttributeValue(TV_FLEX_STRING02, eTruck.getNamespace()) : "";
        LOGGER.warn("WS API Intercepted Tran BOOKING: "+tranEqoNbr);
        String rfid = eTruck != null ? eTruck.getAttributeValue(TRUCK_TAG_ID, eTruck.getNamespace()) : "";
        LOGGER.warn("WS API Intercepted Truck tag-id: "+rfid);
        String truckLicense = eTruck != null ? eTruck.getAttributeValue(TRUCK_LICENSE_NBR, eTruck.getNamespace()) : "";
        LOGGER.warn("WS API Intercepted Truck License: "+truckLicense);
        String trkcId = eTruck != null ? eTruck.getAttributeValue(TT_TRUCKING_CO_ID, eTruck.getNamespace()) : "";

        String gateId = inEAction.getChild(GATE_ID).getText();
        String stageId = inEAction.getChild(STAGE_ID).getText();
        String laneId = inEAction.getChild(LANE_ID).getText();
        LOGGER.warn("Posted gate ID : "+gateId);
        Element eEquip = inEAction.getChild(EQUIPMENT);
        Element eContainer = eEquip != null ? eEquip.getChild(CONTAINER) : null;
        Element eChassis = eEquip != null ? eEquip.getChild(CHASSIS) : null;
        Element eAccessory = eContainer!= null ? eContainer.getChild(ACCESSORY) : eChassis != null ? eChassis.getChild(ACCESSORY) : null;
        Element eOog = eContainer!= null ? eContainer.getChild(CTR_IS_OOG) : null;
        Element ePlacards = eContainer!= null ? eContainer.getChild(TT_CTR_PLACARDS) : null;
        List <Element>ePlacardList = ePlacards != null ? ePlacards.getChildren(TT_CTR_PLACARD) : new ArrayList<Element>();

        String ctrId = eContainer != null ? eContainer.getAttributeValue(EQO_CTR_NBR, inEAction.getNamespace()) : "";
        Container ctr = null ;
        LOGGER.warn("Container found : "+ctrId);
        String seal1 = eContainer != null ? eContainer.getAttributeValue(SEAL_1, inEAction.getNamespace()) : "";
        String seal2 = eContainer != null ? eContainer.getAttributeValue(SEAL_2, inEAction.getNamespace()) : "";
        LOGGER.warn("Seal -1 : "+seal1);
        LOGGER.warn("Seal -2 : "+seal2);
        String tranType = eContainer != null ? eContainer.getAttributeValue(TRAN_TYPE, inEAction.getNamespace()) : "";
        LOGGER.warn("Tran Type : "+tranType);
        TranSubTypeEnum subTypeEnum = TranSubTypeEnum.getEnum(tranType);
        LOGGER.warn("Tran SUB Type : "+subTypeEnum);
        String chassis = eChassis != null ? eChassis.getAttributeValue(EQO_CTR_NBR, inEAction.getNamespace()) : "";
        LOGGER.warn("chassis found : "+chassis);
        if ((chassis == null || StringUtils.isBlank(chassis)) && eContainer != null) {
            chassis = eContainer.getAttributeValue(CTR_ON_CHASSIS_ID, inEAction.getNamespace());
        }
        if (inEAction.getChild(LANE_ID) != null) {
            laneId = inEAction.getChild(LANE_ID).getText();
            if (laneId != null) {
                inEAction.getChild(LANE_ID).setText(laneId.toUpperCase());
            }
        }

        LOGGER.warn("chassis found : "+chassis);
        String accessory = eAccessory != null ? eAccessory.getAttributeValue(EQO_CTR_NBR, inEAction.getNamespace()) : "";
        LOGGER.warn("accessory found : "+accessory);
        String scanSetId = inEAction != null ? inEAction.getAttributeValue(SCAN_SET_ID) : null;
        boolean isOog = eOog != null ? eOog.getAttributeValue(IS_OOG) : null;
        String yardRow = eContainer != null ? eContainer.getAttributeValue(UNIT_FLEX_STRING_3, inEAction.getNamespace()) : "";
        String grossWt = eContainer != null ? eContainer.getAttributeValue(GROSS_WEIGHT, inEAction.getNamespace()) : "";
        String ecc = eContainer != null ? eContainer.getAttributeValue(UFV_FLEX_STRING_8, inEAction.getNamespace()) : "";
        String ucc = eContainer != null ? eContainer.getAttributeValue(UNIT_FLEX_STRING_15, inEAction.getNamespace()) : "";
        String gateNotes = eContainer != null ? eContainer.getAttributeValue(UNIT_FLEX_STRING_8, inEAction.getNamespace()) : "";
        LOGGER.warn("gateNotes found : "+gateNotes);
        String blips = eContainer != null ? eContainer.getAttributeValue(UNIT_FLEX_STRING_12, inEAction.getNamespace()) : "";

        EquipmentOrder tranEqo = null;

        Truck truck = null;
        String truckId = null;
        Long trkcGkey = null;
        String trkId = null;
        String trkCoId = null;

        TruckingCompany trkc = null;
        TruckDriver driver = null;
        String driverGkey = null;
        String driverName = null;

        trkCoId = eTruck != null ? eTruck.getAttributeValue(TT_TRUCKING_CO_ID, eTruck.getNamespace()) : "";

        /***************************************
         * RESOLVE TRUCK, DRIVER TRUCKING COMPANY FROM THE DETAILS RECEIVED FROM RECORD-SCAN
         ***************************************/
        CarinaPersistenceCallback findTruckCB = new CarinaPersistenceCallback() {
            protected void doInTransaction() {
                if (rfid != null && truckLicense != null) {
                    truck = Truck.findOrCreate(truckLicense, truckLicense, null, rfid, false);
                } else if (rfid != null ) {
                    truck = Truck.findTruckByTagId(rfid);
                } else if (truckLicense != null) {
                    truck = Truck.findTruckByLicNbr(truckLicense);
                }
                if (truck != null) {
                    inEAction.getChild(TRUCK).setAttribute(TRUCK_LICENSE_NBR, truck.getTruckLicenseNbr());
                    truckLicense = truck.getTruckLicenseNbr();
                    trkc = truck.getTruckTrkCo();
                    trkId = truck.getTruckId();
                    driver = truck.getTruckDriver();
                    if (driver != null) {
                        driverGkey = driver.getDriverGkey();
                        driverName = driver.getDriverName();
                    }
                }
                if (trkc != null && StringUtils.isBlank(trkCoId)) {
                    trkCoId = trkc.getBzuId();
                }
                if (!StringUtils.isBlank(trkCoId)){
                    trkc = TruckingCompany.findTruckingCompany(trkCoId);
                }
                LOGGER.warn("Trucking Co ID : "+trkCoId);
                if (trkCoId != null && truck != null) {
                    truck.setTruckTrkCo(trkc);
                    inEAction.getChild(TRUCK).setAttribute(TT_TRUCKING_CO_ID, trkCoId);
                }
                if (trkCoId != null) {
                    inEAction.getChild(TRUCK).setAttribute(TT_TRUCKING_CO_ID, trkCoId);
                }

                HibernateApi.getInstance().flush();
            }
        };
        inMessageCollector = (new PersistenceTemplatePropagationRequired(inUserContext)).invoke(findTruckCB);

        Boolean isDeliveryTran = Boolean.FALSE;
        /*********************************************************************************************
         * CREATE TRANSACTION ONLY FOR RE, RM, RC, RI. IF ANY FAILED DELIVERY TRANSACTION, RECORD-SCAN
         * MESSAGE WILL BE SENT. THEY ALREADY HAVE INGATE STAGE TRANSACTION WHEN PROCESS-TRUCK MESSAGE
         * GOT PROCESSED.
         ********************************************************************************************/
        CarinaPersistenceCallback inStageTranCB = new CarinaPersistenceCallback() {
            protected void doInTransaction() {

                try {
                    LOGGER.warn("scanSetId : "+scanSetId);
                    RoadManager roadMgr = (RoadManager)Roastery.getBean("roadManager");
                    String equipmentId = null;
                    if (StringUtils.isNotBlank(ctrId)) {
                        equipmentId = ctrId;
                    } else if (chassis != null && StringUtils.isBlank(ctrId)) {
                        chassis = Chassis.findFullIdOrPadCheckDigit(chassis);
                        LOGGER.warn("Full Chassis : "+chassis);
                        equipmentId = chassis;
                    }
                    Unit activeUnit = findActiveUnitToDeliver(equipmentId);
                    LOGGER.warn("Active unit found : "+activeUnit);
                    if (activeUnit != null) {
                        //CHECKING IF CHASSIS is IN YARD, BUT ABOUT TO RECEIVE
                        if (deriveInOrOutLane(laneId).equalsIgnoreCase("INLANE")) {
                            //means, trying to receive a container or chassis, which is already active in yard
                            LOGGER.warn("TRYING TO GATE IN A CONTAINER/CHASSIS WHICH IS ACTIVE IN YARD");
                        } else {
                            isDeliveryTran = Boolean.TRUE;
                            String delTranType = getUnitTranType(activeUnit, activeUnit.getPrimaryEq().getEqClass().getName(), "LANE 4");
                            subTypeEnum = TranSubTypeEnum.getEnum(delTranType);
                        }
                        //TODO IF THERE IS ANYTHING SPECIFIC TO DRAY-OUT
                        if (TranSubTypeEnum.DE.equals(subTypeEnum)) {

                        }
                    }

                    tranEqo = getBookingBumber(tranEqoNbr);

                    if (subTypeEnum != null) {
                        if (!isDelivery(subTypeEnum)) {
                            subTypeEnum = findReceiveTranTypeFromBooking(tranEqo, ctrId, chassis, tranEqoNbr, subTypeEnum);
                        } else {
                            isDeliveryTran = Boolean.TRUE;
                        }
                    } else if (!isDeliveryTran) {
                        subTypeEnum = findReceiveTranTypeFromBooking(tranEqo, ctrId, chassis, tranEqoNbr, subTypeEnum);
                    }
                    Equipment tranEquipment = Equipment.findEquipment(equipmentId);
                    LOGGER.warn("In Tran Sub Type : " + subTypeEnum);

                    if (subTypeEnum == null && tranType != null) {
                        subTypeEnum = TranSubTypeEnum.getEnum(tranType);
                    }
                    //@TODO reverify below scenario
                    // FIX FOR CHASSIS TRAN, IF CHASSIS IS AT OUTGATE, BUT SYSTEM SHOWS IT IS ALREADY OUTGATED.
                    //BY DEFAULT IT IS RC, WE SHOULD FIX THAT AS DC. should be only for chassis
                    if (StringUtils.isBlank(tranType) && (subTypeEnum == null || TranSubTypeEnum.RC.equals(subTypeEnum))
                            && StringUtils.isBlank(ctrId) && StringUtils.isNotBlank(chassis) && deriveInOrOutLane(laneId).equalsIgnoreCase("OUTLANE")
                            && EquipClassEnum.CHASSIS.getName().equalsIgnoreCase(tranEquipment.getEqClass().getName())) {
                        tranType = "DC";
                        subTypeEnum = TranSubTypeEnum.getEnum(tranType);
                    }

                    LOGGER.warn("In Tran Sub Type defaulting from Nascent : " + subTypeEnum);

                    if (scanSetId != null) {
                        LOGGER.warn(" stage ID : "+inEAction.getChild(STAGE_ID).getText());

                        TruckVisitDetails tvdtls = null;
                        if ((StringUtils.isBlank(tranType) && !isDeliveryTran) || TranSubTypeEnum.RE.equals(subTypeEnum)
                                || TranSubTypeEnum.RM.equals(subTypeEnum) || TranSubTypeEnum.RC.equals(subTypeEnum)) {

                            tvdtls = roadMgr.createTruckVisit(gateId, INSTAGE, null, laneId, null, trkId, rfid, truckLicense,
                                    trkCoId, trkcGkey, null, null, null, null, new FieldChanges(), GateClientTypeEnum.AUTOGATE, null);

                            LOGGER.warn("TVDTLS : "+tvdtls);
                            //LOGGER.warn("Truck Visit got created at : "+tvdtls.getStageStats("ingateauto"));
                            tvdtls.setNextStageId("gateauto");
                            /*if (driver != null) {
                                LOGGER.warn("Setting Driver : "+driver.getDriverName());
                                tvdtls.setTvdtlsDriver(driver);
                            }*/
                            LOGGER.warn("Creating Tran");
                            TruckTransaction tran = TruckTransaction.create(tvdtls, subTypeEnum);
                            if (TranSubTypeEnum.RE.equals(subTypeEnum)) {
                                LOGGER.warn("Updating Freight Kind : "+FreightKindEnum.FCL);
                                tran.setTranCtrFreightKind(FreightKindEnum.FCL);
                            } else if (TranSubTypeEnum.RM.equals(subTypeEnum)) {
                                LOGGER.warn("Updating Freight Kind : "+FreightKindEnum.MTY);
                                tran.setTranCtrFreightKind(FreightKindEnum.MTY);
                            }
                            LOGGER.warn("Created Tran : "+tran);
                            tran.setTranStageId("ingateauto");
                            tran.setTranNextStageId("gateauto");
                            LOGGER.warn("Setting stages for Tran : "+tran.getTranStageId());

                            FieldChanges inTranChanges = new FieldChanges();
                            FieldChanges inNonTranChanges = new FieldChanges();

                            if (!"DC".equalsIgnoreCase(tranType) && ctrId != null) {
                                inTranChanges.setFieldChange(RoadField.TRAN_CTR_NBR, ctrId);
                                ctr = Container.findContainer(ctrId);
                                if (ctr != null) {
                                    tran.setTranCtrTypeId(ctr.getEqEquipType().getEqtypId());
                                    inTranChanges.setFieldChange(RoadField.TRAN_CTR_TYPE_ID, ctr.getEqEquipType().getEqtypId());
                                    LOGGER.warn("trying to set ctr type : "+ctr.getEqEquipType().getEqtypId());
                                    tran.setTranCtrOperator(ctr.getEquipmentOperator());
                                    tran.setTranCtrOwner(ctr.getEquipmentOwner());
                                    if (ctr.getEquipmentOwner() != null) {
                                        inTranChanges.setFieldChange(RoadField.TRAN_CTR_OWNER_ID, ctr.getEquipmentOwner().getBzuId());
                                        inTranChanges.setFieldChange(RoadField.TRAN_CTR_OWNER, ctr.getEquipmentOwner());
                                    }
                                }
                            }
                            Chassis chs = Chassis.findChassis(Chassis.findFullIdOrPadCheckDigit(chassis));
                            LOGGER.warn("setting chassis at ingate : "+chs);
                            chassis = chs != null ? chs.getEqIdFull() : chassis;
                            LOGGER.warn("Found chassis full ID : "+chassis);
                            if (StringUtils.isNotBlank(chassis)) {
                                inTranChanges.setFieldChange(RoadField.TRAN_CHS_NBR, chassis);
                            }
                            if (chs != null) {
                                inTranChanges.setFieldChange(RoadField.TRAN_CHS_TYPE_ID, chs.getEqEquipType().getEqtypId());
                                inTranChanges.setFieldChange(RoadField.TRAN_CHS_OPERATOR, chs.getEquipmentOperator());
//                                tran.setTranChsTypeId(chs.getEqEquipType().getEqtypId());
                            }
                            Accessory acc = findAccessoryFullIdOrPadCheckDigit(accessory)
                            if (acc != null) {
                                accessory = acc.getEqIdFull();
                                tran.setTranCtrAccessory(acc);
                            }
                            if (StringUtils.isNotBlank(accessory)) {
                                tran.setTranCtrAccNbr(accessory);
                            }
                            if (StringUtils.isNotBlank(seal1)) {
                                tran.setTranSealNbr1(seal1);
                            }
                            if (StringUtils.isNotBlank(seal2)) {
                                tran.setTranSealNbr2(seal2);
                            }
                            RoadManagerPea roadManagerPea = (RoadManagerPea) Roastery.getBean(RoadManager.BEAN_ID);
                            TransactionAndVisitHolder wfCtx = roadManagerPea.submitTransaction(tran, inTranChanges, inNonTranChanges, tvdtls.getTvdtlsGate(), GateClientTypeEnum.AUTOGATE, null, null);
                            TruckTransaction submittedTran = wfCtx.getTran();
                            LOGGER.warn("Submitted gate stage : "+submittedTran.getTranStageId());
                            LOGGER.warn("Submitted gate next stage: "+submittedTran.getTranNextStageId());
                            wfCtx = roadMgr.submitStageDone(tvdtls,tvdtls.getTvdtlsGate(),INSTAGE, null, Boolean.TRUE, GateClientTypeEnum.AUTOGATE,null,null);
                            LOGGER.warn("Submitted gate stage 1: "+submittedTran.getTranStageId());
                            LOGGER.warn("Submitted gate next stage 2: "+submittedTran.getTranNextStageId());
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("ERROR : Transaction not created : "+e.getMessage());
                    BizViolation.create(BizFailure.create(e.getLocalizedMessage()));
                }
            }
        };

        MessageCollector inStageTranCall = (new PersistenceTemplatePropagationRequired(inUserContext)).invoke(inStageTranCB);

        /*********************************************************************************************
         * APPEND ALL SCANNED DATA INTO SUBMITTED TRANSACTION TO POPULATE IN OUT-STAGE.
         ********************************************************************************************/
        Boolean outgateSuccess = Boolean.FALSE;
        LOGGER.warn("Started outgate transaction : ");
        CarinaPersistenceCallback outStageTranCB = new CarinaPersistenceCallback() {
            protected void doInTransaction() {
                RoadManager roadMgr = (RoadManager)Roastery.getBean("roadManager");
                try {
                    if (tranEqo == null) {
                        tranEqo = getBookingBumber(tranEqoNbr);
                    }

                    if (subTypeEnum != null || tranType != null) {
                        if (!isDelivery(subTypeEnum)) {
                            subTypeEnum = findReceiveTranTypeFromBooking(tranEqo, ctrId, chassis, tranEqoNbr, subTypeEnum);
                        } else {
                            isDeliveryTran = Boolean.TRUE;
                        }
                    }
                    String equipmentId = null;
                    if (ctrId != null) {
                        equipmentId = ctrId;
                    } else if (chassis != null && ctrId == null) {
                        equipmentId = chassis;
                    }
                    Unit delUnit = findActiveUnitToDeliver(equipmentId);
                    if (delUnit != null){
                        isDeliveryTran = Boolean.TRUE;
                    }
                    LOGGER.warn("Out Tran Sub Type : " + subTypeEnum);

                    if (subTypeEnum == null && tranType != null) {
                        subTypeEnum = TranSubTypeEnum.getEnum(tranType);
                        LOGGER.warn("Out Tran Sub Type defaulting from Nascent : " + subTypeEnum);
                    }

                    if (isDeliveryTran) {
                        TruckVisitDetails delTvdtls = TruckVisitDetails.findMostRecentTruckVisitByField(RoadField.TVDTLS_TRUCK_AEI_TAG_ID, rfid, TruckVisitStatusGroupEnum.IN_PROGRESS);
                        LOGGER.warn("Delivery Truck Visit found at : " + delTvdtls);
                        if (delTvdtls != null) {
                            LOGGER.warn("Delivery Truck Visit found with Gkey : " + delTvdtls.getCvdGkey());
                            delTvdtls = (TruckVisitDetails)HibernateApi.getInstance().load(TruckVisitDetails.class, delTvdtls.getCvdGkey());
                            LOGGER.warn("Delivery Truck Visit retreived at : " + delTvdtls);

                            LOGGER.warn("Delivery Truck Visit found with tran : " + delTvdtls.getActiveTransactions());
                            if (isDelivery(subTypeEnum)) {
                                LOGGER.warn("Delivery Tran Sub Type1 : " + subTypeEnum);
                                TruckTransaction tran = null;
                                if (delTvdtls != null && delTvdtls.getActiveTransactions() != null) {
                                    if (delTvdtls.getActiveTransactions().iterator() != null && delTvdtls.getActiveTransactions().iterator().hasNext()) {
                                        tran = delTvdtls.getActiveTransactions().iterator().next();
                                        LOGGER.warn("Delivery Tran found with status : " + tran.getTranStatus());
                                        if (TranStatusEnum.OK.equals(tran.getTranStatus()) || TranStatusEnum.TROUBLE.equals(tran.getTranStatus())) {
                                            LOGGER.warn("Found existing transaction for outgate : " + tran.getTranCtrNbr());
                                            tran.setTranCtrNbr(ctrId);
                                            tran.setTranContainer(ctr);
                                            Chassis chs = Chassis.findChassis(Chassis.findFullIdOrPadCheckDigit(chassis));
                                            tran.setTranChassis(chs);
                                            if (StringUtils.isNotBlank(tranEqoNbr)) {
                                                tran.setTranEqoNbr(tranEqoNbr);
                                            }
                                            tran.setTranChsNbr(chassis);
                                            if (chs != null) {
                                                LOGGER.warn("Chassis full ID : "+chs.getEqIdFull());
                                                tran.setTranChsNbr(chs.getEqIdFull());
                                            }
                                            Accessory acc = findAccessoryFullIdOrPadCheckDigit(accessory);
                                            if (acc != null) {
                                                accessory = acc.getEqIdFull();
                                                tran.setTranCtrAccessory(acc);
                                            }
                                            tran.setTranCtrAccNbr(accessory);
                                            if (StringUtils.isNotBlank(gateNotes)) {
                                                LOGGER.warn("setting gate notes : " + gateNotes);
                                                tran.setTranFlexString08(gateNotes);
                                            }
                                            if (delUnit != null) {
                                                if (delUnit.getUnitSealNbr1() != null) {
                                                    tran.setTranSealNbr1(delUnit.getUnitSealNbr1());
                                                }
                                                if (delUnit.getUnitGoods() != null) {
                                                    tran.setTranImportReleaseNbr(delUnit.getUnitGoods().getGdsBlNbr());
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else if ((StringUtils.isBlank(tranType) && !isDeliveryTran) || TranSubTypeEnum.RE.equals(subTypeEnum)
                            || TranSubTypeEnum.RM.equals(subTypeEnum) || TranSubTypeEnum.RI.equals(subTypeEnum)) {
//                        if ((tranEqoNbr != null && StringUtils.isNotBlank(tranEqoNbr))) {
                        TruckVisitDetails tvdtls = TruckVisitDetails.findMostRecentTruckVisitByField(RoadField.TVDTLS_TRUCK_AEI_TAG_ID, rfid, TruckVisitStatusGroupEnum.IN_PROGRESS);
                        LOGGER.warn("Truck Visit got created at : " + tvdtls);
                        if ((tvdtls == null) || (tvdtls != null && !tvdtls.isActive())) {
                            //tvdtls = TruckVisitDetails.create(Gate.findGateById(gateId), null,truckLicense, ContextHelper.getThreadFacility());
                            tvdtls = roadMgr.createTruckVisit(gateId, INSTAGE, null, laneId, null, trkId, rfid, truckLicense,
                                    trkCoId, trkcGkey, null, null, null, null, new FieldChanges(), GateClientTypeEnum.AUTOGATE, null);
                            tvdtls.setNextStageId("");
                            LOGGER.warn("Truck Visit got created now : " + tvdtls);
                        }
                        if (tranEqo == null) {
                            tranEqo = getBookingBumber(tranEqoNbr);
                        }
                        if (!isDelivery(subTypeEnum)) {
                            subTypeEnum = findReceiveTranTypeFromBooking(tranEqo, ctrId, chassis, tranEqoNbr, subTypeEnum);
                        }
                        LOGGER.warn("Out Tran Sub Type1 : " + subTypeEnum);
                        if (subTypeEnum == null && tranType != null) {
                            subTypeEnum = TranSubTypeEnum.getEnum(tranType);
                            LOGGER.warn("In case of RI : Out Tran Sub Type defaulting from Nascent : " + subTypeEnum);
                        }
                        TruckTransaction tran = null;
                        if (tvdtls != null && tvdtls.getActiveTransactions() != null) {
                            if (tvdtls.getActiveTransactions().iterator() != null && tvdtls.getActiveTransactions().iterator().hasNext()) {
                                tran = tvdtls.getActiveTransactions().iterator().next();
                                LOGGER.warn("Found existing transaction at ingate for : " + tran.getTranCtrNbr());
                            }
                        }

                        if (subTypeEnum == null) {
                            subTypeEnum = TranSubTypeEnum.UK;
                        }
                        if (tran == null) {
                            tran = TruckTransaction.create(tvdtls, subTypeEnum);
                        }
                        if (tran != null) {
                            tran.setTranEqoNbr(tranEqoNbr);
                            tran.setTranCtrNbr(ctrId);
                            if (TranSubTypeEnum.RE.equals(subTypeEnum)) {
                                LOGGER.warn("Updating Freight Kind : "+FreightKindEnum.FCL);
                                tran.setTranCtrFreightKind(FreightKindEnum.FCL);
                            } else if (TranSubTypeEnum.RM.equals(subTypeEnum)) {
                                LOGGER.warn("Updating Freight Kind : "+FreightKindEnum.MTY);
                                tran.setTranCtrFreightKind(FreightKindEnum.MTY);
                            }
                            if (ctrId != null) {
                                ctr = Container.findContainer(ctrId);
                                if (ctr != null) {
                                    tran.setTranContainer(ctr);

                                    if (ctr != null) {
                                        tran.setTranCtrOperator(ctr.getEquipmentOperator());
                                        LOGGER.warn("Ctr Eq Owner :" + ctr.getEquipmentOwner());
                                        tran.setTranCtrOwner(ctr.getEquipmentOwner());
                                        LOGGER.warn("Ctr Eq Type :" + ctr.getEqEquipType().getEqtypId());
                                        tran.setTranCtrTypeId(ctr.getEqEquipType().getEqtypId());
                                        LOGGER.warn("Ctr Eq Type set through entity :" + tran.getTranCtrTypeId());

                                        tran.setFieldValue(RoadField.TRAN_CTR_TYPE_ID, ctr.getEqEquipType().getEqtypId());
                                        LOGGER.warn("Ctr Eq Type set through field :" + tran.getTranCtrTypeId());

                                        LOGGER.warn("trying to set ctr type : "+ctr.getEqEquipType().getEqtypId());
                                        tran.setTranCtrOperator(ctr.getEquipmentOperator());
                                        tran.setTranCtrOwner(ctr.getEquipmentOwner());
                                        if (ctr.getEquipmentOwner() != null) {
                                            tran.setFieldValue(RoadField.TRAN_CTR_OWNER_ID, ctr.getEquipmentOwner().getBzuId());
                                            tran.setFieldValue(RoadField.TRAN_CTR_OWNER, ctr.getEquipmentOwner());
                                        }

                                    }
                                }
                                tran.setTranPlacards(new HashSet());
                                if (ePlacardList != null && ePlacardList.size() > 0) {
                                    LOGGER.warn("Placards list : "+ ePlacardList.toString());
                                    HashSet placardSet = new HashSet();
                                    for (Element placardElemet : ePlacardList) {
                                        String placardName = placardElemet.getAttributeValue(TT_CTR_PLACARD_TEXT);
                                        if(placardName != null) {
                                            LOGGER.warn("received placard : "+placardName);
                                            String mappedPlacards = getPlacardValueFromMap(placardName);
                                            LOGGER.warn("mapped placard : "+mappedPlacards);
                                            if (StringUtils.isNotBlank(mappedPlacards)) {
                                                Placard placard = Placard.findPlacard(mappedPlacards);
                                                LOGGER.warn("derived TOS placard : " + placard);
                                                TransactionPlacard tranPlacard = TransactionPlacard.create(tran, placard);
                                                if (tranPlacard != null) {
                                                    tran.getTranPlacards().add(tranPlacard);
                                                }
                                            }
                                        }
                                    }
                                    LOGGER.warn("Adding Placards to Tran : "+tran.getTranPlacards().size());
                                }
                            }
                            tran.setTranChsNbr(chassis);
                            tran.setTranChsNbrAssigned(chassis);
                            LOGGER.warn("Chassis : " + chassis);
                            if (chassis != null) {
                                Chassis chs = Chassis.findChassis(Chassis.findFullIdOrPadCheckDigit(chassis));
                                if (chs != null) {
                                    tran.setTranChassis(chs);
                                    chassis = chs.getEqIdFull();
                                    tran.setTranChsNbr(chassis);
                                    /*LOGGER.warn("Chs Eq Type :" + chs.getEqEquipType().getEqtypId());
                                    tran.setTranChsTypeId(chs.getEqEquipType().getEqtypId());*/
                                }
                            }
                            tran.setTranStageId(stageId);
                            tran.setTranCtrAccNbr(accessory);
                            LOGGER.warn("Accessory : " + accessory);
                            if (accessory != null) {
                                Accessory acc = findAccessoryFullIdOrPadCheckDigit(accessory);
                                if (acc != null) {
                                    tran.setTranCtrAccessory(acc);
                                    tran.setTranCtrAccNbr(acc.getEqIdFull());
                                }
                            }
                            LOGGER.warn("Tran Placards : "+tran.getTranPlacards());

                            if (tranEqo != null) {
                                LOGGER.warn("got booking : " + tranEqo.getEqboNbr());
                                tran.setTranEqo(tranEqo);
                                LOGGER.warn("got booking gkey : " + tranEqo.getEqboGkey());
                                LOGGER.warn("got booking Dest : " + tranEqo.getEqoDestination());
                                EquipmentOrder tranEqo1 = (EquipmentOrder) HibernateApi.getInstance().load(EquipmentOrder.class, tranEqo.getEqboGkey());
//                                    EquipmentOrder order = EquipmentOrder.resolveEqoFromEqbo(tranEqo);
                                /*Thread.sleep(100);
                                CarrierVisit cv = tranEqo.getEqoVesselVisit();
                                Thread.sleep(100);
                                LOGGER.warn("Booking VV : " + cv);
                                String cvId = cv.getCvId();
                                LOGGER.warn("Visit ID : "+cvId);*/

                                //tran.setTranCarrierVisit(cv);
                                LOGGER.warn("Booking POD 1: ");
                                tran.setTranDischargePoint1(tranEqo1.getEqoPod1());
                                LOGGER.warn("Booking POD 2: ");
                                LOGGER.warn("Booking POD 4: ");
                                LOGGER.warn("Booking consignee: "+tranEqo1.getEqoConsignee());
                                tran.setTranConsignee(tranEqo1.getEqoConsignee() != null ? tranEqo1.getEqoConsignee().getBzuName() : "");
                                LOGGER.warn("Booking Shipper: "+tranEqo1.getEqoShipper());
                                tran.setTranShipper(tranEqo1.getEqoShipper() != null ? tranEqo1.getEqoShipper().getBzuName() : "");
                                LOGGER.warn("Booking VV : " + tranEqo1.getEqoVesselVisit());
                                tran.setTranCarrierVisit(tranEqo1.getEqoVesselVisit());
                                //@TODO ECC setting is in UFV Flex String 08 - not in inspection - pending assignment
                                tran.setTranNotes(tranEqo1.getEqoNotes());
                            }

                            LOGGER.warn("yard row : " + yardRow + "/gross wt : " + grossWt + "/ecc : " + ecc + "/ucc : " + ucc + "/gate Notes : " + gateNotes + "/blips :" + blips);
                            tran.setTranFlexString03(yardRow);
                            tran.setTranFlexString01(ecc);
                            tran.setTranFlexString02(ucc);
                            tran.setTranFlexString08(gateNotes);
                            tran.setTranUnitFlexString12(blips);
                            tran.setTranIsOog(isOog)
                            LOGGER.warn("BLIPS : " + blips);

                            //Populate properties for DRAY IN
                            if (TranSubTypeEnum.RI.equals(subTypeEnum)) {
                                Unit drayUnit = findActiveUnitToReceive(ctrId);
                                if (drayUnit != null) {
                                    tran.setTranDischargePoint1(drayUnit.getUnitRouting().getRtgPOD1());
                                    if (drayUnit.getUnitActiveUfvNowActive() != null) {
                                        tran.setTranCarrierVisit(drayUnit.getUnitActiveUfvNowActive().getUfvActualObCv());
                                    }
                                    tran.setTranUnitFlexString09(drayUnit.getUnitFlexString09()); // Booking Num
                                    LOGGER.warn("Dray IN unit Booking Nbr : "+drayUnit.getUnitFlexString09());
                                    tran.setTranUnitFlexString15(drayUnit.getUnitFlexString15()); //UCC
                                    tran.setTranFlexString02(drayUnit.getUnitFlexString15()); //UCC
                                    LOGGER.warn("Dray IN unit seal Nbr : "+drayUnit.getUnitSealNbr1());  //SEAL1

                                    if (StringUtils.isNotBlank(seal1)) {
                                        tran.setTranSealNbr1(seal1);
                                    } else {
                                        tran.setTranSealNbr1(drayUnit.getUnitSealNbr1());
                                    }
                                    if (StringUtils.isNotBlank(seal2)) {
                                        tran.setTranSealNbr2(seal2);
                                    } else {
                                        tran.setTranSealNbr2(drayUnit.getUnitSealNbr2()); //SEAL_TYPE
                                    }
                                }
                            } else {
                                if (StringUtils.isNotBlank(seal1)) {
                                    tran.setTranSealNbr1(seal1);
                                }
                                if (StringUtils.isNotBlank(seal2)) {
                                    tran.setTranSealNbr2(seal2);
                                }
                            }

                            if (truckCommodity != null) {
                                tran.setTranUnitFlexString05(truckCommodity);
                            }

                            if (grossWt != null && StringUtils.isNotBlank(grossWt)) {
                                Double netWtlb = new Double(grossWt);
                                LOGGER.warn("Container Net Weight : " + netWtlb);
                                Double netWtKg = netWtlb / 2.20462;
                                tran.setTranCtrNetWeight(netWtKg);
                            }
                            tran.setTranFacility(ContextHelper.getThreadFacility());
                            tran.setTranFlexString05(truckCommodity);

                            FieldChanges inTranChanges = new FieldChanges();
                            FieldChanges inNonTranChanges = new FieldChanges();

                            if (!TranSubTypeEnum.DC.equals(subTypeEnum) || !TranSubTypeEnum.RC.equals(subTypeEnum)) {
                                LOGGER.warn("NOT DC Transaction : " + subTypeEnum);
                                tran.setTranCtrNbr(ctrId);
                                tran.setTranContainer(ctr);
                            } else {
                                LOGGER.warn("DC Transaction : " + subTypeEnum);
                                tran.setTranCtrNbr("");
                                tran.setTranContainer(null);
                            }
                            LOGGER.warn("Transaction created at outgate : " + tran.getTranStageId());
                        }
                        outgateSuccess = Boolean.TRUE;
//                        }
                        LOGGER.warn("Setting Driver : ");
                        if (tvdtls != null && driverName != null) {
//                            TruckDriver tDriver = TruckDriver.getDr
                            tvdtls.setTvdtlsDriverName(driverName);
                        }
                    }
                } catch (Exception e) {
                    LOGGER.warn("ERROR WHILE CREATING OUTSTAGE TRANSACTION : "+e.getMessage());

                }
            }
        };

        MessageCollector outStageTranCall = (new PersistenceTemplatePropagationRequired(inUserContext)).invoke(outStageTranCB);
        LOGGER.warn("Transaction created at outgate final : ");
        //if (tranEqoNbr != null || StringUtils.isNotBlank(tranEqoNbr)) {
        if (inEAction != null) {
            inEAction.removeChild(EQUIPMENT);
            LOGGER.warn("Equipment tag removed from scanned data : " + inEAction.toString());
        }

        //}

        //Thread.sleep(1000);
        LOGGER.warn("WS API Intercepted calling default service after update insp trucker commodity: "+truckCommodity);
        //if (!isDeliveryTran) {
        getBuiltinApiHandler().executeInternal(
                inUserContext,
                inMessageCollector,
                inEAction,
                inOutEResult,
                inOutAdditionalResponses,
                inWslogGkey
        );
        //}
        LOGGER.warn("WS API Intercepted calling : END ");
    }

    private EquipmentOrder getBookingBumber(String tranEqoNbr) {
        EquipmentOrder tranEqo = null;
        if (tranEqoNbr != null && StringUtils.isNotBlank(tranEqoNbr)) {
            OrdersFinder ordersFinder = (OrdersFinder) Roastery.getBean(OrdersFinder.BEAN_ID);
            List<EquipmentOrder> eqOrderList = ordersFinder.findEquipmentOrderByNbr(tranEqoNbr);
            LOGGER.warn("Booking find : " + eqOrderList != null ? eqOrderList.size() : "0");
            if (eqOrderList != null && eqOrderList.size() == 1) {
                tranEqo = eqOrderList.get(0);
            }
        }
        return tranEqo;
    }

    private TranSubTypeEnum findReceiveTranTypeFromBooking(EquipmentOrder tranEqo, String ctrId, String chassis, String inEqoNbr, TranSubTypeEnum defaultSubType) {
        TranSubTypeEnum tranSubType = null;

        if (tranEqo != null) {

            if (ctrId == null && chassis != null && tranEqoNbr == null) {
                tranSubType = TranSubTypeEnum.RC;
            }
            if (findActiveUnitToReceive(ctrId)) {
                tranSubType = TranSubTypeEnum.RI;
            }
            //TODO - Check with Prakash why subTypeEnum is referred here
            if (tranSubType == null) {
                tranSubType = defaultSubType;
            }
            if (tranSubType == null) {
                if (FreightKindEnum.FCL.equals(tranEqo.getEqoEqStatus())) {
                    tranSubType = TranSubTypeEnum.RE;
                } else {
                    tranSubType = TranSubTypeEnum.RM;
                }
            }

            LOGGER.warn("Tran Sub Type : " + tranSubType);
        } else {
            LOGGER.warn("No Booking, should be RM or RC : "+ctrId + "/"+chassis);
            if (findActiveUnitToReceive(ctrId)) {
                LOGGER.warn("derived : RI");
                tranSubType = TranSubTypeEnum.RI;
            } else if (StringUtils.isNotEmpty(ctrId) && StringUtils.isNotBlank(ctrId) && StringUtils.isNotEmpty(inEqoNbr)) {
                LOGGER.warn("derived : RE/RM from Nascent");
                tranSubType = defaultSubType;
            } else if (StringUtils.isNotEmpty(ctrId) && StringUtils.isNotBlank(ctrId) && StringUtils.isBlank(inEqoNbr)) {
                LOGGER.warn("derived : RM");
                tranSubType = TranSubTypeEnum.RM;
            } else if ((StringUtils.isEmpty(ctrId) || StringUtils.isBlank(ctrId)) && StringUtils.isNotBlank(chassis)) {
                LOGGER.warn("derived : RC");
                tranSubType = TranSubTypeEnum.RC;
            } else {
                LOGGER.warn("else derived : RM");
                tranSubType = TranSubTypeEnum.RM;
            }
        }
        LOGGER.warn("Derived tran type is : "+tranSubType);
        return  tranSubType;
    }

    private Unit findActiveUnitToReceive(String equipmentId) {
        Long facility = ContextHelper.getThreadFacilityKey();
        if (equipmentId == null || facility == null) {
            return null;
        }
        Unit activeUnit = null;
        try {
            UnitManager unitManager = (UnitManager) Roastery.getBean(UnitManager.BEAN_ID);
            UnitFacilityVisit activeUfv = unitManager.findActiveUfvForUnitDigits(equipmentId);

            if (facility == activeUfv.getUfvFacility().getFcyGkey() && UfvTransitStateEnum.S20_INBOUND.equals(activeUfv.getUfvTransitState())) {
                activeUnit = activeUfv.getUfvUnit();
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            LOGGER.error("Requested unit from GATE OCR not found in ANK YARD");
        }
        //Unit unit= ufv.getUfvUnit();
        //UnitFacilityVisit activeUfv = unit.getUnitActiveUfvNowActive();


        return activeUnit;
    }

    private Boolean isDelivery(TranSubTypeEnum tranType) {
        Boolean isDelivery = Boolean.FALSE;
        if (TranSubTypeEnum.DE.equals(tranType) || TranSubTypeEnum.DI.equals(tranType) || TranSubTypeEnum.DM.equals(tranType) ||
                TranSubTypeEnum.DC.equals(tranType)) {
            isDelivery = Boolean.TRUE;
        }
        return isDelivery;
    }

    private Unit findActiveUnitToDeliver(String equipmentId) {
        Long facility = ContextHelper.getThreadFacilityKey();
        if (equipmentId == null || facility == null) {
            return null;
        }
        Unit activeUnit = null;
        try {
            UnitManager unitManager = (UnitManager) Roastery.getBean(UnitManager.BEAN_ID);
            UnitFacilityVisit activeUfv = unitManager.findActiveUfvForUnitDigits(equipmentId);

            if (activeUfv != null && facility == activeUfv.getUfvFacility().getFcyGkey()) {
                if (UfvTransitStateEnum.S40_YARD.equals(activeUfv.getUfvTransitState())) {
                    activeUnit = activeUfv.getUfvUnit();
                }
            }
        } catch (Exception e) {
            LOGGER.error("Requested unit from GATE OCR not found in ANK YARD : "+e.getMessage());
        }

        return activeUnit;
    }

    private static String deriveInOrOutLane(String laneId) {
        List inLanes = new ArrayList(20);
        List outLanes = new ArrayList(20);
        String[] lanesIn   = ["LANE 1", "LANE 2", "LANE 50", "LANE 51", "LANE 52", "LANE 53", "LANE 54", "LANE 55", "P101"];
        String[] lanesOut   = ["LANE 3", "LANE 4", "LANE 60", "LANE 61", "LANE 62", "LANE 63", "LANE 64", "LANE 65", "P301"];
        String laneType = "INLANE";
        if (laneId != null && !laneId.startsWith("LANE")) {
            laneId = "LANE "+laneId;
        }
        inLanes = Arrays.asList(lanesIn);
        outLanes = Arrays.asList(lanesOut);
        LOGGER.warn("Lane ID : "+laneId);
        LOGGER.warn("InLane contains : "+inLanes.contains(laneId));
        LOGGER.warn("OutLane contains : "+outLanes.contains(laneId));
        if (inLanes.contains(laneId)) {
            laneType = "INLANE";
        } else {
            laneType = "OUTLANE";
        }
        return laneType;
    }

    private String getUnitTranType(Unit activeUnit, String eqClass, String laneId) {
        String tranType = "DI";
        String LANE1 = "LANE 1";
        String LANE2 = "LANE 2";
        String LANE3 = "LANE 3";
        String LANE4 = "LANE 4";
        String INCONSOLE = "P101";
        String OUTCONSOLE = "P301";

        List inLanes = new ArrayList(20);
        List outLanes = new ArrayList(20);
        String[] lanesIn   = ["LANE 1", "LANE 2", "LANE 50", "LANE 51", "LANE 52", "LANE 53", "LANE 54", "LANE 55", "P101"];
        String[] lanesOut   = ["LANE 3", "LANE 4", "LANE 60", "LANE 61", "LANE 62", "LANE 63", "LANE 64", "LANE 65", "P301"];

        if (laneId != null && !laneId.startsWith("LANE")) {
            laneId = "LANE "+laneId;
        }
        inLanes = Arrays.asList(lanesIn);
        outLanes = Arrays.asList(lanesOut);
        LOGGER.warn("Lane ID : "+laneId);
        LOGGER.warn("InLane contains : "+inLanes.contains(laneId));
        LOGGER.warn("OutLane contains : "+outLanes.contains(laneId));
        String category =  activeUnit.getUnitCategory().getName();
        LOGGER.warn("category : "+category);
        if (inLanes.contains(laneId)) {
            if (EquipClassEnum.CONTAINER.getName().equalsIgnoreCase(eqClass) && UnitCategoryEnum.EXPORT.getName().equalsIgnoreCase(category)) {
                tranType = "RE";
            } else if (EquipClassEnum.CONTAINER.getName().equalsIgnoreCase(eqClass) && activeUnit.getUnitDrayStatus() != null) {
                tranType = "RI";
            } else if (EquipClassEnum.CHASSIS.getName().equalsIgnoreCase(eqClass)) {
                tranType = "RC";
            } else if (EquipClassEnum.CONTAINER.getName().equalsIgnoreCase(eqClass) && UnitCategoryEnum.STORAGE.getName().equalsIgnoreCase(category)) {
                tranType = "RM";
            }
        } else if (outLanes.contains(laneId)) {
            if (EquipClassEnum.CONTAINER.getName().equalsIgnoreCase(eqClass) &&
                    (activeUnit.getUnitDrayStatus() != null || UnitCategoryEnum.EXPORT.getName().equalsIgnoreCase(activeUnit.getUnitCategory().getName()))) {
                tranType = "DE";
            } else  if (EquipClassEnum.CONTAINER.getName().equalsIgnoreCase(eqClass) && UnitCategoryEnum.IMPORT.getName().equalsIgnoreCase(category)
                    && FreightKindEnum.FCL.equals(activeUnit.getUnitFreightKind())) {
                tranType = "DI";
            } else if (EquipClassEnum.CONTAINER.getName().equalsIgnoreCase(eqClass) &&
                    (UnitCategoryEnum.STORAGE.getName().equalsIgnoreCase(category) || FreightKindEnum.MTY.equals(activeUnit.getUnitFreightKind())) ) {
                tranType = "DM";
            } else if (EquipClassEnum.CHASSIS.getName().equalsIgnoreCase(eqClass)) {
                tranType = "DC";
            }
        }
        return tranType;
    }

    private static Accessory findAccessoryFullIdOrPadCheckDigit(String inAccId) {
        String accIdFull = inAccId;
        Accessory acc = Accessory.findAccessory(inAccId);
        if(acc == null) {
            if(inAccId.length() == 10) {
                DomainQuery dq = QueryUtils.createDomainQuery("Accessory").addDqPredicate(PredicateFactory.eq(ArgoRefField.EQ_LIFE_CYCLE_STATE, LifeCycleStateEnum.ACTIVE)).addDqPredicate(PredicateFactory.like(ArgoRefField.EQ_ID_FULL, inAccId + "%"));
                List eqs = HibernateApi.getInstance().findEntitiesByDomainQuery(dq);
                if(eqs != null && eqs.size() == 1) {
                    acc = (Accessory)eqs.get(0);
                }
            }
        }
        return acc;
    }
    static String getPlacardValueFromMap(String placardName) {
        return placardMap.get(placardName);
    }

    public void setBuiltInApiHandler(IGateApiHandler inBuiltInApiHandler) {
        this._builtInApiHandler = inBuiltInApiHandler;
    }

    protected IGateApiHandler getBuiltinApiHandler() {
        return this._builtInApiHandler;
    }

    static Map<String, String> placardMap = new HashMap<String, String>();
    static {
        placardMap.put("1", "1 EXPL");
        placardMap.put("1.1", "1.1 EXPL");
        placardMap.put("1.2", "1.2 EXPL");
        placardMap.put("1.3", "1.3 EXPL");
        placardMap.put("1.4", "1.4 EXPL");
        placardMap.put("1.5", "1.5 BLAST");
        placardMap.put("1.6", "1.6 EXPL");
        placardMap.put("2", "2 FLAM");
        placardMap.put("2.1", "2.1 FLAM");
        placardMap.put("2.2", "2.2 NON FLAM");
        placardMap.put("2.3", "2.3 INH HAZ");
        placardMap.put("2.4", "2.4 OXGN");
        //placardMap.put("2.2a", "2 OXGN");
        placardMap.put("3", "3 COMB");
        placardMap.put("3.1", "3.1 FLAM");
        placardMap.put("3.2", "3.2 GAS");
        placardMap.put("3.3", "3.3 COMB");
        placardMap.put("3.4", "3.4 FUEL");
        placardMap.put("4.1", "4/4.1 FLAM SOL");
        placardMap.put("4.2", "4/4.2 COMB");
        placardMap.put("4.3", "4/4.3 DNGR WET");
        placardMap.put("5.1", "5.1 OXDZ");
        placardMap.put("5.2", "5.2 ORG PXDE");
        placardMap.put("6.1", "6/6.1 POIS");
        placardMap.put("6.2", "6/6.1 INH HAZ/TOX");
        placardMap.put("6.3", "6/6.1 PGIII");
        placardMap.put("7", "7 RADIO");
        placardMap.put("8", "8 CORR");
        placardMap.put("9", "9 CLASS");
        placardMap.put("DNGR", "DNGR");
        placardMap.put("LTD QTY", "LTD QTY");
        placardMap.put("MARINE POLL", "MARINE POLL");
        placardMap.put("OPTIONAL", "OPTIONAL");
        placardMap.put("SEE BOL", "SEE BOL");
        placardMap.put("VEHICLE ONLY", "VEHICLE ONLY");
        placardMap.put("WRNG LBL", "WRNG LBL");
    }
}
