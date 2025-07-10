package com.altf4studios.corebringer.entities;

public class Enemy {
    public String entityID;
    public String name;
    public int healthPoints;
    public enemyType type;
    public int skillCooldown;
    public String[] skills;


    public enum enemyType{
        NORMAL, ELITE, BOSS, SPECIAL;
    }
}
