package test.extension.groovy

import com.navis.argo.ArgoEntity
import com.navis.argo.ArgoField
import com.navis.argo.business.atoms.CheKindEnum
import com.navis.argo.business.atoms.CheStatusEnum
import com.navis.argo.business.atoms.WiMoveKindEnum
import com.navis.argo.business.xps.model.Che
import com.navis.control.business.ControlTestUtils
import com.navis.framework.business.Roastery
import com.navis.framework.persistence.DatabaseHelper
import com.navis.framework.persistence.HibernateApi
import com.navis.framework.portal.QueryUtils
import com.navis.framework.portal.query.PredicateFactory
import com.navis.inventory.InventoryEntity
import com.navis.inventory.InventoryField
import com.navis.inventory.MovesEntity
import com.navis.inventory.MovesField
import com.navis.inventory.business.api.SearchResults
import com.navis.inventory.business.api.UnitFinder
import com.navis.inventory.business.moves.WorkInstruction
import com.navis.inventory.business.units.Unit
import com.navis.inventory.business.units.UnitFacilityVisit
import org.apache.log4j.Logger

/**
 * Created with IntelliJ IDEA.
 * User: rajansh
 * Date: 29/10/13
 * Time: 3:15 PM
 * To change this template use File | Settings | File Templates.
 */
class TestCommandTEAMS {

  /** holds the result returned by each method */
  def String returnString = null;
  /** json builder, frames the output in json format */
  def builder = new groovy.json.JsonBuilder();
  private DatabaseHelper _teamsDbHelper;
  /** handler to invoke the fields in helper class */
  test.extension.groovy.TestCommandHelper _testCommandHelper= new test.extension.groovy.TestCommandHelper();

  //Logger for TestCommand
  public Logger LOGGER = Logger.getLogger(TestCommandTEAMS.class);

  /**
   * Sets the che operating mode to the given che whether it is OFFLINE or AUTOMATIC.<br>
   * It operates on TEAMS table, but it expects the workflow to be generated on SPARCS.<br>
   *
   * For ASC If Operating mode = OFFLINE/AUTOMATIC,Work_Status = UNDEFINED/IDLE,Technical_status = RED/GREEN<br>
   * For AGV If Operating mode = OFFLINE/AUTOMATIC,Technical_status = RED/GREEN<br>
   * For QC If Operating mode = OFFLINE/AUTOMATIC,Technical_status = RED/GREEN
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command value=SetCheOperatingMode<br>
   * cheId value=Name of the CHE for which opMode needs to be set<br
   * cheOpMode value= OFFLINEor AUTOMATIC
   * cheTeamsLocation= YARD.T1.001.1.1 , teams yard location in format of YARD.block.row.column.tier   <code> optional param </code>
   * @return JSON , <code>Che mode updated</code> if success, else<br <code>Che mode update failed</code>
   * @Postcondition
   * status_enum value for the given CHE should be updated by workflow to UNAVAIL/WORKING depends on the value set is OFFLINE/AUTOMATIC
   * @Example
   * Table invoked in TEAMS : asc_status/agv_status
   * Table triggered through workflow in SPARCS : xps_che
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="SetCheOperatingMode" /&gt;<br>
   * &lt;parameter id="cheId" value="CH01" /&gt;<br>
   * &lt;parameter id="cheOpMode" value="AUTOMATIC" /&gt;<br>
   * &lt;parameter id="cheTeamsLocation" value="YARD.T1.001.1.1" /&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String SetCheOperatingMode(Map inParameters) {
    assert inParameters.size() >= 3, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 3 or more parameters:
                                        <parameter id="command" value="SetCheOperatingMode" />
                                        <parameter id="cheId" value="<che Id>" />
                                        <parameter id="cheOpMode" value="<che operating mode>" />
                                        <parameter id="cheTeamsLocation" value="<teams yard location>" />'''

    String inCheId = _testCommandHelper.checkParameter('cheId', inParameters);
    String inCheOpMode = _testCommandHelper.checkParameter('cheOpMode', inParameters);
    String inTeamsLocation = ''
    String[] locationList
    if (inParameters.containsKey('cheTeamsLocation')) {
      inTeamsLocation = inParameters.get('cheTeamsLocation')
    };
    if (inTeamsLocation.contains(',')) {
      locationList = inTeamsLocation.split(',')
    }
    String[] cheList = inCheId.split(',')

    try {
      int index = 0
      boolean result = false;
      cheList.each {
        Che che = ((Che) HibernateApi.getInstance().getUniqueEntityByDomainQuery(QueryUtils.createDomainQuery(ArgoEntity.CHE)
                .addDqPredicate(PredicateFactory.eq(ArgoField.CHE_SHORT_NAME, it))));
        String cheType = che.cheKindEnum;
        if (che != null) {
          if (!inTeamsLocation.isEmpty()) {
            if (locationList != null) { //request contains multiple ches
              result = setStatusInDB(cheType, it, inCheOpMode, locationList.getAt(index));    //set multiple op mode at a time
              index++;
            } else   //request contains single che
            {
              result = setStatusInDB(cheType, it, inCheOpMode, inTeamsLocation)
            };    //set single op mode
          } else {  //cheTeamsLocation is empty
            result = setStatusInDB(cheType, it, inCheOpMode, "");
          }
          if (result) {
            returnString = 'Che mode updated';
            LOGGER.debug('Che mode updated :' + inCheId + ":" + inCheOpMode)
          } else {
            returnString = 'Che mode update failed';
            LOGGER.error('Che mode update failed :' + result)
          }
        }
      }
    } catch (Exception ex) {
      returnString = 'Che mode update failed:' + ex;
      LOGGER.error('Che mode update failed :' + ex)
    }
    builder {
      actual_result returnString;
    }
    return builder;
  }

  //Method to update the work and technical status in Teams Status table according to the
  // obtained che type and operating mode given.
  private boolean setStatusInDB(String cheType, String inCheId, String opMode, String location) {
    String technicalStatus, workStatus, tableName = "";
    if ("OFFLINE".equalsIgnoreCase(opMode)) {
      technicalStatus = "RED";
      workStatus = "UNDEFINED";
    } else if ("AUTOMATIC".equalsIgnoreCase(opMode)) {
      technicalStatus = "GREEN";
      workStatus = "IDLE";
    } else if ("MAINTENANCE".equalsIgnoreCase(opMode)) {
      technicalStatus = "RED";
      workStatus = "UNDEFINED";
      opMode = "OFFLINE"
    }
    tableName = findCheTableName(_testCommandHelper.getActualEnumValue(cheType));
    try {
      if ("asc_status".equalsIgnoreCase(tableName)) {
        final List qr = _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).updateForObject(
                String.format(
                        "merge " + tableName + " as tche " +
                                "using (select '" + inCheId + "' as che_id)  as source " +
                                "on tche.che_id = source.che_id " +
                                "when matched then " +
                                "update set " +
                                "tche.work_status = '" + workStatus + "', " +
                                "tche.technical_status = '" + technicalStatus + "', " +
                                "tche.operational_status = '" + opMode.toUpperCase() + "', " +
                                "tche.location = '" + location + "', " +
                                "tche.updated = SYSDATETIME()" +
                                " when not matched then " +
                                "insert (che_id, work_status, technical_status, operational_status, location, updated) values" +
                                "('" + inCheId + "','" + workStatus + "','" + technicalStatus + "','" + opMode.toUpperCase() + "','" + location + "', SYSDATETIME());"

                )
        );
      } else if ("agv_status".equalsIgnoreCase(tableName)) {
        final List qr = _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).updateForObject(
                String.format(
                        "merge " + tableName + " as tche " +
                                "using (select '" + inCheId + "' as che_id)  as source " +
                                "on tche.che_id = source.che_id " +
                                "when matched then " +
                                "update set " +
                                "tche.technical_status = '" + technicalStatus + "', " +
                                "tche.operational_status = '" + opMode.toUpperCase() + "', " +
                                "tche.location = '" + location + "', " +
                                "tche.updated = SYSDATETIME()" +
                                " when not matched then " +
                                "insert (che_id, technical_status, operational_status, location, updated,lift_capability,order_gkey,command_gkey,fuel_type) values" +
                                "('" + inCheId + "','" + technicalStatus + "','" + opMode.toUpperCase() + "','" + location + "', SYSDATETIME(),'YES',1,2,'BATTERY');"

                )
        );
      } else if ("qc_status".equalsIgnoreCase(tableName)) {
        final List qr = _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).updateForObject(
                String.format(
                        "merge " + tableName + " as tche " +
                                "using (select qc_id from " + tableName + " where qc_id = '" + inCheId + "') as source " +
                                "on tche.qc_id = source.qc_id " +
                                "when matched then " +
                                "update set " +
                                "tche.technical_status = '" + technicalStatus + "'," +
                                "tche.updated = SYSDATETIME()" +
                                " when not matched then " +
                                "insert " +
                                "(qc_id, technical_status, updated) values" +
                                "('" + inCheId + "','" + technicalStatus + "', SYSDATETIME());"
                )
        );
      }
    } catch (Exception ex) {
      return false;
    }
    return true;
  }

  /**
   * Finds AGV and updates each of the attribute with its value
   * This operates on agv_status table in TEAMS Database
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=SetCheAttribute<br>
   * cheId=CHE Id<br>
   * cheMetaFields=LIFT_CAPABILITY/BATTERY_STATE/REMAINING_FUEL/RUNNING_HOURS
   * @return JSON ,<code>CHE updated with given list of attribute/values</code> if success
   *               <code>CHE not updated with given list of attribute/values</code>
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="SetCheAttribute"/&gt;<br>
   * &lt;parameter id="cheId" value="ASC01"/&gt;<br>
   * &lt;parameter id="cheMetaFields" value="LIFT_CAPABILITY,FUEL_TYPE,RUNNING_HOURS,LOCATION,NEXT_LOCATION"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String SetCheAttribute(Map inParameters) {
    assert inParameters.size() == 3, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 3 parameters:
                                        <parameter id="command" value="SetCheAttribute" />
                                        <parameter id="cheId" value="<che Id>" />
                                        <parameter id="cheMetaFields" value="<che meta fields>" />'''

    String inCheId = _testCommandHelper.checkParameter('cheId', inParameters);
    String inCheMetaFields = _testCommandHelper.checkParameter('cheMetaFields', inParameters);
    String[] cheAttributeList = inCheMetaFields.split(',');
    try {
      Che che = ((Che) HibernateApi.getInstance().getUniqueEntityByDomainQuery(QueryUtils.createDomainQuery(ArgoEntity.CHE)
              .addDqPredicate(PredicateFactory.eq(ArgoField.CHE_SHORT_NAME, inCheId))));
      String cheType = che.cheKindEnum;
      if (che != null) {
        String tableName = findCheTableName(_testCommandHelper.getActualEnumValue(cheType));
        String key, value = "";
        String[] attributeArray = "";
        cheAttributeList.each {  //iterates each attribute in the list
          attributeArray = it.trim().split("=");
          if (attributeArray.size() == 2) {    //checks whether there are key and value tokens
            key = attributeArray[0].trim();
            value = attributeArray[1].trim();
            switch (key) {
              case 'LIFT_CAPABILITY': key = 'lift_capability'; break;
              case 'FUEL_TYPE': key = 'fuel_type'; break;
              case 'RUNNING_HOURS': key = 'running_hours'; break;
              case 'LOCATION': key = 'location'; break;
              case 'NEXT_LOCATION': key = 'next_location'; break;
            }
            _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).execute(
                    String.format(
                            "update " + tableName + " SET " + key + " = '" + value + "' where che_id = '" + inCheId + "'"
                    )
            );
            returnString = 'Che attributes updated';
          } else {
            returnString = 'Che attributes not updated - please check the input format'
          }
        }
      } else {
        returnString = 'Che attributes not updated - Vessel Visit is null'
      }
    } catch (Exception ex) {
      returnString = 'Che attributes not updated : ' + ex;
    }
    builder {
      actual_result returnString;
    }
    return builder;
  }

  /**
   * Gets the data from ASC Orders to ASC Commands table according to the given CHE Id.
   *
   * @Precondition
   * DispatchUnitToChe should be invoked before calling this method.<br>
   * An order should be inserted into ASC_Orders in teams
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=ASCAccept<br>
   * cheId=Name of the ASC which needs to accept the order
   * @return JSON , <code>ASC command accepted</code> if order is inserted into commands<br>
   *               <code>There are no ASC orders to accept</code> if there are no orders in ASC_ORDDERS or dispatch doesnt happen<br>
   *               <code>ASC command not accepted</code> fails
   * @Postcondition
   * Required work flow should be generated, this can be verified by taking the report using ReportEntities API.
   * @Example
   * Table invoked by TEAMS : asc_commands<br>
   * Table invoked by SPARCS (Work Flow) : xps_che,inv_wi,xps_workassignment,xps_ecevent<br>
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="ASCAccept"/&gt;<br>
   * &lt;parameter id="cheId" value="ASC01"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String ASCAccept(Map inParameters) {
    assert inParameters.size() >= 2, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 2 parameters:
                                        <parameter id="command" value="ASCAccept" />
                                        <parameter id="cheId" value="<asc Name>" />
                                        <parameter id="location" value="<location>" />'''

    String inCheId = _testCommandHelper.checkParameter('cheId', inParameters);
    String inToPosition = '', location = ''
    String commands_query = ''
    if (inParameters.containsKey('location')) {
      inToPosition = inParameters.get('location')
    }
    commands_query = "select che_id,order_gkey, order_version,command_version = command_version + 1, status,created=SYSDATETIME(),planned_destination,'PLANNED' from ASC_ORDERS" +
            " where che_id = '" + inCheId + "' and order_gkey = (select MAX(order_gkey) from ASC_ORDERS " +
            "where che_id = '" + inCheId + "') and status = 'ENTERED'";
    if (inToPosition != null) {
      if (!inToPosition.isEmpty()) {
        location = "'" + inToPosition + "'"
        commands_query = "select che_id,order_gkey, order_version,command_version = command_version + 1, status,created=SYSDATETIME()," + location + ",'PLANNED' from ASC_ORDERS" +
                " where che_id = '" + inCheId + "' and order_gkey = (select MAX(order_gkey) from ASC_ORDERS " +
                "where che_id = '" + inCheId + "') and status = 'ENTERED'";
      }
    }

    try {
      //check whether order is there in ASC_ORDERS table and its status is 'Entered'
      List qr = _testCommandHelper.getTeamsDbHelper("teams1").queryForList(
              String.format(
                      "select status from ASC_ORDERS where che_id = '" + inCheId + "' and order_gkey = (select MAX(order_gkey) from ASC_ORDERS " +
                              "where che_id = '" + inCheId + "')"
              )
      );
      if (qr.size() > 0 && qr.get(0).toString().contains('ENTERED')) {
        _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).execute(
                String.format(
                        "insert into asc_commands ( che_id,order_gkey, order_version, command_version, " +
                                "status,created,actual_destination,order_progress ) " + commands_query
                )
        );
        returnString = 'ASC command accepted';
        LOGGER.debug('ASC order inserted into ASCCommands for the ASC :' + inCheId)
      } else {
        LOGGER.warn('There are no ASCOrders when ASCMove is invoked' + inCheId)
        returnString = 'There are no ASC orders to accept';
      }
    } catch (Exception ex) {
      returnString = 'ASC command not accepted' + ex;
    }

    builder {
      actual_result returnString;
    }
    return builder;
  }

  /**
   * Moves the ASC to a different location and updates the status to 'Working' in asc_commands table in Teams<br>
   * Updates the location in asc_commands and asc_status with the given position, if the position is not given in<br>
   * input, then take the planned_destination value from asc_orders<br>
   * This triggers the work flow which updates the work assignment,work instruction,che,ecevents tables in N4.
   *
   * @Precondition
   * A command should be available in ASC_Commands in teams for the given CHE<br>
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=ASCMove<br>
   * cheId=Name of the ASC which requires a move<br>
   * location=to Position where ASC needs to be moved<br>
   * @return <code>ASC move done</code> if moved successfully<br>
   *         <code> ASC not moved</code> failed to move ASC
   * @Postcondition
   * Required work flow should be generated, this can be verified by taking the report using ReportEntities API.
   * @Example
   * Table invoked by TEAMS : asc_commands , asc_status<br>
   * Table invoked by SPARCS (Work Flow) : xps_che,inv_wi,xps_workassignment,xps_ecevent <br>
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="ASCMove"/&gt;<br>
   * &lt;parameter id="cheId" value="ASC01"/&gt;<br>
   * &lt;parameter id="toPosition" value="161001.1"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String ASCMove(Map inParameters) {
    assert inParameters.size() >= 2, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply atleast first two parameters:
                                        <parameter id="command" value="ASCMove" />
                                        <parameter id="cheId" value="<asc Name>" />
										<parameter id="location" value="<to Position>" />'''

    String inCheId = _testCommandHelper.checkParameter('cheId', inParameters);
    String inToPosition = inParameters.get('location');
    if (inToPosition != null) {
      if (inToPosition.isEmpty()) {
        inToPosition = "asc_orders.planned_destination"
      } //if pos is empty
      else {
        inToPosition = "'" + inToPosition + "'"
      }
    } else {
      inToPosition = "asc_orders.planned_destination"
    }
    String query = "(select MAX(order_gkey) from asc_orders where che_id = '" + inCheId + "')"
    List qr = _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).queryForList(
            String.format(
                    "(select status from asc_commands where order_gkey = " + query + " and status IN ('ENTERED','WORKING'))"
            )
    );
    if (qr.size() > 0 && (qr.get(0).toString().contains('ENTERED') || qr.get(0).toString().contains('WORKING'))) {
      try {
        _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).execute(
                String.format(
                        "update asc_commands set " +
                                "command_version = asc_commands.command_version+1, " +
                                "updated = SYSDATETIME()," +
                                "order_version = asc_orders.order_version," +
                                "status = 'WORKING'," +
                                "actual_destination = " + inToPosition +
                                " from asc_commands " +
                                "inner join asc_orders on asc_orders.order_gkey = asc_commands.order_gkey " +
                                "where asc_orders.che_id = '" + inCheId + "' and " +
                                "asc_orders.status IN ('ENTERED','WORKING')"
                )
        ); //update location in asc_status table in TEAMS
        _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).execute(
                String.format(
                        "update asc_status SET location = " + inToPosition + " from asc_status " +
                                "inner join asc_orders on asc_orders.che_id = asc_status.che_id where " +
                                "asc_orders.status IN ('ENTERED','WORKING') and asc_orders.che_id = '" + inCheId + "'"
                )
        );
        returnString = 'ASC move done';
        LOGGER.debug('ASC started moving towards the destination :' + inCheId)
      } catch (Exception ex) {
        returnString = 'ASC not moved ' + ex;
      }
    } else {
      returnString = 'There are no ASC orders'
      LOGGER.warn('There are no ASC Orders when ASCMove is invoked' + inCheId)
    }
    builder {
      actual_result returnString;
    }
    return builder;
  }

  /**
   * Lifts the ASC,updates the lift time in asc_commands table in Teams<br>
   * which in turn triggers the work flow which updates the Work instruction,Che,EC events tables in N4.
   *
   * @Precondition
   * A command should be available in ASC_Commands with status = 'WORKING' in teams for the given CHE<br>
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=ASCLift<br>
   * cheId=Name of the ASC which needs to be lifted<br>
   * unitId=unit Id
   * @return <code>ASC lift done</code> if moved successfully<br>
   *         <code>ASC not lifted</code> failed to move ASC
   * @Postcondition
   * Required work flow should be generated, this can be verified by taking the report using ReportEntities API.
   * @Example
   * Table invoked by TEAMS : asc_commands<br>
   * Table invoked by SPARCS (Work Flow) : xps_che,inv_wi,xps_workassignment,xps_ecevent<br>
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="ASCLift"/&gt;<br>
   * &lt;parameter id="cheId" value="ASC01"/&gt;<br>
   * &lt;parameter id="unitId" value="TEST0000001"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String ASCLift(Map inParameters) {
    assert inParameters.size() >= 2, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply atleast 2 parameters:
                                        <parameter id="command" value="ASCLift" />
                                        <parameter id="cheId" value="<cheId>" />
                                        <parameter id="unitId" value="<unit Id>" />'''

    String inCheId = _testCommandHelper.checkParameter('cheId', inParameters);
    try {
      //get wa report from the wa_ref in asc_orders
      def waReport = getWAReportfromWaRef(inCheId)
      List qr = _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).queryForList(
              String.format(
                      "select status from asc_commands where order_gkey = (select MAX(order_gkey) " +
                              "from ASC_ORDERS where che_id = '" + inCheId + "' and status = 'WORKING')"
              )
      );
      def ascTpResult = ''
      if (qr.size() > 0 && qr.get(0).toString().contains('WORKING')) {
        if (waReport.toString().contains('TRANSFER_IN')) {  //check if the movePurpose is Transfer_In - Vsl discharge
          Map<String, String> asc_tp = new HashMap<String, String>()
          asc_tp.put('command', 'ASCArriveAtTZ')
          asc_tp.put('cheId', inCheId)
          ascTpResult = ASCArriveAtTZ(asc_tp)
        }

        _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).execute(
                String.format(
                        "update asc_commands set " +
                                "command_version = asc_commands.command_version+1, " +
                                "updated = SYSDATETIME()," +
                                "order_version = asc_orders.order_version," +
                                "lift_time = SYSDATETIME()" +
                                " from asc_commands " +
                                "inner join asc_orders on asc_orders.order_gkey = asc_commands.order_gkey " +
                                "where asc_orders.che_id = '" + inCheId + "' and " +
                                "asc_orders.status = 'WORKING'"
                )
        );
        returnString = 'ASC lift done';
        LOGGER.debug('ASC lifts the container :' + inCheId)
        if (!ascTpResult.isEmpty()) {     //validate whether TRANSFER_POINT_ENTRY is updated when its an Inbound move
          if (!(ascTpResult.contains('ASC arrived at TZ'))) {
            returnString = 'ASC lift done - <Warning:TRANSFER_POINT_ENTRY not updated>'
            LOGGER.debug(returnString + ' : ' + inCheId)
          }
        }
      } else {
        LOGGER.warn('There are no ASC orders when ASCLift is invoked' + inCheId)
        returnString = 'There are no ASC orders'
      }
    } catch (Exception ex) {
      returnString = 'ASC not lifted' + ex;
    }
    builder {
      actual_result returnString;
    }
    return builder;
  }

  /**
   * Gets wa_ref from asc_orders for the corresponding che and reports the move purpose of the work assignment
   * @param inCheId
   * @return MovePurpose of the workassignment
   */
  private String getWAReportfromWaRef(String inCheId) {
    List wa_ref = _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).queryForList(
            String.format(
                    "select wa_ref from asc_orders where order_gkey = (select MAX(order_gkey)" +
                            "from ASC_COMMANDS where che_id = '" + inCheId + "' and status = 'WORKING')"
            )
    );
    def waRef = wa_ref.toString().substring(wa_ref.toString().indexOf(':') + 1, wa_ref.toString().indexOf(']'))
    Map<String, String> reportWA = new HashMap<String, String>()
    reportWA.put('command', 'ReportWorkAssignment')
    reportWA.put('waGKey', waRef)
    reportWA.put('waMetaFields', 'MOVE_PURPOSE')
    def waReport = new test.extension.groovy.TestCommandReport().ReportWorkAssignment(reportWA)

    return waReport
  }

  /**
   * Updates transfer_point_entry in asc_commands for inbound and outbound moves
   * @param inParameters
   * @return
   */
  public String ASCArriveAtTZ(Map inParameters) {
    assert inParameters.size() >= 2, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 2 parameters:
                                        <parameter id="command" value="ASCArriveAtTZ" />
                                        <parameter id="cheId" value="<cheId>" />'''

    String inCheId = _testCommandHelper.checkParameter('cheId', inParameters);
    try {
      List qr = _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).queryForList(
              String.format(
                      "select status from asc_commands where order_gkey = (select MAX(order_gkey) " +
                              "from ASC_ORDERS where che_id = '" + inCheId + "' and status = 'WORKING')"
              )
      );
      if (qr.size() > 0 && qr.get(0).toString().contains('WORKING')) {
        _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).execute(
                String.format(
                        "update asc_commands set " +
                                "command_version = asc_commands.command_version+1, " +
                                "updated = SYSDATETIME(), " +
                                "transfer_point_entry=SYSDATETIME()," +
                                "order_version = asc_orders.order_version " +
                                "from asc_commands " +
                                "inner join asc_orders on asc_orders.order_gkey = asc_commands.order_gkey " +
                                "where asc_orders.che_id = '" + inCheId + "'and " +
                                "asc_orders.status = 'WORKING'"
                )
        );
        sleep(2000)
        returnString = 'ASC arrived at TZ';
        LOGGER.debug('ASC arrived at TZ :' + inCheId)
      } else {
        LOGGER.warn('There are no ASC orders to arrive at TZ when ASCArriveAtTZ is invoked' + inCheId)
        returnString = 'There are no ASC orders'
      }
    } catch (Exception ex) {
      returnString = 'ASC set not done' + ex;
    }
    builder {
      actual_result returnString;
    }
    return builder;
  }

  /**
   * Completes the ASC job, which updates the status='Complete' in asc_commands table in Teams<br>
   * Updates the location in asc_commands with the given position if the position is given,<br>
   * else takes the planned_destination value from asc_orders <br>
   *
   * @Precondition
   * A command should be available in ASC_Commands with status = 'WORKING' in teams for the given CHE<br>
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=ASCSet<br>
   * cheId=Name of the ASC whose job needs to be completed<br>
   * location=to Position
   * @return <code>ASC set done</code> if success<br>
   *         <code>ASC set not done</code> failed to set ASC
   * @Postcondition
   * Required work flow should be generated, this can be verified by taking the report using ReportEntities API.
   * @Example
   * Table invoked by TEAMS : asc_commands <br>
   * Table invoked by SPARCS (Work Flow) : xps_che,inv_wi,xps_workassignment,xps_ecevent  <br>
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="ASCSet"/&gt;<br>
   * &lt;parameter id="cheId" value="ASC01"/&gt;<br>
   * &lt;parameter id="toPosition" value="YARD.AS09.22.A.A"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String ASCSet(Map inParameters) {
    assert inParameters.size() >= 2, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 2 parameters:
                                        <parameter id="command" value="ASCSet" />
                                        <parameter id="cheId" value="<cheId>" />
                                        <parameter id="location" value="<to Position>" />'''

    String inCheId = _testCommandHelper.checkParameter('cheId', inParameters);
    String inPosition = inParameters.get('location');
    String status = "COMPLETE";

    String query = "(select MAX(order_gkey) from asc_orders where che_id = '" + inCheId + "')"
    if (inPosition != null) {
      if (inPosition.isEmpty()) {
        inPosition = "asc_orders.planned_destination"
      } //if pos is empty
      else {
        inPosition = "'" + inPosition + "'"
      }
      if (inPosition.equalsIgnoreCase("Truck")) {
        status = "COMPLETE_DOOR";
      }
    } else {
      inPosition = "(select planned_destination from asc_orders where che_id ='" + inCheId + "' and order_gkey=" + query + ")"
    }

    try {
      //get wa report from the wa_ref in asc_orders
      def waReport = getWAReportfromWaRef(inCheId)

      List qr = _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).queryForList(
              String.format(
                      "select status from asc_commands where order_gkey = (select MAX(order_gkey) " +
                              "from ASC_ORDERS where che_id = '" + inCheId + "' and status = 'WORKING')"
              )
      );

      def ascTpResult = ''
      if (waReport.toString().contains('TRANSFER_OUT')) {  //check if the movePurpose is Transfer_Out - Vsl Load
        Map<String, String> asc_tp = new HashMap<String, String>()
        asc_tp.put('command', 'ASCArriveAtTZ')
        asc_tp.put('cheId', inCheId)
        ascTpResult = ASCArriveAtTZ(asc_tp)
      }

      if (qr.size() > 0 && qr.get(0).toString().contains('WORKING')) {
        _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).execute(
                String.format(
                        "update asc_commands set " +
                                "command_version = asc_commands.command_version+1, " +
                                "updated = SYSDATETIME(), " +
                                "set_time=SYSDATETIME()," +
                                "order_version = asc_orders.order_version," +
                                "actual_destination = " + inPosition +
                                ",status = '" + status + "'" +
                                "from asc_commands " +
                                "inner join asc_orders on asc_orders.order_gkey = asc_commands.order_gkey " +
                                "where asc_orders.che_id = '" + inCheId + "' and " +
                                "asc_orders.status = 'WORKING'"
                )
        );
        returnString = 'ASC set done';
        LOGGER.debug('ASC completes its move :' + inCheId)
        if (!ascTpResult.isEmpty()) {     //validate whether TRANSFER_POINT_ENTRY is updated when its an Inbound move
          if (!(ascTpResult.contains('ASC arrived at TZ'))) {
            returnString = 'ASC set done - <Warning:TRANSFER_POINT_ENTRY not updated>'
            LOGGER.debug(returnString + ' : ' + inCheId)
          }
        }
      } else {
        LOGGER.warn('There are no ASC orders to complete when ASCComplete is invoked' + inCheId)
        returnString = 'There are no ASC orders'
      }
    } catch (Exception ex) {
      returnString = 'ASC set not done' + ex;
    }
    builder {
      actual_result returnString;
    }
    return builder;
  }

  /**
   * Simulates the ASC Abort[Aborts can only be performed by the TEAMS],<br>
   * abort can happen because of these reasons :ABORTED_BY_OPERATOR, BLOCKMAP_ERROR, EXECUTION_FAILED
   *
   * @Precondition
   * A command should be available in ASC_Commands with status != 'COMPLETED' in teams for the given ASC<br>
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=ASCAbort<br>
   * cheId=Name of the ASC which needs to be aborted<br>
   * problemDesc=problem description for abort<br>
   * problemCode=abort problem code
   * @return <code>ASC aborted</code> if aborted successfully<br>
   *         <code>ASC order not aborted</code> failed to abort ASC
   * @Example
   * Table invoked by TEAMS : asc_commands
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="ASCAbort"/&gt;<br>
   * &lt;parameter id="cheId" value="ASC01"/&gt;<br>
   * &lt;parameter id="problemDesc" value="execution failure"/&gt;<br>
   * &lt;parameter id="problemType" value="EXECUTION_FAILED"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String ASCAbort(Map inParameters) {
    assert inParameters.size() >= 3, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 3 parameters:
                                        <parameter id="command" value="ASCAbort" />
                                        <parameter id="cheId" value="<cheId>" />
                                        <parameter id="problemDesc" value="<problem description for abort, a free text describing the abort reason>" />
                                        <parameter id="problemCode" value="BLOCKMAP_ERROR, EXECUTION_FAILED" />
                                        <parameter id="craneAbortCode" value="<problem description for abort, a free text describing the abort reason>" />
                                        <parameter id="craneAbortDescription" value="BLOCKMAP_ERROR, EXECUTION_FAILED" />'''

    String inCheId = _testCommandHelper.checkParameter('cheId', inParameters);
    String inProblemType = _testCommandHelper.checkParameter('problemCode', inParameters);
    String inProblemDesc = null;
    String inCraneAbortCode, inCraneAbortDescription;
    if (inParameters.containsKey('problemDesc')) {
      inProblemDesc = inParameters.get('problemDesc')
    }
    if (inProblemDesc == null) {
      inProblemDesc = inProblemType
    }

    if (inParameters.containsKey('craneAbortCode') && !inParameters.get('craneAbortCode').toString().isEmpty()) {
      inCraneAbortCode = inParameters.get('craneAbortCode')
    }

    if (inParameters.containsKey('craneAbortDescription') && !inParameters.get('craneAbortDescription').toString().isEmpty()) {
      inCraneAbortDescription = "'" + inParameters.get('craneAbortDescription') + "'"
    }


    try {
      String query = "(select MAX(order_gkey) from asc_orders where che_id = '" + inCheId + "')"
      List qr = _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).queryForList(
              String.format(
                      "select status from asc_commands where order_gkey = (select MAX(order_gkey) " +
                              "from ASC_ORDERS where che_id = '" + inCheId + "' and status IN ('ENTERED','WORKING'))"
              )
      );
      if (qr.size() > 0 && (qr.get(0).toString().contains('ENTERED') || qr.get(0).toString().contains('WORKING'))) {
        _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).execute(
                String.format(
                        "update asc_commands SET status = 'ABORTED' , CRANE_ABORT_CODE = " + inCraneAbortCode + ",CRANE_ABORT_DESCRIPTION = " + inCraneAbortDescription +
                                ",TEAMS_PROBLEM_TYPE='" + inProblemType + "' , TEAMS_PROBLEM_DESCRIPTION='" + inProblemDesc + "'," +
                                " command_version = command_version+1, updated = SYSDATETIME()" +
                                " where order_gkey = " + query +
                                " and status IN ('ENTERED','WORKING') and command_gkey = (select MAX(command_gkey) from asc_commands" +
                                " where order_gkey = " + query + " )"
                )
        );
        returnString = 'ASC aborted';
        LOGGER.debug('ASC job aborted :' + inCheId)
      } else {
        returnString = 'There are no ASC orders'
        LOGGER.warn('There are no ASC orders to complete when ASCAbort is invoked' + inCheId)
      }
    } catch (Exception ex) {
      returnString = 'ASC order not aborted ' + ex;
    }
    builder {
      actual_result returnString;
    }
    return builder;
  }

  /**
   * Rejects the ASC job.<br>
   * reject can happen because of these reasons :ASC_NOT_AVAILABLE, VALIDATION_ERROR, REEFER_ON_POWER
   *
   * @Precondition
   * A command should be available in ASC_Commands with status != 'COMPLETED' in teams for the given ASC
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=ASCReject<br>
   * cheId=Name of the ASC which needs to be rejected<br>
   * problemDesc=problem description for reject<br>
   * problemCode=reject problem code
   * @return <code>ASC rejected</code> if rejected successfully<br>
   *         <code>ASC order not rejected</code> failed to reject ASC
   * @Example
   * Table invoked by TEAMS : asc_commands
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="ASCReject"/&gt;<br>
   * &lt;parameter id="cheId" value="ASC01"/&gt;<br>
   * &lt;parameter id="problemDesc" value="ASC not available"/&gt;<br>
   * &lt;parameter id="problemType" value="ASC_NOT_AVAILABLE"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String ASCReject(Map inParameters) {
    assert inParameters.size() >= 3, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 3 parameters:
                                        <parameter id="command" value="ASCReject" />
                                        <parameter id="cheId" value="<cheId>" />
                                        <parameter id="problemDesc" value="<problem description for reject, free text descripting the reason for rejection>" />
                                        <parameter id="problemCode" value="ASC_NOT_AVAILABLE, VALIDATION_ERROR, REEFER_ON_POWER" />'''

    String inCheId = _testCommandHelper.checkParameter('cheId', inParameters);
    String inProblemType = _testCommandHelper.checkParameter('problemCode', inParameters);
    String inProblemDesc = null;
    if (inParameters.containsKey('problemDesc')) {
      inProblemDesc = inParameters.get('problemDesc')
    }
    if (inProblemDesc == null) {
      inProblemDesc = inProblemType
    }

    try {
      String query = "(select MAX(order_gkey) from asc_orders where che_id = '" + inCheId + "')"
      List qr = _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).queryForList(
              String.format(
                      "select status from asc_commands where order_gkey = (select MAX(order_gkey) " +
                              "from ASC_ORDERS where che_id = '" + inCheId + "' and status IN ('ENTERED','WORKING'))"
              )
      );
      if (qr.size() > 0 && (qr.get(0).toString().contains('ENTERED') || qr.get(0).toString().contains('WORKING'))) {
        _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).execute(
                String.format(
                        "update asc_commands SET status = 'REJECTED' , TEAMS_PROBLEM_TYPE='" + inProblemType + "' , TEAMS_PROBLEM_DESCRIPTION='" + inProblemDesc +
                                " ', command_version = command_version+1, updated = SYSDATETIME()" +
                                " where order_gkey = " + query +
                                " and status IN ('ENTERED','WORKING') and command_gkey = (select MAX(command_gkey) from asc_commands" +
                                " where order_gkey = " + query + " )"
                )
        );
        returnString = 'ASC rejected';
        LOGGER.debug('ASC job rejected :' + inCheId)
      } else {
        returnString = 'There are no ASC orders'
        LOGGER.warn('There are no ASC orders to complete when ASCReject is invoked' + inCheId)
      }
    } catch (Exception ex) {
      returnString = 'ASC order not rejected ' + ex;
    }
    builder {
      actual_result returnString;
    }
    return builder;
  }

  /**
   * Updates the work status for the given ASC in ASC_STATUS table in Teams
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=ASCWorkStatus<br>
   * cheId=Name of the ASC for which work status needs to be updated<br>
   * workStatus=SUSPENDED/UNDEFINED<br>
   * @return <code>ASC work status set</code> if work status set successfully<br>
   *         <code>ASC work status not set</code> failed to set work status
   * @Example
   * Table invoked by TEAMS : asc_status
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="ASCWorkStatus"/&gt;<br>
   * &lt;parameter id="cheId" value="ASC01"/&gt;<br>
   * &lt;parameter id="workStatus" value="SUSPENDED"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String ASCWorkStatus(Map inParameters) {
    assert inParameters.size() == 3, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 3 parameters:
                                        <parameter id="command" value="ASCWorkStatus" />
                                        <parameter id="cheId" value="<cheId>" />
                                        <parameter id="workStatus" value="SUSPENDED/UNDEFINED" />'''

    String inCheId = _testCommandHelper.checkParameter('cheId', inParameters);
    String inWorkStatus = _testCommandHelper.checkParameter('workStatus', inParameters);

    try {
      List query = _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).queryForList(
              String.format(
                      "select * from asc_status where che_id = '" + inCheId + "')"
              )
      );
      if (query.size() > 0) {
        _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).execute(
                String.format(
                        "update asc_status SET updated = SYSDATETIME(),WORK_STATUS = '" + inWorkStatus + "' where che_id = '" + inCheId +
                                "'"
                )
        );
        returnString = 'ASC Work Status set';
      } else {
        returnString = 'Please switch on CHE before invoking ASCWorkStatus '
      }
    } catch (Exception ex) {
      returnString = 'ASC Work Status not set ' + ex;
    }
    builder {
      actual_result returnString;
    }
    return builder;
  }

  /**
   * Updates the technical status for the given CHE in ASC/AGV status table in Teams.<br>
   * With the given CHE name it finds whether the CHE type is ASC,AGV or QC
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=CHETechStatus<br>
   * cheId=Name of th CHE for which technical status needs to be updated<br>
   * techStatus=SUSPENDED/UNDEFINED
   * @return <code>CHE Status Set</code> if status set successfully<br>
   *         <code>CHE Status not Set</code> failed to set CHE status
   * @Example
   * Table invoked by TEAMS : asc_status
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="CHETechStatus"/&gt;<br>
   * &lt;parameter id="cheId" value="ASC01"/&gt;<br>
   * &lt;parameter id="techStatus" value="BREAKDOWN"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String CHETechStatus(Map inParameters) {
    assert inParameters.size() == 3, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 3 parameters:
                                        <parameter id="command" value="CHETechStatus" />
                                        <parameter id="cheId" value="<cheId>" />
                                        <parameter id="techStatus" value="Breakdown(R)/Recover(O)/Working(G)/MALFUNCTION(Y) " />'''

    String inCheId = _testCommandHelper.checkParameter('cheId', inParameters);
    String inTechStatus = _testCommandHelper.checkParameter('techStatus', inParameters);

    try {
      Che che = ((Che) HibernateApi.getInstance().getUniqueEntityByDomainQuery(QueryUtils.createDomainQuery(ArgoEntity.CHE)
              .addDqPredicate(PredicateFactory.eq(ArgoField.CHE_SHORT_NAME, inCheId))));
      String tableName, techStatus = "";
      if (che != null) {
        String cheType = che.cheKindEnum;
        tableName = findCheTableName(_testCommandHelper.getActualEnumValue(cheType));
      }
      switch (inTechStatus) {
        case 'Breakdown':
        case 'BREAKDOWN':
          techStatus = "RED";
          break;
        case 'LowFuel':
        case 'LOWFUEL':
          techStatus = "ORANGE";
          break;
        case 'Working':
        case 'WORKING':
          techStatus = "GREEN";
          break;
        case 'Malfunction':
        case 'MALFUNCTION':
          techStatus = "YELLOW";
          break;
      }
      List query = _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).queryForList(
              String.format(
                      "(select * from " + tableName + " where che_id = '" + inCheId + "')"
              )
      );
      if (query.size() > 0) {
        _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).execute(
                String.format(
                        "update " + tableName + " SET updated = SYSDATETIME(), TECHNICAL_STATUS = '" + techStatus + "' where che_id = '" + inCheId +
                                "'"
                )
        );
        returnString = 'CHE Status set';
      } else {
        returnString = 'Please switch on CHE before invoking CHETechStatus '
      }
    } catch (Exception ex) {
      returnString = 'CHE Status not set ' + ex;
    }
    builder {
      actual_result returnString;
    }
    return builder;
  }

  /**
   * Gets the data from AGV Orders to AGV Commands table according to the given CHE Id.
   *
   * @Precondition
   * DispatchUnitToChe should be invoked before calling this method.<br>
   * An order should be inserted into AGV_Orders in teams
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=AGVAccept<br>
   * cheId=Name of the AGV which needs to accept the order
   * @return JSON , <code>AGV command accepted</code> if order is inserted into commands<br>
   *               <code>There are no AGV orders to accept</code> if there are no orders in AGV_ORDERS or dispatch doesn't happen<br>
   *               <code>AGV command not accepted</code> fails<br>
   * @Postcondition
   * Required work flow should be generated, this can be verified by taking the report using ReportEntities API.
   * @Example
   * Table invoked by TEAMS : agv_commands <br>
   * Table invoked by SPARCS (Work Flow) : xps_che,inv_wi,xps_workassignment,xps_ecevent  <br>
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="AGVAccept"/&gt;<br>
   * &lt;parameter id="cheId" value="AGV01"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String AGVAccept(Map inParameters) {
    assert inParameters.size() == 2, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 2 parameters:
                                        <parameter id="command" value="AGVAccept" />
                                        <parameter id="cheId" value="<agv Name>" />'''

    String inCheId = _testCommandHelper.checkParameter('cheId', inParameters);

    //check whether order is there in AGV_ORDERS table and its status is 'Entered'
    String query = "select status from AGV_ORDERS where che_id = '" + inCheId + "' and order_gkey = (select MAX(order_gkey) from AGV_ORDERS " +
            "where che_id = '" + inCheId + "') and status = 'ENTERED'";
    String commands_query = """select order_gkey, order_version,command_version = command_version + 1, status, created=SYSDATETIME() from AGV_ORDERS
                            where che_id = '""" + inCheId + "' and order_gkey = (select MAX(order_gkey) from AGV_ORDERS " +
            "where che_id = '" + inCheId + "') and status = 'ENTERED'";
    List qr = _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).queryForList(
            String.format(query)
    );
    if (qr.size() > 0 && (qr.get(0).toString().contains('ENTERED'))) {
      try {
        _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).execute(
                String.format(
                        "insert into agv_commands (  order_gkey, order_version, command_version, " +
                                " status,created ) " + commands_query
                )
        );
        returnString = 'AGV command accepted';
        LOGGER.debug('AGV order inserted into AGVCommands for the AGV :' + inCheId)
      } catch (Exception ex) {
        returnString = 'AGV command not accepted';
        LOGGER.error('AGV command not accepted : ' + ex)
      }
    } else {
      returnString = 'There are no AGV orders to accept';
      LOGGER.warn('There are no AGV orders to accept :' + inCheId)
    }
    builder {
      actual_result returnString;
    }
    return builder;
  }

  /**
   * Moves the AGV to a different location and updates the status to 'Working' in AGV_commands table in Teams<br>
   * which in turn triggers the work flow which updates the work assignment,work instruction,che,ecevents tables in N4.
   *
   * @Precondition
   * A command should be available in AGV_Commands in teams for the given CHE
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=AGVMove<br>
   * cheId=Name of the AGV which requires a move<br>
   * location=to Position where AGV needs to be moved
   * @return <code>AGV move done</code> if moved successfully<br>
   *         <code> AGV not moved</code> failed to move AGV
   * @Postcondition
   * Required work flow should be generated, this can be verified by taking the report using ReportEntities API.
   * @Example
   * Table invoked by TEAMS : agv_commands <br>
   * Table invoked by SPARCS (Work Flow) : xps_che,inv_wi,xps_workassignment,xps_ecevent  <br>
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="AGVMove"/&gt;<br>
   * &lt;parameter id="cheId" value="AGV01"/&gt;<br>
   * &lt;parameter id="location" value="161001.1"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String AGVMove(Map inParameters) {
    assert inParameters.size() >= 2, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 2 parameters:
                                        <parameter id="command" value="AGVMove" />
                                        <parameter id="cheId" value="<agv Name>" />
                                        <parameter id="toPosition" value="<toPosition>" />'''

    String inCheId = _testCommandHelper.checkParameter('cheId', inParameters);

    try {
      List qr = _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).queryForList(
              String.format(
                      "select status from agv_commands where order_gkey = (select MAX(order_gkey) " +
                              "from AGV_ORDERS where che_id = '" + inCheId + "' and status = 'ENTERED')"
              )
      );
      if (qr.size() > 0 && (qr.get(0).toString().contains('ENTERED'))) {
        _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).execute(
                String.format(
                        "update agv_commands set " +
                                "command_version = agv_commands.command_version+1, " +
                                "updated = SYSDATETIME(), " +
                                "order_version = agv_orders.order_version," +
                                "status = 'WORKING' " +
                                "from agv_commands " +
                                "inner join agv_orders on agv_orders.order_gkey = agv_commands.order_gkey " +
                                "where agv_orders.che_id = '" + inCheId + "' and " +
                                "agv_orders.status = 'ENTERED'"
                )
        );
        returnString = 'AGV move done';
        LOGGER.debug('AGV started moving towards the destination :' + inCheId)
      } else {
        returnString = 'There are no AGV orders'
        LOGGER.warn('There are no AGV orders when AGVMove is invoked' + inCheId)
      }
    } catch (Exception ex) {
      returnString = 'AGV not moved' + ex;
      LOGGER.error('Exception while AGV starts moving towards destination : ' + ex)
    }
    builder {
      actual_result returnString;
    }
    return builder;
  }

  /**
   * Makes the AGV arrives in the given destination which updates the destination value in asc_commands table in Teams DB,
   * In turn it triggers the work flow which updates the work assignment,Che,EC event tables in N4.
   *
   * @Precondition
   * A command should be available in AGV_Commands in teams for the given CHE
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=AGVArrive<br>
   * cheId=Name of the AGV which needs to be arrived in the toPosition<br>
   * toPosition=toPosition
   * @return <code>AGV arrived</code> if arrived successfully<br>
   *         <code>AGV not arrived</code> failed
   * @Postcondition
   * Required work flow should be generated, this can be verified by taking the report using ReportEntities API.
   * @Example
   * Table invoked by TEAMS : agv_commands <br>
   * Table invoked by SPARCS (Work Flow) : xps_che,inv_wi,xps_workassignment,xps_ecevent  <br>
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="AGVArrive"/&gt;<br>
   * &lt;parameter id="cheId" value="AGV01"/&gt;<br>
   * &lt;parameter id="toPosition" value="161001.1"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String AGVArrive(Map inParameters) {
    assert inParameters.size() >= 2, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 2 parameters:
                                        <parameter id="command" value="AGVArrive" />
                                        <parameter id="cheId" value="<agv Name>" />
                                        <parameter id="toPosition" value="<toPosition>" />'''

    String inCheId = _testCommandHelper.checkParameter('cheId', inParameters);
    String inToPosition = inParameters.get('toPosition');

    try {
      //If user does not give position, set it as planned pos.
      // This will cover positive cases.If user gives explicit position, use that. This will cover negative and explicit movement cases.
      if (inToPosition != null) {
        if (inToPosition.isEmpty()) {
          inToPosition = "agv_orders.planned_destination"
        } //if toPos is empty, take it from orders
        else {
          inToPosition = "'" + inToPosition + "'"
        }
      } else {
        inToPosition = "agv_orders.planned_destination"
      } // if toPosition is not supplied
      List qr = _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).queryForList(
              String.format(
                      "select status from agv_commands where order_gkey = (select MAX(order_gkey) " +
                              "from AGV_ORDERS where che_id = '" + inCheId + "' and status = 'WORKING')"
              )
      );
      if (qr.size() > 0 && (qr.get(0).toString().contains('WORKING'))) {
        _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).execute(
                String.format(
                        "update agv_commands set " +
                                "command_version = agv_commands.command_version+1, " +
                                "updated = SYSDATETIME()," +
                                "order_version = agv_orders.order_version," +
                                "time_at_destination = SYSDATETIME(), " +
                                "actual_destination = " + inToPosition +
                                " from agv_commands " +
                                "inner join agv_orders on agv_orders.order_gkey = agv_commands.order_gkey " +
                                "where agv_orders.che_id = '" + inCheId + "' and " +
                                "agv_orders.status = 'WORKING'"
                )
        );
        _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).execute(
                String.format(
                        "update agv_status SET location = " + inToPosition + " from agv_status " +
                                "inner join agv_orders on agv_orders.che_id = agv_status.che_id where " +
                                "agv_orders.status = 'WORKING'  and agv_orders.che_id = '" + inCheId + "'"
                )
        );
        returnString = 'AGV arrived';
        LOGGER.debug('AGV arrives near QC/ASC to receive/deliver the container :' + inCheId)
      } else {
        LOGGER.warn('There are no AGV orders when AGVArrive is invoked' + inCheId)
        returnString = 'There are no AGV orders'
      }
    } catch (Exception ex) {
      returnString = 'AGV not arrived' + ex;
      LOGGER.error('Exception while AGV arrives near the destination :' + inCheId + ": " + ex)
    }
    builder {
      actual_result returnString;
    }
    return builder;
  }

  /**
   * Completes the AGV job, which updates the status='Complete' in AGV_commands table in Teams<br>
   * which in turn triggers the work flow which updates the Work assignment,work instruction tables in N4.
   *
   * @Precondition
   * A command should be available in AGV_Commands with status = 'WORKING' in teams for the given CHE
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=AGVComplete<br>
   * cheId=Name of the AGV for which job needs to be completed<br>
   * location=to Position
   * @return <code>AGV complete done</code> if success<br>
   *         <code>AGV complete not done</code> failed to complete AGV
   * @Postcondition
   * Required work flow should be generated, this can be verified by taking the report using ReportEntities API.
   * @Example
   * Table invoked by TEAMS : agv_commands <br>
   * Table invoked by SPARCS (Work Flow) : xps_che,inv_wi,xps_workassignment,xps_ecevent  <br>
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="AGVComplete"/&gt;<br>
   * &lt;parameter id="cheId" value="AGV01"/&gt;<br>
   * &lt;parameter id="toPosition" value="TRUCK"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String AGVComplete(Map inParameters) {
    assert inParameters.size() >= 2, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 2 parameters:
                                        <parameter id="command" value="AGVComplete" />
                                        <parameter id="cheId" value="<agv Name>" />
                                        <parameter id="containerRefId1" value="<Container Id 1 in case of Twin>" />
                                        <parameter id="containerRefId2" value="<Container Id 2 in case of Twin>" />'''

    String inCheId = _testCommandHelper.checkParameter('cheId', inParameters);
    String inContainerRefId1, inContainerRefId2
    if (inParameters.containsKey('containerRefId1') && !inParameters.get('containerRefId1').toString().isEmpty()) {
      inContainerRefId1 = "'" + inParameters.get('containerRefId1') + "'"
    }

    if (inParameters.containsKey('containerRefId2') && !inParameters.get('containerRefId2').toString().isEmpty()) {
      inContainerRefId2 = "'" + inParameters.get('containerRefId2') + "'"
    }

    //handle population containerrefIds internally if it is not provided as an input
    if (inContainerRefId1 == null) {
      List wa_ref = _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).queryForList(
              String.format(
                      "select wa_ref from agv_orders where order_gkey = (select MAX(order_gkey)" +
                              "from AGV_COMMANDS where che_id = '" + inCheId + "' and status = 'WORKING')"
              )
      );
      println 'wa_ref' + wa_ref
      if(!wa_ref.isEmpty()) {
        def waRef = wa_ref.toString().substring(wa_ref.toString().indexOf(':') + 1, wa_ref.toString().indexOf(']'))
        if (waRef != null) {
          WorkInstruction workInstruction = ((WorkInstruction) HibernateApi.getInstance().getUniqueEntityByDomainQuery(QueryUtils.createDomainQuery(MovesEntity.WORK_INSTRUCTION)
                  .addDqPredicate(PredicateFactory.eq(MovesField.WI_ITV_WORK_ASSIGNMENT, new BigInteger(waRef)))));
          if (workInstruction != null) { //get unitid from wi_ufv
            if (workInstruction.wiMoveKind.equals(WiMoveKindEnum.VeslDisch)) {  //populate containerId only in case of discharge
              Unit unit = ((Unit) HibernateApi.getInstance().getUniqueEntityByDomainQuery(QueryUtils.createDomainQuery(InventoryEntity.UNIT)
                      .addDqPredicate(PredicateFactory.eq(InventoryField.UNIT_ACTIVE_UFV, new BigInteger(workInstruction.wiUfv.ufvGkey)))));
              if (unit != null) {
                inContainerRefId1 = "'" + unit.unitId + "_REF'"
              }
            }
          }
        }
      }
    }

    try {
      List qr = _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).queryForList(
              String.format(
                      "select status from agv_commands where order_gkey = (select MAX(order_gkey) " +
                              "from AGV_ORDERS where che_id = '" + inCheId + "' and (status = 'WORKING' or status='CANCELED'))"
              )
      );
      if (qr.size() > 0 && (qr.get(0).toString().contains('WORKING') || qr.get(0).toString().contains('CANCELED'))) {
        _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).execute(
                String.format(
                        "update agv_commands set " +
                                "command_version = agv_commands.command_version+1, " +
                                "updated = SYSDATETIME(), " +
                                "time_at_destination = SYSDATETIME(), " +
                                "order_version = agv_orders.order_version," +
                                "status = 'COMPLETE' , qc_reference_id_1 = " + inContainerRefId1 + ", qc_reference_id_2 = " + inContainerRefId2 +
                                " from agv_commands " +
                                "inner join agv_orders on agv_orders.order_gkey = agv_commands.order_gkey " +
                                "where agv_orders.che_id = '" + inCheId + "' and " +
                                "(agv_orders.status = 'WORKING' or agv_orders.status = 'CANCELED')"
                )
        );
        returnString = 'AGV complete done';
        LOGGER.debug('AGV completes its job :' + inCheId)
      } else {
        LOGGER.warn('There are no AGV orders to complete when AGVComplete is invoked' + inCheId)
        returnString = 'There are no AGV orders'
      }
    } catch (Exception ex) {
      returnString = 'AGV complete not done' + ex;
      LOGGER.error('Exception while completing AGV move :' + inCheId + ": " + ex)
    }

    builder {
      actual_result returnString;
    }
    return builder;
  }

  /**
   * Aborts the AGV job[Aborts can only be performed by the TEAMS],<br>
   * Aborts are performed for an order in progress when something negatively affected the order.<br>
   * abort can happen because of the below reasons :<br> EXECUTION_FAILED
   *
   * @Precondition
   * A command should be available in AGV_Commands with status != 'COMPLETED' in teams for the given AGV<br>
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=AGVAbort<br>
   * cheId=Name of the AGV which needs to be aborted<br>
   * problemCode=problem code for the abort
   * problemDesc=problem description for abort<br>  <code> optional param </code>
   * @return <code>AGV aborted</code> if aborted successfully<br>
   *         <code>AGV order not aborted</code> failed to abort AGV
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="AGVAbort"/&gt;<br>
   * &lt;parameter id="cheId" value="AGV01"/&gt;<br>
   * &lt;parameter id="problemCode" value="EXECUTION_FAILED"/&gt;<br>
   * &lt;parameter id="problemDesc" value="aborted"/&gt;<br> <code> optional param </code>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String AGVAbort(Map inParameters) {
    assert inParameters.size() >= 3, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 3 parameters:
                                        <parameter id="command" value="AGVAbort" />
                                        <parameter id="cheId" value="<cheId>" />
                                        <parameter id="problemCode" value="EXECUTION_FAILED" />
                                        <parameter id="problemDesc" value="<problem description for abort>" />'''

    String inCheId = _testCommandHelper.checkParameter('cheId', inParameters);
    String inProblemCode = _testCommandHelper.checkParameter('problemCode', inParameters);
    String inProblemDesc;
    if (inParameters.containsKey('problemDesc')) {
      inProblemDesc = inParameters.get('problemDesc')
    }
    if (inProblemDesc == null) {
      inProblemDesc = inProblemCode
    }

    try {
      String query = "(select MAX(order_gkey) from agv_orders where che_id = '" + inCheId + "')"
      List qr = _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).queryForList(
              String.format(
                      "select status from agv_commands where order_gkey = (select MAX(order_gkey) " +
                              "from AGV_ORDERS where che_id = '" + inCheId + "' and status IN ('ENTERED','WORKING'))"
              )
      );
      if (qr.size() > 0 && (qr.get(0).toString().contains('ENTERED') || (qr.get(0).toString().contains('WORKING')))) {
        _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).execute(
                String.format(
                        "update agv_commands SET status = 'ABORTED', command_version = command_version+1, updated = SYSDATETIME()," +
                                " problem_description='" + inProblemDesc + "',problem_code='" + inProblemCode + "' where order_gkey = " + query +
                                " and status IN ('ENTERED','WORKING') and command_gkey = (select MAX(command_gkey) from agv_commands" +
                                " where order_gkey = " + query + " )"
                )
        );
        returnString = 'AGV aborted';
        LOGGER.debug('AGV job aborted :' + inCheId)
      } else {
        returnString = 'There are no AGV orders'
        LOGGER.warn('There are no AGV orders to complete when AGVAbort is invoked' + inCheId)
      }
    } catch (Exception ex) {
      returnString = 'AGV order not aborted';
      LOGGER.error('Exception while aborting AGV job :' + inCheId + ": " + ex)
    }
    builder {
      actual_result returnString;
    }
    return builder;
  }

  /**
   * Simulates the AGV Reject[Rejects can only be performed by the TEAMS],<br>
   * Rejections are performed when TEAMS cannot accept order<br>
   * it can happen because of the below reasons :<br>
   * DESTINATION_INVALID/ROUTE_REJECTED
   *
   * @Precondition
   * A command should be available in AGV_Commands with status != 'COMPLETED' in teams for the given AGV
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=AGVReject<br>
   * cheId=Name of the AGV which needs to be rejected <br>
   * problemCode=problem code for the rejection of order
   * problemDesc=problem description for reject  <code> optional param </code>
   * @return <code>AGV rejected</code> if rejected successfully<br>
   *         <code>AGV order not rejected</code> failed to reject AGV
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="AGVReject"/&gt;<br>
   * &lt;parameter id="cheId" value="AGV01"/&gt;<br>
   * &lt;parameter id="problemCode" value="DESTINATION_INVALID"/&gt;<br>
   * &lt;parameter id="problemDesc" value="Rejected"/&gt;<br> <code> optional param </code>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String AGVReject(Map inParameters) {
    assert inParameters.size() >= 3, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 3 parameters:
                                        <parameter id="command" value="AGVReject" />
                                        <parameter id="cheId" value="<cheId>" />
                                        <parameter id="problemCode" value="<problem code for reject>" />
                                        <parameter id="problemDesc" value="<problem description for reject>" />'''

    String inCheId = _testCommandHelper.checkParameter('cheId', inParameters);
    String inProblemCode = _testCommandHelper.checkParameter('problemCode', inParameters);
    String inProblemDesc;
    if (inParameters.containsKey('problemDesc')) {
      inProblemDesc = inParameters.get('problemDesc')
    }
    if (inProblemDesc == null) {
      inProblemDesc = inProblemCode
    }

    try {
      String query = "(select MAX(order_gkey) from agv_orders where che_id = '" + inCheId + "')"
      List qr = _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).queryForList(
              String.format(
                      "select status from agv_commands where order_gkey = (select MAX(order_gkey) " +
                              "from AGV_ORDERS where che_id = '" + inCheId + "' and status IN ('ENTERED','WORKING'))"
              )
      );
      if (qr.size() > 0 && (qr.get(0).toString().contains('ENTERED') || (qr.get(0).toString().contains('WORKING')))) {
        _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).execute(
                String.format(
                        "update agv_commands SET status = 'REJECTED', command_version = command_version+1, updated = SYSDATETIME()," +
                                " problem_description='" + inProblemDesc + "',problem_code='" + inProblemCode + "' where order_gkey = " + query +
                                " and status IN ('ENTERED','WORKING') and command_gkey = (select MAX(command_gkey) from agv_commands" +
                                " where order_gkey = " + query + " )"
                )
        );
        returnString = 'AGV rejected';
        LOGGER.debug('AGV job rejected :' + inCheId)
      } else {
        returnString = 'There are no AGV orders';
        LOGGER.warn('There are no AGV orders to reject when AGVReject is invoked' + inCheId)
      }
    } catch (Exception ex) {
      returnString = 'AGV order not rejected';
      LOGGER.error('Exception while rejecting AGV job :' + inCheId + ": " + ex)
    }
    builder {
      actual_result returnString;
    }
    return builder;
  }

  public String AGVCancel(Map inParameters) {
    assert inParameters.size() >= 2, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 3 parameters:
                                        <parameter id="command" value="AGVCancel" />
                                        <parameter id="cheId" value="<cheId>" />
                                        <parameter id="problemCode" value="<problem code for reject>" />
                                        <parameter id="problemDesc" value="<problem description for reject>" />'''

    String inCheId = _testCommandHelper.checkParameter('cheId', inParameters);
    String inProblemCode;
    String inProblemDesc;
    if (inParameters.containsKey('problemDesc')) {
      inProblemDesc = inParameters.get('problemDesc')
    }
    if (inParameters.containsKey('problemCode')) {
      inProblemCode = inParameters.get('problemCode')
    }
    if (inProblemDesc == null) {
      inProblemDesc = inProblemCode
    }

    try {
      String query = "(select MAX(order_gkey) from agv_orders where che_id = '" + inCheId + "')"
      List qr = _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).queryForList(
              String.format(
                      "select status from agv_commands where order_gkey = (select MAX(order_gkey) " +
                              "from AGV_ORDERS where che_id = '" + inCheId + "' and status IN ('ENTERED','WORKING'))"
              )
      );
      if (qr.size() > 0 && (qr.get(0).toString().contains('ENTERED') || (qr.get(0).toString().contains('WORKING')))) {
        _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).execute(
                String.format(
                        "update agv_commands SET status = 'CANCELED', command_version = command_version+1, updated = SYSDATETIME()," +
                                " problem_description='" + inProblemDesc + "',problem_code='" + inProblemCode + "' where order_gkey = " + query +
                                " and status IN ('ENTERED','WORKING') and command_gkey = (select MAX(command_gkey) from agv_commands" +
                                " where order_gkey = " + query + " )"
                )
        );
        returnString = 'AGV canceled';
        LOGGER.debug('AGV job canceled :' + inCheId)
      } else {
        returnString = 'There are no AGV orders';
        LOGGER.warn('There are no AGV orders to cancel when AGVCancel is invoked' + inCheId)
      }
    } catch (Exception ex) {
      returnString = 'AGV order not canceled';
      LOGGER.error('Exception while cancelling AGV job :' + inCheId + ": " + ex)
    }
    builder {
      actual_result returnString;
    }
    return builder;
  }

  /**
   * Gets the cancelled order data from AGV Orders to AGV Commands table according to the given CHE Id.
   *
   * @Precondition
   * DispatchUnitToChe should be invoked before calling this method or scheduler should be running in the background to dispatch jobs.<br>
   * An order should be inserted into AGV_Orders in teams
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=AcceptAGVCancelledOrder<br>
   * cheId=Name of the AGV which needs to accept the order
   * @return JSON , <code>AGV accepted the cancel triggered by SPARCS</code> if order is inserted into commands<br>
   *               <code>There are no AGV orders in Working state</code> if there are no orders in AGV_ORDERS or dispatch doesn't happen<br>
   *               <code>AGV command not cancelled</code> fails<br>
   * @Postcondition
   * Required work flow should be generated, this can be verified by taking the report using ReportEntities API.
   * @Example
   * Table invoked by TEAMS : agv_commands <br>
   * Table invoked by SPARCS (Work Flow) : xps_che,inv_wi,xps_workassignment,xps_ecevent  <br>
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="AcceptAGVCancelledOrder"/&gt;<br>
   * &lt;parameter id="cheId" value="AGV01"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String AcceptAGVCancelledOrder(Map inParameters) {
    assert inParameters.size() >= 2, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 2 parameters:
                                        <parameter id="command" value="AcceptAGVCancelledOrder" />
                                        <parameter id="cheId" value="<agv Name>" />
                                        <parameter id="toPosition" value="<toPosition>" />'''

    String inCheId = _testCommandHelper.checkParameter('cheId', inParameters);

    try {
      List qr = _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).queryForList(
              String.format(
                      "select status from agv_commands where order_gkey = (select MAX(order_gkey) " +
                              "from AGV_ORDERS where che_id = '" + inCheId + "' and status = 'CANCELED')"
              )
      );
      if (qr.size() > 0 && (qr.get(0).toString().contains('ENTERED') || qr.get(0).toString().contains('WORKING'))) {
        _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).execute(
                String.format(
                        "update agv_commands set " +
                                "command_version = agv_commands.command_version+1, " +
                                "updated = SYSDATETIME(), " +
                                "order_version = agv_orders.order_version," +
                                "status = 'CANCELED' " +
                                "from agv_commands " +
                                "inner join agv_orders on agv_orders.order_gkey = agv_commands.order_gkey " +
                                "where agv_orders.che_id = '" + inCheId + "' and " +
                                "agv_orders.status = 'CANCELED'"
                )
        );
        returnString = 'AGV accepted the cancel triggered by SPARCS';
        LOGGER.debug('AGV accepted the cancel triggered by SPARCS :' + inCheId)
      } else {
        returnString = 'There are no AGV orders in Working state'
        LOGGER.warn('There are no AGV orders in Working state' + inCheId)
      }
    } catch (Exception ex) {
      returnString = 'AGV command not cancelled' + ex;
      LOGGER.error('Exception while cancelling AGV order : ' + ex)
    }
    builder {
      actual_result returnString;
    }
    return builder;
  }

  /**
   * Gets the cancelled order data from AGV Orders to ASC Commands table according to the given CHE Id.
   *
   * @Precondition
   * DispatchUnitToChe should be invoked before calling this method or scheduler should be running in the background to dispatch jobs.<br>
   * An order should be inserted into AGV_Orders in teams
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=AcceptASCCancelledOrder<br>
   * cheId=Name of the ASC which needs to accept the order
   * @return JSON , <code>ASC accepted the cancel triggered by SPARCS</code> if order is inserted into commands<br>
   *               <code>There are no ASC orders in Working state</code> if there are no orders in ASC_ORDERS or dispatch doesn't happen<br>
   *               <code>ASC command not cancelled</code> fails<br>
   * @Postcondition
   * Required work flow should be generated, this can be verified by taking the report using ReportEntities API.
   * @Example
   * Table invoked by TEAMS : asc_commands <br>
   * Table invoked by SPARCS (Work Flow) : xps_che,inv_wi,xps_workassignment,xps_ecevent  <br>
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="AcceptASCCancelledOrder"/&gt;<br>
   * &lt;parameter id="cheId" value="AGV01"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String AcceptASCCancelledOrder(Map inParameters) {
    assert inParameters.size() >= 2, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 2 parameters:
                                        <parameter id="command" value="AcceptASCCancelledOrder" />
                                        <parameter id="cheId" value="<asc Name>" />
                                        <parameter id="toPosition" value="<toPosition>" />'''

    String inCheId = _testCommandHelper.checkParameter('cheId', inParameters);

    try {
      List qr = _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).queryForList(
              String.format(
                      "select status from asc_commands where order_gkey = (select MAX(order_gkey) " +
                              "from ASC_ORDERS where che_id = '" + inCheId + "' and status = 'CANCELED')"
              )
      );
      if (qr.size() > 0 && (qr.get(0).toString().contains('ENTERED') || qr.get(0).toString().contains('WORKING'))) {
        _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).execute(
                String.format(
                        "update asc_commands set " +
                                "command_version = asc_commands.command_version+1, " +
                                "updated = SYSDATETIME(), " +
                                "order_version = asc_orders.order_version," +
                                "status = 'CANCELED' " +
                                "from asc_commands " +
                                "inner join asc_orders on asc_orders.order_gkey = asc_commands.order_gkey " +
                                "where asc_orders.che_id = '" + inCheId + "' and " +
                                "asc_orders.status = 'CANCELED'"
                )
        );
        returnString = 'ASC accepted the cancel triggered by SPARCS';
        LOGGER.debug('ASC accepted the cancel triggered by SPARCS :' + inCheId)
      } else {
        returnString = 'There are no ASC orders in Working state'
        LOGGER.warn('There are no ASC orders in Working state' + inCheId)
      }
    } catch (Exception ex) {
      returnString = 'ASC command not cancelled' + ex;
      LOGGER.error('Exception while cancelling ASC order : ' + ex)
    }
    builder {
      actual_result returnString;
    }
    return builder;
  }

  /**
   * Generic API call to fecth data from DB for any entity in TEAMS using sql query
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=ExecuteECSQuery<br>
   * sqlStatement=SQL query<br>
   * @return <code>Executed Query</code> if executed successfully<br>
   *         <code>Query not executed</code> failed to execute query
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="ExecuteECSQuery"/&gt;<br>
   * &lt;parameter id="sqlStatement" value="select * from asc_commands/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String ExecuteECSQuery(Map inParameters) {
    assert inParameters.size() == 2, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply atleast first two parameters:
                                        <parameter id="command" value="ExecuteECSQuery" />
                                        <parameter id="sqlStatement" value="sql statement" />'''

    String inSQLStatement = _testCommandHelper.checkParameter('sqlStatement', inParameters);


    try {
      if (inSQLStatement.contains('SELECT') || inSQLStatement.contains('select')) {
        List qr = _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).queryForList(
                String.format(inSQLStatement)
        );
        returnString = 'Executed query succesfully ' + qr;
      } else {
        _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).execute(
                String.format(inSQLStatement)
        );
        returnString = 'Executed Query';
      }
    } catch (Exception ex) {
      returnString = 'Query not executed'
    }

    builder {
      actual_result returnString;
    }
    return builder;
  }

  /**
   * Starts the QC job
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=QCInit<br>
   * QCName=Name of the QC<br>
   * containerId=Name of the container to be lifted by QC
   * currentLocation=new location of the container
   * moveKind - Load,Discharge
   * @return JSON ,
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="QCInit"/&gt;<br>
   * &lt;parameter id="QCName" value="QC01"/&gt;<br>
   * &lt;parameter id="containerId" value="TEST0000001"/&gt;<br>
   * &lt;parameter id="currentLocation" value=""/&gt;<br>
   * &lt;parameter id="moveKind" value="Load,Discharge"/&gt;<br>
   * &lt;parameter id="measuredWtKg" value="20"/&gt;<br>   //optional
   * &lt;parameter id="referenceId" value="unique value"/&gt;<br>   //optional , reqd in case of APRON involved
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String QCInit(Map inParameters) {
    assert inParameters.size() >= 4, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 4 parameters:
                                        <parameter id="command" value="QCInit" />
                                        <parameter id="QCName" value="<QC Id>" />
                                        <parameter id="containerId" value="<Container Id>" />
                                        <parameter id="currentLocation" value="QCSPREADER.QC101.M.C.C" />
										<parameter id="previousLocation" value="VESSEL.ADAM_11.19.01.82" />
                                        <parameter id="moveKind" value="Load,Discharge" />
                                        <parameter id="measuredWtKg" value="20" /> //optional
                                        <parameter id="transId" value="100" />
                                        <parameter id="referenceId" value="unique id" /> //optional, reqd in case of APRON involved
                                        '''

    String inQcId = _testCommandHelper.checkParameter('QCName', inParameters);
    String inContainerId = inParameters.get('containerId');
    String inCurrentLocation = _testCommandHelper.checkParameter('currentLocation', inParameters);
    String inPreviousLocation = _testCommandHelper.checkParameter('previousLocation', inParameters);
    String inMoveKind = _testCommandHelper.checkParameter('moveKind', inParameters);
    String presumedContainerId = ''
    String isoCode = ''
    def containerLength = 0
    def referenceId =  inContainerId + "_REF"
    int measuredWtKg
    int transId = 100
    if (inMoveKind.equalsIgnoreCase('Load')) {
      presumedContainerId = "'" + inContainerId + "'"
    } else if (inMoveKind.equalsIgnoreCase('Discharge') || inMoveKind.equalsIgnoreCase('SHOB'))     // set it as null in discharge scenario
    {
      presumedContainerId = "NULL"
    } else {
      returnString = 'QCInit not done : Invalid Move'
      builder {
        actual_result returnString;
      }
      return builder;
    }

    if (inParameters.containsKey('measuredWtKg') && !inParameters.get('measuredWtKg').toString().isEmpty()) {
      measuredWtKg = inParameters.get('measuredWtKg').toString().toInteger()
    }

    if (inParameters.containsKey('transId') && !inParameters.get('transId').toString().isEmpty()) {
      transId = inParameters.get('transId').toString().toInteger()
    }

    if (inParameters.containsKey('referenceId') && !inParameters.get('referenceId').toString().isEmpty()) {
      referenceId = inParameters.get('referenceId').toString()
    }

    //Find the ufv
    UnitFinder finder = (UnitFinder) Roastery.getBean(UnitFinder.BEAN_ID);
    SearchResults results = finder.findUfvByDigits(inContainerId, false, false);
    if (results.getFoundCount() > 0) {
      UnitFacilityVisit ufv = (UnitFacilityVisit) HibernateApi.getInstance().load(UnitFacilityVisit.class, results.getFoundPrimaryKey());
      containerLength = ControlTestUtils.getUnitNominalLength(ufv)

      try {
        Che che = ((Che) HibernateApi.getInstance().getUniqueEntityByDomainQuery(QueryUtils.createDomainQuery(ArgoEntity.CHE)
                .addDqPredicate(PredicateFactory.eq(ArgoField.CHE_SHORT_NAME, inQcId))
                .addDqPredicate(PredicateFactory.eq(ArgoField.CHE_KIND_ENUM, CheKindEnum.QC))));
        if (che != null) {
          che.setCheStatusEnum(CheStatusEnum.WORKING);
          che.setCheStatus(1L);

          _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).execute(
                  String.format(
                          "insert into qc_commands (  order_version, \n" +
                                  "  command_version, \n" +
                                  "  qc_id, \n" +
                                  "  status,\n" +
                                  "  reference_id,\n" +
                                  "  transaction_id,\n" +
                                  "  presumed_container_id,\n" +
                                  "  container_last_location, \n" +
                                  "  container_curr_location,\n" +
                                  "  created,updated,container_length,measured_weight_kg ) values ( 0, 1,'" + inQcId + "','WORKING','" + referenceId +  "'" +
                                  "  ," + transId + ", " + presumedContainerId + " ,'" + inPreviousLocation + "','" + inCurrentLocation + "',SYSDATETIME(),SYSDATETIME()" +
                                  "  ,'" + containerLength + "'," + measuredWtKg + " ) "

                  )
          );
          returnString = 'QC init done';
        } else {
          returnString 'QC init not done-QC not found'
        }
      } catch (Exception ex) {
        returnString = 'QC init not done' + ex;
      }
    } else {
      returnString = 'QC init not done - container Id not valid '
    }
    LOGGER.debug('QC : ' + inQcId + 'job initiated for the container : ' + inContainerId + ":" + returnString)
    builder {
      actual_result returnString;
    }
    return builder;
  }

  /**
   * Sets QC to the new position
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=QCSet<br>
   * QCName=Name of the QC<br>
   * containerId=Name of the container to be lifted by QC
   * @return JSON ,
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="QCSet"/&gt;<br>
   * &lt;parameter id="QCName" value="QC01"/&gt;<br>
   * &lt;parameter id="containerId" value="TEST0000001"/&gt;<br>
   * &lt;parameter id="currentLocation" value=""/&gt;<br>
   * &lt;parameter id="referenceId" value="unique value"/&gt;<br>   //optional , reqd in case of APRON involved
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String QCSet(Map inParameters) {
    assert inParameters.size() >= 4, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 4 parameters:                                        <parameter id="command" value="QCSet" />
                                        <parameter id="QCName" value="<QC Id>" />
                                        <parameter id="containerId" value="<Container Id>" />
                                        <parameter id="currentLocation" value="<container current location>" />
                                        <parameter id="transId" value="100" />
                                        <parameter id="referenceId" value="unique id" /> //optional, reqd in case of APRON involved'''

    String inQcId = _testCommandHelper.checkParameter('QCName', inParameters);
    String inContainerId = _testCommandHelper.checkParameter('containerId', inParameters);
    String inCurrentLocation = _testCommandHelper.checkParameter('currentLocation', inParameters);

    def referenceId =  inContainerId + "_REF"
    int transId = 100
    try {
      if (inParameters.containsKey('transId') && !inParameters.get('transId').toString().isEmpty()) {
        transId = inParameters.get('transId').toString().toInteger()
      }

      if (inParameters.containsKey('referenceId') && !inParameters.get('referenceId').toString().isEmpty()) {
        referenceId = inParameters.get('referenceId').toString()
      }
      _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).execute(
              String.format(
                      """update qc_commands
                               set command_version = qc_commands.command_version+1, updated = SYSDATETIME(),
                               order_gkey = qc_orders.order_gkey,
                               order_version = qc_orders.order_version,
                               container_last_location = container_curr_location,
                               TRANSACTION_ID = """ + transId +
                              """, container_curr_location = '""" + inCurrentLocation +
                              "' from qc_commands\n" +
                              "    inner join qc_orders on qc_orders.command_gkey = qc_commands.command_gkey" +
                              " where qc_commands.qc_id = '" + inQcId +
                              "' and qc_commands.reference_id = '" + referenceId + "'" +
                              " and qc_commands.status = 'WORKING' "
              )
      );
      returnString = 'QC set done';
    } catch (Exception ex) {
      returnString = 'QC set not done';
    }
    if (inCurrentLocation.contains('AGV')) {
      LOGGER.debug('QC : ' + inQcId + 'placed the container : ' + inContainerId + 'on AGV')
    } else if (inCurrentLocation.contains('QCSPREADER')) {
      LOGGER.debug('QC : ' + inQcId + 'placed the container : ' + inContainerId + 'on SPREADER')
    }
    builder {
      actual_result returnString;
    }
    return builder;
  }


  /**
   * Sets QC to the new position
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=QCComplete<br>
   * QCName=Name of the QC<br>
   * containerId=Name of the container to be lifted by QC
   * @return JSON ,
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="QCComplete"/&gt;<br>
   * &lt;parameter id="QCName" value="QC01"/&gt;<br>
   * &lt;parameter id="containerId" value="TEST0000001"/&gt;<br>
   * &lt;parameter id="currentLocation" value=""/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String QCComplete(Map inParameters) {
    assert inParameters.size() == 3, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 3 parameters:                                        <parameter id="command" value="QCComplete" />
                                        <parameter id="QCName" value="<QC Id>" />
                                        <parameter id="containerId" value="<Container Id>" />'''

    String inQcId = _testCommandHelper.checkParameter('QCName', inParameters);
    String inContainerId = _testCommandHelper.checkParameter('containerId', inParameters);

    try {
      _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).execute(
              String.format(
                      "update qc_commands " +
                              "set command_version = qc_commands.command_version+1, updated = SYSDATETIME(), order_version = qc_orders.order_version, qc_commands.status = 'COMPLETE'" +
                              " from qc_commands\n" +
                              "    inner join qc_orders on qc_orders.command_gkey = qc_commands.command_gkey " +
                              "where qc_commands.qc_id = '" + inQcId + "'and qc_commands.reference_id = '" + inContainerId + "_REF'" + " and qc_commands.status = 'WORKING' "
              )
      );
      returnString = 'QC complete done';
    } catch (Exception ex) {
      returnString = 'QC complete not done';
    }
    LOGGER.debug('QC job completed for the QC : ' + inQcId)
    builder {
      actual_result returnString;
    }
    return builder;
  }

  //API required for TEAMS
  /**
   * Sends instruction params to TEAMS
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=SendToTEAMS <br>
   * QCName=Name of the QC<br>
   * containerId=Name of the container to be lifted by QC
   * @return JSON ,
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="SendToTEAMS "/&gt;<br>
   * &lt;parameter id="instruction" value="QcOutOperation"/&gt;<br>
   * &lt;parameter id="instructionParams" value="AREA.MAX= AREA.MIN= QC.ID=ALL QC.TROLLEY= SPREADERONSAFEHEIGHT="/&gt;<br>
   * &lt;parameter id="timeOut" value="40"/&gt;<br>   <code> optional - only when synch is true, by default it is taken as '30' secs </code>
   * &lt;parameter id="isSync" value="true"/&gt;<br>  <code> optional only if the execution needs to be synchronous and not in parallel </code>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String SendToTEAMS(Map inParameters) {
    assert inParameters.size() >= 3, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 3 parameters: <parameter id="command" value="SendToTEAMS" />
                                        <parameter id="instruction" value="<instruction>" />
                                        <parameter id="instructionParams" value="params" />
                                        <parameter id="timeOut" value="30" />
                                        <parameter id="sync" value="true" />'''

    String inTestOrder = _testCommandHelper.checkParameter('instruction', inParameters);
    String inInstructionParams = inParameters.get('instructionParams');
    def inIsSynch = ''
    Long timeOut = 30
    if (inParameters.containsKey('sync') && !inParameters.get('sync').toString().isEmpty()) {
      inIsSynch = inParameters.get('sync')
    }
    if (inParameters.containsKey('timeOut') && !inParameters.get('timeOut').toString().isEmpty()) {
      timeOut = inParameters.get('timeOut').toString().toLong()
    }
    String inStatus = ''
    inStatus = inParameters.get('status')
    if (inStatus != null) {
      if (inStatus.isEmpty()) {
        inStatus = "'ENTERED'"
      } else {
        inStatus = "'" + inStatus + "'"
      }
    } else {
      inStatus = "'ENTERED'"
    }


    try {
      _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).execute(
              String.format(
                      "insert into test_orders ( order_version,test_order,PARAMETERS,status ) values (1, '" + inTestOrder + "','" + inInstructionParams + "'," + inStatus + ")"
                      // "insert into test_orders ( order_version,test_order,PARAMETERS,status ) values ('1', '',''," + inStatus + ")"
              )
      );
      returnString = 'Sent instruction params to TEAMS';
      if (inIsSynch.toString().equalsIgnoreCase('true')) {
        List qr = _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).queryForList(
                String.format(
                        "select order_gkey from test_orders where test_order = '" + inTestOrder + "' and PARAMETERS = '" + inInstructionParams + "'"
                )
        );
        if (qr.size() > 0) {
          def resultList = qr.get(0).toString().split(':')
          def gkey;
          if (resultList.size() >= 2) {
            def val = resultList[0]
            gkey = resultList[1]
            if (gkey.trim().endsWith(']')) {
              gkey = gkey.substring(0, gkey.length() - 1);
            }
          }
          LOGGER.debug('SendToTEAMS:sync:true - order_gkey of test_orders' + gkey)
          long start = System.currentTimeMillis();
          long end = start + timeOut * 1000; // 60 seconds * 1000 ms/sec
          List teamsResponse;
          boolean timeOutExit = true;
          while (System.currentTimeMillis() < end)   //wait till the end time reaches
          {
            teamsResponse = _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).queryForList(
                    String.format(
                            "select status from test_commands where order_gkey = " + gkey
                    )
            );
            if (teamsResponse.toString().contains('COMPLETE')) {
              timeOutExit = false;
              break; //if the status is complete come out of the loop
            }
          }
          if (timeOutExit == false) {
            returnString = 'SendToTEAMS : sync true successful - TEAMS response is complete '
          } else {
            returnString = 'SendToTEAMS : sync true - TEAMS response is not complete , exit because of timeOut'
          }
        }
      }
    } catch (Exception ex) {
      returnString = 'Sending instruction params to TEAMS failed : ' + ex;
    }
    builder {
      actual_result returnString;
    }
    LOGGER.debug('SendToTEAMS': returnString)
    return builder;
  }

  /**
   * Teams sets the stack status on particular bay and inserts/update record in area_status table.
   * xps reads this table and sets the stack status accordingly and inserts data in stackstatus table
   *
   * @Precondition
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=UpdateASCAreaStatus<br>
   * area=asc stack's bay info (WSTP.W008.A.01) (CLTP.C013.12) (YARD.M008.A.01)
   * status=0(Temp Blocked/Unavailable), 1(Available), 2(MenWorking)
   * @return JSON , <code>Area status updated</code> if order is inserted/updated in area_status<br>
   *               <code>Area status not updated</code> if order is inserted/updated in area_status<br>
   * @Postcondition
   * Required work flow should be generated, this can be verified by taking the report using ReportEntities API.
   * @Example
   * Table invoked by TEAMS : area_status<br>
   * Table invoked by SPARCS : xps_stackstatus<br>
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="UpdateASCAreaStatus"/&gt;<br>
   * &lt;parameter id="area" value="WSTP.W008.A.01"/&gt;<br>
   * &lt;parameter id="status" value="2"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String UpdateASCAreaStatus(Map inParameters) {
    assert inParameters.size() <= 3, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 2 parameters:
                                        <parameter id="command" value="UpdateASCAreaStatus" />
                                        <parameter id="area" value="<asc stack's bay list>" />
                                        <parameter id="areaStatus" value="<status>" />'''

    String inAreaIdList = _testCommandHelper.checkParameter('area', inParameters);
    String[] areaList = inAreaIdList.split(",");
    String inAreaStatus = _testCommandHelper.checkParameter('areaStatus', inParameters);
    try {
      for (String areaId : areaList) {

        final List qr = _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).updateForObject(
                String.format(
                        "merge area_Status as tas " +
                                "using (select '" + areaId + "' as area)  as source " +
                                "on tas.area = source.area " +
                                "when matched then " +
                                "update set " +
                                "tas.status = '" + inAreaStatus + "', " +
                                "tas.updated = SYSDATETIME()" +
                                " when not matched then " +
                                "insert (area, status, updated) values" +
                                "('" + areaId + "','" + inAreaStatus + "', SYSDATETIME());"

                )
        );
        if (inAreaStatus == '0') {
          returnString = 'Area (' + areaId + ') Status updated with Temp. Blocked';
        } else if (inAreaStatus == '1') {
          returnString = 'Temp. Blocked / Men Working status cleared on area (' + areaId + ')';
        } else if (inAreaStatus == '2') {
          returnString = 'Area (' + areaId + ') Status updated with Men Working';
        }
      }
    } catch (Exception ex) {
      returnString = 'Area Status not updated' + ex
    }
    builder {
      actual_result returnString
    }
    return builder;
  }
  
   /**
   * Inserts record to ASC_Commands for the ASC Move initiated by TEAMS, could be a housekeeping move.
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=AcceptASCUnorderedMove<br>
   * cheId=Name of the ASC which needs to accept the order
   * @return JSON , <code>ASC unordered move accepted</code> if record is inserted into commands<br>
   *               <code>ASC unordered move not accepted</code> fails
   * @Postcondition
   * Required work flow should be generated, this can be verified by taking the report using ReportEntities API.
   * @Example
   * Table invoked by TEAMS : asc_commands<br>
   * Table invoked by SPARCS (Work Flow) : xps_che,inv_wi,xps_workassignment,xps_ecevent<br>
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="AcceptASCUnorderedMove"/&gt;<br>
   * &lt;parameter id="cheId" value="ASC01"/&gt;<br>
   * &lt;parameter id="origin" value="YARD.M008.A.24.A"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String AcceptASCUnorderedMove(Map inParameters) {
    assert inParameters.size() >= 2, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 2 parameters:
                                        <parameter id="command" value="AcceptASCUnorderedMove" />
                                        <parameter id="cheId" value="<asc Name>" />
										<parameter id="origin" value="<origin>" />'''

    String inCheId = _testCommandHelper.checkParameter('cheId', inParameters);
    String inOrigin = _testCommandHelper.checkParameter('origin', inParameters);
    try {
      _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).execute(
              String.format(
                      "insert into asc_commands ( order_version, command_version, che_id, origin, status, order_progress, created) values (0, 1," +
                              "'" + inCheId + "','" + inOrigin + "','WORKING', 'Unknown',SYSDATETIME())")
      );
      returnString = 'ASC unordered move accepted';
      LOGGER.debug('ASC unordered move inserted into ASCCommands for the ASC :' + inCheId)
    } catch (Exception ex) {
      returnString = 'ASC unordered move not accepted' + ex;
    }

    builder {
      actual_result returnString;
    }
    return builder;
  }

  /**
   * Completes unordered ASC move initiated by TEAMS.
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=CompleteASCUnorderedMove<br>
   * cheId=Name of the ASC which needs to accept the order
   * @return JSON , <code>ASC unordered move completed</code> if unordered move status is updated to 'COMPLETE'<br>
   *               <code>ASC unordered move not completed'</code> exception
   * @Postcondition
   * Required work flow should be generated, this can be verified by taking the report using ReportEntities API.
   * @Example
   * Table invoked by TEAMS : asc_commands<br>
   * Table invoked by SPARCS (Work Flow) : xps_che,inv_wi,xps_workassignment,xps_ecevent<br>
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="CompleteASCUnorderedMove"/&gt;<br>
   * &lt;parameter id="cheId" value="ASC01"/&gt;<br>
   * &lt;parameter id="destionation" value="asc destination"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String CompleteASCUnorderedMove(Map inParameters) {
    assert inParameters.size() >= 2, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 2 parameters:
                                        <parameter id="command" value="CompleteASCUnorderedMove" />
                                        <parameter id="cheId" value="<asc Name>" />
                                        <parameter id="destination" value="<destination>" />'''

    String inCheId = _testCommandHelper.checkParameter('cheId', inParameters);
    String inDestination = _testCommandHelper.checkParameter('destination', inParameters);

    try {
      _testCommandHelper.getTeamsDbHelper(_testCommandHelper.eciAdapterId).execute(
              String.format(
                      "update asc_commands set command_version = asc_commands.command_version+1, updated = SYSDATETIME()," +
                              "order_version = asc_orders.order_version," +
                              "status = 'COMPLETE', " +
                              "actual_destination = '" + inDestination + "'" +
                              ", set_time = SYSDATETIME()" +
                              " from asc_commands left join asc_orders on asc_orders.order_gkey = asc_commands.order_gkey where asc_commands.che_id = " + "'" + inCheId + "'" +
                      " and asc_commands.status = 'WORKING'" )
      );
      returnString = 'ASC unordered move completed';
      LOGGER.debug('ASC unordered move inserted into ASCCommands for the ASC :' + inCheId)
    } catch (Exception ex) {
      returnString = 'ASC unordered move not completed' + ex;
    }
    builder {
      actual_result returnString;
    }
    return builder;
  }

  //Method to find Che Table Name by having the che type[AGV,ASC,QC]
  private String findCheTableName(String cheType) {
    String tableName = "";
    if (cheType.contains("ASC")) {
      tableName = "asc_status";
    } else if (cheType.contains("AGV")) {
      tableName = "agv_status";
    } else if (cheType.contains("QC")) {
      tableName = "qc_status";
    }
    return tableName;
  }
}
