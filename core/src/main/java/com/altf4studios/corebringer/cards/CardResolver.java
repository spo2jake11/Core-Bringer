package com.altf4studios.corebringer.cards;

import com.altf4studios.corebringer.effects.CardEffectType;
import com.altf4studios.corebringer.entities.Entity;
import com.altf4studios.corebringer.entities.Player;
// Removed JShell-based custom effect executor
import com.altf4studios.corebringer.utils.CombatLog;

public class CardResolver {
    public void resolve(Cards card, Entity user, Entity target) {
        System.out.println("Resolving card: " + card.name + " on target: " + target.getName());
        
        // Log card play
        CombatLog.logCard(user.getName(), card.name, "plays card");

        for (CardEffectType effectType : card.effectTypes) {
            switch (effectType) {
                case DAMAGE:
                    int damage = user.applyDamageModifiers(target, card.baseDamage);
                    System.out.println("Applying " + damage + " damage to " + target.getName());
                    target.takeDamage(damage);
                    CombatLog.logAttack(user.getName(), target.getName(), damage);
                    break;
                case HEAL:
                    if (user instanceof Player) {
                        System.out.println("Healing " + user.getName() + " for " + card.baseHeal);
                        ((Player) user).heal(card.baseHeal);
                        CombatLog.logInfo(user.getName() + " heals for " + card.baseHeal + " HP");
                    }
                    break;
                case BLOCK:
                    System.out.println("Adding " + card.baseBlock + " block to " + user.getName());
                    user.gainBlock(card.baseBlock);
                    CombatLog.logDefense(user.getName(), card.baseBlock);
                    break;
                case APPLY_STATUS:
                    System.out.println("Applying status " + card.statusName + " to " + target.getName());
                    target.addStatus(card.statusName, card.statusAmount);
                    CombatLog.logStatus(target.getName(), card.statusName, "Duration: " + card.statusAmount);
                    break;
                case ENERGY_GAIN:
                    if (user instanceof Player) {
                        System.out.println("Gaining " + card.energyAmount + " energy for " + user.getName());
                        ((Player) user).restoreEnergy(card.energyAmount);
                        CombatLog.logInfo(user.getName() + " gains " + card.energyAmount + " energy");
                    }
                    break;
                case CUSTOM:
                    // Custom code effects disabled without interpreter
                    CombatLog.logInfo("Custom effect execution is disabled");
                    break;
            }
        }
        System.out.println("Card " + card.name + " resolved.");
    }
} 