package de.zortax.injection.api;// Created by leo on 27.05.18

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public final class Flags {

    public static boolean verbose = false;
    public static String version = "1.8.9";
    public static String agentJar = "MinecraftInjectionAPI.jar";
    public static String vm = "net.minecraft.launchwrapper.Launch";

    public static void parseArgs(String[] args) {

        List<String> argList = Arrays.asList(args);

        for (Iterator<String> it = argList.iterator(); it.hasNext(); ) {
            String c = it.next();

            if (c.equalsIgnoreCase("--verbose"))
                verbose = true;
            else if ((c.equalsIgnoreCase("--version") || c.equals("-V")) && it.hasNext())
                version = it.next();
            else if ((c.equalsIgnoreCase("--agent") || c.equals("-A")) && it.hasNext() )
                agentJar = it.next();
            else if (c.equalsIgnoreCase("--vm") && it.hasNext())
                vm = it.next();

        }

    }

}
