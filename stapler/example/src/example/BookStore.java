package example;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

/**
 * Root object of this web application.
 *
 * This object is bound to URL '/' in this application.
 *
 * @author Kohsuke Kawaguchi
 */
public class BookStore {

    private final Map items = new Hashtable();

    /**
     * Singleton instance, to make it easier to access.
     */
    public static final BookStore theStore = new BookStore();

    private BookStore() {
        addItem(new Book("b1","The life of Calvin McCoy"));
        addItem(new CD("c1","Beethoven: Sonatas",new Track[]{
            new Track("Sonata, Op.57 'Appassionata' In F Minor: Allegro assai",180),
            new Track("Sonata, Op.53 'Waldstein' In C: Introduzione: Adagio molto",172)
        }));
    }

    private void addItem(Item item) {
        items.put(item.getSku(),item);
    }

    /**
     * Bind "/items/[sku]" to the {@link Item} object
     * in the list.
     *
     * <p>
     * Alternatively, you can define the following:
     * <pre>
     * public Item getItem(String id) {
     *     return items.get(id);
     * }
     * </pre>
     * ..., which works in the same way.
     */
    public Map getItems() {
        return items;
    }


    /**
     * Define an action method that handles requests to "/hello"
     * (and below, like "/hello/foo/bar")
     *
     * <p>
     * Action methods are useful to perform some operations in Java.
     */
    public void doHello( StaplerRequest request, StaplerResponse response ) throws IOException, ServletException {
        System.out.println("Hello operation");
        request.setAttribute("systemTime",new Long(System.currentTimeMillis()));

        // it can generate the response by itself, just like
        // servlets can do so. Or you can redirect the client.
        // Basically, you can do anything that a servlet can do.

        // the following code shows how you can forward it to
        // another object. It's almost like a relative URL where '.'
        // corresponds to 'this' object.
        response.forward(this,"helloJSP",request);
    }
}
