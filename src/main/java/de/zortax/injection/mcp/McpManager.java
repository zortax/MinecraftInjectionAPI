package de.zortax.injection.mcp;// Created by leo on 27.05.18

import de.zortax.injection.api.Flags;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class McpManager {

    public static final String MCP_SRG_LINK_TEMPLATE = "http://mcpbot.bspk.rs/mcp/%version%/mcp-%version%-srg.zip";

    public static final String MCP_MAPPINGS_LINK_1_8_9 = "http://export.mcpbot.bspk.rs/mcp_stable/22-1.8.9/mcp_stable-22-1.8.9.zip";
    public static final String MCP_SRG_LINK_1_8_9 = createMcpSrgLink("1.8.9");

    public static String createMcpSrgLink(String version) {
        return MCP_SRG_LINK_TEMPLATE.replaceAll("%version%", version);
    }

    public static void downloadMappings(String version, String mcpMappingLink, String mcpSrgLink) {

        File f = new File("mappings/" + version + "/");
        if (f.exists() && f.isDirectory()) {
            if (Flags.verbose)
                System.out.println("Mappings already downloaded... [SKIPPED]");
            return;
        }

        try {
            if (Flags.verbose)
                System.out.println("Downloading MCP mappings from " + mcpMappingLink + " ...");
            URL mcpUrl = new URL(mcpMappingLink);
            ReadableByteChannel rbc = Channels.newChannel(mcpUrl.openStream());
            FileOutputStream fos = new FileOutputStream("mcp-" + version + ".zip");
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

            if (Flags.verbose)
                System.out.println("Downloading SRG mappings from " + mcpSrgLink + " ...");
            URL srgUrl = new URL(mcpSrgLink);
            rbc = Channels.newChannel(srgUrl.openStream());
            fos = new FileOutputStream("srg-" + version + ".zip");
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

            File mappingsDir = new File("mappings/" + version + "/");
            if (!mappingsDir.exists() || !mappingsDir.isDirectory()) {
                if (!mappingsDir.mkdirs()) {
                    if (Flags.verbose)
                        System.out.println("Couldn't create mappings directory...");
                    return;
                }
            }

            unzipFile("mcp-" + version + ".zip", "mappings/" + version + "/mcp/");
            unzipFile("srg-" + version + ".zip", "mappings/" + version + "/srg/");

            if (Flags.verbose)
                System.out.println("Deleting downloaded ZIP files...");

            File mcpZip = new File("mcp-" + version + ".zip");
            File srgZip = new File("srg-" + version + ".zip");
            mcpZip.delete();
            srgZip.delete();

        } catch (IOException e) {
            if (Flags.verbose)
                e.printStackTrace();
        }

        if (Flags.verbose)
            System.out.println("Done!");
    }

    private static void unzipFile(String zipFile, String outputFolder){

        if (Flags.verbose)
            System.out.println("Unzipping " + zipFile + " ...");

        byte[] buffer = new byte[1024];

        try{

            File folder = new File(outputFolder);
            if(!folder.exists()){
                if (!folder.mkdir()) {
                    if (Flags.verbose)
                        System.out.println("Couldn't create mappings directory...");
                    return;
                }
            }

            ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry ze = zis.getNextEntry();

            while(ze!=null){

                if (ze.isDirectory()) {
                    ze = zis.getNextEntry();
                    continue;
                }

                String fileName = ze.getName();
                File newFile = new File(outputFolder + File.separator + fileName);

                if (Flags.verbose)
                    System.out.println("Extracting "+ newFile.getPath() + " ...");
                new File(newFile.getParent()).mkdirs();
                FileOutputStream fos = new FileOutputStream(newFile);

                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }

                fos.close();
                ze = zis.getNextEntry();
            }

            zis.closeEntry();
            zis.close();

            if (Flags.verbose)
                System.out.println("Done unzipping " + zipFile);

        }catch(IOException ex){
            if (Flags.verbose)
                ex.printStackTrace();
        }
    }


}
