package com.altf4studios.corebringer.entities;

public abstract class Entity implements BattleEntity {
    protected String name;
    protected int maxHealth;
    protected int health;
    protected int attack;
    protected int defense;
    protected boolean alive = true;

    public Entity(String name, int maxHealth, int attack, int defense) {
        this.name = name;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.attack = attack;
        this.defense = defense;
    }

    public String getName() {
        return name;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public int getAttack() {
        return attack;
    }

    public int getDefense() {
        return defense;
    }

    public boolean isAlive() {
        return alive;
    }

    @Override
    public void takeDamage(int amount) {
        int damage = Math.max(0, amount - defense);
        health -= damage;
        if (health <= 0) {
            health = 0;
            alive = false;
            onDeath();
        }
    }

    protected void onDeath() {
        // Default death behavior, can be overridden
    }

    @Override
    public void applyStatus() {
        // Default: do nothing. Override in subclasses for status effects.
    }

    // Targeting methods to be implemented in subclasses
}
