package org.kohsuke.stapler.framework.io;

import junit.framework.TestCase;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;

/**
 * TODO: make it a real junit test
 *
 * @author jglick
 */
public class WriterOutputStreamTest extends TestCase {
    public void testFoo() {} // otherwise surefire will be unhappy
    
    public static void main(String[] args) throws IOException {
        OutputStream os = new WriterOutputStream(new OutputStreamWriter(System.out));
        PrintStream ps = new PrintStream(os);
        for (int i = 0; i < 200; i++) {
            ps.println("#" + i + " blah blah blah");
        }
        os.close();
    }
}
