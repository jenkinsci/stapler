package org.kohsuke.stapler.jelly;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.Tag;
import org.apache.commons.jelly.TagLibrary;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.expression.ConstantExpression;
import org.apache.commons.jelly.expression.Expression;
import org.apache.commons.jelly.impl.ExpressionAttribute;
import org.apache.commons.jelly.impl.StaticTag;
import org.apache.commons.jelly.impl.StaticTagScript;
import org.apache.commons.jelly.impl.TagScript;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.util.Map;

/**
 * Jelly tag library for static tags.
 *
 * <p>
 * Unlike {@link StaticTagScript}, this doesn't even try to see if the tag name is available as a dynamic tag.
 * By not doing so, this implementation achieves a better performance both in speed and memory usage. 
 *
 * <p>
 * Jelly by default uses {@link StaticTagScript} instance to represent a tag that's parsed as a static tag,
 * and for each invocation, this code checks if the tag it represents is now defined as a dynamic tag.
 * Plus it got the code to cache {@link StaticTag} instances per thread, which consumes more space and time.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.342
 */
public class ReallyStaticTagLibrary extends TagLibrary {
    /**
     * IIUC, this method will never be invoked.
     */
    @Override
    public Tag createTag(final String name, Attributes attributes) throws JellyException {
        return null;
    }

    @Override
    public TagScript createTagScript(String tagName, Attributes atts) throws JellyException {
        return new TagScript() {
            /**
             * If all the attributes are constant, as is often the case with literal tags,
             * then we can skip the attribute expression evaluation altogether.
             */
            private boolean allAttributesAreConstant = true;
            
            @Override
            public void addAttribute(String name, Expression expression) {
                allAttributesAreConstant &= expression instanceof ConstantExpression;
                super.addAttribute(name, expression);
            }

            @Override
            public void addAttribute(String name, String prefix, String nsURI, Expression expression) {
                allAttributesAreConstant &= expression instanceof ConstantExpression;
                super.addAttribute(name, prefix, nsURI, expression);
            }

            public void run(JellyContext context, XMLOutput output) throws JellyTagException {
                Attributes actual = allAttributesAreConstant ? getSaxAttributes() : buildAttributes(context);

                try {
                    output.startElement(getLocalName(),actual);
                    getTagBody().run(context,output);
                    output.endElement(getLocalName());
                } catch (SAXException x) {
                    throw new JellyTagException(x);
                }
            }

            private AttributesImpl buildAttributes(JellyContext context) {
                AttributesImpl actual = new AttributesImpl();

                for (Map.Entry<String, ExpressionAttribute> e : attributes.entrySet()) {
                    String name = e.getKey();
                    Expression expression = e.getValue().exp;
                    actual.addAttribute("",name,name,"CDATA",expression.evaluateAsString(context));
                }
                return actual;
            }
        };
    }

    public static final TagLibrary INSTANCE = new ReallyStaticTagLibrary();
}
