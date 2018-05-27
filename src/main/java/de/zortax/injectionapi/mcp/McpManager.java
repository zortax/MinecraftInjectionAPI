package de.zortax.injectionapi.mcp;// Created by leo on 27.05.18

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class McpManager {

    public static final String MCP_SRG_LINK_TEMPLATE = "http://mcpbot.bspk.rs/mcp/%version%/mcp-%version%-srg.zip";

    public static final String MCP_MAPPINGS_LINK_1_8_9 = "http://export.mcpbot.bspk.rs/mcp_stable/22-1.8.9/mcp_stable-22-1.8.9.zip";
    public static final String MCP_SRG_LINK_1_8_9 = createMcpSrgLink("1.8.9");

    public static String createMcpSrgLink(String version) {
        return MCP_SRG_LINK_TEMPLATE.replaceAll("%version%", version);
    }

    public static void downloadMappings(String version, String mcpMappingLink, String mcpSrgLink) {
        try {

            System.out.println("Downloading MCP mappings from " + mcpMappingLink + " ...");
            URL mcpUrl = new URL(mcpMappingLink);
            ReadableByteChannel rbc = Channels.newChannel(mcpUrl.openStream());
            FileOutputStream fos = new FileOutputStream("mcp-" + version + ".zip");
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

            System.out.println("Downloading SRG mappings from " + mcpSrgLink + " ...");
            URL srgUrl = new URL(mcpSrgLink);
            rbc = Channels.newChannel(srgUrl.openStream());
            fos = new FileOutputStream("srg-" + version + ".zip");
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
