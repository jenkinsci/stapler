package org.kohsuke.stapler.jelly.issue76;

import java.net.URL;
import org.htmlunit.TextPage;
import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlPage;
import org.kohsuke.stapler.test.JettyTestCase;

/**
 * @author Kohsuke Kawaguchi
 */
public class Issue76Test extends JettyTestCase {
    // bound to /robot
    public final Robot robot = new Robot();

    // protected version of /robot which mimics the URL structure but tweaks its routes
    public final Protection protectedRobot = new Protection(robot);

    public void testRouting() throws Exception {
        WebClient wc = createWebClient();
        HtmlPage p = wc.getPage(new URL(url, "robot/head/eye/3/"));
        assertTrue(p.getWebResponse().getContentAsString().contains("This is eye 3"));

        // protected parts have different index view
        TextPage tp = wc.getPage(new URL(url, "protectedRobot/head/eye/3/"));
        assertTrue(tp.getContent().startsWith("protected eye #3"));

        tp = wc.getPage(new URL(url, "protectedRobot/head/nose"));
        assertEquals("nose",tp.getContent().trim());

        tp = wc.getPage(new URL(url, "protectedRobot/arm/hand/nail"));
        assertEquals("/hand/nail",tp.getContent().trim());

        tp = wc.getPage(new URL(url, "protectedRobot/arm"));
        assertEquals("protected arm",tp.getContent().trim());

        {// list lookup
            p = wc.getPage(new URL(url, "robot/legs/0/"));
            assertTrue(p.getWebResponse().getContentAsString().contains("left leg"));

            tp = wc.getPage(new URL(url, "protectedRobot/legs/1/"));
            assertEquals("protected right leg", tp.getContent().trim());
        }

        {// map lookup
            p = wc.getPage(new URL(url, "robot/buttons/red/"));
            assertTrue(p.getWebResponse().getContentAsString().contains("This is a button"));

            tp = wc.getPage(new URL(url, "protectedRobot/buttons/red/"));
            assertEquals("protected red button", tp.getContent().trim());
        }
    }
}
