package org.kohsuke.stapler.jsr269;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Very simple multi-map implmentation.
 *
 * <p>
 * We historically used Google Collections, but there have been multiple reports that
 * certain versions of Maven (and IDEs such as IntelliJ) fail to correctly compute
 * classpath for annotation processors and cause a NoClassDefFoundError.
 *
 * To work around this problem, we are now using this class.
 *
 * @author Kohsuke Kawaguchi
 */
class PoormansMultimap<K,V> {
    private final HashMap<K,List<V>> store = new HashMap<K,List<V>>();

    public void put(K k, V v) {
        List<V> l = store.get(k);
        if (l==null)
            store.put(k,l=new ArrayList<V>());
        l.add(v);
    }

    public Map<K,Collection<V>> asMap() {
        return (Map)store;
    }
}
