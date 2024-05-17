package org.kohsuke.stapler.util;

public class IllegalReflectiveAccessLogHandler {

    public static String get(IllegalAccessException e) {
        return e.getClass().getName()
                + ": Processing this request relies on deprecated behavior that will be disallowed in future releases of Java. See https://jenkins.io/redirect/stapler-reflective-access/ for more information. Details: "
                + e.getMessage();
    }
}
