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
 * Hazard Placard
 * 
 * <p>Java class for tPlacard complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tPlacard">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="placard-text" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="explanation" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="min-wt-kg" type="{http://www.w3.org/2001/XMLSchema}double" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tPlacard")
public class TPlacard {

    @XmlAttribute(name = "placard-text", required = true)
    protected String placardText;
    @XmlAttribute
    protected String explanation;
    @XmlAttribute(name = "min-wt-kg")
    protected Double minWtKg;

    /**
     * Gets the value of the placardText property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPlacardText() {
        return placardText;
    }

    /**
     * Sets the value of the placardText property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPlacardText(String value) {
        this.placardText = value;
    }

    /**
     * Gets the value of the explanation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExplanation() {
        return explanation;
    }

    /**
     * Sets the value of the explanation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExplanation(String value) {
        this.explanation = value;
    }

    /**
     * Gets the value of the minWtKg property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getMinWtKg() {
        return minWtKg;
    }

    /**
     * Sets the value of the minWtKg property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setMinWtKg(Double value) {
        this.minWtKg = value;
    }

}
