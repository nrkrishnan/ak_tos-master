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
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * Group
 * 
 * <p>Java class for tGroup complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tGroup">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="trucking-companies" type="{http://www.navis.com/argo}tTruckingCompanies" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="description" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="life-cycle-state" type="{http://www.navis.com/argo}tLifeCycleStateType" />
 *       &lt;attribute name="purpose" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="flex-string-1" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="flex-string-2" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="flex-string-3" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="time-start-delivery" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="time-end-delivery" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="destination-facility" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tGroup", propOrder = {
    "truckingCompanies"
})
public class TGroup {

    @XmlElement(name = "trucking-companies")
    protected TTruckingCompanies truckingCompanies;
    @XmlAttribute(required = true)
    protected String id;
    @XmlAttribute
    protected String description;
    @XmlAttribute(name = "life-cycle-state")
    protected TLifeCycleStateType lifeCycleState;
    @XmlAttribute
    protected String purpose;
    @XmlAttribute(name = "flex-string-1")
    protected String flexString1;
    @XmlAttribute(name = "flex-string-2")
    protected String flexString2;
    @XmlAttribute(name = "flex-string-3")
    protected String flexString3;
    @XmlAttribute(name = "time-start-delivery")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar timeStartDelivery;
    @XmlAttribute(name = "time-end-delivery")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar timeEndDelivery;
    @XmlAttribute(name = "destination-facility")
    protected String destinationFacility;

    /**
     * Gets the value of the truckingCompanies property.
     * 
     * @return
     *     possible object is
     *     {@link TTruckingCompanies }
     *     
     */
    public TTruckingCompanies getTruckingCompanies() {
        return truckingCompanies;
    }

    /**
     * Sets the value of the truckingCompanies property.
     * 
     * @param value
     *     allowed object is
     *     {@link TTruckingCompanies }
     *     
     */
    public void setTruckingCompanies(TTruckingCompanies value) {
        this.truckingCompanies = value;
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

    /**
     * Gets the value of the purpose property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPurpose() {
        return purpose;
    }

    /**
     * Sets the value of the purpose property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPurpose(String value) {
        this.purpose = value;
    }

    /**
     * Gets the value of the flexString1 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFlexString1() {
        return flexString1;
    }

    /**
     * Sets the value of the flexString1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFlexString1(String value) {
        this.flexString1 = value;
    }

    /**
     * Gets the value of the flexString2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFlexString2() {
        return flexString2;
    }

    /**
     * Sets the value of the flexString2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFlexString2(String value) {
        this.flexString2 = value;
    }

    /**
     * Gets the value of the flexString3 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFlexString3() {
        return flexString3;
    }

    /**
     * Sets the value of the flexString3 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFlexString3(String value) {
        this.flexString3 = value;
    }

    /**
     * Gets the value of the timeStartDelivery property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getTimeStartDelivery() {
        return timeStartDelivery;
    }

    /**
     * Sets the value of the timeStartDelivery property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setTimeStartDelivery(XMLGregorianCalendar value) {
        this.timeStartDelivery = value;
    }

    /**
     * Gets the value of the timeEndDelivery property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getTimeEndDelivery() {
        return timeEndDelivery;
    }

    /**
     * Sets the value of the timeEndDelivery property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setTimeEndDelivery(XMLGregorianCalendar value) {
        this.timeEndDelivery = value;
    }

    /**
     * Gets the value of the destinationFacility property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDestinationFacility() {
        return destinationFacility;
    }

    /**
     * Sets the value of the destinationFacility property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDestinationFacility(String value) {
        this.destinationFacility = value;
    }

}
