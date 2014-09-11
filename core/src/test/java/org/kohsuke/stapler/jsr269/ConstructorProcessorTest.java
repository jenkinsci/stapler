package org.kohsuke.stapler.jsr269;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
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

    @Test public void privateConstructor() {
        Compilation compilation = new Compilation();
        compilation.addSource("some.pkg.Stuff").
                addLine("package some.pkg;").
                addLine("import org.kohsuke.stapler.DataBoundConstructor;").
                addLine("public class Stuff {").
                addLine("  @DataBoundConstructor Stuff() {}").
                addLine("}");
        compilation.doCompile(null, "-source", "6");
        List<Diagnostic<? extends JavaFileObject>> diagnostics = Utils.filterSupportedSourceVersionWarnings(compilation.getDiagnostics());
        assertEquals(1, diagnostics.size());
        String msg = diagnostics.get(0).getMessage(Locale.ENGLISH);
        assertTrue(msg, msg.contains("public"));
    }

    @Test public void abstractClass() {
        Compilation compilation = new Compilation();
        compilation.addSource("some.pkg.Stuff").
                addLine("package some.pkg;").
                addLine("import org.kohsuke.stapler.DataBoundConstructor;").
                addLine("public abstract class Stuff {").
                addLine("  @DataBoundConstructor public Stuff() {}").
                addLine("}");
        compilation.doCompile(null, "-source", "6");
        List<Diagnostic<? extends JavaFileObject>> diagnostics = Utils.filterSupportedSourceVersionWarnings(compilation.getDiagnostics());
        assertEquals(1, diagnostics.size());
        String msg = diagnostics.get(0).getMessage(Locale.ENGLISH);
        assertTrue(msg, msg.contains("abstract"));
    }

    // TODO nested classes use qualified rather than binary name
    // TODO behavior when multiple @DataBoundConstructor's specified on a single class - error?

}
