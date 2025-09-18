package com.altf4studios.corebringer.screens.gamescreen;

import com.badlogic.gdx.utils.Array;

public class SampleCardHandler {
    public String id;
    public int level;
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