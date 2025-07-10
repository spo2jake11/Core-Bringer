package com.altf4studios.corebringer.entities;

public interface BattleEntity {
    void takeDamage(int amount);
    void applyStatus();
    void target(Player player);
    void target(Enemy enemy);

}
