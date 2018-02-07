/**
 * ArgoService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package com.matson.tos.nascent.webservice;

public interface ArgoService extends javax.xml.rpc.Service {
    public java.lang.String getArgoServicePortAddress();

    public com.matson.tos.nascent.webservice.ArgoServicePort_PortType getArgoServicePort() throws javax.xml.rpc.ServiceException;

    public com.matson.tos.nascent.webservice.ArgoServicePort_PortType getArgoServicePort(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
