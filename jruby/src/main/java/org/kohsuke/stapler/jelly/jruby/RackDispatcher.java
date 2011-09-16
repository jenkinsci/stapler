package org.kohsuke.stapler.jelly.jruby;

import org.jruby.Ruby;
import org.jruby.RubyModule;
import org.jruby.RubyObject;
import org.jruby.internal.runtime.methods.DynamicMethod;
import org.jruby.rack.DefaultRackApplication;
import org.jruby.rack.servlet.ServletRackConfig;
import org.jruby.rack.servlet.ServletRackContext;
import org.jruby.rack.servlet.ServletRackEnvironment;
import org.jruby.rack.servlet.ServletRackResponseEnvironment;
import org.jruby.runtime.builtin.IRubyObject;
import org.kohsuke.stapler.Dispatcher;
import org.kohsuke.stapler.RequestImpl;
import org.kohsuke.stapler.ResponseImpl;
import org.kohsuke.stapler.Stapler;

import javax.servlet.ServletException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * {@link Dispatcher} that looks for the Rack-compliant call method.
 *
 * @author Kohsuke Kawaguchi
 */
public class RackDispatcher extends Dispatcher {
    @Override
    public boolean dispatch(final RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException, IllegalAccessException, InvocationTargetException {
        RubyObject x = (RubyObject) node;
        Ruby runtime = x.getRuntime();

        DynamicMethod m = x.getMetaClass().searchMethod("call");
        if (m==null) // does this instance respond to the 'call' method?
            return false;

        // TODO: does the context need to live longer?
        ServletRackContext rackContext = new ServletRackContext(new ServletRackConfig(req.getServletContext()));

        // we don't want the Rack app to consider the portion of the URL that was already consumed
        // to reach to the Rack app, so for PATH_INFO we use getRestOfPath(), not getPathInfo()
        ServletRackEnvironment env = new ServletRackEnvironment(req, rackContext) {
            @Override
            public String getPathInfo() {
                return req.getRestOfPath();
            }
        };
        // servletHandler = Rack::Handler::Servlet.new(node)
        runtime.getLoadService().require("rack/handler/servlet");
        IRubyObject servletHandler = ((RubyModule)runtime.getModule("Rack").getConstantAt("Handler")).getClass("Servlet").callMethod("new", x);

        DefaultRackApplication dra = new DefaultRackApplication();
        dra.setApplication(servletHandler);
        dra.call(env)
                .respond(new ServletRackResponseEnvironment(Stapler.getCurrentResponse()));

        return true;
    }

    public String toString() {
        return "call(env) to delegate to Rack-compatible Ruby objects";
    }
}
