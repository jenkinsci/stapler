package org.kohsuke.stapler.jelly;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.TagLibrary;
import org.apache.commons.jelly.expression.ExpressionFactory;
import org.apache.commons.jelly.expression.jexl.JexlExpressionFactory;
import org.apache.commons.jelly.parser.XMLParser;
import org.kohsuke.stapler.MetaClassLoader;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

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

    public synchronized TagLibrary getTagLibrary(String nsUri) {
        TagLibrary tl=null;

        if(owner.parent!=null)        // parent first
            tl = owner.parent.loadTearOff(JellyClassLoaderTearOff.class).getTagLibrary(nsUri);

        if(tl!=null)
            return tl;

        Map<String,TagLibrary> m=null;
        if(taglibs!=null)
            m = taglibs.get();
        if(m==null) {
            m = new HashMap<String, TagLibrary>();
            taglibs = new WeakReference<Map<String,TagLibrary>>(m);
        }

        // then see if it's cached
        tl = m.get(nsUri);

        if(tl==null) { // can we load them here?
            String taglibBasePath = trimHeadSlash(nsUri);
            URL res = owner.loader.getResource(taglibBasePath +"/taglib");
            if(res!=null) {
                tl = new CustomTagLibrary(createContext(),owner.loader,taglibBasePath);
                m.put(nsUri,tl);
            }
        }

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
        JellyContext context = new CustomJellyContext(ROOT_CONTEXT) {
            @Override
            public TagLibrary getTagLibrary(String namespaceURI) {
                TagLibrary tl = super.getTagLibrary(namespaceURI);
                // attempt to resolve nsUri from taglibs
                if(tl==null) {
                    tl = JellyClassLoaderTearOff.this.getTagLibrary(namespaceURI);
                    if(tl!=null)
                        registerTagLibrary(namespaceURI,tl);
                }
                return tl;
            }
        };
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

}
