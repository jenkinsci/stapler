package org.kohsuke.stapler.jelly.groovy;

import org.kohsuke.stapler.jelly.JellyTagFileLoader;
import org.kohsuke.stapler.jelly.CustomTagLibrary;
import org.kohsuke.MetaInfServices;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.JellyException;

import java.io.IOException;
import java.net.URL;

/**
 * @author Kohsuke Kawaguchi
 */
@MetaInfServices
public class GroovyTagFileLoader extends JellyTagFileLoader {
    public Script load(CustomTagLibrary taglib, String name, ClassLoader classLoader) throws JellyException {
        URL res = classLoader.getResource(taglib.basePath + '/' + name + ".groovy");
        if(res==null)   return null;

        try {
            GroovyClassLoaderTearOff gcl = taglib.metaClassLoader.getTearOff(GroovyClassLoaderTearOff.class);
            return gcl.parse(res);
        } catch (IOException e) {
            throw new JellyException(e);
        }
    }
}
