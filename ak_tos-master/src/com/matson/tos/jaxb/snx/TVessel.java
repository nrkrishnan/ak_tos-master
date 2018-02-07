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
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * Definition of an individual vessel.
 * 
 * <p>Java class for tVessel complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tVessel">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="captain" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="country-id" type="{http://www.w3.org/2001/XMLSchema}NCName" />
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}NCName" />
 *       &lt;attribute name="lloyds-id" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="notes" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="owner" type="{http://www.w3.org/2001/XMLSchema}NCName" />
 *       &lt;attribute name="radio-call-sign" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="documentationNbr" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="serviceRegistryNbr" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="stowage-scheme">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NCName">
 *             &lt;enumeration value="UNKNOWN"/>
 *             &lt;enumeration value="BBRRTTB"/>
 *             &lt;enumeration value="DECKHOLD"/>
 *             &lt;enumeration value="ISO"/>
 *             &lt;enumeration value="RUSSIAN"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="temperature-units">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NCName">
 *             &lt;enumeration value="F"/>
 *             &lt;enumeration value="C"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="unit-system">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NCName">
 *             &lt;enumeration value="SI"/>
 *             &lt;enumeration value="BRITISH"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="vessel-class" type="{http://www.w3.org/2001/XMLSchema}NCName" />
 *       &lt;attribute name="life-cycle-state" type="{http://www.navis.com/argo}tLifeCycleStateType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tVessel")
public class TVessel {

    @XmlAttribute
    protected String captain;
    @XmlAttribute(name = "country-id")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    protected String countryId;
    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    protected String id;
    @XmlAttribute(name = "lloyds-id")
    protected String lloydsId;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String name;
    @XmlAttribute
    protected String notes;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    protected String owner;
    @XmlAttribute(name = "radio-call-sign")
    protected String radioCallSign;
    @XmlAttribute
    protected String documentationNbr;
    @XmlAttribute
    protected String serviceRegistryNbr;
    @XmlAttribute(name = "stowage-scheme")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String stowageScheme;
    @XmlAttribute(name = "temperature-units")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String temperatureUnits;
    @XmlAttribute(name = "unit-system")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String unitSystem;
    @XmlAttribute(name = "vessel-class")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    protected String vesselClass;
    @XmlAttribute(name = "life-cycle-state")
    protected TLifeCycleStateType lifeCycleState;

    /**
     * Gets the value of the captain property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCaptain() {
        return captain;
    }

    /**
     * Sets the value of the captain property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCaptain(String value) {
        this.captain = value;
    }

    /**
     * Gets the value of the countryId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCountryId() {
        return countryId;
    }

    /**
     * Sets the value of the countryId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCountryId(String value) {
        this.countryId = value;
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
     * Gets the value of the lloydsId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLloydsId() {
        return lloydsId;
    }

    /**
     * Sets the value of the lloydsId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLloydsId(String value) {
        this.lloydsId = value;
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
     * Gets the value of the notes property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNotes() {
        return notes;
    }

    /**
     * Sets the value of the notes property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNotes(String value) {
        this.notes = value;
    }

    /**
     * Gets the value of the owner property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Sets the value of the owner property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOwner(String value) {
        this.owner = value;
    }

    /**
     * Gets the value of the radioCallSign property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRadioCallSign() {
        return radioCallSign;
    }

    /**
     * Sets the value of the radioCallSign property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRadioCallSign(String value) {
        this.radioCallSign = value;
    }

    /**
     * Gets the value of the documentationNbr property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDocumentationNbr() {
        return documentationNbr;
    }

    /**
     * Sets the value of the documentationNbr property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDocumentationNbr(String value) {
        this.documentationNbr = value;
    }

    /**
     * Gets the value of the serviceRegistryNbr property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getServiceRegistryNbr() {
        return serviceRegistryNbr;
    }

    /**
     * Sets the value of the serviceRegistryNbr property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setServiceRegistryNbr(String value) {
        this.serviceRegistryNbr = value;
    }

    /**
     * Gets the value of the stowageScheme property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStowageScheme() {
        return stowageScheme;
    }

    /**
     * Sets the value of the stowageScheme property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStowageScheme(String value) {
        this.stowageScheme = value;
    }

    /**
     * Gets the value of the temperatureUnits property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTemperatureUnits() {
        return temperatureUnits;
    }

    /**
     * Sets the value of the temperatureUnits property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTemperatureUnits(String value) {
        this.temperatureUnits = value;
    }

    /**
     * Gets the value of the unitSystem property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUnitSystem() {
        return unitSystem;
    }

    /**
     * Sets the value of the unitSystem property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUnitSystem(String value) {
        this.unitSystem = value;
    }

    /**
     * Gets the value of the vesselClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVesselClass() {
        return vesselClass;
    }

    /**
     * Sets the value of the vesselClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVesselClass(String value) {
        this.vesselClass = value;
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