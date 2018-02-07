/**
 * ScopeCoordinateIdsWsType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package com.matson.tos.nascent.webservice;

public class ScopeCoordinateIdsWsType  implements java.io.Serializable {
    private java.lang.String operatorId;
    private java.lang.String complexId;
    private java.lang.String facilityId;
    private java.lang.String yardId;
    private java.lang.String externalUserId;

    public ScopeCoordinateIdsWsType() {
    }

    public ScopeCoordinateIdsWsType(
           java.lang.String operatorId,
           java.lang.String complexId,
           java.lang.String facilityId,
           java.lang.String yardId,
           java.lang.String externalUserId) {
           this.operatorId = operatorId;
           this.complexId = complexId;
           this.facilityId = facilityId;
           this.yardId = yardId;
           this.externalUserId = externalUserId;
    }


    /**
     * Gets the operatorId value for this ScopeCoordinateIdsWsType.
     * 
     * @return operatorId
     */
    public java.lang.String getOperatorId() {
        return operatorId;
    }


    /**
     * Sets the operatorId value for this ScopeCoordinateIdsWsType.
     * 
     * @param operatorId
     */
    public void setOperatorId(java.lang.String operatorId) {
        this.operatorId = operatorId;
    }


    /**
     * Gets the complexId value for this ScopeCoordinateIdsWsType.
     * 
     * @return complexId
     */
    public java.lang.String getComplexId() {
        return complexId;
    }


    /**
     * Sets the complexId value for this ScopeCoordinateIdsWsType.
     * 
     * @param complexId
     */
    public void setComplexId(java.lang.String complexId) {
        this.complexId = complexId;
    }


    /**
     * Gets the facilityId value for this ScopeCoordinateIdsWsType.
     * 
     * @return facilityId
     */
    public java.lang.String getFacilityId() {
        return facilityId;
    }


    /**
     * Sets the facilityId value for this ScopeCoordinateIdsWsType.
     * 
     * @param facilityId
     */
    public void setFacilityId(java.lang.String facilityId) {
        this.facilityId = facilityId;
    }


    /**
     * Gets the yardId value for this ScopeCoordinateIdsWsType.
     * 
     * @return yardId
     */
    public java.lang.String getYardId() {
        return yardId;
    }


    /**
     * Sets the yardId value for this ScopeCoordinateIdsWsType.
     * 
     * @param yardId
     */
    public void setYardId(java.lang.String yardId) {
        this.yardId = yardId;
    }


    /**
     * Gets the externalUserId value for this ScopeCoordinateIdsWsType.
     * 
     * @return externalUserId
     */
    public java.lang.String getExternalUserId() {
        return externalUserId;
    }


    /**
     * Sets the externalUserId value for this ScopeCoordinateIdsWsType.
     * 
     * @param externalUserId
     */
    public void setExternalUserId(java.lang.String externalUserId) {
        this.externalUserId = externalUserId;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ScopeCoordinateIdsWsType)) return false;
        ScopeCoordinateIdsWsType other = (ScopeCoordinateIdsWsType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.operatorId==null && other.getOperatorId()==null) || 
             (this.operatorId!=null &&
              this.operatorId.equals(other.getOperatorId()))) &&
            ((this.complexId==null && other.getComplexId()==null) || 
             (this.complexId!=null &&
              this.complexId.equals(other.getComplexId()))) &&
            ((this.facilityId==null && other.getFacilityId()==null) || 
             (this.facilityId!=null &&
              this.facilityId.equals(other.getFacilityId()))) &&
            ((this.yardId==null && other.getYardId()==null) || 
             (this.yardId!=null &&
              this.yardId.equals(other.getYardId()))) &&
            ((this.externalUserId==null && other.getExternalUserId()==null) || 
             (this.externalUserId!=null &&
              this.externalUserId.equals(other.getExternalUserId())));
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
        if (getOperatorId() != null) {
            _hashCode += getOperatorId().hashCode();
        }
        if (getComplexId() != null) {
            _hashCode += getComplexId().hashCode();
        }
        if (getFacilityId() != null) {
            _hashCode += getFacilityId().hashCode();
        }
        if (getYardId() != null) {
            _hashCode += getYardId().hashCode();
        }
        if (getExternalUserId() != null) {
            _hashCode += getExternalUserId().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ScopeCoordinateIdsWsType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://types.webservice.argo.navis.com/v1.0", "ScopeCoordinateIdsWsType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("operatorId");
        elemField.setXmlName(new javax.xml.namespace.QName("http://types.webservice.argo.navis.com/v1.0", "operatorId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("complexId");
        elemField.setXmlName(new javax.xml.namespace.QName("http://types.webservice.argo.navis.com/v1.0", "complexId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("facilityId");
        elemField.setXmlName(new javax.xml.namespace.QName("http://types.webservice.argo.navis.com/v1.0", "facilityId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("yardId");
        elemField.setXmlName(new javax.xml.namespace.QName("http://types.webservice.argo.navis.com/v1.0", "yardId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("externalUserId");
        elemField.setXmlName(new javax.xml.namespace.QName("http://types.webservice.argo.navis.com/v1.0", "externalUserId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
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
