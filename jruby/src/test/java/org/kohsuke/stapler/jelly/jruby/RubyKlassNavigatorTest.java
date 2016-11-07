package org.kohsuke.stapler.jelly.jruby;

import org.jruby.CompatVersion;
import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.RubyModule;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.ScriptingContainer;
import org.junit.Assert;
import org.junit.Test;
import org.kohsuke.stapler.lang.FieldRef;
import org.kohsuke.stapler.lang.Klass;
import org.jvnet.hudson.test.Issue;

import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import org.hamcrest.collection.IsEmptyCollection;
import org.jruby.RubyFixnum;
import org.jruby.RubyInteger;
import org.jruby.internal.runtime.methods.CallConfiguration;
import org.jruby.internal.runtime.methods.DynamicMethod;
import org.jruby.runtime.Block;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.Visibility;
import org.jruby.runtime.builtin.IRubyObject;
import static org.junit.Assume.assumeThat;
import org.kohsuke.stapler.Function;
import org.kohsuke.stapler.lang.MethodRef;

/**
 * Tests handling of Ruby classes and modules with {@link RubyKlassNavigator}.
 * @author Oleg Nenashev.
 */
public class RubyKlassNavigatorTest {

    /**
     * Verifies that declared method retrieval works well.
     * Effective use-case - Ruby Runtime Plugin for Jenkins.
     * @throws Exception Test failure
     */
    @Test
    public void shouldProperlyHandleDeclaredMethodsInRubyModules() throws Exception {
        final ScriptingContainer ruby = createRubyInstance();
        final RubyKlassNavigator navigator = new RubyKlassNavigator(ruby.getProvider().getRuntime(), ClassLoader.getSystemClassLoader());
        final MyRubyModule myModule = new MyRubyModule(ruby.getRuntime(), new MyRubyClass(ruby.getRuntime()), true);


        final Klass<RubyModule> classInstance = new Klass<RubyModule>(myModule, navigator);
        
        final List<MethodRef> declaredMethods = classInstance.getDeclaredMethods();
        for (MethodRef ref : declaredMethods) {
            if (ref instanceof RubyMethodRef) {
                // Ruby engine API allows creating methods with null names, not our bug BTW...
                if ("doDynamic".equals(ref.getName())) {
                    //TODO: More consistency checks
                    return;
                }
            }
        }
        Assert.fail("Have not found 'doDynamic' in the returned function list");
    }
    
    /**
     * Verifies that field retrieval do not fail horribly for {@link RubyModule}.
     * Effective use-case - Ruby Runtime Plugin for Jenkins.
     * @throws Exception Test failure
     */
    @Test
    @Issue("JENKINS-39414")
    public void shouldProperlyHandleFieldsInRubyModules() throws Exception {
        final ScriptingContainer ruby = createRubyInstance();
        final RubyKlassNavigator navigator = new RubyKlassNavigator(ruby.getProvider().getRuntime(), ClassLoader.getSystemClassLoader());
        final MyRubyModule myModule = new MyRubyModule(ruby.getRuntime(), new MyRubyClass(ruby.getRuntime()), true);


        final Klass<RubyModule> classInstance = new Klass<RubyModule>(myModule, navigator);
        final List<FieldRef> declaredFields = classInstance.getDeclaredFields();

        assumeThat("Access to fields in Ruby Modules has not been implemented yet",
                declaredFields, not(IsEmptyCollection.<FieldRef>empty()));
        for (FieldRef ref : declaredFields) {
            if ("fooField".equals(ref.getName())) {
                //TODO: check fields once implemented in Stapler
                return;
            }
        }
        Assert.fail("Have not found 'fooField' in the returned field list");
    }

    //TODO: fix the test when Ruby routing gets implemented (https://github.com/stapler/stapler/issues/87)
    /**
     * Verifies that function retrieval do not fail horribly for {@link RubyModule}.
     * Effective use-case - Ruby Runtime Plugin for Jenkins.
     * @throws Exception Test failure
     */
    @Test
    @Issue("JENKINS-39414")
    public void shouldProperlyHandleFunctionsInRubyModules() throws Exception {
        final ScriptingContainer ruby = createRubyInstance();
        final RubyKlassNavigator navigator = new RubyKlassNavigator(ruby.getProvider().getRuntime(), ClassLoader.getSystemClassLoader());
        final MyRubyModule myModule = new MyRubyModule(ruby.getRuntime(), new MyRubyClass(ruby.getRuntime()), true);


        final Klass<RubyModule> classInstance = new Klass<RubyModule>(myModule, navigator);
        
        final List<Function> declaredFunctions = classInstance.getFunctions();
        for (Function ref : declaredFunctions) {
            if ("doDynamic".equals(ref.getName())) {
                //TODO: check fields once implemented in Stapler
                return;
            }
        }
        
        assumeThat("Routing of declared routable methods is not fully implemented (See https://github.com/stapler/stapler/issues/87)", 
                ruby, not(anything("Nothing to do in this code")));
        Assert.fail("Have not found 'doDynamic' in the returned function list");
    }
    
    private static final class MyRubyModule extends RubyModule {
                
        private int fooField = 123;
        
        MyRubyModule(Ruby runtime, RubyClass metaClass, boolean objectSpace) {
            super(runtime, metaClass, objectSpace);
            
            // Register a Mock method for the class
            getMethodsForWrite().put("doDynamic", new DynamicMethod(this, Visibility.PUBLIC, CallConfiguration.FrameFullScopeFull, "doDynamic") {
                @Override
                public IRubyObject call(ThreadContext context, IRubyObject self, RubyModule clazz, String name, IRubyObject[] args, Block block) {
                    return doDynamic("foo");
                }
                
                @Override
                public DynamicMethod dup() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            });
        }
        
        public final IRubyObject doDynamic(String token) {
            // Just return a routable object
            return new RubyFixnum(getRuntime(), 0);
        }
    }

    private static final class MyRubyClass extends RubyClass {     
        MyRubyClass(Ruby runtime) {
            super(runtime);
        }   
    }

    private ScriptingContainer createRubyInstance() {
        // Similar to Ruby Runtime Plugin, but with non-Jenkins classloader
        final ScriptingContainer ruby = new ScriptingContainer(LocalContextScope.SINGLETHREAD);
        ruby.setCompatVersion(CompatVersion.RUBY1_9);
        ruby.setClassLoader(ClassLoader.getSystemClassLoader());
        return ruby;
    }
}
