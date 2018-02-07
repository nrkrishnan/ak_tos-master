import com.navis.argo.ArgoRefField
import com.navis.argo.ContextHelper
import com.navis.argo.business.api.ArgoUtils
import com.navis.argo.business.atoms.EquipClassEnum
import com.navis.argo.business.reference.Chassis
import com.navis.argo.business.reference.EqComponent
import com.navis.argo.business.reference.EquipDamageType
import com.navis.argo.business.reference.Equipment
import com.navis.argo.web.ArgoGuiMetafield
import com.navis.external.road.AbstractGateTaskInterceptor
import com.navis.external.road.EGateTaskInterceptor
import com.navis.framework.AllOtherFrameworkPropertyKeys
import com.navis.framework.business.Roastery
import com.navis.framework.persistence.hibernate.CarinaPersistenceCallback
import com.navis.framework.persistence.hibernate.PersistenceTemplatePropagationRequired
import com.navis.framework.portal.QueryUtils
import com.navis.framework.portal.query.DomainQuery
import com.navis.framework.portal.query.PredicateFactory
import com.navis.framework.util.BizFailure
import com.navis.framework.util.BizViolation
import com.navis.framework.util.message.MessageCollector
import com.navis.framework.util.message.MessageLevel
import com.navis.inventory.*
import com.navis.inventory.business.api.UnitField
import com.navis.inventory.business.api.UnitFinder
import com.navis.inventory.business.atoms.UfvTransitStateEnum
import com.navis.inventory.business.atoms.UnitVisitStateEnum
import com.navis.inventory.business.units.Unit
import com.navis.inventory.business.units.UnitEquipDamageItem
import com.navis.inventory.business.units.UnitEquipDamages
import com.navis.inventory.business.units.UnitEquipment
import com.navis.inventory.business.units.UnitFacilityVisit
import com.navis.road.business.atoms.TranSubTypeEnum
import com.navis.road.business.model.TruckTransaction
import com.navis.road.business.util.RoadBizUtil
import com.navis.road.business.workflow.TransactionAndVisitHolder
import com.navis.framework.persistence.HibernateApi
import com.navis.inventory.business.atoms.EqDamageSeverityEnum
import com.navis.services.business.event.Event;
import org.apache.log4j.Level
import org.apache.log4j.Logger

import java.text.SimpleDateFormat

/**
 * @author Keerthi Ramachandran
 * @since 7/18/2017
 * <p>MatsonAncValidateChassisInspectionDate is ..</p>
 */
class MatsonAncValidateChassisInspectionDate extends AbstractGateTaskInterceptor implements EGateTaskInterceptor {
    private static final Logger LOGGER = Logger.getLogger(MatsonAncValidateChassisInspectionDate.class);
    public static final String WARNING_MESSAGE = "Chassis PM about to Expire";
    public static final String SUCCESS = "SUCCESS";
    public static final String MONTH_ERROR = "Month Passed";
    public static final String RECEIVAL_ERROR = "Receival Error";
    public static final String YEAR_ERROR = "Year Error";
    public static final String INSPECTIONDUE_NULL = "Inspection Date Null";

    @Override
    void execute(TransactionAndVisitHolder inTransactionAndVisitHolder) {
        super.execute(inTransactionAndVisitHolder);
        LOGGER.setLevel(Level.DEBUG);
        LOGGER.info("Begin MatsonAncValidateChassisInspectionDate.execute after super.execute(inTransactionAndVisitHolder)");
        if (this.hasError()) {
            //todo are validating as the transaction already errored out.
            LOGGER.debug("The Transaction already has error, returning")
        }
        //begin the logic here
        TruckTransaction truckTransaction = inTransactionAndVisitHolder.getTran();
        LOGGER.debug("TruckTransaction\t" + truckTransaction);
        Unit unit = truckTransaction.getTranUnit();
        LOGGER.debug("Unit\t" + unit);
        LOGGER.debug("truckTransaction.getTranEqoNbr()\t" + truckTransaction.getTranEqoNbr());
        Chassis chassis = truckTransaction.getTranChassis();

        LOGGER.debug("Chassis\t" + chassis);
        if (TranSubTypeEnum.DC.equals(truckTransaction.getTranSubType()) ||
                TranSubTypeEnum.DI.equals(truckTransaction.getTranSubType()) ||
                TranSubTypeEnum.DE.equals(truckTransaction.getTranSubType()) ||
                TranSubTypeEnum.DM.equals(truckTransaction.getTranSubType()) ||
                TranSubTypeEnum.DB.equals(truckTransaction.getTranSubType())) {
            if (chassis != null && !truckTransaction.isOwnerChassis()) {
                Date timeNow = ArgoUtils.timeNow();
                LOGGER.debug("Timenow\t" + timeNow);
                Date federalChassisInspectionExpDate = chassis.getEqFedInspectExp();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM");
                String message = compareDateAndSetWarningMessageForDelivery(federalChassisInspectionExpDate, timeNow);

                if (SUCCESS.equals(message)) {
                    return;
                } else if (INSPECTIONDUE_NULL.equals(message)) {
                    LOGGER.info("Chassis "+chassis+" do not have Federal Inspection Expiry Date Set");
                    return;
                } else if (WARNING_MESSAGE.equals(message)) {
                    RoadBizUtil.appendExceptionChainAsWarnings(BizViolation.createFieldViolation(AllOtherFrameworkPropertyKeys.ERROR__NULL_MESSAGE, (BizViolation) null, ArgoGuiMetafield.EQ_FED_INSPECT_EXP,
                            "The Chassis (" + chassis.getEqIdFull() + ") PM about to Expire"));
                } else if (MONTH_ERROR.equals(message)) {

                    LOGGER.debug("Calling 1");
                    addDamagesToFederalInspExpiredChassis(chassis, truckTransaction);


                    RoadBizUtil.appendExceptionChain(BizViolation.createFieldViolation(AllOtherFrameworkPropertyKeys.ERROR__NULL_MESSAGE, (BizViolation) null, ArgoGuiMetafield.EQ_FED_INSPECT_EXP,
                            "The Chassis (" + chassis.getEqIdFull() + ") PM Expired on " + dateFormat.format(federalChassisInspectionExpDate)));

                    return;

                } else if (YEAR_ERROR.equals(message)) {
                    RoadBizUtil.appendExceptionChain(BizViolation.createFieldViolation(AllOtherFrameworkPropertyKeys.ERROR__NULL_MESSAGE, (BizViolation) null, ArgoGuiMetafield.EQ_FED_INSPECT_EXP,
                            "The Chassis (" + chassis.getEqIdFull() + ") PM Expired on " + dateFormat.format(federalChassisInspectionExpDate)));
                    addDamagesToFederalInspExpiredChassis(chassis, truckTransaction);
                }
            } else {
                //no validation
            }
        } else if (TranSubTypeEnum.RC.equals(truckTransaction.getTranSubType()) ||
                TranSubTypeEnum.RE.equals(truckTransaction.getTranSubType()) ||
                TranSubTypeEnum.RI.equals(truckTransaction.getTranSubType()) ||
                TranSubTypeEnum.RM.equals(truckTransaction.getTranSubType()) ||
                TranSubTypeEnum.RB.equals(truckTransaction.getTranSubType())) {

            if (chassis != null && !truckTransaction.isOwnerChassis()) {
                Date timeNow = ArgoUtils.timeNow();
                LOGGER.debug("Timenow\t" + timeNow);
                Date federalChassisInspectionExpDate = chassis.getEqFedInspectExp();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM");
                String message = compareDateAndSetWarningMessageForReceival(federalChassisInspectionExpDate, timeNow);
                if (SUCCESS.equals(message)) {
                    return;
                } else if (INSPECTIONDUE_NULL.equals(message)) {
                    LOGGER.info("Chassis "+chassis+" do not have Federal Inspection Expiry Date Set");
                    return;
                } else if (WARNING_MESSAGE.equals(message)) {
                    RoadBizUtil.appendExceptionChainAsWarnings(BizViolation.createFieldViolation(AllOtherFrameworkPropertyKeys.ERROR__NULL_MESSAGE, (BizViolation) null, ArgoGuiMetafield.EQ_FED_INSPECT_EXP,
                            "The Chassis (" + chassis.getEqIdFull() + ") PM is Expiring this Month"));
                    truckTransaction.setTranUnitFlexString12("M");
                    LOGGER.info("Chassis " + chassis.getEqIdFull() + " PM about to expire this month, setting the BLIPS (setTranUnitFlexString12()) as M");

                    addDamagesToFederalInspExpiredChassis(chassis, truckTransaction);
                } else if (RECEIVAL_ERROR.equals(message)) {
                    LOGGER.info("Calling Add Damage");
                    addDamagesToFederalInspExpiredChassis(chassis, truckTransaction);
                    RoadBizUtil.appendExceptionChainAsWarnings(BizViolation.createFieldViolation(AllOtherFrameworkPropertyKeys.ERROR__NULL_MESSAGE, (BizViolation) null, ArgoGuiMetafield.EQ_FED_INSPECT_EXP,
                            "The Chassis (" + chassis.getEqIdFull() + ") PM Expired "));
                    LOGGER.info("Chassis "+chassis.getEqIdFull()+" PM Expired, setting the BLIPS (setTranUnitFlexString12()) as M");
                    truckTransaction.setTranUnitFlexString12("M");
                }

            } else {
                //no validation
            }

        }
        LOGGER.info("End MatsonAncValidateChassisInspectionDate");
    }

    static final String compareDateAndSetWarningMessageForDelivery(Date inInspectionDue, Date inTimeNow) {
        if (inInspectionDue != null && inTimeNow != null) {
            if (inTimeNow.after(inInspectionDue)) {
                return MONTH_ERROR;
            }
            if (inInspectionDue.getYear() >= inTimeNow.getYear()) {
                if (inInspectionDue.getMonth() >= inTimeNow.getMonth()) {
                    if ((inInspectionDue.getMonth() == inTimeNow.getMonth()) && inTimeNow.getDate() >= 15) {
                        return WARNING_MESSAGE;
                    } else if ((inInspectionDue.getMonth() == inTimeNow.getMonth()) && inTimeNow.getDate() < 15) {
                        return SUCCESS;
                    }
                } else if ((inInspectionDue.getYear() <= inTimeNow.getYear()) && (inInspectionDue.getMonth() < inTimeNow.getMonth())) {
                    return MONTH_ERROR;
                }
            } else if (inInspectionDue.getYear() < inTimeNow.getYear()) {
                return YEAR_ERROR;
            }
        } else {
            return INSPECTIONDUE_NULL;
        }
    }

    static final String compareDateAndSetWarningMessageForReceival(Date inInspectionDue, Date inTimeNow) {
        if (inInspectionDue != null && inTimeNow != null) {
            if (inInspectionDue.getYear() == inTimeNow.getYear()) {
                if (inInspectionDue.getMonth() == inTimeNow.getMonth()) {
                    return WARNING_MESSAGE;
                } else if (inInspectionDue.getMonth() < inTimeNow.getMonth()) {
                    return RECEIVAL_ERROR;
                }
            } else if (inInspectionDue.getYear() < inTimeNow.getYear()) {
                return RECEIVAL_ERROR;
            } else {
                return SUCCESS;
            }
        } else {
            return INSPECTIONDUE_NULL;
        }
    }

    final void addDamagesToFederalInspExpiredChassis(Chassis inChassis, TruckTransaction truckTransaction) {
        if (truckTransaction.getTranUnit() != null && inChassis != null) {
            EquipDamageType eqdmgtyp = EquipDamageType.findOrCreateEquipDamageType("PM", "PM", EquipClassEnum.CHASSIS);
            EqComponent eqComponent = EqComponent.findOrCreateEqComponent("PM INSPECTION", "PM INSPECTION", EquipClassEnum.CHASSIS);
            EqDamageSeverityEnum eqdmgSeverity = EqDamageSeverityEnum.getEnum("MAJOR");
            UnitEquipment chassisEquipObj = null;
            if (truckTransaction.getTranUnit().isPrimaryEqAChassis()) {
                LOGGER.info("1 Adding damages to primary chassis -->" + truckTransaction.getTranUnit().getUnitPrimaryUe());
                truckTransaction.getTranUnit().getUnitPrimaryUe().addDamageItem(eqdmgtyp, eqComponent, eqdmgSeverity, new Date(), null);
                chassisEquipObj=truckTransaction.getTranUnit().getUnitPrimaryUe();
            } else {
                UnitEquipment chsUe = truckTransaction.getTranUnit().getUnitCarriageUe();
                LOGGER.info("2 Adding damages to carraige chassis -->" + chsUe);
                if (chsUe != null && chsUe.getUeEquipment() != null && chsUe.getUeEquipment().getEqIdFull().equalsIgnoreCase(inChassis.getEqIdFull())) {
                    LOGGER.info("2 Adding damages to chassis -->" + inChassis.getEqIdFull());
                    chsUe.addDamageItem(eqdmgtyp, eqComponent, eqdmgSeverity, new Date(), null);
                }
                chassisEquipObj = chsUe;
            }
            LOGGER.info("before saving chassisEquipObj-->" + chassisEquipObj);
            if (chassisEquipObj != null) {
                try {
                    HibernateApi.getInstance().saveOrUpdate(chassisEquipObj);
                } catch (Exception ex) {
                    LOGGER.error("Exception while adding damages to federal inspection date expired chassis ", ex);
                }
            }
        }
        /*if (inChassis != null) {
            EquipDamageType eqdmgtyp = EquipDamageType.findOrCreateEquipDamageType("PM", "PM", EquipClassEnum.CHASSIS);
            EqComponent eqComponent = EqComponent.findOrCreateEqComponent("PM INSPECTION", "PM INSPECTION", EquipClassEnum.CHASSIS);
            EqDamageSeverityEnum eqdmgSeverity = EqDamageSeverityEnum.getEnum("MAJOR");
            LOGGER.info("eqdmgtyp" + eqdmgtyp + "eqComponent-->" + eqComponent + "eqdmgSeverity->" + eqdmgSeverity);
            Equipment equip = Equipment.findEquipment(inChassis.getEqIdFull().toString());
            if (equip == null) {
                LOGGER.error("Adding damages failed equip found null" + equip);
                return;
            }
            LOGGER.info(" equip --> " + equip + "truckTransaction.getTranUnit().getUnitCarriageUe() " + truckTransaction.getTranUnit());
            UnitFinder unitFinder = (UnitFinder) Roastery.getBean(UnitFinder.BEAN_ID);
            Unit chsUnit = unitFinder.findActiveUnit(ContextHelper.getThreadComplex(), equip);
            LOGGER.info(" 1 chsUnit --> " + chsUnit + "truckTransaction.getTranUnit()-->" + truckTransaction.getTranUnit());
            Object chassisEquipObj = null;
            if (chsUnit == null && truckTransaction.getTranUnit() != null) {
                LOGGER.info(" 1 truckTransaction.getTranUnit().getUnitCarriageUe() --> " + truckTransaction.getTranUnit().getUnitCarriageUe()  +"isPrimaryEqAChassis()"+truckTransaction.getTranUnit().isPrimaryEqAChassis());
                if(truckTransaction.getTranUnit().isPrimaryEqAChassis()){
                    chassisEquipObj= truckTransaction.getTranUnit().getUnitPrimaryUe().addDamageItem(eqdmgtyp, eqComponent, eqdmgSeverity, new Date(), new Date());
                }
                if (truckTransaction.getTranUnit().getUnitCarriageUe() != null  && !truckTransaction.getTranUnit().isPrimaryEqAChassis()) {
                    truckTransaction.getTranUnit().getUnitCarriageUe().addDamageItem(eqdmgtyp, eqComponent, eqdmgSeverity, new Date(), new Date());
                    chassisEquipObj = truckTransaction.getTranUnit().getUnitCarriageUe();
                }
            }
            LOGGER.info(" chsUnit --> " + chsUnit + "chassisEquipObj-->" + chassisEquipObj);

            if (chsUnit == null) {
                LOGGER.error("Adding damages failed chassis unit found null" + chsUnit);
                return;
            }

            if (chsUnit.getUnitPrimaryUe() == null) {
                LOGGER.error("Adding damages failed unitequipment for chassis found null " + chsUnit);
                return;
            }

            if (chsUnit != null && chsUnit.getUnitPrimaryUe() != null && chassisEquipObj == null) {
                LOGGER.info(" Adding damages " + chsUnit);
                chsUnit.getUnitPrimaryUe().addDamageItem(eqdmgtyp, eqComponent, eqdmgSeverity, new Date(), new Date());
                chassisEquipObj = chsUnit.getUnitPrimaryUe();
            }

            LOGGER.info("before saving chassisEquipObj-->" + chassisEquipObj);
            if (chassisEquipObj != null) {
                try {
                    HibernateApi.getInstance().saveOrUpdate(chassisEquipObj);
                } catch (Exception ex) {
                    LOGGER.error("Exception while adding damages to federal inspection date expired chassis ", ex);
                }
            }
        }*/
    }
}


