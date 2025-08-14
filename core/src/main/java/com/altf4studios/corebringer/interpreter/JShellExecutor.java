package com.altf4studios.corebringer.interpreter;

import com.altf4studios.corebringer.entities.Entity;
import jdk.jshell.JShell;
import jdk.jshell.Snippet;
import jdk.jshell.SnippetEvent;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class JShellExecutor {
    private JShell shell;
    private static JShellExecutor instance;
    private ByteArrayOutputStream capturedOutput;
    private PrintStream customOut;

    public JShellExecutor() {
        capturedOutput = new ByteArrayOutputStream();
        customOut = new PrintStream(capturedOutput);
        shell = JShell.builder()
            .out(customOut)
            .build();
        // Optionally preload game objects here
    }

    public static JShellExecutor getInstance() {
        if (instance == null) {
            instance = new JShellExecutor();
        }
        return instance;
    }

    public void setContext(Entity user, Entity target) {
        this.shell.eval(String.format("com.altf4studios.corebringer.entities.Entity target = (%s) shell.varValue(\"target\");", target.getClass().getName()));
        this.shell.eval("import com.altf4studios.corebringer.entities.*;");
    }

    public static void runScript(String codeEffect, Entity user, Entity target) {
        JShellExecutor executor = getInstance();
        executor.setContext(user, target);
        System.out.println("Executing JShell script: " + codeEffect);
        executor.submitCode(codeEffect);
    }

    /**
     * Submits code to the JShell session and returns output/errors as a string.
     * Captures System.out output during execution.
     */
    public String submitCode(String code) {
        System.out.println("JShellExecutor.submitCode() called with: " + code);
        
        // Clear previous output
        capturedOutput.reset();
        
        StringBuilder output = new StringBuilder();
        
        try {
            List<SnippetEvent> events = shell.eval(code);
            for (SnippetEvent event : events) {
                if (event.exception() != null) {
                    output.append("Exception: ").append(event.exception().getMessage()).append("\n");
                } else if (event.value() != null) {
                    output.append(event.value()).append("\n");
                } else if (event.status() == Snippet.Status.REJECTED) {
                    output.append("Error: ").append(event.snippet().source()).append(" was rejected.\n");
                }
            }
            
            // Get captured output from JShell
            String capturedOut = capturedOutput.toString();
            System.out.println("Captured JShell output: '" + capturedOut + "'");
            if (!capturedOut.isEmpty()) {
                output.append(capturedOut);
            }
            
        } catch (Exception e) {
            output.append("Error: ").append(e.getMessage()).append("\n");
        }
        
        System.out.println("Final output: '" + output.toString() + "'");
        return output.toString();
    }

    //Timeout stuff
    public String TimeOut(String code, long Time) {
        final FutureTask<String> task = new FutureTask<>(() -> {
            shell.eval(code);
            return "Code Successful!";
        });

        Thread thread = new Thread(task);
        thread.start();

        try {
            return task.get(Time, TimeUnit.MINUTES);

        } catch (TimeoutException e) {
            thread.interrupt();
            return "Timeout: Enemy Turn!";

        } catch (Exception e) {
            thread.interrupt();
            return "Error: Enemy Turn!!";

        }
    }
    /**
     * Resets the JShell session, clearing all user-defined state.
     */
    public void resetSession() {
        shell.close();
        capturedOutput.reset();
        shell = JShell.builder()
            .out(customOut)
            .build();
        // Optionally reload game objects
    }
}
