package org.kohsuke.stapler.export;

import org.kohsuke.stapler.StaplerResponse;

import java.util.Stack;
import java.io.Writer;
import java.io.IOException;
import java.beans.Introspector;

/**
 * Writes XML.
 *
 * @author Kohsuke Kawaguchi
 */
final class XMLDataWriter implements DataWriter {

    private String name;
    private final Stack<String> objectNames = new Stack<String>();
    private final Stack<Boolean> arrayState = new Stack<Boolean>();
    private final Writer out;
    public boolean isArray;

    XMLDataWriter(Object bean, StaplerResponse rsp) throws IOException {
        name = Introspector.decapitalize(bean.getClass().getSimpleName());
        out = rsp.getWriter();
    }

    public void name(String name) {
        this.name = name;
    }

    public void valuePrimitive(Object v) throws IOException {
        value(v.toString());
    }

    public void value(String v) throws IOException {
        String n = adjustName();
        out.write('<'+n+'>');
        out.write(escape(v));
        out.write("</"+n+'>');
    }

    private String escape(String v) {
        StringBuffer buf = new StringBuffer(v.length()+64);
        for( int i=0; i<v.length(); i++ ) {
            char ch = v.charAt(i);
            if(ch=='<')
                buf.append("&lt;");
            else
            if(ch=='>')
                buf.append("&gt;");
            else
            if(ch=='&')
                buf.append("&amp;");
            else
                buf.append(ch);
        }
        return buf.toString();
    }

    public void valueNull() {
        // use absence to indicate null.
    }

    public void startArray() {
        // use repeated element to display array
        // this means nested arrays are not supported
        isArray = true;
    }

    public void endArray() {
        isArray = false;
    }

    public void startObject() throws IOException {
        objectNames.push(name);
        arrayState.push(isArray);
        out.write('<'+adjustName()+'>');
    }

    public void endObject() throws IOException {
        name = objectNames.pop();
        isArray = arrayState.pop();
        out.write("</"+adjustName()+'>');
    }

    /**
     * Returns the name to be used as an element name
     * by considering {@link #isArray}
     */
    private String adjustName() {
        if(isArray) return toSingular(name);
        return name;
    }

    /*package*/ static String toSingular(String name) {
        if(name.endsWith("s"))
            return name.substring(0,name.length()-1);
        return name;
    }
}