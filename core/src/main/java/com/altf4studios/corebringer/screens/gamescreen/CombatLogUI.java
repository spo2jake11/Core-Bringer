package com.altf4studios.corebringer.screens.gamescreen;

import com.altf4studios.corebringer.utils.CombatLog;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

public class CombatLogUI {
    private Table logContainer;
    private ScrollPane scrollPane;
    private List<String> logList;
    private Array<String> logItems;
    private Skin skin;
    
    public CombatLogUI(Skin skin) {
        this.skin = skin;
        this.logItems = new Array<>();
        setupCombatLogUI();
    }
    
    private void setupCombatLogUI() {
        // Create the list for log entries
        logList = new List<>(skin);
        logList.setItems(logItems);
        
        // Create scroll pane for the log
        scrollPane = new ScrollPane(logList, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false); // Disable horizontal scrolling
        scrollPane.setForceScroll(false, true); // Force vertical scrolling
        
        // Create container table
        logContainer = new Table();
        logContainer.setBackground(skin.getDrawable("list")); // Use list background
        
        // Add a title label
        Label titleLabel = new Label("Combat Log", skin);
        titleLabel.setAlignment(Align.center);
        titleLabel.setColor(Color.YELLOW);
        
        // Add title and scroll pane to container
        logContainer.add(titleLabel).fillX().pad(5).row();
        logContainer.add(scrollPane).fill().expand().pad(5);
        
        // Set default size
        logContainer.setSize(300, 200);
    }
    
    public void updateLog() {
        // Get recent log entries and convert to display strings
        Array<CombatLog.LogEntry> entries = CombatLog.getRecentEntries(20); // Show last 20 entries
        logItems.clear();
        
        for (CombatLog.LogEntry entry : entries) {
            String displayText = formatLogEntry(entry);
            logItems.add(displayText);
        }
        
        // Update the list
        logList.setItems(logItems);
        
        // Scroll to bottom to show latest entries
        scrollPane.setScrollY(scrollPane.getMaxY());
    }
    
    private String formatLogEntry(CombatLog.LogEntry entry) {
        // Add color coding based on log type
        String colorPrefix = getColorPrefix(entry.type);
        return colorPrefix + entry.toString();
    }
    
    private String getColorPrefix(CombatLog.LogType type) {
        switch (type) {
            case ATTACK:
                return "[RED]"; // Red for attacks
            case DEFENSE:
                return "[BLUE]"; // Blue for defense
            case STATUS:
                return "[PURPLE]"; // Purple for status effects
            case CARD:
                return "[GREEN]"; // Green for card plays
            case SYSTEM:
                return "[YELLOW]"; // Yellow for system messages
            case INFO:
            default:
                return "[WHITE]"; // White for general info
        }
    }
    
    public Table getLogContainer() {
        return logContainer;
    }
    
    public void setSize(float width, float height) {
        logContainer.setSize(width, height);
    }
    
    public void setPosition(float x, float y) {
        logContainer.setPosition(x, y);
    }
    
    public void clear() {
        logItems.clear();
        logList.setItems(logItems);
    }
    
    // Method to add a custom entry directly to the UI
    public void addCustomEntry(String message) {
        logItems.add("[WHITE]" + message);
        if (logItems.size > 50) { // Keep UI list manageable
            logItems.removeIndex(0);
        }
        logList.setItems(logItems);
        scrollPane.setScrollY(scrollPane.getMaxY());
    }
} 