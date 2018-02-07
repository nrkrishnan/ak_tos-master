/**
 * ManifestWSRequestHeader.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.tempuri.ManifestWSRequest_xsd;

public class ManifestWSRequestHeader  implements java.io.Serializable {
    private org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestHeaderTransporter transporter;

    private org.apache.axis.types.NormalizedString vesselName;

    private org.apache.axis.types.NormalizedString voyage;

    private org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestHeaderPortOfLoading portOfLoading;

    private org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestHeaderPortOfDischarge portOfDischarge;

    private java.util.Calendar ATD;

    private java.util.Calendar ETA;

    private org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestHeaderManifestType manifestType;

    private java.util.Calendar transmitDateTime;

    private int transmitCount;

    public ManifestWSRequestHeader() {
    }

    public ManifestWSRequestHeader(
           org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestHeaderTransporter transporter,
           org.apache.axis.types.NormalizedString vesselName,
           org.apache.axis.types.NormalizedString voyage,
           org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestHeaderPortOfLoading portOfLoading,
           org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestHeaderPortOfDischarge portOfDischarge,
           java.util.Calendar ATD,
           java.util.Calendar ETA,
           org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestHeaderManifestType manifestType,
           java.util.Calendar transmitDateTime,
           int transmitCount) {
           this.transporter = transporter;
           this.vesselName = vesselName;
           this.voyage = voyage;
           this.portOfLoading = portOfLoading;
           this.portOfDischarge = portOfDischarge;
           this.ATD = ATD;
           this.ETA = ETA;
           this.manifestType = manifestType;
           this.transmitDateTime = transmitDateTime;
           this.transmitCount = transmitCount;
    }


    /**
     * Gets the transporter value for this ManifestWSRequestHeader.
     * 
     * @return transporter
     */
    public org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestHeaderTransporter getTransporter() {
        return transporter;
    }


    /**
     * Sets the transporter value for this ManifestWSRequestHeader.
     * 
     * @param transporter
     */
    public void setTransporter(org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestHeaderTransporter transporter) {
        this.transporter = transporter;
    }


    /**
     * Gets the vesselName value for this ManifestWSRequestHeader.
     * 
     * @return vesselName
     */
    public org.apache.axis.types.NormalizedString getVesselName() {
        return vesselName;
    }


    /**
     * Sets the vesselName value for this ManifestWSRequestHeader.
     * 
     * @param vesselName
     */
    public void setVesselName(org.apache.axis.types.NormalizedString vesselName) {
        this.vesselName = vesselName;
    }


    /**
     * Gets the voyage value for this ManifestWSRequestHeader.
     * 
     * @return voyage
     */
    public org.apache.axis.types.NormalizedString getVoyage() {
        return voyage;
    }


    /**
     * Sets the voyage value for this ManifestWSRequestHeader.
     * 
     * @param voyage
     */
    public void setVoyage(org.apache.axis.types.NormalizedString voyage) {
        this.voyage = voyage;
    }


    /**
     * Gets the portOfLoading value for this ManifestWSRequestHeader.
     * 
     * @return portOfLoading
     */
    public org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestHeaderPortOfLoading getPortOfLoading() {
        return portOfLoading;
    }


    /**
     * Sets the portOfLoading value for this ManifestWSRequestHeader.
     * 
     * @param portOfLoading
     */
    public void setPortOfLoading(org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestHeaderPortOfLoading portOfLoading) {
        this.portOfLoading = portOfLoading;
    }


    /**
     * Gets the portOfDischarge value for this ManifestWSRequestHeader.
     * 
     * @return portOfDischarge
     */
    public org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestHeaderPortOfDischarge getPortOfDischarge() {
        return portOfDischarge;
    }


    /**
     * Sets the portOfDischarge value for this ManifestWSRequestHeader.
     * 
     * @param portOfDischarge
     */
    public void setPortOfDischarge(org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestHeaderPortOfDischarge portOfDischarge) {
        this.portOfDischarge = portOfDischarge;
    }


    /**
     * Gets the ATD value for this ManifestWSRequestHeader.
     * 
     * @return ATD
     */
    public java.util.Calendar getATD() {
        return ATD;
    }


    /**
     * Sets the ATD value for this ManifestWSRequestHeader.
     * 
     * @param ATD
     */
    public void setATD(java.util.Calendar ATD) {
        this.ATD = ATD;
    }


    /**
     * Gets the ETA value for this ManifestWSRequestHeader.
     * 
     * @return ETA
     */
    public java.util.Calendar getETA() {
        return ETA;
    }


    /**
     * Sets the ETA value for this ManifestWSRequestHeader.
     * 
     * @param ETA
     */
    public void setETA(java.util.Calendar ETA) {
        this.ETA = ETA;
    }


    /**
     * Gets the manifestType value for this ManifestWSRequestHeader.
     * 
     * @return manifestType
     */
    public org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestHeaderManifestType getManifestType() {
        return manifestType;
    }


    /**
     * Sets the manifestType value for this ManifestWSRequestHeader.
     * 
     * @param manifestType
     */
    public void setManifestType(org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestHeaderManifestType manifestType) {
        this.manifestType = manifestType;
    }


    /**
     * Gets the transmitDateTime value for this ManifestWSRequestHeader.
     * 
     * @return transmitDateTime
     */
    public java.util.Calendar getTransmitDateTime() {
        return transmitDateTime;
    }


    /**
     * Sets the transmitDateTime value for this ManifestWSRequestHeader.
     * 
     * @param transmitDateTime
     */
    public void setTransmitDateTime(java.util.Calendar transmitDateTime) {
        this.transmitDateTime = transmitDateTime;
    }


    /**
     * Gets the transmitCount value for this ManifestWSRequestHeader.
     * 
     * @return transmitCount
     */
    public int getTransmitCount() {
        return transmitCount;
    }


    /**
     * Sets the transmitCount value for this ManifestWSRequestHeader.
     * 
     * @param transmitCount
     */
    public void setTransmitCount(int transmitCount) {
        this.transmitCount = transmitCount;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ManifestWSRequestHeader)) return false;
        ManifestWSRequestHeader other = (ManifestWSRequestHeader) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.transporter==null && other.getTransporter()==null) || 
             (this.transporter!=null &&
              this.transporter.equals(other.getTransporter()))) &&
            ((this.vesselName==null && other.getVesselName()==null) || 
             (this.vesselName!=null &&
              this.vesselName.equals(other.getVesselName()))) &&
            ((this.voyage==null && other.getVoyage()==null) || 
             (this.voyage!=null &&
              this.voyage.equals(other.getVoyage()))) &&
            ((this.portOfLoading==null && other.getPortOfLoading()==null) || 
             (this.portOfLoading!=null &&
              this.portOfLoading.equals(other.getPortOfLoading()))) &&
            ((this.portOfDischarge==null && other.getPortOfDischarge()==null) || 
             (this.portOfDischarge!=null &&
              this.portOfDischarge.equals(other.getPortOfDischarge()))) &&
            ((this.ATD==null && other.getATD()==null) || 
             (this.ATD!=null &&
              this.ATD.equals(other.getATD()))) &&
            ((this.ETA==null && other.getETA()==null) || 
             (this.ETA!=null &&
              this.ETA.equals(other.getETA()))) &&
            ((this.manifestType==null && other.getManifestType()==null) || 
             (this.manifestType!=null &&
              this.manifestType.equals(other.getManifestType()))) &&
            ((this.transmitDateTime==null && other.getTransmitDateTime()==null) || 
             (this.transmitDateTime!=null &&
              this.transmitDateTime.equals(other.getTransmitDateTime()))) &&
            this.transmitCount == other.getTransmitCount();
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
        if (getTransporter() != null) {
            _hashCode += getTransporter().hashCode();
        }
        if (getVesselName() != null) {
            _hashCode += getVesselName().hashCode();
        }
        if (getVoyage() != null) {
            _hashCode += getVoyage().hashCode();
        }
        if (getPortOfLoading() != null) {
            _hashCode += getPortOfLoading().hashCode();
        }
        if (getPortOfDischarge() != null) {
            _hashCode += getPortOfDischarge().hashCode();
        }
        if (getATD() != null) {
            _hashCode += getATD().hashCode();
        }
        if (getETA() != null) {
            _hashCode += getETA().hashCode();
        }
        if (getManifestType() != null) {
            _hashCode += getManifestType().hashCode();
        }
        if (getTransmitDateTime() != null) {
            _hashCode += getTransmitDateTime().hashCode();
        }
        _hashCode += getTransmitCount();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ManifestWSRequestHeader.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "ManifestWSRequestHeader"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("transporter");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "Transporter"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "ManifestWSRequestHeaderTransporter"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("vesselName");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "VesselName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "normalizedString"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("voyage");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "Voyage"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "normalizedString"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("portOfLoading");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "PortOfLoading"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "ManifestWSRequestHeaderPortOfLoading"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("portOfDischarge");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "PortOfDischarge"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "ManifestWSRequestHeaderPortOfDischarge"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("ATD");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "ATD"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("ETA");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "ETA"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("manifestType");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "ManifestType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "ManifestWSRequestHeaderManifestType"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("transmitDateTime");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "TransmitDateTime"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("transmitCount");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "TransmitCount"));
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

}
