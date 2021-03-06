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
 * Shipping Line Operator
 * 
 * <p>Java class for tBookingRule complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tBookingRule">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="booking-unique" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="booking-usage" use="required">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;enumeration value="REQUIRED"/>
 *             &lt;enumeration value="OPTIONAL"/>
 *             &lt;enumeration value="NOTUSED"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="booking-roll" use="required">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;enumeration value="NO"/>
 *             &lt;enumeration value="ROLL"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="booking-adhoc" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="is-booking-not-validated" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="dot-cert-nbr" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="notes" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="gen-pin-nbr" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="use-pin-nbr" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="roll-late-ctr" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="bl-unique" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="order-item-not-unique" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="is-order-nbr-unique" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tBookingRule")
public class TBookingRule {

    @XmlAttribute(name = "booking-unique")
    protected String bookingUnique;
    @XmlAttribute(name = "booking-usage", required = true)
    protected String bookingUsage;
    @XmlAttribute(name = "booking-roll", required = true)
    protected String bookingRoll;
    @XmlAttribute(name = "booking-adhoc")
    protected String bookingAdhoc;
    @XmlAttribute(name = "is-booking-not-validated")
    protected String isBookingNotValidated;
    @XmlAttribute(name = "dot-cert-nbr")
    protected String dotCertNbr;
    @XmlAttribute
    protected String notes;
    @XmlAttribute(name = "gen-pin-nbr")
    protected String genPinNbr;
    @XmlAttribute(name = "use-pin-nbr")
    protected String usePinNbr;
    @XmlAttribute(name = "roll-late-ctr")
    protected String rollLateCtr;
    @XmlAttribute(name = "bl-unique")
    protected String blUnique;
    @XmlAttribute(name = "order-item-not-unique")
    protected String orderItemNotUnique;
    @XmlAttribute(name = "is-order-nbr-unique")
    protected String isOrderNbrUnique;

    /**
     * Gets the value of the bookingUnique property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBookingUnique() {
        return bookingUnique;
    }

    /**
     * Sets the value of the bookingUnique property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBookingUnique(String value) {
        this.bookingUnique = value;
    }

    /**
     * Gets the value of the bookingUsage property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBookingUsage() {
        return bookingUsage;
    }

    /**
     * Sets the value of the bookingUsage property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBookingUsage(String value) {
        this.bookingUsage = value;
    }

    /**
     * Gets the value of the bookingRoll property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBookingRoll() {
        return bookingRoll;
    }

    /**
     * Sets the value of the bookingRoll property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBookingRoll(String value) {
        this.bookingRoll = value;
    }

    /**
     * Gets the value of the bookingAdhoc property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBookingAdhoc() {
        return bookingAdhoc;
    }

    /**
     * Sets the value of the bookingAdhoc property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBookingAdhoc(String value) {
        this.bookingAdhoc = value;
    }

    /**
     * Gets the value of the isBookingNotValidated property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIsBookingNotValidated() {
        return isBookingNotValidated;
    }

    /**
     * Sets the value of the isBookingNotValidated property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIsBookingNotValidated(String value) {
        this.isBookingNotValidated = value;
    }

    /**
     * Gets the value of the dotCertNbr property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDotCertNbr() {
        return dotCertNbr;
    }

    /**
     * Sets the value of the dotCertNbr property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDotCertNbr(String value) {
        this.dotCertNbr = value;
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

    /**
     * Gets the value of the genPinNbr property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGenPinNbr() {
        return genPinNbr;
    }

    /**
     * Sets the value of the genPinNbr property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGenPinNbr(String value) {
        this.genPinNbr = value;
    }

    /**
     * Gets the value of the usePinNbr property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUsePinNbr() {
        return usePinNbr;
    }

    /**
     * Sets the value of the usePinNbr property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUsePinNbr(String value) {
        this.usePinNbr = value;
    }

    /**
     * Gets the value of the rollLateCtr property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRollLateCtr() {
        return rollLateCtr;
    }

    /**
     * Sets the value of the rollLateCtr property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRollLateCtr(String value) {
        this.rollLateCtr = value;
    }

    /**
     * Gets the value of the blUnique property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBlUnique() {
        return blUnique;
    }

    /**
     * Sets the value of the blUnique property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBlUnique(String value) {
        this.blUnique = value;
    }

    /**
     * Gets the value of the orderItemNotUnique property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOrderItemNotUnique() {
        return orderItemNotUnique;
    }

    /**
     * Sets the value of the orderItemNotUnique property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOrderItemNotUnique(String value) {
        this.orderItemNotUnique = value;
    }

    /**
     * Gets the value of the isOrderNbrUnique property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIsOrderNbrUnique() {
        return isOrderNbrUnique;
    }

    /**
     * Sets the value of the isOrderNbrUnique property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIsOrderNbrUnique(String value) {
        this.isOrderNbrUnique = value;
    }

}
