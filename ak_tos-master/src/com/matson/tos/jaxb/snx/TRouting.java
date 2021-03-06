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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Routing specifies the carrier that brought or will bring the inventory
 *                 unit to the facility and that which took it or will take it from the facility. Both
 *                 intended and actual carriers are recorded.
 *             
 * 
 * <p>Java class for tRouting complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tRouting">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="carrier" maxOccurs="4">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;extension base="{http://www.navis.com/argo}tCarrier">
 *               &lt;/extension>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *       &lt;attribute name="pol" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="pol-name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="pod-1" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="pod-1-name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="pod-2" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="pod-2-name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="opl" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="origin" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="destination" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="pin-number" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="return-to-location" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="designated-trucker" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="carrier-service" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="group" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="next-facility" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tRouting", propOrder = {
    "carrier"
})
public class TRouting {

    @XmlElement(required = true)
    protected List<TRouting.Carrier> carrier;
    @XmlAttribute
    protected String pol;
    @XmlAttribute(name = "pol-name")
    protected String polName;
    @XmlAttribute(name = "pod-1")
    protected String pod1;
    @XmlAttribute(name = "pod-1-name")
    protected String pod1Name;
    @XmlAttribute(name = "pod-2")
    protected String pod2;
    @XmlAttribute(name = "pod-2-name")
    protected String pod2Name;
    @XmlAttribute
    protected String opl;
    @XmlAttribute
    protected String origin;
    @XmlAttribute
    protected String destination;
    @XmlAttribute(name = "pin-number")
    protected String pinNumber;
    @XmlAttribute(name = "return-to-location")
    protected String returnToLocation;
    @XmlAttribute(name = "designated-trucker")
    protected String designatedTrucker;
    @XmlAttribute(name = "carrier-service")
    protected String carrierService;
    @XmlAttribute
    protected String group;
    @XmlAttribute(name = "next-facility")
    protected String nextFacility;

    /**
     * Gets the value of the carrier property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the carrier property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCarrier().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TRouting.Carrier }
     * 
     * 
     */
    public List<TRouting.Carrier> getCarrier() {
        if (carrier == null) {
            carrier = new ArrayList<TRouting.Carrier>();
        }
        return this.carrier;
    }

    /**
     * Gets the value of the pol property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPol() {
        return pol;
    }

    /**
     * Sets the value of the pol property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPol(String value) {
        this.pol = value;
    }

    /**
     * Gets the value of the polName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPolName() {
        return polName;
    }

    /**
     * Sets the value of the polName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPolName(String value) {
        this.polName = value;
    }

    /**
     * Gets the value of the pod1 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPod1() {
        return pod1;
    }

    /**
     * Sets the value of the pod1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPod1(String value) {
        this.pod1 = value;
    }

    /**
     * Gets the value of the pod1Name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPod1Name() {
        return pod1Name;
    }

    /**
     * Sets the value of the pod1Name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPod1Name(String value) {
        this.pod1Name = value;
    }

    /**
     * Gets the value of the pod2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPod2() {
        return pod2;
    }

    /**
     * Sets the value of the pod2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPod2(String value) {
        this.pod2 = value;
    }

    /**
     * Gets the value of the pod2Name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPod2Name() {
        return pod2Name;
    }

    /**
     * Sets the value of the pod2Name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPod2Name(String value) {
        this.pod2Name = value;
    }

    /**
     * Gets the value of the opl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOpl() {
        return opl;
    }

    /**
     * Sets the value of the opl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOpl(String value) {
        this.opl = value;
    }

    /**
     * Gets the value of the origin property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOrigin() {
        return origin;
    }

    /**
     * Sets the value of the origin property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOrigin(String value) {
        this.origin = value;
    }

    /**
     * Gets the value of the destination property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDestination() {
        return destination;
    }

    /**
     * Sets the value of the destination property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDestination(String value) {
        this.destination = value;
    }

    /**
     * Gets the value of the pinNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPinNumber() {
        return pinNumber;
    }

    /**
     * Sets the value of the pinNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPinNumber(String value) {
        this.pinNumber = value;
    }

    /**
     * Gets the value of the returnToLocation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReturnToLocation() {
        return returnToLocation;
    }

    /**
     * Sets the value of the returnToLocation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReturnToLocation(String value) {
        this.returnToLocation = value;
    }

    /**
     * Gets the value of the designatedTrucker property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDesignatedTrucker() {
        return designatedTrucker;
    }

    /**
     * Sets the value of the designatedTrucker property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDesignatedTrucker(String value) {
        this.designatedTrucker = value;
    }

    /**
     * Gets the value of the carrierService property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCarrierService() {
        return carrierService;
    }

    /**
     * Sets the value of the carrierService property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCarrierService(String value) {
        this.carrierService = value;
    }

    /**
     * Gets the value of the group property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGroup() {
        return group;
    }

    /**
     * Sets the value of the group property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGroup(String value) {
        this.group = value;
    }

    /**
     * Gets the value of the nextFacility property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNextFacility() {
        return nextFacility;
    }

    /**
     * Sets the value of the nextFacility property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNextFacility(String value) {
        this.nextFacility = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;extension base="{http://www.navis.com/argo}tCarrier">
     *     &lt;/extension>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Carrier
        extends TCarrier
    {


    }

}
