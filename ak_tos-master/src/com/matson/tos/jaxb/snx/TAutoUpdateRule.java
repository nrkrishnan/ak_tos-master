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
 * Auto Update Rule
 * 
 * <p>Java class for tAutoUpdateRule complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tAutoUpdateRule">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="filter" type="{http://www.navis.com/argo}tFilter" minOccurs="0"/>
 *         &lt;element name="update-fields" type="{http://www.navis.com/argo}tUpdateFields" minOccurs="0"/>
 *         &lt;element name="update-flags" type="{http://www.navis.com/argo}tUpdateFlags" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="event-type" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="life-cycle-state" type="{http://www.navis.com/argo}tLifeCycleStateType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tAutoUpdateRule", propOrder = {
    "filter",
    "updateFields",
    "updateFlags"
})
public class TAutoUpdateRule {

    protected TFilter filter;
    @XmlElement(name = "update-fields")
    protected TUpdateFields updateFields;
    @XmlElement(name = "update-flags")
    protected TUpdateFlags updateFlags;
    @XmlAttribute(required = true)
    protected String name;
    @XmlAttribute(name = "event-type")
    protected String eventType;
    @XmlAttribute(name = "life-cycle-state")
    protected TLifeCycleStateType lifeCycleState;

    /**
     * Gets the value of the filter property.
     * 
     * @return
     *     possible object is
     *     {@link TFilter }
     *     
     */
    public TFilter getFilter() {
        return filter;
    }

    /**
     * Sets the value of the filter property.
     * 
     * @param value
     *     allowed object is
     *     {@link TFilter }
     *     
     */
    public void setFilter(TFilter value) {
        this.filter = value;
    }

    /**
     * Gets the value of the updateFields property.
     * 
     * @return
     *     possible object is
     *     {@link TUpdateFields }
     *     
     */
    public TUpdateFields getUpdateFields() {
        return updateFields;
    }

    /**
     * Sets the value of the updateFields property.
     * 
     * @param value
     *     allowed object is
     *     {@link TUpdateFields }
     *     
     */
    public void setUpdateFields(TUpdateFields value) {
        this.updateFields = value;
    }

    /**
     * Gets the value of the updateFlags property.
     * 
     * @return
     *     possible object is
     *     {@link TUpdateFlags }
     *     
     */
    public TUpdateFlags getUpdateFlags() {
        return updateFlags;
    }

    /**
     * Sets the value of the updateFlags property.
     * 
     * @param value
     *     allowed object is
     *     {@link TUpdateFlags }
     *     
     */
    public void setUpdateFlags(TUpdateFlags value) {
        this.updateFlags = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the eventType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEventType() {
        return eventType;
    }

    /**
     * Sets the value of the eventType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEventType(String value) {
        this.eventType = value;
    }

    /**
     * Gets the value of the lifeCycleState property.
     * 
     * @return
     *     possible object is
     *     {@link TLifeCycleStateType }
     *     
     */
    public TLifeCycleStateType getLifeCycleState() {
        return lifeCycleState;
    }

    /**
     * Sets the value of the lifeCycleState property.
     * 
     * @param value
     *     allowed object is
     *     {@link TLifeCycleStateType }
     *     
     */
    public void setLifeCycleState(TLifeCycleStateType value) {
        this.lifeCycleState = value;
    }

}
