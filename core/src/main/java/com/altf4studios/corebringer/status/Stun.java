package com.altf4studios.corebringer.status;

import com.altf4studios.corebringer.entities.Entity;
import com.badlogic.gdx.Gdx;

public class Stun extends StatusEffect {
    
    public Stun(int duration) {
        super("Stun", 0, duration); // Stun doesn't use power, only duration
    }
    
    public Stun(String name, int duration) {
        super(name, 0, duration);
    }
    
    @Override
    public void onApply() {
        // Log when stun is applied
        Gdx.app.log("Stun", "Stun applied for " + duration + " turns");
    }
    
    @Override
    public void onTurnStart(Entity target) {
        // Stunned entities cannot take actions
        Gdx.app.log("Stun", target.getName() + " is stunned and cannot act this turn");
        
        // Reduce duration
        tick();
    }
    
    @Override
    public void onExpire(Entity target) {
        Gdx.app.log("Stun", target.getName() + " is no longer stunned");
    }
    
    @Override
    public void onRemove(Entity target) {
        Gdx.app.log("Stun", "Stun removed from " + target.getName());
    }
    
    /**
     * Check if the entity is currently stunned
     * @return true if stun duration is greater than 0
     */
    public boolean isStunned() {
        return duration > 0;
    }
    
    /**
     * Check if the entity can act (not stunned)
     * @return true if the entity can take actions
     */
    public boolean canAct() {
        return !isStunned();
    }
    
    /**
     * Get the remaining stun duration
     * @return the number of turns remaining stunned
     */
    public int getStunDuration() {
        return duration;
    }
    
    /**
     * Extend stun duration (useful for stacking stuns)
     * @param additionalDuration additional turns to add
     */
    public void extendStun(int additionalDuration) {
        extendDuration(additionalDuration);
        Gdx.app.log("Stun", "Stun duration extended to: " + duration + " turns");
    }
}
