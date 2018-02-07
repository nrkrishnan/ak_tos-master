/**
 * InterfaceControlLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.matson.tos.nascent.webservice;

public class InterfaceControlLocator extends org.apache.axis.client.Service implements InterfaceControl {

    public InterfaceControlLocator() {
    }


    public InterfaceControlLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public InterfaceControlLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for InterfaceControlSoap
    //private java.lang.String InterfaceControlSoap_address = "http://10.157.32.40/SYNAPSE%20AGS%20Connector%20Web%20Service/interfacecontrol.asmx";
    private java.lang.String InterfaceControlSoap_address =
            com.matson.tos.util.TosRefDataUtil.getValue("NASCENT_WS_URL") + "/SYNAPSE%20AGS%20Connector%20Web%20Service/interfacecontrol.asmx";

    public java.lang.String getInterfaceControlSoapAddress() {
        return InterfaceControlSoap_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String InterfaceControlSoapWSDDServiceName = "InterfaceControlSoap";

    public java.lang.String getInterfaceControlSoapWSDDServiceName() {
        return InterfaceControlSoapWSDDServiceName;
    }

    public void setInterfaceControlSoapWSDDServiceName(java.lang.String name) {
        InterfaceControlSoapWSDDServiceName = name;
    }

    public InterfaceControlSoap_PortType getInterfaceControlSoap() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(InterfaceControlSoap_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getInterfaceControlSoap(endpoint);
    }

    public InterfaceControlSoap_PortType getInterfaceControlSoap(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            InterfaceControlSoap_BindingStub _stub = new InterfaceControlSoap_BindingStub(portAddress, this);
            _stub.setPortName(getInterfaceControlSoapWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setInterfaceControlSoapEndpointAddress(java.lang.String address) {
        InterfaceControlSoap_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (InterfaceControlSoap_PortType.class.isAssignableFrom(serviceEndpointInterface)) {
                InterfaceControlSoap_BindingStub _stub = new InterfaceControlSoap_BindingStub(new java.net.URL(InterfaceControlSoap_address), this);
                _stub.setPortName(getInterfaceControlSoapWSDDServiceName());
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
        if ("InterfaceControlSoap".equals(inputPortName)) {
            return getInterfaceControlSoap();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://tempuri.org/SYNAPSE_AGS_Connector_Web_Service/InterfaceControl", "InterfaceControl");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://tempuri.org/SYNAPSE_AGS_Connector_Web_Service/InterfaceControl", "InterfaceControlSoap"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("InterfaceControlSoap".equals(portName)) {
            setInterfaceControlSoapEndpointAddress(address);
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
