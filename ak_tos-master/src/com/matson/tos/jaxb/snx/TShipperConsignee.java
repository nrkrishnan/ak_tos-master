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
 * Defines an organization that acts as a Shipper or Consignee
 * 
 * <p>Java class for tShipperConsignee complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tShipperConsignee">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.navis.com/argo}tBusinessUnit">
 *       &lt;sequence>
 *         &lt;element name="agent-representations" type="{http://www.navis.com/argo}tAgentRepresentations" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="is-eq-operator" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="is-eq-owner" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="life-cycle-state" type="{http://www.navis.com/argo}tLifeCycleStateType" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tShipperConsignee", propOrder = {
    "agentRepresentations"
})
public class TShipperConsignee
    extends TBusinessUnit
{

    @XmlElement(name = "agent-representations")
    protected TAgentRepresentations agentRepresentations;
    @XmlAttribute(name = "is-eq-operator")
    protected String isEqOperator;
    @XmlAttribute(name = "is-eq-owner")
    protected String isEqOwner;
    @XmlAttribute(name = "life-cycle-state")
    protected TLifeCycleStateType lifeCycleState;

    /**
     * Gets the value of the agentRepresentations property.
     * 
     * @return
     *     possible object is
     *     {@link TAgentRepresentations }
     *     
     */
    public TAgentRepresentations getAgentRepresentations() {
        return agentRepresentations;
    }

    /**
     * Sets the value of the agentRepresentations property.
     * 
     * @param value
     *     allowed object is
     *     {@link TAgentRepresentations }
     *     
     */
    public void setAgentRepresentations(TAgentRepresentations value) {
        this.agentRepresentations = value;
    }

    /**
     * Gets the value of the isEqOperator property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIsEqOperator() {
        return isEqOperator;
    }

    /**
     * Sets the value of the isEqOperator property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIsEqOperator(String value) {
        this.isEqOperator = value;
    }

    /**
     * Gets the value of the isEqOwner property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIsEqOwner() {
        return isEqOwner;
    }

    /**
     * Sets the value of the isEqOwner property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIsEqOwner(String value) {
        this.isEqOwner = value;
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
