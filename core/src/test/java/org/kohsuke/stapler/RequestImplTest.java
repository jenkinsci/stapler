/*
 * The MIT License
 *
 * Copyright (c) 2015, CloudBees, Inc.
 * Copyright (c) 2020, Nikolas Falco
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
import org.jvnet.hudson.test.Issue;
import org.mockito.Mockito;

import net.sf.json.JSONObject;

import javax.servlet.ReadListener;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 * @author Nikolas Falco
 */
public class RequestImplTest {

    public static class SetterObject {
        private List<String> choices;

        @DataBoundConstructor
        public SetterObject() {
            choices = new ArrayList<>();
        }

        @SuppressWarnings("unchecked")
        @DataBoundSetter
        public void setChoices(Object choices) {
            if (choices instanceof String) {
                for (String choice : ((String) choices).split("\n")) {
                    this.choices.add(choice);
                }
            } else {
                this.choices = (List<String>) choices;
            }
        }

        public List<String> getChoices() {
            return choices;
        }

    }

    @Issue("JENKINS-61438")
    @Test
    public void verify_JSON_bind_work_with_setter_that_accept_object_type() throws Exception {
        final Stapler stapler = new Stapler();
        stapler.setWebApp(new WebApp(Mockito.mock(ServletContext.class)));
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        RequestImpl req = new RequestImpl(stapler, request, Collections.<AncestorImpl>emptyList(), null);

        JSONObject json = new JSONObject();
        json.put("$class", SetterObject.class.getName());
        json.put("choices", "1\n2\n3");

        SetterObject o = req.bindJSON(SetterObject.class, json);
        Assert.assertEquals(o.getChoices(), Arrays.asList("1", "2", "3"));
    }

    @Test
    public void test_multipart_formdata() throws IOException, ServletException {
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
            public String getHeader(String name) {
                // FILEUPLOAD-195/FILEUPLOAD-228: ignore FileUploadBase.CONTENT_LENGTH
                return null;
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
                    @Override
                    public boolean isFinished() {
                        return is.available() != 0;
                    }
                    @Override
                    public boolean isReady() {
                        return true;
                    }
                    @Override
                    public void setReadListener(ReadListener readListener) {
                        // ignored
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
