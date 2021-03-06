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
 * Line List
 * 
 * <p>Java class for tLineList complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tLineList">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="routing" type="{http://www.navis.com/argo}tLoadListRouting"/>
 *         &lt;element name="hazards" type="{http://www.navis.com/argo}tHazards" minOccurs="0"/>
 *         &lt;element name="contents" type="{http://www.navis.com/argo}tLoadListUnitContents" minOccurs="0"/>
 *         &lt;element name="seals" type="{http://www.navis.com/argo}tUnitSeals" minOccurs="0"/>
 *         &lt;element name="reefer" type="{http://www.navis.com/argo}tReeferRequirements" minOccurs="0"/>
 *         &lt;element name="oog" type="{http://www.navis.com/argo}tOog" minOccurs="0"/>
 *         &lt;element name="unit-flex" type="{http://www.navis.com/argo}tUnitFlexFields" minOccurs="0"/>
 *         &lt;element name="ufv-flex" type="{http://www.navis.com/argo}tUfvFlexFields" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="action" type="{http://www.navis.com/argo}tLoadListAction" />
 *       &lt;attribute name="line-id" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="sender-id" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="complex-id" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="unit-id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="eq-type-id" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="category" type="{http://www.navis.com/argo}tCategory" />
 *       &lt;attribute name="freight-kind">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;enumeration value="FCL"/>
 *             &lt;enumeration value="MTY"/>
 *             &lt;enumeration value="BBK"/>
 *             &lt;enumeration value="LCL"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="order-nbr" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="ctr-operator-id" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="special-stow-id" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="stow-position" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tLineList", propOrder = {
    "routing",
    "hazards",
    "contents",
    "seals",
    "reefer",
    "oog",
    "unitFlex",
    "ufvFlex"
})
public class TLineList {

    @XmlElement(required = true)
    protected TLoadListRouting routing;
    protected THazards hazards;
    protected TLoadListUnitContents contents;
    protected TUnitSeals seals;
    protected TReeferRequirements reefer;
    protected TOog oog;
    @XmlElement(name = "unit-flex")
    protected TUnitFlexFields unitFlex;
    @XmlElement(name = "ufv-flex")
    protected TUfvFlexFields ufvFlex;
    @XmlAttribute
    protected TLoadListAction action;
    @XmlAttribute(name = "line-id")
    protected String lineId;
    @XmlAttribute(name = "sender-id")
    protected String senderId;
    @XmlAttribute(name = "complex-id")
    protected String complexId;
    @XmlAttribute(name = "unit-id", required = true)
    protected String unitId;
    @XmlAttribute(name = "eq-type-id")
    protected String eqTypeId;
    @XmlAttribute
    protected TCategory category;
    @XmlAttribute(name = "freight-kind")
    protected String freightKind;
    @XmlAttribute(name = "order-nbr")
    protected String orderNbr;
    @XmlAttribute(name = "ctr-operator-id")
    protected String ctrOperatorId;
    @XmlAttribute(name = "special-stow-id")
    protected String specialStowId;
    @XmlAttribute(name = "stow-position")
    protected String stowPosition;

    /**
     * Gets the value of the routing property.
     * 
     * @return
     *     possible object is
     *     {@link TLoadListRouting }
     *     
     */
    public TLoadListRouting getRouting() {
        return routing;
    }

    /**
     * Sets the value of the routing property.
     * 
     * @param value
     *     allowed object is
     *     {@link TLoadListRouting }
     *     
     */
    public void setRouting(TLoadListRouting value) {
        this.routing = value;
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
     * Gets the value of the contents property.
     * 
     * @return
     *     possible object is
     *     {@link TLoadListUnitContents }
     *     
     */
    public TLoadListUnitContents getContents() {
        return contents;
    }

    /**
     * Sets the value of the contents property.
     * 
     * @param value
     *     allowed object is
     *     {@link TLoadListUnitContents }
     *     
     */
    public void setContents(TLoadListUnitContents value) {
        this.contents = value;
    }

    /**
     * Gets the value of the seals property.
     * 
     * @return
     *     possible object is
     *     {@link TUnitSeals }
     *     
     */
    public TUnitSeals getSeals() {
        return seals;
    }

    /**
     * Sets the value of the seals property.
     * 
     * @param value
     *     allowed object is
     *     {@link TUnitSeals }
     *     
     */
    public void setSeals(TUnitSeals value) {
        this.seals = value;
    }

    /**
     * Gets the value of the reefer property.
     * 
     * @return
     *     possible object is
     *     {@link TReeferRequirements }
     *     
     */
    public TReeferRequirements getReefer() {
        return reefer;
    }

    /**
     * Sets the value of the reefer property.
     * 
     * @param value
     *     allowed object is
     *     {@link TReeferRequirements }
     *     
     */
    public void setReefer(TReeferRequirements value) {
        this.reefer = value;
    }

    /**
     * Gets the value of the oog property.
     * 
     * @return
     *     possible object is
     *     {@link TOog }
     *     
     */
    public TOog getOog() {
        return oog;
    }

    /**
     * Sets the value of the oog property.
     * 
     * @param value
     *     allowed object is
     *     {@link TOog }
     *     
     */
    public void setOog(TOog value) {
        this.oog = value;
    }

    /**
     * Gets the value of the unitFlex property.
     * 
     * @return
     *     possible object is
     *     {@link TUnitFlexFields }
     *     
     */
    public TUnitFlexFields getUnitFlex() {
        return unitFlex;
    }

    /**
     * Sets the value of the unitFlex property.
     * 
     * @param value
     *     allowed object is
     *     {@link TUnitFlexFields }
     *     
     */
    public void setUnitFlex(TUnitFlexFields value) {
        this.unitFlex = value;
    }

    /**
     * Gets the value of the ufvFlex property.
     * 
     * @return
     *     possible object is
     *     {@link TUfvFlexFields }
     *     
     */
    public TUfvFlexFields getUfvFlex() {
        return ufvFlex;
    }

    /**
     * Sets the value of the ufvFlex property.
     * 
     * @param value
     *     allowed object is
     *     {@link TUfvFlexFields }
     *     
     */
    public void setUfvFlex(TUfvFlexFields value) {
        this.ufvFlex = value;
    }

    /**
     * Gets the value of the action property.
     * 
     * @return
     *     possible object is
     *     {@link TLoadListAction }
     *     
     */
    public TLoadListAction getAction() {
        return action;
    }

    /**
     * Sets the value of the action property.
     * 
     * @param value
     *     allowed object is
     *     {@link TLoadListAction }
     *     
     */
    public void setAction(TLoadListAction value) {
        this.action = value;
    }

    /**
     * Gets the value of the lineId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLineId() {
        return lineId;
    }

    /**
     * Sets the value of the lineId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLineId(String value) {
        this.lineId = value;
    }

    /**
     * Gets the value of the senderId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSenderId() {
        return senderId;
    }

    /**
     * Sets the value of the senderId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSenderId(String value) {
        this.senderId = value;
    }

    /**
     * Gets the value of the complexId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getComplexId() {
        return complexId;
    }

    /**
     * Sets the value of the complexId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setComplexId(String value) {
        this.complexId = value;
    }

    /**
     * Gets the value of the unitId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUnitId() {
        return unitId;
    }

    /**
     * Sets the value of the unitId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUnitId(String value) {
        this.unitId = value;
    }

    /**
     * Gets the value of the eqTypeId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEqTypeId() {
        return eqTypeId;
    }

    /**
     * Sets the value of the eqTypeId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEqTypeId(String value) {
        this.eqTypeId = value;
    }

    /**
     * Gets the value of the category property.
     * 
     * @return
     *     possible object is
     *     {@link TCategory }
     *     
     */
    public TCategory getCategory() {
        return category;
    }

    /**
     * Sets the value of the category property.
     * 
     * @param value
     *     allowed object is
     *     {@link TCategory }
     *     
     */
    public void setCategory(TCategory value) {
        this.category = value;
    }

    /**
     * Gets the value of the freightKind property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFreightKind() {
        return freightKind;
    }

    /**
     * Sets the value of the freightKind property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFreightKind(String value) {
        this.freightKind = value;
    }

    /**
     * Gets the value of the orderNbr property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOrderNbr() {
        return orderNbr;
    }

    /**
     * Sets the value of the orderNbr property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOrderNbr(String value) {
        this.orderNbr = value;
    }

    /**
     * Gets the value of the ctrOperatorId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCtrOperatorId() {
        return ctrOperatorId;
    }

    /**
     * Sets the value of the ctrOperatorId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCtrOperatorId(String value) {
        this.ctrOperatorId = value;
    }

    /**
     * Gets the value of the specialStowId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSpecialStowId() {
        return specialStowId;
    }

    /**
     * Sets the value of the specialStowId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSpecialStowId(String value) {
        this.specialStowId = value;
    }

    /**
     * Gets the value of the stowPosition property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStowPosition() {
        return stowPosition;
    }

    /**
     * Sets the value of the stowPosition property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStowPosition(String value) {
        this.stowPosition = value;
    }

}
