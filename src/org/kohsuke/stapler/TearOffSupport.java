package org.kohsuke.stapler;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Allows "tear-off" objects to be linked to the parent object.
 *
 * <p>
 * This mechanism is used to avoid static linking optional packages,
 * so that stapler can work even when the optional dependencies are missing.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class TearOffSupport {
    private volatile Map<Class,Object> tearOffs;

    public final <T> T getTearOff(Class<T> t) {
        Map<Class,Object> m = tearOffs;
        if(m==null)     return null;
        return t.cast(m.get(t));
    }

    public final <T> T loadTearOff(Class<T> t) {
        T o = getTearOff(t);
        if(o==null) {
            try {
                o = t.getConstructor(getClass()).newInstance(this);
                setTearOff(t,o);
            } catch (InstantiationException e) {
                throw new InstantiationError(e.getMessage());
            } catch (IllegalAccessException e) {
                throw new IllegalAccessError(e.getMessage());
            } catch (InvocationTargetException e) {
                throw new Error(e);
            } catch (NoSuchMethodException e) {
                throw new NoSuchMethodError(e.getMessage());
            }
        }
        return o;
    }

    public synchronized <T> void setTearOff(Class<T> type, T instance) {
        Map<Class,Object> m = tearOffs;
        Map<Class,Object> r = m!=null ? new HashMap<Class, Object>(tearOffs) : new HashMap<Class,Object>();
        r.put(type,instance);
        tearOffs = r;
    }
}
