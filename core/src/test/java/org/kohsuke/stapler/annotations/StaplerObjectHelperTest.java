package org.kohsuke.stapler.annotations;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class StaplerObjectHelperTest {

    @Test
    public void given__class_without_annotation__when__checking__then__notObject() {
        assertThat(StaplerObject.Helper.isObject(AbstractBaseClass.class), is(false));
    }

    @Test
    public void given__class_without_annotation__when__checking__then__notHasObject() {
        assertThat(StaplerObject.Helper.hasObject(AbstractBaseClass.class), is(false));
    }

    @Test
    public void given__class_with_annotation__when__checking__then__isObject() {
        assertThat(StaplerObject.Helper.isObject(AbstractSubclassWithAnnotation.class), is(true));
    }

    @Test
    public void given__class_with_annotation__when__checking__then__hasObject() {
        assertThat(StaplerObject.Helper.hasObject(AbstractSubclassWithAnnotation.class), is(true));
    }

    @Test
    public void given__class_extends_annotation__when__checking__then__notObject() {
        assertThat(StaplerObject.Helper.isObject(AbstractGrandclassWithoutAnnotation.class), is(false));
    }

    @Test
    public void given__class_extends_annotation__when__checking__then__hasObject() {
        assertThat(StaplerObject.Helper.hasObject(AbstractGrandclassWithoutAnnotation.class), is(true));
    }

    @Test
    public void given__annotated_interface__when__checking__then__isObject() {
        assertThat(StaplerObject.Helper.isObject(AnnotatedChildInterface.class), is(true));
    }

    @Test
    public void given__annotated_interface__when__checking__then__hasObject() {
        assertThat(StaplerObject.Helper.hasObject(AnnotatedChildInterface.class), is(true));
    }

    @Test
    public void given__class_implements_annotated_interface__when__checking__then__notObject() {
        assertThat(StaplerObject.Helper.isObject(ChildInterfaceImpl.class), is(false));
    }

    @Test
    public void given__class_implements_annotated_interface__when__checking__then__hasObject() {
        assertThat(StaplerObject.Helper.hasObject(ChildInterfaceImpl.class), is(true));
    }

    @Test
    public void given__class_indirect_implements_annotated_interface__when__checking__then__notObject() {
        assertThat(StaplerObject.Helper.isObject(GrandInterfaceImpl.class), is(false));
    }

    @Test
    public void given__class_indirect_implements_annotated_interface__when__checking__then__hasObject() {
        System.out.println(Arrays.asList(GrandInterfaceImpl.class.getInterfaces()));
        assertThat(StaplerObject.Helper.hasObject(GrandInterfaceImpl.class), is(true));
    }

    public static List<Class<?>> declaringClasses(Method method) {
        List<Class<?>> result = new ArrayList<>();
        result.add(method.getDeclaringClass());
        for (Method m: StaplerObject.Helper.declaredSuperMethods(method)) {
            result.add(m.getDeclaringClass());
        }
        return result;
    }



    @Test
    public void given__classDefiningMethodFromSuperAndInterfaces__when__listing_supers__orderReflectsPriority1() throws Exception {
        assertThat(declaringClasses(A4.class.getMethod("m1")),
                is(Arrays.asList(A4.class, A3.class, A1.class, B2.class, B1.class)));
    }

    @Test
    public void given__classDefiningMethodFromSuperAndInterfaces__when__listing_supers__orderReflectsPriority2() throws Exception {
        assertThat(declaringClasses(A4.class.getMethod("m2")),
                is(Arrays.<Class<?>>asList(A3.class, A2.class, A1.class)));
    }

    @Test
    public void given__classDefiningMethodFromSuperAndInterfaces__when__listing_supers__orderReflectsPriority3() throws Exception {
        assertThat(declaringClasses(A4.class.getMethod("m3")),
                is(Arrays.<Class<?>>asList(A3.class, A1.class)));
    }

    @Test
    public void given__classDefiningMethodFromSuperAndInterfaces__when__listing_supers__orderReflectsPriority4() throws Exception {
        assertThat(declaringClasses(A4.class.getMethod("m4")),
                is(Arrays.<Class<?>>asList(A2.class, A1.class)));
    }

    @Test
    public void given__classDefiningMethodFromSuperAndInterfaces__when__listing_supers__orderReflectsPriority5() throws Exception {
        assertThat(declaringClasses(A4.class.getMethod("m5")),
                is(Arrays.<Class<?>>asList(A4.class, C1.class, B1.class)));
    }

    @Test
    public void given__classDefiningMethodFromSuperAndInterfaces__when__listing_supers__orderReflectsPriority6() throws Exception {
        assertThat(declaringClasses(A4.class.getMethod("m6")),
                is(Arrays.<Class<?>>asList(A4.class, B2.class, B1.class)));
    }

    public static abstract class AbstractBaseClass {}

    @StaplerObject
    public static abstract class AbstractSubclassWithAnnotation extends AbstractBaseClass {}

    public static abstract class AbstractGrandclassWithoutAnnotation extends AbstractSubclassWithAnnotation {}

    public interface BaseInterface {}

    public interface ChildInterface extends BaseInterface {}

    @StaplerObject
    public interface AnnotatedChildInterface extends BaseInterface {}

    public interface GrandInterface extends AnnotatedChildInterface {}

    public static class ChildInterfaceImpl extends AbstractBaseClass implements AnnotatedChildInterface {}
    public static class GrandInterfaceImpl extends AbstractBaseClass implements GrandInterface {}

    public static abstract class A1 {
        public abstract void m1();
        public abstract void m2();
        public abstract void m3();
        public abstract void m4();
        protected abstract void n1();
        protected abstract void n2();
        protected abstract void n3();
        protected abstract void n4();
        abstract void o1();
        abstract void o2();
        abstract void o3();
        abstract void o4();
        private void p1() {}
        private void p2() {}
        private void p3() {}
        private void p4() {}
    }

    public static abstract class A2 extends A1 {
        public abstract void m2();
        public void m4() {}

        public abstract void n3();

        protected abstract void n4();

        public abstract void o2();

        protected abstract void o3();

        abstract void o4();

        private void p1() {}
        private void p2() {}
    }

    public static class A3 extends A2 {

        @Override
        public void m1() {

        }

        @Override
        public void m3() {

        }

        @Override
        protected void n1() {

        }

        @Override
        protected void n2() {

        }

        @Override
        void o1() {

        }

        @Override
        public void m2() {

        }

        @Override
        public void n3() {

        }

        @Override
        protected void n4() {

        }

        @Override
        public void o2() {

        }

        @Override
        protected void o3() {

        }

        @Override
        void o4() {

        }

        private void p1() {
        }

        public void p2() {}
    }

    public interface B1 {
        void m1();
        void m5();
        void m6();
    }

    public interface B2 extends B1{
        void m1();
        void m6();
    }

    public interface C1 {
        void m5();
    }

    public static class A4 extends A3 implements B2, C1 {

        public void m1() {

        }

        @Override
        public void m5() {

        }

        @Override
        public void m6() {

        }
    }
}
