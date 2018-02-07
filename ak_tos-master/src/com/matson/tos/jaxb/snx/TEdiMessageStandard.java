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
 * <p>Java class for tEdiMessageStandard.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="tEdiMessageStandard">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="X12"/>
 *     &lt;enumeration value="EDIFACT"/>
 *     &lt;enumeration value="Navis standard XML"/>
 *     &lt;enumeration value="FLATFILE"/>
 *     &lt;enumeration value="Misc. standard or prorpietary"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "tEdiMessageStandard")
@XmlEnum
public enum TEdiMessageStandard {


    /**
     * X12
     * 
     */
    @XmlEnumValue("X12")
    X_12("X12"),

    /**
     * EDIFACT
     * 
     */
    EDIFACT("EDIFACT"),

    /**
     * Navis standard XML
     * 
     */
    @XmlEnumValue("Navis standard XML")
    NAVIS_STANDARD_XML("Navis standard XML"),

    /**
     * FLATFILE
     * 
     */
    FLATFILE("FLATFILE"),

    /**
     * Misc. standard or proprietary
     * 
     */
    @XmlEnumValue("Misc. standard or prorpietary")
    MISC_STANDARD_OR_PRORPIETARY("Misc. standard or prorpietary");
    private final String value;

    TEdiMessageStandard(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static TEdiMessageStandard fromValue(String v) {
        for (TEdiMessageStandard c: TEdiMessageStandard.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
