/*
 * Copyright (c) 2022, CloudBees, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this list of
 *       conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
 * THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.kohsuke.stapler;

import java.net.URL;
import java.net.URLClassLoader;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class MetaClassLoaderTest {

    @Test
    public void inheritanceAndCaching() {
        ClassLoader cl1 = new URLClassLoader(new URL[0]);
        ClassLoader cl2 = new URLClassLoader(new URL[0], cl1);
        ClassLoader cl3 = new URLClassLoader(new URL[0], cl2);
        ClassLoader cl4 = new URLClassLoader(new URL[0], cl1);
        MetaClassLoader mcl3 = MetaClassLoader.get(cl3);
        MetaClassLoader mcl1 = MetaClassLoader.get(cl1);
        MetaClassLoader mcl2 = MetaClassLoader.get(cl2);
        MetaClassLoader mcl4 = MetaClassLoader.get(cl4);
        assertEquals(cl1, mcl1.loader);
        assertEquals(cl2, mcl2.loader);
        assertEquals(cl3, mcl3.loader);
        assertEquals(cl4, mcl4.loader);
        assertEquals(mcl1, mcl2.parent);
        assertEquals(mcl2, mcl3.parent);
        assertEquals(mcl1, mcl4.parent);
        assertEquals(mcl1, MetaClassLoader.get(cl1));
        assertEquals(mcl2, MetaClassLoader.get(cl2));
        assertEquals(mcl3, MetaClassLoader.get(cl3));
        assertEquals(mcl4, MetaClassLoader.get(cl4));
    }

}
