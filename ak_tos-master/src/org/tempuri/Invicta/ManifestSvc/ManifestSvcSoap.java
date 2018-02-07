/**
 * ManifestSvcSoap.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.tempuri.Invicta.ManifestSvc;

public interface ManifestSvcSoap extends java.rmi.Remote {

    /**
     * Saves a manifest with default risk/release actions and validation.
     * If validation fails the service will stop and return an error. No
     * import or default risk assignment will occur. If successful, a success
     * message will be returned.
     */
    public org.tempuri.Invicta.ManifestSvc.ManifestWSResponse save(org.tempuri.ManifestWSRequest_xsd.ManifestWSRequest eManifest) throws java.rmi.RemoteException;

    /**
     * Retrieves all possible ports of origin for a shipping unit.
     * These can be any harbor or airport in the world.
     */
    public org.tempuri.Invicta.ManifestSvc.PortOfOrigin[] getPortsOfOrigin() throws java.rmi.RemoteException;

    /**
     * Returns validation schema for the Save method input parameter.
     */
    public java.lang.String getValidationSchema() throws java.rmi.RemoteException;
}
