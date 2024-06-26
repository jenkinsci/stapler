package io.jenkins.servlet.descriptor;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.servlet.descriptor.JspConfigDescriptor;
import javax.servlet.descriptor.JspPropertyGroupDescriptor;
import javax.servlet.descriptor.TaglibDescriptor;

public class JspConfigDescriptorWrapper {
    public static jakarta.servlet.descriptor.JspConfigDescriptor toJakartaJspConfigDescriptor(
            JspConfigDescriptor from) {
        Objects.requireNonNull(from);
        return new jakarta.servlet.descriptor.JspConfigDescriptor() {
            @Override
            public Collection<jakarta.servlet.descriptor.TaglibDescriptor> getTaglibs() {
                return from.getTaglibs().stream()
                        .map(TaglibDescriptorWrapper::toJakartaTaglibDescriptor)
                        .collect(Collectors.toList());
            }

            @Override
            public Collection<jakarta.servlet.descriptor.JspPropertyGroupDescriptor> getJspPropertyGroups() {
                return from.getJspPropertyGroups().stream()
                        .map(JspPropertyGroupDescriptorWrapper::toJakartaJspPropertyGroupDescriptor)
                        .collect(Collectors.toList());
            }
        };
    }

    public static JspConfigDescriptor fromJakartaJspConfigDescriptor(
            jakarta.servlet.descriptor.JspConfigDescriptor from) {
        Objects.requireNonNull(from);
        return new JspConfigDescriptor() {
            @Override
            public Collection<TaglibDescriptor> getTaglibs() {
                return from.getTaglibs().stream()
                        .map(TaglibDescriptorWrapper::fromJakartaTaglibDescriptor)
                        .collect(Collectors.toList());
            }

            @Override
            public Collection<JspPropertyGroupDescriptor> getJspPropertyGroups() {
                return from.getJspPropertyGroups().stream()
                        .map(JspPropertyGroupDescriptorWrapper::fromJakartaJspPropertyGroupDescriptor)
                        .collect(Collectors.toList());
            }
        };
    }
}
