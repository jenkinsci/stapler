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

package org.kohsuke.stapler.jelly;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.jelly.JellyContext;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.XMLOutputFactory;
import org.apache.commons.jelly.impl.TagScript;

import edu.umd.cs.findbugs.annotations.NonNull;
import javax.servlet.ServletContext;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Enumeration;

/**
 * Standard implementation of {@link ScriptInvoker}.
 * 
 * @author Kohsuke Kawaguchi
 */
public class DefaultScriptInvoker implements ScriptInvoker, XMLOutputFactory {
    @Override
    public void invokeScript(StaplerRequest req, StaplerResponse rsp, Script script, Object it) throws IOException, JellyTagException {
        XMLOutput xmlOutput = createXMLOutput(req, rsp, script, it);

        invokeScript(req,rsp,script,it,xmlOutput);
        
        xmlOutput.flush();
        xmlOutput.close();
    }

    @Override
    public void invokeScript(StaplerRequest req, StaplerResponse rsp, Script script, Object it, XMLOutput out) throws IOException, JellyTagException {
        JellyContext context = createContext(req,rsp,script,it);
        exportVariables(req, rsp, script, it, context);

        script.run(context,out);
    }

    protected XMLOutput createXMLOutput(StaplerRequest req, StaplerResponse rsp, Script script, Object it) throws IOException {
        // TODO: make XMLOutput auto-close OutputStream to avoid leak
        String ct = rsp.getContentType();
        XMLOutput output;
        if (ct != null && !ct.startsWith("text/html")) {
            output = XMLOutput.createXMLOutput(createOutputStream(req, rsp, script, it));
        } else {
            output = HTMLWriterOutput.create(createOutputStream(req, rsp, script, it));

        }
        return output;
    }

    private boolean doCompression(Script script) {
        if (COMPRESS_BY_DEFAULT)    return true;
        if (script instanceof TagScript) {
            TagScript ts = (TagScript) script;
            if(ts.getLocalName().equals("compress"))
                return true;
        }
        return false;
    }

    private interface OutputStreamSupplier {
        @NonNull OutputStream get() throws IOException;
    }

    private static class LazyOutputStreamSupplier implements OutputStreamSupplier {
        private final OutputStreamSupplier supplier;
        private volatile OutputStream out;

        private LazyOutputStreamSupplier(OutputStreamSupplier supplier) {
            this.supplier = supplier;
        }

        @Override
        @NonNull
        public OutputStream get() throws IOException {
            if (out == null) {
                synchronized (this) {
                    if (out == null) {
                        out = supplier.get();
                    }
                }
            }
            return out;
        }
    }

    protected OutputStream createOutputStream(StaplerRequest req, StaplerResponse rsp, Script script, Object it) throws IOException {
        OutputStreamSupplier out = new LazyOutputStreamSupplier(() -> {
            req.getWebApp().getDispatchValidator().requireDispatchAllowed(req, rsp);
            return doCompression(script) ? rsp.getCompressedOutputStream(req) : new BufferedOutputStream(rsp.getOutputStream());
        });
        return new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                out.get().write(b);
            }

            @Override
            public void write(byte[] b) throws IOException {
                out.get().write(b);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                out.get().write(b, off, len);
            }

            @Override
            public void flush() throws IOException {
                // flushing ServletOutputStream causes Tomcat to
                // send out headers, making it impossible to set contentType from the script.
                // so don't let Jelly flush.
            }

            @Override
            public void close() throws IOException {
                out.get().close();
            }
        };
    }

    protected void exportVariables(StaplerRequest req, StaplerResponse rsp, Script script, Object it, JellyContext context) {
        Enumeration en = req.getAttributeNames();
        // expose request attributes, just like JSP
        while (en.hasMoreElements()) {
            String name = (String) en.nextElement();
            context.setVariable(name,req.getAttribute(name));
        }

        context.setVariable("request",req);
        context.setVariable("response",rsp);
        context.setVariable("it",it);
        ServletContext servletContext = req.getServletContext();
        context.setVariable("servletContext",servletContext);
        context.setVariable("app",servletContext.getAttribute("app"));
        // property bag to store request scope variables
        context.setVariable("requestScope",context.getVariables());
        // this variable is needed to make "jelly:fmt" taglib work correctly
        context.setVariable("org.apache.commons.jelly.tags.fmt.locale",req.getLocale());
    }

    protected JellyContext createContext(final StaplerRequest req, StaplerResponse rsp, Script script, Object it) {
        CustomJellyContext context = new CustomJellyContext();
        // let Jelly see the whole classes
        context.setClassLoader(req.getWebApp().getClassLoader());
        // so TagScript.getBodyText() will use HTMLWriterOutput
        context.setVariable(XMLOutputFactory.class.getName(), this);
        return context;
    }

    @Override
    public XMLOutput createXMLOutput(Writer writer, boolean escapeText) {
        StaplerResponse rsp = Stapler.getCurrentResponse();
        String ct = rsp!=null ? rsp.getContentType() : "?";
        if (ct != null && !ct.startsWith("text/html"))
            return XMLOutput.createXMLOutput(writer, escapeText);
        return HTMLWriterOutput.create(writer, escapeText);
    }

    /**
     * Whether gzip compression of the dynamic content is enabled by default or not.
     *
     * <p>
     * For non-trivial web applications, where the performance matters, it is normally a good trade-off to spend
     * a bit of CPU cycles to compress data. This is because:
     *
     * <ul>
     * <li>CPU is already 1 or 2 order of magnitude faster than RAM and network.
     * <li>CPU is getting faster than any other components, such as RAM and network.
     * <li>Because of the TCP window slow start, on a large latency network, compression makes difference in
     *     the order of 100ms to 1sec to the completion of a request by saving multiple roundtrips.
     * </ul>
     *
     * Stuff rendered by Jelly is predominantly text, so the compression would work well.
     *
     * @see <a href="http://www.slideshare.net/guest22d4179/latency-trumps-all">Latency Trumps All</a>
     */
    @SuppressFBWarnings(value = "MS_SHOULD_BE_FINAL", justification = "Legacy switch.")
    public static boolean COMPRESS_BY_DEFAULT = Boolean.parseBoolean(System.getProperty(DefaultScriptInvoker.class.getName()+".compress","true"));
}
