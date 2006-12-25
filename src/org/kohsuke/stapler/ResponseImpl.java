package org.kohsuke.stapler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.InputStream;
import java.net.URL;

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

    public void serveFile(StaplerRequest req, URL resource) throws ServletException, IOException {
        stapler.serveStaticResource(req,this,resource);
    }

    public void serveFile(StaplerRequest req, InputStream data, long lastModified, int contentLength, String fileName) throws ServletException, IOException {
        stapler.serveStaticResource(req,this,data,lastModified,contentLength,fileName);
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
}
