package com.altf4studios.corebringer.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

public class SaveManager {
    // Save file will be stored in the user's external storage in a subfolder
    private static final String FILENAME = "Core Bringer/corebringer_save.json";

    public static boolean saveExists() {
        FileHandle file = Gdx.files.external(FILENAME);
        return file.exists();
    }

    public static void deleteSave() {
        FileHandle file = Gdx.files.external(FILENAME);
        if (file.exists()) {
            file.delete();
            System.out.println("[SaveManager] Save file deleted: " + file.file().getAbsolutePath());
        } else {
            System.out.println("[SaveManager] No save file to delete at: " + file.file().getAbsolutePath());
        }
    }

    // New API: includes maxEnergy, gold, and stageLevel
    public static void saveStats(int currentHp, int maxHp, int energy, int maxEnergy, String[] cards, int battleWon, int gold, int stageLevel) {
        // Load existing data to preserve questionData and other fields
        SaveData data = loadStats();
        if (data == null) {
            data = new SaveData(currentHp, maxHp, energy, maxEnergy, cards, battleWon);
        } else {
            // Update the existing data with new values
            data.currentHp = currentHp;
            data.maxHp = maxHp;
            data.energy = energy;
            data.maxEnergy = maxEnergy;
            data.cards = cards;
            data.battleWon = battleWon;
        }
        // Also populate deprecated field for compatibility with old readers/tools
        data.hp = currentHp;
        data.gold = gold;
        data.stageLevel = Math.max(1, Math.min(5, stageLevel));
        Json json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);
        FileHandle file = Gdx.files.external(FILENAME);
        file.writeString(json.prettyPrint(data), false);
    }

    // Backward-compatible: includes maxEnergy and gold, preserves existing stageLevel if any
    public static void saveStats(int currentHp, int maxHp, int energy, int maxEnergy, String[] cards, int battleWon, int gold) {
        int stage = 1;
        try {
            SaveData existing = loadStats();
            if (existing != null && existing.stageLevel > 0) stage = existing.stageLevel;
        } catch (Exception ignored) {}
        saveStats(currentHp, maxHp, energy, maxEnergy, cards, battleWon, gold, stage);
    }

    // Backward-compatible overload (deprecated). Uses hp for both current and max.
    @Deprecated
    public static void saveStats(int hp, int energy, String[] cards, int battleWon) {
        // Preserve existing maxEnergy if present; default to 3
        int maxE = 3;
        try {
            SaveData existing = loadStats();
            if (existing != null && existing.maxEnergy > 0) maxE = existing.maxEnergy;
        } catch (Exception ignored) {}
        saveStats(hp, hp, energy, maxE, cards, battleWon, 0);
    }

    // Backward-compatible overload without maxEnergy, default to 3
    public static void saveStats(int currentHp, int maxHp, int energy, String[] cards, int battleWon) {
        // Preserve existing maxEnergy if present; default to 3
        int maxE = 3;
        try {
            SaveData existing = loadStats();
            if (existing != null && existing.maxEnergy > 0) maxE = existing.maxEnergy;
        } catch (Exception ignored) {}
        saveStats(currentHp, maxHp, energy, maxE, cards, battleWon, 0);
    }

    // Previous primary signature kept for source compatibility (gold defaults to 0 if not provided)
    public static void saveStats(int currentHp, int maxHp, int energy, int maxEnergy, String[] cards, int battleWon) {
        saveStats(currentHp, maxHp, energy, maxEnergy, cards, battleWon, 0);
    }

    public static SaveData loadStats() {
        FileHandle file = Gdx.files.external(FILENAME);
        if (!file.exists()) {
            System.out.println("[SaveManager] No save file found at: " + file.file().getAbsolutePath());
            return null;
        }
        Json json = new Json();
        String content = file.readString();
        return json.fromJson(SaveData.class, content);
    }

    // Initialize stageLevel to a default if missing, without changing other fields
    public static void ensureStageLevelInitialized(int defaultStage) {
        int clamped = Math.max(1, Math.min(5, defaultStage));
        try {
            SaveData data = loadStats();
            if (data == null) {
                data = new SaveData();
                data.currentHp = 20;
                data.maxHp = 20;
                data.energy = 0;
                data.maxEnergy = 3;
                data.cards = new String[]{};
                data.battleWon = 0;
                data.gold = 0;
                data.stageLevel = clamped;
                Json json = new Json();
                json.setOutputType(JsonWriter.OutputType.json);
                FileHandle file = Gdx.files.external(FILENAME);
                file.writeString(json.prettyPrint(data), false);
            } else if (data.stageLevel <= 0) {
                data.stageLevel = clamped;
                Json json = new Json();
                json.setOutputType(JsonWriter.OutputType.json);
                FileHandle file = Gdx.files.external(FILENAME);
                file.writeString(json.prettyPrint(data), false);
            }
        } catch (Exception ignored) {}
    }

    // Update only stageLevel (clamped), keep all other values as-is
    public static void updateStageLevelOnly(int stageLevel) {
        int clamped = Math.max(1, Math.min(5, stageLevel));
        try {
            SaveData data = loadStats();
            if (data == null) {
                data = new SaveData();
                data.stageLevel = clamped;
            } else {
                data.stageLevel = clamped;
            }
            Json json = new Json();
            json.setOutputType(JsonWriter.OutputType.json);
            FileHandle file = Gdx.files.external(FILENAME);
            file.writeString(json.prettyPrint(data), false);
        } catch (Exception ignored) {}
    }
}
