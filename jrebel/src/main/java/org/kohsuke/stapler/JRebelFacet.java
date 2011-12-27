package org.kohsuke.stapler;

import org.kohsuke.MetaInfServices;
import org.kohsuke.stapler.export.ModelBuilder;
import org.kohsuke.stapler.lang.Klass;
import org.zeroturnaround.javarebel.ClassEventListener;
import org.zeroturnaround.javarebel.ReloaderFactory;

import javax.servlet.RequestDispatcher;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import static java.util.logging.Level.*;

/**
 * Adds JRebel reloading support.
 *
 * @author Kohsuke Kawaguchi
 */
@MetaInfServices
public class JRebelFacet extends Facet {
    private final Map<Class,MetaClass> metaClasses = new HashMap<Class, MetaClass>();

    public class ReloaderHook implements Runnable {
        public void run() {
            ReloaderFactory.getInstance().addClassReloadListener(new ClassEventListener() {
                public void onClassEvent(int eventType, Class klass) {
                    synchronized (metaClasses) {
                        for (Entry<Class, MetaClass> e : metaClasses.entrySet()) {
                            if (klass.isAssignableFrom(e.getKey())) {
                                LOGGER.fine("Reloaded Stapler MetaClass for "+e.getKey());
                                e.getValue().buildDispatchers();
                            }
                        }
                    }
                    // purge the model builder cache
                    ResponseImpl.MODEL_BUILDER = new ModelBuilder();
                }

                public int priority() {
                    return PRIORITY_DEFAULT;
                }
            });
        }
    }

    public JRebelFacet() {
        try {
            Runnable r = (Runnable)Class.forName(JRebelFacet.class.getName()+"$ReloaderHook")
                    .getConstructor(JRebelFacet.class).newInstance(this);
            r.run();
        } catch (Throwable e) {
            LOGGER.log(FINE, "JRebel support failed to load", e);
        }
    }


    @Override
    public void buildViewDispatchers(MetaClass owner, List<Dispatcher> dispatchers) {
        if (owner.klass.clazz instanceof Class) {
            synchronized (metaClasses) {
                metaClasses.put((Class)owner.klass.clazz,owner);
            }
        }
    }

    @Override
    public RequestDispatcher createRequestDispatcher(RequestImpl request, Klass<?> type, Object it, String viewName) {
        return null;
    }

    @Override
    public boolean handleIndexRequest(RequestImpl req, ResponseImpl rsp, Object node, MetaClass nodeMetaClass) {
        return false;
    }

    private static final Logger LOGGER = Logger.getLogger(JRebelFacet.class.getName());
}
