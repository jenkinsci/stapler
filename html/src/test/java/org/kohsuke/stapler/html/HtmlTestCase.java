package org.kohsuke.stapler.html;

import org.kohsuke.stapler.test.JettyTestCase;

public abstract class HtmlTestCase extends JettyTestCase {

    // TODO enable fine logging in subpackages

    protected final String load(String uri) throws Exception {
        return createWebClient()
                .getPage(url.toURI().resolve(uri).toURL())
                .getWebResponse()
                .getContentAsString()
                .replaceAll("<!--.+?-->", " ")
                .replaceAll("\\s+", " ")
                .trim()
                .replace('"', '\'');
    }
}
