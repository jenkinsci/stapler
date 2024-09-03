package org.kohsuke.stapler;

import io.jenkins.servlet.FilterChainWrapper;
import io.jenkins.servlet.FilterConfigWrapper;
import io.jenkins.servlet.ServletExceptionWrapper;
import io.jenkins.servlet.ServletRequestWrapper;
import io.jenkins.servlet.ServletResponseWrapper;
import io.jenkins.servlet.http.HttpServletRequestWrapper;
import io.jenkins.servlet.http.HttpServletResponseWrapper;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;

public interface CompatibleFilter extends Filter {
    /**
     * @deprecated use {@link #init(FilterConfig)}
     */
    @Deprecated
    default void init(javax.servlet.FilterConfig filterConfig) throws javax.servlet.ServletException {
        try {
            init(FilterConfigWrapper.toJakartaFilterConfig(filterConfig));
        } catch (ServletException e) {
            throw ServletExceptionWrapper.fromJakartaServletException(e);
        }
    }

    /**
     * @deprecated use {@link #doFilter(ServletRequest, ServletResponse, FilterChain)}
     */
    @Deprecated
    default void doFilter(
            javax.servlet.ServletRequest request,
            javax.servlet.ServletResponse response,
            javax.servlet.FilterChain chain)
            throws IOException, javax.servlet.ServletException {
        try {
            if (request instanceof javax.servlet.http.HttpServletRequest
                    && response instanceof javax.servlet.http.HttpServletResponse) {
                javax.servlet.http.HttpServletRequest httpRequest = (javax.servlet.http.HttpServletRequest) request;
                javax.servlet.http.HttpServletResponse httpResponse = (javax.servlet.http.HttpServletResponse) response;
                doFilter(
                        HttpServletRequestWrapper.toJakartaHttpServletRequest(httpRequest),
                        HttpServletResponseWrapper.toJakartaHttpServletResponse(httpResponse),
                        FilterChainWrapper.toJakartaFilterChain(chain));
            } else {
                doFilter(
                        ServletRequestWrapper.toJakartaServletRequest(request),
                        ServletResponseWrapper.toJakartaServletResponse(response),
                        FilterChainWrapper.toJakartaFilterChain(chain));
            }
        } catch (ServletException e) {
            throw ServletExceptionWrapper.fromJakartaServletException(e);
        }
    }
}
