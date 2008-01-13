package org.kohsuke.stapler.jelly;

import org.apache.commons.jelly.expression.Expression;
import org.apache.commons.jelly.expression.ExpressionSupport;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.JellyContext;
import org.jvnet.localizer.LocaleProvider;

import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

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
    private final ResourceBundle resourceBundle;
    private final Expression[] arguments;
    private final String key;
    private final String expressionText;

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

    public Object evaluate(JellyContext jellyContext) {
        Object[] args = new Object[arguments.length];
        for (int i = 0; i < args.length; i++)
            args[i] = arguments[i].evaluate(jellyContext);
        return resourceBundle.format(LocaleProvider.getLocale(),key,args);
    }

    private static final Expression[] EMPTY_ARGUMENTS = new Expression[0];
}
