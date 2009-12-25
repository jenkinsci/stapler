/**
 * JavaScript/CSS packaging mechanism.
 *
 * <p>
 * A part of writing a web application involves reusing JavaScript libraries that are developed by 3rd parties.
 * Such JavaScript libraries often come in a package of js files, CSS, images, and so on. To integrate those
 * libraries to your application, you often need to do:
 *
 * <ul>
 * <li>Copy files into the webapp resource directories.
 * <li>If the JavaScript library depends on other JavaScript libraries, copy those, too.
 * <li>For each page where you use them, write script and link tags to load images and CSS, as well as their dependencies.
 * </ul>
 *
 * <p>
 * This tediousness hurts the small-scale reusability of JavaScript libraries.
 * The adjunct framework in Stapler attempts to solve this by allowing libraries/components to express
 * dependencies among themselves, and grouping CSS and JavaScript together, so that a single "inclusion"
 * would include everything that a JavaScript library needs.
 *
 * <h3>What packagers do</h3>
 * <ol>
 * <li>
 * Package JavaScript libraries as a jar file, including CSS and asset files, typically through Maven.
 * CSS (*.css), JavaScript (*.js), and HTML (*.html) that are only different by their extensions are grouped
 * into "adjunct", which becomes the unit of dependency management. All three aspects of an adjunct is optional.
 * More about what these files do later.
 *
 * <li>
 * If this library depends on other libraries, use Maven's dependency management to have the resulting jars
 * express dependencies.
 *
 * <li>
 * Express dependencies among adjuncts.
 *
 * </ol>
 *
 *
 * <h3>What webapp developers do</h3>
 * <ol>
 * <li>
 * Include adjunct jar files into WEB-INF/libs either directly or indirectly, typically via Maven.
 *
 * <li>
 * Bind {@link AdjunctManager} to URL by using Stapler. This object serves adjunct JavaScript, CSS, images, and so on.
 *
 * <li>
 * Use &lt;st:adjunct> tag to load adjunct into the page. This tag expands to the &lt;script> and &lt;link> tags
 * for the adjunct itself and all the dependencies. It also remembers what adjuncts are already loaded into the page,
 * so when multiple &lt;st:adjunt> tags are used to load different libraries, it won't repeatedly load the same script. 
 *
 * </ol>
 *
 *
 * <h2>Adjunct</h2>
 * <h3>Name</h3>
 * <p>
 * Adjuncts are identified by their fully qualified names, which is the package name + base name of the file name
 * (this is just like how a Java class gets its FQCN.)
 *
 * <h3>Expressing dependencies</h3>
 * <p>
 * Lines of the following form in JavaScript and CSS are interpreted by the adjunct framework to express
 * dependencies to other adjuncts. They have to start at the beginning of the line, without a leading whitespace.
 * <pre>
 * // @include fully.qualified.adjunct.name
 * /* @include fully.qualified.adjunct.name
 * </pre>
 * <p>
 * HTML file can have the following line to indicate a dependency.
 * <pre>
 * <@include fully.qualified.adjunct.name>
 * </pre>
 *
 * <h3>Page injection</h3>
 * <p>
 * Stapler loads an adjunct into a page by generating a link tag and a script tag to load the JS and CSS files,
 * respectively. The URLs these tags point to are served by {@link AdjunctManager}. If an HTML file is a part of an adjunct,
 * its content is just included inline along with script and link tags. This is useful to write a glue to load a large
 * 3rd party JavaScript libraries without modifying them or changing their names.
 */
package org.kohsuke.stapler.framework.adjunct;