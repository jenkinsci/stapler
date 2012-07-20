package org.kohsuke.stapler.jsr269;

import java.util.Collections;
import net.java.dev.hickory.testing.Compilation;
import org.junit.Test;
import static org.junit.Assert.*;

public class QueryParameterAnnotationProcessorTest {

    @Test public void basicOutput() {
        Compilation compilation = new Compilation();
        compilation.addSource("some.pkg.Stuff").
                addLine("package some.pkg;").
                addLine("import org.kohsuke.stapler.QueryParameter;").
                addLine("public class Stuff {").
                addLine("  public void doOneThing(@QueryParameter String key) {}").
                addLine("  public void doAnother(@QueryParameter(\"ignoredHere\") String name, @QueryParameter String address) {}").
                addLine("}");
        compilation.doCompile(null, "-source", "6");
        assertEquals(Collections.emptyList(), Utils.filterSupportedSourceVersionWarnings(compilation.getDiagnostics()));
        assertEquals("key", Utils.getGeneratedResource(compilation, "some/pkg/Stuff/doOneThing.stapler"));
        assertEquals("name,address", Utils.getGeneratedResource(compilation, "some/pkg/Stuff/doAnother.stapler"));
    }

    // XXX nested classes use qualified rather than binary name

}
