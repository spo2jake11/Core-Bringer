package com.altf4studios.corebringer.utils;

import com.altf4studios.corebringer.utils.SaveData;
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

    public static void saveStats(int hp, int energy, String[] cards, int battleWon) {
        SaveData data = new SaveData(hp, energy, cards, battleWon);
        Json json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);
        FileHandle file = Gdx.files.external(FILENAME);
        file.writeString(json.prettyPrint(data), false);
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
}
