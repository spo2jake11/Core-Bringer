package com.altf4studios.corebringer.status;

import com.altf4studios.corebringer.entities.Entity;
import com.badlogic.gdx.Gdx;

public class Heal extends StatusEffect {
    
    public Heal(int power, int duration) {
        super("Heal", power, duration);
    }
    
    public Heal(String name, int power, int duration) {
        super(name, power, duration);
    }
    
    @Override
    public void onApply() {
        // Log when heal is applied
        Gdx.app.log("Heal", "Heal applied with " + power + " healing per turn for " + duration + " turns");
    }
    
    @Override
    public void onTurnStart(Entity target) {
        // Apply healing at the start of each turn
        if (target != null && target.isAlive() && power > 0) {
            // Calculate healing amount (don't exceed max HP)
            int currentHp = target.getHp();
            int maxHp = target.getMaxHealth();
            int healingAmount = Math.min(power, maxHp - currentHp);
            
            if (healingAmount > 0) {
                // Apply healing
                target.setHp(currentHp + healingAmount);
                
                // Log the healing
                Gdx.app.log("Heal", target.getName() + " healed for " + healingAmount + " HP. Current HP: " + target.getHp() + "/" + maxHp);
            } else {
                Gdx.app.log("Heal", target.getName() + " is already at full health");
            }
        }
        
        // Reduce duration
        tick();
    }
    
    @Override
    public void onExpire(Entity target) {
        Gdx.app.log("Heal", "Heal effect expired for " + target.getName());
    }
    
    @Override
    public void onRemove(Entity target) {
        Gdx.app.log("Heal", "Heal removed from " + target.getName());
    }
    
    @Override
    public void increasePower(int amount) {
        super.increasePower(amount);
        Gdx.app.log("Heal", "Heal power increased to: " + power + " healing per turn");
    }
    
    @Override
    public void extendDuration(int amount) {
        super.extendDuration(amount);
        Gdx.app.log("Heal", "Heal duration extended to: " + duration + " turns");
    }
    
    /**
     * Check if the heal effect has expired
     * @return true if duration is 0 or less
     */
    public boolean isExpired() {
        return duration <= 0;
    }
    
    /**
     * Get the current healing amount per turn
     * @return the healing amount
     */
    public int getHealAmount() {
        return power;
    }
    
    /**
     * Get the remaining heal duration
     * @return the number of turns remaining
     */
    public int getHealDuration() {
        return duration;
    }
    
    /**
     * Stack heal effects (increase power and extend duration)
     * @param additionalPower additional healing to add
     * @param additionalDuration additional turns to add
     */
    public void stackHeal(int additionalPower, int additionalDuration) {
        increasePower(additionalPower);
        extendDuration(additionalDuration);
        Gdx.app.log("Heal", "Heal stacked: " + power + " healing per turn for " + duration + " turns");
    }
    
    /**
     * Check if the target is at full health
     * @param target The entity to check
     * @return true if the entity is at maximum HP
     */
    public boolean isTargetAtFullHealth(Entity target) {
        return target != null && target.getHp() >= target.getMaxHealth();
    }
    
    /**
     * Get the effective healing amount (considering max HP)
     * @param target The entity to heal
     * @return the actual healing amount that would be applied
     */
    public int getEffectiveHealing(Entity target) {
        if (target == null) return 0;
        int currentHp = target.getHp();
        int maxHp = target.getMaxHealth();
        return Math.min(power, maxHp - currentHp);
    }
}
