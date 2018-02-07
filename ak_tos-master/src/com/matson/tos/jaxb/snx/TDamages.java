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
import javax.xml.bind.annotation.XmlType;


/**
 * Encapsulates all damage to a piece of equipment.
 * 
 * <p>Java class for tDamages complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tDamages">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="damage" type="{http://www.navis.com/argo}tDamage" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tDamages", propOrder = {
    "damage"
})
public class TDamages {

    protected List<TDamage> damage;

    /**
     * Gets the value of the damage property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the damage property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDamage().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TDamage }
     * 
     * 
     */
    public List<TDamage> getDamage() {
        if (damage == null) {
            damage = new ArrayList<TDamage>();
        }
        return this.damage;
    }

}
