package org.kohsuke.stapler;

import java.util.AbstractList;
import java.util.Iterator;

/**
 * Single linked list which allows sharing of the suffix.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.220
 */
public class SingleLinkedList<T> extends AbstractList<T> {
    public final T head;
    public final SingleLinkedList<T> tail;

    public SingleLinkedList(T head, SingleLinkedList<T> tail) {
        this.head = head;
        this.tail = tail;
    }

    /**
     * Creates a new list by adding a new element as the head.
     */
    public SingleLinkedList<T> grow(T item) {
        return new SingleLinkedList<T>(item,this);
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            SingleLinkedList<T> next = SingleLinkedList.this;

            public boolean hasNext() {
                return next!=EMPTY_LIST;
            }

            public T next() {
                T r = next.head;
                next = next.tail;
                return r;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public T get(int index) {
        return null;
    }

    @Override
    public int size() {
        int sz = 0;
        for (SingleLinkedList<T> head=this; head!=EMPTY_LIST; head=head.tail)
            sz++;
        return sz;
    }

    @Override
    public boolean isEmpty() {
        return this==EMPTY_LIST;
    }

    public static <T> SingleLinkedList<T> empty() {
        return EMPTY_LIST;
    }

    private static final SingleLinkedList EMPTY_LIST = new SingleLinkedList(null,null);
}
