package de.zortax.injectionapi.injector;// Created by leo on 27.05.18

import de.zortax.injectionapi.mcp.McpManager;

public class RuntimeInjector {

    public static void main(String[] args) {

        McpManager.downloadMappings("1.8.9", McpManager.MCP_MAPPINGS_LINK_1_8_9, McpManager.MCP_SRG_LINK_1_8_9);

    }

}
