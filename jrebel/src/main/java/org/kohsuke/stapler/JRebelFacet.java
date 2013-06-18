package org.kohsuke.stapler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.kohsuke.MetaInfServices;
import org.kohsuke.stapler.export.ModelBuilder;
import org.kohsuke.stapler.lang.Klass;

import javax.servlet.RequestDispatcher;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Adds JRebel reloading support.
 *
 * @author Kohsuke Kawaguchi
 */
@MetaInfServices
public class JRebelFacet extends Facet {
    private final Map<Class,MetaClass> metaClasses = new HashMap<Class, MetaClass>();

    public JRebelFacet() throws Exception {
        Class<?> _ReloaderFactory;
        try {
            _ReloaderFactory = Class.forName("org.zeroturnaround.javarebel.ReloaderFactory");
        } catch (ClassNotFoundException x) {
            LOGGER.fine("JavaRebel not present");
            return;
        }
        LOGGER.log(Level.FINE, "JavaRebel found at {0}", _ReloaderFactory.getProtectionDomain());
        Class<?> _Reloader = Class.forName("org.zeroturnaround.javarebel.Reloader");
        final Class<?> _ClassEventListener = Class.forName("org.zeroturnaround.javarebel.ClassEventListener");
        /*Reloader*/Object instance = _ReloaderFactory.getMethod("getInstance").invoke(null);
        /*ClassEventListener*/Object listener = Proxy.newProxyInstance(JRebelFacet.class.getClassLoader(), new Class<?>[] {_ClassEventListener}, new InvocationHandler() {
            @Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String name = method.getName();
                if (name.equals("onClassEvent")) {
                    Class<?> klass = (Class) args[1];
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
                    return null;
                } else if (name.equals("priority")) {
                    return _ClassEventListener.getField("PRIORITY_DEFAULT").get(null);
                } else {
                    throw new AssertionError(name);
                }
            }
        });
        _Reloader.getMethod("addClassReloadListener", _ClassEventListener).invoke(instance, listener);
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
