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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.json.JSONObject;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.converters.DoubleConverter;
import org.apache.commons.beanutils.converters.FloatConverter;
import org.apache.commons.beanutils.converters.IntegerConverter;
import org.apache.commons.fileupload2.core.FileItem;
import org.apache.commons.io.IOUtils;
import org.kohsuke.stapler.bind.BoundObjectTable;

/**
 * Maps an HTTP request to a method call / JSP invocation against a model object
 * by evaluating the request URL in a EL-ish way.
 *
 * <p>
 * This servlet should be used as the default servlet.
 *
 * @author Kohsuke Kawaguchi
 */
public class Stapler extends HttpServlet {

    /**
     * Exceptions we don't want to print a stacktrace for because they are normal behaviour.
     * [JENKINS-4834] A stack trace is too noisy for this; could just need to log in.
     */
    private static final Set<String> EXCEPTIONS_DO_NOT_PRINT_STACKTRACE = Set.of(
            "org.acegisecurity.AccessDeniedException", "org.springframework.security.access.AccessDeniedException");

    private /*final*/ ServletContext context;

    @SuppressFBWarnings(value = "SE_BAD_FIELD", justification = "The Stapler class is not expected to be serialized.")
    private /*final*/ WebApp webApp;

    /**
     * All the resources that exist in {@link ServletContext#getResource(String)},
     * as a cache.
     *
     * If this field is null, no cache.
     */
    private volatile Map<String, URL> resourcePaths;

    /**
     * Temporarily updates the thread name to reflect the request being processed.
     * On by default for convenience, but for webapps that use filters, use
     * {@link DiagnosticThreadNameFilter} as the first filter and switch this off.
     */
    private boolean diagnosticThreadName = true;

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        this.context = servletConfig.getServletContext();
        this.webApp = WebApp.get(context);
        String defaultEncodings = servletConfig.getInitParameter("default-encodings");
        if (defaultEncodings != null) {
            for (String t : defaultEncodings.split(";")) {
                t = t.trim();
                int idx = t.indexOf('=');
                if (idx < 0) {
                    throw new ServletException("Invalid format: " + t);
                }
                webApp.defaultEncodingForStaticResources.put(t.substring(0, idx), t.substring(idx + 1));
            }
        }
        buildResourcePaths();
        webApp.addStaplerServlet(servletConfig.getServletName(), this);

        String v = servletConfig.getInitParameter("diagnosticThreadName");
        if (v != null) {
            diagnosticThreadName = Boolean.parseBoolean(v);
        }
    }

    /**
     * Rebuild the internal cache for static resources.
     */
    public void buildResourcePaths() {
        try {
            if (Boolean.getBoolean(Stapler.class.getName() + ".noResourcePathCache")) {
                resourcePaths = null;
                return;
            }

            Map<String, URL> paths = new HashMap<>();
            Stack<String> q = new Stack<>();
            q.push("/");
            while (!q.isEmpty()) {
                String dir = q.pop();
                Set<String> children = context.getResourcePaths(dir);
                if (children != null) {
                    for (String child : children) {
                        if (child.endsWith("/")) {
                            q.push(child);
                        } else {
                            URL v = context.getResource(child);
                            if (v == null) {
                                resourcePaths = null;
                                return; // this can't happen. abort with no cache
                            }
                            paths.put(child, v);
                        }
                    }
                }
            }

            resourcePaths = Collections.unmodifiableMap(paths);
        } catch (MalformedURLException e) {
            resourcePaths = null; // abort
        }
    }

    public WebApp getWebApp() {
        return webApp;
    }

    /*package*/ void setWebApp(WebApp webApp) {
        this.webApp = webApp;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse rsp) throws ServletException, IOException {
        Thread t = Thread.currentThread();
        final String oldName = t.getName();
        try {
            if (diagnosticThreadName) {
                t.setName("Handling " + req.getMethod() + ' ' + req.getRequestURI() + " : " + oldName);
            }

            String servletPath = getServletPath(req);

            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Processing request for " + servletPath);
            }

            if (servletPath.startsWith(BoundObjectTable.PREFIX)) {
                // serving exported objects
                invoke(req, rsp, webApp.boundObjectTable, servletPath.substring(BoundObjectTable.PREFIX.length()));
                return;
            }

            boolean staticLink = false;

            if (servletPath.startsWith("/static/")) {
                // skip "/static/..../ portion
                int idx = servletPath.indexOf('/', 8);
                if (idx != -1) {
                    servletPath = servletPath.substring(idx);
                    staticLink = true;
                }
            }

            String lowerPath = servletPath.toLowerCase(Locale.ENGLISH);
            if (!servletPath.isEmpty() && !lowerPath.startsWith("/web-inf") && !lowerPath.startsWith("/meta-inf")) {
                // getResource requires '/' prefix (and resin insists on that, too) but servletPath can be empty string
                // (JENKINS-879)
                // so make sure servletPath is at least length 1 before calling getResource()

                // WEB-INF and META-INF are by convention hidden and not supposed to be rendered to clients
                // (JENKINS-7457/JENKINS-11538)
                // also note that Windows allows "/WEB-INF./" to refer to refer to this directory.
                // here we also reject that (by rejecting /WEB-INF*)

                OpenConnection con = openResourcePathByLocale(req, servletPath);
                if (con != null) {
                    long expires = MetaClass.NO_CACHE ? 0 : 24L * 60 * 60 * 1000; /*1 day*/
                    if (staticLink) {
                        expires *= 365; // static resources are unique, so we can set a long expiration date
                    }
                    if (serveStaticResource(req, new ResponseImpl(this, rsp), con, expires)) {
                        return; // done
                    }
                }
            }

            Object root = webApp.getApp();
            if (root == null) {
                throw new ServletException("there's no \"app\" attribute in the application context.");
            }

            // consider reusing this ArrayList.
            invoke(req, rsp, root, servletPath);
        } finally {
            t.setName(oldName);
        }
    }

    /**
     * Tomcat and GlassFish returns a fresh {@link InputStream} every time
     * {@link URLConnection#getInputStream()} is invoked in their {@code org.apache.naming.resources.DirContextURLConnection}.
     *
     * <p>
     * All the other {@link URLConnection}s in JDK don't do this --- they return the same {@link InputStream},
     * even the one for the file:// URLs.
     *
     * <p>
     * In Tomcat (and most likely in GlassFish, although this is not verified), resource look up on
     * {@link ServletContext#getResource(String)} successfully returns non-existent URL, and
     * the failure can be only detected by {@link IOException} from {@link URLConnection#getInputStream()}.
     *
     * <p>
     * Therefore, for the whole thing to work without resource leak, once we open {@link InputStream}
     * for the sake of really making sure that the resource exists, we need to hang on to that stream.
     *
     * <p>
     * Hence the need for this tuple.
     */
    private static final class OpenConnection {
        final URLConnection connection;
        final InputStream stream;

        private OpenConnection(URLConnection connection, InputStream stream) {
            this.connection = connection;
            this.stream = stream;
        }

        private OpenConnection(URLConnection connection) throws IOException {
            this(connection, connection.getInputStream());
        }

        private void close() throws IOException {
            stream.close();
        }

        /**
         * Can't just use {@code connection.getLastModified()} because
         * of a file descriptor leak in {@code JarURLConnection}.
         * See http://sourceforge.net/p/freemarker/bugs/189/
         */
        public long getLastModified() {
            if (connection instanceof JarURLConnection) {
                // There is a bug in sun's jar url connection that causes file handle leaks when calling
                // getLastModified()
                // Since the time stamps of jar file contents can't vary independent from the jar file timestamp, just
                // use
                // the jar file timestamp
                URL jarURL = ((JarURLConnection) connection).getJarFileURL();
                if (jarURL.getProtocol().equals("file")) {
                    // Return the last modified time of the underlying file - saves some opening and closing
                    return determineLastModified(jarURL);
                } else {
                    // Use the URL mechanism
                    URLConnection jarConn = null;
                    try {
                        jarConn = openConnection(jarURL);
                        return jarConn.getLastModified();
                    } catch (IOException e) {
                        return -1;
                    } finally {
                        if (jarConn != null) {
                            try {
                                IOUtils.closeQuietly(jarConn.getInputStream());
                            } catch (IOException e) {
                                // ignore this error
                            }
                        }
                    }
                }
            } else {
                return connection.getLastModified();
            }
        }

        @SuppressFBWarnings(value = "PATH_TRAVERSAL_IN", justification = "Protected by checks at other layers.")
        private long determineLastModified(URL jarURL) {
            // TODO Fix this so that it works correctly. Needs to handle metacharacters
            // Windows UNC, etc. Should use URL.toURI, Paths.get(URI), and Files.getLastModifiedTime.
            return new File(jarURL.getFile()).lastModified();
        }
    }

    /**
     * Logic for performing locale driven resource selection.
     *
     * Different subtypes provide different meanings for the 'path' parameter.
     */
    private abstract class LocaleDrivenResourceSelector {
        /**
         * The 'path' is divided into the base part and the extension, and the locale-specific
         * suffix is inserted to the base portion. {@link #map(String)} is used to convert
         * the combined path into {@link URL}, until we find one that works.
         *
         * <p>
         * The syntax of the locale specific resource is the same as property file localization.
         * So Japanese resource for {@code foo.html} would be named {@code foo_ja.html}.
         *
         * @param path
         *      path/URL-like string that represents the path of the base resource,
         *      say "foo/bar/index.html" or "file:///a/b/c/d/efg.png"
         * @param locale
         *      The preferred locale
         * @param fallback
         *      The {@link URL} representation of the {@code path} parameter
         *      Used as a fallback.
         */
        OpenConnection open(String path, Locale locale, URL fallback) throws IOException {
            String s = path;
            int idx = s.lastIndexOf('.');
            if (idx < 0) { // no file extension, so no locale switch available
                return openURL(fallback);
            }
            String base = s.substring(0, idx);
            String ext = s.substring(idx);
            if (ext.indexOf('/') >= 0) { // the '.' we found was not an extension separator
                return openURL(fallback);
            }

            // RegExps found in Locale JavaDoc
            String language = locale.getLanguage();
            boolean languageOk = language.matches("^[a-zA-Z]{2,8}$");
            String country = locale.getCountry();
            boolean countryOk = country.matches("^[a-zA-Z]{2}|[0-9]{3}$");
            String variant = locale.getVariant();

            String SUBTAG = "(?:[0-9][0-9a-zA-Z]{3}|[0-9a-zA-Z]{5,8})";
            boolean variantOk = variant.matches("^" + SUBTAG + "(?:[_\\-]" + SUBTAG + ")*$");

            OpenConnection con;

            // try locale specific resources first.
            if (languageOk && countryOk && variantOk) {
                con = openURL(map(base + '_' + language + '_' + country + '_' + variant + ext));
                if (con != null) {
                    return con;
                }
            }
            if (languageOk && countryOk) {
                con = openURL(map(base + '_' + language + '_' + country + ext));
                if (con != null) {
                    return con;
                }
            }
            if (languageOk) {
                con = openURL(map(base + '_' + language + ext));
                if (con != null) {
                    return con;
                }
            }
            // default
            return openURL(fallback);
        }

        /**
         * Maps the 'path' into {@link URL}.
         */
        abstract URL map(String path) throws IOException;
    }

    @SuppressFBWarnings(value = "SE_BAD_FIELD", justification = "The Stapler class is not expected to be serialized.")
    private final LocaleDrivenResourceSelector resourcePathLocaleSelector = new LocaleDrivenResourceSelector() {
        @Override
        URL map(String path) throws IOException {
            URL override = LocaleDrivenResourceProvider.lookupResource(path);
            if (override != null) {
                return override;
            }
            return getResource(path);
        }
    };

    private OpenConnection openResourcePathByLocale(HttpServletRequest req, String resourcePath) throws IOException {
        URL url = getResource(resourcePath);
        if (url == null) {
            return null;
        }

        // hopefully HotSpot would be able to inline all the virtual calls in here
        return resourcePathLocaleSelector.open(resourcePath, req.getLocale(), url);
    }

    /**
     * {@link LocaleDrivenResourceSelector} that uses a complete URL as 'path'
     */
    @SuppressFBWarnings(value = "SE_BAD_FIELD", justification = "The Stapler class is not expected to be serialized.")
    private final LocaleDrivenResourceSelector urlLocaleSelector = new LocaleDrivenResourceSelector() {
        @Override
        URL map(String url) throws IOException {
            URL override = LocaleDrivenResourceProvider.lookupResource(url);
            if (override != null) {
                return override;
            }
            return new URL(url);
        }
    };

    OpenConnection selectResourceByLocale(URL url, Locale locale) throws IOException {
        // hopefully HotSpot would be able to inline all the virtual calls in here
        return urlLocaleSelector.open(url.toString(), locale, url);
    }

    /**
     * Serves the specified {@link URLConnection} as a static resource.
     */
    boolean serveStaticResource(HttpServletRequest req, StaplerResponse rsp, OpenConnection con, long expiration)
            throws IOException {
        if (con == null) {
            return false;
        }
        try {
            return serveStaticResource(
                    req,
                    rsp,
                    con.stream,
                    con.getLastModified(),
                    expiration,
                    con.connection.getContentLength(),
                    con.connection.getURL().toString());
        } finally {
            con.close();
        }
    }

    /**
     * Serves the specified {@link URL} as a static resource.
     */
    boolean serveStaticResource(HttpServletRequest req, StaplerResponse rsp, URL url, long expiration)
            throws IOException {
        return serveStaticResource(req, rsp, openURL(url), expiration);
    }

    /**
     * Opens URL, with error handling to absorb container differences.
     * <p>
     * This method returns null if the resource pointed by URL doesn't exist. The initial attempt was to
     * distinguish "resource exists but failed to load" vs "resource doesn't exist", but as more reports
     * from the field come in, we discovered that it's impossible to make such a distinction and work with
     * many environments both at the same time.
     */
    private OpenConnection openURL(URL url) {
        if (url == null) {
            return null;
        }

        // jetty reports directories    as URLs, which isn't what this is intended for,
        // so check and reject.
        File f = toFile(url);
        if (f != null && f.isDirectory()) {
            return null;
        }

        try {
            // in normal protocol handlers like http/file, openConnection doesn't actually open a connection
            // (that's deferred until URLConnection.connect()), so this method doesn't result in an error,
            // even if URL points to something that doesn't exist.
            //
            // but we've heard a report from http://github.com/adreghiciu that some URLS backed by custom
            // protocol handlers can throw an exception as early as here. So treat this IOException
            // as "the resource pointed by URL is missing".
            URLConnection con = openConnection(url);

            OpenConnection c = new OpenConnection(con);
            // Some URLs backed by custom broken protocol handler can return null from getInputStream(),
            // so let's be defensive here. An example of that is an OSGi container --- unfortunately
            // we don't have more details than that.
            if (c.stream == null) {
                return null;
            }
            return c;
        } catch (IOException e) {
            // Tomcat only reports a missing resource error here, from URLConnection.getInputStream()
            return null;
        }
    }

    @SuppressFBWarnings(value = "URLCONNECTION_SSRF_FD", justification = "Not relevant in this situation.")
    private static URLConnection openConnection(URL url) throws IOException {
        return url.openConnection();
    }

    /**
     * Serves the specified {@link InputStream} as a static resource.
     *
     * @param contentLength
     *      if the length of the input stream is known in advance, specify that value
     *      so that HTTP keep-alive works. Otherwise specify -1 to indicate that the length is unknown.
     * @param expiration
     *      The number of milliseconds until the resource will "expire".
     *      Until it expires the browser will be allowed to cache it
     *      and serve it without checking back with the server.
     *      After it expires, the client will send conditional GET to
     *      check if the resource is actually modified or not.
     *      If 0, it will immediately expire.
     * @param fileName
     *      file name of this resource. Used to determine the MIME type.
     *      Since the only important portion is the file extension, this could be just a file name,
     *      or a full path name, or even a pseudo file name that doesn't actually exist.
     *      It supports both '/' and '\\' as the path separator.
     * @return false
     *      if the resource doesn't exist.
     */
    boolean serveStaticResource(
            HttpServletRequest req,
            StaplerResponse rsp,
            InputStream in,
            long lastModified,
            long expiration,
            long contentLength,
            String fileName)
            throws IOException {
        try {
            { // send out Last-Modified, or check If-Modified-Since
                if (lastModified != 0) {
                    String since = req.getHeader("If-Modified-Since");
                    SimpleDateFormat format = HTTP_DATE_FORMAT.get();
                    if (since != null) {
                        try {
                            long ims = format.parse(since).getTime();
                            if (lastModified < ims + 1000) {
                                // +1000 because date header is second-precision and Java has milli-second precision
                                rsp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                                return true;
                            }
                        } catch (ParseException e) {
                            // just ignore and serve the content
                        } catch (NumberFormatException e) {
                            // trying to locate a bug with Jetty
                            getServletContext().log("Error parsing [" + since + "]", e);
                            throw e;
                        }
                    }

                    String lastModifiedStr = format.format(new Date(lastModified));
                    rsp.setHeader("Last-Modified", lastModifiedStr);
                    if (expiration <= 0) {
                        rsp.setHeader("Expires", lastModifiedStr);
                    } else {
                        rsp.setHeader("Expires", format.format(new Date(new Date().getTime() + expiration)));
                    }
                }
            }

            rsp.setHeader("Accept-Ranges", "bytes"); // advertize that we support the range header

            String mimeType = getMimeType(fileName);
            rsp.setContentType(mimeType);

            // use nosniff to enforce the content type we are setting above, instead of letting browser
            // guess it on its own. I found http://security.stackexchange.com/questions/12896/
            // a comprehensive discussion on this topic
            rsp.setHeader("X-Content-Type-Options", "nosniff");

            // somewhat limited implementation of the partial GET
            String range = req.getHeader("Range");
            if (range != null
                    && contentLength != -1) { // I'm lazy and only implementing this for known content length case
                if (range.startsWith("bytes=")) {
                    range = range.substring(6);
                    Matcher m = RANGE_SPEC.matcher(range);
                    if (m.matches()) {
                        long s = Long.parseLong(m.group(1));
                        long e = !m.group(2).isEmpty()
                                ? Long.parseLong(m.group(2)) + 1 // range set is inclusive
                                : contentLength; // unspecified value means "all the way to the end"
                        e = Math.min(e, contentLength);

                        // ritual for responding to a partial GET
                        rsp.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                        rsp.setHeader(
                                "Content-Range",
                                "bytes " + s + "-" + (e - 1) + '/' + contentLength); // end is inclusive.

                        // prepare to send the partial content
                        DataInputStream dis = new DataInputStream(in);
                        long toSkip = s, thisSkip;
                        while (toSkip > 0
                                && (thisSkip = dis.skipBytes((int) Math.min(toSkip, Integer.MAX_VALUE))) > 0) {
                            toSkip -= thisSkip;
                        }
                        if (toSkip > 0) {
                            throw new IOException("skipBytes failure (" + toSkip + " of " + s + " bytes unskipped)");
                        }
                        in = new TruncatedInputStream(in, e - s);
                        contentLength = Math.min(e - s, contentLength);
                    }
                    // if the Range header doesn't look like what we can handle,
                    // pretend as if we didn't understand it, instead of doing a proper error reporting
                }
            }

            if (contentLength != -1) {
                rsp.setHeader("Content-Length", Long.toString(contentLength));
            }
            OutputStream out = rsp.getOutputStream();
            in.transferTo(out);
            out.close();
            return true;
        } finally {
            in.close();
        }
    }

    /**
     * Strings like "5-300", "0-900", or "100-"
     */
    private static final Pattern RANGE_SPEC = Pattern.compile("([\\d]+)-([\\d]*)");

    private String getMimeType(String fileName) {
        if (fileName.startsWith("mime-type:")) {
            return fileName.substring("mime-type:".length());
        }

        int idx = fileName.lastIndexOf('/');
        fileName = fileName.substring(idx + 1);
        idx = fileName.lastIndexOf('\\');
        fileName = fileName.substring(idx + 1);

        String mimeType = getServletContext().getMimeType(fileName);
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }
        if (webApp.defaultEncodingForStaticResources.containsKey(mimeType)) {
            mimeType += ";charset=" + webApp.defaultEncodingForStaticResources.get(mimeType);
        }
        return mimeType;
    }

    /**
     * If the URL is "file://", return its file representation.
     *
     * See http://weblogs.java.net/blog/kohsuke/archive/2007/04/how_to_convert.html
     */
    @SuppressFBWarnings(value = "PATH_TRAVERSAL_IN", justification = "Protected by checks at other layers.")
    /*package for test*/ File toFile(URL url) {
        // TODO Fix this so that it works correctly. Should use URI and Path.
        String urlstr = url.toExternalForm();
        if (!urlstr.startsWith("file:")) {
            return null;
        }

        //  File(String) does fs.normalize, which is really forgiving in fixing up
        // malformed stuff. I couldn't make the other URL.toURI() or File(URI) work
        // in all the cases that we test
        return new File(URLDecoder.decode(urlstr.substring(5), StandardCharsets.UTF_8));
    }

    /**
     * Performs stapler processing on the given root object and request URL.
     */
    public void invoke(HttpServletRequest req, HttpServletResponse rsp, Object root, String url)
            throws IOException, ServletException {
        RequestImpl sreq = new RequestImpl(this, req, new ArrayList<>(), new TokenList(url));
        RequestImpl oreq = CURRENT_REQUEST.get();
        CURRENT_REQUEST.set(sreq);

        ResponseImpl srsp = new ResponseImpl(this, rsp);
        ResponseImpl orsp = CURRENT_RESPONSE.get();
        CURRENT_RESPONSE.set(srsp);

        try {
            invoke(sreq, srsp, root);
        } finally {
            CURRENT_REQUEST.set(oreq);
            CURRENT_RESPONSE.set(orsp);
        }
    }

    /**
     * Try to dispatch the request against the given node, and if there's no route to deliver a request, return false.
     *
     * <p>
     * If a route to deliver a request is found but it failed to handle the request, the request is considered
     * handled and the method returns true.
     *
     * @see #invoke(RequestImpl, ResponseImpl, Object)
     */
    boolean tryInvoke(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException {
        Dispatcher.anonymizedTraceEval(req, rsp, node, "%s: Dispatch");
        if (Dispatcher.traceable()) {
            Dispatcher.traceEval(req, rsp, node);
        }

        if (node instanceof StaplerProxy) {
            Dispatcher.anonymizedTraceEval(req, rsp, node, "%s: StaplerProxy.getTarget()");
            if (Dispatcher.traceable()) {
                Dispatcher.traceEval(req, rsp, node, "((StaplerProxy)", ").getTarget()");
            }
            Object n = null;
            try {
                n = ((StaplerProxy) node).getTarget();
            } catch (RuntimeException e) {
                if (Function.renderResponse(req, rsp, node, e)) {
                    return true; // let the exception serve the request and we are done
                } else {
                    throw e; // unprocessed exception
                }
            }
            if (n == node) {
                // if the proxy returns itself, assume that it doesn't want to proxy.
            } else if (n == null) {
                // if null, no one will handle the request
                return false;
            } else {
                // recursion helps debugging by leaving the trace in the stack.
                invoke(req, rsp, n);
                return true;
            }
        }

        // adds this node to ancestor list
        AncestorImpl ancestor = new AncestorImpl(req, node);
        ancestor.addToOwner();

        // try overrides
        if (node instanceof StaplerOverridable o) {
            Collection<?> list = o.getOverrides();
            if (list != null) {
                int count = 0;
                for (Object subject : list) {
                    if (subject == null) {
                        continue;
                    }
                    Dispatcher.anonymizedTraceEval(req, rsp, node, "%s: StaplerOverridable.getOverrides()[i]");
                    if (Dispatcher.traceable()) {
                        Dispatcher.traceEval(
                                req, rsp, node, "((StaplerOverridable)", ").getOverrides()[" + (count++) + ']');
                    }

                    if (tryInvoke(req, rsp, subject)) {
                        return true;
                    }
                }
            }
        }

        MetaClass metaClass = webApp.getMetaClass(node);

        try {
            for (Dispatcher d : metaClass.dispatchers) {
                if (d.dispatch(req, rsp, node)) {
                    if (LOGGER.isLoggable(Level.FINER)) {
                        LOGGER.finer("Handled by " + d);
                    }
                    return true;
                }
            }
        } catch (IllegalAccessException e) {
            // this should never really happen
            if (!isSocketException(e)) {
                getServletContext().log("Error while serving " + req.getRequestURL(), e);
            }
            throw new ServletException(e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause == null) { // ???
                getServletContext().log("Error while serving " + req.getRequestURL(), e);
                throw new ServletException();
            }

            // allow the exception from the dispatch to be handled. This is handy to throw HttpResponse as an exception
            // from the getXyz method.
            for (HttpResponseRenderer r : webApp.getResponseRenderers()) {
                if (r.generateResponse(req, rsp, node, cause)) {
                    return true;
                }
            }

            StringBuffer url = req.getRequestURL();
            if (cause instanceof IOException) {
                if (!isSocketException(e)) {
                    getServletContext().log("Error while serving " + url, e);
                }
                throw (IOException) cause;
            }
            if (cause instanceof ServletException) {
                if (!isSocketException(e)) {
                    getServletContext().log("Error while serving " + url, e);
                }
                throw (ServletException) cause;
            }
            for (Class<?> c = cause.getClass(); c != null; c = c.getSuperclass()) {
                if (c == Object.class) {
                    if (!isSocketException(e)) {
                        getServletContext().log("Error while serving " + url, e);
                    }
                } else if (EXCEPTIONS_DO_NOT_PRINT_STACKTRACE.contains(c.getName())) {
                    getServletContext().log("While serving " + url + ": " + cause);
                    break;
                }
            }
            throw new ServletException(cause);
        }

        if (node instanceof StaplerFallback) {
            Dispatcher.anonymizedTraceEval(req, rsp, node, "%s: StaplerFallback.getStaplerFallback()");
            if (Dispatcher.traceable()) {
                Dispatcher.traceEval(req, rsp, node, "((StaplerFallback)", ").getStaplerFallback()");
            }
            Object n;
            try {
                n = ((StaplerFallback) node).getStaplerFallback();
            } catch (RuntimeException e) {
                if (Function.renderResponse(req, rsp, node, e)) {
                    return true; // let the exception serve the request and we are done
                } else {
                    throw e; // unprocessed exception
                }
            }
            if (n != node && n != null) {
                // delegate to the fallback object
                invoke(req, rsp, n);
                return true;
            }
        }

        return false;
    }

    /**
     * Used to detect exceptions thrown when writing content that seem to be due merely to a closed socket.
     * @param x an exception that got caught
     * @return true if this looks like a closed stream, false for some real problem
     * @since 1.222
     */
    public static boolean isSocketException(Throwable x) { // JENKINS-10524
        if (x == null) {
            return false;
        }
        if (x instanceof SocketException) {
            return true;
        }
        if (String.valueOf(x.getMessage()).equals("Broken pipe")) { // TBD I18N
            return true;
        }
        // JENKINS-20074: various things that Jetty and other components could throw
        if (x instanceof EOFException) {
            return true;
        }
        if (x instanceof IOException && "Closed".equals(x.getMessage())) { // org.eclipse.jetty.server.HttpOutput.print
            return true;
        }
        if (x instanceof IOException
                && "write beyond end of stream".equals(x.getMessage())) { // java.util.zip.DeflaterOutputStream.write
            return true;
        }
        return isSocketException(x.getCause());
    }

    /**
     * Try to dispatch the request against the given node, and if it fails, report an error to the client.
     */
    @SuppressFBWarnings(value = "XSS_SERVLET", justification = "Handled by the escape() method or implementing code.")
    void invoke(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException {
        if (node == null) {
            // node is null
            if (!Dispatcher.isTraceEnabled(req)) {
                rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
            } else {
                // show error page
                rsp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                rsp.setContentType("text/html;charset=UTF-8");
                PrintWriter w = rsp.getWriter();
                w.println("<html><body>");
                w.println("<h1>404 Not Found</h1>");
                w.println(
                        "<p>Stapler processed this HTTP request as follows, but couldn't find the resource to consume the request");
                w.println("<pre>");
                EvaluationTrace.get(req).printHtml(w);
                w.println("<font color=red>-&gt; unexpected null!</font>");
                w.println("</pre>");
                w.println(
                        "<p>If this 404 is unexpected, double check the last part of the trace to see if it should have evaluated to null.");
                w.println("</body></html>");
            }
            return;
        }

        if (tryInvoke(req, rsp, node)) {
            return; // done
        }

        // we really run out of options.
        if (!Dispatcher.isTraceEnabled(req)) {
            rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            // show error page
            rsp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            rsp.setContentType("text/html;charset=UTF-8");
            PrintWriter w = rsp.getWriter();
            w.println("<html><body>");
            w.println("<h1>404 Not Found</h1>");
            w.println(
                    "<p>Stapler processed this HTTP request as follows, but couldn't find the resource to consume the request");
            w.println("<pre>");
            EvaluationTrace.get(req).printHtml(w);
            w.printf(
                    "<font color=red>-&gt; No matching rule was found on &lt;%s&gt; for \"%s\"</font>%n",
                    escape(node.toString()), req.tokens.assembleOriginalRestOfPath());
            w.println("</pre>");
            w.printf(
                    "<p>&lt;%s&gt; has the following URL mappings, in the order of preference:",
                    escape(node.toString()));
            w.println("<ol>");
            MetaClass metaClass = webApp.getMetaClass(node);
            for (Dispatcher d : metaClass.dispatchers) {
                w.println("<li>");
                w.println(d.toString());
            }
            w.println("</ol>");
            w.println("</body></html>");
        }
    }

    @SuppressFBWarnings(
            value = "REQUESTDISPATCHER_FILE_DISCLOSURE",
            justification = "Forwarding the request to be handled correctly.")
    public void forward(RequestDispatcher dispatcher, StaplerRequest req, HttpServletResponse rsp)
            throws ServletException, IOException {
        dispatcher.forward(req, new ResponseImpl(this, rsp));
    }

    /**
     * {@link ServletContext#getResource(String)} with caching.
     */
    /*package*/ URL getResource(String name) throws MalformedURLException {
        if (resourcePaths != null) {
            return resourcePaths.get(name);
        } else {
            return context.getResource(name);
        }
    }

    /**
     * Gets the URL (e.g., "/WEB-INF/side-files/fully/qualified/class/name/jspName")
     * from a class and the JSP name.
     */
    public static String getViewURL(Class clazz, String jspName) {
        return "/WEB-INF/side-files/" + clazz.getName().replace('.', '/') + '/' + jspName;
    }

    /**
     * Sets the specified object as the root of the web application.
     *
     * <p>
     * This method should be invoked from your implementation of
     * {@link ServletContextListener#contextInitialized(ServletContextEvent)}.
     *
     * <p>
     * This is just a convenience method to invoke
     * <code>servletContext.setAttribute("app",rootApp)</code>.
     *
     * <p>
     * The root object is bound to the URL '/' and used to resolve
     * all the requests to this web application.
     */
    public static void setRoot(ServletContextEvent event, Object rootApp) {
        event.getServletContext().setAttribute("app", rootApp);
    }

    /**
     * Sets the classloader used by {@link StaplerRequest#bindJSON(Class, JSONObject)} and its sibling methods.
     *
     * @deprecated
     *      Use {@link WebApp#setClassLoader(ClassLoader)}
     */
    @Deprecated
    public static void setClassLoader(ServletContext context, ClassLoader classLoader) {
        WebApp.get(context).setClassLoader(classLoader);
    }

    /**
     * @deprecated
     *      Use {@link WebApp#getClassLoader()}
     */
    @Deprecated
    public static ClassLoader getClassLoader(ServletContext context) {
        return WebApp.get(context).getClassLoader();
    }

    /**
     * @deprecated
     *      Use {@link WebApp#getClassLoader()}
     */
    @Deprecated
    public ClassLoader getClassLoader() {
        return webApp.getClassLoader();
    }

    /**
     * Gets the current {@link StaplerRequest} that the calling thread is associated with.
     */
    public static StaplerRequest getCurrentRequest() {
        return CURRENT_REQUEST.get();
    }

    /**
     * Gets the current {@link StaplerResponse} that the calling thread is associated with.
     */
    public static StaplerResponse getCurrentResponse() {
        return CURRENT_RESPONSE.get();
    }

    /**
     * Gets the current {@link Stapler} that the calling thread is associated with.
     */
    public static Stapler getCurrent() {
        return CURRENT_REQUEST.get().getStapler();
    }

    /**
     * HTTP date format. Notice that {@link SimpleDateFormat} is thread unsafe.
     */
    static final ThreadLocal<SimpleDateFormat> HTTP_DATE_FORMAT = ThreadLocal.withInitial(() -> {
        // RFC1945 section 3.3 Date/Time Formats states that timezones must be in GMT
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        return format;
    });

    /*package*/ static ThreadLocal<RequestImpl> CURRENT_REQUEST = new ThreadLocal<>();
    /*package*/ static ThreadLocal<ResponseImpl> CURRENT_RESPONSE = new ThreadLocal<>();

    private static final Logger LOGGER = Logger.getLogger(Stapler.class.getName());

    /**
     * Get raw servlet path (decoded in TokenList).
     */
    /*package*/ String getServletPath(HttpServletRequest req) {
        return canonicalPath(req.getRequestURI().substring(req.getContextPath().length()));
    }

    /**
     * Some web containers (e.g., Winstone) leaves ".." and "." in the request URL,
     * which is a security risk. Fix that by normalizing them.
     */
    static String canonicalPath(String path) {
        List<String> r = new ArrayList<>(Arrays.asList(path.split("/+")));
        for (int i = 0; i < r.size(); ) {
            if (r.get(i).isEmpty() || r.get(i).equals(".")) {
                // empty token occurs for example, "".split("/+") is [""]
                r.remove(i);
            } else if (r.get(i).equals("..")) {
                // i==0 means this is a broken URI.
                r.remove(i);
                if (i > 0) {
                    r.remove(i - 1);
                    i--;
                }
            } else {
                i++;
            }
        }

        StringBuilder buf = new StringBuilder();
        if (path.startsWith("/")) {
            buf.append('/');
        }
        boolean first = true;
        for (String token : r) {
            if (!first) {
                buf.append('/');
            } else {
                first = false;
            }
            buf.append(token);
        }
        // translation: if (path.endsWith("/") && !buf.endsWith("/"))
        if (path.endsWith("/") && (buf.isEmpty() || buf.charAt(buf.length() - 1) != '/')) {
            buf.append('/');
        }
        return buf.toString();
    }

    /**
     * This is the {@link Converter} registry that Stapler uses, primarily
     * for form-to-JSON binding in {@link StaplerRequest#bindJSON(Class, JSONObject)}
     * and its family of methods.
     */
    public static final ConvertUtilsBean CONVERT_UTILS = new ConvertUtilsBean();

    public static Converter lookupConverter(Class type) {
        Converter c = CONVERT_UTILS.lookup(type);
        if (c != null) {
            return c;
        }
        // fall back to compatibility behavior
        c = ConvertUtils.lookup(type);
        if (c != null) {
            return c;
        }

        // look for the associated converter
        try {
            if (type.getClassLoader() == null) {
                return null;
            }
            Class<?> cl = type.getClassLoader().loadClass(type.getName() + "$StaplerConverterImpl");
            c = (Converter) cl.getDeclaredConstructor().newInstance();
            CONVERT_UTILS.register(c, type);
            return c;
        } catch (ClassNotFoundException e) {
            // fall through
        } catch (IllegalAccessException e) {
            IllegalAccessError x = new IllegalAccessError();
            x.initCause(e);
            throw x;
        } catch (NoSuchMethodException e) {
            NoSuchMethodError x = new NoSuchMethodError();
            x.initCause(e);
            throw x;
        } catch (InstantiationException e) {
            InstantiationError x = new InstantiationError();
            x.initCause(e);
            throw x;
        } catch (InvocationTargetException e) {
            Throwable t = e.getCause();
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else if (t instanceof IOException) {
                throw new UncheckedIOException((IOException) t);
            } else if (t instanceof Exception) {
                throw new RuntimeException(t);
            } else if (t instanceof Error) {
                throw (Error) t;
            } else {
                throw new Error(e);
            }
        }

        // bean utils doesn't check the super type, so converters that apply to multiple types
        // need to be handled outside its semantics
        if (Enum.class.isAssignableFrom(type)) { // enum
            return ENUM_CONVERTER;
        }

        return null;
    }

    static {
        CONVERT_UTILS.register(
                new Converter() {
                    @Override
                    public Object convert(Class type, Object value) {
                        if (value == null) {
                            return null;
                        }
                        try {
                            return new URL(value.toString());
                        } catch (MalformedURLException e) {
                            throw new ConversionException(e);
                        }
                    }
                },
                URL.class);

        CONVERT_UTILS.register(
                new Converter() {
                    @Override
                    public FileItem convert(Class type, Object value) {
                        if (value == null) {
                            return null;
                        }
                        try {
                            return Stapler.getCurrentRequest().getFileItem2(value.toString());
                        } catch (ServletException | IOException e) {
                            throw new ConversionException(e);
                        }
                    }
                },
                FileItem.class);

        CONVERT_UTILS.register(
                new Converter() {
                    @Override
                    public org.apache.commons.fileupload.FileItem convert(Class type, Object value) {
                        if (value == null) {
                            return null;
                        }
                        try {
                            return org.apache.commons.fileupload.FileItem.fromFileUpload2FileItem(
                                    Stapler.getCurrentRequest().getFileItem2(value.toString()));
                        } catch (ServletException | IOException e) {
                            throw new ConversionException(e);
                        }
                    }
                },
                org.apache.commons.fileupload.FileItem.class);

        // mapping for boxed types should map null to null, instead of null to zero.
        CONVERT_UTILS.register(new IntegerConverter(null), Integer.class);
        CONVERT_UTILS.register(new FloatConverter(null), Float.class);
        CONVERT_UTILS.register(new DoubleConverter(null), Double.class);
    }

    private static final Converter ENUM_CONVERTER = new Converter() {
        @Override
        public Object convert(Class type, Object value) {
            if (value == null) {
                return null;
            }
            return Enum.valueOf(type, value.toString());
        }
    };

    /**
     * Escapes HTML/XML unsafe characters for the PCDATA section.
     * This method does not handle whitespace-preserving escape, nor attribute escapes.
     */
    public static String escape(String v) {
        if (v == null) {
            return null;
        }

        StringBuilder buf = new StringBuilder(v.length() + 64);
        for (int i = 0; i < v.length(); i++) {
            char ch = v.charAt(i);
            if (ch == '<') {
                buf.append("&lt;");
            } else if (ch == '>') {
                buf.append("&gt;");
            } else if (ch == '&') {
                buf.append("&amp;");
            } else {
                buf.append(ch);
            }
        }
        if (buf.length() == v.length()) {
            return v; // unmodified
        }
        return buf.toString();
    }

    public static Object[] htmlSafeArguments(Object[] args) {
        for (int i = 0; i < args.length; i++) {
            args[i] = htmlSafeArgument(args[i]);
        }
        return args;
    }

    /**
     * For XSS prevention, escape unsafe characters in String by default,
     * unless it's specifically wrapped in {@link RawHtmlArgument}.
     */
    public static Object htmlSafeArgument(Object o) {
        if (o instanceof RawHtmlArgument) {
            return ((RawHtmlArgument) o).getValue();
        }
        if (o instanceof Number || o instanceof Calendar || o instanceof Date) {
            // formatting numbers and date often requires that they be kept intact
            return o;
        }
        if (o == null) {
            return o;
        }

        return escape(o.toString());
    }
}
