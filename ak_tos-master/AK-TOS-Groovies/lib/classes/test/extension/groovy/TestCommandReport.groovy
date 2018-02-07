package test.extension.groovy

import com.navis.argo.*
import com.navis.argo.business.api.ServicesManager
import com.navis.argo.business.atoms.LocTypeEnum
import com.navis.argo.business.model.CarrierVisit
import com.navis.argo.business.model.JobStepProjection
import com.navis.argo.business.model.Yard
import com.navis.argo.business.reference.EquipType
import com.navis.argo.business.reference.Equipment
import com.navis.argo.business.xps.model.*
import com.navis.control.business.agv.AgvQaTestUtils
import com.navis.framework.business.Roastery
import com.navis.framework.persistence.DatabaseHelper
import com.navis.framework.persistence.HibernateApi
import com.navis.framework.portal.QueryUtils
import com.navis.framework.portal.query.DomainQuery
import com.navis.framework.portal.query.PredicateFactory
import com.navis.framework.util.BizFailure
import com.navis.inventory.InventoryEntity
import com.navis.inventory.InventoryField
import com.navis.inventory.MovesEntity
import com.navis.inventory.MovesField
import com.navis.inventory.business.api.SearchResults
import com.navis.inventory.business.api.UnitFinder
import com.navis.inventory.business.moves.WorkInstruction
import com.navis.inventory.business.units.EquipmentState
import com.navis.inventory.business.units.Unit
import com.navis.inventory.business.units.UnitEquipment
import com.navis.inventory.business.units.UnitFacilityVisit
import com.navis.services.business.event.Event
import com.navis.spatial.business.model.AbstractBin
import com.navis.spatial.business.model.BinContext
import com.navis.yard.business.YardTestUtils
import com.navis.yard.business.model.StackBlock
import com.navis.yard.business.model.YardBinModel
import com.navis.yard.business.model.YardStack
import org.apache.commons.lang.StringEscapeUtils
import org.apache.log4j.Logger
import org.hibernate.SQLQuery
import org.quartz.Trigger

import java.text.SimpleDateFormat

/**
 * Created with IntelliJ IDEA.
 * User: rajansh
 * Date: 29/10/13
 * Time: 2:58 PM
 * To change this template use File | Settings | File Templates.
 */
class TestCommandReport {

  private HibernateApi _hibernateApi;
  /** holds the result returned by each method */
  def String returnString = null;
  /** json builder, frames the output in json format */
  def builder = new groovy.json.JsonBuilder();
  private DatabaseHelper _teamsDbHelper;
  /** handler to invoke the fields in helper class */
  test.extension.groovy.TestCommandHelper _testCommandHelper = new test.extension.groovy.TestCommandHelper();

  //Logger for TestCommand
  public Logger LOGGER = Logger.getLogger(TestCommandReport.class);

  /**
   * Reports the direction of the given block.
   *
   * @param inParameters The map containing the method name to call along with the parameters<br><br>
   * command=ReportBlock<br>
   * blockName=Block details to be reported for the given block name<br>
   * blockMetaFields=list of block fields required to be reported [DIRECTION]
   * @return JSON , key-value pair of the yard block report
   * @Example
   * Table invoked in SPARCS : spatial_bins<br>
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="ReportBlock"/&gt;<br>
   * &lt;parameter id="blockName" value="ASC01"/&gt;<br>
   * &lt;parameter id="blockMetaFields" value="DIRECTION"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;
   */
  public String ReportBlock(Map inParameters) {
    assert inParameters.size() == 3, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 3 parameters:
                                        <parameter id="command" value="ReportBlock" />
                                        <parameter id="blockName" value="<Block Name>" />
                                        <parameter id="blockMetaFields" value="<list of block fields from AbstractBinHbr>" />'''

    String inBlockName = _testCommandHelper.checkParameter('blockName', inParameters);
    String inBlockFields = _testCommandHelper.checkParameter('blockMetaFields', inParameters);

    try {
      Yard yard = ContextHelper.getThreadYard();
      AbstractBin yardBinModel = yard.getYrdBinModel();
      BinContext stowageContext = BinContext.findBinContext(Yard.CONTAINER_STOWAGE_BIN_CONTEXT);
      AbstractBin bin = yardBinModel.findDescendantBinFromInternalSlotString(inBlockName, stowageContext);
      if (bin != null) {
        StackBlock stackBlock = ((StackBlock) HibernateApi.getInstance().get(StackBlock.class, bin.getAbnGkey()));
        //Iterating through all the input params passed
        def fields = inBlockFields.split(',');  // splitting the value to find each field
        String retVal = "";
        def bins, moveKindAllowed, name = '', result = ''
        List<StackStatus> statusList
        builder {
          actual_result fields.collect {
            switch (it) {
              case 'DIRECTION':
                if (stackBlock != null) {
                  retVal = stackBlock.getStkblkDirection()
                }
                ["$it": _testCommandHelper.getActualEnumValue(retVal)]
                break;
            }
          }
        }
      } else {
        builder {
          actual_result "Block is null";
        }
      }
    } catch (Exception ex) {
      builder {
        actual_result 'Failed to report block ' + ex
      }
    }
    return builder;
  }

  /**
   * Reports the transfer point usage for the given transfer points
   *
   * @param inParameters The map containing the method name to call along with the parameters<br><br>
   * command=ReportTransferPointUsage<br>
   * tzName=Block details to be reported for the given block name<br>
   * tzMetaFields=list of transfer point fields required to be reported [TP_USAGE]
   * startRow=Row position in Internal format of the block
   * endRow=Row position in Internal format of the block
   * startCol=Col position in Internal format of the block
   * endCol=Col position in Internal format of the block
   * @return JSON , key-value pair of the yard block report
   * @Example
   * Table invoked in SPARCS : spatial_bins<br>
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="ReportTransferPointUsage"/&gt;<br>
   * &lt;parameter id="tzName" value="ASC01"/&gt;<br>
   * &lt;parameter id="tzMetaFields" value="TP_USAGE"/&gt;<br>
   * &lt;parameter id="startRow" value="A" /&gt;<br>
   * &lt;parameter id="endRow" value="F" /&gt;<br>
   * &lt;parameter id="startCol" value="01" /&gt;<br>
   * &lt;parameter id="endCol" value="05" /&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;
   */
  public String ReportTransferPointUsage(Map inParameters) {
    assert inParameters.size() == 7, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 7 parameters:
                                          <parameter id="command" value="ReportTransferPointUsage" />
                                          <parameter id="tzName" value="<Block Name>" />
                                          <parameter id="tzMetaFields" value="TP_USAGE" />
                                          <parameter id="startRow" value="<Row position like B2R3>" />
                                          <parameter id="endRow" value="<Row position in Internal format of the block>" />
                                          <parameter id="startCol" value="<Col position in Internal format of the block>" />
                                          <parameter id="endCol" value="<Col position in Internal format of the block>" />'''

    String inBlockName = _testCommandHelper.checkParameter('tzName', inParameters);
    String inTZMetaFields = _testCommandHelper.checkParameter('tzMetaFields', inParameters);
    String inStartRow = _testCommandHelper.checkParameter('startRow', inParameters);
    String inEndRow = _testCommandHelper.checkParameter('endRow', inParameters);
    String inStartCol = _testCommandHelper.checkParameter('startCol', inParameters);
    String inEndCol = _testCommandHelper.checkParameter('endCol', inParameters);

    try {
      Yard yard = ContextHelper.getThreadYard();
      AbstractBin yardBinModel = yard.getYrdBinModel();
      BinContext stowageContext = BinContext.findBinContext(Yard.CONTAINER_STOWAGE_BIN_CONTEXT);
      AbstractBin bin = yardBinModel.findDescendantBinFromInternalSlotString(inBlockName, stowageContext);
      if (bin != null) {
        StackBlock stackBlock = ((StackBlock) HibernateApi.getInstance().get(StackBlock.class, bin.getAbnGkey()));
        //Iterating through all the input params passed
        def fields = inTZMetaFields.split(',');  // splitting the value to find each field
        String retVal = "";
        def bins, moveKindAllowed, name = '', result = ''
        List<StackStatus> statusList
        builder {
          actual_result fields.collect {
            switch (it) {
              case 'TP_USAGE':
                if (stackBlock != null) {
                  statusList = findOrCreateStackStatus(stackBlock, inStartRow, inEndRow,
                          inStartCol, inEndCol)
                };
                statusList.each {
                  name = it.getStackstatusBinName();
                  //commenting this method call as it is deprecated
                  //moveKindAllowed = YardTestUtils.findOrCreateMoveKindAllowed(it.getStackstatusBin())
                  moveKindAllowed = MoveKindAllowed.findOrCreateMoveKindAllowed(it.getStackstatusBin(), ContextHelper.getThreadYard())
                  retVal = moveKindAllowed.getMovekindallowedMoveKinds()
                  retVal = _testCommandHelper.getMoveKindAllowedReport(retVal)
                  if (!retVal.isEmpty()) {
                    result += name + ":" + _testCommandHelper.truncateEndingComma(retVal) + ","
                  } else {
                    result += name + ": TP usage not set"
                  }
                }
                [_testCommandHelper.truncateEndingComma(result)]
                break;
            }
          }
        }
        LOGGER.debug('ReportTransferPointUsage:' + result)
      } else {
        builder {
          actual_result "Transfer Zone is null";
        }
        LOGGER.error('ReportTransferPointUsage: Transfer zone is null')
      }
    } catch (Exception ex) {
      builder {
        actual_result 'Failed to report transfer point usage ' + ex
      }
      LOGGER.error('Failed to report transfer point usage ' + ex)
    }
    return builder;
  }

  /**
   * Method to get the details for the given Ec Event.<br>
   * It accepts CHE as primary input and finds the ec event associated with the CHE and reports its details.
   * If the CHE has more than one ec events , all the ec events for that CHE will be reported.
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=ReportEcEvent<br>
   * cheId=che from which Ec event will be found<br>
   * ecEventMetaFields=EVENT_TYPE,EVENT_NAME
   * @return Key-value pair of the ec event details<br>
   * @Example
   * Table invoked in SPARCS: xps_ecevent<br>
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="ReportEcEvent"/&gt;<br>
   * &lt;parameter id="cheId" value="* &lt;CHE01" /&gt;<br>
   * &lt;parameter id="ecEventMetaFields" value="EVENT_TYPE,EVENT_NAME"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String ReportEcEvent(Map inParameters) {
    assert inParameters.size() == 3, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 3 parameters:
                                        <parameter id="command" value="ReportEcEvent" />
                                        <parameter id="cheId" value="<che Id>" />
                                        <parameter id="ecEventMetaFields" value="<EVENT_TYPE,EVENT_NAME>" />'''

    String inCheName = _testCommandHelper.checkParameter('cheId', inParameters);
    String inEcEventMetaFields = _testCommandHelper.checkParameter('ecEventMetaFields', inParameters);
    String retVal, type = "";

    try {
      EcEvent ecEvent = null;
      //find ec event
      DomainQuery domainQuery = QueryUtils.createDomainQuery(ArgoEntity.EC_EVENT).addDqPredicate(PredicateFactory.disjunction().
              add(PredicateFactory.eq(ArgoField.ECEVENT_CHE_NAME, inCheName)));
      List<EcEvent> ecEventList = HibernateApi.getInstance().findEntitiesByDomainQuery(domainQuery); //get all ec events for the che
      if (!ecEventList.isEmpty()) {
        Iterator ecEventIterator = ecEventList.iterator();
        while (ecEventIterator.hasNext()) {
          ecEvent = ecEventIterator.next()
          if (ecEvent != null) {
            if (_testCommandHelper.ecEventMap.containsKey(ecEvent.getEceventTypeDescription())) {
              type += _testCommandHelper.ecEventMap.get(ecEvent.getEceventTypeDescription()) + ',';
              retVal = ecEvent.getEceventOperatorName();
            } else {
              type += ecEvent.getEceventTypeDescription() + ',';
              retVal = ecEvent.getEceventOperatorName();
            }
          } else {
            type = 'Ec Event not found';
            retVal = 'Ec Event not found';
          }
        }
      } else {
        type = 'Ec Event not found';
        retVal = 'Ec Event not found';
      }
      builder {
        def fields = inEcEventMetaFields.split(',');  // splitting the value to find each field
        actual_result fields.collect {
          switch (it) {
            case 'EVENT_TYPE':
              ['EVENT_TYPE': _testCommandHelper.truncateEndingComma(type)]
              break;
            case 'EVENT_NAME':
              ['EVENT_NAME': retVal]
              break;
          }
        }
      }
    } catch (Exception ex) {
      builder {
        actual_result 'Failed to report ec event ' + ex
      }
    }
    return builder;
  }

  /**
   * Reports the stack status for the given block.<br>
   * It internally converts the obtained stack status format to readable format. <br>
   * M - MENWORKING  <br>
   * R - RACK / ROADWAY according to the char position<br>
   * If charposition 1 is R , then it is ROAD WAY , if the position is 7 then it is RACK  <br>
   * T - TEMPBLOCKED <br>
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=ReportStackStatus<br>
   * blockName=Name of the block whose details needs to be reported<br>
   * startRow=Row position in Internal format of the block <br>
   * endRow=Row position in Internal format of the block <br>
   * startCol=Col position in Internal format of the block <br>
   * endCol=Col position in Internal format of the block<br>
   * @return JSON , which reports the stack status details in a key-value pair format if success, <br>
   *                <code>Status not set</code> If no status is set for these stacks<br>
   *                <code>Could not find valid stack block with the given inputs, please check the internal format of the block</code><br>
   *                <code>Failed to report stack status</code> Exception occured internally while processing the request
   * @Example
   * Tabled invoked in SPARCS : xps_stackstatus<br>
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="ReportStackStatus" /&gt;<br>
   * &lt;parameter id="blockName" value="BL01" /&gt;<br>
   * &lt;parameter id="startRow" value="A" /&gt;<br>
   * &lt;parameter id="endRow" value="F" /&gt;<br>
   * &lt;parameter id="startCol" value="01" /&gt;<br>
   * &lt;parameter id="endCol" value="05" /&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   *
   */
  public String ReportStackStatus(Map inParameters) {
    assert inParameters.size() == 6, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 6 parameters:
                                        <parameter id="command" value="ReportStackStatus" />
                                        <parameter id="blockName" value="<Block Name>" />
                                        <parameter id="startRow" value="<Row position in Internal format of the block>" />
                                        <parameter id="endRow" value="<Row position in Internal format of the block>" />
                                        <parameter id="startCol" value="<Col position in Internal format of the block>" />
                                        <parameter id="endCol" value="<Col position in Internal format of the block>" />'''

    String inBlockName = _testCommandHelper.checkParameter('blockName', inParameters);
    String inStartRow = _testCommandHelper.checkParameter('startRow', inParameters);
    String inEndRow = _testCommandHelper.checkParameter('endRow', inParameters);
    String inStartCol = _testCommandHelper.checkParameter('startCol', inParameters);
    String inEndCol = _testCommandHelper.checkParameter('endCol', inParameters);

    try {
      //Find yard model
      Yard thisYard = ContextHelper.getThreadYard();
      YardBinModel yardModel =
        (YardBinModel) HibernateApi.getInstance().downcast(thisYard.getYrdBinModel(), YardBinModel.class);
      StackBlock stackBlock = StackBlock.findStackBlock(yardModel, inBlockName);
      assert stackBlock != null, returnString = 'Could not find stack block ' + inBlockName;
      def result = '', statusString = '';
      List<StackStatus> statusList = findOrCreateStackStatus(stackBlock, inStartRow, inEndRow,
              inStartCol, inEndCol);
      if (statusList != null) {
        statusList.each {
          def statusVal = it.getStackstatusStatusChars();
          if (statusVal != null) {
            if (!statusVal.isEmpty()) {
              statusString = _testCommandHelper.getStackStatusReport(statusVal)
              result += it.getStackstatusBinName() + ":" + _testCommandHelper.truncateOnlyEndingComma(statusString) + ",";
            } else {
              result = 'Status not set'
            }
          } else {
            result += it.getStackstatusBinName() + ": Status not set , "
          };
        }
        returnString = _testCommandHelper.truncateEndingComma(result);
      } else {
        returnString = 'Could not find valid stack block with the given inputs, please check the internal format of the block'
      };
    } catch (Exception ex) {
      returnString = 'Failed to report stack status ' + ex;
    }
    LOGGER.debug('ReportStackStatus:' + returnString)
    builder {
      actual_result returnString
    }
    return builder;
  }

  /**
   * Method to get the ROLE,CATEGORY,STATE,DAMAGE_FLAG,EQTYPE_DESC,REEFER_TYPE,LAST_POSITION,ISO_TYPE,HOLD for the given unit.
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=ReportUnit<br>
   * unitId=unit Id for which report is required<br>
   * unitMetaFields=list of unit fields<br>
   * @return Key-value pair of the Unit details<br>
   * @Example
   * Table invoked in SPARCS : inv_unit <br>
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="ReportUnit" /&gt;<br>
   * &lt;parameter id="unitId" value="ASCU0000001" /&gt;<br>
   * &lt;parameter id="unitMetaFields" value="ROLE,CATEGORY,STATE,DAMAGE_FLAG,EQTYPE_DESC,REEFER_TYPE,LAST_POSITION,ISO_TYPE,HOLD,WEIGHT_KG,INBOUND_CARRIER,CURRENT_POSITION" /&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String ReportUnit(Map inParameters) {
    assert inParameters.size() == 3, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 3 parameters:
                                        <parameter id="command" value="ReportUnit" />
                                        <parameter id="unitId" value="<unit Id>" />
                                        <parameter id="unitMetaFields" value="<list of unit fields>" />'''

    String inUnitId = _testCommandHelper.checkParameter('unitId', inParameters);
    String inUnitMetaFields = _testCommandHelper.checkParameter('unitMetaFields', inParameters);

    try {
      //find unit
      Unit unitObj = ((Unit) HibernateApi.getInstance().getUniqueEntityByDomainQuery(QueryUtils.createDomainQuery(InventoryEntity.UNIT)
              .addDqPredicate(PredicateFactory.eq(InventoryField.UNIT_ID, inUnitId))));
      if (unitObj != null) {
        UnitFinder finder = (UnitFinder) Roastery.getBean(UnitFinder.BEAN_ID);
        SearchResults results = finder.findUfvByDigits(inUnitId, false, false);
        assert results.getFoundCount() == 1, 'More than one equipment found for id ' + inUnitId;
        UnitFacilityVisit ufv = (UnitFacilityVisit) HibernateApi.getInstance().load(UnitFacilityVisit.class, results.getFoundPrimaryKey());
        UnitEquipment unitEquipment = ((UnitEquipment) HibernateApi.getInstance().getUniqueEntityByDomainQuery(QueryUtils.createDomainQuery(InventoryEntity.UNIT_EQUIPMENT)
                .addDqPredicate(PredicateFactory.eq(InventoryField.UE_GKEY, unitObj.unitGkey))));
        Equipment equipment = ((Equipment) HibernateApi.getInstance().getUniqueEntityByDomainQuery(QueryUtils.createDomainQuery(ArgoRefEntity.EQUIPMENT)
                .addDqPredicate(PredicateFactory.eq(ArgoRefField.EQ_ID_FULL, inUnitId))));
        EquipType equipType = ((EquipType) HibernateApi.getInstance().getUniqueEntityByDomainQuery(QueryUtils.createDomainQuery(ArgoRefEntity.EQUIP_TYPE)
                .addDqPredicate(PredicateFactory.eq(ArgoRefField.EQTYP_GKEY, equipment.getEqEquipType().getEqtypGkey()))));
        EquipmentState equipmentState = ((EquipmentState) HibernateApi.getInstance().getUniqueEntityByDomainQuery(QueryUtils.createDomainQuery(InventoryEntity.EQUIPMENT_STATE)
                .addDqPredicate(PredicateFactory.eq(InventoryField.EQS_EQUIPMENT, equipment.eqGkey))));

        String retVal = "";
        def fields = inUnitMetaFields.split(',');
        builder {
          //Iterating through all the input params passed
          actual_result fields.collect {
            switch (it) {
              case 'ROLE':
                if (unitEquipment != null) {
                  retVal = unitEquipment.getUeEqRole();
                  ["$it": _testCommandHelper.getActualEnumValue(retVal)]
                }
                break;
              case 'DAMAGE_FLAG':
                if (unitEquipment != null) {
                  retVal = unitEquipment.getUeDamageSeverity();
                  ["$it": _testCommandHelper.getActualEnumValue(retVal)]
                }
                break;
              case 'STATE':
                if (unitEquipment != null) {
                  retVal = unitEquipment.getUeEquipmentState();
                  ["$it": retVal]
                }
                break;
			  case 'ORIENTATION':
                if (ufv != null) {
                  retVal = ufv.getUfvDoorDirection()
                  ["$it": retVal]
                }
                break;
              case 'CATEGORY':
                if (unitObj != null) {
                  retVal = unitObj.getUnitCategory();
                  ["$it": _testCommandHelper.getActualEnumValue(retVal)]
                }
                break;
              case 'EQTYPE_DESC':
                if (equipType != null) {
                  retVal = equipType.getEqtypDescription();
                  ["$it": retVal]
                }
                break;
              case 'REEFER_TYPE':
                if (equipType != null) {
                  retVal = equipType.getEqtypRfrType();
                  ["$it": _testCommandHelper.getActualEnumValue(retVal)]
                }
                break;
              case 'ISO_TYPE':
                if (equipType != null) {
                  retVal = equipType.getEqtypId()
                };
                ["$it": retVal]
                break;
              case 'LAST_POSITION':
                if (equipmentState != null) {
                  retVal = equipmentState.getEqsLastPosName()
                  ["$it": retVal]
                }
                break;
              case 'WEIGHT_KG':
                if (equipment != null) {
                  retVal = equipment.getEqTareWeightKg()
                  ["$it": retVal]
                }
                break;
              case 'HEIGHT_MM':
                if (equipment != null) {
                  retVal = equipment.getEqHeightMm()
                  ["$it": retVal]
                }
                break;
              case 'INBOUND_CARRIER':
                if (unitObj != null) {
                  retVal = unitObj.getUnitDeclaredIbCv()
                  ["$it": retVal]
                }
                break;
              case 'CURRENT_POSITION':
                if (ufv != null) {
                  retVal = ufv.getUfvLastKnownPosition()
                  ["$it": retVal]
                }
                break;
              case 'default':
                ["$it": 'Not found']
                break;
            }
          }
        }
      } else {
        builder {
          actual_result 'Failed to report unit - Unit null'
        }
      }
    } catch (Exception ex) {
      builder {
        actual_result 'Failed to report unit ' + ex
      }
    }
    return builder;
  }

  /**
   * Reports the given ECI data for the provided meta fields<br>
   * Either report for a single field or multiple fields can be requested.<br>
   * For multiple field report,pass the metafields in a comma separated format.<br>
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=ReportECI<br>
   * cheId=ECI Id<br>
   * eciMetaFields=ASC_ORDERS (asc_orders), AGV_ORDERS (agv_orders) <br>
   *               QC_COMMANDS ( qc_commands), QC_ORDERS (qc_orders) <br>
   *               OPERATIONAL_STATUS,TECHNICAL_DETAILS (applicable only for ASC and AGV, asc_status, agv_status)<br>
   *               TECHNICAL_STATUS (applicable for ASC,AGV,QC. asc_status,agv_status,qc_status)<br>
   *               WORK_STATUS(applicable only for ASC . asc_status)<br>
   *               QC_STATUS (qc_status)<br>
   *               WORKINSTRUCTION_STATUS (containerId input param must be supplied. work_instruction_status)<br>
   *               QC_WORKQUEUE_STATUS (qc_work_queue_status)<br>
   *               BERTH_STATUS (berth_status)<br>
   *
   * @return JSON , key-value pair is reported for the required meta fields
   * @Example
   * Table invoked in TEAMS : almost all TEAMS tables are invoked depending on the input provided.<br>
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="ReportECI"/&gt;<br>
   * &lt;parameter id="cheId" value="ASC01"/&gt;<br>
   * &lt;parameter id="eciMetaFields" value="ASC_ORDERS,AGV_ORDERS,QC_COMMANDS,QC_ORDERS,OPERATIONAL_STATUS,TECHNICAL_STATUS,WORK_STATUS,TECHNICAL_DETAILS,
   * QC_STATUS,WORKINSTRUCTION_STATUS,QC_WORKQUEUE_STATUS,BERTH_STATUS"/&gt;<br>
   * &lt;parameter id="vesselVisitId" value="Name of the vessel visit"/&gt;<br> <code>optional - should be given to report BERTH_STATUS </code>
   * &lt;parameter id="workQueueName" value="Name of the Work Queue"/&gt;<br> <code>optional - should be given to report QC_WORKQUEUE_STATUS </code>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String ReportECI(Map inParameters) {
    assert inParameters.size() >= 3, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 3 parameters:
                                        <parameter id="command" value="ReportECI" />
                                        <parameter id="cheId" value="<che Id>" />
                                        <parameter id="eciMetaFields" value="<list of eci fields>" />
                                        <parameter id="containerId" value="<container id>" />
                                        <parameter id="vesselVisitId" value="vessel visit" />
                                        <parameter id="workQueueName" value="wqName" />'''

    def inEciId
    if (inParameters.containsKey('cheId')) {
      inEciId = inParameters.get('cheId', inParameters)
    };
    String inEciFields = _testCommandHelper.checkParameter('eciMetaFields', inParameters);
    String inContainerId = inParameters.get('containerId');
    String inVesselVisitId = inParameters.get('vesselVisitId');
    String inWorkQueueName = inParameters.get('workQueueName');

    //Iterating through all the input params passed
    def fields = inEciFields.split(',');  // splitting the value to find each field
    String retVal = "";
    def ascData, agvData, berthData, qcData, wiData
    def column_name, tableName
    Che che
    try {
      if (inEciId != null) {
        che = ((Che) HibernateApi.getInstance().getUniqueEntityByDomainQuery(QueryUtils.createDomainQuery(ArgoEntity.CHE)
                .addDqPredicate(PredicateFactory.eq(ArgoField.CHE_SHORT_NAME, inEciId))));
        if (che != null) {
          String cheType = che.cheKindEnum;
          column_name = ''
          tableName = new test.extension.groovy.TestCommandTEAMS().findCheTableName(_testCommandHelper.getActualEnumValue(cheType));
          if (tableName.equals('qc_status')) {
            column_name = 'qc_id'
          } else {
            column_name = 'che_id'
          }
        }
      }
      String query = "";
      builder {
        actual_result fields.collect {
          switch (it) {
            case 'ASC_ORDERS':
              query = "(select MAX(order_gkey) from asc_orders where che_id = '" + inEciId + "')"
              List qr = _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).queryForList(
                      String.format(
                              "select status,container_id,origin,orig_vehicle_type,orig_pos_truck," +
                                      "planned_destination,dest_vehicle_type," +
                                      "dest_pos_truck" +
                                      " from asc_orders where che_id = '" + inEciId + "' and order_gkey = " + query
                      )
              );
              String result = qr.toString()
              List status = _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).queryForList(
                      String.format(
                              "select status from asc_orders where che_id = '" + inEciId + "' and order_gkey = " + query
                      )
              );
              ascData = status.toString()
              if (qr.size() == 0) {
                result = 'No ASC orders found for the given ASC :' + inEciId
              }
              ["$it": result]
              break;
            case 'AGV_ORDERS':
              query = "(select MAX(order_gkey) from agv_orders where che_id = '" + inEciId + "')"
              List qr = _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).queryForList(
                      String.format(
                              "select status,planned_destination,agv_order_type,work_queue,container_id_1,dest_door_1," +
                                      "container_id_2,dest_door_2,bypass_sequence" +
                                      " from agv_orders where che_id = '" + inEciId + "' and order_gkey = " + query
                      )
              );
              String result = qr.toString()
              List status = _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).queryForList(
                      String.format(
                              "select status,agv_order_type from agv_orders where che_id = '" + inEciId + "' and order_gkey = " + query
                      )
              );
              agvData = status.toString()
              if (qr.size() == 0) {
                result = 'No AGV orders found for the given AGV :' + inEciId
              }
              ["$it": result]
              break;
            case 'QC_COMMANDS':
              query = "(select MAX(command_gkey) from qc_commands where qc_id = '" + inEciId + "')"
              List qr = _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).queryForList(
                      String.format(
                              "select reference_id,presumed_container_id,work_queue,status,container_last_location,container_curr_location," +
                                      "container_length,transaction_id" +
                                      " from qc_commands where qc_id = '" + inEciId + "' and command_gkey = " + query
                      )
              );
              String result = qr.toString();
              if (qr.size() == 0) {
                result = 'No QC commands found for the given QC :' + inEciId
              }
              ["$it": result]
              break;
            case 'QC_ORDERS':
              query = "(select MAX(command_gkey) from qc_commands where qc_id = '" + inEciId + "')"
              List qr = _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).queryForList(
                      String.format(
                              "select container_id,door_direction_at_qc,status,transfer_allowed" +
                                      " from qc_orders where command_gkey = " + query
                      )
              );
              String result = qr.toString();
              if (qr.size() == 0) {
                result = 'No QC orders found for the given QC :' + inEciId
              }
              ["$it": result]
              break;
            case 'OPERATIONAL_STATUS':
              String result = ''
              if (tableName.equalsIgnoreCase('agv_status') || tableName.equalsIgnoreCase('asc_status')) {
                List qr = _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).queryForList(
                        String.format(
                                "select operational_status from " + tableName + " where " + column_name + " = '" + inEciId + "'"
                        )
                );
                result = qr.toString()
                if (qr.size() == 0) {
                  result = 'No status record found for the given CHE :' + inEciId
                }
              }
              ["$it": result]
              break;
            case 'TECHNICAL_STATUS':
              List qr = _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).queryForList(
                      String.format(
                              "select technical_status from " + tableName + " where " + column_name + " = '" + inEciId + "'"
                      )
              );
              String result = qr.toString()
              if (qr.size() == 0) {
                result = 'No status record found for the given CHE :' + inEciId
              }
              ["$it": result]
              break;
            case 'WORK_STATUS':
              List qr
              if (tableName.equalsIgnoreCase('asc_status')) {
                qr = _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).queryForList(
                        String.format(
                                "select work_status from " + tableName + " where che_id = '" + inEciId + "'"
                        )
                );
              }
              String result = qr.toString()
              if (qr.size() == 0) {
                result = 'No status record found for the given CHE :' + inEciId
              }
              ["$it": result]
              break;
            case 'TECHNICAL_DETAILS':
              String result = '';
              if (tableName.equalsIgnoreCase('agv_status') || tableName.equalsIgnoreCase('asc_status')) {
                List qr = _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).queryForList(
                        String.format(
                                "select technical_details from " + tableName + " where che_id = '" + inEciId + "'"
                        )
                );
                result = qr.toString()
                if (qr.size() == 0) {
                  result = 'No status record found for the given CHE :' + inEciId
                }
              }
              ["$it": result]
              break;
            case 'QC_STATUS':
              List qr = _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).queryForList(
                      String.format(
                              "select technical_status from " + tableName + " where qc_id = '" + inEciId + "'"
                      )
              );
              String result = qr.toString()
              if (qr.size() == 0) {
                result = 'No status record found for the given CHE :' + inEciId
              }
              ["$it": result]
              break;
            case 'WORKINSTRUCTION_STATUS':
              List qr = _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).queryForList(
                      String.format(
                              "select container_id,is_tank,rack_suitable,hold,work_queue,point_of_work,move_kind,move_stage," +
                                      "container_iso" +
                                      " from work_instruction_status where CONTAINER_ID = '" + inContainerId + "'"
                      )
              );
              String result = qr.toString()
              List details = _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).queryForList(
                      String.format(
                              "select container_id,work_queue,point_of_work,move_kind,move_stage" +
                                      " from work_instruction_status where CONTAINER_ID = '" + inContainerId + "'"
                      )
              );
              wiData = details.toString()
              if (qr.size() == 0) {
                result = 'No work instruction found for the given container :' + inContainerId
              }
              ["$it": result]
              break;
            case 'QC_WORKQUEUE_STATUS':
              List qr = _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).queryForList(
                      String.format(
                              """select work_queue,move_kind,above_below,qc_id,vessel_visit,vessel_bay,qc_bollard,
                                   qc_bollard_offset_cm,wq_seq,configuration from qc_work_queue_status where qc_id = """ +
                                      "'" + inEciId + "'"
                      )
              );
              String result = qr.toString()
              List qcWQ = _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).queryForList(
                      String.format(
                              """select work_queue,move_kind,qc_id,configuration,wq_status,vessel_visit from qc_work_queue_status where qc_id = """ +
                                      "'" + inEciId + "' and work_queue = '" + inWorkQueueName + "'"
                      )
              );
              qcData = qcWQ.toString()
              if (qr.size() == 0) {
                result = 'No data found for the given work queue :' + inEciId
              }
              ["$it": result]
              break;
            case 'BERTH_STATUS':
              List qr = _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).queryForList(
                      String.format(
                              """select vessel_visit,vessel_call_sign,vessel_name,bow_bollard,
                                   bow_bollard_offset_cm,stern_bollard,stern_bollard_offset_cm from berth_status where vessel_visit = '""" + inVesselVisitId + "'"
                      )
              );
              String result = qr.toString()
              List berth = _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).queryForList(
                      String.format(
                              """select vessel_visit,vessel_classification,vessel_visit_phase,bow_bollard,
                                   bow_bollard_offset_cm,stern_bollard,stern_bollard_offset_cm from berth_status where vessel_visit = '""" + inVesselVisitId + "'"
                      )
              );
              berthData = berth.toString()
              if (qr.size() == 0) {
                result = 'No berthing details for the given vessel visit :' + inVesselVisitId
              }
              ["$it": result]
              break;
            default: ["ReportECI : Not a valid input, please give any of these[ASC_ORDERS,AGV_ORDERS,QC_COMMANDS,QC_ORDERS,OPERATIONAL_STATUS,TECHNICAL_STATUS,WORK_STATUS,TECHNICAL_DETAILS,QC_STATUS," +
                    "WORKINSTRUCTION_STATUS,QC_WORKQUEUE_STATUS,BERTH_STATUS]"]
          }
        }
        if (ascData != null || agvData != null || berthData != null || qcData != null || wiData != null) {
          data('ASC': ascData, 'AGV': agvData, 'BERTH': berthData, 'QC': qcData, 'WI': wiData)
        }
        returnString = 'Reported ECI details'
      }
    } catch (Exception ex) {
      builder {
        returnString = 'Failed to report ECI :' + ex;
      }
      LOGGER.debug('ReportECI:' + returnString)
    }
    return builder;
  }

  /**
   * Reports the CHE data for the given meta fields<br>
   * Report for single field or multiple fields can be requested.<br>
   * For multiple field report,pass the metafields in a comma separated format.<br>
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=ReportChe<br>
   * cheId=CHE Id<br>
   * cheMetaFields=list of CHE fields.JOB_STEP_STATE,OPERATING_MODE,AUTOCHE_TECHNICAL_STATUS
   * AUTOCHE_OPERATING_STATUS
   * @return JSON , key-value pair is reported for the required meta fields
   * @Example
   * Table invoked in SPARCS : xps_che<br>
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="ReportChe"/&gt;<br>
   * &lt;parameter id="cheId" value="ASC01"/&gt;<br>
   * &lt;parameter id="cheMetaFields" value="JOB_STEP_STATE,OPERATING_MODE,AUTOCHE_TECHNICAL_STATUS,AUTOCHE_OPERATING_STATUS"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String ReportChe(Map inParameters) {
    assert inParameters.size() == 3, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 3 parameters:
                                        <parameter id="command" value="ReportChe" />
                                        <parameter id="cheId" value="<che Id>" />
                                        <parameter id="cheMetaFields" value="<list of che fields>" />'''

    String inCheId = _testCommandHelper.checkParameter('cheId', inParameters);
    String inCheFields = _testCommandHelper.checkParameter('cheMetaFields', inParameters);

    try {
      //find che
      Che cheObj = ((Che) HibernateApi.getInstance().getUniqueEntityByDomainQuery(QueryUtils.createDomainQuery(ArgoEntity.CHE)
              .addDqPredicate(PredicateFactory.eq(ArgoField.CHE_SHORT_NAME, inCheId))));

      if (cheObj != null) {
        //Iterating through all the input params passed
        def fields = inCheFields.split(',');  // splitting the value to find each field
        String retVal = "";
        builder {
          actual_result fields.collect {
            switch (it) {
              case 'JOB_STEP_STATE':
                retVal = cheObj.getCheJobStepStateEnum();
                ["$it": retVal]
                break;
              case 'OPERATING_MODE':
                retVal = cheObj.getCheOperatingModeEnum();
                ["$it": _testCommandHelper.getActualEnumValue(retVal)]
                break;
              case 'AUTOCHE_TECHNICAL_STATUS':
                retVal = cheObj.getCheAutoCheTechnicalStatus();
                ["$it": retVal]
                break;
              case 'AUTOCHE_OPERATING_STATUS':
                retVal = cheObj.getCheAutoCheOperationalStatus();
                ["$it": retVal]
                break;
              case 'LAST_POSITION':
                retVal = cheObj.getCheLastPosition();
                ["$it": retVal]
                break;
              case 'STATUS':
                retVal = cheObj.getCheStatusEnum();
                ["$it": _testCommandHelper.getActualEnumValue(retVal)]
                break;
            }
          }
        }
        returnString = 'Reported CHE details '
      } else {
        returnString = 'Could not find the given CHE, please check the given CHE is available'
        builder {
          actual_result returnString;
        }
      }
    } catch (Exception ex) {
      returnString = "Failed to report Che:" + ex
      builder {
        actual_result returnString;
      }
    }
    LOGGER.debug('ReportCHE:' + returnString)
    return builder;
  }

  /**
   * Reports the data fields which are triggered by work flow during ASC or AGV Moves <br>
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=ReportEntities<br>
   * entities=WorkInstruction=wiGKey,WorkAssignment=waGKey,EcEvent=<Name of the CHE>
   * @return JSON , key-value pair is reported for the required meta fields
   * @Example
   * Table invoked in SPARCS: inv_wi,xps_workassignment,xps_ecevent <br>
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="ReportEntities"/&gt;<br>
   * &lt;parameter id="entities" value="WorkInstruction=65,WorkAssignment=23,EcEvent=CH05"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String ReportEntities(Map inParameters) {
    assert inParameters.size() == 2, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 2 parameters:
                                        <parameter id="command" value="ReportEntities" />
                                        <parameter id="entities" value="<che Id>" />'''

    String inEntities = _testCommandHelper.checkParameter('entities', inParameters);
    try {
      //Iterating through all the input params passed
      def fields = inEntities.split(',');  // splitting the value to find each field
      String[] attributeArray = "";
      String retVal, key, value = "";
      Che che = null;
      EcEvent ecEvent = null;
      WorkAssignment workAssignment = null;
      WorkInstruction workInstruction = null;
      DomainQuery domainQuery = null;
      builder {
        actual_result fields.collect { //iterates each key value pair and gets its attributes
          attributeArray = it.trim().split("=")  // gets key and value tokens
          if (attributeArray.size() == 2) {
            key = attributeArray[0].trim();
            value = attributeArray[1].trim();
            if (key.equalsIgnoreCase("Che")) {
              che = ((Che) HibernateApi.getInstance().getUniqueEntityByDomainQuery(QueryUtils.createDomainQuery(ArgoEntity.CHE)
                      .addDqPredicate(PredicateFactory.eq(ArgoField.CHE_SHORT_NAME, value))));
              if (che != null) {
                retVal = che.getCheJobStepStateEnum();
                ['CheJobStepState': _testCommandHelper.getActualEnumValue(retVal)];
              } else {
                ['Che not found']
              };
            } else if (key.equalsIgnoreCase("WorkAssignment")) {
              workAssignment = ((WorkAssignment) HibernateApi.getInstance().getUniqueEntityByDomainQuery(QueryUtils.createDomainQuery
              (ArgoEntity.WORK_ASSIGNMENT).addDqPredicate(PredicateFactory.eq(ArgoField.WORKASSIGNMENT_GKEY, value))));
              if (workAssignment != null) {
                retVal = workAssignment.getWorkassignmentStatusEnum();
                ['WorkAssignment_Status': _testCommandHelper.getActualEnumValue(retVal)]
              } else {
                ['Work Assignment not found']
              }
            } else if (key.equalsIgnoreCase("WorkInstruction")) {
              workInstruction = ((WorkInstruction) HibernateApi.getInstance().getUniqueEntityByDomainQuery(QueryUtils.createDomainQuery
              (MovesEntity.WORK_INSTRUCTION).addDqPredicate(PredicateFactory.eq(MovesField.WI_GKEY, value))));
              if (workInstruction != null) {
                retVal = workInstruction.getWiMoveStage();
                ['WorkInstruction_Status': _testCommandHelper.getActualEnumValue(retVal)];
              } else {
                ['Work Instruction not found']
              };
            } else if (key.equalsIgnoreCase("EcEvent")) {
              retVal = ""; //clearing the retVal to append more than once ecevent type
              domainQuery = QueryUtils.createDomainQuery(ArgoEntity.EC_EVENT).addDqPredicate(PredicateFactory.disjunction().
                      add(PredicateFactory.eq(ArgoField.ECEVENT_CHE_NAME, value)));
              List ecEventList = HibernateApi.getInstance().findEntitiesByDomainQuery(domainQuery); //get all ec events for the che
              Iterator ecEventIterator = ecEventList.iterator();
              while (ecEventIterator.hasNext()) {       //iterating the list of ec events and getting all ec event types
                String gKey = ecEventIterator.next().toString();
                gKey = gKey.substring(gKey.indexOf(":") + 1);
                ecEvent = ((EcEvent) HibernateApi.getInstance().getUniqueEntityByDomainQuery(QueryUtils.createDomainQuery(ArgoEntity.EC_EVENT).addDqPredicate(PredicateFactory.disjunction().
                        add(PredicateFactory.eq(ArgoField.ECEVENT_GKEY, gKey.toLong())))));
                if (ecEvent != null) {
                  retVal += ecEvent.getEceventTypeDescription() + ',';
                } else {
                  retVal = 'Ec Event not found'
                };
              }
              ['EcEvent_Type': retVal];
            }
          } else {
            ["Please check the input format, attributes not set"]
          }
        }
      }
    } catch (Exception ex) {
      builder {
        actual_result 'Failed to report entities ' + ex;
      }
    }
    return builder;
  }

  /**
   * Reports the Work Assignment status for the requested fields.<br>
   * Either report for a single field or multiple fields can be requested.<br>
   * For multiple field report,pass the metafields in a comma separated format.<br>
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=ReportWorkAssignment<br>
   * waGKey=Work Assignment of the gKey<br>
   * waMetaFields=list of wa fields - WORKASSIGNMENT_STATUS
   * @return JSON , key-value pair is reported for the required meta fields
   * @Example
   * Table invoked in SPARCS : xps_workassignment <br>
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="ReportWorkAssignment"/&gt;<br>
   * &lt;parameter id="waGKey" value="67"/&gt;<br>
   * &lt;parameter id="waMetaFields" value="WORKASSIGNMENT_STATUS,JOB_TYPE,MOVE_PURPOSE"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   *
   */
  public String ReportWorkAssignment(Map inParameters) {
    assert inParameters.size() == 3, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 3 parameters:
                                        <parameter id="command" value="ReportWorkAssignment" />
                                        <parameter id="waGKey" value="<waGKey>" />
                                        <parameter id="waMetaFields" value="<list of WorkAssignment fields>" />'''

    String inWAGKey = _testCommandHelper.checkParameter('waGKey', inParameters);
    String inWAMetaFields = _testCommandHelper.checkParameter('waMetaFields', inParameters);

    try {
      WorkAssignment waObj = ((WorkAssignment) HibernateApi.getInstance().getUniqueEntityByDomainQuery(QueryUtils.createDomainQuery(ArgoEntity.WORK_ASSIGNMENT)
              .addDqPredicate(PredicateFactory.eq(ArgoField.WORKASSIGNMENT_GKEY, inWAGKey))));
      String waStatus = '', waJobType = '', waMovePurpose = ''
      if (waObj != null) {
        //Iterating through all the input params passed
        def fields = inWAMetaFields.split(',');  // splitting the value to find each field
        builder {
          actual_result fields.collect {
            switch (it) {
              case 'WORKASSIGNMENT_STATUS':
                waStatus = waObj.getWorkassignmentStatusEnum().toString();
                ["$it": waStatus]
                break;
              case 'JOB_TYPE':
                waJobType = waObj.getWorkassignmentJobType().toString();
                ["$it": waJobType]
                break;
              case 'MOVE_PURPOSE':
                waMovePurpose = waObj.getWorkassignmentMovePurposeEnum().toString();
                ["$it": waMovePurpose]
                break
            }
          }
          if (!waMovePurpose.isEmpty() || !waJobType.isEmpty() || !waStatus.isEmpty()) {
            data('WA_MovePurpose': _testCommandHelper.getActualEnumValue(waMovePurpose), 'WA_JobType': _testCommandHelper.getActualEnumValue(waJobType), 'WA_Status': _testCommandHelper.getActualEnumValue(waStatus))
          }
        }
      } else {
        builder {
          actual_result 'Work Assignment is not found'
        }
      }
    } catch (Exception ex) {
      builder {
        actual_result 'Failed to report work assignment ' + ex;
      }
    }
    LOGGER.debug('ReportWorkAssignment : ' + inWAGKey)
    return builder;
  }

  /**
   * Reports the Work Instruction data for the requested fields.<br>
   * Either report for a single field or multiple fields can be requested.<br>
   * For multiple field report,pass the metafields in a comma separated format.
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=ReportWorkInstruction<br>
   * cheId=cheId<br>
   * wiMetaFields=list of wi fields - MOVE_STAGE,CHE_FETCH,CHE_PUT,CARRY_PUT,MOVE_TIME,MOVE_KIND,CURRENT_POSITION,TO_POSITION,FROM_POSITION
   * @return JSON , key-value pair is reported for the required meta fields
   * @Example
   * Table invoked in SPARCS : inv_wi <br>
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="ReportWorkInstruction"/&gt;<br>
   * &lt;parameter id="wiGKey" value="52"/&gt;<br>
   * &lt;parameter id="wiMetaFields" value="MOVE_STAGE,CHE_FETCH,CHE_PUT,CARRY_PUT,MOVE_TIME,MOVE_KIND,CURRENT_POSITION,TO_POSITION,FROM_POSITION"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String ReportWorkInstruction(Map inParameters) {
    assert inParameters.size() == 3, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 3 parameters:
                                        <parameter id="command" value="ReportWorkInstruction" />
                                        <parameter id="wiGKey" value="<wiGkey>" />
                                        <parameter id="wiMetaFields" value="<list of wi fields>" />'''

    String inWIGkey = _testCommandHelper.checkParameter('wiGKey', inParameters);
    String inWIMetaFields = _testCommandHelper.checkParameter('wiMetaFields', inParameters);

    try {
      //find WI
      DomainQuery dqWq = QueryUtils.createDomainQuery(MovesEntity.WORK_INSTRUCTION)
              .addDqPredicate(PredicateFactory.eq(MovesField.WI_GKEY, inWIGkey));
      WorkInstruction workInstructionObj = (WorkInstruction) HibernateApi.getInstance().getUniqueEntityByDomainQuery(dqWq);
      if (workInstructionObj != null) {
        com.navis.inventory.business.moves.MoveHistory moveHistory = workInstructionObj.getWiMoveHistory();
        //Iterating through all the input params passed
        def fields = inWIMetaFields.split(',');  // splitting the value to find each field
        String retVal = "";
        builder {
          actual_result fields.collect {
            switch (it) {
              case 'MOVE_STAGE':
                retVal = workInstructionObj.getWiMoveStage();
                ["$it": _testCommandHelper.getActualEnumValue(retVal)]
                break;
              case 'CHE_FETCH':
                if (moveHistory != null) {
                  retVal = moveHistory.getMvhsFetchCheId();
                  ["$it": retVal]
                }
                break;
              case 'CHE_PUT':
                if (moveHistory != null) {
                  retVal = moveHistory.getMvhsPutCheIndex();
                  ["$it": retVal]
                }
                break;
              case 'CARRY_PUT':
                if (moveHistory != null) {
                  retVal = moveHistory.getMvhsCarryCheId();
                  ["$it": retVal]
                }
                break;
              case 'MOVE_TIME'://MOVE_STAGE,CHE_FETCH,CHE_PUT,CARRY_PUT,MOVE_TIME,MOVE_KIND,CURRENT_POSITION,TO_POSITION,FROM_POSITION,MOVE_HISTORY,
                retVal = workInstructionObj.getWiEstimatedMoveTime();
                ["$it": retVal]
                break;
              case 'CURRENT_POSITION':
                retVal = workInstructionObj.getWiPosition();
                ["$it": retVal]
                break;
              case 'TO_POSITION':
                retVal = workInstructionObj.getWiToPosition();
                ["$it": retVal]
                break;
              case 'FROM_POSITION':
                retVal = workInstructionObj.getWiFromPosition();
                ["$it": retVal]
                break;
              case 'MOVE_KIND':
                retVal = workInstructionObj.getWiMoveKind();
                ["$it": _testCommandHelper.getActualEnumValue(retVal)]
                break;
              case 'SUSPEND_STATE':
                retVal = workInstructionObj.getWiSuspendState()
                ["$it": _testCommandHelper.getActualEnumValue(retVal)]
                break;
              default:
                retVal = 'Attribute : ' + it + ' not found in the list';
            }
          }
        }
      }
    } catch (Exception ex) {
      builder {
        actual_result 'Failed to report work instruction ' + ex;
      }
    }
    LOGGER.debug('ReportWorkInstruction:' + inWIGkey)
    return builder;
  }

  /**
   * Reports all job step projections related to the given CHE
   * when unit is disptched through RunAgvScheduler, job step projections will be created for the same
   * Either report for a single field or multiple fields can be requested.<br>
   * For multiple field report,pass the metafields in a comma separated format.
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=ReportJobStepProjections<br>
   * wiGKey=work instruction gkey of the plan<br>
   * cheKind=AGV,ASC<br>
   * waGKey="work assignment gkey" //In case of parking orders WI will not be created
   * @return JSON , key-value pair is reported for the required meta fields
   * @Example
   * Table invoked in SPARCS : argo_job_step_projection<br>
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="ReportJobStepProjections"/&gt;<br>
   * &lt;parameter id="wiGKey" value="13499"/&gt;<br>
   * &lt;parameter id="waGKey" value="356" /&gt;//In case of parking orders WI will not be created <br>
   * &lt;parameter id="cheKind" value="ASC,AGV"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String ReportJobStepProjections(Map inParameters) {
    assert inParameters.size() >= 2, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 3 parameters:
                                        <parameter id="command" value="ReportJobStepProjections" />
                                        <parameter id="wiGKey" value="work instruction gkey" />
                                        <parameter id="waGKey" value="work assignment gkey" /> //In case of parking orders WI will not be created
                                        <parameter id="cheKind" value="ASC,AGV" />'''

    String inWIGkey = '', inWAGkey = ''
    if (inParameters.containsKey('wiGKey')) {
      inWIGkey = inParameters.get('wiGKey', inParameters)
    };
    if (inParameters.containsKey('waGKey')) {
      inWAGkey = inParameters.get('waGKey', inParameters)
    };
    String inCheKind = _testCommandHelper.checkParameter('cheKind', inParameters);
    def result = ''
    try {
      WorkAssignment workAssignment
      if (!inWIGkey.isEmpty()) {   //when WI is given
        DomainQuery dqWq = QueryUtils.createDomainQuery(MovesEntity.WORK_INSTRUCTION)
                .addDqPredicate(PredicateFactory.eq(MovesField.WI_GKEY, inWIGkey));
        WorkInstruction workInstructionObj = (WorkInstruction) HibernateApi.getInstance().getUniqueEntityByDomainQuery(dqWq);
        if (workInstructionObj != null) {
          if (inCheKind.equalsIgnoreCase('AGV')) {
            workAssignment = workInstructionObj.wiItvWorkAssignment
          } else if (inCheKind.equalsIgnoreCase('ASC')) {
            workAssignment = workInstructionObj.wiCheWorkAssignment
          }
        } else {
          returnString = 'Failed to report job step projection : Given work instruction not found'
        }
      }
      if (!inWAGkey.isEmpty()) { //if only WA is given, in case of parking orders no WI will be created
        DomainQuery dq = QueryUtils.createDomainQuery(ArgoEntity.WORK_ASSIGNMENT).addDqPredicate(PredicateFactory.disjunction()
                .add(PredicateFactory.eq(ArgoField.WORKASSIGNMENT_GKEY, inWAGkey)));
        workAssignment = HibernateApi.getInstance().getUniqueEntityByDomainQuery(dq)
      }
      if (workAssignment != null) {
        Che che = workAssignment.workassignmentChe
        if (che != null) {
          List<JobStepProjection> jobStepProjectionList = com.navis.control.business.agv.AgvQaTestUtils.reportJobStepProjections(che.cheGkey)
          String inSQLStatement = 'select job_step_state from argo_job_step_projection where work_assignment_gkey=' + workAssignment.workassignmentGkey
          SQLQuery sqlQuery = HibernateApi.getInstance().getCurrentSession().createSQLQuery(inSQLStatement);
          List jspList = sqlQuery.list()
          if (jspList != null) {
            if (!jspList.toString().isEmpty()) {
              returnString = 'Reported job step projection:' + jspList
              result = "CHE : " + che.cheFullName + " JobStepProjections:" + jobStepProjectionList + "workAssignment: " + workAssignment.workassignmentGkey
            } else {
              returnString = 'Failed to report job step projection : No job step projection found for the che '
            }
          } else {
            returnString = 'Failed to report job step projection : No job step projection found for the che '
          }
        } else {
          returnString = 'Failed to report job step projection : No CHE is associated with the given work instruction'
        }
      } else {
        returnString = 'Failed to report job step projection : Unit not dispatched'
      }
    } catch (Exception ex) {
      returnString = 'Failed to report job step projection : ' + ex;
    }
    builder {
      actual_result returnString
      if (!result.isEmpty()) {
        data('JobStepProjection': result)
      }
    }
    LOGGER.debug('ReportJobSteProjection:' + returnString)
    return builder;
  }

  /**
   * Reports all undispatched jobs by the AGV scheduler currently.
   * related to the given CHE pool
   * Either report for a single field or multiple fields can be requested.<br>
   * For multiple field report,pass the metafields in a comma separated format.
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=ReportUndispatchedJobs<br>
   * poolName=name of the pool<br>
   * @return JSON , key-value pair is reported for the required meta fields
   * @Example
   * Table invoked in SPARCS : xps_pool <br>
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="ReportUndispatchedJobs"/&gt;<br>
   * &lt;parameter id="poolName" value="AGV01"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String ReportUndispatchedJobs(Map inParameters) {
    assert inParameters.size() == 2, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 2 parameters:
                                        <parameter id="command" value="ReportUndispatchedJobs" />
                                        <parameter id="poolName" value="pool name" />'''

    String inPoolName = _testCommandHelper.checkParameter('poolName', inParameters);
    def WI = ''
    try {
      ChePool chePool = ((ChePool) HibernateApi.getInstance().getUniqueEntityByDomainQuery(QueryUtils.createDomainQuery(ArgoEntity.CHE_POOL)
              .addDqPredicate(PredicateFactory.eq(ArgoField.POOL_NAME, inPoolName))));

      if (chePool != null) {
        List<WorkInstruction> workInstructionList = AgvQaTestUtils.reportJobList(chePool.getPoolGkey())
        if (workInstructionList != null) {
          if (!workInstructionList.isEmpty()) {
            String[] list = workInstructionList.toString().split(',')
            StringBuilder stringBuilder = new StringBuilder();
            def wiKey = 'WorkInstruction['
            def actualOut = '', out = ''
            list.each {
              WI += it.substring(it.indexOf(wiKey) + wiKey.length(), it.indexOf(']')) + ","
            }
            WI = _testCommandHelper.truncateEndingComma(WI)
            returnString = "Found 1 or more UnDispatched Jobs in the pool : " + inPoolName
          } else {
            returnString = 'No undispatched jobs found in the pool: ' + inPoolName
          }
        } else {
          returnString = 'No undispatched jobs found in the pool: ' + inPoolName
        }
      }
    } catch (Exception ex) {
      returnString = 'Failed to report undispatched jobs ' + ex;
    }
    builder {
      actual_result returnString
      data('WorkInstruction:': WI)
    }
    LOGGER.debug('ReportUndispatchedJobs:' + returnString)
    return builder;
  }

  /**
   * Reports all Dispatched jobs by the AGV scheduler currently.
   * related to the given CHE pool
   * Either report for a single field or multiple fields can be requested.<br>
   * For multiple field report,pass the metafields in a comma separated format.
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=ReportDispatchedJobs<br>
   * poolName=name of the pool<br>
   * @return JSON , key-value pair is reported for the required meta fields
   * @Example
   * Table invoked in SPARCS : xps_pool <br>
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="ReportDispatchedJobs"/&gt;<br>
   * &lt;parameter id="poolName" value="AGV01"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String ReportDispatchedJobs(Map inParameters) {
    assert inParameters.size() == 2, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 2 parameters:
                                        <parameter id="command" value="ReportDispatchedJobs" />
                                        <parameter id="poolName" value="pool name" />'''

    String inPoolName = _testCommandHelper.checkParameter('poolName', inParameters);
    def WI = ''
    List<WorkInstruction> workInstructionList;
    try {
      ChePool chePool = ((ChePool) HibernateApi.getInstance().getUniqueEntityByDomainQuery(QueryUtils.createDomainQuery(ArgoEntity.CHE_POOL)
              .addDqPredicate(PredicateFactory.eq(ArgoField.POOL_NAME, inPoolName))));
      if (chePool != null) {
        workInstructionList = AgvQaTestUtils.reportDispatchedJobs(chePool.getPoolGkey())

        if (workInstructionList != null && !workInstructionList.isEmpty()) {
          //WI = workInstructionList.toString()
          String[] list = workInstructionList.toString().split(',')
          StringBuilder stringBuilder = new StringBuilder();
          def wiKey = 'WorkInstruction['
          def actualOut = '', out = ''
          list.each {
            WI += it.substring(it.indexOf(wiKey) + wiKey.length(), it.indexOf(']')) + ","
            //actualOut += _testCommandHelper.getActualReport(it, wiKey)
          }
          WI = _testCommandHelper.truncateEndingComma(WI)
          returnString = "Found 1 or more Dispatched Jobs in the pool : " + inPoolName
        } else {
          returnString = 'No dispatched jobs found in the pool: ' + inPoolName
        }
      } else {
        returnString = 'Failed to report dispatched jobs : Pool is null'
      }
    } catch (Exception ex) {
      returnString = 'Failed to report dispatched jobs ' + ex;
    }
    builder {
      actual_result returnString
      if (!WI.isEmpty()) {
        data('DispatchedJobs': WI)
      }
    }
    LOGGER.debug('ReportDispatchedJobs:' + returnString)
    return builder;
  }

  public String ReportUrgentJobs(Map inParameters) {
    assert inParameters.size() == 2, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 2 parameters:
                                        <parameter id="command" value="ReportUrgentJobs" />
                                        <parameter id="poolName" value="pool name" />'''

    String inPoolName = _testCommandHelper.checkParameter('poolName', inParameters);
    def WI = ''
    try {
      ChePool chePool = ((ChePool) HibernateApi.getInstance().getUniqueEntityByDomainQuery(QueryUtils.createDomainQuery(ArgoEntity.CHE_POOL)
              .addDqPredicate(PredicateFactory.eq(ArgoField.POOL_NAME, inPoolName))));

      if (chePool != null) {
        List<WorkInstruction> workInstructionList = AgvQaTestUtils.reportUrgentJobs(chePool.getPoolGkey())
        if (workInstructionList != null) {
          if (!workInstructionList.isEmpty()) {
            String[] list = workInstructionList.toString().split(',')
            StringBuilder stringBuilder = new StringBuilder();
            def wiKey = 'WorkInstruction['
            def actualOut = '', out = ''
            list.each {
              WI += it.substring(it.indexOf(wiKey) + wiKey.length(), it.indexOf(']')) + ","
            }
            WI = _testCommandHelper.truncateEndingComma(WI)
            returnString = "Found 1 or more urgent jobs in the pool : " + inPoolName
          } else {
            returnString = 'No urgent jobs found in the pool: ' + inPoolName
          }
        } else {
          returnString = 'No urgent jobs found in the pool: ' + inPoolName
        }
      }
    } catch (Exception ex) {
      returnString = 'Failed to report urgent jobs list ' + ex;
    }
    builder {
      actual_result returnString
      data('WorkInstruction:': WI)
    }
    LOGGER.debug('ReportUrgentJobs:' + returnString)
    return builder;
  }

  /**
   * Returns a list of Events registered for Vessel/Unit
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=ReportHistoryEvent<br>
   * @param IdType - Indicates Vessel/Unit - entity for which events need to be fetched
   * @param Id - Id for which event needs to be fetched. for IdType = Vessel,Id = Vessel visit id.
   *             for IdType = Unit, Id= unit id
   * @return JSON ,
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="ReportHistoryEvent"/&gt;<br>
   * &lt;parameter id="IdType" value="VESSEL/UNIT"/&gt;<br>
   * &lt;parameter id="Id" value="1234" /&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String ReportHistoryEvent(Map inParameters) {
    assert inParameters.size() == 3, '''Must supply 3 parameters:
                                          <parameter id="command" value="ReportHistoryEvent" />
                                          <parameter id="IdType" value="VESSEL/UNIT" />
                                          <parameter id="Id" value="1234" />'''
    String inIdType = _testCommandHelper.checkParameter("IdType", inParameters);
    String inId = _testCommandHelper.checkParameter("Id", inParameters);
    String eventIds = "";
    String eventChanges = "";
    ServicesManager srvcMgr = (ServicesManager) Roastery.getBean(ServicesManager.BEAN_ID);
    List eventsList = null;
    try {
      if (inIdType.isEmpty()) {
        throw BizFailure.create("Invalid Id Type. Enter Vessel/Unit");
      } else {
        if ("VESSEL".equalsIgnoreCase(inIdType)) {
          CarrierVisit visit = CarrierVisit.findCarrierVisit(ContextHelper.getThreadFacility(), LocTypeEnum.VESSEL, inId);
          if (visit != null && visit.getCvCvd() != null) {
            eventsList = srvcMgr.getEventHistory(visit.getCvCvd());
          } else {
            throw BizFailure.create("No vessel visit was found with given ID = " + inId);
          }
        } else if ("UNIT".equalsIgnoreCase(inIdType)) {
          SearchResults results = getUnitFinder().findUfvByDigits(inId, false, false);
          assert results.getFoundCount() == 1, 'More than one equipment or no equipment found for id ' + inId;
          UnitFacilityVisit ufv = (UnitFacilityVisit) HibernateApi.getInstance().load(UnitFacilityVisit.class, results.getFoundPrimaryKey());
          assert ufv != null, "Could not find equipment " + inId;
          Unit unit = ufv.getUfvUnit();
          if (unit != null) {
            eventsList = srvcMgr.getEventHistory(unit);
          } else {
            throw BizFailure.create("No unit was found with given ID = " + inId);
          }
        } else {
          throw BizFailure.create("Invalid Entity Type. Enter Vessel/Unit");
        }
      }
      if (eventsList != null && !eventsList.isEmpty()) {
        eventsList.each {
          Event event = it;
          eventIds = eventIds + event.getEventTypeId() + ',';
          if (event.getEventFieldChangesString() != null && !event.getEventFieldChangesString().isEmpty()) {
            eventChanges = eventChanges + 'Changes for Event Id ' + event.getEventTypeId() + ' : ' + event.getEventFieldChangesString() + ','
          };
        }
        eventIds = _testCommandHelper.truncateEndingComma(eventIds);
        eventChanges = StringEscapeUtils.unescapeXml(_testCommandHelper.truncateEndingComma(eventChanges));
        returnString = 'History/Event successfully reported for given entity: ' + StringEscapeUtils.unescapeXml(eventIds);
      } else {
        throw BizFailure.create("No events where recorded for entity: " + inId);
      }
    } catch (Exception inEx) {
      returnString = 'Report History Event failed ' + inEx;
    } finally {
      builder {
        actual_result returnString;
        data('EventFieldChanges': eventChanges)
      }
    }
    LOGGER.debug('ReportHistoryEvent:' + returnString)
    return builder;
  }

  /**
   * Reports information about all the WIs for the given unit
   *
   *
   * @param inParameters The map containing the method name to call along with the parameters<br><br>
   * <br>command=ReportWorkInstructionByUnit
   * <br>unitId=Container Number for which truck transaction should be deleted
   * @return JSON , <code>Truck Transactions for Unit: '+ inUnitId + 'are cancelled</code><br>
   *                <code>Truck Transactions for Unit: '+ inUnitId + 'are cancelled </code>
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="ReportWorkInstructionByUnit" /&gt;<br>
   * &lt;parameter id="unitId" value="TEST0000019"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String ReportWorkInstructionByUnit(Map inParameters) {
    assert inParameters.size() == 3, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 3 parameters:
                                        <parameter id="command" value="ReportWorkInstructionByUnit" />
                                        <parameter id="unitId" value="container id" />
                                        <parameter id="wiMetaFields" value="MOVE_STAGE,CHE_FETCH,CHE_PUT,CARRY_PUT,FROM_POSITION,TO_POSITION,MOVE_KIND" />
                                        '''

    String inUnitId = _testCommandHelper.checkParameter('unitId', inParameters);
    String inMetaFields = _testCommandHelper.checkParameter('wiMetaFields', inParameters);
    def wiList = ''
    try {
      Unit unitObj = ((Unit) HibernateApi.getInstance().getUniqueEntityByDomainQuery(QueryUtils.createDomainQuery(InventoryEntity.UNIT)
              .addDqPredicate(PredicateFactory.eq(InventoryField.UNIT_ID, inUnitId))));
      if (unitObj != null) {
        //find WI
        if (unitObj.unitActiveUfv != null) {
          DomainQuery dqWq = QueryUtils.createDomainQuery(MovesEntity.WORK_INSTRUCTION)
                  .addDqPredicate(PredicateFactory.eq(MovesField.WI_UYV,
                  unitObj.unitActiveUfv.ufvGkey));
          List<WorkInstruction> workInstructionList = HibernateApi.getInstance().findEntitiesByDomainQuery(dqWq);
          def out = ''
          workInstructionList.each {
            def eachWI = it
            if (eachWI != null) {
              com.navis.inventory.business.moves.MoveHistory moveHistory = eachWI.getWiMoveHistory();
              //Iterating through all the input params passed
              def fields = inMetaFields.split(',');  // splitting the value to find each field
              String retVal = "";
              fields.each {
                switch (it) {
                  case 'MOVE_STAGE':
                    def moveStage = eachWI.getWiMoveStage().toString()
                    retVal += it + ":" + _testCommandHelper.getActualEnumValue(moveStage) + ",";
                    break;
                  case 'CHE_FETCH':
                    if (moveHistory != null) {
                      retVal += it + ":" + moveHistory.getMvhsFetchCheId() + ","
                    };
                    break;
                  case 'CHE_PUT':
                    if (moveHistory != null) {
                      retVal += it + ":" + moveHistory.getMvhsPutCheIndex() + ","
                    };
                    break;
                  case 'CARRY_PUT':
                    if (moveHistory != null) {
                      retVal += it + ":" + moveHistory.getMvhsCarryCheId() + ","
                    };
                    break;
                  case 'MOVE_TIME':
                    retVal += it + ":" + eachWI.getWiEstimatedMoveTime() + ",";
                    break;
                  case 'CURRENT_POSITION':
                    retVal += it + ":" + eachWI.getWiPosition() + ",";
                    break;
                  case 'TO_POSITION':
                    retVal += it + ":" + eachWI.getWiToPosition() + ",";
                    break;
                  case 'FROM_POSITION':
                    retVal += it + ":" + eachWI.getWiFromPosition() + ",";
                    break;
                  case 'MOVE_KIND':
                    def moveKind = eachWI.getWiMoveKind().toString()
                    retVal += it + ":" + _testCommandHelper.getActualEnumValue(moveKind) + ",";
                    break;
                  default:
                    retVal += 'Attribute : ' + it + ' not found in the list';
                }
              }
              retVal = _testCommandHelper.truncateEndingComma(retVal)
              if(eachWI != null)
              wiList += eachWI.wiGkey + ","
              out += 'Unit:' + inUnitId + ':' + retVal + ","
            }
          }
          out = _testCommandHelper.truncateEndingComma(out)
          returnString = out;
          if(wiList != null && !wiList.isEmpty())
          wiList = _testCommandHelper.truncateOnlyEndingComma(wiList)
        } else {
          returnString = 'Work Instruction details not reported : Active Ufv not found'
        }
      } else {
        returnString = 'Work Instruction details not reported : Unit not found'
      }
    } catch (Exception ex) {
      returnString = 'Exception while obtaining work instruction report' + ex.printStackTrace();
    }
    builder {
      actual_result returnString
    }
    return builder;
  }

  /**
   * Reports previous fire time for all background jobs
   *
   *
   * @param inParameters The map containing the method name to call along with the parameters<br><br>
   * <br>command=ReportPreviousFireTime
   * @return JSON , <code>JobName : PreviousFireTime</code><br>
   *                <code>Background jobs are not scheduled</code>  if no jobs are scheduled
   *                <code>Caught exception while reporting previous fire time</code>  if exception occured
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="ReportPreviousFireTime" /&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String ReportPreviousFireTime(Map inParameters) {
    assert inParameters.size() >= 1, '''Must supply 1 parameters:
                                          <parameter id="command" value="ReportPreviousFireTime" />'''
    try {
    Collection<com.navis.framework.quartz.QuartzJobDetail> scheduledJobs = com.navis.framework.quartz.QuartzHelper.getAllScheduledJobs(true);
    Trigger trigger;
    if (scheduledJobs != null && scheduledJobs.size() > 0) {
      for (com.navis.framework.quartz.QuartzJobDetail jobDetail : scheduledJobs) {
        if (jobDetail != null) {
          trigger = jobDetail.getTrigger()
          def jobName = jobDetail.getName()
          if(jobName != null) {
          if (trigger != null) {
            def previousFiretime = trigger.getPreviousFireTime()
            if (previousFiretime != null) {
              returnString += jobName + ":" + previousFiretime + ","
            } else returnString += 'Previous fire time for the job :' + jobName + ' is null'
            } else returnString += 'Trigger for the job :' + jobName + ' is null'
          } else returnString += 'JobName is null'
        }
      }
    } else returnString = 'Background jobs are not scheduled'
    } catch (Exception ex) {
      LOGGER.error(ex.getMessage(),ex.getCause())
      returnString = 'Caught exception while reporting previous fire time'
    }
    LOGGER.debug('ReportPreviousFireTime:' + returnString)
    builder {
      actual_result returnString
    }
    return builder;
  }

  /**
   * Creates or find stack status for the given ASC Block.
   * Takes row,column names as inputs
   * @param ascBlock
   * @param inStartRow
   * @param inStartColumn
   * @param inEndRow
   * @param inEndColumn
   * @return
   */
  private List<StackStatus> findOrCreateStackStatus(StackBlock ascBlock, String inStartRow, String inEndRow, String inStartColumn, String inEndColumn) {
    List<StackStatus> statuses;

    AbstractBin lowerLeftBin = ascBlock.findYardBinFromRowAndColumnStrings(inStartRow, inStartColumn);
    AbstractBin upperRightBin = ascBlock.findYardBinFromRowAndColumnStrings(inEndRow, inEndColumn);
    YardStack lowerLeftStack = (YardStack) HibernateApi.getInstance().downcast(lowerLeftBin, YardStack.class);
    YardStack upperRightStack = (YardStack) HibernateApi.getInstance().downcast(upperRightBin, YardStack.class);

    if (lowerLeftStack != null && upperRightStack != null) {
      statuses = YardTestUtils.findOrCreateStackStatus(lowerLeftStack, upperRightStack)
    };
    return statuses;
  }

  public static UnitFinder getUnitFinder() {
    return (UnitFinder) Roastery.getBean(UnitFinder.BEAN_ID);
  }
}
