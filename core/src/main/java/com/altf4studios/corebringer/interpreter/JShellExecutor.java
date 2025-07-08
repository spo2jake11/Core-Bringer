package com.altf4studios.corebringer.interpreter;
import jdk.jshell.JShell;
import jdk.jshell.SnippetEvent;

public class JShellExecutor {
    private JShell shell;

    public JShellExecutor(){
        shell = JShell.create();
        System.out.println(shell.eval("System.out.println('Hello World')"));
    }
}
