package org.kohsuke.stapler.jelly;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.Tag;
import org.apache.commons.jelly.TagLibrary;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.impl.DynamicTag;
import org.apache.commons.jelly.impl.TagFactory;
import org.apache.commons.jelly.impl.TagScript;
import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.MetaClassLoader;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.net.URL;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * {@link TagLibrary} that loads tags from tag files in a directory.
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
    public final MetaClassLoader metaClassLoader;
    public final String nsUri;
    public final String basePath;

    /**
     * Compiled tag files.
     */
    private final Map<String,Script> scripts = new Hashtable<String,Script>();

    private final List<JellyTagFileLoader> loaders;

    public CustomTagLibrary(JellyContext master, ClassLoader classLoader, String nsUri, String basePath) {
        this.master = master;
        this.classLoader = classLoader;
        this.nsUri = nsUri;
        this.basePath = basePath;
        this.metaClassLoader = MetaClassLoader.get(classLoader);
        this.loaders = JellyTagFileLoader.discover(classLoader);
    }

    public TagScript createTagScript(String name, Attributes attributes) throws JellyException {
        final Script s = load(name);
        if(s==null) return null;

        return new TagScript(new TagFactory() {
            public Tag createTag(String name, Attributes attributes) {
                return CustomTagLibrary.this.createTag(name,s);
            }
        });
    }

    public Tag createTag(String name, Attributes attributes) throws JellyException {
        Script s = load(name);
        if(s==null)
            return null;
        return createTag(name,s);
    }

    /**
     * Wraps a {@link Script} into a tag.
     */
    private Tag createTag(String tagName, Script s) {
        if(JellyFacet.TRACE) {
            // trace execution
            final String source = "{jelly:"+nsUri+"}:"+tagName;
            return new StaplerDynamicTag(nsUri,tagName,s) {
                public void doTag(XMLOutput output) throws JellyTagException {
                    try {
                        String msg = "<" + source+">";
                        output.comment(msg.toCharArray(),0,msg.length());
                        super.doTag(output);
                        msg = "</" + source+">";
                        output.comment(msg.toCharArray(),0,msg.length());
                    } catch (SAXException e) {
                        throw new JellyTagException(e);
                    }
                }
            };
        }
        return new StaplerDynamicTag(nsUri,tagName,s);
    }

    /**
     * When &lt;d:invokeBody/> is used to call back into the calling script,
     * the Jelly name resolution rule is in such that the body is evaluated with
     * the variable scope of the &lt;d:invokeBody/> caller. This is very different
     * from a typical closure name resolution mechanism, where the body is evaluated
     * with the variable scope of where the body was created.
     *
     * <p>
     * More concretely, in Jelly, this often shows up as a problem as inability to
     * access the "attrs" variable from inside a body, because every {@link DynamicTag}
     * invocation sets this variable in a new scope.
     *
     * <p>
     * To couner this effect, this class temporarily restores the original "attrs"
     * when the body is evaluated. This makes the name resolution of 'attrs' work
     * like what programmers normally expect.
     *
     * <p>
     * The same problem also shows up as a lack of local variables &mdash; when a tag
     * calls into the body via &lt;d:invokeBody/>, the invoked body will see all the
     * variables that are defined in the caller, which is again not what a normal programming language
     * does. But unfortunately, changing this is too pervasive.
     */
    public static class StaplerDynamicTag extends DynamicTag {
        private final String nsUri;
        private final String localName;

        public StaplerDynamicTag(String nsUri, String localName, Script template) {
            super(template);
            this.nsUri = nsUri;
            this.localName = localName;
        }

        @Override
        public Script getBody() {
            final Script body = super.getBody();
            return new Script() {
                final JellyContext currentContext = getContext();

                public Script compile() throws JellyException {
                    return this;
                }

                public void run(JellyContext context, XMLOutput output) throws JellyTagException {
                    Map m = context.getVariables();
                    Object oldAttrs = m.put("attrs",currentContext.getVariable("attrs"));
                    try {
                        body.run(context,output);
                    } finally {
                        m.put("attrs",oldAttrs);
                    }
                }
            };
        }

        public String getNsUri() {
            return nsUri;
        }

        public String getLocalName() {
            return localName;
        }
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

        script=null;
        if(MetaClassLoader.debugLoader!=null)
            script = load(name, MetaClassLoader.debugLoader.loader);
        if(script==null)
            script = load(name, classLoader);
        return script;
    }

    private Script load(String name, ClassLoader classLoader) throws JellyException {
        Script script;
        URL res = classLoader.getResource(basePath + '/' + name + ".jelly");
        if(res!=null) {
            script = loadJellyScript(res);
            scripts.put(name,script);
            return script;
        }

        for (JellyTagFileLoader loader : loaders) {
            Script s = loader.load(this, name, classLoader);
            if(s!=null) {
                scripts.put(name,s);
                return s;
            }
        }

        return null;
    }

    private Script loadJellyScript(URL res) throws JellyException {
        // compile script
        JellyContext context = new CustomJellyContext(master);
        context.setClassLoader(classLoader);
        return context.compileScript(res);
    }
}
