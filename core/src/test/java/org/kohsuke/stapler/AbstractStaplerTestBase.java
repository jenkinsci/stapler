package org.kohsuke.stapler;

import junit.framework.TestCase;
import org.kohsuke.stapler.test.AbstractStaplerTest;
import org.mockito.Mockito;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;

/**
 * This class needs to be in this package to access package-protected stuff.
 * You should extend from {@link AbstractStaplerTest}.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class AbstractStaplerTestBase extends TestCase {

    protected WebApp webApp;
    protected RequestImpl request;
    protected ResponseImpl response;
    protected Stapler stapler = new Stapler();
    protected HttpServletRequest rawRequest;
    protected HttpServletResponse rawResponse;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ServletContext servletContext = Mockito.mock(ServletContext.class);

        webApp = new WebApp(servletContext);

        ServletConfig servletConfig = Mockito.mock(ServletConfig.class);
        Mockito.when(servletConfig.getServletContext()).thenReturn(servletContext);
        stapler.init(servletConfig);

        rawRequest = Mockito.mock(HttpServletRequest.class);
        rawResponse = Mockito.mock(HttpServletResponse.class);

        this.request = new RequestImpl(stapler, rawRequest,new ArrayList<AncestorImpl>(),new TokenList(""));
        Stapler.CURRENT_REQUEST.set(this.request);

        this.response = new ResponseImpl(stapler, rawResponse);
        Stapler.CURRENT_RESPONSE.set(this.response);
    }
}
