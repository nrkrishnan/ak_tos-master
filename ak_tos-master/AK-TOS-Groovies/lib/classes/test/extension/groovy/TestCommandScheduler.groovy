package test.extension.groovy

import com.navis.argo.ArgoEntity
import com.navis.argo.ArgoField
import com.navis.argo.ContextHelper
import com.navis.argo.business.atoms.ScopeEnum
import com.navis.argo.business.xps.model.ChePool
import com.navis.argo.portal.context.ArgoUserContextProvider
import com.navis.argo.util.FileUtil
import com.navis.control.business.ControlTestUtils
import com.navis.control.business.agv.AgvQaTestUtils
import com.navis.control.business.asc.AscTestUtils
import com.navis.control.business.atoms.ControlProblemTypeEnum
import com.navis.control.portal.optimization.asc.AscSchedulerSolveContext
import com.navis.control.portal.optimization.asc.config.AscTestConfigurationProvider
import com.navis.control.portal.solver.context.DefaultAgvSolveContext
import com.navis.external.framework.AbstractExtensionCallback
import com.navis.framework.business.Roastery
import com.navis.framework.persistence.DatabaseHelper
import com.navis.framework.persistence.HibernateApi
import com.navis.framework.portal.QueryUtils
import com.navis.framework.portal.UserContext
import com.navis.framework.portal.context.IUserContextProvider
import com.navis.framework.portal.context.PortalApplicationContext
import com.navis.framework.portal.query.DomainQuery
import com.navis.framework.portal.query.PredicateFactory
import com.navis.framework.quartz.QuartzJobDetail
import com.navis.framework.util.scope.ScopeCoordinates
import com.navis.optimization.business.model.ProblemSolution
import com.navis.optimization.portal.solver.events.ISolveStatusEvent
import com.navis.optimization.portal.solver.ilog.CplexTestUtil
import com.navis.optimization.util.OptimizationTestUtils
import com.navis.yard.business.model.StackBlock
import org.apache.log4j.Level
import org.apache.log4j.Logger

/**
 * Created with IntelliJ IDEA.
 * User: rajansh
 * Date: 29/10/13
 * Time: 3:15 PM
 * To change this template use File | Settings | File Templates.
 */
class TestCommandScheduler {

  /** holds the result returned by each method */
  def String returnString = null;
  /** json builder, frames the output in json format */
  def builder = new groovy.json.JsonBuilder();
  private DatabaseHelper _teamsDbHelper;
  /** handler to invoke the fields in helper class */
  test.extension.groovy.TestCommandHelper _testCommandHelper = new test.extension.groovy.TestCommandHelper();

  //Logger for TestCommand
  public Logger LOGGER = Logger.getLogger(TestCommandScheduler.class);

  /**
   * Starts an AGV Auto Dispatch job <br>
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command = "StartAgvAutoDispatch"
   * @return JSON , <code>Auto dispatch command sent</code> - if success<br>
   *                <code>Failed to send auto dispatch command</code> - if failed<br>
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="StartAgvAutoDispatch" /&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String StartAgvAutoDispatch(Map inParameters) {
    try {
      ControlTestUtils.startAgvAutoDispatch(UserContext.getThreadUserContext(),'AGV_SCHEDULER');
      //To do: Get the quartz job back and assert if job has been scheduled
      returnString = "Auto dispatch command sent";
    } catch (Exception ex) { returnString 'Failed to send auto dispatch command : ' + ex; }
    LOGGER.debug('StartAgvAutoDispatch:' + returnString)
    builder {
      actual_result returnString
    }
    return builder;
  }

  /**
   * Load the ASC scheduler Problem TYpe/ solution for the current scope at Facility level<br>
   *
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=LoadAscSchedulerProblem<br>
   * @return JSON ,
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="LoadAscSchedulerProblem"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String LoadAscSchedulerProblem(Map inParameters) {
    assert inParameters.size() == 1, '''Must supply 1 parameters:
                                 <parameter id="command" value="LoadAscSchedulerProblem" /> '''

    UserContext userContext = ContextHelper.getThreadUserContext();
    Serializable facilityGkey = userContext.getScopeCoordinate().getScopeLevelCoord(ScopeCoordinates.SCOPE_LEVEL_3.intValue());
    ArgoUserContextProvider contextProvider = (ArgoUserContextProvider) PortalApplicationContext.getBean(IUserContextProvider.BEAN_ID);
    UserContext facilityContext = contextProvider.getSystemUserContextForScope(ScopeEnum.FACILITY, facilityGkey, Roastery.getBeanFactory());

    try {
      AscTestUtils.loadAscidSchedulerProblem(facilityContext);
      returnString = 'Problem for ASC loaded successfully';
    } catch (Exception ex) {
      returnString = 'Problem for ASC not loaded successfully';
    }
    builder {
      actual_result returnString;
    }
    return builder;
  }

  /**
   * Runs the ASC Scheduler on the loaded ASC Problem type /solution <br>
   * <a href="http://jira.navis.com/browse/ARGO-44379"> ARGO-44379 </a>
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=RunAscScheduler<br>
   * @return JSON ,
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="RunAscScheduler"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String RunAscScheduler(Map inParameters) {
    assert inParameters.size() == 2, '''Must supply 2 parameters:
                                 <parameter id="command" value="RunAscScheduler" />
                                 <parameter id="blockName" value="<blockName>" />'''


    String inBlockName = _testCommandHelper.checkParameter('blockName', inParameters);
    UserContext userContext = ContextHelper.getThreadUserContext();
    StackBlock ascBlock = AscTestUtils.findBlockByName(inBlockName);
    Serializable blockGkey = ascBlock.getAbnGkey();

    Logger.getLogger("com.navis.control.business.taskhandlers.AbstractTaskHandler").setLevel(Level.DEBUG);
    Logger.getLogger("com.navis.control.business.model.ControlManagerPea").setLevel(Level.DEBUG);
    Logger.getLogger("com.navis.control.business.dispatch.ControlDispatcherPea").setLevel(Level.DEBUG);
    Logger.getLogger("com.navis.control.business.TransferZoneWorkInstructionProvider").setLevel(Level.DEBUG);
    Logger.getLogger("org.mule.routing.CollectionSplitter").setLevel(Level.ERROR);
    Logger.getLogger("com.navis.control.portal.optimization").setLevel(Level.DEBUG);
    Logger.getLogger(AbstractExtensionCallback.class).setLevel(Level.DEBUG);
    Logger.getLogger("com.navis.control.portal.optimization").setLevel(Level.ERROR);
    Logger.getLogger(AbstractExtensionCallback.class).setLevel(Level.ERROR);
    AscTestUtils.setLogsMoreVerbose();
    CplexTestUtil.loadNeededLibraries();
    ControlTestUtils.setupDeckingPenalties();

    // disable/enable config settings as required
    final AscTestConfigurationProvider configProvider = new AscTestConfigurationProvider(AscTestUtils.getDefaultAscidConfigProvider());
    configProvider.setPrepositionEnabled(false); // tests calling this method do not expect prepositions to be enabled by defualt
    final AscSchedulerSolveContext solveContext = new AscSchedulerSolveContext(blockGkey);
    solveContext.setConfigProviderOverride(configProvider);

    ISolveStatusEvent schedulerResult = AscTestUtils.runAscidScheduler(userContext, solveContext);
    if (schedulerResult.isSuccess()) {
      ISolveStatusEvent cheDispatchResult = ControlTestUtils.runCheDispatcher(userContext);
      if (cheDispatchResult.isSuccess()) {
        returnString = 'ASC Scheduler started dispatching jobs';
      } else {
        returnString = 'ASC Scheduler Started but could not dispatch jobs. See logs for more info';
      }
    } else {
      returnString = 'ASC Scheduler failed to start';
    }
    builder {
      actual_result returnString;
    }
    LOGGER.debug('RunAscScheduler:' + returnString)
    return builder;
  }

  /**
   * Runs the ASC Scheduler ONLY (No Dispatch) on the loaded ASC Problem type /solution <br>
   * <a href="http://jira.navis.com/browse/ARGO-44379"> ARGO-44379 </a>
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=RunAscSchedulerOnly<br>
   * @return JSON ,
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="RunAscSchedulerOnly"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String RunAscSchedulerOnly(Map inParameters) {
    assert inParameters.size() == 2, '''Must supply 2 parameters:
                                 <parameter id="command" value="RunASCSchedulerOnly" />
                                 <parameter id="blockName" value="<blockName>" />'''


    String inBlockName = _testCommandHelper.checkParameter('blockName', inParameters);
    UserContext userContext = ContextHelper.getThreadUserContext();
    StackBlock ascBlock = AscTestUtils.findBlockByName(inBlockName);
    Serializable blockGkey = ascBlock.getAbnGkey();

    Logger.getLogger("com.navis.control.business.taskhandlers.AbstractTaskHandler").setLevel(Level.DEBUG);
    Logger.getLogger("com.navis.control.business.model.ControlManagerPea").setLevel(Level.DEBUG);
    Logger.getLogger("com.navis.control.business.dispatch.ControlDispatcherPea").setLevel(Level.DEBUG);
    Logger.getLogger("com.navis.control.business.TransferZoneWorkInstructionProvider").setLevel(Level.DEBUG);
    Logger.getLogger("org.mule.routing.CollectionSplitter").setLevel(Level.ERROR);
    Logger.getLogger("com.navis.control.portal.optimization").setLevel(Level.DEBUG);
    Logger.getLogger(AbstractExtensionCallback.class).setLevel(Level.DEBUG);
    Logger.getLogger("com.navis.control.portal.optimization").setLevel(Level.ERROR);
    Logger.getLogger(AbstractExtensionCallback.class).setLevel(Level.ERROR);
    AscTestUtils.setLogsMoreVerbose();
    CplexTestUtil.loadNeededLibraries();
    ControlTestUtils.setupDeckingPenalties();

    // disable/enable config settings as required
    final AscTestConfigurationProvider configProvider = new AscTestConfigurationProvider(AscTestUtils.getDefaultAscidConfigProvider());
    configProvider.setPrepositionEnabled(false); // tests calling this method do not expect prepositions to be enabled by defualt
    final AscSchedulerSolveContext solveContext = new AscSchedulerSolveContext(blockGkey);
    solveContext.setConfigProviderOverride(configProvider);

    ISolveStatusEvent schedulerResult = AscTestUtils.runAscidScheduler(userContext, solveContext);
    if (schedulerResult.isSuccess()) {
      returnString = 'ASC Scheduler started running successfully';
    } else {
      returnString = 'ASC Scheduler failed to start';
    }
    builder {
      actual_result returnString;
    }
    LOGGER.debug('RunAscSchedulerOnly:' + returnString)
    return builder;
  }

  /**
   * Runs the ASC Scheduler ONLY - For Housekeeping & Preposition (No Dispatch) on the loaded ASC Problem type /solution <br>
   * <a href="http://jira.navis.com/browse/ARGO-44379"> ARGO-44379 </a>
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=RunAscSchedulerOnlyHK<br>
   * @return JSON ,
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="RunAscSchedulerOnlyHK"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String RunAscSchedulerOnlyHK(Map inParameters) {
    assert inParameters.size() >= 2, '''Must supply at least 2 parameters:
                                 <parameter id="command" value="RunASCSchedulerOnlyHK" />
                                 <parameter id="blockName" value="<blockName>" />
								 <parameter id="houseKeeping" value="false/true" />
								 <parameter id="preposition" value="false/true" />'''


    String inBlockName = _testCommandHelper.checkParameter('blockName', inParameters);
    String inhouseKeeping = '';
    String inpreposition = '';
    if (inParameters.containsKey('houseKeeping')) {
      inhouseKeeping = inParameters.get('houseKeeping');
    }
    if (inParameters.containsKey('preposition')) {
      inpreposition = inParameters.get('preposition');
    }

    UserContext userContext = ContextHelper.getThreadUserContext();
    StackBlock ascBlock = AscTestUtils.findBlockByName(inBlockName);
    Serializable blockGkey = ascBlock.getAbnGkey();

    Logger.getLogger("com.navis.control.business.taskhandlers.AbstractTaskHandler").setLevel(Level.DEBUG);
    Logger.getLogger("com.navis.control.business.model.ControlManagerPea").setLevel(Level.DEBUG);
    Logger.getLogger("com.navis.control.business.dispatch.ControlDispatcherPea").setLevel(Level.DEBUG);
    Logger.getLogger("com.navis.control.business.TransferZoneWorkInstructionProvider").setLevel(Level.DEBUG);
    Logger.getLogger("org.mule.routing.CollectionSplitter").setLevel(Level.ERROR);
    Logger.getLogger("com.navis.control.portal.optimization").setLevel(Level.DEBUG);
    Logger.getLogger(AbstractExtensionCallback.class).setLevel(Level.DEBUG);
    Logger.getLogger("com.navis.control.portal.optimization").setLevel(Level.ERROR);
    Logger.getLogger(AbstractExtensionCallback.class).setLevel(Level.ERROR);
    AscTestUtils.setLogsMoreVerbose();
    CplexTestUtil.loadNeededLibraries();
    ControlTestUtils.setupDeckingPenalties();

    AscSchedulerSolveContext solveContext = new AscSchedulerSolveContext(blockGkey);
    AscTestConfigurationProvider configProvider = new AscTestConfigurationProvider(AscTestUtils.getDefaultAscidConfigProvider());
    if (inhouseKeeping != null) {
      if (inhouseKeeping.equalsIgnoreCase('true')) {
        configProvider.setHousekeepingEnabled(true);
      } else if (inhouseKeeping.equalsIgnoreCase('false')) {
        configProvider.setHousekeepingEnabled(false);
      }
    }
    if (inpreposition != null) {
      if (inpreposition.equalsIgnoreCase('true')) {
        configProvider.setPrepositionEnabled(true);
      } else if (inpreposition.equalsIgnoreCase('false')) {
        configProvider.setPrepositionEnabled(false);
      }
    }
    //modelConfig.setWhateverPropertyYouAreTesting()
    solveContext.setConfigProviderOverride(configProvider)
    ISolveStatusEvent schedulerResult = AscTestUtils.runAscidScheduler(userContext, solveContext);

    //ISolveStatusEvent schedulerResult = AscTestUtils.runAscidScheduler(userContext, blockGkey);
    if (schedulerResult.isSuccess()) {
      returnString = 'ASC Scheduler started running successfully';
    } else {
      returnString = 'ASC Scheduler failed to start';
    }
    builder {
      actual_result returnString;
    }
    LOGGER.debug('RunAscSchedulerOnlyHK:' + returnString)
    return builder;
  }

  /**
   * Runs the ASC Scheduler Dispatch only
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=RunAscCheDispatch<br>
   * @return JSON ,
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="RunAscCheDispatch"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String RunCheDispatcher(Map inParameters) {
    assert inParameters.size() == 1, '''Must supply 1 parameters:
                                 <parameter id="command" value="RunCheDispatcher" />'''

    UserContext userContext = ContextHelper.getThreadUserContext();
    Logger.getLogger("com.navis.control.business.taskhandlers.AbstractTaskHandler").setLevel(Level.DEBUG);
    Logger.getLogger("com.navis.control.business.model.ControlManagerPea").setLevel(Level.DEBUG);
    Logger.getLogger("com.navis.control.business.dispatch.ControlDispatcherPea").setLevel(Level.DEBUG);
    Logger.getLogger("com.navis.control.business.TransferZoneWorkInstructionProvider").setLevel(Level.DEBUG);
    Logger.getLogger("org.mule.routing.CollectionSplitter").setLevel(Level.ERROR);
    ISolveStatusEvent cheDispatchResult = ControlTestUtils.runCheDispatcher(userContext);
    if (cheDispatchResult.isSuccess()) {
      returnString = 'Che dispatcher is now running. Jobs will be dispatched';
    } else {
      returnString = 'Che dispatcher fialed to start. See logs for more info';
    }
    builder {
      actual_result returnString;
    }
    LOGGER.debug('RunCheDispatcher:' + returnString)
    return builder;
  }

  /**
   * Deletes the loaded ASC Problem type/solution <br>
   * <a href="http://jira.navis.com/browse/ARGO-44380"> ARGO-44380 </a>
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=StartAscAutoDispatch<br>
   * @return JSON ,
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="StartAscAutoDispatch"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String DeleteAscSchedulerProblem(Map inParameters) {

    UserContext userContext = ContextHelper.getThreadUserContext();
    Serializable facilityGkey = userContext.getScopeCoordinate().getScopeLevelCoord(ScopeCoordinates.SCOPE_LEVEL_3.intValue());
    ArgoUserContextProvider contextProvider = (ArgoUserContextProvider) PortalApplicationContext.getBean(IUserContextProvider.BEAN_ID);
    UserContext facilityContext = contextProvider.getSystemUserContextForScope(ScopeEnum.FACILITY, facilityGkey, Roastery.getBeanFactory());

    AscTestUtils.deleteAscidSchedulerProblem(facilityContext);
    returnString = 'Problem for ASC deleted successfully';
    builder {
      actual_result returnString;
    }
    return builder;
  }

  /**
   * Stops the ASC Scheduler
   * <a href="http://jira.navis.com/browse/ARGO-44380"> ARGO-44380 </a>
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=StopAscScheduler<br>
   * @return JSON ,
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="StopAscScheduler"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String StopAscScheduler(Map inParameters) {
    assert inParameters.size() >= 1, '''Must supply 1 parameter:
                                        <parameter id="command" value="StopAscScheduler" />'''
    try {
      /*String jobName = _testCommandHelper.checkParameter("JobName", inParameters);
      String groupName = _testCommandHelper.checkParameter("GroupName", inParameters);*/
      String jobName = inParameters.get("JobName");
      String groupName = inParameters.get("GroupName");
      if (null == jobName || jobName.isEmpty()) {
        jobName = SCHEDULER_JOB_NAME;
      }
      if (null == groupName || groupName.isEmpty()) {
        groupName = "ASC";
      }
      QuartzJobDetail jobDetail = ControlTestUtils.stopAscScheduler(jobName, groupName);
      returnString = "Asc scheduler stopped";
    } catch (Exception ex) {
      returnString = 'Asc scheduler stopping failed : no job groups configured for ASC ' + ex;
    }
    builder {
      actual_result returnString
    }
    return builder;
  }

  /**
   * Runs the AGV Scheduler on the loaded AGV Problem type /solution <br>
   * <a href="http://jira.navis.com/browse/ARGO-45483"> ARGO-45483 </a>
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=RunAgvScheduler<br>
   * @return JSON ,
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="RunAgvScheduler"/&gt;<br>
   * &lt;parameter id="timeOutInSeconds" value="5"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String RunAgvScheduler(Map inParameters) {
    assert inParameters.size() == 3, '''Must supply 3 parameters:
                                        <parameter id="command" value="RunAgvScheduler" />
                                        <parameter id="timeOutInSeconds" value="Time to wait for AGV Scheduler to start processing " />
                                        <parameter id="poolName" value="poolName" />'''

    String timeOut = _testCommandHelper.checkParameter('timeOutInSeconds', inParameters);
    String inPoolName = _testCommandHelper.checkParameter('poolName', inParameters);
    try {
      CplexTestUtil.loadNeededLibraries();
      Logger.getLogger("com.navis.control.business.taskhandlers.AbstractTaskHandler").setLevel(Level.DEBUG);
      Logger.getLogger("com.navis.control.business.model.ControlManagerPea").setLevel(Level.DEBUG);
      Logger.getLogger("com.navis.control.business.dispatch.ControlDispatcherPea").setLevel(Level.DEBUG);
      Logger.getLogger("com.navis.control.business.TransferZoneWorkInstructionProvider").setLevel(Level.DEBUG);
      Logger.getLogger("org.mule.routing.CollectionSplitter").setLevel(Level.ERROR);
      Logger.getLogger("com.navis.control.portal.optimization").setLevel(Level.ERROR);
      Logger.getLogger(AbstractExtensionCallback.class).setLevel(Level.ERROR);

      // AgvQATestUtils has some utility methods regarding Logging for AgvScheduling classes
      // setLogsLessVerbose() : sets Log Level to WARN
      // setLogsMoreVerbose() : sets Log Level to DEBUG
      // setLogLevels(Level.?) : sets Log Level to input Level provided (i.e. Level.INFO);
      // normally It should be WARN, more verbose: should be INFO, most detailed one will be Debug
      AgvQaTestUtils.setLogLevels(Level.INFO);

      DomainQuery dq = QueryUtils.createDomainQuery(ArgoEntity.CHE_POOL)
              .addDqPredicate(PredicateFactory.eq(ArgoField.POOL_NAME, inPoolName));
      ChePool pool = ((ChePool) HibernateApi.getInstance().getUniqueEntityByDomainQuery(dq))
      Long timeOutValue = new Long(timeOut);
      DefaultAgvSolveContext agvSolveContext = new DefaultAgvSolveContext();
      agvSolveContext.setChePoolGkey(pool.getPoolGkey());


      UserContext userContext = ContextHelper.getThreadUserContext();
      String problemTypeId = ControlProblemTypeEnum.AGV_SCHEDULER.getId();
      Long timeOutInMillis = new Long(timeOutValue.longValue() * 1000L);

      Logger.getLogger("com.navis.control.portal.optimization").setLevel(Level.DEBUG);
      Logger.getLogger(AbstractExtensionCallback.class).setLevel(Level.DEBUG);
      ISolveStatusEvent solveStatusEvent = ControlTestUtils.runBasicAgvScheduler(userContext, pool.getPoolGkey(), timeOutInMillis);

      if (solveStatusEvent.isSuccess()) {
        returnString = 'AGV Scheduler started running successfully';
      } else {
        returnString = 'AGV Scheduler failed to start. See logs for more info';
      }
    } catch (Exception inEx) {
      returnString = 'AGV Scheduler failed to start. See logs for more info ' + inEx;
    }
    builder {
      actual_result returnString;
    }
    LOGGER.debug('RunAgvScheduler:' + returnString)
    return builder;
  }

  /**
   * Loads the User defined  Data Provider, Config Provider, Problem solution file
   * But user can specify combination of  Data provider , Config Provider or Problem solution file
   * dataProviderPath=Path of Data Provider
   * configProvider=Config Provider
   * problemSolutionFile=XML file for the problem solution
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=LoadProblemDefinition<br>
   * @return JSON ,
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="LoadProblemDefinition" /&gt;<br>
   * &lt;parameter id="problemTypeId" value="Problem type id" /&gt;<br>
   * &lt;parameter id="dataProviderPath" value="dataProviderPath" /&gt;<br>
   * &lt;parameter id="configProvider" value="configProvider" /&gt;<br>
   * &lt;parameter id="problemSolutionFile" value="file.xml" /&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String LoadProblemDefinition(Map inParameters) {
    assert inParameters.size() == 5, '''Must supply 5 parameters:
                                        <parameter id="command" value="LoadProblemDefinition"/>
                                        <parameter id="problemTypeId" value="Problem type id"/>
                                        <parameter id="dataProviderPath" value="dataProviderPath" />
                                        <parameter id="configProvider" value="configProvider"/>
                                        <parameter id="problemSolutionFile" value="file.xml"/>'''
    HibernateApi _hibernateApi = HibernateApi.getInstance();
    try {
      String problemTypeId = _testCommandHelper.checkParameter('problemTypeId', inParameters);
      String dataProviderPath = _testCommandHelper.checkParameter('dataProviderPath', inParameters);
      String configProviderPath = _testCommandHelper.checkParameter('configProviderPath', inParameters);
      String problemSolutionFilePath = _testCommandHelper.checkParameter('problemSolutionFilePath', inParameters);
      String problemSolutionDescription = 'TZ_DECKER';
      File dataProvider = FileUtil.getFile(dataProviderPath);
      File configProvider = FileUtil.getFile(configProviderPath);
      File problemSolution = FileUtil.getFile(problemSolutionFilePath);
      ProblemSolution solution = OptimizationTestUtils.loadProblemDefinition(ContextHelper.getThreadUserContext(), _hibernateApi, problemSolution, dataProvider, configProvider, problemTypeId, problemSolutionDescription, null);
      returnString = 'User defined Problem Definition successfully loaded with solution gkey = ' + solution.getSolutionGkey();
    } catch (Exception ex) {
      returnString = 'Problem solution was not successfully loaded ' + ex.printStackTrace();
    }
    builder {
      actual_result returnString;
    }
    LOGGER.debug('LoadProblemDefinition:' + returnString)
    return builder;
  }

  /**
   * Loads the System provided problem solutions
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=LoadProblemSolution<br>
   * @return JSON ,
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="LoadProblemSolution" /&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String LoadProblemSolution(Map inParameters) {
    assert inParameters.size() == 1, '''Must supply 1 parameter:
                                        <parameter id="command" value="LoadProblemSolution" />'''
    try {
      OptimizationTestUtils.loadSystemProvidedProblemSolutions();
      returnString = 'System provided Problem Solutions successfully loaded';
    } catch (Exception ex) {
      returnString = 'Problem solutions were not successfully loaded ' + ex;
    }
    builder {
      actual_result returnString;
    }
    LOGGER.debug('LoadProblemSolution:' + returnString)
    return builder;
  }

  /**
   * Runs the ASC Scheduler on the loaded ASC Problem type /solution <br>
   * <a href="http://jira.navis.com/browse/ARGO-44379"> ARGO-44379 </a>
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=RunAscScheduler<br>
   * @return JSON ,
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="RunAscScheduler"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String RunAscSTScheduler(Map inParameters) {
    assert inParameters.size() == 2, '''Must supply 2 parameters:
                                 <parameter id="command" value="RunAscSTScheduler" />
                                 <parameter id="blockName" value="<blockName>" />'''


    String inBlockName = _testCommandHelper.checkParameter('blockName', inParameters);
    UserContext userContext = ContextHelper.getThreadUserContext();
    StackBlock ascBlock = AscTestUtils.findBlockByName(inBlockName);
    Serializable blockGkey = ascBlock.getAbnGkey();

    Logger.getLogger("com.navis.control.business.taskhandlers.AbstractTaskHandler").setLevel(Level.DEBUG);
    Logger.getLogger("com.navis.control.business.model.ControlManagerPea").setLevel(Level.DEBUG);
    Logger.getLogger("com.navis.control.business.dispatch.ControlDispatcherPea").setLevel(Level.DEBUG);
    Logger.getLogger("com.navis.control.business.TransferZoneWorkInstructionProvider").setLevel(Level.DEBUG);
    Logger.getLogger("org.mule.routing.CollectionSplitter").setLevel(Level.ERROR);
    Logger.getLogger("com.navis.control.portal.optimization").setLevel(Level.DEBUG);
    Logger.getLogger(AbstractExtensionCallback.class).setLevel(Level.DEBUG);
    Logger.getLogger("com.navis.control.portal.optimization").setLevel(Level.ERROR);
    Logger.getLogger(AbstractExtensionCallback.class).setLevel(Level.ERROR);
    AscTestUtils.setLogsMoreVerbose();
    //CplexTestUtil.loadNeededLibraries();
    //ControlTestUtils.setupDeckingPenalties();

    // disable/enable config settings as required
    final AscTestConfigurationProvider configProvider = new AscTestConfigurationProvider(AscTestUtils.getDefaultAscstConfigProvider());
    configProvider.setPrepositionEnabled(false); // tests calling this method do not expect prepositions to be enabled by defualt
    final AscSchedulerSolveContext solveContext = new AscSchedulerSolveContext(blockGkey);
    solveContext.setConfigProviderOverride(configProvider);

    ISolveStatusEvent schedulerResult = AscTestUtils.runAscstScheduler(userContext, solveContext);
    if (schedulerResult.isSuccess()) {
      returnString = 'ASC ShortTerm Scheduler started running';
    } else {
      returnString = 'ASC ShortTerm Scheduler failed to start';
    }

    builder {
      actual_result returnString;
    }
    LOGGER.debug('RunAscSTScheduler:' + returnString)
    return builder;
  }

  /**
   * Runs the ASC Scheduler on the loaded ASC Problem type /solution <br>
   * <a href="http://jira.navis.com/browse/ARGO-44379"> ARGO-44379 </a>
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=RunAscScheduler<br>
   * @return JSON ,
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="RunAscScheduler"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String RunAscGMScheduler(Map inParameters) {
    assert inParameters.size() >= 2, '''Must supply 2 parameters:
                                 <parameter id="command" value="RunAscGMScheduler" />
                                 <parameter id="blockName" value="<blockName>" />
                                 <parameter id="houseKeeping" value="false/true" />
                                 <parameter id="preposition" value="false/true" />
                                 <parameter id="positionRevision" value="false/true" />'''


    String inBlockName = _testCommandHelper.checkParameter('blockName', inParameters);

    String inHouseKeeping = '';
    String inPreposition = '';
    String inPositionRevision = '';

    if (inParameters.containsKey('houseKeeping')) {
      inHouseKeeping = inParameters.get('houseKeeping');
    }
    if (inParameters.containsKey('preposition')) {
      inPreposition = inParameters.get('preposition');
    }
    if (inParameters.containsKey('positionRevision')) {
      inPositionRevision = inParameters.get('positionRevision');
    }

    UserContext userContext = ContextHelper.getThreadUserContext();
    StackBlock ascBlock = AscTestUtils.findBlockByName(inBlockName);
    Serializable blockGkey = ascBlock.getAbnGkey();

    Logger.getLogger("com.navis.control.business.taskhandlers.AbstractTaskHandler").setLevel(Level.DEBUG);
    Logger.getLogger("com.navis.control.business.model.ControlManagerPea").setLevel(Level.DEBUG);
    Logger.getLogger("com.navis.control.business.dispatch.ControlDispatcherPea").setLevel(Level.DEBUG);
    Logger.getLogger("com.navis.control.business.TransferZoneWorkInstructionProvider").setLevel(Level.DEBUG);
    Logger.getLogger("org.mule.routing.CollectionSplitter").setLevel(Level.ERROR);
    Logger.getLogger("com.navis.control.portal.optimization").setLevel(Level.DEBUG);
    Logger.getLogger(AbstractExtensionCallback.class).setLevel(Level.DEBUG);
    Logger.getLogger("com.navis.control.portal.optimization").setLevel(Level.ERROR);
    Logger.getLogger(AbstractExtensionCallback.class).setLevel(Level.ERROR);
    AscTestUtils.setLogsMoreVerbose();
    //CplexTestUtil.loadNeededLibraries();
    //ControlTestUtils.setupDeckingPenalties();

    // disable/enable config settings as required
    final AscTestConfigurationProvider configProvider = new AscTestConfigurationProvider(AscTestUtils.getDefaultAscgmConfigProvider());
    configProvider.setPrepositionEnabled(false); // tests calling this method do not expect prepositions to be enabled by defualt
    final AscSchedulerSolveContext solveContext = new AscSchedulerSolveContext(blockGkey);


    if (inHouseKeeping != null) {
      configProvider.setHousekeepingEnabled(Boolean.valueOf(inHouseKeeping));
    }

    if (inPreposition != null) {
      configProvider.setPrepositionEnabled(Boolean.valueOf(inPreposition));
    }
    if (inPositionRevision != null) {
      configProvider.setRevisionEnabled(Boolean.valueOf(inPositionRevision))
    }
    //modelConfig.setWhateverPropertyYouAreTesting()
    solveContext.setConfigProviderOverride(configProvider)

    ISolveStatusEvent schedulerResult = AscTestUtils.runAscgmScheduler(userContext, solveContext);
    if (schedulerResult.isSuccess()) {
      returnString = 'ASC Grand Model Scheduler started running';
    } else {
      returnString = 'ASC Grand Model Scheduler failed to start';
    }

    builder {
      actual_result returnString;
    }
    LOGGER.debug('RunAscGMScheduler:' + returnString)
    return builder;
  }


  private static final String SCHEDULER_JOB_NAME = "GLOBAL";
}
