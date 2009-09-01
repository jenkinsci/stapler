package org.kohsuke.stapler;

import java.util.StringTokenizer;

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
        StringTokenizer tknzr = new StringTokenizer(url,"/");
        tokens = new String[tknzr.countTokens()];
        rawTokens = new String[tknzr.countTokens()];
        for(int i=0; tknzr.hasMoreTokens(); i++) {
            rawTokens[i] = tknzr.nextToken();
            tokens[i] = decode(rawTokens[i]);
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
        StringBuffer buf = new StringBuffer();
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
        StringBuilder buf = new StringBuilder(s.substring(0, i));
        char c, upper, lower;
        for (int m = s.length(); i < m; i++) {
            c = s.charAt(i);
            if (c == '%') try {
                upper = s.charAt(++i);
                lower = s.charAt(++i);
                c = (char)(((upper & 0xF) + ((upper & 0x40) != 0 ? 9 : 0)) * 16
                           + ((lower & 0xF) + ((lower & 0x40) != 0 ? 9 : 0)));
            } catch (IndexOutOfBoundsException ignore) { }
            buf.append(c);
        }
        return buf.toString();
    }
}
