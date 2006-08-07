/*
 * Copyright  2000-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.kohsuke.stapler.subloader;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * {@link URLClassLoader} replacement.
 *
 */
public final class SubWebAppClassLoader extends ClassLoader {

    private static final Logger logger = Logger.getLogger(SubWebAppClassLoader.class.getName());

    /**
     * An enumeration of all resources of a given name found within the
     * classpath of this class loader. This enumeration is used by the
     * ClassLoader.findResources method, which is in
     * turn used by the ClassLoader.getResources method.
     *
     * @see SubWebAppClassLoader#findResources(String)
     * @see ClassLoader#getResources(String)
     */
    private class ResourceEnumeration implements Enumeration<URL> {
        /**
         * The name of the resource being searched for.
         */
        private String resourceName;

        /**
         * The index of the next classpath element to search.
         */
        private int pathElementsIndex;

        /**
         * The URL of the next resource to return in the enumeration. If this
         * field is <code>null</code> then the enumeration has been completed,
         * i.e., there are no more elements to return.
         */
        private URL nextResource;

        /**
         * Constructs a new enumeration of resources of the given name found
         * within this class loader's classpath.
         *
         * @param name the name of the resource to search for.
         */
        ResourceEnumeration(String name) {
            this.resourceName = name;
            this.pathElementsIndex = 0;
            findNextResource();
        }

        /**
         * Indicates whether there are more elements in the enumeration to
         * return.
         *
         * @return <code>true</code> if there are more elements in the
         *         enumeration; <code>false</code> otherwise.
         */
        public boolean hasMoreElements() {
            return (this.nextResource != null);
        }

        /**
         * Returns the next resource in the enumeration.
         *
         * @return the next resource in the enumeration
         */
        public URL nextElement() {
            URL ret = this.nextResource;
            findNextResource();
            return ret;
        }

        /**
         * Locates the next resource of the correct name in the classpath and
         * sets <code>nextResource</code> to the URL of that resource. If no
         * more resources can be found, <code>nextResource</code> is set to
         * <code>null</code>.
         */
        private void findNextResource() {
            URL url = null;
            while ((pathElementsIndex < pathComponents.size())
                    && (url == null)) {
                File pathComponent
                    = pathComponents.elementAt(pathElementsIndex);
                url = getResourceURL(pathComponent, this.resourceName);
                pathElementsIndex++;
            }
            this.nextResource = url;
        }
    }

    /**
     * The size of buffers to be used in this classloader.
     */
    private static final int BUFFER_SIZE = 8192;

    /**
     * The components of the classpath that the classloader searches
     * for classes.
     */
    private Vector<File> pathComponents  = new Vector<File>();

    /**
     * Indicates whether the parent class loader should be
     * consulted before trying to load with this class loader.
     */
    private boolean parentFirst = true;

    /**
     * These are the package roots that are to be loaded by the parent class
     * loader regardless of whether the parent class loader is being searched
     * first or not.
     */
    private Vector<String> systemPackages = new Vector<String>();

    /**
     * These are the package roots that are to be loaded by this class loader
     * regardless of whether the parent class loader is being searched first
     * or not.
     */
    private Vector<String> loaderPackages = new Vector<String>();

    /**
     * Whether or not this classloader will ignore the base
     * classloader if it can't find a class.
     *
     * @see #setIsolated(boolean)
     */
    private boolean ignoreBase = false;

    /**
     * A hashtable of zip files opened by the classloader (File to ZipFile).
     */
    private Hashtable<File,ZipFile> zipFiles = new Hashtable<File,ZipFile>();

    /**
     * Create an Ant Class Loader
     */
    public SubWebAppClassLoader(ClassLoader cl) {
        super(fixNull(cl));
    }

    /**
     * Set the parent for this class loader. This is the class loader to which
     * this class loader will delegate to load classes
     *
     * @param parent the parent class loader.
     */
    private static ClassLoader fixNull(ClassLoader parent) {
        if(parent==null) {
            return SubWebAppClassLoader.class.getClassLoader();
        } else {
            return parent;
        }
    }

    /**
     * Control whether class lookup is delegated to the parent loader first
     * or after this loader. Use with extreme caution. Setting this to
     * false violates the class loader hierarchy and can lead to Linkage errors
     *
     * @param parentFirst if true, delegate initial class search to the parent
     *                    classloader.
     */
    public void setParentFirst(boolean parentFirst) {
        this.parentFirst = parentFirst;
    }


    /**
     * Adds all the jar files in the given directory.
     */
    public void addJarFiles(File dir) throws IOException {
        // list up *.jar files in the appDir
        File[] jarFiles = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });

        if(jarFiles==null)
            return;     // no such dir

        for (File jar : jarFiles)
            addPathFile(jar);
    }

    /**
     * Add a file to the path. This classloader reads the manifest, if
     * available, and adds any additional class path jars specified in the
     * manifest.
     *
     * @param pathComponent the file which is to be added to the path for
     *                      this class loader
     *
     * @throws IOException if data needed from the file cannot be read.
     */
    public void addPathFile(File pathComponent) throws IOException {
        pathComponents.addElement(pathComponent);

        if (pathComponent.isDirectory()) {
            return;
        }

        String absPathPlusTimeAndLength =
            pathComponent.getAbsolutePath() + pathComponent.lastModified() + "-"
            + pathComponent.length();
        String classpath = pathMap.get(absPathPlusTimeAndLength);
        if (classpath == null) {
            ZipFile jarFile = null;
            InputStream manifestStream = null;
            try {
                jarFile = new ZipFile(pathComponent);
                manifestStream
                    = jarFile.getInputStream(new ZipEntry("META-INF/MANIFEST.MF"));

                if (manifestStream == null) {
                    return;
                }
                Manifest manifest = new Manifest(manifestStream);
                classpath = manifest.getMainAttributes().getValue("Class-Path");

            } finally {
                if (manifestStream != null) {
                    manifestStream.close();
                }
                if (jarFile != null) {
                    jarFile.close();
                }
            }
            if (classpath == null) {
                classpath = "";
            }
            pathMap.put(absPathPlusTimeAndLength, classpath);
        }

        if (!"".equals(classpath)) {
            URL baseURL = pathComponent.toURL();
            StringTokenizer st = new StringTokenizer(classpath);
            while (st.hasMoreTokens()) {
                String classpathElement = st.nextToken();
                URL libraryURL = new URL(baseURL, classpathElement);
                if (!libraryURL.getProtocol().equals("file")) {
                    logger.fine("Skipping jar library " + classpathElement
                        + " since only relative URLs are supported by this"
                        + " loader");
                    continue;
                }
                File libraryFile = new File(libraryURL.getFile());
                if (libraryFile.exists() && !isInPath(libraryFile)) {
                    addPathFile(libraryFile);
                }
            }
        }
    }

    /**
     * Returns the classpath this classloader will consult.
     *
     * @return the classpath used for this classloader, with elements
     *         separated by the path separator for the system.
     */
    public String getClasspath() {
        StringBuffer sb = new StringBuffer();
        boolean firstPass = true;
        Enumeration componentEnum = pathComponents.elements();
        while (componentEnum.hasMoreElements()) {
            if (!firstPass) {
                sb.append(System.getProperty("path.separator"));
            } else {
                firstPass = false;
            }
            sb.append(((File) componentEnum.nextElement()).getAbsolutePath());
        }
        return sb.toString();
    }

    /**
     * Sets whether this classloader should run in isolated mode. In
     * isolated mode, classes not found on the given classpath will
     * not be referred to the parent class loader but will cause a
     * ClassNotFoundException.
     *
     * @param isolated Whether or not this classloader should run in
     *                 isolated mode.
     */
    public synchronized void setIsolated(boolean isolated) {
        ignoreBase = isolated;
    }

    /**
     * Adds a package root to the list of packages which must be loaded on the
     * parent loader.
     *
     * All subpackages are also included.
     *
     * @param packageRoot The root of all packages to be included.
     *                    Should not be <code>null</code>.
     */
    public void addSystemPackageRoot(String packageRoot) {
        systemPackages.addElement(packageRoot
                                  + (packageRoot.endsWith(".") ? "" : "."));
    }

    /**
     * Adds a package root to the list of packages which must be loaded using
     * this loader.
     *
     * All subpackages are also included.
     *
     * @param packageRoot The root of all packages to be included.
     *                    Should not be <code>null</code>.
     */
    public void addLoaderPackageRoot(String packageRoot) {
        loaderPackages.addElement(packageRoot
                                  + (packageRoot.endsWith(".") ? "" : "."));
    }

    /**
     * Loads a class through this class loader even if that class is available
     * on the parent classpath.
     *
     * This ensures that any classes which are loaded by the returned class
     * will use this classloader.
     *
     * @param classname The name of the class to be loaded.
     *                  Must not be <code>null</code>.
     *
     * @return the required Class object
     *
     * @exception ClassNotFoundException if the requested class does not exist
     *                                   on this loader's classpath.
     */
    public Class forceLoadClass(String classname)
         throws ClassNotFoundException {
        logger.finer("force loading " + classname);

        Class theClass = findLoadedClass(classname);

        if (theClass == null) {
            theClass = findClass(classname);
        }

        return theClass;
    }

    /**
     * Loads a class through this class loader but defer to the parent class
     * loader.
     *
     * This ensures that instances of the returned class will be compatible
     * with instances which have already been loaded on the parent
     * loader.
     *
     * @param classname The name of the class to be loaded.
     *                  Must not be <code>null</code>.
     *
     * @return the required Class object
     *
     * @exception ClassNotFoundException if the requested class does not exist
     * on this loader's classpath.
     */
    public Class forceLoadSystemClass(String classname)
         throws ClassNotFoundException {
        logger.finer("force system loading " + classname);

        Class theClass = findLoadedClass(classname);

        if (theClass == null) {
            theClass = findBaseClass(classname);
        }

        return theClass;
    }

    /**
     * Returns a stream to read the requested resource name.
     *
     * @param name The name of the resource for which a stream is required.
     *             Must not be <code>null</code>.
     *
     * @return a stream to the required resource or <code>null</code> if the
     *         resource cannot be found on the loader's classpath.
     */
    public InputStream getResourceAsStream(String name) {

        InputStream resourceStream;
        if (isParentFirst(name)) {
            resourceStream = loadBaseResource(name);
            if (resourceStream != null) {
                logger.finer("ResourceStream for " + name
                    + " loaded from parent loader");

            } else {
                resourceStream = loadResource(name);
                if (resourceStream != null) {
                    logger.finer("ResourceStream for " + name
                        + " loaded from ant loader");
                }
            }
        } else {
            resourceStream = loadResource(name);
            if (resourceStream != null) {
                logger.finer("ResourceStream for " + name
                    + " loaded from ant loader");

            } else {
                resourceStream = loadBaseResource(name);
                if (resourceStream != null) {
                    logger.finer("ResourceStream for " + name
                        + " loaded from parent loader");
                }
            }
        }

        if (resourceStream == null) {
            logger.finer("Couldn't load ResourceStream for " + name);
        }

        return resourceStream;
    }

    /**
     * Returns a stream to read the requested resource name from this loader.
     *
     * @param name The name of the resource for which a stream is required.
     *             Must not be <code>null</code>.
     *
     * @return a stream to the required resource or <code>null</code> if
     *         the resource cannot be found on the loader's classpath.
     */
    private InputStream loadResource(String name) {
        // we need to search the components of the path to see if we can
        // find the class we want.
        InputStream stream = null;

        Enumeration e = pathComponents.elements();
        while (e.hasMoreElements() && stream == null) {
            File pathComponent = (File) e.nextElement();
            stream = getResourceStream(pathComponent, name);
        }
        return stream;
    }

    /**
     * Finds a system resource (which should be loaded from the parent
     * classloader).
     *
     * @param name The name of the system resource to load.
     *             Must not be <code>null</code>.
     *
     * @return a stream to the named resource, or <code>null</code> if
     *         the resource cannot be found.
     */
    private InputStream loadBaseResource(String name) {
        if (getParent() == null) {
            return getSystemResourceAsStream(name);
        } else {
            return getParent().getResourceAsStream(name);
        }
    }

    /**
     * Returns an inputstream to a given resource in the given file which may
     * either be a directory or a zip file.
     *
     * @param file the file (directory or jar) in which to search for the
     *             resource. Must not be <code>null</code>.
     * @param resourceName The name of the resource for which a stream is
     *                     required. Must not be <code>null</code>.
     *
     * @return a stream to the required resource or <code>null</code> if
     *         the resource cannot be found in the given file.
     */
    private InputStream getResourceStream(File file, String resourceName) {
        try {
            if (!file.exists()) {
                return null;
            }

            if (file.isDirectory()) {
                File resource = new File(file, resourceName);

                if (resource.exists()) {
                    return new FileInputStream(resource);
                }
            } else {
                // is the zip file in the cache
                ZipFile zipFile = zipFiles.get(file);
                if (zipFile == null) {
                    zipFile = new ZipFile(file);
                    zipFiles.put(file, zipFile);
                }
                ZipEntry entry = zipFile.getEntry(resourceName);
                if (entry != null) {
                    return zipFile.getInputStream(entry);
                }
            }
        } catch (Exception e) {
            logger.fine("Ignoring Exception " + e.getClass().getName()
                + ": " + e.getMessage() + " reading resource " + resourceName
                + " from " + file);
        }

        return null;
    }

    /**
     * Tests whether or not the parent classloader should be checked for
     * a resource before this one. If the resource matches both the
     * "use parent classloader first" and the "use this classloader first"
     * lists, the latter takes priority.
     *
     * @param resourceName The name of the resource to check.
     *                     Must not be <code>null</code>.
     *
     * @return whether or not the parent classloader should be checked for a
     *         resource before this one is.
     */
    private boolean isParentFirst(String resourceName) {
        // default to the global setting and then see
        // if this class belongs to a package which has been
        // designated to use a specific loader first
        // (this one or the parent one)

        // XXX - shouldn't this always return false in isolated mode?

        boolean useParentFirst = parentFirst;

        for (Enumeration e = systemPackages.elements(); e.hasMoreElements();) {
            String packageName = (String) e.nextElement();
            if (resourceName.startsWith(packageName)) {
                useParentFirst = true;
                break;
            }
        }

        for (Enumeration e = loaderPackages.elements(); e.hasMoreElements();) {
            String packageName = (String) e.nextElement();
            if (resourceName.startsWith(packageName)) {
                useParentFirst = false;
                break;
            }
        }

        return useParentFirst;
    }

    /**
     * Finds the resource with the given name. A resource is
     * some data (images, audio, text, etc) that can be accessed by class
     * code in a way that is independent of the location of the code.
     *
     * @param name The name of the resource for which a stream is required.
     *             Must not be <code>null</code>.
     *
     * @return a URL for reading the resource, or <code>null</code> if the
     *         resource could not be found or the caller doesn't have
     *         adequate privileges to get the resource.
     */
    public URL getResource(String name) {
        // we need to search the components of the path to see if
        // we can find the class we want.
        URL url = null;
        if (isParentFirst(name)) {
            url = (getParent() == null) ? super.getResource(name)
                                        : getParent().getResource(name);
        }

        if (url != null) {
            logger.finer("Resource " + name + " loaded from parent loader");

        } else {
            // try and load from this loader if the parent either didn't find
            // it or wasn't consulted.
            Enumeration e = pathComponents.elements();
            while (e.hasMoreElements() && url == null) {
                File pathComponent = (File) e.nextElement();
                url = getResourceURL(pathComponent, name);
                if (url != null) {
                    logger.finer("Resource " + name
                        + " loaded from ant loader");
                }
            }
        }

        if (url == null && !isParentFirst(name)) {
            // this loader was first but it didn't find it - try the parent

            url = (getParent() == null) ? super.getResource(name)
                : getParent().getResource(name);
            if (url != null) {
                logger.finer("Resource " + name + " loaded from parent loader");
            }
        }

        if (url == null) {
            logger.finer("Couldn't load Resource " + name);
        }

        return url;
    }

    /**
     * Returns an enumeration of URLs representing all the resources with the
     * given name by searching the class loader's classpath.
     *
     * @param name The resource name to search for.
     *             Must not be <code>null</code>.
     * @return an enumeration of URLs for the resources
     */
    protected Enumeration<URL> findResources(String name) {
        return new ResourceEnumeration(name);
    }

    /**
     * Returns the URL of a given resource in the given file which may
     * either be a directory or a zip file.
     *
     * @param file The file (directory or jar) in which to search for
     *             the resource. Must not be <code>null</code>.
     * @param resourceName The name of the resource for which a stream
     *                     is required. Must not be <code>null</code>.
     *
     * @return a stream to the required resource or <code>null</code> if the
     *         resource cannot be found in the given file object.
     */
    protected URL getResourceURL(File file, String resourceName) {
        try {
            if (!file.exists()) {
                return null;
            }

            if (file.isDirectory()) {
                File resource = new File(file, resourceName);

                if (resource.exists()) {
                    try {
                        return resource.toURL();
                    } catch (MalformedURLException ex) {
                        return null;
                    }
                }
            } else {
                ZipFile zipFile = zipFiles.get(file);
                if (zipFile == null) {
                    zipFile = new ZipFile(file);
                    zipFiles.put(file, zipFile);
                }

                ZipEntry entry = zipFile.getEntry(resourceName);
                if (entry != null) {
                    try {
                        return new URL("jar:" + file.toURL()
                            + "!/" + entry);
                    } catch (MalformedURLException ex) {
                        return null;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Loads a class with this class loader.
     *
     * This class attempts to load the class in an order determined by whether
     * or not the class matches the system/loader package lists, with the
     * loader package list taking priority. If the classloader is in isolated
     * mode, failure to load the class in this loader will result in a
     * ClassNotFoundException.
     *
     * @param classname The name of the class to be loaded.
     *                  Must not be <code>null</code>.
     * @param resolve <code>true</code> if all classes upon which this class
     *                depends are to be loaded.
     *
     * @return the required Class object
     *
     * @exception ClassNotFoundException if the requested class does not exist
     * on the system classpath (when not in isolated mode) or this loader's
     * classpath.
     */
    protected synchronized Class loadClass(String classname, boolean resolve)
         throws ClassNotFoundException {
        // 'sync' is needed - otherwise 2 threads can load the same class
        // twice, resulting in LinkageError: duplicated class definition.
        // findLoadedClass avoids that, but without sync it won't work.

        Class theClass = findLoadedClass(classname);
        if (theClass != null) {
            return theClass;
        }

        if (isParentFirst(classname)) {
            try {
                theClass = findBaseClass(classname);
                logger.finer("Class " + classname + " loaded from parent loader "
                    + "(parentFirst)");
            } catch (ClassNotFoundException cnfe) {
                theClass = findClass(classname);
                logger.finer("Class " + classname + " loaded from ant loader "
                    + "(parentFirst)");
            }
        } else {
            try {
                theClass = findClass(classname);
                logger.finer("Class " + classname + " loaded from ant loader");
            } catch (ClassNotFoundException cnfe) {
                if (ignoreBase) {
                    throw cnfe;
                }
                theClass = findBaseClass(classname);
                logger.finer("Class " + classname + " loaded from parent loader");
            }
        }

        if (resolve) {
            resolveClass(theClass);
        }

        return theClass;
    }

    /**
     * Converts the class dot notation to a filesystem equivalent for
     * searching purposes.
     *
     * @param classname The class name in dot format (eg java.lang.Integer).
     *                  Must not be <code>null</code>.
     *
     * @return the classname in filesystem format (eg java/lang/Integer.class)
     */
    private String getClassFilename(String classname) {
        return classname.replace('.', '/') + ".class";
    }

    protected Class defineClassFromData(File container, byte[] classData,
                                        String className) throws IOException {
        definePackage(container, className);
        return defineClass(className, classData, 0, classData.length,
                           null); // Project.class.getProtectionDomain());

    }

    /**
     * Get the manifest from the given jar, if it is indeed a jar and it has a
     * manifest
     *
     * @param container the File from which a manifest is required.
     *
     * @return the jar's manifest or null is the container is not a jar or it
     *         has no manifest.
     *
     * @exception IOException if the manifest cannot be read.
     */
    private Manifest getJarManifest(File container) throws IOException {
        if (container.isDirectory()) {
            return null;
        }
        JarFile jarFile = null;
        try {
            jarFile = new JarFile(container);
            return jarFile.getManifest();
        } finally {
            if (jarFile != null) {
                jarFile.close();
            }
        }
    }

    /**
     * Define the package information associated with a class.
     *
     * @param container the file containing the class definition.
     * @param className the class name of for which the package information
     *        is to be determined.
     *
     * @exception IOException if the package information cannot be read from the
     *            container.
     */
    protected void definePackage(File container, String className)
        throws IOException {
        int classIndex = className.lastIndexOf('.');
        if (classIndex == -1) {
            return;
        }

        String packageName = className.substring(0, classIndex);
        if (getPackage(packageName) != null) {
            // already defined
            return;
        }

        // define the package now
        Manifest manifest = getJarManifest(container);

        if (manifest == null) {
            definePackage(packageName, null, null, null, null, null,
                          null, null);
        } else {
            definePackage(container, packageName, manifest);
        }
    }

    /**
     * Define the package information when the class comes from a
     * jar with a manifest
     *
     * @param container the jar file containing the manifest
     * @param packageName the name of the package being defined.
     * @param manifest the jar's manifest
     */
    protected void definePackage(File container, String packageName,
                                 Manifest manifest) {
        String sectionName = packageName.replace('.', '/') + "/";

        String specificationTitle = null;
        String specificationVendor = null;
        String specificationVersion = null;
        String implementationTitle = null;
        String implementationVendor = null;
        String implementationVersion = null;
        String sealedString = null;
        URL sealBase = null;

        Attributes sectionAttributes = manifest.getAttributes(sectionName);
        if (sectionAttributes != null) {
            specificationTitle
                = sectionAttributes.getValue(Attributes.Name.SPECIFICATION_TITLE);
            specificationVendor
                = sectionAttributes.getValue(Attributes.Name.SPECIFICATION_VENDOR);
            specificationVersion
                = sectionAttributes.getValue(Attributes.Name.SPECIFICATION_VERSION);
            implementationTitle
                = sectionAttributes.getValue(Attributes.Name.IMPLEMENTATION_TITLE);
            implementationVendor
                = sectionAttributes.getValue(Attributes.Name.IMPLEMENTATION_VENDOR);
            implementationVersion
                = sectionAttributes.getValue(Attributes.Name.IMPLEMENTATION_VERSION);
            sealedString
                = sectionAttributes.getValue(Attributes.Name.SEALED);
        }

        Attributes mainAttributes = manifest.getMainAttributes();
        if (mainAttributes != null) {
            if (specificationTitle == null) {
                specificationTitle
                    = mainAttributes.getValue(Attributes.Name.SPECIFICATION_TITLE);
            }
            if (specificationVendor == null) {
                specificationVendor
                    = mainAttributes.getValue(Attributes.Name.SPECIFICATION_VENDOR);
            }
            if (specificationVersion == null) {
                specificationVersion
                    = mainAttributes.getValue(Attributes.Name.SPECIFICATION_VERSION);
            }
            if (implementationTitle == null) {
                implementationTitle
                    = mainAttributes.getValue(Attributes.Name.IMPLEMENTATION_TITLE);
            }
            if (implementationVendor == null) {
                implementationVendor
                    = mainAttributes.getValue(Attributes.Name.IMPLEMENTATION_VENDOR);
            }
            if (implementationVersion == null) {
                implementationVersion
                    = mainAttributes.getValue(Attributes.Name.IMPLEMENTATION_VERSION);
            }
            if (sealedString == null) {
                sealedString
                    = mainAttributes.getValue(Attributes.Name.SEALED);
            }
        }

        if (sealedString != null && sealedString.equalsIgnoreCase("true")) {
            try {
                sealBase = new URL("file:" + container.getPath());
            } catch (MalformedURLException e) {
                // ignore
            }
        }

        definePackage(packageName, specificationTitle, specificationVersion,
                      specificationVendor, implementationTitle,
                      implementationVersion, implementationVendor, sealBase);
    }

    /**
     * Reads a class definition from a stream.
     *
     * @param stream The stream from which the class is to be read.
     *               Must not be <code>null</code>.
     * @param classname The name of the class in the stream.
     *                  Must not be <code>null</code>.
     * @param container the file or directory containing the class.
     *
     * @return the Class object read from the stream.
     *
     * @exception IOException if there is a problem reading the class from the
     * stream.
     * @exception SecurityException if there is a security problem while
     * reading the class from the stream.
     */
    private Class getClassFromStream(InputStream stream, String classname,
                                     File container)
                throws IOException, SecurityException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int bytesRead;
        byte[] buffer = new byte[BUFFER_SIZE];

        while ((bytesRead = stream.read(buffer, 0, BUFFER_SIZE)) != -1) {
            baos.write(buffer, 0, bytesRead);
        }

        byte[] classData = baos.toByteArray();
        return defineClassFromData(container, classData, classname);
    }

    /**
     * Searches for and load a class on the classpath of this class loader.
     *
     * @param name The name of the class to be loaded. Must not be
     *             <code>null</code>.
     *
     * @return the required Class object
     *
     * @exception ClassNotFoundException if the requested class does not exist
     *                                   on this loader's classpath.
     */
    public Class findClass(String name) throws ClassNotFoundException {
        logger.finer("Finding class " + name);

        return findClassInComponents(name);
    }

    /**
     * Indicate if the given file is in this loader's path
     *
     * @param component the file which is to be checked
     *
     * @return true if the file is in the class path
     */
    protected boolean isInPath(File component) {
        for (Enumeration e = pathComponents.elements(); e.hasMoreElements();) {
            File pathComponent = (File) e.nextElement();
            if (pathComponent.equals(component)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Finds a class on the given classpath.
     *
     * @param name The name of the class to be loaded. Must not be
     *             <code>null</code>.
     *
     * @return the required Class object
     *
     * @exception ClassNotFoundException if the requested class does not exist
     * on this loader's classpath.
     */
    private Class findClassInComponents(String name)
         throws ClassNotFoundException {
        // we need to search the components of the path to see if
        // we can find the class we want.
        InputStream stream = null;
        String classFilename = getClassFilename(name);
        try {
            Enumeration e = pathComponents.elements();
            while (e.hasMoreElements()) {
                File pathComponent = (File) e.nextElement();
                try {
                    stream = getResourceStream(pathComponent, classFilename);
                    if (stream != null) {
                        logger.finer("Loaded from " + pathComponent + " "
                            + classFilename);
                        return getClassFromStream(stream, name, pathComponent);
                    }
                } catch (SecurityException se) {
                    throw se;
                } catch (IOException ioe) {
                    // ioe.printStackTrace();
                    logger.fine("Exception reading component " + pathComponent
                        + " (reason: " + ioe.getMessage() + ")");
                }
            }

            throw new ClassNotFoundException(name);
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e) {
                //ignore
            }
        }
    }

    /**
     * Finds a system class (which should be loaded from the same classloader
     * as the Ant core).
     *
     * For JDK 1.1 compatibility, this uses the findSystemClass method if
     * no parent classloader has been specified.
     *
     * @param name The name of the class to be loaded.
     *             Must not be <code>null</code>.
     *
     * @return the required Class object
     *
     * @exception ClassNotFoundException if the requested class does not exist
     * on this loader's classpath.
     */
    private Class findBaseClass(String name) throws ClassNotFoundException {
        if (getParent() == null) {
            return findSystemClass(name);
        } else {
            return getParent().loadClass(name);
        }
    }

    /**
     * Cleans up any resources held by this classloader. Any open archive
     * files are closed.
     */
    public synchronized void cleanup() {
        for (Enumeration e = zipFiles.elements(); e.hasMoreElements();) {
            ZipFile zipFile = (ZipFile) e.nextElement();
            try {
                zipFile.close();
            } catch (IOException ioe) {
                // ignore
            }
        }
        zipFiles = new Hashtable<File,ZipFile>();
    }

    /**
     * Locates the Main class through {@code META-INF/MANIFEST.MF} and
     * returns it.
     *
     * @return
     *      always non-null.
     */
    public Class loadMainClass() throws IOException, ClassNotFoundException {
        // determine the Main class name
        Enumeration<URL> res = getResources("META-INF/MANIFEST.MF");
        while(res.hasMoreElements()) {
            URL url = res.nextElement();
            InputStream is = new BufferedInputStream(url.openStream());
            try {
                Manifest mf = new Manifest(is);
                String value = mf.getMainAttributes().getValue("Dalma-Main-Class");
                if(value!=null) {
                    logger.info("Found Dalma-Main-Class="+value+" in "+url);
                    return loadClass(value);
                }
            } finally {
                is.close();
            }
        }

        // default location
        return loadClass("Main");
    }

    /** Static map of jar file/time to manifiest class-path entries */
    private static Map<String,String> pathMap = Collections.synchronizedMap(new HashMap<String,String>());
}
