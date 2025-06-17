package org.kohsuke.stapler.html;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public final class IncludedFromTest extends HtmlTestCase {

    public void testIncludedFromJelly() throws Exception {
        assertThat(
                load("top"),
                is(
                        "A prologue. <div> Center text includes a <span>special &lt;cool&gt; value</span>. </div> An epilogue."));
    }

    @HtmlView("center")
    public Center getCenter() {
        return new Center("special <cool> value");
    }

    public record Center(String value) {}
}
