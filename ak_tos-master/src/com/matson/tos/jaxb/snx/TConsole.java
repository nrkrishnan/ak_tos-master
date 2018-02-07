//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.5-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.08.11 at 01:33:48 PM GMT-10:00 
//


package com.matson.tos.jaxb.snx;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Console
 * 
 * <p>Java class for tConsole complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tConsole">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="gate-lane-selected" type="{http://www.navis.com/argo}tGateLaneSelected" minOccurs="0"/>
 *         &lt;element name="console-printers" type="{http://www.navis.com/argo}tConsolePrinters" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="hardware-id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="document-type-id" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="hardware-location" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="mac-address" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="external-console-id" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tConsole", propOrder = {
    "gateLaneSelected",
    "consolePrinters"
})
public class TConsole {

    @XmlElement(name = "gate-lane-selected")
    protected TGateLaneSelected gateLaneSelected;
    @XmlElement(name = "console-printers")
    protected TConsolePrinters consolePrinters;
    @XmlAttribute(name = "hardware-id", required = true)
    protected String hardwareId;
    @XmlAttribute(name = "document-type-id")
    protected String documentTypeId;
    @XmlAttribute(name = "hardware-location")
    protected String hardwareLocation;
    @XmlAttribute(name = "mac-address")
    protected String macAddress;
    @XmlAttribute(name = "external-console-id")
    protected String externalConsoleId;

    /**
     * Gets the value of the gateLaneSelected property.
     * 
     * @return
     *     possible object is
     *     {@link TGateLaneSelected }
     *     
     */
    public TGateLaneSelected getGateLaneSelected() {
        return gateLaneSelected;
    }

    /**
     * Sets the value of the gateLaneSelected property.
     * 
     * @param value
     *     allowed object is
     *     {@link TGateLaneSelected }
     *     
     */
    public void setGateLaneSelected(TGateLaneSelected value) {
        this.gateLaneSelected = value;
    }

    /**
     * Gets the value of the consolePrinters property.
     * 
     * @return
     *     possible object is
     *     {@link TConsolePrinters }
     *     
     */
    public TConsolePrinters getConsolePrinters() {
        return consolePrinters;
    }

    /**
     * Sets the value of the consolePrinters property.
     * 
     * @param value
     *     allowed object is
     *     {@link TConsolePrinters }
     *     
     */
    public void setConsolePrinters(TConsolePrinters value) {
        this.consolePrinters = value;
    }

    /**
     * Gets the value of the hardwareId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHardwareId() {
        return hardwareId;
    }

    /**
     * Sets the value of the hardwareId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHardwareId(String value) {
        this.hardwareId = value;
    }

    /**
     * Gets the value of the documentTypeId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDocumentTypeId() {
        return documentTypeId;
    }

    /**
     * Sets the value of the documentTypeId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDocumentTypeId(String value) {
        this.documentTypeId = value;
    }

    /**
     * Gets the value of the hardwareLocation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHardwareLocation() {
        return hardwareLocation;
    }

    /**
     * Sets the value of the hardwareLocation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHardwareLocation(String value) {
        this.hardwareLocation = value;
    }

    /**
     * Gets the value of the macAddress property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMacAddress() {
        return macAddress;
    }

    /**
     * Sets the value of the macAddress property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMacAddress(String value) {
        this.macAddress = value;
    }

    /**
     * Gets the value of the externalConsoleId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExternalConsoleId() {
        return externalConsoleId;
    }

    /**
     * Sets the value of the externalConsoleId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExternalConsoleId(String value) {
        this.externalConsoleId = value;
    }

}