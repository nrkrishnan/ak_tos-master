package com.matson.tos.json.jsonToSnx;

import com.matson.tos.dao.TosLookup;
import com.matson.tos.exception.TosException;
import com.matson.tos.jaxb.snx.*;
import com.matson.tos.json.HAZMAT;
import com.matson.tos.json.HazardousCommodityLine;
import com.matson.tos.util.UnitConversion;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Keerthi Ramachandran
 * @since 6/25/2015
 * <p>HazardJsonToSNX is ..</p>
 */

public class HazardJsonToSNX {
    private static Logger logger = Logger.getLogger(HazardJsonToSNX.class);
    private String _bookingNo;
    private String _containerNo;
    
    // connection pool issue start
    TosLookup tosLookUp = null; 
    // connection pool issue end

    public HazardJsonToSNX(String inConatinerNo, String inBillNo) {
        _containerNo = inConatinerNo;
        _bookingNo = inBillNo;

    }

    public Snx getSNXObject(HAZMAT inHAZMAT) throws TosException {
        logger.debug("HAZMAT Object is :" + inHAZMAT.toString());
        /**
         * Part 1: associate with booking
         */
        Map<String, String> bookingRequiredValuesLookUp = new HashMap<String, String>();
        
        // connection pool issue start
        
       /* try {
            TosLookup tosLookUp = new TosLookup();
            bookingRequiredValuesLookUp = tosLookUp.getBookingDetailsForHazardous(_bookingNo);
        } catch (Exception e) {
            logger.debug(e);
            throw new TosException("Booking "+_bookingNo+" not found, could not update BDX Hazard data"+e);
        }*/
        
        	try {
            if (tosLookUp == null){
                 tosLookUp = new TosLookup();
        		}
           bookingRequiredValuesLookUp = tosLookUp.getBookingDetailsForHazardous(_bookingNo);
             } catch (Exception e) {
                 logger.debug(e);
                 throw new TosException("Booking "+_bookingNo+" not found, could not update BDX Hazard data"+e);
             } finally {
                   if (tosLookUp!= null)
                   {
                          tosLookUp.close();
                          tosLookUp = null;
                   }
             }

        // connection pool issue end
        String CARRIER_VISIT_ID, LINE_OP, EQ_STATUS, PREVENT_TYPE_SUBST, FACILITY;
        if (bookingRequiredValuesLookUp != null && !bookingRequiredValuesLookUp.isEmpty()) {

            CARRIER_VISIT_ID = bookingRequiredValuesLookUp.get("CARRIER_VISIT_ID");
            LINE_OP = bookingRequiredValuesLookUp.get("LINE_OP");
            EQ_STATUS = bookingRequiredValuesLookUp.get("EQ_STATUS");
            PREVENT_TYPE_SUBST = bookingRequiredValuesLookUp.get("PREVENT_TYPE_SUBST");
            FACILITY = bookingRequiredValuesLookUp.get("FACILITY");
        } else {
            logger.debug("CARRIER_VISIT_ID, LINE_OP, EQ_STATUS, PREVENT_TYPE_SUBST values are not available, as the DB has no value for booking " + _bookingNo);
            throw new TosException("Booking "+_bookingNo+" not found, could not update BDX Hazard data");
        }

        TBooking tBooking = new TBooking();
        tBooking.setNbr(_bookingNo);
        tBooking.setLine(LINE_OP);
        TCarrier tCarrier = new TCarrier();
        tCarrier.setId(CARRIER_VISIT_ID);
        tCarrier.setFacility(FACILITY);
        tBooking.setCarrier(tCarrier);
        tBooking.setEqStatus(EQ_STATUS);
        String preventTypeSubst = "N";
        if (PREVENT_TYPE_SUBST != null && !PREVENT_TYPE_SUBST.isEmpty() && !"0".equalsIgnoreCase(PREVENT_TYPE_SUBST)) {
            preventTypeSubst = "Y";
        }
        tBooking.setPreventTypeSubst(preventTypeSubst);

        THazards bookingHazards = new THazards();
        for (HazardousCommodityLine commodityLine : inHAZMAT.getHazardousCommodityLines()) {
            bookingHazards.getHazard().add(getHazard(commodityLine));
        }
        tBooking.setHazards(bookingHazards);

        Snx bookingSnx = new Snx();
        bookingSnx.getBooking().add(tBooking);


        /*Snx snx = bookingSnx;
        TUnit tUnit = new TUnit();
        tUnit.setId(_containerNo);
        snx.getUnit().add(tUnit);
        TUnit unit = snx.getUnit().get(0);
        unit.setId(_containerNo);
        THazards unitHazards = new THazards();
        for (HazardousCommodityLine commodityLine : inHAZMAT.getHazardousCommodityLines()) {
            unitHazards.getHazard().add(getHazard(commodityLine));
        }
        unit.setHazards(unitHazards);*/
        return bookingSnx;
    }

    /*public Snx getUnitSNXObject(HAZMAT inHAZMAT) throws TosException {
        String transitState = "";
        return getUnitSNXObject(inHAZMAT, transitState);
    }*/

    public Snx getUnitSNXObject(HAZMAT inHAZMAT, String facility, Snx output) throws TosException {
        logger.debug("HAZMAT Object is :" + inHAZMAT.toString());
        boolean sameUnit = false;
        TUnit unit = new TUnit();
        if (output.getUnit() != null && output.getUnit().size()>0) {
            unit = output.getUnit().get(0);
            sameUnit = true;
        }
        unit.setId(_containerNo);
        THazards unitHazards = unit.getHazards();
        if (unitHazards == null) {
            unitHazards = new THazards();
        }
        for (HazardousCommodityLine commodityLine : inHAZMAT.getHazardousCommodityLines()) {
            unitHazards.getHazard().add(getHazard(commodityLine));
        }
        unit.setHazards(unitHazards);
        if (!sameUnit) {
            TRouting routing = new TRouting();
            TRouting.Carrier carrier = new TRouting.Carrier();
            try {
                if (tosLookUp == null) {
                    tosLookUp = new TosLookup();
                }
                HashMap<String, String> unitCarrierDetails = tosLookUp.getUnitCarrierDetails(_containerNo);
                if (unitCarrierDetails != null) {
                    if (unitCarrierDetails.get("TRANSIT_STATE") != null && unitCarrierDetails.get("TRANSIT_STATE").length() > 4) {
                        unit.setTransitState(unitCarrierDetails.get("TRANSIT_STATE").substring(4));
                    }
                    carrier.setId(unitCarrierDetails.get("IB_ACTUAL"));
                    carrier.setMode(unitCarrierDetails.get("IB_ACTUAL_MODE"));
                    carrier.setQualifier("ACTUAL");
                    carrier.setDirection(TDirection.IB);
                    carrier.setFacility(facility);
                    routing.getCarrier().add(carrier);
                    carrier = new TRouting.Carrier();
                    carrier.setId(unitCarrierDetails.get("IB_DECLRD"));
                    carrier.setMode(unitCarrierDetails.get("IB_DECLRD_MODE"));
                    carrier.setQualifier("DECLARED");
                    carrier.setDirection(TDirection.IB);
                    carrier.setFacility(facility);
                    routing.getCarrier().add(carrier);
                    carrier = new TRouting.Carrier();
                    carrier.setId(unitCarrierDetails.get("OB_ACTUAL"));
                    carrier.setMode(unitCarrierDetails.get("OB_ACTUAL_MODE"));
                    carrier.setQualifier("ACTUAL");
                    carrier.setDirection(TDirection.OB);
                    carrier.setFacility(facility);
                    routing.getCarrier().add(carrier);
                    carrier = new TRouting.Carrier();
                    carrier.setId(unitCarrierDetails.get("OB_DECLRD"));
                    carrier.setMode(unitCarrierDetails.get("OB_DECLRD_MODE"));
                    carrier.setQualifier("DECLARED");
                    carrier.setDirection(TDirection.OB);
                    carrier.setFacility(facility);
                    routing.getCarrier().add(carrier);
                    unit.setRouting(routing);
                }
            } catch (Exception e) {
                logger.error("Erro while populating SNX using HAZMAT : " + e.getMessage());
                e.printStackTrace();
            } finally {
                if (tosLookUp != null) {
                    tosLookUp.close();
                }
            }
        }
        Snx unitSnx = new Snx();
        unitSnx.getUnit().add(unit);

        return unitSnx;
    }

    private THazard getHazard(HazardousCommodityLine commodityLine) {
        THazard hazard = new THazard();
        checkAndUpdateIMDGClassForHazard(commodityLine,hazard);
        logger.info("new hazard Imdg-->" + hazard.getImdg());
        hazard.setHazNbrType(THazardNumberType.valueOf(commodityLine.getHazType()));
        hazard.setUn(commodityLine.getHazNumber());
        String ltdQtyFlag = commodityLine.getHazLimitedQuantity() != null && !commodityLine.getHazLimitedQuantity().isEmpty() && "1".equals(commodityLine.getHazLimitedQuantity()) ? "Y" : "N";
        hazard.setLtdQtyFlag(ltdQtyFlag);
        //hazard.setPackageType(commodityLine.getHazPackageGroup());
        Double flashPointD = null;

        if (commodityLine.getHazFlashPoint() != null && !commodityLine.getHazFlashPoint().isEmpty()) {
            flashPointD = Double.valueOf(commodityLine.getHazFlashPoint());
            if (commodityLine.getHazFlashPointUomCode() != null && !commodityLine.getHazFlashPointUomCode().isEmpty()) {
                if ("F".equalsIgnoreCase(commodityLine.getHazFlashPointUomCode())) {
                    flashPointD = Double.valueOf((flashPointD - 32.0) * 5.0 / 9.0);// with conversion from F to C
                }
            }
        }
        if (flashPointD != null)
            hazard.setFlashPoint(BigDecimal.valueOf(flashPointD));
        else {
            logger.warn("No Flashpoint, the value from the JSON is null or not available");
        }        //hazard.setFlashPoint(commodityLine.getHazFlashPoint() != null && !commodityLine.getHazFlashPoint().isEmpty() ? BigDecimal.valueOf(Double.valueOf(commodityLine.getHazFlashPoint())) : null);
        hazard.setTechName(commodityLine.getHazCommodityName());
        hazard.setProperName(commodityLine.getHazCommodityName());
        String packingGroup = "";
        if (commodityLine.getHazPackageGroup()!=null &&1 == commodityLine.getHazPackageGroup()) {
            packingGroup = "I";
        } else if (commodityLine.getHazPackageGroup()!=null && 2 == commodityLine.getHazPackageGroup()) {
            packingGroup = "II";
        } else if (commodityLine.getHazPackageGroup()!=null && 3 == commodityLine.getHazPackageGroup()) {
            packingGroup = "III";
        }
        if (!packingGroup.isEmpty())
        hazard.setPackingGroup(packingGroup);
        String weightStr = commodityLine.getHazWeight() != null ?
                BigDecimal.valueOf(commodityLine.getHazWeight()).toString() : BigDecimal.valueOf(0l).toString();
        logger.info("Hazard weight for "+hazard.getUn() + ", is from HazMat : "+weightStr);
        String weightKg = "";
        if (commodityLine.getHazWeightUomCode() != null && (commodityLine.getHazWeightUomCode().contains("kg") || commodityLine.getHazWeightUomCode().contains("KG"))) {
            weightKg = weightStr;
        } else {
            weightKg = UnitConversion.weightFromLBToKg(weightStr);
        }
        logger.info("Hazard weight for "+hazard.getUn() + ", is to SNX KG : "+weightKg);

        hazard.setWeightKg(new BigDecimal(weightKg)) ;
        //set hazard.getPackageType()
        if (commodityLine.getHazPiecesUomCode() != null && !commodityLine.getHazPiecesUomCode().isEmpty())
            hazard.setPackageType(commodityLine.getHazPiecesUomCode());
        if (hazard.getPackageType() != null && commodityLine.getHazPieces() != 0l)
            hazard.setQuantity(BigInteger.valueOf(commodityLine.getHazPieces()));
        if (commodityLine.getHazSecondaryClass() != null && !commodityLine.getHazSecondaryClass().isEmpty())
            hazard.setSecondaryImo1(commodityLine.getHazSecondaryClass());
        if (commodityLine.getHazTertiaryClass() != null && !commodityLine.getHazTertiaryClass().isEmpty())
            hazard.setSecondaryImo2(commodityLine.getHazTertiaryClass());
        String marinePollutants = commodityLine.getHazMarinePollutant()!=null && !commodityLine.getHazMarinePollutant().isEmpty() &&
                "1".equals(commodityLine.getHazMarinePollutant()) ? "Y" : "N";
        hazard.setMarinePollutants(marinePollutants);
        hazard.setEmergencyTelephone(commodityLine.getHazEmergencyContactPhone());
        return hazard;
    }


    private void checkAndUpdateIMDGClassForHazard(HazardousCommodityLine hazardousCommodityLine, THazard hazard) {

        String hasPrimaryClass = hazardousCommodityLine.getHazPrimaryClass() != null ?
                hazardousCommodityLine.getHazPrimaryClass().trim() : "";
        if (hasPrimaryClass.isEmpty() || "x".equalsIgnoreCase(hasPrimaryClass)) {
            hazard.setImdg("X");
        } else {
            hazard.setImdg(hasPrimaryClass);
        }
    }


    public String get_bookingNo() {
        return _bookingNo;
    }

    public void set_bookingNo(String _bookingNo) {
        this._bookingNo = _bookingNo;
    }

    public String get_containerNo() {
        return _containerNo;
    }

    public void set_containerNo(String _containerNo) {
        this._containerNo = _containerNo;
    }
}
