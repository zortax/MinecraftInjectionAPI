package de.zortax.injection.mcp.gen;// Created by leo on 28.05.18

public enum GenType {
    CLASS("class"),
    ENUM("enum"),
    INTERFACE("interface"),
    AT_INTERFACE("@interface");

    private String text;

    GenType(String text) {
        this.text = text;
    }

    public String getText() {
        return this.text;
    }

}
