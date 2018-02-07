/**
 * VINSightHDOAWebServicePort.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.matson.vinsight.webservice;

public interface VINSightHDOAWebServicePort extends java.rmi.Remote {
    public com.matson.vinsight.webservice.cars.ws.to.VINSightHDOACommodity[] getHDOACommodities(java.lang.String string) throws java.rmi.RemoteException;
    public com.matson.vinsight.webservice.cars.ws.to.VINSightHDOACommodity[] putHDOACommoditiesOnHold(int[] ints) throws java.rmi.RemoteException;
}
