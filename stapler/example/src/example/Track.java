package example;

/**
 * A track of a CD.
 *
 * @author Kohsuke Kawaguchi
 */
public class Track {
    private String name;
    private int length;

    public Track(String name, int length) {
        this.name = name;
        this.length = length;
    }

    public String getName() {
        return name;
    }

    public int getLength() {
        return length;
    }
}
