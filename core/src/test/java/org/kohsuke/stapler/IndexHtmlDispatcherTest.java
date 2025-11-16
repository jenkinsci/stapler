package org.kohsuke.stapler;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.ServletContext;
import java.net.MalformedURLException;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.Issue;

class IndexHtmlDispatcherTest {

    @Test
    @Issue("#109")
    void ignoreInterfaces() throws MalformedURLException {
        ServletContext context = mock(ServletContext.class);
        when(context.getResource(any(String.class))).thenReturn(null);

        assertNull(IndexHtmlDispatcher.make(context, TestInterface.class));
    }

    private interface TestInterface {}
}
