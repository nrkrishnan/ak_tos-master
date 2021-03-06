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
 * Notice Request
 * 
 * <p>Java class for tNoticeRequest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tNoticeRequest">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="filter" type="{http://www.navis.com/argo}tFilter" minOccurs="0"/>
 *         &lt;element name="message-template" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="business-entity" use="required" type="{http://www.navis.com/argo}tBusinessEntity" />
 *       &lt;attribute name="business-entity-id" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="event-type" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="email-party" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="action">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;enumeration value="EMAIL"/>
 *             &lt;enumeration value="PRINT"/>
 *             &lt;enumeration value="GROOVY"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="email-address" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="suspend-date" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="printer-address" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="sms-address" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="email-language" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="description" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tNoticeRequest", propOrder = {
    "filter",
    "messageTemplate"
})
public class TNoticeRequest {

    protected TFilter filter;
    @XmlElement(name = "message-template")
    protected String messageTemplate;
    @XmlAttribute(name = "business-entity", required = true)
    protected TBusinessEntity businessEntity;
    @XmlAttribute(name = "business-entity-id")
    protected String businessEntityId;
    @XmlAttribute(name = "event-type")
    protected String eventType;
    @XmlAttribute(name = "email-party")
    protected String emailParty;
    @XmlAttribute
    protected String action;
    @XmlAttribute(name = "email-address")
    protected String emailAddress;
    @XmlAttribute(name = "suspend-date")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar suspendDate;
    @XmlAttribute(name = "printer-address")
    protected String printerAddress;
    @XmlAttribute(name = "sms-address")
    protected String smsAddress;
    @XmlAttribute(name = "email-language")
    protected String emailLanguage;
    @XmlAttribute
    protected String description;

    /**
     * Gets the value of the filter property.
     * 
     * @return
     *     possible object is
     *     {@link TFilter }
     *     
     */
    public TFilter getFilter() {
        return filter;
    }

    /**
     * Sets the value of the filter property.
     * 
     * @param value
     *     allowed object is
     *     {@link TFilter }
     *     
     */
    public void setFilter(TFilter value) {
        this.filter = value;
    }

    /**
     * Gets the value of the messageTemplate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMessageTemplate() {
        return messageTemplate;
    }

    /**
     * Sets the value of the messageTemplate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMessageTemplate(String value) {
        this.messageTemplate = value;
    }

    /**
     * Gets the value of the businessEntity property.
     * 
     * @return
     *     possible object is
     *     {@link TBusinessEntity }
     *     
     */
    public TBusinessEntity getBusinessEntity() {
        return businessEntity;
    }

    /**
     * Sets the value of the businessEntity property.
     * 
     * @param value
     *     allowed object is
     *     {@link TBusinessEntity }
     *     
     */
    public void setBusinessEntity(TBusinessEntity value) {
        this.businessEntity = value;
    }

    /**
     * Gets the value of the businessEntityId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBusinessEntityId() {
        return businessEntityId;
    }

    /**
     * Sets the value of the businessEntityId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBusinessEntityId(String value) {
        this.businessEntityId = value;
    }

    /**
     * Gets the value of the eventType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEventType() {
        return eventType;
    }

    /**
     * Sets the value of the eventType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEventType(String value) {
        this.eventType = value;
    }

    /**
     * Gets the value of the emailParty property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEmailParty() {
        return emailParty;
    }

    /**
     * Sets the value of the emailParty property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEmailParty(String value) {
        this.emailParty = value;
    }

    /**
     * Gets the value of the action property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAction() {
        return action;
    }

    /**
     * Sets the value of the action property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAction(String value) {
        this.action = value;
    }

    /**
     * Gets the value of the emailAddress property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEmailAddress() {
        return emailAddress;
    }

    /**
     * Sets the value of the emailAddress property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEmailAddress(String value) {
        this.emailAddress = value;
    }

    /**
     * Gets the value of the suspendDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getSuspendDate() {
        return suspendDate;
    }

    /**
     * Sets the value of the suspendDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setSuspendDate(XMLGregorianCalendar value) {
        this.suspendDate = value;
    }

    /**
     * Gets the value of the printerAddress property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPrinterAddress() {
        return printerAddress;
    }

    /**
     * Sets the value of the printerAddress property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPrinterAddress(String value) {
        this.printerAddress = value;
    }

    /**
     * Gets the value of the smsAddress property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSmsAddress() {
        return smsAddress;
    }

    /**
     * Sets the value of the smsAddress property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSmsAddress(String value) {
        this.smsAddress = value;
    }

    /**
     * Gets the value of the emailLanguage property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEmailLanguage() {
        return emailLanguage;
    }

    /**
     * Sets the value of the emailLanguage property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEmailLanguage(String value) {
        this.emailLanguage = value;
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

}
