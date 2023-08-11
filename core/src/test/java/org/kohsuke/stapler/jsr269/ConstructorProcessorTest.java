package org.kohsuke.stapler.jsr269;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.karuslabs.elementary.Results;
import com.karuslabs.elementary.junit.JavacExtension;
import com.karuslabs.elementary.junit.annotations.Inline;
import com.karuslabs.elementary.junit.annotations.Options;
import com.karuslabs.elementary.junit.annotations.Processors;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(JavacExtension.class)
@Options("-Werror")
@Processors(ConstructorProcessor.class)
class ConstructorProcessorTest {

    @Inline(
            name = "some.pkg.Stuff",
            source = {
                "package some.pkg;",
                "import org.kohsuke.stapler.DataBoundConstructor;",
                "public class Stuff {",
                "  @DataBoundConstructor public Stuff(int count, String name) {}",
                "}",
            })
    @Test
    void basicOutput(Results results) {
        assertEquals(Collections.emptyList(), results.diagnostics);
        assertEquals("{constructor=count,name}", Utils.normalizeProperties(Utils.getGeneratedResource(results.sources, "some/pkg/Stuff.stapler")));
    }

    @Inline(
            name = "some.pkg.Stuff",
            source = {
                "package some.pkg;",
                "public class Stuff {",
                "  /** @stapler-constructor */ public Stuff(String name, int count) {}",
                "}"
            })
    @Test
    void preAnnotationCompatibility(Results results) {
        assertEquals(Collections.emptyList(), results.diagnostics);
        assertEquals("{constructor=name,count}", Utils.normalizeProperties(Utils.getGeneratedResource(results.sources, "some/pkg/Stuff.stapler")));
    }

    @Inline(
            name = "some.pkg.Stuff",
            source = {
                "package some.pkg;",
                "import org.kohsuke.stapler.DataBoundConstructor;",
                "public class Stuff {",
                "  @DataBoundConstructor public Stuff(int count, String name) {}",
                "}"
            })
    @Inline(
            name = "some.pkg.package-info",
            source = {"package some.pkg;"})
    @Test
    void JENKINS_11739(Results results) {
        assertEquals(Collections.emptyList(), results.diagnostics);
        assertEquals("{constructor=count,name}", Utils.normalizeProperties(Utils.getGeneratedResource(results.sources, "some/pkg/Stuff.stapler")));
    }

    @Inline(
            name = "some.pkg.Stuff",
            source = {
                "package some.pkg;",
                "import org.kohsuke.stapler.DataBoundConstructor;",
                "public class Stuff {",
                "  @DataBoundConstructor Stuff() {}",
                "}"
            })
    @Test
    void privateConstructor(Results results) {
        List<Diagnostic<? extends JavaFileObject>> diagnostics = results.diagnostics;
        assertEquals(1, diagnostics.size());
        String msg = diagnostics.get(0).getMessage(Locale.ENGLISH);
        assertTrue(msg.contains("public"), msg);
    }

    @Inline(
            name = "some.pkg.Stuff",
            source = {
                "package some.pkg;",
                "import org.kohsuke.stapler.DataBoundConstructor;",
                "public abstract class Stuff {",
                "  @DataBoundConstructor public Stuff() {}",
                "}"
            })
    @Test
    void abstractClass(Results results) {
        List<Diagnostic<? extends JavaFileObject>> diagnostics = results.diagnostics;
        assertEquals(1, diagnostics.size());
        String msg = diagnostics.get(0).getMessage(Locale.ENGLISH);
        assertTrue(msg.contains("abstract"), msg);
    }

    //issue-179
    @Inline(
            name = "some.pkg.Stuff",
            source = {
                "package some.pkg;",
                "import org.kohsuke.stapler.DataBoundConstructor;",
                "public class Stuff {",
                "  @DataBoundConstructor public Stuff() {}",
                "  @DataBoundConstructor public Stuff(int i) {}",
                "}"
            })
    @Test
    void duplicatedConstructor1(Results results) {
        List<Diagnostic<? extends JavaFileObject>> diagnostics = results.diagnostics;
        assertEquals(1, diagnostics.size());
        String msg = diagnostics.get(0).getMessage(Locale.ENGLISH);
        assertTrue(msg.contains(ConstructorProcessor.MESSAGE), msg);
    }

    //issue-179
    @Inline(
            name = "some.pkg.Stuff",
            source = {
                "package some.pkg;",
                "import org.kohsuke.stapler.DataBoundConstructor;",
                "public class Stuff {",
                "  @DataBoundConstructor public Stuff() {}",
                "  /**",
                "    @stapler-constructor Another constructor",
                "   **/",
                "  public Stuff(int i) {}",
                "}"
            })
    @Test
    void duplicatedConstructor2(Results results) {
        List<Diagnostic<? extends JavaFileObject>> diagnostics = results.diagnostics;
        assertEquals(1, diagnostics.size());
        String msg = diagnostics.get(0).getMessage(Locale.ENGLISH);
        assertTrue(msg.contains(ConstructorProcessor.MESSAGE), msg);
    }

    //issue-179
    @Inline(
            name = "some.pkg.Stuff",
            source = {
                "package some.pkg;",
                "import org.kohsuke.stapler.DataBoundConstructor;",
                "public class Stuff {",
                "  @DataBoundConstructor public Stuff() {}",
                "  public Stuff(int i) {}",
                "}"
            })
    @Test
    void duplicatedButNotAnnotatedConstructor(Results results) {
        List<Diagnostic<? extends JavaFileObject>> diagnostics = results.diagnostics;
        assertEquals(0, diagnostics.size());
    }
    // TODO nested classes use qualified rather than binary name
}
