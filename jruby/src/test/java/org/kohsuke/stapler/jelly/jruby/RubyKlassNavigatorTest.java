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
import static org.junit.Assume.assumeThat;

/**
 * Tests handling of Ruby classes and modules with {@link RubyKlassNavigator}.
 * @author Oleg Nenashev.
 */
public class RubyKlassNavigatorTest {

    /**
     * Verifies that field retrieval do not fail horribly for {@link RubyModule}.
     * Effective use-case - Ruby Runtime Plugin for Jenkins.
     * @throws Exception Test failure
     */
    @Test
    @Issue("JENKINS-39414")
    public void shouldProperlyHandleRubyModules() throws Exception {
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

    private static final class MyRubyModule extends RubyModule {

        private int fooField = 123;

        MyRubyModule(Ruby runtime, RubyClass metaClass, boolean objectSpace) {
            super(runtime, metaClass, objectSpace);
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
