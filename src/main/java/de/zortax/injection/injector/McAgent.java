package de.zortax.injection.injector;// Created by leo on 27.05.18

import de.zortax.injection.api.Flags;
import de.zortax.injection.mcp.McpManager;

import java.lang.instrument.Instrumentation;

public class McAgent {

    public static void agentmain(final String args, final Instrumentation instrumentation) {
        Flags.parseArgs(args.split(";"));
        if (Flags.verbose)
            System.out.println("Loading VM agent...");
        McpManager.downloadMappings(Flags.version, McpManager.MCP_MAPPINGS_LINK_1_8_9, McpManager.MCP_SRG_LINK_1_8_9);
        McpManager.loadMappings(Flags.version);
    }

}
