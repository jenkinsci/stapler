package io.jenkins.servlet;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.jenkins.servlet.http.HttpServletRequestWrapper;
import io.jenkins.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.util.Objects;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RequestDispatcherWrapper {
    public static jakarta.servlet.RequestDispatcher toJakartaRequestDispatcher(RequestDispatcher from) {
        Objects.requireNonNull(from);
        return new jakarta.servlet.RequestDispatcher() {
            @Override
            @SuppressFBWarnings(value = "REQUESTDISPATCHER_FILE_DISCLOSURE", justification = "for compatibility")
            public void forward(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response)
                    throws jakarta.servlet.ServletException, IOException {
                try {
                    if (request instanceof jakarta.servlet.http.HttpServletRequest
                            && response instanceof jakarta.servlet.http.HttpServletResponse) {
                        jakarta.servlet.http.HttpServletRequest httpRequest =
                                (jakarta.servlet.http.HttpServletRequest) request;
                        jakarta.servlet.http.HttpServletResponse httpResponse =
                                (jakarta.servlet.http.HttpServletResponse) response;
                        from.forward(
                                HttpServletRequestWrapper.fromJakartaHttpServletRequest(httpRequest),
                                HttpServletResponseWrapper.fromJakartaHttpServletResponse(httpResponse));
                    } else {
                        from.forward(
                                io.jenkins.servlet.ServletRequestWrapper.fromJakartaServletRequest(request),
                                ServletResponseWrapper.fromJakartaServletResponse(response));
                    }
                } catch (ServletException e) {
                    throw ServletExceptionWrapper.toJakartaServletException(e);
                }
            }

            @Override
            @SuppressFBWarnings(value = "REQUESTDISPATCHER_FILE_DISCLOSURE", justification = "for compatibility")
            public void include(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response)
                    throws jakarta.servlet.ServletException, IOException {
                try {
                    if (request instanceof jakarta.servlet.http.HttpServletRequest
                            && response instanceof jakarta.servlet.http.HttpServletResponse) {
                        jakarta.servlet.http.HttpServletRequest httpRequest =
                                (jakarta.servlet.http.HttpServletRequest) request;
                        jakarta.servlet.http.HttpServletResponse httpResponse =
                                (jakarta.servlet.http.HttpServletResponse) response;
                        from.include(
                                HttpServletRequestWrapper.fromJakartaHttpServletRequest(httpRequest),
                                HttpServletResponseWrapper.fromJakartaHttpServletResponse(httpResponse));
                    } else {
                        from.include(
                                ServletRequestWrapper.fromJakartaServletRequest(request),
                                ServletResponseWrapper.fromJakartaServletResponse(response));
                    }
                } catch (ServletException e) {
                    throw ServletExceptionWrapper.toJakartaServletException(e);
                }
            }
        };
    }

    public static RequestDispatcher fromJakartaRequestDispatcher(jakarta.servlet.RequestDispatcher from) {
        Objects.requireNonNull(from);
        return new RequestDispatcher() {
            @Override
            public void forward(ServletRequest request, ServletResponse response) throws ServletException, IOException {
                try {
                    if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
                        HttpServletRequest httpRequest = (HttpServletRequest) request;
                        HttpServletResponse httpResponse = (HttpServletResponse) response;
                        from.forward(
                                HttpServletRequestWrapper.toJakartaHttpServletRequest(httpRequest),
                                HttpServletResponseWrapper.toJakartaHttpServletResponse(httpResponse));
                    } else {
                        from.forward(
                                io.jenkins.servlet.ServletRequestWrapper.toJakartaServletRequest(request),
                                ServletResponseWrapper.toJakartaServletResponse(response));
                    }
                } catch (jakarta.servlet.ServletException e) {
                    throw ServletExceptionWrapper.fromJakartaServletException(e);
                }
            }

            @Override
            public void include(ServletRequest request, ServletResponse response) throws ServletException, IOException {
                try {
                    if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
                        HttpServletRequest httpRequest = (HttpServletRequest) request;
                        HttpServletResponse httpResponse = (HttpServletResponse) response;
                        from.include(
                                HttpServletRequestWrapper.toJakartaHttpServletRequest(httpRequest),
                                HttpServletResponseWrapper.toJakartaHttpServletResponse(httpResponse));
                    } else {
                        from.include(
                                io.jenkins.servlet.ServletRequestWrapper.toJakartaServletRequest(request),
                                ServletResponseWrapper.toJakartaServletResponse(response));
                    }
                } catch (jakarta.servlet.ServletException e) {
                    throw ServletExceptionWrapper.fromJakartaServletException(e);
                }
            }
        };
    }
}
