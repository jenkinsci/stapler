package org.kohsuke.stapler.export;

/**
 * Signals an error that the class didn't have {@link ExportedBean}.
 *
 * @author Kohsuke Kawaguchi
 * @see Model
 */
public class NotExportableException extends IllegalArgumentException {
    private final Class type;

    public NotExportableException(Class type) {
        this(type+" doesn't have @"+ ExportedBean.class.getSimpleName(),type);
    }

    public NotExportableException(String s, Class type) {
        super(s);
        this.type = type;
    }

    public NotExportableException(String message, Throwable cause, Class type) {
        super(message, cause);
        this.type = type;
    }

    public NotExportableException(Throwable cause, Class type) {
        super(cause);
        this.type = type;
    }

    /**
     * Gets the type that didn't have {@link ExportedBean}
     */
    public Class getType() {
        return type;
    }
}
