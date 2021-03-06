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
import javax.xml.bind.annotation.XmlType;


/**
 * Item Service Type Units
 * 
 * <p>Java class for tItemServiceTypeUnits complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tItemServiceTypeUnits">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="unit" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="equip-nbr" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="unit-status" type="{http://www.navis.com/argo}tServiceOrderUnitStatus" />
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
@XmlType(name = "tItemServiceTypeUnits", propOrder = {
    "unit"
})
public class TItemServiceTypeUnits {

    protected List<TItemServiceTypeUnits.Unit> unit;

    /**
     * Gets the value of the unit property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the unit property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getUnit().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TItemServiceTypeUnits.Unit }
     * 
     * 
     */
    public List<TItemServiceTypeUnits.Unit> getUnit() {
        if (unit == null) {
            unit = new ArrayList<TItemServiceTypeUnits.Unit>();
        }
        return this.unit;
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
     *       &lt;attribute name="equip-nbr" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="unit-status" type="{http://www.navis.com/argo}tServiceOrderUnitStatus" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Unit {

        @XmlAttribute(name = "equip-nbr", required = true)
        protected String equipNbr;
        @XmlAttribute(name = "unit-status")
        protected TServiceOrderUnitStatus unitStatus;

        /**
         * Gets the value of the equipNbr property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getEquipNbr() {
            return equipNbr;
        }

        /**
         * Sets the value of the equipNbr property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setEquipNbr(String value) {
            this.equipNbr = value;
        }

        /**
         * Gets the value of the unitStatus property.
         * 
         * @return
         *     possible object is
         *     {@link TServiceOrderUnitStatus }
         *     
         */
        public TServiceOrderUnitStatus getUnitStatus() {
            return unitStatus;
        }

        /**
         * Sets the value of the unitStatus property.
         * 
         * @param value
         *     allowed object is
         *     {@link TServiceOrderUnitStatus }
         *     
         */
        public void setUnitStatus(TServiceOrderUnitStatus value) {
            this.unitStatus = value;
        }

    }

}
