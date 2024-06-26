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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.security.Principal;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletMapping;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;
import javax.servlet.http.PushBuilder;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.fileupload2.core.FileItem;
import org.kohsuke.stapler.bind.BoundObjectTable;
import org.kohsuke.stapler.json.SubmittedForm;
import org.kohsuke.stapler.lang.Klass;

/**
 * Defines additional parameters/operations made available by Stapler.
 *
 * @see Stapler#getCurrentRequest()
 * @author Kohsuke Kawaguchi
 * @deprecated use {@link StaplerRequest2}
 */
@Deprecated
public interface StaplerRequest extends HttpServletRequest {
    /**
     * Gets the {@link Stapler} instance that this belongs to.
     */
    Stapler getStapler();

    /**
     * Short for {@code getStapler().getWebApp()}
     */
    WebApp getWebApp();

    /**
     * Returns the additional URL portion that wasn't used by the stapler,
     * excluding the query string.
     *
     * <p>
     * For example, if the requested URL is "foo/bar/zot/abc?def=ghi" and
     * "foo/bar" portion matched {@code bar.jsp}, this method returns
     * "/zot/abc".
     *
     * <p>
     * If this method is invoked from getters or {@link StaplerProxy#getTarget()}
     * during the object traversal, this method returns the path portion
     * that is not yet processed.
     *
     * @return
     *      can be empty string, but never null.
     */
    String getRestOfPath();

    /**
     * Returns the same thing as {@link #getRestOfPath()} but in the pre-decoded form,
     * so all "%HH"s as present in the request URL is intact.
     */
    String getOriginalRestOfPath();

    /**
     * Returns the {@link ServletContext} object given to the stapler
     * dispatcher servlet.
     */
    @Override
    ServletContext getServletContext();

    /**
     * {@link #getRequestURI()} plus additional query string part, if it exists.
     */
    String getRequestURIWithQueryString();

    /**
     * {@link #getRequestURL()} plus additional query string part, if it exists.
     */
    StringBuffer getRequestURLWithQueryString();

    /**
     * Gets the {@link RequestDispatcher} that represents a specific view
     * for the given object.
     *
     * This support both JSP and Jelly.
     *
     * @param viewName
     *      If this name is relative name like "foo.jsp" or "bar/zot.jelly",
     *      then the corresponding "side file" is searched by this name.
     *      <p>
     *      For Jelly, this also accepts absolute path name that starts
     *      with '/', such as "/foo/bar/zot.jelly". In this case,
     *      {@code it.getClass().getClassLoader()} is searched for this script.
     *
     * @return null
     *      if neither JSP nor Jelly is not found by the given name.
     */
    RequestDispatcher getView(Object it, String viewName) throws IOException;

    /**
     * Convenience method to call {@link #getView(Klass, String)} with {@link Class}.
     */
    RequestDispatcher getView(Class clazz, String viewName) throws IOException;

    /**
     * Gets the {@link RequestDispatcher} that represents a specific view
     * for the given class.
     *
     * <p>
     * Unlike {@link #getView(Object, String)}, calling this request dispatcher
     * doesn't set the "it" variable, so
     * {@code getView(it.getClass(),viewName)} and {@code getView(it,viewName)}
     * aren't the same thing.
     */
    RequestDispatcher getView(Klass<?> clazz, String viewName) throws IOException;

    /**
     * Gets the part of the request URL from protocol up to the context path.
     * So typically it's something like {@code http://foobar:8080/something}
     */
    String getRootPath();

    /**
     * Gets the referer header (like "http://foobar.com/zot") or null.
     *
     * This is just a convenience method.
     */
    String getReferer();

    /**
     * Returns a list of ancestor objects that lead to the "it" object.
     * The returned list contains {@link Ancestor} objects sorted in the
     * order from root to the "it" object.
     *
     * <p>
     * For example, if the URL was "foo/bar/zot" and the "it" object
     * was determined as <code>root.getFoo().getBar("zot")</code>,
     * then this list will contain the following 3 objects in this order:
     * <ol>
     *  <li>the root object
     *  <li>root.getFoo() object
     *  <li>root.getFoo().getBar("zot") object (the "it" object)
     * </ol>
     * <p>
     *
     *
     * @return
     *      list of {@link Ancestor}s. Can be empty, but always non-null.
     */
    List<Ancestor> getAncestors();

    /**
     * Finds the nearest ancestor that has the object of the given type, or null if not found.
     */
    Ancestor findAncestor(Class type);

    /**
     * Short for {@code findAncestor(type).getObject()}, with proper handling for null de-reference.
     * This version is also type safe.
     */
    <T> T findAncestorObject(Class<T> type);

    /**
     * Finds the nearest ancestor whose {@link Ancestor#getObject()} matches the given object.
     */
    Ancestor findAncestor(Object o);

    /**
     * Short for {@code getParameter(name)!=null}
     */
    boolean hasParameter(String name);

    /**
     * Gets the {@link HttpServletRequest#getRequestURI() request URI}
     * of the original request, so that you can access the value even from
     * JSP.
     */
    String getOriginalRequestURI();

    /**
     * Checks "If-Modified-Since" header and returns false
     * if the resource needs to be served.
     *
     * <p>
     * This method can behave in three ways.
     *
     * <ol>
     *  <li>If {@code timestampOfResource} is 0 or negative,
     *      this method just returns false.
     *
     *  <li>If "If-Modified-Since" header is sent and if it's bigger than
     *      {@code timestampOfResource}, then this method sets
     *      {@link HttpServletResponse#SC_NOT_MODIFIED} as the response code
     *      and returns true.
     *
     *  <li>Otherwise, "Last-Modified" header is added with {@code timestampOfResource} value,
     *      and this method returns false.
     * </ol>
     *
     * <p>
     * This method sends out the "Expires" header to force browser
     * to re-validate all the time.
     *
     * @param timestampOfResource
     *      The time stamp of the resource.
     * @param rsp
     *      This object is updated accordingly to simplify processing.
     *
     * @return
     *      false to indicate that the caller has to serve the actual resource.
     *      true to indicate that the caller should just quit processing right there
     *      (and send back {@link HttpServletResponse#SC_NOT_MODIFIED}.
     */
    boolean checkIfModified(long timestampOfResource, StaplerResponse rsp);

    /**
     * @see #checkIfModified(long, StaplerResponse)
     */
    boolean checkIfModified(Date timestampOfResource, StaplerResponse rsp);

    /**
     * @see #checkIfModified(long, StaplerResponse)
     */
    boolean checkIfModified(Calendar timestampOfResource, StaplerResponse rsp);

    /**
     * @param expiration
     *      The number of milliseconds until the resource will "expire".
     *      Until it expires the browser will be allowed to cache it
     *      and serve it without checking back with the server.
     *      After it expires, the client will send conditional GET to
     *      check if the resource is actually modified or not.
     *      If 0, it will immediately expire.
     *
     * @see #checkIfModified(long, StaplerResponse)
     */
    boolean checkIfModified(long timestampOfResource, StaplerResponse rsp, long expiration);

    /**
     * Binds form parameters to a bean by using introspection.
     *
     * For example, if there's a parameter called 'foo' that has value 'abc',
     * then {@code bean.setFoo('abc')} will be invoked. This will be repeated
     * for all parameters. Parameters that do not have corresponding setters will
     * be simply ignored.
     *
     * <p>
     * Values are converted into the right type. See {@link ConvertUtils#convert(String, Class)}.
     *
     * @see BeanUtils#setProperty(Object, String, Object)
     *
     * @param bean
     *      The object which will be filled out.
     */
    void bindParameters(Object bean);

    /**
     * Binds form parameters to a bean by using introspection.
     *
     * This method works like {@link #bindParameters(Object)}, but it performs a
     * pre-processing on property names. Namely, only property names that start
     * with the given prefix will be used for binding, and only the portion of the
     * property name after the prefix is used.
     *
     * So for example, if the prefix is "foo.", then property name "foo.bar" with value
     * "zot" will invoke {@code bean.setBar("zot")}.
     *
     *
     * @deprecated
     *      Instead of using prefix to group object among form parameter names,
     *      use structured form submission and {@link #bindJSON(Class, JSONObject)}.
     */
    @Deprecated
    void bindParameters(Object bean, String prefix);

    /**
     * Binds collection form parameters to beans by using introspection or
     * constructor parameters injection.
     *
     * <p>
     * This method works like {@link #bindParameters(Object,String)} and
     * {@link #bindParameters(Class, String)}, but it assumes
     * that form parameters have multiple-values, and use individual values to
     * fill in multiple beans.
     *
     * <p>
     * For example, if {@code getParameterValues("foo")=={"abc","def"}}
     * and {@code getParameterValues("bar")=={"5","3"}}, then this method will
     * return two objects (the first with "abc" and "5", the second with
     * "def" and "3".)
     *
     * @param type
     *      Type of the bean to be created. This class must have the default no-arg
     *      constructor.
     *
     * @param prefix
     *      See {@link #bindParameters(Object, String)} for details.
     *
     * @return
     *      Can be empty but never null.
     *
     *
     * @deprecated
     *      Instead of using prefix to group object among form parameter names,
     *      use structured form submission and {@link #bindJSON(Class, JSONObject)}.
     */
    @Deprecated
    <T> List<T> bindParametersToList(Class<T> type, String prefix);

    /**
     * Instantiates a new object by injecting constructor parameters from the form parameters.
     *
     * <p>
     * The given class must have a constructor annotated with '&#64;stapler-constructor',
     * and must be processed by the maven-stapler-plugin, so that the parameter names
     * of the constructor is available at runtime.
     *
     * <p>
     * The prefix is used to control the form parameter name. For example,
     * if the prefix is "foo." and if the constructor is define as
     * <code>Foo(String a, String b)</code>, then the constructor will be invoked
     * as <code>new Foo(getParameter("foo.a"),getParameter("foo.b"))</code>.
     *
     * @deprecated
     *      Instead of using prefix to group object among form parameter names,
     *      use structured form submission and {@link #bindJSON(Class, JSONObject)}.
     */
    @Deprecated
    <T> T bindParameters(Class<T> type, String prefix);

    /**
     * Works like {@link #bindParameters(Class, String)} but uses n-th value
     * of all the parameters.
     *
     * <p>
     * This is useful for creating multiple instances from repeated form fields.
     *
     *
     * @deprecated
     *      Instead of using prefix to group object among form parameter names,
     *      use structured form submission and {@link #bindJSON(Class, JSONObject)}.
     */
    @Deprecated
    <T> T bindParameters(Class<T> type, String prefix, int index);

    /**
     * Data-bind from a {@link JSONObject} to the given target type,
     * by using introspection or constructor parameters injection.
     *
     * <p>
     * For example, if you have a constructor that looks like the following:
     *
     * <pre>
     * class Foo {
     *   &#64;{@link DataBoundConstructor}
     *   public Foo(Integer x, String y, boolean z, Bar bar) { ... }
     * }
     *
     * class Bar {
     *   &#64;{@link DataBoundConstructor}
     *   public Bar(int x, int y) {}
     * }
     * </pre>
     *
     * ... and if JSONObject looks like
     *
     * <pre>{ y:"text", z:true, bar:{x:1,y:2}}</pre>
     *
     * then, this method returns
     *
     * <pre>new Foo(null,"text",true,new Bar(1,2))</pre>
     *
     * <p><strong>Sub-typing:</strong> In the above example,
     * a new instance of {@code Bar} was created,
     * but you can also create a subtype of Bar by having the '$class' property in
     * JSON like this:
     *
     * <pre>
     * class BarEx extends Bar {
     *   &#64;{@link DataBoundConstructor}
     *   public BarEx(int a, int b, int c) {}
     * }
     *
     * { y:"text", z:true, bar: { $class:"p.k.g.BarEx", a:1, b:2, c:3 } }
     * </pre>
     *
     * <p>
     * The type that shows up in the constructor ({@code Bar} in this case)
     * can be an interface or an abstract class.
     */
    <T> T bindJSON(Class<T> type, JSONObject src);

    /**
     * Data-bind from one of the JSON object types ({@link JSONObject}, {@link JSONArray},
     * {@link String}, {@link Integer}, and so on) to the expected type given as an argument.
     *
     * @param genericType
     *      The generic type of the 'erasure' parameter.
     * @param erasure
     *      The expected type to convert the JSON argument to.
     * @param json
     *      One of the JSON value type.
     */
    <T> T bindJSON(Type genericType, Class<T> erasure, Object json);

    /**
     * Data-binds from {@link JSONObject} to the given object.
     *
     * <p>
     * This method is bit like {@link #bindJSON(Class, JSONObject)}, except that this method
     * populates an existing object, instead of creating a new instance.
     *
     * <p>
     * This method is also bit like {@link #bindParameters(Object, String)}, in that it
     * populates an existing object from a form submission, except that this method
     * obtains data from {@link JSONObject} thus more structured, whereas {@link #bindParameters(Object, String)}
     * uses the map structure of the form submission.
     */
    void bindJSON(Object bean, JSONObject src);

    /**
     * Data-bind from either {@link JSONObject} or {@link JSONArray} to a list,
     * by using {@link #bindJSON(Class, JSONObject)} as the lower-level mechanism.
     *
     * <p>
     * If the source is {@link JSONObject}, the returned list will contain
     * a single item. If it is {@link JSONArray}, each item will be bound.
     * If it is null, then the list will be empty.
     */
    <T> List<T> bindJSONToList(Class<T> type, Object src);

    /**
     * Gets the {@link BindInterceptor} set for this request.
     *
     * @see WebApp#bindInterceptors
     */
    BindInterceptor getBindInterceptor();

    /**
     * @deprecated
     * Typo. Use {@link #setBindInterceptor(BindInterceptor)}
     */
    @Deprecated
    BindInterceptor setBindListener(BindInterceptor bindListener);

    /**
     * @deprecated
     * Typo. Use {@link #setBindInterceptor(BindInterceptor)}
     */
    @Deprecated
    BindInterceptor setBindInterceptpr(BindInterceptor bindListener);

    BindInterceptor setBindInterceptor(BindInterceptor bindListener);

    /**
     * Gets the content of the structured form submission.
     *
     * @see <a href="https://wiki.jenkins-ci.org/display/JENKINS/Structured+Form+Submission">Structured Form Submission</a>
     * @see SubmittedForm
     */
    JSONObject getSubmittedForm() throws ServletException;

    /**
     * Obtains a commons-fileupload2 object that represents an uploaded file.
     *
     * @return
     *      null if a file of the given form field name doesn't exist.
     *      This includes the case where the name corresponds to a simple
     *      form field (like textbox, checkbox, etc.)
     */
    FileItem getFileItem2(String name) throws ServletException, IOException;

    /**
     * Obtains a commons-fileupload object that represents an uploaded file.
     *
     * @return
     *      null if a file of the given form field name doesn't exist.
     *      This includes the case where the name corresponds to a simple
     *      form field (like textbox, checkbox, etc.)
     * @deprecated use {@link #getFileItem2(String)}
     */
    @Deprecated
    org.apache.commons.fileupload.FileItem getFileItem(String name) throws ServletException, IOException;

    /**
     * Returns true if this request represents a server method call to a JavaScript proxy object.
     */
    boolean isJavaScriptProxyCall();

    /**
     * Short cut for obtaining {@link BoundObjectTable} associated with this webapp.
     */
    BoundObjectTable getBoundObjectTable();

    /**
     * Exports the given Java object as a JavaScript proxy and returns a JavaScript expression to create
     * a proxy on the client side.
     *
     * Short cut for {@code getBoundObjectTable().bind(toBeExported).getProxyScript()}
     *
     * @deprecated Use {@link #createJavaScriptProxyParameters(Object)} and invoke {@code makeStaplerProxy} yourself.
     */
    @Deprecated
    String createJavaScriptProxy(Object toBeExported);

    /**
     * Return value of {@link #createJavaScriptProxyParameters(Object)}
     */
    final class RenderOnDemandParameters {
        public final String proxyMethod;
        public final String url;
        public final String crumb;
        public final Set<String> urlNames;

        public RenderOnDemandParameters(String proxyMethod, String url, String crumb, Set<String> urlNames) {
            this.proxyMethod = proxyMethod;
            this.url = url;
            this.crumb = crumb;
            this.urlNames = urlNames;
        }

        public String getUrlNames() {
            return String.join(",", urlNames);
        }
    }

    /**
     * Exports the given Java object as a JavaScript proxy and returns the parameters needed to call
     * {@code makeStaplerProxy}.
     */
    RenderOnDemandParameters createJavaScriptProxyParameters(Object toBeExported);

    default StaplerRequest2 toStaplerRequest2() {
        return new StaplerRequest2() {
            @Override
            public Object getAttribute(String name) {
                return StaplerRequest.this.getAttribute(name);
            }

            @Override
            public Enumeration<String> getAttributeNames() {
                return StaplerRequest.this.getAttributeNames();
            }

            @Override
            public String getCharacterEncoding() {
                return StaplerRequest.this.getCharacterEncoding();
            }

            @Override
            public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
                StaplerRequest.this.setCharacterEncoding(env);
            }

            @Override
            public int getContentLength() {
                return StaplerRequest.this.getContentLength();
            }

            @Override
            public long getContentLengthLong() {
                return StaplerRequest.this.getContentLengthLong();
            }

            @Override
            public String getContentType() {
                return StaplerRequest.this.getContentType();
            }

            @Override
            public jakarta.servlet.ServletInputStream getInputStream() throws IOException {
                return StaplerRequest.this.getInputStream().toJakartaServletInputStream();
            }

            @Override
            public String getParameter(String name) {
                return StaplerRequest.this.getParameter(name);
            }

            @Override
            public Enumeration<String> getParameterNames() {
                return StaplerRequest.this.getParameterNames();
            }

            @Override
            public String[] getParameterValues(String name) {
                return StaplerRequest.this.getParameterValues(name);
            }

            @Override
            public Map<String, String[]> getParameterMap() {
                return StaplerRequest.this.getParameterMap();
            }

            @Override
            public String getProtocol() {
                return StaplerRequest.this.getProtocol();
            }

            @Override
            public String getScheme() {
                return StaplerRequest.this.getScheme();
            }

            @Override
            public String getServerName() {
                return StaplerRequest.this.getServerName();
            }

            @Override
            public int getServerPort() {
                return StaplerRequest.this.getServerPort();
            }

            @Override
            public BufferedReader getReader() throws IOException {
                return StaplerRequest.this.getReader();
            }

            @Override
            public String getRemoteAddr() {
                return StaplerRequest.this.getRemoteAddr();
            }

            @Override
            public String getRemoteHost() {
                return StaplerRequest.this.getRemoteHost();
            }

            @Override
            public void setAttribute(String name, Object o) {
                StaplerRequest.this.setAttribute(name, o);
            }

            @Override
            public void removeAttribute(String name) {
                StaplerRequest.this.removeAttribute(name);
            }

            @Override
            public Locale getLocale() {
                return StaplerRequest.this.getLocale();
            }

            @Override
            public Enumeration<Locale> getLocales() {
                return StaplerRequest.this.getLocales();
            }

            @Override
            public boolean isSecure() {
                return StaplerRequest.this.isSecure();
            }

            @Override
            public jakarta.servlet.RequestDispatcher getRequestDispatcher(String path) {
                return StaplerRequest.this.getRequestDispatcher(path).toJakartaRequestDispatcher();
            }

            @Override
            public String getRealPath(String path) {
                return StaplerRequest.this.getRealPath(path);
            }

            @Override
            public int getRemotePort() {
                return StaplerRequest.this.getRemotePort();
            }

            @Override
            public String getLocalName() {
                return StaplerRequest.this.getLocalName();
            }

            @Override
            public String getLocalAddr() {
                return StaplerRequest.this.getLocalAddr();
            }

            @Override
            public int getLocalPort() {
                return StaplerRequest.this.getLocalPort();
            }

            @Override
            public Stapler getStapler() {
                return StaplerRequest.this.getStapler();
            }

            @Override
            public WebApp getWebApp() {
                return StaplerRequest.this.getWebApp();
            }

            @Override
            public String getRestOfPath() {
                return StaplerRequest.this.getRestOfPath();
            }

            @Override
            public String getOriginalRestOfPath() {
                return StaplerRequest.this.getOriginalRestOfPath();
            }

            @Override
            public jakarta.servlet.ServletContext getServletContext() {
                return StaplerRequest.this.getServletContext().toJakartaServletContext();
            }

            @Override
            public String getRequestURIWithQueryString() {
                return StaplerRequest.this.getRequestURIWithQueryString();
            }

            @Override
            public StringBuffer getRequestURLWithQueryString() {
                return StaplerRequest.this.getRequestURLWithQueryString();
            }

            @Override
            public jakarta.servlet.RequestDispatcher getView(Object it, String viewName) throws IOException {
                return StaplerRequest.this.getView(it, viewName).toJakartaRequestDispatcher();
            }

            @Override
            public jakarta.servlet.RequestDispatcher getView(Class clazz, String viewName) throws IOException {
                return StaplerRequest.this.getView(clazz, viewName).toJakartaRequestDispatcher();
            }

            @Override
            public jakarta.servlet.RequestDispatcher getView(Klass<?> clazz, String viewName) throws IOException {
                return StaplerRequest.this.getView(clazz, viewName).toJakartaRequestDispatcher();
            }

            @Override
            public String getRootPath() {
                return StaplerRequest.this.getRootPath();
            }

            @Override
            public String getReferer() {
                return StaplerRequest.this.getReferer();
            }

            @Override
            public List<Ancestor> getAncestors() {
                return StaplerRequest.this.getAncestors();
            }

            @Override
            public Ancestor findAncestor(Class type) {
                return StaplerRequest.this.findAncestor(type);
            }

            @Override
            public <T> T findAncestorObject(Class<T> type) {
                return StaplerRequest.this.findAncestorObject(type);
            }

            @Override
            public Ancestor findAncestor(Object o) {
                return StaplerRequest.this.findAncestor(o);
            }

            @Override
            public boolean hasParameter(String name) {
                return StaplerRequest.this.hasParameter(name);
            }

            @Override
            public String getOriginalRequestURI() {
                return StaplerRequest.this.getOriginalRequestURI();
            }

            @Override
            public boolean checkIfModified(long timestampOfResource, StaplerResponse2 rsp) {
                return StaplerRequest.this.checkIfModified(
                        timestampOfResource, StaplerResponse.fromStaplerResponse2(rsp));
            }

            @Override
            public boolean checkIfModified(Date timestampOfResource, StaplerResponse2 rsp) {
                return StaplerRequest.this.checkIfModified(
                        timestampOfResource, StaplerResponse.fromStaplerResponse2(rsp));
            }

            @Override
            public boolean checkIfModified(Calendar timestampOfResource, StaplerResponse2 rsp) {
                return StaplerRequest.this.checkIfModified(
                        timestampOfResource, StaplerResponse.fromStaplerResponse2(rsp));
            }

            @Override
            public boolean checkIfModified(long timestampOfResource, StaplerResponse2 rsp, long expiration) {
                return StaplerRequest.this.checkIfModified(
                        timestampOfResource, StaplerResponse.fromStaplerResponse2(rsp));
            }

            @Override
            public void bindParameters(Object bean) {
                StaplerRequest.this.bindParameters(bean);
            }

            @Override
            public void bindParameters(Object bean, String prefix) {
                StaplerRequest.this.bindParameters(bean, prefix);
            }

            @Override
            public <T> List<T> bindParametersToList(Class<T> type, String prefix) {
                return StaplerRequest.this.bindParametersToList(type, prefix);
            }

            @Override
            public <T> T bindParameters(Class<T> type, String prefix) {
                return StaplerRequest.this.bindParameters(type, prefix);
            }

            @Override
            public <T> T bindParameters(Class<T> type, String prefix, int index) {
                return StaplerRequest.this.bindParameters(type, prefix, index);
            }

            @Override
            public <T> T bindJSON(Class<T> type, JSONObject src) {
                return StaplerRequest.this.bindJSON(type, src);
            }

            @Override
            public <T> T bindJSON(Type genericType, Class<T> erasure, Object json) {
                return StaplerRequest.this.bindJSON(genericType, erasure, json);
            }

            @Override
            public void bindJSON(Object bean, JSONObject src) {
                StaplerRequest.this.bindJSON(bean, src);
            }

            @Override
            public <T> List<T> bindJSONToList(Class<T> type, Object src) {
                return StaplerRequest.this.bindJSONToList(type, src);
            }

            @Override
            public BindInterceptor getBindInterceptor() {
                return StaplerRequest.this.getBindInterceptor();
            }

            @Override
            public BindInterceptor setBindListener(BindInterceptor bindListener) {
                return StaplerRequest.this.setBindListener(bindListener);
            }

            @Override
            public BindInterceptor setBindInterceptpr(BindInterceptor bindListener) {
                return StaplerRequest.this.setBindInterceptpr(bindListener);
            }

            @Override
            public BindInterceptor setBindInterceptor(BindInterceptor bindListener) {
                return StaplerRequest.this.setBindInterceptor(bindListener);
            }

            @Override
            public JSONObject getSubmittedForm() throws jakarta.servlet.ServletException {
                try {
                    return StaplerRequest.this.getSubmittedForm();
                } catch (ServletException e) {
                    throw e.toJakartaServletException();
                }
            }

            @Override
            public FileItem getFileItem2(String name) throws jakarta.servlet.ServletException, IOException {
                try {
                    return StaplerRequest.this.getFileItem2(name);
                } catch (ServletException e) {
                    throw e.toJakartaServletException();
                }
            }

            @Override
            public org.apache.commons.fileupload.FileItem getFileItem(String name)
                    throws jakarta.servlet.ServletException, IOException {
                try {
                    return StaplerRequest.this.getFileItem(name);
                } catch (ServletException e) {
                    throw e.toJakartaServletException();
                }
            }

            @Override
            public boolean isJavaScriptProxyCall() {
                return StaplerRequest.this.isJavaScriptProxyCall();
            }

            @Override
            public BoundObjectTable getBoundObjectTable() {
                return StaplerRequest.this.getBoundObjectTable();
            }

            @Override
            public String createJavaScriptProxy(Object toBeExported) {
                return StaplerRequest.this.createJavaScriptProxy(toBeExported);
            }

            @Override
            public RenderOnDemandParameters createJavaScriptProxyParameters(Object toBeExported) {
                StaplerRequest.RenderOnDemandParameters result =
                        StaplerRequest.this.createJavaScriptProxyParameters(toBeExported);
                return new RenderOnDemandParameters(result.proxyMethod, result.url, result.crumb, result.urlNames);
            }

            @Override
            public jakarta.servlet.AsyncContext startAsync() {
                return StaplerRequest.this.startAsync().toJakartaAsyncContext();
            }

            @Override
            public jakarta.servlet.AsyncContext startAsync(
                    jakarta.servlet.ServletRequest servletRequest, jakarta.servlet.ServletResponse servletResponse) {
                return StaplerRequest.this
                        .startAsync(
                                ServletRequest.fromJakartaServletRequest(servletRequest),
                                ServletResponse.fromJakartaServletResponse(servletResponse))
                        .toJakartaAsyncContext();
            }

            @Override
            public boolean isAsyncStarted() {
                return StaplerRequest.this.isAsyncStarted();
            }

            @Override
            public boolean isAsyncSupported() {
                return StaplerRequest.this.isAsyncSupported();
            }

            @Override
            public jakarta.servlet.AsyncContext getAsyncContext() {
                return StaplerRequest.this.getAsyncContext().toJakartaAsyncContext();
            }

            @Override
            public jakarta.servlet.DispatcherType getDispatcherType() {
                return DispatcherType.toJakartaDispatcherType(StaplerRequest.this.getDispatcherType());
            }

            @Override
            public String getAuthType() {
                return StaplerRequest.this.getAuthType();
            }

            @Override
            public jakarta.servlet.http.Cookie[] getCookies() {
                return Stream.of(StaplerRequest.this.getCookies())
                        .map(Cookie::toJakartaServletHttpCookie)
                        .toArray(jakarta.servlet.http.Cookie[]::new);
            }

            @Override
            public long getDateHeader(String name) {
                return StaplerRequest.this.getDateHeader(name);
            }

            @Override
            public String getHeader(String name) {
                return StaplerRequest.this.getHeader(name);
            }

            @Override
            public Enumeration<String> getHeaders(String name) {
                return StaplerRequest.this.getHeaders(name);
            }

            @Override
            public Enumeration<String> getHeaderNames() {
                return StaplerRequest.this.getHeaderNames();
            }

            @Override
            public int getIntHeader(String name) {
                return StaplerRequest.this.getIntHeader(name);
            }

            @Override
            public jakarta.servlet.http.HttpServletMapping getHttpServletMapping() {
                return StaplerRequest.this.getHttpServletMapping().toJakartaHttpServletMapping();
            }

            @Override
            public String getMethod() {
                return StaplerRequest.this.getMethod();
            }

            @Override
            public String getPathInfo() {
                return StaplerRequest.this.getPathInfo();
            }

            @Override
            public String getPathTranslated() {
                return StaplerRequest.this.getPathTranslated();
            }

            @Override
            public jakarta.servlet.http.PushBuilder newPushBuilder() {
                // TODO implement this
                throw new UnsupportedOperationException();
            }

            @Override
            public String getContextPath() {
                return StaplerRequest.this.getContextPath();
            }

            @Override
            public String getQueryString() {
                return StaplerRequest.this.getQueryString();
            }

            @Override
            public String getRemoteUser() {
                return StaplerRequest.this.getRemoteUser();
            }

            @Override
            public boolean isUserInRole(String role) {
                return StaplerRequest.this.isUserInRole(role);
            }

            @Override
            public Principal getUserPrincipal() {
                return StaplerRequest.this.getUserPrincipal();
            }

            @Override
            public String getRequestedSessionId() {
                return StaplerRequest.this.getRequestedSessionId();
            }

            @Override
            public String getRequestURI() {
                return StaplerRequest.this.getRequestURI();
            }

            @Override
            public StringBuffer getRequestURL() {
                return StaplerRequest.this.getRequestURL();
            }

            @Override
            public String getServletPath() {
                return StaplerRequest.this.getServletPath();
            }

            @Override
            public jakarta.servlet.http.HttpSession getSession(boolean create) {
                HttpSession session = StaplerRequest.this.getSession(create);
                return session != null ? session.toJakartaHttpSession() : null;
            }

            @Override
            public jakarta.servlet.http.HttpSession getSession() {
                HttpSession session = StaplerRequest.this.getSession();
                return session != null ? session.toJakartaHttpSession() : null;
            }

            @Override
            public String changeSessionId() {
                return StaplerRequest.this.changeSessionId();
            }

            @Override
            public boolean isRequestedSessionIdValid() {
                return StaplerRequest.this.isRequestedSessionIdValid();
            }

            @Override
            public boolean isRequestedSessionIdFromCookie() {
                return StaplerRequest.this.isRequestedSessionIdFromCookie();
            }

            @Override
            public boolean isRequestedSessionIdFromURL() {
                return StaplerRequest.this.isRequestedSessionIdFromURL();
            }

            @Override
            public boolean isRequestedSessionIdFromUrl() {
                return StaplerRequest.this.isRequestedSessionIdFromUrl();
            }

            @Override
            public boolean authenticate(jakarta.servlet.http.HttpServletResponse response)
                    throws IOException, jakarta.servlet.ServletException {
                try {
                    return StaplerRequest.this.authenticate(
                            HttpServletResponse.fromJakartaHttpServletResponse(response));
                } catch (ServletException e) {
                    throw e.toJakartaServletException();
                }
            }

            @Override
            public void login(String username, String password) throws jakarta.servlet.ServletException {
                try {
                    StaplerRequest.this.login(username, password);
                } catch (ServletException e) {
                    throw e.toJakartaServletException();
                }
            }

            @Override
            public void logout() throws jakarta.servlet.ServletException {
                try {
                    StaplerRequest.this.logout();
                } catch (ServletException e) {
                    throw e.toJakartaServletException();
                }
            }

            @Override
            public Collection<jakarta.servlet.http.Part> getParts()
                    throws IOException, jakarta.servlet.ServletException {
                try {
                    return StaplerRequest.this.getParts().stream()
                            .map(Part::toJakartaPart)
                            .collect(Collectors.toList());
                } catch (ServletException e) {
                    throw e.toJakartaServletException();
                }
            }

            @Override
            public jakarta.servlet.http.Part getPart(String name) throws IOException, jakarta.servlet.ServletException {
                try {
                    return StaplerRequest.this.getPart(name).toJakartaPart();
                } catch (ServletException e) {
                    throw e.toJakartaServletException();
                }
            }

            @Override
            public <T extends jakarta.servlet.http.HttpUpgradeHandler> T upgrade(Class<T> handlerClass) {
                // TODO implement this
                throw new UnsupportedOperationException();
            }

            @Override
            public Map<String, String> getTrailerFields() {
                return StaplerRequest.this.getTrailerFields();
            }

            @Override
            public boolean isTrailerFieldsReady() {
                return StaplerRequest.this.isTrailerFieldsReady();
            }
        };
    }

    static StaplerRequest fromStaplerRequest2(StaplerRequest2 from) {
        Objects.requireNonNull(from);
        return new StaplerRequest() {
            @Override
            public Object getAttribute(String name) {
                return from.getAttribute(name);
            }

            @Override
            public Enumeration<String> getAttributeNames() {
                return from.getAttributeNames();
            }

            @Override
            public String getCharacterEncoding() {
                return from.getCharacterEncoding();
            }

            @Override
            public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
                from.setCharacterEncoding(env);
            }

            @Override
            public int getContentLength() {
                return from.getContentLength();
            }

            @Override
            public long getContentLengthLong() {
                return from.getContentLengthLong();
            }

            @Override
            public String getContentType() {
                return from.getContentType();
            }

            @Override
            public ServletInputStream getInputStream() throws IOException {
                return ServletInputStream.fromJakartaServletInputStream(from.getInputStream());
            }

            @Override
            public String getParameter(String name) {
                return from.getParameter(name);
            }

            @Override
            public Enumeration<String> getParameterNames() {
                return from.getParameterNames();
            }

            @Override
            public String[] getParameterValues(String name) {
                return from.getParameterValues(name);
            }

            @Override
            public Map<String, String[]> getParameterMap() {
                return from.getParameterMap();
            }

            @Override
            public String getProtocol() {
                return from.getProtocol();
            }

            @Override
            public String getScheme() {
                return from.getScheme();
            }

            @Override
            public String getServerName() {
                return from.getServerName();
            }

            @Override
            public int getServerPort() {
                return from.getServerPort();
            }

            @Override
            public BufferedReader getReader() throws IOException {
                return from.getReader();
            }

            @Override
            public String getRemoteAddr() {
                return from.getRemoteAddr();
            }

            @Override
            public String getRemoteHost() {
                return from.getRemoteHost();
            }

            @Override
            public void setAttribute(String name, Object o) {
                from.setAttribute(name, o);
            }

            @Override
            public void removeAttribute(String name) {
                from.removeAttribute(name);
            }

            @Override
            public Locale getLocale() {
                return from.getLocale();
            }

            @Override
            public Enumeration<Locale> getLocales() {
                return from.getLocales();
            }

            @Override
            public boolean isSecure() {
                return from.isSecure();
            }

            @Override
            public RequestDispatcher getRequestDispatcher(String path) {
                return RequestDispatcher.fromJakartaRequestDispatcher(from.getRequestDispatcher(path));
            }

            @Override
            public String getRealPath(String path) {
                return from.getRealPath(path);
            }

            @Override
            public int getRemotePort() {
                return from.getRemotePort();
            }

            @Override
            public String getLocalName() {
                return from.getLocalName();
            }

            @Override
            public String getLocalAddr() {
                return from.getLocalAddr();
            }

            @Override
            public int getLocalPort() {
                return from.getLocalPort();
            }

            @Override
            public Stapler getStapler() {
                return from.getStapler();
            }

            @Override
            public WebApp getWebApp() {
                return from.getWebApp();
            }

            @Override
            public String getRestOfPath() {
                return from.getRestOfPath();
            }

            @Override
            public String getOriginalRestOfPath() {
                return from.getOriginalRestOfPath();
            }

            @Override
            public ServletContext getServletContext() {
                return ServletContext.fromJakartServletContext(from.getServletContext());
            }

            @Override
            public String getRequestURIWithQueryString() {
                return from.getRequestURIWithQueryString();
            }

            @Override
            public StringBuffer getRequestURLWithQueryString() {
                return from.getRequestURLWithQueryString();
            }

            @Override
            public RequestDispatcher getView(Object it, String viewName) throws IOException {
                return RequestDispatcher.fromJakartaRequestDispatcher(from.getView(it, viewName));
            }

            @Override
            public RequestDispatcher getView(Class clazz, String viewName) throws IOException {
                return RequestDispatcher.fromJakartaRequestDispatcher(from.getView(clazz, viewName));
            }

            @Override
            public RequestDispatcher getView(Klass<?> clazz, String viewName) throws IOException {
                return RequestDispatcher.fromJakartaRequestDispatcher(from.getView(clazz, viewName));
            }

            @Override
            public String getRootPath() {
                return from.getRootPath();
            }

            @Override
            public String getReferer() {
                return from.getReferer();
            }

            @Override
            public List<Ancestor> getAncestors() {
                return from.getAncestors();
            }

            @Override
            public Ancestor findAncestor(Class type) {
                return from.findAncestor(type);
            }

            @Override
            public <T> T findAncestorObject(Class<T> type) {
                return from.findAncestorObject(type);
            }

            @Override
            public Ancestor findAncestor(Object o) {
                return from.findAncestor(o);
            }

            @Override
            public boolean hasParameter(String name) {
                return from.hasParameter(name);
            }

            @Override
            public String getOriginalRequestURI() {
                return from.getOriginalRequestURI();
            }

            @Override
            public boolean checkIfModified(long timestampOfResource, StaplerResponse rsp) {
                return from.checkIfModified(timestampOfResource, rsp.toStaplerResponse2());
            }

            @Override
            public boolean checkIfModified(Date timestampOfResource, StaplerResponse rsp) {
                return from.checkIfModified(timestampOfResource, rsp.toStaplerResponse2());
            }

            @Override
            public boolean checkIfModified(Calendar timestampOfResource, StaplerResponse rsp) {
                return from.checkIfModified(timestampOfResource, rsp.toStaplerResponse2());
            }

            @Override
            public boolean checkIfModified(long timestampOfResource, StaplerResponse rsp, long expiration) {
                return from.checkIfModified(timestampOfResource, rsp.toStaplerResponse2(), expiration);
            }

            @Override
            public void bindParameters(Object bean) {
                from.bindParameters(bean);
            }

            @Override
            public void bindParameters(Object bean, String prefix) {
                from.bindParameters(bean, prefix);
            }

            @Override
            public <T> List<T> bindParametersToList(Class<T> type, String prefix) {
                return from.bindParametersToList(type, prefix);
            }

            @Override
            public <T> T bindParameters(Class<T> type, String prefix) {
                return from.bindParameters(type, prefix);
            }

            @Override
            public <T> T bindParameters(Class<T> type, String prefix, int index) {
                return from.bindParameters(type, prefix, index);
            }

            @Override
            public <T> T bindJSON(Class<T> type, JSONObject src) {
                return from.bindJSON(type, src);
            }

            @Override
            public <T> T bindJSON(Type genericType, Class<T> erasure, Object json) {
                return from.bindJSON(genericType, erasure, json);
            }

            @Override
            public void bindJSON(Object bean, JSONObject src) {
                from.bindJSON(bean, src);
            }

            @Override
            public <T> List<T> bindJSONToList(Class<T> type, Object src) {
                return from.bindJSONToList(type, src);
            }

            @Override
            public BindInterceptor getBindInterceptor() {
                return from.getBindInterceptor();
            }

            @Override
            public BindInterceptor setBindListener(BindInterceptor bindListener) {
                return from.setBindListener(bindListener);
            }

            @Override
            public BindInterceptor setBindInterceptpr(BindInterceptor bindListener) {
                return from.setBindInterceptpr(bindListener);
            }

            @Override
            public BindInterceptor setBindInterceptor(BindInterceptor bindListener) {
                return from.setBindInterceptor(bindListener);
            }

            @Override
            public JSONObject getSubmittedForm() throws ServletException {
                try {
                    return from.getSubmittedForm();
                } catch (jakarta.servlet.ServletException e) {
                    throw ServletException.fromJakartaServletException(e);
                }
            }

            @Override
            public FileItem getFileItem2(String name) throws ServletException, IOException {
                try {
                    return from.getFileItem2(name);
                } catch (jakarta.servlet.ServletException e) {
                    throw ServletException.fromJakartaServletException(e);
                }
            }

            @Override
            public org.apache.commons.fileupload.FileItem getFileItem(String name)
                    throws ServletException, IOException {
                try {
                    return from.getFileItem(name);
                } catch (jakarta.servlet.ServletException e) {
                    throw ServletException.fromJakartaServletException(e);
                }
            }

            @Override
            public boolean isJavaScriptProxyCall() {
                return from.isJavaScriptProxyCall();
            }

            @Override
            public BoundObjectTable getBoundObjectTable() {
                return from.getBoundObjectTable();
            }

            @Override
            public String createJavaScriptProxy(Object toBeExported) {
                return from.createJavaScriptProxy(toBeExported);
            }

            @Override
            public RenderOnDemandParameters createJavaScriptProxyParameters(Object toBeExported) {
                StaplerRequest2.RenderOnDemandParameters result = from.createJavaScriptProxyParameters(toBeExported);
                return new RenderOnDemandParameters(result.proxyMethod, result.crumb, result.url, result.urlNames);
            }

            @Override
            public AsyncContext startAsync() {
                return AsyncContext.fromJakartaAsyncContext(from.startAsync());
            }

            @Override
            public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) {
                return AsyncContext.fromJakartaAsyncContext(from.startAsync(
                        servletRequest.toJakartaServletRequest(), servletResponse.toJakartaServletResponse()));
            }

            @Override
            public boolean isAsyncStarted() {
                return from.isAsyncStarted();
            }

            @Override
            public boolean isAsyncSupported() {
                return from.isAsyncSupported();
            }

            @Override
            public AsyncContext getAsyncContext() {
                return AsyncContext.fromJakartaAsyncContext(from.getAsyncContext());
            }

            @Override
            public DispatcherType getDispatcherType() {
                return DispatcherType.fromJakartaDispatcherType(from.getDispatcherType());
            }

            @Override
            public String getAuthType() {
                return from.getAuthType();
            }

            @Override
            public Cookie[] getCookies() {
                return Stream.of(from.getCookies())
                        .map(Cookie::fromJakartaServletHttpCookie)
                        .toArray(Cookie[]::new);
            }

            @Override
            public long getDateHeader(String name) {
                return from.getDateHeader(name);
            }

            @Override
            public String getHeader(String name) {
                return from.getHeader(name);
            }

            @Override
            public Enumeration<String> getHeaders(String name) {
                return from.getHeaders(name);
            }

            @Override
            public Enumeration<String> getHeaderNames() {
                return from.getHeaderNames();
            }

            @Override
            public int getIntHeader(String name) {
                return from.getIntHeader(name);
            }

            @Override
            public HttpServletMapping getHttpServletMapping() {
                return HttpServletMapping.fromJakartaHttpServletMapping(from.getHttpServletMapping());
            }

            @Override
            public String getMethod() {
                return from.getMethod();
            }

            @Override
            public String getPathInfo() {
                return from.getPathInfo();
            }

            @Override
            public String getPathTranslated() {
                return from.getPathTranslated();
            }

            @Override
            public PushBuilder newPushBuilder() {
                // TODO implement this
                throw new UnsupportedOperationException();
            }

            @Override
            public String getContextPath() {
                return from.getContextPath();
            }

            @Override
            public String getQueryString() {
                return from.getQueryString();
            }

            @Override
            public String getRemoteUser() {
                return from.getRemoteUser();
            }

            @Override
            public boolean isUserInRole(String role) {
                return from.isUserInRole(role);
            }

            @Override
            public Principal getUserPrincipal() {
                return from.getUserPrincipal();
            }

            @Override
            public String getRequestedSessionId() {
                return from.getRequestedSessionId();
            }

            @Override
            public String getRequestURI() {
                return from.getRequestURI();
            }

            @Override
            public StringBuffer getRequestURL() {
                return from.getRequestURL();
            }

            @Override
            public String getServletPath() {
                return from.getServletPath();
            }

            @Override
            public HttpSession getSession(boolean create) {
                jakarta.servlet.http.HttpSession session = from.getSession(create);
                return session != null ? HttpSession.fromJakartaHttpSession(session) : null;
            }

            @Override
            public HttpSession getSession() {
                jakarta.servlet.http.HttpSession session = from.getSession();
                return session != null ? HttpSession.fromJakartaHttpSession(session) : null;
            }

            @Override
            public String changeSessionId() {
                return from.changeSessionId();
            }

            @Override
            public boolean isRequestedSessionIdValid() {
                return from.isRequestedSessionIdValid();
            }

            @Override
            public boolean isRequestedSessionIdFromCookie() {
                return from.isRequestedSessionIdFromCookie();
            }

            @Override
            public boolean isRequestedSessionIdFromURL() {
                return from.isRequestedSessionIdFromURL();
            }

            @Override
            public boolean isRequestedSessionIdFromUrl() {
                return from.isRequestedSessionIdFromUrl();
            }

            @Override
            public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
                try {
                    return from.authenticate(response.toJakartaHttpServletResponse());
                } catch (jakarta.servlet.ServletException e) {
                    throw ServletException.fromJakartaServletException(e);
                }
            }

            @Override
            public void login(String username, String password) throws ServletException {
                try {
                    from.login(username, password);
                } catch (jakarta.servlet.ServletException e) {
                    throw ServletException.fromJakartaServletException(e);
                }
            }

            @Override
            public void logout() throws ServletException {
                try {
                    from.logout();
                } catch (jakarta.servlet.ServletException e) {
                    throw ServletException.fromJakartaServletException(e);
                }
            }

            @Override
            public Collection<Part> getParts() throws IOException, ServletException {
                try {
                    return from.getParts().stream().map(Part::fromJakartaPart).collect(Collectors.toList());
                } catch (jakarta.servlet.ServletException e) {
                    throw ServletException.fromJakartaServletException(e);
                }
            }

            @Override
            public Part getPart(String name) throws IOException, ServletException {
                try {
                    return Part.fromJakartaPart(from.getPart(name));
                } catch (jakarta.servlet.ServletException e) {
                    throw ServletException.fromJakartaServletException(e);
                }
            }

            @Override
            public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) {
                // TODO implement this
                throw new UnsupportedOperationException();
            }

            @Override
            public Map<String, String> getTrailerFields() {
                return from.getTrailerFields();
            }

            @Override
            public boolean isTrailerFieldsReady() {
                return from.isTrailerFieldsReady();
            }

            @Override
            public jakarta.servlet.ServletRequest toJakartaServletRequest() {
                return from;
            }

            @Override
            public jakarta.servlet.http.HttpServletRequest toJakartaHttpServletRequest() {
                return from;
            }

            @Override
            public StaplerRequest2 toStaplerRequest2() {
                return from;
            }
        };
    }
}
