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

package org.kohsuke.stapler.export;

import org.kohsuke.stapler.StaplerResponse;

import java.io.Writer;
import java.io.IOException;

/**
 * Writes out the format that can be <tt>eval</tt>-ed from Python.
 *
 * <p>
 * Python uses the same list and map literal syntax as JavaScript.
 * The only difference is <tt>null</tt> vs <tt>None</tt>.
 *
 * @author Kohsuke Kawaguchi
 */
final class PythonDataWriter extends JSONDataWriter {
    public PythonDataWriter(Writer out, ExportConfig config) throws IOException {
        super(out, config);
    }

    @Override
    public void valueNull() throws IOException {
        data("None");
    }

    public void valuePrimitive(Object v) throws IOException {
        if(v instanceof Boolean) {
            if((Boolean)v)  data("True");
            else            data("False");
            return;
        }
        super.valuePrimitive(v);
    }
}
