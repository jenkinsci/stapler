package org.kohsuke.stapler;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * Type-safe attribute accessor.
 *
 * <p>
 * Servlet API has a bag of stuff in several scopes (such as request, session, ...)
 * but the API is not type-safe.
 *
 * This object provides a convenient type-safe access to to such bags, as well
 * as providing uniform API regardless of the actual scope.
 *
 * <p>
 * Each instance of {@link AttributeKey} gets an unique attribute name, which means
 * in the most typical case, these instances should be used as a singleton.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class AttributeKey<T> {
    protected final String name;

    public AttributeKey() {
        name = UUID.randomUUID().toString();
    }

    public AttributeKey(String name) {
        this.name = name;
    }

    public abstract T get(HttpServletRequest req);

    public abstract void set(HttpServletRequest req, T value);

    public abstract void remove(HttpServletRequest req);

    public final T get() {
        return get(Stapler.getCurrentRequest());
    }

    public final void set(T value) {
        set(Stapler.getCurrentRequest(),value);
    }

    public final void remove() {
        remove(Stapler.getCurrentRequest());
    }

    /**
     * Creates a new request-scoped {@link AttributeKey}.
     */
    public static <T> AttributeKey<T> requestScoped() {
        return new AttributeKey<T>() {
            public T get(HttpServletRequest req) {
                return (T)req.getAttribute(name);
            }

            public void set(HttpServletRequest req, T value) {
                req.setAttribute(name, value);
            }

            public void remove(HttpServletRequest req) {
                req.removeAttribute(name);
            }
        };
    }

    /**
     * Creates a new session-scoped {@link AttributeKey}.
     */
    public static <T> AttributeKey<T> sessionScoped() {
        return new AttributeKey<T>() {
            public T get(HttpServletRequest req) {
                HttpSession s = req.getSession(false);
                if (s==null)    return null;
                return (T)s.getAttribute(name);
            }

            public void set(HttpServletRequest req, T value) {
                req.getSession().setAttribute(name, value);
            }

            public void remove(HttpServletRequest req) {
                HttpSession s = req.getSession(false);
                if (s!=null)
                    s.removeAttribute(name);
            }
        };
    }

    /**
     * Creates a new {@link ServletContext}-scoped {@link AttributeKey}.
     */
    public static <T> AttributeKey<T> appScoped() {
        return new AttributeKey<T>() {
            public T get(HttpServletRequest req) {
                return (T) getContext(req).getAttribute(name);
            }

            public void set(HttpServletRequest req, T value) {
                getContext(req).setAttribute(name, value);
            }

            public void remove(HttpServletRequest req) {
                getContext(req).removeAttribute(name);
            }

            private ServletContext getContext(HttpServletRequest req) {
                return ((StaplerRequest)req).getServletContext();
            }
        };
    }
}
