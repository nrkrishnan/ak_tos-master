/*
 * Copyright (c) 2013 Navis LLC. All Rights Reserved.
 *
 */

package test.extension.groovy

import com.navis.argo.ArgoEntity
import com.navis.argo.ArgoField
import com.navis.argo.business.api.EcEventTypeConsts
import com.navis.argo.business.atoms.CheInstructionTypeEnum
import com.navis.argo.business.atoms.WaMovePurposeEnum
import com.navis.argo.business.xps.model.Che
import com.navis.argo.business.xps.model.WorkAssignment
import com.navis.framework.persistence.HibernateApi
import com.navis.framework.portal.QueryUtils
import com.navis.framework.portal.query.DomainQuery
import com.navis.framework.portal.query.PredicateFactory
import org.apache.log4j.Logger
import org.hibernate.SQLQuery

/**
 * Created with IntelliJ IDEA.
 * User: rajansh
 * Date: 10/12/13
 * Time: 6:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestCommandListenWait {

  private HibernateApi _hibernateApi;
  /** holds the result returned by each method */
  def String returnString = null;
  /** json builder, frames the output in json format */
  def builder = new groovy.json.JsonBuilder();
  /** handler to invoke the fields in helper class */
  test.extension.groovy.TestCommandHelper _testCommandHelper = new test.extension.groovy.TestCommandHelper();

  //Logger for TestCommand
  public Logger LOGGER = Logger.getLogger(TestCommandListenWait.class);

  /**
   * Wait for the given event to occur by polling the database for every 15 secs till the event occurs or timeOut happens
   * @param inParameters
   * @return
   */
  public String WaitForEvent(Map inParameters) {
    assert inParameters.size() >= 4, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 4 parameters:
                                        <parameter id="command" value="WaitForEvent" />
                                        <parameter id="sqlQuery" value="<SQL query>" />
                                        <parameter id="cheId" value="<che name>" />
                                        <parameter id="eventType" value="<type of the event>" />
                                        <parameter id="entity" value="<entity name>" />
                                        <parameter id="movePurpose" value="<Receive/Deliver/Reposition>" /> // optional
                                        <parameter id="TimeOutInSeconds" value="<in secs>" />
                                        <parameter id="database" value="SPARCS/TEAMS" />'''

    String inTimeOut = _testCommandHelper.checkParameter('timeOutInSeconds', inParameters);
    String inDBName = inParameters.get('database')
    String inSQLQuery = ''
    if (inParameters.containsKey('sqlQuery'))   //if sqlquery is directly given in the input, start polling the db for the given query
    {
      inSQLQuery = inParameters.get('sqlQuery')
    }
    if (inSQLQuery.isEmpty()) { //form the sql query here based on the inputs given
      initializeEntityMap() // initialize the map, it will be used sooner
      String cheId = '', eventType = '', entity = '', inMovePurpose = ''
      if (inParameters.containsKey('cheId')) {
        cheId = inParameters.get('cheId')
      }
      if (inParameters.containsKey('eventType')) {
        eventType = inParameters.get('eventType')
      }
      if (inParameters.containsKey('entity')) {
        entity = inParameters.get('entity')
      }
      if (inParameters.containsKey('movePurpose')) {
        inMovePurpose = inParameters.get('movePurpose')
      }

        inSQLQuery = formSQLQuery(cheId, eventType, entity, inMovePurpose)
    }
    long pollingFrequency = 5000 //by default polling frequency is kept as 5 secs
    try {
      returnString = WaitForDBEvent(inSQLQuery, pollingFrequency, inTimeOut.toLong(), inDBName)
      LOGGER.debug(returnString)
    } catch (Exception ex) {
      ex.printStackTrace()
      LOGGER.error('Wait for DB event failed : ' + inSQLQuery + ":" + ex.getMessage() + ex)
    }
    builder {
      actual_result returnString;
    }
    return builder;
  }

  /**
   * Forms sql query from the inputs given
   * @param cheId
   * @param eventType
   * @param db
   * @return
   */
  private String formSQLQuery(String cheId, String eventType, String entity, String inMvPurpose) {
    String sqlQuery = ''
    //get the che gkey here as workassignment table should be queried with che gkey
    Che che = ((Che) HibernateApi.getInstance().getUniqueEntityByDomainQuery(QueryUtils.createDomainQuery(ArgoEntity.CHE)
            .addDqPredicate(PredicateFactory.eq(ArgoField.CHE_SHORT_NAME, cheId))));
    Long cheGKey = 0L;
    def cheKindEnum;
    if (che != null) {
      cheGKey = che.cheGkey
      cheKindEnum = che.cheKindEnum
    }
    if (entity.equalsIgnoreCase(WorkAssignment)) {
      // select status_enum from xps_workassignment where che_entity_gkey = 29 and status_enum = 'SENDING/ACCEPTED/COMPLETED/REJECTED/ABORTED'
      // and move_purpose_enum = 'ITV_RECEIVE/ITV_DELIVER/ITV_REPOSITION'
      if (che.cheKindEnum.toString().contains(AGV))  //if chekind is AGV
      {
      def movePurpose = getMovePurposeEnum(inMvPurpose)
      sqlQuery = "select " + entityMap.get(WorkAssignmentStatus) + " from " + entityMap.get(WorkAssignment) + " where che_entity_gkey = " + cheGKey +
              " and " + entityMap.get(WorkAssignmentStatus) + " = '" + eventType + "' and move_purpose_enum = '" + movePurpose + "'"
      }
      if (che.cheKindEnum.toString().contains(ASC) || che.cheKindEnum.toString().contains(QC))  //if chekind is ASC or QC
      {
        sqlQuery = "select " + entityMap.get(WorkAssignmentStatus) + " from " + entityMap.get(WorkAssignment) + " where che_entity_gkey = " + cheGKey +
                " and " + entityMap.get(WorkAssignmentStatus) + " = '" + eventType + "'"
      }
    } else if (entity.equalsIgnoreCase(WorkInstruction)) {
      //select move_stage from inv_wi where itv_gkey = 29 and move_stage = 'PLANNED'
      if (che.cheKindEnum.toString().contains(AGV))  //if chekind is AGV
      {
        sqlQuery = "select " + entityMap.get(WorkInstructionMoveStage) + " from " + entityMap.get(WorkInstruction) + " where itv_gkey = " + cheGKey +
                " and " + entityMap.get(WorkInstructionMoveStage) + " = '" + eventType + "'"
      }
      //select move_stage from inv_wi where che_gkey = 30 and move_stage = 'PLANNED'
      if (che.cheKindEnum.toString().contains(ASC)) //if chekind is ASC
      {
        sqlQuery = "select " + entityMap.get(WorkInstructionMoveStage) + " from " + entityMap.get(WorkInstruction) + " where che_gkey = " + cheGKey +
                " and " + entityMap.get(WorkInstructionMoveStage) + " = '" + eventType + "'"
      }
    } else if (entity.equalsIgnoreCase(ECEvent)) {
      eventType = _testCommandHelper.ecEventVarMap.get(eventType)
      //EcEvents such as Idle,Complete,Dispatch may come twice/thrice according to the job processed, so handle it differently to identify whether
      //its a laden or unladen move
    /*  boolean isWACheckReqd = getWACheckRequired(che, eventType)
      if (isWACheckReqd) {
        //select type_description,che_name from xps_ecevent where che_name = 'AGV502' and type_description='DSPT' and  work_assignment_gkey = 61
        def waGKey = getWAGKey(che, eventType)
        sqlQuery = "select " + entityMap.get(ECEventType) + " from " + entityMap.get(ECEvent) + " where che_name = " + cheId +
                " and " + entityMap.get(ECEventType) + " = '" + EcEventTypeConsts."$eventType" + "' and work_assignment_gkey = " + waGKey
      } else {*/
        //select type_description,che_name from xps_ecevent where che_name = 'AGV502' and type_description='DSPT'
        sqlQuery = "select " + entityMap.get(ECEventType) + " from " + entityMap.get(ECEvent) + " where che_name = '" + cheId +
                "' and " + entityMap.get(ECEventType) + " = '" + EcEventTypeConsts."$eventType" + "'"
      //}
    } else if (entity.contains(AGV)) {
      //select STATUS from agv_orders where CHE_ID = 'AGV510' and STATUS = 'ENTERED' and AGV_ORDER_TYPE = 'RECEIVE/DELIVER/REPOSITION'
      sqlQuery = "select status from " + entityMap.get(entity) + " where che_id = '" + cheId + "' and status = '" + getEcEventType(eventType) +
              "' and AGV_ORDER_TYPE = '" + getAgvOrderType(eventType) + "'"
    } else if (entity.contains(ASC)) {
      // select STATUS from asc_orders where CHE_ID = 'AS412W' and STATUS = 'ENTERED'
      sqlQuery = "select status from " + entityMap.get(entity) + " where che_id = '" + cheId + "' and status = '" + getEcEventType(eventType) + "'"
    } else if (entity.contains(QC)) {
      //select STATUS from qc_orders where QC_ID = 'QC101' and STATUS = 'ENTERED'
      sqlQuery = "select status from " + entityMap.get(entity) + " where qc_id = '" + cheId + "' and status = '" + getEcEventType(eventType) + "'"
    }
    return sqlQuery
  }

  private boolean getWACheckRequired(Che inChe, String eventType) {
    if (inChe.cheKindEnum.toString().equalsIgnoreCase('AGV')) { //make it true if che is AGV and eventtype is any of the below
      if (eventType.equalsIgnoreCase('Idle') || eventType.equalsIgnoreCase('Complete') || eventType.equalsIgnoreCase('Dispatch')) {
        return true
      } else {
        return false
      };
    } else {
      return false
    };
  }

  private String getWAGKey(Che inChe, String eventType) {
    WorkAssignment workAssignment = null;
    eventType = getMovePurposeEnum(eventType)
    DomainQuery dq = QueryUtils.createDomainQuery(ArgoEntity.WORK_ASSIGNMENT)
    dq.addDqPredicate(PredicateFactory.eq(ArgoField.WORKASSIGNMENT_CHE, inChe.cheGkey))
    dq.addDqPredicate(PredicateFactory.eq(ArgoField.WORKASSIGNMENT_MOVE_PURPOSE_ENUM, WaMovePurposeEnum."$eventType"))
    workAssignment = (WorkAssignment) HibernateApi.getInstance().getUniqueEntityByDomainQuery(dq);
  }

  private String getEcEventType(String eventType) {
    if (eventType.contains('Entered')) {
      return 'ENTERED'
    } else if (eventType.contains('Accepted')) {
      return 'WORKING'
    } else if (eventType.contains('Completed')) {
      return 'COMPLETE'
    }
  }

  private String getAgvOrderType(String eventType) {
    if (eventType.contains('Receive')) {
      return 'RECEIVE'
    } else if (eventType.contains('Deliver')) {
      return 'DELIVER'
    } else if (eventType.contains('Reposition')) {
      return 'REPOSITION'
    }
  }

  private String getMovePurposeEnum(String eventType) {
    if (eventType.contains('Receive')) {
      return 'ITV_RECEIVE'
    } else if (eventType.contains('Deliver')) {
      return 'ITV_DELIVER'
    } else if (eventType.contains('Reposition')) {
      return 'ITV_REPOSITION'
    } else if (eventType.contains('Recharge')) {
      return 'ITV_SERVICING'
    }
  }

  /**
   * Starts polling DB for the given SQLQuery's result.
   * Stops polling DB either on the event happens or the timeout happens
   * @param sqlQuery
   * @param pollingFrequency
   * @param timeOut
   * @return
   */
  private String WaitForDBEvent(String sqlQuery, long pollingFrequency, long timeOut, String inDBName) {
    List resultList = null;
    long duration = System.currentTimeMillis() + timeOut * 1000;
    resultList = getSQLResultList(sqlQuery, inDBName) //gets query results from DB
    def result = 'WaitForEvent failed : ' + sqlQuery // by default setting it to 'failed' msg, it will be overwritten if successful
    if (resultList != null) {
      if (resultList.empty == false) {
        result = 'WaitForEvent succeeded : ' + sqlQuery
      } else { //if db event for the given sql query did not occur, start polling the DB
        boolean timeOutExit = true;
        while (resultList.empty == true && System.currentTimeMillis() < duration) {
          sleep(pollingFrequency)
          resultList = getSQLResultList(sqlQuery, inDBName) //gets query results from DB
          if (resultList.empty == false) { //if result list is not empty
            result = 'WaitForEvent succeeded : ' + sqlQuery
            timeOutExit = false; //setting time out exit false here, as the event occured before time out
            break //breaking the loop once the event occur
          }
        }
        if (timeOutExit == false) {
          result = 'WaitForEvent succeeded : ' + sqlQuery
        } else {
          result = 'WaitForEvent failed , exit because of timeOut : ' + sqlQuery
        }
      }
    } else {
      result = 'WaitForEvent failed , error in sql query : ' + sqlQuery
    }
  }

  /**
   * Gets SQL result list for the given SQL query
   * @param sqlQuery
   * @return
   */
  private List getSQLResultList(String sqlQuery, String inDB) {
    SQLQuery resultSet = null;
    List resultList = null;
    if (inDB.equalsIgnoreCase('SPARCS')) {
      resultSet = HibernateApi.getInstance().getCurrentSession().createSQLQuery(sqlQuery);
      if(resultSet != null)
      resultList = resultSet.list();
    } else if (inDB.equalsIgnoreCase('TEAMS')) {
      resultList = _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).queryForList(String.format(sqlQuery));
    }
    return resultList
  }

  private final Map entityMap = new HashMap<String, String>()  //final map which holds the entity name and its class

  //Initializes entity map with its corresponding table name
  private void initializeEntityMap() {
    //add WI class
    entityMap.put('WorkAssignment', 'xps_workassignment')
    entityMap.put('WorkAssignmentStatus', 'status_enum')

    entityMap.put('WorkInstruction', 'inv_wi')
    entityMap.put('WorkInstructionMoveStage', 'move_stage')

    entityMap.put('ECEvent', 'xps_ecevent')
    entityMap.put('ECEventType', 'type_description')

    entityMap.put('AGVOrders', 'agv_orders')
    entityMap.put('AGVCommands', 'agv_commands')

    entityMap.put('ASCOrders', 'asc_orders')
    entityMap.put('ASCCommands', 'asc_commands')

    entityMap.put('QCOrders', 'qc_orders')
    entityMap.put('QCCommands', 'qc_commands')
  }

  //constant variables
  private final String WorkAssignment = 'WorkAssignment'
  private final String WorkInstruction = 'WorkInstruction'
  private final String WorkInstructionMoveStage = 'WorkInstructionMoveStage'
  private final String WorkAssignmentStatus = 'WorkAssignmentStatus'
  private final String ECEvent = 'ECEvent'
  private final String ECEventType = 'ECEventType'
  private final String AGV = 'AGV'
  private final String ASC = 'ASC'
  private final String QC = 'QC'
}
