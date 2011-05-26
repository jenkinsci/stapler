package org.kohsuke.stapler.jelly.jruby;

import org.apache.commons.beanutils.ConvertingWrapDynaBean;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.jelly.DynaTag;
import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.Tag;
import org.apache.commons.jelly.TagLibrary;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.expression.ConstantExpression;
import org.apache.commons.jelly.expression.Expression;
import org.apache.commons.jelly.impl.TagScript;
import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.RubyObject;
import org.jruby.RubyProc;
import org.jruby.RubySymbol;
import org.jruby.embed.ScriptingContainer;
import org.jruby.runtime.Block;
import org.jruby.runtime.builtin.IRubyObject;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Kohsuke Kawaguchi
 */
public abstract class JRubyJellyScript implements Script {
//    private final ScriptingContainer jruby;


    protected JRubyJellyScript() {
    }

    public Script compile() throws JellyException {
        return this;
    }

    // this method is implemented in Ruby
    public abstract void run(JellyContext context, XMLOutput output) throws JellyTagException;

    public void invokeTaglib(final IJRubyContext rcon, JellyContext context, XMLOutput output, String uri, String localName, Map<RubySymbol,?> attributes, final RubyProc proc) throws JellyException {
        TagScript tagScript = createTagScript(context, uri, localName);

        if (attributes!=null) {
            for (Entry<RubySymbol, ?> e : attributes.entrySet()) {
                tagScript.addAttribute(e.getKey().asJavaString(), new ConstantExpression(e.getValue()));
            }
        }
        
        if (proc!=null) {
            final Ruby runtime = ((IRubyObject)rcon).getRuntime();

            tagScript.setTagBody(new Script() {
                public Script compile() throws JellyException {
                    return this;
                }

                public void run(JellyContext context, XMLOutput output) throws JellyTagException {
                    JellyContext oc = rcon.getJellyContext();
                    XMLOutput oo = rcon.getOutput();
                    try {
                        rcon.setJellyContext(context);
                        rcon.setOutput(output);
                        proc.getBlock().yield(runtime.getCurrentContext(),null);
                    } finally {
                        rcon.setJellyContext(oc);
                        rcon.setOutput(oo);
                    }
                }
            });
        }
        tagScript.run(context, output);
    }

    private TagScript createTagScript(JellyContext context, String uri, String name) throws JellyException {
        TagLibrary lib = context.getTagLibrary(uri);
        if (lib==null)
            throw new JellyException("Undefined tag library namespace URI: "+uri);
        TagScript tagScript = lib.createTagScript(name, null/*this parameter appears to be unused.*/);
        if (tagScript!=null) return tagScript;
        tagScript = lib.createTagScript(name.replace('_','-'), null);
        if (tagScript!=null) return tagScript;
        throw new JellyException(String.format("name '%s' not found for '%s'", name, uri));
    }
}
