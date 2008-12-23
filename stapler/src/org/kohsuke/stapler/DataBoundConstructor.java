package org.kohsuke.stapler;

import net.sf.json.JSONObject;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Desginates the constructor to be created
 * from methods like
 * {@link StaplerRequest#bindJSON(Class, JSONObject)} and
 * {@link StaplerRequest#bindParameters(Class, String)}.
 *
 * <p>
 * This replaces "@stapler-constructor" annotation.
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(RUNTIME)
@Target(CONSTRUCTOR)
@Documented
public @interface DataBoundConstructor {
}
