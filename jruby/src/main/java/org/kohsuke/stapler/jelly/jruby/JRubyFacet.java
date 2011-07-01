package org.kohsuke.stapler.jelly.jruby;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.Script;
import org.jruby.RubyClass;
import org.jruby.RubyObject;
import org.kohsuke.MetaInfServices;
import org.kohsuke.stapler.*;
import org.kohsuke.stapler.jelly.JellyCompatibleFacet;
import org.kohsuke.stapler.jelly.JellyFacet;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * @author Kohsuke Kawaguchi
 */
@MetaInfServices(Facet.class)
public class JRubyFacet extends Facet implements JellyCompatibleFacet {
    private final Map<RubyClass,JRubyClassInfo> classMap = new WeakHashMap<RubyClass,JRubyClassInfo>();

    private JRubyScriptProvider jruby = new JRubyScriptProvider();

    public JRubyFacet() {
    }

    public Script parse(URL template) throws IOException {
        return jruby.parseScript(template);
    }

    public synchronized JRubyClassInfo getClassInfo(RubyClass r) {
        if (r==null)    return null;

        JRubyClassInfo o = classMap.get(r);
        if (o==null)
            classMap.put(r,o=new JRubyClassInfo(this,r));
        return o;
    }

    public void buildViewDispatchers(final MetaClass owner, List<Dispatcher> dispatchers) {
        if (RubyObject.class.isAssignableFrom(owner.clazz)) {
            dispatchers.add(new ScriptInvokingDispatcher() {
                @Override
                public boolean dispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException, IllegalAccessException, InvocationTargetException {
                    String next = req.tokens.peek();
                    if(next==null) return false;
                    JRubyClassInfo info = getClassInfo(((RubyObject) node).getMetaClass());
                    Script script = findScript(next, info);
                    return invokeScript(req, rsp, node, next, script);
                }
            });
        }
        dispatchers.add(new ScriptInvokingDispatcher() {
            final JRubyERbClassTearOff tearOff = owner.loadTearOff(JRubyERbClassTearOff.class);
            @Override
            public boolean dispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException {
                String next = req.tokens.peek();
                if(next==null) return false;
                Script script = findScript(next, tearOff);
                return invokeScript(req, rsp, node, next, script);
            }
        });
    }

    protected abstract class ScriptInvokingDispatcher extends Dispatcher {
        protected boolean invokeScript(RequestImpl req, ResponseImpl rsp, Object node, String next, Script script) throws IOException, ServletException {
            try {
                if(script==null) return false;
                
                req.tokens.next();

                if(traceable())
                    trace(req,rsp,"Invoking "+next+" on "+node+" for "+req.tokens);

                WebApp.getCurrent().getFacet(JellyFacet.class).scriptInvoker.invokeScript(req, rsp, script, node);

                return true;
            } catch (RuntimeException e) {
                throw e;
            } catch (IOException e) {
                throw e;
            } catch (Exception e) {
                throw new ServletException(e);
            }
        }

        public String toString() {
            return "TOKEN for url=/TOKEN/...";
        }
    }

    public Class<JRubyERbClassTearOff> getClassTearOffType() {
        return JRubyERbClassTearOff.class;
    }

    public RequestDispatcher createRequestDispatcher(RequestImpl request, Class type, Object it, String viewName) throws IOException {
        TearOffSupport mc = request.stapler.getWebApp().getMetaClass(type);
        return mc.loadTearOff(JRubyERbClassTearOff.class).createDispatcher(it,viewName);
    }

    public boolean handleIndexRequest(RequestImpl req, ResponseImpl rsp, Object node, MetaClass nodeMetaClass) throws IOException, ServletException {
        if (node instanceof RubyObject) {
            JRubyClassInfo info = getClassInfo(((RubyObject) node).getMetaClass());
            Script script = findScript("index", info);
            if (script!=null) {
                try {
                    WebApp.getCurrent().getFacet(JellyFacet.class).scriptInvoker.invokeScript(req, rsp, script, node);
                    return true;
                } catch (JellyTagException e) {
                    throw new ServletException(e);
                }
            }
        }
        return nodeMetaClass.loadTearOff(JRubyERbClassTearOff.class).serveIndexErb(req, rsp, node);
    }

    private Script findScript(String next, CachingScriptLoader<Script, IOException> loader) throws IOException {
        for (String extension : jruby.getSupportedExtensions()) {
            Script script = loader.findScript(next + "." + extension);
            if (script!=null) return script;
        }
        return null;
    }
}

