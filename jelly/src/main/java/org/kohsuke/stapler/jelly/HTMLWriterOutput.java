package org.kohsuke.stapler.jelly;

import org.apache.commons.jelly.XMLOutput;
import org.dom4j.io.HTMLWriter;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * Wrapper for XMLOutput using HTMLWriter that can turn off its HTML handling
 * (if the Content-Type gets set to something other than text/html).
 * 
 * @author Alan.Harder@Sun.Com
 */
public class HTMLWriterOutput extends XMLOutput {
    private HTMLWriter htmlWriter;
    private OutputFormat format;

    public static HTMLWriterOutput create(OutputStream out) throws UnsupportedEncodingException {
        OutputFormat format = new OutputFormat();
        format.setXHTML(true);
        // Only use short close for tags identified by HTMLWriter:
        format.setExpandEmptyElements(true);
        return new HTMLWriterOutput(new HTMLWriter(out, format), format);
    }

    private HTMLWriterOutput(HTMLWriter hw, OutputFormat fmt) {
        super(hw);
        hw.setEscapeText(false);
        this.htmlWriter = hw;
        this.format = fmt;
    }

    @Override public void close() throws IOException {
        htmlWriter.close();
    }

    /**
     * False to turn off HTML handling and reenable "/>" for any empty XML element.
     * True to switch back to default mode with HTML handling.
     */
    public void useHTML(boolean enabled) {
        htmlWriter.setEnabled(enabled);
        format.setExpandEmptyElements(enabled);
    }
}
