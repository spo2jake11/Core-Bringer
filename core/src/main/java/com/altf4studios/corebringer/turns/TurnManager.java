package com.altf4studios.corebringer.turns;

import com.altf4studios.corebringer.entities.Player;
import com.altf4studios.corebringer.entities.Enemy;
import com.badlogic.gdx.Gdx;

public class TurnManager {
    public enum TurnPhase {
        PLAYER_TURN,
        ENEMY_TURN
    }

    private TurnPhase currentPhase;
    private Player player;
    private Enemy enemy;
    private boolean turnEnded = false;

    // Turn delay system
    private float turnDelay = 1.0f; // 1 second delay between turns
    private float delayTimer = 0.0f;
    private boolean isDelaying = false;

    // Game over logging control
    private boolean gameOverLogged = false;

    // Poison resolution staging to make damage visible between turns
    private enum PendingPoisonTarget { NONE, PLAYER, ENEMY }
    private PendingPoisonTarget pendingPoisonTarget = PendingPoisonTarget.NONE;
    private boolean isResolvingPoison = false;
    private float poisonResolutionDelay = 0f; // brief delay to show poison damage

    public TurnManager(Player player, Enemy enemy) {
        this.player = player;
        this.enemy = enemy;
        this.currentPhase = TurnPhase.PLAYER_TURN;
    }

    public TurnPhase getCurrentPhase() {
        return currentPhase;
    }

    public boolean isPlayerTurn() {
        return currentPhase == TurnPhase.PLAYER_TURN;
    }

    public boolean isEnemyTurn() {
        return currentPhase == TurnPhase.ENEMY_TURN;
    }

    public void endPlayerTurn() {
        if (currentPhase == TurnPhase.PLAYER_TURN) {
            // Clear enemy shield at end of player's turn
            if (enemy != null) enemy.clearBlock();
            currentPhase = TurnPhase.ENEMY_TURN;
            turnEnded = true;
//            isDelaying = true;
//            delayTimer = turnDelay;
            Gdx.app.log("TurnManager", "Player turn ended, switching to enemy turn");
            // After delay, resolve start-of-turn statuses for enemy (poison/bleed/stun)
            pendingPoisonTarget = PendingPoisonTarget.ENEMY;
            isResolvingPoison = false;
        }
    }

    public void endEnemyTurn() {
        if (currentPhase == TurnPhase.ENEMY_TURN) {
            // Clear player shield at end of enemy's turn
            if (player != null) player.clearBlock();
            currentPhase = TurnPhase.PLAYER_TURN;
            turnEnded = true;
            // Remove these lines to eliminate delay:
            // isDelaying = true;
            // delayTimer = turnDelay;
            Gdx.app.log("TurnManager", "Enemy turn ended, switching to player turn");
            // After delay, resolve start-of-turn statuses for player (poison/bleed/stun)
            pendingPoisonTarget = PendingPoisonTarget.PLAYER;
            isResolvingPoison = false;
        }
    }

    public void executeEnemyTurn() {
        if (currentPhase == TurnPhase.ENEMY_TURN && enemy.isAlive() && player.isAlive() && !isDelaying) {
            // Stun check: if stunned, decrement and skip action
            int stunTurns = enemy.getStatusValue("Stun");
            if (stunTurns > 0) {
                Gdx.app.log("TurnManager", "Enemy is stunned and skips the turn (" + stunTurns + " turns remaining)");
                enemy.decrementStatus("Stun", 1);
                endEnemyTurn();
                return;
            }
            // Randomize action: 40% attack, 35% defend, 25% heal
            double roll = Math.random();
            if (roll < 0.60) {
                Gdx.app.log("TurnManager", "Enemy attacks (40%)");
                enemy.attack(player);
                endEnemyTurn();
            } else if (roll < 0.6 + 0.25) { // 0.75
                Gdx.app.log("TurnManager", "Enemy defends (35%)");
                enemy.defend();
                endEnemyTurn();
            } else {
                // Heal branch (25%)
                int healAmount = Math.max(3, enemy.getMaxHealth() / 10); // at least 3, ~10% max HP
                Gdx.app.log("TurnManager", "Enemy heals for " + healAmount + " HP (25%)");
                enemy.heal(healAmount);
                endEnemyTurn();
            }
        }
    }

    public void reset() {
        currentPhase = TurnPhase.PLAYER_TURN;
        turnEnded = false;
    }

    public void resetGame() {
        // Reset player and enemy HP to full
        player.setHp(player.getMaxHealth());
        enemy.setHp(enemy.getMaxHealth());

        // Reset turn system
        reset();

        // Reset delay system
        isDelaying = false;
        delayTimer = 0.0f;

        // Reset game over logging
        gameOverLogged = false;

        Gdx.app.log("TurnManager", "Game reset - Player HP: " + player.getHp() + ", Enemy HP: " + enemy.getHp());
    }

    public boolean isTurnEnded() {
        return turnEnded;
    }

    public void resetTurnEnded() {
        turnEnded = false;
    }

    public boolean isGameOver() {
        return !player.isAlive() || !enemy.isAlive();
    }

    public boolean shouldLogGameOver() {
        if (isGameOver() && !gameOverLogged) {
            gameOverLogged = true;
            return true;
        }
        return false;
    }

    public String getWinner() {
        if (!player.isAlive()) {
            return "Enemy";
        } else if (!enemy.isAlive()) {
            return "Player";
        }
        return null; // Game is still ongoing
    }

    public void update(float deltaTime) {
        if (isDelaying) {
            delayTimer -= deltaTime;
            if (delayTimer <= 0) {
                isDelaying = false;
                delayTimer = 0.0f;
                turnEnded = false;
            }
        }

        // After initial delay, resolve scheduled start-of-turn statuses and add a short delay so it's visible
        if (!isDelaying && pendingPoisonTarget != PendingPoisonTarget.NONE) {
            com.altf4studios.corebringer.status.StatusManager sm = com.altf4studios.corebringer.status.StatusManager.getInstance();
            if (pendingPoisonTarget == PendingPoisonTarget.ENEMY) {
                Gdx.app.log("TurnManager", "Resolving enemy start-of-turn statuses");
                // Process all statuses (Poison, Bleed, Stun effects via their handlers)
                sm.processTurnStart(enemy);
            } else if (pendingPoisonTarget == PendingPoisonTarget.PLAYER) {
                Gdx.app.log("TurnManager", "Resolving player start-of-turn statuses");
                sm.processTurnStart(player);
            }
            pendingPoisonTarget = PendingPoisonTarget.NONE;
            isResolvingPoison = true;
            isDelaying = true;
            delayTimer = poisonResolutionDelay;
        }

        // Clear resolving flag after poison delay completes
        if (!isDelaying && isResolvingPoison) {
            isResolvingPoison = false;
        }
    }

    public boolean isDelaying() {
        return isDelaying;
    }

    public void setTurnDelay(float delay) {
        this.turnDelay = Math.max(0.1f, delay); // Minimum 0.1 second delay
    }

    public float getTurnDelay() {
        return turnDelay;
    }
}
