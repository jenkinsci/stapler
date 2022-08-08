package org.kohsuke.stapler.jsr269;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.karuslabs.elementary.Results;
import com.karuslabs.elementary.junit.JavacExtension;
import com.karuslabs.elementary.junit.annotations.Inline;
import com.karuslabs.elementary.junit.annotations.Options;
import com.karuslabs.elementary.junit.annotations.Processors;
import java.util.Collections;
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
                "}",
            })
    @Test
    void basicOutput(Results results) {
        assertEquals(Collections.emptyList(), results.diagnostics);
        assertEquals("key", Utils.getGeneratedResource(results.sources, "some/pkg/Stuff/doOneThing.stapler"));
        assertEquals("name,address", Utils.getGeneratedResource(results.sources, "some/pkg/Stuff/doAnother.stapler"));
    }

    // TODO nested classes use qualified rather than binary name

}
