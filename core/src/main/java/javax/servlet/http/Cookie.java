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

package javax.servlet.http;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class Cookie implements Cloneable, Serializable {
    private static final long serialVersionUID = -6454587001725327448L;

    private static final String TSPECIALS;

    private static final String LSTRING_FILE = "javax.servlet.http.LocalStrings";

    private static ResourceBundle lStrings = ResourceBundle.getBundle(LSTRING_FILE);

    static {
        if (Boolean.valueOf(
                        System.getProperty(
                                "org.glassfish.web.rfc2109_cookie_names_enforced", "true"))
                .booleanValue()) {
            TSPECIALS = "/()<>@,;:\\\"[]?={} \t";
        } else {
            TSPECIALS = ",; ";
        }
    }

    //
    // The value of the cookie itself.
    //

    private String name; // NAME= ... "$Name" style is reserved
    private String value; // value of NAME

    //
    // Attributes encoded in the header's cookie fields.
    //

    private String comment; // ;Comment=VALUE ... describes cookie's use
    // ;Discard ... implied by maxAge < 0
    private String domain; // ;Domain=VALUE ... domain that sees cookie
    private int maxAge = -1; // ;Max-Age=VALUE ... cookies auto-expire
    private String path; // ;Path=VALUE ... URLs that see the cookie
    private boolean secure; // ;Secure ... e.g. use SSL
    private int version = 0; // ;Version=1 ... means RFC 2109++ style
    private boolean isHttpOnly = false;

    public Cookie(String name, String value) {
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException(lStrings.getString("err.cookie_name_blank"));
        }
        if (!isToken(name)
                || name.equalsIgnoreCase("Comment")
                || // rfc2019
                name.equalsIgnoreCase("Discard")
                || // 2019++
                name.equalsIgnoreCase("Domain")
                || name.equalsIgnoreCase("Expires")
                || // (old cookies)
                name.equalsIgnoreCase("Max-Age")
                || // rfc2019
                name.equalsIgnoreCase("Path")
                || name.equalsIgnoreCase("Secure")
                || name.equalsIgnoreCase("Version")
                || name.startsWith("$")) {
            String errMsg = lStrings.getString("err.cookie_name_is_token");
            Object[] errArgs = new Object[1];
            errArgs[0] = name;
            errMsg = MessageFormat.format(errMsg, errArgs);
            throw new IllegalArgumentException(errMsg);
        }

        this.name = name;
        this.value = value;
    }

    public void setComment(String purpose) {
        comment = purpose;
    }

    public String getComment() {
        return comment;
    }

    public void setDomain(String domain) {
        this.domain = domain.toLowerCase(Locale.ENGLISH); // IE allegedly needs this
    }

    public String getDomain() {
        return domain;
    }

    public void setMaxAge(int expiry) {
        maxAge = expiry;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public void setPath(String uri) {
        path = uri;
    }

    public String getPath() {
        return path;
    }

    public void setSecure(boolean flag) {
        secure = flag;
    }

    public boolean getSecure() {
        return secure;
    }

    public String getName() {
        return name;
    }

    public void setValue(String newValue) {
        value = newValue;
    }

    public String getValue() {
        return value;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int v) {
        version = v;
    }

    /*
     * Tests a string and returns true if the string counts as a reserved token in the Java language.
     *
     * @param value the <code>String</code> to be tested
     *
     * @return <code>true</code> if the <code>String</code> is a reserved token; <code>false</code> otherwise
     */
    private boolean isToken(String value) {
        int len = value.length();
        for (int i = 0; i < len; i++) {
            char c = value.charAt(i);
            if (c < 0x20 || c >= 0x7f || TSPECIALS.indexOf(c) != -1) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void setHttpOnly(boolean isHttpOnly) {
        this.isHttpOnly = isHttpOnly;
    }

    public boolean isHttpOnly() {
        return isHttpOnly;
    }
}
