package com.altf4studios.corebringer.entities;

public class Player extends Entity {
    private int currentCardCount;
    private final int totalCardCount = 15;
    private int energy;

    public Player(String name, int maxHealth, int attack, int defense, int energy) {
        super(name, maxHealth, attack, defense);
        this.energy = energy;
        this.currentCardCount = totalCardCount;
    }

    public int getCurrentCardCount() {
        return currentCardCount;
    }

    public int getTotalCardCount() {
        return totalCardCount;
    }

    public int getEnergy() {
        return energy;
    }

    public void useCard() {
        if (currentCardCount > 0) {
            currentCardCount--;
        }
    }

    public void restoreCards() {
        currentCardCount = totalCardCount;
    }

    public void useEnergy(int amount) {
        if (energy >= amount) {
            energy -= amount;
        }
    }

    public void restoreEnergy(int amount) {
        energy += amount;
    }

    // Player-specific action
    public void heal(int amount) {
        if (alive) {
            health = Math.min(maxHealth, health + amount);
        }
    }

    @Override
    public void target(Player player) {
        // Player targeting another player (if needed)
    }

    @Override
    public void target(Enemy enemy) {
        // Player targets an enemy (e.g., attack)
    }
}
