package org.kohsuke.stapler.jelly.issue76;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.kohsuke.stapler.test.JettyTestCase;

import java.io.IOException;
import java.net.URL;

/**
 * @author Kohsuke Kawaguchi
 */
public class Issue76Test extends JettyTestCase {
    // bound to /robot
    public final Robot robot = new Robot();

    public final Protection protectedRobot = new Protection(robot);

    public void testRouting() throws Exception {
        WebClient wc = new WebClient();
        HtmlPage p = wc.getPage(new URL(url, "robot/head/"));
        assertTrue(p.getWebResponse().getContentAsString().contains("This is head"));

        // protected parts do not expose Jelly views
        try {
            wc.getPage(new URL(url, "protectedRobot/head/"));
            fail("Expected 404");
        } catch (FailingHttpStatusCodeException e) {
            assertEquals(404,e.getStatusCode());
        }
    }
}
