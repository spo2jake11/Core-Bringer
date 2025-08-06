package com.altf4studios.corebringer.turns;

import com.altf4studios.corebringer.entities.Player;
import com.altf4studios.corebringer.entities.Enemy;
import com.altf4studios.corebringer.screens.gamescreen.SampleCardHandler;
import java.util.Queue;
import java.util.LinkedList;

public class TurnManager {
    public enum TurnPhase {
        PLAYER_START,
        PLAYER_ACTION,
        PLAYER_END,
        ENEMY_START,
        ENEMY_ACTION,
        ENEMY_END,
    }

    private TurnPhase currentPhase;
    private Queue<SampleCardHandler> playerCardQueue;
    private Queue<SampleCardHandler> enemyCardQueue;
    private Player player;
    private Enemy enemy;

    // Slot queues for player
    private Queue<SampleCardHandler> basicSlotQueue = new LinkedList<>();
    private Queue<SampleCardHandler> combineSlotQueue = new LinkedList<>();
    private Queue<SampleCardHandler> extendSlotQueue = new LinkedList<>();

    public TurnManager(Player player, Enemy enemy) {
        this.player = player;
        this.enemy = enemy;
        this.currentPhase = TurnPhase.PLAYER_START;
        this.playerCardQueue = new LinkedList<>();
        this.enemyCardQueue = new LinkedList<>();
    }

    public TurnPhase getCurrentPhase() {
        return currentPhase;
    }

    public void nextPhase() {
        switch (currentPhase) {
            case PLAYER_START:
                // Setup for player turn (e.g., draw cards, reset energy)
                // Apply poison effects at the start of player turn
                if (player.hasPoison()) {
                    player.applyPoisonEffects();
                }
                currentPhase = TurnPhase.PLAYER_ACTION;
                break;
            case PLAYER_ACTION:
                currentPhase = TurnPhase.PLAYER_END;
                break;
            case PLAYER_END:
                currentPhase = TurnPhase.ENEMY_START;
                break;
            case ENEMY_START:
                // Setup for enemy turn (e.g., choose actions)
                // Apply poison effects at the start of enemy turn
                if (enemy.hasPoison()) {
                    enemy.applyPoisonEffects();
                }
                currentPhase = TurnPhase.ENEMY_ACTION;
                break;
            case ENEMY_ACTION:
                currentPhase = TurnPhase.ENEMY_END;
                break;
            case ENEMY_END:
                currentPhase = TurnPhase.PLAYER_START;
                break;
        }
    }

    public void queuePlayerCard(SampleCardHandler card) {
        playerCardQueue.add(card);
    }

    public void queueEnemyCard(SampleCardHandler card) {
        enemyCardQueue.add(card);
    }

    public void executeNextCard() {
        if (currentPhase == TurnPhase.PLAYER_ACTION && !playerCardQueue.isEmpty()) {
            SampleCardHandler card = playerCardQueue.poll();
            executeCard(card, player, enemy);
        } else if (currentPhase == TurnPhase.ENEMY_ACTION && !enemyCardQueue.isEmpty()) {
            SampleCardHandler card = enemyCardQueue.poll();
            executeCard(card, enemy, player);
        }
    }

    private void executeCard(SampleCardHandler card, Player player, Enemy enemy) {
        // Handle different card types based on the cards.json structure
        if (card.type.equalsIgnoreCase("ATTACK")) {
            enemy.takeDamage(card.baseEffect);
        } else if (card.type.equalsIgnoreCase("DEFENSE")) {
            player.gainBlock(card.baseEffect);
        } else if (card.type.equalsIgnoreCase("BUFF")) {
            // Handle buff effects - for now just log them
            if (card.tags != null && card.tags.contains("INCREASE_ATTACK", false)) {
                // TODO: Implement attack buffing
            } else if (card.tags != null && card.tags.contains("APPLY_STATS", false)) {
                // TODO: Implement stat buffing
            }
        } else if (card.type.equalsIgnoreCase("DEBUFF")) {
            // Handle debuff effects - for now just log them
        }

        // Check for poison application in card description
        if (card.description != null && card.description.toLowerCase().contains("poison")) {
            int poisonPower = 5; // Default poison power
            int poisonDuration = 3; // Default duration
            
            // Try to extract poison power from description
            if (card.description.contains("apply") && card.description.contains("poison")) {
                String[] words = card.description.split("\\s+");
                for (int i = 0; i < words.length; i++) {
                    if (words[i].toLowerCase().contains("poison") && i > 0) {
                        try {
                            poisonPower = Integer.parseInt(words[i-1]);
                            break;
                        } catch (NumberFormatException e) {
                            // Use default value
                        }
                    }
                }
            }
            
            com.altf4studios.corebringer.status.Poison poison = new com.altf4studios.corebringer.status.Poison("Poison", poisonPower, poisonDuration);
            enemy.addPoison(poison);
        }
    }

    private void executeCard(SampleCardHandler card, Enemy enemy, Player player) {
        // Handle different card types for enemy actions
        if (card.type.equalsIgnoreCase("ATTACK")) {
            player.takeDamage(card.baseEffect);
        } else if (card.type.equalsIgnoreCase("DEFENSE")) {
            enemy.gainBlock(card.baseEffect);
        } else if (card.type.equalsIgnoreCase("BUFF")) {
            // Handle buff effects for enemy - for now just log them
        } else if (card.type.equalsIgnoreCase("DEBUFF")) {
            // Handle debuff effects for enemy - for now just log them
        }

        // Check for poison application in card description (enemy applying poison to player)
        if (card.description != null && card.description.toLowerCase().contains("poison")) {
            int poisonPower = 5; // Default poison power
            int poisonDuration = 3; // Default duration
            
            // Try to extract poison power from description
            if (card.description.contains("apply") && card.description.contains("poison")) {
                String[] words = card.description.split("\\s+");
                for (int i = 0; i < words.length; i++) {
                    if (words[i].toLowerCase().contains("poison") && i > 0) {
                        try {
                            poisonPower = Integer.parseInt(words[i-1]);
                            break;
                        } catch (NumberFormatException e) {
                            // Use default value
                        }
                    }
                }
            }
            
            com.altf4studios.corebringer.status.Poison poison = new com.altf4studios.corebringer.status.Poison("Poison", poisonPower, poisonDuration);
            player.addPoison(poison);
        }
    }

    public void reset() {
        playerCardQueue.clear();
        enemyCardQueue.clear();
        currentPhase = TurnPhase.PLAYER_START;
    }

    // Add card to a specific slot queue
    public void addToBasicSlot(SampleCardHandler card) {
        basicSlotQueue.add(card);
    }
    public void addToCombineSlot(SampleCardHandler card) {
        combineSlotQueue.add(card);
    }
    public void addToExtendSlot(SampleCardHandler card) {
        extendSlotQueue.add(card);
    }
    // Getters for slot queues
    public Queue<SampleCardHandler> getBasicSlotQueue() { return basicSlotQueue; }
    public Queue<SampleCardHandler> getCombineSlotQueue() { return combineSlotQueue; }
    public Queue<SampleCardHandler> getExtendSlotQueue() { return extendSlotQueue; }
} 