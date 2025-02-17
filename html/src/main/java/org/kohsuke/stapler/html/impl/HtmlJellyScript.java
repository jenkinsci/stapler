package org.kohsuke.stapler.html.impl;

import java.lang.reflect.Method;
import java.util.logging.Logger;
import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.XMLOutput;
import org.dom4j.Element;
import org.dom4j.VisitorSupport;
import org.kohsuke.stapler.html.HtmlViewRenderer;

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
        LOGGER.info(() -> "TODO running " + method + " on " + it);
        try {
            var renderer = (HtmlViewRenderer) method.invoke(it);
            // Could find a more efficient way to render without allocation:
            var rendered = (Element) root.clone();
            rendered.accept(new VisitorSupport() {
                @Override
                public void visit(Element node) {
                    var id = node.attributeValue("id");
                    if (id != null) {
                        String text;
                        try {
                            text = renderer.supplyText(id);
                        } catch (RuntimeException x) {
                            throw x;
                        } catch (Exception x) {
                            throw new RuntimeException(x);
                        }
                        if (text != null) {
                            node.addText(text);
                        }
                    }
                }
            });
            rendered.write(output.asWriter());
        } catch (Exception x) {
            throw new JellyTagException(x);
        }
    }
}
