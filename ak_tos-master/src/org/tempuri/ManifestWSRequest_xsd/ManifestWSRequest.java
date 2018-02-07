/**
 * ManifestWSRequest.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.tempuri.ManifestWSRequest_xsd;

public class ManifestWSRequest  implements java.io.Serializable {
    private org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestHeader header;

    private org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestDataPerUnit[] dataPerUnit;    

    public ManifestWSRequest() {
    }

    public ManifestWSRequest(
           org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestHeader header,
           org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestDataPerUnit[] dataPerUnit) {
           this.header = header;
           this.dataPerUnit = dataPerUnit;
    }


    /**
     * Gets the header value for this ManifestWSRequest.
     * 
     * @return header
     */
    public org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestHeader getHeader() {
        return header;
    }


    /**
     * Sets the header value for this ManifestWSRequest.
     * 
     * @param header
     */
    public void setHeader(org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestHeader header) {
        this.header = header;
    }


    /**
     * Gets the dataPerUnit value for this ManifestWSRequest.
     * 
     * @return dataPerUnit
     */
    public org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestDataPerUnit[] getDataPerUnit() {
        return dataPerUnit;
    }


    /**
     * Sets the dataPerUnit value for this ManifestWSRequest.
     * 
     * @param dataPerUnit
     */
    public void setDataPerUnit(org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestDataPerUnit[] dataPerUnit) {
        this.dataPerUnit = dataPerUnit;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ManifestWSRequest)) return false;
        ManifestWSRequest other = (ManifestWSRequest) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.header==null && other.getHeader()==null) || 
             (this.header!=null &&
              this.header.equals(other.getHeader()))) &&
            ((this.dataPerUnit==null && other.getDataPerUnit()==null) || 
             (this.dataPerUnit!=null &&
              java.util.Arrays.equals(this.dataPerUnit, other.getDataPerUnit())));
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
        if (getHeader() != null) {
            _hashCode += getHeader().hashCode();
        }
        if (getDataPerUnit() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getDataPerUnit());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getDataPerUnit(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ManifestWSRequest.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "ManifestWSRequest"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("header");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "Header"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "ManifestWSRequestHeader"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("dataPerUnit");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "DataPerUnit"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "ManifestWSRequestDataPerUnit"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "ManifestWSRequestDataPerUnit"));
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
