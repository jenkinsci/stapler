package org.kohsuke.stapler.jsr269;

import java.util.Collections;
import net.java.dev.hickory.testing.Compilation;
import static org.junit.Assert.*;
import org.junit.Test;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

public class ExportedBeanAnnotationProcessorTest {

    @Test public void basicOutput() {
        Compilation compilation = new Compilation();
        compilation.addSource("some.pkg.Stuff").
                addLine("package some.pkg;").
                addLine("import org.kohsuke.stapler.export.*;").
                addLine("@ExportedBean public class Stuff {").
                addLine("  /* this is not Javadoc */").
                addLine("  @Exported public int getCount() {return 0;}").
                addLine("  /** This gets the display name. */").
                addLine("  @Exported(name=\"name\") public String getDisplayName() {return null;}").
                addLine("}");
        compilation.doCompile(null, "-source", "6");
        assertEquals(Collections.emptyList(), Utils.filterSupportedSourceVersionWarnings(compilation.getDiagnostics()));
        assertEquals("some.pkg.Stuff\n", Utils.getGeneratedResource(compilation, "META-INF/annotations/org.kohsuke.stapler.export.ExportedBean"));
        assertEquals("some.pkg.Stuff\n", Utils.getGeneratedResource(compilation, ExportedBeanAnnotationProcessor.STAPLER_BEAN_FILE));
        assertEquals("{getDisplayName=This gets the display name. }", Utils.normalizeProperties(Utils.getGeneratedResource(compilation, "some/pkg/Stuff.javadoc")));
    }

    @Test public void noJavadoc() {
        Compilation compilation = new Compilation();
        compilation.addSource("some.pkg.Stuff").
                addLine("package some.pkg;").
                addLine("import org.kohsuke.stapler.export.*;").
                addLine("@ExportedBean public class Stuff {").
                addLine("  @Exported public int getCount() {return 0;}").
                addLine("}");
        compilation.doCompile(null, "-source", "6");
        assertEquals(Collections.emptyList(), Utils.filterSupportedSourceVersionWarnings(compilation.getDiagnostics()));
        assertEquals("some.pkg.Stuff\n", Utils.getGeneratedResource(compilation, "META-INF/annotations/org.kohsuke.stapler.export.ExportedBean"));
        assertEquals("some.pkg.Stuff\n", Utils.getGeneratedResource(compilation, ExportedBeanAnnotationProcessor.STAPLER_BEAN_FILE));
        // XXX should it be null, i.e. is it desired to create an empty *.javadoc file?
        assertEquals("{}", Utils.normalizeProperties(Utils.getGeneratedResource(compilation, "some/pkg/Stuff.javadoc")));
    }
    
    @ExportedBean public static abstract class Super {
        @Exported public abstract int getCount();
    }
    @Test public void subclassOfExportedBean() {
        Compilation compilation = new Compilation();
        compilation.addSource("some.pkg.Stuff").
                addLine("package some.pkg;").
                addLine("import org.kohsuke.stapler.export.*;").
                addLine("public class Stuff extends " + Super.class.getCanonicalName() + " {").
                addLine("  @Override public int getCount() {return 0;}").
                addLine("}");
        compilation.doCompile(null, "-source", "6");
        assertEquals(Collections.emptyList(), Utils.filterSupportedSourceVersionWarnings(compilation.getDiagnostics()));
        /* #7188605: broken in JDK 6u33 + org.jvnet.hudson:annotation-indexer:1.2:
        assertEquals("some.pkg.Stuff\n", Utils.getGeneratedResource(compilation, "META-INF/annotations/org.kohsuke.stapler.export.ExportedBean"));
        */
        // XXX is it intentional that these are not listed here? (example: hudson.plugins.mercurial.MercurialSCM)
        assertEquals(null, Utils.getGeneratedResource(compilation, ExportedBeanAnnotationProcessor.STAPLER_BEAN_FILE));
        assertEquals(null, Utils.normalizeProperties(Utils.getGeneratedResource(compilation, "some/pkg/Stuff.javadoc")));
    }

    // XXX multiple beans, incremental compilation
    // XXX nested classes - currently saved as qualified rather than binary name, intentional?

}
