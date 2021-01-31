package org.kohsuke.stapler.export;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


public class RangeTest {
    String[] array = new String[]{"a", "b", "c", "d", "e", "f"};
    List<String> list = Arrays.asList(array);
    Set<String> set = new LinkedHashSet<String>(list);

    @Test
    public void normalRange() {
        Range r = new Range(2,4);
        assertThat(r.apply(array), contains("c", "d"));
        assertThat(r.apply(list), contains("c", "d"));
        assertThat(r.apply(set), contains("c", "d"));
    }

    @Test
    public void maxOnlyRange() {
        Range r = new Range(-1,2);
        assertThat(r.apply(array), contains("a", "b"));
        assertThat(r.apply(list), contains("a", "b"));
        assertThat(r.apply(set), contains("a", "b"));
    }

    @Test
    public void minOnlyRange() {
        Range r = new Range(4,Integer.MAX_VALUE);
        assertThat(r.apply(array), contains("e", "f"));
        assertThat(r.apply(list), contains("e", "f"));
        assertThat(r.apply(set), contains("e", "f"));
    }
}