package org.kohsuke.stapler.jsr269;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.karuslabs.elementary.Results;
import com.karuslabs.elementary.junit.JavacExtension;
import com.karuslabs.elementary.junit.annotations.Inline;
import com.karuslabs.elementary.junit.annotations.Options;
import com.karuslabs.elementary.junit.annotations.Processors;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(JavacExtension.class)
@Options("-Werror")
@Processors(QueryParameterAnnotationProcessor.class)
class QueryParameterAnnotationProcessorTest {

    @Inline(
            name = "some.pkg.Stuff",
            source = {
                "package some.pkg;",
                "import org.kohsuke.stapler.QueryParameter;",
                "public class Stuff {",
                "  public void doOneThing(@QueryParameter String key) {}",
                "  public void doAnother(@QueryParameter(\"ignoredHere\") String name, @QueryParameter String address) {}",
                "}"
            })
    @Test
    void basicOutput(Results results) {
        Set<String> diagnostics = results.diagnostics.stream()
                .map(d -> d.getMessage(Locale.ENGLISH))
                .collect(Collectors.toSet());
        assertEquals(
                Set.of("Generating some/pkg/Stuff/doOneThing.stapler", "Generating some/pkg/Stuff/doAnother.stapler"),
                diagnostics);
        assertEquals("key", Utils.getGeneratedResource(results.sources, "some/pkg/Stuff/doOneThing.stapler"));
        assertEquals("name,address", Utils.getGeneratedResource(results.sources, "some/pkg/Stuff/doAnother.stapler"));
    }

    @Inline(
            name = "some.pkg.Stuff",
            source = {
                "package some.pkg;",
                "import org.kohsuke.stapler.QueryParameter;",
                "import org.kohsuke.stapler.StaplerRequest;",
                "import org.kohsuke.stapler.StaplerResponse;",
                "import org.kohsuke.stapler.StaplerRequest2;",
                "import org.kohsuke.stapler.StaplerResponse2;",
                "public class Stuff {",
                "  public void doBuild(StaplerRequest2 req, StaplerResponse2 rsp, @QueryParameter int delay) {}",
                "  @Deprecated",
                "  public void doBuild(StaplerRequest req, StaplerResponse rsp, @QueryParameter int delay) {}",
                "}"
            })
    @Test
    void deprecated(Results results) {
        Set<String> diagnostics = results.diagnostics.stream()
                .map(d -> d.getMessage(Locale.ENGLISH))
                .collect(Collectors.toSet());
        assertEquals(Set.of("Generating some/pkg/Stuff/doBuild.stapler"), diagnostics);
        assertEquals("req,rsp,delay", Utils.getGeneratedResource(results.sources, "some/pkg/Stuff/doBuild.stapler"));
    }

    // TODO nested classes use qualified rather than binary name

}
