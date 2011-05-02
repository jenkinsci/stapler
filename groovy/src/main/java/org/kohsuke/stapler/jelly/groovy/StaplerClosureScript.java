package org.kohsuke.stapler.jelly.groovy;

import org.jvnet.localizer.LocaleProvider;
import org.kohsuke.stapler.jelly.ResourceBundle;

import java.net.URL;

/**
 * @author Kohsuke Kawaguchi
 */
public abstract class StaplerClosureScript extends GroovyClosureScript {
    /**
     * Where was this script loaded from?
     */
    protected URL scriptURL;

    /**
     * Resource bundle for messages in this page.
     * Lazily loaded on demand.
     */
    private volatile ResourceBundle resourceBundle;

    /**
     * Looks up the resource bundle with the given key, and returns that string,
     * or otherwise return 'text' as-is.
     */
    public String i18n(String text) {
        return i18n(text, EMPTY_ARRAY);
    }

    /**
     * Looks up the resource bundle with the given key, formats with arguments,
     * then return that formatted string.
     */
    public String i18n(String key, Object... args) {
//        JellyBuilder b = (JellyBuilder)getDelegate();

        ResourceBundle resourceBundle = getResourceBundle();

        // notify the listener if set
//        InternationalizedStringExpressionListener listener = (InternationalizedStringExpressionListener) Stapler.getCurrentRequest().getAttribute(LISTENER_NAME);
//        if(listener!=null)
//            listener.onUsed(this, args);

        // TODO: XSS prevention
        return resourceBundle.format(LocaleProvider.getLocale(), key, args);
    }

    private ResourceBundle getResourceBundle() {
        if (resourceBundle==null) {
            synchronized (this) {
                if (resourceBundle==null) {
                    String baseURL = scriptURL.toExternalForm();
                    baseURL = baseURL.substring(0,baseURL.lastIndexOf('.'));
                    resourceBundle = ResourceBundle.load(baseURL);
                }
            }
        }
        return resourceBundle;
    }

    private static final Object[] EMPTY_ARRAY = new Object[0];
}
