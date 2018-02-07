/**
 * LaneIndicatorWebServiceSoap_PortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.matson.tos.nascent.webservice;

public interface LaneIndicatorWebServiceSoap_PortType extends java.rmi.Remote {
    public boolean controlLaneIndicator(java.lang.String lane, java.lang.String lampIndicator, boolean state) throws java.rmi.RemoteException;
}
