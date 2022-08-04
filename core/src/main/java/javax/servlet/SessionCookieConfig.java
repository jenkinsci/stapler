/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates and others.
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

package javax.servlet;

public interface SessionCookieConfig {
    void setName(String name);

    String getName();

    void setDomain(String domain);

    String getDomain();

    void setPath(String path);

    String getPath();

    void setComment(String comment);

    String getComment();

    void setHttpOnly(boolean httpOnly);

    boolean isHttpOnly();

    void setSecure(boolean secure);

    boolean isSecure();

    void setMaxAge(int maxAge);

    int getMaxAge();

    default jakarta.servlet.SessionCookieConfig toJakartaSessionCookieConfig() {
        return new jakarta.servlet.SessionCookieConfig() {
            @Override
            public void setName(String s) {
                SessionCookieConfig.this.setName(s);
            }

            @Override
            public String getName() {
                return SessionCookieConfig.this.getName();
            }

            @Override
            public void setDomain(String s) {
                SessionCookieConfig.this.setDomain(s);
            }

            @Override
            public String getDomain() {
                return SessionCookieConfig.this.getDomain();
            }

            @Override
            public void setPath(String s) {
                SessionCookieConfig.this.setPath(s);
            }

            @Override
            public String getPath() {
                return SessionCookieConfig.this.getPath();
            }

            @Override
            public void setComment(String s) {
                SessionCookieConfig.this.setComment(s);
            }

            @Override
            public String getComment() {
                return SessionCookieConfig.this.getComment();
            }

            @Override
            public void setHttpOnly(boolean b) {
                SessionCookieConfig.this.setHttpOnly(b);
            }

            @Override
            public boolean isHttpOnly() {
                return SessionCookieConfig.this.isHttpOnly();
            }

            @Override
            public void setSecure(boolean b) {
                SessionCookieConfig.this.setSecure(b);
            }

            @Override
            public boolean isSecure() {
                return SessionCookieConfig.this.isSecure();
            }

            @Override
            public void setMaxAge(int i) {
                SessionCookieConfig.this.setMaxAge(i);
            }

            @Override
            public int getMaxAge() {
                return SessionCookieConfig.this.getMaxAge();
            }
        };
    }

    static SessionCookieConfig fromJakartaSessionCookieConfig(
            jakarta.servlet.SessionCookieConfig from) {
        return new SessionCookieConfig() {
            @Override
            public void setName(String name) {
                from.setName(name);
            }

            @Override
            public String getName() {
                return from.getName();
            }

            @Override
            public void setDomain(String domain) {
                from.setDomain(domain);
            }

            @Override
            public String getDomain() {
                return from.getDomain();
            }

            @Override
            public void setPath(String path) {
                from.setPath(path);
            }

            @Override
            public String getPath() {
                return from.getPath();
            }

            @Override
            public void setComment(String comment) {
                from.setComment(comment);
            }

            @Override
            public String getComment() {
                return from.getComment();
            }

            @Override
            public void setHttpOnly(boolean httpOnly) {
                from.setHttpOnly(httpOnly);
            }

            @Override
            public boolean isHttpOnly() {
                return from.isHttpOnly();
            }

            @Override
            public void setSecure(boolean secure) {
                from.setSecure(secure);
            }

            @Override
            public boolean isSecure() {
                return from.isSecure();
            }

            @Override
            public void setMaxAge(int maxAge) {
                from.setMaxAge(maxAge);
            }

            @Override
            public int getMaxAge() {
                return from.getMaxAge();
            }

            @Override
            public jakarta.servlet.SessionCookieConfig toJakartaSessionCookieConfig() {
                return from;
            }
        };
    }
}
