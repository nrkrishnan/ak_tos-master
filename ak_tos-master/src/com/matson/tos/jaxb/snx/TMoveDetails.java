//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.5-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.08.11 at 01:33:48 PM GMT-10:00 
//


package com.matson.tos.jaxb.snx;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * Details about the execution of a move
 * 
 * <p>Java class for tMoveDetails complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tMoveDetails">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="meters-to-start" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *       &lt;attribute name="meters-of-carry" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *       &lt;attribute name="time-carry-complete" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="time-dispatch" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="time-fetch" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="time-discharge" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="time-put" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="time-carry-che-fetch-ready" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="time-carry-che-put-ready" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="time-carry-che-dispatch" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="rehandle-count" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *       &lt;attribute name="was-twin-fetch" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="was-twin-carry" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="was-twin-put" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="restow-account" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="service-order" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="restow-reason" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="che-fetch" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="che-carry" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="che-put" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="che-fetch-login-name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="che-carry-login-name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="che-put-login-name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tMoveDetails")
public class TMoveDetails {

    @XmlAttribute(name = "meters-to-start")
    protected BigInteger metersToStart;
    @XmlAttribute(name = "meters-of-carry")
    protected BigInteger metersOfCarry;
    @XmlAttribute(name = "time-carry-complete")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar timeCarryComplete;
    @XmlAttribute(name = "time-dispatch")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar timeDispatch;
    @XmlAttribute(name = "time-fetch")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar timeFetch;
    @XmlAttribute(name = "time-discharge")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar timeDischarge;
    @XmlAttribute(name = "time-put")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar timePut;
    @XmlAttribute(name = "time-carry-che-fetch-ready")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar timeCarryCheFetchReady;
    @XmlAttribute(name = "time-carry-che-put-ready")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar timeCarryChePutReady;
    @XmlAttribute(name = "time-carry-che-dispatch")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar timeCarryCheDispatch;
    @XmlAttribute(name = "rehandle-count")
    protected BigInteger rehandleCount;
    @XmlAttribute(name = "was-twin-fetch")
    protected String wasTwinFetch;
    @XmlAttribute(name = "was-twin-carry")
    protected String wasTwinCarry;
    @XmlAttribute(name = "was-twin-put")
    protected String wasTwinPut;
    @XmlAttribute(name = "restow-account")
    protected String restowAccount;
    @XmlAttribute(name = "service-order")
    protected String serviceOrder;
    @XmlAttribute(name = "restow-reason")
    protected String restowReason;
    @XmlAttribute(name = "che-fetch")
    protected String cheFetch;
    @XmlAttribute(name = "che-carry")
    protected String cheCarry;
    @XmlAttribute(name = "che-put")
    protected String chePut;
    @XmlAttribute(name = "che-fetch-login-name")
    protected String cheFetchLoginName;
    @XmlAttribute(name = "che-carry-login-name")
    protected String cheCarryLoginName;
    @XmlAttribute(name = "che-put-login-name")
    protected String chePutLoginName;

    /**
     * Gets the value of the metersToStart property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getMetersToStart() {
        return metersToStart;
    }

    /**
     * Sets the value of the metersToStart property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setMetersToStart(BigInteger value) {
        this.metersToStart = value;
    }

    /**
     * Gets the value of the metersOfCarry property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getMetersOfCarry() {
        return metersOfCarry;
    }

    /**
     * Sets the value of the metersOfCarry property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setMetersOfCarry(BigInteger value) {
        this.metersOfCarry = value;
    }

    /**
     * Gets the value of the timeCarryComplete property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getTimeCarryComplete() {
        return timeCarryComplete;
    }

    /**
     * Sets the value of the timeCarryComplete property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setTimeCarryComplete(XMLGregorianCalendar value) {
        this.timeCarryComplete = value;
    }

    /**
     * Gets the value of the timeDispatch property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getTimeDispatch() {
        return timeDispatch;
    }

    /**
     * Sets the value of the timeDispatch property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setTimeDispatch(XMLGregorianCalendar value) {
        this.timeDispatch = value;
    }

    /**
     * Gets the value of the timeFetch property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getTimeFetch() {
        return timeFetch;
    }

    /**
     * Sets the value of the timeFetch property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setTimeFetch(XMLGregorianCalendar value) {
        this.timeFetch = value;
    }

    /**
     * Gets the value of the timeDischarge property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getTimeDischarge() {
        return timeDischarge;
    }

    /**
     * Sets the value of the timeDischarge property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setTimeDischarge(XMLGregorianCalendar value) {
        this.timeDischarge = value;
    }

    /**
     * Gets the value of the timePut property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getTimePut() {
        return timePut;
    }

    /**
     * Sets the value of the timePut property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setTimePut(XMLGregorianCalendar value) {
        this.timePut = value;
    }

    /**
     * Gets the value of the timeCarryCheFetchReady property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getTimeCarryCheFetchReady() {
        return timeCarryCheFetchReady;
    }

    /**
     * Sets the value of the timeCarryCheFetchReady property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setTimeCarryCheFetchReady(XMLGregorianCalendar value) {
        this.timeCarryCheFetchReady = value;
    }

    /**
     * Gets the value of the timeCarryChePutReady property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getTimeCarryChePutReady() {
        return timeCarryChePutReady;
    }

    /**
     * Sets the value of the timeCarryChePutReady property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setTimeCarryChePutReady(XMLGregorianCalendar value) {
        this.timeCarryChePutReady = value;
    }

    /**
     * Gets the value of the timeCarryCheDispatch property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getTimeCarryCheDispatch() {
        return timeCarryCheDispatch;
    }

    /**
     * Sets the value of the timeCarryCheDispatch property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setTimeCarryCheDispatch(XMLGregorianCalendar value) {
        this.timeCarryCheDispatch = value;
    }

    /**
     * Gets the value of the rehandleCount property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getRehandleCount() {
        return rehandleCount;
    }

    /**
     * Sets the value of the rehandleCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setRehandleCount(BigInteger value) {
        this.rehandleCount = value;
    }

    /**
     * Gets the value of the wasTwinFetch property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWasTwinFetch() {
        return wasTwinFetch;
    }

    /**
     * Sets the value of the wasTwinFetch property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWasTwinFetch(String value) {
        this.wasTwinFetch = value;
    }

    /**
     * Gets the value of the wasTwinCarry property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWasTwinCarry() {
        return wasTwinCarry;
    }

    /**
     * Sets the value of the wasTwinCarry property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWasTwinCarry(String value) {
        this.wasTwinCarry = value;
    }

    /**
     * Gets the value of the wasTwinPut property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWasTwinPut() {
        return wasTwinPut;
    }

    /**
     * Sets the value of the wasTwinPut property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWasTwinPut(String value) {
        this.wasTwinPut = value;
    }

    /**
     * Gets the value of the restowAccount property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRestowAccount() {
        return restowAccount;
    }

    /**
     * Sets the value of the restowAccount property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRestowAccount(String value) {
        this.restowAccount = value;
    }

    /**
     * Gets the value of the serviceOrder property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getServiceOrder() {
        return serviceOrder;
    }

    /**
     * Sets the value of the serviceOrder property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setServiceOrder(String value) {
        this.serviceOrder = value;
    }

    /**
     * Gets the value of the restowReason property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRestowReason() {
        return restowReason;
    }

    /**
     * Sets the value of the restowReason property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRestowReason(String value) {
        this.restowReason = value;
    }

    /**
     * Gets the value of the cheFetch property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCheFetch() {
        return cheFetch;
    }

    /**
     * Sets the value of the cheFetch property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCheFetch(String value) {
        this.cheFetch = value;
    }

    /**
     * Gets the value of the cheCarry property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCheCarry() {
        return cheCarry;
    }

    /**
     * Sets the value of the cheCarry property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCheCarry(String value) {
        this.cheCarry = value;
    }

    /**
     * Gets the value of the chePut property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getChePut() {
        return chePut;
    }

    /**
     * Sets the value of the chePut property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setChePut(String value) {
        this.chePut = value;
    }

    /**
     * Gets the value of the cheFetchLoginName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCheFetchLoginName() {
        return cheFetchLoginName;
    }

    /**
     * Sets the value of the cheFetchLoginName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCheFetchLoginName(String value) {
        this.cheFetchLoginName = value;
    }

    /**
     * Gets the value of the cheCarryLoginName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCheCarryLoginName() {
        return cheCarryLoginName;
    }

    /**
     * Sets the value of the cheCarryLoginName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCheCarryLoginName(String value) {
        this.cheCarryLoginName = value;
    }

    /**
     * Gets the value of the chePutLoginName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getChePutLoginName() {
        return chePutLoginName;
    }

    /**
     * Sets the value of the chePutLoginName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setChePutLoginName(String value) {
        this.chePutLoginName = value;
    }

}
