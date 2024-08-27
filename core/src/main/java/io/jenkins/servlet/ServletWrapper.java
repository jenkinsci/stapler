package io.jenkins.servlet;

import io.jenkins.servlet.http.HttpServletRequestWrapper;
import io.jenkins.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.util.Objects;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ServletWrapper {
    public static jakarta.servlet.Servlet toJakartaServlet(Servlet from) {
        Objects.requireNonNull(from);
        return new jakarta.servlet.Servlet() {
            @Override
            public void init(jakarta.servlet.ServletConfig config) throws jakarta.servlet.ServletException {
                try {
                    from.init(ServletConfigWrapper.fromJakartaServletConfig(config));
                } catch (ServletException e) {
                    throw ServletExceptionWrapper.toJakartaServletException(e);
                }
            }

            @Override
            public jakarta.servlet.ServletConfig getServletConfig() {
                return ServletConfigWrapper.toJakartaServletConfig(from.getServletConfig());
            }

            @Override
            public void service(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response)
                    throws jakarta.servlet.ServletException, IOException {
                try {
                    if (request instanceof jakarta.servlet.http.HttpServletRequest
                            && response instanceof jakarta.servlet.http.HttpServletResponse) {
                        jakarta.servlet.http.HttpServletRequest httpRequest =
                                (jakarta.servlet.http.HttpServletRequest) request;
                        jakarta.servlet.http.HttpServletResponse httpResponse =
                                (jakarta.servlet.http.HttpServletResponse) response;
                        from.service(
                                HttpServletRequestWrapper.fromJakartaHttpServletRequest(httpRequest),
                                HttpServletResponseWrapper.fromJakartaHttpServletResponse(httpResponse));
                    } else {
                        from.service(
                                ServletRequestWrapper.fromJakartaServletRequest(request),
                                ServletResponseWrapper.fromJakartaServletResponse(response));
                    }
                } catch (ServletException e) {
                    throw ServletExceptionWrapper.toJakartaServletException(e);
                }
            }

            @Override
            public String getServletInfo() {
                return from.getServletInfo();
            }

            @Override
            public void destroy() {
                from.destroy();
            }
        };
    }

    public static Servlet fromJakartaServlet(jakarta.servlet.Servlet from) {
        Objects.requireNonNull(from);
        return new Servlet() {
            @Override
            public void init(ServletConfig config) throws ServletException {
                try {
                    from.init(ServletConfigWrapper.toJakartaServletConfig(config));
                } catch (jakarta.servlet.ServletException e) {
                    throw ServletExceptionWrapper.fromJakartaServletException(e);
                }
            }

            @Override
            public ServletConfig getServletConfig() {
                return ServletConfigWrapper.fromJakartaServletConfig(from.getServletConfig());
            }

            @Override
            public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
                try {
                    if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
                        HttpServletRequest httpRequest = (HttpServletRequest) request;
                        HttpServletResponse httpResponse = (HttpServletResponse) response;
                        from.service(
                                HttpServletRequestWrapper.toJakartaHttpServletRequest(httpRequest),
                                HttpServletResponseWrapper.toJakartaHttpServletResponse(httpResponse));
                    } else {
                        from.service(
                                io.jenkins.servlet.ServletRequestWrapper.toJakartaServletRequest(request),
                                ServletResponseWrapper.toJakartaServletResponse(response));
                    }
                } catch (jakarta.servlet.ServletException e) {
                    throw ServletExceptionWrapper.fromJakartaServletException(e);
                }
            }

            @Override
            public String getServletInfo() {
                return from.getServletInfo();
            }

            @Override
            public void destroy() {
                from.destroy();
            }
        };
    }
}
