/**
 * ManifestWSResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.tempuri.Invicta.ManifestSvc;

public class ManifestWSResponse  implements java.io.Serializable {
    private org.tempuri.Invicta.ManifestSvc.Acknowledgment ack;

    public ManifestWSResponse() {
    }

    public ManifestWSResponse(
           org.tempuri.Invicta.ManifestSvc.Acknowledgment ack) {
           this.ack = ack;
    }


    /**
     * Gets the ack value for this ManifestWSResponse.
     * 
     * @return ack
     */
    public org.tempuri.Invicta.ManifestSvc.Acknowledgment getAck() {
        return ack;
    }


    /**
     * Sets the ack value for this ManifestWSResponse.
     * 
     * @param ack
     */
    public void setAck(org.tempuri.Invicta.ManifestSvc.Acknowledgment ack) {
        this.ack = ack;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ManifestWSResponse)) return false;
        ManifestWSResponse other = (ManifestWSResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.ack==null && other.getAck()==null) || 
             (this.ack!=null &&
              this.ack.equals(other.getAck())));
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
        if (getAck() != null) {
            _hashCode += getAck().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ManifestWSResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/Invicta/ManifestSvc", "ManifestWSResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("ack");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/Invicta/ManifestSvc", "ack"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/Invicta/ManifestSvc", "Acknowledgment"));
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
