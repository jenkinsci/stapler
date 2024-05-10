package org.kohsuke.stapler;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import javax.servlet.ServletContext;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;

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
