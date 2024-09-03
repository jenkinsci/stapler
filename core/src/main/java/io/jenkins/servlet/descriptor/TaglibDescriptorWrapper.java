package io.jenkins.servlet.descriptor;

import java.util.Objects;
import javax.servlet.descriptor.TaglibDescriptor;

public class TaglibDescriptorWrapper {
    public static jakarta.servlet.descriptor.TaglibDescriptor toJakartaTaglibDescriptor(TaglibDescriptor from) {
        Objects.requireNonNull(from);
        return new jakarta.servlet.descriptor.TaglibDescriptor() {
            @Override
            public String getTaglibURI() {
                return from.getTaglibURI();
            }

            @Override
            public String getTaglibLocation() {
                return from.getTaglibLocation();
            }
        };
    }

    public static TaglibDescriptor fromJakartaTaglibDescriptor(jakarta.servlet.descriptor.TaglibDescriptor from) {
        Objects.requireNonNull(from);
        return new TaglibDescriptor() {
            @Override
            public String getTaglibURI() {
                return from.getTaglibURI();
            }

            @Override
            public String getTaglibLocation() {
                return from.getTaglibLocation();
            }
        };
    }
}
