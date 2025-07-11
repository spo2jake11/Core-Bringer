package com.altf4studios.corebringer.screens;

import com.altf4studios.corebringer.screens.*;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Array;

public class SampleCardHandler {
    public String id;
    public String name;
    public String type;
    public String description;
    public String targetType;
    public int cost;
    public int baseEffect;
    public String codeEffect;
    public String suggestion;
    public Array<String> tags; // or List<String> tags;

    @Override
    public String toString() {
        return ("Card{id= '" + id + "', name= '" + name + "', type= '" + type + "', power=" + baseEffect + "}");
    }
}
