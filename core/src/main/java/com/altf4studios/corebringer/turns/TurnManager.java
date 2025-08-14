package com.altf4studios.corebringer.turns;

import com.altf4studios.corebringer.entities.Player;
import com.altf4studios.corebringer.entities.Enemy;
import com.altf4studios.corebringer.screens.gamescreen.SampleCardHandler;
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
            currentPhase = TurnPhase.ENEMY_TURN;
            turnEnded = true;
            isDelaying = true;
            delayTimer = turnDelay;
            Gdx.app.log("TurnManager", "Player turn ended, switching to enemy turn");
            // Apply poison effects at the start of enemy turn
            if (enemy.hasPoison()) {
                enemy.applyPoisonEffects();
            }
        }
    }

    public void endEnemyTurn() {
        if (currentPhase == TurnPhase.ENEMY_TURN) {
            currentPhase = TurnPhase.PLAYER_TURN;
            turnEnded = true;
            isDelaying = true;
            delayTimer = turnDelay;
            Gdx.app.log("TurnManager", "Enemy turn ended, switching to player turn");
            // Apply poison effects at the start of player turn
            if (player.hasPoison()) {
                player.applyPoisonEffects();
            }
        }
    }

    public void executeEnemyTurn() {
        if (currentPhase == TurnPhase.ENEMY_TURN && enemy.isAlive() && player.isAlive() && !isDelaying) {
            Gdx.app.log("TurnManager", "Executing enemy turn - enemy attacks player");
            // Simple enemy attack using the enemy's attack method
            enemy.attack(player);
            
            // End enemy turn after attacking
            endEnemyTurn();
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
