package org.kohsuke.stapler.test;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.UUID;
import org.eclipse.jetty.ee9.servlet.ServletContextHandler;
import org.eclipse.jetty.ee9.servlet.ServletHolder;
import org.eclipse.jetty.ee9.webapp.WebAppContext;
import org.eclipse.jetty.http.UriCompliance;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.htmlunit.WebClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.kohsuke.stapler.CrumbIssuer;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.WebApp;

/**
 * Base test case for embedded Jetty.
 *
 * Your test method runs with embedded Jetty with the instance of your test class bound to the root "application"
 * object in Stapler. You can use {@link WebClient} to make HTTP calls, receive that on your test class,
 * and inspect side-effects, etc.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class JettyTestCase {
    protected Server server;
    /**
     * The top URL of this test web application.
     */
    protected URL url;

    protected Stapler stapler;
    protected ServletContext servletContext;
    protected WebApp webApp;

    private static final String ATTRIBUTE_NAME = JettyTestCase.class.getName() + ".CRUMB_ISSUER";

    protected static final CrumbIssuer CRUMB_ISSUER = new CrumbIssuer() {
        @Override
        public String issueCrumb(StaplerRequest2 request) {
            HttpSession s = request.getSession();
            String v = (String) s.getAttribute(ATTRIBUTE_NAME);
            if (v != null) {
                return v;
            }
            v = UUID.randomUUID().toString();
            s.setAttribute(ATTRIBUTE_NAME, v);
            return v;
        }

        @Override
        public String getCrumbExpression() {
            return "document.head.dataset.crumbValue";
        }
    };

    @BeforeEach
    protected void beforeEach() throws Exception {
        server = new Server();

        server.setHandler(new WebAppContext("/noroot", ""));

        final ServletContextHandler context =
                new ServletContextHandler(server, getContextPath(), ServletContextHandler.SESSIONS);
        configure(context);
        server.setHandler(context);

        ServerConnector connector = new ServerConnector(server);
        HttpConfiguration hc =
                connector.getConnectionFactory(HttpConnectionFactory.class).getHttpConfiguration();
        hc.setUriCompliance(UriCompliance.LEGACY);
        server.addConnector(connector);
        server.start();

        url = new URL("http://localhost:" + connector.getLocalPort() + getContextPath() + "/");

        servletContext = context.getServletContext();
        webApp = WebApp.get(servletContext);
        webApp.setCrumbIssuer(CRUMB_ISSUER);

        // export the test object as the root as a reasonable default.
        webApp.setApp(this);
    }

    /**
     * Can be used to set different context path for the root object.
     *
     * For example, "/foo/bar"
     */
    protected String getContextPath() {
        return "";
    }

    /**
     * Sets up how the servlet/filters are bound.
     */
    protected void configure(ServletContextHandler context) {
        context.addServlet(new ServletHolder(new Stapler()), "/*");
    }

    /**
     * Get the {@link WebClient} preconfigured for testing.
     */
    protected WebClient createWebClient() {
        WebClient webClient = new WebClient();
        webClient.getOptions().setFetchPolyfillEnabled(true);
        return webClient;
    }

    /**
     * Blocks until the ENTER key is hit.
     * This is useful during debugging a test so that one can inspect the state through the web browser.
     */
    protected void interactiveBreak() throws IOException {
        System.out.println("Jetty is running at " + url);
        new BufferedReader(new InputStreamReader(System.in, Charset.defaultCharset())).readLine();
    }

    @AfterEach
    protected void afterEach() throws Exception {
        server.stop();
    }
}
