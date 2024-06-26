/*
 * Copyright (c) 2017, CloudBees, Inc
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

package org.kohsuke.stapler;

import static org.junit.Assert.assertThrows;

import jakarta.servlet.http.HttpServletResponse;
import java.net.URL;
import org.htmlunit.FailingHttpStatusCodeException;
import org.htmlunit.WebClient;
import org.jvnet.hudson.test.For;
import org.jvnet.hudson.test.Issue;
import org.kohsuke.stapler.test.JettyTestCase;

@For(Stapler.class) // but StaplerTest is not a JettyTestCase
public class Stapler2Test extends JettyTestCase {

    @Issue("SECURITY-390")
    public void testTraceXSS() {
        WebClient wc = createWebClient();
        FailingHttpStatusCodeException exc;
        Dispatcher.TRACE = true;
        try {
            exc = assertThrows(
                    FailingHttpStatusCodeException.class,
                    () -> wc.getPage(new URL(this.url, "thing/<button>/x")).getWebResponse());
        } finally {
            Dispatcher.TRACE = false;
        }
        assertEquals(HttpServletResponse.SC_NOT_FOUND, exc.getStatusCode());
        String html = exc.getResponse().getContentAsString();
        assertTrue(html, html.contains("&lt;button&gt;"));
        assertFalse(html, html.contains("<button>"));
    }

    public Object getThing(String name) {
        return name;
    }
}
