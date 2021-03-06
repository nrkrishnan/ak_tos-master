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
 * Session
 * 
 * <p>Java class for tEdiSession complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tEdiSession">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="filter" type="{http://www.navis.com/argo}tFilter" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="mailboxes" type="{http://www.navis.com/argo}tEdiMailBoxes" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="settings" type="{http://www.navis.com/argo}tSettings" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="edi-filters" type="{http://www.navis.com/argo}tEdiFilters" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="extension" type="{http://www.navis.com/argo}tCodeExtension" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="message-class" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="direction" type="{http://www.navis.com/argo}tEdiMessageDirection" />
 *       &lt;attribute name="message-map" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="file-ext" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="is-auto-posted" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="description" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="delimeter" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="life-cycle-state" type="{http://www.navis.com/argo}tLifeCycleStateType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tEdiSession", propOrder = {
    "filter",
    "mailboxes",
    "settings",
    "ediFilters",
    "extension"
})
public class TEdiSession {

    protected List<TFilter> filter;
    protected List<TEdiMailBoxes> mailboxes;
    protected List<TSettings> settings;
    @XmlElement(name = "edi-filters")
    protected List<TEdiFilters> ediFilters;
    protected TCodeExtension extension;
    @XmlAttribute(required = true)
    protected String name;
    @XmlAttribute(name = "message-class")
    protected String messageClass;
    @XmlAttribute
    protected TEdiMessageDirection direction;
    @XmlAttribute(name = "message-map")
    protected String messageMap;
    @XmlAttribute(name = "file-ext")
    protected String fileExt;
    @XmlAttribute(name = "is-auto-posted")
    protected String isAutoPosted;
    @XmlAttribute
    protected String description;
    @XmlAttribute
    protected String delimeter;
    @XmlAttribute(name = "life-cycle-state")
    protected TLifeCycleStateType lifeCycleState;

    /**
     * Gets the value of the filter property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the filter property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFilter().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TFilter }
     * 
     * 
     */
    public List<TFilter> getFilter() {
        if (filter == null) {
            filter = new ArrayList<TFilter>();
        }
        return this.filter;
    }

    /**
     * Gets the value of the mailboxes property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the mailboxes property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMailboxes().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TEdiMailBoxes }
     * 
     * 
     */
    public List<TEdiMailBoxes> getMailboxes() {
        if (mailboxes == null) {
            mailboxes = new ArrayList<TEdiMailBoxes>();
        }
        return this.mailboxes;
    }

    /**
     * Gets the value of the settings property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the settings property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSettings().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TSettings }
     * 
     * 
     */
    public List<TSettings> getSettings() {
        if (settings == null) {
            settings = new ArrayList<TSettings>();
        }
        return this.settings;
    }

    /**
     * Gets the value of the ediFilters property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the ediFilters property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEdiFilters().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TEdiFilters }
     * 
     * 
     */
    public List<TEdiFilters> getEdiFilters() {
        if (ediFilters == null) {
            ediFilters = new ArrayList<TEdiFilters>();
        }
        return this.ediFilters;
    }

    /**
     * Gets the value of the extension property.
     * 
     * @return
     *     possible object is
     *     {@link TCodeExtension }
     *     
     */
    public TCodeExtension getExtension() {
        return extension;
    }

    /**
     * Sets the value of the extension property.
     * 
     * @param value
     *     allowed object is
     *     {@link TCodeExtension }
     *     
     */
    public void setExtension(TCodeExtension value) {
        this.extension = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
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
     * Gets the value of the messageMap property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMessageMap() {
        return messageMap;
    }

    /**
     * Sets the value of the messageMap property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMessageMap(String value) {
        this.messageMap = value;
    }

    /**
     * Gets the value of the fileExt property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFileExt() {
        return fileExt;
    }

    /**
     * Sets the value of the fileExt property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFileExt(String value) {
        this.fileExt = value;
    }

    /**
     * Gets the value of the isAutoPosted property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIsAutoPosted() {
        return isAutoPosted;
    }

    /**
     * Sets the value of the isAutoPosted property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIsAutoPosted(String value) {
        this.isAutoPosted = value;
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
     * Gets the value of the delimeter property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDelimeter() {
        return delimeter;
    }

    /**
     * Sets the value of the delimeter property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDelimeter(String value) {
        this.delimeter = value;
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
