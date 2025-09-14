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

    @Override
    public void onTurnStart(Entity target) {
        // Apply poison damage at the start of each turn
        if (target != null && target.isAlive() && power > 0) {
            // Apply poison damage equal to current power
            target.takeDamage(power);

            // Log the poison damage
            Gdx.app.log("Poison", target.getName() + " took " + power + " poison damage. Remaining HP: " + target.getHp());

            // Reduce power by 1 per turn (poison stacks decrease over time)
            decreasePower(1);

            // Log remaining power
            if (power > 0) {
                Gdx.app.log("Poison", "Poison power remaining: " + power);
            } else {
                Gdx.app.log("Poison", "Poison effect has expired");
            }
        }
        
        // Reduce duration
        tick();
    }

    @Override
    public void onExpire(Entity target) {
        Gdx.app.log("Poison", "Poison effect expired for: " + name);
    }

    @Override
    public void increasePower(int amount) {
        super.increasePower(amount);
        Gdx.app.log("Poison", "Poison power increased to: " + power);
    }

    @Override
    public void extendDuration(int amount) {
        super.extendDuration(amount);
        Gdx.app.log("Poison", "Poison duration extended to: " + duration + " turns");
    }

    /**
     * Apply poison damage to the target entity (legacy method for backward compatibility)
     * @param target The entity to apply poison damage to
     */
    public void applyPoisonDamage(Entity target) {
        onTurnStart(target);
    }
}
