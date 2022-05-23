package com.github.mickeer.codegen.util;

import java.lang.reflect.Field;

/**
 * Reflection utilities for this library's usage.
 * <p>
 * WARNING: these are not usable in general. E.g. these methods do not handle fields in parent classes.
 */
@SuppressWarnings("unused") // Methods are used from generated classes.
public class FieldGenReflectionUtil {

    public static void setFieldValue(Object object, String fieldName, Object value) {
        Field field = getDeclaredField(object, fieldName);
        doPreservingAccessible(field, () -> {
            field.set(fieldName, value);
            return null;
        });
    }

    public static Object getFieldValue(Object object, String fieldName) {
        Field field = getDeclaredField(object, fieldName);
        return doPreservingAccessible(field, () -> field.get(fieldName));
    }

    public static <T> T doPreservingAccessible(Field field, ThrowingSupplier<T> runnable) {
        boolean accessible = field.isAccessible();

        try {
            field.setAccessible(true);
            return runnable.get();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } finally {
            field.setAccessible(accessible);
        }
    }

    public static Field getDeclaredField(Object obj, String fieldName) {
        try {
            return obj.getClass().getDeclaredField(fieldName);
        } catch (final NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @FunctionalInterface
    private interface ThrowingSupplier<T> {
        T get() throws IllegalAccessException;
    }
}
