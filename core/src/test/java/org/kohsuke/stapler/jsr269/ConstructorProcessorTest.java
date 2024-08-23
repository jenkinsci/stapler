package org.kohsuke.stapler.jsr269;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.karuslabs.elementary.Results;
import com.karuslabs.elementary.junit.JavacExtension;
import com.karuslabs.elementary.junit.annotations.Inline;
import com.karuslabs.elementary.junit.annotations.Options;
import com.karuslabs.elementary.junit.annotations.Processors;
import java.time.Year;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
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
                "}"
            })
    @Test
    void basicOutput(Results results) {
        Set<String> diagnostics = results.diagnostics.stream()
                .map(d -> d.getMessage(Locale.ENGLISH))
                .collect(Collectors.toSet());
        assertEquals(Set.of("Generating some/pkg/Stuff.stapler"), diagnostics);
        assertEquals(
                "{constructor=count,name}",
                Utils.normalizeProperties(Utils.getGeneratedResource(results.sources, "some/pkg/Stuff.stapler")));
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
        Set<String> diagnostics = results.diagnostics.stream()
                .map(d -> d.getMessage(Locale.ENGLISH))
                .collect(Collectors.toSet());
        assertEquals(Set.of("Generating some/pkg/Stuff.stapler"), diagnostics);
        assertEquals(
                "{constructor=name,count}",
                Utils.normalizeProperties(Utils.getGeneratedResource(results.sources, "some/pkg/Stuff.stapler")));
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
    @Inline(name = "some.pkg.package-info", source = "package some.pkg;")
    @Test
    void JENKINS_11739(Results results) {
        Set<String> diagnostics = results.diagnostics.stream()
                .map(d -> d.getMessage(Locale.ENGLISH))
                .collect(Collectors.toSet());
        assertEquals(Set.of("Generating some/pkg/Stuff.stapler"), diagnostics);
        assertEquals(
                "{constructor=count,name}",
                Utils.normalizeProperties(Utils.getGeneratedResource(results.sources, "some/pkg/Stuff.stapler")));
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
        Set<String> diagnostics = results.diagnostics.stream()
                .map(d -> d.getMessage(Locale.ENGLISH))
                .collect(Collectors.toSet());
        assertEquals(Set.of("@DataBoundConstructor must be applied to a public constructor"), diagnostics);
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
        Set<String> diagnostics = results.diagnostics.stream()
                .map(d -> d.getMessage(Locale.ENGLISH))
                .collect(Collectors.toSet());
        assertEquals(
                Set.of("@DataBoundConstructor may not be used on an abstract class (only on concrete subclasses)"),
                diagnostics);
    }

    // issue-179
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
        Set<String> diagnostics = results.diagnostics.stream()
                .map(d -> d.getMessage(Locale.ENGLISH))
                .collect(Collectors.toSet());
        assertEquals(Set.of("Generating some/pkg/Stuff.stapler", ConstructorProcessor.MESSAGE), diagnostics);
    }

    // issue-179
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
        Set<String> diagnostics = results.diagnostics.stream()
                .map(d -> d.getMessage(Locale.ENGLISH))
                .collect(Collectors.toSet());
        assertEquals(Set.of("Generating some/pkg/Stuff.stapler", ConstructorProcessor.MESSAGE), diagnostics);
    }

    // issue-179
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
        Set<String> diagnostics = results.diagnostics.stream()
                .map(d -> d.getMessage(Locale.ENGLISH))
                .collect(Collectors.toSet());
        assertEquals(Set.of("Generating some/pkg/Stuff.stapler"), diagnostics);
    }
    // TODO nested classes use qualified rather than binary name

    // issue-526
    @Inline(
            name = "some.pkg.Stuff",
            source = {
                "package some.pkg;",
                "import org.kohsuke.stapler.DataBoundConstructor;",
                "public class Stuff {",
                "  @DataBoundConstructor public Stuff(int count, String name) {}",
                "}"
            })
    @Test
    void reproducibleBuild(Results results) {
        Set<String> diagnostics = results.diagnostics.stream()
                .map(d -> d.getMessage(Locale.ENGLISH))
                .collect(Collectors.toSet());
        assertEquals(Set.of("Generating some/pkg/Stuff.stapler"), diagnostics);
        assertThat(
                Utils.getGeneratedResource(results.sources, "some/pkg/Stuff.stapler"),
                not(containsString(Integer.toString(Year.now().getValue()))));
    }
}
