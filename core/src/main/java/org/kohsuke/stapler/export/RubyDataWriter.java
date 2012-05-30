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

import java.io.IOException;
import java.io.Writer;

/**
 * Writes out the format that can be <tt>eval</tt>-ed from Ruby.
 *
 * <p>
 * Ruby uses a similar list and map literal syntax as JavaScript.
 * The only differences are <tt>null</tt> vs <tt>nil</tt> and
 * <tt>key:value</tt> vs <tt>key => value</tt>.
 *
 * @author Kohsuke Kawaguchi, Jim Meyer
 */
final class RubyDataWriter extends JSONDataWriter {
    public RubyDataWriter(Writer out, ExportConfig config) throws IOException {
        super(out,config);
    }

    @Override
    public void name(String name) throws IOException {
        comma();
        out.write('"'+name+"\" => ");
        needComma = false;
    }

    public void valueNull() throws IOException {
        data("nil");
    }

    @Override
    public void startObject() throws IOException {
        comma();
        out.write("OpenStruct.new({");
        needComma=false;
    }

    @Override
    public void endObject() throws IOException {
        out.write("})");
        needComma=true;
    }
}
