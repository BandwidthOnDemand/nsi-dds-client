//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.09.30 at 11:05:06 PM EDT 
//


package net.es.nsi.dds.api.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for BasePropertyType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="BasePropertyType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:ietf:params:xml:ns:vcard-4.0}parameters" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BasePropertyType", propOrder = {
    "parameters"
})
@XmlSeeAlso({
    XcardNPropType.class,
    XcardDateDatetimePropertyType.class,
    XcardTextListPropertyType.class,
    XcardNonEmptyTextListPropertyType.class,
    XcardAffiliationPropType.class,
    XcardTextOrUriPropertyType.class,
    XcardRevPropType.class,
    XcardUriPropertyType.class,
    XcardTextPropertyType.class,
    XcardLangPropType.class,
    XcardAdrPropType.class
})
public abstract class XcardBasePropertyType {

    @XmlElement(namespace = "urn:ietf:params:xml:ns:vcard-4.0")
    protected XcardArrayOfParameters parameters;

    /**
     * Gets the value of the parameters property.
     * 
     * @return
     *     possible object is
     *     {@link XcardArrayOfParameters }
     *     
     */
    public XcardArrayOfParameters getParameters() {
        return parameters;
    }

    /**
     * Sets the value of the parameters property.
     * 
     * @param value
     *     allowed object is
     *     {@link XcardArrayOfParameters }
     *     
     */
    public void setParameters(XcardArrayOfParameters value) {
        this.parameters = value;
    }

}
