/*
 * Copyright (c) 2004-2010, Kohsuke Kawaguchi
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this list of
 *       conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
 * THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.kohsuke.stapler.export;

import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlAttribute;
import com.sun.xml.txw2.annotation.XmlElement;

import javax.xml.namespace.QName;

/**
 * TXW interfaces to generate schema.
 *
 * @author Kohsuke Kawaguchi
 */
public interface XSD {
    public static final String URI = "http://www.w3.org/2001/XMLSchema";

    @XmlElement("schema")
    public interface Schema extends TypedXmlWriter {
        Element element();
        ComplexType complexType();
        SimpleType simpleType();
    }

    public interface Element extends Annotated {
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

    public interface Annotated extends TypedXmlWriter {
        Annotation annotation();
    }

    public interface Annotation extends TypedXmlWriter {
        void documentation(String value);
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
