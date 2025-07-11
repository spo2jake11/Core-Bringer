package com.altf4studios.corebringer.interpreter;

import jdk.jshell.JShell;
import jdk.jshell.Snippet;
import jdk.jshell.SnippetEvent;
import java.util.List;

import jdk.jshell.JShell;

import java.time.Duration;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class JShellExecutor {
<<<<<<< HEAD
    private JShell shell = JShell.create();
    final Duration timeout = Duration.ofSeconds(900);

    /* public String TimeOut(String code, long timeOut){

    } */

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

=======
    private JShell shell;

    public JShellExecutor() {
        shell = JShell.create();
        // Optionally preload game objects here
    }

    /**
     * Submits code to the JShell session and returns output/errors as a string.
     */
    public String submitCode(String code) {
        StringBuilder output = new StringBuilder();
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
        return output.toString();
    }

    /**
     * Resets the JShell session, clearing all user-defined state.
     */
    public void resetSession() {
        shell.close();
        shell = JShell.create();
        // Optionally reload game objects
    }
>>>>>>> 5e4b9dc6da09a5abb75e1ad864474c84b0c0491e
}
