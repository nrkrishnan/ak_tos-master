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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Console Printers
 * 
 * <p>Java class for tConsolePrinters complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tConsolePrinters">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="console-printer" type="{http://www.navis.com/argo}tConsolePrinter" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tConsolePrinters", propOrder = {
    "consolePrinter"
})
public class TConsolePrinters {

    @XmlElement(name = "console-printer", required = true)
    protected List<TConsolePrinter> consolePrinter;

    /**
     * Gets the value of the consolePrinter property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the consolePrinter property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getConsolePrinter().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TConsolePrinter }
     * 
     * 
     */
    public List<TConsolePrinter> getConsolePrinter() {
        if (consolePrinter == null) {
            consolePrinter = new ArrayList<TConsolePrinter>();
        }
        return this.consolePrinter;
    }

}
