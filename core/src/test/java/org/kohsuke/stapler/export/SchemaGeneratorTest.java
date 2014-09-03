/*
 * Copyright (c) 2012, Jesse Glick
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.kohsuke.stapler.export;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import static org.junit.Assert.*;
import org.junit.Test;
import org.xml.sax.SAXParseException;

public class SchemaGeneratorTest {

    @Test public void basics() throws Exception {
        validate(new XMLDataWriterTest.X(), XMLDataWriterTest.X.class);
    }

    /* TODO currently fails
    @Test public void inheritance() throws Exception {
        validate(new XMLDataWriterTest.Container(), XMLDataWriterTest.Container.class);
    }
    */
    
    private static <T> void validate(T bean, Class<T> clazz) throws Exception {
        Model<T> model = new ModelBuilder().get(clazz);
        ByteArrayOutputStream schema = new ByteArrayOutputStream();
        new SchemaGenerator(model).generateSchema(new StreamResult(schema));
        StringWriter xml = new StringWriter();
        model.writeTo(bean, Flavor.XML.createDataWriter(bean, xml));
        try {
            SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(new StreamSource(new ByteArrayInputStream(schema.toByteArray()))).newValidator().validate(new StreamSource(new StringReader(xml.toString())));
        } catch (SAXParseException x) {
            fail(x + "\n" + xml + "\n" + schema);
        }
    }

}
