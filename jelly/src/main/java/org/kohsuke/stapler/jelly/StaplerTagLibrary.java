package org.kohsuke.stapler.jelly;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.TagLibrary;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.impl.TagScript;
import org.xml.sax.Attributes;

import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Kohsuke Kawaguchi
 */
public class StaplerTagLibrary extends TagLibrary {
    public StaplerTagLibrary() {
        registerTag("adjunct",AdjunctTag.class);
        registerTag("bind",BindTag.class);
        registerTag("compress",CompressTag.class);
        registerTag("contentType",ContentTypeTag.class);
        registerTag("copyStream",CopyStreamTag.class);
        registerTag("doctype",DoctypeTag.class);
        registerTag("findAncestor",FindAncestorTag.class);
        registerTag("header",HeaderTag.class);
        registerTag("include",IncludeTag.class);
        registerTag("isUserInRole",IsUserInRoleTag.class);
        registerTag("nbsp",NbspTag.class);
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

        if (name.equals("once"))
            return new TagScript() {
                /**
                 * Adds {@link XMLOutput} to the context.
                 */
                public void run(JellyContext context, XMLOutput output) throws JellyTagException {
                    HttpServletRequest request = (HttpServletRequest)context.getVariable("request");
                    Set<String> executedScripts = (Set<String>) request.getAttribute(ONCE_TAG_KEY);
                    if(executedScripts==null)
                        request.setAttribute(ONCE_TAG_KEY,executedScripts=new HashSet<String>());

                    String key = getFileName()+':'+getLineNumber()+':'+getColumnNumber();

                    if(executedScripts.add(key)) // run it just for the first time
                        getTagBody().run(context,output);
                }
            };

        return super.createTagScript(name, attributes);
    }

    private static final String ONCE_TAG_KEY = "stapler.once";
}
