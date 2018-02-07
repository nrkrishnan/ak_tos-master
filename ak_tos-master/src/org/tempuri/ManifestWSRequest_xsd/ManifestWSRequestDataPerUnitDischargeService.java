/**
 * ManifestWSRequestDataPerUnitDischargeService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.tempuri.ManifestWSRequest_xsd;

public class ManifestWSRequestDataPerUnitDischargeService implements java.io.Serializable {
    private java.lang.String _value_;
    private static java.util.HashMap _table_ = new java.util.HashMap();

    // Constructor
    protected ManifestWSRequestDataPerUnitDischargeService(java.lang.String value) {
        _value_ = value;
        _table_.put(_value_,this);
    }

    public static final java.lang.String _value1 = "CY";
    public static final java.lang.String _value2 = "CFS";
    public static final java.lang.String _value3 = "AUT";
    public ManifestWSRequestDataPerUnitDischargeService() {
		super();
		// TODO Auto-generated constructor stub
	}

	public static final java.lang.String _value4 = "R/S";
    public static final java.lang.String _value5 = "CON";
    public static final ManifestWSRequestDataPerUnitDischargeService value1 = new ManifestWSRequestDataPerUnitDischargeService(_value1);
    public static final ManifestWSRequestDataPerUnitDischargeService value2 = new ManifestWSRequestDataPerUnitDischargeService(_value2);
    public static final ManifestWSRequestDataPerUnitDischargeService value3 = new ManifestWSRequestDataPerUnitDischargeService(_value3);
    public static final ManifestWSRequestDataPerUnitDischargeService value4 = new ManifestWSRequestDataPerUnitDischargeService(_value4);
    public static final ManifestWSRequestDataPerUnitDischargeService value5 = new ManifestWSRequestDataPerUnitDischargeService(_value5);
    public java.lang.String getValue() { return _value_;}
    public static ManifestWSRequestDataPerUnitDischargeService fromValue(java.lang.String value)
          throws java.lang.IllegalArgumentException {
        ManifestWSRequestDataPerUnitDischargeService enumeration = (ManifestWSRequestDataPerUnitDischargeService)
            _table_.get(value);
        if (enumeration==null) throw new java.lang.IllegalArgumentException();
        return enumeration;
    }
    public static ManifestWSRequestDataPerUnitDischargeService fromString(java.lang.String value)
          throws java.lang.IllegalArgumentException {
        return fromValue(value);
    }
    public boolean equals(java.lang.Object obj) {return (obj == this);}
    public int hashCode() { return toString().hashCode();}
    public java.lang.String toString() { return _value_;}
    public java.lang.Object readResolve() throws java.io.ObjectStreamException { return fromValue(_value_);}
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new org.apache.axis.encoding.ser.EnumSerializer(
            _javaType, _xmlType);
    }
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new org.apache.axis.encoding.ser.EnumDeserializer(
            _javaType, _xmlType);
    }
    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ManifestWSRequestDataPerUnitDischargeService.class);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "ManifestWSRequestDataPerUnitDischargeService"));
    }
    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

}
