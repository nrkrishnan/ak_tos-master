/**
 * ManifestSvcLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.tempuri.Invicta.ManifestSvc;

import com.matson.tos.util.ManifestHelper;

public class ManifestSvcLocator extends org.apache.axis.client.Service implements org.tempuri.Invicta.ManifestSvc.ManifestSvc {

    public ManifestSvcLocator() {
    }


    public ManifestSvcLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public ManifestSvcLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for ManifestSvcSoap
    ManifestHelper manifestHelper = new ManifestHelper();
    private java.lang.String ManifestSvcSoap_address = manifestHelper.getWSURL();

    public java.lang.String getManifestSvcSoapAddress() {
        return ManifestSvcSoap_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String ManifestSvcSoapWSDDServiceName = "ManifestSvcSoap";

    public java.lang.String getManifestSvcSoapWSDDServiceName() {
        return ManifestSvcSoapWSDDServiceName;
    }

    public void setManifestSvcSoapWSDDServiceName(java.lang.String name) {
        ManifestSvcSoapWSDDServiceName = name;
    }

    public org.tempuri.Invicta.ManifestSvc.ManifestSvcSoap getManifestSvcSoap() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(ManifestSvcSoap_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getManifestSvcSoap(endpoint);
    }

    public org.tempuri.Invicta.ManifestSvc.ManifestSvcSoap getManifestSvcSoap(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            org.tempuri.Invicta.ManifestSvc.ManifestSvcSoapStub _stub = new org.tempuri.Invicta.ManifestSvc.ManifestSvcSoapStub(portAddress, this);
            _stub.setPortName(getManifestSvcSoapWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setManifestSvcSoapEndpointAddress(java.lang.String address) {
        ManifestSvcSoap_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (org.tempuri.Invicta.ManifestSvc.ManifestSvcSoap.class.isAssignableFrom(serviceEndpointInterface)) {
                org.tempuri.Invicta.ManifestSvc.ManifestSvcSoapStub _stub = new org.tempuri.Invicta.ManifestSvc.ManifestSvcSoapStub(new java.net.URL(ManifestSvcSoap_address), this);
                _stub.setPortName(getManifestSvcSoapWSDDServiceName());
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
        if ("ManifestSvcSoap".equals(inputPortName)) {
            return getManifestSvcSoap();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://tempuri.org/Invicta/ManifestSvc", "ManifestSvc");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://tempuri.org/Invicta/ManifestSvc", "ManifestSvcSoap"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("ManifestSvcSoap".equals(portName)) {
            setManifestSvcSoapEndpointAddress(address);
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
