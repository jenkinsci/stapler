/*
 * Copyright (c) 2017, 2019 Oracle and/or its affiliates and others.
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

public interface HttpServletMapping {
    String getMatchValue();

    String getPattern();

    String getServletName();

    MappingMatch getMappingMatch();

    default jakarta.servlet.http.HttpServletMapping toJakartaHttpServletMapping() {
        return new jakarta.servlet.http.HttpServletMapping() {
            @Override
            public String getMatchValue() {
                return HttpServletMapping.this.getMatchValue();
            }

            @Override
            public String getPattern() {
                return HttpServletMapping.this.getPattern();
            }

            @Override
            public String getServletName() {
                return HttpServletMapping.this.getServletName();
            }

            @Override
            public jakarta.servlet.http.MappingMatch getMappingMatch() {
                return HttpServletMapping.this.getMappingMatch().toJakartaMappingMatch();
            }
        };
    }

    static HttpServletMapping fromJakartaHttpServletMapping(
            jakarta.servlet.http.HttpServletMapping from) {
        return new HttpServletMapping() {
            @Override
            public String getMatchValue() {
                return from.getMatchValue();
            }

            @Override
            public String getPattern() {
                return from.getPattern();
            }

            @Override
            public String getServletName() {
                return from.getServletName();
            }

            @Override
            public MappingMatch getMappingMatch() {
                return MappingMatch.fromJakartaMappingMatch(from.getMappingMatch());
            }

            @Override
            public jakarta.servlet.http.HttpServletMapping toJakartaHttpServletMapping() {
                return from;
            }
        };
    }
}
