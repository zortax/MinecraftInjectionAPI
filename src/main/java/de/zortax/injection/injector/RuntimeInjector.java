package de.zortax.injection.injector;// Created by leo on 27.05.18

import de.zortax.injection.api.Flags;
import de.zortax.injection.mcp.McpManager;

public class RuntimeInjector {

    public static void main(String[] args) {
        Flags.parseArgs(args);
        McpManager.downloadMappings(Flags.version, McpManager.MCP_MAPPINGS_LINK_1_8_9, McpManager.MCP_SRG_LINK_1_8_9);
        McpManager.loadMappings(Flags.version);
    }

}
