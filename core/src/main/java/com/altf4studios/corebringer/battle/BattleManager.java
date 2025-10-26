package com.altf4studios.corebringer.battle;

import com.altf4studios.corebringer.entities.Enemy;
import com.altf4studios.corebringer.entities.Player;
import com.altf4studios.corebringer.screens.gamescreen.BattleStageUI;
import com.altf4studios.corebringer.turns.TurnManager;
import com.badlogic.gdx.Gdx;

/**
 * Coordinates battle flow and encapsulates the TurnManager so that GameScreen stays lean.
 */
public class BattleManager {
    private final Player player;
    private final Enemy enemy;
    private final BattleStageUI battleStageUI;
    private final TurnManager turnManager;

    public BattleManager(Player player, Enemy enemy, BattleStageUI battleStageUI) {
        this.player = player;
        this.enemy = enemy;
        this.battleStageUI = battleStageUI;
        this.turnManager = new TurnManager(player, enemy);
    }

    /**
     * Advances battle state for this frame and updates turn indicator UI.
     */
    public void update(float deltaTime) {
        // Progress turn timings and staged effects
        turnManager.update(deltaTime);

        // Reflect current turn state on the UI
        if (battleStageUI != null) {
            if (turnManager.isPlayerTurn()) {
                battleStageUI.updateTurnIndicator("Player's Turn");
            } else {
                battleStageUI.updateTurnIndicator("Enemy Turn");
            }

            // Update status badges visibility
            boolean pShield = player.getBlock() > 0;
            boolean pPoison = player.hasPoison();
            boolean pBleed = player.hasStatus("Bleed");
            boolean pStun = player.hasStatus("Stun");
            battleStageUI.updatePlayerStatusBadges(pShield, pPoison, pBleed, pStun);

            boolean eShield = enemy.getBlock() > 0;
            boolean ePoison = enemy.hasPoison();
            boolean eBleed = enemy.hasStatus("Bleed");
            boolean eStun = enemy.hasStatus("Stun");
            battleStageUI.updateEnemyStatusBadges(eShield, ePoison, eBleed, eStun);

            // Update numeric values using StatusManager where available
            com.altf4studios.corebringer.status.StatusManager sm = com.altf4studios.corebringer.status.StatusManager.getInstance();
            int pPoisonStacks = sm.getPower(player, "Poison");
            int ePoisonStacks = sm.getPower(enemy, "Poison");
            int pBleedStacks = sm.getPower(player, "Bleed");
            int eBleedStacks = sm.getPower(enemy, "Bleed");
            int pPoisonDur = sm.getDuration(player, "Poison");
            int ePoisonDur = sm.getDuration(enemy, "Poison");
            int pBleedDur = sm.getDuration(player, "Bleed");
            int eBleedDur = sm.getDuration(enemy, "Bleed");
            int pStunTurns = player.getStatusValue("Stun");
            int eStunTurns = enemy.getStatusValue("Stun");
            battleStageUI.updatePlayerStatusValuesWithDuration(player.getBlock(), pPoisonStacks, pPoisonDur, pBleedStacks, pBleedDur, pStunTurns);
            battleStageUI.updateEnemyStatusValuesWithDuration(enemy.getBlock(), ePoisonStacks, ePoisonDur, eBleedStacks, eBleedDur, eStunTurns);
        }

        if (turnManager.shouldLogGameOver()) {
            String winner = turnManager.getWinner();
            Gdx.app.log("BattleManager", "Game Over! Winner: " + winner);
        }
    }

    /**
     * Manually execute enemy turn - called when player ends their turn
     */
    public void executeEnemyTurn() {
        if (turnManager.isEnemyTurn() && enemy.isAlive() && player.isAlive()) {
            double roll = Math.random();
            if (roll < 0.60) { // attack
                Gdx.app.log("TurnManager", "Enemy attacks (40%)");
                int beforeHp = player.getHp();
                enemy.attack(player);
                int hpLost = Math.max(0, beforeHp - player.getHp());
                if (battleStageUI != null && hpLost > 0) {
                    battleStageUI.showDamageOnPlayer(hpLost);
                }
                turnManager.endEnemyTurn();
            } else if (roll < 0.85) { // defend
                Gdx.app.log("TurnManager", "Enemy defends (35%)");
                int beforeBlock = enemy.getBlock();
                enemy.defend();
                int delta = Math.max(0, enemy.getBlock() - beforeBlock);
                if (battleStageUI != null && delta > 0) {
                    battleStageUI.showShieldOnEnemy(delta);
                }
                turnManager.endEnemyTurn();
            } else { // heal
                int healAmount = Math.max(3, enemy.getMaxHealth() / 10); // at least 3, ~10% max HP
                Gdx.app.log("TurnManager", "Enemy heals for " + healAmount + " HP (25%)");
                int beforeHp = enemy.getHp();
                enemy.heal(healAmount);
                int healed = Math.max(0, enemy.getHp() - beforeHp);
                if (battleStageUI != null && healed > 0) {
                    battleStageUI.showHealOnEnemy(healed);
                }
            }
        }
    }
    // --- Accessors and wrappers restored for compatibility ---
    public TurnManager getTurnManager() {
        return turnManager;
    }

    public void resetTurns() {
        if (turnManager != null) turnManager.reset();
    }

    public void resetGame() {
        if (turnManager != null) turnManager.resetGame();
    }

    public boolean isGameOver() {
        return turnManager != null && turnManager.isGameOver();
    }

    public String getWinner() {
        return (turnManager != null) ? turnManager.getWinner() : null;
    }
    // --- Overhack helpers ---

    /**
     * Instantly kill the current enemy (sets HP to 0).
     */


    /**
     * Ends the player's turn immediately.
     */
    public void endPlayerTurnNow() {
        if (turnManager != null && turnManager.isPlayerTurn()) {
            turnManager.endPlayerTurn();
        }
    }
}

