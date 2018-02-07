package test.extension.groovy

import com.navis.argo.ArgoEntity
import com.navis.argo.ArgoField
import com.navis.argo.ContextHelper
import com.navis.argo.business.atoms.*
import com.navis.argo.business.model.CarrierVisit
import com.navis.argo.business.model.LocPosition
import com.navis.argo.business.model.Yard
import com.navis.argo.business.xps.model.*
import com.navis.control.business.ControlTestUtils
import com.navis.control.business.asc.AscMove
import com.navis.control.business.asc.AscTestUtils
import com.navis.framework.business.Roastery
import com.navis.framework.persistence.DatabaseHelper
import com.navis.framework.persistence.HibernateApi
import com.navis.framework.portal.QueryUtils
import com.navis.framework.portal.UserContext
import com.navis.framework.portal.context.PortalApplicationContext
import com.navis.framework.portal.query.DomainQuery
import com.navis.framework.portal.query.PredicateFactory
import com.navis.framework.util.BizFailure
import com.navis.framework.util.BizViolation
import com.navis.inventory.MovesEntity
import com.navis.inventory.MovesField
import com.navis.inventory.business.TimeFrame
import com.navis.inventory.business.api.SearchResults
import com.navis.inventory.business.api.UnitFinder
import com.navis.inventory.business.atoms.WqTypeEnum
import com.navis.inventory.business.moves.WorkInstruction
import com.navis.inventory.business.moves.WorkQueue
import com.navis.inventory.business.units.InventoryTestUtils
import com.navis.inventory.business.units.UnitFacilityVisit
import com.navis.inventory.business.units.UnitYardVisit
import com.navis.rail.business.entity.RailcarVisit
import com.navis.spatial.business.api.IBinModel
import com.navis.spatial.business.model.AbstractBin
import com.navis.spatial.business.model.BinType
import com.navis.yard.business.YardTestUtils
import com.navis.yard.business.model.StackBlock
import com.navis.yard.business.model.YardBinModel
import com.navis.yard.business.model.YardStack
import com.navis.yard.business.stow.IYardStowValidator
import org.apache.log4j.Level
import org.apache.log4j.Logger

/**
 * Created with IntelliJ IDEA.
 * User: rajansh
 * Date: 29/10/13
 * Time: 2:58 PM
 * To change this template use File | Settings | File Templates.
 */
class TestCommandManipulation {

  private HibernateApi _hibernateApi;
  /** holds the result returned by each method */
  def String returnString = null;
  /** json builder, frames the output in json format */
  def builder = new groovy.json.JsonBuilder();
  private DatabaseHelper _teamsDbHelper;
  /** handler to invoke the fields in helper class */
  test.extension.groovy.TestCommandHelper _testCommandHelper = new test.extension.groovy.TestCommandHelper();

  //Logger for TestCommand
  public Logger LOGGER = Logger.getLogger(TestCommandManipulation.class);

  /**
   * Performs a stow validation of the unit on the location with the given time frame
   *
   * @param inParameters The map containing the method name to call along with the parameters<br><br>
   * command=ValidateYardStow<br>
   * unitId=Name of the equipment<br>
   * location=Yard Location<br>
   * timeFrame=Time Frame=CURRENT, FUTURE, etc..
   * @return JSON , <code>Stow validation successful</code> - if success<br>
   *              <code>Could not find equipment</code> - if the equipment is not available<br>
   *              <code>More than one equipment or no equipment found for id</code> - when there are duplicate equipments
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="ValidateYardStow"/&gt;<br>
   * &lt;parameter id="unitId" value="APLU8362946" /&gt;<br>
   * &lt;parameter id="location" value="161001.1" /&gt;<br>
   * &lt;parameter id="timeFrame" value="CURRENT" /&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String ValidateYardStow(Map inParameters) {
    assert inParameters.size() == 4, '''Must supply 4 parameters:
                                        <parameter id="command" value="ValidateYardStow" />
                                        <parameter id="unitId" value="<Unit Id>" />
                                        <parameter id="location" value="<Yard Location>" />
                                        <parameter id="timeFrame" value="<Time Frame> = CURRENT, FUTURE, etc.." />'''

    String inUnitId = _testCommandHelper.checkParameter('unitId', inParameters);
    String inlocation = _testCommandHelper.checkParameter('location', inParameters);
    String inTimeFrame = _testCommandHelper.checkParameter('timeFrame', inParameters);
    try {
      IYardStowValidator yardStowValidator = (IYardStowValidator) PortalApplicationContext.getBean(IYardStowValidator.BEAN_ID);

      //Find the ufv first
      UnitFinder finder = (UnitFinder) Roastery.getBean(UnitFinder.BEAN_ID);
      SearchResults results = finder.findUfvByDigits(inUnitId, false, false);
      assert results.getFoundCount() == 1, 'More than one equipment or no equipment found for id ' + inUnitId;
      UnitFacilityVisit ufv = (UnitFacilityVisit) HibernateApi.getInstance().load(UnitFacilityVisit.class, results.getFoundPrimaryKey());
      assert ufv != null, "Could not find equipment " + inUnitId;

      //Validate stow
      BizViolation violation = yardStowValidator.validateYardStow(ufv, inlocation, TimeFrame."$inTimeFrame");

      //Return all the error keys as a string if there is error else return 'Stow successful'
      //hasMessage(violation, YardPropertyKeys.YARD_STOW_CONTAINER_FLOATS_NONE_BELOW)
      if (violation == null) {
        returnString = "Stow validation successful";
      } else {
        while (violation != null) {
          if (returnString == null) {
            returnString = violation._errkey.key;
          } else {
            returnString += ',' + violation._errkey.key;
          }
          violation = violation.getNextViolation();
        }
      }
    } catch (Exception ex) {
      returnString = 'Stow validation failed ' + ex;
    }
    LOGGER.debug('ValidateYardStow :' + inUnitId + ' : ' + returnString)
    builder {
      actual_result returnString
    }
    return builder;
  }

  /**
   * Selects the given range of Transfer point in the Transfer zone and sets the usage for all of them. <br>
   * Handles clearing the transfer point usage also when the tpUsage is passed as 'Other'
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=SetTransferPointUsage<br>
   * tzName=Name of the transfer zone<br>
   * startRow=Row position in Internal format of the block <br>
   * endRow=Row position in Internal format of the block  <br>
   * startCol=Col position in Internal format of the block <br>
   * endCol=Col position in Internal format of the block <br>
   * tpUsage=Receival,Delivery,YardShift,YardMove,VeslDisch,VeslLoad,ShiftOnBoard,RailDisch,RailLoad, Other (Clear) <br>
   * @return JSON
   * @Example
   * Table invoked in SPARCS : xps_movekindallowed <br>
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="SetTransferPointUsage" /&gt;<br>
   * &lt;parameter id="tzName" value="BL01" /&gt;<br>
   * &lt;parameter id="startRow" value="A" /&gt;<br>
   * &lt;parameter id="endRow" value="F" /&gt;<br>
   * &lt;parameter id="startCol" value="01" /&gt;<br>
   * &lt;parameter id="endCol" value="05" /&gt;<br>
   * &lt;parameter id="tpUsage" value="Receival,Delivery" /&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   *
   */
  public String SetTransferPointUsage(Map inParameters) {
    assert inParameters.size() >= 3, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply more than 3 parameters:
                                        <parameter id="command" value="SetTransferPointUsage" />
                                        <parameter id="tzName" value="<Block Name>" />
                                        <parameter id="startRow" value="<Row position in Internal format of the block>" />
                                        <parameter id="endRow" value="<Row position in Internal format of the block>" />
                                        <parameter id="startCol" value="<Col position in Internal format of the block>" />
                                        <parameter id="endCol" value="<Col position in Internal format of the block>" />
                                        <parameter id="tpUsage" value="Receival,Delivery"/>'''

    String inTzName = _testCommandHelper.checkParameter('tzName', inParameters);
    String inTPUsage = _testCommandHelper.checkParameter('tpUsage', inParameters);
    String inStartRow, inEndRow, inStartCol, inEndCol = "";
    StackBlock transferPoint = YardTestUtils.findStackBlock(inTzName);


    ArrayList<WiMoveKindEnum> enumList = new ArrayList<WiMoveKindEnum>();
    def list = [];
    //finding all status values to be set
    inTPUsage.split(',').each {
      String enumValue = it.trim();
      WiMoveKindEnum moveKindEnum = WiMoveKindEnum."$enumValue";
      list.add(enumValue);
      enumList.add(moveKindEnum);
    }
    //get tp usage
    String moveKindsAllowed = _testCommandHelper.getMoveKindAllowedString(list);
    WiMoveKindEnum[] moveKindEnums = (WiMoveKindEnum[]) enumList.toArray();
    MoveKindAllowed moveKindAllowed;
    int countAffected = 0;
    if (inParameters.size() == 7) {
      inStartRow = inParameters.get('startRow');
      inEndRow = inParameters.get('endRow');
      inStartCol = inParameters.get('startCol');
      inEndCol = inParameters.get('endCol');
      //If the start and end row columns are not empty, set tp usage for the selected tp's
      if (!inStartRow.isEmpty() && !inStartCol.isEmpty() && !inEndCol.isEmpty() && !inEndRow.isEmpty()) {
        List<StackStatus> statusList = findOrCreateStackStatus(transferPoint, inStartRow, inEndRow,
                inStartCol, inEndCol);
        if (statusList != null) {
          statusList = statusList.sort()
          statusList.each {
            def binName = it.getStackstatusBin()
            if (binName != null) {
              countAffected = YardTestUtils.createOrUpdateMoveKindsAllowed(binName, moveKindEnums)
            };
          }
          if (countAffected > 0) {
            returnString = 'TP Usage set'
          } else {
            returnString = 'TP could not be found'
          }
        } else {
          returnString = 'TP could not be found'
        }
      } else {  //set tp usage for the whole block.
        List<AbstractBin> bins;
        if (BinType.findBinType(IBinModel.BIN_TYPE_ID_STACK) != null) {
          bins = transferPoint.findDescendantsOfBinType(BinType.findBinType(IBinModel.BIN_TYPE_ID_STACK));
        }
        if (bins != null) {
          for (AbstractBin bin : bins) {
            if (bin != null) {
              countAffected = YardTestUtils.createOrUpdateMoveKindsAllowed(bin, moveKindEnums)
            };
            returnString = 'TP Usage set';
          }
        } else {
          returnString = 'TP could not be found'
        }
      }
    } else { //set tp usage for the whole block.
      List<AbstractBin> bins;
      if (BinType.findBinType(IBinModel.BIN_TYPE_ID_STACK) != null) {
        bins = transferPoint.findDescendantsOfBinType(BinType.findBinType(IBinModel.BIN_TYPE_ID_STACK));
      }
      if (bins != null) {
        for (AbstractBin bin : bins) {
          countAffected = YardTestUtils.createOrUpdateMoveKindsAllowed(bin, moveKindEnums);
        }
        returnString = 'TP Usage set';
      } else {
        returnString = 'TP could not be found'
      }
    }
    LOGGER.debug('SetTransferPointUsage:' + returnString + ': TZ Name : ' + inTzName)
    builder {
      actual_result returnString;
    }
    return builder;
  }

  /**
   * Plans to move container within the yard and creates a Work Instruction for the plan.
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=PlanUnitYard<br>
   * unitId=Equipment which required the plan to move<br>
   * location=Destination Slot to where the container needs to be moved
   * @return <code>Plan to move unit within yard created</code> if success<br>
   *         <code>More than one equipment found for id</code> if duplicate equipment found<br>
   *         It also contains a key 'data' in the JSON, to report the gKey of the Work Instruction created.
   * @Example
   * Table invoked by SPARCS : inv_wi, inv_unit<br>
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="PlanUnitYard"/&gt;<br>
   * &lt;parameter id="unitId" value="APLU8362946" /&gt;<br>
   * &lt;parameter id="location" value="161001.1" /&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String PlanUnitYard(Map inParameters) {
    assert inParameters.size() >= 3, '''Must supply 3 parameters:
                                          <parameter id="command" value="PlanUnitYard" />
                                          <parameter id="unitId" value="<Unit Id>" />
                                          <parameter id="location" value="<Destination Location>" />
                                          <parameter id="moveType" value="InterStack,IntraStack" />'''


    String inUnitId = _testCommandHelper.checkParameter('unitId', inParameters);
    String inDestinationSlot = _testCommandHelper.checkParameter('location', inParameters);
    String inMoveType = ''
    if (inParameters.containsKey('moveType')) {
      inMoveType = inParameters.get('moveType')
    }
    WorkInstruction wi = null;
    try {
      Yard yard = ContextHelper.getThreadYard();
      UnitFinder finder = (UnitFinder) Roastery.getBean(UnitFinder.BEAN_ID);
      SearchResults results = finder.findUfvInYardByDigits(inUnitId, false);
      assert results.getFoundCount() == 1, 'More than one equipment found for id ' + inUnitId;

      UnitFacilityVisit ufv = (UnitFacilityVisit) HibernateApi.getInstance().load(UnitFacilityVisit.class, results.getFoundPrimaryKey());
      UnitYardVisit uyv = ufv.getUyvForYard(yard);
      def wqType = 'yard-Admin'
      if (!inMoveType.isEmpty()) {
        if (inMoveType.equalsIgnoreCase('InterStack')) {
          wqType = 'yard-INTERSTACKMOVES'
        } else if (inMoveType.equalsIgnoreCase('IntraStack')) {
          wqType = 'yard-INTRASTACKMOVES'
        }
      }

      WorkQueue wq = InventoryTestUtils.findOrCreateWorkQueue(wqType, WqTypeEnum.YARD, Boolean.TRUE);
      assert wq != null;
      wi = InventoryTestUtils.createWorkInstruction(uyv, wq, WiMoveKindEnum.YardMove, WiMoveStageEnum.PLANNED, inDestinationSlot);
      assert wi != null;
      returnString = "Plan to move unit within yard created";
    } catch (Exception ex) {
      returnString = 'Plan to move unit failed ' + ex;
    }
    builder {
      actual_result returnString;
      if (wi != null) {
        data('WiGkey': wi.wiGkey)
      }
    }
    LOGGER.debug('PlanUnitYard:' + returnString + 'for the unit :' + inUnitId)
    return builder;
  }

  /**
   * Dispatches unit to che and creates a work assignment for the same.
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=DispatchUnitToChe<br>
   * cheId=che name to which the unit needs to be dispatched<br>
   * unitId=equipment which needs to be dispatched<br>
   * @return <code>Unit dispatched to Che</code> if success, else<br>
   *         <code>Failed to assign units to Che</code><br>
   *         <code>Che not found/dispatched</code> - when che is not available or the the unit is already dispatched
   * @Postcondition
   * An order should be inserted into asc/agv orders in TEAMS depends on whether the CHE is AGV or ASC.
   * @Example
   * Table invoked by SPARCS : xps_workassignment, inv_unit <br>
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="DispatchUnitToChe" /&gt;<br>
   * &lt;parameter id="unitId" value="APLU8362946" /&gt;<br>
   * &lt;parameter id="cheId" value="ASC01" /&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String DispatchUnitToChe(Map inParameters) {
    assert inParameters.size() >= 3, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 3 or more parameters:
                                        <parameter id="command" value="DispatchUnitToChe" />
                                        <parameter id="cheId" value="<che Id>" />
                                        <parameter id="unitId" value="<unitId>" />
                                        <parameter id="isTwin" value="false/true" />'''

    String inCheId = _testCommandHelper.checkParameter('cheId', inParameters);
    String inUnitId = _testCommandHelper.checkParameter('unitId', inParameters);
    String inIsTwin = inParameters.get('isTwin');
    List assignedChes = null;
    boolean isTwin = false;
    Logger.getLogger("com.navis.control.business.taskhandlers.AbstractTaskHandler").setLevel(Level.DEBUG);
    Logger.getLogger("com.navis.control.business.model.ControlManagerPea").setLevel(Level.DEBUG);
    Logger.getLogger("com.navis.control.business.dispatch.ControlDispatcherPea").setLevel(Level.DEBUG);
    Logger.getLogger("com.navis.control.business.TransferZoneWorkInstructionProvider").setLevel(Level.DEBUG);
    Logger.getLogger("org.mule.routing.CollectionSplitter").setLevel(Level.ERROR);

    if (inIsTwin != null) {
      if (!inIsTwin.isEmpty()) {
        if (inIsTwin.equalsIgnoreCase('true')) {
          isTwin = true
        } else {
          isTwin = false
        }
      }
    };
    try {
      assignedChes = ControlTestUtils.dispatchUnit(inCheId, inUnitId, isTwin);
      if (assignedChes == null) {
        returnString = "Che not found/dispatched";
      } else {
        returnString = "Unit dispatched to che";
      }
      assert assignedChes != null, returnString = "Failed to assign units to Che"
      returnString = "Unit dispatched to che";
    }
    catch (Exception ex) {
      //2013-07-25 oviyak 2.6.J ARGO-49884 Hide dynamic gkey value in the DispatchUnitChe API.
      String errorMsg = ex.getLocalizedMessage();
      String[] msgs = errorMsg.split(':');
      for (int i = 0; i < msgs.size(); i++) {
        String removeGkeyFromThis = msgs.getAt(i);
        int gkeyIndex = removeGkeyFromThis.indexOf('[');
        if (gkeyIndex != -1) {
          String removedMsg = removeGkeyFromThis.substring(0, gkeyIndex + 1);
          msgs[i] = removedMsg;
        }
      }
      returnString = "Exception while dispatching che : " + msgs.join('');
      LOGGER.error('Exception while dispatching che :' + ex)
    }
    builder {
      actual_result returnString;
      if (assignedChes != null) {
        data('WAGkey': assignedChes.get(0))
      }
    }
    LOGGER.debug('DispatchUnitToChe:' + returnString + 'for the unit :' + inUnitId)
    return builder;
  }

  /**
   * Resets che to IDLE state so that new jobs can be assigned
   * It operates on SPARCS table.
   * If the CHE is ASC, it sets the work_status = 'IDLE' in asc_status table in TEAMS database as well.
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=ResetChe<br>
   * cheId=che name for which reset is required
   * @return <code>Che reset successful</code> if success, else<br>
   *         <code>Che reset not done , Reason:Che is null</code>
   * @Example
   * Table invoked by SPARCS : xps_che <br>
   * Table invoked by TEAMS (if CHE is ASC) : asc_status
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="ResetChe /&gt;<br>
   * &lt;parameter id="cheId" value="ASC01"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   *
   */
  public String ResetChe(Map inParameters) {
    assert inParameters.size() == 2, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 2 parameters:
                                        <parameter id="command" value="ResetChe" />
                                        <parameter id="cheId" value="<cheId>" />'''

    String inCheId = _testCommandHelper.checkParameter('cheId', inParameters);
    String returnString, tableName = "";
    try {
      //find che
      String[] cheList = inCheId.split(',')
      cheList.each {
        Che che = ((Che) HibernateApi.getInstance().getUniqueEntityByDomainQuery(QueryUtils.createDomainQuery(ArgoEntity.CHE)
                .addDqPredicate(PredicateFactory.eq(ArgoField.CHE_SHORT_NAME, it))));
        if (che != null) {
          String cheType = che.cheKindEnum;
          che.setCheJobStepStateEnum(CheJobStepStateEnum.IDLE);
          tableName = new test.extension.groovy.TestCommandTEAMS().findCheTableName(_testCommandHelper.getActualEnumValue(cheType));

          if ("asc_status".equalsIgnoreCase(tableName)) {
            _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).execute(
                    String.format(
                            "update " + tableName + " SET work_status = 'IDLE' where che_id = '" + it + "'"
                    )
            );
          }
          returnString = 'Che reset successful';
        } else {
          returnString = "Che reset not done , Reason:Che is null";
        }
      }
    } catch (Exception ex) {
      returnString 'Reset che failed ' + ex;
    }
    builder {
      actual_result returnString
    }
    LOGGER.debug('ResetChe:' + returnString + 'for the che :' + inCheId)
    return builder;
  }

  /**
   * Plans to load unit to vessel
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=PlanUnitLoad<br>
   * unitId=unitId<br>
   * location=Destination slot<br>
   * vesselId=Vessel Id<br>
   * poolName=Pool name<br>
   * cheId=Name of the CHE
   * @return JSON , <code>Plan to load unit to vessel created</code> if success, else<br>
   *               <code>Load unit failed</code>
   * @Example
   * Table invoked by SPARCS : inv_wi, inv_unit <br>
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="PlanUnitLoad"/&gt;<br>
   * &lt;parameter id="unitId" value="APLU8362946"/&gt;<br>
   * &lt;parameter id="location" value="161001.1"/&gt;<br>
   * &lt;parameter id="powName" value="Pow01" /&gt;<br>
   * &lt;parameter id="vesselId" value="VESSEL01"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String PlanUnitLoad(Map inParameters) {
    assert inParameters.size() >= 5, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 5 parameters:
                                        <parameter id="command" value="PlanUnitLoad" />
                                        <parameter id="unitId" value="<Unit Id>" />
                                        <parameter id="location" value="<Destination slot>" />
										<parameter id="vesselId" value="<Vessel Id>" />
										<parameter id="powName" value="<Pow name>" />
										<parameter id="isPlanAsTwin" value="<twin containers>" />'''

    String inUnitId = _testCommandHelper.checkParameter('unitId', inParameters);
    String inDestinationSlot = _testCommandHelper.checkParameter('location', inParameters);
    String inVesselId = _testCommandHelper.checkParameter('vesselId', inParameters);
    String inPowName = _testCommandHelper.checkParameter('powName', inParameters);
    boolean isTwin = false
    String inIsPlanAsTwin = ''
    if (inParameters.containsKey('isPlanAsTwin')) {
      inIsPlanAsTwin = inParameters.get('isPlanAsTwin')
    }

    //Find yard model
    Yard yard = ContextHelper.getThreadYard();
    YardBinModel yardModel = (YardBinModel) HibernateApi.getInstance().downcast(yard.getYrdBinModel(), YardBinModel.class);
    WorkInstruction workInstruction;
    PointOfWork pointOfWork = ((PointOfWork) HibernateApi.getInstance().getUniqueEntityByDomainQuery(QueryUtils.createDomainQuery(ArgoEntity.POINT_OF_WORK)
            .addDqPredicate(PredicateFactory.eq(ArgoField.POINTOFWORK_NAME, inPowName))));
    //Find the ufv
    UnitFinder finder = (UnitFinder) Roastery.getBean(UnitFinder.BEAN_ID);
    SearchResults results = finder.findUfvByDigits(inUnitId, false, false);
    assert results.getFoundCount() == 1, 'More than one equipment found for id ' + inUnitId;
    UnitFacilityVisit ufv = (UnitFacilityVisit) HibernateApi.getInstance().load(UnitFacilityVisit.class, results.getFoundPrimaryKey());
    UnitYardVisit uyv = ufv.getUyvForYard(yard);
    if (!inIsPlanAsTwin.isEmpty() && inIsPlanAsTwin.equalsIgnoreCase('Yes')) {
      isTwin = true
    };
    try {
      workInstruction = ControlTestUtils.planUnitLoad(uyv, inVesselId, inDestinationSlot, pointOfWork, isTwin);
      if (workInstruction != null) {
        returnString = "Plan to load unit to vessel created"
      } else {
        returnString = "Plan to load unit to vessel failed, please check vesselId and its location is valid"
      };
    } catch (Exception ex) {
      returnString "Load unit failed " + ex;
    }
    builder {
      actual_result returnString;
      if (workInstruction != null) {
        data('WiGkey': workInstruction.wiGkey)
      }
    }
    LOGGER.debug('PlanUnitLoad:' + returnString + 'for the unit :' + inUnitId)
    return builder;
  }

  /**
   * Plans to move unit from vessel to yard and return the work instruction for this plan.
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * unitId=Unit Id<br>
   * location=Destination slot<br>
   * poolName=Pool name<br>
   * cheId=NAme of the CHE
   * @return JSON , <code>Plan to discharge unit from vessel created</code> if success, else<br> <code>Failed to discharge unit</code>
   * @Example
   * Table invoked by SPARCS : inv_wi, inv_unit  <br>
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="PlanUnitDischarge" /&gt;<br>
   * &lt;parameter id="unitId" value="APLU8362946" /&gt;<br>
   * &lt;parameter id="location" value="161001.1" /&gt;<br>
   * &lt;parameter id="powName" value="Pool01" /&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String PlanUnitDischarge(Map inParameters) {
    assert inParameters.size() >= 4, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 4 parameters:
                                        <parameter id="command" value="PlanUnitDischarge" />
                                        <parameter id="unitId" value="<Unit Id>" />
                                        <parameter id="location" value="<Destination slot>" />
                                        <parameter id="powName" value="<Pow name>" />
                                        <parameter id="isPlanAsTwin" value="<twin containers>" />'''

    String inUnitId = _testCommandHelper.checkParameter('unitId', inParameters);
    String inDestinationSlot = _testCommandHelper.checkParameter('location', inParameters);
    String inPowName = _testCommandHelper.checkParameter('powName', inParameters);
    boolean isTwin = false
    String inIsPlanAsTwin = ''
    if (inParameters.containsKey('isPlanAsTwin')) {
      inIsPlanAsTwin = inParameters.get('isPlanAsTwin')
    }
    String wiGkey = null;
    try {
      //Find yard
      Yard yard = ContextHelper.getThreadYard();
      YardBinModel yardModel = (YardBinModel) HibernateApi.getInstance().downcast(yard.getYrdBinModel(), YardBinModel.class);

      //Find the ufv
      UnitFinder finder = (UnitFinder) Roastery.getBean(UnitFinder.BEAN_ID);
      SearchResults results = finder.findUfvByDigits(inUnitId, false, false);
      assert results.getFoundCount() == 1, 'More than one equipment found for id ' + inUnitId;
      UnitFacilityVisit ufv = (UnitFacilityVisit) HibernateApi.getInstance().load(UnitFacilityVisit.class, results.getFoundPrimaryKey());
      UnitYardVisit uyv = ufv.getUyvForYard(yard);
      PointOfWork pointOfWork = ((PointOfWork) HibernateApi.getInstance().getUniqueEntityByDomainQuery(QueryUtils.createDomainQuery(ArgoEntity.POINT_OF_WORK)
              .addDqPredicate(PredicateFactory.eq(ArgoField.POINTOFWORK_NAME, inPowName))));
      if (!inIsPlanAsTwin.isEmpty() && inIsPlanAsTwin.equalsIgnoreCase('Yes')) {
        isTwin = true
      };
      WorkInstruction wi = ControlTestUtils.planUnitDsch(uyv, inDestinationSlot, pointOfWork, isTwin);
      if (wi != null) {
        wiGkey = wi.getWiGkey().toString();
        returnString = "Plan to discharge unit from vessel created";
      } else {
        returnString = "Plan to discharge unit from vessel failed, please check unit and its location is valid"
      };

    } catch (BizViolation inBizViolation) {
      returnString = "Failed to discharge unit " + inBizViolation.message;
    } catch (BizFailure inBizFailure) {
      returnString = "Failed to discharge unit " + inBizFailure.message;
    } catch (Exception ex) {
      returnString = "Failed to discharge unit " + ex.stackTrace;
    }
    builder {
      actual_result returnString
      data('WiGkey': wiGkey)
    }
    LOGGER.debug('PlanUnitDischarge:' + returnString + 'for the unit :' + inUnitId)
    return builder;
  }

  //Method to get the qc che for given che name and pool name
  private Che getQcChe(String poolName, String cheId) {
    ChePool chePool = ControlTestUtils.findOrCreateChePool(poolName);
    PointOfWork qcPow = ControlTestUtils.findOrCreatePow(cheId, chePool);
    Che qcChe = ControlTestUtils.findOrCreateChe(cheId, CheKindEnum.QC, chePool, qcPow);
    che.setCheIsOcrDataBeingAccepted(true);
    qcChe.setCheStatusEnum(CheStatusEnum.WORKING);
    return qcChe;
  }

  /**
   * Returns GKey for requested ASC which is required as input parameter for 'Run AscScheduler'
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=GetAscBlockGkey<br>
   * @return JSON ,
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="GetAscBlockGkey"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String GetAscBlockGkey(Map inParameters) {
    assert inParameters.size() == 2, '''Must supply 2 parameters:
                                        <parameter id="command" value="GetAscBlockGkey" />
                                        <parameter id="blockName" value="<1>" />'''

    String inBlockName = _testCommandHelper.checkParameter('blockName', inParameters);
    Long gkey = null;
    try {
      Yard thisYard = ContextHelper.getThreadYard();
      assert thisYard != null;

      YardBinModel yardModel =
        (YardBinModel) HibernateApi.getInstance().downcast(thisYard.getYrdBinModel(), YardBinModel.class);
      gkey = StackBlock.findStackBlock(yardModel, inBlockName).getAbnGkey();
      returnString = 'ASC block gkey obtained ';
    } catch (Exception ex) {
      returnString 'Failed to retrieve ASC block gKey ' + ex;
    }
    builder {
      actual_result returnString;
      if (gkey != null) {
        data('gkey': gkey)
      }
    }
    return builder;
  }

  /**
   * Returns the MoveList for specific ASC block and verify
   * After execution of 'Run AscScheduler', the MoveList will be generated in DB.
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=GetMoveList<br>
   * @return JSON ,
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="GetMoveList"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String GetMoveList(Map inParameters) {
    assert inParameters.size() >= 2, '''Must supply 2 parameters:
                                 <parameter id="command" value="GetMoveList" />
								 <parameter id="blockName" value="<blockName>" />
                                 <parameter id="option" value="optional" />'''

    String inBlockName = _testCommandHelper.checkParameter('blockName', inParameters);
    String inOption = "";
    if (inParameters.containsKey('option')) {
      inOption = inParameters.get('option');
    }
    if (inOption.isEmpty()) {
      inOption = "1";
    }
    UserContext userContext = ContextHelper.getThreadUserContext();
    StackBlock ascBlock = AscTestUtils.findBlockByName(inBlockName);
    Serializable blockGkey = ascBlock.getAbnGkey();

    Collection<AscMove> retmoves = AscTestUtils.findAscMovesInDatabase(blockGkey);
    StringBuilder result = new StringBuilder("");
    for (AscMove m : retmoves) {
      if (inOption.toInteger() == 1) {  //return default movelist
        result.append("moveType:" + m.ascmoveMoveType).append(";");
        result.append("purpose:" + m.ascmovePurpose).append(";");
        result.append("craneGkey:" + m.ascmoveCraneGkey).append(";");
      } else if (inOption.toInteger() == 2) { //exclude cranegkey
        result.append("moveType:" + m.ascmoveMoveType).append(";");
        result.append("purpose:" + m.ascmovePurpose).append(";");
      } else if (inOption.toInteger() == 3) {  //return movelist count
        //just return movesize.
      }
    }
    result.append("moveSize:" + retmoves.size());
    returnString = result;
    builder {
      actual_result returnString;
    }
    return builder;
  }

  /**
   * Plans to deliver unit from yard to truck and creates a Work Instruction for it.
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=PlanUnitDelivery<br>
   * unitId=Id of the unit
   * location=yard location from where the unit needs to be moved
   * truckVisitId=Id of the truck visit to where the unit needs to be moved
   * @return JSON ,
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="PlanUnitDelivery"/&gt;<br>
   * &lt;parameter id="unitId" value="TEST00000001"/&gt;<br>
   * &lt;parameter id="location" value="166601.1"/&gt;<br>
   * &lt;parameter id="truckVisitId" value="TRK01"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String PlanUnitDelivery(Map inParameters) {
    assert inParameters.size() == 4, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 4 parameters:
                                        <parameter id="command" value="PlanUnitDelivery" />
                                        <parameter id="unitId" value="<Unit Id>" />
                                        <parameter id="location" value="<Destination slot>" />
                                        <parameter id="truckVisitId" value="<Truck Visit Id>" />'''

    String inUnitId = _testCommandHelper.checkParameter('unitId', inParameters);
    String location = _testCommandHelper.checkParameter('location', inParameters);
    String truckVisitId = _testCommandHelper.checkParameter('truckVisitId', inParameters);

    //Find yard model
    Yard yard = ContextHelper.getThreadYard();

    //Find the ufv
    UnitFinder finder = (UnitFinder) Roastery.getBean(UnitFinder.BEAN_ID);
    SearchResults results = finder.findUfvByDigits(inUnitId, false, false);
    assert results.getFoundCount() == 1, 'More than one equipment found for id ' + inUnitId;
    UnitFacilityVisit ufv = (UnitFacilityVisit) HibernateApi.getInstance().load(UnitFacilityVisit.class, results.getFoundPrimaryKey());
    UnitYardVisit uyv = ufv.getUyvForYard(yard);
    String wiGkey = null;
    try {
      CarrierVisit truckVisit = CarrierVisit.findCarrierVisit(ContextHelper.getThreadFacility(), com.navis.argo.business.atoms.LocTypeEnum.TRUCK, truckVisitId);
      if (location != null && (location.isEmpty() || location.isAllWhitespace())) {
        location = null;
      }
      LocPosition truckPosition = LocPosition.createTruckPosition(truckVisit, location, null);
      WorkInstruction wi = ControlTestUtils.planUnitDelivery(uyv, truckPosition);
      wiGkey = wi.getWiGkey().toString();
    } catch (BizViolation inBizViolation) {
      returnString = "Failed to discharge unit " + inBizViolation.message;
    } catch (BizFailure inBizFailure) {
      returnString = "Failed to discharge unit " + inBizFailure.message;
    } catch (Exception ex) {
      returnString = "Unit delivery failed " + ex;
    }
    returnString = "Plan to deliver unit to truck created";
    builder {
      actual_result returnString;
      data('WiGkey': wiGkey)
    }
    LOGGER.debug('PlanUnitDelivery:' + returnString + ' for unit : ' + inUnitId)
    return builder;

  }

  /**
   * Plans to receive unit from Truck to Yard and creates a Work Instruction for it.
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=PlanUnitReceival<br>
   * unitId=Id of the unit
   * location=location in the yard
   * @return JSON ,
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="PlanUnitReceival"/&gt;<br>
   * &lt;parameter id="unitId" value="TEST00000001"/&gt;<br>
   * &lt;parameter id="location" value="161001.1"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String PlanUnitReceival(Map inParameters) {
    assert inParameters.size() == 3, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 3 parameters:
                                        <parameter id="command" value="PlanUnitReceival" />
                                        <parameter id="unitId" value="<Unit Id>" />
                                        <parameter id="location" value="<Destination slot>" />'''

    String inUnitId = _testCommandHelper.checkParameter('unitId', inParameters);
    String inDestinationSlot = _testCommandHelper.checkParameter('location', inParameters);

    //Find yard model
    Yard yard = ContextHelper.getThreadYard();

    //Find the ufv
    UnitFinder finder = (UnitFinder) Roastery.getBean(UnitFinder.BEAN_ID);
    SearchResults results = finder.findUfvByDigits(inUnitId, false, false);
    assert results.getFoundCount() == 1, 'More than one equipment found for id ' + inUnitId;
    UnitFacilityVisit ufv = (UnitFacilityVisit) HibernateApi.getInstance().load(UnitFacilityVisit.class, results.getFoundPrimaryKey());
    UnitYardVisit uyv = ufv.getUyvForYard(yard);
    String wiGkey = null;
    try {
      WorkInstruction wi = ControlTestUtils.planUnitReceival(uyv, inDestinationSlot);
      wiGkey = wi.getWiGkey().toString();
    } catch (Exception ex) {
      returnString "Receive unit failed " + ex;
    }
    returnString = "Plan to Receive unit to vessel created";
    builder {
      actual_result returnString;
      data('WiGkey': wiGkey)
    }
    LOGGER.debug('PlanUnitReceival:' + returnString + ' for unit : ' + inUnitId)
    return builder;
  }

  /**
   * Sets the stack status for the whole block or the given part of the block
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=ReportStackStatus<br>
   * blockName=Name of the block whose details needs to be reported<br>
   * startRow=Row position in Internal format of the block
   * endRow=Row position in Internal format of the block
   * startCol=Col position in Internal format of the block
   * endCol=Col position in Internal format of the block
   * status=MEN_WORKING,TEMP_BLOCKED,ROAD_WAY,RACK,TEMPBLOCKED_MENWORKING, ROADWAY_MENWORKING, RACK_MENWORKING, RACK_TEMPBLOCKED, RACK_ROADWAY
   * @return JSON , <code>Stack Status Value is set</code> if success
   *                <code>Stack Status Value not set : StackStatus list is null</code> if given block itself is not available
   *                <code>Stack Status Value not set</code> if any other exception occurs
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="SetStackStatus" /&gt;<br>
   * &lt;parameter id="blockName" value="BL01" /&gt;<br>
   * &lt;parameter id="startRow" value="A" /&gt;<br>
   * &lt;parameter id="endRow" value="F" /&gt;<br>
   * &lt;parameter id="startCol" value="01" /&gt;<br>
   * &lt;parameter id="endCol" value="05" /&gt;<br>
   * &lt;parameter id="status" value="MEN_WORKING,TEMP_BLOCKED" /&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   *
   */
  public String SetStackStatus(Map inParameters) {
    assert inParameters.size() == 7, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 7 parameters:
                                        <parameter id="command" value="SetStackStatus" />
                                        <parameter id="blockName" value="block name" />
                                        <parameter id="startRow" value="<Start Row in the block>" />
                                        <parameter id="startCol" value="<Start Col in the block>" />
                                        <parameter id="endRow" value="<Last Row in the block>" />
                                        <parameter id="endCol" value="<Last Col in the block>" />
                                        <parameter id="status" value="<Status> =MEN_WORKING" />'''
    String inBlockName = _testCommandHelper.checkParameter('blockName', inParameters);
    String inStartRow = _testCommandHelper.checkParameter('startRow', inParameters);
    String inEndRow = _testCommandHelper.checkParameter('endRow', inParameters);
    String inStartCol = _testCommandHelper.checkParameter('startCol', inParameters);
    String inEndCol = _testCommandHelper.checkParameter('endCol', inParameters);
    String inStatus = _testCommandHelper.checkParameter('status', inParameters);

    Yard thisYard = ContextHelper.getThreadYard();
    assert thisYard != null;
    YardBinModel yardModel = (YardBinModel) HibernateApi.getInstance().downcast(thisYard.getYrdBinModel(), YardBinModel.class);
    StackBlock stackBlock = StackBlock.findStackBlock(yardModel, inBlockName);
    try {
      List<StackStatus> statusList = findOrCreateStackStatus(stackBlock, inStartRow, inEndRow,
              inStartCol, inEndCol);
      if (statusList != null) {
        def preserveStatus = '', statusObtained = '';
        def statusArray = inStatus.split(',')
        statusList.each {
          for (int index = 0; index < statusArray.size(); index++) {
            inStatus = statusArray[index]
            // If the previous status chars are null or already cleared, then set the new status only
            def stackStatus = it.getStackstatusStatusChars() //TIER_LIMIT_CHAR_INDEX = 1;
            if (stackStatus == null) {
              stackStatus = _testCommandHelper.CLEAR
            }
            println 'stackStatus:' + stackStatus
            statusObtained = _testCommandHelper.getStackStatusValue(inStatus, stackStatus)
            if (!statusObtained.isEmpty()) {
              it.setStackstatusStatusChars(statusObtained);
              returnString = "Stack Status Value is set"; //setting stack status value
            } else {
              returnString = 'Input format is wrong, stack status not set'
            }
          }
        }
      } else {
        returnString = "Stack Status Value not set : StackStatus list is null"
      }
    } catch (Exception ex) {
      returnString = "Stack Status Value not set " + ex;
    }
    builder {
      actual_result returnString;
    }
    LOGGER.debug('SetStackStatus:' + returnString + 'for block : ' + inBlockName)
    return builder
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
  protected List<StackStatus> findOrCreateStackStatus(StackBlock ascBlock, String inStartRow, String inEndRow, String inStartColumn, String inEndColumn) {
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

  /**
   * Fetch the next work instruction for any given work instruction
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=NextWorkInstruction<br>
   * wiGkey=Work instruction gkey for which next work instruction needs to be fetched
   *
   * @return JSON - If there is a next WI, will return WiGkey
   * else - returns a message that Next WI was not found
   *
   * @Example
   * <groovy class-location=""classpath"" class-directory=""test/extension/groovy"" class-name=""TestCommand"">
   *  <parameters>
   *    <parameter id=""command"" value=""NextWorkInstruction""/>
   *    <parameter id=""wiGKey"" value=""1""/>
   *  </parameters>
   * </groovy>
   */
  public String NextWorkInstruction(Map inParameters) {
    assert inParameters.size() == 2, '''Must supply 2 parameters:
                                        <parameter id="command" value="NextWorkInstruction" />
                                        <parameter id="wiGKey" value="<1>" />'''

    String wiGKey = _testCommandHelper.checkParameter('wiGKey', inParameters);
    WorkInstruction nextWorkInstruction = null;
    try {
      WorkInstruction wi1 = WorkInstruction.hydrate(wiGKey);
      nextWorkInstruction = wi1.getNextWorkInstruction();
      if (null != nextWorkInstruction) {
        returnString = 'Next Work instruction fetched ';
      } else {
        returnString = 'Next WorkInstruction not found';
      }
    } catch (Exception inEx) {
      returnString = 'Next Work Instruction not fetched' + inEx;
    }
    finally {
      builder {
        actual_result returnString;
        if (nextWorkInstruction != null) {
          data('WiGkey': nextWorkInstruction.wiGkey)
        }
      }
    }
    LOGGER.debug('NextWorkInstruction : ' + returnString + 'for wiGKey : ' + wiGKey)
    return builder;
  }

  /**
   * Fetch the Get work instruction for any given Unit and move number
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=GetWorkInstruction<br>
   * unit=Unit id
   * move number = move number in the work queue
   *
   * @return JSON - If there is a WI, will return WiGkey
   * else - returns a message that a WI was not found
   *
   * @Example
   * <groovy class-location=""classpath"" class-directory=""test/extension/groovy"" class-name=""TestCommand"">
   *  <parameters>
   *    <parameter id=""command"" value=""GetWorkInstruction""/>
   *    <parameter id="UnitId" value="<11234>" />
   *    <parameter id="MoveNumber" value="<2>" />
   *  </parameters>
   * </groovy>
   */
  public String GetWorkInstruction(Map inParameters) {
    assert inParameters.size() == 3, '''Must supply 3 parameters:
                                        <parameter id="command" value="GetWorkInstruction" />
                                        <parameter id="unitId" value="<11234>" />
                                        <parameter id="moveNumber" value="2" />'''

    String unitId = _testCommandHelper.checkParameter('unitId', inParameters);
    String moveNumber = _testCommandHelper.checkParameter('moveNumber', inParameters);
    WorkInstruction wi = null;
    try {
      wi = InventoryTestUtils.getWorkInstructionByUnitAndMoveNum(unitId, Double.valueOf(moveNumber));
      if (null != wi) {
        returnString = 'Work instruction fetched ';
      } else {
        returnString = 'WorkInstruction not found';
      }
    } catch (Exception inEx) {
      returnString = 'Work Instruction not fetched ' + inEx;
    }
    finally {
      builder {
        actual_result returnString;
        if (wi != null) {
          data('WiGkey': wi.wiGkey)
        }
      }
    }
    LOGGER.debug('GetWorkInstruction : ' + returnString + 'for unit : ' + unitId)
    return builder;
  }

  /**
   * Fetch the Get work assignment for any given work instruction
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=GetWorkAssignment<br>
   * wiGkey=Work instruction gkey
   *
   * @return JSON - If there is a WA, will return Work assignment Gkey
   * else - returns a message that a W.A was not found
   *
   * @Example
   * <groovy class-location=""classpath"" class-directory=""test/extension/groovy"" class-name=""TestCommand"">
   *  <parameters>
   *    <parameter id=""command"" value=""GetWorkAssignment""/>
   *    <parameter id="wiGkey" value="<11234>" />
   *  </parameters>
   * </groovy>
   */
  public String GetWorkAssignment(Map inParameters) {
    assert inParameters.size() >= 2, '''Must supply 3 parameters:
                                        <parameter id="command" value="GetWorkAssignment" />
                                        <parameter id="cheId" value="AGV510" />
                                        <parameter id="wiGkey" value="<11234>"
                                        <parameter id="cheKind" value="<ASC,AGV>" />'''

    String wiGkey = '', cheKind = '', cheShortName = ''
    if (inParameters.containsKey('wiGkey')) {
      wiGkey = inParameters.get('wiGkey')
    };
    if (inParameters.containsKey('cheKind')) {
      cheKind = inParameters.get('cheKind')
    };
    if (inParameters.containsKey('cheId')) {
      cheShortName = inParameters.get('cheId', inParameters)
    };
    WorkAssignment workAssignment = null;
    try {
      Che che;
      if (!cheShortName.isEmpty()) {  //taking cheId as optional param. This will be helpful when there are multiple CHEs and multiple jobs and scheduler is invoked to pre-dispatch the jobs
        che = ((Che) HibernateApi.getInstance().getUniqueEntityByDomainQuery(QueryUtils.createDomainQuery(ArgoEntity.CHE)
                .addDqPredicate(PredicateFactory.eq(ArgoField.CHE_SHORT_NAME, cheShortName))));
        if (che != null) {
          if (cheKind.isEmpty()) {
            cheKind = che.cheKindEnum.key
          }
        }
      }
      if (!wiGkey.isEmpty()) { //in case of parking orders, there will be no WI
        WorkInstruction wi = WorkInstruction.hydrate(wiGkey);
        if (!cheKind.isEmpty()) {
          if (cheKind.equals("AGV")) {
            workAssignment = wi.getWiItvWorkAssignment()
          } else {
            workAssignment = wi.getWiCheWorkAssignment()
          };
        } else {
          returnString = 'WA not fetched : Che or CheKind should be given in the input'
        }
        if (null != workAssignment) {
          returnString = 'WorkAssignment  fetched';
        } else {
          returnString = 'WorkAssignment not found';
        }
      } else {  //to get WA for parking orders (ITV_REPOSITION)
        if (che != null) {
          def cheGKey = che.cheGkey
          DomainQuery dq = QueryUtils.createDomainQuery(ArgoEntity.WORK_ASSIGNMENT)
          dq.addDqPredicate(PredicateFactory.eq(ArgoField.WORKASSIGNMENT_CHE, che.cheGkey))
          dq.addDqPredicate(PredicateFactory.eq(ArgoField.WORKASSIGNMENT_JOB_TYPE, CheInstructionTypeEnum.AGV_REPOSITION))
          workAssignment = (WorkAssignment) HibernateApi.getInstance().getUniqueEntityByDomainQuery(dq);
          if (null != workAssignment) {
            returnString = 'WorkAssignment  fetched';
          } else {
            returnString = 'WorkAssignment not found';
          }
        }
      }
    } catch (Exception inEx) {
      returnString = 'WorkAssignment not fetched ' + inEx;
    }
    finally {
      builder {
        actual_result returnString;
        if (workAssignment != null) {
          data('WaGkey': workAssignment.workassignmentGkey)
        }
      }
    }
    LOGGER.debug('GetWorkAssignment : ' + returnString + 'for wiGKey : ' + wiGkey)
    return builder;
  }

  /**
   * Gets the current job on which the given CHE is working
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=GetCurrentJobForChe<br>
   * cheId=Che Name for which current job needs to be known
   *
   * @return JSON - If Che is working on a job, will return the current job
   * else - returns a message that current job was not found
   *
   * @Example
   * <groovy class-location=""classpath"" class-directory=""test/extension/groovy"" class-name=""TestCommand"">
   *  <parameters>
   *    <parameter id="command" value="GetCurrentJobForChe"/>
   *    <parameter id="cheId" value="1"/>
   *  </parameters>
   * </groovy>
   */
  public String GetCurrentJobForChe(Map inParameters) {
    assert inParameters.size() == 2, '''Must supply 2 parameters:
                                        <parameter id="command" value="GetCurrentJobForChe" />
                                        <parameter id="cheId" value="<1>" />'''

    String inCheName = _testCommandHelper.checkParameter('cheId', inParameters);
    Long wiGKey = 0L;
    try {
      Che che = Che.findCheByShortName(inCheName, ContextHelper.getThreadYard());
      WorkAssignment wa = AscTestUtils.findCurrentWorkAssignmentForChe(che);
      WorkInstruction workInstruction = null;
      if (wa != null) {
        DomainQuery dq
        if(che.cheKindEnum.toString().contains("ASC")) {
        dq = QueryUtils.createDomainQuery(MovesEntity.WORK_INSTRUCTION);
        dq.addDqPredicate(PredicateFactory.eq(MovesField.WI_CHE_WORK_ASSIGNMENT, wa.getWorkassignmentGkey()));
        } else if(che.cheKindEnum.toString().contains("AGV")) {
          dq = QueryUtils.createDomainQuery(MovesEntity.WORK_INSTRUCTION);
          dq.addDqPredicate(PredicateFactory.eq(MovesField.WI_ITV_WORK_ASSIGNMENT, wa.getWorkassignmentGkey()));
        }
        workInstruction = (WorkInstruction) HibernateApi.getInstance().getUniqueEntityByDomainQuery(dq);
        if (workInstruction != null) {
          wiGKey = workInstruction.getWiGkey();
          returnString = 'Current job obtained'
        } else {
          returnString 'Work Instruction is null';
        }
      } else {
        returnString 'Work Assignment is null';
      }
    } catch (BizFailure inBizFailure) {
      returnString = inBizFailure.message;
    } catch (Exception inEx) {
      returnString = 'Current job not obtained ' + inEx.printStackTrace();
    }
    finally {
      builder {
        actual_result returnString;
        data('WiGkey': wiGKey)
      }
    }
    return builder;
  }

  /**
   * Fetch the Work Instruction for given truck transaction gkey
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=FindGateMovesForTruckTransaction<br>
   * tranGkey=Truck Transaction gkey
   *
   * @return JSON - If there is a WI, will return Work Instruction Gkey
   * else - returns a message that a W.I was not found
   *
   * @Example
   * <groovy class-location=""classpath"" class-directory=""test/extension/groovy"" class-name=""TestCommand"">
   *  <parameters>
   *    <parameter id=""command"" value=""FindGateMovesForTruckTransaction""/>
   *    <parameter id="tranGkey" value="<11234>" />
   *  </parameters>
   * </groovy>
   */
  public String FindGateMovesForTruckTransaction(Map inParameters) {
    assert inParameters.size() == 2, '''Must supply 2 parameters:
                                        <parameter id="command" value="FindGateMovesForTruckTransaction" />
                                        <parameter id="tranGkey" value="<tranGkey>" />'''

    String inTranGkey = _testCommandHelper.checkParameter('tranGkey', inParameters);
    List wis = null; Long wiGkey;
    WorkInstruction wi;
    try {
      wis = WorkInstruction.findGateMovesForTruckTransaction(Long.parseLong(inTranGkey));
      if (!wis.isEmpty()) {
        wi = wis.get(0);
        wiGkey = wi.getWiGkey();
        returnString = 'Work instruction for truck transaction fetched ';
      } else {
        returnString = 'WorkInstruction for truck transaction not found';
      }
    } catch (Exception inEx) {
      returnString = 'Work Instruction not fetched ' + inEx;
    }
    builder {
      actual_result returnString;
      if (!wis.isEmpty()) {
        data('WiGKey': wiGkey)
      }
    }
    LOGGER.debug('FindGateMovesForTruckTransaction:' + returnString)
    return builder;
  }

  /**
   * Plans to discharge unit from rail to yard and creates a Work Instruction for it.
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=PlanUnitRailDisch<br>
   * unitId=Id of the unit
   * location=yard location to where the unit needs to be discharged
   * powName=Name of the POW
   * @return JSON ,
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="PlanUnitRailDisch"/&gt;<br>
   * &lt;parameter id="unitId" value="TEST00000001"/&gt;<br>
   * &lt;parameter id="location" value="161001.1"/&gt;<br>
   * &lt;parameter id="powName" value="POW01"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String PlanUnitRailDisch(Map inParameters) {
    assert inParameters.size() >= 3, '''Must supply 3 parameters:
                                            <parameter id="command" value="PlanUnitRailDisch" />
                                            <parameter id="unitId" value="<Equipment Id>" />
                                            <parameter id="location" value="<destination slot>" />
                                            <parameter id="powName" value="<point of work>"'''
    String inUnitId = _testCommandHelper.checkParameter('unitId', inParameters);
    String inLocation = _testCommandHelper.checkParameter('location', inParameters);
    String inPowName = _testCommandHelper.checkParameter('powName', inParameters);

    try {
      Yard yard = ContextHelper.getThreadYard();
      YardBinModel yardModel = (YardBinModel) HibernateApi.getInstance().downcast(yard.getYrdBinModel(), YardBinModel.class);
      WorkInstruction workInstruction;
      PointOfWork pointOfWork = ((PointOfWork) HibernateApi.getInstance().getUniqueEntityByDomainQuery(QueryUtils.createDomainQuery(ArgoEntity.POINT_OF_WORK)
              .addDqPredicate(PredicateFactory.eq(ArgoField.POINTOFWORK_NAME, inPowName))));
      //Find the ufv
      UnitFinder finder = (UnitFinder) Roastery.getBean(UnitFinder.BEAN_ID);
      SearchResults results = finder.findUfvByDigits(inUnitId, false, false);
      if (results.getFoundCount() >= 1) {
        UnitFacilityVisit ufv = (UnitFacilityVisit) HibernateApi.getInstance().load(UnitFacilityVisit.class, results.getFoundPrimaryKey());
        UnitYardVisit uyv = ufv.getUyvForYard(yard);
        workInstruction = com.navis.control.business.ControlRailTestUtils.planUnitRailDischarge(uyv, inLocation, pointOfWork)
        if (workInstruction != null) {
          returnString = 'Plan to discharge unit from rail created'
        } else {
          returnString = 'Plan to discharge unit from rail failed - No WI created'
        }
      } else {
        returnString = 'PlanUnitDischarge failed - given unit not found'
      }
    } catch (Exception ex) {
      returnString = 'Plan to discharge unit from rail failed ' + ex
    }
    builder {
      actual_result returnString
    }
    LOGGER.debug('PlanUnitRailDisch' + returnString)
    return builder;
  }

  /**
   * Plans to load unit from yard to rail and creates a Work Instruction for it.
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=PlanUnitRailLoad<br>
   * unitId=Id of the unit<br>
   * location=yard location from where the unit needs to be moved<br>
   * powName=Name of the POW<br>
   * railCarCvId=Rail Visit Id to load the unit to that position
   * @return JSON ,
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="PlanUnitRailLoad"/&gt;<br>
   * &lt;parameter id="unitId" value="TEST00000001"/&gt;<br>
   * &lt;parameter id="location" value="161001.1"/&gt;<br>
   * &lt;parameter id="powName" value="POW01"/&gt;<br>
   * &lt;parameter id="railCarCvId" value="RL01"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String PlanUnitRailLoad(Map inParameters) {
    assert inParameters.size() >= 3, '''Must supply 3 parameters:
                                            <parameter id="command" value="PlanUnitRailLoad" />
                                            <parameter id="unitId" value="<Equipment Id>" />
                                            <parameter id="location" value="<destination slot Vessel slot in case of discharge and rail car position in case of load>" />
                                            <parameter id="powName" value="<point of work>" />
                                            <parameter id="railCvGKey" value="<GKey of rail car visit - returned in InitUnitInRailCar>" />
                                            <parameter id="railCarCvId" value="<Name of the rail car visit>" />'''
    String inUnitId = _testCommandHelper.checkParameter('unitId', inParameters);
    String inLocation = inParameters.get('location');
    String inPowName = _testCommandHelper.checkParameter('powName', inParameters);
    String inRailCvGKey = inParameters.get('railCvGKey')
    String inRailCarCvId = inParameters.get('railCarCvId')

    try {
      Yard yard = ContextHelper.getThreadYard();
      YardBinModel yardModel = (YardBinModel) HibernateApi.getInstance().downcast(yard.getYrdBinModel(), YardBinModel.class);
      WorkInstruction workInstruction;
      PointOfWork pointOfWork = ((PointOfWork) HibernateApi.getInstance().getUniqueEntityByDomainQuery(QueryUtils.createDomainQuery(ArgoEntity.POINT_OF_WORK)
              .addDqPredicate(PredicateFactory.eq(ArgoField.POINTOFWORK_NAME, inPowName))));
      //Find the ufv
      UnitFinder finder = (UnitFinder) Roastery.getBean(UnitFinder.BEAN_ID);
      SearchResults results = finder.findUfvByDigits(inUnitId, false, false);
      if (results.getFoundCount() >= 1) {
        UnitFacilityVisit ufv = (UnitFacilityVisit) HibernateApi.getInstance().load(UnitFacilityVisit.class, results.getFoundPrimaryKey());
        UnitYardVisit uyv = ufv.getUyvForYard(yard);
        RailcarVisit railcarVisit = RailcarVisit.hydrate(inRailCvGKey);
        LocPosition railcarPos = LocPosition.createRailcarPosition(railcarVisit, inLocation, null);
        workInstruction = com.navis.control.business.ControlRailTestUtils.planUnitRailLoad(uyv, railcarPos, pointOfWork)
        if (workInstruction != null) {
          returnString = 'Plan to load unit to rail created'
        } else {
          returnString = 'Plan to load unit to rail failed - No WI created'
        }
      } else {
        returnString = 'Plan to load unit to rail failed - given unit not found'
      }
    } catch (Exception ex) {
      returnString = 'Plan to load unit to rail failed ' + ex
    }
    builder {
      actual_result returnString
    }
    LOGGER.debug('PlanUnitRailLoad' + returnString)
    return builder;
  }
}
