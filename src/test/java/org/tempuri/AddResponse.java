
package org.tempuri;

import jakarta.annotation.Generated;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{<a href="http://www.w3.org/2001/XMLSchema">...</a>}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="AddResult" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "addResult"
})
@XmlRootElement(name = "AddResponse")
@Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.6", date = "2023-05-23T10:30:04+02:00")
public class AddResponse {

    @XmlElement(name = "AddResult")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.6", date = "2023-05-23T10:30:04+02:00")
    protected int addResult;

    /**
     * Gets the value of the addResult property.
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.6", date = "2023-05-23T10:30:04+02:00")
    public int getAddResult() {
        return addResult;
    }

    /**
     * Sets the value of the addResult property.
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.6", date = "2023-05-23T10:30:04+02:00")
    public void setAddResult(int value) {
        this.addResult = value;
    }

}
