package org.kohsuke.stapler.jelly.jruby;

import org.apache.commons.io.IOUtils;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.Script;
import org.jruby.RubyClass;
import org.jruby.RubyObject;
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

    private volatile ScriptingContainer jruby;
    private RubyClass scriptImpl;

    public JRubyFacet() {
    }

    public Script parse(URL script) throws IOException {
        if (jruby==null) {
            // lazily create interpreter, in the hope that by the time we have this executed
            // the app has set the proper classloader. This isn't really reliable, but then
            // I suspect JRuby interpreter starts behaving funny if we setClassLoader in the middle of the operation.
            synchronized (this) {
                if (jruby==null) {
                    this.jruby = new ScriptingContainer();
                    jruby.setClassLoader(WebApp.getCurrent().getClassLoader());
                    scriptImpl = (RubyClass)jruby.runScriptlet(
                            "require 'org/kohsuke/stapler/jelly/jruby/JRubyJellyScriptImpl'\n" +
                                    "JRubyJellyScriptImpl::JRubyJellyERbScript");
                }
            }
        }

        try {
            String template = IOUtils.toString(script.openStream(), "UTF-8");
            Object o = jruby.callMethod(scriptImpl, "new", template);
            return (Script) o;
        } catch (Exception e) {
            throw (IOException)new IOException().initCause(e);
        }
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
                    if(next==null)  return false;

                    JRubyClassInfo info = getClassInfo(((RubyObject) node).getMetaClass());

                    return invokeScript(req, rsp, node, next, info.findScript(next + ".erb"));
                }
            });
        }
        dispatchers.add(new ScriptInvokingDispatcher() {
            final JRubyClassTearOff tearOff = owner.loadTearOff(JRubyClassTearOff.class);

            public boolean dispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException {
                // check ERB view
                String next = req.tokens.peek();
                if(next==null)  return false;

                return invokeScript(req, rsp, node, next, tearOff.findScript(next + ".erb"));
            }
        });
    }

    protected abstract class ScriptInvokingDispatcher extends Dispatcher {
        protected boolean invokeScript(RequestImpl req, ResponseImpl rsp, Object node, String next, Script script) throws IOException, ServletException {
            try {
                if(script ==null)        return false;   // no ERB script found

                req.tokens.next();

                if(traceable())
                    trace(req,rsp,"Invoking "+next+".erb"+" on "+node+" for "+req.tokens);

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
            return "TOKEN.erb for url=/TOKEN/...";
        }
    }

    public Class<JRubyClassTearOff> getClassTearOffType() {
        return JRubyClassTearOff.class;
    }

    public RequestDispatcher createRequestDispatcher(RequestImpl request, Class type, Object it, String viewName) throws IOException {
        TearOffSupport mc = request.stapler.getWebApp().getMetaClass(type);
        return mc.loadTearOff(JRubyClassTearOff.class).createDispatcher(it,viewName);
    }

    public boolean handleIndexRequest(RequestImpl req, ResponseImpl rsp, Object node, MetaClass nodeMetaClass) throws IOException, ServletException {
        if (node instanceof RubyObject) {
            JRubyClassInfo info = getClassInfo(((RubyObject) node).getMetaClass());
            Script script = info.findScript("index.erb");
            if (script!=null) {
                try {
                    WebApp.getCurrent().getFacet(JellyFacet.class).scriptInvoker.invokeScript(req, rsp, script, node);
                    return true;
                } catch (JellyTagException e) {
                    throw new ServletException(e);
                }
            }
        }
        return nodeMetaClass.loadTearOff(JRubyClassTearOff.class).serveIndexErb(req, rsp, node);
    }
}

