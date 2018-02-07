/**
 * ManifestWSRequestDataPerUnit.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.tempuri.ManifestWSRequest_xsd;

public class ManifestWSRequestDataPerUnit  implements java.io.Serializable {
    private org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestDataPerUnitTransactionType transactionType;

    private org.apache.axis.types.NormalizedString containerNo;

    private org.apache.axis.types.NormalizedString nonContainerNo;

    private org.apache.axis.types.NormalizedString VINRefNo;

    private org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestDataPerUnitContainerNonType containerNonType;

    private org.apache.axis.types.NormalizedString contentsCommodity;

    private java.lang.String tariffDesc;

    private org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestDataPerUnitWeightSpecification weightSpecification;

    private java.math.BigDecimal grossWeight;

    private java.math.BigDecimal netWeight;

    private java.lang.Integer containerSizeFeet;

    private java.lang.Integer containerParcelCount;

    private org.apache.axis.types.NormalizedString billOfLadingNo;

    private org.apache.axis.types.NormalizedString marksMake;

    private org.apache.axis.types.NormalizedString shippersRef;

    private org.apache.axis.types.NormalizedString shipper;

    private org.apache.axis.types.NormalizedString consignee;

    private org.apache.axis.types.NormalizedString portOfOriginUnit;

    private org.apache.axis.types.NormalizedString inlandPortOriginUnit;

    private org.tempuri.ManifestWSRequest_xsd.HawaiiHarbors deliveryPortUnit;

    private org.apache.axis.types.NormalizedString deliveryPlaceDescUnit;

    private org.tempuri.ManifestWSRequest_xsd.HawaiiHarbors receiptPortUnit;

    private org.apache.axis.types.NormalizedString receiptPlaceDescUnit;

    private java.math.BigDecimal HDOAFee;

    private org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestDataPerUnitHDOAHold HDOAHold;

    private org.apache.axis.types.NormalizedString thirdPartyCheck;

    private org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestDataPerUnitHAZ HAZ;

    private java.lang.Integer temperature;

    private org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestDataPerUnitTempSpecification tempSpecification;

    private org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestDataPerUnitDischargeService dischargeService;

    private org.apache.axis.types.NormalizedString hatchCellNo;

    private org.apache.axis.types.NormalizedString shipperSealNo;

    private org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestDataPerUnitTOS TOS;

    public ManifestWSRequestDataPerUnit() {
    }

    public ManifestWSRequestDataPerUnit(
           org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestDataPerUnitTransactionType transactionType,
           org.apache.axis.types.NormalizedString containerNo,
           org.apache.axis.types.NormalizedString nonContainerNo,
           org.apache.axis.types.NormalizedString VINRefNo,
           org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestDataPerUnitContainerNonType containerNonType,
           org.apache.axis.types.NormalizedString contentsCommodity,
           java.lang.String tariffDesc,
           org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestDataPerUnitWeightSpecification weightSpecification,
           java.math.BigDecimal grossWeight,
           java.math.BigDecimal netWeight,
           java.lang.Integer containerSizeFeet,
           java.lang.Integer containerParcelCount,
           org.apache.axis.types.NormalizedString billOfLadingNo,
           org.apache.axis.types.NormalizedString marksMake,
           org.apache.axis.types.NormalizedString shippersRef,
           org.apache.axis.types.NormalizedString shipper,
           org.apache.axis.types.NormalizedString consignee,
           org.apache.axis.types.NormalizedString portOfOriginUnit,
           org.apache.axis.types.NormalizedString inlandPortOriginUnit,
           org.tempuri.ManifestWSRequest_xsd.HawaiiHarbors deliveryPortUnit,
           org.apache.axis.types.NormalizedString deliveryPlaceDescUnit,
           org.tempuri.ManifestWSRequest_xsd.HawaiiHarbors receiptPortUnit,
           org.apache.axis.types.NormalizedString receiptPlaceDescUnit,
           java.math.BigDecimal HDOAFee,
           org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestDataPerUnitHDOAHold HDOAHold,
           org.apache.axis.types.NormalizedString thirdPartyCheck,
           org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestDataPerUnitHAZ HAZ,
           java.lang.Integer temperature,
           org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestDataPerUnitTempSpecification tempSpecification,
           org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestDataPerUnitDischargeService dischargeService,
           org.apache.axis.types.NormalizedString hatchCellNo,
           org.apache.axis.types.NormalizedString shipperSealNo,
           org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestDataPerUnitTOS TOS) {
           this.transactionType = transactionType;
           this.containerNo = containerNo;
           this.nonContainerNo = nonContainerNo;
           this.VINRefNo = VINRefNo;
           this.containerNonType = containerNonType;
           this.contentsCommodity = contentsCommodity;
           this.tariffDesc = tariffDesc;
           this.weightSpecification = weightSpecification;
           this.grossWeight = grossWeight;
           this.netWeight = netWeight;
           this.containerSizeFeet = containerSizeFeet;
           this.containerParcelCount = containerParcelCount;
           this.billOfLadingNo = billOfLadingNo;
           this.marksMake = marksMake;
           this.shippersRef = shippersRef;
           this.shipper = shipper;
           this.consignee = consignee;
           this.portOfOriginUnit = portOfOriginUnit;
           this.inlandPortOriginUnit = inlandPortOriginUnit;
           this.deliveryPortUnit = deliveryPortUnit;
           this.deliveryPlaceDescUnit = deliveryPlaceDescUnit;
           this.receiptPortUnit = receiptPortUnit;
           this.receiptPlaceDescUnit = receiptPlaceDescUnit;
           this.HDOAFee = HDOAFee;
           this.HDOAHold = HDOAHold;
           this.thirdPartyCheck = thirdPartyCheck;
           this.HAZ = HAZ;
           this.temperature = temperature;
           this.tempSpecification = tempSpecification;
           this.dischargeService = dischargeService;
           this.hatchCellNo = hatchCellNo;
           this.shipperSealNo = shipperSealNo;
           this.TOS = TOS;
    }


    /**
     * Gets the transactionType value for this ManifestWSRequestDataPerUnit.
     * 
     * @return transactionType
     */
    public org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestDataPerUnitTransactionType getTransactionType() {
        return transactionType;
    }


    /**
     * Sets the transactionType value for this ManifestWSRequestDataPerUnit.
     * 
     * @param transactionType
     */
    public void setTransactionType(org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestDataPerUnitTransactionType transactionType) {
        this.transactionType = transactionType;
    }


    /**
     * Gets the containerNo value for this ManifestWSRequestDataPerUnit.
     * 
     * @return containerNo
     */
    public org.apache.axis.types.NormalizedString getContainerNo() {
        return containerNo;
    }


    /**
     * Sets the containerNo value for this ManifestWSRequestDataPerUnit.
     * 
     * @param containerNo
     */
    public void setContainerNo(org.apache.axis.types.NormalizedString containerNo) {
        this.containerNo = containerNo;
    }


    /**
     * Gets the nonContainerNo value for this ManifestWSRequestDataPerUnit.
     * 
     * @return nonContainerNo
     */
    public org.apache.axis.types.NormalizedString getNonContainerNo() {
        return nonContainerNo;
    }


    /**
     * Sets the nonContainerNo value for this ManifestWSRequestDataPerUnit.
     * 
     * @param nonContainerNo
     */
    public void setNonContainerNo(org.apache.axis.types.NormalizedString nonContainerNo) {
        this.nonContainerNo = nonContainerNo;
    }


    /**
     * Gets the VINRefNo value for this ManifestWSRequestDataPerUnit.
     * 
     * @return VINRefNo
     */
    public org.apache.axis.types.NormalizedString getVINRefNo() {
        return VINRefNo;
    }


    /**
     * Sets the VINRefNo value for this ManifestWSRequestDataPerUnit.
     * 
     * @param VINRefNo
     */
    public void setVINRefNo(org.apache.axis.types.NormalizedString VINRefNo) {
        this.VINRefNo = VINRefNo;
    }


    /**
     * Gets the containerNonType value for this ManifestWSRequestDataPerUnit.
     * 
     * @return containerNonType
     */
    public org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestDataPerUnitContainerNonType getContainerNonType() {
        return containerNonType;
    }


    /**
     * Sets the containerNonType value for this ManifestWSRequestDataPerUnit.
     * 
     * @param containerNonType
     */
    public void setContainerNonType(org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestDataPerUnitContainerNonType containerNonType) {
        this.containerNonType = containerNonType;
    }


    /**
     * Gets the contentsCommodity value for this ManifestWSRequestDataPerUnit.
     * 
     * @return contentsCommodity
     */
    public org.apache.axis.types.NormalizedString getContentsCommodity() {
        return contentsCommodity;
    }


    /**
     * Sets the contentsCommodity value for this ManifestWSRequestDataPerUnit.
     * 
     * @param contentsCommodity
     */
    public void setContentsCommodity(org.apache.axis.types.NormalizedString contentsCommodity) {
        this.contentsCommodity = contentsCommodity;
    }


    /**
     * Gets the tariffDesc value for this ManifestWSRequestDataPerUnit.
     * 
     * @return tariffDesc
     */
    public java.lang.String getTariffDesc() {
        return tariffDesc;
    }


    /**
     * Sets the tariffDesc value for this ManifestWSRequestDataPerUnit.
     * 
     * @param tariffDesc
     */
    public void setTariffDesc(java.lang.String tariffDesc) {
        this.tariffDesc = tariffDesc;
    }


    /**
     * Gets the weightSpecification value for this ManifestWSRequestDataPerUnit.
     * 
     * @return weightSpecification
     */
    public org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestDataPerUnitWeightSpecification getWeightSpecification() {
        return weightSpecification;
    }


    /**
     * Sets the weightSpecification value for this ManifestWSRequestDataPerUnit.
     * 
     * @param weightSpecification
     */
    public void setWeightSpecification(org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestDataPerUnitWeightSpecification weightSpecification) {
        this.weightSpecification = weightSpecification;
    }


    /**
     * Gets the grossWeight value for this ManifestWSRequestDataPerUnit.
     * 
     * @return grossWeight
     */
    public java.math.BigDecimal getGrossWeight() {
        return grossWeight;
    }


    /**
     * Sets the grossWeight value for this ManifestWSRequestDataPerUnit.
     * 
     * @param grossWeight
     */
    public void setGrossWeight(java.math.BigDecimal grossWeight) {
        this.grossWeight = grossWeight;
    }


    /**
     * Gets the netWeight value for this ManifestWSRequestDataPerUnit.
     * 
     * @return netWeight
     */
    public java.math.BigDecimal getNetWeight() {
        return netWeight;
    }


    /**
     * Sets the netWeight value for this ManifestWSRequestDataPerUnit.
     * 
     * @param netWeight
     */
    public void setNetWeight(java.math.BigDecimal netWeight) {
        this.netWeight = netWeight;
    }


    /**
     * Gets the containerSizeFeet value for this ManifestWSRequestDataPerUnit.
     * 
     * @return containerSizeFeet
     */
    public java.lang.Integer getContainerSizeFeet() {
        return containerSizeFeet;
    }


    /**
     * Sets the containerSizeFeet value for this ManifestWSRequestDataPerUnit.
     * 
     * @param containerSizeFeet
     */
    public void setContainerSizeFeet(java.lang.Integer containerSizeFeet) {
        this.containerSizeFeet = containerSizeFeet;
    }


    /**
     * Gets the containerParcelCount value for this ManifestWSRequestDataPerUnit.
     * 
     * @return containerParcelCount
     */
    public java.lang.Integer getContainerParcelCount() {
        return containerParcelCount;
    }


    /**
     * Sets the containerParcelCount value for this ManifestWSRequestDataPerUnit.
     * 
     * @param containerParcelCount
     */
    public void setContainerParcelCount(java.lang.Integer containerParcelCount) {
        this.containerParcelCount = containerParcelCount;
    }


    /**
     * Gets the billOfLadingNo value for this ManifestWSRequestDataPerUnit.
     * 
     * @return billOfLadingNo
     */
    public org.apache.axis.types.NormalizedString getBillOfLadingNo() {
        return billOfLadingNo;
    }


    /**
     * Sets the billOfLadingNo value for this ManifestWSRequestDataPerUnit.
     * 
     * @param billOfLadingNo
     */
    public void setBillOfLadingNo(org.apache.axis.types.NormalizedString billOfLadingNo) {
        this.billOfLadingNo = billOfLadingNo;
    }


    /**
     * Gets the marksMake value for this ManifestWSRequestDataPerUnit.
     * 
     * @return marksMake
     */
    public org.apache.axis.types.NormalizedString getMarksMake() {
        return marksMake;
    }


    /**
     * Sets the marksMake value for this ManifestWSRequestDataPerUnit.
     * 
     * @param marksMake
     */
    public void setMarksMake(org.apache.axis.types.NormalizedString marksMake) {
        this.marksMake = marksMake;
    }


    /**
     * Gets the shippersRef value for this ManifestWSRequestDataPerUnit.
     * 
     * @return shippersRef
     */
    public org.apache.axis.types.NormalizedString getShippersRef() {
        return shippersRef;
    }


    /**
     * Sets the shippersRef value for this ManifestWSRequestDataPerUnit.
     * 
     * @param shippersRef
     */
    public void setShippersRef(org.apache.axis.types.NormalizedString shippersRef) {
        this.shippersRef = shippersRef;
    }


    /**
     * Gets the shipper value for this ManifestWSRequestDataPerUnit.
     * 
     * @return shipper
     */
    public org.apache.axis.types.NormalizedString getShipper() {
        return shipper;
    }


    /**
     * Sets the shipper value for this ManifestWSRequestDataPerUnit.
     * 
     * @param shipper
     */
    public void setShipper(org.apache.axis.types.NormalizedString shipper) {
        this.shipper = shipper;
    }


    /**
     * Gets the consignee value for this ManifestWSRequestDataPerUnit.
     * 
     * @return consignee
     */
    public org.apache.axis.types.NormalizedString getConsignee() {
        return consignee;
    }


    /**
     * Sets the consignee value for this ManifestWSRequestDataPerUnit.
     * 
     * @param consignee
     */
    public void setConsignee(org.apache.axis.types.NormalizedString consignee) {
        this.consignee = consignee;
    }


    /**
     * Gets the portOfOriginUnit value for this ManifestWSRequestDataPerUnit.
     * 
     * @return portOfOriginUnit
     */
    public org.apache.axis.types.NormalizedString getPortOfOriginUnit() {
        return portOfOriginUnit;
    }


    /**
     * Sets the portOfOriginUnit value for this ManifestWSRequestDataPerUnit.
     * 
     * @param portOfOriginUnit
     */
    public void setPortOfOriginUnit(org.apache.axis.types.NormalizedString portOfOriginUnit) {
        this.portOfOriginUnit = portOfOriginUnit;
    }


    /**
     * Gets the inlandPortOriginUnit value for this ManifestWSRequestDataPerUnit.
     * 
     * @return inlandPortOriginUnit
     */
    public org.apache.axis.types.NormalizedString getInlandPortOriginUnit() {
        return inlandPortOriginUnit;
    }


    /**
     * Sets the inlandPortOriginUnit value for this ManifestWSRequestDataPerUnit.
     * 
     * @param inlandPortOriginUnit
     */
    public void setInlandPortOriginUnit(org.apache.axis.types.NormalizedString inlandPortOriginUnit) {
        this.inlandPortOriginUnit = inlandPortOriginUnit;
    }


    /**
     * Gets the deliveryPortUnit value for this ManifestWSRequestDataPerUnit.
     * 
     * @return deliveryPortUnit
     */
    public org.tempuri.ManifestWSRequest_xsd.HawaiiHarbors getDeliveryPortUnit() {
        return deliveryPortUnit;
    }


    /**
     * Sets the deliveryPortUnit value for this ManifestWSRequestDataPerUnit.
     * 
     * @param deliveryPortUnit
     */
    public void setDeliveryPortUnit(org.tempuri.ManifestWSRequest_xsd.HawaiiHarbors deliveryPortUnit) {
        this.deliveryPortUnit = deliveryPortUnit;
    }


    /**
     * Gets the deliveryPlaceDescUnit value for this ManifestWSRequestDataPerUnit.
     * 
     * @return deliveryPlaceDescUnit
     */
    public org.apache.axis.types.NormalizedString getDeliveryPlaceDescUnit() {
        return deliveryPlaceDescUnit;
    }


    /**
     * Sets the deliveryPlaceDescUnit value for this ManifestWSRequestDataPerUnit.
     * 
     * @param deliveryPlaceDescUnit
     */
    public void setDeliveryPlaceDescUnit(org.apache.axis.types.NormalizedString deliveryPlaceDescUnit) {
        this.deliveryPlaceDescUnit = deliveryPlaceDescUnit;
    }


    /**
     * Gets the receiptPortUnit value for this ManifestWSRequestDataPerUnit.
     * 
     * @return receiptPortUnit
     */
    public org.tempuri.ManifestWSRequest_xsd.HawaiiHarbors getReceiptPortUnit() {
        return receiptPortUnit;
    }


    /**
     * Sets the receiptPortUnit value for this ManifestWSRequestDataPerUnit.
     * 
     * @param receiptPortUnit
     */
    public void setReceiptPortUnit(org.tempuri.ManifestWSRequest_xsd.HawaiiHarbors receiptPortUnit) {
        this.receiptPortUnit = receiptPortUnit;
    }


    /**
     * Gets the receiptPlaceDescUnit value for this ManifestWSRequestDataPerUnit.
     * 
     * @return receiptPlaceDescUnit
     */
    public org.apache.axis.types.NormalizedString getReceiptPlaceDescUnit() {
        return receiptPlaceDescUnit;
    }


    /**
     * Sets the receiptPlaceDescUnit value for this ManifestWSRequestDataPerUnit.
     * 
     * @param receiptPlaceDescUnit
     */
    public void setReceiptPlaceDescUnit(org.apache.axis.types.NormalizedString receiptPlaceDescUnit) {
        this.receiptPlaceDescUnit = receiptPlaceDescUnit;
    }


    /**
     * Gets the HDOAFee value for this ManifestWSRequestDataPerUnit.
     * 
     * @return HDOAFee
     */
    public java.math.BigDecimal getHDOAFee() {
        return HDOAFee;
    }


    /**
     * Sets the HDOAFee value for this ManifestWSRequestDataPerUnit.
     * 
     * @param HDOAFee
     */
    public void setHDOAFee(java.math.BigDecimal HDOAFee) {
        this.HDOAFee = HDOAFee;
    }


    /**
     * Gets the HDOAHold value for this ManifestWSRequestDataPerUnit.
     * 
     * @return HDOAHold
     */
    public org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestDataPerUnitHDOAHold getHDOAHold() {
        return HDOAHold;
    }


    /**
     * Sets the HDOAHold value for this ManifestWSRequestDataPerUnit.
     * 
     * @param HDOAHold
     */
    public void setHDOAHold(org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestDataPerUnitHDOAHold HDOAHold) {
        this.HDOAHold = HDOAHold;
    }


    /**
     * Gets the thirdPartyCheck value for this ManifestWSRequestDataPerUnit.
     * 
     * @return thirdPartyCheck
     */
    public org.apache.axis.types.NormalizedString getThirdPartyCheck() {
        return thirdPartyCheck;
    }


    /**
     * Sets the thirdPartyCheck value for this ManifestWSRequestDataPerUnit.
     * 
     * @param thirdPartyCheck
     */
    public void setThirdPartyCheck(org.apache.axis.types.NormalizedString thirdPartyCheck) {
        this.thirdPartyCheck = thirdPartyCheck;
    }


    /**
     * Gets the HAZ value for this ManifestWSRequestDataPerUnit.
     * 
     * @return HAZ
     */
    public org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestDataPerUnitHAZ getHAZ() {
        return HAZ;
    }


    /**
     * Sets the HAZ value for this ManifestWSRequestDataPerUnit.
     * 
     * @param HAZ
     */
    public void setHAZ(org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestDataPerUnitHAZ HAZ) {
        this.HAZ = HAZ;
    }


    /**
     * Gets the temperature value for this ManifestWSRequestDataPerUnit.
     * 
     * @return temperature
     */
    public java.lang.Integer getTemperature() {
        return temperature;
    }


    /**
     * Sets the temperature value for this ManifestWSRequestDataPerUnit.
     * 
     * @param temperature
     */
    public void setTemperature(java.lang.Integer temperature) {
        this.temperature = temperature;
    }


    /**
     * Gets the tempSpecification value for this ManifestWSRequestDataPerUnit.
     * 
     * @return tempSpecification
     */
    public org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestDataPerUnitTempSpecification getTempSpecification() {
        return tempSpecification;
    }


    /**
     * Sets the tempSpecification value for this ManifestWSRequestDataPerUnit.
     * 
     * @param tempSpecification
     */
    public void setTempSpecification(org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestDataPerUnitTempSpecification tempSpecification) {
        this.tempSpecification = tempSpecification;
    }


    /**
     * Gets the dischargeService value for this ManifestWSRequestDataPerUnit.
     * 
     * @return dischargeService
     */
    public org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestDataPerUnitDischargeService getDischargeService() {
        return dischargeService;
    }


    /**
     * Sets the dischargeService value for this ManifestWSRequestDataPerUnit.
     * 
     * @param dischargeService
     */
    public void setDischargeService(org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestDataPerUnitDischargeService dischargeService) {
        this.dischargeService = dischargeService;
    }


    /**
     * Gets the hatchCellNo value for this ManifestWSRequestDataPerUnit.
     * 
     * @return hatchCellNo
     */
    public org.apache.axis.types.NormalizedString getHatchCellNo() {
        return hatchCellNo;
    }


    /**
     * Sets the hatchCellNo value for this ManifestWSRequestDataPerUnit.
     * 
     * @param hatchCellNo
     */
    public void setHatchCellNo(org.apache.axis.types.NormalizedString hatchCellNo) {
        this.hatchCellNo = hatchCellNo;
    }


    /**
     * Gets the shipperSealNo value for this ManifestWSRequestDataPerUnit.
     * 
     * @return shipperSealNo
     */
    public org.apache.axis.types.NormalizedString getShipperSealNo() {
        return shipperSealNo;
    }


    /**
     * Sets the shipperSealNo value for this ManifestWSRequestDataPerUnit.
     * 
     * @param shipperSealNo
     */
    public void setShipperSealNo(org.apache.axis.types.NormalizedString shipperSealNo) {
        this.shipperSealNo = shipperSealNo;
    }


    /**
     * Gets the TOS value for this ManifestWSRequestDataPerUnit.
     * 
     * @return TOS
     */
    public org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestDataPerUnitTOS getTOS() {
        return TOS;
    }


    /**
     * Sets the TOS value for this ManifestWSRequestDataPerUnit.
     * 
     * @param TOS
     */
    public void setTOS(org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestDataPerUnitTOS TOS) {
        this.TOS = TOS;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ManifestWSRequestDataPerUnit)) return false;
        ManifestWSRequestDataPerUnit other = (ManifestWSRequestDataPerUnit) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.transactionType==null && other.getTransactionType()==null) || 
             (this.transactionType!=null &&
              this.transactionType.equals(other.getTransactionType()))) &&
            ((this.containerNo==null && other.getContainerNo()==null) || 
             (this.containerNo!=null &&
              this.containerNo.equals(other.getContainerNo()))) &&
            ((this.nonContainerNo==null && other.getNonContainerNo()==null) || 
             (this.nonContainerNo!=null &&
              this.nonContainerNo.equals(other.getNonContainerNo()))) &&
            ((this.VINRefNo==null && other.getVINRefNo()==null) || 
             (this.VINRefNo!=null &&
              this.VINRefNo.equals(other.getVINRefNo()))) &&
            ((this.containerNonType==null && other.getContainerNonType()==null) || 
             (this.containerNonType!=null &&
              this.containerNonType.equals(other.getContainerNonType()))) &&
            ((this.contentsCommodity==null && other.getContentsCommodity()==null) || 
             (this.contentsCommodity!=null &&
              this.contentsCommodity.equals(other.getContentsCommodity()))) &&
            ((this.tariffDesc==null && other.getTariffDesc()==null) || 
             (this.tariffDesc!=null &&
              this.tariffDesc.equals(other.getTariffDesc()))) &&
            ((this.weightSpecification==null && other.getWeightSpecification()==null) || 
             (this.weightSpecification!=null &&
              this.weightSpecification.equals(other.getWeightSpecification()))) &&
            ((this.grossWeight==null && other.getGrossWeight()==null) || 
             (this.grossWeight!=null &&
              this.grossWeight.equals(other.getGrossWeight()))) &&
            ((this.netWeight==null && other.getNetWeight()==null) || 
             (this.netWeight!=null &&
              this.netWeight.equals(other.getNetWeight()))) &&
            ((this.containerSizeFeet==null && other.getContainerSizeFeet()==null) || 
             (this.containerSizeFeet!=null &&
              this.containerSizeFeet.equals(other.getContainerSizeFeet()))) &&
            ((this.containerParcelCount==null && other.getContainerParcelCount()==null) || 
             (this.containerParcelCount!=null &&
              this.containerParcelCount.equals(other.getContainerParcelCount()))) &&
            ((this.billOfLadingNo==null && other.getBillOfLadingNo()==null) || 
             (this.billOfLadingNo!=null &&
              this.billOfLadingNo.equals(other.getBillOfLadingNo()))) &&
            ((this.marksMake==null && other.getMarksMake()==null) || 
             (this.marksMake!=null &&
              this.marksMake.equals(other.getMarksMake()))) &&
            ((this.shippersRef==null && other.getShippersRef()==null) || 
             (this.shippersRef!=null &&
              this.shippersRef.equals(other.getShippersRef()))) &&
            ((this.shipper==null && other.getShipper()==null) || 
             (this.shipper!=null &&
              this.shipper.equals(other.getShipper()))) &&
            ((this.consignee==null && other.getConsignee()==null) || 
             (this.consignee!=null &&
              this.consignee.equals(other.getConsignee()))) &&
            ((this.portOfOriginUnit==null && other.getPortOfOriginUnit()==null) || 
             (this.portOfOriginUnit!=null &&
              this.portOfOriginUnit.equals(other.getPortOfOriginUnit()))) &&
            ((this.inlandPortOriginUnit==null && other.getInlandPortOriginUnit()==null) || 
             (this.inlandPortOriginUnit!=null &&
              this.inlandPortOriginUnit.equals(other.getInlandPortOriginUnit()))) &&
            ((this.deliveryPortUnit==null && other.getDeliveryPortUnit()==null) || 
             (this.deliveryPortUnit!=null &&
              this.deliveryPortUnit.equals(other.getDeliveryPortUnit()))) &&
            ((this.deliveryPlaceDescUnit==null && other.getDeliveryPlaceDescUnit()==null) || 
             (this.deliveryPlaceDescUnit!=null &&
              this.deliveryPlaceDescUnit.equals(other.getDeliveryPlaceDescUnit()))) &&
            ((this.receiptPortUnit==null && other.getReceiptPortUnit()==null) || 
             (this.receiptPortUnit!=null &&
              this.receiptPortUnit.equals(other.getReceiptPortUnit()))) &&
            ((this.receiptPlaceDescUnit==null && other.getReceiptPlaceDescUnit()==null) || 
             (this.receiptPlaceDescUnit!=null &&
              this.receiptPlaceDescUnit.equals(other.getReceiptPlaceDescUnit()))) &&
            ((this.HDOAFee==null && other.getHDOAFee()==null) || 
             (this.HDOAFee!=null &&
              this.HDOAFee.equals(other.getHDOAFee()))) &&
            ((this.HDOAHold==null && other.getHDOAHold()==null) || 
             (this.HDOAHold!=null &&
              this.HDOAHold.equals(other.getHDOAHold()))) &&
            ((this.thirdPartyCheck==null && other.getThirdPartyCheck()==null) || 
             (this.thirdPartyCheck!=null &&
              this.thirdPartyCheck.equals(other.getThirdPartyCheck()))) &&
            ((this.HAZ==null && other.getHAZ()==null) || 
             (this.HAZ!=null &&
              this.HAZ.equals(other.getHAZ()))) &&
            ((this.temperature==null && other.getTemperature()==null) || 
             (this.temperature!=null &&
              this.temperature.equals(other.getTemperature()))) &&
            ((this.tempSpecification==null && other.getTempSpecification()==null) || 
             (this.tempSpecification!=null &&
              this.tempSpecification.equals(other.getTempSpecification()))) &&
            ((this.dischargeService==null && other.getDischargeService()==null) || 
             (this.dischargeService!=null &&
              this.dischargeService.equals(other.getDischargeService()))) &&
            ((this.hatchCellNo==null && other.getHatchCellNo()==null) || 
             (this.hatchCellNo!=null &&
              this.hatchCellNo.equals(other.getHatchCellNo()))) &&
            ((this.shipperSealNo==null && other.getShipperSealNo()==null) || 
             (this.shipperSealNo!=null &&
              this.shipperSealNo.equals(other.getShipperSealNo()))) &&
            ((this.TOS==null && other.getTOS()==null) || 
             (this.TOS!=null &&
              this.TOS.equals(other.getTOS())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getTransactionType() != null) {
            _hashCode += getTransactionType().hashCode();
        }
        if (getContainerNo() != null) {
            _hashCode += getContainerNo().hashCode();
        }
        if (getNonContainerNo() != null) {
            _hashCode += getNonContainerNo().hashCode();
        }
        if (getVINRefNo() != null) {
            _hashCode += getVINRefNo().hashCode();
        }
        if (getContainerNonType() != null) {
            _hashCode += getContainerNonType().hashCode();
        }
        if (getContentsCommodity() != null) {
            _hashCode += getContentsCommodity().hashCode();
        }
        if (getTariffDesc() != null) {
            _hashCode += getTariffDesc().hashCode();
        }
        if (getWeightSpecification() != null) {
            _hashCode += getWeightSpecification().hashCode();
        }
        if (getGrossWeight() != null) {
            _hashCode += getGrossWeight().hashCode();
        }
        if (getNetWeight() != null) {
            _hashCode += getNetWeight().hashCode();
        }
        if (getContainerSizeFeet() != null) {
            _hashCode += getContainerSizeFeet().hashCode();
        }
        if (getContainerParcelCount() != null) {
            _hashCode += getContainerParcelCount().hashCode();
        }
        if (getBillOfLadingNo() != null) {
            _hashCode += getBillOfLadingNo().hashCode();
        }
        if (getMarksMake() != null) {
            _hashCode += getMarksMake().hashCode();
        }
        if (getShippersRef() != null) {
            _hashCode += getShippersRef().hashCode();
        }
        if (getShipper() != null) {
            _hashCode += getShipper().hashCode();
        }
        if (getConsignee() != null) {
            _hashCode += getConsignee().hashCode();
        }
        if (getPortOfOriginUnit() != null) {
            _hashCode += getPortOfOriginUnit().hashCode();
        }
        if (getInlandPortOriginUnit() != null) {
            _hashCode += getInlandPortOriginUnit().hashCode();
        }
        if (getDeliveryPortUnit() != null) {
            _hashCode += getDeliveryPortUnit().hashCode();
        }
        if (getDeliveryPlaceDescUnit() != null) {
            _hashCode += getDeliveryPlaceDescUnit().hashCode();
        }
        if (getReceiptPortUnit() != null) {
            _hashCode += getReceiptPortUnit().hashCode();
        }
        if (getReceiptPlaceDescUnit() != null) {
            _hashCode += getReceiptPlaceDescUnit().hashCode();
        }
        if (getHDOAFee() != null) {
            _hashCode += getHDOAFee().hashCode();
        }
        if (getHDOAHold() != null) {
            _hashCode += getHDOAHold().hashCode();
        }
        if (getThirdPartyCheck() != null) {
            _hashCode += getThirdPartyCheck().hashCode();
        }
        if (getHAZ() != null) {
            _hashCode += getHAZ().hashCode();
        }
        if (getTemperature() != null) {
            _hashCode += getTemperature().hashCode();
        }
        if (getTempSpecification() != null) {
            _hashCode += getTempSpecification().hashCode();
        }
        if (getDischargeService() != null) {
            _hashCode += getDischargeService().hashCode();
        }
        if (getHatchCellNo() != null) {
            _hashCode += getHatchCellNo().hashCode();
        }
        if (getShipperSealNo() != null) {
            _hashCode += getShipperSealNo().hashCode();
        }
        if (getTOS() != null) {
            _hashCode += getTOS().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ManifestWSRequestDataPerUnit.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "ManifestWSRequestDataPerUnit"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("transactionType");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "TransactionType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "ManifestWSRequestDataPerUnitTransactionType"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("containerNo");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "ContainerNo"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "normalizedString"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("nonContainerNo");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "NonContainerNo"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "normalizedString"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("VINRefNo");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "VINRefNo"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "normalizedString"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("containerNonType");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "ContainerNonType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "ManifestWSRequestDataPerUnitContainerNonType"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("contentsCommodity");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "ContentsCommodity"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "normalizedString"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("tariffDesc");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "TariffDesc"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("weightSpecification");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "WeightSpecification"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "ManifestWSRequestDataPerUnitWeightSpecification"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("grossWeight");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "GrossWeight"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "decimal"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("netWeight");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "NetWeight"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "decimal"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("containerSizeFeet");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "ContainerSizeFeet"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("containerParcelCount");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "ContainerParcelCount"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("billOfLadingNo");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "BillOfLadingNo"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "normalizedString"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("marksMake");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "MarksMake"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "normalizedString"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("shippersRef");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "ShippersRef"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "normalizedString"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("shipper");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "Shipper"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "normalizedString"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("consignee");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "Consignee"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "normalizedString"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("portOfOriginUnit");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "PortOfOriginUnit"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "normalizedString"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("inlandPortOriginUnit");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "InlandPortOriginUnit"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "normalizedString"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("deliveryPortUnit");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "DeliveryPortUnit"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "HawaiiHarbors"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("deliveryPlaceDescUnit");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "DeliveryPlaceDescUnit"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "normalizedString"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("receiptPortUnit");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "ReceiptPortUnit"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "HawaiiHarbors"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("receiptPlaceDescUnit");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "ReceiptPlaceDescUnit"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "normalizedString"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("HDOAFee");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "HDOAFee"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "decimal"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("HDOAHold");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "HDOAHold"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "ManifestWSRequestDataPerUnitHDOAHold"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("thirdPartyCheck");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "ThirdPartyCheck"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "normalizedString"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("HAZ");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "HAZ"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "ManifestWSRequestDataPerUnitHAZ"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("temperature");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "Temperature"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("tempSpecification");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "TempSpecification"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "ManifestWSRequestDataPerUnitTempSpecification"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("dischargeService");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "DischargeService"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "ManifestWSRequestDataPerUnitDischargeService"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("hatchCellNo");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "HatchCellNo"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "normalizedString"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("shipperSealNo");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "ShipperSealNo"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "normalizedString"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("TOS");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "TOS"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "ManifestWSRequestDataPerUnitTOS"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
