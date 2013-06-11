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

package org.kohsuke.stapler.framework.adjunct;

import org.apache.commons.jelly.XMLOutput;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This request-scope object keeps track of which {@link Adjunct}s are already included.
 *
 * @author Kohsuke Kawaguchi
 */
public class AdjunctsInPage {
    private final AdjunctManager manager;
    /**
     * All adjuncts that are already included in the page.
     */
    private final Set<String> included = new HashSet<String>();

    /**
     * {@link Adjunct}s that haven't written to HTML yet because &lt;head>
     * tag hasn't been written yet.
     */
    private final List<Adjunct> pending = new ArrayList<Adjunct>();
    
    private final StaplerRequest request;

    /**
     * Obtains the instance associated with the current request of the given {@link StaplerRequest}.
     */
    public static AdjunctsInPage get() {
        return get(Stapler.getCurrentRequest());
    }
    /**
     * Obtains the instance associated with the current request of the given {@link StaplerRequest}.
     *
     * <p>
     * This method is handy when the caller already have the request object around,
     * so that we can save {@link Stapler#getCurrentRequest()} call.
     */
    public static AdjunctsInPage get(StaplerRequest request) {
        AdjunctsInPage aip = (AdjunctsInPage) request.getAttribute(KEY);
        if(aip==null)
            request.setAttribute(KEY,aip=new AdjunctsInPage(AdjunctManager.get(request.getServletContext()),request));
        return aip;
    }

    private AdjunctsInPage(AdjunctManager manager,StaplerRequest request) {
        this.manager = manager;
        this.request = request;
    }

    /**
     * Gets what has been already included/assumed.
     *
     * This method returns a live unmodifiable view of what's included.
     * So if at some later point more adjuncts are loaded, the view
     * obtained earlier will reflect that.
     */
    public Set<String> getIncluded() {
        return Collections.unmodifiableSet(included);
    }

    /**
     * Checks if something has already been included/assumed.
     */
    public boolean isIncluded(String include) {
        return included.contains(include);
    }

    /**
     * Generates the script tag and CSS link tag to include necessary adjuncts,
     * and records the fact that those adjuncts are already included in the page,
     * so that it won't be loaded again.
     */
    public void generate(XMLOutput out, String... includes) throws IOException, SAXException {
        List<Adjunct> needed = new ArrayList<Adjunct>();
        for (String include : includes)
            findNeeded(include,needed);

        for (Adjunct adj : needed)
            adj.write(request,out);
    }

    /**
     * When you include your version of the adjunct externally, you can use
     * this method to inform {@link AdjunctsInPage} that those adjuncts are
     * already included in the page.
     */
    public void assumeIncluded(String... includes) throws IOException, SAXException {
        assumeIncluded(Arrays.asList(includes));
    }

    public void assumeIncluded(Collection<String> includes) throws IOException, SAXException {
        List<Adjunct> needed = new ArrayList<Adjunct>();
        for (String include : includes)
            findNeeded(include,needed);
    }

    /**
     * Works like the {@link #generate(XMLOutput, String...)} method
     * but just put the adjuncts to {@link #pending} without writing it.
     */
    public void spool(String... includes) throws IOException, SAXException {
        for (String include : includes)
            findNeeded(include,pending);
    }

    /**
     * Writes out what's spooled by {@link #spool(String...)} method.
     */
    public void writeSpooled(XMLOutput out) throws SAXException, IOException {
        for (Adjunct adj : pending)
            adj.write(request,out);
        pending.clear();
    }

    /**
     * Builds up the needed adjuncts into the 'needed' list.
     */
    private void findNeeded(String include, List<Adjunct> needed) throws IOException {
        if(!included.add(include))
            return; // already sent

        // list dependencies first
        try {
            Adjunct a = manager.get(include);
            for (String req : a.required)
                findNeeded(req,needed);
            needed.add(a);
        } catch (NoSuchAdjunctException e) {
            LOGGER.log(Level.WARNING, "No such adjunct found: "+include,e);
        }
    }

    private static final String KEY = AdjunctsInPage.class.getName();

    private static final Logger LOGGER = Logger.getLogger(AdjunctsInPage.class.getName());
}
