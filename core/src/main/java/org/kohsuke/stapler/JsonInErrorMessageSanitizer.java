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

import net.sf.json.JSONObject;

/**
 * Customize / sanitize the JSON before putting it in the stack trace / error messages.
 * Mainly thought to avoid leaking secrets / credentials in the log.
 */
public interface JsonInErrorMessageSanitizer {
    /**
     * Removes/redacts all the confidential information to let the result to be printed in the log / stack trace / error message.
     * Must return a new instance of the JSON.
     */
    JSONObject sanitize(JSONObject jsonData);

    /**
     * Used by default when no other sanitizer are configured. Has no effect on the information, just returning a copy.
     */
    JsonInErrorMessageSanitizer NOOP = JSONObject::fromObject;
}
