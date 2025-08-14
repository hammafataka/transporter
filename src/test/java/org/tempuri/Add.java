
package org.tempuri;

import jakarta.annotation.Generated;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
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
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="intA" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="intB" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
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
    "intA",
    "intB"
})
@XmlRootElement(name = "Add")
@Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.6", date = "2023-05-23T10:30:04+02:00")
public class Add {

    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.6", date = "2023-05-23T10:30:04+02:00")
    protected int intA;
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.6", date = "2023-05-23T10:30:04+02:00")
    protected int intB;

    /**
     * Gets the value of the intA property.
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.6", date = "2023-05-23T10:30:04+02:00")
    public int getIntA() {
        return intA;
    }

    /**
     * Sets the value of the intA property.
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.6", date = "2023-05-23T10:30:04+02:00")
    public void setIntA(int value) {
        this.intA = value;
    }

    /**
     * Gets the value of the intB property.
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.6", date = "2023-05-23T10:30:04+02:00")
    public int getIntB() {
        return intB;
    }

    /**
     * Sets the value of the intB property.
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.6", date = "2023-05-23T10:30:04+02:00")
    public void setIntB(int value) {
        this.intB = value;
    }

}
