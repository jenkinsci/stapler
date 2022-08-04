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

public enum DispatcherType {
    FORWARD,
    INCLUDE,
    REQUEST,
    ASYNC,
    ERROR;

    public jakarta.servlet.DispatcherType toJakartaDispatcherType() {
        switch (DispatcherType.this) {
            case FORWARD:
                return jakarta.servlet.DispatcherType.FORWARD;
            case INCLUDE:
                return jakarta.servlet.DispatcherType.INCLUDE;
            case REQUEST:
                return jakarta.servlet.DispatcherType.REQUEST;
            case ASYNC:
                return jakarta.servlet.DispatcherType.ASYNC;
            case ERROR:
                return jakarta.servlet.DispatcherType.ERROR;
            default:
                throw new IllegalArgumentException(
                        "Unknown DispatcherType: " + DispatcherType.this);
        }
    }

    public static DispatcherType fromJakartaDispatcherType(jakarta.servlet.DispatcherType from) {
        switch (from) {
            case FORWARD:
                return DispatcherType.FORWARD;
            case INCLUDE:
                return DispatcherType.INCLUDE;
            case REQUEST:
                return DispatcherType.REQUEST;
            case ASYNC:
                return DispatcherType.ASYNC;
            case ERROR:
                return DispatcherType.ERROR;
            default:
                throw new IllegalArgumentException("Unknown DispatcherType: " + from);
        }
    }
}
