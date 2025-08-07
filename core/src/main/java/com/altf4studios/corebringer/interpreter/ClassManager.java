package com.altf4studios.corebringer.interpreter;

import jdk.jshell.JShell;
import jdk.jshell.Snippet;
import jdk.jshell.SnippetEvent;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ClassManager {
    private JShell shell;
    private Validator validator;
    private static ClassManager instance;
    
    public ClassManager() {
        this.shell = JShell.create();
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
        if (!validator.isValid(code)) {
            return new CompilationResult(false, "Validation Error: " + validator.getLastError(), null);
        }
        
        try {
            List<SnippetEvent> events = shell.eval(code);
            StringBuilder output = new StringBuilder();
            boolean success = true;
            
            for (SnippetEvent event : events) {
                if (event.exception() != null) {
                    output.append("Compilation Error: ").append(event.exception().getMessage()).append("\n");
                    success = false;
                } else if (event.status() == Snippet.Status.REJECTED) {
                    output.append("Rejected: ").append(event.snippet().source()).append("\n");
                    success = false;
                } else if (event.value() != null) {
                    output.append(event.value()).append("\n");
                }
            }
            
            return new CompilationResult(success, output.toString(), events);
        } catch (Exception e) {
            return new CompilationResult(false, "Unexpected error: " + e.getMessage(), null);
        }
    }
    
    /**
     * Executes a main method from a compiled class
     */
    public ExecutionResult executeMainMethod(String className) {
        try {
            String executionCode = String.format("%s.main(new String[0]);", className);
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
            
            return new ExecutionResult(success, output.toString());
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
        shell = JShell.create();
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