package com.matson.vinsight.webservice;

public class VINSightHDOAWebServicePortProxy implements com.matson.vinsight.webservice.VINSightHDOAWebServicePort {
  private String _endpoint = null;
  private com.matson.vinsight.webservice.VINSightHDOAWebServicePort vINSightHDOAWebServicePort = null;
  
  public VINSightHDOAWebServicePortProxy() {
    _initVINSightHDOAWebServicePortProxy();
  }
  
  public VINSightHDOAWebServicePortProxy(String endpoint) {
    _endpoint = endpoint;
    _initVINSightHDOAWebServicePortProxy();
  }
  
  private void _initVINSightHDOAWebServicePortProxy() {
    try {
      vINSightHDOAWebServicePort = (new com.matson.vinsight.webservice.VINSightHDOAWebServiceLocator()).getVINSightHDOAWebServicePort();
      if (vINSightHDOAWebServicePort != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)vINSightHDOAWebServicePort)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)vINSightHDOAWebServicePort)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (vINSightHDOAWebServicePort != null)
      ((javax.xml.rpc.Stub)vINSightHDOAWebServicePort)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public com.matson.vinsight.webservice.VINSightHDOAWebServicePort getVINSightHDOAWebServicePort() {
    if (vINSightHDOAWebServicePort == null)
      _initVINSightHDOAWebServicePortProxy();
    return vINSightHDOAWebServicePort;
  }
  
  public com.matson.vinsight.webservice.cars.ws.to.VINSightHDOACommodity[] getHDOACommodities(java.lang.String string) throws java.rmi.RemoteException{
    if (vINSightHDOAWebServicePort == null)
      _initVINSightHDOAWebServicePortProxy();
    return vINSightHDOAWebServicePort.getHDOACommodities(string);
  }
  
  public com.matson.vinsight.webservice.cars.ws.to.VINSightHDOACommodity[] putHDOACommoditiesOnHold(int[] ints) throws java.rmi.RemoteException{
    if (vINSightHDOAWebServicePort == null)
      _initVINSightHDOAWebServicePortProxy();
    return vINSightHDOAWebServicePort.putHDOACommoditiesOnHold(ints);
  }
  
  
}