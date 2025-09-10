package com.altf4studios.corebringer.utils;

import com.altf4studios.corebringer.utils.SaveData;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import java.io.*;
import java.nio.file.*;

public class SaveManager {
    private static final String FILENAME = "corebringer_save.json";
    private static final String DOCS_PATH = System.getProperty("user.home") + File.separator + "Documents";
    private static final String GAME_FOLDER = "Core Bringer";
    private static final String GAME_FOLDER_PATH = DOCS_PATH + File.separator + GAME_FOLDER;
    private static final String FULL_PATH = GAME_FOLDER_PATH + File.separator + FILENAME;

    public static void ensureGameFolderExists() {
        File folder = new File(GAME_FOLDER_PATH);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    public static String getSavePath() {
        ensureGameFolderExists();
        return FULL_PATH;
    }

    public static boolean saveExists() {
        ensureGameFolderExists();
        return Files.exists(Paths.get(FULL_PATH));
    }

    public static void deleteSave() {
        ensureGameFolderExists();
        try {
            Files.deleteIfExists(Paths.get(FULL_PATH));
            System.out.println("[SaveManager] Save file deleted: " + FULL_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveStats(int hp, int energy, String[] cards, int battleWon) {
        ensureGameFolderExists();
        SaveData data = new SaveData(hp, energy, cards, battleWon);
        Json json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);
        try (FileWriter writer = new FileWriter(FULL_PATH)) {
            writer.write(json.prettyPrint(data));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static SaveData loadStats() {
        ensureGameFolderExists();
        Json json = new Json();
        try {
            String content = new String(Files.readAllBytes(Paths.get(FULL_PATH)));
            return json.fromJson(SaveData.class, content);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
