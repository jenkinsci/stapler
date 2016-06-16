package org.kohsuke.stapler.jelly.issue76;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Root of the model object.
 *
 * <p>
 * Test ground of various traversal logic.
 *
 * @author Kohsuke Kawaguchi
 */
public class Robot {
    public final Head head = new Head();

    public final List<Leg> legs = Arrays.asList(new Leg("left"),new Leg("right"));

    public final Map<String,Button> buttons = new HashMap<String, Button>();

    public Robot() {
        buttons.put("red", new Button("red"));
        buttons.put("blue",new Button("blue"));
    }

    public Object getDynamic(String part) {
        if (part.equals("arm"))
            return new Arm();
        return null;
    }
}
