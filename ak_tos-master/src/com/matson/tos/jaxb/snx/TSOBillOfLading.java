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
 * Bill Of Lading
 * 
 * <p>Java class for tSOBillOfLading complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tSOBillOfLading">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="bl-nbr" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="bl-line" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="bl-carrier-visit" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tSOBillOfLading")
public class TSOBillOfLading {

    @XmlAttribute(name = "bl-nbr")
    protected String blNbr;
    @XmlAttribute(name = "bl-line")
    protected String blLine;
    @XmlAttribute(name = "bl-carrier-visit")
    protected String blCarrierVisit;

    /**
     * Gets the value of the blNbr property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBlNbr() {
        return blNbr;
    }

    /**
     * Sets the value of the blNbr property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBlNbr(String value) {
        this.blNbr = value;
    }

    /**
     * Gets the value of the blLine property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBlLine() {
        return blLine;
    }

    /**
     * Sets the value of the blLine property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBlLine(String value) {
        this.blLine = value;
    }

    /**
     * Gets the value of the blCarrierVisit property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBlCarrierVisit() {
        return blCarrierVisit;
    }

    /**
     * Sets the value of the blCarrierVisit property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBlCarrierVisit(String value) {
        this.blCarrierVisit = value;
    }

}
