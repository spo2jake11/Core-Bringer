package com.altf4studios.corebringer.interpreter;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Validator {
    private static final List<String> FORBIDDEN_KEYWORDS = Arrays.asList(
        "System.exit", "Runtime", "Thread", "File", "Process", "reflect", "exec", "getRuntime", "new ProcessBuilder",
        "SecurityManager", "AccessController", "PrivilegedAction", "doPrivileged"
    );
    
    // Keywords that are allowed in class definitions but restricted in execution
    private static final List<String> RESTRICTED_KEYWORDS = Arrays.asList(
        "com.altf4studios.corebringer", "Main", "GameScreen", "EditorStageUI"
    );
    
    private static final Pattern[] FORBIDDEN_PATTERNS = new Pattern[] {
        Pattern.compile("while\\s*\\(\\s*true\\s*\\)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("for\\s*\\(\\s*;\\s*;\\s*\\)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("while\\s*\\(\\s*1\\s*==\\s*1\\s*\\)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("import\\s+com\\.altf4studios\\.corebringer\\..*", Pattern.CASE_INSENSITIVE),
        Pattern.compile("extends\\s+.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile("implements\\s+.*", Pattern.CASE_INSENSITIVE)
    };
    
    private String lastError = "";

    public boolean isValid(String code) {
        // Check for forbidden keywords
        for (String keyword : FORBIDDEN_KEYWORDS) {
            if (code.contains(keyword)) {
                lastError = "Forbidden keyword detected: " + keyword;
                return false;
            }
        }
        
        // Check for restricted keywords (backend access)
        for (String keyword : RESTRICTED_KEYWORDS) {
            if (code.contains(keyword)) {
                lastError = "Restricted keyword detected (backend access): " + keyword;
                return false;
            }
        }
        
        // Check for forbidden patterns
        for (Pattern pattern : FORBIDDEN_PATTERNS) {
            Matcher matcher = pattern.matcher(code);
            if (matcher.find()) {
                lastError = "Forbidden pattern detected: " + pattern.pattern();
                return false;
            }
        }
        
        // Validate class structure
        if (!validateClassStructure(code)) {
            return false;
        }
        
        lastError = "";
        return true;
    }
    
    private boolean validateClassStructure(String code) {
        // Check for proper class syntax
        if (code.contains("public class") || code.contains("class")) {
            // Ensure classes don't extend or implement restricted types
            if (code.contains("extends") || code.contains("implements")) {
                lastError = "Classes cannot extend or implement other classes";
                return false;
            }
            
            // Check for proper class naming
            Pattern classNamePattern = Pattern.compile("(public\\s+)?class\\s+(\\w+)");
            Matcher matcher = classNamePattern.matcher(code);
            if (matcher.find()) {
                String className = matcher.group(2);
                if (className.equals("Main") || className.equals("GameScreen")) {
                    lastError = "Cannot create classes with reserved names: " + className;
                    return false;
                }
            }
        }
        
        return true;
    }

    public String getLastError() {
        return lastError;
    }
}
