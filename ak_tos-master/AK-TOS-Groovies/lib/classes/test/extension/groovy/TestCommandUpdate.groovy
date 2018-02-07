package test.extension.groovy

import com.navis.argo.ArgoEntity
import com.navis.argo.ArgoField
import com.navis.argo.ArgoRefEntity
import com.navis.argo.ArgoRefField
import com.navis.argo.ContextHelper
import com.navis.argo.business.api.Serviceable
import com.navis.argo.business.api.ServicesManager
import com.navis.argo.business.atoms.CarrierVisitPhaseEnum
import com.navis.argo.business.atoms.EventEnum
import com.navis.argo.business.atoms.FlagPurposeEnum
import com.navis.argo.business.atoms.LocTypeEnum
import com.navis.argo.business.atoms.LogicalEntityEnum
import com.navis.argo.business.atoms.ServiceQuantityUnitEnum
import com.navis.argo.business.atoms.TankRailTypeEnum
import com.navis.argo.business.atoms.UnitCategoryEnum
import com.navis.argo.business.atoms.WiMoveStageEnum
import com.navis.argo.business.model.CarrierVisit
import com.navis.argo.business.model.Facility
import com.navis.argo.business.model.LocPosition
import com.navis.argo.business.model.Quay
import com.navis.argo.business.model.Yard
import com.navis.argo.business.reference.Container
import com.navis.argo.business.reference.EquipType
import com.navis.argo.business.reference.Equipment
import com.navis.argo.business.xps.model.PointOfWork
import com.navis.argo.business.xps.model.WorkShift
import com.navis.framework.business.Roastery
import com.navis.framework.persistence.DatabaseHelper
import com.navis.framework.persistence.HibernateApi
import com.navis.framework.persistence.hibernate.CarinaPersistenceCallback
import com.navis.framework.portal.FieldChanges
import com.navis.framework.portal.QueryUtils
import com.navis.framework.portal.query.DomainQuery
import com.navis.framework.portal.query.PredicateFactory
import com.navis.framework.util.BizViolation
import com.navis.framework.util.ValueHolder
import com.navis.framework.util.ValueObject
import com.navis.inventory.InventoryBizMetafield
import com.navis.inventory.InventoryEntity
import com.navis.inventory.InventoryField
import com.navis.inventory.MovesEntity
import com.navis.inventory.MovesField
import com.navis.inventory.business.api.SearchResults
import com.navis.inventory.business.api.UnitField
import com.navis.inventory.business.api.UnitFinder
import com.navis.inventory.business.api.UnitManager
import com.navis.inventory.business.api.UnitReroutePoster
import com.navis.inventory.business.atoms.DoorDirectionEnum
import com.navis.inventory.business.atoms.EqDamageSeverityEnum
import com.navis.inventory.business.atoms.WiEcStateEnum
import com.navis.inventory.business.imdg.HazardItem
import com.navis.inventory.business.imdg.HazardousGoods
import com.navis.inventory.business.imdg.Hazards
import com.navis.inventory.business.imdg.ImdgClass
import com.navis.inventory.business.moves.WorkInstruction
import com.navis.inventory.business.units.EquipmentState
import com.navis.inventory.business.units.GoodsBase
import com.navis.inventory.business.units.ReeferRecord
import com.navis.inventory.business.units.ReeferRqmnts
import com.navis.inventory.business.units.Unit
import com.navis.inventory.business.units.UnitEquipment
import com.navis.inventory.business.units.UnitFacilityVisit
import com.navis.rail.RailField
import com.navis.rail.business.entity.TrainVisitDetails
import com.navis.services.ServicesField
import com.navis.services.business.atoms.ServiceRuleTypeEnum
import com.navis.services.business.rules.EventType
import com.navis.services.business.rules.FlagType
import com.navis.services.business.rules.HoldPermissionView
import com.navis.services.business.rules.ServiceRule
import com.navis.services.business.rules.ServiceTestUtils
import com.navis.vessel.VesselEntity
import com.navis.vessel.VesselField
import com.navis.vessel.api.VesselVisitField
import com.navis.vessel.business.schedule.VesselVisitBerthing
import com.navis.vessel.business.schedule.VesselVisitDetails
import com.navis.xpscache.business.atoms.EquipBasicLengthEnum
import org.apache.log4j.Logger

import java.util.concurrent.TimeUnit

/**
 * Created with IntelliJ IDEA.
 * User: rajansh
 * Date: 29/10/13
 * Time: 5:28 PM
 * To change this template use File | Settings | File Templates.
 */
class TestCommandUpdate {
  /** holds the result returned by each method */
  def String returnString = null;
  /** json builder, frames the output in json format */
  def builder = new groovy.json.JsonBuilder();
  private DatabaseHelper _teamsDbHelper;
  /** handler to invoke the fields in helper class */
  test.extension.groovy.TestCommandHelper _testCommandHelper = new test.extension.groovy.TestCommandHelper();

  //Logger for TestCommand
  public Logger LOGGER = Logger.getLogger(TestCommandUpdate.class);

  public String UpdateBerthingCallRemarks(Map inParameters) {
    assert inParameters.size() >= 3, '''Must supply 3 parameters:
                                        <parameter id="command" value="UpdateBerthingCallRemarks" />
					                    <parameter id="vesselId" value="<name of the vessel visit>" />
					                    <parameter id="quayId" value="<quay of the vessel visit>" />
					                    <parameter id="bollardAftOffset" value="<Bollard Aft offset (cm)>" />
                                        <parameter id="bollardForeOffset" value="<Bollard Fore offset (cm)>" />
                                        <parameter id="berthATA" value="<BERTH ATA timestamp>" />
                                        <parameter id="berthStartWorkTime" value="<Start work time stamp>" />
					                    <parameter id="sequence" value="1"'''
    String inVesselId = _testCommandHelper.checkParameter('vesselId', inParameters);
    String inQuayId = _testCommandHelper.checkParameter('quayId', inParameters);
    String inSequence = _testCommandHelper.checkParameter('sequence', inParameters);
    String bollardAftOffset = inParameters.get('bollardAftOffset');
    String bollardForeOffset = inParameters.get('bollardForeOffset');
    String berthATA = '', berthStartWorkTime = ''

    if (inParameters.containsKey('berthATA') && !inParameters.get('berthATA').toString().isEmpty()) {
      berthATA = inParameters.get('berthATA').toString()
    }

    if (inParameters.containsKey('berthStartWorkTime') && !inParameters.get('berthStartWorkTime').toString().isEmpty()) {
      berthStartWorkTime = inParameters.get('berthStartWorkTime').toString()
    }

    def callRemarks = new Date().format("dd-MM-yyyy HH:mm")

    CarrierVisit carrierVisit = (CarrierVisit) HibernateApi.getInstance().getUniqueEntityByDomainQuery(
            QueryUtils.createDomainQuery(ArgoEntity.CARRIER_VISIT)
                    .addDqPredicate(PredicateFactory.eq(ArgoField.CV_ID, inVesselId))
                    .addDqPredicate(PredicateFactory.eq(ArgoField.CV_CARRIER_MODE, LocTypeEnum.VESSEL))
    );
    FieldChanges changes = new FieldChanges();
    if (carrierVisit != null) {
      VesselVisitDetails vesselVisitDetails = resolveVvdFromCv(carrierVisit);
      Facility facility = vesselVisitDetails.getCvdCv().getCvFacility();
      Quay quay = Quay.findOrCreateQuay(facility, inQuayId);
      if (quay != null) {
        DomainQuery dq = QueryUtils.createDomainQuery(VesselEntity.VESSEL_VISIT_BERTHING)
                .addDqPredicate(PredicateFactory.eq(VesselField.BRTHG_QUAY, quay.getPrtlGkey()))
                .addDqPredicate(PredicateFactory.eq(VesselField.BRTHG_VVD, vesselVisitDetails.getCvdGkey()))
                .addDqPredicate(PredicateFactory.eq(VesselField.BRTHG_SEQ, inSequence.toLong()));
        VesselVisitBerthing vvb = HibernateApi.getInstance().getUniqueEntityByDomainQuery(dq)
        if (vvb != null) {
          changes.setFieldChange(VesselVisitField.BRTHG_ABS_CALL_REMARKS, callRemarks.toString());
          if (!bollardAftOffset.isEmpty()) {
            changes.setFieldChange(VesselVisitField.BRTHG_ABS_BOLLARD_AFT_OFFSET, bollardAftOffset)
          };
          if (!bollardForeOffset.isEmpty()) {
            changes.setFieldChange(VesselVisitField.BRTHG_ABS_BOLLARD_FORE_OFFSET, bollardForeOffset)
          };
          if (!berthATA.isEmpty()) {
            changes.setFieldChange(VesselVisitField.BRTHG_A_T_A, _testCommandHelper.calculateTime(berthATA))
          };
          if (!berthStartWorkTime.isEmpty()) {
            changes.setFieldChange(VesselVisitField.BRTHG_TIME_START_WORK, _testCommandHelper.calculateTime(berthStartWorkTime))
          };
          vvb.applyFieldChanges(changes)
          returnString = 'Updated call remarks for the given vessel berthing : ' + inVesselId
        } else {
          returnString = 'Updating call remarks failed, berthing/sequence not found for the vessel visit :' + inVesselId
        }
      } else {
        returnString = 'Updating call remarks failed, given quay ' + inQuayId + 'not found for the vessel visit :' + inVesselId
      }
    } else {
      returnString = 'Updating call remarks failed, given vesselVisit ' + inVesselId + 'not found'
    }
    LOGGER.debug('UpdateBerthingCallRemarks' + returnString)
    builder {
      actual_result returnString
    }
    return builder;
  }

  /**
   * Updates the work instruction attributes<br>
   * Input is passed in a comma separated format, where each value is a key-value pair.<br>
   * The values are iterated to update each field value in DB<br>
   * MOVE_STAGE : PLANNED/FETCH_UNDERWAY/CARRY_READY/CARRY_UNDERWAY/<br>
   * CARRY_COMPLETE/PUT_UNDERWAY/COMPLETE/NONE<br>
   * DISPATCH_STATE : True/False<br>
   * CHE_FETCH : STANDARD_FETCH/PRIORITY_FETCH/PRIORITY_DISPATCH/NONE<br>
   * CARRY_PUT : True/False<br>
   * CHE_PUT : True/False<br>
   * MOVE_TIME : date <br>
   * SERVICE_TIME : time
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=UpdateWorkInstruction<br>
   * wiGKey=work instruction gKey <br>
   * attributes=attribute key value pair<br>
   * For updated list of work instruction fields that are supported, please refer the block under<br>
   * Work Instruction Attributes in TestCommandHelper.groovy
   * @return <code>WorkInstruction updated with given values</code> - if
   *          the details are updated in DB successfully; <br>
   *          <code>WorkInstruction not updated,please check the input
   *          format</code> - if the input format is wrong; <br>
   *          <code>WorkInstruction updated for available fields</code> - if<br>
   *          any one or more of the attributes supplied is not available in<br>
   *          the pre-determined attribute list <br>
   *          <code>WorkInstruction not updated</code> - if WorkInstruction  itself
   *          is null or any other internal exception occurs.
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="UpdateWorkInstruction" /&gt;<br>
   * &lt;parameter id="wiGKey" value="94" /&gt;<br>
   * &lt;parameter id="attributes" value="MOVE_STAGE=FETCH_UNDERWAY,MOVE_TIME=01/10/2013" /&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String UpdateWorkInstruction(Map inParameters) {
    assert inParameters.size() == 3, '''Must supply 3 parameters:
                                        <parameter id="command" value="UpdateWorkInstruction" />
					                    <parameter id="wiGKey" value="<work instruction gKey>" />
					                    <parameter id="attributes" value="" />'''
    String inWIGKey = _testCommandHelper.checkParameter('wiGKey', inParameters);
    String inAttributes = _testCommandHelper.checkParameter('attributes', inParameters);
    String[] attributeList = inAttributes.split(',');
    String key, value, notFoundAttr = "";
    String[] attributeArray = "";
    DomainQuery domainQuery = null;
    //Move_Stage='value',Dispatch_State='value,Che_Fetch='value'>

    try {
      domainQuery = QueryUtils.createDomainQuery(MovesEntity.WORK_INSTRUCTION).addDqPredicate(PredicateFactory.disjunction()
              .add(PredicateFactory.eq(MovesField.WI_GKEY, inWIGKey)));
      WorkInstruction workInstruction = (WorkInstruction) HibernateApi.getInstance().getUniqueEntityByDomainQuery(domainQuery);
      if (workInstruction != null) {
        attributeList.each {
          attributeArray = it.trim().split("=")
          if (attributeArray.size() == 2) {
            key = attributeArray[0].trim();
            value = attributeArray[1].trim();
            switch (key) {
              case 'MOVE_STAGE':
                workInstruction."$_testCommandHelper.MOVE_STAGE" = WiMoveStageEnum."$value";
                break;
              case 'CHE_FETCH':
                workInstruction."$_testCommandHelper.CHE_FETCH" = WiEcStateEnum."$value";
                break;
              case 'MOVE_TIME':
                workInstruction."$_testCommandHelper.MOVE_TIME" = _testCommandHelper.calculateTime(value);
                break;
              default:
                notFoundAttr = 'Attribute : ' + key + ' not found in the list';
            }
            if (!notFoundAttr.isEmpty()) {
              returnString = 'WorkInstruction updated for the available fields.' + ' [Warning: ' + notFoundAttr + "]"
            } else {
              returnString = 'WorkInstruction updated with given values'
            };
            HibernateApi.getInstance().save(workInstruction);
            HibernateApi.getInstance().flush();
          } else {
            returnString = 'WorkInstruction not updated - please check the input format'
          }
        }
      } else {
        returnString = 'WorkInstruction not updated - WI is null'
      }
    } catch (Exception ex) {
      returnString = 'WorkInstruction not updated : ' + ex;
    }
    builder {
      actual_result returnString;
    }
    LOGGER.debug('UpdateWorkInstruction:' + returnString + 'for the wiGKey :' + inWIGKey)
    return builder;
  }

  /** Updates the work shift fields<br>
   * Input is passed in a comma separated format, where each value is a key-value pair.<br>
   * The values are iterated to update each field value in DB[START_TIME=<date>,DURATION=<in hours>]
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=UpdateWorkShift<br>
   * workShiftName=workShiftName<br>
   * powName=powName<br>
   * attributes=START_TIME=current(for current date time),current+3D (to increment 3 days), current +3H (in increment 3 hours)
   *            DURATION=in hours
   * For updated list of work shift fields that are supported, please refer the block under<br>
   * Work Shift Attributes in TestCommandHelper.groovy
   * @return <code>WorkShift updated with given values</code> - if
   *          the details are updated in DB successfully; <br>
   *          <code>WorkShift not updated,please check the input
   *          format</code> - if the input format is wrong; <br>
   *          <code>WorkShift updated for available fields</code> - if
   *          any one or more of the attributes supplied is not available in
   *          the pre-determined attribute list <br>
   *          <code>WorkShift not updated</code> - if Work shift  itself
   *          is null or any other internal exception occurs.
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="UpdateWorkShift" /&gt;<br>
   * &lt;parameter id="powNAme" value="QC05" /&gt;<br>
   * &lt;parameter id="workShiftName" value="WS05" /&gt;<br>
   * &lt;parameter id="attributes" value="START_TIME=current, DURATION= 9 /&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String UpdateWorkShift(Map inParameters) {
    assert inParameters.size() == 4, '''Must supply 4 parameters:
                                        <parameter id="command" value="UpdateWorkShift" />
					                    <parameter id="workShiftName" value="<visitName>" />
					                    <parameter id="powName" value="<powName>" />
					                    <parameter id="attributes" value="START_TIME=<date>,DURATION=<in hours>" />'''
    String inVisitName = _testCommandHelper.checkParameter('workShiftName', inParameters);
    String inPowName = _testCommandHelper.checkParameter('powName', inParameters);
    String inAttributes = _testCommandHelper.checkParameter('attributes', inParameters);
    String[] attributeList = inAttributes.split(',');
    String key, value, notFoundAttr = "";
    String[] attributeArray = "";

    try {
      DomainQuery dq = QueryUtils.createDomainQuery(ArgoEntity.POINT_OF_WORK)
              .addDqPredicate(PredicateFactory.eq(ArgoField.POINTOFWORK_NAME, inPowName));
      PointOfWork pointOfWork = ((PointOfWork) HibernateApi.getInstance().getUniqueEntityByDomainQuery(dq));
      if (pointOfWork != null) {
        List<WorkShift> workShiftList = (List<WorkShift>) HibernateApi.getInstance().findEntitiesByDomainQuery(QueryUtils.createDomainQuery(ArgoEntity.WORK_SHIFT)
                .addDqPredicate(PredicateFactory.eq(ArgoField.WORKSHIFT_SHIFT_NAME, inVisitName))
                .addDqPredicate(PredicateFactory.eq(ArgoField.WORKSHIFT_OWNER_POW_PKEY, pointOfWork.getPointofworkPkey())));
        if (workShiftList != null) {
          if (workShiftList.size() > 0) {
            attributeList.each {
              attributeArray = it.trim().split("=")
              if (attributeArray.size() == 2) {
                key = attributeArray[0].trim();
                value = attributeArray[1].trim();
                switch (key) {
                  case 'START_TIME':
                    workShiftList.get(0)."$_testCommandHelper.START_TIME" = _testCommandHelper.calculateTime(value);
                    break;
                  case 'DURATION':
                    workShiftList.get(0)."$_testCommandHelper.DURATION" = TimeUnit.HOURS.toMillis(value.toLong());
                    break;
                  default:
                    notFoundAttr = 'Attribute : ' + key + ' not found in the list';
                }
                if (!notFoundAttr.isEmpty()) {
                  returnString = 'WorkShift updated for available fields.' + '[Warning:' + notFoundAttr + "]"
                } else {
                  returnString = 'WorkShift updated with given values'
                };
                HibernateApi.getInstance().save(workShiftList.get(0));
                HibernateApi.getInstance().flush();
              } else {
                returnString = 'WorkShift not updated - please check the input format'
              }
            }
          } else {
            returnString = 'WorkShift not updated - WorkShift not found'
          }
        } else {
          returnString = 'WorkShift not updated - WorkShift not found'
        }
      } else {
        returnString = 'WorkShift not updated - POW not found'
      }
    } catch (Exception ex) {
      returnString = 'WorkShift not updated : ' + ex;
    }
    builder {
      actual_result returnString;
    }
    LOGGER.debug('UpdateWorkShift:' + returnString + 'for the workshift :' + inVisitName)
    return builder;
  }

/** Updates the unit fields<br>
 * Input is passed in a comma separated format, where each value is a key-value pair.<br>
 * The values are iterated to update each field value in DB[REEFER_CONNECT=true,DAMAGE_FLAG,IMO_CODES]
 *
 * @param inParameters The map containing the method name to call along with the parameters<br>
 * command=UpdateUnitAttribute<br>
 * unitId=unitId<br>
 * attributes=REEFER_CONNECT,DAMAGE_FLAG,HEIGHT_MM,WEIGHT_KG,CURRENT_POSITION,INBOUND_CARRIER,IS_OOG
 * For updated list of work shift fields that are supported, please refer the block under<br>
 * Work Shift Attributes in TestCommandHelper.groovy
 * @return <code>Unit updated with given values</code> - if
 *          the details are updated in DB successfully; <br>
 *          <code>Unit not updated,please check the input
 *          format</code> - if the input format is wrong; <br>
 *          <code>Unit updated for available fields</code> - if
 *          any one or more of the attributes supplied is not available in
 *          the pre-determined attribute list <br>
 *          <code>Unit not updated</code> - if Unit itself
 *          is null or any other internal exception occurs.
 * @Example
 * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
 * &lt;parameters&gt;<br>
 * &lt;parameter id="command" value="UpdateUnitAttribute" /&gt;<br>
 * &lt;parameter id="unitId" value="TEST0000001" /&gt;<br>
 * &lt;parameter id="attributes" value="REEFER_POWERED=true,DAMAGE_FLAG /&gt;<br>
 * &lt;/parameters&gt;<br>
 * &lt;/groovy&gt;<br>
 */
  public String UpdateUnitAttribute(Map inParameters) {
    assert inParameters.size() >= 3, '''Must supply 3 parameters:
                                       <parameter id="command" value="UpdateUnitAttribute" />
                                       <parameter id="unitId" value="<unitId>" />
                                       <parameter id="attributes" value="REEFER_POWERED,DAMAGE_FLAG" />
                                       <parameter id="holdId" value="<holdId>" />'''
    String inUnitId = _testCommandHelper.checkParameter('unitId', inParameters);
    String inAttributes = _testCommandHelper.checkParameter('attributes', inParameters);
    String[] attributeList = inAttributes.split(',');
    String key, value, notFoundAttr = "";
    String[] attributeArray = "";

    try {
      Unit unitObj = ((Unit) HibernateApi.getInstance().getUniqueEntityByDomainQuery(QueryUtils.createDomainQuery(InventoryEntity.UNIT)
              .addDqPredicate(PredicateFactory.eq(InventoryField.UNIT_ID, inUnitId))));
      if (unitObj != null) {
        UnitFinder finder = (UnitFinder) Roastery.getBean(UnitFinder.BEAN_ID);
        SearchResults results = finder.findUfvByDigits(inUnitId, false, false);
        assert results.getFoundCount() == 1, 'More than one equipment found for id ' + inUnitId;
        UnitFacilityVisit ufv = (UnitFacilityVisit) HibernateApi.getInstance().load(UnitFacilityVisit.class, results.getFoundPrimaryKey());
        EquipType equipType;
        Equipment equipment = ((Equipment) HibernateApi.getInstance().getUniqueEntityByDomainQuery(QueryUtils.createDomainQuery(ArgoRefEntity.EQUIPMENT)
                .addDqPredicate(PredicateFactory.eq(ArgoRefField.EQ_ID_FULL, inUnitId))));
        ReeferRecord reeferRecord;
        UnitEquipment unitEquipment = ((UnitEquipment) HibernateApi.getInstance().getUniqueEntityByDomainQuery(QueryUtils.createDomainQuery(InventoryEntity.UNIT_EQUIPMENT)
                .addDqPredicate(PredicateFactory.eq(InventoryField.UE_UNIT, unitObj.unitGkey))));
        ServicesManager srvcMgr = (ServicesManager) Roastery.getBean(ServicesManager.BEAN_ID);
        EventType eventType = EventType.findEventType(EventEnum.UNIT_PROPERTY_UPDATE.getKey());
        FieldChanges changes = new FieldChanges();
        Long oogBack, oogFront, oogLeft, oogRight, oogTop = null;
        String seal1, seal2, seal3, seal4 = null;
        attributeList.each {
          attributeArray = it.trim().split("=")
          if (attributeArray.size() == 2) {
            key = attributeArray[0].trim();
            value = attributeArray[1].trim();
            switch (key) {
              case 'HOLD':
                if (unitObj != null) {
                  try {
                    String inHold = 'YARD HOLD'  //by default set it for yard hold
                    if(inParameters.containsKey('holdId')) {  //if user supplied the hold id take that
                      inHold = inParameters.get('holdId', inParameters);
                    }
                    ServicesManager sm = (ServicesManager) Roastery.getBean(ServicesManager.BEAN_ID);
                    final String EVENT_ID = "EVNT_".concat(inHold);
                    final String FLAG_TYPE_ID = inHold
                    final String SERVICE_RULE_ID = "SRVC_RLE_ON_HLD";
                    //create hold
                    HoldPermissionView hpv = HoldPermissionView.findHoldPermissionView(FLAG_TYPE_ID);
                    if (hpv == null) {
                      hpv = HoldPermissionView.createHoldPermissionView(FLAG_TYPE_ID);
                      //Create event, flag(hold) and service rule...
                      LogicalEntityEnum entityEnum = LogicalEntityEnum.UNIT;
                      EventType testEventType = EventType.findOrCreateEventType(EVENT_ID, EVENT_ID, entityEnum, null);
                      FlagType holdOrPermFlagType = ServiceTestUtils.findOrCreateFlagType(FLAG_TYPE_ID, "flag", true, entityEnum);
                      holdOrPermFlagType.setFieldValue(ServicesField.FLGTYP_APPLY_EVENT_TYPE, testEventType);
                      holdOrPermFlagType.setFieldValue(ServicesField.FLGTYP_HOLD_PERM_VIEW, hpv);
                      ServiceTestUtils.findOrCreateServiceRule(SERVICE_RULE_ID, ServiceRuleTypeEnum.SIMPLE_HOLD, null, null, testEventType, holdOrPermFlagType);
                    }
                  if(value.equalsIgnoreCase('true')) {   //apply hold
                    sm.applyHold(FLAG_TYPE_ID, unitObj, null, null, 'Hold Applied');
                  } else if(value.equalsIgnoreCase('false')) {  //release hold
                    sm.applyPermission(FLAG_TYPE_ID, unitObj, null, null, "Hold Released");
                  } else  notFoundAttr = 'Please specify either true/false to apply/release hold';
                  }
                  catch (BizViolation ex) {
                    ex.printStackTrace()
                  }
                };
                break;
              case 'WEIGHT_KG':
                if (unitObj != null) {
                  String WEIGHT_KG = 'unitGoodsAndCtrWtKg'
                  unitObj."$WEIGHT_KG" = Double.parseDouble(value);
                  changes.setFieldChange(UnitField.UNIT_GOODS_AND_CTR_WT_KG, Double.parseDouble(value));
                };
                break;
              case 'HEIGHT_MM':
                if (equipment != null) {
                  String HEIGHT_MM = 'eqHeightMm'
                  equipment."$HEIGHT_MM" = Long.parseLong(value);
                };
                break;
              case 'CATEGORY':
                if (unitObj != null) {
                  UnitCategoryEnum oldCategory = unitObj.getUnitCategory();
                  UnitCategoryEnum newCategory;
                  if(value.equalsIgnoreCase('IMPORT') || value.equalsIgnoreCase('EXPORT') || value.equalsIgnoreCase('THROUGH') ||
                          value.equalsIgnoreCase('TRANSSHIP') || value.equalsIgnoreCase('DOMESTIC') || value.equalsIgnoreCase('STORAGE')) // get the string value from enum
                    newCategory = UnitCategoryEnum."$value"
                  else if(value.equalsIgnoreCase('IMPRT') || value.equalsIgnoreCase('EXPRT') || value.equalsIgnoreCase('TRSHP') ||
                          value.equalsIgnoreCase('DMSTC') || value.equalsIgnoreCase('STRGE') || value.equalsIgnoreCase('THRGH')) //get the enum value
                    newCategory = UnitCategoryEnum.getEnum(value);
                  if (newCategory != null) {
                    unitObj.setUnitCategory(newCategory);
                    changes.setFieldChange(UnitField.UNIT_CATEGORY, oldCategory, newCategory);
                  } else {
                    notFoundAttr = 'Please specify any one of the valid category';
                  }
                };
                break;
              case 'TANK_RAIL_TYPE':
                if (ufv != null) {
                  TankRailTypeEnum tankType = TankRailTypeEnum.getEnum(value)
                  if (tankType != null) {
                    Equipment eq = ufv.getUfvUnit().getPrimaryEq();
                    Container ctr = Container.resolveCtrFromEq(eq);
                    ctr.setFieldValue(ArgoRefField.EQ_TANK_RAILS, tankType);
                    HibernateApi.getInstance().update(ctr);
                  } else {
                    notFoundAttr = 'Please specify any one of the valid rail type: TOP, BOTTOM, BOTH or NONE'
                  }
                }
                break;
              case 'HAZARD':
                if (unitObj != null) {
                  //this is how hazard entity needs to be created and attached
                  HazardousGoods unNumber = HazardousGoods.findHazardousGoods(value);
                  Hazards hazards = Hazards.createHazardsEntity();
                  HazardItem hazardItem = null;
                  ValueHolder[] vh = (ValueHolder[]) unitObj.getHazardsHazItemsVao();
                  if (vh != null && vh.size() > 0) {
                    for (ValueObject vo : vh) {
                      ImdgClass imdg = vo.getFieldValue(InventoryField.HZRDI_IMDG_CLASS);
                      hazardItem = HazardItem.createHazardItemEntity(hazards, imdg, "");
                    }
                  }
                  if (unNumber != null) {
                    ImdgClass imdgClass = unNumber.getHzgoodsImdgClass();
                    hazardItem = HazardItem.createHazardItemEntity(hazards, imdgClass, "");
                    hazardItem.setFieldValue(InventoryField.HZRDI_EMERGENCY_TELEPHONE, "787545454");
                    HibernateApi.getInstance().saveOrUpdate(hazardItem);
                    unitObj.attachHazards(hazards);
                    HibernateApi.getInstance().saveOrUpdate(unitObj);
                  } else {
                    notFoundAttr = 'Please specify any one of the valid UN Number';
                  }

                };
                break;
              case 'OOG_BACK_CM':
                oogBack = Long.valueOf(value);
                break;
              case 'OOG_FRONT_CM':
                oogFront = Long.valueOf(value);
                break;
              case 'OOG_LEFT_CM':
                oogLeft = Long.valueOf(value);
                break;
              case 'OOG_RIGHT_CM':
                oogRight = Long.valueOf(value);
                break;
              case 'OOG_TOP_CM':
                oogTop = Long.valueOf(value);
                break;
              case 'SEAL1':
                seal1 = value;
                break;
              case 'SEAL2':
                seal2 = value;
                break;
              case 'SEAL3':
                seal3 = value;
                break;
              case 'SEAL4':
                seal4 = value;
                break;
              case 'RFR_TEMP_REQD':
                if (unitObj != null) {
                  GoodsBase goods = unitObj.ensureGoods();
                  ReeferRqmnts reeferRqmnts = goods.ensureGdsReeferRqmnts();
                  reeferRqmnts.setRfreqTempRequiredC(Double.parseDouble(value));

                  unitObj.setFieldValue(InventoryField.UNIT_REQUIRES_POWER, true);
                }
                break;
              case 'ORIENTATION':
                if (ufv != null) {
                  DoorDirectionEnum posOrientation
                  if(value.equalsIgnoreCase('UNKNOWN') || value.equalsIgnoreCase('ANY') || value.equalsIgnoreCase('FWD') ||
                          value.equalsIgnoreCase('AFT') || value.equalsIgnoreCase('NORTH') || value.equalsIgnoreCase('SOUTH')) // get the string value from enum
                    posOrientation = DoorDirectionEnum."$value"
                  else if(value.equalsIgnoreCase('U') || value.equalsIgnoreCase('Y') || value.equalsIgnoreCase('F') ||
                          value.equalsIgnoreCase('A') || value.equalsIgnoreCase('N') || value.equalsIgnoreCase('S')) //get the enum value
                    posOrientation = DoorDirectionEnum.getEnum(value);

                  if (posOrientation != null) {
                    LocPosition oldPos = ufv.getUfvLastKnownPosition();
                    LocPosition newPos = LocPosition.createLocPosition(oldPos.getPosLocType(), oldPos.getPosLocId(), oldPos.getPosLocGkey(),
                            oldPos.getPosSlot(), posOrientation.getKey());
                    ufv.correctPosition(newPos, true);
                  } else {
                    notFoundAttr = 'Please specify any one of the valid door direction';
                  }
                }
                break;
              case 'ISO_TYPE':
                if (unitEquipment != null) {
                  EquipType oldEqType = unitEquipment.getUeEquipment().getEqEquipType();
                  equipType = EquipType.findOrCreateEquipType(value);
                  equipment.eqEquipType = equipType
                  changes.setFieldChange(UnitField.UNIT_PRIMARY_EQUIP_TYPE, oldEqType.getEqtypGkey(), equipType.getEqtypGkey());
                  CarrierVisit cv = ufv.getUfvActualIbCv();
                  if (cv != null && cv.getCvSendOnBoardUnitUpdates() && cv.getCvVisitPhase().equals(CarrierVisitPhaseEnum.WORKING)) {
                    EventType onBoardEvent = EventType.findEventType("UNIT_ON_BOARD_UPDATE");
                    srvcMgr.recordEvent(onBoardEvent, null, 0, ServiceQuantityUnitEnum.UNKNOWN, unitObj, changes);
                  }
                }
                break;
              case 'REEFER_POWERED':
                if (unitObj != null) {
                  unitObj.setFieldValue(InventoryField.UNIT_IS_POWERED, Boolean.parseBoolean(value));
                }
                break;
              case 'DAMAGE_FLAG':
                if (unitEquipment != null) {
                  EqDamageSeverityEnum dmgSeverity = EqDamageSeverityEnum.getEnum(value);
                  if (dmgSeverity != null) {
                    unitEquipment.setUeDamageSeverity(dmgSeverity);
                  } else {
                    notFoundAttr = 'Please specify any one of the valid Damage severity';
                  }
                };
                break;
              case 'CURRENT_POSITION':
                if (ufv != null) {
                  //2013-07-22 oviyak 2.6.H ARGO-49887 Fixed to update current or complex position correctly.
                  String[] inValues = value.split('-');
                  String vslId = inValues[1];
                  String posID = inValues[2];
                  CarrierVisit cv = CarrierVisit.findOrCreateVesselVisit(ContextHelper.getThreadFacility(), vslId);
                  LocPosition lastKnownPos = LocPosition.createVesselPosition(cv, posID, null, false)
                  ufv."$_testCommandHelper.CURRENT_POSITION" = lastKnownPos
                  changes.setFieldChange(UnitField.UNIT_CURRENT_POSITION, lastKnownPos);
                };
                break;
              case 'LAST_POSITION':
                if (ufv != null) {
                  CarrierVisit cv = CarrierVisit.findOrCreateVesselVisit(ContextHelper.getThreadFacility(), value);
                  LocPosition lastKnownPos = LocPosition.createVesselPosition(cv, value, null, false)
                  ufv."$_testCommandHelper.LAST_POSITION" = lastKnownPos
                };
                break;
              case 'LOGICAL_POSITION':
                if (unitObj != null) {
                  boolean isApron = LocPosition.isApron(value)
                  if (isApron) {
                    final Yard yard = ContextHelper.getThreadYard();
                    LocPosition locPosition = LocPosition.createYardPosition(yard, value, null, EquipBasicLengthEnum.BASIC20, false);
                    unitObj.move(locPosition)
                  } else {
                    notFoundAttr = 'Logical block : APRON is only supported now'
                  }
                }
                break;
              case 'INBOUND_CARRIER':
                if (unitObj != null) {
                  CarrierVisit oldCv = unitObj.getUnitDeclaredIbCv();
                  CarrierVisit cv = CarrierVisit.findOrCreateVesselVisit(ContextHelper.getThreadFacility(), value);
                  changes.setFieldChange(UnitField.UNIT_CURRENT_UFV_ACTUAL_IB_CV, oldCv.getCvGkey(), cv.getCvGkey());
                  EventType reRoute = EventType.findEventType(EventEnum.UNIT_REROUTE.getKey());
                  srvcMgr.recordEvent(reRoute, null, 0, ServiceQuantityUnitEnum.UNKNOWN, unitObj, changes);
                 /* if (oldCv != null && oldCv.getCvSendOnBoardUnitUpdates() && oldCv.getCvVisitPhase().equals(CarrierVisitPhaseEnum.WORKING)) {
                    EventType onBoardEvent = EventType.findEventType("UNIT_ON_BOARD_UPDATE");
                    srvcMgr.recordEvent(onBoardEvent, null, 0, ServiceQuantityUnitEnum.UNKNOWN, unitObj, changes);
                    isInbound = true
                  }*/
                  unitObj."$_testCommandHelper.INBOUND_CARRIER" = cv
                };
                break;
              case 'OUTBOUND_CARRIER':
                if (unitObj != null) {
                  CarrierVisit oldCv = unitObj.getOutboundCv();
                  CarrierVisit cv = CarrierVisit.findOrCreateVesselVisit(ContextHelper.getThreadFacility(), value);
                  if (cv != null) {
                    FieldChanges fcs = new FieldChanges();
                    fcs.setFieldChange(UnitField.UFV_INTENDED_OB_CV, cv.getCvGkey());
                    fcs.setFieldChange(UnitField.UFV_DECLARED_OB_CV, cv.getCvGkey());
                    Serializable[] ufvGkey = new Serializable[1];
                    ufvGkey[0] = ufv.getUfvGkey();
                    getRerouteMngr().updateRouting(ufvGkey, fcs);
                  } else {
                    notFoundAttr = "Please specify a valid carrier";
                  }
                };
                break;
              default:
                notFoundAttr += 'Attribute : ' + key + ' not found in the list';
            }
            unitObj.applyFieldChanges(changes);
            srvcMgr.recordEvent(eventType, null, 0, ServiceQuantityUnitEnum.UNKNOWN, unitObj, changes);
            if (!notFoundAttr.isEmpty()) {
              returnString = 'Unit updated for available fields.' + '[Warning:' + notFoundAttr + "]"
            } else {
              returnString = 'Unit updated with given values'
            };
            HibernateApi.getInstance().save(unitObj);
            HibernateApi.getInstance().flush();
          } else {
            returnString = 'Unit not updated - please check the input format'
          }
        }
        if (oogFront != null || oogBack != null || oogLeft != null || oogRight != null || oogTop != null) {
          oogFront = unitObj.getUnitOogFrontCm() != null ? unitObj.getUnitOogFrontCm() : oogFront;
          oogBack = unitObj.getUnitOogBackCm() != null ? unitObj.getUnitOogBackCm() : oogBack;
          oogLeft = unitObj.getUnitOogLeftCm() != null ? unitObj.getUnitOogLeftCm() : oogLeft;
          oogRight = unitObj.getUnitOogRightCm() != null ? unitObj.getUnitOogRightCm() : oogRight;
          oogTop = unitObj.getUnitOogTopCm() != null ? unitObj.getUnitOogTopCm() : oogTop;
          unitObj.updateOog(oogBack, oogFront, oogLeft, oogRight, oogTop)
        }
        if (seal1 != null || seal2 != null || seal3 != null || seal4 != null) {
          seal1 = unitObj.getUnitSealNbr1() != null ? unitObj.getUnitSealNbr1() : seal1;
          seal2 = unitObj.getUnitSealNbr2() != null ? unitObj.getUnitSealNbr2() : seal2;
          seal3 = unitObj.getUnitSealNbr3() != null ? unitObj.getUnitSealNbr3() : seal3;
          seal4 = unitObj.getUnitSealNbr4() != null ? unitObj.getUnitSealNbr4() : seal4;
          unitObj.updateSeals(seal1, seal2, seal3, seal4)
        }
      } else {
        returnString = 'Unit not updated - Unit is null'
      }
    } catch (Exception ex) {
      returnString = 'Unit not updated : ' + ex;
    }
    builder {
      actual_result returnString;
    }
    LOGGER.debug('UpdateUnitAttribute:' + returnString)
    return builder;
  }

  protected UnitManager getUnitMngr() {
    return (UnitManager) Roastery.getBean(UnitManager.BEAN_ID);
  }

  protected UnitReroutePoster getRerouteMngr() {
    return (UnitReroutePoster) Roastery.getBean(UnitReroutePoster.BEAN_ID); ;
  }

  /** Updates the vessel visit fields in DB for the given vessel.
   * Input is passed in a comma separated format, where each token is a
   * key-value pair.The tokens are iterated to update each field in DB
   * [ATA=<date>,ATD=<date>,PHASE=<Phase>,STARTWORK=<date>,ENDWORK=<date>,ETA=<data>,ETD=<date>]
   * <date> --> current(for current date time),current+3D (to increment 3 days), current +3H (in increment 3 hours)
   * <Phase> -->  CREATED,INBOUND,ARRIVED,WORKING,COMPLETE,DEPARTED,CLOSED,CANCELED,ARCHIVED
   *
   * @param inParameters map contains this method name to call along with
   *                     other parameters.<br>command=UpdateVesselVisit<br>
   *                     vesselId=vessel Name<br>attributes=set of attributes
   *                     which requires updation in DB
   * @return <code>VesselVisitDetails updated with given values</code> - if
   *          the details are updated in DB successfully; <br>
   *          <code>VesselVisitDetails not updated,please check the input
   *          format</code> - if the input format is wrong; <br>
   *          <code>VesselVisitDetails updated for available fields</code> - if
   *          any one or more of the attributes supplied is not available in
   *          the pre-determined attribute list <br>
   *          <code>VesselVisitDetails not updated</code> - if vessel visit itself
   *          is null or any other internal exception occurs.
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy"
   * class-name="TestCommand"&gt; <br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="UpdateVesselVisit"/&gt;<br>
   * &lt;parameter id="vesselId" value="VESS01"/&gt;<br>
   * &lt;parameter id="attributes" value="ATA=current,ATD=current+3D"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;
   */
  public String UpdateVesselVisit(Map inParameters) {
    assert inParameters.size() == 3, '''Must supply 3 parameters:
                                        <parameter id="command" value="UpdateVesselVisit" />
					                    <parameter id="vesselId" value="<name of the vessel visit>" />
					                    <parameter id="attributes" value="STARTWORK=<date>,ENDWORK=<date>,PHASE,ATA=<date>,ATD=<date>" />'''
    String inVesselId = _testCommandHelper.checkParameter('vesselId', inParameters);
    String inAttributes = _testCommandHelper.checkParameter('attributes', inParameters);
    def attributeList = inAttributes.split(',');
    String key, value, notFoundAttr = "";
    def attributeArray = "";
    ServicesManager srvcMgr = (ServicesManager) Roastery.getBean(ServicesManager.BEAN_ID);
    //vesselName,Phase, ATA=Current,ATD=Current+3D,StartWork=Current+1H,EndWork=Current+1D,Phase=Working
    try {
      CarrierVisit carrierVisit = (CarrierVisit) HibernateApi.getInstance().getUniqueEntityByDomainQuery(
              QueryUtils.createDomainQuery(ArgoEntity.CARRIER_VISIT)
                      .addDqPredicate(PredicateFactory.eq(ArgoField.CV_ID, inVesselId))
                      .addDqPredicate(PredicateFactory.eq(ArgoField.CV_CARRIER_MODE, LocTypeEnum.VESSEL)));
      if (carrierVisit != null) {
        VesselVisitDetails vesselVisitDetails = resolveVvdFromCv(carrierVisit);
        EventType eventType = EventType.findEventType(EventEnum.UPDATE_VV.getKey());
        FieldChanges changes = new FieldChanges();
        FieldChanges vvdChanges = new FieldChanges();
        if (carrierVisit != null) {
          attributeList.each {
            attributeArray = it.trim().split("=")
            if (attributeArray.size() == 2) {
              key = attributeArray[0].trim();
              value = attributeArray[1].trim();
              switch (key) {
                case 'ATA':
                  carrierVisit."$_testCommandHelper.ATA" = _testCommandHelper.calculateTime(value);
                  changes.setFieldChange(ArgoField.CV_A_T_A, _testCommandHelper.calculateTime(value));
                  break;
                case 'ATD':
                  carrierVisit."$_testCommandHelper.ATD" = _testCommandHelper.calculateTime(value);
                  changes.setFieldChange(ArgoField.CV_A_T_D, _testCommandHelper.calculateTime(value));
                  break;
                case 'ETA':
                  carrierVisit.cvCvd.cvdETA = _testCommandHelper.calculateTime(value)
                  break;
                case 'ETD':
                  carrierVisit.cvCvd.cvdETD = _testCommandHelper.calculateTime(value)
                  break;
                case 'STARTWORK':
                  if (vesselVisitDetails != null) {
                    vesselVisitDetails."$_testCommandHelper.STARTWORK" = _testCommandHelper.calculateTime(value)
                  };
                  vvdChanges.setFieldChange(VesselField.VVD_TIME_START_WORK, _testCommandHelper.calculateTime(value));
                  break;
                case 'ENDWORK':
                  if (vesselVisitDetails != null) {
                    vesselVisitDetails."$_testCommandHelper.ENDWORK" = _testCommandHelper.calculateTime(value)
                  };
                  vvdChanges.setFieldChange(VesselField.VVD_TIME_END_WORK, _testCommandHelper.calculateTime(value));
                  break;
                case 'PHASE':
                  carrierVisit."$_testCommandHelper.PHASE" = CarrierVisitPhaseEnum."$value";
                  eventType = EventType.findEventType(EventEnum.PHASE_VV.getKey());
                  vvdChanges.setFieldChange(VesselVisitField.VVD_VISIT_PHASE, CarrierVisitPhaseEnum."$value");
                  srvcMgr.recordEvent(eventType, null, 0, ServiceQuantityUnitEnum.UNKNOWN, vesselVisitDetails, vvdChanges);
                  break;
                default:
                  notFoundAttr = 'Attribute : ' + key + ' not found in the list';
              }
              carrierVisit.applyFieldChanges(changes);
              vesselVisitDetails.applyFieldChanges(vvdChanges);
              if (!notFoundAttr.isEmpty()) {
                returnString = 'VesselVisitDetails updated for available fields' + ' [ Warning:' + notFoundAttr + " ]"
              } else {
                returnString = 'VesselVisitDetails updated with given values'
              }
            } else {
              returnString = 'VesselVisitDetails not updated - please check the input format'
            }
          }
        }
      } else {
        returnString = 'VesselVisitDetails not updated - Vessel Visit is null'
      }
    } catch (Exception ex) {
      returnString = 'VesselVisitDetails not updated : ' + ex;
    }
    LOGGER.debug('UpdateVesselVisit:' + returnString + 'for the visit :' + inVesselId)
    builder {
      actual_result returnString;
    }
    return builder;
  }

  /** Updates the train visit fields in DB for the given vessel.
   * Input is passed in a comma separated format, where each token is a
   * key-value pair.The tokens are iterated to update each field in DB
   * [ATA=<date>,ATD=<date>,PHASE=<Phase>,STARTWORK=<date>,ENDWORK=<date>,ETA=<data>,ETD=<date>]
   * <date> --> current(for current date time),current+3D (to increment 3 days), current +3H (in increment 3 hours)
   * <Phase> -->  CREATED,INBOUND,ARRIVED,WORKING,COMPLETE,DEPARTED,CLOSED,CANCELED,ARCHIVED
   *
   * @param inParameters map contains this method name to call along with
   *                     other parameters.<br>command=UpdateVesselVisit<br>
   *                     vesselId=vessel Name<br>attributes=set of attributes
   *                     which requires updation in DB
   * @return <code>TrainVisitDetails updated with given values</code> - if
   *          the details are updated in DB successfully; <br>
   *          <code>TrainVisitDetails not updated,please check the input
   *          format</code> - if the input format is wrong; <br>
   *          <code>TrainVisitDetails updated for available fields</code> - if
   *          any one or more of the attributes supplied is not available in
   *          the pre-determined attribute list <br>
   *          <code>TrainVisitDetails not updated</code> - if vessel visit itself
   *          is null or any other internal exception occurs.
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy"
   * class-name="TestCommand"&gt; <br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="UpdateTrainVisit"/&gt;<br>
   * &lt;parameter id="vesselId" value="VESS01"/&gt;<br>
   * &lt;parameter id="attributes" value="ATA=current,ATD=current+3D"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;
   */
  public String UpdateTrainVisit(Map inParameters) {
    assert inParameters.size() == 3, '''Must supply 3 parameters:
                                        <parameter id="command" value="UpdateTrainVisit" />
					                    <parameter id="trainCvId" value="<name of the vessel visit>" />
					                    <parameter id="attributes" value="STARTWORK=<date>,ENDWORK=<date>,PHASE,ATA=<date>,ATD=<date>" />'''
    String inVesselId = _testCommandHelper.checkParameter('trainCvId', inParameters);
    String inAttributes = _testCommandHelper.checkParameter('attributes', inParameters);
    def attributeList = inAttributes.split(',');
    String key, value, notFoundAttr = "";
    def attributeArray = "";
    ServicesManager srvcMgr = (ServicesManager) Roastery.getBean(ServicesManager.BEAN_ID);
    //vesselName,Phase, ATA=Current,ATD=Current+3D,StartWork=Current+1H,EndWork=Current+1D,Phase=Working
    try {
      CarrierVisit carrierVisit = (CarrierVisit) HibernateApi.getInstance().getUniqueEntityByDomainQuery(
              QueryUtils.createDomainQuery(ArgoEntity.CARRIER_VISIT)
                      .addDqPredicate(PredicateFactory.eq(ArgoField.CV_ID, inVesselId))
                      .addDqPredicate(PredicateFactory.eq(ArgoField.CV_CARRIER_MODE, LocTypeEnum.TRAIN)));
      if (carrierVisit != null) {
        TrainVisitDetails trainVisitDetails = TrainVisitDetails.resolveTvdFromCv(carrierVisit);
        EventType eventType = EventType.findEventType(EventEnum.UPDATE_RV.getKey());
        FieldChanges changes = new FieldChanges();
        FieldChanges tvdChanges = new FieldChanges();
        if (carrierVisit != null) {
          attributeList.each {
            attributeArray = it.trim().split("=")
            if (attributeArray.size() == 2) {
              key = attributeArray[0].trim();
              value = attributeArray[1].trim();
              switch (key) {
                case 'ATA':
                  carrierVisit."$_testCommandHelper.ATA" = _testCommandHelper.calculateTime(value);
                  changes.setFieldChange(ArgoField.CV_A_T_A, _testCommandHelper.calculateTime(value));
                  break;
                case 'ATD':
                  carrierVisit."$_testCommandHelper.ATD" = _testCommandHelper.calculateTime(value);
                  changes.setFieldChange(ArgoField.CV_A_T_D, _testCommandHelper.calculateTime(value));
                  break;
                case 'ETA':
                  carrierVisit.cvCvd.cvdETA = _testCommandHelper.calculateTime(value)
                  break;
                case 'ETD':
                  carrierVisit.cvCvd.cvdETD = _testCommandHelper.calculateTime(value)
                  break;
                case 'STARTWORK':
                  if (trainVisitDetails != null) {
                    trainVisitDetails.rvdtlsTimeStartWork = _testCommandHelper.calculateTime(value)
                  };
                  tvdChanges.setFieldChange(RailField.RVDTLS_TIME_START_WORK, _testCommandHelper.calculateTime(value));
                  break;
                case 'ENDWORK':
                  if (trainVisitDetails != null) {
                    trainVisitDetails.rvdtlsTimeEndWork = _testCommandHelper.calculateTime(value)
                  };
                  tvdChanges.setFieldChange(RailField.RVDTLS_TIME_END_WORK, _testCommandHelper.calculateTime(value));
                  break;
                case 'PHASE':
                  carrierVisit."$_testCommandHelper.PHASE" = CarrierVisitPhaseEnum."$value";
                  eventType = EventType.findEventType(EventEnum.PHASE_VV.getKey());
                  //tvdChanges.setFieldChange(ArgoField.CV_VISIT_PHASE, CarrierVisitPhaseEnum."$value");
                 // srvcMgr.recordEvent(eventType, null, 0, ServiceQuantityUnitEnum.UNKNOWN, trainVisitDetails, tvdChanges);
                  break;
                default:
                  notFoundAttr = 'Attribute : ' + key + ' not found in the list';
              }
              carrierVisit.applyFieldChanges(changes);
              trainVisitDetails.applyFieldChanges(tvdChanges);
              if (!notFoundAttr.isEmpty()) {
                returnString = 'TrainVisitDetails updated for available fields' + ' [ Warning:' + notFoundAttr + " ]"
              } else {
                returnString = 'TrainVisitDetails updated with given values'
              }
            } else {
              returnString = 'TrainVisitDetails not updated - please check the input format'
            }
          }
        }
      } else {
        returnString = 'TrainVisitDetails not updated - Vessel Visit is null'
      }
    } catch (Exception ex) {
      returnString = 'TrainVisitDetails not updated : ' + ex;
    }
    LOGGER.debug('UpdateTrainVisit:' + returnString + 'for the visit :' + inVesselId)
    builder {
      actual_result returnString;
    }
    return builder;
  }


  /**
   * Gets veselvisitdetails from the given carriervisit
   * @param inCv
   * @return
   */
  public static VesselVisitDetails resolveVvdFromCv(CarrierVisit inCv) {
    VesselVisitDetails vvd = null;
    com.navis.argo.business.model.VisitDetails cvd = inCv.getCvCvd();
    if (cvd != null && LocTypeEnum.VESSEL.equals(inCv.getCvCarrierMode())) {
      vvd = (VesselVisitDetails) HibernateApi.getInstance().downcast(cvd, VesselVisitDetails.class);
    }
    return vvd;
  }

  public Serviceable getServiceable(final Unit inUnit, final boolean isOnEquipment) {
    final EquipmentState[] eqs = new EquipmentState[1];
        UnitEquipment ue = inUnit.getUnitPrimaryUe();
        if (ue != null) {
          eqs[0] = ue.getUeEquipmentState();
        }
    return isOnEquipment ? eqs[0] : inUnit;
  }
}
