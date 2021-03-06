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
 * Platforms
 * 
 * <p>Java class for tLowerAndUpperTierRestrictions complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tLowerAndUpperTierRestrictions">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="lower-tier" type="{http://www.navis.com/argo}tLowerTierRestrictions"/>
 *         &lt;element name="upper-tier" type="{http://www.navis.com/argo}tUpperTierRestrictions"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tLowerAndUpperTierRestrictions", propOrder = {
    "lowerTier",
    "upperTier"
})
public class TLowerAndUpperTierRestrictions {

    @XmlElement(name = "lower-tier", required = true)
    protected TLowerTierRestrictions lowerTier;
    @XmlElement(name = "upper-tier", required = true)
    protected TUpperTierRestrictions upperTier;

    /**
     * Gets the value of the lowerTier property.
     * 
     * @return
     *     possible object is
     *     {@link TLowerTierRestrictions }
     *     
     */
    public TLowerTierRestrictions getLowerTier() {
        return lowerTier;
    }

    /**
     * Sets the value of the lowerTier property.
     * 
     * @param value
     *     allowed object is
     *     {@link TLowerTierRestrictions }
     *     
     */
    public void setLowerTier(TLowerTierRestrictions value) {
        this.lowerTier = value;
    }

    /**
     * Gets the value of the upperTier property.
     * 
     * @return
     *     possible object is
     *     {@link TUpperTierRestrictions }
     *     
     */
    public TUpperTierRestrictions getUpperTier() {
        return upperTier;
    }

    /**
     * Sets the value of the upperTier property.
     * 
     * @param value
     *     allowed object is
     *     {@link TUpperTierRestrictions }
     *     
     */
    public void setUpperTier(TUpperTierRestrictions value) {
        this.upperTier = value;
    }

}
