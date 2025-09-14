package com.altf4studios.corebringer.compiler;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Static policy validator for user-provided Java code.
 * - Blocks dangerous APIs (processes, file/network IO, reflection, classloading, system exit)
 * - Blocks common infinite-loop patterns
 * - Enforces size limits
 * - Provides a user-facing policy summary/template
 */
public final class  CodePolicyValidator {
    private static final int MAX_SOURCE_CHARS = 12000;
    private static final int MAX_SOURCE_LINES = 400;

    private static final Pattern[] FORBIDDEN_PATTERNS = new Pattern[] {
        // Disallow packages and custom classloaders
        Pattern.compile("\\bpackage\\s+"),
        Pattern.compile("ClassLoader"),

        // System/process control
        Pattern.compile("System\\.exit\\s*\\("),
        Pattern.compile("Runtime\\.getRuntime\\s*\\("),
        Pattern.compile("ProcessBuilder"),
        Pattern.compile("Process\\s*\\("),

        // Reflection
        Pattern.compile("java\\.lang\\.reflect"),
        Pattern.compile("Class\\s*\\.forName\\s*\\("),
        Pattern.compile("Method\\b"),
        Pattern.compile("Field\\b"),

        // File/FS/Network
        Pattern.compile("java\\.io\\."),
        Pattern.compile("java\\.nio\\."),
        Pattern.compile("java\\.net\\."),
        Pattern.compile("new\\s+File\\s*\\("),
        Pattern.compile("Files\\."),
        Pattern.compile("Paths\\."),

        // Properties/security
        Pattern.compile("SecurityManager"),
        Pattern.compile("System\\.setProperty\\s*\\("),

        // Native/unsafe/internal
        Pattern.compile("\\bnative\\b"),
        Pattern.compile("sun\\.|com\\.sun\\."),

        // Threads and sleeping (can be abused); execution timeout will still protect us
        Pattern.compile("Thread\\.sleep\\s*\\("),
        Pattern.compile("new\\s+Thread\\s*\\("),

        // Infinite loop primitives
        Pattern.compile("while\\s*\\(\\s*true\\s*\\)"),
        Pattern.compile("for\\s*\\(\\s*;\\s*;\\s*\\)"),
        Pattern.compile("do\\s*\\{[\\s\\S]*?\\}\\s*while\\s*\\(\\s*true\\s*\\)"),

        // Synchronization primitives (avoid deadlocks)
        Pattern.compile("synchronized\\s*\\("),

        // Loading arbitrary classes/resources
        Pattern.compile("Class\\.getResource"),
        Pattern.compile("Class\\.getResourceAsStream")
    };

    public static ValidationResult validate(String source) {
        List<String> violations = new ArrayList<>();

        if (source == null || source.trim().isEmpty()) {
            return ValidationResult.fail("Empty source code");
        }

        if (source.length() > MAX_SOURCE_CHARS) {
            violations.add("Source too large: " + source.length() + " chars (max " + MAX_SOURCE_CHARS + ")");
        }

        int lines = source.split("\n", -1).length;
        if (lines > MAX_SOURCE_LINES) {
            violations.add("Too many lines: " + lines + " (max " + MAX_SOURCE_LINES + ")");
        }

        for (Pattern p : FORBIDDEN_PATTERNS) {
            if (p.matcher(source).find()) {
                violations.add("Disallowed pattern matched: '" + p.pattern() + "'");
            }
        }

        // Optional: basic recursion heuristic (best effort)
        // If a method name appears calling itself more than N times in source, flag
        // This is intentionally conservative and complemented by execution timeout

        if (violations.isEmpty()) {
            return ValidationResult.ok();
        }
        return ValidationResult.fail(String.join("\n", violations));
    }

    public static String policyTemplate() {
        return String.join("\n",
            "=== Allowed Code Template ===",
            "- Provide a single public class with a main method.",
            "- No package declarations or custom imports needed.",
            "",
            "Example:",
            "public class Main {",
            "    public static void main(String[] args) {",
            "        // Your logic here",
            "        System.out.println(\"Hello!\");",
            "    }",
            "}",
            "",
            "=== Disallowed (examples) ===",
            "- package declarations",
            "- File/Network/FS APIs: java.io.*, java.nio.*, java.net.*, new File(...), Files.*, Paths.*",
            "- Process and system control: System.exit(...), Runtime.getRuntime(), ProcessBuilder",
            "- Reflection/Classloading: java.lang.reflect.*, Class.forName, ClassLoader",
            "- Security/system changes: SecurityManager, System.setProperty",
            "- Native/internal: native methods, sun.*, com.sun.*",
            "- Thread control: Thread.sleep, new Thread(...) (execution is still time-limited)",
            "- Infinite loops: while(true), for(;;), do { ... } while(true)",
            "- Synchronization blocks: synchronized(...)",
            "",
            "Notes:",
            "- Execution time is limited to prevent long-running code.",
            "- Output is captured and shown in the Output window.",
            "- Keep solutions small: max " + MAX_SOURCE_LINES + " lines / " + MAX_SOURCE_CHARS + " chars.");
    }

    public static final class ValidationResult {
        public final boolean valid;
        public final String message;
        private ValidationResult(boolean valid, String message) {
            this.valid = valid; this.message = message;
        }
        public static ValidationResult ok() { return new ValidationResult(true, "OK"); }
        public static ValidationResult fail(String msg) { return new ValidationResult(false, msg); }
    }
}


