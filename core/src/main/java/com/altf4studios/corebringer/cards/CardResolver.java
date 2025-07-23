package com.altf4studios.corebringer.cards;

import com.altf4studios.corebringer.effects.CardEffectType;
import com.altf4studios.corebringer.entities.Entity;
import com.altf4studios.corebringer.entities.Player;
import com.altf4studios.corebringer.interpreter.JShellExecutor;

public class CardResolver {
    public void resolve(Cards card, Entity user, Entity target) {
        System.out.println("Resolving card: " + card.name + " on target: " + target.getName());

        for (CardEffectType effectType : card.effectTypes) {
            switch (effectType) {
                case DAMAGE:
                    System.out.println("Applying " + card.baseDamage + " damage to " + target.getName());
                    target.takeDamage(user.applyDamageModifiers(target, card.baseDamage));
                    break;
                case HEAL:
                    if (user instanceof Player) {
                        System.out.println("Healing " + user.getName() + " for " + card.baseHeal);
                        ((Player) user).heal(card.baseHeal);
                    }
                    break;
                case BLOCK:
                    System.out.println("Adding " + card.baseBlock + " block to " + user.getName());
                    user.gainBlock(card.baseBlock);
                    break;
                case APPLY_STATUS:
                    System.out.println("Applying status " + card.statusName + " to " + target.getName());
                    target.addStatus(card.statusName, card.statusAmount);
                    break;
                case ENERGY_GAIN:
                    if (user instanceof Player) {
                        System.out.println("Gaining " + card.energyAmount + " energy for " + user.getName());
                        ((Player) user).restoreEnergy(card.energyAmount);
                    }
                    break;
                case CUSTOM:
                    if (card.codeEffect != null && !card.codeEffect.isEmpty()) {
                        JShellExecutor.runScript(card.codeEffect, user, target);
                    }
                    break;
            }
        }
        System.out.println("Card " + card.name + " resolved.");
    }
} 