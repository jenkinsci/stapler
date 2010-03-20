package org.kohsuke.stapler.jelly;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.Tag;
import org.apache.commons.jelly.TagLibrary;
import org.apache.commons.jelly.impl.TagScript;
import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.WebApp;
import org.xml.sax.Attributes;

/**
 * Loads Jelly views associated with "it" as if it were taglibs.
 *
 * @author Kohsuke Kawaguchi
 */
public class ThisTagLibrary extends TagLibrary {
    private ThisTagLibrary() {}

    /**
     * IIUC, this method will never be invoked.
     */
    @Override
    public Tag createTag(final String name, Attributes attributes) throws JellyException {
        return null;
    }

    @Override
    public TagScript createTagScript(final String tagName, Attributes atts) throws JellyException {
        return new CallTagLibScript() {
            @Override
            protected Script resolveDefinition(JellyContext context) throws JellyTagException {
                Object it = context.getVariable("it");
                if (it==null)
                    throw new JellyTagException("'it' was not defined");
                try {
                    MetaClass c = WebApp.getCurrent().getMetaClass(it.getClass());
                    return c.loadTearOff(JellyClassTearOff.class).findScript(tagName+".jelly");
                } catch (JellyException e) {
                    throw new JellyTagException("Failed to load "+tagName+".jelly from "+it,e);
                }
            }
        };
    }

    public static final ThisTagLibrary INSTANCE = new ThisTagLibrary();
}
