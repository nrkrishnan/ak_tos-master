package test.extension.groovy

import com.navis.argo.ArgoEntity
import com.navis.argo.ArgoField
import com.navis.argo.ContextHelper
import com.navis.argo.business.api.ArgoUtils
import com.navis.argo.business.api.IImpediment
import com.navis.argo.business.api.ServicesManager
import com.navis.argo.business.atoms.CarrierVisitPhaseEnum
import com.navis.argo.business.atoms.FlagPurposeEnum
import com.navis.argo.business.atoms.FlagStatusEnum
import com.navis.argo.business.atoms.LocTypeEnum
import com.navis.argo.business.atoms.ServiceRuleTypeRoleEnum
import com.navis.argo.business.model.CarrierVisit
import com.navis.argo.business.model.Yard
import com.navis.argo.business.reference.Equipment
import com.navis.argo.business.xps.model.Che
import com.navis.argo.business.xps.model.StackStatus
import com.navis.argo.business.xps.model.WorkAssignment
import com.navis.control.business.asc.AscMove
import com.navis.control.business.asc.AscTestUtils
import com.navis.framework.business.Roastery
import com.navis.framework.persistence.DatabaseHelper
import com.navis.framework.persistence.HibernateApi
import com.navis.framework.portal.QueryUtils
import com.navis.framework.portal.UserContext
import com.navis.framework.portal.query.DomainQuery
import com.navis.framework.portal.query.PredicateFactory
import com.navis.inventory.InventoryEntity
import com.navis.inventory.InventoryField
import com.navis.inventory.MovesEntity
import com.navis.inventory.MovesField
import com.navis.inventory.ServicesMovesEntity
import com.navis.inventory.ServicesMovesField
import com.navis.inventory.business.units.EquipmentState
import com.navis.inventory.business.units.InventoryTestUtils
import com.navis.inventory.business.units.Unit
import com.navis.inventory.business.units.UnitEquipment
import com.navis.road.RoadEntity
import com.navis.road.RoadField
import com.navis.road.business.atoms.TruckVisitStatusEnum
import com.navis.road.business.model.TruckTransaction
import com.navis.road.business.model.TruckTransactionStage
import com.navis.road.business.model.TruckVisitDetails
import com.navis.spatial.BinField
import com.navis.spatial.business.model.AbstractBin
import com.navis.spatial.business.model.BinContext
import com.navis.yard.business.YardTestUtils
import com.navis.yard.business.model.StackBlock
import com.navis.yard.business.model.YardBinModel
import org.apache.log4j.Logger

/**
 * Created with IntelliJ IDEA.
 * User: rajansh
 * Date: 29/10/13
 * Time: 2:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestCommandPurge {

  private HibernateApi _hibernateApi;
  /** holds the result returned by each method */
  def String returnString = null;
  /** json builder, frames the output in json format */
  def builder = new groovy.json.JsonBuilder();
  private DatabaseHelper _teamsDbHelper;
  /** handler to invoke the fields in helper class */
  test.extension.groovy.TestCommandHelper _testCommandHelper = new test.extension.groovy.TestCommandHelper();

  //Logger for TestCommand
  public Logger LOGGER = Logger.getLogger(test.extension.groovy.TestCommandPurge.class);


  /**
   * Purges the given equipment(s)<br>
   * It purges more than one equipment at a time in a comma separated format
   *
   * @param inParameters The map containing the method name to call along with the parameters<br><br>
   * command=PurgeUnit<br>
   * unitId=Unit Id1,Unit Id2...
   * @return JSON , <code>Unit Purged</code><br>
   *                <code>Unit not purged</code>
   * @Example
   * Table invoked in SPARCS : inv_unit<br>
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="PurgeUnit" /&gt;<br>
   * &lt;parameter id="unitId" value="APLU8362946" /&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   *
   */
  public String PurgeUnit(Map inParameters) {
    assert inParameters.size() == 2, '''Must supply 2 parameters:
                                        <parameter id="command" value="PurgeUnit" />
                                        <parameter id="unitId" value="<Unit Id>,<Unit Id>..." />'''

    String inUnitIdList = _testCommandHelper.checkParameter('unitId', inParameters);

    String[] unitIds = inUnitIdList.split(',');
    unitIds.each {
      try {
        InventoryTestUtils.purgeUnitsByEqId(it); //delete references in inv_unit
        returnString = 'Unit purged';
      } catch (Exception ex) {
        returnString = "Unit not purged:" + ex;
      }
      LOGGER.debug('PurgeUnit from refactored TC:' + inUnitIdList + ' : ' + returnString)
    }
    builder {
      actual_result returnString;
    }
    return builder;
  }

  /**
   * Purges the given unit and equipment(s)<br>
   * It purges more than one equipment at a time in a comma separated format
   *
   * @param inParameters The map containing the method name to call along with the parameters<br><br>
   * command=PurgeUnit<br>
   * unitId=Unit Id1,Unit Id2...
   * @return JSON , <code>Unit Purged</code><br>
   *                <code>Unit not purged</code>
   * @Example
   * Table invoked in SPARCS : inv_unit<br>
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="PurgeUnitAndEquipment" /&gt;<br>
   * &lt;parameter id="unitId" value="APLU8362946" /&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   *
   */
  public String PurgeUnitAndEquipment(Map inParameters) {
    assert inParameters.size() == 2, '''Must supply 2 parameters:
                                        <parameter id="command" value="PurgeUnitAndEquipment" />
                                        <parameter id="unitId" value="<Unit Id>,<Unit Id>..." />'''

    String inUnitIdList = _testCommandHelper.checkParameter('unitId', inParameters);
    String[] unitIds = inUnitIdList.split(',');
    unitIds.each {
      try {
        //delete references in inv_wi, inv_move_event and in inv_unit
        InventoryTestUtils.purgeUnitsByEqId(it);

        //to delete reference in road_truck_transaction and road_truck_transaction_stage
        DomainQuery dqRoadTran = QueryUtils.createDomainQuery(RoadEntity.TRUCK_TRANSACTION)
                .addDqPredicate(PredicateFactory.eq(RoadField.TRAN_CTR_NBR, it));
        List tTranList = HibernateApi.getInstance().findEntitiesByDomainQuery(dqRoadTran);
        if (tTranList != null && !tTranList.isEmpty()) {
          List tranGkey = new ArrayList();
          tTranList.each {
            TruckTransaction tran = (TruckTransaction) it;
            tranGkey.add(tran.getTranGkey());
          }
          DomainQuery dqRoadTranStages = QueryUtils.createDomainQuery(RoadEntity.TRUCK_TRANSACTION_STAGE)
                  .addDqPredicate(PredicateFactory.in(RoadField.STAGE_TRANSACTION, tranGkey));
          List tTranStgList = HibernateApi.getInstance().findEntitiesByDomainQuery(dqRoadTranStages);
          if (tTranStgList != null && !tTranStgList.isEmpty()) {
            tTranStgList.each {
              TruckTransactionStage tStage = (TruckTransactionStage) it;
              HibernateApi.getInstance().delete(tStage);
            }
          }
          //and then the transactions
          tTranList.each {
            TruckTransaction tran = (TruckTransaction) it;
            HibernateApi.getInstance().delete(tran);
          }
        }
        Equipment eq = Equipment.findEquipment(it)
        if (eq != null) {
          DomainQuery dqUe = QueryUtils.createDomainQuery(InventoryEntity.UNIT_EQUIPMENT)
                  .addDqPredicate(PredicateFactory.eq(InventoryField.UE_EQUIPMENT, eq.getEqGkey()));
          UnitEquipment ue = (UnitEquipment) HibernateApi.getInstance().getUniqueEntityByDomainQuery(dqUe);
          if (ue != null) {
            HibernateApi.getInstance().delete(ue);
          }
          DomainQuery dqEs = QueryUtils.createDomainQuery(InventoryEntity.EQUIPMENT_STATE)
                  .addDqPredicate(PredicateFactory.eq(InventoryField.EQS_EQUIPMENT, eq.getEqGkey()));
          EquipmentState eqState = (EquipmentState) HibernateApi.getInstance().getUniqueEntityByDomainQuery(dqEs);
          if (eqState != null) {
            HibernateApi.getInstance().delete(eqState);
            HibernateApi.getInstance().flush();
          }
          InventoryTestUtils.purgeEquipment(it); //delete references in ref_equipment
          returnString = 'Unit and Equipment purged';

        }
      } catch (Exception ex) {
        returnString = "Unit and Equipment not purged:" + ex;
      }
    }
    builder {
      actual_result returnString;
    }
    return builder;
  }

  /**
   * Purges Truck visit inturn closes the truck transaction<br>
   * Either purges a single truck visit details or multiple truck visit details<br>
   * To purge multiple truck visit,pass the values in a comma separated format.
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=PurgeTruckVisit<br>
   * truckVisit=Name of the truck visit<br>
   * @return <code>WorkInstruction purged</code> if purged,<br> else
   *         <code>WorkInstruction not purged</code>
   * @Example
   * Table invoked by SPARCS : argo_carrier_visit<br>
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="PurgeTruckVisit" /&gt;<br>
   * &lt;parameter id="truckVisit" value="TRK1" /&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String PurgeTruckVisit(Map inParameters) {
    assert inParameters.size() == 2, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 2 parameters:
                                        <parameter id="command" value="PurgeTruckVisit" />
                                        <parameter id="truckVisit" value="truckVisit" />'''

    String inTruckId = _testCommandHelper.checkParameter('truckVisit', inParameters);
    String[] idList = inTruckId.split(',');
    try {
      idList.each {
        CarrierVisit truckVisit = CarrierVisit.findCarrierVisit(ContextHelper.getThreadFacility(), LocTypeEnum.TRUCK, it);
        List<TruckVisitDetails> truckVisitDetailsList = TruckVisitDetails.findTVActiveByTruckLicenseNbr(it)
        TruckVisitDetails truckVisitDetails = null;
        if (truckVisit != null) {
          if (truckVisitDetailsList.size() >= 1) {
            truckVisitDetails = truckVisitDetailsList.get(0)
            truckVisit.setCvVisitPhase(CarrierVisitPhaseEnum.CLOSED)
            truckVisitDetails.setTvdtlsStatus(TruckVisitStatusEnum.CLOSED)
            returnString = 'Truck visit CLOSED';
          } else {
            returnString = 'Truck visit not CLOSED:No active trucks found for the given truck license'
          }
        } else {
          returnString = 'Truck visit not CLOSED';
        }
      }
    } catch (Exception ex) {
      returnString = 'Truck visit not CLOSED due to: ' + ex;
    }
    builder {
      actual_result returnString;
    }
    LOGGER.debug('PurgeTruckVisit:' + returnString + 'for the truckVisit :' + inTruckId)
    return builder;
  }

  /**
   * Purge ECS - AGV/ASC/QC orders from Teams database
   * Purges orders for any given CHE id
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=PurgeECS<br>
   * cheId=Che Id
   * @return <code>Orders purged</code> if purged,<br> else
   *         <code>Orders not purged</code>
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="PurgeECS" /&gt;<br>
   * &lt;parameter id="cheId" value="ABC" /&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String PurgeECS(Map inParameters) {
    assert inParameters.size() >= 2, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 2 parameters:
                                        <parameter id="command" value="PurgeECS" />
                                        <parameter id="cheId" value="ABC" />
                                        <parameter id="containerId" value="ABC" />'''

    String inCheShortName = _testCommandHelper.checkParameter('cheId', inParameters);
    String inContainerId = ''
    if (inParameters.containsKey('containerId')) {
      if (!inParameters.get('containerId').toString().isEmpty()) {
        inContainerId = inParameters.get('containerId')
      }
    }

    try {
      Che che = Che.findCheByShortName(inCheShortName, ContextHelper.getThreadYard());
      if (che != null) {
        String cheType = che.getCheKindEnum();
        if (cheType.contains('QC')) {
          //to handle qc delete - delete orders
          _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).execute(
                  String.format(
                          "delete from qc_orders where command_gkey in (select command_gkey from qc_commands where qc_id in ('" + inCheShortName + "'))"
                  )
          );
          //delete commands
          _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).execute(
                  String.format(
                          "delete from qc_commands where qc_id in ('" + inCheShortName + "')"
                  )
          );
          _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).execute(
                  String.format(
                          "delete from qc_status where qc_id in ('" + inCheShortName + "')"
                  )
          );

        } else if (cheType.contains('AGV')) { //for AGV
          //to handle qgv delete - delete commands
          _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).execute(
                  String.format(
                          "delete from agv_commands where order_gkey in (select order_gkey from agv_orders where che_id in  ('" + inCheShortName + "'))"
                  )
          );

          //delete orders
          _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).execute(
                  String.format(
                          "delete from agv_orders where che_id in ('" + inCheShortName + "')"
                  )
          );
          _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).execute(  //delete status
                  String.format(
                          "delete from agv_status where che_id in ('" + inCheShortName + "')"
                  )
          );
        } else if (cheType.contains('ASC')) {
          //to handle qc delete - delete commands
          _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).execute(
                  String.format(
                          "delete from asc_commands where order_gkey in (select order_gkey from asc_orders where che_id in  ('" + inCheShortName + "'))"
                  )
          );

          //this is required to purge records from asc_commands when moves are initiated by ASCs
          _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).execute(
                  String.format(
                          "delete from asc_commands where command_gkey in (select command_gkey from asc_orders where che_id in  ('" + inCheShortName + "'))"
                  )
          );

          //delete orders
          _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).execute(
                  String.format(
                          "delete from asc_orders where che_id in ('" + inCheShortName + "')"
                  )
          );
          _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).execute(      //delete status
                  String.format(
                          "delete from asc_status where che_id in ('" + inCheShortName + "')"
                  )
          );
        }
        //work instruction status
        if (!inContainerId.isEmpty()) {
          _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).execute(
                  String.format(
                          "delete from work_instruction_status where container_id = '" + inContainerId + "'"
                  )
          )
        }
        returnString = 'PurgeECS success : All orders for the CHE deleted';
      } else {
        returnString = 'No CHE available with given Che Id: ' + inCheShortName;
      }
    } catch (Exception ex) {
      returnString = 'ECS not purged' + ex;
    }
    builder {
      actual_result returnString;
    }
    LOGGER.debug('PurgeECS:' + returnString + 'for the che :' + inCheShortName)
    return builder;
  }

  /**
   * Purges vessel visit<br>
   * Either purges a single pool or multiple pools<br>
   * For multiple vessel visit,pass the vessel name in a comma separated format.
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=PurgeVesselVisit<br>
   * vesselId=Name of the vessel<br>
   * @return <code>Vessel visit purged</code> if purged successfully,else<br>
   *          <code>Vessel visit not purged</code>
   * @Example
   * Table invoked by SPARCS : argo_carrier_visit
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="PurgeVesselVisit" /&gt;<br>
   * &lt;parameter id="vesselId" value="VV01,VV02" /&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String PurgeVesselVisit(Map inParameters) {
    assert inParameters.size() == 2, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 2 parameters:
                                        <parameter id="command" value="PurgeVesselVisit" />
                                        <parameter id="vesselId" value="<Vessel Name>" />'''
    String inVesselVisit = _testCommandHelper.checkParameter('vesselId', inParameters);
    def inVesselVisitList = inVesselVisit.split(',');
    inVesselVisitList.each {
      try {
        Serializable inComplexGkey = ContextHelper.getThreadComplex().getPrimaryKey();
        DomainQuery dq = QueryUtils.createDomainQuery(ArgoEntity.CARRIER_VISIT)
                .addDqPredicate(PredicateFactory.eq(ArgoField.CV_COMPLEX, inComplexGkey))
                .addDqPredicate(PredicateFactory.eq(ArgoField.CV_ID, it))
                .addDqPredicate(PredicateFactory.eq(ArgoField.CV_CARRIER_MODE, LocTypeEnum.VESSEL));

        List cvList = Roastery.getHibernateApi().findEntitiesByDomainQuery(dq);
        if (cvList != null && cvList.size() > 0) {
          for (Iterator iterator = cvList.iterator(); iterator.hasNext();) {
            CarrierVisit cv = (CarrierVisit) iterator.next();
            if (LocTypeEnum.VESSEL.equals(cv.getCvCarrierMode())) {
              cv.setCvVisitPhase(CarrierVisitPhaseEnum.CANCELED)
              ArgoUtils.carefulDelete(cv);
              returnString = 'Vessel visit purged'
            }
          }
        } else {
          returnString = 'Vessel visit purged'
        }
      } catch (Exception ex) {
        returnString = 'Vessel visit not purged' + ex;
      }
    }
    builder {
      actual_result returnString;
    }
    return builder;
  }

  /**
   * Removes moves from Move list for specified ASC Block
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=PurgeASCMoveList<br>
   * @return JSON ,
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="PurgeASCMoveList"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String PurgeASCMoveList(Map inParameters) {
    assert inParameters.size() == 2, '''Must supply 2 parameters:
                                 <parameter id="command" value="PurgeASCMoveList" />
                                 <parameter id="blockName" value="<blockName>" />'''

    String inBlockName = _testCommandHelper.checkParameter('blockName', inParameters);

    try{
    UserContext userContext = ContextHelper.getThreadUserContext();
    StackBlock ascBlock = AscTestUtils.findBlockByName(inBlockName);

    Serializable blockGkey = ascBlock.getAbnGkey();

    Collection<AscMove> retmoves = AscTestUtils.findAscMovesInDatabase(blockGkey);
    StringBuilder result = new StringBuilder("");
    for (AscMove m : retmoves) {
      AscTestUtils.getAscMoveManager().deleteMove(m);
    }
    HibernateApi.getInstance().flush();
    retmoves = AscTestUtils.findAscMovesInDatabase(blockGkey);
    if (retmoves.size() == 0) {
      returnString = 'Move List removed';
    } else {
      returnString = "move list not Removed.";
    }
    } catch(Exception ex) {
      returnString = "move list not Removed, exception occured";
    }
    builder {
      actual_result returnString;
    }
    return builder;
  }

  /**
   * Purges all references (EC Events, Move Events, Work Instruction's and Work Assignment's)  <br>
   * to the specified che from the database <br>
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=PurgeReferencesToChe<br>
   * @return JSON ,
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="PurgeReferencesToChe"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String PurgeReferencesToChe(Map inParameters) {
    assert inParameters.size() == 2, '''Must supply 2 parameters:
                                 <parameter id="command" value="PurgeReferencesToChe" />
                                 <parameter id="cheId" value="<che id list>" />'''


    String inCheIdList = _testCommandHelper.checkParameter('cheId', inParameters);
    String[] cheList = inCheIdList.split(",");
    DomainQuery dq = null, dq_ecev = null, dq_mve = null, dq_wa = null;

    try {
      for (String cheId : cheList) {
        Che che = ((Che) HibernateApi.getInstance().getUniqueEntityByDomainQuery(QueryUtils.createDomainQuery(ArgoEntity.CHE)
                .addDqPredicate(PredicateFactory.eq(ArgoField.CHE_SHORT_NAME, cheId))));

        if (che != null) {
          // this deletes all EC events
          dq_ecev = QueryUtils.createDomainQuery(ArgoEntity.EC_EVENT).addDqPredicate(PredicateFactory.disjunction()
                  .add(PredicateFactory.eq(ArgoField.ECEVENT_CHE_ID, che.getCheId())));
          HibernateApi.getInstance().deleteByDomainQuery(dq_ecev);

          // this deletes all move events
          dq_mve = QueryUtils.createDomainQuery(ServicesMovesEntity.MOVE_EVENT).addDqPredicate(PredicateFactory.disjunction()
                  .add(PredicateFactory.eq(ServicesMovesField.MVE_CHE_FETCH, che.getCheId())));
          HibernateApi.getInstance().deleteByDomainQuery(dq_mve);

          dq_mve = QueryUtils.createDomainQuery(ServicesMovesEntity.MOVE_EVENT).addDqPredicate(PredicateFactory.disjunction()
                  .add(PredicateFactory.eq(ServicesMovesField.MVE_CHE_PUT, che.getCheId())));
          HibernateApi.getInstance().deleteByDomainQuery(dq_mve);


          dq_mve = QueryUtils.createDomainQuery(ServicesMovesEntity.MOVE_EVENT).addDqPredicate(PredicateFactory.disjunction()
                  .add(PredicateFactory.eq(ServicesMovesField.MVE_CHE_CARRY, che.getCheId())));
          HibernateApi.getInstance().deleteByDomainQuery(dq_mve);

          // remove any WIs and WA's that have been dispatched
          dq_wa = QueryUtils.createDomainQuery(ArgoEntity.WORK_ASSIGNMENT).addDqPredicate(PredicateFactory.disjunction()
                  .add(PredicateFactory.eq(ArgoField.WORKASSIGNMENT_CHE, che.getCheGkey())));

          //create query to get WA related to che's gkey and delete them
          List workAssignmentList = HibernateApi.getInstance().findEntitiesByDomainQuery(dq_wa);
          if (workAssignmentList != null) {
            for (WorkAssignment workAssignment : workAssignmentList) {
              Long wa_gKey = workAssignment.getWorkassignmentGkey();    //get WA's gkey to find WI related to WA
              if (wa_gKey != null) {
                dq = QueryUtils.createDomainQuery(MovesEntity.WORK_INSTRUCTION).addDqPredicate(PredicateFactory.disjunction()
                        .add(PredicateFactory.eq(MovesField.WI_ITV_WORK_ASSIGNMENT, wa_gKey))
                        .add(PredicateFactory.eq(MovesField.WI_CHE_WORK_ASSIGNMENT, wa_gKey)));
                List workInstructionList = HibernateApi.getInstance().findEntitiesByDomainQuery(dq);
                if (workInstructionList != null) {
                  workInstructionList.each {
                    HibernateApi.getInstance().delete(it, true);
                  }
                }
                //Delete job step projection prior to deleting WA corresponding to the gKey
                DomainQuery dq_jps = QueryUtils.createDomainQuery(ArgoEntity.JOB_STEP_PROJECTION)
                        .addDqPredicate(PredicateFactory.eq(ArgoField.JSP_WORK_ASSIGNMENT, wa_gKey))
                List jobProjectionsList = HibernateApi.getInstance().findEntitiesByDomainQuery(dq_jps);
                if (jobProjectionsList != null) { //iterate each job step projection and delete
                  jobProjectionsList.each {
                    HibernateApi.getInstance().delete(it, true);
                  }
                }
                //Delete ctl_drive_instruction prior to deleting WA corresponding to the gKey
                DomainQuery dq_ctl = QueryUtils.createDomainQuery(com.navis.control.ControlEntity.DRIVE_INSTRUCTION)
                        .addDqPredicate(PredicateFactory.eq(com.navis.control.ControlField.DI_WORK_ASSIGNMENT, wa_gKey))
                List ctlList = HibernateApi.getInstance().findEntitiesByDomainQuery(dq_ctl);
                if (ctlList != null) { //iterate each ctl drive instruction and delete
                  ctlList.each {
                    HibernateApi.getInstance().delete(it, true);
                  }
                }
              }
              //delete WA
              HibernateApi.getInstance().delete(workAssignment, true);
            }
          }
        }
        returnString = 'All references to che (EC Events / Move Events / WI / WA) purged';
      }
    } catch (Exception ex) {
      returnString = 'EC Events / Move Events / WI / WA  not purged - ' + 'Reason:' + ex;
    }
    builder {
      actual_result returnString;
    }
    LOGGER.debug('PurgeReferencesToChe:' + returnString + ' cheId : ' + inCheIdList)
    return builder;
  }

  /**
   * Clears stack status for the given Transfer Point
   * For the stack status set to 'RACK', it doesn't clear the status.
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=ClearStackStatus<br>
   * blockName=Name of the block<br>
   * startRow=starting row position to be selected<br>
   * startCol=starting col position to be selected<br>
   * endRow=ending row position to be selected
   * endCol=ending column position to be selected
   * @return JSON ,
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="ClearStackStatus" /&gt;<br>
   * &lt;parameter id="blockName" value="asc01" /&gt;<br>
   * &lt;parameter id="startRow" value="1" /&gt;<br>
   * &lt;parameter id="endRow" value="20" /&gt;<br>
   * &lt;parameter id="startCol" value="1" /&gt;<br>
   * &lt;parameter id="endCol" value="8" /&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String ClearStackStatus(Map inParameters) {
    assert inParameters.size() == 6, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 6 parameters:
                                        <parameter id="command" value="ClearStackStatus" />
                                        <parameter id="blockName" value="block name" />
                                        <parameter id="startRow" value="<Start Row in the block>" />
                                        <parameter id="startCol" value="<Start Col in the block>" />
                                        <parameter id="endRow" value="<Last Row in the block>" />
                                        <parameter id="endCol" value="<Last Col in the block>" />'''
    String inBlockName = _testCommandHelper.checkParameter('blockName', inParameters);
    String inStartRow = _testCommandHelper.checkParameter('startRow', inParameters);
    String inEndRow = _testCommandHelper.checkParameter('endRow', inParameters);
    String inStartCol = _testCommandHelper.checkParameter('startCol', inParameters);
    String inEndCol = _testCommandHelper.checkParameter('endCol', inParameters);

    try {
      Yard thisYard = ContextHelper.getThreadYard();
      assert thisYard != null;
      YardBinModel yardModel = (YardBinModel) HibernateApi.getInstance().downcast(thisYard.getYrdBinModel(), YardBinModel.class);
      //Find stack
      StackBlock stackBlock = StackBlock.findStackBlock(yardModel, inBlockName);
      if (stackBlock != null) {
        if (ClearStackStatusForRange(stackBlock, inStartRow, inEndRow, inStartCol, inEndCol)) {
          returnString = "Stack Status cleared"
        } else {
          returnString = "Stack Status cleared"
        };
      } else {
        returnString = 'Stack status not cleared - Stack block not found'
      }
    } catch (Exception ex) {
      returnString = "Stack Status not cleared" + ex; ;
    }
    builder {
      actual_result returnString
    }
    return builder;
  }

  //Method used to clear stack status and set only the default value 'RACK'
  private boolean ClearStackStatusForRange(StackBlock inBlock, String inStartRow, String inEndRow, String inStartCol, inEndCol) {
    List<StackStatus> statusList = new test.extension.groovy.TestCommandManipulation().findOrCreateStackStatus(inBlock, inStartRow, inEndRow,
            inStartCol, inEndCol);
    if (statusList != null) {
      def preserveStatus = '';
      statusList.each {
        preserveStatus = it.getStackstatusStatusChars();
        if (preserveStatus != null) {
          if (!preserveStatus.isEmpty() && preserveStatus.contains('R') && preserveStatus.size() == 7) {
            if (it.getStackstatusStatusChars().charAt(6).toString().equals('R')) {   //status set to rack, need to restore the status
              it.setStackstatusStatusChars(_testCommandHelper.CLEAR) //clears other status and set the status to rack back.
              it.setStackstatusStatusChars(_testCommandHelper.RACK)
            } else {
              it.setStackstatusStatusChars(_testCommandHelper.CLEAR)
            }
          } else {
            it.setStackstatusStatusChars(_testCommandHelper
                    .CLEAR)
          }
        } else {
          return false
        };
      }
      return true;
    } else {
      return false
    };
  }

  /**
   * Cancels the truck transactions for given Containers .
   *
   * @Precondition
   * Trasactions should have been created by gate operations
   * @param inParameters The map containing the method name to call along with the parameters<br><br>
   * <br>command=CancelTruckTransactions
   * <br>unitId=Container Number for which truck transaction should be deleted
   * @return JSON , <code>Truck Transactions for Unit: '+ inUnitId + 'are cancelled</code><br>
   *                <code>Truck Transactions for Unit: '+ inUnitId + 'are cancelled </code>
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="CancelTruckTransactions" /&gt;<br>
   * &lt;parameter id="unitId" value="TEST0000019"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String CancelTruckTransactions(Map inParameters) {
    assert inParameters.size() == 2, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 2 parameters:
                                        <parameter id="command" value="CancelTruckTransactions" />
                                        <parameter id="unitId" value="<Unit Id>" />'''

    String inUnitId = _testCommandHelper.checkParameter('unitId', inParameters);
    try {
      com.navis.road.business.RoadTestUtils.cancelTruckTransactions(inUnitId);
      returnString = 'Truck Transactions for Unit: ' + inUnitId + ' cancelled'
    } catch (Exception ex) {
      returnString = 'Truck Transactions for Unit: ' + inUnitId + '  not cancelled : ' + ex.printStackTrace();
    }
    builder {
      actual_result returnString
    }
    LOGGER.debug('CancelTruckTransactions:' + returnString)
    return builder;
  }

  /**
   * Purges train visit<br>
   * For multiple vessel visit,pass the vessel name in a comma separated format.
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=PurgeTrainVisit<br>
   * trainCvId=Name of the train visit<br>
   * @return <code>Train visit purged</code> if purged successfully,else<br>
   *          <code>Train visit not purged</code>
   * @Example
   * Table invoked by SPARCS : argo_carrier_visit
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="PurgeTrainVisit" /&gt;<br>
   * &lt;parameter id="trainCvId" value="RAILT01" /&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String PurgeTrainVisit(Map inParameters) {
    assert inParameters.size() == 2, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 2 parameters:
                                        <parameter id="command" value="PurgeVesselVisit" />
                                        <parameter id="trainCvId" value="<Train visit Name>" />'''
    String inTrainVisit = _testCommandHelper.checkParameter('trainCvId', inParameters);
    def inTrainVisitList = inTrainVisit.split(',');
    inTrainVisitList.each {
      try {
        Serializable inComplexGkey = ContextHelper.getThreadComplex().getPrimaryKey();
        DomainQuery dq = QueryUtils.createDomainQuery(ArgoEntity.CARRIER_VISIT)
                .addDqPredicate(PredicateFactory.eq(ArgoField.CV_COMPLEX, inComplexGkey))
                .addDqPredicate(PredicateFactory.eq(ArgoField.CV_ID, it))
                .addDqPredicate(PredicateFactory.eq(ArgoField.CV_CARRIER_MODE, LocTypeEnum.TRAIN));

        List cvList = Roastery.getHibernateApi().findEntitiesByDomainQuery(dq);
        if (cvList != null && cvList.size() > 0) {
          for (Iterator iterator = cvList.iterator(); iterator.hasNext();) {
            CarrierVisit cv = (CarrierVisit) iterator.next();
            if (LocTypeEnum.TRAIN.equals(cv.getCvCarrierMode())) {
              cv.setCvVisitPhase(CarrierVisitPhaseEnum.CANCELED)
              ArgoUtils.carefulDelete(cv);
              returnString = 'Train visit purged'
            }
          }
        } else {
          returnString = 'Train visit purged'
        }
      } catch (Exception ex) {
        returnString = 'Train visit not purged' + ex;
      }
    }
    builder {
      actual_result returnString;
    }
    return builder;
  }

  /**
   * Releases all holds and grants all permissions applied to the given unit
   * For multiple units,pass the unit name in a comma separated format.
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=ReleaseGrantAllHoldsAndPermissions<br>
   * unitId=name of the unit
   *
   * @return JSON , <code>Released holds and granted permissions for given units successfully</code> if granted successfully,else<br>
   *                <code>There are no permissions/holds applied to the given unit</code>
   *                <code>Releasing holds/granting permission for units failed</code>
   *
   * @see test
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="ReleaseGrantAllHoldsAndPermissions"/&gt;<br>
   * &lt;parameter id="unitId" value="SHAU2225432" /&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String ReleaseGrantAllHoldsAndPermissions(Map inParameters) {
    assert inParameters.size() == 2, '''Must supply 2 parameters:
                                        <parameter id="command" value="ReleaseGrantAllHoldsAndPermissions" />
                                        <parameter id="unitId" value="SHAU0908071,SHAU2225432" />'''

    String inUnitIdList = _testCommandHelper.checkParameter('unitId', inParameters);
    String[] unitIds = inUnitIdList.split(',');
    try {
      ServicesManager servicesManager = (ServicesManager) Roastery.getBean(ServicesManager.BEAN_ID);
      unitIds.each {
        Unit unit = ((Unit) HibernateApi.getInstance().getUniqueEntityByDomainQuery(QueryUtils.createDomainQuery(InventoryEntity.UNIT)
                .addDqPredicate(PredicateFactory.eq(InventoryField.UNIT_ID, it))));
        Collection impedimentsList = servicesManager.getImpedimentsForEntity(unit)
        //if the list contains some hold or permission , traverse and find out its details
        if (!impedimentsList.isEmpty()) {
          for (IImpediment impediment : impedimentsList) {
            //grants all permissions if applied
            if (impediment.getRuleTypeRole().equals(ServiceRuleTypeRoleEnum.SIMPLE)) {
              if ((impediment.getFlagType().getPurpose().equals(FlagPurposeEnum.PERMISSION) &&
                      impediment.getStatus().equals(FlagStatusEnum.REQUIRED)) ||
                      (impediment.getFlagType().getPurpose().equals(FlagPurposeEnum.HOLD) &&
                              impediment.getStatus().equals(FlagStatusEnum.ACTIVE))) {
                servicesManager.applyPermission(impediment.getFlagType().getId(), unit, null, null, null)
              }
            }
          }
        } else {
          returnString = 'There are no permissions/holds applied to the given unit'
        }
      }
      returnString = 'Released holds and granted permissions for given units successfully'
    } catch (Exception inEx) {
      returnString = 'Releasing holds/granting permission for units failed ' + inEx;
      LOGGER.debug('ReleaseGrantAllHoldsAndPermissions:' + returnString)
    }
    LOGGER.debug('ReleaseGrantAllHoldsAndPermissions:' + returnString)
    builder {
      actual_result returnString;
    }
    return builder;

  }

}
