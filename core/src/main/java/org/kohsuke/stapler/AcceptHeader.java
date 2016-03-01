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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MIME-Type Parser
 *
 * This class provides basic functions for handling mime-types. It can handle
 * matching mime-types against a list of media-ranges. See section 14.1 of the
 * HTTP specification [RFC 2616] for a complete explanation.
 *
 * http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.1
 *
 * A port to Java of Joe Gregorio's MIME-Type Parser:
 *
 * http://code.google.com/p/mimeparse/
 *
 * Ported by Tom Zellman <tzellman@gmail.com>.
 *
 */
public final class AcceptHeader
{
    private final List<Atom> atoms = new ArrayList<Atom>();

    /**
     * Parse the accept header value.
     *
     * @param ranges
     *      something like "text/*;q=0.5,*; q=0.1"
     */
    public AcceptHeader(String ranges) {
        for (String r : StringUtils.split(ranges, ','))
            atoms.add(Atom.parseMediaRange(r));
    }

    /**
     * Media range plus parameters and extensions
     */
    protected static class Atom {
        String type;
        String subType;

        // !a dictionary of all the parameters for the media range
        Map<String, String> params;

        @Override
        public String toString() {
            StringBuffer s = new StringBuffer("('" + type + "', '" + subType
                    + "', {");
            for (String k : params.keySet())
                s.append("'" + k + "':'" + params.get(k) + "',");
            return s.append("})").toString();
        }

        /**
         * Carves up a mime-type and returns an Atom object
         *
         * For example, the media range 'application/xhtml;q=0.5' would get parsed
         * into:
         *
         * ('application', 'xhtml', {'q', '0.5'})
         */
        protected static Atom parseMimeType(String mimeType) {
            String[] parts = StringUtils.split(mimeType, ";");
            Atom a = new Atom();
            a.params = new HashMap<String, String>();

            for (int i = 1; i < parts.length; ++i) {
                String p = parts[i];
                String[] subParts = StringUtils.split(p, '=');
                if (subParts.length == 2)
                    a.params.put(subParts[0].trim(), subParts[1].trim());
            }
            String fullType = parts[0].trim();

            // Java URLConnection class sends an Accept header that includes a
            // single "*" - Turn it into a legal wildcard.
            if (fullType.equals("*"))
                fullType = "*/*";
            String[] types = StringUtils.split(fullType, "/");
            a.type = types[0].trim();
            a.subType = types[1].trim();
            return a;
        }

        /**
         * Carves up a media range and returns a ParseResults.
         *
         * For example, the media range 'application/*;q=0.5' would get parsed into:
         *
         * ('application', '*', {'q', '0.5'})
         *
         * In addition this function also guarantees that there is a value for 'q'
         * in the params dictionary, filling it in with a proper default if
         * necessary.
         *
         * @param range
         */
        protected static Atom parseMediaRange(String range) {
            Atom results = parseMimeType(range);
            String q = results.params.get("q");
            float f = NumberUtils.toFloat(q, 1);
            if (StringUtils.isBlank(q) || f < 0 || f > 1)
                results.params.put("q", "1");
            return results;
        }
    }


    /**
     * Structure for holding a fitness/quality combo
     */
    protected static class FitnessAndQuality implements Comparable<FitnessAndQuality> {
        private final int fitness;
        private final float quality;
        String mimeType; // optionally used

        public FitnessAndQuality(int fitness, float quality) {
            this.fitness = fitness;
            this.quality = quality;
        }

        public int compareTo(FitnessAndQuality o) {
            if (fitness == o.fitness) {
                if (quality == o.quality)
                    return 0;
                else
                    return quality < o.quality ? -1 : 1;
            } else
                return fitness < o.fitness ? -1 : 1;
        }
    }

    /**
     * Find the best match for a given mimeType against a list of media_ranges
     * that have already been parsed by MimeParse.parseMediaRange(). Returns a
     * tuple of the fitness value and the value of the 'q' quality parameter of
     * the best match, or (-1, 0) if no match was found. Just as for
     * quality_parsed(), 'parsed_ranges' must be a list of parsed media ranges.
     *
     * @param mimeType
     */
    protected FitnessAndQuality fitnessAndQualityParsed(String mimeType) {
        int bestFitness = -1;
        float bestFitQ = 0;
        Atom target = Atom.parseMediaRange(mimeType);

        for (Atom range : atoms)
        {
            if ((target.type.equals(range.type) || range.type.equals("*") || target.type
                    .equals("*"))
                    && (target.subType.equals(range.subType)
                            || range.subType.equals("*") || target.subType
                            .equals("*")))
            {
                for (String k : target.params.keySet())
                {
                    int paramMatches = 0;
                    if (!k.equals("q") && range.params.containsKey(k)
                            && target.params.get(k).equals(range.params.get(k)))
                    {
                        paramMatches++;
                    }
                    int fitness = (range.type.equals(target.type)) ? 100 : 0;
                    fitness += (range.subType.equals(target.subType)) ? 10 : 0;
                    fitness += paramMatches;
                    if (fitness > bestFitness)
                    {
                        bestFitness = fitness;
                        bestFitQ = NumberUtils
                                .toFloat(range.params.get("q"), 0);
                    }
                }
            }
        }
        return new FitnessAndQuality(bestFitness, bestFitQ);
    }

    /**
     * Returns the quality 'q' of a mime-type when compared against the
     * mediaRanges in ranges. For example:
     *
     * @param mimeType
     */
    public float quality(String mimeType) {
        return fitnessAndQualityParsed(mimeType).quality;
    }

    /**
     * Takes a list of supported mime-types and finds the best match for all the
     * media-ranges listed in header. The value of header must be a string that
     * conforms to the format of the HTTP Accept: header. The value of
     * 'supported' is a list of mime-types.
     *
     * MimeParse.bestMatch(Arrays.asList(new String[]{"application/xbel+xml",
     * "text/xml"}), "text/*;q=0.5,*; q=0.1") 'text/xml'
     *
     * @param supported
     * @return
     */
    public String bestMatch(Collection<String> supported) {
        List<FitnessAndQuality> weightedMatches = new ArrayList<FitnessAndQuality>();

        for (String s : supported) {
            FitnessAndQuality fitnessAndQuality = fitnessAndQualityParsed(s);
            fitnessAndQuality.mimeType = s;
            weightedMatches.add(fitnessAndQuality);
        }
        Collections.sort(weightedMatches);

        FitnessAndQuality lastOne = weightedMatches
                .get(weightedMatches.size() - 1);
        return NumberUtils.compare(lastOne.quality, 0) != 0 ? lastOne.mimeType : "";
    }
}