/*
 * The MIT License
 *
 * Copyright (c) 2017 IKEDA Yasuyuki
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.kohsuke.stapler.jelly;

import java.net.URL;
import java.util.Locale;

import org.jvnet.localizer.LocaleProvider;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.test.JettyTestCase;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Tests for {@link ResourceBundle}
 */
public class ResourceBundleTest extends JettyTestCase{
    private LocaleProvider newLocaleProvider = new LocaleProvider() {
        public Locale get() {
            Locale locale = null;
            StaplerRequest req = Stapler.getCurrentRequest();
            if(req != null) {
                locale = req.getLocale();
            }
            if(locale == null) {
                locale = Locale.getDefault();
            }
            return locale;
        }
    };
    private LocaleProvider oldLocaleProvider;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        oldLocaleProvider = LocaleProvider.getProvider();
        LocaleProvider.setProvider(newLocaleProvider);
    }

    @Override
    protected void tearDown() throws Exception {
        assert(newLocaleProvider == LocaleProvider.getProvider());
        LocaleProvider.setProvider(oldLocaleProvider);
        super.tearDown();
    }

    public void testPropertiesFile() throws Exception {
        WebClient wc = new WebClient();
        wc.addRequestHeader("Accept-Language", "en-US");
        HtmlPage page = wc.getPage(new URL(url, "/"));
        assertEquals("English(US)", page.getElementById("language").getTextContent());
    }

    public void testXmlFile() throws Exception {
        WebClient wc = new WebClient();
        wc.addRequestHeader("Accept-Language", "ja-JP");
        HtmlPage page = wc.getPage(new URL(url, "/"));
        assertEquals("日本語", page.getElementById("language").getTextContent());
    }

    public void testBothFile() throws Exception {
        WebClient wc = new WebClient();
        wc.addRequestHeader("Accept-Language", "fr-FR");
        HtmlPage page = wc.getPage(new URL(url, "/"));
        assertEquals("Français(properties)", page.getElementById("language").getTextContent());
    }
}
