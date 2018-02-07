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
 * Storage Rule
 * 
 * <p>Java class for tStorageRule complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tStorageRule">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="rule-id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="is-rule-for-power" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="start-day-rule" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="is-include-start-day" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="end-day-rule" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="is-include-end-day" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="is-include-exempt-days" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="is-include-gratis-days" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="start-day-cut-off-hours" type="{http://www.w3.org/2001/XMLSchema}long" />
 *       &lt;attribute name="end-day-cut-off-hours" type="{http://www.w3.org/2001/XMLSchema}long" />
 *       &lt;attribute name="power-charge-by">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;enumeration value="HOUR"/>
 *             &lt;enumeration value="DAY"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="power-first-tier-rounding" type="{http://www.w3.org/2001/XMLSchema}long" />
 *       &lt;attribute name="power-other-tier-rounding" type="{http://www.w3.org/2001/XMLSchema}long" />
 *       &lt;attribute name="calendar" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="round-up-hours" type="{http://www.w3.org/2001/XMLSchema}long" />
 *       &lt;attribute name="round-up-minutes" type="{http://www.w3.org/2001/XMLSchema}long" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tStorageRule")
public class TStorageRule {

    @XmlAttribute(name = "rule-id", required = true)
    protected String ruleId;
    @XmlAttribute(name = "is-rule-for-power")
    protected String isRuleForPower;
    @XmlAttribute(name = "start-day-rule")
    protected String startDayRule;
    @XmlAttribute(name = "is-include-start-day")
    protected String isIncludeStartDay;
    @XmlAttribute(name = "end-day-rule")
    protected String endDayRule;
    @XmlAttribute(name = "is-include-end-day")
    protected String isIncludeEndDay;
    @XmlAttribute(name = "is-include-exempt-days")
    protected String isIncludeExemptDays;
    @XmlAttribute(name = "is-include-gratis-days")
    protected String isIncludeGratisDays;
    @XmlAttribute(name = "start-day-cut-off-hours")
    protected Long startDayCutOffHours;
    @XmlAttribute(name = "end-day-cut-off-hours")
    protected Long endDayCutOffHours;
    @XmlAttribute(name = "power-charge-by")
    protected String powerChargeBy;
    @XmlAttribute(name = "power-first-tier-rounding")
    protected Long powerFirstTierRounding;
    @XmlAttribute(name = "power-other-tier-rounding")
    protected Long powerOtherTierRounding;
    @XmlAttribute
    protected String calendar;
    @XmlAttribute(name = "round-up-hours")
    protected Long roundUpHours;
    @XmlAttribute(name = "round-up-minutes")
    protected Long roundUpMinutes;

    /**
     * Gets the value of the ruleId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRuleId() {
        return ruleId;
    }

    /**
     * Sets the value of the ruleId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRuleId(String value) {
        this.ruleId = value;
    }

    /**
     * Gets the value of the isRuleForPower property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIsRuleForPower() {
        return isRuleForPower;
    }

    /**
     * Sets the value of the isRuleForPower property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIsRuleForPower(String value) {
        this.isRuleForPower = value;
    }

    /**
     * Gets the value of the startDayRule property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStartDayRule() {
        return startDayRule;
    }

    /**
     * Sets the value of the startDayRule property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStartDayRule(String value) {
        this.startDayRule = value;
    }

    /**
     * Gets the value of the isIncludeStartDay property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIsIncludeStartDay() {
        return isIncludeStartDay;
    }

    /**
     * Sets the value of the isIncludeStartDay property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIsIncludeStartDay(String value) {
        this.isIncludeStartDay = value;
    }

    /**
     * Gets the value of the endDayRule property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEndDayRule() {
        return endDayRule;
    }

    /**
     * Sets the value of the endDayRule property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEndDayRule(String value) {
        this.endDayRule = value;
    }

    /**
     * Gets the value of the isIncludeEndDay property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIsIncludeEndDay() {
        return isIncludeEndDay;
    }

    /**
     * Sets the value of the isIncludeEndDay property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIsIncludeEndDay(String value) {
        this.isIncludeEndDay = value;
    }

    /**
     * Gets the value of the isIncludeExemptDays property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIsIncludeExemptDays() {
        return isIncludeExemptDays;
    }

    /**
     * Sets the value of the isIncludeExemptDays property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIsIncludeExemptDays(String value) {
        this.isIncludeExemptDays = value;
    }

    /**
     * Gets the value of the isIncludeGratisDays property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIsIncludeGratisDays() {
        return isIncludeGratisDays;
    }

    /**
     * Sets the value of the isIncludeGratisDays property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIsIncludeGratisDays(String value) {
        this.isIncludeGratisDays = value;
    }

    /**
     * Gets the value of the startDayCutOffHours property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getStartDayCutOffHours() {
        return startDayCutOffHours;
    }

    /**
     * Sets the value of the startDayCutOffHours property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setStartDayCutOffHours(Long value) {
        this.startDayCutOffHours = value;
    }

    /**
     * Gets the value of the endDayCutOffHours property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getEndDayCutOffHours() {
        return endDayCutOffHours;
    }

    /**
     * Sets the value of the endDayCutOffHours property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setEndDayCutOffHours(Long value) {
        this.endDayCutOffHours = value;
    }

    /**
     * Gets the value of the powerChargeBy property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPowerChargeBy() {
        return powerChargeBy;
    }

    /**
     * Sets the value of the powerChargeBy property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPowerChargeBy(String value) {
        this.powerChargeBy = value;
    }

    /**
     * Gets the value of the powerFirstTierRounding property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getPowerFirstTierRounding() {
        return powerFirstTierRounding;
    }

    /**
     * Sets the value of the powerFirstTierRounding property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setPowerFirstTierRounding(Long value) {
        this.powerFirstTierRounding = value;
    }

    /**
     * Gets the value of the powerOtherTierRounding property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getPowerOtherTierRounding() {
        return powerOtherTierRounding;
    }

    /**
     * Sets the value of the powerOtherTierRounding property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setPowerOtherTierRounding(Long value) {
        this.powerOtherTierRounding = value;
    }

    /**
     * Gets the value of the calendar property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCalendar() {
        return calendar;
    }

    /**
     * Sets the value of the calendar property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCalendar(String value) {
        this.calendar = value;
    }

    /**
     * Gets the value of the roundUpHours property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getRoundUpHours() {
        return roundUpHours;
    }

    /**
     * Sets the value of the roundUpHours property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setRoundUpHours(Long value) {
        this.roundUpHours = value;
    }

    /**
     * Gets the value of the roundUpMinutes property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getRoundUpMinutes() {
        return roundUpMinutes;
    }

    /**
     * Sets the value of the roundUpMinutes property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setRoundUpMinutes(Long value) {
        this.roundUpMinutes = value;
    }

}
