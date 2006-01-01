package org.kohsuke.stapler;

import java.util.StringTokenizer;

/**
 * Tokenized strings.
 *
 * @author Kohsuke Kawaguchi
 */
final class TokenList {
    public final String[] tokens;
    public int idx;

    TokenList(String url) {
        StringTokenizer tknzr = new StringTokenizer(url,"/");
        tokens = new String[tknzr.countTokens()];
        int i=0;
        while(tknzr.hasMoreTokens())
            tokens[i++] = tknzr.nextToken();
    }

    public boolean hasMore() {
        return tokens.length==idx;
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
        int i = Integer.valueOf(peek());
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
}
