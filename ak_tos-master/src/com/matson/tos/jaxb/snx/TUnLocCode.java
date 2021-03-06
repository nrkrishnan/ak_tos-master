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
 * Un Locations
 * 
 * <p>Java class for tUnLocCode complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tUnLocCode">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="category" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="is-airport" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="is-port" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="is-rail-terminal" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="is-road-terminal" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="miscellaneous" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="is-multimodal" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="is-border-crossing" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="is-fixed-transport" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="is-function-unknown" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="compass" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="latitude-nor-s" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="longitude-eor-w" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="latitude" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="longitude" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="place-code" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="place-name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="status" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="country" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="sub-division-code" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="remarks" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="life-cycle-state" type="{http://www.navis.com/argo}tLifeCycleStateType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tUnLocCode", propOrder = {
    "category",
    "miscellaneous",
    "compass"
})
public class TUnLocCode {

    protected TUnLocCode.Category category;
    protected TUnLocCode.Miscellaneous miscellaneous;
    protected TUnLocCode.Compass compass;
    @XmlAttribute(required = true)
    protected String id;
    @XmlAttribute(name = "place-code", required = true)
    protected String placeCode;
    @XmlAttribute(name = "place-name", required = true)
    protected String placeName;
    @XmlAttribute(required = true)
    protected String status;
    @XmlAttribute
    protected String country;
    @XmlAttribute(name = "sub-division-code")
    protected String subDivisionCode;
    @XmlAttribute
    protected String remarks;
    @XmlAttribute(name = "life-cycle-state")
    protected TLifeCycleStateType lifeCycleState;

    /**
     * Gets the value of the category property.
     * 
     * @return
     *     possible object is
     *     {@link TUnLocCode.Category }
     *     
     */
    public TUnLocCode.Category getCategory() {
        return category;
    }

    /**
     * Sets the value of the category property.
     * 
     * @param value
     *     allowed object is
     *     {@link TUnLocCode.Category }
     *     
     */
    public void setCategory(TUnLocCode.Category value) {
        this.category = value;
    }

    /**
     * Gets the value of the miscellaneous property.
     * 
     * @return
     *     possible object is
     *     {@link TUnLocCode.Miscellaneous }
     *     
     */
    public TUnLocCode.Miscellaneous getMiscellaneous() {
        return miscellaneous;
    }

    /**
     * Sets the value of the miscellaneous property.
     * 
     * @param value
     *     allowed object is
     *     {@link TUnLocCode.Miscellaneous }
     *     
     */
    public void setMiscellaneous(TUnLocCode.Miscellaneous value) {
        this.miscellaneous = value;
    }

    /**
     * Gets the value of the compass property.
     * 
     * @return
     *     possible object is
     *     {@link TUnLocCode.Compass }
     *     
     */
    public TUnLocCode.Compass getCompass() {
        return compass;
    }

    /**
     * Sets the value of the compass property.
     * 
     * @param value
     *     allowed object is
     *     {@link TUnLocCode.Compass }
     *     
     */
    public void setCompass(TUnLocCode.Compass value) {
        this.compass = value;
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
     * Gets the value of the placeCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPlaceCode() {
        return placeCode;
    }

    /**
     * Sets the value of the placeCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPlaceCode(String value) {
        this.placeCode = value;
    }

    /**
     * Gets the value of the placeName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPlaceName() {
        return placeName;
    }

    /**
     * Sets the value of the placeName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPlaceName(String value) {
        this.placeName = value;
    }

    /**
     * Gets the value of the status property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStatus(String value) {
        this.status = value;
    }

    /**
     * Gets the value of the country property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCountry() {
        return country;
    }

    /**
     * Sets the value of the country property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCountry(String value) {
        this.country = value;
    }

    /**
     * Gets the value of the subDivisionCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSubDivisionCode() {
        return subDivisionCode;
    }

    /**
     * Sets the value of the subDivisionCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSubDivisionCode(String value) {
        this.subDivisionCode = value;
    }

    /**
     * Gets the value of the remarks property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRemarks() {
        return remarks;
    }

    /**
     * Sets the value of the remarks property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRemarks(String value) {
        this.remarks = value;
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


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;attribute name="is-airport" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="is-port" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="is-rail-terminal" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="is-road-terminal" type="{http://www.w3.org/2001/XMLSchema}string" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Category {

        @XmlAttribute(name = "is-airport")
        protected String isAirport;
        @XmlAttribute(name = "is-port")
        protected String isPort;
        @XmlAttribute(name = "is-rail-terminal")
        protected String isRailTerminal;
        @XmlAttribute(name = "is-road-terminal")
        protected String isRoadTerminal;

        /**
         * Gets the value of the isAirport property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getIsAirport() {
            return isAirport;
        }

        /**
         * Sets the value of the isAirport property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setIsAirport(String value) {
            this.isAirport = value;
        }

        /**
         * Gets the value of the isPort property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getIsPort() {
            return isPort;
        }

        /**
         * Sets the value of the isPort property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setIsPort(String value) {
            this.isPort = value;
        }

        /**
         * Gets the value of the isRailTerminal property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getIsRailTerminal() {
            return isRailTerminal;
        }

        /**
         * Sets the value of the isRailTerminal property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setIsRailTerminal(String value) {
            this.isRailTerminal = value;
        }

        /**
         * Gets the value of the isRoadTerminal property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getIsRoadTerminal() {
            return isRoadTerminal;
        }

        /**
         * Sets the value of the isRoadTerminal property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setIsRoadTerminal(String value) {
            this.isRoadTerminal = value;
        }

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
     *       &lt;attribute name="latitude-nor-s" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="longitude-eor-w" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="latitude" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="longitude" type="{http://www.w3.org/2001/XMLSchema}string" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Compass {

        @XmlAttribute(name = "latitude-nor-s", required = true)
        protected String latitudeNorS;
        @XmlAttribute(name = "longitude-eor-w", required = true)
        protected String longitudeEorW;
        @XmlAttribute
        protected String latitude;
        @XmlAttribute
        protected String longitude;

        /**
         * Gets the value of the latitudeNorS property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getLatitudeNorS() {
            return latitudeNorS;
        }

        /**
         * Sets the value of the latitudeNorS property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setLatitudeNorS(String value) {
            this.latitudeNorS = value;
        }

        /**
         * Gets the value of the longitudeEorW property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getLongitudeEorW() {
            return longitudeEorW;
        }

        /**
         * Sets the value of the longitudeEorW property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setLongitudeEorW(String value) {
            this.longitudeEorW = value;
        }

        /**
         * Gets the value of the latitude property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getLatitude() {
            return latitude;
        }

        /**
         * Sets the value of the latitude property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setLatitude(String value) {
            this.latitude = value;
        }

        /**
         * Gets the value of the longitude property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getLongitude() {
            return longitude;
        }

        /**
         * Sets the value of the longitude property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setLongitude(String value) {
            this.longitude = value;
        }

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
     *       &lt;attribute name="is-multimodal" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="is-border-crossing" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="is-fixed-transport" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="is-function-unknown" type="{http://www.w3.org/2001/XMLSchema}string" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Miscellaneous {

        @XmlAttribute(name = "is-multimodal")
        protected String isMultimodal;
        @XmlAttribute(name = "is-border-crossing")
        protected String isBorderCrossing;
        @XmlAttribute(name = "is-fixed-transport")
        protected String isFixedTransport;
        @XmlAttribute(name = "is-function-unknown")
        protected String isFunctionUnknown;

        /**
         * Gets the value of the isMultimodal property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getIsMultimodal() {
            return isMultimodal;
        }

        /**
         * Sets the value of the isMultimodal property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setIsMultimodal(String value) {
            this.isMultimodal = value;
        }

        /**
         * Gets the value of the isBorderCrossing property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getIsBorderCrossing() {
            return isBorderCrossing;
        }

        /**
         * Sets the value of the isBorderCrossing property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setIsBorderCrossing(String value) {
            this.isBorderCrossing = value;
        }

        /**
         * Gets the value of the isFixedTransport property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getIsFixedTransport() {
            return isFixedTransport;
        }

        /**
         * Sets the value of the isFixedTransport property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setIsFixedTransport(String value) {
            this.isFixedTransport = value;
        }

        /**
         * Gets the value of the isFunctionUnknown property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getIsFunctionUnknown() {
            return isFunctionUnknown;
        }

        /**
         * Sets the value of the isFunctionUnknown property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setIsFunctionUnknown(String value) {
            this.isFunctionUnknown = value;
        }

    }

}
