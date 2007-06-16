package org.kohsuke.stapler.export;

import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlAttribute;

import javax.xml.namespace.QName;

/**
 * TXW interfaces to generate schema.
 *
 * @author Kohsuke Kawaguchi
 */
public interface XSD {
    public static final String URI = "http://www.w3.org/2001/XMLSchema";

    public interface Schema extends TypedXmlWriter {
        Element element();
        ComplexType complexType();
        SimpleType simpleType();
    }

    public interface Element extends TypedXmlWriter {
        @XmlAttribute
        Element name(String v);
        @XmlAttribute
        Element type(QName t);

        @XmlAttribute
        Element minOccurs(int i);
        @XmlAttribute
        Element maxOccurs(String v);
    }

    public interface ComplexType extends TypedXmlWriter {
        @XmlAttribute
        ComplexType name(String v);
        ContentModel sequence();
        ComplexContent complexContent();
    }

    public interface ComplexContent extends TypedXmlWriter {
        Restriction extension();
    }

    public interface ContentModel extends TypedXmlWriter {
        Element element();
    }

    public interface SimpleType extends TypedXmlWriter {
        @XmlAttribute
        SimpleType name(String v);
        Restriction restriction();
    }

    public interface Restriction extends TypedXmlWriter {
        @XmlAttribute
        Restriction base(QName t);
        // for simple type
        Enumeration enumeration();
        // for complex type
        ContentModel sequence();
    }

    public interface Enumeration extends TypedXmlWriter {
        @XmlAttribute
        void value(String v);
    }

    public abstract class Types {
        public static final QName STRING = t("string");
        public static final QName BOOLEAN = t("boolean");
        public static final QName INT = t("int");
        public static final QName LONG = t("long");
        public static final QName ANYTYPE = t("anyType");

        private static QName t(String name) {
            return new QName(XSD.URI, name);
        }
    }
}
