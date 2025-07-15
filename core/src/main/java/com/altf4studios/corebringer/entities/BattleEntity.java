package com.altf4studios.corebringer.entities;

public interface BattleEntity {
    void takeDamage(int amount);
    void gainBlock(int amount);
    void dealDamage(Entity target, int amount);
    int applyDamageModifiers(Entity target, int baseDamage);
    int applyBlockModifiers(int baseBlock);
    void addStatus(String status, int value);
    void removeStatus(String status);
    boolean hasStatus(String status);
    void applyStatus();
    void target(Player player);
    void target(Enemy enemy);
}
