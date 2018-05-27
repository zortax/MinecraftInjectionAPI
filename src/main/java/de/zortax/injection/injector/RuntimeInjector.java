package de.zortax.injection.injector;// Created by leo on 27.05.18

import com.sun.tools.attach.*;

import java.io.File;
import java.io.IOException;

public class RuntimeInjector {

    public static void main(String[] args) {
        Flags.parseArgs(args);
        StringBuilder agentArgs = new StringBuilder();
        for (String c : args)
            agentArgs.append(c).append(";");
        RuntimeInjector.attachToVm(agentArgs.toString());
    }

    public static void attachToVm(String args) {

        if (Flags.verbose)
            System.out.println("\nTrying to attach to Minecraft VM (" + Flags.vm + ")...");

        for (VirtualMachineDescriptor vmDesc : VirtualMachine.list()) {
            if (vmDesc.displayName().startsWith(Flags.vm))  {
                try {
                    VirtualMachine vm = VirtualMachine.attach(vmDesc);
                    if (Flags.verbose)
                        System.out.println("Trying to attach agent to VM...");
                    vm.loadAgent((new File(Flags.agentJar)).getAbsolutePath(), args);
                } catch (AttachNotSupportedException | AgentLoadException | AgentInitializationException | IOException e) {
                    if (Flags.verbose) {
                        System.out.println("Failed to attach to Minecraft VM (tried " + Flags.vm + ")...");
                        e.printStackTrace();
                    }
                }
                return;
            }
        }

        if (Flags.verbose)
            System.out.println("Couldn't find Minecraft VM (searched for " + Flags.vm + ")...");

    }

}
