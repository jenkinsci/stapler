package org.kohsuke.stapler.export;

import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;

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
    },
    XML("application/xml;charset=UTF-8") {
        public DataWriter createDataWriter(Object bean, StaplerResponse rsp) throws IOException {
            return new XMLDataWriter(bean,rsp);
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
}
