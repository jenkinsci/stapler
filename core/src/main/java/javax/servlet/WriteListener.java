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

import java.io.IOException;
import java.util.EventListener;

public interface WriteListener extends EventListener {
    void onWritePossible() throws IOException;

    void onError(final Throwable t);

    default jakarta.servlet.WriteListener toJakartaWriteListener() {
        return new jakarta.servlet.WriteListener() {
            @Override
            public void onWritePossible() throws IOException {
                WriteListener.this.onWritePossible();
            }

            @Override
            public void onError(Throwable throwable) {
                WriteListener.this.onError(throwable);
            }
        };
    }

    static WriteListener fromJakartaWriteListener(jakarta.servlet.WriteListener from) {
        return new WriteListener() {
            @Override
            public void onWritePossible() throws IOException {
                from.onWritePossible();
            }

            @Override
            public void onError(Throwable t) {
                from.onError(t);
            }

            @Override
            public jakarta.servlet.WriteListener toJakartaWriteListener() {
                return from;
            }
        };
    }
}
