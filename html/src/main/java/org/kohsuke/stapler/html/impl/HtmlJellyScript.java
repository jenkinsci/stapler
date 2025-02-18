package org.kohsuke.stapler.html.impl;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Logger;
import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.XMLOutput;
import org.dom4j.Element;

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
        try {
            var record = (Record) method.invoke(it);
            LOGGER.info(() -> "TODO " + method + " on " + it + " ⇒ " + record);
            // TODO find a more efficient way to render without allocation:
            var rendered = (Element) root.clone();
            render(rendered, record);
            rendered.write(output.asWriter());
        } catch (Exception x) {
            throw new JellyTagException(x);
        }
    }

    /**
     * Render a view or subtree of a view.
     * {@link Visitor} cannot be used easily here because it lacks any way to prune a tree
     * or record entry and exit from an element.
     */
    private void render(Element rendered, Record record) throws Exception {
        for (var field : record.getClass().getRecordComponents()) {
            var id = "st." + field.getName();
            var elt = find(rendered, id);
            if (elt == null) {
                throw new IllegalArgumentException("did not find " + id + " in " + rendered);
            }
            elt.remove(elt.attribute("id"));
            var value = field.getAccessor().invoke(record);
            if (value instanceof String text) {
                elt.setText(text);
            } else if (value instanceof Boolean enabled) {
                if (!enabled) {
                    elt.detach();
                }
            } else if (value instanceof Record subrecord) {
                render(elt, subrecord);
            } else if (value instanceof List<?> list) {
                var parent = elt.getParent();
                elt.detach();
                for (var item : list) {
                    var elt2 = (Element) elt.clone();
                    render(elt2, (Record) item);
                    parent.add(elt2);
                }
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
    private Element find(Element elt, String id) {
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
}
