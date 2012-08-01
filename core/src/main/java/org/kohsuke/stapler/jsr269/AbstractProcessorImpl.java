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

import javax.annotation.processing.AbstractProcessor;
import javax.lang.model.element.Element;
import javax.tools.FileObject;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

import static javax.tools.Diagnostic.Kind.*;
import static javax.tools.StandardLocation.*;

/**
 * @author Kohsuke Kawaguchi
 */
@SuppressWarnings({"Since15"})
abstract class AbstractProcessorImpl extends AbstractProcessor {
    protected String toString(Throwable t) {
        StringWriter w = new StringWriter();
        t.printStackTrace(new PrintWriter(w));
        return w.toString();
    }

    protected void error(Throwable t) {
        error(toString(t));
    }

    protected void error(String msg) {
        processingEnv.getMessager().printMessage(ERROR, msg);
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
        OutputStream os = f.openOutputStream();
        try {
            p.store(os,null);
        } finally {
            os.close();
        }
    }

    protected FileObject getResource(String name) throws IOException {
        return processingEnv.getFiler().getResource(CLASS_OUTPUT, "", name);
    }

    protected FileObject createResource(String name) throws IOException {
        return processingEnv.getFiler().createResource(CLASS_OUTPUT, "", name);
    }
}
