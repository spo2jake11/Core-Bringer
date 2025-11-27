// ...new file...
package com.altf4studios.corebringer.battle;

/**
 * Simple DTO describing boss modifiers applied when entering a boss fight.
 */
public class BossModifier {
    // Multipliers (nullable)
    public Float bossMaxHpMultiplier; // multiply boss max HP
    public Float bossAttackMultiplier; // multiply boss attack damage
    public Float playerCardEffectMultiplier; // multiply player's card effects (can be <1)

    // Optional explicit probabilities for enemy action pattern (sum will be normalized)
    public Float attackProb;
    public Float defendProb;
    public Float healProb;

    // Human-facing text selected from JSON reward (for UI display/debug)
    public String selectedRewardText;

    public BossModifier() {}

    public static BossModifier createCompleteR1() {
        BossModifier m = new BossModifier();
        m.bossMaxHpMultiplier = 0.5f;
        return m;
    }
    public static BossModifier createCompleteR2() {
        BossModifier m = new BossModifier();
        m.bossAttackMultiplier = 0.5f;
        return m;
    }
    public static BossModifier createCompleteR3() {
        BossModifier m = new BossModifier();
        m.playerCardEffectMultiplier = 1.5f;
        return m;
    }

    public static BossModifier createFailedR1() {
        BossModifier m = new BossModifier();
        m.bossMaxHpMultiplier = 1.5f;
        return m;
    }
    public static BossModifier createFailedR2() {
        BossModifier m = new BossModifier();
        m.attackProb = 0.8f;
        m.defendProb = 0.1f;
        m.healProb = 0.1f;
        return m;
    }
    public static BossModifier createFailedR3() {
        BossModifier m = new BossModifier();
        m.attackProb = 0.2f;
        m.defendProb = 0.7f;
        m.healProb = 0.1f;
        return m;
    }
    public static BossModifier createFailedR4() {
        BossModifier m = new BossModifier();
        m.playerCardEffectMultiplier = 0.5f;
        return m;
    }

    /**
     * Factory: build modifier from branch ("complete"/"failed") and reward key (r1,r2...)
     */
    public static BossModifier fromBranchAndKey(String branch, String key) {
        if (branch == null || key == null) return null;
        branch = branch.toLowerCase();
        key = key.toLowerCase();
        try {
            if (branch.equals("complete")) {
                switch (key) {
                    case "r1": return createCompleteR1();
                    case "r2": return createCompleteR2();
                    case "r3": return createCompleteR3();
                    default: return null;
                }
            } else if (branch.equals("failed")) {
                switch (key) {
                    case "r1": return createFailedR1();
                    case "r2": return createFailedR2();
                    case "r3": return createFailedR3();
                    case "r4": return createFailedR4();
                    default: return null;
                }
            }
        } catch (Exception ignored) {}
        return null;
    }
}

