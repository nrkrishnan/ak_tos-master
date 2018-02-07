/*
 * Copyright (c) 2013 Navis LLC. All Rights Reserved.
 *
 */

package test.extension.groovy

/**
 * Created with IntelliJ IDEA.
 * User: rajansh
 * Date: 24/8/13
 * Time: 6:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestCommandBasicEmulationData {

  //main variables for all emaulation data
  String qcId = ''
  String carryChe = ''
  String cvId = ''

  //general fields common for all scenarios
  String moveKind = ''
  String platformScenario = ''
  String isTwin = ' '

  //for single container
  String qcPlatform = ''
  String qcMainTrolley = ''
  String qcPortalTrolley = ''
  String teamsPosQcTp = ''
  String qcRefId = ''
  String teamsPosAgv = ''
  String teamsPosAsc = ''

  //for Twin containers
  String teamsPos1QCPlatform = ''
  String teamsPos2QCPlatform = ''
  String teamsPos1QCMainTrolley = ''
  String teamsPos2QCMainTrolley = ''
  String teamsPos1Destination = ''
  String teamsPos2Destination = ''
  String qcRef1Id = ''
  String qcRef2Id = ''
  String teamsPos1QCPortalTrolley = ''
  String teamsPos2QCPortalTrolley = ''
  String teamsPos1Agv = ''
  String teamsPos2Agv = ''

  boolean supportMultipleJobs = false

  public void getDefaultData(Map inParameters) {
    setQcId(inParameters.get('craneId'))
    setCarryChe(inParameters.get('cheId'))
    setQcPlatform('QCPLATFORM.' + inParameters.get('craneId') + '.WS.C')
    setQcMainTrolley('QCSPREADER.' + inParameters.get('craneId') + '.M.C.C')
    setQcPortalTrolley('QCSPREADER.' + inParameters.get('craneId') + '.P.C.C')
    setTeamsPosQcTp('QCTP.' + inParameters.get('craneId'))
    setMoveKind(inParameters.get('moveKind'))
    setPlatformScenario(inParameters.get('platformScenario'))
    setTeamsPosAgv('AGV.' + inParameters.get('cheId') + '.C.F')
    setCvId("VESSEL." + inParameters.get('vesselVisitId'))

    //if its a twin load/ discharge then do some more value settings
    if (inParameters.containsKey('isTwin')) {
      setIsTwin(inParameters.get('isTwin'))

      def unitIdList = inParameters.get('unitId').toString()
      if (unitIdList.contains(',')) { //start emulation for multiple jobs (multiple containers planned for load/discharge)
        String[] unitIds = unitIdList.split(',');
        if (unitIds.length >= 2) { //iterate each container and add it to ref id which will be then split and added into emulation scenario
          //  qcRefId += getQcId() + '_' + it + '_REF' + ','
          //TODO - parse multiple unit Id's in case of twin

          /*setQcRef1Id(getQcId() + '_' + unitIds[0] + '_REF')
          setQcRef2Id(getQcId() + '_' + unitIds[1] + '_REF')*/
          setQcRef1Id(unitIds[0] + '_REF')
          setQcRef2Id(unitIds[1] + '_REF')
        }
        //qcRefId = truncateEndingComma(qcRefId)
        supportMultipleJobs = true
      }
    } else { // start emulation for a single job (single container planned for load/discharge)
      setQcRefId(inParameters.get('unitId') + '_REF')
    }

    //set twin data only if the scenario is twin load or discharge
    if (inParameters.containsKey('isTwin')) {
      if (inParameters.get('isTwin').toString().equalsIgnoreCase('True')) {
        getDefaultDataForTwin(inParameters)
      }
    }
  }

  protected void getDefaultDataForTwin(Map inParams) {
    setTeamsPos1QCPlatform('QCPLATFORM.' + getQcId() + '.WS.H')
    setTeamsPos2QCPlatform('QCPLATFORM.' + getQcId() + '.WS.L')
    setTeamsPos1QCMainTrolley('QCSPREADER.' + getQcId() + '.M.C.H')
    setTeamsPos2QCMainTrolley('QCSPREADER.' + getQcId() + '.M.C.L')
    setTeamsPos1QCPortalTrolley('QCSPREADER.' + getQcId() + '.P.C.H')
    setTeamsPos2QCPortalTrolley('QCSPREADER.' + getQcId() + '.P.C.L')
    setTeamsPos1Agv('AGV.' + getCarryChe() + '.F.F')
    setTeamsPos2Agv('AGV.' + getCarryChe() + '.R.F')
  }

  String getQcId() {
    return qcId
  }

  void setQcId(String inQcId) {
    qcId = inQcId
  }

  String getQcPlatform() {
    return qcPlatform
  }

  void setQcPlatform(String inQcPlatform) {
    qcPlatform = inQcPlatform
  }

  String getQcMainTrolley() {
    return qcMainTrolley
  }

  void setQcMainTrolley(String inQcMainTrolley) {
    qcMainTrolley = inQcMainTrolley
  }

  String getQcPortalTrolley() {
    return qcPortalTrolley
  }

  void setQcPortalTrolley(String inQcPortalTrolley) {
    qcPortalTrolley = inQcPortalTrolley
  }

  String getTeamsPosQcTp() {
    return teamsPosQcTp
  }

  void setTeamsPosQcTp(String inTeamsPosQcTp) {
    teamsPosQcTp = inTeamsPosQcTp
  }

  String getQcRefId() {
    return qcRefId
  }

  void setQcRefId(String inQcRefId) {
    qcRefId = inQcRefId
  }

  String getMoveKind() {
    return moveKind
  }

  void setMoveKind(String inMoveKind) {
    moveKind = inMoveKind
  }

  String getPlatformScenario() {
    return platformScenario
  }

  void setPlatformScenario(String inPlatformScenario) {
    platformScenario = inPlatformScenario
  }

  String getTeamsPosAgv() {
    return teamsPosAgv
  }

  void setTeamsPosAgv(String inTeamsPosAgv) {
    teamsPosAgv = inTeamsPosAgv
  }

  String getCarryChe() {
    return carryChe
  }

  void setCarryChe(String inCarryChe) {
    carryChe = inCarryChe
  }

  String getTeamsPosAsc() {
    return teamsPosAsc
  }

  void setTeamsPosAsc(String inTeamsPosAsc) {
    teamsPosAsc = inTeamsPosAsc
  }

  String getCvId() {
    return cvId
  }

  void setCvId(String inCvId) {
    cvId = inCvId
  }

  String getTeamsPos1QCPlatform() {
    return teamsPos1QCPlatform
  }

  void setTeamsPos1QCPlatform(String inTeamsPos1QCPlatform) {
    teamsPos1QCPlatform = inTeamsPos1QCPlatform
  }

  String getTeamsPos2QCPlatform() {
    return teamsPos2QCPlatform
  }

  void setTeamsPos2QCPlatform(String inTeamsPos2QCPlatform) {
    teamsPos2QCPlatform = inTeamsPos2QCPlatform
  }

  String getTeamsPos1QCMainTrolley() {
    return teamsPos1QCMainTrolley
  }

  void setTeamsPos1QCMainTrolley(String inTeamsPos1QCMainTrolley) {
    teamsPos1QCMainTrolley = inTeamsPos1QCMainTrolley
  }

  String getTeamsPos2QCMainTrolley() {
    return teamsPos2QCMainTrolley
  }

  void setTeamsPos2QCMainTrolley(String inTeamsPos2QCMainTrolley) {
    teamsPos2QCMainTrolley = inTeamsPos2QCMainTrolley
  }

  String getTeamsPos1Destination() {
    return teamsPos1Destination
  }

  void setTeamsPos1Destination(String inTeamsPos1Destination) {
    teamsPos1Destination = inTeamsPos1Destination
  }

  String getTeamsPos2Destination() {
    return teamsPos2Destination
  }

  void setTeamsPos2Destination(String inTeamsPos2Destination) {
    teamsPos2Destination = inTeamsPos2Destination
  }

  String getQcRef1Id() {
    return qcRef1Id
  }

  void setQcRef1Id(String inQcRef1Id) {
    qcRef1Id = inQcRef1Id
  }

  String getQcRef2Id() {
    return qcRef2Id
  }

  void setQcRef2Id(String inQcRef2Id) {
    qcRef2Id = inQcRef2Id
  }

  String getTeamsPos1QCPortalTrolley() {
    return teamsPos1QCPortalTrolley
  }

  void setTeamsPos1QCPortalTrolley(String inTeamsPos1QCPortalTrolley) {
    teamsPos1QCPortalTrolley = inTeamsPos1QCPortalTrolley
  }

  String getTeamsPos2QCPortalTrolley() {
    return teamsPos2QCPortalTrolley
  }

  void setTeamsPos2QCPortalTrolley(String inTeamsPos2QCPortalTrolley) {
    teamsPos2QCPortalTrolley = inTeamsPos2QCPortalTrolley
  }

  String getTeamsPos1Agv() {
    return teamsPos1Agv
  }

  void setTeamsPos1Agv(String inTeamsPos1Agv) {
    teamsPos1Agv = inTeamsPos1Agv
  }

  String getTeamsPos2Agv() {
    return teamsPos2Agv
  }

  void setTeamsPos2Agv(String inTeamsPos2Agv) {
    teamsPos2Agv = inTeamsPos2Agv
  }

  String getIsTwin() {
    return isTwin
  }

  void setIsTwin(String inTwin) {
    isTwin = inTwin
  }

  boolean getSupportMultipleJobs() {
    return supportMultipleJobs
  }

  void setSupportMultipleJobs(boolean inSupportMultipleJobs) {
    supportMultipleJobs = inSupportMultipleJobs
  }

  private def truncateEndingComma(def str) {
    if (str.trim().endsWith(',')) {
      str = str.substring(0, str.length() - 1);
    }
    return str;
  }
}
