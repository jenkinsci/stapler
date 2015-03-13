package org.kohsuke.stapler;

import com.gargoylesoftware.htmlunit.WebClient;
import org.kohsuke.stapler.test.JettyTestCase;

import java.net.URL;

/**
 * @author Kohsuke Kawaguchi
 */
public class AncestorImplTest extends JettyTestCase {
    public Object testRestOfUrl = new Foo();

    class Foo {
        public Object bar = new Bar();
    }

    class Bar {
        public HttpResponse doZot(StaplerRequest req) {
            assertEquals("testRestOfUrl/bar/zot",   req.getAncestors().get(0).getRestOfUrl());
            assertEquals("bar/zot",                 req.getAncestors().get(1).getRestOfUrl());
            assertEquals("zot",                     req.getAncestors().get(2).getRestOfUrl());
            return HttpResponses.ok();
        }
    }

    // issue 34
    public void testRestOfUrl() throws Exception {
        WebClient wc = new WebClient();
        wc.getPage(new URL(url,"testRestOfUrl/bar/zot"));
    }

    @Override
    protected String getContextPath() {
        return "/contextPathPart";
    }
}
