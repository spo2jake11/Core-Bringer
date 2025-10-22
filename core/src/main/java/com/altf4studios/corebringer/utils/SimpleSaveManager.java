package com.altf4studios.corebringer.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

/**
 * Simplified save data manager that allows each component to save its own data
 */
public class SimpleSaveManager {
    private static final String FILENAME = "Core Bringer/corebringer_save.json";
    private static final Json json = new Json();
    
    static {
        json.setOutputType(JsonWriter.OutputType.json);
    }
    
    /**
     * Load the complete save data
     */
    public static SaveData loadData() {
        FileHandle file = Gdx.files.external(FILENAME);
        if (!file.exists()) {
            return new SaveData();
        }
        try {
            String content = file.readString();
            return json.fromJson(SaveData.class, content);
        } catch (Exception e) {
            Gdx.app.error("SimpleSaveManager", "Failed to load save data: " + e.getMessage());
            return new SaveData();
        }
    }
    
    /**
     * Save the complete save data
     */
    public static void saveData(SaveData data) {
        try {
            FileHandle file = Gdx.files.external(FILENAME);
            file.writeString(json.prettyPrint(data), false);
            Gdx.app.log("SimpleSaveManager", "Save data updated successfully");
        } catch (Exception e) {
            Gdx.app.error("SimpleSaveManager", "Failed to save data: " + e.getMessage());
        }
    }
    
    /**
     * Update specific fields in save data (preserves all other data)
     */
    public static void updateData(DataUpdater updater) {
        SaveData data = loadData();
        updater.update(data);
        saveData(data);
    }
    
    /**
     * Functional interface for updating save data
     */
    @FunctionalInterface
    public interface DataUpdater {
        void update(SaveData data);
    }
    
    /**
     * Check if save file exists
     */
    public static boolean saveExists() {
        return Gdx.files.external(FILENAME).exists();
    }
    
    /**
     * Delete save file
     */
    public static void deleteSave() {
        FileHandle file = Gdx.files.external(FILENAME);
        if (file.exists()) {
            file.delete();
            Gdx.app.log("SimpleSaveManager", "Save file deleted");
        }
    }
    
    /**
     * Save stats with specific parameters (compatibility method)
     */
    public static void saveStats(int currentHp, int maxHp, int energy, int maxEnergy, String[] cards, int battleWon, int gold, int stageLevel) {
        updateData(data -> {
            data.currentHp = currentHp;
            data.maxHp = maxHp;
            data.energy = energy;
            data.maxEnergy = maxEnergy;
            data.cards = cards;
            data.battleWon = battleWon;
            data.gold = gold;
            data.stageLevel = stageLevel;
            data.hp = currentHp; // For backward compatibility
        });
    }
    
    /**
     * Save stats without stage level (compatibility method)
     */
    public static void saveStats(int currentHp, int maxHp, int energy, int maxEnergy, String[] cards, int battleWon, int gold) {
        updateData(data -> {
            data.currentHp = currentHp;
            data.maxHp = maxHp;
            data.energy = energy;
            data.maxEnergy = maxEnergy;
            data.cards = cards;
            data.battleWon = battleWon;
            data.gold = gold;
            data.hp = currentHp; // For backward compatibility
        });
    }
}
