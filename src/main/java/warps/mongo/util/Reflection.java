package warps.mongo.util;

import warps.mongo.MongoWarps;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Clase para utilizar reflections con MethodHandles y comprobaciones añadidas.
 */
public class Reflection {
    public static MethodHandle safeSetter(String clazz, String properties, String field) {
        try {
            return safeSetter(Class.forName(clazz), properties, field);
        }
        catch (ClassNotFoundException e) {
            MongoWarps.get().error("Class not found: " + clazz, e);
            throw new RuntimeException(e);
        }
    }

    public static MethodHandle safeSetter(Class<?> clazz, String properties, String field) {
        try {
            final Field f = clazz.getDeclaredField(field);
            String fieldProperties = Modifier.toString(f.getModifiers()) + " " + f.getType().getSimpleName();
            if (!properties.equals(fieldProperties)) throw new NoSuchFieldException("Field properties invalid. Current: " + fieldProperties + ". Expected: " + properties);
            if (!f.isAccessible()) f.setAccessible(true);
            return MethodHandles.lookup().unreflectSetter(f);
        }
        catch (Exception e) {
            MongoWarps.get().error("Error safeSetter: " + clazz.getName() + " -> " + properties + " " + field, e);
            throw new RuntimeException(e);
        }
    }

    public static MethodHandle safeGetter(String clazz, String properties, String field) {
        try {
            return safeGetter(Class.forName(clazz), properties, field);
        }
        catch (ClassNotFoundException e) {
            MongoWarps.get().error("Class not found: " + clazz, e);
            throw new RuntimeException(e);
        }
    }

    public static MethodHandle safeGetter(Class<?> clazz, String properties, String field) {
        try {
            final Field f = clazz.getDeclaredField(field);
            String fieldProperties = Modifier.toString(f.getModifiers()) + " " + f.getType().getSimpleName();
            if (!properties.equals(fieldProperties)) throw new NoSuchFieldException("Field properties invalid. Current: " + fieldProperties + ". Expected: " + properties);
            if (!f.isAccessible()) f.setAccessible(true);
            return MethodHandles.lookup().unreflectGetter(f);
        }
        catch (Exception e) {
            MongoWarps.get().error("Error safeGetter: " + clazz.getName() + " -> " + properties + " " + field, e);
            throw new RuntimeException(e);
        }
    }

    public static MethodHandle safeMethod(String clazz, String properties, String method, Class<?>... params) {
        try {
            return safeMethod(Class.forName(clazz), properties, method, params);
        }
        catch (ClassNotFoundException e) {
            MongoWarps.get().error("Class not found: " + clazz, e);
            throw new RuntimeException(e);
        }
    }

    public static MethodHandle safeMethod(Class<?> clazz, String properties, String method, Class<?>... params) {
        try {
            final Method m = clazz.getDeclaredMethod(method, params);
            String fieldProperties = Modifier.toString(m.getModifiers());
            if (!properties.equals(fieldProperties)) throw new NoSuchFieldException("Field properties invalid. Current: " + fieldProperties + ". Expected: " + properties);
            if (!m.isAccessible()) m.setAccessible(true);
            return MethodHandles.lookup().unreflect(m);
        }
        catch (Exception e) {
            MongoWarps.get().error("Error safeMethod: " + clazz.getName() + " -> " + properties + " " + method, e);
            throw new RuntimeException(e);
        }
    }
}
