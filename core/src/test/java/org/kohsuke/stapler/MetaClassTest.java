package org.kohsuke.stapler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kohsuke.stapler.lang.Klass;

class MetaClassTest {

    @Test
    void testGetObjectProhibited() {
        MetaClass metaClass = new MetaClass(new WebApp(new MockServletContext()), Klass.java(Object.class));

        // ensure no getClass dispatcher for Object
        Assertions.assertFalse(metaClass.dispatchers.stream()
                .filter(NameBasedDispatcher.class::isInstance)
                .anyMatch(d -> ((NameBasedDispatcher) d).name.equals("class")));

        // in fact, there should be no name based dispatchers at all
        Assertions.assertFalse(metaClass.dispatchers.stream().anyMatch(d -> d instanceof NameBasedDispatcher));
    }
}
