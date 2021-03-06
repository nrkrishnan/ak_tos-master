//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.5-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.08.11 at 01:33:48 PM GMT-10:00 
//


package com.matson.tos.jaxb.snx;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * Edo items.
 * 
 * <p>Java class for tEqOrderItems complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tEqOrderItems">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="item" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="serial-ranges" type="{http://www.navis.com/argo}tSerialRanges" minOccurs="0"/>
 *                   &lt;element name="reserved-empty-containers" type="{http://www.navis.com/argo}tResMtyCtrs" minOccurs="0"/>
 *                   &lt;element name="hazards" type="{http://www.navis.com/argo}tHazards" minOccurs="0"/>
 *                 &lt;/sequence>
 *                 &lt;attribute name="quantity" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *                 &lt;attribute name="eq-iso-group" use="required" type="{http://www.navis.com/argo}tIsoGroup" />
 *                 &lt;attribute name="eq-size" use="required" type="{http://www.navis.com/argo}tEquipNominalLength" />
 *                 &lt;attribute name="eq-height" use="required" type="{http://www.navis.com/argo}tEquipNominalHeight" />
 *                 &lt;attribute name="tally-limit" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *                 &lt;attribute name="equipment-type" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="ear-csc-exprn-dt" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *                 &lt;attribute name="ear-mfg-dt" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *                 &lt;attribute name="lat-mfg-dt" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *                 &lt;attribute name="eq-grade" type="{http://www.w3.org/2001/XMLSchema}NCName" />
 *                 &lt;attribute name="acc-type" type="{http://www.navis.com/argo}tAccType" />
 *                 &lt;attribute name="tare-wt-max" type="{http://www.w3.org/2001/XMLSchema}double" />
 *                 &lt;attribute name="eq-material" type="{http://www.navis.com/argo}tMaterial" />
 *                 &lt;attribute name="feature-id" type="{http://www.w3.org/2001/XMLSchema}NCName" />
 *                 &lt;attribute name="remarks" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="safe-wt-min" type="{http://www.w3.org/2001/XMLSchema}double" />
 *                 &lt;attribute name="req-equip-condition" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="is-powered" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="temp-reqrd" type="{http://www.w3.org/2001/XMLSchema}decimal" />
 *                 &lt;attribute name="seq-nbr" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *                 &lt;attribute name="eq-iso-group-description" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="equip-type-description" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="created-by" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="created-date" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *                 &lt;attribute name="modified-by" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="modified-date" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
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
@XmlType(name = "tEqOrderItems", propOrder = {
    "item"
})
public class TEqOrderItems {

    protected List<TEqOrderItems.Item> item;

    /**
     * Gets the value of the item property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the item property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getItem().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TEqOrderItems.Item }
     * 
     * 
     */
    public List<TEqOrderItems.Item> getItem() {
        if (item == null) {
            item = new ArrayList<TEqOrderItems.Item>();
        }
        return this.item;
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
     *         &lt;element name="serial-ranges" type="{http://www.navis.com/argo}tSerialRanges" minOccurs="0"/>
     *         &lt;element name="reserved-empty-containers" type="{http://www.navis.com/argo}tResMtyCtrs" minOccurs="0"/>
     *         &lt;element name="hazards" type="{http://www.navis.com/argo}tHazards" minOccurs="0"/>
     *       &lt;/sequence>
     *       &lt;attribute name="quantity" type="{http://www.w3.org/2001/XMLSchema}integer" />
     *       &lt;attribute name="eq-iso-group" use="required" type="{http://www.navis.com/argo}tIsoGroup" />
     *       &lt;attribute name="eq-size" use="required" type="{http://www.navis.com/argo}tEquipNominalLength" />
     *       &lt;attribute name="eq-height" use="required" type="{http://www.navis.com/argo}tEquipNominalHeight" />
     *       &lt;attribute name="tally-limit" type="{http://www.w3.org/2001/XMLSchema}integer" />
     *       &lt;attribute name="equipment-type" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="ear-csc-exprn-dt" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
     *       &lt;attribute name="ear-mfg-dt" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
     *       &lt;attribute name="lat-mfg-dt" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
     *       &lt;attribute name="eq-grade" type="{http://www.w3.org/2001/XMLSchema}NCName" />
     *       &lt;attribute name="acc-type" type="{http://www.navis.com/argo}tAccType" />
     *       &lt;attribute name="tare-wt-max" type="{http://www.w3.org/2001/XMLSchema}double" />
     *       &lt;attribute name="eq-material" type="{http://www.navis.com/argo}tMaterial" />
     *       &lt;attribute name="feature-id" type="{http://www.w3.org/2001/XMLSchema}NCName" />
     *       &lt;attribute name="remarks" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="safe-wt-min" type="{http://www.w3.org/2001/XMLSchema}double" />
     *       &lt;attribute name="req-equip-condition" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="is-powered" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="temp-reqrd" type="{http://www.w3.org/2001/XMLSchema}decimal" />
     *       &lt;attribute name="seq-nbr" type="{http://www.w3.org/2001/XMLSchema}integer" />
     *       &lt;attribute name="eq-iso-group-description" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="equip-type-description" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="created-by" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="created-date" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
     *       &lt;attribute name="modified-by" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="modified-date" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "serialRanges",
        "reservedEmptyContainers",
        "hazards"
    })
    public static class Item {

        @XmlElement(name = "serial-ranges")
        protected TSerialRanges serialRanges;
        @XmlElement(name = "reserved-empty-containers")
        protected TResMtyCtrs reservedEmptyContainers;
        protected THazards hazards;
        @XmlAttribute
        protected BigInteger quantity;
        @XmlAttribute(name = "eq-iso-group", required = true)
        protected TIsoGroup eqIsoGroup;
        @XmlAttribute(name = "eq-size", required = true)
        protected TEquipNominalLength eqSize;
        @XmlAttribute(name = "eq-height", required = true)
        protected TEquipNominalHeight eqHeight;
        @XmlAttribute(name = "tally-limit")
        protected BigInteger tallyLimit;
        @XmlAttribute(name = "equipment-type")
        protected String equipmentType;
        @XmlAttribute(name = "ear-csc-exprn-dt")
        @XmlSchemaType(name = "dateTime")
        protected XMLGregorianCalendar earCscExprnDt;
        @XmlAttribute(name = "ear-mfg-dt")
        @XmlSchemaType(name = "dateTime")
        protected XMLGregorianCalendar earMfgDt;
        @XmlAttribute(name = "lat-mfg-dt")
        @XmlSchemaType(name = "dateTime")
        protected XMLGregorianCalendar latMfgDt;
        @XmlAttribute(name = "eq-grade")
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        @XmlSchemaType(name = "NCName")
        protected String eqGrade;
        @XmlAttribute(name = "acc-type")
        protected TAccType accType;
        @XmlAttribute(name = "tare-wt-max")
        protected Double tareWtMax;
        @XmlAttribute(name = "eq-material")
        protected TMaterial eqMaterial;
        @XmlAttribute(name = "feature-id")
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        @XmlSchemaType(name = "NCName")
        protected String featureId;
        @XmlAttribute
        protected String remarks;
        @XmlAttribute(name = "safe-wt-min")
        protected Double safeWtMin;
        @XmlAttribute(name = "req-equip-condition")
        protected String reqEquipCondition;
        @XmlAttribute(name = "is-powered")
        protected String isPowered;
        @XmlAttribute(name = "temp-reqrd")
        protected BigDecimal tempReqrd;
        @XmlAttribute(name = "seq-nbr")
        protected BigInteger seqNbr;
        @XmlAttribute(name = "eq-iso-group-description")
        protected String eqIsoGroupDescription;
        @XmlAttribute(name = "equip-type-description")
        protected String equipTypeDescription;
        @XmlAttribute(name = "created-by")
        protected String createdBy;
        @XmlAttribute(name = "created-date")
        @XmlSchemaType(name = "dateTime")
        protected XMLGregorianCalendar createdDate;
        @XmlAttribute(name = "modified-by")
        protected String modifiedBy;
        @XmlAttribute(name = "modified-date")
        @XmlSchemaType(name = "dateTime")
        protected XMLGregorianCalendar modifiedDate;

        /**
         * Gets the value of the serialRanges property.
         * 
         * @return
         *     possible object is
         *     {@link TSerialRanges }
         *     
         */
        public TSerialRanges getSerialRanges() {
            return serialRanges;
        }

        /**
         * Sets the value of the serialRanges property.
         * 
         * @param value
         *     allowed object is
         *     {@link TSerialRanges }
         *     
         */
        public void setSerialRanges(TSerialRanges value) {
            this.serialRanges = value;
        }

        /**
         * Gets the value of the reservedEmptyContainers property.
         * 
         * @return
         *     possible object is
         *     {@link TResMtyCtrs }
         *     
         */
        public TResMtyCtrs getReservedEmptyContainers() {
            return reservedEmptyContainers;
        }

        /**
         * Sets the value of the reservedEmptyContainers property.
         * 
         * @param value
         *     allowed object is
         *     {@link TResMtyCtrs }
         *     
         */
        public void setReservedEmptyContainers(TResMtyCtrs value) {
            this.reservedEmptyContainers = value;
        }

        /**
         * Gets the value of the hazards property.
         * 
         * @return
         *     possible object is
         *     {@link THazards }
         *     
         */
        public THazards getHazards() {
            return hazards;
        }

        /**
         * Sets the value of the hazards property.
         * 
         * @param value
         *     allowed object is
         *     {@link THazards }
         *     
         */
        public void setHazards(THazards value) {
            this.hazards = value;
        }

        /**
         * Gets the value of the quantity property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getQuantity() {
            return quantity;
        }

        /**
         * Sets the value of the quantity property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setQuantity(BigInteger value) {
            this.quantity = value;
        }

        /**
         * Gets the value of the eqIsoGroup property.
         * 
         * @return
         *     possible object is
         *     {@link TIsoGroup }
         *     
         */
        public TIsoGroup getEqIsoGroup() {
            return eqIsoGroup;
        }

        /**
         * Sets the value of the eqIsoGroup property.
         * 
         * @param value
         *     allowed object is
         *     {@link TIsoGroup }
         *     
         */
        public void setEqIsoGroup(TIsoGroup value) {
            this.eqIsoGroup = value;
        }

        /**
         * Gets the value of the eqSize property.
         * 
         * @return
         *     possible object is
         *     {@link TEquipNominalLength }
         *     
         */
        public TEquipNominalLength getEqSize() {
            return eqSize;
        }

        /**
         * Sets the value of the eqSize property.
         * 
         * @param value
         *     allowed object is
         *     {@link TEquipNominalLength }
         *     
         */
        public void setEqSize(TEquipNominalLength value) {
            this.eqSize = value;
        }

        /**
         * Gets the value of the eqHeight property.
         * 
         * @return
         *     possible object is
         *     {@link TEquipNominalHeight }
         *     
         */
        public TEquipNominalHeight getEqHeight() {
            return eqHeight;
        }

        /**
         * Sets the value of the eqHeight property.
         * 
         * @param value
         *     allowed object is
         *     {@link TEquipNominalHeight }
         *     
         */
        public void setEqHeight(TEquipNominalHeight value) {
            this.eqHeight = value;
        }

        /**
         * Gets the value of the tallyLimit property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getTallyLimit() {
            return tallyLimit;
        }

        /**
         * Sets the value of the tallyLimit property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setTallyLimit(BigInteger value) {
            this.tallyLimit = value;
        }

        /**
         * Gets the value of the equipmentType property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getEquipmentType() {
            return equipmentType;
        }

        /**
         * Sets the value of the equipmentType property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setEquipmentType(String value) {
            this.equipmentType = value;
        }

        /**
         * Gets the value of the earCscExprnDt property.
         * 
         * @return
         *     possible object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public XMLGregorianCalendar getEarCscExprnDt() {
            return earCscExprnDt;
        }

        /**
         * Sets the value of the earCscExprnDt property.
         * 
         * @param value
         *     allowed object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public void setEarCscExprnDt(XMLGregorianCalendar value) {
            this.earCscExprnDt = value;
        }

        /**
         * Gets the value of the earMfgDt property.
         * 
         * @return
         *     possible object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public XMLGregorianCalendar getEarMfgDt() {
            return earMfgDt;
        }

        /**
         * Sets the value of the earMfgDt property.
         * 
         * @param value
         *     allowed object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public void setEarMfgDt(XMLGregorianCalendar value) {
            this.earMfgDt = value;
        }

        /**
         * Gets the value of the latMfgDt property.
         * 
         * @return
         *     possible object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public XMLGregorianCalendar getLatMfgDt() {
            return latMfgDt;
        }

        /**
         * Sets the value of the latMfgDt property.
         * 
         * @param value
         *     allowed object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public void setLatMfgDt(XMLGregorianCalendar value) {
            this.latMfgDt = value;
        }

        /**
         * Gets the value of the eqGrade property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getEqGrade() {
            return eqGrade;
        }

        /**
         * Sets the value of the eqGrade property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setEqGrade(String value) {
            this.eqGrade = value;
        }

        /**
         * Gets the value of the accType property.
         * 
         * @return
         *     possible object is
         *     {@link TAccType }
         *     
         */
        public TAccType getAccType() {
            return accType;
        }

        /**
         * Sets the value of the accType property.
         * 
         * @param value
         *     allowed object is
         *     {@link TAccType }
         *     
         */
        public void setAccType(TAccType value) {
            this.accType = value;
        }

        /**
         * Gets the value of the tareWtMax property.
         * 
         * @return
         *     possible object is
         *     {@link Double }
         *     
         */
        public Double getTareWtMax() {
            return tareWtMax;
        }

        /**
         * Sets the value of the tareWtMax property.
         * 
         * @param value
         *     allowed object is
         *     {@link Double }
         *     
         */
        public void setTareWtMax(Double value) {
            this.tareWtMax = value;
        }

        /**
         * Gets the value of the eqMaterial property.
         * 
         * @return
         *     possible object is
         *     {@link TMaterial }
         *     
         */
        public TMaterial getEqMaterial() {
            return eqMaterial;
        }

        /**
         * Sets the value of the eqMaterial property.
         * 
         * @param value
         *     allowed object is
         *     {@link TMaterial }
         *     
         */
        public void setEqMaterial(TMaterial value) {
            this.eqMaterial = value;
        }

        /**
         * Gets the value of the featureId property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getFeatureId() {
            return featureId;
        }

        /**
         * Sets the value of the featureId property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setFeatureId(String value) {
            this.featureId = value;
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
         * Gets the value of the safeWtMin property.
         * 
         * @return
         *     possible object is
         *     {@link Double }
         *     
         */
        public Double getSafeWtMin() {
            return safeWtMin;
        }

        /**
         * Sets the value of the safeWtMin property.
         * 
         * @param value
         *     allowed object is
         *     {@link Double }
         *     
         */
        public void setSafeWtMin(Double value) {
            this.safeWtMin = value;
        }

        /**
         * Gets the value of the reqEquipCondition property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getReqEquipCondition() {
            return reqEquipCondition;
        }

        /**
         * Sets the value of the reqEquipCondition property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setReqEquipCondition(String value) {
            this.reqEquipCondition = value;
        }

        /**
         * Gets the value of the isPowered property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getIsPowered() {
            return isPowered;
        }

        /**
         * Sets the value of the isPowered property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setIsPowered(String value) {
            this.isPowered = value;
        }

        /**
         * Gets the value of the tempReqrd property.
         * 
         * @return
         *     possible object is
         *     {@link BigDecimal }
         *     
         */
        public BigDecimal getTempReqrd() {
            return tempReqrd;
        }

        /**
         * Sets the value of the tempReqrd property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigDecimal }
         *     
         */
        public void setTempReqrd(BigDecimal value) {
            this.tempReqrd = value;
        }

        /**
         * Gets the value of the seqNbr property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getSeqNbr() {
            return seqNbr;
        }

        /**
         * Sets the value of the seqNbr property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setSeqNbr(BigInteger value) {
            this.seqNbr = value;
        }

        /**
         * Gets the value of the eqIsoGroupDescription property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getEqIsoGroupDescription() {
            return eqIsoGroupDescription;
        }

        /**
         * Sets the value of the eqIsoGroupDescription property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setEqIsoGroupDescription(String value) {
            this.eqIsoGroupDescription = value;
        }

        /**
         * Gets the value of the equipTypeDescription property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getEquipTypeDescription() {
            return equipTypeDescription;
        }

        /**
         * Sets the value of the equipTypeDescription property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setEquipTypeDescription(String value) {
            this.equipTypeDescription = value;
        }

        /**
         * Gets the value of the createdBy property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getCreatedBy() {
            return createdBy;
        }

        /**
         * Sets the value of the createdBy property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setCreatedBy(String value) {
            this.createdBy = value;
        }

        /**
         * Gets the value of the createdDate property.
         * 
         * @return
         *     possible object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public XMLGregorianCalendar getCreatedDate() {
            return createdDate;
        }

        /**
         * Sets the value of the createdDate property.
         * 
         * @param value
         *     allowed object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public void setCreatedDate(XMLGregorianCalendar value) {
            this.createdDate = value;
        }

        /**
         * Gets the value of the modifiedBy property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getModifiedBy() {
            return modifiedBy;
        }

        /**
         * Sets the value of the modifiedBy property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setModifiedBy(String value) {
            this.modifiedBy = value;
        }

        /**
         * Gets the value of the modifiedDate property.
         * 
         * @return
         *     possible object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public XMLGregorianCalendar getModifiedDate() {
            return modifiedDate;
        }

        /**
         * Sets the value of the modifiedDate property.
         * 
         * @param value
         *     allowed object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public void setModifiedDate(XMLGregorianCalendar value) {
            this.modifiedDate = value;
        }

    }

}
