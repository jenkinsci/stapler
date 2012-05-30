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

package org.kohsuke.stapler;

import org.kohsuke.stapler.compression.CompressionFilter;
import org.kohsuke.stapler.export.ExportConfig;
import org.kohsuke.stapler.export.NamedPathPruner;
import org.kohsuke.stapler.export.Flavor;
import org.kohsuke.stapler.export.Model;
import org.kohsuke.stapler.export.ModelBuilder;
import org.apache.commons.io.IOUtils;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.zip.GZIPOutputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.kohsuke.stapler.export.TreePruner;
import org.kohsuke.stapler.export.TreePruner.ByDepth;

/**
 * {@link StaplerResponse} implementation.
 * 
 * @author Kohsuke Kawaguchi
 */
public class ResponseImpl extends HttpServletResponseWrapper implements StaplerResponse {
    private final Stapler stapler;
    private final HttpServletResponse response;

    enum OutputMode { BYTE, CHAR }

    private OutputMode mode=null;
    private Throwable origin;

    public ResponseImpl(Stapler stapler, HttpServletResponse response) {
        super(response);
        this.stapler = stapler;
        this.response = response;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if(mode==OutputMode.CHAR)
            throw new IllegalStateException("getWriter has already been called. Its call site is in the nested exception",origin);
        if(mode==null) {
            mode = OutputMode.BYTE;
            origin = new Throwable();
        }
        return super.getOutputStream();
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if(mode==OutputMode.BYTE)
            throw new IllegalStateException("getOutputStream has already been called. Its call site is in the nested exception",origin);
        if(mode==null) {
            mode = OutputMode.CHAR;
            origin = new Throwable();
        }
        return super.getWriter();
    }

    public void forward(Object it, String url, StaplerRequest request) throws ServletException, IOException {
        stapler.invoke(request,response,it,url);
    }

    public void forwardToPreviousPage(StaplerRequest request) throws ServletException, IOException {
        String referer = request.getHeader("Referer");
        if(referer==null)   referer=".";
        sendRedirect(referer);
    }

    @Override
    public void sendRedirect(String url) throws IOException {
        // WebSphere doesn't apparently handle relative URLs, so
        // to be safe, always resolve relative URLs to absolute URLs by ourselves.
        // see http://www.nabble.com/Hudson%3A-1.262%3A-Broken-link-using-update-manager-to21067157.html
        if(url.startsWith("http://") || url.startsWith("https://") || url.startsWith("/")) {
            // absolute URLs
            super.sendRedirect(url);
            return;
        }

        // example: /foo/bar/zot + ../abc -> /foo/bar/../abc
        String base = Stapler.getCurrentRequest().getRequestURI();
        base = base.substring(0,base.lastIndexOf('/')+1);
        if(!url.equals("."))
            base += url;
        super.sendRedirect(base);
    }

    public void sendRedirect2(String url) throws IOException {
        // Tomcat doesn't encode URL (servlet spec isn't very clear on it)
        // so do the encoding by ourselves
        sendRedirect(encode(url));
    }

    public void serveFile(StaplerRequest req, URL resource, long expiration) throws ServletException, IOException {
        if(!stapler.serveStaticResource(req,this,resource,expiration))
            sendError(SC_NOT_FOUND);
    }

    public void serveFile(StaplerRequest req, URL resource) throws ServletException, IOException {
        serveFile(req,resource,-1);
    }

    public void serveLocalizedFile(StaplerRequest request, URL res) throws ServletException, IOException {
        serveLocalizedFile(request,res,-1);
    }

    public void serveLocalizedFile(StaplerRequest request, URL res, long expiration) throws ServletException, IOException {
        if(!stapler.serveStaticResource(request, this, stapler.selectResourceByLocale(res,request.getLocale()), expiration))
            sendError(SC_NOT_FOUND);
    }

    public void serveFile(StaplerRequest req, InputStream data, long lastModified, long expiration, long contentLength, String fileName) throws ServletException, IOException {
        if(!stapler.serveStaticResource(req,this,data,lastModified,expiration,contentLength,fileName))
            sendError(SC_NOT_FOUND);        
    }

    public void serveFile(StaplerRequest req, InputStream data, long lastModified, long expiration, int contentLength, String fileName) throws ServletException, IOException {
        serveFile(req,data,lastModified,expiration,(long)contentLength,fileName);
    }

    public void serveFile(StaplerRequest req, InputStream data, long lastModified, long contentLength, String fileName) throws ServletException, IOException {
        serveFile(req,data,lastModified,-1,contentLength,fileName);
    }

    public void serveFile(StaplerRequest req, InputStream data, long lastModified, int contentLength, String fileName) throws ServletException, IOException {
        serveFile(req,data,lastModified,(long)contentLength,fileName);
    }

    @SuppressWarnings({"unchecked", "rawtypes"}) // API design flaw prevents this from type-checking
    public void serveExposedBean(StaplerRequest req, Object exposedBean, Flavor flavor) throws ServletException, IOException {
        String pad=null;
        setContentType(flavor.contentType);
        Writer w = getCompressedWriter(req);

        if(flavor== Flavor.JSON) {
            pad = req.getParameter("jsonp");
            if(pad!=null) w.write(pad+'(');
        }
        
        TreePruner pruner;
        String tree = req.getParameter("tree");
        if (tree != null) {
            try {
                pruner = new NamedPathPruner(tree);
            } catch (IllegalArgumentException x) {
                throw new ServletException("Malformed tree expression: " + x, x);
            }
        } else {
            int depth = 0;
            try {
                String s = req.getParameter("depth");
                if (s != null) {
                    depth = Integer.parseInt(s);
                }
            } catch (NumberFormatException e) {
                throw new ServletException("Depth parameter must be a number");
            }
            pruner = new ByDepth(1 - depth);
        }

        ExportConfig config = new ExportConfig();
        config.prettyPrint = req.hasParameter("pretty");

        Model p = MODEL_BUILDER.get(exposedBean.getClass());
        p.writeTo(exposedBean, pruner, flavor.createDataWriter(exposedBean,w,config));

        if(pad!=null) w.write(')');
        w.close();
    }

    public OutputStream getCompressedOutputStream(HttpServletRequest req) throws IOException {
        String acceptEncoding = req.getHeader("Accept-Encoding");
        if(acceptEncoding==null || acceptEncoding.indexOf("gzip")==-1)
            return getOutputStream();   // compression not available

        addHeader("Content-Encoding","gzip");
        if (CompressionFilter.has(req))
            return getOutputStream(); // CompressionFilter will set up compression. no need to do anything
        return new GZIPOutputStream(getOutputStream());
    }

    public Writer getCompressedWriter(HttpServletRequest req) throws IOException {
        String acceptEncoding = req.getHeader("Accept-Encoding");
        if(acceptEncoding==null || acceptEncoding.indexOf("gzip")==-1)
            return getWriter();   // compression not available

        addHeader("Content-Encoding","gzip");
        if (CompressionFilter.has(req))
            return getWriter(); // CompressionFilter will set up compression. no need to do anything
        return new OutputStreamWriter(new GZIPOutputStream(getOutputStream()),getCharacterEncoding());
    }

    public int reverseProxyTo(URL url, StaplerRequest req) throws IOException {
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setDoOutput(true);

        Enumeration h = req.getHeaderNames();
        while(h.hasMoreElements()) {
            String key = (String) h.nextElement();
            Enumeration v = req.getHeaders(key);
            while (v.hasMoreElements()) {
                con.addRequestProperty(key,(String)v.nextElement());
            }
        }

        // copy the request body
        con.setRequestMethod(req.getMethod());
        // TODO: how to set request headers?
        copyAndClose(req.getInputStream(), con.getOutputStream());

        // copy the response
        int code = con.getResponseCode();
        setStatus(code,con.getResponseMessage());
        Map<String,List<String>> rspHeaders = con.getHeaderFields();
        for (Entry<String, List<String>> header : rspHeaders.entrySet()) {
            if(header.getKey()==null)   continue;   // response line
            for (String value : header.getValue()) {
                addHeader(header.getKey(),value);
            }
        }

        copyAndClose(con.getInputStream(), getOutputStream());

        return code;
    }

    private void copyAndClose(InputStream in, OutputStream out) throws IOException {
        IOUtils.copy(in, out);
        IOUtils.closeQuietly(in);
        IOUtils.closeQuietly(out);
    }

    /**
     * Escapes non-ASCII characters.
     */
    public static String encode(String s) {
        try {
            boolean escaped = false;

            StringBuilder out = new StringBuilder(s.length());

            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            OutputStreamWriter w = new OutputStreamWriter(buf,"UTF-8");

            for (int i = 0; i < s.length(); i++) {
                int c = (int) s.charAt(i);
                if (c<128 && c!=' ') {
                    out.append((char) c);
                } else {
                    // 1 char -> UTF8
                    w.write(c);
                    w.flush();
                    for (byte b : buf.toByteArray()) {
                        out.append('%');
                        out.append(toDigit((b >> 4) & 0xF));
                        out.append(toDigit(b & 0xF));
                    }
                    buf.reset();
                    escaped = true;
                }
            }

            return escaped ? out.toString() : s;
        } catch (IOException e) {
            throw new Error(e); // impossible
        }
    }

    private static char toDigit(int n) {
        char ch = Character.forDigit(n,16);
        if(ch>='a')     ch = (char)(ch-'a'+'A');
        return ch;
    }

    /*package*/ static ModelBuilder MODEL_BUILDER = new ModelBuilder();
}
