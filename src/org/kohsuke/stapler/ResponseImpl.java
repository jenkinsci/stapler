package org.kohsuke.stapler;

import org.kohsuke.stapler.export.Flavor;
import org.kohsuke.stapler.export.Model;
import org.kohsuke.stapler.export.ModelBuilder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URL;
import java.util.zip.GZIPOutputStream;

/**
 * @author Kohsuke Kawaguchi
 */
class ResponseImpl extends HttpServletResponseWrapper implements StaplerResponse {
    private final Stapler stapler;
    private final HttpServletResponse response;

    public ResponseImpl(Stapler stapler, HttpServletResponse response) {
        super(response);
        this.stapler = stapler;
        this.response = response;
    }

    public void forward(Object it, String url, StaplerRequest request) throws ServletException, IOException {
        stapler.invoke(request,response,it,url);
    }

    public void forwardToPreviousPage(StaplerRequest request) throws ServletException, IOException {
        String referer = request.getHeader("Referer");
        if(referer==null)   referer=".";
        sendRedirect(referer);
    }

    public void sendRedirect2(String url) throws IOException {
        // Tomcat doesn't encode URL (servlet spec isn't very clear on it)
        // so do the encoding by ourselves
        sendRedirect(encode(url));
    }

    public void serveFile(StaplerRequest req, URL resource, long expiration) throws ServletException, IOException {
        stapler.serveStaticResource(req,this,resource,expiration);
    }

    public void serveFile(StaplerRequest req, URL resource) throws ServletException, IOException {
        serveFile(req,resource,-1);
    }

    public void serveFile(StaplerRequest req, InputStream data, long lastModified, long expiration, int contentLength, String fileName) throws ServletException, IOException {
        stapler.serveStaticResource(req,this,data,lastModified,expiration,contentLength,fileName);
    }

    public void serveFile(StaplerRequest req, InputStream data, long lastModified, int contentLength, String fileName) throws ServletException, IOException {
        serveFile(req,data,lastModified,-1,contentLength,fileName);
    }

    public void serveExposedBean(StaplerRequest req, Object exposedBean, Flavor flavor) throws ServletException, IOException {
        String pad=null;
        PrintWriter w=null;

        setContentType(flavor.contentType);

        if(flavor== Flavor.JSON) {
            pad = req.getParameter("jsonp");
            w = getWriter();
            if(pad!=null) w.print(pad+'(');
        }

        Model p = MODEL_BUILDER.get(exposedBean.getClass());
        p.writeTo(exposedBean,flavor.createDataWriter(exposedBean,this));


        if(pad!=null) w.print(')');
    }

    public OutputStream getCompressedOutputStream(StaplerRequest req) throws IOException {
        String acceptEncoding = req.getHeader("Accept-Encoding");
        if(acceptEncoding==null || acceptEncoding.indexOf("gzip")==-1)
            return getOutputStream();   // compression not available

        addHeader("Content-Encoding","gzip");
        return new GZIPOutputStream(getOutputStream());
    }

    public Writer getCompressedWriter(StaplerRequest req) throws IOException {
        String acceptEncoding = req.getHeader("Accept-Encoding");
        if(acceptEncoding==null || acceptEncoding.indexOf("gzip")==-1)
            return getWriter();   // compression not available

        addHeader("Content-Encoding","gzip");
        return new OutputStreamWriter(new GZIPOutputStream(getOutputStream()),getCharacterEncoding());
    }


    /**
     * Escapes non-ASCII characters.
     */
    public static String encode(String s) {
        try {
            boolean escaped = false;

            StringBuffer out = new StringBuffer(s.length());

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

    private static final ModelBuilder MODEL_BUILDER = new ModelBuilder();
}
