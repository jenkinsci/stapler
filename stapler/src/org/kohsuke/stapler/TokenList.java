package org.kohsuke.stapler;

import java.util.StringTokenizer;

/**
 * Tokenized strings.
 *
 * @author Kohsuke Kawaguchi
 */
final class TokenList {
    public final String[] tokens;
    /**
     * Index of the next token.
     */
    public int idx;

    TokenList(String url) {
        StringTokenizer tknzr = new StringTokenizer(url,"/");
        tokens = new String[tknzr.countTokens()];
        int i=0;
        while(tknzr.hasMoreTokens())
            tokens[i++] = tknzr.nextToken();
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
        StringBuffer buf = new StringBuffer();
        for( int i=0; i<tokens.length; i++) {
            if(buf.length()>0)  buf.append('/');
            if(i==idx)  buf.append('!');
            buf.append(tokens[i]);
        }
        return buf.toString();
    }

    public String assembleRestOfPath() {
        StringBuilder buf = new StringBuilder();
        for( int i=idx; i<length(); i++ ) {
            buf.append('/');
            buf.append(tokens[i]);
        }
        return buf.toString();
    }
}
