//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.5-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.08.11 at 01:33:48 PM GMT-10:00 
//


package com.matson.tos.jaxb.snx;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for tCategory.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="tCategory">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="IMPORT"/>
 *     &lt;enumeration value="STORAGE"/>
 *     &lt;enumeration value="EXPORT"/>
 *     &lt;enumeration value="TRANSSHIP"/>
 *     &lt;enumeration value="THROUGH"/>
 *     &lt;enumeration value="DOMESTIC"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "tCategory")
@XmlEnum
public enum TCategory {

    IMPORT,
    STORAGE,
    EXPORT,
    TRANSSHIP,
    THROUGH,
    DOMESTIC;

    public String value() {
        return name();
    }

    public static TCategory fromValue(String v) {
        return valueOf(v);
    }

}