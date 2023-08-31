package org.kohsuke.stapler;

import org.junit.Test;
import org.jvnet.hudson.test.Issue;

import jakarta.servlet.ServletContext;
import java.net.MalformedURLException;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

public class IndexHtmlDispatcherTest {

    @Test
    @Issue("#109")
    public void ignoreInterfaces() throws MalformedURLException {
        ServletContext context = mock(ServletContext.class);
        when(context.getResource(any(String.class))).thenReturn(null);

        assertNull(IndexHtmlDispatcher.make(context, TestInterface.class));
    }

    private interface TestInterface {}

}
