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
     * @param code User-submitted code
     * @return Output or error message
     */
    public String simulate(String code) {
        if (!validator.isValid(code)) {
            return "Validation Error: " + validator.getLastError();
        }
        return executor.submitCode(code);
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
            return "Compilation: Success\n" + 
                   "Execution: " + (execution.isSuccess() ? "Success" : "Failed") + "\n" +
                   "Output: " + execution.getOutput();
        }
        
        return "Compilation: Success\n" + compilation.getOutput();
    }
    
    /**
     * Creates a simple class with the given content
     */
    public String createClass(String className, String classContent) {
        codeBuilder.clear();
        codeBuilder.addClass(className, classContent);
        return compileAndExecute(codeBuilder.build());
    }
    
    /**
     * Creates a class with a main method
     */
    public String createMainClass(String className, String mainMethodContent) {
        codeBuilder.clear();
        codeBuilder.addMainClass(className, mainMethodContent);
        return compileAndExecute(codeBuilder.build());
    }
    
    /**
     * Creates a utility class with static methods
     */
    public String createUtilityClass(String className, String methods) {
        codeBuilder.clear();
        codeBuilder.addUtilityClass(className, methods);
        return compileAndExecute(codeBuilder.build());
    }
    
    /**
     * Extracts the main class name from code
     */
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
