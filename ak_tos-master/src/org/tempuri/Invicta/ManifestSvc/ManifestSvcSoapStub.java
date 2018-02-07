/**
 * ManifestSvcSoapStub.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.tempuri.Invicta.ManifestSvc;

import org.apache.log4j.Logger;
import com.matson.tos.util.EmailSender;
import com.matson.tos.util.TosRefDataUtil;

public class ManifestSvcSoapStub extends org.apache.axis.client.Stub implements org.tempuri.Invicta.ManifestSvc.ManifestSvcSoap {
    private java.util.Vector cachedSerClasses = new java.util.Vector();
    private java.util.Vector cachedSerQNames = new java.util.Vector();
    private java.util.Vector cachedSerFactories = new java.util.Vector();
    private java.util.Vector cachedDeserFactories = new java.util.Vector();
    
    private static Logger logger = Logger.getLogger(ManifestSvcSoapStub.class);
    
    static org.apache.axis.description.OperationDesc [] _operations;

    static {
        _operations = new org.apache.axis.description.OperationDesc[3];
        _initOperationDesc1();
    }

    private static void _initOperationDesc1(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("Save");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "eManifest"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "ManifestWSRequest"), org.tempuri.ManifestWSRequest_xsd.ManifestWSRequest.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://tempuri.org/Invicta/ManifestSvc", "ManifestWSResponse"));
        oper.setReturnClass(org.tempuri.Invicta.ManifestSvc.ManifestWSResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://tempuri.org/Invicta/ManifestSvc", "SaveResult"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[0] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("GetPortsOfOrigin");
        oper.setReturnType(new javax.xml.namespace.QName("http://tempuri.org/Invicta/ManifestSvc", "ArrayOfPortOfOrigin"));
        oper.setReturnClass(org.tempuri.Invicta.ManifestSvc.PortOfOrigin[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://tempuri.org/Invicta/ManifestSvc", "GetPortsOfOriginResult"));
        param = oper.getReturnParamDesc();
        param.setItemQName(new javax.xml.namespace.QName("http://tempuri.org/Invicta/ManifestSvc", "PortOfOrigin"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[1] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("GetValidationSchema");
        oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(java.lang.String.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://tempuri.org/Invicta/ManifestSvc", "GetValidationSchemaResult"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[2] = oper;

    }

    public ManifestSvcSoapStub() throws org.apache.axis.AxisFault {
         this(null);
    }

    public ManifestSvcSoapStub(java.net.URL endpointURL, javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
         this(service);
         super.cachedEndpoint = endpointURL;
    }

    public ManifestSvcSoapStub(javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
        if (service == null) {
            super.service = new org.apache.axis.client.Service();
        } else {
            super.service = service;
        }
        ((org.apache.axis.client.Service)super.service).setTypeMappingVersion("1.2");
            java.lang.Class cls;
            javax.xml.namespace.QName qName;
            javax.xml.namespace.QName qName2;
            java.lang.Class beansf = org.apache.axis.encoding.ser.BeanSerializerFactory.class;
            java.lang.Class beandf = org.apache.axis.encoding.ser.BeanDeserializerFactory.class;
            java.lang.Class enumsf = org.apache.axis.encoding.ser.EnumSerializerFactory.class;
            java.lang.Class enumdf = org.apache.axis.encoding.ser.EnumDeserializerFactory.class;
            java.lang.Class arraysf = org.apache.axis.encoding.ser.ArraySerializerFactory.class;
            java.lang.Class arraydf = org.apache.axis.encoding.ser.ArrayDeserializerFactory.class;
            java.lang.Class simplesf = org.apache.axis.encoding.ser.SimpleSerializerFactory.class;
            java.lang.Class simpledf = org.apache.axis.encoding.ser.SimpleDeserializerFactory.class;
            java.lang.Class simplelistsf = org.apache.axis.encoding.ser.SimpleListSerializerFactory.class;
            java.lang.Class simplelistdf = org.apache.axis.encoding.ser.SimpleListDeserializerFactory.class;
            qName = new javax.xml.namespace.QName("http://tempuri.org/Invicta/ManifestSvc", "Acknowledgment");
            cachedSerQNames.add(qName);
            cls = org.tempuri.Invicta.ManifestSvc.Acknowledgment.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://tempuri.org/Invicta/ManifestSvc", "ArrayOfPortOfOrigin");
            cachedSerQNames.add(qName);
            cls = org.tempuri.Invicta.ManifestSvc.PortOfOrigin[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://tempuri.org/Invicta/ManifestSvc", "PortOfOrigin");
            qName2 = new javax.xml.namespace.QName("http://tempuri.org/Invicta/ManifestSvc", "PortOfOrigin");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://tempuri.org/Invicta/ManifestSvc", "ManifestWSResponse");
            cachedSerQNames.add(qName);
            cls = org.tempuri.Invicta.ManifestSvc.ManifestWSResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://tempuri.org/Invicta/ManifestSvc", "PortOfOrigin");
            cachedSerQNames.add(qName);
            cls = org.tempuri.Invicta.ManifestSvc.PortOfOrigin.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://tempuri.org/Invicta/ManifestSvc", "statusEnum");
            cachedSerQNames.add(qName);
            cls = org.tempuri.Invicta.ManifestSvc.StatusEnum.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "ArrayOfManifestWSRequestDataPerUnit");
            cachedSerQNames.add(qName);
            cls = org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestDataPerUnit[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "ManifestWSRequestDataPerUnit");
            qName2 = new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "ManifestWSRequestDataPerUnit");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "HawaiiHarbors");
            cachedSerQNames.add(qName);
            cls = org.tempuri.ManifestWSRequest_xsd.HawaiiHarbors.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "ManifestWSRequest");
            cachedSerQNames.add(qName);
            cls = org.tempuri.ManifestWSRequest_xsd.ManifestWSRequest.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "ManifestWSRequestDataPerUnit");
            cachedSerQNames.add(qName);
            cls = org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestDataPerUnit.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "ManifestWSRequestDataPerUnitContainerNonType");
            cachedSerQNames.add(qName);
            cls = org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestDataPerUnitContainerNonType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "ManifestWSRequestDataPerUnitDischargeService");
            cachedSerQNames.add(qName);
            cls = org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestDataPerUnitDischargeService.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "ManifestWSRequestDataPerUnitHAZ");
            cachedSerQNames.add(qName);
            cls = org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestDataPerUnitHAZ.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "ManifestWSRequestDataPerUnitHDOAHold");
            cachedSerQNames.add(qName);
            cls = org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestDataPerUnitHDOAHold.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "ManifestWSRequestDataPerUnitTempSpecification");
            cachedSerQNames.add(qName);
            cls = org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestDataPerUnitTempSpecification.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "ManifestWSRequestDataPerUnitTOS");
            cachedSerQNames.add(qName);
            cls = org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestDataPerUnitTOS.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "ManifestWSRequestDataPerUnitTransactionType");
            cachedSerQNames.add(qName);
            cls = org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestDataPerUnitTransactionType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "ManifestWSRequestDataPerUnitWeightSpecification");
            cachedSerQNames.add(qName);
            cls = org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestDataPerUnitWeightSpecification.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "ManifestWSRequestHeader");
            cachedSerQNames.add(qName);
            cls = org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestHeader.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "ManifestWSRequestHeaderManifestType");
            cachedSerQNames.add(qName);
            cls = org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestHeaderManifestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "ManifestWSRequestHeaderPortOfDischarge");
            cachedSerQNames.add(qName);
            cls = org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestHeaderPortOfDischarge.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "ManifestWSRequestHeaderPortOfLoading");
            cachedSerQNames.add(qName);
            cls = org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestHeaderPortOfLoading.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://tempuri.org/ManifestWSRequest.xsd", "ManifestWSRequestHeaderTransporter");
            cachedSerQNames.add(qName);
            cls = org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestHeaderTransporter.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

    }

    protected org.apache.axis.client.Call createCall() throws java.rmi.RemoteException {
        try {
            org.apache.axis.client.Call _call = super._createCall();

            if (super.maintainSessionSet) {
                _call.setMaintainSession(super.maintainSession);
            }
            if (super.cachedUsername != null) {
                _call.setUsername(super.cachedUsername);
            }
            if (super.cachedPassword != null) {
                _call.setPassword(super.cachedPassword);
            }
            if (super.cachedEndpoint != null) {
                _call.setTargetEndpointAddress(super.cachedEndpoint);
            }
            if (super.cachedTimeout != null) {
                _call.setTimeout(super.cachedTimeout);
            }
            if (super.cachedPortName != null) {
                _call.setPortName(super.cachedPortName);
            }
            java.util.Enumeration keys = super.cachedProperties.keys();
            while (keys.hasMoreElements()) {
                java.lang.String key = (java.lang.String) keys.nextElement();
                _call.setProperty(key, super.cachedProperties.get(key));
            }
            // All the type mapping information is registered
            // when the first call is made.
            // The type mapping information is actually registered in
            // the TypeMappingRegistry of the service, which
            // is the reason why registration is only needed for the first call.
            synchronized (this) {
                if (firstCall()) {
                    // must set encoding style before registering serializers
                    _call.setEncodingStyle(null);
                    for (int i = 0; i < cachedSerFactories.size(); ++i) {
                        java.lang.Class cls = (java.lang.Class) cachedSerClasses.get(i);
                        javax.xml.namespace.QName qName =
                                (javax.xml.namespace.QName) cachedSerQNames.get(i);
                        java.lang.Object x = cachedSerFactories.get(i);
                        if (x instanceof Class) {
                            java.lang.Class sf = (java.lang.Class)
                                 cachedSerFactories.get(i);
                            java.lang.Class df = (java.lang.Class)
                                 cachedDeserFactories.get(i);
                            _call.registerTypeMapping(cls, qName, sf, df, false);
                        }
                        else if (x instanceof javax.xml.rpc.encoding.SerializerFactory) {
                            org.apache.axis.encoding.SerializerFactory sf = (org.apache.axis.encoding.SerializerFactory)
                                 cachedSerFactories.get(i);
                            org.apache.axis.encoding.DeserializerFactory df = (org.apache.axis.encoding.DeserializerFactory)
                                 cachedDeserFactories.get(i);
                            _call.registerTypeMapping(cls, qName, sf, df, false);
                        }
                    }
                }
            }
            return _call;
        }
        catch (java.lang.Throwable _t) {
            throw new org.apache.axis.AxisFault("Failure trying to get the Call object", _t);
        }
    }

    public org.tempuri.Invicta.ManifestSvc.ManifestWSResponse save(org.tempuri.ManifestWSRequest_xsd.ManifestWSRequest eManifest) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[0]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://tempuri.org/Invicta/ManifestSvc/Save");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://tempuri.org/Invicta/ManifestSvc", "Save"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {eManifest});
 
          String requestXml = _call.getMessageContext().getRequestMessage().getSOAPPartAsString(); 
          logger.debug("Email requestXml out");
          String emailto = TosRefDataUtil.getValue( "MANIFEST_REQ_EMAIL");
          String emailfrom = TosRefDataUtil.getValue( "SUPPORT_EMAIL");
		  String mailhost = TosRefDataUtil.getValue( "MAIL_HOST");
		  EmailSender.mailAttachment(emailto,emailfrom,mailhost,"ManifestSoapRequest.xml",requestXml,"Manifest Soap Request Xml","Manifest Soap Request Xml");
 
        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (org.tempuri.Invicta.ManifestSvc.ManifestWSResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (org.tempuri.Invicta.ManifestSvc.ManifestWSResponse) org.apache.axis.utils.JavaUtils.convert(_resp, org.tempuri.Invicta.ManifestSvc.ManifestWSResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public org.tempuri.Invicta.ManifestSvc.PortOfOrigin[] getPortsOfOrigin() throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[1]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://tempuri.org/Invicta/ManifestSvc/GetPortsOfOrigin");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://tempuri.org/Invicta/ManifestSvc", "GetPortsOfOrigin"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (org.tempuri.Invicta.ManifestSvc.PortOfOrigin[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (org.tempuri.Invicta.ManifestSvc.PortOfOrigin[]) org.apache.axis.utils.JavaUtils.convert(_resp, org.tempuri.Invicta.ManifestSvc.PortOfOrigin[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public java.lang.String getValidationSchema() throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[2]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://tempuri.org/Invicta/ManifestSvc/GetValidationSchema");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://tempuri.org/Invicta/ManifestSvc", "GetValidationSchema"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (java.lang.String) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String) org.apache.axis.utils.JavaUtils.convert(_resp, java.lang.String.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

}
