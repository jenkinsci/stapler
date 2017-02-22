package org.kohsuke.stapler.export;

import javax.annotation.Nonnull;

/**
 * Controls the output behaviour.
 *
 * @author Kohsuke Kawaguchi
 */
public class ExportConfig {
    private final boolean prettyPrint;
    private final ClassAttributeBehaviour classAttribute;
    private final ExportInterceptor exportInterceptor;
    private final boolean skipIfFail;
    private final Flavor flavor;

    /**
     * Creates {@link ExportConfig} with a flavor
     *
     * @param flavor must be non-null
     */
    private ExportConfig(Flavor flavor, ExportInterceptor exportInterceptor,
                         ClassAttributeBehaviour classAttribute, boolean skipIfFail, boolean prettyPrint) {
        this.flavor = flavor;
        this.prettyPrint = prettyPrint;
        this.classAttribute = (classAttribute == null) ? ClassAttributeBehaviour.IF_NEEDED.simple(): classAttribute;
        this.exportInterceptor = exportInterceptor == null ? ExportInterceptor.DEFAULT :exportInterceptor;
        this.skipIfFail = skipIfFail;
    }

    /**
     * If true, output will be indented to make it easier for humans to understand.
     */
    public boolean isPrettyPrint() {
        return prettyPrint;
    }

    /**
     * Controls the behaviour of the class attribute to be produced.
     */
    public ClassAttributeBehaviour getClassAttribute() {
        return classAttribute;
    }

    /**
     * Controls the behaviour of the class attribute to be produced.
     */
    public ClassAttributeBehaviour getClassAttributeBehaviour(){
        return this.classAttribute;
    }

    /**
     * Gives {@link ExportInterceptor}. Always non-null.
     */
    public ExportInterceptor getExportInterceptor() {
        return exportInterceptor;
    }

    /**
     *  Tells whether to skip serialization failure.
     */
    public boolean isSkipIfFail() {
        return skipIfFail;
    }

    /**
     * Gives {@link Flavor}. Always non-null.
     */
    public Flavor getFlavor() {
        return flavor;
    }

    /**
     * {@link ExportConfig} builder
     */
    public static class Builder{
        private  boolean prettyPrint;

        private  ClassAttributeBehaviour classAttribute = ClassAttributeBehaviour.IF_NEEDED;

        private  ExportInterceptor exportInterceptor = ExportInterceptor.DEFAULT;

        private  boolean skipIfFail = false;

        private  final Flavor flavor;

        public Builder(Flavor flavor){
            this.flavor = flavor;
        }

        /**
         * Turn on or off pretty printing of serialized data.
         *
         * @param prettyPrint default false.
         *
         * @return {@link Builder} instance
         */
        public Builder prettyPrint(boolean prettyPrint){
            this.prettyPrint = prettyPrint;
            return this;
        }

        /**
         * If true serialization error will be ignored
         *
         * @param skipIfFail default false.
         *
         * @return {@link Builder} instance
         */
        public Builder skipIfFail(boolean skipIfFail){
            this.skipIfFail = skipIfFail;
            return this;
        }

        /**
         * Control how _class attribute is written
         *
         * @param classAttribute default {@link ClassAttributeBehaviour#IF_NEEDED}.
         *
         * @return {@link Builder} instance
         */
        public Builder classAttribute(@Nonnull ClassAttributeBehaviour classAttribute){
            this.classAttribute = classAttribute;
            return this;
        }

        /**
         * Controls serialization {@link @Exported} properties
         *
         * @param exportInterceptor default {@link ExportInterceptor#DEFAULT}.
         *
         * @return {@link Builder} instance
         */
        public Builder exportInterceptor(@Nonnull ExportInterceptor exportInterceptor){
            this.exportInterceptor = exportInterceptor;
            return this;
        }

        /**
         * Builds and returns {@link ExportConfig}
         */
        public @Nonnull ExportConfig build(){
            return new ExportConfig(flavor,exportInterceptor,classAttribute,skipIfFail,prettyPrint);
        }
    }
}
