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
 * Metafield Override
 * 
 * <p>Java class for tMetafieldOverride complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tMetafieldOverride">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="metafield-lov" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="value" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="description" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="scope-level" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="importance" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="scope-id" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="short-name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="long-name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="help-label" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="group-id" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="widget-type" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="user-unit" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="max-chars" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tMetafieldOverride", propOrder = {
    "metafieldLov"
})
public class TMetafieldOverride {

    @XmlElement(name = "metafield-lov")
    protected List<TMetafieldOverride.MetafieldLov> metafieldLov;
    @XmlAttribute(required = true)
    protected String id;
    @XmlAttribute(name = "scope-level", required = true)
    protected String scopeLevel;
    @XmlAttribute(required = true)
    protected String importance;
    @XmlAttribute(name = "scope-id")
    protected String scopeId;
    @XmlAttribute(name = "short-name")
    protected String shortName;
    @XmlAttribute(name = "long-name")
    protected String longName;
    @XmlAttribute(name = "help-label")
    protected String helpLabel;
    @XmlAttribute(name = "group-id")
    protected String groupId;
    @XmlAttribute(name = "widget-type")
    protected String widgetType;
    @XmlAttribute(name = "user-unit")
    protected String userUnit;
    @XmlAttribute(name = "max-chars")
    protected String maxChars;

    /**
     * Gets the value of the metafieldLov property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the metafieldLov property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMetafieldLov().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TMetafieldOverride.MetafieldLov }
     * 
     * 
     */
    public List<TMetafieldOverride.MetafieldLov> getMetafieldLov() {
        if (metafieldLov == null) {
            metafieldLov = new ArrayList<TMetafieldOverride.MetafieldLov>();
        }
        return this.metafieldLov;
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
     * Gets the value of the scopeLevel property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getScopeLevel() {
        return scopeLevel;
    }

    /**
     * Sets the value of the scopeLevel property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setScopeLevel(String value) {
        this.scopeLevel = value;
    }

    /**
     * Gets the value of the importance property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getImportance() {
        return importance;
    }

    /**
     * Sets the value of the importance property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setImportance(String value) {
        this.importance = value;
    }

    /**
     * Gets the value of the scopeId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getScopeId() {
        return scopeId;
    }

    /**
     * Sets the value of the scopeId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setScopeId(String value) {
        this.scopeId = value;
    }

    /**
     * Gets the value of the shortName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * Sets the value of the shortName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setShortName(String value) {
        this.shortName = value;
    }

    /**
     * Gets the value of the longName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLongName() {
        return longName;
    }

    /**
     * Sets the value of the longName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLongName(String value) {
        this.longName = value;
    }

    /**
     * Gets the value of the helpLabel property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHelpLabel() {
        return helpLabel;
    }

    /**
     * Sets the value of the helpLabel property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHelpLabel(String value) {
        this.helpLabel = value;
    }

    /**
     * Gets the value of the groupId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * Sets the value of the groupId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGroupId(String value) {
        this.groupId = value;
    }

    /**
     * Gets the value of the widgetType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWidgetType() {
        return widgetType;
    }

    /**
     * Sets the value of the widgetType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWidgetType(String value) {
        this.widgetType = value;
    }

    /**
     * Gets the value of the userUnit property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUserUnit() {
        return userUnit;
    }

    /**
     * Sets the value of the userUnit property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUserUnit(String value) {
        this.userUnit = value;
    }

    /**
     * Gets the value of the maxChars property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMaxChars() {
        return maxChars;
    }

    /**
     * Sets the value of the maxChars property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMaxChars(String value) {
        this.maxChars = value;
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
     *       &lt;attribute name="value" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="description" type="{http://www.w3.org/2001/XMLSchema}string" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class MetafieldLov {

        @XmlAttribute
        protected String value;
        @XmlAttribute
        protected String description;

        /**
         * Gets the value of the value property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getValue() {
            return value;
        }

        /**
         * Sets the value of the value property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setValue(String value) {
            this.value = value;
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

}
