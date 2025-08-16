package com.altf4studios.corebringer.interpreter;

import jdk.jshell.JShell;
import jdk.jshell.Snippet;
import jdk.jshell.SnippetEvent;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ClassManager {
    private JShell shell;
    private Validator validator;
    private static ClassManager instance;
    private ByteArrayOutputStream capturedOutput;
    private PrintStream customOut;
    
    public ClassManager() {
        this.capturedOutput = new ByteArrayOutputStream();
        this.customOut = new PrintStream(capturedOutput);
        this.shell = JShell.builder()
            .out(customOut)
            .build();
        this.validator = new Validator();
    }
    
    public static ClassManager getInstance() {
        if (instance == null) {
            instance = new ClassManager();
        }
        return instance;
    }
    
    /**
     * Compiles and validates a complete class structure
     */
    public CompilationResult compileClasses(String code) {
        System.out.println("ClassManager.compileClasses() called with code length: " + code.length());
        
        if (!validator.isValid(code)) {
            System.out.println("VALIDATION FAILED: " + validator.getLastError());
            return new CompilationResult(false, "Validation Error: " + validator.getLastError(), null);
        }
        
        System.out.println("VALIDATION PASSED, proceeding with compilation");
        
        // Clear previous output
        capturedOutput.reset();
        
        try {
            System.out.println("Evaluating code with JShell...");
            List<SnippetEvent> events = shell.eval(code);
            System.out.println("JShell evaluation completed, processing " + events.size() + " events");
            
            StringBuilder output = new StringBuilder();
            boolean success = true;
            
            for (SnippetEvent event : events) {
                System.out.println("Processing event: " + event.status() + ", snippet: " + event.snippet().source().substring(0, Math.min(50, event.snippet().source().length())) + "...");
                
                if (event.exception() != null) {
                    String error = "Compilation Error: " + event.exception().getMessage();
                    output.append(error).append("\n");
                    System.out.println("COMPILATION ERROR: " + error);
                    success = false;
                } else if (event.status() == Snippet.Status.REJECTED) {
                    String error = "Rejected: " + event.snippet().source();
                    output.append(error).append("\n");
                    System.out.println("COMPILATION REJECTED: " + error);
                    success = false;
                } else if (event.value() != null) {
                    output.append(event.value()).append("\n");
                    System.out.println("COMPILATION SUCCESS: " + event.value());
                }
            }
            
            // Get captured output from JShell
            String capturedOut = capturedOutput.toString();
            if (!capturedOut.isEmpty()) {
                output.append(capturedOut);
                System.out.println("Captured output: " + capturedOut);
            }
            
            System.out.println("Compilation result - Success: " + success + ", Output length: " + output.length());
            return new CompilationResult(success, output.toString(), events);
        } catch (Exception e) {
            System.out.println("UNEXPECTED ERROR: " + e.getMessage());
            return new CompilationResult(false, "Unexpected error: " + e.getMessage(), null);
        }
    }
    
    /**
     * Executes a main method from a compiled class
     */
    public ExecutionResult executeMainMethod(String className) {
        System.out.println("ClassManager.executeMainMethod() called with className: " + className);
        
        // Clear previous output
        capturedOutput.reset();
        
        try {
            String executionCode = String.format("%s.main(new String[0]);", className);
            System.out.println("Executing: " + executionCode);
            List<SnippetEvent> events = shell.eval(executionCode);
            
            StringBuilder output = new StringBuilder();
            boolean success = true;
            
            for (SnippetEvent event : events) {
                if (event.exception() != null) {
                    output.append("Runtime Error: ").append(event.exception().getMessage()).append("\n");
                    success = false;
                } else if (event.value() != null) {
                    output.append(event.value()).append("\n");
                }
            }
            
            // Get captured output from JShell
            String capturedOut = capturedOutput.toString();
            System.out.println("Captured JShell output in executeMainMethod: '" + capturedOut + "'");
            if (!capturedOut.isEmpty()) {
                output.append(capturedOut);
            }
            
            System.out.println("Final executeMainMethod output: '" + output.toString() + "'");
            return new ExecutionResult(success, output.toString());
        } catch (Exception e) {
            return new ExecutionResult(false, "Execution error: " + e.getMessage());
        }
    }
    
    /**
     * Executes a method from a class with timeout
     */
    public ExecutionResult executeWithTimeout(String methodCall, long timeoutMinutes) {
        final FutureTask<ExecutionResult> task = new FutureTask<>(() -> {
            // Clear previous output
            capturedOutput.reset();
            
            try {
                List<SnippetEvent> events = shell.eval(methodCall);
                StringBuilder output = new StringBuilder();
                boolean success = true;
                
                for (SnippetEvent event : events) {
                    if (event.exception() != null) {
                        output.append("Runtime Error: ").append(event.exception().getMessage()).append("\n");
                        success = false;
                    } else if (event.value() != null) {
                        output.append(event.value()).append("\n");
                    }
                }
                
                // Get captured output from JShell
                String capturedOut = capturedOutput.toString();
                if (!capturedOut.isEmpty()) {
                    output.append(capturedOut);
                }
                
                return new ExecutionResult(success, output.toString());
            } catch (Exception e) {
                return new ExecutionResult(false, "Error: " + e.getMessage());
            }
        });
        
        Thread thread = new Thread(task);
        thread.start();
        
        try {
            return task.get(timeoutMinutes, TimeUnit.MINUTES);
        } catch (TimeoutException e) {
            thread.interrupt();
            return new ExecutionResult(false, "Timeout: Execution took too long");
        } catch (Exception e) {
            thread.interrupt();
            return new ExecutionResult(false, "Error: " + e.getMessage());
        }
    }
    
    /**
     * Resets the shell session
     */
    public void resetSession() {
        shell.close();
        capturedOutput.reset();
        shell = JShell.builder()
            .out(customOut)
            .build();
    }
    
    /**
     * Result class for compilation operations
     */
    public static class CompilationResult {
        private final boolean success;
        private final String output;
        private final List<SnippetEvent> events;
        
        public CompilationResult(boolean success, String output, List<SnippetEvent> events) {
            this.success = success;
            this.output = output;
            this.events = events;
        }
        
        public boolean isSuccess() { return success; }
        public String getOutput() { return output; }
        public List<SnippetEvent> getEvents() { return events; }
    }
    
    /**
     * Result class for execution operations
     */
    public static class ExecutionResult {
        private final boolean success;
        private final String output;
        
        public ExecutionResult(boolean success, String output) {
            this.success = success;
            this.output = output;
        }
        
        public boolean isSuccess() { return success; }
        public String getOutput() { return output; }
    }
} 