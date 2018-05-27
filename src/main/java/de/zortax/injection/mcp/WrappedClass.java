package de.zortax.injection.mcp;// Created by leo on 27.05.18

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Set;

public class WrappedClass {

    private String obfName;
    private String className;
    private HashMap<String, String> srgFields;
    private HashMap<String, String> fields;
    private HashMap<String, String> srgFunctions;
    private HashMap<String, String> functions;

    private Class classObject;

    public WrappedClass(String obfName, String className) {
        this.obfName = obfName;
        this.className = className.replace("/", ".");
        this.srgFields = new HashMap<>();
        this.fields = new HashMap<>();
        this.srgFunctions = new HashMap<>();
        this.functions = new HashMap<>();

    }

    public void loadClassObject() {
        try {
            System.out.println("Trying to load class " + obfName + " (" + className + ")...");
            this.classObject = Class.forName(obfName.replace("/", "."));
        } catch (ClassNotFoundException e) {
            this.classObject = null;
            System.out.println("Could not load class " + obfName + " (" + className + ")...");
            //e.printStackTrace();
        }
    }

    public void putSrgFieldName(String obfName, String srgName) {
        srgFields.put(srgName.split("/")[srgName.split("/").length - 1], obfName.split("/")[1]);
    }

    public void putFieldName(String srgName, String name) {
        fields.put(name, srgName);
    }

    public void putSrgFunctionName(String obfName, String srgName) {
        srgFunctions.put(srgName.split("/")[srgName.split("/").length - 1], obfName.split("/")[1]);
    }

    public void putFunctionName(String srgName, String name) {
        functions.put(name, srgName);
    }

    public String getObfFunctionName(String name) {
        return srgFunctions.get(functions.get(name));
    }

    public String getObfFieldName(String name) {
        return srgFields.get(fields.get(name));
    }

    public String getObfName() {
        return obfName;
    }

    public String getClassName() {
        return className;
    }

    public Set<String> getFieldNames() {
        return fields.keySet();
    }

    public Set<String> getFunctionNames() {
        return functions.keySet();
    }

    public Class getClassObject() {
        if (classObject == null)
            loadClassObject();
        return classObject;
    }

    public MinecraftInstance callStaticGetter(String function, Class[] parameterTypes, Object... args) {
        try {
            Method m = classObject.getDeclaredMethod(this.getObfFunctionName(function), parameterTypes);
            Object o = m.invoke(null, args);
            if (o == null)
                return null;
            return new MinecraftInstance(McpManager.getWrappedClassByObfName(o.getClass().getName()), o);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void callStaticMethod(String function, Class[] parameterTypes, Object... args) {
        try {
            Method m = classObject.getDeclaredMethod(this.getObfFunctionName(function), parameterTypes);
            m.invoke(null, args);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public MinecraftInstance callStaticGetter(String function) {
        return callStaticGetter(function, new Class[]{});
    }

    public void callStaticMethod(String function) {
        callStaticMethod(function, new Class[]{});
    }

    public MinecraftInstance getStaticField(String name) {
        try {
            Field f = classObject.getDeclaredField(this.getObfFieldName(name));
            f.setAccessible(true);
            Object o = f.get(null);
            return new MinecraftInstance(McpManager.getWrappedClassByObfName(o.getClass().getName()), o);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setStaticField(String name, Object value) {
        try {
            Field f = classObject.getDeclaredField(this.getObfFieldName(name));
            f.setAccessible(true);
            f.set(null, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
