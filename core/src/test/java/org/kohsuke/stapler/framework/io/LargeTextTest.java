/*
 * Copyright (c) 2016, CloudBees, Inc
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

package org.kohsuke.stapler.framework.io;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;
import org.jvnet.hudson.test.Issue;

public class LargeTextTest {

    @Issue("JENKINS-37664")
    @Test
    public void writeLogTo() throws Exception {
        assertEquals("", tail("", 0));
        assertEquals("abcde", tail("abcde", 0));
        assertEquals("de", tail("abcde", 3));
        assertEquals("", tail("abcde", 5));
        try {
            fail(tail("abcde", 6));
        } catch (EOFException x) {
            // right
        }
        try {
            fail(tail("abcde", 99));
        } catch (EOFException x) {
            // right
        }
    }
    String tail(String text, long start) throws IOException {
        ByteBuffer bb = new ByteBuffer();
        bb.write(text.getBytes(), 0, text.length());
        LargeText t = new LargeText(bb, true);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assertEquals(text.length(), t.writeLogTo(start, baos));
        return baos.toString();
    }

}
