package org.kohsuke.stapler;

import java.io.IOException;

/**
 * A data structure that support replacing its content.
 * Primarily used to support hudson.util.PersistedList, which can be exposed as public final fields, but still
 * (re)configured by stapler on structured form submission.
 *
 * @author <a href="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a>
 * @since TODO
 */
public interface Replaceable<T> {

    /**
     * Replace the data content of current object with new value.
     * @param newValue
     * @throws IOException
     */
    void replaceBy(T newValue) throws IOException;
}