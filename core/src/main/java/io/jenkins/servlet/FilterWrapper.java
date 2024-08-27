package io.jenkins.servlet;

import io.jenkins.servlet.http.HttpServletRequestWrapper;
import io.jenkins.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.util.Objects;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FilterWrapper {
    public static jakarta.servlet.Filter toJakartaFilter(Filter from) {
        Objects.requireNonNull(from);
        return new jakarta.servlet.Filter() {
            @Override
            public void init(jakarta.servlet.FilterConfig filterConfig) throws jakarta.servlet.ServletException {
                try {
                    from.init(FilterConfigWrapper.fromJakartaFilterConfig(filterConfig));
                } catch (ServletException e) {
                    throw ServletExceptionWrapper.toJakartaServletException(e);
                }
            }

            @Override
            public void doFilter(
                    jakarta.servlet.ServletRequest request,
                    jakarta.servlet.ServletResponse response,
                    jakarta.servlet.FilterChain chain)
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
                                HttpServletResponseWrapper.fromJakartaHttpServletResponse(httpResponse),
                                FilterChainWrapper.fromJakartaFilterChain(chain));
                    } else {
                        from.doFilter(
                                ServletRequestWrapper.fromJakartaServletRequest(request),
                                ServletResponseWrapper.fromJakartaServletResponse(response),
                                FilterChainWrapper.fromJakartaFilterChain(chain));
                    }
                } catch (ServletException e) {
                    throw ServletExceptionWrapper.toJakartaServletException(e);
                }
            }

            @Override
            public void destroy() {
                from.destroy();
            }
        };
    }

    public static Filter fromJakartaFilter(jakarta.servlet.Filter from) {
        Objects.requireNonNull(from);
        return new Filter() {
            @Override
            public void init(FilterConfig filterConfig) throws ServletException {
                try {
                    from.init(FilterConfigWrapper.toJakartaFilterConfig(filterConfig));
                } catch (jakarta.servlet.ServletException e) {
                    throw ServletExceptionWrapper.fromJakartaServletException(e);
                }
            }

            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                    throws IOException, ServletException {
                try {
                    if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
                        HttpServletRequest httpRequest = (HttpServletRequest) request;
                        HttpServletResponse httpResponse = (HttpServletResponse) response;
                        from.doFilter(
                                HttpServletRequestWrapper.toJakartaHttpServletRequest(httpRequest),
                                HttpServletResponseWrapper.toJakartaHttpServletResponse(httpResponse),
                                FilterChainWrapper.toJakartaFilterChain(chain));
                    } else {
                        from.doFilter(
                                io.jenkins.servlet.ServletRequestWrapper.toJakartaServletRequest(request),
                                ServletResponseWrapper.toJakartaServletResponse(response),
                                FilterChainWrapper.toJakartaFilterChain(chain));
                    }
                } catch (jakarta.servlet.ServletException e) {
                    throw ServletExceptionWrapper.fromJakartaServletException(e);
                }
            }

            @Override
            public void destroy() {
                from.destroy();
            }
        };
    }
}
