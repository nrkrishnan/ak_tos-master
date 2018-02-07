import com.navis.argo.ArgoRefField
import com.navis.argo.ContextHelper
import com.navis.argo.business.atoms.EquipClassEnum
import com.navis.argo.business.atoms.FreightKindEnum
import com.navis.argo.business.atoms.LocTypeEnum
import com.navis.argo.business.atoms.UnitCategoryEnum
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
import com.navis.inventory.InventoryPropertyKeys
import com.navis.inventory.business.api.UnitFinder
import com.navis.inventory.business.api.UnitManager
import com.navis.inventory.business.units.Unit
import com.navis.inventory.business.units.UnitFacilityVisit
import com.navis.road.RoadField
import com.navis.road.business.api.IGateApiHandler
import com.navis.road.business.api.RoadManager
import com.navis.road.business.apihandler.CreateTruckVisitHandler
import com.navis.road.business.atoms.GateClientTypeEnum
import com.navis.road.business.atoms.TranSubTypeEnum
import com.navis.road.business.atoms.TruckVisitStatusGroupEnum
import com.navis.road.business.model.RoadManagerPea
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
 * Created by psethuraman on 7/6/2017.
 */


class ProcessTruckHandlerInterceptor implements EGateApiHandlerInterceptor, GateApiConstants {
    private static final Logger LOGGER = Logger.getLogger(ProcessTruckHandlerInterceptor.class.getName());
    String[] lanesIn   = ["LANE 1", "LANE 2", "LANE 50", "LANE 51", "LANE 52", "LANE 53", "LANE 54", "LANE 55", "P101"];
    String[] lanesOut   = ["LANE 3", "LANE 4", "LANE 60", "LANE 61", "LANE 62", "LANE 63", "LANE 64", "LANE 65", "P301"];

    IGateApiHandler _builtInApiHandler;
    public void execute(UserContext inUserContext, MessageCollector inMessageCollector,
                        Element inEAction, Element inOutEResult, List<Element> inOutAdditionalResponses,
                        Long inWslogGkey) throws BizViolation {
        XMLOutputter outputter=new XMLOutputter();
        String inXmlString = outputter.outputString(inEAction);
        LOGGER.error("Inbound Message is :"+inXmlString);

        LOGGER.warn("ProcessTruck WS API Intercepted TV Visit: "+inEAction.getName());

        Element eEquip = inEAction.getChild(EQUIPMENT);
        Element eContainer = eEquip != null ? eEquip.getChild(CONTAINER) : null;
        Element eChassis = eEquip != null ? eEquip.getChild(CHASSIS) : null;
        Element eAccessory = eContainer != null ? eContainer.getChild(ACCESSORY) : null;

        String ctrId = eContainer != null ? eContainer.getAttributeValue(EQO_CTR_NBR, eEquip.getNamespace()) : "";
        String chsId = eChassis != null ? eChassis.getAttributeValue(EQO_CTR_NBR, inEAction.getNamespace()) : "";
        LOGGER.warn("chassis found : "+chsId);
        if ((chsId == null || StringUtils.isBlank(chsId)) && eContainer != null) {
            chsId = eContainer.getAttributeValue(CTR_ON_CHASSIS_ID, inEAction.getNamespace());
        }
        LOGGER.warn("chassis found1 : "+chsId);

        String accId = eAccessory != null ? eAccessory.getAttributeValue(EQO_CTR_NBR, eEquip.getNamespace()) : "";
        LOGGER.warn("Accessory Found : "+accId);
        String tranType = eContainer != null ? eContainer.getAttributeValue(TRAN_TYPE, eEquip.getNamespace()) : "";
        LOGGER.warn("tranType Found : "+tranType);

        Element eTruck = inEAction.getChild(TRUCK);
        String license = eTruck.getAttributeValue(TRUCK_LICENSE_NBR);
        String laneId = inEAction.getChild(LANE_ID).getText();
        String rfid = eTruck != null ? eTruck.getAttributeValue(TRUCK_TAG_ID, eTruck.getNamespace()) : "";
        String trkCoId = eTruck != null ? eTruck.getAttributeValue(TT_TRUCKING_CO_ID, eTruck.getNamespace()) : "";
        Element eTruckVisit = inEAction.getChild(TRUCK_VISIT);
        String tranEqoNbr = eTruckVisit != null ? eTruckVisit.getAttributeValue(TV_FLEX_STRING02, eTruck.getNamespace()) : "";
        String gateNotes = eContainer != null ? eContainer.getAttributeValue(UNIT_FLEX_STRING_8, eEquip.getNamespace()) : "";
        LOGGER.warn("gateNotes Found : "+gateNotes);
        Truck truck = null;
        String trkId = null;


        TruckingCompany trkc = null;
        TruckDriver driver = null;

        /*********
         * MAKING CHANGES WITH SINGLE GATE, AS IT CREATES DOUBLE TRANSACTIONS RANDOMLY
         */
        /*LOGGER.warn("Posted stage ID : "+inEAction.getChild(STAGE_ID).getText());
        inEAction.getChild(STAGE_ID).setText("ingateauto");*/
        if (inEAction.getChild(LANE_ID) != null) {
            laneId = inEAction.getChild(LANE_ID).getText();
            if (laneId != null) {
                inEAction.getChild(LANE_ID).setText(laneId.toUpperCase());
            }
        }
        LOGGER.warn("NOT Replaced stage ID : "+inEAction.getChild(STAGE_ID).getText());
        //REMOVE PLACARDS FROM PROCESS-TRUCK AS PLACARDS VALIDATION IS NOT NEEDED
        if (eContainer != null) {
            eContainer.removeChild(TT_CTR_PLACARDS);
        }
        new CreateTruckVisitHandler().execute(inUserContext, inMessageCollector, inEAction, inOutEResult, inOutAdditionalResponses, inWslogGkey);
        Thread.sleep(1000L);//todo kramacha, added delay
        LOGGER.warn("Truck Visit got created with Next stage  : ");

        //FINDING TRUCKING COMPANY AND LICENSE PLATE FROM TAG-ID
        CarinaPersistenceCallback findTruckCB = new CarinaPersistenceCallback() {
            protected void doInTransaction() {
                if (rfid != null && license != null) {
                    truck = Truck.findOrCreate(license, license, null, rfid, false);
                } else if (rfid != null ) {
                    truck = Truck.findTruckByTagId(rfid);
                } else if (StringUtils.isBlank(rfid) && license != null) {
                    truck = Truck.findTruckByLicNbr(license);
                }
                if (truck != null) {
                    inEAction.getChild(TRUCK).setAttribute(TRUCK_LICENSE_NBR, truck.getTruckLicenseNbr());
                    license = truck.getTruckLicenseNbr();
                    trkc = truck.getTruckTrkCo();
                    trkId = truck.getTruckId();
                    driver = truck.getTruckDriver();
                }
                if (trkc != null) {
                    trkCoId = trkc.getBzuId();
                } else {
                    trkc = TruckingCompany.findTruckingCompany(trkCoId);
                }
                if (trkCoId != null) {
                    truck.setTruckTrkCo(trkc);
                    inEAction.getChild(TRUCK).setAttribute(TT_TRUCKING_CO_ID, trkCoId);
                }
                HibernateApi.getInstance().flush();
            }
        };
        inMessageCollector = (new PersistenceTemplatePropagationRequired(inUserContext)).invoke(findTruckCB);
        //Thread.sleep(2000l); //todo, kramacha, added 2 seconds delay



        CarinaPersistenceCallback inSubmitTranCb = new CarinaPersistenceCallback() {
            TranSubTypeEnum subTypeEnum = null;
            protected void doInTransaction() {
                Chassis chs = null;
                if (StringUtils.isNotBlank(chsId)) {
                    chs = Chassis.findChassis(Chassis.findFullIdOrPadCheckDigit(chsId));
                    chsId = chs != null ? chs.getEqIdFull() : chsId;
                }
                Unit activeUnit = null;
                if (StringUtils.isBlank(tranType)) {
                    String equipmentId = ctrId;
                    if (StringUtils.isBlank(ctrId) && StringUtils.isNotBlank(chsId)) {
                        equipmentId = Chassis.findFullIdOrPadCheckDigit(chsId);
                    }
                    LOGGER.warn("Equip for finding Tran Type : "+equipmentId);
                    Equipment equipment = Equipment.findEquipment(equipmentId);
                    LOGGER.warn("Equipment found : "+equipment);
                    if (equipment != null) {
                        String eqClass = equipment.getEqClass().getName();
                        activeUnit = findActiveUnitToDeliver(equipmentId);
                        LOGGER.warn("activeUnit found : "+activeUnit);
                        if (activeUnit != null) {
                            LOGGER.warn("activeUnit found id : "+activeUnit.getUnitId());
                            tranType = getUnitTranType(activeUnit, eqClass, laneId);
                        }


                        if (tranType != null) {
                            subTypeEnum = TranSubTypeEnum.getEnum(tranType);
                        }
                        if (StringUtils.isBlank(tranType) && subTypeEnum == null && EquipClassEnum.CHASSIS.getName().equalsIgnoreCase(equipment.getEqClass().getName())
                                && lanesOut.contains(laneId)) {
                            tranType = "DC";
                            subTypeEnum = TranSubTypeEnum.getEnum(tranType);
                        } else if (StringUtils.isBlank(tranType) && subTypeEnum == null && EquipClassEnum.CHASSIS.getName().equalsIgnoreCase(equipment.getEqClass().getName())
                                && lanesIn.contains(laneId)) {
                            tranType = "RC";
                            subTypeEnum = TranSubTypeEnum.getEnum(tranType);
                        }
                    }
                }
                subTypeEnum = TranSubTypeEnum.getEnum(tranType);
                LOGGER.warn("Tran Type : "+subTypeEnum);

                TruckVisitDetails tvdtls = TruckVisitDetails.findMostRecentTruckVisitByField(RoadField.TVDTLS_TRUCK_AEI_TAG_ID, rfid, TruckVisitStatusGroupEnum.IN_PROGRESS);
                if (tvdtls != null) {
                    LOGGER.warn("Truck Visit got created with tag at: " + tvdtls.getTvdtlsNextStageId());
                } else {
                    tvdtls = TruckVisitDetails.findMostRecentTruckVisitByField(RoadField.TVDTLS_TRUCK_LICENSE_NBR, license, TruckVisitStatusGroupEnum.IN_PROGRESS);
                    LOGGER.warn("Truck Visit got created with license at: " + tvdtls.getTvdtlsNextStageId());
                }
                //tvdtls.setNextStageId("gateauto");
                LOGGER.warn("TVDTLS : "+tvdtls);
                if (subTypeEnum != null) {

                    TruckTransaction tran = TruckTransaction.create(tvdtls, subTypeEnum);

                    //COMMENTED FOR SINGLE STAGE CHANGES
                    tran.setTranStageId("gateauto");
                    tran.setTranNextStageId("gateauto");
                    /* FieldChanges inTranChanges = new FieldChanges();
                 FieldChanges inNonTranChanges = new FieldChanges();*/

                    LOGGER.warn("Trucking Company : " + trkCoId);
                    tvdtls.setTvdtlsTruck(truck);
                    tvdtls.setTvdtlsTruckingCoId(trkCoId);
                    tvdtls.setTvdtlsTruckId(trkId);
                    tvdtls.setTvdtlsTruckAeiTagId(rfid);



                    if (!"DC".equalsIgnoreCase(tranType) && ctrId != null) {
                        tran.setTranCtrNbr(ctrId);
                        tran.setTranChsNbr(chsId);
                    } else if (chsId != null) {
                        ctrId = "";
                        tran.setTranChsNbr(chsId);
                    }
                    if (accId != null) {
                        Accessory acc = findFullIdOrPadCheckDigit(accId);
                        LOGGER.warn("Accessory Entity : " + acc);
                        if (acc != null) {
                            accId = acc.getEqIdFull();
                            eAccessory.setAttribute(EQO_CTR_NBR, accId);
                            tran.setTranCtrAccessory(acc);
                        }
                        tran.setTranCtrAccNbr(accId);
                    }

                    if (StringUtils.isNotBlank(gateNotes)) {
                        LOGGER.warn("setting gate notes : " + gateNotes);
                        tran.setTranFlexString08(gateNotes);
                    }
                    //COMMENTED FOR SINGLE STAGE CHANGES
                    /*RoadManagerPea roadManagerPea = (RoadManagerPea) Roastery.getBean(RoadManager.BEAN_ID);
                TransactionAndVisitHolder wfCtx = roadManagerPea.submitTransaction(tran, inTranChanges, inNonTranChanges, tvdtls.getTvdtlsGate(), GateClientTypeEnum.AUTOGATE, null, null);
                TruckTransaction submittedTran = wfCtx.getTran();*/
                    LOGGER.warn("Submitted gate stage : " + tran.getTranStageId());
                    LOGGER.warn("Submitted gate next stage: " + tran.getTranNextStageId());
                    if (!"DC".equalsIgnoreCase(tranType)) {
                        Container ctr = Container.findContainer(ctrId);
                        LOGGER.warn("Non DC transaction " + tranType);
                        if (ctr != null) {
                            //todo, kramacha added logger to trace it
                            tran.setTranContainer(ctr);
                            tran.setTranCtrTypeId(ctr.getEqEquipType().getEqtypId());
                            tran.setTranCtrOwnerId(ctr.getEquipmentOwnerId());
                            tran.setTranCtrOperator(ctr.getEquipmentOperator());
                            LOGGER.warn("Setting Tran Container Properties, ctr / eqtype/owner/opr" + ctr + "," + ctr.getEqEquipType().getEqtypId() + "," + ctr.getEquipmentOwnerId() + "," + ctr.getEquipmentOperatorId());
                        }
                        if (chs != null) {
                            //todo, kramacha added logger to trace it
                            LOGGER.warn("chs.getEqEquipType().getEqtypId()" + chs.getEqEquipType().getEqtypId());
                            tran.setTranChsTypeId(chs.getEqEquipType().getEqtypId());
                        }
                        if (activeUnit != null) {
                            if (activeUnit.getUnitSealNbr1() != null) {
                                tran.setTranSealNbr1(activeUnit.getUnitSealNbr1());
                            }
                            if (delUnit.getUnitGoods() != null) {
                                tran.setTranImportReleaseNbr(activeUnit.getUnitGoods().getGdsBlNbr());
                            }
                        }
                        if (tranEqoNbr != null) {
                            tran.setTranEqoNbr(tranEqoNbr);
                        }
                    } else if (chs != null) {
                        LOGGER.warn("DC transaction " + tranType);
                        UnitFinder unitFinder = (UnitFinder) Roastery.getBean(UnitFinder.BEAN_ID);
                        Unit chsUnit = unitFinder.findActiveUnit(ContextHelper.getThreadComplex(), chs);
                        if (chsUnit != null) {
                            UnitFacilityVisit chsUfv = chsUnit.getUnitActiveUfvNowActive();
                            Serializable fcyGkey = ContextHelper.getThreadFacilityKey();
                            if (fcyGkey == chsUfv.getUfvFacility().getFcyGkey()) {
                                chsUfv.getUfvLastKnownPosition().setPosLocType(LocTypeEnum.TRUCK);
                            }
                            tran.setTranChsTypeId(chs.getEqEquipType().getEqtypId());
                        }
                    }
                    /*submittedTran.setTranChassis(chs);
                submittedTran.setTranChsAccessory(findFullIdOrPadCheckDigit(accId));*/
                    HibernateApi.getInstance().flush();
                    LOGGER.warn("Transaction created : " + tran.getTranStatus().toString());
                }
            }
        };
        inMessageCollector = (new PersistenceTemplatePropagationRequired(inUserContext)).invoke(inSubmitTranCb);
        Thread.sleep(1000L);//todo, kramacha added delay after persistence

        //roadManagerPea.submitTransaction()
        //new SubmitTransactionHandler().execute(inUserContext, inMessageCollector, inEAction, inOutEResult, inOutAdditionalResponses, inWslogGkey);
        /*getBuiltinApiHandler().executeInternal(
                inUserContext,
                inMessageCollector,
                inEAction,
                inOutEResult,
                inOutAdditionalResponses,
                inWslogGkey
        );*/

//COMMENTED FOR SINGLE STAGE CHANGES
        /*if ("DC".equalsIgnoreCase(tranType)) {
            if (inEAction != null) {
                inEAction.removeChild(EQUIPMENT);
                LOGGER.warn("Equipment tag removed from scanned data : " + inEAction.toString());
            }
        }*/

//        inEAction.getChild(STAGE_ID).setText("gateauto");
        LOGGER.warn("Replaced back stage ID : "+inEAction.getChild(STAGE_ID).getText());
        getBuiltinApiHandler().executeInternal(
                inUserContext,
                inMessageCollector,
                inEAction,
                inOutEResult,
                inOutAdditionalResponses,
                inWslogGkey
        );

        LOGGER.warn("ProcessTruck WS API Intercepted calling : END ");
    }

    private static Accessory findFullIdOrPadCheckDigit(String inAccId) {
        String accIdFull = inAccId;
        Accessory acc = Accessory.findAccessory(inAccId);
        if(acc == null) {
            if(inAccId.length() == 10) {
                DomainQuery dq = QueryUtils.createDomainQuery("Accessory").addDqPredicate(PredicateFactory.eq(ArgoRefField.EQ_LIFE_CYCLE_STATE, LifeCycleStateEnum.ACTIVE)).addDqPredicate(PredicateFactory.like(ArgoRefField.EQ_ID_FULL, inAccId + "%"));
                List eqs = HibernateApi.getInstance().findEntitiesByDomainQuery(dq);
                if(eqs != null && eqs.size() == 1) {
                    acc = (Accessory)eqs.get(0);
                    //accIdFull = acc.getEqIdFull();
                }
            }
        }

        return acc;
    }

    private String getUnitTranType(Unit activeUnit, String eqClass, String laneId) {
        String tranType = "DI";
        /*String LANE1 = "LANE 1";
        String LANE2 = "LANE 2";
        String LANE3 = "LANE 3";
        String LANE4 = "LANE 4";*/
        String INCONSOLE = "P101";
        String OUTCONSOLE = "P301";

        List inLanes = new ArrayList(20);
        List outLanes = new ArrayList(20);


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
        LOGGER.warn("tranType derived : "+tranType);
        return tranType;
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

            if (facility == activeUfv.getUfvFacility().getFcyGkey()) {
                activeUnit = activeUfv.getUfvUnit();
            }
        } catch (Exception e) {
            LOGGER.error("Requested unit from GATE OCR not found in ANK YARD");
        }
        //Unit unit= ufv.getUfvUnit();
        //UnitFacilityVisit activeUfv = unit.getUnitActiveUfvNowActive();


        return activeUnit;
    }

    public void setBuiltInApiHandler(IGateApiHandler inBuiltInApiHandler) {
        this._builtInApiHandler = inBuiltInApiHandler;
    }

    protected IGateApiHandler getBuiltinApiHandler() {
        return this._builtInApiHandler;
    }
}
