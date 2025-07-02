package com.altf4studios.corebringer.cards;

import com.altf4studios.corebringer.slots.SlotType;

import java.util.List;

public abstract class Cards {
    public String id;
    public String name;
    public String description;
    public int baseEffect;
    public SlotType type;
    public List<String> tags;
    public String targetType;
    public int cost;
    public String codeEffect;
    public String suggestion;


    public enum TargetSelection{
        PLAYER, ENEMY, AREA;
    }


}
