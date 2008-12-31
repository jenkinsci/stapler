package example;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * A book in the bookstore.
 *
 * <p>
 * In this example we define a few side JSP files to
 * define views of this object. See
 * /resources/WEB-INF/side-files/example/Book/*.jsp
 *
 * @author Kohsuke Kawaguchi
 */
public class Book extends Item {

    private String isbn;

    public Book(String isbn, String title) {
        super(isbn,title);
        this.isbn = isbn;
    }


    public String getIsbn() {
        return isbn;
    }


    /**
     * Defines an action to delete this book from the store.
     */
    public void doDelete( StaplerRequest request, StaplerResponse response ) throws IOException, ServletException {
        BookStore.theStore.getItems().remove(getSku());
        response.sendRedirect(request.getContextPath()+'/');
    }
}
