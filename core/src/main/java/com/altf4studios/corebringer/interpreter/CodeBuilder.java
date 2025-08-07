package com.altf4studios.corebringer.interpreter;

import java.util.ArrayList;
import java.util.List;

public class CodeBuilder {
    private StringBuilder codeBuilder;
    private List<String> imports;
    private List<String> classes;
    private String mainClass;
    
    public CodeBuilder() {
        this.codeBuilder = new StringBuilder();
        this.imports = new ArrayList<>();
        this.classes = new ArrayList<>();
        this.mainClass = null;
    }
    
    /**
     * Adds a standard import statement
     */
    public CodeBuilder addImport(String importStatement) {
        if (!imports.contains(importStatement)) {
            imports.add(importStatement);
        }
        return this;
    }
    
    /**
     * Creates a simple class with the given name and content
     */
    public CodeBuilder addClass(String className, String classContent) {
        String classCode = String.format("public class %s {\n%s\n}", className, classContent);
        classes.add(classCode);
        return this;
    }
    
    /**
     * Creates a class with a main method
     */
    public CodeBuilder addMainClass(String className, String mainMethodContent) {
        String mainClassCode = String.format(
            "public class %s {\n" +
            "    public static void main(String[] args) {\n" +
            "        %s\n" +
            "    }\n" +
            "}", 
            className, mainMethodContent
        );
        this.mainClass = mainClassCode;
        return this;
    }
    
    /**
     * Creates a utility class with static methods
     */
    public CodeBuilder addUtilityClass(String className, String methods) {
        String utilityClassCode = String.format(
            "public class %s {\n" +
            "    %s\n" +
            "}", 
            className, methods
        );
        classes.add(utilityClassCode);
        return this;
    }
    
    /**
     * Builds the complete code with imports, classes, and main class
     */
    public String build() {
        StringBuilder result = new StringBuilder();
        
        // Add imports
        for (String importStatement : imports) {
            result.append(importStatement).append("\n");
        }
        result.append("\n");
        
        // Add regular classes
        for (String classCode : classes) {
            result.append(classCode).append("\n\n");
        }
        
        // Add main class if exists
        if (mainClass != null) {
            result.append(mainClass).append("\n");
        }
        
        return result.toString();
    }
    
    /**
     * Clears all content
     */
    public void clear() {
        codeBuilder.setLength(0);
        imports.clear();
        classes.clear();
        mainClass = null;
    }
    
    /**
     * Gets the current code as a string
     */
    public String getCurrentCode() {
        return build();
    }
}
