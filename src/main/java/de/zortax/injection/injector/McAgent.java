package de.zortax.injection.injector;// Created by leo on 27.05.18

import de.zortax.injection.mcp.McpManager;
import de.zortax.injection.mcp.WrappedClass;

import java.lang.annotation.Annotation;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
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

    }

    public static void addTransformer(Class clazz) {
        for (Method m : clazz.getDeclaredMethods()) {
            for (Annotation a : m.getAnnotations()) {
                if (a.getClass().equals(FunctionHook.class))
                    hooks.add(m);
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

    }

}
