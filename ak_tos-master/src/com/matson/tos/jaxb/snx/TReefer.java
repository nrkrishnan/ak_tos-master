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
 * <p>Java class for tReefer complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tReefer">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.navis.com/argo}tBaseReefer">
 *       &lt;attribute name="is-starvent" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tReefer")
public class TReefer
    extends TBaseReefer
{

    @XmlAttribute(name = "is-starvent")
    protected String isStarvent;

    /**
     * Gets the value of the isStarvent property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIsStarvent() {
        return isStarvent;
    }

    /**
     * Sets the value of the isStarvent property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIsStarvent(String value) {
        this.isStarvent = value;
    }

}
