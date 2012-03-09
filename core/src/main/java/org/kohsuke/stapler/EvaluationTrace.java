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

import java.util.List;
import java.util.ArrayList;
import java.io.PrintWriter;

/**
 * Remebers the {@link Stapler#invoke(RequestImpl, ResponseImpl, Object)}
 * evaluation traces.
 *
 * @author Kohsuke Kawaguchi
 */
public class EvaluationTrace {
    private final List<String> traces = new ArrayList<String>();

    public void trace(StaplerResponse rsp, String msg) {
        traces.add(msg);
        // Firefox Live HTTP header plugin cannot nicely render multiple headers
        // with the same name, so give each one unique name.
        rsp.addHeader(String.format("Stapler-Trace-%03d",traces.size()),msg.replace("\n","\\n").replace("\r","\\r"));
    }
    
    public void printHtml(PrintWriter w) {
        for (String trace : traces)
            w.println(trace.replaceAll("&","&amp;").replaceAll("<","&lt;"));
    }

    public static EvaluationTrace get(StaplerRequest req) {
        EvaluationTrace et = (EvaluationTrace) req.getAttribute(KEY);
        if(et==null)
            req.setAttribute(KEY,et=new EvaluationTrace());
        return et;
    }

    /**
     * Used for counting trace header.
     */
    private static final String KEY = EvaluationTrace.class.getName();
}
