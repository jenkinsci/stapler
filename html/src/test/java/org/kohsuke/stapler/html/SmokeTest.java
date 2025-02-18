package org.kohsuke.stapler.html;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import java.util.List;
import org.kohsuke.stapler.test.JettyTestCase;

public final class SmokeTest extends JettyTestCase {

    Status status;

    public void testStandalone() throws Exception {
        status = new Status(
                "Something #123",
                null,
                false,
                new Status.Items(
                        List.of(new Status.Items.Element("k1", "v1"), new Status.Items.Element("k2", "v2 &c."))));
        assertThat(
                load("standalone"),
                allOf(
                        containsString("<title>Something #123</title>"),
                        containsString(
                                "<p>All items:</p> <table style='border: 1px solid black'> <tr> <td>k1</td> <td>v1</td> </tr><tr> <td>k2</td> <td>v2 &amp;c.</td> </tr></table>"),
                        not(containsString("Error in")),
                        not(containsString("There are"))));
        status = new Status("Something #456", new Status.Warning("rotor"), true, null);
        assertThat(
                load("standalone"),
                allOf(
                        containsString("<title>Something #456</title>"),
                        not(containsString("All items:")),
                        containsString("<p style='color: red'>Error in <code>rotor</code>.</p>"),
                        containsString("<p>There are <em>no</em> items.</p>")));
    }

    private String load(String uri) throws Exception {
        return createWebClient()
                .getPage(url.toURI().resolve(uri).toURL())
                .getWebResponse()
                .getContentAsString()
                .replaceAll("<!--.+?-->", " ")
                .replaceAll("\\s+", " ")
                .replace('"', '\'');
    }

    @HtmlView("standalone")
    public Status getStandalone() throws Exception {
        return status;
    }

    public record Status(
            String displayName, @CheckForNull Warning warning, boolean empty, @CheckForNull Items nonempty) {

        public record Warning(String component) {}

        public record Items(List<Element> elements) {

            public record Element(String name, String value) {}
        }
    }
}
