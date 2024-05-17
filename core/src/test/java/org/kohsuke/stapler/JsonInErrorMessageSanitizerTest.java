/*
 * The MIT License
 *
 * Copyright (c) 2018, CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.kohsuke.stapler;

import junit.framework.TestCase;
import net.sf.json.JSONObject;

public class JsonInErrorMessageSanitizerTest extends TestCase {
    public void testNoopToReturnCopy() {
        JSONObject input = new JSONObject();
        input.accumulate("a", 1);
        input.accumulate("a", 2);
        input.accumulate("a", 3);
        input.accumulate("b", 4);

        JSONObject sub = new JSONObject();
        sub.accumulate("d", 5);
        input.accumulate("c", sub);

        JSONObject output = JsonInErrorMessageSanitizer.NOOP.sanitize(input);
        assertNotSame(output, input);
        assertEquals(output.getJSONArray("a").get(0), input.getJSONArray("a").get(0));
        assertEquals(output.getJSONArray("a").get(2), input.getJSONArray("a").get(2));
        assertEquals(output.getInt("b"), input.getInt("b"));
        assertEquals(
                output.getJSONObject("c").getInt("d"), input.getJSONObject("c").getInt("d"));
    }
}
