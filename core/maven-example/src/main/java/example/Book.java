package example;

import jakarta.servlet.ServletException;
import java.io.IOException;
import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.StaplerResponse2;

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
    public void doDelete( StaplerRequest2 request, StaplerResponse2 response ) throws IOException, ServletException {
        BookStore.theStore.getItems().remove(getSku());
        response.sendRedirect(request.getContextPath()+'/');
    }
}
