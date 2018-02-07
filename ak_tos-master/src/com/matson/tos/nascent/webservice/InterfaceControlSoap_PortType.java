/**
 * InterfaceControlSoap_PortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.matson.tos.nascent.webservice;

public interface InterfaceControlSoap_PortType extends java.rmi.Remote {
    public boolean workstationConfig(java.lang.String clerkWorkstation, java.lang.String parameter, java.lang.String value) throws java.rmi.RemoteException;
    public boolean laneConnect(java.lang.String clerkWorkstation, java.lang.String interfaceID, java.lang.String saveImagePath, java.lang.String saveImageFile) throws java.rmi.RemoteException;
    public boolean laneDisconnect(java.lang.String clerkWorkstation) throws java.rmi.RemoteException;
    public boolean simpleQueueMark(java.lang.String interfaceID, boolean connected, java.lang.String consoleID) throws java.rmi.RemoteException;
}
