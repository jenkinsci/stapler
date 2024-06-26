package io.jenkins.servlet.http;

import java.util.Objects;
import javax.servlet.http.HttpServletMapping;
import javax.servlet.http.MappingMatch;

public class HttpServletMappingWrapper {
    public static jakarta.servlet.http.HttpServletMapping toJakartaHttpServletMapping(HttpServletMapping from) {
        Objects.requireNonNull(from);
        return new jakarta.servlet.http.HttpServletMapping() {
            @Override
            public String getMatchValue() {
                return from.getMatchValue();
            }

            @Override
            public String getPattern() {
                return from.getPattern();
            }

            @Override
            public String getServletName() {
                return from.getServletName();
            }

            @Override
            public jakarta.servlet.http.MappingMatch getMappingMatch() {
                return MappingMatchWrapper.toJakartaMappingMatch(from.getMappingMatch());
            }
        };
    }

    public static HttpServletMapping fromJakartaHttpServletMapping(jakarta.servlet.http.HttpServletMapping from) {
        Objects.requireNonNull(from);
        return new HttpServletMapping() {
            @Override
            public String getMatchValue() {
                return from.getMatchValue();
            }

            @Override
            public String getPattern() {
                return from.getPattern();
            }

            @Override
            public String getServletName() {
                return from.getServletName();
            }

            @Override
            public MappingMatch getMappingMatch() {
                return MappingMatchWrapper.fromJakartaMappingMatch(from.getMappingMatch());
            }
        };
    }
}
