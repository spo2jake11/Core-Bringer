package com.altf4studios.corebringer.interpreter;

import java.util.Arrays;
import java.util.List;

public class Validator {
    private static final List<String> FORBIDDEN_KEYWORDS = Arrays.asList(
        "System.exit", "Runtime", "Thread", "File", "Process", "reflect", "exec", "getRuntime", "new ProcessBuilder"
    );
    private String lastError = "";

    public boolean isValid(String code) {
        for (String keyword : FORBIDDEN_KEYWORDS) {
            if (code.contains(keyword)) {
                lastError = "Forbidden keyword detected: " + keyword;
                return false;
            }
        }
        lastError = "";
        return true;
    }

    public String getLastError() {
        return lastError;
    }
}
