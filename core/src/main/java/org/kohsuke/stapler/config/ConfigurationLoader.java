package org.kohsuke.stapler.config;

import com.google.common.base.Function;
import org.apache.commons.beanutils.ConvertUtils;

import java.beans.Introspector;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Comparator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import static org.kohsuke.stapler.config.Configuration.UNSPECIFIED;

/**
 * Provides a type-safe access to the configuration of the application.
 *
 * <p>
 * Often web applications need to load configuration from outside. T
 *
 * <p>
 * Typical usage would be {@code MyConfig config=ConfigurationLoad.from(...).as(MyConfig.class)}
 * where the <tt>MyConfig</tt> interface defines a bunch of methods named after
 * the property name:
 *
 * <pre>
 * interface MyConfig {
 *     File rootDir();
 *     int retryCount();
 *     String domainName();
 *     ...
 * }
 * </pre>
 *
 * <p>
 * Method calls translate to respective property lookup. For example, {@code config.rootDir()} would
 * be equivalent to {@code new File(properties.get("rootDir"))}.
 *
 * <p>
 * The method name can include common prefixes, such as "get", "is", and "has", and those portions
 * will be excluded from the property name. Thus the {@code rootDir()} could have been named {@code getRootDir()}.
 *
 *
 * @author Kohsuke Kawaguchi
 */
public class ConfigurationLoader {
    private final Function<String,String> source;

    /**
     * The caller should use one of the fromXyz methods.
     */
    private ConfigurationLoader(Function<String,String> source) {
        this.source = source;
    }

    private static Properties load(File f) throws IOException {
        Properties config = new Properties();
        FileInputStream in = new FileInputStream(f);
        try {
            config.load(in);
            return config;
        } finally {
            in.close();
        }
    }

    /**
     * Loads the configuration from the specified property file.
     */
    public static ConfigurationLoader from(File configPropertyFile) throws IOException {
        return from(load(configPropertyFile));
    }

    /**
     * Loads the configuration from the specified {@link Properties} object.
     */
    public static ConfigurationLoader from(final Properties props) throws IOException {
        return new ConfigurationLoader(new Function<String, String>() {
            public String apply(String from) {
                return props.getProperty(from);
            }
        });
    }

    public static ConfigurationLoader from(final Map<String,String> props) throws IOException {
        return new ConfigurationLoader(new Function<String, String>() {
            public String apply(String from) {
                return props.get(from);
            }
        });
    }

    /**
     * Creates {@link ConfigurationLoader} that uses all the system properties as the source.
     */
    public static ConfigurationLoader fromSystemProperties() throws IOException {
        return from(System.getProperties());
    }

    /**
     * Creates {@link ConfigurationLoader} that uses environment variables as the source.
     *
     * Since environment variables are often by convention all caps, while system properties
     * and other properties tend to be camel cased, this method creates a case-insensitive configuration
     * (that allows retrievals by both "path" and "PATH" to fill this gap.
     */
    public static ConfigurationLoader fromEnvironmentVariables() throws IOException {
        TreeMap<String, String> m = new TreeMap<String, String>(new Comparator<String>() {
            public int compare(String o1, String o2) {
                return o1.compareToIgnoreCase(o2);
            }
        });
        m.putAll(System.getenv());
        return from(m);
    }

    /**
     * Creates a type-safe proxy that reads from the source specified by one of the fromXyz methods.
     */
    public <T> T as(Class<T> type) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class[]{type}, new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (method.getDeclaringClass() == Object.class)
                    return method.invoke(this, args);

                Configuration cn = method.getAnnotation(Configuration.class);

                Class<?> r = method.getReturnType();
                String key = getKey(method,cn);

                String v = source.apply(key);
                if (v==null && cn!=null && !cn.defaultValue().equals(UNSPECIFIED))
                    v = cn.defaultValue();

                if (v==null)    return null;    // TODO: check how the primitive types are handled here

                return ConvertUtils.convert(v,r);
            }

            private String getKey(Method method, Configuration c) {
                if (c!=null && !c.name().equals(UNSPECIFIED))
                    return c.name();        // name override

                String n = method.getName();
                for (String p : GETTER_PREFIX) {
                    if (n.startsWith(p))
                        return Introspector.decapitalize(n.substring(p.length()));
                }
                return n;
            }
        }));
    }

    private static final String[] GETTER_PREFIX = {"get","has","is"};
}
