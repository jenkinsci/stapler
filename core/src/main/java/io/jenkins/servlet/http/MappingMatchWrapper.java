package io.jenkins.servlet.http;

import java.util.Objects;
import javax.servlet.http.MappingMatch;

public class MappingMatchWrapper {
    public static jakarta.servlet.http.MappingMatch toJakartaMappingMatch(MappingMatch from) {
        Objects.requireNonNull(from);
        switch (from) {
            case CONTEXT_ROOT:
                return jakarta.servlet.http.MappingMatch.CONTEXT_ROOT;
            case DEFAULT:
                return jakarta.servlet.http.MappingMatch.DEFAULT;
            case EXACT:
                return jakarta.servlet.http.MappingMatch.EXACT;
            case EXTENSION:
                return jakarta.servlet.http.MappingMatch.EXTENSION;
            case PATH:
                return jakarta.servlet.http.MappingMatch.PATH;
            default:
                throw new IllegalArgumentException("Unknown MappingMatch: " + from);
        }
    }

    public static MappingMatch fromJakartaMappingMatch(jakarta.servlet.http.MappingMatch from) {
        Objects.requireNonNull(from);
        switch (from) {
            case CONTEXT_ROOT:
                return MappingMatch.CONTEXT_ROOT;
            case DEFAULT:
                return MappingMatch.DEFAULT;
            case EXACT:
                return MappingMatch.EXACT;
            case EXTENSION:
                return MappingMatch.EXTENSION;
            case PATH:
                return MappingMatch.PATH;
            default:
                throw new IllegalArgumentException("Unknown MappingMatch: " + from);
        }
    }
}
