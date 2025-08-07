package com.altf4studios.corebringer.utils;

/**
 * Simple test class to demonstrate the Combat Log functionality
 */
public class CombatLogTest {
    
    public static void main(String[] args) {
        // Test the combat log system
        System.out.println("=== Combat Log Test ===");
        
        // Clear any existing logs
        CombatLog.clear();
        
        // Test different log types
        CombatLog.logSystem("Combat started!");
        CombatLog.logInfo("Player HP: 100 | Enemy HP: 100");
        
        // Test attack logs
        CombatLog.logAttack("Player", "Goblin", 15);
        CombatLog.logAttack("Goblin", "Player", 8);
        
        // Test defense logs
        CombatLog.logDefense("Player", 5);
        
        // Test status effects
        CombatLog.logStatus("Goblin", "Poison", "Takes 3 damage per turn");
        CombatLog.logStatus("Player", "Burning", "Takes 2 damage per turn");
        
        // Test card plays
        CombatLog.logCard("Player", "Fireball", "Deals 20 damage");
        CombatLog.logCard("Goblin", "Heal", "Restores 10 HP");
        
        // Test info messages
        CombatLog.logInfo("Player gains 3 energy");
        CombatLog.logInfo("Enemy is stunned for 1 turn");
        
        // Display all log entries
        System.out.println("\n=== All Log Entries ===");
        for (CombatLog.LogEntry entry : CombatLog.getLogEntries()) {
            System.out.println(entry.toString());
        }
        
        // Display recent entries
        System.out.println("\n=== Recent Entries (last 5) ===");
        for (CombatLog.LogEntry entry : CombatLog.getRecentEntries(5)) {
            System.out.println(entry.toString());
        }
        
        System.out.println("\n=== Test Complete ===");
    }
} 