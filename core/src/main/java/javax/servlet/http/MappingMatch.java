/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package javax.servlet.http;

public enum MappingMatch {
    CONTEXT_ROOT,
    DEFAULT,
    EXACT,
    EXTENSION,
    PATH;

    public jakarta.servlet.http.MappingMatch toJakartaMappingMatch() {
        switch (MappingMatch.this) {
            case CONTEXT_ROOT:
                return jakarta.servlet.http.MappingMatch.CONTEXT_ROOT;
            case DEFAULT:
                return jakarta.servlet.http.MappingMatch.DEFAULT;
            case EXACT:
                return jakarta.servlet.http.MappingMatch.EXACT;
            case EXTENSION:
                return jakarta.servlet.http.MappingMatch.EXTENSION;
            case PATH:
                return jakarta.servlet.http.MappingMatch.PATH;
            default:
                throw new IllegalArgumentException("Unknown MappingMatch: " + MappingMatch.this);
        }
    }

    public static MappingMatch fromJakartaMappingMatch(jakarta.servlet.http.MappingMatch from) {
        switch (from) {
            case CONTEXT_ROOT:
                return MappingMatch.CONTEXT_ROOT;
            case DEFAULT:
                return MappingMatch.DEFAULT;
            case EXACT:
                return MappingMatch.EXACT;
            case EXTENSION:
                return MappingMatch.EXTENSION;
            case PATH:
                return MappingMatch.PATH;
            default:
                throw new IllegalArgumentException("Unknown MappingMatch: " + from);
        }
    }
}
