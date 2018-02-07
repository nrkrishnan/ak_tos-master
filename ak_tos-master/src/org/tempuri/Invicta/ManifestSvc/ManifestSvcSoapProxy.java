package org.tempuri.Invicta.ManifestSvc;

public class ManifestSvcSoapProxy implements org.tempuri.Invicta.ManifestSvc.ManifestSvcSoap {
  private String _endpoint = null;
  private org.tempuri.Invicta.ManifestSvc.ManifestSvcSoap manifestSvcSoap = null;
  
  public ManifestSvcSoapProxy() {
    _initManifestSvcSoapProxy();
  }
  
  public ManifestSvcSoapProxy(String endpoint) {
    _endpoint = endpoint;
    _initManifestSvcSoapProxy();
  }
  
  private void _initManifestSvcSoapProxy() {
    try {
      manifestSvcSoap = (new org.tempuri.Invicta.ManifestSvc.ManifestSvcLocator()).getManifestSvcSoap();
      if (manifestSvcSoap != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)manifestSvcSoap)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)manifestSvcSoap)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (manifestSvcSoap != null)
      ((javax.xml.rpc.Stub)manifestSvcSoap)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public org.tempuri.Invicta.ManifestSvc.ManifestSvcSoap getManifestSvcSoap() {
    if (manifestSvcSoap == null)
      _initManifestSvcSoapProxy();
    return manifestSvcSoap;
  }
  
  public org.tempuri.Invicta.ManifestSvc.ManifestWSResponse save(org.tempuri.ManifestWSRequest_xsd.ManifestWSRequest eManifest) throws java.rmi.RemoteException{
    if (manifestSvcSoap == null)
      _initManifestSvcSoapProxy();
    return manifestSvcSoap.save(eManifest);
  }
  
  public org.tempuri.Invicta.ManifestSvc.PortOfOrigin[] getPortsOfOrigin() throws java.rmi.RemoteException{
    if (manifestSvcSoap == null)
      _initManifestSvcSoapProxy();
    return manifestSvcSoap.getPortsOfOrigin();
  }
  
  public java.lang.String getValidationSchema() throws java.rmi.RemoteException{
    if (manifestSvcSoap == null)
      _initManifestSvcSoapProxy();
    return manifestSvcSoap.getValidationSchema();
  }
  
  
}