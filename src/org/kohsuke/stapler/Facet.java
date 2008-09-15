package org.kohsuke.stapler;

import org.apache.commons.discovery.ResourceClassIterator;
import org.apache.commons.discovery.resource.ClassLoaders;
import org.apache.commons.discovery.resource.classes.DiscoverClasses;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Aspect of stapler that brings in an optional language binding.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class Facet {
    /**
     * Adds {@link Dispatcher}s that look at one token and binds that
     * to the views associated with the 'it' object.
     */
    public abstract void buildViewDispatchers(MetaClass owner, List<Dispatcher> dispatchers);

    /**
     * Discovers all the facets in the classloader.
     */
    public static List<Facet> discover(ClassLoader cl) {
        List<Facet> r = new ArrayList<Facet>();

        ClassLoaders classLoaders = new ClassLoaders();
        classLoaders.put(cl);
        DiscoverClasses dc = new DiscoverClasses(classLoaders);
        ResourceClassIterator itr = dc.findResourceClasses("/META-INF/services/" + Facet.class.getName());
        while(itr.hasNext()) {
            Class c = itr.nextResourceClass().loadClass();
            try {
                r.add((Facet)c.newInstance());
            } catch (InstantiationException e) {
                LOGGER.log(Level.WARNING, "Failed to instanticate "+c,e);
            } catch (IllegalAccessException e) {
                LOGGER.log(Level.WARNING, "Failed to instanticate "+c,e);
            }
        }
        return r;

    }

    public static final Logger LOGGER = Logger.getLogger(Facet.class.getName());

    public abstract RequestDispatcher createRequestDispatcher(RequestImpl request, Object it, String viewName) throws IOException;

    /**
     * Attempts to route the HTTP request to the 'index' page of the 'it' object.
     *
     * @return
     *      true if the processing succeeds. Otherwise false.
     */
    public abstract boolean handleIndexRequest(RequestImpl req, ResponseImpl rsp, Object node, MetaClass nodeMetaClass) throws IOException, ServletException;
}
