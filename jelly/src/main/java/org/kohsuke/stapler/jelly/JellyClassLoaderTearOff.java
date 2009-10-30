package org.kohsuke.stapler.jelly;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.TagLibrary;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.Tag;
import org.apache.commons.jelly.impl.TagScript;
import org.apache.commons.jelly.impl.TagFactory;
import org.apache.commons.jelly.expression.ExpressionFactory;
import org.apache.commons.jelly.expression.Expression;
import org.apache.commons.jelly.expression.jexl.JexlExpressionFactory;
import org.kohsuke.stapler.MetaClassLoader;
import org.xml.sax.Attributes;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Map;

import com.google.common.collect.MapMaker;
import com.google.common.base.Function;

/**
 * {@link MetaClassLoader} tear-off for Jelly support.
 *
 * @author Kohsuke Kawaguchi
 */
public class JellyClassLoaderTearOff {
    private final MetaClassLoader owner;

    /**
     * See {@link JellyClassTearOff#scripts} for why we use {@link WeakReference} here.
     */
    private volatile WeakReference<Map<String,TagLibrary>> taglibs;

    public static ExpressionFactory EXPRESSION_FACTORY = new JexlExpressionFactory();

    public JellyClassLoaderTearOff(MetaClassLoader owner) {
        this.owner = owner;
    }

    public TagLibrary getTagLibrary(String nsUri) {
        Map<String,TagLibrary> m=null;
        if(taglibs!=null)
            m = taglibs.get();
        if(m==null) {
            m = new MapMaker().makeComputingMap(new Function<String,TagLibrary>() {
                public TagLibrary apply(String nsUri) {
                    if(owner.parent!=null) {
                        // parent first
                        TagLibrary tl = owner.parent.loadTearOff(JellyClassLoaderTearOff.class).getTagLibrary(nsUri);
                        if(tl!=null)    return tl;
                    }

                    String taglibBasePath = trimHeadSlash(nsUri);
                    URL res = owner.loader.getResource(taglibBasePath +"/taglib");
                    if(res!=null)
                        return new CustomTagLibrary(createContext(),owner.loader,nsUri,taglibBasePath);

                    return NO_SUCH_TAGLIBRARY;    // "not found" is also cached.
                }
            });
            taglibs = new WeakReference<Map<String,TagLibrary>>(m);
        }

        TagLibrary tl = m.get(nsUri);
        if (tl==NO_SUCH_TAGLIBRARY)     return null;
        return tl;
    }

    private String trimHeadSlash(String nsUri) {
        if(nsUri.startsWith("/"))
            return nsUri.substring(1);
        else
            return nsUri;
    }

    /**
     * Creates {@link JellyContext} for compiling view scripts
     * for classes in this classloader.
     */
    public JellyContext createContext() {
        JellyContext context = new CustomJellyContext(ROOT_CONTEXT);
        context.setClassLoader(owner.loader);
        context.setExportLibraries(false);
        return context;
    }

    /**
     * Used as the root context for compiling scripts.
     */
    private static final JellyContext ROOT_CONTEXT = new CustomJellyContext();

    static {
        ROOT_CONTEXT.registerTagLibrary("jelly:stapler",new StaplerTagLibrary());
    }

    /**
     * Place holder in the cache to indicate "no such taglib"
     */
    private static final TagLibrary NO_SUCH_TAGLIBRARY = new TagLibrary() {};

}
