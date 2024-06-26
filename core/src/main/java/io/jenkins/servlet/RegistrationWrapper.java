package io.jenkins.servlet;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.servlet.Registration;

public class RegistrationWrapper {
    public static jakarta.servlet.Registration toJakartaRegistration(Registration from) {
        Objects.requireNonNull(from);
        return new jakarta.servlet.Registration() {
            @Override
            public String getName() {
                return from.getName();
            }

            @Override
            public String getClassName() {
                return from.getClassName();
            }

            @Override
            public boolean setInitParameter(String name, String value) {
                return from.setInitParameter(name, value);
            }

            @Override
            public String getInitParameter(String name) {
                return from.getInitParameter(name);
            }

            @Override
            public Set<String> setInitParameters(Map<String, String> initParameters) {
                return from.setInitParameters(initParameters);
            }

            @Override
            public Map<String, String> getInitParameters() {
                return from.getInitParameters();
            }
        };
    }

    public static Registration fromJakartaRegistration(jakarta.servlet.Registration from) {
        Objects.requireNonNull(from);
        return new Registration() {
            @Override
            public String getName() {
                return from.getName();
            }

            @Override
            public String getClassName() {
                return from.getClassName();
            }

            @Override
            public boolean setInitParameter(String name, String value) {
                return from.setInitParameter(name, value);
            }

            @Override
            public String getInitParameter(String name) {
                return from.getInitParameter(name);
            }

            @Override
            public Set<String> setInitParameters(Map<String, String> initParameters) {
                return from.setInitParameters(initParameters);
            }

            @Override
            public Map<String, String> getInitParameters() {
                return from.getInitParameters();
            }
        };
    }
}
