package org.kohsuke.stapler.annotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.InterceptorAnnotation;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeThat;

@RunWith(Theories.class)
public class StaplerMethodHelperTest {

    @DataPoints
    public static Method[] someMethods() {
        return Probe.class.getDeclaredMethods();
    }

    private static Method method(String name) throws NoSuchFieldException {
        for (Method m : Probe.class.getDeclaredMethods()) {
            if (name.equals(m.getName())) {
                return m;
            }
        }
        fail("Cannot find a method called '" + name + "' on " + StaplerPathHelperTest.Probe.class);
        return null;
    }

    @Theory
    public void given__privateMethod__when__checking__then__notStaplerMethod(Method method) {
        assumeThat(Modifier.isPrivate(method.getModifiers()), is(true));
        assertThat(StaplerMethod.Helper.isMethod(method), is(false));
    }

    @Theory
    public void given__packageMethod__when__checking__then__notStaplerMethod(Method method) {
        assumeThat(Modifier.isPrivate(method.getModifiers()), is(false));
        assumeThat(Modifier.isProtected(method.getModifiers()), is(false));
        assumeThat(Modifier.isPublic(method.getModifiers()), is(false));
        assertThat(StaplerMethod.Helper.isMethod(method), is(false));
    }

    @Theory
    public void given__protectedMethod__when__checking__then__notStaplerMethod(Method method) {
        assumeThat(Modifier.isProtected(method.getModifiers()), is(true));
        assertThat(StaplerMethod.Helper.isMethod(method), is(false));
    }

    @Theory
    public void given__publicMethodWithoutAnnotation__when__checking__then__notStaplerMethod(Method method) {
        assumeThat(Modifier.isPublic(method.getModifiers()), is(true));
        assumeThat(method.getDeclaredAnnotations().length, is(0));
        assertThat(StaplerMethod.Helper.isMethod(method), is(false));
    }

    @Theory
    public void given__publicMethodWithoutStaplerMethod__when__checking__then__notStaplerMethod(Method method) {
        assumeThat(Modifier.isPublic(method.getModifiers()), is(true));
        for (Annotation a: method.getDeclaredAnnotations()) {
            InterceptorAnnotation interceptor = a.annotationType().getAnnotation(InterceptorAnnotation.class);
            assumeThat(interceptor, nullValue());
        }
        assertThat(StaplerMethod.Helper.isMethod(method), is(false));
    }

    @Theory
    public void given__publicMethodWithStaplerMethod__when__checking__then__staplerMethod(Method method) {
        assumeThat(Modifier.isPublic(method.getModifiers()), is(true));
        InterceptorAnnotation interceptor = null;
        for (Annotation a: method.getDeclaredAnnotations()) {
            interceptor = a.annotationType().getAnnotation(InterceptorAnnotation.class);
            if (interceptor != null) {
                break;
            }
        }
        assumeThat(interceptor, notNullValue());
        assertThat(StaplerMethod.Helper.isMethod(method), is(true));
    }

    public static class Probe {
        private static Object getPrivateStaticNoAnnotation() {
            return null;
        }

        static Object getPackageStaticNoAnnotation() {
            return null;
        }

        protected static Object getProtectedStaticNoAnnotation() {
            return null;
        }

        public static Object getPublicStaticNoAnnotation() {
            return null;
        }

        private static void doPrivateStaticNoAnnotation(StaplerRequest req, StaplerResponse rsp) {
        }

        static void doPackageStaticNoAnnotation(StaplerRequest req, StaplerResponse rsp) {
        }

        protected static void doProtectedStaticNoAnnotation(StaplerRequest req, StaplerResponse rsp) {
        }

        public static void doPublicStaticNoAnnotation(StaplerRequest req, StaplerResponse rsp) {
        }

        private Object getPrivateNoAnnotation() {
            return null;
        }

        static Object getPackageNoAnnotation() {
            return null;
        }

        protected Object getProtectedNoAnnotation() {
            return null;
        }

        public Object getPublicNoAnnotation() {
            return null;
        }

        private void doPrivateNoAnnotation(StaplerRequest req, StaplerResponse rsp) {
        }

        void doPackageNoAnnotation(StaplerRequest req, StaplerResponse rsp) {
        }

        protected void doProtectedNoAnnotation(StaplerRequest req, StaplerResponse rsp) {
        }

        public void doPublicNoAnnotation(StaplerRequest req, StaplerResponse rsp) {
        }

        @StaplerPath
        private static Object getPrivateStaticStaplerPath() {
            return null;
        }

        @StaplerPath
        static Object getPackageStaticStaplerPath() {
            return null;
        }

        @StaplerPath
        protected static Object getProtectedStaticStaplerPath() {
            return null;
        }

        @StaplerPath
        public static Object getPublicStaticStaplerPath() {
            return null;
        }

        @StaplerPath
        private static void doPrivateStaticStaplerPath(StaplerRequest req, StaplerResponse rsp) {
        }

        @StaplerPath
        static void doPackageStaticStaplerPath(StaplerRequest req, StaplerResponse rsp) {
        }

        @StaplerPath
        protected static void doProtectedStaticStaplerPath(StaplerRequest req, StaplerResponse rsp) {
        }

        @StaplerPath
        public static void doPublicStaticStaplerPath(StaplerRequest req, StaplerResponse rsp) {
        }

        @StaplerPath
        private Object getPrivateStaplerPath() {
            return null;
        }

        @StaplerPath
        static Object getPackageStaplerPath() {
            return null;
        }

        @StaplerPath
        protected Object getProtectedStaplerPath() {
            return null;
        }

        @StaplerPath
        public Object getPublicStaplerPath() {
            return null;
        }

        @StaplerPath
        private void doPrivateStaplerPath(StaplerRequest req, StaplerResponse rsp) {
        }

        @StaplerPath
        void doPackageStaplerPath(StaplerRequest req, StaplerResponse rsp) {
        }

        @StaplerPath
        protected void doProtectedStaplerPath(StaplerRequest req, StaplerResponse rsp) {
        }

        @StaplerPath
        public void doPublicStaplerPath(StaplerRequest req, StaplerResponse rsp) {
        }

        @StaplerGET
        private static Object getPrivateStaticStaplerGET() {
            return null;
        }

        @StaplerGET
        static Object getPackageStaticStaplerGET() {
            return null;
        }

        @StaplerGET
        protected static Object getProtectedStaticStaplerGET() {
            return null;
        }

        @StaplerGET
        public static Object getPublicStaticStaplerGET() {
            return null;
        }

        @StaplerGET
        private static void doPrivateStaticStaplerGET(StaplerRequest req, StaplerResponse rsp) {
        }

        @StaplerGET
        static void doPackageStaticStaplerGET(StaplerRequest req, StaplerResponse rsp) {
        }

        @StaplerGET
        protected static void doProtectedStaticStaplerGET(StaplerRequest req, StaplerResponse rsp) {
        }

        @StaplerGET
        public static void doPublicStaticStaplerGET(StaplerRequest req, StaplerResponse rsp) {
        }

        @StaplerGET
        private Object getPrivateStaplerGET() {
            return null;
        }

        @StaplerGET
        static Object getPackageStaplerGET() {
            return null;
        }

        @StaplerGET
        protected Object getProtectedStaplerGET() {
            return null;
        }

        @StaplerGET
        public Object getPublicStaplerGET() {
            return null;
        }

        @StaplerGET
        private void doPrivateStaplerGET(StaplerRequest req, StaplerResponse rsp) {
        }

        @StaplerGET
        void doPackageStaplerGET(StaplerRequest req, StaplerResponse rsp) {
        }

        @StaplerGET
        protected void doProtectedStaplerGET(StaplerRequest req, StaplerResponse rsp) {
        }

        @StaplerGET
        public void doPublicStaplerGET(StaplerRequest req, StaplerResponse rsp) {
        }

    }
}
