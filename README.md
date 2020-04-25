# MinecraftInjectionAPI
![Build](https://travis-ci.org/Zortax/MinecraftInjectionAPI.svg?branch=master)
![Github All Releases](https://img.shields.io/github/downloads/Zortax/MinecraftInjectionAPI/total.svg)
![GitHub release](https://img.shields.io/github/release/Zortax/MinecraftInjectionAPI.svg)
![Twitter Follow](https://img.shields.io/twitter/follow/leoseib.svg?style=social&label=Follow)

A simple API using MCP deobfuscation mappings helping you to inject mods at runtime.

With this library you can inject code into Minecraft at runtime! You can write a program, that does so after Minecraft (Vanilla) was already started, but you could also use it to hook into minecraft methods from any other context (e.g. Labymod / 5zigMod addons, Forge, Liteloader). To deal with an obfuscated minecraft environment the library includes a reflection wrapper that automatically downloads MCP deobfuscation mapping and gives you reflective access to minecraft classes by using their corresponding MCP names.

Maven:
```xml
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>
<dependency>
  <groupId>com.github.Zortax</groupId>
  <artifactId>MinecraftInjectionAPI</artifactId>
  <version>v1.0</version>
</dependency>
```
Gradle:
```gradle
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}
dependencies {
    implementation 'com.github.Zortax:MinecraftInjectionAPI:v1.0'
}
```

Here is an example class that should explain what the MinecraftInjectionAPI is doing:

```java
package de.zortax.injection.example.transformer;

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
    
    // this DOES work in an obfuscated environment
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
```
This class could be compiled into a fat-jar (that includes all dependencies).
The already mentioned reflection wrapper is easy to use as well. This is how you can check if minecraft is running on a 64bit JVM by using a method in the `Minecraft` class:
```java
boolean is64bit = (boolean) McpManager.getWrappedClass("net/minecraft/client/Minecraft")
                                      .callStaticGetter("getMinecraft")
                                      .callGetter("isJava64bit").getObject();
```
If you are working with forge or an deobfuscated environment, there is no need to use this wrapper. Instead you will need the classes you want to use directly as build dependency (you should not put them into jar as they are already available in the minecraft JVM).
