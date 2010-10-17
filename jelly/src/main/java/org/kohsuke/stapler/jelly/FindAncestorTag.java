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
import org.apache.commons.jelly.NamespaceAwareTag;
import org.apache.commons.jelly.Tag;
import org.apache.commons.jelly.XMLOutput;
import org.jvnet.maven.jellydoc.annotation.NoContent;

import java.util.Map;

/**
 * Finds the nearest tag (in the call stack) that has the given tag name,
 * and sets that as a variable.
 *
 * @author Kohsuke Kawaguchi
 */
@NoContent
public class FindAncestorTag extends AbstractStaplerTag implements NamespaceAwareTag {
    private String tag;

    private String var;

    private Map nsMap;

    /**
     * Variable name to set the discovered {@link Tag} object.
     */
    public void setVar(String var) {
        this.var = var;
    }

    /**
     * QName of the tag to look for.
     */
    public void setTag(String tag) {
        this.tag = tag;
    }

    public void doTag(XMLOutput output) throws JellyTagException {
        // I don't think anyone is using this, but if we need to resurrect this,
        // we need to tweak CustomTagLibrary class and build up the stack of elements being processed.
        throw new UnsupportedOperationException();

//        int idx = tag.indexOf(':');
//        String prefix = tag.substring(0,idx);
//        String localName = tag.substring(idx+1);
//
//        String uri = (String) nsMap.get(prefix);
//
//        Tag tag = this;
//        while((tag=findAncestorWithClass(tag,StaplerDynamicTag.class))!=null) {
//            StaplerDynamicTag t = (StaplerDynamicTag)tag;
//            if(t.getLocalName().equals(localName) && t.getNsUri().equals(uri))
//                break;
//        }
//        getContext().setVariable(var,tag);
    }

    public void setNamespaceContext(Map prefixToUriMap) {
        this.nsMap = prefixToUriMap;
    }
}
