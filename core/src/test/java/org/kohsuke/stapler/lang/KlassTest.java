package org.kohsuke.stapler.lang;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Contains tests for {@link Klass}.
 * @author Oleg Nenashev.
 */
public class KlassTest {

    @Test
    public void shouldProperlyAccessJavaDeclaredFields() throws Exception {
        final Klass<Class> classInstance = Klass.java(FooClass.class);
        final List<FieldRef> declaredFields = classInstance.getDeclaredFields();
        for (FieldRef ref : declaredFields) {
            if ("fooField".equals(ref.getName())) {
                //TODO: check field parameters once Stapler provides such info
                return;
            }
        }
        Assert.fail("Have not found 'fooField' in the returned field list");
    }

    private static final class FooClass {
        private int fooField;
    }
}
