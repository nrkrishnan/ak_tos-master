//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.5-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.08.11 at 01:33:48 PM GMT-10:00 
//


package com.matson.tos.jaxb.snx;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Pool Members
 * 
 * <p>Java class for tMembers complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tMembers">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="member" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="equpment-classes" type="{http://www.navis.com/argo}tPoolEqClasses" minOccurs="0"/>
 *                   &lt;element name="equpment-types" type="{http://www.navis.com/argo}tPoolEqTypes" minOccurs="0"/>
 *                 &lt;/sequence>
 *                 &lt;attribute name="equipment-operator" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
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
@XmlType(name = "tMembers", propOrder = {
    "member"
})
public class TMembers {

    protected List<TMembers.Member> member;

    /**
     * Gets the value of the member property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the member property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMember().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TMembers.Member }
     * 
     * 
     */
    public List<TMembers.Member> getMember() {
        if (member == null) {
            member = new ArrayList<TMembers.Member>();
        }
        return this.member;
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
     *       &lt;sequence>
     *         &lt;element name="equpment-classes" type="{http://www.navis.com/argo}tPoolEqClasses" minOccurs="0"/>
     *         &lt;element name="equpment-types" type="{http://www.navis.com/argo}tPoolEqTypes" minOccurs="0"/>
     *       &lt;/sequence>
     *       &lt;attribute name="equipment-operator" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "equpmentClasses",
        "equpmentTypes"
    })
    public static class Member {

        @XmlElement(name = "equpment-classes")
        protected TPoolEqClasses equpmentClasses;
        @XmlElement(name = "equpment-types")
        protected TPoolEqTypes equpmentTypes;
        @XmlAttribute(name = "equipment-operator", required = true)
        protected String equipmentOperator;

        /**
         * Gets the value of the equpmentClasses property.
         * 
         * @return
         *     possible object is
         *     {@link TPoolEqClasses }
         *     
         */
        public TPoolEqClasses getEqupmentClasses() {
            return equpmentClasses;
        }

        /**
         * Sets the value of the equpmentClasses property.
         * 
         * @param value
         *     allowed object is
         *     {@link TPoolEqClasses }
         *     
         */
        public void setEqupmentClasses(TPoolEqClasses value) {
            this.equpmentClasses = value;
        }

        /**
         * Gets the value of the equpmentTypes property.
         * 
         * @return
         *     possible object is
         *     {@link TPoolEqTypes }
         *     
         */
        public TPoolEqTypes getEqupmentTypes() {
            return equpmentTypes;
        }

        /**
         * Sets the value of the equpmentTypes property.
         * 
         * @param value
         *     allowed object is
         *     {@link TPoolEqTypes }
         *     
         */
        public void setEqupmentTypes(TPoolEqTypes value) {
            this.equpmentTypes = value;
        }

        /**
         * Gets the value of the equipmentOperator property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getEquipmentOperator() {
            return equipmentOperator;
        }

        /**
         * Sets the value of the equipmentOperator property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setEquipmentOperator(String value) {
            this.equipmentOperator = value;
        }

    }

}
