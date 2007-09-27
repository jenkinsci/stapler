package org.kohsuke.stapler.export;

import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;
import java.io.Writer;

/**
 * Export flavor.
 *
 * @author Kohsuke Kawaguchi
 */
public enum Flavor {
    JSON("application/javascript;charset=UTF-8") {
        public DataWriter createDataWriter(Object bean, StaplerResponse rsp) throws IOException {
            return new JSONDataWriter(rsp);
        }
        public DataWriter createDataWriter(Object bean, Writer w) throws IOException {
            return new JSONDataWriter(w);
        }
        // JSON can be written without a specific root object given. 
        public DataWriter createDataWriter(StaplerResponse rsp) throws IOException {
            return new JSONDataWriter(rsp);
        }
        public DataWriter createDataWriter(Writer w) throws IOException {
            return new JSONDataWriter(w);
        }
    },
    XML("application/xml;charset=UTF-8") {
        public DataWriter createDataWriter(Object bean, StaplerResponse rsp) throws IOException {
            return new XMLDataWriter(bean,rsp);
        }
        public DataWriter createDataWriter(Object bean, Writer w) throws IOException {
            return new XMLDataWriter(bean,w);
        }
    };

    /**
     * Content-type of this flavor, including charset "UTF-8".
     */
    public final String contentType;

    Flavor(String contentType) {
        this.contentType = contentType;
    }

    public abstract DataWriter createDataWriter(Object bean, StaplerResponse rsp) throws IOException;
    public abstract DataWriter createDataWriter(Object bean, Writer w) throws IOException;
}
