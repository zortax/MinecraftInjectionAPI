package de.zortax.injection.injector;// Created by leo on 27.05.18

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class McClassTransformer implements ClassFileTransformer {

    private String targetClass;
    private String targetMethod;
    private String hookClass;
    private String hookFunction;
    private boolean before;

    McClassTransformer(String targetClass, String targetMethod, String hookClass, String hookFunction, boolean before) {
        this.targetClass = targetClass;
        this.targetMethod = targetMethod;
        this.hookClass = hookClass;
        this.hookFunction = hookFunction;
        this.before = before;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (!classBeingRedefined.getName().equals(targetClass))
            return classfileBuffer;

        try {

            ClassPool classPool = ClassPool.getDefault();

            classPool.appendClassPath(new LoaderClassPath(loader));
            classPool.appendClassPath(new LoaderClassPath(getClass().getClassLoader()));

            classPool.importPackage(hookClass.substring(0, hookClass.lastIndexOf(".")));

            CtClass ctClass = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));
            CtMethod ctMethod = ctClass.getDeclaredMethod(targetMethod);

            StringBuilder code = new StringBuilder();
            code.append("{ ").append(hookClass, hookClass.lastIndexOf(".") + 1, hookClass.length()).append(".").append(hookFunction).append("(); }");
            if (before)
                ctMethod.insertBefore(code.toString());
            else
                ctMethod.insertAfter(code.toString());

            byte[] byteCode = ctClass.toBytecode();
            ctClass.detach();

            return byteCode;

        } catch (Exception e) {
            if (Flags.verbose)
                e.printStackTrace();
        }
        return classfileBuffer;
    }
}
