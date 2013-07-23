/*
 * Copyright (c) 2013, Jesse Glick
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.kohsuke.stapler.jsr269;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import net.java.dev.hickory.testing.Compilation;
import org.junit.Test;
import static org.junit.Assert.*;

public class RequirePOSTAnnotationProcessorTest {

    @Test public void fine() throws Exception {
        Compilation compilation = new Compilation();
        compilation.addSource("some.pkg.Stuff").
                addLine("package some.pkg;").
                addLine("import org.kohsuke.stapler.interceptor.RequirePOST;").
                addLine("public class Stuff {").
                addLine("  @RequirePOST public void doSomething() {}").
                addLine("}");
        compilation.doCompile(null, "-source", "6");
        assertEquals(Collections.emptyList(), Utils.filterSupportedSourceVersionWarnings(compilation.getDiagnostics()));
    }

    @Test public void noAbstract() throws Exception {
        Compilation compilation = new Compilation();
        compilation.addSource("some.pkg.Stuff").
                addLine("package some.pkg;").
                addLine("import org.kohsuke.stapler.interceptor.RequirePOST;").
                addLine("public abstract class Stuff {").
                addLine("  @RequirePOST public abstract void doSomething();").
                addLine("}");
        compilation.doCompile(null, "-source", "6");
        List<Diagnostic<? extends JavaFileObject>> diagnostics = Utils.filterSupportedSourceVersionWarnings(compilation.getDiagnostics());
        assertEquals(1, diagnostics.size());
        assertEquals(Diagnostic.Kind.ERROR, diagnostics.get(0).getKind());
        String msg = diagnostics.get(0).getMessage(Locale.ENGLISH);
        assertTrue(msg, msg.contains("abstract"));
    }

    @Test public void failsToOverride() throws Exception {
        Compilation compilation = new Compilation();
        compilation.addSource("some.pkg.Stuff").
                addLine("package some.pkg;").
                addLine("import org.kohsuke.stapler.interceptor.RequirePOST;").
                addLine("public class Stuff {").
                addLine("  @RequirePOST public void doSomething() {}").
                addLine("}");
        compilation.addSource("some.pkg.SpecialStuff").
                addLine("package some.pkg;").
                addLine("public class SpecialStuff extends Stuff {").
                addLine("  @Override public void doSomething() {}").
                addLine("}");
        compilation.doCompile(null, "-source", "6");
        List<Diagnostic<? extends JavaFileObject>> diagnostics = Utils.filterSupportedSourceVersionWarnings(compilation.getDiagnostics());
        assertEquals(1, diagnostics.size());
        assertEquals(Diagnostic.Kind.WARNING, diagnostics.get(0).getKind());
        assertEquals("some/pkg/SpecialStuff.java", diagnostics.get(0).getSource().toUri().toString());
        String msg = diagnostics.get(0).getMessage(Locale.ENGLISH);
        assertTrue(msg, msg.contains("overrid"));
    }

    @Test public void overridesOK() throws Exception {
        Compilation compilation = new Compilation();
        compilation.addSource("some.pkg.Stuff").
                addLine("package some.pkg;").
                addLine("import org.kohsuke.stapler.interceptor.RequirePOST;").
                addLine("public class Stuff {").
                addLine("  @RequirePOST public void doSomething() {}").
                addLine("}");
        compilation.addSource("some.pkg.SpecialStuff").
                addLine("package some.pkg;").
                addLine("import org.kohsuke.stapler.interceptor.RequirePOST;").
                addLine("public class SpecialStuff extends Stuff {").
                addLine("  @RequirePOST  @Override public void doSomething() {}").
                addLine("}");
        compilation.doCompile(null, "-source", "6");
        assertEquals(Collections.emptyList(), Utils.filterSupportedSourceVersionWarnings(compilation.getDiagnostics()));
    }

}