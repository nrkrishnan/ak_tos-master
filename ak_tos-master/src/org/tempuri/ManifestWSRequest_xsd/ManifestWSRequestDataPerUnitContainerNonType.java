/**
 * ManifestWSRequestDataPerUnitContainerNonType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.tempuri.ManifestWSRequest_xsd;

public class ManifestWSRequestDataPerUnitContainerNonType implements java.io.Serializable {
    private java.lang.String _value_;
    private static java.util.HashMap _table_ = new java.util.HashMap();

    // Constructor
    protected ManifestWSRequestDataPerUnitContainerNonType(java.lang.String value) {
        _value_ = value;
        _table_.put(_value_,this);
    }

    public ManifestWSRequestDataPerUnitContainerNonType() {
		super();
		// TODO Auto-generated constructor stub
	}

	public static final java.lang.String _value1 = "1";
    public static final java.lang.String _value2 = "2";
    public static final java.lang.String _value3 = "3";
    public static final java.lang.String _value4 = "4";
    public static final java.lang.String _value5 = "5";
    public static final java.lang.String _value6 = "6";
    public static final java.lang.String _value7 = "7";
    public static final java.lang.String _value8 = "8";
    public static final java.lang.String _value9 = "9";
    public static final java.lang.String _value10 = "10";
    public static final java.lang.String _value11 = "11";
    public static final java.lang.String _value12 = "12";
    public static final java.lang.String _value13 = "13";
    public static final ManifestWSRequestDataPerUnitContainerNonType value1 = new ManifestWSRequestDataPerUnitContainerNonType(_value1);
    public static final ManifestWSRequestDataPerUnitContainerNonType value2 = new ManifestWSRequestDataPerUnitContainerNonType(_value2);
    public static final ManifestWSRequestDataPerUnitContainerNonType value3 = new ManifestWSRequestDataPerUnitContainerNonType(_value3);
    public static final ManifestWSRequestDataPerUnitContainerNonType value4 = new ManifestWSRequestDataPerUnitContainerNonType(_value4);
    public static final ManifestWSRequestDataPerUnitContainerNonType value5 = new ManifestWSRequestDataPerUnitContainerNonType(_value5);
    public static final ManifestWSRequestDataPerUnitContainerNonType value6 = new ManifestWSRequestDataPerUnitContainerNonType(_value6);
    public static final ManifestWSRequestDataPerUnitContainerNonType value7 = new ManifestWSRequestDataPerUnitContainerNonType(_value7);
    public static final ManifestWSRequestDataPerUnitContainerNonType value8 = new ManifestWSRequestDataPerUnitContainerNonType(_value8);
    public static final ManifestWSRequestDataPerUnitContainerNonType value9 = new ManifestWSRequestDataPerUnitContainerNonType(_value9);
    public static final ManifestWSRequestDataPerUnitContainerNonType value10 = new ManifestWSRequestDataPerUnitContainerNonType(_value10);
    public static final ManifestWSRequestDataPerUnitContainerNonType value11 = new ManifestWSRequestDataPerUnitContainerNonType(_value11);
    public static final ManifestWSRequestDataPerUnitContainerNonType value12 = new ManifestWSRequestDataPerUnitContainerNonType(_value12);
    public static final ManifestWSRequestDataPerUnitContainerNonType value13 = new ManifestWSRequestDataPerUnitContainerNonType(_value13);
    public java.lang.String getValue() { return _value_;}
    public static ManifestWSRequestDataPerUnitContainerNonType fromValue(java.lang.String value)
          throws java.lang.IllegalArgumentException {
        ManifestWSRequestDataPerUnitContainerNonType enumeration = (ManifestWSRequestDataPerUnitContainerNonType)
            _table_.get(value);
        if (enumeration==null) throw new java.lang.IllegalArgumentException();
        return enumeration;
    }
    public static ManifestWSRequestDataPerUnitContainerNonType fromString(java.lang.String value)
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
        new org.apache.axis.description.TypeDesc(ManifestWSRequestDataPerUnitContainerNonType.class);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "ManifestWSRequestDataPerUnitContainerNonType"));
    }
    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

}
