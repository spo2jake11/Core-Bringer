package com.altf4studios.corebringer.entities;

public class Enemy extends Entity {
    private String entityID;
    private enemyType type;
    private int skillCooldown;
    private String[] skills;

    public enum enemyType {
        NORMAL, ELITE, BOSS, SPECIAL;
    }

    public Enemy(String entityID, String name, int maxHealth, int attack, int defense, enemyType type, int skillCooldown, String[] skills) {
        super(name, maxHealth, attack, defense);
        this.entityID = entityID;
        this.type = type;
        this.skillCooldown = skillCooldown;
        this.skills = skills;
    }

    public String getEntityID() {
        return entityID;
    }

    public enemyType getType() {
        return type;
    }

    public int getSkillCooldown() {
        return skillCooldown;
    }

    public String[] getSkills() {
        return skills;
    }

    // Enemy-specific action
    public void useSkill(int index, Entity target) {
        // Implement skill logic here
    }

    @Override
    public void target(Player player) {
        // Enemy targets a player (e.g., attack or use skill)
    }

    @Override
    public void target(Enemy enemy) {
        // Enemy targets another enemy (if needed)
    }
}
