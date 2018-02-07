/**
 * ResponseType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package com.matson.tos.nascent.webservice;

public class ResponseType  implements java.io.Serializable {
    private java.lang.String status;
    private java.lang.String statusDescription;
    private com.matson.tos.nascent.webservice.MessageCollectorType messageCollector;
    private com.matson.tos.nascent.webservice.QueryResultType[] queryResults;

    public ResponseType() {
    }

    public ResponseType(
           java.lang.String status,
           java.lang.String statusDescription,
           com.matson.tos.nascent.webservice.MessageCollectorType messageCollector,
           com.matson.tos.nascent.webservice.QueryResultType[] queryResults) {
           this.status = status;
           this.statusDescription = statusDescription;
           this.messageCollector = messageCollector;
           this.queryResults = queryResults;
    }


    /**
     * Gets the status value for this ResponseType.
     * 
     * @return status
     */
    public java.lang.String getStatus() {
        return status;
    }


    /**
     * Sets the status value for this ResponseType.
     * 
     * @param status
     */
    public void setStatus(java.lang.String status) {
        this.status = status;
    }


    /**
     * Gets the statusDescription value for this ResponseType.
     * 
     * @return statusDescription
     */
    public java.lang.String getStatusDescription() {
        return statusDescription;
    }


    /**
     * Sets the statusDescription value for this ResponseType.
     * 
     * @param statusDescription
     */
    public void setStatusDescription(java.lang.String statusDescription) {
        this.statusDescription = statusDescription;
    }


    /**
     * Gets the messageCollector value for this ResponseType.
     * 
     * @return messageCollector
     */
    public com.matson.tos.nascent.webservice.MessageCollectorType getMessageCollector() {
        return messageCollector;
    }


    /**
     * Sets the messageCollector value for this ResponseType.
     * 
     * @param messageCollector
     */
    public void setMessageCollector(com.matson.tos.nascent.webservice.MessageCollectorType messageCollector) {
        this.messageCollector = messageCollector;
    }


    /**
     * Gets the queryResults value for this ResponseType.
     * 
     * @return queryResults
     */
    public com.matson.tos.nascent.webservice.QueryResultType[] getQueryResults() {
        return queryResults;
    }


    /**
     * Sets the queryResults value for this ResponseType.
     * 
     * @param queryResults
     */
    public void setQueryResults(com.matson.tos.nascent.webservice.QueryResultType[] queryResults) {
        this.queryResults = queryResults;
    }

    public com.matson.tos.nascent.webservice.QueryResultType getQueryResults(int i) {
        return this.queryResults[i];
    }

    public void setQueryResults(int i, com.matson.tos.nascent.webservice.QueryResultType _value) {
        this.queryResults[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ResponseType)) return false;
        ResponseType other = (ResponseType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.status==null && other.getStatus()==null) || 
             (this.status!=null &&
              this.status.equals(other.getStatus()))) &&
            ((this.statusDescription==null && other.getStatusDescription()==null) || 
             (this.statusDescription!=null &&
              this.statusDescription.equals(other.getStatusDescription()))) &&
            ((this.messageCollector==null && other.getMessageCollector()==null) || 
             (this.messageCollector!=null &&
              this.messageCollector.equals(other.getMessageCollector()))) &&
            ((this.queryResults==null && other.getQueryResults()==null) || 
             (this.queryResults!=null &&
              java.util.Arrays.equals(this.queryResults, other.getQueryResults())));
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
        if (getStatus() != null) {
            _hashCode += getStatus().hashCode();
        }
        if (getStatusDescription() != null) {
            _hashCode += getStatusDescription().hashCode();
        }
        if (getMessageCollector() != null) {
            _hashCode += getMessageCollector().hashCode();
        }
        if (getQueryResults() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getQueryResults());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getQueryResults(), i);
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
        new org.apache.axis.description.TypeDesc(ResponseType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://types.webservice.argo.navis.com/v1.0", "ResponseType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("status");
        elemField.setXmlName(new javax.xml.namespace.QName("http://types.webservice.argo.navis.com/v1.0", "Status"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("statusDescription");
        elemField.setXmlName(new javax.xml.namespace.QName("http://types.webservice.argo.navis.com/v1.0", "StatusDescription"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("messageCollector");
        elemField.setXmlName(new javax.xml.namespace.QName("http://types.webservice.argo.navis.com/v1.0", "MessageCollector"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://types.webservice.argo.navis.com/v1.0", "MessageCollectorType"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("queryResults");
        elemField.setXmlName(new javax.xml.namespace.QName("http://types.webservice.argo.navis.com/v1.0", "QueryResults"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://types.webservice.argo.navis.com/v1.0", "QueryResultType"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
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
