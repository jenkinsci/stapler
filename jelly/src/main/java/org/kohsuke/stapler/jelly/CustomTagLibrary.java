package org.kohsuke.stapler.jelly;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.Tag;
import org.apache.commons.jelly.TagLibrary;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.expression.Expression;
import org.apache.commons.jelly.impl.DynamicTag;
import org.apache.commons.jelly.impl.ExpressionAttribute;
import org.apache.commons.jelly.impl.TagScript;
import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.MetaClassLoader;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.net.URL;
import java.util.HashMap;
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
        final Script def = load(name);
        if(def==null) return null;

        return new TagScript() {
            @Override
            public void run(final JellyContext context, XMLOutput output) throws JellyTagException {
                // evaluated values of the attributes
                Map args = new HashMap(attributes.size());

                for (Map.Entry<String, ExpressionAttribute> e : attributes.entrySet()) {
                    Expression expression = e.getValue().exp;
                    args.put(e.getKey(),expression.evaluate(context));
                }

                // create new context based on current attributes
                JellyContext newJellyContext = context.newJellyContext(args);
                newJellyContext.setVariable( "attrs", args );

                // <d:invokeBody> uses this to discover what to invoke
                newJellyContext.setVariable("org.apache.commons.jelly.body", new Script() {
                    public Script compile() throws JellyException {
                        return this;
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
                     * To counter this effect, this class temporarily restores the original "attrs"
                     * when the body is evaluated. This makes the name resolution of 'attrs' work
                     * like what programmers normally expect.
                     *
                     * <p>
                     * The same problem also shows up as a lack of local variables &mdash; when a tag
                     * calls into the body via &lt;d:invokeBody/>, the invoked body will see all the
                     * variables that are defined in the caller, which is again not what a normal programming language
                     * does. But unfortunately, changing this is too pervasive.
                     */
                    public void run(JellyContext nestedContext, XMLOutput output) throws JellyTagException {
                        Map m = nestedContext.getVariables();
                        Object oldAttrs = m.put("attrs",context.getVariable("attrs"));
                        try {
                            getTagBody().run(nestedContext,output);
                        } finally {
                            m.put("attrs",oldAttrs);
                        }
                    }
                });
                newJellyContext.setVariable("org.apache.commons.jelly.body.scope", context);

                if(JellyFacet.TRACE) {
                    try {
                        String source = "{jelly:"+nsUri+"}:"+getLocalName();
                        String msg = "<" + source+">";
                        output.comment(msg.toCharArray(),0,msg.length());
                        def.run(newJellyContext, output);
                        msg = "</" + source+">";
                        output.comment(msg.toCharArray(),0,msg.length());
                    } catch (SAXException e) {
                        throw new JellyTagException(e);
                    }
                } else {
                    def.run(newJellyContext, output);
                }
            }
        };
    }

    public Tag createTag(String name, Attributes attributes) throws JellyException {
        // IIUC, this method is only used by static tag to discover the correct tag at runtime,
        // and since stapler taglibs are always resolved statically, we shouldn't have to implement this method
        // at all.

        // by not implementing this method, we can put all the login in the TagScript-subtype, which eliminates
        // the need of stateful Tag instances and their overheads.
        return null;
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
