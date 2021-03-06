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
import javax.xml.bind.annotation.XmlType;


/**
 * Event Type
 * 
 * <p>Java class for tEventType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tEventType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="filter" type="{http://www.navis.com/argo}tFilter" minOccurs="0"/>
 *         &lt;element name="effects" type="{http://www.navis.com/argo}tEventEffects" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="applies-to" type="{http://www.navis.com/argo}tBusinessEntity" />
 *       &lt;attribute name="description" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="is-billable" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="is-notifiable" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="can-bulk-update" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="is-builtin-event" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="is-event-recorded" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="is-facility-service" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="life-cycle-state" type="{http://www.navis.com/argo}tLifeCycleStateType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tEventType", propOrder = {
    "filter",
    "effects"
})
public class TEventType {

    protected TFilter filter;
    protected TEventEffects effects;
    @XmlAttribute(required = true)
    protected String id;
    @XmlAttribute(name = "applies-to")
    protected TBusinessEntity appliesTo;
    @XmlAttribute
    protected String description;
    @XmlAttribute(name = "is-billable")
    protected String isBillable;
    @XmlAttribute(name = "is-notifiable")
    protected String isNotifiable;
    @XmlAttribute(name = "can-bulk-update")
    protected String canBulkUpdate;
    @XmlAttribute(name = "is-builtin-event")
    protected String isBuiltinEvent;
    @XmlAttribute(name = "is-event-recorded")
    protected String isEventRecorded;
    @XmlAttribute(name = "is-facility-service")
    protected String isFacilityService;
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
     * Gets the value of the effects property.
     * 
     * @return
     *     possible object is
     *     {@link TEventEffects }
     *     
     */
    public TEventEffects getEffects() {
        return effects;
    }

    /**
     * Sets the value of the effects property.
     * 
     * @param value
     *     allowed object is
     *     {@link TEventEffects }
     *     
     */
    public void setEffects(TEventEffects value) {
        this.effects = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the appliesTo property.
     * 
     * @return
     *     possible object is
     *     {@link TBusinessEntity }
     *     
     */
    public TBusinessEntity getAppliesTo() {
        return appliesTo;
    }

    /**
     * Sets the value of the appliesTo property.
     * 
     * @param value
     *     allowed object is
     *     {@link TBusinessEntity }
     *     
     */
    public void setAppliesTo(TBusinessEntity value) {
        this.appliesTo = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the isBillable property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIsBillable() {
        return isBillable;
    }

    /**
     * Sets the value of the isBillable property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIsBillable(String value) {
        this.isBillable = value;
    }

    /**
     * Gets the value of the isNotifiable property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIsNotifiable() {
        return isNotifiable;
    }

    /**
     * Sets the value of the isNotifiable property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIsNotifiable(String value) {
        this.isNotifiable = value;
    }

    /**
     * Gets the value of the canBulkUpdate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCanBulkUpdate() {
        return canBulkUpdate;
    }

    /**
     * Sets the value of the canBulkUpdate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCanBulkUpdate(String value) {
        this.canBulkUpdate = value;
    }

    /**
     * Gets the value of the isBuiltinEvent property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIsBuiltinEvent() {
        return isBuiltinEvent;
    }

    /**
     * Sets the value of the isBuiltinEvent property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIsBuiltinEvent(String value) {
        this.isBuiltinEvent = value;
    }

    /**
     * Gets the value of the isEventRecorded property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIsEventRecorded() {
        return isEventRecorded;
    }

    /**
     * Sets the value of the isEventRecorded property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIsEventRecorded(String value) {
        this.isEventRecorded = value;
    }

    /**
     * Gets the value of the isFacilityService property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIsFacilityService() {
        return isFacilityService;
    }

    /**
     * Sets the value of the isFacilityService property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIsFacilityService(String value) {
        this.isFacilityService = value;
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
