package org.kohsuke.stapler.interceptor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface InterceptorAnnotation {
    /**
     * Actual interceptor logic. Must have a default constructor.
     */
    Class<? extends Interceptor> value();

    /**
     * The point of invocation of this interceptor.
     */
    Stage stage() default Stage.PREINVOKE;
}
