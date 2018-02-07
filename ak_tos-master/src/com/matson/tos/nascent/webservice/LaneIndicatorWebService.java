/**
 * LaneIndicatorWebService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.matson.tos.nascent.webservice;

import com.matson.tos.nascent.webservice.LaneIndicatorWebServiceSoap_PortType;

public interface LaneIndicatorWebService extends javax.xml.rpc.Service {
    public java.lang.String getLaneIndicatorWebServiceSoapAddress();

    public LaneIndicatorWebServiceSoap_PortType getLaneIndicatorWebServiceSoap() throws javax.xml.rpc.ServiceException;

    public LaneIndicatorWebServiceSoap_PortType getLaneIndicatorWebServiceSoap(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
