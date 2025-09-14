package com.altf4studios.corebringer.status;

public class BlockStatus extends StatusEffect {
    public BlockStatus(int power, int duration) {
        super("Block", power, duration);
    }
    @Override
    public void onApply() {
        // Custom logic for block effect
    }
}
