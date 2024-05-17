package org.kohsuke.stapler;

import junit.framework.TestCase;
import org.junit.Assert;
import org.kohsuke.stapler.lang.Klass;

public class MetaClassTest extends TestCase {
    public void testGetObjectProhibited() {
        MetaClass metaClass = new MetaClass(new WebApp(new MockServletContext()), Klass.java(Object.class));

        // ensure no getClass dispatcher for Object
        Assert.assertFalse(metaClass.dispatchers.stream()
                .filter(d -> d instanceof NameBasedDispatcher)
                .anyMatch(d -> ((NameBasedDispatcher) d).name.equals("class")));

        // in fact, there should be no name based dispatchers at all
        Assert.assertFalse(metaClass.dispatchers.stream().anyMatch(d -> d instanceof NameBasedDispatcher));
    }
}
