package io.jenkins.servlet.descriptor;

import java.util.Collection;
import java.util.Objects;
import javax.servlet.descriptor.JspPropertyGroupDescriptor;

public class JspPropertyGroupDescriptorWrapper {
    public static jakarta.servlet.descriptor.JspPropertyGroupDescriptor toJakartaJspPropertyGroupDescriptor(
            JspPropertyGroupDescriptor from) {
        Objects.requireNonNull(from);
        return new jakarta.servlet.descriptor.JspPropertyGroupDescriptor() {
            @Override
            public Collection<String> getUrlPatterns() {
                return from.getUrlPatterns();
            }

            @Override
            public String getElIgnored() {
                return from.getElIgnored();
            }

            @Override
            public String getPageEncoding() {
                return from.getPageEncoding();
            }

            @Override
            public String getScriptingInvalid() {
                return from.getScriptingInvalid();
            }

            @Override
            public String getIsXml() {
                return from.getIsXml();
            }

            @Override
            public Collection<String> getIncludePreludes() {
                return from.getIncludePreludes();
            }

            @Override
            public Collection<String> getIncludeCodas() {
                return from.getIncludeCodas();
            }

            @Override
            public String getDeferredSyntaxAllowedAsLiteral() {
                return from.getDeferredSyntaxAllowedAsLiteral();
            }

            @Override
            public String getTrimDirectiveWhitespaces() {
                return from.getTrimDirectiveWhitespaces();
            }

            @Override
            public String getDefaultContentType() {
                return from.getDefaultContentType();
            }

            @Override
            public String getBuffer() {
                return from.getBuffer();
            }

            @Override
            public String getErrorOnUndeclaredNamespace() {
                return from.getErrorOnUndeclaredNamespace();
            }
        };
    }

    public static JspPropertyGroupDescriptor fromJakartaJspPropertyGroupDescriptor(
            jakarta.servlet.descriptor.JspPropertyGroupDescriptor from) {
        Objects.requireNonNull(from);
        return new JspPropertyGroupDescriptor() {
            @Override
            public Collection<String> getUrlPatterns() {
                return from.getUrlPatterns();
            }

            @Override
            public String getElIgnored() {
                return from.getElIgnored();
            }

            @Override
            public String getPageEncoding() {
                return from.getPageEncoding();
            }

            @Override
            public String getScriptingInvalid() {
                return from.getScriptingInvalid();
            }

            @Override
            public String getIsXml() {
                return from.getIsXml();
            }

            @Override
            public Collection<String> getIncludePreludes() {
                return from.getIncludePreludes();
            }

            @Override
            public Collection<String> getIncludeCodas() {
                return from.getIncludeCodas();
            }

            @Override
            public String getDeferredSyntaxAllowedAsLiteral() {
                return from.getDeferredSyntaxAllowedAsLiteral();
            }

            @Override
            public String getTrimDirectiveWhitespaces() {
                return from.getTrimDirectiveWhitespaces();
            }

            @Override
            public String getDefaultContentType() {
                return from.getDefaultContentType();
            }

            @Override
            public String getBuffer() {
                return from.getBuffer();
            }

            @Override
            public String getErrorOnUndeclaredNamespace() {
                return from.getErrorOnUndeclaredNamespace();
            }
        };
    }
}
