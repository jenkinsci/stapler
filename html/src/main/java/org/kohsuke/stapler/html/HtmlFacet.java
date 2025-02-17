package org.kohsuke.stapler.html;

import static org.kohsuke.stapler.Facet.LOGGER;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.Script;
import org.kohsuke.MetaInfServices;
import org.kohsuke.stapler.AbstractTearOff;
import org.kohsuke.stapler.Dispatcher;
import org.kohsuke.stapler.Facet;
import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.RequestImpl;
import org.kohsuke.stapler.ResponseImpl;
import org.kohsuke.stapler.jelly.JellyClassTearOff;
import org.kohsuke.stapler.jelly.JellyCompatibleFacet;
import org.kohsuke.stapler.jelly.JellyFacet;
import org.kohsuke.stapler.lang.Klass;

@MetaInfServices(Facet.class)
public final class HtmlFacet extends Facet implements JellyCompatibleFacet {

    private static final Logger LOGGER = Logger.getLogger(HtmlFacet.class.getName());

    // TODO seems like there could be some default methods in JellyCompatibleFacet to avoid boilerplate

    @Override
    public void buildViewDispatchers(MetaClass owner, List<Dispatcher> dispatchers) {
        dispatchers.add(createValidatingDispatcher(
                owner.loadTearOff(HtmlClassTearOff.class), owner.webApp.getFacet(JellyFacet.class).scriptInvoker));
    }

    @Override
    public RequestDispatcher createRequestDispatcher(RequestImpl request, Klass<?> type, Object it, String viewName)
            throws IOException {
        // TODO is this actually used?
        return createRequestDispatcher(
                request.getWebApp().getMetaClass(type).loadTearOff(HtmlClassTearOff.class),
                request.getWebApp().getFacet(JellyFacet.class).scriptInvoker,
                it,
                viewName);
    }

    @Override
    public void buildIndexDispatchers(MetaClass owner, List<Dispatcher> dispatchers) {
        try {
            if (owner.loadTearOff(JellyClassTearOff.class).findScript("index") != null) {
                super.buildIndexDispatchers(owner, dispatchers);
            }
        } catch (JellyException e) {
            LOGGER.log(Level.WARNING, "Failed to parse index.xhtml for " + owner, e);
        }
    }

    @Override
    public boolean handleIndexRequest(RequestImpl req, ResponseImpl rsp, Object node, MetaClass nodeMetaClass)
            throws IOException, ServletException {
        return handleIndexRequest(
                nodeMetaClass.loadTearOff(HtmlClassTearOff.class),
                req.getWebApp().getFacet(JellyFacet.class).scriptInvoker,
                req,
                rsp,
                node);
    }

    @Override
    public Collection<? extends Class<? extends AbstractTearOff<?, ? extends Script, ?>>> getClassTearOffTypes() {
        return Set.of(HtmlClassTearOff.class);
    }

    @Override
    public Collection<String> getScriptExtensions() {
        // TODO allow *.html if it can be parsed without external deps
        return Set.of(".xhtml");
    }
}
