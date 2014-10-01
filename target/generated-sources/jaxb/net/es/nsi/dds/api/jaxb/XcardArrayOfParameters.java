//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.09.30 at 11:05:06 PM EDT 
//


package net.es.nsi.dds.api.jaxb;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ArrayOfParameters complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ArrayOfParameters">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:ietf:params:xml:ns:vcard-4.0}baseParameter" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfParameters", propOrder = {
    "baseParameter"
})
public class XcardArrayOfParameters {

    @XmlElementRef(name = "baseParameter", namespace = "urn:ietf:params:xml:ns:vcard-4.0", type = JAXBElement.class)
    protected List<JAXBElement<? extends XcardBaseParameterType>> baseParameter;

    /**
     * Gets the value of the baseParameter property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the baseParameter property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBaseParameter().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link XcardPrefParamType }{@code >}
     * {@link JAXBElement }{@code <}{@link XcardTypeParamType }{@code >}
     * {@link JAXBElement }{@code <}{@link XcardLabelParamType }{@code >}
     * {@link JAXBElement }{@code <}{@link XcardXuidParamType }{@code >}
     * {@link JAXBElement }{@code <}{@link XcardBaseParameterType }{@code >}
     * 
     * 
     */
    public List<JAXBElement<? extends XcardBaseParameterType>> getBaseParameter() {
        if (baseParameter == null) {
            baseParameter = new ArrayList<JAXBElement<? extends XcardBaseParameterType>>();
        }
        return this.baseParameter;
    }

}
