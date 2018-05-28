package de.zortax.injection.mcp.gen;// Created by leo on 28.05.18

import de.zortax.injection.mcp.McpManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class WrapperClassBuilder {

    private String parentPackage;
    private String className;
    private GenType genType;
    private ArrayList<String> imports;
    private ArrayList<FieldDescription> fields;
    private ArrayList<FunctionDescription> functions;

    private WrapperClassBuilder() {}

    public static WrapperClassBuilder createWrappedClass(String parentPackage, String className, GenType type) {
        WrapperClassBuilder wcb = new WrapperClassBuilder();
        wcb.parentPackage = parentPackage;
        wcb.className = className;
        wcb.genType = type;
        wcb.imports = new ArrayList<>();
        wcb.fields = new ArrayList<>();
        wcb.functions = new ArrayList<>();
        return wcb;
    }

    public WrapperClassBuilder importClass(String importClass) {
        this.imports.add(importClass);
        return this;
    }

    public WrapperClassBuilder addField(String fieldName, String type, boolean isStatic, int arrayDimensions) {
        this.fields.add(new FieldDescription(fieldName, type, isStatic, arrayDimensions));
        return this;
    }

    public WrapperClassBuilder addMethod(String name, String descriptor, boolean isStatic) {
        this.functions.add(new FunctionDescription(name, descriptor, isStatic));
        return this;
    }

    public String build() {


        StringBuilder builder = new StringBuilder();

        builder.append("// GENERATED AUTOMATICALLY FROM MCP MAPPINGS\n");
        builder.append("package ").append(parentPackage).append(".").append(className.replaceAll("/", "."), 0, className.lastIndexOf("/")).append(";\n\n");

        ArrayList<String> actualImports = new ArrayList<>();
        actualImports.add("de.zortax.injection.mcp.McpManager");
        actualImports.add("de.zortax.injection.mcp.MinecraftInstance");
        for (String s : imports) {
            s = s.replace("/", ".");
            if (s.contains(".") && !actualImports.contains(s))
                actualImports.add(s);
        }

        for (FieldDescription f : fields) {
            String type = f.getType().replaceAll("/", ".");
            if (type.contains(".") && !actualImports.contains(type))
                actualImports.add(type);
        }

        for (FunctionDescription f : functions) {

            for (String s : f.getParameterTypes()) {
                s = s.replaceAll("/", ".");
                if (s.contains(".") && !actualImports.contains(s))
                    actualImports.add(s);
            }

            String retType = f.getReturnType().replaceAll("/", ".");
            if (retType.contains(".") && !actualImports.contains(retType))
                actualImports.add(retType);

        }

        for (String i : actualImports) {
            builder.append("import ");
            if (i.startsWith("net.minecraft"))
                i = parentPackage + "." + i;
            builder.append(i).append(";\n");
        }

        builder.append("\n\n");

        // class header
        builder.append("public ").append(genType.getText()).append(" ").append(className, className.lastIndexOf("/") + 1, className.length()).append(" {\n");

        // insert fields
        builder.append("\n");
        builder.append("    public MinecraftInstance mcInstance;\n\n");

        for (FieldDescription f : fields) {


            builder.append("    public ");

            if (f.isStatic) {
                if (f.getType().startsWith("net/minecraft")) {
                    builder.append("static ")
                            .append(f.getText())
                            .append(" ")
                            .append(f.getName())
                            .append(" = new ")
                            .append(f.getText())
                            .append("(McpManager.getWrappedClass(\"")
                            .append(f.getType())
                            .append("\").getStaticField(\"")
                            .append(f.getName())
                            .append("\"));\n");
                } else {
                    builder.append("static ")
                            .append(f.getText())
                            .append(" ")
                            .append(f.getName())
                            .append(" = (")
                            .append(f.getText())
                            .append(") McpManager.getWrappedClass(\"")
                            .append(className)
                            .append("\").getStaticField(\"")
                            .append(f.getName())
                            .append("\").getObject();\n");
                }
            } else
                builder.append(f.getText()).append(" ").append(f.getName()).append(";\n");


        }

        // add default constructor

        builder.append("\n");

        builder.append("    public ").append(className, className.lastIndexOf("/") + 1, className.length()).append("(MinecraftInstance instance) {\n");
        builder.append("        this.mcInstance = instance;\n");
        builder.append("        this.reloadFields(instance.getObject());\n");
        builder.append("    }\n\n");

        builder.append("    public void reloadFields(Object obj) {\n");

        for (FieldDescription f : fields) {
            if (!f.isStatic) {

                builder.append("        this.").append(f.getName()).append(" = ");

                if (f.getType().startsWith("net/minecraft")) {
                    builder.append("new ")
                            .append(f.getType(), f.getType().lastIndexOf("/") + 1, f.getType().length())
                            .append("(McpManager.getWrappedClass(\"")
                            .append(className)
                            .append("\").getField(\"")
                            .append(f.getName())
                            .append("\", obj));\n");
                } else {
                    builder.append("(")
                            .append(f.getType(), f.getType().lastIndexOf("/") + 1, f.getType().length())
                            .append(") McpManager.getWrappedClass(\"")
                            .append(className)
                            .append("\").getField(\"")
                            .append(f.getName())
                            .append("\", obj).getObject();\n");
                }


            }
        }

        builder.append("    }\n");

        for (FunctionDescription function : functions) {
            builder.append("\n");
            builder.append(function.build());
        }

        builder.append("\n}\n");

        return builder.toString();
    }

    private static String[] parseFieldDescriptor(Iterator<Character> it, int arrayDimensions) {
        String[] ret = new String[2];

        char current = it.next();

        if (current == '[')
            return null;
        StringBuilder parameterText = null;
        switch (current) {
            case 'B':
                ret[0] = "byte";
                parameterText = new StringBuilder("byte");
                for (int i = 0; i < arrayDimensions; i++)
                    parameterText.append("[]");
                ret[1] = parameterText.toString();
                break;
            case 'C':
                ret[0] = "char";
                parameterText = new StringBuilder("char");
                for (int i = 0; i < arrayDimensions; i++)
                    parameterText.append("[]");
                ret[1] = parameterText.toString();
                break;
            case 'D':
                ret[0] = "double";
                parameterText = new StringBuilder("double");
                for (int i = 0; i < arrayDimensions; i++)
                    parameterText.append("[]");
                ret[1] = parameterText.toString();
                break;
            case 'F':
                ret[0] = "float";
                parameterText = new StringBuilder("float");
                for (int i = 0; i < arrayDimensions; i++)
                    parameterText.append("[]");
                ret[1] = parameterText.toString();
                break;
            case 'I':
                ret[0] = "int";
                parameterText = new StringBuilder("int");
                for (int i = 0; i < arrayDimensions; i++)
                    parameterText.append("[]");
                ret[1] = parameterText.toString();
                break;
            case 'J':
                ret[0] = "long";
                parameterText = new StringBuilder("long");
                for (int i = 0; i < arrayDimensions; i++)
                    parameterText.append("[]");
                ret[1] = parameterText.toString();
                break;
            case 'S':
                ret[0] = "long";
                parameterText = new StringBuilder("long");
                for (int i = 0; i < arrayDimensions; i++)
                    parameterText.append("[]");
                ret[1] = parameterText.toString();
                break;
            case 'Z':
                ret[0] = "boolean";
                parameterText = new StringBuilder("boolean");
                for (int i = 0; i < arrayDimensions; i++)
                    parameterText.append("[]");
                ret[1] = parameterText.toString();
                break;
            case 'L':

                StringBuilder complexType = new StringBuilder();
                while (it.hasNext()) {
                    char c = it.next();
                    if (c == ';')
                        break;
                    complexType.append(c);
                }
                ret[0] = complexType.toString();
                //ret[0] = McpManager.getWrappedClass(ret[0]).getClassName().replaceAll("\\.", "/");
                String text = ret[0].substring(ret[0].lastIndexOf("/") + 1, ret[0].length());
                StringBuilder textBuilder = new StringBuilder(text);
                for (int i = 0; i < arrayDimensions; i++)
                    textBuilder.append("[]");
                ret[1] = textBuilder.toString();
                break;
            case 'V':
                ret[0] = "void";
                ret[1] = "void";
                break;
            default:
                throw new IllegalArgumentException("Unknown method descriptor element " + current + "!");

        }
        return ret;
    }

    private class FunctionDescription {

        String methodName;
        String methoDescriptor;
        boolean isStatic;

        String returnType;
        ArrayList<String> parameterTypes;

        String returnText;
        ArrayList<String> parameterTexts;

        FunctionDescription(String methodName, String methodDescriptor, boolean isStatic) {
            this.methodName = methodName;
            this.methoDescriptor = methodDescriptor;
            this.isStatic = isStatic;
            this.parameterTypes = new ArrayList<>();
            this.parameterTexts = new ArrayList<>();

            String[] split = methodDescriptor.replaceFirst("\\(", "").split("\\)");
            String parameterDescriptor = split[0];
            String returnDescriptor =  split[1];

            // parse parameter description
            ArrayList<Character> parameterChars = new ArrayList<>();
            for (char c : parameterDescriptor.toCharArray())
                parameterChars.add(c);
            int arrayDimensions = 0;
            for (Iterator<Character> it = parameterChars.iterator(); it.hasNext(); ) {
                String[] ret = parseFieldDescriptor(it, arrayDimensions);
                if (ret == null)
                    arrayDimensions++;
                else {
                    parameterTypes.add(ret[0]);
                    parameterTexts.add(ret[1]);
                    arrayDimensions = 0;
                }
            }


            ArrayList<Character> returnChars = new ArrayList<>();
            for (char c : returnDescriptor.toCharArray())
                returnChars.add(c);
            String[] ret = null;
            while (ret == null) {
                ret = parseFieldDescriptor(returnChars.iterator(), arrayDimensions);
                if (ret == null)
                    arrayDimensions++;
            }
            returnType = ret[0];
            returnText = ret[1];

        }

        String build() {
            StringBuilder builder = new StringBuilder();

            builder.append("    public ");
            if (isStatic)
                builder.append("static ");
            builder.append(returnText);
            builder.append(" ");
            builder.append(methodName);
            builder.append("(");

            for (int i = 0; i < parameterTypes.size(); i++) {
                builder.append(parameterTexts.get(i));
                builder.append(" arg");
                builder.append(i);
                if (i < parameterTypes.size() - 1)
                    builder.append(", ");
            }

            builder.append(") {\n");

            if (returnText.equals("void")) {
                if (isStatic) {
                    builder.append("        McpManager.getWrappedClass(\"");
                    builder.append(className);
                    builder.append("\").callStaticMethod(\"");
                } else {
                    builder.append("        mcInstance.callMethod(\"");
                }

                builder.append(methodName);
                builder.append("\", ");

                StringBuilder typeBuilder = new StringBuilder();
                typeBuilder.append("new Class[]{");
                for (int i = 0; i < parameterTypes.size(); i++) {
                    String t = parameterTypes.get(i);
                    if (t.startsWith("net/minecraft")) {
                        typeBuilder.append("McpManager.getWrappedClass(\"");
                        typeBuilder.append(t);
                        typeBuilder.append("\").getClassObject()");
                    } else {
                        typeBuilder.append(t, t.lastIndexOf("/") + 1, t.length());
                        typeBuilder.append(".class");
                    }
                    if (i < parameterTypes.size() - 1)
                        typeBuilder.append(", ");
                }
                typeBuilder.append("}");
                builder.append(typeBuilder.toString());

                for (int i = 0; i < parameterTypes.size(); i++) {
                    builder.append(", arg");
                    builder.append(i);
                    if (parameterTypes.get(i).startsWith("net/minecraft")) {
                        builder.append(".mcInstance.getObject()");
                    }
                }

                builder.append(");\n");
            } else {

                builder.append("        return ");

                if (returnType.startsWith("net/minecraft")) {

                    builder.append("new ");
                    builder.append(returnType, returnType.lastIndexOf("/") + 1, returnType.length());

                    if (isStatic) {
                        builder.append("(McpManager.getWrappedClass(\"");
                        builder.append(returnType);
                        builder.append("\").callStaticGetter(\"");
                    } else {
                        builder.append("(mcInstance.callGetter(\"");
                    }

                    builder.append(methodName);
                    builder.append("\", ");

                    StringBuilder typeBuilder = new StringBuilder();
                    typeBuilder.append("new Class[]{");
                    for (int i = 0; i < parameterTypes.size(); i++) {
                        String t = parameterTypes.get(i);
                        if (t.startsWith("net/minecraft")) {
                            typeBuilder.append("McpManager.getWrappedClass(\"");
                            typeBuilder.append(t);
                            typeBuilder.append("\").getClassObject()");
                        } else {
                            typeBuilder.append(t, t.lastIndexOf("/") + 1, t.length());
                            typeBuilder.append(".class");
                        }
                        if (i < parameterTypes.size() - 1)
                            typeBuilder.append(", ");
                    }
                    typeBuilder.append("}");
                    builder.append(typeBuilder.toString());

                    for (int i = 0; i < parameterTypes.size(); i++) {
                        builder.append(", arg");
                        builder.append(i);
                        if (parameterTypes.get(i).startsWith("net/minecraft")) {
                            builder.append(".mcInstance.getObject()");
                        }
                    }

                    builder.append(")");
                    builder.append(");\n");

                } else {
                    builder.append("(");
                    builder.append(returnText);
                    builder.append(") ");

                    if (isStatic) {
                        builder.append("McpManager.getWrappedClass(\"");
                        builder.append(returnType);
                        builder.append("\").callStaticGetter(\"");
                    } else {
                        builder.append("mcInstance.callGetter(\"");
                    }

                    builder.append(methodName);
                    builder.append("\", ");

                    StringBuilder typeBuilder = new StringBuilder();
                    typeBuilder.append("new Class[]{");
                    for (int i = 0; i < parameterTypes.size(); i++) {
                        String t = parameterTypes.get(i);
                        if (t.startsWith("net/minecraft")) {
                            typeBuilder.append("McpManager.getWrappedClass(\"");
                            typeBuilder.append(t);
                            typeBuilder.append("\").getClassObject()");
                        } else {
                            typeBuilder.append(t, t.lastIndexOf("/") + 1, t.length());
                            typeBuilder.append(".class");
                        }
                        if (i < parameterTypes.size() - 1)
                            typeBuilder.append(", ");
                    }
                    typeBuilder.append("}");
                    builder.append(typeBuilder.toString());

                    for (int i = 0; i < parameterTypes.size(); i++) {
                        builder.append(", arg");
                        builder.append(i);
                        if (parameterTypes.get(i).startsWith("net/minecraft")) {
                            builder.append(".mcInstance.getObject()");
                        }
                    }

                    builder.append(").getObject();\n");

                }

            }

            builder.append("    }\n");

            return builder.toString();
        }

        ArrayList<String> getParameterTypes() {
            return parameterTypes;
        }

        ArrayList<String> getParameterTexts() {
            return parameterTexts;
        }

        String getReturnType() {
            return returnType;
        }

        String getReturnText() {
            return returnText;
        }

    }

    private class FieldDescription {

        String name;
        String type;
        boolean isStatic;
        int arrayDimensions;

        FieldDescription(String name, String type, boolean isStatic, int arrayDimensions) {
            this.name = name;
            this.type = type;
            this.isStatic = isStatic;
            this.arrayDimensions = arrayDimensions;
        }

        String getType() {
            return type;
        }

        String getText() {
            StringBuilder text = new StringBuilder();
            text.append(type, type.lastIndexOf("/") + 1, type.length());
            for (int i = 0; i < arrayDimensions; i++)
                text.append("[]");
            return text.toString();
        }

        String getName() {
            return name;
        }

    }

}
