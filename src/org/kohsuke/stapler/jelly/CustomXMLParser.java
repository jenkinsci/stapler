package org.kohsuke.stapler.jelly;

import org.apache.commons.jelly.parser.XMLParser;
import org.apache.commons.jelly.expression.ExpressionFactory;
import org.apache.commons.jelly.expression.Expression;
import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyException;

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
        return new CustomXMLParser();
    }

    private static class CustomXMLParser extends XMLParser implements ExpressionFactory {
        private ResourceBundle resourceBundle;

        @Override
        protected ExpressionFactory createExpressionFactory() {
            return this;
        }

        public Expression createExpression(String text) throws JellyException {
            if(text.startsWith("%")) {
                if(resourceBundle==null) {
                    String scriptUrl = locator.getSystemId();
                    if(scriptUrl.endsWith(".jelly"))    // cut the trailing .jelly
                        scriptUrl = scriptUrl.substring(0,scriptUrl.length()-".jelly".length());
                    resourceBundle = new ResourceBundle(scriptUrl);
                }
    
                return new InternationalizedStringExpression(resourceBundle,text);
            } else {
                return JellyClassLoaderTearOff.EXPRESSION_FACTORY.createExpression(text);
            }
        }
    }
}