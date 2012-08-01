package org.kohsuke.stapler.jsr269;

import java.util.Collections;
import net.java.dev.hickory.testing.Compilation;
import org.junit.Test;
import static org.junit.Assert.*;

public class ConstructorProcessorTest {

    @Test public void basicOutput() {
        Compilation compilation = new Compilation();
        compilation.addSource("some.pkg.Stuff").
                addLine("package some.pkg;").
                addLine("import org.kohsuke.stapler.DataBoundConstructor;").
                addLine("public class Stuff {").
                addLine("  @DataBoundConstructor public Stuff(int count, String name) {}").
                addLine("}");
        compilation.doCompile(null, "-source", "6");
        assertEquals(Collections.emptyList(), Utils.filterSupportedSourceVersionWarnings(compilation.getDiagnostics()));
        assertEquals("{constructor=count,name}", Utils.normalizeProperties(Utils.getGeneratedResource(compilation, "some/pkg/Stuff.stapler")));
    }

    @Test public void preAnnotationCompatibility() {
        Compilation compilation = new Compilation();
        compilation.addSource("some.pkg.Stuff").
                addLine("package some.pkg;").
                addLine("public class Stuff {").
                addLine("  /** @stapler-constructor */ public Stuff(String name, int count) {}").
                addLine("}");
        compilation.doCompile(null, "-source", "6");
        assertEquals(Collections.emptyList(), Utils.filterSupportedSourceVersionWarnings(compilation.getDiagnostics()));
        assertEquals("{constructor=name,count}", Utils.normalizeProperties(Utils.getGeneratedResource(compilation, "some/pkg/Stuff.stapler")));
    }

    @Test public void JENKINS_11739() {
        Compilation compilation = new Compilation();
        compilation.addSource("some.pkg.Stuff").
                addLine("package some.pkg;").
                addLine("import org.kohsuke.stapler.DataBoundConstructor;").
                addLine("public class Stuff {").
                addLine("  @DataBoundConstructor public Stuff(int count, String name) {}").
                addLine("}");
        compilation.addSource("some.pkg.package-info").
                addLine("package some.pkg;");
        compilation.doCompile(null, "-source", "6");
        assertEquals(Collections.emptyList(), Utils.filterSupportedSourceVersionWarnings(compilation.getDiagnostics()));
        assertEquals("{constructor=count,name}", Utils.normalizeProperties(Utils.getGeneratedResource(compilation, "some/pkg/Stuff.stapler")));
    }

    // XXX nested classes use qualified rather than binary name
    // XXX behavior when multiple @DataBoundConstructor's specified on a single class - error?

}
