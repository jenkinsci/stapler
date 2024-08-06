package io.jenkins.servlet;

import java.util.Objects;
import javax.servlet.DispatcherType;

public class DispatcherTypeWrapper {
    public static jakarta.servlet.DispatcherType toJakartaDispatcherType(DispatcherType from) {
        Objects.requireNonNull(from);
        switch (from) {
            case FORWARD:
                return jakarta.servlet.DispatcherType.FORWARD;
            case INCLUDE:
                return jakarta.servlet.DispatcherType.INCLUDE;
            case REQUEST:
                return jakarta.servlet.DispatcherType.REQUEST;
            case ASYNC:
                return jakarta.servlet.DispatcherType.ASYNC;
            case ERROR:
                return jakarta.servlet.DispatcherType.ERROR;
            default:
                throw new IllegalArgumentException("Unknown DispatcherType: " + from);
        }
    }

    public static DispatcherType fromJakartaDispatcherType(jakarta.servlet.DispatcherType from) {
        Objects.requireNonNull(from);
        switch (from) {
            case FORWARD:
                return DispatcherType.FORWARD;
            case INCLUDE:
                return DispatcherType.INCLUDE;
            case REQUEST:
                return DispatcherType.REQUEST;
            case ASYNC:
                return DispatcherType.ASYNC;
            case ERROR:
                return DispatcherType.ERROR;
            default:
                throw new IllegalArgumentException("Unknown DispatcherType: " + from);
        }
    }
}
