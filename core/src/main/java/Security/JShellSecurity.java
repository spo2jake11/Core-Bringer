package Security;

import java.util.*;

public class JShellSecurity {
    private static final Map<String, String> bannedKeywords = new HashMap<>();

    static {
        bannedKeywords.put("System.exit", "Cannot terminate the game.");
        bannedKeywords.put("Thread", "Thread manipulation is not allowed.");
        bannedKeywords.put("Runtime", "Runtime access is not allowed.");
        bannedKeywords.put("ProcessBuilder", "Process creation is not allowed.");
        bannedKeywords.put("File", "File access is not allowed.");
        bannedKeywords.put("Paths", "File system access is not allowed.");
        bannedKeywords.put("InputStream", "Input/Output streams are not allowed.");
        bannedKeywords.put("OutputStream", "Input/Output streams are not allowed.");
        bannedKeywords.put("Socket", "Network access is not allowed.");
        bannedKeywords.put("exec", "Process execution is not allowed.");
        bannedKeywords.put("java.lang.reflect", "Reflection is not allowed.");
        bannedKeywords.put("while(true)", "Infinite loops are not allowed.");
        bannedKeywords.put("for(;;)", "Infinite loops are not allowed.");
        bannedKeywords.put("while ( true )", "Infinite loops are not allowed.");
        bannedKeywords.put("while(1==1)", "Infinite loops are not allowed.");
        bannedKeywords.put("for(;true;)", "Infinite loops are not allowed.");
    }

    public static String checkSafety(String code) {
        for (Map.Entry<String, String> entry : bannedKeywords.entrySet()) {
            if (code.contains(entry.getKey())) {
                return "Rejected: " + entry.getValue();
            }
        }
        return null; // null means safe
    }
}
