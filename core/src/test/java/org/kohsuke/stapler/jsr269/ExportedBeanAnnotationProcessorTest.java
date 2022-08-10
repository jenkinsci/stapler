package org.kohsuke.stapler.jsr269;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.karuslabs.elementary.Results;
import com.karuslabs.elementary.junit.JavacExtension;
import com.karuslabs.elementary.junit.annotations.Inline;
import com.karuslabs.elementary.junit.annotations.Options;
import com.karuslabs.elementary.junit.annotations.Processors;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.jvnet.hudson.annotation_indexer.AnnotationProcessorImpl;

@ExtendWith(JavacExtension.class)
@Options("-Werror")
@Processors({
    AnnotationProcessorImpl.class,
    ExportedBeanAnnotationProcessor.class,
})
class ExportedBeanAnnotationProcessorTest {

    private void assertEqualsCRLF(String s1, String s2) {
        assertEquals(s1.replace("\r\n","\n"), s2.replace("\r\n","\n"));
    }

    @Inline(
            name = "some.pkg.Stuff",
            source = {
                "package some.pkg;",
                "import org.kohsuke.stapler.export.*;",
                "@ExportedBean public class Stuff {",
                "  /* this is not Javadoc */",
                "  @Exported public int getCount() {return 0;}",
                "  /** This gets the display name. */",
                "  @Exported(name=\"name\") public String getDisplayName() {return null;}",
                "}"
            })
    @Test
    void basicOutput(Results results) {
        assertEquals(Collections.emptyList(), results.diagnostics);
        assertEqualsCRLF("some.pkg.Stuff\n", Utils.getGeneratedResource(results.sources, "META-INF/services/annotations/org.kohsuke.stapler.export.ExportedBean"));
        assertEqualsCRLF("some.pkg.Stuff\n", Utils.getGeneratedResource(results.sources, ExportedBeanAnnotationProcessor.STAPLER_BEAN_FILE));
        assertEquals("{getDisplayName=This gets the display name. }", Utils.normalizeProperties(Utils.getGeneratedResource(results.sources, "some/pkg/Stuff.javadoc")));
    }

    @Inline(
            name = "some.pkg.Stuff",
            source = {
                "package some.pkg;",
                "import org.kohsuke.stapler.export.*;",
                "@ExportedBean public class Stuff {",
                "  @Exported public int getCount() {return 0;}",
                "}"
            })
    @Test
    void noJavadoc(Results results) {
        assertEquals(Collections.emptyList(), results.diagnostics);
        assertEqualsCRLF("some.pkg.Stuff\n", Utils.getGeneratedResource(results.sources, "META-INF/services/annotations/org.kohsuke.stapler.export.ExportedBean"));
        assertEqualsCRLF("some.pkg.Stuff\n", Utils.getGeneratedResource(results.sources, ExportedBeanAnnotationProcessor.STAPLER_BEAN_FILE));
        // TODO should it be null, i.e. is it desired to create an empty *.javadoc file?
        assertEquals("{}", Utils.normalizeProperties(Utils.getGeneratedResource(results.sources, "some/pkg/Stuff.javadoc")));
    }

    @Inline(
            name = "some.pkg.Super",
            source = {
                "package some.pkg;",
                "import org.kohsuke.stapler.export.*;",
                "@ExportedBean public abstract class Super {",
                "  @Exported public abstract int getCount();",
                "}",
            })
    @Inline(
            name = "some.pkg.Stuff",
            source = {
                "package some.pkg;",
                "import org.kohsuke.stapler.export.*;",
                "public class Stuff extends some.pkg.Super {",
                "  @Override public int getCount() {return 0;}",
                "}"
            })
    @Test
    void subclassOfExportedBean(Results results) {
        assertEquals(Collections.emptyList(), results.diagnostics);
        /* #7188605: broken in JDK 6u33 + org.jvnet.hudson:annotation-indexer:1.2:
        assertEquals("some.pkg.Stuff\n", Utils.getGeneratedResource(results.sources, "META-INF/services/annotations/org.kohsuke.stapler.export.ExportedBean"));
        */
        // TODO is it intentional that these are not listed here? (example: hudson.plugins.mercurial.MercurialSCM)
        assertEqualsCRLF("some.pkg.Super\n", Utils.getGeneratedResource(results.sources, ExportedBeanAnnotationProcessor.STAPLER_BEAN_FILE));
        assertNull(Utils.normalizeProperties(Utils.getGeneratedResource(results.sources, "some/pkg/Stuff.javadoc")));
    }

    @Inline(
            name = "some.pkg.Stuff",
            source = {
                "package some.pkg;",
                "import org.kohsuke.stapler.export.*;",
                "@org.kohsuke.stapler.jsr269.SourceGeneratingAnnotation",
                "@ExportedBean public class Stuff {",
                "  @Exported public int getCount() {return 0;}",
                "}"
            })
    @Inline(
            name = "some.pkg.MoreStuff",
            source = {
                "package some.pkg;",
                "import org.kohsuke.stapler.export.*;",
                "@ExportedBean public class MoreStuff {",
                "  @Exported public int getCount() {return 0;}",
                "}"
            })
    @Test
    void multiple(Results results) {
        assertEquals(Collections.emptyList(), results.diagnostics);
        assertEqualsCRLF("some.pkg.MoreStuff\nsome.pkg.Stuff\n", Utils.getGeneratedResource(results.sources, "META-INF/services/annotations/org.kohsuke.stapler.export.ExportedBean"));
        assertEqualsCRLF("some.pkg.MoreStuff\nsome.pkg.Stuff\n", Utils.getGeneratedResource(results.sources, ExportedBeanAnnotationProcessor.STAPLER_BEAN_FILE));
    }

    // TODO nested classes - currently saved as qualified rather than binary name, intentional?

}
