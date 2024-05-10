/*
 * Copyright (c) 2011, CloudBees, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this list of
 *       conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
 * THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.kohsuke.stapler.jsr269;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.processing.AbstractProcessor;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

/**
 * @author Kohsuke Kawaguchi
 */
abstract class AbstractProcessorImpl extends AbstractProcessor {
    @SuppressFBWarnings(value = "INFORMATION_EXPOSURE_THROUGH_AN_ERROR_MESSAGE", justification = "Jenkins handles this issue differently or doesn't care about it")
    protected String toString(Throwable t) {
        StringWriter w = new StringWriter();
        t.printStackTrace(new PrintWriter(w));
        return w.toString();
    }

    protected void error(Throwable t) {
        error(toString(t));
    }

    protected void error(String msg) {
        processingEnv.getMessager().printMessage(Kind.ERROR, msg);
    }

    protected String getJavadoc(Element md) {
        return processingEnv.getElementUtils().getDocComment(md);
    }

    protected void notice(String msg, Element location) {
        // IntelliJ flags this as an error. So disabling it for now.
        // See http://youtrack.jetbrains.net/issue/IDEA-71822
        // processingEnv.getMessager().printMessage(NOTE, msg, location);
    }

    protected void writePropertyFile(Properties p, String name) throws IOException {
        FileObject f = createResource(name);
        /*
         * This is somewhat fragile, but it is the only practical option on Java 11 and 17. In Java 21, we could instead
         * set the "java.properties.date" system property to a fixed string and avoid the need to work around internal
         * Java Platform implementation details.
         */
        try (Writer w = f.openWriter(); BufferedWriter bw = new CommentStrippingBufferedWriter(w)) {
            p.store(bw,null);
        }
    }

    protected FileObject getResource(String name) throws IOException {
        return processingEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "", name);
    }

    protected FileObject createResource(String name) throws IOException {
        return processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", name);
    }

    private static class CommentStrippingBufferedWriter extends BufferedWriter {
        private final AtomicInteger count = new AtomicInteger(0);

        public CommentStrippingBufferedWriter(Writer out) {
            super(out);
        }

        @Override
        public void write(String str) throws IOException {
            if (count.getAndIncrement() > 0 || !str.startsWith("#")) {
                super.write(str);
            }
        }
    }
}
