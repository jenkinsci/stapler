package example;

/**
 * Item in the bookstore.
 *
 * This example shows the power of the polymorphic behavior that
 * Stapler enables.
 *
 * We have two kinds of Item in this system -- Book and CD.
 * They are both accessible by the same form of URL "/items/[sku]",
 * but their index.jsp are different.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class Item {
    private final String sku;
    private final String title;

    protected Item(String sku,String title) {
        this.sku = sku;
        this.title = title;
    }

    public String getSku() {
        return sku;
    }

    public String getTitle() {
        return title;
    }
}
