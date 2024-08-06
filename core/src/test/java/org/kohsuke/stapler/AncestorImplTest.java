package org.kohsuke.stapler;

import java.net.URL;
import org.htmlunit.WebClient;
import org.kohsuke.stapler.test.JettyTestCase;

/**
 * @author Kohsuke Kawaguchi
 */
public class AncestorImplTest extends JettyTestCase {
    public Object testRestOfUrl = new Foo();

    class Foo {
        public Object bar = new Bar();
    }

    class Bar {
        public HttpResponse doZot(StaplerRequest2 req) {
            assertEquals("testRestOfUrl/bar/zot", req.getAncestors().get(0).getRestOfUrl());
            assertEquals("bar/zot", req.getAncestors().get(1).getRestOfUrl());
            assertEquals("zot", req.getAncestors().get(2).getRestOfUrl());
            return HttpResponses.ok();
        }
    }

    // issue 34
    public void testRestOfUrl() throws Exception {
        WebClient wc = createWebClient();
        wc.getPage(new URL(url, "testRestOfUrl/bar/zot"));
    }

    @Override
    protected String getContextPath() {
        return "/contextPathPart";
    }
}
