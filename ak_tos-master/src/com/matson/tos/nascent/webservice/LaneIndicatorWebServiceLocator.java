/**
 * LaneIndicatorWebServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.matson.tos.nascent.webservice;

import com.matson.tos.nascent.webservice.LaneIndicatorWebServiceSoap_PortType;

public class LaneIndicatorWebServiceLocator extends org.apache.axis.client.Service implements LaneIndicatorWebService {

    public LaneIndicatorWebServiceLocator() {
    }


    public LaneIndicatorWebServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public LaneIndicatorWebServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for LaneIndicatorWebServiceSoap
    //private java.lang.String LaneIndicatorWebServiceSoap_address = "http://10.157.32.40/SYNAPSE%20AGS%20Connector%20Web%20Service/LaneIndicatorWebService.asmx";

    private java.lang.String LaneIndicatorWebServiceSoap_address =
            com.matson.tos.util.TosRefDataUtil.getValue("NASCENT_WS_URL") + "/SYNAPSE%20AGS%20Connector%20Web%20Service/LaneIndicatorWebService.asmx";

    public java.lang.String getLaneIndicatorWebServiceSoapAddress() {
        return LaneIndicatorWebServiceSoap_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String LaneIndicatorWebServiceSoapWSDDServiceName = "LaneIndicatorWebServiceSoap";

    public java.lang.String getLaneIndicatorWebServiceSoapWSDDServiceName() {
        return LaneIndicatorWebServiceSoapWSDDServiceName;
    }

    public void setLaneIndicatorWebServiceSoapWSDDServiceName(java.lang.String name) {
        LaneIndicatorWebServiceSoapWSDDServiceName = name;
    }

    public LaneIndicatorWebServiceSoap_PortType getLaneIndicatorWebServiceSoap() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(LaneIndicatorWebServiceSoap_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getLaneIndicatorWebServiceSoap(endpoint);
    }

    public LaneIndicatorWebServiceSoap_PortType getLaneIndicatorWebServiceSoap(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            LaneIndicatorWebServiceSoap_BindingStub _stub = new LaneIndicatorWebServiceSoap_BindingStub(portAddress, this);
            _stub.setPortName(getLaneIndicatorWebServiceSoapWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setLaneIndicatorWebServiceSoapEndpointAddress(java.lang.String address) {
        LaneIndicatorWebServiceSoap_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (LaneIndicatorWebServiceSoap_PortType.class.isAssignableFrom(serviceEndpointInterface)) {
                LaneIndicatorWebServiceSoap_BindingStub _stub = new LaneIndicatorWebServiceSoap_BindingStub(new java.net.URL(LaneIndicatorWebServiceSoap_address), this);
                _stub.setPortName(getLaneIndicatorWebServiceSoapWSDDServiceName());
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
        if ("LaneIndicatorWebServiceSoap".equals(inputPortName)) {
            return getLaneIndicatorWebServiceSoap();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://tempuri.org/SYNAPSE_AGS_Connector_Web_Service/LaneIndicatorWebService", "LaneIndicatorWebService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://tempuri.org/SYNAPSE_AGS_Connector_Web_Service/LaneIndicatorWebService", "LaneIndicatorWebServiceSoap"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("LaneIndicatorWebServiceSoap".equals(portName)) {
            setLaneIndicatorWebServiceSoapEndpointAddress(address);
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
