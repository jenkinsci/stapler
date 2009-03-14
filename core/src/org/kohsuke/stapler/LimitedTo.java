package org.kohsuke.stapler;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Declares that methods are only available for requests that
 * have the specified role(s).
 *
 * <p>
 * This annotation should be placed on methods that need to be
 * secured (iow protected from anonymous users.)
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(RUNTIME)
@Target({METHOD,FIELD})
public @interface LimitedTo {
    /**
     * The name of role.
     * the method can be invoked only if the user belongs
     * to this role.
     */
    String value();
}
