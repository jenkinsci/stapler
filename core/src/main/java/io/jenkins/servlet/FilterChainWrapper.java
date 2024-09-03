package io.jenkins.servlet;

import io.jenkins.servlet.http.HttpServletRequestWrapper;
import io.jenkins.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.util.Objects;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FilterChainWrapper {
    public static jakarta.servlet.FilterChain toJakartaFilterChain(FilterChain from) {
        Objects.requireNonNull(from);
        return new jakarta.servlet.FilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response)
                    throws IOException, jakarta.servlet.ServletException {
                try {
                    if (request instanceof jakarta.servlet.http.HttpServletRequest
                            && response instanceof jakarta.servlet.http.HttpServletResponse) {
                        jakarta.servlet.http.HttpServletRequest httpRequest =
                                (jakarta.servlet.http.HttpServletRequest) request;
                        jakarta.servlet.http.HttpServletResponse httpResponse =
                                (jakarta.servlet.http.HttpServletResponse) response;
                        from.doFilter(
                                HttpServletRequestWrapper.fromJakartaHttpServletRequest(httpRequest),
                                HttpServletResponseWrapper.fromJakartaHttpServletResponse(httpResponse));
                    } else {
                        from.doFilter(
                                ServletRequestWrapper.fromJakartaServletRequest(request),
                                ServletResponseWrapper.fromJakartaServletResponse(response));
                    }
                } catch (ServletException e) {
                    throw ServletExceptionWrapper.toJakartaServletException(e);
                }
            }
        };
    }

    public static FilterChain fromJakartaFilterChain(jakarta.servlet.FilterChain from) {
        Objects.requireNonNull(from);
        return new FilterChain() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response)
                    throws IOException, ServletException {
                try {
                    if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
                        HttpServletRequest httpRequest = (HttpServletRequest) request;
                        HttpServletResponse httpResponse = (HttpServletResponse) response;
                        from.doFilter(
                                HttpServletRequestWrapper.toJakartaHttpServletRequest(httpRequest),
                                HttpServletResponseWrapper.toJakartaHttpServletResponse(httpResponse));
                    } else {
                        from.doFilter(
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
