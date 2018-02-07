import com.navis.argo.ArgoRefField
import com.navis.argo.ContextHelper
import com.navis.argo.business.atoms.BizRoleEnum
import com.navis.argo.business.atoms.EquipClassEnum
import com.navis.argo.business.atoms.FreightKindEnum
import com.navis.argo.business.atoms.LocTypeEnum
import com.navis.argo.business.atoms.UnitCategoryEnum
import com.navis.argo.business.model.CarrierVisit
import com.navis.argo.business.reference.Accessory
import com.navis.argo.business.reference.Chassis
import com.navis.argo.business.reference.Container
import com.navis.argo.business.reference.Equipment
import com.navis.argo.business.reference.RoutingPoint
import com.navis.framework.business.Roastery
import com.navis.framework.business.atoms.LifeCycleStateEnum
import com.navis.framework.persistence.HibernateApi
import com.navis.framework.portal.QueryUtils
import com.navis.framework.portal.query.DomainQuery
import com.navis.framework.portal.query.PredicateFactory
import com.navis.inventory.InventoryEntity
import com.navis.inventory.business.api.UnitField
import com.navis.inventory.business.api.UnitManager
import com.navis.inventory.business.atoms.UfvTransitStateEnum
import com.navis.inventory.business.atoms.UnitVisitStateEnum
import com.navis.inventory.business.units.Unit
import com.navis.inventory.business.units.UnitFacilityVisit
import com.navis.road.business.atoms.TranSubTypeEnum
import com.navis.road.business.model.TruckTransaction
import com.navis.road.business.model.TruckingCompany
import com.navis.road.portal.GateApiConstants
import org.apache.commons.lang.StringUtils
import org.apache.log4j.Logger
import org.jdom.Document;
import org.jdom.Element
import com.navis.argo.util.XmlUtil;
import com.navis.external.argo.AbstractCustomWSHandler;
import com.navis.framework.portal.UserContext;
import com.navis.framework.util.message.MessageCollector

/*

<validate>
    <chassis eqid="PSRU1201202"/>
</validate>
*/

public class CustomValidationHandler extends AbstractCustomWSHandler implements GateApiConstants {
    private static final Logger LOGGER = Logger.getLogger(CustomValidationHandler.class.getName());
    private static final String RESPONSE = new String("RESPONSE");
    private static final String FOUND = new String("Found");
    private static final String NOT_FOUND = new String("Not Found");
    private final String TRUCKING_CO = new String("trucking-company");
    private final String TRUCKING_COMPANIES = new String("trucking-companies");
    private final String TRAN_EQ = new String("tran-equipment");

    /**
     * Entry point to the custom webservice handler
     * @param inUserContext the authenticated user from the webservice call
     * @param inOutMessageCollector the message collector of the webservice call
     * @param inRequest the request message
     * @param inWslogGkey the key of the entry in the webservice logs for this call
     * @return the response message
     */
    public String execute(
            UserContext inUserContext,
            MessageCollector inOutMessageCollector,
            String inRequest,
            Long inWslogGkey
    ) {
        LOGGER.warn("Equipment WS Validation Request from Nascent Started");
        Element eResponse = new Element(RESPONSE);
        Document dRequest = XmlUtil.parse(inRequest);

        try {
            if (dRequest != null) {
                Element eRequest = dRequest.getRootElement();

                Element eContainer = validateAndFormResponseXML(eRequest.getChild(CONTAINER), CONTAINER);
                Element eChassis = validateAndFormResponseXML(eRequest.getChild(CHASSIS), CHASSIS);
                Element eAccessory = validateAndFormResponseXML(eRequest.getChild(ACCESSORY), ACCESSORY);
                Element eTruckCo = validateAndFormResponseXML(eRequest.getChild(TRUCKING_CO), TRUCKING_CO);
                Element eTruckCos = validateAndFormResponseXML(eRequest.getChild(TRUCKING_COMPANIES), TRUCKING_COMPANIES);
                Element eTranEq = validateAndFormResponseXML(eRequest.getChild(TRAN_EQ), TRAN_EQ);

                if (eContainer != null ) {
                    eResponse.addContent(eContainer);
                }
                if (eChassis!= null ) {
                    eResponse.addContent(eChassis);
                }
                if (eAccessory != null ) {
                    eResponse.addContent(eAccessory);
                }
                if (eTruckCo != null) {
                    eResponse.addContent(eTruckCo);
                }
                if (eTruckCos != null) {
                    eResponse.addContent(eTruckCos);
                }
                if (eTranEq != null) {
                    eResponse.addContent(eTranEq);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error thrown while doing the custom validation");
            LOGGER.error(e.toString())
            eResponse.setText("ERROR");
        }

        LOGGER.warn("Equipment Validation WS Request from Nascent End")
        return XmlUtil.toString(eResponse, true);
    }

    private Element validateAndFormResponseXML(Element element, String elementType) {
        Element retElement = new Element(elementType);
        if (element != null) {
            if (CONTAINER.equalsIgnoreCase(element.getName())
                    || CHASSIS.equalsIgnoreCase(element.getName())
                    || ACCESSORY.equalsIgnoreCase(element.getName())) {
                retElement = validateAndFormEquipmentXML(element, elementType);
            } else if (TRUCKING_CO.equalsIgnoreCase(element.getName())) {
                retElement = validateAndFormTruckingCoXML(element, elementType);
            } else if (TRUCKING_COMPANIES.equalsIgnoreCase(element.getName())) {
                retElement = validateAndFormTruckingCoListXML(element, elementType);
            } else if (TRAN_EQ.equalsIgnoreCase(element.getName())) {
                retElement = validateAndFormTranCtrXML(element, elementType);
            }
        } else {
            return null;
        }
        return  retElement;
    }

    private Element validateAndFormEquipmentXML(Element element, String eqClass) {

        Element retElement = new Element(eqClass);
        if (element != null) {
            String retString;
            Boolean found = Boolean.FALSE;
            String eqId = element.getAttributeValue(EQ_ID);

            String eqType = null;
            String eqTareWeight = 0.0;
            if (CONTAINER.equalsIgnoreCase(element.getName()) && StringUtils.isNotEmpty(eqId)) {
                Container container = Container.findContainer(Container.findFullIdOrPadCheckDigit(eqId));
                if (container != null) {
                    eqId = container.getEqIdFull();
                    eqType = container.getEqEquipType().getEqtypId();
                    eqTareWeight = container.getEqTareWeightKg();
                    found = Boolean.TRUE;
                }
            } else if (CHASSIS.equalsIgnoreCase(element.getName()) && StringUtils.isNotEmpty(eqId)) {
                Chassis chassis = Chassis.findChassis(Chassis.findFullIdOrPadCheckDigit(eqId));
                if (chassis != null) {
                    eqId = chassis.getEqIdFull();
                    eqType = chassis.getEqEquipType().getEqtypId();
                    eqTareWeight = chassis.getEqTareWeightKg();
                    found = Boolean.TRUE;
                }
            } else if (ACCESSORY.equalsIgnoreCase(element.getName()) && StringUtils.isNotEmpty(eqId)) {
                Accessory accessory = Accessory.findAccessory(findFullIdOrPadCheckDigit(eqId));
                if (accessory != null) {
                    eqId = accessory.getEqIdFull();
                    eqType = accessory.getEqEquipType().getEqtypId();
                    eqTareWeight = accessory.getEqTareWeightKg();
                    found = Boolean.TRUE;
                }
            }

            retElement.setAttribute(EQ_ID, eqId);
            if (found) {
                retString = FOUND;
                retElement.setAttribute(EQ_TYPE, eqType);
                retElement.setAttribute(TT_CTR_TARE_WEIGHT+"_"+SCALE_WEIGHT_UNIT__KG, eqTareWeight);
            } else {
                retString = NOT_FOUND;
            }
            LOGGER.warn(retString);
            retElement.setText(retString)
        }
        return retElement;

    }

    private Element validateAndFormTruckingCoXML(Element element, String truckClass) {
        Element retElement = new Element(truckClass);
        Boolean found = Boolean.FALSE;
        String retString = NOT_FOUND;
        if (element != null) {
            Boolean isActive = Boolean.FALSE;
            String truckingCoId = element.getAttributeValue("id");
            Date expDate = null;
            if (StringUtils.isNotEmpty(truckingCoId)) {
                retElement.setAttribute(TRUCKING_CO_ID, truckingCoId);
                TruckingCompany company = TruckingCompany.findTruckingCompany(truckingCoId);
                LOGGER.warn("Found TRKC : " + company);
                if (company != null) {
                    found = Boolean.TRUE;
                    retString = FOUND;
                    isActive = company.isActive();
                    expDate = company.getTrkcInsuranceExpiration();
                }
            }
            if (found) {
                retElement.setAttribute("ACTIVE", isActive.toString());
                retElement.setAttribute("EXP-DATE", expDate != null ? expDate.toString() : "");
            }
            retElement.setText(retString);
        }
        return retElement

    }

    private Element validateAndFormTruckingCoListXML(Element element, String truckClass) {
        Element retElement = new Element(truckClass);
        Boolean found = Boolean.FALSE;
        String retString = NOT_FOUND;
        if (element != null) {
            Boolean isActive = Boolean.FALSE;
            String truckingCoName = element.getAttributeValue("name");
            String truckingCoId = element.getAttributeValue("id");
            Date expDate = null;

            DomainQuery dq = QueryUtils.createDomainQuery("TruckingCompany");

            if (StringUtils.isNotEmpty(truckingCoName)) {
                dq.addDqPredicate(PredicateFactory.like(ArgoRefField.BZU_NAME, truckingCoName+"%"));
            }
            if (StringUtils.isNotEmpty(truckingCoId)) {
                dq.addDqPredicate(PredicateFactory.like(ArgoRefField.BZU_ID, truckingCoId+"%"));
            }
            dq.addDqPredicate(PredicateFactory.eq(ArgoRefField.BZU_ROLE, BizRoleEnum.HAULIER));
            List<TruckingCompany> truckingCompanies = HibernateApi.getInstance().findEntitiesByDomainQuery(dq);
            LOGGER.warn("List of companies : "+truckingCompanies.size());
            for (TruckingCompany company : truckingCompanies) {
                Element truckCo = new Element(TRUCKING_CO);
                //LOGGER.warn("TRKC NAME : "+company.getBzuName());
                truckCo.setAttribute(TRUCKING_CO_ID, company.getBzuId() != null ? company.getBzuId() : "");
                truckCo.setAttribute(TRUCKING_CO_NAME, company.getBzuName() != null ? company.getBzuName() : "");
                isActive = company.isActive();
                expDate = company.getTrkcInsuranceExpiration();
                truckCo.setAttribute("ACTIVE", isActive.toString());
                truckCo.setAttribute("EXP-DATE", expDate != null ? expDate.toString() : "");
                retElement.addContent(truckCo);
            }
        }
        return retElement

    }

    private Element validateAndFormTranCtrXML(Element element, String equipType) {
        Element retElement = new Element(equipType);
        //< tran-equipment eqid="MATU5167866" eqClass=”CHASSIS” tran-type=”IMPRT”/>
        if (element != null) {
            String retString = "NOT IN YARD";
            String ctrId = element.getAttributeValue("eqid");
            String laneId = element.getAttributeValue("laneId");
            LOGGER.warn("Received Container Id to Deliver : "+ctrId);
            if (StringUtils.isNotEmpty(ctrId)) {
                retElement.setAttribute(EQ_ID, ctrId);
                Unit activeUnit = findActiveUnitToDeliver(ctrId);
                LOGGER.warn("Unit to be delivered : " +ctrId +" "+activeUnit != null ? FOUND : NOT_FOUND);
                if (activeUnit != null) {
                    retString = FOUND;
                    Equipment equipment = activeUnit.getUnitPrimaryUe().getUeEquipment();
                    String eqClass = equipment.getEqClass().getName();
                    String category =  activeUnit.getUnitCategory().getName();
                    if (laneId == null) {
                        laneId = "LANE 1";
                    }
                    String tranType = getUnitTranType(activeUnit, eqClass, laneId);
                    retElement.setAttribute("CLASS", eqClass);
                    retElement.setAttribute("CATEGORY", category);
                    retElement.setAttribute(TRAN_TYPE, tranType);
                    LOGGER.warn("Equipment Type to be deliver : " + eqClass);
                }
            }

            retElement.setText(retString);
        }
        return retElement

    }

    private static String findFullIdOrPadCheckDigit(String inAccId) {
        String accIdFull = inAccId;
        Accessory acc = Accessory.findAccessory(inAccId);
        if(acc == null) {
            if(inAccId.length() == 10) {
                DomainQuery dq = QueryUtils.createDomainQuery("Accessory").addDqPredicate(PredicateFactory.eq(ArgoRefField.EQ_LIFE_CYCLE_STATE, LifeCycleStateEnum.ACTIVE)).addDqPredicate(PredicateFactory.like(ArgoRefField.EQ_ID_FULL, inAccId + "%"));
                List eqs = HibernateApi.getInstance().findEntitiesByDomainQuery(dq);
                if(eqs != null && eqs.size() == 1) {
                    acc = (Accessory)eqs.get(0);
                    accIdFull = acc.getEqIdFull();
                }
            }
        } else {
            accIdFull = acc.getEqIdFull();
        }

        return accIdFull;
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
}

