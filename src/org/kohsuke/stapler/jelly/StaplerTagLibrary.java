package org.kohsuke.stapler.jelly;

import org.apache.commons.jelly.TagLibrary;

/**
 * @author Kohsuke Kawaguchi
 */
public class StaplerTagLibrary extends TagLibrary {
    public StaplerTagLibrary() {
        registerTag("contentType",ContentTypeTag.class);
        registerTag("include",IncludeTag.class);
    }
}
