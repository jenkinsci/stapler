/*
 * Copyright (C) 2007 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kohsuke.stapler.export;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * This class contains static utility methods that operate on or return objects of type {@link
 * Iterator}.
 *
 * <p><i>Performance notes:</i> Unless otherwise noted, all of the iterators produced in this class
 * are <i>lazy</i>, which means that they only advance the backing iteration when absolutely
 * necessary.
 *
 * <p>See the Guava User Guide section on <a href=
 * "https://github.com/google/guava/wiki/CollectionUtilitiesExplained#iterables"> {@code
 * Iterators}</a>.
 *
 * @author Kevin Bourrillion
 * @author Jared Levy
 */
final class Iterators {
    private Iterators() {}

    /**
     * Returns a view containing the first {@code limitSize} elements of {@code iterator}. If {@code
     * iterator} contains fewer than {@code limitSize} elements, the returned view contains all of its
     * elements. The returned iterator supports {@code remove()} if {@code iterator} does.
     *
     * @param iterator the iterator to limit
     * @param limitSize the maximum number of elements in the returned iterator
     * @throws IllegalArgumentException if {@code limitSize} is negative
     */
    static <T> Iterator<T> limit(final Iterator<T> iterator, final int limitSize) {
        if (iterator == null) {
            throw new NullPointerException();
        }
        if (limitSize < 0) {
            throw new IllegalArgumentException("limit is negative");
        }
        return new Iterator<T>() {
            private int count;

            @Override
            public boolean hasNext() {
                return count < limitSize && iterator.hasNext();
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                count++;
                return iterator.next();
            }

            @Override
            public void remove() {
                iterator.remove();
            }
        };
    }
}
