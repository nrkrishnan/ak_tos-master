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
 * facilities.
 * 
 * <p>Java class for tFacilities complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tFacilities">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="facility" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="yards" type="{http://www.navis.com/argo}tYards" minOccurs="0"/>
 *                   &lt;element name="quays" type="{http://www.navis.com/argo}tQuays" minOccurs="0"/>
 *                   &lt;element name="routing-point" type="{http://www.navis.com/argo}tRoutingPoint" minOccurs="0"/>
 *                   &lt;element name="jms-connection" type="{http://www.navis.com/argo}tJmsConnection" minOccurs="0"/>
 *                 &lt;/sequence>
 *                 &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="message-class" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="teu-capacity-green" type="{http://www.w3.org/2001/XMLSchema}long" />
 *                 &lt;attribute name="teu-capacity-red" type="{http://www.w3.org/2001/XMLSchema}long" />
 *                 &lt;attribute name="teu-capacity-yellow" type="{http://www.w3.org/2001/XMLSchema}long" />
 *                 &lt;attribute name="is-non-operational" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="time-zone-id" type="{http://www.w3.org/2001/XMLSchema}string" />
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
@XmlType(name = "tFacilities", propOrder = {
    "facility"
})
public class TFacilities {

    protected List<TFacilities.Facility> facility;

    /**
     * Gets the value of the facility property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the facility property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFacility().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TFacilities.Facility }
     * 
     * 
     */
    public List<TFacilities.Facility> getFacility() {
        if (facility == null) {
            facility = new ArrayList<TFacilities.Facility>();
        }
        return this.facility;
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
     *         &lt;element name="yards" type="{http://www.navis.com/argo}tYards" minOccurs="0"/>
     *         &lt;element name="quays" type="{http://www.navis.com/argo}tQuays" minOccurs="0"/>
     *         &lt;element name="routing-point" type="{http://www.navis.com/argo}tRoutingPoint" minOccurs="0"/>
     *         &lt;element name="jms-connection" type="{http://www.navis.com/argo}tJmsConnection" minOccurs="0"/>
     *       &lt;/sequence>
     *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="message-class" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="teu-capacity-green" type="{http://www.w3.org/2001/XMLSchema}long" />
     *       &lt;attribute name="teu-capacity-red" type="{http://www.w3.org/2001/XMLSchema}long" />
     *       &lt;attribute name="teu-capacity-yellow" type="{http://www.w3.org/2001/XMLSchema}long" />
     *       &lt;attribute name="is-non-operational" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="time-zone-id" type="{http://www.w3.org/2001/XMLSchema}string" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "yards",
        "quays",
        "routingPoint",
        "jmsConnection"
    })
    public static class Facility {

        protected TYards yards;
        protected TQuays quays;
        @XmlElement(name = "routing-point")
        protected TRoutingPoint routingPoint;
        @XmlElement(name = "jms-connection")
        protected TJmsConnection jmsConnection;
        @XmlAttribute(required = true)
        protected String id;
        @XmlAttribute(name = "message-class")
        protected String messageClass;
        @XmlAttribute(name = "teu-capacity-green")
        protected Long teuCapacityGreen;
        @XmlAttribute(name = "teu-capacity-red")
        protected Long teuCapacityRed;
        @XmlAttribute(name = "teu-capacity-yellow")
        protected Long teuCapacityYellow;
        @XmlAttribute(name = "is-non-operational")
        protected String isNonOperational;
        @XmlAttribute(name = "time-zone-id")
        protected String timeZoneId;

        /**
         * Gets the value of the yards property.
         * 
         * @return
         *     possible object is
         *     {@link TYards }
         *     
         */
        public TYards getYards() {
            return yards;
        }

        /**
         * Sets the value of the yards property.
         * 
         * @param value
         *     allowed object is
         *     {@link TYards }
         *     
         */
        public void setYards(TYards value) {
            this.yards = value;
        }

        /**
         * Gets the value of the quays property.
         * 
         * @return
         *     possible object is
         *     {@link TQuays }
         *     
         */
        public TQuays getQuays() {
            return quays;
        }

        /**
         * Sets the value of the quays property.
         * 
         * @param value
         *     allowed object is
         *     {@link TQuays }
         *     
         */
        public void setQuays(TQuays value) {
            this.quays = value;
        }

        /**
         * Gets the value of the routingPoint property.
         * 
         * @return
         *     possible object is
         *     {@link TRoutingPoint }
         *     
         */
        public TRoutingPoint getRoutingPoint() {
            return routingPoint;
        }

        /**
         * Sets the value of the routingPoint property.
         * 
         * @param value
         *     allowed object is
         *     {@link TRoutingPoint }
         *     
         */
        public void setRoutingPoint(TRoutingPoint value) {
            this.routingPoint = value;
        }

        /**
         * Gets the value of the jmsConnection property.
         * 
         * @return
         *     possible object is
         *     {@link TJmsConnection }
         *     
         */
        public TJmsConnection getJmsConnection() {
            return jmsConnection;
        }

        /**
         * Sets the value of the jmsConnection property.
         * 
         * @param value
         *     allowed object is
         *     {@link TJmsConnection }
         *     
         */
        public void setJmsConnection(TJmsConnection value) {
            this.jmsConnection = value;
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
         * Gets the value of the messageClass property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getMessageClass() {
            return messageClass;
        }

        /**
         * Sets the value of the messageClass property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setMessageClass(String value) {
            this.messageClass = value;
        }

        /**
         * Gets the value of the teuCapacityGreen property.
         * 
         * @return
         *     possible object is
         *     {@link Long }
         *     
         */
        public Long getTeuCapacityGreen() {
            return teuCapacityGreen;
        }

        /**
         * Sets the value of the teuCapacityGreen property.
         * 
         * @param value
         *     allowed object is
         *     {@link Long }
         *     
         */
        public void setTeuCapacityGreen(Long value) {
            this.teuCapacityGreen = value;
        }

        /**
         * Gets the value of the teuCapacityRed property.
         * 
         * @return
         *     possible object is
         *     {@link Long }
         *     
         */
        public Long getTeuCapacityRed() {
            return teuCapacityRed;
        }

        /**
         * Sets the value of the teuCapacityRed property.
         * 
         * @param value
         *     allowed object is
         *     {@link Long }
         *     
         */
        public void setTeuCapacityRed(Long value) {
            this.teuCapacityRed = value;
        }

        /**
         * Gets the value of the teuCapacityYellow property.
         * 
         * @return
         *     possible object is
         *     {@link Long }
         *     
         */
        public Long getTeuCapacityYellow() {
            return teuCapacityYellow;
        }

        /**
         * Sets the value of the teuCapacityYellow property.
         * 
         * @param value
         *     allowed object is
         *     {@link Long }
         *     
         */
        public void setTeuCapacityYellow(Long value) {
            this.teuCapacityYellow = value;
        }

        /**
         * Gets the value of the isNonOperational property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getIsNonOperational() {
            return isNonOperational;
        }

        /**
         * Sets the value of the isNonOperational property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setIsNonOperational(String value) {
            this.isNonOperational = value;
        }

        /**
         * Gets the value of the timeZoneId property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getTimeZoneId() {
            return timeZoneId;
        }

        /**
         * Sets the value of the timeZoneId property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setTimeZoneId(String value) {
            this.timeZoneId = value;
        }

    }

}