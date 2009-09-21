package org.kohsuke.stapler.export;

import org.kohsuke.stapler.StaplerResponse;

import java.io.Writer;
import java.io.IOException;

/**
 * Writes out the format that can be <tt>eval</tt>-ed from Ruby.
 *
 * <p>
 * Ruby uses a similar list and map literal syntax as JavaScript.
 * The only differences are <tt>null</tt> vs <tt>nil</tt> and
 * <tt>key:value</tt> vs <tt>key => value</tt>.
 *
 * @author Kohsuke Kawaguchi, Jim Meyer
 */
final class RubyDataWriter extends JSONDataWriter {
    public RubyDataWriter(Writer out) throws IOException {
        super(out);
    }

    public RubyDataWriter(StaplerResponse rsp) throws IOException {
        super(rsp);
    }

    @Override
    public void name(String name) throws IOException {
        comma();
        out.write('"'+name+"\" => ");
        needComma = false;
    }

    public void valueNull() throws IOException {
        data("nil");
    }

    @Override
    public void startObject() throws IOException {
        comma();
        out.write("OpenStruct.new({");
        needComma=false;
    }

    @Override
    public void endObject() throws IOException {
        out.write("})");
        needComma=true;
    }
}
