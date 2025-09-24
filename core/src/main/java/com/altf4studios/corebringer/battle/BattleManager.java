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

        // Enemy AI action - REMOVED: No longer automatic, only when player ends turn
        // if (turnManager.isEnemyTurn() && !turnManager.isDelaying()) {
        //     turnManager.executeEnemyTurn();
        // }

        // Reflect current turn state on the UI
        if (battleStageUI != null) {
            if (turnManager.isPlayerTurn()) {
                battleStageUI.updateTurnIndicator("Your Turn - Play cards or end turn");
            } else {
                if (enemy.hasStatus("Stun")) {
                    battleStageUI.updateTurnIndicator("Enemy Turn (Stunned)");
                } else {
                    battleStageUI.updateTurnIndicator("Enemy Turn - Enemy is acting...");
                }
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

        // Log game over once
        if (turnManager.shouldLogGameOver()) {
            String winner = getWinner();
            Gdx.app.log("BattleManager", "Game Over! Winner: " + winner);
        }
    }

    public void resetTurns() {
        turnManager.reset();
    }

    public void resetGame() {
        turnManager.resetGame();
    }

    public boolean isGameOver() {
        return turnManager.isGameOver();
    }

    public String getWinner() {
        return turnManager.getWinner();
    }

    public TurnManager getTurnManager() {
        return turnManager;
    }

    /**
     * Manually execute enemy turn - called when player ends their turn
     */
    public void executeEnemyTurn() {
        if (turnManager.isEnemyTurn() && !turnManager.isDelaying()) {
            turnManager.executeEnemyTurn();
        }
    }
}


