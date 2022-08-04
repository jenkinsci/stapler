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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import javax.servlet.annotation.HttpMethodConstraint;
import javax.servlet.annotation.ServletSecurity;

public class ServletSecurityElement extends HttpConstraintElement {
    private Collection<String> methodNames;
    private Collection<HttpMethodConstraintElement> methodConstraints;

    public ServletSecurityElement() {
        methodConstraints = new HashSet<>();
        methodNames = Collections.emptySet();
    }

    public ServletSecurityElement(HttpConstraintElement constraint) {
        super(
                constraint.getEmptyRoleSemantic(),
                constraint.getTransportGuarantee(),
                constraint.getRolesAllowed());
        methodConstraints = new HashSet<>();
        methodNames = Collections.emptySet();
    }

    public ServletSecurityElement(Collection<HttpMethodConstraintElement> methodConstraints) {
        this.methodConstraints = (methodConstraints == null ? new HashSet<>() : methodConstraints);
        methodNames = checkMethodNames(this.methodConstraints);
    }

    public ServletSecurityElement(
            HttpConstraintElement constraint,
            Collection<HttpMethodConstraintElement> methodConstraints) {
        super(
                constraint.getEmptyRoleSemantic(),
                constraint.getTransportGuarantee(),
                constraint.getRolesAllowed());
        this.methodConstraints = (methodConstraints == null ? new HashSet<>() : methodConstraints);
        methodNames = checkMethodNames(this.methodConstraints);
    }

    public ServletSecurityElement(ServletSecurity annotation) {
        super(
                annotation.value().value(),
                annotation.value().transportGuarantee(),
                annotation.value().rolesAllowed());
        this.methodConstraints = new HashSet<>();
        for (HttpMethodConstraint constraint : annotation.httpMethodConstraints()) {
            this.methodConstraints.add(
                    new HttpMethodConstraintElement(
                            constraint.value(),
                            new HttpConstraintElement(
                                    constraint.emptyRoleSemantic(),
                                    constraint.transportGuarantee(),
                                    constraint.rolesAllowed())));
        }
        methodNames = checkMethodNames(this.methodConstraints);
    }

    public Collection<HttpMethodConstraintElement> getHttpMethodConstraints() {
        return Collections.unmodifiableCollection(methodConstraints);
    }

    public Collection<String> getMethodNames() {
        return Collections.unmodifiableCollection(methodNames);
    }

    private Collection<String> checkMethodNames(
            Collection<HttpMethodConstraintElement> methodConstraints) {
        Collection<String> methodNames = new HashSet<>();
        for (HttpMethodConstraintElement methodConstraint : methodConstraints) {
            String methodName = methodConstraint.getMethodName();
            if (!methodNames.add(methodName)) {
                throw new IllegalArgumentException("Duplicate HTTP method name: " + methodName);
            }
        }
        return methodNames;
    }
}
