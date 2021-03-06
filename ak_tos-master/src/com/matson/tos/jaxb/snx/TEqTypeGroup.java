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
 * Equivalent Type Group
 * 
 * <p>Java class for tEqTypeGroup complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tEqTypeGroup">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="eq-type-group-items" type="{http://www.navis.com/argo}tEqTypeGroupItems" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="direction" type="{http://www.navis.com/argo}tEqTypeGroupKind" />
 *       &lt;attribute name="line" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tEqTypeGroup", propOrder = {
    "eqTypeGroupItems"
})
public class TEqTypeGroup {

    @XmlElement(name = "eq-type-group-items")
    protected TEqTypeGroupItems eqTypeGroupItems;
    @XmlAttribute(required = true)
    protected String id;
    @XmlAttribute
    protected TEqTypeGroupKind direction;
    @XmlAttribute
    protected String line;

    /**
     * Gets the value of the eqTypeGroupItems property.
     * 
     * @return
     *     possible object is
     *     {@link TEqTypeGroupItems }
     *     
     */
    public TEqTypeGroupItems getEqTypeGroupItems() {
        return eqTypeGroupItems;
    }

    /**
     * Sets the value of the eqTypeGroupItems property.
     * 
     * @param value
     *     allowed object is
     *     {@link TEqTypeGroupItems }
     *     
     */
    public void setEqTypeGroupItems(TEqTypeGroupItems value) {
        this.eqTypeGroupItems = value;
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
     * Gets the value of the direction property.
     * 
     * @return
     *     possible object is
     *     {@link TEqTypeGroupKind }
     *     
     */
    public TEqTypeGroupKind getDirection() {
        return direction;
    }

    /**
     * Sets the value of the direction property.
     * 
     * @param value
     *     allowed object is
     *     {@link TEqTypeGroupKind }
     *     
     */
    public void setDirection(TEqTypeGroupKind value) {
        this.direction = value;
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

}
