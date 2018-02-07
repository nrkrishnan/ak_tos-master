/**
 * VINSightHDOACommodity.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.matson.vinsight.webservice.cars.ws.to;

import org.apache.commons.lang.builder.ToStringBuilder;

public class VINSightHDOACommodity  implements java.io.Serializable {
    private boolean agHold;

    private java.lang.String bookingNumber;

    private java.lang.String commodityShortDescription;

    private int commodityType;

    private java.lang.String consignee;

    private java.lang.String destinationPort;

    private boolean haz;

    private java.lang.String make;

    private java.lang.String model;

    private double netWeight;

    private java.lang.String originPort;

    private java.lang.String reasonForHoldFailure;

    private java.lang.String shipper;

    private java.lang.String vinNumber;

    private int vinsightId;

    private java.lang.String weightUnit;

    private int year;

    public VINSightHDOACommodity() {
    }

    public VINSightHDOACommodity(
           boolean agHold,
           java.lang.String bookingNumber,
           java.lang.String commodityShortDescription,
           int commodityType,
           java.lang.String consignee,
           java.lang.String destinationPort,
           boolean haz,
           java.lang.String make,
           java.lang.String model,
           double netWeight,
           java.lang.String originPort,
           java.lang.String reasonForHoldFailure,
           java.lang.String shipper,
           java.lang.String vinNumber,
           int vinsightId,
           java.lang.String weightUnit,
           int year) {
           this.agHold = agHold;
           this.bookingNumber = bookingNumber;
           this.commodityShortDescription = commodityShortDescription;
           this.commodityType = commodityType;
           this.consignee = consignee;
           this.destinationPort = destinationPort;
           this.haz = haz;
           this.make = make;
           this.model = model;
           this.netWeight = netWeight;
           this.originPort = originPort;
           this.reasonForHoldFailure = reasonForHoldFailure;
           this.shipper = shipper;
           this.vinNumber = vinNumber;
           this.vinsightId = vinsightId;
           this.weightUnit = weightUnit;
           this.year = year;
    }


    /**
     * Gets the agHold value for this VINSightHDOACommodity.
     * 
     * @return agHold
     */
    public boolean isAgHold() {
        return agHold;
    }


    /**
     * Sets the agHold value for this VINSightHDOACommodity.
     * 
     * @param agHold
     */
    public void setAgHold(boolean agHold) {
        this.agHold = agHold;
    }


    /**
     * Gets the bookingNumber value for this VINSightHDOACommodity.
     * 
     * @return bookingNumber
     */
    public java.lang.String getBookingNumber() {
        return bookingNumber;
    }


    /**
     * Sets the bookingNumber value for this VINSightHDOACommodity.
     * 
     * @param bookingNumber
     */
    public void setBookingNumber(java.lang.String bookingNumber) {
        this.bookingNumber = bookingNumber;
    }


    /**
     * Gets the commodityShortDescription value for this VINSightHDOACommodity.
     * 
     * @return commodityShortDescription
     */
    public java.lang.String getCommodityShortDescription() {
        return commodityShortDescription;
    }


    /**
     * Sets the commodityShortDescription value for this VINSightHDOACommodity.
     * 
     * @param commodityShortDescription
     */
    public void setCommodityShortDescription(java.lang.String commodityShortDescription) {
        this.commodityShortDescription = commodityShortDescription;
    }


    /**
     * Gets the commodityType value for this VINSightHDOACommodity.
     * 
     * @return commodityType
     */
    public int getCommodityType() {
        return commodityType;
    }


    /**
     * Sets the commodityType value for this VINSightHDOACommodity.
     * 
     * @param commodityType
     */
    public void setCommodityType(int commodityType) {
        this.commodityType = commodityType;
    }


    /**
     * Gets the consignee value for this VINSightHDOACommodity.
     * 
     * @return consignee
     */
    public java.lang.String getConsignee() {
        return consignee;
    }


    /**
     * Sets the consignee value for this VINSightHDOACommodity.
     * 
     * @param consignee
     */
    public void setConsignee(java.lang.String consignee) {
        this.consignee = consignee;
    }


    /**
     * Gets the destinationPort value for this VINSightHDOACommodity.
     * 
     * @return destinationPort
     */
    public java.lang.String getDestinationPort() {
        return destinationPort;
    }


    /**
     * Sets the destinationPort value for this VINSightHDOACommodity.
     * 
     * @param destinationPort
     */
    public void setDestinationPort(java.lang.String destinationPort) {
        this.destinationPort = destinationPort;
    }


    /**
     * Gets the haz value for this VINSightHDOACommodity.
     * 
     * @return haz
     */
    public boolean isHaz() {
        return haz;
    }


    /**
     * Sets the haz value for this VINSightHDOACommodity.
     * 
     * @param haz
     */
    public void setHaz(boolean haz) {
        this.haz = haz;
    }


    /**
     * Gets the make value for this VINSightHDOACommodity.
     * 
     * @return make
     */
    public java.lang.String getMake() {
        return make;
    }


    /**
     * Sets the make value for this VINSightHDOACommodity.
     * 
     * @param make
     */
    public void setMake(java.lang.String make) {
        this.make = make;
    }


    /**
     * Gets the model value for this VINSightHDOACommodity.
     * 
     * @return model
     */
    public java.lang.String getModel() {
        return model;
    }


    /**
     * Sets the model value for this VINSightHDOACommodity.
     * 
     * @param model
     */
    public void setModel(java.lang.String model) {
        this.model = model;
    }


    /**
     * Gets the netWeight value for this VINSightHDOACommodity.
     * 
     * @return netWeight
     */
    public double getNetWeight() {
        return netWeight;
    }


    /**
     * Sets the netWeight value for this VINSightHDOACommodity.
     * 
     * @param netWeight
     */
    public void setNetWeight(double netWeight) {
        this.netWeight = netWeight;
    }


    /**
     * Gets the originPort value for this VINSightHDOACommodity.
     * 
     * @return originPort
     */
    public java.lang.String getOriginPort() {
        return originPort;
    }


    /**
     * Sets the originPort value for this VINSightHDOACommodity.
     * 
     * @param originPort
     */
    public void setOriginPort(java.lang.String originPort) {
        this.originPort = originPort;
    }


    /**
     * Gets the reasonForHoldFailure value for this VINSightHDOACommodity.
     * 
     * @return reasonForHoldFailure
     */
    public java.lang.String getReasonForHoldFailure() {
        return reasonForHoldFailure;
    }


    /**
     * Sets the reasonForHoldFailure value for this VINSightHDOACommodity.
     * 
     * @param reasonForHoldFailure
     */
    public void setReasonForHoldFailure(java.lang.String reasonForHoldFailure) {
        this.reasonForHoldFailure = reasonForHoldFailure;
    }


    /**
     * Gets the shipper value for this VINSightHDOACommodity.
     * 
     * @return shipper
     */
    public java.lang.String getShipper() {
        return shipper;
    }


    /**
     * Sets the shipper value for this VINSightHDOACommodity.
     * 
     * @param shipper
     */
    public void setShipper(java.lang.String shipper) {
        this.shipper = shipper;
    }


    /**
     * Gets the vinNumber value for this VINSightHDOACommodity.
     * 
     * @return vinNumber
     */
    public java.lang.String getVinNumber() {
        return vinNumber;
    }


    /**
     * Sets the vinNumber value for this VINSightHDOACommodity.
     * 
     * @param vinNumber
     */
    public void setVinNumber(java.lang.String vinNumber) {
        this.vinNumber = vinNumber;
    }


    /**
     * Gets the vinsightId value for this VINSightHDOACommodity.
     * 
     * @return vinsightId
     */
    public int getVinsightId() {
        return vinsightId;
    }


    /**
     * Sets the vinsightId value for this VINSightHDOACommodity.
     * 
     * @param vinsightId
     */
    public void setVinsightId(int vinsightId) {
        this.vinsightId = vinsightId;
    }


    /**
     * Gets the weightUnit value for this VINSightHDOACommodity.
     * 
     * @return weightUnit
     */
    public java.lang.String getWeightUnit() {
        return weightUnit;
    }


    /**
     * Sets the weightUnit value for this VINSightHDOACommodity.
     * 
     * @param weightUnit
     */
    public void setWeightUnit(java.lang.String weightUnit) {
        this.weightUnit = weightUnit;
    }


    /**
     * Gets the year value for this VINSightHDOACommodity.
     * 
     * @return year
     */
    public int getYear() {
        return year;
    }


    /**
     * Sets the year value for this VINSightHDOACommodity.
     * 
     * @param year
     */
    public void setYear(int year) {
        this.year = year;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof VINSightHDOACommodity)) return false;
        VINSightHDOACommodity other = (VINSightHDOACommodity) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            this.agHold == other.isAgHold() &&
            ((this.bookingNumber==null && other.getBookingNumber()==null) || 
             (this.bookingNumber!=null &&
              this.bookingNumber.equals(other.getBookingNumber()))) &&
            ((this.commodityShortDescription==null && other.getCommodityShortDescription()==null) || 
             (this.commodityShortDescription!=null &&
              this.commodityShortDescription.equals(other.getCommodityShortDescription()))) &&
            this.commodityType == other.getCommodityType() &&
            ((this.consignee==null && other.getConsignee()==null) || 
             (this.consignee!=null &&
              this.consignee.equals(other.getConsignee()))) &&
            ((this.destinationPort==null && other.getDestinationPort()==null) || 
             (this.destinationPort!=null &&
              this.destinationPort.equals(other.getDestinationPort()))) &&
            this.haz == other.isHaz() &&
            ((this.make==null && other.getMake()==null) || 
             (this.make!=null &&
              this.make.equals(other.getMake()))) &&
            ((this.model==null && other.getModel()==null) || 
             (this.model!=null &&
              this.model.equals(other.getModel()))) &&
            this.netWeight == other.getNetWeight() &&
            ((this.originPort==null && other.getOriginPort()==null) || 
             (this.originPort!=null &&
              this.originPort.equals(other.getOriginPort()))) &&
            ((this.reasonForHoldFailure==null && other.getReasonForHoldFailure()==null) || 
             (this.reasonForHoldFailure!=null &&
              this.reasonForHoldFailure.equals(other.getReasonForHoldFailure()))) &&
            ((this.shipper==null && other.getShipper()==null) || 
             (this.shipper!=null &&
              this.shipper.equals(other.getShipper()))) &&
            ((this.vinNumber==null && other.getVinNumber()==null) || 
             (this.vinNumber!=null &&
              this.vinNumber.equals(other.getVinNumber()))) &&
            this.vinsightId == other.getVinsightId() &&
            ((this.weightUnit==null && other.getWeightUnit()==null) || 
             (this.weightUnit!=null &&
              this.weightUnit.equals(other.getWeightUnit()))) &&
            this.year == other.getYear();
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
        _hashCode += (isAgHold() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        if (getBookingNumber() != null) {
            _hashCode += getBookingNumber().hashCode();
        }
        if (getCommodityShortDescription() != null) {
            _hashCode += getCommodityShortDescription().hashCode();
        }
        _hashCode += getCommodityType();
        if (getConsignee() != null) {
            _hashCode += getConsignee().hashCode();
        }
        if (getDestinationPort() != null) {
            _hashCode += getDestinationPort().hashCode();
        }
        _hashCode += (isHaz() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        if (getMake() != null) {
            _hashCode += getMake().hashCode();
        }
        if (getModel() != null) {
            _hashCode += getModel().hashCode();
        }
        _hashCode += new Double(getNetWeight()).hashCode();
        if (getOriginPort() != null) {
            _hashCode += getOriginPort().hashCode();
        }
        if (getReasonForHoldFailure() != null) {
            _hashCode += getReasonForHoldFailure().hashCode();
        }
        if (getShipper() != null) {
            _hashCode += getShipper().hashCode();
        }
        if (getVinNumber() != null) {
            _hashCode += getVinNumber().hashCode();
        }
        _hashCode += getVinsightId();
        if (getWeightUnit() != null) {
            _hashCode += getWeightUnit().hashCode();
        }
        _hashCode += getYear();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(VINSightHDOACommodity.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("java:com.matson.cars.ws.to", "VINSightHDOACommodity"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("agHold");
        elemField.setXmlName(new javax.xml.namespace.QName("java:com.matson.cars.ws.to", "agHold"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("bookingNumber");
        elemField.setXmlName(new javax.xml.namespace.QName("java:com.matson.cars.ws.to", "bookingNumber"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("commodityShortDescription");
        elemField.setXmlName(new javax.xml.namespace.QName("java:com.matson.cars.ws.to", "commodityShortDescription"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("commodityType");
        elemField.setXmlName(new javax.xml.namespace.QName("java:com.matson.cars.ws.to", "commodityType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("consignee");
        elemField.setXmlName(new javax.xml.namespace.QName("java:com.matson.cars.ws.to", "consignee"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("destinationPort");
        elemField.setXmlName(new javax.xml.namespace.QName("java:com.matson.cars.ws.to", "destinationPort"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("haz");
        elemField.setXmlName(new javax.xml.namespace.QName("java:com.matson.cars.ws.to", "haz"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("make");
        elemField.setXmlName(new javax.xml.namespace.QName("java:com.matson.cars.ws.to", "make"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("model");
        elemField.setXmlName(new javax.xml.namespace.QName("java:com.matson.cars.ws.to", "model"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("netWeight");
        elemField.setXmlName(new javax.xml.namespace.QName("java:com.matson.cars.ws.to", "netWeight"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("originPort");
        elemField.setXmlName(new javax.xml.namespace.QName("java:com.matson.cars.ws.to", "originPort"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("reasonForHoldFailure");
        elemField.setXmlName(new javax.xml.namespace.QName("java:com.matson.cars.ws.to", "reasonForHoldFailure"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("shipper");
        elemField.setXmlName(new javax.xml.namespace.QName("java:com.matson.cars.ws.to", "shipper"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("vinNumber");
        elemField.setXmlName(new javax.xml.namespace.QName("java:com.matson.cars.ws.to", "vinNumber"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("vinsightId");
        elemField.setXmlName(new javax.xml.namespace.QName("java:com.matson.cars.ws.to", "vinsightId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("weightUnit");
        elemField.setXmlName(new javax.xml.namespace.QName("java:com.matson.cars.ws.to", "weightUnit"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("year");
        elemField.setXmlName(new javax.xml.namespace.QName("java:com.matson.cars.ws.to", "year"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
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

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this).append("typeDesc", this.getTypeDesc())
				.append("model", this.model)
				.append("vinNumber", this.vinNumber).append("haz", this.haz)
				.append("commodityShortDescription",
						this.commodityShortDescription).append("year",
						this.year).append("vinsightId", this.vinsightId)
				.append("reasonForHoldFailure", this.reasonForHoldFailure)
				.append("originPort", this.originPort).append("weightUnit",
						this.weightUnit).append("shipper", this.shipper)
				.append("destinationPort", this.destinationPort).append("make",
						this.make).append("commodityType", this.commodityType)
				.append("agHold", this.agHold).append("consignee",
						this.consignee).append("netWeight", this.netWeight)
				.append("bookingNumber", this.bookingNumber).toString();
	}

}
