package com.altf4studios.corebringer.cards;

import com.altf4studios.corebringer.slots.SlotType;

import java.util.List;

public class Cards {
    public String id;
    public String name;
    public String description;
    public int baseDamage;
    public SlotType type;
    public Tags tags;
    public TargetType targetType;
    public int cost;
    public String codeEffect;
    public String suggestion;

    public enum TargetType{
        SELF, ENEMY;
    }

    public enum Tags{
        BUFF, DEBUFF, ATTACK, DEFENSE;
    }
}
