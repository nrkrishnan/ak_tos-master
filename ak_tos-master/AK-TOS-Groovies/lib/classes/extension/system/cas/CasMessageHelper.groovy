/*
 * Copyright (c) 2013 Navis LLC. All Rights Reserved.
 *
 */

package system.cas

import com.navis.external.framework.AbstractExtensionCallback

/**
 * This class is a helper class containing  the human readable CAS messages
 * and their codes in this reference implementation. The handler groovy classes use these constants to
 * construct messages which are returned/logged.
 *
 * @author <a href="mailto:arvinder.brar@navis.com">Arvinder Brar</a>, 5/22/13
 */
class CasMessageHelper extends AbstractExtensionCallback{

  public final String SUCCESS_CODE = "NCAS-5000"

  public final String BIZ_ERROR_CODE = "NCAS-0000"
  public final String BIZ_ERROR_MESSAGE = "Business error occured. Please see the messages section"

  public final String MISSING_REQUEST_TYPE_CODE =  "NCAS-0001"
  public final String MISSING_REQUEST_TYPE_MESSAGE =  "Required parameter [requestType] is missing" //NCAS-0001

  public final String MISSING_VISIT_TYPE_CODE = "NCAS-0002"
  public final String MISSING_VISIT_TYPE_MESSAGE = "Required parameter [visitType] is missing" //NCAS-0002

  public final String MISSING_VISIT_ID_CODE = "NCAS-0003"
  public final String MISSING_VISIT_ID_MESSAGE = "Required parameter [visitId] is missing" //NCAS-0003

  public final String INVALID_VISIT_TYPE_CODE = "NCAS-0004"
  public final String INVALID_VISIT_TYPE_MESSAGE = "Invalid visit type. Valid visit types are [VESSEL] and [TRAIN]" //NCAS-0004

  public final String NO_CARRIER_VISIT_FOUND_CODE = "NCAS-0005"
  public final String NO_CARRIER_VISIT_FOUND_MESSAGE = "No carrier visit found " //NCAS-0005

  public final String CARRIER_GEOMETRY_NOT_FOUND_CODE = "NCAS-0006"
  public final String CARRIER_GEOMETRY_NOT_FOUND_MESSAGE = "Carrier geometry is not available  " //NCAS-0006

  public final String INVALID_REQUEST_TYPE_CODE = "NCAS-0007"
  public final String INVALID_REQUEST_TYPE_MESSAGE = "Invalid request type. Valid request types are [carrierGeometryRequest] and [containerListRequest]"//NCAS-0007

  public final String INVALID_MESSAGE_REQUEST_TYPE_CODE = "NCAS-0008"
  public final String INVALID_MESSAGE_REQUEST_TYPE_MESSAGE = "Invalid request type. Valid request types are [unitCaptureMessage] and [unitPositionUpdateMessage]" //NCAS-0008

  public final String MISSING_UNIT_XML_CODE = "NCAS-0009"
  public final String MISSING_UNIT_XML_MESSAGE = "No Unit Xml was sent as part of message" //NCAS-0009

  public final String INVALID_UNIT_XML_CODE = "NCAS-0010"
  public final String INVALID_UNIT_XML_MESSAGE = "The Unit Xml sent as part of the message is invalid" //NCAS-0010

  public final String INVALID_CAPTURE_TYPE_CODE = "NCAS-0011"
  public final String INVALID_CAPTURE_TYPE_MESSAGE = "Invalid capture type. Valid types are [Image], [Identify], [Create] and [Update]"; //NCAS-0011

  public final String INVALID_UNIT_POSITION_TYPE_CODE = "NCAS-0012"
  public final String INVALID_UNIT_POSITION_TYPE_MESSAGE = "Invalid position update type. Valid types are [Lift], [Set]"; //NCAS-0012

  public final String MISSING_UNIT_ID_CODE = "NCAS-0013"
  public final String MISSING_UNIT_ID_MESSAGE = "Required unit attribute [id] is missing"; //NCAS-0013

  public final String MISSING_CAS_UNIT_REFERENCE_CODE = "NCAS-0014"
  public final String MISSING_CAS_UNIT_REFERENCE_MESSAGE = "Required unit attribute [cas-unit-reference] is missing" //NCAS-0014

  public final String MISSING_CRANE_ID_CODE = "NCAS-0015"
  public final String MISSING_CRANE_ID_MESSAGE = "Required parameter [craneId] is missing" //NCAS-0015

  public final String MISSING_ISO_CODE_FOR_CREATE_CODE = "NCAS-0016"
  public final String MISSING_ISO_CODE_FOR_CREATE_MESSAGE = "Unit attribute [iso-code] for [action=Create] is missing "; //NCAS-0016

  public final String INVALID_CRANE_ID_CODE = "NCAS-0017"
  public final String INVALID_CRANE_ID_MESSAGE = "Invalid crane Id "; //NCAS-0017

  public final String UNIT_REFERENCE_NOT_SAME_CODE = "NCAS-0018"
  public final String UNIT_REFERENCE_NOT_SAME_MESSAGE = "The unit reference in the message is different than the existing unit reference id of the unit" //NCAS-0018

  public final String TRANSACTION_REFERENCE_NOT_SAME_CODE = "NCAS-0019"
  public final String TRANSACTION_REFERENCE_NOT_SAME_MESSAGE = "The transaction reference in the message is different than the existing transaction reference id of the unit" //NCAS-0019

  public final String MISSING_LOC_TYPE_CODE = "NCAS-0020"
  public final String MISSING_LOC_TYPE_MESSAGE = "The unit position update message is missing the required attribute [loc-type] in current-position node" //NCAS-0020

  public final String MISSING_LOCATION_CODE = "NCAS-0021"
  public final String MISSING_LOCATION_MESSAGE = "The unit position update message is missing the required attribute [location] in current-position node" //NCAS-0021

  public final String MISSING_SLOT_CODE = "NCAS-0022"
  public final String MISSING_SLOT_MESSAGE = "The unit position update message is missing the required attribute [slot] in current-position node" //NCAS-0022

  public final String MISSING_CURRENT_POSITION_NODE_CODE = "NCAS-0023"
  public final String MISSING_CURRENT_POSITION_NODE_MESSAGE = "The unit position update message is missing the required current-position node" //NCAS-0023

  public final String INVALID_25_MESSAGE_REQUEST_TYPE_CODE = "NCAS-0024"
  public final String INVALID_25_MESSAGE_REQUEST_TYPE_MESSAGE = "Invalid request type. Valid request type is [unitCaptureMessage]" //NCAS-0024

  public final String BUNDLED_EQUIPMENT_NOT_FOUND_CODE = "NCAS-0025"
  public final String BUNDLED_EQUIPMENT_NOT_FOUND_MESSAGE = "Equipment specified as bundled equipment not found" //NCAS-025

  public final String NOT_IN_LOAD_OR_DSCH_PLAN_ERR_CODE = "NCAS-0026"
  public final String NOT_IN_LOAD_OR_DSCH_PLAN_ERR_MESSAGE = "Identified unit is not planned for load or discharge" //NCAS-0026

  public final String EMPTY_POSITION_FOR_TBD_UNIT_CODE = "NCAS-0027"
  public final String EMPTY_POSITION_FOR_TBD_UNIT_MESSAGE = "Cannot load as position is empty for TBD unit" //NCAS-0027

  public final String CRANE_OCR_DATA_NOT_BEING_ACCEPTED = "NCAS-0028"
  public final String CRANE_OCR_DATA_NOT_BEING_ACCEPTED_MESSAGE = "Currently OCR data is not being accepted from this crane" //NCAS-0028

  //Warnings
  public final String CONTAINER_LIST_REQUEST_MISSING_PARAM_CODE = "NCAS-1001"
  public final String CONTAINER_LIST_REQUEST_MISSING_PARAM_MESSAGE = "Either [onBoard] parameter should be [Y] or [craneId] parameter needs to be defined" //NCAS-1001

  public final String NOT_IN_LOAD_OR_DSCH_PLAN_CODE = "NCAS-1002"
  public final String NOT_IN_LOAD_OR_DSCH_PLAN_MESSAGE = "Identified unit is not planned for load or discharge" //NCAS-1002

  public final String NOT_IN_LOAD_PLAN_CODE = "NCAS-1003"
  public final String NOT_IN_LOAD_PLAN_MESSAGE = "Identified unit is not planned for load" //NCAS-1003

  public final String NOT_IN_DSCH_PLAN_CODE = "NCAS-1004"
  public final String NOT_IN_DSCH_PLAN_MESSAGE = "Identified unit is not planned for discharge" //NCAS-1004

  public final String MISSING_ISO_CODE_FOR_CREATE_WARNING_CODE = "NCAS-1005"
  public final String MISSING_ISO_CODE_FOR_CREATE_WARNING_MESSAGE = "Unit attribute [iso-code] for [action=Create] is missing "; //NCAS-1005

  public final String CARRIER_MISMATCH_LOAD_CODE = "NCAS-1006"
  public final String CARRIER_MISMATCH_LOAD_MESSAGE = "Carrier mismatch for Load"  //NCAS-1006

  public final String CARRIER_MISMATCH_DSCH_CODE = "NCAS 1007"
  public final String CARRIER_MISMATCH_DSCH_MESSAGE = "Carrier mismatch for Discharge" //NCAS 1007

  public final String NOT_VALID_FOR_TBD_LOAD_CODE = "NCAS-1008"
  public final String NOT_VALID_FOR_TBD_LOAD_MESSAGE = "Identified unit is not planned and is not valid for TBD load" //NCAS-1008

  public final String UNIT_NOT_PROCESSED_CODE = "NCAS-1009"
  public final String UNIT_NOT_PROCESSED_MESSAGE = "Unit not processed" //NCAS-1009

  //Information
  public final String UNIT_SUCCESSFULLY_PROCESSED_CODE = "NCAS-2001"
  public final String UNIT_SUCCESSFULLY_PROCESSED_MESSAGE = "Unit successfully processed" //NCAS-2001

  public final String UNIT_CAPTURE_HANDLED_SUCCESSFULLY_CODE = "NCAS-2002"
  public final String UNIT_CAPTURE_HANDLED_SUCCESSFULLY_MESSAGE = "Unit capture handled successfully" //NCAS-2002

  public final String UNIT_POSITION_UPDATED_SUCCESSFULLY_CODE = "NCAS-2003"
  public final String UNIT_POSITION_UPDATED_SUCCESSFULLY_MESSAGE = "Unit position update handled successfully" //NCAS-2003
}
