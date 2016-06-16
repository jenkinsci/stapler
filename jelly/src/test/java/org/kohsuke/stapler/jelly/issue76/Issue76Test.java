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

    public final Protection protectedRobot = new Protection(robot);

    public void testRouting() throws Exception {
        WebClient wc = new WebClient();
        HtmlPage p = wc.getPage(new URL(url, "robot/head/"));
        assertTrue(p.getWebResponse().getContentAsString().contains("This is head"));

        // protected parts have different index view
        TextPage tp = wc.getPage(new URL(url, "protectedRobot/head/"));
        assertTrue(tp.getContent().startsWith("protected"));
    }
}
