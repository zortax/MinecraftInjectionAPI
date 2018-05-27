package de.zortax.injection.example.transformer;// Created by leo on 27.05.18

import de.zortax.injection.injector.Flags;
import de.zortax.injection.injector.FunctionHook;
import de.zortax.injection.injector.McAgent;
import de.zortax.injection.injector.RuntimeInjector;

public class MinecraftTransformer {

    public static void main(String[] args) {
        // Turn on console output
        Flags.verbose = true;
        // Concatenate the arguments to pass them to the agent
        StringBuilder sb = new StringBuilder();
        for (String arg : args)
            sb.append(arg).append(";");
        // male the agemt execute out onAgentLoaded() method after it os done loading
        sb.append("--load-hook-class;de.zortax.injection.example.transformer.MinecraftTransformer;--load-hook-method;onAgentLoaded");
        // attach the agent to the minecraft VM
        RuntimeInjector.attachToVm(sb.toString());

    }

    // this method will be called automatically as we told the McAgent to do so via arguments
    public static void onAgentLoaded() {
        // add this class as a transformer to register our function hooks
        McAgent.addTransformer(MinecraftTransformer.class);
        // call restransform() to modify the bytecode of the minecraft classes to activate our hooks
        McAgent.retransform();
    }

    @FunctionHook(targetClass = "net/minecraft/client/Minecraft", targetMethod = "runTick")
    public static void onTick() {
        // This method is getting called 20 times a second
        System.out.println("Ticked!");
    }

    @FunctionHook(targetClass = "net/minecraft/client/renderer/entity/RendererLivingEntity", targetMethod = "renderName")
    public static void onNameRender() {
        // Don't do this unless you want to shred your minecraft launcher's log output...
        System.out.println("Name rendered!");
    }

}
