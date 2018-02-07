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
 * <p>Java class for tSparcsCraneCodes.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="tSparcsCraneCodes">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="BR"/>
 *     &lt;enumeration value="MA"/>
 *     &lt;enumeration value="SP"/>
 *     &lt;enumeration value="DO"/>
 *     &lt;enumeration value="US"/>
 *     &lt;enumeration value="HA"/>
 *     &lt;enumeration value="BB"/>
 *     &lt;enumeration value="LA"/>
 *     &lt;enumeration value="CB"/>
 *     &lt;enumeration value="WL"/>
 *     &lt;enumeration value="WV"/>
 *     &lt;enumeration value="HC"/>
 *     &lt;enumeration value="WP"/>
 *     &lt;enumeration value="CS"/>
 *     &lt;enumeration value="SC"/>
 *     &lt;enumeration value="BM"/>
 *     &lt;enumeration value="WI"/>
 *     &lt;enumeration value="BW"/>
 *     &lt;enumeration value="PB"/>
 *     &lt;enumeration value="CF"/>
 *     &lt;enumeration value="WS"/>
 *     &lt;enumeration value="SB"/>
 *     &lt;enumeration value="TB"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "tSparcsCraneCodes")
@XmlEnum
public enum TSparcsCraneCodes {


    /**
     * Sparcs delay code indicating 'Break'
     * 
     */
    BR,

    /**
     * Sparcs delay code indicating 'Maintenance'
     * 
     */
    MA,

    /**
     * Sparcs delay code indicating 'Special'
     * 
     */
    SP,

    /**
     * Sparcs delay code indicating 'Downtime'
     * 
     */
    DO,

    /**
     * Sparcs delay code indicating 'User Defined'
     * 
     */
    US,

    /**
     * Sparcs delay code indicating 'Hatch Move'
     * 
     */
    HA,

    /**
     * Sparcs delay code indicating 'Break Bulk'
     * 
     */
    BB,

    /**
     * Sparcs delay code indicating 'Late Start'
     * 
     */
    LA,

    /**
     * Sparcs delay code indicating 'Crane Breakdown'
     * 
     */
    CB,

    /**
     * Sparcs delay code indicating 'Waiting Lashing'
     * 
     */
    WL,

    /**
     * Sparcs delay code indicating 'Waiting Vessel'
     * 
     */
    WV,

    /**
     * Sparcs delay code indicating 'Hatch Cover'
     * 
     */
    HC,

    /**
     * Sparcs delay code indicating 'Waiting CHE'
     * 
     */
    WP,

    /**
     * Sparcs delay code indicating 'Change Shift'
     * 
     */
    CS,

    /**
     * Sparcs delay code indicating 'Spreader Change'
     * 
     */
    SC,

    /**
     * Sparcs delay code indicating 'Boom Up'
     * 
     */
    BM,

    /**
     * Sparcs delay code indicating 'Waiting Instruction'
     * 
     */
    WI,

    /**
     * Sparcs delay code indicating 'Bad Weather'
     * 
     */
    BW,

    /**
     * Sparcs delay code indicating 'Primay Break'
     * 
     */
    PB,

    /**
     * Sparcs delay code indicating 'CHE Failure'
     * 
     */
    CF,

    /**
     * Sparcs delay code indicating 'Work Station'
     * 
     */
    WS,

    /**
     * Sparcs delay code indicating 'Secondary Break'
     * 
     */
    SB,

    /**
     * Sparcs delay code indicating 'Timed Break'
     * 
     */
    TB;

    public String value() {
        return name();
    }

    public static TSparcsCraneCodes fromValue(String v) {
        return valueOf(v);
    }

}