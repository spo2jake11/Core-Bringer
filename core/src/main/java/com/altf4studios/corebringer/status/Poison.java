package com.altf4studios.corebringer.status;

import com.altf4studios.corebringer.entities.Entity;
import com.badlogic.gdx.Gdx;

public class Poison extends StatusEffect {

    public Poison(String name, int power, int duration) {
        super(name, power, duration);
    }

    @Override
    public void onApply() {
        // Log when poison is applied
        Gdx.app.log("Poison", "Poison applied with power: " + power + " for " + duration + " turns");
    }

    /**
     * Apply poison damage to the target entity
     * @param target The entity to apply poison damage to
     */
    public void applyPoisonDamage(Entity target) {
        if (target != null && target.isAlive() && power > 0) {
            // Apply poison damage equal to current stacks (power)
            target.takeDamage(power);
            
            // Log the poison damage
            Gdx.app.log("Poison", target.getName() + " took " + power + " poison damage. Remaining HP: " + target.getHp());
            
            // Reduce stacks by 1 per turn
            power--;
            
            // Log remaining stacks
            if (power > 0) {
                Gdx.app.log("Poison", "Poison stacks remaining: " + power);
            } else {
                Gdx.app.log("Poison", "Poison effect has expired");
            }
        }
    }

    /**
     * Check if the poison effect has expired
     * @return true if duration is 0 or less
     */
    public boolean isExpired() {
        return power <= 0;
    }

    /**
     * Get the current power of the poison
     * @return the poison damage amount
     */
    public int getPower() {
        return power;
    }

    /**
     * Get the remaining duration
     * @return the number of turns remaining
     */
    public int getDuration() {
        return duration;
    }

    /**
     * Increase poison power (for stacking effects)
     * @param additionalPower additional poison damage to add
     */
    public void increasePower(int additionalPower) {
        this.power += additionalPower;
        Gdx.app.log("Poison", "Poison power increased to: " + power);
    }

    /**
     * Extend poison duration
     * @param additionalDuration additional turns to add
     */
    public void extendDuration(int additionalDuration) {
        this.duration += additionalDuration;
        Gdx.app.log("Poison", "Poison duration extended to: " + duration + " turns");
    }
}
