package com.altf4studios.corebringer.status;

import com.altf4studios.corebringer.entities.Entity;
import com.badlogic.gdx.Gdx;

public class ShieldStatus extends StatusEffect {
    
    public ShieldStatus(int power, int duration) {
        super("Shield", power, duration);
    }
    
    @Override
    public void onApply() {
        // Log when shield is applied
        Gdx.app.log("Shield", "Shield applied with " + power + " protection for " + duration + " turns");
    }
    
    @Override
    public int modifyIncomingDamage(int baseDamage, Entity target) {
        // Shield reduces incoming damage by its power amount
        if (power > 0) {
            int damageReduction = Math.min(power, baseDamage);
            int remainingDamage = baseDamage - damageReduction;
            
            // Reduce shield power by the amount of damage blocked
            decreasePower(damageReduction);
            
            // Log shield blocking
            if (damageReduction > 0) {
                Gdx.app.log("Shield", target.getName() + "'s shield blocked " + damageReduction + " damage. Remaining shield: " + power);
            }
            
            return remainingDamage;
        }
        return baseDamage;
    }
    
    @Override
    public void onTurnStart(Entity target) {
        // Shield doesn't do anything special at turn start, just reduce duration
        tick();
    }
    
    @Override
    public void onExpire(Entity target) {
        Gdx.app.log("Shield", "Shield effect expired for " + target.getName());
    }
    
    @Override
    public void onRemove(Entity target) {
        Gdx.app.log("Shield", "Shield removed from " + target.getName());
    }
    
    /**
     * Check if the shield is completely depleted
     * @return true if shield power is 0 or less
     */
    public boolean isDepleted() {
        return power <= 0;
    }
    
    /**
     * Get the current shield protection amount
     * @return the remaining shield power
     */
    public int getShieldAmount() {
        return power;
    }
}
