package org.kohsuke.stapler;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

public final class MethodHandleCache {

    public static MethodHandle get(Method method) {
        return METHOD_HANDLES.getUnchecked(method);
    }

    private static final LoadingCache<Method, MethodHandle> METHOD_HANDLES = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.HOURS).build(new CacheLoader<Method, MethodHandle>() {

        private final Lookup lookup = MethodHandles.lookup();

        @Override
        public MethodHandle load(Method method) throws Exception {
            return lookup.unreflect(method);
        }
    });

    private MethodHandleCache() {}
}
