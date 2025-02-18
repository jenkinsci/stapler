package org.kohsuke.stapler.html.impl;

import java.net.URL;
import java.util.logging.Logger;
import org.dom4j.io.SAXReader;
import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.MetaClassLoader;
import org.kohsuke.stapler.html.HtmlView;

public final class HtmlClassLoaderTearOff {
    private static final Logger LOGGER = Logger.getLogger(HtmlClassLoaderTearOff.class.getName());
    private final MetaClassLoader owner;
    private final SAXReader parser;

    public HtmlClassLoaderTearOff(MetaClassLoader owner) throws Exception {
        this.owner = owner;
        parser = new SAXReader();
        parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        parser.setFeature("http://xml.org/sax/features/external-general-entities", false);
        parser.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        // TODO enable validation against XHTML 1.1 schema when in test mode (-ea)
    }

    public HtmlJellyScript parse(URL script, MetaClass owner) throws Exception {
        LOGGER.info(() -> "TODO parsing " + script + " from " + owner);
        Class<?> c = owner.klass.toJavaClass();
        var base = c.getProtectionDomain().getCodeSource().getLocation()
                + c.getName().replace('.', '/') + "/";
        for (var m : c.getMethods()) {
            var ann = m.getAnnotation(HtmlView.class);
            if (ann == null) {
                continue;
            }
            if (m.getParameterCount() > 0) {
                throw new Exception(m + " must not take arguments");
            }
            if (!m.getReturnType().isRecord()) {
                throw new Exception(m + " must return a record");
            }
            // TODO verify that the Record fields & nesting match st.* DOM structure
            // (or do this all at compile time using an annotation processor)
            if (script.toString().equals(base + ann.value() + ".xhtml")) {
                return new HtmlJellyScript(m, parser.read(script).getRootElement());
            }
        }
        throw new Exception(c + " does not have a @HtmlView corresponding to " + script);
    }
}
