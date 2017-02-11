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

    private ClassAttributeBehaviour classAttribute = ClassAttributeBehaviour.IF_NEEDED;

    /**
     * Interceptor used to check if given model object or it's property should be included in serialization
     */
    private ExportInterceptor exportInterceptor = ExportInterceptor.DEFAULT;

    private boolean skipIfFail = false;

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

    public ClassAttributeBehaviour getClassAttribute() {
        return classAttribute;
    }

    /**
     * Controls the behaviour of the class attribute to be produced.
     */
    public ExportConfig withClassAttribute(ClassAttributeBehaviour cab) {
        if (cab==null)  throw new NullPointerException();
        this.classAttribute = cab;
        return this;
    }

    public ExportInterceptor getExportInterceptor() {
        return exportInterceptor;
    }

    public ExportConfig withExportInterceptor(ExportInterceptor interceptor){
        this.exportInterceptor = interceptor;
        return this;
    }

    public ExportConfig withSkipIfFail(boolean skipIfFail){
        this.skipIfFail = skipIfFail;
        return this;
    }

    public boolean isSkipIfFail() {
        return skipIfFail;
    }
}
