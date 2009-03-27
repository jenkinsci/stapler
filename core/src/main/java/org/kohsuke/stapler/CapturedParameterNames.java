package org.kohsuke.stapler;

import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This "hidden" annotation is injected by Groovy compiler to capture parameter names
 * in the class file. Groovyc doesn't let me generate additional files, so this is easier
 * to do than generating the same files that the annotation processor does.
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(RUNTIME)
public @interface CapturedParameterNames {
    String[] value();
}
