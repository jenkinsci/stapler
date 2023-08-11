package org.kohsuke.stapler.jelly;

import org.htmlunit.WebClient;
import org.htmlunit.html.DomElement;
import org.htmlunit.html.DomNodeList;
import org.htmlunit.html.HtmlDefinitionDescription;
import org.htmlunit.html.HtmlDefinitionTerm;
import org.htmlunit.html.HtmlPage;
import hudson.util.VersionNumber;
import io.jenkins.lib.versionnumber.JavaSpecificationVersion;
import java.net.URL;
import org.kohsuke.stapler.test.JettyTestCase;

public class BodyExpressionTest extends JettyTestCase {

    public void testBodyExpression() throws Exception {
        WebClient wc = createWebClient();
        HtmlPage page = wc.getPage(new URL(url, "/"));

        DomNodeList<DomElement> dts = page.getElementsByTagName("dt");
        assertEquals(1, dts.size());
        HtmlDefinitionTerm dt = (HtmlDefinitionTerm) dts.get(0);
        assertEquals("Timezone", dt.asNormalizedText());

        DomNodeList<DomElement> dds = page.getElementsByTagName("dd");
        assertEquals(1, dds.size());
        HtmlDefinitionDescription dd = (HtmlDefinitionDescription) dds.get(0);
        if (JavaSpecificationVersion.forCurrentJVM().isOlderThan(new VersionNumber("16"))) {
            // TODO JENKINS-68215 does not yet work on Java 16+
            assertNotNull(Float.parseFloat(dd.asNormalizedText()));
        }
    }
}
