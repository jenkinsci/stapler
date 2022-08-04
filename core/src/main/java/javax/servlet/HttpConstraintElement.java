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

import javax.servlet.annotation.ServletSecurity.EmptyRoleSemantic;
import javax.servlet.annotation.ServletSecurity.TransportGuarantee;

public class HttpConstraintElement {
    private EmptyRoleSemantic emptyRoleSemantic;
    private TransportGuarantee transportGuarantee;
    private String[] rolesAllowed;

    public HttpConstraintElement() {
        this(EmptyRoleSemantic.PERMIT);
    }

    public HttpConstraintElement(EmptyRoleSemantic semantic) {
        this(semantic, TransportGuarantee.NONE, new String[0]);
    }

    public HttpConstraintElement(TransportGuarantee guarantee, String... roleNames) {
        this(EmptyRoleSemantic.PERMIT, guarantee, roleNames);
    }

    public HttpConstraintElement(
            EmptyRoleSemantic semantic, TransportGuarantee guarantee, String... roleNames) {
        if (semantic == EmptyRoleSemantic.DENY && roleNames.length > 0) {
            throw new IllegalArgumentException("Deny semantic with rolesAllowed");
        }
        this.emptyRoleSemantic = semantic;
        this.transportGuarantee = guarantee;
        this.rolesAllowed = copyStrings(roleNames);
    }

    public EmptyRoleSemantic getEmptyRoleSemantic() {
        return this.emptyRoleSemantic;
    }

    public TransportGuarantee getTransportGuarantee() {
        return this.transportGuarantee;
    }

    public String[] getRolesAllowed() {
        return copyStrings(this.rolesAllowed);
    }

    private String[] copyStrings(String[] strings) {
        String[] arr = null;
        if (strings != null) {
            int len = strings.length;
            arr = new String[len];
            if (len > 0) {
                System.arraycopy(strings, 0, arr, 0, len);
            }
        }

        return ((arr != null) ? arr : new String[0]);
    }
}
