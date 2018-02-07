//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.5-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.08.11 at 01:33:48 PM GMT-10:00 
//


package com.matson.tos.jaxb.snx;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for tEro complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tEro">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="items" type="{http://www.navis.com/argo}tOrderItems" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="number" use="required" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="line" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="agent" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="estimated-date" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="earliest-date" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="latest-date" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="prevent-type-subst" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="eq-status" type="{http://www.navis.com/argo}tEquipStatus" />
 *       &lt;attribute name="notes" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="quantity" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tEro", propOrder = {
    "items"
})
public class TEro {

    protected TOrderItems items;
    @XmlAttribute(required = true)
    @XmlSchemaType(name = "anySimpleType")
    protected String number;
    @XmlAttribute(required = true)
    protected String line;
    @XmlAttribute
    protected String agent;
    @XmlAttribute(name = "estimated-date")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar estimatedDate;
    @XmlAttribute(name = "earliest-date")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar earliestDate;
    @XmlAttribute(name = "latest-date")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar latestDate;
    @XmlAttribute(name = "prevent-type-subst")
    protected String preventTypeSubst;
    @XmlAttribute(name = "eq-status")
    protected TEquipStatus eqStatus;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String notes;
    @XmlAttribute
    protected BigInteger quantity;

    /**
     * Gets the value of the items property.
     * 
     * @return
     *     possible object is
     *     {@link TOrderItems }
     *     
     */
    public TOrderItems getItems() {
        return items;
    }

    /**
     * Sets the value of the items property.
     * 
     * @param value
     *     allowed object is
     *     {@link TOrderItems }
     *     
     */
    public void setItems(TOrderItems value) {
        this.items = value;
    }

    /**
     * Gets the value of the number property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNumber() {
        return number;
    }

    /**
     * Sets the value of the number property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNumber(String value) {
        this.number = value;
    }

    /**
     * Gets the value of the line property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLine() {
        return line;
    }

    /**
     * Sets the value of the line property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLine(String value) {
        this.line = value;
    }

    /**
     * Gets the value of the agent property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAgent() {
        return agent;
    }

    /**
     * Sets the value of the agent property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAgent(String value) {
        this.agent = value;
    }

    /**
     * Gets the value of the estimatedDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getEstimatedDate() {
        return estimatedDate;
    }

    /**
     * Sets the value of the estimatedDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setEstimatedDate(XMLGregorianCalendar value) {
        this.estimatedDate = value;
    }

    /**
     * Gets the value of the earliestDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getEarliestDate() {
        return earliestDate;
    }

    /**
     * Sets the value of the earliestDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setEarliestDate(XMLGregorianCalendar value) {
        this.earliestDate = value;
    }

    /**
     * Gets the value of the latestDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getLatestDate() {
        return latestDate;
    }

    /**
     * Sets the value of the latestDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setLatestDate(XMLGregorianCalendar value) {
        this.latestDate = value;
    }

    /**
     * Gets the value of the preventTypeSubst property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPreventTypeSubst() {
        return preventTypeSubst;
    }

    /**
     * Sets the value of the preventTypeSubst property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPreventTypeSubst(String value) {
        this.preventTypeSubst = value;
    }

    /**
     * Gets the value of the eqStatus property.
     * 
     * @return
     *     possible object is
     *     {@link TEquipStatus }
     *     
     */
    public TEquipStatus getEqStatus() {
        return eqStatus;
    }

    /**
     * Sets the value of the eqStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link TEquipStatus }
     *     
     */
    public void setEqStatus(TEquipStatus value) {
        this.eqStatus = value;
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
     * Gets the value of the quantity property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getQuantity() {
        return quantity;
    }

    /**
     * Sets the value of the quantity property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setQuantity(BigInteger value) {
        this.quantity = value;
    }

}