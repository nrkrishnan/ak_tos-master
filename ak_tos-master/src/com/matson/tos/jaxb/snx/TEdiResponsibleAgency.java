//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.5-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.08.11 at 01:33:48 PM GMT-10:00 
//


package com.matson.tos.jaxb.snx;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for tEdiResponsibleAgency.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="tEdiResponsibleAgency">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="UN"/>
 *     &lt;enumeration value="ANSI"/>
 *     &lt;enumeration value="ACS"/>
 *     &lt;enumeration value="Nonstandard"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "tEdiResponsibleAgency")
@XmlEnum
public enum TEdiResponsibleAgency {


    /**
     * United Nations
     * 
     */
    UN("UN"),

    /**
     * American National Standards Institute
     * 
     */
    ANSI("ANSI"),

    /**
     * Australia Chamber of Shipping
     * 
     */
    ACS("ACS"),

    /**
     * Nonstandard
     * 
     */
    @XmlEnumValue("Nonstandard")
    NONSTANDARD("Nonstandard");
    private final String value;

    TEdiResponsibleAgency(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static TEdiResponsibleAgency fromValue(String v) {
        for (TEdiResponsibleAgency c: TEdiResponsibleAgency.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
