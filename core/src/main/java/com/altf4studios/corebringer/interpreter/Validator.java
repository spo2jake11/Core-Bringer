package com.altf4studios.corebringer.interpreter;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Validator {
    private static final List<String> FORBIDDEN_KEYWORDS = Arrays.asList(
        "System.exit", "Runtime", "Thread", "File", "Process", "reflect", "exec", "getRuntime", "new ProcessBuilder"
    );
    private static final Pattern[] FORBIDDEN_PATTERNS = new Pattern[] {
        Pattern.compile("while\\s*\\(\\s*true\\s*\\)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("for\\s*\\(\\s*;\\s*;\\s*\\)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("while\\s*\\(\\s*1\\s*==\\s*1\\s*\\)", Pattern.CASE_INSENSITIVE),
        // Add more patterns as needed
        // for PATTERNS MUST AND EFFICIENT
    };
    private String lastError = "";

    public boolean isValid(String code) {
        for (String keyword : FORBIDDEN_KEYWORDS) {
            if (code.contains(keyword)) {
                lastError = "Forbidden keyword detected: " + keyword;
                return false;
            }
        }
             for (Pattern pattern : FORBIDDEN_PATTERNS) {
                   Matcher matcher = pattern.matcher(code);
                   if (matcher.find()) {
                        lastError = "Forbidden pattern detected: " + pattern.pattern();
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
