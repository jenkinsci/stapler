package org.kohsuke.stapler.jelly;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.XMLOutput;

import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.Set;

/**
 * Tag that only evaluates its body once during the entire request processing.
 *
 * @author Kohsuke Kawaguchi
 */
public class OnceTag extends AbstractStaplerTag {

    public void doTag(XMLOutput xmlOutput) throws JellyTagException {
        HttpServletRequest request = getRequest();
        Set<Script> executedScripts = (Set<Script>) request.getAttribute(KEY);
        if(executedScripts==null)
            request.setAttribute(KEY,executedScripts=new HashSet<Script>());
        if(executedScripts.add(getBody())) {
            getBody().run(getContext(),xmlOutput);
        }
    }

    private static final String KEY = OnceTag.class.getName();
}
