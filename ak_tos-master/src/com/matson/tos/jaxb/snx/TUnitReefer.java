//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.5-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.08.11 at 01:33:48 PM GMT-10:00 
//


package com.matson.tos.jaxb.snx;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for tUnitReefer complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tUnitReefer">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="unit-identity" type="{http://www.navis.com/argo}tUnitIdentity"/>
 *         &lt;element name="reefer" type="{http://www.navis.com/argo}tReeferRequirements"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tUnitReefer", propOrder = {
    "unitIdentity",
    "reefer"
})
public class TUnitReefer {

    @XmlElement(name = "unit-identity", required = true)
    protected TUnitIdentity unitIdentity;
    @XmlElement(required = true)
    protected TReeferRequirements reefer;

    /**
     * Gets the value of the unitIdentity property.
     * 
     * @return
     *     possible object is
     *     {@link TUnitIdentity }
     *     
     */
    public TUnitIdentity getUnitIdentity() {
        return unitIdentity;
    }

    /**
     * Sets the value of the unitIdentity property.
     * 
     * @param value
     *     allowed object is
     *     {@link TUnitIdentity }
     *     
     */
    public void setUnitIdentity(TUnitIdentity value) {
        this.unitIdentity = value;
    }

    /**
     * Gets the value of the reefer property.
     * 
     * @return
     *     possible object is
     *     {@link TReeferRequirements }
     *     
     */
    public TReeferRequirements getReefer() {
        return reefer;
    }

    /**
     * Sets the value of the reefer property.
     * 
     * @param value
     *     allowed object is
     *     {@link TReeferRequirements }
     *     
     */
    public void setReefer(TReeferRequirements value) {
        this.reefer = value;
    }

}
