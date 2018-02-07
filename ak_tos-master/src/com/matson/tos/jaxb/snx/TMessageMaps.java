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
 * Edi Message Map
 * 
 * <p>Java class for tMessageMaps complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tMessageMaps">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="message-map" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="dic-file" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="mgt-file" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                 &lt;/sequence>
 *                 &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="direction" type="{http://www.navis.com/argo}tEdiMessageDirection" />
 *                 &lt;attribute name="description" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="life-cycle-state" type="{http://www.navis.com/argo}tLifeCycleStateType" />
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
@XmlType(name = "tMessageMaps", propOrder = {
    "messageMap"
})
public class TMessageMaps {

    @XmlElement(name = "message-map")
    protected List<TMessageMaps.MessageMap> messageMap;

    /**
     * Gets the value of the messageMap property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the messageMap property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMessageMap().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TMessageMaps.MessageMap }
     * 
     * 
     */
    public List<TMessageMaps.MessageMap> getMessageMap() {
        if (messageMap == null) {
            messageMap = new ArrayList<TMessageMaps.MessageMap>();
        }
        return this.messageMap;
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
     *         &lt;element name="dic-file" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="mgt-file" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *       &lt;/sequence>
     *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="direction" type="{http://www.navis.com/argo}tEdiMessageDirection" />
     *       &lt;attribute name="description" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="life-cycle-state" type="{http://www.navis.com/argo}tLifeCycleStateType" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "dicFile",
        "mgtFile"
    })
    public static class MessageMap {

        @XmlElement(name = "dic-file", required = true)
        protected String dicFile;
        @XmlElement(name = "mgt-file", required = true)
        protected String mgtFile;
        @XmlAttribute(required = true)
        protected String id;
        @XmlAttribute
        protected TEdiMessageDirection direction;
        @XmlAttribute
        protected String description;
        @XmlAttribute(name = "life-cycle-state")
        protected TLifeCycleStateType lifeCycleState;

        /**
         * Gets the value of the dicFile property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getDicFile() {
            return dicFile;
        }

        /**
         * Sets the value of the dicFile property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setDicFile(String value) {
            this.dicFile = value;
        }

        /**
         * Gets the value of the mgtFile property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getMgtFile() {
            return mgtFile;
        }

        /**
         * Sets the value of the mgtFile property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setMgtFile(String value) {
            this.mgtFile = value;
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
         *     {@link TEdiMessageDirection }
         *     
         */
        public TEdiMessageDirection getDirection() {
            return direction;
        }

        /**
         * Sets the value of the direction property.
         * 
         * @param value
         *     allowed object is
         *     {@link TEdiMessageDirection }
         *     
         */
        public void setDirection(TEdiMessageDirection value) {
            this.direction = value;
        }

        /**
         * Gets the value of the description property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getDescription() {
            return description;
        }

        /**
         * Sets the value of the description property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setDescription(String value) {
            this.description = value;
        }

        /**
         * Gets the value of the lifeCycleState property.
         * 
         * @return
         *     possible object is
         *     {@link TLifeCycleStateType }
         *     
         */
        public TLifeCycleStateType getLifeCycleState() {
            return lifeCycleState;
        }

        /**
         * Sets the value of the lifeCycleState property.
         * 
         * @param value
         *     allowed object is
         *     {@link TLifeCycleStateType }
         *     
         */
        public void setLifeCycleState(TLifeCycleStateType value) {
            this.lifeCycleState = value;
        }

    }

}
