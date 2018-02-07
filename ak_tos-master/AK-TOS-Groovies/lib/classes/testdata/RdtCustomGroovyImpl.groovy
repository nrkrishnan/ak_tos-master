import java.util.Map;

import com.navis.apex.business.model.GroovyInjectionBase;
import com.navis.framework.persistence.HibernateApi;
import com.navis.framework.portal.FieldChanges;
import com.navis.framework.util.BizViolation;
import com.navis.inventory.InventoryBizMetafield;
import com.navis.inventory.InventoryField;
import com.navis.inventory.business.units.EqBaseOrderItem;
import com.navis.inventory.business.units.Unit;
import com.navis.inventory.web.InventoryMobileUtil;
import com.navis.orders.business.eqorders.EquipmentOrderItem;

/**
 * This is an example Groovy class and is  not readily production deploable. The idea is to provide a simple template that can be extended by the
 * deployers. As the program below shows (1) Same (this) groovy class is called by each of the RDT progras (except Gate for which this plugin is not
 * supported). (2) Each call comes with a sete of parameters including the Application Names (Rail, Yard, Hatch, Rail Inv, Reefer etc) (3) While most
 * of the programs get an entity as part of the arguments, but entity is not guaranteed. Specially in case of rail inventory there can be situations
 * where entity is not creted because no action is needed on a particular slot or because user entered possibly a wrong container number which would
 * be caught later by the product validations. This class is called when RDT's submit (commit) changes The usecase is specfically for inspection data
 * that needs to go through additional validations that are not part of the product (out of the box)
 */

public class RdtCustomGroovyImpl extends GroovyInjectionBase {

    final String CLASS_NAME = "RdtCustomGroovyImpl: ";

    public void log(String inMsg) {
        super.log(CLASS_NAME + inMsg);
    }

    public void validateChanges(Map args) throws BizViolation {

        if (ignoreAllOverrideableErrors()) {
            log("skipping groovy validations as override flag is set");
            return;
        } else {
            if (args == null) {
                log("skipping groovy validations as required parameters are missing");
                return;
            }

            String appName = (String) args.get(InventoryBizMetafield.RDT_APPLICATION_NAME);
            if (appName == null) {
                log("skipping groovy validations as app is not set");
                return;
            }
            Object entity = args.get(InventoryBizMetafield.RDT_ENTITY);
            FieldChanges fcs = (FieldChanges) args.get(InventoryBizMetafield.RDT_FORM_CHANGES);

            log(appName);

            Unit unit;

            // Hatch Clerk Program
            if (appName.equals(InventoryMobileUtil.HATCH_CLERK_PROGRAM_NAME)) {
                doExtraValidations(entity, fcs, appName);

                // Rail or Yard Inspection Program get the same app name and we distinguish based on
                // the container location.
            } else if (appName.equals(InventoryMobileUtil.RAIL_OR_YARD_INSPECTION_PROGRAM_NAME)) {
                unit = (Unit) entity;
                if (unit.isUnitInYard()) {
                    log("Yard Inspection Program");
                } else {

                    log(" Rail Inspection Program");
                }
                doExtraValidations(entity, fcs, appName);

                // Rail Inventory program iterates over the rail car slots and makes
                // a decision about load, position correction, removal, bump, swap etc.
                // The example code below shows what is avalable in the parameters.
            } else if (appName.equals(InventoryMobileUtil.RAIL_INVENTORY_PROGRAM_NAME)) {
                unit = (Unit) entity;
                String newPosName = (String) fcs.getFieldChange(InventoryField.POS_NAME).getNewValue();
                String newUnitId = (String) fcs.getFieldChange(InventoryField.UNIT_ID).getNewValue();
                String oldUnitId = (String) fcs.getFieldChange(InventoryField.UNIT_ID).getPriorValue();
                log("=== Rail Inventory Program: New Unit Id: " + newUnitId);
                log("=== Rail Inventory Program: New Position " + newPosName);
                if (newUnitId == null || newUnitId.trim().length() == 0) {
                    log("Non-LOAD operation, returning without validations for: " + newPosName);
                    return;
                }

                if (newUnitId.equals(oldUnitId)) {
                    log("No change in the position, allowing the operation: " + newPosName);
                    return;
                }
                if (unit != null) {
                    log("TODO, Add additional Antwerp validation on unit " + newUnitId);
                    return;
                } else {
                    log("TODO, N4 Could not locate a Unit..Returning " + newUnitId);
                    return;
                }
            } else if (appName.equals(InventoryMobileUtil.REEFER_MONITORING_PROGRAM_NAME)) {
                doExtraValidations(entity, fcs, appName);
            } else {
                // this is for unit test purpose
                log("Uknown program type, skipping custom validations");
                println("Uknown program type, skipping custom validations");
                registerOverridableError("booking values don't seem to match with unit values");
            }
        }
    }

    /**
     * Internal Helper Method
     *
     * @param inEntity  entity being updated or loaded. Can be NULL
     * @param inFcs     field changes
     * @param inAppName Hand-held application ID.
     */
    void doExtraValidations(Object inEntity, com.navis.framework.portal.FieldChanges inFcs, String inAppName) {
        Unit unit;
        unit = (Unit) inEntity;

        // by reading from the unit we get the new values applied in memory (not persisted yet)
        Object seal = unit.getFieldValue(InventoryField.UNIT_SEAL_NBR1);
        log("========== Unit Seal1:" + seal);
        Boolean pm = (Boolean) unit.getFieldValue(InventoryField.UNIT_ARE_PLACARDS_MISMATCHED);
        log(" ========== Unit Placard Mismatch?:" + pm);
        if (pm != null && pm.booleanValue()) {
            registerOverridableError("Required placards don't match with observed placards");
        }

        Object oogBack = unit.getFieldValue(InventoryField.UNIT_OOG_BACK_CM);
        log("=========== Unit OOG Back:" + oogBack);

        String eqType = "";
        if (unit.getUnitPrimaryUe().getUeEquipment().getEqEquipType() != null) {
            eqType = (String) unit.getUnitPrimaryUe().getUeEquipment().getEqEquipType().getEqtypId();
            log("======== Unit ISO Code Being Updated: " + eqType);
        }

        // booking values
        if (unit.getUnitPrimaryUe().getUeDepartureOrderItem() != null) {
            EqBaseOrderItem bIso = unit.getUnitPrimaryUe().getUeDepartureOrderItem();

            EquipmentOrderItem eqoi = (EquipmentOrderItem) HibernateApi.getInstance().downcast(bIso, EquipmentOrderItem.class);
            log("======== Booking Reference : " + eqoi);
            if (eqoi != null) {
                String bIsoStr = eqoi.getEqoiSampleEquipType().getEqtypId();
                log("Booking ISO Code: " + bIsoStr);
                if (!bIsoStr.equals(eqType)) {
                    registerOverridableError("booking ISO Code: " + bIsoStr + " doesn't seem to match with unit value:" + eqType);
                }
            }
        }
    }
}
