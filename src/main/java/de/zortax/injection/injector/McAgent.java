package de.zortax.injection.injector;// Created by leo on 27.05.18

import de.zortax.injection.mcp.McpManager;
import de.zortax.injection.mcp.MinecraftInstance;
import de.zortax.injection.mcp.WrappedClass;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;

public class McAgent {

    private static Instrumentation instrumentation;
    private static ArrayList<Method> hooks;

    public static void agentmain(final String args, final Instrumentation instrumentation) {

        McAgent.instrumentation = instrumentation;

        Flags.parseArgs(args.split(";"));
        if (Flags.verbose)
            System.out.println("Loading VM agent...");
        McpManager.downloadMappings(Flags.version, McpManager.MCP_MAPPINGS_LINK_1_8_9, McpManager.MCP_SRG_LINK_1_8_9);
        McpManager.loadMappings(Flags.version);

        hooks = new ArrayList<>();

        addToMinecraftClassLoader(
                Flags.class,
                FunctionHook.class,
                InjectPosition.class,
                McClassTransformer.class,
                RuntimeInjector.class,
                McpManager.class,
                MinecraftInstance.class,
                WrappedClass.class
        );

        if (Flags.loadHookClass != null && Flags.loadHookMethod != null) {
            try {
                Class<?> loadHook = Class.forName(Flags.loadHookClass);
                Method m = loadHook.getDeclaredMethod(Flags.loadHookMethod);
                m.setAccessible(true);
                m.invoke(null);
            } catch (Exception e) {
                if (Flags.verbose) {
                    System.out.println("Couldn't load hook from " + Flags.loadHookClass + "!");
                    e.printStackTrace();
                }
            }
        }

    }

    public static void addTransformer(Class clazz) {
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.isAnnotationPresent(FunctionHook.class)) {
                if (Flags.verbose)
                    System.out.println("Adding function hook " + m.getName() + " in " + m.getDeclaringClass().getName() + " ...");
                hooks.add(m);
            }

        }
        addToMinecraftClassLoader(clazz);
    }

    public static void addToMinecraftClassLoader(Class... classes) {

        for (Class c : instrumentation.getAllLoadedClasses()) {
            if (c.getClassLoader() != null
                    && c.getClassLoader().getClass().getName().equals("net.minecraft.launchwrapper.LaunchClassLoader")) {
                Object cl = c.getClassLoader();
                try {
                    Method addUrl = cl.getClass().getDeclaredMethod("addURL", URL.class);
                    addUrl.setAccessible(true);
                    for (Class clazz : classes )
                        addUrl.invoke(cl, clazz.getProtectionDomain().getCodeSource().getLocation());
                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                    if (Flags.verbose)
                        e.printStackTrace();
                }
                break;
            }
        }

    }

    public static void retransform() {

        for (Class clazz : instrumentation.getAllLoadedClasses()) {

            for (Method hook : hooks) {
                FunctionHook info = hook.getAnnotation(FunctionHook.class);
                WrappedClass classToModify = McpManager.getWrappedClass(info.targetClass());
                String methodToModify = classToModify.getObfFunctionName(info.targetMethod());

                if (clazz.getName().equals(classToModify.getObfName())) {
                    if (Flags.verbose)
                        System.out.println("Transforming " + info.targetClass() + " ...");

                    try {

                        McClassTransformer ct = new McClassTransformer(classToModify.getObfName(), methodToModify, hook.getDeclaringClass().getName(), hook.getName(), info.position().equals(InjectPosition.BEFORE));
                        instrumentation.addTransformer(ct, true);
                        instrumentation.retransformClasses(clazz);
                        instrumentation.removeTransformer(ct);

                    } catch (Exception e) {
                        if (Flags.verbose)
                            e.printStackTrace();
                    }

                }


            }

        }

        hooks.clear();

    }

}
