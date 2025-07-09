package com.altf4studios.corebringer.interpreter;

import jdk.jshell.JShell;

import java.time.Duration;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class JShellExecutor {
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

}
