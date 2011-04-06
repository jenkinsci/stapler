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

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.XMLOutput;
import org.kohsuke.stapler.framework.adjunct.AdjunctManager;
import org.kohsuke.stapler.framework.adjunct.AdjunctsInPage;
import org.xml.sax.SAXException;
import org.jvnet.maven.jellydoc.annotation.NoContent;
import org.jvnet.maven.jellydoc.annotation.Required;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Writes out links to adjunct CSS and JavaScript, if not done so already.
 * 
 * @author Kohsuke Kawaguchi
 */
@NoContent
public class AdjunctTag extends AbstractStaplerTag {
    private String[] includes;
    private String[] assumes;

    /**
     * Comma-separated adjunct names.
     */
    public void setIncludes(String _includes) {
        includes = parse(_includes);
    }

    /**
     * Comma-separated adjunct names that are externally included in the page
     * and should be suppressed.
     */
    public void setAssumes(String _assumes) {
        assumes = parse(_assumes);
    }

    private String[] parse(String s) {
        String[] r = s.split(",");
        for (int i = 0; i < r.length; i++)
              r[i] = r[i].trim();
        return r;
    }

    public void doTag(XMLOutput out) throws JellyTagException {
        AdjunctManager m = AdjunctManager.get(getServletContext());
        if(m==null) {
            LOGGER.log(Level.WARNING,"AdjunctManager is not installed for this application. Skipping <adjunct> tags", new Exception());
            return;
        }

        try {
            AdjunctsInPage a = AdjunctsInPage.get();
            if (assumes!=null)
                a.assumeIncluded(assumes);
            if (includes!=null)
                a.generate(out, includes);
        } catch (IOException e) {
            throw new JellyTagException(e);
        } catch (SAXException e) {
            throw new JellyTagException(e);
        }
    }

    private static final Logger LOGGER = Logger.getLogger(AdjunctTag.class.getName());
}
