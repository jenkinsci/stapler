/*
 * Copyright (c) 2021 CloudBees, Inc.
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

package org.kohsuke.stapler.jelly;

import org.apache.commons.jelly.expression.Expression;
import org.apache.commons.jelly.impl.TagScript;

/**
 * This class implements a {@link TagScript} that allows rewriting attribute names.
 *
 */
/* package */ class AttributeNameRewritingTagScript extends TagScript {
    private final String original;
    private final String replacement;

    public AttributeNameRewritingTagScript(String original, String replacement) {
        this.original = original;
        this.replacement = replacement;
    }

    @Override
    public void addAttribute(String name, Expression expression) {
        if (replacement.equals(name)) {
            // cf. TagScript#run
            throw new IllegalArgumentException("This tag does not understand the '" + replacement + "' attribute");
        }
        if (original.equals(name)) {
            super.addAttribute(replacement, expression);
        } else {
            super.addAttribute(name, expression);
        }
    }
}
