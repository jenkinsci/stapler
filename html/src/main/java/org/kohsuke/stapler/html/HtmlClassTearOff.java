package org.kohsuke.stapler.html;

import java.net.URL;
import java.util.logging.Logger;
import org.kohsuke.stapler.AbstractTearOff;
import org.kohsuke.stapler.MetaClass;

public final class HtmlClassTearOff extends AbstractTearOff<HtmlClassLoaderTearOff, HtmlJellyScript, Exception> {
    private static final Logger LOGGER = Logger.getLogger(HtmlClassTearOff.class.getName());

    public HtmlClassTearOff(MetaClass owner) {
        super(owner, HtmlClassLoaderTearOff.class);
        LOGGER.fine(() -> "initialized " + owner);
    }

    @Override
    protected String getDefaultScriptExtension() {
        return ".xhtml";
    }

    @Override
    public HtmlJellyScript parseScript(URL res) throws Exception {
        return classLoader.parse(res, owner);
    }
}
