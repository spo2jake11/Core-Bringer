package com.altf4studios.corebringer.utils;

public class SettingsData {
    public float volume; // 0.0 - 1.0
    public boolean muted;

    public SettingsData() {
        // default constructor for LibGDX Json
    }

    public SettingsData(float volume, boolean muted) {
        this.volume = volume;
        this.muted = muted;
    }
}
