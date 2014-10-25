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

import org.apache.commons.jelly.expression.Expression;
import org.apache.commons.jelly.expression.ExpressionSupport;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.JellyContext;
import org.jvnet.localizer.LocaleProvider;
import org.kohsuke.stapler.Stapler;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;

/**
 * Expression of the form "%messageName(arg1,arg2,...)" that represents
 * internationalized text.
 *
 * <p>
 * The "(arg1,...)" portion is optional and can be ommitted. Each argument
 * is assumed to be a parenthesis-balanced expression and passed to
 * {@link JellyClassLoaderTearOff#EXPRESSION_FACTORY} to be parsed.
 *
 * <p>
 * The message resource is loaded from files like "xyz.properties" and
 * "xyz_ja.properties" when the expression is placed in "xyz.jelly". 
 *
 *
 * @author Kohsuke Kawaguchi
 */
public class InternationalizedStringExpression extends ExpressionSupport {
    public final ResourceBundle resourceBundle;
    private final Expression[] arguments;
    public final String key;
    public final String expressionText;

    public InternationalizedStringExpression(ResourceBundle resourceBundle, String text) throws JellyException {
        this.resourceBundle = resourceBundle;
        this.expressionText = text;
        if(!text.startsWith("%"))
            throw new JellyException(text+" doesn't start with %");
        text = text.substring(1);

        int idx = text.indexOf('(');
        if(idx<0) {
            // no arguments
            key = text;
            arguments = EMPTY_ARGUMENTS;
            return;
        }

        List<Expression> args = new ArrayList<Expression>();
        key = text.substring(0,idx);
        text = text.substring(idx+1);   // at this point text="arg,arg)"
        while(text.length()>0) {
            String token = tokenize(text);
            args.add(JellyClassLoaderTearOff.EXPRESSION_FACTORY.createExpression(token));
            text = text.substring(token.length()+1);
        }

        this.arguments = args.toArray(new Expression[args.size()]);
    }
    
    public List<Expression> getArguments() {
        return Collections.unmodifiableList(Arrays.asList(arguments));
    }

    /**
     * Takes a string like "arg)" or "arg,arg,...)", then
     * find "arg" and returns it.
     *
     * Note: this code is also copied into idea-stapler-plugin,
     * so don't forget to update that when this code changes.
     */
    private String tokenize(String text) throws JellyException {
        int parenthesis=0;
        for(int idx=0;idx<text.length();idx++) {
            char ch = text.charAt(idx);
            switch (ch) {
            case ',':
                if(parenthesis==0)
                    return text.substring(0,idx);
                break;
            case '(':
            case '{':
            case '[':
                parenthesis++;
                break;
            case ')':
                if(parenthesis==0)
                    return text.substring(0,idx);
                // fall through
            case '}':
            case ']':
                parenthesis--;
                break;
            case '"':
            case '\'':
                // skip strings
                idx = text.indexOf(ch,idx+1);
                break;
            }
        }
        throw new JellyException(expressionText+" is missing ')' at the end");
    }

    public String getExpressionText() {
        return expressionText;
    }

    public Object evaluate(JellyContext context) {
        return format(evaluateArguments(context));
    }

    private Object format(Object[] args) {
        // notify the listener if set
        InternationalizedStringExpressionListener listener = (InternationalizedStringExpressionListener) Stapler.getCurrentRequest().getAttribute(LISTENER_NAME);
        if(listener!=null)
            listener.onUsed(this, args);

        return resourceBundle.format(LocaleProvider.getLocale(), key, args);
    }

    private Object[] evaluateArguments(JellyContext jellyContext) {
        Object[] args = new Object[arguments.length];
        for (int i = 0; i < args.length; i++)
            args[i] = arguments[i].evaluate(jellyContext);
        return args;
    }

    /**
     * Creates a new {@link Expression} that performs proper HTML escaping.
     */
    public Expression makeEscapingExpression() {
        return new ExpressionSupport() {
            public String getExpressionText() {
                return expressionText;
            }

            public Object evaluate(JellyContext context) {
                Object[] args = evaluateArguments(context);
                args = Stapler.htmlSafeArguments(args);
                return format(args);
            }
        };
    }

    /**
     * @deprecated Use {@link org.kohsuke.stapler.RawHtmlArgument}
     */
    public static final class RawHtmlArgument extends org.kohsuke.stapler.RawHtmlArgument {
        public RawHtmlArgument(Object value) {
            super(value);
        }
    }

    private static final Expression[] EMPTY_ARGUMENTS = new Expression[0];
    private static final String LISTENER_NAME = InternationalizedStringExpressionListener.class.getName();
}
