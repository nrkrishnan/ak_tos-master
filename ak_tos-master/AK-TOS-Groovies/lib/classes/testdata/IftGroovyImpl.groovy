/*
 * Copyright (c) 2010 Zebra Technologies Corp. All Rights Reserved.
 * $Id: $
 */


import com.navis.apex.business.model.GroovyInjectionBase
import com.navis.argo.business.atoms.UnitCategoryEnum
import com.navis.argo.business.model.CarrierVisit
import com.navis.argo.business.model.Complex
import com.navis.argo.business.model.Facility
import com.navis.argo.business.model.LocPosition
import com.navis.framework.business.Roastery
import com.navis.framework.metafields.MetafieldId
import com.navis.framework.persistence.HibernateApi
import com.navis.framework.persistence.HibernatingEntity
import com.navis.framework.util.BizViolation
import com.navis.inventory.InventoryField
import com.navis.inventory.InventoryPropertyKeys
import com.navis.inventory.business.api.UnitField
import com.navis.inventory.business.api.UnitManager
import com.navis.inventory.business.units.*
import com.navis.inventory.web.InventoryGuiMetafield
import com.navis.orders.business.eqorders.Booking
import com.navis.services.business.event.GroovyEvent
import com.navis.spatial.business.model.AbstractBin
import com.navis.spatial.business.model.block.BinModelHelper
import org.apache.log4j.Level
import org.apache.log4j.Logger

/**
 * This class contains custom code for Maher IFT
 *
 * Note to implementers/deployers
 * For the export process to work, you need to do the following
 *
 * 1. Flex Field Mapping
 * When exports are transfered, the requirement is to wipe out the Gross weight, Booking and Outbound
 * carrier information and copy them to FlexFields. The default mapping provided maps
 * Gross Weight --> Unit Flex String 02
 * Outbound Carrier --> Unit Flex String 03
 * Booking --> Unit Flex String 04.
 * If in the implementation these fields are used up for some other purpose, please map to available flex fields
 * Look for FLEX_FIELD_MAPPING.put and replace as appropriate.
 *
 * 2. Track side location
 * The requirement is that exports that are in the track side locations cannot be transfered from the Rail Facility
 * The out-of-box implementation identifies track side as yard blocks whose block id is T01 thru T18. If in the
 * production system tracksides have different Block Ids, change the implementation of isTrackside and list all
 * the blocks that represent track side location.
 *
 * 3. For Imports
 * By default we transfer to the Facility MMR. If the topology is configured differently, you need to change the value of
 * MMR_FACILITY_ID
 *
 * 4. WI Verification
 * A Clerk is supposed to verify a WI and this essentially sets a Unit Flex Field to 'Y'. This default implementation
 * uses UNIT_FLEX_STRING06. If you need to use some other flex field, please change XFER_VERIFIED_FLEX_STRING definition below
 */

class IftGroovyImpl extends GroovyInjectionBase {

  public IftGroovyImpl() {
    super();
    FLEX_FIELD_MAPPING = new HashMap();
    FLEX_FIELD_MAPPING.put(InventoryField.UNIT_GOODS_AND_CTR_WT_KG, InventoryField.UNIT_FLEX_STRING02);
    FLEX_FIELD_MAPPING.put(InventoryField.UFV_INTENDED_OB_CV, InventoryField.UNIT_FLEX_STRING03);
    FLEX_FIELD_MAPPING.put(InventoryField.UE_DEPARTURE_ORDER_ITEM, InventoryField.UNIT_FLEX_STRING04);
  }

  /**
   * This method will be invoked from the business layer to perform pre-transfer validations.
   */
  public void preTransferValidate(Map inArgs) throws BizViolation {
    LOGGER.info("In preTransferValidate");
    UnitFacilityVisit ufv = (UnitFacilityVisit) inArgs.get(UnitField.UNIT_ACTIVE_UFV);
    if (!ufv instanceof UnitFacilityVisit) {
      registerError("No Ufv was passed to preTransferValidate");
      return;
    }

    Facility targetFacility = (Facility) inArgs.get(InventoryGuiMetafield.TO_FACILITY);
    if (targetFacility == null) {
      registerError("No Target Facility was passed to preTransferValidate");
      return;
    }

    Unit unit = ufv.getUfvUnit();
    String cntrId = unit.getUnitId();
    Facility currentFacility = ufv.getUfvFacility();
    if (currentFacility == null) {
      registerError("Failed to determing current facility of Ufv");
      return;
    }

    if (currentFacility.equals(targetFacility)) {
      registerError("Prevented attempt to transfer Unit: " + cntrId + " to its current facility");
      return;
    }

    if (UnitCategoryEnum.EXPORT.equals(unit.getUnitCategory())) {
      LOGGER.info("Performing pre-transfer validations for Exports. Unit: " + cntrId);
      verifyExportAssignedToBooking(unit, targetFacility);
      verifyExportNotInTrackSide(ufv);
      LOGGER.info("Completed pre-transfer validations for Exports. Unit: " + cntrId);
    }

    LOGGER.info("Leaving preTransferValidate");
  }

  /**
   * This method will be invoked from the business layer to perform post transfer actions.
   */
  public void postTransferPerformActions(Map inArgs) {
    LOGGER.info("In postTransferPerformActions");
    Object entity = (Unit) inArgs.get(UnitField.UFV_UNIT);
    if (!entity instanceof Unit) {
      registerError("No Unit was passed to postTransferPerformActions");
      return;

    }
    Unit unit = (Unit) entity;
    String cntrId = unit.getUnitId();
    UnitFacilityVisit activeUfv = unit.getUnitActiveUfvNowActive();
    /*Post-Transfer Groovy Customizations for Maher on When Transferring Export to Marine Facility (Fleet Street)
      When an export is transferred from the Rail facility to Marine facility copy the unit attributes:
         * Export Booking
         * Gross Weight
         * Outbound Carrier Visit
    * to unit flex string attributes and then reset the unit attributes to null.
    */
    if (UnitCategoryEnum.EXPORT.equals(unit.getUnitCategory())) {
      //Gross Weight
      copyFieldValue(unit, unit, InventoryField.UNIT_GOODS_AND_CTR_WT_KG, unit.getUnitGoodsAndCtrWtKg());

      //Outbound Carrier Visit
      CarrierVisit genVessel = CarrierVisit.getGenericVesselVisit(unit.getUnitComplex());
      copyFieldValue(activeUfv, unit, InventoryField.UFV_INTENDED_OB_CV, genVessel);
      copyFieldValue(activeUfv, unit, InventoryField.UFV_ACTUAL_OB_CV, genVessel);

      //Booking
      UnitEquipment ue = unit.getUnitPrimaryUe();
      copyFieldValue(ue, unit, InventoryField.UE_DEPARTURE_ORDER_ITEM, null);
    }

    LOGGER.info("Leaving postTransferPerformActions");
  }

  /**
   * This method will be called when a Yard move occurs for a unit. 
   */
  public void transferImport(GroovyEvent inEvent) {
    Unit unit = (Unit) inEvent.getEntity();
    if (unit == null) {
      LOGGER.error("handleYardMove failed to resolve a valid Unit from the Event Entity");
      return;
    }

    UnitFacilityVisit ufv = unit.getUnitActiveUfvNowActive();
    String cntrId = unit.getUnitId();
    Complex unitComplex = unit.getUnitComplex();
    Facility mmrFacility = Facility.findFacility(MMR_FACILITY_ID, unitComplex)
    if (mmrFacility == null) {
      LOGGER.error("handleYardMove failed to find Facility: " + MMR_FACILITY_ID + " in the Complex for Unit: "
              + cntrId);
      return;
    }

    LOGGER.info("Initiating transfer of Unit " + cntrId + " to Facility " + MMR_FACILITY_ID);
    try {
      UnitManager unitMgr = (UnitManager) Roastery.getBean(UnitManager.BEAN_ID);
      unitMgr.transferUnitToFacility(ufv, mmrFacility);
      LOGGER.info("Completed transfer of Unit " + cntrId + " to Facility " + MMR_FACILITY_ID);
    } catch (BizViolation bv) {
      StringBuffer msg = new StringBuffer();
      msg.append("Encountered exception while transferring Unit: ");
      msg.append(cntrId);
      msg.append(" Facility: ");
      msg.append(MMR_FACILITY_ID);
      msg.append(" Exception: ");
      msg.append(bv);
      LOGGER.error(msg.toString());
    }
  }

  /*
  Copy value of field 'inField' in Entity 'inFromEntity' to a flexfield of the toEntity and then set
  inFromEntity.inField to inNewValue :)
   */

  private void copyFieldValue(HibernatingEntity inFromEntity,
                              HibernatingEntity toEntity,
                              MetafieldId inField,
                              Object inNewValue) {
    String currValue = getFieldValue(inFromEntity, inField);
    MetafieldId flexFieldForField = (MetafieldId) FLEX_FIELD_MAPPING.get(inField);
    if (flexFieldForField != null) {
      LOGGER.info("Setting " + flexFieldForField + " to " + currValue);
      toEntity.setFieldValue(flexFieldForField, currValue);
    }
    else {
      LOGGER.error("Failed to find Flex Field mapping for " + inField);
    }

    LOGGER.info("Setting " + inField + " to " + inNewValue);
    inFromEntity.setFieldValue(inField, inNewValue);
  }

  private String getFieldValue(HibernatingEntity inEntity, MetafieldId inField) {
    if (InventoryField.UNIT_GOODS_AND_CTR_WT_KG.equals(inField)) {
      Unit unit = (Unit) inEntity;
      Double grossWeight = unit.getUnitGoodsAndCtrWtKg();
      if (grossWeight != null) {
        return grossWeight.toString();
      }
    }

    if (InventoryField.UFV_INTENDED_OB_CV.equals(inField)) {
      UnitFacilityVisit ufv = (UnitFacilityVisit) inEntity;
      CarrierVisit cv = ufv.getUfvObCv();
      if (cv != null) {
        return cv.getCvId();
      }
    }

    if (InventoryField.UE_DEPARTURE_ORDER_ITEM.equals(inField)) {
      UnitEquipment ue = (UnitEquipment) inEntity;
      EqBaseOrderItem eqboi = ue.getUeDepartureOrderItem();
      if (eqboi != null) {
        EqBaseOrder booking = eqboi.getEqboiOrder();
        if (booking != null) {
          return booking.getEqboNbr();
        }
      }
    }

    return null;
  }

  //If export,  Do not permit the transfer if the unit not assigned to a booking for a vessel calling the transferee facility

  private void verifyExportAssignedToBooking(final Unit inUnit, final Facility inTargetFacility) throws BizViolation {
    String cntrId = inUnit.getUnitId();
    if (!inUnit.isAssignedForBooking()) {
      throw BizViolation.create(InventoryPropertyKeys.PRETRANSFERVALIDATE_EXPORT_NOT_ASSIGNED_TO_BOOKING, null, cntrId);
    }

    EqBaseOrderItem orderItem = inUnit.getUnitPrimaryUe().getUeDepartureOrderItem();
    Booking booking = (Booking) HibernateApi.getInstance().downcast(orderItem.getEqboiOrder(), Booking.class);
    CarrierVisit eqoVesselVisit = booking.getEqoVesselVisit();
    if (!inTargetFacility.equals(eqoVesselVisit.getCvFacility())) {
      throw BizViolation.create(InventoryPropertyKeys.PRETRANSFERVALIDATE_EXPORT_ASSIGNED_TO_WRONG_BOOKING, null, cntrId, eqoVesselVisit.getCvId(),
              booking.getEqboNbr());
    }
  }

  /*
  If export, Do not permit the transfer if the current position is in a specified set of yard blocks (which define trackside locations).
  Per Rohan Patel
  Currently they don't have trackside slotted positons even though the do tend to line up containers alongside the train platforms
  based on which platform will be loaded.
  There are 18 heaps labeled T1 to T18 that represent trackside dumps. Will heaps be an issue?
  Maher rail ops is quite "manual" compared to marine terminal. How hard would it be if they migrated to a slotted trackside blocks?
  */

  private void verifyExportNotInTrackSide(final UnitFacilityVisit inUfv) {
    Unit unit = inUfv.getUfvUnit();
    String cntrId = unit.getUnitId();
    LocPosition pos = inUfv.getUfvLastKnownPosition()
    if (pos == null) {
      registerError("Last known position of " + cntrId + " is null");
      return;
    }

    if (!pos.isYardPosition()) {
      registerError("Last known position of " + cntrId + " is not in Yard");
      return;
    }

    AbstractBin yardBin = pos.getPosBin();
    if (yardBin == null) {
      registerError("Failed to resolve last known position of " + cntrId + " to a valid Yard Location");
      return;
    }

    AbstractBin yardBlock = BinModelHelper.getBlockFromModelBin(yardBin);
    if (yardBlock == null) {
      registerError("Failed to resolve last known position of " + cntrId + " to a valid Yard Block");
      return;
    }

    if (isTrackside(yardBlock)) {
      registerError("IFT failed for " + cntrId + ". It is in a trackside location.");
      return;
    }
  }

  //There are 18 heaps labeled T1 to T18 that represent trackside dumps

  private boolean isTrackside(final AbstractBin inBlock) {
    String blockName = inBlock.getAbnName();
    final String[] trackSideBlockNames = [
            "T01", "T02", "T03", "T04", "T05", "T06", "T07",
            "T08", "T09", "T10", "T11", "T12",
            "T13", "T14", "T15", "T16", "T17", "T18"];

    for (ii in 0..trackSideBlockNames.length - 1) {
      if (trackSideBlockNames[ii].equalsIgnoreCase(blockName)) {
        return true;
      }
    }
    return false;
  }

  static {
    LOGGER.setLevel(Level.INFO);
  }

  private final Map FLEX_FIELD_MAPPING;
  private static final String MMR_FACILITY_ID = "MMR";
  private static final String XFER_MOVE_VERIFIED_VALUE = "Y";
  private static final MetafieldId XFER_VERIFIED_FLEX_STRING = InventoryField.UNIT_FLEX_STRING06;
  private static final Logger LOGGER = Logger.getLogger(IftGroovyImpl.class);
}