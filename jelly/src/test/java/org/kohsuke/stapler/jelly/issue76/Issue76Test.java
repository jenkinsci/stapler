package org.kohsuke.stapler.jelly.issue76;

import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.kohsuke.stapler.test.JettyTestCase;

import java.net.URL;

/**
 * @author Kohsuke Kawaguchi
 */
public class Issue76Test extends JettyTestCase {
    // bound to /robot
    public final Robot robot = new Robot();

    // protected version of /robot which mimics the URL structure but tweaks its routes
    public final Protection protectedRobot = new Protection(robot);

    public void testRouting() throws Exception {
        WebClient wc = new WebClient();
        HtmlPage p = wc.getPage(new URL(url, "robot/head/eye/3/"));
        assertTrue(p.getWebResponse().getContentAsString().contains("This is eye 3"));

        // protected parts have different index view
        TextPage tp = wc.getPage(new URL(url, "protectedRobot/head/eye/3/"));
        assertTrue(tp.getContent().startsWith("protected eye #3"));
    }
}
