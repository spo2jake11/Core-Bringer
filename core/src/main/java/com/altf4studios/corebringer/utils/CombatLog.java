package com.altf4studios.corebringer.utils;

import com.badlogic.gdx.utils.Array;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CombatLog {
    private static Array<LogEntry> logEntries = new Array<>();
    private static final int MAX_ENTRIES = 100; // Prevent memory issues
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    
    public static class LogEntry {
        public final String message;
        public final String timestamp;
        public final LogType type;
        
        public LogEntry(String message, LogType type) {
            this.message = message;
            this.timestamp = timeFormat.format(new Date());
            this.type = type;
        }
        
        @Override
        public String toString() {
            return "[" + timestamp + "] " + message;
        }
    }
    
    public enum LogType {
        ATTACK("Attack"),
        DEFENSE("Defense"), 
        STATUS("Status"),
        CARD("Card"),
        SYSTEM("System"),
        INFO("Info");
        
        private final String displayName;
        
        LogType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Add a log entry
    public static void addEntry(String message, LogType type) {
        LogEntry entry = new LogEntry(message, type);
        logEntries.add(entry);
        
        // Keep only the last MAX_ENTRIES entries
        if (logEntries.size > MAX_ENTRIES) {
            logEntries.removeIndex(0);
        }
        
        // Also print to console for debugging
        System.out.println("[CombatLog] " + entry.toString());
    }
    
    // Convenience methods for different log types
    public static void logAttack(String attacker, String target, int damage) {
        addEntry(attacker + " attacks " + target + " for " + damage + " damage!", LogType.ATTACK);
    }
    
    public static void logDefense(String defender, int blockedDamage) {
        addEntry(defender + " blocks " + blockedDamage + " damage!", LogType.DEFENSE);
    }
    
    public static void logStatus(String target, String status, String effect) {
        addEntry(target + " is affected by " + status + ": " + effect, LogType.STATUS);
    }
    
    public static void logCard(String player, String cardName, String effect) {
        addEntry(player + " plays " + cardName + ": " + effect, LogType.CARD);
    }
    
    public static void logSystem(String message) {
        addEntry(message, LogType.SYSTEM);
    }
    
    public static void logInfo(String message) {
        addEntry(message, LogType.INFO);
    }
    
    // Get all log entries
    public static Array<LogEntry> getLogEntries() {
        return logEntries;
    }
    
    // Get recent entries (last N entries)
    public static Array<LogEntry> getRecentEntries(int count) {
        Array<LogEntry> recent = new Array<>();
        int startIndex = Math.max(0, logEntries.size - count);
        for (int i = startIndex; i < logEntries.size; i++) {
            recent.add(logEntries.get(i));
        }
        return recent;
    }
    
    // Clear the log
    public static void clear() {
        logEntries.clear();
    }
    
    // Legacy method for backward compatibility
    public static void logs(String msg) {
        logInfo(msg);
    }
}
