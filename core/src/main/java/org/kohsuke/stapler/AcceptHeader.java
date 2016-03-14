/*
Copyright (c) 2010 Joe Gregorio

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */
package org.kohsuke.stapler;

import org.apache.commons.beanutils.Converter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the <tt>Accept</tt> HTTP header and help server choose the right media type to serve.
 *
 * <p>
 * Typical usage:
 * </p>
 * <pre>
 * HttpResponse doXyz(&#64;Header("Accept") AcceptHeader accept, ...) {
 *     swtich (accept.select("application/json","text/xml")) {
 *     case "application/json":
 *         ...
 *     case "text/html":
 *         ...
 *     }
 * }
 * </pre>
 *
 * <p>
 * A port to Java of Joe Gregorio's MIME-Type Parser: http://code.google.com/p/mimeparse/
 * Ported by Tom Zellman &lt;tzellman@gmail.com>.
 *
 * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.1">definition of Accept header</a>
 */
public final class AcceptHeader {
    private final List<Atom> atoms = new ArrayList<Atom>();
    private final String ranges;

    /**
     * Parse the accept header value into a typed object.
     *
     * @param ranges
     *      something like "text/*;q=0.5,*; q=0.1"
     */
    public AcceptHeader(String ranges) {
        this.ranges = ranges;
        for (String r : StringUtils.split(ranges, ','))
            atoms.add(new Atom(r));
    }

    /**
     * Media range plus parameters and extensions
     */
    protected static class Atom {
        private final String major;
        private final String minor;
        private final Map<String, String> params = new HashMap<String, String>();

        private final float q;

        @Override
        public String toString() {
            StringBuilder s = new StringBuilder(major +'/'+ minor);
            for (String k : params.keySet())
                s.append(";").append(k).append("=").append(params.get(k));
            return s.toString();
        }

        /**
         * Parses a string like 'application/*;q=0.5' into a typed object.
         */
        protected Atom(String range) {
            String[] parts = StringUtils.split(range, ";");

            for (int i = 1; i < parts.length; ++i) {
                String p = parts[i];
                String[] subParts = StringUtils.split(p, '=');
                if (subParts.length == 2)
                    params.put(subParts[0].trim(), subParts[1].trim());
            }
            String fullType = parts[0].trim();

            // Java URLConnection class sends an Accept header that includes a
            // single "*" - Turn it into a legal wildcard.
            if (fullType.equals("*"))
                fullType = "*/*";
            String[] types = StringUtils.split(fullType, "/");
            major = types[0].trim();
            minor = types[1].trim();

            float q = NumberUtils.toFloat(params.get("q"), 1);
            if (q < 0 || q > 1)
                q = 1;
            this.q = q;

            params.remove("q"); // normalize this away as this gets in the fitting
        }

        /**
         * Consider the score of fitness between two Atoms.
         *
         * <p>
         * Higher fitness means better match. For example, "text/html;level=1" fits "text/html" better
         * than "text/*", which still fits better than "* /*"
         */
        private int fit(Atom that) {
            if (!wildcardMatch(that.major, this.major) || !wildcardMatch(that.minor, this.minor))
                return -1;

            int fitness;
            fitness = (this.major.equals(that.major)) ? 10000 : 0;
            fitness += (this.minor.equals(that.minor)) ? 1000 : 0;

            // parameter matches increase score
            for (String k : that.params.keySet()) {
                if (that.params.get(k).equals(this.params.get(k))) {
                    fitness++;
                }
            }

            return fitness;
        }
    }

    private static boolean wildcardMatch(String a, String b) {
        return a.equals(b) || a.equals("*") || b.equals("*");
    }

    /**
     * Given a MIME type, find the entry from this Accept header that fits the best.
     *
     * @param mimeType
     */
    protected @Nullable Atom match(String mimeType) {
        Atom target = new Atom(mimeType);

        int bestFitness = -1;
        Atom best = null;
        for (Atom a : atoms) {
            int f = a.fit(target);
            if (f>bestFitness) {
                best = a;
            }
            bestFitness = f;
        }

        return best;
    }

    /**
     * Takes a list of supported mime-types and finds the best match for all the
     * media-ranges listed in header. The value of header must be a string that
     * conforms to the format of the HTTP Accept: header. The value of
     * 'supported' is a list of mime-types.
     *
     * <pre>
     * // Client: I prefer text/*, but if not I'm happy to take anything
     * // Server: I can serve you xbel or xml
     * // Result: let's serve you text/xml
     * new AcceptHeader("text/*;q=0.5, *;q=0.1").select("application/xbel+xml", "text/xml") => "text/xml"
     *
     * // Client: I want image, ideally PNG
     * // Server: I can give you plain text or XML
     * // Result: there's nothing to serve you here
     * new AcceptHeader("image/*;q=0.5, image/png;q=1").select("text/plain","text/xml") => null
     * </pre>
     *
     * @return null if none of the choices in {@code supported} is acceptable to the client.
     */
    public String select(Iterable<String> supported) {
        float bestQ = 0;
        String best = null;

        for (String s : supported) {
            Atom a = match(s);
            if (a!= null && a.q > bestQ) {
                bestQ = a.q;
                best = s;
            }
        }

        if (best==null)
            throw HttpResponses.error(HttpServletResponse.SC_NOT_ACCEPTABLE,
                    "Requested MIME types '" + ranges + "' didn't match any of the available options "+supported);
        return best;
    }

    public String select(String... supported) {
        return select(Arrays.asList(supported));
    }

    @Override
    public String toString() {
        return super.toString()+"["+ranges+"]";
    }

    // this performs databinding for @Header parameter injection
    public static class StaplerConverterImpl implements Converter {
        public Object convert(Class type, Object value) {
            return new AcceptHeader(value.toString());
        }
    }
}