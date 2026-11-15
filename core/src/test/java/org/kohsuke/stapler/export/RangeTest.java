package org.kohsuke.stapler.export;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyIterable;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class RangeTest {

    private final String[] array = new String[] {"a", "b", "c", "d", "e", "f"};
    private final List<String> list = Arrays.asList(array);
    private final Set<String> set = new LinkedHashSet<>(list);

    @Test
    void normalRange() {
        Range r = new Range(2, 4);
        assertThat(r.apply(array), contains("c", "d"));
        assertThat(r.apply(list), contains("c", "d"));
        assertThat(r.apply(set), contains("c", "d"));
    }

    @Test
    void maxOnlyRange() {
        Range r = new Range(-1, 2);
        assertThat(r.apply(array), contains("a", "b"));
        assertThat(r.apply(list), contains("a", "b"));
        assertThat(r.apply(set), contains("a", "b"));
    }

    @Test
    void minOnlyRange() {
        Range r = new Range(4, Integer.MAX_VALUE);
        assertThat(r.apply(array), contains("e", "f"));
        assertThat(r.apply(list), contains("e", "f"));
        assertThat(r.apply(set), contains("e", "f"));
    }

    @Test
    void rangeBeyondListSizeReturnsEmpty() {
        Range r = new Range(10, 20);
        assertThat(r.apply(array), empty());
        assertThat(r.apply(list), empty());
        assertThat(r.apply(set), emptyIterable());
    }

    @Test
    void rangeExactlyAtBoundaryReturnsEmpty() {
        Range r = new Range(6, 10);
        assertThat(r.apply(array), empty());
        assertThat(r.apply(list), empty());
        assertThat(r.apply(set), emptyIterable());
    }

    @Test
    void markedListUsesIteratorForEveryOverload() {
        Range r = new Range(2, 4);
        IteratedList<String> values = new IteratedList<>(list);

        Iterable<String> selected = r.apply((Iterable<String>) values);
        assertEquals(0, values.consumed);
        assertThat(selected, contains("c", "d"));
        assertEquals(4, values.consumed);

        values.consumed = 0;
        selected = r.apply((Collection<String>) values);
        assertEquals(0, values.consumed);
        assertThat(selected, contains("c", "d"));
        assertEquals(4, values.consumed);

        values.consumed = 0;
        assertThat(r.apply((List<String>) values), contains("c", "d"));
        assertEquals(4, values.consumed);
    }

    @Test
    void markedListHandlesRangesBeyondEnd() {
        IteratedList<String> values = new IteratedList<>(list);
        assertThat(new Range(10, 20).apply((Iterable<String>) values), emptyIterable());
        assertEquals(6, values.consumed);
        values.consumed = 0;
        assertThat(new Range(6, 10).apply((List<String>) values), empty());
        assertEquals(6, values.consumed);
    }

    static class IteratedList<T> extends AbstractList<T> implements AlwaysIterate {
        private final List<T> values;
        int consumed;

        IteratedList(List<T> values) {
            this.values = values;
        }

        @Override
        public int size() {
            throw new AssertionError("size must not be called");
        }

        @Override
        public T get(int index) {
            throw new AssertionError("get must not be called");
        }

        @Override
        public Iterator<T> iterator() {
            Iterator<T> delegate = values.iterator();
            return new Iterator<>() {
                @Override
                public boolean hasNext() {
                    return delegate.hasNext();
                }

                @Override
                public T next() {
                    consumed++;
                    return delegate.next();
                }
            };
        }
    }
}
