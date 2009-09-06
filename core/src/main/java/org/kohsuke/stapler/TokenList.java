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
