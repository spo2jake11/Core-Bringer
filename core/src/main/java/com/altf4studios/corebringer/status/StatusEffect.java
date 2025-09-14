package com.altf4studios.corebringer.status;

import com.altf4studios.corebringer.entities.Entity;

public abstract class StatusEffect {
    protected String name;
    protected int power;
    protected int duration;

    public StatusEffect(String name, int power, int duration){
        this.name = name;
        this.power = power;
        this.duration = duration;
    }

    // Abstract methods that must be implemented by subclasses
    public abstract void onApply();

    // Lifecycle methods that can be overridden by specific status effects
    /**
     * Called at the start of each turn for the affected entity
     * @param target The entity affected by this status
     */
    public void onTurnStart(Entity target) {
        // Default implementation does nothing
    }

    /**
     * Called at the end of each turn for the affected entity
     * @param target The entity affected by this status
     */
    public void onTurnEnd(Entity target) {
        // Default implementation does nothing
    }

    /**
     * Called when the status effect expires
     * @param target The entity that was affected by this status
     */
    public void onExpire(Entity target) {
        // Default implementation does nothing
    }

    /**
     * Called when the status effect is removed (manually or by expiration)
     * @param target The entity that was affected by this status
     */
    public void onRemove(Entity target) {
        // Default implementation does nothing
    }

    // Damage and block modification methods
    /**
     * Modify incoming damage to the affected entity
     * @param baseDamage The original damage amount
     * @param target The entity receiving damage
     * @return The modified damage amount
     */
    public int modifyIncomingDamage(int baseDamage, Entity target) {
        return baseDamage;
    }

    /**
     * Modify outgoing damage from the affected entity
     * @param baseDamage The original damage amount
     * @param target The entity dealing damage
     * @return The modified damage amount
     */
    public int modifyOutgoingDamage(int baseDamage, Entity target) {
        return baseDamage;
    }

    /**
     * Modify block amount for the affected entity
     * @param baseBlock The original block amount
     * @param target The entity with block
     * @return The modified block amount
     */
    public int modifyBlock(int baseBlock, Entity target) {
        return baseBlock;
    }

    /**
     * Modify healing amount for the affected entity
     * @param baseHealing The original healing amount
     * @param target The entity receiving healing
     * @return The modified healing amount
     */
    public int modifyHealing(int baseHealing, Entity target) {
        return baseHealing;
    }

    // Utility methods for status management
    /**
     * Check if the status effect has expired
     * @return true if duration is 0 or less
     */
    public boolean isExpired() {
        return duration <= 0;
    }

    /**
     * Reduce duration by 1 turn
     */
    public void tick() {
        if (duration > 0) {
            duration--;
        }
    }

    /**
     * Reduce duration by specified amount
     * @param amount The amount to reduce duration by
     */
    public void reduceDuration(int amount) {
        duration = Math.max(0, duration - amount);
    }

    /**
     * Extend duration by specified amount
     * @param amount The amount to extend duration by
     */
    public void extendDuration(int amount) {
        duration += amount;
    }

    /**
     * Increase power by specified amount
     * @param amount The amount to increase power by
     */
    public void increasePower(int amount) {
        power += amount;
    }

    /**
     * Decrease power by specified amount
     * @param amount The amount to decrease power by
     */
    public void decreasePower(int amount) {
        power = Math.max(0, power - amount);
    }

    // Getter methods
    public String getName() {
        return name;
    }

    public int getPower() {
        return power;
    }

    public int getDuration() {
        return duration;
    }

    // Setter methods
    public void setName(String name) {
        this.name = name;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return name + " (Power: " + power + ", Duration: " + duration + ")";
    }
}
