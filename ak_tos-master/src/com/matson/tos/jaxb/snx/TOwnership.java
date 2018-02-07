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
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for tOwnership complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tOwnership">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="owner" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="operator" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="previous-operator" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="lease-expiration" type="{http://www.w3.org/2001/XMLSchema}date" />
 *       &lt;attribute name="offhire-location" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tOwnership")
public class TOwnership {

    @XmlAttribute
    protected String owner;
    @XmlAttribute
    protected String operator;
    @XmlAttribute(name = "previous-operator")
    protected String previousOperator;
    @XmlAttribute(name = "lease-expiration")
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar leaseExpiration;
    @XmlAttribute(name = "offhire-location")
    protected String offhireLocation;

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
     * Gets the value of the operator property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOperator() {
        return operator;
    }

    /**
     * Sets the value of the operator property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOperator(String value) {
        this.operator = value;
    }

    /**
     * Gets the value of the previousOperator property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPreviousOperator() {
        return previousOperator;
    }

    /**
     * Sets the value of the previousOperator property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPreviousOperator(String value) {
        this.previousOperator = value;
    }

    /**
     * Gets the value of the leaseExpiration property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getLeaseExpiration() {
        return leaseExpiration;
    }

    /**
     * Sets the value of the leaseExpiration property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setLeaseExpiration(XMLGregorianCalendar value) {
        this.leaseExpiration = value;
    }

    /**
     * Gets the value of the offhireLocation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOffhireLocation() {
        return offhireLocation;
    }

    /**
     * Sets the value of the offhireLocation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOffhireLocation(String value) {
        this.offhireLocation = value;
    }

}
