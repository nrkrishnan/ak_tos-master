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
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * Servcie Order
 * 
 * <p>Java class for tServiceOrder complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tServiceOrder">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="bill-of-ladding" type="{http://www.navis.com/argo}tSOBillOfLading" minOccurs="0"/>
 *         &lt;element name="booking" type="{http://www.navis.com/argo}tSOBkg" minOccurs="0"/>
 *         &lt;element name="items" type="{http://www.navis.com/argo}tSOItems" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="service-order-number" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="billing-party" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="order-line" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="start-date" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="completion-date" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="notes" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="status" type="{http://www.navis.com/argo}tServiceOrderStatus" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tServiceOrder", propOrder = {
    "billOfLadding",
    "booking",
    "items"
})
public class TServiceOrder {

    @XmlElement(name = "bill-of-ladding")
    protected TSOBillOfLading billOfLadding;
    protected TSOBkg booking;
    protected TSOItems items;
    @XmlAttribute(name = "service-order-number", required = true)
    protected String serviceOrderNumber;
    @XmlAttribute(name = "billing-party", required = true)
    protected String billingParty;
    @XmlAttribute(name = "order-line")
    protected String orderLine;
    @XmlAttribute(name = "start-date")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar startDate;
    @XmlAttribute(name = "completion-date")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar completionDate;
    @XmlAttribute
    protected String notes;
    @XmlAttribute
    protected TServiceOrderStatus status;

    /**
     * Gets the value of the billOfLadding property.
     * 
     * @return
     *     possible object is
     *     {@link TSOBillOfLading }
     *     
     */
    public TSOBillOfLading getBillOfLadding() {
        return billOfLadding;
    }

    /**
     * Sets the value of the billOfLadding property.
     * 
     * @param value
     *     allowed object is
     *     {@link TSOBillOfLading }
     *     
     */
    public void setBillOfLadding(TSOBillOfLading value) {
        this.billOfLadding = value;
    }

    /**
     * Gets the value of the booking property.
     * 
     * @return
     *     possible object is
     *     {@link TSOBkg }
     *     
     */
    public TSOBkg getBooking() {
        return booking;
    }

    /**
     * Sets the value of the booking property.
     * 
     * @param value
     *     allowed object is
     *     {@link TSOBkg }
     *     
     */
    public void setBooking(TSOBkg value) {
        this.booking = value;
    }

    /**
     * Gets the value of the items property.
     * 
     * @return
     *     possible object is
     *     {@link TSOItems }
     *     
     */
    public TSOItems getItems() {
        return items;
    }

    /**
     * Sets the value of the items property.
     * 
     * @param value
     *     allowed object is
     *     {@link TSOItems }
     *     
     */
    public void setItems(TSOItems value) {
        this.items = value;
    }

    /**
     * Gets the value of the serviceOrderNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getServiceOrderNumber() {
        return serviceOrderNumber;
    }

    /**
     * Sets the value of the serviceOrderNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setServiceOrderNumber(String value) {
        this.serviceOrderNumber = value;
    }

    /**
     * Gets the value of the billingParty property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBillingParty() {
        return billingParty;
    }

    /**
     * Sets the value of the billingParty property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBillingParty(String value) {
        this.billingParty = value;
    }

    /**
     * Gets the value of the orderLine property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOrderLine() {
        return orderLine;
    }

    /**
     * Sets the value of the orderLine property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOrderLine(String value) {
        this.orderLine = value;
    }

    /**
     * Gets the value of the startDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getStartDate() {
        return startDate;
    }

    /**
     * Sets the value of the startDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setStartDate(XMLGregorianCalendar value) {
        this.startDate = value;
    }

    /**
     * Gets the value of the completionDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getCompletionDate() {
        return completionDate;
    }

    /**
     * Sets the value of the completionDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setCompletionDate(XMLGregorianCalendar value) {
        this.completionDate = value;
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
     * Gets the value of the status property.
     * 
     * @return
     *     possible object is
     *     {@link TServiceOrderStatus }
     *     
     */
    public TServiceOrderStatus getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     * 
     * @param value
     *     allowed object is
     *     {@link TServiceOrderStatus }
     *     
     */
    public void setStatus(TServiceOrderStatus value) {
        this.status = value;
    }

}