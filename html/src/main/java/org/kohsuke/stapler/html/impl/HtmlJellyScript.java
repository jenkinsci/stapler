package org.kohsuke.stapler.html.impl;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.XMLOutput;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXContentHandler;
import org.dom4j.io.SAXWriter;
import org.kohsuke.stapler.WebApp;
import org.kohsuke.stapler.html.HtmlInclude;
import org.kohsuke.stapler.jelly.JellyClassTearOff;

final class HtmlJellyScript implements Script {

    private static final Logger LOGGER = Logger.getLogger(HtmlJellyScript.class.getName());

    private final Method method;
    private final Element root;

    HtmlJellyScript(Method method, Element root) {
        this.method = method;
        this.root = root;
    }

    @Override
    public Script compile() throws JellyException {
        return this;
    }

    @Override
    public void run(JellyContext context, XMLOutput output) throws JellyTagException {
        var it = context.getVariable("it");
        if (it == null) {
            throw new JellyTagException("No `it` bound");
        }
        // TODO set thread name like JellyViewScript does
        try {
            var record = (Record) method.invoke(it);
            LOGGER.info(() -> "TODO " + method + " on " + it + " ⇒ " + record);
            // TODO find a more efficient way to render without allocation:
            var rendered = (Element) root.clone();
            render(context, rendered, record);
            new SAXWriter(output, output).write(rendered);
        } catch (Exception x) {
            throw new JellyTagException(x);
        }
    }

    /**
     * Render a view or subtree of a view.
     * {@link Visitor} cannot be used easily here because it lacks any way to prune a tree
     * or record entry and exit from an element.
     */
    private void render(JellyContext context, Element rendered, Record record) throws Exception {
        for (var field : record.getClass().getRecordComponents()) {
            var id = "st." + field.getName();
            var elt = find(rendered, id);
            if (elt == null) {
                throw new IllegalArgumentException("did not find " + id + " in " + rendered);
            }
            elt.remove(elt.attribute("id"));
            var value = field.getAccessor().invoke(record);
            var include = field.getAnnotation(HtmlInclude.class);
            if (include != null) {
                var metaClass = WebApp.getCurrent().getMetaClass(value.getClass());
                var script = metaClass.loadTearOff(JellyClassTearOff.class).findScript(include.value());
                var subcontext = new JellyContext(context);
                subcontext.setExportLibraries(false); // defaults to true, weirdly
                subcontext.setVariable("from", value);
                subcontext.setVariable("it", value);
                var handler = new SAXContentHandler();
                var oldCCL = Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader(metaClass.classLoader.loader);
                try {
                    script.run(subcontext, new XMLOutput(handler));
                } finally {
                    Thread.currentThread().setContextClassLoader(oldCCL);
                }
                // TODO honor JellyFacet.TRACE somehow
                replace(elt, List.of(handler.getDocument().getRootElement()));
            } else if (value instanceof String text) {
                // TODO why do we need to replace entities like this?
                // It is not necessary if run calls rendered.write(output.asWriter())
                // but that does not seem right because it would be just sending text content, not XML.
                // (And then includes do not work at all: the SAXContentHandler is left empty.)
                // Also using SAXWriter causes DefaultScriptInvoker.createXMLOutput to use HTMLWriterOutput
                // and thus dropping </p>, which does not match what actual Jenkins text/html output is like.
                // Retest in the context of Jenkins which might wrap things differently.
                elt.setText(text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"));
            } else if (value instanceof Boolean enabled) {
                if (!enabled) {
                    elt.detach();
                }
            } else if (value instanceof Record subrecord) {
                render(context, elt, subrecord);
            } else if (value instanceof List<?> list) {
                var replacements = new ArrayList<Element>();
                for (var item : list) {
                    var elt2 = (Element) elt.clone();
                    render(context, elt2, (Record) item);
                    replacements.add(elt2);
                }
                replace(elt, replacements);
            } else if (value == null) {
                elt.detach();
            } else {
                throw new IllegalArgumentException("did not recognize " + value);
            }
        }
    }

    /**
     * Like {@link Element#elementByID} except using attribute {@code id} not {@code ID}.
     */
    @CheckForNull
    private static Element find(Element elt, String id) {
        if (id.equals(elt.attributeValue("id"))) {
            return elt;
        }
        for (var child : elt.elements()) {
            var found = find(child, id);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    // TODO dom4j does not seem to define an insertAt or replace
    // also Element.node(int) does not work as documented
    private static void replace(Element elt, List<Element> replacements) {
        var parent = elt.getParent();
        var kids = new ArrayList<Node>();
        parent.nodeIterator().forEachRemaining(kids::add);
        kids.stream().forEach(Node::detach);
        int index = kids.indexOf(elt);
        kids.remove(index);
        kids.addAll(index, replacements);
        kids.stream().forEach(parent::add);
    }
}
