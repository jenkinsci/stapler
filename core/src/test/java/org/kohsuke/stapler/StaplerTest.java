package org.kohsuke.stapler;

import junit.framework.TestCase;

import java.io.File;
import java.net.URL;
import java.util.Arrays;

/**
 * @author Kohsuke Kawaguchi
 */
public class StaplerTest extends TestCase {
    public void testNormalization() {
        assertIdemPotent("/");
        assertIdemPotent("");
        assertIdemPotent("/foo");
        assertIdemPotent("/bar/");
        assertIdemPotent("zot/");
        testC12n("",".");
        testC12n("","foo/..");
        testC12n("foo", "foo/bar/./..");

        testC12n("/abc","/abc");
        testC12n("/abc/","/abc/");
        testC12n("/","/abc/../");
        testC12n("/","/abc/def/../../");
        testC12n("/def","/abc/../def");
        testC12n("/xxx","/../../../xxx");
    }

    private void testC12n(String expected, String input) {
        assertEquals(expected, Stapler.canonicalPath(input));
    }

    private void assertIdemPotent(String str) {
        assertEquals(str,Stapler.canonicalPath(str));
    }

    private void assertToFile(String s, URL... urls) {
        for (URL url : urls)
            assertEquals(s,new Stapler().toFile(url).getPath());
    }

    public void testToFileOnWindows() throws Exception {
        if (File.pathSeparatorChar==':')    return; // this is Windows only test

        // these two URLs has the same toExternalForm(), but it'll have different components
        String a = "\\\\vboxsvr\\root";
        assertToFile(a,
                new URL("file://vboxsvr/root/"),
                new File(a).toURL()
        );

        // whitespace + UNC
        a = "\\\\vboxsvr\\root\\documents and files";
        assertToFile(a,
                new URL("file://vboxsvr/root/documents and files"),
                new URL("file://vboxsvr/root/documents%20and%20files"),
                new File(a).toURL()
        );


        // whitespace
        for (String path : Arrays.asList("","Documents and files","Documents%20and%20Files")) {
            assertToFile("c:\\"+path.replace("%20"," "),
                    new URL("file:///c:/"+path),
                    new URL("file://c:/"+path),
                    new URL("file:/c:/"+path)
            );

            assertToFile(path.length()==0 ? "\\\\vboxsvr" : "\\\\vboxsvr\\"+path.replace("%20"," "),
                    new URL("file://vboxsvr/"+path),
                    new URL("file:////vboxsvr/"+path)
            );
        }
    }

    public void testToFileOnUnix() throws Exception {
        if (File.pathSeparatorChar==';')    return; // this is Unix only test

        // these two URLs has the same toExternalForm(), but it'll have different components
        String a = "/tmp/foo";
        assertToFile(a,
                new URL("file:///tmp/foo/"),
                new URL("file://tmp/foo/"),
                new URL("file:/tmp/foo/")
        );

        // whitespace
        for (String path : Arrays.asList("foo bar","foo%20bar")) {
            assertToFile("/tmp/"+path.replace("%20"," "),
                    new URL("file:///tmp/"+path),
                    new URL("file://tmp/"+path),
                    new URL("file:/tmp/"+path)
            );
        }
    }
}
