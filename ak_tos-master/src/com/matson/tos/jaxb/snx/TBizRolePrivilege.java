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
 * <p>Java class for tBizRolePrivilege complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tBizRolePrivilege">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="priv-id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="role" use="required" type="{http://www.navis.com/argo}tBizRole" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tBizRolePrivilege")
public class TBizRolePrivilege {

    @XmlAttribute(name = "priv-id", required = true)
    protected String privId;
    @XmlAttribute(required = true)
    protected TBizRole role;

    /**
     * Gets the value of the privId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPrivId() {
        return privId;
    }

    /**
     * Sets the value of the privId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPrivId(String value) {
        this.privId = value;
    }

    /**
     * Gets the value of the role property.
     * 
     * @return
     *     possible object is
     *     {@link TBizRole }
     *     
     */
    public TBizRole getRole() {
        return role;
    }

    /**
     * Sets the value of the role property.
     * 
     * @param value
     *     allowed object is
     *     {@link TBizRole }
     *     
     */
    public void setRole(TBizRole value) {
        this.role = value;
    }

}