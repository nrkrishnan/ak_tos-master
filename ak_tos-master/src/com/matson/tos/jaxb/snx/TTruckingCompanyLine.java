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
 * Trucking company line
 * 
 * <p>Java class for tTruckingCompanyLine complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tTruckingCompanyLine">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.navis.com/argo}tLineAgreementBase">
 *       &lt;attribute name="trucking-company" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tTruckingCompanyLine")
public class TTruckingCompanyLine
    extends TLineAgreementBase
{

    @XmlAttribute(name = "trucking-company")
    protected String truckingCompany;

    /**
     * Gets the value of the truckingCompany property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTruckingCompany() {
        return truckingCompany;
    }

    /**
     * Sets the value of the truckingCompany property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTruckingCompany(String value) {
        this.truckingCompany = value;
    }

}
