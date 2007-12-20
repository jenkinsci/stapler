package org.kohsuke.stapler.jelly;

import org.apache.commons.jelly.parser.XMLParser;
import org.apache.commons.jelly.expression.ExpressionFactory;
import org.apache.commons.jelly.JellyContext;

import java.net.URL;

/**
 * {@link XMLParser} that uses {@link JellyClassLoaderTearOff#EXPRESSION_FACTORY}
 * for expression parsing.
 *
 * @author Kohsuke Kawaguchi
*/
class CustomJellyContext extends JellyContext {
    CustomJellyContext() {
    }

    CustomJellyContext(URL url) {
        super(url);
    }

    CustomJellyContext(URL url, URL url1) {
        super(url, url1);
    }

    CustomJellyContext(JellyContext jellyContext) {
        super(jellyContext);
    }

    CustomJellyContext(JellyContext jellyContext, URL url) {
        super(jellyContext, url);
    }

    CustomJellyContext(JellyContext jellyContext, URL url, URL url1) {
        super(jellyContext, url, url1);
    }

    @Override
    protected XMLParser createXMLParser() {
        return new XMLParser() {
            @Override
            protected ExpressionFactory createExpressionFactory() {
                return JellyClassLoaderTearOff.EXPRESSION_FACTORY;
            }
        };
    }
}