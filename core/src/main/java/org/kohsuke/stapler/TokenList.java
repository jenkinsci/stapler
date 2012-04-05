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

import java.util.StringTokenizer;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

/**
 * Tokenized strings.
 *
 * @author Kohsuke Kawaguchi
 */
public final class TokenList {
    public final String[] tokens, rawTokens;
    /**
     * Index of the next token.
     */
    public int idx;

    TokenList(String url) {
        StringTokenizer tknzr = new StringTokenizer(url,"/\\");
        final int tokenCount = tknzr.countTokens();
        tokens = new String[tokenCount];
        rawTokens = new String[tokenCount];
        for(int i=0; tknzr.hasMoreTokens(); i++) {
            rawTokens[i] = tknzr.nextToken();
            tokens[i] = decode(rawTokens[i]);
            if (tokens[i].equals(".."))
                throw new IllegalArgumentException(url);
        }
    }

    public boolean hasMore() {
        return tokens.length!=idx;
    }

    public String peek() {
        if(hasMore())
            return tokens[idx];
        else
            return null;
    }

    public String next() {
        return tokens[idx++];
    }
    public String prev() {
        return tokens[--idx];
    }
    public int nextAsInt() throws NumberFormatException {
        String p = peek();
        if(p==null)
            throw new NumberFormatException();  // no more token
        int i = Integer.valueOf(p);
        idx++;
        return i;
    }

    public int length() {
        return tokens.length;
    }

    public String get(int i) {
        return tokens[i];
    }

    public int countRemainingTokens() {
        return length()-idx;
    }


    public String toString() {
        StringBuilder buf = new StringBuilder();
        for( int i=0; i<tokens.length; i++) {
            if(buf.length()>0)  buf.append('/');
            if(i==idx)  buf.append('!');
            buf.append(tokens[i]);
        }
        return buf.toString();
    }

    private String assembleRestOfPath(String[] tokens) {
        StringBuilder buf = new StringBuilder();
        for( int i=idx; i<length(); i++ ) {
            buf.append('/');
            buf.append(tokens[i]);
        }
        return buf.toString();
    }

    public String assembleRestOfPath() {
        return assembleRestOfPath(tokens);
    }

    public String assembleOriginalRestOfPath() {
        return assembleRestOfPath(rawTokens);
    }

    public static String decode(String s) {
        int i = s.indexOf('%');
        if (i < 0) return s;

        try {
            // to properly handle non-ASCII characters, decoded bytes need to be stored and translated in bulk.
            // this complex set up is necessary for us to work gracefully if 's' already contains decoded non-ASCII chars.
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            StringBuilder buf = new StringBuilder(s.substring(0, i));
            char c, upper, lower;
            for (int m = s.length(); i < m; i++) {
                c = s.charAt(i);
                if (c == '%') {
                    try {
                        upper = s.charAt(++i);
                        lower = s.charAt(++i);
                        baos.write(fromHex(upper) * 16 + fromHex(lower));
                    } catch (IndexOutOfBoundsException ignore) {
                        // malformed %HH.
                    }
                } else {
                    if (baos.size()>0) {
                        buf.append(new String(baos.toByteArray(),"UTF-8"));
                        baos.reset();
                    }
                    buf.append(c);
                }
            }
            if (baos.size()>0)
                buf.append(new String(baos.toByteArray(),"UTF-8"));
            return buf.toString();
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e); // UTF-8 is mandatory encoding
        }
    }

    private static int fromHex(char upper) {
        return ((upper & 0xF) + ((upper & 0x40) != 0 ? 9 : 0));
    }
}
