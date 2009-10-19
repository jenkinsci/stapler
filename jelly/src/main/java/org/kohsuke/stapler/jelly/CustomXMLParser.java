package org.kohsuke.stapler.jelly;

import org.apache.commons.jelly.parser.XMLParser;
import org.apache.commons.jelly.expression.ExpressionFactory;
import org.apache.commons.jelly.expression.Expression;
import org.apache.commons.jelly.expression.ExpressionSupport;
import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyException;
import org.kohsuke.stapler.WebApp;

import java.net.URL;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Map;
import java.util.HashMap;

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

        public Expression createExpression(final String text) throws JellyException {
            if(text.startsWith("%")) {
                // this is a message resource reference
                return createI18nExp(text);
            } else {
                Matcher m = RESOURCE_LITERAL_STRING.matcher(text);
                if(m.find()) {
                    // contains the resource literal, so pre-process them.
                    
                    final StringBuilder buf = new StringBuilder();
                    final Map<String,InternationalizedStringExpression> resourceLiterals = new HashMap<String,InternationalizedStringExpression>();
                    int e=0;
                    do {
                        // copy the text preceding the match
                        buf.append(text.substring(e,m.start()));

                        String varName = "__resourceLiteral__"+resourceLiterals.size()+"__";
                        InternationalizedStringExpression exp = createI18nExp(unquote(m.group()));
                        resourceLiterals.put(varName,exp);

                        // replace the literal by the evaluation
                        buf.append(varName).append(".evaluate(context)");
                        e = m.end();
                    } while(m.find());

                    buf.append(text.substring(e));

                    return new ExpressionSupport() {
                        final Expression innerExpression = JellyClassLoaderTearOff.EXPRESSION_FACTORY.createExpression(buf.toString());
                        public String getExpressionText() {
                            return text;
                        }

                        public Object evaluate(JellyContext context) {
                            context = new CustomJellyContext(context);
                            context.setVariables(resourceLiterals);
                            return innerExpression.evaluate(context);
                        }
                    };
                }

                return JellyClassLoaderTearOff.EXPRESSION_FACTORY.createExpression(text);
            }
        }

        private InternationalizedStringExpression createI18nExp(String text) throws JellyException {
            return new InternationalizedStringExpression(getResourceBundle(),text);
        }

        private String unquote(String s) {
            return s.substring(1,s.length()-1);
        }

        private ResourceBundle getResourceBundle() {
            if(resourceBundle==null)
                resourceBundle = ResourceBundle.load(locator.getSystemId());
            return resourceBundle;
        }
    }

    // "%...."    string literal that starts with '%'
    private static final Pattern RESOURCE_LITERAL_STRING = Pattern.compile("(\"%[^\"]+\")|('%[^']+')");
}