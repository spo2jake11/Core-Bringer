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
        // Example: Only basic attack/heal logic, expand as needed
        if (card.type.equalsIgnoreCase("attack")) {
            enemy.takeDamage(card.baseEffect);
        } else if (card.type.equalsIgnoreCase("heal")) {
            player.heal(card.baseEffect);
        }
        // Add more card types and effects as needed
    }

    private void executeCard(SampleCardHandler card, Enemy enemy, Player player) {
        // Example: Only basic attack logic for enemy
        if (card.type.equalsIgnoreCase("attack")) {
            player.takeDamage(card.baseEffect);
        }
        // Add more card types and effects as needed
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