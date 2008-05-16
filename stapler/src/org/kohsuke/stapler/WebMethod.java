package org.kohsuke.stapler;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.METHOD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Indicates that the method is bound to HTTP and used to
 * serve the HTTP request.
 *
 * <p>
 * This annotation is assumed to be implicit on every public methods
 * that start with 'do', like 'doFoo' or 'doBar', but you can use this annotation
 * on those methods to assign different names.
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(RUNTIME)
@Target(METHOD)
@Documented
public @interface WebMethod {
    /**
     * URL names assigned to this method.
     *
     * <p>
     * Normally, for <tt>doXyz</tt> method, the name is <tt>xyz</tt>,
     * but you can use this to assign multiple names or non-default names.
     * Often useful for using names that contain non-identifier characters. 
     */
    String[] name();
}
