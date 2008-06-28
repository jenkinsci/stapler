package org.kohsuke.stapler.export;

import org.kohsuke.stapler.StaplerResponse;

import java.io.Writer;
import java.io.IOException;

/**
 * Writes out the format that can be <tt>eval</tt>-ed from Python.
 *
 * <p>
 * Python uses the same list and map literal syntax as JavaScript.
 * The only difference is <tt>null</tt> vs <tt>None</tt>.
 *
 * @author Kohsuke Kawaguchi
 */
final class PythonDataWriter extends JSONDataWriter {
    public PythonDataWriter(Writer out) throws IOException {
        super(out);
    }

    public PythonDataWriter(StaplerResponse rsp) throws IOException {
        super(rsp);
    }

    @Override
    public void valueNull() throws IOException {
        data("None");
    }
}
