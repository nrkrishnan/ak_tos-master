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
 * Console Printer
 * 
 * <p>Java class for tConsolePrinter complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tConsolePrinter">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="console-printer-id" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="doc-type-id" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tConsolePrinter")
public class TConsolePrinter {

    @XmlAttribute(name = "console-printer-id")
    protected String consolePrinterId;
    @XmlAttribute(name = "doc-type-id")
    protected String docTypeId;

    /**
     * Gets the value of the consolePrinterId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConsolePrinterId() {
        return consolePrinterId;
    }

    /**
     * Sets the value of the consolePrinterId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConsolePrinterId(String value) {
        this.consolePrinterId = value;
    }

    /**
     * Gets the value of the docTypeId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDocTypeId() {
        return docTypeId;
    }

    /**
     * Sets the value of the docTypeId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDocTypeId(String value) {
        this.docTypeId = value;
    }

}