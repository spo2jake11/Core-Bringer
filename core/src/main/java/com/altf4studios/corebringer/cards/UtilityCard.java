package com.altf4studios.corebringer.cards;

public class UtilityCard extends Cards {

    public UtilityCard() {}

    @Override
    public Cards copy() {
        UtilityCard copy = new UtilityCard();
        copy.id = this.id;
        copy.name = this.name;
        copy.description = this.description;
        copy.baseEffect = this.baseEffect;
        copy.type = this.type;
        copy.tags = this.tags;
        copy.targetType = this.targetType;
        copy.cost = this.cost;
        copy.codeEffect = this.codeEffect;
        copy.suggestion = this.suggestion;
        return copy;
    }

    @Override
    public String toString() {
        return "[UTILITY] " + name + " (Cost: " + cost + ") -> " + description;
    }
}
