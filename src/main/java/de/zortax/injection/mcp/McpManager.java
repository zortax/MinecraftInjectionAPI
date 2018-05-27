package de.zortax.injection.mcp;// Created by leo on 27.05.18

import de.zortax.injection.injector.Flags;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class McpManager {

    public static final String MCP_SRG_LINK_TEMPLATE = "http://mcpbot.bspk.rs/mcp/%version%/mcp-%version%-srg.zip";

    public static final String MCP_MAPPINGS_LINK_1_8_9 = "http://export.mcpbot.bspk.rs/mcp_stable/22-1.8.9/mcp_stable-22-1.8.9.zip";
    public static final String MCP_SRG_LINK_1_8_9 = createMcpSrgLink("1.8.9");

    private static String version = null;

    private static HashMap<String, String> names = null;

    private static HashMap<String, WrappedClass> classes = null;
    private static HashMap<String, String> fieldClasses = null;
    private static HashMap<String, String> functionClasses = null;
    private static HashMap<String, WrappedClass> obfClasses = null;

    public static void loadMappings(String version) {
        McpManager.version = version;

        names = new HashMap<>();
        classes = new HashMap<>();
        fieldClasses = new HashMap<>();
        functionClasses = new HashMap<>();
        obfClasses = new HashMap<>();

        Scanner joinedScanner = null;
        Scanner fieldsScanner = null;
        Scanner methodsScanner = null;
        try {
            joinedScanner = new Scanner(new File("mappings/" + version + "/srg/joined.srg"));
            fieldsScanner = new Scanner(new File("mappings/" + version + "/mcp/fields.csv"));
            methodsScanner = new Scanner(new File("mappings/" + version + "/mcp/methods.csv"));
        } catch (FileNotFoundException e) {
            if (Flags.verbose)
                e.printStackTrace();
            return;
        }

        if (Flags.verbose)
            System.out.println("\nLoading SRG mappings...");

        while (joinedScanner.hasNext()) {

            String current = joinedScanner.nextLine();

            if (current.startsWith("CL:")) {
                String[] split = current.split(" ");
                if (split[2].contains("$"))
                    continue;
                if (Flags.debug)
                    System.out.println("Loading SRG mapping for class " + split[1] + " ...");
                WrappedClass wc = new WrappedClass(split[1], split[2]);
                classes.put(split[2], wc);
                obfClasses.put(split[1], wc);
            } else if (current.startsWith("FD:")) {
                String[] split = current.split(" ");
                String className = split[2].substring(0, split[2].lastIndexOf("/"));
                if (Flags.debug)
                    System.out.println("Loading SRG mapping field " + split[2].split("/")[split[2].split("/").length - 1] + " in class " + className + " ...");
                WrappedClass wrappedClass = classes.get(className);
                if (wrappedClass != null) {
                    wrappedClass.putSrgFieldName(split[1], split[2]);
                    fieldClasses.put(split[2].split("/")[split[2].split("/").length - 1], className);
                }
            } else if (current.startsWith("MD: ")) {
                String[] split = current.split(" ");
                String className = split[3].substring(0, split[3].lastIndexOf("/"));
                if (Flags.debug)
                    System.out.println("Loading SRG mapping for method " + split[3].split("/")[split[3].split("/").length - 1] + "in class " + className + " ...");
                WrappedClass wrappedClass = classes.get(className);
                if (wrappedClass != null) {
                    wrappedClass.putSrgFunctionName(split[1], split[3]);
                    functionClasses.put(split[3].split("/")[split[3].split("/").length - 1], className);
                }
            }


        }

        if (Flags.verbose)
            System.out.println("Loading MCP mappings...");

        while (fieldsScanner.hasNext()) {
            String current = fieldsScanner.nextLine();
            String[] split = current.split(",");
            String className = fieldClasses.getOrDefault(split[0], null);
            if (className == null || className.contains("$"))
                continue;
            if (Flags.debug)
                System.out.println("Loading MCP mappings for field " + split[1] + " in class " + className + " ...");
            WrappedClass wrappedClass = classes.get(className);
            if (wrappedClass != null) {
                wrappedClass.putFieldName(split[0], split[1]);
            }
        }

        while (methodsScanner.hasNext()) {
            String current = methodsScanner.nextLine();
            String[] split = current.split(",");
            String className = functionClasses.getOrDefault(split[0], null);
            if (className == null || className.contains("$"))
                continue;
            if (Flags.debug)
                System.out.println("Loading MCP mappings for method " + split[1] + " in class " + className + " ...");
            WrappedClass wrappedClass = classes.get(className);
            if (wrappedClass != null) {
                wrappedClass.putFunctionName(split[0], split[1]);
            }
        }

        if (Flags.verbose)
            System.out.println("Done! All mappings loaded...");


    }

    public static WrappedClass getWrappedClassByObfName(String obfName) {
        return obfClasses.getOrDefault(obfName, null);
    }

    public static WrappedClass getWrappedClass(String name) {
        return classes.getOrDefault(name, null);
    }

    public static String getName(String mcpName) {
        return names.getOrDefault(mcpName, null);
    }

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

                if (Flags.debug)
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
