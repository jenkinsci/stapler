package org.kohsuke.stapler.lang;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.kohsuke.stapler.Function;

/**
 * Contains tests for {@link Klass}.
 * @author Oleg Nenashev.
 */
public class KlassTest {

    @Test
    public void shouldProperlyAccessJavaDeclaredMethods() throws Exception {
        final Klass<Class> classInstance = Klass.java(FooClass.class);
        final List<MethodRef> declaredFunctions = classInstance.getDeclaredMethods();
        for (MethodRef ref : declaredFunctions) {
            if ("doDynamic".equals(ref.getName())) {
                // TODO: check field parameters once Stapler provides such info
                return;
            }
        }
        Assert.fail("Have not found the 'doDynamic' declared method for FooClass");
    }

    @Test
    public void shouldProperlyAccessJavaDeclaredFields() throws Exception {
        final Klass<Class> classInstance = Klass.java(FooClass.class);
        final List<FieldRef> declaredFields = classInstance.getDeclaredFields();
        for (FieldRef ref : declaredFields) {
            if ("fooField".equals(ref.getName())) {
                // TODO: check field parameters once Stapler provides such info
                return;
            }
        }
        Assert.fail("Have not found 'fooField' in the returned field list");
    }

    @Test
    public void shouldProperlyAccessJavaDeclaredFunctions() throws Exception {
        final Klass<Class> classInstance = Klass.java(FooClass.class);
        final List<Function> declaredFunctions = classInstance.getFunctions();
        for (Function ref : declaredFunctions) {
            if ("doDynamic".equals(ref.getName())) {
                // TODO: check field parameters once Stapler provides such info
                return;
            }
        }
        Assert.fail("Have not found 'doDynamic' function for FooClass");
    }

    private static final class FooClass {
        private int fooField;

        public Object doDynamic(String token) {
            // Just return something potentially routable
            return Integer.valueOf(0);
        }
    }
}
