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

public interface ReadListener extends EventListener {
    void onDataAvailable() throws IOException;

    void onAllDataRead() throws IOException;

    void onError(Throwable t);

    default jakarta.servlet.ReadListener toJakartaReadListener() {
        return new jakarta.servlet.ReadListener() {
            @Override
            public void onDataAvailable() throws IOException {
                ReadListener.this.onDataAvailable();
            }

            @Override
            public void onAllDataRead() throws IOException {
                ReadListener.this.onAllDataRead();
            }

            @Override
            public void onError(Throwable throwable) {
                ReadListener.this.onError(throwable);
            }
        };
    }

    static ReadListener fromJakartaReadListener(jakarta.servlet.ReadListener from) {
        return new ReadListener() {
            @Override
            public void onDataAvailable() throws IOException {
                from.onDataAvailable();
            }

            @Override
            public void onAllDataRead() throws IOException {
                from.onAllDataRead();
            }

            @Override
            public void onError(Throwable t) {
                from.onError(t);
            }

            @Override
            public jakarta.servlet.ReadListener toJakartaReadListener() {
                return from;
            }
        };
    }
}
