package com.altf4studios.corebringer.interpreter;

public class CodeSimulator {
    private final JShellExecutor executor;
    private final ClassManager classManager;
    private final Validator validator;
    private final CodeBuilder codeBuilder;

    public CodeSimulator() {
        this.executor = new JShellExecutor();
        this.classManager = ClassManager.getInstance();
        this.validator = new Validator();
        this.codeBuilder = new CodeBuilder();
    }

    /**
     * Simulates code execution: validates, then executes if valid.
     * Handles snippets, methods, and classes.
     * @param code User-submitted code
     * @return Output or error message
     */
    public String simulate(String code) {
        System.out.println("CodeSimulator.simulate() called with: " + code);
        if (!validator.isValid(code)) {
            return "Validation Error: " + validator.getLastError();
        }
        
        // Check for multiple classes or inheritance
        if (hasMultipleClasses(code)) {
            System.out.println("Detected multiple classes");
            if (hasInheritance(code)) {
                System.out.println("Detected inheritance");
            }
            // Try the separate compilation approach for multiple classes
            return compileMultipleClasses(code);
        }
        
        // Detect if code is a method (not inside a class)
        if (isMethodOnly(code)) {
            System.out.println("Detected method-only code");
            // Wrap in a class with a main method that calls the method
            String className = "UserMethod";
            String methodName = extractMethodName(code);
            StringBuilder mainContent = new StringBuilder();
            if (methodName != null) {
                // Try to call the method with default arguments if possible
                mainContent.append(methodName).append("();");
            }
            String wrapped = "public class " + className + " {\n" + code + "\npublic static void main(String[] args) {\n" + mainContent + "\n}\n}";
            return compileAndExecute(wrapped);
        } else if (isClassWithMain(code)) {
            System.out.println("Detected class with main method");
            // It's a class with a main method, treat as class
            return compileAndExecute(code);
        } else {
            System.out.println("Treating as snippet");
            // Treat as snippet
            return executor.submitCode(code);
        }
    }

    /**
     * Compiles and executes class-based code
     * @param code Complete class code
     * @return Compilation and execution result
     */
    public String compileAndExecute(String code) {
        // For multiple classes, use the JShellExecutor approach which handles snippets better
        if (hasMultipleClasses(code)) {
            System.out.println("Multiple classes detected, using JShellExecutor approach");
            return compileWithJShellExecutor(code);
        }
        
        // For single classes, use the ClassManager approach
        ClassManager.CompilationResult compilation = classManager.compileClasses(code);
        if (!compilation.isSuccess()) {
            return "Compilation Error: " + compilation.getOutput();
        }
        
        // Try to find and execute main method
        String mainClassName = extractMainClassName(code);
        if (mainClassName != null) {
            System.out.println("Found main class: " + mainClassName);
            ClassManager.ExecutionResult execution = classManager.executeMainMethod(mainClassName);
            return (execution.getOutput().isEmpty() ? "(No output)\n" : "") + execution.getOutput();
        }
        
        return "(No output)\n" + compilation.getOutput();
    }

    // --- Helper methods ---
    private boolean isMethodOnly(String code) {
        // Detects if code is a method definition (not inside a class)
        String methodPattern = "(public|private|protected)?\\s*static\\s+\\w+\\s+\\w+\\s*\\([^)]*\\)\\s*\\{";
        return code.trim().matches(methodPattern + ".*");
    }

    private boolean isClassWithMain(String code) {
        // Detects if code contains a class with a main method
        return code.contains("class") && code.contains("static void main");
    }
    
    private boolean hasMultipleClasses(String code) {
        // Count the number of class declarations
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\b(public\\s+)?class\\s+\\w+");
        java.util.regex.Matcher matcher = pattern.matcher(code);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count > 1;
    }
    
    private boolean hasInheritance(String code) {
        // Check for extends keyword
        return code.contains("extends");
    }

    private String extractMethodName(String code) {
        // Extracts the method name from a method definition
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(public|private|protected)?\\s*static\\s+\\w+\\s+(\\w+)\\s*\\(");
        java.util.regex.Matcher matcher = pattern.matcher(code);
        if (matcher.find()) {
            return matcher.group(2);
        }
        return null;
    }

    private String extractMainClassName(String code) {
        // Find all classes with main methods
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
            "public\\s+class\\s+(\\w+)\\s*\\{[^}]*public\\s+static\\s+void\\s+main"
        );
        java.util.regex.Matcher matcher = pattern.matcher(code);
        
        // Return the first class with main method
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        // If no public class with main, try any class with main
        pattern = java.util.regex.Pattern.compile(
            "class\\s+(\\w+)\\s*\\{[^}]*public\\s+static\\s+void\\s+main"
        );
        matcher = pattern.matcher(code);
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        return null;
    }

    /**
     * Executes a specific method from a class
     * @param className The name of the class
     * @param methodName The name of the method to execute
     * @param args Optional arguments for the method
     * @return Execution result
     */
    public String executeMethod(String className, String methodName, String... args) {
        StringBuilder methodCall = new StringBuilder();
        methodCall.append(className).append(".").append(methodName).append("(");
        
        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                if (i > 0) methodCall.append(", ");
                methodCall.append(args[i]);
            }
        }
        
        methodCall.append(");");
        
        return executor.submitCode(methodCall.toString());
    }
    
    /**
     * Compiles multiple classes by submitting them as separate snippets to JShell
     * This approach works better with JShell's snippet-based compilation
     */
    public String compileMultipleClasses(String code) {
        System.out.println("Attempting to compile multiple classes using JShell snippets");
        
        // Try to compile all classes together first (JShell should handle this)
        ClassManager.CompilationResult compilation = classManager.compileClasses(code);
        if (compilation.isSuccess()) {
            System.out.println("Multiple classes compiled successfully together");
            
            // Find and execute the main class
            String mainClassName = extractMainClassName(code);
            if (mainClassName != null) {
                System.out.println("Executing main class: " + mainClassName);
                ClassManager.ExecutionResult execution = classManager.executeMainMethod(mainClassName);
                return execution.getOutput();
            }
            
            return "All classes compiled successfully! No main method found.";
        }
        
        // If compilation failed, try using JShellExecutor directly for better snippet handling
        System.out.println("Joint compilation failed, trying JShellExecutor approach");
        return compileWithJShellExecutor(code);
    }
    
    /**
     * Uses JShellExecutor directly for better snippet handling
     */
    private String compileWithJShellExecutor(String code) {
        System.out.println("Using JShellExecutor for compilation");
        
        // Submit the entire code to JShellExecutor
        String result = executor.submitCode(code);
        
        if (result.contains("Error:") || result.contains("Exception:")) {
            return "Compilation Error: " + result;
        }
        
        // If compilation succeeded, try to execute main method
        String mainClassName = extractMainClassName(code);
        if (mainClassName != null) {
            System.out.println("Executing main class: " + mainClassName);
            String executionResult = executor.submitCode(mainClassName + ".main(new String[0]);");
            return result + "\n" + executionResult;
        }
        
        return result;
    }
    
    public void reset() {
        executor.resetSession();
        classManager.resetSession();
        codeBuilder.clear();
    }
}
