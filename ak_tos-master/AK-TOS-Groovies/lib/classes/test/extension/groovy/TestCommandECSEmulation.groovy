/*
 * Copyright (c) 2013 Navis LLC. All Rights Reserved.
 *
 */

package test.extension.groovy

import com.navis.control.data.BasicVesselDschOnAgvJobTestData
import com.navis.control.eci.emulation.EciEmulationQcStepData
import com.navis.control.eci.emulation.EciEmulationScenario
import com.navis.control.esb.teams.emulation.TeamsEmulationTestHelper

/**
 * Created with IntelliJ IDEA.
 * User: rajansh
 * Date: 24/8/13
 * Time: 6:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestCommandECSEmulation {
  test.extension.groovy.TestCommandBasicEmulationData _basicEmulationData
  final TeamsEmulationTestHelper _teamsEmulationTestHelper

  TestCommandECSEmulation() {
    _teamsEmulationTestHelper = new TeamsEmulationTestHelper()
  }

  /**
   * Overriding default constructor to initiliaze certain data
   */
  TestCommandECSEmulation(test.extension.groovy.TestCommandBasicEmulationData inBasicEmulationData) {
    _basicEmulationData = inBasicEmulationData
    _teamsEmulationTestHelper = new TeamsEmulationTestHelper()
  }

  /**
   * Starts emulation scenario for loads or discharges with the given properties
   * It handles the below cases
   *  Vessel Discharge With Platform
   *  Vessel Discharge WithOut Platform
   *  Vessel Load With Platform
   *  Vessel Load WithOut Platform
   *
   *
   * @return
   */
  public String loadAdvancedEmulationScenario() {   //
    String moveKind = _basicEmulationData.getMoveKind()
    String platformScenario = _basicEmulationData.getPlatformScenario()
    String isTwin = _basicEmulationData.getIsTwin()
    final EciEmulationQcStepData[] qcStepDatas
    final EciEmulationQcStepData[] qcTwinStepDatas

    try {
      //If moveKind is load or discharge, gets into it
      if (moveKind.equalsIgnoreCase('Load') || moveKind.equalsIgnoreCase('Discharge')) {
        final String handoffVisitKey
        final String[] referenceIds
        // create a standard scenario configuration object
        final EciEmulationScenario eciEmulationScenario = _teamsEmulationTestHelper.createStandardEmulationScenario();
        // set handoff handshake timeout to 5 minutes (or ??)
        eciEmulationScenario.setHandshakeTimeoutMsec(300000L);

        // single/twin container loads
        if (moveKind.equalsIgnoreCase('Load')) {
          //set load data according to the platform Scenario
          if (platformScenario.equalsIgnoreCase('With')) {
            if (!isTwin.isEmpty() & isTwin.equalsIgnoreCase('true')) { //twin load scenario
              qcStepDatas = getQCStepDataForTwinVslLoadWithPlatformForContainer1()
              qcTwinStepDatas = getQCStepDataForTwinVslLoadWithPlatformForContainer2()
            } else {    //single load with platform
              qcStepDatas = getQCStepDataForVslLoadWithPlatform(qcStepDatas)   //get QC step data for vsl load with platform  - single load
            }
          } else if (platformScenario.equalsIgnoreCase('Without')) {   //loads doesnt involve QC Platform
            if (!isTwin.isEmpty() & isTwin.equalsIgnoreCase('true')) { //twin load scenario
              qcStepDatas = getQCStepDataForTwinVslLoadWithoutPlatformForContainer1()
              qcTwinStepDatas = getQCStepDataForTwinVslLoadWithoutPlatformForContainer2()
            } else {  //single load without platform
              qcStepDatas = getQCStepDataForVslLoadWithOutPlatform(qcStepDatas) //get QC step data for vsl load without platform
            }
          }
        }

        //single or twin discharge
        if (moveKind.equalsIgnoreCase('Discharge')) {
          //set discharge data according to the platform Scenario
          if (platformScenario.equalsIgnoreCase('With')) {
            if (!isTwin.isEmpty() & isTwin.equalsIgnoreCase('true')) { //twin discharge scenario
              qcStepDatas = getQCStepDataForTwinVslDischargeWithPlatformForContainer1()
              qcTwinStepDatas = getQCStepDataForTwinVslDischargeWithPlatformForContainer2()
            } else {
              qcStepDatas = getQCStepDataForVslDschWithPlatform(qcStepDatas)   //get QC step data for vsl discharge with platform
            }
          } else if (platformScenario.equalsIgnoreCase('Without')) {
            if (!isTwin.isEmpty() & isTwin.equalsIgnoreCase('true')) { //twin discharge scenario
              qcStepDatas = getQCStepDataForTwinVslDischargeWithoutPlatformForContainer1()
              qcTwinStepDatas = getQCStepDataForTwinVslDischargeWithoutPlatformForContainer2()
            } else {
              qcStepDatas = getQCStepDataForVslDscWithOutPlatform(qcStepDatas) //get QC step data for vsl discharge with out platform
            }
          }
        }

        // pass to it the "REFERENCE ID to TEAMS QC STEPS" controlling the sequence of move steps
        eciEmulationScenario.setReferenceIdToQcSteps(_basicEmulationData.getQcRefId(), qcStepDatas);
        handoffVisitKey = _basicEmulationData.getTeamsPosQcTp();  //get handshake key to hand off container from QC to AGV

        if (!isTwin.isEmpty() & isTwin.equalsIgnoreCase('true')) { //twin load/discharge scenario
          // pass the "handoff visit key to QC Reference IDs" entry allowing the AGV and QC to build a common handoff key
          //for container 1
          referenceIds = [_basicEmulationData.getQcRef1Id()];
          eciEmulationScenario.setHandoffVisitKeyToReferenceIds(handoffVisitKey, referenceIds);

          //for container 2
          referenceIds = [_basicEmulationData.getQcRef2Id()];
          eciEmulationScenario.setHandoffVisitKeyToReferenceIds(handoffVisitKey, referenceIds);
        } else {          //for single vsl load/discharge
          referenceIds = [_basicEmulationData.getQcRefId()];
          eciEmulationScenario.setHandoffVisitKeyToReferenceIds(handoffVisitKey, referenceIds);
        }
        //now start emulation as all the qcSteps,handShakeKey and refIds are all set
        startEmulationScenario(eciEmulationScenario)
      }
      return 'ECSEmulation Started'
    } catch (Exception ex) {
      return 'Failed to start ECSEmulation ' + ex
    }

  }

  /**
   * Returns QcStepdata for vessel load operation with platform
   * @param qcStepDatas
   * @return
   */
  private EciEmulationQcStepData[] getQCStepDataForVslLoadWithPlatform(EciEmulationQcStepData[] qcStepDatas) {
    qcStepDatas = [
            new EciEmulationQcStepData(_basicEmulationData.getQcPlatform(), _basicEmulationData.getTeamsPosQcTp()),
            new EciEmulationQcStepData(_basicEmulationData.getQcMainTrolley()),
            new EciEmulationQcStepData(_basicEmulationData.getCvId())
    ];
    return qcStepDatas;
  }

  /**
   * Returns QcStepdata for vessel load operation with out platform
   * @param qcStepDatas
   * @return
   */
  private EciEmulationQcStepData[] getQCStepDataForVslLoadWithOutPlatform(EciEmulationQcStepData[] qcStepDatas) {
    qcStepDatas = [
            new EciEmulationQcStepData(_basicEmulationData.getTeamsPosQcTp()),
            new EciEmulationQcStepData(_basicEmulationData.getQcMainTrolley()),
            new EciEmulationQcStepData(_basicEmulationData.getCvId())
    ];
    return qcStepDatas;
  }

  /**
   * Returns QcStepdata for vessel discharge operation with platform
   * @param qcStepDatas
   * @return
   */
  private EciEmulationQcStepData[] getQCStepDataForVslDschWithPlatform(EciEmulationQcStepData[] qcStepDatas) {
    qcStepDatas = [
            new EciEmulationQcStepData(_basicEmulationData.getQcPlatform()),
            new EciEmulationQcStepData(_basicEmulationData.getQcPortalTrolley()),
            new EciEmulationQcStepData(_basicEmulationData.getTeamsPosAgv(), _basicEmulationData.getTeamsPosQcTp())
    ];
    return qcStepDatas;
  }

  /**
   * Returns QcStepdata for vessel discharge operation with out platform
   * @param qcStepDatas
   * @return
   */
  private EciEmulationQcStepData[] getQCStepDataForVslDscWithOutPlatform(EciEmulationQcStepData[] qcStepDatas) {
    qcStepDatas = [
            new EciEmulationQcStepData(_basicEmulationData.getQcMainTrolley()),
            new EciEmulationQcStepData(_basicEmulationData.getTeamsPosAgv(), _basicEmulationData.getTeamsPosQcTp())
    ];
    return qcStepDatas;
  }

  /**
   * Returns QcStepdata for twin vessel load operation with platform
   * @param qcStepDatas
   * @return
   */
  private EciEmulationQcStepData[] getQCStepDataForTwinVslLoadWithPlatformForContainer1(EciEmulationQcStepData[] qcStepDatas) {
    qcStepDatas = [
            new EciEmulationQcStepData(_basicEmulationData.getTeamsPos1QCPlatform(), _basicEmulationData.getTeamsPosQcTp()),
            new EciEmulationQcStepData(_basicEmulationData.getTeamsPos1QCMainTrolley()),
            new EciEmulationQcStepData(_basicEmulationData.getCvId())
    ];
    return qcStepDatas;
  }

  /**
   * Returns QcStepdata for twin vessel load operation with platform
   * @param qcStepDatas
   * @return
   */
  private EciEmulationQcStepData[] getQCStepDataForTwinVslLoadWithPlatformForContainer2(EciEmulationQcStepData[] qcStepDatas) {
    qcStepDatas = [
            new EciEmulationQcStepData(_basicEmulationData.getTeamsPos2QCPlatform(), _basicEmulationData.getTeamsPosQcTp()),
            new EciEmulationQcStepData(_basicEmulationData.getTeamsPos2QCMainTrolley()),
            new EciEmulationQcStepData(_basicEmulationData.getCvId())
    ];
    return qcStepDatas;
  }

  /**
   * Returns QcStepdata for twin vessel load operation without platform
   * @param qcStepDatas
   * @return
   */
  private EciEmulationQcStepData[] getQCStepDataForTwinVslLoadWithoutPlatformForContainer1(EciEmulationQcStepData[] qcStepDatas) {
    qcStepDatas = [
            new EciEmulationQcStepData(_basicEmulationData.getTeamsPosQcTp()),
            new EciEmulationQcStepData(_basicEmulationData.getTeamsPos1QCMainTrolley()),
            new EciEmulationQcStepData(_basicEmulationData.getCvId())
    ];
    return qcStepDatas;
  }

  /**
   * Returns QcStepdata for twin vessel load operation without platform
   * @param qcStepDatas
   * @return
   */
  private EciEmulationQcStepData[] getQCStepDataForTwinVslLoadWithoutPlatformForContainer2(EciEmulationQcStepData[] qcStepDatas) {
    qcStepDatas = [
            new EciEmulationQcStepData(_basicEmulationData.getTeamsPosQcTp()),
            new EciEmulationQcStepData(_basicEmulationData.getTeamsPos2QCMainTrolley()),
            new EciEmulationQcStepData(_basicEmulationData.getCvId())
    ];
    return qcStepDatas;
  }

  /**
   * Returns QcStepdata for twin vessel discharge operation with platform
   * @param qcStepDatas
   * @return
   */
  private EciEmulationQcStepData[] getQCStepDataForTwinVslDischargeWithPlatformForContainer1(EciEmulationQcStepData[] qcStepDatas) {
    qcStepDatas = [
            new EciEmulationQcStepData(_basicEmulationData.getTeamsPos1QCPlatform()),
            new EciEmulationQcStepData(_basicEmulationData.getTeamsPos1QCPortalTrolley()),
            new EciEmulationQcStepData(_basicEmulationData.getTeamsPos1Agv(), _basicEmulationData.getTeamsPosQcTp())
    ];
    return qcStepDatas;
  }

  /**
   * Returns QcStepdata for twin vessel load operation with platform
   * @param qcStepDatas
   * @return
   */
  private EciEmulationQcStepData[] getQCStepDataForTwinVslDischargeWithPlatformForContainer2(EciEmulationQcStepData[] qcStepDatas) {
    qcStepDatas = [
            new EciEmulationQcStepData(_basicEmulationData.getTeamsPos2QCPlatform()),
            new EciEmulationQcStepData(_basicEmulationData.getTeamsPos2QCPortalTrolley()),
            new EciEmulationQcStepData(_basicEmulationData.getTeamsPos2Agv(), _basicEmulationData.getTeamsPosQcTp())
    ];
    return qcStepDatas;
  }

  /**
   * Returns QcStepdata for twin vessel load operation without platform
   * @param qcStepDatas
   * @return
   */
  private EciEmulationQcStepData[] getQCStepDataForTwinVslDischargeWithoutPlatformForContainer1(EciEmulationQcStepData[] qcStepDatas) {
    qcStepDatas = [
            new EciEmulationQcStepData(_basicEmulationData.getTeamsPos1QCMainTrolley()),
            new EciEmulationQcStepData(_basicEmulationData.getTeamsPos1Agv(), _basicEmulationData.getTeamsPosQcTp())
    ];
    return qcStepDatas;
  }

  /**
   * Returns QcStepdata for twin vessel load operation without platform
   * @param qcStepDatas
   * @return
   */
  private EciEmulationQcStepData[] getQCStepDataForTwinVslDischargeWithoutPlatformForContainer2(EciEmulationQcStepData[] qcStepDatas) {
    qcStepDatas = [
            new EciEmulationQcStepData(_basicEmulationData.getTeamsPos2QCMainTrolley()),
            new EciEmulationQcStepData(_basicEmulationData.getTeamsPos2Agv(), _basicEmulationData.getTeamsPosQcTp())
    ];
    return qcStepDatas;
  }

  /**
   * returns standard emulation scenario which takes care of Asc/Agv emulation w/o QC involves
   * InterStack Moves
   * IntraStack Moves
   */
  private String loadStandardEmulationScenario() {   //loadEmulationScenario
    final BasicVesselDschOnAgvJobTestData basicJobTestData = BasicVesselDschOnAgvJobTestData.getDefaultData()

    final EciEmulationQcStepData[] emulationSteps = [
            new EciEmulationQcStepData(basicJobTestData.getTeamsPosQcPlatform()),
            new EciEmulationQcStepData(basicJobTestData.getTeamsPosQcPortalTrolley()),
            new EciEmulationQcStepData(basicJobTestData.getTeamsPosAgv(), basicJobTestData.getTeamsPosQcTp())
    ];

    // create a standard scenario configuration object
    final EciEmulationScenario eciEmulationScenario = _teamsEmulationTestHelper.createStandardEmulationScenario();

    // set handoff handshake timeout to 5 minutes (or ??)
    eciEmulationScenario.setHandshakeTimeoutMsec(300000L);

    // pass to it the "REFERENCE ID to TEAMS QC STEPS" controlling the sequence of move steps
    eciEmulationScenario.setReferenceIdToQcSteps(basicJobTestData.getQcRefId(), emulationSteps);

    // pass the "handoff visit key to QC Reference IDs" entry allowing the AGV and QC to build a common handoff key
    final String[] referenceIds = [basicJobTestData.getQcRefId()];
    eciEmulationScenario.setHandoffVisitKeyToReferenceIds(basicJobTestData.getTeamsPosQcTp(), referenceIds);

    startEmulationScenario()

    return 'ECS Emulation started'
  }

  /**
   * Starts emulation with the scenario properties provided
   * @param inEciEmulationScenario
   * @return
   */
  public String startEmulationScenario(EciEmulationScenario inEciEmulationScenario) {
    // now set the new emulation scenario configuration into the emulation context
    _teamsEmulationTestHelper.getEciEmulationContext().setEciEmulationScenario(inEciEmulationScenario);

    // start the emulation processes
    _teamsEmulationTestHelper.startEmulationScenario();
  }

  /**
   * Stops emulation
   * @return
   */
  public String stopEmulationScenario() {
    try {
      _teamsEmulationTestHelper.getEciEmulationContext().setEsbConfigurationTypeEnum(
              _teamsEmulationTestHelper.getStandardTeamsEmulationEsbConfigurationTypeEnum()
      );
      _teamsEmulationTestHelper.stopEmulationScenario();
    } catch (Exception ex) {
      return 'Failed to stop ECS Emulation : ' + ex
    }
    return 'ECSEmulation Stopped'
  }
}
