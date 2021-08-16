package org.kohsuke.stapler.test;

import com.gargoylesoftware.htmlunit.WebClient;
import junit.framework.TestCase;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.WebApp;

import javax.servlet.ServletContext;
import java.net.URL;

/**
 * Base test case for embedded Jetty.
 *
 * Your test method runs with embedded Jetty with the instance of your test class bound to the root "application"
 * object in Stapler. You can use {@link WebClient} to make HTTP calls, receive that on your test class,
 * and inspect side-effects, etc.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class JettyTestCase extends TestCase {
    protected Server server;
    /**
     * The top URL of this test web application.
     */
    protected URL url;
    protected Stapler stapler;
    protected ServletContext servletContext;
    protected WebApp webApp;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        server = new Server();

        server.setHandler(new WebAppContext("/noroot", ""));

        final ServletContextHandler context = new ServletContextHandler(server, getContextPath(), ServletContextHandler.SESSIONS);
        configure(context);
        server.setHandler(context);

        ServerConnector connector = new ServerConnector(server);
        server.addConnector(connector);
        server.start();

        url = new URL("http://localhost:"+connector.getLocalPort()+getContextPath()+"/");

        servletContext = context.getServletContext();
        webApp = WebApp.get(servletContext);

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

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        server.stop();
    }
}
