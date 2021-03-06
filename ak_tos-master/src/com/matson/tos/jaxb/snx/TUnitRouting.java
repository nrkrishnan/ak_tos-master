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
 * <p>Java class for tUnitRouting complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tUnitRouting">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="unit-identity" type="{http://www.navis.com/argo}tUnitIdentity"/>
 *         &lt;element name="routing" type="{http://www.navis.com/argo}tRouting"/>
 *         &lt;element name="unit-etc" type="{http://www.navis.com/argo}tUnitEtc" minOccurs="0"/>
 *         &lt;element name="unit-flex" type="{http://www.navis.com/argo}tUnitFlexFields" minOccurs="0"/>
 *         &lt;element name="ufv-flex" type="{http://www.navis.com/argo}tUfvFlexFields" minOccurs="0"/>
 *         &lt;element name="event" type="{http://www.navis.com/argo}tEvent" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="category" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="facility" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tUnitRouting", propOrder = {
    "unitIdentity",
    "routing",
    "unitEtc",
    "unitFlex",
    "ufvFlex",
    "event"
})
public class TUnitRouting {

    @XmlElement(name = "unit-identity", required = true)
    protected TUnitIdentity unitIdentity;
    @XmlElement(required = true)
    protected TRouting routing;
    @XmlElement(name = "unit-etc")
    protected TUnitEtc unitEtc;
    @XmlElement(name = "unit-flex")
    protected TUnitFlexFields unitFlex;
    @XmlElement(name = "ufv-flex")
    protected TUfvFlexFields ufvFlex;
    protected TEvent event;
    @XmlAttribute
    protected String category;
    @XmlAttribute
    protected String facility;

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
     * Gets the value of the routing property.
     * 
     * @return
     *     possible object is
     *     {@link TRouting }
     *     
     */
    public TRouting getRouting() {
        return routing;
    }

    /**
     * Sets the value of the routing property.
     * 
     * @param value
     *     allowed object is
     *     {@link TRouting }
     *     
     */
    public void setRouting(TRouting value) {
        this.routing = value;
    }

    /**
     * Gets the value of the unitEtc property.
     * 
     * @return
     *     possible object is
     *     {@link TUnitEtc }
     *     
     */
    public TUnitEtc getUnitEtc() {
        return unitEtc;
    }

    /**
     * Sets the value of the unitEtc property.
     * 
     * @param value
     *     allowed object is
     *     {@link TUnitEtc }
     *     
     */
    public void setUnitEtc(TUnitEtc value) {
        this.unitEtc = value;
    }

    /**
     * Gets the value of the unitFlex property.
     * 
     * @return
     *     possible object is
     *     {@link TUnitFlexFields }
     *     
     */
    public TUnitFlexFields getUnitFlex() {
        return unitFlex;
    }

    /**
     * Sets the value of the unitFlex property.
     * 
     * @param value
     *     allowed object is
     *     {@link TUnitFlexFields }
     *     
     */
    public void setUnitFlex(TUnitFlexFields value) {
        this.unitFlex = value;
    }

    /**
     * Gets the value of the ufvFlex property.
     * 
     * @return
     *     possible object is
     *     {@link TUfvFlexFields }
     *     
     */
    public TUfvFlexFields getUfvFlex() {
        return ufvFlex;
    }

    /**
     * Sets the value of the ufvFlex property.
     * 
     * @param value
     *     allowed object is
     *     {@link TUfvFlexFields }
     *     
     */
    public void setUfvFlex(TUfvFlexFields value) {
        this.ufvFlex = value;
    }

    /**
     * Gets the value of the event property.
     * 
     * @return
     *     possible object is
     *     {@link TEvent }
     *     
     */
    public TEvent getEvent() {
        return event;
    }

    /**
     * Sets the value of the event property.
     * 
     * @param value
     *     allowed object is
     *     {@link TEvent }
     *     
     */
    public void setEvent(TEvent value) {
        this.event = value;
    }

    /**
     * Gets the value of the category property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCategory() {
        return category;
    }

    /**
     * Sets the value of the category property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCategory(String value) {
        this.category = value;
    }

    /**
     * Gets the value of the facility property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFacility() {
        return facility;
    }

    /**
     * Sets the value of the facility property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFacility(String value) {
        this.facility = value;
    }

}
