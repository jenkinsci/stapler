/*
 * Copyright (c) 2004-2010, Kohsuke Kawaguchi
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

package org.kohsuke.stapler;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Remembers the {@link Stapler#invoke(RequestImpl, ResponseImpl, Object)}
 * evaluation traces.
 *
 * @author Kohsuke Kawaguchi
 */
public class EvaluationTrace {
    private final List<String> traces = new ArrayList<>();

    private static final Logger LOGGER = Logger.getLogger(EvaluationTrace.class.getName());

    public void trace(StaplerResponse rsp, String msg) {
        traces.add(msg);
        // Firefox Live HTTP header plugin cannot nicely render multiple headers
        // with the same name, so give each one unique name.
        rsp.addHeader(
                String.format("Stapler-Trace-%03d", traces.size()),
                msg.replace("\n", "\\n").replace("\r", "\\r"));
    }

    public void printHtml(PrintWriter w) {
        for (String trace : traces) {
            w.println(Stapler.escape(trace));
        }
    }

    public static EvaluationTrace get(StaplerRequest req) {
        EvaluationTrace et = (EvaluationTrace) req.getAttribute(KEY);
        if (et == null) {
            req.setAttribute(KEY, et = new EvaluationTrace());
        }
        return et;
    }

    /**
     * Used for counting trace header.
     */
    private static final String KEY = EvaluationTrace.class.getName();

    public abstract static class ApplicationTracer {
        protected abstract void record(StaplerRequest req, String message);

        public static void trace(StaplerRequest req, String message) {
            List<ApplicationTracer> tracers = getTracers();
            for (ApplicationTracer tracer : tracers) {
                tracer.record(req, message);
            }
        }

        private static volatile List<ApplicationTracer> tracers;

        @NonNull
        private static List<ApplicationTracer> getTracers() {
            synchronized (ApplicationTracer.class) {
                if (tracers == null) {
                    List<ApplicationTracer> t = new ArrayList<>();
                    for (ApplicationTracer tracer : ServiceLoader.load(
                            EvaluationTrace.ApplicationTracer.class,
                            Stapler.getCurrent().getWebApp().getClassLoader())) {
                        try {
                            t.add(tracer);
                        } catch (Exception e) {
                            // robustness
                            if (LOGGER.isLoggable(Level.FINE)) {
                                LOGGER.log(Level.FINE, "Exception thrown when notifying tracer", e);
                            }
                        }
                    }
                    tracers = t;
                }
            }
            return tracers;
        }
    }
}
