/**
 * GenericInvokeResponseWsType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package com.matson.tos.nascent.webservice;

public class GenericInvokeResponseWsType  implements java.io.Serializable {
    private com.matson.tos.nascent.webservice.ResponseType commonResponse;
    private java.lang.String status;
    private java.lang.String responsePayLoad;

    public GenericInvokeResponseWsType() {
    }

    public GenericInvokeResponseWsType(
           com.matson.tos.nascent.webservice.ResponseType commonResponse,
           java.lang.String status,
           java.lang.String responsePayLoad) {
           this.commonResponse = commonResponse;
           this.status = status;
           this.responsePayLoad = responsePayLoad;
    }


    /**
     * Gets the commonResponse value for this GenericInvokeResponseWsType.
     * 
     * @return commonResponse
     */
    public com.matson.tos.nascent.webservice.ResponseType getCommonResponse() {
        return commonResponse;
    }


    /**
     * Sets the commonResponse value for this GenericInvokeResponseWsType.
     * 
     * @param commonResponse
     */
    public void setCommonResponse(com.matson.tos.nascent.webservice.ResponseType commonResponse) {
        this.commonResponse = commonResponse;
    }


    /**
     * Gets the status value for this GenericInvokeResponseWsType.
     * 
     * @return status
     */
    public java.lang.String getStatus() {
        return status;
    }


    /**
     * Sets the status value for this GenericInvokeResponseWsType.
     * 
     * @param status
     */
    public void setStatus(java.lang.String status) {
        this.status = status;
    }


    /**
     * Gets the responsePayLoad value for this GenericInvokeResponseWsType.
     * 
     * @return responsePayLoad
     */
    public java.lang.String getResponsePayLoad() {
        return responsePayLoad;
    }


    /**
     * Sets the responsePayLoad value for this GenericInvokeResponseWsType.
     * 
     * @param responsePayLoad
     */
    public void setResponsePayLoad(java.lang.String responsePayLoad) {
        this.responsePayLoad = responsePayLoad;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GenericInvokeResponseWsType)) return false;
        GenericInvokeResponseWsType other = (GenericInvokeResponseWsType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.commonResponse==null && other.getCommonResponse()==null) || 
             (this.commonResponse!=null &&
              this.commonResponse.equals(other.getCommonResponse()))) &&
            ((this.status==null && other.getStatus()==null) || 
             (this.status!=null &&
              this.status.equals(other.getStatus()))) &&
            ((this.responsePayLoad==null && other.getResponsePayLoad()==null) || 
             (this.responsePayLoad!=null &&
              this.responsePayLoad.equals(other.getResponsePayLoad())));
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
        if (getCommonResponse() != null) {
            _hashCode += getCommonResponse().hashCode();
        }
        if (getStatus() != null) {
            _hashCode += getStatus().hashCode();
        }
        if (getResponsePayLoad() != null) {
            _hashCode += getResponsePayLoad().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(GenericInvokeResponseWsType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://types.webservice.argo.navis.com/v1.0", "GenericInvokeResponseWsType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("commonResponse");
        elemField.setXmlName(new javax.xml.namespace.QName("http://types.webservice.argo.navis.com/v1.0", "commonResponse"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://types.webservice.argo.navis.com/v1.0", "ResponseType"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("status");
        elemField.setXmlName(new javax.xml.namespace.QName("http://types.webservice.argo.navis.com/v1.0", "status"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("responsePayLoad");
        elemField.setXmlName(new javax.xml.namespace.QName("http://types.webservice.argo.navis.com/v1.0", "responsePayLoad"));
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
