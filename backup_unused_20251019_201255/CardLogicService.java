package com.altf4studios.corebringer.cards;

import com.altf4studios.corebringer.entities.Enemy;
import com.altf4studios.corebringer.entities.Player;
import com.altf4studios.corebringer.screens.gamescreen.SampleCardHandler;
import com.badlogic.gdx.Gdx;

public class CardLogicService {
    public void applyCardEffect(SampleCardHandler card, Player player, Enemy enemy) {
        if (card == null) return;

        boolean hasAttackEffect = card.description != null &&
            (card.description.toLowerCase().contains("deal") || card.description.toLowerCase().contains("damage"));
        boolean hasShieldEffect = card.description != null &&
            card.description.toLowerCase().contains("gain") && card.description.toLowerCase().contains("shield");

        if ("ATTACK".equalsIgnoreCase(card.type)) {
            int damage = card.baseEffect;
            if (enemy != null && enemy.isAlive()) enemy.takeDamage(damage);
            if (hasShieldEffect) {
                int shieldFromDesc = parseNumberBeforeKeyword(card.description, "shield");
                if (shieldFromDesc > 0 && player != null && player.isAlive()) player.gainBlock(shieldFromDesc);
            }
        } else if ("DEFENSE".equalsIgnoreCase(card.type)) {
            int shieldFromDesc = parseNumberBeforeKeyword(card.description, "shield");
            int block = shieldFromDesc > 0 ? shieldFromDesc : card.baseEffect;
            if (player != null && player.isAlive()) player.gainBlock(block);
            if (hasAttackEffect) {
                int damage = card.baseEffect;
                if (damage > 0 && enemy != null && enemy.isAlive()) enemy.takeDamage(damage);
            }
        } else if ("DEBUFF".equalsIgnoreCase(card.type)) {
            Gdx.app.log("CardEffect", "Debuff applied: " + card.name);
        } else if ("BUFF".equalsIgnoreCase(card.type)) {
            // If a BUFF card is actually a heal card, log it as heal instead of generic buff
            if (card.id != null && card.id.toLowerCase().contains("heal")) {
                int healLogAmount = parseNumberBeforeKeyword(card.id, "heal");
                if (healLogAmount <= 0) healLogAmount = Math.max(0, card.baseEffect);
                Gdx.app.log("CardEffect", "Card '" + card.name + "' used! Heal: " + healLogAmount + " HP");
            } else {
                Gdx.app.log("CardEffect", "Buff applied: " + card.name);
            }
        }


        // Heal
        if (card.description != null && card.description.toLowerCase().contains("heal")) {
            int healAmount = parseNumberBeforeKeyword(card.description, "heal");
            if (healAmount <= 0) healAmount = Math.max(0, card.baseEffect);
            if (player != null && player.isAlive() && healAmount > 0) {
                // Instant heal on use
                player.heal(healAmount);
                Gdx.app.log("CardEffect", "Healed " + healAmount + " HP using '" + card.name + "'");
            }
        }


        /* // Stun (commented out per request)
        if (card.description != null && card.description.toLowerCase().contains("stun")) {
            int stunDuration = parseNumberBeforeKeyword(card.description, "stun");
            if (stunDuration <= 0) stunDuration = Math.max(0, card.baseEffect);
            if (enemy != null && enemy.isAlive() && stunDuration > 0) enemy.addStatus("Stun", stunDuration);
        }
        */
    }

    // Extract the integer immediately before keyword
    public int parseNumberBeforeKeyword(String text, String keyword) {
        if (text == null || keyword == null) return 0;
        String lower = text.toLowerCase();
        String[] words = lower.split("\\s+");
        for (int i = 0; i < words.length; i++) {
            if (words[i].startsWith(keyword.toLowerCase())) {
                if (i > 0) {
                    try {
                        return Integer.parseInt(words[i - 1].replaceAll("[^0-9]", ""));
                    } catch (NumberFormatException ignored) {}
                }
                return 0;
            }
        }
        return 0;
    }
}


