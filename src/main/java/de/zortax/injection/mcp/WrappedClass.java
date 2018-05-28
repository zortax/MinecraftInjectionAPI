package de.zortax.injection.mcp;// Created by leo on 27.05.18

import de.zortax.injection.injector.Flags;
import de.zortax.injection.mcp.gen.GenType;
import de.zortax.injection.mcp.gen.WrapperClassBuilder;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Set;

public class WrappedClass {

    private String obfName;
    private String className;
    private HashMap<String, String> srgFields;
    private HashMap<String, String> fields;
    private HashMap<String, String> srgFunctions;
    private HashMap<String, String> functions;
    private HashMap<String, String> srgToMethodDesc;
    private HashMap<String, String> nameToMethodDesc;

    private Class classObject;

    public WrappedClass(String obfName, String className) {
        this.obfName = obfName;
        this.className = className.replace("/", ".");
        this.srgFields = new HashMap<>();
        this.fields = new HashMap<>();
        this.srgFunctions = new HashMap<>();
        this.functions = new HashMap<>();
        this.srgToMethodDesc = new HashMap<>();
        this.nameToMethodDesc = new HashMap<>();
    }

    public void setClassObject(Class clazz) {
        this.classObject = clazz;
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

    public void putSrgFunctionName(String obfName, String srgName, String methodDescriptor) {
        String srg = srgName.split("/")[srgName.split("/").length - 1];
        srgToMethodDesc.put(srg, methodDescriptor);
        srgFunctions.put(srg, obfName.split("/")[1]);

    }

    public void putFunctionName(String srgName, String name) {
        functions.put(name, srgName);
        nameToMethodDesc.put(name, srgToMethodDesc.get(srgName));
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
        return getField(name, null);
    }

    public MinecraftInstance getField(String name, Object instance) {
        try {
            Field f = classObject.getDeclaredField(this.getObfFieldName(name));
            f.setAccessible(true);
            Object o = f.get(instance);
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

    public void writeWrapperClass() throws Exception {

        if (classObject == null)
            loadClassObject();

        System.out.println(className.replaceAll("\\.", "/"));
        String directory = Flags.generateWrapperClasses + className.substring(0, className.lastIndexOf(".")).replaceAll("\\.", "/");
        File dir = new File(directory);
        dir.mkdirs();
        File f = new File("/home/leo/Projects/IntelliJ/MinecraftInjectionAPI/src/main/java/de/zortax/injection/api/" + className.replaceAll("\\.", "/") + ".java");
        if (!f.exists()) {
            f.createNewFile();
        }

        WrapperClassBuilder wcb = WrapperClassBuilder.createWrappedClass(Flags.generatorParentPackage, className.replaceAll("\\.", "/"), GenType.CLASS);

        for (String field : getFieldNames()) {

            Field rf = classObject.getDeclaredField(getObfFieldName(field));
            WrappedClass wc = McpManager.getWrappedClassByObfName(rf.getType().getName());
            String name = wc == null ? rf.getType().getName().replaceAll("\\.", "/") : wc.getClassName().replaceAll("\\.", "/");
            while (name.startsWith("["))
                name = name.replaceFirst("\\[", "");
            wcb.addField(field, name, Modifier.isStatic(rf.getModifiers()), getArrayDimensions(rf.getType()));
        }

        for (String function : getFunctionNames()) {
            Method m = classObject.getDeclaredMethod(getObfFunctionName(function));
            wcb.addMethod(function, nameToMethodDesc.get(function), Modifier.isStatic(m.getModifiers()));
        }

        PrintWriter pw = new PrintWriter(f);
        pw.print(wcb.build());
        pw.flush();
        pw.close();


    }

    public static int getArrayDimensions(Class arrayClass) {
        int count = 0;
        while ( arrayClass.isArray() ) {
            count++;
            arrayClass = arrayClass.getComponentType();
        }
        return count;
    }

}
