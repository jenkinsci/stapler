package org.kohsuke.stapler.annotations;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.kohsuke.stapler.Function;
import org.kohsuke.stapler.FunctionList;
import org.kohsuke.stapler.lang.FieldRef;
import org.kohsuke.stapler.lang.Klass;
import org.kohsuke.stapler.lang.MethodRef;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeThat;
import static org.junit.Assume.assumeTrue;
import static org.kohsuke.stapler.annotations.StaplerPath.Helper.getPaths;

@RunWith(Theories.class)
public class StaplerPathHelperTest {

    @DataPoints
    public static Field[] someFields() {
        return Probe.class.getDeclaredFields();
    }

    @DataPoints
    public static Method[] someMethods() {
        return Probe.class.getDeclaredMethods();
    }

    private static Field field(String name) throws NoSuchFieldException {
        return Probe.class.getField(name);
    }

    private static Method method(String name) throws NoSuchFieldException {
        for (Method m : Probe.class.getDeclaredMethods()) {
            if (name.equals(m.getName())) {
                return m;
            }
        }
        fail("Cannot find a method called '" + name + "' on " + Probe.class);
        return null;
    }

    @Test
    public void helperIsAUtilityClass() throws Exception {
        Constructor<StaplerPath.Helper> constructor = StaplerPath.Helper.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        try {
            constructor.newInstance();
            fail("Construction of utility class instances should not be allowed");
        } catch (InvocationTargetException e) {
            assertThat(e.getCause(), instanceOf(IllegalAccessError.class));
        }
    }

    @Theory
    public void given__nonPublicField__when__checkingIfPath__then__notAPath(Field field) throws Exception {
        assumeThat(Modifier.isPublic(field.getModifiers()), is(false));
        assertThat(StaplerPath.Helper.isPath(field), is(false));
    }

    @Theory
    public void given__publicFieldWithAnnotation__when__checkingIfPath__then__aPath(Field field) throws Exception {
        assumeThat(Modifier.isPublic(field.getModifiers()), is(true));
        assumeThat(field.getAnnotation(StaplerPath.class), notNullValue());
        assumeThat(field.getAnnotation(StaplerPath.class).value(), not(is(StaplerPath.DYNAMIC)));
        assertThat(field.getName(), StaplerPath.Helper.isPath(field), is(true));
    }

    @Theory
    public void given__publicFieldWithoutAnnotation__when__checkingIfPath__then__notAPath(Field field) throws Exception {
        assumeThat(Modifier.isPublic(field.getModifiers()), is(true));
        assumeThat(field.getAnnotation(StaplerPath.class), nullValue());
        assumeThat(field.getAnnotation(StaplerPaths.class), nullValue());
        for (Annotation a: field.getAnnotations()) {
            assumeThat(a.annotationType().getAnnotation(StaplerPath.Implicit.class), nullValue());
        }
        assertThat(field.getName(), StaplerPath.Helper.isPath(field), is(false));
    }

    @Theory
    public void given__publicFieldWithAnnotations__when__checkingIfPath__then__aPath(Field field) throws Exception {
        assumeThat(Modifier.isPublic(field.getModifiers()), is(true));
        assumeThat(field.getAnnotation(StaplerPaths.class), notNullValue());
        boolean haveOne = false;
        boolean haveDynamic = false;
        for (StaplerPath p: field.getAnnotation(StaplerPaths.class).value()) {
            if (StaplerPath.DYNAMIC.equals(p.value())) {
                haveDynamic = true;
            } else {
                haveOne = true;
            }

        }
        assumeThat(haveOne || !haveDynamic, is(true));
        assertThat(field.getName(), StaplerPath.Helper.isPath(field), is(true));
    }

    @Theory
    public void given__publicFieldWithOnlyDynamicAnnotations__when__checkingIfPath__then__notAPath(Field field) throws Exception {
        assumeThat(Modifier.isPublic(field.getModifiers()), is(true));
        assumeThat(field.getAnnotation(StaplerPaths.class), notNullValue());
        boolean haveOne = false;
        boolean haveDynamic = false;
        for (StaplerPath p: field.getAnnotation(StaplerPaths.class).value()) {
            if (StaplerPath.DYNAMIC.equals(p.value())) {
                haveDynamic = true;
            } else {
                haveOne = true;
            }

        }
        assumeThat(haveDynamic && !haveOne, is(true));
        assertThat(field.getName(), StaplerPath.Helper.isPath(field), is(false));
    }

    @Theory
    public void given__nonPublicMethod__when__checkingIfPath__then__notAPath(Method method) throws Exception {
        assumeThat(Modifier.isPublic(method.getModifiers()), is(false));
        assertThat(StaplerPath.Helper.isPath(method), is(false));
    }

    @Theory
    public void given__publicMethodWithAnnotation__when__checkingIfPath__then__aPath(Method method) throws Exception {
        assumeThat(Modifier.isPublic(method.getModifiers()), is(true));
        assumeThat(method.getAnnotation(StaplerPath.class), notNullValue());
        assumeThat(method.getAnnotation(StaplerPath.class).value(), not(is(StaplerPath.DYNAMIC)));
        assertThat(method.getName(), StaplerPath.Helper.isPath(method), is(true));
    }

    @Theory
    public void given__publicMethodWithoutAnnotation__when__checkingIfPath__then__notAPath(Method method)
            throws Exception {
        assumeThat(Modifier.isPublic(method.getModifiers()), is(true));
        assumeThat(method.getAnnotation(StaplerPath.class), nullValue());
        assumeThat(method.getAnnotation(StaplerPaths.class), nullValue());
        for (Annotation a : method.getAnnotations()) {
            assumeThat(a.annotationType().getAnnotation(StaplerPath.Implicit.class), nullValue());
        }
        assertThat(method.getName(), StaplerPath.Helper.isPath(method), is(false));
    }

    @Theory
    public void given__publicMethodWithoutImplied__when__checkingIfPath__then__aPath(Method method)
            throws Exception {
        assumeThat(Modifier.isPublic(method.getModifiers()), is(true));
        assumeThat(method.getAnnotation(StaplerPath.class), nullValue());
        assumeThat(method.getAnnotation(StaplerPaths.class), nullValue());
        boolean haveImplied = false;
        for (Annotation a : method.getAnnotations()) {
            haveImplied = haveImplied || a.annotationType().getAnnotation(StaplerPath.Implicit.class) != null;
        }
        assumeThat(haveImplied, is(true));
        assertThat(method.getName(), StaplerPath.Helper.isPath(method), is(true));
    }

    @Theory
    public void given__publicMethodWithAnnotations__when__checkingIfPath__then__aPath(Method method) throws Exception {
        assumeThat(Modifier.isPublic(method.getModifiers()), is(true));
        assumeThat(method.getAnnotation(StaplerPaths.class), notNullValue());
        assertThat(method.getName(), StaplerPath.Helper.isPath(method), is(true));
    }

    @Theory
    public void given__publicMethodWithOnlyDynamicAnnotations__when__checkingIfPath__then__aPath(Method method)
            throws Exception {
        assumeThat(Modifier.isPublic(method.getModifiers()), is(true));
        assumeThat(method.getAnnotation(StaplerPaths.class), notNullValue());
        boolean haveStatic = false;
        boolean haveDynamic = false;
        for (StaplerPath p : method.getAnnotation(StaplerPaths.class).value()) {
            if (StaplerPath.DYNAMIC.equals(p.value())) {
                haveDynamic = true;
            } else {
                haveStatic = true;
            }

        }
        assumeThat(haveDynamic && !haveStatic, is(true));
        assertThat(method.getName(), StaplerPath.Helper.isPath(method), is(true));
    }

    @Test
    public void given__publicFieldNoAnnotations__when__getPaths__then__noNames() throws Exception {
        assertThat(StaplerPath.Helper.getPaths(field("pubFieldSansAnnotation")),
                StaplerPathHelperTest.<String>setEquivalence());
    }

    @Theory
    public void given__nonPublicField__when__getPaths__then__noNames(Field field) throws Exception {
        assumeThat(Modifier.isPublic(field.getModifiers()), is(false));
        assertThat(StaplerPath.Helper.getPaths(field),
                StaplerPathHelperTest.<String>setEquivalence());
    }

    @Test
    public void given__publicFieldDefaultStaplerPath__when__getPaths__then__nameInferred() throws Exception {
        assertThat(StaplerPath.Helper.getPaths(field("pubFieldWithAnnotation")),
                setEquivalence("pubFieldWithAnnotation"));
    }

    @Test
    public void given__publicFieldCustomStaplerPath__when__getPaths__then__nameAsSpecified() throws Exception {
        assertThat(StaplerPath.Helper.getPaths(field("pubFieldWithAnnotationName")),
                setEquivalence("named-public-field"));
    }

    @Test
    public void given__publicFieldDynamicStaplerPath__when__getPaths__then__noNameInferred() throws Exception {
        assertThat(StaplerPath.Helper.getPaths(field("pubFieldWithAnnotationDynamic")),
                StaplerPathHelperTest.<String>setEquivalence());
    }

    @Test
    public void given__publicFieldIndexStaplerPath__when__getPaths__then__nameIndex() throws Exception {
        assertThat(StaplerPath.Helper.getPaths(field("pubFieldWithAnnotationIndex")),
                setEquivalence(""));
    }

    @Test
    public void given__publicFieldMultipleStaplerPath__when__getPaths__then__namesAsSpecified() throws Exception {
        assertThat(StaplerPath.Helper.getPaths(field("pubFieldWithAnnotationMultiple")),
                setEquivalence("pubFieldWithAnnotationMultiple", "alternative-path"));
    }

    @Test
    public void given__publicFieldMultipleStaplerPathWithDynamic__when__getPaths__then__namesAsSpecified() throws Exception {
        assertThat(StaplerPath.Helper.getPaths(field("pubFieldWithAnnotationMultipleAndDynamic")),
                setEquivalence("pubFieldWithAnnotationMultipleAndDynamic", "alternative-path"));
    }

    @Test
    public void given__publicFieldEmptyStaplerPaths__when__getPaths__then__noNamesInferred() throws Exception {
        assertThat(StaplerPath.Helper.getPaths(field("pubFieldWithAnnotationEmpty")),
                setEquivalence("pubFieldWithAnnotationEmpty"));
    }

    @Test
    public void given__publicFieldImplicit__when__getPaths__then__nameInferred() throws Exception {
        assertThat(StaplerPath.Helper.isPath(field("pubFieldWithImplicit")), is(true));
        assertThat(StaplerPath.Helper.getPaths(field("pubFieldWithImplicit")),
                setEquivalence("pubFieldWithImplicit"));
    }

    @Test
    public void given__publicFieldImplicitAndPathInferred__when__getPaths__then__nameInferred() throws Exception {
        assertThat(StaplerPath.Helper.getPaths(field("pubFieldWithImplicitAndPath")),
                setEquivalence("pubFieldWithImplicitAndPath"));
    }

    @Test
    public void given__publicFieldImplicitAndPathSpecified__when__getPaths__then__nameSpecified() throws Exception {
        assertThat(StaplerPath.Helper.getPaths(field("pubFieldWithImplicitAndPathName")),
                setEquivalence("named-not-inferred"));
    }

    @Test
    public void given__publicFieldImplicitAndPathsSpecified__when__getPaths__then__namesSpecified() throws Exception {
        assertThat(StaplerPath.Helper.getPaths(field("pubFieldWithImplicitAndPathNames")),
                setEquivalence("", "named-not-inferred"));
    }

    @Test
    public void given__publicMethodNoAnnotations__when__getPaths__then__noNames() throws Exception {
        assertThat(StaplerPath.Helper.getPaths(method("doPublicSansAnnotation")),
                StaplerPathHelperTest.<String>setEquivalence());
    }

    @Theory
    public void given__nonPublicMethod__when__getPaths__then__noNames(Method method) throws Exception {
        assumeThat(Modifier.isPublic(method.getModifiers()), is(false));
        assertThat(StaplerPath.Helper.getPaths(method),
                StaplerPathHelperTest.<String>setEquivalence());
    }

    @Test
    public void given__publicMethodPathInferred__when__getPaths__then__nameInferredWithPrefixRemoved()
            throws Exception {
        assertThat(StaplerPath.Helper.getPaths(method("doPublicWithAnnotation")),
                setEquivalence("publicWithAnnotation"));
    }

    @Test
    public void given__publicMethodPathInferredNoPrefix__when__getPaths__then__nameInferred()
            throws Exception {
        assertThat(StaplerPath.Helper.getPaths(method("publicWithAnnotation")),
                setEquivalence("publicWithAnnotation"));
    }

    @Test
    public void given__publicMethodPathInferredPrefixShortestName__when__getPaths__then__nameInferred()
            throws Exception {
        assertThat(StaplerPath.Helper.getPaths(method("doA")),
                setEquivalence("a"));
    }

    @Test
    public void given__publicMethodPathInferredPrefixShortName__when__getPaths__then__nameInferred()
            throws Exception {
        assertThat(StaplerPath.Helper.getPaths(method("doAt")),
                setEquivalence("at"));
    }

    @Test
    public void given__publicMethodPathImpliedPrefixTooShortName__when__getPaths__then__nameInferred()
            throws Exception {
        assertThat(StaplerPath.Helper.getPaths(method("js")),
                setEquivalence("js"));
    }

    @Test
    public void given__publicMethodPathImpliedPrefixShortestName__when__getPaths__then__nameInferred()
            throws Exception {
        assertThat(StaplerPath.Helper.getPaths(method("jsA")),
                setEquivalence("a"));
    }

    @Test
    public void given__publicMethodPathImpliedPrefixShortName__when__getPaths__then__nameInferred()
            throws Exception {
        assertThat(StaplerPath.Helper.getPaths(method("jsAt")),
                setEquivalence("at"));
    }

    @Test
    public void given__publicMethodPathSpecified__when__getPaths__then__nameAsSpecified()
            throws Exception {
        assertThat(StaplerPath.Helper.getPaths(method("doPublicWithAnnotationName")),
                setEquivalence("named-public-method"));
    }

    @Test
    public void given__publicMethodPathIndex__when__getPaths__then__nameIndex()
            throws Exception {
        assertThat(StaplerPath.Helper.getPaths(method("doPublicWithAnnotationIndex")),
                setEquivalence(StaplerPath.INDEX));
    }

    @Test
    public void given__publicMethodPathDynamic__when__getPaths__then__nameEmpty()
            throws Exception {
        assertThat(StaplerPath.Helper.getPaths(method("doPublicWithAnnotationDynamic")),
                StaplerPathHelperTest.<String>setEquivalence());
    }

    @Test
    public void given__publicMethodPathsEmpty__when__getPaths__then__nameInferred()
            throws Exception {
        assertThat(StaplerPath.Helper.getPaths(method("doPublicWithAnnotationsEmpty")),
                setEquivalence("publicWithAnnotationsEmpty"));
    }

    @Test
    public void given__publicMethodPaths__when__getPaths__then__nameInferredAndSpecified()
            throws Exception {
        assertThat(StaplerPath.Helper.getPaths(method("doPublicWithAnnotations")),
                setEquivalence("publicWithAnnotations", "alternative"));
        assertThat(StaplerPath.Helper.isDynamic(method("doPublicWithAnnotations")),
                is(true));
    }

    @Test
    public void given__publicMethodPathsDynamic__when__getPaths__then__nameEmpty()
            throws Exception {
        assertThat(StaplerPath.Helper.getPaths(method("doPublicWithDynamicAnnotations")),
                StaplerPathHelperTest.<String>setEquivalence());
    }

    @Test
    public void given__publicGetMethod__when__getPaths__then__nameInferred()
            throws Exception {
        assertThat(StaplerPath.Helper.getPaths(method("doGet")),
                setEquivalence("get"));
    }

    @Test
    public void given__publicPostMethod__when__getPaths__then__nameInferred()
            throws Exception {
        assertThat(StaplerPath.Helper.getPaths(method("doPost")),
                setEquivalence("post"));
    }

    @Test
    public void given__publicPutMethod__when__getPaths__then__nameInferred()
            throws Exception {
        assertThat(StaplerPath.Helper.getPaths(method("doPut")),
                setEquivalence("put"));
    }

    @Test
    public void given__publicCustomMethod__when__getPaths__then__nameInferred()
            throws Exception {
        assertThat(StaplerPath.Helper.getPaths(method("doGetAlt")),
                setEquivalence("getAlt"));
    }

    @Test
    public void given__publicGetIndexMethod__when__getPaths__then__nameIndex()
            throws Exception {
        assertThat(StaplerPath.Helper.getPaths(method("doGetIndex")),
                setEquivalence(StaplerPath.INDEX));
    }

    @Test
    public void given__publicPostIndexMethod__when__getPaths__then__nameInferred()
            throws Exception {
        assertThat(StaplerPath.Helper.getPaths(method("doPostIndex")),
                setEquivalence(StaplerPath.INDEX));
    }

    @Test
    public void given__differentInferredPrefixes__when__noPrefix__then__nameVerbatim()
            throws Exception {
        assertThat(StaplerPath.Helper.getPaths(method("manyPrefixes")),
                setEquivalence("manyPrefixes"));
    }

    @Test
    public void given__differentInferredPrefixes__when__firstPrefix__then__nameInferred()
            throws Exception {
        assertThat(StaplerPath.Helper.getPaths(method("doManyPrefixes")),
                setEquivalence("manyPrefixes"));
    }

    @Test
    public void given__differentInferredPrefixes__when__secondPrefix__then__nameInferred()
            throws Exception {
        assertThat(StaplerPath.Helper.getPaths(method("jsManyPrefixes")),
                setEquivalence("manyPrefixes"));
    }

    @Test
    public void given__differentInferredPrefixes__when__duplicatePrefix__then__onlyFirstRemoved()
            throws Exception {
        assertThat(StaplerPath.Helper.getPaths(method("jsDoManyPrefixes")),
                setEquivalence("doManyPrefixes"));
        assertThat(StaplerPath.Helper.getPaths(method("doJsManyPrefixes")),
                setEquivalence("jsManyPrefixes"));
    }

    @Test
    public void given__repeatedPrefixes__when__getPaths__then__onlyFirstRemoved()
            throws Exception {
        assertThat(StaplerPath.Helper.getPaths(method("doDoManyPrefixes")),
                setEquivalence("doManyPrefixes"));
        assertThat(StaplerPath.Helper.getPaths(method("jsJsManyPrefixes")),
                setEquivalence("jsManyPrefixes"));
    }

    @Test
    public void given__navigatePrefixNoAnnotation__when__getPaths__then__noNames()
            throws Exception {
        assertThat(StaplerPath.Helper.getPaths(method("getNoAnnotation")),
                StaplerPathHelperTest.<String>setEquivalence());
    }

    @Test
    public void given__navigatePrefixInferAnnotation__when__getPaths__then__nameInferred()
            throws Exception {
        assertThat(StaplerPath.Helper.getPaths(method("getInferAnnotation")),
                setEquivalence("inferAnnotation"));
    }

    @Test
    public void given__navigatePrefixInferAnnotation__when__isPath__then__true()
            throws Exception {
        assertThat(StaplerPath.Helper.isPath(method("getInferAnnotation")), is(true));
    }

    @Test
    public void given__navigateNoPrefixInferAnnotation__when__getPaths__then__nameInferred()
            throws Exception {
        assertThat(StaplerPath.Helper.getPaths(method("inferAnnotation")),
                setEquivalence("inferAnnotation"));
    }

    @Test
    public void given__navigatePrefixExplicitAnnotation__when__getPaths__then__nameExplicit()
            throws Exception {
        assertThat(StaplerPath.Helper.getPaths(method("getExplicitAnnotation")),
                setEquivalence("alternative"));
    }

    @Test
    public void given__navigateNoPrefixExplicitAnnotation__when__getPaths__then__nameExplicit()
            throws Exception {
        assertThat(StaplerPath.Helper.getPaths(method("explicitAnnotation")),
                setEquivalence("alternative"));
    }

    @Test
    public void given__navigatePrefixDynamicAnnotation__when__getPaths__then__nameEmpty()
            throws Exception {
        assertThat(StaplerPath.Helper.getPaths(method("getDynamic")),
                StaplerPathHelperTest.<String>setEquivalence());
    }

    @Test
    public void given__navigateNoPrefixDynamicAnnotation__when__getPaths__then__nameEmpty()
            throws Exception {
        assertThat(StaplerPath.Helper.getPaths(method("dynamic")),
                StaplerPathHelperTest.<String>setEquivalence());
    }

    @Test
    public void given__navigatePrefixIndexAnnotation__when__getPaths__then__nameIndex()
            throws Exception {
        assertThat(StaplerPath.Helper.getPaths(method("getIndex")),
                setEquivalence(StaplerPath.INDEX));
    }

    @Test
    public void given__navigateNoPrefixIndexAnnotation__when__getPaths__then__nameExplicit()
            throws Exception {
        assertThat(StaplerPath.Helper.getPaths(method("index")),
                setEquivalence(StaplerPath.INDEX));
    }

    @Test
    public void given__publicMethodPathNotDynamic__when__isDynamic__then__false()
            throws Exception {
        assertThat(StaplerPath.Helper.isDynamic(method("doPublicWithAnnotation")), is(false));
        assertThat(StaplerPath.Helper.isDynamic(method("doPublicWithAnnotationName")), is(false));
        assertThat(StaplerPath.Helper.isDynamic(method("doPublicWithAnnotationIndex")), is(false));
    }

    @Test
    public void given__publicMethodPathDynamic__when__isDynamic__then__true()
            throws Exception {
        assertThat(StaplerPath.Helper.isDynamic(method("doPublicWithAnnotationDynamic")), is(true));
    }

    @Test
    public void given__publicMethodPathsDynamic__when__isDynamic__then__true()
            throws Exception {
        assertThat(StaplerPath.Helper.isDynamic(method("doPublicWithDynamicAnnotations")), is(true));
    }

    @Theory
    public void given__nonPublicMethod__when__isDynamic__then__notAPath(Method method) throws Exception {
        assumeThat(Modifier.isPublic(method.getModifiers()), is(false));
        assertThat(StaplerPath.Helper.isDynamic(method), is(false));
    }

    @Test
    public void given__navigatePrefixDynamicAnnotation__when__isDynamic__then__dynamic()
            throws Exception {
        assertThat(StaplerPath.Helper.isDynamic(method("getDynamic")), is(true));
    }

    @Test
    public void given__navigateNoPrefixDynamicAnnotation__when__isDynamic__then__dynamic()
            throws Exception {
        assertThat(StaplerPath.Helper.isDynamic(method("dynamic")), is(true));
    }

    @Test
    public void viaFunctions() throws Exception {
        for (Function function: new FunctionList(Klass.java(Probe.class).getFunctions()).staplerPath()) {
            assertThat(StaplerPath.Helper.isPath(function), is(true));
            Method m = method(function.getName());
            assertThat(StaplerPath.Helper.isDynamic(function), is(StaplerPath.Helper.isDynamic(m)));
            assertThat(StaplerPath.Helper.getPaths(function), is(getPaths(m)));
        }
    }

    @Theory
    public void viaMethodRefs(Method method) throws Exception {
        assertThat(StaplerPath.Helper.isPath(MethodRef.wrap(method)), is(StaplerPath.Helper.isPath(method)));
        assertThat(StaplerPath.Helper.isDynamic(MethodRef.wrap(method)), is(StaplerPath.Helper.isDynamic(method)));
        assertThat(StaplerPath.Helper.getPaths(MethodRef.wrap(method)), setEquivalence(StaplerPath.Helper.getPaths(method)));
    }

    @Theory
    public void viaFieldRefs(Field field) throws Exception {
        assertThat(StaplerPath.Helper.isPath(FieldRef.wrap(field)), is(StaplerPath.Helper.isPath(field)));
        assertThat(StaplerPath.Helper.getPaths(FieldRef.wrap(field)), setEquivalence(StaplerPath.Helper.getPaths(field)));
    }

    public static class Probe {

        protected String protFieldSansAnnotation;
        @StaplerPath
        protected String protFieldWithAnnotation;
        public String pubFieldSansAnnotation;
        @StaplerPath
        public String pubFieldWithAnnotation;
        @StaplerPath("named-public-field")
        public String pubFieldWithAnnotationName;
        @StaplerPath(StaplerPath.DYNAMIC)
        public String pubFieldWithAnnotationDynamic;
        @StaplerPath(StaplerPath.INDEX)
        public String pubFieldWithAnnotationIndex;
        @StaplerPaths({
                              @StaplerPath(),
                              @StaplerPath("alternative-path")
                      })
        public String pubFieldWithAnnotationMultiple;
        @StaplerPaths({@StaplerPath(), @StaplerPath(StaplerPath.DYNAMIC), @StaplerPath("alternative-path")})
        public String pubFieldWithAnnotationMultipleAndDynamic;
        @StaplerPaths({})
        public String pubFieldWithAnnotationEmpty;
        @CustomStaplerImplicitField
        public String pubFieldWithImplicit;
        @CustomStaplerImplicitField
        @StaplerPath
        public String pubFieldWithImplicitAndPath;
        @CustomStaplerImplicitField
        @StaplerPath("named-not-inferred")
        public String pubFieldWithImplicitAndPathName;
        @CustomStaplerImplicitField
        @StaplerPaths({@StaplerPath(StaplerPath.INDEX), @StaplerPath("named-not-inferred")})
        public String pubFieldWithImplicitAndPathNames;
        @Deprecated
        public String deprecated;
        @StaplerPaths({})
        public String emptyPaths;
        @StaplerPaths({@StaplerPath(StaplerPath.DYNAMIC)})
        public String dynamicPaths;
        @StaplerPaths({@StaplerPath(StaplerPath.DYNAMIC), @StaplerPath(StaplerPath.DYNAMIC)})
        public String manyDynamicPaths;
        @StaplerPaths({@StaplerPath(),@StaplerPath(StaplerPath.DYNAMIC)})
        public String manyPaths;

        protected void doProtectedSansAnnotation() {
        }

        @StaplerPath
        @StaplerMethod(StaplerMethod.ALL)
        protected void doProtectedWithAnnotation() {
        }

        public void doPublicSansAnnotation() {
        }

        @StaplerPath
        @StaplerMethod(StaplerMethod.ALL)
        public void doPublicWithAnnotation() {
        }

        @StaplerPath
        @StaplerMethod(StaplerMethod.ALL)
        public void publicWithAnnotation() {
        }

        @StaplerPath
        @StaplerMethod(StaplerMethod.ALL)
        public void doA() {
        }

        @StaplerPath
        @StaplerMethod(StaplerMethod.ALL)
        public void doAt() {
        }

        @StaplerRMI
        public void js() {
        }

        @StaplerRMI
        public void jsA() {
        }

        @StaplerRMI
        public void jsAt() {
        }

        @StaplerPath("named-public-method")
        @StaplerMethod(StaplerMethod.ALL)
        public void doPublicWithAnnotationName() {
        }

        @StaplerPath(StaplerPath.INDEX)
        @StaplerMethod(StaplerMethod.ALL)
        public void doPublicWithAnnotationIndex() {
        }

        @StaplerPath(StaplerPath.DYNAMIC)
        @StaplerMethod(StaplerMethod.ALL)
        public void doPublicWithAnnotationDynamic() {
        }

        @StaplerPaths({})
        @StaplerMethod(StaplerMethod.ALL)
        public void doPublicWithAnnotationsEmpty() {
        }

        @StaplerPaths({@StaplerPath, @StaplerPath("alternative"), @StaplerPath(StaplerPath.DYNAMIC)})
        @StaplerMethod(StaplerMethod.ALL)
        public void doPublicWithAnnotations() {
        }

        @StaplerPaths({@StaplerPath(StaplerPath.DYNAMIC), @StaplerPath(StaplerPath.DYNAMIC)})
        @StaplerMethod(StaplerMethod.ALL)
        public void doPublicWithDynamicAnnotations() {
        }

        @StaplerGET
        public void doGet() {}
        @StaplerPOST
        public void doPost() {}
        @StaplerPUT
        public void doPut() {}
        @StaplerMethod("GET")
        public void doGetAlt() {}

        @StaplerGET
        @StaplerPath(StaplerPath.INDEX)
        public void doGetIndex() {
        }

        @StaplerPOST
        @StaplerPath(StaplerPath.INDEX)
        public void doPostIndex() {
        }

        @StaplerGET
        @StaplerRMI
        public void manyPrefixes() {

        }
        @StaplerGET
        @StaplerRMI
        public void doManyPrefixes() {

        }
        @StaplerGET
        @StaplerRMI
        public void jsManyPrefixes() {

        }
        @StaplerGET
        @StaplerRMI
        public void doJsManyPrefixes() {

        }
        @StaplerGET
        @StaplerRMI
        public void jsDoManyPrefixes() {

        }
        @StaplerGET
        public void doDoManyPrefixes() {

        }
        @StaplerRMI
        public void jsJsManyPrefixes() {

        }

        public Object getNoAnnotation() {
            return null;
        }

        @StaplerPath
        public Object getInferAnnotation() {
            return null;
        }

        @StaplerPath("alternative")
        public Object getExplicitAnnotation() {
            return null;
        }

        @StaplerPath(StaplerPath.DYNAMIC)
        public Object getDynamic() {
            return null;
        }

        @StaplerPath(StaplerPath.INDEX)
        public Object getIndex() {
            return null;
        }

        @StaplerPath
        public Object inferAnnotation() {
            return null;
        }

        @StaplerPath("alternative")
        public Object explicitAnnotation() {
            return null;
        }

        @StaplerPath(StaplerPath.DYNAMIC)
        public Object dynamic() {
            return null;
        }

        @StaplerPath(StaplerPath.INDEX)
        public Object index() {
            return null;
        }
    }

    @Target({FIELD})
    @Retention(RUNTIME)
    @StaplerPath.Implicit
    @Documented
    public @interface CustomStaplerImplicitField {
    }


    private static <T> Matcher<? super Iterable<T>> setEquivalence(final T... items) {
        return new BaseMatcher<Iterable<T>>() {
            @Override
            public boolean matches(Object item) {
                Set<T> expected = new HashSet<>(Arrays.asList(items));
                Set<T> actual = new HashSet<>(expected.size());
                for (T i : (Iterable<T>) item) {
                    actual.add(i);
                }
                return expected.equals(actual);
            }

            @Override
            public void describeTo(Description description) {
                description.appendValueList("an iterable over [", ",", "]", items);
            }
        };
    }

    private static <T> Matcher<? super Iterable<T>> setEquivalence(final Iterable<T> items) {
        return new BaseMatcher<Iterable<T>>() {
            @Override
            public boolean matches(Object item) {
                Set<T> expected = new HashSet<>();
                for (T i: items) {
                    expected.add(i);
                }
                Set<T> actual = new HashSet<>(expected.size());
                for (T i : (Iterable<T>) item) {
                    actual.add(i);
                }
                return expected.equals(actual);
            }

            @Override
            public void describeTo(Description description) {
                description.appendValueList("an iterable over [", ",", "]", items);
            }
        };
    }

}
