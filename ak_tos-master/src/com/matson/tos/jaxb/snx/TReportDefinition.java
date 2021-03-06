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
import javax.xml.bind.annotation.XmlType;


/**
 * Report Definition
 * 
 * <p>Java class for tReportDefinition complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tReportDefinition">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="roles" type="{http://www.navis.com/argo}tRoles" minOccurs="0"/>
 *         &lt;element name="sort-fields" type="{http://www.navis.com/argo}tSortFields" minOccurs="0"/>
 *         &lt;element name="filter" type="{http://www.navis.com/argo}tFilter" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="report-design" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="description" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="display-title" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="output-type" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="reporting-entity" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tReportDefinition", propOrder = {
    "roles",
    "sortFields",
    "filter"
})
public class TReportDefinition {

    protected TRoles roles;
    @XmlElement(name = "sort-fields")
    protected TSortFields sortFields;
    protected TFilter filter;
    @XmlAttribute(required = true)
    protected String name;
    @XmlAttribute(name = "report-design")
    protected String reportDesign;
    @XmlAttribute
    protected String description;
    @XmlAttribute(name = "display-title")
    protected String displayTitle;
    @XmlAttribute(name = "output-type")
    protected String outputType;
    @XmlAttribute(name = "reporting-entity")
    protected String reportingEntity;

    /**
     * Gets the value of the roles property.
     * 
     * @return
     *     possible object is
     *     {@link TRoles }
     *     
     */
    public TRoles getRoles() {
        return roles;
    }

    /**
     * Sets the value of the roles property.
     * 
     * @param value
     *     allowed object is
     *     {@link TRoles }
     *     
     */
    public void setRoles(TRoles value) {
        this.roles = value;
    }

    /**
     * Gets the value of the sortFields property.
     * 
     * @return
     *     possible object is
     *     {@link TSortFields }
     *     
     */
    public TSortFields getSortFields() {
        return sortFields;
    }

    /**
     * Sets the value of the sortFields property.
     * 
     * @param value
     *     allowed object is
     *     {@link TSortFields }
     *     
     */
    public void setSortFields(TSortFields value) {
        this.sortFields = value;
    }

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
     * Gets the value of the reportDesign property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReportDesign() {
        return reportDesign;
    }

    /**
     * Sets the value of the reportDesign property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReportDesign(String value) {
        this.reportDesign = value;
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
     * Gets the value of the displayTitle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDisplayTitle() {
        return displayTitle;
    }

    /**
     * Sets the value of the displayTitle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDisplayTitle(String value) {
        this.displayTitle = value;
    }

    /**
     * Gets the value of the outputType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOutputType() {
        return outputType;
    }

    /**
     * Sets the value of the outputType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOutputType(String value) {
        this.outputType = value;
    }

    /**
     * Gets the value of the reportingEntity property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReportingEntity() {
        return reportingEntity;
    }

    /**
     * Sets the value of the reportingEntity property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReportingEntity(String value) {
        this.reportingEntity = value;
    }

}
