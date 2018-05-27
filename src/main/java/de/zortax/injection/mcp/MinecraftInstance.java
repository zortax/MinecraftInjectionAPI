package de.zortax.injection.mcp;// Created by leo on 27.05.18

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MinecraftInstance {

    private WrappedClass mcClass;
    private Object instance;

    public MinecraftInstance(WrappedClass mcClass, Object instance) {
        this.mcClass = mcClass;
        this.instance = instance;
    }

    public MinecraftInstance callGetter(String function, Class[] parameterTypes, Object... args) {
        Class clazz = instance.getClass();
        try {
            Method m = clazz.getDeclaredMethod(mcClass.getObfFunctionName(function), parameterTypes);
            Object o = m.invoke(instance, args);
            if (o == null)
                return null;
            return new MinecraftInstance(McpManager.getWrappedClassByObfName(o.getClass().getName()), o);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void callMethod(String function, Class[] parameterTypes, Object... args) {
        Class clazz = instance.getClass();
        try {
            Method m = clazz.getDeclaredMethod(mcClass.getObfFunctionName(function), parameterTypes);
            m.invoke(instance, args);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public MinecraftInstance callGetter(String function) {
        return callGetter(function, new Class[]{});
    }

    public void callMethod(String function) {
        callMethod(function, new Class[]{});
    }

    public MinecraftInstance getField(String name) {
        Class clazz = instance.getClass();
        try {
            Field f = clazz.getDeclaredField(mcClass.getObfFieldName(name));
            f.setAccessible(true);
            Object o = f.get(instance);
            return new MinecraftInstance(McpManager.getWrappedClassByObfName(o.getClass().getName()), o);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setField(String name, Object value) {
        Class clazz = instance.getClass();
        try {
            Field f = clazz.getDeclaredField(mcClass.getObfFieldName(name));
            f.setAccessible(true);
            f.set(instance, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public Object getObject() {
        return instance;
    }

    public WrappedClass getWrappedClass() {
        return mcClass;
    }

}
