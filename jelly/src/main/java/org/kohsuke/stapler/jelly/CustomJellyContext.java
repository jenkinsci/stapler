/*
 * Copyright (c) 2004-2010, Kohsuke Kawaguchi
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this list of
 *       conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
 * THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.kohsuke.stapler.jelly;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.beanutils.SuppressPropertiesBeanIntrospector;
import org.apache.commons.jelly.parser.XMLParser;
import org.apache.commons.jelly.expression.ExpressionFactory;
import org.apache.commons.jelly.expression.Expression;
import org.apache.commons.jelly.expression.ExpressionSupport;
import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.TagLibrary;
import org.kohsuke.stapler.MetaClassLoader;

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
    private JellyClassLoaderTearOff jclt;

    CustomJellyContext() {
        init();
    }

    CustomJellyContext(URL url) {
        super(url);
        init();
    }

    CustomJellyContext(URL url, URL url1) {
        super(url, url1);
        init();
    }

    CustomJellyContext(JellyContext jellyContext) {
        super(jellyContext);
        init();
    }

    CustomJellyContext(JellyContext jellyContext, URL url) {
        super(jellyContext, url);
        init();
    }

    CustomJellyContext(JellyContext jellyContext, URL url, URL url1) {
        super(jellyContext, url, url1);
        init();
    }

    private void init() {
        // by not allowing the empty namespace URI "" to be handled as dynamic tags,
        // we achieve substantial performance improvement.
        registerTagLibrary("",ReallyStaticTagLibrary.INSTANCE);
        registerTagLibrary("this",ThisTagLibrary.INSTANCE);

        if (DISABLE_BEANUTILS_CLASS_SUPPRESSION) {
            /* In case our existing workarounds in StaplerTagLibrary for IncludeTag aren't enough */
            PropertyUtilsBean propertyUtilsBean = BeanUtilsBean.getInstance().getPropertyUtils();
            propertyUtilsBean.removeBeanIntrospector(SuppressPropertiesBeanIntrospector.SUPPRESS_CLASS);
        }
    }

    @Override
    protected XMLParser createXMLParser() {
        return new CustomXMLParser();
    }

    @Override
    public void setClassLoader(ClassLoader classLoader) {
        super.setClassLoader(classLoader);
        jclt = MetaClassLoader.get(classLoader).loadTearOff(JellyClassLoaderTearOff.class);
    }

    @Override
    public TagLibrary getTagLibrary(String namespaceURI) {
        TagLibrary tl = super.getTagLibrary(namespaceURI);

        // delegate to JellyClassLoaderTearOff for taglib handling
        if(tl==null && jclt!=null) {
            tl = jclt.getTagLibrary(namespaceURI);
            if (tl!=null)
                registerTagLibrary(namespaceURI,tl);
        }
        return tl;
    }

    public static /* final */ boolean ESCAPE_BY_DEFAULT = Boolean.valueOf(System.getProperty(CustomJellyContext.class.getName() + ".escapeByDefault", "true"));

    private static final boolean DISABLE_BEANUTILS_CLASS_SUPPRESSION = Boolean.getBoolean(CustomJellyContext.class.getName() + ".disableBeanUtilsClassSuppression");

    private static class CustomXMLParser extends XMLParser implements ExpressionFactory {
        private ResourceBundle resourceBundle;
        @Override
        protected ExpressionFactory createExpressionFactory() {
            return this;
        }

        CustomXMLParser() {
            this.setEscapeByDefault(ESCAPE_BY_DEFAULT);
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
                    final Map<String,InternationalizedStringExpression> resourceLiterals = new HashMap<>();
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

                    return new I18nExpWithArgsExpression(text, resourceLiterals, buf.toString());
                }

                return JellyClassLoaderTearOff.EXPRESSION_FACTORY.createExpression(text);
            }
        }

        private InternationalizedStringExpression createI18nExp(String text) throws JellyException {
            return new InternationalizedStringExpression(getResourceBundle(),text);
        }

        @Override
        protected Expression createEscapingExpression(Expression exp) {
            if ( exp instanceof InternationalizedStringExpression) {
                InternationalizedStringExpression i18nexp = (InternationalizedStringExpression) exp;
                return i18nexp.makeEscapingExpression();
            }
            return super.createEscapingExpression(exp);
        }

        private String unquote(String s) {
            return s.substring(1,s.length()-1);
        }

        private ResourceBundle getResourceBundle() {
            if(resourceBundle==null)
                resourceBundle = ResourceBundle.load(locator.getSystemId());
            return resourceBundle;
        }

        /**
         * {@link Expression} that handles things like "%foo(a,b,c)"
         */
        private static class I18nExpWithArgsExpression extends ExpressionSupport {
            final Expression innerExpression;
            private final String text;
            private final Map<String, InternationalizedStringExpression> resourceLiterals;

            public I18nExpWithArgsExpression(String text, Map<String, InternationalizedStringExpression> resourceLiterals, String exp) throws JellyException {
                this.text = text;
                this.resourceLiterals = resourceLiterals;
                innerExpression = JellyClassLoaderTearOff.EXPRESSION_FACTORY.createExpression(exp);
            }

            public String getExpressionText() {
                return text;
            }

            public Object evaluate(JellyContext context) {
                context = new CustomJellyContext(context);
                context.setVariables(resourceLiterals);
                return innerExpression.evaluate(context);
            }
        }
    }

    // "%...."    string literal that starts with '%'
    private static final Pattern RESOURCE_LITERAL_STRING = Pattern.compile("(\"%[^\"]+\")|('%[^']+')");
}