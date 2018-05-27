package de.zortax.injection.injector;// Created by leo on 27.05.18

import com.sun.tools.attach.*;
import de.zortax.injection.api.Flags;
import de.zortax.injection.mcp.McpManager;

import java.io.IOException;

public class RuntimeInjector {

    public static void main(String[] args) {
        Flags.parseArgs(args);
        McpManager.downloadMappings(Flags.version, McpManager.MCP_MAPPINGS_LINK_1_8_9, McpManager.MCP_SRG_LINK_1_8_9);
        McpManager.loadMappings(Flags.version);
        RuntimeInjector.attachToVm();
    }

    public static void attachToVm() {

        if (Flags.verbose)
            System.out.println("\nTrying to attach to Minecraft VM (" + Flags.vm + ")...");

        for (VirtualMachineDescriptor vmDesc : VirtualMachine.list()) {
            System.out.println(vmDesc.displayName());
            if (vmDesc.displayName().startsWith(Flags.vm))  {
                try {
                    VirtualMachine vm = VirtualMachine.attach(vmDesc);
                    if (Flags.verbose)
                        System.out.println("Loading agent...");
                    vm.loadAgent(Flags.agentJar);
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
