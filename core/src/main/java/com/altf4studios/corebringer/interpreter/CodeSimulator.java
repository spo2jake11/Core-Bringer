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
        if (!validator.isValid(code)) {
            return "Validation Error: " + validator.getLastError();
        }
        // Detect if code is a method (not inside a class)
        if (isMethodOnly(code)) {
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
            // It's a class with a main method, treat as class
            return compileAndExecute(code);
        } else {
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
        ClassManager.CompilationResult compilation = classManager.compileClasses(code);
        if (!compilation.isSuccess()) {
            return "Compilation Error: " + compilation.getOutput();
        }
        // Try to find and execute main method
        String mainClassName = extractMainClassName(code);
        if (mainClassName != null) {
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
        // Simple regex to find class with main method
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
            "public\\s+class\\s+(\\w+)\\s*\\{[^}]*public\\s+static\\s+void\\s+main"
        );
        java.util.regex.Matcher matcher = pattern.matcher(code);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    public void reset() {
        executor.resetSession();
        classManager.resetSession();
        codeBuilder.clear();
    }
}
