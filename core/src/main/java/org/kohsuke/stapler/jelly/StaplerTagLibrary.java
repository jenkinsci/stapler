package org.kohsuke.stapler.jelly;

import org.apache.commons.jelly.TagLibrary;

/**
 * @author Kohsuke Kawaguchi
 */
public class StaplerTagLibrary extends TagLibrary {
    public StaplerTagLibrary() {
        registerTag("adjunct",AdjunctTag.class);
        registerTag("compress",CompressTag.class);
        registerTag("contentType",ContentTypeTag.class);
        registerTag("copyStream",CopyStreamTag.class);
        registerTag("doctype",DoctypeTag.class);
        registerTag("documentation",DocumentationTag.class);
        registerTag("findAncestor",FindAncestorTag.class);
        registerTag("header",HeaderTag.class);
        registerTag("include",IncludeTag.class);
        registerTag("isUserInRole",IsUserInRoleTag.class);
        registerTag("nbsp",NbspTag.class);
        registerTag("once",OnceTag.class);
        registerTag("out",OutTag.class);
        registerTag("parentScope",ParentScopeTag.class);
        registerTag("redirect",RedirectTag.class);
        registerTag("statusCode",StatusCodeTag.class);
    }
}
