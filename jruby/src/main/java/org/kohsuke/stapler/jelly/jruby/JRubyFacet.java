package org.kohsuke.stapler.jelly.jruby;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.Script;
import org.jruby.RubyClass;
import org.jruby.RubyModule;
import org.jruby.RubyObject;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.ScriptingContainer;
import org.kohsuke.MetaInfServices;
import org.kohsuke.stapler.Dispatcher;
import org.kohsuke.stapler.Facet;
import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.RequestImpl;
import org.kohsuke.stapler.ResponseImpl;
import org.kohsuke.stapler.TearOffSupport;
import org.kohsuke.stapler.WebApp;
import org.kohsuke.stapler.jelly.JellyCompatibleFacet;
import org.kohsuke.stapler.jelly.JellyFacet;
import org.kohsuke.stapler.jelly.jruby.erb.ERbClassTearOff;
import org.kohsuke.stapler.lang.Klass;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

/**
 * {@link Facet} that adds Ruby-based view technologies.
 *
 * @author Kohsuke Kawaguchi
 * @author Hiroshi Nakamura
 */
@MetaInfServices(Facet.class)
public class JRubyFacet extends Facet implements JellyCompatibleFacet {
    /*package*/ final List<RubyTemplateLanguage> languages = new CopyOnWriteArrayList<RubyTemplateLanguage>();

    /**
     * There are all kinds of downsides in doing this, but for the time being we just use one scripting container.
     */
    private final ScriptingContainer container;

    private final RubyKlassNavigator navigator;

    /**
     * {@link RubyTemplateContainer}s keyed by their {@linkplain RubyTemplateLanguage#getScriptExtension() extensions}.
     * (since {@link #container} is a singleton per {@link JRubyFacet}, this is also just one map.
     */
    private final Map<String,RubyTemplateContainer> templateContainers = new HashMap<String, RubyTemplateContainer>();

    private final Collection<Class<? extends AbstractRubyTearOff>> tearOffTypes = new CopyOnWriteArrayList<Class<? extends AbstractRubyTearOff>>();

    public JRubyFacet() {
        // TODO: is this too early? Shall we allow registrations later?
        languages.addAll(Facet.discoverExtensions(RubyTemplateLanguage.class,
                Thread.currentThread().getContextClassLoader(), getClass().getClassLoader()));

        container = new ScriptingContainer(LocalContextScope.SINGLETHREAD); // we don't want any funny multiplexing from ScriptingContainer.
        container.runScriptlet("require 'org/kohsuke/stapler/jelly/jruby/JRubyJellyScriptImpl'");

        navigator = new RubyKlassNavigator(container.getProvider().getRuntime(), getClass().getClassLoader());

        for (RubyTemplateLanguage l : languages) {
            templateContainers.put(l.getScriptExtension(),l.createContainer(container));
            tearOffTypes.add(l.getTearOffClass());
        }
    }

    private RubyTemplateContainer selectTemplateContainer(String path) {
        int idx = path.lastIndexOf('.');
        if (idx >= 0) {
            RubyTemplateContainer t = templateContainers.get(path.substring(idx));
            if (t!=null)    return t;
        }
        throw new IllegalArgumentException("Unrecognized file extension: "+path);
    }

    public Script parseScript(URL template) throws IOException {
        return selectTemplateContainer(template.getPath()).parseScript(template);
    }

    @Override
    public Klass<RubyModule> getKlass(Object o) {
        if (o instanceof RubyObject)
            return makeKlass(((RubyObject) o).getMetaClass());
        return null;
    }

    private Klass<RubyModule> makeKlass(RubyModule o) {
        return new Klass<RubyModule>(o,navigator);
    }

    public synchronized MetaClass getClassInfo(RubyClass r) {
        return WebApp.getCurrent().getMetaClass(makeKlass(r));
    }

    private boolean isRuby(MetaClass mc) {
        return mc.klass.clazz instanceof RubyModule;
    }

    public void buildViewDispatchers(final MetaClass owner, List<Dispatcher> dispatchers) {
        for (final Class<? extends AbstractRubyTearOff> t : getClassTearOffTypes()) {
            dispatchers.add(new ScriptInvokingDispatcher() {
                final AbstractRubyTearOff tearOff = owner.loadTearOff(t);
                @Override
                public boolean dispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException {
                    String next = req.tokens.peek();
                    if(next==null) return false;
                    return invokeScript(req, rsp, node, next, tearOff.findScript(next));
                }
            });
        }
    }

    @Override
    public void buildFallbackDispatchers(MetaClass owner, List<Dispatcher> dispatchers) {
        if (isRuby(owner)) {
            dispatchers.add(new RackDispatcher());
        }
    }

    public Collection<Class<? extends AbstractRubyTearOff>> getClassTearOffTypes() {
        return tearOffTypes;
    }

    public Collection<String> getScriptExtensions() {
        List<String> r = new ArrayList<String>();
        for (RubyTemplateLanguage l : languages)
            r.add(l.getScriptExtension());
        return r;
    }


    public RequestDispatcher createRequestDispatcher(RequestImpl request, Klass<?> type, Object it, String viewName) throws IOException {
        TearOffSupport mc = request.stapler.getWebApp().getMetaClass(type);
        return mc.loadTearOff(ERbClassTearOff.class).createDispatcher(it,viewName);
    }

    public boolean handleIndexRequest(RequestImpl req, ResponseImpl rsp, Object node, MetaClass mc) throws IOException, ServletException {
        for (Class<? extends AbstractRubyTearOff> t : getClassTearOffTypes()) {
            AbstractRubyTearOff rt = mc.loadTearOff(t);
            Script script = rt.findScript("index");
            if(script!=null) {
                try {
                    if(LOGGER.isLoggable(Level.FINE))
                        LOGGER.fine("Invoking index"+rt.getDefaultScriptExtension()+" on " + node);
                    WebApp.getCurrent().getFacet(JellyFacet.class).scriptInvoker.invokeScript(req, rsp, script, node);
                    return true;
                } catch (JellyTagException e) {
                    throw new ServletException(e);
                }
            }
        }

        return false;
    }

}

