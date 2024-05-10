package example;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.kohsuke.stapler.Stapler;

/**
 * This class is invoked by the container at the beginning
 * and at the end.
 *
 * @author Kohsuke Kawaguchi
 */
public class WebAppMain implements ServletContextListener {
    public void contextInitialized(ServletContextEvent event) {
        // BookStore.theStore is the singleton instance of the application
        Stapler.setRoot(event,BookStore.theStore);
    }

    public void contextDestroyed(ServletContextEvent event) {
    }
}
