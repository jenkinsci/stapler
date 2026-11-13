package org.kohsuke.stapler.html;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public final class IncludesTest extends HtmlTestCase {

    public void testIncludes() throws Exception {
        assertThat(
                load("top"),
                is(
                        "<div> A prologue. <span style='color: red'> There are <span>23</span> items. </span> An epilogue. </div>"));
    }

    @HtmlView("top")
    public Top getTop() {
        return new Top(new Nested());
    }

    public record Top(@HtmlInclude("center") Nested nested) {}

    public static final class Nested {

        @HtmlView("center")
        public Center getCenter() {
            return new Center("23");
        }

        public record Center(String count) {}
    }
}
