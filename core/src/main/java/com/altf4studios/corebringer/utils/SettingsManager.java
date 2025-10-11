package com.altf4studios.corebringer.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

public class SettingsManager {
    private static final String FILENAME = "Core Bringer/corebringer_settings.json";

    public static boolean exists() {
        FileHandle file = Gdx.files.external(FILENAME);
        return file.exists();
    }

    public static void delete() {
        FileHandle file = Gdx.files.external(FILENAME);
        if (file.exists()) file.delete();
    }

    public static void saveSettings(float volume, boolean muted) {
        SettingsData data = new SettingsData(volume, muted);
        Json json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);
        FileHandle file = Gdx.files.external(FILENAME);
        file.writeString(json.prettyPrint(data), false);
    }

    public static SettingsData loadSettings() {
        FileHandle file = Gdx.files.external(FILENAME);
        if (!file.exists()) return null;
        Json json = new Json();
        String content = file.readString();
        return json.fromJson(SettingsData.class, content);
    }
}
