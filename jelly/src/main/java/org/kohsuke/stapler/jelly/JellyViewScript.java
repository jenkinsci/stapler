package org.kohsuke.stapler.jelly;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.XMLOutput;

import java.net.URL;

/**
 * Represents a loaded Jelly view script that remembers where it came from.
 *
 * @author Kohsuke Kawaguchi
 */
public final class JellyViewScript implements Script {
    /**
     * Which class is this view loaded from?
     */
    public final Class from;
    /**
     * Full URL that points to the source of the script.
     */
    public final URL source;

    private Script base;

    public JellyViewScript(Class from, URL source, Script base) {
        this.from = from;
        this.source = source;
        this.base = base;
    }

    public Script compile() throws JellyException {
        base = base.compile();
        return this;
    }

    public void run(JellyContext context, XMLOutput output) throws JellyTagException {
        base.run(context,output);
    }

    public String getName() {
        // get to the file name portion
        String url = source.toExternalForm();
        url = url.substring(url.lastIndexOf('/') +1);
        url = url.substring(url.lastIndexOf('\\') +1);

        return from.getName().replace('.','/').replace('$','/')+'/'+url;
    }
}
