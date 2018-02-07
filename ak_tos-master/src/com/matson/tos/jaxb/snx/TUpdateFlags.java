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
 * Auto Update Flags
 * 
 * <p>Java class for tUpdateFlags complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tUpdateFlags">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="flag" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="type-id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="is-released" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="notes" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *       &lt;attribute name="update-mode" type="{http://www.navis.com/argo}tUpdateMode" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tUpdateFlags", propOrder = {
    "flag"
})
public class TUpdateFlags {

    protected List<TUpdateFlags.Flag> flag;
    @XmlAttribute(name = "update-mode")
    protected TUpdateMode updateMode;

    /**
     * Gets the value of the flag property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the flag property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFlag().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TUpdateFlags.Flag }
     * 
     * 
     */
    public List<TUpdateFlags.Flag> getFlag() {
        if (flag == null) {
            flag = new ArrayList<TUpdateFlags.Flag>();
        }
        return this.flag;
    }

    /**
     * Gets the value of the updateMode property.
     * 
     * @return
     *     possible object is
     *     {@link TUpdateMode }
     *     
     */
    public TUpdateMode getUpdateMode() {
        return updateMode;
    }

    /**
     * Sets the value of the updateMode property.
     * 
     * @param value
     *     allowed object is
     *     {@link TUpdateMode }
     *     
     */
    public void setUpdateMode(TUpdateMode value) {
        this.updateMode = value;
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
     *       &lt;attribute name="type-id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="is-released" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="notes" type="{http://www.w3.org/2001/XMLSchema}string" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Flag {

        @XmlAttribute(name = "type-id", required = true)
        protected String typeId;
        @XmlAttribute(name = "is-released")
        protected String isReleased;
        @XmlAttribute
        protected String notes;

        /**
         * Gets the value of the typeId property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getTypeId() {
            return typeId;
        }

        /**
         * Sets the value of the typeId property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setTypeId(String value) {
            this.typeId = value;
        }

        /**
         * Gets the value of the isReleased property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getIsReleased() {
            return isReleased;
        }

        /**
         * Sets the value of the isReleased property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setIsReleased(String value) {
            this.isReleased = value;
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

    }

}
