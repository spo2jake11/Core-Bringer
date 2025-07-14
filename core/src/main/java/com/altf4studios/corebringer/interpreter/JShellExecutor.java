package com.altf4studios.corebringer.interpreter;

import jdk.jshell.JShell;
import jdk.jshell.Snippet;
import jdk.jshell.SnippetEvent;
import java.util.List;

public class JShellExecutor {
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
}
