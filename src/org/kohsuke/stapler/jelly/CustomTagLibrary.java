package org.kohsuke.stapler.jelly;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.Tag;
import org.apache.commons.jelly.TagLibrary;
import org.apache.commons.jelly.impl.DynamicTag;
import org.apache.commons.jelly.impl.TagFactory;
import org.apache.commons.jelly.impl.TagScript;
import org.kohsuke.stapler.MetaClass;
import org.xml.sax.Attributes;

import java.net.URL;
import java.util.Hashtable;
import java.util.Map;

/**
 * {@link TagLibrary} that loads tags from tag files in a given directory.
 *
 * @author Kohsuke Kawaguchi
 */
public final class CustomTagLibrary extends TagLibrary {

    /**
     * Inherits values from this context.
     * This context would be shared by multiple threads,
     * but as long as we all just read it, it should be OK.
     */
    private final JellyContext master;

    private final ClassLoader classLoader;
    private final String basePath;
    /**
     * Compiled tag files.
     */
    private final Map<String,Script> scripts = new Hashtable<String,Script>();

    public CustomTagLibrary(JellyContext master, ClassLoader classLoader, String basePath) {
        this.master = master;
        this.classLoader = classLoader;
        this.basePath = basePath;
    }

    public TagScript createTagScript(String name, Attributes attributes) throws JellyException {
        final Script s = load(name);
        if(s==null) return null;

        return new TagScript(new TagFactory() {
            public Tag createTag(String name, Attributes attributes) {
                return new DynamicTag(s);
            }
        });
    }

    public Tag createTag(String name, Attributes attributes) throws JellyException {
        Script s = load(name);
        if(s==null)
            return null;
        return new DynamicTag(s);
    }

    /**
     * Obtains the script for the given tag name. Loads if necessary.
     *
     * <p>
     * Synchronizing this method would have a potential race condition
     * if two threads try to load two tags that are referencing each other.
     *
     * <p>
     * So we synchronize {@link #scripts}, even though this means
     * we may end up compiling the same script twice.
     */
    private Script load(String name) throws JellyException {

        Script script = scripts.get(name);
        if(script!=null && !MetaClass.NO_CACHE)
            return script;

        URL res = classLoader.getResource(basePath + '/' + name + ".jelly");
        if(res==null)
            return null;

        // compile script
        JellyContext context = new JellyContext(master);
        context.setClassLoader(classLoader);
        script = context.compileScript(res);
        scripts.put(name,script);

        return script;
    }
}
