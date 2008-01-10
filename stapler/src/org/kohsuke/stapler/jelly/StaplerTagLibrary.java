package org.kohsuke.stapler.jelly;

import org.apache.commons.jelly.TagLibrary;

/**
 * @author Kohsuke Kawaguchi
 */
public class StaplerTagLibrary extends TagLibrary {
    public StaplerTagLibrary() {
        registerTag("contentType",ContentTypeTag.class);
        registerTag("header",HeaderTag.class);
        registerTag("include",IncludeTag.class);
        registerTag("nbsp",NbspTag.class);
        registerTag("isUserInRole",IsUserInRoleTag.class);
        registerTag("parentScope",ParentScopeTag.class);
        registerTag("out",OutTag.class);
        registerTag("copyStream",CopyStreamTag.class);
        registerTag("compress",CompressTag.class);
        registerTag("redirect",RedirectTag.class);
    }
}
