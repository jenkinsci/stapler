package org.kohsuke.stapler.interceptor;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * Marks the annotation as an interceptor annotation,
 * which executes before/after the method invocation of domain objects happen
 * as a part of the request processing.
 *
 * <p>
 * This mechanism is useful for performing declarative processing/check on domain objects,
 * such as checking HTTP headers, performing the access control, etc.
 *
 * @author Kohsuke Kawaguchi
 * @see Interceptor
 * @see RequirePOST
 */
@Target(ANNOTATION_TYPE)
@Retention(RUNTIME)
@Documented
public @interface InterceptorAnnotation  {
    /**
     * Actual interceptor logic. Must have a default constructor.
     */
    Class<? extends Interceptor> value();
}
