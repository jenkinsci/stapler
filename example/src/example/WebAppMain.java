package example;

import org.kohsuke.stapler.Stapler;

import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;

/**
 * This class is invoked by the container at the beginning
 * and at the end.
 *
 * @author Kohsuke Kawaguchi
 */
public class WebAppMain implements ServletContextListener {
    public void contextInitialized(ServletContextEvent event) {
        Stapler.setRoot(event,BookStore.theStore);
    }

    public void contextDestroyed(ServletContextEvent event) {
    }
}
