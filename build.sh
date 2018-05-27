#!/bin/bash

# Build MinecraftInjectionAPI.jar

mvn clean compile assembly:single
mv target/InjectionApi-1.0-jar-with-dependencies.jar MinecraftInjectionAPI.jar
