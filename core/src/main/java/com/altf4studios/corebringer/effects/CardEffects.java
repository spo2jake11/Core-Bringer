package com.altf4studios.corebringer.effects;

public interface CardEffects {
    void apply();

    public enum CardEffectType {
        INCREASE_ATK,
        APPLY_STATUS,
        ATTACK,
        DEFENSE,
        COST,
        MULTIHIT,
        REMOVE_STATUS,
    }


}
