package org.kohsuke.stapler.jelly;

import hudson.util.VersionNumber;
import io.jenkins.lib.versionnumber.JavaSpecificationVersion;
import java.net.URL;
import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlDivision;
import org.htmlunit.html.HtmlPage;
import org.kohsuke.stapler.test.JettyTestCase;

public class AttributeExpressionTest extends JettyTestCase {

    public void testAttributeExpression() throws Exception {
        WebClient wc = createWebClient();
        HtmlPage page = wc.getPage(new URL(url, "/"));

        HtmlDivision div = page.getHtmlElementById("build-timeline-div");
        assertEquals("Timezone", div.asNormalizedText());
        if (JavaSpecificationVersion.forCurrentJVM().isOlderThan(new VersionNumber("16"))) {
            // TODO JENKINS-68215 does not yet work on Java 16+
            assertNotNull(Float.parseFloat(div.getAttribute("data-hour-local-timezone")));
        }
    }
}
