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
 * <p>Java class for tUnitRenumber complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tUnitRenumber">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="unit-identity" type="{http://www.navis.com/argo}tUnitIdentity"/>
 *         &lt;element name="renumber">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="bad-id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="corrected-id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tUnitRenumber", propOrder = {
    "unitIdentity",
    "renumber"
})
public class TUnitRenumber {

    @XmlElement(name = "unit-identity", required = true)
    protected TUnitIdentity unitIdentity;
    @XmlElement(required = true)
    protected TUnitRenumber.Renumber renumber;

    /**
     * Gets the value of the unitIdentity property.
     * 
     * @return
     *     possible object is
     *     {@link TUnitIdentity }
     *     
     */
    public TUnitIdentity getUnitIdentity() {
        return unitIdentity;
    }

    /**
     * Sets the value of the unitIdentity property.
     * 
     * @param value
     *     allowed object is
     *     {@link TUnitIdentity }
     *     
     */
    public void setUnitIdentity(TUnitIdentity value) {
        this.unitIdentity = value;
    }

    /**
     * Gets the value of the renumber property.
     * 
     * @return
     *     possible object is
     *     {@link TUnitRenumber.Renumber }
     *     
     */
    public TUnitRenumber.Renumber getRenumber() {
        return renumber;
    }

    /**
     * Sets the value of the renumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link TUnitRenumber.Renumber }
     *     
     */
    public void setRenumber(TUnitRenumber.Renumber value) {
        this.renumber = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;attribute name="bad-id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="corrected-id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Renumber {

        @XmlAttribute(name = "bad-id", required = true)
        protected String badId;
        @XmlAttribute(name = "corrected-id", required = true)
        protected String correctedId;

        /**
         * Gets the value of the badId property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getBadId() {
            return badId;
        }

        /**
         * Sets the value of the badId property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setBadId(String value) {
            this.badId = value;
        }

        /**
         * Gets the value of the correctedId property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getCorrectedId() {
            return correctedId;
        }

        /**
         * Sets the value of the correctedId property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setCorrectedId(String value) {
            this.correctedId = value;
        }

    }

}
