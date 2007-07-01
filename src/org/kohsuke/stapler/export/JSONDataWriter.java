package org.kohsuke.stapler.export;

import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;
import java.io.Writer;

/**
 * JSON writer.
 *
 * @author Kohsuke Kawaguchi
 */
final class JSONDataWriter implements DataWriter {
    private boolean needComma;
    private final Writer out;

    JSONDataWriter(Writer out) throws IOException {
        this.out = out;
    }

    JSONDataWriter(StaplerResponse rsp) throws IOException {
        out = rsp.getWriter();
    }

    public void name(String name) throws IOException {
        comma();
        out.write(name+':');
        needComma = false;
    }

    private void data(String v) throws IOException {
        comma();
        out.write(v);
    }

    private void comma() throws IOException {
        if(needComma) out.write(',');
        needComma = true;
    }

    public void valuePrimitive(Object v) throws IOException {
        data(v.toString());
    }

    public void value(String v) throws IOException {
        StringBuilder buf = new StringBuilder(v.length());
        buf.append('\"');
        for( int i=0; i<v.length(); i++ ) {
            char c = v.charAt(i);
            switch(c) {
            case '"':   buf.append("\\\"");break;
            case '\\':  buf.append("\\\\");break;
            case '\n':  buf.append("\\n");break;
            case '\r':  buf.append("\\r");break;
            case '\t':  buf.append("\\t");break;
            default:    buf.append(c);break;
            }
        }
        buf.append('\"');
        data(buf.toString());
    }

    public void valueNull() throws IOException {
        data("null");
    }

    public void startArray() throws IOException {
        comma();
        out.write('[');
        needComma = false;
    }

    public void endArray() throws IOException {
        out.write(']');
        needComma = true;
    }

    public void startObject() throws IOException {
        comma();
        out.write('{');
        needComma=false;
    }

    public void endObject() throws IOException {
        out.write('}');
        needComma=true;
    }
}