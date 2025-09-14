package com.altf4studios.corebringer.status;

import com.altf4studios.corebringer.entities.Entity;
import com.badlogic.gdx.Gdx;

public class Bleed extends StatusEffect {
    
    public Bleed(int power, int duration) {
        super("Bleed", power, duration);
    }
    
    public Bleed(String name, int power, int duration) {
        super(name, power, duration);
    }
    
    @Override
    public void onApply() {
        // Log when bleed is applied
        Gdx.app.log("Bleed", "Bleed applied with " + power + " damage per turn for " + duration + " turns");
    }
    
    @Override
    public void onTurnStart(Entity target) {
        // Apply bleed damage at the start of each turn
        if (target != null && target.isAlive() && power > 0) {
            // Apply bleed damage equal to current power
            target.takeDamage(power);
            
            // Log the bleed damage
            Gdx.app.log("Bleed", target.getName() + " took " + power + " bleed damage. Remaining HP: " + target.getHp());
            
            // Bleed damage doesn't decrease over time (unlike poison)
            // Only duration decreases
        }
        
        // Reduce duration
        tick();
    }
    
    @Override
    public void onExpire(Entity target) {
        Gdx.app.log("Bleed", "Bleed effect expired for " + target.getName());
    }
    
    @Override
    public void onRemove(Entity target) {
        Gdx.app.log("Bleed", "Bleed removed from " + target.getName());
    }
    
    @Override
    public void increasePower(int amount) {
        super.increasePower(amount);
        Gdx.app.log("Bleed", "Bleed power increased to: " + power + " damage per turn");
    }
    
    @Override
    public void extendDuration(int amount) {
        super.extendDuration(amount);
        Gdx.app.log("Bleed", "Bleed duration extended to: " + duration + " turns");
    }
    
    /**
     * Check if the bleed effect has expired
     * @return true if duration is 0 or less
     */
    public boolean isExpired() {
        return duration <= 0;
    }
    
    /**
     * Get the current bleed damage per turn
     * @return the bleed damage amount
     */
    public int getBleedDamage() {
        return power;
    }
    
    /**
     * Get the remaining bleed duration
     * @return the number of turns remaining
     */
    public int getBleedDuration() {
        return duration;
    }
    
    /**
     * Stack bleed effects (increase power and extend duration)
     * @param additionalPower additional bleed damage to add
     * @param additionalDuration additional turns to add
     */
    public void stackBleed(int additionalPower, int additionalDuration) {
        increasePower(additionalPower);
        extendDuration(additionalDuration);
        Gdx.app.log("Bleed", "Bleed stacked: " + power + " damage per turn for " + duration + " turns");
    }
}
