/**
 * ArgoServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package com.matson.tos.nascent.webservice;

import org.apache.log4j.Logger;

import com.matson.tos.processor.NascentProcessor;
import com.matson.tos.util.TosRefDataUtil;

public class ArgoServiceLocator extends org.apache.axis.client.Service implements com.matson.tos.nascent.webservice.ArgoService {
	
	private static Logger logger = Logger.getLogger(ArgoServiceLocator.class);

    public ArgoServiceLocator() {
    }


    public ArgoServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public ArgoServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for ArgoServicePort
    //private java.lang.String ArgoServicePort_address = "http://10.102.10.33:9282/apex/services/argoservice";
    public static String ArgoServicePort_address = TosRefDataUtil.getValue("ARGO_SERVICE_URL");
       

    public java.lang.String getArgoServicePortAddress() {
        return ArgoServicePort_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String ArgoServicePortWSDDServiceName = "ArgoServicePort";

    public java.lang.String getArgoServicePortWSDDServiceName() {
        return ArgoServicePortWSDDServiceName;
    }

    public void setArgoServicePortWSDDServiceName(java.lang.String name) {
        ArgoServicePortWSDDServiceName = name;
    }

    public com.matson.tos.nascent.webservice.ArgoServicePort_PortType getArgoServicePort() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(ArgoServicePort_address);
            logger.info("Argo Service URL  " + ArgoServicePort_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getArgoServicePort(endpoint);
    }

    public com.matson.tos.nascent.webservice.ArgoServicePort_PortType getArgoServicePort(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.matson.tos.nascent.webservice.ArgoServiceSoapBindingStub _stub = new com.matson.tos.nascent.webservice.ArgoServiceSoapBindingStub(portAddress, this);
            _stub.setPortName(getArgoServicePortWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setArgoServicePortEndpointAddress(java.lang.String address) {
        ArgoServicePort_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (com.matson.tos.nascent.webservice.ArgoServicePort_PortType.class.isAssignableFrom(serviceEndpointInterface)) {
                com.matson.tos.nascent.webservice.ArgoServiceSoapBindingStub _stub = new com.matson.tos.nascent.webservice.ArgoServiceSoapBindingStub(new java.net.URL(ArgoServicePort_address), this);
                _stub.setPortName(getArgoServicePortWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("ArgoServicePort".equals(inputPortName)) {
            return getArgoServicePort();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://www.navis.com/services/argoservice", "ArgoService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://www.navis.com/services/argoservice", "ArgoServicePort"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("ArgoServicePort".equals(portName)) {
            setArgoServicePortEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
