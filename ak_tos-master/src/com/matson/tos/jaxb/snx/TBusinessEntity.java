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
 * <p>Java class for tBusinessEntity.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="tBusinessEntity">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="EQ"/>
 *     &lt;enumeration value="UNIT"/>
 *     &lt;enumeration value="CTR"/>
 *     &lt;enumeration value="CHS"/>
 *     &lt;enumeration value="ACC"/>
 *     &lt;enumeration value="GOODS"/>
 *     &lt;enumeration value="VES"/>
 *     &lt;enumeration value="VV"/>
 *     &lt;enumeration value="RV"/>
 *     &lt;enumeration value="RCARV"/>
 *     &lt;enumeration value="BKG"/>
 *     &lt;enumeration value="DO"/>
 *     &lt;enumeration value="LO"/>
 *     &lt;enumeration value="EDISESS"/>
 *     &lt;enumeration value="YARD"/>
 *     &lt;enumeration value="BL"/>
 *     &lt;enumeration value="RO"/>
 *     &lt;enumeration value="ERO"/>
 *     &lt;enumeration value="TV"/>
 *     &lt;enumeration value="NA"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "tBusinessEntity")
@XmlEnum
public enum TBusinessEntity {


    /**
     * An entity representing a container, chassis, or accessory
     * 
     */
    EQ,

    /**
     * An entity representing a moveable Unit
     * 
     */
    UNIT,

    /**
     * An entity representing a container
     * 
     */
    CTR,

    /**
     * An entity representing a container
     * 
     */
    CHS,

    /**
     * An entity representing a container
     * 
     */
    ACC,

    /**
     * An entity representing goods or cargo
     * 
     */
    GOODS,

    /**
     * An entity representing a vessel
     * 
     */
    VES,

    /**
     * An entity representing a vessel call
     * 
     */
    VV,

    /**
     * An entity representing a train visit
     * 
     */
    RV,

    /**
     * An entity representing railcar visit
     * 
     */
    RCARV,

    /**
     * An entity representing an export booking
     * 
     */
    BKG,

    /**
     * An entity representing a delivery order
     * 
     */
    DO,

    /**
     * An entity representing a load order
     * 
     */
    LO,

    /**
     * An entity representing an EDI Session
     * 
     */
    EDISESS,

    /**
     * An entity representing a Yard
     * 
     */
    YARD,

    /**
     * An entity representing a bill of lading
     * 
     */
    BL,

    /**
     * An entity representing a rail order
     * 
     */
    RO,

    /**
     * An entity representing an equipment receive order
     * 
     */
    ERO,

    /**
     * An entity representing a truck visit
     * 
     */
    TV,

    /**
     * Not Applicable
     * 
     */
    NA;

    public String value() {
        return name();
    }

    public static TBusinessEntity fromValue(String v) {
        return valueOf(v);
    }

}