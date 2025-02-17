package org.kohsuke.stapler.html;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import org.htmlunit.html.HtmlPage;
import org.kohsuke.stapler.test.JettyTestCase;

public final class SmokeTest extends JettyTestCase {

    String text;

    public void testStandalone() throws Exception {
        text = "Some text & stuff.";
        var wc = createWebClient();
        HtmlPage page = wc.getPage(url.toURI().resolve("standalone").toURL());
        assertThat(
                page.getWebResponse().getContentAsString().replaceAll("\\s+", " "),
                containsString("<body> <p id=\"main\">Some text &amp; stuff.</p> </body>"));
        text = "Altered text.";
        page = wc.getPage(url.toURI().resolve("standalone").toURL());
        assertThat(
                page.getWebResponse().getContentAsString().replaceAll("\\s+", " "),
                containsString("<body> <p id=\"main\">Altered text.</p> </body>"));
    }

    @HtmlView("standalone")
    public HtmlViewRenderer renderStandalone() throws Exception {
        return new HtmlViewRenderer() {
            @Override
            public String supplyText(String id) throws Exception {
                if (id.equals("main")) {
                    return text;
                } else {
                    throw new IllegalArgumentException();
                }
            }
        };
    }
}
