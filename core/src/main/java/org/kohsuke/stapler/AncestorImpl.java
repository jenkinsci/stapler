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

package org.kohsuke.stapler;

import java.util.List;

/**
 * @author Kohsuke Kawaguchi
 */
class AncestorImpl implements Ancestor {
    private final List<AncestorImpl> owner;
    private final int listIndex;

    private final Object object;
    private final String[] tokens;
    private final int index;
    private final String contextPath;

    /**
     * True if the request URL had '/' in the end.
     */
    private final boolean endsWithSlash;

    AncestorImpl(RequestImpl req, Object object) {
        this.owner = req.ancestors;
        listIndex = owner.size();
        owner.add(this);
        this.object = object;
        this.tokens = req.tokens.rawTokens;
        this.index = req.tokens.idx;
        this.endsWithSlash = req.tokens.endsWithSlash;
        this.contextPath = req.getContextPath();
    }

    public Object getObject() {
        return object;
    }

    public String getUrl() {
        StringBuilder buf = new StringBuilder(contextPath);
        for( int i=0; i<index; i++ ) {
            buf.append('/');
            buf.append(tokens[i]);
        }
        
        return buf.toString();
    }

    public String getRestOfUrl() {
        StringBuilder buf = new StringBuilder();
        for( int i=index; i<tokens.length; i++ ) {
            if (buf.length()>0) buf.append('/');
            buf.append(tokens[i]);
        }

        return buf.toString();
    }

    public String getFullUrl() {
        StringBuilder buf = new StringBuilder();
        StaplerRequest req = Stapler.getCurrentRequest();
        buf.append(req.getScheme());
        buf.append("://");
        buf.append(req.getServerName());
        if(req.getServerPort()!=80)
            buf.append(':').append(req.getServerPort());
        buf.append(getUrl());

        return buf.toString();
    }

    public String getRelativePath() {
        StringBuilder buf = new StringBuilder();
        for( int i=index+(endsWithSlash?0:1); i<tokens.length; i++ ) {
            if(buf.length()>0)  buf.append('/');
            buf.append("..");
        }
        if(buf.length()==0) buf.append('.');
        return buf.toString();
    }

    public String getNextToken(int n) {
        return tokens[index+n];
    }

    public Ancestor getPrev() {
        if(listIndex==0)
            return null;
        else
            return owner.get(listIndex-1);
    }

    public Ancestor getNext() {
        if(listIndex==owner.size()-1)
            return null;
        else
            return owner.get(listIndex+1);
    }

    @Override
    public String toString() {
        return object.toString();
    }
}
