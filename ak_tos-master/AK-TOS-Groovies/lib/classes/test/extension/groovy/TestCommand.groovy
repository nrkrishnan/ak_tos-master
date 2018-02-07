package test.extension.groovy

import org.apache.log4j.Level
import org.apache.log4j.Logger

/**
 * Created with IntelliJ IDEA.
 * User: rajansh
 * Date: 29/10/13
 * Time: 5:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestCommand {

  /** handler to invoke the fields in helper class */
  test.extension.groovy.TestCommandHelper _testCommandHelper = new test.extension.groovy.TestCommandHelper();

  //Delegate all sub classes of testcommand , so that all the methods can be invoked from the class 'TestCommand' itself
  @Delegate test.extension.groovy.TestCommandPurge _testCommandPurge = new test.extension.groovy.TestCommandPurge()
  @Delegate test.extension.groovy.TestCommandCreate _testCommandCreate = new test.extension.groovy.TestCommandCreate()
  @Delegate test.extension.groovy.TestCommandECS _testCommandECS = new test.extension.groovy.TestCommandECS()
  @Delegate test.extension.groovy.TestCommandInit _testCommandInit = new test.extension.groovy.TestCommandInit()
  @Delegate test.extension.groovy.TestCommandManipulation _testCommandManipulation = new test.extension.groovy.TestCommandManipulation()
  @Delegate test.extension.groovy.TestCommandReport _testCommandReport = new test.extension.groovy.TestCommandReport()
  @Delegate test.extension.groovy.TestCommandScheduler _testCommandScheduler = new test.extension.groovy.TestCommandScheduler()
  @Delegate test.extension.groovy.TestCommandTEAMS _testCommandTEAMS = new test.extension.groovy.TestCommandTEAMS()
  @Delegate test.extension.groovy.TestCommandOCR _testCommandOCR = new test.extension.groovy.TestCommandOCR()
  @Delegate test.extension.groovy.TestCommandUpdate _testCommandUpdate = new test.extension.groovy.TestCommandUpdate()
  @Delegate test.extension.groovy.TestCommandListenWait _testCommandListenWait = new test.extension.groovy.TestCommandListenWait()
  @Delegate test.extension.groovy.TestCommandSnxEdi _testCommandSnxEdi = new test.extension.groovy.TestCommandSnxEdi()
  @Delegate test.extension.groovy.TestCommandRGC _testCommandRGC = new test.extension.groovy.TestCommandRGC()

  TestCommand() {
    Logger.getLogger(test.extension.groovy.TestCommandReport.class).setLevel(Level.DEBUG)
    Logger.getLogger(test.extension.groovy.TestCommandCreate.class).setLevel(Level.DEBUG)
    Logger.getLogger(test.extension.groovy.TestCommandECS.class).setLevel(Level.DEBUG)
    Logger.getLogger(test.extension.groovy.TestCommandInit.class).setLevel(Level.DEBUG)
    Logger.getLogger(test.extension.groovy.TestCommandUpdate.class).setLevel(Level.DEBUG)
    Logger.getLogger(test.extension.groovy.TestCommandManipulation.class).setLevel(Level.DEBUG)
    Logger.getLogger(test.extension.groovy.TestCommandOCR.class).setLevel(Level.DEBUG)
    Logger.getLogger(test.extension.groovy.TestCommandPurge.class).setLevel(Level.DEBUG)
    Logger.getLogger(test.extension.groovy.TestCommandScheduler.class).setLevel(Level.DEBUG)
    Logger.getLogger(test.extension.groovy.TestCommandTEAMS.class).setLevel(Level.DEBUG)
    Logger.getLogger(test.extension.groovy.TestCommandListenWait.class).setLevel(Level.DEBUG)
    Logger.getLogger(test.extension.groovy.TestCommandSnxEdi.class).setLevel(Level.DEBUG)
    Logger.getLogger(test.extension.groovy.TestCommandRGC.class).setLevel(Level.DEBUG)
    //enabling few important N4 class loggers by default when an API is invoked
    Logger.getLogger("com.navis.external.framework.AbstractExtensionCallback").setLevel(Level.DEBUG);
    Logger.getLogger("dev.com.navis.control.flowlog").setLevel(Level.DEBUG);
    Logger.getLogger("com.navis.control.portal.optimization").setLevel(Level.DEBUG);
    Logger.getLogger("com.navis.control.esb").setLevel(Level.DEBUG);
    Logger.getLogger("com.navis.control.eci").setLevel(Level.DEBUG);
    Logger.getLogger("com.navis.control.business.taskhandlers.AbstractTaskHandler").setLevel(Level.DEBUG);
  }

  /**
   * Entry point to this class
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * <parameter id="command" value="<API name to execute>" />
   * <parameter id="<Refer each API>" value="<Refer each API>" />
   * @return JSON , output from the respective API call
   */
  public String execute(Map inParameters) {
    assert inParameters.size() > 0, '''Must supply at least 1 parameter:
                                       <parameter id="command" value="<API name>" />''';
    String methodName = _testCommandHelper.checkParameter('command', inParameters);

    this."$methodName"(inParameters);

  }
}
