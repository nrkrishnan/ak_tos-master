/*
 * Copyright (c) 2013 Navis LLC. All Rights Reserved.
 *
 */

package test.extension.groovy

import com.navis.argo.business.atoms.WiMoveKindEnum
import com.navis.control.configuration.EciConfigurationHelper
import com.navis.control.eci.api.IEciConfiguration
import com.navis.control.eci.teams.TeamsEciAdapter
import com.navis.framework.business.Roastery
import com.navis.framework.persistence.DatabaseHelper
import org.apache.commons.lang.time.DateUtils
import org.jetbrains.annotations.NotNull

/**
 * Class to support mapping of DB fields with the variable names in *Hbr files.
 * It is used mainly for Report and Update APIs, where we need the mapping of user requested parameter
 * to the actual field in DB.
 * This layer is created mainly to avoid future changes/updates directly in TestCommand_backup.groovy each and every time a
 * new attribute is added or the existing one is modified.
 *
 * Created with IntelliJ IDEA.
 * User: Sharanya
 * Date: 1/9/13
 * Time: 12:32 PM
 */
class TestCommandHelper {

  //Work Instruction Attributes
  public String MOVE_STAGE = "wiMoveStage";
  public String DISPATCH_STATE = "wiEcStateDispatch";
  public String CHE_FETCH = "wiEcStateFetch";
  public String CARRY_PUT = "";
  public String CHE_PUT = "";
  public String MOVE_TIME = "wiEstimatedMoveTime";
  public String SERVICE_TIME = "";

  //Work Shift Attributes
  public String START_TIME = "workshiftStartTime";
  public String DURATION = "workshiftDuration";

  //Vessel Visit Attributes
  public String STARTWORK = "vvdTimeStartWork";
  public String ENDWORK = "vvdTimeEndWork";
  public String ATA = "cvATA";
  public String ATD = "cvATD";
  public String PHASE = "cvVisitPhase";

  //Unit Attributes
  public String WEIGHT_KG = 'eqTareWeightKg'
  public String ISO_TYPE = 'eqtypId'
  public String REEFER_POWERED = 'rfrecIsPowered'
  public String DAMAGE_FLAG = 'ueDamageSeverity'
  public String CURRENT_POSITION = 'ufvLastKnownPosition'
  public String LAST_POSITION = 'ufvLastKnownPosition'
  public String INBOUND_CARRIER = 'unitDeclaredIbCv'
  public String HEIGHT = 'eqHeightMm'
  public String OOG = 'unitIsOog'
  public String TEMPERATURE = 'rfreqTempRequiredC'

  //Teams EciAdapter
  public String eciAdapterId = "teams1";

  public HashMap<String, String> ecEventMap = new HashMap();
  public HashMap<String, String> ecEventVarMap = new HashMap();

  //Stack status fields
  public String TEMP_BLOCKED = 'X     ';
  public String MEN_WORKING = '   M  ';
  public String ROAD_WAY = 'R     ';
  public char TEMPBLOCKED_PRESERVE = 'X';
  public char MENWORKING_PRESERVE = 'M';
  public char ROADWAY_PRESERVE = 'R'
  public char RACK_PRESERVE = 'R';
  public String RACK = 'R';
  public String CLEAR = '       ';
  public char TIER_HIGH_1 = '1';
  public char TIER_HIGH_2 = '2';
  public char TIER_HIGH_3 = '3';
  public char TIER_HIGH_4 = '4';
  public char TIER_HIGH_5 = '5';
  public char TIER_HIGH_6 = '6';
  public char TIER_HIGH_7 = '7';
  public char TIER_HIGH_8 = '8';
  public char TIER_HIGH_9 = '9';
  public char ONLY_20 = '2';
  public char ONLY_45 = '5';
  public char ROADWAY = 'R';
  public char HYDRANT = 'H';
  public char BUILDING = 'B';
  public char AISLE = 'A';
  public char POLE = 'P';
  public char TP_EXTENDED = 'T';
  public char PORTHOLE_TOWER = 'Z';
  public char PORTHOLE_THIRDTIER = 'I';
  public char PORTHOLE_SECONDTIER = 'U';
  public char PORTHOLE_FIRSTTIER = 'L';
  public char REEFER_HIGH_1 = '1';
  public char REEFER_HIGH_2 = '2';
  public char REEFER_HIGH_3 = '3';
  public char REEFER_HIGH_4 = '4';
  public char REEFER_HIGH_5 = '5';


  TestCommandHelper() {
    addValuesToEventHashMap();
    addValuesToEcEventMap();
  }

  /**
   * This map is invoked by ReportEcEvents
   * Map to have values as below in key value pair
   * Key : <4 letter keyword for ECEvents> DSPT, EARW
   * Value : <Ec event name > Dispatch,EmptyAtRow
   */
  private void addValuesToEventHashMap() {
    //dispatch
    ecEventMap.put('DSPT', 'Dispatch');
    ecEventMap.put('EARW', 'EmptyAtRow')
    //ASC Container Move
    ecEventMap.put('ETRW', 'EmptyToRow')
    ecEventMap.put('LTRW', 'LadenToRow')
    ecEventMap.put('LARW', 'LadenAtRow')
    //AGV Travel To ASC To Receive
    ecEventMap.put('TYCO', 'ToRowToCollect')
    ecEventMap.put('AYCO', 'ArriveAtRowToCollect')
    //AGV Travel To ASC To Deliver
    ecEventMap.put('TYDR', 'ToRowToDrop')
    ecEventMap.put('AYDR', 'ArriveAtRowToDrop')
    // AGV Travel To QC To Receive
    ecEventMap.put('TVCO', 'ToVesselToCollect')
    ecEventMap.put('AVCO', 'ArriveAtVesselToCollect')
    //AGV Travel To QC To Deliver
    ecEventMap.put('TVDR', 'ToVesselToDrop')
    ecEventMap.put('AVDR', 'ArriveAtVesselToDrop')
    //QC Known Container Move (Load)
    ecEventMap.put('QCFL', 'QcFetch')
    ecEventMap.put('QCPL', 'QcPut')
    //common
    ecEventMap.put('CMPL', 'Complete')
    ecEventMap.put('IDLE ', 'Idle')
  }

  /**
   * This map is invoked by Listen/WaitForECEvents
   * Map to have values as below in key value pair
   * Key : <Ec event name > Dispatch,EmptyAtRow
   * Key : <N4 defined Ec_Event_constants> DISPATCH,EMPTY_TO_ROW
   */
  private void addValuesToEcEventMap() {
    //DISPATCH
    ecEventVarMap.put('Dispatch', 'DISPATCH');
    //ASC Container Move
    ecEventVarMap.put('EmptyToRow', 'EMPTY_TO_ROW')
    ecEventVarMap.put('EmptyAtRow', 'EMPTY_AT_ROW')
    ecEventVarMap.put('LadenToRow', 'LADEN_TO_ROW')
    ecEventVarMap.put('LadenAtRow', 'LADEN_AT_ROW')
    //AGV Travel To ASC To Receive
    ecEventVarMap.put('ToRowToCollect', 'TO_ROW_TO_COLLECT')
    ecEventVarMap.put('ArriveAtRowToCollect', 'ARRIVE_AT_ROW_TO_COLLECT')
    //AGV Travel To ASC To Deliver
    ecEventVarMap.put('ToRowToDrop', 'TO_ROW_TO_DROP')
    ecEventVarMap.put('ArriveAtRowToDrop', 'ARRIVE_AT_ROW_TO_DROP')
    // AGV Travel To QC To Receive
    ecEventVarMap.put('ToVesselToCollect', 'TO_VESSEL_TO_COLLECT')
    ecEventVarMap.put('ArriveAtVesselToCollect', 'ARRIVE_AT_VESSEL_TO_COLLECT')
    //AGV Travel To QC To Deliver
    ecEventVarMap.put('ToVesselToDrop', 'TO_VESSEL_TO_DROP')
    ecEventVarMap.put('ArriveAtVesselToDrop', 'ARRIVE_AT_VESSEL_TO_DROP')
    //QC Known Container Move (Load)
    ecEventVarMap.put('QcFetch', 'QC_FETCH')
    ecEventVarMap.put('QcPut', 'QC_PUT')
    //OCR Unit Identify
    ecEventVarMap.put('UnitIdentify', 'UNIT_CAPTURE_IDENTIFY')
    ecEventVarMap.put('UnitImage', 'UNIT_CAPTURE_IMAGE')
    ecEventVarMap.put('TransferAllowed', 'UNIT_TRANSFER_ALLOWED')
    //common
    ecEventVarMap.put('Complete', 'COMPLETE')
    ecEventVarMap.put('Idle', 'IDLE')
  }

  /**
   * Invoked by SetTransferPointUsage
   * @param list
   * @return
   */
  public String getMoveKindAllowedString(ArrayList list) {
    char allowed = 'T';
    String allBitsOff = "FFFFFFFFF";
    char[] moveKindChars = allBitsOff.toCharArray();
    list.each {
      if (WiMoveKindEnum.Receival.equals(WiMoveKindEnum."$it")) {
        moveKindChars[0] = allowed;
      } else if (WiMoveKindEnum.Delivery.equals(WiMoveKindEnum."$it")) {
        moveKindChars[1] = allowed;
      } else if (WiMoveKindEnum.YardShift.equals(WiMoveKindEnum."$it")) {
        moveKindChars[2] = allowed;
      } else if (WiMoveKindEnum.YardMove.equals(WiMoveKindEnum."$it")) {
        moveKindChars[3] = allowed;
      } else if (WiMoveKindEnum.VeslDisch.equals(WiMoveKindEnum."$it")) {
        moveKindChars[4] = allowed;
      } else if (WiMoveKindEnum.VeslLoad.equals(WiMoveKindEnum."$it")) {
        moveKindChars[5] = allowed;
      } else if (WiMoveKindEnum.ShiftOnBoard.equals(WiMoveKindEnum."$it")) {
        moveKindChars[6] = allowed;
      } else if (WiMoveKindEnum.RailDisch.equals(WiMoveKindEnum."$it")) {
        moveKindChars[7] = allowed;
      } else if (WiMoveKindEnum.RailLoad.equals(WiMoveKindEnum."$it")) {
        moveKindChars[8] = allowed;
      }
    }
    return moveKindChars.toString();
  }

  /**
   * Invoked by SetStackStatus
   * Sets the char values accordingly to the given status in xps_stackstatus table
   * @param inStatus
   * @param preverveStatus
   * @return
   */
  public String getStackStatusValue(String inStatus, String preserveStatus) {
    StringBuilder statusBuilder = new StringBuilder(preserveStatus)
    if (inStatus.equalsIgnoreCase('MEN_WORKING')) {
      statusBuilder.setCharAt(3, MENWORKING_PRESERVE)
    } else if (inStatus.equalsIgnoreCase('TEMP_BLOCKED')) {
      statusBuilder.setCharAt(0, TEMPBLOCKED_PRESERVE)
    } else if (inStatus.equalsIgnoreCase('ROAD_WAY')) {
      statusBuilder.setCharAt(0, ROADWAY_PRESERVE)
    } else if (inStatus.equalsIgnoreCase('BUILDING')) {
      statusBuilder.setCharAt(0, BUILDING)
    } else if (inStatus.equalsIgnoreCase('HYDRANT')) {
      statusBuilder.setCharAt(0, HYDRANT)
    } else if (inStatus.equalsIgnoreCase('AISLE')) {
      statusBuilder.setCharAt(0, AISLE)
    } else if (inStatus.equalsIgnoreCase('POLE')) {
      statusBuilder.setCharAt(0, POLE)
    } else if (inStatus.equalsIgnoreCase('TP_EXTENDED')) {
      statusBuilder.setCharAt(0, TP_EXTENDED)
    } else if (inStatus.equalsIgnoreCase('RACK') || preserveStatus.contains('R')) {
      statusBuilder.setCharAt(5, RACK_PRESERVE)
    } else if (preserveStatus.size() >= 7) {
      if (inStatus.equalsIgnoreCase('RACK')) {
        statusBuilder.setCharAt(6, RACK_PRESERVE)
      }
    }

    //stacking limits
    if (inStatus.equalsIgnoreCase('TIER_HIGH_1')) {
      statusBuilder.setCharAt(1, TIER_HIGH_1)
    } else if (inStatus.equalsIgnoreCase('TIER_HIGH_2')) {
      statusBuilder.setCharAt(1, TIER_HIGH_2)
    } else if (inStatus.equalsIgnoreCase('TIER_HIGH_3')) {
      statusBuilder.setCharAt(1, TIER_HIGH_3)
    } else if (inStatus.equalsIgnoreCase('TIER_HIGH_4')) {
      statusBuilder.setCharAt(1, TIER_HIGH_4)
    } else if (inStatus.equalsIgnoreCase('TIER_HIGH_5')) {
      statusBuilder.setCharAt(1, TIER_HIGH_5)
    } else if (inStatus.equalsIgnoreCase('TIER_HIGH_6')) {
      statusBuilder.setCharAt(1, TIER_HIGH_6)
    } else if (inStatus.equalsIgnoreCase('TIER_HIGH_7')) {
      statusBuilder.setCharAt(1, TIER_HIGH_7)
    } else if (inStatus.equalsIgnoreCase('TIER_HIGH_8')) {
      statusBuilder.setCharAt(1, TIER_HIGH_8)
    } else if (inStatus.equalsIgnoreCase('TIER_HIGH_9')) {
      statusBuilder.setCharAt(1, TIER_HIGH_9)
    }

    //chassis values
    if (inStatus.equalsIgnoreCase('Only20_Chassis')) {
      statusBuilder.setCharAt(4, ONLY_20)
    } else if (inStatus.equalsIgnoreCase('Only45_Chassis')) {
      statusBuilder.setCharAt(4, ONLY_45)
    }

    //reefer status
    if (inStatus.equalsIgnoreCase('REEFER_HIGH_1')) {
      statusBuilder.setCharAt(2, REEFER_HIGH_1)
    } else if (inStatus.equalsIgnoreCase('REEFER_HIGH_2')) {
      statusBuilder.setCharAt(2, REEFER_HIGH_2)
    } else if (inStatus.equalsIgnoreCase('REEFER_HIGH_3')) {
      statusBuilder.setCharAt(2, REEFER_HIGH_3)
    } else if (inStatus.equalsIgnoreCase('REEFER_HIGH_4')) {
      statusBuilder.setCharAt(2, REEFER_HIGH_4)
    } else if (inStatus.equalsIgnoreCase('REEFER_HIGH_5')) {
      statusBuilder.setCharAt(2, REEFER_HIGH_5)
    } else if (inStatus.equalsIgnoreCase('PORTHOLE_TOWER')) {
      statusBuilder.setCharAt(2, PORTHOLE_TOWER)
    } else if (inStatus.equalsIgnoreCase('PORTHOLE_THIRDTIER')) {
      statusBuilder.setCharAt(2, PORTHOLE_THIRDTIER)
    } else if (inStatus.equalsIgnoreCase('PORTHOLE_SECONDTIER')) {
      statusBuilder.setCharAt(2, PORTHOLE_SECONDTIER)
    } else if (inStatus.equalsIgnoreCase('PORTHOLE_FIRSTTIER')) {
      statusBuilder.setCharAt(2, PORTHOLE_FIRSTTIER)
    }
    return statusBuilder.toString();
  }

  /**
   * Invoked by ReportStackStatus
   * Gets the status for each stack from DB and returns the stack status value
   * @param inStatus
   * @return
   */
  public String getStackStatusReport(String inStatus) {
    def result = ''
    if (inStatus.size() >= 6 || inStatus.size() >= 7) {
      if (inStatus.charAt(3).charValue().equals(MENWORKING_PRESERVE)) {
        result += 'Men Working' + ","
      };
      if (inStatus.charAt(0).charValue().equals(TEMPBLOCKED_PRESERVE)) {
        result += 'Temp Blocked' + ","
      }
      if (inStatus.charAt(0).charValue().equals(ROADWAY_PRESERVE)) {
        result += 'Road Way' + ","
      }
      if (inStatus.charAt(0).charValue().equals(BUILDING)) {
        result += 'Building' + ","
      }
      if (inStatus.charAt(0).charValue().equals(HYDRANT)) {
        result += 'Hydrant' + ","
      }
      if (inStatus.charAt(0).charValue().equals(AISLE)) {
        result += 'Aisle' + ","
      }
      if (inStatus.charAt(0).charValue().equals(POLE)) {
        result += 'Pole' + ","
      }
      if (inStatus.charAt(0).charValue().equals(TP_EXTENDED)) {
        result += 'TP Extended' + ","
      }
      if (inStatus.charAt(5).charValue().equals(RACK_PRESERVE)) {
        result += 'Rack' + ","
      }
      if (inStatus.charAt(6).charValue().equals(RACK_PRESERVE)) {
        result += 'Rack' + ","
      }

      //Tier limits
      if (inStatus.charAt(1).charValue().equals(TIER_HIGH_1)) {
        result += 'TierHigh1' + ","
      } else if (inStatus.charAt(1).charValue().equals(TIER_HIGH_2)) {
        result += 'TierHigh2' + ","
      } else if (inStatus.charAt(1).charValue().equals(TIER_HIGH_3)) {
        result += 'TierHigh3' + ","
      } else if (inStatus.charAt(1).charValue().equals(TIER_HIGH_4)) {
        result += 'TierHigh4' + ","
      } else if (inStatus.charAt(1).charValue().equals(TIER_HIGH_5)) {
        result += 'TierHigh5' + ","
      } else if (inStatus.charAt(1).charValue().equals(TIER_HIGH_6)) {
        result += 'TierHigh6' + ","
      } else if (inStatus.charAt(1).charValue().equals(TIER_HIGH_7)) {
        result += 'TierHigh7' + ","
      } else if (inStatus.charAt(1).charValue().equals(TIER_HIGH_8)) {
        result += 'TierHigh8' + ","
      } else if (inStatus.charAt(1).charValue().equals(TIER_HIGH_9)) {
        result += 'TierHigh9' + ","
      }

      //chassis
      if (inStatus.charAt(4).charValue().equals(ONLY_20)) {
        result += 'Only20_Chassis' + ","
      } else if (inStatus.charAt(4).charValue().equals(ONLY_45)) {
        result += 'Only45_Chassis' + ","
      }

      //Reefer Status
      if (inStatus.charAt(2).charValue().equals(REEFER_HIGH_1)) {
        result += 'Reefer - 1 High Integral' + ","
      } else if (inStatus.charAt(2).charValue().equals(REEFER_HIGH_2)) {
        result += 'Reefer - 2 High Integral' + ","
      } else if (inStatus.charAt(2).charValue().equals(REEFER_HIGH_3)) {
        result += 'Reefer - 3 High Integral' + ","
      } else if (inStatus.charAt(2).charValue().equals(REEFER_HIGH_4)) {
        result += 'Reefer - 4 High Integral' + ","
      } else if (inStatus.charAt(2).charValue().equals(REEFER_HIGH_5)) {
        result += 'Reefer - 5 Integral' + ","
      } else if (inStatus.charAt(2).charValue().equals(PORTHOLE_TOWER)) {
        result += 'Porthole Tower' + ","
      } else if (inStatus.charAt(2).charValue().equals(PORTHOLE_THIRDTIER)) {
        result += 'Porthole Tower - 3rd Tier Ok' + ","
      } else if (inStatus.charAt(2).charValue().equals(PORTHOLE_SECONDTIER)) {
        result += 'Porthole Tower - 2nd Tier Ok' + ","
      } else if (inStatus.charAt(2).charValue().equals(PORTHOLE_FIRSTTIER)) {
        result += 'Porthole Tower - 1st Tier Ok' + ","
      }
    }
    return result;
  }

  /**
   * Invoked by ReportTransferPointUsage
   * @param list
   * @return
   */
  public String getMoveKindAllowedReport(String moveKindAllowed) {
    char allowed = 'T';
    def result = '';
    if (moveKindAllowed.charAt(0).charValue().equals(allowed)) {
      result += 'Receival' + ","
    }
    if (moveKindAllowed.charAt(1).charValue().equals(allowed)) {
      result += 'Delivery' + ","
    }
    if (moveKindAllowed.charAt(2).charValue().equals(allowed)) {
      result += 'YardShift' + ","
    }
    if (moveKindAllowed.charAt(3).charValue().equals(allowed)) {
      result += 'YardMove' + ","
    }
    if (moveKindAllowed.charAt(4).charValue().equals(allowed)) {
      result += 'VesselDischarge' + ","
    }
    if (moveKindAllowed.charAt(5).charValue().equals(allowed)) {
      result += 'VesselLoad' + ","
    }
    if (moveKindAllowed.charAt(6).charValue().equals(allowed)) {
      result += 'ShiftOnBoard' + ","
    }
    if (moveKindAllowed.charAt(7).charValue().equals(allowed)) {
      result += 'RailDischarge' + ","
    }
    if (moveKindAllowed.charAt(8).charValue().equals(allowed)) {
      result += 'RailLoad'
    }
    return result;
  }

  /**
   * Returns the actual report by truncating extra brackets and spaces.
   * @param listOutput
   * @param wiKey
   * @return
   */
  private String getActualReport(String listOutput, wiKey) {
    StringBuilder stringBuilder = new StringBuilder()
    stringBuilder.append(listOutput)
    stringBuilder.delete(listOutput.indexOf(wiKey), listOutput.indexOf(':') + 1)
    def out = stringBuilder.toString()
    if (out.startsWith('[')) {
      out = out.substring(1, out.length())
    };
    if (out.endsWith(']')) {
      out = out.substring(0, out.length() - 1)
    };
    if (out.endsWith(']')) {
      out = out.substring(0, out.length() - 1)
    };
    return out;
  }

  private String getActualEnumValue(String enumObtained) {
    if (enumObtained != null) {
      if (enumObtained.contains('[')) {
        enumObtained = enumObtained.substring(enumObtained.indexOf('[') + 1, enumObtained.indexOf(']'));
      }
    }
    return enumObtained;
  }

  private def truncateEndingComma(def str) {
    if (str.trim().endsWith(',')) {
      str = str.substring(0, str.length() - 1);
    }
    return str;
  }

  private def truncateOnlyEndingComma(def str) {
    if (str.endsWith(',')) {
      str = str.substring(0, str.length() - 1);
    }
    return str;
  }

/**
 * Calculates time with the given value
 * Current indicate the current system date and time
 * Current + 'n'H indicates + 'n' hours to the current system datetime
 * Current + 'n'D indicates + 'n' days to the current system datetime
 * Current - 'n'H indicates - 'n' hours to the current system datetime
 * Current - 'n'D indicates - 'n' days to the current system datetime
 * @param inDate
 * @return
 */
  private Date calculateTime(String inDate) {
    //, ATA=Current,ATD=Current+3D,StartWork=Current+1H,EndWork=Current+1D,Phase=Working
    def values;
    if (inDate.equalsIgnoreCase('Current')) {
      return new Date();
    } else if (inDate.contains('+')) {
      if (inDate.contains('H')) {  //to increment hours
        values = inDate.substring(inDate.indexOf('+') + 1, inDate.indexOf('H')).trim();
        return DateUtils.addHours(new Date(), values.toInteger());
      } else if (inDate.contains('D')) { //to increment days
        values = inDate.substring(inDate.indexOf('+') + 1, inDate.indexOf('D')).trim();
        return new Date() + values.toInteger();
      } else if (inDate.contains('M')) { //to increment minutes
        values = inDate.substring(inDate.indexOf('+') + 1, inDate.indexOf('M')).trim();
        return DateUtils.addMinutes(new Date(), values.toInteger());
      } else if (inDate.contains('h')) {  //to increment hours
        values = inDate.substring(inDate.indexOf('+') + 1, inDate.indexOf('h')).trim();
        return DateUtils.addHours(new Date(), values.toInteger());
      } else if (inDate.contains('d')) { //to increment days
        values = inDate.substring(inDate.indexOf('+') + 1, inDate.indexOf('d')).trim();
        return new Date() + values.toInteger();
      } else if (inDate.contains('m')) { //to increment minutes
        values = inDate.substring(inDate.indexOf('+') + 1, inDate.indexOf('m')).trim();
        return DateUtils.addMinutes(new Date(), values.toInteger());
      }
    } else if (inDate.contains('-')) {
      if (inDate.contains('H')) {      //to decrement hours
        values = inDate.substring(inDate.indexOf('-') + 1, inDate.indexOf('H')).trim();
        return DateUtils.addHours(new Date(), -values.toInteger());
      } else if (inDate.contains('D') || inDate.contains('d')) {    //to decrement days
        values = inDate.substring(inDate.indexOf('-') + 1, inDate.indexOf('D')).trim();
        return new Date() - values.toInteger();
      } else if (inDate.contains('h')) {   //to decrement hours
        values = inDate.substring(inDate.indexOf('-') + 1, inDate.indexOf('h')).trim();
        return DateUtils.addHours(new Date(), -values.toInteger());
      } else if (inDate.contains('d')) {    //to decrement days
        values = inDate.substring(inDate.indexOf('-') + 1, inDate.indexOf('d')).trim();
        return new Date() - values.toInteger();
      } else if (inDate.contains('M')) { //to decrement minutes
        values = inDate.substring(inDate.indexOf('-') + 1, inDate.indexOf('M')).trim();
        return DateUtils.addMinutes(new Date(), -values.toInteger());
      } else if (inDate.contains('m')) { //to decrement minutes
        values = inDate.substring(inDate.indexOf('-') + 1, inDate.indexOf('m')).trim();
        return DateUtils.addMinutes(new Date(), -values.toInteger());
      }
    }
  }

  //Utility method to convert the first letter of given method name to uppercase
  //to return a valid method name to be used.
  private String getActualMethodOrClassName(String methodName) {
    def c = methodName.toCharArray();
    c[0] = Character.toUpperCase(c[0]);
    methodName = new String(c);
    return methodName;
  }

  //Utility Method to get the actual json output expected by DB
  private String getActualOutput(String returnValue) {
    //replace all '{' with empty space
    returnValue = returnValue.replace('{', ' ');
    returnValue = returnValue.replace('}', ' ');
    //replace '[]' with '{}'
    returnValue = returnValue.replace('[', '{');
    returnValue = returnValue.replace(']', '}');
    //replace all empty space with nothing
    //append string in between { }
    returnValue = "{" + returnValue + "}";
    return returnValue;
  }

  //Gets ECI configuration
  private static IEciConfiguration getEciConfiguration() {
    return (IEciConfiguration) Roastery.getBean(IEciConfiguration.BEAN_ID);
  }

  private DatabaseHelper getTeamsDbHelper(@NotNull final String inEciAdapterId) {
    final TeamsEciAdapter eciAdapter = EciConfigurationHelper.getEciConfiguration().getEciAdapter(TeamsEciAdapter.class, inEciAdapterId);
    return eciAdapter.getJdbcEciConnection().getDatabaseHelper();
  }

  public String checkParameter(String inKeyName, Map inParameters) {
    String value = null;
    value = inParameters.get(inKeyName);
    assert value != null, inKeyName + " parameter not supplied";
    assert value != "", inKeyName + " parameter is empty";
    return value;
  }
}