package org.kohsuke.stapler.jelly;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import hudson.util.VersionNumber;
import io.jenkins.lib.versionnumber.JavaSpecificationVersion;
import java.net.URL;
import org.kohsuke.stapler.test.JettyTestCase;

public class AttributeExpressionTest extends JettyTestCase {

    public void testAttributeExpression() throws Exception {
        WebClient wc = new WebClient();
        HtmlPage page = wc.getPage(new URL(url, "/"));

        HtmlDivision div = page.getHtmlElementById("build-timeline-div");
        assertEquals("Timezone", div.asNormalizedText());
        if (JavaSpecificationVersion.forCurrentJVM().isOlderThan(new VersionNumber("16"))) {
            // TODO JENKINS-68215 does not yet work on Java 16+
            assertNotNull(Float.parseFloat(div.getAttribute("data-hour-local-timezone")));
        }
    }
}
