/**
 * HawaiiHarbors.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.tempuri.ManifestWSRequest_xsd;

public class HawaiiHarbors implements java.io.Serializable {
    public HawaiiHarbors() {
		super();
		// TODO Auto-generated constructor stub
	}

	private java.lang.String _value_;
    private static java.util.HashMap _table_ = new java.util.HashMap();

    // Constructor
    protected HawaiiHarbors(java.lang.String value) {
        _value_ = value;
        _table_.put(_value_,this);
    }

    public static final java.lang.String _PIO = "PIO";
    public static final java.lang.String _MUA = "MUA";
    public static final java.lang.String _NAW = "NAW";
    public static final java.lang.String _KWE = "KWE";
    public static final java.lang.String _HIL = "HIL";
    public static final java.lang.String _GUM = "GUM";
    public static final HawaiiHarbors PIO = new HawaiiHarbors(_PIO);
    public static final HawaiiHarbors MUA = new HawaiiHarbors(_MUA);
    public static final HawaiiHarbors NAW = new HawaiiHarbors(_NAW);
    public static final HawaiiHarbors KWE = new HawaiiHarbors(_KWE);
    public static final HawaiiHarbors HIL = new HawaiiHarbors(_HIL);
    public static final HawaiiHarbors GUM = new HawaiiHarbors(_GUM);
    public java.lang.String getValue() { return _value_;}
    public static HawaiiHarbors fromValue(java.lang.String value)
          throws java.lang.IllegalArgumentException {
        HawaiiHarbors enumeration = (HawaiiHarbors)
            _table_.get(value);
        if (enumeration==null) throw new java.lang.IllegalArgumentException();
        return enumeration;
    }
    public static HawaiiHarbors fromString(java.lang.String value)
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
        new org.apache.axis.description.TypeDesc(HawaiiHarbors.class);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "HawaiiHarbors"));
    }
    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

}
