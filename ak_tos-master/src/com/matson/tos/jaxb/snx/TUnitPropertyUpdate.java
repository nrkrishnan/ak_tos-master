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
 * This message is used to effect an update to a Unit that is specific to the customer's business process.
 *             
 * 
 * <p>Java class for tUnitPropertyUpdate complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tUnitPropertyUpdate">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="unit-identity" type="{http://www.navis.com/argo}tUnitIdentity"/>
 *         &lt;element name="position" type="{http://www.navis.com/argo}tPosition" minOccurs="0"/>
 *         &lt;element name="routing" type="{http://www.navis.com/argo}tRouting" minOccurs="0"/>
 *         &lt;element name="hazards" type="{http://www.navis.com/argo}tHazards" minOccurs="0"/>
 *         &lt;element name="reefer" type="{http://www.navis.com/argo}tReeferRequirements" minOccurs="0"/>
 *         &lt;element name="oog" type="{http://www.navis.com/argo}tOog" minOccurs="0"/>
 *         &lt;element name="handling" type="{http://www.navis.com/argo}tUnitHandling" minOccurs="0"/>
 *         &lt;element name="contents" type="{http://www.navis.com/argo}tUnitContents" minOccurs="0"/>
 *         &lt;element name="flags" type="{http://www.navis.com/argo}tFlags" minOccurs="0"/>
 *         &lt;element name="unit-etc" type="{http://www.navis.com/argo}tUnitEtc" minOccurs="0"/>
 *         &lt;element name="unit-flex" type="{http://www.navis.com/argo}tUnitFlexFields" minOccurs="0"/>
 *         &lt;element name="ufv-flex" type="{http://www.navis.com/argo}tUfvFlexFields" minOccurs="0"/>
 *         &lt;element name="event" type="{http://www.navis.com/argo}tEvent" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tUnitPropertyUpdate", propOrder = {
    "unitIdentity",
    "position",
    "routing",
    "hazards",
    "reefer",
    "oog",
    "handling",
    "contents",
    "flags",
    "unitEtc",
    "unitFlex",
    "ufvFlex",
    "event"
})
public class TUnitPropertyUpdate {

    @XmlElement(name = "unit-identity", required = true)
    protected TUnitIdentity unitIdentity;
    protected TPosition position;
    protected TRouting routing;
    protected THazards hazards;
    protected TReeferRequirements reefer;
    protected TOog oog;
    protected TUnitHandling handling;
    protected TUnitContents contents;
    protected TFlags flags;
    @XmlElement(name = "unit-etc")
    protected TUnitEtc unitEtc;
    @XmlElement(name = "unit-flex")
    protected TUnitFlexFields unitFlex;
    @XmlElement(name = "ufv-flex")
    protected TUfvFlexFields ufvFlex;
    protected TEvent event;

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
     * Gets the value of the position property.
     * 
     * @return
     *     possible object is
     *     {@link TPosition }
     *     
     */
    public TPosition getPosition() {
        return position;
    }

    /**
     * Sets the value of the position property.
     * 
     * @param value
     *     allowed object is
     *     {@link TPosition }
     *     
     */
    public void setPosition(TPosition value) {
        this.position = value;
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
     * Gets the value of the hazards property.
     * 
     * @return
     *     possible object is
     *     {@link THazards }
     *     
     */
    public THazards getHazards() {
        return hazards;
    }

    /**
     * Sets the value of the hazards property.
     * 
     * @param value
     *     allowed object is
     *     {@link THazards }
     *     
     */
    public void setHazards(THazards value) {
        this.hazards = value;
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

    /**
     * Gets the value of the oog property.
     * 
     * @return
     *     possible object is
     *     {@link TOog }
     *     
     */
    public TOog getOog() {
        return oog;
    }

    /**
     * Sets the value of the oog property.
     * 
     * @param value
     *     allowed object is
     *     {@link TOog }
     *     
     */
    public void setOog(TOog value) {
        this.oog = value;
    }

    /**
     * Gets the value of the handling property.
     * 
     * @return
     *     possible object is
     *     {@link TUnitHandling }
     *     
     */
    public TUnitHandling getHandling() {
        return handling;
    }

    /**
     * Sets the value of the handling property.
     * 
     * @param value
     *     allowed object is
     *     {@link TUnitHandling }
     *     
     */
    public void setHandling(TUnitHandling value) {
        this.handling = value;
    }

    /**
     * Gets the value of the contents property.
     * 
     * @return
     *     possible object is
     *     {@link TUnitContents }
     *     
     */
    public TUnitContents getContents() {
        return contents;
    }

    /**
     * Sets the value of the contents property.
     * 
     * @param value
     *     allowed object is
     *     {@link TUnitContents }
     *     
     */
    public void setContents(TUnitContents value) {
        this.contents = value;
    }

    /**
     * Gets the value of the flags property.
     * 
     * @return
     *     possible object is
     *     {@link TFlags }
     *     
     */
    public TFlags getFlags() {
        return flags;
    }

    /**
     * Sets the value of the flags property.
     * 
     * @param value
     *     allowed object is
     *     {@link TFlags }
     *     
     */
    public void setFlags(TFlags value) {
        this.flags = value;
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

}
