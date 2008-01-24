package example;



/**
 * @author Kohsuke Kawaguchi
 */
public class CD extends Item {

    private final Track[] tracks;

    public CD(String sku, String title, Track[] tracks ) {
        super(sku,title);
        this.tracks = tracks;
    }

    /**
     * Allocates the sub directory "track/[num]" to
     * the corresponding Track object.
     */
    public Track[] getTracks() {
        return tracks;
    }
}
