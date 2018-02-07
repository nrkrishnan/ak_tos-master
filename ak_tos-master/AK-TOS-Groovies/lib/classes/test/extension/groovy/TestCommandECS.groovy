package test.extension.groovy

import com.navis.control.configuration.EciAdapterIds
import com.navis.control.data.BasicVesselDschOnAgvJobTestData
import com.navis.control.database.StandardEciDatabaseDumpUtil
import com.navis.control.eci.emulation.EciEmulationQcStepData
import com.navis.control.eci.emulation.EciEmulationScenario
import com.navis.control.eci.emulation.EciEmulationTestHelper
import com.navis.control.eci.emulation.IValueHandoffManager
import com.navis.control.esb.teams.emulation.TeamsEmulationTestHelper
import com.navis.control.util.AutomationTestUtils
import com.navis.framework.util.LogUtils
import org.apache.log4j.Level
import org.apache.log4j.Logger

/**
 * Created with IntelliJ IDEA.
 * User: rajansh
 * Date: 29/10/13
 * Time: 3:47 PM
 * To change this template use File | Settings | File Templates.
 */
class TestCommandECS {

  /** holds the result returned by each method */
  def String returnString = null;
  /** json builder, frames the output in json format */
  def builder = new groovy.json.JsonBuilder();
  /** handler to invoke the fields in helper class */
  test.extension.groovy.TestCommandHelper _testCommandHelper = new test.extension.groovy.TestCommandHelper();

  //Logger for TestCommand
  public Logger LOGGER = Logger.getLogger(TestCommandECS.class);

  /**
   * Starts a standard emulation scenario using default properties and Stop the ECS emulation
   * Starts and Stops the ECS emulation.
   * Once a job has been dispatched, this emulation will automatically complete the moves.
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=ECSEmulation<br>
   * @return JSON ,
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="ECSEmulation"/&gt;<br>
   * &lt;parameter id="ecsEmulation" value="START/STOP"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String ECSEmulation(Map inParameters) {
    assert inParameters.size() >= 2, '''Must supply at least 2 parameters:
                                          <parameter id="command" value="ECSEmulation" />
                                          <parameter id="ecsEmulation" value="START/STOP/PLACE/TEAMSDB-REPORT" />'''

    // retrieve the active TEAMS emulation test helper (if any)
    final TeamsEmulationTestHelper activeTeamsEmulationTestHelper = EciEmulationTestHelper.getActiveEciEmulationTestHelper(
            TeamsEmulationTestHelper.class,
            EciAdapterIds.TEAMS1
    );

    // use the active TEAMS emulation test helper if found, or create a new one if not
    final TeamsEmulationTestHelper _teamsEmulationTestHelper;

    if (activeTeamsEmulationTestHelper == null) {
      // no active emulation helper was found, so create a new one
      _teamsEmulationTestHelper = new TeamsEmulationTestHelper();
      // and indicate that we don't want to publish the scenario results
      // (at some point we probably want to enable this via an invocation property,
      // since the scenario results include some pretty useful stuff, including
      // the TEAMS database snapshot report and the "flowlog" report...  The main
      // reson I suggest not moving towards that now is the question of "where to
      // write the published results")
      _teamsEmulationTestHelper.setPublishScenarioResults(false);
    } else {
      // already active - just use it
      _teamsEmulationTestHelper = activeTeamsEmulationTestHelper;
    }

    LogUtils.forceLogAtInfo(
            LOGGER,
            String.format(
                    "using activeTeamsEmulationTestHelper(%s), _teamsEmulationTestHelper(%s)",
                    activeTeamsEmulationTestHelper,
                    _teamsEmulationTestHelper
            )
    );

    String inECSCommand = _testCommandHelper.checkParameter('ecsEmulation', inParameters);

    try {

      Logger.getLogger("com.navis.control.esb").setLevel(Level.ALL);
      Logger.getLogger("com.navis.control.eci").setLevel(Level.ALL);

      if (inECSCommand.equalsIgnoreCase('ENABLE-ECI-FLOWS')) {
        AutomationTestUtils.enableAutomationFeatures();
      } else if (inECSCommand.equalsIgnoreCase('DISABLE-ECI-FLOWS')) {
        AutomationTestUtils.disableAutomationFeatures();
      } else if (inECSCommand.equalsIgnoreCase('PLACE')) {

        // for a yard handoff, the key is the TEAMS position and the value is the container id
        // for a vessel handoff, the key is the TEAMS position (QCTP) and the value is the QC reference id
        assert inParameters.size() == 3, '''Must supply 3 parameters: (found ''' + inParameters.size() +
                ''') <parameter id="command" value="ECSEmulation" />
                  <parameter id="ecsEmulation" value="PLACE" />
                  <parameter id="handoffExpressionList" value="<handoffKey.handoffValue>[,<handoffKey.handoffValue>[, ...]]" />'''

        final String handoffExpressionList = _testCommandHelper.checkParameter('handoffExpressionList', inParameters);
        final String[] handoffExpressionArray = handoffExpressionList.split(",");
        final IValueHandoffManager handoffManager = _teamsEmulationTestHelper.getEciEmulationContext().getActiveObjectHandoffManager();
        final String actorName = getClass().getSimpleName();

        for (final String handoffExpression : handoffExpressionArray) {
          final int lastDotPos = handoffExpression.lastIndexOf('.');
          assert lastDotPos > 0 && (lastDotPos + 1) < handoffExpression.length(): String.format("invalid handoff expression(%s)", handoffExpression);
          final String handoffKey = handoffExpression.substring(0, lastDotPos);
          final String handoffValue = handoffExpression.substring(lastDotPos + 1);
          handoffManager.handoff(actorName, handoffKey, handoffValue, true, IValueHandoffManager.HANDOFFMODE_ALWAYS_PRODUCER);
        }

        returnString = 'Handoff complete'
      } else if (inECSCommand.equalsIgnoreCase('TEAMSDB-REPORT')) {

        final String outputFilename = _testCommandHelper.checkParameter('outputFilename', inParameters);
        assert outputFilename != null, "must specify 'outputfilename' parameter";

        final String[] dumpUtilArgs = [EciAdapterIds.TEAMS1, outputFilename, 'TestCommand TEAMSDB-REPORT'];

        StandardEciDatabaseDumpUtil.main(dumpUtilArgs);

        returnString = 'Dump complete';
      } else if (inECSCommand.equalsIgnoreCase('START')) {

        if (activeTeamsEmulationTestHelper != null) {
          return 'ERROR - detected active TEAMS emulation';
        }

        // forcefully enable the ECI flows if necessary (probably should require the user to do this??)
        if (!AutomationTestUtils.isAutomationFeaturesEnabled()) {
          AutomationTestUtils.enableAutomationFeatures();
        }

        final BasicVesselDschOnAgvJobTestData dschOnAgvJobTestData = BasicVesselDschOnAgvJobTestData.getDefaultData()

        final EciEmulationQcStepData[] dischargeSteps = [
                new EciEmulationQcStepData(dschOnAgvJobTestData.getTeamsPosQcPlatform()),
                new EciEmulationQcStepData(dschOnAgvJobTestData.getTeamsPosQcPortalTrolley()),
                new EciEmulationQcStepData(dschOnAgvJobTestData.getTeamsPosAgv(), dschOnAgvJobTestData.getTeamsPosQcTp())
        ];

        // create a standard scenario configuration object
        final EciEmulationScenario eciEmulationScenario = _teamsEmulationTestHelper.createStandardEmulationScenario();

        // set handoff handshake timeout to 5 minutes (or ??)
        eciEmulationScenario.setHandshakeTimeoutMsec(300000L);

        // pass to it the "REFERENCE ID to TEAMS QC STEPS" controlling the sequence of move steps
        eciEmulationScenario.setReferenceIdToQcSteps(dschOnAgvJobTestData.getQcRefId(), dischargeSteps);

        // pass the "handoff visit key to QC Reference IDs" entry allowing the AGV and QC to build a common handoff key
        final String[] referenceIds = [dschOnAgvJobTestData.getQcRefId()];
        eciEmulationScenario.setHandoffVisitKeyToReferenceIds(dschOnAgvJobTestData.getTeamsPosQcTp(), referenceIds);

        // now set the new emulation scenario configuration into the emulation context
        _teamsEmulationTestHelper.getEciEmulationContext().setEciEmulationScenario(eciEmulationScenario);

        // initialize the TEAMS emulation test helper class instance
        //ARGO-54174 fix - Jan 2nd
        _teamsEmulationTestHelper.setUp(getClass(), getClass().getSimpleName(), EciAdapterIds.TEAMS1);

        // start the emulation processes
        _teamsEmulationTestHelper.startEmulationScenario();

        returnString = 'ECS Emulation started'

      } else if (inECSCommand.equalsIgnoreCase('STOP')) {

        // stops the currently active emulation process

        if (activeTeamsEmulationTestHelper == null) {
          return 'ERROR - no active TEAMS emulation detected';
        }

        // NOTE: tearDown() will also stop any active emulation scenario
        activeTeamsEmulationTestHelper.tearDown();

        returnString = 'ECS Emulation stopped';
      }
    } catch (Exception ex) {
      returnString = 'Error in ECS Emulation Start/Stop ' + ex;
    }
    builder {
      actual_result returnString;
    }
    LOGGER.debug('ECSEmulation:' + returnString)
    return builder;
  }

  public String ECSEmulationForLoadsAndDischarges(Map inParameters) {
    assert inParameters.size() >= 2, '''Must supply at least 2 parameters:
                                          <parameter id="command" value="ECSEmulation" />
                                          <parameter id="ecsEmulation" value="START/STOP/PLACE/TEAMSDB-REPORT" />'''

    String inECSCommand = _testCommandHelper.checkParameter('ecsEmulation', inParameters);
    String moveKind = 'Yard'
    if (inParameters.containsKey('moveKind')) {
      moveKind = inParameters.get('moveKind')
    }

    try {
      Logger.getLogger("com.navis.control.esb").setLevel(Level.DEBUG);
      Logger.getLogger("com.navis.control.eci").setLevel(Level.DEBUG);

      if (inECSCommand.equalsIgnoreCase('TEAMSDB-REPORT')) {
        final String outputFilename = _testCommandHelper.checkParameter('outputFilename', inParameters);
        assert outputFilename != null, "must specify 'outputfilename' parameter";
        final String[] dumpUtilArgs = [outputFilename, 'TestCommand TEAMSDB-REPORT'];
        StandardEciDatabaseDumpUtil.main(dumpUtilArgs);
        returnString = 'Dump complete';
      } else if (inECSCommand.equalsIgnoreCase('START') || inECSCommand.equalsIgnoreCase('STOP')) {
        test.extension.groovy.TestCommandECSEmulation testCommandECSEmulation;
        if (inECSCommand.equalsIgnoreCase('START')) {
          if (moveKind.equalsIgnoreCase('Yard')) {
            //for inter/intra stack moves use default base data available
            testCommandECSEmulation = new test.extension.groovy.TestCommandECSEmulation()
            returnString = testCommandECSEmulation.loadStandardEmulationScenario()
          } else {  //set emulation default data for load/discharge to make qc emulation successful
            test.extension.groovy.TestCommandBasicEmulationData testCommandBasicEmulationData = new test.extension.groovy.TestCommandBasicEmulationData()
            testCommandBasicEmulationData.getDefaultData(inParameters)
            testCommandECSEmulation = new test.extension.groovy.TestCommandECSEmulation(testCommandBasicEmulationData)
            returnString = testCommandECSEmulation.loadAdvancedEmulationScenario()
          }
          LOGGER.debug('ECS emulation started ')
        } else if (inECSCommand.equalsIgnoreCase('STOP')) {
          testCommandECSEmulation = new test.extension.groovy.TestCommandECSEmulation()
          returnString = testCommandECSEmulation.stopEmulationScenario()
        }
      }
    } catch (Exception ex) {
      returnString = 'Error in ECS Emulation Start/Stop ' + ex;
    }
    builder {
      actual_result returnString;
    }
    return builder;
  }
}
