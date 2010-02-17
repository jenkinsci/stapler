package org.kohsuke.stapler.jelly;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.TagLibrary;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.impl.TagScript;
import org.xml.sax.Attributes;

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
        registerTag("structuredMessageArgument",StructuredMessageArgumentTag.class);
        registerTag("structuredMessageFormat",StructuredMessageFormatTag.class);
    }

    @Override
    public TagScript createTagScript(String name, Attributes attributes) throws JellyException {
        // performance optimization
        if (name.equals("documentation"))
            return new TagScript() {
                public void run(JellyContext context, XMLOutput output) throws JellyTagException {
                    // noop
                }

                @Override
                public void setTagBody(Script tagBody) {
                    // noop, we don't evaluate the body, so don't even keep it in memory.
                }
            };

        if (name.equals("getOutput"))
            return new TagScript() {
                /**
                 * Adds {@link XMLOutput} to the context.
                 */
                public void run(JellyContext context, XMLOutput output) throws JellyTagException {
                    context.setVariable(getAttribute("var").evaluateAsString(context),output);
                }
            };

        return super.createTagScript(name, attributes);
    }
}
