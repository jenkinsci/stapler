package org.kohsuke.stapler;

import net.sf.json.JSONObject;

import javax.annotation.PostConstruct;
import java.beans.Introspector;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * Designates a setter method or a field used to databind JSON values into objects in methods like
 * {@link StaplerRequest#bindJSON(Class, JSONObject)} and
 * {@link StaplerRequest#bindParameters(Class, String)}.
 *
 * <p>
 * Stapler will first invoke {@link DataBoundConstructor}-annotated constructor, and if there's any
 * remaining properties in JSON, it'll try to find a matching {@link DataBoundSetter}-annotated setter
 * method or a field.
 *
 * <p>
 * The setter method is discovered through {@link Introspector}, so setter method name must match
 * the property name (such as <tt>setFoo</tt> for the <tt>foo</tt> property), and it needs to be public.
 *
 * <p>
 * The field is discovered through simple reflection, so its name must match the property name, but
 * its access modifier can be anything.
 *
 * <p>
 * To create a method to be called after all the setter injections are complete, annotate a method
 * with {@link PostConstruct}.
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(RUNTIME)
@Target({METHOD,FIELD})
@Documented
public @interface DataBoundSetter {
}
