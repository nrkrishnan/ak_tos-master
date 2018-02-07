/*
 * Copyright (c) 2013 Navis LLC. All Rights Reserved.
 *
 */



package extension.system.cas

import com.navis.argo.ArgoField
import com.navis.argo.ArgoRefField
import com.navis.argo.ContextHelper
import com.navis.argo.business.api.IImpediment
import com.navis.argo.business.api.ServicesManager
import com.navis.argo.business.atoms.FlagPurposeEnum
import com.navis.argo.business.atoms.LocTypeEnum
import com.navis.argo.business.atoms.WiMoveKindEnum
import com.navis.argo.business.atoms.WiMoveStageEnum
import com.navis.argo.business.model.CarrierVisit
import com.navis.argo.business.model.VisitDetails
import com.navis.argo.business.model.Yard
import com.navis.argo.business.snx.ArgoPropertyResolverProvider
import com.navis.argo.business.xps.model.Che
import com.navis.argo.business.xps.model.PointOfWork
import com.navis.control.business.taskhandlers.qc.QcTasks
import com.navis.external.framework.AbstractExtensionCallback
import com.navis.framework.business.Roastery
import com.navis.framework.metafields.MetafieldId
import com.navis.framework.metafields.MetafieldIdFactory
import com.navis.framework.persistence.Entity
import com.navis.framework.persistence.HibernateApi
import com.navis.framework.portal.Ordering
import com.navis.framework.portal.QueryUtils
import com.navis.framework.portal.query.DomainQuery
import com.navis.framework.portal.query.ObsoletableFilterFactory
import com.navis.framework.portal.query.PredicateFactory
import com.navis.framework.portal.query.PredicateIntf
import com.navis.framework.query.common.api.QueryResult
import com.navis.framework.util.AtomizedEnum
import com.navis.framework.util.BizViolation
import com.navis.framework.util.ValueHolder
import com.navis.framework.util.internationalization.PropertyKey
import com.navis.framework.util.message.MessageCollector
import com.navis.framework.util.message.MessageLevel
import com.navis.inventory.*
import com.navis.inventory.business.api.UnitField
import com.navis.inventory.business.atoms.EqUnitRoleEnum
import com.navis.inventory.business.atoms.TwinWithEnum
import com.navis.inventory.business.moves.IWorkFinder
import com.navis.inventory.business.moves.MoveEvent
import com.navis.inventory.business.moves.WorkQueue
import com.navis.inventory.business.units.Unit
import com.navis.inventory.business.units.UnitEquipDamageItem
import com.navis.inventory.business.units.UnitEquipDamages
import com.navis.mensa.business.atoms.CraneLiftModeEnum
import com.navis.services.business.rules.EventType
import com.navis.spatial.BinEntity
import com.navis.spatial.BinField
import com.navis.spatial.BlockField
import com.navis.spatial.business.atoms.BinNameTypeEnum
import com.navis.vessel.VesselField
import com.navis.xpscache.business.atoms.EquipBasicLengthEnum
import groovy.xml.MarkupBuilder
import org.apache.commons.collections.map.MultiKeyMap
import org.apache.commons.lang.StringUtils
import org.apache.log4j.Level
import org.apache.log4j.LogManager
import org.apache.log4j.Logger
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

import java.text.SimpleDateFormat

/**
 * This class contains helper methods for Crane Automation API
 *
 * @author <a href="mailto:arvinder.brar@navis.com">Arvinder Brar</a>, 1/14/13
 */
class CasHelper extends AbstractExtensionCallback{
  //Outbound service name as defined in the integration services screen
  public final String CAS_OUTBOUND = "CAS-Outbound"

  //MetafieldIds for CAS
  public final MetafieldId UNIT_ID = UnitField.UFV_UNIT_ID;
  public final MetafieldId UNIT_GKEY = MetafieldIdFactory.getCompoundMetafieldId(UnitField.UFV_UNIT, UnitField.UNIT_GKEY);
  public final MetafieldId GROSS_WEIGHT = MetafieldIdFactory.getCompoundMetafieldId(UnitField.UFV_UNIT, UnitField.UNIT_GOODS_AND_CTR_WT_KG);
  public final MetafieldId GROSS_WEIGHT_ADVISED =
    MetafieldIdFactory.getCompoundMetafieldId(UnitField.UFV_UNIT, UnitField.UNIT_GOODS_AND_CTR_WT_KG_ADVISED);
  public final MetafieldId GROSS_WEIGHT_GATE_MEASURED =
    MetafieldIdFactory.getCompoundMetafieldId(UnitField.UFV_UNIT, UnitField.UNIT_GOODS_AND_CTR_WT_KG_GATE_MEASURED);
  public final MetafieldId GROSS_WEIGHT_YARD_MEASURED =
    MetafieldIdFactory.getCompoundMetafieldId(UnitField.UFV_UNIT, UnitField.UNIT_GOODS_AND_CTR_WT_KG_YARD_MEASURED);
  //Equipment
  public final MetafieldId PRIMARY_EQ_GKEY = UnitField.UFV_PRIMARY_EQ_GKEY;
  public final MetafieldId PRIMARY_EQ_CLASS = UnitField.UFV_PRIMARY_EQ_CLASS;
  public final MetafieldId PRIMARY_EQS_GKEY = UnitField.UFV_PRIMARY_EQS_GKEY;
  public final MetafieldId EQTYPE_ID = UnitField.UFV_PRIMARY_EQTYPE_ID;
  public final MetafieldId EQ_BASIC_LENGTH = UnitField.UFV_PRIMARY_EQ_BASIC_LENGTH;
  public final MetafieldId EQ_LENGTH_MM = UnitField.UFV_PRIMARY_EQ_LENGTH_MM;
  public final MetafieldId EQ_WIDTH_MM = UnitField.UFV_PRIMARY_EQ_WIDTH_MM;
  public final MetafieldId EQ_HEIGHT_MM = UnitField.UFV_PRIMARY_EQ_HEIGHT_MM;
  public final MetafieldId EQ_TANK_RAILS = MetafieldIdFactory.getCompoundMetafieldId(UnitField.UFV_PRIMARY_EQ, ArgoRefField.EQ_TANK_RAILS);

  //OOG
  public final MetafieldId IS_OOG = MetafieldIdFactory.getCompoundMetafieldId(UnitField.UFV_UNIT, UnitField.UNIT_IS_OOG);
  public final MetafieldId OOG_FRONT = MetafieldIdFactory.getCompoundMetafieldId(UnitField.UFV_UNIT, UnitField.UNIT_OOG_FRONT_CM);
  public final MetafieldId OOG_BACK = MetafieldIdFactory.getCompoundMetafieldId(UnitField.UFV_UNIT, UnitField.UNIT_OOG_BACK_CM);
  public final MetafieldId OOG_TOP = MetafieldIdFactory.getCompoundMetafieldId(UnitField.UFV_UNIT, UnitField.UNIT_OOG_TOP_CM);
  public final MetafieldId OOG_LEFT = MetafieldIdFactory.getCompoundMetafieldId(UnitField.UFV_UNIT, UnitField.UNIT_OOG_LEFT_CM);
  public final MetafieldId OOG_RIGHT = MetafieldIdFactory.getCompoundMetafieldId(UnitField.UFV_UNIT, UnitField.UNIT_OOG_RIGHT_CM);
  //Seals
  public final MetafieldId IS_CTR_SEALED = MetafieldIdFactory.getCompoundMetafieldId(UnitField.UFV_UNIT, UnitField.UNIT_IS_CTR_SEALED);
  public final MetafieldId SEAL1 = MetafieldIdFactory.getCompoundMetafieldId(UnitField.UFV_UNIT, UnitField.UNIT_SEAL_NBR1);
  public final MetafieldId SEAL2 = MetafieldIdFactory.getCompoundMetafieldId(UnitField.UFV_UNIT, UnitField.UNIT_SEAL_NBR2);
  public final MetafieldId SEAL3 = MetafieldIdFactory.getCompoundMetafieldId(UnitField.UFV_UNIT, UnitField.UNIT_SEAL_NBR3);
  public final MetafieldId SEAL4 = MetafieldIdFactory.getCompoundMetafieldId(UnitField.UFV_UNIT, UnitField.UNIT_SEAL_NBR4);
  //Damages
  public final MetafieldId PRIMARY_DAMAGES_GKEY =
    MetafieldIdFactory.getCompoundMetafieldId(UnitField.UFV_UNIT, UnitField.UNIT_PRIMARY_DAMAGES);
  //Other
  public final MetafieldId UFV_GKEY = UnitField.UFV_GKEY;
  //Flags added by unit  (see 4)
  //Placards added by unit gkey (see 3)
  //Location
  public final MetafieldId UFV_TRANSIT_STATE = UnitField.UFV_TRANSIT_STATE;
  public final MetafieldId LOC_TYPE = UnitField.UFV_POS_LOC_TYPE;
  public final MetafieldId LOC_ID = UnitField.UFV_POS_LOC_ID;
  public final MetafieldId SLOT = UnitField.UFV_POS_SLOT;
  public final MetafieldId ORIENTATION = UnitField.UFV_POS_ORIENTATION;

  public final MetafieldId WQ_CRANE_ID =
    MetafieldIdFactory.getCompoundMetafieldId(MovesField.WQ_FIRST_RELATED_SHIFT,
            MetafieldIdFactory.getCompoundMetafieldId(ArgoField.WORKSHIFT_OWNER_POW, ArgoField.POINTOFWORK_NAME));
  public final MetafieldId WQ_CARRIER_VISIT_ID =
    MetafieldIdFactory.getCompoundMetafieldId(MovesField.WQ_FIRST_RELATED_SHIFT, ArgoField.WORKSHIFT_VISIT);
  public final MetafieldId WI_UFV_GKEY = MetafieldIdFactory.getCompoundMetafieldId(MovesField.WI_UYV,
          MetafieldIdFactory.getCompoundMetafieldId(InventoryField.UYV_UFV, InventoryField.UFV_GKEY));
  public final MetafieldId WI_UYV_GKEY = MetafieldIdFactory.getCompoundMetafieldId(MovesField.WI_UYV,InventoryField.UYV_GKEY);
  public final MetafieldId WI_WQ_GKEY = MetafieldIdFactory.getCompoundMetafieldId(MovesField.WI_WORK_QUEUE, MovesField.WQ_GKEY);
  public final MetafieldId WI_UNIT_ID = MetafieldIdFactory.getCompoundMetafieldId(MovesField.WI_UYV,
          MetafieldIdFactory.getCompoundMetafieldId(InventoryField.UYV_UFV,
                  MetafieldIdFactory.getCompoundMetafieldId(InventoryField.UFV_UNIT, InventoryField.UNIT_ID)));
  //WI position
  public final MetafieldId WI_POS_LOC_TYPE = MetafieldIdFactory.getCompoundMetafieldId(MovesField.WI_POSITION, InventoryField.POS_LOC_TYPE);
  public final MetafieldId WI_POS_LOC_ID = MetafieldIdFactory.getCompoundMetafieldId(MovesField.WI_POSITION, InventoryField.POS_LOC_ID);
  public final MetafieldId WI_POS_SLOT = MetafieldIdFactory.getCompoundMetafieldId(MovesField.WI_POSITION, InventoryField.POS_SLOT);

  public final MetafieldId UE_EQ_ID_FULL = UnitField.UE_EQ_ID_FULL;
  public final MetafieldId UE_EQ_EQTYPE_ID = UnitField.UE_EQ_EQTYPE_ID;
  public final MetafieldId UE_EQ_LENGTH_MM = UnitField.UE_EQ_LENGTH_MM;
  public final MetafieldId UE_EQ_HEIGHT_MM = MetafieldIdFactory.getCompoundMetafieldId(UnitField.UE_EQUIPMENT, ArgoRefField.EQ_HEIGHT_MM);
  public final MetafieldId UE_EQ_WIDTH_MM = MetafieldIdFactory.getCompoundMetafieldId(UnitField.UE_EQUIPMENT, ArgoRefField.EQ_WIDTH_MM);
  public final MetafieldId UE_EQ_ROLE = InventoryField.UE_EQ_ROLE;
  public final MetafieldId IS_BUNDLE = MetafieldIdFactory.getCompoundMetafieldId(UnitField.UFV_UNIT, UnitField.UNIT_IS_BUNDLE);

  public final MetafieldId HAZARDS_GKEY = MetafieldIdFactory.getCompoundMetafieldId(UnitField.UFV_HAZARDS, InventoryField.HZRD_GKEY);

  public final MetafieldId CAS_TRANSACTION_REFERENCE = InventoryField.UFV_CAS_TRANSACTION_REFERENCE;
  public final MetafieldId CAS_UNIT_REFERENCE = InventoryField.UFV_CAS_UNIT_REFERENCE;

  public final MetafieldId OBSPLACARD_PLACARD_TEXT = MetafieldIdFactory.getCompoundMetafieldId(InventoryField.OBSPLACARD_PLACARD,
          InventoryField.PLACARD_TEXT);
  /*Position fields - current ufv position*/
  //Block fields
  public final MetafieldId CURR_POS_BLOCK_NAME = MetafieldIdFactory.getCompoundMetafieldId(UnitField.UFV_POS_GRANDPARENT_BIN, BinField.ABN_NAME);
  public final MetafieldId CURR_POS_BLOCK_CONV_ID_ROW_GKEY = MetafieldIdFactory.getCompoundMetafieldId(
    MetafieldIdFactory.getCompoundMetafieldId(UnitField.UFV_POS_GRANDPARENT_BIN, BlockField.ABL_CONV_ID_ROW_STD), BinField.BNT_GKEY);
  public final MetafieldId CURR_POS_BLOCK_CONV_ID_ROW_ALT_GKEY = MetafieldIdFactory.getCompoundMetafieldId(
    MetafieldIdFactory.getCompoundMetafieldId(UnitField.UFV_POS_GRANDPARENT_BIN, BlockField.ABL_CONV_ID_ROW_ALT), BinField.BNT_GKEY);
  public final MetafieldId CURR_POS_BLOCK_CONV_ID_TIER_GKEY = MetafieldIdFactory.getCompoundMetafieldId(
    MetafieldIdFactory.getCompoundMetafieldId(UnitField.UFV_POS_GRANDPARENT_BIN, BlockField.ABL_CONV_ID_TIER), BinField.BNT_GKEY);
  //Section
  public final MetafieldId CURR_POS_ROW_INDEX = MetafieldIdFactory.getCompoundMetafieldId(UnitField.UFV_POS_PARENT_BIN, BlockField.ASN_ROW_INDEX);
  public final MetafieldId CURR_POS_ROW_CONV_ID_COL_GKEY = MetafieldIdFactory.getCompoundMetafieldId(
          MetafieldIdFactory.getCompoundMetafieldId(UnitField.UFV_POS_PARENT_BIN, BlockField.ASN_CONV_ID_COL), BinField.BNT_GKEY);
  //The following is applicable to ship sections only
  public final MetafieldId CURR_POS_CELL_SECTION_CONV_ID_TIER_GKEY = MetafieldIdFactory.getCompoundMetafieldId(
    MetafieldIdFactory.getCompoundMetafieldId(UnitField.UFV_POS_PARENT_BIN, VesselField.CSN_CONV_ID_TIER), BinField.BNT_GKEY);
  //Stack
  public final MetafieldId CURR_POS_COLUMN_INDEX = MetafieldIdFactory.getCompoundMetafieldId(UnitField.UFV_POS_BIN, BlockField.AST_COL_INDEX);
  //Tier
  public final MetafieldId CURR_POS_TIER = UnitField.UFV_POS_TIER;

/*Position fields - planned ufv position from WI*/
  public final MetafieldId WI_POS_PARENT_BIN = MetafieldIdFactory.getCompoundMetafieldId(
          UnitField.WI_POS_BIN, BinField.ABN_PARENT_BIN);
  public final MetafieldId WI_POS_GRANDPARENT_BIN = MetafieldIdFactory.getCompoundMetafieldId(
          WI_POS_PARENT_BIN, BinField.ABN_PARENT_BIN);
  public final MetafieldId WI_EQ_BASIC_LENGTH = MetafieldIdFactory.getCompoundMetafieldId(UnitField.WI_UNIT, UnitField.UNIT_PRIMARY_EQ_BASIC_LENGTH);
  //Block fields
  public final MetafieldId WI_POS_BLOCK_NAME = MetafieldIdFactory.getCompoundMetafieldId(WI_POS_GRANDPARENT_BIN, BinField.ABN_NAME);
  public final MetafieldId WI_POS_BLOCK_CONV_ID_ROW_GKEY = MetafieldIdFactory.getCompoundMetafieldId(
          MetafieldIdFactory.getCompoundMetafieldId(WI_POS_GRANDPARENT_BIN, BlockField.ABL_CONV_ID_ROW_STD), BinField.BNT_GKEY);
  public final MetafieldId WI_POS_BLOCK_CONV_ID_ROW_ALT_GKEY = MetafieldIdFactory.getCompoundMetafieldId(
          MetafieldIdFactory.getCompoundMetafieldId(WI_POS_GRANDPARENT_BIN, BlockField.ABL_CONV_ID_ROW_ALT), BinField.BNT_GKEY);
  public final MetafieldId WI_POS_BLOCK_CONV_ID_TIER_GKEY = MetafieldIdFactory.getCompoundMetafieldId(
          MetafieldIdFactory.getCompoundMetafieldId(WI_POS_GRANDPARENT_BIN, BlockField.ABL_CONV_ID_TIER), BinField.BNT_GKEY);
  //Section
  public final MetafieldId WI_POS_ROW_INDEX = MetafieldIdFactory.getCompoundMetafieldId(WI_POS_PARENT_BIN, BlockField.ASN_ROW_INDEX);
  public final MetafieldId WI_POS_ROW_CONV_ID_COL_GKEY = MetafieldIdFactory.getCompoundMetafieldId(
          MetafieldIdFactory.getCompoundMetafieldId(WI_POS_PARENT_BIN, BlockField.ASN_CONV_ID_COL), BinField.BNT_GKEY);
  //The following is applicable to ship sections only
  public final MetafieldId WI_POS_CELL_SECTION_CONV_ID_TIER_GKEY = MetafieldIdFactory.getCompoundMetafieldId(
          MetafieldIdFactory.getCompoundMetafieldId(WI_POS_PARENT_BIN, VesselField.CSN_CONV_ID_TIER), BinField.BNT_GKEY);
  //Stack
  public final MetafieldId WI_POS_COLUMN_INDEX = MetafieldIdFactory.getCompoundMetafieldId(UnitField.WI_POS_BIN, BlockField.AST_COL_INDEX);
  //Tier
  public final MetafieldId WI_POS_TIER = MetafieldIdFactory.getCompoundMetafieldId(MovesField.WI_POSITION, InventoryField.POS_TIER);

  /**
   * Creates the scalar query for CAS unit(s)
   * @param inPrimaryKeys primary keys for the units
   * @return domain query
   */
  public DomainQuery createUnitScalarQuery(Serializable[] inPrimaryKeys) {
    PredicateIntf predicate = PredicateFactory.in(UFV_GKEY, inPrimaryKeys);
    List<PredicateIntf> predicateList = new ArrayList<PredicateIntf>();
    predicateList.add(predicate);
    return createUnitScalarQuery(predicateList);
  }
  /**
   * Creates the scalar query for CAS unit(s)
   * @param inPrimaryKeys primary keys for the units
   * @return domain query
   */
  public DomainQuery createUnitScalarQuery(List<PredicateIntf> inPredicateList) {
    DomainQuery dq = getUfvDomainQueryWithoutPredicate()
    for (PredicateIntf predicate : inPredicateList) {
      dq.addDqPredicate(predicate);
    }
    dq.addDqOrdering(Ordering.desc(MetafieldId.PRIMARY_KEY));
    return dq;
  }

  protected DomainQuery getUfvDomainQueryWithoutPredicate() {
    DomainQuery dq = QueryUtils.createDomainQuery(InventoryEntity.UNIT_FACILITY_VISIT)
            .addDqField(UNIT_ID)
            .addDqField(UNIT_GKEY)
            .addDqField(LOC_TYPE)
            .addDqField(LOC_ID)
            .addDqField(CURR_POS_BLOCK_NAME)
            .addDqField(CURR_POS_BLOCK_CONV_ID_ROW_GKEY)
            .addDqField(CURR_POS_BLOCK_CONV_ID_ROW_ALT_GKEY)
            .addDqField(CURR_POS_BLOCK_CONV_ID_TIER_GKEY)
            .addDqField(CURR_POS_ROW_INDEX)
            .addDqField(CURR_POS_ROW_CONV_ID_COL_GKEY)
            .addDqField(CURR_POS_CELL_SECTION_CONV_ID_TIER_GKEY)
            .addDqField(CURR_POS_COLUMN_INDEX)
            .addDqField(CURR_POS_TIER)
            .addDqField(EQ_BASIC_LENGTH)
            .addDqField(SLOT)
            .addDqField(ORIENTATION)
            .addDqField(UFV_TRANSIT_STATE)
            .addDqField(SEAL1)
            .addDqField(SEAL2)
            .addDqField(SEAL3)
            .addDqField(SEAL4)
            .addDqField(GROSS_WEIGHT)
            .addDqField(GROSS_WEIGHT_ADVISED)
            .addDqField(GROSS_WEIGHT_GATE_MEASURED)
            .addDqField(GROSS_WEIGHT_YARD_MEASURED)
            .addDqField(PRIMARY_EQ_GKEY)
            .addDqField(PRIMARY_EQ_CLASS)
            .addDqField(EQTYPE_ID)
            .addDqField(EQ_LENGTH_MM)
            .addDqField(EQ_WIDTH_MM)
            .addDqField(EQ_HEIGHT_MM)
            .addDqField(IS_OOG)
            .addDqField(OOG_FRONT)
            .addDqField(OOG_BACK)
            .addDqField(OOG_TOP)
            .addDqField(OOG_LEFT)
            .addDqField(OOG_RIGHT)
            .addDqField(UFV_GKEY)
            .addDqField(PRIMARY_DAMAGES_GKEY)
            .addDqField(PRIMARY_EQS_GKEY)
            .addDqField(IS_CTR_SEALED)
            .addDqField(IS_BUNDLE)
            .addDqField(HAZARDS_GKEY)
            .addDqField(EQ_TANK_RAILS)
            .addDqField(CAS_TRANSACTION_REFERENCE)
            .addDqField(CAS_UNIT_REFERENCE)
            .setFullLeftOuterJoin(true);
    return dq;
  }
  /**
   * Creates CAS units xml for a given domain query, without apending the WI element
   * @param inDomainQuery domain query for units and the attributes to be included in xml
   * @return CAS Ubit xml
   */
  public String createUnitXml(DomainQuery inDomainQuery) {
    return createUnitXml(inDomainQuery, false, null);
  }
  /**
   * Creates CAS units xml for a given domain query
   * @param inDomainQuery domain query for units and the attributes to be included in xml
   * @param inAppendWiElement whether to appen WI element in the unit xml
   * @param inUfvGkeyWiMap a map of ufvGkey --> List<ValueHolder> of WI, if available; avoids re-querying the DB
   * In most cases the list will contain a single WI, especially for the QC WQs
   * @return CAS Ubit xml
   */
  public String createUnitXml(DomainQuery inDomainQuery, boolean inAppendWiElement,  Map<Serializable, List<ValueHolder>> inUfvGkeyWiMap) {
    def writer = new StringWriter();
    def xml = new MarkupBuilder(writer);
    MultiKeyMap binNamePositionMap = new MultiKeyMap();
    Map<Serializable, List<ValueHolder>> ufvGkeyWiMap = new LinkedHashMap<Serializable, List<ValueHolder>>();
    Map<Serializable, ValueHolder> wiSequenceWiMap = new LinkedHashMap<Serializable, ValueHolder>();
    List<ValueHolder> fullWiVhList = new ArrayList<ValueHolder>();
    final QueryResult qr = HibernateApi.getInstance().findValuesByDomainQuery(inDomainQuery);
    if (qr == null || qr.getTotalResultCount() == 0) {
      registerMessage(MessageLevel.INFO, InventoryPropertyKeys.CAS_NO_ONBOARD_UNITS);
    } else {
      //Do the WI related processing if needed
      if (inAppendWiElement){
        if (inUfvGkeyWiMap != null){
          ufvGkeyWiMap = inUfvGkeyWiMap;
        }else{//query the WIs and build the map
          //Get the ufv gkeys from unit query result
          Set<Serializable> ufvGkeys = new LinkedHashSet<Serializable>();
          for (int i = 0; i < qr.getTotalResultCount(); i++) {
            ufvGkeys.add((Serializable) qr.getValue(i, UFV_GKEY));
          }
          //Get the WIs for the ufvs and stash them in the list, which then becomes value in the map
          DomainQuery wiDq = getWiDomainQueryWithoutPredicate();
          wiDq = wiDq.addDqPredicate(PredicateFactory.in(WI_UFV_GKEY, ufvGkeys))
                  .addDqPredicate(PredicateFactory.ne(MovesField.WI_MOVE_STAGE, WiMoveStageEnum.COMPLETE))
                  .addDqOrdering(Ordering.asc(MovesField.WI_MOVE_NUMBER));
          QueryResult wiQr = HibernateApi.getInstance().findValuesByDomainQuery(wiDq);
          if (wiQr != null && wiQr.getTotalResultCount() != 0) {
            Serializable wiUfvGkey = null;
            for (int j = 0; j < wiQr.getTotalResultCount(); j++) {
              wiUfvGkey = (Serializable) wiQr.getValue(j, WI_UFV_GKEY);
              List<ValueHolder> wiValueHolderList;
              if (ufvGkeyWiMap.containsKey(wiUfvGkey)) {
                wiValueHolderList = ufvGkeyWiMap.get(wiUfvGkey);
              } else {
                wiValueHolderList = new ArrayList<ValueHolder>();
                ufvGkeyWiMap.put(wiUfvGkey, wiValueHolderList);
              }
              wiValueHolderList.add(wiQr.getValueHolder(j));
            }
          }
        }
        for (List<ValueHolder> wiVhList : ufvGkeyWiMap.values()) {
          fullWiVhList.addAll(wiVhList);
        }
        //Populate the wiSequenceWiMap map; The assumption enforced while populating map below is that all WIs belong to same WQ
        if (!fullWiVhList.isEmpty()) {
          Long wqGkey = fullWiVhList.get(0).getFieldValue(WI_WQ_GKEY) as Long;
          for (ValueHolder wiVh : fullWiVhList) {
            Long wiSequence = wiVh.getFieldValue(MovesField.WI_SEQUENCE) as Long;
            Long wiWqGkey = wiVh.getFieldValue(WI_WQ_GKEY) as Long;
            if (wiSequence != null) {
              if (wqGkey.equals(wiWqGkey)) {
                wiSequenceWiMap.put(wiSequence, wiVh);
              } else {
                log(Level.WARN, "createUnitXml(): Not added to wiSeqWiVhMap as the wi wq gkey does not match the wq gkey[" + wqGkey +
                        "].  Current WI[" + wiVh.getEntityPrimaryKey() + "]");
              }
            }else{
              log(Level.WARN, "createUnitXml(): Not added to wiSeqWiVhMap as the wi sequence is null for WI[gkey="
                      + wiVh.getEntityPrimaryKey() + "]");
            }
          }
        }
      }
      //First create the map of bin names for the positions in the result set; it would be needed to resolve the row, columne and tier information
      //from the slot position
      binNamePositionMap = generateBinNamePositionMap(qr, fullWiVhList);
      int resultCount = qr.getTotalResultCount();
      for (int i = 0; i < resultCount; i++) {
        //Add each unit
        ValueHolder valueHolder = qr.getValueHolder(i);
        String unitId = translateValue(valueHolder.getFieldValue(UNIT_ID));
        String unitGrossWt = translateValue(valueHolder.getFieldValue(GROSS_WEIGHT));
        String unitYardMeasuredWt = translateValue(valueHolder.getFieldValue(GROSS_WEIGHT_YARD_MEASURED));
        String eqIsoCode = translateValue(valueHolder.getFieldValue(EQTYPE_ID));
        String eqLengthMm = translateValue(valueHolder.getFieldValue(EQ_LENGTH_MM));
        String eqWidthMm = translateValue(valueHolder.getFieldValue(EQ_WIDTH_MM));
        String eqHeightMm = translateValue(valueHolder.getFieldValue(EQ_HEIGHT_MM));
        String seal1 = translateValue(valueHolder.getFieldValue(SEAL1));
        String seal2 = translateValue(valueHolder.getFieldValue(SEAL2));
        String seal3 = translateValue(valueHolder.getFieldValue(SEAL3));
        String seal4 = translateValue(valueHolder.getFieldValue(SEAL4));
        //current position attributes
        String locType = translateValue(valueHolder.getFieldValue(LOC_TYPE));
        String locId = translateValue(valueHolder.getFieldValue(LOC_ID));
        String slot = translateValue(valueHolder.getFieldValue(SLOT));
        String back = translateValue(valueHolder.getFieldValue(OOG_BACK));
        String front = translateValue(valueHolder.getFieldValue(OOG_FRONT));
        String left = translateValue(valueHolder.getFieldValue(OOG_LEFT));
        String right = translateValue(valueHolder.getFieldValue(OOG_RIGHT));
        String top = translateValue(valueHolder.getFieldValue(OOG_TOP));
        Object unitGkey = valueHolder.getFieldValue(UNIT_GKEY);
        Object dmgsGkey = valueHolder.getFieldValue(PRIMARY_DAMAGES_GKEY);
        Object hazGkey = valueHolder.getFieldValue(HAZARDS_GKEY)
        String isHazardous = translateBoolean(hazGkey != null && (Boolean) hazGkey);
        String tankRails = translateValue(valueHolder.getFieldValue(EQ_TANK_RAILS));
        boolean isCtrSealed = valueHolder.getFieldValue(IS_CTR_SEALED) == null ? false : (Boolean) valueHolder.getFieldValue(IS_CTR_SEALED);
        boolean isBundle = valueHolder.getFieldValue(IS_BUNDLE) == null ? false : (Boolean) valueHolder.getFieldValue(IS_BUNDLE);
        boolean isOog = valueHolder.getFieldValue(IS_OOG) == null ? false : (Boolean) valueHolder.getFieldValue(IS_OOG);
        String casUnitRef = translateValue(valueHolder.getFieldValue(CAS_UNIT_REFERENCE));
        String casTranRef = translateValue(valueHolder.getFieldValue(CAS_TRANSACTION_REFERENCE));


        Object ufvGkey = valueHolder.getFieldValue(UFV_GKEY);

        xml.unit(id: unitId, "cas-unit-reference": casUnitRef, "cas-transaction-reference": casTranRef, "gross-weight": unitGrossWt,
                "yard-measured-weight": unitYardMeasuredWt, "iso-code": eqIsoCode, "length-mm": eqLengthMm,
                "height-mm": eqHeightMm, "width-mm": eqWidthMm, "is-hazardous":isHazardous, "tank-rail-type": tankRails) {
          String[] position = resolvePosition(valueHolder, binNamePositionMap, true);
          "current-position"("loc-type": locType, location: locId, block: position[0], row: position[1], column: position[2], tier: position[3]) {}
          if (isOog) {
            oog("top-cm":top, "front-cm":front, "back-cm":back, "left-cm":left, "right-cm":right);
          }
          if (isCtrSealed) {
            seals("seal-1":seal1, "seal-2":seal2, "seal-3":seal3, "seal-4":seal4);
          }
          if (isBundle) {
            DomainQuery dq = QueryUtils.createDomainQuery(InventoryEntity.UNIT_EQUIPMENT)
                    .addDqField(UE_EQ_ID_FULL)
                    .addDqField(UE_EQ_EQTYPE_ID)
                    .addDqField(UE_EQ_LENGTH_MM)
                    .addDqField(UE_EQ_HEIGHT_MM)
                    .addDqField(UE_EQ_WIDTH_MM)
                    .addDqPredicate(PredicateFactory.eq(UnitField.UE_UNIT, unitGkey))
                    .addDqPredicate(PredicateFactory.eq(UE_EQ_ROLE, EqUnitRoleEnum.PAYLOAD));
            final QueryResult eqQr = HibernateApi.getInstance().findValuesByDomainQuery(dq);
            if (eqQr != null && eqQr.getTotalResultCount() != 0) {
              "bundled-equipment"() {
                for (int j = 0; j < eqQr.getTotalResultCount(); j++) {
                  String eqId = translateValue(eqQr.getValue(j, UE_EQ_ID_FULL));
                  String typeId = translateValue(eqQr.getValue(j, UE_EQ_EQTYPE_ID));
                  String length = translateValue(eqQr.getValue(j, UE_EQ_LENGTH_MM));
                  String height = translateValue(eqQr.getValue(j, UE_EQ_HEIGHT_MM));
                  String width = translateValue(eqQr.getValue(j, UE_EQ_WIDTH_MM));
                  "equipment"(id: eqId, "iso-code": typeId, "length-mm": length, "height-mm": height, "width-mm": width);
                }
              }
            }
          }
          if (dmgsGkey != null) {
            UnitEquipDamages ueDamages = (UnitEquipDamages) HibernateApi.getInstance().load(UnitEquipDamages.class, dmgsGkey);
            if (ueDamages != null) {
              damages() {
                Iterator iter = ueDamages.getDamageItemsIterator();
                while (iter.hasNext()) {
                  UnitEquipDamageItem item = (UnitEquipDamageItem) iter.next();
                  String typeId = item.getDmgitemType().getEqdmgtypId();
                  String typeDesc = item.getDmgitemType().getEqdmgtypDescription();
                  String eqCmpId = item.getDmgitemComponent() != null ? item.getDmgitemComponent().getEqcmpId() : "";
                  String cmpDesc = item.getDmgitemComponent() != null ? item.getDmgitemComponent().getEqcmpDescription() : "";
                  String loc = item.getDmgitemLocation();
                  String reportedDate = item.getDmgitemReported() == null ? "" : translateDate(item.getDmgitemReported());
                  String repairedDate = item.getDmgitemRepaired() == null ? "" : translateDate(item.getDmgitemRepaired());
                  String dmgSeverity = translateValue(item.getDmgitemSeverity());
                  "damage-item"(type:typeId, "type-description":typeDesc, component:eqCmpId, "component-description":cmpDesc, location:loc,
                          "reported-date":reportedDate, "repaired-date":repairedDate, severity:dmgSeverity);
                }
              }
            }
          }
          Unit unit = Unit.hydrate((Serializable) unitGkey);
          ServicesManager servicesMgr = (ServicesManager) Roastery.getBean(ServicesManager.BEAN_ID);
          Collection<IImpediment> impediments = servicesMgr.getImpedimentsForEntity(unit);
          if (!impediments.isEmpty()) {
            flags() {
              for (IImpediment impediment : impediments) {
                FlagPurposeEnum purpose = impediment.getFlagType().getPurpose();
                String flagId = impediment.getFlagType().getId();
                if (FlagPurposeEnum.HOLD.equals(purpose)) {
                  hold(id: flagId);
                } else if (FlagPurposeEnum.PERMISSION.equals(purpose)) {
                  permission(id: flagId);
                }
              }
            }
          }
          if (unitGkey != null) {
            DomainQuery dq = QueryUtils.createDomainQuery(InventoryEntity.OBSERVED_PLACARD)
                    .addDqField(OBSPLACARD_PLACARD_TEXT)
                    .addDqField(InventoryField.OBSPLACARD_REMARK)
                    .addDqPredicate(PredicateFactory.eq(InventoryField.OBSPLACARD_UNIT, unitGkey))
                    .setFullLeftOuterJoin(true);

            final QueryResult plcQr = HibernateApi.getInstance().findValuesByDomainQuery(dq);
            if (plcQr != null && plcQr.getTotalResultCount() != 0) {
              "observed-placards"() {
                for (Iterator iterator = plcQr.getIterator(); iterator.hasNext();) {
                  Object[] v = (Object[]) iterator.next();
                  Object placardText = v[0];
                  Object placardRemarks = v[1];
                  "observed-placard"(placard: placardText, remarks: placardRemarks);
                }
              }
            }
          }
          if (inAppendWiElement) {
            final List<ValueHolder> wiVhList = ufvGkeyWiMap.get(ufvGkey);
            if (wiVhList != null) {
              for (ValueHolder wiVh : wiVhList) {
                String seq = translateValue(wiVh.getFieldValue(MovesField.WI_SEQUENCE));
                String moveKind = translateValue(wiVh.getFieldValue(MovesField.WI_MOVE_KIND));
                String craneLane = translateValue(wiVh.getFieldValue( MovesField.WI_CRANE_LANE));
                String doorDirection = translateValue(wiVh.getFieldValue(MovesField.WI_DOOR_DIRECTION));
                String wiLocType = translateValue(wiVh.getFieldValue(WI_POS_LOC_TYPE));
                String wiLocId = translateValue(wiVh.getFieldValue(WI_POS_LOC_ID));
                String wiSlot = translateValue(wiVh.getFieldValue(WI_POS_SLOT));

                CraneLiftModeEnum liftModeEnum = getLiftType(wiVh);
                String liftType = liftModeEnum.getKey();
                String[] associatedUnitIds = new String[3];
                associatedUnitIds[0] = associatedUnitIds[1] = associatedUnitIds[2] = "";
                if (!(CraneLiftModeEnum.SINGLE == liftModeEnum)){
                  populateAssociatedUnitIds(wiVh, wiSequenceWiMap, liftModeEnum, associatedUnitIds)
                }
                "work-instruction"(sequence:seq, "move-kind":moveKind, "crane-lane":craneLane, "door-direction":doorDirection, "lift-type": liftType,
                        "associated-unit-id-1": associatedUnitIds[0], "associated-unit-id-2": associatedUnitIds[1],
                        "associated-unit-id-3": associatedUnitIds[2]){
                  String[] wiPosition = resolvePosition(wiVh, binNamePositionMap, false);
                  "planned-position"("loc-type": wiLocType, location: wiLocId, block: wiPosition[0], row: wiPosition[1], column: wiPosition[2], tier: wiPosition[3]) {}
                }
              }
            }else{
              log(Level.WARN, "No WI found for UFV entity[gkey=" + ufvGkey + "]");
            }
          }
        }
      }
    }
    String out = writer.toString();
    return out;

  }
  private CraneLiftModeEnum getLiftType(ValueHolder inWiVh){
    TwinWithEnum twinWithEnum = inWiVh.getFieldValue(MovesField.WI_TWIN_WITH) as TwinWithEnum;
    Boolean isTandemWithNext = inWiVh.getFieldValue(MovesField.WI_IS_TANDEM_WITH_NEXT) as Boolean;
    Boolean isTandemWithPrevious = inWiVh.getFieldValue(MovesField.WI_IS_TANDEM_WITH_PREVIOUS) as Boolean;
    boolean isPartOfTandemSet = isTandemWithNext || isTandemWithPrevious;
    if (TwinWithEnum.NEXT == twinWithEnum || TwinWithEnum.PREV == twinWithEnum){
       if (isPartOfTandemSet){
         //Twin as well as tandem; must be a quad
         return CraneLiftModeEnum.QUAD;
       }else{
         //Twin but not a tandem; so it is a twin
         return CraneLiftModeEnum.TWIN;
       }
    }else{ //twinWithEnum is neither 'NEXT' or 'PREV'; not a twin
      if (isPartOfTandemSet) {
        //Tandem
        return CraneLiftModeEnum.TANDEM;
      }
    }
    return CraneLiftModeEnum.SINGLE;
  }

  private void populateAssociatedUnitIds(ValueHolder inWiVh, Map<Serializable, ValueHolder> inWiSeqWiVhMap,
                                        CraneLiftModeEnum inLiftModeEnum,  String[] inOutAssociatedUnitIds){
     if (CraneLiftModeEnum.SINGLE == inLiftModeEnum ||  inLiftModeEnum == null){
       return;
     }
     if (CraneLiftModeEnum.TWIN == inLiftModeEnum){
       TwinWithEnum twinWithEnum = inWiVh.getFieldValue(MovesField.WI_TWIN_WITH) as TwinWithEnum;
       if (TwinWithEnum.NEXT == twinWithEnum) {
         ValueHolder nextWiVh = getNextWiInWq(inWiVh, inWiSeqWiVhMap);
         if (nextWiVh != null) {
           inOutAssociatedUnitIds[0] = translateValue(nextWiVh.getFieldValue(WI_UNIT_ID));
         }
       } else if (TwinWithEnum.PREV == twinWithEnum) {
         ValueHolder previousWiVh = getPreviousWiInWq(inWiVh, inWiSeqWiVhMap);
         if (previousWiVh != null) {
           inOutAssociatedUnitIds[0] = translateValue(previousWiVh.getFieldValue(WI_UNIT_ID));
         }
       }else{
         log(Level.WARN, "populateAssociatedUnitIds(): The lift mode is 'twin' but 'twin with' value is " +
                 "neither 'NEXT' or 'PREV' for WI[gkey=" + inWiVh.getEntityPrimaryKey() + "]");
       }
       return;
     }else{ //it is either tandem or quad
       Boolean isTandemWithNext = inWiVh.getFieldValue(MovesField.WI_IS_TANDEM_WITH_NEXT) as Boolean;
       Boolean isTandemWithPrevious = inWiVh.getFieldValue(MovesField.WI_IS_TANDEM_WITH_PREVIOUS) as Boolean;
       boolean isPartOfTandemSet = isTandemWithNext || isTandemWithPrevious;
       if (!isPartOfTandemSet) {
         return;
       }
       if (CraneLiftModeEnum.TANDEM == inLiftModeEnum){ //tandem with two 40s (assumption: no triplets)
         if (isTandemWithNext) {
           ValueHolder nextWiVh = getNextWiInWq(inWiVh, inWiSeqWiVhMap);
           if (nextWiVh != null) {
             inOutAssociatedUnitIds[0] = translateValue(nextWiVh.getFieldValue(WI_UNIT_ID));
           }
         }else if (isTandemWithPrevious) {
           ValueHolder previousWiVh = getPreviousWiInWq(inWiVh, inWiSeqWiVhMap);
           if (previousWiVh != null) {
             inOutAssociatedUnitIds[0] = translateValue(previousWiVh.getFieldValue(WI_UNIT_ID));
           }
         } else {
           log(Level.ERROR, "populateAssociatedUnitIds(): The lift mode is 'tandem' but " +
                   "neither 'isTandemWithNext' or 'isTandemWithNext' is true for WI[gkey=" + inWiVh.getEntityPrimaryKey() + "]");
         }
         return;
       }else if (CraneLiftModeEnum.QUAD == inLiftModeEnum){//4 20s; no triplets
         TwinWithEnum twinWithEnum = inWiVh.getFieldValue(MovesField.WI_TWIN_WITH) as TwinWithEnum;
           if (TwinWithEnum.NEXT == twinWithEnum && isTandemWithNext && !isTandemWithPrevious){
             //First WI ; get number 2,3 and 4
             boolean hasError = false;
             ValueHolder wiVh = inWiVh;
             for (int i = 0; i < 3; i++){
               ValueHolder nextWiVh = getNextWiInWq(wiVh, inWiSeqWiVhMap);
               if (nextWiVh != null && !hasError) {
                 inOutAssociatedUnitIds[i] = translateValue(nextWiVh.getFieldValue(WI_UNIT_ID));
                 wiVh = nextWiVh;
               }else{
                 hasError = true;
                 log(Level.ERROR, "populateAssociatedUnitIds(): The lift mode is 'quad' but " +
                         "could not find WI number[" + (i+2) + "]. First WI[gkey=" + inWiVh.getEntityPrimaryKey() + "]");
                 //Reset all associated unit ids to empty strings
                 inOutAssociatedUnitIds[0] = inOutAssociatedUnitIds[1] = inOutAssociatedUnitIds[2] = "";
               }
             }
           }else if (TwinWithEnum.PREV == twinWithEnum && isTandemWithNext && isTandemWithPrevious) {
              //Second WI ; get number 1,3 and 4
             boolean hasError = false;
             //Get first unit
             ValueHolder previousWiVh = getPreviousWiInWq(inWiVh, inWiSeqWiVhMap);
             if (previousWiVh != null) {
               inOutAssociatedUnitIds[0] = translateValue(previousWiVh.getFieldValue(WI_UNIT_ID));
             }else{
               hasError = true;
               log(Level.ERROR, "populateAssociatedUnitIds(): The lift mode is 'quad' but " +
                       "could not find WI number[1]. Second WI[" + inWiVh + "]");
               //Reset all associated unit ids to empty strings
               inOutAssociatedUnitIds[0] = inOutAssociatedUnitIds[1] = inOutAssociatedUnitIds[2] = "";
             }
             //Get 3 and 4
             if (!hasError) {
               ValueHolder wiVh = inWiVh;
               for (int i = 1; i < 3; i++) {
                 ValueHolder nextWiVh = getNextWiInWq(wiVh, inWiSeqWiVhMap);
                 if (nextWiVh != null && !hasError) {
                   inOutAssociatedUnitIds[i] = translateValue(nextWiVh.getFieldValue(WI_UNIT_ID));
                   wiVh = nextWiVh;
                 } else {
                   hasError = true;
                   log(Level.ERROR, "populateAssociatedUnitIds(): The lift mode is 'quad' but " +
                           "could not find WI number[" + (i + 2) + "]. Second WI[gkey=" + inWiVh.getEntityPrimaryKey() + "]");
                   //Reset all associated unit ids to empty strings
                   inOutAssociatedUnitIds[0] = inOutAssociatedUnitIds[1] = inOutAssociatedUnitIds[2] = "";
                 }
               }
             }
           }else if (TwinWithEnum.NEXT == twinWithEnum && isTandemWithNext && isTandemWithPrevious){
             //Third WI ; get number 1,2 and 4
             boolean hasError = false;
             ValueHolder wiVh = inWiVh;
             //Get 1 & 2
             for (int i = 1; i >= 0; i--) {
               ValueHolder prevWiVh = getPreviousWiInWq(wiVh, inWiSeqWiVhMap);
               if (prevWiVh != null && !hasError) {
                 inOutAssociatedUnitIds[i] = translateValue(prevWiVh.getFieldValue(WI_UNIT_ID));
                 wiVh = prevWiVh;
               } else {
                 hasError = true;
                 log(Level.ERROR, "populateAssociatedUnitIds(): The lift mode is 'quad' but " +
                         "could not find WI number[" + (i + 1) + "]. Third WI[gkey=" + inWiVh.getEntityPrimaryKey() + "]");
                 //Reset all associated unit ids to empty strings
                 inOutAssociatedUnitIds[0] = inOutAssociatedUnitIds[1] = inOutAssociatedUnitIds[2] = "";
               }
             }
             //Get 4th
             if (!hasError){
               wiVh = inWiVh;
               ValueHolder nextWiVh = getNextWiInWq(wiVh, inWiSeqWiVhMap);
               if (nextWiVh != null) {
                 inOutAssociatedUnitIds[2] = translateValue(nextWiVh.getFieldValue(WI_UNIT_ID));
               } else {
                 log(Level.ERROR, "populateAssociatedUnitIds(): The lift mode is 'quad' but " +
                         "could not find WI number[4]. Third WI[gkey=" + inWiVh.getEntityPrimaryKey() + "]");
                 //Reset all associated unit ids to empty strings
                 inOutAssociatedUnitIds[0] = inOutAssociatedUnitIds[1] = inOutAssociatedUnitIds[2] = "";
               }
             }
           }else if (TwinWithEnum.PREV == twinWithEnum && !isTandemWithNext && isTandemWithPrevious){
             //Fourth WI ; get number 1,2 and 3
             boolean hasError = false;
             ValueHolder wiVh = inWiVh;
             for (int i = 2; i >= 0; i--) {
               ValueHolder prevWiVh = getPreviousWiInWq(wiVh, inWiSeqWiVhMap);
               if (prevWiVh != null && !hasError) {
                 inOutAssociatedUnitIds[i] = translateValue(prevWiVh.getFieldValue(WI_UNIT_ID));
                 wiVh = prevWiVh;
               } else {
                 hasError = true;
                 log(Level.ERROR, "populateAssociatedUnitIds(): The lift mode is 'quad' but " +
                         "could not find WI number[" + (i+1) + "]. Fourth WI[gkey=" + inWiVh.getEntityPrimaryKey() + "]");
                 //Reset all associated unit ids to empty strings
                 inOutAssociatedUnitIds[0] = inOutAssociatedUnitIds[1] = inOutAssociatedUnitIds[2] = "";
               }
             }
           }
         }
       }
  }
  private ValueHolder getNextWiInWq(ValueHolder inWiVh, Map<Serializable, ValueHolder> inWiSeqWiVhMap){
    Long wiSeq = inWiVh.getFieldValue(MovesField.WI_SEQUENCE) as Long;
    if (wiSeq != null) {
      Long companionWiSeq = wiSeq + 1;
      ValueHolder companionWiVh = inWiSeqWiVhMap.get(companionWiSeq);
      if (companionWiVh == null) {
        log(Level.ERROR, "getNextWiInWq():No next WI found in inWiSeqWiVhMap for WI[gkey=" + inWiVh.getEntityPrimaryKey() + "]");
        return null;
      } else {
        return companionWiVh;
      }
    } else {
      log(Level.ERROR, "getNextWiInWq(): The WI sequence is null for WI[gkey=" + inWiVh.getEntityPrimaryKey() + "]");
    }
    return null;
  }

  private ValueHolder getPreviousWiInWq(ValueHolder inWiVh, Map<Serializable, ValueHolder> inWiSeqWiVhMap) {
    Long wiSeq = inWiVh.getFieldValue(MovesField.WI_SEQUENCE) as Long;
    if (wiSeq != null) {
      Long companionWiSeq = wiSeq - 1;
      ValueHolder companionWiVh = inWiSeqWiVhMap.get(companionWiSeq);
      if (companionWiVh == null) {
        log(Level.ERROR, "getPreviousWiInWq(): No previous wi found in inWiSeqWiVhMap for WI[gkey=" + inWiVh.getEntityPrimaryKey() + "]");
        return null;
      }else{
        return companionWiVh;
      }
    } else {
      log(Level.ERROR, "getPreviousWiInWq(): The wi sequence is null for WI[gkey=" + inWiVh.getEntityPrimaryKey() + "]");
    }
    return null;
  }
/**
 * Create a multikey map for bin names. It is used to resolve the position to a block, row, column and tier.
 * Each entry in the map has
 * key1= BinTableName gkey
 * key2= logical position
 * value= user name for that logical position and bin table
 * @param inUnitQueryResult units(current positions) for which the map is built
 * @param inWiVhColl WI positions (planned positions) for which the map is being built
 * @return multikey map as described above
 */
  protected MultiKeyMap generateBinNamePositionMap(QueryResult inUnitQueryResult, Collection<ValueHolder> inWiVhColl) {
    List<Serializable> binNameTableGkeys = new ArrayList<Serializable>();
    List<Serializable> binLogicalPositions = new ArrayList<Serializable>();
    MultiKeyMap binNamePositionMap = new MultiKeyMap();
    if (inUnitQueryResult != null) {
      int resultCount = inUnitQueryResult.getTotalResultCount();
      //Get the Bin names corresponding to the positions in the query-result and add them to the multi key map
      ValueHolder unitValueHolder = null;
      for (int i = 0; i < resultCount; i++) {
        //Get position and conversion related fields
        unitValueHolder = inUnitQueryResult.getValueHolder(i);
        Serializable blockConvIdRowGkey = unitValueHolder.getFieldValue(CURR_POS_BLOCK_CONV_ID_ROW_GKEY) as Serializable;
        Serializable blockConvIdRowAltGkey = unitValueHolder.getFieldValue(CURR_POS_BLOCK_CONV_ID_ROW_ALT_GKEY) as Serializable;
        Serializable blockConvIdTierGkey = unitValueHolder.getFieldValue(CURR_POS_BLOCK_CONV_ID_TIER_GKEY) as Serializable;

        Serializable sectionConvIdColGkey = unitValueHolder.getFieldValue(CURR_POS_ROW_CONV_ID_COL_GKEY) as Serializable;
        Serializable cellSectionConvIdTierGkey = unitValueHolder.getFieldValue(CURR_POS_CELL_SECTION_CONV_ID_TIER_GKEY) as Serializable;

        Serializable currentRowIndex = unitValueHolder.getFieldValue(CURR_POS_ROW_INDEX) as Serializable;
        Serializable currentColumnIndex = unitValueHolder.getFieldValue(CURR_POS_COLUMN_INDEX) as Serializable;
        Serializable posTier = unitValueHolder.getFieldValue(CURR_POS_TIER) as Serializable;

        //Add to the lists
        //Add to the bin name table gkey list
        if (blockConvIdRowGkey != null && !binNameTableGkeys.contains(blockConvIdRowGkey)){
          binNameTableGkeys.add(blockConvIdRowGkey)
        }
        if (blockConvIdRowAltGkey != null && !binNameTableGkeys.contains(blockConvIdRowAltGkey)){
          binNameTableGkeys.add(blockConvIdRowAltGkey)
        }
        if (blockConvIdTierGkey != null && !binNameTableGkeys.contains(blockConvIdTierGkey)){
          binNameTableGkeys.add(blockConvIdTierGkey)
        }
        if (sectionConvIdColGkey != null && !binNameTableGkeys.contains(sectionConvIdColGkey)){
          binNameTableGkeys.add(sectionConvIdColGkey)
        }
        if (cellSectionConvIdTierGkey != null && !binNameTableGkeys.contains(cellSectionConvIdTierGkey)){
          binNameTableGkeys.add(cellSectionConvIdTierGkey)
        }
        //Add to the logical positions list
        if (currentRowIndex != null && !binLogicalPositions.contains(currentRowIndex)){
          binLogicalPositions.add(currentRowIndex)
        }
        if (currentColumnIndex != null && !binLogicalPositions.contains(currentColumnIndex)){
          binLogicalPositions.add(currentColumnIndex)
        }
        if (posTier instanceof Long && !binLogicalPositions.contains(posTier)){
          binLogicalPositions.add(posTier)
        }
      }
    }
    if (inWiVhColl != null){
      for (ValueHolder wiValueHolder : inWiVhColl){
        //Get position and conversion related fields
        Serializable blockConvIdRowGkey = wiValueHolder.getFieldValue(WI_POS_BLOCK_CONV_ID_ROW_GKEY) as Serializable;
        Serializable blockConvIdRowAltGkey = wiValueHolder.getFieldValue(WI_POS_BLOCK_CONV_ID_ROW_ALT_GKEY) as Serializable;
        Serializable blockConvIdTierGkey = wiValueHolder.getFieldValue(WI_POS_BLOCK_CONV_ID_TIER_GKEY) as Serializable;

        Serializable sectionConvIdColGkey = wiValueHolder.getFieldValue(WI_POS_ROW_CONV_ID_COL_GKEY) as Serializable;
        Serializable cellSectionConvIdTierGkey = wiValueHolder.getFieldValue(WI_POS_CELL_SECTION_CONV_ID_TIER_GKEY) as Serializable;

        Serializable wiPosRowIndex = wiValueHolder.getFieldValue(WI_POS_ROW_INDEX) as Serializable;
        Serializable wiPosColumnIndex = wiValueHolder.getFieldValue(WI_POS_COLUMN_INDEX) as Serializable;
        Serializable posTier = wiValueHolder.getFieldValue(WI_POS_TIER) as Serializable;

        //Add to the lists
        //Add to the bin name table gkey list
        if (blockConvIdRowGkey != null && !binNameTableGkeys.contains(blockConvIdRowGkey)){
          binNameTableGkeys.add(blockConvIdRowGkey)
        }
        if (blockConvIdRowAltGkey != null && !binNameTableGkeys.contains(blockConvIdRowAltGkey)){
          binNameTableGkeys.add(blockConvIdRowAltGkey)
        }
        if (blockConvIdTierGkey != null && !binNameTableGkeys.contains(blockConvIdTierGkey)){
          binNameTableGkeys.add(blockConvIdTierGkey)
        }
        if (sectionConvIdColGkey != null && !binNameTableGkeys.contains(sectionConvIdColGkey)){
          binNameTableGkeys.add(sectionConvIdColGkey)
        }
        if (cellSectionConvIdTierGkey != null && !binNameTableGkeys.contains(cellSectionConvIdTierGkey)){
          binNameTableGkeys.add(cellSectionConvIdTierGkey)
        }
        //Add to the logical positions list
        if (wiPosRowIndex != null && !binLogicalPositions.contains(wiPosRowIndex)){
          binLogicalPositions.add(wiPosRowIndex)
        }
        if (wiPosColumnIndex != null && !binLogicalPositions.contains(wiPosColumnIndex)){
          binLogicalPositions.add(wiPosColumnIndex)
        }
        if (posTier instanceof Long && !binLogicalPositions.contains(posTier)){
          binLogicalPositions.add(posTier)
        }
      }
    }
    //Query the BinName values based on the logical positions and bin name tables collected above; aim is to get the minimum result based on the
    //units and WIs for which the map is to be generated
    if (!binNameTableGkeys.isEmpty() && !binLogicalPositions.isEmpty()) {
      DomainQuery binNameDq = QueryUtils.createDomainQuery(BinEntity.BIN_NAME).
              addDqField(BinField.BNM_NAME_TABLE).
              addDqField(BinField.BNM_LOGICAL_POSITION).
              addDqField(BinField.BNM_USER_NAME).
              addDqPredicate(PredicateFactory.in(BinField.BNM_NAME_TABLE, binNameTableGkeys.toArray(new Serializable[binNameTableGkeys.size()]))).
              addDqPredicate(PredicateFactory.in(BinField.BNM_LOGICAL_POSITION, binLogicalPositions.toArray(new Serializable[binLogicalPositions.size()])));
      binNameDq.setFilter(ObsoletableFilterFactory.createShowActiveFilter());
      final QueryResult binNameQueryResult = HibernateApi.getInstance().findValuesByDomainQuery(binNameDq);
      int binNameCount = binNameQueryResult.getTotalResultCount();
      ValueHolder binNameValueHolder = null;
      for (int i = 0; i < binNameCount; i++) {
        binNameValueHolder = binNameQueryResult.getValueHolder(i);
        binNamePositionMap.put(binNameValueHolder.getFieldValue(0), binNameValueHolder.getFieldValue(1), binNameValueHolder.getFieldValue(2));
      }
    }
    return binNamePositionMap;
  }
  /**
   * Resolves the position name by getting the position from the value holder and getting the corresponding username from the bin position map
   * @param inValueHolder
   * @param inBinNamePositionMap
   * @param isCurrentUnitPosition whether it is a current position or a planned position
   * @return String array containing block, row, column and tier name
   */
  protected String[] resolvePosition(ValueHolder inValueHolder, MultiKeyMap inBinNamePositionMap, boolean isCurrentUnitPosition){
    String[] position = new String[4];
    position[0] = position[1] = position[2]= position[3] = "";
    LocTypeEnum locTypeEnum = null;
    String blockName = null;
    Serializable blockConvIdRowGkey = null;
    Serializable blockConvIdRowAltGkey = null;
    Serializable blockConvIdTierGkey = null;
    Serializable currentRowIndex = null;
    Serializable cellSectionConvIdTierGkey = null;

    Serializable sectionConvIdColGkey = null;
    Serializable currentColumnIndex = null;

    Serializable posTier = null;
    String slot = null;
    EquipBasicLengthEnum equipBasicLengthEnum = null;

    if (isCurrentUnitPosition){ //it is unit current position(not a planned position)
       locTypeEnum = inValueHolder.getFieldValue(LOC_TYPE) as LocTypeEnum;
       blockName = inValueHolder.getFieldValue(CURR_POS_BLOCK_NAME);
       blockConvIdRowGkey = inValueHolder.getFieldValue(CURR_POS_BLOCK_CONV_ID_ROW_GKEY) as Serializable;
       blockConvIdRowAltGkey = inValueHolder.getFieldValue(CURR_POS_BLOCK_CONV_ID_ROW_ALT_GKEY) as Serializable;
       blockConvIdTierGkey = inValueHolder.getFieldValue(CURR_POS_BLOCK_CONV_ID_TIER_GKEY) as Serializable;
       currentRowIndex = inValueHolder.getFieldValue(CURR_POS_ROW_INDEX) as Serializable;
       cellSectionConvIdTierGkey = inValueHolder.getFieldValue(CURR_POS_CELL_SECTION_CONV_ID_TIER_GKEY) as Serializable;

       sectionConvIdColGkey = inValueHolder.getFieldValue(CURR_POS_ROW_CONV_ID_COL_GKEY) as Serializable;
       currentColumnIndex = inValueHolder.getFieldValue(CURR_POS_COLUMN_INDEX) as Serializable;

       posTier = inValueHolder.getFieldValue(CURR_POS_TIER) as Serializable;
       slot = inValueHolder.getFieldValue(SLOT);
       equipBasicLengthEnum = inValueHolder.getFieldValue(EQ_BASIC_LENGTH) as EquipBasicLengthEnum;
    }else{ //planned position (from WI)
      locTypeEnum = inValueHolder.getFieldValue(WI_POS_LOC_TYPE) as LocTypeEnum;
      blockName = inValueHolder.getFieldValue(WI_POS_BLOCK_NAME);
      blockConvIdRowGkey = inValueHolder.getFieldValue(WI_POS_BLOCK_CONV_ID_ROW_GKEY) as Serializable;
      blockConvIdRowAltGkey = inValueHolder.getFieldValue(WI_POS_BLOCK_CONV_ID_ROW_ALT_GKEY) as Serializable;
      blockConvIdTierGkey = inValueHolder.getFieldValue(WI_POS_BLOCK_CONV_ID_TIER_GKEY) as Serializable;
      currentRowIndex = inValueHolder.getFieldValue(WI_POS_ROW_INDEX) as Serializable;
      cellSectionConvIdTierGkey = inValueHolder.getFieldValue(WI_POS_CELL_SECTION_CONV_ID_TIER_GKEY) as Serializable;

      sectionConvIdColGkey = inValueHolder.getFieldValue(WI_POS_ROW_CONV_ID_COL_GKEY) as Serializable;
      currentColumnIndex = inValueHolder.getFieldValue(WI_POS_COLUMN_INDEX) as Serializable;

      posTier = inValueHolder.getFieldValue(WI_POS_TIER) as Serializable;
      slot = inValueHolder.getFieldValue(WI_POS_SLOT);
      equipBasicLengthEnum = inValueHolder.getFieldValue(WI_EQ_BASIC_LENGTH) as EquipBasicLengthEnum;
    }

    if (LocTypeEnum.TRAIN == locTypeEnum) {
        position = getUnitRailPosition(slot);
    }else { // not a rail position
      //Block
      position[0] = translateValue(blockName);

      //Row
      BinNameTypeEnum binTableNameEnum = EquipBasicLengthEnum.BASIC20 == equipBasicLengthEnum? BinNameTypeEnum.STANDARD : BinNameTypeEnum.ALTERNATE;
      Serializable rowConvGkey = null;
      if (BinNameTypeEnum.STANDARD == binTableNameEnum){
        rowConvGkey = blockConvIdRowGkey;
      }else{//BinNameTypeEnum.ALTERNATE
        rowConvGkey = blockConvIdRowAltGkey;
      }
      if (rowConvGkey != null && currentRowIndex != null){
        if (inBinNamePositionMap.containsKey(rowConvGkey, currentRowIndex)){
          position[1] = inBinNamePositionMap.get(rowConvGkey, currentRowIndex);
        }
      }
      //Column
      if (sectionConvIdColGkey != null && currentColumnIndex != null){
        if (inBinNamePositionMap.containsKey(sectionConvIdColGkey, currentColumnIndex)){
          position[2] = inBinNamePositionMap.get(sectionConvIdColGkey, currentColumnIndex);
        }
      }
      //Tier(will be modified further based on the loc type)
      position[3] = translateValue(posTier);

      if (LocTypeEnum.VESSEL == locTypeEnum){
        //Tier
        //For vessel positions the bin name table entry for user name for tier is different; it is a property of CellSection instead of AbstractBlock
        if (cellSectionConvIdTierGkey != null && posTier instanceof Long){
          if (inBinNamePositionMap.containsKey(cellSectionConvIdTierGkey, posTier)){
            position[3] = inBinNamePositionMap.get(cellSectionConvIdTierGkey, posTier);
          }
        }

        //If any of the values is empty fall back on using slot to get the positions. This is the case when their is no bin associated with the unit
        // one of the reasons is the non-availablility of the  ship bin model
        if (StringUtils.isBlank(position[0]) || StringUtils.isBlank(position[1])
                || StringUtils.isBlank(position[2]) || StringUtils.isBlank(position[3])) {
          //Reset the tier if it is 0; comes as zero from DB
          if ("0".equals(position[3])){
            position[3] = "";
          }
          String locSlot = translateValue(slot);
          String[] positionFromSlot = getUnitVesselPosition(locSlot);
          for (int i = 0; i < position.length; i++) {
            if (StringUtils.isBlank(position[i])){
              position[i] = positionFromSlot[i];
            }
          }
        }
      }else if (LocTypeEnum.YARD == locTypeEnum){
        //Tier
        if (blockConvIdTierGkey != null && posTier instanceof Long){
          if (inBinNamePositionMap.containsKey(blockConvIdTierGkey, posTier)){
            position[3] = inBinNamePositionMap.get(blockConvIdTierGkey, posTier);
          }
        }
      }else{
        log(Level.ERROR, "Not a supported loacation type[" + locTypeEnum + "]")
      }
    }
    return position;
  }
  /**
   * Returns a string array representing the 4 vessel coordinates. The array is always guaranteed to be 4 elements: 1) Vessel Deck 2) Vessel Bay 3)
   * Vessel Column 4) Vessel Tier
   *
   * @param inLocSlot the vessel slot name that we are decoding
   * @return string array of position coordinate names
   */
  @NotNull
  protected  String[] getUnitVesselPosition(@NotNull String inLocSlot) {
    String[] position = new String[4];
    position[0] = position[1] = position[2]= position[3] = ""
    // If the string has dots, parse the bay component
    if (inLocSlot.indexOf('.') > 0) {
      final String[] components = inLocSlot.split("\\.");
      if (components == null || components.length == 0) {
        log(Level.ERROR, String.format("Failed to get the bay position of slot string >%s<", inLocSlot));
        return position;
      }
      if (components.length == 3) {
        position[0] = getVesselDeck(components[2]);
        position[1] =  components[0];
        position[2] =  components[1];
        position[3] =  components[2];
        return position;
      } else {
        // strip out the dots and try again
        inLocSlot = StringUtils.remove(inLocSlot, '.');
      }
    }

    switch (inLocSlot.length()) {
      case 4:
        position[0] = "";
        position[1] = inLocSlot.substring(0, 2);
        position[2] = inLocSlot.substring(2, 4);
        position[3] = "";
        return position;
      case 6:
        position[0] = getVesselDeck(inLocSlot.substring(4, 6));
        position[1] = inLocSlot.substring(0, 2);
        position[2] = inLocSlot.substring(2, 4);
        position[3] = inLocSlot.substring(4, 6);
        return position;
      case 5:
        position[0] = getVesselDeck(inLocSlot.substring(3, 5));
        position[1] = inLocSlot.substring(0, 1);
        position[2] = inLocSlot.substring(1, 3);
        position[3] = inLocSlot.substring(3, 5);
        return position;
      default:
        if (inLocSlot.length() >= 7) {
          position[0] = getVesselDeck(inLocSlot.substring(5, 7));
          position[1] = inLocSlot.substring(0, 3);
          position[2] = inLocSlot.substring(3, 5);
          position[3] = inLocSlot.substring(5, 7);
          return position;
        }else if (inLocSlot.length() >= 1) {
            position[0] = "";
            position[1] = inLocSlot.substring(0, 1);
            position[2] = "";
            position[3] = "";
          return position;
        }
    }
    log(Level.ERROR, String.format("Failed to get the vessel position of slot string >%s<", inLocSlot));
    return position;
  }
  /**
   * Returns a string array representing the 4 vessel coordinates. The array is always guaranteed to be 4 elements: 1) RailCar name
   * 2) RailCar platform 3) RailCar slot 4) RailCar level
   *
   * @param inLocSlot the rail slot name that we are decoding
   * @return string array of position coordinate names
   */
  @NotNull
  protected String[] getUnitRailPosition(@NotNull String inLocSlot)  {
    String[] position = new String[4];
    position[0] = position[1] = position[2]= position[3] = ""

    String railCarName = "";
    String railCarPlatform = "";
    String railCarSlot = "";
    String railCarLevel = "";
    final String posLocSlot = inLocSlot;
    if (posLocSlot.indexOf('.') > 0) {
      // Expected posLocSlot syntax:  <railcar>.<platform><level><slot>
      String[] components = posLocSlot.split("\\.");
      if (components == null || components.length == 0) {
        log(Level.ERROR, String.format("Failed to parse rail position >%s<", posLocSlot));
        return position;
      }
      if (components.length != 2) {
        log(Level.ERROR, String.format("Rail position has invalid rail locSlot >%s<", posLocSlot));
        return position;
      }
      railCarName = components[0];
      final String railDetails = components[1];
      if (railDetails == null || railDetails.length() != 3) {
        log(Level.ERROR, String.format("Invalid rail position rail for locSlot >%s<", posLocSlot));
        return position;
      }
      railCarPlatform = railDetails.substring(0, 1);
      railCarLevel = railDetails.substring(1, 2);
      railCarSlot = railDetails.substring(2, 3);
    } else {
      log(Level.ERROR, String.format("Failed to parse rail position >%s<", posLocSlot));
      return position;
    }
    position[0] = railCarName;
    position[1] = railCarPlatform;
    position[2] = railCarSlot;
    position[3] = railCarLevel;

    return position;
  }

  /**
   * Returns the deck name based on the provided tier name. This only works for ISO named tiers. On containerships, above deck tiers start with 80
   * or 82 and below deck tiers start at 00 or 01
   *
   * @param inTierName the name of the tier
   * @return the name of the deck, A for above deck or B for below deck. An empty string will be returned if the deck cannot be determined.
   */
  @NotNull
  protected String getVesselDeck(final String inTierName) {
    String deckName = "";
    try {
      final int tierValue = Integer.parseInt(inTierName);
      if (tierValue >= 70) {
        deckName = "A";
      } else {
        deckName = "B";
      }
    } catch (NumberFormatException e) {
        log(Level.INFO, String.format("Vessel tier coordinate >%s< is not an integer value message %s", inTierName, e.getMessage()));
    }
    return deckName;
  }
  /**
   * Creates xml element and its children for the on board units
   * @param inDomainQuery domain query for the on board units
   * @return on board units xml
   */
  public String createOnBoardUnitsXml(List<PredicateIntf> inPredicateList) {
    def unitXml = "";
    DomainQuery domainQuery = createUnitScalarQuery(inPredicateList);
    unitXml = createUnitXml(domainQuery);
    def writer = new StringWriter();
    def xml = new MarkupBuilder(writer);

    xml."onboard-units"() {
      getMkp().yieldUnescaped("\n" + unitXml + "\n");
    }
    String out = writer.toString();
    return out;
  }
  /**
   * Creates crane work lists element which contains work queues and the units
   * @param inCraneIds a crane or a list of cranes (coma separated)
   * @param inCarrierVisitDetails VisitDetails of the carrier visit whose crane work lists are created
   * @param inCurrentAndNextWqOnly set to true, if only current and the next worklist is desired
   * @return crane work lists element and its children
   */
  public String createCraneWorkListXmlContent(String inCraneIds, VisitDetails inCarrierVisitDetails, boolean inCurrentAndNextWqOnly) {
    CarrierVisit carrierVisit = inCarrierVisitDetails.getCvdCv();
    Map<String, List<Map<MetafieldId, Object>>> wqListByCrane = new HashMap<String, List<Map<MetafieldId, Object>>>();
    if (LocTypeEnum.VESSEL == carrierVisit.getCvCarrierMode()) {
      if (inCurrentAndNextWqOnly) {
        wqListByCrane =  getCurrentAndNextWorkQueuesForCrane(inCraneIds, carrierVisit.getCvId());
      } else {
        wqListByCrane =  getWorkQueueListByCrane(inCraneIds, carrierVisit.getCvId());
      }
    } else if (LocTypeEnum.TRAIN == carrierVisit.getCvCarrierMode()){
      //For train we get all the work queues in all cases, whether they are active or not
      wqListByCrane = getWorkQueueListForTrain(carrierVisit.getCvId());
    }
    def writer = new StringWriter();
    def xml = new MarkupBuilder(writer);
    xml."crane-work-lists"() {
      Set<String> craneIds = wqListByCrane.keySet();
      for (String craneId: craneIds) {
        List<Map<MetafieldId, Object>> wqList = wqListByCrane.get(craneId);
        crane(id: craneId) {
          wqList.each { Map<MetafieldId, Object> wqData ->
            String name = wqData.get(MovesField.WQ_NAME);
            String deck = wqData.get(MovesField.WQ_DECK);
            String row = wqData.get(MovesField.WQ_ROW);
            String vesselLcg = wqData.get(MovesField.WQ_VESSEL_LCG);
            Object wqGkey = wqData.get(MovesField.WQ_GKEY);
            "work-queue"(name: name, deck: deck, row: row, "vessel-lcg": vesselLcg) {
              DomainQuery wiDq = getWiDomainQueryWithoutPredicate();
              wiDq = wiDq.addDqPredicate(PredicateFactory.eq(MovesField.WI_WORK_QUEUE, wqGkey))
                      .addDqPredicate(PredicateFactory.ne(MovesField.WI_MOVE_STAGE, WiMoveStageEnum.COMPLETE))
                      .addDqPredicate(PredicateFactory.ne(MovesField.WI_MOVE_KIND, WiMoveKindEnum.YardShift))
                      .addDqOrdering(Ordering.asc(MovesField.WI_ESTIMATED_MOVE_TIME));

              QueryResult wiQr = HibernateApi.getInstance().findValuesByDomainQuery(wiDq);
              if (wiQr != null && wiQr.getTotalResultCount() != 0) {
                Set<Serializable> ufvGkeys = new LinkedHashSet<Serializable>();
                Map<Serializable, List<ValueHolder>> ufvGkeyWiMap = new LinkedHashMap<Serializable, List<ValueHolder>>();
                Serializable ufvGkey = null;
                for (int k = 0; k < wiQr.getTotalResultCount(); k++) {
                  ufvGkey = (Serializable) wiQr.getValue(k, WI_UFV_GKEY)
                  ufvGkeys.add(ufvGkey);
                  List<ValueHolder> wiList = new ArrayList<ValueHolder>();
                  wiList.add(wiQr.getValueHolder(k));
                  ufvGkeyWiMap.put(ufvGkey, wiList);
                }
                units() {
                  getMkp().yieldUnescaped("\n" +
                          createUnitXml(createUnitScalarQuery(ufvGkeys.toArray(new Serializable[ufvGkeys.size()])), true, ufvGkeyWiMap) + "\n");
                }
              }
            }
          }
        }
      }
    }
    return writer.toString();
  }

  protected DomainQuery getWiDomainQueryWithoutPredicate() {
    DomainQuery wiDq = QueryUtils.createDomainQuery(MovesEntity.WORK_INSTRUCTION)
            .addDqField(WI_UFV_GKEY)
            .addDqField(WI_UNIT_ID)
            .addDqField(WI_WQ_GKEY)
            .addDqField(MovesField.WI_SEQUENCE)
            .addDqField(MovesField.WI_MOVE_KIND)
            .addDqField(MovesField.WI_CRANE_LANE)
            .addDqField(MovesField.WI_DOOR_DIRECTION)
            .addDqField(MovesField.WI_TWIN_WITH)
            .addDqField(MovesField.WI_IS_TANDEM_WITH_NEXT)
            .addDqField(MovesField.WI_IS_TANDEM_WITH_PREVIOUS)
            .addDqField(WI_POS_LOC_TYPE)
            .addDqField(WI_POS_LOC_ID)
            .addDqField(WI_POS_SLOT)
            .addDqField(WI_POS_BLOCK_NAME)
            .addDqField(WI_POS_BLOCK_CONV_ID_ROW_GKEY)
            .addDqField(WI_POS_BLOCK_CONV_ID_ROW_ALT_GKEY)
            .addDqField(WI_POS_BLOCK_CONV_ID_TIER_GKEY)
            .addDqField(WI_POS_ROW_INDEX)
            .addDqField(WI_POS_ROW_CONV_ID_COL_GKEY)
            .addDqField(WI_POS_CELL_SECTION_CONV_ID_TIER_GKEY)
            .addDqField(WI_POS_COLUMN_INDEX)
            .addDqField(WI_POS_TIER)
            .addDqField(WI_EQ_BASIC_LENGTH);
    return wiDq;
  }
  /**
   * Creates a map with crane id as key and list of work queue attributes in it
   * @param inCraneIds a crane or a list of cranes (coma separated), if null then return map contains all cranes
   * @param inCarrierVisitId carrier visit for which the information is desired
   * @return a map of crane ids and list of work queue attributes
   */
  public Map<String, List<Map<MetafieldId, Object>>> getWorkQueueListByCrane(String inCraneIds, String inCarrierVisitId) {
    IWorkFinder workFinder = Roastery.getBean(IWorkFinder.BEAN_ID) as IWorkFinder;
    QueryResult queryResult;
    if (StringUtils.isBlank(inCraneIds)) {
      // "ALL" is replaced by null by the caller for query purposes
      queryResult = workFinder.findAllWQsForCarrierVisit(inCarrierVisitId, Ordering.asc(MovesField.WQ_ORDER));
    } else {
      String[] craneIds = inCraneIds.split(",")
      queryResult = workFinder.findAllWQsForPOW(craneIds, inCarrierVisitId, Ordering.asc(MovesField.WQ_ORDER))
    }
    Map<String, List<Map<MetafieldId, Object>>> wqListByCrane = new HashMap<String, List<Map<MetafieldId, Object>>>();
    for (int j = 0; j < queryResult.getTotalResultCount(); j++) {
      Map<MetafieldId, Object> wq = new HashMap<MetafieldId, Object>();
      wq.put(MovesField.WQ_NAME, queryResult.getValue(j, MovesField.WQ_NAME))
      wq.put(MovesField.WQ_DECK, queryResult.getValue(j, MovesField.WQ_DECK))
      wq.put(MovesField.WQ_ROW, queryResult.getValue(j, MovesField.WQ_ROW))
      wq.put(MovesField.WQ_VESSEL_LCG, queryResult.getValue(j, MovesField.WQ_VESSEL_LCG))
      wq.put(MovesField.WQ_GKEY, queryResult.getValue(j, MovesField.WQ_GKEY))
      String craneId = (String) queryResult.getValue(j, WQ_CRANE_ID);
      List<Map<MetafieldId, Object>> wqList = wqListByCrane.get(craneId);
      if (wqList == null) {
        wqList = new ArrayList<Map<MetafieldId, Object>>();
        wqListByCrane.put(craneId, wqList)
      }
      wqList.add(wq)
    }
    return wqListByCrane;
  }
  /**
   * Returns a list of work queue values for a train visit. This collection is wrapped in a map with key 'ALL' to keep it consistent with the
   * XML API contract
   * @param inCarrierVisitId carrier visit for train for which the information is desired
   * @return a map of crane id(whose value is always 'ALL" and list of work queue attributes
   */
  public Map<String, List<Map<MetafieldId, Object>>> getWorkQueueListForTrain(String inCarrierVisitId) {
    IWorkFinder workFinder = Roastery.getBean(IWorkFinder.BEAN_ID) as IWorkFinder;
    QueryResult queryResult;
    queryResult = workFinder.findWQsForTrainVisit(inCarrierVisitId, Ordering.asc(MovesField.WQ_ORDER))
    Map<String, List<Map<MetafieldId, Object>>> wqListByCrane = new HashMap<String, List<Map<MetafieldId, Object>>>();
    String craneId = "ALL";
    List<Map<MetafieldId, Object>> wqList = new ArrayList<Map<MetafieldId, Object>>();
    wqListByCrane.put(craneId, wqList);
    for (int j = 0; j < queryResult.getTotalResultCount(); j++) {
      Map<MetafieldId, Object> wq = new HashMap<MetafieldId, Object>();
      wq.put(MovesField.WQ_NAME, queryResult.getValue(j, MovesField.WQ_NAME))
      wq.put(MovesField.WQ_DECK, queryResult.getValue(j, MovesField.WQ_DECK))
      wq.put(MovesField.WQ_ROW, queryResult.getValue(j, MovesField.WQ_ROW))
      wq.put(MovesField.WQ_VESSEL_LCG, queryResult.getValue(j, MovesField.WQ_VESSEL_LCG))
      wq.put(MovesField.WQ_GKEY, queryResult.getValue(j, MovesField.WQ_GKEY))
      wqList.add(wq)
    }
    return wqListByCrane;
  }
  /**
   * Creates a map of crane ids and list of work queue attributes
   * @param inCraneId crane id
   * @param inCarrierVisitId carrier visit id
   * @return map of crane ids and list of work queue attributes
   */
  public Map<String, List<Map<MetafieldId, Object>>> getCurrentAndNextWorkQueuesForCrane(String inCraneId, String inCarrierVisitId) {
    Map<String, List<Map<MetafieldId, Object>>> wqListForCrane = new HashMap<String, List<Map<MetafieldId, Object>>>();
    List<Map<MetafieldId, Object>> wqList = new ArrayList<Map<MetafieldId, Object>>();
    wqListForCrane.put(inCraneId, wqList);
    IWorkFinder workFinder = Roastery.getBean(IWorkFinder.BEAN_ID) as IWorkFinder;
    Serializable yardKey = ContextHelper.getYardKey(ContextHelper.getThreadUserContext());
    PointOfWork pow = null;
    if (inCraneId != null) {
      pow = PointOfWork.findPointOfWorkByName(inCraneId, yardKey);
    }
    WorkQueue[] workQueues = new WorkQueue[2]
    /*workQueues[0] = workFinder.findCurrentWorkQueueForPOW(inCraneId)*/
    workQueues[0] = workFinder.findCurrentWorkQueueForPOW(pow, inCarrierVisitId)
    workQueues[1] = workFinder.findNextActiveWorkQueueForPOW(workQueues[0])
    for (int n = 0; n < workQueues.length; n++) {
      if (workQueues[n] != null) {
        Map<MetafieldId, Object> wq = new HashMap<MetafieldId, Object>();
        wq.put(MovesField.WQ_NAME, workQueues[n].getWqName())
        wq.put(MovesField.WQ_DECK, workQueues[n].getWqDeck())
        wq.put(MovesField.WQ_ROW, workQueues[n].getWqRow())
        wq.put(MovesField.WQ_VESSEL_LCG, workQueues[n].getWqVesselLcg())
        wq.put(MovesField.WQ_GKEY, workQueues[n].getWqGkey())
        wqList.add(wq)
      }
    }
    return wqListForCrane;
  }

  /**
   * Creates payload xml with additional info
   * @param inAdditionalInfo additional attributes which would be added to payload in addition to the core content
   * @param inXml core content xml
   * @return payload xml
   */
  public String getXmlPayloadContent(Map<String, String> inAdditionalInfo, String inXml){
    def writer = new StringWriter();
    def xml = new MarkupBuilder(writer);
    xml.payload() {
      if (inAdditionalInfo != null && !inAdditionalInfo.isEmpty()) {
        "additional-info"(){
          inAdditionalInfo.keySet().each {
            field(id:it, value:inAdditionalInfo.get(it));
          }
        }
      }
      getMkp().yieldUnescaped(inXml);
    }
    String out = writer.toString();
    return out;
  }
  /**
   * Creates payload xml with additional info
   * @param inXml core content xml
   * @return payload xml
   */

  public String getXmlPayloadContent(String inXml){
    def writer = new StringWriter();
    def xml = new MarkupBuilder(writer);
    xml.payload() {
      getMkp().yieldUnescaped(inXml);
    }
    String out = writer.toString();
    return out;
  }

  /**
   * Validates the crane id
   * @param inCraneId crane id from the CAS message
   * @throws BizViolation bizViolation
   *   2013-04-15 azharad ARGO-45859 Validate given crane information
   */
  public Che validateCraneId(String inCraneId) throws BizViolation {

    Che currentChe = null;
    boolean isQuayOrRailCrane = false;
    Serializable yardKey = ContextHelper.getYardKey(ContextHelper.getThreadUserContext());
    currentChe = MoveEvent.resolveCheByShortName(inCraneId, yardKey);
    if (currentChe != null) {
      isQuayOrRailCrane = MoveEvent.isQuayCrane(currentChe) || MoveEvent.isRailCrane(currentChe);
    } else {
      LOGGER.error("Could not find Che ["+ inCraneId+"]");
      final BizViolation violation = BizViolation.create(InventoryPropertyKeys.INVALID_CRANE_ID, null, null)
      getMessageCollector().appendMessage(violation);
      throw violation;
    }

    if (!isQuayOrRailCrane) {
      LOGGER.error("Che ["+ inCraneId+"] is neither a quay crane nor a rail crane.");
      final BizViolation violation = BizViolation.create(InventoryPropertyKeys.INVALID_CRANE_ID, null, null)
      getMessageCollector().appendMessage(violation);
      throw violation;
    }
    return currentChe;
  }

  public void recordExceptionServiceEvent(String inCraneName, Entity inRelatedEntity, Yard inYard, EventType inEventType, String inMessage) {
    try {
      Che che = Che.findCheByShortName(inCraneName, inYard);
      if (che == null) {
        LOGGER.error("Could not find Che with name: "+inCraneName+". Cannot record exception: "+inMessage);
        return;
      }
      QcTasks.recordServiceEventOnCheException(che, inRelatedEntity, inEventType, inMessage);
    } catch(Exception e){
      // Failed to send alert...
      LOGGER.error("Error sending exception ServiceEvent.", e);
    }
  }

  /**
   * Converts a property value for insertion into XML.
   *
   * @param inTag   The tag, i.e. attribute or element name
   * @param inValue The raw value from the domain model
   * @return converted and translated value
   */
  @Nullable
  public String translateValue(Object inValue) {
    if (inValue == null) {
      return "";
    }

    if (inValue instanceof Boolean) {
      return translateBoolean((Boolean) inValue);
    }

    if (inValue instanceof Date) {
      return translateDate((Date) inValue);
    }

    String valueString;
    if (inValue instanceof AtomizedEnum) {
      valueString = ((AtomizedEnum) inValue).getKey();
    } else {
      valueString = inValue.toString();
    }
    return valueString;
  }

  protected String translateBoolean(Boolean inValue) {
    return inValue ? Y : N;
  }

  protected String translateDate(Date inValue) {
    if (ContextHelper.getThreadComplex() != null) {
      TimeZone tz = ArgoPropertyResolverProvider.getTimeZone();
      XML_DATE_TIME_SIMPLE_DATE_FORMAT.setTimeZone(tz);
    }
    return XML_DATE_TIME_SIMPLE_DATE_FORMAT.format(inValue);
  }

  /**
   * Helper method for the Groovy code to register a message.
   *
   * @param inLevel message level
   * @param inPropertyKey message key
   */
  protected void registerMessage(MessageLevel inLevel, PropertyKey inPropertyKey) {
    MessageCollector ms = getMessageCollector();
    if (ms != null) {
      ms.appendMessage(inLevel, inPropertyKey, null, null);
    }
  }

  public  final String Y = "Y";
  public  final String N = "N";
  private  final String XML_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
  private  final SimpleDateFormat XML_DATE_TIME_SIMPLE_DATE_FORMAT = new SimpleDateFormat(XML_DATE_TIME_FORMAT);

  private static Logger LOGGER = LogManager.getLogger(CasHelper.class);
}
