package org.kohsuke.stapler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.kohsuke.stapler.test.AbstractStaplerTestV4;

/**
 * This class needs to be in this package to access package-protected stuff.
 * You should extend from {@link AbstractStaplerTestV4}.
 * Like {@link AbstractStaplerTestBase} but for JUnit4 style tests.
 *
 * @author Jakob Ackermann
 */
public abstract class AbstractStaplerTestBaseV4 {

    protected WebApp webApp;
    protected RequestImpl request;
    protected ResponseImpl response;
    protected Stapler stapler = new Stapler();
    protected HttpServletRequest rawRequest;
    protected HttpServletResponse rawResponse;

    @BeforeEach
    protected void beforeEach() throws Exception {
        ServletContext servletContext = mock(ServletContext.class);

        webApp = new WebApp(servletContext);

        ServletConfig servletConfig = mock(ServletConfig.class);
        when(servletConfig.getServletContext()).thenReturn(servletContext);
        stapler.init(servletConfig);

        rawRequest = mock(HttpServletRequest.class);
        rawResponse = mock(HttpServletResponse.class);

        this.request = new RequestImpl(stapler, rawRequest, new ArrayList<>(), new TokenList(""));
        Stapler.CURRENT_REQUEST.set(this.request);

        this.response = new ResponseImpl(stapler, rawResponse);
        Stapler.CURRENT_RESPONSE.set(this.response);
    }

    @AfterEach
    protected void afterEach() throws Exception {
        Stapler.CURRENT_REQUEST.remove();
        Stapler.CURRENT_RESPONSE.remove();
    }
}
