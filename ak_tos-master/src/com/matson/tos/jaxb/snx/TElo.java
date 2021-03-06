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
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for tElo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tElo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="items" type="{http://www.navis.com/argo}tEqOrderItems" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="number" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="line-operator" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="vessel-visit" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="port-of-load" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="port-of-discharge" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="optional-pod" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="notes" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="quantity" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tElo", propOrder = {
    "items"
})
public class TElo {

    protected TEqOrderItems items;
    @XmlAttribute(required = true)
    protected String number;
    @XmlAttribute(name = "line-operator", required = true)
    protected String lineOperator;
    @XmlAttribute(name = "vessel-visit")
    protected String vesselVisit;
    @XmlAttribute(name = "port-of-load")
    protected String portOfLoad;
    @XmlAttribute(name = "port-of-discharge")
    protected String portOfDischarge;
    @XmlAttribute(name = "optional-pod")
    protected String optionalPod;
    @XmlAttribute
    protected String notes;
    @XmlAttribute
    protected BigInteger quantity;

    /**
     * Gets the value of the items property.
     * 
     * @return
     *     possible object is
     *     {@link TEqOrderItems }
     *     
     */
    public TEqOrderItems getItems() {
        return items;
    }

    /**
     * Sets the value of the items property.
     * 
     * @param value
     *     allowed object is
     *     {@link TEqOrderItems }
     *     
     */
    public void setItems(TEqOrderItems value) {
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
     * Gets the value of the lineOperator property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLineOperator() {
        return lineOperator;
    }

    /**
     * Sets the value of the lineOperator property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLineOperator(String value) {
        this.lineOperator = value;
    }

    /**
     * Gets the value of the vesselVisit property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVesselVisit() {
        return vesselVisit;
    }

    /**
     * Sets the value of the vesselVisit property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVesselVisit(String value) {
        this.vesselVisit = value;
    }

    /**
     * Gets the value of the portOfLoad property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPortOfLoad() {
        return portOfLoad;
    }

    /**
     * Sets the value of the portOfLoad property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPortOfLoad(String value) {
        this.portOfLoad = value;
    }

    /**
     * Gets the value of the portOfDischarge property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPortOfDischarge() {
        return portOfDischarge;
    }

    /**
     * Sets the value of the portOfDischarge property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPortOfDischarge(String value) {
        this.portOfDischarge = value;
    }

    /**
     * Gets the value of the optionalPod property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOptionalPod() {
        return optionalPod;
    }

    /**
     * Sets the value of the optionalPod property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOptionalPod(String value) {
        this.optionalPod = value;
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
