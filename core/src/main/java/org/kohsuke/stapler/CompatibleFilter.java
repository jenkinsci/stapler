package org.kohsuke.stapler;

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
    default void init(javax.servlet.FilterConfig filterConfig)
            throws javax.servlet.ServletException {
        try {
            init(filterConfig.toJakartaFilterConfig());
        } catch (ServletException e) {
            throw new javax.servlet.ServletException(e);
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
            doFilter(
                    request.toJakartaServletRequest(),
                    response.toJakartaServletResponse(),
                    chain.toJakartaFilterChain());
        } catch (ServletException e) {
            throw new javax.servlet.ServletException(e);
        }
    }
}
