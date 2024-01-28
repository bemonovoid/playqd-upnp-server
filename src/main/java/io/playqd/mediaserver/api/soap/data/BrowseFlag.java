//
// This file was generated by the Eclipse Implementation of JAXB, v4.0.2 
// See https://eclipse-ee4j.github.io/jaxb-ri 
// Any modifications to this file will be lost upon recompilation of the source schema. 
//


package io.playqd.mediaserver.api.soap.data;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for BrowseFlag.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <pre>{@code
 * <simpleType name="BrowseFlag">
 *   <restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     <enumeration value="BrowseDirectChildren"/>
 *     <enumeration value="BrowseMetadata"/>
 *   </restriction>
 * </simpleType>
 * }</pre>
 * 
 */
@XmlType(name = "BrowseFlag")
@XmlEnum
public enum BrowseFlag {

    @XmlEnumValue("BrowseDirectChildren")
    BROWSE_DIRECT_CHILDREN("BrowseDirectChildren"),
    @XmlEnumValue("BrowseMetadata")
    BROWSE_METADATA("BrowseMetadata");
    private final String value;

    BrowseFlag(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static BrowseFlag fromValue(String v) {
        for (BrowseFlag c: BrowseFlag.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}