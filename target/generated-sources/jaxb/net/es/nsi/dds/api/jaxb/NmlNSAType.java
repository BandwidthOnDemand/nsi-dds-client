//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.09.30 at 11:05:06 PM EDT 
//


package net.es.nsi.dds.api.jaxb;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for NSAType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="NSAType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.ogf.org/nml/2013/05/base#}NetworkObject">
 *       &lt;sequence>
 *         &lt;element ref="{http://schemas.ogf.org/nsi/2013/09/topology#}Service" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://schemas.ogf.org/nsi/2013/09/topology#}Relation" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://schemas.ogf.org/nml/2013/05/base#}Topology" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="extension" type="{http://schemas.ogf.org/nsi/2013/09/topology#}ExtensionType" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;anyAttribute processContents='lax' namespace='##other'/>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NSAType", namespace = "http://schemas.ogf.org/nsi/2013/09/topology#", propOrder = {
    "service",
    "relation",
    "topology",
    "extension"
})
public class NmlNSAType
    extends NmlNetworkObject
{

    @XmlElement(name = "Service", namespace = "http://schemas.ogf.org/nsi/2013/09/topology#")
    protected List<NmlServiceType> service;
    @XmlElement(name = "Relation", namespace = "http://schemas.ogf.org/nsi/2013/09/topology#")
    protected List<NmlNSARelationType> relation;
    @XmlElement(name = "Topology", namespace = "http://schemas.ogf.org/nml/2013/05/base#")
    protected List<NmlTopologyType> topology;
    @XmlElement(namespace = "http://schemas.ogf.org/nsi/2013/09/topology#")
    protected NmlExtensionType extension;

    /**
     * Gets the value of the service property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the service property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getService().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link NmlServiceType }
     * 
     * 
     */
    public List<NmlServiceType> getService() {
        if (service == null) {
            service = new ArrayList<NmlServiceType>();
        }
        return this.service;
    }

    /**
     * Gets the value of the relation property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the relation property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRelation().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link NmlNSARelationType }
     * 
     * 
     */
    public List<NmlNSARelationType> getRelation() {
        if (relation == null) {
            relation = new ArrayList<NmlNSARelationType>();
        }
        return this.relation;
    }

    /**
     * Gets the value of the topology property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the topology property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTopology().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link NmlTopologyType }
     * 
     * 
     */
    public List<NmlTopologyType> getTopology() {
        if (topology == null) {
            topology = new ArrayList<NmlTopologyType>();
        }
        return this.topology;
    }

    /**
     * Gets the value of the extension property.
     * 
     * @return
     *     possible object is
     *     {@link NmlExtensionType }
     *     
     */
    public NmlExtensionType getExtension() {
        return extension;
    }

    /**
     * Sets the value of the extension property.
     * 
     * @param value
     *     allowed object is
     *     {@link NmlExtensionType }
     *     
     */
    public void setExtension(NmlExtensionType value) {
        this.extension = value;
    }

}
