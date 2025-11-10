package org.kohsuke.stapler.framework.io;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import org.junit.jupiter.api.Test;

/**
 * @author Kohsuke Kawaguchi
 */
class LineEndNormalizingWriterTest {

    @Test
    void test1() throws IOException {
        StringWriter sw = new StringWriter();
        Writer w = new LineEndNormalizingWriter(sw);

        w.write("abc\r\ndef\r");
        w.write("\n");

        assertEquals("abc\r\ndef\r\n", sw.toString());
    }

    @Test
    void test2() throws IOException {
        StringWriter sw = new StringWriter();
        Writer w = new LineEndNormalizingWriter(sw);

        w.write("abc\ndef\n");
        w.write("\n");

        assertEquals("abc\r\ndef\r\n\r\n", sw.toString());
    }

    @Test
    void test3() throws IOException {
        StringWriter sw = new StringWriter();
        Writer w = new LineEndNormalizingWriter(sw);

        w.write("\r\n\n");

        assertEquals("\r\n\r\n", sw.toString());
    }
}
