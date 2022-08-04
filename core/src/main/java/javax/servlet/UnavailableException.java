/*
 * Copyright (c) 1997-2018 Oracle and/or its affiliates and others.
 * All rights reserved.
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package javax.servlet;

public class UnavailableException extends ServletException {
    private static final long serialVersionUID = 5622686609215003468L;

    private Servlet servlet; // what's unavailable
    private boolean permanent; // needs admin action?
    private int seconds; // unavailability estimate

    @Deprecated
    public UnavailableException(Servlet servlet, String msg) {
        super(msg);
        this.servlet = servlet;
        permanent = true;
    }

    @Deprecated
    public UnavailableException(int seconds, Servlet servlet, String msg) {
        super(msg);
        this.servlet = servlet;
        if (seconds <= 0) this.seconds = -1;
        else this.seconds = seconds;
        permanent = false;
    }

    public UnavailableException(String msg) {
        super(msg);

        permanent = true;
    }

    public UnavailableException(String msg, int seconds) {
        super(msg);

        if (seconds <= 0) this.seconds = -1;
        else this.seconds = seconds;

        permanent = false;
    }

    public boolean isPermanent() {
        return permanent;
    }

    @Deprecated
    public Servlet getServlet() {
        return servlet;
    }

    public int getUnavailableSeconds() {
        return permanent ? -1 : seconds;
    }
}
