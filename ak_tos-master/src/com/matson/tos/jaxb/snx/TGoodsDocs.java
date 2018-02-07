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
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * Documentations
 * 
 * <p>Java class for tGoodsDocs complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tGoodsDocs">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="goods-doc" maxOccurs="unbounded">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="type" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="description1" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="description2" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="category" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="remarks" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="stamp-code" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="number-of-documents" type="{http://www.w3.org/2001/XMLSchema}long" />
 *                 &lt;attribute name="validation-date" type="{http://www.w3.org/2001/XMLSchema}date" />
 *                 &lt;attribute name="validation-office" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tGoodsDocs", propOrder = {
    "goodsDoc"
})
public class TGoodsDocs {

    @XmlElement(name = "goods-doc", required = true)
    protected List<TGoodsDocs.GoodsDoc> goodsDoc;

    /**
     * Gets the value of the goodsDoc property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the goodsDoc property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getGoodsDoc().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TGoodsDocs.GoodsDoc }
     * 
     * 
     */
    public List<TGoodsDocs.GoodsDoc> getGoodsDoc() {
        if (goodsDoc == null) {
            goodsDoc = new ArrayList<TGoodsDocs.GoodsDoc>();
        }
        return this.goodsDoc;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="type" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="description1" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="description2" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="category" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="remarks" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="stamp-code" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="number-of-documents" type="{http://www.w3.org/2001/XMLSchema}long" />
     *       &lt;attribute name="validation-date" type="{http://www.w3.org/2001/XMLSchema}date" />
     *       &lt;attribute name="validation-office" type="{http://www.w3.org/2001/XMLSchema}string" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class GoodsDoc {

        @XmlAttribute(required = true)
        protected String id;
        @XmlAttribute(required = true)
        protected String type;
        @XmlAttribute
        protected String description1;
        @XmlAttribute
        protected String description2;
        @XmlAttribute
        protected String category;
        @XmlAttribute
        protected String remarks;
        @XmlAttribute(name = "stamp-code")
        protected String stampCode;
        @XmlAttribute(name = "number-of-documents")
        protected Long numberOfDocuments;
        @XmlAttribute(name = "validation-date")
        @XmlSchemaType(name = "date")
        protected XMLGregorianCalendar validationDate;
        @XmlAttribute(name = "validation-office")
        protected String validationOffice;

        /**
         * Gets the value of the id property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getId() {
            return id;
        }

        /**
         * Sets the value of the id property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setId(String value) {
            this.id = value;
        }

        /**
         * Gets the value of the type property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getType() {
            return type;
        }

        /**
         * Sets the value of the type property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setType(String value) {
            this.type = value;
        }

        /**
         * Gets the value of the description1 property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getDescription1() {
            return description1;
        }

        /**
         * Sets the value of the description1 property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setDescription1(String value) {
            this.description1 = value;
        }

        /**
         * Gets the value of the description2 property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getDescription2() {
            return description2;
        }

        /**
         * Sets the value of the description2 property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setDescription2(String value) {
            this.description2 = value;
        }

        /**
         * Gets the value of the category property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getCategory() {
            return category;
        }

        /**
         * Sets the value of the category property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setCategory(String value) {
            this.category = value;
        }

        /**
         * Gets the value of the remarks property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getRemarks() {
            return remarks;
        }

        /**
         * Sets the value of the remarks property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setRemarks(String value) {
            this.remarks = value;
        }

        /**
         * Gets the value of the stampCode property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getStampCode() {
            return stampCode;
        }

        /**
         * Sets the value of the stampCode property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setStampCode(String value) {
            this.stampCode = value;
        }

        /**
         * Gets the value of the numberOfDocuments property.
         * 
         * @return
         *     possible object is
         *     {@link Long }
         *     
         */
        public Long getNumberOfDocuments() {
            return numberOfDocuments;
        }

        /**
         * Sets the value of the numberOfDocuments property.
         * 
         * @param value
         *     allowed object is
         *     {@link Long }
         *     
         */
        public void setNumberOfDocuments(Long value) {
            this.numberOfDocuments = value;
        }

        /**
         * Gets the value of the validationDate property.
         * 
         * @return
         *     possible object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public XMLGregorianCalendar getValidationDate() {
            return validationDate;
        }

        /**
         * Sets the value of the validationDate property.
         * 
         * @param value
         *     allowed object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public void setValidationDate(XMLGregorianCalendar value) {
            this.validationDate = value;
        }

        /**
         * Gets the value of the validationOffice property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getValidationOffice() {
            return validationOffice;
        }

        /**
         * Sets the value of the validationOffice property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setValidationOffice(String value) {
            this.validationOffice = value;
        }

    }

}