/*
 * The MIT License
 *
 * Copyright (c) 2015, CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.kohsuke.stapler;

import org.apache.commons.fileupload.FileItem;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.junit.Assert;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class RequestImplTest {

    @Test
    public void test_mutipart_formdata() throws IOException, ServletException {
        final Stapler stapler = new Stapler();
        final byte[] buf = generateMultipartData();
        final ByteArrayInputStream is = new ByteArrayInputStream(buf);
        final MockRequest mockRequest = new MockRequest() {
            @Override
            public String getContentType() {
                return "multipart/form-data; boundary=mpboundary";
            }
            @Override
            public String getCharacterEncoding() {
                return "UTF-8";
            }
            @Override
            public int getContentLength() {
                return buf.length;
            }

            @Override
            public Enumeration getParameterNames() {
                return Collections.enumeration(Arrays.asList("p1"));
            }

            @Override
            public ServletInputStream getInputStream() throws IOException {
                return new ServletInputStream() {
                    @Override
                    public int read() throws IOException {
                        return is.read();
                    }
                    @Override
                    public int read(byte[] b) throws IOException {
                        return is.read(b);
                    }
                    @Override
                    public int read(byte[] b, int off, int len) throws IOException {
                        return is.read(b, off, len);
                    }                    
                };
            }
        };
        
        RequestImpl request = new RequestImpl(stapler, mockRequest, Collections.<AncestorImpl>emptyList(), null);

        // Check that we can get the Form Fields. See https://github.com/stapler/stapler/issues/52
        Assert.assertEquals("text1_val", request.getParameter("text1"));
        Assert.assertEquals("text2_val", request.getParameter("text2"));
        
        // Check that we can get the file
        FileItem fileItem = request.getFileItem("pomFile");
        Assert.assertNotNull(fileItem);
        
        // Check getParameterValues
        Assert.assertEquals("text1_val", request.getParameterValues("text1")[0]);
        
        // Check getParameterNames
        Assert.assertTrue(Collections.list(request.getParameterNames()).contains("p1"));
        Assert.assertTrue(Collections.list(request.getParameterNames()).contains("text1"));
        
        // Check getParameterMap
        Assert.assertTrue(request.getParameterMap().containsKey("text1"));        
    }

    private byte[] generateMultipartData() throws IOException {
        MultipartEntityBuilder reqEntityBuilder = MultipartEntityBuilder.create();

        reqEntityBuilder.setBoundary("mpboundary");
        reqEntityBuilder.addBinaryBody("pomFile", new File("./pom.xml"), ContentType.TEXT_XML, "pom.xml");
        reqEntityBuilder.addTextBody("text1", "text1_val");
        reqEntityBuilder.addTextBody("text2", "text2_val");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            reqEntityBuilder.build().writeTo(outputStream);
            outputStream.flush();
            return outputStream.toByteArray();
        } finally {
            outputStream.close();
        }
    }
}
