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

package javax.servlet;

import java.util.Enumeration;
import java.util.ResourceBundle;

public abstract class GenericFilter implements Filter, FilterConfig, java.io.Serializable {
    private static final long serialVersionUID = 4060116231031076581L;

    private static final String LSTRING_FILE = "javax.servlet.LocalStrings";
    private static final ResourceBundle lStrings = ResourceBundle.getBundle(LSTRING_FILE);

    private transient FilterConfig config;

    public GenericFilter() {}

    @Override
    public String getInitParameter(String name) {
        FilterConfig fc = getFilterConfig();
        if (fc == null) {
            throw new IllegalStateException(
                    lStrings.getString("err.filter_config_not_initialized"));
        }

        return fc.getInitParameter(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        FilterConfig fc = getFilterConfig();
        if (fc == null) {
            throw new IllegalStateException(
                    lStrings.getString("err.filter_config_not_initialized"));
        }

        return fc.getInitParameterNames();
    }

    public FilterConfig getFilterConfig() {
        return config;
    }

    @Override
    public ServletContext getServletContext() {
        FilterConfig sc = getFilterConfig();
        if (sc == null) {
            throw new IllegalStateException(
                    lStrings.getString("err.filter_config_not_initialized"));
        }

        return sc.getServletContext();
    }

    @Override
    public void init(FilterConfig config) throws ServletException {
        this.config = config;
        this.init();
    }

    public void init() throws ServletException {}

    @Override
    public String getFilterName() {
        FilterConfig sc = getFilterConfig();
        if (sc == null) {
            throw new IllegalStateException(
                    lStrings.getString("err.servlet_config_not_initialized"));
        }

        return sc.getFilterName();
    }
}
