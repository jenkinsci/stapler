package org.kohsuke.stapler;

import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;

import javax.annotation.PostConstruct;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Designates a field used to databind JSON values into objects in methods like
 * {@link StaplerRequest#bindJSON(Class, JSONObject)} and
 * {@link StaplerRequest#bindParameters(Class, String)}.
 *
 * <p>
 * Stapler will first invoke {@link DataBoundConstructor}-annotated constructor, and if there's any
 * remaining properties in JSON, it'll try to find a matching {@link DataBound}-annotated fields.
 *
 * <p>
 * To create a method to be called after all the setter injections are complete, annotate a method
 * with {@link PostConstruct}.
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(RUNTIME)
@Target({FIELD})
@Documented
public @interface DataBound {

    enum Trim {
        NONE {
            @Override
            public Object apply(Object val) {
                return val;
            }
        }, TONULL {
            @Override
            public Object apply(Object val) {
                return StringUtils.trimToNull((String) val);
            }
        }, TOEMPTY {
            @Override
            public Object apply(Object val) {
                return StringUtils.trimToEmpty((String) val);
            }
        };

        public abstract Object apply(Object val);

    }

    /**
     * Define pre-processing of string-based values before they get injected and validated.
     */
    Trim trim() default Trim.NONE;
}
