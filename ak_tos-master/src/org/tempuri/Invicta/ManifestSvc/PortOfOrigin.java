/**
 * PortOfOrigin.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.tempuri.Invicta.ManifestSvc;

public class PortOfOrigin  implements java.io.Serializable {
    private java.lang.String airportCode;

    private java.lang.String cityState;

    private java.lang.String country;

    public PortOfOrigin() {
    }

    public PortOfOrigin(
           java.lang.String airportCode,
           java.lang.String cityState,
           java.lang.String country) {
           this.airportCode = airportCode;
           this.cityState = cityState;
           this.country = country;
    }


    /**
     * Gets the airportCode value for this PortOfOrigin.
     * 
     * @return airportCode
     */
    public java.lang.String getAirportCode() {
        return airportCode;
    }


    /**
     * Sets the airportCode value for this PortOfOrigin.
     * 
     * @param airportCode
     */
    public void setAirportCode(java.lang.String airportCode) {
        this.airportCode = airportCode;
    }


    /**
     * Gets the cityState value for this PortOfOrigin.
     * 
     * @return cityState
     */
    public java.lang.String getCityState() {
        return cityState;
    }


    /**
     * Sets the cityState value for this PortOfOrigin.
     * 
     * @param cityState
     */
    public void setCityState(java.lang.String cityState) {
        this.cityState = cityState;
    }


    /**
     * Gets the country value for this PortOfOrigin.
     * 
     * @return country
     */
    public java.lang.String getCountry() {
        return country;
    }


    /**
     * Sets the country value for this PortOfOrigin.
     * 
     * @param country
     */
    public void setCountry(java.lang.String country) {
        this.country = country;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof PortOfOrigin)) return false;
        PortOfOrigin other = (PortOfOrigin) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.airportCode==null && other.getAirportCode()==null) || 
             (this.airportCode!=null &&
              this.airportCode.equals(other.getAirportCode()))) &&
            ((this.cityState==null && other.getCityState()==null) || 
             (this.cityState!=null &&
              this.cityState.equals(other.getCityState()))) &&
            ((this.country==null && other.getCountry()==null) || 
             (this.country!=null &&
              this.country.equals(other.getCountry())));
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
        if (getAirportCode() != null) {
            _hashCode += getAirportCode().hashCode();
        }
        if (getCityState() != null) {
            _hashCode += getCityState().hashCode();
        }
        if (getCountry() != null) {
            _hashCode += getCountry().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(PortOfOrigin.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/Invicta/ManifestSvc", "PortOfOrigin"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("airportCode");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/Invicta/ManifestSvc", "airportCode"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("cityState");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/Invicta/ManifestSvc", "cityState"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("country");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/Invicta/ManifestSvc", "country"));
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
