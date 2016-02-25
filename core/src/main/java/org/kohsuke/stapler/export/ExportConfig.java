package org.kohsuke.stapler.export;

/**
 * Controls the output behaviour.
 *
 * @author Kohsuke Kawaguchi
 */
public class ExportConfig {
    /**
     * @deprecated
     *      Use getter and setter
     */
    public boolean prettyPrint;

    private TypeAttributeBehaviour typeAttributeBehaviour = TypeAttributeBehaviour.IF_NEEDED;

    public boolean isPrettyPrint() {
        return prettyPrint;
    }

    /**
     * If true, output will be indented to make it easier for humans to understand.
     */
    public ExportConfig withPrettyPrint(boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
        return this;
    }

    public TypeAttributeBehaviour getTypeAttributeBehaviour() {
        return typeAttributeBehaviour;
    }

    /**
     * Controls the behaviour of the type attribute to be produced.
     */
    public ExportConfig withRedundantTypes(TypeAttributeBehaviour tab) {
        if (tab==null)  throw new NullPointerException();
        this.typeAttributeBehaviour = tab;
        return this;
    }
}
